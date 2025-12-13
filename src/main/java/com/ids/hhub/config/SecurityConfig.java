package com.ids.hhub.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disabilita CSRF (necessario per le API REST non browser-based)
                .csrf(csrf -> csrf.disable())

                // Gestione delle autorizzazioni URL
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoint pubblici (Visitor)
                        .requestMatchers("/api/auth/**").permitAll()      // Login e Register aperti a tutti
                        .requestMatchers("/api/hackathons").permitAll()   // Lista hackathon pubblica
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger aperto
                        .requestMatchers("/h2-console/**").permitAll()  // Lascia passare liberamente la console H2
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()   // 2. Tutto il resto richiede autenticazione
                )

                // 3. ABILITARE I FRAME
                // Senza questo, vedrai la pagina bianca o errore dopo il login
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // Usa Basic Auth per semplicità nei test (Postman chiederà user/pass)
                .httpBasic(basic -> {})

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // 1. L'URL per fare logout
                        .invalidateHttpSession(true)   // 2. Distrugge la sessione server
                        .deleteCookies("JSESSIONID")   // 3. Cancella il cookie
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // 4. Risposta custom per le API (invece di redirect)
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Logout effettuato con successo!");
                        })
                );

        return http.build();
    }
}

