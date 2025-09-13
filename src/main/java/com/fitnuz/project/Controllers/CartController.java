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

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDto> addProductToCart(@PathVariable Long productId,@PathVariable Integer quantity){
        CartDto cartDto = cartService.addProduct(productId,quantity);
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

    @PutMapping("/carts/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDto> updateProductStock(@PathVariable Long productId,@PathVariable String operation){
        CartDto cartDto = cartService.updateProductStock(productId,operation);
        return new ResponseEntity<>(cartDto,HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}/products/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long productId,@PathVariable Long cartId){
        String response = cartService.deleteProductFromCart(productId,cartId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }


}
