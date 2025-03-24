package com.criando.projeto.entities.enums;

public enum PaymentMethod {
    CREDIT_CARD(1),
    DEBIT_CARD(2),
    PAGAR_COM_PAGBANK(3),
    PIX(4);

    //Atributo que armazena o código do metodo de pagamento.
    private int code;

    //Construtor pra receber o code
    private PaymentMethod(int code) {
        this.code = code;
    }

    //metodo pra acessar o code..
    public int getCode() {
        return code;
    }

    /*
    * O metodo percorre todos os valores do enum, compara o código de cada constante
    *  com o código recebido como parâmetro e se encontrar um valor correspondente,
    * retorna esse PaymentMethod. Se não encontrar, lança uma exceção (IllegalArgumentException).*/
    public static PaymentMethod valueOf(int code) {
        for (PaymentMethod paymentMethod : PaymentMethod.values()) {
            if (paymentMethod.getCode() == code) {
                return paymentMethod;
            }
        }
        throw new IllegalArgumentException("Código de pagamento invalido");
    }
}
