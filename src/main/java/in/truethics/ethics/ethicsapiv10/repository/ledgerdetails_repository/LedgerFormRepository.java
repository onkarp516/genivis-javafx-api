package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerFormParameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerFormRepository extends JpaRepository<LedgerFormParameter,Long> {
    LedgerFormParameter findByFormName(String others);
}
