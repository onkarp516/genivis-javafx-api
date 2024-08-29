package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.BalancingMethod;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.BalancingMethodRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class BalancingMethodService {

    @Autowired
    BalancingMethodRepository repository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;

    private static final Logger balancingMethodLogger = LoggerFactory.getLogger(BalancingMethodService.class);

    public Object createBalancingMethod(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        BalancingMethod balancingMethod = new BalancingMethod();
        balancingMethod.setBalancingMethod(request.getParameter("balancing_method"));
        balancingMethod.setStatus(true);
        balancingMethod.setCreatedBy(users.getId());
        ResponseMessage responseMessage = new ResponseMessage();
        try {
            repository.save(balancingMethod);
            responseMessage.setMessage("Balancing method created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            balancingMethodLogger.error("createBalancingMethod -> failed to create Balancing method" + e);

            System.out.println(e.getMessage());
        }
        return responseMessage;
    }

    public JsonObject getBalancingMethod(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject output = new JsonObject();
        List<BalancingMethod> list = new ArrayList<>();
        list = repository.findAll();
        if (list.size() > 0) {
            for (BalancingMethod mBal : list) {
                JsonObject response = new JsonObject();
                response.addProperty("balancing_id", mBal.getId());
                response.addProperty("balance_method", mBal.getBalancingMethod());
                result.add(response);
            }
            output.addProperty("message", "success");
            output.addProperty("responseStatus", HttpStatus.OK.value());
            output.add("response", result);
        } else {
            output.addProperty("message", "empty list");
            output.addProperty("responseStatus", HttpStatus.OK.value());
            output.add("response", result);
        }
        return output;
    }

}
