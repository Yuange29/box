package storage.com.box.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import storage.com.box.entity.Role;
import storage.com.box.entity.User;
import storage.com.box.repository.RoleRepository;
import storage.com.box.repository.UserRepository;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@Slf4j
public class ApplicationInitStart {

    PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnProperty(prefix = "spring", name = "datasource.driver-class-name",
    havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {

            if (!userRepository.existsByUserName("#admin")) {

                Role roleAdmin = roleRepository.findById("ADMIN")
                        .orElseGet(() -> roleRepository.save(
                                Role.builder().name("ADMIN").permissions(null).build()
                        ));

                var roles = new HashSet<Role>();
                roles.add(roleAdmin);

                User user = User.builder()
                        .userName("#admin")
                        .password(passwordEncoder.encode("#admin"))
                        .roles(roles)
                        .build();

                userRepository.save(user);

                log.info("user has default password, please change");

            }
        };
    }
}
