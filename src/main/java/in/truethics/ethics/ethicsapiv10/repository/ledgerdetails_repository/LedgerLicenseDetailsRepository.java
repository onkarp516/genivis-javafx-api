package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerLicenseDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerLicenseDetailsRepository extends JpaRepository<LedgerLicenseDetails,Long> {




    List<LedgerLicenseDetails> findByLedgerMasterIdAndStatus(Long id, boolean b);

    LedgerLicenseDetails findByIdAndStatus(long lid, boolean b);

}
