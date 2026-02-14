package com.fitnuz.project.Config;

import com.fitnuz.project.Model.Product;
import com.fitnuz.project.Model.ProductVariant;
import com.fitnuz.project.Repository.ProductRepository;
import com.fitnuz.project.Repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataMigrationRunner implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Override
    public void run(String... args) {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            if (product.getVariants() == null || product.getVariants().isEmpty()) {
                ProductVariant defaultVariant = new ProductVariant();
                defaultVariant.setProduct(product);
                defaultVariant.setWeightLabel("1kg");
                defaultVariant.setWeightInGrams(1000);
                defaultVariant.setPrice(product.getProductPrice() != null ? product.getProductPrice() : 0.0);
                defaultVariant.setDiscount(product.getDiscount() != null ? product.getDiscount() : 0.0);
                defaultVariant.setSpecialPrice(product.getSpecialPrice() != null ? product.getSpecialPrice() : 0.0);
                defaultVariant.setStock(product.getProductStock() != null ? product.getProductStock() : 0);
                productVariantRepository.save(defaultVariant);
            }
        }
    }
}
