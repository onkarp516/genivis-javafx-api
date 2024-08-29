package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnProductSrNo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesReturnPrSrNoRepository extends
        JpaRepository<TranxSalesReturnProductSrNo, Long> {
    TranxSalesReturnProductSrNo findByIdAndStatus(Long detailsId, boolean b);

    @Query(value = "select id,serial_no from tranx_sales_return_prod_sr_no_tbl where product_id=?1 " +
            "AND sales_return_unit_details_id=?2 AND status=?3",nativeQuery = true)
    List<Object[]> findSerialnumbers(Long id, Long id1, boolean b);
}
