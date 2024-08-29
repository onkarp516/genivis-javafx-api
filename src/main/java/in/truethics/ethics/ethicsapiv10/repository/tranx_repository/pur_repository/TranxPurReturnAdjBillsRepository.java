package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnAdjustmentBills;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxPurReturnAdjBillsRepository extends JpaRepository<TranxPurReturnAdjustmentBills,Long> {
    List<TranxPurReturnAdjustmentBills> findByTranxPurReturnIdAndStatus(Long id, boolean b);

    TranxPurReturnAdjustmentBills findByIdAndStatus(long id, boolean b);
}
