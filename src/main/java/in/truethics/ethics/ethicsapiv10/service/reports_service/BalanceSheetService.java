package in.truethics.ethics.ethicsapiv10.service.reports_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.contra_repository.TranxContraDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository.TranxJournalDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurReturnsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesReturnRepository;
import in.truethics.ethics.ethicsapiv10.util.DateConvertUtil;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class BalanceSheetService {


    @Autowired
    private LedgerTransactionPostingsRepository postingsRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    FiscalYearRepository fiscalYearRepository;
    @Autowired
    LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    PrincipleRepository principleRepository;
    @Autowired
    FoundationRepository foundationRepository;
    @Autowired
    PrincipleGroupsRepository principleGroupsRepository;
    @Autowired
    TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    TranxSalesReturnRepository tranxSalesReturnRepository;
    @Autowired
    TranxReceiptPerticularsDetailsRepository tranxReceiptPerticularsDetailsRepository;
    @Autowired
    TranxPurReturnsRepository tranxPurReturnsRepository;
    @Autowired
    TranxPaymentPerticularsDetailsRepository tranxPaymentPerticularsDetailsRepository;
    @Autowired
    TranxCreditNoteDetailsRepository tranxCreditNoteDetailsRepository;
    @Autowired
    TranxDebitNoteDetailsRepository tranxDebitNoteDetailsRepository;
    @Autowired
    TranxJournalDetailsRepository tranxJournalDetailsRepository;
    @Autowired
    TranxContraDetailsRepository tranxContraDetailsRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TransactionTypeMasterRepository transactionTypeMasterRepository;

    public Object getBalanceSheetAc(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        LedgerTransactionDetails ledgerTransactionDetails = null;
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Map<String, String[]> paramMap = request.getParameterMap();
            Long branchId = users.getBranch() != null ? users.getBranch().getId() : null;
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);
            } else {
                List<Object[]> list = new ArrayList<>();
                list = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatus();
                System.out.println("list" + list);
                Object obj[] = list.get(0);
                System.out.println("start Date:" + obj[0].toString());
                System.out.println("end Date:" + obj[1].toString());
                startDatep = LocalDate.parse(obj[0].toString());
                endDatep = LocalDate.parse(obj[1].toString());
            }

            Double fixed_assets = 0.0, investments = 0.0, current_assets = 0.0, capital_account = 0.0, loans = 0.0, current_liabilities = 0.0, purchaseAC = 0.0, salesAC = 0.0;
            Double list = 0.0;
