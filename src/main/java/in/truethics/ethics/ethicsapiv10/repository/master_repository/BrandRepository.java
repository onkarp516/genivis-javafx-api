package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand,Long> {
    Brand findByIdAndStatus(long id, boolean b);
    Brand findFirstByBrandName(String brandName);

    List<Brand> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    Brand findByOutletIdAndBranchIdAndBrandNameAndStatus(Long id, Long id1, String brandName, boolean b);

    List<Brand> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    Brand findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    Brand findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    Brand findByOutletIdAndBrandNameAndStatusAndBranchIsNull(Long id, String brandName, boolean b);

    Brand findByOutletIdAndBranchIdAndBrandNameAndStatusAndIdNot(Long id, Long id1, String brandName, boolean b, long brandId);

    Brand findByOutletIdAndBrandNameAndStatusAndIdNotAndBranchIsNull(Long id, String brandName, boolean b, long brandId);

    Brand findByOutletIdAndBranchIdAndBrandNameIgnoreCaseAndStatusAndIdNot(Long id, Long id1, String brandName, boolean b, Long brandId);

    Brand findByOutletIdAndBranchIdAndBrandNameIgnoreCaseAndStatus(Long id, Long id1, String brandName, boolean b);

    Brand findByOutletIdAndBrandNameIgnoreCaseAndStatusAndIdNotAndBranchIsNull(Long id, String brandName, boolean b, Long brandId);

    Brand findByOutletIdAndBrandNameIgnoreCaseAndStatusAndBranchIsNull(Long id, String brandName, boolean b);
}
