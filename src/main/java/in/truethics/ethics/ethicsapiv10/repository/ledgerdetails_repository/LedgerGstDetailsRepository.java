package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerGstDetails;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LedgerGstDetailsRepository extends JpaRepository<LedgerGstDetails,Long> {
    List<LedgerGstDetails> findByLedgerMasterIdAndStatus(Long ledgerId, boolean b);

    LedgerGstDetails findByIdAndStatus(long id, boolean b);

    @Query(
            value = "SELECT IFNULL(sum(total_amount),0.0),IFNULL(sum(total_base_amount),0.0), IFNULL(sum(total_tax),0.0), " +
                    "IFNULL(sum(totaligst),0.0),  IFNULL(sum(totalsgst),0.0),  IFNULL(sum(totalcgst),0.0)," +
                    "tranx_sales_return_invoice_tbl.sales_return_no,DATE(tranx_sales_return_invoice_tbl.transaction_date)," +
                    " tranx_sales_return_invoice_tbl.id FROM tranx_sales_return_invoice_tbl WHERE tranx_sales_return_invoice_tbl.status=1 AND sundry_debtors_id=?1 ", nativeQuery = true
    )
    List<String> findTotalSalesReturn(LedgerMaster mLedger);

    @Query(
            value = "SELECT IFNULL(sum(total_amount),0.0),IFNULL(sum(total_base_amount),0.0), IFNULL(sum(total_tax),0.0), " +
                    "IFNULL(sum(totaligst),0.0),  IFNULL(sum(totalsgst),0.0),  IFNULL(sum(totalcgst),0.0)," +
                    "tranx_pur_return_invoice_tbl.pur_rtn_no,DATE(tranx_pur_return_invoice_tbl.transaction_date),tranx_pur_return_invoice_tbl.id  FROM tranx_pur_return_invoice_tbl WHERE tranx_pur_return_invoice_tbl.status=1 AND sundry_creditors_id=?1 ", nativeQuery = true
    )
    List<String> findTotal(LedgerMaster mLedger);

    @Query(
            value = "SELECT IFNULL(sum(total_amount),0.0),IFNULL(sum(total_base_amount),0.0), IFNULL(sum(total_tax),0.0), " +
                    "IFNULL(sum(totaligst),0.0),  IFNULL(sum(totalsgst),0.0),  IFNULL(sum(totalcgst),0.0) " +
                    "FROM tranx_purchase_invoice_tbl WHERE tranx_purchase_invoice_tbl.status=1 AND sundry_creditors_id=?1 ", nativeQuery = true
    )
    List<String> findtheTotal(LedgerMaster mLedger);

}
