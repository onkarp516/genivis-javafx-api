package in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CashBankBookReportService {
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private FiscalYearRepository fiscalYearRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;

    public Object getCashBookTransactionDetails(HttpServletRequest request) {
        JsonObject finalRes = new JsonObject();
        JsonArray particular = new JsonArray();
        JsonObject particularObject = new JsonObject();
        JsonArray bankParticular = new JsonArray();
        JsonObject bankparticularObject = new JsonObject();

        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            Double opening_balance = 0.0, cash_account = 0.0, bank_account = 0.0, closing_balance = 0.0, bank_account_dr = 0.0, bank_account_cr = 0.0;
            Double cash_dr = 0.0, cash_cr = 0.0, bank_cr = 0.0, bank_dr = 0.0, le_bank_cr = 0.0, le_bank_dr = 0.0;
            Double opening_balance_dr = 0.0, opening_balance_cr = 0.0, total_opening_dr = 0.0, total_opening_cr = 0.0, total_opening_bank_dr = 0.0, total_opening_bank_cr = 0.0, opening_balance_bank = 0.0;
            Boolean flag = false;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                startDatep = LocalDate.parse(request.getParameter("start_date"));
                endDatep = LocalDate.parse(request.getParameter("end_date"));
                flag = true;
            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();

                }
                List<LedgerMaster> cashLedgers = ledgerMasterRepository.findByUniqueCodeAndStatus("CAIH", true);
            }
            List<LedgerMaster> cashLedgers;
            cashLedgers = ledgerMasterRepository.findByUniqueCodeAndStatus("CAIH", true);
            List<LedgerMaster> bankLedger = ledgerMasterRepository.findByUniqueCodeAndStatus("BAAC", true);
            if (users.getBranch() != null) {
                cash_dr = ledgerTransactionPostingsRepository.findCashBankBookTotal("CAIH",
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "DR");
                cash_cr = ledgerTransactionPostingsRepository.findCashBankBookTotal("CAIH",
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "CR");
                bank_dr = ledgerTransactionPostingsRepository.findCashBankBookTotal("BAAC",
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "DR");
                bank_cr = ledgerTransactionPostingsRepository.findCashBankBookTotal("BAAC",
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "CR");
            } else {
//                cash_dr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(cashLedgers.getId()), startDatep, endDatep, "DR");
//
//                cash_cr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(cashLedgers.getId()), startDatep, endDatep, "CR");
                cash_dr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("CAIH",
                        users.getOutlet().getId(), startDatep, endDatep, "DR");
                cash_cr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("CAIH",
                        users.getOutlet().getId(), startDatep, endDatep, "CR");

                bank_dr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("BAAC",
                        users.getOutlet().getId(), startDatep, endDatep, "DR");
                bank_cr = ledgerTransactionPostingsRepository.findCashBankBookTotalWithNoBR("BAAC",
                        users.getOutlet().getId(), startDatep, endDatep, "CR");
            }
