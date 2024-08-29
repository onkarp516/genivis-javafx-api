package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ContentPackageMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.DrugTypeRepository;
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
public class DrugTypeService {
    @Autowired
    private DrugTypeRepository drugTypeRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;

    private static final Logger drugTypeLogger = LogManager.getLogger(DrugTypeService.class);

    public Object createDrugType(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();

        try {
            DrugType drugType = new DrugType();
            drugType.setDrugName(request.getParameter("drugName").trim());
            drugType.setStatus(true);
            DrugType mDrug = drugTypeRepository.save(drugType);
            responseObject.setMessage("Drug Type created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mDrug.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            drugTypeLogger.error("createDrugType-> failed to create DrugType" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            drugTypeLogger.error("createDrugType-> failed to create DrugType" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllDrugTypes(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<DrugType> list = new ArrayList<>();
        try {
            list = drugTypeRepository.findByStatus(true);
            if (list.size() > 0) {
                for (DrugType mDrug : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mDrug.getId());
                    response.addProperty("drugName", mDrug.getDrugName());
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
            drugTypeLogger.error("Error in getAllDrugType:"+exceptionAsString);
        }
        return res;
    }

}
