package storage.com.box.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {

    @Size(min = 6, message = "INVALID_USERNAME")
    String userName;
    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;
}
