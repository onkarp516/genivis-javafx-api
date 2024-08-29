package in.truethics.ethics.ethicsapiv10.controller.masters;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.UnitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UnitController {
    @Autowired
    UnitsService service;

    @PostMapping(path = "/create_unit")
    public ResponseEntity<?> createUnit(HttpServletRequest request) {
        return ResponseEntity.ok(service.createUnit(request));
    }

    /* update unit by id */
    @PostMapping(path = "/update_unit")
    public Object updateUnit(HttpServletRequest request) {
        JsonObject result = service.updateUnit(request);
        return result.toString();
    }

    /* Get All units of outlet */
    @GetMapping(path = "/get_units_by_outlet")
    public Object getUnits(HttpServletRequest request) {
        JsonObject result = service.getUnits(request);
        return result.toString();
    }

    /* Get units by id */
    @PostMapping(path = "/get_units_by_id")
    public Object getUnitsById(HttpServletRequest request) {
        JsonObject result = service.getUnitsById(request);
        return result.toString();
    }

    @PostMapping(path = "/removeunits")
    public Object removeUnits(HttpServletRequest request) {
        JsonObject result = service.removeUnit(request);
        return result.toString();
    }
    /**** remove Multiple Units *****/
    @PostMapping(path = "/remove-multiple-units")
    public Object removeMultipleUnits(HttpServletRequest request) {
        JsonObject result = service.removeMultipleUnits(request);
        return result.toString();
    }
}
