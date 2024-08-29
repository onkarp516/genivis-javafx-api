package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrderDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPurOrderDutiesTaxesRepository extends JpaRepository<TranxPurOrderDutiesTaxes, Long> {

    TranxPurOrderDutiesTaxes findByTranxPurOrderId(Long id);

    @Query(
            value = "SELECT duties_taxes_ledger_id FROM tranx_purchase_order_duties_taxes_tbl WHERE" +
                    " tranx_pur_order_id=?1 AND status =1 ",
            nativeQuery = true

    )
    List<Long> findByDutiesAndTaxesId(Long id);

    List<TranxPurOrderDutiesTaxes> findByTranxPurOrderIdAndStatus(Long id, boolean b);
}
