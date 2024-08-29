package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.CommissionMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommissionMasterRepository extends JpaRepository<CommissionMaster, Long> {
    List<CommissionMaster> findByStatus(boolean b);

    CommissionMaster findByIdAndStatus(long id, boolean b);

    CommissionMaster findByRoleTypeIgnoreCaseAndStatus(String roleType, boolean b);

    Double findByRoleTypeAndStatus(String state, boolean b);
    CommissionMaster findByRoleTypeIgnoreCase(String areaRole);
}