package storage.com.box.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import storage.com.box.entity.Permission;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {

    String name;
    Set<Permission> permissions;
}
