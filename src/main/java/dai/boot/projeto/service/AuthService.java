package dai.boot.projeto.service;
import dai.boot.projeto.entities.User;

public interface AuthService {
    String login(String username, String password);
    void logout(String username);
}