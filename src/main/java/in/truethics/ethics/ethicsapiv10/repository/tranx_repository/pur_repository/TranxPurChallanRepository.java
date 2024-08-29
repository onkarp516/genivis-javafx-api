package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxPurChallanRepository extends JpaRepository<TranxPurChallan, Long> {


    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_purchase_challan_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(invoice_date) " +
                    "BETWEEN ?3 AND ?4 AND status=?5", nativeQuery = true
    )
    Double findTotalchallanAmtwithBr(Long outletId, Long branchId, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_purchase_challan_tbl` WHERE outlet_id=?1 AND branch_id IS NULL AND DATE(invoice_date) " +
                    "BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true
    )
    Double findTotalchallanAmtNoBranch(Long id, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);


    @Query(
            value = "SELECT * FROM `tranx_purchase_challan_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "DATE(invoice_date) BETWEEN ?3 AND ?4 ORDER by invoice_date",
            nativeQuery = true)
    List<TranxPurChallan> findChallan(Long id, Long branchId, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT * FROM `tranx_purchase_challan_tbl` WHERE outlet_id=?1 AND " +
                    "DATE(invoice_date) BETWEEN ?2 AND ?3 AND branch_id IS NULL ORDER by invoice_date",
            nativeQuery = true)
    List<TranxPurChallan> findChallanNoBr(Long id, LocalDate startDate, LocalDate endDate);
    @Query(
            value = " SELECT COUNT(*) FROM tranx_purchase_challan_tbl WHERE outlet_id=?1 And " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long outletId);

    List<TranxPurChallan> findAllByStatus(boolean b);

    TranxPurChallan findByIdAndStatus(long poChallanInvoiceId, boolean b);

    List<TranxPurChallan> findBySundryCreditorsIdAndStatus(Long id, boolean b);

    @Query(
            value = " SELECT * FROM tranx_purchase_challan_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_creditors_id=?3 AND DATE(a.invoice_date) BETWEEN ?4 AND ?5",
            nativeQuery = true
    )
    List<TranxPurChallan> findBySuppliersWithDates(
            Long id, boolean status, long sundry_creditor_id, String dateFrom, String dateTo);

    List<TranxPurChallan> findBySundryCreditorsIdAndOutletIdAndTransactionStatusIdAndStatus(
            Long ledgerId, Long id, long l, boolean b);

    TranxPurChallan findByIdAndOutletIdAndStatus(Long id, Long id1, boolean b);

    List<TranxPurChallan> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);
    @Query(
            value = "SELECT * FROM `tranx_purchase_challan_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(invoice_date) BETWEEN ?3 AND ?4 AND status=?5 ORDER by invoice_date DESC", nativeQuery = true)
    List<TranxPurChallan> findPurchaseChallanListWithDate(Long id, Long id1, LocalDate startDate, LocalDate endDate, boolean b);
    @Query(
            value = "SELECT * FROM `tranx_purchase_challan_tbl` WHERE outlet_id=?1 AND DATE(invoice_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by invoice_date DESC", nativeQuery = true)
    List<TranxPurChallan> findPurchaseChallanListWithDateNoBr(Long id,LocalDate startDate, LocalDate endDate, boolean b);
    @Query(
            value = " SELECT COUNT(*) FROM tranx_purchase_challan_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long outletId, Long branchId);

    List<TranxPurChallan> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    TranxPurChallan findByOutletIdAndBranchIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCase(Long id, Long id1, Long sundryCreditorId, String bill_no);

    TranxPurChallan findByOutletIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(Long id, Long sundryCreditorId, String bill_no);


    TranxPurChallan findByIdAndStatusAndTransactionStatusId(long id, boolean b, long l);

    TranxPurChallan findByIdAndOutletIdAndBranchIdAndStatus(Long id, Long id1, Long id2, boolean b);

    TranxPurChallan findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);

    @Query(value = "SELECT COUNT(*) FROM tranx_purchase_challan_tbl WHERE sundry_creditors_id=?1 AND " +
            "transaction_status_id=?2 AND status=?3", nativeQuery = true

    )
    Long countChallan(Long id, long l,Boolean status);

    List<TranxPurChallan> findBySundryCreditorsIdAndStatusAndTransactionStatusId(Long id, boolean b, long l);

    TranxPurChallan findByTransactionTrackingNoAndStatus(String transactionTrackingNo, boolean b);
}
