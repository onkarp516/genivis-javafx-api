package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.LevelA;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.master.PaymentModeMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.PaymentModeMasterRepository;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentModeMasterService {
    @Autowired
    private PaymentModeMasterRepository repository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    private static final Logger paymentLogger = LogManager.getLogger(PaymentModeMasterService.class);

    public JsonObject getAllPaymentModes(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<PaymentModeMaster> list = new ArrayList<>();
       list = repository.findAllByStatus(true);
        if (list.size() > 0) {
            for (PaymentModeMaster paymentMode : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", paymentMode.getId());
                response.addProperty("payment_mode", paymentMode.getPaymentMode());
                result.add(response);
            }
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);

        } else {
            res.add("responseObject", result);
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return res;
    }
    public JsonObject createPaymentMode(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Long branchId = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            branchId = branch.getId();
        }
        if (validatePaymentMode(request.getParameter("payment_mode").trim(), users.getOutlet(), branch, 0L)) {
            jsonObject.addProperty("message", request.getParameter("payment_mode").trim() + " already created");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            PaymentModeMaster mPaymentMode = null;
            PaymentModeMaster paymentModeMaster = new PaymentModeMaster();
            paymentModeMaster.setPaymentMode(request.getParameter("payment_mode").trim());
            paymentModeMaster.setBranchId(branchId);
            paymentModeMaster.setOutletId(users.getOutlet().getId());
            paymentModeMaster.setStatus(true);

            try {
                mPaymentMode = repository.save(paymentModeMaster);
            } catch (Exception e) {
                e.printStackTrace();

                jsonObject.addProperty("message", "error");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            jsonObject.addProperty("message", "Payment mode created successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            jsonObject.addProperty("responseObject", mPaymentMode.getId());
        }
        return jsonObject;
    }

    private boolean validatePaymentMode(String paymentMode, Outlet outlet, Branch branch, long l) {
        Boolean flag = false;
        PaymentModeMaster mpaymentMode = null;
        if (branch != null) {
            mpaymentMode = repository.findByOutletIdAndBranchIdAndPaymentModeIgnoreCaseAndStatus(outlet.getId(), branch.getId(), paymentMode, true);

        } else {
            mpaymentMode = repository.findByOutletIdAndPaymentModeIgnoreCaseAndStatusAndBranchIdIsNull(
                    outlet.getId(), paymentMode, true);
        }

        if (mpaymentMode != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    public JsonObject editPaymentMode(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        PaymentModeMaster paymentModeMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (users.getBranch() != null) branch = users.getBranch();
        if (validatePaymentMode(request.getParameter("payment_mode").trim(), users.getOutlet(), branch, paymentModeMaster.getId())) {
            jsonObject.addProperty("message", request.getParameter("payment_mode").trim()+ " already created");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            try {
                paymentModeMaster.setPaymentMode(request.getParameter("payment_mode").trim());
                repository.save(paymentModeMaster);
                jsonObject.addProperty("message", "Payment Mode updated successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } catch (Exception e) {
                e.printStackTrace();
                paymentLogger.error("updatePackaging -> failed to update payment mode" + e);
                jsonObject.addProperty("message", "error");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
        return jsonObject;
    }

    public JsonObject getPaymentModeById(HttpServletRequest request) {
        JsonObject mObject = new JsonObject();
        JsonObject res = new JsonObject();
        PaymentModeMaster mPayment = null;
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            mPayment = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            mObject.addProperty("id", mPayment.getId());
            mObject.addProperty("paymentMode", mPayment.getPaymentMode());
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("data", mObject);
        } catch (Exception e) {
            e.printStackTrace();
            paymentLogger.error("updgetPackagingById-> failed to updgetPaymentById" + e);
        }
        return res;

    }

    public JsonObject removePaymentMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        try {
            PaymentModeMaster paymentModeMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            if (paymentModeMaster != null) {
                paymentModeMaster.setStatus(false);
                repository.save(paymentModeMaster);
                jsonObject.addProperty("message", "Payment mode Master deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Error in Payment Master deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            paymentLogger.error("removePaymentMaster-> failed to delete PaymentMaster" + exceptionAsString);
        }
        return jsonObject;
    }
}
