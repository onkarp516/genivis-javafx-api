package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.ContentMasterDose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ContentMasterDoseRepository extends JpaRepository<ContentMasterDose, Long> {

    List<ContentMasterDose> findByStatus(boolean b);
}
