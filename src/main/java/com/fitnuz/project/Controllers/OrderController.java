package com.fitnuz.project.Controllers;


import com.fitnuz.project.Payload.DTO.OrderRequestDto;
import com.fitnuz.project.Payload.DTO.StripePaymentDto;
import com.fitnuz.project.Payload.Response.OrderResponse;
import com.fitnuz.project.Service.Definations.OrderService;
import com.fitnuz.project.Service.Definations.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private StripeService stripeService;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderResponse> orderProducts(@PathVariable String paymentMethod, @RequestBody OrderRequestDto orderRequestDto) {
        OrderResponse order = orderService.placeOrder(
                paymentMethod,
                orderRequestDto
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/order/stripe-client-secret")
    public ResponseEntity<String> createStripeClientSecret(@RequestBody StripePaymentDto stripePaymentDto) throws StripeException {
        PaymentIntent paymentIntent = stripeService.paymentIntent(stripePaymentDto
        );
        return new ResponseEntity<>(paymentIntent.getClientSecret(), HttpStatus.CREATED);
    }
}
