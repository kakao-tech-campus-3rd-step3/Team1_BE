package knu.team1.be.boost.notification.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.notification.entity.Notification;
import knu.team1.be.boost.notification.repository.NotificationRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.repository.TaskRepository;
import knu.team1.be.boost.task.repository.TaskRepository.DueTask;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void notifyTaskReview(Project project, Task task) {
        Set<Member> assignees = task.getAssignees();

        List<Member> members = project.getProjectMemberships().stream()
            .filter(ProjectMembership::isNotificationEnabled)
            .map(ProjectMembership::getMember)
            .filter(member -> !assignees.contains(member))
            .toList();

        for (Member member : members) {
            sendNotification(
                member,
                "작업 검토 요청",
                String.format("[%s] 작업이 검토 중 상태로 변경되었습니다.", task.getTitle())
            );
        }
    }

    @Transactional
    public void notifyTaskApprove(Project project, Task task) {
        List<Member> assignees = project.getProjectMemberships().stream()
            .filter(
                pm -> task.getAssignees().contains(pm.getMember()) && pm.isNotificationEnabled())
            .map(ProjectMembership::getMember)
            .toList();

        for (Member assignee : assignees) {
            sendNotification(
                assignee,
                "작업 승인 완료",
                String.format("[%s] 작업이 모든 승인자를 통해 승인되었습니다.", task.getTitle())
            );
        }
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    public void notifyDueTomorrowTasks() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<DueTask> dueTasks = taskRepository.findDueTasksByMember(tomorrow);

        Map<UUID, Map<String, List<String>>> tasksByMember = new HashMap<>();

        for (DueTask dueTask : dueTasks) {
            tasksByMember
                .computeIfAbsent(dueTask.getMemberId(), k -> new HashMap<>())
                .computeIfAbsent(dueTask.getProjectName(), k -> new ArrayList<>())
                .add(dueTask.getTaskTitle());
        }

        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern("MM월 dd일"));

        for (Map.Entry<UUID, Map<String, List<String>>> memberEntry : tasksByMember.entrySet()) {
            Member member = memberRepository.findById(memberEntry.getKey())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.MEMBER_NOT_FOUND, "memberId: " + memberEntry.getKey()
                    )
                );

            StringBuilder message = new StringBuilder(formattedDate + "마감 임박 작업\n");

            for (Map.Entry<String, List<String>> projectEntry : memberEntry.getValue().entrySet()) {
                String projectName = projectEntry.getKey();
                String taskTitles = String.join(", ", projectEntry.getValue());
                message.append("[").append(projectName).append("] ").append(taskTitles)
                    .append("\n");
            }

            sendNotification(member, "마감 임박 작업 알림", message.toString());
        }
    }

    private void sendNotification(Member member, String title, String message) {
        Notification notification = Notification.builder()
            .member(member)
            .title(title)
            .message(message)
            .build();

        notificationRepository.save(notification);
    }

}
