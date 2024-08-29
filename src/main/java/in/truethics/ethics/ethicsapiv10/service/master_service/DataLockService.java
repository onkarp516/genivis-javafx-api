package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.DataLockModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class DataLockService {

    public JsonObject removeInstance(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        String key = request.getParameter("key");
        DataLockModel dataLockModel = DataLockModel.getInstance();
        if (dataLockModel != null) {
            dataLockModel.removeObject(key);
            jsonObject.addProperty("message", "Key removed successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }else{
            jsonObject.addProperty("message", "Error in removing lock");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return jsonObject;
    }
}
