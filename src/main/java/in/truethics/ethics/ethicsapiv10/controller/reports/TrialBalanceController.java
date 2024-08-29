package in.truethics.ethics.ethicsapiv10.controller.reports;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.reports_service.TrialBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
@RestController
public class TrialBalanceController {
    @Autowired
    private TrialBalanceService service;

    /* get All Ledgers of outlets with Dr and Cr */
    @GetMapping(path = "/get_all_ledgers_trial_balance")
    public Object getAllLedgers(HttpServletRequest request) {
        JsonObject result = service.getAllLedgers(request);
        return result.toString();
    }
}
