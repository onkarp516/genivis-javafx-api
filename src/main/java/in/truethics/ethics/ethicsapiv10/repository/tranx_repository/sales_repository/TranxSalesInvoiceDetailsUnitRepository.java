package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrderDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetailsUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxSalesInvoiceDetailsUnitRepository extends JpaRepository<TranxSalesInvoiceDetailsUnits, Long> {

    TranxSalesInvoiceDetailsUnits findByIdAndStatus(Long details_id, boolean b);

    List<TranxSalesInvoiceDetailsUnits> findBySalesInvoiceIdAndStatus(long transactionId, boolean b);


    @Query(
            value = " SELECT product_id FROM tranx_sales_invoice_details_units_tbl " +
                    "WHERE sales_invoice_id=?1 AND status=?2 GROUP BY product_id ", nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);

    List<TranxSalesInvoiceDetailsUnits> findByProductIdAndStatusOrderByIdDesc(Long productId, boolean b);

    @Query(
            value = " SELECT IFNULL(avg(rate),0.0) FROM tranx_sales_invoice_details_units_tbl WHERE product_id=?1 and unit_id=?2", nativeQuery = true
    )
    Double findTotalValue(Long productId, Long id);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) FROM `tranx_sales_invoice_details_units_tbl` " +
                    "WHERE product_id=?1 AND batch_id=?2 AND DATE(created_at) BETWEEN ?3 AND ?4", nativeQuery = true
    )
    Double findExpiredSalesSumQty(Long productId, Long batchId, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = " SELECT IFNULL(avg(total_amount),0.0) FROM tranx_sales_invoice_details_units_tbl " +
                    "WHERE product_id=?1 and batch_id=?2", nativeQuery = true
    )
    Double findTotalBatchValue(Long productId, Long batchId);

    TranxSalesInvoiceDetailsUnits findBySalesInvoiceIdAndProductIdAndProductBatchNoId(Long id, Long productId, Long batchId);

    @Query(
            value = "SELECT * FROM `tranx_sales_invoice_details_units_tbl` WHERE sales_invoice_id=?1 AND status=?2", nativeQuery = true)
    List<TranxSalesInvoiceDetailsUnits> findSalesInvoicesDetails(Long id, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_invoice_details_units_tbl WHERE " +
                    "batch_id=?1 AND status=?2", nativeQuery = true
    )
    Long countBatchExists(Long batch_id, Boolean status);

    TranxSalesInvoiceDetailsUnits findBySalesInvoiceIdAndStatusAndProductId(Long id, boolean b, Long id1);

    @Query(
            value = "SELECT count(sales_invoice_id) FROM tranx_sales_invoice_details_units_tbl WHERE sales_invoice_id=?1 AND transaction_status_id=?2 " +
                    "AND status=?3", nativeQuery = true)
    Double totalinvoiceNumberOfProduct(Long id, int i, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(a.qty),0.0),IFNULL(SUM(a.rate),0.0) FROM tranx_sales_invoice_details_units_tbl AS a LEFT JOIN" +
                    " tranx_sales_invoice_tbl ON a.sales_invoice_id=tranx_sales_invoice_tbl.id WHERE product_id=?1" +
                    " AND a.status=?2 AND tranx_sales_invoice_tbl.bill_date BETWEEN ?3 AND ?4", nativeQuery = true
    )
    String findSalesQtyByProductIdAndStatus(Long productId, boolean b, String toString, String toString1);

    @Query(
            value = "SELECT IFNULL(SUM(a.qty),0.0),IFNULL(SUM(a.rate),0.0), a.unit_id FROM tranx_sales_return_details_units_tbl AS a " +
                    "LEFT JOIN tranx_sales_return_invoice_tbl ON a.sales_return_id=tranx_sales_return_invoice_tbl.id WHERE product_id=?1 AND a.status=?2 AND " +
                    "tranx_sales_return_invoice_tbl.transaction_date BETWEEN ?3 AND ?4", nativeQuery = true
    )
    String findSalesReturnQtyByProductIdAndStatus(Long productId, boolean b, String toString, String toString1);


    @Query(
            value = "SELECT * FROM tranx_sales_invoice_details_units_tbl AS a LEFT JOIN tranx_sales_invoice_tbl AS b " +
                    "ON a.sales_invoice_id=b.id LEFT JOIN product_tbl on a.product_id=product_tbl.id WHERE " +
                    "product_tbl.product_code=?1 AND b.status=?2 AND b.id=?3", nativeQuery = true
    )
    TranxSalesInvoiceDetailsUnits findProductBatch(String product_code, Boolean status, Long salesInvoiceId);

    TranxSalesInvoiceDetailsUnits findBySalesInvoiceIdAndProductIdAndStatus(Long id, Long productId, boolean b);
}