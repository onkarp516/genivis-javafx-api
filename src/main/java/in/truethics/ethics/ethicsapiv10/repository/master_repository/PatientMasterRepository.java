package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.DoctorMaster;
import in.truethics.ethics.ethicsapiv10.model.master.PatientMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientMasterRepository extends JpaRepository<PatientMaster, Long> {

    List<PatientMaster> findByStatus(boolean b);

    PatientMaster findByIdAndStatus(long id, boolean b);

    PatientMaster findByIdAndOutletIdAndStatus(long id, Long id1, boolean b);

    PatientMaster findByPatientCodeAndStatus(String debtors_id, boolean b);

    PatientMaster findTopByOrderByIdDesc();
}
