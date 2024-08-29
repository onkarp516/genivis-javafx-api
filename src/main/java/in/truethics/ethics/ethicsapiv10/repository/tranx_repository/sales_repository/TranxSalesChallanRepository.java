package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxSalesChallanRepository extends JpaRepository<TranxSalesChallan, Long> {
    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_challan_tbl where outlet_id=?1 And " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    List<TranxSalesChallan> findByOutletIdAndStatus(Long id, boolean b);

    TranxSalesChallan findByIdAndStatus(long parseLong, boolean b);
    List<TranxSalesChallan> findBySundryDebtorsIdAndStatus(Long id, boolean b);

     @Query(
            value = " SELECT * FROM tranx_sales_invoice_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_debtors_id=?3 AND date(a.invoice_date) BETWEEN ?4 AND ?5",
            nativeQuery = true
    )

    List<TranxSalesChallan> findBySuppliersWithDates(
            Long id, boolean status, long sundry_debtors_id, String dateFrom, String dateTo);

    List<TranxSalesChallan> findBySundryDebtorsIdAndOutletIdAndTransactionStatusIdAndStatus(
            Long ledgerId, Long id, long l, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_challan_tbl where outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id,Long branchId);
    @Query(
            value = "SELECT * FROM `tranx_sales_challan_tbl` WHERE outlet_id=?1 AND DATE(bill_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by bill_date DESC", nativeQuery = true)
    List<TranxSalesChallan> findSaleChallanListWithDate(Long id, LocalDate startDatep, LocalDate endDatep, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_sales_challan_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(bill_date) BETWEEN ?3 AND ?4 AND status=?5 ORDER by bill_date DESC", nativeQuery = true)
    List<TranxSalesChallan> findSaleChallanListWithDateWithBr(Long id,Long id1, LocalDate startDatep, LocalDate endDatep, boolean b);

    List<TranxSalesChallan> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    TranxSalesChallan findByIdAndOutletIdAndStatus(Long id, Long id1, boolean b);

    List<TranxSalesChallan> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    @Query(value = "SELECT COUNT(*) FROM tranx_sales_challan_tbl WHERE sundry_debtors_id=?1 AND " +
            "transaction_status_id=?2 AND status=?3", nativeQuery = true

    )
    Long countChallan(Long id, long l,Boolean status);


    List<TranxSalesChallan> findBySundryDebtorsIdAndStatusAndTransactionStatusId(Long id, boolean b, long l);

    @Query(
            value = "SELECT * FROM tranx_sales_challan_tbl WHERE outlet_id=?1 AND branch_id=?2 AND sc_bill_no=?3", nativeQuery = true
    )

    TranxSalesChallan findByScBillWithBranch(Long id, Long id1, String salesChallanNo);

    @Query(
            value = "SELECT * FROM tranx_sales_challan_tbl WHERE outlet_id=?1 AND sc_bill_no=?2 AND branch_id IS NULL", nativeQuery = true
    )

    TranxSalesChallan findByScBill(Long id, String salesChallanNo);

    TranxSalesChallan findBySalesChallanInvoiceNoAndOutletIdAndBranchIdAndStatus(String invoiceNo, Long id, Long id1, boolean b);

    TranxSalesChallan findByIdAndOutletIdAndBranchIdAndStatus(Long id, Long id1, Long id2, boolean b);


    TranxSalesChallan findByOutletIdAndSalesChallanInvoiceNoIgnoreCaseAndBranchIsNull(Long id, String invoiceNo);

    TranxSalesChallan findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_sales_challan_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "DATE(bill_date) BETWEEN ?3 AND ?4 ORDER by bill_date",
            nativeQuery = true)
    List<TranxSalesChallan> findInvoices(Long outletId, Long branchId,
                                       LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT * FROM `tranx_sales_challan_tbl` WHERE outlet_id=?1 AND " +
                    "DATE(bill_date) BETWEEN ?2 AND ?3 AND branch_id IS NULL ORDER by bill_date",
            nativeQuery = true)
    List<TranxSalesChallan> findInvoicesNoBr(Long id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_Challan_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(bill_date) " +
                    "BETWEEN ?3 AND ?4 AND status=?5", nativeQuery = true
    )
    Double findTotalInvoiceAmtwithBr(Long id, Long id1, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_challan_tbl` WHERE outlet_id=?1 AND branch_id IS NULL AND DATE(bill_date) " +
                    "BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true
    )
    Double findTotalInvoicesAmtNoBranch(Long id, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);
}


