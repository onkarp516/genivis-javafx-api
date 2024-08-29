package in.truethics.ethics.ethicsapiv10.repository.dispatch_management_repository;

import in.truethics.ethics.ethicsapiv10.model.dispatch_management.DeliveryBoy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryBoyRepository extends JpaRepository<DeliveryBoy, Long> {

    List<DeliveryBoy> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);


    DeliveryBoy findByIdAndStatus(long id, boolean b);

    List<DeliveryBoy> findByOutletIdAndStatusAndBranchIdIsNull(Long outletId, boolean b);
}
