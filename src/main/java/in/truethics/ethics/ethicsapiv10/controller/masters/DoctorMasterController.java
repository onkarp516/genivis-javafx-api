package in.truethics.ethics.ethicsapiv10.controller.masters;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.DoctorMasterRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.ContentMasterService;
import in.truethics.ethics.ethicsapiv10.service.master_service.DoctorMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DoctorMasterController {
    @Autowired
    private DoctorMasterRepository doctorMasterRepository;
    @Autowired
    private DoctorMasterService doctorMasterService;

    @PostMapping(path = "/create_doctor_master")
    public ResponseEntity<?> createDoctorMaster(HttpServletRequest request) {
        return ResponseEntity.ok(doctorMasterService.createDoctorMaster(request));
    }

    @GetMapping(path = "/get_all_doctor_master")
    public Object getAllDoctorMaster(HttpServletRequest request) {
        JsonObject result = doctorMasterService.getAllDoctorMaster(request);
        return result.toString();
    }

    @PostMapping(path = "/get_doctor_master_by_id")
    public Object getDoctorMasterById(HttpServletRequest request) {
        JsonObject result = doctorMasterService.getDoctorMasterById(request);
        return result.toString();
    }

    @PostMapping(path = "/update_doctor_master")
    public Object updateDoctorMaster(HttpServletRequest request) {
        JsonObject result = doctorMasterService.updateDoctorMaster(request);
        return result.toString();
    }

    @PostMapping(path = "/remove_doctor_master")
    public Object removeDoctorMaster(HttpServletRequest request) {
        JsonObject result = doctorMasterService.removeDoctorMaster(request);
        return result.toString();
    }

}
