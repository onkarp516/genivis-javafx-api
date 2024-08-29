package in.truethics.ethics.ethicsapiv10.repository.master_repository;


import in.truethics.ethics.ethicsapiv10.model.master.FlavourMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlavourMasterRepository extends JpaRepository<FlavourMaster,Long> {

    FlavourMaster findByIdAndStatus(long id, boolean b);

    List<FlavourMaster> findByOutletIdAndBranchIdAndStatus(Long id, Long id1, boolean b);

    List<FlavourMaster> findByOutletIdAndStatus(Long id, boolean b);
}
