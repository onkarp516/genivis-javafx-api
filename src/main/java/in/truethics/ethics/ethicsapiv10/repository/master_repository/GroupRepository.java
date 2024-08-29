package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Group findByIdAndStatus(long id, boolean b);
    Group findFirstByGroupNameIgnoreCase(String groupName);


    List<Group> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    Group findByOutletIdAndBranchIdAndStatusAndGroupNameIgnoreCase(Long id, Long id1, boolean b, String groupName);

    List<Group> findByOutletIdAndStatusAndBranchIsNull(Long outletId, boolean b);

    Group findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    Group findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    Group findByOutletIdAndStatusAndGroupNameIgnoreCaseAndBranchIsNull(Long id, boolean b, String groupName);

    Group findByOutletIdAndBranchIdAndStatusAndGroupNameIgnoreCaseAndIdNot(Long id, Long id1, boolean b, String groupName, Long id2);

    Group findByOutletIdAndStatusAndGroupNameIgnoreCaseAndIdNotAndBranchIsNull(Long id, boolean b, String groupName, Long id1);
}
