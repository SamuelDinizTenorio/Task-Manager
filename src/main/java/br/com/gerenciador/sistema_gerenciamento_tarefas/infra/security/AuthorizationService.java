package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security;

import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por carregar os dados de um usuário para o Spring Security.
 * Esta classe implementa a interface {@link UserDetailsService}, atuando como uma ponte
 * entre o repositório de usuários da aplicação e o framework de segurança.
 */
@Service
public class AuthorizationService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Localiza um usuário com base no seu nome de usuário (neste caso, o login).
     * Este método é chamado pelo Spring Security durante o processo de autenticação.
     *
     * @param username o nome de usuário que identifica o usuário cujos dados são necessários.
     * @return um registro de usuário totalmente preenchido (nunca {@code null}).
     * @throws UsernameNotFoundException se o usuário não puder ser encontrado no banco de dados.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("The user with the username: " + username + ". Not found in the database."));
    }
}
