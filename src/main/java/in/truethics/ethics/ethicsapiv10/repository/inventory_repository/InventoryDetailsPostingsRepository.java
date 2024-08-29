package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.InventoryDetailsPostings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface InventoryDetailsPostingsRepository extends
        JpaRepository<InventoryDetailsPostings, Long> {


    @Query(
            value = " SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL) AND tranx_action=?4 AND " +
                    "inventory_details_postings_tbl.tranx_date BETWEEN ?5 AND ?6", nativeQuery = true
    )
    Double findClosing(Long productId, Long outletId, Long branchId, String tranx_action, LocalDate startDate, LocalDate endDate);

    @Query(
            value = " SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL) AND tranx_action=?4 AND " +
                    "fiscal_year_id=?5", nativeQuery = true
    )
    Double findFiscalyearClosing(Long productId, Long outletId, Long branchId, String tranx_action, Long fiscalyearId);


    @Query(
            value = " SELECT qty FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL) AND  " +
                    "inventory_details_postings_tbl.tranx_date BETWEEN ?4 AND ?5 LIMIT 1", nativeQuery = true
    )
    Double findOpening(Long productId, Long outletId, Long branchId, LocalDate startDate, LocalDate endDate);

    @Query(
            value = " SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL) AND tranx_action=?4 AND " +
                    "inventory_details_postings_tbl.tranx_date BETWEEN ?5 AND ?6 LIMIT 1", nativeQuery = true
    )
    Double findInword(Long productId, Long outletId, Long branchId, String tranx_action, LocalDate startDate, LocalDate endDate);

    @Query(
            value = " SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL) AND tranx_action=?4 AND " +
                    "fiscal_year_id=?5", nativeQuery = true
    )
    Double findInwordFiscYear(Long productId, Long outletId, Long branchId, String tranx_action, Long fiscalyearId);


    @Query(
            value = " SELECT * FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND fiscal_year_id=?2 " +
                    "AND outlet_id=?3 AND (branch_id=?4 OR branch_id IS NULL) AND transaction_type_id=?5 AND" +
                    " tranx_id=?6 AND (level_a_id=?7 OR level_a_id IS NULL) AND (level_b_id=?8 OR level_b_id IS NULL) AND " +
                    "(level_c_id=?9 OR level_c_id IS NULL) AND (batch_id=?10 OR batch_id IS NULL) AND" +
                    " (units_id=?11 OR units_id IS NULL) AND status=1", nativeQuery = true
    )
    InventoryDetailsPostings findByRow(Long product, Long fiscalYear, Long outlet, Long branch,
                                       Long tranxMasterType, Long invoiceId, Long levela, Long levelb, Long levelc,
                                       Long productBatch, Long unit);

    @Query(
            value = " SELECT * FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND fiscal_year_id=?2 " +
                    "AND outlet_id=?3 AND (branch_id=?4 OR branch_id IS NULL) AND transaction_type_id=?5 AND" +
                    " tranx_id=?6 AND (level_a_id=?7 OR level_a_id IS NULL) AND (level_b_id=?8 OR level_b_id IS NULL) AND " +
                    "(level_c_id=?9 OR level_c_id IS NULL) AND (batch_id=?10 OR batch_id IS NULL) AND" +
                    " (units_id=?11 OR units_id IS NULL) AND status=1 AND tranx_action=?12 order by id desc limit 1", nativeQuery = true
    )
    InventoryDetailsPostings findByPurInventoryRow(Long product, Long fiscalYear, Long outlet, Long branch,
                                       Long tranxMasterType, Long invoiceId, Long levela, Long levelb, Long levelc,
                                       Long productBatch, Long unit,String tranxAction);

    List<InventoryDetailsPostings> findByOutletIdAndBranchIdOrderByProductId(Long id, Long id1);

    List<InventoryDetailsPostings> findByOutletIdOrderByProductId(Long id);

    InventoryDetailsPostings findTop1ByTranxDateBetweenAndProductIdAndUnitsIdAndFlavourMasterIdOrderByIdDesc(LocalDate parse, LocalDate parse1, Long id, Long id1, Long id2);

    InventoryDetailsPostings findTop1ByTranxDateBetweenAndProductIdAndUnitsIdOrderByIdDesc(LocalDate parse, LocalDate parse1, Long id, Long id1);

    InventoryDetailsPostings findTop1ByTranxDateAndProductIdAndUnitsIdAndFlavourMasterIdOrderByIdDesc(LocalDate currentDate, Long id, Long id1, Long id2);

    InventoryDetailsPostings findTop1ByTranxDateAndProductIdAndUnitsIdOrderByIdDesc(LocalDate currentDate, Long id, Long id1);

    @Query(
            value = " SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE product_id=?1 " +
                    "AND outlet_id=?2 AND (branch_id=?3 OR branch_id IS NULL) AND tranx_date<=?4 AND tranx_action=?5 ",
            nativeQuery = true
//            SELECT SUM(qty) FROM `inventory_details_postings_tbl` WHERE DATE_SUB('2023-01-13', INTERVAL 1 DAY);
    )
    Double findProductOpeningStocks(Long productId, Long outletId, Long branchId, LocalDate startDate, String type);

    @Query(
            value = " SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "outlet_id=?2 AND branch_id=?3 AND tranx_action=?4 AND " +
                    "fiscal_year_id=?5", nativeQuery = true
    )
    Double findClosingWithBranch(Long productId, Long outletId, Long branchId, String dr, Long id);
    @Query(
            value = " SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "outlet_id=?2 AND branch_id IS NULL AND tranx_action=?3 AND " +
                    "fiscal_year_id=?4", nativeQuery = true
    )
    Double findClosingWithoutBranch(Long productId, Long outletId, String dr, Long id);

    @Query(
            value = " SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    " tranx_action=?2 AND " +
                    "fiscal_year_id=?3", nativeQuery = true
    )
    Double findMobileClosingWithoutBranch(Long productId,String dr, Long id);


    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE branch_id=?1 AND outlet_id=?2 " +
                    "AND product_id=?3 AND (level_a_id=?4 OR level_a_id IS NULL) AND (level_b_id=?5 OR level_b_id IS NULL) AND " +
                    "(level_c_id=?6 OR level_c_id IS NULL) AND (units_id=?7 OR units_id IS NULL) AND " +
                    "(batch_id=?8 OR batch_id IS NULL) AND (fiscal_year_id=?9 OR fiscal_year_id IS NULL) AND tranx_action=?10",
            nativeQuery = true
    )
    Double findClosingWithBranchFilter(Long branch, Long outlet, Long mProduct, Long levelAId, Long levelbId, Long levelcId,
                                       Long units, Long batchNo, Long fiscalId, String tranxActions);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE branch_id IS NULL AND outlet_id=?1 " +
                    "AND product_id=?2 AND (level_a_id=?3 OR level_a_id IS NULL) AND (level_b_id=?4 OR level_b_id IS NULL) AND " +
                    "(level_c_id=?5 OR level_c_id IS NULL) AND (units_id=?6 OR units_id IS NULL) AND " +
                    "(batch_id=?7 OR batch_id IS NULL) AND (fiscal_year_id=?8 OR fiscal_year_id IS NULL) AND " +
                    "tranx_action=?9", nativeQuery = true
    )
    Double findClosingWithoutBranchFilter(Long outlet, Long mProduct, Long levelAId, Long levelbId, Long levelcId,
                                          Long units, Long batchNo, Long fiscalId, String dr);

    InventoryDetailsPostings findByIdAndStatus(Long inventoryId, boolean b);

    List<InventoryDetailsPostings> findByProductIdAndStatus(Long id, boolean b);

    @Query(
            value = "SELECT id FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND status=?2 GROUP BY units_id"
            , nativeQuery = true
    )
    List<Object[]> findProductsGroupByUnits(Long id, boolean b);

    @Query(
            value = "SELECT SUM(qty) from `inventory_details_postings_tbl` WHERE tranx_action =?1 and product_id=?2", nativeQuery = true
    )
    Long findQtyDr(String dr, Long id);

    @Query(
            value = "SELECT SUM(qty) from `inventory_details_postings_tbl` WHERE tranx_action =?1 and product_id=?2", nativeQuery = true
    )
    Long findQtyCr(String cr, Long id);


    List<InventoryDetailsPostings> findByTransactionTypeIdAndProductIdAndStatus(Long i, Long productId, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) from `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "transaction_type_id=?2 AND units_id=?3 AND tranx_date BETWEEN ?4 ANd ?5 ", nativeQuery = true
    )
    Double findByTotalQty(Long productId, long l, Long id, String toString, String toString1);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) from `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "transaction_type_id=?2 AND units_id=?3 AND tranx_date BETWEEN ?4 ANd ?5", nativeQuery = true
    )
    Double findBySaleTotalQty(Long productId, long l, Long id, String toString, String toString1);


