package in.truethics.ethics.ethicsapiv10.repository.master_repository;


import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductHsnRepository extends JpaRepository<ProductHsn, Long> {
    ProductHsn findByIdAndStatus(long hsnId, boolean b);

    List<ProductHsn> findByOutletIdAndStatusAndBranchId(Long id, boolean b, Long id1);

    ProductHsn findByOutletIdAndBranchIdAndHsnNumberAndStatus(Long id, Long id1, String hsnNumber, boolean b);

    ProductHsn findByOutletIdAndHsnNumberAndStatus(Long id, String hsnNumber, boolean b);

    List<ProductHsn> findByOutletIdAndStatusAndBranchIsNull(Long id, boolean b);

    ProductHsn findByHsnNumber(String toString);

    ProductHsn findByOutletIdAndHsnNumberAndStatusAndBranchIsNull(Long id, String hsnNumber, boolean b);

    ProductHsn findByOutletIdAndBranchIdAndHsnNumberAndStatusAndIdNot(Long id, Long id1, String hsnNumber, boolean b, Long hsnId);

    ProductHsn findByOutletIdAndHsnNumberAndStatusAndIdNotAndBranchIsNull(Long id, String hsnNumber, boolean b, Long hsnId);
}
