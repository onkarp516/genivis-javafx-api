package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


public interface TranxPaymentMasterRepository extends JpaRepository<TranxPaymentMaster,Long>{
    @Query(
            value = " SELECT COUNT(*) FROM tranx_payment_master_tbl WHERE outlet_id=?1 AND branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    List<TranxPaymentMaster> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_payment_master_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long id1);

    TranxPaymentMaster findByIdAndOutletIdAndStatus(Long paymentId, Long id, boolean b);

    TranxPaymentMaster findByIdAndStatus(long payment_id, boolean b);

    List<TranxPaymentMaster> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_payment_master_tbl WHERE outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )

    List<Object[]> findmobilePaymentInvoiceList(Long masterId, LocalDate startDatep, LocalDate endDatep, boolean b);

    TranxPaymentMaster findByOutletIdAndBranchIdAndPaymentNoIgnoreCase(Long id, Long id1, String paymentNo);

    TranxPaymentMaster findByOutletIdAndPaymentNoIgnoreCaseAndBranchIsNull(Long id, String paymentNo);

    TranxPaymentMaster findByTranxIdAndStatus(Long id, boolean b);
}
