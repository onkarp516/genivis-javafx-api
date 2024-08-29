package in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports;

import com.google.gson.JsonArray;
import org.apache.poi.ss.util.CellRangeAddress;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesOrder;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesChallanRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesOrderRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesReturnRepository;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SalesRegisterReportService {
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private FiscalYearRepository fiscalYearRepository;

    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private TranxSalesReturnRepository tranxSalesReturnRepository;
    @Autowired
    private TranxSalesOrderRepository tranxSalesOrderRepository;
    @Autowired
    private TranxSalesChallanRepository tranxSalesChallanRepository;

    private static final Logger stockLogger = LogManager.getLogger(SalesRegisterReportService.class);

    public Object getMonthwiseSalesRegisterTransactionDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);
            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
            currentStartDate = startDatep;
            currentEndDate = endDatep;
            if (startDatep.isAfter(endDatep)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject jsonObject = new JsonObject();
                Double totalInvoices = 0.0;
                Double totalInvoiceAmt = 0.0;
                Double totalReturnAmt = 0.0;
                Double closingBalance = 0.0;
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate);
                /******  If You Want To Print  All Start And End Date of each month  between Fiscal Year ******/
                startDatep = endMonthDate.plusDays(1);
                System.out.println();
                //****This Code For Users Dates Selection Between Start And End Date Manually****//
                if (users.getBranch() != null) {
                    totalInvoiceAmt = tranxSalesInvoiceRepository.findTotalInvoiceAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);
                    totalReturnAmt = tranxSalesReturnRepository.findTotalInvoiceAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);
                } else {
                    totalInvoiceAmt = tranxSalesInvoiceRepository.findTotalInvoicesAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);
                    totalReturnAmt = tranxSalesReturnRepository.findTotalInvoicesAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);
                }
                totalInvoices = totalInvoiceAmt - totalReturnAmt;
                closingBalance = totalInvoiceAmt - totalReturnAmt;
                jsonObject.addProperty("month", month);
                if(totalInvoices > 0){
                    jsonObject.addProperty("debit", totalInvoices);
                    jsonObject.addProperty("credit", 0.0);
                }
                else if(totalInvoices<0){
                    jsonObject.addProperty("debit", 0.0);
                    jsonObject.addProperty("credit", Math.abs(totalInvoices));
                }
                else{
                    {
                        jsonObject.addProperty("debit", 0.0);
                        jsonObject.addProperty("credit", 0.0);
                    }
                }

                jsonObject.addProperty("start_date", startMonthDate.toString());
                jsonObject.addProperty("end_date", endMonthDate.toString());
                if(closingBalance>0){
                    jsonObject.addProperty("closing_balance", Math.abs(closingBalance));
                }
                else{
                    jsonObject.addProperty("closing_balance", 0.0);
                }

                jsonObject.addProperty("type", closingBalance > 0 ? "DR" : "CR");
                innerArr.add(jsonObject);
            }
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.addProperty("company_name", users.getOutlet().getCompanyName());
            res.add("response", innerArr);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public Object getSalesRegisterTransactionDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();

        Map<String, String[]> paramMap = request.getParameterMap();
        LocalDate endDatep = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        String durations = null;
        List<TranxSalesInvoice> mlist = new ArrayList<>();
        List<TranxSalesReturnInvoice> mreturnList = new ArrayList<>();
        List<TranxDebitNoteNewReferenceMaster> debitNoteList = new ArrayList<>();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Long branchId = null;

