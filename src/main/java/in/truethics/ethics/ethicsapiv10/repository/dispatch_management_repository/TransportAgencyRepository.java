package in.truethics.ethics.ethicsapiv10.repository.dispatch_management_repository;

import in.truethics.ethics.ethicsapiv10.model.dispatch_management.TransportAgency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportAgencyRepository extends JpaRepository<TransportAgency,Long> {


    TransportAgency save(TransportAgency transportAgency);

    List<TransportAgency> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);




    List<TransportAgency> findByOutletIdAndStatusAndBranchIdIsNull(Long outletId, boolean b);

    TransportAgency findByIdAndStatus(long id, boolean b);
}
