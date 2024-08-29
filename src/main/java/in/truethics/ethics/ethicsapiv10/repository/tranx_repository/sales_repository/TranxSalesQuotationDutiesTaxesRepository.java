package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesQuotationDutiesTaxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesQuotationDutiesTaxesRepository extends JpaRepository<TranxSalesQuotationDutiesTaxes, Long> {

    @Query(
            value = "SELECT duties_taxes_ledger_id FROM tranx_sales_quotation_duties_taxes_tbl WHERE" +
                    " sales_quotation_invoice_id=?1 AND status =1 ",
            nativeQuery = true
    )
    List<Long> findByDutiesAndTaxesId(Long id);

    List<TranxSalesQuotationDutiesTaxes> findBySalesQuotationTransactionIdAndStatus(Long id, boolean b);
}