package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurchaseInvoiceProductSrNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaseInvoiceProductSrNumberRepository extends
        JpaRepository<TranxPurchaseInvoiceProductSrNumber, Long> {

    @Query(value = "select id,serial_no from tranx_purchase_invoice_product_sr_no_tbl where product_id=?1 " +
            "AND pur_invc_unit_details_id=?2 AND status=?3",nativeQuery = true)
    List<Object[]> findSerialnumbers(Long id, Long detailsId,boolean b);

    TranxPurchaseInvoiceProductSrNumber findByIdAndStatus(Long detailsId, boolean b);
}
