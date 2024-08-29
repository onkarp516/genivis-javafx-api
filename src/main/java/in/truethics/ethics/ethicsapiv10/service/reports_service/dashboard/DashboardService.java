package in.truethics.ethics.ethicsapiv10.service.reports_service.dashboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase.TranxPurInvoiceService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Service
public class DashboardService {
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    private final TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    private final TranxPurInvoiceRepository tranxPurInvoiceRepository;

    private static final Logger dashboard = LogManager.getLogger(DashboardService.class);


    @Autowired
    LedgerMasterRepository ledgerMasterRepository;

    @Autowired
    TranxReceiptMasterRepository tranxReceiptMasterRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;

    public DashboardService(TranxSalesInvoiceRepository tranxSalesInvoiceRepository,
                            TranxPurInvoiceRepository tranxPurInvoiceRepository) {
        this.tranxSalesInvoiceRepository = tranxSalesInvoiceRepository;
        this.tranxPurInvoiceRepository = tranxPurInvoiceRepository;
    }

    public JsonObject getSalesData(Map<String, String> request){
        JsonObject resultObject = new JsonObject();
        JsonArray salesArray = new JsonArray();
        String mode=request.get("datamode");
        switch (mode){
            case "daily":
                salesArray=getSalesDailyData();
                break;
            case "weekly":
                salesArray=getSalesWeeklyData();
                break;
            case "monthly":
                salesArray=getSalesMonthlyData();
                break;
        }

        resultObject.addProperty("message", "success");
        resultObject.addProperty("responseStatus", HttpStatus.OK.value());
        resultObject.add("result", salesArray);
        return resultObject;
    }

    public JsonArray getSalesDailyData(){
        JsonArray dailyData=new JsonArray();

        LocalDate currentDate=LocalDate.now();
        for(int i=0;i<7;i++){
            LocalDate cDate=currentDate.minusDays(i);
            Double daySaleAmt = tranxSalesInvoiceRepository.findTodayTotalAmtByStatus(cDate,true);
            JsonObject jsonObject=new JsonObject();
            jsonObject.addProperty("dayName",cDate.getDayOfWeek().name().substring(0,3));
            jsonObject.addProperty("dayValue",daySaleAmt);
            dailyData.add(jsonObject);
        }
        return dailyData;
    }
    public JsonArray getSalesWeeklyData(){
        JsonArray weeklyData=new JsonArray();
        LocalDate currentDate=LocalDate.now();
        LocalDate firstDateOfMonth=currentDate.withDayOfMonth(1);

        int daysInMonth=Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        Integer dayOfWeek = currentDate.getDayOfWeek().compareTo(DayOfWeek.MONDAY);
        LocalDate startOfWeek = currentDate.minusDays(dayOfWeek);
        int currentDayOfMonth=currentDate.get(ChronoField.DAY_OF_MONTH);


        for(int i=0;i<6;i++){

            if(firstDateOfMonth.isBefore(currentDate) || firstDateOfMonth.isEqual(currentDate)){
                LocalDate fromDate=firstDateOfMonth;
                firstDateOfMonth.plusDays(6);
                Double weeksalesAmt = tranxSalesInvoiceRepository.findTotalAmtByStatus(fromDate,firstDateOfMonth,true);
                JsonObject jsonObject=new JsonObject();
                jsonObject.addProperty("weekName","Week "+(i+1));
                jsonObject.addProperty("weekValue",weeksalesAmt);
                weeklyData.add(jsonObject);
                firstDateOfMonth.plusDays(1);
            }else{
                break;
            }
        }
        System.out.println(""+weeklyData);
        return weeklyData;
    }
    public JsonArray getSalesMonthlyData(){
        JsonArray monthlyData=new JsonArray();

        LocalDate currentDate=LocalDate.now();
        LocalDate firstDateOfMonth=currentDate.with(firstDayOfMonth());


        for(int i=0;i<3;i++){
            LocalDate cdate=currentDate;
            if(i>0)
            cdate=currentDate.minusMonths(i);

            System.out.println("currentDate=>"+cdate);
            firstDateOfMonth=cdate.with(firstDayOfMonth());
            System.out.println("firstDateOfMonth=>"+firstDateOfMonth);
            LocalDate lastDateOfMonth=firstDateOfMonth.with(lastDayOfMonth());
            System.out.println("lastDateOfMonth=>"+lastDateOfMonth);

            Double monthlySalesAmt = tranxSalesInvoiceRepository.findTotalAmtByStatus(firstDateOfMonth,lastDateOfMonth,true);
            JsonObject jsonObject=new JsonObject();
            jsonObject.addProperty("monthName",lastDateOfMonth.getMonth().name().substring(0,3));
            jsonObject.addProperty("monthValue",monthlySalesAmt);
            monthlyData.add(jsonObject);

        }

        return monthlyData;
    }


