package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDNParticularBillDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxDNBillDetailsRepository extends JpaRepository<TranxDNParticularBillDetails,Long> {
    List<TranxDNParticularBillDetails> findByTranxDebitNoteDetailsAndStatus(Long id, boolean b);

    TranxDNParticularBillDetails findByIdAndStatus(Long bill_details_id, boolean b);

    TranxDNParticularBillDetails findByTranxDebitNoteMasterAndTranxInvoiceIdAndStatus(Long id, Long invoiceId, boolean b);
}
