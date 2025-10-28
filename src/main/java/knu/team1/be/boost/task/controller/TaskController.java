package knu.team1.be.boost.task.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.task.dto.MemberTaskStatusCountResponseDto;
import knu.team1.be.boost.task.dto.MyTaskStatusCountResponseDto;
import knu.team1.be.boost.task.dto.ProjectTaskStatusCountResponseDto;
import knu.team1.be.boost.task.dto.TaskApproveResponseDto;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskDetailResponseDto;
import knu.team1.be.boost.task.dto.TaskMemberSectionResponseDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.dto.TaskSortBy;
import knu.team1.be.boost.task.dto.TaskSortDirection;
import knu.team1.be.boost.task.dto.TaskStatusRequestDto;
import knu.team1.be.boost.task.dto.TaskStatusSectionDto;
import knu.team1.be.boost.task.dto.TaskUpdateRequestDto;
import knu.team1.be.boost.task.entity.TaskStatus;
import knu.team1.be.boost.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TaskController implements TaskApi {

    private final TaskService taskService;

    @Override
    public ResponseEntity<TaskResponseDto> createTask(
        @PathVariable UUID projectId,
        @Valid @RequestBody TaskCreateRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        TaskResponseDto response = taskService.createTask(projectId, request, user);
        URI location = URI.create(
            "/api/projects/" + response.projectId() + "/tasks/" + response.taskId());
        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<TaskResponseDto> updateTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @Valid @RequestBody TaskUpdateRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        TaskResponseDto response = taskService.updateTask(projectId, taskId, request, user);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        taskService.deleteTask(projectId, taskId, user);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TaskResponseDto> changeTaskStatus(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @Valid @RequestBody TaskStatusRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        TaskResponseDto response = taskService.changeTaskStatus(projectId, taskId, request, user);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TaskDetailResponseDto> getTaskDetail(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        TaskDetailResponseDto response = taskService.getTaskDetail(projectId, taskId, user);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TaskStatusSectionDto> listTasksByStatus(
        @PathVariable UUID projectId,
        @RequestParam(required = false) UUID tagId,
        @RequestParam(required = false, defaultValue = "TODO") TaskStatus status,
        @RequestParam(required = false, defaultValue = "CREATED_AT") TaskSortBy sortBy,
        @RequestParam(required = false, defaultValue = "ASC") TaskSortDirection direction,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        TaskStatusSectionDto response =
            taskService.listByStatus(
                projectId,
                tagId,
                status,
                sortBy,
                direction,
                search,
                cursor,
                limit,
                user
            );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TaskStatusSectionDto> listMyTasksByStatus(
        @RequestParam(required = false, defaultValue = "TODO") TaskStatus status,
        @RequestParam(required = false, defaultValue = "CREATED_AT") TaskSortBy sortBy,
        @RequestParam(required = false, defaultValue = "ASC") TaskSortDirection direction,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
        @AuthenticationPrincipal UserPrincipalDto user

    ) {
        TaskStatusSectionDto result =
            taskService.listMyTasksByStatus(
                status,
                sortBy,
                direction,
                search,
                cursor,
                limit,
                user
            );
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<TaskMemberSectionResponseDto> listTasksByMember(
        @PathVariable UUID projectId,
        @PathVariable UUID memberId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        TaskMemberSectionResponseDto response =
            taskService.listByMember(projectId, memberId, search, cursor, limit, user);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TaskApproveResponseDto> approveTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        TaskApproveResponseDto response = taskService.approveTask(projectId, taskId, user);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MyTaskStatusCountResponseDto> getMyTaskStatusCount(
        @RequestParam(required = false) String search,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        MyTaskStatusCountResponseDto response = taskService.countMyTasksByStatus(search, user);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ProjectTaskStatusCountResponseDto> getProjectTaskStatusCount(
        @PathVariable UUID projectId,
        @RequestParam(required = false) String search,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        ProjectTaskStatusCountResponseDto response = taskService.countTasksByStatusForProject(
            projectId,
            search,
            user
        );
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<List<MemberTaskStatusCountResponseDto>> getMemberTaskStatusCount(
        @PathVariable UUID projectId,
        @RequestParam(required = false) String search,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        List<MemberTaskStatusCountResponseDto> response = taskService.countTasksByStatusForAllMembers(
            projectId,
            search,
            user
        );
        return ResponseEntity.ok(response);
    }
}