    public JsonObject getDashboardData(Map<String, String> request) {
        JsonObject resultObject = new JsonObject();
        JsonArray salesArray = new JsonArray();
        JsonArray purchaseArray = new JsonArray();
        JsonArray revenueArray = new JsonArray();
        JsonArray collenctioArray = new JsonArray();
        JsonArray receivableArray = new JsonArray();
        JsonArray expensesArray = new JsonArray();
        JsonArray assetsArray =  new JsonArray();
        JsonArray liabilitiesArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Double total_today_revenue = 0.0;
        Double total_weekly_revnue = 0.0;
        Double total_monthly_revenue = 0.0;


        try {
            /****** Getting Total Sales of Current Month ******/
//            LocalDate currentDate = LocalDate.now();
//            Users users = jwtRequestFilter.getUserDataFromToken(request.get("Authorization").substring(7));
            LocalDate endMDatep = null;
            LocalDate startMDatep = null;
//        if (!request.get("end_date").equalsIgnoreCase("") && !request.get("start_date").equalsIgnoreCase("")) {
            endMDatep = LocalDate.parse(request.get("end_date").toString());
            startMDatep = LocalDate.parse(request.get("start_date"));
            System.out.println("current Date"+startMDatep);
            /**** Getting current week ****/
            Integer dayOfWeek = startMDatep.now().getDayOfWeek().compareTo(DayOfWeek.MONDAY);
            LocalDate startOfWeek = startMDatep.now().minusDays(dayOfWeek);//26
            LocalDate endOfWeek = startMDatep.now().plusDays(DayOfWeek.SUNDAY.getValue() - dayOfWeek - 1);
            System.out.println("weekstart"+startOfWeek);
            System.out.println("weekend"+endOfWeek);
            /****Getting current month******/
            LocalDate startDatep = startMDatep.withDayOfMonth(1);
            LocalDate endDatep = startDatep.withDayOfMonth(startMDatep.lengthOfMonth());
            System.out.println("monthstart"+startDatep);
            System.out.println("monthend"+endDatep);

            JsonObject totalSalesAmtObject = new JsonObject();
            Double todaySaleAmt = tranxSalesInvoiceRepository.findTodayTotalAmtByStatus(startMDatep,true);
            Double weeksalesAmt = tranxSalesInvoiceRepository.findTotalAmtByStatus(startOfWeek,endOfWeek,true);
            Double monthsalesAmt = tranxSalesInvoiceRepository.findTotalAmtByStatus(startDatep,endDatep,true);
            totalSalesAmtObject.addProperty("today_sale_amt",todaySaleAmt);
            totalSalesAmtObject.addProperty("week_sale_amt",weeksalesAmt);
            totalSalesAmtObject.addProperty("month_sale_amt",monthsalesAmt);


            ///////////////////////////// Sales Weekly Amt ////////////////////////////////////////////
            int j = 0;

            Double totalSaleWeekAmt = 0.0;

            while (startOfWeek.compareTo(endOfWeek) <= 0) {
                LocalDate mDate = startOfWeek;

                DayOfWeek dayNameWeek = mDate.getDayOfWeek();
                j = j + 1;
                JsonObject totalWeekSalesAmtObject = new JsonObject();
                Double saleAmt = tranxSalesInvoiceRepository.findSaleTotalAmtByStatus(startOfWeek, true);
                totalWeekSalesAmtObject.addProperty(String.valueOf(dayNameWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())), saleAmt);
                salesArray.add(totalWeekSalesAmtObject);
                System.out.println("SaleAmt"+saleAmt);
                totalSaleWeekAmt=totalSaleWeekAmt+saleAmt;
                startOfWeek = mDate.plusDays(1);

            }
            JsonObject weeksaletotal = new JsonObject();
//            Double purAmt = tranxPurInvoiceRepository.findPurTotalWeekAmtByStatus(startOfWeek,endOfWeek, true);
            weeksaletotal.addProperty("TotalWeekSaleAmt",totalSaleWeekAmt);
            salesArray.add(weeksaletotal);

            resultObject.add("Sales_List", salesArray);

            /********************************************* Purchase Weekly Amt *********************************************************/
            /****** Getting Total Sales of Current Month ******/
            LocalDate currentsDate = LocalDate.now();
            System.out.println("current Date"+startMDatep);
            /**** Getting current week ****/
            Integer daysOfWeek = LocalDate.now().getDayOfWeek().compareTo(DayOfWeek.MONDAY);
            LocalDate startsOfWeek = LocalDate.now().minusDays(daysOfWeek);//26
            LocalDate endsOfWeek = LocalDate.now().plusDays(DayOfWeek.SUNDAY.getValue() - daysOfWeek - 1);
            System.out.println("weekstart"+startsOfWeek);
            System.out.println("weekend"+endsOfWeek);

            int i = 0;

            Double totalPurWeekAmt = 0.0;
            while (startsOfWeek.compareTo(endsOfWeek) <= 0) {
                LocalDate mDate = startsOfWeek;
                DayOfWeek dayNameWeek = mDate.getDayOfWeek();
                i = i + 1;
                JsonObject totalPurAmtObject = new JsonObject();
                Double purAmt = tranxPurInvoiceRepository.findPurTotalAmtByStatus(startsOfWeek, true);
                totalPurAmtObject.addProperty(String.valueOf(dayNameWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())), purAmt);
                purchaseArray.add(totalPurAmtObject);
                System.out.println("PurAmt"+purAmt);
                totalPurWeekAmt=totalPurWeekAmt+purAmt;
                startsOfWeek = mDate.plusDays(1);

            }
            JsonObject weektotal = new JsonObject();
//            Double purAmt = tranxPurInvoiceRepository.findPurTotalWeekAmtByStatus(startOfWeek,endOfWeek, true);
            weektotal.addProperty("TotalWeekPurAmt",totalPurWeekAmt);
            purchaseArray.add(weektotal);
            resultObject.add("purchase_list", purchaseArray);
            List<LedgerMaster> balanceSummaries = new ArrayList<>();
            balanceSummaries = ledgerMasterRepository.findByPrinciplesIdAndStatus(9L, true);

