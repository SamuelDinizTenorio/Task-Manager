package br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user;

/**
 * Enumeração que define os papéis (roles) de usuário no sistema.
 * Estes papéis são utilizados para controle de acesso e autorização.
 */
public enum UserRole {
    USER, // Representa um usuário comum com permissões básicas.
    ADMIN; // Representa um administrador com permissões elevadas.
}
