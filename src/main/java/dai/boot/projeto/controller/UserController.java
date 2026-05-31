package dai.boot.projeto.controller;

import dai.boot.projeto.entities.User;
import dai.boot.projeto.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Listar todos os utilizadores
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Obter um utilizador por id
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> opt = userRepository.findById(id);
        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Criar utilizador
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username é obrigatório.");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email é obrigatório.");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Já existe um utilizador com esse username.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Já existe um utilizador com esse email.");
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    // Editar utilizador
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User details) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = opt.get();

        if (details.getUsername() != null && !details.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(details.getUsername())) {
                return ResponseEntity.badRequest().body("Já existe um utilizador com esse username.");
            }
            user.setUsername(details.getUsername());
        }

        if (details.getEmail() != null && !details.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(details.getEmail())) {
                return ResponseEntity.badRequest().body("Já existe um utilizador com esse email.");
            }
            user.setEmail(details.getEmail());
        }

        if (details.getPassword() != null) {
            user.setPassword(details.getPassword());
        }

        if (details.getRole() != null) {
            user.setRole(details.getRole());
        }

        user.setOnline(details.isOnline());

        return ResponseEntity.ok(userRepository.save(user));
    }

    // Apagar utilizador
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Alterar role/permissão
    @PostMapping("/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestParam String role) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = opt.get();
        user.setRole(role);
        return ResponseEntity.ok(userRepository.save(user));
    }

    // Listar utilizadores online
    @GetMapping("/online")
    public List<User> getOnlineUsers() {
        return userRepository.findByIsOnline(true);
    }
}