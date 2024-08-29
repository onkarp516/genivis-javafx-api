package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerClosingDateSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LedgerClosingDateSummaryRepository extends JpaRepository<LedgerClosingDateSummary, Long> {
    LedgerClosingDateSummary findByLedgerMasterIdAndFiscalYearIdAndClosingDateAndStatus(Long id, Long id1, LocalDate invoiceDate, boolean b);

    @Query(value = "SELECT * FROM ledger_closing_date_summary_tbl WHERE ledger_id=?1 AND closing_date>?2 AND status=?3", nativeQuery = true)
    List<LedgerClosingDateSummary> findByLedgerMasterIdAndClosingDateAndStatus(Long id, LocalDate invDt, boolean b);
}
