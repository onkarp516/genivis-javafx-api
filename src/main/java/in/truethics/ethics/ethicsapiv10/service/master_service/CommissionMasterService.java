package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CommissionMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.AreaHeadRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ContentPackageMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FranchiseMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.AreaheadCommissionRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CommissionMasterService {
    @Autowired
    private CommissionMasterRepository commisssionMasterRepository;

    @Autowired
    private ContentPackageMasterRepository contentPackageMasterRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;

    private static final Logger commissionMasterLogger = LogManager.getLogger(CommissionMasterService.class);
    @Autowired
    private AreaheadCommissionRepository areaheadCommissionRepository;
    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private FranchiseMasterRepository franchiseMasterRepository;
    @Autowired
    private AreaHeadRepository areaHeadRepository;


    public Object createCommissionMaster(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();

        try {
            CommissionMaster commissionMaster = new CommissionMaster();
            commissionMaster.setRoleType(request.getParameter("roleType").trim());
            commissionMaster.setProductLevel(request.getParameter("productLevel").trim());
            commissionMaster.setStatus(true);
            if (paramMap.containsKey("tds_per"))
                commissionMaster.setTdsPer(Double.parseDouble(request.getParameter("tds_per")));
            else commissionMaster.setTdsPer(0.0);
            CommissionMaster mContent = commisssionMasterRepository.save(commissionMaster);
            responseObject.setMessage("Commission Mastercreated successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setResponseObject(mContent.getId().toString());
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            commissionMasterLogger.error("createCommissionMaster-> failed to create CommissionMaster" + exceptionAsString);
            responseObject.setMessage("Internal Server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            commissionMasterLogger.error("createCommissionMaster-> failed to create CommissionMaster" + exceptionAsString);
            responseObject.setMessage("Error");
        }
        return responseObject;
    }


    public JsonObject getAllCommissionMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<CommissionMaster> list = new ArrayList<>();
        try {
            list = commisssionMasterRepository.findByStatus(true);
            if (list.size() > 0) {
                for (CommissionMaster mCommission : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mCommission.getId());
                    response.addProperty("roleType", mCommission.getRoleType());
                    response.addProperty("tdsPer", mCommission.getTdsPer() != null ? mCommission.getTdsPer() :
                            0.00);
                    response.addProperty("productLevel", mCommission.getProductLevel());
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
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            commissionMasterLogger.error("Error in getAllCommissionMaster:" + exceptionAsString);
        }
        return res;
    }

    public JsonObject getCommissionMasterById(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        try {
            CommissionMaster commission = commisssionMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            if (commission != null) {
                response.addProperty("id", commission.getId());
                response.addProperty("roleType", commission.getRoleType());
                response.addProperty("tdsPer", commission.getTdsPer());
                response.addProperty("productLevel", commission.getProductLevel());
                result.addProperty("message", "success");
                result.addProperty("responseStatus", HttpStatus.OK.value());
                result.add("responseObject", response);
            } else {
                result.addProperty("message", "not found");
                result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            commissionMasterLogger.error("Error in getCommissionMaster:" + exceptionAsString);
        }
        return result;
    }

    public JsonObject updateCommissionMaster(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        try {
            CommissionMaster commissionMaster = commisssionMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            commissionMaster.setRoleType(request.getParameter("roleType").trim());
            commissionMaster.setProductLevel(request.getParameter(("productLevel").trim()));
            if (paramMap.containsKey("tds_per"))
                commissionMaster.setTdsPer(Double.parseDouble(request.getParameter("tds_per")));
            CommissionMaster mContent = commisssionMasterRepository.save(commissionMaster);
            responseObject.addProperty("message", "Commission Master updated succussfully");
            responseObject.addProperty("responseStatus", HttpStatus.OK.value());
            responseObject.addProperty("responseObject", mContent.getId().toString());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            commissionMasterLogger.error("updateCommissionMaster-> failed to update CommissionMaster" + exceptionAsString);
            responseObject.addProperty("message", "Internal Server Error");
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            commissionMasterLogger.error("updateCommissionMaster-> failed to update CommissionMaster" + exceptionAsString);
            responseObject.addProperty("message", "Error");
        }
        return responseObject;
    }

    public JsonObject removeCommissionMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        try {
            CommissionMaster commissionMaster = commisssionMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            if (commissionMaster != null) {
                commissionMaster.setStatus(false);
                commisssionMasterRepository.save(commissionMaster);
                jsonObject.addProperty("message", "Commission Master deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Error in Commission Master deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            commissionMasterLogger.error("removeContentMaster-> failed to delete ContentMaster" + exceptionAsString);
        }
        return jsonObject;
    }

    public Object validateCommissionMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        String roleType = request.getParameter("roleType");
        CommissionMaster commissionMaster = commisssionMasterRepository.findByRoleTypeIgnoreCaseAndStatus(roleType, true);
        if (commissionMaster != null) {
            responseMessage.setMessage("Duplicate Commission Master");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New Ledger");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object validateCommissionMasterUpdate(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        String roleType = request.getParameter("roleType");
        Long id = Long.parseLong(request.getParameter("id"));
        CommissionMaster commissionMaster = commisssionMasterRepository.findByRoleTypeIgnoreCaseAndStatus(roleType, true);
        if (commissionMaster != null && id != commissionMaster.getId()) {
            responseMessage.setMessage("Duplicate Commission Master");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New company");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }


    public JsonObject getPartnerCommissionPayment(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        String strFrom = "";
        String strTo = "";
        LocalDate startDate = null;
        LocalDate endDate = null;
        List<AreaheadCommission> mList = new ArrayList<>();
        if (paramMap.containsKey("fromDate")) {
            strFrom = request.getParameter("fromDate");
            startDate = LocalDate.parse(strFrom);
        }
        if (paramMap.containsKey("toDate")) {
            strTo = request.getParameter("toDate");
            endDate = LocalDate.parse(strTo);
        }
        if (startDate != null && endDate != null) {

        } else {
            startDate = LocalDate.now();
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }
        mList = areaheadCommissionRepository.findList(startDate, endDate, true);
        JsonArray mArray = new JsonArray();
        JsonObject result = new JsonObject();
        for (AreaheadCommission mCommission : mList) {
            JsonObject object = new JsonObject();
            TranxSalesInvoice mInvoice = tranxSalesInvoiceRepository.findBySalesInvoiceNoAndStatus(
                    mCommission.getSalesInvoiceNumber(), true);
            FranchiseMaster franchiseMaster = franchiseMasterRepository.
                    findByFranchiseCodeAndStatus(mCommission.getFranchiseCode(), true);
            object.addProperty("franchise_name", franchiseMaster != null ? franchiseMaster.getFranchiseName() : "");
            object.addProperty("sales_date", mCommission.getInvoiceDate().toString());
            object.addProperty("sales_invoice_no", mCommission.getSalesInvoiceNumber());
            object.addProperty("sales_invoice_amt", mCommission.getSalesInvoiceAmount());
            object.addProperty("sales_tax_amt", mInvoice != null ? mInvoice.getTotalTax() : 0.00);
            AreaHead areaHead = areaHeadRepository.findByIdAndStatus(mCommission.getAreaheadId(), true);
            object.addProperty("partner_name", areaHead != null ?
                    (areaHead.getFirstName() + " " + areaHead.getLastName()) : "");
            object.addProperty("designation", areaHead != null ? areaHead.getAreaRole() : "");
            object.addProperty("taxable_amt", mCommission.getCommissionAmount());
            object.addProperty("taxable_amt", mCommission.getInvoiceBaseAmount());
            Double incentiveAmt= mCommission.getCommissionAmount();
            object.addProperty("incentive_amt", incentiveAmt);
            CommissionMaster commissionMaster = commisssionMasterRepository.
                    findByRoleTypeIgnoreCase(mCommission.getAreaheadRole());
            Double tdsPer = commissionMaster.getTdsPer();
            Double tdsVal = incentiveAmt * tdsPer / 100.00;
            object.addProperty("tds", tdsVal);
            Double partnerCommisionAmt = incentiveAmt - tdsVal;
            object.addProperty("payment", partnerCommisionAmt);
            mArray.add(object);
        }
        result.addProperty("message","success");
        result.addProperty("responseStatus",HttpStatus.OK.value());
        result.add("list",mArray);
        return result;
    }
}
