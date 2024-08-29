package in.truethics.ethics.ethicsapiv10.controller.barcode;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.Barcode_service.BarcodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpResponse;

@RestController
public class BarcodeController {

    @Autowired
    private BarcodeService barcodeService;

    /* Creation of Barcode */
    @PostMapping(path = "/create_barcode")
    public Object createBarcode(HttpServletRequest request) {
        JsonObject jsonObject = barcodeService.createBarcode(request);
        return jsonObject.toString();
    }

    /* get of Barcode of Product */
    @PostMapping(path = "/get_barcode")
    public Object getBarcode(HttpServletRequest request) {
        JsonObject jsonObject = barcodeService.getBarcode(request);
        return jsonObject.toString();
    }




    @PostMapping(path = "/get_barcode_home_path")
    public Object getBarcodeHomePath(HttpServletRequest request) {
        JsonObject jsonObject=new JsonObject();
        jsonObject = barcodeService.getBarcodeHomePath(request);
        return jsonObject.toString();
    }

    @PostMapping(path = "/create_barcode_home_path")
    public Object createBarcodeHomePath(HttpServletRequest request) {
        JsonObject jsonObject=new JsonObject();
        jsonObject = barcodeService.createBarcodeHomePath(request);
        return jsonObject.toString();
    }

    @PostMapping(path = "/update_barcode_home_path")
    public Object updateBarcodeHomePath(HttpServletRequest request) {
        JsonObject jsonObject = barcodeService.updateBarcodeHomePath(request);
        return jsonObject.toString();
    }


}
