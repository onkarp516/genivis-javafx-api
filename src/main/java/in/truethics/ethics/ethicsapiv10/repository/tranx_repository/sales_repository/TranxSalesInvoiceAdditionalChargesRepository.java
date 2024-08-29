package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceAdditionalCharges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxSalesInvoiceAdditionalChargesRepository extends JpaRepository<TranxSalesInvoiceAdditionalCharges,Long> {
    List<TranxSalesInvoiceAdditionalCharges> findBySalesTransactionAndStatus(TranxSalesInvoice mSalesTranx,
                                                                         boolean b);
    //    List<TranxSalesInvoiceAdditionalCharges> findBySalesTransaction(TranxSalesInvoice mSalesTranx);
    List<TranxSalesInvoiceAdditionalCharges> findBySalesTransactionIdAndStatus(Long id, boolean b);
    TranxSalesInvoiceAdditionalCharges findByIdAndStatus(Long detailsId, boolean b);

   TranxSalesInvoiceAdditionalCharges findByAdditionalChargesIdAndSalesTransactionIdAndStatus(Long id, Long ledgerId, boolean b);
}
