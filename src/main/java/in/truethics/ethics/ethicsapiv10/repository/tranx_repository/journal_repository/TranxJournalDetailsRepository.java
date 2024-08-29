package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxJournalDetailsRepository extends JpaRepository<TranxJournalDetails,Long> {
    TranxJournalDetails findByIdAndStatus(Long detailsId, boolean b);

    List<TranxJournalDetails> findByTranxJournalMasterIdAndStatus(Long id, boolean b);

    TranxJournalDetails findByIdAndOutletIdAndBranchIdAndStatus(Long tranx_type, Long id, Long id1, boolean b);

    TranxJournalDetails findByIdAndOutletIdAndStatus(Long tranx_type, Long id, boolean b);

   List<TranxJournalDetails>  findByTranxJournalMasterIdAndTypeAndStatus(Long id, String dr, boolean b);

    TranxJournalDetails findByTranxJournalMasterIdAndOutletIdAndBranchIdAndStatusAndType(Long transactionId, Long id, Long id1, boolean b, String dr);

    TranxJournalDetails findByTranxJournalMasterIdAndOutletIdAndStatusAndType(Long transactionId, Long id, boolean b, String dr);

    List <TranxJournalDetails> findByTranxJournalMasterIdAndOutletIdAndBranchIdAndStatus(Long transactionId, Long id, Long id1, boolean b);

   List  <TranxJournalDetails> findByTranxJournalMasterIdAndOutletIdAndStatus(Long transactionId, Long id, boolean b);

    TranxJournalDetails findByStatusAndTranxJournalMasterIdAndOutletIdAndBranchId(boolean b, Long tranxId, Long id, Long id1);

    TranxJournalDetails findByStatusAndTranxJournalMasterIdAndOutletId(boolean b, Long tranxId, Long id);
}