            /*******************************Revenue***********************************/

            JsonObject revenueAmtObject = new JsonObject();
            for (LedgerMaster balanceSummary : balanceSummaries) {
                Long ledgerId = balanceSummary.getId();
                try {

                    Double todayRevenue = ledgerTransactionPostingsRepository.totalTodayRevenue(ledgerId,"CR",startMDatep,true);
                    total_today_revenue = total_today_revenue + todayRevenue;
                    Double weeklyRevenue = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"CR",startOfWeek,endOfWeek,true);
                    total_weekly_revnue = total_weekly_revnue + weeklyRevenue;
                    Double monthRevenue = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"CR",startDatep,endDatep,true);
                    total_monthly_revenue = total_monthly_revenue + monthRevenue;
                    revenueAmtObject.addProperty("today_revenue_amt",todayRevenue);
                    revenueAmtObject.addProperty("weekly_revenue_amt",weeklyRevenue);
                    revenueAmtObject.addProperty("monthly_revenue_amt",monthRevenue);
                    revenueArray.add(revenueAmtObject);
                    resultObject.add("RevenueList", revenueArray);

                } catch (Exception e) {
                    dashboard.error("Error in salesDelete()->" + e.getMessage());
                }
            }
            /**********Collenction ************/
            JsonObject collectionRevenue = new JsonObject();
            Double todayReceipAmt = tranxReceiptMasterRepository.findTotalAmt(startMDatep,true);
            Double weeklyReceipAmt = tranxReceiptMasterRepository.findWeeklyTotalAmt(startOfWeek,endOfWeek,true);
            Double monthReceipAmt = tranxReceiptMasterRepository.findWeeklyTotalAmt(startDatep,endDatep,true);
            collectionRevenue.addProperty("today_receipt_amt",todayReceipAmt);
            collectionRevenue.addProperty("weekly_receipt_amt",weeklyReceipAmt);
            collectionRevenue.addProperty("month_receipt_amt",monthReceipAmt);
            collenctioArray.add(collectionRevenue);
            resultObject.add("CollectAmt", collenctioArray);

            /*****************Receivable******************/
            JsonObject receivableObject = new JsonObject();
            receivableObject.addProperty("today_receivable_amt",todaySaleAmt);
            receivableObject.addProperty("weekly_receivable_amt",weeksalesAmt);
            receivableObject.addProperty("monthly_receivable_amt",monthsalesAmt);
            receivableArray.add(receivableObject);
            resultObject.add("ReceivableAmt", receivableArray);

            /*********************Expensive**************************************/
            Double total_today_expensive=0.0;
            Double total_weekly_expensive=0.0;
            Double total_month_expensive=0.0;
            Double today_indExp_expensive=0.0;
            Double weekly_indExp_expensive=0.0;
            Double month_indExp_expensive=0.0;
            Double today_dirExp_expensive=0.0;
            Double weekly_dirExp_expensive=0.0;
            Double month_dirExp_expensive=0.0;

            JsonObject expenses = new JsonObject();

            List<LedgerMaster> ledgerMasterList = new ArrayList<>();
            ledgerMasterList = ledgerMasterRepository.findByPrinciplesIdAndStatus(12L, true);
            for (LedgerMaster balanceSummary : ledgerMasterList) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_indExp_expensive = ledgerTransactionPostingsRepository.totalTodayRevenue(ledgerId,"DR",startMDatep,true);
                    weekly_indExp_expensive = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"DR",startOfWeek,endOfWeek,true);
                    month_indExp_expensive = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"DR",startDatep,endDatep,true);


                } catch (Exception e) {
                    dashboard.error("Error in salesDelete()->" + e.getMessage());
                }
            }

            List<LedgerMaster> ledgerMasterList1 = new ArrayList<>();
            ledgerMasterList1 = ledgerMasterRepository.findByPrinciplesIdAndStatus(11L, true);
            for (LedgerMaster balanceSummary : ledgerMasterList1) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_dirExp_expensive = ledgerTransactionPostingsRepository.totalTodayRevenue(ledgerId,"DR",startMDatep,true);
                    weekly_dirExp_expensive = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"DR",startOfWeek,endOfWeek,true);
                    month_dirExp_expensive = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"DR",startDatep,endDatep,true);


                } catch (Exception e) {
                    dashboard.error("Error in expensive()->" + e.getMessage());
                }
            }

            total_today_expensive = today_indExp_expensive + today_dirExp_expensive;
            total_weekly_expensive=weekly_dirExp_expensive + weekly_indExp_expensive;
            total_month_expensive = month_dirExp_expensive + month_indExp_expensive;
            expenses.addProperty("today_expensive_amt",total_today_expensive);
            expenses.addProperty("weekly_expensive_amt",total_weekly_expensive);
            expenses.addProperty("monthly_expensive_amt",total_month_expensive);
            expensesArray.add(expenses);
            resultObject.add("Expenses", expensesArray);

            /******************************* Assets ****************************************/
            Double total_today_assets=0.0;
            Double total_weekly_assets=0.0;
            Double total_month_assets=0.0;
            Double today_fix_assets=0.0;
            Double weekly_fix_assets=0.0;
            Double month_fix_assets=0.0;
            Double today_curr_assets=0.0;
            Double weekly_curr_assets=0.0;
            Double month_curr_assets=0.0;

            JsonObject assets = new JsonObject();

            List<LedgerMaster> ledgerMasterAssetsList = new ArrayList<>();
            ledgerMasterAssetsList = ledgerMasterRepository.findByPrinciplesIdAndStatus(1L, true);
            for (LedgerMaster balanceSummary : ledgerMasterAssetsList) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_fix_assets = ledgerTransactionPostingsRepository.totalTodayRevenue(ledgerId,"DR",startMDatep,true);
                    weekly_fix_assets = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"DR",startOfWeek,endOfWeek,true);
                    month_fix_assets = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"DR",startDatep,endDatep,true);


                } catch (Exception e) {
                    dashboard.error("Error in salesDelete()->" + e.getMessage());
                }
            }

            List<LedgerMaster> ledgerMasterCAssetsList1 = new ArrayList<>();
            ledgerMasterCAssetsList1 = ledgerMasterRepository.findByPrinciplesIdAndStatus(3L, true);
            for (LedgerMaster balanceSummary : ledgerMasterCAssetsList1) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_curr_assets = ledgerTransactionPostingsRepository.totalTodayRevenue(ledgerId,"DR",startMDatep,true);
                    weekly_curr_assets = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"DR",startOfWeek,endOfWeek,true);
                    month_curr_assets = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"DR",startDatep,endDatep,true);


                } catch (Exception e) {
                    dashboard.error("Error in expensive()->" + e.getMessage());
                }
            }

            total_today_assets = today_fix_assets + today_curr_assets;
            total_weekly_assets=weekly_fix_assets + weekly_curr_assets;
            total_month_assets = month_fix_assets + month_curr_assets;
            assets.addProperty("today_assets_amt",total_today_assets);
            assets.addProperty("weekly_assets_amt",total_weekly_assets);
            assets.addProperty("monthly_assets_amt",total_month_assets);
            assetsArray.add(assets);
            resultObject.add("AssetsAmt", assetsArray);


            /********************************************* Liabilities ******************************************************/
            Double total_today_liabilities=0.0;
            Double total_weekly_liabilities=0.0;
            Double total_month_liabilities=0.0;
            Double today_loan_liabilities=0.0;
            Double weekly_loan_liabilities=0.0;
            Double month_loan_liabilities=0.0;
            Double today_curr_liabilities=0.0;
            Double weekly_curr_liabilities=0.0;
            Double month_curr_liabilities=0.0;

            JsonObject liabilities = new JsonObject();

            List<LedgerMaster> ledgerMasterLiabilitiesList = new ArrayList<>();
            ledgerMasterLiabilitiesList = ledgerMasterRepository.findByPrinciplesIdAndStatus(5L, true);
            for (LedgerMaster balanceSummary : ledgerMasterLiabilitiesList) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_loan_liabilities = ledgerTransactionPostingsRepository.totalTodayRevenue(ledgerId,"CR",startMDatep,true);
                    weekly_loan_liabilities = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"CR",startOfWeek,endOfWeek,true);
                    month_loan_liabilities = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"CR",startDatep,endDatep,true);


                } catch (Exception e) {
                    dashboard.error("Error in salesDelete()->" + e.getMessage());
                }
            }

            List<LedgerMaster> ledgerMasterLiabilitiesList1 = new ArrayList<>();
            ledgerMasterLiabilitiesList1 = ledgerMasterRepository.findByPrinciplesIdAndStatus(6L, true);
            for (LedgerMaster balanceSummary : ledgerMasterLiabilitiesList1) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_curr_liabilities = ledgerTransactionPostingsRepository.totalTodayRevenue(ledgerId,"CR",startMDatep,true);
                    weekly_curr_liabilities = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"CR",startOfWeek,endOfWeek,true);
                    month_curr_liabilities = ledgerTransactionPostingsRepository.totalWeeklyRevenue(ledgerId,"CR",startDatep,endDatep,true);


                } catch (Exception e) {
                    dashboard.error("Error in expensive()->" + e.getMessage());
                }
            }

            total_today_liabilities = today_loan_liabilities + today_curr_liabilities;
            total_weekly_liabilities=weekly_loan_liabilities + weekly_curr_liabilities;
            total_month_liabilities = month_loan_liabilities + month_curr_liabilities;
            liabilities.addProperty("today_liabilities_amt",total_today_liabilities);
            liabilities.addProperty("weekly_liabilities_amt",total_weekly_liabilities);
            liabilities.addProperty("monthly_liabilities_amt",total_month_liabilities);
            liabilitiesArray.add(liabilities);
            resultObject.add("LiabilitiesAmt", liabilitiesArray);

        }catch (Exception e) {
            dashboard.error("Error in expensive()->" + e.getMessage());
        }

        finalObject.addProperty("message", "success");
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("result", resultObject);
        return finalObject;
    }

    //API for Web App Dashboard
    public JsonObject getDashboardData1(HttpServletRequest request) {
        JsonObject resultObject = new JsonObject();
        JsonArray salesArray = new JsonArray();
        JsonArray purchaseArray = new JsonArray();
        JsonArray revenueArray = new JsonArray();
        JsonArray collenctioArray = new JsonArray();
        JsonArray receivableArray = new JsonArray();
        JsonArray expensesArray = new JsonArray();
        JsonArray assetsArray =  new JsonArray();
        JsonArray liabilitiesArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Double total_today_revenue = 0.0;
        Double total_weekly_revnue = 0.0;
        Double total_monthly_revenue = 0.0;


        try {
            /****** Getting Total Sales of Current Month ******/
//            LocalDate currentDate = LocalDate.now();
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            LocalDate endMDatep = LocalDate.now();
            LocalDate startMDatep = LocalDate.now();
//        if (!request.get("end_date").equalsIgnoreCase("") && !request.get("start_date").equalsIgnoreCase("")) {
//            endMDatep = LocalDate.parse(request.getParameter("end_date"));
//            startMDatep = LocalDate.parse(request.getParameter("start_date"));
            System.out.println("current Date"+startMDatep);
            /**** Getting current week ****/
            Integer dayOfWeek = startMDatep.now().getDayOfWeek().compareTo(DayOfWeek.MONDAY);
            LocalDate startOfWeek = startMDatep.now().minusDays(dayOfWeek);//26
            LocalDate endOfWeek = startMDatep.now().plusDays(DayOfWeek.SUNDAY.getValue() - dayOfWeek - 1);
            System.out.println("dayOfWeek " +dayOfWeek);
            System.out.println("weekstart "+startOfWeek);
            System.out.println("weekend "+endOfWeek);
            /****Getting current month******/
            LocalDate startDatep = startMDatep.withDayOfMonth(1);
            LocalDate endDatep = startDatep.withDayOfMonth(startMDatep.lengthOfMonth());
            System.out.println("monthstart"+startDatep);
            System.out.println("monthend"+endDatep);

            JsonObject totalSalesAmtObject = new JsonObject();
            Double todaySaleAmt = tranxSalesInvoiceRepository.findTodayTotalAmtByStatusOutlet(startMDatep,true, users.getOutlet().getId());
            Double weeksalesAmt = tranxSalesInvoiceRepository.findTotalAmtByStatusOutlet(startOfWeek,endOfWeek,true, users.getOutlet().getId() );
            Double monthsalesAmt = tranxSalesInvoiceRepository.findTotalAmtByStatusOutlet(startDatep,endDatep,true, users.getOutlet().getId());
            totalSalesAmtObject.addProperty("today_sale_amt",todaySaleAmt);
            totalSalesAmtObject.addProperty("week_sale_amt",weeksalesAmt);
            totalSalesAmtObject.addProperty("month_sale_amt",monthsalesAmt);


            ///////////////////////////// Sales Weekly Amt ////////////////////////////////////////////
            int j = 0;

            Double totalSaleWeekAmt = 0.0;
            while (startOfWeek.compareTo(endOfWeek) <= 0) {
                LocalDate mDate = startOfWeek;

                DayOfWeek dayNameWeek = mDate.getDayOfWeek();
                j = j + 1;
                JsonObject totalWeekSalesAmtObject = new JsonObject();
                Double saleAmt = tranxSalesInvoiceRepository.findSaleTotalAmtByStatusOutlet(startOfWeek, true, users.getOutlet().getId());
                totalWeekSalesAmtObject.addProperty(String.valueOf(dayNameWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())), saleAmt);
                salesArray.add(totalWeekSalesAmtObject);
                System.out.println("SaleAmt"+saleAmt);
                totalSaleWeekAmt=totalSaleWeekAmt+saleAmt;
                startOfWeek = mDate.plusDays(1);

            }
            JsonObject weeksaletotal = new JsonObject();
//            Double purAmt = tranxPurInvoiceRepository.findPurTotalWeekAmtByStatus(startOfWeek,endOfWeek, true);
            weeksaletotal.addProperty("TotalWeekSaleAmt",totalSaleWeekAmt);
            salesArray.add(weeksaletotal);

            resultObject.add("Sales_List", salesArray);

            /********************************************* Purchase Weekly Amt *********************************************************/
            /****** Getting Total Sales of Current Month ******/
            LocalDate currentsDate = LocalDate.now();
            System.out.println("current Date"+startMDatep);
            /**** Getting current week ****/
            Integer daysOfWeek = LocalDate.now().getDayOfWeek().compareTo(DayOfWeek.MONDAY);
            LocalDate startsOfWeek = LocalDate.now().minusDays(daysOfWeek);//26
            LocalDate endsOfWeek = LocalDate.now().plusDays(DayOfWeek.SUNDAY.getValue() - daysOfWeek - 1);
            System.out.println("weekstart"+startsOfWeek);
            System.out.println("weekend"+endsOfWeek);

            int i = 0;

            Double totalPurWeekAmt = 0.0;
            while (startsOfWeek.compareTo(endsOfWeek) <= 0) {
                LocalDate mDate = startsOfWeek;
                DayOfWeek dayNameWeek = mDate.getDayOfWeek();
                i = i + 1;
                JsonObject totalPurAmtObject = new JsonObject();
                Double purAmt = tranxPurInvoiceRepository.findPurTotalAmtByStatusOutlet(startsOfWeek, true, users.getOutlet().getId());
                totalPurAmtObject.addProperty(String.valueOf(dayNameWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())), purAmt);
                purchaseArray.add(totalPurAmtObject);
                System.out.println("PurAmt"+purAmt);
                totalPurWeekAmt=totalPurWeekAmt+purAmt;
                startsOfWeek = mDate.plusDays(1);

            }
            JsonObject weektotal = new JsonObject();
