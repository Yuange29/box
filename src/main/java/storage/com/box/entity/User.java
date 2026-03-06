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
    @Column(name = "user_id")
    String userId;

    @Column(name = "user_name")
    @Size(min = 6, message = "invalid user name")
    String userName;

    @Column(name = "password")
    @Size(min = 6, message = "invalid pw")
    String password;

    @Column(name = "email")
    String email;

    @ManyToMany
    Set<Role> roles;

}
