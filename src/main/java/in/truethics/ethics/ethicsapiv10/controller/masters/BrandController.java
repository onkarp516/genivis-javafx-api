package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class BrandController {

    @Autowired
    private BrandService brandService;

    @PostMapping(path = "/create_brand")
    public ResponseEntity<?> createGroup(HttpServletRequest request) {
        return ResponseEntity.ok(brandService.addBrand(request));
    }

    /* Get all Brands of Outlets */
    @GetMapping(path = "/get_outlet_brands")
    public Object getAllBrands(HttpServletRequest request) {
        JsonObject result = brandService.getAllBrands(request);
        return result.toString();
    }

    /* get Brands by Id */
    @PostMapping(path = "/get_brand_by_id")
    public Object getBrand(HttpServletRequest request) {
        JsonObject result = brandService.getBrand(request);
        return result.toString();
    }
    @PostMapping(path = "/update_brand")
    public Object updateBrands(HttpServletRequest request) {
        JsonObject result = brandService.updateBrand(request);
        return result.toString();
    }

    /* @PostMapping(path="/remove_brand")
     public Object removeBrand(HttpServletRequest request)
     {
         JsonObject res=brandService.removeBrand(request);
         return res.toString();
     }*/
    /**** Remove Multiple Brands ****/
    @PostMapping(path = "/remove-multiple-brand")
    public Object removeMultipleBrands(HttpServletRequest request) {
        JsonObject res = brandService.removeMultipleBrands(request);
        return res.toString();
    }

    @PostMapping(path = "/DTBrand")
    public Object DTBrand(@RequestBody Map<String, String> request, HttpServletRequest req) {
        return brandService.DTBrand(request, req);
    }
}
