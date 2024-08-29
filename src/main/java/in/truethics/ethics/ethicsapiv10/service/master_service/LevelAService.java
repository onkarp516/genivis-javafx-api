package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.FindCatlog;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.LevelA;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.LevelARepository;
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
public class LevelAService {
    @Autowired
    private LevelARepository repository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    private FindCatlog findCatlog;
    @Autowired
    private ProductUnitRepository productUnitRepository;

    private static final Logger levelLogger = LoggerFactory.getLogger(LevelAService.class);

    public Object addLevelA(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        try {
            if (users.getBranch() != null) {
                branch = users.getBranch();
            }
            if (validateLevelA(request.getParameter("levelName").trim(), users.getOutlet(), branch, 0L)) {
                responseObject.setMessage(request.getParameter("levelName").trim() + " already Created");
                responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
            } else {
                LevelA level = new LevelA();
                level.setLevelName(request.getParameter("levelName").trim());
                level.setBranch(branch);
                level.setOutlet(users.getOutlet());
                level.setCreatedBy(users.getId());
                level.setUpdatedBy(users.getId());
                level.setStatus(true);
                LevelA mLevel = repository.save(level);
                responseObject.setMessage("LevelA Created Successfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());
                responseObject.setResponseObject(mLevel.getId().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            levelLogger.error("addLevelA()-> failed to crate LevelA" + e);
            responseObject.setMessage("Internal server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return responseObject;
    }

    private boolean validateLevelA(String levelName, Outlet outlet, Branch branch, Long id) {
        Boolean flag = false;
        LevelA level = null;
        if (branch != null) {
            if (id != 0)
                level = repository.findByOutletIdAndBranchIdAndStatusAndLevelNameIgnoreCaseAndIdNot(outlet.getId(), branch.getId(),
                        true, levelName, id);
            else
                level = repository.findByOutletIdAndBranchIdAndStatusAndLevelNameIgnoreCase(outlet.getId(), branch.getId(),
                        true, levelName);
        } else {
            if (id != 0)
                level = repository.findByOutletIdAndStatusAndLevelNameIgnoreCaseAndIdNotAndBranchIsNull(
                        outlet.getId(), true, levelName, id);
            else
                level = repository.findByOutletIdAndStatusAndLevelNameIgnoreCaseAndBranchIsNull(outlet.getId(), true, levelName);
        }
        if (level != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    public JsonObject updateLevelA(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        JsonObject response = new JsonObject();
        Branch branch = null;
        LevelA level = repository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            if (validateLevelA(request.getParameter("levelName").trim(), users.getOutlet(), branch, level.getId())) {
                response.addProperty("message", request.getParameter("levelName").trim() + " already Created");
                response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {

                if (level != null) {
                    level.setLevelName(request.getParameter("levelName").trim());
                    level.setUpdatedBy(users.getId());
                    repository.save(level);
                    response.addProperty("message", "LevelA updated successfully");
                    response.addProperty("responseStatus", HttpStatus.OK.value());
                } else {
                    response.addProperty("message", "Not found");
                    response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            levelLogger.error("updateLevelA-> failed to update LevelA" + e);
        }
        return response;
    }

    public JsonObject getAllOutletLevelA(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<LevelA> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = repository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());

        } else {
            list = repository.findByOutletIdAndStatusAndBranchIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (LevelA mLevel : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mLevel.getId());
                response.addProperty("levelName", mLevel.getLevelName());
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

    public JsonObject getLevelAById(HttpServletRequest request) {
        LevelA level = repository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (level != null) {
            response.addProperty("id", level.getId());
            response.addProperty("levelName", level.getLevelName());
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "LevelA Not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }


    public JsonObject removeMultipleLevelA(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonArray usedArray = new JsonArray();
        JsonArray removedArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        String removeLevelAList = request.getParameter("removeLevelAList");
        JsonElement removeLevelAElement = parser.parse(removeLevelAList);
        JsonArray removeDeptJson = removeLevelAElement.getAsJsonArray();
        LevelA levelA = null;
        List<ProductUnitPacking> products = new ArrayList<>();
        if (removeDeptJson.size() > 0) {
            for (JsonElement mList : removeDeptJson) {
                Long object = mList.getAsLong();
                JsonObject removeLevelA = new JsonObject();
                JsonObject usedLevelA = new JsonObject();
                if (object != 0) {
                    if (branch != null) {
                        levelA = repository.findByOutletIdAndBranchIdAndIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                    } else {
                        levelA = repository.findByOutletIdAndBranchIsNullAndIdAndStatus(
                                users.getOutlet().getId(), object, true);
                    }
                    products = productUnitRepository.findByLevelAIdAndStatus(object, true);
                    if (products != null && products.size() > 0) {
                        usedLevelA.addProperty("message", levelA.getLevelName() + " is used in product ,First delete product");
                        usedLevelA.addProperty("name", levelA.getLevelName());
                        usedArray.add(usedLevelA);
                    } else {
                        if (levelA != null) levelA.setStatus(false);
                        try {
                            repository.save(levelA);
                            removeLevelA.addProperty("message", levelA.getLevelName() + " Deleted Successfully");
                            removeLevelA.addProperty("name", levelA.getLevelName());
                            removedArray.add(removeLevelA);
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