//            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
//                endDate = LocalDate.parse(request.getParameter("end_date"));
////                endDatep = LocalDate.parse(endDate);
//                startDate = LocalDate.parse(request.getParameter("start_date"));
////                startDatep = LocalDate.parse(startDate);
//            } else {
//                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
//                if (fiscalYear != null) {
//                    startDatep = fiscalYear.getDateStart();
//                    endDatep = fiscalYear.getDateEnd();
//                }
//            }
//            currentStartDate = startDatep;
//            currentEndDate = endDatep;
            if(paramMap.containsKey("start_date") && paramMap.containsKey("end_date") ){
                startDate = LocalDate.parse(request.getParameter("start_date"));

                endDate = LocalDate.parse(request.getParameter("end_date"));
            }
            else if(paramMap.containsKey("duration")){
                durations = request.getParameter("duration");
                if(durations.equalsIgnoreCase("month")){
                    //for finding first and last day of current month
                    LocalDate thisMonth = LocalDate.now();
                    String fDay = thisMonth.withDayOfMonth(1).toString();
                    String lDay = thisMonth.withDayOfMonth(thisMonth.lengthOfMonth()).toString();
                    startDate=LocalDate.parse(fDay);
                    endDate= LocalDate.parse(lDay);

                }else if(durations.equalsIgnoreCase("lastMonth")){
                    //for finding first day and last day of previous month
                    Calendar aCalendar = Calendar.getInstance();
                    // add -1 month to current month
                    aCalendar.add(Calendar.MONTH, -1);
                    // set DATE to 1, so first date of previous month
                    aCalendar.set(Calendar.DATE, 1);
                    Date firstDateOfPreviousMonth = aCalendar.getTime();
                    // set actual maximum date of previous month
                    aCalendar.set(Calendar.DATE, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    //read it
                    Date lastDateOfPreviousMonth = aCalendar.getTime();

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String firstDay = df.format(firstDateOfPreviousMonth);  //here we get the first day of last month
                    String lastDay = df.format(lastDateOfPreviousMonth);    //here we get the last day of last month
                    startDate= LocalDate.parse(firstDay);
                    endDate=LocalDate.parse(lastDay);

                }else if(durations.equalsIgnoreCase("halfYear")){
                    //for finding first and second half year start day and end day
                    LocalDate currentDate = LocalDate.now();
                    //for first half-year
                    LocalDate lastYear =currentDate.minusYears(1);
                    LocalDate firstDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 1, 1);
                    LocalDate lastDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 6, 30);

                    // Second half-year
                    LocalDate firstDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 7, 1);
                    LocalDate lastDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 12, 31);

                    // Format the dates in dd-MM-yyyy format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    String firstDayFirstHalfFormatted = firstDayOfFirstHalf.format(formatter);
                    String lastDayFirstHalfFormatted = lastDayOfFirstHalf.format(formatter);

                    String firstDaySecondHalfFormatted = firstDayOfSecondHalf.format(formatter);
                    String lastDaySecondHalfFormatted = lastDayOfSecondHalf.format(formatter);
                    System.out.println("firstDayFirstHalfFormatted "+firstDayFirstHalfFormatted+" lastDayFirstHalfFormatted "+lastDayFirstHalfFormatted);
                    System.out.println("firstDaySecondHalfFormatted "+firstDaySecondHalfFormatted+"  lastDaySecondHalfFormatted "+lastDaySecondHalfFormatted);
                    startDate=LocalDate.parse(firstDaySecondHalfFormatted);
                    endDate=LocalDate.parse(lastDaySecondHalfFormatted);
                }else if(durations.equalsIgnoreCase("fullYear")){
                    List<Object[]> nlist = new ArrayList<>();
                    nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();
                    for (int i = 0; i < nlist.size(); i++) {
                        Object obj[] = nlist.get(i);
//                    System.out.println("start Date:" + obj[0].toString());
//                    System.out.println("end Date:" + obj[1].toString());
                        startDate = LocalDate.parse(obj[0].toString());
                        endDate = LocalDate.parse(obj[1].toString());
                    }
                }
            }

            if (startDate.isAfter(endDate)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }

            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                mlist = tranxSalesInvoiceRepository.findInvoices(users.getOutlet().getId(), branchId, startDate, endDate);
                mreturnList = tranxSalesReturnRepository.findInvoice(users.getOutlet().getId(), branchId, startDate, endDate);
            } else {
                mlist = tranxSalesInvoiceRepository.findInvoicesNoBr(
                        users.getOutlet().getId(), startDate, endDate);
                mreturnList = tranxSalesReturnRepository.findInvoicesNoBr(
                        users.getOutlet().getId(), startDate, endDate);
            }
            JsonArray debitArray = new JsonArray();
            for (TranxSalesInvoice mInvoice : mlist) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getBillDate().toString());
                inside.addProperty("voucher_no", mInvoice.getSalesInvoiceNo());
                inside.addProperty("voucher_type", "Sales Voucher");
                inside.addProperty("particulars", mInvoice.getSundryDebtors() != null ? mInvoice.getSundryDebtors().getLedgerName() : "");
                inside.addProperty("debit", mInvoice.getTotalAmount());
                inside.addProperty("credit", 0.0);

                debitArray.add(inside);
            }
            JsonArray creditArray = new JsonArray();
            for (TranxSalesReturnInvoice mInvoice : mreturnList) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getTransactionDate().toString());
                inside.addProperty("voucher_no", mInvoice.getSalesReturnNo());
                inside.addProperty("voucher_type", "Sales Return");
                inside.addProperty("particulars", mInvoice.getSundryDebtors() != null ? mInvoice.getSundryDebtors().getLedgerName() : "");
                inside.addProperty("credit", mInvoice.getTotalAmount());
                inside.addProperty("debit", 0.0);

