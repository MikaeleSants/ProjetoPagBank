package com.criando.projeto.services;

import com.criando.projeto.entities.Coupon;
import com.criando.projeto.repositories.CouponRepository;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServicesTest {

    @InjectMocks
    private CouponServices couponServices;

    @Mock
    private CouponRepository couponRepository;

    @Test
    @DisplayName("Deve retornar todos os cupons")
    void findAll() {
        Coupon c1 = new Coupon(1L, "DESCONTO10", 10.0);
        Coupon c2 = new Coupon(2L, "DESCONTO20", 20.0);
        when(couponRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<Coupon> result = couponServices.findAll();

        assertThat(result).hasSize(2).contains(c1, c2);
    }

    @Test
    @DisplayName("Deve retornar cupom por ID")
    void findById() {
        Coupon coupon = new Coupon(1L, "TESTE", 15.0);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        Coupon result = couponServices.findById(1L);

        assertThat(result).isEqualTo(coupon);
    }

    @Test
    @DisplayName("Deve lançar exceção se ID não existir")
    void findByIdException() {
        when(couponRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponServices.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    //tenho que revisar isso aqui, tá desnecessário
    @Test
    @DisplayName("Deve inserir cupom com porcentagem tratada")
    void insert() {
        Coupon cupom = new Coupon(null, "DESCONTO%", 0.0);
        when(couponRepository.save(any())).thenReturn(new Coupon(1L, "DESCONTO%", 15.0));

        cupom.setDiscountPercentage(15.0);
        Coupon result = couponServices.insert(cupom);

        assertThat(result.getId()).isEqualTo(1L);
        verify(couponRepository).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar ID inexistente")
    void delete() {
        doThrow(new EmptyResultDataAccessException(1)).when(couponRepository).deleteById(99L);

        assertThatThrownBy(() -> couponServices.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve atualizar um cupom existente")
    void update() {
        Long id = 1L;
        Coupon existente = new Coupon(id, "VELHO10", 10.0);
        Coupon atualizado = new Coupon(id, "NOVO20", 20.0);

        when(couponRepository.findById(id)).thenReturn(Optional.of(existente));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Coupon result = couponServices.update(id, atualizado);

        assertThat(result.getCode()).isEqualTo("NOVO20");
        assertThat(result.getDiscountPercentage()).isEqualTo(20.0);
        verify(couponRepository).save(existente); // garante que foi o mesmo objeto modificado que foi salvo
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar cupom inexistente")
    void updateException() {
        Long idInvalido = 999L;
        Coupon novo = new Coupon(idInvalido, "NOVO", 25.0);

        when(couponRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponServices.update(idInvalido, novo))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cupom não encontrado: ID");
    }


}
