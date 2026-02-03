package storage.com.box.mapper;

import org.mapstruct.Mapper;
import storage.com.box.dto.request.PermissionCreationRequest;
import storage.com.box.dto.response.CategoryResponse;
import storage.com.box.dto.response.PermissionResponse;
import storage.com.box.entity.Category;
import storage.com.box.entity.Permission;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse categoryToCategoryResponse(Category category);

}
