package storage.com.box.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeeCreationRequest {

    String feeName;
    double feePrice;
    String feeDescription;
    Date date;
    String categoryName;

}
