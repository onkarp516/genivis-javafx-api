package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPaymentPerticularsDetailsRepository extends JpaRepository<TranxPaymentPerticularsDetails,Long> {
    TranxPaymentPerticularsDetails findByIdAndStatus(Long id, boolean b);

    @Query(
            value = "SELECT * FROM `tranx_payment_perticulars_details_tbl` WHERE id=?1 AND status=?2", nativeQuery = true
    )
    TranxPaymentPerticularsDetails findListStatus(Long id, boolean b);

    TranxPaymentPerticularsDetails findByIdAndOutletIdAndBranchIdAndStatus(Long tranx_type, Long id, Long id1, boolean b);


    TranxPaymentPerticularsDetails findByIdAndOutletIdAndStatus(Long tranx_type, Long id, boolean b);

    List<TranxPaymentPerticularsDetails> findByTranxPaymentMasterIdAndStatus(Long id, boolean b);

    List<TranxPaymentPerticularsDetails> findByTranxPaymentPerticularsIdAndStatus(Long id, boolean b);

    List<TranxPaymentPerticularsDetails> findByLedgerMasterIdAndTranxInvoiceIdAndStatus(Long id, Long id1, boolean b);

    TranxPaymentPerticularsDetails findByTranxPaymentMasterIdAndTranxInvoiceIdAndStatus(Long id, long asLong, boolean b);


    TranxPaymentPerticularsDetails findByStatusAndTranxPaymentPerticularsId(boolean b, Long id);

    TranxPaymentPerticularsDetails findByTranxPaymentMasterIdAndOutletIdAndBranchIdAndStatus(Long tranxId, Long id, Long id1, boolean b);

    TranxPaymentPerticularsDetails findByTranxPaymentMasterIdAndOutletIdAndStatus(Long tranxId, Long id, boolean b);
}
