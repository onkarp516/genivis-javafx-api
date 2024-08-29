package in.truethics.ethics.ethicsapiv10.service.reports_service;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.model.master.TransactionTypeMaster;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionTypeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.PaymentReportService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DayBookService {

    @Autowired
    JwtTokenUtil jwtRequestFilter;

    @Autowired
    private DaybookRepository daybookRepository;

    public JsonObject getAllLedgersTransactions(HttpServletRequest request) {
        TransactionTypeMaster tranxType = null;
        Map<String, String[]> paramMap = request.getParameterMap();

        JsonArray result = new JsonArray();

        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        if (paramMap.containsKey("startDate")) {
            startDate = request.getParameter("startDate");
            startDatep = LocalDate.parse(startDate);
            endDate = request.getParameter("startDate");
            endDatep = LocalDate.parse(endDate);
        } else {
            startDatep = LocalDate.now();
            endDatep = LocalDate.now();

        }


        List<DayBook> summaries = new ArrayList<>();
        if (users.getBranch() != null)
            summaries = daybookRepository.findByTranxDateAndStatusAndOutletIdAndBranchId(startDatep, endDatep, true, users.getOutlet().getId(), users.getBranch().getId());
        else {
            summaries = daybookRepository.findByTranxDateAndStatusAndOutletId(startDatep, endDatep, true, users.getOutlet().getId());
        }
        for (DayBook details : summaries) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("transaction_date", details.getTranxDate().toString());
            jsonObject.addProperty("perticulars", details.getParticulars());
            jsonObject.addProperty("voucher_type", details.getVoucherType());
            jsonObject.addProperty("voucher_no", details.getVoucherNo());
            jsonObject.addProperty("amount", Math.abs(details.getAmount()));
            jsonObject.addProperty("id", details.getId());
            result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        json.addProperty("company_name", users.getOutlet().getCompanyName());
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }
    public InputStream exportToExcelDaybook(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
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
                    Sheet sheet = workbook.createSheet("Daybook");

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
                        row.createCell(1).setCellValue(batchNo.get("perticulars").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("voucher_type").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("voucher_no").getAsString());
                        if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Payment")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Invoice")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase Return Invoice")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Debitnote")
                        )
                        row.createCell(4).setCellValue(batchNo.get("amount").getAsDouble());
                        else row.createCell(4).setCellValue(0.0);
                        if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Receipt")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase Invoice")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Return Invoice")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Contra")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Journal")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Creditnote")
                        )
                        row.createCell(5).setCellValue(batchNo.get("amount").getAsDouble());
                        else row.createCell(5).setCellValue(0.0);
//
                        if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Payment")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Invoice")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase Return Invoice")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Debitnote")

                        )
                            sumOfQty += batchNo.get("amount").getAsDouble();

                        if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Receipt")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Purchase Invoice")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Return Invoice")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Contra")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Journal")||
                                batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Creditnote")
                        )
                        sumOfQty1 += batchNo.get("amount").getAsDouble();


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
            PaymentReportService.stockLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


}
