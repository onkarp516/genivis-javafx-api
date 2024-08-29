package in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrder;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurChallanRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurReturnsRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
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

//import static in.truethics.ethics.ethicsapiv10.service.reports_service.Service.productLogger;
//import static in.truethics.ethics.ethicsapiv10.service.reports_service.StockService.productLogger;


@Service
public class PurRegisterReportService {
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private FiscalYearRepository fiscalYearRepository;
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private TranxPurOrderRepository tranxPurOrderRepository;
    @Autowired
    private TranxPurReturnsRepository tranxPurReturnsRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TranxPurChallanRepository tranxPurChallanRepository;

    static final Logger stockLogger = LogManager.getLogger(PurRegisterReportService.class);

    public Object getMonthwisePurRegisterTransactionDetails(HttpServletRequest request) {
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
                    totalInvoiceAmt = tranxPurInvoiceRepository.findTotalInvoiceAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);
                    totalReturnAmt = tranxPurReturnsRepository.findTotalInvoiceAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);
                } else {
                    totalInvoiceAmt = tranxPurInvoiceRepository.findTotalInvoicesAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);
                    totalReturnAmt = tranxPurReturnsRepository.findTotalInvoicesAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);
                }
                totalInvoices = totalInvoiceAmt - totalReturnAmt;
                closingBalance = totalInvoiceAmt - totalReturnAmt;
                if(totalInvoices > 0){
                    jsonObject.addProperty("debit", 0.0);
                    jsonObject.addProperty("credit",  Math.abs(totalInvoices));
                }
                else if(totalInvoices<0){
                    jsonObject.addProperty("debit", Math.abs(totalInvoices));
                    jsonObject.addProperty("credit", 0.0);
                }
                else{
                    jsonObject.addProperty("debit", 0.0);
                    jsonObject.addProperty("credit", 0.0);
                }
                jsonObject.addProperty("month", month);
                jsonObject.addProperty("start_date", startMonthDate.toString());
                jsonObject.addProperty("end_date", endMonthDate.toString());
                jsonObject.addProperty("closing_balance", Math.abs(closingBalance));
                jsonObject.addProperty("type", closingBalance > 0 ? "CR" : "DR");
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

    public Object getPurRegisterTransactionDetails(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<TranxPurInvoice> mlist = new ArrayList<>();
        List<TranxPurReturnInvoice> mreturnList = new ArrayList<>();
        List<TranxCreditNoteNewReferenceMaster> creditNoteList = new ArrayList<>();
        Map<String, String[]> paraMap = request.getParameterMap();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            LocalDate startDate =null;
            LocalDate endDate=null;
            String durations=null;
            if(paraMap.containsKey("start_date") && paraMap.containsKey("end_date") ){
                startDate = LocalDate.parse(request.getParameter("start_date"));

                endDate = LocalDate.parse(request.getParameter("end_date"));
            }
            else if(paraMap.containsKey("duration")){
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
                        startDate = LocalDate.parse(obj[0].toString());
                        endDate = LocalDate.parse(obj[1].toString());
                    }
                }
            }
            Long branchId = null;
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                mlist = tranxPurInvoiceRepository.findInvoices(users.getOutlet().getId(), branchId, startDate, endDate);
                mreturnList = tranxPurReturnsRepository.findInvoice(users.getOutlet().getId(), branchId, startDate, endDate);
            } else {
                mlist = tranxPurInvoiceRepository.findInvoicesNoBr(users.getOutlet().getId(), startDate, endDate);
                mreturnList = tranxPurReturnsRepository.findInvoicesNoBr(users.getOutlet().getId(), startDate, endDate);
            }
            JsonArray debitArray = new JsonArray();
            JsonArray creditArray = new JsonArray();
            for (TranxPurInvoice mInvoice : mlist) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getInvoiceDate().toString());
                inside.addProperty("voucher_no", mInvoice.getVendorInvoiceNo());
                inside.addProperty("voucher_type", "Purchase Voucher");
                inside.addProperty("particulars", mInvoice.getSundryCreditors() != null ?
                        mInvoice.getSundryCreditors().getLedgerName() : "");
                inside.addProperty("credit", mInvoice.getTotalAmount());
                inside.addProperty("debit",0.0);
