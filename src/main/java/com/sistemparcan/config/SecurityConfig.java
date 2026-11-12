package com.sistemparcan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sistemparcan.entity.Usuario;
import com.sistemparcan.repository.UsuarioRepository;
import com.sistemparcan.service.PasswordService;

/**
 * Configuración de seguridad: protege los módulos y la API,
 * define el formulario de login y el cierre de sesión.
 */
@Configuration
public class SecurityConfig {

    /**
     * Carga el usuario desde la base de datos usando su nombre normalizado
     * (minúsculas y sin espacios) y lo expone a Spring Security.
     */
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> {
            String normalizado = username == null ? "" : username.trim().toLowerCase().replaceAll("\\s+", "");
            Usuario usuario = usuarioRepository.findByUsuarioNormalizado(normalizado)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            return User.withUsername(usuario.getUsuario())
                    .password(usuario.getContrasena())
                    .roles("USUARIO")
                    .disabled(Boolean.TRUE.equals(usuario.getEliminado()))
                    .build();
        };
    }

    /**
     * Codificador de contraseñas compatible con BCrypt y con contraseñas
     * antiguas almacenadas en texto plano (hacia atrás).
     */
    @Bean
    public PasswordEncoder passwordEncoder(PasswordService passwordService) {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return passwordService.hash(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return passwordService.verificar(rawPassword.toString(), encodedPassword);
            }
        };
    }

    /**
     * Cadena de filtros de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Se desactiva CSRF porque las peticiones se hacen desde el mismo frontend
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Recursos accesibles sin autenticación (login y registro)
                .requestMatchers(
                        "/login.html", "/registro.html",
                        "/css/**", "/js/**", "/images/**",
                        "/api/login", "/api/registro",
                        "/error"
                ).permitAll()
                // La lista de cargos es pública (solo lectura) para el formulario de registro
                .requestMatchers(HttpMethod.GET, "/api/cargos").permitAll()
                // Todo lo demás requiere sesión autenticada
                .anyRequest().authenticated()
            )
            // Formulario de inicio de sesion y redireccion segun el resultado
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .usernameParameter("usuario")
                .passwordParameter("contrasena")
                .defaultSuccessUrl("/index.html", true)
                .failureHandler((request, response, exception) -> {
                    String param = (exception instanceof DisabledException) ? "disabled" : "error";
                    response.sendRedirect("/login.html?" + param);
                })
                .permitAll()
            )
            // Cierre de sesion: invalida la sesion y elimina la cookie
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }
}
