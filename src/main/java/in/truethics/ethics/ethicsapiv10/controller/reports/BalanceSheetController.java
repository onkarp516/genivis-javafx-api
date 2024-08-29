package in.truethics.ethics.ethicsapiv10.controller.reports;

import in.truethics.ethics.ethicsapiv10.service.reports_service.BalanceSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class BalanceSheetController {

    @Autowired
    BalanceSheetService balanceSheetService;

    @PostMapping(path = "/get_balance_sheet_ac")
    public Object getBalanceSheetAc(HttpServletRequest request) {
        return balanceSheetService.getBalanceSheetAc(request).toString();
    }


    @PostMapping(path = "/get_balance_ac_step1")
    public Object getBalanceAcStep1(HttpServletRequest request) {
        return balanceSheetService.getBalanceAcStep1(request).toString();
    }
    @PostMapping(path = "/get_balance_ac_step2")
    public Object getBalanceAcStep2(HttpServletRequest request) {
        return balanceSheetService.getBalanceAcStep2(request).toString();
    }

    @PostMapping(path = "/get_balance_ac_step3")
    public Object getBalanceAcStep3(HttpServletRequest request) {
        return balanceSheetService.getBalanceAcStep3(request).toString();
    }
    @PostMapping(path = "/get_balance_ac_step4")
    public Object getbalanceacstep4(HttpServletRequest request) {
        return balanceSheetService.getbalanceacstep4(request).toString();
    }
}
