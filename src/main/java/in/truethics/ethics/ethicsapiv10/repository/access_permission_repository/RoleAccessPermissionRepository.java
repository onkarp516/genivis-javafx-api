package in.truethics.ethics.ethicsapiv10.repository.access_permission_repository;

import in.truethics.ethics.ethicsapiv10.model.access_permissions.RoleAccessPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleAccessPermissionRepository extends JpaRepository<RoleAccessPermission,Long> {
    List<RoleAccessPermission> findByUserRoleIdAndStatus(Long roleId, boolean b);


    RoleAccessPermission findByUserRoleIdAndStatusAndSystemActionMappingId(Long id, boolean b, Long id1);
}
