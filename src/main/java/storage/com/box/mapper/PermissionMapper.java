package storage.com.box.mapper;

import org.mapstruct.Mapper;
import storage.com.box.dto.request.PermissionCreationRequest;
import storage.com.box.dto.response.PermissionResponse;
import storage.com.box.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    Permission toPermission(PermissionCreationRequest request);
    PermissionResponse topermissionResponse(Permission permission);

}
