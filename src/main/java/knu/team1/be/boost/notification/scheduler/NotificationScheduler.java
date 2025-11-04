package knu.team1.be.boost.notification.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.notification.service.NotificationSenderService;
import knu.team1.be.boost.task.repository.TaskRepository;
import knu.team1.be.boost.task.repository.TaskRepository.DueTask;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final NotificationSenderService notificationSenderService;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    public void notifyDueTomorrowTasks() {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        List<DueTask> dueTasks = taskRepository.findDueTasksByMember(tomorrow);

        Map<UUID, Map<UUID, List<DueTask>>> groupedDueTask = dueTasks.stream()
            .collect(Collectors.groupingBy(
                DueTask::getMemberId,
                Collectors.groupingBy(DueTask::getProjectId)
            ));
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern("MM월 dd일"));

        groupedDueTask.forEach((memberId, projectTasks) -> {
            Member member = findMember(memberId);
            String message = buildNotificationMessage(projectTasks);
            String title = formattedDate + " 마감 임박 작업";
            notificationSenderService.saveAndSendNotification(member, title, message);
        });
    }

    private String buildNotificationMessage(Map<UUID, List<DueTask>> projectTasks) {
        StringBuilder message = new StringBuilder();

        projectTasks.forEach((projectId, tasks) -> {
            String projectName = tasks.getFirst().getProjectName();
            String taskTitles = tasks.stream()
                .map(DueTask::getTaskTitle)
                .collect(Collectors.joining(", "));

            message.append("[")
                .append(projectName)
                .append("] ")
                .append(taskTitles)
                .append("\n");
        });

        return message.toString().trim();
    }

    private Member findMember(UUID memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND, "memberId: " + memberId
            ));
    }
}
