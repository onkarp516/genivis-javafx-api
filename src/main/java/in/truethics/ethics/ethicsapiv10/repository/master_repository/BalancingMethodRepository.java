package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.BalancingMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalancingMethodRepository extends JpaRepository<BalancingMethod,Long> {
    BalancingMethod findByIdAndStatus(long balancing_method, boolean b);
}
