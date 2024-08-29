package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.FindCatlog;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Category;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CategoryRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;

    @Autowired
    private FindCatlog findCatlog;

    @Autowired
    private ProductRepository productRepository;
    private static final Logger categoryLogger = LoggerFactory.getLogger(CategoryService.class);

    public Object createCategory(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        try {
            if (users.getBranch() != null) {
                branch = users.getBranch();
            }
            if (validateCategory(request.getParameter("categoryName").trim(), users.getOutlet(), branch, 0L)) {
                responseObject.setMessage(request.getParameter("categoryName").trim()+ " already created");
                responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
            } else {
                Category category = new Category();
                category.setCategoryName(request.getParameter("categoryName").trim());
                category.setBranch(branch);
                category.setOutlet(users.getOutlet());
                category.setCreatedBy(users.getId());
                category.setUpdatedBy(users.getId());
                category.setStatus(true);
                Category mCategory = categoryRepository.save(category);
                responseObject.setMessage("Category created successfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());
                responseObject.setResponseObject(mCategory.getId().toString());
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            categoryLogger.error("createCategory -> failed to createCategory" + e);
            responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
            responseObject.setMessage("Already Exist");
        } catch (Exception e) {
            e.printStackTrace();
            categoryLogger.error("createCategory -> failed to createCategory" + e);
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Internal Server Error");
        }
        return responseObject;
    }

    private boolean validateCategory(String categoryName, Outlet outlet, Branch branch, Long id) {
        Boolean flag = false;
        Category category = null;
        if (branch != null) {
            if (id != 0)
                category = categoryRepository.findByOutletIdAndBranchIdAndStatusAndCategoryNameIgnoreCaseAndIdNot(
                        outlet.getId(), branch.getId(), true, categoryName, id);
            else
                category = categoryRepository.findByOutletIdAndBranchIdAndStatusAndCategoryNameIgnoreCase(outlet.getId(), branch.getId(), true, categoryName);
        } else {
            if (id != 0)
                category = categoryRepository.findByOutletIdAndStatusAndCategoryNameIgnoreCaseAndIdNotAndBranchIsNull(
                        outlet.getId(), true, categoryName, id);
            else category = categoryRepository.findByOutletIdAndStatusAndCategoryNameIgnoreCaseAndBranchIsNull(
                    outlet.getId(), true, categoryName);
        }
        if (category != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    public JsonObject getCategory(HttpServletRequest request) {
        Category category = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (category != null) {
            response.addProperty("id", category.getId());
            response.addProperty("categoryName", category.getCategoryName());
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public Object updateCategory(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Category category = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (users.getBranch() != null) branch = users.getBranch();
        try {
            if (validateCategory(request.getParameter("categoryName").trim(), users.getOutlet(), branch, category.getId())) {
                responseObject.setMessage(request.getParameter("categoryName").trim()+ " already created");
                responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
            } else {

                if (category != null) {
                    category.setCategoryName(request.getParameter("categoryName").trim());
                    category.setUpdatedBy(users.getId());
                    category.setUpdatedBy(users.getId());
                    categoryRepository.save(category);
                    responseObject.setMessage("Category update successfully");
                    responseObject.setResponseStatus(HttpStatus.OK.value());
                } else {
                    responseObject.setMessage("Not Found");
                    responseObject.setResponseStatus(HttpStatus.NOT_FOUND.value());
                }
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            categoryLogger.error("updateCategory -> failed to updateCategory " + e);
            responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
            responseObject.setMessage("Already Exist");
        } catch (Exception e) {
            e.printStackTrace();
            categoryLogger.error("updateCategory -> failed to updateCategory " + e);
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Internal Server Error");
        }
        return responseObject;
    }

    /* Get  all Categories of Outlets */
    public JsonObject getAllOutletCategories(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<Category> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = categoryRepository.findByOutletIdAndBranchIdAndStatus(outletId, users.getBranch().getId(), true);
        } else {

            list = categoryRepository.findByOutletIdAndStatusAndBranchIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (Category mCategory : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mCategory.getId());
                response.addProperty("categoryName", mCategory.getCategoryName());
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


    public JsonObject removeMultipleCategory(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonArray usedArray = new JsonArray();
        JsonArray removedArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        String removeCategoryList = request.getParameter("removecategorylist");
        JsonElement removeCategoryElement = parser.parse(removeCategoryList);
        JsonArray removeDeptJson = removeCategoryElement.getAsJsonArray();
        Category category = null;
        List<Product> products = new ArrayList<>();
        if (removeDeptJson.size() > 0) {
            for (JsonElement mList : removeDeptJson) {
                Long object = mList.getAsLong();
                JsonObject removeCategory = new JsonObject();
                JsonObject usedCateogry = new JsonObject();
                if (object != 0) {
                    if (branch != null) {
                        category = categoryRepository.findByOutletIdAndBranchIdAndIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIdAndCategoryIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                    } else {
                        category = categoryRepository.findByOutletIdAndBranchIsNullAndIdAndStatus(
                                users.getOutlet().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIsNullAndCategoryIdAndStatus(
                                users.getOutlet().getId(), object, true);
                    }
                    if (products != null && products.size() > 0) {
                        usedCateogry.addProperty("message", category.getCategoryName() + " is used in product ,First delete product");
                        usedCateogry.addProperty("name", category.getCategoryName());
                        usedArray.add(usedCateogry);
                    } else {
                        if (category != null) category.setStatus(false);
                        try {
                            categoryRepository.save(category);
                            removeCategory.addProperty("message", category.getCategoryName() + " Deleted Successfully");
                            removeCategory.addProperty("name", category.getCategoryName());
                            removedArray.add(removeCategory);
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
