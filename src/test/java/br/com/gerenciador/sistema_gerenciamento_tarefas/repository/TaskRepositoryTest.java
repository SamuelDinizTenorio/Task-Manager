package br.com.gerenciador.sistema_gerenciamento_tarefas.repository;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.Task;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskCreateDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("Should get all tasks from the database")
    void findAllSuccess() {
        // Cenário
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO("Test Task", "Test Description");
        createTask(taskCreateDTO);

        // Execução
        Page<Task> result = this.taskRepository.findAll(Pageable.unpaged());

        // Verificação
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Task");
        assertThat(result.getContent().getFirst().getDescription()).isEqualTo("Test Description");
        assertThat(result.getContent().getFirst().getCreationDate()).isNotNull();
        assertThat(result.getContent().getFirst().getCompleted()).isFalse();
    }

    private void createTask(TaskCreateDTO data) {
        Task newTask = new Task(null, data.title(), data.description(), LocalDateTime.now(), false);
        this.entityManager.persist(newTask);
    }
}