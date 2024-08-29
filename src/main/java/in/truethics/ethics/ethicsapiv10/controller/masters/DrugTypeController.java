package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.DrugTypeRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.DrugTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
@RestController
public class DrugTypeController {

    @Autowired
    private DrugTypeService drugTypeService;
    @Autowired
    private DrugTypeRepository drugTypeRepository;

    @PostMapping(path = "/create_drug_Type")
    public ResponseEntity<?> createDrugType(HttpServletRequest request) {
        return ResponseEntity.ok(drugTypeService.createDrugType(request));
    }

    @GetMapping(path = "/get_all_drug_types")
    public Object getAllDrugTypes(HttpServletRequest request) {
        JsonObject result = drugTypeService.getAllDrugTypes(request);
        return result.toString();
    }

}
