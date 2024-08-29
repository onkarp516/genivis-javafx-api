package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPurChallanDetailsRepository extends JpaRepository<TranxPurChallanDetails,Long> {
    List<TranxPurChallanDetails> findByTranxPurChallanIdAndStatus(long id, boolean b);


    List<TranxPurChallanDetails> findBytranxPurChallanIdAndStatus(Long id, boolean b);

    TranxPurChallanDetails findByIdAndStatus(Long detailsId, boolean b);


}
