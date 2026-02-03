package storage.com.box.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import storage.com.box.dto.request.FeeCreationRequest;
import storage.com.box.dto.request.FeeUpdateRequest;
import storage.com.box.dto.response.CategoryResponse;
import storage.com.box.dto.response.FeeCreationResponse;
import storage.com.box.entity.Category;
import storage.com.box.entity.Fee;

@Mapper(componentModel = "spring")
public interface FeeMapper {

    FeeCreationResponse toFeeCreationResponse(Fee request);

    Fee toFee(FeeCreationRequest request);

    @Mapping(target = "date", ignore = true)
    void updateFee(FeeUpdateRequest request, @MappingTarget Fee fee);
}
