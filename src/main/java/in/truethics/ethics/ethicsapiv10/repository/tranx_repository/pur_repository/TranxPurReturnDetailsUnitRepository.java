package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrderDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnDetailsUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxPurReturnDetailsUnitRepository extends JpaRepository<TranxPurReturnDetailsUnits, Long> {


    @Query(
            value = "select * from tranx_purchase_return_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_return_details_id=?2 AND status=?3 GROUP BY brand_id",
            nativeQuery = true
    )
    List<TranxPurReturnDetailsUnits> findByUniqueBrands(Long productId, Long id, boolean b);

    @Query(
            value = "select * from tranx_purchase_return_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_return_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND status=?4 " +
                    "GROUP BY category_id",
            nativeQuery = true
    )
    List<TranxPurReturnDetailsUnits> findByUniqueCategoryListwithBrands(Long productId, Long id, Long subGroupId, boolean b);

    @Query(
            value = "select * from tranx_purchase_return_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_return_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "status=?6 GROUP BY subcategory_id",
            nativeQuery = true
    )
    List<TranxPurReturnDetailsUnits> findByUniqueSubCategoryListBrands(Long productId, Long id,
                                                                       Long brandId, Long groupId, Long categoryId, boolean b);

    /*@Query(
            value = "select * from tranx_purchase_return_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_return_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(category_id=?4 OR category_id IS NULL) AND (subcategory_id=?5 OR subcategory_id IS NULL) AND " +
                    "status=?6 GROUP BY flavour_master_id",
            nativeQuery = true
    )
    List<TranxPurReturnDetailsUnits> findByUniqueFlavourListBrands(Long productId, Long id, Long subGroupId, Long categoryId, Long subCateId, boolean b);
*/
    @Query(
            value = "select * from tranx_purchase_return_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_return_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "(subcategory_id=?6 OR subcategory_id IS NULL) AND status=?7 GROUP BY packaging_id",
            nativeQuery = true
    )
    List<TranxPurReturnDetailsUnits> findByUniquePackageListBrands(Long productId, Long id, Long subGroupId, Long categoryId, Long subCateId, Long flavourId, boolean b);

    @Query(
            value = "select * from tranx_purchase_return_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_return_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "(subcategory_id=?6 OR subcategory_id IS NULL) AND " +
                    "(packaging_id=?7 OR packaging_id IS NULL) AND status=?8",
            nativeQuery = true
    )
    List<TranxPurReturnDetailsUnits> findByUniqueUnitsBrands(Long productId, Long id, Long subGroupId, Long categoryId, Long subCateId, Long flavourId, Long packageId, boolean b);


    List<TranxPurReturnDetailsUnits> findByTranxPurReturnInvoiceIdAndStatus(long transactionId, boolean b);

    @Query(
            value = "select * from tranx_purchase_return_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_return_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND status=?4 " +
                    "GROUP BY group_id",
            nativeQuery = true
    )
    List<TranxPurReturnDetailsUnits> findByUniqueGroups(Long productId, Long id, Long brandId, boolean b);

    @Query(
            value = "select * from tranx_purchase_return_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_return_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND status=?5 " +
                    "GROUP BY category_id",
            nativeQuery = true
    )
    List<TranxPurReturnDetailsUnits> findByUniqueCategoryListBrands(Long productId, Long id, Long brandId, Long groupId,
                                                                    boolean b);

    @Query(value = "SELECT COUNT(*) FROM tranx_purchase_return_details_units_tbl WHERE status=0 AND " +
            "purchase_return_invoice_id=?1 AND purchase_return_details_id=?2 ", nativeQuery = true)
    Integer findStatus(Long purchaseInvoiceId, Long detailsId);

    @Query(value = "SELECT COUNT(*) FROM tranx_purchase_return_details_units_tbl WHERE " +
            "purchase_return_invoice_id=?1 AND purchase_return_details_id=?2 ", nativeQuery = true)
    Integer findCount(Long purchaseInvoiceId, Long detailsId);

    @Query(
            value = " SELECT product_id FROM tranx_purchase_return_details_units_tbl " +
                    "WHERE purchase_return_invoice_id=?1 AND status=?2 GROUP BY product_id " , nativeQuery = true)
    List<Object[]> findByTranxPurId(Long id, boolean b);


    List<TranxPurReturnDetailsUnits> findByProductIdAndStatusOrderByIdDesc(Long productId, boolean b);

    TranxPurReturnDetailsUnits findByTranxPurReturnInvoiceIdAndProductIdAndStatus(Long id, Long productId, boolean b);


//    @Query("SELECT * FROM `tranx_purchase_return_details_units_tbl` WHERE purchase_return_invoice_id=1 " +
//            "GROUP BY purchase_return_invoice_id")
//    List<TranxPurInvoiceDetailsUnits> findInvoices(Long ledgerId, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(a.qty),0.0),IFNULL(SUM(a.rate),0.0), a.unit_id FROM tranx_purchase_return_details_units_tbl AS a " +
                    "LEFT JOIN tranx_pur_return_invoice_tbl ON a.purchase_return_invoice_id=tranx_pur_return_invoice_tbl.id" +
                    " WHERE product_id=?1 AND a.status=?2 AND " +
                    "tranx_pur_return_invoice_tbl.transaction_date BETWEEN ?3 AND ?4",nativeQuery = true
    )
    String findPurReturnQtyByProductIdAndStatus(Long productId, boolean b, String toString, String toString1);
//@Query(
//        value = "SELECT SUM(a.qty),a.rate, a.unit_id FROM tranx_purchase_return_details_units_tbl AS a " +
//                "LEFT JOIN tranx_pur_return_invoice_tbl ON a.purchase_return_invoice_id=tranx_pur_return_invoice_tbl.id" +
//                " WHERE product_id=?1 AND a.status=?2 AND " +
//                "tranx_pur_return_invoice_tbl.transaction_date BETWEEN ?3 AND ?4",nativeQuery = true
//)
//List findPurReturnQtyByProductIdAndStatus(Long productId, boolean b, String toString, String toString1);

    TranxPurReturnDetailsUnits findByIdAndStatus(Long detailsId, boolean b);
}
