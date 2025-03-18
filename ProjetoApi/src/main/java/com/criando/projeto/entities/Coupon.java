package com.criando.projeto.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    @NotBlank
    private String code;
    @NotBlank
    @Min(value = 1, message = "O desconto deve ser no mínimo 1.")
    @Max(value = 100, message = "O desconto não pode ser maior que 100.")
    private Double discountPercentage;

    @JsonIgnore
    @OneToOne
    private Order coupon_discount;

    public Coupon() {
    }

    public Coupon(Long id, String code, Double discountPercentage) {
        this.id = id;
        this.code = code;
        this.discountPercentage = discountPercentage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", discountPercentage=" + discountPercentage +
                '}';
    }
}
