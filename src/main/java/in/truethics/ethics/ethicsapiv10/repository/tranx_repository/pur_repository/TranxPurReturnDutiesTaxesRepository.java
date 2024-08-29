package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoiceDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPurReturnDutiesTaxesRepository extends JpaRepository<
        TranxPurReturnInvoiceDutiesTaxes, Long> {
    List<TranxPurReturnInvoiceDutiesTaxes> findByPurReturnInvoiceAndStatus(TranxPurReturnInvoice mPurchaseTranx, boolean b);

    @Query(
            value = "SELECT duties_taxes_ledger_id FROM tranx_pur_return_invoice_duties_taxes_tbl WHERE " +
                    "pur_return_invoice_id=?1 AND status =1 ",
            nativeQuery = true

    )
    List<Long> findByDutiesAndTaxesId(Long id);
}
