package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.PaymentModeMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentModeMasterRepository extends JpaRepository<PaymentModeMaster,Long> {
    PaymentModeMaster findByPaymentModeIgnoreCaseAndStatus(String paymentMethod,Boolean status);

    PaymentModeMaster findByOutletIdAndBranchIdAndPaymentModeIgnoreCaseAndStatus(Long id, Long id1, String paymentMode, boolean b);

    PaymentModeMaster findByOutletIdAndPaymentModeIgnoreCaseAndStatusAndBranchIdIsNull(Long id, String paymentMode, boolean b);

    PaymentModeMaster findByIdAndStatus(long id, boolean b);

    List<PaymentModeMaster> findAllByStatus(boolean b);
}
