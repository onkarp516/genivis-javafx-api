package in.truethics.ethics.ethicsapiv10.controller.dispatch_management;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.dispatch_management.DeliveryBoyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DeliveryBoyController {

    @Autowired
    private DeliveryBoyService deliveryBoyService;

    @PostMapping(path = "/create_delivery_boy_data")
    public ResponseEntity<?> createDeliveryBoyData(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(deliveryBoyService.createDeliveryBoyData(request));
    }

    /* Get all Delivery boys details */
    @GetMapping(path = "/get_delivery_boy_data")
    public Object getAllDeliveryBoyData(HttpServletRequest request) {
        JsonObject result = deliveryBoyService.getAllDeliveryBoyData(request);
        return result.toString();
    }

    /* Get Delivery boys details by Id  */
    @PostMapping(path = "/get_delivery_boy_data_by_id")
    public Object getDeliveryBoyDataById(MultipartHttpServletRequest request) {
        JsonObject result = deliveryBoyService.getDeliveryBoyDataById(request);
        return result.toString();
    }

    /* Update Delivery boys details  */

    @PostMapping(path = "/update_delivery_boy_data")
    public ResponseEntity<?> updateDeliveryBoyData(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(deliveryBoyService.updateDeliveryBoyData(request));
    }

    /* Delete Delivery boys details  */
    @PostMapping(path = "/remove_delivery_boy_data")
    public Object removeDeliveryBoyData(MultipartHttpServletRequest request) {
        JsonObject result = deliveryBoyService.removeDeliveryBoyData(request);
        return result.toString();
    }

}
