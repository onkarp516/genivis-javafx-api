package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.ContentMaster;
import in.truethics.ethics.ethicsapiv10.model.master.DoctorMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface DoctorMasterRepository extends JpaRepository<DoctorMaster, Long> {

    List<DoctorMaster> findByStatus(boolean b);

    DoctorMaster findByIdAndStatus(long id, boolean b);

    DoctorMaster findByIdAndOutletIdAndStatus(long id, Long id1, boolean b);
}
