package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Foundations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoundationRepository extends JpaRepository<Foundations,Long> {

    Foundations findByIdAndStatus(Long foundationId, boolean b);
}
