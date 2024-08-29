package in.truethics.ethics.ethicsapiv10.repository.master_repository;


import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import org.apache.tomcat.jni.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long> {
    // 0 for default and 1 for User defined financial year and month
    @Query(
            value = " SELECT * FROM fiscal_year_tbl WHERE ?1 BETWEEN ?2 AND ?3 ORDER BY id DESC limit 1", nativeQuery = true
    )
    FiscalYear findFiscalYear(LocalDate curDate, LocalDate startDate, LocalDate endDate);


    @Query(
            value = " SELECT YEAR(date_start) FROM fiscal_year_tbl ", nativeQuery = true
    )
    String getStartYear();

    @Query(
            value = " SELECT YEAR(date_end) FROM fiscal_year_tbl ", nativeQuery = true
    )
    String getLastYear();
    @Query(
            value = " SELECT fiscal_year_tbl.date_start,fiscal_year_tbl.date_end FROM fiscal_year_tbl WHERE YEAR(date_start)=?1", nativeQuery = true
    )
    List<FiscalYear> StartAndEndDateofFiscalYear(String startDate);

    @Query(
            value = " select date_start,date_end from fiscal_year_tbl where YEAR(fiscal_year_tbl.date_start) <= YEAR(CURDATE()) Order by id desc limit 1", nativeQuery = true
    )
    List<Object[]> findByStartDateAndEndDateOutletIdAndBranchIdAndStatus();

    @Query(
            value = " select date_start,date_end from fiscal_year_tbl where YEAR(fiscal_year_tbl.date_start) <= YEAR(CURDATE()) Order by id desc limit 1", nativeQuery = true
    )
    List<Object[]> findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();

    FiscalYear findTopByOrderByIdDesc();
}
