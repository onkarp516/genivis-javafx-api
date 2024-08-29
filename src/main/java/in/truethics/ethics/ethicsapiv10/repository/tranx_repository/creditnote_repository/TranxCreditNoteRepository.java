package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TranxCreditNoteRepository extends JpaRepository<TranxCreditNote,Long>{
    @Query(
            value = "select COUNT(*) from tranx_credit_note_tbl WHERE outlet_id=?1 AND status=1 ", nativeQuery = true
    )
    Long findCreditNoteLastRecord(Long id);
}
