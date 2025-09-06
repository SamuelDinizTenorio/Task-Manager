package br.com.gerenciador.sistema_gerenciamento_tarefas.domain;

import jakarta.persistence.*;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.TaskUpdateDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "Task")
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
    private Boolean completed;

    public void updateInfo(TaskUpdateDTO data) {
        if (data.title() != null) {
            this.title = data.title();
        }
        if (data.description() != null) {
            this.description = data.description();
        }
        if (data.completed() != null) {
            this.completed = data.completed();
        }
    }
}
