package com.criando.projeto.resource;


import com.criando.projeto.entities.User;
import com.criando.projeto.entities.enums.UserRole;
import com.criando.projeto.services.UserServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.mockito.MockedStatic;
import org.springframework.web.util.UriComponents;
import org.springframework.security.core.Authentication;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class UserResourcesTest {
    @InjectMocks
    private UserResources userResources;
    @Mock
    private UserServices userServices;
    @Mock
    private UserDetails userDetails;

    private User user;
    private User userDois;


    @BeforeEach
    void setUp() {
        this.user = new User();
        this.user.setId(1L);
        this.user.setName("Maria Bruna");
        this.user.setEmail("MariaBruna@gmail.com");
        this.user.setPassword("Mar@123");

        this.userDois = new User();
        this.userDois.setId(2L);
        this.userDois.setName("Carol Silva");
        this.userDois.setEmail("CarolSilva@gmail.com");
        this.userDois.setPassword("Car@123");
    }

    @Test
    @DisplayName("Deve retornar todos os usuários, com status 200 OK")
    void findAll() {
        List<User> users = List.of(user, userDois);
        when(userServices.findAll()).thenReturn(users);
        ResponseEntity<List<User>> result = userResources.findAll();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(users, result.getBody());

        verify(userServices).findAll();
    }

    @Test
    @DisplayName("Deve retornar um usuário por ID, com status 200 OK")
    void findById() {
        when(userServices.findById(1L)).thenReturn(user);
        ResponseEntity<User> result = userResources.findById(1L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(user, result.getBody());

        verify(userServices).findById(1L);
    }


    @Test
    @DisplayName("Deve inserir usuário com role ADMIN quando usuário autenticado for admin")
    void insertQuandoUsuarioForAdmin() {
        // Arrange
        User usuarioParaInserir = new User(null, "Admin", "admin@email.com", "999999999", "321@xyz", null);
        User usuarioSalvo = new User(2L, "Admin", "admin@email.com", "999999999", "321@xyz", UserRole.ADMIN);
        
        URI fakeUri = URI.create("/users/2");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); // Mudamos para ROLE_ADMIN
        doReturn(authorities).when(userDetails).getAuthorities();

        // Mock estático do ServletUriComponentsBuilder
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = Mockito.mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentRequest).thenReturn(builder);
            when(builder.path("/{id}")).thenReturn(builder);
            when(builder.buildAndExpand(usuarioSalvo.getId())).thenReturn(Mockito.mock(UriComponents.class));
            when(builder.buildAndExpand(usuarioSalvo.getId()).toUri()).thenReturn(fakeUri);

            when(userServices.insert(any(User.class))).thenReturn(usuarioSalvo);

            // Act
            ResponseEntity<User> response = userResources.insert(usuarioParaInserir, userDetails);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(UserRole.ADMIN, response.getBody().getRole());
            assertEquals(usuarioSalvo.getId(), response.getBody().getId());
            assertEquals(fakeUri, response.getHeaders().getLocation());
            verify(userServices).insert(any(User.class));
        }
    }

    @Test
    @DisplayName("Deve inserir usuário com role USER quando não for admin")
    void insertQuandoUsuarioNaoForAdmin() {
        // Arrange
        User usuarioParaInserir = new User(null, "Maria", "maria@email.com", "999999999", "321@xyz", null);
        User usuarioSalvo = new User(2L, "Maria", "maria@email.com", "999999999", "321@xyz", UserRole.USER);
        
        URI fakeUri = URI.create("/users/2");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(userDetails).getAuthorities();

        // Mock estático do ServletUriComponentsBuilder
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = Mockito.mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentRequest).thenReturn(builder);
            when(builder.path("/{id}")).thenReturn(builder);
            when(builder.buildAndExpand(usuarioSalvo.getId())).thenReturn(Mockito.mock(UriComponents.class));
            when(builder.buildAndExpand(usuarioSalvo.getId()).toUri()).thenReturn(fakeUri);

            when(userServices.insert(any(User.class))).thenReturn(usuarioSalvo);

            // Act
            ResponseEntity<User> response = userResources.insert(usuarioParaInserir, userDetails);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(UserRole.USER, response.getBody().getRole());
            assertEquals(usuarioSalvo.getId(), response.getBody().getId());
            assertEquals(fakeUri, response.getHeaders().getLocation());
            verify(userServices).insert(any(User.class));
        }
    }

        @Test
    @DisplayName("Deve deletar uma categoria com sucesso e retornar status 204 No Content")
    void Delete() {
        doNothing().when(userServices).delete(user.getId());
        ResponseEntity<Void> response = userResources.delete(user.getId());
        verify(userServices, times(1)).delete(user.getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve permitir usuário atualizar seus próprios dados")
    void updateUser() {
        Long userId = 2L;
        User usuarioParaAtualizar = new User(null, "João Atualizado", "joao@email.com", "888888888", "Sen@456", null);
        User usuarioAtualizado = new User(userId, "João Atualizado", "joao@email.com", "888888888", "Joa@456", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        lenient().doReturn(authorities).when(userDetails).getAuthorities();
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn(user.getEmail());
        when(userServices.updatePatch(eq(1L), any(User.class), eq(authentication))).thenReturn(usuarioAtualizado);
        ResponseEntity<User> response = userResources.updatePatch(1L, usuarioParaAtualizar, authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(usuarioAtualizado, response.getBody());
        assertEquals("João Atualizado", response.getBody().getName());
        verify(userServices).updatePatch(eq(1L), any(User.class), eq(authentication));
    }

    @Test
    @DisplayName("Deve permitir admin atualizar dados de qualquer usuário")
    void updateAdmin() {
        Long userId = 2L;
        User usuarioParaAtualizar = new User(null, "Carol Atualizada", "carol@email.com", "777777777", "Car@789", null);
        User usuarioAtualizado = new User(userId, "Carol Atualizada", "carol@email.com", "777777777", "Car@789", UserRole.USER);
        // Configurando autenticação como ADMIN
        Authentication authentication = mock(Authentication.class);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        lenient().doReturn(authorities).when(userDetails).getAuthorities();
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn("admin@email.com");
        when(userServices.updatePatch(eq(2L), any(User.class), eq(authentication))).thenReturn(usuarioAtualizado);
        ResponseEntity<User> response = userResources.updatePatch(2L, usuarioParaAtualizar, authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(usuarioAtualizado, response.getBody());
        assertEquals("Carol Atualizada", response.getBody().getName());
        verify(userServices).updatePatch(eq(2L), any(User.class), eq(authentication));
    }
}