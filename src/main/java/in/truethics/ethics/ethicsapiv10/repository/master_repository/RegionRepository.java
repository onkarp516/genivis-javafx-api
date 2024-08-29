package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.City;
import in.truethics.ethics.ethicsapiv10.model.master.Region;
import in.truethics.ethics.ethicsapiv10.model.master.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByStatus(boolean b);

    Region findByIdAndStatus(long regionCode, boolean b);

    List<Region> findByStateIdAndZoneIdAndStatus(Long stateId, Long zoneId, boolean b);
}
