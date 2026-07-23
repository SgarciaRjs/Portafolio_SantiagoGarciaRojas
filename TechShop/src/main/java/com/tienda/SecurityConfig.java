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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import com.tienda.service.RutaService;
import com.tienda.domain.Ruta;

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

    @Autowired
    private RutaService rutaService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var rutas = rutaService.getRutas();
        http.authorizeHttpRequests(requests -> {
            for (Ruta ruta : rutas) {
                if (ruta.isRequiereRol()) {
                    requests.requestMatchers(ruta.getRuta()).hasRole(ruta.getRol().getRol());
                } else {
                    requests.requestMatchers(ruta.getRuta()).permitAll();
                }
            }
            requests.anyRequest().authenticated();
        });
        http.formLogin(form -> form // Configuración de formulario de login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout // Configuración de logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        ).exceptionHandling(exceptions -> exceptions // Manejo de excepciones
                .accessDeniedPage("/acceso_denegado")
        ).sessionManagement(session -> session // Configuración de sesiones
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
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

    @Autowired
    public void configurerGlobal(AuthenticationManagerBuilder build,
                                 @Lazy PasswordEncoder passwordEncoder,
                                 @Lazy UserDetailsService userDetailsService) throws Exception {
        build.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

}
