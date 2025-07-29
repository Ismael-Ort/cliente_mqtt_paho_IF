package org.javadominicano.cmp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize y otras anotaciones de seguridad a nivel de método
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/login", "/ws/**").permitAll()

                // Rutas solo accesibles por ADMIN
                .requestMatchers(
                    "/dashboard/administrar-estaciones/**",
                    "/dashboard/estaciones/guardar",
                    "/dashboard/estaciones/editar/**",
                    "/dashboard/estaciones/borrar/**",
                    "/dashboard/estaciones/actualizar"
                ).hasRole("ADMIN")

                // Acceso para ADMIN y USER
                .requestMatchers(
                    "/dashboard/estaciones",
                    "/dashboard/reportes/**",
                    "/api/**"
                ).hasAnyRole("ADMIN", "USER")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard/estaciones", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")  // Permite logout desde GET también
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(e -> e
                .accessDeniedPage("/access-denied")
            )
            .csrf(csrf -> csrf.disable()) // ✅ Desactiva CSRF para permitir logout vía <a>
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withUsername("admin")
            .password("{noop}admin123")
            .roles("ADMIN")
            .build();

        UserDetails usuario = User.withUsername("usuario")
            .password("{noop}usuario123")
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(admin, usuario);
    }
}
