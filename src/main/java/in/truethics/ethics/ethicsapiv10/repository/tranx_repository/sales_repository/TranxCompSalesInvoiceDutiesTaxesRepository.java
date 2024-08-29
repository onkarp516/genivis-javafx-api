package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesCompInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesCompInvoiceDutiesTaxes;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxCompSalesInvoiceDutiesTaxesRepository extends JpaRepository
        <TranxSalesCompInvoiceDutiesTaxes,Long> {

//    List<TranxSalesCompInvoiceDutiesTaxes> findBySalesTransactionAndStatus(TranxSalesCompInvoice mSalesTranx, boolean b);
    @Query(
            value = "SELECT duties_taxes_id FROM tranx_sales_comp_duties_taxes_tbl WHERE " +
                    "sales_transaction_id=?1 AND status =1 ",
            nativeQuery = true

    )
    List<Long> findByDutiesAndTaxesId(Long id);

    List<TranxSalesCompInvoiceDutiesTaxes> findBySalesTransactionIdAndStatus(Long id, boolean b);
}
