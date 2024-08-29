package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.ReceiptReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ReceiptReportController {
    @Autowired
    private ReceiptReportService service;

    @PostMapping(path = "/get_monthwise_receipt_details")
    public Object getMonthwiseReceiptTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseReceiptTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_receipt_details")
    public Object getReceiptTransactionDetails(HttpServletRequest request) {
        return service.getReceiptTransactionDetails(request).toString();
    }

    @PostMapping(path = "/exportToExcelReceipt")
    public ResponseEntity<?> exportToExcelReceipt(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelReceiptReport.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelReceipt(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }


    @PostMapping(path = "/exportToExcelReceiptmonth")
    public ResponseEntity<?> exportToExcelReceiptmonth(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelReceiptReport.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelReceiptmonth(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

}
