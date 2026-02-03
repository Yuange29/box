package storage.com.box.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import storage.com.box.entity.Fee;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, String> {

    List<Fee> findByUserId(String userId);
}
