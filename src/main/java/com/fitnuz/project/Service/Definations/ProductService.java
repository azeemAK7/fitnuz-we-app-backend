package com.fitnuz.project.Service.Definations;


import com.fitnuz.project.Payload.DTO.ProductDto;
import com.fitnuz.project.Payload.Response.ProductResponse;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductDto createProduct(ProductDto productDto, Long categoryId);

    ProductResponse getAllProducts(Integer pageNumber,Integer pageSize,String sortBy,String sortOrderDir,String keyword,String category);

    ProductResponse getProductsByCategory(Long categoryId,Integer pageNumber,Integer pageSize,String sortBy,String sortOrderDir);

    ProductResponse getProductsByKeyword(String keyword,Integer pageNumber,Integer pageSize,String sortBy,String sortOrderDir);

    ProductDto updateProduct(ProductDto productDto, Long productId);

    @Transactional
    ProductDto deleteProduct(Long productId);

    ProductDto updateProductImage(MultipartFile image, Long productId) throws IOException;

    ProductResponse getAllProductsForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortOrderDir);
}
