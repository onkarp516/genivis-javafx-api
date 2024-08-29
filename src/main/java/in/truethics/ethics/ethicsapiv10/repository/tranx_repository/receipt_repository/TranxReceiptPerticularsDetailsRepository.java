package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository;


import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxReceiptPerticularsDetailsRepository extends JpaRepository<TranxReceiptPerticularsDetails, Long> {
    List<TranxReceiptPerticularsDetails> findByIdAndStatus(Long id, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_receipt_perticulars_details_tbl` WHERE id=?1 AND status=?2", nativeQuery = true
    )
    TranxReceiptPerticularsDetails findBylistStatus(Long id, boolean b);


    TranxReceiptPerticularsDetails findByIdAndOutletIdAndBranchIdAndStatus(Long tranx_type, Long id, Long id1, boolean b);

    TranxReceiptPerticularsDetails findByIdAndOutletIdAndStatus(Long tranx_type, Long id, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amt),0.0),COUNT(*) FROM `tranx_receipt_perticulars_details_tbl` WHERE type=?1 AND transaction_date BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true)
    List<Object[]> findmobileReceiptTotalAmt(String type, LocalDate startDatep, LocalDate endDatep, boolean b);

    List<LedgerMaster> findByStatus(boolean b);

    List<TranxReceiptPerticularsDetails> findByTranxReceiptPerticularsIdAndStatus(Long id, boolean b);

    TranxReceiptPerticularsDetails findByStatusAndId(boolean b, Long bill_details_id);

    TranxReceiptPerticularsDetails findByStatusAndTranxReceiptPerticularsId( boolean b,Long id);

    TranxReceiptPerticularsDetails findByLedgerMasterIdAndTranxInvoiceIdAndStatus(Long id, Long id1, boolean b);

    TranxReceiptPerticularsDetails findByTranxReceiptMasterIdAndTranxInvoiceIdAndStatus(Long id, long asLong, boolean b);

    TranxReceiptPerticularsDetails findByTranxReceiptMasterIdAndOutletIdAndBranchIdAndStatus(Long transactionId, Long id, Long id1, boolean b);

    TranxReceiptPerticularsDetails findByTranxReceiptMasterIdAndOutletIdAndStatus(Long transactionId, Long id, boolean b);
}
