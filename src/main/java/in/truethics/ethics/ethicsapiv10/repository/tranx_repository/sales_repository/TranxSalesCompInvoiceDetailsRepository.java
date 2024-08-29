package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesCompInvoiceDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesCompInvoiceDetailsRepository extends JpaRepository<TranxSalesCompInvoiceDetails, Long> {
    List<TranxSalesCompInvoiceDetails> findBySalesInvoiceIdAndStatus(long sales_invoice_id, boolean b);

    @Query(
            value = " SELECT * FROM tranx_sales_comp_invoice_details_tbl " +
                    "WHERE sales_invoice_id=?1 AND status=?2 AND id IN(?3) "
            , nativeQuery = true
    )
    List<TranxSalesCompInvoiceDetails> findSalesCompBillByIdWithProductsId(Long id, boolean b, String str);


    TranxSalesCompInvoiceDetails findBySalesInvoiceIdAndProductIdAndStatus(Long id, Long id1, boolean b);

    TranxSalesCompInvoiceDetails findByIdAndStatus(Long detailsId, boolean b);
}
