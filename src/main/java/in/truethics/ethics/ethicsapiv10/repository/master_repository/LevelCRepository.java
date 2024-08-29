package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LevelC;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelCRepository extends JpaRepository<LevelC,Long> {
    LevelC findByOutletIdAndBranchIdAndStatusAndLevelNameIgnoreCase(Long id, Long id1, boolean b, String levelName);

    LevelC findByOutletIdAndStatusAndLevelNameIgnoreCase(Long id, boolean b, String levelName);

    LevelC findByIdAndStatus(long id, boolean b);

    List<LevelC> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    List<LevelC> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    List<LevelC> findByOutletIdAndBranchIdAndStatus(Long id, Long id1, boolean b);

    LevelC findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    LevelC findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    LevelC findByOutletIdAndStatusAndLevelNameIgnoreCaseAndBranchIsNull(Long id, boolean b, String levelName);

    LevelC findByOutletIdAndBranchIdAndStatusAndLevelNameIgnoreCaseAndIdNot(Long id, Long id1, boolean b, String levelName, Long id2);

    LevelC findByOutletIdAndStatusAndLevelNameIgnoreCaseAndIdNotAndBranchIsNull(Long id, boolean b, String levelName, Long id1);
}
