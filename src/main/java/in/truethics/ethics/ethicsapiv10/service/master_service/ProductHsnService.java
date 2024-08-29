package in.truethics.ethics.ethicsapiv10.service.master_service;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.GenericDTData;
import in.truethics.ethics.ethicsapiv10.common.UnitConversion;
import in.truethics.ethics.ethicsapiv10.dto.masterdto.HsnDTO;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ProductHsnRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductHsnService {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private ProductHsnRepository repository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;

    @Autowired
    private ProductUnitRepository productUnitRepository;
    private static final Logger productHsnLogger = LoggerFactory.getLogger(ProductHsnService.class);

    public Object createHsn(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseMessage = new ResponseMessage();
        Branch branch = null;
        String hsnNo = request.getParameter("hsnNumber");
        ProductHsn findhsn = repository.findByOutletIdAndHsnNumberAndStatus(users.getOutlet().getId(), hsnNo, true);
        if (findhsn == null) {
            try {
                if (users.getBranch() != null) branch = users.getBranch();
                ProductHsn productHsn = new ProductHsn();
                productHsn.setBranch(branch);
                productHsn.setOutlet(users.getOutlet());
                productHsn.setHsnNumber(hsnNo);
             /*   productHsn.setIgst(Double.parseDouble(request.getParameter("igst") != null && !request.getParameter("igst").isEmpty() ? request.getParameter("igst") : String.valueOf(0.0)));
                productHsn.setCgst(Double.parseDouble(request.getParameter("cgst") != null && !request.getParameter("cgst").isEmpty() ? request.getParameter("cgst") : String.valueOf(0.0)));
                productHsn.setSgst(Double.parseDouble(request.getParameter("sgst") != null && !request.getParameter("sgst").isEmpty() ? request.getParameter("sgst") : String.valueOf(0.0)));*/
                productHsn.setStatus(true);
                if (paramMap.containsKey("description")) {
                    productHsn.setDescription(request.getParameter("description"));
                } else {
                    productHsn.setDescription("");
                }
                productHsn.setType(request.getParameter("type"));
                ProductHsn mHsn = repository.save(productHsn);
                responseMessage.setMessage("HSN created successfully");
                responseMessage.setResponseObject(mHsn.getId().toString());
                responseMessage.setResponseStatus(HttpStatus.OK.value());

            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                productHsnLogger.error("createHsn -> failed to createHSN" + e);
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Internal Server Error");
                e.getMessage();

            } catch (Exception e1) {
                e1.printStackTrace();
                productHsnLogger.error("createHsn -> failed to createHSN" + e1);
                e1.getMessage();
            }
        } else {
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseMessage.setMessage("HSN already Created");
        }
        return responseMessage;
    }

    /*private boolean validateHsn(String hsnNumber, Outlet outlet, Branch branch) {
        Boolean flag = false;
        ProductHsn productHsn = null;
        if (branch != null) {
            productHsn = repository.findByOutletIdAndBranchIdAndHsnNumberAndStatus(outlet.getId(), branch.getId(), hsnNumber, true);
        } else {
            productHsn = repository.findByOutletIdAndHsnNumberAndStatus(outlet.getId(), hsnNumber, true);
        }
        if (productHsn != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }*/


    /* Get All Hsn of outlet */
    public JsonObject getHsn(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<ProductHsn> hsnList = new ArrayList<>();
        if (users.getBranch() != null) {
            hsnList = repository.findByOutletIdAndStatusAndBranchId(users.getOutlet().getId(), true, users.getBranch().getId());
        } else {
            hsnList = repository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        if (hsnList.size() > 0) {
            for (ProductHsn mHsn : hsnList) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mHsn.getId());
                response.addProperty("hsnno", mHsn.getHsnNumber());
                response.addProperty("hsndesc", mHsn.getDescription() != null ? mHsn.getDescription() : "");
                /*response.addProperty("igst", mHsn.getIgst());
                response.addProperty("cgst", mHsn.getCgst());
                response.addProperty("sgst", mHsn.getSgst());*/
                response.addProperty("type", mHsn.getType());
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

    //for pagination testing of HSN  start
    public Object getHsnTest(@RequestBody Map<String, String> request, HttpServletRequest req) {
        ResponseMessage responseMessage = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        Integer pageNo = Integer.parseInt(request.get("pageNo"));   //input 1
        Integer pageSize = Integer.parseInt(request.get("pageSize"));      //input 2
        String searchText = request.get("searchText");         //input 3

        List<ProductHsn> hsnList = new ArrayList<>();
        List<ProductHsn> productHsnArrayList = new ArrayList<>();
        List<HsnDTO> hsnDTOList = new ArrayList<>();
        GenericDTData genericData = new GenericDTData();
        try {
            String query = "SELECT * FROM product_hsn_tbl WHERE status=1";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND hsn_number LIKE '%" + searchText + "%'";
            }

            String jsonToStr = request.get("sort");       //input 4
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") &&   //input 5
                    jsonObject.get("colId").toString() != null) {
                System.out.println(" ORDER BY " + jsonObject.get("colId").toString());
                String sortBy = jsonObject.get("colId").toString();
                query = query + " ORDER BY " + sortBy;
                if (jsonObject.get("isAsc").getAsBoolean()) {
                    query = query + " ASC";
                } else {
                    query = query + " DESC";
                }
            } else {
                query = query + " ORDER BY id ASC";
            }
            String query1 = query;
//            1-1 = 0 * 10 = 0 LIMIT 0,10
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            System.out.println("query " + query);
            Query q = entityManager.createNativeQuery(query, ProductHsn.class);

            hsnList = q.getResultList();

            Query q1 = entityManager.createNativeQuery(query1, ProductHsn.class);
            productHsnArrayList = q1.getResultList();
//            98 / 10
            Integer total_pages = (productHsnArrayList.size() / pageSize);
            if ((productHsnArrayList.size() % pageSize) > 0) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);

            for (ProductHsn hsnListView : hsnList) {
                hsnDTOList.add(convertToDTO(hsnListView));
            }
            System.out.println("Limit total rows " + hsnDTOList.size());
            GenericDatatable<HsnDTO> data = new GenericDatatable<>(hsnDTOList, productHsnArrayList.size(),
                    pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            genericData.setRows(hsnDTOList);
            genericData.setTotalRows(0);
        }

        return responseMessage;

    }

    //End of HSN pagination testing
    //start of DTO for hsn list
    private HsnDTO convertToDTO(ProductHsn hsnList) {
        HsnDTO hsnDTO = new HsnDTO();
        hsnDTO.setId(hsnList.getId());
        hsnDTO.setHsnNumber(hsnList.getHsnNumber());
        hsnDTO.setDescription(hsnList.getDescription());
        hsnDTO.setType(hsnList.getType());

        return hsnDTO;
    }

    //End of DTO for hsn list
    public JsonObject getHsnbyId(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        ProductHsn mHsn = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (mHsn != null) {
            response.addProperty("id", mHsn.getId());
            response.addProperty("hsnno", mHsn.getHsnNumber());
            response.addProperty("hsndesc", mHsn.getDescription());
            /*response.addProperty("igst", mHsn.getIgst());
            response.addProperty("cgst", mHsn.getCgst());
            response.addProperty("sgst", mHsn.getSgst());*/
            response.addProperty("type", mHsn.getType());
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }

    public JsonObject updateHsn(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ProductHsn productHsn = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        try {
            productHsn.setHsnNumber(request.getParameter("hsnNumber"));
            productHsn.setType(request.getParameter("type"));
           /* productHsn.setIgst(Double.parseDouble(request.getParameter("igst") != null ? request.getParameter("igst") : String.valueOf(0.0)));
            productHsn.setCgst(Double.parseDouble(request.getParameter("cgst") != null ? request.getParameter("cgst") : String.valueOf(0.0)));
            productHsn.setSgst(Double.parseDouble(request.getParameter("sgst") != null ? request.getParameter("sgst") : String.valueOf(0.0)));*/
            productHsn.setUpdatedBy(users.getId());
            if (paramMap.containsKey("description")) productHsn.setDescription(request.getParameter("description"));
            else {
                productHsn.setDescription("");
            }
            repository.save(productHsn);
            response.addProperty("message", "HSN updated successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("responseObject", productHsn.getId());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            productHsnLogger.error("updateHsn -> failed to updateHSN" + e);
            response.addProperty("message", "error");
            response.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
            System.out.println(e.getMessage());
            e.getMessage();

        } catch (Exception e1) {
            e1.printStackTrace();
            productHsnLogger.error("updateHsn -> failed to updateHSN" + e1);
            System.out.println(e1.getMessage());
        }
        return response;
    }

    public JsonObject validateHSN(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        ProductHsn productHsn = null;
        if (users.getBranch() != null) {
            productHsn = repository.findByOutletIdAndBranchIdAndHsnNumberAndStatus(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("hsnNumber"), true);
        } else {
            productHsn = repository.findByOutletIdAndHsnNumberAndStatus(users.getOutlet().getId(), request.getParameter("hsnNumber"), true);
        }
        JsonObject result = new JsonObject();
        if (productHsn != null) {
            result.addProperty("message", "duplicate HSN number");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            result.addProperty("message", "new HSN Number");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return result;
    }

    public JsonObject validateHSNUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        ProductHsn productHsn = null;
        Long hsnId = Long.parseLong(request.getParameter("id"));
        if (users.getBranch() != null) {
            productHsn = repository.findByOutletIdAndBranchIdAndHsnNumberAndStatusAndIdNot(
                    users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("hsnNumber"), true, hsnId);
        } else {
            productHsn = repository.findByOutletIdAndHsnNumberAndStatusAndIdNotAndBranchIsNull(
                    users.getOutlet().getId(), request.getParameter("hsnNumber"), true, hsnId);
        }
        JsonObject result = new JsonObject();
        if (productHsn != null && hsnId != productHsn.getId()) {
            result.addProperty("message", "duplicate HSN number");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            result.addProperty("message", "new HSN Number");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return result;
    }

    public JsonObject producthsnDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        ProductHsn productHsn = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);

        count = productUnitRepository.findByProductHsnTranx(productHsn.getId(), true);

        try {
            if (count == 0) {
                productHsn.setStatus(false);
                repository.save(productHsn);
                jsonObject.addProperty("message", "HSN deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());

            } else {
                jsonObject.addProperty("message", "Hsn is used in product,First delete Product");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject searchHsn(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        JsonArray resultsArray = new JsonArray();
        try {
            String searchTerm = request.getParameter("search_term");

            String query = "SELECT * FROM genivis_pharma_db.product_hsn_tbl " +
                    "WHERE hsn_number LIKE '%" + searchTerm + "%' OR " +
                    "description LIKE '%" + searchTerm + "%' OR " +
                    "type LIKE '%" + searchTerm + "%'";
            Query nativeQuery = entityManager.createNativeQuery(query, ProductHsn.class);
            List<ProductHsn> resultList = nativeQuery.getResultList();


            for (ProductHsn productHsn : resultList) {
                JsonObject productJson = new JsonObject();

                productJson.addProperty("id", productHsn.getId());
                productJson.addProperty("hsnno", productHsn.getHsnNumber());
                productJson.addProperty("hsndesc", productHsn.getDescription() != null ? productHsn.getDescription() : "");
                productJson.addProperty("type", productHsn.getType());
//                productJson.addProperty("hsn_number", productHsn.getHsnNumber());
//                productJson.addProperty("description", productHsn.getDescription());
//                productJson.addProperty("type", productHsn.getType());

                resultsArray.add(productJson);
            }
            response.addProperty("message", "success");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("responseObject", resultsArray);

        } catch (Exception e) {
            response.addProperty("message", "empty list");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("responseObject", resultsArray);
        }

        return response;
    }
}
