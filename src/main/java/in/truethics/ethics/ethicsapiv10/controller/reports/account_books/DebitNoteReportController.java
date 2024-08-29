package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.ContraReportService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.DebitNoteReportService;
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
public class DebitNoteReportController {
    @Autowired
    private DebitNoteReportService service;

    @PostMapping(path = "/get_monthwise_debitnote_details")
    public Object getMonthwiseDebitNoteTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseDebitNoteTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_debitnote_details")
    public Object getDebitNoteTransactionDetails(HttpServletRequest request) {
        return service.getDebitNoteTransactionDetails(request).toString();
    }


    @PostMapping(path = "/exportToExcelDebitNote")
    public ResponseEntity<?> exportToExcelDebitNote(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelDebitNoteReport.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelDebitNote(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }


    @PostMapping(path = "/exportToExcelDebitNotemonth")
    public ResponseEntity<?> exportToExcelDebitNotemonth(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelDebitNoteReport.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelDebitNotemonth(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }




}
