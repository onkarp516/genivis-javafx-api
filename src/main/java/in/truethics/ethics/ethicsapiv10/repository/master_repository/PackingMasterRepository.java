package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.PackingMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackingMasterRepository extends JpaRepository<PackingMaster,Long> {

    PackingMaster findByIdAndStatus(long aPackage, boolean b);
    PackingMaster findFirstByPackNameIgnoreCase(String package_name);


    List<PackingMaster> findByOutletIdAndBranchIdAndStatus(Long id, Long id1, boolean b);

    PackingMaster findByOutletIdAndBranchIdAndPackNameIgnoreCaseAndStatus(Long id, Long id1, String package_name, boolean b);

    List<PackingMaster> findByOutletIdAndStatusAndBranchIsNull(Long id, boolean b);

    PackingMaster findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    PackingMaster findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    PackingMaster findByOutletIdAndPackNameIgnoreCaseAndStatusAndBranchIsNull(Long id, String package_name, boolean b);

    PackingMaster findByOutletIdAndBranchIdAndPackNameIgnoreCaseAndStatusAndIdNot(Long id, Long id1, String packageName, boolean b, Long id2);

    PackingMaster findByOutletIdAndPackNameIgnoreCaseAndStatusAndIdNotAndBranchIsNull(Long id, String packageName, boolean b, Long b1);
}
