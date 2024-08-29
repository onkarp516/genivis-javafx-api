package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPurOrderDetailsRepository extends JpaRepository<TranxPurOrderDetails, Long> {

    List<TranxPurOrderDetails> findByTranxPurOrderIdAndStatus(long id, boolean b);

    List<TranxPurOrderDetails> findByStatus(boolean b);

    TranxPurOrderDetails findByTranxPurOrderIdAndProductIdAndStatus(Long referenceId, Long prdId, boolean b);

    TranxPurOrderDetails findByTranxPurOrderId(Long id);

    TranxPurOrderDetails findByIdAndStatus(Long detailsId, boolean b);


}
