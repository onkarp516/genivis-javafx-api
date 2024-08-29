package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPurReturnDetailsRepository extends JpaRepository<TranxPurReturnInvoiceDetails,Long> {
    @Query(
            value = " SELECT product_id FROM tranx_pur_return_invoice_details_tbl " +
                    "WHERE pur_return_invoice_id=?1 AND status=?2 GROUP BY product_id " , nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);

    List<TranxPurReturnInvoiceDetails> findByPurReturnInvoiceIdAndStatus(Long id, boolean b);

    TranxPurReturnInvoiceDetails findByIdAndStatus(Long detailsId, boolean b);
}
