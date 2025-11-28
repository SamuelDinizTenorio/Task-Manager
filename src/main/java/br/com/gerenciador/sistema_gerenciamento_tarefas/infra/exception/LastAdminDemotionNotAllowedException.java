package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando se tenta demotar o último usuário administrador do sistema.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // 403 Forbidden
public class LastAdminDemotionNotAllowedException extends RuntimeException {
    public LastAdminDemotionNotAllowedException(String message) {
        super(message);
    }
}
