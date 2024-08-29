package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.AreaMaster;
import in.truethics.ethics.ethicsapiv10.model.master.ContentMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ContentMasterRepository extends JpaRepository<ContentMaster, Long> {

    ContentMaster findByIdAndStatus(long id, boolean b);


    List<ContentMaster> findByStatus(boolean b);

    ContentMaster findByContentNameAndStatus(String rawValue, boolean b);
}
