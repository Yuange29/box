package storage.com.box.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import storage.com.box.dto.request.UserCreationRequest;
import storage.com.box.dto.request.UserUpdateRequest;
import storage.com.box.dto.response.UserCreationResponse;
import storage.com.box.dto.response.UserResponse;
import storage.com.box.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper{

    User toUser(UserCreationRequest request);

    @Mapping(target = "roles", ignore = true)
    UserCreationResponse toUserCreationResponse(User user);

    @Mapping(target = "roles",  ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    UserResponse toUserResponse(User user);
}
