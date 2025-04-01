package com.criando.projeto.entities.enums;

public enum PaymentMethod {
    CREDIT_CARD(1),
    DEBIT_CARD(2),
    PAGAR_COM_PAGBANK(3),
    PIX(4);


    private int code;


    private PaymentMethod(int code) {
        this.code = code;
    }


    public int getCode() {
        return code;
    }


    public static PaymentMethod valueOf(int code) {
        for (PaymentMethod paymentMethod : PaymentMethod.values()) {
            if (paymentMethod.getCode() == code) {
                return paymentMethod;
            }
        }
        throw new IllegalArgumentException("Código de pagamento invalido");
    }
}
