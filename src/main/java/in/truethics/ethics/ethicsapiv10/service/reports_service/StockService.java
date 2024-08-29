package in.truethics.ethics.ethicsapiv10.service.reports_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.InventoryCommonPostings;
import in.truethics.ethics.ethicsapiv10.common.UnitConversion;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.*;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.DoctorMaster;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.DoctorMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.*;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import in.truethics.ethics.ethicsapiv10.util.DateConvertUtil;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import javassist.CtClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.sound.midi.Soundbank;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

//import static antlr.FileLineFormatter.formatter;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

@Service
public class StockService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TranxPurReturnsRepository tranxPurReturnsRepository;
    @Autowired
    private TranxCounterSalesRepository tranxCounterSalesRepository;
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private TranxSalesReturnDetailsUnitsRepository tranxSalesReturnDetailsUnitsRepository;
    @Autowired
    private TranxSalesReturnRepository tranxSalesReturnRepository;
    @Autowired
    private ProductOpeningStocksRepository openingStocksRepository;
    @Autowired
    private TranxPurReturnDetailsUnitRepository tranxPurReturnDetailsUnitRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;

    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    private static final Logger productLogger = LogManager.getLogger(ProductService.class);
    @Autowired
    private TranxPurInvoiceDetailsUnitsRepository tranxPurInvoiceDetailsUnitsRepository;
    @Autowired
    private TranxSalesInvoiceDetailsUnitRepository tranxSalesInvoiceDetailsUnitRepository;
    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;

    @Autowired
    FiscalYearRepository fiscalYearRepository;

    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private TranxPurChallanRepository tranxPurChallanRepository;
    @Autowired
    private TranxSalesChallanRepository tranxSalesChallanRepository;
    @Autowired
    private TranxPurChallanDetailsUnitRepository tranxPurChallanDetailsUnitRepository;
    @Autowired
    private TranxSalesChallanDetailsUnitsRepository tranxSalesChallanDetailsUnitsRepository;
    @Autowired
    private TranxSalesCompInvoiceRepository tranxSalesCompInvoiceRepository;

    @Autowired
    private DoctorMasterRepository doctorMasterRepository;
    @Autowired
    private TranxCounterSalesDetailsRepository tranxCounterSalesDetailsRepository;
    @Autowired
    private TranxCSDetailsUnitsRepository tranxCSDetailsUnitsRepository;
    @Autowired
    private StockSummaryRepository summaryRepository;
    @Autowired
    private UnitConversion unitConversion;

    /****** WholeStock and Available Stock and Batch Stock product details ********/
    public JsonObject getWholeStockProducts(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<Product> productList = new ArrayList<>();
        List<InventoryDetailsPostings> inventoryDetailsPostings = new ArrayList<>();
        List<Object[]> list = new ArrayList<>();
        Long branchId = null;
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
            branchId = users.getBranch().getId();
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        for (Product mProduct : productList) {
            JsonObject mObject = new JsonObject();
            JsonArray productunitarray = new JsonArray();
            mObject.addProperty("id", mProduct.getId());
            mObject.addProperty("product_name", mProduct.getProductName());
            mObject.addProperty("product_code", mProduct.getProductCode());
            mObject.addProperty("brand_name", mProduct.getBrand().getBrandName());
            mObject.addProperty("packaging", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
            mObject.addProperty("companyName", mProduct.getOutlet().getCompanyName());
            mObject.addProperty("tax_per", mProduct.getTaxMaster().getIgst());

            //inventoryDetailsPostings = inventoryDetailsPostingsRepository.findProductsGroupByUnits(mProduct.getId(), true);
            list = inventoryDetailsPostingsRepository.findProductsGroupByUnits(mProduct.getId(), true);
            for (int j = 0; j < list.size(); j++) {
                Object[] objects = list.get(j);
                Long inventoryId = parseLong(objects[0].toString());
                InventoryDetailsPostings unitPacking = inventoryDetailsPostingsRepository.findByIdAndStatus(inventoryId, true);
                JsonObject productunitobject = new JsonObject();
                productunitobject.addProperty("row_id", unitPacking.getId());
                productunitobject.addProperty("unit_name", unitPacking.getUnits().getUnitName());
                productunitobject.addProperty("qty", unitPacking.getQty());
                productunitobject.addProperty("batchno", unitPacking.getUniqueBatchNo() != null ? unitPacking.getUniqueBatchNo() : "");
                productunitobject.addProperty("batchid", unitPacking.getProductBatch() != null ? unitPacking.getProductBatch().getId().toString() : "");
                productunitobject.addProperty("hsn", unitPacking.getProduct().getProductHsn().getHsnNumber());
                productunitobject.addProperty("group", unitPacking.getProduct().getGroup() != null ? unitPacking.getProduct().getGroup().getGroupName() : "");
                productunitobject.addProperty("subgroup", unitPacking.getProduct().getSubgroup() != null ? unitPacking.getProduct().getSubgroup().getSubgroupName() : "");
                productunitobject.addProperty("category", unitPacking.getProduct().getCategory() != null ? unitPacking.getProduct().getCategory().getCategoryName() : "");
                productunitobject.addProperty("tax_type", unitPacking.getProduct().getTaxType() != null ? unitPacking.getProduct().getTaxType() : "");
//                productunitobject.addProperty("tax_per", unitPacking.getProduct().getTaxMaster() != null ?unitPacking.getProduct().getTaxMaster().getIgst() : 0);
                productunitobject.addProperty("Shelf_id", unitPacking.getProduct().getShelfId() != null ? unitPacking.getProduct().getShelfId() : "");

                productunitobject.addProperty("purchase_rate", unitPacking.getProduct().getPurchaseRate());
                productunitobject.addProperty("Shelf_id", unitPacking.getProduct().getShelfId());
                productunitobject.addProperty("margin", unitPacking.getProduct().getMarginPer());
                productunitobject.addProperty("Cost", unitPacking.getProduct().getPurchaseRate());
//                productunitobject.addProperty("opening_qty",unitPacking.getProduct().get);

                //   ProductBatchNo productBatchNo = productBatchNoRepository.findByBatchNoAndStatus(unitPacking.getUniqueBatchNo(), true);
                productunitobject.addProperty("expiryDate", unitPacking.getProductBatch() != null ?
                        (unitPacking.getProductBatch().getExpiryDate() != null ? unitPacking.getProductBatch().getExpiryDate().toString() : "") : "");
                Long fiscalId = null;
                LocalDate currentDate = LocalDate.now();
                FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
                if (fiscalYear != null)
                    fiscalId = fiscalYear.getId();
                Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId,
                        users.getOutlet().getId(), mProduct.getId(), unitPacking.getLevelA() != null ?
                                unitPacking.getLevelA().getId() : null,
                        null, null, unitPacking.getUnits().getId(),
                        unitPacking.getProductBatch() != null ? unitPacking.getProductBatch().getId() : null, fiscalId);
                productunitobject.addProperty("closing_stock", closing);
                productunitarray.add(productunitobject);
            }

            mObject.add("product_unit_data", productunitarray);
            jsonArray.add(mObject);
        }
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }


    //API for stockReport screen 1
    public InputStream exportExcelWholeStock1(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"CODE", "PRODUCT NAME", "PACKING", "BRAND ", "BATCH", "EXPIRY DATE", "UNIT", "QUANTITY"};

                    Sheet sheet = workbook.createSheet("excelExportWholeStock1");

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

//                    int sumOfQty = 0;
//                    int sumOfSaleQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject productObject = productBatchNos.get(i).getAsJsonObject();
                        JsonArray batchArray = productObject.get("product_unit_data").getAsJsonArray();
                        System.out.println("batchNo  " + productObject);
                        JsonObject prodUnit = null;

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(productObject.get("item_code").getAsString());
                        row.createCell(1).setCellValue(productObject.get("product_name").getAsString());
                        row.createCell(2).setCellValue(productObject.get("packaging").getAsString());
                        row.createCell(3).setCellValue(productObject.get("brand_name").getAsString());

                        if (batchArray.size() > 0) {
                            for (int j = 0; j < batchArray.size(); j++) {
                                Row brow = sheet.createRow(rowIdx++);
                                JsonObject batchobject = batchArray.get(j).getAsJsonObject();
                                JsonArray unitArray = batchobject.get("batch_unit_data").getAsJsonArray();
                                brow.createCell(4).setCellValue(batchobject.get("batchno").getAsString());
                                brow.createCell(5).setCellValue(batchobject.get("expiry_date").getAsString());
//
//                                row.createCell(4).setCellValue("");

                                if (batchArray.size() > 0) {
                                    for (int k = 0; k < unitArray.size(); k++) {
                                        Row prow = sheet.createRow(rowIdx++);
                                        JsonObject unitobject = unitArray.get(k).getAsJsonObject();
                                        prow.createCell(6).setCellValue(unitobject.get("unit_name").getAsString());
                                        prow.createCell(7).setCellValue(unitobject.get("unit_qty").getAsString());

                                    }
                                }

                            }

                        }
                        rowIdx++;
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            //    System.out.println("wholeStock1ExcelExport" + out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public JsonObject getNarcoticProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesCompInvoiceDetailsUnits> detailsUnitsList = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        JsonObject finalResult = new JsonObject();
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

        String sql = "SELECT * FROM tranx_sales_comp_details_units_tbl AS sdut left join tranx_sales_comp_invoice_tbl AS tscit on" +
                " sdut.sales_invoice_id=tscit.id left join product_tbl AS pt on sdut.product_id=pt.id WHERE" +
                " tscit.outlet_id=" + users.getOutlet().getId() + " AND pt.drug_type=3 AND tscit.bill_date BETWEEN '" + startDatep + "' AND '" + endDatep + "' ";
        System.out.println(sql);


        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }
        JsonArray jsonArray = null;
        jsonArray = new JsonArray();

//        while (startDatep.isBefore(endDatep)) {
        if (users.getBranch() != null)
            sql += " AND sdut.branch_id=" + users.getBranch().getId();

        Query q = entityManager.createNativeQuery(sql, TranxSalesCompInvoiceDetailsUnits.class);
        detailsUnitsList = q.getResultList();


        for (TranxSalesCompInvoiceDetailsUnits detailsUnits : detailsUnitsList) {
            JsonObject mObject = new JsonObject();
            Product product = productRepository.findByIdAndStatus(detailsUnits.getProductId(), true);
            ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(detailsUnits.getProductBatchNoId(), true);
            TranxSalesCompInvoice compInvoice = tranxSalesCompInvoiceRepository.findByIdAndStatus(detailsUnits.getSalesInvoiceId(), true);
            DoctorMaster doctorMaster = doctorMasterRepository.findByIdAndStatus(compInvoice.getDoctorId(), true);


            mObject.addProperty("id", detailsUnits.getId());
            mObject.addProperty("Date", compInvoice.getBillDate().toString());
            mObject.addProperty("product_name", product.getProductName());
            mObject.addProperty("invoiceNo", compInvoice.getSalesInvoiceNo());
            mObject.addProperty("DoctorName", doctorMaster.getDoctorName());
            mObject.addProperty("Address", compInvoice.getClientName());
            mObject.addProperty("patient", compInvoice.getClientAddress());
            mObject.addProperty("qty", detailsUnits.getQty());
            mObject.addProperty("batchNo", productBatchNo.getBatchNo());
            mObject.addProperty("brand_name", product.getBrand().getBrandName());
            mObject.addProperty("rate", detailsUnits.getRate());
            mObject.addProperty("expiryDate", productBatchNo.getExpiryDate().toString());
            jsonArray.add(mObject);
        }


//        }
        System.out.println("lst" + jsonArray);
        finalResult.addProperty("d_start_date", currentStartDate.toString());
        finalResult.addProperty("d_end_date", currentEndDate.toString());
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

    public JsonObject getNarcoticPurProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurInvoiceDetailsUnits> detailsUnitsList = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        JsonObject finalResult = new JsonObject();
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

        String sql = "SELECT * FROM tranx_purchase_invoice_details_units_tbl AS sdut left join tranx_purchase_invoice_tbl AS tscit on" +
                " sdut.purchase_invoice_id=tscit.id left join product_tbl AS pt on sdut.product_id=pt.id WHERE" +
                " tscit.outlet_id=" + users.getOutlet().getId() + " AND pt.drug_type=3 AND tscit.invoice_date BETWEEN '" + startDatep + "' AND '" + endDatep + "' ";

        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }
        JsonArray jsonArray = null;
        jsonArray = new JsonArray();

//        while (startDatep.isBefore(endDatep)) {
        if (users.getBranch() != null)
            sql += " AND sdut.branch_id=" + users.getBranch().getId();

        Query q = entityManager.createNativeQuery(sql, TranxPurInvoiceDetailsUnits.class);
        detailsUnitsList = q.getResultList();

//        JsonObject finalResult = new JsonObject();
//        JsonArray jsonArray = new JsonArray();

        for (TranxPurInvoiceDetailsUnits detailsUnits : detailsUnitsList) {
            JsonObject mObject = new JsonObject();
            Product product = productRepository.findByIdAndStatus(detailsUnits.getProduct().getId(), true);
            ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(detailsUnits.getProductBatchNo().getId(), true);
            TranxPurInvoiceDetailsUnits compInvoice = tranxPurInvoiceDetailsUnitsRepository.findByIdAndStatus(detailsUnits.getId(), true);
//            DoctorMaster doctorMaster = doctorMasterRepository.findByIdAndStatus(compInvoice.getDoctorId(), true);


            mObject.addProperty("id", detailsUnits.getId());
            mObject.addProperty("Date", compInvoice != null ? compInvoice.getPurchaseTransaction().getInvoiceDate().toString() : "");
            mObject.addProperty("product_name", product.getProductName());
            mObject.addProperty("invoiceNo", compInvoice != null ? compInvoice.getPurchaseTransaction().getVendorInvoiceNo() : "");
            mObject.addProperty("ledger_name", compInvoice != null ? compInvoice.getPurchaseTransaction().getSundryCreditors().getLedgerName() : "");
//            mObject.addProperty("patient", compInvoice.getClientAddress());
            mObject.addProperty("qty", detailsUnits.getQty());
            mObject.addProperty("batchNo", productBatchNo.getBatchNo());
            mObject.addProperty("brand_name", product.getBrand().getBrandName());
            mObject.addProperty("rate", detailsUnits.getRate());
            mObject.addProperty("expiryDate", productBatchNo != null ? productBatchNo.getExpiryDate().toString() : "");
            jsonArray.add(mObject);
        }
        finalResult.addProperty("d_start_date", currentStartDate.toString());
        finalResult.addProperty("d_end_date", currentEndDate.toString());
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

    public JsonObject getScheduleH1Product(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesCompInvoiceDetailsUnits> detailsUnitsList = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        JsonObject finalResult = new JsonObject();
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


        String sql = "SELECT * FROM tranx_sales_comp_details_units_tbl AS sdut left join tranx_sales_comp_invoice_tbl AS tscit on" +
                " sdut.sales_invoice_id=tscit.id left join product_tbl AS pt on sdut.product_id=pt.id WHERE" +
                " tscit.outlet_id=" + users.getOutlet().getId() + " AND pt.drug_type=2 AND tscit.bill_date BETWEEN '" + startDatep + "' AND '" + endDatep + "' ";
        System.out.println(sql);


        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }
        JsonArray jsonArray = null;
        jsonArray = new JsonArray();

//        while (startDatep.isBefore(endDatep)) {
        if (users.getBranch() != null)
            sql += " AND sdut.branch_id=" + users.getBranch().getId();


        Query q = entityManager.createNativeQuery(sql, TranxSalesCompInvoiceDetailsUnits.class);
        detailsUnitsList = q.getResultList();

//        JsonObject finalResult = new JsonObject();
//        JsonArray jsonArray = new JsonArray();

        for (TranxSalesCompInvoiceDetailsUnits detailsUnits : detailsUnitsList) {
            JsonObject mObject = new JsonObject();
            Product product = productRepository.findByIdAndStatus(detailsUnits.getProductId(), true);
            ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(detailsUnits.getProductBatchNoId(), true);
            TranxSalesCompInvoice compInvoice = tranxSalesCompInvoiceRepository.findByIdAndStatus(detailsUnits.getSalesInvoiceId(), true);
            DoctorMaster doctorMaster = doctorMasterRepository.findByIdAndStatus(compInvoice.getDoctorId(), true);


            mObject.addProperty("id", detailsUnits.getId());
            mObject.addProperty("Date", compInvoice.getBillDate().toString());
            mObject.addProperty("product_name", product.getProductName());
            mObject.addProperty("invoiceNo", compInvoice.getSalesInvoiceNo());
            mObject.addProperty("DoctorName", doctorMaster.getDoctorName());
            mObject.addProperty("Address", compInvoice.getClientName());
            mObject.addProperty("patient", compInvoice.getClientAddress());
            mObject.addProperty("qty", detailsUnits.getQty());
            mObject.addProperty("batchNo", productBatchNo.getBatchNo());
            mObject.addProperty("brand_name", product.getBrand().getBrandName());
            mObject.addProperty("rate", detailsUnits.getRate());
            mObject.addProperty("expiryDate", productBatchNo.getExpiryDate().toString());


            jsonArray.add(mObject);
        }
        finalResult.addProperty("d_start_date", currentStartDate.toString());
        finalResult.addProperty("d_end_date", currentEndDate.toString());
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

    public JsonObject getScheduleH1PurProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurInvoiceDetailsUnits> detailsUnitsList = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        JsonObject finalResult = new JsonObject();
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


        String sql = "SELECT * FROM tranx_purchase_invoice_details_units_tbl AS sdut left join tranx_purchase_invoice_tbl AS tscit on" +
                " sdut.purchase_invoice_id=tscit.id left join product_tbl AS pt on sdut.product_id=pt.id WHERE" +
                " tscit.outlet_id=" + users.getOutlet().getId() + " AND pt.drug_type=2 AND tscit.invoice_date BETWEEN '" + startDatep + "' AND '" + endDatep + "' ";
        System.out.println(sql);


        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }
        JsonArray jsonArray = null;
        jsonArray = new JsonArray();

//        while (startDatep.isBefore(endDatep)) {
        if (users.getBranch() != null)
            sql += " AND sdut.branch_id=" + users.getBranch().getId();


        Query q = entityManager.createNativeQuery(sql, TranxPurInvoiceDetailsUnits.class);
        detailsUnitsList = q.getResultList();

