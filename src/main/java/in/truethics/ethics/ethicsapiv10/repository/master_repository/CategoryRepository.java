package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByIdAndStatus(long id, boolean b);

    List<Category> findByOutletIdAndBranchIdAndStatus(Long outletId, Long id, boolean b);

    Category findByOutletIdAndBranchIdAndStatusAndCategoryNameIgnoreCase(Long id, Long id1, boolean b, String categoryName);


    List<Category> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    Category findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    Category findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    Category findByOutletIdAndStatusAndCategoryNameIgnoreCaseAndBranchIsNull(Long id, boolean b, String categoryName);

    Category findByOutletIdAndBranchIdAndStatusAndCategoryNameIgnoreCaseAndIdNot(Long id, Long id1, boolean b, String categoryName, Long id2);

    Category findByOutletIdAndStatusAndCategoryNameIgnoreCaseAndIdNotAndBranchIsNull(Long id, boolean b, String categoryName, Long id1);

    Category findFirstByCategoryNameIgnoreCase(String categoryName);

}
