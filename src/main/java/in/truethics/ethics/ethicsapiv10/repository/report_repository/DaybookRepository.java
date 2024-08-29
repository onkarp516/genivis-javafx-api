package in.truethics.ethics.ethicsapiv10.repository.report_repository;

import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DaybookRepository extends JpaRepository<DayBook,Long> {

    @Query(
            value = "SELECT * FROM `day_book_tbl` WHERE tranx_date BETWEEN ?1 AND ?2 " +
                    "AND status=?3 AND outlet_id=?4 AND branch_id=?5",nativeQuery = true
    )
    List<DayBook> findByTranxDateAndStatusAndOutletIdAndBranchId(LocalDate startDate,LocalDate endDate, boolean b, Long id, Long id1);

    @Query(
            value = "SELECT * FROM `day_book_tbl` WHERE tranx_date BETWEEN ?1 AND ?2 " +
                    "AND status=?3 AND outlet_id=?4",nativeQuery = true
    )
    List<DayBook>   findByTranxDateAndStatusAndOutletId(LocalDate startDate,LocalDate endDate, boolean b, Long id);
}
