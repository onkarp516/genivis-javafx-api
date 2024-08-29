package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.AreaMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaMasterRepository extends JpaRepository<AreaMaster, Long> {
    List<AreaMaster> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    List<AreaMaster> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    AreaMaster findByIdAndStatus(long id, boolean b);

    AreaMaster findByOutletIdAndAreaNameIgnoreCaseAndStatusAndBranchId(Long outletId, String areaName, boolean b, Long id);

    AreaMaster findByOutletIdAndAreaNameIgnoreCaseAndStatusAndBranchIsNull(Long outletId, String areaName, boolean b);

    AreaMaster findByOutletIdAndBranchIdAndAreaNameAndStatus(Long id, Long id1, String areaName, boolean b);

    AreaMaster findByOutletIdAndAreaNameAndStatusAndBranchIsNull(Long id, String areaName, boolean b);

    List<AreaMaster> findByPincodeAndStatus(String pincode, boolean b);
}
