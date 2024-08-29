package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxPurReturnsRepository extends JpaRepository<TranxPurReturnInvoice, Long> {

    @Query(
            value = " SELECT COUNT(*) FROM tranx_pur_return_invoice_tbl WHERE outlet_id=?1 And " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);


    @Query(
            value = " SELECT COUNT(*) FROM tranx_pur_return_invoice_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long branchId);

    List<TranxPurReturnInvoice> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_pur_return_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND pur_return_date BETWEEN ?3 AND ?4 AND status=?5 ORDER by pur_return_date DESC", nativeQuery = true)
    List<TranxPurReturnInvoice> findPurchaseReturnListWithDate(Long id, Long id1, LocalDate startDate, LocalDate endDate, boolean b);
    @Query(
            value = "SELECT * FROM `tranx_pur_return_invoice_tbl` WHERE outlet_id=?1 AND pur_return_date BETWEEN ?2 AND ?3 AND status=?4 ORDER by pur_return_date DESC", nativeQuery = true)
    List<TranxPurReturnInvoice> findPurchaseReturnListWithDateNoBr(Long id,LocalDate startDate, LocalDate endDate, boolean b);

    TranxPurReturnInvoice findByIdAndStatus(long id, boolean b);


    TranxPurReturnInvoice findByIdAndOutletIdAndBranchIdAndStatus(Long tranx_type, Long id, Long id1, boolean b);

    TranxPurReturnInvoice findByIdAndOutletIdAndStatus(Long tranx_type, Long id, boolean b);

    List<TranxPurReturnInvoice> findByTranxPurInvoiceIdAndStatus(Long id, boolean b);

    List<TranxPurReturnInvoice> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);


    TranxPurReturnInvoice findByOutletIdAndBranchIdAndSundryCreditorsIdAndPurRtnNoIgnoreCase(Long id, Long id1, Long sundryCreditorId, String billNo);

    TranxPurReturnInvoice findByOutletIdAndSundryCreditorsIdAndPurRtnNoIgnoreCaseAndBranchIsNull(Long id, Long sundryCreditorId, String billNo);


    @Query(
            value = "SELECT * FROM `tranx_pur_return_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "pur_return_date BETWEEN ?3 AND ?4 ORDER by pur_return_date",
            nativeQuery = true)
    List<TranxPurReturnInvoice> findInvoice(Long id, Long branchId, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT * FROM `tranx_pur_return_invoice_tbl` WHERE outlet_id=?1 AND " +
                    "pur_return_date BETWEEN ?2 AND ?3 AND branch_id IS NULL ORDER by pur_return_date",
            nativeQuery = true)
    List<TranxPurReturnInvoice> findInvoicesNoBr(Long id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_pur_return_invoice_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND pur_return_date " +
                    "BETWEEN ?3 AND ?4 AND status=?5", nativeQuery = true
    )
    Double findTotalInvoiceAmtwithBr(Long outletId, Long branchId, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0) FROM " +
                    "`tranx_pur_return_invoice_tbl` WHERE outlet_id=?1 AND branch_id IS NULL AND pur_return_date " +
                    "BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true
    )
    Double findTotalInvoicesAmtNoBranch(Long id, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(total_amount),0.0),IFNULL(SUM(total_base_amount),0.0),IFNULL(SUM(total_tax),0.0),COUNT(*) FROM `tranx_pur_return_invoice_tbl` WHERE sundry_creditors_id=?1 AND pur_return_date BETWEEN ?2 AND ?3 AND status=?4", nativeQuery = true)
    List<Object[]> findmobilePurReturnTotalAmt(Long ledgerId, LocalDate startDatep, LocalDate endDatep,boolean b);
    @Query(
            value = "SELECT * FROM `tranx_pur_return_invoice_tbl` WHERE sundry_creditors_id=?1 AND pur_return_date BETWEEN ?2 AND ?3 AND status=?4 ORDER by pur_return_date DESC", nativeQuery = true)
    List<TranxPurReturnInvoice> findPurReturnInvoicesListNoBr(Long sundryCreditorId, LocalDate startDatep, LocalDate endDatep, boolean b);

    TranxPurReturnInvoice findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);
}
