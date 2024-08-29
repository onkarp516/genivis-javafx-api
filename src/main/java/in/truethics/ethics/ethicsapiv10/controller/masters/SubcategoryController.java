package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.SubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SubcategoryController {
    @Autowired
    private SubcategoryService subcategoryService;

    @PostMapping(path = "/create_sub_category")
    public ResponseEntity<?> createSubcategory(HttpServletRequest request) {
        return ResponseEntity.ok(subcategoryService.createSubcategory(request));
    }

    @PostMapping(path = "/update_sub_category")
    public Object updateSubcategory(HttpServletRequest request) {
        JsonObject jsonObject = subcategoryService.updateSubcategory(request);
        return jsonObject.toString();
    }

    /* Get  all Categories of Outlets */
    @GetMapping(path = "/get_outlet_subcategories")
    public Object getAllOutletSubCategories(HttpServletRequest request) {
        JsonObject result = subcategoryService.getAllOutletSubCategories(request);
        return result.toString();
    }

    @PostMapping(path = "/get_subcategory")
    public Object getSubCategory(HttpServletRequest request) {
        JsonObject result = subcategoryService.getSubCategory(request);
        return result.toString();
    }

    /**** Remove Multiple Sub Category ****/
    @PostMapping(path = "/remove-multiple-subcategories")
    public Object removeMultipleSubcategories(HttpServletRequest request) {
        JsonObject result = subcategoryService.removeMultipleSubcategories(request);
        return result.toString();
    }

}
