package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface PurchaseInvoiceDetailsRepository extends JpaRepository<TranxPurInvoiceDetails, Long> {
   

    List<TranxPurInvoiceDetails> findByPurchaseTransactionIdAndStatus(Long id, boolean b);

    TranxPurInvoiceDetails findByIdAndStatus(Long detailsId, boolean b);

    Set<TranxPurInvoiceDetails> findByProductId(Long id);

    TranxPurInvoiceDetails findTop1ByProductIdOrderByIdDesc(Long id);


}
