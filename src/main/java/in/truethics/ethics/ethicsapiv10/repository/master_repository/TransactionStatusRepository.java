package in.truethics.ethics.ethicsapiv10.repository.master_repository;


import in.truethics.ethics.ethicsapiv10.model.master.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionStatusRepository extends JpaRepository<TransactionStatus,Long> {
    List<TransactionStatus> findAllByStatus(boolean b);

    TransactionStatus findByStatusNameAndStatus(String opened,boolean status);

    TransactionStatus findByIdAndStatus(long l, boolean b);
}