//    @Query(
//            value="SELECT SUM(qty) from `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
//                    "transaction_type_id=?2 AND units_id=?3" ,nativeQuery = true
//    )
//    Double findByTotalQty(Long productId, Long transactType, Long unitId);


    /****** Batch wise Monthwise product details ********/
    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) from `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "transaction_type_id=?2 AND units_id=?3 AND tranx_date BETWEEN ?4 ANd ?5 And unique_batch_no=?6", nativeQuery = true
    )
    Double findByTotalBatchQty(Long productId, long l, Long id, String startDate, String endDate, String batchno);


    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) from `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "transaction_type_id=?2 AND units_id=?3 AND tranx_date BETWEEN ?4 ANd ?5 AND unique_batch_no=?6", nativeQuery = true
    )
    Double findBySaleBatchQty(Long productId, long l, Long id, String startDate, String endDate, String batchno);

    @Query(
            value = "SELECT * from `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "batch_id=?2 AND tranx_date BETWEEN ?3 ANd ?4 AND status=1", nativeQuery = true
    )
    List<InventoryDetailsPostings> finProductsDetails(Long productId, Long batchId,
                                                      LocalDate startDatep, LocalDate endDatep);

    @Query(value = "SELECT IFNULL(SUM(qty),0) FROM inventory_details_postings_tbl where product_id=?1 AND tranx_action=?2", nativeQuery = true)
    double getSumOfCrOrDrByProductId(Long object, String cr);

    @Query(
            value = "SELECT id FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND inventory_details_postings_tbl.transaction_type_id=1 AND status=?2"
            , nativeQuery = true
    )
    List<Object[]> findProductDatabyPurchaseId(Long id, boolean b);

    @Query(
            value = "SELECT * from `inventory_details_postings_tbl` WHERE product_id=?1 AND " +
                    "tranx_date BETWEEN ?2 AND ?3 AND status=1", nativeQuery = true
    )
    List<InventoryDetailsPostings>findProductsDetails(Long productId,LocalDate startDatep, LocalDate endDatep);



