package dai.boot.projeto.config;

import dai.boot.projeto.entities.User;
import dai.boot.projeto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        createPanelUser();
        createWorkerUser();
    }

    private void createPanelUser() {
        String username = "painel@tub.pt";
        if (!userRepository.existsByUsername(username)) {
            User painel = new User();
            painel.setUsername(username);
            painel.setPassword("painel");
            painel.setEmail(username);
            painel.setRole("ADMIN");
            painel.setOnline(false);
            userRepository.save(painel);
        }
    }

    private void createWorkerUser() {
        String username = "worker@tub.pt";
        if (!userRepository.existsByUsername(username)) {
            User worker = new User();
            worker.setUsername(username);
            worker.setPassword("worker");
            worker.setEmail(username);
            worker.setRole("WORKER");
            worker.setOnline(false);
            userRepository.save(worker);
        }
    }
}
