package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxCreditNoteDetailsRepository extends JpaRepository<TranxCreditNoteDetails, Long> {
    List<TranxCreditNoteDetails> findBySundryDebtorsIdAndStatusAndTransactionStatusIdAndOutletId(
            Long sundryDebtorsId, boolean b, long l, Long id);

    List<TranxCreditNoteDetails> findByTranxCreditNoteMasterIdAndStatus(Long id, boolean b);

    TranxCreditNoteDetails findByIdAndStatus(Long detailsId, boolean b);

    TranxCreditNoteDetails findByIdAndOutletIdAndBranchIdAndStatus(Long tranx_type, Long id, Long id1, boolean b);

    TranxCreditNoteDetails findByIdAndOutletIdAndStatus(Long tranx_type, Long id, boolean b);

    @Query(
            value = "SELECT * FROM tranx_credit_note_details_tbl WHERE " +
                    "tranx_creditnote_master_id=?1 AND type='cr' And outlet_id=?2 AND status=?3 ",
            nativeQuery = true

    )
    List<TranxCreditNoteDetails> findLedgerName(Long id, Long outlteId, boolean status);

    TranxCreditNoteDetails findByTranxCreditNoteMasterIdAndOutletIdAndBranchIdAndStatusAndType(Long transactionId, Long id, Long id1, boolean b, String dr);

    @Query(
            value = "SELECT * FROM tranx_credit_note_details_tbl WHERE " +
                    "tranx_creditnote_master_id=?1 AND outlet_id=?2 AND status=?3 AND type=?4 ",
            nativeQuery = true

    )
    TranxCreditNoteDetails findByTranxDetailsNoBR(Long transactionId, Long id, boolean b, String dr);

    @Query(
            value = "SELECT * FROM tranx_credit_note_details_tbl WHERE " +
                    "tranx_creditnote_master_id=?1 AND outlet_id=?2 AND branch_id=?2 AND status=?3 AND type=?4 ",
            nativeQuery = true

    )
    TranxCreditNoteDetails findByTranxDetailsWithBR(Long transactionId, Long outletId, Long branchId, boolean b, String dr);

    TranxCreditNoteDetails findByStatusAndTranxCreditNoteMasterId(boolean b, Long id);

    List <TranxCreditNoteDetails> findByTranxCreditNoteMasterIdAndOutletIdAndBranchIdAndStatus(Long transactionId, Long id, Long id1, boolean b);

   List <TranxCreditNoteDetails> findByTranxCreditNoteMasterIdAndOutletIdAndStatus(Long transactionId, Long id, boolean b);

    TranxCreditNoteDetails findByStatusAndTranxCreditNoteMasterIdAndOutletIdAndBranchId(boolean b,Long tranxId, Long id, Long id1);

    TranxCreditNoteDetails findByStatusAndTranxCreditNoteMasterIdAndOutletId(boolean b,Long tranxId, Long id);
}
