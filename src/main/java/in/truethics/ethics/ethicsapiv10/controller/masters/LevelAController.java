package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.LevelAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class LevelAController {
    @Autowired
    private LevelAService service;

    @PostMapping(path = "/create_levelA")
    public ResponseEntity<?> addLevelA(HttpServletRequest request) {
        return ResponseEntity.ok(service.addLevelA(request));
    }

    /* update group by id*/
    @PostMapping(path = "/update_levelA")
    public Object updateGroup(HttpServletRequest request) {
        JsonObject result = service.updateLevelA(request);
        return result.toString();
    }

    /* Get all groups of Outlets */
    @GetMapping(path = "/get_outlet_levelA")
    public Object getAllOutletLevelA(HttpServletRequest request) {
        JsonObject result = service.getAllOutletLevelA(request);
        return result.toString();
    }

    /* get LevelA by Id */
    @PostMapping(path = "/get_levelA_by_id")
    public Object getLevelAById(HttpServletRequest request) {
        JsonObject result = service.getLevelAById(request);
        return result.toString();
    }

    /***** remove multipl Level A ****/
    @PostMapping(path = "/remove-muiltpe-levelA")
    public Object removeMultipleLevelA(HttpServletRequest request) {
        JsonObject result = service.removeMultipleLevelA(request);
        return result.toString();
    }

}
