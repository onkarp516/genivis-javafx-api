package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesCompInvoiceProductSrNumber;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceProductSrNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesCompInvoicePrSrNoRepository extends
        JpaRepository<TranxSalesCompInvoiceProductSrNumber,Long> {

    TranxSalesCompInvoiceProductSrNumber findByIdAndStatus(Long detailsId, boolean b);
    @Query(value = "select id,serial_no from tranx_sales_comp_invoice_product_sr_no_tbl where product_id=?1 " +
            "AND sales_inv_unit_details_id=?2 AND status=?3",nativeQuery = true)
    List<Object[]> findSerialnumbers(Long id, Long id1, boolean b);
}
