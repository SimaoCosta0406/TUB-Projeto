package dai.boot.projeto.repository;

import dai.boot.projeto.entities.User; // Caminho correto agora
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar utilizador pelo username (para login)
    Optional<User> findByUsername(String username);

    // Buscar utilizador pelo email
    Optional<User> findByEmail(String email);

    // Verificar se username já existe
    boolean existsByUsername(String username);

    // Verificar se email já existe
    boolean existsByEmail(String email);
}
