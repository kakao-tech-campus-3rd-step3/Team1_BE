package knu.team1.be.boost.task.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        @Valid @RequestBody TaskCreateRequestDto request) {
        TaskResponseDto response = taskService.createTask(projectId, request);
        URI location = URI.create(
            "/api/projects/" + response.projectId() + "/tasks/" + response.taskId());
        return ResponseEntity.created(location).body(response);
    }
}
