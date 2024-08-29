package in.truethics.ethics.ethicsapiv10.repository.access_permission_repository;

import in.truethics.ethics.ethicsapiv10.model.access_permissions.SystemAccessPermissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemAccessPermissionsRepository extends JpaRepository<SystemAccessPermissions,Long> {

    List<SystemAccessPermissions> findByUsersIdAndStatus(Long id, boolean b);

    SystemAccessPermissions findByUsersIdAndStatusAndSystemActionMappingId(Long id, boolean b, Long id1);
}
