package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.EcommerceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcommerceTypeRepository extends JpaRepository<EcommerceType, Long> {
    EcommerceType findByTypeAndOutletIdAndStatus(String ecomType, Long id, boolean b);

    EcommerceType findByTypeAndOutletIdAndBranchIdAndStatus(String ecomType, Long id, Long id1, boolean b);
}
