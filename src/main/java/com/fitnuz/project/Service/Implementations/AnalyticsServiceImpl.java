package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Payload.Response.AnalyticsResponse;
import com.fitnuz.project.Repository.OrderRepository;
import com.fitnuz.project.Repository.ProductRepository;
import com.fitnuz.project.Service.Definations.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Override
    public AnalyticsResponse getAnalyticsData() {
        AnalyticsResponse response = new AnalyticsResponse();

        long productCount = productRepository.count();
        long orderCount = orderRepository.count();
        Double totalRevenue = orderRepository.getTotalRevenue();

        response.setTotalProducts(String.valueOf(productCount));
        response.setTotalOrders(String.valueOf(orderCount));
        response.setTotalRevenue(String.valueOf(totalRevenue != null ? totalRevenue : 0));

        return response;
    }
}
