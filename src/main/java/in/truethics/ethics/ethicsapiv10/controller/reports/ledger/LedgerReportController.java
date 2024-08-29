package in.truethics.ethics.ethicsapiv10.controller.reports.ledger;

import in.truethics.ethics.ethicsapiv10.service.reports_service.LedgerReportService;
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
public class LedgerReportController {
    @Autowired
    LedgerReportService ledgerReportService;
   /* @PostMapping(path = "/get_profit_and_loss_ac")
    public Object getProfitAndLossAc(HttpServletRequest request) {
        return profitAndLossService.getProfitAndLossAc(request).toString();
    }
    @PostMapping(path = "/get_profit_and_loss_ac_step1")
    public Object getProfitAndLossAcStep1(HttpServletRequest request) {
        return profitAndLossService.getProfitAndLossAcStep1(request).toString();
    }
    @PostMapping(path = "/get_profit_and_loss_ac_step2")
    public Object getProfitAndLossAcStep2(HttpServletRequest request) {
        return profitAndLossService.getProfitAndLossAcStep2(request).toString();
    }*/

    /***** getting all transactions against ledger id *****/
    @PostMapping(path = "/get_ledger_tranx_details_report")
    public Object getLedgerTransactionsDetails(HttpServletRequest request) {
        return ledgerReportService.getLedgerTransactionsDetails(request).toString();
    }

    @PostMapping(path = "/get_ledger_tranx_details_report_with_dates")
    public Object getLedgerTransactionsDetailsWithDates(HttpServletRequest request) {
        return ledgerReportService.getLedgerTransactionsDetailsWithDates(request).toString();
    }

    /***** getting details of transaction against invoice number *****/
    @PostMapping(path = "/get_tranx_details_report")
    public Object getTransactionsDetails(HttpServletRequest request) {
        return ledgerReportService.getTransactionsDetailsReports(request).toString();
    }
    @PostMapping(path="/get_monthwise_tranx_details")
        public Object getMonthwiseTransactionDetails(HttpServletRequest request){
            return ledgerReportService.getMonthwiseTranscationDetails(request).toString();
        }

    @PostMapping(path = "/get_tranx_detail_of_month")
    public Object getTranxDetailofMonth(HttpServletRequest request) {
        return ledgerReportService.getTranxDetailofMonth(request).toString();
    }

    @PostMapping(path = "/mobile/get_ledger_tranx_details_report")
    public Object getMobileLedgerTransactionsDetails(@RequestBody Map<String, String> request) {
        return ledgerReportService.getMobileLedgerTransactionsDetails(request).toString();
    }
  //API for Export TO Excel
    @PostMapping(path = "/exportExcelLedgerReport1")
    public Object exportLedgerReport1(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "excelLedgerReport1.xlsx";
        InputStreamResource file = new InputStreamResource(ledgerReportService.exportLedgerReport1(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @PostMapping(path = "/exportExcelLedgerReport2")
    public Object exportLedgerReport2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "excelLedgerReport1.xlsx";
        InputStreamResource file = new InputStreamResource(ledgerReportService.exportLedgerReport2(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @PostMapping(path = "/exportToExcelLedgerReport3")
    public ResponseEntity<?> exportToExcelLedgerReport3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "ExcelLedgerReport3.xlsx";
            InputStreamResource file = new InputStreamResource(ledgerReportService.exportToExcelLedgerReport3(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }


}
