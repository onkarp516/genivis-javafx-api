package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesReturnDetailsRepository extends
        JpaRepository<TranxSalesReturnInvoiceDetails, Long> {
    @Query(
            value = " SELECT product_id FROM tranx_sales_return_invoice_details_tbl " +
                    "WHERE sales_return_invoice_id=?1 AND status=?2 GROUP BY product_id " , nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);

    List<TranxSalesReturnInvoiceDetails> findByTranxSalesReturnInvoiceIdAndStatus(Long id, boolean b);

    TranxSalesReturnInvoiceDetails findByIdAndStatus(Long detailsId, boolean b);
}
