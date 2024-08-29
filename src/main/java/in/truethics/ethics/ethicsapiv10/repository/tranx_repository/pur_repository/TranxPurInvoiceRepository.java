package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxPurInvoiceRepository extends JpaRepository<TranxPurInvoice, Long> {
    @Query(
            value = " SELECT COUNT(*) FROM tranx_purchase_invoice_tbl WHERE outlet_id=?1 And " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    TranxPurInvoice findByIdAndOutletIdAndStatus(Long id, Long id1, boolean b);


    @Query(
            value = " SELECT * FROM tranx_purchase_invoice_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_creditors_id=?3 AND date(a.invoice_date) BETWEEN ?4 AND ?5 AND branch_id IS NULL",
            nativeQuery = true
    )
    List<TranxPurInvoice> findBySuppliersWithDates(
            Long id, boolean status, long sundry_creditor_id, String dateFrom, String dateTo);


    @Query(
            value = " SELECT * FROM tranx_purchase_invoice_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_creditors_id=?3 AND date(a.invoice_date) BETWEEN ?4 AND ?5 AND branch_id=?6 ",
            nativeQuery = true
    )
    List<TranxPurInvoice> findByBranchSuppliersWithDates(
            Long id, boolean status, long sundry_creditor_id, String dateFrom, String dateTo, Long branchId);

    TranxPurInvoice findByIdAndStatus(long pur_invoice_id, boolean b);

    @Query(
            value = " SELECT * FROM tranx_purchase_invoice_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_creditors_id=?3 And balance>0 And a.branch_id IS NULL And DATE(invoice_date) BETWEEN ?4 AND ?5",
            nativeQuery = true
    )
    List<TranxPurInvoice> findPRPendingBillsWithDates(Long outletId, boolean b, Long ledgerId,
                                                      LocalDate startDate, LocalDate endDate);

    /*@Query(
            value = " SELECT * FROM tranx_purchase_invoice_tbl as a WHERE a.outlet_id=?1 And a.status=?2 " +
                    "And a.sundry_creditors_id=?3 And balance>0 And a.branch_id IS NULL",
            nativeQuery = true
    )
    List<TranxPurInvoice> findPRPendingBillsWithDates(Long outletId, boolean b, Long ledgerId,
                                                      LocalDate startDate, LocalDate endDate);
*/
    @Query(
            value = " SELECT * FROM tranx_purchase_invoice_tbl WHERE outlet_id=?1 And status=?2 " +
                    "And sundry_creditors_id=?3 And balance>0 And branch_id IS NULL",
            nativeQuery = true
    )
    List<TranxPurInvoice> findPRPendingBills(Long outletId, boolean b, Long ledgerId);

    @Query(
            value = " SELECT * FROM tranx_purchase_invoice_tbl as a WHERE a.outlet_id=?1 And a.branch_id=?2 And a.status=?3 " +
                    "And a.sundry_creditors_id=?4 And balance>0 AND DATE(invoice_date) BETWEEN ?5 AND ?6 ",
            nativeQuery = true)
    List<TranxPurInvoice> findPendingBillsByBranchWithDates(Long outletId, Long branchId, boolean b, Long ledgerId,
                                                            LocalDate startDate, LocalDate endDate);

    @Query(
            value = " SELECT * FROM tranx_purchase_invoice_tbl WHERE outlet_id=?1 And branch_id=?2 And status=?3 " +
                    "And sundry_creditors_id=?4 And balance>0",
            nativeQuery = true
    )
    List<TranxPurInvoice> findPendingBillsByBranch(Long outletId, Long branchId, boolean b, Long ledgerId);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_purchase_invoice_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long outletId, Long branchId);

    List<TranxPurInvoice> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    List<TranxPurInvoice> findByOutletIdAndBranchIdAndStatusAndSundryCreditorsId(Long id, Long id1, boolean b, Long ledgerId);


    TranxPurInvoice findByIdAndOutletIdAndBranchIdAndStatus(Long tranx_type, Long id, Long id1, boolean b);

    TranxPurInvoice findByOutletIdAndBranchIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCase(Long id, Long id1, Long sundryCreditorId, String bill_no);

    TranxPurInvoice findByOutletIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(Long id, Long sundryCreditorId, String bill_no);

    List<TranxPurInvoice> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_purchase_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    " DATE(invoice_date) BETWEEN ?3 AND ?4 ORDER by invoice_date",
            nativeQuery = true)
    List<TranxPurInvoice> findInvoices(Long id, Long branchId, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT * FROM `tranx_purchase_invoice_tbl` WHERE outlet_id=?1 AND " +
                    " DATE(invoice_date) BETWEEN ?2 AND ?3 AND branch_id IS NULL ORDER by invoice_date",
            nativeQuery = true)
    List<TranxPurInvoice> findInvoicesNoBr(Long id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_purchase_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(invoice_date) " +
                    "BETWEEN ?3 AND ?4 AND status=?5", nativeQuery = true
    )
    Double findTotalInvoiceAmtwithBr(Long outletId, Long branchId, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_purchase_invoice_tbl` WHERE outlet_id=?1 AND branch_id IS NULL AND DATE(invoice_date) " +
                    "BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true
    )
    Double findTotalInvoicesAmtNoBranch(Long id, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_purchase_invoice_tbl` WHERE sundry_creditors_id=?1 AND  DATE(invoice_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by invoice_date DESC", nativeQuery = true)
    List<TranxPurInvoice> findPurchaseInvoicesListNoBr(Long id, LocalDate startDate, LocalDate endDate, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_purchase_invoice_tbl` WHERE outlet_id=?1 AND  DATE(invoice_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by invoice_date DESC", nativeQuery = true)
    List<TranxPurInvoice> findPurchaseInvoicesListWithDateNoBr(Long id, LocalDate startDate, LocalDate endDate, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_purchase_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND  DATE(invoice_date) BETWEEN ?3 AND ?4 AND status=?5 ORDER by invoice_date DESC", nativeQuery = true)
    List<TranxPurInvoice> findPurchaseInvoicesListWithDate(Long id, Long id1, LocalDate startDate, LocalDate endDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0),IFNULL(SUM(total_base_amount),0.0),IFNULL(SUM(total_tax),0.0),COUNT(*) FROM `tranx_purchase_invoice_tbl` WHERE sundry_creditors_id=?1 AND  DATE(invoice_date) BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true)
    List<Object[]> findmobilePurchaseTotalAmt(Long ledgerId, LocalDate startDatep, LocalDate endDatep, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_purchase_invoice_tbl` WHERE  DATE(invoice_date)=?1 AND status=?2", nativeQuery = true
    )
    Double findPurTotalAmtByStatus(LocalDate startMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_purchase_invoice_tbl` WHERE  DATE(invoice_date)=?1 AND status=?2 AND outlet_id=?3", nativeQuery = true
    )
    Double findPurTotalAmtByStatusOutlet(LocalDate startMonthDate, boolean b, Long id);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_purchase_invoice_tbl` WHERE  DATE(invoice_date) BETWEEN ?1 AND ?2 AND status=?3", nativeQuery = true
    )
    Double findPurTotalWeekAmtByStatus(LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(balance),0.0),COUNT(*) FROM `tranx_purchase_invoice_tbl` WHERE sundry_creditors_id=?1" +
                    " AND  DATE(invoice_date) BETWEEN ?2 AND ?3 AND balance>0 AND status=?4", nativeQuery = true)
    List<Object[]> findmobilePayableTotalAmt(Long ledgerId, LocalDate startDatep, LocalDate endDatep, boolean b);

    TranxPurInvoice findByVendorInvoiceNoAndOutletIdAndBranchIdAndStatus(String invoiceNo, Long id, Long id1, boolean b);

    TranxPurInvoice findByVendorInvoiceNoAndOutletIdAndStatusAndBranchIsNull(String invoiceNo, Long id, boolean b);

    TranxPurInvoice findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);

    TranxPurInvoice findByOutletIdAndBranchIdAndSundryCreditorsIdAndStatusAndVendorInvoiceNoIgnoreCase(Long id, Long id1, Long sundryCreditorId, boolean b, String bill_no);

    TranxPurInvoice findByOutletIdAndSundryCreditorsIdAndStatusAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(Long id, Long sundryCreditorId, boolean b, String bill_no);

    List<TranxPurInvoice> findByOutletIdAndStatusAndSundryCreditorsIdAndBranchIsNull(Long id, boolean b, Long ledgerId);

    @Query(
            value = "SELECT lmt.ledger_name, tpit.sundry_creditors_id, tpit.vendor_invoice_no, tpit.balance," +
                    " lmt.credit_days, lmt.balancing_method_id, tpit.total_amount, tppdt.paid_amt," +
                    " tppdt.remaining_amt, DATE(tpit.invoice_date), tppdt.balancing_type from `tranx_purchase_invoice_tbl` as tpit LEFT JOIN ledger_master_tbl AS lmt" +
                    " ON tpit.sundry_creditors_id = lmt.id LEFT JOIN tranx_payment_perticulars_details_tbl as tppdt ON" +
                    " tpit.sundry_creditors_id = tppdt.ledger_id AND tpit.id = tppdt.tranx_invoice_id WHERE " +
                    "tpit.sundry_creditors_id =?1 AND tpit.balance != 0 AND DATE(tpit.invoice_date) BETWEEN ?2 AND ?3", nativeQuery = true)
    List<String[]> findByDate(Long Ledger_id, LocalDate startDatep, LocalDate endDatep);
    @Query(
            value = "SELECT tpit.sundry_creditors_id, lmt.ledger_name, lmt.balancing_method_id from`tranx_purchase_invoice_tbl` as tpit LEFT JOIN ledger_master_tbl AS lmt" +
                    " ON tpit.sundry_creditors_id = lmt.id LEFT JOIN tranx_payment_perticulars_details_tbl as tppdt ON" +
                    " tpit.sundry_creditors_id = tppdt.ledger_id AND tpit.id = tppdt.tranx_invoice_id WHERE " +
                    "tpit.balance != 0 AND DATE(tpit.invoice_date) BETWEEN ?1 AND ?2 GROUP BY tpit.sundry_creditors_id", nativeQuery = true)
    List<String[]> findLedgerByDate( LocalDate startDatep, LocalDate endDatep);

}
