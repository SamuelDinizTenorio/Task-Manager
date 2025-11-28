package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um usuário específico não é encontrado no sistema.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Define o status HTTP padrão para esta exceção
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
