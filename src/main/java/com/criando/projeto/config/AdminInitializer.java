package com.criando.projeto.config;

import com.criando.projeto.entities.User;
import com.criando.projeto.entities.enums.UserRole;
import com.criando.projeto.repositories.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByRole(UserRole.ADMIN)) {
            User admin = new User();
            admin.setName("Administrador");
            admin.setEmail("admin@admin.com");
            admin.setPhone("11999999999");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(UserRole.ADMIN);

            userRepository.save(admin);
            System.out.println("✅ Usuário ADMIN criado com sucesso.");
        } else {
            System.out.println("ℹ️ Usuário ADMIN já existe, nenhum novo admin criado.");
        }
    }
}
