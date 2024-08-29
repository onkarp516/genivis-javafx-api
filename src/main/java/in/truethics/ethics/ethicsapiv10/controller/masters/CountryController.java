package in.truethics.ethics.ethicsapiv10.controller.masters;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
public class CountryController {
    @Autowired
    private CountryService service;

    @Autowired
    FranchiseMasterController franchiseMasterController;


    /* get all Country  */
    @PostMapping(path = "/getCountry")
    public Object getCountry(HttpServletRequest request) {
        JsonObject res = service.getCountry(request);
        return res.toString();
    }

    /* get India Country  */
    @GetMapping(path = "/getIndiaCountry")
    public ResponseEntity<?> getIndiaCountry(HttpServletRequest request) {
        return ResponseEntity.ok(service.getIndiaCountry(request));
    }

    @PostMapping(path = "/saveCountry")
    public Object saveCountry(HttpServletRequest request) {
        return service.saveCountry(request).toString();
    }
}
