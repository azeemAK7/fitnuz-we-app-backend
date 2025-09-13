package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Exception.CustomException.GeneralAPIException;
import com.fitnuz.project.Exception.CustomException.ResourceNotFoundException;
import com.fitnuz.project.Model.*;
import com.fitnuz.project.Payload.DTO.OrderItemDto;
import com.fitnuz.project.Payload.DTO.OrderRequestDto;
import com.fitnuz.project.Payload.Response.AddressResponse;
import com.fitnuz.project.Payload.Response.OrderResponse;
import com.fitnuz.project.Repository.*;
import com.fitnuz.project.Service.Definations.CartService;
import com.fitnuz.project.Service.Definations.MailService;
import com.fitnuz.project.Service.Definations.OrderService;
import com.fitnuz.project.Util.AuthUtil;
import com.fitnuz.project.Util.PdfGenerator;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AuthUtil authUtil;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private PdfGenerator pdfGenerator;

    @Autowired
    private MailService mailService;

    @Value("${spring.mail.username}")
    private String mail;


    @Transactional
    @Override
    public OrderResponse placeOrder(String paymentMethod, OrderRequestDto orderRequestDto) {
        String emailId = authUtil.getUserEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }
        Address address = addressRepository.findById(orderRequestDto.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", orderRequestDto.getAddressId()));

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, orderRequestDto.getPgPaymentId(), orderRequestDto.getPgStatus(), orderRequestDto.getPgResponseMessage(), orderRequestDto.getPgName());
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new GeneralAPIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getProductDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        List<CartItem> items = new ArrayList<>(cart.getCartItems());
        for (CartItem item : items){
            int quantity = item.getQuantity();
            Product product = item.getProduct();

            // Reduce stock quantity
            product.setProductStock(product.getProductStock() - quantity);

            // Save product back to the database
            productRepository.save(product);

            cartItemRepository.deleteCartItemByProductIdAndCartId(cart.getCartId(), product.getProductId());
        }
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);
        OrderResponse orderResponse = modelMapper.map(savedOrder, OrderResponse.class);
        orderItems.forEach(item -> orderResponse.getOrderItems().add(modelMapper.map(item, OrderItemDto.class)));

        AddressResponse addressResponse = modelMapper.map(savedOrder.getAddress(), AddressResponse.class);
        orderResponse.setAddress(addressResponse.getFullAddress());

        try {
            byte[] pdfReport = pdfGenerator.generateOrderReport(orderResponse);
            String adminEmail = mail; // Replace with actual admin email
            mailService.sendOrderReport(
                    adminEmail,
                    "New Order Received - #" + savedOrder.getOrderId(),
                    "Please find the attached order report.",
                    pdfReport,
                    "Order_" + savedOrder.getOrderId() + ".pdf"
            );
        } catch (Exception e) {
            e.printStackTrace(); // Optionally log the failure but don't fail the order placement
        }

        return orderResponse;
    }



}

