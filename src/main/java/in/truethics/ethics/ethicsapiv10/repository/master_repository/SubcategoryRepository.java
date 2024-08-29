package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    List<Subcategory> findByOutletIdAndStatus(Long outletId, boolean b);

    Subcategory findByIdAndStatus(long id, boolean b);

    Subcategory findByOutletIdAndBranchIdAndStatusAndSubcategoryNameIgnoreCase(Long id, Long id1, boolean b, String subCategoryName);

    List<Subcategory> findByOutletIdAndBranchIdAndStatus(Long outletId, Long id, boolean b);

    List<Subcategory> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    Subcategory findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    Subcategory findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    Subcategory findByOutletIdAndStatusAndSubcategoryNameIgnoreCaseAndBranchIsNull(Long id, boolean b, String subCategoryName);

    Subcategory findByOutletIdAndBranchIdAndStatusAndSubcategoryNameIgnoreCaseAndIdNot(Long id, Long id1, boolean b, String subCategoryName, Long id2);

    Subcategory findByOutletIdAndStatusAndSubcategoryNameIgnoreCaseAndIdNotAndBranchIsNull(Long id, boolean b, String subCategoryName, Long id1);

    Subcategory findFirstBySubcategoryNameIgnoreCase(String subcategoryName);

}