//            List<Object[]> list = new ArrayList<>();


            String clsql = "SELECT IFNULL(SUM(closing_amount),0) AS closeAmount FROM ledger_opening_closing_detail_tbl AS locdt" +
                    " LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE ledger_master_tbl.principle_id=6" +
                    " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' AND locdt.status=1 AND" +
                    " locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                clsql += " AND locdt.branch_id=" + branchId;
            else clsql += " AND locdt.branch_id IS NULL ";

            System.out.println("clsql " + clsql);
            Query query = entityManager.createNativeQuery(clsql);
            List<Double> resultList = query.getResultList();
            current_liabilities = resultList.get(0);
            System.out.println("current_liabilities " + current_liabilities);

            String fasql = "SELECT IFNULL(SUM(closing_amount),0) AS closeAmount FROM ledger_opening_closing_detail_tbl AS locdt" +
                    " LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE ledger_master_tbl.principle_id=1" +
                    " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' AND locdt.status=1 AND" +
                    " locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                fasql += " AND locdt.branch_id=" + branchId;
            else fasql += " AND locdt.branch_id IS NULL ";

            System.out.println("fasql " + fasql);
            query = entityManager.createNativeQuery(fasql);
            resultList = query.getResultList();
            fixed_assets = resultList.get(0);
            System.out.println("fixed_assets " + fixed_assets);

            String invsql = "SELECT IFNULL(SUM(closing_amount),0) AS closeAmount FROM ledger_opening_closing_detail_tbl AS locdt" +
                    " LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE ledger_master_tbl.principle_id=2" +
                    " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' AND locdt.status=1 AND" +
                    " locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                invsql += " AND locdt.branch_id=" + branchId;
            else invsql += " AND locdt.branch_id IS NULL ";

            System.out.println("invsql " + invsql);
            query = entityManager.createNativeQuery(invsql);
            resultList = query.getResultList();
            investments = resultList.get(0);
            System.out.println("investments " + investments);

            String cavsql = "SELECT IFNULL(SUM(closing_amount),0) AS closeAmount FROM ledger_opening_closing_detail_tbl AS locdt" +
                    " LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE ledger_master_tbl.principle_id=3" +
                    " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' AND locdt.status=1 AND" +
                    " locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                cavsql += " AND locdt.branch_id=" + branchId;
            else cavsql += " AND locdt.branch_id IS NULL ";

            System.out.println("cavsql " + cavsql);
            query = entityManager.createNativeQuery(cavsql);
            resultList = query.getResultList();
            current_assets = resultList.get(0);
            System.out.println("current_assets " + current_assets);

            String capsql = "SELECT IFNULL(SUM(closing_amount),0) AS closeAmount FROM ledger_opening_closing_detail_tbl AS locdt" +
                    " LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE ledger_master_tbl.principle_id=4" +
                    " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' AND locdt.status=1 AND" +
                    " locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                capsql += " AND locdt.branch_id=" + branchId;
            else capsql += " AND locdt.branch_id IS NULL ";

            System.out.println("capsql " + capsql);
            query = entityManager.createNativeQuery(capsql);
            resultList = query.getResultList();
            capital_account = resultList.get(0);
            System.out.println("capital_account " + capital_account);

            String loansql = "SELECT IFNULL(SUM(closing_amount),0) AS closeAmount FROM ledger_opening_closing_detail_tbl AS locdt" +
                    " LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE ledger_master_tbl.principle_id=5" +
                    " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' AND locdt.status=1 AND" +
                    " locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                loansql += " AND locdt.branch_id=" + branchId;
            else loansql += " AND locdt.branch_id IS NULL ";

            System.out.println("loansql " + loansql);
            query = entityManager.createNativeQuery(loansql);
            resultList = query.getResultList();
            loans = resultList.get(0);
            System.out.println("loans " + loans);

            String pasql = "SELECT IFNULL(SUM(closing_amount),0) AS closeAmount FROM ledger_opening_closing_detail_tbl AS locdt" +
                    " LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE ledger_master_tbl.foundation_id=3" +
                    " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' AND locdt.status=1 AND" +
                    " locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                pasql += " AND locdt.branch_id=" + branchId;
            else pasql += " AND locdt.branch_id IS NULL ";

            System.out.println("pasql " + pasql);
            query = entityManager.createNativeQuery(pasql);
            resultList = query.getResultList();
            purchaseAC = resultList.get(0);
            System.out.println("purchaseAC " + purchaseAC);

            String sasql = "SELECT IFNULL(SUM(closing_amount),0) AS closeAmount FROM ledger_opening_closing_detail_tbl AS locdt" +
                    " LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE ledger_master_tbl.foundation_id=4" +
                    " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' AND locdt.status=1 AND" +
                    " locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                sasql += " AND locdt.branch_id=" + branchId;
            else sasql += " AND locdt.branch_id IS NULL ";

            System.out.println("sasql " + sasql);
            query = entityManager.createNativeQuery(sasql);
            resultList = query.getResultList();
            salesAC = resultList.get(0);
            System.out.println("salesAC " + salesAC);


            /*if (endDate != null) {
                if (users.getBranch() != null) {
                    //If Branch Found
                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 6L, startDatep, endDatep);
                    current_liabilities = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 1L, startDatep, endDatep);
                    fixed_assets = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 2L, startDatep, endDatep);
                    investments = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 3L, startDatep, endDatep);
                    current_assets = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 4L, startDatep, endDatep);
                    capital_account = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 5L, startDatep, endDatep);
                    loans = list;

                }
                else {
                    //If Branch Not Found
                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 6L, startDatep, endDatep);
                    current_liabilities = list;


                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 1L, startDatep, endDatep);
                    fixed_assets = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 2L, startDatep, endDatep);
                    investments = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 3L, startDatep, endDatep);
                    current_assets = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 4L, startDatep, endDatep);
                    capital_account = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 5L, startDatep, endDatep);
                    loans = list;

                }
            } else {
                if (users.getBranch() != null) {
                    //If Branch Found
                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 6L, startDatep, endDatep);
                    current_liabilities = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 1L, startDatep, endDatep);
                    fixed_assets = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 2L, startDatep, endDatep);
                    investments = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 3L, startDatep, endDatep);
                    current_assets = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 4L, startDatep, endDatep);
                    capital_account = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndBranchStatus(users.getOutlet().getId(), users.getBranch().getId(), true, 5L, startDatep, endDatep);
                    loans = list;

                }
                else {
                    //If Branch Not Found
                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 6L, startDatep, endDatep);
                    current_liabilities = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 1L, startDatep, endDatep);
                    fixed_assets = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 2L, startDatep, endDatep);
                    investments = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 3L, startDatep, endDatep);
                    current_assets = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 4L, startDatep, endDatep);
                    capital_account = list;

                    list = postingsRepository.findByDateWiseTotalBalanceAmountOuletAndStatus(users.getOutlet().getId(), true, 5L, startDatep, endDatep);
                    loans = list;

                    list = postingsRepository.findByDateWiseTotalBalanceSheetProfitLossAmountOuletAndStatus(users.getOutlet().getId(), true, 3L, startDatep, endDatep);
                    purchaseAC = list;

                    list = postingsRepository.findByDateWiseTotalBalanceSheetProfitLossAmountOuletAndStatus(users.getOutlet().getId(), true, 4L, startDatep, endDatep);
                    salesAC = list;

                }
            }*/
            // For Debit Side
            res.addProperty("current_liabilities", Math.abs(current_liabilities) > 0 ? current_liabilities : 0);
            res.addProperty("current_liabilities_id", 6);

            res.addProperty("fixed_assets", Math.abs(fixed_assets) > 0 ? fixed_assets : 0);
            res.addProperty("fixed_assets_id", 1);

            res.addProperty("investments", Math.abs(investments) > 0 ? investments : 0);
            res.addProperty("investments_id", 2);

            res.addProperty("current_assets", Math.abs(current_assets) > 0 ? current_assets : 0);
            res.addProperty("current_assets_id", 3);

            res.addProperty("capital_account", Math.abs(capital_account) > 0 ? capital_account : 0);
            res.addProperty("capital_account_id", 4);

            res.addProperty("loans", Math.abs(loans) > 0 ? loans : 0);
            res.addProperty("loans_id", 5);

            res.addProperty("purchaseAC", Math.abs(purchaseAC) > 0 ? purchaseAC : 0);
            res.addProperty("salesAC", Math.abs(salesAC) > 0 ? salesAC : 0);

            res.addProperty("d_start_date", startDatep.toString());
            res.addProperty("d_end_date", endDatep.toString());
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public Object getBalanceAcStep1(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<Object[]> list = new ArrayList<>();
        List<LedgerTransactionDetails> mList = new ArrayList<>();
        Long ledger_master_id = 0L;
        Double credit_total = 0.0;
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Map<String, String[]> paramMap = request.getParameterMap();
            Long branchId = users.getBranch() != null ? users.getBranch().getId() : null;
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            Double opening_balm = 0.0, opening_bal = 0.0;

            Long principle_id = Long.valueOf(request.getParameter("principle_id"));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);

            } else {
                List<Object[]> nlist = new ArrayList<>();
                nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatus();
                for (int i = 0; i < nlist.size(); i++) {
                    Object obj[] = nlist.get(i);
                    System.out.println("start Date:" + obj[0].toString());
                    System.out.println("end Date:" + obj[1].toString());
                    startDatep = LocalDate.parse(obj[0].toString());
                    endDatep = LocalDate.parse(obj[1].toString());
                }
            }

            String sql = "";

            List<PrincipleGroups> principleGroups = principleGroupsRepository.findByPrinciplesIdAndStatus(principle_id, true);
            if(principleGroups.isEmpty()){

                sql = "SELECT IFNULL(closing_amount,0), ledger_id, tranx_action FROM ledger_opening_closing_detail_tbl" +
                        " AS locdt LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE" +
                        " ledger_master_tbl.principle_id=" + principle_id + " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep +
                        "' AND '" + endDatep + "' AND locdt.status=1 AND locdt.outlet_id=" + users.getOutlet().getId();
                if (branchId != null)
                    sql += " AND locdt.branch_id=" + branchId;
                else sql += " AND locdt.branch_id IS NULL ";

                sql += " GROUP BY ledger_id";
            } else {
                sql = "SELECT IFNULL(SUM(closing_amount),0), ledger_id, tranx_action FROM ledger_opening_closing_detail_tbl" +
                        " AS locdt LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE" +
                        " ledger_master_tbl.principle_id=" + principle_id + " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep +
                        "' AND '" + endDatep + "' AND locdt.status=1 AND locdt.outlet_id=" + users.getOutlet().getId();
                if (branchId != null)
                    sql += " AND locdt.branch_id=" + branchId;
                else sql += " AND locdt.branch_id IS NULL ";

                sql += " GROUP BY ledger_master_tbl.principle_groups_id";
            }

            System.out.println("sql " + sql);
            Query query = entityManager.createNativeQuery(sql);
            list = query.getResultList();
            System.out.println("list size " + list.size());


            /*if (endDate != null) {
                if (users.getBranch() != null) {
                    list = postingsRepository.findByPrincipleGroupTotalAmountOuletAndBranchStatusStep1(users.getOutlet().getId(), users.getBranch().getId(), true, principle_id, startDatep, endDatep);
                } else {
                    list = postingsRepository.findByPrincipleGroupTotalAmountOuletAndStatusStep1(users.getOutlet().getId(), true, principle_id, startDatep, endDatep);
                }
            } else {
                if (users.getBranch() != null) {
                    list = postingsRepository.findByPrincipleGroupTotalAmountOuletAndBranchStatusStep1(users.getOutlet().getId(), users.getBranch().getId(), true, principle_id, startDatep, endDatep);
                } else {
                    list = postingsRepository.findByPrincipleGroupTotalAmountOuletAndStatusStep1(users.getOutlet().getId(), true, principle_id, startDatep, endDatep);
                }
            }*/
            JsonArray innerArr = new JsonArray();
            for (int i = 0; i < list.size(); i++) {
                JsonObject inside = new JsonObject();
                Object[] objp = list.get(i);
                credit_total = Double.parseDouble(objp[0].toString());
                ledger_master_id = Long.parseLong(objp[1].toString());
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(ledger_master_id, true);
                inside.addProperty("total_balance", credit_total);
                if (objp[2].toString().equalsIgnoreCase("DR")) {
                    inside.addProperty("debit", credit_total);
                    inside.addProperty("credit", 0.0);

                } else {
                    inside.addProperty("credit", credit_total);
                    inside.addProperty("debit", 0.0);

                }
                if (mLedger.getPrincipleGroups() != null) {
//                    inside.addProperty("particular", mLedger.getPrincipleGroups().getGroupName());
                    inside.addProperty("principleGroup_id", mLedger.getPrincipleGroups().getId());
                }
//                else

                inside.addProperty("particular", mLedger.getPrincipleGroups() != null ? mLedger.getPrincipleGroups().getGroupName() : mLedger.getLedgerName());
                inside.addProperty("ledger_master_id", ledger_master_id);
                innerArr.add(inside);
            }
            Principles principles = principleRepository.findByIdAndStatus(principle_id, true);
            if (principles != null) {
                Long foundationId = principles.getFoundations().getId();
                Foundations foundations = foundationRepository.findByIdAndStatus(foundationId, true);
                if (foundations != null) {
                    String foundationName = foundations.getFoundationName();
                    if (foundationName.equalsIgnoreCase("Assets")) {
                        res.addProperty("rules_type", "DR");
                    } else if (foundationName.equalsIgnoreCase("Liabilities")) {
                        res.addProperty("rules_type", "CR");
                    } else if (foundationName.equalsIgnoreCase("Income")) {
                        res.addProperty("rules_type", "CR");
                    } else if (foundationName.equalsIgnoreCase("Expenses")) {
                        res.addProperty("rules_type", "DR");
                    }
                } else {
                    System.out.println("Foundation Name Not Found..!");
                }
            } else {
                System.out.println("Principle Id Not Found..!");
            }
            res.addProperty("d_start_date", startDatep.toString());
            res.addProperty("d_end_date", endDatep.toString());
            res.add("response", innerArr);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public Object getBalanceAcStep2(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<Object[]> list = new ArrayList<>();
        List<LedgerTransactionDetails> mList = new ArrayList<>();
        Long ledger_master_id = 0L;
        Double credit_total = 0.0;
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            Double opening_balm = 0.0, opening_bal = 0.0;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Long branchId = users.getBranch() != null ? users.getBranch().getId() : null;
            Long principle_groups_id = Long.valueOf(request.getParameter("principle_groups_id"));
            /**This Code for When User Selected( Two Dates )**/
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);

            } else {
                List<Object[]> nlist = new ArrayList<>();
                nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatus();
                for (int i = 0; i < nlist.size(); i++) {
                    Object obj[] = nlist.get(i);
                    System.out.println("start Date:" + obj[0].toString());
                    System.out.println("end Date:" + obj[1].toString());
                    startDatep = LocalDate.parse(obj[0].toString());
                    endDatep = LocalDate.parse(obj[1].toString());
                }
            }

            String sql = "SELECT IFNULL(SUM(closing_amount),0), ledger_id, tranx_action FROM ledger_opening_closing_detail_tbl" +
                    " AS locdt LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE" +
                    " ledger_master_tbl.principle_groups_id=" + principle_groups_id + " AND DATE(locdt.tranx_date) BETWEEN '" + startDatep +
                    "' AND '" + endDatep + "' AND locdt.status=1 AND locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                sql += " AND locdt.branch_id=" + branchId;
            else sql += " AND locdt.branch_id IS NULL ";

            sql += " GROUP BY ledger_id";
            System.out.println("sql " + sql);
            Query query = entityManager.createNativeQuery(sql);
            list = query.getResultList();
            System.out.println("list size " + list.size());

            /*if (endDate != null) {
                if (users.getBranch() != null) {
                    list = postingsRepository.findByLedgerNameTotalAmountOuletAndBranchStatusStep2(users.getOutlet().getId(), users.getBranch().getId(), true, principle_groups_id, startDatep, endDatep);
                } else {
                    list = postingsRepository.findByLedgerNameTotalAmountOuletAndStatusStep2(users.getOutlet().getId(), true, principle_groups_id, startDatep, endDatep);
                }
            } else {
                if (users.getBranch() != null) {
                    list = postingsRepository.findByLedgerNameTotalAmountOuletAndBranchStatusStep2(users.getOutlet().getId(), users.getBranch().getId(), true, principle_groups_id, startDatep, endDatep);
                } else {
                    list = postingsRepository.findByLedgerNameTotalAmountOuletAndStatusStep2(users.getOutlet().getId(), true, principle_groups_id, startDatep, endDatep);
                }
            }*/
            JsonArray innerArr = new JsonArray();
            for (int i = 0; i < list.size(); i++) {
                JsonObject inside = new JsonObject();
                Object[] objp = list.get(i);
                credit_total = Double.parseDouble(objp[0].toString());
                ledger_master_id = Long.parseLong(objp[1].toString());
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(ledger_master_id, true);
                inside.addProperty("total_balance", credit_total);
                if (objp[2].toString().equalsIgnoreCase("DR")) {
                    inside.addProperty("debit", credit_total);
                    inside.addProperty("credit", 0.0);

                } else {
                    inside.addProperty("credit", credit_total);
                    inside.addProperty("debit", 0.0);

                }
                if (mLedger.getPrinciples() != null)
                    inside.addProperty("particular", mLedger.getLedgerName());

                else
                    inside.addProperty("particular", mLedger.getLedgerName());
                inside.addProperty("ledger_master_id", ledger_master_id);
                innerArr.add(inside);
            }
            PrincipleGroups principleGroups = principleGroupsRepository.findByIdAndStatus(principle_groups_id, true);

            if (principleGroups != null) {
                Long principleId = principleGroups.getPrinciples().getId();
                Principles principles = principleRepository.findByIdAndStatus(principleId, true);
                if (principleId != null) {
                    String principleName = principles.getPrincipleName();
                    if (principleName.equalsIgnoreCase("Fixed Assets")) {
                        res.addProperty("rules_type", "DR");
                    } else if (principleName.equalsIgnoreCase("Investments")) {
                        res.addProperty("rules_type", "DR");
                    } else if (principleName.equalsIgnoreCase("Current Assets")) {
                        res.addProperty("rules_type", "DR");
                    } else if (principleName.equalsIgnoreCase("Capital A/c")) {
                        res.addProperty("rules_type", "CR");
                    } else if (principleName.equalsIgnoreCase("Loans (Liabilities)")) {
                        res.addProperty("rules_type", "CR");
                    } else if (principleName.equalsIgnoreCase("Current Liabilities")) {
                        res.addProperty("rules_type", "CR");
                    } else if (principleName.equalsIgnoreCase("Expenses")) {
                        res.addProperty("rules_type", "CR");
                    }
                } else {
                    System.out.println("Foundation Name Not Found..!");
                }
            } else {
                System.out.println("Principle Id Not Found..!");
            }
            res.addProperty("d_start_date", startDatep.toString());
            res.addProperty("d_end_date", endDatep.toString());
            res.add("response", innerArr);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public Object getBalanceAcStep3(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        Long id = 0L;
        Double total_month_sum = 0.0;
        Double credit_total = 0.0;
        List<Object[]> list = new ArrayList<>();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            Double opening_bal = 0.0;
            Long ledger_master_id = Long.valueOf(request.getParameter("ledger_master_id"));
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Long branchId = users.getBranch() != null ? users.getBranch().getId() : null;
            //****This Code For Users Dates Selection Between Start And End Date Manually****//
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);
                /**Openig Balance For When Two Dates Selected By User **/

            } else {
                //****This Code For Load Data Default Current Year From Automatically load And Select Fiscal Year From Fiscal Year Table****//
                List<Object[]> nlist = new ArrayList<>();
                nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatus();
                for (int i = 0; i < nlist.size(); i++) {
                    Object obj[] = nlist.get(i);
                    System.out.println("start Date:" + obj[0].toString());
                    System.out.println("end Date:" + obj[1].toString());
                    startDatep = LocalDate.parse(obj[0].toString());
                    endDatep = LocalDate.parse(obj[1].toString());
                }
                //**Openig Balance for Fiscal Year**//
                if (users.getBranch() != null) {
                    opening_bal = ledgerMasterRepository.findByIdAndOutletIdAndBranchIdAndStatuslm(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id);
                } else {
                    opening_bal = ledgerMasterRepository.findByIdAndOutletIdAndStatuslm(users.getOutlet().getId(), true, ledger_master_id);
                }
            }
            if (startDatep.isAfter(endDatep)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                Double closing_bal = 0.0;
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate); //**  If You Want To Print  All Start And End Date of each month  between Fiscal Year **//
                startDatep = endMonthDate.plusDays(1);


                String sql = "SELECT IFNULL(SUM(closing_amount),0), ledger_id, tranx_action FROM ledger_opening_closing_detail_tbl" +
                        " AS locdt LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE" +
                        " ledger_id=" + ledger_master_id + " AND DATE(locdt.tranx_date) BETWEEN '" + startMonthDate +
                        "' AND '" + endMonthDate + "' AND locdt.status=1 AND locdt.outlet_id=" + users.getOutlet().getId();
                if (branchId != null)
                    sql += " AND locdt.branch_id=" + branchId;
                else sql += " AND locdt.branch_id IS NULL ";

                sql += " GROUP BY ledger_id";
                System.out.println("sql " + sql);
                Query query = entityManager.createNativeQuery(sql);
                list = query.getResultList();
                System.out.println("list size " + list.size());

               /* if (endDate != null) {
                    *//****This Code For Users Dates Selection Between Start And End Date Manually****//*
                    if (users.getBranch() != null) {
                        list = postingsRepository.findByTotalAmountByMonthStartDateAndEndDateAndBranchAndOutletAndStatus3(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
                    } else {
                        list = postingsRepository.findByTotalAmountByMonthStartDateAndEndDateAndOutletAndStatus3(users.getOutlet().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
                    }
                } else {
                    *//****This Code For Load Data Default Current Year From Automatically load And Select Fiscal Year From Fiscal Year Table****//*
                    try {
                        if (users.getBranch() != null) {
                            list = postingsRepository.findByTotalAmountByMonthStartDateAndEndDateAndBranchAndOutletAndStatus3(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
                        } else {
                            list = postingsRepository.findByTotalAmountByMonthStartDateAndEndDateAndOutletAndStatus3(users.getOutlet().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Exception:" + e.getMessage());
                    }
                }*/
                JsonObject inside = new JsonObject();
                for (int i = 0; i < list.size(); i++) {
                    Object[] objp = list.get(i);
                    credit_total = Double.parseDouble(objp[0].toString());
////                principle_id = Long.parseLong(objp[2].toString());
                    if (objp[1] != null)
                        ledger_master_id = Long.parseLong(objp[1].toString());
                    LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(ledger_master_id, true);
                    inside.addProperty("total_balance", credit_total);
                    if (objp[2] != null) {
                        if (objp[2].toString().equalsIgnoreCase("DR")) {
                            inside.addProperty("debit", credit_total);
                            inside.addProperty("credit", 0.0);

                        } else {
                            inside.addProperty("credit", credit_total);
                            inside.addProperty("debit", 0.0);
                        }
                    } else {
                        inside.addProperty("debit", 0.0);
                        inside.addProperty("credit", 0.0);
                    }
//                inside.addProperty("total_balance", closing_bal);
//                inside.addProperty("debit", closing_bal > 0 ? 0 : closing_bal);
//                inside.addProperty("credit", closing_bal > 0 ? closing_bal : 0);
//                    for (int i = 0; i < list.size(); i++) {
//                        Object[] objp = list.get(i);
//                        closing_bal = Double.parseDouble(objp[0].toString());
//
//                        if (objp[1].toString().equalsIgnoreCase("DR")) {
//                            inside.addProperty("debit", closing_bal);
//                            inside.addProperty("credit", 0.0);
//
//                        } else {
//                            inside.addProperty("credit", closing_bal);
//                            inside.addProperty("debit", 0.0);
//
//                        }
                    inside.addProperty("month_name", month);
                    inside.addProperty("start_date", startMonthDate.toString());
                    inside.addProperty("end_date", endMonthDate.toString());
                    innerArr.add(inside);
                }


