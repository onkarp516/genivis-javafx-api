package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.AreaMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Brand;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.AreaMasterRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.awt.geom.Area;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AreaMasterService {
    @Autowired
    private AreaMasterRepository areaMasterRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    private static final Logger areaLogger = LogManager.getLogger(AreaMasterService.class);

    public Object createAreaMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            AreaMaster areaMaster = new AreaMaster();
            areaMaster.setAreaName(request.getParameter("areaName").trim());
            areaMaster.setBranch(branch);
            areaMaster.setOutlet(users.getOutlet());
            areaMaster.setCreatedBy(users.getId());
            areaMaster.setUpdatedBy(users.getId());
            areaMaster.setStatus(true);
            if (paramMap.containsKey("areaCode"))
                areaMaster.setAreaCode(request.getParameter("areaCode"));
            if (paramMap.containsKey("pincode"))
                areaMaster.setPincode(request.getParameter("pincode"));
            AreaMaster mArea = areaMasterRepository.save(areaMaster);
            responseObject.setMessage("Area Master created succussfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mArea.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaLogger.error("createAreaMaster-> failed to create AreaMaster" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaLogger.error("createAreaMaster-> failed to create AreaMaster" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllAreaMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<AreaMaster> list = new ArrayList<>();
        try {
            if (users.getBranch() != null) {
                list = areaMasterRepository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());
            } else {
                list = areaMasterRepository.findByOutletIdAndStatusAndBranchIsNull(outletId, true);
            }
            if (list.size() > 0) {
                for (AreaMaster mArea : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mArea.getId());
                    response.addProperty("areaName", mArea.getAreaName());
                    response.addProperty("areaCode", mArea.getAreaCode() != null ? mArea.getAreaCode() : "");
                    response.addProperty("pincode", mArea.getPincode() != null ? mArea.getPincode() : "");
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
            areaLogger.error("Error in getAllAreaMaster:"+exceptionAsString);
        }
        return res;
    }

    public JsonObject getAreaMaster(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        try {
            AreaMaster area = areaMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            if (area != null) {
                response.addProperty("id", area.getId());
                response.addProperty("areaName", area.getAreaName());
                response.addProperty("areaCode", area.getAreaCode() != null ? area.getAreaCode() : "");
                response.addProperty("pincode", area.getPincode() != null ? area.getPincode() : "");
                result.addProperty("message", "success");
                result.addProperty("responseStatus", HttpStatus.OK.value());
                result.add("responseObject", response);
            } else {
                result.addProperty("message", "not found");
                result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            }
        }
        catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaLogger.error("Error in getAreaMaster:"+exceptionAsString);
        }
        return result;
    }

    public JsonObject updateAreaMaster(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            AreaMaster areaMaster = areaMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            areaMaster.setAreaName(request.getParameter("areaName").trim());
            areaMaster.setBranch(branch);
            areaMaster.setOutlet(users.getOutlet());
            areaMaster.setCreatedBy(users.getId());
            areaMaster.setUpdatedBy(users.getId());
            if (paramMap.containsKey("areaCode"))
                areaMaster.setAreaCode(request.getParameter("areaCode"));
            if (paramMap.containsKey("pincode"))
                areaMaster.setPincode(request.getParameter("pincode"));
            AreaMaster mArea = areaMasterRepository.save(areaMaster);
            responseObject.addProperty("message", "Area Master updated succussfully");
            responseObject.addProperty("responseStatus", HttpStatus.OK.value());
            responseObject.addProperty("responseObject", mArea.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaLogger.error("updateAreaMaster-> failed to update AreaMaster" + exceptionAsString);
            responseObject.addProperty("message", "Internal Server Error");
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaLogger.error("updateAreaMaster-> failed to update AreaMaster" + exceptionAsString);
            responseObject.addProperty("message", "Error");
        }
        return responseObject;
    }

    public JsonObject removeAreaMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        try {
            AreaMaster areaMaster = areaMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            if (areaMaster != null) {
                areaMaster.setStatus(false);
                areaMasterRepository.save(areaMaster);
                jsonObject.addProperty("message", "Area Master deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Error in Area Master deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaLogger.error("removeAreaMaster-> failed to delete AreaMaster" + exceptionAsString);
        }
        return jsonObject;
    }

    public JsonObject duplicateAreaMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        Long outletId = users.getOutlet().getId();
        String areaName = request.getParameter("areaName").trim();
        AreaMaster area = null;
        try {
            if (users.getBranch() != null) {
                area = areaMasterRepository.findByOutletIdAndAreaNameIgnoreCaseAndStatusAndBranchId(outletId, areaName, true,
                        users.getBranch().getId());
            } else {
                area = areaMasterRepository.findByOutletIdAndAreaNameIgnoreCaseAndStatusAndBranchIsNull(outletId, areaName,
                        true);
            }
            if (area != null) {
                jsonObject.addProperty("message", "Duplicate Area Master");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                jsonObject.addProperty("message", "New Area Master");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        }catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaLogger.error("duplicateAreaMaster-> failed to validation AreaMaster" + exceptionAsString);
        }
        return jsonObject;
    }

    public JsonObject duplicateAreaMasterUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        AreaMaster areaMaster = null;
        Long areaId = Long.parseLong(request.getParameter("id"));
        JsonObject result = new JsonObject();
        try {
            if (users.getBranch() != null) {
                areaMaster = areaMasterRepository.findByOutletIdAndBranchIdAndAreaNameAndStatus(users.getOutlet().getId(),
                        users.getBranch().getId(), request.getParameter("areaName"), true);
            } else {
                areaMaster = areaMasterRepository.findByOutletIdAndAreaNameAndStatusAndBranchIsNull(
                        users.getOutlet().getId(), request.getParameter("areaName"), true);
            }
            if (areaMaster != null && areaId != areaMaster.getId()) {
                result.addProperty("message", "duplicate area");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "New area master");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        }catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaLogger.error("duplicateAreaMasterUpdate-> failed to validation AreaMasterUpdate" + exceptionAsString);
        }
        return result;
    }
}
