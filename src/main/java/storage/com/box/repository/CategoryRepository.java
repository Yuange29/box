package storage.com.box.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import storage.com.box.entity.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> getCategoriesByUserId(String userId);
    boolean existsByCategoryName(String categoryName);

    @Override
    boolean existsById(String s);
}
