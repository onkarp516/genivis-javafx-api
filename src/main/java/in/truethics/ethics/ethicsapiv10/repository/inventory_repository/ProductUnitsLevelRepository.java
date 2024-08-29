package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitsLevelMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductUnitsLevelRepository extends JpaRepository<ProductUnitsLevelMapping,Long> {
}
