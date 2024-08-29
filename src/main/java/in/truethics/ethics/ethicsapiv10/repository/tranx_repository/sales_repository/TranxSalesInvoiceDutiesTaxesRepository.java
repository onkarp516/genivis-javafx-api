package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesInvoiceDutiesTaxesRepository extends JpaRepository
        <TranxSalesInvoiceDutiesTaxes,Long> {
    List<TranxSalesInvoiceDutiesTaxes> findBySalesTransactionAndStatus(TranxSalesInvoice mSalesTranx, boolean b);

    @Query(
            value = "SELECT duties_taxes_ledger_id FROM tranx_sales_duties_taxes_tbl WHERE" +
                    " sales_invoice_id=?1 AND status =1 ",
            nativeQuery = true

    )
    List<Long> findByDutiesAndTaxesId(Long id);

//    List<TranxSalesInvoiceDutiesTaxes> findBySalesInvoiceAndStatus(TranxSalesInvoice mSalesTranx, boolean b);
}
