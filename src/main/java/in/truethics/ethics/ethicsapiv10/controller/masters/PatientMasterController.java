package in.truethics.ethics.ethicsapiv10.controller.masters;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.DoctorMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.PackingMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.PatientMasterRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.DoctorMasterService;
import in.truethics.ethics.ethicsapiv10.service.master_service.PatientMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PatientMasterController {
    @Autowired
    private PatientMasterRepository patientMasterRepository;
    @Autowired
    private PatientMasterService patientMasterService;

    @PostMapping(path = "/create_patient_master")
    public ResponseEntity<?> createPatientMaster(HttpServletRequest request) {
        return ResponseEntity.ok(patientMasterService.createPatientMaster(request));
    }

    @GetMapping(path = "/get_all_patient_master")
    public Object getAllPatientMaster(HttpServletRequest request) {
        JsonObject result = patientMasterService.getAllPatientMaster(request);
        return result.toString();
    }
//
    @PostMapping(path = "/get_patient_master_by_id")
    public Object getPatientMasterById(HttpServletRequest request) {
        JsonObject result = patientMasterService.getPatientMasterById(request);
        return result.toString();
    }

    @PostMapping(path = "/update_patient_master")
    public Object updatePatientMaster(HttpServletRequest request) {
        JsonObject result = patientMasterService.updatePatientMaster(request);
        return result.toString();
    }
//
    @PostMapping(path = "/remove_patient_master")
    public Object removePatientMaster(HttpServletRequest request) {
        JsonObject result = patientMasterService.removePatientMaster(request);
        return result.toString();
    }

}
