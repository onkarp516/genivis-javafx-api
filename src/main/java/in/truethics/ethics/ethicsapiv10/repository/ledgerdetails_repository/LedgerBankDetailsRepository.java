package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerBankDetailsRepository extends JpaRepository<LedgerBankDetails,Long> {
    List<LedgerBankDetails> findByLedgerMasterIdAndStatus(Long id, boolean b);

    LedgerBankDetails findByIdAndStatus(long id, boolean b);

    LedgerBankDetails findByLedgerMasterIdAndBankNameIgnoreCaseAndStatus(Long id, String bankName, boolean b);

    LedgerBankDetails findByStatusAndLedgerMasterId(boolean b, Long id);
}
