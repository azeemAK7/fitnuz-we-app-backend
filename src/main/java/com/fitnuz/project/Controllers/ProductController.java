package com.fitnuz.project.Controllers;


import com.fitnuz.project.Config.AppConstant;
import com.fitnuz.project.Payload.DTO.ProductDto;
import com.fitnuz.project.Payload.Response.ProductResponse;
import com.fitnuz.project.Service.Definations.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(@RequestParam (name = "pageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer pageNumber,
                                                          @RequestParam (name = "keyword",required = false) String keyword,
                                                          @RequestParam (name = "category",required = false) String category,
                                                          @RequestParam (name = "pageSize",defaultValue = AppConstant.PAGE_SIZE,required = false) Integer pageSize,
                                                          @RequestParam (name = "sortBy",defaultValue = AppConstant.SORT_PRODUCT_BY,required = false) String sortBy,
                                                          @RequestParam (name = "sortOrderDir",defaultValue = AppConstant.SORT_ORDER_DIR,required = false) String sortOrderDir){
        ProductResponse productResponse = productService.getAllProducts(pageNumber,pageSize,sortBy,sortOrderDir,keyword,category);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable Long categoryId,
                                                                 @RequestParam (name = "pageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer pageNumber,
                                                                 @RequestParam (name = "pageSize",defaultValue = AppConstant.PAGE_SIZE,required = false) Integer pageSize,
                                                                 @RequestParam (name = "sortBy",defaultValue = AppConstant.SORT_PRODUCT_BY,required = false) String sortBy,
                                                                 @RequestParam (name = "sortOrderDir",defaultValue = AppConstant.SORT_ORDER_DIR,required = false) String sortOrderDir){
        ProductResponse productResponse = productService.getProductsByCategory(categoryId,pageNumber,pageSize,sortBy,sortOrderDir);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(@PathVariable String keyword,
                                                                @RequestParam (name = "pageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer pageNumber,
                                                                @RequestParam (name = "pageSize",defaultValue = AppConstant.PAGE_SIZE,required = false) Integer pageSize,
                                                                @RequestParam (name = "sortBy",defaultValue = AppConstant.SORT_PRODUCT_BY,required = false) String sortBy,
                                                                @RequestParam (name = "sortOrderDir",defaultValue = AppConstant.SORT_ORDER_DIR,required = false) String sortOrderDir){
        ProductResponse productResponse = productService.getProductsByKeyword(keyword,pageNumber,pageSize,sortBy,sortOrderDir);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    @PostMapping("/admin/categories/{categoryId}/products")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto,
                                                    @PathVariable Long categoryId
    ){
        ProductDto savedProductDto = productService.createProduct(productDto,categoryId);
        return new ResponseEntity<>(savedProductDto,HttpStatus.CREATED);
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDto> updateProduct(@Valid @RequestBody ProductDto productDto,@PathVariable Long productId){
        ProductDto updatedProductDto = productService.updateProduct(productDto,productId);
        return new ResponseEntity<>(updatedProductDto,HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}/image")
    public ResponseEntity<ProductDto> updateProductImage(@RequestParam(name = "image") MultipartFile image, @PathVariable Long productId) throws IOException {
        ProductDto updatedProductDto = productService.updateProductImage(image,productId);
        return new ResponseEntity<>(updatedProductDto,HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDto> deleteProduct(@PathVariable Long productId){
        ProductDto updatedProductDto = productService.deleteProduct(productId);
        return new ResponseEntity<>(updatedProductDto,HttpStatus.OK);
    }




}
