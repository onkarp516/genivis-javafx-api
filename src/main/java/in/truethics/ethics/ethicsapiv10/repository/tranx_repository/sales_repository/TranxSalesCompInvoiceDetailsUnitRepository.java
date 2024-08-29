package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesCompInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetailsUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxSalesCompInvoiceDetailsUnitRepository extends JpaRepository<TranxSalesCompInvoiceDetailsUnits, Long> {
    TranxSalesCompInvoiceDetailsUnits findByIdAndStatus(Long details_id, boolean b);

    List<TranxSalesCompInvoiceDetailsUnits> findBySalesInvoiceIdAndStatus(long transactionId, boolean b);


    @Query(
            value = " SELECT product_id FROM tranx_sales_comp_details_units_tbl " +
                    "WHERE sales_invoice_id=?1 AND status=?2 GROUP BY product_id ", nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);

    List<TranxSalesCompInvoiceDetailsUnits> findByProductIdAndStatusOrderByIdDesc(Long productId, boolean b);

    @Query(
            value = " SELECT IFNULL(avg(rate),0.0) FROM tranx_sales_comp_details_units_tbl WHERE product_id=?1 and unit_id=?2", nativeQuery = true
    )
    Double findTotalValue(Long productId, Long id);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) FROM `tranx_sales_comp_details_units_tbl` " +
                    "WHERE product_id=?1 AND batch_id=?2 AND DATE(created_at) BETWEEN ?3 AND ?4", nativeQuery = true
    )
    Double findExpiredSalesSumQty(Long productId, Long batchId, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = " SELECT IFNULL(avg(total_amount),0.0) FROM tranx_sales_comp_details_units_tbl " +
                    "WHERE product_id=?1 and batch_id=?2", nativeQuery = true
    )
    Double findTotalBatchValue(Long productId, Long batchId);

    TranxSalesCompInvoiceDetailsUnits findBySalesInvoiceIdAndProductIdAndProductBatchNoId(Long id, Long productId, Long batchId);

    @Query(
            value = "SELECT * FROM `tranx_sales_comp_details_units_tbl` WHERE sales_invoice_id=?1 AND status=?2", nativeQuery = true)
    List<TranxSalesInvoiceDetailsUnits> findSalesInvoicesDetails(Long id, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_comp_details_units_tbl WHERE "+
                    "batch_id=?1 AND status=?2", nativeQuery = true
    )
    Long countBatchExists(Long batch_id, Boolean status);
}
