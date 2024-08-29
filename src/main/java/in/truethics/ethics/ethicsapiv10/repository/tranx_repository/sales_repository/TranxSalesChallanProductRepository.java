package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranxSalesChallanProductRepository extends JpaRepository<Product,Long> {
    List<Product> findByOutletId(Long id);

    Product findByIdAndStatus(long product_id, boolean b);
}
