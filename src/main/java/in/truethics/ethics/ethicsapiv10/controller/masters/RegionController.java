package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.RegionRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
@RestController
public class RegionController {
    @Autowired
    private RegionService regionService;
    @Autowired
    private RegionRepository regionRepository;

    @PostMapping(path = "/create_region")
    public ResponseEntity<?> createZone(HttpServletRequest request) {
        return ResponseEntity.ok(regionService.createRegion(request));
    }

    @GetMapping(path = "/get_all_regions")
    public Object getAllZones(HttpServletRequest request) {
        JsonObject result = regionService.getAllRegions(request);
        return result.toString();
    }
}
