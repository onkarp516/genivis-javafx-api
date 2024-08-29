package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.ProductOpeningStocks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductOpeningStocksRepository extends JpaRepository<ProductOpeningStocks, Long> {
    @Query(
            value = " SELECT opening_qty FROM `product_opening_stocks_tbl` WHERE product_id=?1 AND fiscal_year_id=?2 " +
                    "AND outlet_id=?3 AND (branch_id=?4 OR branch_id IS NULL) ", nativeQuery = true
    )
    Double findProductOpeningStocks(Long productId, Long fiscalYearId, Long outletId, Long branchId);

    @Query(
            value = " SELECT * FROM `product_opening_stocks_tbl` WHERE product_id=?1 AND (units_id=?2 OR units_id IS NULL) " +
                    "AND (level_a_id=?3 OR level_a_id IS NULL) AND (level_b_id=?4 OR level_b_id IS NULL) AND " +
                    "(level_c_id=?5 OR level_c_id IS NULL)", nativeQuery = true
    )
    List<ProductOpeningStocks> findByProductOpening(Long productId, Long unitId,
                                                    Long levelaUnit, Long levelbUnit, Long levelcUnit);

    ProductOpeningStocks findByIdAndStatus(Long id, Boolean status);

    @Query(
            value = " SELECT IFNULL(SUM(opening_stocks),0.0) FROM `product_opening_stocks_tbl` WHERE product_id=?1 " +
                    "AND outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL) AND fiscal_year_id=?4", nativeQuery = true
    )
    Double findSumProductOpeningStocks(Long productId, Long outletId, Long branchId, Long fiscalyearId);

    @Query(
            value = " SELECT IFNULL(SUM(opening_stocks),0.0) FROM `product_opening_stocks_tbl` WHERE product_id=?1 " +
                    "AND outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL) AND fiscal_year_id=?4 AND batch_id=?5", nativeQuery = true
    )
    Double findSumProductOpeningStocksBatchwise(Long productId, Long outletId, Long branchId, Long fiscalyearId, Long batchId);

    @Query(
            value = " SELECT IFNULL(SUM(free_opening_qty),0.0) FROM `product_opening_stocks_tbl` WHERE product_id=?1 " +
                    "AND outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL) AND fiscal_year_id=?4 AND batch_id=?5", nativeQuery = true
    )
    Double findSumProductFreeQtyBatchwise(Long productId, Long outletId, Long branchId, Long fiscalyearId, Long batchId);

    @Query(
            value = " SELECT IFNULL(SUM(free_opening_qty),0.0) FROM `product_opening_stocks_tbl` WHERE product_id=?1 " +
                    "AND outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL)", nativeQuery = true
    )
    Double findSumProductFreeQty(Long id, Long id1, Long branchId);

    List<ProductOpeningStocks> findByProductIdAndStatus(Long object, boolean b);

    @Query(
            value = " SELECT * FROM `product_opening_stocks_tbl` WHERE product_id=?1 " +
                    "AND status=?2 ", nativeQuery = true
    )
    List<ProductOpeningStocks> findOpeningStockByProductIdAndStatus(Long productId, boolean b);

    //
    @Query(
            value = " SELECT SUM(opening_stocks), SUM(costing), SUM(free_opening_qty),units_tbl.unit_name FROM " +
                    "`product_opening_stocks_tbl` LEFT JOIN units_tbl ON product_opening_stocks_tbl.units_id = units_tbl.id " +
                    "WHERE product_id=?1 ", nativeQuery = true
    )
    List<Object[]> findOpeningStockByProductId(Long productId, boolean b);

    @Query(
            value = " SELECT IFNULL(opening_stocks,0) FROM `product_opening_stocks_tbl` WHERE batch_id=?1 " +
                    "AND status=?2 ", nativeQuery = true
    )
    Long findByBatchIdAndStatus(Long batchId, boolean b);


}
