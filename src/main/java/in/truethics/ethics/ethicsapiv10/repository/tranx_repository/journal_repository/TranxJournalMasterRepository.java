package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface TranxJournalMasterRepository extends JpaRepository<TranxJournalMaster,Long> {
    @Query(
            value = "select COUNT(*) from tranx_journal_master_tbl WHERE outlet_id=?1 AND branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);


    @Query(
            value = " SELECT COUNT(*) FROM tranx_journal_master_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long id1);
    List<TranxJournalMaster> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    TranxJournalMaster findByIdAndStatus(long journal_id, boolean b);

    TranxJournalMaster findByIdAndOutletIdAndStatus(Long journalId, Long id, boolean b);

    List<TranxJournalMaster> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);
}
