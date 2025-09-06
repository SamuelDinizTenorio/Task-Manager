package br.com.gerenciador.sistema_gerenciamento_tarefas.service;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.Task;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskCreateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Page<TaskResponseDTO> listAllTasks(Pageable page) {
        log.info("Listing all tasks for page request: {}", page);
        return taskRepository.findAll(page)
                .map(TaskResponseDTO::new);
    }

    public TaskResponseDTO getTaskById(Long id) {
        log.info("Fetching task by ID: {}", id);
        Task taskFound = getTaskByIdOrThrow(id);

        return new TaskResponseDTO(taskFound);
    }

    @Transactional
    public TaskResponseDTO createTask(TaskCreateDTO data) {
        log.info("Creating a new task.");
        log.debug("Task creation data: {}", data);
        Task task = new Task(null, data.title(), data.description(), LocalDateTime.now(), false);

        log.debug("Saving the new task to the database.");
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());
        return new TaskResponseDTO(savedTask);
    }

    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskUpdateDTO data) {
        log.info("Updating task with ID: {}", id);
        log.debug("Task update data: {}", data);
        Task taskFound = getTaskByIdOrThrow(id);

        taskFound.updateInfo(data);
        log.info("Task with ID {} updated successfully.", id);
        return new TaskResponseDTO(taskFound);
    }

    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        Task taskFound = getTaskByIdOrThrow(id);
        taskRepository.delete(taskFound);
        log.info("Task with ID {} deleted successfully.", id);
    }
    
    @Transactional
    public TaskResponseDTO concludeTask(Long id) {
        log.info("Concluding task with ID: {}", id);
        Task taskFound = getTaskByIdOrThrow(id);
        taskFound.setCompleted(true);
        log.info("Task with ID {} has been marked as concluded.", id);
        return new TaskResponseDTO(taskFound);
    }
    
    private Task getTaskByIdOrThrow(Long id) {
        log.debug("Fetching task with ID {} from database.", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
    }
}
