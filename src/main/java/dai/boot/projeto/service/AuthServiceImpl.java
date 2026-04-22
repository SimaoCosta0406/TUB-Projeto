package dai.boot.projeto.service;

import dai.boot.projeto.entities.User;
import dai.boot.projeto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User não encontrado"));
        if (!user.getPassword().equals(password)) throw new RuntimeException("Senha errada");
        
        user.setOnline(true); // TAREFA UC1
        userRepository.save(user);
        return "mock-token-" + UUID.randomUUID();
    }

    @Override
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOnline(false); // TAREFA UC1
            userRepository.save(user);
        });
    }
}