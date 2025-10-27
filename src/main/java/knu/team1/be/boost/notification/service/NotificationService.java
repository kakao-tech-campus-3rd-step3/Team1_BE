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
import knu.team1.be.boost.webPush.service.WebPushClient;
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

    private final WebPushClient webPushClient;

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

        Map<UUID, Map<String, List<String>>> tasksByMember = groupTasksByMember(dueTasks);
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern("MM월 dd일"));

        tasksByMember.forEach((memberId, projectTasks) -> {
            Member member = findMember(memberId);
            String message = buildNotificationMessage(projectTasks);
            String title = formattedDate + " 마감 임박 작업";
            sendNotification(member, title, message);
        });
    }

    private void sendNotification(Member member, String title, String message) {
        Notification notification = Notification.builder()
            .member(member)
            .title(title)
            .message(message)
            .build();

        notificationRepository.save(notification);

        webPushClient.sendNotification(member, title, message);
    }

    private Map<UUID, Map<String, List<String>>> groupTasksByMember(List<DueTask> dueTasks) {
        Map<UUID, Map<String, List<String>>> tasksByMember = new HashMap<>();

        for (DueTask dueTask : dueTasks) {
            UUID memberId = dueTask.getMemberId();
            String projectName = dueTask.getProjectName();
            String taskTitle = dueTask.getTaskTitle();

            Map<String, List<String>> projectMap = tasksByMember.get(memberId);
            if (projectMap == null) {
                projectMap = new HashMap<>();
                tasksByMember.put(memberId, projectMap);
            }

            List<String> taskTitles = projectMap.get(projectName);
            if (taskTitles == null) {
                taskTitles = new ArrayList<>();
                projectMap.put(projectName, taskTitles);
            }

            taskTitles.add(taskTitle);
        }

        return tasksByMember;
    }

    private String buildNotificationMessage(Map<String, List<String>> projectTasks) {
        StringBuilder message = new StringBuilder();

        projectTasks.forEach((projectName, tasks) ->
            message.append("[")
                .append(projectName)
                .append("] ")
                .append(String.join(", ", tasks))
                .append("\n")
        );

        return message.toString().trim();
    }

    private Member findMember(UUID memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            ));
    }

}
