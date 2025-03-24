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

### CRIANDO CUPOM:

/cupons
{
"code": "DESC40",
"discountPercentage": 40.0
}

### TESTE DE CRIAÇÃO DE ORDER SEM PAYMENT E SEM CUPOM:

/orders
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
  "total": 779.7
}
```

### APLICANDO O CUPOM EM UM ORDER JÁ CRIADO:

POST COM A URL:
/orders/1/apply-coupon/2
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
/orders

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
### ADICIONANDO PAGAMENTO AO ORDER
orders/5/payment
{"paymentMethod": "PIX"}

### PUT DE ATUALIZAÇÃO GERAL (SEM MEXER NOS PRODUTOS):
/orders
```json
{
"orderStatus": 1,
"client": {
"id": 1
},
"discount": {
"id": 3
}
}
```
### ADICIONANDO PRODUTOS AO ORDER:
/orders/8/items

```json
[
{
"product": {
"id": 1
},
"quantity": 3
},
{
"product": {
"id": 2
},
"quantity": 1
}
]
```
### atualizando o status:
orders/5/status

```json
{ "orderStatus": "CANCELED" }
```

### DELETANDO O CUPOM DE UM PEDIDO:
/orders/8/remove-coupon

### DELETENDO UM PRODUTO:
/orders/5/remove-product/3

-- NOTA:
Caso queria resetar a contagem dos id, usar esse comando:
ALTER SEQUENCE tb_user_id_seq RESTART WITH 1;
trocando o tb_user pela tabela especifica

Para verificar o próximo ID que será atribuído automaticamente a uma tabela no MySQL:
SELECT pg_sequence_last_value('tb_order_id_seq') + 1