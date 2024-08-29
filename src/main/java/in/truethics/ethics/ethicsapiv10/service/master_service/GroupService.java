package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Group;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.GroupRepository;
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
public class GroupService {


    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    GroupRepository groupRepository;

    @Autowired
    private ProductRepository productRepository;

    private static final Logger groupLogger = LoggerFactory.getLogger(GroupService.class);


    public Object addGroup(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        try {
            if (users.getBranch() != null) {
                branch = users.getBranch();
            }
            if (validateGroup(request.getParameter("groupName").trim(), users.getOutlet(), branch, 0L)) {
                responseObject.setMessage(request.getParameter("groupName").trim()+ " already Created");
                responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
            } else {
                Group group = new Group();
                group.setGroupName(request.getParameter("groupName").trim());
                group.setBranch(branch);
                group.setOutlet(users.getOutlet());
                group.setCreatedBy(users.getId());
                group.setUpdatedBy(users.getId());
                group.setStatus(true);
                Group mSubgroup = groupRepository.save(group);
                responseObject.setMessage("Group Created Successfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());
                responseObject.setResponseObject(mSubgroup.getId().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            groupLogger.error("addGroup-> failed to addGroup" + e);
            responseObject.setMessage("Internal server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return responseObject;
    }

    private boolean validateGroup(String groupName, Outlet outlet, Branch branch, Long id) {
        Boolean flag = false;
        Group group = null;
        if (branch != null) {
            if (id != 0)
                group = groupRepository.findByOutletIdAndBranchIdAndStatusAndGroupNameIgnoreCaseAndIdNot(outlet.getId(), branch.getId(),
                        true, groupName, id);
            else
                group = groupRepository.findByOutletIdAndBranchIdAndStatusAndGroupNameIgnoreCase(outlet.getId(), branch.getId(),
                        true, groupName);
        } else {
            if (id != 0)
                group = groupRepository.findByOutletIdAndStatusAndGroupNameIgnoreCaseAndIdNotAndBranchIsNull(
                        outlet.getId(), true, groupName, id);
            else group = groupRepository.findByOutletIdAndStatusAndGroupNameIgnoreCaseAndBranchIsNull(
                    outlet.getId(), true, groupName);
        }
        if (group != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }


    /* Get all groups of outlets */
    public JsonObject getAllOutletGroups(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<Group> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = groupRepository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());

        } else {
            list = groupRepository.findByOutletIdAndStatusAndBranchIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (Group mGroup : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mGroup.getId());
                response.addProperty("groupName", mGroup.getGroupName());
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

    public JsonObject getGroupById(HttpServletRequest request) {
        Group group = groupRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (group != null) {
            response.addProperty("id", group.getId());
            response.addProperty("groupName", group.getGroupName());
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public JsonObject updateGroup(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject response = new JsonObject();
        Group subgroup = groupRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            if (validateGroup(request.getParameter("groupName").trim(), users.getOutlet(), branch, subgroup.getId())) {
                response.addProperty("message", request.getParameter("groupName").trim()+ " already Created");
                response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                if (subgroup != null) {
                    subgroup.setGroupName(request.getParameter("groupName").trim());
                    subgroup.setUpdatedBy(users.getId());
                    groupRepository.save(subgroup);
                    response.addProperty("message", "Group updated successfully");
                    response.addProperty("responseStatus", HttpStatus.OK.value());
                } else {
                    response.addProperty("message", "Not found");
                    response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            groupLogger.error("updateGroup-> failed to updateGroupProduct" + e);
        }
        return response;
    }

    public JsonObject removeMultipleGroups(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonArray usedArray = new JsonArray();
        JsonArray removedArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        String removeBrandList = request.getParameter("removegrouplist");
        JsonElement removeBrandElements = parser.parse(removeBrandList);
        JsonArray removeDeptJson = removeBrandElements.getAsJsonArray();
        Group group = null;
        List<Product> products = new ArrayList<>();
        if (removeDeptJson.size() > 0) {
            for (JsonElement mList : removeDeptJson) {
                Long object = mList.getAsLong();
                JsonObject removeGroup = new JsonObject();
                JsonObject usedGroup = new JsonObject();
                if (object != 0) {
                    if (branch != null) {
                        group = groupRepository.findByOutletIdAndBranchIdAndIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIdAndGroupIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                    } else {
                        group = groupRepository.findByOutletIdAndBranchIsNullAndIdAndStatus(
                                users.getOutlet().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIsNullAndGroupIdAndStatus(
                                users.getOutlet().getId(), object, true);
                    }
                    if (products != null && products.size() > 0) {
                        usedGroup.addProperty("message", group.getGroupName() + " is used in product ,First delete product");
                        usedGroup.addProperty("name", group.getGroupName());
                        usedArray.add(usedGroup);
                    } else {
                        if (group != null) group.setStatus(false);
                        try {
                            groupRepository.save(group);
                            removeGroup.addProperty("message", group.getGroupName() + " Deleted Successfully");
                            removeGroup.addProperty("name", group.getGroupName());
                            removedArray.add(removeGroup);
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
        finalObject.add("removedGroups", removedArray);
        finalObject.add("usedGroups", usedArray);
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        return finalObject;
    }
}
