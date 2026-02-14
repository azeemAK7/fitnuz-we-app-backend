package com.fitnuz.project.Controllers;


import com.fitnuz.project.Payload.DTO.CartDto;
import com.fitnuz.project.Payload.DTO.CartItemDto;
import com.fitnuz.project.Service.Definations.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/carts/variants/{variantId}/quantity/{quantity}")
    public ResponseEntity<CartDto> addProductToCart(@PathVariable Long variantId,@PathVariable Integer quantity){
        CartDto cartDto = cartService.addProduct(variantId,quantity);
        return new ResponseEntity<>(cartDto, HttpStatus.CREATED);
    }

    @PostMapping("/carts/updater")
    public ResponseEntity<String> createOrUpdateCartItems(@RequestBody List<CartItemDto> cartItemDto){
        String response = cartService.createOrUpdateCartItems(cartItemDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/admin/carts")
    public ResponseEntity<List<CartDto>> getAllCarts(){
        List<CartDto> cartDto = cartService.getAllCarts();
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDto> getUserCart(){
        CartDto cartDto = cartService.getUserCart();
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    @PutMapping("/carts/variants/{variantId}/quantity/{operation}")
    public ResponseEntity<CartDto> updateProductStock(@PathVariable Long variantId,@PathVariable String operation){
        CartDto cartDto = cartService.updateProductStock(variantId,operation);
        return new ResponseEntity<>(cartDto,HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}/variants/{variantId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long variantId,@PathVariable Long cartId){
        String response = cartService.deleteProductFromCart(variantId,cartId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }


}
