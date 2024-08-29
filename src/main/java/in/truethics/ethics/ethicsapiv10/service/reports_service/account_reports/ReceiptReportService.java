package in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticulars;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsRepository;
import in.truethics.ethics.ethicsapiv10.util.DateConvertUtil;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
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

import static in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.PaymentReportService.stockLogger;

@Service
public class ReceiptReportService {

    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private FiscalYearRepository fiscalYearRepository;
    @Autowired
    private LedgerTransactionPostingsRepository postingsRepository;

    @Autowired
    private TranxReceiptPerticularsRepository tranxReceiptPerticularsRepository;

    public Object getMonthwiseReceiptTransactionDetails(HttpServletRequest request) {
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
            System.out.println("Outlet Id:" + users.getOutlet().getId());
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
                    totalInvoices = postingsRepository.findTotalNumberInvoices(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, 5L, true, "CR");
                    totalInvoiceAmt = postingsRepository.findTotalInvoiceAmtwithBr(users.getOutlet().getId(),
                            users.getBranch().getId(), startMonthDate, endMonthDate, 5L, true, "CR");
                } else {
                    totalInvoices = postingsRepository.findTotalNumberInvoicesNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, 5L, true, "CR");
                    totalInvoiceAmt = postingsRepository.findTotalInvoicesAmtNoBranch(
                            users.getOutlet().getId(), startMonthDate, endMonthDate, 5L, true, "CR");
                }
                jsonObject.addProperty("month", month);
                jsonObject.addProperty("no_vouchers", totalInvoices);
                jsonObject.addProperty("total_amt", totalInvoiceAmt);
                jsonObject.addProperty("start_date", startMonthDate.toString());
                jsonObject.addProperty("end_date", endMonthDate.toString());
                innerArr.add(jsonObject);
            }

            res.addProperty("d_start_date", currentStartDate.toString());
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

    public Object getReceiptTransactionDetails(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        LocalDate startDatep =null;
        LocalDate endDatep = null;
        String durations = null;
        List<LedgerTransactionPostings> mlist = new ArrayList<>();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Map<String, String[]> paraMap = request.getParameterMap();

            if(paraMap.containsKey("start_date") && paraMap.containsKey("end_date")){
                String stDay = request.getParameter("start_date");
                startDatep= LocalDate.parse(stDay);
                String endDay = request.getParameter("end_date");
                endDatep= LocalDate.parse(endDay);
            }
            else if (paraMap.containsKey("duration")) {
                durations = request.getParameter("duration");
                if (durations.equalsIgnoreCase("month")) {
                    //for finding first and last day of current month
                    LocalDate thisMonth = LocalDate.now();
                    String fDay = thisMonth.withDayOfMonth(1).toString();
                    String lDay = thisMonth.withDayOfMonth(thisMonth.lengthOfMonth()).toString();
                    startDatep = LocalDate.parse(fDay);
                    endDatep = LocalDate.parse(lDay);

                } else if (durations.equalsIgnoreCase("lastMonth")) {
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
                    startDatep = LocalDate.parse(firstDay);
                    endDatep = LocalDate.parse(lastDay);

                } else if (durations.equalsIgnoreCase("halfYear")) {
                    //for finding first and second half year start day and end day
                    LocalDate currentDate = LocalDate.now();
                    //for first half-year
                    LocalDate lastYear = currentDate.minusYears(1);
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
                    System.out.println("firstDayFirstHalfFormatted " + firstDayFirstHalfFormatted + " lastDayFirstHalfFormatted " + lastDayFirstHalfFormatted);
                    System.out.println("firstDaySecondHalfFormatted " + firstDaySecondHalfFormatted + "  lastDaySecondHalfFormatted " + lastDaySecondHalfFormatted);
                    startDatep = LocalDate.parse(firstDaySecondHalfFormatted);
                    endDatep = LocalDate.parse(lastDaySecondHalfFormatted);
                } else if (durations.equalsIgnoreCase("fullYear")) {
                    List<Object[]> nlist = new ArrayList<>();
                    nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();
                    for (int i = 0; i < nlist.size(); i++) {
                        Object obj[] = nlist.get(i);
//                    System.out.println("start Date:" + obj[0].toString());
//                    System.out.println("end Date:" + obj[1].toString());
                        startDatep = LocalDate.parse(obj[0].toString());
                        endDatep = LocalDate.parse(obj[1].toString());
                    }
                }
            }
//            LocalDate startDate = LocalDate.parse(request.getParameter("start_date"));
//            LocalDate endDate = LocalDate.parse(request.getParameter("end_date"));
            Long branchId = null;
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                mlist = postingsRepository.findReceiptDetails(users.getOutlet().getId(), branchId, startDatep, endDatep,
                        5L, "CR");
            } else {
                mlist = postingsRepository.findReceiptDetailsNoBranch(users.getOutlet().getId(), startDatep, endDatep,
                        5L, "CR");
            }
            JsonArray innerArr = new JsonArray();
            for (LedgerTransactionPostings ledgerTransactionPostings : mlist) {
                JsonObject inside = new JsonObject();
                inside.addProperty("row_id", ledgerTransactionPostings.getId());
                inside.addProperty("transaction_date", DateConvertUtil.convertDateToLocalDate(ledgerTransactionPostings.getTransactionDate()).toString());
                inside.addProperty("voucher_no", ledgerTransactionPostings.getInvoiceNo());
                inside.addProperty("voucher_id", ledgerTransactionPostings.getTransactionId());
                TranxReceiptPerticulars tranxReceiptPerticulars = null;
                if (users.getBranch() != null) {
                    tranxReceiptPerticulars = tranxReceiptPerticularsRepository.
                            findByTranxReceiptMasterIdAndOutletIdAndBranchIdAndStatusAndType(ledgerTransactionPostings.getTransactionId(),
                                    users.getOutlet().getId(), users.getBranch().getId(), true, "CR");
                    inside.addProperty("particulars", tranxReceiptPerticulars.getLedgerMaster().getLedgerName());
                } else {
                    tranxReceiptPerticulars = tranxReceiptPerticularsRepository.
                            findByTranxReceiptMasterIdAndOutletIdAndStatusAndType(ledgerTransactionPostings.getTransactionId(),
                                    users.getOutlet().getId(), true, "CR");

                    inside.addProperty("particulars", tranxReceiptPerticulars.getLedgerMaster() != null ?
                            tranxReceiptPerticulars.getLedgerMaster().getLedgerName() : "");
                }
                inside.addProperty("voucher_type", ledgerTransactionPostings.getTransactionType().getTransactionName());
                inside.addProperty("credit", ledgerTransactionPostings.getAmount());
                inside.addProperty("debit", 0.00);
                innerArr.add(inside);
            }
            res.addProperty("d_start_date", startDatep.toString());
            res.addProperty("d_end_date", endDatep.toString());
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

    public InputStream exportToExcelReceipt(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
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

    public InputStream exportToExcelReceiptmonth(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos "+productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = { "MONTHS", "NO OF VOUCHER", "TOTAL AMOUNT"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("PAYMENT");

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

                    int sumOfQty = 0;
                    long sumOfQty1 = 0;
                    int rowIdx = 1;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("month").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("no_vouchers").getAsDouble());
                        row.createCell(2).setCellValue(batchNo.get("total_amt").getAsDouble());


//
                        sumOfQty1 += batchNo.get("total_amt").getAsDouble();
                        sumOfQty += batchNo.get("no_vouchers").getAsDouble();


                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(1);
                    cell.setCellValue(sumOfQty);
                    Cell cell1 = prow.createCell(2);
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


}
