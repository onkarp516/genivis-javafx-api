package in.truethics.ethics.ethicsapiv10.service.reports_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
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
public class TrialBalanceService {


    @Autowired
    JwtTokenUtil jwtRequestFilter;

    @Autowired
    FiscalYearRepository fiscalYearRepository;
    @Autowired
    private LedgerTransactionPostingsRepository postingsRepository;

    public JsonObject getAllLedgers(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        LedgerTransactionDetails ledgerTransactionDetails = null;

        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Map<String, String[]> paramMap = request.getParameterMap();
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


            if (endDate != null) {
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

                } else {
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

                } else {
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
            }
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

}
