package storage.com.box.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String userId;

    @Size(min = 6, message = "invalid user name")
    String userName;
    @Size(min = 6, message = "invalid pw")
    String password;
    String email;

    @ManyToMany
    Set<Role> roles;

}
