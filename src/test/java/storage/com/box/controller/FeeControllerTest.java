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
import storage.com.box.dto.request.FeeCreationRequest;
import storage.com.box.dto.request.FeeUpdateRequest;
import storage.com.box.dto.response.FeeCreationResponse;
import storage.com.box.entity.Fee;
import storage.com.box.entity.User;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.repository.FeeRepository;
import storage.com.box.repository.UserRepository;
import storage.com.box.service.FeeService;
import tools.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@TestPropertySource("/test.properties")
@AutoConfigureMockMvc
public class FeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FeeService feeService;
    @MockitoBean
    UserRepository userRepository;

    Fee  fee;
    FeeCreationRequest request;
    FeeCreationResponse response;
    FeeUpdateRequest updateRequest;
    User user;


    @BeforeEach
    void init() {
        request = FeeCreationRequest.builder()
                .feeName("test fee")
                .feePrice(10000)
                .feeDescription("test fee bill")
                .date(new Date())
                .categoryName("test")
                .build();

        updateRequest = FeeUpdateRequest.builder()
                .feeName("test bill")
                .feePrice(12000)
                .feeDescription("test fee bill")
                .build();

        response = FeeCreationResponse.builder()
                .feeName("test fee")
                .feePrice(10000)
                .feeDescription("test fee bill")
                .date(new Date())
                .categoryName("test")
                .build();

        fee = Fee.builder()
                .feeId("fee_id_test")
                .feeName("test fee")
                .feePrice(10000)
                .feeDescription("test fee bill")
                .date(new Date())
                .categoryName("test")
                .userId("test_user_id")
                .build();

        user = User.builder()
                .userId("test_user_id")
                .userName("test_user")
                .build();
    }

    @Test
    void createFee_validRequest_success() throws Exception {
        ObjectMapper  mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);

        when(userRepository.findByUserName(any())).thenReturn(Optional.of(user));
        when(feeService.createFee(any())).thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/fee")
                        .with(jwt().authorities(new SimpleGrantedAuthority("CREATE")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));

    }

    @Test
    void updateFee_validRequest_success() throws Exception {
        ObjectMapper  mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(updateRequest);
        when(feeService.updateFee(any(), any())).thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/fee/{id}", "fee_id_test")
                        .with(jwt().authorities(new SimpleGrantedAuthority("UPDATE")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }

    @Test
    void updateFee_invalidRequest_fail() throws Exception {
        ObjectMapper  mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(updateRequest);

        doThrow(new AppException(ErrorCode.FEE_NOT_FOUND))
                .when(feeService)
                .updateFee(any(), any());

        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/fee/{id}", "invalid_Id")
                        .with(jwt().authorities(new SimpleGrantedAuthority("UPDATE")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1009));
    }

    @Test
    void deleteFee_validRequest_success() throws Exception {
        doNothing()
                .when(feeService)
                .deleteFee(any());

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/fee/{id}", "fee_id_test")
                        .with(jwt().authorities(new SimpleGrantedAuthority("DELETE")))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }

    @Test
    void deleteFee_invalidRequest_fail() throws Exception {
        doThrow(new AppException(ErrorCode.FEE_NOT_FOUND))
                .when(feeService).deleteFee(any());

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/fee/{id}", "invalid_Id")
                        .with(jwt().authorities(new SimpleGrantedAuthority("DELETE")))
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1009));
    }

    @Test
    void getFee_validRequest_success() throws Exception {
        when(feeService.getFee(any())).thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/fee/{id}", "fee_id_test")
                        .with(jwt().authorities(new SimpleGrantedAuthority("GET")))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }

    @Test
    void getFee_invalidRequest_fail() throws Exception {
        doThrow(new AppException(ErrorCode.FEE_NOT_FOUND))
                .when(feeService).getFee(any());

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/fee/{id}", "invalid_Id")
                        .with(jwt().authorities(new SimpleGrantedAuthority("GET")))
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1009));
    }

    @Test
    void getFees_validRequest_success() throws Exception {
        when(feeService.getUserFee()).thenReturn(List.of(response));

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/fee/userFee")
                        .with(jwt().authorities(new SimpleGrantedAuthority("GET")))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }

}
