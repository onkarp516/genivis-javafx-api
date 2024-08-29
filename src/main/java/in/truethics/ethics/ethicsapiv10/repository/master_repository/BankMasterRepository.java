package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.BankMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankMasterRepository extends JpaRepository<BankMaster,Long> {
    List<BankMaster> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    List<BankMaster> findByOutletIdAndStatusAndBranchIdIsNull(Long outletId, boolean b);

    BankMaster findByIdAndStatus(long id, boolean b);
}
