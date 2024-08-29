package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalBillDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxJournalBillsDetailsRepository extends JpaRepository<TranxJournalBillDetails,Long> {

    List<TranxJournalBillDetails> findByTranxJournalDetailsIdAndStatus(Long id, boolean b);

    TranxJournalBillDetails findByIdAndStatus(Long bill_details_id, boolean b);
}
