package com.criando.projeto.services;

import com.criando.projeto.entities.User;
import com.criando.projeto.entities.enums.UserRole;
import com.criando.projeto.repositories.UserRepository;
import com.criando.projeto.security.UserSecurity;
import com.criando.projeto.services.exceptions.*;
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

    @Test
    @DisplayName("Deve retornar todos os usuários quando o usuário for ADMIN")
    void findAll_Admin() {
        User user1 = new User(1L, "User 1", "user1@example.com", "123456789", "Sen@1234", UserRole.USER);
        User user2 = new User(2L, "User 2", "user2@example.com", "987654321", "Sen@1234", UserRole.ADMIN);
        // Configurando o mock de autenticação como ADMIN
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Configura o repositório
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        // chamando aqui o metodo de fato
        List<User> result = userServices.findAll();
        // validando
        assertThat(result).hasSize(2);
        assertThat(result).contains(user1, user2);
        // Verifica se a autenticação foi verificada, só pra garantir
        verify(authenticationFacade).isUser(authentication);
    }

    @Test
    @DisplayName("Deve retornar o próprio usuário quando o usuário for USER")
    void findAll_User() {
        User user = new User(1L, "User 1", "user1@example.com", "123456789", "Sen@1234", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.isUser(authentication)).thenReturn(true);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        List<User> result = userServices.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(user);
        verify(userRepository).findById(user.getId());
    }

    @Test
    @DisplayName("Deve permitir que ADMIN busque qualquer usuário pelo ID")
    void findById_Admin() {
        Long userIdToFind = 2L;
        User adminUser = new User(1L, "Admin User", "admin@example.com", "123456789", "Admin@123", UserRole.ADMIN);
        User userToFind = new User(userIdToFind, "Regular User", "user@example.com", "987654321", "User@123", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(adminUser);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        when(userRepository.findById(userIdToFind)).thenReturn(Optional.of(userToFind));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User result = userServices.findById(userIdToFind);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userIdToFind);
        assertThat(result.getName()).isEqualTo("Regular User");
        verify(userRepository).findById(userIdToFind);
        verify(userSecurity, never()).checkUserOwnership(any(), any());
        //É como dizer:
        //"Além de retornar o usuário correto, garanta que não houve verificação desnecessária de permissão" pq admin nao precisa checar ownership.
    }


    @Test
    @DisplayName("Deve lançar exceção se usuário não for encontrado por ID")
    void findByIdException() {
        User adminUser = new User(1L, "Admin User", "admin@example.com", "123456789", "Admin@123", UserRole.ADMIN);
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(adminUser);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThatThrownBy(() -> userServices.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Busca incompleta! id: 99 não encontrado!");
        verify(userRepository).findById(99L);
    }

    @Test
    @DisplayName("Deve inserir um novo usuário com sucesso")
    void insert() {
        User user = new User(null, "User 1", "user1@example.com", "123456789", "Sen@1234", UserRole.USER);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userServices.insert(user);

        assertThat(result.getEmail()).isEqualTo("user1@example.com");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Deve lançar exceção se o email já estiver em uso")
    void insertEmailAlreadyExistsException() {
        User user = new User(1L, "User 1", "user1@example.com", "123456789", "Sen@1234", UserRole.USER);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
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
        Long userId = 1L;
        User user = new User(userId, "User 1", "user1@example.com", "123456789", "Sen@1234", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(userSecurity.checkUserOwnership(authentication, user.getId())).thenReturn(true);
        //O deleteById() normalmente é void (não retorna nada). Para configurar comportamentos em métodos void, usamos:
        doNothing().when(userRepository).deleteById(user.getId());
        //Ao invés do when().thenReturn() usado em métodos que retornam valores.
        SecurityContextHolder.getContext().setAuthentication(authentication);
        userServices.delete(user.getId());
        verify(userRepository).deleteById(user.getId());
    }

    @Test
    @DisplayName("Deve permitir que ADMIN delete outro usuário")
    void delete_AdminCanDeleteOtherUser() {
        User adminUser = new User(1L, "Admin User", "admin@example.com", "123456789", "Admin@123", UserRole.ADMIN);
        User userToDelete = new User(2L, "Regular User", "user@example.com", "987654321", "User@123", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(adminUser);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        // Configura o security para permitir a deleção (admin pode deletar qualquer usuário), essa parte tipo simula o comportamento da minha security config pra liberar os admins
        when(userSecurity.checkUserOwnership(authentication, userToDelete.getId())).thenReturn(true);
        doNothing().when(userRepository).deleteById(userToDelete.getId());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        userServices.delete(userToDelete.getId());
        verify(userRepository).deleteById(userToDelete.getId());
        verify(userSecurity).checkUserOwnership(authentication, userToDelete.getId());
        assertThat(adminUser.getRole()).isEqualTo(UserRole.ADMIN);
    }


    @Test
    @DisplayName("Deve lançar exceção se não tiver permissão para deletar usuário")
    void deleteAccessDeniedException() {
        Long userId = 1L;
        User user = new User(userId, "User 1", "user1@example.com", "123456789", "Sen@1234", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(user);
        when(authenticationFacade.isUser(authentication)).thenReturn(true);
        // Simula permissão negada
        when(userSecurity.checkUserOwnership(authentication, userId)).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThatThrownBy(() -> userServices.delete(user.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você não tem permissão para excluir este usuário.");
        verify(userSecurity).checkUserOwnership(authentication, userId);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando USER tentar excluir um usuário inexistente")
    void deleteResourceNotFoundException() {
        Long userId = 100L;
        User authenticatedUser = new User(1L, "User", "user@email.com", "123456789", "password", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(authenticationFacade.isUser(authentication)).thenReturn(true);
        when(userSecurity.checkUserOwnership(authentication, userId)).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Simula que o usuário não existe (causa EmptyResultDataAccessException)
        doThrow(new EmptyResultDataAccessException(1)).when(userRepository).deleteById(userId);
        assertThatThrownBy(() -> userServices.delete(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Busca incompleta! id: 100 não encontrado!");
        verify(userSecurity).checkUserOwnership(authentication, userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando ADMIN tentar deletar usuário inexistente")
    void delete_AdminTryingToDeleteNonExistentUser() {
        Long nonExistentUserId = 999L;
        User adminUser = new User(1L, "Admin User", "admin@example.com", "123456789", "Admin@123", UserRole.ADMIN);
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(adminUser);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        when(userSecurity.checkUserOwnership(authentication, nonExistentUserId)).thenReturn(true);
        doThrow(new EmptyResultDataAccessException(1)).when(userRepository).deleteById(nonExistentUserId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThrows(ResourceNotFoundException.class, () -> {
            userServices.delete(nonExistentUserId);
        });
        verify(userRepository).deleteById(nonExistentUserId);
    }


    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir usuário com dependências")
    void deleteDatabaseException() {
        Long userId = 100L;
        User user = new User(userId, "Usuário com dependências", "user@example.com", "123456789", "senha", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Simula permissões (admin ou dono do recurso)
        when(userSecurity.checkUserOwnership(authentication, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Simula erro de integridade
        doThrow(new DataIntegrityViolationException("")).when(userRepository).deleteById(userId);
        // Executa e verifica
        assertThatThrownBy(() -> userServices.delete(userId))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Não é possível excluir este usuário, pois há referências a ele.");
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando ADMIN tenta excluir usuário com dependências")
    void deleteDatabaseException_Admin() {
        Long userId = 100L;
        User adminUser = new User(1L, "Admin", "admin@example.com", "123456789", "Admin@123", UserRole.ADMIN);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(adminUser);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        when(userSecurity.checkUserOwnership(authentication, userId)).thenReturn(true);
        // Simula erro de integridade referencial
        doThrow(new DataIntegrityViolationException("Constraint violation"))
                .when(userRepository).deleteById(userId);
        assertThatThrownBy(() -> userServices.delete(userId))
                .isInstanceOf(DatabaseException.class)
                .hasMessage("Não é possível excluir este usuário, pois há referências a ele.");
        verify(userRepository).deleteById(userId);
    }


    @Test
    @DisplayName("Deve atualizar um usuário com sucesso")
    void update() {
        Long id = 1L;
        User existingUser = new User(id, "User 1", "user1@example.com", "123456789", "oldPass", UserRole.USER);
        User updatedData = new User(id, "User 2", "user2@example.com", "987654321", "new@123", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(existingUser);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(false);
        when(passwordEncoder.encode(updatedData.getPassword())).thenReturn("newEncodedPass");
        User result = userServices.updatePatch(id, updatedData, authentication);
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("User 2");
        assertThat(result.getEmail()).isEqualTo("user2@example.com");
        assertThat(result.getPhone()).isEqualTo("987654321");
        assertThat(result.getPassword()).isEqualTo("newEncodedPass");
        verify(userRepository).findById(id);
        verify(userRepository).save(existingUser);
        verify(passwordEncoder).encode(updatedData.getPassword());
        verify(authenticationFacade).getAuthenticatedUser();
    }

    @Test
    @DisplayName("Deve permitir que ADMIN atualize outro usuário com sucesso")
    void update_AdminCanUpdateOtherUser() {
        Long userIdToUpdate = 2L;
        User adminUser = new User(1L, "Admin User", "admin@example.com", "123456789", "Adm@123", UserRole.ADMIN);
        User existingUser = new User(userIdToUpdate, "Existing User", "existing@example.com", "987654321", "Old@123", UserRole.USER);
        User updatedUserData = new User(userIdToUpdate, "Updated User", "updated@example.com", "987654321", "New@123", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(userRepository.findById(userIdToUpdate)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(updatedUserData.getPassword())).thenReturn("EncodedNewPass@123");
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(adminUser);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        User result = userServices.updatePatch(userIdToUpdate, updatedUserData, authentication);
        assertThat(result.getId()).isEqualTo(userIdToUpdate);
        assertThat(result.getName()).isEqualTo("Updated User");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getPassword()).isEqualTo("EncodedNewPass@123");
        verify(userRepository).findById(userIdToUpdate);
        verify(userRepository).save(existingUser);
        verify(authenticationFacade).isAdmin(authentication);
        verify(authenticationFacade).getAuthenticatedUser();
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar outro usuário sem permissão")
    void updateAccessDeniedException() {
        Long id = 1L;
        User authenticatedUser = new User(id, "Old User", "user@example.com", "123456789", "password", UserRole.USER);
        Long targetId = 2L;
        User userToUpdate = new User(targetId, "Another User", "another@example.com", "987654321", "password", UserRole.USER);
        User updatedUser = new User(targetId, "Updated User", "another@example.com", "987654321", "password", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(false);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(userToUpdate));
        assertThatThrownBy(() -> userServices.updatePatch(targetId, updatedUser, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você só pode atualizar os seus próprios dados.");
        verify(authenticationFacade).isAdmin(authentication);
        verify(authenticationFacade).getAuthenticatedUser();
        verify(userRepository).findById(targetId);
    }


    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar usuário inexistente")
    void updateResourceNotFoundException() {
        Long id = 99L;
        User updatedUser = new User(id, "User 1", "user1@example.com", "123456789", "password", UserRole.USER);
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userServices.updatePatch(id, updatedUser, mock(Authentication.class)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ADMIN tenta atualizar usuário inexistente")
    void updateResourceNotFoundException_Admin() {
        Long nonExistentId = 99L;
        User adminUser = new User(1L, "Admin", "admin@example.com", "123456789", "Admin@123", UserRole.ADMIN);
        User updatedUser = new User(nonExistentId, "Non-existent User", "nonexistent@example.com", "987654321", "Pass@123", UserRole.USER);
        Authentication authentication = mock(Authentication.class);
        when(authenticationFacade.getAuthenticatedUser()).thenReturn(adminUser);
        when(authenticationFacade.isAdmin(authentication)).thenReturn(true);
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userServices.updatePatch(nonExistentId, updatedUser, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(nonExistentId.toString());
        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any());
    }
}
