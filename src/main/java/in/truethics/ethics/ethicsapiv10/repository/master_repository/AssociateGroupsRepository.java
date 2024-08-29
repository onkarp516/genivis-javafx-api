package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.AssociateGroups;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociateGroupsRepository extends JpaRepository<AssociateGroups, Long> {
    AssociateGroups findByIdAndStatus(long associates_id, boolean b);

    @Query(
            value = " SELECT associates_name FROM `associates_groups_tbl` WHERE id=?1 AND Status=?2",
            nativeQuery = true
    )
    String findName(Long associateId, boolean b);

    List<AssociateGroups> findByOutletId(Long id);


    @Query(
            value = " SELECT * FROM associates_groups_tbl WHERE outlet_id=?1 AND (branch_id=?2 OR branch_id IS NULL) " +
                    "AND principle_id=?3 AND (principle_groups_id=?4 OR principle_groups_id IS NULL) " +
                    "AND associates_name=?5 AND status=?6", nativeQuery = true
    )
    AssociateGroups findDuplicateAG(Long outletId, Long branchId, Long principleId, Long pgroupId,
                                    String associates_name, Boolean status);

    List<AssociateGroups> findByOutletIdAndStatusAndBranchIdOrderByIdDesc(Long id, boolean b, Long id1);

    List<AssociateGroups> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);
    List<AssociateGroups> findByOutletIdAndStatus(Long id, boolean b);

    @Query(
            value = " SELECT * FROM associates_groups_tbl WHERE outlet_id=?1 AND (branch_id=?2 OR branch_id IS NULL)" +
                    "AND principle_id=?3 AND principle_groups_id=?4 " +
                    "AND lower(associates_name=?5) AND status=?6", nativeQuery = true
    )
    AssociateGroups findDuplicateWithName(Long id, Long branchId, long principle_id, Long pgroupId, String ledger_group_name, boolean b);

    @Query(
            value = " SELECT * FROM associates_groups_tbl WHERE outlet_id=?1 AND (branch_id=?2 OR branch_id IS NULL)" +
                    "AND principle_id=?3 AND lower(associates_name=?4) AND status=?5", nativeQuery = true
    )
    AssociateGroups findDuplicate(Long id, Long branchId, long principle_id, String ledger_group_name, boolean b);

    AssociateGroups findByAssociatesNameIgnoreCaseAndStatus(String partner_commission, boolean b);

    @Query(value = "SELECT * FROM associates_groups_tbl ag " +
            "LEFT JOIN foundations_tbl ft ON ag.foundation_id = ft.id " +
            "LEFT JOIN principles_tbl pt ON ag.principle_id = pt.id " +
            "LEFT JOIN principle_groups_tbl pgt ON ag.principle_groups_id = pgt.id " +
            "WHERE ft.foundation_name LIKE %:criteria% " +
            "OR pt.principle_name LIKE %:criteria% " +
            "OR pgt.group_name LIKE %:criteria% " +
            "OR ag.associates_name LIKE %:criteria% " +
            "GROUP BY ft.foundation_name, pt.principle_name, pgt.group_name, ag.associates_name " +
            "ORDER BY ag.id DESC", nativeQuery = true)
    List<AssociateGroups> findByCriteria(String criteria);
}
