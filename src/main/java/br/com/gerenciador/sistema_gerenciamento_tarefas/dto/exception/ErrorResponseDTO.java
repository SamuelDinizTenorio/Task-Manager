package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * DTO padrão para respostas de erro da API.
 * Fornece uma estrutura consistente para todos os erros retornados pela aplicação.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<?> details
) {
    // Construtor para erros sem lista de detalhes
    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, Collections.emptyList());
    }
}
