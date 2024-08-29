package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxReceiptMasterRepository extends JpaRepository<TranxReceiptMaster,Long> {

    @Query(
            value = " SELECT COUNT(*) FROM tranx_receipt_master_tbl WHERE outlet_id=?1 And " +
                    "branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_receipt_master_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long id1);

    List<TranxReceiptMaster> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    TranxReceiptMaster findByIdAndOutletIdAndStatus(Long receiptId, Long id, boolean b);

    TranxReceiptMaster findByIdAndStatus(long receiptId, boolean b);

    TranxReceiptMaster findByTranxSalesOrderIdAndStatus(Long id, boolean b);

    List<TranxReceiptMaster> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    TranxReceiptMaster findByTranxSalesInvoiceIdAndStatus(Long id, boolean b);
    @Query(
            value = " SELECT IFNULL(SUM(total_amt),0.0) FROM tranx_receipt_master_tbl WHERE transcation_date=?1 AND status=?2", nativeQuery = true
    )
    Double findTotalAmt(LocalDate currentDate, boolean b);
    @Query(
            value = " SELECT IFNULL(SUM(total_amt),0.0) FROM tranx_receipt_master_tbl WHERE transcation_date=?1 AND status=?2 AND outlet_id=?3", nativeQuery = true
    )
    Double findTotalAmtOutlet(LocalDate currentDate, boolean b, Long id);  //for web app
    @Query(
            value = " SELECT IFNULL(SUM(total_amt),0.0) FROM tranx_receipt_master_tbl WHERE transcation_date BETWEEN ?1 AND ?2 AND status=?3", nativeQuery = true
    )
    Double findWeeklyTotalAmt(LocalDate startOfWeek, LocalDate endOfWeek, boolean b);
    @Query(
            value = " SELECT IFNULL(SUM(total_amt),0.0) FROM tranx_receipt_master_tbl WHERE transcation_date BETWEEN ?1 AND ?2 AND status=?3 AND outlet_id=?4", nativeQuery = true
    )
    Double findWeeklyTotalAmtOutlet(LocalDate startOfWeek, LocalDate endOfWeek, boolean b, Long id);

    TranxReceiptMaster findByOutletIdAndBranchIdAndReceiptNoIgnoreCase(Long id, Long id1, String receiptNo);

    TranxReceiptMaster findByOutletIdAndReceiptNoIgnoreCaseAndBranchIsNull(Long id, String receiptNo);

    TranxReceiptMaster findByTranxSalesCompInvoiceIdAndStatus(Long id, boolean b);
}
