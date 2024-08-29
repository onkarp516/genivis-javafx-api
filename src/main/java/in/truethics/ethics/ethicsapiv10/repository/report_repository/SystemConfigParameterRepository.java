package in.truethics.ethics.ethicsapiv10.repository.report_repository;

import in.truethics.ethics.ethicsapiv10.model.appconfig.SystemConfigParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemConfigParameterRepository extends JpaRepository<SystemConfigParameter,Long> {
    List<SystemConfigParameter> findByStatus(boolean b);

    SystemConfigParameter findByIdAndStatus(Long id, boolean b);
}
