package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxCounterSalesProdSrNo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxCounterSalesPrSrNoRepository extends JpaRepository<TranxCounterSalesProdSrNo,Long> {
    List<TranxCounterSalesProdSrNo> findByProductIdAndStatus(Long id, boolean b);
}
