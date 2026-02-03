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
import storage.com.box.dto.request.RoleCreationRequest;
import storage.com.box.dto.response.RoleResponse;
import storage.com.box.entity.Role;
import storage.com.box.repository.PermissionRepository;
import storage.com.box.repository.RoleRepository;

import java.util.Collections;

import static org.mockito.Mockito.doNothing;

@SpringBootTest
@TestPropertySource("/test.properties")
public class RoleServiceTest {

    @MockitoBean
    RoleRepository roleRepository;
    @MockitoBean
    PermissionRepository permissionRepository;

    @Autowired
    RoleService roleService;

    RoleCreationRequest request;
    RoleResponse response;
    Role role;

    @BeforeEach
    void initData() {

        request = RoleCreationRequest.builder()
                .name("USER")
                .permissions(null)
                .build();

        response = RoleResponse.builder()
                .name("USER")
                .permissions(null)
                .build();

        role = Role.builder()
                .name("USER")
                .permissions(null)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_validRequest_success() {
        Mockito.when(roleRepository.save(ArgumentMatchers.any()))
                .thenReturn(role);
        Mockito.when(permissionRepository.findAllById(ArgumentMatchers.any()))
                .thenReturn(Collections.emptyList());

        var response = roleService.createRole(request);

        Assertions.assertThat(response.getName()).isEqualTo("USER");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoles_validRequest_success() {
        Mockito.when(roleRepository.findAll()).thenReturn(Collections.singletonList(role));

        var response = roleService.getRoles();

    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "USER")
    void deleteRole_validRequest_success() {
        doNothing().when(roleRepository).deleteById(ArgumentMatchers.any());

        roleService.deleteRole("USER");
    }

}
