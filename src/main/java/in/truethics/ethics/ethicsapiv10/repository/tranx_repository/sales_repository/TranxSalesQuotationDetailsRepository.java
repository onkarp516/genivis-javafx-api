package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesQuotationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesQuotationDetailsRepository extends JpaRepository<TranxSalesQuotationDetails, Long> {

    List<TranxSalesQuotationDetails> findByTranxSalesQuotationId(Long id);

    @Query(
            value = " SELECT * FROM tranx_sales_quotation_details_tbl WHERE tranx_sales_quotation_id=?1 AND status=?2 ",
            nativeQuery = true
    )
    List<TranxSalesQuotationDetails> findBySalesQuotationIdsAndStatus(long id, boolean b);

    TranxSalesQuotationDetails findByTranxSalesQuotationIdAndProductIdAndStatus(Long referenceId,
                                                                              Long prdId, boolean b);

    List<TranxSalesQuotationDetails> findByTranxSalesQuotationIdAndStatus(Long id, boolean b);

    TranxSalesQuotationDetails findByIdAndStatus(Long detailsId, boolean b);

    @Query(
            value = " SELECT product_id FROM tranx_sales_quotation_details_tbl " +
                    "WHERE tranx_sales_quotation_id=?1 AND status=?2 GROUP BY product_id " , nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);

}