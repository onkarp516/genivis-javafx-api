package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.FranchiseMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FranchiseMasterRepository  extends JpaRepository<FranchiseMaster,Long> {
    FranchiseMaster findByFranchiseNameIgnoreCaseAndStatus(String franchiseName, boolean b);

    FranchiseMaster findByIdAndStatus(long id, boolean b);


    List<FranchiseMaster> findByStatus(boolean b);

    List<FranchiseMaster> findByStateIdAndStatus(Long id, boolean b);

    List<FranchiseMaster> findByZoneIdAndStatus(Long id, boolean b);

    List<FranchiseMaster> findByRegionalIdAndStatus(Long id, boolean b);

    List<FranchiseMaster> findByDistrictIdAndStatus(Long id, boolean b);

    int countByStateIdAndZoneIdAndStatus(Long stateId, Long zoneId, boolean b);

    int countByStateIdAndZoneIdAndRegionalIdAndStatus(Long stateId, Long zoneId, Long regionId, boolean b);

    int countByStateIdAndZoneIdAndRegionalIdAndDistrictIdAndStatus(Long stateId, Long zoneId, Long regionId, Long districtId, boolean b);

    int countByStateIdAndStatus(Long stateHeadId, boolean b);

    int countByZoneIdAndStatus(Long zoneHeadId, boolean b);

    int countByRegionalIdAndStatus(Long regionalHeadId, boolean b);

    int countByDistrictIdAndStatus(Long districtHeadId, boolean b);

    FranchiseMaster findByFranchiseCodeAndStatus(String franchiseCode, boolean b);

    FranchiseMaster findByFranchiseCodeIgnoreCaseAndStatus(String franchiseCode, boolean b);


}
