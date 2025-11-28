package br.com.gerenciador.sistema_gerenciamento_tarefas.domain.task;

import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade {@link Task}.
 * Foco: Testar a lógica de negócio encapsulada na própria entidade.
 */
class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        // Cria uma tarefa base para cada teste
        task = new Task(1L, "Old Title", "Old Description", LocalDateTime.now(), false);
    }

    @Test
    @DisplayName("updateInfo should update all fields when DTO is full")
    void updateInfo_shouldUpdateAllFields_whenDtoIsFull() {
        // Arrange
        var updateDTO = new TaskUpdateDTO("New Title", "New Description", true);

        // Act
        task.updateInfo(updateDTO);

        // Assert
        assertEquals("New Title", task.getTitle());
        assertEquals("New Description", task.getDescription());
        assertTrue(task.getCompleted());
    }

    @Test
    @DisplayName("updateInfo should update only title when other fields are null")
    void updateInfo_shouldUpdateOnlyTitle_whenOtherFieldsAreNull() {
        // Arrange
        var updateDTO = new TaskUpdateDTO("New Title", null, null);

        // Act
        task.updateInfo(updateDTO);

        // Assert
        assertEquals("New Title", task.getTitle());
        assertEquals("Old Description", task.getDescription()); // Deve permanecer o mesmo
        assertFalse(task.getCompleted()); // Deve permanecer o mesmo
    }

    @Test
    @DisplayName("updateInfo should update only description when other fields are null")
    void updateInfo_shouldUpdateOnlyDescription_whenOtherFieldsAreNull() {
        // Arrange
        var updateDTO = new TaskUpdateDTO(null, "New Description", null);

        // Act
        task.updateInfo(updateDTO);

        // Assert
        assertEquals("Old Title", task.getTitle()); // Deve permanecer o mesmo
        assertEquals("New Description", task.getDescription());
        assertFalse(task.getCompleted()); // Deve permanecer o mesmo
    }

    @Test
    @DisplayName("updateInfo should update only completed status when other fields are null")
    void updateInfo_shouldUpdateOnlyCompletedStatus_whenOtherFieldsAreNull() {
        // Arrange
        var updateDTO = new TaskUpdateDTO(null, null, true);

        // Act
        task.updateInfo(updateDTO);

        // Assert
        assertEquals("Old Title", task.getTitle()); // Deve permanecer o mesmo
        assertEquals("Old Description", task.getDescription()); // Deve permanecer o mesmo
        assertTrue(task.getCompleted());
    }

    @Test
    @DisplayName("updateInfo should not change fields when DTO fields are null or blank")
    void updateInfo_shouldNotChangeFields_whenDtoFieldsAreNullOrBlank() {
        // Arrange
        var updateDTO = new TaskUpdateDTO(null, "  ", null); // Descrição em branco

        // Act
        task.updateInfo(updateDTO);

        // Assert
        assertEquals("Old Title", task.getTitle());
        assertEquals("Old Description", task.getDescription()); // Não deve mudar para branco
        assertFalse(task.getCompleted());
    }

    @Test
    @DisplayName("updateInfo should throw IllegalArgumentException when DTO is null")
    void updateInfo_shouldThrowIllegalArgumentException_whenDtoIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            task.updateInfo(null);
        });
        assertEquals("Update DTO cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("completeTask should set completed to true")
    void completeTask_shouldSetCompletedToTrue() {
        // Arrange
        // A tarefa começa com 'completed = false' a partir do setUp()

        // Act
        task.completeTask();

        // Assert
        assertTrue(task.getCompleted());
    }
}
