package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Subgroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubgroupRepository extends JpaRepository<Subgroup, Long> {

    List<Subgroup> findByOutletIdAndStatus(Long outletId, boolean b);

    Subgroup findByIdAndStatus(long id, boolean b);


    List<Subgroup> findByOutletIdAndStatusAndBranchId(Long outletId, boolean b, Long id);

    List<Subgroup> findByStatus(boolean b);

    Subgroup findByOutletIdAndBranchIdAndIdAndStatus(Long id, Long id1, Long object, boolean b);

    Subgroup findByOutletIdAndBranchIsNullAndIdAndStatus(Long id, Long object, boolean b);

    Subgroup findByOutletIdAndBranchIdAndStatusAndSubgroupNameIgnoreCase(Long id, Long id1, boolean b, String subgroupName);

    Subgroup findByOutletIdAndStatusAndSubgroupNameIgnoreCaseAndBranchIsNull(Long id, boolean b, String subgroupName);

    Subgroup findByOutletIdAndBranchIdAndStatusAndSubgroupNameIgnoreCaseAndIdNot(Long id, Long id1, boolean b, String subgroupName, Long id2);

    Subgroup findByOutletIdAndStatusAndSubgroupNameIgnoreCaseAndIdNotAndBranchIsNull(Long id, boolean b, String subgroupName, Long id1);
    Subgroup findFirstBySubgroupNameIgnoreCase(String subgroupName);
}
