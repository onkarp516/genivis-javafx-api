package in.truethics.ethics.ethicsapiv10.controller.dispatch_management;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.service.dispatch_management.TransportAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
@RestController
public class TransportAgencyController {
    @Autowired
    private TransportAgencyService transportAgencyService;

    @PostMapping(path = "/create_transport_agency_data")
    public ResponseEntity<?> createTransportAgencyData(HttpServletRequest request) {
        return ResponseEntity.ok(transportAgencyService.createTransportAgencyData(request));
    }

    /* Get all Area Master of Outlets */
    @GetMapping(path = "/get_all_transport_agency_data")
    public Object getAllTransportAgencyData(HttpServletRequest request) {
        JsonObject result = transportAgencyService.getAllTransportAgencyData(request);
        return result.toString();
//        return result;
    }


    /* get Area Master by Id */
    @PostMapping(path = "/get_transport_agency_data_by_id")
    public Object getTransportAgencyDataById(HttpServletRequest request) {
        JsonObject result = transportAgencyService.getTransportAgencyDataById(request);
        return result.toString();
    }

    @PostMapping(path = "/update_transport_agency_data")
    public Object updateTransportAgencyData(HttpServletRequest request) {
        JsonObject result = transportAgencyService.updateTransportAgencyData(request);
        return result.toString();
    }

    @PostMapping(path = "/remove_transport_agency_data")
    public Object removeTransportAgencyData(HttpServletRequest request) {
        JsonObject result = transportAgencyService.removeTransportAgencyData(request);
        return result.toString();
    }

}
