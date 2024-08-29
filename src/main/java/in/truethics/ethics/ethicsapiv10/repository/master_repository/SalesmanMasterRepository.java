package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.SalesManMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SalesmanMasterRepository extends JpaRepository<SalesManMaster, Long> {
    SalesManMaster findByIdAndStatus(long id, boolean b);

    List<SalesManMaster> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    List<SalesManMaster> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    SalesManMaster getByIdAndStatus(String salesmanId, boolean b);

    @Query(
            value = "SELECT * FROM salesman_master_tbl WHERE outlet_id = ?1 AND branch_id = ?2 AND first_name = ?3 AND " +
                    "middle_name = ?4 AND last_name = ?5 AND status = ?6 AND id != ?7",
            nativeQuery = true
    )
    SalesManMaster findDuplicateWithBranch(Long outletId, Long branchId, String firstName, String middleName, String lastName, boolean status,Long id);


    @Query(
            value = "SELECT * FROM salesman_master_tbl WHERE outlet_id = ?1 AND first_name = ?2 AND " +
                    "middle_name = ?3 AND last_name = ?4 AND status = ?5 AND id != ?6 AND branch_id IS NULL",
            nativeQuery = true
    )
    SalesManMaster findDuplicate(Long outletId, String firstName, String middleName, String lastName, boolean status, Long id);

}