@Query(
        value = "SELECT SUM(qty) from `inventory_details_postings_tbl` WHERE product_id=?1 AND" +
                " tranx_date<=?2 AND tranx_action='CR' AND status=1", nativeQuery = true

)
 Long findPurProductQty(Long productId,LocalDate startDatep);

    @Query(
            value = "SELECT SUM(qty) from `inventory_details_postings_tbl` WHERE product_id=?1 AND" +
                    " tranx_date<?2 AND tranx_action='DR' AND status=1", nativeQuery = true

    )
    Long findSalesProductQty(Long productId,LocalDate startDatep);

    @Query(
            value = "SELECT units_id,product_id,batch_id FROM `inventory_details_postings_tbl` WHERE product_id=?1 AND status=?2 GROUP by batch_id", nativeQuery = true

    )
    List<InventoryDetailsPostings>  findUnitAndBatchByProductId(Long productId,boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE branch_id IS NULL AND outlet_id=?1 " +
                    "AND product_id=?2 AND (level_a_id=?3 OR level_a_id IS NULL) AND (level_b_id=?4 OR level_b_id IS NULL) AND " +
                    "(level_c_id=?5 OR level_c_id IS NULL) AND " +
                    "(batch_id=?6 OR batch_id IS NULL) AND (fiscal_year_id=?7 OR fiscal_year_id IS NULL) AND " +
                    "tranx_action=?8", nativeQuery = true
    )
    Double findClosingProductBatchWithoutBranch(Long outlet, Long mProduct, Long levelAId, Long levelbId, Long levelcId,
                                           Long batchNo, Long fiscalId, String dr);


    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) FROM `inventory_details_postings_tbl` WHERE branch_id=?1 AND outlet_id=?2 " +
                    "AND product_id=?3 AND (level_a_id=?4 OR level_a_id IS NULL) AND (level_b_id=?5 OR level_b_id IS NULL) AND " +
                    "(level_c_id=?6 OR level_c_id IS NULL) AND " +
                    "(batch_id=?7 OR batch_id IS NULL) AND (fiscal_year_id=?8 OR fiscal_year_id IS NULL) AND " +
                    "tranx_action=?9", nativeQuery = true
    )
    Double findClosingWithProductBatch(Long branch, Long outlet, Long mProduct, Long levelAId, Long levelbId,
                                       Long levelCId, Long batchId, Long fiscalYear, String dr);
}

