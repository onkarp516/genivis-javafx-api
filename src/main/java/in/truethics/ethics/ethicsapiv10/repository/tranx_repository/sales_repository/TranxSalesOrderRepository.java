package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxSalesOrderRepository extends JpaRepository<TranxSalesOrder, Long> {
    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_order_tbl WHERE outlet_id=?1 And " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    TranxSalesOrder findByIdAndStatus(long parseLong, boolean b);

    List<TranxSalesOrder> findByOutletIdAndStatus(Long id, boolean b);

    TranxSalesOrder findByIdAndOutletIdAndStatus(long id, Long id1, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_order_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id,Long branchId);
    @Query(
            value = "SELECT * FROM `tranx_sales_order_tbl` WHERE outlet_id=?1 AND DATE(bill_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by bill_date DESC", nativeQuery = true)
    List<TranxSalesOrder> findSaleOrderListWithDate(Long id, LocalDate startDatep, LocalDate endDatep, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_sales_order_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(bill_date) BETWEEN ?3 AND ?4 AND status=?5 ORDER by bill_date DESC", nativeQuery = true)
    List<TranxSalesOrder> findSaleOrderListWithDateWithBr(Long id,Long id1, LocalDate startDatep, LocalDate endDatep, boolean b);
    List<TranxSalesOrder> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    TranxSalesOrder findByIdAndOutletIdAndBranchIdAndStatus(Long id, Long id1, Long id2, boolean b);

    List<TranxSalesOrder> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    TranxSalesOrder findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);

    TranxSalesOrder findByIdAndStatusAndTransactionStatusId(long id, boolean b, long l);

    @Query(value = "SELECT COUNT(*) FROM tranx_sales_order_tbl WHERE sundry_debtors_id=?1 AND " +
            "transaction_status_id=?2 AND status=?3", nativeQuery = true

    )
    Long countOrders(Long id, long l,Boolean status);



    List<TranxSalesOrder> findBySundryDebtorsIdAndStatusAndTransactionStatusId(Long id, boolean b, long l);

    @Query(
            value = "SELECT * FROM tranx_sales_order_tbl WHERE outlet_id=?1 AND branch_id=?2 AND so_bill_no=?3", nativeQuery = true
    )
    TranxSalesOrder findBySoBillWithBranch(Long id, Long id1, String salesQuotationNo);

    @Query(
            value = "SELECT * FROM tranx_sales_order_tbl WHERE outlet_id=?1 AND so_bill_no=?2 AND branch_id IS NULL", nativeQuery = true
    )
    TranxSalesOrder findBySoBill(Long id, String salesQuotationNo);

    @Query(
            value = "SELECT * FROM `tranx_sales_order_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "DATE(bill_date) BETWEEN ?3 AND ?4 ORDER by bill_date",
            nativeQuery = true)
    List<TranxSalesOrder> findInvoices(Long outletId, Long branchId,
                                         LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT * FROM `tranx_sales_order_tbl` WHERE outlet_id=?1 AND " +
                    "DATE(bill_date) BETWEEN ?2 AND ?3 AND branch_id IS NULL ORDER by bill_date",
            nativeQuery = true)
    List<TranxSalesOrder> findInvoicesNoBr(Long id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_order_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(bill_date) " +
                    "BETWEEN ?3 AND ?4 AND status=?5", nativeQuery = true
    )
    Double findTotalInvoiceAmtwithBr(Long id, Long id1, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_order_tbl` WHERE outlet_id=?1 AND branch_id IS NULL AND DATE(bill_date) " +
                    "BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true
    )
    Double findTotalInvoicesAmtNoBranch(Long id, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

}
