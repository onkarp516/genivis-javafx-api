package in.truethics.ethics.ethicsapiv10.repository.master_repository;


import in.truethics.ethics.ethicsapiv10.model.master.ContentMaster;
import in.truethics.ethics.ethicsapiv10.model.master.ContentPackageMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ContentPackageMasterRepository extends JpaRepository<ContentPackageMaster, Long> {
    List<ContentPackageMaster> findByStatus(boolean b);
}
