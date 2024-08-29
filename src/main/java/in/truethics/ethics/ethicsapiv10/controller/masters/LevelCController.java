package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.LevelCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class LevelCController {
    @Autowired
    private LevelCService service;

    @PostMapping(path = "/create_levelC")
    public ResponseEntity<?> addLevelC(HttpServletRequest request) {
        return ResponseEntity.ok(service.addLevelC(request));
    }

    /* update group by id*/
    @PostMapping(path = "/update_levelC")
    public Object updateLevelC(HttpServletRequest request) {
        JsonObject result = service.updateLevelC(request);
        return result.toString();
    }

    /* Get all groups of Outlets */
    @GetMapping(path = "/get_outlet_levelC")
    public Object getAllOutletLevelC(HttpServletRequest request) {
        JsonObject result = service.getAllOutletLevelC(request);
        return result.toString();
    }

    /* get LevelC by Id */
    @PostMapping(path = "/get_levelC_by_id")
    public Object getLevelCById(HttpServletRequest request) {
        JsonObject result = service.getLevelCById(request);
        return result.toString();
    }

    /***** remove multipl Level B ****/
    @PostMapping(path = "/remove-muiltpe-levelC")
    public Object removeMultipleLevelB(HttpServletRequest request) {
        JsonObject result = service.removeMultipleLevelC(request);
        return result.toString();
    }
}
