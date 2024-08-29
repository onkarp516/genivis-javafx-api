package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxCounterSales;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxCounterSalesRepository extends JpaRepository<TranxCounterSales, Long> {

    @Query(
            value = " SELECT COUNT(*) FROM tranx_counter_sales_tbl WHERE outlet_id=?1 AND status =1 ", nativeQuery = true
    )
    Long findLastRecord(Long outletId);

    TranxCounterSales findByIdAndStatus(long id, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_counter_sales_tbl WHERE outlet_id=?1 AND status =1 AND branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long id1);


    @Query(
            value = " SELECT COUNT(*) FROM tranx_counter_sales_tbl WHERE outlet_id=?1 AND " +
                    "branch_id=?2 AND status=?3", nativeQuery = true
    )
    Long findLastRecordWithBranch(Long id, Long branchId, Boolean flag);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_counter_sales_tbl WHERE outlet_id=?1 AND " +
                    "branch_id IS NULL AND status=?2", nativeQuery = true
    )
    Long findLastRecord(Long id, Boolean flag);

    TranxCounterSales findByCounterSaleNoAndOutletIdAndBranchIdAndStatus(String invoiceNo, Long id, Long id1, boolean b);

    TranxCounterSales findByIdAndOutletIdAndBranchIdAndStatus(Long id, Long id1, Long id2, boolean b);



    TranxCounterSales findByCounterSaleNoAndOutletIdAndStatusAndBranchIsNull(String invoiceNo, Long id, boolean b);

    TranxCounterSales findByIdAndOutletIdAndStatusAndBranchIsNull(Long id, Long id1, boolean b);

    TranxCounterSales findByIdAndStatusAndIsBillConverted(long id, boolean b, boolean b1);

    List<TranxCounterSales> findByStatus(boolean b);

    List<TranxCounterSales> findByStatusAndTransactionDate(boolean b, LocalDate date);


    List<TranxCounterSales> findByOutletIdAndBranchIdAndStatus(Long id, Long id1, boolean b);

    List<TranxCounterSales> findByOutletIdAndStatusAndBranchIsNull(Long id, boolean b);

    List<TranxCounterSales> findByOutletIdAndBranchIdAndStatusAndPaymentModeIgnoreCase(Long id, Long id1, boolean b, String paymentMode);

    List<TranxCounterSales> findByOutletIdAndStatusAndPaymentModeIgnoreCaseAndBranchIsNull(Long id, boolean b, String paymentMode);
}
