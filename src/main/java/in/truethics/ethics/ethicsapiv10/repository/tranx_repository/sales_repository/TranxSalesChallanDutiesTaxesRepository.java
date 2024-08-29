package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallanDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesChallanDutiesTaxesRepository extends JpaRepository<TranxSalesChallanDutiesTaxes, Long> {

    @Query(
            value = "SELECT duties_taxes_ledger_id FROM tranx_sales_challan_duties_taxes_tbl WHERE" +
                    " sales_challan_id=?1 AND status =1 ",
            nativeQuery = true

    )
    List<Long> findByDutiesAndTaxesId(Long id);

    List<TranxSalesChallanDutiesTaxes> findBySalesTransactionAndStatus(TranxSalesChallan invoiceTranx, boolean b);
}