//                creditArray.add(inside);
                debitArray.add(inside);
            }
            for (TranxDebitNoteNewReferenceMaster mInvoice : debitNoteList) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getTranscationDate().toString());
                inside.addProperty("voucher_no", mInvoice.getDebitnoteNewReferenceNo());
                inside.addProperty("voucher_type", "Debit Note");
                inside.addProperty("particulars", mInvoice.getSundryCreditor().getLedgerName());
                inside.addProperty("credit", mInvoice.getTotalAmount());
                inside.addProperty("debit", 0.0);

//                creditArray.add(inside);
                debitArray.add(inside);
            }
            res.addProperty("d_start_date", startDate.toString());
            res.addProperty("d_end_date", endDate.toString());
            res.addProperty("company_name", users.getOutlet().getCompanyName());
            res.add("debit", debitArray);
//            res.add("credit", creditArray);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }
    public Object getSalesOrderTransactionDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();

        Map<String, String[]> paramMap = request.getParameterMap();
        LocalDate endDatep = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        String durations = null;
        List<TranxSalesOrder> mlist = new ArrayList<>();
        List<TranxSalesReturnInvoice> mreturnList = new ArrayList<>();
        List<TranxDebitNoteNewReferenceMaster> debitNoteList = new ArrayList<>();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Long branchId = null;

            if(paramMap.containsKey("start_date") && paramMap.containsKey("end_date") ){
                startDate = LocalDate.parse(request.getParameter("start_date"));

                endDate = LocalDate.parse(request.getParameter("end_date"));
            }
            else if(paramMap.containsKey("duration")){
                durations = request.getParameter("duration");
                if(durations.equalsIgnoreCase("month")){
                    //for finding first and last day of current month
                    LocalDate thisMonth = LocalDate.now();
                    String fDay = thisMonth.withDayOfMonth(1).toString();
                    String lDay = thisMonth.withDayOfMonth(thisMonth.lengthOfMonth()).toString();
                    startDate=LocalDate.parse(fDay);
                    endDate= LocalDate.parse(lDay);

                }else if(durations.equalsIgnoreCase("lastMonth")){
                    //for finding first day and last day of previous month
                    Calendar aCalendar = Calendar.getInstance();
                    // add -1 month to current month
                    aCalendar.add(Calendar.MONTH, -1);
                    // set DATE to 1, so first date of previous month
                    aCalendar.set(Calendar.DATE, 1);
                    Date firstDateOfPreviousMonth = aCalendar.getTime();
                    // set actual maximum date of previous month
                    aCalendar.set(Calendar.DATE, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    //read it
                    Date lastDateOfPreviousMonth = aCalendar.getTime();

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String firstDay = df.format(firstDateOfPreviousMonth);  //here we get the first day of last month
                    String lastDay = df.format(lastDateOfPreviousMonth);    //here we get the last day of last month
                    startDate= LocalDate.parse(firstDay);
                    endDate=LocalDate.parse(lastDay);

                }else if(durations.equalsIgnoreCase("halfYear")){
                    //for finding first and second half year start day and end day
                    LocalDate currentDate = LocalDate.now();
                    //for first half-year
                    LocalDate lastYear =currentDate.minusYears(1);
                    LocalDate firstDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 1, 1);
                    LocalDate lastDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 6, 30);

                    // Second half-year
                    LocalDate firstDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 7, 1);
                    LocalDate lastDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 12, 31);

                    // Format the dates in dd-MM-yyyy format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    String firstDayFirstHalfFormatted = firstDayOfFirstHalf.format(formatter);
                    String lastDayFirstHalfFormatted = lastDayOfFirstHalf.format(formatter);

                    String firstDaySecondHalfFormatted = firstDayOfSecondHalf.format(formatter);
                    String lastDaySecondHalfFormatted = lastDayOfSecondHalf.format(formatter);
                    System.out.println("firstDayFirstHalfFormatted "+firstDayFirstHalfFormatted+" lastDayFirstHalfFormatted "+lastDayFirstHalfFormatted);
                    System.out.println("firstDaySecondHalfFormatted "+firstDaySecondHalfFormatted+"  lastDaySecondHalfFormatted "+lastDaySecondHalfFormatted);
                    startDate=LocalDate.parse(firstDaySecondHalfFormatted);
                    endDate=LocalDate.parse(lastDaySecondHalfFormatted);
                }else if(durations.equalsIgnoreCase("fullYear")){
                    List<Object[]> nlist = new ArrayList<>();
                    nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();
                    for (int i = 0; i < nlist.size(); i++) {
                        Object obj[] = nlist.get(i);
//                    System.out.println("start Date:" + obj[0].toString());
//                    System.out.println("end Date:" + obj[1].toString());
                        startDate = LocalDate.parse(obj[0].toString());
                        endDate = LocalDate.parse(obj[1].toString());
                    }
                }
            }

            if (startDate.isAfter(endDate)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }

            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                mlist = tranxSalesOrderRepository.findInvoices(users.getOutlet().getId(), branchId, startDate, endDate);
