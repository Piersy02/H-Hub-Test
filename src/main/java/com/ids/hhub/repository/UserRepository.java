package com.ids.hhub.repository;

import com.ids.hhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Trova un utente completo tramite email
    // Restituisce Optional perché l'utente potrebbe non esistere
    Optional<User> findByEmail(String email)
    ;

    // 2. Controlla solo se esiste (utile per la registrazione)
    // Restituisce true/false, è più leggero di findByEmail
    boolean existsByEmail(String email);
}