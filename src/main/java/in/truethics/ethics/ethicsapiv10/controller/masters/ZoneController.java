package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ZoneRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
@RestController
public class ZoneController {
    @Autowired
    private ZoneService zoneService;
     @Autowired
     private ZoneRepository zoneRepository;

    @PostMapping(path = "/create_zone")
    public ResponseEntity<?> createZone(HttpServletRequest request) {
        return ResponseEntity.ok(zoneService.createZone(request));
    }

    @GetMapping(path = "/get_all_zones")
    public Object getAllZones(HttpServletRequest request) {
        JsonObject result = zoneService.getAllZones(request);
        return result.toString();
    }
}
