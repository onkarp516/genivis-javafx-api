package in.truethics.ethics.ethicsapiv10.service.Gstr_Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.GenericDTData;
import in.truethics.ethics.ethicsapiv10.common.NumFormat;
import in.truethics.ethics.ethicsapiv10.dto.salesdto.SalesInvoiceDTO;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerGstDetails;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerGstDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.spring.web.json.Json;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

@Service
public class GSTR1Service {
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private LedgerGstDetailsRepository gstDetailsRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private NumFormat numFormat;
    @Autowired
    FiscalYearRepository fiscalYearRepository;

    private static final Logger productLogger = LogManager.getLogger(ProductService.class);


    public JsonObject getGSTR1Data(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        String searchText = request.getParameter("searchText");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        Boolean flag = false;
        List sundryDebtorsData = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();


        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
            query += " AND bill_date BETWEEN '" + startDate + "' AND '" + endDate + "'";

        if (!searchText.equalsIgnoreCase("")) {
            query = query + " AND ledger_master_id LIKE '%" + searchText + "%'";
        }
        query = query+" GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUDR");
        q.setParameter(2, "DR");
        q.setParameter(3, 3);
        sundryDebtorsData = q.getResultList();

        for (Object sdId : sundryDebtorsData) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(sdId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;
                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails != null && mGstDetails.size() > 0) {
                    count = ledgerTransactionPostingsRepository.findSdInvoiceCount("DR", 3L, mLedger.getId());
                    list = tranxSalesInvoiceRepository.findTotal(mLedger);
                }
                JsonObject mObject = new JsonObject();
                if(list != null && !list.isEmpty() && list.get(0) != null) {
                    String data[] = list.get(0).split(",");
                    mObject.addProperty("total_amt", numFormat.numFormat(Double.parseDouble(data[0])));
                    mObject.addProperty("taxable_amt", numFormat.numFormat(Double.parseDouble(data[1])));
                    mObject.addProperty("total_tax", numFormat.numFormat(Double.parseDouble(data[2])));
                    mObject.addProperty("total_igst", numFormat.numFormat(Double.parseDouble(data[3])));
                    mObject.addProperty("total_cgst", numFormat.numFormat(Double.parseDouble(data[4])));
                    mObject.addProperty("total_sgst", numFormat.numFormat(Double.parseDouble(data[5])));
                    mObject.addProperty("ledger_name", mLedger.getLedgerName());
                    mObject.addProperty("ledger_id", mLedger.getId());
                    mObject.addProperty("total_invoices", count);

                    mArray.add(mObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalObject.addProperty("message", "Success");
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;
    }

    public JsonObject getGSTR1DataScreen1(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        String searchText = request.getParameter("searchText");
        Boolean flag = false;
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else{
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }

        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;

        if ( startDatep != null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }

        List sundryDebtorsData = new ArrayList<>();
        JsonArray mArray = new JsonArray();


        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_transaction_postings_tbl.status=1 AND ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (startDatep != null && endDatep != null  && !startDatep.equals("") && !endDatep.equals(""))
            query += " AND DATE(transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        if (!searchText.equalsIgnoreCase("")) {
            query = query + " AND (ledger_master_id LIKE '%" + searchText + "%' OR " +
                    "ledger_master_tbl.ledger_name LIKE '%" + searchText + "%')";
        }
        query = query + " GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUDR");
        q.setParameter(2, "DR");
        q.setParameter(3, 3);
        sundryDebtorsData = q.getResultList();

