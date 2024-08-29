package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.ContraReportService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.JournalReportService;
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
public class ContraReportController {
    @Autowired
    private ContraReportService service;

    @PostMapping(path = "/get_monthwise_contra_details")
    public Object getMonthwiseContraTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseContraTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_conta_details")
    public Object getContraTransactionDetails(HttpServletRequest request) {
        return service.getContraTransactionDetails(request).toString();
    }


    @PostMapping(path = "/exportToExcelContra")
    public ResponseEntity<?> exportToExcelContra(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelContraReport.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelContra(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }


    @PostMapping(path = "/exportToExcelContramonth")
    public ResponseEntity<?> exportToExcelContramonth(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelContraReport.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelContramonth(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }


}
