package in.truethics.ethics.ethicsapiv10.controller.gstr;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.Gstr_Service.GSTR2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class GSTR2Controller {

    @Autowired
    private GSTR2Service service;
    @PostMapping(path = "/get_GSTR2_B2B_data")
    public Object getGSTR2Data( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR2Data(request);
        return  mObject.toString();
    }

    @PostMapping(path = "/get_GSTR2_DebNoteReg_data")
    public Object getGSTR2DRNOTEReg( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR2DRNOTEReg(request);
        return  mObject.toString();
    }

    @PostMapping(path = "/get_GSTR2_DebNoteUnreg_data")
    public Object getGSTR2DRNOTEUnreg( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR2DRNOTEUnreg(request);
        return  mObject.toString();
    }

    //    GSTR2 Nil-rated Api
    @PostMapping(path = "/get_GSTR2_NilRate_data")
    public Object getGSTR2NIlRATEReg( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR2NIlRATEReg(request);
        return  mObject.toString();
    }
}
