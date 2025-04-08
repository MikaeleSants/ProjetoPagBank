package com.criando.projeto.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private OrderSecurity orderSecurity;
    @Autowired
    private UserSecurity userSecurity;
    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        // Documentação
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // ORDERS
                        .requestMatchers("/orders/**").authenticated()

                        // USERS
                        .requestMatchers("/users/**").authenticated()

                        // PRODUCTS
                        .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/{id}").hasRole("ADMIN")

                        // COUPONS
                        .requestMatchers(HttpMethod.POST, "/cupons").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/cupons/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/cupons/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/cupons/{id}").hasRole("ADMIN")

                        // CATEGORIES
                        .requestMatchers(HttpMethod.POST, "/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/{id}").hasRole("ADMIN")

                        // PÚBLICO
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**", "/cupons/**").permitAll()

                        // QUALQUER OUTRA COISA
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}) // configs padrão
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