//        JsonObject finalResult = new JsonObject();
//        JsonArray jsonArray = new JsonArray();

        for (TranxPurInvoiceDetailsUnits detailsUnits : detailsUnitsList) {
            JsonObject mObject = new JsonObject();
            Product product = productRepository.findByIdAndStatus(detailsUnits.getProduct().getId(), true);
            ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(detailsUnits.getProductBatchNo().getId(), true);
            TranxPurInvoiceDetailsUnits compInvoice = tranxPurInvoiceDetailsUnitsRepository.findByIdAndStatus(detailsUnits.getId(), true);
//            DoctorMaster doctorMaster = doctorMasterRepository.findByIdAndStatus(compInvoice.getDoctorId(), true);


            mObject.addProperty("id", detailsUnits.getId());
            mObject.addProperty("Date", compInvoice != null ? compInvoice.getPurchaseTransaction().getInvoiceDate().toString() : "");
            mObject.addProperty("product_name", product.getProductName());
            mObject.addProperty("invoiceNo", compInvoice != null ? compInvoice.getPurchaseTransaction().getVendorInvoiceNo() : "");
            mObject.addProperty("ledger_name", compInvoice != null ? compInvoice.getPurchaseTransaction().getSundryCreditors().getLedgerName() : "");
//            mObject.addProperty("patient", compInvoice.getClientAddress());
            mObject.addProperty("qty", detailsUnits.getQty());
            mObject.addProperty("batchNo", productBatchNo.getBatchNo());
            mObject.addProperty("brand_name", product.getBrand().getBrandName());
            mObject.addProperty("rate", detailsUnits.getRate());
            mObject.addProperty("expiryDate", productBatchNo != null ? productBatchNo.getExpiryDate().toString() : "");
            jsonArray.add(mObject);
        }

        finalResult.addProperty("d_start_date", currentStartDate.toString());
        finalResult.addProperty("d_end_date", currentEndDate.toString());
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

    public JsonObject getScheduleHPurProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurInvoiceDetailsUnits> detailsUnitsList = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        JsonObject finalResult = new JsonObject();
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


        String sql = "SELECT * FROM tranx_purchase_invoice_details_units_tbl AS sdut left join tranx_purchase_invoice_tbl AS tscit on" +
                " sdut.purchase_invoice_id=tscit.id left join product_tbl AS pt on sdut.product_id=pt.id WHERE" +
                " tscit.outlet_id=" + users.getOutlet().getId() + " AND pt.drug_type=1 AND tscit.invoice_date BETWEEN '" + startDatep + "' AND '" + endDatep + "' ";
//        System.out.println(sql);


        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }
        JsonArray jsonArray = null;
        jsonArray = new JsonArray();

        if (users.getBranch() != null)
            sql += " AND sdut.branch_id=" + users.getBranch().getId();

        Query q = entityManager.createNativeQuery(sql, TranxPurInvoiceDetailsUnits.class);
        detailsUnitsList = q.getResultList();

//        JsonObject finalResult = new JsonObject();
//        JsonArray jsonArray = new JsonArray();

        for (TranxPurInvoiceDetailsUnits detailsUnits : detailsUnitsList) {
            JsonObject mObject = new JsonObject();
            Product product = productRepository.findByIdAndStatus(detailsUnits.getProduct().getId(), true);
            ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(detailsUnits.getProductBatchNo().getId(), true);
            TranxPurInvoiceDetailsUnits compInvoice = tranxPurInvoiceDetailsUnitsRepository.findByIdAndStatus(detailsUnits.getId(), true);
            mObject.addProperty("id", detailsUnits.getId());
            mObject.addProperty("invoice_date", compInvoice != null ? compInvoice.getPurchaseTransaction().getInvoiceDate().toString() : "");
            mObject.addProperty("product_name", product.getProductName());
            mObject.addProperty("invoiceNo", compInvoice != null ? compInvoice.getPurchaseTransaction().getVendorInvoiceNo() : "");
            mObject.addProperty("ledger_name", compInvoice != null ? compInvoice.getPurchaseTransaction().getSundryCreditors().getLedgerName() : "");
//            mObject.addProperty("patient", compInvoice.getClientAddress());
            mObject.addProperty("qty", detailsUnits.getQty());
            mObject.addProperty("batchNo", productBatchNo.getBatchNo());
            mObject.addProperty("brand_name", product.getBrand().getBrandName());
            mObject.addProperty("rate", detailsUnits.getRate());
            mObject.addProperty("expiryDate", productBatchNo != null ? productBatchNo.getExpiryDate().toString() : "");
            jsonArray.add(mObject);
        }
//        System.out.println("lst"+jsonArray);
        finalResult.addProperty("d_start_date", currentStartDate.toString());
        finalResult.addProperty("d_end_date", currentEndDate.toString());
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

    public JsonObject getScheduleHProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesCompInvoiceDetailsUnits> detailsUnitsList = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        JsonObject finalResult = new JsonObject();
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

        String sql = "SELECT * FROM tranx_sales_comp_details_units_tbl AS sdut left join tranx_sales_comp_invoice_tbl AS tscit on" +
                " sdut.sales_invoice_id=tscit.id left join product_tbl AS pt on sdut.product_id=pt.id WHERE" +
                " tscit.outlet_id=" + users.getOutlet().getId() + " AND pt.drug_type=1 AND tscit.bill_date BETWEEN '" + startDatep + "' AND '" + endDatep + "' ";
        System.out.println(sql);


        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }
        JsonArray jsonArray = null;
        jsonArray = new JsonArray();

//        while (startDatep.isBefore(endDatep)) {
        if (users.getBranch() != null)
            sql += " AND sdut.branch_id=" + users.getBranch().getId();

        Query q = entityManager.createNativeQuery(sql, TranxSalesCompInvoiceDetailsUnits.class);
        detailsUnitsList = q.getResultList();

//        JsonObject finalResult = new JsonObject();
//        JsonArray jsonArray = new JsonArray();

        for (TranxSalesCompInvoiceDetailsUnits detailsUnits : detailsUnitsList) {
            JsonObject mObject = new JsonObject();
            Product product = productRepository.findByIdAndStatus(detailsUnits.getProductId(), true);
            ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(detailsUnits.getProductBatchNoId(), true);
            TranxSalesCompInvoice compInvoice = tranxSalesCompInvoiceRepository.findByIdAndStatus(detailsUnits.getSalesInvoiceId(), true);
            DoctorMaster doctorMaster = doctorMasterRepository.findByIdAndStatus(compInvoice.getDoctorId(), true);


            mObject.addProperty("id", detailsUnits.getId());
            mObject.addProperty("Date", compInvoice.getBillDate().toString());
            mObject.addProperty("product_name", product.getProductName());
            mObject.addProperty("invoiceNo", compInvoice.getSalesInvoiceNo());
            mObject.addProperty("DoctorName", doctorMaster.getDoctorName());
            mObject.addProperty("Address", compInvoice.getClientName());
            mObject.addProperty("patient", compInvoice.getClientAddress());
            mObject.addProperty("qty", detailsUnits.getQty());
            mObject.addProperty("batchNo", productBatchNo.getBatchNo());
            mObject.addProperty("brand_name", product.getBrand().getBrandName());
            mObject.addProperty("rate", detailsUnits.getRate());
            mObject.addProperty("expiryDate", productBatchNo.getExpiryDate().toString());


            jsonArray.add(mObject);
        }

        finalResult.addProperty("d_start_date", currentStartDate.toString());
        finalResult.addProperty("d_end_date", currentEndDate.toString());
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

    /****** WholeStock and Available Stock Monthwise product details ********/
    public Object getMonthwiseWholeStockDetails(HttpServletRequest request) {
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
            Long productId = Long.valueOf(request.getParameter("productId"));
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            //****This Code For Users Dates Selection Between Start And End Date Manually****//
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
            JsonArray mainArr = new JsonArray();
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject monthObject = new JsonObject();
                JsonArray monthPurArray = new JsonArray();
                JsonObject mainObj = new JsonObject();
                JsonArray saleArray = new JsonArray();
                JsonArray closingArray = new JsonArray();
                Double closing_bal = 0.0;
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate); //**  If You Want To Print  All Start And End Date of each month  between Fiscal Year **//
                startDatep = endMonthDate.plusDays(1);
                System.out.println();

//                List<ProductUnitPacking> productUnitPackings = productUnitRepository.findByProductIdAndStatus(productId, true);
//                for (ProductUnitPacking mUnits : productUnitPackings) {
//                    Double totalQty = 0.0;
//                    Double totalpValue = 0.0;
//                    Double totalsValue = 0.0;
//                    Double totalClosingqty = 0.0;
//                    Double totalpQty = 0.0;
//                    Double totalsQty = 0.0;
//                    /*****  Purchase *******/
//                    JsonObject inside = new JsonObject();
////                    totalpQty = inventoryDetailsPostingsRepository.
////                            findByTotalQty(productId, 1L, mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());
////                    if (mUnits.getProduct().getIsBatchNumber())
////                        totalpValue = productBatchNoRepository.findPurchaseTotalVale(productId, mUnits.getUnits().getId());
////                    else
////                        totalpValue = tranxPurInvoiceDetailsUnitsRepository.findTotalValue(productId, mUnits.getUnits().getId());
////                    System.out.println("totalValue" + totalpValue);
//
//                    inside.addProperty("qty", totalpQty);
//                    inside.addProperty("unit", mUnits.getUnits().getUnitName());
//                    inside.addProperty("value", totalpValue);
//                    monthPurArray.add(inside);
//
//                    /****** Sales ****/
//                    JsonObject saleInside = new JsonObject();
////                    totalsQty = inventoryDetailsPostingsRepository.
////                            findBySaleTotalQty(productId, 3L, mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());
////                    if (mUnits.getProduct().getIsBatchNumber())
////                        totalsValue = productBatchNoRepository.findTotalVale(productId, mUnits.getUnits().getId());
////                    else
////                        totalsValue = tranxSalesInvoiceDetailsUnitRepository.findTotalValue(productId, mUnits.getUnits().getId());
////                    System.out.println("totalValue" + totalsValue);
//
//                    saleInside.addProperty("qty", totalsQty);
//                    saleInside.addProperty("unit", mUnits.getUnits().getUnitName());
//                    saleInside.addProperty("value", totalsValue);
//                    saleArray.add(saleInside);
//
//                    JsonObject closingInside = new JsonObject();
//                    totalClosingqty = totalpQty - totalsQty;
//
//                    closingInside.addProperty("qty", totalClosingqty);
//                    closingInside.addProperty("unit", mUnits.getUnits().getUnitName());
//                    closingInside.addProperty("value", totalClosingqty * totalpValue);
//                    closingArray.add(closingInside);
//                }
                List<Object[]> units = productUnitRepository.findUnitName();
                List<Object[]> purData = productUnitRepository.findPurQtyAndRateByProductIdAndTranxTypeId(productId, 1, startMonthDate, endMonthDate);
                List<Object[]> purRetData = productUnitRepository.findPurRetQtyAndRateByProductIdAndTranxTypeId(productId, 2, startMonthDate, endMonthDate);
                List<Object[]> purChallData = productUnitRepository.findPurChallQtyAndRateByProductIdAndTranxTypeId(productId, 12, startMonthDate, endMonthDate);
                List<Object[]> salesData = productUnitRepository.findsalesQtyAndRateByProductIdAndTranxTypeId(productId, 3, startMonthDate, endMonthDate);
                List<Object[]> salesRetData = productUnitRepository.findsallesRetQtyAndRateByProductIdAndTranxTypeId(productId, 4, startMonthDate, endMonthDate);
                List<Object[]> salesChallData = productUnitRepository.findSalesChallQtyAndRateByProductIdAndTranxTypeId(productId, 15, startMonthDate, endMonthDate);

                for (int ut = 0; ut < units.size(); ut++) {
                    String unit = units.get(ut)[0].toString();
                    System.out.println("unit   >>> " + unit);
                    Double purQty = 0.0, purValue = 0.0;
                    for (int i = 0; i < purData.size(); i++) {
                        if (purData != null) {
                            String unit1 = purData.get(i)[0].toString();
                            Double purQty1 = parseDouble(purData.get(i)[1].toString());
                            Double purValue1 = parseDouble(purData.get(i)[2].toString());
                            System.out.println("unit1 " + unit1 + "purQty1 " + purQty1 + "purValue1 " + purValue1);
                            if (unit.equalsIgnoreCase(unit1)) {
                                purQty = purQty + purQty1;
                                purValue = purValue + purValue1;
                            }
                        }
                    }
                    for (int j = 0; j < purRetData.size(); j++) {
                        if (purRetData != null) {
                            String unit2 = purRetData.get(j)[0].toString();
                            Double purretQty = parseDouble(purRetData.get(j)[1].toString());
                            Double purRetValue = parseDouble(purRetData.get(j)[2].toString());
                            System.out.println("unit2 " + unit2 + "purretQty " + purretQty + "purRetValue " + purRetValue);
                            if (unit.equalsIgnoreCase(unit2)) {
                                purQty = purQty - purretQty;
                                purValue = purValue - purRetValue;
                            }
                        }
                    }
                    for (int k = 0; k < purChallData.size(); k++) {
                        if (purChallData != null) {
                            String unit3 = purChallData.get(k)[0].toString();
                            Double purChallQty = parseDouble(purChallData.get(k)[1].toString());
                            Double purChallValue = parseDouble(purChallData.get(k)[2].toString());
                            System.out.println("unit3 " + unit3 + "purQty1 " + purChallQty + "purChallQty " + purChallValue);
                            if (unit.equalsIgnoreCase(unit3)) {
                                purQty = purQty + purChallQty;
                                purValue = purValue + purChallValue;
                            }
                        }
                    }
                    JsonObject inside = new JsonObject();
                    inside.addProperty("qty", purQty);
                    inside.addProperty("unit", unit);
                    inside.addProperty("value", purValue);
                    monthPurArray.add(inside);
                }

                for (int ut = 0; ut < units.size(); ut++) {
                    String unit = units.get(ut)[0].toString();
                    System.out.println("unit   >>> " + unit);
                    Double salesQty = 0.0, salesValue = 0.0;
                    for (int i = 0; i < salesData.size(); i++) {
                        if (salesData != null) {
                            String unit1 = salesData.get(i)[0].toString();
                            Double salesQty1 = parseDouble(salesData.get(i)[1].toString());
                            Double salesValue1 = parseDouble(salesData.get(i)[2].toString());
                            System.out.println("unit1 " + unit1 + "salesQty1 " + salesQty1 + "salesValue1 " + salesValue1);
                            if (unit.equalsIgnoreCase(unit1)) {
                                salesQty = salesQty + salesQty1;
                                salesValue = salesValue + salesValue1;
                            }
                        }
                    }
                    for (int j = 0; j < salesRetData.size(); j++) {
                        if (salesRetData != null) {
                            String unit2 = salesRetData.get(j)[0].toString();
                            Double salesretQty = parseDouble(salesRetData.get(j)[1].toString());
                            Double salesRetValue = parseDouble(salesRetData.get(j)[2].toString());
                            System.out.println("unit2 " + unit2 + "salesretQty " + salesretQty + "salesRetValue " + salesRetValue);
                            if (unit.equalsIgnoreCase(unit2)) {
                                salesQty = salesQty - salesretQty;
                                salesValue = salesValue - salesRetValue;
                            }
                        }
                    }
                    for (int k = 0; k < salesChallData.size(); k++) {
                        if (salesChallData != null) {
                            String unit3 = salesChallData.get(k)[0].toString();
                            Double salesChallQty = parseDouble(salesChallData.get(k)[1].toString());
                            Double salesChallValue = parseDouble(salesChallData.get(k)[2].toString());
                            System.out.println("unit3 " + unit3 + "salesChallQty " + salesChallQty + "salesChallValue " + salesChallValue);
                            if (unit.equalsIgnoreCase(unit3)) {
                                salesQty = salesQty + salesChallQty;
                                salesValue = salesValue + salesChallValue;
                            }
                        }
                    }
                    JsonObject inside = new JsonObject();
                    inside.addProperty("qty", salesQty);
                    inside.addProperty("unit", unit);
                    inside.addProperty("value", salesValue);
                    saleArray.add(inside);
                }

                mainObj.add("purchase", monthPurArray);
                mainObj.add("sale", saleArray);
                mainObj.add("closing", closingArray);

                monthObject.addProperty("month_name", month);
                monthObject.addProperty("start_date", startMonthDate.toString());
                monthObject.addProperty("end_date", endMonthDate.toString());
                monthObject.add("responseObject", mainObj);
                innerArr.add(monthObject);
            }
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
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

