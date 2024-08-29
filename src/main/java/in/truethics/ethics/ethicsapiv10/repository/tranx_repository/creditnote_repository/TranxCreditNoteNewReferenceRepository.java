package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxCreditNoteNewReferenceRepository extends
        JpaRepository<TranxCreditNoteNewReferenceMaster, Long> {
    @Query(
            value = " SELECT COUNT(*) FROM tranx_credit_note_new_reference_tbl WHERE " +
                    "outlet_id=?1 And branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    TranxCreditNoteNewReferenceMaster findByIdAndStatus(long debitNoteId, boolean b);


    List<TranxCreditNoteNewReferenceMaster>
    findBySundryDebtorsIdAndStatusAndTransactionStatusIdAndAdjustmentStatusAndOutletId(
            Long sundryDebtorsId, boolean b, long l, String future, Long id);


    @Query(
            value = " SELECT COUNT(*) FROM tranx_credit_note_new_reference_tbl WHERE " +
                    "outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long branchId);

    List<TranxCreditNoteNewReferenceMaster> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    TranxCreditNoteNewReferenceMaster findByIdAndOutletIdAndStatus(Long creditId, Long id, boolean b);


    List<TranxCreditNoteNewReferenceMaster> findByOutletIdAndStatusAndBranchIdIsNullOrderByIdDesc(Long id, boolean b);

    TranxCreditNoteNewReferenceMaster findByTranxSalesReturnInvoiceIdAndStatus(Long id, boolean b);

    List<TranxCreditNoteNewReferenceMaster> findBySalesInvoiceIdAndStatus(Long id, boolean b);

    TranxCreditNoteNewReferenceMaster findByReceiptIdAndStatus(Long id, boolean b);

    @Query(
            value = " SELECT * FROM tranx_credit_note_new_reference_tbl WHERE " +
                    "sundry_debtors_id=?1 AND status=?2 AND " +
                    "transaction_status_id=?3 AND (adjustment_status=?4 OR adjustment_status=?5) AND " +
                    "outlet_id=?6", nativeQuery = true
    )
    List<TranxCreditNoteNewReferenceMaster> findByCreditnoteListReceipt(Long ledgerId, boolean b, long l, String credit,
                                                                        String advanceRcpt, Long id);

    @Query(
            value = " SELECT * FROM tranx_credit_note_new_reference_tbl WHERE " +
                    "sundry_debtors_id=?1 AND status=?2 AND " +
                    "transaction_status_id=?3 AND (adjustment_status=?4 OR adjustment_status=?5) AND " +
                    "outlet_id=?6 AND transcation_date BETWEEN ?7 AND ?8", nativeQuery = true
    )
    List<TranxCreditNoteNewReferenceMaster> findByCreditnoteListReceiptWithDate(Long ledgerId, boolean b, long l,
                                                                                String credit, String advance_receipt,
                                                                                Long id, LocalDate startDate,
                                                                                LocalDate endDate);
}
