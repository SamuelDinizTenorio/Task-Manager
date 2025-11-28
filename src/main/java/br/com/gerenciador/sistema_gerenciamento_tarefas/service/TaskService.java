package br.com.gerenciador.sistema_gerenciamento_tarefas.service;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.task.Task;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskCreateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.TaskNotFoundException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.validation.ValidationUtils;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Serviço que encapsula a lógica de negócio para operações relacionadas a tarefas.
 */
@Service
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Lista todas as tarefas de forma paginada.
     * @param page Objeto de paginação.
     * @return Uma página de DTOs de resposta de tarefa.
     */
    public Page<TaskResponseDTO> listAllTasks(Pageable page) {
        ValidationUtils.validateNotNull(page, "Pageable object");
        log.info("Listing all tasks for page request: {}", page);
        return taskRepository.findAll(page)
                .map(TaskResponseDTO::new);
    }

    /**
     * Busca uma tarefa pelo seu ID.
     * @param id O ID da tarefa.
     * @return Um DTO de resposta da tarefa.
     */
    public TaskResponseDTO getTaskById(Long id) {
        ValidationUtils.validateNotNull(id, "Task ID");
        log.info("Fetching task by ID: {}", id);
        Task taskFound = getTaskByIdOrThrow(id);

        return new TaskResponseDTO(taskFound);
    }

    /**
     * Cria uma nova tarefa.
     * Este método é transacional.
     * @param data DTO com os dados de criação.
     * @return Um DTO de resposta da tarefa recém-criada.
     */
    @Transactional
    public TaskResponseDTO createTask(TaskCreateDTO data) {
        ValidationUtils.validateNotNull(data, "TaskCreateDTO");
        log.info("Creating a new task.");
        log.debug("Task creation data: {}", data);
        Task task = new Task(null, data.title(), data.description(), LocalDateTime.now(), false);

        log.debug("Saving the new task to the database.");
        Task savedTask = taskRepository.save(task);
        log.info("New task created with ID: {}", savedTask.getId());
        return new TaskResponseDTO(savedTask);
    }

    /**
     * Atualiza uma tarefa existente.
     * Este método é transacional.
     * @param id O ID da tarefa a ser atualizada.
     * @param data DTO com os dados de atualização.
     * @return Um DTO de resposta da tarefa atualizada.
     */
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskUpdateDTO data) {
        ValidationUtils.validateNotNull(id, "Task ID");
        ValidationUtils.validateNotNull(data, "TaskUpdateDTO");
        log.info("Updating task with ID: {}", id);
        log.debug("Task update data: {}", data);
        Task taskFound = getTaskByIdOrThrow(id);

        taskFound.updateInfo(data);
        log.info("Task with ID {} updated successfully.", id);
        return new TaskResponseDTO(taskFound);
    }

    /**
     * Deleta uma tarefa.
     * Este método é transacional.
     * @param id O ID da tarefa a ser deletada.
     */
    @Transactional
    public void deleteTask(Long id) {
        ValidationUtils.validateNotNull(id, "Task ID");
        log.info("Deleting task with ID: {}", id);
        Task taskFound = getTaskByIdOrThrow(id);
        taskRepository.delete(taskFound);
        log.info("Task with ID {} deleted successfully.", id);
    }
    
    /**
     * Marca uma tarefa como concluída.
     * Este método é transacional.
     * @param id O ID da tarefa a ser concluída.
     * @return Um DTO de resposta da tarefa atualizada.
     */
    @Transactional
    public TaskResponseDTO concludeTask(Long id) {
        ValidationUtils.validateNotNull(id, "Task ID");
        log.info("Concluding task with ID: {}", id);
        Task taskFound = getTaskByIdOrThrow(id);
        taskFound.setCompleted(true);
        log.info("Task with ID {} has been marked as concluded.", id);
        return new TaskResponseDTO(taskFound);
    }
    
    /**
     * Método auxiliar para buscar uma tarefa pelo ID ou lançar uma exceção se não encontrada.
     * @param id O ID da tarefa.
     * @return A entidade Task encontrada.
     * @throws TaskNotFoundException se a tarefa não for encontrada.
     */
    private Task getTaskByIdOrThrow(Long id) {
        log.debug("Fetching task with ID {} from database.", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
    }
}
