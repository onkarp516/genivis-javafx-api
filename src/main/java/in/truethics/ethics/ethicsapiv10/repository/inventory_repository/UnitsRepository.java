package in.truethics.ethics.ethicsapiv10.repository.inventory_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Units;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UnitsRepository extends JpaRepository<Units,Long> {

    Units findByIdAndStatus(Long unitId, boolean b);
    Units findFirstByUnitNameIgnoreCase(String unitName);

    List<Units> findByOutletIdAndBranchIdAndStatus(Long id, Long id1, boolean b);

    Units findByOutletIdAndBranchIdAndUnitNameIgnoreCaseAndStatus(Long id, Long id1, String unitName, boolean b);

    Units findByOutletIdAndUnitNameIgnoreCaseAndStatus(Long id, String unitName, boolean b);

    List<Units> findByOutletIdAndStatusAndBranchIsNull(Long id, boolean b);
    Units findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);
    Units findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    Units findByOutletIdAndBranchIdAndUnitNameIgnoreCaseAndStatusAndIdNot(Long id, Long id1, String unitName, boolean b, Long id2);

    Units findByOutletIdAndUnitNameIgnoreCaseAndStatusAndIdNot(Long id, String unitName, boolean b, Long id1);




}
