package br.com.gerenciador.sistema_gerenciamento_tarefas.service;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.task.Task;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskCreateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.TaskNotFoundException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe {@link TaskService}.
 * Foco: Testar a lógica de negócio de gerenciamento de tarefas em isolamento.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    // --- Testes para o método listAllTasks ---

    @Test
    @DisplayName("listAllTasks should return a page of tasks")
    void listAllTasks_shouldReturnPageOfTasks() {
        // Arrange
        var task = new Task(1L, "Test Task", "Description", LocalDateTime.now(), false);
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findAll(pageable)).thenReturn(taskPage);

        // Act
        Page<TaskResponseDTO> result = taskService.listAllTasks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Task", result.getContent().get(0).title());
    }

    @Test
    @DisplayName("listAllTasks should return an empty page when no tasks exist")
    void listAllTasks_shouldReturnEmptyPage_whenNoTasksExist() {
        // Arrange
        Page<Task> emptyPage = Page.empty();
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<TaskResponseDTO> result = taskService.listAllTasks(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("listAllTasks should propagate exceptions from the repository")
    void listAllTasks_shouldPropagateRepositoryExceptions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String errorMessage = "Database connection failed";
        when(taskRepository.findAll(pageable)).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            taskService.listAllTasks(pageable);
        });
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @DisplayName("listAllTasks should throw IllegalArgumentException when pageable is null")
    void listAllTasks_shouldThrowException_whenPageableIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.listAllTasks(null);
        });

        assertEquals("Pageable object cannot be null.", exception.getMessage());
    }

    // --- Testes para o método getTaskById ---

    @Test
    @DisplayName("getTaskById should return a task when ID exists")
    void getTaskById_shouldReturnTask_whenIdExists() {
        // Arrange
        long taskId = 1L;
        var task = new Task(taskId, "Test Task", "Description", LocalDateTime.now(), false);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        TaskResponseDTO result = taskService.getTaskById(taskId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.title());
    }

    @Test
    @DisplayName("getTaskById should throw TaskNotFoundException when ID does not exist")
    void getTaskById_shouldThrowException_whenIdDoesNotExist() {
        // Arrange
        long nonExistentId = 99L;
        when(taskRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTaskById(nonExistentId);
        });

        assertEquals("Task not found with ID: " + nonExistentId, exception.getMessage());
    }

    @Test
    @DisplayName("getTaskById should throw IllegalArgumentException when ID is null")
    void getTaskById_shouldThrowException_whenIdIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTaskById(null);
        });

        assertEquals("Task ID cannot be null.", exception.getMessage());
    }

    // --- Testes para o método createTask ---

    @Test
    @DisplayName("createTask should save and return a new task")
    void createTask_shouldSaveAndReturnNewTask() {
        // Arrange
        var createDTO = new TaskCreateDTO("New Task", "New Description");
        var savedTask = new Task(1L, createDTO.title(), createDTO.description(), LocalDateTime.now(), false);

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // Act
        TaskResponseDTO result = taskService.createTask(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals(createDTO.title(), result.title());
        assertEquals(createDTO.description(), result.description());
        assertFalse(result.completed());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();

        assertEquals(createDTO.title(), capturedTask.getTitle());
        assertEquals(createDTO.description(), capturedTask.getDescription());
        assertFalse(capturedTask.getCompleted());
        assertNotNull(capturedTask.getCreationDate());
    }

    @Test
    @DisplayName("createTask should throw IllegalArgumentException when DTO is null")
    void createTask_shouldThrowException_whenDtoIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(null);
        });

        assertEquals("TaskCreateDTO cannot be null.", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask should propagate exceptions from the repository")
    void createTask_shouldPropagateRepositoryExceptions() {
        // Arrange
        var createDTO = new TaskCreateDTO("New Task", "New Description");
        String errorMessage = "Database save failed";
        when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            taskService.createTask(createDTO);
        });
        assertEquals(errorMessage, exception.getMessage());
    }

    // --- Testes para o método updateTask ---

    @Test
    @DisplayName("updateTask should update and return the task when data is valid")
    void updateTask_shouldUpdateAndReturnTask_whenDataIsValid() {
        // Arrange
        long taskId = 1L;
        var existingTask = new Task(taskId, "Old Title", "Old Description", LocalDateTime.now(), false);
        var updateDTO = new TaskUpdateDTO("New Title", "New Description", true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act
        TaskResponseDTO result = taskService.updateTask(taskId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.title(), result.title());
        assertEquals(updateDTO.description(), result.description());
        assertEquals(updateDTO.completed(), result.completed());

        // Verifica se o estado do objeto foi alterado
        assertEquals("New Title", existingTask.getTitle());
        assertEquals("New Description", existingTask.getDescription());
        assertTrue(existingTask.getCompleted());
        verify(taskRepository, never()).save(any(Task.class)); // Garante que save não é chamado
    }

    @Test
    @DisplayName("updateTask should throw TaskNotFoundException when ID does not exist")
    void updateTask_shouldThrowTaskNotFoundException_whenIdDoesNotExist() {
        // Arrange
        long nonExistentId = 99L;
        var updateDTO = new TaskUpdateDTO("New Title", "New Description", true);

        when(taskRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(nonExistentId, updateDTO);
        });

        assertEquals("Task not found with ID: " + nonExistentId, exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTask should throw IllegalArgumentException when ID is null")
    void updateTask_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Arrange
        var updateDTO = new TaskUpdateDTO("New Title", "New Description", true);

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(null, updateDTO);
        });

        assertEquals("Task ID cannot be null.", exception.getMessage());
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTask should throw IllegalArgumentException when DTO is null")
    void updateTask_shouldThrowIllegalArgumentException_whenDtoIsNull() {
        // Arrange
        long taskId = 1L;

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(taskId, null);
        });

        assertEquals("TaskUpdateDTO cannot be null.", exception.getMessage());
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTask should propagate exceptions from the repository on findById")
    void updateTask_shouldPropagateRepositoryExceptions() {
        // Arrange
        long taskId = 1L;
        var updateDTO = new TaskUpdateDTO("New Title", "New Description", true);
        String errorMessage = "Database find failed";

        when(taskRepository.findById(taskId)).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            taskService.updateTask(taskId, updateDTO);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    // --- Testes para o método deleteTask ---

    @Test
    @DisplayName("deleteTask should delete the task when ID exists")
    void deleteTask_shouldDeleteTask_whenIdExists() {
        // Arrange
        long taskId = 1L;
        var existingTask = new Task(taskId, "Task to delete", "Description", LocalDateTime.now(), false);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        doNothing().when(taskRepository).delete(existingTask);

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).delete(existingTask);
    }

    @Test
    @DisplayName("deleteTask should throw TaskNotFoundException when ID does not exist")
    void deleteTask_shouldThrowTaskNotFoundException_whenIdDoesNotExist() {
        // Arrange
        long nonExistentId = 99L;
        when(taskRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(nonExistentId);
        });

        assertEquals("Task not found with ID: " + nonExistentId, exception.getMessage());
        verify(taskRepository, times(1)).findById(nonExistentId);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("deleteTask should throw IllegalArgumentException when ID is null")
    void deleteTask_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.deleteTask(null);
        });

        assertEquals("Task ID cannot be null.", exception.getMessage());
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("deleteTask should propagate exceptions from the repository")
    void deleteTask_shouldPropagateRepositoryExceptions() {
        // Arrange
        long taskId = 1L;
        var existingTask = new Task(taskId, "Task to delete", "Description", LocalDateTime.now(), false);
        String errorMessage = "Database delete failed";

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        doThrow(new RuntimeException(errorMessage)).when(taskRepository).delete(existingTask);

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            taskService.deleteTask(taskId);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).delete(existingTask);
    }

    // --- Testes para o método concludeTask ---

    @Test
    @DisplayName("concludeTask should mark task as completed and return it")
    void concludeTask_shouldMarkTaskAsCompleted_whenIdExists() {
        // Arrange
        long taskId = 1L;
        var existingTask = new Task(taskId, "Task to conclude", "Description", LocalDateTime.now(), false);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act
        TaskResponseDTO result = taskService.concludeTask(taskId);

        // Assert
        assertNotNull(result);
        assertTrue(result.completed());

        // Verifica se o estado do objeto foi alterado, já que save() não é chamado
        assertTrue(existingTask.getCompleted());
        verify(taskRepository, never()).save(any(Task.class)); // Garante que save não é chamado
    }

    @Test
    @DisplayName("concludeTask should throw TaskNotFoundException when ID does not exist")
    void concludeTask_shouldThrowTaskNotFoundException_whenIdDoesNotExist() {
        // Arrange
        long nonExistentId = 99L;
        when(taskRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(TaskNotFoundException.class, () -> {
            taskService.concludeTask(nonExistentId);
        });

        assertEquals("Task not found with ID: " + nonExistentId, exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("concludeTask should throw IllegalArgumentException when ID is null")
    void concludeTask_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.concludeTask(null);
        });

        assertEquals("Task ID cannot be null.", exception.getMessage());
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("concludeTask should propagate exceptions from the repository")
    void concludeTask_shouldPropagateRepositoryExceptions() {
        // Arrange
        long taskId = 1L;
        var existingTask = new Task(taskId, "Task to conclude", "Description", LocalDateTime.now(), false);
        String errorMessage = "Database find failed";

        when(taskRepository.findById(taskId)).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            taskService.concludeTask(taskId);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }
}
