package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import com.google.gson.JsonElement;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesPaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransSalesPaymentTypeRepository extends JpaRepository<TranxSalesPaymentType,Long> {
    List<TranxSalesPaymentType> findByTranxSalesInvoiceIdAndStatus(Long id, boolean b);

    TranxSalesPaymentType findByIdAndStatus(Long id,Boolean status);

    @Query(value = "SELECT IFNULL(SUM(payment_amount),0.0) FROM sales_payment_type_tbl where " +
            " tranx_sales_invoice_id=?1 AND ledger_id=?2 group by type",nativeQuery = true)
    Double findAmount(Long invoiceId, Long ledgerId);

    @Query(value = "SELECT * FROM sales_payment_type_tbl WHERE " +
            "tranx_sales_invoice_id=?1 AND status=?2 group by type",nativeQuery = true)
    List<TranxSalesPaymentType> findPaymentModes(Long id, boolean b);

    List<TranxSalesPaymentType> findByTranxSalesInvoiceIdAndStatusAndType(Long id, boolean b, String type);
}
