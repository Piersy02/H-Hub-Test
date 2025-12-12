package com.ids.hhub.Service;
import com.ids.hhub.dto.LoginDto;
import com.ids.hhub.dto.RegisterUserDto;
import com.ids.hhub.model.User;
import com.ids.hhub.model.PlatformRole;
import com.ids.hhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- REGISTRAZIONE ---
    public User register(RegisterUserDto dto) {
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email già registrata!");
        }

        User newUser = new User();
        newUser.setName(dto.getName());
        newUser.setSurname(dto.getSurname());
        newUser.setEmail(dto.getEmail());

        // Cifriamo la password prima di salvarla
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Di default è un utente semplice
        newUser.setPlatformRole(PlatformRole.USER);

        return userRepo.save(newUser);
    }

    // --- LOGIN (Semplificato per API) ---
    public User login(LoginDto dto) {
        // 1. Cerca l'utente
        User user = UserRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // 2. Confronta la password inviata con quella cifrata nel DB
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password errata");
        }

        return user;
    }

}
