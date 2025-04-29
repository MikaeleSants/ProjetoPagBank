package com.criando.projeto.services;

import com.criando.projeto.entities.User;
import com.criando.projeto.entities.enums.UserRole;
import com.criando.projeto.repositories.UserRepository;
import com.criando.projeto.security.UserSecurity;
import com.criando.projeto.services.exceptions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServicesTest {

    @InjectMocks
    private UserServices userServices;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private UserSecurity userSecurity;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    private static final Long NONEXISTENT_ID = 99L;

    private User user;
    private User userDois;


    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        user = new User();
        user.setId(1L);
        user.setName("User Um");
        user.setEmail("UserUm@gmail.com");
        user.setPassword("UsUm@123");
        user.setRole(UserRole.USER);

        userDois = new User();
        userDois.setId(2L);
        userDois.setName("User Dois");
        userDois.setEmail("UserDois@gmail.com");
        userDois.setPassword("UsDo@123");
        userDois.setRole(UserRole.ADMIN);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAsAdmin() {
        Authentication authentication = mock(Authentication.class);
        lenient().when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        lenient().when(userSecurity.checkUserOwnership(authentication, user.getId())).thenReturn(true);
    }

    private void setAsUser() {
        Authentication authentication = mock(Authentication.class);
        lenient().when(authenticationFacade.isUser(authentication)).thenReturn(true);
        lenient().when(authenticationFacade.getAuthenticatedUser()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setAsDonoDoUsuario() {
        Authentication authentication = mock(Authentication.class);
        lenient().when(userSecurity.checkUserOwnership(authentication, user.getId())).thenReturn(true);
        lenient().when(authenticationFacade.getAuthenticatedUser()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setAsDonoDoOutroUsuario() {
        Authentication authentication = mock(Authentication.class);
        lenient().when(authenticationFacade.getAuthenticatedUser()).thenReturn(user);
        lenient().when(authenticationFacade.isUser(authentication)).thenReturn(true);
        lenient().when(userSecurity.checkUserOwnership(authentication, user.getId())).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    @Test
    @DisplayName("Deve retornar todos os usuários quando o usuário for ADMIN")
    void findAll_Admin() {
        setAsAdmin();
        lenient().when(userRepository.findAll()).thenReturn(List.of(user, userDois));
        List<User> result = userServices.findAll();
        assertThat(result).hasSize(2);
        assertThat(result).contains(user, userDois);
        verify(userRepository).findAll();
        verify(authenticationFacade).isUser(any());
    }

    @Test
    @DisplayName("Deve retornar o próprio usuário quando o usuário for USER")
    void findAll_User() {
        setAsUser();
        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        List<User> result = userServices.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(user);
        verify(userRepository).findById(user.getId());
    }

    @Test
    @DisplayName("Deve permitir que ADMIN busque qualquer usuário pelo ID")
    void findById_Admin() {
        setAsAdmin();
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = userServices.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo(user.getName());
        verify(userRepository).findById(1L);
        verify(userSecurity, never()).checkUserOwnership(any(), any());
    }


    @Test
    @DisplayName("Deve lançar exceção se usuário não for encontrado por ID")
    void findByIdException() {
        setAsAdmin();
        lenient().when(userRepository.findById(NONEXISTENT_ID)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userServices.findById(NONEXISTENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Busca incompleta! id: " + NONEXISTENT_ID + " não encontrado!");
                
        verify(userRepository).findById(NONEXISTENT_ID);
    }

    @Test
    @DisplayName("Deve inserir um novo usuário com sucesso")
    void insert() {
        lenient().when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        lenient().when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userServices.insert(user);

        assertThat(result.getEmail()).isEqualTo("UserUm@gmail.com");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Deve lançar exceção se o email já estiver em uso")
    void insertEmailAlreadyExistsException() {
       lenient().when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> userServices.insert(user))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("O e-mail informado já está em uso.");
    }

    @Test
    @DisplayName("Deve lançar exceção se a senha for inválida no insert (comprimento)")
    void insertInvalidPasswordLengthException() {
        User user = new User(1L, "User 1", "user1@example.com", "888888888", "Sen@12345", UserRole.USER); // Senha com comprimento inválido
        assertThatThrownBy(() -> userServices.insert(user))
                .isInstanceOf(InvalidPasswordLengthException.class)
                .hasMessageContaining("A senha deve ter entre 3 e 8 caracteres.");
    }

    @Test
    @DisplayName("Deve lançar exceção se a senha não corresponder ao padrão")
    void insertInvalidPasswordPatternException() {
        User user = new User(1L, "User 1", "user1@example.com", "123456", "123456", UserRole.USER); // Senha sem caracteres especiais
        assertThatThrownBy(() -> userServices.insert(user))
                .isInstanceOf(InvalidPasswordPatternException.class)
                .hasMessageContaining("A senha deve conter pelo menos uma letra, um número e um caractere especial.");
    }

    @Test
    @DisplayName("Deve deletar o própio usuário com sucesso")
    void delete() {
        setAsDonoDoUsuario();
        doNothing().when(userRepository).deleteById(user.getId());
        userServices.delete(user.getId());
        verify(userRepository).deleteById(user.getId());
    }

    @Test
    @DisplayName("Deve permitir que ADMIN delete outro usuário")
    void delete_Admin() {
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Configura o usuário autenticado como ADMIN
        lenient().when(authenticationFacade.getAuthenticatedUser()).thenReturn(userDois);
        lenient().when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        lenient().when(userSecurity.checkUserOwnership(any(), any())).thenReturn(true);
        
        // Configura o comportamento do repository
        doNothing().when(userRepository).deleteById(user.getId());
        
        // Executa a operação
        userServices.delete(user.getId());
        
        // Verifica se o método foi chamado
        verify(userRepository).deleteById(user.getId());
    }


    @Test
    @DisplayName("Deve lançar exceção se não tiver permissão para deletar usuário")
    void deleteAccessDeniedException() {
        setAsDonoDoOutroUsuario();

        assertThatThrownBy(() -> userServices.delete(user.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você não tem permissão para excluir este usuário.");
            
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando USER tentar excluir um usuário inexistente")
    void deleteResourceNotFoundExceptionUser() {
    setAsUser();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    lenient().when(userSecurity.checkUserOwnership(authentication, NONEXISTENT_ID)).thenReturn(true);

    doThrow(new EmptyResultDataAccessException(1)).when(userRepository).deleteById(NONEXISTENT_ID);

    assertThatThrownBy(() -> userServices.delete(NONEXISTENT_ID))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Busca incompleta! id: " + NONEXISTENT_ID + " não encontrado!");
        
    verify(userRepository).deleteById(NONEXISTENT_ID);
}

@Test
@DisplayName("Deve lançar exceção quando ADMIN tentar deletar usuário inexistente")
void deleteResourceNotFoundExceptionAdmin() {
    setAsAdmin();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    lenient().when(userSecurity.checkUserOwnership(authentication, NONEXISTENT_ID)).thenReturn(true);

    doThrow(new EmptyResultDataAccessException(1))
        .when(userRepository)
        .deleteById(NONEXISTENT_ID);

    assertThatThrownBy(() -> userServices.delete(NONEXISTENT_ID))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Busca incompleta! id: " + NONEXISTENT_ID + " não encontrado!");

    verify(userRepository).deleteById(NONEXISTENT_ID);
}


    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir usuário com dependências")
    void deleteDatabaseExceptionUser() {
        setAsDonoDoUsuario();

        doThrow(new DataIntegrityViolationException(""))
                .when(userRepository).deleteById(user.getId());

        assertThatThrownBy(() -> userServices.delete(user.getId()))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Não é possível excluir este usuário, pois há referências a ele.");
                
        verify(userRepository).deleteById(user.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ADMIN tenta excluir usuário com dependências")
    void deleteDatabaseExceptionAdmin() {
        setAsAdmin();

        doThrow(new DataIntegrityViolationException("Constraint violation"))
                .when(userRepository).deleteById(user.getId());

        assertThatThrownBy(() -> userServices.delete(user.getId()))
                .isInstanceOf(DatabaseException.class)
                .hasMessage("Não é possível excluir este usuário, pois há referências a ele.");
            
        verify(userRepository).deleteById(user.getId());
    }


    @Test
    @DisplayName("Deve atualizar um usuário com sucesso")
    void updateUser() {
        Long id = 1L;
        User updatedData = new User(id, "User 2", "user2@example.com", "987654321", "new@123", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        setAsUser();
        
        lenient().when(userRepository.findById(id)).thenReturn(Optional.of(user));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(passwordEncoder.encode(updatedData.getPassword())).thenReturn("newEncodedPass");
        
        User result = userServices.updatePatch(id, updatedData, authentication);
        
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("User 2");
        assertThat(result.getEmail()).isEqualTo("user2@example.com");
        assertThat(result.getPhone()).isEqualTo("987654321");
        assertThat(result.getPassword()).isEqualTo("newEncodedPass");
        
        verify(userRepository).findById(id);
        verify(userRepository).save(user);
        verify(passwordEncoder).encode(updatedData.getPassword());
    }

    @Test
    @DisplayName("Deve permitir que ADMIN atualize outro usuário com sucesso")
    void updateAdmin() {
        Long userIdToUpdate = 2L;
        User existingUser = new User(userIdToUpdate, "Existing User", "existing@example.com", "987654321", "Old@123", UserRole.USER);
        User updatedUserData = new User(userIdToUpdate, "Updated User", "updated@example.com", "987654321", "New@123", UserRole.USER);
        Authentication authentication = mock(Authentication.class);

        // Configura o ambiente como ADMIN
        setAsAdmin();
        
        // Configura o usuário autenticado (admin)
        lenient().when(authenticationFacade.getAuthenticatedUser()).thenReturn(userDois); // userDois é o admin definido no setUp()
        lenient().when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        
        // Configura os comportamentos dos repositórios
        lenient().when(userRepository.findById(userIdToUpdate)).thenReturn(Optional.of(existingUser));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(passwordEncoder.encode(updatedUserData.getPassword())).thenReturn("EncodedNewPass@123");
        
        User result = userServices.updatePatch(userIdToUpdate, updatedUserData, authentication);
        
        // Verificações
        assertThat(result.getId()).isEqualTo(userIdToUpdate);
        assertThat(result.getName()).isEqualTo("Updated User");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getPassword()).isEqualTo("EncodedNewPass@123");
        
        verify(userRepository).findById(userIdToUpdate);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar outro usuário sem permissão")
    void updateAccessDeniedException() {
        Long targetId = 2L;
        User userToUpdate = new User(targetId, "Another User", "another@example.com", "987654321", "password", UserRole.USER);
        User updatedUser = new User(targetId, "Updated User", "another@example.com", "987654321", "password", UserRole.USER);
        Authentication authentication = mock(Authentication.class);

        setAsUser();

        lenient().when(userRepository.findById(targetId)).thenReturn(Optional.of(userToUpdate));

        assertThatThrownBy(() -> userServices.updatePatch(targetId, updatedUser, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você só pode atualizar os seus próprios dados.");

        verify(userRepository).findById(targetId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar usuário inexistente")
    void updateResourceNotFoundExceptionUser() {
        User updatedUser = new User(NONEXISTENT_ID, "User 1", "user1@example.com", "123456789", "password", UserRole.USER);
        Authentication authentication = mock(Authentication.class);

        setAsUser();
        lenient().when(userRepository.findById(NONEXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userServices.updatePatch(NONEXISTENT_ID, updatedUser, authentication))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository).findById(NONEXISTENT_ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ADMIN tenta atualizar usuário inexistente")
    void updateResourceNotFoundExceptionAdmin() {
        User updatedUser = new User(NONEXISTENT_ID, "Non-existent User", "nonexistent@example.com", "987654321", "Pass@123", UserRole.USER);
        Authentication authentication = mock(Authentication.class);

        setAsAdmin();
        lenient().when(userRepository.findById(NONEXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userServices.updatePatch(NONEXISTENT_ID, updatedUser, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(NONEXISTENT_ID.toString());

        verify(userRepository).findById(NONEXISTENT_ID);
        verify(userRepository, never()).save(any());
    }
}