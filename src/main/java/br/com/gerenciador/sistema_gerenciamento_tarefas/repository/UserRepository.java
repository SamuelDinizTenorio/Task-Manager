package br.com.gerenciador.sistema_gerenciamento_tarefas.repository;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para a entidade User.
 * Fornece métodos CRUD e de busca para operações com usuários no banco de dados.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Busca um usuário pelo seu login.
     * Este método é utilizado pelo Spring Security para carregar os detalhes do usuário
     * durante o processo de autenticação.
     *
     * @param login O login do usuário a ser buscado.
     * @return Um Optional contendo os detalhes do usuário (UserDetails) se encontrado,
     *         ou um Optional vazio caso contrário.
     */
    Optional<UserDetails> findByLogin(String login);

    /**
     * Busca o primeiro usuário encontrado com a role especificada.
     * Útil para verificar a existência de um usuário com uma determinada role.
     *
     * @param role A role do usuário a ser buscada.
     * @return Um Optional contendo o usuário se encontrado, ou um Optional vazio caso contrário.
     */
    Optional<User> findFirstByRole(UserRole role);

    /**
     * Conta o número de usuários com uma role específica.
     *
     * @param role A role a ser contada.
     * @return O número de usuários com a role especificada.
     */
    long countByRole(UserRole role);
}
