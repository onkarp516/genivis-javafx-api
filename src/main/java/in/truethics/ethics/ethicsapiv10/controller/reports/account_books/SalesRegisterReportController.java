package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.ContraReportService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.SalesRegisterReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class SalesRegisterReportController {
    @Autowired
    private SalesRegisterReportService service;

    @PostMapping(path = "/get_monthwise_sales_register_details")
    public Object getMonthwiseSalesRegisterTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseSalesRegisterTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_sales_register_details")
    public Object getSalesRegisterTransactionDetails(HttpServletRequest request) {
        return service.getSalesRegisterTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_sales_order_details")
    public Object getSalesOrderTransactionDetails(HttpServletRequest request) {
        return service.getSalesOrderTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_monthwise_sales_order_details")
    public Object getMonthwiseSalesOrderTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseSalesOrderTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_sales_Challan_details")
    public Object getSalesChallanTransactionDetails(HttpServletRequest request) {
        return service.getSalesChallanTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_monthwise_sales_challan_details")
    public Object getMonthwiseSalesChallanTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseSalesChallanTransactionDetails(request).toString();
    }

//    Sales Register month Data Export to excel
    @PostMapping(path = "/export_xls_slsReg_month")
    public ResponseEntity<?> exportToExcelSalesRegisterForMonth(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelExportSalesRegisterOfMonth.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelSalesRegisterForMonth(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

//    Sales Register Year Data export to excel
    @PostMapping(path = "/exportToExcelsalesRegisteryear")
    public ResponseEntity<?> exportToExcelsalesRegisteryear(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelExcelSalesRegisterOfYear.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelsalesRegisteryear(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

    //    Sales Order month Data Export to excel
    @PostMapping(path = "/export_xls_slsOrder_month")
    public ResponseEntity<?> exportToExcelSalesOrderForMonth(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelExportSalesOrderOfMonth.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelSalesOrderForMonth(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

    //    Sales Order Year Data export to excel
    @PostMapping(path = "/exportToExcelsalesOrderyear")
    public ResponseEntity<?> exportToExcelsalesOrderyear(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelExcelSalesOrderOfYear.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelsalesOrderyear(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

    //    Sales Challan month Data Export to excel
    @PostMapping(path = "/export_xls_slsChallan_month")
    public ResponseEntity<?> exportToExcelSalesChallanForMonth(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelExportSalesChallanOfMonth.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelSalesChallanForMonth(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

    //    Sales Challan Year Data export to excel
    @PostMapping(path = "/exportToExcelsalesChallanyear")
    public ResponseEntity<?> exportToExcelsalesChallanyear(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelExcelSalesRegisterOfYear.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelsalesChallanyear(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }
}
