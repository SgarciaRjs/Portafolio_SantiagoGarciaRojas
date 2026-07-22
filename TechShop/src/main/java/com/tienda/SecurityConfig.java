package com.tienda;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    // Recursos estáticos y páginas de acceso público (sin login)
    public static final String[] PUBLIC_URLS = {
        "/", "/index", "/fav/**", "/carrito/**", "/consultas/**", "/registro/**",
        "/js/**", "/css/**", "/webjars/**", "/login", "/acceso_denegado",
        // Los listados de producto y categoría son visibles para todos (vitrina pública)
        "/producto/listado", "/categoria/listado"
    };

    // Solo usuarios autenticados como USUARIO, VENDEDOR o ADMIN
    public static final String[] USUARIO_URLS = {
        "/facturar/**"
    };

    // Gestión de catálogo: solo VENDEDOR y ADMIN pueden agregar/editar/eliminar
    public static final String[] VENDEDOR_O_ADMIN_URLS = {
        "/producto/**", "/categoria/**", "/pruebas/**"
    };

    // Solo ADMIN: gestión de usuarios, roles y configuración del sistema
    public static final String[] ADMIN_URLS = {
        "/usuario/**", "/role/**", "/usuario_rol/**", "/ruta/**", "/constante/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(USUARIO_URLS).hasAnyRole("USUARIO", "VENDEDOR", "ADMIN")
                .requestMatchers(VENDEDOR_O_ADMIN_URLS).hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/acceso_denegado")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Este método será reemplazado la siguiente semana
    @Bean
    public UserDetailsService users(PasswordEncoder passwordEncoder) {
        UserDetails juan = User.builder()
                .username("juan")
                .password(passwordEncoder.encode("123"))
                .roles("ADMIN")
                .build();
        UserDetails rebeca = User.builder()
                .username("rebeca")
                .password(passwordEncoder.encode("456"))
                .roles("VENDEDOR")
                .build();
        UserDetails pedro = User.builder()
                .username("pedro")
                .password(passwordEncoder.encode("789"))
                .roles("USUARIO")
                .build();
        return new InMemoryUserDetailsManager(juan, rebeca, pedro);
    }

}
