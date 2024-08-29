package in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.AreaheadCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AreaheadCommissionRepository extends JpaRepository<AreaheadCommission, Long> {
    @Query(value = "SELECT IFNULL(sum(commission_amount),0) as commission from areahead_commission_tbl where areahead_id=?3 AND (invoice_date between ?1 AND ?2)", nativeQuery = true)
    double getTotalCommission(String fromDate, String toDate, String id);

    @Query(value = "SELECT IFNULL(sum(commission_amount),0) as commission from areahead_commission_tbl where areahead_id=?3 AND franchise_code=?4 AND (invoice_date between ?1 AND ?2)", nativeQuery = true)
    double getTotalCommission(String fromDate, String toDate, String id, String franchiseCode);

    @Query(value = "SELECT IFNULL(sum(sales_invoice_amount),0) as commission from areahead_commission_tbl where areahead_id=?3 AND (invoice_date between ?1 AND ?2)", nativeQuery = true)
    double getPurchaseAmount(String fromDate, String toDate, String areaHeadId);

    @Query(value = "SELECT IFNULL(sum(sales_invoice_amount),0) as commission from areahead_commission_tbl where areahead_id=?3 AND franchise_code=?4 AND (invoice_date between ?1 AND ?2)", nativeQuery = true)
    double getPurchaseAmount(String fromDate, String toDate, String areaHeadId, String franchiseCode);


    AreaheadCommission findTop1ByFranchiseCodeOrderByIdDesc(String franchiseCode);

    @Query(value = "SELECT franchise_code, SUM(sales_invoice_amount) as sales_invoice_amount FROM areahead_commission_tbl where areahead_id=?1 and (invoice_date between ?2 and ?3) group by franchise_code order by SUM(sales_invoice_amount) ?4 LIMIT 5", nativeQuery = true)
    List<String> getPerformers(String areaheadId, String startDate, String endDate, String sortType);

    List<AreaheadCommission> findBySalesInvoiceNumberAndStatus(String salesInvoiceNo, boolean b);

    @Query(value = "SELECT *  FROM areahead_commission_tbl where invoice_date between ?1 AND ?2 AND status=?3",
            nativeQuery = true)
    List<AreaheadCommission> findList(LocalDate startDate, LocalDate endDate, Boolean status);
}