package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxDebitNoteDetailsRepository extends JpaRepository<TranxDebitNoteDetails, Long> {

   // List<TranxDebitNoteDetails> findBySundryCreditorIdAndStatusAndTransactionStatusIdAndAdjustmentStatusAndOutletId(Long sundryCreditorId, boolean b, long l, String future, Long id);

    List<TranxDebitNoteDetails> findBySundryCreditorIdAndStatusAndTransactionStatusIdAndOutletId(Long sundryCreditorId, boolean b, long l, Long id);

    TranxDebitNoteDetails findByIdAndStatus(Long detailsId, boolean b);

    List<TranxDebitNoteDetails> findByTranxDebitNoteMasterIdAndStatus(Long id, boolean b);

    TranxDebitNoteDetails findByIdAndOutletIdAndBranchIdAndStatus(Long tranx_type, Long id, Long id1, boolean b);

    TranxDebitNoteDetails findByIdAndOutletIdAndStatus(Long tranx_type, Long id, boolean b);

    @Query(
            value = "SELECT * FROM tranx_debit_note_details_tbl WHERE " +
                    "tranx_debitnote_master_id=?1 AND type='dr' And outlet_id=?2 AND status=?3 ",
            nativeQuery = true

    )
    List<TranxDebitNoteDetails> findLedgerName(Long id, Long id1, boolean b);

    List<TranxDebitNoteDetails> findByTranxDebitNoteMasterIdAndOutletIdAndBranchIdAndStatusAndType(Long transactionId, Long id, Long id1, boolean b, String dr);

    List<TranxDebitNoteDetails> findByTranxDebitNoteMasterIdAndOutletIdAndStatusAndType(Long transactionId, Long id, boolean b, String dr);

    TranxDebitNoteDetails findByStatusAndTranxDebitNoteMasterId(boolean b, Long id);

    List <TranxDebitNoteDetails> findByTranxDebitNoteMasterIdAndOutletIdAndBranchIdAndStatus(Long transactionId, Long id, Long id1, boolean b);

    List <TranxDebitNoteDetails> findByTranxDebitNoteMasterIdAndOutletIdAndStatus(Long transactionId, Long id, boolean b);

    TranxDebitNoteDetails findByStatusAndTranxDebitNoteMasterIdAndOutletIdAndBranchId(boolean b, Long tranxId, Long id, Long id1);

    TranxDebitNoteDetails findByStatusAndTranxDebitNoteMasterIdAndOutletId(boolean b, Long tranxId, Long id);

//    @Query(
//            value = "SELECT * FROM tranx_debit_note_details_tbl WHERE" +
//                    " tranx_debitnote_master_id=?1 AND (ledger_type='SC' OR ledger_type='SD')  AND outlet_id=?2 AND status =?3 ",
//            nativeQuery = true
//
//    )
//    List<TranxDebitNoteNewReferenceMaster> findLedgerName(Long id, Long id1, boolean b);
}
