package in.truethics.ethics.ethicsapiv10.controller.masters;

import in.truethics.ethics.ethicsapiv10.service.master_service.EcommerceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class EcommerceTypeController {
    @Autowired
    private EcommerceTypeService ecommerceTypeService;

    @PostMapping(path = "create_ecom_master")
    public Object createEcommerceType(HttpServletRequest request){
        return ecommerceTypeService.createEcommerceType(request).toString();
    }

    @GetMapping(path = "get_all_ecom_master")
    public Object getAllEcommerceType(HttpServletRequest request){
        return ecommerceTypeService.getAllEcommerceType(request).toString();
    }
}