//                mreturnList = tranxSalesReturnRepository.findInvoice(users.getOutlet().getId(), branchId, startDate, endDate);
            } else {
                mlist = tranxSalesOrderRepository.findInvoicesNoBr(
                        users.getOutlet().getId(), startDate, endDate);
//                mreturnList = tranxSalesReturnRepository.findInvoicesNoBr(
//                        users.getOutlet().getId(), startDate, endDate);
            }
            JsonArray debitArray = new JsonArray();
            for (TranxSalesOrder mInvoice : mlist) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getBillDate().toString());
                inside.addProperty("voucher_no", mInvoice.getSalesOrderSrNo());
                inside.addProperty("voucher_type", "Sales Order");
                inside.addProperty("particulars", mInvoice.getSundryDebtors() != null ? mInvoice.getSundryDebtors().getLedgerName() : "");
                inside.addProperty("debit", mInvoice.getTotalAmount());
                inside.addProperty("credit", 0.0);

                debitArray.add(inside);
            }
//            JsonArray creditArray = new JsonArray();
//            for (TranxSalesReturnInvoice mInvoice : mreturnList) {
//                JsonObject inside = new JsonObject();
//                inside.addProperty("row_id", mInvoice.getId());
//                inside.addProperty("transaction_date", mInvoice.getTransactionDate().toString());
//                inside.addProperty("voucher_no", mInvoice.getSalesReturnNo());
//                inside.addProperty("voucher_type", "Sales Return");
//                inside.addProperty("particulars", mInvoice.getSundryDebtors() != null ? mInvoice.getSundryDebtors().getLedgerName() : "");
//                inside.addProperty("credit", mInvoice.getTotalAmount());
//                inside.addProperty("debit", 0.0);
//
////                creditArray.add(inside);
//                debitArray.add(inside);
//            }
//            for (TranxDebitNoteNewReferenceMaster mInvoice : debitNoteList) {
//                JsonObject inside = new JsonObject();
//                inside.addProperty("row_id", mInvoice.getId());
//                inside.addProperty("transaction_date", mInvoice.getTranscationDate().toString());
//                inside.addProperty("voucher_no", mInvoice.getDebitnoteNewReferenceNo());
//                inside.addProperty("voucher_type", "Debit Note");
//                inside.addProperty("particulars", mInvoice.getSundryCreditor().getLedgerName());
//                inside.addProperty("credit", mInvoice.getTotalAmount());
//                inside.addProperty("debit", 0.0);
//
////                creditArray.add(inside);
//                debitArray.add(inside);
//            }
            res.addProperty("d_start_date", startDate.toString());
            res.addProperty("d_end_date", endDate.toString());
            res.addProperty("company_name", users.getOutlet().getCompanyName());
            res.add("debit", debitArray);
