package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.contra_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranxContraMasterRepository extends JpaRepository<TranxContraMaster,Long> {
    @Query(
            value = "select COUNT(*) from tranx_contra_master_tbl WHERE outlet_id=?1 AND branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    @Query(
            value = "select COUNT(*) from tranx_contra_master_tbl WHERE outlet_id=?1 And branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id,Long id1);
    TranxContraMaster findByIdAndStatus(long contra_id, boolean b);

    TranxContraMaster findByIdAndOutletIdAndStatus(Long contraId, Long id, boolean b);

    List<TranxContraMaster> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    List<TranxContraMaster> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);
}
