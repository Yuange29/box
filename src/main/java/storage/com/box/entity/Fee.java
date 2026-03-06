package storage.com.box.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "fee")
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "fee_id")
    String feeId;

    @Column(name = "fee_name")
    String feeName;

    @Column(name = "fee_price")
    double feePrice;

    @Column(name = "fee_description")
    String feeDescription;

    @Column(name = "date")
    Date date;

    @Column(name = "category_name")
    String categoryName;

    @Column(name = "user_id")
    String userId;

}
