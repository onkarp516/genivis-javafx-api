package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface LedgerOpeningClosingDetailRepository extends JpaRepository<LedgerOpeningClosingDetail, Long> {
    @Query(value = "SELECT * FROM ledger_opening_closing_detail_tbl WHERE ledger_id=?1 AND tranx_date<=?2 AND" +
            " status=?3 ORDER BY tranx_date DESC, id DESC LIMIT 1", nativeQuery = true) //tranx_type_id=?3 AND
    LedgerOpeningClosingDetail getLastRowByLedgerIdAndTranxDateAndTranxTypeIdAndStatus(Long id, Date invoiceDate, boolean b);

    @Query(value = "SELECT ledger_id FROM ledger_opening_closing_detail_tbl WHERE tranx_id=?1 AND tranx_type_id=?2" +
            " AND status=?3", nativeQuery = true)
    List<Long> getLedgersByTranxIdAndTranxTypeIdAndStatus(Long id, Long id1, boolean b);
    LedgerOpeningClosingDetail findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(Long id, Long id1, Long id2, boolean b);

    @Query(value = "SELECT * FROM `ledger_opening_closing_detail_tbl` WHERE ledger_id=?1 AND tranx_date>?2 AND id!=?3" +
            " AND status=?4 ORDER BY `tranx_date` ASC", nativeQuery = true)
    List<LedgerOpeningClosingDetail> getNextRowsByLedgerIdAndTranxDateAndStatusForUpdate(Long id, Date invoiceDate, Long id1, boolean b);

    @Query(value = "SELECT * FROM `ledger_opening_closing_detail_tbl` WHERE ledger_id=?1 AND tranx_date BETWEEN ?2 AND ?3" +
            " AND id!=?4 AND status=?5 ORDER BY `tranx_date` ASC", nativeQuery = true)
    List<LedgerOpeningClosingDetail> getBetweenDatesByLedgerId(Long id, Date oldDate, Date newDate, Long ledgerDetailId, boolean b);

    @Query(value = "SELECT IFNULL(closing_amount,0) FROM ledger_opening_closing_detail_tbl WHERE ledger_id=?1 AND" +
            " DATE(tranx_date)=?2 AND status=?3 ORDER BY tranx_date DESC LIMIT 1", nativeQuery = true)
    Double getClosingAmountOfLedgerIdAndStatus(Long id, LocalDate invDt, boolean b);
    @Query(value = "SELECT IFNULL(opening_amount,0) FROM ledger_opening_closing_detail_tbl WHERE ledger_id=?1 AND" +
            " DATE(tranx_date)=?2 AND status=?3 ORDER BY tranx_date ASC LIMIT 1", nativeQuery = true)
    Double getOpeningAmountOfLedgerIdAndStatus(Long id, LocalDate invDt, boolean b);

//    @Query(value = "SELECT IFNULL(opening_amount,0) FROM ledger_opening_closing_detail_tbl WHERE ledger_id=?1 AND" +
//            " DATE(tranx_date)=?2 AND status=?3 ORDER BY tranx_date ASC LIMIT 1", nativeQuery = true)
//    Double getOpeningAmountOfLedgerId(Long id, String month, boolean b);

    List<LedgerOpeningClosingDetail> findByTranxTypeIdAndTranxIdAndStatus(Long id, Long id1, boolean b);

    @Query(
            value = "SELECT * FROM ledger_opening_closing_detail_tbl WHERE ledger_id=?1 AND DATE(tranx_date) BETWEEN ?2 AND ?3",nativeQuery = true
    )
    List<LedgerOpeningClosingDetail> findLedgerByIdAndDate(long id, LocalDate sDate, LocalDate eDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0),ledger_id,closing_amount, tranx_action FROM `ledger_opening_closing_detail_tbl`" +
                    " WHERE outlet_id=?1 AND branch_id=?2 status=?3 AND " +
                    "ledger_id=?4 AND tranx_date BETWEEN ?5 ANd ?6", nativeQuery = true
    )

    List<Object[]> findByTotalAmountByMonthStartDateAndEndDateAndBranchAndOutletAndStatus31(Long id, Long id1, boolean b, Long ledgerMasterId, LocalDate startMonthDate, LocalDate endMonthDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0),ledger_id,closing_amount,tranx_action FROM `ledger_opening_closing_detail_tbl`" +
                    " WHERE outlet_id=?1 AND  status=?2 AND " +
                    "ledger_id=?3 AND tranx_date BETWEEN ?4 AND ?5", nativeQuery = true
    )
    List<Object[]> findByTotalAmountByMonthStartDateAndEndDateAndOutletAndStatus31(Long id, boolean b, Long ledger_master_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT closing_amount FROM ledger_opening_closing_detail_tbl WHERE ledger_id=?1 and  month(tranx_date)=?2 order by tranx_date DESC limit 1", nativeQuery = true
    )
    Double findsumCR1(Long id, int month);

    @Query(
            value = "SELECT IFNULL(closing_amount, 0.0) FROM ledger_opening_closing_detail_tbl WHERE ledger_id=?1 AND date(tranx_date)<?2 order by tranx_date desc limit 1;", nativeQuery = true
    )
    Double findLedgerOpeningAmt(Long id, LocalDate startDate);



            @Query(value = "SELECT IFNULL(opening_amount,0) FROM ledger_opening_closing_detail_tbl WHERE ledger_id=?1 AND" +
            " DATE(tranx_date)=?2 AND status=?3 ORDER BY tranx_date ASC LIMIT 1", nativeQuery = true)
            List<String>  getClosingAmountOfLedgerIdAndStatus1(Long id, LocalDate invDt, boolean b);
//    List<String> findOpeningByIdAndStatus(Long Ledger_id, boolean b);

}
