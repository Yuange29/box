package storage.com.box.service;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import storage.com.box.dto.request.CategoryCreationRequest;
import storage.com.box.dto.response.CategoryResponse;
import storage.com.box.dto.response.UserResponse;
import storage.com.box.entity.Category;
import storage.com.box.entity.User;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.repository.CategoryRepository;
import storage.com.box.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@TestPropertySource("/test.properties")
public class CategoryServiceTest {

    @MockitoBean
    CategoryRepository categoryRepository;
    @MockitoBean
    UserRepository userRepository;

    @Autowired
    CategoryService categoryService;
    @Autowired
    UserService userService;

    Category category;
    CategoryCreationRequest request;
    CategoryResponse response;
    UserResponse userResponse;
    User user;

    @BeforeEach
    void initData() {

        request = CategoryCreationRequest.builder()
                .categoryName("food")
                .categoryDescription("food")
                .build();

        response = CategoryResponse.builder()
                .categoryName("food")
                .categoryDescription("food")
                .build();

        category = Category.builder()
                .categoryId("re2sdw2dfc3as1gd4sd1")
                .categoryName("food")
                .categoryDescription("food")
                .userId("s2mds2dfh62xcb5gh6")
                .build();

        userResponse = UserResponse.builder()
                .userId("s2mds2dfh62xcb5gh6")
                .userName("testApi")
                .password("test001")
                .email("test@gmail.com")
                .roles(null)
                .build();

        user = User.builder()
                .userId("s2mds2dfh62xcb5gh6")
                .userName("testApi")
                .password("test001")
                .email("test@gmail.com")
                .roles(null)
                .build();

    }

    @Test
    @WithMockUser(roles = {"CREATE"})
    void createCategory_validRequest_success() {
        when(categoryRepository.save(ArgumentMatchers.any()))
                .thenReturn(category);
        when(categoryRepository.existsByCategoryName(ArgumentMatchers.any()))
                .thenReturn(false);
        when(userRepository.findByUserName(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(user));

        var response = categoryService.createCategory(request);

        Assertions.assertThat(response.getCategoryName()).isEqualTo("food");
    }

    @Test
    @WithMockUser(roles = {"CREATE"})
    void createCategory_invalidRequest_fail() {

        when(categoryRepository.existsByCategoryName(ArgumentMatchers.any()))
                .thenReturn(true);

        var exception = org.junit.jupiter.api.Assertions
                .assertThrows(AppException.class, () -> categoryService.createCategory(request));

        Assertions.assertThat(exception.getMessage()).isEqualTo("category already exists");
    }

    @Test
    @WithMockUser(roles = "GET")
    void getAllCategory_validRequest_success() {
        when(userRepository.findByUserName(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(user));
        when((categoryRepository.getCategoriesByUserId(ArgumentMatchers.anyString())))
                .thenReturn(List.of(category));

        var response = categoryService.getUserCategory();

        Assertions.assertThat(response).isEqualTo(List.of(category));
    }

    @Test
    @WithMockUser(roles = "DELETE")
    void deleteCategory_validRequest_success() {
        when(categoryRepository.findById(ArgumentMatchers.any()))
                .thenReturn(Optional.of(category));

        doNothing().when(categoryRepository).deleteById(ArgumentMatchers.any());

        categoryRepository.deleteById("re2sdw2dfc3as1gd4sd1");
        log.info("chay het delete");
    }

    @Test
    @WithMockUser(roles = "DELETE")
    void deleteCategory_invalidRequest_fail() {
        when((categoryRepository.findById(ArgumentMatchers.anyString())))
                .thenReturn(Optional.empty());

        var exception = org.junit.jupiter.api.Assertions
                .assertThrows(AppException.class,() ->
                        categoryService.deleteCategory("da1sd4sd2fd"));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("category not found");
    }
}
