package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.AreaMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.DoctorMasterRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.awt.geom.Area;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DoctorMasterService {
    @Autowired
    private DoctorMasterRepository doctorMasterRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    private static final Logger doctorLogger = LogManager.getLogger(DoctorMasterService.class);

    public Object createDoctorMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();

        try {
            DoctorMaster doctorMaster = new DoctorMaster();
            doctorMaster.setDoctorName(request.getParameter("doctorName").trim());
            doctorMaster.setSpecialization(request.getParameter("specialization").trim());
            doctorMaster.setHospitalName(request.getParameter("hospitalName").trim());
            doctorMaster.setHospitalAddress(request.getParameter("hospitalAddress").trim());
            doctorMaster.setMobileNumber(request.getParameter("mobileNumber").trim());
//            if (paramMap.containsKey("commision"))
            doctorMaster.setCommision(!request.getParameter("commision").equalsIgnoreCase("") ? Long.valueOf(request.getParameter("commision")): 0);
            doctorMaster.setQualification(request.getParameter("qualification").trim());
            doctorMaster.setRegister(request.getParameter("registerNo").trim());
            doctorMaster.setOutletId(users.getOutlet().getId());
            doctorMaster.setStatus(true);
            DoctorMaster mContent = doctorMasterRepository.save(doctorMaster);
            responseObject.setMessage("Doctor Master created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mContent.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("createDoctorMaster-> failed to create DoctorMaster" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("createDoctorMaster-> failed to create DoctorMaster" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllDoctorMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<DoctorMaster> list = new ArrayList<>();
        try {
            list = doctorMasterRepository.findByStatus(true);
            if (list.size() > 0) {
                for (DoctorMaster mDoctor : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mDoctor.getId());
                    response.addProperty("doctorName", mDoctor.getDoctorName());
                    response.addProperty("specialization", mDoctor.getSpecialization());
                    response.addProperty("hospitalName", mDoctor.getHospitalName());
                    response.addProperty("hospitalAddress", mDoctor.getHospitalAddress());
                    response.addProperty("mobileNumber", mDoctor.getMobileNumber());
                    response.addProperty("qualification",mDoctor.getQualification());
                    response.addProperty("registerNo",mDoctor.getRegister());
                    response.addProperty("commision",mDoctor.getCommision());
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
            doctorLogger.error("Error in getAllDoctorMaster:"+exceptionAsString);
        }
        return res;
    }

    public JsonObject getDoctorMasterById(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        try {
            DoctorMaster doctor = doctorMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("id")),users.getOutlet().getId(), true);
            if (doctor != null) {
                response.addProperty("id", doctor.getId());
                response.addProperty("doctorName", doctor.getDoctorName());
                response.addProperty("specialization", doctor.getSpecialization());
                response.addProperty("hospitalName", doctor.getHospitalName());
                response.addProperty("hospitalAddress", doctor.getHospitalAddress());
                response.addProperty("mobileNumber", doctor.getMobileNumber());
                response.addProperty("qualification",doctor.getQualification());
                response.addProperty("registerNo",doctor.getRegister());
                response.addProperty("commision",doctor.getCommision());
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
            doctorLogger.error("Error in getDoctorMaster:"+exceptionAsString);
        }
        return result;
    }

    public JsonObject updateDoctorMaster(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        try {
            DoctorMaster doctorMaster = doctorMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("id")),users.getOutlet().getId(), true);
            doctorMaster.setDoctorName(request.getParameter("doctorName").trim());
            doctorMaster.setSpecialization(request.getParameter("specialization").trim());
            doctorMaster.setHospitalName(request.getParameter("hospitalName").trim());
            doctorMaster.setHospitalAddress(request.getParameter("hospitalAddress").trim());
            doctorMaster.setMobileNumber(request.getParameter("mobileNumber").trim());
            doctorMaster.setCommision(Long.valueOf(request.getParameter("commision")));
            doctorMaster.setQualification(request.getParameter("qualification"));
            doctorMaster.setRegister(request.getParameter("registerNo"));
            DoctorMaster mContent = doctorMasterRepository.save(doctorMaster);
            responseObject.addProperty("message", "Docter Master updated succussfully");
            responseObject.addProperty("responseStatus", HttpStatus.OK.value());
            responseObject.addProperty("responseObject", mContent.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("updateDoctorMaster-> failed to update DoctorMaster" + exceptionAsString);
            responseObject.addProperty("message", "Internal Server Error");
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("updateDoctorMaster-> failed to update DoctorMaster" + exceptionAsString);
            responseObject.addProperty("message", "Error");
        }
        return responseObject;
    }

    public JsonObject removeDoctorMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        try {
            DoctorMaster doctorMaster = doctorMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("id")),users.getOutlet().getId(), true);
            if (doctorMaster != null) {
                doctorMaster.setStatus(false);
                doctorMasterRepository.save(doctorMaster);
                jsonObject.addProperty("message", "Doctor Master deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Error in Content Master deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            doctorLogger.error("removeDoctorMaster-> failed to delete DoctorMaster" + exceptionAsString);
        }
        return jsonObject;
    }


}
