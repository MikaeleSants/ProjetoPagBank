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
                /*
                .requestMatchers(HttpMethod.GET, "/orders/{id}")
                .access("@orderSecurity.checkOrderOwnership(authentication, #id)")  // Usuário pode acessar somente os próprios pedidos
                .requestMatchers(HttpMethod.GET, "/orders")
                .access("hasRole('ADMIN') or @orderSecurity.checkOrderOwnership(authentication, #id)")
                .requestMatchers(HttpMethod.POST, "/orders").hasRole("USER")
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}")
                .access("@orderSecurity.checkOrderOwnership(authentication, #id)")  // Usuário pode alterar somente os próprios pedidos
                .requestMatchers(HttpMethod.PUT, "/orders/{id}")
                .access("@orderSecurity.checkOrderOwnership(authentication, #id)")  // Usuário pode alterar somente os próprios pedidos
                    //Permissões para produtos
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}/items")
                .access("@orderSecurity.checkOrderOwnership(authentication, #id)")  // Usuário pode atualizar itens de pedidos somente para os próprios pedidos
                .requestMatchers(HttpMethod.DELETE, "/orders/{orderId}/remove-product/{productId}")
                .access("@orderSecurity.checkOrderOwnership(authentication, #orderId)")
                    //Permissões para cupons
                .requestMatchers(HttpMethod.POST, "/orders/{orderId}/apply-coupon/{couponId}")
                .access("@orderSecurity.checkOrderOwnership(authentication, #orderId)")  // Usuário pode aplicar cupom apenas nos seus próprios pedidos
                .requestMatchers(HttpMethod.DELETE, "/orders/{orderId}/remove-coupon")
                .access("@orderSecurity.checkOrderOwnership(authentication, #orderId)")  // Usuário pode remover cupom apenas nos seus próprios pedidos
                    // Permissões para status de pedido
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}/status")
                .access("@orderSecurity.checkOrderOwnership(authentication, #id) or hasRole('ADMIN')")  // Usuário pode atualizar status de seus próprios pedidos ou administradores podem alterar qualquer status
                    // Permissões para pagamento
                .requestMatchers(HttpMethod.POST, "/orders/{id}/payment")
                .access("@orderSecurity.checkOrderOwnership(authentication, #id) or hasRole('ADMIN')")  // Usuário pode adicionar pagamento aos seus próprios pedidos ou administradores podem adicionar a qualquer pedido
                    // Permissões para gerenciamento do próprio usuário
                .requestMatchers(HttpMethod.PATCH, "/users/{id}")
                .access("@userSecurity.checkUserOwnership(authentication, #id) or hasRole('ADMIN')")
                .requestMatchers(HttpMethod.GET, "/users/{id}").access("@userSecurity.checkUserOwnership(authentication, #id) or hasRole('ADMIN')")
                .requestMatchers(HttpMethod.DELETE, "/users/{id}")
                .access("@userSecurity.checkUserOwnership(authentication, #id) or hasRole('ADMIN')")

                // Permissões para administradores
                .requestMatchers(HttpMethod.POST, "/admin/users").hasRole("ADMIN")  // Somente admin pode criar admin
                .requestMatchers("/update-passwords").hasRole("ADMIN")
                .requestMatchers("/**").hasRole("ADMIN")*/


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
/*
                // **Permissões para Administradores** (Role ADMIN) com CRUD completo

                // **Orders**: Admin pode acessar, criar, editar e excluir
                .requestMatchers(HttpMethod.GET, "/orders").hasRole("ADMIN") // Admin pode ver todos os pedidos
                .requestMatchers(HttpMethod.GET, "/orders/{id}").hasRole("ADMIN") // Admin pode ver um pedido específico
                .requestMatchers(HttpMethod.POST, "/orders").hasRole("ADMIN") // Admin pode criar pedidos
                .requestMatchers(HttpMethod.PUT, "/orders/{id}").hasRole("ADMIN") // Admin pode editar qualquer pedido
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}").hasRole("ADMIN") // Admin pode editar qualquer pedido
                .requestMatchers(HttpMethod.DELETE, "/orders/{id}").hasRole("ADMIN") // Admin pode deletar qualquer pedido
                .requestMatchers(HttpMethod.GET, "/orders/filter").hasRole("ADMIN") // Admin pode aplicar filtros em pedidos
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}/items").hasRole("ADMIN") // Admin pode alterar itens de pedidos
                .requestMatchers(HttpMethod.DELETE, "/orders/{orderId}/remove-product/{productId}").hasRole("ADMIN") // Admin pode remover produtos de pedidos
                .requestMatchers(HttpMethod.POST, "/orders/{orderId}/apply-coupon/{couponId}").hasRole("ADMIN") // Admin pode aplicar cupons em qualquer pedido
                .requestMatchers(HttpMethod.DELETE, "/orders/{orderId}/remove-coupon").hasRole("ADMIN") // Admin pode remover cupons de qualquer pedido
                .requestMatchers(HttpMethod.PATCH, "/orders/{id}/status").hasRole("ADMIN") // Admin pode alterar status de qualquer pedido
                .requestMatchers(HttpMethod.POST, "/orders/{id}/payment").hasRole("ADMIN") // Admin pode registrar pagamento em qualquer pedido

                // **Users**: Admin pode acessar, criar, editar e excluir
                .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN") // Admin pode ver todos os usuários
                .requestMatchers(HttpMethod.GET, "/users/{id}").hasRole("ADMIN") // Admin pode ver um usuário específico
                .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN") // Admin pode criar usuários
                .requestMatchers(HttpMethod.PUT, "/users/{id}").hasRole("ADMIN") // Admin pode editar qualquer usuário
                .requestMatchers(HttpMethod.PATCH, "/users/{id}").hasRole("ADMIN") // Admin pode editar qualquer usuário
                .requestMatchers(HttpMethod.DELETE, "/users/{id}").hasRole("ADMIN") // Admin pode deletar qualquer usuário


                // **Products**: Admin pode acessar, criar, editar e excluir
                .requestMatchers(HttpMethod.GET, "/products").hasRole("ADMIN") // Admin pode ver todos os produtos
                .requestMatchers(HttpMethod.GET, "/products/{id}").hasRole("ADMIN") // Admin pode ver um produto específico
                .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN") // Admin pode criar produtos
                .requestMatchers(HttpMethod.PUT, "/products/{id}").hasRole("ADMIN") // Admin pode editar qualquer produto
                .requestMatchers(HttpMethod.PATCH, "/products/{id}").hasRole("ADMIN") // Admin pode editar qualquer produto
                .requestMatchers(HttpMethod.DELETE, "/products/{id}").hasRole("ADMIN") // Admin pode deletar qualquer produto

                // **Coupons**: Admin pode acessar, criar, editar e excluir
                .requestMatchers(HttpMethod.GET, "/cupons").hasRole("ADMIN") // Admin pode ver todos os cupons
                .requestMatchers(HttpMethod.GET, "/cupons/{id}").hasRole("ADMIN") // Admin pode ver um cupom específico
                .requestMatchers(HttpMethod.POST, "/cupons").hasRole("ADMIN") // Admin pode criar cupons
                .requestMatchers(HttpMethod.PUT, "/cupons/{id}").hasRole("ADMIN") // Admin pode editar qualquer cupom
                .requestMatchers(HttpMethod.PATCH, "/cupons/{id}").hasRole("ADMIN") // Admin pode editar qualquer cupom
                .requestMatchers(HttpMethod.DELETE, "/cupons/{id}").hasRole("ADMIN") // Admin pode deletar qualquer cupom


                // **Categories**: Admin pode acessar, criar, editar e excluir
                .requestMatchers(HttpMethod.GET, "/categories").hasRole("ADMIN") // Admin pode ver todas as categorias
                .requestMatchers(HttpMethod.GET, "/categories/{id}").hasRole("ADMIN") // Admin pode ver uma categoria específica
                .requestMatchers(HttpMethod.POST, "/categories").hasRole("ADMIN") // Admin pode criar categorias
                .requestMatchers(HttpMethod.PUT, "/categories/{id}").hasRole("ADMIN") // Admin pode editar qualquer categoria
                .requestMatchers(HttpMethod.PATCH, "/categories/{id}").hasRole("ADMIN") // Admin pode editar qualquer categoria
                .requestMatchers(HttpMethod.DELETE, "/categories/{id}").hasRole("ADMIN") // Admin pode deletar qualquer categoria
*/
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
