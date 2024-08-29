package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummary;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface StockTranxDetailsRepository extends JpaRepository<InventorySummaryTransactionDetails, Long> {
    @Query(value = "SELECT COUNT(id) FROM stock_opening_closing_snap_tbl where status=?1", nativeQuery = true)
    Long countRecords(Boolean status);

    InventorySummaryTransactionDetails findByIdAndStatus(Long rowId, boolean b);

    @Query(value = "SELECT id FROM stock_opening_closing_snap_tbl WHERE " +
            "tranx_id=?1 AND product_id=?2 AND tranx_type_id=?3 AND tranx_date=?4 ", nativeQuery = true)
    String findRow(Long tranxId, Long productId, Long tranxTypeId, Date tranxDate);


    @Query(value = "SELECT * FROM stock_opening_closing_snap_tbl WHERE " +
            "product_id=?1 AND id!=?2 AND tranx_date>?3 AND status=?4 ORDER BY tranx_date ASC ", nativeQuery = true)
    List<InventorySummaryTransactionDetails> findSuccessiveRow(Long productId, Long rowId,
                                                               Date invoiceDate,Boolean b);


    @Query(value = "SELECT * FROM stock_opening_closing_snap_tbl WHERE " +
            "product_id=?1 AND tranx_date<=?2 AND status=?3 " +
            "AND financial_year=?4 ORDER BY tranx_date DESC, id DESC LIMIT 1", nativeQuery = true)
    InventorySummaryTransactionDetails findTranx(Long productId, Date purDate,
                                                 Boolean status,Long fiscalYearId);

/*    @Query(value = "SELECT * FROM stock_opening_closing_snap_tbl WHERE " +
            "product_id=?1 AND tranx_date<=?2 AND status=?3 " +
            "ORDER BY tranx_date DESC, id DESC LIMIT 1", nativeQuery = true)
    InventorySummaryTransactionDetails findTranx(Long productId, Date purDate,
                                                 Boolean status);*/



    @Query(value = "SELECT * FROM stock_opening_closing_snap_tbl WHERE " +
            "product_id=?1 AND tranx_date>?2 AND status=?3 " +
            "AND financial_year=?4 ORDER BY tranx_date ASC", nativeQuery = true)
    List<InventorySummaryTransactionDetails> findList(Long productId, Date purDate,
                                                      Boolean status,Long fiscalYearId);


    InventorySummaryTransactionDetails findByTranxIdAndProductIdAndTranxTypeIdAndTranxDateAndStatus(Long tranxId,
                                                                                                    Long productId,
                                                                                                    Long tranxTypeId,
                                                                                                    Date invoiceDate,
                                                                                                    Boolean b);

    @Query(value = "SELECT * FROM `stock_opening_closing_snap_tbl` WHERE product_id=?1 AND tranx_date BETWEEN ?2 AND ?3" +
            " AND id!=?4 AND status=?5 ORDER BY tranx_date ASC", nativeQuery = true)
    List<InventorySummaryTransactionDetails> getBetweenDateProductId(Long productId, Date oldDate,
                                                                     Date newDate, Long rowId, Boolean b);

    InventorySummaryTransactionDetails findByProductIdAndTranxTypeIdAndTranxId(Long id, Long id1, Long id2);

    InventorySummaryTransactionDetails findTop1ByProductIdOrderByIdDesc(Long id);


}
