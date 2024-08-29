package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesOrderDetailsRepository extends JpaRepository<TranxSalesOrderDetails,Long> {
    List<TranxSalesOrderDetails> findBySalesTransactionIdAndStatus(long id, boolean b);

    TranxSalesOrderDetails findBySalesTransactionIdAndProductIdAndStatus(Long referenceId, Long prdId, boolean b);


    TranxSalesOrderDetails findByIdAndStatus(Long detailsId, boolean b);

    @Query(
            value = " SELECT product_id FROM tranx_sales_order_details_units_tbl " +
                    "WHERE sales_order_id=?1 AND status=?2 GROUP BY product_id " , nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);
}
