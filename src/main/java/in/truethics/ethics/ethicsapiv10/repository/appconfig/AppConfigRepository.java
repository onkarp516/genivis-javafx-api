package in.truethics.ethics.ethicsapiv10.repository.appconfig;

import in.truethics.ethics.ethicsapiv10.model.appconfig.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    AppConfig findByIdAndStatus(long id, boolean b);

    List<AppConfig> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    List<AppConfig> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);
}
