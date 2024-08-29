package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.FranchiseMaster;
import in.truethics.ethics.ethicsapiv10.service.master_service.FranchiseMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Map;

@RestController
public class FranchiseMasterController {
    @Autowired
    private FranchiseMasterService franchiseMasterService;
    @PostMapping(path = "/create_franchise")
    public ResponseEntity<?> createFranchise(HttpServletRequest request) throws ParseException {
        return ResponseEntity.ok(franchiseMasterService.createFranchise(request));
    }

/*    @PostMapping(path = "/saveFranchiseSuperadmin")
    public Object saveFranchiseSuperadmin(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request){
        return franchiseMasterService.saveFranchiseSuperadmin(jsonRequest, request);

    }*/

    @PostMapping(path = "/get_franchise_by_id")
    public Object getFranchiseById(HttpServletRequest request) {
        JsonObject result = franchiseMasterService.getFranchiseById(request);
        return result.toString();
    }
    @PostMapping(path = "/update_franchise")
    public Object updateFranchise(HttpServletRequest request) throws ParseException {
        JsonObject object = franchiseMasterService.updateFranchise(request);
        return object.toString();
    }
    @GetMapping(path = "/get_all_franchise")
    public Object getAllFranchise(HttpServletRequest request) {
        JsonObject result = franchiseMasterService.getAllFranchise(request);
        return result.toString();
    }
    @PostMapping(path = "/mobile/get_total_franchise")
    public Object getTotalFranchise(HttpServletRequest request) {
        JsonObject result = franchiseMasterService.getTotalFranchise(request);
        return result.toString();
    }
    @PostMapping(path = "/validate_franchise_code")
    public Object validateFranchiseCode(HttpServletRequest request) {
        JsonObject object = franchiseMasterService.validateFranchiseCode(request);
        return object.toString();
    }

    @PostMapping(path = "/validate_franchise_update")
    public ResponseEntity<?> validateFranchiseUpdate(HttpServletRequest request) throws ParseException {
        return ResponseEntity.ok(franchiseMasterService.validateFranchiseUpdate(request));
    }
}
