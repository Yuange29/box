package storage.com.box.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import storage.com.box.dto.request.CategoryCreationRequest;
import storage.com.box.dto.response.CategoryResponse;
import storage.com.box.entity.Category;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.repository.CategoryRepository;
import storage.com.box.service.CategoryService;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@TestPropertySource("/test.properties")
@AutoConfigureMockMvc
public class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CategoryService categoryService;
    @MockitoBean
    CategoryRepository categoryRepository;

    Category category;
    CategoryCreationRequest request;
    CategoryResponse response;

    @BeforeEach
    void initData() {
        request = CategoryCreationRequest.builder()
                .categoryName("product")
                .categoryDescription("product test")
                .build();

        response = CategoryResponse.builder()
                .categoryName("product")
                .categoryDescription("product test")
                .build();

        category = Category.builder()
                .categoryId("test_product_id")
                .categoryName("product")
                .categoryDescription("product test")
                .userId("test_user_id")
                .build();
    }

    @Test
    void createCategory_validRequest_success() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        String content =  mapper.writeValueAsString(request);
        when(categoryService.createCategory(request)).thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/category")
                        .with(jwt().authorities(new SimpleGrantedAuthority("CREATE")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }

    @Test
    void deleteCategory_validRequest_success() throws Exception{

        when(categoryRepository.existsById(any())).thenReturn(true);

        doNothing().when(categoryService).deleteCategory(any());

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/category/{id}", "test_product_id")
                        .with(jwt().authorities(new SimpleGrantedAuthority("DELETE")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }

    @Test
    void deleteCategory_invalidRequest_fail() throws Exception{

        doThrow(new AppException(ErrorCode.CATEGORY_NOT_EXIST))
                .when(categoryService)
                .deleteCategory(any());

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/category/{id}", "test_product_id")
                        .with(jwt().authorities(new SimpleGrantedAuthority("DELETE")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1010));
    }
}
