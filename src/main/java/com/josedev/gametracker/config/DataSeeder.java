package com.josedev.gametracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.josedev.gametracker.user.Role;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;

/**
 * Creates the initial accounts the first time the app starts (empty users table).
 * Passwords come from properties so they are hashed with BCrypt like any other user.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String demoPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin-password}") String adminPassword,
            @Value("${app.seed.demo-password}") String demoPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0)
            return;

        userRepository.save(new UserEntity("admin", "admin@gametracker.dev", passwordEncoder.encode(adminPassword),
                Role.ADMIN));
        userRepository.save(new UserEntity("demo", "demo@gametracker.dev", passwordEncoder.encode(demoPassword),
                Role.USER));

        log.info("Seeded default users: admin (ADMIN) and demo (USER)");
    }

}
