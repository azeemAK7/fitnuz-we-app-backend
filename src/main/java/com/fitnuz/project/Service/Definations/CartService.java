package com.fitnuz.project.Service.Definations;

import com.fitnuz.project.Payload.DTO.CartDto;
import com.fitnuz.project.Payload.DTO.CartItemDto;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
    @Transactional
    CartDto addProduct(Long productId, Integer quantity);

    List<CartDto> getAllCarts();


    CartDto getUserCart();

    @Transactional
    CartDto updateProductStock(Long productId, String operration);

    @Transactional
    String deleteProductFromCart(Long productId, Long cartId);

    @Transactional
    void updateProductInCarts(Long cartId, Long productId);

    @Transactional
    String createOrUpdateCartItems(List<CartItemDto> cartItemDto);
}
