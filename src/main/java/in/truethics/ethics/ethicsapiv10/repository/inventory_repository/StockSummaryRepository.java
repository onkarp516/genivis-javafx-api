package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;

public interface StockSummaryRepository extends JpaRepository<InventorySummary,Long> {

    InventorySummary findByOutletIdAndBranchIdAndProductIdAndUnitsIdAndTranxDate(Long id, Long id1, Long id2, Long unitId, Date tranxDate);

    InventorySummary findByOutletIdAndBranchIdIsNullAndProductIdAndUnitsIdAndTranxDate(Long id, Long id1,  Long unitId, Date tranxDate);


    @Query(value = "SELECT closing_stock FROM `stock_opening_closing_summary_tbl` WHERE " +
            "branch_id=?1 AND outlet_id=?2 AND product_id=?3 AND units_id=?4 AND batch_id=?5 AND fiscal_year_id=?6 " +
            "order by id desc limit 1", nativeQuery = true)
    Double getClosingStock(Long branchId, Long outletId, Long productId, Long unitId, Long batchId,Long fiscalId);

    @Query(value = "SELECT closing_stock FROM `stock_opening_closing_summary_tbl` WHERE " +
            "branch_id is null AND outlet_id=?1 AND product_id=?2 AND units_id=?3 AND batch_id=?4 AND " +
            "fiscal_year_id=?5 order by id desc limit 1", nativeQuery = true)

    Double getClosingStockWTBr(Long outletId, Long productId, Long unitId, Long batchId,Long fiscalId);
}
