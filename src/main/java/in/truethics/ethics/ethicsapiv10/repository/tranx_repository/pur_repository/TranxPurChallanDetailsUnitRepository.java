package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallanDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrderDetailsUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPurChallanDetailsUnitRepository extends JpaRepository<TranxPurChallanDetailsUnits, Long> {
    List<TranxPurChallanDetailsUnits> findByTranxPurChallanDetailsIdAndStatus(Long id, boolean b);

    TranxPurChallanDetailsUnits findByIdAndStatus(Long details_id, boolean b);

    @Query(
            value = "SELECT * FROM tranx_purchase_challan_details_units_tbl WHERE purchase_challan_id=?1 AND product_id=?2 " +
                    "AND unit_id=?3 AND (packaging_id=?4 OR packaging_id IS NULL) AND " +
                    "(subcategory_id=?5 OR subcategory_id IS NULL) AND (category_id=?6 OR category_id IS NULL) " +
                    "AND (group_id=?7 OR group_id IS NULL) AND (brand_id=?8 OR brand_id IS NULL)" +
                    "AND status=?9", nativeQuery = true)
    TranxPurChallanDetailsUnits findByProductDetails(Long referenceId, Long prdId, Long unitId, Long packageId,
                                                    Long subCategoryId, Long categoryId,  Long groupId,
                                                     Long brandId, boolean b);

    @Query(
            value = "select * from tranx_purchase_challan_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_challan_details_id=?2 AND status=?3 GROUP BY brand_id",
            nativeQuery = true
    )
    List<TranxPurChallanDetailsUnits> findByUniqueBrands(Long productId, Long id, boolean b);

    @Query(
            value = "select * from tranx_purchase_challan_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_challan_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND status=?4 " +
                    "GROUP BY category_id",
            nativeQuery = true
    )
    List<TranxPurChallanDetailsUnits> findByUniqueCategoryListwithBrands(Long productId, Long id, Long subGroupId, boolean b);

    @Query(
            value = "select * from tranx_purchase_challan_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_challan_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND status=?6 " +
                    "GROUP BY subcategory_id",
            nativeQuery = true
    )
    List<TranxPurChallanDetailsUnits> findByUniqueSubCategoryListBrands(Long productId, Long id,
                                                                        Long brandId,Long groupId, Long categoryId, boolean b);

    /*@Query(
            value = "select * from tranx_purchase_challan_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_challan_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(category_id=?4 OR category_id IS NULL) AND (subcategory_id=?5 OR subcategory_id IS NULL) AND " +
                    "status=?6 GROUP BY flavour_master_id",
            nativeQuery = true
    )
    List<TranxPurChallanDetailsUnits> findByUniqueFlavourListBrands(Long productId, Long id, Long subGroupId, Long categoryId, Long subCateId, boolean b);
*/
    @Query(
            value = "select * from tranx_purchase_challan_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_challan_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "(subcategory_id=?6 OR subcategory_id IS NULL) AND status=?7 GROUP BY packaging_id",
            nativeQuery = true
    )
    List<TranxPurChallanDetailsUnits> findByUniquePackageListBrands(Long productId, Long id, Long subGroupId, Long categoryId, Long subCateId, Long flavourId, boolean b);

    @Query(
            value = "select * from tranx_purchase_challan_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_challan_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "(subcategory_id=?6 OR subcategory_id IS NULL) AND " +
                    "(packaging_id=?7 OR packaging_id IS NULL) AND status=?8",
            nativeQuery = true
    )
    List<TranxPurChallanDetailsUnits> findByUniqueUnitsBrands(Long productId, Long id, Long subGroupId, Long categoryId, Long subCateId, Long flavourId, Long packageId, boolean b);

    List<TranxPurChallanDetailsUnits> findByTranxPurChallanIdAndStatus(long transactionId, boolean b);

    @Query(
            value = "select * from tranx_purchase_challan_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_challan_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND status=?4 " +
                    "GROUP BY group_id",
            nativeQuery = true
    )
    List<TranxPurChallanDetailsUnits> findByUniqueGroupListwithBrands(Long productId, Long id, Long brandId, boolean b);

    @Query(
            value = "select * from tranx_purchase_challan_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_challan_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND status=?5 " +
                    "GROUP BY category_id",
            nativeQuery = true
    )
    List<TranxPurChallanDetailsUnits> findByUniqueCategoryListBrands(Long productId, Long id, Long brandId,
                                                                     Long groupId, boolean b);

    @Query(value = "SELECT COUNT(*) FROM tranx_purchase_challan_details_units_tbl WHERE " +
            "purchase_challan_id=?1 AND purchase_challan_details_id=?2 ",nativeQuery = true)
    Integer findCount(Long purchaseInvoiceId, Long detailsId);

    @Query(value = "SELECT COUNT(*) FROM tranx_purchase_challan_details_units_tbl WHERE status=0 AND " +
            "purchase_challan_id=?1 AND purchase_challan_details_id=?2 ",nativeQuery = true)
    Integer findStatus(Long purchaseInvoiceId, Long detailsId);

    @Query(
            value = " SELECT product_id FROM tranx_purchase_challan_details_units_tbl " +
                    "WHERE purchase_challan_id=?1 AND status=?2 GROUP BY product_id  "
            , nativeQuery = true
    )
    List<Object[]> findByTranxPurId(Long id, boolean b);

    @Query(
            value = "SELECT * FROM tranx_purchase_challan_details_units_tbl WHERE purchase_challan_id=?1 AND product_id=?2 " +
                    "AND unit_id=?3 AND (level_a_id=?4 OR level_a_id IS NULL) AND " +
                    "(level_b_id=?5 OR level_b_id IS NULL) AND (level_c_id=?6 OR level_c_id IS NULL) " +
                    "AND status=?7", nativeQuery = true)
    TranxPurChallanDetailsUnits findByProductDetailsLevel(Long referenceId, Long prdId, Long unitId,
                                                        Long levelAId, Long levelBId, Long levelCId, boolean b);


    List<TranxPurChallanDetailsUnits> findByTranxPurChallanIdAndTransactionStatusAndStatus(Long id, long l, boolean b);

    List<TranxPurChallanDetailsUnits> findByProductIdAndStatusOrderByIdDesc(Long productId, boolean b);




    TranxPurChallanDetailsUnits findByTranxPurChallanIdAndProductIdAndStatus(Long id, Long productId, boolean b);

    @Query(
            value = " SELECT COUNT(*) FROM tranx_purchase_challan_details_units_tbl WHERE " +
                    "batch_id=?1 AND status=?2", nativeQuery = true
    )
    Long countChallanBatchExists(Long id, boolean b);
}

