package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.FindCatlog;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.master.Subcategory;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.SubcategoryRepository;
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
public class SubcategoryService {


    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    private FindCatlog findCatlog;

    @Autowired
    private ProductRepository productRepository;
    private static final Logger subcategoryLogger = LoggerFactory.getLogger(SubcategoryService.class);

    public Object createSubcategory(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        try {
            if (users.getBranch() != null) {
                branch = users.getBranch();
            }
            if (validateSubCategory(request.getParameter("subCategoryName").trim(), users.getOutlet(), branch, 0L)) {
                responseObject.setMessage(request.getParameter("subCategoryName").trim()+" already created ");
                responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
            } else {
                Subcategory subcategory = new Subcategory();
                subcategory.setSubcategoryName(request.getParameter("subCategoryName").trim());
                subcategory.setBranch(branch);
                subcategory.setOutlet(users.getOutlet());
                subcategory.setCreatedBy(users.getId());
                subcategory.setUpdatedBy(users.getId());
                subcategory.setStatus(true);
                Subcategory mSubcategory = subcategoryRepository.save(subcategory);
                responseObject.setMessage("SubCategory created successfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());
                responseObject.setResponseObject(mSubcategory.getId().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            subcategoryLogger.error("createSubcategory-> failed to createSubcategory" + e);
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Internal Server Error");
        }
        return responseObject;
    }

    private boolean validateSubCategory(String subCategoryName, Outlet outlet, Branch branch, Long id) {
        Boolean flag = false;
        Subcategory subcategory = null;
        if (branch != null) {
            if (id != 0)
                subcategory = subcategoryRepository.findByOutletIdAndBranchIdAndStatusAndSubcategoryNameIgnoreCaseAndIdNot(
                        outlet.getId(), branch.getId(), true, subCategoryName, id);
            else
                subcategory = subcategoryRepository.findByOutletIdAndBranchIdAndStatusAndSubcategoryNameIgnoreCase(outlet.getId(), branch.getId(), true, subCategoryName);
        } else {
            if(id != 0)
            subcategory = subcategoryRepository.findByOutletIdAndStatusAndSubcategoryNameIgnoreCaseAndIdNotAndBranchIsNull(
                    outlet.getId(), true, subCategoryName, id);
            else subcategory = subcategoryRepository.findByOutletIdAndStatusAndSubcategoryNameIgnoreCaseAndBranchIsNull(
                    outlet.getId(), true, subCategoryName);
        }
        if (subcategory != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    /* Get  all SubCategories of Outlets */
    public JsonObject getAllOutletSubCategories(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<Subcategory> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = subcategoryRepository.findByOutletIdAndBranchIdAndStatus(outletId, users.getBranch().getId(), true);
        } else {
            list = subcategoryRepository.findByOutletIdAndStatusAndBranchIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (Subcategory mGroup : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mGroup.getId());
                response.addProperty("subcategoryName", mGroup.getSubcategoryName());
                result.add(response);
            }
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        } else {
            res.addProperty("message", "Empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject getSubCategory(HttpServletRequest request) {
        Subcategory subcategory = subcategoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (subcategory != null) {
            response.addProperty("id", subcategory.getId());
            response.addProperty("subcategoryName", subcategory.getSubcategoryName());
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public JsonObject updateSubcategory(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Subcategory subcategory = subcategoryRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        try {
            if (users.getBranch() != null) {
                branch = users.getBranch();
            }
            if (validateSubCategory(request.getParameter("subCategoryName").trim(), users.getOutlet(), branch, subcategory.getId())) {
                responseObject.addProperty("message", request.getParameter("subCategoryName").trim()+" already created ");
                responseObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {

                try {
                    subcategory.setSubcategoryName(request.getParameter("subCategoryName").trim());
                    subcategory.setUpdatedBy(users.getId());
                    Subcategory mSubcategory = subcategoryRepository.save(subcategory);
                    responseObject.addProperty("message", "SubCategory updated successfully");
                    responseObject.addProperty("responseStatus", HttpStatus.OK.value());
                } catch (Exception e) {
                    e.printStackTrace();
                    subcategoryLogger.error("updateSubcategory -> failed to updateSubcategory" + e);
                    responseObject.addProperty("message", "error");
                    responseObject.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            subcategoryLogger.error("updateSubcategory-> failed to updateSubcategory" + e);
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.addProperty("message", "Internal Server Error");
        }
        return responseObject;
    }


    public JsonObject removeMultipleSubcategories(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonArray usedArray = new JsonArray();
        JsonArray removedArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        String removeSubCategoryList = request.getParameter("removesubcategorylist");
        JsonElement removeSubCateogryElement = parser.parse(removeSubCategoryList);
        JsonArray removeDeptJson = removeSubCateogryElement.getAsJsonArray();
        Subcategory subCategory = null;
        List<Product> products = new ArrayList<>();
        if (removeDeptJson.size() > 0) {
            for (JsonElement mList : removeDeptJson) {
                Long object = mList.getAsLong();
                JsonObject removeSubCateogry = new JsonObject();
                JsonObject usedSubCategory = new JsonObject();
                if (object != 0) {
                    if (branch != null) {
                        subCategory = subcategoryRepository.findByOutletIdAndBranchIdAndIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIdAndSubcategoryIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                    } else {
                        subCategory = subcategoryRepository.findByOutletIdAndBranchIsNullAndIdAndStatus(
                                users.getOutlet().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIsNullAndSubcategoryIdAndStatus(
                                users.getOutlet().getId(), object, true);
                    }
                    if (products != null && products.size() > 0) {
                        usedSubCategory.addProperty("message", subCategory.getSubcategoryName() + " is used in product ,First delete product");
                        usedSubCategory.addProperty("name", subCategory.getSubcategoryName());
                        usedArray.add(usedSubCategory);
                    } else {
                        if (subCategory != null) subCategory.setStatus(false);
                        try {
                            subcategoryRepository.save(subCategory);
                            removeSubCateogry.addProperty("message", subCategory.getSubcategoryName() + " Deleted Successfully");
                            removeSubCateogry.addProperty("name", subCategory.getSubcategoryName());
                            removedArray.add(removeSubCateogry);
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
        finalObject.add("removedCategory", removedArray);
        finalObject.add("usedCategory", usedArray);
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        return finalObject;
    }
}

