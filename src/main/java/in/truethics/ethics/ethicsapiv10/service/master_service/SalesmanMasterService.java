package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.SalesManMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.SalesmanMasterRepository;
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
public class SalesmanMasterService {
    @Autowired
    private SalesmanMasterRepository repository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    private static final Logger salesmanLogger = LogManager.getLogger(SalesmanMasterService.class);

    public Object createSalesmanMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            SalesManMaster salesman = new SalesManMaster();
            salesman.setFirstName(request.getParameter("firstName").trim());
            if (paramMap.containsKey("lastName")) salesman.setLastName(request.getParameter("lastName").trim());
            if (paramMap.containsKey("middleName")) salesman.setMiddleName(request.getParameter("middleName").trim());
            salesman.setBranch(branch);
            salesman.setOutlet(users.getOutlet());
            salesman.setCreatedBy(users.getId());
            salesman.setStatus(true);
            if (paramMap.containsKey("mobileNumber")) salesman.setMobileNumber(request.getParameter("mobileNumber"));
            if (paramMap.containsKey("pincode")) salesman.setPincode(request.getParameter("pincode"));
            if (paramMap.containsKey("dob") && !request.getParameter("dob").equalsIgnoreCase("")) {
                LocalDate dob = LocalDate.parse(request.getParameter("dob"));
                salesman.setDob(dob);
            }
            if (paramMap.containsKey("address")) salesman.setAddress(request.getParameter("address"));
            SalesManMaster mArea = repository.save(salesman);
            responseObject.setMessage("Salesman master created succussfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mArea.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesmanLogger.error("create SalesMan Master-> failed to create Salesman Master" + e);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesmanLogger.error("Create SalesMan Master-> failed to create Salesman Master" + e1);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllSalesmanMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<SalesManMaster> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = repository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());
        } else {
            list = repository.findByOutletIdAndStatusAndBranchIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (SalesManMaster mSalesman : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mSalesman.getId());
                response.addProperty("firstName", mSalesman.getFirstName());
                response.addProperty("lastName", mSalesman.getLastName() != null ? mSalesman.getLastName() : "");
                response.addProperty("middleName", mSalesman.getMiddleName() != null ? mSalesman.getMiddleName() : "");
                response.addProperty("mobile", mSalesman.getMobileNumber() != null ? mSalesman.getMobileNumber() : "");
                response.addProperty("pincode", mSalesman.getPincode() != null ? mSalesman.getPincode() : "");
                response.addProperty("address", mSalesman.getAddress() != null ? mSalesman.getAddress() : "");
                response.addProperty("dob", mSalesman.getDob() != null ? mSalesman.getDob().toString() : "");
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

    public JsonObject getSalesmanMaster(HttpServletRequest request) {
        SalesManMaster mSalesman = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (mSalesman != null) {
            response.addProperty("id", mSalesman.getId());
            response.addProperty("firstName", mSalesman.getFirstName());
            response.addProperty("lastName", mSalesman.getLastName() != null ? mSalesman.getLastName() : "");
            response.addProperty("middleName", mSalesman.getMiddleName() != null ? mSalesman.getMiddleName() : "");
            response.addProperty("mobile", mSalesman.getMobileNumber() != null ? mSalesman.getMobileNumber() : "");
            response.addProperty("pincode", mSalesman.getPincode() != null ? mSalesman.getPincode() : "");
            response.addProperty("address", mSalesman.getAddress() != null ? mSalesman.getAddress() : "");
            response.addProperty("dob", mSalesman.getDob() != null ? mSalesman.getDob().toString() : "");
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public ResponseMessage updateSalesmanMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            SalesManMaster salesman = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            salesman.setFirstName(request.getParameter("firstName").trim());
            if (paramMap.containsKey("lastName")) salesman.setLastName(request.getParameter("lastName").trim());
            if (paramMap.containsKey("middleName")) salesman.setMiddleName(request.getParameter("middleName").trim());
            salesman.setBranch(branch);
            salesman.setOutlet(users.getOutlet());
            salesman.setCreatedBy(users.getId());
            salesman.setUpdatedBy(users.getId());
            salesman.setStatus(true);
            if (paramMap.containsKey("mobileNumber")) salesman.setMobileNumber(request.getParameter("mobileNumber"));
            if (paramMap.containsKey("pincode")) salesman.setPincode(request.getParameter("pincode"));
            if (paramMap.containsKey("dob") && !request.getParameter("dob").equalsIgnoreCase("")) {
                LocalDate dob = LocalDate.parse(request.getParameter("dob"));
                salesman.setDob(dob);
            }
            if (paramMap.containsKey("address")) salesman.setAddress(request.getParameter("address"));
            SalesManMaster mArea = repository.save(salesman);
            responseObject.setMessage("Salesman master updated succussfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mArea.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesmanLogger.error("udpate SalesMan Master-> failed to udpate Salesman Master" + e);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesmanLogger.error("Update SalesMan Master-> failed to update Salesman Master" + e1);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject duplicateSalesmanMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        String firstName = request.getParameter("firstName").trim();
        String middleName = request.getParameter("middleName").trim();
        String lastName = request.getParameter("lastName").trim();
        Long salesManId = Long.valueOf(request.getParameter("id").trim());

        Long outletId = users.getOutlet().getId();
        SalesManMaster saleman = null;
        if (users.getBranch() != null) {
            saleman  = repository.findDuplicateWithBranch(outletId, users.getBranch().getId(), firstName, middleName,
                    lastName, true,salesManId);
        } else {
            saleman  = repository.findDuplicate(outletId, firstName, middleName, lastName,true,salesManId);
        }
        if (saleman  != null) {
            jsonObject.addProperty("message", "Duplicate Salesman Master");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            jsonObject.addProperty("message", "New Salesman Master");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return jsonObject;
    }

    public JsonObject duplicateSalesmanMasterUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
       SalesManMaster salemanMaster = null;
        String firstName = request.getParameter("firstName").trim();
        String middleName = request.getParameter("middleName").trim();
        String lastName = request.getParameter("lastName").trim();
        Long salesmanId = Long.parseLong(request.getParameter("id"));
        if (users.getBranch() != null) {
            salemanMaster  = repository.findDuplicateWithBranch(users.getOutlet().getId(), users.getBranch().getId(),
                    firstName, middleName, lastName, true, salesmanId);
        } else {
            salemanMaster  = repository.findDuplicate(users.getOutlet().getId(),firstName,middleName,lastName,true, salesmanId);
        }
        if (salemanMaster != null && salesmanId != salemanMaster.getId()) {
            jsonObject.addProperty("message", "Duplicate Salesman Master");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            jsonObject.addProperty("message", "New salesman Master");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return jsonObject;
    }

    public JsonObject removeSalesmanMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        SalesManMaster salesmanMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (salesmanMaster != null) {
            salesmanMaster.setStatus(false);
            repository.save(salesmanMaster);
            jsonObject.addProperty("message", "Salesman Master deleted successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        } else {
            jsonObject.addProperty("message", "Error in Salesman Master deletion");
            jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return jsonObject;
    }


}