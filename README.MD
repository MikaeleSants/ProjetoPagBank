# Documentação da API de Pedidos

## **Objetivo e Contexto**
Esta é uma API desenvolvida com Spring Boot e tem como objetivo de gerenciar pedidos de um sistema de vendas online.

## **Arquitetura**
- **Tipo de API**: REST
- **Comunicação**: Síncrona (requisição-resposta)


## **Ferramentas e Tecnologias**
- **Linguagem**: Java 21
- **Automação e gerenciamento**: Maven 3.0
- **Framework**: Spring Boot
- **Banco de dados**: PostgreSQL e H2
- **Autenticação**: Basic Auth com Spring Security
- **Containerização**: Docker
- **Testes de API:** Postman
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
A API possui CRUD básico (GET, POST, PATCH, PUT E DELETE) para ORDER, USERS, PRODUCTS,COUPONS E CATEGORIES.
- `POST /orders` → Cadastra um novo Order
- `POST /users` → Cadastra um novo usuário
- `GET /products` → Busca produtos cadastrados

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
## Autenticação e Autorização
A API usa Spring Security com autenticação baseada em banco de dados. "A autenticação é feita via Basic Auth. No Postman, selecione a aba 'Authorization',
escolha 'Basic Auth' e insira o e-mail e senha do usuário."

Os dois tipos de usuários na API:

#### Perfis de Usuário
ADMIN → Tem acesso total (CRUD completo em todas as entidades).
USER → Acesso limitado a pedidos e leitura de produtos/categorias.

#### Regras de Acesso
  1. Endpoints Públicos (sem autenticação)
    `POST /users → Criar um novo usuário comum.
     GET /products/** → Listar produtos.
     GET /categories/** → Listar categorias.
     GET /cupons/** → Listar coupons.`

  2. Acesso para Usuários Comuns

     //Usuário pode gerenciar seus próprios dados
    `GET /users/{id} → Ver seus próprios dados.
     PATCH /users/{id} → Atualizar seus próprios dados.
     DELETE /users/{id} → Excluir sua própria conta.`

     //Pedidos (Orders)
    `GET /orders → Ver seus próprios pedidos.
     GET /orders/{id} → Ver detalhes de um pedido próprio.
     POST /orders → Criar pedido.
     PUT/PATCH /orders/{id} → Editar apenas seus próprios pedidos.
     DELETE /orders/{id} → Não permitido para USER, apenas alterar o status para cancelado`

  3. Acesso para Administradores (ADMIN)
  Acesso completo para o CRUD de todas as entidades.


## **Fluxos Principais**
### **Fluxo de Cadastro de Order**
1. O usuário chama `POST /orders` com os itens do carrinho e o campo Client será automaticamente associado ao ID do usuário logado.
2. A API valida estoque e calcula o total
3. Salva o pedido
4. Retorna os dados do pedido ao cliente
    Exemplo de requisição:
    ```Json
    {
          "orderStatus":"WAITING_PAYMENT",
          "items": [
              {
                  "quantity": 1,
                  "product": {
                      "id": 6
                  }
              },
              {
                  "quantity": 1,
                  "product": {
                      "id": 8
                  }
              }
          ],
          "discount": {
          "id": 3
        }
    }
    ```
**As requisições de order contam com os seguintes endpoints:**
   * Adicionar/remover cupom de desconto
   * Adicionar pagamento (CREDIT_CARD(1), DEBIT_CARD(2), PAGAR_COM_PAGBANK(3), PIX(4)), OBS: AO adicionar pagamento, os STATUS muda automático para PAID
   * Adicionar/remover produtos
   * Mudar status do Order WAITING_PAYMENT(1), PAID(2), CANCELED(3), OBS: Pedidos com status PAID e CANCELED não podem ser editados, apenas consultados.

### **Fluxo de Cadastro de Produto**
1. O usuário chama `POST /products` com os dados do produto
2. A API valida os dados seguindo critérios: e chama `ProductService`
3. O `ProductService` salva no banco via `ProductRepository`
4. Retorna o produto criado ao usuário
   Exemplo de requisição:
    ```Json
    {
    "name": "Produto A",
    "description": "Descrição do Produto A",
    "price": 199.99
    }
    ```

### **Fluxo de Cadastro de User**
1. Usuário chama o POST /users
2. Os novos usuários terão o Role de "USER" atribuídos automaticamente, a menos que a operação esteja sendo feita por um ADMIN
3. Qualquer pessoa consegue criar um usuário comum
4. Apenas Admins podem criar usuários Admins
   Exemplo de requisição:
    ```Json
    {
        "name": "Paula Araujo",
        "email": "Paula.braga@email.com",
        "phone": "11954327784",
        "password": "PA@c123",
        "role": "USER"
    }   
    ```
   
### **Validações de Entrada**
Para garantir a integridade dos dados, foram aplicadas as seguintes validações:

### **Validações Gerais:**
- Uso de `@NotNull`, `@NotBlank` e `@Size` para campos obrigatórios e tamanhos mínimos/máximos
- `@Email` para validação de formato de e-mails
- `@Column(unique = true)` junto a um código auxiliar para garantir que nenhum email possa ser associados a dois usuários diferentes
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
  * Filtro por Nome: Permite a busca de produtos pelo nome: http://localhost:8080/products?name=Camiseta
  * Filtro por Categoria: Permite a filtragem de produtos com base na categoria: http://localhost:8080/products?categoryName=Eletrônicos
  * Filtro por Valor: Permite a busca de produtos dentro de uma faixa de valor específica, pelo valor min ou max:
    http://localhost:8080/products?minPrice=100
    http://localhost:8080/products?maxPrice=500
    http://localhost:8080/products?minPrice=100&maxPrice=500

### Classe Order:
  * Filtro por Status do Pedido: http://localhost:8080/orders?PAID (para admins) e http://localhost:8080/orders?orderStatus=WAITING_PAYMENT (para user)
    Permite a filtragem de pedidos com base no status (os status estão definidos em uma enum e o filtro permite buscar pelo nome da etapa)
  * Filtro por User: http://localhost:8080/orders?userId=130 (apenas admins)

## **Banco de Dados e Persistência**
- **Banco de Dados**: PostgreSQL (perfil: DEV) e H2 (perfil: test)
- **Conexões**: Via Spring Data JPA
- **Queries**: Uso de Criteria API e JPQL para consultas dinâmicas (em construção)

## **PERFIS NO CÓDIGO**
  * "TESTE: Ambiente de testes utilizando o banco de dados H2, acessível via console web (http://localhost:8080/h2-console)."
    Utiliza a classe TestConfig para inserção de dados no banco e o Postman para testar requisições.
  * DEV: Associado ao PostgreSQL, utilizando o PgAdmin. A inserção de dados de usuários e produtos é feita através do Postman, 
  enquanto as demais tabelas são povoadas via PgAdmin.

