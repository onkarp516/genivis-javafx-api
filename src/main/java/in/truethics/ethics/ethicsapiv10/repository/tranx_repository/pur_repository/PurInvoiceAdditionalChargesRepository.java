package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceAdditionalCharges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurInvoiceAdditionalChargesRepository extends
        JpaRepository<TranxPurInvoiceAdditionalCharges, Long> {

    List<TranxPurInvoiceAdditionalCharges> findByPurchaseTransaction(TranxPurInvoice mPurchaseTranx);

    List<TranxPurInvoiceAdditionalCharges> findByPurchaseTransactionIdAndStatus(Long id, boolean b);

    TranxPurInvoiceAdditionalCharges findByIdAndStatus(Long detailsId, boolean b);

    TranxPurInvoiceAdditionalCharges findByAdditionalChargesIdAndPurchaseTransactionIdAndStatus(Long ledgerId, Long id, boolean b);
}
