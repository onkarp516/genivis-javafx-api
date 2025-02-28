package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LedgerTransactionPostingsRepository extends JpaRepository<LedgerTransactionPostings, Long> {

    LedgerTransactionPostings findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(Long id, Long id1, Long id2);

    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND status=?3 AND " +
                    "ledger_master_id=?4 AND DATE(transaction_date) BETWEEN ?5 AND ?6 ORDER BY transaction_date ASC", nativeQuery = true
    )
    List<LedgerTransactionPostings> findByDetailsBetweenDates(Long id, Long id1, boolean b, Long ledger_master_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND branch_id IS NULL AND status=?2 AND " +
                    "ledger_master_id=?3 AND DATE(transaction_date) BETWEEN ?4 AND ?5 ORDER BY transaction_date ASC", nativeQuery = true
    )
    List<LedgerTransactionPostings> findByDetails(Long id, boolean b, Long ledger_master_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND status=?3 AND " +
                    "ledger_master_id=?4 ORDER BY transaction_date ASC", nativeQuery = true
    )
    List<LedgerTransactionPostings> findByDetailsBranch(Long id, Long id1, boolean b, Long ledger_master_id);

    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND status=?2 AND " +
                    "ledger_master_id=?3 ORDER BY transaction_date ASC", nativeQuery = true
    )
    List<LedgerTransactionPostings> findByDetailsFisc(Long id, boolean b, Long ledger_master_id);

    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE status=?1 AND " +
                    "ledger_master_id=?2 ORDER BY transaction_date ASC", nativeQuery = true
    )
    List<LedgerTransactionPostings> findByMobileDetailsFisc(boolean b, Long ledger_master_id);

    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE status=?1 AND " +
                    "ledger_master_id=?2 ORDER BY transaction_date ASC", nativeQuery = true
    )
    List<LedgerTransactionPostings> findmobileDetailsFisc(boolean b, Long ledger_master_id);

    @Query(
            value =
                    "SELECT * FROM `ledger_transaction_postings_tbl` WHERE transaction_type_id in(1,2,3,4,11,12,13,14,15,16) and status=?1", nativeQuery = true)
    List<LedgerTransactionPostings> findByTransactionTypeIdAndStatus(boolean b);

    ///Balance Sheeet Query startted///
    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` LEFT JOIN " +
                    "ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE " +
                    "ledger_transaction_postings_tbl.outlet_id=1" + "AND ledger_transaction_details_tbl.branch_id=?2 AND ledger_transaction_postings_tbl.status=?3 AND" +
                    " ledger_master_tbl.principle_id=?4 AND ledger_transaction_postings_tbl.transaction_date BETWEEN  ?5 AND ?6 ORDER BY ledger_transaction_postings_tbl.transaction_date ASC",
            nativeQuery = true
    )
    Double findByDateWiseTotalBalanceAmountOuletAndBranchStatus(Long id, Long id1, boolean b, Long a, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " +
                    " ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE " +
                    "ledger_transaction_postings_tbl.outlet_id=?1" + " AND ledger_transaction_postings_tbl.status=?2 AND ledger_master_tbl.principle_id=?3" +
                    " AND ledger_transaction_postings_tbl.transaction_date BETWEEN  ?4 AND ?5 ORDER BY ledger_transaction_postings_tbl.transaction_date ASC",
            nativeQuery = true
    )
    Double findByDateWiseTotalBalanceAmountOuletAndStatus(Long id, boolean b, Long a, LocalDate startDatep, LocalDate endDatep);

    @Query(
            value = "SELECT SUM(amount),ledger_master_id ,ledger_type  FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON "
                    + " ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE" + " ledger_transaction_postings_tbl.outlet_id=?1 AND"
                    + " ledger_transaction_postings_tbl.branch_id=?2 " +
                    " AND ledger_transaction_postings_tbl.status=?3 AND principle_id=?4 AND transaction_date BETWEEN ?5 AND ?6 GROUP BY ledger_master_tbl.principle_groups_id", nativeQuery = true
    )
    List<Object[]> findByPrincipleGroupTotalAmountOuletAndBranchStatusStep1(Long id, Long id1, boolean b, Long principle_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0),ledger_master_id ,ledger_type FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " + " ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE" + " ledger_transaction_postings_tbl.outlet_id=?1 AND " +
                    "ledger_transaction_postings_tbl.status=?2 AND principle_id=?3 AND" +
                    " transaction_date BETWEEN ?4 AND ?5 GROUP BY ledger_master_tbl.principle_groups_id", nativeQuery = true
    )
    List<Object[]> findByPrincipleGroupTotalAmountOuletAndStatusStep1(Long id, boolean b, Long principle_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT SUM(amount),ledger_master_id,ledger_type  FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " + " ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE" + " ledger_transaction_postings_tbl.outlet_id=?1 AND" + " ledger_transaction_postings_tbl.branch_id=?2 " +
                    " AND ledger_transaction_postings_tbl.status=?3 AND principle_groups_id=?4 AND transaction_date BETWEEN ?5 AND ?6 GROUP BY ledger_master_tbl.id", nativeQuery = true
    )
    List<Object[]> findByLedgerNameTotalAmountOuletAndBranchStatusStep2(Long id, Long id1, boolean b, Long principle_groups_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0),ledger_master_id,ledger_type FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON "
                    + " ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE" + " ledger_transaction_postings_tbl.outlet_id=?1 AND " +
                    "ledger_transaction_postings_tbl.status=?2 AND principle_groups_id=?3 AND" +
                    " transaction_date BETWEEN ?4 AND ?5 GROUP BY ledger_master_tbl.id", nativeQuery = true
    )
    List<Object[]> findByLedgerNameTotalAmountOuletAndStatusStep2(Long id, boolean b, Long principle_groups_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0),ledger_master_id,ledger_type FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " +
                    "ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE " +
                    "ledger_transaction_postings_tbl.outlet_id=?1 AND ledger_transaction_postings_tbl.branch_id=?2 ledger_transaction_postings_tbl.status=?3 AND " +
                    "ledger_master_id=?4 AND ledger_transaction_postings_tbl.transaction_date BETWEEN ?5 ANd ?6", nativeQuery = true
    )
    List<Object[]> findByTotalAmountByMonthStartDateAndEndDateAndBranchAndOutletAndStatus3(Long id, Long id1, boolean b, Long ledger_master_id, LocalDate startDate, LocalDate endDate);


    @Query(
            value = "SELECT IFNULL(SUM(amount),0),ledger_master_id,ledger_type FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " +
                    "ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE ledger_transaction_postings_tbl.outlet_id=?1 AND " +
                    "ledger_transaction_postings_tbl.status=?2 AND ledger_transaction_postings_tbl.ledger_master_id=?3 AND DATE(transaction_date) BETWEEN ?4 ANd ?5", nativeQuery = true
    )
    List<Object[]> findByTotalAmountByMonthStartDateAndEndDateAndOutletAndStatus3(Long id, boolean b, Long ledger_master_id, LocalDate startDate, LocalDate endDate);


    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND (branch_id=?2 OR branch_id IS NULL) AND  status=?3 AND " +
                    "ledger_master_id=?4 AND DATE(transaction_date) BETWEEN ?5 ANd ?6 ;", nativeQuery = true
    )
    List<LedgerTransactionPostings> findByIdAndOutletIdAndBranchAndStatusBalanceStep4(Long id, Long id1, boolean b, Long ledger_master_id, LocalDate startDate, LocalDate endDate);

//    @Query(
//    value =  "SELECT * FROM 'ledger_opening_closing_detail_tbl' LEFT JOIN 'ledger_transaction_postings_tbl'ON "+
//            "ledger_opening_closing_detail_tbl.ledger_id = ledger_transaction_postings_tbl.ledger_master_id  WHERE outlet_id=?1"+
//            "AND (branch_id=?2 OR branch_id IS NULL) AND  status=?3 AND ledger_master_id=?4 AND DATE(transaction_date) BETWEEN ?5 ANd ?6",nativeQuery = true
//)
//    List<LedgerTransactionPostings> findByIdAndOutletIdAndBranchAndStatusBalanceStep5(Long id, Long id1, boolean b, Long ledger_master_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " + " ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE " +
                    "ledger_transaction_postings_tbl.outlet_id=?1" + " AND ledger_transaction_postings_tbl.status=?2 AND ledger_master_tbl.foundation_id=?3" + " AND ledger_transaction_postings_tbl.transaction_date BETWEEN  ?4 AND ?5 ORDER BY ledger_transaction_postings_tbl.transaction_date ASC",
            nativeQuery = true
    )
    Double findByDateWiseTotalBalanceSheetProfitLossAmountOuletAndStatus(Long id, boolean b, Long a, LocalDate startDatep, LocalDate endDatep);

    ///Profit Loss Query started////
    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0),IFNULL(ledger_transaction_postings_tbl.principle_id,0) FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " +
                    "ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE ledger_transaction_postings_tbl.outlet_id=?1 " +
                    "AND ledger_transaction_postings_tbl.branch_id=?2 AND ledger_transaction_postings_tbl.status=?3 AND ledger_master_tbl.principle_id=?4 AND " +
                    "ledger_transaction_postings_tbl.transaction_date BETWEEN  ?5 AND ?6 " +
                    "ORDER BY ledger_transaction_postings_tbl.transaction_date ASC",
            nativeQuery = true
    )
    Double findByDateWiseTotalAmountOuletAndBranchStatusPL(Long id, Long id1, boolean b, Long a, LocalDate startDatep, LocalDate endDatep);


    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " +
                    "ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE ledger_transaction_postings_tbl.outlet_id=?1 AND" +
                    " ledger_transaction_postings_tbl.status=?2 AND ledger_master_tbl.principle_id=?3 AND" +
                    " ledger_transaction_postings_tbl.transaction_date BETWEEN  ?4 AND ?5 ORDER BY ledger_transaction_postings_tbl.transaction_date ASC;",
            nativeQuery = true
    )
    Double findByDateWiseTotalAmountOuletAndStatusPL(Long id, boolean b, Long a, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT SUM(amount),principle_id  FROM `ledger_transaction_postings_tbl` WHERE ledger_transaction_postings_tbl.outlet_id=?1 AND ledger_transaction_postings_tbl.branch_id=?2 " +
                    " AND ledger_transaction_postings_tbl.status=?3 AND principle_id=?4 AND ledger_transaction_postings_tbl.transaction_date BETWEEN ?5 AND ?6 GROUP BY ledger_transaction_postings_tbl.ledger_master_id;", nativeQuery = true
    )
    List<Object[]> findByDateWiseTotalAmountOuletAndBranchStatusPLStep1(Long id, Long id1, boolean b, Long principle_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0),ledger_master_id FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON" + " ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE ledger_transaction_postings_tbl.outlet_id=?1 AND " +
                    "ledger_transaction_postings_tbl.status=?2 AND ledger_master_tbl.principle_id=?3 AND " +
                    "ledger_transaction_postings_tbl.transaction_date BETWEEN ?4 AND ?5 GROUP BY ledger_transaction_postings_tbl.ledger_master_id;", nativeQuery = true
    )
    List<Object[]> findByDateWiseTotalAmountOuletAndStatusPLStep1(Long id, boolean b, Long principle_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0),ledger_master_id,ledger_type FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " +
                    "ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE " +
                    "ledger_transaction_postings_tbl.outlet_id=?1 AND ledger_transaction_postings_tbl.branch_id=?2 ledger_transaction_postings_tbl.status=?3 AND " +
                    "ledger_master_id=?4 AND ledger_transaction_postings_tbl.transaction_date BETWEEN ?5 ANd ?6", nativeQuery = true
    )
    List<Object[]> findByTotalAmountByMonthStartDateAndEndDateAndBranchAndOutletAndStatus2PL(Long id, Long id1, boolean b, Long ledger_master_id, LocalDate startDate, LocalDate endDate);


    @Query(
            value = "SELECT IFNULL(SUM(amount),0),ledger_master_id,ledger_type FROM `ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl ON " +
                    "ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE ledger_transaction_postings_tbl.outlet_id=?1 AND " +
                    "ledger_transaction_postings_tbl.status=?2 AND ledger_transaction_postings_tbl.ledger_master_id=?3 AND DATE(transaction_date) BETWEEN ?4 ANd ?5", nativeQuery = true
    )
    List<Object[]> findByTotalAmountByMonthStartDateAndEndDateAndOutletAndStatus2PL(Long id, boolean b, Long ledger_master_id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) ,ledger_type FROM `ledger_transaction_postings_tbl` WHERE ledger_type ='CR' AND " +
                    "ledger_master_id=?1 ORDER BY transaction_date ASC", nativeQuery = true
    )
    Double findsumCR(Long id);

    @Query(
            value = "SELECT closing_amount FROM ledger_opening_closing_detail_tbl WHERE ledger_id=?1", nativeQuery = true
    )
    Double findsumCR1(Long id, LocalDate startDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) ,ledger_type FROM `ledger_transaction_postings_tbl`" +
                    " WHERE ledger_type ='DR' AND ledger_master_id=?1 AND transaction_date<?2" +
                    " ORDER BY transaction_date ASC", nativeQuery = true
    )
    Double findsumDR1(Long id, LocalDate startDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0),ledger_type FROM `ledger_transaction_postings_tbl` WHERE ledger_type ='DR' AND " +
                    "ledger_master_id=?1 ORDER BY transaction_date ASC", nativeQuery = true
    )
    Double findsumDR(Long id);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) ,ledger_type FROM `ledger_transaction_postings_tbl` WHERE ledger_type ='CR' AND " +
                    "ledger_master_id=?1 AND transaction_date BETWEEN ?2 AND ?3", nativeQuery = true
    )
    Double findmobilesumCR(Long id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0),ledger_type FROM `ledger_transaction_postings_tbl` WHERE ledger_type ='DR' AND " +
                    "ledger_master_id=?1 AND transaction_date BETWEEN ?2 AND ?3", nativeQuery = true
    )
    Double findmobilesumDR(Long id, LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT COUNT(ledger_master_id) FROM `ledger_transaction_postings_tbl` WHERE ledger_master_id=?1 " +
                    "AND status=?2", nativeQuery = true
    )
    Long findByLedgerTranx(Long id, boolean b);

    @Query(
            value = "SELECT COUNT(associate_groups_id) FROM `ledger_transaction_postings_tbl` WHERE associate_groups_id=?1 " +
                    "AND status=?2", nativeQuery = true
    )
    Long findByLedgerGroupTranx(Long id, boolean b);

    List<LedgerTransactionPostings> findByTransactionTypeIdAndTransactionIdAndStatus(long l, Long id, boolean b);

    @Query(
            value = " SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` WHERE ledger_master_id=?1 AND " +
                    "outlet_id=?2 AND branch_id=?3 AND " +
                    "ledger_type=?4 AND transaction_date <=?5 ", nativeQuery = true
    )
    Double findLedgerOpeningBranch(Long ledgerId, Long outletId, Long branchId, String ledgerType, LocalDate startDate);

    @Query(
            value = " SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` WHERE ledger_master_id=?1 AND " +
                    "outlet_id=?2 AND branch_id IS NULL AND " +
                    "ledger_type=?3 AND transaction_date <= ?4", nativeQuery = true
    )
    Double findLedgerOpening(Long ledgerId, Long outletId, String ledgerType, LocalDate startDate);


    @Query(
            value = " SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` WHERE ledger_master_id=?1 AND " +
                    "ledger_type=?2 AND transaction_date <= ?3", nativeQuery = true
    )
    Double findMobileLedgerOpening(Long ledgerId, String ledgerType, LocalDate startDate);

    @Query(
            value = " SELECT ledger_master_id FROM ledger_transaction_postings_tbl WHERE" +
                    " transaction_id=?1 AND transaction_type_id =?2 ", nativeQuery = true
    )
    List<Long> findByTransactionId(Long id, Long id1);

    @Query(
            value = "SELECT COUNT(*) FROM `ledger_transaction_postings_tbl` WHERE  outlet_id=?1 AND branch_id=?2 " +
                    "AND transaction_date BETWEEN ?3 AND ?4 AND transaction_type_id=?5 AND status=?6 AND " +
                    "ledger_type=?7", nativeQuery = true)
    Double findTotalNumberInvoices(Long outletId, Long branchId, LocalDate startDate, LocalDate endMonthDate,
                                   Long tranxTypeId, boolean status, String type);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND " +
                    "branch_id=?2 AND transaction_date BETWEEN ?3 AND ?4 AND transaction_type_id=?5 AND " +
                    "status=?6 AND ledger_type=?7",
            nativeQuery = true)
    Double findTotalInvoiceAmtwithBr(Long outletId, Long branchId, LocalDate startDate, LocalDate endMonthDate,
                                     Long tranxTypeId, boolean status, String type);

    @Query(
            value = "SELECT COUNT(*) FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND " +
                    "transaction_date BETWEEN ?2 AND ?3 AND transaction_type_id=?4 AND " +
                    "status=?5 AND branch_id IS NULL AND ledger_type=?6", nativeQuery = true)
    Double findTotalNumberInvoicesNoBranch(Long outletId, LocalDate startDate, LocalDate endMonthDate,
                                           Long tranxTypeId, boolean status, String type);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND " +
                    "transaction_date BETWEEN ?2 AND ?3 AND transaction_type_id=?4 AND " +
                    "status=?5 AND branch_id IS NULL AND ledger_type=?6", nativeQuery = true)
    Double findTotalInvoicesAmtNoBranch(Long outletId, LocalDate startDate, LocalDate endMonthDate,
                                        Long tranxTypeId, boolean status, String type);

    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND branch_id=?2 AND " +
                    "transaction_date BETWEEN ?3 AND ?4 AND transaction_type_id=?5 AND ledger_type=?6 " +
                    "ORDER by transaction_date",
            nativeQuery = true)
    List<LedgerTransactionPostings> findReceiptDetails(Long outletId, Long branchId, LocalDate startDate,
                                                       LocalDate endDate, Long tranxTypeId, String ledgerType);

    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE outlet_id=?1 AND " +
                    "transaction_date BETWEEN ?2 AND ?3 AND transaction_type_id=?4 AND ledger_type=?5 " +
                    "AND branch_id IS NULL ORDER by transaction_date",
            nativeQuery = true)
    List<LedgerTransactionPostings> findReceiptDetailsNoBranch(Long outletId, LocalDate startDate, LocalDate endDate,
                                                               Long tranxTypeId, String ledgerType);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` " +
                    "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id " +
                    "WHERE ledger_master_tbl.unique_code=?1 AND ledger_transaction_postings_tbl.outlet_id=?2 AND " +
                    "ledger_transaction_postings_tbl.branch_id=?3 AND ledger_transaction_postings_tbl.transaction_date " +
                    "BETWEEN ?4 AND ?5 AND ledger_type=?6 ", nativeQuery = true
    )
    Double findCashBankBookTotal(String ledgerCode, Long outletId, Long branchId, LocalDate startDate,
                                 LocalDate endDate, String type);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM " +
                    "`ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl " +
                    "ON ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE " +
                    "ledger_master_tbl.unique_code=?1 AND ledger_transaction_postings_tbl.outlet_id=?2 AND " +
                    "ledger_transaction_postings_tbl.branch_id IS NULL AND " +
                    "ledger_transaction_postings_tbl.transaction_date BETWEEN ?3 AND ?4 AND ledger_type=?5  ", nativeQuery = true
    )
    Double findCashBankBookTotalWithNoBR(String ledgerCode, Long outletId, LocalDate startDate,
                                         LocalDate endDate, String type);

    /***** Direct Expenses *****/
    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` " +
                    "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id " +
                    "WHERE ledger_master_tbl.principle_id=?1 AND ledger_transaction_postings_tbl.outlet_id=?2 AND " +
                    "ledger_transaction_postings_tbl.branch_id=?3 AND ledger_transaction_postings_tbl.transaction_date " +
                    "BETWEEN ?4 AND ?5 AND ledger_type=?6 ", nativeQuery = true
    )
    Double findExpensesTotal(Long principleId, Long outletId, Long branchId, LocalDate startDate,
                             LocalDate endDate, String type);

    @Query(
            value = "SELECT ledger_master_id FROM `ledger_transaction_postings_tbl` " +
                    "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id " +
                    "WHERE ledger_master_tbl.principle_id=?1 AND ledger_transaction_postings_tbl.outlet_id=?2 AND " +
                    "ledger_transaction_postings_tbl.branch_id=?3 AND ledger_transaction_postings_tbl.transaction_date " +
                    "BETWEEN ?4 AND ?5 AND ledger_type=?6 ", nativeQuery = true
    )
    Double findExpensesIdTotal(Long principleId, Long outletId, Long branchId, LocalDate startDate,
                               LocalDate endDate, String type);

    /***** Indirect Expenses *****/
    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM " +
                    "`ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl " +
                    "ON ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE " +
                    "ledger_master_tbl.principle_id=?1=?1 AND ledger_transaction_postings_tbl.outlet_id=?2 AND " +
                    "ledger_transaction_postings_tbl.branch_id IS NULL AND " +
                    "ledger_transaction_postings_tbl.transaction_date BETWEEN ?3 AND ?4 AND ledger_type=?5  ", nativeQuery = true
    )
    Double findExpensesTotalWithNoBR(Long principleId, Long outletId, LocalDate startDate,
                                     LocalDate endDate, String type);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM " +
                    "`ledger_transaction_postings_tbl` WHERE ledger_transaction_postings_tbl.ledger_master_id=?1 ", nativeQuery = true
    )
    Double findExpensesAmtTotalWithNoBR(Long principleId);

    @Query(
            value = "SELECT ledger_master_id FROM " +
                    "`ledger_transaction_postings_tbl` LEFT JOIN ledger_master_tbl " +
                    "ON ledger_transaction_postings_tbl.ledger_master_id=ledger_master_tbl.id WHERE " +
                    "ledger_master_tbl.principle_id=?1 AND ledger_transaction_postings_tbl.outlet_id=?2 AND " +
                    "ledger_transaction_postings_tbl.branch_id IS NULL AND " +
                    "ledger_transaction_postings_tbl.transaction_date BETWEEN ?3 AND ?4 AND " +
                    "ledger_transaction_postings_tbl.ledger_type=?5 group by ledger_master_id", nativeQuery = true
    )
    List<String> findExpensesIDTotalWithNoBR(Long principleId, Long outletId, LocalDate startDate,
                                             LocalDate endDate, String type);

    @Query(
            value = "SELECT COUNT(ledger_master_id=?1),ledger_type FROM `ledger_transaction_postings_tbl` WHERE transaction_date BETWEEN ?2 AND ?3", nativeQuery = true
    )
    Double findTotalInvoices(Long id, LocalDate startDatep, LocalDate endDatep);

    @Query(
            value = "SELECT * FROM `ledger_transaction_postings_tbl` WHERE status=?1 AND " +
                    "ledger_master_id=?2 ORDER BY transaction_date ASC", nativeQuery = true
    )
    List<LedgerTransactionPostings> findMobileDetailsFisc(boolean b, Long ledger_master_id);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` WHERE ledger_master_id=?1 AND ledger_type=?2 AND transaction_date=?3 AND status=?4", nativeQuery = true
    )
    Double totalTodayRevenue(Long ledgerId, String cr, LocalDate startDatep, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` WHERE ledger_master_id=?1 AND ledger_type=?2 AND transaction_date=?3 AND status=?4 AND outlet_id=?5", nativeQuery = true
    )
    Double totalTodayRevenueOutlet(Long ledgerId, String cr, LocalDate startDatep, boolean b, Long id);   //for web app

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` WHERE ledger_master_id=?1 AND ledger_type=?2 AND transaction_date BETWEEN ?3 AND ?4 AND status=?5", nativeQuery = true
    )
    Double totalWeeklyRevenue(Long ledgerId, String cr, LocalDate startOfWeek, LocalDate endOfWeek, boolean b);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM `ledger_transaction_postings_tbl` WHERE ledger_master_id=?1 AND ledger_type=?2 AND transaction_date BETWEEN ?3 AND ?4 AND status=?5 AND outlet_id=?6", nativeQuery = true
    )
    Double totalWeeklyRevenueOutlet(Long ledgerId, String cr, LocalDate startOfWeek, LocalDate endOfWeek, boolean b, Long id);   //for web app

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM ledger_transaction_postings_tbl WHERE " +
                    "ledger_master_id=?1 AND transaction_date BETWEEN ?2 AND ?3 AND " +
                    "ledger_type=?4", nativeQuery = true
    )
    Double findSumDRCR(Long valueOf, LocalDate startDatep, LocalDate endDatep, String dr);

    @Query(
            value = "SELECT COUNT(*) FROM ledger_transaction_postings_tbl WHERE " +
                    "ledger_type =?1 AND transaction_type_id =?2 AND ledger_master_id=?3", nativeQuery = true
    )
    Long findSdInvoiceCount(String type, Long tranxType, Long ledgerId);

    @Query(
            value = "SELECT IFNULL(SUM(amount),0.0) FROM ledger_transaction_postings_tbl where transaction_date<?1 AND ledger_type=?2 AND ledger_master_id=?3 AND status=?4", nativeQuery = true
    )
    Double sumOfOpeningAmtWithdate(LocalDate startDatep, String dr, Long id, boolean b);

    List<LedgerTransactionPostings> findByTranxCode(String tranx_code);

    @Query(
            value = "SELECT COUNT(*) FROM ledger_transaction_postings_tbl WHERE ledger_transaction_postings_tbl.status=1 AND  " +
                    "ledger_type =?1 AND transaction_type_id =?2 AND ledger_master_id=?3",nativeQuery = true
    )
    Long findInvoiceCount(String type,Long tranxType,Long ledgerId);
}
