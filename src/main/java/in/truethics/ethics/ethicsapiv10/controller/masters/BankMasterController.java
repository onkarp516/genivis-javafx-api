package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.BankMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class BankMasterController {
    @Autowired
    private BankMasterService bankMasterService;

    @PostMapping(path = "/create_bank_master")
    public ResponseEntity<?> createBankMaster(HttpServletRequest request) {
        return ResponseEntity.ok(bankMasterService.createBankMaster(request));
    }

    /* Get all Bank Master of Outlets */
    @GetMapping(path = "/get_outlet_bank_master")
    public Object getAllBankMaster(HttpServletRequest request) {
        JsonObject result = bankMasterService.getAllBankMaster(request);
        return result.toString();
    }

    /* get Bank Master by Id */
    @PostMapping(path = "/get_bank_master_by_id")
    public Object getBankMaster(HttpServletRequest request) {
        JsonObject result = bankMasterService.getBankMaster(request);
        return result.toString();
    }
    @PostMapping(path = "/update_bank_master")
    public Object updateBankMaster(HttpServletRequest request) {
        JsonObject result = bankMasterService.updateBankMaster(request);
        return result.toString();
    }
    @PostMapping(path = "/remove_bank_master")
    public Object removeBankMaster(HttpServletRequest request) {
        JsonObject result = bankMasterService.removeBankMaster(request);
        return result.toString();
    }
}
