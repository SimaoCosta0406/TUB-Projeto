package dai.boot.projeto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de Segurança PERMISSIVA para OAuth2/OIDC com Auth0.
 * 
 * Esta configuração:
 * - Mantém o sistema de painéis (/api/auth/**) completamente público
 * - Permite todos os ficheiros estáticos públicos
 * - NÃO força autenticação JWT (opcional)
 * - Permite visitantes sem login acederem à aplicação
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ===== PÚBLICO: Ficheiros estáticos =====
                .requestMatchers(
                    "/",
                    "/*.html",
                    "/*.css",
                    "/*.js",
                    "/*.png",
                    "/*.jpg",
                    "/*.json"
                ).permitAll()
                
                // ===== PÚBLICO: Sistema de painéis (NÃO TOCAR) =====
                .requestMatchers("/api/auth/**").permitAll()

                // ===== PÚBLICO: Auth0 Management (gestão de utilizadores) =====
                .requestMatchers("/api/auth0-users/**").permitAll()

                // ===== PÚBLICO: Endpoints de leitura (visitantes) =====
                .requestMatchers(HttpMethod.GET, "/api/stops/**").permitAll()
                
                // ===== RESTO: Permite tudo (sem forçar JWT) =====
                // Isto permite que a aplicação funcione sem autenticação obrigatória
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable()) // Desabilitar CSRF para APIs REST
            .cors(cors -> cors.configure(http)); // Habilitar CORS
        
        return http.build();
    }
}
