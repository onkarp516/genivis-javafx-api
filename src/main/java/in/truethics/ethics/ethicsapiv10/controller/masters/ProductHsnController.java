package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductHsnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ProductHsnController {
    @Autowired
    ProductHsnService service;

    @PostMapping(path = "/create_hsn")
    public ResponseEntity<?> createHsn(HttpServletRequest request) {
        return ResponseEntity.ok(service.createHsn(request));
    }

    /* update Hsn by id */
    @PostMapping(path = "/update_hsn")
    public Object updateHsn(HttpServletRequest request) {
        JsonObject jsonObject = service.updateHsn(request);
        return jsonObject.toString();
    }

    /* Get All Hsn of outlet */
    @GetMapping(path = "/get_hsn_by_outlet")
    public Object getHsn(HttpServletRequest request) {
        JsonObject result = service.getHsn(request);
        return result.toString();
    }
    //testing of HSN testing
//    @PostMapping(path = "/hsn_list_test")
//    public Object getHsnTest(@RequestBody Map<String, String> request,HttpServletRequest req) {
//        return service.getHsnTest(request,req);
//    }

    /* get Hsn by id */
    @PostMapping(path = "/get_hsn_by_id")
    public Object getHsnbyId(HttpServletRequest request) {
        JsonObject result = service.getHsnbyId(request);
        return result.toString();
    }

    @PostMapping(path = "/validate_HSN")
    public Object validateHSN(HttpServletRequest request) {
        JsonObject object = service.validateHSN(request);
        return object.toString();
    }

    @PostMapping(path = "/validate_HSN_update")
    public Object validateHSNUpdate(HttpServletRequest request) {
        JsonObject object = service.validateHSNUpdate(request);
        return object.toString();
    }

    @PostMapping(path = "/delete_product_hsn")
    public Object producthsnDelete(HttpServletRequest request) {
        JsonObject object = service.producthsnDelete(request);
        return object.toString();
    }

    @PostMapping(path = "/search_hsn")
    public Object searchHsn(HttpServletRequest request) {
        JsonObject object = service.searchHsn(request);
        return object.toString();
    }
}
