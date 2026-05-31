package dai.boot.projeto.controller;

import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import dai.boot.projeto.service.Auth0ManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth0-users")
@CrossOrigin(origins = "*")
public class Auth0UserController {

    private final Auth0ManagementService auth0Service;

    public Auth0UserController(Auth0ManagementService auth0Service) {
        this.auth0Service = auth0Service;
    }

    /**
     * Lista todos os utilizadores do Auth0
     */
    @GetMapping
    public ResponseEntity<?> listUsers() {
        try {
            List<User> users = auth0Service.listUsers();
            return ResponseEntity.ok(users);
        } catch (Auth0Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar utilizadores: " + e.getMessage());
        }
    }

    /**
     * Cria um novo utilizador no Auth0
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String password = payload.get("password");
            String name = payload.get("name");
            String roleId = payload.get("roleId");

            // Validações
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Email é obrigatório");
            }
            if (password == null || password.length() < 8) {
                return ResponseEntity.badRequest().body("Password deve ter pelo menos 8 caracteres");
            }

            User user = auth0Service.createUser(email, password, name, roleId);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
            
        } catch (Auth0Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar utilizador: " + e.getMessage());
        }
    }

    /**
     * Apaga um utilizador do Auth0
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            auth0Service.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Auth0Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao apagar utilizador: " + e.getMessage());
        }
    }

    /**
     * Lista todos os roles disponíveis
     */
    @GetMapping("/roles")
    public ResponseEntity<?> listRoles() {
        try {
            List<Map<String, Object>> roles = auth0Service.listRoles();
            return ResponseEntity.ok(roles);
        } catch (Auth0Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar roles: " + e.getMessage());
        }
    }

    /**
     * Obtém os roles de um utilizador
     */
    @GetMapping("/{userId}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable String userId) {
        try {
            List<Map<String, Object>> roles = auth0Service.getUserRoles(userId);
            return ResponseEntity.ok(roles);
        } catch (Auth0Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao obter roles: " + e.getMessage());
        }
    }

    /**
     * Atribui um role a um utilizador
     */
    @PostMapping("/{userId}/roles")
    public ResponseEntity<?> assignRole(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        try {
            String roleId = payload.get("roleId");
            if (roleId == null || roleId.isBlank()) {
                return ResponseEntity.badRequest().body("roleId é obrigatório");
            }
            
            auth0Service.assignRoleToUser(userId, roleId);
            return ResponseEntity.ok().body("Role atribuído com sucesso");
        } catch (Auth0Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atribuir role: " + e.getMessage());
        }
    }

    /**
     * Remove um role de um utilizador
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<?> removeRole(@PathVariable String userId, @PathVariable String roleId) {
        try {
            auth0Service.removeRoleFromUser(userId, roleId);
            return ResponseEntity.ok().body("Role removido com sucesso");
        } catch (Auth0Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao remover role: " + e.getMessage());
        }
    }

    /**
     * Obtém detalhes de um utilizador
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        try {
            User user = auth0Service.getUser(userId);
            return ResponseEntity.ok(user);
        } catch (Auth0Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao obter utilizador: " + e.getMessage());
        }
    }
}
