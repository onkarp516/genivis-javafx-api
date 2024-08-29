package in.truethics.ethics.ethicsapiv10.service.dispatch_management;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.dispatch_management.DeliveryBoy;
import in.truethics.ethics.ethicsapiv10.model.dispatch_management.TransportAgency;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CityRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.StateRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CountryRepository;
import in.truethics.ethics.ethicsapiv10.repository.dispatch_management_repository.TransportAgencyRepository;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//import static in.truethics.ethics.ethicsapiv10.service.dispatch_management.TransportAgencyService.TransportLogger;
@Service
public class TransportAgencyService {

    @Autowired
    private TransportAgencyRepository transportAgencyRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CityRepository cityRepository;
    //    @Autowired
//     JwtTokenUtil jwtRequestFilter;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    private static final Logger transportLogger = LogManager.getLogger(TransportAgencyService.class);

    public Object createTransportAgencyData(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        Long branchId = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            branchId = branch.getId();
        }
        try {
            TransportAgency transportAgency = new TransportAgency();
            transportAgency.setTransportAgencyName(request.getParameter("transportAgencyName").trim());
            transportAgency.setAddress(request.getParameter("address").trim());
            transportAgency.setContactPerson(request.getParameter("contactPerson").trim());
            transportAgency.setContactNo(Long.valueOf(request.getParameter("contactNo")));
            if (paramMap.containsKey("pincode"))
                transportAgency.setPincode(request.getParameter("pincode"));

            Long countryId = Long.parseLong(request.getParameter(
                    "countryId"));
            Optional<Country> countryOptional = countryRepository.findById(countryId);
            transportAgency.setCountryId(countryOptional.get().getId());
            Long stateId = Long.parseLong(request.getParameter("stateId"));
            Optional<State> stateOptional = stateRepository.findById(stateId);
            transportAgency.setStateId(stateOptional.get().getId());
//            if (paramMap.containsKey("cityId")) {
            Long cityId = Long.parseLong(request.getParameter("cityId"));
            Optional<City> cityOptional = cityRepository.findById(cityId);
            transportAgency.setCityId(cityOptional.get().getId());
//            }

            transportAgency.setBranchId(branchId);
            transportAgency.setOutletId(users.getOutlet().getId());
            transportAgency.setCreatedBy(users.getId());
            transportAgency.setUpdatedBy(users.getId());
            transportAgency.setStatus(true);


            TransportAgency transportAgency1 = transportAgencyRepository.save(transportAgency);
            responseObject.setMessage("Transport Agency data created succussfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(transportAgency1.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            transportLogger.error("TransportAgencyData-> failed to create Transport Agency data" + e);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            transportLogger.error("TransportAgencyData-> failed to create Transport Agency data" + e1);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }


    public JsonObject getAllTransportAgencyData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<TransportAgency> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = transportAgencyRepository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());
        } else {
            list = transportAgencyRepository.findByOutletIdAndStatusAndBranchIdIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (TransportAgency transportAgency : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", transportAgency.getId());
                response.addProperty("transportAgencyName", transportAgency.getTransportAgencyName());
                response.addProperty("contactPerson", transportAgency.getContactPerson());
                response.addProperty("address", transportAgency.getAddress());
                response.addProperty("contactNo", transportAgency.getContactNo());
                response.addProperty("pincode", transportAgency.getPincode());

//                response.addProperty("city", transportAgency.getCityId());
//                response.addProperty("state", transportAgency.getStateId());
//                response.addProperty("country", transportAgency.getCountryId());

                City city = cityRepository.findById(transportAgency.getCityId()).get();
                response.addProperty("city", city != null ? city.getName() : "");

                State state = stateRepository.findById(transportAgency.getStateId()).get();
                response.addProperty("state", state != null ? state.getName() : "");

                Country country = countryRepository.findById(transportAgency.getCountryId()).get();
                response.addProperty("country", country != null ? country.getName() : "");

                  response.addProperty("cityId", transportAgency.getCityId());
                response.addProperty("stateId", transportAgency.getStateId());
                response.addProperty("countryId", transportAgency.getCountryId());

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
        return res;
    }


    public JsonObject getTransportAgencyDataById(HttpServletRequest request) {

        TransportAgency transportAgency = transportAgencyRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (transportAgency != null) {
            response.addProperty("id", transportAgency.getId());
            response.addProperty("transportAgencyName", transportAgency.getTransportAgencyName());
            response.addProperty("contactPerson", transportAgency.getContactPerson());
            response.addProperty("contactNo", transportAgency.getContactNo());
            response.addProperty("address",transportAgency.getAddress());
            response.addProperty("pincode",transportAgency.getPincode());

            City city = cityRepository.findById(transportAgency.getCityId()).get();
            response.addProperty("city", city != null ? city.getName() : "");

            State state = stateRepository.findById(transportAgency.getStateId()).get();
            response.addProperty("state", state != null ? state.getName() : "");

            Country country = countryRepository.findById(transportAgency.getCountryId()).get();
            response.addProperty("country", country != null ? country.getName() : "");

            response.addProperty("cityId", transportAgency.getCityId());
            response.addProperty("stateId", transportAgency.getStateId());
            response.addProperty("countryId", transportAgency.getCountryId());

            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", " Data not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }


    public JsonObject updateTransportAgencyData(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
         Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        Long branchId =null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            branchId=branch.getId();
        }
        try {
            TransportAgency transportAgency = transportAgencyRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            transportAgency.setTransportAgencyName(request.getParameter("transportAgencyName").trim());
            transportAgency.setAddress(request.getParameter("address").trim());
            transportAgency.setContactPerson(request.getParameter("contactPerson").trim());
            transportAgency.setContactNo(Long.valueOf(request.getParameter("contactNo")));
            Long countryId = Long.parseLong(request.getParameter(
                    "countryId"));
            Optional<Country> countryOptional = countryRepository.findById(countryId);
            transportAgency.setCountryId(countryOptional.get().getId());
            Long stateId = Long.parseLong(request.getParameter("stateId"));
            Optional<State> stateOptional = stateRepository.findById(stateId);
            transportAgency.setStateId(stateOptional.get().getId());
//            if (paramMap.containsKey("cityId")) {
            Long cityId = Long.parseLong(request.getParameter("cityId"));
            Optional<City> cityOptional = cityRepository.findById(cityId);
            transportAgency.setCityId(cityOptional.get().getId());

            transportAgency.setBranchId(branchId);
            transportAgency.setOutletId(users.getOutlet().getId());
            transportAgency.setCreatedBy(users.getId());
            transportAgency.setUpdatedBy(users.getId());

            if (paramMap.containsKey("pincode"))
                transportAgency.setPincode(request.getParameter("pincode"));
            TransportAgency transportAgency1 = transportAgencyRepository.save(transportAgency);
            responseObject.addProperty("message", "Transport Agency updated succussfully");
            responseObject.addProperty("responseStatus", HttpStatus.OK.value());
            responseObject.addProperty("responseObject", transportAgency1.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            transportLogger.error("updateTransportAgency-> failed to update TransportAgency" + e);
            responseObject.addProperty("message", "Internal Server Error");
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            transportLogger.error("updateTransportAgency-> failed to update TransportAgency" + e1);
            responseObject.addProperty("message", "Error");
        }
        return responseObject;
    }

    public JsonObject removeTransportAgencyData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        TransportAgency transportAgency = transportAgencyRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (transportAgency != null) {
            transportAgency.setStatus(false);
            transportAgencyRepository.save(transportAgency);
            jsonObject.addProperty("message", "Transport Agency data deleted successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        } else {
            jsonObject.addProperty("message", "Error in Transport Agency data deletion");
            jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return jsonObject;
    }
}
