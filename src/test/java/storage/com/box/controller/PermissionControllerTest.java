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
import storage.com.box.dto.request.PermissionCreationRequest;
import storage.com.box.dto.response.PermissionResponse;
import storage.com.box.entity.Permission;
import storage.com.box.service.PermissionService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@TestPropertySource("/test.properties")
@AutoConfigureMockMvc
public class PermissionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PermissionService permissionService;

    Permission permission;
    PermissionCreationRequest request;
    PermissionResponse response;

    @BeforeEach
    void initData() {
        request = PermissionCreationRequest.builder()
                .name("CREATE")
                .description("create request")
                .build();

        response = PermissionResponse.builder()
                .name("CREATE")
                .description("create response")
                .build();

        permission = Permission.builder()
                .name("CREATE")
                .description("create permission")
                .build();
    }

    @Test
    void createPermission_validRequest_success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(request);
        when(permissionService.createPermission(request)).thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/permission")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(1000));
    }

    @Test
    void getAllPermissions_validRequest_success() throws Exception {

        when(permissionService.getAllPermissions()).thenReturn(List.of(response));

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/permission")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))

                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(1000));
    }

    @Test
    void deletePermission_validRequest_success() throws Exception {

        doNothing()
                .when(permissionService)
                .deletePermission(any());

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/permission/{id}", "CREATE")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }

}
