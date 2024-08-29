package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.SalesmanMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SalesmanMasterController {
    @Autowired
    private SalesmanMasterService service;

    @PostMapping(path = "/create_salesman_master")
    public ResponseEntity<?> createSalesmanMaster(HttpServletRequest request) {
        return ResponseEntity.ok(service.createSalesmanMaster(request));
    }

    /* Get all Salesman Master of Outlets */
    @GetMapping(path = "/get_outlet_salesman_master")
    public Object getAllSalesmanMaster(HttpServletRequest request) {
        JsonObject result = service.getAllSalesmanMaster(request);
        return result.toString();
    }

    /* get Salesman Master by Id */
    @PostMapping(path = "/get_salesman_master_by_id")
    public Object getSalesmanMaster(HttpServletRequest request) {
        JsonObject result = service.getSalesmanMaster(request);
        return result.toString();
    }

    @PostMapping(path = "/update_salesman_master")
    public ResponseEntity<?> updateSalesmanMaster(HttpServletRequest request) {
        return ResponseEntity.ok(service.updateSalesmanMaster(request));
    }
    /* Validate Salesman Masters */
    @PostMapping(path = "/validate_salesman_master")
    public Object validateAssocitesgroup(HttpServletRequest request) {
        JsonObject object = service.duplicateSalesmanMaster(request);
        return  object.toString();
    }
    /* Validate Salesman Master for update  */
    @PostMapping(path = "/validate_salesman_master_update")
    public Object validateAssocitesgroupUpdate(HttpServletRequest request) {
        JsonObject object = service.duplicateSalesmanMasterUpdate(request);
        return  object.toString();
    }
    @PostMapping(path = "/remove_salesman_master")
    public Object removeSalesmanMaster(HttpServletRequest request) {
        JsonObject result = service.removeSalesmanMaster(request);
        return result.toString();
    }
}
