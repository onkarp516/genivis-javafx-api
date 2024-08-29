package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnAdjustmentBills;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxSalesReturnAdjBillsRepository extends JpaRepository<TranxSalesReturnAdjustmentBills,Long> {
    List<TranxSalesReturnAdjustmentBills> findByTranxSalesReturnIdAndStatus(Long id, boolean b);

    TranxSalesReturnAdjustmentBills findByIdAndStatus(long id, boolean b);
}
