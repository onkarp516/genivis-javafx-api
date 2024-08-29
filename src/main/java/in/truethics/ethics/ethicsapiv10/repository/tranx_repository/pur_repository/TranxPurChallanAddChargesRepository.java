package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallanAdditionalCharges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TranxPurChallanAddChargesRepository extends JpaRepository<TranxPurChallanAdditionalCharges,Long> {
    List<TranxPurChallanAdditionalCharges> findByTranxPurChallanIdAndStatus(Long id, boolean b);

    TranxPurChallanAdditionalCharges findByAdditionalChargesIdAndTranxPurChallanIdAndStatus(Long ledgerId, Long id, boolean b);

    TranxPurChallanAdditionalCharges findByIdAndStatus(long del_id, boolean b);
}
