package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;


import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.Units;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface ProductUnitRepository extends JpaRepository<ProductUnitPacking, Long> {

    List<ProductUnitPacking> findByProductIdAndStatus(Long id, boolean b);

    @Query(
            value = "select distinct brand_id from `product_unit_packing_tbl` where product_id=?1",
            nativeQuery = true
    )
    List<Long> findBrandsIdDistinct(Long product_id);

    @Query(
            value = "select distinct group_id from `product_unit_packing_tbl` where product_id=?1 AND " +
                    "(brand_id=?2 OR brand_id IS NULL)",
            nativeQuery = true
    )
    List<Long> findGroupIdDistinct(Long product_id, Long id);

    @Query(
            value = "select distinct category_id from `product_unit_packing_tbl` where product_id=?1 AND " +
                    "(brand_id=?2 OR brand_id IS NULL) AND (group_id=?3 OR group_id IS NULL)",
            nativeQuery = true
    )
    List<Long> findCategoryIdDistinct(Long product_id, Long mBrands, Long mGroup);

    @Query(
            value = "select distinct subcategory_id from `product_unit_packing_tbl` where product_id=?1 AND " +
                    "(brand_id=?2 OR brand_id IS NULL) AND (group_id=?3 OR group_id IS NULL) " +
                    "AND (category_id=?4 OR category_id IS NULL) ",
            nativeQuery = true
    )
    List<Long> findSubCategoryIdDistincts(Long product_id, Long mBrands, Long mGroup, Long mCategory);

    @Query(
            value = "select distinct packing_master_id from `product_unit_packing_tbl` where product_id=?1 AND " +
                    "(brand_id=?2 OR brand_id IS NULL) AND (group_id=?3 OR group_id IS NULL)  " +
                    "AND (category_id=?4 OR category_id IS NULL) AND (subcategory_id=?5 OR subcategory_id IS NULL) ",
            nativeQuery = true
    )
    List<Long> findPackageDistincts(Long product_id, Long mBrands, Long mGroup, Long mCategory, Long mSubCategory);

    @Query(
            value = "select * from `product_unit_packing_tbl` where product_id=?1 AND" +
                    "(brand_id=?2 OR brand_id IS NULL) AND (group_id=?3 OR group_id IS NULL) AND " +
                    "(category_id=?4 OR category_id IS NULL) AND (subcategory_id=?5 OR subcategory_id IS NULL) AND " +
                    "(packing_master_id=?6 OR packing_master_id IS NULL)",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByBrandsGroupPackingUnits(Long product_id, Long mBrands, Long mGroup, Long mCategory,
                                                           Long mSubCategory, Long mPack);

    @Query(
            value = "select brand_id from `product_unit_packing_tbl` where product_id=?1 AND " +
                    "status=?2",
            nativeQuery = true
    )
    List<ProductUnitPacking> findBrandsId(Long product_id, Boolean status);

    ProductUnitPacking findByIdAndStatus(Long details_id, boolean b);

    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND status=?2 " +
                    "GROUP BY product_id",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByUniqueProductIdAndStatus(long parseLong, boolean b);

    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND status=?2 " +
                    "GROUP BY brand_id",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByUniqueBrandsList(long parseLong, boolean b);

    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND (brand_id=?2 OR brand_id IS NULL) AND status=?3 " +
                    "GROUP BY group_id",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByUniqueGroupListwithBrands(long parseLong, Long brandId, boolean b);

    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND (brand_id=?2 OR brand_id IS NULL) " +
                    "AND (group_id=?3 OR group_id IS NULL) AND status=?4 " +
                    "GROUP BY category_id",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByUniqueCategoryListBrands(long parseLong, Long brandId, Long groupId, boolean b);

    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND (brand_id=?2 OR brand_id IS NULL) " +
                    "AND (group_id=?3 OR group_id IS NULL) AND (category_id=?4 OR category_id IS NULL) AND " +
                    "status=?5 GROUP BY subcategory_id",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByUniqueSubcategoryListBrands(long parseLong, Long brandId, Long groupId, Long categoryId, boolean b);

    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND (brand_id=?2 OR brand_id IS NULL) " +
                    "AND (group_id=?3 OR group_id IS NULL) AND (category_id=?4 OR category_id IS NULL) AND " +
                    "(subcategory_id=?5 OR subcategory_id IS NULL) AND status=?6 GROUP BY packing_master_id",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByUniquePackageListBrands(long parseLong, Long brandId, Long groupId,
                                                           Long categoryId, Long subCateId, boolean b);

    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND (brand_id=?2 OR brand_id IS NULL) " +
                    "AND (group_id=?3 OR group_id IS NULL) AND (category_id=?4 OR category_id IS NULL) " +
                    "And (subcategory_id=?5 OR subcategory_id IS NULL) " +
                    "AND (packing_master_id=?6 OR packing_master_id IS NULL) AND status=?7",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByUniqueUnitsBrands(long parseLong, Long brandId, Long groupId, Long categoryId,
                                                     Long subCateId, Long packageId, boolean b);

    List<ProductUnitPacking> findByProductId(Long product_id);

    @Query(
            value = "select distinct packing_master_id from `product_unit_packing_tbl` where product_id=?1",
            nativeQuery = true
    )
    List<Long> findProductIdDistinct(long product_id);

    List<ProductUnitPacking> findByProductIdAndPackingMasterId(Long product_id, Long mPack);

    @Query(
            value = "select * from `product_unit_packing_tbl` where product_id=?1 AND packing_master_id IS NULL",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByProductIdAndPackingIsNULL(Long product_id);

    @Query(
            value = "SELECT COUNT(id) FROM `product_unit_packing_tbl` WHERE hsn_id=?1 " +
                    "AND status=?2", nativeQuery = true
    )
    Long findByProductHsnTranx(Long id, boolean b);

    @Query(
            value = "SELECT COUNT(id) FROM `product_unit_packing_tbl` WHERE taxmaster_id=?1 " +
                    "AND status=?2", nativeQuery = true
    )
    Long findByProductTaxTranx(Long id, boolean b);

    @Query(
            value = "SELECT * FROM `product_unit_packing_tbl` LEFT JOIN product_tbl ON " + " product_unit_packing_tbl.product_id=product_tbl.id WHERE" + " product_tbl.outlet_id=?1 AND " +
                    "product_tbl.branch_id=?2 AND " + "product_tbl.status=?3 ORDER BY product_tbl.product_name", nativeQuery = true

    )
    List<ProductUnitPacking> findOutletAndBranchAllProduct(Long id, Long id1, boolean b);

    @Query(
            value = "SELECT * FROM `product_unit_packing_tbl` LEFT JOIN product_tbl ON product_unit_packing_tbl.product_id=product_tbl.id WHERE" + " product_tbl.outlet_id=?1 AND " +
                    "product_tbl.status=?2 ORDER BY product_tbl.product_name", nativeQuery = true

    )
    List<ProductUnitPacking> findOutletAllProduct(Long id, boolean b);

    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND (brand_id=?2 OR brand_id IS NULL) " +
                    "AND (group_id=?3 OR group_id IS NULL) AND (category_id=?4 OR category_id IS NULL) " +
                    "And (subcategory_id=?5 OR subcategory_id IS NULL) " +
                    "AND (packing_master_id=?6 OR packing_master_id IS NULL) AND units_id=?7 AND status=?8 AND " +
                    "(subgroup_id=?9 OR subgroup_id IS NULL)",
            nativeQuery = true
    )
    ProductUnitPacking findNegativeStatus(Long productId, Long brandId, Long groupId,
                                          Long categoryId, Long subcategoryId, Long packagingId,
                                          Long unitsId, Boolean status, Long subgroupId);

    /****** PK visit ******/
    @Query(
            value = "select distinct(level_a_id) from `product_unit_packing_tbl` where product_id=?1 AND status=1",
            nativeQuery = true
    )
    List<Long> findLevelAIdDistinct(Long product_id);

    @Query(
            value = "select distinct level_b_id from `product_unit_packing_tbl` where product_id=?1 AND " +
                    "(level_a_id=?2 OR level_a_id IS NULL) AND status=1",
            nativeQuery = true
    )
    List<Long> findLevelBIdDistinct(Long product_id, Long id);

    @Query(
            value = "select distinct level_b_id from `product_unit_packing_tbl` where product_id=?1 AND  status=1",
            nativeQuery = true
    )
    List<Long> findLevelBIdDistinctProductId(Long product_id);

    @Query(
            value = "select distinct level_c_id from `product_unit_packing_tbl` where product_id=?1 AND " +
                    "(level_a_id=?2 OR level_a_id IS NULL) AND (level_b_id=?3 OR level_b_id IS NULL) " +
                    "AND status=1",
            nativeQuery = true
    )
    List<Long> findLevelCIdDistinct(Long product_id, Long levelA, Long levelB);

    @Query(
            value = "select * from `product_unit_packing_tbl` where product_id=?1 AND" +
                    "(level_a_id=?2 OR level_a_id IS NULL) AND (level_b_id=?3 OR level_b_id IS NULL) AND " +
                    "(level_c_id=?4 OR level_c_id IS NULL) AND status=1",
            nativeQuery = true
    )
    List<ProductUnitPacking> findByPackingUnits(Long product_id, Long mLevelA, Long mLevelB, Long mLevelC);


    @Query(
            value = "select distinct(level_b_id) from `product_unit_packing_tbl` where product_id=?1 AND status=1" +
                    " AND (level_a_id=?2 OR level_a_id IS NULL)",
            nativeQuery = true
    )
    List<Long> findByProductsLevelB(Long id, Long mLeveA);

    @Query(
            value = "select distinct(level_c_id) from `product_unit_packing_tbl` where product_id=?1 AND status=1" +
                    " AND (level_a_id=?2 OR level_a_id IS NULL) AND (level_b_id=?2 OR level_b_id IS NULL)",
            nativeQuery = true
    )
    List<Long> findByProductsLevelC(Long id, Long mLeveA, Long mLeveB);


    @Query(value = "SELECT units_id, units_tbl.unit_name, units_tbl.unit_code, product_unit_packing_tbl.unit_conversion FROM `product_unit_packing_tbl` LEFT JOIN" +
            " units_tbl ON product_unit_packing_tbl.units_id=units_tbl.id WHERE product_id=?1 AND " +
            "(level_a_id=?2 OR level_a_id IS NULL) AND (level_b_id IS NULL OR level_b_id=?3) AND (level_c_id IS NULL OR level_c_id=?4)", nativeQuery = true)
    List<Object[]> findUniqueUnitsByProductId(Long productId, Long levelAId, Long levelBId, Long levelCId);

    List<ProductUnitPacking> findByUnitsIdAndStatus(Long object, boolean b);

    List<ProductUnitPacking> findByLevelAIdAndStatus(Long object, boolean b);

    List<ProductUnitPacking> findByLevelBIdAndStatus(Long object, boolean b);

    List<ProductUnitPacking> findByLevelCIdAndStatus(Long object, boolean b);

    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND (level_a_id=?2 OR level_a_id IS NULL) " +
                    "AND (level_b_id=?3 OR level_b_id IS NULL) AND (level_c_id=?4 OR level_c_id IS NULL) AND " +
                    "units_id=?5 AND status=?6",
            nativeQuery = true
    )
    ProductUnitPacking findRate(Long id, Long levelAId, Long levelBId, Long levelCId, Long id1, Boolean status);

    /*** JavaFX ****/
    @Query(
            value = "select * from product_unit_packing_tbl WHERE product_id=?1 AND (level_a_id=?2 OR level_a_id IS NULL) " +
                    "AND (level_b_id=?3 OR level_b_id IS NULL) AND (level_c_id=?4 OR level_c_id IS NULL) AND " +
                    "status=?5",
            nativeQuery = true
    )
    ProductUnitPacking findRateMultiUnit(Long id, Long levelAId, Long levelBId, Long levelCId, Boolean status);

    @Query(
            value = "SELECT units_tbl.unit_name, SUM(qty),SUM(pur_price) FROM `stock_opening_closing_snap_tbl` " +
                    "LEFT JOIN units_tbl ON stock_opening_closing_snap_tbl.unit_id = units_tbl.id WHERE product_id=?1 " +
                    "AND tranx_type_id=?2 AND Date(stock_opening_closing_snap_tbl.tranx_date) BETWEEN ?3 AND ?4 GROUP BY unit_id", nativeQuery = true
    )
    List<Object[]> findPurQtyAndRateByProductIdAndTranxTypeId(Long product_id, int tranx_type_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT units_tbl.unit_name, SUM(qty),SUM(pur_price) FROM `stock_opening_closing_snap_tbl` " +
                    "LEFT JOIN units_tbl ON stock_opening_closing_snap_tbl.unit_id = units_tbl.id WHERE product_id=?1 " +
                    "AND tranx_type_id=?2 AND Date(stock_opening_closing_snap_tbl.tranx_date) BETWEEN ?3 AND ?4 GROUP BY unit_id", nativeQuery = true
    )
    List<Object[]> findPurRetQtyAndRateByProductIdAndTranxTypeId(Long product_id, int tranx_type_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT units_tbl.unit_name, SUM(qty),SUM(pur_price) FROM `stock_opening_closing_snap_tbl` " +
                    "LEFT JOIN units_tbl ON stock_opening_closing_snap_tbl.unit_id = units_tbl.id WHERE product_id=?1 " +
                    "AND tranx_type_id=?2 AND Date(stock_opening_closing_snap_tbl.tranx_date) BETWEEN ?3 AND ?4 GROUP BY unit_id", nativeQuery = true
    )
    List<Object[]> findPurChallQtyAndRateByProductIdAndTranxTypeId(Long product_id, int tranx_type_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT units_tbl.unit_name, SUM(qty),SUM(sales_price) FROM `stock_opening_closing_snap_tbl` " +
                    "LEFT JOIN units_tbl ON stock_opening_closing_snap_tbl.unit_id = units_tbl.id WHERE product_id=?1 " +
                    "AND tranx_type_id=?2 AND Date(stock_opening_closing_snap_tbl.tranx_date) BETWEEN ?3 AND ?4 GROUP BY unit_id", nativeQuery = true
    )
    List<Object[]> findsalesQtyAndRateByProductIdAndTranxTypeId(Long product_id, int tranx_type_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT units_tbl.unit_name, SUM(qty),SUM(sales_price) FROM `stock_opening_closing_snap_tbl` " +
                    "LEFT JOIN units_tbl ON stock_opening_closing_snap_tbl.unit_id = units_tbl.id WHERE product_id=?1 " +
                    "AND tranx_type_id=?2 AND Date(stock_opening_closing_snap_tbl.tranx_date) BETWEEN ?3 AND ?4 GROUP BY unit_id", nativeQuery = true
    )
    List<Object[]> findsallesRetQtyAndRateByProductIdAndTranxTypeId(Long product_id, int tranx_type_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT units_tbl.unit_name, SUM(qty),SUM(sales_price) FROM `stock_opening_closing_snap_tbl` " +
                    "LEFT JOIN units_tbl ON stock_opening_closing_snap_tbl.unit_id = units_tbl.id WHERE product_id=?1 " +
                    "AND tranx_type_id=?2 AND Date(stock_opening_closing_snap_tbl.tranx_date) BETWEEN ?3 AND ?4 GROUP BY unit_id", nativeQuery = true
    )
    List<Object[]> findSalesChallQtyAndRateByProductIdAndTranxTypeId(Long product_id, int tranx_type_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT unit_name FROM `units_tbl`", nativeQuery = true
    )
    List<Object[]> findUnitName();

    ProductUnitPacking findByProductIdAndUnitsId(Long productId, Long unitId);


    ProductUnitPacking findByProductIdAndUnitsIdAndIsRate(long l, long p, Boolean b);

    ProductUnitPacking findByProductIdAndIsRate(Long productId, boolean b);

    List<ProductUnitPacking> findByProductIdOrderByIdDesc(Long productId);

}
