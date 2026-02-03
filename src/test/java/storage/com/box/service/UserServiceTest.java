package storage.com.box.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import storage.com.box.dto.request.UserCreationRequest;
import storage.com.box.dto.request.UserUpdateRequest;
import storage.com.box.dto.response.UserCreationResponse;
import storage.com.box.entity.Role;
import storage.com.box.entity.User;
import storage.com.box.exception.AppException;
import storage.com.box.repository.RoleRepository;
import storage.com.box.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestPropertySource("/test.properties")
public class UserServiceTest {

    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    RoleRepository roleRepository;

    @Autowired
    UserService userService;

    UserCreationRequest request;
    UserCreationResponse response;
    UserUpdateRequest update;
    User user;

    @BeforeEach
    public void beforeEach() {

        request = UserCreationRequest.builder()
                .userName("testApi")
                .password("testApi")
                .email("testApi@gmai.com")
                .build();

        update = UserUpdateRequest.builder()
                .userName("ApiTest0")
                .password("testApi")
                .email("ApiTest@mail")
                .build();

        response = UserCreationResponse.builder()
                .userName("testApi")
                .email("testApi@gmai.com")
                .build();

         user = User.builder()
                .userId("sdh1_ssd/sda_1wads1_1wd2")
                .userName("testApi")
                .password("testApi")
                .email("testApi@gmail.com")
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        Mockito.when(userRepository.existsByUserName(ArgumentMatchers.anyString())).thenReturn(false);
        Mockito.when(userRepository.save(ArgumentMatchers.any())).thenReturn(user);

        var response = userService.createUser(request);

        Assertions.assertThat(response.getUserName())
                .isEqualTo("testApi");
    }

    @Test
    void createUser_invalidRequest_fail() {
        Mockito.when(userRepository.existsByUserName(ArgumentMatchers.anyString())).thenReturn(true);

        var exception = org.junit.jupiter.api.Assertions
                .assertThrows(AppException.class, () -> userService.createUser(request));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("user already exist");
    }

    @Test
    @WithMockUser(roles = "UPDATE")
    void updateUser_validRequest_success() {
        Mockito.when(userRepository.save(ArgumentMatchers.any()))
                .thenReturn(user);
        Mockito.when(userRepository.findByUserId(ArgumentMatchers.any()))
                .thenReturn(Optional.ofNullable(user));

        var response = userService.updateUser(user.getUserId(), update);

        Assertions.assertThat(response.getUserName()).isEqualTo("ApiTest0");
    }

    @Test
    @WithMockUser(roles = "UPDATE")
    void updateUser_invalidRequest_fail() {
        Mockito.when(userRepository.save(ArgumentMatchers.any()))
                .thenReturn(null);

        var exception = org.junit.jupiter.api.Assertions.assertThrows(AppException.class, () ->
                        userService.updateUser("qwkJsh2nsiAnb", update));

        Assertions.assertThat(exception.getMessage()).isEqualTo("user not exist");
    }

    @Test
    @WithMockUser(roles = "DELETE")
    void deleteUser_validRequest_success() {
        Mockito.when(userRepository.findByUserId(ArgumentMatchers.any())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getUserId());
    }

    @Test
    @WithMockUser(roles = "DELETE")
    void deleteUser_invalidRequest_fail() {
        Mockito.when(userRepository.findByUserId(ArgumentMatchers.any())).thenReturn(Optional.empty());

        var exception = org.junit.jupiter.api.Assertions
                .assertThrows(AppException.class, () -> userService.deleteUser("djh2wq/oi3edq.w"));

        Assertions.assertThat(exception.getMessage()).isEqualTo("user not exist");
    }

    @Test
    @WithMockUser(roles = "GET", username = "testAPI")
    void getInfo_validRequest_success() {

        Mockito.when(userRepository.findByUserName(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(user));

        var response = userService.myInfo();

        Assertions.assertThat(response.getUserName()).isEqualTo("testApi");
        Assertions.assertThat(response.getUserId()).isEqualTo("sdh1_ssd/sda_1wads1_1wd2");
    }

    @Test
    @WithMockUser(roles = "GET", username = "testAPI")
    void getInfo_invalidRequest_fail() {
        Mockito.when(userRepository.findByUserName(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        var exception = org.junit.jupiter.api.Assertions
                .assertThrows(AppException.class, () -> userService.myInfo());

        Assertions.assertThat(exception.getMessage()).isEqualTo("user not exist");
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "#admin")
    void getAllUsers_validRequest_success() {
        Mockito.when(userRepository.findAll()).thenReturn(List.of(user));

        var response = userService.findAllUsers();

//        chưa define
    }

    @Test
    @WithMockUser(roles = "GET", username = "testAPI")
    void getUser_validRequest_success() {
        Mockito.when(userRepository.findByUserId(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(user));

        var response = userService.getUser("sdh1_ssd/sda_1wads1_1wd2");

        Assertions.assertThat(response.getUserName()).isEqualTo("testApi");
    }

    @Test
    @WithMockUser(roles = "GET", username = "testAPI")
    void getUser_invalidRequest_fail() {
        Mockito.when(userRepository.findByUserName(ArgumentMatchers.anyString()))
                .thenReturn(null);

        var exception = org.junit.jupiter.api.Assertions
                .assertThrows(AppException.class, () -> userService.getUser("qwkJsh2nsiAnb"));

        Assertions.assertThat(exception.getMessage()).isEqualTo("user not exist");
    }
}
