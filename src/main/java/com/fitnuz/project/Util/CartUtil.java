package com.fitnuz.project.Util;

import com.fitnuz.project.Model.Cart;
import org.springframework.stereotype.Component;

@Component
public class CartUtil {

    public  double calculateTotalPrice(Cart cart) {
        return cart.getCartItems()
                .stream()
                .mapToDouble(item -> item.getProductPrice() * item.getQuantity())
                .sum();
    }
}
