package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticulars;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxPaymentPerticularsRepository extends JpaRepository<TranxPaymentPerticulars, Long> {

    List<TranxPaymentPerticulars> findByOutletIdAndStatusOrderByIdDesc(Long id, boolean b);

    @Query(
            value = "SELECT * FROM tranx_payment_perticulars_tbl WHERE" +
                    " tranx_payment_master_id=?1 AND (ledger_type='SC' OR ledger_type='SD') AND outlet_id=?2 AND status=?3 ",
            nativeQuery = true

    )
    List<TranxPaymentPerticulars> findLedgerName(Long id, Long outlteId, boolean status);

    List<TranxPaymentPerticulars> findByTranxPaymentMasterIdAndStatus(Long id, boolean b);

    TranxPaymentPerticulars findByIdAndStatus(Long detailsId, boolean b);

    TranxPaymentPerticulars findByTranxPaymentMasterIdAndOutletIdAndBranchIdAndStatusAndType(Long transactionId, Long id, Long id1, boolean b, String dr);

    TranxPaymentPerticulars findByTranxPaymentMasterIdAndOutletIdAndStatusAndType(Long transactionId, Long id, boolean b, String dr);
    @Query(
            value = "SELECT IFNULL(SUM(dr),0.0),COUNT(*) FROM `tranx_payment_perticulars_tbl` WHERE type=?1 AND transaction_date BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true)
      List<Object[]> findmobilePaymentTotalAmt(String type, LocalDate startDatep, LocalDate endDatep, boolean b);

    List<TranxPaymentPerticulars> findByTypeAndStatus(String dr, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_payment_perticulars_tbl` WHERE ledger_id=?1 AND transaction_date BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true)
    List<TranxPaymentPerticulars> findByLedgerMasterAndStatus(Long ledgerId,LocalDate startDatep, LocalDate endDatep, boolean b);

    TranxPaymentPerticulars findByTranxPaymentMasterIdAndTypeAndStatus(Long masterId, String dr, boolean b);

    List<TranxPaymentPerticulars> findByTranxPaymentMasterIdAndOutletIdAndBranchIdAndStatusAndTypeIgnoreCase(Long transactionId, Long id, Long id1, boolean b, String cr);

    List<TranxPaymentPerticulars> findByTranxPaymentMasterIdAndOutletIdAndStatusAndTypeIgnoreCase(Long transactionId, Long id, boolean b, String cr);

    TranxPaymentPerticulars findByTranxPaymentMasterIdAndStatusAndLedgerTypeIgnoreCaseAndLedgerMasterId(Long id, boolean b, String type, Long id1);
}
