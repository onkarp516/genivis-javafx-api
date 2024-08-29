package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.CourierServices;
import in.truethics.ethics.ethicsapiv10.model.master.SalesManMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CourierServicesRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CourierServicesService {
    @Autowired
    JwtTokenUtil jwtRequestFilter;

    @Autowired
    CourierServicesRepository courierServicesRepository;
    private static final Logger courierLogger = LogManager.getLogger(CourierServicesService.class);

    public Object createCourierServicesMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            CourierServices courierServices = new CourierServices();
            courierServices.setService_name(request.getParameter("service_name").trim());
            courierServices.setContact_person(request.getParameter("contact_person").trim());

            courierServices.setBranch(branch);
            courierServices.setOutlet(users.getOutlet());
            courierServices.setCreatedBy(users.getId());
            courierServices.setStatus(true);
            if (paramMap.containsKey("contact_No")) courierServices.setMobileNumber(request.getParameter("contact_No"));

            if (paramMap.containsKey("service_Add")) courierServices.setAddress(request.getParameter("service_Add"));
            CourierServices mArea = courierServicesRepository.save(courierServices);
            responseObject.setMessage("Courier master created succussfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mArea.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            courierLogger.error("create Courier Master-> failed to create Salesman Master" + e);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            courierLogger.error("Create Courier Master-> failed to create Salesman Master" + e1);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllCourierServicesMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<CourierServices> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = courierServicesRepository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());
        } else {
            list = courierServicesRepository.findByOutletIdAndStatusAndBranchIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (CourierServices mSalesman : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mSalesman.getId());
                response.addProperty("service_name", mSalesman.getService_name());
                response.addProperty("contact_person", mSalesman.getContact_person() != null ? mSalesman.getContact_person() : "");
                response.addProperty("contact_No", mSalesman.getMobileNumber() != null ? mSalesman.getMobileNumber() : "");
                response.addProperty("service_Add", mSalesman.getAddress() != null ? mSalesman.getAddress() : "");

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

    public JsonObject getCourierServicesById(HttpServletRequest request) {
        CourierServices mSalesman = courierServicesRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (mSalesman != null) {
            response.addProperty("id", mSalesman.getId());
            response.addProperty("service_name", mSalesman.getService_name());
            response.addProperty("contact_person", mSalesman.getContact_person() != null ? mSalesman.getContact_person() : "");
            response.addProperty("contact_No", mSalesman.getMobileNumber() != null ? mSalesman.getMobileNumber() : "");
            response.addProperty("service_Add", mSalesman.getAddress() != null ? mSalesman.getAddress() : "");
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public Object updateCourierServicesMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            CourierServices courierServices = courierServicesRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            courierServices.setService_name(request.getParameter("service_name").trim());
            if (paramMap.containsKey("contact_person")) courierServices.setContact_person(request.getParameter("contact_person").trim());
            courierServices.setBranch(branch);
            courierServices.setOutlet(users.getOutlet());
            courierServices.setCreatedBy(users.getId());
            courierServices.setUpdatedBy(users.getId());
            courierServices.setStatus(true);
            if (paramMap.containsKey("contact_No")) courierServices.setMobileNumber(request.getParameter("contact_No"));

            if (paramMap.containsKey("service_Add")) courierServices.setAddress(request.getParameter("service_Add"));
            CourierServices mArea = courierServicesRepository.save(courierServices);
            responseObject.setMessage("Courier master updated succussfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mArea.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            courierLogger.error("udpate Courier Master-> failed to udpate Courier Master" + e);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            courierLogger.error("Update Courier Master-> failed to update Courier Master" + e1);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }
}
