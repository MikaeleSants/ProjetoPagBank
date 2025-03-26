package com.criando.projeto.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    WAITING_PAYMENT(1),
    PAID(2),
    CANCELED(3);

    private final int code;
    private final String name;

    OrderStatus(int code) {
        this.code = code;
        this.name = name();
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @JsonCreator
    public static OrderStatus fromString(String value) {
        if (value != null) {
            // Tenta converter para código (caso seja um número)
            try {
                int code = Integer.parseInt(value);
                for (OrderStatus status : OrderStatus.values()) {
                    if (status.getCode() == code) {
                        return status;
                    }
                }
            } catch (NumberFormatException e) {
                // Caso não seja número, tenta o nome do enum
                for (OrderStatus status : OrderStatus.values()) {
                    if (status.getName().equalsIgnoreCase(value)) {
                        return status;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Invalid OrderStatus value: " + value);
    }

    @JsonValue
    public String toValue() {
        return name;  // Retorna o nome do enum (pode ser usado para serialização)
    }
}
