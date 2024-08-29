package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerOpeningBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LedgerOpeningBalanceRepository extends JpaRepository<LedgerOpeningBalance, Long> {
    List<LedgerOpeningBalance> findByLedgerIdAndStatus(Long id, boolean b);

    LedgerOpeningBalance findByIdAndStatus(long bid, boolean b);

    @Query(value = "SELECT * from ledger_opening_balance_tbl where ledger_id=?1 AND invoice_no=?2 AND status=?3"
            , nativeQuery = true)
    LedgerOpeningBalance findByOpeningBalInvoice(Long id, String invoiceNo, boolean b);
}
