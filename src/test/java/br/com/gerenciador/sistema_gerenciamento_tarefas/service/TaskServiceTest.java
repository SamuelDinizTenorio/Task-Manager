package br.com.gerenciador.sistema_gerenciamento_tarefas.service;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.Task;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskCreateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Deve retornar uma tarefa quando o ID for válido")
    void getTaskById_shouldReturnTask_whenIdIsValid() {
        // Cenário
        var task = setupIncompleteTaskFound(1L);

        // Execução
        TaskResponseDTO result = taskService.getTaskById(1L);

        // Verificação
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(task.getTitle(), result.title());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID não for encontrado")
    void getTaskById_shouldThrowException_whenIdNotFound() {
        // Cenário
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Execução e Verificação
        assertThrows(EntityNotFoundException.class, () -> taskService.getTaskById(99L));
        verify(taskRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve criar uma nova tarefa quando os dados forem válidos")
    void createTask_shouldSaveTask_whenDataIsValid() {
        // Cenário
        var taskCreateDTO = new TaskCreateDTO("Estudar Spring", "Revisar testes");

        // Simula o comportamento do repositório: recebe uma Task sem ID e a retorna com um ID gerado.
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task taskToSave = invocation.getArgument(0);
            // Simula a geração de ID pelo banco de dados
            return new Task(1L, taskToSave.getTitle(), taskToSave.getDescription(), taskToSave.getCreationDate(), taskToSave.getCompleted());
        });

        // Execução
        TaskResponseDTO result = taskService.createTask(taskCreateDTO);

        // Verificação
        assertNotNull(result);
        assertEquals(1L, result.id(), "O ID da tarefa criada deve ser 1L");
        assertEquals(taskCreateDTO.title(), result.title(), "O título deve corresponder ao DTO de entrada");
        assertEquals(taskCreateDTO.description(), result.description(), "A descrição deve corresponder ao DTO de entrada");
        assertNotNull(result.creationDate(), "A data de criação deve ser preenchida");
        assertFalse(result.completed(), "A tarefa deve ser criada como não concluída");

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar tarefa quando o repositório falhar")
    void createTask_shouldThrowException_whenRepositoryFails() {
        // Cenário
        var taskCreateDTO = new TaskCreateDTO("Tarefa com erro", "Descrição que vai falhar");

        // Simula uma falha no banco de dados durante a operação de salvar
        when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException("Simulated database error"));

        // Execução e Verificação
        var exception = assertThrows(RuntimeException.class, () -> {
            taskService.createTask(taskCreateDTO);
        }, "Deveria ter lançado uma RuntimeException");

        // Verifica se a mensagem da exceção é a esperada
        assertEquals("Simulated database error", exception.getMessage());
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    
    @Test
    @DisplayName("Deve atualizar todos os campos da tarefa quando os dados forem válidos")
    void updateTask_shouldUpdateAllFields_whenDataIsValid() {
        // Cenário
        var existingTask = setupIncompleteTaskFound(1L);
        var updateData = new TaskUpdateDTO("Título Novo", "Descrição Nova", true);

        // Execução
        TaskResponseDTO result = taskService.updateTask(1L, updateData);

        // Verificação
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Título Novo", result.title(), "O título deveria ter sido atualizado.");
        assertEquals("Descrição Nova", result.description(), "A descrição deveria ter sido atualizada.");
        assertTrue(result.completed(), "O status de conclusão deveria ter sido atualizado.");

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve atualizar apenas o título da tarefa e manter os outros campos")
    void updateTask_shouldUpdateOnlyTitle_whenOnlyTitleIsProvided() {
        // Cenário
        var existingTask = setupIncompleteTaskFound(1L);
        // Apenas o título é fornecido na atualização
        var updateData = new TaskUpdateDTO("Título Novo", null, null);

        // Execução
        TaskResponseDTO result = taskService.updateTask(1L, updateData);

        // Verificação
        assertNotNull(result);
        assertEquals("Título Novo", result.title(), "O título deveria ter sido atualizado.");
        assertEquals("Descrição Original", result.description(), "A descrição não deveria ter sido alterada.");
        assertFalse(result.completed(), "O status de conclusão não deveria ter sido alterado.");
        assertEquals(existingTask.getCreationDate(), result.creationDate(), "A data de criação não deveria ter sido alterada.");

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar uma tarefa inexistente")
    void updateTask_shouldThrowException_whenIdNotFound() {
        // Cenário
        var updateData = new TaskUpdateDTO("Qualquer Título", null, null);
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // Execução e Verificação
        assertThrows(EntityNotFoundException.class, () -> taskService.updateTask(99L, updateData));
        verify(taskRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve deletar a tarefa quando o ID for válido")
    void deleteTask_shouldDeleteTask_whenIdIsValid() {
        // Cenário
        var existingTask = setupIncompleteTaskFound(1L);
        doNothing().when(taskRepository).delete(existingTask);

        // Execução e Verificação
        assertDoesNotThrow(() -> taskService.deleteTask(1L), "A deleção não deve lançar exceção para um ID válido.");

        // Verifica se os métodos corretos do repositório foram chamados
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).delete(existingTask);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar uma tarefa inexistente")
    void deleteTask_shouldThrowException_whenIdNotFound() {
        // Cenário
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // Execução e Verificação
        assertThrows(EntityNotFoundException.class, () -> taskService.deleteTask(99L));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("Deve marcar a tarefa como concluída quando o ID for válido")
    void concludeTask_shouldMarkTaskAsCompleted_whenIdIsValid() {
        // Cenário
        setupIncompleteTaskFound(1L);

        // Execução
        TaskResponseDTO result = taskService.concludeTask(1L);

        // Verificação
        assertNotNull(result);
        assertTrue(result.completed(), "A tarefa deveria estar marcada como concluída.");
        assertEquals(1L, result.id());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve manter a tarefa como concluída se ela já estiver concluída")
    void concludeTask_shouldRemainCompleted_whenTaskIsAlreadyConcluded() {
        // Cenário
        setupCompletedTaskFound(1L);

        // Execução
        TaskResponseDTO result = taskService.concludeTask(1L);

        // Verificação
        assertTrue(result.completed(), "A tarefa deveria permanecer concluída.");
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar concluir uma tarefa inexistente")
    void concludeTask_shouldThrowException_whenIdNotFound() {
        // Cenário
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // Execução e Verificação
        assertThrows(EntityNotFoundException.class, () -> taskService.concludeTask(99L));
        verify(taskRepository, times(1)).findById(99L);
    }

    // Métodos auxiliares para encapsular a criação de mocks

    private Task setupIncompleteTaskFound(Long id) {
        var task = new Task(id, "Título Antigo", "Descrição Original", LocalDateTime.now().minusDays(1), false);
        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        return task;
    }

    private Task setupCompletedTaskFound(Long id) {
        var task = new Task(id, "Tarefa já concluída", "Descrição", LocalDateTime.now(), true);
        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        return task;
    }

}