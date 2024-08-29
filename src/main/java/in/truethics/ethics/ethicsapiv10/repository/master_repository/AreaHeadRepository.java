package in.truethics.ethics.ethicsapiv10.repository.master_repository;


import in.truethics.ethics.ethicsapiv10.model.master.AreaHead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
public interface AreaHeadRepository extends JpaRepository<AreaHead, Long> {

    List<AreaHead> findByStatus(boolean b);

    AreaHead findByUsernameIgnoreCaseAndStatus(String userName, boolean b);

    AreaHead findByIdAndStatus(long id, boolean b);


    //get State heads
    AreaHead findByAreaRoleAndStateIdAndStatus(String role, Long stateId, boolean status);
    List<AreaHead> findByAreaRoleAndStateId(String role, Long stateId);
    //
//get zonal head
    AreaHead findByAreaRoleAndZoneIdAndStatus(String role, Long zoneId, boolean status);
    List<AreaHead> findByAreaRoleAndZoneId(String role, Long zoneId);
    //get reginal head
    AreaHead findByAreaRoleAndRegionIdAndStatus(String role, Long regionId, boolean status);
    List<AreaHead> findByAreaRoleAndRegionId(String role, Long regionId);

    AreaHead findByAreaRoleAndDistrictIdAndStatus(String role, Long districtId, boolean status);

    List<AreaHead> findByStatusAndAreaRole(boolean b, String district);
    AreaHead findByUsernameAndAreaRoleAndStatus(String username, String areaRole, boolean b);

    int countByAreaRoleAndZoneIdAndStatus(String region, Long zoneId, boolean b);

    @Query(value = "SELECT * FROM `area_head_tbl` WHERE area_role=?1 AND state_id=?2 AND status=?3", nativeQuery = true)
    List<AreaHead> findByAreaRoleAndStateIdAndStatusNative(String zonal, Long id, boolean b);

    int countByAreaRoleAndStateIdAndStatus(String zonal, Long id, boolean b);
    @Query(value = "SELECT * FROM `area_head_tbl` WHERE area_role=?1 AND zone_id=?2 AND status=?3", nativeQuery = true)
    List<AreaHead> findByAreaRoleAndZoneIdAndStatusNative(String region, Long id, boolean b);

    int countByAreaRoleAndRegionIdAndStatus(String district, Long id, boolean b);
    int countByAreaRoleAndDistrictIdAndStatus(String district, Long id, boolean b);


    int countByStateIdAndStatus(Long id, boolean b);

    int countByZoneIdAndStatus(Long id, boolean b);


    int countByDistrictIdAndStatus(Long id, boolean b);

    int countByRegionIdAndStatus(Long id, boolean b);
}
