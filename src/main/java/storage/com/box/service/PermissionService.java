package storage.com.box.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import storage.com.box.dto.request.PermissionCreationRequest;
import storage.com.box.dto.response.PermissionResponse;
import storage.com.box.entity.Permission;
import storage.com.box.exception.AppException;
import storage.com.box.mapper.PermissionMapper;
import storage.com.box.repository.PermissionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse createPermission(PermissionCreationRequest request)
            throws AppException {

        Permission permission = permissionMapper.toPermission(request);

        permissionRepository.save(permission);

        return permissionMapper.topermissionResponse(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<PermissionResponse> getAllPermissions()
            throws AppException {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::topermissionResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deletePermission(String permission)
            throws AppException {
        permissionRepository.deleteById(permission);
    }
}
