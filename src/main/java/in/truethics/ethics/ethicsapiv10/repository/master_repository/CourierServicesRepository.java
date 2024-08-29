package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Country;
import in.truethics.ethics.ethicsapiv10.model.master.CourierServices;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourierServicesRepository extends JpaRepository<CourierServices,Long> {
    CourierServices save(CourierServices courierServices);

    List<CourierServices> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    List<CourierServices> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    CourierServices findByIdAndStatus(long id, boolean b);
}
