package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTranxDetailsBatchwise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface StockTranxDetailsBatchwiseRepository extends JpaRepository<InventorySummaryTranxDetailsBatchwise, Long> {
    @Query(value = "SELECT COUNT(id) FROM stock_opening_closing_batchwise_snap_tbl where status=?1", nativeQuery = true)
    Long countRecords(Boolean status);

    InventorySummaryTranxDetailsBatchwise findByIdAndStatus(Long rowId, boolean b);

    @Query(value = "SELECT id FROM stock_opening_closing_batchwise_snap_tbl WHERE " +
            "tranx_id=?1 AND product_id=?2 AND tranx_type_id=?3 AND tranx_date=?4 ", nativeQuery = true)
    String findRow(Long tranxId, Long productId, Long tranxTypeId, Date tranxDate);


    @Query(value = "SELECT * FROM stock_opening_closing_batchwise_snap_tbl WHERE " +
            "product_id=?1 AND id!=?2 AND tranx_date>?3 AND status=?4 AND batch_id=?5 ORDER BY tranx_date ASC ", nativeQuery = true)
    List<InventorySummaryTranxDetailsBatchwise> findSuccessiveRow(Long productId, Long rowId,
                                                               Date invoiceDate,Boolean b, Long batchId);


    @Query(value = "SELECT * FROM stock_opening_closing_batchwise_snap_tbl WHERE " +
            "product_id=?1 AND tranx_date<=?2 AND status=?3 " +
            "AND financial_year=?4 And batch_id=?5 ORDER BY tranx_date DESC, id DESC LIMIT 1", nativeQuery = true)
    InventorySummaryTranxDetailsBatchwise findTranx(Long productId, Date purDate,
                                                 Boolean status,Long fiscalYearId,Long batchId);

/*    @Query(value = "SELECT * FROM stock_opening_closing_batchwise_snap_tbl WHERE " +
            "product_id=?1 AND tranx_date<=?2 AND status=?3 " +
            "ORDER BY tranx_date DESC, id DESC LIMIT 1", nativeQuery = true)
    InventorySummaryTransactionDetails findTranx(Long productId, Date purDate,
                                                 Boolean status);*/



    @Query(value = "SELECT * FROM stock_opening_closing_batchwise_snap_tbl WHERE " +
            "product_id=?1 AND tranx_date>?2 AND status=?3 " +
            "AND financial_year=?4 ORDER BY tranx_date ASC", nativeQuery = true)
    List<InventorySummaryTranxDetailsBatchwise> findList(Long productId, Date purDate,
                                                      Boolean status,Long fiscalYearId);


    InventorySummaryTranxDetailsBatchwise findByTranxIdAndProductIdAndTranxTypeIdAndTranxDateAndStatus(Long tranxId,
                                                                                                    Long productId,
                                                                                                    Long tranxTypeId,
                                                                                                    Date invoiceDate,
                                                                                                    Boolean b);

    @Query(value = "SELECT * FROM `stock_opening_closing_batchwise_snap_tbl` WHERE product_id=?1 AND tranx_date BETWEEN ?2 AND ?3" +
            " AND id!=?4 AND status=?5 ORDER BY tranx_date ASC", nativeQuery = true)
    List<InventorySummaryTranxDetailsBatchwise> getBetweenDateProductId(Long productId, Date oldDate,
                                                                     Date newDate, Long rowId, Boolean b);

    InventorySummaryTranxDetailsBatchwise findByProductIdAndTranxTypeIdAndTranxId(Long id, Long id1, Long id2);

    InventorySummaryTranxDetailsBatchwise findTop1ByProductIdOrderByIdDesc(Long id);


    @Query(value = "SELECT * FROM stock_opening_closing_batchwise_snap_tbl where id in (select max(id) from stock_opening_closing_batchwise_snap_tbl where product_id=?1 and closing_stock>0 group by unit_id, batch_id);", nativeQuery = true)
    ArrayList<InventorySummaryTranxDetailsBatchwise> findBatchwiseStockByProductId(Long productId);

    @Query(value = "SELECT * FROM stock_opening_closing_batchwise_snap_tbl where id in (select max(id) from stock_opening_closing_batchwise_snap_tbl where product_id=?1 and closing_stock>0 group by batch_id);", nativeQuery = true)
    ArrayList<InventorySummaryTranxDetailsBatchwise> findBatchListByProductId(Long productId);

    @Query(value = "SELECT * FROM stock_opening_closing_batchwise_snap_tbl where product_id=?1 and batch_id=?2 and unit_id=?3 and closing_stock>0 order by id desc limit 1;", nativeQuery = true)
    InventorySummaryTranxDetailsBatchwise findCurrentStock(Long productId, Long batchId, Long unitId);

    @Query(value = "SELECT * FROM stock_opening_closing_batchwise_snap_tbl where product_id=?1 and batch_id=?2 and closing_stock>0 order by id desc;", nativeQuery = true)
    ArrayList<InventorySummaryTranxDetailsBatchwise> findCurrentStockByBatchId(Long productId, Long batchId);
}
