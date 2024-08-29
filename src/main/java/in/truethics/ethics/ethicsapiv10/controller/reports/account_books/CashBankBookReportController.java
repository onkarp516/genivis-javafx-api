package in.truethics.ethics.ethicsapiv10.controller.reports.account_books;

import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.CashBankBookReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CashBankBookReportController {
    @Autowired
    private CashBankBookReportService service;
    @PostMapping(path = "/get_cashbook_details")
    public Object getCashBookTransactionDetails(HttpServletRequest request) {
        return service.getCashBookTransactionDetails(request).toString();
    }
    @PostMapping(path = "/get_expenses_reports")
    public Object getExpensesReports(HttpServletRequest request) {
        return service.getExpensesReports(request).toString();
    }

}
