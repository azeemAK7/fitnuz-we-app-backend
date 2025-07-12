package com.fitnuz.project.Service.Definations;

import com.fitnuz.project.Payload.DTO.OrderRequestDto;
import com.fitnuz.project.Payload.Response.OrderResponse;
import jakarta.transaction.Transactional;

public interface OrderService {
    @Transactional
    OrderResponse placeOrder(String paymentMethod, OrderRequestDto orderRequestDto);
}
