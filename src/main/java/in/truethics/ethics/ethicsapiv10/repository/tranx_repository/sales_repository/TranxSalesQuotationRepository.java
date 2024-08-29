package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxSalesQuotationRepository extends JpaRepository<TranxSalesQuotation, Long> {
    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_quotation_tbl WHERE outlet_id=?1 And " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    List<TranxSalesQuotation> findAllByStatus(boolean b);
    List<TranxSalesQuotation> findByOutletIdAndStatus(Long id, boolean b);

    TranxSalesQuotation findByIdAndStatus(long parseLong, boolean b);

    List<TranxSalesQuotation> findByOutletIdAndStatusOrderByIdDesc(Long id, boolean b);

    TranxSalesQuotation findByIdAndOutletIdAndStatus(long id, Long id1, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_quotation_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id,Long branchId);
    @Query(
            value = "SELECT * FROM `tranx_sales_quotation_tbl` WHERE outlet_id=?1 AND DATE(bill_date) BETWEEN ?2 AND ?3 AND status=?4 ORDER by DATE(bill_date) DESC", nativeQuery = true)
    List<TranxSalesQuotation> findSaleQuotationListWithDate(Long id, LocalDate startDatep, LocalDate endDatep, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_sales_quotation_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND bill_date BETWEEN ?3 AND ?4 AND status=?5 ORDER by DATE(bill_date) DESC", nativeQuery = true)
    List<TranxSalesQuotation> findSaleQuotationListWithDateWithBr(Long id,Long id1, LocalDate startDatep, LocalDate endDatep, boolean b);
    List<TranxSalesQuotation> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    List<TranxSalesQuotation> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    TranxSalesQuotation findByIdAndStatusAndTransactionStatusId(long id, boolean b, long l);

    TranxSalesQuotation findByIdAndOutletIdAndBranchIdAndStatus(Long id, Long id1, Long id2, boolean b);

    TranxSalesQuotation findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);

    @Query(value = "SELECT COUNT(*) FROM tranx_sales_quotation_tbl WHERE sundry_debtors_id=?1 AND " +
            "transaction_status_id=?2 AND status=?3", nativeQuery = true

    )
    Long countquotation(Long id, long l,Boolean status);



    List<TranxSalesQuotation> findBySundryDebtorsIdAndStatusAndTransactionStatusId(Long id, boolean b, long l);

    @Query(
            value = "SELECT * FROM tranx_sales_quotation_tbl WHERE outlet_id=?1 AND branch_id=?2 AND sq_bill_no=?3", nativeQuery = true
    )
    TranxSalesQuotation findSqNoWithBranch(Long id, Long id1, String salesInvoiceNo);

    @Query(
            value = "SELECT * FROM tranx_sales_quotation_tbl WHERE outlet_id=?1 AND sq_bill_no=?2 AND branch_id IS NULL", nativeQuery = true
    )

    TranxSalesQuotation findSqNo(Long id, String salesInvoiceNo);
}