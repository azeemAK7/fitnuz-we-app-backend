package com.fitnuz.project.Repository;

import com.fitnuz.project.Model.Cart;
import com.fitnuz.project.Model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    Category findByCategoryName(String categoryName);
}
