package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurchaseChallanProductSrNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPurchaseChallanProductSrNumberRepository extends JpaRepository<TranxPurchaseChallanProductSrNumber, Long> {
    List<TranxPurchaseChallanProductSrNumber> findByProductIdAndStatus(Long id, boolean b);

    TranxPurchaseChallanProductSrNumber findByIdAndStatus(Long detailsId, boolean b);
    @Query(value = "select id,serial_no from tranx_purchase_challan_product_sr_no_tbl where product_id=?1 " +
            "AND pur_challan_unit_details_id=?2 AND status=?3",nativeQuery = true)
    List<Object[]> findSerialnumbers(Long id, Long id1, boolean b);
}