//                creditArray.add(inside);
                debitArray.add(inside);
            }
            for (TranxPurReturnInvoice mInvoice : mreturnList) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getTransactionDate().toString());
                inside.addProperty("voucher_no", mInvoice.getPurRtnNo());
                inside.addProperty("voucher_type", "Purchase Return");
                inside.addProperty("particulars", mInvoice.getSundryCreditors() != null ?
                        mInvoice.getSundryCreditors().getLedgerName() : "");
                inside.addProperty("debit", mInvoice.getTotalAmount());
                inside.addProperty("credit",0.0);
                debitArray.add(inside);
            }
            for (TranxCreditNoteNewReferenceMaster mInvoice : creditNoteList) {
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(mInvoice.getSundryDebtorsId(), true);
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getTranscationDate().toString());
                inside.addProperty("voucher_no", mInvoice.getCreditnoteNewReferenceNo());
                inside.addProperty("voucher_type", "Credit Note");
                inside.addProperty("particulars", ledgerMaster != null ? ledgerMaster.getLedgerName() : "");
                inside.addProperty("debit", mInvoice.getTotalAmount());
                inside.addProperty("credit",0.0);
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

    // purchase order API


    public Object getMonthwisePurRegisterOrderDetails(HttpServletRequest request) {
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
                    totalInvoiceAmt = tranxPurOrderRepository.findTotalOrderAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);
                    totalReturnAmt = tranxPurReturnsRepository.findTotalInvoiceAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);
                } else {
                    totalInvoiceAmt = tranxPurOrderRepository.findTotalOrderAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);
                    totalReturnAmt = tranxPurReturnsRepository.findTotalInvoicesAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);
                }
                totalInvoices = totalInvoiceAmt - totalReturnAmt;
                closingBalance = totalInvoiceAmt - totalReturnAmt;
                if(totalInvoices > 0){
                    jsonObject.addProperty("debit", 0.0);
                    jsonObject.addProperty("credit", totalInvoices);
                }
                else if(totalInvoices<0){
                    jsonObject.addProperty("debit", Math.abs(totalInvoices));
                    jsonObject.addProperty("credit", "");
                }
                else{
                    jsonObject.addProperty("debit", 0.0);
                    jsonObject.addProperty("credit", 0.0);
                }
                jsonObject.addProperty("month", month);

                jsonObject.addProperty("start_date", startMonthDate.toString());
                jsonObject.addProperty("end_date", endMonthDate.toString());
                jsonObject.addProperty("closing_balance", Math.abs(closingBalance));
                jsonObject.addProperty("type", closingBalance > 0 ? "CR" : "DR");
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



    public Object getPurRegisterOrderDetails(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<TranxPurOrder> mlist = new ArrayList<>();
        List<TranxPurReturnInvoice> mreturnList = new ArrayList<>();
        List<TranxCreditNoteNewReferenceMaster> creditNoteList = new ArrayList<>();
        Map<String, String[]> paraMap = request.getParameterMap();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            LocalDate startDate =null;
            LocalDate endDate=null;
            String durations=null;
            if(paraMap.containsKey("start_date") && paraMap.containsKey("end_date") ){
                startDate = LocalDate.parse(request.getParameter("start_date"));

                endDate = LocalDate.parse(request.getParameter("end_date"));
            }
            else if(paraMap.containsKey("duration")){
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
                        startDate = LocalDate.parse(obj[0].toString());
                        endDate = LocalDate.parse(obj[1].toString());
                    }
                }
            }
            Long branchId = null;
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                mlist = tranxPurOrderRepository.findorder(users.getOutlet().getId(), branchId, startDate, endDate);
//                mreturnList = tranxPurReturnsRepository.findInvoice(users.getOutlet().getId(), branchId, startDate, endDate);
            } else {
                mlist = tranxPurOrderRepository.findordersNoBr(users.getOutlet().getId(), startDate, endDate);
//                mreturnList = tranxPurReturnsRepository.findInvoicesNoBr(users.getOutlet().getId(), startDate, endDate);
            }
            JsonArray debitArray = new JsonArray();
            JsonArray creditArray = new JsonArray();
            for (TranxPurOrder mInvoice : mlist) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getInvoiceDate().toString());
                inside.addProperty("voucher_no", mInvoice.getVendorInvoiceNo());
                inside.addProperty("voucher_type", "Purchase Voucher");
                inside.addProperty("particulars", mInvoice.getSundryCreditors() != null ?
                        mInvoice.getSundryCreditors().getLedgerName() : "");
                inside.addProperty("credit", mInvoice.getTotalAmount());
                inside.addProperty("debit",0.0);
