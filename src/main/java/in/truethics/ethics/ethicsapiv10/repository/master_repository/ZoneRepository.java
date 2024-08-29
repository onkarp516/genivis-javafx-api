package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.AreaHead;
import in.truethics.ethics.ethicsapiv10.model.master.City;
import in.truethics.ethics.ethicsapiv10.model.master.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    List<Zone> findByStatus(boolean b);


    Zone findByIdAndStatus(long zoneCode, boolean b);

    List<Zone> findByStateIdAndStatus(Long stateId, boolean b);
}
