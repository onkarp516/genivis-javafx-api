package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurInvoiceDutiesTaxesRepository extends JpaRepository<TranxPurInvoiceDutiesTaxes, Long> {
    List<TranxPurInvoiceDutiesTaxes> findByPurchaseTransactionAndStatus(TranxPurInvoice mPurchaseTranx,
                                                                        Boolean status);


    List<TranxPurInvoiceDutiesTaxes> findByPurchaseTransactionIdAndStatus(Long id, boolean b);

    @Query(
            value = "SELECT duties_taxes_ledger_id FROM tranx_purchase_invoice_duties_taxes_tbl WHERE" +
                    " purchase_invoice_id=?1 AND status =1 ",
            nativeQuery = true

    )

    List<Long> findByDutiesAndTaxesId(Long id);
}
