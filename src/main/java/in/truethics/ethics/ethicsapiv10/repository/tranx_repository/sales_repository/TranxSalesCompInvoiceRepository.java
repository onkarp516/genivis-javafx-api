package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesCompInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesCompInvoiceRepository extends JpaRepository<TranxSalesCompInvoice, Long> {

    TranxSalesCompInvoice findByIdAndOutletIdAndStatus(Long id, Long id1, boolean b);

    TranxSalesCompInvoice findByOutletIdAndBranchIdAndSalesInvoiceNoIgnoreCase(Long id, Long id1, String bill_no);

    TranxSalesCompInvoice findByOutletIdAndSalesInvoiceNoIgnoreCaseAndBranchIdIsNull(Long id, String bill_no);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_comp_invoice_tbl WHERE outlet_id=?1 AND status =1 ", nativeQuery = true
    )
    Long findLastRecord(Long outletId);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_sales_comp_invoice_tbl WHERE outlet_id=?1 AND status =1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long id1);

    TranxSalesCompInvoice findByIdAndStatus(long sales_invoice_id, boolean b);

    TranxSalesCompInvoice findBySalesInvoiceNoAndOutletIdAndBranchIdAndStatus(String invoiceNo, Long id, Long id1, boolean b);

    TranxSalesCompInvoice findByIdAndOutletIdAndBranchIdAndStatus(Long id, Long id1, Long id2, boolean b);
}
