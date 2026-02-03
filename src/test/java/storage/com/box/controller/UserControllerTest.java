package storage.com.box.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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
import storage.com.box.dto.request.UserCreationRequest;
import storage.com.box.dto.request.UserUpdateRequest;
import storage.com.box.dto.response.UserResponse;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.service.UserService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@TestPropertySource("/test.properties")
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    private UserCreationRequest request;
    private UserResponse response, updateResponse;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void init_Data() {
        request = UserCreationRequest.builder()
                .userName("test01")
                .password("password01")
                .email("test01@gmail.com")
                .build();

        updateRequest = UserUpdateRequest.builder()
                .userName("test02")
                .password("password02")
                .email("test02@gmail.com")
                .build();

        response = UserResponse.builder()
                .userId("ssd1d3qw2d3j4hn1hj2")
                .userName("test01")
                .password("password01")
                .email("test01@gmail.com")
                .roles(null)
                .build();

        updateResponse = UserResponse.builder()
                .userId("ssd1d3qw2d3j4hn1hj2")
                .userName("test02")
                .password("password02")
                .email("test02@gmail.com")
                .roles(null)
                .build();
    }

    @Test
    void createUser_validRequest_success() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))

                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value("1000"));
    }

    @Test
    void createUser_invalidRequest_fail() throws Exception {

        request.setUserName("joh");

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(request);

        Mockito.when(userService.createUser(ArgumentMatchers.any())).
                thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1001))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("message")
                        .value("user name must be at least 6 characters")
                );
    }

    @Test
    void updateUser_validRequest_success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(updateRequest);
        Mockito.when(userService.updateUser(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(updateResponse);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/users/{id}", "ssd1d3qw2d3j4hn1hj2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("UPDATE")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value("1000"));
    }

    @Test
    void updateUser_invalidRequest_fail() throws Exception {

        updateRequest.setUserName("iuu");

        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(updateRequest);
        Mockito.when(userService.updateUser(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(updateResponse);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/users/{id}", "ssd1d3qw2d3j4hn1hj2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("UPDATE")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1001));

    }

    @Test
    void deleteUser_validRequest_success() throws Exception {
        doNothing().when(userService).deleteUser(ArgumentMatchers.any());

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/users/{id}", "ssd1d3qw2d3j4hn1hj2").with(jwt()
                .authorities(new SimpleGrantedAuthority("DELETE")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void deleteUser_invalidRequest_fail() throws Exception {

        doThrow(new AppException(ErrorCode.USER_NOT_EXIST))
                .when(userService)
                .deleteUser(ArgumentMatchers.any());

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/users/{id}", "ssd1d3qw2d3j4hn1hj2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("DELETE")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1004));
    }

    @Test
    void getUser_validRequest_success() throws Exception {

        Mockito.when(userService.getUser(ArgumentMatchers.any())).thenReturn(updateResponse);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users/{id}", "ssd1d3qw2d3j4hn1hj2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("GET")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }

    @Test
    void getUser_invalidRequest_fail() throws Exception {
        doThrow(new AppException(ErrorCode.USER_NOT_EXIST))
                .when(userService)
                .getUser(ArgumentMatchers.any());

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users/{id}", "ssd1d3qw2d3j4hn1hj2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("GET")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1004));
    }

    @Test
    void getAllUsers_validRequest_success() throws Exception {

        Mockito.when(userService.findAllUsers()).thenReturn(List.of(response));

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000)
                );
    }

    @Test
    void myInfo_validRequest_success() throws Exception {
        Mockito.when(userService.myInfo()).thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users/getInfo")
                        .with(jwt().authorities(new SimpleGrantedAuthority("GET")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }
    @Test
    void myInfo_invalidRequest_fail() throws Exception {
        doThrow(new AppException(ErrorCode.USER_NOT_EXIST))
                .when(userService)
                .myInfo();

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users/getInfo")
                        .with(jwt().authorities(new SimpleGrantedAuthority("GET")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1004));
    }
}
