package in.truethics.ethics.ethicsapiv10.repository.report_repository;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceSheet extends JpaRepository<FiscalYear,Long> {
}
