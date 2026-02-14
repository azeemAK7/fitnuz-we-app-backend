package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Exception.CustomException.DuplicateResourceFoundException;
import com.fitnuz.project.Exception.CustomException.GeneralAPIException;
import com.fitnuz.project.Exception.CustomException.ResourceNotFoundException;
import com.fitnuz.project.Model.Cart;
import com.fitnuz.project.Model.CartItem;
import com.fitnuz.project.Model.Category;
import com.fitnuz.project.Model.Product;
import com.fitnuz.project.Model.ProductVariant;
import com.fitnuz.project.Payload.DTO.CartDto;
import com.fitnuz.project.Payload.DTO.ProductDto;
import com.fitnuz.project.Payload.DTO.ProductVariantDto;
import com.fitnuz.project.Payload.Response.ProductResponse;
import com.fitnuz.project.Repository.CartItemRepository;
import com.fitnuz.project.Repository.CartRepository;
import com.fitnuz.project.Repository.CategoryRepository;
import com.fitnuz.project.Repository.ProductRepository;
import com.fitnuz.project.Repository.ProductVariantRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    ProductVariantRepository productVariantRepository;

    @Autowired
    CartService cartService;

    @Autowired
    @Qualifier("cloudinaryFileService")
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

        Product product = new Product();
        product.setProductName(productDto.getProductName());
        product.setProductDescription(productDto.getProductDescription());
        product.setCategory(category);
        product.setImage("default.png");

        // Save product first to get the ID
        Product savedProduct = productRepository.save(product);

        // Create variants from DTO
        if (productDto.getVariants() != null && !productDto.getVariants().isEmpty()) {
            List<ProductVariant> variants = new ArrayList<>();
            for (ProductVariantDto variantDto : productDto.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setWeightLabel(variantDto.getWeightLabel());
                variant.setWeightInGrams(variantDto.getWeightInGrams());
                variant.setPrice(variantDto.getPrice());
                variant.setDiscount(variantDto.getDiscount() != null ? variantDto.getDiscount() : 0.0);
                Double specialPrice = variantDto.getPrice() - ((variantDto.getDiscount() != null ? variantDto.getDiscount() : 0.0) * 0.01 * variantDto.getPrice());
                variant.setSpecialPrice(specialPrice);
                variant.setStock(variantDto.getStock());
                variants.add(variant);
            }
            productVariantRepository.saveAll(variants);
            savedProduct.setVariants(variants);

            // Set product-level fields from first variant for backward compat
            ProductVariant firstVariant = variants.get(0);
            savedProduct.setProductPrice(firstVariant.getPrice());
            savedProduct.setSpecialPrice(firstVariant.getSpecialPrice());
            savedProduct.setDiscount(firstVariant.getDiscount());
            savedProduct.setProductStock(variants.stream().mapToInt(ProductVariant::getStock).sum());
        } else {
            // Fallback: use product-level fields if no variants provided
            Double specialPrice = (productDto.getProductPrice()) - ((productDto.getDiscount() * 0.01) * productDto.getProductPrice());
            savedProduct.setProductPrice(productDto.getProductPrice());
            savedProduct.setSpecialPrice(specialPrice);
            savedProduct.setDiscount(productDto.getDiscount());
            savedProduct.setProductStock(productDto.getProductStock());
        }

        savedProduct = productRepository.save(savedProduct);
        ProductDto savedProductDto = mapProductToDto(savedProduct);
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
                    ProductDto dto = mapProductToDto(product);
                    dto.setImage(product.getImage());
                    return dto;
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
                .map(this::mapProductToDto)
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
                .map(this::mapProductToDto)
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
        product.setProductDescription(productDto.getProductDescription());

        // Update variants
        if (productDto.getVariants() != null && !productDto.getVariants().isEmpty()) {
            // Clear old variants (orphanRemoval will delete them)
            product.getVariants().clear();
            productRepository.flush();

            List<ProductVariant> newVariants = new ArrayList<>();
            for (ProductVariantDto variantDto : productDto.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setWeightLabel(variantDto.getWeightLabel());
                variant.setWeightInGrams(variantDto.getWeightInGrams());
                variant.setPrice(variantDto.getPrice());
                variant.setDiscount(variantDto.getDiscount() != null ? variantDto.getDiscount() : 0.0);
                Double specialPrice = variantDto.getPrice() - ((variantDto.getDiscount() != null ? variantDto.getDiscount() : 0.0) * 0.01 * variantDto.getPrice());
                variant.setSpecialPrice(specialPrice);
                variant.setStock(variantDto.getStock());
                newVariants.add(variant);
            }
            product.getVariants().addAll(newVariants);

            // Set product-level fields from first variant
            ProductVariant firstVariant = newVariants.get(0);
            product.setProductPrice(firstVariant.getPrice());
            product.setSpecialPrice(firstVariant.getSpecialPrice());
            product.setDiscount(firstVariant.getDiscount());
            product.setProductStock(newVariants.stream().mapToInt(ProductVariant::getStock).sum());
        } else {
            product.setDiscount(productDto.getDiscount());
            product.setProductPrice(productDto.getProductPrice());
            product.setProductStock(productDto.getProductStock());
            Double specialPrice = (productDto.getProductPrice()) -  ((productDto.getDiscount() * 0.01 ) * productDto.getProductPrice());
            product.setSpecialPrice(specialPrice);
        }

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


        ProductDto updatedProductDto = mapProductToDto(savedProduct);
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
        return mapProductToDto(product);
    }

    @Override
    public ProductDto updateProductImage(MultipartFile image, Long productId) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","ProductId",productId));
        String fileName = fileService.creteFileName(image,path);

        product.setImage(fileName);
        productRepository.save(product);
        ProductDto productDto = mapProductToDto(product);
        productDto.setProductCategory(product.getCategory().getCategoryName());

        return productDto;
    }

    @Override
    public ProductResponse getAllProductsForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortOrderDir) {
        Sort sort = sortOrderDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> productsPage = productRepository.findAll(pageDetails);
        List<Product> products = productsPage.getContent();

        List<ProductDto> productDtos = products.stream()
                .map(product -> {
                    ProductDto dto = mapProductToDto(product);
                    dto.setImage(constructImageUrl(product.getImage()));
                    return dto;
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

    private ProductDto mapProductToDto(Product product) {
        ProductDto dto = modelMapper.map(product, ProductDto.class);
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            List<ProductVariantDto> variantDtos = product.getVariants().stream()
                    .map(v -> modelMapper.map(v, ProductVariantDto.class))
                    .toList();
            dto.setVariants(variantDtos);
        }
        return dto;
    }

    public String constructImageUrl(String fileName) {
        String baseUrl = imageUrl.endsWith("/") ? imageUrl : imageUrl + "/";
        String finalPath = path.startsWith("/") ? path.substring(1) : path; // remove leading /
        return baseUrl + finalPath + fileName;
    }


}