//            Double purAmt = tranxPurInvoiceRepository.findPurTotalWeekAmtByStatus(startOfWeek,endOfWeek, true);
            weektotal.addProperty("TotalWeekPurAmt",totalPurWeekAmt);
            purchaseArray.add(weektotal);
            resultObject.add("purchase_list", purchaseArray);
            List<LedgerMaster> balanceSummaries = new ArrayList<>();
            balanceSummaries = ledgerMasterRepository.findByPrinciplesIdAndStatus(9L, true);

            /*******************************Revenue***********************************/
            JsonObject revenueAmtObject = new JsonObject();
            for (LedgerMaster balanceSummary : balanceSummaries) {
                Long ledgerId = balanceSummary.getId();
                try {

                    Double todayRevenue = ledgerTransactionPostingsRepository.totalTodayRevenueOutlet(ledgerId,"CR",startMDatep,true, users.getOutlet().getId() );
                    total_today_revenue = total_today_revenue + todayRevenue;
                    Double weeklyRevenue = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"CR",startOfWeek,endOfWeek,true, users.getOutlet().getId());
                    total_weekly_revnue = total_weekly_revnue + weeklyRevenue;
                    Double monthRevenue = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"CR",startDatep,endDatep,true, users.getOutlet().getId());
                    total_monthly_revenue = total_monthly_revenue + monthRevenue;


                } catch (Exception e) {
                    dashboard.error("Error in salesDelete()->" + e.getMessage());
                }
            }

            revenueAmtObject.addProperty("today_revenue_amt",total_today_revenue);
            revenueAmtObject.addProperty("weekly_revenue_amt",total_weekly_revnue);
            revenueAmtObject.addProperty("monthly_revenue_amt",total_monthly_revenue);
            revenueArray.add(revenueAmtObject);
            resultObject.add("RevenueList", revenueArray);

            /**********Collenction ************/
            JsonObject collectionRevenue = new JsonObject();
            Double todayReceipAmt = tranxReceiptMasterRepository.findTotalAmtOutlet(startMDatep,true, users.getOutlet().getId());
            Double weeklyReceipAmt = tranxReceiptMasterRepository.findWeeklyTotalAmtOutlet(startOfWeek,endOfWeek,true, users.getOutlet().getId());
            Double monthReceipAmt = tranxReceiptMasterRepository.findWeeklyTotalAmtOutlet(startDatep,endDatep,true, users.getOutlet().getId());
            collectionRevenue.addProperty("today_receipt_amt",todayReceipAmt);
            collectionRevenue.addProperty("weekly_receipt_amt",weeklyReceipAmt);
            collectionRevenue.addProperty("month_receipt_amt",monthReceipAmt);
            collenctioArray.add(collectionRevenue);
            resultObject.add("CollectAmt", collenctioArray);

            /*****************Receivable******************/
            JsonObject receivableObject = new JsonObject();
            receivableObject.addProperty("today_receivable_amt",todaySaleAmt);
            receivableObject.addProperty("weekly_receivable_amt",weeksalesAmt);
            receivableObject.addProperty("monthly_receivable_amt",monthsalesAmt);
            receivableArray.add(receivableObject);
            resultObject.add("ReceivableAmt", receivableArray);

            /*********************Expensive**************************************/
            Double total_today_expensive=0.0;
            Double total_weekly_expensive=0.0;
            Double total_month_expensive=0.0;
            Double today_indExp_expensive=0.0;
            Double weekly_indExp_expensive=0.0;
            Double month_indExp_expensive=0.0;
            Double today_dirExp_expensive=0.0;
            Double weekly_dirExp_expensive=0.0;
            Double month_dirExp_expensive=0.0;

            JsonObject expenses = new JsonObject();

            List<LedgerMaster> ledgerMasterList = new ArrayList<>();
            ledgerMasterList = ledgerMasterRepository.findByPrinciplesIdAndStatus(12L, true);
            for (LedgerMaster balanceSummary : ledgerMasterList) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_indExp_expensive = ledgerTransactionPostingsRepository.totalTodayRevenueOutlet(ledgerId,"DR",startMDatep,true, users.getOutlet().getId());
                    weekly_indExp_expensive = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"DR",startOfWeek,endOfWeek,true, users.getOutlet().getId());
                    month_indExp_expensive = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"DR",startDatep,endDatep,true, users.getOutlet().getId());


                } catch (Exception e) {
                    dashboard.error("Error in salesDelete()->" + e.getMessage());
                }
            }

            List<LedgerMaster> ledgerMasterList1 = new ArrayList<>();
            ledgerMasterList1 = ledgerMasterRepository.findByPrinciplesIdAndStatus(11L, true);
            for (LedgerMaster balanceSummary : ledgerMasterList1) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_dirExp_expensive = ledgerTransactionPostingsRepository.totalTodayRevenueOutlet(ledgerId,"DR",startMDatep,true, users.getOutlet().getId());
                    weekly_dirExp_expensive = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"DR",startOfWeek,endOfWeek,true, users.getOutlet().getId());
                    month_dirExp_expensive = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"DR",startDatep,endDatep,true, users.getOutlet().getId());


                } catch (Exception e) {
                    dashboard.error("Error in expensive()->" + e.getMessage());
                }
            }

            total_today_expensive = today_indExp_expensive + today_dirExp_expensive;
            total_weekly_expensive=weekly_dirExp_expensive + weekly_indExp_expensive;
            total_month_expensive = month_dirExp_expensive + month_indExp_expensive;
            expenses.addProperty("today_expensive_amt",total_today_expensive);
            expenses.addProperty("weekly_expensive_amt",total_weekly_expensive);
            expenses.addProperty("monthly_expensive_amt",total_month_expensive);
            expensesArray.add(expenses);
            resultObject.add("Expenses", expensesArray);

            /******************************* Assets ****************************************/
            Double total_today_assets=0.0;
            Double total_weekly_assets=0.0;
            Double total_month_assets=0.0;
            Double today_fix_assets=0.0;
            Double weekly_fix_assets=0.0;
            Double month_fix_assets=0.0;
            Double today_curr_assets=0.0;
            Double weekly_curr_assets=0.0;
            Double month_curr_assets=0.0;

            JsonObject assets = new JsonObject();

            List<LedgerMaster> ledgerMasterAssetsList = new ArrayList<>();
            ledgerMasterAssetsList = ledgerMasterRepository.findByPrinciplesIdAndStatus(1L, true);
            for (LedgerMaster balanceSummary : ledgerMasterAssetsList) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_fix_assets = ledgerTransactionPostingsRepository.totalTodayRevenueOutlet(ledgerId,"DR",startMDatep,true, users.getOutlet().getId());
                    weekly_fix_assets = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"DR",startOfWeek,endOfWeek,true, users.getOutlet().getId());
                    month_fix_assets = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"DR",startDatep,endDatep,true, users.getOutlet().getId());


                } catch (Exception e) {
                    dashboard.error("Error in salesDelete()->" + e.getMessage());
                }
            }

            List<LedgerMaster> ledgerMasterCAssetsList1 = new ArrayList<>();
            ledgerMasterCAssetsList1 = ledgerMasterRepository.findByPrinciplesIdAndStatus(3L, true);
            for (LedgerMaster balanceSummary : ledgerMasterCAssetsList1) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_curr_assets = ledgerTransactionPostingsRepository.totalTodayRevenueOutlet(ledgerId,"DR",startMDatep,true, users.getOutlet().getId());
                    weekly_curr_assets = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"DR",startOfWeek,endOfWeek,true, users.getOutlet().getId());
                    month_curr_assets = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"DR",startDatep,endDatep,true, users.getOutlet().getId());


                } catch (Exception e) {
                    dashboard.error("Error in expensive()->" + e.getMessage());
                }
            }

            total_today_assets = today_fix_assets + today_curr_assets;
            total_weekly_assets=weekly_fix_assets + weekly_curr_assets;
            total_month_assets = month_fix_assets + month_curr_assets;
            assets.addProperty("today_assets_amt",total_today_assets);
            assets.addProperty("weekly_assets_amt",total_weekly_assets);
            assets.addProperty("monthly_assets_amt",total_month_assets);
            assetsArray.add(assets);
            resultObject.add("AssetsAmt", assetsArray);


            /********************************************* Liabilities ******************************************************/
            Double total_today_liabilities=0.0;
            Double total_weekly_liabilities=0.0;
            Double total_month_liabilities=0.0;
            Double today_loan_liabilities=0.0;
            Double weekly_loan_liabilities=0.0;
            Double month_loan_liabilities=0.0;
            Double today_curr_liabilities=0.0;
            Double weekly_curr_liabilities=0.0;
            Double month_curr_liabilities=0.0;

            JsonObject liabilities = new JsonObject();

            List<LedgerMaster> ledgerMasterLiabilitiesList = new ArrayList<>();
            ledgerMasterLiabilitiesList = ledgerMasterRepository.findByPrinciplesIdAndStatus(5L, true);
            for (LedgerMaster balanceSummary : ledgerMasterLiabilitiesList) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_loan_liabilities = ledgerTransactionPostingsRepository.totalTodayRevenueOutlet(ledgerId,"CR",startMDatep,true, users.getOutlet().getId());
                    weekly_loan_liabilities = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"CR",startOfWeek,endOfWeek,true, users.getOutlet().getId());
                    month_loan_liabilities = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"CR",startDatep,endDatep,true, users.getOutlet().getId());


                } catch (Exception e) {
                    dashboard.error("Error in salesDelete()->" + e.getMessage());
                }
            }

            List<LedgerMaster> ledgerMasterLiabilitiesList1 = new ArrayList<>();
            ledgerMasterLiabilitiesList1 = ledgerMasterRepository.findByPrinciplesIdAndStatus(6L, true);
            for (LedgerMaster balanceSummary : ledgerMasterLiabilitiesList1) {
                Long ledgerId = balanceSummary.getId();
                try {

                    today_curr_liabilities = ledgerTransactionPostingsRepository.totalTodayRevenueOutlet(ledgerId,"CR",startMDatep,true, users.getOutlet().getId());
                    weekly_curr_liabilities = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"CR",startOfWeek,endOfWeek,true, users.getOutlet().getId());
                    month_curr_liabilities = ledgerTransactionPostingsRepository.totalWeeklyRevenueOutlet(ledgerId,"CR",startDatep,endDatep,true, users.getOutlet().getId());


                } catch (Exception e) {
                    dashboard.error("Error in expensive()->" + e.getMessage());
                }
            }

            total_today_liabilities = today_loan_liabilities + today_curr_liabilities;
            total_weekly_liabilities=weekly_loan_liabilities + weekly_curr_liabilities;
            total_month_liabilities = month_loan_liabilities + month_curr_liabilities;
            liabilities.addProperty("today_liabilities_amt",total_today_liabilities);
            liabilities.addProperty("weekly_liabilities_amt",total_weekly_liabilities);
            liabilities.addProperty("monthly_liabilities_amt",total_month_liabilities);
            liabilitiesArray.add(liabilities);
            resultObject.add("LiabilitiesAmt", liabilitiesArray);

        }catch (Exception e) {
            dashboard.error("Error in expensive()->" + e.getMessage());
        }


        finalObject.addProperty("message", "success");
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("result", resultObject);
        return finalObject;
    }


}
