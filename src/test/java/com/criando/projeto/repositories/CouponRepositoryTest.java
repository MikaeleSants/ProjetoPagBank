package com.criando.projeto.repositories;

import com.criando.projeto.entities.Coupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CouponRepositoryTest {

    @Autowired
    private CouponRepository couponRepository;

    private Coupon coupon;
    private Coupon coupon2;
    private Coupon coupon3;


    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();

        coupon = new Coupon(null, "BLACKFRIDAY", 50.0);
        coupon2 = new Coupon(null, "WELLCOME", 20.0);
        coupon3 = new Coupon(null, "10DESC", 10.0);

        couponRepository.saveAll(List.of(coupon, coupon2, coupon3));
    }

    @Test
    @DisplayName("Deve encontrar categoria pelo ID")
    void deveEncontrarCouponPorId() {
        Optional<Coupon> result = couponRepository.findById(coupon.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("BLACKFRIDAY");
    }

    @Test
    @DisplayName("Deve retornar todas as categorias")
    void deveRetornarTodosOsCoupons() {
        var coupons = couponRepository.findAll();
        assertThat(coupons).hasSize(3);
        assertThat(coupons).extracting(Coupon::getCode)
        .containsExactlyInAnyOrder("BLACKFRIDAY", "WELLCOME", "10DESC");
    }

    @Test
    @DisplayName("Deve salvar um novo Coupon")
    void deveSalvarCoupon() {
        Coupon newCoupon = new Coupon(null, "DESC30", 30.0);
        couponRepository.save(newCoupon);

        assertThat(newCoupon.getId()).isNotNull();
        assertThat(newCoupon.getCode()).isEqualTo("DESC30");
    }

    @Test
    @DisplayName("Deve atualizar um Coupon existente")
    void deveAtualizarCoupon() {
        Coupon coupon1 = couponRepository.findById(coupon.getId()).orElseThrow();
        coupon1.setCode("BLACKFRIDAY2");
        Coupon atualizado = couponRepository.save(coupon1);

        assertThat(atualizado.getCode()).isEqualTo("BLACKFRIDAY2");
    }

    @Test
    @DisplayName("Deve deletar uma categoria pelo ID")
    void deveDeletarCoupon() {
        couponRepository.deleteById(coupon.getId());
        Optional<Coupon> result = couponRepository.findById(coupon.getId());
        assertThat(result).isEmpty();
    }
}