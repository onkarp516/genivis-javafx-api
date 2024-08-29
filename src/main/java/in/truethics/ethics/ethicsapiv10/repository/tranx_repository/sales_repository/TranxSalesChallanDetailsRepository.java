package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesChallanDetailsRepository extends JpaRepository<TranxSalesChallanDetails, Long> {

    List<TranxSalesChallanDetails> findBySalesTransactionIdAndStatus(long id, boolean b);

    TranxSalesChallanDetails findBySalesTransactionIdAndProductIdAndStatus(Long referenceId,
                                                                           Long prdId, boolean b);

    TranxSalesChallanDetails findByIdAndStatus(Long detailsId, boolean b);

}