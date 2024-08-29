package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerPaymentModeDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerPaymentModeRepository extends JpaRepository<LedgerPaymentModeDetails,Long> {
    List<LedgerPaymentModeDetails> findByLedgerIdAndStatus(Long id, boolean b);

    LedgerPaymentModeDetails findByIdAndStatus(Long detailsId, boolean b);
}
