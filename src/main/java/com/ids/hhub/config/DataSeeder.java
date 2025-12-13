package com.ids.hhub.config;

import com.ids.hhub.model.User;
import com.ids.hhub.model.PlatformRole;
import com.ids.hhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            // Controlla se esiste già un admin (per non ricrearlo a ogni avvio)
            if (!userRepo.existsByEmail("admin@hackhub.com")) {
                User admin = new User();
                admin.setName("Super");
                admin.setSurname("Admin");
                admin.setEmail("admin@hackhub.com");
                admin.setPassword(passwordEncoder.encode("admin123")); // Password nota
                admin.setPlatformRole(PlatformRole.ADMIN); // imposti il ruolo piattaforma di admin

                userRepo.save(admin);
                System.out.println("ADMIN DI DEFAULT CREATO: admin@hackhub.com / admin123");
            }
        };
    }
}
