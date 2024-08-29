package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findAllByStatus(boolean b);

    List<Branch> findByCreatedByAndStatus(Long userId, boolean b);

    Branch findByIdAndStatus(long id, boolean b);

    List<Branch> findByOutletIdAndStatus(Long id, boolean b);

    Branch findByOutletIdAndBranchNameIgnoreCaseAndStatus(Long outletId, String branchName, boolean b);

    @Procedure("create_counter_customer_ledger_branch")
    void createCounterCustomerBranch(Long outletId, Long branchId, Long createdBy);

}
