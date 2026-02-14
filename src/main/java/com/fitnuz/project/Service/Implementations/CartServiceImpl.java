package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Exception.CustomException.DuplicateResourceFoundException;
import com.fitnuz.project.Exception.CustomException.GeneralAPIException;
import com.fitnuz.project.Exception.CustomException.ResourceNotFoundException;
import com.fitnuz.project.Model.Cart;
import com.fitnuz.project.Model.CartItem;
import com.fitnuz.project.Model.Product;
import com.fitnuz.project.Model.ProductVariant;
import com.fitnuz.project.Model.User;
import com.fitnuz.project.Payload.DTO.CartDto;
import com.fitnuz.project.Payload.DTO.CartItemDto;
import com.fitnuz.project.Payload.DTO.ProductDto;
import com.fitnuz.project.Payload.DTO.ProductVariantDto;
import com.fitnuz.project.Repository.CartItemRepository;
import com.fitnuz.project.Repository.CartRepository;
import com.fitnuz.project.Repository.ProductRepository;
import com.fitnuz.project.Repository.ProductVariantRepository;
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
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Value("${project.image}")
    private String path;

    @Value("${spring.app.backend}")
    private String imageUrl;

    @Transactional
    @Override
    public CartDto addProduct(Long variantId, Integer quantity) {
        Cart cart = createCart();
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant","variantId",variantId));
        Product product = variant.getProduct();

        CartItem cartItem = cartItemRepository.findCartItemByVariantIdAndCartId(
                cart.getCartId(),
                variantId
        );

        if(cartItem != null){
            throw new DuplicateResourceFoundException("Product variant " + product.getProductName() + " (" + variant.getWeightLabel() + ") already exists in cart");
        }
        if(variant.getStock() == 0){
            throw new GeneralAPIException("Product " + product.getProductName() + " (" + variant.getWeightLabel() + ") is out of stock");
        }
        if(variant.getStock() < quantity){
            throw new GeneralAPIException("Add quantity below " + variant.getStock());
        }
        cartItem = new CartItem();
        cartItem.setQuantity(quantity);
        cartItem.setProductPrice(variant.getSpecialPrice());
        cartItem.setProductDiscount(variant.getDiscount());
        cartItem.setProduct(product);
        cartItem.setProductVariant(variant);
        cartItem.setCart(cart);
        cartItemRepository.save(cartItem);

        cart.setTotalPrice(cart.getTotalPrice() + (variant.getSpecialPrice() * quantity));
        cart.getCartItems().add(cartItem);
        cart = cartRepository.save(cart);

        CartDto cartDto = modelMapper.map(cart,CartDto.class);
        cartDto.setUserName(cart.getUser().getUserName());
        List<CartItem> cartItems = cart.getCartItems();
        List<ProductDto> productDtos = cartItems.stream().map(item->{
            ProductDto productDto = modelMapper.map(item.getProduct(),ProductDto.class);
            productDto.setProductStock(quantity);
            if (item.getProductVariant() != null) {
                mapVariantInfoToProductDto(productDto, item);
            }
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
                        productDto.setProductStock(cartItem.getQuantity());
                        if (cartItem.getProductVariant() != null) {
                            mapVariantInfoToProductDto(productDto, cartItem);
                        }
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
        CartDto cartDto = modelMapper.map(cart,CartDto.class);
        List<ProductDto> productDtos = cart.getCartItems().stream()
                .map(cartItem -> {
                    ProductDto productDto = modelMapper.map(cartItem.getProduct(), ProductDto.class);
                    productDto.setCartQuantity(cartItem.getQuantity());
                    if (cartItem.getProductVariant() != null) {
                        mapVariantInfoToProductDto(productDto, cartItem);
                    }
                    return productDto;
                })
                .toList();
        cartDto.setProducts(productDtos);
        cartDto.setUserName(cart.getUser().getUserName());
        return cartDto;
    }

    @Transactional
    @Override
    public CartDto updateProductStock(Long variantId, String operation) {

        int value =  operation.equalsIgnoreCase("increase") ? 1:-1;

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant","variantId",variantId));

        Cart cart = cartRepository.findCartByEmail(authUtil.getUserEmail());
        if(cart == null){
            throw new GeneralAPIException("Cart Not Found");
        }

        CartItem cartItem = cartItemRepository.findCartItemByVariantIdAndCartId(cart.getCartId(), variantId);
        if(cartItem == null){
            throw new ResourceNotFoundException("CartItem","CartId",cart.getCartId());
        }

        if(variant.getStock() == 0){
            throw new GeneralAPIException("Product " + variant.getProduct().getProductName() + " (" + variant.getWeightLabel() + ") is out of stock");
        }

        Integer quantity = cartItem.getQuantity() + value;

        if (variant.getStock() < quantity) {
            throw new GeneralAPIException("Add quantity below or equal to " + variant.getStock());
        }

        if (quantity <= 0) {
            cart.getCartItems().remove(cartItem);
        } else {
            cartItem.setProductPrice(variant.getSpecialPrice());
            cartItem.setProductDiscount(variant.getDiscount());
            cartItem.setQuantity(quantity);

            if (!cart.getCartItems().contains(cartItem)) {
                cart.getCartItems().add(cartItem);
            }
        }

        cart.setTotalPrice(cartUtil.calculateTotalPrice(cart));
        cartRepository.save(cart);

        CartDto cartDto = modelMapper.map(cart,CartDto.class);
        List<ProductDto> productDtos = cart.getCartItems().stream()
                .map(item -> {
                    ProductDto productDto = modelMapper.map(item.getProduct(), ProductDto.class);
                    productDto.setProductStock(item.getQuantity());
                    if (item.getProductVariant() != null) {
                        mapVariantInfoToProductDto(productDto, item);
                    }
                    return productDto;
                })
                .toList();
        cartDto.setProducts(productDtos);
        return cartDto;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long variantId, Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("cart","cartId",cartId));

        CartItem cartItem = cartItemRepository.findCartItemByVariantIdAndCartId(cartId, variantId);
        if(cartItem == null){
           throw  new ResourceNotFoundException("cartItem","cartId",cartId);
        }

        Product product = cartItem.getProduct();
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice()*cartItem.getQuantity()));
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        String weightInfo = cartItem.getProductVariant() != null ? " (" + cartItem.getProductVariant().getWeightLabel() + ")" : "";
        return "product with product name : " + product.getProductName() + weightInfo + " removed from cart";
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

        // If cart item has a variant, use variant pricing
        if (cartItem.getProductVariant() != null) {
            ProductVariant variant = cartItem.getProductVariant();
            cartItem.setProductPrice(variant.getSpecialPrice());
            cartItem.setProductDiscount(variant.getDiscount());
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setProductDiscount(product.getDiscount());
        }

        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice()*cartItem.getQuantity()));
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
            Long variantId = cartItemDto.getVariantId();
            Long productId = cartItemDto.getProductId();
            Integer quantity = cartItemDto.getProductStock();

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setQuantity(quantity);

            if (variantId != null) {
                ProductVariant variant = productVariantRepository.findById(variantId)
                        .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "variantId", variantId));
                cartItem.setProduct(variant.getProduct());
                cartItem.setProductVariant(variant);
                cartItem.setProductPrice(variant.getSpecialPrice());
                cartItem.setProductDiscount(variant.getDiscount());
                totalPrice += variant.getSpecialPrice() * quantity;
            } else {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
                cartItem.setProduct(product);
                cartItem.setProductPrice(product.getSpecialPrice());
                cartItem.setProductDiscount(product.getDiscount());
                totalPrice += product.getSpecialPrice() * quantity;
            }

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

    private void mapVariantInfoToProductDto(ProductDto productDto, CartItem cartItem) {
        ProductVariant variant = cartItem.getProductVariant();
        if (variant != null) {
            // Set variant identity fields
            productDto.setVariantId(variant.getVariantId());
            productDto.setWeightLabel(variant.getWeightLabel());

            // Override product-level pricing with variant pricing for cart display
            productDto.setProductPrice(variant.getPrice());
            productDto.setSpecialPrice(variant.getSpecialPrice());
            productDto.setDiscount(variant.getDiscount());
            productDto.setProductStock(variant.getStock());
        }
    }

    public String constructImageUrl(String fileName){
        String fullUrl = imageUrl + path;
        if(fileName.equals("default.png")){
            return fullUrl + "a06f0845-8a92-4af9-8ee5-f655012d7fff.png";
        }
        return path.endsWith("/") ? fullUrl + fileName : fullUrl + "/" + fileName;
    }
}
