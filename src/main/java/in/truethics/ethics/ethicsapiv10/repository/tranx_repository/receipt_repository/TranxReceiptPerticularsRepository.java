package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticulars;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxReceiptPerticularsRepository extends JpaRepository<TranxReceiptPerticulars, Long> {
    @Query(
            value = "SELECT * FROM tranx_receipt_perticulars_tbl WHERE" +
                    " tranx_receipt_master_id=?1 AND (ledger_type='SC' OR ledger_type='SD')  AND outlet_id=?2 AND status =?3 ",
            nativeQuery = true

    )
    List<TranxReceiptPerticulars> findLedgerName(Long id, Long outlteId, boolean status);

    List<TranxReceiptPerticulars> findByTranxReceiptMasterIdAndStatus(Long id, boolean b);

    TranxReceiptPerticulars findByIdAndStatus(Long detailsId, boolean b);

    TranxReceiptPerticulars findByStatusAndTranxReceiptMasterId(boolean b, Long id);

    TranxReceiptPerticulars findByIdAndOutletIdAndBranchIdAndStatus(Long transactionId, Long id, Long id1, boolean b);

    TranxReceiptPerticulars findByIdAndOutletIdAndStatus(Long transactionId, Long id, boolean b);

    TranxReceiptPerticulars findByTranxReceiptMasterIdAndOutletIdAndBranchIdAndStatusAndType(Long transactionId, Long id, Long id1, boolean b, String cr);

    TranxReceiptPerticulars findByTranxReceiptMasterIdAndOutletIdAndStatusAndType(Long transactionId, Long id, boolean b, String cr);


    @Query(
            value = "SELECT IFNULL(SUM(cr),0.0),COUNT(*) FROM `tranx_receipt_perticulars_tbl` WHERE type=?1 AND transaction_date BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true)
    List<Object[]> findmobileReceiptTotalAmt(String type, LocalDate startDatep, LocalDate endDatep, boolean b);

    List<TranxReceiptPerticulars> findByTypeAndStatus(String cr, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_receipt_perticulars_tbl` WHERE ledger_id=?1 AND transaction_date BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true)
    List<TranxReceiptPerticulars> findByLedgerMasterAndStatus(Long ledgerId, LocalDate startDatep, LocalDate endDatep, boolean b);

    TranxReceiptPerticulars findByTranxReceiptMasterIdAndTypeAndStatus(Long masterId, String dr, boolean b);

    TranxReceiptPerticulars findByTranxReceiptMasterIdAndStatusAndLedgerTypeIgnoreCase(Long id, boolean b, String type);

    TranxReceiptPerticulars findByTranxReceiptMasterIdAndStatusAndLedgerTypeIgnoreCaseAndLedgerMasterId(Long id, boolean b, String type, Long id1);

    List<TranxReceiptPerticulars> findByTranxReceiptMasterIdAndOutletIdAndBranchIdAndStatusAndTypeIgnoreCase(Long transactionId, Long id, Long id1, boolean b, String dr);

    List<TranxReceiptPerticulars> findByTranxReceiptMasterIdAndOutletIdAndStatusAndTypeIgnoreCase(Long transactionId, Long id, boolean b, String dr);


    @Query(
            value = "SELECT * FROM tranx_receipt_perticulars_tbl WHERE tranx_receipt_master_id=?1 AND status=?2", nativeQuery = true
    )
    List<TranxReceiptPerticulars> findByReceiptIdAndStatus(String receiptId, boolean b);
}
