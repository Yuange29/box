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
import storage.com.box.dto.request.PermissionCreationRequest;
import storage.com.box.dto.request.RoleCreationRequest;
import storage.com.box.dto.response.PermissionResponse;
import storage.com.box.dto.response.RoleResponse;
import storage.com.box.entity.Permission;
import storage.com.box.entity.Role;
import storage.com.box.repository.PermissionRepository;
import storage.com.box.repository.RoleRepository;

import java.util.Collections;

import static org.mockito.Mockito.doNothing;

@SpringBootTest
@TestPropertySource("/test.properties")
public class PermissionServiceTest {

    @MockitoBean
    PermissionRepository permissionRepository;

    @Autowired
    PermissionService permissionService;

    Permission permission;
    PermissionCreationRequest request;
    PermissionResponse response;

    @BeforeEach
    void initData() {
        request = PermissionCreationRequest.builder()
                .name("CREATE")
                .description("Create new data")
                .build();

        response = PermissionResponse.builder()
                .name("CREATE")
                .description("Create new data")
                .build();

        permission = Permission.builder()
                .name("CREATE")
                .description("Create new data")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPermission_validRequest_success() {
        Mockito.when(permissionRepository.save(ArgumentMatchers.any())).thenReturn(permission);

        var response = permissionService.createPermission(request);

        Assertions.assertThat(response.getName()).isEqualTo("CREATE");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPermissions_validRequest_success() {
        Mockito.when(permissionRepository.findAll())
                .thenReturn(Collections.singletonList(permission));

        var response = permissionService.getAllPermissions();

//        Assertions.assertThat(response).isEqualTo(Collections.singletonList(permission));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePermission_validRequest_success() {
        doNothing().when(permissionRepository).deleteById(ArgumentMatchers.any());

        permissionService.deletePermission("CREATE");
    }
}
