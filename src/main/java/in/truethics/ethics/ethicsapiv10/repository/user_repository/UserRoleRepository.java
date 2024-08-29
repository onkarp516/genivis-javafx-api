package in.truethics.ethics.ethicsapiv10.repository.user_repository;

import in.truethics.ethics.ethicsapiv10.model.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole,Long> {
    List<UserRole> findByStatus(boolean b);

    UserRole findByIdAndStatus(long roleId, boolean b);



    UserRole findByRoleNameIgnoreCaseAndStatus(String roleName, boolean b);
}
