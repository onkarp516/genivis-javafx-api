package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxPurOrderRepository extends JpaRepository<TranxPurOrder, Long> {
    @Query(
            value = "SELECT COUNT(*) FROM tranx_purchase_order_tbl WHERE outlet_id=?1 And " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long outletId);

    TranxPurOrder findByIdAndStatus(long poInvoiceId, boolean b);

    List<TranxPurOrder> findAllByStatus(boolean b);

    TranxPurOrder findByIdAndOutletIdAndStatus(Long id, Long id1, boolean b);

    List<TranxPurOrder> findBySundryCreditorsIdAndStatusAndTransactionStatusId(Long id, boolean b, long l);

    List<TranxPurOrder> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_purchase_order_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(invoice_date) BETWEEN ?3 AND ?4 AND status=?5 ORDER by invoice_date DESC", nativeQuery = true)
    List<TranxPurOrder> findPurchaseOrderListWithDate(Long id, Long id1, LocalDate startDate, LocalDate endDate, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_purchase_order_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "DATE(invoice_date) BETWEEN ?3 AND ?4 ORDER by invoice_date",
            nativeQuery = true)
    List<TranxPurOrder> findorder(Long id, Long branchId, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT * FROM `tranx_purchase_order_tbl` WHERE outlet_id=?1 AND DATE(invoice_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by invoice_date DESC", nativeQuery = true)
    List<TranxPurOrder> findPurchaseOrderListWithDateNoBr(Long id, LocalDate startDate, LocalDate endDate, boolean b);

    @Query(
            value = "SELECT COUNT(*) FROM tranx_purchase_order_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long outletId, Long branchId);

    @Query(
            value = "SELECT * FROM tranx_purchase_order_tbl WHERE outlet_id=?1 AND " +
                    "DATE(invoice_date) BETWEEN ?2 AND ?3 AND branch_id IS NULL ORDER by invoice_date",
            nativeQuery = true)
    List<TranxPurOrder> findordersNoBr(Long id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_purchase_order_tbl` WHERE outlet_id=?1 AND branch_id IS NULL AND DATE(invoice_date) " +
                    "BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true
    )
    Double findTotalOrderAmtNoBranch(Long id, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);


    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_purchase_order_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND DATE(invoice_date) " +
                    "BETWEEN ?3 AND ?4 AND status=?5", nativeQuery = true
    )
    Double findTotalOrderAmtwithBr(Long outletId, Long branchId, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    List<TranxPurOrder> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    TranxPurOrder findByIdAndStatusAndTransactionStatusId(long id, boolean b, Long tranxStatus);

    TranxPurOrder findByIdAndOutletIdAndBranchIdAndStatus(Long id, Long id1, Long id2, boolean b);

    TranxPurOrder findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);

    TranxPurOrder findByOutletIdAndBranchIdAndSundryCreditorsIdAndStatusAndVendorInvoiceNoIgnoreCase(Long id, Long id1, Long sundryCreditorId, boolean b, String bill_no);

    TranxPurOrder findByOutletIdAndSundryCreditorsIdAndStatusAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(Long id, Long sundryCreditorId, boolean b, String bill_no);

    TranxPurOrder findByOutletIdAndBranchIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCase(Long id, Long id1, Long sundryCreditorId, String bill_no);

    TranxPurOrder findByOutletIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(Long id, Long sundryCreditorId, String bill_no);

    @Query(value = "SELECT COUNT(*) FROM tranx_purchase_order_tbl WHERE sundry_creditors_id=?1 AND " +
            "transaction_status_id=?2 AND status=?3", nativeQuery = true

    )
    Long countOrders(Long id,Long statusId,Boolean status);

    TranxPurOrder findByTransactionTrackingNoAndStatus(String transactionTrackingNo, boolean b);
}
