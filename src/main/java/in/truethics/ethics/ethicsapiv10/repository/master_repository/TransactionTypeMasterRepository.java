package in.truethics.ethics.ethicsapiv10.repository.master_repository;

import in.truethics.ethics.ethicsapiv10.model.master.TransactionTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionTypeMasterRepository extends JpaRepository<TransactionTypeMaster, Long> {

    TransactionTypeMaster findByTransactionNameIgnoreCase(String tranxType);

    TransactionTypeMaster findByTransactionCodeIgnoreCase(String purchase_return);
}

