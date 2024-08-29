package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerBalanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;

public interface LedgerBalanceSummaryRepository extends JpaRepository<LedgerBalanceSummary, Long> {
    LedgerBalanceSummary findByLedgerMasterId(Long id);

    @Query(
            value = " SELECT ledger_master_id,balance,ledger_name FROM ledger_balance_summary_tbl" +
                    " WHERE principle_groups_id=? AND balance<>0", nativeQuery = true
    )
    List<Object[]> calculate_total_amount(Long id);

    List<LedgerBalanceSummary> findByOutletId(Long id);

    List<LedgerBalanceSummary> findByPrincipleGroupsId(Long valueOf);

    @Query(
            value = "SELECT balance FROM ledger_balance_summary_tbl" +
                    " WHERE ledger_master_id=?1", nativeQuery = true
    )
    Double findBalance(Long sundryCreditorId);

    List<LedgerBalanceSummary> findByOutletIdOrderByIdDesc(Long id);

    List<LedgerBalanceSummary> findByOutletIdAndPrinciplesIdOrderByIdDesc(Long id, long l);

    List<LedgerBalanceSummary> findByOutletIdAndBranchIdOrderByIdDesc(Long id, Long id1);

    List<LedgerBalanceSummary> findByOutletIdAndBranchIdAndPrinciplesIdOrderByIdDesc(Long id, Long id1, long l);

    @Procedure("LEDGER_BALANCE_SUMMARY_POSTINGS_INSERT")
    void callBalanceSummaryPostings(Long id, Long aLong, Long aLong1, Long aLong2, Long aLong3, Long id1, String underPrefix, Long id2);

    LedgerBalanceSummary findByLedgerMasterIdAndFiscalYearIdAndStatus(Long id, Long id1, boolean b);
}
