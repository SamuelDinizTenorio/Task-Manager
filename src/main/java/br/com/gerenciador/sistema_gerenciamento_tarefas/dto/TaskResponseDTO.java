package br.com.gerenciador.sistema_gerenciamento_tarefas.dto;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.Task;

import java.time.LocalDateTime;

public record TaskResponseDTO(
        Long id,
        String title,
        String description,
        LocalDateTime creationDate,
        Boolean completed
) {
    public TaskResponseDTO(Task task) {
        this(task.getId(), task.getTitle(), task.getDescription(), task.getCreationDate(), task.getCompleted());
    }
}
