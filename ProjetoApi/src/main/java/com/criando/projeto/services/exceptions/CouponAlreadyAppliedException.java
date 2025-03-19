package com.criando.projeto.services.exceptions;

public class CouponAlreadyAppliedException extends RuntimeException {
    public CouponAlreadyAppliedException(Long couponId) {
        super("Coupon with ID " + couponId + " is already applied to the order.");
    }
}
