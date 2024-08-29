package in.truethics.ethics.ethicsapiv10.controller.masters;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.OutletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@RestController
public class OutletController {
    @Autowired
    private OutletService outletService;

    @PostMapping(path = "/create_company")
    public ResponseEntity<?> createOutlet(MultipartHttpServletRequest request) throws ParseException {
        return ResponseEntity.ok(outletService.createOutlet(request));
    }


    /* update Company by id */
    @PostMapping(path = "/update_company")
    public Object updateOutlet(MultipartHttpServletRequest request) throws ParseException {
        JsonObject object = outletService.updateOutlet(request);
        return object.toString();
    }

    /* get Company by super admin  */
    @PostMapping(path = "/get_company_by_id")
    public Object getOutletById(HttpServletRequest request) {
        JsonObject result = outletService.getOutletById(request);
        return result.toString();
    }

    @PostMapping(path = "/get_company_by_user")
    public Object getOutletByUserId(HttpServletRequest request) {
        JsonObject result = outletService.getOutletByUser(request);
        return result.toString();
    }

    /* get all companies of super admin */
    @GetMapping(path = "/get_companies_super_admin")
    public Object getOutletsOfSuperAdmin(HttpServletRequest request) {
        JsonObject result = outletService.getOutletsOfSuperAdmin(request);
        return result.toString();
    }
 /***** get Company of Super Admin for Company List *****/
    @GetMapping(path = "/get_companies_data")
    public Object getCompanySuperAdmin(HttpServletRequest request) {
        JsonObject result = outletService.getCompanySuperAdmin(request);
        return result.toString();
    }

    /* Get GstTypemaster */
    @GetMapping(path = "/get_gst_type")
    public Object getGstType() {
        JsonObject res = outletService.getGstType();
        return res.toString();
    }

    @PostMapping(path = "/validate_company")
    public ResponseEntity<?> validateOutlet(HttpServletRequest request) throws ParseException {
        return ResponseEntity.ok(outletService.validateOutlet(request));
    }

    @PostMapping(path = "/validate_company_update")
    public ResponseEntity<?> validateOutletUpdate(HttpServletRequest request) throws ParseException {
        return ResponseEntity.ok(outletService.validateOutletUpdate(request));
    }
    /**** get picode Master from pincode *****/
    @PostMapping(path = "/get_pincode")
    public Object getPincode(HttpServletRequest request) throws ParseException {
        JsonObject object = outletService.getPincode(request);
        return object.toString();
    }
    @PostMapping(path= "/delete_company")
    public Object companyDelete(HttpServletRequest request){
        JsonObject object =outletService.companyDelete(request);
        return object.toString();
    }
    /**** validate picode  *****/
    @PostMapping(path = "/validate_pincode")
    public Object validatePincode(HttpServletRequest request) throws ParseException {
        JsonObject object = outletService.validatePincode(request);
        return object.toString();
    }
}
