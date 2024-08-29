package in.truethics.ethics.ethicsapiv10.repository.barcode_repository;

import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProductBatchNoRepository extends JpaRepository<ProductBatchNo, Long> {
    @Query(
            value = "select * from product_batchno_tbl WHERE product_id=?1 AND outlet_id=?2 AND status=?3" +
                    " AND (brand_id=?4 OR brand_id IS NULL) AND (group_id=?5 OR group_id IS NULL) AND (category_id=?6 OR category_id IS NULL)" +
                    " AND (subcategory_id=?7 OR subcategory_id IS NULL) AND (packaging_id=?8 OR packaging_id IS NULL)" +
                    " AND (unit_id=?9 OR unit_id IS NULL)", nativeQuery = true
    )
    List<ProductBatchNo> findByBatch(Long productId, Long id, boolean b, Long brandId, Long groupId, Long categoryId, Long subCategoryId, Long packageId, Long unitId);

    @Query(
            value = "select * from product_batchno_tbl WHERE product_id=?1 AND outlet_id=?2 AND status=?3" +
                    " AND (level_a_id=?4 OR level_a_id IS NULL) AND (level_b_id=?5 OR level_b_id IS NULL) AND (level_c_id=?6 OR level_c_id IS NULL)",
            nativeQuery = true
    )
        // AND (unit_id=?7 OR unit_id IS NULL)
    List<ProductBatchNo> findByBatchList(Long productId, Long id, boolean b, Long levelAId, Long levelBId, Long levelCId); //, Long unitId

//    @Query(
//            value = "select * from product_batchno_tbl WHERE product_id=?1 AND outlet_id=?2 AND status=?3",
//            nativeQuery = true
//    )
//        // AND (unit_id=?7 OR unit_id IS NULL)
//    List<ProductBatchNo> findByBatchList1(Long productId, Long id, boolean b);
    @Query(
            value = "select * from product_batchno_tbl WHERE product_id=?1 AND outlet_id=?2 AND status=?3 GROUP BY batch_no",
            nativeQuery = true
    )
        // AND (unit_id=?7 OR unit_id IS NULL)
    List<ProductBatchNo> findByBatchList1(Long batchId, Long id, boolean b);

    ProductBatchNo findByIdAndStatus(long details_id, boolean b);


    @Query(
            value = "select * from product_batchno_tbl WHERE product_id=?1 AND (brand_id=?2 OR brand_id IS NULL) " +
                    "AND (group_id=?3 OR group_id IS NULL) AND (category_id=?4 OR category_id IS NULL) And " +
                    "(subcategory_id=?5 OR subcategory_id IS NULL) AND (packaging_id=?6 OR packaging_id IS NULL) " +
                    "AND (unit_id=?7 OR unit_id IS NULL) AND status=?8",
            nativeQuery = true
    )
    List<ProductBatchNo> findByUniqueProductIdAndStatus(Long id, Long brandId, Long groupId, Long categoryId,
                                                        Long subCateId, Long packageId, Long unitId, boolean b);


    ProductBatchNo findTop1ByBatchNoOrderByIdDesc(String batchNo);

    ProductBatchNo findByBatchNo(String batchNo);

    @Query(value = "select * from product_batchno_tbl WHERE product_id=?1 AND outlet_id=?2 AND status=?3" +
            " AND (level_a_id=?4 OR level_a_id IS NULL) AND (level_b_id=?5 OR level_b_id IS NULL) AND (level_c_id=?6 OR level_c_id IS NULL)" +
            " AND (unit_id=?7 OR unit_id IS NULL) ORDER BY id DESC LIMIT 1", nativeQuery = true)
    ProductBatchNo getLastRecordByFilterForCosting(Long productId, Long id, boolean b, Long levelAId, Long levelBId, Long levelCId, Long unitId);

    @Query(
            value = "select * from product_batchno_tbl WHERE product_id=?1 AND (level_a_id=?2 OR level_a_id IS NULL)" +
                    " AND (level_b_id=?3 OR level_b_id IS NULL) AND (level_c_id=?4 OR level_c_id IS NULL) AND" +
                    " (unit_id=?5 OR unit_id IS NULL) AND status=?6",
            nativeQuery = true
    )
    List<ProductBatchNo> findByUniqueBatchProductIdAndStatus(Long id, Long levelAId, Long levelBId, Long levelCId, Long unitId, boolean b);

    ProductBatchNo findTop1ByProductIdAndStatusOrderByIdDesc(Long id, boolean b);

    @Query(
            value = "SELECT IFNULL(AVG(purchase_rate),0.0) FROM product_batchno_tbl WHERE product_id=?1 and unit_id=?2 ",
            nativeQuery = true
    )
    Double findPurchaseTotalVale(Long productId, Long id);

    @Query(
            value = "SELECT IFNULL(AVG(sales_rate),0.0) FROM product_batchno_tbl WHERE product_id=?1 and unit_id=?2",
            nativeQuery = true
    )
    Double findTotalVale(Long productId, Long id);

    /****** Batch wise Monthwise product details ********/
    @Query(
            value = "SELECT IFNULL(AVG(purchase_rate),0.0) FROM product_batchno_tbl WHERE product_id=?1 and unit_id=?2 " +
                    "AND batch_no=?3 ",
            nativeQuery = true
    )
    Double findPurchaseBatchTotalVale(Long productId, Long id, String batchno);

    @Query(
            value = "SELECT IFNULL(AVG(sales_rate),0.0) FROM product_batchno_tbl WHERE product_id=?1 and unit_id=?2 " +
                    "AND batch_no=?3",
            nativeQuery = true
    )
    Double findSaleBatchTotalVale(Long productId, Long id, String batchno);

    List<ProductBatchNo> findByBatchNoAndStatus(String uniqueBatchNo, boolean b);

    @Query(
            value = "SELECT * FROM product_batchno_tbl WHERE expiry_date<?1",
            nativeQuery = true
    )
    List<ProductBatchNo> FindExpiredProduct(LocalDate currentDate);

    List<ProductBatchNo> findByProductIdAndStatus(Long productId, boolean b);

    @Query(
            value = "SELECT pbnt.batch_no, pbnt.id, pbnt.unit_id,units_tbl.unit_name, pbnt.qnty, pbnt.opening_qty,pupt.unit_conversion, pbnt.sales_rate, pbnt.purchase_rate," +
                    " pbnt.costing, pbnt.costing_with_tax, pbnt.mrp FROM `product_batchno_tbl`" +
                    " AS pbnt LEFT JOIN product_unit_packing_tbl AS pupt ON pbnt.unit_id=pupt.id LEFT JOIN units_tbl ON pbnt.unit_id = units_tbl.id WHERE" +
                    " pbnt.batch_no=?2 AND pbnt.product_id=?1 ",
            nativeQuery = true
    )
    List<String[]> FindUnitByProductIdAndBatchId(Long productId, Long batchno);

    @Query(
                value = "SELECT sum(costing) FROM `product_batchno_tbl` WHERE product_id=?1" ,nativeQuery = true
          )
    Double findCostByProductId(Long productId);
}