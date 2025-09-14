package com.fitnuz.project.Controllers;


import com.fitnuz.project.Config.AppConstant;
import com.fitnuz.project.Payload.DTO.OrderRequestDto;
import com.fitnuz.project.Payload.DTO.OrderStatusDto;
import com.fitnuz.project.Payload.DTO.StripePaymentDto;
import com.fitnuz.project.Payload.Response.OrderDto;
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
    public ResponseEntity<OrderDto> orderProducts(@PathVariable String paymentMethod, @RequestBody OrderRequestDto orderRequestDto) {
        OrderDto order = orderService.placeOrder(
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

    @GetMapping("/admin/orders")
    public ResponseEntity<OrderResponse> getAllOrders(@RequestParam (name = "pageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer pageNumber,
                                                      @RequestParam (name = "pageSize",defaultValue = AppConstant.PAGE_SIZE_ORDERS,required = false) Integer pageSize,
                                                      @RequestParam (name = "sortBy",defaultValue = AppConstant.SORT_ORDER_BY,required = false) String sortBy,
                                                      @RequestParam (name = "sortOrderDir",defaultValue = AppConstant.SORT_ORDER_DIR_ORDERS,required = false) String sortOrderDir){
        OrderResponse response = orderService.getAllOrders(pageNumber,pageSize,sortBy,sortOrderDir);
        return new ResponseEntity<OrderResponse>(response,HttpStatus.OK);
    }

    @PutMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(@PathVariable Long orderId,@RequestBody OrderStatusDto orderStatus){
        String response = orderService.updateOrderStatus(orderId,orderStatus.getStatus());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
