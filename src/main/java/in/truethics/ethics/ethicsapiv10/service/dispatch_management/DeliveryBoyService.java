package in.truethics.ethics.ethicsapiv10.service.dispatch_management;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageService;
import in.truethics.ethics.ethicsapiv10.model.dispatch_management.DeliveryBoy;
//import in.truethics.ethics.ethicsapiv10.model.master.AreaMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.dispatch_management_repository.DeliveryBoyRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class DeliveryBoyService {


    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Autowired
    private DeliveryBoyRepository deliveryBoyRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;


    @Autowired
    private FileStorageService fileStorageService;
    private static final Logger DeliveryLogger = LogManager.getLogger(DeliveryBoyService.class);

    public Object createDeliveryBoyData(MultipartHttpServletRequest request) {
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        Long branchId =null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            branchId=branch.getId();
        }
        try {
            DeliveryBoy deliveryBoy = new DeliveryBoy();
            deliveryBoy.setFirstName(request.getParameter("firstName").trim());
            deliveryBoy.setLastName(request.getParameter("lastName").trim());
            deliveryBoy.setAddress(request.getParameter("address").trim());
            deliveryBoy.setMobileNo(Long.valueOf(request.getParameter("mobileNo")));
            deliveryBoy.setBranchId(branchId);
            deliveryBoy.setOutletId(users.getOutlet().getId());
            deliveryBoy.setCreatedBy(users.getId());
            deliveryBoy.setUpdatedBy(users.getId());
            deliveryBoy.setStatus(true);

                if (request.getFile("identityDocument") != null) {
                    MultipartFile image = request.getFile("identityDocument");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator + "deliveryBoy" + File.separator);
                    String identityDocument = fileStorageService.storeFile(image, fileStorageProperties);

                    if (identityDocument != null) {
                        deliveryBoy.setIdentityDocument(File.separator + "uploads" + File.separator + "deliveryBoy" + File.separator + identityDocument);
                    } else {
                        responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                        responseObject.setMessage("Failed to upload image. Please try again!");
                        return responseObject;
                    }
                }
            DeliveryBoy deliveryBoy1 = deliveryBoyRepository.save(deliveryBoy);

            responseObject.setMessage("DeliveryBoy data created succussfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(deliveryBoy1.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            DeliveryLogger.error("createDeliveryBoyData-> failed to create DeliveryBoyData" + e);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            DeliveryLogger.error("createDeliveryBoyData-> failed to create DeliveryBoyData" + e1);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }


    public JsonObject getAllDeliveryBoyData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<DeliveryBoy> list = new ArrayList<>();
            if (users.getBranch() != null) {
                list = deliveryBoyRepository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());
        } else {
            list = deliveryBoyRepository.findByOutletIdAndStatusAndBranchIdIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (DeliveryBoy deliveryBoy1 : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", deliveryBoy1.getId());
                response.addProperty("firstName", deliveryBoy1.getFirstName());
                response.addProperty("lastName", deliveryBoy1.getLastName());
                response.addProperty("address", deliveryBoy1.getAddress());
                response.addProperty("mobileNo", deliveryBoy1.getMobileNo());
                response.addProperty("identityDocument", (deliveryBoy1.getIdentityDocument() != null ? serverUrl + deliveryBoy1.getIdentityDocument() : ""));
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

    public JsonObject getDeliveryBoyDataById(MultipartHttpServletRequest request) {

        DeliveryBoy deliveryBoy = deliveryBoyRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (deliveryBoy != null) {
            response.addProperty("id", deliveryBoy.getId());
            response.addProperty("firstName", deliveryBoy.getFirstName());
            response.addProperty("lastName", deliveryBoy.getLastName());
            response.addProperty("mobileNo", deliveryBoy.getMobileNo());
            response.addProperty("identityDocument", (deliveryBoy.getIdentityDocument() != null ? serverUrl + deliveryBoy.getIdentityDocument() : ""));
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", " Data not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public Object updateDeliveryBoyData(MultipartHttpServletRequest request) {
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        ResponseMessage responseObject = new  ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        Long branchId =null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            branchId=branch.getId();
        }
        try {
            DeliveryBoy deliveryBoy = deliveryBoyRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            deliveryBoy.setFirstName(request.getParameter("firstName").trim());
            deliveryBoy.setLastName(request.getParameter("lastName").trim());
            deliveryBoy.setAddress(request.getParameter("address").trim());
            deliveryBoy.setMobileNo(Long.valueOf(request.getParameter("mobileNo")));
                        deliveryBoy.setBranchId(branchId);
            deliveryBoy.setOutletId(users.getOutlet().getId());
            deliveryBoy.setCreatedBy(users.getId());
            deliveryBoy.setUpdatedBy(users.getId());

            if (request.getFile("identityDocument") != null) {
                MultipartFile image = request.getFile("identityDocument");
                fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator + "userprofile" + File.separator);
                String identityDocument = fileStorageService.storeFile(image, fileStorageProperties);

                if (identityDocument != null) {
                    deliveryBoy.setIdentityDocument(File.separator + "uploads" + File.separator + "identityDocument" + File.separator + identityDocument);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload image. Please try again!");
                    return responseObject;

                }
            }


            DeliveryBoy deliveryBoy1 = deliveryBoyRepository.save(deliveryBoy);
            responseObject.setMessage("Delivery boy data updated succussfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(deliveryBoy1.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            DeliveryLogger.error("updateDeliveryBoyData-> failed to update DeliveryBoyData" + e);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            DeliveryLogger.error("updateDeliveryBoyData-> failed to update DeliveryBoyData" + e1);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

    public JsonObject removeDeliveryBoyData(MultipartHttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        DeliveryBoy deliveryBoy = deliveryBoyRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (deliveryBoy != null) {
            deliveryBoy.setStatus(false);
            deliveryBoyRepository.save(deliveryBoy);
            jsonObject.addProperty("message", "Delivey boy data deleted successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        } else {
            jsonObject.addProperty("message", "Error in Delivey boy data deletion");
            jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return jsonObject;
    }
}
