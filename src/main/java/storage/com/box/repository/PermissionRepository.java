package storage.com.box.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import storage.com.box.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
