package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.City;
import in.truethics.ethics.ethicsapiv10.model.master.District;
import in.truethics.ethics.ethicsapiv10.model.master.Region;
import in.truethics.ethics.ethicsapiv10.model.master.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District, Long> {


    List<District> findByStatus(boolean b);

    District findByIdAndStatus(long districtCode, boolean b);

    List<District> findByStateIdAndZoneIdAndRegionIdAndStatus(Long stateId, Long zoneId, Long regionId, boolean b);
}
