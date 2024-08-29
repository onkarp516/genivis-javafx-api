package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.JournalReportService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.PaymentReportService;
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
public class JournalReportController {
    @Autowired
    private JournalReportService service;

    @PostMapping(path = "/get_monthwise_journal_details")
    public Object getMonthwiseJournalTransactionDetails(HttpServletRequest request) {
        return service.getMonthwiseJournalTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_journal_details")
    public Object getJournalTransactionDetails(HttpServletRequest request) {
        return service.getJournalTransactionDetails(request).toString();
    }

    @PostMapping(path = "/exportToExcelJournal")
    public ResponseEntity<?> exportToExcelJournal(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelJournalReport.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelJournal(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }


    @PostMapping(path = "/exportToExcelJournalmonth")
    public ResponseEntity<?> exportToExcelJournalmonth(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelJournalReport.xlsx";
            InputStreamResource file = new InputStreamResource(service.exportToExcelJournalmonth(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

}