        for (Object sdId : sundryDebtorsData) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(parseLong(sdId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;
                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails != null && mGstDetails.size() > 0) {
                    count = ledgerTransactionPostingsRepository.findSdInvoiceCount("DR", 3L, mLedger.getId());
                    list = tranxSalesInvoiceRepository.findTotal(mLedger);
                }
                JsonObject mObject = new JsonObject();
                if (list != null && !list.isEmpty() && list.get(0) != null) {
                    String data[] = list.get(0).split(",");
                    mObject.addProperty("total_amt", numFormat.numFormat(parseDouble(data[0])));
                    mObject.addProperty("taxable_amt", numFormat.numFormat(parseDouble(data[1])));
                    mObject.addProperty("total_tax", numFormat.numFormat(parseDouble(data[2])));
                    mObject.addProperty("total_igst", numFormat.numFormat(parseDouble(data[3])));
                    mObject.addProperty("total_cgst", numFormat.numFormat(parseDouble(data[4])));
                    mObject.addProperty("total_sgst", numFormat.numFormat(parseDouble(data[5])));
                    mObject.addProperty("ledger_name", mLedger.getLedgerName());
                    mObject.addProperty("ledger_id", mLedger.getId());
                    mObject.addProperty("gst_number", mGstDetails.get(0).getGstin());
                    mObject.addProperty("total_invoices", count);
                    mArray.add(mObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    public InputStream exportExcelGSTR1B2B1Data(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Sr.No", "Particulars", "GSTIN/UIN", "Voucher Count", "Taxable Amt.", "IGST Amt", "CGST Amt", "SGST Amt", "Cess Amt", "Tax Amt", "Invoice Amt"};
//                    if(mfgShow)
//                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "MFG.", "EXP.", "QTY", "UNIT"};
                    Sheet sheet = workbook.createSheet("GSTR1_B2B_Data");

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

                    int sumOfVoucherCount = 0;
                    Double sumOfTaxableAmt = 0.0, sumOfIGSTAmt = 0.0, sumOfCGSTAmt = 0.0, sumOfSGSTAmt = 0.0, sumOfTaxAmt = 0.0, sumOfInvoiceAmt = 0.0;

                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
//                        1st row serial no.
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(batchNo.get("ledger_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("gst_number").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("total_invoices").getAsLong());
                        row.createCell(4).setCellValue(batchNo.get("taxable_amt").getAsDouble());
//                        if(mfgShow) {
                        row.createCell(5).setCellValue(batchNo.get("total_igst").getAsDouble());
                        row.createCell(6).setCellValue(batchNo.get("total_cgst").getAsDouble());
                        row.createCell(7).setCellValue(batchNo.get("total_sgst").getAsDouble());
                        row.createCell(8).setCellValue("");
                        row.createCell(9).setCellValue(batchNo.get("total_tax").getAsDouble());
//                        }else {
                        row.createCell(10).setCellValue(batchNo.get("total_amt").getAsDouble());
//                        }

                        sumOfVoucherCount += batchNo.get("total_invoices").getAsDouble();
                        sumOfTaxableAmt += batchNo.get("taxable_amt").getAsDouble();
                        sumOfIGSTAmt += batchNo.get("total_igst").getAsDouble();
                        sumOfCGSTAmt += batchNo.get("total_cgst").getAsDouble();
                        sumOfSGSTAmt += batchNo.get("total_sgst").getAsDouble();
                        sumOfTaxAmt += batchNo.get("total_tax").getAsDouble();
                        sumOfInvoiceAmt += batchNo.get("total_amt").getAsDouble();


                    }

                    Row prow = sheet.createRow(rowIdx++);
//                    for (int i = 0; i < headers.length; i++) {
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(3).setCellValue(sumOfVoucherCount);
                    prow.createCell(4).setCellValue(sumOfTaxableAmt);
                    prow.createCell(5).setCellValue(sumOfIGSTAmt);
                    prow.createCell(6).setCellValue(sumOfCGSTAmt);
                    prow.createCell(7).setCellValue(sumOfSGSTAmt);
                    prow.createCell(8).setCellValue("");
                    prow.createCell(9).setCellValue(sumOfTaxAmt);
                    prow.createCell(10).setCellValue(sumOfInvoiceAmt);
//                        Cell cell = prow.createCell(10);
//                        cell.setCellValue("total");
//                        cell.setCellValue("");
//                        cell.setCellValue("");
//                        cell.setCellValue(sumOfVoucherCount);
//                        cell.setCellValue(sumOfTaxableAmt);
//                        cell.setCellValue(sumOfIGSTAmt);
//                        cell.setCellValue(sumOfCGSTAmt);
//                        cell.setCellValue(sumOfSGSTAmt);
//
//                        cell.setCellValue(sumOfTaxAmt);
//                        cell.setCellValue(sumOfInvoiceAmt);
//                    }

//                    >>>>>>>>>>>

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream ExportExcelGSTR1B2BSalesData(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + productBatchNos.size());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Sr.No", "Particulars", "Dates","Invoice No.","Taxable Amt.", "IGST Amt", "CGST Amt", "SGST Amt", "Cess Amt", "Tax Amt", "Invoice Amt"};
//                    if(mfgShow)
//                        headers = new String[]{"BRAND NAME", "PRODUCT NAME", "PACKING", "BATCH NO", "MFG.", "EXP.", "QTY", "UNIT"};
                    Sheet sheet = workbook.createSheet("GSTR1_B2C_ExcelSheet");

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

//                    int sumOfVoucherCount = 0;
                    Double sumOfTaxableAmt = 0.0, sumOfIGSTAmt = 0.0, sumOfCGSTAmt = 0.0, sumOfSGSTAmt = 0.0, sumOfTaxAmt = 0.0, sumOfInvoiceAmt = 0.0;

                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
//                        1st row serial no.
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(batchNo.get("sundry_debtor_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("invoice_date").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("invoice_no").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("taxable_amt").getAsDouble());
//                        if(mfgShow) {
                        row.createCell(5).setCellValue(batchNo.get("totaligst").getAsDouble());
                        row.createCell(6).setCellValue(batchNo.get("totalcgst").getAsDouble());
                        row.createCell(7).setCellValue(batchNo.get("totalsgst").getAsDouble());
                        row.createCell(8).setCellValue("");
                        row.createCell(9).setCellValue(batchNo.get("tax_amt").getAsDouble());
//                        }else {
                        row.createCell(10).setCellValue(batchNo.get("total_amount").getAsDouble());
//                        }

//                        sumOfVoucherCount += batchNo.get("total_invoices").getAsDouble();
                        sumOfTaxableAmt += batchNo.get("taxable_amt").getAsDouble();
                        sumOfIGSTAmt += batchNo.get("totaligst").getAsDouble();
                        sumOfCGSTAmt += batchNo.get("totalcgst").getAsDouble();
                        sumOfSGSTAmt += batchNo.get("totalsgst").getAsDouble();
                        sumOfTaxAmt += batchNo.get("tax_amt").getAsDouble();
                        sumOfInvoiceAmt += batchNo.get("total_amount").getAsDouble();


                    }

                    Row prow = sheet.createRow(rowIdx++);
//                    for (int i = 0; i < headers.length; i++) {
                    prow.createCell(0).setCellValue("Total");
//                    prow.createCell(3).setCellValue(sumOfVoucherCount);
                    prow.createCell(4).setCellValue(sumOfTaxableAmt);
                    prow.createCell(5).setCellValue(sumOfIGSTAmt);
                    prow.createCell(6).setCellValue(sumOfCGSTAmt);
                    prow.createCell(7).setCellValue(sumOfSGSTAmt);
                    prow.createCell(8).setCellValue("");
                    prow.createCell(9).setCellValue(sumOfTaxAmt);
                    prow.createCell(10).setCellValue(sumOfInvoiceAmt);
//                        Cell cell = prow.createCell(10);
//                        cell.setCellValue("total");
//                        cell.setCellValue("");
//                        cell.setCellValue("");
//                        cell.setCellValue(sumOfVoucherCount);
//                        cell.setCellValue(sumOfTaxableAmt);
//                        cell.setCellValue(sumOfIGSTAmt);
//                        cell.setCellValue(sumOfCGSTAmt);
//                        cell.setCellValue(sumOfSGSTAmt);
//
//                        cell.setCellValue(sumOfTaxAmt);
//                        cell.setCellValue(sumOfInvoiceAmt);
//                    }

//                    >>>>>>>>>>>

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public JsonObject getGSTR1B2CLData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        System.out.println("users " + users.toString());
        JsonObject finalObject = new JsonObject();
//        String searchText = request.getParameter("searchText");

        Boolean flag = false;
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate
                = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
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
        }
//        List B2CscreenData = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        String query ="SELECT state_tbl.id, state_tbl.name, state_tbl.state_code, tsidu.igst, SUM(tsi.total_base_amount), SUM(tsi.totaligst), " +
                "SUM(tsi.totalsgst), SUM(tsi.totalcgst), SUM(tsi.total_tax), SUM(tsi.total_amount) FROM tranx_sales_invoice_tbl AS tsi " +
                "LEFT JOIN ledger_master_tbl ON tsi.sundry_debtors_id = ledger_master_tbl.id " +
                "LEFT JOIN tranx_sales_invoice_details_units_tbl AS tsidu ON tsi.id = tsidu.sales_invoice_id " +
                "LEFT JOIN state_tbl ON ledger_master_tbl.state_id = state_tbl.id " +
                "WHERE tsi.status=1 AND tsidu.final_amount > 250000  AND ledger_master_tbl.taxable=0 AND DATE(tsi.bill_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' " +
                "GROUP BY ledger_master_tbl.state_id, tsidu.igst";


        Query q = entityManager.createNativeQuery(query);
        System.out.println("q-===>"+query);
        List<Object[]> B2CscreenData = q.getResultList();

//        for (Object b2Cscreen1Data : B2CscreenData) {
        System.out.println("b2Cscreen1Data"+B2CscreenData.size());
        try {
            for (int j = 0; j < B2CscreenData.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + B2CscreenData);
//                    inspObj.addProperty("actualSize", "-");
//                    inspObj.addProperty("sizeResult", "");
                if (B2CscreenData != null) {
                    inspObj.addProperty("sr_no", j+1);
                    inspObj.addProperty("state_id", B2CscreenData.get(j)[0].toString());
                    inspObj.addProperty("state_name", B2CscreenData.get(j)[1].toString());
                    inspObj.addProperty("state_code", B2CscreenData.get(j)[2].toString());
                    inspObj.addProperty("rate_of_tax", B2CscreenData.get(j)[3].toString());
                    inspObj.addProperty("taxable_amt", parseDouble( B2CscreenData.get(j)[4].toString()));
                    inspObj.addProperty("igst_amt", parseDouble(B2CscreenData.get(j)[5].toString()));
                    inspObj.addProperty("sgst_amt", parseDouble(B2CscreenData.get(j)[6].toString()));
                    inspObj.addProperty("cgst_amt", parseDouble(B2CscreenData.get(j)[7].toString()));
                    inspObj.addProperty("tax_amt", parseDouble(B2CscreenData.get(j)[8].toString()));
                    inspObj.addProperty("invoice_amt", parseDouble(B2CscreenData.get(j)[9].toString()));
                }
                mArray.add(inspObj);
            }
//                JsonObject data = new JsonObject();
//                data.addProperty("state_id", "1");
//                data.addProperty("state_id2", "2");
//                data.addProperty("state_id3",b2Cscreen1Data.toString() );


//          mArray.add(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }
    public JsonObject getGSTR1B2CSData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        System.out.println("users " + users.toString());
        JsonObject finalObject = new JsonObject();
//        String searchText = request.getParameter("searchText");

        Boolean flag = false;
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
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
        }
//        List B2CscreenData = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        String query ="SELECT state_tbl.id, state_tbl.name, state_tbl.state_code, tsidu.igst, SUM(tsi.total_base_amount), SUM(tsi.totaligst), " +
                "SUM(tsi.totalsgst), SUM(tsi.totalcgst), SUM(tsi.total_tax), SUM(tsi.total_amount), ledger_master_tbl.state_id FROM tranx_sales_invoice_tbl AS tsi " +
                "LEFT JOIN ledger_master_tbl ON tsi.sundry_debtors_id = ledger_master_tbl.id " +
                "LEFT JOIN tranx_sales_invoice_details_units_tbl AS tsidu ON tsi.id = tsidu.sales_invoice_id " +
                "LEFT JOIN state_tbl ON ledger_master_tbl.state_id = state_tbl.id " +
                "WHERE tsi.status=1 AND tsidu.final_amount <= 250000 AND ledger_master_tbl.taxable=0 AND DATE(tsi.bill_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "' " +
                "GROUP BY ledger_master_tbl.state_id, tsidu.igst";


        Query q = entityManager.createNativeQuery(query);
        System.out.println("q-===>"+query);
        List<Object[]> B2CscreenData = q.getResultList();

//        for (Object b2Cscreen1Data : B2CscreenData) {
        System.out.println("b2Cscreen1Data"+B2CscreenData.size());
        try {
            for (int j = 0; j < B2CscreenData.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + B2CscreenData);
                if (B2CscreenData != null) {
                    inspObj.addProperty("sr_no", j+1);
                    inspObj.addProperty("state_name", B2CscreenData.get(j)[1].toString());
                    inspObj.addProperty("state_code", B2CscreenData.get(j)[2].toString());
                    inspObj.addProperty("rate_of_tax", B2CscreenData.get(j)[3].toString());
                    inspObj.addProperty("taxable_amt", parseDouble(B2CscreenData.get(j)[4].toString()));
                    inspObj.addProperty("igst_amt", parseDouble(B2CscreenData.get(j)[5].toString()));
                    inspObj.addProperty("sgst_amt", parseDouble(B2CscreenData.get(j)[6].toString()));
                    inspObj.addProperty("cgst_amt", parseDouble(B2CscreenData.get(j)[7].toString()));
                    inspObj.addProperty("tax_amt", parseDouble( B2CscreenData.get(j)[8].toString()));
                    inspObj.addProperty("invoice_amt", parseDouble(B2CscreenData.get(j)[9].toString()));
                    inspObj.addProperty("state_id", B2CscreenData.get(j)[10].toString());
                }
                mArray.add(inspObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    public InputStream exportExcelGSTR1B2C1Data(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
//            String state_code = jsonRequest.get("state_code");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + jsonRequest);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Sr.No", "Particulars", "Rate of tax%", "Taxable Amt", "Integrated Tax Amt", "Centeral Tax Amt", "State Tax Amt", "Cess Amt", "Tax Amt", "Invoice Amt"};
                    Sheet sheet = workbook.createSheet("GSTR1_B2C1_ExcelSheet");

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

                    int sumOfVoucherCount = 0;
                    Double sumOfTaxableAmt = 0.0, sumOfIGSTAmt = 0.0, sumOfCGSTAmt = 0.0, sumOfSGSTAmt = 0.0, sumOfTaxAmt = 0.0, sumOfInvoiceAmt = 0.0;

                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        System.out.println("batchNo=="+batchNo);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(batchNo.get("state_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("rate_of_tax").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("taxable_amt").getAsLong());
                        row.createCell(5).setCellValue(batchNo.get("cgst_amt").getAsDouble());
                        row.createCell(6).setCellValue(batchNo.get("sgst_amt").getAsDouble());
                        row.createCell(4).setCellValue(batchNo.get("igst_amt").getAsDouble());
                        row.createCell(8).setCellValue(batchNo.get("tax_amt").getAsDouble());
                        row.createCell(9).setCellValue(batchNo.get("invoice_amt").getAsDouble());
                        sumOfTaxableAmt += batchNo.get("taxable_amt").getAsDouble();
                        sumOfCGSTAmt += batchNo.get("cgst_amt").getAsDouble();
                        sumOfSGSTAmt += batchNo.get("sgst_amt").getAsDouble();
                        sumOfIGSTAmt += batchNo.get("igst_amt").getAsDouble();

                        sumOfTaxAmt += batchNo.get("tax_amt").getAsDouble();
                        sumOfInvoiceAmt += batchNo.get("invoice_amt").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(3).setCellValue(sumOfTaxableAmt);
                    prow.createCell(4).setCellValue(sumOfIGSTAmt);
                    prow.createCell(5).setCellValue(sumOfCGSTAmt);
                    prow.createCell(6).setCellValue(sumOfSGSTAmt);
                    prow.createCell(8).setCellValue(sumOfTaxAmt);
                    prow.createCell(9).setCellValue(sumOfInvoiceAmt);


                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    //    Api for GSTR1 B2C Large screen2 outward data excel export
    public InputStream exportExcelGSTR1B2C2Data(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + jsonRequest);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Sr.No", "Date", "Invoice NO", "Particulars", "Voucher Type","Taxable Amt","Integrated Tax Amt", "Centeral Tax Amt", "State Tax Amt", "Cess Amt", "Tax Amt", "Invoice Amt"};
                    Sheet sheet = workbook.createSheet("GSTR1_B2CLarge2_ExcelSheet");

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

                    int sumOfVoucherCount = 0;
                    Double sumOfTaxableAmt = 0.0, sumOfIGSTAmt = 0.0, sumOfCGSTAmt = 0.0, sumOfSGSTAmt = 0.0, sumOfTaxAmt = 0.0, sumOfInvoiceAmt = 0.0;

                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        System.out.println("batchNo=="+batchNo);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(batchNo.get("invoice_date").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("invoice_no").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("sundry_debtor_name").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("sale_account_name").getAsString());
                        row.createCell(5).setCellValue(batchNo.get("taxable_amt").getAsDouble());
                        row.createCell(6).setCellValue(batchNo.get("totaligst").getAsDouble());
                        row.createCell(7).setCellValue(batchNo.get("totalcgst").getAsDouble());
                        row.createCell(8).setCellValue(batchNo.get("totalsgst").getAsDouble());
                        row.createCell(10).setCellValue(batchNo.get("tax_amt").getAsDouble());
                        row.createCell(11).setCellValue(batchNo.get("total_amount").getAsDouble());

                        sumOfTaxableAmt += batchNo.get("taxable_amt").getAsDouble();
                        sumOfCGSTAmt += batchNo.get("totalcgst").getAsDouble();
                        sumOfSGSTAmt += batchNo.get("totalsgst").getAsDouble();
                        sumOfIGSTAmt += batchNo.get("totaligst").getAsDouble();

                        sumOfTaxAmt += batchNo.get("tax_amt").getAsDouble();
                        sumOfInvoiceAmt += batchNo.get("total_amount").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(5).setCellValue(sumOfTaxableAmt);
                    prow.createCell(6).setCellValue(sumOfIGSTAmt);
                    prow.createCell(7).setCellValue(sumOfCGSTAmt);
                    prow.createCell(8).setCellValue(sumOfSGSTAmt);
                    prow.createCell(10).setCellValue(sumOfTaxAmt);
                    prow.createCell(11).setCellValue(sumOfInvoiceAmt);


                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    //API for B2C Small screen1 outward data excel export
    public InputStream exportExcelGSTR1B2C1Small(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
//            String state_code = jsonRequest.get("state_code");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size111:" + jsonRequest);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Sr.No", "Particulars", "Rate of tax%", "Taxable Amt", "Integrated Tax Amt", "Centeral Tax Amt", "State Tax Amt", "Cess Amt", "Tax Amt", "Invoice Amt"};
                    Sheet sheet = workbook.createSheet("GSTR1_B2C1_ExcelSheet");

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

                    int sumOfVoucherCount = 0;
                    Double sumOfTaxableAmt = 0.0, sumOfIGSTAmt = 0.0, sumOfCGSTAmt = 0.0, sumOfSGSTAmt = 0.0, sumOfTaxAmt = 0.0, sumOfInvoiceAmt = 0.0;

                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(batchNo.get("state_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("rate_of_tax").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("taxable_amt").getAsLong());
                        row.createCell(5).setCellValue(batchNo.get("cgst_amt").getAsDouble());
                        row.createCell(6).setCellValue(batchNo.get("sgst_amt").getAsDouble());
                        row.createCell(4).setCellValue(batchNo.get("igst_amt").getAsDouble());
                        row.createCell(8).setCellValue(batchNo.get("tax_amt").getAsDouble());
                        row.createCell(9).setCellValue(batchNo.get("invoice_amt").getAsDouble());

                        sumOfTaxableAmt += batchNo.get("taxable_amt").getAsDouble();
                        sumOfCGSTAmt += batchNo.get("cgst_amt").getAsDouble();
                        sumOfSGSTAmt += batchNo.get("sgst_amt").getAsDouble();
                        sumOfIGSTAmt += batchNo.get("igst_amt").getAsDouble();

                        sumOfTaxAmt += batchNo.get("tax_amt").getAsDouble();
                        sumOfInvoiceAmt += batchNo.get("invoice_amt").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(3).setCellValue(sumOfTaxableAmt);
                    prow.createCell(4).setCellValue(sumOfIGSTAmt);
                    prow.createCell(5).setCellValue(sumOfCGSTAmt);
                    prow.createCell(6).setCellValue(sumOfSGSTAmt);
                    prow.createCell(8).setCellValue(sumOfTaxAmt);
                    prow.createCell(9).setCellValue(sumOfInvoiceAmt);


                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    //    Api for GSTR1 B2C Small screen2 outward data excel export
    public InputStream exportExcelGSTR1B2Csmall2Data(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + jsonRequest);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Sr.No", "Date", "Invoice NO", "Particulars", "Voucher Type","Taxable Amt","Integrated Tax Amt", "Centeral Tax Amt", "State Tax Amt", "Cess Amt", "Tax Amt", "Invoice Amt"};
                    Sheet sheet = workbook.createSheet("GSTR1_B2CSmall2_ExcelSheet");

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

                    int sumOfVoucherCount = 0;
                    Double sumOfTaxableAmt = 0.0, sumOfIGSTAmt = 0.0, sumOfCGSTAmt = 0.0, sumOfSGSTAmt = 0.0, sumOfTaxAmt = 0.0, sumOfInvoiceAmt = 0.0;

                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        System.out.println("batchNo=="+batchNo);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(batchNo.get("invoice_date").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("invoice_no").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("sundry_debtor_name").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("sale_account_name").getAsString());
                        row.createCell(5).setCellValue(batchNo.get("taxable_amt").getAsDouble());
                        row.createCell(6).setCellValue(batchNo.get("totaligst").getAsDouble());
                        row.createCell(7).setCellValue(batchNo.get("totalcgst").getAsDouble());
                        row.createCell(8).setCellValue(batchNo.get("totalsgst").getAsDouble());
                        row.createCell(10).setCellValue(batchNo.get("tax_amt").getAsDouble());
                        row.createCell(11).setCellValue(batchNo.get("total_amount").getAsDouble());

                        sumOfTaxableAmt += batchNo.get("taxable_amt").getAsDouble();
                        sumOfCGSTAmt += batchNo.get("totalcgst").getAsDouble();
                        sumOfSGSTAmt += batchNo.get("totalsgst").getAsDouble();
                        sumOfIGSTAmt += batchNo.get("totaligst").getAsDouble();

                        sumOfTaxAmt += batchNo.get("tax_amt").getAsDouble();
                        sumOfInvoiceAmt += batchNo.get("total_amount").getAsDouble();
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    prow.createCell(5).setCellValue(sumOfTaxableAmt);
                    prow.createCell(6).setCellValue(sumOfIGSTAmt);
                    prow.createCell(7).setCellValue(sumOfCGSTAmt);
                    prow.createCell(8).setCellValue(sumOfSGSTAmt);
                    prow.createCell(10).setCellValue(sumOfTaxAmt);
                    prow.createCell(11).setCellValue(sumOfInvoiceAmt);


                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }
    //    Api for GSTR1 B2C Large screen1 outward data excel export
//    GSTR1 Credit/Debit Note Registered Api
    public JsonObject getGSTR2CRNOTEReg(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        String searchText = request.getParameter("searchText");
        Boolean flag = false;
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        }else {

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
        }
        List Data = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();


        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_transaction_postings_tbl.status=1 AND ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (!startDatep.equals("") && !endDatep.equals(""))
            query += " AND DATE(transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        if (searchText != null && !searchText.isEmpty()) {
            query = query + " AND (ledger_master_id LIKE '%" + searchText + "%' OR " +
                    "ledger_master_tbl.ledger_name LIKE '%" + searchText + "%')";
        }
        query = query + " GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (jsonObject != null && !jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUDR");
        q.setParameter(2, "CR");
        q.setParameter(3, 4);
        Data = q.getResultList();

        for (Object sdId : Data) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(parseLong(sdId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;
                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails != null && mGstDetails.size() > 0) {
                    count = ledgerTransactionPostingsRepository.findInvoiceCount("CR", 4L, mLedger.getId());
                    list = gstDetailsRepository.findTotalSalesReturn(mLedger);
                }
                JsonObject mObject = new JsonObject();
                if (list != null && !list.isEmpty() && list.get(0) != null) {
                    String data[] = list.get(0).split(",");
                    System.out.println("data1" + data);
                    mObject.addProperty("total_amt", numFormat.numFormat(parseDouble(data[0])));
                    mObject.addProperty("taxable_amt", numFormat.numFormat(parseDouble(data[1])));
                    mObject.addProperty("total_tax", numFormat.numFormat(parseDouble(data[2])));
                    mObject.addProperty("total_igst", numFormat.numFormat(parseDouble(data[3])));
                    mObject.addProperty("total_cgst", numFormat.numFormat(parseDouble(data[4])));
                    mObject.addProperty("total_sgst", numFormat.numFormat(parseDouble(data[5])));
                    mObject.addProperty("ledger_name", mLedger.getLedgerName());
                    mObject.addProperty("ledger_id", mLedger.getId());
                    mObject.addProperty("gst_number", mGstDetails.get(0).getGstin());
                    mObject.addProperty("invoice_no", data[6]);
                    mObject.addProperty("transaction_date", data[7]);
                    mObject.addProperty("id", parseInt(data[8]));
                    mObject.addProperty("voucher_type", "Sales Return");


                    mArray.add(mObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    //    GSTR1 Credit/Debit Note Un-Registered Api
    public JsonObject getGSTR2CRNOTEUnreg(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        String searchText = request.getParameter("searchText");

        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;

        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;

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
        }


        List Data = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();


        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_transaction_postings_tbl.status=1 AND ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (!startDatep.equals("") && !endDatep.equals(""))
            query += " AND DATE(transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        if (searchText != null && !searchText.isEmpty()) {
            query = query + " AND (ledger_master_id LIKE '%" + searchText + "%' OR " +
                    "ledger_master_tbl.ledger_name LIKE '%" + searchText + "%')";
        }
        query = query + " GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (jsonObject != null && !jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUDR");
        q.setParameter(2, "CR");
        q.setParameter(3, 4);
        Data = q.getResultList();

        for (Object sdId : Data) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(parseLong(sdId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;
                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails.size() == 0) {
                    count = ledgerTransactionPostingsRepository.findInvoiceCount("CR", 4L, mLedger.getId());
                    list = gstDetailsRepository.findTotalSalesReturn(mLedger);
                }
                JsonObject mObject = new JsonObject();
                if (list != null && !list.isEmpty() && list.get(0) != null) {
                    String data[] = list.get(0).split(",");
                    System.out.println("data1" + data);
                    mObject.addProperty("total_amount", numFormat.numFormat(parseDouble(data[0])));
                    mObject.addProperty("taxable_amt", numFormat.numFormat(parseDouble(data[1])));
                    mObject.addProperty("tax_amt", numFormat.numFormat(parseDouble(data[2])));
                    mObject.addProperty("total_igst", numFormat.numFormat(parseDouble(data[3])));
                    mObject.addProperty("total_cgst", numFormat.numFormat(parseDouble(data[4])));
                    mObject.addProperty("total_sgst", numFormat.numFormat(parseDouble(data[5])));
                    mObject.addProperty("sundry_debtor_name", mLedger.getLedgerName());
                    mObject.addProperty("sundry_debtor_id", mLedger.getId());
                    mObject.addProperty("sales_return_no", data[6]);
                    mObject.addProperty("transaction_date", data[7]);
                    mObject.addProperty("id", data[8]);
                    mObject.addProperty("sales_account_name", "Sales Return");
                    mArray.add(mObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    //GSTR1 Nil rated Api
    public JsonObject getGSTR1NIlRATEReg(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        System.out.println("users " + users.toString());
        JsonObject finalObject = new JsonObject();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;

        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            startDate = request.getParameter("start_date");
            endDate = request.getParameter("end_date");
            if(endDate!=null && !endDate.isEmpty() && startDate!=null && !startDate.isEmpty()){
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else{
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep !=null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");
        }
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        String query ="SELECT product_tbl.tax_type, lmt.ledger_name, tsit.total_base_amount, tsit.id, tsit.total_amount FROM `tranx_sales_invoice_details_units_tbl`" +
                " AS tsidut LEFT JOIN product_tbl ON tsidut.product_id = product_tbl.id LEFT JOIN tranx_sales_invoice_tbl" +
                " AS tsit ON tsidut.id = tsit.id LEFT JOIN ledger_master_tbl AS lmt ON tsit.sundry_debtors_id = lmt.id" +
                " WHERE tsidut.status=1 AND (product_tbl.tax_type = 'nilrated' OR product_tbl.tax_type = 'exempted') AND DATE(tsit.bill_date) BETWEEN '" + startDatep +"' AND '" + endDatep + "' ";

        Query q = entityManager.createNativeQuery(query);

        System.out.println("q-===>"+query);
        List<Object[]> nilrateData = q.getResultList();
        System.out.println("nilrate-data"+ nilrateData.size());

        String query1 ="SELECT product_tbl.tax_type, lmt.ledger_name, tsrit.total_base_amount, tsrit.id, tsrit.total_amount FROM `tranx_sales_return_details_units_tbl`" +
                " AS tsrdut LEFT JOIN product_tbl ON tsrdut.product_id = product_tbl.id LEFT JOIN tranx_sales_return_invoice_tbl" +
                " AS tsrit ON tsrdut.id = tsrit.id LEFT JOIN ledger_master_tbl AS lmt ON tsrit.sundry_debtors_id = lmt.id" +
                " WHERE tsrdut.status=1 AND (product_tbl.tax_type = 'nilrated' OR product_tbl.tax_type = 'exempted') AND DATE(tsrit.transaction_date) BETWEEN '" + startDatep +"' AND '" + endDatep + "' ";

        Query q1 = entityManager.createNativeQuery(query1);

        System.out.println("q-===>"+query);
        List<Object[]> nilrateData1 = q1.getResultList();
        System.out.println("nilrate-data1"+ nilrateData1.size());
        try {
            for (int j = 0; j < nilrateData.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + nilrateData.get(j));
                if (nilrateData != null) {
                    inspObj.addProperty("tax_type", nilrateData.get(j)[0].toString());
                    inspObj.addProperty("ledger_name", nilrateData.get(j)[1].toString());
//                    if(nilrateData.get(j)[0].toString().equalsIgnoreCase("nilrated")){
                    inspObj.addProperty("total_amount", parseDouble(nilrateData.get(j)[2].toString()));
//                    }
//                    else{
//                        inspObj.addProperty("total_amount", parseDouble(nilrateData.get(j)[4].toString()));
//                    }

                    inspObj.addProperty("id", parseLong(nilrateData.get(j)[3].toString()));
                    inspObj.addProperty("voucher_type", "Sales Invoice");
                }
                mArray.add(inspObj);
            }
            for (int j = 0; j < nilrateData1.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + nilrateData1.get(j));
                if (nilrateData != null) {
                    inspObj.addProperty("tax_type", nilrateData1.get(j)[0].toString());
                    inspObj.addProperty("ledger_name", nilrateData1.get(j)[1].toString());
//                    if(nilrateData.get(j)[0].toString().equalsIgnoreCase("nilrated")){
                    inspObj.addProperty("total_amount", parseDouble(nilrateData.get(j)[2].toString()));
//                    }
//                    else{
//                        inspObj.addProperty("total_amount", parseDouble(nilrateData.get(j)[4].toString()));
//                    }
                    inspObj.addProperty("id", parseLong(nilrateData1.get(j)[3].toString()));
                    inspObj.addProperty("voucher_type", "Sales Return");
                }
                mArray.add(inspObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    //    GSTR1 Main Screen B2B data
    public JsonObject getGSTR1MainScreen1(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        Boolean flag = false;
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else{
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }

        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;

        if ( startDatep != null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");
        }

        List sundryDebtorsData = new ArrayList<>();

        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (startDatep != null && endDatep != null  && !startDatep.equals("") && !endDatep.equals(""))
            query += " AND DATE(transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        query = query + " GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUDR");
        q.setParameter(2, "DR");
        q.setParameter(3, 3);
        sundryDebtorsData = q.getResultList();
        Long voucher_count = 0L;
        Double T_taxable_amt = 0.0, T_igst_amt = 0.0, T_cgst_amt = 0.0, T_sgst_amt = 0.0, T_tax_amt = 0.0, T_invoice_amt = 0.0;

        for (Object sdId : sundryDebtorsData) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(parseLong(sdId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;

                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails != null && mGstDetails.size() > 0) {
                    count = ledgerTransactionPostingsRepository.findSdInvoiceCount("DR", 3L, mLedger.getId());
                    list = tranxSalesInvoiceRepository.findTotal(mLedger);
                }
                if (list != null && !list.isEmpty() && list.get(0) != null) {
                    String data[] = list.get(0).split(",");

                    if(count>0) {
                        voucher_count = voucher_count + count;
                        T_taxable_amt = T_taxable_amt + numFormat.numFormat(parseDouble(data[1]));
                        T_igst_amt = T_igst_amt + numFormat.numFormat(parseDouble(data[3]));
                        T_cgst_amt = T_cgst_amt + numFormat.numFormat(parseDouble(data[5]));
                        T_sgst_amt = T_sgst_amt + numFormat.numFormat(parseDouble(data[4]));
                        T_tax_amt = T_tax_amt + numFormat.numFormat(parseDouble(data[2]));
                        T_invoice_amt = T_invoice_amt + numFormat.numFormat(parseDouble(data[0]));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        finalObject.addProperty("total_amt", T_invoice_amt);
        finalObject.addProperty("taxable_amt", T_taxable_amt);
        finalObject.addProperty("total_tax", T_tax_amt );
        finalObject.addProperty("total_igst", T_igst_amt);
        finalObject.addProperty("total_cgst", T_cgst_amt);
        finalObject.addProperty("total_sgst", T_sgst_amt);
        finalObject.addProperty("voucher_count", voucher_count);
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());


        return finalObject;

    }

    //    GSTR1 Main Screen B2C large data Api

    public JsonObject getGSTR1MainScreenB2CLarge(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        System.out.println("users " + users.toString());
        JsonObject finalObject = new JsonObject();
//        String searchText = request.getParameter("searchText");

        Boolean flag = false;
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate
                = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        }else {

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
        }

        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        String query ="SELECT SUM(tsi.total_base_amount), SUM(tsi.totaligst), SUM(tsi.totalsgst), SUM(tsi.totalcgst), " +
                "SUM(tsi.total_tax), SUM(tsi.total_amount), COUNT(DISTINCT tsi.id) AS invoice_count FROM tranx_sales_invoice_tbl AS tsi LEFT JOIN ledger_master_tbl" +
                " ON tsi.sundry_debtors_id = ledger_master_tbl.id LEFT JOIN tranx_sales_invoice_details_units_tbl AS tsidu" +
                " ON tsi.id = tsidu.sales_invoice_id LEFT JOIN state_tbl ON ledger_master_tbl.state_id = state_tbl.id" +
                " WHERE tsidu.final_amount > 250000 AND ledger_master_tbl.taxable=0 AND DATE(tsi.bill_date) BETWEEN'"
                + startDatep + "' AND '" + endDatep + "' ";


        Query q = entityManager.createNativeQuery(query);
        System.out.println("q-===>"+query);
        List<Object[]> B2CscreenData = q.getResultList();
        Long voucher_count = 0L;
        Double T_taxable_amt = 0.0, T_igst_amt = 0.0, T_cgst_amt = 0.0, T_sgst_amt = 0.0, T_tax_amt = 0.0, T_invoice_amt = 0.0;

        System.out.println("b2Cscreen1Data"+B2CscreenData.size());
        try {
            for (int j = 0; j < B2CscreenData.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + B2CscreenData);
                if (B2CscreenData != null) {

                    inspObj.addProperty("taxable_amt", parseDouble( B2CscreenData.get(j)[0].toString()));
                    inspObj.addProperty("total_igst", parseDouble(B2CscreenData.get(j)[1].toString()));
                    inspObj.addProperty("total_sgst", parseDouble(B2CscreenData.get(j)[2].toString()));
                    inspObj.addProperty("total_cgst", parseDouble(B2CscreenData.get(j)[3].toString()));
                    inspObj.addProperty("total_tax", parseDouble(B2CscreenData.get(j)[4].toString()));
                    inspObj.addProperty("invoice_amt", parseDouble(B2CscreenData.get(j)[5].toString()));
                    inspObj.addProperty("voucher_count", B2CscreenData.get(j)[6].toString());
//                    inspObj.addProperty("voucher_count", B2CscreenData.get(j)[7].toString());
                }
                mArray.add(inspObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    //    GSTR1 Main Screen B2C small data Api
    public JsonObject getGSTR1MainScreenB2Csmall(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        System.out.println("users " + users.toString());
        JsonObject finalObject = new JsonObject();
//        String searchText = request.getParameter("searchText");

        Boolean flag = false;
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate
                = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        }else {

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
        }

        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        String query ="SELECT SUM(tsi.total_base_amount), SUM(tsi.totaligst), SUM(tsi.totalsgst), SUM(tsi.totalcgst), " +
                "SUM(tsi.total_tax), SUM(tsi.total_amount), COUNT(DISTINCT tsi.id) FROM tranx_sales_invoice_tbl AS tsi LEFT JOIN ledger_master_tbl" +
                " ON tsi.sundry_debtors_id = ledger_master_tbl.id LEFT JOIN tranx_sales_invoice_details_units_tbl AS tsidu" +
                " ON tsi.id = tsidu.sales_invoice_id LEFT JOIN state_tbl ON ledger_master_tbl.state_id = state_tbl.id" +
                " WHERE tsidu.final_amount <= 250000 AND ledger_master_tbl.taxable=0 AND DATE(tsi.bill_date) BETWEEN'"
                + startDatep + "' AND '" + endDatep + "' ";


        Query q = entityManager.createNativeQuery(query);
        System.out.println("q-===>"+query);
        List<Object[]> B2CscreenData = q.getResultList();
        Long voucher_count = 0L;
        Double T_taxable_amt = 0.0, T_igst_amt = 0.0, T_cgst_amt = 0.0, T_sgst_amt = 0.0, T_tax_amt = 0.0, T_invoice_amt = 0.0;

        System.out.println("b2Cscreen1Data"+B2CscreenData.size());
        try {
            for (int j = 0; j < B2CscreenData.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + B2CscreenData);
                if (B2CscreenData != null) {
                    inspObj.addProperty("taxable_amt", parseDouble( B2CscreenData.get(j)[0].toString()));
                    inspObj.addProperty("total_igst", parseDouble(B2CscreenData.get(j)[1].toString()));
                    inspObj.addProperty("total_sgst", parseDouble(B2CscreenData.get(j)[2].toString()));
                    inspObj.addProperty("total_cgst", parseDouble(B2CscreenData.get(j)[3].toString()));
                    inspObj.addProperty("total_tax", parseDouble(B2CscreenData.get(j)[4].toString()));
                    inspObj.addProperty("total_amt", parseDouble(B2CscreenData.get(j)[5].toString()));
                    inspObj.addProperty("voucher_count", B2CscreenData.get(j)[6].toString());
                }
                mArray.add(inspObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    //    GSTR1 main Screen Credit note Registered Api
    public JsonObject getGSTR1MainScreenCRNoteReg(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        String searchText = request.getParameter("searchText");
        Boolean flag = false;
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        }else {

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
        }
        List Data = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();


        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (!startDatep.equals("") && !endDatep.equals(""))
            query += " AND DATE(transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        if (searchText != null && !searchText.isEmpty()) {
            query = query + " AND (ledger_master_id LIKE '%" + searchText + "%' OR " +
                    "ledger_master_tbl.ledger_name LIKE '%" + searchText + "%')";
        }
        query = query + " GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (jsonObject != null && !jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUDR");
        q.setParameter(2, "CR");
        q.setParameter(3, 4);
        Data = q.getResultList();

        Long voucher_count = 0L;
        Double T_taxable_amt = 0.0, T_igst_amt = 0.0, T_cgst_amt = 0.0, T_sgst_amt = 0.0, T_tax_amt = 0.0, T_invoice_amt = 0.0;
        for (Object sdId : Data) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(parseLong(sdId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;
                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails != null && mGstDetails.size() > 0) {
                    count = ledgerTransactionPostingsRepository.findInvoiceCount("CR", 4L, mLedger.getId());
                    list = gstDetailsRepository.findTotalSalesReturn(mLedger);
                }

                JsonObject mObject = new JsonObject();
                if (list != null && !list.isEmpty() && list.get(0) != null) {
                    String data[] = list.get(0).split(",");
                    System.out.println("data1" + data);
                    voucher_count = voucher_count + count;
                    if(count>0) {
//                        voucher_count = voucher_count + count;
                        T_taxable_amt = T_taxable_amt + numFormat.numFormat(parseDouble(data[1]));
                        T_igst_amt = T_igst_amt + numFormat.numFormat(parseDouble(data[3]));
                        T_cgst_amt = T_cgst_amt + numFormat.numFormat(parseDouble(data[5]));
                        T_sgst_amt = T_sgst_amt + numFormat.numFormat(parseDouble(data[4]));
                        T_tax_amt = T_tax_amt + numFormat.numFormat(parseDouble(data[2]));
                        T_invoice_amt = T_invoice_amt + numFormat.numFormat(parseDouble(data[0]));
                    }


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalObject.addProperty("voucher_count",voucher_count);
        finalObject.addProperty("total_amt",T_invoice_amt);
        finalObject.addProperty("taxable_amt", T_taxable_amt);
        finalObject.addProperty("total_tax", T_tax_amt);
        finalObject.addProperty("total_igst", T_igst_amt !=null ? T_igst_amt : 0.0);
        finalObject.addProperty("total_cgst", T_cgst_amt !=null ? T_cgst_amt : 0.0);
        finalObject.addProperty("total_sgst", T_sgst_amt !=null ? T_sgst_amt : 0.0);
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    //    GSTR1 main Screen Credit note UnRegistered Api
    public JsonObject getGSTR1MainScreenCRNoteUnreg(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        String searchText = request.getParameter("searchText");

        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;

        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;

        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else {
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        }else {

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
        }


        List Data = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();


        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (!startDatep.equals("") && !endDatep.equals(""))
            query += " AND DATE(transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        if (searchText != null && !searchText.isEmpty()) {
            query = query + " AND (ledger_master_id LIKE '%" + searchText + "%' OR " +
                    "ledger_master_tbl.ledger_name LIKE '%" + searchText + "%')";
        }
        query = query + " GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (jsonObject != null && !jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUDR");
        q.setParameter(2, "CR");
        q.setParameter(3, 4);
        Data = q.getResultList();

        Long voucher_count = 0L;
        Double T_taxable_amt = 0.0, T_igst_amt = 0.0, T_cgst_amt = 0.0, T_sgst_amt = 0.0, T_tax_amt = 0.0, T_invoice_amt = 0.0;

        for (Object sdId : Data) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(parseLong(sdId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;
                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails.size() == 0) {
                    count = ledgerTransactionPostingsRepository.findInvoiceCount("CR", 4L, mLedger.getId());
                    list = gstDetailsRepository.findTotalSalesReturn(mLedger);
                }
                JsonObject mObject = new JsonObject();
                if (list != null && !list.isEmpty() && list.get(0) != null) {
                    String data[] = list.get(0).split(",");
                    System.out.println("data1" + data);

                    voucher_count = voucher_count + count;
                    if(count>0) {
                        T_taxable_amt = T_taxable_amt + numFormat.numFormat(parseDouble(data[1]));
                        T_igst_amt = T_igst_amt + numFormat.numFormat(parseDouble(data[3]));
                        T_cgst_amt = T_cgst_amt + numFormat.numFormat(parseDouble(data[5]));
                        T_sgst_amt = T_sgst_amt + numFormat.numFormat(parseDouble(data[4]));
                        T_tax_amt = T_tax_amt + numFormat.numFormat(parseDouble(data[2]));
                        T_invoice_amt = T_invoice_amt + numFormat.numFormat(parseDouble(data[0]));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalObject.addProperty("voucher_count",voucher_count);
        finalObject.addProperty("total_amt",T_invoice_amt);
        finalObject.addProperty("taxable_amt", T_taxable_amt);
        finalObject.addProperty("total_tax", T_tax_amt);
        finalObject.addProperty("total_igst", T_igst_amt !=null ? T_igst_amt : 0.0);
        finalObject.addProperty("total_cgst", T_cgst_amt !=null ? T_cgst_amt : 0.0);
        finalObject.addProperty("total_sgst", T_sgst_amt !=null ? T_sgst_amt : 0.0);
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    public JsonObject getGSTR1MainScreenHSN(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        System.out.println("users " + users.toString());
        JsonObject finalObject = new JsonObject();

        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;

        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            startDate = request.getParameter("start_date");
            endDate = request.getParameter("end_date");
            if(endDate!=null && !endDate.isEmpty() && startDate!=null && !startDate.isEmpty()){
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else{
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep !=null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");
        }
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        String query ="SELECT DATE(tsit.bill_date), tsit.sales_invoice_no, ledger_master_tbl.ledger_name, " +
                "ledger_gst_details_tbl.gstin, units_tbl.unit_name, tsit.total_base_amount, tsit.totaligst,tsit.totalcgst, " +
                " tsit.totalsgst, tsit.total_tax, tsit.id, tsit.total_amount FROM `tranx_sales_invoice_details_units_tbl` AS tsidut LEFT" +
                " JOIN product_tbl ON tsidut.product_id = product_tbl.id LEFT JOIN tranx_sales_invoice_tbl AS tsit ON" +
                " tsidut.sales_invoice_id = tsit.id LEFT JOIN ledger_master_tbl ON tsit.sundry_debtors_id =" +
                " ledger_master_tbl.id LEFT JOIN ledger_gst_details_tbl ON ledger_gst_details_tbl.ledger_id =" +
                " ledger_master_tbl.id LEFT JOIN units_tbl ON tsidut.unit_id= units_tbl.id WHERE DATE(tsit.bill_date) BETWEEN '" + startDatep + "' AND '" + endDatep+"' ";
        System.out.printf("Query:"+query);
        Query q = entityManager.createNativeQuery(query);
        System.out.println("q-===>"+query);
        List<Object[]> HSNData = q.getResultList();
        System.out.println("HSN data"+HSNData.size());

        Long voucher_count = 0L;
        Double T_taxable_amt = 0.0, T_igst_amt = 0.0, T_cgst_amt = 0.0, T_sgst_amt = 0.0, T_tax_amt = 0.0, T_invoice_amt = 0.0;

        try {

            for (int j = 0; j < HSNData.size(); j++) {
                JsonObject inspObj = new JsonObject();

                Long count =0L;
                System.out.println("inspObj " + HSNData);
                if (HSNData != null) {

                    System.out.println("HSNData.get(j).length " + HSNData.get(j).length);
                    if(HSNData.get(j).length >0) {
                        count++;
                        voucher_count = voucher_count + count;
                        T_taxable_amt = T_taxable_amt + numFormat.numFormat(parseDouble( HSNData.get(j)[5].toString()));
                        T_igst_amt = T_igst_amt + numFormat.numFormat(parseDouble( HSNData.get(j)[6].toString()));
                        T_cgst_amt = T_cgst_amt + numFormat.numFormat(parseDouble( HSNData.get(j)[7].toString()));
                        T_sgst_amt = T_sgst_amt + numFormat.numFormat(parseDouble( HSNData.get(j)[8].toString()));
                        T_tax_amt = T_tax_amt + numFormat.numFormat(parseDouble( HSNData.get(j)[9].toString()));
                        T_invoice_amt = T_invoice_amt + numFormat.numFormat(parseDouble( HSNData.get(j)[11].toString()));
                    }
//                    inspObj.addProperty("hsn_no", HSNData.get(j)[0].toString());
//                    inspObj.addProperty("hsn_description", HSNData.get(j)[1].toString());
//                    inspObj.addProperty("supply_type", HSNData.get(j)[2].toString());
//                    inspObj.addProperty("Tqty", parseDouble(HSNData.get(j)[3].toString()));
//                    inspObj.addProperty("total_amount",  parseDouble(HSNData.get(j)[4].toString()));
//                    inspObj.addProperty("tax_rate",  parseDouble(HSNData.get(j)[5].toString()));
//                    inspObj.addProperty("taxable_amt", parseDouble( HSNData.get(j)[6].toString()));
//                    inspObj.addProperty("igst_amt", parseDouble(HSNData.get(j)[7].toString()));
//                    inspObj.addProperty("cgst_amt", parseDouble(HSNData.get(j)[8].toString()));
//                    inspObj.addProperty("sgst_amt", parseDouble(HSNData.get(j)[9].toString()));
//                    inspObj.addProperty("tax_amt", parseDouble(HSNData.get(j)[10].toString()));
//                    inspObj.addProperty("hsn_id", HSNData.get(j)[11].toString());

                }
//                mArray.add(inspObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finalObject.addProperty("voucher_count",voucher_count);
        finalObject.addProperty("total_amt",T_invoice_amt);
        finalObject.addProperty("taxable_amt", T_taxable_amt);
        finalObject.addProperty("total_tax", T_tax_amt);
        finalObject.addProperty("total_igst", T_igst_amt !=null ? T_igst_amt : 0.0);
        finalObject.addProperty("total_cgst", T_cgst_amt !=null ? T_cgst_amt : 0.0);
        finalObject.addProperty("total_sgst", T_sgst_amt !=null ? T_sgst_amt : 0.0);
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    //    Api for GSTR1 nilrated and Exempted excel export
    public InputStream excelExportGSTR1Nilrated(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            System.out.println("productBatchNos size:" + jsonRequest);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"Sr.No", "voucher_type", "Particulars","nilrated Amt", "Exempted Amt"};
                    Sheet sheet = workbook.createSheet("GSTR1_nilRated_ExcelSheet");

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
                    Double sumOfTaxableAmt = 0.0, sumOfTaxableAmt1=0.0;

                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        System.out.println("batchNo=="+batchNo);
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(batchNo.get("voucher_type").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("ledger_name").getAsString());
                        if(batchNo.get("tax_type").getAsString().equalsIgnoreCase("nilrated")) {
                            row.createCell(3).setCellValue(batchNo.get("total_amount").getAsString());
                            if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Invoice")){
                                sumOfTaxableAmt += batchNo.get("total_amount").getAsDouble();
                            }
                            else{
                                sumOfTaxableAmt -= batchNo.get("total_amount").getAsDouble();
                            }

                        }
                        else{
                            row.createCell(4).setCellValue(batchNo.get("total_amount").getAsString());
                            if(batchNo.get("voucher_type").getAsString().equalsIgnoreCase("Sales Invoice")){
                                sumOfTaxableAmt1 += batchNo.get("total_amount").getAsDouble();
                            }
                            else{
                                sumOfTaxableAmt1 -= batchNo.get("total_amount").getAsDouble();
                            }
                        }
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");

                    prow.createCell(3).setCellValue(sumOfTaxableAmt);
                    prow.createCell(4).setCellValue(sumOfTaxableAmt1);


                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw
                    new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    public JsonObject getGSTR1HsnData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        System.out.println("users " + users.toString());
        JsonObject finalObject = new JsonObject();

        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;

        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            startDate = request.getParameter("start_date");
            endDate = request.getParameter("end_date");
            if(endDate!=null && !endDate.isEmpty() && startDate!=null && !startDate.isEmpty()){
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else{
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep !=null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");
        }
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        String query ="SELECT product_hsn_tbl.hsn_number, product_hsn_tbl.description, product_hsn_tbl.type, " +
                "SUM(tsidut.qty), SUM(tsit.total_amount), product_tbl.igst, SUM(tsit.taxable_amount), SUM(tsit.totaligst)," +
                "SUM(tsit.totalcgst), SUM(tsit.totalsgst), SUM(tsit.total_tax), product_hsn_tbl.id FROM `tranx_sales_invoice_details_units_tbl`" +
                " AS tsidut LEFT JOIN product_tbl ON tsidut.product_id = product_tbl.id LEFT JOIN tranx_sales_invoice_tbl AS " +
                "tsit ON tsidut.sales_invoice_id = tsit.id LEFT JOIN product_hsn_tbl ON product_tbl.hsn_id = product_hsn_tbl.id WHERE" +
                " tsidut.status=1 AND DATE(tsit.bill_date) " +
                "BETWEEN '" + startDatep +"' AND '" + endDatep + "' GROUP BY product_tbl.hsn_id";

        Query q = entityManager.createNativeQuery(query);
        System.out.println("q-===>"+query);
        List<Object[]> HSNData = q.getResultList();
        System.out.println("HSN data"+HSNData.size());
        try {
            for (int j = 0; j < HSNData.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + HSNData);
                if (HSNData != null) {
                    inspObj.addProperty("hsn_no", HSNData.get(j)[0].toString());
                    inspObj.addProperty("hsn_description", HSNData.get(j)[1].toString());
                    inspObj.addProperty("supply_type", HSNData.get(j)[2].toString());
                    inspObj.addProperty("Tqty", parseDouble(HSNData.get(j)[3].toString()));
                    inspObj.addProperty("total_amount",  parseDouble(HSNData.get(j)[4].toString()));
                    inspObj.addProperty("tax_rate",  parseDouble(HSNData.get(j)[5].toString()));
                    inspObj.addProperty("taxable_amt", parseDouble( HSNData.get(j)[6].toString()));
                    inspObj.addProperty("igst_amt", parseDouble(HSNData.get(j)[7].toString()));
                    inspObj.addProperty("cgst_amt", parseDouble(HSNData.get(j)[8].toString()));
                    inspObj.addProperty("sgst_amt", parseDouble(HSNData.get(j)[9].toString()));
                    inspObj.addProperty("tax_amt", parseDouble(HSNData.get(j)[10].toString()));
                    inspObj.addProperty("hsn_id", HSNData.get(j)[11].toString());

                }
                mArray.add(inspObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    public JsonObject getGSTR1HsnScreen2(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        System.out.println("users " + users.toString());
        JsonObject finalObject = new JsonObject();
        Long tax_rate = Long.valueOf(request.getParameter("tax_rate"));
        Long  hsn_id = Long.valueOf(request.getParameter("hsn_id"));

        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;

        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            startDate = request.getParameter("start_date");
            endDate = request.getParameter("end_date");
            if(endDate!=null && !endDate.isEmpty() && startDate!=null && !startDate.isEmpty()){
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else{
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep !=null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");
        }
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        String query ="SELECT DATE(tsit.bill_date), tsit.sales_invoice_no, ledger_master_tbl.ledger_name," +
                " ledger_gst_details_tbl.gstin, units_tbl.unit_name, tsit.total_base_amount, tsit.totaligst," +
                " tsit.totalcgst, tsit.totalsgst, tsit.total_tax, tsit.id FROM `tranx_sales_invoice_details_units_tbl` " +
                "AS tsidut LEFT JOIN product_tbl ON tsidut.product_id = product_tbl.id LEFT JOIN tranx_sales_invoice_tbl" +
                " AS tsit ON tsidut.sales_invoice_id = tsit.id LEFT JOIN ledger_master_tbl ON tsit.sundry_debtors_id " +
                "= ledger_master_tbl.id LEFT JOIN ledger_gst_details_tbl ON ledger_gst_details_tbl.ledger_id = " +
                "ledger_master_tbl.id LEFT JOIN units_tbl ON tsidut.unit_id= units_tbl.id WHERE " +
                "tsidut.status=1 AND product_tbl.hsn_id = " + hsn_id +
                " AND product_tbl.igst= "+ tax_rate + " AND DATE(tsit.bill_date) BETWEEN '" + startDatep +"' AND '" + endDatep + "' ";

        Query q = entityManager.createNativeQuery(query);
//        q.setParameter(1, 1);
//        q.setParameter(2, 5);
//        q.setParameter(3, startDatep);
//        q.setParameter(3, endDatep);

        System.out.println("q-===>"+query);
        List<Object[]> HSNData = q.getResultList();
        System.out.println("HSN-2 data"+HSNData.size());
        try {
            for (int j = 0; j < HSNData.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + HSNData.get(j));
                if (HSNData != null) {
                    inspObj.addProperty("invoice_date", HSNData.get(j)[0].toString());
                    inspObj.addProperty("invoice_no", HSNData.get(j)[1].toString());
                    inspObj.addProperty("ledger_name", HSNData.get(j)[2].toString());
                    inspObj.addProperty("gst_no", HSNData.get(j)[3] != null ? HSNData.get(j)[3].toString() : "");
                    inspObj.addProperty("voucher_type", "sales");
                    inspObj.addProperty("unit_name",  HSNData.get(j)[4].toString());
                    inspObj.addProperty("taxable_amt",  parseDouble(HSNData.get(j)[5].toString()));
                    inspObj.addProperty("total_igst", parseDouble( HSNData.get(j)[6].toString()));
                    inspObj.addProperty("total_cgst", parseDouble(HSNData.get(j)[7].toString()));
                    inspObj.addProperty("total_sgst", parseDouble(HSNData.get(j)[8].toString()));
                    inspObj.addProperty("total_tax", parseDouble(HSNData.get(j)[9].toString()));
                    inspObj.addProperty("id", parseLong(HSNData.get(j)[10].toString()));
                }
                mArray.add(inspObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }
}
