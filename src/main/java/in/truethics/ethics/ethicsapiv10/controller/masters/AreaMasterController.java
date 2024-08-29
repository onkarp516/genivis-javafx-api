package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.AreaMasterRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.AreaMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AreaMasterController {
    @Autowired
    private AreaMasterService areaMasterService;
    @Autowired
    private AreaMasterRepository areaMasterRepository;

    @PostMapping(path = "/create_area_master")
    public ResponseEntity<?> createAreaMaster(HttpServletRequest request) {
        return ResponseEntity.ok(areaMasterService.createAreaMaster(request));
    }

    /* Get all Area Master of Outlets */
    @GetMapping(path = "/get_outlet_area_master")
    public Object getAllAreaMaster(HttpServletRequest request) {
        JsonObject result = areaMasterService.getAllAreaMaster(request);
        return result.toString();
    }

    /* get Area Master by Id */
    @PostMapping(path = "/get_area_master_by_id")
    public Object getAreaMaster(HttpServletRequest request) {
        JsonObject result = areaMasterService.getAreaMaster(request);
        return result.toString();
    }
    @PostMapping(path = "/update_area_master")
    public Object updateAreaMaster(HttpServletRequest request) {
        JsonObject result = areaMasterService.updateAreaMaster(request);
        return result.toString();
    }
    @PostMapping(path = "/remove_area_master")
    public Object removeAreaMaster(HttpServletRequest request) {
        JsonObject result = areaMasterService.removeAreaMaster(request);
        return result.toString();
    }
    /***** duplicate Area Master *****/
    @PostMapping(path = "/validate_area_master")
    public Object duplicateAreaMaster(HttpServletRequest request) {
        JsonObject jsonObject = areaMasterService.duplicateAreaMaster(request);
        return jsonObject.toString();
    }
    /***** duplicate Area Master for Update*****/
    @PostMapping(path = "/validate_area_master_update")
    public Object duplicateAreaMasterUpdate(HttpServletRequest request) {
        JsonObject jsonObject = areaMasterService.duplicateAreaMasterUpdate(request);
        return jsonObject.toString();
    }
}
