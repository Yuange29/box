package storage.com.box.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import storage.com.box.dto.request.RoleCreationRequest;
import storage.com.box.dto.response.RoleResponse;
import storage.com.box.entity.Role;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface RoleMapper {


    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleCreationRequest roleCreationRequest);

    RoleResponse toRoleResponse(Role role);
}