//                creditArray.add(inside);
                debitArray.add(inside);
            }
//            for (TranxPurReturnInvoice mInvoice : mreturnList) {
//                JsonObject inside = new JsonObject();
//                inside.addProperty("row_id", mInvoice.getId());
//                inside.addProperty("transaction_date", mInvoice.getTransactionDate().toString());
//                inside.addProperty("voucher_no", mInvoice.getPurRtnNo());
//                inside.addProperty("voucher_type", "Purchase Return");
//                inside.addProperty("particulars", mInvoice.getSundryCreditors() != null ?
//                        mInvoice.getSundryCreditors().getLedgerName() : "");
//                inside.addProperty("debit", mInvoice.getTotalAmount());
//                inside.addProperty("credit",0.0);
//                debitArray.add(inside);
//            }
//            for (TranxCreditNoteNewReferenceMaster mInvoice : creditNoteList) {
//                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(mInvoice.getSundryDebtorsId(), true);
//                JsonObject inside = new JsonObject();
//                inside.addProperty("row_id", mInvoice.getId());
//                inside.addProperty("transaction_date", mInvoice.getTranscationDate().toString());
//                inside.addProperty("voucher_no", mInvoice.getCreditnoteNewReferenceNo());
//                inside.addProperty("voucher_type", "Credit Note");
//                inside.addProperty("particulars", ledgerMaster != null ? ledgerMaster.getLedgerName() : "");
//                inside.addProperty("debit", mInvoice.getTotalAmount());
//                inside.addProperty("credit",0.0);
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



    // purchase Challan API


    public Object getMonthwisePurRegisterChallanDetails(HttpServletRequest request) {
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
                    totalInvoiceAmt = tranxPurChallanRepository.findTotalchallanAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);
                    totalReturnAmt = tranxPurReturnsRepository.findTotalInvoiceAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, true);
                } else {
                    totalInvoiceAmt = tranxPurChallanRepository.findTotalchallanAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);
                    totalReturnAmt = tranxPurReturnsRepository.findTotalInvoicesAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, true);
                } totalInvoices = totalInvoiceAmt - totalReturnAmt;
                closingBalance = totalInvoiceAmt - totalReturnAmt;
                if(totalInvoices > 0){
                    jsonObject.addProperty("debit", 0.0);
                    jsonObject.addProperty("credit", totalInvoices);
                }
                else if(totalInvoices<0){
                    jsonObject.addProperty("debit", Math.abs(totalInvoices));
                    jsonObject.addProperty("credit", 0.0);
                }
                else{
                    jsonObject.addProperty("debit", 0.0);
                    jsonObject.addProperty("credit", 0.0);
                }
                jsonObject.addProperty("month", month);
                jsonObject.addProperty("start_date", startMonthDate.toString());
                jsonObject.addProperty("end_date", endMonthDate.toString());
                jsonObject.addProperty("closing_balance", Math.abs(closingBalance));
                jsonObject.addProperty("type", closingBalance > 0 ? "CR" : "DR");
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



    public Object getPurRegisterChallanDetails(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<TranxPurChallan> mlist = new ArrayList<>();
        List<TranxPurReturnInvoice> mreturnList = new ArrayList<>();
        List<TranxCreditNoteNewReferenceMaster> creditNoteList = new ArrayList<>();
        Map<String, String[]> paraMap = request.getParameterMap();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            LocalDate startDate =null;
            LocalDate endDate=null;
            String durations=null;
            if(paraMap.containsKey("start_date") && paraMap.containsKey("end_date") ){
                startDate = LocalDate.parse(request.getParameter("start_date"));

                endDate = LocalDate.parse(request.getParameter("end_date"));
            }
            else if(paraMap.containsKey("duration")){
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
                        startDate = LocalDate.parse(obj[0].toString());
                        endDate = LocalDate.parse(obj[1].toString());
                    }
                }
            }
            Long branchId = null;
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                mlist = tranxPurChallanRepository.findChallan(users.getOutlet().getId(), branchId, startDate, endDate);
//                mreturnList = tranxPurReturnsRepository.findInvoice(users.getOutlet().getId(), branchId, startDate, endDate);
            } else {
                mlist = tranxPurChallanRepository.findChallanNoBr(users.getOutlet().getId(), startDate, endDate);
//                mreturnList = tranxPurReturnsRepository.findInvoicesNoBr(users.getOutlet().getId(), startDate, endDate);
            }
            JsonArray debitArray = new JsonArray();
            JsonArray creditArray = new JsonArray();
            for (TranxPurChallan mInvoice : mlist) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", mInvoice.getId());
                inside.addProperty("transaction_date", mInvoice.getInvoiceDate().toString());
                inside.addProperty("voucher_no", mInvoice.getVendorInvoiceNo());
                inside.addProperty("voucher_type", "Purchase Voucher");
                inside.addProperty("particulars", mInvoice.getSundryCreditors() != null ?
                        mInvoice.getSundryCreditors().getLedgerName() : "");
                inside.addProperty("credit", mInvoice.getTotalAmount());
                inside.addProperty("debit",0.0);
