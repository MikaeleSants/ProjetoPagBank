package com.criando.projeto.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;

/*
@Configuration → Indica que essa classe define configurações do Spring Security.
@RequiredArgsConstructor → Faz com que o Lombok gere automaticamente um construtor para injetar dependências marcadas como final.
*/
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private OrderSecurity orderSecurity;
    @Autowired
    private UserSecurity userSecurity;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // DE VEM A REQUEST
                .authorizeRequests()

                // Permissões para qualquer pessoa (criar usuário comum)
                .requestMatchers(HttpMethod.POST, "/users").permitAll() // Permitir qualquer pessoa criar um usuário

                // Permissões para usuários comuns
                .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**", "/cupons/**").permitAll()  // Acesso público
                .requestMatchers(HttpMethod.GET, "/orders/{id}")
                .access("@orderSecurity.checkOrderOwnership(authentication, #id)")  // Usuário pode acessar somente os próprios pedidos
                .requestMatchers(HttpMethod.GET, "/orders").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/orders").hasRole("USER")
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}")
                .access("@orderSecurity.checkOrderOwnership(authentication, #id)")  // Usuário pode alterar somente os próprios pedidos

                // Permissões para gerenciamento do próprio usuário
                .requestMatchers(HttpMethod.GET, "/users/me").access("@userSecurity.checkUserOwnership(authentication)")
                .requestMatchers(HttpMethod.PATCH, "/users/me").access("@userSecurity.checkUserOwnership(authentication)")
                .requestMatchers(HttpMethod.GET, "/users/{id}").access("@userSecurity.checkUserOwnership(authentication, #id) or hasRole('ADMIN')")

                // Permissões para administradores
                .requestMatchers(HttpMethod.POST, "/admin/users").hasRole("ADMIN")  // Somente admin pode criar admin
                .requestMatchers("/update-passwords").hasRole("ADMIN")
                .requestMatchers("/**").hasRole("ADMIN")

                .anyRequest().authenticated()
                .and()
                .httpBasic();

        return http.build();
    }
    /* O metodo cria um bean (@Bean) que fornece uma instância do BCryptPasswordEncoder.
    Esse encoder é utilizado para criptografar e verificar senhas.
    O Bean permite que guarde a logica desse metodo aqui e possa reutiliza-lo no em outros pontos do projeto*/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
