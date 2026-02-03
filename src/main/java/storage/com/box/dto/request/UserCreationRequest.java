package storage.com.box.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserCreationRequest {

    @Size(min = 6, message = "INVALID_USERNAME")
    String userName;
    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;
    String email;
}
