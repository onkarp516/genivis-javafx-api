package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetailsUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesInvoiceDetailsRepository extends JpaRepository<TranxSalesInvoiceDetails, Long> {
    List<TranxSalesInvoiceDetails> findBySalesInvoiceIdAndStatus(long sales_invoice_id, boolean b);

    @Query(
            value = " SELECT * FROM tranx_sales_invoice_details_tbl " +
                    "WHERE sales_invoice_id=?1 AND status=?2 AND id IN(?3) "
            , nativeQuery = true
    )
    List<TranxSalesInvoiceDetails> findSalesBillByIdWithProductsId(Long id, boolean b, String str);



    TranxSalesInvoiceDetails findByIdAndStatus(Long detailsId, boolean b);

}
