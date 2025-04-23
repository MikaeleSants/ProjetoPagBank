package com.criando.projeto.resource;

import com.criando.projeto.entities.Coupon;
import com.criando.projeto.repositories.CouponRepository;
import com.criando.projeto.services.CouponServices;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponResourcesTest {
    @InjectMocks
    private CouponResources couponResources;

    @Mock
    private CouponServices couponServices;

    private Coupon coupon;
    private Coupon couponDois;

    @BeforeEach
    void setUp() {
        coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("DESC10");
        coupon.setDiscountPercentage(10.0);

        couponDois = new Coupon();
        couponDois.setId(2L);
        couponDois.setCode("DESC20");
        couponDois.setDiscountPercentage(20.0);
    }

    @Test
    @DisplayName("Deve retornar todos os cupons, com status 200 OK")
    void findAll() {
        List<Coupon> coupons = List.of(coupon, couponDois);
        when(couponServices.findAll()).thenReturn(coupons);

        ResponseEntity<List<Coupon>> result = couponResources.findAll();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(coupons, result.getBody());

        verify(couponServices).findAll();
    }

    @Test
    @DisplayName("Deve retornar um cupom por ID, com status 200 OK")
    void findById() {
        when(couponServices.findById(1L)).thenReturn(coupon);

        ResponseEntity<Coupon> result = couponResources.findById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(coupon, result.getBody());

        verify(couponServices).findById(1L);
    }

    //Ver se precisa manter mesmo
    @Test
    @DisplayName("Deve lançar exceção quando cupom não for encontrado")
    void findByIdshouldThrowWhenCouponNotFound() {
        Long invalidId = 999L;

        when(couponServices.findById(invalidId))
                .thenThrow(new ResourceNotFoundException("Cupom não encontrado")); // ou sua exceção custom
        assertThrows(ResourceNotFoundException.class, () -> couponResources.findById(invalidId));
        verify(couponServices).findById(invalidId);
    }

    @Test
    @DisplayName("Deve criar um novo cupom e retornar status 201 Created")
    void Insert() {
        Coupon newCoupon = new Coupon();
        newCoupon.setCode("DESC15");
        newCoupon.setDiscountPercentage(15.0);

        Coupon savedCoupon = new Coupon();
        savedCoupon.setId(1L);
        savedCoupon.setCode("DESC15");
        savedCoupon.setDiscountPercentage(15.0);

        URI fakeUri = URI.create("/cupons/1");

        // mock estático do builder
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = Mockito.mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentRequest).thenReturn(builder);
            Mockito.when(builder.path("/{id}")).thenReturn(builder);
            Mockito.when(builder.buildAndExpand(savedCoupon.getId())).thenReturn(Mockito.mock(UriComponents.class));
            Mockito.when(builder.buildAndExpand(savedCoupon.getId()).toUri()).thenReturn(fakeUri);

            Mockito.when(couponServices.insert(newCoupon)).thenReturn(savedCoupon);

            ResponseEntity<Coupon> response = couponResources.insert(newCoupon);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(savedCoupon, response.getBody());
            assertEquals(fakeUri, response.getHeaders().getLocation());
            verify(couponServices).insert(newCoupon);
        }
    }

    @Test
    @DisplayName("Deve deletar o cupom com sucesso e retornar status 204 No Content")
    void Delete() {
        doNothing().when(couponServices).delete(coupon.getId());
        ResponseEntity<Void> response = couponResources.delete(coupon.getId());
        verify(couponServices, times(1)).delete(coupon.getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve atualizar o cupom com sucesso e retornar o cupom atualizado")
    void Update() {
        Coupon updatedCoupon = new Coupon();
        updatedCoupon.setId(1L);
        updatedCoupon.setCode("DESC30");
        updatedCoupon.setDiscountPercentage(30.0);
        // Quando o método update for chamado
        Mockito.when(couponServices.update(coupon.getId(), coupon)).thenReturn(updatedCoupon);
        // Chamando o método do controller
        ResponseEntity<Coupon> response = couponResources.update(coupon.getId(), coupon);
        // E verificamos se o status retornado é 200 OK e se o cupom retornado é o atualizado
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedCoupon, response.getBody());
        // Então, verificamos se o método update foi chamado com o ID e o objeto corretos
        verify(couponServices, times(1)).update(coupon.getId(), coupon);
    }


}
