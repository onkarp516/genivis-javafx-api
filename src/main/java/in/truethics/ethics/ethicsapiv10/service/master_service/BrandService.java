package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Brand;
import in.truethics.ethics.ethicsapiv10.model.master.Group;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.BrandRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
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
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BrandService {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;

    private static final Logger groupLogger = LogManager.getLogger(BrandService.class);
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UsersRepository usersRepository;

    public Object addBrand(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        if (validateGroup(request.getParameter("brandName").trim(), users.getOutlet(), branch, 0L)) {
            responseObject.setMessage(request.getParameter("brandName").trim() +" already created");
            responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            try {
                Brand brand = new Brand();
                brand.setBrandName(request.getParameter("brandName").trim());
                brand.setBranch(branch);
                brand.setOutlet(users.getOutlet());
                brand.setCreatedBy(users.getId());
                brand.setUpdatedBy(users.getId());
                brand.setStatus(true);
                Brand mGroup = brandRepository.save(brand);
                responseObject.setMessage("Brand created succussfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());
                responseObject.setResponseObject(mGroup.getId().toString());
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                groupLogger.error("addBrand-> failed to addBrand" + e);
                responseObject.setMessage("Internal Server Error");
                responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            } catch (Exception e1) {
                e1.printStackTrace();
                groupLogger.error("addBrand-> failed to addBrand" + e1);
                responseObject.setMessage("Error");
            }
        }
        return responseObject;
    }

    private boolean validateGroup(String brandName, Outlet outlet, Branch branch, Long brandId) {
        Boolean flag = false;
        Brand brand = null;
        if (brand != null) {
            if (brandId != 0)
                brand = brandRepository.findByOutletIdAndBranchIdAndBrandNameIgnoreCaseAndStatusAndIdNot(
                        outlet.getId(), branch.getId(), brandName, true, brandId);
            else
                brand = brandRepository.findByOutletIdAndBranchIdAndBrandNameIgnoreCaseAndStatus(outlet.getId(), branch.getId(), brandName, true);
        } else {
            if (brandId != 0)
                brand = brandRepository.findByOutletIdAndBrandNameIgnoreCaseAndStatusAndIdNotAndBranchIsNull(
                        outlet.getId(), brandName, true, brandId);
            else
                brand = brandRepository.findByOutletIdAndBrandNameIgnoreCaseAndStatusAndBranchIsNull(outlet.getId(), brandName, true);
        }
        if (brand != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    /* Get  all groups of Outlets */
    public JsonObject getAllBrands(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<Brand> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = brandRepository.findByOutletIdAndStatusAndBranchId(outletId, true, users.getBranch().getId());
        } else {
            list = brandRepository.findByOutletIdAndStatusAndBranchIsNull(outletId, true);
        }
        if (list.size() > 0) {
            for (Brand mBrand : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mBrand.getId());
                response.addProperty("brandName", mBrand.getBrandName());
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

    /* get Group by id */
    public JsonObject getBrand(HttpServletRequest request) {
        Brand brand = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        if (brand != null) {
            response.addProperty("id", brand.getId());
            response.addProperty("brandName", brand.getBrandName());
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public JsonObject updateBrand(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Brand brand = new Brand();
        JsonObject response = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Brand mBrand = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        if (validateGroup(request.getParameter("brandName").trim(), users.getOutlet(), branch, mBrand.getId())) {
            response.addProperty("message", request.getParameter("brandName").trim() +" already created");
            response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            try {
                if (mBrand != null) {
                    mBrand.setBrandName(request.getParameter("brandName").trim());
                    mBrand.setUpdatedBy(users.getId());
                    brandRepository.save(mBrand);
                    response.addProperty("message", "Brand updated successfully");
                    response.addProperty("responseStatus", HttpStatus.OK.value());
                } else {
                    response.addProperty("message", "Not found");
                    response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
                }


            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                groupLogger.error("updateBrand -> failed to updateBrand" + e);
                responseObject.setMessage("Internal Server Error");
                responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            } catch (Exception e1) {
                e1.printStackTrace();
                groupLogger.error("updateBrand -> failed to updateBrand" + e1);
                responseObject.setMessage("Error");
            }
        }
        return response;
    }

    public Object DTBrand(Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        Long outletId = users.getOutlet().getId();
        Integer from = Integer.parseInt(request.get("from"));
        Integer to = Integer.parseInt(request.get("to"));
        String searchText = request.get("searchText");

        GenericDatatable genericDatatable = new GenericDatatable();
        List<Group> groupList = new ArrayList<>();
        try {
            String query = "SELECT * FROM `brand_tbl` WHERE brand_tbl.outlet_id='" + outletId + "' AND brand_tbl.status=1";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND (brand_name LIKE '%" + searchText + "%')";
            }

            String jsonToStr = request.get("sort");
            System.out.println(" sort " + jsonToStr);
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
                System.out.println(" ORDER BY " + jsonObject.get("colId").getAsString());
                String sortBy = jsonObject.get("colId").getAsString();
                query = query + " ORDER BY " + sortBy;
                if (jsonObject.get("isAsc").getAsBoolean() == true) {
                    query = query + " ASC";
                } else {
                    query = query + " DESC";
                }
            } else {
                query = query + " ORDER BY brand_name ASC";
            }
            String query1 = query;
            Integer endLimit = to - from;
            query = query + " LIMIT " + from + ", " + endLimit;
            System.out.println("query " + query);

            Query q = entityManager.createNativeQuery(query, Group.class);
            Query q1 = entityManager.createNativeQuery(query1, Group.class);

            groupList = q.getResultList();
            System.out.println("Limit total rows " + groupList.size());

            List<Group> groupArrayList = new ArrayList<>();
            groupArrayList = q1.getResultList();
            System.out.println("total rows " + groupArrayList.size());

//            genericDatatable.setRows(groupList);
//            genericDatatable.setTotalRows(groupArrayList.size());
        } catch (Exception e) {
            e.printStackTrace();
            groupLogger.error("DTBrand -> failed to DTBrand" + e);
            System.out.println("Exception " + e.getMessage());

//            genericDatatable.setRows(groupList);
//            genericDatatable.setTotalRows(0);
        }
        return genericDatatable;
    }

    public JsonObject removeMultipleBrands(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonArray usedArray = new JsonArray();
        JsonArray removedArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        String removeBrandList = request.getParameter("removebrandlist");
        JsonElement removeBrandElements = parser.parse(removeBrandList);
        JsonArray removeDeptJson = removeBrandElements.getAsJsonArray();
        Brand brand = null;
        List<Product> products = new ArrayList<>();
        if (removeDeptJson.size() > 0) {
            for (JsonElement mList : removeDeptJson) {
                Long object = mList.getAsLong();
                JsonObject removeBrand = new JsonObject();
                JsonObject usedBrand = new JsonObject();
                if (object != 0) {
                    if (branch != null) {
                        brand = brandRepository.findByOutletIdAndBranchIdAndIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIdAndBrandIdAndStatus(
                                users.getOutlet().getId(), users.getBranch().getId(), object, true);
                    } else {
                        brand = brandRepository.findByOutletIdAndBranchIsNullAndIdAndStatus(
                                users.getOutlet().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIsNullAndBrandIdAndStatus(
                                users.getOutlet().getId(), object, true);
                    }
                    if (products != null && products.size() > 0) {
                        usedBrand.addProperty("message", brand.getBrandName() + " " +
                                "is used in product,First delete product");
                        usedBrand.addProperty("name", brand.getBrandName());
                        usedArray.add(usedBrand);
                    } else {
                        if (brand != null) brand.setStatus(false);
                        try {
                            brandRepository.save(brand);
                            removeBrand.addProperty("message", brand.getBrandName() + " Deleted Successfully");
                            removeBrand.addProperty("name", brand.getBrandName());
                            removedArray.add(removeBrand);
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
        finalObject.add("removedBrands", removedArray);
        finalObject.add("usedBrands", usedArray);
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        return finalObject;
    }

}
