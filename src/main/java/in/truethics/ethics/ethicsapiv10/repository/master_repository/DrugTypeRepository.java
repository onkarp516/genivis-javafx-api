package in.truethics.ethics.ethicsapiv10.repository.master_repository;


import in.truethics.ethics.ethicsapiv10.model.master.DrugType;
import in.truethics.ethics.ethicsapiv10.model.master.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrugTypeRepository extends JpaRepository<DrugType, Long> {

    List<DrugType> findByStatus(boolean b);
    DrugType findFirstByDrugNameIgnoreCase(String drugName);
}
