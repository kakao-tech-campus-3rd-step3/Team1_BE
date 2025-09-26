package knu.team1.be.boost.task.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.dto.TaskStatusRequestDto;
import knu.team1.be.boost.task.dto.TaskUpdateRequestDto;
import knu.team1.be.boost.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
}
