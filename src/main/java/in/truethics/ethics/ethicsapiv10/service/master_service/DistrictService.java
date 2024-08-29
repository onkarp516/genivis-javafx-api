package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DistrictService {

    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    JwtTokenUtil jwtRequestFilter;

    private static final Logger districtLogger = LogManager.getLogger(DistrictService.class);
    @Autowired
    private ZoneRepository zoneRepository;

    public Object createDistrict(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();

        try {
            District district = new District();
            if (paramMap.containsKey("regionCode")) {
                // zone.setStateCode(request.getParameter("stateCode"));
                Region region = regionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("regionCode")),true);
                if (region != null) {
                    district.setRegion(region);
                    district.setState(region.getState());
                    district.setZone(region.getZone());
                }
            } //else district.setStateCode("");
            district.setDistrictName(request.getParameter("districtName").trim());
            district.setStatus(true);
            District mDistrict = districtRepository.save(district);
            responseObject.setMessage("District created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mDistrict.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            districtLogger.error("createDistrict-> failed to create District" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            districtLogger.error("createDistrict-> failed to create District" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllDistricts(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<District> list = new ArrayList<>();
        try {
            list = districtRepository.findByStatus(true);
            if (list.size() > 0) {
                for (District mDistrict : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mDistrict.getId());
                    response.addProperty("districtName", mDistrict.getDistrictName());
                    List<State> state = stateRepository.findByStateCode(String.valueOf(mDistrict.getId()));
                    for (State state1 :state){
                        response.addProperty("stateName",state1.getName());
                    }
                    response.addProperty("stateId", mDistrict.getState() != null ? mDistrict.getState().getId().toString() : "");
//                    Zone zone = zoneRepository.findByIdAndStatus(Long.parseLong(String.valueOf(mDistrict.getId())),true);
//                    response.addProperty("zoneName",zone.getZoneName());
//                    response.addProperty("zoneId", mDistrict.getZone() != null ? mDistrict.getZone().getId().toString() : "");
//                    result.add(response);
//                    Region region = regionRepository.findByIdAndStatus(Long.parseLong(String.valueOf(mDistrict.getId())),true);
//                    response.addProperty("regionName",region.getRegionName());
//                    response.addProperty("regionId", mDistrict.getRegion() != null ? mDistrict.getRegion().getId().toString() : "");
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
            districtLogger.error("Error in getAllDistricts:"+exceptionAsString);
        }
        return res;
    }


    public Object createDistrictMultipart(MultipartHttpServletRequest request) {

        System.out.println("Multipart Started");
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();

        try {
            District district = new District();
            if (paramMap.containsKey("regionCode")) {
                // zone.setStateCode(request.getParameter("stateCode"));
                Region region = regionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("regionCode")),true);
                if (region != null) {
                    district.setRegion(region);
                    district.setState(region.getState());
                    district.setZone(region.getZone());
                }
            } //else district.setStateCode("");
            district.setDistrictName(request.getParameter("districtName").trim());
            district.setStatus(true);
            District mDistrict = districtRepository.save(district);
            responseObject.setMessage("District created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mDistrict.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            districtLogger.error("createDistrict-> failed to create District" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            districtLogger.error("createDistrict-> failed to create District" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;

    }
}
