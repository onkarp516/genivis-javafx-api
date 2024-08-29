package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrderDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesQuotationDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnDetailsUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxSalesQuotaionDetailsUnitsRepository extends JpaRepository<TranxSalesQuotationDetailsUnits, Long> {
    List<TranxSalesQuotationDetailsUnits> findBySalesQuotationDetailsIdAndStatus(Long id, boolean b);

    TranxSalesQuotationDetailsUnits findByIdAndStatus(Long details_id, boolean b);

    @Query(
            value = "SELECT * FROM tranx_sales_quotation_details_units_tbl WHERE sales_quotation_id=?1 AND product_id=?2 " +
                    "AND unit_id=?3 AND (packaging_id=?4 OR packaging_id IS NULL) AND " +
                    "(subcategory_id=?5 OR subcategory_id IS NULL) AND (category_id=?6 OR category_id IS NULL) " +
                    "AND (group_id=?7 OR group_id IS NULL) AND (brand_id=?8 OR brand_id IS NULL) AND status=?9", nativeQuery = true)
    TranxSalesQuotationDetailsUnits findByProductDetails(Long referenceId, Long prdId, Long unitId, Long packageId,
                                                         Long subCategoryId, Long categoryId, Long groupId,
                                                         Long brandsId, boolean b);

    @Query(
            value = "select * from tranx_sales_quotation_details_units_tbl WHERE product_id=?1 AND " +
                    "sales_quotation_details_id=?2 AND status=?3 GROUP BY brand_id",
            nativeQuery = true
    )
    List<TranxSalesQuotationDetailsUnits> findByUniqueBrands(Long productId, Long id, boolean b);

    @Query(
            value = "select * from tranx_sales_quotation_details_units_tbl WHERE product_id=?1 AND " +
                    "sales_quotation_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND status=?4 " +
                    "GROUP BY category_id",
            nativeQuery = true
    )
    List<TranxSalesQuotationDetailsUnits> findByUniqueCategoryListwithBrands(Long productId, Long id, Long subGroupId, boolean b);

    @Query(
            value = "select * from tranx_sales_quotation_details_units_tbl WHERE product_id=?1 AND " +
                    "sales_quotation_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND status=?6 " +
                    "GROUP BY subcategory_id",
            nativeQuery = true
    )
    List<TranxSalesQuotationDetailsUnits> findByUniqueSubCategoryListBrands(Long productId, Long id, Long brandId,
                                                                            Long groupId, Long categoryId, boolean b);

    /*   @Query(
               value = "select * from tranx_sales_quotation_details_units_tbl WHERE product_id=?1 AND " +
                       "sales_quotation_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                       "(category_id=?4 OR category_id IS NULL) AND (subcategory_id=?5 OR subcategory_id IS NULL) AND " +
                       "status=?6 GROUP BY flavour_master_id",
               nativeQuery = true
       )
       List<TranxSalesQuotationDetailsUnits> findByUniqueFlavourListBrands(Long productId, Long id, Long subGroupId, Long categoryId, Long subCateId, boolean b);
   */
    @Query(
            value = "select * from tranx_sales_quotation_details_units_tbl WHERE product_id=?1 AND " +
                    "sales_quotation_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "(subcategory_id=?6 OR subcategory_id IS NULL) AND status=?7 GROUP BY packaging_id",
            nativeQuery = true
    )
    List<TranxSalesQuotationDetailsUnits> findByUniquePackageListBrands(Long productId,
                                                                        Long id, Long brandId, Long groupId,
                                                                        Long categoryId, Long subCateId, boolean b);

    @Query(
            value = "select * from tranx_sales_quotation_details_units_tbl WHERE product_id=?1 AND " +
                    "sales_quotation_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "(subcategory_id=?6 OR subcategory_id IS NULL) AND " +
                    "(packaging_id=?7 OR packaging_id IS NULL) AND status=?8",
            nativeQuery = true
    )
    List<TranxSalesQuotationDetailsUnits> findByUniqueUnitsBrands(Long productId, Long id, Long brandId,
                                                                  Long groupId, Long categoryId, Long subCateId,
                                                                  Long packageId, boolean b);

    List<TranxSalesQuotationDetailsUnits> findBySalesQuotationIdAndStatus(long transactionId, boolean b);

    @Query(
            value = "select * from tranx_sales_quotation_details_units_tbl WHERE product_id=?1 AND " +
                    "sales_quotation_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND status=?4 " +
                    "GROUP BY group_id",
            nativeQuery = true
    )
    List<TranxSalesQuotationDetailsUnits> findByUniqueGroupListwithBrands(Long productId, Long id, Long brandId, boolean b);

    @Query(
            value = "select * from tranx_sales_quotation_details_units_tbl WHERE product_id=?1 AND " +
                    "sales_quotation_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND status=?5 " +
                    "GROUP BY category_id",
            nativeQuery = true
    )
    List<TranxSalesQuotationDetailsUnits> findByUniqueCategoryListBrands(Long productId, Long id, Long brandId, Long groupId, boolean b);

    @Query(
            value = " SELECT product_id FROM tranx_sales_quotation_details_units_tbl " +
                    "WHERE sales_quotation_id=?1 AND status=?2 GROUP BY product_id ", nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);

    @Query(
            value = "SELECT * FROM tranx_sales_quotation_details_units_tbl WHERE sales_quotation_id=?1 AND product_id=?2 " +
                    "AND unit_id=?3 AND (level_a_id=?4 OR level_a_id IS NULL) AND " +
                    "(level_b_id=?5 OR level_b_id IS NULL) AND (level_c_id=?6 OR level_c_id IS NULL) " +
                    "AND status=?7", nativeQuery = true)
    TranxSalesQuotationDetailsUnits findByProductDetailsLevel(Long referenceId, Long prdId, Long unitId,
                                                        Long levelAId, Long levelBId, Long levelCId, boolean b);


//    List<TranxSalesQuotationDetailsUnits> findByProductIdAndStatus(Long productId, boolean b);

    List<TranxSalesQuotationDetailsUnits> findBySalesQuotationIdAndTransactionStatusAndStatus(Long id, long l, boolean b);

    List<TranxSalesQuotationDetailsUnits> findByProductIdAndStatusOrderByIdDesc(Long product, boolean b);
}
