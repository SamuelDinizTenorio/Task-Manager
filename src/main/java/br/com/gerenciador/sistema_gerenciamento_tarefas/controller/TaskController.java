package br.com.gerenciador.sistema_gerenciamento_tarefas.controller;

import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskCreateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/tasks")
@Slf4j
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> listAllTasks(@PageableDefault(size = 10, sort = {"creationDate"}) Pageable pageable) {
        log.info("Received request to list all tasks. Pageable: {}", pageable);
        Page<TaskResponseDTO> tasks = taskService.listAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        log.info("Received request to get task by ID: {}", id);
        TaskResponseDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid TaskCreateDTO data, UriComponentsBuilder uriBuilder) {
        log.info("Received request to create a new task.");
        log.debug("Request body: {}", data);
        TaskResponseDTO createdTask = taskService.createTask(data);
        var uri = uriBuilder.path("/tasks/{id}").buildAndExpand(createdTask.id()).toUri();
        log.info("Task created successfully. URI: {}", uri);
        return ResponseEntity.created(uri).body(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @RequestBody @Valid TaskUpdateDTO data) {
        log.info("Received request to update task with ID: {}", id);
        log.debug("Request body: {}", data);
        TaskResponseDTO updatedTask = taskService.updateTask(id, data);
        log.info("Task with ID {} updated successfully.", id);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Received request to delete task with ID: {}", id);
        taskService.deleteTask(id);
        log.info("Task with ID {} deleted successfully.", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/conclude")
    public ResponseEntity<TaskResponseDTO> concludeTask(@PathVariable Long id) {
        log.info("Received request to conclude task with ID: {}", id);
        TaskResponseDTO concludedTask = taskService.concludeTask(id);
        log.info("Task with ID {} concluded successfully.", id);
        return ResponseEntity.ok(concludedTask);
    }
}