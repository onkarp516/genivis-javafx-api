package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxCounterSales;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxCounterSalesDetailsUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxCSDetailsUnitsRepository extends JpaRepository<TranxCounterSalesDetailsUnits, Long> {
    TranxCounterSalesDetailsUnits findByIdAndStatus(Long details_id, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_counter_sales_details_units_tbl WHERE counter_sales_id=?1 AND status =1",
            nativeQuery = true
    )
    Long findTotalProducts(Long id);

    List<TranxCounterSalesDetailsUnits> findByCounterSalesIdAndStatus(Long id, boolean b);

    @Query(
            value = " SELECT product_id FROM tranx_counter_sales_details_units_tbl " +
                    "WHERE counter_sales_id=?1 AND status=?2 GROUP BY product_id ", nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);

    List<TranxCounterSalesDetailsUnits> findByCounterSalesIdAndStatusAndTransactionStatus(Long id, boolean b, long l);

    TranxCounterSalesDetailsUnits findByCounterSalesIdAndProductIdAndStatus(Long id, Long productId, boolean b);
}