//            Double closingCash = opening_balance + cash_dr - cash_cr;
//            Double closingBank = opening_balance + bank_dr - bank_cr;

            /**** Cash Account *****/
            particularObject.addProperty("particulars", "Cash Account");
            particularObject.addProperty("tranxDebit", cash_dr);
            particularObject.addProperty("tranxCredit", cash_cr);

            JsonArray multiData = new JsonArray();

            for (LedgerMaster ledgerMaster : cashLedgers) {
                JsonObject cashObject = new JsonObject();
                cashObject.addProperty("name", ledgerMaster.getLedgerName());
                cashObject.addProperty("id", ledgerMaster.getId());
                Double dr = ledgerTransactionPostingsRepository.findSumDRCR(ledgerMaster.getId(), startDatep, endDatep, "DR");
                Double cr = ledgerTransactionPostingsRepository.findSumDRCR(ledgerMaster.getId(), startDatep, endDatep, "CR");
                cashObject.addProperty("tranxDebit", Math.abs(dr));
                cashObject.addProperty("tranxCredit", Math.abs(cr));
                if (flag == false) {
                    if (ledgerMaster.getOpeningBalType().equalsIgnoreCase("DR")) {
                        opening_balance_dr = ledgerMasterRepository.findOpening(users.getOutlet().getId(), true, ledgerMaster.getId(), "DR");
                        total_opening_bank_dr = total_opening_bank_dr + opening_balance_dr;

                    }
                    else {
                        opening_balance_cr = ledgerMasterRepository.findOpening(users.getOutlet().getId(), true, ledgerMaster.getId(), "CR");
                        total_opening_bank_cr = total_opening_bank_cr + opening_balance_cr;
                    }

                } else {
                    if (ledgerMaster.getOpeningBalType().equalsIgnoreCase("DR")) {
                        opening_balance_dr = ledgerTransactionPostingsRepository.sumOfOpeningAmtWithdate(startDatep, "DR", ledgerMaster.getId(), true);
                        total_opening_dr = total_opening_dr + opening_balance_dr;
                    }else {
                        opening_balance_cr = ledgerTransactionPostingsRepository.sumOfOpeningAmtWithdate(startDatep, "CR", ledgerMaster.getId(), true);

                        total_opening_cr = total_opening_cr + opening_balance_cr;
                    }

//                    SELECT SUM(amount) FROM core_product_multilevel_db.ledger_transaction_postings_tbl where transaction_date<"2023-12-01" AND ledger_type="DR" AND ledger_master_id=1;
//                    SELECT SUM(amount) FROM core_product_multilevel_db.ledger_transaction_postings_tbl where transaction_date<"2023-12-01" AND ledger_type="CR" AND ledger_master_id=1;

                }
                opening_balance = opening_balance_dr - opening_balance_cr;

                if (opening_balance > 0) {
                    cashObject.addProperty("opnDebit", opening_balance);
                    cashObject.addProperty("opnCredit", 0);


                } else {
                    cashObject.addProperty("opnCredit", opening_balance);
                    cashObject.addProperty("opnDebit", 0);

                }
                closing_balance = opening_balance + dr - cr;
                if (closing_balance > 0) {
                    cashObject.addProperty("cloDebit", Math.abs(closing_balance));
                    cashObject.addProperty("cloCredit", 0);
                } else {
                    cashObject.addProperty("cloCredit", Math.abs(closing_balance));
                    cashObject.addProperty("cloDebit", 0);
                }


                multiData.add(cashObject);


            }
            particularObject.addProperty("opnDebit", total_opening_dr);
            particularObject.addProperty("opnCredit", total_opening_cr);
            Double total_opening = total_opening_dr - total_opening_cr;
            Double total_closing_amt = total_opening + cash_dr - cash_cr;
            if (total_closing_amt > 0) {
                particularObject.addProperty("cloDebit", total_closing_amt);
                particularObject.addProperty("cloCredit", 0);
            } else {
                particularObject.addProperty("cloCredit", total_closing_amt);
                particularObject.addProperty("cloDebit", 0);
            }

            particularObject.add("data", multiData);
            particular.add(particularObject);

            /**********bank Account************/
            bankparticularObject.addProperty("particulars", "Bank Account");
            bankparticularObject.addProperty("tranxDebit", bank_dr);
            bankparticularObject.addProperty("tranxCredit", bank_cr);

            JsonArray multiBankData = new JsonArray();
            for (LedgerMaster ledgerMaster : bankLedger) {
                JsonObject bankObject = new JsonObject();

                bankObject.addProperty("name", ledgerMaster.getLedgerName());
                bankObject.addProperty("id", ledgerMaster.getId());
                Double dr = ledgerTransactionPostingsRepository.findSumDRCR(ledgerMaster.getId(), startDatep, endDatep, "DR");
                Double cr = ledgerTransactionPostingsRepository.findSumDRCR(ledgerMaster.getId(), startDatep, endDatep, "CR");
                bankObject.addProperty("tranxDebit", Math.abs(dr));
                bankObject.addProperty("tranxCredit", Math.abs(cr));
                if (flag == false) {
                    if (ledgerMaster.getOpeningBalType().equalsIgnoreCase("DR")) {
                        opening_balance_dr = ledgerMasterRepository.findOpening(users.getOutlet().getId(), true, ledgerMaster.getId(), "DR");
                        total_opening_bank_dr = total_opening_bank_dr + opening_balance_dr;

                    }
                    else {
                        opening_balance_cr = ledgerMasterRepository.findOpening(users.getOutlet().getId(), true, ledgerMaster.getId(), "CR");
                        total_opening_bank_cr = total_opening_bank_cr + opening_balance_cr;
                    }

                } else {
                    if (ledgerMaster.getOpeningBalType().equalsIgnoreCase("DR")) {
                        opening_balance_dr = ledgerTransactionPostingsRepository.sumOfOpeningAmtWithdate(startDatep, "DR", ledgerMaster.getId(), true);
                        total_opening_bank_dr = total_opening_bank_dr + opening_balance_dr;
                    }else {
                        opening_balance_cr = ledgerTransactionPostingsRepository.sumOfOpeningAmtWithdate(startDatep, "CR", ledgerMaster.getId(), true);

                        total_opening_bank_cr = total_opening_bank_cr + opening_balance_cr;
                    }

//                    SELECT SUM(amount) FROM core_product_multilevel_db.ledger_transaction_postings_tbl where transaction_date<"2023-12-01" AND ledger_type="DR" AND ledger_master_id=1;
//                    SELECT SUM(amount) FROM core_product_multilevel_db.ledger_transaction_postings_tbl where transaction_date<"2023-12-01" AND ledger_type="CR" AND ledger_master_id=1;

                }
                opening_balance_bank = opening_balance_dr - opening_balance_cr;

                if (opening_balance_bank > 0) {
                    bankObject.addProperty("opnDebit", opening_balance_bank);
                    bankObject.addProperty("opnCredit", 0);

                } else {
                    bankObject.addProperty("opnCredit", opening_balance_bank);
                    bankObject.addProperty("opnDebit", 0);

                }
                closing_balance = opening_balance_bank + dr - cr;
                if (closing_balance > 0) {
                    bankObject.addProperty("cloDebit", Math.abs(closing_balance));
                    bankObject.addProperty("cloCredit", 0);
                } else {
                    bankObject.addProperty("cloCredit", Math.abs(closing_balance));
                    bankObject.addProperty("cloDebit", 0);
                }
                multiBankData.add(bankObject);

            }
            bankparticularObject.addProperty("opnDebit", total_opening_bank_dr);
            bankparticularObject.addProperty("opnCredit", total_opening_bank_cr);
            Double total_opening_bank = total_opening_bank_dr - total_opening_bank_cr;
            Double total_closing_amt_bank = total_opening_bank + bank_dr - bank_cr;
            if (total_closing_amt > 0) {
                bankparticularObject.addProperty("cloDebit", total_closing_amt_bank);
                bankparticularObject.addProperty("cloCredit", 0);
            } else {
                bankparticularObject.addProperty("cloCredit", total_closing_amt_bank);
                bankparticularObject.addProperty("cloDebit", 0);
            }


            bankparticularObject.add("data", multiBankData);
            bankParticular.add(bankparticularObject);
            finalRes.addProperty("message", "success");
            finalRes.addProperty("responseStatus", HttpStatus.OK.value());
            finalRes.addProperty("company_name", users.getOutlet().getCompanyName());
            finalRes.addProperty("d_start_date", startDatep.toString());
            finalRes.addProperty("d_end_date", endDatep.toString());
            finalRes.add("cashParticular", particular);
            finalRes.add("bankParticular", bankParticular);


        }catch (Exception e) {
            System.out.println(e);
        }
        return finalRes;
    }

    public Object getExpensesReports(HttpServletRequest request) {
        JsonObject dexp = new JsonObject();
        JsonObject indexp = new JsonObject();
        JsonObject finalRes = new JsonObject();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            Double opening_balance = 0.0;
            Double dexp_dr = 0.0, dexp_cr = 0.0, indexp_cr = 0.0, indexp_dr = 0.0;
            Double sumdexp_dr = 0.0, sumdexp_cr = 0.0;
            Double sumindexp_dr = 0.0, sumindexp_cr = 0.0;

            Boolean flag = false;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                startDatep = LocalDate.parse(request.getParameter("start_date"));
                endDatep = LocalDate.parse(request.getParameter("end_date"));
                flag = true;
            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
            JsonArray indexpledgersArray = new JsonArray();
            JsonArray dexpLedgersArray = new JsonArray();
            /***** find Direct Expenses Ledger from Ledger Master *******/
            List<LedgerMaster> directExpensLedgers = new ArrayList<>();
            directExpensLedgers = ledgerMasterRepository.findByPrinciplesIdAndStatus(11L, true);
            try {
                for (LedgerMaster mLedger : directExpensLedgers) {
                    JsonObject ledgerObject = new JsonObject();
                    ledgerObject.addProperty("name", mLedger.getLedgerName());
                    ledgerObject.addProperty("ledger_master_id", mLedger.getId());
                    dexp_dr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(mLedger.getId()), startDatep, endDatep, "DR");
                    sumdexp_dr = sumdexp_dr + dexp_dr;
                    dexp_cr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(mLedger.getId()), startDatep, endDatep, "CR");
                    sumdexp_cr = sumdexp_cr + dexp_cr;
                    ledgerObject.addProperty("dr", dexp_dr);
                    ledgerObject.addProperty("cr", dexp_cr);

                    dexpLedgersArray.add(ledgerObject);
                }
            } catch (Exception e) {

            }


            /***** find Indirect Expenses Ledger from Ledger Master *******/
            List<LedgerMaster> indrectExpensLedgers = new ArrayList<>();
            indrectExpensLedgers = ledgerMasterRepository.findByPrinciplesIdAndStatus(12L, true);
            try {
                for (LedgerMaster mLedger : indrectExpensLedgers) {
                    JsonObject ledgerObject = new JsonObject();
                    ledgerObject.addProperty("name", mLedger.getLedgerName());
                    ledgerObject.addProperty("ledger_master_id", mLedger.getId());
                    indexp_dr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(mLedger.getId()), startDatep, endDatep, "DR");
                    sumindexp_dr = sumindexp_dr + indexp_dr;
                    indexp_cr = ledgerTransactionPostingsRepository.findSumDRCR(Long.valueOf(mLedger.getId()), startDatep, endDatep, "CR");
                    sumindexp_cr = sumindexp_cr + indexp_cr;
                    ledgerObject.addProperty("dr", indexp_dr);
                    ledgerObject.addProperty("cr", indexp_cr);
                    indexpledgersArray.add(ledgerObject);
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            Double closingDexp = sumdexp_dr - sumdexp_cr; //DR Rule
            Double closingIdexp = sumindexp_dr - sumindexp_cr; //DR Rule
            /***** Direct Expenses Account *****/
            dexp.addProperty("particulars", "Direct Expenses");
            dexp.addProperty("direct_exp_dr", sumdexp_dr);
            dexp.addProperty("direct_exp_cr", sumdexp_cr);
            dexp.addProperty("ledger", "Cash");
            dexp.add("ledgerName", dexpLedgersArray);
            if (closingDexp > 0)
                dexp.addProperty("closingDidexp", closingDexp);
            else
                dexp.addProperty("closingDidexp", Math.abs(closingDexp));
            JsonArray dexpArray = new JsonArray();
            dexpArray.add(dexp);
            /***** Indirect Expenses Account *****/
            indexp.addProperty("particulars", "Indirect Expenses");
            indexp.addProperty("indirect_exp_dr", sumindexp_dr);
            indexp.addProperty("indirect_exp_cr", sumindexp_cr);
            indexp.add("ledgerName", indexpledgersArray);
            if (closingIdexp > 0)
                indexp.addProperty("closingIdexp", closingIdexp);
            else
                indexp.addProperty("closingIdexp", Math.abs(closingIdexp));
            JsonArray indexpArray = new JsonArray();
            indexpArray.add(indexp);
            finalRes.addProperty("message", "success");
            finalRes.addProperty("company_name", users.getOutlet().getCompanyName());
            finalRes.addProperty("d_start_date", startDatep.toString());
            finalRes.addProperty("d_end_date", endDatep.toString());

            finalRes.addProperty("responseStatus", HttpStatus.OK.value());
            finalRes.add("directExpensesParticular", dexpArray);
            finalRes.add("indirectExpensesParticular", indexpArray);

        } catch (Exception e) {

        }
        return finalRes;
    }

    public Object getExpensesReports1(HttpServletRequest request) {
        JsonObject dexp = new JsonObject();
        JsonObject indexp = new JsonObject();
        JsonObject finalRes = new JsonObject();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            Double opening_balance = 0.0, cash_account = 0.0, bank_account = 0.0, closing_balance = 0.0;
            Double dexp_dr = 0.0, dexp_cr = 0.0, indexp_cr = 0.0, indexp_dr = 0.0;


            List<String> dexpIdDr = new ArrayList<>();
            List<String> dexpIdCr = new ArrayList<>();

            List<String> idexpIdDr = new ArrayList<>();
            List<String> idexpIdCr = new ArrayList<>();

            List<Object[]> indexp_obj = new ArrayList<>();
            Boolean flag = false;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                startDatep = LocalDate.parse(request.getParameter("start_date"));
                endDatep = LocalDate.parse(request.getParameter("end_date"));
                flag = true;
            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
            if (users.getBranch() != null) {
                dexp_dr = ledgerTransactionPostingsRepository.findExpensesIdTotal(11L,
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "DR");
                dexp_cr = ledgerTransactionPostingsRepository.findExpensesIdTotal(11L,
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "CR");
                indexp_dr = ledgerTransactionPostingsRepository.findExpensesIdTotal(12L,
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "DR");
                indexp_cr = ledgerTransactionPostingsRepository.findExpensesIdTotal(12L,
                        users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, "CR");

            } else {
                dexpIdDr = ledgerTransactionPostingsRepository.findExpensesIDTotalWithNoBR(11L,
                        users.getOutlet().getId(), startDatep, endDatep, "DR");
                dexpIdCr = ledgerTransactionPostingsRepository.findExpensesIDTotalWithNoBR(11L,
                        users.getOutlet().getId(), startDatep, endDatep, "CR");
                idexpIdDr = ledgerTransactionPostingsRepository.findExpensesIDTotalWithNoBR(12L,
                        users.getOutlet().getId(), startDatep, endDatep, "DR");
                idexpIdCr = ledgerTransactionPostingsRepository.findExpensesIDTotalWithNoBR(12L,
                        users.getOutlet().getId(), startDatep, endDatep, "CR");
            }
            //Direct Expenses DR
            JsonArray indexpledgersArray = new JsonArray();
            JsonArray dexpLedgersArray = new JsonArray();
            for (String ledgerId : dexpIdDr) {
                dexp_dr = ledgerTransactionPostingsRepository.findExpensesAmtTotalWithNoBR(Long.valueOf(ledgerId));
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(Long.valueOf(ledgerId), true);
                JsonObject ledgerObject = new JsonObject();
                ledgerObject.addProperty("name", ledgerMaster.getLedgerName());
                ledgerObject.addProperty("ledger_master_id", ledgerId);
                ledgerObject.addProperty("Amt", dexp_dr);
                dexpLedgersArray.add(ledgerObject);

            }
            for (String ledgerId : dexpIdCr) {
                dexp_cr = ledgerTransactionPostingsRepository.findExpensesAmtTotalWithNoBR(Long.valueOf(ledgerId));
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(Long.valueOf(ledgerId), true);
                JsonObject ledgerObject = new JsonObject();
                ledgerObject.addProperty("name", ledgerMaster.getLedgerName());
                ledgerObject.addProperty("ledger_master_id", ledgerId);
                ledgerObject.addProperty("Amt", dexp_cr);
                dexpLedgersArray.add(ledgerObject);

            }
            for (String ledgerId : idexpIdDr) {
                indexp_dr = ledgerTransactionPostingsRepository.findExpensesAmtTotalWithNoBR(Long.valueOf(ledgerId));
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(Long.valueOf(ledgerId), true);
                JsonObject ledgerObject = new JsonObject();
                ledgerObject.addProperty("name", ledgerMaster.getLedgerName());
                ledgerObject.addProperty("ledger_master_id", ledgerId);
                ledgerObject.addProperty("Amt", indexp_dr);
                indexpledgersArray.add(ledgerObject);
            }
            for (String ledgerId : idexpIdCr) {
                indexp_cr = ledgerTransactionPostingsRepository.findExpensesAmtTotalWithNoBR(Long.valueOf(ledgerId));
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(Long.valueOf(ledgerId), true);
                JsonObject ledgerObject = new JsonObject();
                ledgerObject.addProperty("name", ledgerMaster.getLedgerName());
                ledgerObject.addProperty("ledger_master_id", ledgerId);
                ledgerObject.addProperty("Amt", indexp_cr);
                indexpledgersArray.add(ledgerObject);
            }
            Double closingDexp = opening_balance + dexp_dr - dexp_cr; //DR Rule
            Double closingIdexp = opening_balance + indexp_dr - indexp_cr; //CR Rule
            /***** Direct Expenses Account *****/
            dexp.addProperty("particulars", "Direct Expenses");
            dexp.addProperty("ledger", "Cash");

//            List<LedgerMaster> dexpLedgers = new ArrayList<>();
//            JsonArray dexpLedgersArray = new JsonArray();
//            if (users.getBranch() != null) {
//                dexpLedgers = ledgerMasterRepository.findByUniqueCodeAndBranchIdAndOutletIdAndStatus("DIEX",
//                        users.getBranch().getId(), users.getOutlet().getId(), true);
//            } else {
//                dexpLedgers = ledgerMasterRepository.findByUniqueCodeAndBranchIsNullAndOutletIdAndStatus("DIEX",
//                        users.getOutlet().getId(), true);
//            }
//            for (LedgerMaster mIndexp : dexpLedgers) {
//                JsonObject ledgerObject = new JsonObject();
//                ledgerObject.addProperty("name", mIndexp.getLedgerName());
//                ledgerObject.addProperty("ledger_master_id", mIndexp.getId());
//                dexpLedgersArray.add(ledgerObject);
//            }
            dexp.add("ledgerName", dexpLedgersArray);
            if (closingDexp > 0)
                dexp.addProperty("debit", closingDexp);
            else
                dexp.addProperty("credit", Math.abs(closingDexp));
            JsonArray dexpArray = new JsonArray();
            dexpArray.add(dexp);
            /***** Indirect Expenses Account *****/
            indexp.addProperty("particulars", "Indirect Expenses");
//            List<LedgerMaster> indexpLedgers = new ArrayList<>();
//            if (users.getBranch() != null) {
//                indexpLedgers = ledgerMasterRepository.findByUniqueCodeAndBranchIdAndOutletIdAndStatus("INEX",
//                        users.getBranch().getId(), users.getOutlet().getId(), true);
//            } else {
//                indexpLedgers = ledgerMasterRepository.findByUniqueCodeAndBranchIsNullAndOutletIdAndStatus("INEX",
//                        users.getOutlet().getId(), true);
//            }
//            JsonArray indexpledgersArray1 = new JsonArray();
//            for (LedgerMaster mIndexp : indexpLedgers) {
//                JsonObject ledgerObject = new JsonObject();
//                ledgerObject.addProperty("name", mIndexp.getLedgerName());
//                ledgerObject.addProperty("ledger_master_id", mIndexp.getId());
//                indexpledgersArray.add(ledgerObject);
//            }
            indexp.add("ledgerName", indexpledgersArray);
            if (closingIdexp > 0)
                indexp.addProperty("debit", closingIdexp);
            else
                indexp.addProperty("credit", Math.abs(closingIdexp));
            JsonArray indexpArray = new JsonArray();
            indexpArray.add(indexp);

            finalRes.addProperty("message", "success");
            finalRes.addProperty("responseStatus", HttpStatus.OK.value());
            finalRes.add("directExpensesParticular", dexpArray);
            finalRes.add("indirectExpensesParticular", indexpArray);

        } catch (Exception e) {

        }
        return finalRes;
    }

}
