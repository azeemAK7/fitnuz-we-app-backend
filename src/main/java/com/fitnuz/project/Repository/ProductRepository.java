package com.fitnuz.project.Repository;

import com.fitnuz.project.Model.Category;
import com.fitnuz.project.Model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {
    Product findByProductName(String productName);

//    Page<Product> findByCategoryOrderByProductPriceAsc(Category category, Pageable pageDetails);

    Page<Product> findByProductNameLikeIgnoreCase(String keyword,Pageable pageDetails);

    Page<Product> findByCategory(Category category, Pageable pageDetails);
}
