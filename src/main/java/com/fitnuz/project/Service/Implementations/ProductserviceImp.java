package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Exception.CustomException.DuplicateResourceFoundException;
import com.fitnuz.project.Exception.CustomException.GeneralAPIException;
import com.fitnuz.project.Exception.CustomException.ResourceNotFoundException;
import com.fitnuz.project.Model.Cart;
import com.fitnuz.project.Model.CartItem;
import com.fitnuz.project.Model.Category;
import com.fitnuz.project.Model.Product;
import com.fitnuz.project.Payload.DTO.CartDto;
import com.fitnuz.project.Payload.DTO.ProductDto;
import com.fitnuz.project.Payload.Response.ProductResponse;
import com.fitnuz.project.Repository.CartItemRepository;
import com.fitnuz.project.Repository.CartRepository;
import com.fitnuz.project.Repository.CategoryRepository;
import com.fitnuz.project.Repository.ProductRepository;
import com.fitnuz.project.Service.Definations.CartService;
import com.fitnuz.project.Service.Definations.FileService;
import com.fitnuz.project.Service.Definations.ProductService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class ProductserviceImp implements ProductService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartService cartService;

    @Autowired
    @Qualifier("localFileService")
    FileService fileService;

    @Autowired
    ModelMapper modelMapper;

    @Value("${project.image}")
    private String path;

    @Value("${spring.app.backend}")
    private String imageUrl;

    @Override
    public ProductDto createProduct(ProductDto productDto, Long categoryId) {
        Category  category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category","categoryid",categoryId));

        Product productFromDB = productRepository.findByProductName(productDto.getProductName());
        if(productFromDB != null){
            throw new DuplicateResourceFoundException("Product with product name : " + productDto.getProductName()+" already exists");
        }

        Product product = modelMapper.map(productDto,Product.class);
        product.setCategory(category);
        product.setImage("default.png");
        Double specialPrice = (product.getProductPrice()) -  ((product.getDiscount() * 0.01 ) * product.getProductPrice());
        product.setSpecialPrice(specialPrice);
        Product savedProductFromDB =  productRepository.save(product);
        ProductDto savedProductDto = modelMapper.map(savedProductFromDB,ProductDto.class);
        savedProductDto.setProductCategory(category.getCategoryName());
        return savedProductDto;
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber,Integer pageSize,String sortBy,String sortOrderDir,String keyword,String category) {
       
        Sort sort = sortOrderDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sort);
        Specification<Product> spec = Specification.where(null);
        if(keyword!= null && !keyword.isEmpty()){
            spec = spec.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")),"%" + keyword.toLowerCase() + "%")
            ));
        }

        if(category!= null && !category.isEmpty()){
            spec = spec.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("category").get("categoryName"),category)
            ));
        }


        Page<Product> productsPage = productRepository.findAll(spec,pageDetails);

        List<Product> products = productsPage.getContent();

        if(products.isEmpty()){
            throw new GeneralAPIException("Product List Is Empty At The Moment");
        }

        List<ProductDto> productDtos = products.stream()
                .map(product -> {
                    ProductDto productDto = modelMapper.map(product,ProductDto.class);
                    productDto.setImage(constructImageUrl(product.getImage()));
                    return productDto;
                })
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDtos);
        productResponse.setPageSize(productsPage.getSize());
        productResponse.setPageNumber(productsPage.getNumber());
        productResponse.setTotalElements(productsPage.getTotalElements());
        productResponse.setTotalPages(productsPage.getTotalPages());
        productResponse.setLastPage(productsPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId,Integer pageNumber,Integer pageSize,String sortBy,String sortOrderDir) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category","categoryid",categoryId));

        Sort sort = sortOrderDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> productsPage = productRepository.findByCategory(category,pageDetails);

        List<Product> products = productsPage.getContent();

        if(products.isEmpty()){
            throw new GeneralAPIException("Product List Is Empty At The Moment");
        }

        List<ProductDto> productDto = products.stream()
                .map(product -> modelMapper.map(product,ProductDto.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDto);
        productResponse.setPageSize(productsPage.getSize());
        productResponse.setPageNumber(productsPage.getNumber());
        productResponse.setTotalElements(productsPage.getTotalElements());
        productResponse.setTotalPages(productsPage.getTotalPages());
        productResponse.setLastPage(productsPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword,Integer pageNumber,Integer pageSize,String sortBy,String sortOrderDir) {
        Sort sort = sortOrderDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> productsPage = productRepository.findByProductNameLikeIgnoreCase('%'+ keyword + '%',pageDetails);

        List<Product> products = productsPage.getContent();
        if(products.isEmpty()){
            throw new ResourceNotFoundException("Product","Keyword",keyword);
        }
        List<ProductDto> productDto = products.stream()
                .map(product -> modelMapper.map(product,ProductDto.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDto);
        productResponse.setPageSize(productsPage.getSize());
        productResponse.setPageNumber(productsPage.getNumber());
        productResponse.setTotalElements(productsPage.getTotalElements());
        productResponse.setTotalPages(productsPage.getTotalPages());
        productResponse.setLastPage(productsPage.isLast());
        return productResponse;
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto, Long productId) {
       Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","ProductId",productId));


        Product productFromDB = productRepository.findByProductName(productDto.getProductName());
        if(productFromDB != null && !productFromDB.getProductId().equals(productId)){
            throw new DuplicateResourceFoundException("Product with product name : " + productDto.getProductName()+" already exists");
        }
        product.setProductName(productDto.getProductName());
        product.setProductDiscription(productDto.getProductDiscription());
        product.setDiscount(productDto.getDiscount());
        product.setProductPrice(productDto.getProductPrice());
        product.setProductQuantity(productDto.getProductQuantity());
        Double specialPrice = (productDto.getProductPrice()) -  ((productDto.getDiscount() * 0.01 ) * productDto.getProductPrice());
        product.setSpecialPrice(specialPrice);
        Product savedProduct =  productRepository.save(product);

        List<Cart> carts = cartRepository.findCartByProductId(product.getProductId());
        List<CartDto> cartDtos = carts.stream().map(cart -> {
            CartDto cartDto = modelMapper.map(cart,CartDto.class);
            List<ProductDto> productDtos = cart.getCartItems().stream()
                    .map(p-> modelMapper.map(p.getProduct(),ProductDto.class)).toList();
            cartDto.setProducts(productDtos);
            return cartDto;
        }).toList();

        cartDtos.forEach(cartDto -> cartService.updateProductInCarts(cartDto.getCartId(),productId));


        ProductDto updatedProductDto = modelMapper.map(savedProduct,ProductDto.class);
        updatedProductDto.setProductCategory(product.getCategory().getCategoryName());

        return updatedProductDto;
    }

    @Transactional
    @Override
    public ProductDto deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","ProductId",productId));

        List<CartItem> cartItems = cartItemRepository.findByProductId(productId);
        for (CartItem item : cartItems) {
            Cart cart = item.getCart();
            cart.setTotalPrice(cart.getTotalPrice() - (item.getProductPrice() * item.getQuantity()));
            cartItemRepository.delete(item);
        }

        productRepository.delete(product);
        return modelMapper.map(product,ProductDto.class);
    }

    @Override
    public ProductDto updateProductImage(MultipartFile image, Long productId) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","ProductId",productId));
        String fileName = fileService.creteFileName(image,path);

        product.setImage(fileName);
        productRepository.save(product);
        ProductDto productDto = modelMapper.map(product,ProductDto.class);
        productDto.setProductCategory(product.getCategory().getCategoryName());

        return productDto;
    }




    public String constructImageUrl(String fileName) {
        String baseUrl = imageUrl.endsWith("/") ? imageUrl : imageUrl + "/";
        String finalPath = path.startsWith("/") ? path.substring(1) : path; // remove leading /
        return baseUrl + finalPath + fileName;
    }


}
