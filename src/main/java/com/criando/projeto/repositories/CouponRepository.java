package com.criando.projeto.repositories;

import com.criando.projeto.entities.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
//O Long representa o tipo de dado da chave primária (ID) da entidade.
}
