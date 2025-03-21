# POVOANDO O BANCO DE DADOS:

## Criando Users
No postman, para criar multiplos usuários eu usei, na aba "tests/scripts":
var users = [
    { "name": "Carlos Almeida", "email": "carlos.almeida@email.com", "phone": "11987651234", "password": "A@b123" },
    { "name": "Mariana Souza", "email": "mariana.souza@email.com", "phone": "11976549876", "password": "M@r123" },
    { "name": "Roberto Lima", "email": "roberto.lima@email.com", "phone": "11965438765", "password": "R0b@321" },
    { "name": "Fernanda Castro", "email": "fernanda.castro@email.com", "phone": "11954327654", "password": "F@c123" },
    { "name": "Eduardo Menezes", "email": "eduardo.menezes@email.com", "phone": "11943216543", "password": "E@m456" }
];

users.forEach(user => {
    pm.sendRequest({
        url: "http://localhost:8080/users",
        method: "POST",
        header: { "Content-Type": "application/json" },
        body: { mode: "raw", raw: JSON.stringify(user) }
    }, function (err, res) {
        console.log(res);
    });
});

## Criando Category
Depois, dentro do PgAdmin, eu criei as categorias:
INSERT INTO tb_category (name)
VALUES
    ('Roupas'),
    ('Eletrônicos'),
    ('Móveis'),
    ('Esportes'),
    ('Beleza'),
    ('Alimentos'),
    ('Automóveis');

## Criando Product
No PgAdmin tbm:

-- Inserir produtos
INSERT INTO tb_product (name, description, price)
VALUES
('Camiseta Básica', 'Camiseta de algodão, confortável e durável.', 49.90),
('Calça Jeans', 'Calça jeans de corte reto e confortável.', 99.90),
('Tênis Esportivo', 'Tênis para caminhada com ótima aderência.', 179.90),
('Mochila de Couro', 'Mochila de couro legítimo, ideal para o trabalho ou estudos.', 299.90),
('Relógio de Pulso', 'Relógio analógico com design moderno.', 159.90),
('Fone de Ouvido Bluetooth', 'Fones de ouvido com cancelamento de ruído e excelente qualidade de som.', 299.90),
('Smartphone', 'Smartphone com câmera de alta resolução e grande capacidade de armazenamento.', 1999.90),
('Laptop Gamer', 'Laptop de alta performance, ideal para jogos.', 5000.00),
('Mesa de Escritório', 'Mesa ampla, ideal para home office.', 699.90),
('Cadeira Ergonômica', 'Cadeira ergonômica, ideal para longas horas de trabalho.', 399.90);


## Associando os produtos às categorias
No PgAdmin:

-- Associar produtos às categorias (tb_product_category)
-- A primeira coluna é o product_id e a segunda é o category_id

-- Camiseta Básica
INSERT INTO tb_product_category (product_id, category_id) VALUES (1, 1);  -- Roupas

-- Calça Jeans
INSERT INTO tb_product_category (product_id, category_id) VALUES (2, 1);  -- Roupas

-- Tênis Esportivo
INSERT INTO tb_product_category (product_id, category_id) VALUES (3, 4);  -- Esportes

-- Mochila de Couro
INSERT INTO tb_product_category (product_id, category_id) VALUES (4, 1);  -- Roupas

-- Relógio de Pulso
INSERT INTO tb_product_category (product_id, category_id) VALUES (5, 2);  -- Eletrônicos

-- Fone de Ouvido Bluetooth
INSERT INTO tb_product_category (product_id, category_id) VALUES (6, 2);  -- Eletrônicos

-- Smartphone
INSERT INTO tb_product_category (product_id, category_id) VALUES (7, 2);  -- Eletrônicos

-- Laptop Gamer
INSERT INTO tb_product_category (product_id, category_id) VALUES (8, 2);  -- Eletrônicos

-- Mesa de Escritório
INSERT INTO tb_product_category (product_id, category_id) VALUES (9, 3);  -- Móveis

