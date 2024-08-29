package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.LevelBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class LevelBController {
    @Autowired
    private LevelBService service;

    @PostMapping(path = "/create_levelB")
    public ResponseEntity<?> addLevelB(HttpServletRequest request) {
        return ResponseEntity.ok(service.addLevelB(request));
    }

    /* update group by id*/
    @PostMapping(path = "/update_levelB")
    public Object updateLevelB(HttpServletRequest request) {
        JsonObject result = service.updateLevelB(request);
        return result.toString();
    }

    /* Get all groups of Outlets */
    @GetMapping(path = "/get_outlet_levelB")
    public Object getAllOutletLevelB(HttpServletRequest request) {
        JsonObject result = service.getAllOutletLevelB(request);
        return result.toString();
    }

    /* get LevelB by Id */
    @PostMapping(path = "/get_levelB_by_id")
    public Object getLevelBById(HttpServletRequest request) {
        JsonObject result = service.getLevelBById(request);
        return result.toString();
    }


    /***** remove multipl Level B ****/
    @PostMapping(path = "/remove-muiltpe-levelB")
    public Object removeMultipleLevelB(HttpServletRequest request) {
        JsonObject result = service.removeMultipleLevelB(request);
        return result.toString();
    }
}
