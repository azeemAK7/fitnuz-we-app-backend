package com.fitnuz.project.Service.Definations;

import com.fitnuz.project.Payload.DTO.OrderRequestDto;
import com.fitnuz.project.Payload.Response.OrderDto;
import com.fitnuz.project.Payload.Response.OrderResponse;
import jakarta.transaction.Transactional;

public interface OrderService {
    @Transactional
    OrderDto placeOrder(String paymentMethod, OrderRequestDto orderRequestDto);

    OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrderDir);

    String updateOrderStatus(Long orderId, String status);


    OrderResponse getUserOrders(String sortBy, String sortOrderDir);
}
