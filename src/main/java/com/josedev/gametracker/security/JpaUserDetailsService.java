package com.josedev.gametracker.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;

/**
 * Loads users from the database for the login (username + password).
 * A banned user is returned as "locked", so Spring Security rejects the login with LockedException.
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public JpaUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .accountLocked(user.isBanned())
                .build();
    }

}
