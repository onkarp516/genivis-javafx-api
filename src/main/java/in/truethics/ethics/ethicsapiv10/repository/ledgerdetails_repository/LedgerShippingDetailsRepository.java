package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerShippingDetailsRepository extends JpaRepository<LedgerShippingAddress,Long> {
    List<LedgerShippingAddress> findByLedgerMasterIdAndStatus(Long ledgerId, boolean b);

    LedgerShippingAddress findByIdAndStatus(Long id, boolean b);
}
