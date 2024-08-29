package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.DataLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DataLockController {

    @Autowired
    private DataLockService dataLockService;
    @PostMapping(path = "/remove_instance")
    public Object removeInstance(HttpServletRequest request) {
        JsonObject result = dataLockService.removeInstance(request);
        return result.toString();
    }
}
