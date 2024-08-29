package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoice;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.sales.TranxSalesReturnService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxSalesReturnRepository extends JpaRepository<TranxSalesReturnInvoice, Long> {

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_return_invoice_tbl WHERE outlet_id=?1 And " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    List<TranxSalesReturnInvoice> findByOutletIdAndStatusOrderByIdDesc(Long id, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_return_invoice_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long id1);
    @Query(
            value = "SELECT * FROM `tranx_sales_return_invoice_tbl` WHERE outlet_id=?1 AND DATE(transaction_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by transaction_date DESC", nativeQuery = true)
    List<TranxSalesReturnInvoice> findSaleReturnListWithDate(Long id, LocalDate startDatep, LocalDate endDatep, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_sales_return_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(transaction_date) BETWEEN ?3 AND ?4 AND status=?5 ORDER by transaction_date DESC", nativeQuery = true)
    List<TranxSalesReturnInvoice> findSaleReturnListWithDateWithBr(Long id,Long id1, LocalDate startDatep, LocalDate endDatep, boolean b);
    List<TranxSalesReturnInvoice> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    TranxSalesReturnInvoice findByIdAndStatus(long id, boolean b);

    TranxSalesReturnInvoice findByIdAndOutletIdAndBranchIdAndStatus(Long tranx_type, Long id, Long id1, boolean b);

    TranxSalesReturnInvoice findByIdAndOutletIdAndStatus(Long tranx_type, Long id, boolean b);

    List<TranxSalesReturnInvoice> findByTranxSalesInvoiceIdAndStatus(Long id, boolean b);

    List<TranxSalesReturnInvoice> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_sales_return_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "DATE(transaction_date) BETWEEN ?3 AND ?4 ORDER by transaction_date",
            nativeQuery = true)
    List<TranxSalesReturnInvoice> findInvoice(Long id, Long branchId, LocalDate startDate, LocalDate endDate);

   @Query(
            value = "SELECT * FROM `tranx_sales_return_invoice_tbl` WHERE outlet_id=?1 AND " +
                    "DATE(transaction_date) BETWEEN ?2 AND ?3 AND branch_id IS NULL ORDER by transaction_date",
            nativeQuery = true)
    List<TranxSalesReturnInvoice> findInvoicesNoBr(Long id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_return_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(transaction_date) " +
                    "BETWEEN ?3 AND ?4 AND status=?5", nativeQuery = true
    )
    Double findTotalInvoiceAmtwithBr(Long id, Long id1, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_sales_return_invoice_tbl` WHERE outlet_id=?1 AND branch_id IS NULL AND DATE(transaction_date) " +
                    "BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true
    )
    Double findTotalInvoicesAmtNoBranch(Long id, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);
    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0),IFNULL(SUM(total_base_amount),0.0),IFNULL(SUM(total_tax),0.0),COUNT(*) FROM `tranx_sales_return_invoice_tbl` WHERE sundry_debtors_id=?1 AND DATE(transaction_date) BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true)
    List<Object[]> findmobilesumSaleReturnTotalAmt(Long ledgerId, LocalDate startDatep, LocalDate endDatep,boolean b);
    @Query(
            value = "SELECT * FROM `tranx_sales_return_invoice_tbl` WHERE sundry_debtors_id=?1 AND DATE(transaction_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by transaction_date DESC", nativeQuery = true)
    List<TranxSalesReturnInvoice> findSaleReturnInvoicesListNoBr(Long sundryCreditorId, LocalDate startDatep, LocalDate endDatep, boolean b);

    TranxSalesReturnInvoice findBySalesReturnNoAndOutletIdAndBranchIdAndStatus(String invoiceNo, Long id, Long id1, boolean b);

    TranxSalesReturnInvoice findBySalesReturnNoAndOutletIdAndStatusAndBranchIsNull(String invoiceNo, Long id, boolean b);

    TranxSalesReturnInvoice findByOutletIdAndBranchIdAndSalesReturnNoIgnoreCase(Long id, Long id1, String salesInvoiceNo);

    TranxSalesReturnInvoice findByOutletIdAndSalesReturnNoIgnoreCaseAndBranchIsNull(Long id, String salesInvoiceNo);

    TranxSalesReturnInvoice findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);
}
