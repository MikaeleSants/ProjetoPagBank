package com.criando.projeto.services;

import com.criando.projeto.entities.Coupon;
import com.criando.projeto.repositories.CouponRepository;

import com.criando.projeto.services.exceptions.DatabaseException;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CouponServices {

    @Autowired
    private CouponRepository couponRepository;

    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    public Coupon findById(Long id) {
        Optional <Coupon> obj =  couponRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public Coupon insert(Coupon obj) {
        double discount = parseDiscount(String.valueOf(obj.getDiscountPercentage()));
        obj.setDiscountPercentage(discount);
        return couponRepository.save(obj);
    }

    public void delete(Long id) {
        try {
            couponRepository.deleteById(id); } catch (EmptyResultDataAccessException e)
        {throw new ResourceNotFoundException(id);} catch (DataIntegrityViolationException e)
        {throw new DatabaseException(e.getMessage());}
    }


    public Coupon update(Long id, Coupon obj) {
        try {
            Coupon entity = couponRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));
            updateData(entity, obj);
            return couponRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }

    private void updateData(Coupon entity, Coupon obj) {
        entity.setCode(obj.getCode());
        entity.setDiscountPercentage(obj.getDiscountPercentage());
    }

    // Remover % se existir
    public double parseDiscount(String discountStr) {
        discountStr = discountStr.replace("%", "").trim();
        try {
            return Double.parseDouble(discountStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato inválido para desconto. Use um número entre 1 e 100.");
        }
    }

}
