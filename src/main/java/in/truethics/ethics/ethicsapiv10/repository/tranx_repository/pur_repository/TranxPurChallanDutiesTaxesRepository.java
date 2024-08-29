package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallanDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPurChallanDutiesTaxesRepository extends JpaRepository<TranxPurChallanDutiesTaxes,Long> {

    @Query(
            value = "SELECT duties_taxes_ledger_id FROM tranx_purchase_challan_duties_taxes_tbl WHERE" +
                    " tranx_pur_challan_id=?1 AND status =1 ",
            nativeQuery = true

    )
    List<Long> findByDutiesAndTaxesId(Long id);

    List<TranxPurChallanDutiesTaxes> findByTranxPurChallanIdAndStatus(Long invoiceTranx, boolean b);
}
