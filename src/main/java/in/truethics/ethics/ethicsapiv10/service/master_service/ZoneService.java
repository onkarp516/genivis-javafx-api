package in.truethics.ethics.ethicsapiv10.service.master_service;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.State;
import in.truethics.ethics.ethicsapiv10.model.master.Zone;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CountryRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.StateRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ZoneRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ZoneService {
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    JwtTokenUtil jwtRequestFilter;

    private static final Logger ZoneLogger = LogManager.getLogger(ZoneService.class);

    public Object createZone(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();

        try {
            Zone zone = new Zone();
            if (paramMap.containsKey("stateCode")) {
                List<State> state = stateRepository.findByStateId(Long.valueOf(request.getParameter("stateCode")));
                if (state != null) {
                    zone.setState(state.get(0));
                }
            } //else zone.setStateCode("");
            zone.setZoneName(request.getParameter("zoneName").trim());
            zone.setStatus(true);
            Zone mZone = zoneRepository.save(zone);
            responseObject.setMessage("Zone created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mZone.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ZoneLogger.error("createZone-> failed to create Zone" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ZoneLogger.error("createZone-> failed to create Zone" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllZones(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<Zone> list = new ArrayList<>();
        try {
            list = zoneRepository.findByStatus(true);
            if (list.size() > 0) {
                for (Zone mZone : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mZone.getId());
                    response.addProperty("zoneName", mZone.getZoneName());
                    response.addProperty("stateId", mZone.getState().getId());
                    result.add(response);
                }
                res.addProperty("message", "success");
                res.addProperty("responseStatus", HttpStatus.OK.value());
                res.add("responseObject", result);

            } else {
                res.addProperty("message", "empty list");
                res.addProperty("responseStatus", HttpStatus.OK.value());
                res.add("responseObject", result);
            }
        }catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ZoneLogger.error("Error in getAllContentMaster:"+exceptionAsString);
        }
        return res;
    }
}
