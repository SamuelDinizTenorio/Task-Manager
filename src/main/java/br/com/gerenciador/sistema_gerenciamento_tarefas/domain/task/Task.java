package br.com.gerenciador.sistema_gerenciamento_tarefas.domain.task;

import jakarta.persistence.*;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task.TaskUpdateDTO;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa a entidade de Tarefa no sistema.
 * Mapeada para a tabela "tasks" no banco de dados.
 */
@Entity(name = "Task")
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID único da tarefa, gerado automaticamente pelo banco de dados.
    private String title; // Título da tarefa.
    private String description; // Descrição detalhada da tarefa.

    @Column(name = "creation_date")
    private LocalDateTime creationDate; // Data e hora de criação da tarefa.
    private Boolean completed; // Indica se a tarefa foi concluída (true) ou está pendente (false).

    /**
     * Atualiza as informações da tarefa com base nos dados fornecidos.
     * Apenas os campos não nulos no DTO de atualização serão modificados.
     * @param data DTO contendo os dados para atualização da tarefa.
     */
    public void updateInfo(TaskUpdateDTO data) {
        if (data == null) {
            throw new IllegalArgumentException("Update DTO cannot be null.");
        }
        if (data.title() != null && !data.title().isBlank()) {
            this.title = data.title();
        }
        if (data.description() != null && !data.description().isBlank()) {
            this.description = data.description();
        }
        if (data.completed() != null) {
            this.completed = data.completed();
        }
    }

    /**
     * Marca a tarefa como concluída.
     * Este método define o campo 'completed' como {@code true}.
     */
    public void completeTask() {
        this.completed = true;
    }
}
