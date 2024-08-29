package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LevelB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelBRepository extends JpaRepository<LevelB,Long> {
    LevelB findByOutletIdAndBranchIdAndStatusAndLevelNameIgnoreCase(Long id, Long id1, boolean b, String levelName);

    LevelB findByOutletIdAndStatusAndLevelNameIgnoreCase(Long id, boolean b, String levelName);

    LevelB findByIdAndStatus(long id, boolean b);

    List<LevelB> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    List<LevelB> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    LevelB findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    LevelB findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    LevelB findByOutletIdAndStatusAndLevelNameIgnoreCaseAndBranchIsNull(Long id, boolean b, String levelName);

    LevelB findByOutletIdAndBranchIdAndStatusAndLevelNameIgnoreCaseAndIdNot(Long id, Long id1, boolean b, String levelName, Long id2);

    LevelB findByOutletIdAndStatusAndLevelNameIgnoreCaseAndIdNotAndBranchIsNull(Long id, boolean b, String levelName, Long id1);
}
