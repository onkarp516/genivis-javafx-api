package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.InventoryDetailsPostings;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TranxPurInvoiceDetailsUnitsRepository extends JpaRepository<TranxPurInvoiceDetailsUnits, Long> {

    List<TranxPurInvoiceDetailsUnits> findByPurInvoiceDetailsIdAndStatus(Long id, boolean b);

    TranxPurInvoiceDetailsUnits findByIdAndStatus(Long dtranx_purchase_invoice_details_units_tbletails_id, boolean b);

    @Query(
            value = "select * from tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_invoice_details_id=?2 AND status=?3 GROUP BY brand_id",
            nativeQuery = true
    )
    List<TranxPurInvoiceDetailsUnits> findByUniqueBrands(Long productId, Long detailsId, boolean b);

    @Query(
            value = "select * from tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_invoice_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND status=?4 " +
                    "GROUP BY category_id",
            nativeQuery = true
    )
    List<TranxPurInvoiceDetailsUnits> findByUniqueCategoryListwithBrands(Long productId, Long detailsId, Long subGroupId,
                                                                         boolean b);

    @Query(
            value = "select * from tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_invoice_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "status=?6 GROUP BY subcategory_id",
            nativeQuery = true
    )
    List<TranxPurInvoiceDetailsUnits> findByUniqueSubCategoryListBrands(Long productId, Long id, Long brandId,
                                                                        Long groupId, Long cateId, boolean b);

    /* @Query(
             value = "select * from tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 AND " +
                     "purchase_invoice_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                     "(category_id=?4 OR category_id IS NULL) AND (subcategory_id=?5 OR subcategory_id IS NULL) AND " +
                     "status=?6 GROUP BY flavour_master_id",
             nativeQuery = true
     )
     List<TranxPurInvoiceDetailsUnits> findByUniqueFlavourListBrands(Long productId, Long id, Long subGroupId, Long categoryId, Long subCateId, boolean b);
 */
    @Query(
            value = "select * from tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_invoice_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "(subcategory_id=?6 OR subcategory_id IS NULL) AND status=?7 GROUP BY packaging_id",
            nativeQuery = true
    )
    List<TranxPurInvoiceDetailsUnits> findByUniquePackageListBrands(Long productId, Long id,
                                                                    Long brandId, Long groupId, Long cateId,
                                                                    Long subCateId, boolean b);

    @Query(
            value = "select * from tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_invoice_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND (category_id=?5 OR category_id IS NULL) AND " +
                    "(subcategory_id=?6 OR subcategory_id IS NULL) AND " +
                    "(packaging_id=?7 OR packaging_id IS NULL) AND status=?8",
            nativeQuery = true
    )
    List<TranxPurInvoiceDetailsUnits> findByUniqueUnitsBrands(Long productId, Long id, Long brandId,
                                                              Long groupId, Long categoryId, Long subCateId,
                                                              Long packageId, boolean b);

    @Query(
            value = "select * from tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_invoice_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND status=?4 " +
                    "GROUP BY group_id",
            nativeQuery = true
    )
    List<TranxPurInvoiceDetailsUnits> findByUniqueGroups(Long productId, Long id, Long brandId, boolean b);

    @Query(
            value = "select * from tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 AND " +
                    "purchase_invoice_details_id=?2 AND (brand_id=?3 OR brand_id IS NULL) AND " +
                    "(group_id=?4 OR group_id IS NULL) AND status=?5 " +
                    "GROUP BY category_id",
            nativeQuery = true
    )
    List<TranxPurInvoiceDetailsUnits> findByUniqueCategoryListBrands(Long productId, Long id, Long brandId,
                                                                     Long groupId, boolean b);


    List<TranxPurInvoiceDetailsUnits> findByPurchaseTransactionIdAndStatus(long transactionId, boolean b);


    @Query(value = "SELECT COUNT(*) FROM tranx_purchase_invoice_details_units_tbl WHERE status=0 AND " +
            "purchase_invoice_id=?1 AND purchase_invoice_details_id=?2 ", nativeQuery = true)
    Integer findStatus(Long purchaseInvoiceId, Long detailsId);

    @Query(value = "SELECT COUNT(*) FROM tranx_purchase_invoice_details_units_tbl WHERE " +
            "purchase_invoice_id=?1 AND purchase_invoice_details_id=?2 ", nativeQuery = true)
    Integer findCount(Long purchaseInvoiceId, Long detailsId);


    @Query(
            value = "select * from tranx_purchase_invoice_details_units_tbl WHERE purchase_invoice_id=?1 AND " +
                    "product_id=?2 AND unit_id=?3 AND (packaging_id=?4 OR packaging_id IS NULL) AND " +
                    "(batch_id=?5 OR batch_id IS NULL) AND (brand_id=?6 OR brand_id IS NULL) AND " +
                    "(group_id=?7 OR group_id IS NULL) AND (category_id=?8 OR category_id IS NULL) AND " +
                    "(subcategory_id=?9 OR subcategory_id IS NULL) AND status=?10",
            nativeQuery = true
    )
    TranxPurInvoiceDetailsUnits findProductQty(Long invoiceId, Long productId, Long unitId, Long packageId,
                                               Long batchId, Long brandId, Long groupId, Long categoryId,
                                               Long subcategoryId, boolean status);


    @Query(value = "select * from tranx_purchase_invoice_details_units_tbl WHERE purchase_invoice_id=?1",
            nativeQuery = true)
    List<TranxPurInvoiceDetailsUnits> findByClosedStatus(Long id);

    TranxPurInvoiceDetailsUnits findTop1ByProductBatchNoIdOrderByIdDesc(Long id);

    TranxPurInvoiceDetailsUnits findTop1ByProductIdOrderByIdDesc(Long id);

    @Query(value = "SELECT IFNULL(SUM(base_amt),'') FROM `tranx_purchase_order_details_units_tbl` WHERE product_id=?1", nativeQuery = true)
    String findCostByProductId(Long id);

    @Query(
            value = " SELECT product_id FROM tranx_purchase_invoice_details_units_tbl " +
                    "WHERE purchase_invoice_id=?1 AND status=?2 GROUP BY product_id  "
            , nativeQuery = true
    )
    List<Object[]> findByInvoiceIdAndStatus(Long id, boolean b);

    List<TranxPurInvoiceDetailsUnits> findByProductIdAndStatusOrderByIdDesc(Long productId, boolean b);

    @Query(
            value = "SELECT IFNULL(avg(rate),0.0) FROM tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 and unit_id=?2 and status=?3", nativeQuery = true
    )
    TranxPurInvoiceDetailsUnits findAvgofRate(Long productId, Long id, boolean b);

    @Query(
            value = " SELECT IFNULL(avg(rate),0.0) FROM tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 and unit_id=?2", nativeQuery = true
    )
    Double findTotalValue(Long productId, Long id);

    @Query(
            value = "SELECT IFNULL(SUM(qty),0.0) FROM `tranx_purchase_invoice_details_units_tbl` " +
                    "WHERE product_id=?1 AND batch_id=?2 AND DATE(created_at) BETWEEN ?3 AND ?4", nativeQuery = true
    )
    Double findExpiredPurSumQty(Long productId, Long batchId, LocalDate startMonthDate, LocalDate endMonthDate, boolean b);

    @Query(
            value = " SELECT IFNULL(avg(total_amount),0.0) FROM tranx_purchase_invoice_details_units_tbl WHERE " +
                    "product_id=?1 and batch_id=?2", nativeQuery = true
    )
    Double findTotalBatchValue(Long productId, Long batchId);

    TranxPurInvoiceDetailsUnits findByPurchaseTransactionIdAndProductIdAndProductBatchNoId(
            Long invoiceId, Long productId, Long batchId);


    List<TranxPurInvoiceDetailsUnits> findTop5ByProductIdAndStatusOrderByIdDesc(Long productId, boolean b);
    List<TranxPurInvoiceDetailsUnits> findTop5ByProductIdOrderByIdDesc(Long productId);

    @Query(
            value = "SELECT * FROM `tranx_purchase_invoice_details_units_tbl` WHERE purchase_invoice_id=?1 AND status=?2", nativeQuery = true)
    List<TranxPurInvoiceDetailsUnits> findPurchaseInvoicesDetails(Long id, boolean b);

    TranxPurInvoiceDetailsUnits findByPurchaseTransactionIdAndStatusAndProductId(Long id, boolean b, Long id1);

    @Query(
            value = "SELECT * FROM tranx_purchase_invoice_details_units_tbl WHERE purchase_invoice_id=?1 AND product_id=?2 AND status=1",nativeQuery = true
    )
    List<TranxPurInvoiceDetailsUnits> findByPurchaseInvoiceIdAndProductIdAndStatus1(Long id, Long productId, boolean b);

//    List<TranxPurReturnInvoice> findTop5BySundryCreditorsIdAndStatusOrderByIdDesc(Long ledgerId, boolean b);


    @Query(
            value = "SELECT SUM(qty),SUM(total_amount) FROM tranx_purchase_invoice_details_units_tbl WHERE product_id=?1 And status=?2", nativeQuery = true
    )
    String findSumByProductId(String productId, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(a.qty),0.0),IFNULL(SUM(a.rate),0.0),a.unit_id FROM tranx_purchase_invoice_details_units_tbl AS a" +
                    " LEFT JOIN tranx_purchase_invoice_tbl ON " +
                    "a.purchase_invoice_id=tranx_purchase_invoice_tbl.id WHERE product_id=?1 " +
                    "AND a.status=?2 AND tranx_purchase_invoice_tbl.invoice_date BETWEEN ?3 AND ?4",nativeQuery = true
    )
    String findPurQtyByProductIdAndStatus(Long productId, boolean b, String toString, String toString1);


//    @Query(
//            value = "SELECT a.qty, a.rate, a.unit_id FROM tranx_purchase_invoice_details_units_tbl AS a" +
//                    " LEFT JOIN tranx_purchase_invoice_tbl ON " +
//                    "a.purchase_invoice_id=tranx_purchase_invoice_tbl.id WHERE product_id=?1 " +
//                    "AND a.status=?2 AND tranx_purchase_invoice_tbl.invoice_date BETWEEN ?3 AND ?4",nativeQuery = true
//    )
//    List findPurQtyByProductIdAndStatus(Long productId, boolean b, String toString, String toString1);
    @Query(
            value = " SELECT COUNT(*) FROM tranx_purchase_invoice_details_units_tbl WHERE "+
                    "batch_id=?1 AND status=?2", nativeQuery = true
    )
    Long countBatchExists(Long id, boolean b);

    @Query(
            value = " SELECT * FROM tranx_purchase_invoice_details_tbl " +
                    "WHERE purchase_invoice_id=?1 AND status=?2 AND id IN(?3) "
            , nativeQuery = true
    )
    List<TranxPurInvoiceDetailsUnits> findInvoiceByIdWithProductsId(Long id, boolean b, String str);
}
