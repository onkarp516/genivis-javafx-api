package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@RestController
public class DistrictController {

    @Autowired
    private DistrictService districtService;


    @PostMapping(path = "/create_district")
    public ResponseEntity<?> createDistrict(HttpServletRequest request) {
        return ResponseEntity.ok(districtService.createDistrict(request));
    }

    @GetMapping(path = "/get_all_districts")
    public Object getAllDistricts(HttpServletRequest request) {
        JsonObject result = districtService.getAllDistricts(request);
        return result.toString();
    }

    @PostMapping(path = "/create_district_multipart")
    public ResponseEntity<?> createDistrictHeadMultipart(MultipartHttpServletRequest request) throws ParseException {
        return ResponseEntity.ok(districtService.createDistrictMultipart(request));
    }
}
