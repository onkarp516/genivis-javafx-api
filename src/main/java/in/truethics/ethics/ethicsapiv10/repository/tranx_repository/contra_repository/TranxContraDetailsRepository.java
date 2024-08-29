package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.contra_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxContraDetailsRepository extends JpaRepository<TranxContraDetails,Long> {
    /*@Query(
            value = "SELECT * FROM tranx_contra_details_tbl WHERE" +
                    " tranx_contra_master_id=?1 AND (ledger_type='others' OR ledger_type='bank_account') And outlet_id=?2 AND status =?3 ",
            nativeQuery = true

    )
    TranxContraDetails findLedgerName(Long id, Long outlteId, boolean status);
*/
    @Query(
            value = "SELECT * FROM tranx_contra_details_tbl WHERE" +
                    " tranx_contra_master_id=?1 AND type='dr' And outlet_id=?2 AND status =?3 ",
            nativeQuery = true

    )
    List<TranxContraDetails> findLedgerName(Long id, Long outlteId, boolean status);
    TranxContraDetails findByIdAndStatus(Long detailsId, boolean b);

    List<TranxContraDetails> findByTranxContraMasterIdAndStatus(Long id, boolean b);

    TranxContraDetails findByIdAndOutletIdAndBranchIdAndStatus(Long tranx_type, Long id, Long id1, boolean b);

    TranxContraDetails findByIdAndOutletIdAndStatus(Long tranx_type, Long id, boolean b);

    TranxContraDetails findByTranxContraMasterIdAndOutletIdAndBranchIdAndStatusAndType(Long transactionId, Long id, Long id1, boolean b, String dr);

    TranxContraDetails findByTranxContraMasterIdAndOutletIdAndStatusAndType(Long transactionId, Long id, boolean b, String dr);


    List<TranxContraDetails> findByTranxContraMasterIdAndOutletIdAndBranchIdAndStatus(Long transactionId, Long id, Long id1, boolean b);

    List<TranxContraDetails> findByTranxContraMasterIdAndOutletIdAndStatus(Long transactionId, Long id, boolean b);

    TranxContraDetails findByStatusAndTranxContraMasterIdAndOutletIdAndBranchId(boolean b, Long tranxId, Long id, Long id1);

    TranxContraDetails findByStatusAndTranxContraMasterIdAndOutletId(boolean b,Long tranxId, Long id);
}
