package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;


import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Product findByIdAndStatus(long product_id, boolean b);

    List<Product> findByOutletIdAndStatus(Long id, boolean b);


    List<Product> findByOutletIdAndBranchIdAndStatus(Long id, Long id1, boolean b);


    @Query(
            value = "SELECT * FROM `product_tbl` WHERE outlet_id=?1 AND branch_id=?2 " +
                    "AND product_code=?3 AND status=?4", nativeQuery = true
    )
    Product findByduplicateProductWithBranch(Long id, Long id1,
                                             String product_code, boolean b);

    @Query(
            value = "SELECT * FROM `product_tbl` WHERE outlet_id=?1 AND branch_id=?2 " +
                    "AND barcode_no=?3 AND status=?4", nativeQuery = true
    )
    Product findByduplicateProductBarcodeWithBranch(Long id, Long id1,
                                             String barcode_no, boolean b);

    @Query(
            value = "SELECT * FROM `product_tbl` WHERE outlet_id=?1 " +
                    "AND barcode_no=?2 AND status=?3", nativeQuery = true
    )
    Product findByduplicateProductBarcode(Long id,
                                                    String barcode_no, boolean b);

    @Query(
            value = "SELECT * FROM `product_tbl` WHERE outlet_id=?1 AND branch_id IS NULL " +
                    "AND product_code=?2 AND status=?3", nativeQuery = true
    )
    Product findByduplicateProduct(Long outletId, String product_code, Boolean b);

    @Query(
            value = "SELECT * FROM `product_tbl` WHERE outlet_id=?1 AND branch_id IS NULL " +
                    "AND product_name=?2 AND packing_master_id=?3 AND status=?4", nativeQuery = true
    )
    Product findByduplicateProductPK(Long outletId, String product_name, Long package_id, Boolean b);

    @Query(
            value = "SELECT * FROM `product_tbl` WHERE outlet_id=?1 AND branch_id=?2 " +
                    "AND product_name=?3 AND packing_master_id=?4 AND status=?5", nativeQuery = true
    )
    Product findByduplicateProductPKWBR(Long outletId, Long branchId, String product_name, Long product_package, Boolean b);


    List<Product> findByOutletIdAndStatusAndBranchIsNull(Long id, boolean b);

    Product findByIdAndOutletIdAndBranchIdAndStatus(Long productId, Long id, Long id1, boolean b);


    Product findByIdAndOutletIdAndStatusAndBranchIsNull(Long productId, Long id, boolean b);

    @Query(
            value = " SELECT * FROM `product_tbl` WHERE outlet_id=?1 AND branch_id=?2 " +
                    "AND (product_name LIKE ?3 OR product_code LIKE ?3 OR packing_master_id LIKE ?3 OR barcode_no LIKE ?3) " +
                    "AND ", nativeQuery = true
    )
    List<Product> findSearchKeyWithBranch(Long id, Long id1, String searchKey);

    @Query(
            value = " SELECT * FROM `product_tbl` WHERE outlet_id=?1 AND branch_id IS NULL " +
                    "AND (product_name LIKE ?3 OR product_code LIKE ?3 OR packing_master_id LIKE ?3 OR barcode_no LIKE ?3) " +
                    "AND ", nativeQuery = true
    )
    List<Product> findSearchKey(Long id, String searchKey);

    List<Product> findByBrandIdAndStatus(Long object, boolean b);

    List<Product> findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIdAndGroupIdAndStatus(Long id, Long id1, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIsNullAndGroupIdAndStatus(Long id, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIdAndCategoryIdAndStatus(Long id, Long id1, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIsNullAndCategoryIdAndStatus(Long id, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIdAndBrandIdAndStatus(Long id, Long id1, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIdAndSubcategoryIdAndStatus(Long id, Long id1, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIsNullAndSubcategoryIdAndStatus(Long id, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIsNullAndBrandIdAndStatus(Long id, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIdAndPackingMasterIdAndStatus(Long id, Long id1, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIsNullAndPackingMasterIdAndStatus(Long id, Long object, boolean b);

    Product findTopByStatusOrderByIdDesc(boolean b);


    @Query(
            value = "SELECT * FROM `product_tbl` WHERE outlet_id=?1 AND branch_id IS NULL " +
                    "AND product_name=?2 AND status=?3", nativeQuery = true
    )
    Product findByduplicateProductPN(Long id, String productName, boolean b);

    @Query(
            value = "SELECT * FROM `product_tbl` WHERE outlet_id=?1 AND branch_id=?2 " +
                    "AND product_name=?3 AND status=?4", nativeQuery = true
    )
    Product findByduplicateProductPNWBR(Long id, Long id1, String productName, boolean b);

    Product findByProductNameAndPackingMasterIdAndOutletIdAndBranchIdAndStatus(String productName,
                                                                               Long packageId, Long id, Long id1, boolean b);

    Product findByProductNameAndOutletIdAndBranchIdAndStatus(String productName, Long id, Long id1, boolean b);

    Product findByProductNameAndPackingMasterIdAndOutletIdAndStatusAndBranchIsNull(String productName, Long packageId,
                                                                                   Long id, boolean b);

    Product findByProductNameAndOutletIdAndStatusAndBranchIsNull(String productName, Long id, boolean b);

    Product findByProductCodeAndStatusAndOutletIdAndBranchId(String productCode, boolean b, Long id, Long id1);

    Product findByProductCode(String productCode);

    Product findFirstByProductCodeAndProductNameAndOutletId(String productCode, String productName, Long outletId);

    Product findByProductCodeAndStatusAndOutletIdAndBranchIsNull(String productCode, boolean b, Long id);

    List<Product> findByOutletIdAndBranchIdAndSubgroupIdAndStatus(Long id, Long id1, Long object, boolean b);

    List<Product> findByOutletIdAndBranchIsNullAndSubgroupIdAndStatus(Long id, Long object, boolean b);

    Product findByIdAndStatusAndIsDelete(Long productId, boolean b, boolean b1);

    Product findFirstByProductCode(String productCode);





    @Query(
            value = "SELECT * FROM genivis_pharma_db.product_tbl WHERE drug_contents LIKE %:contentName%",
            nativeQuery = true
    )
    List<Product> findByContentName(@Param("contentName") String contentName);


}
