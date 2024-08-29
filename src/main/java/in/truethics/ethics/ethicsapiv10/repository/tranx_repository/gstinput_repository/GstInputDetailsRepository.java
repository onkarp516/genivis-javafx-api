package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.gstinput_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GstInputDetailsRepository extends JpaRepository<GstInputDetails,Long> {
    List<GstInputDetails> findByGstInputMasterIdAndStatus(Long id, boolean b);

    GstInputDetails findByIdAndStatus(Long details_id, boolean b);
}
