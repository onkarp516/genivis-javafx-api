package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallanAdditionalCharges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxSalesChallanAdditionalChargesRepository extends
        JpaRepository<TranxSalesChallanAdditionalCharges,Long> {
    List<TranxSalesChallanAdditionalCharges> findBySalesTransaction(TranxSalesChallan mSalesTranx);

    List<TranxSalesChallanAdditionalCharges> findBySalesTransactionIdAndStatus(Long id, boolean b);

    TranxSalesChallanAdditionalCharges findByAdditionalChargesIdAndSalesTransactionIdAndStatus(Long ledgerId, Long id, boolean b);

    TranxSalesChallanAdditionalCharges findByIdAndStatus(long del_id, boolean b);
}