//                creditArray.add(inside);
                debitArray.add(inside);
            }
//            for (TranxPurReturnInvoice mInvoice : mreturnList) {
//                JsonObject inside = new JsonObject();
//                inside.addProperty("row_id", mInvoice.getId());
//                inside.addProperty("transaction_date", mInvoice.getTransactionDate().toString());
//                inside.addProperty("voucher_no", mInvoice.getPurRtnNo());
//                inside.addProperty("voucher_type", "Purchase Return");
//                inside.addProperty("particulars", mInvoice.getSundryCreditors() != null ?
//                        mInvoice.getSundryCreditors().getLedgerName() : "");
//                inside.addProperty("debit", mInvoice.getTotalAmount());
//                inside.addProperty("credit",0.0);
//                debitArray.add(inside);
//            }
//            for (TranxCreditNoteNewReferenceMaster mInvoice : creditNoteList) {
//                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(mInvoice.getSundryDebtorsId(), true);
//                JsonObject inside = new JsonObject();
//                inside.addProperty("row_id", mInvoice.getId());
//                inside.addProperty("transaction_date", mInvoice.getTranscationDate().toString());
//                inside.addProperty("voucher_no", mInvoice.getCreditnoteNewReferenceNo());
//                inside.addProperty("voucher_type", "Credit Note");
//                inside.addProperty("particulars", ledgerMaster != null ? ledgerMaster.getLedgerName() : "");
//                inside.addProperty("debit", mInvoice.getTotalAmount());
//                inside.addProperty("credit",0.0);
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



    public InputStream exportToExcelPurchaseOrder(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("PurchaseOrder");

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

                    long sumOfQty = 0;
                    long sumOfQty1 = 0;

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
                        sumOfQty += batchNo.get("debit").getAsDouble();
                        sumOfQty1 += batchNo.get("credit").getAsDouble();


                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    cell.setCellValue(sumOfQty);
                    Cell cell1 = prow.createCell(5);
                    cell1.setCellValue(sumOfQty1);

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

    public InputStream exportToExcelPurchaseChallan(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("PurchaseChallan");

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

                    long sumOfQty = 0;
                   long sumOfQty1 =0;
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
                        sumOfQty += batchNo.get("debit").getAsDouble();
                        sumOfQty1 += batchNo.get("credit").getAsDouble();


                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    cell.setCellValue(sumOfQty);
                    Cell cell1 = prow.createCell(5);
                    cell1.setCellValue(sumOfQty1);

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


    public InputStream exportToExcelPurchasereg2(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "MONTHS", "DEBIT", "CREDIT","CLOSING BALANCE","TPYE"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("PurchaseRegister");

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

                    long sumOfQty = 0;
                    long sumOfQty1 = 0;

                    long sumOfQty2 = 0;

                    int rowIdx = 1;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(2).setCellValue(batchNo.get("credit").getAsDouble());
                        row.createCell(3).setCellValue(batchNo.get("closing_balance").getAsDouble());
                        row.createCell(4).setCellValue(batchNo.get("type").getAsString());


//
                        sumOfQty += batchNo.get("debit").getAsDouble();
                        sumOfQty1 += batchNo.get("credit").getAsDouble();
                        sumOfQty2 += batchNo.get("closing_balance").getAsDouble();


                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(1);
                    cell.setCellValue(sumOfQty);
                    Cell cell1 = prow.createCell(2);
                    cell1.setCellValue(sumOfQty1);
                    Cell cell2 = prow.createCell(3);
                    cell2.setCellValue(sumOfQty2);

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

    public InputStream exportToExcelPurchaseOrdermonth(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "MONTHS", "DEBIT", "CREDIT","CLOSING BALANCE","TPYE"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("PurchaseOrder");

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

                    long sumOfQty = 0;
                    long sumOfQty1 = 0;

                    long sumOfQty2 = 0;

                    int rowIdx = 1;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(2).setCellValue(batchNo.get("credit").getAsDouble());
                        row.createCell(3).setCellValue(batchNo.get("closing_balance").getAsDouble());
                        row.createCell(4).setCellValue(batchNo.get("type").getAsString());


//
                        sumOfQty += batchNo.get("debit").getAsDouble();
                        sumOfQty1 += batchNo.get("credit").getAsDouble();
                        sumOfQty2 += batchNo.get("closing_balance").getAsDouble();


                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(1);
                    cell.setCellValue(sumOfQty);
                    Cell cell1 = prow.createCell(2);
                    cell1.setCellValue(sumOfQty1);
                    Cell cell2 = prow.createCell(3);
                    cell2.setCellValue(sumOfQty2);

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


    public InputStream exportToExcelPurchaseChallanmonth(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "MONTHS", "DEBIT", "CREDIT","CLOSING BALANCE","TPYE"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("PurchaseChallan");

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

                    long sumOfQty = 0;
                    long sumOfQty2 = 0;
                    long sumOfQty3= 0;

                    int rowIdx = 1;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(2).setCellValue(batchNo.get("credit").getAsDouble());
                        row.createCell(3).setCellValue(batchNo.get("closing_balance").getAsDouble());
                        row.createCell(4).setCellValue(batchNo.get("type").getAsString());


//
                        sumOfQty += batchNo.get("debit").getAsDouble();
                        sumOfQty2 += batchNo.get("credit").getAsDouble();
                        sumOfQty3 += batchNo.get("closing_balance").getAsDouble();


                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(1);
                    cell.setCellValue(sumOfQty);
                    Cell cell1 = prow.createCell(2);
                    cell1.setCellValue(sumOfQty2);
                    Cell cell2 = prow.createCell(3);
                    cell2.setCellValue(sumOfQty3);

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
