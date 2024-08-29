package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallanDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnDetailsUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesReturnDetailsUnitsRepository extends JpaRepository<TranxSalesReturnDetailsUnits,Long> {

    TranxSalesReturnDetailsUnits findByIdAndStatus(Long details_id, boolean b);

    @Query(value = "SELECT COUNT(*) FROM tranx_sales_return_details_units_tbl WHERE " +
            "sales_return_id=?1 AND sales_return_details_id=?2 ",nativeQuery = true)
    Integer findCount(Long purchaseInvoiceId, Long detailsId);

    @Query(value = "SELECT COUNT(*) FROM tranx_sales_return_details_units_tbl WHERE status=0 AND " +
            "sales_return_id=?1 AND sales_return_details_id=?2 ",nativeQuery = true)
    Integer findStatus(Long purchaseInvoiceId, Long detailsId);

    List<TranxSalesReturnDetailsUnits> findBySalesReturnInvoiceIdAndStatus(Long id, boolean b);


    @Query(
            value = " SELECT product_id FROM tranx_sales_return_details_units_tbl " +
                    "WHERE sales_return_id=?1 AND status=?2 GROUP BY product_id " , nativeQuery = true)

    List<Object[]> findByTranxPurId(Long id, boolean b);
    TranxSalesReturnDetailsUnits findBySalesReturnInvoiceIdAndProductIdAndStatus(Long id, Long productId, boolean b);

}
