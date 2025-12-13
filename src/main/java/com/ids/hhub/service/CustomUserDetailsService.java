package com.ids.hhub.service;

import com.ids.hhub.model.User;
import com.ids.hhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Cerca l'utente nel DB tramite email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));

        // 2. Converti il tuo User (Entity) in un UserDetails (Spring Security Object)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(), // Deve essere la password hashata (BCrypt)
                Collections.singletonList(new SimpleGrantedAuthority(user.getPlatformRole().name())) // Es. "ADMIN" o "USER"
        );
    }
}