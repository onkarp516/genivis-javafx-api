package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductContentMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ContentMasterDoseRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ContentMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ContentPackageMasterRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.awt.geom.Area;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ContentMasterService {

    @Autowired
    private ContentMasterRepository contentMasterRepository;

    @Autowired
    private ContentPackageMasterRepository contentPackageMasterRepository;

    @Autowired
    private ContentMasterDoseRepository contentMasterDoseRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;


    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    EntityManager entityManager;

    private static final Logger contentLogger = LogManager.getLogger(ContentMasterService.class);
    @Autowired
    private ProductContentMasterRepository productContentMasterRepository;

    public Object createContentMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();

        try {
            ContentMaster contentMaster = new ContentMaster();
            contentMaster.setContentName(request.getParameter("contentName").trim());
            contentMaster.setStatus(true);
            ContentMaster mContent = contentMasterRepository.save(contentMaster);
            responseObject.setMessage("Content Master created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mContent.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            contentLogger.error("createContentMaster-> failed to create ContentMaster" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            contentLogger.error("createContentMaster-> failed to create ContentMaster" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllContentMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<ContentMaster> list = new ArrayList<>();
        try {
            list = contentMasterRepository.findByStatus(true);
            if (list.size() > 0) {
                for (ContentMaster mContent : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mContent.getId());
                    response.addProperty("contentName", mContent.getContentName());
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
            contentLogger.error("Error in getAllContentMaster:"+exceptionAsString);
        }
        return res;
    }

    public JsonObject getContentMaster(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        try {
            ContentMaster content = contentMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            if (content != null) {
                response.addProperty("id", content.getId());
                response.addProperty("contentName", content.getContentName());
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
            contentLogger.error("Error in getContentMaster:"+exceptionAsString);
        }
        return result;
    }

    public JsonObject updateContentMaster(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        try {
            ContentMaster contentMaster = contentMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            contentMaster.setContentName(request.getParameter("contentName").trim());
            ContentMaster mContent = contentMasterRepository.save(contentMaster);
            responseObject.addProperty("message", "Content Master updated succussfully");
            responseObject.addProperty("responseStatus", HttpStatus.OK.value());
            responseObject.addProperty("responseObject", mContent.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            contentLogger.error("updateContentMaster-> failed to update ContentMaster" + exceptionAsString);
            responseObject.addProperty("message", "Internal Server Error");
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            contentLogger.error("updateContentMaster-> failed to update ContentMaster" + exceptionAsString);
            responseObject.addProperty("message", "Error");
        }
        return responseObject;
    }

    public JsonObject removeContentMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        try {
            ContentMaster contentMaster = contentMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            if (contentMaster != null) {
                String contentMasterName=contentMaster.getContentName();
                int count = productContentMasterRepository.countOfContentType(contentMasterName);
                if(count>0){
                    jsonObject.addProperty("message", "Content already used for Product");
                    jsonObject.addProperty("responseStatus",  HttpStatus.CONFLICT.value());
                }else {
                    contentMaster.setStatus(false);
                    contentMasterRepository.save(contentMaster);
                    jsonObject.addProperty("message", "Content Master deleted successfully");
                    jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
                }
            } else {
                jsonObject.addProperty("message", "Error in Content Master deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            contentLogger.error("removeContentMaster-> failed to delete ContentMaster" + exceptionAsString);
        }
        return jsonObject;
    }


    public JsonObject getAllContentPackageMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<ContentPackageMaster> list = new ArrayList<>();
        try {
            list = contentPackageMasterRepository.findByStatus(true);
            if (list.size() > 0) {
                for (ContentPackageMaster mContent : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mContent.getId());
                    response.addProperty("contentPackageName", mContent.getContentPackageName());
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
            contentLogger.error("Error in getAllContentPackageMaster:"+exceptionAsString);
        }
        return res;
    }


    public Object createContentMasterDose(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();

        try {
            ContentMasterDose contentMasterDose = new ContentMasterDose();
            contentMasterDose.setContentNameDose(request.getParameter("contentNameDose").trim());
            contentMasterDose.setStatus(true);
            ContentMasterDose mContent = contentMasterDoseRepository.save(contentMasterDose);
            responseObject.setMessage("Content Master Dose created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mContent.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            contentLogger.error("createContentMasterDose-> failed to create ContentMasterDose" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            contentLogger.error("createContentMasterDose-> failed to create ContentMasterDose" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject getAllContentMasterDose(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<ContentMasterDose> list = new ArrayList<>();
        try {
            list = contentMasterDoseRepository.findByStatus(true);
            if (list.size() > 0) {
                for (ContentMasterDose mContentDose : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mContentDose.getId());
                    response.addProperty("contentNameDose", mContentDose.getContentNameDose());
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
            contentLogger.error("Error in getAllContentMasterDose:"+exceptionAsString);
        }
        return res;
    }

    public JsonObject validateContentName(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ContentMaster contentMaster = null;
        contentMaster = contentMasterRepository.findByContentNameAndStatus(request.getParameter("contentName"), true);

        JsonObject result = new JsonObject();
        if (contentMaster != null) {
            result.addProperty("message", "Duplicate Content Name");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            result.addProperty("message", "New Content Name");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return result;
    }

}
