package com.fitnuz.project.Service.Definations;

import com.fitnuz.project.Model.Category;
import com.fitnuz.project.Payload.DTO.CategoryDTO;
import com.fitnuz.project.Payload.Response.CategoryResponse;


public interface CategoryService {

    CategoryResponse getAllCategories(Integer pageNumber,Integer pageSize,String sortBy,String sortOrderDir);
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    String deleteCategory(Long categoryId);

    Category updateCategory(Category category, Long categoryId);

    CategoryDTO getCategory(Long categoryId);
}
