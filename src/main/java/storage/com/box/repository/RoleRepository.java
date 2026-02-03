package storage.com.box.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import storage.com.box.entity.Role;

import java.util.List;
import java.util.Set;


@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
