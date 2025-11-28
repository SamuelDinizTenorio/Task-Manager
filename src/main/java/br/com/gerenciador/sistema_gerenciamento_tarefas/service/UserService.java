package br.com.gerenciador.sistema_gerenciamento_tarefas.service;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user.UserResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user.UserUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.LastAdminDeletionNotAllowedException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.LastAdminDemotionNotAllowedException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.SelfDeletionNotAllowedException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.SelfRoleChangeNotAllowedException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.UserNotFoundException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.validation.ValidationUtils;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço que encapsula a lógica de negócio para operações relacionadas a usuários.
 */
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retorna uma lista paginada de todos os usuários do sistema.
     *
     * @param pageable O objeto de paginação contendo informações de página, tamanho e ordenação.
     * @return Uma página (Page) de UserResponseDTO com os dados públicos dos usuários.
     */
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        ValidationUtils.validateNotNull(pageable, "Pageable object");
        log.debug("Fetching all users from database with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(UserResponseDTO::new);
    }

    /**
     * Busca um usuário pelo seu ID.
     *
     * @param id O UUID do usuário a ser buscado.
     * @return Um UserResponseDTO com os dados públicos do usuário.
     * @throws UserNotFoundException se nenhum usuário for encontrado com o ID fornecido.
     */
    public UserResponseDTO getUserById(UUID id) {
        ValidationUtils.validateNotNull(id, "User ID");
        log.debug("Fetching user by ID: {}", id);
        User user = findUserByIdOrThrow(id);
        log.debug("User with ID {} found.", id);
        return new UserResponseDTO(user);
    }

    /**
     * Atualiza a role de um usuário específico.
     * Este método é transacional.
     *
     * @param id O UUID do usuário a ter a role atualizada.
     * @param newRole A nova role a ser atribuída ao usuário.
     * @param currentUser O usuário atualmente autenticado realizando a operação.
     * @return Um UserResponseDTO com os dados atualizados do usuário.
     */
    @Transactional
    public UserResponseDTO updateUserRole(UUID id, UserRole newRole, User currentUser) {
        ValidationUtils.validateNotNull(id, "User ID");
        ValidationUtils.validateNotNull(newRole, "New role");
        ValidationUtils.validateNotNull(currentUser, "Current user");

        log.info("Updating role for user ID: {} to {} by current user: {}", id, newRole, currentUser.getLogin());
        User user = findUserByIdOrThrow(id);

        // Validação: ADMIN não pode alterar a própria role
        if (currentUser.getId().equals(id) && currentUser.getRole() == UserRole.ADMIN) {
            log.warn("Self role change attempt by ADMIN user: {}", currentUser.getLogin());
            throw new SelfRoleChangeNotAllowedException("An ADMIN user cannot change their own role.");
        }

        // Validação: Não permitir demotion do último ADMIN
        if (user.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                log.warn("Attempt to demote the last ADMIN user: {}", user.getLogin());
                throw new LastAdminDemotionNotAllowedException("Cannot demote the last ADMIN user in the system.");
            }
        }

        user.assignRole(newRole);
        log.info("User ID: {} role updated to {}", id, newRole);
        return new UserResponseDTO(user);
    }

    /**
     * Atualiza as informações de um usuário (login e/ou senha).
     * Este método é transacional.
     *
     * @param id O UUID do usuário a ser atualizado.
     * @param data DTO com os dados de atualização (login e/ou senha).
     * @param currentUser O usuário atualmente autenticado realizando a operação.
     * @return Um UserResponseDTO com os dados atualizados do usuário.
     */
    @Transactional
    public UserResponseDTO updateUser(UUID id, UserUpdateDTO data, User currentUser) {
        ValidationUtils.validateNotNull(id, "User ID");
        ValidationUtils.validateNotNull(data, "UserUpdateDTO");
        ValidationUtils.validateNotNull(currentUser, "Current user");

        log.info("Updating user with ID: {} by current user: {}", id, currentUser.getLogin());

        // Autorização: Usuário só pode atualizar o próprio perfil, a menos que seja ADMIN
        if (!currentUser.getId().equals(id) && currentUser.getRole() != UserRole.ADMIN) {
            log.warn("User {} attempted to update another user's profile (ID: {}) without ADMIN role.", currentUser.getLogin(), id);
            throw new SecurityException("You are not authorized to update this user's profile.");
        }

        User user = findUserByIdOrThrow(id);

        // Atualiza o login se fornecido
        if (data.login() != null && !data.login().isBlank()) {
            user.changeLogin(data.login(), userRepository);
        }

        // Atualiza a senha se fornecida
        if (data.password() != null && !data.password().isBlank()) {
            user.changePassword(data.password(), passwordEncoder);
        }

        log.info("User with ID: {} updated successfully.", id);
        return new UserResponseDTO(user);
    }

    /**
     * Deleta um usuário do sistema.
     * Este método é transacional.
     *
     * @param id O UUID do usuário a ser deletado.
     * @param currentUser O usuário atualmente autenticado realizando a operação.
     */
    @Transactional
    public void deleteUser(UUID id, User currentUser) {
        ValidationUtils.validateNotNull(id, "User ID");
        ValidationUtils.validateNotNull(currentUser, "Current user");

        log.info("Attempting to delete user with ID: {} by current user: {}", id, currentUser.getLogin());
        User userToDelete = findUserByIdOrThrow(id);

        // Validação: Usuário não pode deletar a si mesmo
        if (currentUser.getId().equals(id)) {
            log.warn("Self-deletion attempt by user: {}", currentUser.getLogin());
            throw new SelfDeletionNotAllowedException("A user cannot delete themselves.");
        }

        // Validação: Não permitir a exclusão do último ADMIN
        if (userToDelete.getRole() == UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                log.warn("Attempt to delete the last ADMIN user: {}", userToDelete.getLogin());
                throw new LastAdminDeletionNotAllowedException("Cannot delete the last ADMIN user in the system.");
            }
        }

        userRepository.delete(userToDelete);
        log.info("User with ID: {} deleted successfully.", id);
    }

    /**
     * Retorna os dados do usuário fornecido.
     *
     * @param user O objeto User do usuário autenticado.
     * @return Um UserResponseDTO com os dados públicos do usuário.
     */
    public UserResponseDTO getCurrentUser(User user) {
        ValidationUtils.validateNotNull(user, "User");
        log.debug("Converting User entity to UserResponseDTO for user: {}", user.getUsername());
        return new UserResponseDTO(user);
    }

    /**
     * Método auxiliar para buscar um usuário pelo ID ou lançar uma exceção padrão se não encontrado.
     *
     * @param id O UUID do usuário a ser buscado.
     * @return A entidade {@link User} encontrada.
     * @throws UserNotFoundException se o usuário não for encontrado.
     */
    private User findUserByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new UserNotFoundException("User not found with ID: " + id);
                });
    }
}
