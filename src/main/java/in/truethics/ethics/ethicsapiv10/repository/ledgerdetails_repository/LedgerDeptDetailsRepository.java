package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerDeptDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerDeptDetailsRepository extends JpaRepository<LedgerDeptDetails,Long> {
    List<LedgerDeptDetails> findByLedgerMasterIdAndStatus(Long ledgerId, boolean b);

    LedgerDeptDetails findByIdAndStatus(long id, boolean b);
}