//                if (endDate != null) {
//                    //****This Code For Users Dates Selection Between Start And End Date Manually****//
//                    if (users.getBranch() != null) {
//                        closing_bal = postingsRepository.findByTotalAmountByMonthStartDateAndEndDateAndBranchAndOutletAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
//                    } else {
//                        closing_bal = postingsRepository.findByTotalAmountByMonthStartDateAndEndDateAndOutletAndStatus(users.getOutlet().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
//                    }
//                } else {
//                    //****This Code For Load Data Default Current Year From Automatically load And Select Fiscal Year From Fiscal Year Table****//
//                    if (users.getBranch() != null) {
//                        closing_bal = postingsRepository.findByTotalAmountByMonthStartDateAndEndDateAndBranchAndOutletAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
//                    } else {
//                        closing_bal = postingsRepository.findByTotalAmountByMonthStartDateAndEndDateAndOutletAndStatus(users.getOutlet().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
//                    }
//                }
//                JsonObject inside = new JsonObject();
//                inside.addProperty("total_balance", closing_bal);
//                inside.addProperty("debit", closing_bal > 0 ? 0 : closing_bal);
//                inside.addProperty("credit", closing_bal > 0 ? closing_bal : 0);
//                inside.addProperty("month_name", month);
//                inside.addProperty("start_date", startMonthDate.toString());
//                inside.addProperty("end_date", endMonthDate.toString());
//                innerArr.add(inside);
            }
            res.addProperty("d_start_date", startDatep.toString());
            res.addProperty("d_end_date", endDatep.toString());
            res.add("response", innerArr);
            res.addProperty("opening_bal", opening_bal);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public Object getbalanceacstep4(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<LedgerOpeningClosingDetail> mlist = new ArrayList<>();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Long ledger_master_id = Long.valueOf(request.getParameter("ledger_master_id"));
            LocalDate startDate = LocalDate.parse(request.getParameter("start_date"));
            LocalDate endDate = LocalDate.parse(request.getParameter("end_date"));
            Long branchId = null;
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
            }

            String sql = "SELECT * FROM ledger_opening_closing_detail_tbl" +
                    " AS locdt LEFT JOIN ledger_master_tbl ON locdt.ledger_id=ledger_master_tbl.id WHERE" +
                    " ledger_id=" + ledger_master_id + " AND DATE(locdt.tranx_date) BETWEEN '" + startDate +
                    "' AND '" + endDate + "' AND locdt.status=1 AND locdt.outlet_id=" + users.getOutlet().getId();
            if (branchId != null)
                sql += " AND locdt.branch_id=" + branchId;
            else sql += " AND locdt.branch_id IS NULL ";

            System.out.println("sql " + sql);
            Query query = entityManager.createNativeQuery(sql, LedgerOpeningClosingDetail.class);
            mlist = query.getResultList();
            System.out.println("mlist size " + mlist.size());

            /*mlist = postingsRepository.findByIdAndOutletIdAndBranchAndStatusBalanceStep4(users.getOutlet().getId(), branchId, true, ledger_master_id, startDate, endDate);
            System.out.println("mlist " + mlist.size());*/
            JsonArray innerArr = new JsonArray();
            for (LedgerOpeningClosingDetail ledgerTransactionPostings : mlist) {
                JsonObject inside = new JsonObject();
                inside.addProperty("transaction_date", DateConvertUtil.convertDateToLocalDate(ledgerTransactionPostings.getTranxDate()).toString());
                inside.addProperty("invoice_no", ledgerTransactionPostings.getTranxId());
                inside.addProperty("invoice_id", ledgerTransactionPostings.getTranxId());// Invoice Id : 1 or 2
                Long tranx_type = ledgerTransactionPostings.getTranxTypeId(); // Transactions Id : 1:Pur 3: Sales
                if (tranx_type == 1) {
                    TranxPurInvoice tranxPurInvoice;
                    if (users.getBranch() != null) {
                        tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxPurInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurInvoice.getId());
                    } else {
                        tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxPurInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurInvoice.getId());
                    }
                } else if (tranx_type == 2) {
                    TranxPurReturnInvoice tranxPurReturnInvoice;
                    if (users.getBranch() != null) {
                        tranxPurReturnInvoice = tranxPurReturnsRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxPurReturnInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurReturnInvoice.getId());
                    } else {
                        tranxPurReturnInvoice = tranxPurReturnsRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxPurReturnInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurReturnInvoice.getId());
                    }
                } else if (tranx_type == 3) {
                    TranxSalesInvoice tranxSalesInvoice;
                    if (users.getBranch() != null) {
                        tranxSalesInvoice = tranxSalesInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxSalesInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesInvoice.getId());
                    } else {
                        tranxSalesInvoice = tranxSalesInvoiceRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxSalesInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesInvoice.getId());
                    }
                } else if (tranx_type == 4) {
                    TranxSalesReturnInvoice tranxSalesReturnInvoice;
                    if (users.getBranch() != null) {
                        tranxSalesReturnInvoice = tranxSalesReturnRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxSalesReturnInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesReturnInvoice.getId());
                    } else {
                        tranxSalesReturnInvoice = tranxSalesReturnRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxSalesReturnInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesReturnInvoice.getId());
                    }
                } else if (tranx_type == 5) {
                    TranxReceiptPerticularsDetails tranxReceiptPerticularsDetails;
                    if (users.getBranch() != null) {
                        tranxReceiptPerticularsDetails = tranxReceiptPerticularsDetailsRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxReceiptPerticularsDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxReceiptPerticularsDetails.getId());
                    } else {
                        tranxReceiptPerticularsDetails = tranxReceiptPerticularsDetailsRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxReceiptPerticularsDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxReceiptPerticularsDetails.getId());
                    }
                } else if (tranx_type == 6) {
                    TranxPaymentPerticularsDetails tranxPaymentPerticulars;
                    if (users.getBranch() != null) {
                        tranxPaymentPerticulars = tranxPaymentPerticularsDetailsRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxPaymentPerticulars.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxPaymentPerticulars.getId());
                    } else {
                        tranxPaymentPerticulars = tranxPaymentPerticularsDetailsRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxPaymentPerticulars.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxPaymentPerticulars.getId());
                    }
                } else if (tranx_type == 7) {
                    TranxDebitNoteDetails tranxDebitNoteDetails;
                    if (users.getBranch() != null) {
                        tranxDebitNoteDetails = tranxDebitNoteDetailsRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxDebitNoteDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxDebitNoteDetails.getId());
                    } else {
                        tranxDebitNoteDetails = tranxDebitNoteDetailsRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxDebitNoteDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxDebitNoteDetails.getId());
                    }
                } else if (tranx_type == 8) {
                    TranxCreditNoteDetails tranxCreditNoteDetails = null;
                    if (users.getBranch() != null) {
                        tranxCreditNoteDetails = tranxCreditNoteDetailsRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                                tranxCreditNoteDetails.getLedgerMasterId(), true);

                        inside.addProperty("particulars", mLedger != null ? mLedger.getLedgerName() : "");
                        inside.addProperty("id", tranxCreditNoteDetails.getId());
                    } else {
                        tranxCreditNoteDetails = tranxCreditNoteDetailsRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                                tranxCreditNoteDetails.getLedgerMasterId(), true);
                        inside.addProperty("particulars", mLedger != null ? mLedger.getLedgerName() : "");
                        inside.addProperty("id", tranxCreditNoteDetails.getId());
                    }
                } else if (tranx_type == 9) {
                    TranxContraDetails tranxContraDetails;
                    if (users.getBranch() != null) {
                        tranxContraDetails = tranxContraDetailsRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxContraDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxContraDetails.getId());
                    } else {
                        tranxContraDetails = tranxContraDetailsRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxContraDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxContraDetails.getId());
                    }
                } else if (tranx_type == 10) {
                    TranxJournalDetails tranxJournalDetails;
                    if (users.getBranch() != null) {
                        tranxJournalDetails = tranxJournalDetailsRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxJournalDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxJournalDetails.getId());
                    } else {
                        tranxJournalDetails = tranxJournalDetailsRepository.findByIdAndOutletIdAndStatus(ledgerTransactionPostings.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxJournalDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxJournalDetails.getId());
                    }
                }
                TransactionTypeMaster typeMaster = transactionTypeMasterRepository.findById(ledgerTransactionPostings.getTranxTypeId()).get();
                inside.addProperty("voucher_type", typeMaster.getTransactionName());
                if (ledgerTransactionPostings.getTranxAction().equalsIgnoreCase("CR")) {
                    inside.addProperty("credit", ledgerTransactionPostings.getAmount());
                    inside.addProperty("debit", 0.00);
                } else {
                    inside.addProperty("debit", ledgerTransactionPostings.getAmount());
                    inside.addProperty("credit", 0.00);
                }
//                inside.addProperty("debit", ledgerTransactionPostings.getLedgerMaster().getDebit());
//                inside.addProperty("credit", ledgerTransactionPostings.getCredit());

                innerArr.add(inside);
            }
            res.addProperty("d_start_date", startDate.toString());
            res.addProperty("d_end_date", endDate.toString());
            res.add("response", innerArr);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }
}
