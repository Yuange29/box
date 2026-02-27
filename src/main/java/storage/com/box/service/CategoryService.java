package storage.com.box.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import storage.com.box.dto.request.CategoryCreationRequest;
import storage.com.box.dto.response.CategoryResponse;
import storage.com.box.entity.Category;
import storage.com.box.entity.User;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.mapper.CategoryMapper;
import storage.com.box.repository.CategoryRepository;
import storage.com.box.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryService {

    CategoryRepository categoryRepository;
    UserRepository userRepository;
    CategoryMapper categoryMapper;

    @PreAuthorize("hasRole('GET')")
    public List<Category> getUserCategory() {

        var context = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findByUserName(context.getName());

        return categoryRepository.getCategoriesByUserId(user.get().getUserId());
    }

    @PreAuthorize("hasRole('CREATE')")
    public CategoryResponse createCategory(CategoryCreationRequest request) {

        if (categoryRepository.existsByCategoryName(request.getCategoryName()))
            throw new AppException(ErrorCode.CATEGORY_EXISTS);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> user = userRepository.findByUserName(username);

        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .categoryDescription(request.getCategoryDescription())
                .userId(user.get().getUserId())
                .build();

        categoryRepository.save(category);

        return categoryMapper.categoryToCategoryResponse(category);
    }

    @PreAuthorize("hasRole('DELETE')")
    public void deleteCategory(String categoryId) {

        if (!categoryRepository.existsById(categoryId))
            throw new AppException(ErrorCode.CATEGORY_NOT_EXIST);

        categoryRepository.deleteById(categoryId);
    }
}
