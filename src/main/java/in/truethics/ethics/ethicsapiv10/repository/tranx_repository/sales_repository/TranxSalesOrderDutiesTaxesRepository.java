package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesOrder;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesOrderDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesOrderDutiesTaxesRepository extends JpaRepository<TranxSalesOrderDutiesTaxes,Long> {
    @Query(
            value = "SELECT duties_taxes_ledger_id FROM tranx_sales_order_duties_taxes_tbl WHERE" +
                    " sales_order_invoice_id=?1 AND status =1 ",
            nativeQuery = true

    )
    List<Long> findByDutiesAndTaxesId(Long id);

    List<TranxSalesOrderDutiesTaxes> findBySalesTransactionAndStatus(TranxSalesOrder invoiceTranx, boolean b);
}
