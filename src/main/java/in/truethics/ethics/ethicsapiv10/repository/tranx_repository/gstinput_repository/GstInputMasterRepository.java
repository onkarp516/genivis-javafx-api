package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.gstinput_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GstInputMasterRepository extends JpaRepository<GstInputMaster, Long> {

    @Query(
            value = "select COUNT(*) from tranx_gst_input_tbl WHERE outlet_id=?1 And branch_id=?2", nativeQuery = true
    )
    Long findBranchLastRecord(Long id, Long id1);

    @Query(
            value = "select COUNT(*) from tranx_gst_input_tbl WHERE outlet_id=?1 AND branch_id IS NULL", nativeQuery = true
    )
    Long findLastRecord(Long id);

    List<GstInputMaster> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);

    List<GstInputMaster> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    GstInputMaster findByIdAndStatus(Long gstInputId, boolean b);
}
