package in.truethics.ethics.ethicsapiv10.controller.reports;

import in.truethics.ethics.ethicsapiv10.service.reports_service.ProfitAndLossService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ProfitAndLossReportController {
    @Autowired
    ProfitAndLossService profitAndLossService;
    @PostMapping(path = "/get_profit_and_loss_ac")
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
    }
    @PostMapping(path = "/get_profit_and_loss_ac_step3")
    public Object getProfitAndLossAcStep3(HttpServletRequest request) {
        return profitAndLossService.getProfitAndLossAcStep3(request).toString();
    }
}