-- Cadeira Ergonômica
INSERT INTO tb_product_category (product_id, category_id) VALUES (10, 3);  -- Móveis

## Inserido dados na Order
-- Inserir pedidos (tb_order)
INSERT INTO tb_order (moment, order_status, client_id)
VALUES
('2025-03-17T10:30:00Z', 1, 1),  -- Pedido 1 (status: WAITING_PAYMENT)
('2025-03-17T11:00:00Z', 2, 2),  -- Pedido 2 (status: PAID)
('2025-03-17T12:15:00Z', 3, 3),  -- Pedido 3 (status: SHIPPED)
('2025-03-17T14:45:00Z', 4, 4),  -- Pedido 4 (status: DELIVERED)
('2025-03-17T16:00:00Z', 5, 5);  -- Pedido 5 (status: CANCELED)


## Adicionando os Payment:

No PgAdmin:

-- Inserir pagamentos (tb_payment)
INSERT INTO tb_payment (moment, order_id, payment_method)
VALUES
('2025-03-17T10:30:00Z', 1, 1),  -- Pagamento 1 (Método: CREDIT_CARD)
('2025-03-17T11:00:00Z', 2, 2),  -- Pagamento 2 (Método: DEBIT_CARD)
('2025-03-17T12:15:00Z', 3, 4),  -- Pagamento 3 (Método: PIX)
('2025-03-17T14:45:00Z', 4, 3),  -- Pagamento 4 (Método: PAGAR_COM_PAGBANK)
('2025-03-17T16:00:00Z', 5, 1);  -- Pagamento 5 (Método: CREDIT_CARD)

## Inserindo os Orders Itens:

No PgAdmin:

-- Inserir itens de pedido (tb_order_item)
-- Pedido 1 (ID = 1)
INSERT INTO tb_order_item (order_id, product_id, quantity, price)
VALUES
(1, 1, 2, 49.90),  -- 2 Camisetas Básicas
(1, 2, 1, 99.90);  -- 1 Calça Jeans

-- Pedido 2 (ID = 2)
INSERT INTO tb_order_item (order_id, product_id, quantity, price)
VALUES
(2, 3, 1, 179.90),  -- 1 Tênis Esportivo
(2, 4, 1, 299.90);  -- 1 Mochila de Couro

-- Pedido 3 (ID = 3)
INSERT INTO tb_order_item (order_id, product_id, quantity, price)
VALUES
(3, 5, 1, 159.90),  -- 1 Relógio de Pulso
(3, 6, 2, 299.90);  -- 2 Fones de Ouvido Bluetooth

-- Pedido 4 (ID = 4)
INSERT INTO tb_order_item (order_id, product_id, quantity, price)
VALUES
(4, 7, 1, 1999.90),  -- 1 Smartphone
(4, 8, 1, 5000.00);  -- 1 Laptop Gamer

-- Pedido 5 (ID = 5)
INSERT INTO tb_order_item (order_id, product_id, quantity, price)
VALUES
(5, 9, 1, 699.90),  -- 1 Mesa de Escritório
(5, 10, 1, 399.90);  -- 1 Cadeira Ergonômica


-- NOTA:
Caso queria resetar a contagem dos id, usar esse comando:
ALTER SEQUENCE tb_user_id_seq RESTART WITH 1;
trocando o tb_user pela tabela especifica

Para verificar o próximo ID que será atribuído automaticamente a uma tabela no MySQL:
SELECT pg_sequence_last_value('tb_order_id_seq') + 1

# CRIANDO REQUISIÇÕES APÓS CRUD DE ORDER:

### TESTE DE CRIAÇÃO DE ORDER SEM PAYMENT E SEM COUPOM:

Requisição POST:
```Json
{
"orderStatus": 1,
"client": {
"id": 3
},
"items": [
{
"quantity": 2,
"product": {
"id": 4
}
},
{
"quantity": 1,
"product": {
"id": 3
}
}
]
}
```
Resultado:
```json
{
  "id": 4,
  "moment": "2025-03-21T18:02:24Z",
  "orderStatus": "PAID",
  "client": {
    "id": 3,
    "name": null,
    "email": null,
    "phone": null,
    "password": null
  },
  "items": [
    {
      "quantity": 1,
      "price": 179.9,
      "product": {
        "id": 3,
        "name": "Tênis Esportivo",
        "description": "Tênis para caminhada com ótima aderência.",
        "price": 179.9,
        "categories": [
          {
            "id": 4,
            "name": "Esportes"
          }
        ]
      },
      "subTotal": 179.9
    },
    {
      "quantity": 2,
      "price": 299.9,
      "product": {
        "id": 4,
        "name": "Mochila de Couro",
        "description": "Mochila de couro legítimo, ideal para o trabalho ou estudos.",
        "price": 299.9,
        "categories": [
          {
            "id": 1,
            "name": "Roupas"
          }
        ]
      },
      "subTotal": 599.8
    }
  ],
  "payment": null,
  "discount": null,
  "total": 779.6999999999999
}
```
obs: lembrar de formatar os números

### APLICANDO O CUPOM EM UM ORDER JÁ CRIADO:

POST COM A URL:
http://localhost:8080/orders/1/apply-coupon/2
Sendo:
"/{orderId}/apply-coupon/{couponId}"

RESULTADO:

```json
{
  "id": 1,
  "moment": "2025-03-20T16:54:36Z",
  "orderStatus": "PAID",
  "client": {
    "id": 10,
    "name": "Eduardo Menezes",
    "email": "eduardo.menezes@email.com",
    "phone": "11943216543",
    "password": "E@m456"
  },
  "items": [
    {
      "quantity": 2,
      "price": 49.9,
      "product": {
        "id": 1,
        "name": "Camiseta Básica",
        "description": "Camiseta de algodão, confortável e durável.",
        "price": 49.9,
        "categories": [
          {
            "id": 1,
            "name": "Roupas"
          }
        ]
      },
      "subTotal": 99.8
    },
    {
      "quantity": 1,
      "price": 179.9,
      "product": {
        "id": 3,
        "name": "Tênis Esportivo",
        "description": "Tênis para caminhada com ótima aderência.",
        "price": 179.9,
        "categories": [
          {
            "id": 4,
            "name": "Esportes"
          }
        ]
      },
      "subTotal": 179.9
    }
  ],
  "payment": null,
  "discount": {
    "id": 2,
    "code": "WELCOME",
    "discountPercentage": 10.0
  },
  "total": 251.73
}
```

### CRIANDO UM ORDER COM CUPOM:

Requisição POST:
```json
{
  "orderStatus": 1,
  "client": {
    "id": 2
  },
  "items": [
    {
      "quantity": 2,
      "product": {
        "id": 3
      }
    },
    {
      "quantity": 1,
      "product": {
        "id": 5
      }
    }
  ],
  "discount": {
    "id": 2
  }
}
```

RESULTADO:
```json
{
  "id": 5,
  "moment": "2025-03-21T18:14:08Z",
  "orderStatus": "PAID",
  "client": {
    "id": 2,
    "name": null,
    "email": null,
    "phone": null,
    "password": null
  },
  "items": [
    {
      "quantity": 1,
      "price": 159.9,
      "product": {
        "id": 5,
        "name": "Relógio de Pulso",
        "description": "Relógio analógico com design moderno.",
        "price": 159.9,
        "categories": [
          {
            "id": 2,
            "name": "Eletrônicos"
          }
        ]
      },
      "subTotal": 159.9
    },
    {
      "quantity": 2,
      "price": 179.9,
      "product": {
        "id": 3,
        "name": "Tênis Esportivo",
        "description": "Tênis para caminhada com ótima aderência.",
        "price": 179.9,
        "categories": [
          {
            "id": 4,
            "name": "Esportes"
          }
        ]
      },
      "subTotal": 359.8
    }
  ],
  "payment": null,
  "discount": {
    "id": 2,
    "code": "WELCOME",
    "discountPercentage": 10.0
  },
  "total": 467.73
}
```