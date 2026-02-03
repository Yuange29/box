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
import storage.com.box.dto.request.FeeCreationRequest;
import storage.com.box.dto.request.FeeUpdateRequest;
import storage.com.box.dto.response.FeeCreationResponse;
import storage.com.box.entity.Fee;
import storage.com.box.entity.User;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.repository.FeeRepository;
import storage.com.box.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@TestPropertySource("/test.properties")
public class FeeServiceTest {

    @MockitoBean
    FeeRepository feeRepository;
    @MockitoBean
    UserRepository userRepository;

    @Autowired
    FeeService feeService;

    Fee fee;
    User user;
    FeeCreationRequest request;
    FeeUpdateRequest updateRequest;
    FeeCreationResponse response;

    @BeforeEach
    void initData() {
        request = FeeCreationRequest.builder()
                .feeName("test product")
                .feePrice(120000)
                .feeDescription("test data")
                .date(new Date())
                .categoryName("Test")
                .build();

        updateRequest = FeeUpdateRequest.builder()
                .feeName("product test")
                .feePrice(12000)
                .feeDescription("test data")
                .categoryName("Test")
                .build();

        response = FeeCreationResponse.builder()
                .feeName("test product")
                .feePrice(120000)
                .feeDescription("test data")
                .date(new Date())
                .categoryName("Test")
                .build();

        fee = Fee.builder()
                .feeId("12sada2sad3asd43sAds2s")
                .feeName("test product")
                .feePrice(120000)
                .feeDescription("test data")
                .date(new Date())
                .categoryName("Test")
                .userId("sd2ssa3ads1sdfA11sds")
                .build();

        user = User.builder()
                .userId("sd2ssa3ads1sdfA11sds")
                .userName("User test")
                .password("userTest")
                .email("test@gmail.com")
                .roles(null)
                .build();
    }

    @Test
    @WithMockUser(roles = "CREATE")
    void createFee_validRequest_success() {
        when(userRepository.findByUserName(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(user));
        when(feeRepository.save(ArgumentMatchers.any())).thenReturn(fee);

        var response = feeService.createFee(request);

        assertThat(response.getFeeName()).isEqualTo(request.getFeeName());
    }

    @Test
    @WithMockUser(roles = "UPDATE", username = "User test")
    void updateFee_validRequest_success() {
        when(feeRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(fee));

        var response = feeService.updateFee("12sada2sad3asd43sAds2s", updateRequest);

        assertThat(response.getFeeName()).isEqualTo(updateRequest.getFeeName());
    }

    @Test
    @WithMockUser(roles = "UPDATE", username = "User test")
    void updateFee_invalidRequest_fail() {
        when(feeRepository.findById(ArgumentMatchers.any()))
                .thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class, () ->
                        feeService.updateFee("12sada2sad3asd43sAds2s", updateRequest));

        assertThat(exception.getMessage()).isEqualTo("fee not found");
    }

    @Test
    @WithMockUser(roles = "DELETE")
    void deleteFee_validRequest_success() {
        when(feeRepository.findById(ArgumentMatchers.any()))
                .thenReturn(Optional.of(fee));

        feeService.deleteFee("12sada2sad3asd43sAds2s");
    }

    @Test
    @WithMockUser(roles = "DELETE")
    void deleteFee_invalidRequest_fail() {

        doThrow(AppException.class).when(feeRepository).deleteById(ArgumentMatchers.any());

        var exception = assertThrows(AppException.class, () ->
                        feeService.deleteFee("12sada2sad3asd43sAds2s"));

        assertThat(exception.getMessage()).isEqualTo("fee not found");

    }

    @Test
    @WithMockUser(roles = "GET")
    void getFee_validRequest_success() {
        when(feeRepository.findById(ArgumentMatchers.any()))
                .thenReturn(Optional.of(fee));

        var response = feeService.getFee("12sada2sad3asd43sAds2s");

        assertThat(response.getFeeName()).isEqualTo(request.getFeeName());
    }

    @Test
    @WithMockUser(roles = "GET")
    void getFee_invalidRequest_fail() {
        when(feeRepository.findById(ArgumentMatchers.any()))
                .thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class, () ->
                        feeService.getFee("12sada2sad3asd43sAds2s"));

        assertThat(exception.getMessage()).isEqualTo("fee not found");
    }

    @Test
    @WithMockUser(roles = "GET")
    void getUserFee_validRequest_success() {
       when(userRepository.findByUserName(ArgumentMatchers.anyString()))
               .thenReturn(Optional.of(user));

       var response = feeService.getUserFee();

       assertThatList(response).hasSize(0);

    }

    @Test
    @WithMockUser(roles = "GET")
    void getFeeList_invalidRequest_fail() {
        when(userRepository.findByUserName(anyString()))
                .thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class, () -> feeService.getUserFee());

        assertThat(exception.getMessage()).isEqualTo("user not exist");
    }
}
