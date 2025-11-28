package br.com.gerenciador.sistema_gerenciamento_tarefas.controller;

import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskCreateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller para operações CRUD e outras ações relacionadas a tarefas (Tasks).
 */
@RestController
@RequestMapping("/tasks")
@Slf4j
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Lista todas as tarefas de forma paginada.
     * @param pageable Objeto de paginação para controlar o tamanho da página, ordenação, etc.
     * @return Um ResponseEntity contendo uma página (Page) de tarefas.
     */
    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> listAllTasks(@PageableDefault(size = 10, sort = {"creationDate"}) Pageable pageable) {
        log.info("Received request to list all tasks. Pageable: {}", pageable);
        Page<TaskResponseDTO> tasks = taskService.listAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Busca e retorna uma tarefa específica pelo seu ID.
     * @param id O ID da tarefa a ser buscada.
     * @return Um ResponseEntity contendo os dados da tarefa.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        log.info("Received request to get task by ID: {}", id);
        TaskResponseDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    /**
     * Cria uma nova tarefa.
     * @param data DTO com os dados para a criação da tarefa.
     * @param uriBuilder Construtor de URI para gerar o cabeçalho Location da resposta.
     * @return Um ResponseEntity com status 201 Created, o cabeçalho Location e o corpo da tarefa criada.
     */
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid TaskCreateDTO data, UriComponentsBuilder uriBuilder) {
        log.info("Received request to create a new task.");
        log.debug("Request body: {}", data);
        TaskResponseDTO createdTask = taskService.createTask(data);
        var uri = uriBuilder.path("/tasks/{id}").buildAndExpand(createdTask.id()).toUri();
        log.info("Task created successfully. URI: {}", uri);
        return ResponseEntity.created(uri).body(createdTask);
    }

    /**
     * Atualiza os dados de uma tarefa existente.
     * @param id O ID da tarefa a ser atualizada.
     * @param data DTO com os novos dados da tarefa.
     * @return Um ResponseEntity com os dados da tarefa atualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @RequestBody @Valid TaskUpdateDTO data) {
        log.info("Received request to update task with ID: {}", id);
        log.debug("Request body: {}", data);
        TaskResponseDTO updatedTask = taskService.updateTask(id, data);
        log.info("Task with ID {} updated successfully.", id);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Deleta uma tarefa pelo seu ID.
     * @param id O ID da tarefa a ser deletada.
     * @return Um ResponseEntity com status 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Received request to delete task with ID: {}", id);
        taskService.deleteTask(id);
        log.info("Task with ID {} deleted successfully.", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marca uma tarefa como concluída.
     * @param id O ID da tarefa a ser concluída.
     * @return Um ResponseEntity com os dados da tarefa atualizada para o estado 'concluída'.
     */
    @PatchMapping("/{id}/conclude")
    public ResponseEntity<TaskResponseDTO> concludeTask(@PathVariable Long id) {
        log.info("Received request to conclude task with ID: {}", id);
        TaskResponseDTO concludedTask = taskService.concludeTask(id);
        log.info("Task with ID {} concluded successfully.", id);
        return ResponseEntity.ok(concludedTask);
    }
}
