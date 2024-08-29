package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoiceAddCharges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxSalesReturnAddiChargesRepository extends
        JpaRepository<TranxSalesReturnInvoiceAddCharges, Long> {
    List<TranxSalesReturnInvoiceAddCharges> findByTranxSalesReturnInvoiceIdAndStatus(Long id, boolean b);

    TranxSalesReturnInvoiceAddCharges findByAdditionalChargesIdAndTranxSalesReturnInvoiceIdAndStatus(Long ledgerId,Long id, boolean b);

    TranxSalesReturnInvoiceAddCharges findByIdAndStatus(Long detailsId, boolean b);
}
