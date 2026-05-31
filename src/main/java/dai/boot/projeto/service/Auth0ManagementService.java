package dai.boot.projeto.service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Auth0ManagementService {

    @Value("${auth0.management.domain}")
    private String domain;

    @Value("${auth0.management.clientId}")
    private String clientId;

    @Value("${auth0.management.clientSecret}")
    private String clientSecret;

    @Value("${auth0.management.audience}")
    private String audience;

    private String accessToken;
    private long tokenExpiresAt = 0;

    /**
     * Obtém um token de acesso para a Management API
     */
    private String getAccessToken() throws Auth0Exception {
        long now = System.currentTimeMillis() / 1000;
        
        // Se o token ainda é válido (com margem de 5 minutos), reutiliza
        if (accessToken != null && now < tokenExpiresAt - 300) {
            return accessToken;
        }

        // Obter novo token
        AuthAPI authAPI = AuthAPI.newBuilder(domain, clientId, clientSecret).build();
        Request<TokenHolder> authRequest = authAPI.requestToken(audience);
        TokenHolder holder = authRequest.execute().getBody();
        
        accessToken = holder.getAccessToken();
        tokenExpiresAt = now + holder.getExpiresIn();
        
        return accessToken;
    }

    /**
     * Obtém instância da Management API
     */
    private ManagementAPI getManagementAPI() throws Auth0Exception {
        String token = getAccessToken();
        return ManagementAPI.newBuilder(domain, token).build();
    }

    /**
     * Lista todos os utilizadores do Auth0
     */
    public List<User> listUsers() throws Auth0Exception {
        ManagementAPI mgmt = getManagementAPI();
        UserFilter filter = new UserFilter().withPage(0, 100); // Máximo 100 users
        return mgmt.users().list(filter).execute().getBody().getItems();
    }

    /**
     * Cria um novo utilizador no Auth0
     */
    public User createUser(String email, String password, String name, String roleId) throws Auth0Exception {
        ManagementAPI mgmt = getManagementAPI();
        
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password.toCharArray());
        newUser.setConnection("Username-Password-Authentication");
        newUser.setEmailVerified(true); // Auto-verificar email
        
        if (name != null && !name.isBlank()) {
            newUser.setName(name);
        }

        // Criar utilizador
        User createdUser = mgmt.users().create(newUser).execute().getBody();

        // Atribuir role se fornecido
        if (roleId != null && !roleId.isBlank()) {
            assignRoleToUser(createdUser.getId(), roleId);
        }

        return createdUser;
    }

    /**
     * Apaga um utilizador do Auth0
     */
    public void deleteUser(String userId) throws Auth0Exception {
        ManagementAPI mgmt = getManagementAPI();
        mgmt.users().delete(userId).execute();
    }

    /**
     * Lista todos os roles disponíveis no Auth0
     */
        public List<Map<String, Object>> listRoles() throws Auth0Exception {
            try {
                String token = getAccessToken();
                
                // Fazer request HTTP direto à API de roles
                String url = "https://" + domain + "/api/v2/roles";
                
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build();
                
                java.net.http.HttpResponse<String> response = client.send(request, 
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                
                System.out.println("=== ROLES API RESPONSE ===");
                System.out.println("Status: " + response.statusCode());
                System.out.println("Body: " + response.body());
                
                if (response.statusCode() == 200) {
                    // Parse JSON manualmente
                    String jsonResponse = response.body();
                    
                    // Converter JSON para List<Map>
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<Map<String, Object>> roles = mapper.readValue(jsonResponse, 
                            new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                    
                    System.out.println("Roles encontrados: " + roles.size());
                    return roles;
                } else {
                    throw new Auth0Exception("Erro ao listar roles: HTTP " + response.statusCode());
                }
                
            } catch (Exception e) {
                System.err.println("ERRO ao listar roles:");
                e.printStackTrace();
                throw new Auth0Exception("Erro ao listar roles: " + e.getMessage());
            }
        }


    /**
 * Obtém os roles de um utilizador específico
 */
public List<Map<String, Object>> getUserRoles(String userId) throws Auth0Exception {
    try {
        String token = getAccessToken();
        
        // URL encode do userId (para lidar com caracteres especiais como |)
        String encodedUserId = java.net.URLEncoder.encode(userId, java.nio.charset.StandardCharsets.UTF_8);
        
        // Fazer request HTTP direto à API de roles do utilizador
        String url = "https://" + domain + "/api/v2/users/" + encodedUserId + "/roles";
        
        System.out.println("=== USER ROLES API REQUEST ===");
        System.out.println("User ID original: " + userId);
        System.out.println("User ID encoded: " + encodedUserId);
        System.out.println("URL: " + url);
        
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        
        java.net.http.HttpResponse<String> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        
        if (response.statusCode() == 200) {
            // Parse JSON
            String jsonResponse = response.body();
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> roles = mapper.readValue(jsonResponse, 
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
            
            System.out.println("Roles do utilizador: " + roles.size());
            return roles;
        } else {
            System.err.println("Erro ao obter roles do utilizador: HTTP " + response.statusCode());
            return new ArrayList<>();
        }
        
    } catch (Exception e) {
        System.err.println("ERRO ao obter roles do utilizador " + userId + ":");
        e.printStackTrace();
        return new ArrayList<>();
    }
}



    /**
     * Atribui um role a um utilizador
     */
    public void assignRoleToUser(String userId, String roleId) throws Auth0Exception {
        ManagementAPI mgmt = getManagementAPI();
        mgmt.users().addRoles(userId, List.of(roleId)).execute();
    }

    /**
     * Remove um role de um utilizador
     */
    public void removeRoleFromUser(String userId, String roleId) throws Auth0Exception {
        ManagementAPI mgmt = getManagementAPI();
        mgmt.users().removeRoles(userId, List.of(roleId)).execute();
    }

    /**
     * Obtém um utilizador por ID
     */
    public User getUser(String userId) throws Auth0Exception {
        ManagementAPI mgmt = getManagementAPI();
        return mgmt.users().get(userId, null).execute().getBody();
    }
}