//    public Object getMonthwiseWholeStockDetails(HttpServletRequest request) {
//        JsonObject res = new JsonObject();
//        Long id = 0L;
//        Double total_month_sum = 0.0;
//        Double credit_total = 0.0;
//
//        List<Object[]> list = new ArrayList<>();
//        try {
//            Map<String, String[]> paramMap = request.getParameterMap();
//            String endDate = null;
//            LocalDate endDatep = null;
//            String startDate = null;
//            LocalDate startDatep = null;
//            Double opening_bal = 0.0;
//            Long productId = Long.valueOf(request.getParameter("productId"));
//            LocalDate currentStartDate = null;
//            LocalDate currentEndDate = null;
//            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//            //****This Code For Users Dates Selection Between Start And End Date Manually****//
//            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
//                endDate = request.getParameter("end_date");
//                endDatep = LocalDate.parse(endDate);
//                startDate = request.getParameter("start_date");
//                startDatep = LocalDate.parse(startDate);
//
//            } else {
//
//                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
//                if (fiscalYear != null) {
//                    startDatep = fiscalYear.getDateStart();
//                    endDatep = fiscalYear.getDateEnd();
//                }
//
//            }
//            currentStartDate = startDatep;
//            currentEndDate = endDatep;
//            if (startDatep.isAfter(endDatep)) {
//                System.out.println("Start Date Should not be After");
//                return 0;
//            }
//            JsonArray mainArr = new JsonArray();
//            JsonArray innerArr = new JsonArray();
//            while (startDatep.isBefore(endDatep)) {
//                JsonObject monthObject = new JsonObject();
//                JsonArray monthArray = new JsonArray();
//                JsonObject mainObj = new JsonObject();
//                JsonArray saleArray = new JsonArray();
//                JsonArray closingArray = new JsonArray();
//                Double closing_bal = 0.0;
//                String month = startDatep.getMonth().name();
//                System.out.println();
//                LocalDate startMonthDate = startDatep;
//                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
//                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate); //**  If You Want To Print  All Start And End Date of each month  between Fiscal Year **//
//                startDatep = endMonthDate.plusDays(1);
//                System.out.println();
//
//                List<ProductUnitPacking> productUnitPackings = productUnitRepository.findByProductIdAndStatus(productId, true);
//                for (ProductUnitPacking mUnits : productUnitPackings) {
//                    Double totalQty = 0.0;
//                    Double totalpValue = 0.0;
//                    Double totalsValue = 0.0;
//                    Double totalClosingqty = 0.0;
//                    Double totalpQty = 0.0;
//                    Double totalsQty = 0.0;
//                    /*****  Purchase *******/
//                    JsonObject inside = new JsonObject();
//                    totalpQty = inventoryDetailsPostingsRepository.
//                            findByTotalQty(productId, 1L, mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());
//                    if (mUnits.getProduct().getIsBatchNumber())
//                        totalpValue = productBatchNoRepository.findPurchaseTotalVale(productId, mUnits.getUnits().getId());
//                    else
//                        totalpValue = tranxPurInvoiceDetailsUnitsRepository.findTotalValue(productId, mUnits.getUnits().getId());
//                    System.out.println("totalValue" + totalpValue);
//
//                    inside.addProperty("qty", totalpQty);
//                    inside.addProperty("unit", mUnits.getUnits().getUnitName());
//                    inside.addProperty("value", totalpValue);
//                    monthArray.add(inside);
//
//                    /****** Sales ****/
//                    JsonObject saleInside = new JsonObject();
//                    totalsQty = inventoryDetailsPostingsRepository.
//                            findBySaleTotalQty(productId, 3L, mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());
//                    if (mUnits.getProduct().getIsBatchNumber())
//                        totalsValue = productBatchNoRepository.findTotalVale(productId, mUnits.getUnits().getId());
//                    else
//                        totalsValue = tranxSalesInvoiceDetailsUnitRepository.findTotalValue(productId, mUnits.getUnits().getId());
//                    System.out.println("totalValue" + totalsValue);
//
//                    saleInside.addProperty("qty", totalsQty);
//                    saleInside.addProperty("unit", mUnits.getUnits().getUnitName());
//                    saleInside.addProperty("value", totalsValue);
//                    saleArray.add(saleInside);
//
//                    JsonObject closingInside = new JsonObject();
//                    totalClosingqty = totalpQty - totalsQty;
//
//                    closingInside.addProperty("qty", totalClosingqty);
//                    closingInside.addProperty("unit", mUnits.getUnits().getUnitName());
//                    closingInside.addProperty("value", totalClosingqty * totalpValue);
//                    closingArray.add(closingInside);
//                }
//
//                mainObj.add("purchase", monthArray);
//                mainObj.add("sale", saleArray);
//                mainObj.add("closing", closingArray);
//
//                monthObject.addProperty("month_name", month);
//                monthObject.addProperty("start_date", startMonthDate.toString());
//                monthObject.addProperty("end_date", endMonthDate.toString());
//                monthObject.add("responseObject", mainObj);
//                innerArr.add(monthObject);
//            }
//            res.addProperty("d_start_date", currentStartDate.toString());
//            res.addProperty("d_end_date", currentEndDate.toString());
//            res.add("response", innerArr);
//            res.addProperty("opening_bal", opening_bal);
//            res.addProperty("responseStatus", HttpStatus.OK.value());
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.addProperty("message", "Failed To Load Data");
//            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
//        return res;
//
//    }

    /****** Batch wise Monthwise product details ********/
    public Object getMonthwiseBatchStockDetails(HttpServletRequest request) {
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
            Long productId = Long.valueOf(request.getParameter("productId"));
            String batchNo = request.getParameter("batchno");
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            //****This Code For Users Dates Selection Between Start And End Date Manually****//
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);

            } else {
                //****This Code For Load Data Default Current Year From Automatically load And Select Fiscal Year From Fiscal Year Table****//
                /*List<Object[]> nlist = new ArrayList<>();
                nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();
                for (int i = 0; i < nlist.size(); i++) {
                    Object obj[] = nlist.get(i);
                    System.out.println("start Date:" + obj[0].toString());
                    System.out.println("end Date:" + obj[1].toString());
                    startDatep = LocalDate.parse(obj[0].toString());
                    endDatep = LocalDate.parse(obj[1].toString());
                }*/
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
            JsonArray mainArr = new JsonArray();
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject monthObject = new JsonObject();
                JsonArray monthArray = new JsonArray();
                JsonObject mainObj = new JsonObject();
                JsonArray saleArray = new JsonArray();
                JsonArray closingArray = new JsonArray();
                Double closing_bal = 0.0;
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate); //**  If You Want To Print  All Start And End Date of each month  between Fiscal Year **//
                startDatep = endMonthDate.plusDays(1);
                System.out.println();

                List<ProductUnitPacking> productUnitPackings = productUnitRepository.findByProductIdAndStatus(productId, true);
                for (ProductUnitPacking mUnits : productUnitPackings) {
                    Double totalQty = 0.0;
                    Double totalpurBValue = 0.0;
                    Double totalsaleBValue = 0.0;
                    Double totalClosingqty = 0.0;
                    Double totalpurBQty = 0.0;
                    Double totalsaleBQty = 0.0;
                    /*****  Purchase *******/
                    JsonObject inside = new JsonObject();
                    totalpurBQty = inventoryDetailsPostingsRepository.
                            findByTotalBatchQty(productId, 1L, mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString(), batchNo);
                    totalpurBValue = productBatchNoRepository.findPurchaseBatchTotalVale(productId, mUnits.getUnits().getId(), batchNo);
                    System.out.println("totalValue" + totalpurBValue);

                    inside.addProperty("qty", totalpurBQty);
                    inside.addProperty("unit", mUnits.getUnits().getUnitName());
                    inside.addProperty("value", totalpurBValue);
                    monthArray.add(inside);

                    /****** Sales ****/
                    JsonObject saleInside = new JsonObject();
                    totalsaleBQty = inventoryDetailsPostingsRepository.
                            findBySaleBatchQty(productId, 3L, mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString(), batchNo);
                    totalsaleBValue = productBatchNoRepository.findSaleBatchTotalVale(productId, mUnits.getUnits().getId(), batchNo);
                    System.out.println("totalValue" + totalsaleBValue);

                    saleInside.addProperty("qty", totalsaleBQty);
                    saleInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    saleInside.addProperty("value", totalsaleBValue);
                    saleArray.add(saleInside);

                    JsonObject closingInside = new JsonObject();
                    totalClosingqty = totalpurBQty - totalsaleBQty;

                    closingInside.addProperty("qty", totalClosingqty);
                    closingInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    closingInside.addProperty("value", totalClosingqty * totalpurBValue);
                    closingArray.add(closingInside);
                }

                mainObj.add("purchase", monthArray);
                mainObj.add("sale", saleArray);
                mainObj.add("closing", closingArray);

                monthObject.addProperty("month_name", month);
                monthObject.addProperty("start_date", startMonthDate.toString());
                monthObject.addProperty("end_date", endMonthDate.toString());
                monthObject.add("responseObject", mainObj);
                innerArr.add(monthObject);
            }
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
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

    public JsonObject getExpiredProducts(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalResult = new JsonObject();
        try {
            Long branchId = null;
            Long fiscalId = null;
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
            if (fiscalYear != null) fiscalId = fiscalYear.getId();
            LocalDate currentDate = LocalDate.now();
            String startDate = "";
            String endDate = currentDate.toString();
            if (request.getParameterMap().containsKey("start_date") && !request.getParameter("start_date").isEmpty())
                startDate = request.getParameter("start_date");
            if (request.getParameterMap().containsKey("end_date") && !request.getParameter("end_date").isEmpty())
                endDate = request.getParameter("end_date");

            JsonArray jsonArray = new JsonArray();
            List<ProductBatchNo> productBatchNos = new ArrayList<>();

            String basicQuery = "SELECT * FROM product_batchno_tbl WHERE fiscal_year_id=" + fiscalId + " AND outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
                basicQuery += " AND branch_id=" + users.getBranch().getId();
            }

            if (startDate.isEmpty())
                basicQuery += " AND expiry_date<='" + currentDate + "'";
            else
                basicQuery += " AND (expiry_date BETWEEN '" + startDate + "' AND '" + endDate + "') ";
            Query q = entityManager.createNativeQuery(basicQuery, ProductBatchNo.class);
            productBatchNos = q.getResultList();
            for (ProductBatchNo batchNo : productBatchNos) {
                Long level_a_id = null;
                Long level_b_id = null;
                Long level_c_id = null;

                if (batchNo.getLevelA() != null)
                    level_a_id = batchNo.getLevelA().getId();
                if (batchNo.getLevelB() != null)
                    level_b_id = batchNo.getLevelB().getId();
                if (batchNo.getLevelC() != null)
                    level_c_id = batchNo.getLevelC().getId();

                Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(),
                        batchNo.getProduct().getId(), level_a_id, level_b_id, level_c_id, batchNo.getUnits().getId(),
                        batchNo.getId(), fiscalId);

                JsonObject batchObject = new JsonObject();
                batchObject.addProperty("id", batchNo.getProduct().getId());
                batchObject.addProperty("product_name", batchNo.getProduct().getProductName());
                batchObject.addProperty("brand_name", batchNo.getProduct().getBrand() != null ? batchNo.getProduct().getBrand().getBrandName() : "");
                batchObject.addProperty("packaging", batchNo.getProduct().getPackingMaster() != null ? batchNo.getProduct().getPackingMaster().getPackName() : "");
                batchObject.addProperty("companyName", batchNo.getOutlet() != null ? batchNo.getOutlet().getCompanyName() : "");
                batchObject.addProperty("unit_name", batchNo.getUnits() != null ? batchNo.getUnits().getUnitName() : "");
                batchObject.addProperty("qty", closing);
                batchObject.addProperty("batchno", batchNo.getBatchNo());
                batchObject.addProperty("batchid", batchNo.getId());
                batchObject.addProperty("mfgDate", batchNo.getManufacturingDate() != null ?
                        batchNo.getManufacturingDate().toString() : "");
                batchObject.addProperty("expiryDate", batchNo.getExpiryDate() != null ?
                        batchNo.getExpiryDate().toString() : "");
                jsonArray.add(batchObject);
            }
            finalResult.add("data", jsonArray);
            finalResult.addProperty("companyName", users.getOutlet().getCompanyName());
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            finalResult.addProperty("companyName", users.getOutlet().getCompanyName());
            finalResult.addProperty("message", "Failed to load data");
            finalResult.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return finalResult;
    }

    public Object getExpiryProductsMonthwise(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        Long productId = parseLong(request.getParameter("product_id"));
        Long batchId = parseLong(request.getParameter("batch_id"));
        Double sumPurchase = 0.0;
        Double sumSales = 0.0;
        Double totalClosingqty = 0.0;
        Double totalPurValue = 0.0;
        Double totalSalesValue = 0.0;

        JsonArray purArray = new JsonArray();

        JsonArray salesArray = new JsonArray();

        JsonArray closingArray = new JsonArray();

        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDatep = LocalDate.parse(request.getParameter("end_date"));
                startDatep = LocalDate.parse(request.getParameter("start_date"));
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
            JsonObject responseObjct = new JsonObject();
            JsonArray innerArr = new JsonArray();
//            JsonArray sumArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject jsonObject = new JsonObject();
                JsonObject purObject = new JsonObject();
                JsonObject salesObject = new JsonObject();
                JsonObject closingObject = new JsonObject();
                JsonArray purchaseArr = new JsonArray();
                JsonArray saleArr = new JsonArray();
                JsonArray closingArr = new JsonArray();
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate);
                /******  If You Want To Print  All Start And End Date of each month  between Fiscal Year ******/
                startDatep = endMonthDate.plusDays(1);
                System.out.println();
                /**** TranxPurchase Invoice  ****/
                sumPurchase = tranxPurInvoiceDetailsUnitsRepository.findExpiredPurSumQty(
                        productId, batchId, startMonthDate, endMonthDate, true);
                totalPurValue = tranxPurInvoiceDetailsUnitsRepository.findTotalBatchValue(productId, batchId);
                purObject.addProperty("qty", sumPurchase);
                purObject.addProperty("value", totalPurValue);
                purchaseArr.add(purObject);
//                purArray.add(purObject);
                /**** TranxSales Invoice  ****/
                sumSales = tranxSalesInvoiceDetailsUnitRepository.findExpiredSalesSumQty(
                        productId, batchId, startMonthDate, endMonthDate, true);
                totalSalesValue = tranxSalesInvoiceDetailsUnitRepository.findTotalBatchValue(productId, batchId);
                salesObject.addProperty("qty", sumSales);
                salesObject.addProperty("value", totalSalesValue);
                saleArr.add(salesObject);
//                salesArray.add(salesObject);
                /***** closing QTY ******/
                totalClosingqty = sumPurchase - sumSales;
                closingObject.addProperty("qty", totalClosingqty);
                closingObject.addProperty("value", totalClosingqty * totalPurValue);
                closingArr.add(closingObject);
//                closingArray.add(closingObject);
                jsonObject.addProperty("month", month);
                jsonObject.addProperty("start_date", startMonthDate.toString());
                jsonObject.addProperty("end_date", endMonthDate.toString());
//                sumArr.add(purObject);
//                sumArr.add(salesObject);
//                sumArr.add(closingObject);
                jsonObject.add("purchase", purchaseArr);
                jsonObject.add("sale", saleArr);
                jsonObject.add("closing", closingArr);
//                jsonObject.add("responseObject",sumArr);
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

    public Object getExpiryProductsDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        JsonArray tranxInvoice = new JsonArray();
        List<InventoryDetailsPostings> tranxList = new ArrayList<>();
        LocalDate startDatep = LocalDate.parse(request.getParameter("start_date"));
        LocalDate endDatep = LocalDate.parse(request.getParameter("end_date"));
        Long productId = parseLong(request.getParameter("product_id"));
        Long batchId = parseLong(request.getParameter("batch_id"));
        tranxList = inventoryDetailsPostingsRepository.finProductsDetails(
                productId, batchId, startDatep, endDatep);
        for (InventoryDetailsPostings mDetails : tranxList) {
            TranxPurInvoice purVoucher = null;
            TranxSalesInvoice salesVoucher = null;
            JsonObject jsonObject = new JsonObject();
            if (mDetails.getTransactionType().getId() == 1L) {
                purVoucher = tranxPurInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                jsonObject.addProperty("particular", purVoucher != null ?
                        purVoucher.getSundryCreditors().getLedgerName() : "");
                jsonObject.addProperty("voucher_type", purVoucher != null ?
                        purVoucher.getPurchaseAccountLedger().getLedgerName() : "");
                jsonObject.addProperty("invoice_no", purVoucher != null ? purVoucher.getVendorInvoiceNo() : "");
                TranxPurInvoiceDetailsUnits purUnitDetails = tranxPurInvoiceDetailsUnitsRepository.
                        findByPurchaseTransactionIdAndProductIdAndProductBatchNoId(purVoucher.getId(), productId, batchId);
                jsonObject.addProperty("value", purUnitDetails != null ? purUnitDetails.getTotalAmount() : 0);
                jsonObject.addProperty("tranx_type", 1L);
            } else if (mDetails.getTransactionType().getId() == 3L) {
                salesVoucher = tranxSalesInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                jsonObject.addProperty("particular", salesVoucher != null ?
                        salesVoucher.getSundryDebtors().getLedgerName() : "");
                jsonObject.addProperty("voucher_type", salesVoucher != null ?
                        salesVoucher.getSalesAccountLedger().getLedgerName() : "");
                jsonObject.addProperty("invoice_no", salesVoucher != null ?
                        salesVoucher.getSalesInvoiceNo() : "");
                TranxSalesInvoiceDetailsUnits salesUnitDetails = tranxSalesInvoiceDetailsUnitRepository.
                        findBySalesInvoiceIdAndProductIdAndProductBatchNoId(salesVoucher.getId(), productId, batchId);
                jsonObject.addProperty("value", salesUnitDetails != null ?
                        salesUnitDetails.getTotalAmount() : 0);
                jsonObject.addProperty("tranx_type", 3L);

            }
            jsonObject.addProperty("date", mDetails.getTranxDate().toString());
            jsonObject.addProperty("qty", mDetails.getQty());
            jsonObject.addProperty("unit", mDetails.getUnits().getUnitName());
            tranxInvoice.add(jsonObject);
        }
        res.addProperty("message", "success");
        res.add("data", tranxInvoice);
        res.addProperty("responseStatus", HttpStatus.OK.value());
        return res;
    }


    public JsonObject getAllstockValuation(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        List<Product> productList = new ArrayList<>();
        List<InventoryDetailsPostings> inventoryDetailsPostings = new ArrayList<>();
        List<Object[]> list = new ArrayList<>();
        Long branchId = null;
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
            branchId = users.getBranch().getId();
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        String AvgRate = "";
        Double t_qty = 0.0;
        Double t_rate = 0.0;
        Double[] rate = new Double[0];
        for (Product mProduct : productList) {
            JsonObject mObject = new JsonObject();
            JsonArray productunitarray = new JsonArray();
            mObject.addProperty("id", mProduct.getId());
            mObject.addProperty("product_name", mProduct.getProductName());
            mObject.addProperty("brand_name", mProduct.getBrand().getBrandName());
            mObject.addProperty("packaging", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
            mObject.addProperty("companyName", mProduct.getOutlet().getCompanyName());
            mObject.addProperty("group", mProduct.getGroup() != null ? mProduct.getGroup().getGroupName() : "");
            mObject.addProperty("sub_group", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getSubgroupName() : "");
            mObject.addProperty("category", mProduct.getCategory() != null ? mProduct.getCategory().getCategoryName() : "");
            mObject.addProperty("shelfId", mProduct.getShelfId() != null ? mProduct.getShelfId() : "");
            mObject.addProperty("sub_category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");
            mObject.addProperty("HSN", mProduct.getProductHsn().getHsnNumber() != null ? mProduct.getProductHsn().getHsnNumber() : "");
            mObject.addProperty("tax_type", mProduct.getTaxType());
            mObject.addProperty("tax", mProduct.getTaxMaster().getGst_per() != null ? mProduct.getTaxMaster().getGst_per() : "");
            mObject.addProperty("margin_per", mProduct.getMarginPer() != null ? mProduct.getMarginPer().toString() : "");
            mObject.addProperty("min_stock", mProduct.getMinStock() != null ? mProduct.getMinStock().toString() : "");
            mObject.addProperty("max_stock", mProduct.getMaxStock() != null ? mProduct.getMaxStock().toString() : "");

//            >>>>>>>>>
            String productid = mProduct.getId() != null ? String.valueOf(mProduct.getId()) : "";
            System.out.println("productid " + productid);
//            List<ProductBatchNo> productbatch = productBatchNoRepository.findByBatchList1(parseLong(productid), users.getOutlet().getId(),
//                    true);
//            JsonArray batchArray = new JsonArray();
//            if (productbatch != null && productbatch.size() > 0) {
//                for (ProductBatchNo mBatch : productbatch) {
//                    System.out.println("batch_id" + mBatch.getId());
//                     Long opening_stock = openingStocksRepository.findByBatchIdAndStatus(mBatch.getId(), true);
//
//                    JsonObject object = new JsonObject();
//                    object.addProperty("batchno1", mBatch.getBatchNo());
//                    object.addProperty("batchid1", mBatch.getId());
//                    object.addProperty("qty1", mBatch.getQnty());
//                    object.addProperty("batch_pur_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0);
//                    object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
//                    object.addProperty("unit_name", mBatch.getUnits().getUnitName());
//                    object.addProperty("opening_stock", opening_stock != null ? opening_stock : 0);
//                    object.addProperty("mfgDate", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
//
//                                 batchArray.add(object);
//                }
//            }
//            mObject.add("batch_data", batchArray);

            String sums = tranxPurInvoiceDetailsUnitsRepository.findSumByProductId(productid, true);

            if (sums.length() > 0) {
                String[] arrSum = sums.split(",");

//           for (int i=0;i<arrSum.length;i++){
                mObject.addProperty("Qsum", arrSum[0]);
                mObject.addProperty("total_AmountSum", arrSum[1]);
//           }
            } else {
                mObject.addProperty("Qsum", "");
                mObject.addProperty("total_AmountSum", "");
            }

//<<<<<<<<<<<

            list = inventoryDetailsPostingsRepository.findProductDatabyPurchaseId(mProduct.getId(), true);
            double sum_rate = 0.0;
            for (int j = 0; j < list.size(); j++) {
                Object[] objects = list.get(j);
                Long inventoryId = parseLong(objects[0].toString());
                InventoryDetailsPostings unitPacking = inventoryDetailsPostingsRepository.findByIdAndStatus(inventoryId, true);
                JsonObject productunitobject = new JsonObject();
                productunitobject.addProperty("row_id", unitPacking.getId());
                productunitobject.addProperty("unit_name", unitPacking.getUnits().getUnitName());
                productunitobject.addProperty("qty", unitPacking.getQty());
                productunitobject.addProperty("batchno", unitPacking.getUniqueBatchNo() != null ? unitPacking.getUniqueBatchNo() : "");
                productunitobject.addProperty("batchid", unitPacking.getProductBatch() != null ? unitPacking.getProductBatch().getId().toString() : "");
                productunitobject.addProperty("bat_pur_rate", unitPacking.getProductBatch() != null ?
                        unitPacking.getProductBatch().getPurchaseRate().toString() != null ? unitPacking.getProductBatch().getPurchaseRate().toString() : "" : "");
                sum_rate = sum_rate + Double.valueOf(unitPacking.getProductBatch() != null ?
                        unitPacking.getProductBatch().getPurchaseRate().toString() != null ? unitPacking.getProductBatch().getPurchaseRate().toString() : "" : "");
//                String batchid = unitPacking.getProductBatch() != null ? unitPacking.getProductBatch().getId().toString() : ""; ;
//                Double costing = productBatchNoRepository.findByBatchIdAndStatus(parseLong(batchid), true);
//
//                productunitobject.addProperty("costing", costing!=null?costing:0.0);

                productunitobject.addProperty("expiryDate", unitPacking.getProductBatch() != null ?
                        (unitPacking.getProductBatch().getExpiryDate() != null ? unitPacking.getProductBatch().getExpiryDate().toString() : "") : "");
                productunitobject.addProperty("mfgDate", unitPacking.getProductBatch() != null ?
                        (unitPacking.getProductBatch().getManufacturingDate() != null ? unitPacking.getProductBatch().getManufacturingDate().toString() : "") : "");
                Long fiscalId = null;
                LocalDate currentDate = LocalDate.now();
                FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
                if (fiscalYear != null)
                    fiscalId = fiscalYear.getId();
                Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId,
                        users.getOutlet().getId(), mProduct.getId(), unitPacking.getLevelA() != null ?
                                unitPacking.getLevelA().getId() : null,
                        null, null, unitPacking.getUnits().getId(),
                        unitPacking.getProductBatch() != null ? unitPacking.getProductBatch().getId() : null, fiscalId);
                productunitobject.addProperty("closing_stock", closing);
                productunitarray.add(productunitobject);
                t_qty = t_qty + closing;
//                if(productunitarray.size()>1){
//                    AvgRate = String. join(AvgRate, ",", rate);
//                }
            }
            t_rate = t_rate + (sum_rate != 0.0 ? sum_rate / list.size() : 0);
//            t_rate = t_rate + closing;

//            System.out.println("rates" + str[] );
//            Double sum =0.0;
//            for(int i=0;i<AvgRate.length();i++){
//                String str [] = AvgRate.split(",");
//                Double num = Double.parseDouble(str[i]);
//;                sum = sum(sum, num);
//            }
//
//            Double Average = sum/AvgRate.length();
//
//
//            productunitarray.add("Average");
//            mObject.addProperty("total_quqntity", t_qty);
            mObject.add("product_unit_data", productunitarray);
//            mObject.add("total_quqntity", t_qty);;
            jsonArray.add(mObject);
        }
//        finalResult.addProperty("array ", rate);
        finalResult.addProperty("total_closing_qty", t_qty);
        finalResult.addProperty("total_Rate", t_rate);
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

    /****** Stock Valuation and Available Stock Monthwise product details ********/

    public Object getMonthwiseStockValuationDetails(HttpServletRequest request) {
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
            Long productId = Long.valueOf(request.getParameter("productId"));
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            //****This Code For Users Dates Selection Between Start And End Date Manually****//
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
            JsonArray mainArr = new JsonArray();
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                JsonObject monthObject = new JsonObject();
                JsonArray monthArray = new JsonArray();
                JsonObject mainObj = new JsonObject();
                JsonArray saleArray = new JsonArray();
                JsonArray closingArray = new JsonArray();
//                Double closing_bal = 0.0;
                String month = startDatep.getMonth().name();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate); //**  If You Want To Print  All Start And End Date of each month  between Fiscal Year **//
                startDatep = endMonthDate.plusDays(1);
                System.out.println();

                List<ProductUnitPacking> productUnitPackings = productUnitRepository.findByProductIdAndStatus(productId, true);
                for (ProductUnitPacking mUnits : productUnitPackings) {
                    Double totalQty = 0.0;
                    Double totalpValue = 0.0;
                    Double totalsValue = 0.0;
                    Double totalClosingqty = 0.0;
                    Double totalpQty = 0.0;
//                    Double totalpvalue = 0.0;
                    Double totalpurValuetotalpurRturnQty = 0.0;
                    Double totalsQty = 0.0;
                    String totalpurQty = "";
                    String totalPurRtrnQty = "";
                    String totalSaleQty = "";
                    String totalSaleRtrnQty = "";

                    /*****  Purchase *******/
                    JsonObject inside = new JsonObject();


//                    totalpQty = inventoryDetailsPostingsRepository.
//                            findByTotalQty(productId, "CR", mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());
                    totalpurQty = tranxPurInvoiceDetailsUnitsRepository.
                            findPurQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());


                    totalPurRtrnQty = tranxPurReturnDetailsUnitRepository.
                            findPurReturnQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());


                    if (mUnits.getProduct().getIsBatchNumber()) {

                        if (!totalpurQty.isEmpty()) {
                            String[] arr = totalpurQty.split(",");
                            if (!totalPurRtrnQty.isEmpty()) {
                                String[] arr1 = totalPurRtrnQty.split(",");
                                if (arr[0].length() > 0 && arr1[0].length() > 0) {
                                    totalpQty = parseDouble(arr[0]) - parseDouble(arr1[0]);
                                } else if (arr[0].length() > 0) {
                                    totalpQty = parseDouble(arr[0]);
                                }

                                if (arr[1].length() > 0 && arr1[1].length() > 0) {
                                    totalpValue = parseDouble(arr[1]) - parseDouble(arr1[1]);
                                } else if (arr[1].length() > 0) {
                                    totalpValue = parseDouble(arr[1]);
                                }
                            }
                        } else {
                            totalpQty = 0.0;
                            totalpValue = 0.0;
                        }
                    } else {
                        totalpValue = tranxPurInvoiceDetailsUnitsRepository.findTotalValue(productId, mUnits.getUnits().getId());
                    }
                    System.out.println("totalValue" + totalpValue);
                    Double totalpRate = 0.0;
                    if (totalpQty != 0) {
                        totalpRate = totalpValue / totalpQty;
                    } else {
                        totalpRate = 0.0;
                    }

                    inside.addProperty("qty", totalpQty);
                    inside.addProperty("unit", mUnits.getUnits().getUnitName());
                    inside.addProperty("value", totalpValue);
                    inside.addProperty("rate", totalpRate);
                    monthArray.add(inside);

                    /****** Sales ****/
                    JsonObject saleInside = new JsonObject();
