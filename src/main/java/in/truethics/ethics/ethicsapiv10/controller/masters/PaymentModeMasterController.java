package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.PaymentModeMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PaymentModeMasterController {
    @Autowired
    private PaymentModeMasterService service;

    /* Get all groups of Outlets */
    @GetMapping(path = "/get_payment_mode")
    public Object getAllPaymentModes(HttpServletRequest request) {
        JsonObject result = service.getAllPaymentModes(request);
        return result.toString();
    }

    @PostMapping(path = "/create_payment_mode")
    public Object createPaymentMode(HttpServletRequest request) {
        JsonObject object = service.createPaymentMode(request);
        return object.toString();
    }

    @PostMapping(path = "/get_paymentmode_by_id")
    public Object getPaymentModeById(HttpServletRequest request) {
        JsonObject object = service.getPaymentModeById(request);
        return object.toString();
    }

    @PostMapping(path = "/update_payment_mode")
    public Object editPaymentMode(HttpServletRequest request) {
        JsonObject object = service.editPaymentMode(request);
        return object.toString();
    }
    @PostMapping(path = "/remove_payment_master")
    public Object removePaymentMaster(HttpServletRequest request) {
        JsonObject result = service.removePaymentMaster(request);
        return result.toString();
    }

}
