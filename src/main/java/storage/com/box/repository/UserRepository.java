package storage.com.box.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import storage.com.box.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserName(String username);

    Optional<User> findByUserId(String userId);

    boolean existsByUserName(String userName);

    boolean existsByUserId(String userId);
}
