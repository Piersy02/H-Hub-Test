package com.ids.hhub.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disabilita CSRF e Form Login (HTML)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 2. Abilita Basic Auth (per Swagger/Postman)
                .httpBasic(withDefaults())

                // 3. GESTIONE ECCEZIONI (Risposte JSON invece di HTML o redirect)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{ \"error\": \"Non autorizzato. Effettua il login.\" }");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{ \"error\": \"Accesso Negato: Non hai i permessi necessari.\" }");
                        })
                )

                // 4. GESTIONE AUTORIZZAZIONI URL (L'ordine è importante)
                .authorizeHttpRequests(auth -> auth
                        // A. INFRASTRUTTURA (Swagger, H2, Auth) - Tutto Pubblico
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // B. HACKATHON PUBBLICI (Solo Lettura)
                        .requestMatchers(HttpMethod.GET, "/api/hackathons").permitAll()      // Lista
                        .requestMatchers(HttpMethod.GET, "/api/hackathons/{id}").permitAll() // Dettaglio singolo (IMPORTANTE)

                        // C. CREAZIONE HACKATHON (Solo chi ha i permessi specifici)
                        .requestMatchers(HttpMethod.POST, "/api/hackathons").hasAnyAuthority("EVENT_CREATOR", "ADMIN")

                        // D. AMMINISTRAZIONE
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // E. TUTTO IL RESTO (Teams, Submission, Dashboard Staff, ecc.)
                        // Richiede solo di essere loggati (i controlli specifici sono nei Service)
                        .anyRequest().authenticated()
                )

                // 5. Fix per H2 Console (altrimenti vedi pagina bianca)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                // 6. Logout Custom
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Logout effettuato con successo!");
                        })
                );

        return http.build();
    }
}