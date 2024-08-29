package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.CourierServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CourierServicesController {
    @Autowired
    private CourierServicesService courierServicesService;
    @PostMapping(path = "/create_courier_master")
    public ResponseEntity<?> createCourierMaster(HttpServletRequest request) {
        return ResponseEntity.ok(courierServicesService.createCourierServicesMaster(request));
    }
    @GetMapping(path = "/get_all_courier_master")
    public Object getAllCourierMaster(HttpServletRequest request) {
        JsonObject result = courierServicesService.getAllCourierServicesMaster(request);
        return result.toString();
    }
    @PostMapping(path = "/get_courier_master_by_id")
    public Object getSalesmanMaster(HttpServletRequest request) {
        JsonObject result = courierServicesService.getCourierServicesById(request);
        return result.toString();
    }
    @PostMapping(path = "/update_courier_master")
    public ResponseEntity<?> updateSalesmanMaster(HttpServletRequest request) {
        return ResponseEntity.ok(courierServicesService.updateCourierServicesMaster(request));
    }
}
