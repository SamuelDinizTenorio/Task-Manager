package br.com.gerenciador.sistema_gerenciamento_tarefas.controller;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user.UserResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user.UserUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller para operações relacionadas a usuários.
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retorna uma lista paginada de todos os usuários do sistema.
     * O tamanho padrão da página é 10 e a ordenação padrão é por 'login'.
     * A paginação pode ser customizada via parâmetros de URL (ex: ?page=0&size=20&sort=login,asc).
     * Acesso restrito a administradores.
     *
     * @param pageable O objeto de paginação injetado pelo Spring a partir dos parâmetros da requisição.
     * @return Uma página de UserResponseDTO com os dados públicos dos usuários.
     */
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(@PageableDefault(size = 10, sort = "login") Pageable pageable) {
        log.info("Request received to fetch all users with pagination: {}", pageable);
        Page<UserResponseDTO> users = userService.getAllUsers(pageable);
        log.info("Returning {} users on page {}", users.getNumberOfElements(), users.getNumber());
        return ResponseEntity.ok(users);
    }

    /**
     * Busca e retorna um usuário pelo seu ID.
     * Acesso restrito a administradores.
     *
     * @param id O UUID do usuário a ser buscado.
     * @return Um UserResponseDTO com os dados públicos do usuário.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        log.info("Request received to fetch user by ID: {}", id);
        UserResponseDTO user = userService.getUserById(id);
        log.info("User with ID {} found successfully.", id);
        return ResponseEntity.ok(user);
    }

    /**
     * Atualiza as informações de um usuário (login e/ou senha).
     * Um usuário pode atualizar seu próprio perfil. Um ADMIN pode atualizar o perfil de qualquer usuário.
     *
     * @param id O UUID do usuário a ser atualizado.
     * @param data DTO com os dados de atualização.
     * @param currentUser O usuário atualmente autenticado.
     * @return Um UserResponseDTO com os dados atualizados do usuário.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id,
                                                      @RequestBody @Valid UserUpdateDTO data,
                                                      @AuthenticationPrincipal User currentUser) {
        log.info("Request received to update user with ID: {} by current user: {}", id, currentUser.getLogin());
        UserResponseDTO updatedUser = userService.updateUser(id, data, currentUser);
        log.info("User with ID: {} updated successfully.", id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Atualiza a role de um usuário específico.
     * Acesso restrito a administradores.
     *
     * @param id O UUID do usuário a ter a role atualizada.
     * @param newRole A nova role a ser atribuída ao usuário (ex: USER, ADMIN).
     * @param currentUser O usuário atualmente autenticado realizando a operação.
     * @return Um UserResponseDTO com os dados atualizados do usuário.
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> updateUserRole(@PathVariable UUID id,
                                                          @RequestBody UserRole newRole,
                                                          @AuthenticationPrincipal User currentUser) {
        log.info("Request received to update role for user ID: {} to {} by current user: {}", id, newRole, currentUser.getLogin());
        UserResponseDTO updatedUser = userService.updateUserRole(id, newRole, currentUser);
        log.info("User ID: {} role updated successfully.", id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deleta um usuário do sistema.
     * Acesso restrito a administradores.
     *
     * @param id O UUID do usuário a ser deletado.
     * @param currentUser O usuário atualmente autenticado realizando a operação.
     * @return Um ResponseEntity com status 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id,
                                           @AuthenticationPrincipal User currentUser) {
        log.info("Request received to delete user with ID: {} by current user: {}", id, currentUser.getLogin());
        userService.deleteUser(id, currentUser);
        log.info("User with ID {} deleted successfully.", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna os dados do usuário atualmente autenticado.
     *
     * @param currentUser O principal do usuário autenticado, injetado pelo Spring Security.
     * @return Um UserResponseDTO com os dados públicos do usuário logado.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        log.info("Request received to fetch current authenticated user: {}", currentUser.getUsername());
        UserResponseDTO userResponse = userService.getCurrentUser(currentUser);
        log.info("Successfully fetched data for user: {}", currentUser.getUsername());
        return ResponseEntity.ok(userResponse);
    }
}
