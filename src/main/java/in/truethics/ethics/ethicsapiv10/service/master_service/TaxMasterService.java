package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.TaxMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TaxMasterRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TaxMasterService {
    @Autowired
    private TaxMasterRepository repository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    private static final Logger taxMasterLogger = LogManager.getLogger(TaxMasterService.class);

    public JsonObject createTaxMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseMessage = new ResponseMessage();
        JsonObject result = new JsonObject();
        Branch branch = null;
        String taxmastergst = request.getParameter("gst_per");
        /*if(taxmastergst.matches("\\d*\\.?\\d+")){
            result.addProperty("message", "Invalid Tax Format ! ");
            result.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return result;
        }*/
        int taxValue = Integer.parseInt(taxmastergst);

            TaxMaster findgst = repository.findDuplicateGSTWithOutlet(users.getOutlet().getId(), taxmastergst, true);
            if (findgst == null) {
                if (users.getBranch() != null) {
                    branch = users.getBranch();
                }
                try {
                    TaxMaster taxMaster = new TaxMaster();
                    taxMaster.setBranch(branch);
                    taxMaster.setOutlet(users.getOutlet());
                    taxMaster.setGst_per(request.getParameter("gst_per").trim());
                    if (paramMap.containsKey("igst")) {
                        taxMaster.setIgst(Double.parseDouble(request.getParameter("igst")));
                    }
                    if (paramMap.containsKey("cgst")) {
                        taxMaster.setCgst(Double.parseDouble(request.getParameter("cgst")));
                    }
                    if (paramMap.containsKey("sgst")) {
                        taxMaster.setSgst(Double.parseDouble(request.getParameter("sgst")));
                    }
                    taxMaster.setSratio(Double.parseDouble(request.getParameter("sratio")));
                    LocalDate applicableDate = null;
                    if (!request.getParameter("applicable_date").equalsIgnoreCase("")) {
                        applicableDate = LocalDate.parse(request.getParameter("applicable_date"));
                    }
                    taxMaster.setApplicableDate(applicableDate);
                    taxMaster.setStatus(true);
                    taxMaster.setCreatedBy(users.getId());
                    TaxMaster taxM = repository.save(taxMaster);
                    result.addProperty("message", "Tax created successfully");
                    result.addProperty("responseStatus", HttpStatus.OK.value());
                    result.addProperty("responseObject", taxM.getId().toString());
                } catch (DataIntegrityViolationException e) {
                    e.printStackTrace();
                    taxMasterLogger.error("createTaxMaster-> failed to createTaxMaster" + e);
                    result.addProperty("message", "Error in Tax Creation ");
                    result.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
                    e.getMessage();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    taxMasterLogger.error("createTaxMaster -> failed to createTaxMaster" + e1);
                    e1.getMessage();
                }
            } else {

                result.addProperty("message", "Tax already created ! ");
                result.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

        return result;
    }

    public JsonObject getTaxMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TaxMaster> taxMasters = new ArrayList<>();
        if (users.getBranch() != null) {
            taxMasters = repository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            taxMasters = repository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        if (taxMasters.size() > 0) {
            for (TaxMaster mTaxMaster : taxMasters) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mTaxMaster.getId());
                response.addProperty("igst", mTaxMaster.getIgst());
                response.addProperty("cgst", mTaxMaster.getCgst());
                response.addProperty("sgst", mTaxMaster.getSgst());
                response.addProperty("gst_per", mTaxMaster.getGst_per());
                response.addProperty("ratio", mTaxMaster.getSratio());
                response.addProperty("applicable_date", mTaxMaster.getApplicableDate() != null ? mTaxMaster.getApplicableDate().toString() : "");
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

    public JsonObject getTaxMasterbyId(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        TaxMaster taxMasters = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (taxMasters != null) {
            response.addProperty("id", taxMasters.getId());
            response.addProperty("igst", taxMasters.getIgst());
            response.addProperty("cgst", taxMasters.getCgst());
            response.addProperty("sgst", taxMasters.getSgst());
            response.addProperty("gst_per", taxMasters.getGst_per());
            response.addProperty("ratio", taxMasters.getSratio());
            response.addProperty("applicable_date", taxMasters.getApplicableDate() != null ? taxMasters.getApplicableDate().toString() : "");
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public JsonObject updateTaxMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TaxMaster taxMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        LocalDate applicableDate = null;
        try {
            taxMaster.setIgst(Double.parseDouble(request.getParameter("igst")));
            taxMaster.setCgst(Double.parseDouble(request.getParameter("cgst")));
            taxMaster.setSgst(Double.parseDouble(request.getParameter("sgst")));
            // taxMaster.setApplicableDate(LocalDate.parse(request.getParameter("applicable_date")));
            if (!request.getParameter("applicable_date").equalsIgnoreCase("")) {
                applicableDate = LocalDate.parse(request.getParameter("applicable_date"));
            }
            taxMaster.setApplicableDate(applicableDate);
            taxMaster.setGst_per(request.getParameter("gst_per").trim());
            taxMaster.setSratio(Double.parseDouble(request.getParameter("sratio")));
            taxMaster.setCreatedBy(users.getId());
            repository.save(taxMaster);
            response.addProperty("message", "Tax updated successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            taxMasterLogger.error("updateTaxMaster -> failed to updateTaxMaster" + e);
            response.addProperty("message", "error");
            response.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
            System.out.println(e.getMessage());
            e.getMessage();

        } catch (Exception e1) {
            e1.printStackTrace();
            taxMasterLogger.error("updateTaxMaster-> failed to update TaxMaster" + e1);
            System.out.println(e1.getMessage());
        }
        return response;
    }

    public JsonObject validateTax(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        TaxMaster taxMaster = null;
        if (users.getBranch() != null) {
            taxMaster = repository.findDuplicateGSTWithBranch(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("gst_per"), true);
        } else {
            taxMaster = repository.findDuplicateGSTWithOutlet(users.getOutlet().getId(), request.getParameter("gst_per"), true);
        }
        JsonObject result = new JsonObject();
        if (taxMaster != null) {
            result.addProperty("message", "duplicate Tax value");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            result.addProperty("message", "new Tax");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return result;
    }

    public JsonObject productTaxDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        TaxMaster taxMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);

        count = productUnitRepository.findByProductTaxTranx(taxMaster.getId(), true);

        try {
            if (count == 0) {
                taxMaster.setStatus(false);
                repository.save(taxMaster);
                jsonObject.addProperty("message", "Tax deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());

            } else {
                jsonObject.addProperty("message", "Tax is uesed in product,First delete product");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return jsonObject;
    }
}
