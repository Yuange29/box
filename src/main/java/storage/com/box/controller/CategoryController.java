package storage.com.box.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import storage.com.box.dto.request.CategoryCreationRequest;
import storage.com.box.dto.response.ApiResponse;
import storage.com.box.dto.response.CategoryResponse;
import storage.com.box.entity.Category;
import storage.com.box.service.CategoryService;

import java.util.List;


@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/category")
@Builder
public class CategoryController {

    CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<Category>> getUserCategory() {
        return ApiResponse.<List<Category>>builder()
                .result(categoryService.getUserCategory())
                .build();
    }

    @PostMapping
    public ApiResponse<CategoryResponse>  createCategory(@RequestBody CategoryCreationRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .build();
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable String categoryId) {
        categoryService.deleteCategory(categoryId);
        return ApiResponse.<Void>builder()
                .build();
    }

}