//            res.add("credit", creditArray);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }
    public Object getMonthwiseSalesOrderTransactionDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);
            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
            currentStartDate = startDatep;
            currentEndDate = endDatep;
            if (startDatep.isAfter(endDatep)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject jsonObject = new JsonObject();
                Double totalInvoices = 0.0;
                Double totalInvoiceAmt = 0.0;
                Double totalReturnAmt = 0.0;
                Double closingBalance = 0.0;
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate);
                /******  If You Want To Print  All Start And End Date of each month  between Fiscal Year ******/
                startDatep = endMonthDate.plusDays(1);
                System.out.println();
                //****This Code For Users Dates Selection Between Start And End Date Manually****//
                if (users.getBranch() != null) {
                    totalInvoiceAmt = tranxSalesOrderRepository.findTotalInvoiceAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);

                } else {
                    totalInvoiceAmt = tranxSalesOrderRepository.findTotalInvoicesAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);

                }
                totalInvoices = totalInvoiceAmt - totalReturnAmt;
                closingBalance = totalInvoiceAmt - totalReturnAmt;
                jsonObject.addProperty("month", month);
                if(totalInvoices > 0){
                    jsonObject.addProperty("debit", totalInvoices);
                    jsonObject.addProperty("credit", 0.0);
                }
                else if(totalInvoices<0){
                    jsonObject.addProperty("debit", 0.0);
                    jsonObject.addProperty("credit", Math.abs(totalInvoices));
                }
                else{
                    {
                        jsonObject.addProperty("debit", 0.0);
                        jsonObject.addProperty("credit", 0.0);
                    }
                }
                closingBalance = totalInvoiceAmt - totalReturnAmt;
                jsonObject.addProperty("month", month);
                jsonObject.addProperty("start_date", startMonthDate.toString());
                jsonObject.addProperty("end_date", endMonthDate.toString());
                jsonObject.addProperty("closing_balance", Math.abs(closingBalance));
                jsonObject.addProperty("type", closingBalance > 0 ? "DR" : "CR");
                innerArr.add(jsonObject);
            }
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.addProperty("company_name", users.getOutlet().getCompanyName());
            res.add("response", innerArr);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }
    public Object getSalesChallanTransactionDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();

        Map<String, String[]> paramMap = request.getParameterMap();
        LocalDate endDatep = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        String durations = null;
        List<TranxSalesChallan> mlist = new ArrayList<>();
        List<TranxDebitNoteNewReferenceMaster> debitNoteList = new ArrayList<>();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Long branchId = null;

            if(paramMap.containsKey("start_date") && paramMap.containsKey("end_date") ){
                startDate = LocalDate.parse(request.getParameter("start_date"));

                endDate = LocalDate.parse(request.getParameter("end_date"));
            }
            else if(paramMap.containsKey("duration")){
                durations = request.getParameter("duration");
                if(durations.equalsIgnoreCase("month")){
                    //for finding first and last day of current month
                    LocalDate thisMonth = LocalDate.now();
                    String fDay = thisMonth.withDayOfMonth(1).toString();
                    String lDay = thisMonth.withDayOfMonth(thisMonth.lengthOfMonth()).toString();
                    startDate=LocalDate.parse(fDay);
                    endDate= LocalDate.parse(lDay);

                }else if(durations.equalsIgnoreCase("lastMonth")){
                    //for finding first day and last day of previous month
                    Calendar aCalendar = Calendar.getInstance();
                    // add -1 month to current month
                    aCalendar.add(Calendar.MONTH, -1);
                    // set DATE to 1, so first date of previous month
                    aCalendar.set(Calendar.DATE, 1);
                    Date firstDateOfPreviousMonth = aCalendar.getTime();
                    // set actual maximum date of previous month
                    aCalendar.set(Calendar.DATE, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    //read it
                    Date lastDateOfPreviousMonth = aCalendar.getTime();

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String firstDay = df.format(firstDateOfPreviousMonth);  //here we get the first day of last month
                    String lastDay = df.format(lastDateOfPreviousMonth);    //here we get the last day of last month
                    startDate= LocalDate.parse(firstDay);
                    endDate=LocalDate.parse(lastDay);

                }else if(durations.equalsIgnoreCase("halfYear")){
                    //for finding first and second half year start day and end day
                    LocalDate currentDate = LocalDate.now();
                    //for first half-year
                    LocalDate lastYear =currentDate.minusYears(1);
                    LocalDate firstDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 1, 1);
                    LocalDate lastDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 6, 30);

                    // Second half-year
                    LocalDate firstDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 7, 1);
                    LocalDate lastDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 12, 31);

                    // Format the dates in dd-MM-yyyy format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    String firstDayFirstHalfFormatted = firstDayOfFirstHalf.format(formatter);
                    String lastDayFirstHalfFormatted = lastDayOfFirstHalf.format(formatter);

                    String firstDaySecondHalfFormatted = firstDayOfSecondHalf.format(formatter);
                    String lastDaySecondHalfFormatted = lastDayOfSecondHalf.format(formatter);
                    System.out.println("firstDayFirstHalfFormatted "+firstDayFirstHalfFormatted+" lastDayFirstHalfFormatted "+lastDayFirstHalfFormatted);
                    System.out.println("firstDaySecondHalfFormatted "+firstDaySecondHalfFormatted+"  lastDaySecondHalfFormatted "+lastDaySecondHalfFormatted);
                    startDate=LocalDate.parse(firstDaySecondHalfFormatted);
                    endDate=LocalDate.parse(lastDaySecondHalfFormatted);
                }else if(durations.equalsIgnoreCase("fullYear")){
                    List<Object[]> nlist = new ArrayList<>();
                    nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();
                    for (int i = 0; i < nlist.size(); i++) {
                        Object obj[] = nlist.get(i);
//                    System.out.println("start Date:" + obj[0].toString());
//                    System.out.println("end Date:" + obj[1].toString());
                        startDate = LocalDate.parse(obj[0].toString());
                        endDate = LocalDate.parse(obj[1].toString());
                    }
                }
            }

            if (startDate.isAfter(endDate)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }

            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                mlist = tranxSalesChallanRepository.findInvoices(users.getOutlet().getId(), branchId, startDate, endDate);
            } else {
                mlist = tranxSalesChallanRepository.findInvoicesNoBr(
                        users.getOutlet().getId(), startDate, endDate);

            }
            JsonArray debitArray = new JsonArray();
            for (TranxSalesChallan mInvoice : mlist) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getBillDate().toString());
                inside.addProperty("voucher_no", mInvoice.getSalesChallanInvoiceNo());
                inside.addProperty("voucher_type", "Sales Challan");
                inside.addProperty("particulars", mInvoice.getSundryDebtors() != null ? mInvoice.getSundryDebtors().getLedgerName() : "");
                inside.addProperty("debit", mInvoice.getTotalAmount());
                inside.addProperty("credit", 0.0);

                debitArray.add(inside);
            }

            res.addProperty("d_start_date", startDate.toString());
            res.addProperty("d_end_date", endDate.toString());
            res.addProperty("company_name", users.getOutlet().getCompanyName());
            res.add("debit", debitArray);
