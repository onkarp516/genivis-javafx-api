package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.BankMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.FranchiseMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.BankMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.BankMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FranchiseMasterRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BankMasterService {
    @Autowired
    private BankMasterRepository bankMasterRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    private static final Logger bankLogger = LogManager.getLogger(BankMasterService.class);
    @Autowired
    private FranchiseMasterRepository franchiseMasterRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${spring.serversource.frurl}")
    private String frUrl;



    public Object createBankMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            BankMaster bankMaster = new BankMaster();
            bankMaster.setBankName(request.getParameter("bankName").trim());
            if (paramMap.containsKey("branch"))
                bankMaster.setBranch(request.getParameter("branch"));
            bankMaster.setOutletId(users.getOutlet().getId());
            bankMaster.setBranchId(branch != null ? branch.getId() : null);
            bankMaster.setCreatedBy(users.getId());
            bankMaster.setUpdatedBy(users.getId());
            bankMaster.setStatus(true);
            if (paramMap.containsKey("accountNumber"))
                bankMaster.setAccountNumber(request.getParameter("accountNumber"));
            BankMaster mArea = bankMasterRepository.save(bankMaster);
            responseObject.setMessage("Bank Master created succussfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mArea.getId().toString());

            updateBankMasterAllFr(mArea);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bankLogger.error("createBankMaster-> failed to create BankMaster" + e);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            bankLogger.error("createBankMaster-> failed to create BankMaster" + e1);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }

   @Async
   public void updateBankMasterAllFr(BankMaster bank) {

        List<FranchiseMaster> franchiseMasters = franchiseMasterRepository.findByStatus(true);
        if (franchiseMasters != null) {
            System.out.println("Franchise Size : " + franchiseMasters.size());
            for (FranchiseMaster franchiseMaster : franchiseMasters) {
                System.out.println("Franchise : " + franchiseMaster.getFranchiseCode());

                HttpHeaders frHdr = new HttpHeaders();
                frHdr.setContentType(MediaType.MULTIPART_FORM_DATA);
                frHdr.add("branch", franchiseMaster.getFranchiseCode());
                if (bank!=null) {
                    LinkedMultiValueMap body = new LinkedMultiValueMap();
                    body.add("bankName", bank.getBankName());
                    if(bank.getBranch()!=null && !bank.getBranch().isEmpty())
                        body.add("branch", bank.getBranch());

                    if(bank.getAccountNumber()!=null && !bank.getAccountNumber().isEmpty())
                        body.add("accountNumber", bank.getAccountNumber());



                    HttpEntity frEntity = new HttpEntity<>(body, frHdr);

                    String resData = restTemplate.exchange(
                            frUrl + "/update_bank_gv_to_fr", HttpMethod.POST, frEntity, String.class).getBody();
                    System.out.println("frUpdateBankResponse => " + resData);

                }

            }
        }


    }
    public JsonObject getAllBankMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<BankMaster> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = bankMasterRepository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());
        } else {
            list = bankMasterRepository.findByOutletIdAndStatusAndBranchIdIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (BankMaster mArea : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mArea.getId());
                response.addProperty("bankName", mArea.getBankName());
                response.addProperty("branch", mArea.getBranch() != null ? mArea.getBranch() : "");
                response.addProperty("accountNumber", mArea.getAccountNumber() != null ? mArea.getAccountNumber() : "");
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

    public JsonObject getBankMaster(HttpServletRequest request) {
        BankMaster area = bankMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (area != null) {
            response.addProperty("id", area.getId());
            response.addProperty("bankName", area.getBankName());
            response.addProperty("branch", area.getBranch() != null ? area.getBranch() : "");
            response.addProperty("accountNumber", area.getAccountNumber() != null ? area.getAccountNumber() : "");
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public JsonObject updateBankMaster(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            BankMaster bankMaster = bankMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            bankMaster.setBankName(request.getParameter("bankName").trim());
            if (paramMap.containsKey("branch"))
                bankMaster.setBranch(request.getParameter("branch"));
            bankMaster.setOutletId(users.getOutlet().getId());
            bankMaster.setCreatedBy(users.getId());
            bankMaster.setUpdatedBy(users.getId());
            if (paramMap.containsKey("accountNumber"))
                bankMaster.setAccountNumber(request.getParameter("accountNumber"));
            BankMaster mBank = bankMasterRepository.save(bankMaster);
            responseObject.addProperty("message", "Bank Master updated succussfully");
            responseObject.addProperty("responseStatus", HttpStatus.OK.value());
            responseObject.addProperty("responseObject", mBank.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bankLogger.error("updateBankMaster-> failed to update BankMaster" + e);
            responseObject.addProperty("message", "Internal Server Error");
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            bankLogger.error("updateBankMaster-> failed to update BankMaster" + e1);
            responseObject.addProperty("message", "Error");
        }
        return responseObject;
    }

    public JsonObject removeBankMaster(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        BankMaster bankMaster = bankMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (bankMaster != null) {
            bankMaster.setStatus(false);
            bankMasterRepository.save(bankMaster);
            jsonObject.addProperty("message", "Bank Master deleted successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        } else {
            jsonObject.addProperty("message", "Error in Bank Master deletion");
            jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return jsonObject;
    }
}
