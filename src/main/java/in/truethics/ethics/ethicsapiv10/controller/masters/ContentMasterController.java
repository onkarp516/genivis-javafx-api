package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ContentMasterRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.ContentMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
@RestController
public class ContentMasterController {

    @Autowired
    private ContentMasterService contentMasterService;
    @Autowired
    private ContentMasterRepository contentMasterRepository;

    @PostMapping(path = "/create_content_master")
    public ResponseEntity<?> createContentMaster(HttpServletRequest request) {
        return ResponseEntity.ok(contentMasterService.createContentMaster(request));
    }

    @PostMapping(path = "/update_content_master")
    public Object updateContentMaster(HttpServletRequest request) {
        JsonObject result = contentMasterService.updateContentMaster(request);
        return result.toString();
    }

    @GetMapping(path = "/get_all_content_master")
    public Object getAllContentMaster(HttpServletRequest request) {
        JsonObject result = contentMasterService.getAllContentMaster(request);
        return result.toString();
    }

    @PostMapping(path = "/get_content_master_by_id")
    public Object getContentMaster(HttpServletRequest request) {
        JsonObject result = contentMasterService.getContentMaster(request);
        return result.toString();
    }

    @PostMapping(path = "/remove_content_master")
    public Object removeContentMaster(HttpServletRequest request) {
        JsonObject result = contentMasterService.removeContentMaster(request);
        return result.toString();
    }

    @GetMapping(path = "/get_all_content_package_master")
    public Object getAllContentPackageMaster(HttpServletRequest request) {
        JsonObject result = contentMasterService.getAllContentPackageMaster(request);
        return result.toString();
    }

    @PostMapping(path = "/create_content_master_dose")
    public ResponseEntity<?> createContentMasterDose(HttpServletRequest request) {
        return ResponseEntity.ok(contentMasterService.createContentMasterDose(request));
    }

    @GetMapping(path = "/get_all_content_master_dose")
    public Object getAllContentMasterDose(HttpServletRequest request) {
        JsonObject result = contentMasterService.getAllContentMasterDose(request);
        return result.toString();
    }

    @PostMapping(path = "/validate_content_name")
    public Object validateContentName(HttpServletRequest request) {
        JsonObject object = contentMasterService.validateContentName(request);
        return object.toString();
    }

}
