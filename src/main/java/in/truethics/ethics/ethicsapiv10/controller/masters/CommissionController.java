package in.truethics.ethics.ethicsapiv10.controller.masters;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ContentMasterRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.CommissionMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@RestController
public class CommissionController {

    @Autowired
    private CommissionMasterService commissionMasterService;

    @PostMapping(path = "/create_commission_master")
    public ResponseEntity<?> createCommissionMaster(HttpServletRequest request) {
        return ResponseEntity.ok(commissionMasterService.createCommissionMaster(request));
    }

    @GetMapping(path = "/get_all_commission_master")
    public Object getAllCommissionMaster(HttpServletRequest request) {
        JsonObject result = commissionMasterService.getAllCommissionMaster(request);
        return result.toString();
    }

    @PostMapping(path = "/get_commission_master_by_id")
    public Object getCommissionMasterById(HttpServletRequest request) {
        JsonObject result = commissionMasterService.getCommissionMasterById(request);
        return result.toString();
    }

    @PostMapping(path = "/update_commission_master")
    public Object updateCommissionMaster(HttpServletRequest request) {
        JsonObject result = commissionMasterService.updateCommissionMaster(request);
        return result.toString();
    }

    @PostMapping(path = "/remove_commission_master")
    public Object removeCommissionMaster(HttpServletRequest request) {
        JsonObject result = commissionMasterService.removeCommissionMaster(request);
        return result.toString();
    }

    @PostMapping(path = "/validate_commission_master")
    public ResponseEntity<?> validateCommissionMaster(HttpServletRequest request) throws ParseException {
        return ResponseEntity.ok(commissionMasterService.validateCommissionMaster(request));
    }

    @PostMapping(path = "/validate_commission_master_update")
    public ResponseEntity<?> validateCommissionMasterUpdate(HttpServletRequest request) throws ParseException {
        return ResponseEntity.ok(commissionMasterService.validateCommissionMasterUpdate(request));
    }

    @PostMapping(path = "/get_partner_commission_payment")
    public Object getPartnerCommissionPayment(HttpServletRequest request) throws ParseException {
        JsonObject object = commissionMasterService.getPartnerCommissionPayment(request);
        return object.toString();
    }

}
