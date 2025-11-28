package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um usuário tenta deletar a si mesmo.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // 403 Forbidden
public class SelfDeletionNotAllowedException extends RuntimeException {
    public SelfDeletionNotAllowedException(String message) {
        super(message);
    }
}
