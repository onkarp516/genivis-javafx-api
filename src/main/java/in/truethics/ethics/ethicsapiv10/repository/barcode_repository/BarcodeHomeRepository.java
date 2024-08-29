package in.truethics.ethics.ethicsapiv10.repository.barcode_repository;

import in.truethics.ethics.ethicsapiv10.model.barcode.BarcodeHome;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarcodeHomeRepository extends JpaRepository<BarcodeHome, Long> {

    BarcodeHome findFirstByStatus(boolean b);
    BarcodeHome findByIdAndStatus(Long id, boolean b);
}
