package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoiceDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesReturnDutiesTaxesRepository extends
        JpaRepository<TranxSalesReturnInvoiceDutiesTaxes, Long> {
    List<TranxSalesReturnInvoiceDutiesTaxes> findByTranxSalesReturnInvoiceIdAndStatus(Long id, boolean b);

    @Query(
            value = "SELECT duties_taxes_ledger_id FROM tranx_sales_return_duties_taxes_tbl WHERE " +
                    "sales_return_invoice_id=?1 AND status =1 ",
            nativeQuery = true

    )
    List<Long> findByDutiesAndTaxesId(Long id);

    List<TranxSalesReturnInvoiceDutiesTaxes> findByTranxSalesReturnInvoiceAndStatus(TranxSalesReturnInvoice invoiceTranx, boolean b);
}
