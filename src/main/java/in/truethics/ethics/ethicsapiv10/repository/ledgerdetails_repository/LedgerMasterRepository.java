package in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository;

import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LedgerMasterRepository extends JpaRepository<LedgerMaster, Long> {

    /* Get Sundry Creditors by outlet id */
    @Query(
            value = "SELECT id,ledger_name,ledger_code,state_code, sales_rate, is_first_discount_per_calculate," +
                    " take_discount_amount_in_lumpsum FROM ledger_master_tbl WHERE outlet_id =?1 AND " +
                    "principle_groups_id =5 AND status=1 AND branch_id IS NULL",
            nativeQuery = true
    )
    List<Object[]> findSundryCreditorsByOutletId(Long outletId);

    /*Get Sundry Creditors by OutletId and BranchId*/
    @Query(
            value = "SELECT id,ledger_name,ledger_code,state_code, sales_rate, is_first_discount_per_calculate," +
                    " take_discount_amount_in_lumpsum FROM ledger_master_tbl WHERE outlet_id=?1 AND branch_id=?2 AND" +
                    " principle_groups_id=5 AND status=1",
            nativeQuery = true
    )
    List<Object[]> findSundryCreditorsByOutletIdAndBranchId(Long outletId, Long branchId);


    /* Get Sundry Debtors by outlet id */
    @Query(
            value = "SELECT id,ledger_name,ledger_code,state_code, sales_rate, fssai_expiry, drug_expiry FROM ledger_master_tbl " +
                    "WHERE outlet_id =?1 AND " +
                    "principle_groups_id =1 AND status=1 AND branch_id IS NULL",
            nativeQuery = true
    )
    List<Object[]> findSundryDebtorsByOutletId(Long outletId);

    /* Get Sundry Debtors by outlet id  and Brnach Id*/
    @Query(
            value = "SELECT id,ledger_name,ledger_code,state_code, sales_rate FROM ledger_master_tbl " +
                    "WHERE outlet_id =?1 AND " + "branch_id=?2" +
                    " AND principle_groups_id =1 AND status=1",
            nativeQuery = true
    )
    List<Object[]> findSundryDebtorsByOutletIdAndBranchId(Long outletId, Long branchId);

    /* Get Cash-In Hand  by outlet id */
    @Query(
            value = "SELECT id,ledger_name,ledger_code,state_code FROM ledger_master_tbl WHERE outlet_id =?1 AND " +
                    "principle_groups_id =3 And status=1 And branch_id IS NULL",
            nativeQuery = true
    )
    List<Object[]> findCashInHandByOutletId(Long outletId);

    /* Get Cash-In Hand  by outlet id and branch id */
    @Query(
            value = "SELECT id,ledger_name,ledger_code,state_code FROM ledger_master_tbl WHERE outlet_id =?1 AND " +
                    "principle_groups_id =3 And status=1 And branch_id=?2",
            nativeQuery = true
    )
    List<Object[]> findCashInHandByOutletIdAndBranch(Long outletId, Long branchId);

    /* Get Bank Accounts by outlet id */
    @Query(
            value = "SELECT id,ledger_name,ledger_code,state_code FROM ledger_master_tbl WHERE outlet_id =?1 AND " +
                    "principle_groups_id =2 And status=1 AND branch_id IS NULL",
            nativeQuery = true
    )
    List<Object[]> findBankAccountsByOutletId(Long outletId);

    @Query(
            value = "SELECT id,ledger_name,ledger_code,state_code FROM ledger_master_tbl WHERE outlet_id =?1 AND " +
                    "principle_groups_id =2 And status=1 AND branch_id=?2",
            nativeQuery = true
    )
    List<Object[]> findBankAccountsByOutletIdAndBranch(Long outletId, Long branch_id);


    List<LedgerMaster> findByOutletIdAndPrinciplesIdAndStatus(Long outletId, Long id, boolean b);

    LedgerMaster findByIdAndStatus(Long id, boolean b);

    LedgerMaster findByOutletIdAndLedgerNameIgnoreCase(Long id, String round_off);

    LedgerMaster findByIdAndOutletIdAndStatus(long purchase_id, Long id, boolean b);

    LedgerMaster findByLedgerNameIgnoreCaseAndOutletId(String round_off, Long id);
    LedgerMaster findFirstByLedgerNameIgnoreCase(String round_off);

    @Query(
            value = "SELECT * FROM `ledger_master_tbl` WHERE principle_groups_id =3 And status=1 And outlet_id=?1 " +
                    "And branch_id IS NULL", nativeQuery = true
    )
    LedgerMaster findLedgerIdAndName(Long outlet_id);

    @Query(
            value = "SELECT * FROM `ledger_master_tbl` WHERE principle_groups_id =3 And status=1 And outlet_id=?1 " +
                    "And branch_id=?2", nativeQuery = true
    )
    LedgerMaster findLedgerIdAndBranchIdAndName(Long outlet_id, Long branch_id);


//    List<LedgerMaster> findByOutletIdAndPrincipleGroupsId(Long id, Long i);

    //List<LedgerMaster> findByOutletIdAndPrinciplesId(Long id, long l);


    @Query(
            value = "SELECT * FROM `ledger_master_tbl` WHERE (principle_groups_id =3 Or principle_groups_id=2) And " +
                    "status=1 And outlet_id=?1 And branch_id IS NULL", nativeQuery = true
    )
    List<LedgerMaster> findBankAccountCashAccount(Long id);


    @Query(
            value = "SELECT * FROM `ledger_master_tbl` WHERE (principle_groups_id =3 Or principle_groups_id=2)  And " +
                    "status=1 And outlet_id=?1 AND branch_id=?2", nativeQuery = true
    )
    List<LedgerMaster> findBranchBankAccountCashAccount(Long id, Long branchId);

    @Query(
            value = "SELECT * FROM `ledger_master_tbl` WHERE  (principle_groups_id  NOT IN (3,2) OR " +
                    "principle_groups_id IS NULL) And status=1 And outlet_id=?1 And branch_id IS NULL", nativeQuery = true
    )
    List<LedgerMaster> findledgers(Long id);

    @Query(
            value = "SELECT * FROM `ledger_master_tbl` WHERE  (principle_groups_id  NOT IN (3,2) OR principle_groups_id IS NULL) And status=1 And outlet_id=?1 And branch_id=?2", nativeQuery = true
    )
    List<LedgerMaster> findledgersByBranch(Long id, Long id1);

    List<LedgerMaster> findByOutletIdAndBranchIdAndPrinciplesIdAndStatus(Long outletId, Long branchId, Long id, boolean b);


    LedgerMaster findByUniqueCodeAndOutletIdAndBranchIdAndStatus(String caih, Long id, Long id1, boolean b);

    LedgerMaster findByUniqueCodeAndOutletIdAndStatus(String caih, Long id, boolean b);

    LedgerMaster findByMobileAndStatus(Long mobileNo, boolean b);

    LedgerMaster findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus(String counter_customer, Long id, Long id1, boolean b);

    @Query(
            value = "SELECT ledger_master_tbl.opening_bal FROM ledger_master_tbl WHERE outlet_id=?1 AND branch_id=?2 AND status=?3 AND" +
                    " id=?4 ", nativeQuery = true
    )
    Double findByIdAndOutletIdAndBranchIdAndStatus(Long id, Long id1, boolean b, Long principle_id, LocalDate startMonthDate);

    @Query(
            value = "SELECT ledger_master_tbl.opening_bal FROM ledger_master_tbl WHERE outlet_id=?1 AND branch_id=?2 AND status=?3 AND" +
                    " id=?4 ", nativeQuery = true
    )
    Double findByIdAndOutletIdAndBranchIdAndStatuslm(Long id, Long id1, boolean b, Long principle_id);

    @Query(
            value = "SELECT ledger_master_tbl.opening_bal FROM ledger_master_tbl WHERE outlet_id=?1 AND status=?2 AND" +
                    " id=?3 ", nativeQuery = true
    )
    Double findByIdAndOutletIdAndStatuslm(Long id, boolean b, Long principle_id);

    @Query(
            value = " SELECT * FROM ledger_master_tbl WHERE outlet_id=?1 AND (branch_id=?2 OR branch_id IS NULL)" +
                    "AND principle_id=?3 AND principle_groups_id=?4 " +
                    "AND lower(ledger_name=?5) AND status=?6", nativeQuery = true
    )
    LedgerMaster findDuplicateWithName(Long id, Long branchId, Long principleId, Long subPrincipleId, String ledger_name, boolean b);

    @Query(
            value = " SELECT * FROM ledger_master_tbl WHERE outlet_id=?1 AND (branch_id=?2 OR branch_id IS NULL)" +
                    "AND principle_id=?3 AND principle_groups_id=?4 " +
                    "AND lower(ledger_code=?5) AND status=?6", nativeQuery = true
    )
    LedgerMaster findDuplicateWithCode(Long id, Long branchId, long principle_id, Long pgroupId, String ledger_code, boolean b);

    @Query(
            value = " SELECT * FROM ledger_master_tbl WHERE outlet_id=?1 AND (branch_id=?2 OR branch_id IS NULL)" +
                    "AND principle_id=?3 AND lower(ledger_name=?4) AND status=?5", nativeQuery = true
    )
    LedgerMaster findDuplicate(Long id, Long branchId, long principle_id, String ledger_name, boolean b);


    @Query(
            value = "SELECT ledger_master_tbl.opening_bal FROM ledger_master_tbl WHERE ledger_master_tbl.id=?1",
            nativeQuery = true
    )
    Double findOpeningBalance(Long ledgerId);


    LedgerMaster findByIdAndIsDefaultLedgerAndStatus(long id, boolean b, boolean b1);


    List<LedgerMaster> findByOutletIdAndBranchIdAndStatusOrderByIdDesc(Long id, Long id1, boolean b);


    List<LedgerMaster> findByOutletIdAndBranchIdAndPrincipleGroupsIdAndStatus(Long id, Long id1, long l, boolean b);

    List<LedgerMaster> findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(Long id, boolean b);

    List<LedgerMaster> findByOutletIdAndPrinciplesIdAndStatusAndBranchIsNull(Long outletId, Long id, boolean b);




    LedgerMaster findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull(String counter_customer, Long id, boolean b);

    List<LedgerMaster> findByOutletIdAndPrincipleGroupsIdAndStatusAndBranchIsNull(Long id, long l, boolean b);

    @Query(
            value = " SELECT IFNULL(SUM(opening_bal),0.0) FROM `ledger_master_tbl` WHERE id=?1 AND " +
                    "outlet_id=?2 AND branch_id=?3 AND opening_bal_type=?4 ", nativeQuery = true
    )
    Double findLedgerOpeningStocksBranch(Long productId, Long outletId, Long branchId, String openingType);

    @Query(
            value = " SELECT IFNULL(SUM(opening_bal),0.0) FROM `ledger_master_tbl` WHERE id=?1 AND " +
                    "outlet_id=?2 AND branch_id IS NULL AND opening_bal_type=?3 ", nativeQuery = true
    )
    Double findLedgerOpeningStocks(Long productId, Long outletId, String openingType);

    @Query(
            value = " SELECT IFNULL(SUM(opening_bal),0.0) FROM `ledger_master_tbl` WHERE id=?1 AND " +
                    "opening_bal_type=?2 ", nativeQuery = true
    )
    Double findMobileLedgerOpeningStocks(Long productId,String openingType);

    LedgerMaster findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(Long id, Long id1, String round_off);

    LedgerMaster findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(Long id, String round_off);


    LedgerMaster findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull(String caih, Long id, boolean b);

    @Query(
            value = " SELECT * FROM `ledger_master_tbl` WHERE outlet_id=?1 AND branch_id=?2 " +
                    "AND (principle_groups_id=?3 OR principle_groups_id=?4) AND status=?5 ", nativeQuery = true
    )
    List<LedgerMaster> findBySCSDWithBranch(Long outletId, Long branchId, long pg1, long pg2, boolean b);

    @Query(
            value = " SELECT * FROM `ledger_master_tbl` WHERE outlet_id=?1 AND branch_id IS NULL " +
                    "AND (principle_groups_id=?2 OR principle_groups_id=?3) AND status=?4 ", nativeQuery = true
    )
    List<LedgerMaster> findBySCSD(Long outletId, long pg1, long pg2, boolean b);

    LedgerMaster findByOutletIdAndBranchIdAndStatusAndId(Long id, Long id1, boolean b, Long ledgerId);

    LedgerMaster findByOutletIdAndStatusAndIdAndBranchIsNull(Long id, boolean b, Long ledgerId);

    @Query(
            value = " SELECT * FROM `ledger_master_tbl` WHERE outlet_id=?1 AND branch_id=?2" +
                    " AND (ledger_code LIKE ?3 OR ledger_name LIKE ?3 OR city LIKE ?3 OR mobile LIKE ?3)" +
                    " AND (principle_groups_id=?4 OR principle_groups_id=?5) AND status=?6", nativeQuery = true
    )
    List<LedgerMaster> findSearchKeyWithBranch(Long outletId, Long branchId, String searchKey, long l, long l1, boolean b);

    @Query(
            value = "SELECT * FROM `ledger_master_tbl` WHERE outlet_id=?1 AND branch_id IS NULL" +
                    " AND (ledger_code LIKE %?2% OR ledger_name LIKE %?2% OR city LIKE %?2% OR mobile LIKE %?2%)" +
                    " AND (principle_groups_id=?4 OR principle_groups_id=?5) AND status=?6", nativeQuery = true
    )
    List<LedgerMaster> findSearchKey(Long outletId, String searchKey, long l, long l1, boolean b);


    List<LedgerMaster> findByUniqueCodeAndBranchIdAndOutletIdAndStatus(String baac, Long id, Long id1, boolean b);
    List<LedgerMaster> findByUniqueCodeAndBranchIsNullAndOutletIdAndStatus(String baac, Long id, boolean b);
    List<LedgerMaster> findByPrincipleGroupsIdAndStatus(long l, boolean b);
    @Query(
            value = "SELECT * FROM `ledger_master_tbl` WHERE (principle_groups_id=1 OR principle_groups_id=5) AND status=?1", nativeQuery = true
    )
    List<LedgerMaster> findAllLedgerList(boolean b);

    List<LedgerMaster> findByPrinciplesIdAndStatus(long l, boolean b);

    @Query(
            value = "SELECT * FROM `ledger_master_tbl` WHERE outlet_id=?1 AND ledger_name=?2 AND branch_id IS NULL", nativeQuery = true
    )
    LedgerMaster findRoundOff(Long id, String roundOff);

    LedgerMaster findByLedgerCodeAndStatusAndOutletIdAndBranchId(String tcs, boolean b, Long id, Long id1);


    LedgerMaster findByIdAndStatusAndIsDeleted(Long ledgerId, boolean b, boolean b1);

    LedgerMaster findByLedgerCodeAndStatusAndOutletIdAndBranchIsNull(String tcs, boolean b, Long id);


    LedgerMaster findByLedgerCodeAndStatus(String companyCode, boolean b);

    LedgerMaster findByLedgerCodeAndUniqueCodeAndOutletIdAndStatus(String companyCode, String sudr, Long id, boolean b);

    List<LedgerMaster> findByUniqueCodeAndStatus(String caih, boolean b);

    @Query(
            value = "SELECT ledger_master_tbl.opening_bal FROM ledger_master_tbl WHERE outlet_id=?1 AND status=?2 AND" +
                    " id=?3 AND opening_bal_type=?4", nativeQuery = true
    )
    Double findOpening(Long id, boolean b, Long ledgerId,String type);

    List<LedgerMaster> findByUniqueCodeAndBranchIdAndOutletIdAndStatusAndColumnR(String baac, Long id, Long id1, boolean b, boolean b1);

    List<LedgerMaster> findByUniqueCodeAndBranchIdIsNullAndOutletIdAndStatusAndColumnR(String baac, Long id, boolean b, boolean b1);

    LedgerMaster findByLedgerNameIgnoreCaseAndStatus(String gvBankName, boolean b);

    List<LedgerMaster> findByUniqueCodeAndOutletIdAndStatusAndBranchIdIsNull(String baac, Long id, boolean b);
    @Query(
            value = "SELECT ledger_master_tbl.opening_bal, ledger_master_tbl.opening_bal_type FROM" +
                    " ledger_master_tbl WHERE id=?1 AND status=?2 ", nativeQuery = true

//            value = "SELECT opening_bal_type, Closing_bal, FROM `ledger_master_tbl` WHERE id=?1 AND status =?2", nativeQuery = true
    )
    List<String> findOpeningByIdAndStatus(Long Ledger_id, boolean b);

    LedgerMaster findByAccountNumberAndStatus(String gvBankName, boolean b);


    @Query(value = "SELECT * FROM " +
            "ledger_master_tbl WHERE state_head_id=:stateId AND status=:flag AND ledger_code not like %:keyword% ",
            nativeQuery = true)
    LedgerMaster findByStateHeadId(@Param("stateId") Long stateId,@Param("flag") Boolean flag,@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM " +
            "ledger_master_tbl WHERE regional_head_id=:regionId AND status=:flag AND ledger_code not like %:keyword% ",
            nativeQuery = true)
    LedgerMaster findByRegionHead(@Param("regionId") Long regionId,@Param("flag") Boolean flag,@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM " +
            "ledger_master_tbl WHERE zonal_head_id=:zoneId AND status=:flag AND ledger_code not like %:keyword% ",
            nativeQuery = true)
    LedgerMaster findByZonalHead(@Param("zoneId") Long zoneId,@Param("flag") Boolean flag,@Param("keyword") String keyword);
    @Query(value = "SELECT * FROM " +
            "ledger_master_tbl WHERE district_head_id=:districtId AND status=:flag AND ledger_code not like %:keyword% ",
            nativeQuery = true)
    LedgerMaster findByDistrictHead(@Param("districtId") Long districtId,@Param("flag") Boolean flag,@Param("keyword") String keyword);
    LedgerMaster findByRegionalHeadIdAndStatus(Long id, boolean b);

    LedgerMaster findByZonalHeadIdAndStatus(Long id, boolean b);

}

