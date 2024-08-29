package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.City;
import in.truethics.ethics.ethicsapiv10.model.master.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findAllByStateId(Long stateId);
    Optional<City> findById(Long cityId);
}
