package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.TaxMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class TaxMasterController {
    @Autowired
    private TaxMasterService service;

    @PostMapping(path = "/create_tax_master")
    public Object createTaxMaster(HttpServletRequest request) {
        JsonObject result = service.createTaxMaster(request);
        return result.toString();
    }

    /* update Tax Master by id */
    @PostMapping(path = "/update_tax_master")
    public Object updateTaxMaster(HttpServletRequest request) {
        JsonObject jsonObject = service.updateTaxMaster(request);
        return jsonObject.toString();
    }

    /* Get All Tax Master of outlet */
    @GetMapping(path = "/get_tax_master_by_outlet")
    public Object getTaxMaster(HttpServletRequest request) {
        JsonObject result = service.getTaxMaster(request);
        return result.toString();
    }

    /* get Tax Master by id */
    @PostMapping(path = "/get_tax_master_by_id")
    public Object getTaxMasterbyId(HttpServletRequest request) {
        JsonObject result = service.getTaxMasterbyId(request);
        return result.toString();
    }
    @PostMapping(path = "/validate_Tax")
    public Object validateTax(HttpServletRequest request) {
        JsonObject object = service.validateTax(request);
        return object.toString();
    }

    @PostMapping(path = "/delete_product_tax")
    public Object productTaxDelete(HttpServletRequest request) {
        JsonObject object = service.productTaxDelete(request);
        return object.toString();
    }
}
