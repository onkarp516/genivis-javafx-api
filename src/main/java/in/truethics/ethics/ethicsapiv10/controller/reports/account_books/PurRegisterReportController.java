package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.PurRegisterReportService;
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
public class PurRegisterReportController {
    @Autowired
    private PurRegisterReportService service;

    @PostMapping(path = "/get_monthwise_pur_register_details")
    public Object getMonthwisePurRegisterTransactionDetails(HttpServletRequest request) {
        return service.getMonthwisePurRegisterTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_pur_register_details")
    public Object getPurRegisterTransactionDetails(HttpServletRequest request) {
        return service.getPurRegisterTransactionDetails(request).toString();
    }

    @PostMapping(path = "/get_pur_register_order_details")
    public Object getPurRegisterOrderDetails(HttpServletRequest request) {
        return service.getPurRegisterOrderDetails(request).toString();
    }


    @PostMapping(path = "/get_monthwise_pur_reg_order_details")
    public Object getMonthwisePurRegisterOrderDetails(HttpServletRequest request) {
        return service.getMonthwisePurRegisterOrderDetails(request).toString();
    }

    @PostMapping(path = "/get_pur_register_challan_details")
    public Object getPurRegisterChallanDetails(HttpServletRequest request) {
        return service.getPurRegisterChallanDetails(request).toString();
    }


    @PostMapping(path = "/get_monthwise_pur_reg_challan_details")
    public Object getMonthwisePurRegisterChallanDetails(HttpServletRequest request) {
        return service.getMonthwisePurRegisterChallanDetails(request).toString();
    }

    @PostMapping(path = "/exportToExcelPurchaseOrder")
    public ResponseEntity<?> exportToExcelPurchaseOrder(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelPurchaseOrder.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelPurchaseOrder(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

    @PostMapping(path = "/exportToExcelPurchasechallan")
    public ResponseEntity<?> exportToExcelPurchaseChallan(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelPurchaseChallan.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelPurchaseChallan(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

    @PostMapping(path = "/exportToExcelPurchaseRegm")
    public ResponseEntity<?> exportToExcelPurchasereg2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelPurchaseRegister.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelPurchasereg2(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

    @PostMapping(path = "/exportToExcelPurchaseOrdermonth")
    public ResponseEntity<?> exportToExcelPurchaseOrdermonth(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelPurchaseOrder.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelPurchaseOrdermonth(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

    @PostMapping(path = "/exportToExcelPurchaseChallanmonth")
    public ResponseEntity<?> exportToExcelPurchaseChallanmonth(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelPurchaseChallan.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelPurchaseChallanmonth(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

}
