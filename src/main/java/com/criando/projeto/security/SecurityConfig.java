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
                .csrf().disable()
                .authorizeRequests()



                //ACESSOS A ORDERS
                .requestMatchers(HttpMethod.GET, "/orders/{id}")
                .access("@orderSecurity.checkOrderOwnership(authentication, #id)")
                .requestMatchers(HttpMethod.GET, "/orders")
                .access("hasRole('ADMIN') or isAuthenticated()")// Usuário comum pode acessar somente seus próprios pedidos, lógica tá no service
                .requestMatchers(HttpMethod.POST, "/orders")
                .hasAnyRole("USER", "ADMIN") // Ambos podem criar pedidos
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}")
                .access("hasRole('ADMIN') or @orderSecurity.checkOrderOwnership(authentication, #id)") // Admin pode tudo, usuário comum só nos próprios pedidos
                .requestMatchers(HttpMethod.PUT, "/orders/{id}")
                .access("hasRole('ADMIN') or @orderSecurity.checkOrderOwnership(authentication, #id)") // Admin pode tudo, usuário comum só nos próprios pedidos
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}/items")
                .access("hasRole('ADMIN') or @orderSecurity.checkOrderOwnership(authentication, #id)") // Admin pode tudo, usuário comum só nos próprios pedidos
                .requestMatchers(HttpMethod.DELETE, "/orders/{orderId}/remove-product/{productId}")
                .access("hasRole('ADMIN') or @orderSecurity.checkOrderOwnership(authentication, #orderId)") // Admin pode tudo, usuário comum só nos próprios pedidos
                .requestMatchers(HttpMethod.POST, "/orders/{orderId}/apply-coupon/{couponId}")
                .access("hasRole('ADMIN') or @orderSecurity.checkOrderOwnership(authentication, #orderId)") // Admin pode tudo, usuário comum só nos próprios pedidos
                .requestMatchers(HttpMethod.DELETE, "/orders/{orderId}/remove-coupon")
                .access("hasRole('ADMIN') or @orderSecurity.checkOrderOwnership(authentication, #orderId)") // Admin pode tudo, usuário comum só nos próprios pedidos
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}/status")
                .access("hasRole('ADMIN') or @orderSecurity.checkOrderOwnership(authentication, #id)") // Admin pode tudo, usuário comum só nos próprios pedidos
                .requestMatchers(HttpMethod.POST, "/orders/{id}/payment")
                .access("hasRole('ADMIN') or @orderSecurity.checkOrderOwnership(authentication, #id)") // Admin pode tudo, usuário comum só nos próprios pedidos
                // Só ADMIN pode deletar orders
                .requestMatchers(HttpMethod.DELETE, "/orders/{id}").hasRole("ADMIN")


                //ACESSOS A USER
                .requestMatchers(HttpMethod.PATCH, "/users/{id}")
                .access("hasRole('ADMIN') or @userSecurity.checkUserOwnership(authentication, #id)") // Admin pode tudo, usuário comum apenas seus próprios dados
                .requestMatchers(HttpMethod.GET, "/users/{id}")
                .access("hasRole('ADMIN') or @userSecurity.checkUserOwnership(authentication, #id)") // Admin pode tudo, usuário comum apenas seus próprios dados
                .requestMatchers(HttpMethod.GET, "/users")
                .access("hasRole('ADMIN') or isAuthenticated()") // Admin vê todos, usuário comum só os próprios dados (filtrado no Service)
                .requestMatchers(HttpMethod.PUT, "/users/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/{id}")
                .access("hasRole('ADMIN') or @userSecurity.checkUserOwnership(authentication, #id)") // Usuário pode deletar apenas a si mesmo, Admin pode deletar qualquer um

                //ACESSO A PRODUTOS:
                .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN") // Admin pode criar produtos
                .requestMatchers(HttpMethod.PUT, "/products/{id}").hasRole("ADMIN") // Admin pode editar qualquer produto
                .requestMatchers(HttpMethod.PATCH, "/products/{id}").hasRole("ADMIN") // Admin pode editar qualquer produto
                .requestMatchers(HttpMethod.DELETE, "/products/{id}").hasRole("ADMIN") // Admin pode deletar qualquer produto

                //ACESSO A COUPONS:
                .requestMatchers(HttpMethod.POST, "/cupons").hasRole("ADMIN") // Admin pode criar cupons
                .requestMatchers(HttpMethod.PUT, "/cupons/{id}").hasRole("ADMIN") // Admin pode editar qualquer cupom
                .requestMatchers(HttpMethod.PATCH, "/cupons/{id}").hasRole("ADMIN") // Admin pode editar qualquer cupom
                .requestMatchers(HttpMethod.DELETE, "/cupons/{id}").hasRole("ADMIN") // Admin pode deletar qualquer cupom

                //ACESSO A CATEGORIES:
                .requestMatchers(HttpMethod.POST, "/categories").hasRole("ADMIN") // Admin pode criar categorias
                .requestMatchers(HttpMethod.PUT, "/categories/{id}").hasRole("ADMIN") // Admin pode editar qualquer categoria
                .requestMatchers(HttpMethod.PATCH, "/categories/{id}").hasRole("ADMIN") // Admin pode editar qualquer categoria
                .requestMatchers(HttpMethod.DELETE, "/categories/{id}").hasRole("ADMIN") // Admin pode deletar qualquer categoria


                // Permissões para qualquer pessoa (SEM AUTENTICAÇÃO)
                .requestMatchers(HttpMethod.POST, "/users").permitAll() // Permitir qualquer pessoa criar um usuário COMUM
                .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**", "/cupons/**").permitAll()  // Acesso público

                .anyRequest().authenticated()
                .and()
                .httpBasic();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