//                    totalsQty = inventoryDetailsPostingsRepository.
//                            findByTotalQty(productId, "DR", mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());

                    totalSaleQty = tranxSalesInvoiceDetailsUnitRepository.
                            findSalesQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());


                    totalSaleRtrnQty = tranxSalesInvoiceDetailsUnitRepository.
                            findSalesReturnQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());

                    if (mUnits.getProduct().getIsBatchNumber()) {
                        if (!totalSaleQty.isEmpty()) {
                            String[] arr = totalSaleQty.split(",");
                            if (!totalSaleRtrnQty.isEmpty()) {
                                String[] arr1 = totalSaleRtrnQty.split(",");
                                if (arr[0].length() > 0 && arr1[0].length() > 0) {
                                    totalsQty = parseDouble(arr[0]) - parseDouble(arr1[0]);
                                } else if (arr[0].length() > 0) {
                                    totalsQty = parseDouble(arr[0]);
                                }

                                if (arr[1].length() > 0 && arr1[1].length() > 0) {
                                    totalsValue = parseDouble(arr[1]) - parseDouble(arr1[1]);
                                } else if (arr[1].length() > 0) {
                                    totalsValue = parseDouble(arr[1]);
                                }
                            }
                        } else {
                            totalsQty = 0.0;
                            totalsValue = 0.0;
                        }
                    } else {

                        totalsValue = tranxSalesInvoiceDetailsUnitRepository.findTotalValue(productId, mUnits.getUnits().getId());
                    }
                    System.out.println("totalValue" + totalsValue);
                    Double totalsRate = 0.0;
                    if (totalsQty != 0) {
                        totalsRate = totalsValue / totalsQty;
                    } else {
                        totalsRate = 0.0;
                    }


                    saleInside.addProperty("qty", totalsQty);
                    saleInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    saleInside.addProperty("value", totalsValue);
                    saleInside.addProperty("rate", totalsRate);
                    saleArray.add(saleInside);

                    JsonObject closingInside = new JsonObject();

                    closingInside.addProperty("qty", (totalpQty - totalsQty));
                    closingInside.addProperty("unit", mUnits.getUnits().getUnitName());
                    closingInside.addProperty("rate", Math.round((totalpRate - totalsRate) * 100) / 100);
                    closingInside.addProperty("value", (totalpValue - totalsValue));


                    closingArray.add(closingInside);
                }

                mainObj.add("purchase", monthArray);
                mainObj.add("sale", saleArray);
                mainObj.add("closing", closingArray);

                monthObject.addProperty("month_name", month);
                monthObject.addProperty("start_date", startMonthDate.toString());
                monthObject.addProperty("end_date", endMonthDate.toString());
                monthObject.add("responseObject", mainObj);
                innerArr.add(monthObject);
            }
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.add("response", innerArr);

//            >>>>>>>>>
            List<ProductOpeningStocks> openingStocks = openingStocksRepository.findOpeningStockByProductIdAndStatus(productId, true);
            Long PursumQty = inventoryDetailsPostingsRepository.findPurProductQty(productId, startDatep);
            Long SalesumQty = inventoryDetailsPostingsRepository.findSalesProductQty(productId, startDatep);
            for (ProductOpeningStocks mOpeningStocks : openingStocks) {
//                JsonObject mObject = new JsonObject();
                if (mOpeningStocks != null) {
                    res.addProperty("unit", mOpeningStocks.getUnits().getUnitName());
//                mObject.addProperty("b_no", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getBatchNo() : "");
//                mObject.addProperty("batch_id", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getId().toString() : "");
                    res.addProperty("opening_qty", mOpeningStocks.getOpeningStocks());
                    String FreeOpeningQty = mOpeningStocks.getFreeOpeningQty() != null ? mOpeningStocks.getFreeOpeningQty().toString() : "0";
                    res.addProperty("b_free_qty", parseDouble(FreeOpeningQty));
                    res.addProperty("total_opening_qty", mOpeningStocks.getOpeningStocks() + parseDouble(FreeOpeningQty));
//                mObject.addProperty("b_mrp", mOpeningStocks.getMrp());
//                mObject.addProperty("b_sale_rate", mOpeningStocks.getSalesRate());
//                mObject.addProperty("b_purchase_rate", mOpeningStocks.getPurchaseRate());
                    String costing = mOpeningStocks.getCosting() != null ? mOpeningStocks.getCosting().toString() : "0";
                    res.addProperty("costing", parseDouble(costing));
                } else {
                    res.addProperty("unit", "");
                    res.addProperty("opening_qty", "");
                    res.addProperty("b_free_qty", "");
                    res.addProperty("total_opening_qty", "");
                    res.addProperty("costing", "");
                }


            }
//           <<<<<<<<<<
            res.addProperty("PursumQty", PursumQty != null ? PursumQty : 0);
            res.addProperty("SalesumQty", SalesumQty != null ? SalesumQty : 0);
            res.addProperty("responseStatus", HttpStatus.OK.value());

        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;

    }

