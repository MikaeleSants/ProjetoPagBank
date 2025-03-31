## **1. Enum `UserRole`**
O enum `UserRole` define os tipos de usuários na sua aplicação:
```java
public enum UserRole {
    ADMIN, USER
}
```
- **ADMIN** → tem permissões totais.
- **USER** → tem permissões restritas.

---

## **2. Entidade `User`**

### **Campos principais**
```java
@Email(message = "O e-mail deve ser válido.")
@Column(unique = true)
private String email;
```
- O e-mail deve ser único no banco de dados.

```java
@NotBlank(message = "A senha não pode estar vazia.")
private String password;
```
- A senha é obrigatória, mas ela **precisa ser criptografada antes de ser salva no banco**.

```java
@Enumerated(EnumType.STRING)
private UserRole role;
```
- Define o papel do usuário (ADMIN ou USER) e armazena como string no banco.

---

## **3. Repositório `UserRepository`**
A interface `UserRepository` permite consultar usuários no banco de dados.
```java
Optional<User> findByEmail(String email);
```
- Busca um usuário pelo e-mail (necessário para autenticação).

```java
@Transactional
@Modifying
@Query("UPDATE User u SET u.password = :password")
void updateAllPasswords(@Param("password") String password);
```
- Atualiza a senha de **todos os usuários** de uma vez.

---
// metodo que eu coloquei só para alterar as senhas iniciais sem cripto

## **4. Serviço `UserServices`**

### **Criação de Usuário**
```java
public User insert(User obj) {
    Optional<User> existingUser = userRepository.findByEmail(obj.getEmail());
    if (existingUser.isPresent()) {
        throw new EmailAlreadyExistsException(obj.getEmail());
    }

    // Valida a senha antes de criptografar
    if (obj.getPassword() == null || obj.getPassword().length() < 3 || obj.getPassword().length() > 8 ||
        !obj.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$")) {
        throw new IllegalArgumentException("A senha não atende aos requisitos.");
    }

    // Criptografa a senha
    obj.setPassword(passwordEncoder.encode(obj.getPassword()));

    return userRepository.save(obj);
}
```
- **Valida** se o e-mail já existe.
- **Verifica** se a senha segue as regras (mínimo 3, máximo 8 caracteres, com letra, número e símbolo especial).
- **Criptografa** a senha antes de salvar no banco.

---
pórem tá dando erro essa validação, acredito que mesmo que eu valide antes de salvar, na hora de salvar pede a validação de novo
acho que eu posso deixar esse metodo ai e tirar as anotações, só melhorar as exceções

## **5. Segurança com Spring Security**
### **Classe `SecurityConfig`**
A configuração de segurança define quem pode acessar o quê.

```java
.requestMatchers(HttpMethod.POST, "/users").permitAll()
```
- Qualquer pessoa pode criar um usuário (não precisa estar autenticado).

```java
.requestMatchers(HttpMethod.GET, "/products/**", "/categories/**", "/coupons/**").permitAll()
```
- Todos podem ver os produtos, categorias e cupons.

```java
.requestMatchers(HttpMethod.POST, "/orders").hasRole("USER")
```
- Apenas usuários logados podem criar pedidos.

```java
.requestMatchers(HttpMethod.GET, "/orders/{id}").access("@orderSecurity.checkOrderOwnership(authentication, #id)")
```
- Apenas o dono da `order` pode visualizá-la.

```java
.requestMatchers("/**").hasRole("ADMIN")
```
- Qualquer outra rota só pode ser acessada por **admins**.

---

## **6. Classes `OrderSecurity` e `UserSecurity`**
Essas classes garantem que um usuário só possa acessar os próprios pedidos e dados:

### **Exemplo de verificação de propriedade da `Order`**
```java
public boolean checkOrderOwnership(Authentication authentication, Long orderId) {
    String userEmail = authentication.getName();
    return orderRepository.existsByIdAndClient_Email(orderId, userEmail);
}
```
- Verifica se a `order` pertence ao usuário autenticado antes de permitir acesso.

---

## **Resumo**
- `UserRole` define permissões (ADMIN ou USER).
- `User` armazena informações do usuário e criptografa a senha.
- `UserServices` gerencia usuários, validando e criptografando senhas.
- `SecurityConfig` define regras de acesso para cada endpoint.
- `OrderSecurity` e `UserSecurity` impedem usuários de acessarem dados de terceiros.

acessos para teste:

admin@example.com
Mik@232

paloma.souza@email.com
PA@c123