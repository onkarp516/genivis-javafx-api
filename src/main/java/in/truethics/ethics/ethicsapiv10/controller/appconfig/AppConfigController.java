package in.truethics.ethics.ethicsapiv10.controller.appconfig;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.appconfig.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AppConfigController {
    @Autowired
    private AppConfigService service;
    @PostMapping(path = "/create_app_config")
    public ResponseEntity<?> addConfig(HttpServletRequest request) {
        return ResponseEntity.ok(service.addConfig(request));
    }
    /* update App Config by id*/
    @PostMapping(path = "/update_app_config")
    public Object updateConfig(HttpServletRequest request) {
        JsonObject result = service.updateConfig(request);
        return result.toString();
    }
    /* Get all App Config of Outlets */
    @GetMapping(path = "/get_outlet_appConfig")
    public Object getAllOutletAppConfig(HttpServletRequest request) {
        JsonObject result = service.getAllOutletAppConfig(request);
        return result.toString();
    }
   /**** get AppConfig by Id */
    @PostMapping(path = "/get_appConfig_by_id")
    public Object getappConfigById(HttpServletRequest request) {
        JsonObject result = service.getappConfigById(request);
        return result.toString();
    }
    /*** Removal of App Config ****/
    @PostMapping(path="/remove_appConfig")
    public Object removeAppConfig(HttpServletRequest request)
    {
        JsonObject result=service.removeAppConfig(request);
        return result.toString();
    }

    /*** Get all Master System Configuration of App Config ****/
    @PostMapping(path="/get_all_master_system_config")
    public Object getMasterSystemAppConfig(HttpServletRequest request)
    {
        JsonObject result=service.getMasterSystemAppConfig(request);
        return result.toString();
    }


}
