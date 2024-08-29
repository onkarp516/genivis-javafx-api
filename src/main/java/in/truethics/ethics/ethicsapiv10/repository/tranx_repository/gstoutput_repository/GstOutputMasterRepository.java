package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.gstoutput_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstouput.GstOutputMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GstOutputMasterRepository extends JpaRepository<GstOutputMaster,Long> {
}
