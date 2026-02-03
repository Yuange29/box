package storage.com.box.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeeCreationResponse {

    String feeName;
    double feePrice;
    String feeDescription;
    Date date;
    String categoryName;
}
