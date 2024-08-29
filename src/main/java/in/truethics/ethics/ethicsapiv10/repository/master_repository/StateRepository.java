package in.truethics.ethics.ethicsapiv10.repository.master_repository;


import in.truethics.ethics.ethicsapiv10.model.master.City;
import in.truethics.ethics.ethicsapiv10.model.master.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findByCountryCode(String in);

    List<State> findByStateCode(String stateCode);

    State findByName(String district);
    @Query(
            value = "SELECT * FROM `state_tbl` WHERE id=?1", nativeQuery = true
    )
    State stateIdfind(Long stateId);

    Optional<State> findById(Long stateId);
    @Query(
            value = "SELECT * FROM `state_tbl` WHERE id=?1", nativeQuery = true
    )
    List<State> findByStateId(Long stateId);

    @Query(
            value = "SELECT * FROM `state_tbl` WHERE state_code=?1", nativeQuery = true
    )
    State stateCodefind(Long stateId);
}
