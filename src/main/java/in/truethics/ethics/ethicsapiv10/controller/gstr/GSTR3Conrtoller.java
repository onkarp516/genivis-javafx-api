package in.truethics.ethics.ethicsapiv10.controller.gstr;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.Gstr_Service.GSTR3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class GSTR3Conrtoller {
    @Autowired
    private GSTR3Service gstr3Service;

    // GSTR3B - Outward Taxable Supplies
    @PostMapping(path = "/get_GSTR3B_outward_tax_suplier_data")
    public Object getGSTR1DataScreen1( HttpServletRequest request) {
        JsonObject mObject =gstr3Service.getGSTR3BOutwardTaxSuplierData(request);
        return  mObject.toString();
    }

    // GSTR3B - All Other ITC
    @PostMapping(path = "/get_GSTR3B_all_other_itc_data")
    public Object getGSTR3BAllOtherITCData( HttpServletRequest request) {
        JsonObject mObject =gstr3Service.getGSTR3BAllOtherITCData(request);
        return  mObject.toString();
    }
}
