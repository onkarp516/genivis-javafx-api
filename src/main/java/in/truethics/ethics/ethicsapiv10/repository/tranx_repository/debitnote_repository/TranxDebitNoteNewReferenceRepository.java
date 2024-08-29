package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteNewReferenceMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxDebitNoteNewReferenceRepository extends
        JpaRepository<TranxDebitNoteNewReferenceMaster, Long> {

    @Query(
            value = " SELECT COUNT(*) FROM tranx_debit_note_new_reference_tbl WHERE " +
                    "outlet_id=?1 And branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    List<TranxDebitNoteNewReferenceMaster>
    findBySundryCreditorIdAndStatusAndTransactionStatusIdAndAdjustmentStatusAndOutletId(
            Long sundryCreditorId, boolean status, long tranxStatusId, String future, Long outletId);

    @Query(
            value = " SELECT * FROM tranx_debit_note_new_reference_tbl WHERE " +
                    "sundry_creditor_id=?1 AND status=?2 AND " +
                    "transaction_status_id=?3 AND (adjustment_status=?4 OR adjustment_status=?5) AND outlet_id=?6",
            nativeQuery = true)
    List<TranxDebitNoteNewReferenceMaster> findByDebitNoteListPayment(Long sundryCreditorId, boolean status,
                                                                      long tranxStatusId, String future, String advPmt,
                                                                      Long outletId);

    @Query(
            value = " SELECT * FROM tranx_debit_note_new_reference_tbl WHERE " +
                    "sundry_creditor_id=?1 AND status=?2 AND " +
                    "transaction_status_id=?3 AND adjustment_status=?4 AND outlet_id=?5 AND adjusted_id<>?6",
            nativeQuery = true
    )
    List<TranxDebitNoteNewReferenceMaster> findByBills(
            Long sundryCreditorId, boolean status, long tranxStatusId, String future, Long outletId, Long purchaseId);

    TranxDebitNoteNewReferenceMaster findByIdAndStatus(long debitNoteId, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_debit_note_new_reference_tbl WHERE " +
                    "outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long branchId);

    List<TranxDebitNoteNewReferenceMaster> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    TranxDebitNoteNewReferenceMaster findByIdAndOutletIdAndStatus(Long debitId, Long id, boolean b);

    List<TranxDebitNoteNewReferenceMaster> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    TranxDebitNoteNewReferenceMaster findByTranxPurReturnInvoiceIdAndStatus(Long id, boolean b);

    List<TranxDebitNoteNewReferenceMaster> findByPurchaseInvoiceIdAndStatus(Long id, boolean b);


    TranxDebitNoteNewReferenceMaster findByPaymentIdAndAdjustmentStatusAndStatus(Long id, String advance_payment, boolean b);

    @Query(
            value = " SELECT * FROM tranx_debit_note_new_reference_tbl WHERE " +
                    "sundry_creditor_id=?1 AND status=?2 AND " +
                    "adjustment_status=?3 AND adjusted_id=?4",
            nativeQuery = true
    )
    List<TranxDebitNoteNewReferenceMaster> findSelectedBills(Long sundryCreditorId, boolean b, String credit, Long id);

    TranxDebitNoteNewReferenceMaster findByPaymentIdAndStatus(Long id, boolean b);

    @Query(
            value = " SELECT * FROM tranx_debit_note_new_reference_tbl WHERE " +
                    "sundry_creditor_id=?1 AND status=?2 AND " +
                    "transaction_status_id=?3 AND (adjustment_status=?4 OR adjustment_status=?5) AND outlet_id=?6 " +
                    "AND transcation_date BETWEEN ?7 AND ?8",
            nativeQuery = true
    )
    List<TranxDebitNoteNewReferenceMaster> findByDebitNoteListPaymentWithDates(Long ledgerId, boolean b, long l, String credit, String advance_payment, Long id, LocalDate startDate, LocalDate endDate);
}