//            res.add("credit", creditArray);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }
    public Object getMonthwiseSalesChallanTransactionDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);
            } else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
            currentStartDate = startDatep;
            currentEndDate = endDatep;
            if (startDatep.isAfter(endDatep)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject jsonObject = new JsonObject();
                Double totalInvoices = 0.0;
                Double totalInvoiceAmt = 0.0;
                Double totalReturnAmt = 0.0;
                Double closingBalance = 0.0;
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate);
                /******  If You Want To Print  All Start And End Date of each month  between Fiscal Year ******/
                startDatep = endMonthDate.plusDays(1);
                System.out.println();
                //****This Code For Users Dates Selection Between Start And End Date Manually****//
                if (users.getBranch() != null) {
                    totalInvoiceAmt = tranxSalesChallanRepository.findTotalInvoiceAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);

                } else {
                    totalInvoiceAmt = tranxSalesChallanRepository.findTotalInvoicesAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);

                }

                totalInvoices = totalInvoiceAmt - totalReturnAmt;
                closingBalance = totalInvoiceAmt - totalReturnAmt;
                jsonObject.addProperty("month", month);
                if(totalInvoices > 0){
                    jsonObject.addProperty("debit", totalInvoices);
                    jsonObject.addProperty("credit", 0.0);
                }
                else if(totalInvoices<0){
                    jsonObject.addProperty("debit", 0.0);
                    jsonObject.addProperty("credit", Math.abs(totalInvoices));
                }
                else{
                    {
                        jsonObject.addProperty("debit", 0.0);
                        jsonObject.addProperty("credit", 0.0);
                    }
                }
                closingBalance = totalInvoiceAmt - totalReturnAmt;
                jsonObject.addProperty("month", month);
                jsonObject.addProperty("start_date", startMonthDate.toString());
                jsonObject.addProperty("end_date", endMonthDate.toString());
                jsonObject.addProperty("closing_balance", Math.abs(closingBalance));
                jsonObject.addProperty("type", closingBalance > 0 ? "DR" : "CR");
                innerArr.add(jsonObject);
            }
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.addProperty("company_name", users.getOutlet().getCompanyName());
            res.add("response", innerArr);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public InputStream exportToExcelSalesRegisterForMonth(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "DATE", "LEDGER", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("SalesRegister");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    long credit_total = 0L, debit_total = 0L;
                    int rowIdx = 1;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("transaction_date").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("particulars").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("voucher_type").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("voucher_no").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(5).setCellValue(batchNo.get("credit").getAsDouble());


//
                        credit_total += batchNo.get("credit").getAsDouble();
                        debit_total += batchNo.get("debit").getAsDouble();
                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(4).setCellValue(debit_total);
                    prow.createCell(5).setCellValue(credit_total);
//

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }
            }
            return new ByteArrayInputStream(out.toByteArray());

        } catch(Exception e){
            stockLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportToExcelsalesRegisteryear(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "MONTHS", "DEBIT", "CREDIT", "CLOSING BALANCE","TPYE"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("PurchaseRegister");
//                    Row headerRow1 = sheet.createRow(0);
//                    CellStyle headerCellStyle = workbook.createCellStyle();
//                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
//                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//                    Cell cell1 = headerRow1.createCell(0);
//                    Cell cell2 = headerRow1.createCell(1);
//                    Cell cell3 = headerRow1.createCell(2);
//                    Cell cell4 = headerRow1.createCell(3);
//                    Cell cell5 = headerRow1.createCell(4);
//                    cell1.setCellValue("Sales Register Report of Year");
//                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4)); // Merge from cell1 to cell5
//                    for (int i = 0; i < 5; i++) {
//                        headerRow1.getCell(i).setCellStyle(headerCellStyle);
//                    }
//                    Cell cellT = headerRow1.createCell(1);
//                    cellT.setCellValue("Sales Register Report of Year");
//                    cellT.setCellStyle(headerCellStyle);

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    long credit_total = 0L, debit_total = 0L, Tclosing_balance = 0L ;
                    int rowIdx = 2;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(2).setCellValue(batchNo.get("credit").getAsDouble());
                        row.createCell(3).setCellValue(batchNo.get("closing_balance").getAsDouble());
                        if(batchNo.get("debit").getAsDouble() > 0 || batchNo.get("credit").getAsDouble() >0){
                            row.createCell(4).setCellValue(batchNo.get("type").getAsString());
                        }

                        credit_total += batchNo.get("credit").getAsDouble();
                        debit_total += batchNo.get("debit").getAsDouble();
                        Tclosing_balance += batchNo.get("closing_balance").getAsDouble();
                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(1).setCellValue(debit_total);
                    prow.createCell(2).setCellValue(credit_total);
                    prow.createCell(3).setCellValue(Tclosing_balance);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }
            }
            return new ByteArrayInputStream(out.toByteArray());

        } catch(Exception e){
            stockLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportToExcelSalesOrderForMonth(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "DATE", "LEDGER", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("SalesOrder");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    long credit_total = 0L, debit_total = 0L;
                    int rowIdx = 1;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("transaction_date").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("particulars").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("voucher_type").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("voucher_no").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(5).setCellValue(batchNo.get("credit").getAsDouble());


//
                        credit_total += batchNo.get("credit").getAsDouble();
                        debit_total += batchNo.get("debit").getAsDouble();
                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(4).setCellValue(debit_total);
                    prow.createCell(5).setCellValue(credit_total);
//

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }
            }
            return new ByteArrayInputStream(out.toByteArray());

        } catch(Exception e){
            stockLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportToExcelsalesOrderyear(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "MONTHS", "DEBIT", "CREDIT", "CLOSING BALANCE","TPYE"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("PurchaseOrder");
//                    Row headerRow1 = sheet.createRow(0);
//                    CellStyle headerCellStyle = workbook.createCellStyle();
//                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
//                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//                    Cell cell1 = headerRow1.createCell(0);
//                    Cell cell2 = headerRow1.createCell(1);
//                    Cell cell3 = headerRow1.createCell(2);
//                    Cell cell4 = headerRow1.createCell(3);
//                    Cell cell5 = headerRow1.createCell(4);
//                    cell1.setCellValue("Sales Register Report of Year");
//                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4)); // Merge from cell1 to cell5
//                    for (int i = 0; i < 5; i++) {
//                        headerRow1.getCell(i).setCellStyle(headerCellStyle);
//                    }
//                    Cell cellT = headerRow1.createCell(1);
//                    cellT.setCellValue("Sales Register Report of Year");
//                    cellT.setCellStyle(headerCellStyle);

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    long credit_total = 0L, debit_total = 0L, Tclosing_balance = 0L ;
                    int rowIdx = 2;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(2).setCellValue(batchNo.get("credit").getAsDouble());
                        row.createCell(3).setCellValue(batchNo.get("closing_balance").getAsDouble());
                        if(batchNo.get("debit").getAsDouble() > 0 || batchNo.get("credit").getAsDouble() >0){
                            row.createCell(4).setCellValue(batchNo.get("type").getAsString());
                        }

                        credit_total += batchNo.get("credit").getAsDouble();
                        debit_total += batchNo.get("debit").getAsDouble();
                        Tclosing_balance += batchNo.get("closing_balance").getAsDouble();
                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(1).setCellValue(debit_total);
                    prow.createCell(2).setCellValue(credit_total);
                    prow.createCell(3).setCellValue(Tclosing_balance);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }
            }
            return new ByteArrayInputStream(out.toByteArray());

        } catch(Exception e){
            stockLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportToExcelSalesChallanForMonth(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "DATE", "LEDGER", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("SalesChallan");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    long credit_total = 0L, debit_total = 0L;
                    int rowIdx = 1;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("transaction_date").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("particulars").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("voucher_type").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("voucher_no").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(5).setCellValue(batchNo.get("credit").getAsDouble());


//
                        credit_total += batchNo.get("credit").getAsDouble();
                        debit_total += batchNo.get("debit").getAsDouble();
                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(4).setCellValue(debit_total);
                    prow.createCell(5).setCellValue(credit_total);
//

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }
            }
            return new ByteArrayInputStream(out.toByteArray());

        } catch(Exception e){
            stockLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportToExcelsalesChallanyear(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "MONTHS", "DEBIT", "CREDIT", "CLOSING BALANCE","TPYE"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("SalesChallan");
//                    Row headerRow1 = sheet.createRow(0);
//                    CellStyle headerCellStyle = workbook.createCellStyle();
//                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
//                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//                    Cell cell1 = headerRow1.createCell(0);
//                    Cell cell2 = headerRow1.createCell(1);
//                    Cell cell3 = headerRow1.createCell(2);
//                    Cell cell4 = headerRow1.createCell(3);
//                    Cell cell5 = headerRow1.createCell(4);
//                    cell1.setCellValue("Sales Register Report of Year");
//                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4)); // Merge from cell1 to cell5
//                    for (int i = 0; i < 5; i++) {
//                        headerRow1.getCell(i).setCellStyle(headerCellStyle);
//                    }
//                    Cell cellT = headerRow1.createCell(1);
//                    cellT.setCellValue("Sales Register Report of Year");
//                    cellT.setCellStyle(headerCellStyle);

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    long credit_total = 0L, debit_total = 0L, Tclosing_balance = 0L ;
                    int rowIdx = 2;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(2).setCellValue(batchNo.get("credit").getAsDouble());
                        row.createCell(3).setCellValue(batchNo.get("closing_balance").getAsDouble());
                        if(batchNo.get("debit").getAsDouble() > 0 || batchNo.get("credit").getAsDouble() >0){
                            row.createCell(4).setCellValue(batchNo.get("type").getAsString());
                        }

                        credit_total += batchNo.get("credit").getAsDouble();
                        debit_total += batchNo.get("debit").getAsDouble();
                        Tclosing_balance += batchNo.get("closing_balance").getAsDouble();
                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(1).setCellValue(debit_total);
                    prow.createCell(2).setCellValue(credit_total);
                    prow.createCell(3).setCellValue(Tclosing_balance);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }
            }
            return new ByteArrayInputStream(out.toByteArray());

        } catch(Exception e){
            stockLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

}
