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
import storage.com.box.dto.request.RoleCreationRequest;
import storage.com.box.dto.response.RoleResponse;
import storage.com.box.entity.Role;
import storage.com.box.service.RoleService;
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
public class RoleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RoleService roleService;

    Role role;
    RoleCreationRequest request;
    RoleResponse response;

    @BeforeEach
    void init() {
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
    void createRole_validRequest_success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        when(roleService.createRole(request)).thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/roles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));
    }

    @Test
    void getAllRoles_validRequest_success() throws Exception {
        when(roleService.getRoles())
                .thenReturn(List.of(response));

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/roles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));

    }

    @Test
    void deleteRole_validRequest_success() throws Exception {
        doNothing().when(roleService).deleteRole(any());

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .delete("/roles/{id}", "USER")
                                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1000));

    }
}
