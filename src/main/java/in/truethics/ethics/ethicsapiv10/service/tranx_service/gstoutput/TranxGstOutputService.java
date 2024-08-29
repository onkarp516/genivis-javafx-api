package in.truethics.ethics.ethicsapiv10.service.tranx_service.gstoutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class TranxGstOutputService {

    private static final Logger gstOutpuLogger = LogManager.getLogger(TranxGstOutputService.class);

   /* public JsonObject gstOutputLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = gstOutputMasterRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = gstOutputMasterRepository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String csCode = "GSTINPUT" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("gstInputNo", csCode);
        return result;
    }

    public JsonObject createGstOutput(HttpServletRequest request) {
    }

    public JsonObject gstOutputList(HttpServletRequest request) {
    }

    public JsonObject updateGstOutput(HttpServletRequest request) {
    }

    public JsonObject getGstOutputById(HttpServletRequest request) {
    }*/
}
