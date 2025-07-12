package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Exception.CustomException.DuplicateResourceFoundException;
import com.fitnuz.project.Exception.CustomException.GeneralAPIException;
import com.fitnuz.project.Exception.CustomException.ResourceNotFoundException;
import com.fitnuz.project.Model.Cart;
import com.fitnuz.project.Model.CartItem;
import com.fitnuz.project.Model.Product;
import com.fitnuz.project.Model.User;
import com.fitnuz.project.Payload.DTO.CartDto;
import com.fitnuz.project.Payload.DTO.CartItemDto;
import com.fitnuz.project.Payload.DTO.ProductDto;
import com.fitnuz.project.Repository.CartItemRepository;
import com.fitnuz.project.Repository.CartRepository;
import com.fitnuz.project.Repository.CategoryRepository;
import com.fitnuz.project.Repository.ProductRepository;
import com.fitnuz.project.Service.Definations.CartService;
import com.fitnuz.project.Util.AuthUtil;
import com.fitnuz.project.Util.CartUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CartUtil cartUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Value("${project.image}")
    private String path;

    @Value("${spring.app.backend}")
    private String imageUrl;

    @Transactional
    @Override
    public CartDto addProduct(Long productId, Integer quantity) {
        Cart cart = createCart();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","ProductId",productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );

        if(cartItem != null){
            throw new DuplicateResourceFoundException("Product with product name : " + product.getProductName()+" already exists");
        }
        if(product.getProductQuantity() == 0){
            throw new GeneralAPIException("Product " + product.getProductName() +" is out of stock");
        }
        if(product.getProductQuantity() < quantity){
            throw new GeneralAPIException("Add quantity below " + product.getProductQuantity());
        }
        cartItem = new CartItem();
        cartItem.setQuantity(quantity);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setProductDiscount(product.getDiscount());
        cartItem.setProduct(product);
        cartItem.setCart(cart);
        cartItemRepository.save(cartItem);

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cart.getCartItems().add(cartItem);
        cart = cartRepository.save(cart);

        CartDto cartDto = modelMapper.map(cart,CartDto.class);
        cartDto.setUserName(cart.getUser().getUserName());
        List<CartItem> cartItems = cart.getCartItems();
        List<ProductDto> productDtos = cartItems.stream().map(item->{
            ProductDto productDto = modelMapper.map(item.getProduct(),ProductDto.class);
            productDto.setProductQuantity(quantity);
            return productDto;
        })
                .toList();
        cartDto.setProducts(productDtos);
        return cartDto;
    }

    @Override
    public List<CartDto> getAllCarts() {
        List<Cart> carts =  cartRepository.findAll();
        if(carts.isEmpty()){
            throw new GeneralAPIException("Cart Is Empty");
        }
        List<CartDto> cartDtos = carts.stream().map(cart ->{
            CartDto cartDto = modelMapper.map(cart,CartDto.class);
            List<ProductDto> productDtos = cart.getCartItems().stream()
                    .map(cartItem-> {
                        ProductDto productDto = modelMapper.map(cartItem.getProduct(), ProductDto.class);
                        productDto.setProductQuantity(cartItem.getQuantity());
                        return productDto;
                    })
                    .toList();
            cartDto.setUserName(cart.getUser().getUserName());
            cartDto.setProducts(productDtos);
            return cartDto;
        }).toList();
        return cartDtos;
    }

    @Override
    public CartDto getUserCart() {
        User user = authUtil.getUser();
        Cart cart = cartRepository.findCartByEmail(user.getEmail());
        if(cart == null){
            throw new GeneralAPIException("Cart Does Not Exist");
        }
        // If cart is now empty, delete it
//        if (cart.getCartItems().isEmpty()) {
//            cartRepository.delete(cart);
//            throw new GeneralAPIException("Cart Is Empty");
//        }
        CartDto cartDto = modelMapper.map(cart,CartDto.class);
        List<ProductDto> productDtos = cart.getCartItems().stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();
                    product.setProductQuantity(cartItem.getQuantity());
                    product.setImage(constructImageUrl(product.getImage()));
                    return modelMapper.map(cartItem.getProduct(),ProductDto.class);
                })
                .toList();
        cartDto.setProducts(productDtos);
        cartDto.setUserName(cart.getUser().getUserName());
        return cartDto;
    }

    @Transactional
    @Override
    public CartDto updateProductQuantity(Long productId, String operation) {

        int value =  operation.equalsIgnoreCase("increase") ? 1:-1;

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","ProductId",productId));

        Cart cart = cartRepository.findCartByEmail(authUtil.getUserEmail());
        if(cart == null){
            throw new GeneralAPIException("Cart Not Found");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(),productId);
        if(cartItem == null){
            throw new ResourceNotFoundException("CartItem","CartId",cart.getCartId());
        }

        if(product.getProductQuantity() == 0){
            throw new GeneralAPIException("Product " + product.getProductName() +" is out of stock");
        }

        Integer quantity = cartItem.getQuantity() + value;

        if (product.getProductQuantity() < quantity) {
            throw new GeneralAPIException("Add quantity below or equal to " + product.getProductQuantity());
        }

        if (quantity <= 0) {
            // Remove cartItem from cart
            cart.getCartItems().remove(cartItem);// orphanRemoval = true takes care of DB
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setProductDiscount(product.getDiscount());
            cartItem.setQuantity(quantity);

            if (!cart.getCartItems().contains(cartItem)) {
                cart.getCartItems().add(cartItem);
            }
        }


        // Recalculate total price
        cart.setTotalPrice(cartUtil.calculateTotalPrice(cart));

        // Save cart (will cascade to CartItems)
        cartRepository.save(cart);




        CartDto cartDto = modelMapper.map(cart,CartDto.class);
        List<ProductDto> productDtos = cart.getCartItems().stream()
                .map(item -> {
                    ProductDto productDto = modelMapper.map(item.getProduct(), ProductDto.class);
                    productDto.setProductQuantity(item.getQuantity());
                    return productDto;
                })
                .toList();
        cartDto.setProducts(productDtos);
        return cartDto;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long productId, Long cartId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","ProductId",productId));
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("cart","cartId",cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null){
           throw  new ResourceNotFoundException("cartItem","cartId",cartId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice()*cartItem.getQuantity()));
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        return "product with product name : " + product.getProductName() + " removed from cart";
    }

    @Transactional
    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","ProductId",productId));
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("cart","cartId",cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null){
            throw  new ResourceNotFoundException("cartItem","cartId",cartId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice()*cartItem.getQuantity()));
        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice()*cartItem.getQuantity()));
        cartItem.setProductDiscount(product.getDiscount());
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);

    }

    @Transactional
    @Override
    public String createOrUpdateCartItems(List<CartItemDto> cartItems) {
        Cart cart = cartRepository.findCartByEmail(authUtil.getUserEmail());
        if(cart == null){
            cart = new Cart();
            cart.setUser(authUtil.getUser());
            cartRepository.save(cart);
        }else{
            cartItemRepository.deleteAllItemByCartId(cart.getCartId());
        }
        double totalPrice = 0.0;
        for(CartItemDto cartItemDto : cartItems){
            Long productId = cartItemDto.getProductId();
            Integer quantity = cartItemDto.getProductQuantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

            totalPrice += product.getSpecialPrice() * quantity;

            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setProductDiscount(product.getDiscount());
            cartItemRepository.save(cartItem);
        }
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart);
        return "Cart created/updated with the new items successfully";
    }

    public Cart createCart(){
        Cart cart = cartRepository.findCartByEmail(authUtil.getUserEmail());
        if(cart == null){
            cart = new Cart();
            cart.setUser(authUtil.getUser());
            return cartRepository.save(cart);
        }
        return cart;
    }


    public String constructImageUrl(String fileName){
        String fullUrl = imageUrl + path;
        if(fileName.equals("default.png")){
            return fullUrl + "a06f0845-8a92-4af9-8ee5-f655012d7fff.png";
        }
        return path.endsWith("/") ? fullUrl + fileName : fullUrl + "/" + fileName;
    }
}
