package in.truethics.ethics.ethicsapiv10.repository.product_barcode;

import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductBarcodeRepository extends JpaRepository<ProductBarcode, Long> {

    @Query(
            value = "select COUNT(*) from product_barcode_tbl WHERE outlet_id=?1 AND status=1 ", nativeQuery = true
    )
    Long findLastRecord(Long id);

    ProductBarcode findByProductIdAndOutletIdAndStatus(Long product_id, Long id, boolean b);


    @Query(
            value = "select * from product_barcode_tbl WHERE product_id=?1 AND outlet_id=?2 AND status=?3 " +
                    "AND unit_id=?4 AND (packaging_id=?5 OR packaging_id IS NULL)", nativeQuery = true
    )
    ProductBarcode findByBatch(Long product_id, Long outlet_id, boolean status, Long unit_id, Long package_id);

    List<ProductBarcode> findByTransactionIdAndStatus(Long id, boolean b);

    ProductBarcode findByProductIdAndOutletIdAndStatusAndTransactionIdAndTransactionTypeMasterId(Long id, Long id1, boolean b, Long id2, Long id3);

    ProductBarcode findByProductIdAndOutletIdAndStatusAndTransactionIdAndTransactionTypeMasterIdAndProductBatchId(

            Long id, Long id1, boolean b, Long id2, Long id3, Long id4);
}
