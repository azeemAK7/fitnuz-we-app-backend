package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Exception.CustomException.DuplicateResourceFoundException;
import com.fitnuz.project.Exception.CustomException.GeneralAPIException;
import com.fitnuz.project.Exception.CustomException.ResourceNotFoundException;
import com.fitnuz.project.Model.Category;
import com.fitnuz.project.Payload.DTO.CategoryDTO;
import com.fitnuz.project.Payload.Response.CategoryResponse;
import com.fitnuz.project.Repository.CategoryRepository;
import com.fitnuz.project.Service.Definations.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CategoryServiceImp implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;


    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize,String sortBy,String sortOrderDir) {
        Sort sortByAndOrderType = sortOrderDir.equalsIgnoreCase("asc") ?Sort.by(sortBy).ascending(): Sort.by(sortBy).descending();
        Pageable pageDetail = PageRequest.of(pageNumber,pageSize,sortByAndOrderType);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetail);
        List<Category> categories = categoryPage.getContent();
        if(categories.isEmpty()){
            throw new GeneralAPIException("Category List Is Empty At The Moment");
        }
        List<CategoryDTO> categoryDTO = categories.stream()
                .map(category -> modelMapper.map(category,CategoryDTO.class))
                .toList();
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTO);
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDTO getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category","categoryId",categoryId));
        return modelMapper.map(category,CategoryDTO.class);
    }

    @Override
    public CategoryResponse getAllCategoriesForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortOrderDir) {
        Sort sortByAndOrderType = sortOrderDir.equalsIgnoreCase("asc") ?Sort.by(sortBy).ascending(): Sort.by(sortBy).descending();
        Pageable pageDetail = PageRequest.of(pageNumber,pageSize,sortByAndOrderType);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetail);
        List<Category> categories = categoryPage.getContent();
//        if(categories.isEmpty()){
//            throw new GeneralAPIException("Category List Is Empty At The Moment");
//        }
        List<CategoryDTO> categoryDTO = categories.stream()
                .map(category -> modelMapper.map(category,CategoryDTO.class))
                .toList();
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTO);
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO,Category.class);
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(savedCategory != null){
            throw new DuplicateResourceFoundException("Category with category name : " + category.getCategoryName()+" already exists");
        }
        Category dbCategory =  categoryRepository.save(category);
        return modelMapper.map(dbCategory,CategoryDTO.class);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category","categoryId",categoryId));
        categoryRepository.delete(category);
        return "category with categoryId : " + categoryId + "  is deleted";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Category duplicateCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(duplicateCategory != null && !duplicateCategory.getCategoryId().equals( categoryId)){
            throw new DuplicateResourceFoundException("Category with category name : " + category.getCategoryName()+" already exists");
        }
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() ->  new ResourceNotFoundException("category","categoryId",categoryId));
        category.setCategoryId(categoryId);
        savedCategory = categoryRepository.save(category);
        return savedCategory;
    }

}
