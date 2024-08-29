package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxCounterSalesDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxCounterSalesDetailsRepository extends JpaRepository<TranxCounterSalesDetails, Long> {


    List<TranxCounterSalesDetails> findByCounterSaleIdAndStatus(long id, boolean b);
}
