package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.ContentMaster;
import in.truethics.ethics.ethicsapiv10.model.master.State;
import in.truethics.ethics.ethicsapiv10.model.master.Region;
import in.truethics.ethics.ethicsapiv10.model.master.Zone;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CountryRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.RegionRepository;
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
public class RegionService {
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    JwtTokenUtil jwtRequestFilter;

    private static final Logger regionLogger = LogManager.getLogger(RegionService.class);
    @Autowired
    private ZoneRepository zoneRepository;

    public Object createRegion(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();

        try {
            Region region = new Region();
            if (paramMap.containsKey("zoneCode")) {
                // zone.setStateCode(request.getParameter("stateCode"));
                Zone zone = zoneRepository.findByIdAndStatus(Long.parseLong(request.getParameter("zoneCode")),true);
                if (zone != null) {
                    region.setZone(zone);
                    region.setState(zone.getState());
                }
            } //else region.setStateCode("");
            region.setRegionName(request.getParameter("regionName").trim());
            region.setStatus(true);
            Region mRegion = regionRepository.save(region);
            responseObject.setMessage("Region created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mRegion.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            regionLogger.error("createRegion-> failed to create Region" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            regionLogger.error("createRegion-> failed to create Region" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllRegions(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<Region> list = new ArrayList<>();
        try {
            list = regionRepository.findByStatus(true);
            if (list.size() > 0) {
                for (Region mRegion : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mRegion.getId());
                    response.addProperty("regionName", mRegion.getRegionName());
                    List<State> state = stateRepository.findByStateCode(String.valueOf(mRegion.getId()));
                    for (State state1 :state){
                        response.addProperty("stateName",state1.getName());
                    }
                    response.addProperty("stateId", mRegion.getState() != null ? mRegion.getState().getId().toString() : "");
//                    Zone zone = zoneRepository.findByIdAndStatus(Long.parseLong(String.valueOf(mRegion.getId())),true);
//                        response.addProperty("zoneName",zone.getZoneName());
//                    response.addProperty("zoneId", mRegion.getZone() != null ? mRegion.getZone().getId().toString() : "");
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
            regionLogger.error("Error in getAllRegions:"+exceptionAsString);
        }
        return res;
    }
}
