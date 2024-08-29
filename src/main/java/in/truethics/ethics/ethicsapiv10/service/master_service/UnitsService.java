package in.truethics.ethics.ethicsapiv10.service.master_service;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.FindCatlog;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.master.Units;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
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
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class UnitsService {

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private UnitsRepository repository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    private static final Logger unitsLogger = LogManager.getLogger(UnitsService.class);
    @Autowired
    private FindCatlog findCatlog;

    @Autowired
    private ProductUnitRepository productUnitRepository;


    public Object createUnit(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        ResponseMessage responseMessage = new ResponseMessage();
        if (users.getBranch() != null) branch = users.getBranch();
        if (validateUnits(request.getParameter("unitName").trim(), users.getOutlet(), branch, 0L)) {
            responseMessage.setMessage(request.getParameter("unitName").trim()+" already created ");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            try {
                Units units = new Units();
                if (users.getBranch() != null) units.setBranch(branch);
                units.setOutlet(users.getOutlet());
                units.setUnitName(request.getParameter("unitName").trim());
                units.setUnitCode(request.getParameter("unitCode"));
                units.setStatus(true);
                units.setCreatedBy(users.getId());
                Units mUnits = repository.save(units);
                responseMessage.setMessage("Unit created successfully");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
                responseMessage.setResponseObject(mUnits.getId());
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                unitsLogger.error("createUnit -> failed to createunit " + e);
                responseMessage.setMessage("error in unit creation");
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            } catch (Exception e1) {
                e1.printStackTrace();
                unitsLogger.error("createUnit -> failed to createunit " + e1);
                e1.getMessage();
            }
        }
        return responseMessage;
    }

    private boolean validateUnits(String unitName, Outlet outlet, Branch branch, Long id) {
        Boolean flag = false;
        Units units = null;
        if (branch != null) {
            if (id != 0)
                units = repository.findByOutletIdAndBranchIdAndUnitNameIgnoreCaseAndStatusAndIdNot(
                        outlet.getId(), branch.getId(), unitName, true, id);
            else
                units = repository.findByOutletIdAndBranchIdAndUnitNameIgnoreCaseAndStatus(outlet.getId(), branch.getId(), unitName, true);
        } else {
            if (id != 0)
                units = repository.findByOutletIdAndUnitNameIgnoreCaseAndStatusAndIdNot(outlet.getId(), unitName, true, id);
            else units = repository.findByOutletIdAndUnitNameIgnoreCaseAndStatus(outlet.getId(), unitName, true);
        }
        if (units != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    /* Get All units of outlet */
    public JsonObject getUnits(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<Units> unitsList = new ArrayList<>();
        if (users.getBranch() != null) {
            unitsList = repository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            unitsList = repository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        if (unitsList != null && unitsList.size() > 0) {
            for (Units mUnits : unitsList) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mUnits.getId());
                response.addProperty("unitName", mUnits.getUnitName());
                response.addProperty("unitCode", mUnits.getUnitCode());
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

    /* Get units by id */
    public JsonObject getUnitsById(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        Units mUnits = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (mUnits != null) {
            response.addProperty("id", mUnits.getId());
            response.addProperty("unitName", mUnits.getUnitName());
            response.addProperty("unitCode", mUnits.getUnitCode());
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public JsonObject updateUnit(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Units units = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (users.getBranch() != null) branch = users.getBranch();
        if (validateUnits(request.getParameter("unitName").trim(), users.getOutlet(), branch, units.getId())) {
            response.addProperty("message", request.getParameter("unitName").trim()+" already created ");
            response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            try {
                units.setUnitName(request.getParameter("unitName").trim());
                units.setUnitCode(request.getParameter("unitCode"));
                repository.save(units);
                response.addProperty("message", "Unit updated successfully");
                response.addProperty("responseStatus", HttpStatus.OK.value());
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                unitsLogger.error("updateUnit -> failed to updateunit " + e);
                response.addProperty("message", "Error");
                response.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
                System.out.println(e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
                unitsLogger.error("updateUnit -> failed to updateunit " + e1);
                e1.getMessage();
                response.addProperty("message", "Error");
                response.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
                System.out.println(e1.getMessage());
            }
        }
        return response;
    }

    public JsonObject removeUnit(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonParser jsonParser = new JsonParser();


        String removedUnitlist = request.getParameter("removedunitlist");
        JsonElement removedUnitElement = jsonParser.parse(removedUnitlist);
        JsonArray removeUnitArray = removedUnitElement.getAsJsonArray();
        Units units = null;
        if (removeUnitArray.size() > 0) {
            for (JsonElement mList : removeUnitArray) {
                long object = mList.getAsLong();
                if (object != 0) {
                    int count = 0;
                    String nameofCatlog = "unit";
                    count = findCatlog.removeCommonMethod(object, nameofCatlog);
//                    count=removeCommonMethod(object);

                    if (count > 0) {
                        res.addProperty("message", "Unit is used in Transaction,First delete Transaction");
                    } else {
                        units = repository.findByIdAndStatus(object, true);
                        if (units != null)
                            units.setStatus(false);

                        try {
                            repository.save(units);
                            res.addProperty("message", "Units Deleted Successfully");
                            res.addProperty("responseStatus", HttpStatus.OK.value());
                        } catch (Exception e) {
                            e.printStackTrace();
//                        ledgerLogger.error("Exception in updateDeptDetails:" + e.getMessage());
                            System.out.println("Exception:" + e.getMessage());
                            e.getMessage();
                            e.printStackTrace();

                        }
                    }
                }
            }
        }
        return res;
    }


    public JsonObject removeMultipleUnits(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonArray usedArray = new JsonArray();
        JsonArray removedArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        String removePackageList = request.getParameter("removedunitlist");
        JsonElement removePackageElement = parser.parse(removePackageList);
        JsonArray removeDeptJson = removePackageElement.getAsJsonArray();
        Units units = null;
        List<ProductUnitPacking> products = new ArrayList<>();
        if (removeDeptJson.size() > 0) {
            for (JsonElement mList : removeDeptJson) {
                Long object = mList.getAsLong();
                JsonObject removeUnits = new JsonObject();
                JsonObject usedUnits = new JsonObject();
                if (object != 0) {
                    if (branch != null) {
                        units = repository.findByOutletIdAndBranchIdAndIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                    } else {
                        units = repository.findByOutletIdAndBranchIsNullAndIdAndStatus(
                                users.getOutlet().getId(), object, true);
                    }
                    products = productUnitRepository.findByUnitsIdAndStatus(
                            object, true);
                    if (products != null && products.size() > 0) {
                        usedUnits.addProperty("message", units.getUnitName() + " is used in product ,First delete product");
                        usedUnits.addProperty("name", units.getUnitName());
                        usedArray.add(usedUnits);
                    } else {
                        if (units != null) units.setStatus(false);
                        try {
                            repository.save(units);
                            removeUnits.addProperty("message", units.getUnitName() + " Deleted Successfully");
                            removeUnits.addProperty("name", units.getUnitName());
                            removedArray.add(removeUnits);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Exception:" + e.getMessage());
                            e.getMessage();
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        finalObject.add("removedArray", removedArray);
        finalObject.add("usedArray", usedArray);
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        return finalObject;
    }
}
