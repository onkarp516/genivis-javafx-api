package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.EcommerceType;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.EcommerceTypeRepository;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EcommerceTypeService {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private EcommerceTypeRepository ecommerceTypeRepository;
    @Autowired
    private EntityManager entityManager;

    public Object createEcommerceType(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            Users users = jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            EcommerceType ecommerceType = null;

            if (users.getBranch() != null)
                ecommerceType = ecommerceTypeRepository.findByTypeAndOutletIdAndBranchIdAndStatus(
                        request.getParameter("ecomType"), users.getOutlet().getId(), users.getBranch().getId(), true);
            else
                ecommerceType = ecommerceTypeRepository.findByTypeAndOutletIdAndStatus(
                        request.getParameter("ecomType"), users.getOutlet().getId(), true);
            if (ecommerceType != null) {
                response.addProperty("message", request.getParameter("ecomType").trim()+" already created");
                response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }
            else {
                ecommerceType = new EcommerceType();

                ecommerceType.setType(request.getParameter("ecomType"));
                ecommerceType.setCreatedBy(users.getId());
                ecommerceType.setOutletId(users.getOutlet().getId());
                if(users.getBranch() != null)   ecommerceType.setBranchId(users.getBranch().getId());
                ecommerceType.setStatus(true);
                try {
                    ecommerceTypeRepository.save(ecommerceType);
                    response.addProperty("message", "Ecommerce Type saved successfully");
                    response.addProperty("responseStatus", HttpStatus.OK.value());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Exception " + e.getMessage());
                    response.addProperty("message", "Failed to save data");
                    response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            response.addProperty("message", "Failed to save data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object getAllEcommerceType(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try{
            Users users = jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            List<EcommerceType> typeList = new ArrayList<>();

            String sql = "SELECT * FROM `ecommerce_type_tbl` WHERE status=1 AND outlet_id="+users.getOutlet().getId();
            if (users.getBranch() != null)
                sql+=" AND branch_id="+users.getBranch().getId();
            System.out.println("sql :"+sql);
            Query query = entityManager.createNativeQuery(sql, EcommerceType.class);
            typeList = query.getResultList();

            JsonArray typeArray = new JsonArray();
            for(EcommerceType ecommerceType : typeList){
                JsonObject object = new JsonObject();
                object.addProperty("id", ecommerceType.getId());
                object.addProperty("ecommerceType", ecommerceType.getType());
                object.addProperty("status", ecommerceType.getStatus());
                object.addProperty("outletId", ecommerceType.getOutletId());
                object.addProperty("createdBy", ecommerceType.getCreatedBy());
                object.addProperty("createdAt", ecommerceType.getCreatedAt().toString());
                typeArray.add(object);
            }
            response.add("response", typeArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }
}
