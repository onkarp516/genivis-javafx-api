package in.truethics.ethics.ethicsapiv10.repository.user_repository;

import in.truethics.ethics.ethicsapiv10.model.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UsersRepository extends JpaRepository<Users, Long> {


    Users findByUsercodeIgnoreCaseAndStatus(String userCode, boolean b);

    List<Users> findByOutletIdAndStatus(Long outletId, boolean b);

    List<Users> findByUserRoleAndStatus(String userRole, boolean b);

    Users findByIdAndStatus(long id, boolean b);

    Users findByUsername(String usercode);

    List<Users> findByUserRoleAndCreatedByAndStatus(String userRole, Long id, boolean b);

    List<Users> findByUserRoleIgnoreCaseAndStatus(String sadmin, boolean b);

    List<Users> findByUserRoleIgnoreCaseAndStatusAndOutletId(String cadmin, boolean b, Long id);

    @Query(value = "SELECT * FROM `users_tbl` WHERE user_role=?1 AND status=?2", nativeQuery = true)
    List<Users> findUsers(String user,boolean b);

    @Query(value = "SELECT * FROM `users_tbl` WHERE status=?1", nativeQuery = true)
    List<Users> findStatus(boolean b);

  /*  @Query(value = "SELECT * FROM `users_tbl` WHERE status=?1 group by company_id", nativeQuery = true)
    List<Users> findCompanyData(boolean b);*/

    @Query(value = "SELECT * FROM `users_tbl` WHERE status=?1 AND company_id=?2", nativeQuery = true)
    List<Users> findCompanyData(boolean b,Long id);


    /* @Query(value = "SELECT * FROM `users_tbl` WHERE status=?1", nativeQuery = true)
     List<Users> findStatus(boolean b);
 */
    Users findByIdAndOutletIdAndStatus(long userIds, Long id, boolean b);

    Users findByUsernameAndUserRole(String username, String owner);

    Users findByUsernameAndStatus(String username, Boolean status);

    Users findByOutletIdAndBranchIdAndStatusAndUsercode(Long outletId, Long branchId, boolean b, String userCode);

    List<Users> findByUserRoleIgnoreCase(String cadmin);

    Users findByOutletIdAndBranchIdAndUsernameAndStatus(Long id, Long id1, String userName, boolean b);

    Users findByOutletIdAndUsernameAndStatusAndBranchIsNull(Long id, String userName, boolean b);

    Users findByUsernameAndCompanyCode(String username, String companyCode);

    Users findTop1ByUserRoleIgnoreCaseAndCompanyCode(String cadmin, String gvmh001);

    List<Users> findByUserRoleIgnoreCaseAndOutletId(String user, Long id);
}
