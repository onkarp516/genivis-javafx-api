package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCNParticularBillDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxCNBillDetailsRepository extends JpaRepository<TranxCNParticularBillDetails,Long>{


    TranxCNParticularBillDetails findByIdAndStatus(Long bill_details_id, boolean b);

    List<TranxCNParticularBillDetails> findByTranxCreditNoteDetailsIdAndStatus(Long id, boolean b);
}
