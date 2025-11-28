package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.exception;

import org.springframework.validation.FieldError;

/**
 * DTO para representar o detalhe de um erro de validação de campo.
 *
 * @param field O nome do campo que falhou na validação.
 * @param message A mensagem de erro associada ao campo.
 */
public record ValidationErrorDetailDTO(
        String field,
        String message
) {
    /**
     * Construtor que converte um {@link FieldError} do Spring em um DTO.
     * @param error O objeto FieldError a ser convertido.
     */
    public ValidationErrorDetailDTO(FieldError error) {
        this(error.getField(), error.getDefaultMessage());
    }
}
