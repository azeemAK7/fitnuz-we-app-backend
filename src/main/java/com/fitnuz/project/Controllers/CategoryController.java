package com.fitnuz.project.Controllers;


import com.fitnuz.project.Config.AppConstant;
import com.fitnuz.project.Model.Category;
import com.fitnuz.project.Payload.DTO.CategoryDTO;
import com.fitnuz.project.Payload.Response.CategoryResponse;
import com.fitnuz.project.Service.Definations.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/")
@RestController
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @GetMapping("public/categories")
    public ResponseEntity<CategoryResponse>  getCategories(@RequestParam (name = "pageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer pageNumber,
                                                           @RequestParam (name = "pageSize",defaultValue = AppConstant.PAGE_SIZE,required = false) Integer pageSize,
                                                           @RequestParam (name = "sortBy",defaultValue = AppConstant.SORT_CATEGORY_BY,required = false) String sortBy,
                                                           @RequestParam (name = "sortOrderDir",defaultValue = AppConstant.SORT_ORDER_DIR,required = false) String sortOrderDir){
            CategoryResponse categories = categoryService.getAllCategories(pageNumber,pageSize,sortBy,sortOrderDir);
            return new ResponseEntity<>(categories,HttpStatus.OK);
    }

    @GetMapping("admin/categories")
    public ResponseEntity<CategoryResponse>  getCategoriesForAdmin(@RequestParam (name = "pageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer pageNumber,
                                                                   @RequestParam (name = "pageSize",defaultValue = AppConstant.PAGE_SIZE,required = false) Integer pageSize,
                                                                   @RequestParam (name = "sortBy",defaultValue = AppConstant.SORT_CATEGORY_BY,required = false) String sortBy,
                                                                   @RequestParam (name = "sortOrderDir",defaultValue = AppConstant.SORT_ORDER_DIR,required = false) String sortOrderDir){
        CategoryResponse categories = categoryService.getAllCategoriesForAdmin(pageNumber,pageSize,sortBy,sortOrderDir);
        return new ResponseEntity<>(categories,HttpStatus.OK);
    }

    @GetMapping("public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO>  getCategory(@PathVariable Long categoryId){
        CategoryDTO category = categoryService.getCategory(categoryId);
        return new ResponseEntity<>(category,HttpStatus.OK);
    }


    @PostMapping("admin/categories")
    public ResponseEntity<CategoryDTO> craeteCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO savedCategoryDTO =  categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO,HttpStatus.CREATED);
    }

    @PutMapping("admin/categories/{categoryId}")
    public ResponseEntity<String> updateCategory(@Valid @RequestBody Category category,@PathVariable Long categoryId){
            Category updatedCategory = categoryService.updateCategory(category,categoryId);
            return new ResponseEntity<String>("category with categoryId : " + updatedCategory.getCategoryId() +" updated",HttpStatus.OK);
        }

    @DeleteMapping("admin/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        String message = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

}
