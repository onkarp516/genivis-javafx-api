package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Principles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrincipleRepository extends JpaRepository<Principles,Long> {
    Principles findByPrincipleNameIgnoreCaseAndStatus(String key, boolean b);
    Principles findByIdAndStatus(Long ledgeIdpc, boolean b);

}
