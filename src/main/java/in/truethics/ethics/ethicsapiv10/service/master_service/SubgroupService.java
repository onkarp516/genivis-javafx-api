package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.master.Subgroup;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.GroupRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.SubgroupRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubgroupService {

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private SubgroupRepository subgroupRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    private ProductRepository productRepository;

    public Object addSubgroup(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        try {
            if (users.getBranch() != null) {
                branch = users.getBranch();
            }
            if (validateSubGroup(request.getParameter("subgroupName").trim(), users.getOutlet(), branch, 0L)) {
                responseObject.setMessage(request.getParameter("subgroupName").trim() +" already Created");
                responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
            }else {
                Subgroup subgroup = new Subgroup();
                subgroup.setSubgroupName(request.getParameter("subgroupName"));
                subgroup.setBranch(users.getBranch());
                subgroup.setOutlet(users.getOutlet());
                subgroup.setCreatedBy(users.getId());
                subgroup.setUpdatedBy(users.getId());
                subgroup.setStatus(true);
                Subgroup mSubgroup = subgroupRepository.save(subgroup);
                responseObject.setMessage("Subgroup Created Successfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());
                responseObject.setResponseObject(mSubgroup.getId().toString());
            }
        } catch (Exception e) {
            responseObject.setMessage("Internal server Error");
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return responseObject;
    }

    private boolean validateSubGroup(String subgroupName, Outlet outlet, Branch branch, Long id) {
        Boolean flag = false;
        Subgroup subgroup = null;
        if (branch != null) {
            if(id != 0)
            subgroup = subgroupRepository.findByOutletIdAndBranchIdAndStatusAndSubgroupNameIgnoreCaseAndIdNot(outlet.getId(), branch.getId(),
                    true, subgroupName, id);
            else subgroup = subgroupRepository.findByOutletIdAndBranchIdAndStatusAndSubgroupNameIgnoreCase(outlet.getId(), branch.getId(),
                    true, subgroupName);
        } else {
            if(id != 0)
            subgroup = subgroupRepository.findByOutletIdAndStatusAndSubgroupNameIgnoreCaseAndIdNotAndBranchIsNull(
                    outlet.getId(), true, subgroupName, id);
            else subgroup = subgroupRepository.findByOutletIdAndStatusAndSubgroupNameIgnoreCaseAndBranchIsNull(
                    outlet.getId(), true, subgroupName);
        }
        if (subgroup != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    /* get all subgroups of group */
    public JsonObject getAllSubGroups(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<Subgroup> list = new ArrayList<>();
        list = subgroupRepository.findByStatus(true);
        if (list.size() > 0) {
            for (Subgroup mSubgroup : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mSubgroup.getId());
                response.addProperty("subgroupName", mSubgroup.getSubgroupName());
                result.add(response);
            }
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);

        } else {
            res.addProperty("message", "empty list");
            res.add("responseObject", result);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return res;
    }

    /* Get all subgroups of outlets */
    public JsonObject getAllOutletSubGroups(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<Subgroup> list = new ArrayList<>();
        list = subgroupRepository.findByOutletIdAndStatus(outletId, true);
        if (list.size() > 0) {
            for (Subgroup mGroup : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mGroup.getId());
                response.addProperty("subgroupName", mGroup.getSubgroupName());
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

    public JsonObject updateSubgroup(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Subgroup subgroup = subgroupRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        try {
            if (users.getBranch() != null) {
                branch = users.getBranch();
            }
            if (validateSubGroup(request.getParameter("subgroupName").trim(), users.getOutlet(), branch, subgroup.getId())) {
                response.addProperty("message", request.getParameter("subgroupName").trim() +" already Created");
                response.addProperty("responseStatus",HttpStatus.CONFLICT.value());
            } else {

                if (subgroup != null) {
                    subgroup.setSubgroupName(request.getParameter("subgroupName"));
                    subgroup.setUpdatedBy(users.getId());
                    subgroupRepository.save(subgroup);
                    response.addProperty("message", "Subgroup updated successfully");
                    response.addProperty("responseStatus", HttpStatus.OK.value());
                } else {
                    response.addProperty("message", "Not found");
                    response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message","Internal server Error");
            response.addProperty("responseStatus",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject getSubGroupById(HttpServletRequest request) {
        Subgroup subgroup = subgroupRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (subgroup != null) {
            response.addProperty("id", subgroup.getId());
            response.addProperty("subgroupName", subgroup.getSubgroupName());
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public JsonObject removeMultipleSubGroups(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonArray usedArray = new JsonArray();
        JsonArray removedArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        String removeBrandList = request.getParameter("removesubgrouplist");
        JsonElement removeBrandElements = parser.parse(removeBrandList);
        JsonArray removeDeptJson = removeBrandElements.getAsJsonArray();
        Subgroup subgroup = null;
        List<Product> products = new ArrayList<>();
        if (removeDeptJson.size() > 0) {
            for (JsonElement mList : removeDeptJson) {
                Long object = mList.getAsLong();
                JsonObject removeGroup = new JsonObject();
                JsonObject usedGroup = new JsonObject();
                if (object != 0) {
                    if (branch != null) {
                        subgroup = subgroupRepository.findByOutletIdAndBranchIdAndIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIdAndSubgroupIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                    } else {
                        subgroup = subgroupRepository.findByOutletIdAndBranchIsNullAndIdAndStatus(
                                users.getOutlet().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIsNullAndSubgroupIdAndStatus(
                                users.getOutlet().getId(), object, true);
                    }
                    if (products != null && products.size() > 0) {
                        usedGroup.addProperty("message", subgroup.getSubgroupName() + " is used in product ,First delete product");
                        usedGroup.addProperty("name", subgroup.getSubgroupName());
                        usedArray.add(usedGroup);
                    } else {
                        if (subgroup != null) subgroup.setStatus(false);
                        try {
                            subgroupRepository.save(subgroup);
                            removeGroup.addProperty("message", subgroup.getSubgroupName() + " Deleted Successfully");
                            removeGroup.addProperty("name", subgroup.getSubgroupName());
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
        finalObject.add("removedsubGroups", removedArray);
        finalObject.add("usedsubGroups", usedArray);
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        return finalObject;
    }
}