//    /****** Stock Valuation Monthwise with all batch data product details ********/
//    public Object getMonthwiseStockValuationDetails(HttpServletRequest request) {
//        JsonObject res = new JsonObject();
//        Long id = 0L;
//        Double total_month_sum = 0.0;
//        Double credit_total = 0.0;
//
//        List<Object[]> list = new ArrayList<>();
//        try {
//            Map<String, String[]> paramMap = request.getParameterMap();
//            String endDate = null;
//            LocalDate endDatep = null;
//            String startDate = null;
//            LocalDate startDatep = null;
//            Long productId = Long.valueOf(request.getParameter("productId"));
//            LocalDate currentStartDate = null;
//            LocalDate currentEndDate = null;
//            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//            //****This Code For Users Dates Selection Between Start And End Date Manually****//
//            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
//                endDate = request.getParameter("end_date");
//                endDatep = LocalDate.parse(endDate);
//                startDate = request.getParameter("start_date");
//                startDatep = LocalDate.parse(startDate);
//
//            } else {
//
//                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
//                if (fiscalYear != null) {
//                    startDatep = fiscalYear.getDateStart();
//                    endDatep = fiscalYear.getDateEnd();
//                }
//
//            }
//            currentStartDate = startDatep;
//            currentEndDate = endDatep;
//            if (startDatep.isAfter(endDatep)) {
//                System.out.println("Start Date Should not be After");
//                return 0;
//            }
//            JsonArray mainArr = new JsonArray();
//            JsonArray innerArr = new JsonArray();
//            while (startDatep.isBefore(endDatep)) {
//                JsonObject monthObject = new JsonObject();
//                JsonArray monthArray = new JsonArray();
//                JsonObject mainObj = new JsonObject();
//                JsonArray saleArray = new JsonArray();
//                JsonArray closingArray = new JsonArray();
////                Double closing_bal = 0.0;
//                String month = startDatep.getMonth().name();
//                System.out.println();
//                LocalDate startMonthDate = startDatep;
//                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
//                System.out.println("Start Date:" + startMonthDate + "End Date " + endMonthDate); //**  If You Want To Print  All Start And End Date of each month  between Fiscal Year **//
//                startDatep = endMonthDate.plusDays(1);
//                System.out.println();
//
//                List<ProductUnitPacking> productUnitPackings = productUnitRepository.findByProductIdAndStatus(productId, true);
//                for (ProductUnitPacking mUnits : productUnitPackings) {
//                    Double totalQty = 0.0;
//                    Double totalpValue = 0.0;
//                    Double totalsValue = 0.0;
//                    Double totalClosingqty = 0.0;
//                    Double totalpQty = 0.0;
//                    Double totalpRate = 0.0;
////                    Double totalpvalue = 0.0;
//                    Double totalpurValuetotalpurRturnQty = 0.0;
//                    Double totalsQty = 0.0;
////                    String totalpurQty = "";
//                    String totalPurRtrnQty = "";
//                    String totalSaleQty = "";
//                    String totalSaleRtrnQty = "";
//
//                    /*****  Purchase *******/
//                    JsonObject inside = new JsonObject();
//
//
//                    List<Object[]> totalpurQty = new ArrayList<>();
//                    List<Object[]> totalPurRtrnQty = new ArrayList<>();
////                    totalpQty = inventoryDetailsPostingsRepository.
////                            findByTotalQty(productId, "CR", mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());
//                    totalpurQty = tranxPurInvoiceDetailsUnitsRepository.
//                            findPurQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());
//
//
//                    totalPurRtrnQty = tranxPurReturnDetailsUnitRepository.
//                            findPurReturnQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());
//
//
//                    if (mUnits.getProduct().getIsBatchNumber()) {
//                        Double totalpurQty1 = Double.valueOf(Arrays.toString(totalpurQty.get(0)));
//                        Double totalPurRtrnQty1 = Double.valueOf(Arrays.toString(totalPurRtrnQty.get(0)));
//                        Double totalpurRate1 = Double.valueOf(Arrays.toString(totalpurQty.get(1)));
//                        Double totalPurRtrnRate1 = Double.valueOf(Arrays.toString(totalPurRtrnQty.get(1)));
//
//                        if (totalpurQty != null) {
//                            if(totalPurRtrnQty != null){
//                                if(totalpurQty1 >= 0 &&  totalPurRtrnQty1 >=0){
//                                  totalpQty = totalpurQty1-totalPurRtrnQty1;
//                                    totalpRate =   totalpurRate1 - totalPurRtrnRate1;
//                                    totalpValue = totalpQty*totalpRate;
//                               } else {
//                                 totalpQty = 0.0;
//                                 totalpRate = 0.0;
//                                 totalpValue = 0.0;
//                             }
//                    } else {
//                        totalpValue = tranxPurInvoiceDetailsUnitsRepository.findTotalValue(productId, mUnits.getUnits().getId());
//                    }
//                    System.out.println("totalValue" + totalpValue);
//
//                    if (totalpQty != 0) {
//                        totalpRate = totalpValue / totalpQty;
//                    } else {
//                        totalpRate = 0.0;
//                    }
//
//                    inside.addProperty("qty", totalpQty);
//                    inside.addProperty("unit", mUnits.getUnits().getUnitName());
//                    inside.addProperty("value", totalpValue);
//                    inside.addProperty("rate", totalpRate);
//                    monthArray.add(inside);
//
//                    /****** Sales ****/
//                    JsonObject saleInside = new JsonObject();
////                    totalsQty = inventoryDetailsPostingsRepository.
////                            findByTotalQty(productId, "DR", mUnits.getUnits().getId(), startMonthDate.toString(), endMonthDate.toString());
//
//                    totalSaleQty = tranxSalesInvoiceDetailsUnitRepository.
//                            findSalesQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());
//
//
//                    totalSaleRtrnQty = tranxSalesInvoiceDetailsUnitRepository.
//                            findSalesReturnQtyByProductIdAndStatus(productId, true, startMonthDate.toString(), endMonthDate.toString());
//
//                    if (mUnits.getProduct().getIsBatchNumber()) {
//                        if (!totalSaleQty.isEmpty()) {
//                            String[] arr = totalSaleQty.split(",");
//                            if (!totalSaleRtrnQty.isEmpty()) {
//                                String[] arr1 = totalSaleRtrnQty.split(",");
//                                if (arr[0].length() > 0 && arr1[0].length() > 0) {
//                                    totalsQty = Double.parseDouble(arr[0]) - Double.parseDouble(arr1[0]);
//                                } else if (arr[0].length() > 0) {
//                                    totalsQty = Double.parseDouble(arr[0]);
//                                }
//
//                                if (arr[1].length() > 0 && arr1[1].length() > 0) {
//                                    totalsValue = Double.parseDouble(arr[1]) - Double.parseDouble(arr1[1]);
//                                } else if (arr[1].length() > 0) {
//                                    totalsValue = Double.parseDouble(arr[1]);
//                                }
//                            }
//                        } else {
//                            totalsQty = 0.0;
//                            totalsValue = 0.0;
//                        }
//                    } else {
//
//                        totalsValue = tranxSalesInvoiceDetailsUnitRepository.findTotalValue(productId, mUnits.getUnits().getId());
//                    }
//                    System.out.println("totalValue" + totalsValue);
//                    Double totalsRate = 0.0;
//                    if (totalsQty != 0) {
//                        totalsRate = totalsValue / totalsQty;
//                    } else {
//                        totalsRate = 0.0;
//                    }
//
//
//                    saleInside.addProperty("qty", totalsQty);
//                    saleInside.addProperty("unit", mUnits.getUnits().getUnitName());
//                    saleInside.addProperty("value", totalsValue);
//                    saleInside.addProperty("rate", totalsRate);
//                    saleArray.add(saleInside);
//
//                    JsonObject closingInside = new JsonObject();
//
//                    closingInside.addProperty("qty", (totalpQty - totalsQty));
//                    closingInside.addProperty("unit", mUnits.getUnits().getUnitName());
//                    closingInside.addProperty("rate", Math.round((totalpRate - totalsRate) * 100) / 100);
//                    closingInside.addProperty("value", (totalpValue - totalsValue));
//
//
//                    closingArray.add(closingInside);
//                }
//
//                mainObj.add("purchase", monthArray);
//                mainObj.add("sale", saleArray);
//                mainObj.add("closing", closingArray);
//
//                monthObject.addProperty("month_name", month);
//                monthObject.addProperty("start_date", startMonthDate.toString());
//                monthObject.addProperty("end_date", endMonthDate.toString());
//                monthObject.add("responseObject", mainObj);
//                innerArr.add(monthObject);
//            }
//            res.addProperty("d_start_date", currentStartDate.toString());
//            res.addProperty("d_end_date", currentEndDate.toString());
//            res.add("response", innerArr);
//
////            >>>>>>>>>
//            List<ProductOpeningStocks> openingStocks = openingStocksRepository.findOpeningStockByProductIdAndStatus(productId, true);
//            Long PursumQty = inventoryDetailsPostingsRepository.findPurProductQty(productId, startDatep);
//            Long SalesumQty = inventoryDetailsPostingsRepository.findSalesProductQty(productId, startDatep);
//            for (ProductOpeningStocks mOpeningStocks : openingStocks) {
////                JsonObject mObject = new JsonObject();
//                if (mOpeningStocks != null) {
//                    res.addProperty("unit", mOpeningStocks.getUnits().getUnitName());
////                mObject.addProperty("b_no", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getBatchNo() : "");
////                mObject.addProperty("batch_id", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getId().toString() : "");
//                    res.addProperty("opening_qty", mOpeningStocks.getOpeningStocks());
//                    String FreeOpeningQty = mOpeningStocks.getFreeOpeningQty() != null ? mOpeningStocks.getFreeOpeningQty().toString() : "0";
//                    res.addProperty("b_free_qty", Double.parseDouble(FreeOpeningQty));
//                    res.addProperty("total_opening_qty", mOpeningStocks.getOpeningStocks() + Double.parseDouble(FreeOpeningQty));
////                mObject.addProperty("b_mrp", mOpeningStocks.getMrp());
////                mObject.addProperty("b_sale_rate", mOpeningStocks.getSalesRate());
////                mObject.addProperty("b_purchase_rate", mOpeningStocks.getPurchaseRate());
//                    String costing = mOpeningStocks.getCosting() != null ? mOpeningStocks.getCosting().toString() : "0";
//                    res.addProperty("costing", Double.parseDouble(costing));
//                } else {
//                    res.addProperty("unit", "");
//                    res.addProperty("opening_qty", "");
//                    res.addProperty("b_free_qty", "");
//                    res.addProperty("total_opening_qty", "");
//                    res.addProperty("costing", "");
//                }
//
//
//            }
////           <<<<<<<<<<
//            res.addProperty("PursumQty", PursumQty!= null ? PursumQty : 0 );
//            res.addProperty("SalesumQty", SalesumQty != null ? SalesumQty : 0);
//            res.addProperty("responseStatus", HttpStatus.OK.value());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.addProperty("message", "Failed To Load Data");
//            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
//        return res;
//
//    }

    public Object getMonthwiseWholeStockDetailsPrdTranx(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        JsonArray tranxInvoice = new JsonArray();
        List<InventoryDetailsPostings> tranxList = new ArrayList<>();
        LocalDate startDatep = null;
        LocalDate endDatep = null;
        Long productId = parseLong(request.getParameter("productId"));
        String durations = null;
        Long branchId = null;
        try {
            Map<String, String[]> paraMap = request.getParameterMap();
            if (paraMap.containsKey("start_date") && paraMap.containsKey("end_date")) {
                String stDay = request.getParameter("start_date");
                startDatep = LocalDate.parse(stDay);
                String endDay = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDay);

            } else if (paraMap.containsKey("duration")) {
                durations = request.getParameter("duration");
                if (durations.equalsIgnoreCase("month")) {
                    //for finding first and last day of current month
                    LocalDate thisMonth = LocalDate.now();
                    String fDay = thisMonth.withDayOfMonth(1).toString();
                    String lDay = thisMonth.withDayOfMonth(thisMonth.lengthOfMonth()).toString();
                    startDatep = LocalDate.parse(fDay);
                    endDatep = LocalDate.parse(lDay);

//                    LocalDate currentDate = LocalDate.now();
//                    startDatep = currentDate.minusDays(30);
//                    endDatep = currentDate;

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
            } else if (paraMap.containsKey("daysInNumber")) {
                Long days = Long.valueOf(request.getParameter("daysInNumber"));
                LocalDate todayDate = LocalDate.now();
                LocalDate userDate = todayDate.minusDays(days);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedDate = userDate.format(formatter);
                System.out.println("formattedDate " + formattedDate);
                startDatep = LocalDate.parse(formattedDate);
                endDatep = todayDate;

            }
//        Long batchId = Long.parseLong(request.getParameter("batch_id"));
            Double cost_for_closing = productBatchNoRepository.findCostByProductId(productId);
            tranxList = inventoryDetailsPostingsRepository.findProductsDetails(productId, startDatep, endDatep);
            Long PursumQty = inventoryDetailsPostingsRepository.findPurProductQty(productId, startDatep);
            Long SalesumQty = inventoryDetailsPostingsRepository.findSalesProductQty(productId, startDatep);
            for (InventoryDetailsPostings mDetails : tranxList) {
                TranxPurInvoice purVoucher = null;
                TranxSalesInvoice salesVoucher = null;
                TranxPurReturnInvoice purReturnVoucher = null;
                TranxSalesReturnInvoice salesReturnVoucher = null;
                TranxPurChallan purChallanVoucher = null;
                TranxSalesChallan salesChallanVoucher = null;
                TranxCounterSales counterSaleVoucher = null;
                JsonObject jsonObject = new JsonObject();
                if (mDetails.getTransactionType().getId() == 1L) {
                    purVoucher = tranxPurInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", purVoucher != null ?
                            purVoucher.getSundryCreditors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", purVoucher != null ?
                            purVoucher.getPurchaseAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", purVoucher != null ? purVoucher.getVendorInvoiceNo() : "");
                    List<TranxPurInvoiceDetailsUnits> purUnitDetails = tranxPurInvoiceDetailsUnitsRepository.
                            findByPurchaseInvoiceIdAndProductIdAndStatus1(purVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", purUnitDetails != null ? purUnitDetails.get(0).getTotalAmount() : 0);
                    jsonObject.addProperty("qty", purUnitDetails != null ? purUnitDetails.get(0).getQty() : 0);
                    jsonObject.addProperty("Rate", purUnitDetails != null ? purUnitDetails.get(0).getRate() : 0);
                    jsonObject.addProperty("tranx_type", 1L);
//                    jsonObject.addProperty("opening_qty");

                } else if (mDetails.getTransactionType().getId() == 3L) {
                    salesVoucher = tranxSalesInvoiceRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", salesVoucher != null ?
                            salesVoucher.getSundryDebtors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", salesVoucher != null ?
                            salesVoucher.getSalesAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", salesVoucher != null ?
                            salesVoucher.getSalesInvoiceNo() : "");
                    TranxSalesInvoiceDetailsUnits salesUnitDetails = tranxSalesInvoiceDetailsUnitRepository.
                            findBySalesInvoiceIdAndProductIdAndStatus(salesVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", salesUnitDetails != null ?
                            salesUnitDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 3L);

                } else if (mDetails.getTransactionType().getId() == 2L) {
                    purReturnVoucher = tranxPurReturnsRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", purReturnVoucher != null ?
                            purReturnVoucher.getSundryCreditors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", purReturnVoucher != null ?
                            purReturnVoucher.getPurchaseAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", purReturnVoucher != null ?
                            purReturnVoucher.getPurRtnNo() : "");
                    TranxPurReturnDetailsUnits purReturnUnitDetails = tranxPurReturnDetailsUnitRepository.
                            findByTranxPurReturnInvoiceIdAndProductIdAndStatus(purReturnVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", purReturnUnitDetails != null ?
                            purReturnUnitDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 2L);

                } else if (mDetails.getTransactionType().getId() == 4L) {
                    salesReturnVoucher = tranxSalesReturnRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", salesReturnVoucher != null ?
                            salesReturnVoucher.getSundryDebtors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", salesReturnVoucher != null ?
                            salesReturnVoucher.getSalesAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", salesReturnVoucher != null ?
                            salesReturnVoucher.getSalesReturnNo() : "");
                    TranxSalesReturnDetailsUnits salesReturnUnitDetails = tranxSalesReturnDetailsUnitsRepository.
                            findBySalesReturnInvoiceIdAndProductIdAndStatus(salesReturnVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", salesReturnUnitDetails != null ?
                            salesReturnUnitDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 4L);
                } else if (mDetails.getTransactionType().getId() == 12L) {
                    purChallanVoucher = tranxPurChallanRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", purChallanVoucher != null ?
                            purChallanVoucher.getSundryCreditors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", purChallanVoucher != null ?
                            purChallanVoucher.getPurchaseAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", purChallanVoucher != null ? purChallanVoucher.getVendorInvoiceNo() : "");
                    TranxPurChallanDetailsUnits purChallanUnitDetails = tranxPurChallanDetailsUnitRepository.
                            findByTranxPurChallanIdAndProductIdAndStatus(purChallanVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", purChallanUnitDetails != null ? purChallanUnitDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 12L);

                } else if (mDetails.getTransactionType().getId() == 15L) {

                    salesChallanVoucher = tranxSalesChallanRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", salesChallanVoucher != null ?
                            salesChallanVoucher.getSundryDebtors().getLedgerName() : "");
                    jsonObject.addProperty("voucher_type", salesChallanVoucher != null ?
                            salesChallanVoucher.getSalesAccountLedger().getLedgerName() : "");
                    jsonObject.addProperty("invoice_no", salesChallanVoucher != null ? salesChallanVoucher.getSc_bill_no() : "");
                    TranxSalesChallanDetailsUnits salesChallanDetails = tranxSalesChallanDetailsUnitsRepository.
                            findBySalesChallanIdAndProductIdAndStatus(salesChallanVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", salesChallanDetails != null ? salesChallanDetails.getTotalAmount() : 0);
                    jsonObject.addProperty("tranx_type", 15L);

                } else if (mDetails.getTransactionType().getId() == 16L) {

                    counterSaleVoucher = tranxCounterSalesRepository.findByIdAndStatus(mDetails.getTranxId(), true);
                    jsonObject.addProperty("particular", counterSaleVoucher.getCustomerName() != null ? counterSaleVoucher.getCustomerName() : "");
                    jsonObject.addProperty("voucher_type", "Sales Account");
                    jsonObject.addProperty("invoice_no", counterSaleVoucher != null ? counterSaleVoucher.getCounterSaleNo() : "");
                    TranxCounterSalesDetailsUnits salesChallanDetails = tranxCSDetailsUnitsRepository.
                            findByCounterSalesIdAndProductIdAndStatus(counterSaleVoucher.getId(), productId, true);
                    jsonObject.addProperty("value", salesChallanDetails != null ? salesChallanDetails.getNetAmount() : 0);
                    jsonObject.addProperty("tranx_type", 16L);

                }

                jsonObject.addProperty("tranx_unique_code", mDetails.getTransactionType().getTransactionCode());
                jsonObject.addProperty("tranx_type", mDetails.getTransactionType().getTransactionName());
                jsonObject.addProperty("date", DateConvertUtil.convertDateToLocalDate(mDetails.getTranxDate()).toString());
                jsonObject.addProperty("qtyofsmallestunit", mDetails.getQty());
                jsonObject.addProperty("unit", mDetails.getUnits().getUnitName());
                tranxInvoice.add(jsonObject);
            }
            List<Object[]> openingStocks = openingStocksRepository.findOpeningStockByProductId(productId, true);
//            for (ProductOpeningStocks mOpeningStocks : openingStocks) {
            for (int j = 0; j < openingStocks.size(); j++) {
                JsonObject inspObj = new JsonObject();

                Double op_qty1 = openingStocks.get(j)[0] != null ? (Double) openingStocks.get(j)[0] : 0.0;
                Double op_qty2 = openingStocks.get(j)[2] != null ? (Double) openingStocks.get(j)[2] : 0.0;
                Double PursumQty1 = PursumQty != null ? parseDouble(String.valueOf(PursumQty)) : 0.0;
                Double SalesumQty1 = SalesumQty != null ? parseDouble(String.valueOf(SalesumQty)) : 0.0;
                if (openingStocks != null) {
                    Double opening_qty = op_qty1 + op_qty2 + PursumQty1 - SalesumQty1;
                    res.addProperty("opening_qty", op_qty1);
                    res.addProperty("free_qty", op_qty2);
                    res.addProperty("total_opening_qty", opening_qty);

                    res.addProperty("costing", openingStocks.get(j)[1] != null ? parseDouble(openingStocks.get(j)[1].toString()) : 0.0);
                    res.addProperty("unit", openingStocks.get(j)[3] != null ? openingStocks.get(j)[3].toString() : "" +
                            "");

                } else {
                    res.addProperty("total_opening_qty", "");
                    res.addProperty("costing", "");
                    res.addProperty("unit", "");
                }
            }
//                if (openingStocks != null ) {
//                    res.addProperty("unit", Arrays.toString(openingStocks.get(0)));
//                    res.addProperty("opening_qty", mOpeningStocks.getOpeningStocks());
//                    String FreeOpeningQty = mOpeningStocks.getFreeOpeningQty() != null ? mOpeningStocks.getFreeOpeningQty().toString() : "0";
//                    res.addProperty("b_free_qty", parseDouble(FreeOpeningQty));
//                  double opening_qty = mOpeningStocks.getOpeningStocks() + parseDouble(FreeOpeningQty) + (PursumQty != null ? PursumQty : 0) - (SalesumQty != null ? SalesumQty : 0);
//                    res.addProperty("total_opening_qty", opening_qty);
//
//                    String costing = mOpeningStocks.getCosting() != null ? mOpeningStocks.getCosting().toString() : "0";
//                    res.addProperty("costing", parseDouble(costing));
//                } else {
//                    res.addProperty("unit", "");
//                    res.addProperty("opening_qty", "");
//                    res.addProperty("b_free_qty", "");
//                    res.addProperty("total_opening_qty", "");
//                    res.addProperty("costing", "");
//                }

//            }
            res.addProperty("cost_for_closing", cost_for_closing != null ? cost_for_closing : 0.0);
            res.addProperty("PursumQty", PursumQty != null ? PursumQty : 0);
            res.addProperty("SalesumQty", SalesumQty != null ? SalesumQty : 0);
            res.addProperty("message", "success");
            res.add("data", tranxInvoice);
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.addProperty("d_start_date", startDatep.toString());
            res.addProperty("d_end_date", endDatep.toString());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("Error", "Failed to load data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }


        return res;

    }

    public JsonObject getAllstockReportBk(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        List<Product> productList = new ArrayList<>();
        List<Product> units = new ArrayList<>();
        List<InventoryDetailsPostings> inventoryDetailsPostings = new ArrayList<>();
        List<Object[]> list = new ArrayList<>();
        Double closing = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;
        Long branchId = null;
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
            branchId = users.getBranch().getId();
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        String AvgRate = "";
        Double t_qty1 = 0.0;
        Double t_rate1 = 0.0;
        Double[] rate = new Double[0];
        try {
            for (Product mProduct : productList) {
                Double t_qty = 0.0;
                Double t_rate = 0.0;
                JsonObject mObject = new JsonObject();
                JsonArray productunitarray = new JsonArray();

                mObject.addProperty("id", mProduct.getId());

                mObject.addProperty("product_name", mProduct.getProductName());
                mObject.addProperty("item_code", mProduct.getProductCode());
                mObject.addProperty("brand_name", mProduct.getBrand().getBrandName());
                mObject.addProperty("packaging", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
                mObject.addProperty("companyName", mProduct.getOutlet().getCompanyName());
                mObject.addProperty("group", mProduct.getGroup() != null ? mProduct.getGroup().getGroupName() : "");
                mObject.addProperty("sub_group", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getSubgroupName() : "");
                mObject.addProperty("category", mProduct.getCategory() != null ? mProduct.getCategory().getCategoryName() : "");
                mObject.addProperty("sub_category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");
                mObject.addProperty("drugType", mProduct.getDrugType() != null ? mProduct.getDrugType() : "");
                mObject.addProperty("drug_content", mProduct.getDrugContents() != null ? mProduct.getDrugContents() : "");
                mObject.addProperty("hsn", mProduct.getProductHsn().getHsnNumber());
                mObject.addProperty("tax", mProduct.getTaxType().toString());
                mObject.addProperty("tax_per", mProduct.getTaxMaster().getGst_per());
//              mObject.addProperty("content", mProduct.getProductContentMaster().to);
                //            mObject.addProperty("manufacturer-name",mProduct.get);
//            mObject.addProperty("Supplier", mProduct.get);

                mObject.addProperty("shelfId", mProduct.getShelfId() != null ? mProduct.getShelfId() : "");
                mObject.addProperty("sub_category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");
                mObject.addProperty("HSN", mProduct.getProductHsn().getHsnNumber() != null ? mProduct.getProductHsn().getHsnNumber() : "");
                mObject.addProperty("tax_type", mProduct.getTaxType());
                mObject.addProperty("tax", mProduct.getTaxMaster().getGst_per() != null ? mProduct.getTaxMaster().getGst_per() : "");
                mObject.addProperty("margin_per", mProduct.getMarginPer() != null ? mProduct.getMarginPer().toString() : "");
                mObject.addProperty("min_stock", mProduct.getMinStock() != null ? mProduct.getMinStock().toString() : "");
                mObject.addProperty("max_stock", mProduct.getMaxStock() != null ? mProduct.getMaxStock().toString() : "");


                String productid = mProduct.getId() != null ? String.valueOf(mProduct.getId()) : "";
                System.out.println("productid " + productid);

                String sums = tranxPurInvoiceDetailsUnitsRepository.findSumByProductId(productid, true);
//            units = productRepository.findUnitByIdAndStatus(productid, true);

                if (sums.length() > 0) {
                    String[] arrSum = sums.split(",");

//           for (int i=0;i<arrSum.length;i++){
                    mObject.addProperty("Qsum", arrSum[0]);
                    mObject.addProperty("total_AmountSum", arrSum[1]);
//           }
                } else {
                    mObject.addProperty("Qsum", "");
                    mObject.addProperty("total_AmountSum", "");
                }
                List<ProductBatchNo> productbatch = productBatchNoRepository.findByBatchList1(parseLong(productid), users.getOutlet().getId(),
                        true);
                double sum_rate1 = 0.0;
                JsonArray batchArray = new JsonArray();
                if (productbatch != null && productbatch.size() > 0) {
                    for (ProductBatchNo mBatch : productbatch) {
                        System.out.println("batch_id" + mBatch.getBatchNo());
                        Long opening_stock = openingStocksRepository.findByBatchIdAndStatus(mBatch.getId(), true);

                        List<String[]> batchUnits = productBatchNoRepository.FindUnitByProductIdAndBatchId(parseLong(productid), parseLong(mBatch.getBatchNo()));
                        JsonArray UnitArray = new JsonArray();
//                        System.out.println("batchUnits1"+ batchUnits);

                        JsonObject object = new JsonObject();
                        object.addProperty("row_id", mBatch.getId());
                        object.addProperty("batchno", mBatch.getBatchNo());
                        object.addProperty("batchid", mBatch.getId());
                        object.addProperty("qty", mBatch.getQnty());
                        object.addProperty("bat_pur_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0);
                        object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
//                        object.addProperty("unit_name", mBatch.getUnits().getUnitName());
//                    object.addProperty("LevelA", mBatch.getLevelA().getLevelName() != null ? mBatch.getLevelA().getLevelName() : "" );
//                    object.addProperty("LevelB", mBatch.getLevelB().getLevelName() != null ? mBatch.getLevelB().getLevelName() : "" );
//                    object.addProperty("LevelC", mBatch.getLevelC().getLevelName() != null ? mBatch.getLevelC().getLevelName() : "" );
                        object.addProperty("sale_rate1", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.0);
                        object.addProperty("sale_rate2", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0.0);
                        object.addProperty("sale_rate3", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0.0);
                        object.addProperty("CostWithTax", mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.0);
                        object.addProperty("CostWithoutTax", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
                        object.addProperty("mrp", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
                        object.addProperty("opening_stock", opening_stock != null ? opening_stock : 0);
                        object.addProperty("mfgDate", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
//                    sum_rate1 = sum_rate1 + mBatch.getPurchaseRate().doubleValue();
                        sum_rate1 = sum_rate1 + (mBatch != null ?
                                (mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0) : 0.0);

                        Long fiscalId = null;
                        LocalDate currentDate = LocalDate.now();
                        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
                        if (fiscalYear != null)
                            fiscalId = fiscalYear.getId();
                        Double closing1 = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(), mProduct.getId(),
                                mBatch.getLevelA() != null ? mBatch.getLevelA().getId() : null, null, null, mBatch.getUnits().getId(),
                                mBatch != null ? mBatch.getId() : null, fiscalId);
                        object.addProperty("closing_stock", closing1);
                        batchArray.add(object);
                        t_qty = t_qty + closing1;
                        t_qty1 = t_qty1 + closing1;

                        for (int j = 0; j < batchUnits.size(); j++) {
                            JsonObject inspObj = new JsonObject();
                            inspObj.addProperty("actualSize", batchUnits.size());
                            inspObj.addProperty("sizeResult", batchUnits.get(j)[0]);
                            if (batchUnits != null) {
                                inspObj.addProperty("batch_no", batchUnits.get(j)[0]);
                                inspObj.addProperty("batch_id", batchUnits.get(j)[1]);
                                inspObj.addProperty("unit_id", batchUnits.get(j)[2]);
                                inspObj.addProperty("unit_name", batchUnits.get(j)[3]);
                                double qty = batchUnits.get(j)[4] != null && batchUnits.get(j)[5] != null ? Double.parseDouble((batchUnits.get(j)[4])) + Double.parseDouble(batchUnits.get(j)[5])
                                        : (batchUnits.get(j)[4] != null ? Double.parseDouble(batchUnits.get(j)[4]) : batchUnits.get(j)[5] != null ? Double.parseDouble(batchUnits.get(j)[5]) : 0);
                                inspObj.addProperty("unit_qty", qty);
                                inspObj.addProperty("unit_conv", batchUnits.get(j)[6] != null ? Double.parseDouble(batchUnits.get(j)[6]) : 1.0);
                                inspObj.addProperty("unit_saleRate", batchUnits.get(j)[7] != null ? Double.parseDouble(batchUnits.get(j)[7]) : 0.0);
                                inspObj.addProperty("unit_purchaseRate", batchUnits.get(j)[8] != null ? Double.parseDouble(batchUnits.get(j)[8]) : 0.0);
                                inspObj.addProperty("unit_costing", batchUnits.get(j)[9] != null ? Double.parseDouble(batchUnits.get(j)[9]) : 0.0);
                                inspObj.addProperty("unit_costingWT", batchUnits.get(j)[10] != null ? Double.parseDouble(batchUnits.get(j)[10]) : 0.0);
                                inspObj.addProperty("unit_mrp", batchUnits.get(j)[11] != null ? Double.parseDouble(batchUnits.get(j)[11]) : 0.0);

                            }
                            UnitArray.add(inspObj);
                        }
                        System.out.println("batchUnits" + batchUnits);

                        object.add("batch_unit_data", UnitArray);

                    }
                }
                t_rate1 = t_rate1 + (sum_rate1 != 0.0 ? sum_rate1 / productbatch.size() : 0);
                t_rate = t_rate + (sum_rate1 != 0.0 ? sum_rate1 / productbatch.size() : 0);
                mObject.addProperty("Product_clo_qty", t_qty);
                mObject.addProperty("Product_clo_rate", t_rate);
                mObject.add("product_unit_data", batchArray);

                jsonArray.add(mObject);
            }
            finalResult.addProperty("total_closing_qty", t_qty1);
            finalResult.addProperty("total_Rate", t_rate1);

//        finalResult.addProperty("total_closing_qty", t_qty);
//        finalResult.addProperty("total_Rate", t_rate);
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("data", jsonArray);

        } catch (Exception e) {
            e.printStackTrace();
            finalResult.addProperty("Error", "Failed to load data");
            finalResult.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return finalResult;
    }

    public JsonObject getAllstockReport(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        List<Product> productList = new ArrayList<>();
        List<Product> units = new ArrayList<>();
        List<InventorySummary> inventoryDetailsPostings = new ArrayList<>();
        List<Object[]> list = new ArrayList<>();
        Double closing = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;
        Long branchId = null;
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
            branchId = users.getBranch().getId();
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        String AvgRate = "";
        Double t_qty1 = 0.0;
        Double t_rate1 = 0.0;
        Double[] rate = new Double[0];
        try {
            for (Product mProduct : productList) {
                Double t_qty = 0.0;
                Double t_rate = 0.0;
                JsonObject mObject = new JsonObject();
                JsonArray productunitarray = new JsonArray();

                mObject.addProperty("id", mProduct.getId());

                mObject.addProperty("product_name", mProduct.getProductName());
                mObject.addProperty("item_code", mProduct.getProductCode());
                mObject.addProperty("brand_name", mProduct.getBrand().getBrandName());
                mObject.addProperty("packaging", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
                mObject.addProperty("companyName", mProduct.getOutlet().getCompanyName());
                mObject.addProperty("group", mProduct.getGroup() != null ? mProduct.getGroup().getGroupName() : "");
                mObject.addProperty("sub_group", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getSubgroupName() : "");
                mObject.addProperty("category", mProduct.getCategory() != null ? mProduct.getCategory().getCategoryName() : "");
                mObject.addProperty("sub_category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");
                mObject.addProperty("drugType", mProduct.getDrugType() != null ? mProduct.getDrugType() : "");
                mObject.addProperty("drug_content", mProduct.getDrugContents() != null ? mProduct.getDrugContents() : "");
                mObject.addProperty("hsn", mProduct.getProductHsn().getHsnNumber());
                mObject.addProperty("tax", mProduct.getTaxType().toString());
                mObject.addProperty("tax_per", mProduct.getTaxMaster().getGst_per());
//              mObject.addProperty("content", mProduct.getProductContentMaster().to);
                //            mObject.addProperty("manufacturer-name",mProduct.get);
//            mObject.addProperty("Supplier", mProduct.get);

                mObject.addProperty("shelfId", mProduct.getShelfId() != null ? mProduct.getShelfId() : "");
                mObject.addProperty("sub_category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");
                mObject.addProperty("HSN", mProduct.getProductHsn().getHsnNumber() != null ? mProduct.getProductHsn().getHsnNumber() : "");
                mObject.addProperty("tax_type", mProduct.getTaxType());
                mObject.addProperty("tax", mProduct.getTaxMaster().getGst_per() != null ? mProduct.getTaxMaster().getGst_per() : "");
                mObject.addProperty("margin_per", mProduct.getMarginPer() != null ? mProduct.getMarginPer().toString() : "");
                mObject.addProperty("min_stock", mProduct.getMinStock() != null ? mProduct.getMinStock().toString() : "");
                mObject.addProperty("max_stock", mProduct.getMaxStock() != null ? mProduct.getMaxStock().toString() : "");


                String productid = mProduct.getId() != null ? String.valueOf(mProduct.getId()) : "";
                System.out.println("productid " + productid);

                String sums = tranxPurInvoiceDetailsUnitsRepository.findSumByProductId(productid, true);
//            units = productRepository.findUnitByIdAndStatus(productid, true);

                if (sums.length() > 0) {
                    String[] arrSum = sums.split(",");

//           for (int i=0;i<arrSum.length;i++){
                    mObject.addProperty("Qsum", arrSum[0]);
                    mObject.addProperty("total_AmountSum", arrSum[1]);
//           }
                } else {
                    mObject.addProperty("Qsum", "");
                    mObject.addProperty("total_AmountSum", "");
                }

                JsonArray batchArray = new JsonArray();
                batchArray= unitConversion.showWholeStocks(parseLong(productid),null);
                /* Old working code for single unit
                List<ProductBatchNo> productbatch = productBatchNoRepository.findByBatchList1(parseLong(productid), users.getOutlet().getId(),
                        true);
                double sum_rate1 = 0.0;
                JsonArray batchArray = new JsonArray();
                if (productbatch != null && productbatch.size() > 0) {
                    for (ProductBatchNo mBatch : productbatch) {
                        System.out.println("batch_id" + mBatch.getBatchNo());
                        Long opening_stock = openingStocksRepository.findByBatchIdAndStatus(mBatch.getId(), true);

                        List<String[]> batchUnits = productBatchNoRepository.FindUnitByProductIdAndBatchId(parseLong(productid), parseLong(mBatch.getBatchNo()));
                        JsonArray UnitArray = new JsonArray();
//                        System.out.println("batchUnits1"+ batchUnits);

                        JsonObject object = new JsonObject();
                        object.addProperty("row_id", mBatch.getId());
                        object.addProperty("batchno", mBatch.getBatchNo());
                        object.addProperty("batchid", mBatch.getId());
                        object.addProperty("qty", mBatch.getQnty());
                        object.addProperty("bat_pur_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0);
                        object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
//                        object.addProperty("unit_name", mBatch.getUnits().getUnitName());
//                    object.addProperty("LevelA", mBatch.getLevelA().getLevelName() != null ? mBatch.getLevelA().getLevelName() : "" );
//                    object.addProperty("LevelB", mBatch.getLevelB().getLevelName() != null ? mBatch.getLevelB().getLevelName() : "" );
//                    object.addProperty("LevelC", mBatch.getLevelC().getLevelName() != null ? mBatch.getLevelC().getLevelName() : "" );
                        object.addProperty("sale_rate1", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.0);
                        object.addProperty("sale_rate2", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0.0);
                        object.addProperty("sale_rate3", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0.0);
                        object.addProperty("CostWithTax", mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.0);
                        object.addProperty("CostWithoutTax", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
                        object.addProperty("mrp", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
                        object.addProperty("opening_stock", opening_stock != null ? opening_stock : 0);
                        object.addProperty("mfgDate", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
//                    sum_rate1 = sum_rate1 + mBatch.getPurchaseRate().doubleValue();
                        sum_rate1 = sum_rate1 + (mBatch != null ?
                                (mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0) : 0.0);

                        Long fiscalId = null;
                        LocalDate currentDate = LocalDate.now();
                        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
                        if (fiscalYear != null)
                            fiscalId = fiscalYear.getId();

                        Double closing1 = 0.0;
                        if (users.getBranch() != null)
                            closing1 = summaryRepository.getClosingStock(users.getBranch().getId(),
                                    users.getOutlet().getId(),
                                    mProduct.getId(), mBatch.getUnits().getId(),
                                    mBatch != null ? mBatch.getId() : null, fiscalId);
                        else {
                            closing1 = summaryRepository.getClosingStockWTBr(users.getOutlet().getId(),
                                    mProduct.getId(), mBatch.getUnits().getId(),
                                    mBatch != null ? mBatch.getId() : null, fiscalId);
                        }
                        object.addProperty("closing_stock", closing1);
                        batchArray.add(object);
                        if (closing1 == null) closing1 = 0.0;
                        t_qty = t_qty + closing1;
                        t_qty1 = t_qty1 + closing1;

                        for (int j = 0; j < batchUnits.size(); j++) {
                            JsonObject inspObj = new JsonObject();
                            inspObj.addProperty("actualSize", batchUnits.size());
                            inspObj.addProperty("sizeResult", batchUnits.get(j)[0]);
                            if (batchUnits != null) {
                                inspObj.addProperty("batch_no", batchUnits.get(j)[0]);
                                inspObj.addProperty("batch_id", batchUnits.get(j)[1]);
                                inspObj.addProperty("unit_id", batchUnits.get(j)[2]);
                                inspObj.addProperty("unit_name", batchUnits.get(j)[3]);
                                double qty = batchUnits.get(j)[4] != null && batchUnits.get(j)[5] != null ? Double.parseDouble((batchUnits.get(j)[4])) + Double.parseDouble(batchUnits.get(j)[5])
                                        : (batchUnits.get(j)[4] != null ? Double.parseDouble(batchUnits.get(j)[4]) : batchUnits.get(j)[5] != null ? Double.parseDouble(batchUnits.get(j)[5]) : 0);
                                inspObj.addProperty("unit_qty", closing1);
                                inspObj.addProperty("unit_conv", batchUnits.get(j)[6] != null ? Double.parseDouble(batchUnits.get(j)[6]) : 1.0);
                                inspObj.addProperty("unit_saleRate", batchUnits.get(j)[7] != null ? Double.parseDouble(batchUnits.get(j)[7]) : 0.0);
                                inspObj.addProperty("unit_purchaseRate", batchUnits.get(j)[8] != null ? Double.parseDouble(batchUnits.get(j)[8]) : 0.0);
                                inspObj.addProperty("unit_costing", batchUnits.get(j)[9] != null ? Double.parseDouble(batchUnits.get(j)[9]) : 0.0);
                                inspObj.addProperty("unit_costingWT", batchUnits.get(j)[10] != null ? Double.parseDouble(batchUnits.get(j)[10]) : 0.0);
                                inspObj.addProperty("unit_mrp", batchUnits.get(j)[11] != null ? Double.parseDouble(batchUnits.get(j)[11]) : 0.0);

                            }
                            UnitArray.add(inspObj);
                        }
                        System.out.println("batchUnits" + batchUnits);

                        object.add("batch_unit_data", UnitArray);

                    }
                }


                t_rate1 = t_rate1 + (sum_rate1 != 0.0 ? sum_rate1 / productbatch.size() : 0);
                t_rate = t_rate + (sum_rate1 != 0.0 ? sum_rate1 / productbatch.size() : 0);
                mObject.addProperty("Product_clo_qty", t_qty);
                mObject.addProperty("Product_clo_rate", t_rate);*/

                mObject.add("product_unit_data", batchArray);

                jsonArray.add(mObject);
            }
            finalResult.addProperty("total_closing_qty", t_qty1);
            finalResult.addProperty("total_Rate", t_rate1);

//        finalResult.addProperty("total_closing_qty", t_qty);
//        finalResult.addProperty("total_Rate", t_rate);
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("data", jsonArray);

        } catch (Exception e) {
            e.printStackTrace();
            finalResult.addProperty("Error", "Failed to load data");
            finalResult.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return finalResult;
    }


//    public JsonObject getAllstockValReport(HttpServletRequest request) {
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//
//        List<Product> productList = new ArrayList<>();
//        List<Product> units = new ArrayList<>();
//        List<InventoryDetailsPostings> inventoryDetailsPostings = new ArrayList<>();
//        List<Object[]> list = new ArrayList<>();
//        Double closing = 0.0;
//        Double drClosing = 0.0;
//        Double crClosing = 0.0;
//        Long branchId = null;
//        if (users.getBranch() != null) {
//            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
//            branchId = users.getBranch().getId();
//        } else {
//            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
//        }
//        JsonObject finalResult = new JsonObject();
//        JsonArray jsonArray = new JsonArray();
//
//        String AvgRate = "";
//        Double t_qty1 = 0.0;
//        Double all_product_purRate = 0.0, all_product_saleRate = 0.0, all_product_costTaxRate = 0.0,
//                all_product_costWoTaxRate = 0.0, all_product_mrp = 0.0;
//        Double t_value1 = 0.0;
//        Double[] rate = new Double[0];
//        for (Product mProduct : productList) {
//            Double t_product_qty = 0.0;
//            Double t_product_rate = 0.0, Tsum_SaleRate = 0.0;
//            Double Tsum_CostTaxRate = 0.0, Tsum_CostWoTaxRate = 0.0, Tsum_mrp = 0.0;
//            JsonObject mObject = new JsonObject();
//            JsonArray productunitarray = new JsonArray();
//
//            mObject.addProperty("id", mProduct.getId());
//
//            mObject.addProperty("product_name", mProduct.getProductName());
//            mObject.addProperty("item_code", mProduct.getProductCode());
//            mObject.addProperty("brand_name", mProduct.getBrand().getBrandName());
//            mObject.addProperty("packaging", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
//            mObject.addProperty("companyName", mProduct.getOutlet().getCompanyName());
//            mObject.addProperty("group", mProduct.getGroup() != null ? mProduct.getGroup().getGroupName() : "");
//            mObject.addProperty("sub_group", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getSubgroupName() : "");
//            mObject.addProperty("category", mProduct.getCategory() != null ? mProduct.getCategory().getCategoryName() : "");
//            mObject.addProperty("sub-category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");
//            mObject.addProperty("drug-type", mProduct.getDrugType() != null ? mProduct.getDrugType() : "");
//            mObject.addProperty("drug_content", mProduct.getDrugContents() != null ? mProduct.getDrugContents() : "");
//            mObject.addProperty("hsn", mProduct.getProductHsn().getHsnNumber());
//            mObject.addProperty("tax", mProduct.getTaxType().toString());
//            mObject.addProperty("tax-per", mProduct.getTaxMaster().getGst_per());
//
//            //            mObject.addProperty("manufacturer-name",mProduct.get);
////            mObject.addProperty("Supplier", mProduct.get);
//
//
//            mObject.addProperty("shelfId", mProduct.getShelfId() != null ? mProduct.getShelfId() : "");
//            mObject.addProperty("sub_category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");
//            mObject.addProperty("HSN", mProduct.getProductHsn().getHsnNumber() != null ? mProduct.getProductHsn().getHsnNumber() : "");
//            mObject.addProperty("tax_type", mProduct.getTaxType());
//            mObject.addProperty("tax", mProduct.getTaxMaster().getGst_per() != null ? mProduct.getTaxMaster().getGst_per() : "");
//            mObject.addProperty("margin_per", mProduct.getMarginPer() != null ? mProduct.getMarginPer().toString() : "");
//            mObject.addProperty("min_stock", mProduct.getMinStock() != null ? mProduct.getMinStock().toString() : "");
//            mObject.addProperty("max_stock", mProduct.getMaxStock() != null ? mProduct.getMaxStock().toString() : "");
//
//
//            String productid = mProduct.getId() != null ? String.valueOf(mProduct.getId()) : "";
//            System.out.println("productid " + productid);
//
////            Long unit_id;
////            List<InventoryDetailsPostings> batchwiseUnits = inventoryDetailsPostingsRepository.findUnitAndBatchByProductId(parseLong(productid), true);// unit_id, product_id, batch_id
////            if (batchwiseUnits != null && batchwiseUnits.size() > 0) {
////                for (InventoryDetailsPostings mData : batchwiseUnits) {
////                    System.out.println("batch-wiseUnits " + mData.getUnits() + " " + mData.getProductBatch());
////                }}
//            List<ProductBatchNo> productbatch = productBatchNoRepository.findByBatchList1(parseLong(productid), users.getOutlet().getId(),
//                    true);
//            System.out.println("users.getOutlet().getId()" + users.getOutlet().getId());
//            double sum_rate1 = 0.0, sum_SaleRate = 0.0, sum_CostTaxRate = 0.0, sum_CostWoTaxRate = 0.0, sum_mrp = 0.0;
//            JsonArray batchArray = new JsonArray();
//            if (productbatch != null && productbatch.size() > 0) {
//                for (ProductBatchNo mBatch : productbatch) {
//                    System.out.println("batch_id" + mBatch.getId());
//                    Long opening_stock = openingStocksRepository.findByBatchIdAndStatus(mBatch.getId(), true);
//
//                    JsonObject object = new JsonObject();
//                    object.addProperty("row_id", mBatch.getId());
//                    object.addProperty("batchno", mBatch.getBatchNo());
//                    object.addProperty("batchid", mBatch.getId());
//                    object.addProperty("qty", mBatch.getQnty() != null ? mBatch.getQnty() : 0.0);
//                    object.addProperty("pur_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0);
//                    object.addProperty("pur_Valuation", (mBatch.getPurchaseRate() != null && mBatch.getQnty() != null) ? mBatch.getPurchaseRate() * mBatch.getQnty() : 0.0);
//
//                    object.addProperty("sale_rate1", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.0);
//                    object.addProperty("sale_Valuation", (mBatch.getMinRateA() != null && mBatch.getQnty() != null) ? mBatch.getMinRateA() * mBatch.getQnty() : 0.0);
//
//                    object.addProperty("mrp", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
//                    object.addProperty("mrp_Valuation", (mBatch.getMrp() != null && mBatch.getQnty() != null) ? mBatch.getMrp() * mBatch.getQnty() : 0.0);
//
//                    object.addProperty("CostWithTax", mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.0);
//                    object.addProperty("CostTax_Valuation", (mBatch.getCostingWithTax() != null && mBatch.getQnty() != null) ? mBatch.getCostingWithTax() * mBatch.getQnty() : 0.0);
//
//                    object.addProperty("CostWithoutTax", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
//                    object.addProperty("costWoTax_Valuation", (mBatch.getCosting() != null && mBatch.getQnty() != null) ? mBatch.getCosting() * mBatch.getQnty() : 0.0);
//
//                    object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
//                    object.addProperty("unit_name", mBatch.getUnits().getUnitName());
////                    object.addProperty("LevelA", mBatch.getLevelA().getLevelName() != null ? mBatch.getLevelA().getLevelName() : "" );
////                    object.addProperty("LevelB", mBatch.getLevelB().getLevelName() != null ? mBatch.getLevelB().getLevelName() : "" );
////                    object.addProperty("LevelC", mBatch.getLevelC().getLevelName() != null ? mBatch.getLevelC().getLevelName() : "" );
//
//                    object.addProperty("sale_rate2", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0.0);
//                    object.addProperty("sale_rate3", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0.0);
//
//
//                    object.addProperty("opening_stock", opening_stock != null ? opening_stock : 0);
//                    object.addProperty("mfgDate", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
////                    sum_rate1 = sum_rate1 + mBatch.getPurchaseRate().doubleValue();
//                    sum_rate1 = sum_rate1 + (mBatch != null ?
//                            (mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0) : 0.0);
//                    sum_SaleRate = sum_SaleRate + (mBatch != null ?
//                            (mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.0) : 0.0);
//                    sum_CostTaxRate = sum_CostTaxRate + (mBatch != null ?
//                            (mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.0) : 0.0);
//                    sum_CostWoTaxRate = sum_CostWoTaxRate + (mBatch != null ?
//                            (mBatch.getCosting() != null ? mBatch.getCosting() : 0.0) : 0.0);
//                    sum_mrp = sum_mrp + (mBatch != null ?
//                            (mBatch.getMrp() != null ? mBatch.getMrp() : 0.0) : 0.0);
//
//                    Long fiscalId = null;
//                    LocalDate currentDate = LocalDate.now();
//                    FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
//                    if (fiscalYear != null)
//                        fiscalId = fiscalYear.getId();
//                    Double closing1 = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(), mProduct.getId(),
//                            mBatch.getLevelA() != null ? mBatch.getLevelA().getId() : null, null, null, mBatch.getUnits().getId(),
//                            mBatch != null ? mBatch.getId() : null, fiscalId);
//                    object.addProperty("closing_stock", closing1);
//                    batchArray.add(object);
//                    t_product_qty = t_product_qty + (mBatch.getQnty() != null ? mBatch.getQnty() : 0.0);
//
//                }
//            }
//            t_qty1 = t_qty1 + t_product_qty;
//            t_product_rate = (productbatch.size() != 0 && sum_rate1 != 0) ? (sum_rate1 / productbatch.size()) : 0.0;
//            Tsum_SaleRate = (productbatch.size() != 0 && sum_SaleRate != 0) ? (sum_SaleRate / productbatch.size()) : 0.0;
//            Tsum_CostTaxRate = (productbatch.size() != 0 && sum_CostTaxRate != 0) ? (sum_CostTaxRate / productbatch.size()) : 0.0;
//            Tsum_CostWoTaxRate = (productbatch.size() != 0 && sum_CostWoTaxRate != 0) ? (sum_CostWoTaxRate / productbatch.size()) : 0.0;
//            Tsum_mrp = (productbatch.size() != 0 && sum_mrp != 0) ? (sum_mrp / productbatch.size()) : 0.0;
//
//            all_product_purRate = all_product_purRate + t_product_rate;
//            all_product_saleRate = all_product_saleRate + Tsum_SaleRate;
//            all_product_costTaxRate = all_product_costTaxRate + Tsum_CostTaxRate;
//            all_product_costWoTaxRate = all_product_costWoTaxRate + Tsum_CostWoTaxRate;
//            all_product_mrp = all_product_mrp + Tsum_mrp;
//
//            mObject.addProperty("total_product_qty", t_product_qty);
//            mObject.addProperty("total_product_purrate", t_product_rate);
//            mObject.addProperty("total_product_salerate", Tsum_SaleRate);
//            mObject.addProperty("total_product_costTaxrate", Tsum_CostTaxRate);
//            mObject.addProperty("total_product_costWoTaxrate", Tsum_CostWoTaxRate);
//            mObject.addProperty("total_product_mrp", Tsum_mrp);
//
//            mObject.addProperty("total_product_purvalue", t_product_qty * t_product_rate);
//            mObject.addProperty("total_product_salevalue", t_product_qty * Tsum_SaleRate);
//            mObject.addProperty("total_product_costTaxvalue", t_product_qty * Tsum_CostTaxRate);
//            mObject.addProperty("total_product_costWoTaxvalue", t_product_qty * Tsum_CostWoTaxRate);
//            mObject.addProperty("total_product_mrpValue", t_product_qty * Tsum_mrp);
//
//            mObject.add("product_unit_data", batchArray);
//
//            jsonArray.add(mObject);
//        }
//        finalResult.addProperty("total_closing_qty", t_qty1);
//        finalResult.addProperty("total_PurRate", all_product_purRate);
//        finalResult.addProperty("total_PurValue", t_qty1 * all_product_purRate);
//        finalResult.addProperty("total_saleRate", all_product_saleRate);
//        finalResult.addProperty("total_saleValue", t_qty1 * all_product_saleRate);
//        finalResult.addProperty("total_costTaxRate", all_product_costTaxRate);
//        finalResult.addProperty("total_costTaxValue", t_qty1 * all_product_costTaxRate);
//        finalResult.addProperty("total_costWoTaxRate", all_product_costWoTaxRate);
//        finalResult.addProperty("total_costWoTaxValue", t_qty1 * all_product_costWoTaxRate);
//        finalResult.addProperty("total_mrpRate", all_product_mrp);
//        finalResult.addProperty("total_mrpValue", t_qty1 * all_product_mrp);
//
////        finalResult.addProperty("total_closing_qty", t_qty);
////        finalResult.addProperty("total_Rate", t_rate);
//        finalResult.addProperty("message", "success");
//        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
//        finalResult.add("data", jsonArray);
//        return finalResult;
//    }


    public JsonObject getPayableReport(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        LocalDate startDate = null;
        LocalDate endDate = null;
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray1 = new JsonArray();
        Map<String, String[]> paraMap = request.getParameterMap();
        if (paraMap.containsKey("start_date") && paraMap.containsKey("end_date")) {
            startDate = LocalDate.parse(request.getParameter("start_date"));

            endDate = LocalDate.parse(request.getParameter("end_date"));
        } else {
            LocalDate thisMonth = LocalDate.now();
            String fDay = thisMonth.withDayOfMonth(1).toString();
            String lDay = thisMonth.withDayOfMonth(thisMonth.lengthOfMonth()).toString();
            startDate = LocalDate.parse(fDay);
            endDate = LocalDate.parse(lDay);
        }
        LocalDate currentDate = LocalDate.now();

        List<String[]> PayableLedger = tranxPurInvoiceRepository.findLedgerByDate(startDate, endDate);
        for (int i = 0; i < PayableLedger.size(); i++) {
            JsonObject jsonObject1 = new JsonObject();
            JsonArray jsonArray2 = new JsonArray();
            String[] object = PayableLedger.get(i);
            Double total = 0.0, total_balance = 0.0, total_paid_amt = 0.0;
            List<String[]> PayableData = tranxPurInvoiceRepository.findByDate(parseLong(object[0]), startDate, endDate);
            for (int j = 0; j < PayableData.size(); j++) {
                JsonObject jsonObject = new JsonObject();

                String[] obj = PayableData.get(j);

                if (PayableData.size() > 0) {
                    jsonObject.addProperty("ledger_name", obj[0]);
                    jsonObject.addProperty("CreditorsId", parseLong(obj[1]));
                    jsonObject.addProperty("Invoice_no", parseLong(obj[2]));
                    jsonObject.addProperty("balance", parseDouble(obj[3]));
                    jsonObject.addProperty("invoiceDate", String.valueOf(LocalDate.parse(obj[9])));
                    jsonObject.addProperty("credit_days", parseLong(obj[4]));
                    LocalDate invoice_date = LocalDate.parse(obj[9]);
//                System.out.println("parseLong(obj[4]) " + parseLong(obj[4]) + " " + 5);
                    LocalDate dueDate = invoice_date.plusDays(parseLong(obj[4]));
                    jsonObject.addProperty("Due_date", String.valueOf(dueDate));
                    LocalDate dateFrom = dueDate;
                    LocalDate dateTo = currentDate;
                    Period intervalPeriod = Period.between(dateFrom, dateTo);
                    int totalDays = intervalPeriod.getDays();
                    System.out.println("overDueDays " + ((totalDays < 0) ? 0 : totalDays));
                    jsonObject.addProperty("overDueDays", ((totalDays < 0) ? 0 : totalDays));
                    jsonObject.addProperty("balancing_method", parseInt(obj[5]));
                    jsonObject.addProperty("total_amount", parseDouble(obj[6]));
                    jsonObject.addProperty("paid_amt", obj[7] != null ? parseDouble(obj[7]) : 0.0);
                    jsonObject.addProperty("remaining_amt", (obj[8]) != null ? parseDouble(obj[8]) : 0.0);
                    jsonObject.addProperty("balancing_type", (obj[10]) != null ? obj[10] : "");
                    total = total + ((obj[6]) != null ? parseDouble(obj[6]) : 0.0);
                    total_balance = total_balance + ((obj[3]) != null ? parseDouble(obj[3]) : 0);
                    total_paid_amt = total_paid_amt + ((obj[7] != null) ? parseDouble(obj[7]) : 0.0);
                }
                jsonArray2.add(jsonObject);
                jsonObject1.addProperty("Ledger_name", object[1]);
                jsonObject1.addProperty("balancing_method", object[2]);
                jsonObject1.addProperty("total_amount", total);
                jsonObject1.addProperty("total_balance", total_balance);
                jsonObject1.addProperty("total_paid_amt", total_paid_amt);
                jsonObject1.add("invoice_data", jsonArray2);
            }
            jsonArray1.add(jsonObject1);
        }
        finalResult.addProperty("startDate", String.valueOf(startDate));
        finalResult.addProperty("endDate", String.valueOf(endDate));
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray1);
        return finalResult;
    }


    public JsonObject getReceivableReport(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        LocalDate startDate = null;
        LocalDate endDate = null;
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray1 = new JsonArray();
        Map<String, String[]> paraMap = request.getParameterMap();
        if (paraMap.containsKey("start_date") && paraMap.containsKey("end_date")) {
//            startDate = LocalDate.parse(request.getParameter("start_date"));
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            startDate = LocalDate.parse(request.getParameter("start_date"));
            endDate = LocalDate.parse(request.getParameter("end_date"));
//            endDate = LocalDate.parse(request.getParameter("end_date"));
        } else {
            LocalDate thisMonth = LocalDate.now();
            String fDay = thisMonth.withDayOfMonth(1).toString();
            String lDay = thisMonth.withDayOfMonth(thisMonth.lengthOfMonth()).toString();
            startDate = LocalDate.parse(fDay);
            endDate = LocalDate.parse(lDay);
        }
        LocalDate currentDate = LocalDate.now();
        List<String[]> ReceivableLedger = tranxSalesInvoiceRepository.findLedgerByDate(startDate, endDate);
        for (int i = 0; i < ReceivableLedger.size(); i++) {
            JsonObject jsonObject1 = new JsonObject();
            JsonArray jsonArray2 = new JsonArray();
            String[] object = ReceivableLedger.get(i);
            Double total = 0.0, total_balance = 0.0, total_paid_amt = 0.0;
            List<String[]> ReceivableData = tranxSalesInvoiceRepository.findByDate(parseLong(object[0]), startDate, endDate);

            for (int j = 0; j < ReceivableData.size(); j++) {
                JsonObject jsonObject = new JsonObject();

                String[] obj = ReceivableData.get(j);

                if (ReceivableData.size() > 0) {
                    jsonObject.addProperty("ledger_name", obj[0]);
                    jsonObject.addProperty("CreditorsId", obj[1]);
                    jsonObject.addProperty("Invoice_no", obj[2]);
                    jsonObject.addProperty("balance", obj[3]);
                    jsonObject.addProperty("invoiceDate", String.valueOf(LocalDate.parse(obj[9])));
                    jsonObject.addProperty("credit_days", obj[4]);
                    LocalDate invoice_date = LocalDate.parse(obj[9]);
//                System.out.println("parseLong(obj[4]) " + parseLong(obj[4]) + " " + 5);
                    LocalDate dueDate = invoice_date.plusDays(parseLong(obj[4]));
                    jsonObject.addProperty("Due_date", String.valueOf(dueDate));
                    LocalDate dateFrom = dueDate;
                    LocalDate dateTo = currentDate;
                    Period intervalPeriod = Period.between(dateFrom, dateTo);
                    int totalDays = intervalPeriod.getDays();
                    System.out.println("overDueDays " + ((totalDays < 0) ? 0 : totalDays));
                    jsonObject.addProperty("overDueDays", ((totalDays < 0) ? 0 : totalDays));
                    jsonObject.addProperty("balancing_method", parseInt(obj[5]));
                    jsonObject.addProperty("total_amount", (obj[6] != null ? parseDouble(obj[6]) : 0.0));
                    jsonObject.addProperty("paid_amt", (obj[7]) != null ? parseDouble(obj[7]) : 0.0);
                    jsonObject.addProperty("remaining_amt", (obj[8]) != null ? parseDouble(obj[8]) : 0.0);
                    jsonObject.addProperty("balancing_type", (obj[10]) != null ? obj[10] : "");
                    System.out.println("total + parseDouble(obj[6])" + (obj[6] != null ? parseDouble(obj[6]) : 0.0));
                    total = total + (obj[6] != null ? parseDouble(obj[6]) : 0.0);
                    total_balance = total_balance + (obj[3] != null ? parseDouble(obj[3]) : 0.0);
                    total_paid_amt = total_paid_amt + ((obj[7] != null) ? parseDouble(obj[7]) : 0.0);
                }
                jsonArray2.add(jsonObject);
                jsonObject1.addProperty("Ledger_name", object[1]);
                jsonObject1.addProperty("balancing_method", object[2]);
                jsonObject1.addProperty("total_amount", total);
                jsonObject1.addProperty("total_balance", total_balance);
                jsonObject1.addProperty("total_paid_amt", total_paid_amt);
                jsonObject1.add("invoice_data", jsonArray2);
            }
            jsonArray1.add(jsonObject1);

        }
        finalResult.addProperty("startDate", String.valueOf(startDate));
        finalResult.addProperty("endDate", String.valueOf(endDate));
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray1);
        return finalResult;

    }
    // Export Excell API


    public InputStream exportToExcelPurchaseReg(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos " + productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};

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

                    int sumOfQty = 0;
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


                        sumOfQty += batchNo.get("credit").getAsDouble();

                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(5);
                    cell.setCellValue(sumOfQty);

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

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    //    API for stockReport screen 1
    public InputStream exportExcelWholeStock2(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "INVOICE NO.", "PARTICULARS", "VOUCHER TYPE ", "PURCHASE QTY", "PURCHASE QTY", "PURCHASE UNIT", "PURCHASE VALUE", "SALE QTY", "SALE UNIT", "SALE VALUE", "CLOSING QTY", "CLOSING UNIT", "CLOSING VALUE"};

                    Sheet sheet = workbook.createSheet("excelExportStockReport2");

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

//                    int sumOfQty = 0;
//                    int sumOfSaleQty = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject productObject = productBatchNos.get(i).getAsJsonObject();
                        System.out.println("batchNo  " + productObject);


                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(productObject.get("date").getAsString());
                        row.createCell(1).setCellValue(productObject.get("invoice_no").getAsString());
                        row.createCell(2).setCellValue(productObject.get("particular").getAsString());
                        row.createCell(3).setCellValue(productObject.get("voucher_type").getAsString());
                        if (productObject.get("tranx_unique_code").getAsString().equalsIgnoreCase("PRS")
                                || productObject.get("tranx_unique_code").getAsString().equalsIgnoreCase("PRSRT")
                                || productObject.get("tranx_unique_code").getAsString().equalsIgnoreCase("PRSCHN")) {
                            row.createCell(4).setCellValue(productObject.get("qty").getAsString());
                            row.createCell(5).setCellValue(productObject.get("unit").getAsString());
                            row.createCell(6).setCellValue(productObject.get("value").getAsString());
                        }
                        if (productObject.get("tranx_unique_code").getAsString().equalsIgnoreCase("SLS")
                                || productObject.get("tranx_unique_code").getAsString().equalsIgnoreCase("SLSRT")
                                || productObject.get("tranx_unique_code").getAsString().equalsIgnoreCase("CNTS")
                                || productObject.get("tranx_unique_code").getAsString().equalsIgnoreCase("SLSCHN")) {
                            row.createCell(7).setCellValue(productObject.get("qty").getAsString());
                            row.createCell(8).setCellValue(productObject.get("unit").getAsString());
                            row.createCell(9).setCellValue(productObject.get("value").getAsString());
                        }

                        row.createCell(11).setCellValue("");
                        row.createCell(12).setCellValue("");
                        row.createCell(13).setCellValue("");
                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            //    System.out.println("wholeStock1ExcelExport" + out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public JsonObject getAllstockValReport(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        List<Product> productList = new ArrayList<>();
        List<Product> units = new ArrayList<>();
        List<InventoryDetailsPostings> inventoryDetailsPostings = new ArrayList<>();
        List<Object[]> list = new ArrayList<>();
        Double closing = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;
        Long branchId = null;
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
            branchId = users.getBranch().getId();
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        String AvgRate = "";
        Double t_qty1 = 0.0;
        Double all_product_purRate = 0.0, all_product_saleRate = 0.0, all_product_costTaxRate = 0.0,
                all_product_costWoTaxRate = 0.0, all_product_mrp = 0.0;
        Double t_value1 = 0.0;
        Double[] rate = new Double[0];
        for (Product mProduct : productList) {
            Double t_product_qty = 0.0;
            Double t_product_rate = 0.0, Tsum_SaleRate = 0.0;
            Double Tsum_CostTaxRate = 0.0, Tsum_CostWoTaxRate = 0.0, Tsum_mrp = 0.0;
            JsonObject mObject = new JsonObject();
            JsonArray productunitarray = new JsonArray();

            mObject.addProperty("id", mProduct.getId());

            mObject.addProperty("product_name", mProduct.getProductName());
            mObject.addProperty("item_code", mProduct.getProductCode());
            mObject.addProperty("brand_name", mProduct.getBrand().getBrandName());
            mObject.addProperty("packaging", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
            mObject.addProperty("companyName", mProduct.getOutlet().getCompanyName());
            mObject.addProperty("group", mProduct.getGroup() != null ? mProduct.getGroup().getGroupName() : "");
            mObject.addProperty("sub_group", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getSubgroupName() : "");
            mObject.addProperty("category", mProduct.getCategory() != null ? mProduct.getCategory().getCategoryName() : "");
            mObject.addProperty("sub-category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");
            mObject.addProperty("drug-type", mProduct.getDrugType() != null ? mProduct.getDrugType() : "");
            mObject.addProperty("drug_content", mProduct.getDrugContents() != null ? mProduct.getDrugContents() : "");
            mObject.addProperty("hsn", mProduct.getProductHsn().getHsnNumber());
            mObject.addProperty("tax", mProduct.getTaxType().toString());
            mObject.addProperty("tax-per", mProduct.getTaxMaster().getGst_per());

            //            mObject.addProperty("manufacturer-name",mProduct.get);
//            mObject.addProperty("Supplier", mProduct.get);


            mObject.addProperty("shelfId", mProduct.getShelfId() != null ? mProduct.getShelfId() : "");
            mObject.addProperty("sub_category", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getSubcategoryName() : "");
            mObject.addProperty("HSN", mProduct.getProductHsn().getHsnNumber() != null ? mProduct.getProductHsn().getHsnNumber() : "");
            mObject.addProperty("tax_type", mProduct.getTaxType());
            mObject.addProperty("tax", mProduct.getTaxMaster().getGst_per() != null ? mProduct.getTaxMaster().getGst_per() : "");
            mObject.addProperty("margin_per", mProduct.getMarginPer() != null ? mProduct.getMarginPer().toString() : "");
            mObject.addProperty("min_stock", mProduct.getMinStock() != null ? mProduct.getMinStock().toString() : "");
            mObject.addProperty("max_stock", mProduct.getMaxStock() != null ? mProduct.getMaxStock().toString() : "");


            String productid = mProduct.getId() != null ? String.valueOf(mProduct.getId()) : "";
            System.out.println("productid " + productid);

//            Long unit_id;
//            List<InventoryDetailsPostings> batchwiseUnits = inventoryDetailsPostingsRepository.findUnitAndBatchByProductId(parseLong(productid), true);// unit_id, product_id, batch_id
//            if (batchwiseUnits != null && batchwiseUnits.size() > 0) {
//                for (InventoryDetailsPostings mData : batchwiseUnits) {
//                    System.out.println("batch-wiseUnits " + mData.getUnits() + " " + mData.getProductBatch());
//                }}
            List<ProductBatchNo> productbatch = productBatchNoRepository.findByBatchList1(parseLong(productid), users.getOutlet().getId(),
                    true);
            System.out.println("users.getOutlet().getId()" + users.getOutlet().getId());
            double sum_rate1 = 0.0, sum_SaleRate = 0.0, sum_CostTaxRate = 0.0, sum_CostWoTaxRate = 0.0, sum_mrp = 0.0;
            JsonArray batchArray = new JsonArray();
            if (productbatch != null && productbatch.size() > 0) {
                for (ProductBatchNo mBatch : productbatch) {
                    System.out.println("batch_id" + mBatch.getId());
                    Long opening_stock = openingStocksRepository.findByBatchIdAndStatus(mBatch.getId(), true);

                    List<String[]> batchUnits = productBatchNoRepository.FindUnitByProductIdAndBatchId(parseLong(productid), parseLong(mBatch.getBatchNo()));
                    JsonArray UnitArray = new JsonArray();
//                        System.out.println("batchUnits1"+ batchUnits);

                    JsonObject object = new JsonObject();
                    object.addProperty("row_id", mBatch.getId());
                    object.addProperty("batchno", mBatch.getBatchNo());
                    object.addProperty("batchid", mBatch.getId());
                    object.addProperty("qty", mBatch.getQnty() != null ? mBatch.getQnty() : 0.0);
                    object.addProperty("pur_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0);
                    object.addProperty("pur_Valuation", (mBatch.getPurchaseRate() != null && mBatch.getQnty() != null) ? mBatch.getPurchaseRate() * mBatch.getQnty() : 0.0);

                    object.addProperty("sale_rate1", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.0);
                    object.addProperty("sale_Valuation", (mBatch.getMinRateA() != null && mBatch.getQnty() != null) ? mBatch.getMinRateA() * mBatch.getQnty() : 0.0);

                    object.addProperty("mrp", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
                    object.addProperty("mrp_Valuation", (mBatch.getMrp() != null && mBatch.getQnty() != null) ? mBatch.getMrp() * mBatch.getQnty() : 0.0);

                    object.addProperty("CostWithTax", mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.0);
                    object.addProperty("CostTax_Valuation", (mBatch.getCostingWithTax() != null && mBatch.getQnty() != null) ? mBatch.getCostingWithTax() * mBatch.getQnty() : 0.0);

                    object.addProperty("CostWithoutTax", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
                    object.addProperty("costWoTax_Valuation", (mBatch.getCosting() != null && mBatch.getQnty() != null) ? mBatch.getCosting() * mBatch.getQnty() : 0.0);

                    object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                    object.addProperty("unit_name", mBatch.getUnits().getUnitName());
//                    object.addProperty("LevelA", mBatch.getLevelA().getLevelName() != null ? mBatch.getLevelA().getLevelName() : "" );
//                    object.addProperty("LevelB", mBatch.getLevelB().getLevelName() != null ? mBatch.getLevelB().getLevelName() : "" );
//                    object.addProperty("LevelC", mBatch.getLevelC().getLevelName() != null ? mBatch.getLevelC().getLevelName() : "" );

                    object.addProperty("sale_rate2", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0.0);
                    object.addProperty("sale_rate3", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0.0);


                    object.addProperty("opening_stock", opening_stock != null ? opening_stock : 0);
                    object.addProperty("mfgDate", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
//                    sum_rate1 = sum_rate1 + mBatch.getPurchaseRate().doubleValue();
//                    sum_rate1 = sum_rate1 + (mBatch != null ?
//                            (mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0) : 0.0);
//                    sum_SaleRate = sum_SaleRate + (mBatch != null ?
//                            (mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.0) : 0.0);
//                    sum_CostTaxRate = sum_CostTaxRate + (mBatch != null ?
//                            (mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.0) : 0.0);
//                    sum_CostWoTaxRate = sum_CostWoTaxRate + (mBatch != null ?
//                            (mBatch.getCosting() != null ? mBatch.getCosting() : 0.0) : 0.0);
//                    sum_mrp = sum_mrp + (mBatch != null ?
//                            (mBatch.getMrp() != null ? mBatch.getMrp() : 0.0) : 0.0);

                    for (int j = 0; j < batchUnits.size(); j++) {
                        JsonObject inspObj = new JsonObject();
                        if (batchUnits != null) {
                            inspObj.addProperty("batch_no", batchUnits.get(j)[0]);
                            inspObj.addProperty("batch_id", batchUnits.get(j)[1]);
                            inspObj.addProperty("unit_id", batchUnits.get(j)[2]);
                            inspObj.addProperty("unit_name", batchUnits.get(j)[3]);
                            double qty = batchUnits.get(j)[4] != null && batchUnits.get(j)[5] != null ? Double.parseDouble((batchUnits.get(j)[4])) + Double.parseDouble(batchUnits.get(j)[5])
                                    : (batchUnits.get(j)[4] != null ? Double.parseDouble(batchUnits.get(j)[4]) : batchUnits.get(j)[5] != null ? Double.parseDouble(batchUnits.get(j)[5]) : 0);
                            inspObj.addProperty("unit_qty", qty);
                            inspObj.addProperty("unit_conv", batchUnits.get(j)[6] != null ? Double.parseDouble(batchUnits.get(j)[6]) : 1.0);
                            inspObj.addProperty("unit_saleRate", batchUnits.get(j)[7] != null ? Double.parseDouble(batchUnits.get(j)[7]) : 0.0);
                            inspObj.addProperty("unit_salevalue", (batchUnits.get(j)[7] != null && qty != 0.0) ? Double.parseDouble(batchUnits.get(j)[7]) * qty : 0.0);
                            inspObj.addProperty("unit_purchaseRate", batchUnits.get(j)[8] != null ? Double.parseDouble(batchUnits.get(j)[8]) : 0.0);
                            inspObj.addProperty("unit_purchasevalue", (batchUnits.get(j)[8] != null && qty != 0.0) ? Double.parseDouble(batchUnits.get(j)[8]) * qty : 0.0);
                            inspObj.addProperty("unit_costing", batchUnits.get(j)[9] != null ? Double.parseDouble(batchUnits.get(j)[9]) : 0.0);
                            inspObj.addProperty("unit_costingValue", (batchUnits.get(j)[9] != null && qty != 0.0) ? Double.parseDouble(batchUnits.get(j)[9]) * qty : 0.0);
                            inspObj.addProperty("unit_costingWT", batchUnits.get(j)[10] != null ? Double.parseDouble(batchUnits.get(j)[10]) : 0.0);
                            inspObj.addProperty("unit_costingWTValue", (batchUnits.get(j)[10] != null && qty != 0.0) ? Double.parseDouble(batchUnits.get(j)[10]) * qty : 0.0);
                            inspObj.addProperty("unit_mrp", batchUnits.get(j)[11] != null ? Double.parseDouble(batchUnits.get(j)[11]) : 0.0);
                            inspObj.addProperty("unit_mrpValue", (batchUnits.get(j)[11] != null && qty != 0.0) ? Double.parseDouble(batchUnits.get(j)[11]) * qty : 0.0);

                        }
                        UnitArray.add(inspObj);
                    }
                    System.out.println("batchUnits" + batchUnits);

                    object.add("unit_data", UnitArray);

                    Long fiscalId = null;
                    LocalDate currentDate = LocalDate.now();
                    FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
                    if (fiscalYear != null)
                        fiscalId = fiscalYear.getId();
                    Double closing1 = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(), mProduct.getId(),
                            mBatch.getLevelA() != null ? mBatch.getLevelA().getId() : null, null, null, mBatch.getUnits().getId(),
                            mBatch != null ? mBatch.getId() : null, fiscalId);
                    object.addProperty("closing_stock", closing1);
                    batchArray.add(object);
                    t_product_qty = t_product_qty + (mBatch.getQnty() != null ? mBatch.getQnty() : 0.0);

                }
            }
//            t_qty1 = t_qty1 + t_product_qty;
//            t_product_rate = (productbatch.size() != 0 && sum_rate1 != 0) ? (sum_rate1 / productbatch.size()) : 0.0;
//            Tsum_SaleRate = (productbatch.size() != 0 && sum_SaleRate != 0) ? (sum_SaleRate / productbatch.size()) : 0.0;
//            Tsum_CostTaxRate = (productbatch.size() != 0 && sum_CostTaxRate != 0) ? (sum_CostTaxRate / productbatch.size()) : 0.0;
//            Tsum_CostWoTaxRate = (productbatch.size() != 0 && sum_CostWoTaxRate != 0) ? (sum_CostWoTaxRate / productbatch.size()) : 0.0;
//            Tsum_mrp = (productbatch.size() != 0 && sum_mrp != 0) ? (sum_mrp / productbatch.size()) : 0.0;

//            all_product_purRate = all_product_purRate + t_product_rate;
//            all_product_saleRate = all_product_saleRate + Tsum_SaleRate;
//            all_product_costTaxRate = all_product_costTaxRate + Tsum_CostTaxRate;
//            all_product_costWoTaxRate = all_product_costWoTaxRate + Tsum_CostWoTaxRate;
//            all_product_mrp = all_product_mrp + Tsum_mrp;

//            mObject.addProperty("total_product_qty", t_product_qty);
//            mObject.addProperty("total_product_purrate", t_product_rate);
//            mObject.addProperty("total_product_salerate", Tsum_SaleRate);
//            mObject.addProperty("total_product_costTaxrate", Tsum_CostTaxRate);
//            mObject.addProperty("total_product_costWoTaxrate", Tsum_CostWoTaxRate);
//            mObject.addProperty("total_product_mrp", Tsum_mrp);
//
//            mObject.addProperty("total_product_purvalue", t_product_qty * t_product_rate);
//            mObject.addProperty("total_product_salevalue", t_product_qty * Tsum_SaleRate);
//            mObject.addProperty("total_product_costTaxvalue", t_product_qty * Tsum_CostTaxRate);
//            mObject.addProperty("total_product_costWoTaxvalue", t_product_qty * Tsum_CostWoTaxRate);
//            mObject.addProperty("total_product_mrpValue", t_product_qty * Tsum_mrp);

            mObject.add("batch_data", batchArray);

            jsonArray.add(mObject);
        }
//        finalResult.addProperty("total_closing_qty", t_qty1);
//        finalResult.addProperty("total_PurRate", all_product_purRate);
//        finalResult.addProperty("total_PurValue", t_qty1 * all_product_purRate);
//        finalResult.addProperty("total_saleRate", all_product_saleRate);
//        finalResult.addProperty("total_saleValue", t_qty1 * all_product_saleRate);
//        finalResult.addProperty("total_costTaxRate", all_product_costTaxRate);
//        finalResult.addProperty("total_costTaxValue", t_qty1 * all_product_costTaxRate);
//        finalResult.addProperty("total_costWoTaxRate", all_product_costWoTaxRate);
//        finalResult.addProperty("total_costWoTaxValue", t_qty1 * all_product_costWoTaxRate);
//        finalResult.addProperty("total_mrpRate", all_product_mrp);
//        finalResult.addProperty("total_mrpValue", t_qty1 * all_product_mrp);

//        finalResult.addProperty("total_closing_qty", t_qty);
//        finalResult.addProperty("total_Rate", t_rate);
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }


    public InputStream exportToExcelPayable(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos " + productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"LEDGER NAME", " BILL NO", "DATE", " INVOICE AMOUNT", "PAID AMOUNT", "BALANCE AMOUNT ", "TYPE", "DUE DATE", "DUE DAYS"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("Payable");

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
                        row.createCell(0).setCellValue(batchNo.get("Ledger_name").getAsString());
//                        row.createCell(1).setCellValue(batchNo.get("").getAsString());
//                        row.createCell(2).setCellValue(batchNo.get("").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("total_amount").getAsDouble());
                        row.createCell(4).setCellValue(batchNo.get("total_paid_amt").getAsDouble());
                        row.createCell(5).setCellValue(batchNo.get("total_balance").getAsDouble());


                        sumOfQty += batchNo.get("total_amount").getAsDouble();
                        sumOfQty1 += batchNo.get("total_paid_amt").getAsDouble();
                        sumOfQty2 += batchNo.get("total_balance").getAsDouble();


                        JsonArray invoiceData = batchNo.get("invoice_data").getAsJsonArray();
                        JsonObject invoiceDataObject = null;
                        for (int j = 0; j < invoiceData.size(); j++) {
                            invoiceDataObject = invoiceData.get(j).getAsJsonObject();
                            Row row1 = sheet.createRow(rowIdx++);
                            row1.createCell(1).setCellValue(invoiceDataObject.get("Invoice_no").getAsString());
                            row1.createCell(2).setCellValue(invoiceDataObject.get("invoiceDate").getAsString());
                            row1.createCell(3).setCellValue(invoiceDataObject.get("total_amount").getAsDouble());
                            row1.createCell(4).setCellValue(invoiceDataObject.get("paid_amt").getAsDouble());
                            row1.createCell(5).setCellValue(invoiceDataObject.get("remaining_amt").getAsDouble());
                            row1.createCell(6).setCellValue(invoiceDataObject.get("balancing_type").getAsString());
                            row1.createCell(7).setCellValue(invoiceDataObject.get("Due_date").getAsString());
                            row1.createCell(8).setCellValue(invoiceDataObject.get("overDueDays").getAsDouble());


                        }
                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(3);
                    cell.setCellValue(sumOfQty);
                    Cell cell1 = prow.createCell(4);
                    cell1.setCellValue(sumOfQty1);
                    Cell cell2 = prow.createCell(5);
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

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    public InputStream exportToExcelReceivable(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos " + productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"LEDGER NAME", " BILL NO", "DATE", " INVOICE AMOUNT", "PAID AMOUNT", "BALANCE AMOUNT ", "TYPE", "DUE DATE", "DUE DAYS"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("Receivable");

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
                        row.createCell(0).setCellValue(batchNo.get("Ledger_name").getAsString());
//                        row.createCell(1).setCellValue(batchNo.get("").getAsString());
//                        row.createCell(2).setCellValue(batchNo.get("").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("total_amount").getAsDouble());
                        row.createCell(4).setCellValue(batchNo.get("total_paid_amt").getAsDouble());
                        row.createCell(5).setCellValue(batchNo.get("total_balance").getAsDouble());


                        sumOfQty += batchNo.get("total_amount").getAsDouble();
                        sumOfQty1 += batchNo.get("total_paid_amt").getAsDouble();
                        sumOfQty2 += batchNo.get("total_balance").getAsDouble();


                        JsonArray invoiceData = batchNo.get("invoice_data").getAsJsonArray();
                        JsonObject invoiceDataObject = null;
                        for (int j = 0; j < invoiceData.size(); j++) {
                            invoiceDataObject = invoiceData.get(j).getAsJsonObject();
                            Row row1 = sheet.createRow(rowIdx++);
                            row1.createCell(1).setCellValue(invoiceDataObject.get("Invoice_no").getAsString());
                            row1.createCell(2).setCellValue(invoiceDataObject.get("invoiceDate").getAsString());
                            row1.createCell(3).setCellValue(invoiceDataObject.get("total_amount").getAsDouble());
                            row1.createCell(4).setCellValue(invoiceDataObject.get("paid_amt").getAsDouble());
                            row1.createCell(5).setCellValue(invoiceDataObject.get("remaining_amt").getAsDouble());
                            row1.createCell(6).setCellValue(invoiceDataObject.get("balancing_type").getAsString());
                            row1.createCell(7).setCellValue(invoiceDataObject.get("Due_date").getAsString());
                            row1.createCell(8).setCellValue(invoiceDataObject.get("overDueDays").getAsDouble());


                        }
                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(3);
                    cell.setCellValue(sumOfQty);
                    Cell cell1 = prow.createCell(4);
                    cell1.setCellValue(sumOfQty1);
                    Cell cell2 = prow.createCell(5);
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

        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

}


