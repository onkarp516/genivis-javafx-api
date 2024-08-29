package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.FindCatlog;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.LevelB;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.LevelBRepository;
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
public class LevelBService {
    @Autowired
    private LevelBRepository repository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    private FindCatlog findCatlog;
    @Autowired
    private ProductUnitRepository productUnitRepository;

    private static final Logger levelLogger = LoggerFactory.getLogger(LevelBService.class);

    public Object addLevelB(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        try {
            if (users.getBranch() != null) {
                branch = users.getBranch();
            }
            if (validateLevelB(request.getParameter("levelName").trim(), users.getOutlet(), branch, 0L)) {
                responseObject.setMessage(request.getParameter("levelName").trim()+ " already Created");
                responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
            } else {
                LevelB level = new LevelB();
                level.setLevelName(request.getParameter("levelName").trim());
                level.setBranch(branch);
                level.setOutlet(users.getOutlet());
                level.setCreatedBy(users.getId());
                level.setUpdatedBy(users.getId());
                level.setStatus(true);
                LevelB mLevel = repository.save(level);
                responseObject.setMessage("LevelB Created Successfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());
                responseObject.setResponseObject(mLevel.getId().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            levelLogger.error("addLevelB()-> failed to crateLevelB" + e);
            responseObject.setMessage("Internal server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return responseObject;
    }

    private boolean validateLevelB(String levelName, Outlet outlet, Branch branch, Long id) {
        Boolean flag = false;
        LevelB level = null;
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

    public JsonObject updateLevelB(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        JsonObject response = new JsonObject();
        Branch branch = null;
        LevelB level = repository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            if (validateLevelB(request.getParameter("levelName").trim(), users.getOutlet(), branch, level.getId())) {
                response.addProperty("message", request.getParameter("levelName").trim()+ " already Created");
                response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {

                if (level != null) {
                    level.setLevelName(request.getParameter("levelName").trim());
                    level.setUpdatedBy(users.getId());
                    repository.save(level);
                    response.addProperty("message", "LevelB updated successfully");
                    response.addProperty("responseStatus", HttpStatus.OK.value());
                } else {
                    response.addProperty("message", "Not found");
                    response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            levelLogger.error("updateLevelB-> failed to updateLevelB" + e);
        }
        return response;
    }

    public JsonObject getAllOutletLevelB(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<LevelB> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = repository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());

        } else {
            list = repository.findByOutletIdAndStatusAndBranchIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (LevelB mLevel : list) {
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

    public JsonObject getLevelBById(HttpServletRequest request) {
        LevelB level = repository.findByIdAndStatus(Long.parseLong(
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
            result.addProperty("message", "LevelB Not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }


    public JsonObject removeMultipleLevelB(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonArray usedArray = new JsonArray();
        JsonArray removedArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        String removeLevelAList = request.getParameter("removeLevelBList");
        JsonElement removeLevelBElement = parser.parse(removeLevelAList);
        JsonArray removeDeptJson = removeLevelBElement.getAsJsonArray();
        LevelB levelB = null;
        List<ProductUnitPacking> products = new ArrayList<>();
        if (removeDeptJson.size() > 0) {
            for (JsonElement mList : removeDeptJson) {
                Long object = mList.getAsLong();
                JsonObject removeLevelB = new JsonObject();
                JsonObject usedLevelB = new JsonObject();
                if (object != 0) {
                    if (branch != null) {
                        levelB = repository.findByOutletIdAndBranchIdAndIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                    } else {
                        levelB = repository.findByOutletIdAndBranchIsNullAndIdAndStatus(
                                users.getOutlet().getId(), object, true);
                    }
                    products = productUnitRepository.findByLevelBIdAndStatus(object, true);
                    if (products != null && products.size() > 0) {
                        usedLevelB.addProperty("message", levelB.getLevelName() + " is used in product ,First delete product");
                        usedLevelB.addProperty("name", levelB.getLevelName());
                        usedArray.add(usedLevelB);
                    } else {
                        if (levelB != null) levelB.setStatus(false);
                        try {
                            repository.save(levelB);
                            removeLevelB.addProperty("message", levelB.getLevelName() + " Deleted Successfully");
                            removeLevelB.addProperty("name", levelB.getLevelName());
                            removedArray.add(removeLevelB);
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
