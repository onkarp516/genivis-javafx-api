package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.PincodeMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PincodeMasterRepository extends JpaRepository<PincodeMaster, Long> {
    List<PincodeMaster> findByPincode(String pincode);
    PincodeMaster findByIdAndPincode(Long id, String pincode);

    PincodeMaster findByAreaAndDistrictAndPincode(String area, String district, String pincode);
}
