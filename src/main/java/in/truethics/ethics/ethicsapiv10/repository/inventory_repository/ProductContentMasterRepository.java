package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;


import in.truethics.ethics.ethicsapiv10.model.inventory.ProductContentMaster;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductOpeningStocks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
public interface ProductContentMasterRepository extends JpaRepository<ProductContentMaster, Long> {



    List<ProductContentMaster> findByProductIdAndStatus(Long id,boolean b);

    ProductContentMaster findByIdAndStatus(long id, boolean b);

    @Query(
            value = "SELECT COUNT(*) FROM genivis_gvmh002_db.product_content_master_tbl WHERE content_type = ?1",
            nativeQuery = true
    )
    int countOfContentType(String contentType);
}
