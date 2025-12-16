package com.ids.hhub.config;

import com.ids.hhub.model.User;
import com.ids.hhub.model.enums.PlatformRole;
import com.ids.hhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner loadData(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("--- INIZIO CONTROLLO E POPOLAMENTO UTENTI ---");

            // 1. CREA ADMIN (Se non esiste) - Password: admin123
            createUserIfNotFound(userRepo, passwordEncoder,
                    "Super", "Admin", "admin@hackhub.com", "admin123", PlatformRole.ADMIN);

            // 2. CREA EVENT CREATOR (Se non esiste) - Password: password
            createUserIfNotFound(userRepo, passwordEncoder,
                    "Organ", "Creator", "creator@hackhub.com", "password", PlatformRole.EVENT_CREATOR);

            // 3. CREA 18 UTENTI STANDARD (Se non esistono) - Password: password
            for (int i = 1; i <= 18; i++) {
                createUserIfNotFound(userRepo, passwordEncoder,
                        "Utente", "Numero" + i, "user" + i + "@hackhub.com", "password", PlatformRole.USER);
            }

            System.out.println("--- FINE POPOLAMENTO ---");
        };
    }

    // Metodo helper per evitare di ripetere l'if 20 volte
    private void createUserIfNotFound(UserRepository repo, PasswordEncoder encoder,
                                      String name, String surname, String email,
                                      String rawPassword, PlatformRole role) {

        // La tua logica di controllo: se esiste, non fare nulla
        if (repo.existsByEmail(email)) {
            return;
        }

        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(encoder.encode(rawPassword));
        user.setPlatformRole(role);

        repo.save(user);
        System.out.println("Creato utente: " + email + " (" + role + ")");
    }
}