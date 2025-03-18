# Documentação da API de Pedidos

## **Objetivo e Contexto**
Esta API tem como objetivo gerenciar pedidos, produtos e usuários de um sistema de vendas online.

## **Arquitetura**
- **Tipo de API**: REST
- **Comunicação**: Síncrona (requisição-resposta)
- **Autenticação**: JWT (JSON Web Token) via Spring Security (? ainda n tá decidido)
- **Controle de versão**: Git e GitHub

## **Modelagem de Dados**
- **Entidades Principais**:
    - `User`
    - `Product`
    - `Order`
    - `OrderItem`
    - `Payment`
    - `Category`

### **Exemplo de Estrutura JSON**
```json
{
  "id": 1,
  "name": "Notebook",
  "price": 2500.00,
  "category": {
    "id": 3,
    "name": "Eletrônicos"
  }
}
```

## **Endpoints e Operações**
### **Rotas Principais**
A API possui CRUD básico para USERS e PRODUCTS, além de GET para as demais entidades.
- `GET /products` → Lista todos os produtos
- `POST /products` → Cadastra um novo produto
- `GET /orders/{id}` → Busca um pedido pelo ID
- `PUT /users/{id}` → Atualiza um usuário
- `DELETE /categories/{id}` → Remove uma categoria

### **Exemplo de Request e Response**
#### **Request (POST /products)**
```json
{
  "name": "Smartphone",
  "price": 1500.00,
  "categoryId": 2
}
```

#### **Response**
```json
{
  "id": 5,
  "name": "Smartphone",
  "price": 1500.00,
  "category": {
    "id": 2,
    "name": "Eletrônicos"
  }
}
```

## **Fluxos Principais**
### **Fluxo de Cadastro de Produto**
1. O usuário chama `POST /products` com os dados do produto
2. A API valida os dados seguindo critérios: e chama `ProductService`
3. O `ProductService` salva no banco via `ProductRepository`
4. Retorna o produto criado ao usuário

### **Fluxo de Realização de Pedido**
1. O usuário chama `POST /orders` com os itens do carrinho
2. A API valida estoque e calcula o total
3. Salva o pedido
4. Retorna os dados do pedido ao cliente

### **Validações de Entrada**
Para garantir a integridade dos dados, foram aplicadas as seguintes validações:

### **Validações Gerais:**
- Uso de `@NotNull`, `@NotBlank` e `@Size` para campos obrigatórios e tamanhos mínimos/máximos
- `@Positive` e `@PositiveOrZero` para garantir valores positivos em preços e quantidades
- `@Email` para validação de formato de e-mails
- Expressões regulares em alguns campos específicos (exemplo: senha com requisitos de segurança)

### **Validações Específicas por Entidade:**
- **User**:
    - Nome e e-mail obrigatórios; 
    - Ao email também foi adicionada anotação para impedir duplicatas.
    - Senha com requisitos mínimos de segurança
- **Product**:
    - Nome obrigatório e com limite de caracteres
    - Preço deve ser positivo

## **Filtros:**
A criação de filtros foi feita usando as Especifications do Spring, a API de critérios do JPA.

### Classe Produto:
  * Filtro por Nome: Permite a busca de produtos pelo nome.
  * Filtro por Categoria: Permite a filtragem de produtos com base na categoria.
  * Filtro por Valor: Permite a busca de produtos dentro de uma faixa de valor específica, pelo valor min ou max.

### Classe Order:
  * Filtro por Status do Pedido: Permite a filtragem de pedidos com base no status (os status estão definidos em uma enum e 
  o filtro permite buscar pelo nome da etapa)

## **Banco de Dados e Persistência**
- **Banco de Dados**: PostgreSQL (perfil: DEV) e H2 (perfil: test)
- **Conexões**: Via Spring Data JPA
- **Queries**: Uso de Criteria API e JPQL para consultas dinâmicas (em construção)

## **Criação de perfis**
  * TESTE: Perfil de teste associado ao banco H2, acessado via navegador. 
    Utiliza a classe TestConfig para inserção de dados no banco e o Postman para testar requisições.

  * DESENVOLVIMENTO: Associado ao PostgreSQL, utilizando o PgAdmin. A inserção de dados de usuários e produtos é feita através do Postman, 
  enquanto as demais tabelas são povoadas via PgAdmin.

## **Ferramentas e Tecnologias**
- **Framework**: Spring Boot
- **Banco de dados**: PostgreSQL e H2
- **Autenticação**: Spring Security com JWT
- **Containerização**: Docker
- **Testes de API:**: Postman
- **Teste Unitário:**: Ainda preciso decidir
- **Controle de versão**: Git e GitHub
- **Desenho da solução**: Draw.io