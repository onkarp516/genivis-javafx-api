package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.gstoutput_repository;

import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstouput.GstOutputDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GstOutputDetailsRepository extends JpaRepository<GstOutputDetails,Long> {
}
