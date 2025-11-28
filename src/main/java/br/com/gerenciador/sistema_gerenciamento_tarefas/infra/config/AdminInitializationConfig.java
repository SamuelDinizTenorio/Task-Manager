package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.config;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração para inicializar um usuário administrador padrão se nenhum existir.
 * Esta classe é executada uma vez quando a aplicação inicia.
 */
@Configuration
@Slf4j
public class AdminInitializationConfig {
    @Value("${api.security.admin-password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verifica se já existe algum usuário com a role ADMIN
            if (userRepository.findFirstByRole(UserRole.ADMIN).isEmpty()) {
                log.info("No admin user found. Creating default admin user.");
                User adminUser = new User("admin", passwordEncoder.encode(adminPassword), UserRole.ADMIN);
                userRepository.save(adminUser);
                log.info("Default admin user created with login 'admin'.");
            } else {
                log.info("Admin user already exists. Skipping creation.");
            }
        };
    }
}
