package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LevelA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelARepository extends JpaRepository<LevelA, Long> {
    LevelA findByOutletIdAndBranchIdAndStatusAndLevelNameIgnoreCase(Long id, Long id1, boolean b, String levelName);


    LevelA findByIdAndStatus(long id, boolean b);

    List<LevelA> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    List<LevelA> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    LevelA findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    LevelA findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    LevelA findByOutletIdAndStatusAndLevelNameIgnoreCaseAndBranchIsNull(Long id, boolean b, String levelName);

    LevelA findByOutletIdAndBranchIdAndStatusAndLevelNameIgnoreCaseAndIdNot(Long id, Long id1, boolean b, String levelName, Long id2);

    LevelA findByOutletIdAndStatusAndLevelNameIgnoreCaseAndIdNotAndBranchIsNull(Long id, boolean b, String levelName, Long id1);
}
