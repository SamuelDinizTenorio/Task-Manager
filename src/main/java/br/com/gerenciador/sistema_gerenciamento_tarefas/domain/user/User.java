package br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user;

import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.UserAlreadyExistsException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.validation.ValidationUtils;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Representa a entidade de usuário no sistema.
 * Esta classe serve a um propósito duplo:
 * 1. É uma entidade JPA mapeada para a tabela "users" no banco de dados.
 * 2. Implementa a interface {@link UserDetails} para integração com o Spring Security,
 *    fornecendo os detalhes do usuário para o processo de autenticação e autorização.
 */
@Table(name = "users")
@Entity(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; //ID único do usuário, gerado como UUID.

    private String login; // Login do usuário, utilizado para autenticação.
    private String password; // Senha do usuário, armazenada de forma criptografada.

    @Enumerated(EnumType.STRING)
    private UserRole role; // Papel (role) do usuário no sistema (ex: USER, ADMIN).

    /**
     * Construtor para criar um novo usuário.
     * @param login O login do usuário.
     * @param encryptedPassword A senha já criptografada do usuário.
     * @param role O papel do usuário.
     */
    public User(String login, String encryptedPassword, UserRole role) {
        this.login = login;
        this.password = encryptedPassword;
        this.role = role;
    }

    /**
     * Atribui uma nova role ao usuário.
     * @param newRole A nova role a ser atribuída.
     */
    public void assignRole(UserRole newRole) {
        this.role = newRole;
    }

    /**
     * Altera o login do usuário, verificando se o novo login já está em uso.
     * @param newLogin O novo login a ser atribuído.
     * @param userRepository O repositório de usuários para verificar a disponibilidade do login.
     */
    public void changeLogin(String newLogin, UserRepository userRepository) {
        ValidationUtils.validateStringNotNullOrBlank(newLogin, "New login");
        if (this.login.equals(newLogin)) {
            return; // Não faz nada se o login for o mesmo
        }
        if (userRepository.findByLogin(newLogin).isPresent()) {
            throw new UserAlreadyExistsException("Login '" + newLogin + "' is already in use.");
        }
        this.login = newLogin;
    }

    /**
     * Altera a senha do usuário, codificando a nova senha.
     * @param newPassword A nova senha a ser atribuída.
     * @param passwordEncoder O codificador de senhas para criptografar a nova senha.
     */
    public void changePassword(String newPassword, PasswordEncoder passwordEncoder) {
        ValidationUtils.validateStringNotNullOrBlank(newPassword, "New password");
        this.password = passwordEncoder.encode(newPassword);
    }

    /**
     * Retorna as autoridades (roles) concedidas ao usuário.
     * Um ADMIN também possui a role de USER.
     * @return Uma coleção de GrantedAuthority.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    /**
     * Retorna o nome de usuário (login) utilizado para autenticação.
     * @return O login do usuário.
     */
    @Override
    public String getUsername() {
        return login;
    }

    /**
     * Indica se a conta do usuário expirou.
     * Para esta aplicação, todas as contas são consideradas não expiradas por padrão.
     * @return {@code true}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica se o usuário está bloqueado ou desbloqueado.
     * Para esta aplicação, todas as contas são consideradas desbloqueadas por padrão.
     * @return {@code true}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica se as credenciais (senha) do usuário expiraram.
     * Para esta aplicação, as credenciais são consideradas não expiradas por padrão.
     * @return {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica se o usuário está habilitado ou desabilitado.
     * Para esta aplicação, todos os usuários são considerados habilitados por padrão.
     * @return {@code true}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
