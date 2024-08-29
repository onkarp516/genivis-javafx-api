package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.ProductDTO;
import in.truethics.ethics.ethicsapiv10.dto.ProductUnitDTO;
import in.truethics.ethics.ethicsapiv10.dto.PurchaseProductData;
import in.truethics.ethics.ethicsapiv10.dto.masterdto.FRProductDTO;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageService;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.*;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.product_barcode.ProductBarcodeRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceDetailsUnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.Constants;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static in.truethics.ethics.ethicsapiv10.util.Constants.decimalFormat;

@Service
public class ProductService {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    FiscalYearRepository fiscalYearRepository;
    @Autowired
    private ProductHsnRepository productHsnRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private DrugTypeRepository drugTypeRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private TaxMasterRepository taxMasterRepository;
    @Autowired
    private PackingMasterRepository packingMasterRepository;
    @Autowired
    private ProductBarcodeRepository barcodeRepository;
    @Autowired
    private ProductOpeningStocksRepository openingStocksRepository;
    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private FindProduct findProduct;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    private static final Logger productLogger = LogManager.getLogger(ProductService.class);
    @Autowired
    private TranxPurInvoiceDetailsUnitsRepository tranxPurInvoiceDetailsUnitsRepository;
    @Autowired
    private SubcategoryRepository subcategoryRepository;
    @Autowired
    private SubgroupRepository subgroupRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Autowired
    private ProductContentMasterRepository productContentMasterRepository;
    @Autowired
    private ContentMasterRepository contentMasterRepository;

    @Autowired
    private FileStorageService fileStorageService;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Autowired
    private FranchiseMasterRepository franchiseMasterRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${spring.serversource.frurl}")
    private String frUrl;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UnitConversion unitConversion;

    public JsonObject validateProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Long packageId = null;
        String productName = "";
        Product product = null;
        JsonObject result = new JsonObject();
        try {
            if (paramMap.containsKey("packageId") && !request.getParameter("packageId").equalsIgnoreCase("")) {
                packageId = Long.parseLong(request.getParameter("packageId"));
            }
            if (paramMap.containsKey("productName") && !request.getParameter("productName").equalsIgnoreCase(""))
                productName = request.getParameter("productName");
            if (users.getBranch() != null) {
                if (packageId != null) {
                    product = productRepository.findByduplicateProductPKWBR(users.getOutlet().getId(), users.getBranch().getId(), productName, packageId, true);
                } else {
                    product = productRepository.findByduplicateProductPNWBR(users.getOutlet().getId(), users.getBranch().getId(), productName, true);
                }
            } else if (packageId != null) {
                product = productRepository.findByduplicateProductPK(users.getOutlet().getId(), productName, packageId, true);
            } else {
                product = productRepository.findByduplicateProductPN(users.getOutlet().getId(), productName, true);
            }
            if (product != null) {
                result.addProperty("message", "Duplicate Product");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "new product");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Validation:" + exceptionAsString);
        }
        return result;
    }

    private void setProductOpeningStocks(Product newProduct, Users users, double opening_qty, Units unit, PackingMaster packingMaster, Double openingRate, Double valuation) {
        ProductOpeningStocks openingStocks = new ProductOpeningStocks();
        openingStocks.setProduct(newProduct);
        openingStocks.setOpeningQty(opening_qty);
        openingStocks.setUnits(unit);
        openingStocks.setPackingMaster(packingMaster);
        openingStocks.setCreatedBy(users.getId());
        openingStocks.setOpeningStocks(openingRate);
        openingStocks.setOpeningValuation(valuation);
        openingStocks.setStatus(true);
        try {
            openingStocksRepository.save(openingStocks);
        } catch (Exception e) {
            productLogger.error("Error in setProductOpeningStocks()->>" + e.getMessage());
        }
    }

    /* Get Product by id for edit */
    public Object getProductById(HttpServletRequest request) {
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Product mProduct = productRepository.findByIdAndStatus(productId, true);
        ResponseMessage responseMessage = new ResponseMessage();
        ProductDTO pData = new ProductDTO();
        try {
            if (mProduct != null) {
                pData.setId(mProduct.getId());
                pData.setProduct_name(mProduct.getProductName());
                pData.setSearch_code(mProduct.getProductCode());
                pData.setDescription(mProduct.getDescription());
                pData.setIsWarrantyApplicable(mProduct.getIsWarrantyApplicable());
                pData.setWarrantyDays(mProduct.getWarrantyDays());
                pData.setIsSerialNumber(mProduct.getIsSerialNumber());
                pData.setIsBatchNumber(mProduct.getIsBatchNumber());
                pData.setIsInventory(mProduct.getIsInventory());
                pData.setIsBrand(mProduct.getIsBrand());
                pData.setIsGroup(mProduct.getIsGroup());
                pData.setIsCategory(mProduct.getIsCategory());
                pData.setIsSubcategory(mProduct.getIsSubCategory());
                pData.setIsPackage(mProduct.getIsPackage());
                pData.setAlias(mProduct.getAlias());
                List<ProductUnitDTO> list = new ArrayList<>();
                List<ProductUnitPacking> units = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
                for (ProductUnitPacking mUnit : units) {
                    ProductUnitDTO pUnit = new ProductUnitDTO();
                    pUnit.setUnitConversion(mUnit.getUnitConversion());
                    pUnit.setUnitConvMargn(mUnit.getUnitConvMargn());
                    pUnit.setUnitId(mUnit.getUnits().getId());
                    pUnit.setUnitDetailId(mUnit.getId());
                    if (mUnit.getPackingMaster() != null) pUnit.setPackageId(mUnit.getPackingMaster().getId());
                    if (mUnit.getBrand() != null) pUnit.setBrandId(mUnit.getBrand().getId());
                    if (mUnit.getGroup() != null) pUnit.setGroupId(mUnit.getGroup().getId());
                    if (mUnit.getCategory() != null) pUnit.setCategoryId(mUnit.getCategory().getId());
                    if (mUnit.getSubcategory() != null) pUnit.setSubcategoryId(mUnit.getSubcategory().getId());
                    list.add(pUnit);
                }
                pData.setUnits(list);
                responseMessage.setMessage("success");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
                responseMessage.setResponseObject(pData);
            } else {
                responseMessage.setMessage("error");
                responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
                responseMessage.setResponseObject("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            productLogger.error("getProductById-> failed to get ProductById" + e);
            //   productLogger.error("createService -> failed to update Product" + e);
        }
        return responseMessage;
    }

    public JsonObject getProductsOfOutlet(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<Product> productList = new ArrayList<>();
        Long branchId = null;
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
            branchId = users.getBranch().getId();
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        JsonObject finalResult = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Product mProduct : productList) {
            JsonObject mObject = new JsonObject();
            mObject.addProperty("id", mProduct.getId());
            mObject.addProperty("product_name", mProduct.getProductName());
            mObject.addProperty("search_code", mProduct.getProductCode() != null ? mProduct.getProductCode() : "");
            mObject.addProperty("brand", mProduct.getBrand() != null ? mProduct.getBrand().getBrandName() : "");
            mObject.addProperty("packing", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getPackName() : "");
            mObject.addProperty("barcode", mProduct.getBarcodeNo() != null ? mProduct.getBarcodeNo() : "");
            List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(mProduct.getId());
            if (mUnits != null && mUnits.size() > 0)
                mObject.addProperty("unit", mUnits.get(0).getUnits().getUnitName());
            else {
                mObject.addProperty("unit", "PCS");
            }
            if (mProduct.getIsBatchNumber()) {
                TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mProduct.getId());
                if (tranxPurInvoiceDetailsUnits != null) {
                    mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                    mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
                } else {
                    ProductBatchNo productBatchNos = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mProduct.getId(), true);
                    mObject.addProperty("mrp", productBatchNos != null ? productBatchNos.getMrp() : 0.00);
                    mObject.addProperty("sales_rate", productBatchNos != null ? productBatchNos.getSalesRate() : 0.00);
                }
            } else {
                if (mUnits != null && mUnits.size() > 0) {
                    mObject.addProperty("mrp", mUnits.get(0).getMrp() != null ? mUnits.get(0).getMrp() : 0.00);
                    mObject.addProperty("sales_rate", mUnits.get(0).getMinRateA() != null ? mUnits.get(0).getMinRateA() : 0.00);
                }
                mObject.addProperty("unit", "PCS");
                mObject.addProperty("mrp", 0.00);
                mObject.addProperty("sales_rate", 0.00);
            }

            LocalDate currentDate = LocalDate.now();
            /*     fiscal year mapping  */
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
//            System.out.println("Fisc Yesr"+fiscalYear);
            Double freeQty = inventoryCommonPostings.calculateFreeQty(mProduct.getId(), users.getOutlet().getId(), branchId, fiscalYear);
            Double openingStocks = 0.0;
            openingStocks = inventoryCommonPostings.calculateOpening(mProduct.getId(), users.getOutlet().getId(), branchId, fiscalYear);
            mObject.addProperty("opening_stocks", openingStocks);
            Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mProduct.getId(), users.getOutlet().getId(), branchId, fiscalYear);
            Double currentStock = closingStocks + openingStocks + freeQty;
            mObject.addProperty("closing_stocks", currentStock);
            jsonArray.add(mObject);
        }
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("data", jsonArray);
        return finalResult;
    }

    public Object getProductsOfOutletNew(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
        List products = new ArrayList<>();
        List<Product> productList = new ArrayList<>();
        List<ProductDTO> productDTOList = new ArrayList<>();
        Long branchId = null;
        GenericDTData genericDTData = new GenericDTData();
        String query = "";
        try {
            if (searchText.equalsIgnoreCase("")) {

                query = "SELECT distinct(product_tbl.id) FROM product_tbl WHERE status=1 AND outlet_id=" + users.getOutlet().getId();
            } else {
                query = "SELECT distinct(product_tbl.id) FROM product_tbl left join product_content_master_tbl on product_tbl.id=product_content_master_tbl.product_id " +
                        " WHERE product_tbl.status=1 AND product_tbl.outlet_id=" + users.getOutlet().getId();
            }
            if (users.getBranch() != null) {
                query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND product_tbl.branch_id IS NULL";

            }

            if (!searchText.equalsIgnoreCase(""))
                query = query + " AND (product_tbl.product_name LIKE '%" + searchText + "%' OR product_content_master_tbl.content_type LIKE '%" + searchText + "%' OR product_tbl.product_code LIKE '%" + searchText + "%' )";


            query = query + " ORDER BY id desc";  //
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;  //

//            Query q = entityManager.createNativeQuery(query, Product.class);
            //  products = q.getResultList();// limit of 50 product list
            Query q = entityManager.createNativeQuery(query);
            products = q.getResultList();
            String query1 = "SELECT COUNT(product_tbl.id) as totalcount FROM product_tbl WHERE product_tbl.status=? " + "AND product_tbl.outlet_id=?";
            if (users.getBranch() != null) {
                query1 = query1 + " AND product_tbl.branch_id=?";
            } else {
                query1 = query1 + " AND product_tbl.branch_id IS NULL";
            }
            System.out.println("Query=>" + query);
            Query q1 = entityManager.createNativeQuery(query1);
            q1.setParameter(1, true);
            q1.setParameter(2, users.getOutlet().getId());
            if (users.getBranch() != null) q1.setParameter(3, users.getOutlet().getId());
            int totalProducts = ((BigInteger) q1.getSingleResult()).intValue();
            Integer total_pages = (totalProducts / pageSize);
            if ((totalProducts % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (Object mProduct : products) {
                System.out.println("R NO " + Long.parseLong(mProduct.toString()));
                Product productListView = productRepository.findByIdAndStatus(Long.parseLong(mProduct.toString()), true);
                productDTOList.add(convertToDTDTO(productListView, users, branchId));
            }
            GenericDatatable<ProductDTO> data = new GenericDatatable<>(productDTOList, totalProducts, pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Exception:" + exceptionAsString);
            genericDTData.setRows(productDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    private ProductDTO convertToDTDTO(Product productList, Users users, Long branchId) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(productList.getId());
        productDTO.setProduct_name(productList.getProductName());
        productDTO.setSearch_code(productList.getProductCode());
        productDTO.setBrand(productList.getBrand().getBrandName());
        productDTO.setPacking(productList.getPackingMaster() != null ? productList.getPackingMaster().getPackName() : "");
        productDTO.setBarcode(productList.getBarcodeNo() != null ? productList.getBarcodeNo() : "");
       /* productDTO.setFsrmh(productList.getFsrmh());
        productDTO.setFsrai(productList.getFsrai());
        productDTO.setCsrmh(productList.getCsrmh());
        productDTO.setCsrai(productList.getCsrai());
*/
        List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(productList.getId());
        if (mUnits != null && mUnits.size() > 0) productDTO.setUnit(mUnits.get(0).getUnits().getUnitName());
        else {
            productDTO.setUnit("PCS");
        }

        if (productList.getIsBatchNumber()) {
            TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(productList.getId());
            if (tranxPurInvoiceDetailsUnits != null) {
                productDTO.setMrp(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                productDTO.setSales_rate(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
            } else {
                ProductBatchNo productBatchNos = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(productList.getId(), true);
                productDTO.setMrp(productBatchNos != null ? productBatchNos.getMrp() != null ? productBatchNos.getMrp() : 0.0 : 0.00);
                productDTO.setSales_rate(productBatchNos != null ? productBatchNos.getSalesRate() != null ? productBatchNos.getSalesRate() : 0.0 : 0.00);
                productDTO.setPurchase_rate(productBatchNos != null ? productBatchNos.getPurchaseRate() != null ? productBatchNos.getPurchaseRate() : 0.0 : 0.0);

            }
        } else {
            if (mUnits != null && mUnits.size() > 0) {
                productDTO.setMrp(mUnits.get(0).getMrp() != null ? mUnits.get(0).getMrp() : 0.00);
                productDTO.setSales_rate(mUnits.get(0).getMinRateA() != null ? mUnits.get(0).getMinRateA() : 0.00);
                productDTO.setPurchase_rate(mUnits.get(0).getPurchaseRate() != null ? mUnits.get(0).getPurchaseRate() : 0.0);
            } else {
                productDTO.setUnit("PCS");
                productDTO.setMrp(0.00);
                productDTO.setSales_rate(0.00);
            }
        }
        LocalDate currentDate = LocalDate.now();
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        Double freeQty = inventoryCommonPostings.calculateFreeQty(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
        Double openingStocks = 0.0;
        openingStocks = inventoryCommonPostings.calculateOpening(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
        productDTO.setOpening_stocks(openingStocks);
        Double closingStocks = inventoryCommonPostings.getClosingStockProduct(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
        Double currentStock = closingStocks + openingStocks + freeQty;
        productDTO.setClosing_stocks(currentStock);
        return productDTO;

    }

    //Product List API for product-minimum level stock
    public Object getProductsOfOutletNew1(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");

        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        if (request.containsKey("end_date") && request.containsKey("start_date")) {
            endDate = request.get("end_date");
            endDatep = LocalDate.parse(endDate);
            startDate = request.get("start_date");
            startDatep = LocalDate.parse(startDate);
        } else {
            List<Object[]> list = new ArrayList<>();
            list = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatus();
            System.out.println("list" + list);
            Object obj[] = list.get(0);
            System.out.println("start Date111:" + obj[0].toString());
            System.out.println("end Date:" + obj[1].toString());
            startDatep = LocalDate.parse(obj[0].toString());   //start date of fiscal year  2023-04-01
            endDatep = LocalDate.parse(obj[1].toString());      //end date of fiscal year  2024-03-31
        }
        List products = new ArrayList<>();
        List<Product> productList = new ArrayList<>();
        List<ProductDTO> productDTOList = new ArrayList<>();
        Long branchId = null;
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT id FROM product_tbl WHERE status=1 AND outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";

            }
            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND product_name LIKE '%" + searchText + "%'";
            }

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;  //
            //Query q = entityManager.createNativeQuery(query, Product.class);
            //  products = q.getResultList();// limit of 50 product list
            Query q = entityManager.createNativeQuery(query);
            products = q.getResultList();
            String query1 = "SELECT COUNT(product_tbl.id) as totalcount FROM product_tbl WHERE product_tbl.status=? " + "AND product_tbl.outlet_id=?";
            if (users.getBranch() != null) {
                query1 = query1 + " AND product_tbl.branch_id=?";
            } else {
                query1 = query1 + " AND product_tbl.branch_id IS NULL";
            }
            Query q1 = entityManager.createNativeQuery(query1);
            q1.setParameter(1, true);
            q1.setParameter(2, users.getOutlet().getId());
            if (users.getBranch() != null) q1.setParameter(3, users.getOutlet().getId());
            //    Integer size = (Integer) q1.getSingleResult();
            int totalProducts = ((BigInteger) q1.getSingleResult()).intValue();
            Integer total_pages = (totalProducts / pageSize);
            if ((totalProducts % pageSize > 0)) {
                total_pages = total_pages + 1;
            }

            for (Object mProduct : products) {
                Product productListView = productRepository.findByIdAndStatus(Long.parseLong(mProduct.toString()), true);
                productDTOList.add(convertToDTDTO1(productListView, users, branchId));
            }
            GenericDatatable<ProductDTO> data = new GenericDatatable<>(productDTOList, totalProducts, pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Exception:" + exceptionAsString);
            genericDTData.setRows(productDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    private ProductDTO convertToDTDTO1(Product productList, Users users, Long branchId) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(productList.getId());
        productDTO.setProduct_name(productList.getProductName());
        productDTO.setSearch_code(productList.getProductCode());
        productDTO.setBrand(productList.getBrand().getBrandName());
        productDTO.setPacking(productList.getPackingMaster() != null ? productList.getPackingMaster().getPackName() : "");
        productDTO.setBarcode(productList.getBarcodeNo());
        productDTO.setMinimumStock(productList.getMinStock());
        productDTO.setMaximumStock(productList.getMaxStock());
        productDTO.setCategory(productList.getCategory() != null ? productList.getCategory().getCategoryName() : "");
        productDTO.setGroup(productList.getGroup() != null ? productList.getGroup().getGroupName() : "");
        productDTO.setSubGroup(productList.getSubgroup() != null ? productList.getSubgroup().getSubgroupName() : "");
        productDTO.setHsn(productList.getProductHsn().getHsnNumber());
        productDTO.setTaxType(productList.getTaxType());
        productDTO.setPurchase_rate(productList.getPurchaseRate());
        List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(productList.getId());
        if (mUnits != null && mUnits.size() > 0) {
            productDTO.setUnit(mUnits.get(0).getUnits().getUnitName());
            productDTO.setUnitId(mUnits.get(0).getUnits().getId());
        } else {
            productDTO.setUnit("PCS");
        }

        if (productList.getIsBatchNumber()) {
            TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(productList.getId());
            if (tranxPurInvoiceDetailsUnits != null) {
                productDTO.setMrp(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                productDTO.setSales_rate(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
            } else {
                ProductBatchNo productBatchNos = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(productList.getId(), true);
                productDTO.setMrp(productBatchNos != null ? productBatchNos.getMrp() != null ? productBatchNos.getMrp() : 0.0 : 0.00);
                productDTO.setSales_rate(productBatchNos != null ? productBatchNos.getSalesRate() != null ? productBatchNos.getSalesRate() : 0.0 : 0.00);
                productDTO.setPurchase_rate(productBatchNos != null ? productBatchNos.getPurchaseRate() != null ? productBatchNos.getPurchaseRate() : 0.0 : 0.0);

            }
        } else {
            if (mUnits != null && mUnits.size() > 0) {
                productDTO.setMrp(mUnits.get(0).getMrp() != null ? mUnits.get(0).getMrp() : 0.00);
                productDTO.setSales_rate(mUnits.get(0).getMinRateA() != null ? mUnits.get(0).getMinRateA() : 0.00);
                productDTO.setPurchase_rate(mUnits.get(0).getPurchaseRate() != null ? mUnits.get(0).getPurchaseRate() : 0.0);
            } else {
                productDTO.setUnit("PCS");
                productDTO.setMrp(0.00);
                productDTO.setSales_rate(0.00);
            }
        }
        LocalDate currentDate = LocalDate.now();
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        Double freeQty = inventoryCommonPostings.calculateFreeQty(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
        Double openingStocks = 0.0;
        openingStocks = inventoryCommonPostings.calculateOpening(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);
        productDTO.setOpening_stocks(openingStocks);
        Double closingStocks = inventoryCommonPostings.getClosingStockProduct(productList.getId(), users.getOutlet().getId(), branchId, fiscalYear);

        Double currentStock = closingStocks + openingStocks + freeQty;
        productDTO.setClosing_stocks(currentStock);

        return productDTO;

    }


    /****** new Architecture : added Level A ,Level B , Level C and units in Product ,PK visit *****/
    public JsonArray getUnitBrandsFlavourPackageUnitsCommonNew(Long product_id) {
        JsonArray mLevelArray = new JsonArray();
        Product mProduct = productRepository.findByIdAndStatus(product_id, true);
        List<Long> levelaArray = productUnitRepository.findLevelAIdDistinct(product_id);
        for (Long mLeveA : levelaArray) {
            Long levelAId = null;
            JsonObject levelaJsonObject = new JsonObject();
            LevelA levelA = null;
            if (mLeveA != null) {
                levelA = levelARepository.findByIdAndStatus(mLeveA, true);
                if (levelA != null) {
                    levelAId = levelA.getId();
                    levelaJsonObject.addProperty("value", levelA.getId());
                    levelaJsonObject.addProperty("label", levelA.getLevelName());
                }
            } else {
                levelaJsonObject.addProperty("value", "");
                levelaJsonObject.addProperty("label", "");
            }
            JsonArray levelBArray = new JsonArray();
            List<Long> levelBunits = productUnitRepository.findByProductsLevelB(product_id, mLeveA);
            for (Long mLeveB : levelBunits) {
                Long levelBId = null;
                JsonObject levelbJsonObject = new JsonObject();
                LevelB levelB = null;
                if (mLeveB != null) {
                    levelB = levelBRepository.findByIdAndStatus(mLeveB, true);
                    if (levelB != null) {
                        levelBId = levelB.getId();
                        levelbJsonObject.addProperty("value", levelB.getId());
                        levelbJsonObject.addProperty("label", levelB.getLevelName());
                    }
                } else {
                    levelbJsonObject.addProperty("value", "");
                    levelbJsonObject.addProperty("label", "");
                }
                JsonArray levelCArray = new JsonArray();
                List<Long> levelCunits = productUnitRepository.findByProductsLevelC(product_id, mLeveA, mLeveB);
                for (Long mLeveC : levelCunits) {
                    Long levelCId = null;
                    JsonObject levelcJsonObject = new JsonObject();
                    LevelC levelC = null;
                    if (mLeveC != null) {
                        levelC = levelCRepository.findByIdAndStatus(mLeveC, true);
                        if (levelC != null) {
                            levelCId = levelC.getId();
                            levelcJsonObject.addProperty("value", levelC.getId());
                            levelcJsonObject.addProperty("label", levelC.getLevelName());
                        }
                    } else {
                        levelcJsonObject.addProperty("value", "");
                        levelcJsonObject.addProperty("label", "");
                    }
                    List<Object[]> unitList = productUnitRepository.findUniqueUnitsByProductId(product_id, mLeveA, mLeveB, mLeveC);
                    JsonArray unitArray = new JsonArray();
                    for (int j = 0; j < unitList.size(); j++) {
                        Object[] objects = unitList.get(j);

                        Long unitId = Long.parseLong(objects[0].toString());
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("value", Long.parseLong(objects[0].toString()));
                        jsonObject.addProperty("unitId", Long.parseLong(objects[0].toString()));
                        jsonObject.addProperty("label", objects[1].toString());
                        jsonObject.addProperty("unitName", objects[1].toString());
                        jsonObject.addProperty("unitCode", objects[2].toString());
                        jsonObject.addProperty("unitConversion", objects[3].toString());
                        unitArray.add(jsonObject);
                    }
                    levelcJsonObject.add("unitOpts", unitArray);
                    levelCArray.add(levelcJsonObject);
                }
                levelbJsonObject.add("levelCOpts", levelCArray);
                levelBArray.add(levelbJsonObject);
            }
            levelaJsonObject.add("levelBOpts", levelBArray);
            mLevelArray.add(levelaJsonObject);
        }
        return mLevelArray;
    }

    public Object updateProduct(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        Product product = productRepository.findByIdAndStatus(Long.parseLong(request.getParameter("productId")), true);
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        product.setProductName(request.getParameter("productName").trim());
        if (paramMap.containsKey("productCode")) product.setProductCode(request.getParameter("productCode"));
        else product.setProductCode("");
        if (paramMap.containsKey("productDescription"))
            product.setDescription(request.getParameter("productDescription"));
        else product.setDescription("");
        product.setStatus(true);
        if (paramMap.containsKey("barcodeNo")) product.setBarcodeNo(request.getParameter("barcodeNo"));
        else product.setBarcodeNo("");
        if (paramMap.containsKey("isSerialNo"))
            product.setIsSerialNumber(Boolean.parseBoolean(request.getParameter("isSerialNo")));
        product.setIsBatchNumber(Boolean.parseBoolean(request.getParameter("isBatchNo")));
        product.setIsInventory(Boolean.parseBoolean(request.getParameter("isInventory")));
        if (paramMap.containsKey("isWarranty")) {
            product.setIsWarrantyApplicable(Boolean.parseBoolean(request.getParameter("isWarranty")));
            if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                product.setWarrantyDays(Integer.parseInt(request.getParameter("nodays")));
            } else {
                product.setWarrantyDays(0);
            }
        }
        if (paramMap.containsKey("drugType")) product.setDrugType(request.getParameter("drugType"));
        if (paramMap.containsKey("productType")) product.setProductType(request.getParameter("productType"));
        if (paramMap.containsKey("isCommision"))
            product.setIsCommision(Boolean.parseBoolean(request.getParameter("isCommision")));
        if (paramMap.containsKey("isGVProducts")) {
            product.setIsGVProducts(Boolean.parseBoolean(request.getParameter("isGVProducts")));
            if (Boolean.parseBoolean(request.getParameter("isGVProducts"))) {
                if (paramMap.containsKey("gvOfProducts")) product.setGvOfProducts(request.getParameter("gvOfProducts"));
            }
        }
        product.setCreatedBy(users.getId());
        /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
        if (paramMap.containsKey("shelfId")) product.setShelfId(request.getParameter("shelfId"));
        else product.setShelfId("");
        if (paramMap.containsKey("barcodeSaleQuantity"))
            product.setBarcodeSalesQty(Double.parseDouble(request.getParameter("barcodeSaleQuantity")));
        if (paramMap.containsKey("margin")) product.setMarginPer(Double.parseDouble(request.getParameter("margin")));
        else product.setMarginPer(0.0);
        PackingMaster mPackingMaster = null;
        Group mGroupMaster = null;
        Brand mBrandMaster = null;
        Category mCategoryMaster = null;
        if (paramMap.containsKey("brandId")) {
            mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
            product.setBrand(mBrandMaster);
        } else {
            product.setBrand(null);
        }
        if (paramMap.containsKey("packagingId")) {
            mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
            product.setPackingMaster(mPackingMaster);
        } else {
            product.setPackingMaster(null);
        }
        if (paramMap.containsKey("groupId")) {
            mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
            product.setGroup(mGroupMaster);
        } else {
            product.setGroup(null);
        }
        if (paramMap.containsKey("categoryId")) {
            mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
            product.setCategory(mCategoryMaster);
        } else {
            product.setCategory(null);
        }
        if (paramMap.containsKey("weight")) product.setWeight(Double.parseDouble(request.getParameter("weight")));
        else product.setWeight(0.0);

        if (paramMap.containsKey("weightUnit")) product.setWeightUnit(request.getParameter("weightUnit"));
        else product.setWeightUnit("");
        if (paramMap.containsKey("disPer1"))
            product.setDiscountInPer(Double.parseDouble(request.getParameter("disPer1")));
        else product.setDiscountInPer(0.0);
        if (paramMap.containsKey("hsnNo")) {
            ProductHsn productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
            if (productHsn != null) {
                product.setProductHsn(productHsn);
            }
        }
        if (paramMap.containsKey("tax")) {
            LocalDate applicableDate = null;
            TaxMaster taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
            if (taxMaster != null) {
                product.setTaxMaster(taxMaster);
            }
            if (paramMap.containsKey("taxApplicableDate"))
                product.setApplicableDate(LocalDate.parse(request.getParameter("taxApplicableDate")));
          /* inserting into ProductTax Master to maintain tax information of Product,
            /***** End of inserting into ProductTax Master  *****/
        }
        if (paramMap.containsKey("taxType")) product.setTaxType(request.getParameter("taxType"));
        if (paramMap.containsKey("igst")) product.setIgst(Double.parseDouble(request.getParameter("igst")));
        if (paramMap.containsKey("cgst")) product.setCgst(Double.parseDouble(request.getParameter("cgst")));
        if (paramMap.containsKey("sgst")) product.setSgst(Double.parseDouble(request.getParameter("sgst")));
        if (paramMap.containsKey("minStock")) product.setMinStock(Double.parseDouble(request.getParameter("minStock")));
        if (paramMap.containsKey("maxStock")) product.setMaxStock(Double.parseDouble(request.getParameter("maxStock")));
        /**** END ****/
        try {
            Product newProduct = productRepository.save(product);
            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("mstPackaging");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                LevelA levelA = null; //brand
                LevelB levelB = null;//group
                LevelC levelC = null;//category
                /**** LevelA Master ****/
                if (!object.get("levela_id").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("levela_id").getAsLong(), true);
                }
                JsonArray leveBArray = object.get("levelb").getAsJsonArray();
                for (JsonElement mLevelB : leveBArray) {
                    JsonObject mLevelBAsJsonObject = mLevelB.getAsJsonObject();
                    /**** LevelB Master ****/
                    if (!mLevelBAsJsonObject.get("levelb_id").getAsString().equalsIgnoreCase("")) {
                        levelB = levelBRepository.findByIdAndStatus(mLevelBAsJsonObject.get("levelb_id").getAsLong(), true);
                    }
                    JsonArray levelCArray = mLevelBAsJsonObject.get("levelc").getAsJsonArray();
                    for (JsonElement mLevelC : levelCArray) {
                        JsonObject mLevelCObject = mLevelC.getAsJsonObject();
                        /**** LevelC Master ****/
                        if (!mLevelCObject.get("levelc_id").getAsString().equalsIgnoreCase("")) {
                            levelC = levelCRepository.findByIdAndStatus(mLevelCObject.get("levelc_id").getAsLong(), true);
                        }
                        JsonArray unitsArray = mLevelCObject.get("units").getAsJsonArray();
                        for (JsonElement mUnitsList : unitsArray) {
                            ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                            JsonObject mUnitObject = mUnitsList.getAsJsonObject();
                            Long details_id = mUnitObject.get("details_id").getAsLong();
                            if (details_id != 0) {
                                productUnitPacking = productUnitRepository.findByIdAndStatus(details_id, true);
                            } else {
                                productUnitPacking = new ProductUnitPacking();
                                productUnitPacking.setStatus(true);
                            }
                            Units unit = unitsRepository.findByIdAndStatus(mUnitObject.get("unit_id").getAsLong(), true);
                            productUnitPacking.setUnits(unit);
                            productUnitPacking.setUnitConversion(mUnitObject.get("unit_conv").getAsDouble());
                            productUnitPacking.setUnitConvMargn(mUnitObject.get("unit_marg").getAsDouble());
                            if (mUnitObject.get("isNegativeStocks").getAsInt() == 1) {
                                productUnitPacking.setIsNegativeStocks(true);
                            } else {
                                productUnitPacking.setIsNegativeStocks(false);
                            }
                            productUnitPacking.setMrp(mUnitObject.get("mrp").getAsDouble());
                            productUnitPacking.setPurchaseRate(mUnitObject.get("purchase_rate").getAsDouble());
                            productUnitPacking.setMinRateA(mUnitObject.get("min_rate_a").getAsDouble());//sales Rate
                            productUnitPacking.setMinRateB(mUnitObject.get("min_rate_b").getAsDouble());
                            productUnitPacking.setMinRateC(mUnitObject.get("min_rate_c").getAsDouble());
                            productUnitPacking.setStatus(true);
                            productUnitPacking.setProduct(newProduct);
                            productUnitPacking.setCreatedBy(users.getId());
                            /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                            productUnitPacking.setMinQty(mUnitObject.get("min_qty").getAsDouble());
                            productUnitPacking.setMaxQty(mUnitObject.get("max_qty").getAsDouble());
                            productUnitPacking.setLevelA(levelA);
                            productUnitPacking.setLevelB(levelB);
                            productUnitPacking.setLevelC(levelC);
                            try {
                                productUnitRepository.save(productUnitPacking);
                            } catch (Exception e) {
                                System.out.println("Exception:" + e.getMessage());
                                productLogger.error("Error in Product Creation:" + e.getMessage());
                            }
                            /****** Inserting Product Opening Stocks ******/
                            JsonArray mBatchJsonArray = mUnitObject.getAsJsonArray("batchList");
                            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
                            for (JsonElement mBatchElement : mBatchJsonArray) {
                                ProductBatchNo productBatchNo = null;
                                JsonObject mBatchJsonObject = mBatchElement.getAsJsonObject();
                                ProductBatchNo mproductBatchNo = null;
                                Long id = mBatchJsonObject.get("id").getAsLong();
                                if (mBatchJsonObject.get("isOpeningbatch").getAsBoolean()) {
                                    String batch_id = mBatchJsonObject.get("batch_id").getAsString();
                                    if (batch_id.equalsIgnoreCase("")) {
                                        mproductBatchNo = new ProductBatchNo();
                                        mproductBatchNo.setStatus(true);
                                        if (fiscalYear != null) mproductBatchNo.setFiscalYear(fiscalYear);
                                    } else
                                        mproductBatchNo = productBatchNoRepository.findByIdAndStatus(Long.parseLong(batch_id), true);
                                    if (mBatchJsonObject.has("b_no"))
                                        mproductBatchNo.setBatchNo(mBatchJsonObject.get("b_no").getAsString());
                                    if (mBatchJsonObject.has("b_mrp"))
                                        mproductBatchNo.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                                    if (mBatchJsonObject.has("b_purchase_rate"))
                                        mproductBatchNo.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_sale_rate"))
                                        mproductBatchNo.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    mproductBatchNo.setMinRateA(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_free_qty"))
                                        mproductBatchNo.setFreeQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                                    if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                        mproductBatchNo.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                                    if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                        mproductBatchNo.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                                    mproductBatchNo.setUnits(unit);
                                    productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                                }
                                try {
                                    ProductOpeningStocks newOpeningStock = null;
                                    if (id != 0) {
                                        newOpeningStock = openingStocksRepository.findByIdAndStatus(id, true);
                                    } else {
                                        newOpeningStock = new ProductOpeningStocks();
                                        newOpeningStock.setProduct(newProduct);
                                        newOpeningStock.setUnits(unit);
                                        newOpeningStock.setPackingMaster(mPackingMaster);
                                        newOpeningStock.setBrand(mBrandMaster);
                                        newOpeningStock.setGroup(mGroupMaster);
                                        newOpeningStock.setCategory(mCategoryMaster);
                                        newOpeningStock.setLevelA(levelA);
                                        newOpeningStock.setLevelB(levelB);
                                        newOpeningStock.setLevelC(levelC);
                                        newOpeningStock.setStatus(true);
                                        if (fiscalYear != null) newOpeningStock.setFiscalYear(fiscalYear);
                                    }
                                    newOpeningStock.setOpeningStocks(Double.parseDouble(mBatchJsonObject.get("opening_qty").getAsString()));
                                    newOpeningStock.setProductBatchNo(productBatchNo);
                                    if (mBatchJsonObject.has("b_free_qty"))
                                        newOpeningStock.setFreeOpeningQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                                    if (mBatchJsonObject.has("b_mrp"))
                                        newOpeningStock.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                                    if (mBatchJsonObject.has("b_purchase_rate"))
                                        newOpeningStock.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_sale_rate"))
                                        newOpeningStock.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                        newOpeningStock.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                                    if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                        newOpeningStock.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                                    newOpeningStock.setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                                    try {
                                        openingStocksRepository.save(newOpeningStock);
                                    } catch (Exception e) {
                                        productLogger.error("Exception:" + e.getMessage());
                                    }
                                } catch (Exception e) {
                                  /*  responseObject.setMessage("Error in Product Creation");
                                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());*/
                                    e.printStackTrace();
                                    System.out.println("Exception " + e.getMessage());
                                }
                            }
                        }//Units
                    }//Level C
                }//Level B
            }//Level A

            /*** delete Product units while updating product ***/
            String delJsonStr = request.getParameter("rowDelDetailsIds");
            JsonElement delElement = parser.parse(delJsonStr);
            JsonArray delArray = delElement.getAsJsonArray();
            for (JsonElement mDelList : delArray) {
                JsonObject mDelObject = mDelList.getAsJsonObject();
                Long delId = mDelObject.get("del_id").getAsLong();
                ProductUnitPacking mUnitDel = productUnitRepository.findByIdAndStatus(delId, true);
                try {
                    mUnitDel.setStatus(false);
                    productUnitRepository.save(mUnitDel);
                } catch (Exception e) {
                    productLogger.error("Exception in Product Delete:" + e.getMessage());
                    System.out.println("Exception e:" + e.getMessage());
                }
            }
            responseMessage.setMessage("Product updated Successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            productLogger.error("updateProduct-> failed to updateProduct" + e);
            System.out.println(e.getMessage());
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseMessage.setMessage("Internal Server Error");
        }
        return responseMessage;
    }

    /* get Product Level, Multilvel Architecture PK Visit *****/
    public JsonObject getProductLevelsNew(Product mProduct, Users users) {
        JsonObject levelAList = new JsonObject();
        /***** Level A Array of Product ****/
        JsonArray levelaArray = new JsonArray();
        List<LevelA> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = levelARepository.findByOutletIdAndStatusAndBranchId(users.getOutlet().getId(), true, users.getBranch().getId());
        } else {
            list = levelARepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        for (LevelA mLevelA : list) {
            JsonObject levelAJsonObject = new JsonObject();
            levelAJsonObject.addProperty("id", mLevelA.getId());
            levelAJsonObject.addProperty("levelName", mLevelA.getLevelName());
            levelaArray.add(levelAJsonObject);
        }
        levelAList.add("levelALst", levelaArray);
        /***** LevelB Array of Product ****/
        JsonArray levelBJsonArray = new JsonArray();
        List<LevelB> levelBList = new ArrayList<>();
        if (users.getBranch() != null) {
            levelBList = levelBRepository.findByOutletIdAndStatusAndBranchId(users.getOutlet().getId(), true, users.getBranch().getId());
        } else {
            levelBList = levelBRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        for (LevelB mLevelB : levelBList) {
            JsonObject levelBJsonObject = new JsonObject();
            levelBJsonObject.addProperty("id", mLevelB.getId());
            levelBJsonObject.addProperty("levelName", mLevelB.getLevelName());
            levelBJsonArray.add(levelBJsonObject);
        }
        levelAList.add("levelBLst", levelBJsonArray);

        /***** LevelC Array of Product ****/
        JsonArray levelCJsonArray = new JsonArray();
        List<LevelC> levelCList = new ArrayList<>();
        if (users.getBranch() != null) {
            levelCList = levelCRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            levelCList = levelCRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        for (LevelC mLevelC : levelCList) {
            JsonObject levelCJsonObject = new JsonObject();
            levelCJsonObject.addProperty("id", mLevelC.getId());
            levelCJsonObject.addProperty("levelName", mLevelC.getLevelName());
            levelCJsonArray.add(levelCJsonObject);
        }
        levelAList.add("levelCLst", levelCJsonArray);
        return levelAList;
    }

    public JsonArray getUnitPackageCommon(Long product_id, Boolean isPakage) {
        JsonArray packArray = new JsonArray();
        int mCount = 0, mCounts = 0;
        /* for No Packaging */
        if (isPakage == false) {

            JsonObject mObject = new JsonObject();
            mObject.addProperty("id", "");
            mObject.addProperty("pack_name", "");
            /*   product units list*/
            List<ProductUnitPacking> productUnitPackings = productUnitRepository.findByProductId(product_id);
            JsonArray unitArray = new JsonArray();
            for (ProductUnitPacking mUnits : productUnitPackings) {
                JsonObject mUnitsObj = new JsonObject();
                mCount++;
                mUnitsObj.addProperty("units_id", mUnits.getUnits().getId());
                mUnitsObj.addProperty("details_id", mUnits.getId());
                mUnitsObj.addProperty("unit_name", mUnits.getUnits().getUnitName());
                mUnitsObj.addProperty("unit_conv", mUnits.getUnitConversion());
                mUnitsObj.addProperty("unit_marg", mUnits.getUnitConvMargn());
              /*  mUnitsObj.addProperty("disc_per", mUnits.getDiscountInPer());
                mUnitsObj.addProperty("min_qty", mUnits.getMinQty());
                mUnitsObj.addProperty("max_qty", mUnits.getMaxQty());*/
                mUnitsObj.addProperty("mrp", mUnits.getMrp());
                if (mUnits.getIsNegativeStocks() != null)
                    mUnitsObj.addProperty("isNegativeStocks", mUnits.getIsNegativeStocks() == true ? 1 : 0);
                else mUnitsObj.addProperty("isNegativeStocks", 0);
                mUnitsObj.addProperty("purchase_rate", mUnits.getPurchaseRate());
                mUnitsObj.addProperty("rateA", mUnits.getMinRateA() != null ? mUnits.getMinRateA() : 0);
                mUnitsObj.addProperty("rateB", mUnits.getMinRateB() != null ? mUnits.getMinRateB() : 0);
                mUnitsObj.addProperty("rateC", mUnits.getMinRateC() != null ? mUnits.getMinRateC() : 0);
                // mUnitsObj.addProperty("sales_rate", mUnits.getSalesRate());
               /* mUnitsObj.addProperty("min_sales_rate", mUnits.getMinSalesRate());
                mUnitsObj.addProperty("disc_amt", mUnits.getDiscountInAmt());
                mUnitsObj.addProperty("opening_qty", mUnits.getOpeningQty());
                mUnitsObj.addProperty("opening_valution", mUnits.getOpeningValution());*/
                mUnitsObj.addProperty("hsnId", mUnits.getProductHsn() != null ? mUnits.getProductHsn().getId() : null);
                mUnitsObj.addProperty("taxMasterId", mUnits.getTaxMaster() != null ? mUnits.getTaxMaster().getId() : null);
                mUnitsObj.addProperty("applicableDate", mUnits.getTaxApplicableDate() != null ? mUnits.getTaxApplicableDate().toString() : "");
                mUnitsObj.addProperty("igst", mUnits.getTaxMaster().getIgst());
                mUnitsObj.addProperty("cgst", mUnits.getTaxMaster().getCgst());
                mUnitsObj.addProperty("sgst", mUnits.getTaxMaster().getSgst());
                unitArray.add(mUnitsObj);
            }
            if (mCount == 1) {
                mObject.addProperty("is_multi_unit", false);
            } else {
                mObject.addProperty("is_multi_unit", true);
            }
            mObject.addProperty("unitCount", mCount);
            mObject.add("units", unitArray);
            packArray.add(mObject);
        } else {

            /*  product packing List*/
            List<Long> unitPackingList = new ArrayList<>();
            unitPackingList = productUnitRepository.findProductIdDistinct(product_id);

            if (unitPackingList != null && unitPackingList.size() > 0) {
                for (Long mPack : unitPackingList) {
                    mCounts = 0;
                    JsonObject mObject = new JsonObject();
                    PackingMaster mPacking = null;
                    List<ProductUnitPacking> productUnitPackings = new ArrayList<>();
                    if (mPack != null) {
                        mPacking = packingMasterRepository.findById(mPack).get();
                        mObject.addProperty("id", mPacking.getId());
                        mObject.addProperty("pack_name", mPacking.getPackName());
                        productUnitPackings = productUnitRepository.findByProductIdAndPackingMasterId(product_id, mPack);
                    } else {
                        mObject.addProperty("id", "");
                        mObject.addProperty("pack_name", "");
                        productUnitPackings = productUnitRepository.findByProductIdAndPackingIsNULL(product_id);
                    }
                    /*product units list*/

                    JsonArray unitArray = new JsonArray();
                    for (ProductUnitPacking mUnits : productUnitPackings) {

                        JsonObject mUnitsObj = new JsonObject();
                        mUnitsObj.addProperty("units_id", mUnits.getUnits().getId());
                        mUnitsObj.addProperty("details_id", mUnits.getId());
                        mUnitsObj.addProperty("unit_name", mUnits.getUnits().getUnitName());
                        mUnitsObj.addProperty("unit_conv", mUnits.getUnitConversion());
                        mUnitsObj.addProperty("unit_marg", mUnits.getUnitConvMargn());
//                        mUnitsObj.addProperty("max_qty", mUnits.getMaxQty());
//                        mUnitsObj.addProperty("min_qty", mUnits.getMinQty());
//                        mUnitsObj.addProperty("disc_per", mUnits.getDiscountInPer());
                        mUnitsObj.addProperty("mrp", mUnits.getMrp());
                        mUnitsObj.addProperty("purchase_rate", mUnits.getPurchaseRate());
//                        mUnitsObj.addProperty("sales_rate", mUnits.getSalesRate());
                        mUnitsObj.addProperty("rateA", mUnits.getMinRateA() != null ? mUnits.getMinRateA() : 0);
                        mUnitsObj.addProperty("rateB", mUnits.getMinRateB() != null ? mUnits.getMinRateB() : 0);
                        mUnitsObj.addProperty("rateC", mUnits.getMinRateC() != null ? mUnits.getMinRateC() : 0);
                        unitArray.add(mUnitsObj);
                    }
                    if (mCounts == 1) {
                        mObject.addProperty("is_multi_unit", false);
                    } else {
                        mObject.addProperty("is_multi_unit", true);
                    }
                    mObject.addProperty("unitCount", mCounts);
                    /*    end of product units list*/
                    mObject.add("units", unitArray);
                    packArray.add(mObject);


                }
            }
        }
        return packArray;
    }

    public JsonObject getProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<Product> productList = new ArrayList<>();
        if (users.getBranch() != null) {
            productList = productRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            productList = productRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        List<PurchaseProductData> list = new ArrayList<>();
        JsonArray array = new JsonArray();
        JsonObject result = new JsonObject();
        for (Product mProduct : productList) {
            List<ProductUnitPacking> units = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
            JsonObject response = new JsonObject();
            response.addProperty("productName", mProduct.getProductName());
            /* get barcode of product */
            ProductBarcode productBarcode = barcodeRepository.findByProductIdAndOutletIdAndStatus(mProduct.getId(), users.getOutlet().getId(), true);
            if (productBarcode != null) {
                response.addProperty("product_barcode", productBarcode.getBarcodeUniqueCode());
            }
            PurchaseProductData pData = new PurchaseProductData();
            response.addProperty("id", mProduct.getId());
            response.addProperty("productCode", mProduct.getProductCode());
            response.addProperty("isBatchNo", mProduct.getIsBatchNumber() != null ? mProduct.getIsBatchNumber() : false);
            response.addProperty("isInventory", mProduct.getIsInventory());
            array.add(response);
        }
        result.addProperty("messege", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.add("responseObject", array);
        return result;
    }

    public JsonObject getUnitsPackingsFlavours(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long product_id = Long.parseLong(request.getParameter("product_id"));
        Product mProduct = productRepository.findByIdAndStatus(product_id, true);
        JsonArray packArray = new JsonArray();
        JsonObject finalResult = new JsonObject();
        JsonObject result = new JsonObject();
        /*result.addProperty("isBrand", mProduct.getIsBrand());
        result.addProperty("isGroup", mProduct.getIsGroup());
        result.addProperty("isCategory", mProduct.getIsCategory());
        result.addProperty("isSubcategory", mProduct.getIsSubCategory());
        result.addProperty("isPackage", mProduct.getIsPackage());*/
        packArray = getUnitBrandsFlavourPackageUnitsCommonNew(product_id);
        result.add("lst_packages", packArray);
        /* End of  product packing List */
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("responseObject", result);
        return finalResult;
    }

    public JsonObject deleteProductList(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        String removerProductList = request.getParameter("id");
        Long object = Long.valueOf(removerProductList);
        if (object != 0) {
            List<ProductOpeningStocks> productOpeningStocks = openingStocksRepository.findByProductIdAndStatus(object, true);
            if (productOpeningStocks != null && productOpeningStocks.size() > 0) {
                jsonObject.addProperty("message", "Product with opening stocks can't delete ");
            } else {
                List<InventoryDetailsPostings> inventoryList = inventoryDetailsPostingsRepository.findByProductIdAndStatus(object, true);
//                double sumOfCr = inventoryDetailsPostingsRepository.getSumOfCrOrDrByProductId(object, "CR");
//                double sumOfDr = inventoryDetailsPostingsRepository.getSumOfCrOrDrByProductId(object, "DR");
//                double result = sumOfCr - sumOfDr;
                if (inventoryList != null && inventoryList.size() > 0) {
//                if (result > 0) {
                    jsonObject.addProperty("message", "Product is used in transaction ,first delete transaction");
                } else {
                    Product product = productRepository.findByIdAndStatus(object, true);
                    if (product != null) product.setStatus(false);
                    try {
                        productRepository.save(product);
                        jsonObject.addProperty("message", "Product deleted successfully");
                        jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Exception:" + e.getMessage());
                        e.getMessage();
                        e.printStackTrace();
                    }
                }
            }
        }
        return jsonObject;
    }

    /***** Multilevel Architecture PK Visit *****/
    public Object createNewProduct(MultipartHttpServletRequest request) {
        Product product = new Product();
        Product newProduct = new Product();
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        try {
            if (users.getBranch() != null) branch = users.getBranch();
            product.setBranch(branch);
            product.setOutlet(outlet);
            product.setProductName(request.getParameter("productName").trim());
            if (paramMap.containsKey("productCode") && !request.getParameter("productCode").equalsIgnoreCase(""))
                product.setProductCode(request.getParameter("productCode"));
            else product.setProductCode("");
            if (paramMap.containsKey("productDescription"))
                product.setDescription(request.getParameter("productDescription"));
            product.setStatus(true);
            if (paramMap.containsKey("barcodeNo")) product.setBarcodeNo(request.getParameter("barcodeNo"));
            if (paramMap.containsKey("drugType")) product.setDrugType(request.getParameter("drugType"));
            if (paramMap.containsKey("productType")) product.setProductType(request.getParameter("productType"));
            if (paramMap.containsKey("isCommision"))
                product.setIsCommision(Boolean.parseBoolean(request.getParameter("isCommision")));
            if (paramMap.containsKey("isGVProducts")) {
                product.setIsGVProducts(Boolean.parseBoolean(request.getParameter("isGVProducts")));
                if (Boolean.parseBoolean(request.getParameter("gvOfProducts"))) {
                    if (paramMap.containsKey("gvOfProducts"))
                        product.setGvOfProducts(request.getParameter("gvOfProducts"));
                }
            }
            if (paramMap.containsKey("isSerialNo"))
                product.setIsSerialNumber(Boolean.parseBoolean(request.getParameter("isSerialNo")));
            product.setIsBatchNumber(Boolean.parseBoolean(request.getParameter("isBatchNo")));
            product.setIsInventory(Boolean.parseBoolean(request.getParameter("isInventory")));
            if (paramMap.containsKey("isWarranty")) {
                product.setIsWarrantyApplicable(Boolean.parseBoolean(request.getParameter("isWarranty")));
                if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                    product.setWarrantyDays(Integer.parseInt(request.getParameter("nodays")));
                } else {
                    product.setWarrantyDays(0);
                }
            }
            product.setCreatedBy(users.getId());
            /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
            if (paramMap.containsKey("shelfId")) product.setShelfId(request.getParameter("shelfId"));
            if (paramMap.containsKey("barcodeSaleQuantity"))
                product.setBarcodeSalesQty(Double.parseDouble(request.getParameter("barcodeSaleQuantity")));
            if (paramMap.containsKey("purchaseRate"))
                product.setPurchaseRate(Double.parseDouble(request.getParameter("purchaseRate")));
            if (paramMap.containsKey("margin"))
                product.setMarginPer(Double.parseDouble(request.getParameter("margin")));
            PackingMaster mPackingMaster = null;
            Group mGroupMaster = null;
            Brand mBrandMaster = null;
            Category mCategoryMaster = null;
            if (paramMap.containsKey("brandId")) {
                mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
                product.setBrand(mBrandMaster);
            }
            if (paramMap.containsKey("packagingId")) {
                mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
                product.setPackingMaster(mPackingMaster);
            }
            if (paramMap.containsKey("groupId")) {
                mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
                product.setGroup(mGroupMaster);
            }
            if (paramMap.containsKey("categoryId")) {
                mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
                product.setCategory(mCategoryMaster);
            }
            if (paramMap.containsKey("weight")) product.setWeight(Double.parseDouble(request.getParameter("weight")));
            if (paramMap.containsKey("weightUnit")) product.setWeightUnit(request.getParameter("weightUnit"));
            if (paramMap.containsKey("disPer1"))
                product.setDiscountInPer(Double.parseDouble(request.getParameter("disPer1")));
            if (paramMap.containsKey("hsnNo")) {
                ProductHsn productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
                if (productHsn != null) {
                    product.setProductHsn(productHsn);
                }
            }
            if (paramMap.containsKey("tax")) {
                LocalDate applicableDate = null;
                TaxMaster taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
                if (taxMaster != null) {
                    product.setTaxMaster(taxMaster);
                }
                if (paramMap.containsKey("taxApplicableDate"))
                    product.setApplicableDate(LocalDate.parse(request.getParameter("taxApplicableDate")));
          /* inserting into ProductTax Master to maintain tax information of Product,
            if (applicableDate != null) {
                try {
                    ProductTaxDateMaster productTaxDateMaster = new ProductTaxDateMaster();
                    //   productTaxDateMaster = productTaxDateMasterRepository.findTax();
                    productTaxDateMaster.setProduct(newProduct);
                    productTaxDateMaster.setProductHsn(productUnitPacking.getProductHsn());
                    productTaxDateMaster.setTaxMaster(productUnitPacking.getTaxMaster());
                    productTaxDateMaster.setApplicableDate(productUnitPacking.getTaxApplicableDate());
                    productTaxDateMaster.setStatus(true);
                    productTaxDateMaster.setUpdatedBy(users.getId());
                    productTaxDateMasterRepository.save(productTaxDateMaster);
                } catch (Exception e) {
                    productLogger.error("Error in Product Creation-> ProductTaxDateMaster Creation-> " + e.getMessage());
                }
            }
            /***** End of inserting into ProductTax Master  *****/
            }
            if (paramMap.containsKey("taxType")) product.setTaxType(request.getParameter("taxType"));
            if (paramMap.containsKey("igst")) product.setIgst(Double.parseDouble(request.getParameter("igst")));
            if (paramMap.containsKey("cgst")) product.setCgst(Double.parseDouble(request.getParameter("cgst")));
            if (paramMap.containsKey("sgst")) product.setSgst(Double.parseDouble(request.getParameter("sgst")));
            if (paramMap.containsKey("minStock"))
                product.setMinStock(Double.parseDouble(request.getParameter("minStock")));
            if (paramMap.containsKey("maxStock"))
                product.setMaxStock(Double.parseDouble(request.getParameter("maxStock")));
            newProduct = productRepository.save(product);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Creation:" + exceptionAsString);
        }
        /**** END ****/
        if (paramMap.containsKey("cantentMapData")) {
            insertIntoProductContentMaster(product, request);
//            ``else updateShippingDetails(mLedger, request);
        }
        try {
            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("mstPackaging");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                LevelB levelB = null; //group
                LevelA levelA = null;//brand
                LevelC levelC = null;//Category
                /**** LevelA Master ****/
                if (!object.get("levela_id").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("levela_id").getAsLong(), true);
                }
                JsonArray leveBArray = object.get("levelb").getAsJsonArray();
                for (JsonElement mLevelB : leveBArray) {
                    JsonObject mLevelBAsJsonObject = mLevelB.getAsJsonObject();
                    /**** LevelB Master ****/
                    if (!mLevelBAsJsonObject.get("levelb_id").getAsString().equalsIgnoreCase("")) {
                        levelB = levelBRepository.findByIdAndStatus(mLevelBAsJsonObject.get("levelb_id").getAsLong(), true);
                    }
                    JsonArray levelCArray = mLevelBAsJsonObject.get("levelc").getAsJsonArray();
                    for (JsonElement mLevelC : levelCArray) {
                        JsonObject mLevelCObject = mLevelC.getAsJsonObject();
                        /**** LevelC Master ****/
                        if (!mLevelCObject.get("levelc_id").getAsString().equalsIgnoreCase("")) {
                            levelC = levelCRepository.findByIdAndStatus(mLevelCObject.get("levelc_id").getAsLong(), true);
                        }
                        JsonArray unitsArray = mLevelCObject.get("units").getAsJsonArray();
                        for (JsonElement mUnitsList : unitsArray) {
                            ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                            JsonObject mUnitObject = mUnitsList.getAsJsonObject();
                            Units unit = unitsRepository.findByIdAndStatus(mUnitObject.get("unit_id").getAsLong(), true);
                            productUnitPacking.setUnits(unit);
                            productUnitPacking.setUnitConversion(mUnitObject.get("unit_conv").getAsDouble());
                            productUnitPacking.setUnitConvMargn(mUnitObject.get("unit_marg").getAsDouble());
                            if (mUnitObject.get("isNegativeStocks").getAsInt() == 1) {
                                productUnitPacking.setIsNegativeStocks(true);
                            } else {
                                productUnitPacking.setIsNegativeStocks(false);
                            }
                            productUnitPacking.setMrp(mUnitObject.get("mrp").getAsDouble());
                            productUnitPacking.setPurchaseRate(mUnitObject.get("purchase_rate").getAsDouble());
                            productUnitPacking.setMinMargin(mUnitObject.get("min_margin").getAsDouble());
                            productUnitPacking.setMinRateA(mUnitObject.get("min_rate_a").getAsDouble());//sales Rate
                            productUnitPacking.setMinRateB(mUnitObject.get("min_rate_b").getAsDouble());
                            productUnitPacking.setMinRateC(mUnitObject.get("min_rate_c").getAsDouble());
                            productUnitPacking.setStatus(true);
                            productUnitPacking.setProduct(newProduct);
                            productUnitPacking.setCreatedBy(users.getId());
                            /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                            productUnitPacking.setMinQty(mUnitObject.get("min_qty").getAsDouble());
                            productUnitPacking.setMaxQty(mUnitObject.get("max_qty").getAsDouble());
                            productUnitPacking.setIsRate(mUnitObject.get("is_rate").getAsBoolean());
                            if (mUnitObject.get("is_rate").getAsBoolean() == true) {
                                productUnitPacking.setFsrai(mUnitObject.get("fsrai").getAsDouble());
                                productUnitPacking.setFsrmh(mUnitObject.get("fsrmh").getAsDouble());
                                productUnitPacking.setCsrai(mUnitObject.get("csrai").getAsDouble());
                                productUnitPacking.setCsrmh(mUnitObject.get("csrmh").getAsDouble());
                            } else {
                                productUnitPacking.setFsrai(0.0);
                                productUnitPacking.setFsrmh(0.0);
                                productUnitPacking.setCsrai(0.0);
                                productUnitPacking.setCsrmh(0.0);
                            }

                            productUnitPacking.setLevelA(levelA);
                            productUnitPacking.setLevelB(levelB);
                            productUnitPacking.setLevelC(levelC);
                            productUnitRepository.save(productUnitPacking);
                            /****** Inserting Product Opening Stocks ******/
                            JsonArray mBatchJsonArray = mUnitObject.getAsJsonArray("batchList");
                            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
                            for (JsonElement mBatchElement : mBatchJsonArray) {
                                ProductBatchNo productBatchNo = null;
                                JsonObject mBatchJsonObject = mBatchElement.getAsJsonObject();
                                ProductBatchNo mproductBatchNo = null;
                                Long id = mBatchJsonObject.get("id").getAsLong();
                                if (mBatchJsonObject.get("isOpeningbatch").getAsBoolean()) {

                                    mproductBatchNo = new ProductBatchNo();

                                    if (mBatchJsonObject.has("b_no"))
                                        mproductBatchNo.setBatchNo(mBatchJsonObject.get("b_no").getAsString());
                                    if (mBatchJsonObject.has("b_mrp"))
                                        mproductBatchNo.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                                    if (mBatchJsonObject.has("b_purchase_rate"))
                                        mproductBatchNo.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());

                                    if (mBatchJsonObject.has("b_sale_rate"))
                                        mproductBatchNo.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    mproductBatchNo.setMinRateA(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_free_qty"))
                                        mproductBatchNo.setFreeQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                                    if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                        mproductBatchNo.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                                    if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                        mproductBatchNo.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                                    mproductBatchNo.setStatus(true);
                                    mproductBatchNo.setProduct(product);
                                    mproductBatchNo.setOutlet(outlet);
                                    mproductBatchNo.setBranch(branch);
                                    mproductBatchNo.setUnits(unit);
                                    mproductBatchNo.setQnty(Integer.parseInt(mBatchJsonObject.get("opening_qty").getAsString()));
                                    if (fiscalYear != null) mproductBatchNo.setFiscalYear(fiscalYear);
                                    productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                                }
                                try {
                                    ProductOpeningStocks newOpeningStock = new ProductOpeningStocks();
                                    newOpeningStock.setOpeningStocks(Double.parseDouble(mBatchJsonObject.get("opening_qty").getAsString()));
                                    newOpeningStock.setProduct(newProduct);
                                    newOpeningStock.setUnits(unit);
                                    newOpeningStock.setBranch(branch);
                                    newOpeningStock.setOutlet(outlet);
                                    newOpeningStock.setProductBatchNo(productBatchNo);
                                    if (mBatchJsonObject.has("b_free_qty"))
                                        newOpeningStock.setFreeOpeningQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                                    if (mBatchJsonObject.has("b_mrp"))
                                        newOpeningStock.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                                    if (mBatchJsonObject.has("b_purchase_rate"))
                                        newOpeningStock.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                                    if (mBatchJsonObject.has("b_sale_rate"))
                                        newOpeningStock.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                    newOpeningStock.setLevelA(levelA);
                                    newOpeningStock.setLevelB(levelB);
                                    newOpeningStock.setLevelC(levelC);
                                    newOpeningStock.setStatus(true);
                                    if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                        newOpeningStock.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                                    if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                        newOpeningStock.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                                    newOpeningStock.setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                                    if (fiscalYear != null) newOpeningStock.setFiscalYear(fiscalYear);
                                    openingStocksRepository.save(newOpeningStock);


                                    ///
                                } catch (Exception e) {
                                    StringWriter sw = new StringWriter();
                                    e.printStackTrace(new PrintWriter(sw));
                                    String exceptionAsString = sw.toString();
                                    productLogger.error("Product Creation: Product Opening Stock" + exceptionAsString);
                                    responseObject.setMessage("Error in Product Creation:ProductOpeningStock");
                                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                                }
                            }
                        }//Units
                    }//Level C
                }//Level B
            }//Level A
            responseObject.setMessage("Product Created Successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setData(newProduct.getId().toString());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in create product:" + exceptionAsString);
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Internal Server Error");
        }
        return responseObject;
    }

    private void insertIntoProductContentMaster(Product product, HttpServletRequest request) {
        String strJson = request.getParameter("cantentMapData");
        JsonParser parser = new JsonParser();
        JsonElement productContentElements = parser.parse(strJson);
        JsonArray productContentElementsJson = productContentElements.getAsJsonArray();
        Map<String, String[]> paramMap = request.getParameterMap();
        if (productContentElementsJson.size() > 0) {
            for (JsonElement mList : productContentElementsJson) {
                JsonObject object = mList.getAsJsonObject();

                ProductContentMaster productContent = null;
                if (object.has("id") && !object.get("id").getAsString().isEmpty())
                    productContent = productContentMasterRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
                if (productContent == null)
                    productContent = new ProductContentMaster();
                productContent.setContentPackage(object.get("content_package").getAsString());
                productContent.setContentPower(object.get("content_power").getAsString());
                productContent.setContentType(object.get("contentType").getAsString());
                if (object.has("contentTypeDose") && object.get("contentTypeDose") != null) {
                    productContent.setContentTypeDose(object.get("contentTypeDose").getAsString());
                }
                productContent.setProduct(product);
                productContent.setStatus(true);
                productContentMasterRepository.save(productContent);
            }
        }
        if (paramMap.containsKey("removeContentMapData")) {
            String removeBalanceDetails = request.getParameter("removeContentMapData");
            JsonElement removeBalanceElement = parser.parse(removeBalanceDetails);
            JsonArray removeJsonBalance = removeBalanceElement.getAsJsonArray();
            ProductContentMaster mBalanceDetails = null;
            if (removeJsonBalance.size() > 0) {
                for (JsonElement mList : removeJsonBalance) {
                    Long object = mList.getAsLong();
                    if (object != 0) {
                        mBalanceDetails = productContentMasterRepository.findByIdAndStatus(object, true);
                        if (mBalanceDetails != null) mBalanceDetails.setStatus(false);
                        try {
                            productContentMasterRepository.save(mBalanceDetails);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            productLogger.error("Exception in updateProduct:" + exceptionAsString);
                        }
                    }
                }
            }
        }
    }

    public JsonObject getByIdEdit(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long product_id = Long.parseLong(request.getParameter("productId"));
        Product mProduct = productRepository.findByIdAndStatus(product_id, true);
        JsonObject listObject = new JsonObject();
        JsonObject finalResult = new JsonObject();
        JsonObject result = new JsonObject();
        listObject = getProductLevelsNew(mProduct, users);
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("responseObject", listObject);
        return finalResult;
    }

    public JsonObject getProductByIdEditFlavourNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Product mProduct = productRepository.findByIdAndStatus(productId, true);
        JsonObject result = new JsonObject();
        //   List<ProductUnitPacking> units = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
        JsonObject response = new JsonObject();
        try {
            if (mProduct != null) {
                response.addProperty("productName", mProduct.getProductName());
                response.addProperty("id", mProduct.getId());
                response.addProperty("description", mProduct.getDescription());
                response.addProperty("productCode", mProduct.getProductCode());
                response.addProperty("isBatchNo", mProduct.getIsBatchNumber());
                response.addProperty("isInventory", mProduct.getIsInventory());
                response.addProperty("isSerialNo", mProduct.getIsSerialNumber());
                response.addProperty("barcodeNo", mProduct.getBarcodeNo());
                response.addProperty("shelfId", mProduct.getShelfId());
                response.addProperty("barcodeSalesQty", mProduct.getBarcodeSalesQty());
                response.addProperty("purchaseRate", mProduct.getPurchaseRate());
                response.addProperty("margin", mProduct.getMarginPer());
                response.addProperty("brandId", mProduct.getBrand() != null ? mProduct.getBrand().getId() : null);
                response.addProperty("packagingId", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getId() : null);
                response.addProperty("groupId", mProduct.getGroup() != null ? mProduct.getGroup().getId() : null);
                response.addProperty("categoryId", mProduct.getCategory() != null ? mProduct.getCategory().getId() : null);
                response.addProperty("weight", mProduct.getWeight());
                response.addProperty("weightUnit", mProduct.getWeightUnit());
                response.addProperty("disPer1", mProduct.getDiscountInPer());
                response.addProperty("hsnNo", mProduct.getProductHsn() != null ? mProduct.getProductHsn().getId() : null);
                response.addProperty("tax", mProduct.getTaxMaster() != null ? mProduct.getTaxMaster().getId() : null);
                response.addProperty("taxApplicableDate", mProduct.getApplicableDate() != null ? mProduct.getApplicableDate().toString() : null);
                response.addProperty("taxType", mProduct.getTaxType() != null ? mProduct.getTaxType() : null);
                response.addProperty("igst", mProduct.getIgst() != null ? mProduct.getIgst() : null);
                response.addProperty("cgst", mProduct.getCgst() != null ? mProduct.getCgst() : null);
                response.addProperty("sgst", mProduct.getSgst() != null ? mProduct.getSgst() : null);
                response.addProperty("minStock", mProduct.getMinStock() != null ? mProduct.getMinStock() : 0.0);
                response.addProperty("maxStock", mProduct.getMaxStock() != null ? mProduct.getMaxStock() : 0.0);
                /* getting Level A, Level B, Level C and its Units from Product Id */
                JsonArray unitArray = new JsonArray();
                unitArray = getUnitBrandsFlavourPackageUnitsCommonNewProductEdit(mProduct.getId());
                response.add("mstPackaging", unitArray);
                result.addProperty("messege", "success");
                result.addProperty("responseStatus", HttpStatus.OK.value());
                result.add("responseObject", response);
            } else {
                result.addProperty("messege", "empty");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
                result.add("responseObject", response);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in getProductByIdEditFlavourNew:" + exceptionAsString);
        }
        return result;
    }

    public Object productTransactionsDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject mObject = new JsonObject();
        Long productId = Long.parseLong(request.getParameter("product_id").isEmpty() ? "0" : request.getParameter("product_id"));
        Product product = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            if (users.getBranch() != null) {
                product = productRepository.findByIdAndOutletIdAndBranchIdAndStatus(productId, users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                product = productRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(productId, users.getOutlet().getId(), true);
            }
            if (product != null) {
                mObject.addProperty("min_stocks", product.getMinStock() != null ? product.getMinStock() : 0.0);
                mObject.addProperty("max_stocks", product.getMinStock() != null ? product.getMaxStock() : 0.0);
                ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(product.getId(), true);
                List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(product.getId());
                if (mUnits != null) {
                    mObject.addProperty("min_stocks", mUnits.get(0).getMinQty() != null ? mUnits.get(0).getMinQty() : 0.0);
                    mObject.addProperty("max_stocks", mUnits.get(0).getMaxQty() != null ? mUnits.get(0).getMaxQty() : 0.0);
                }
                if (productBatchNo != null) {
                    if (productBatchNo.getExpiryDate() != null) {
                        mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                    } else {
                        mObject.addProperty("batch_expiry", "");
                    }
                    mObject.addProperty("mrp", productBatchNo.getMrp() != null ? productBatchNo.getMrp() : 0.00);
                    mObject.addProperty("purchase_rate", productBatchNo.getPurchaseRate() != null ? productBatchNo.getPurchaseRate() : 0.00);
                    mObject.addProperty("cost", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0.00);
                } else {
                    if (mUnits != null && mUnits.size() > 0) {
                        mObject.addProperty("purchase_rate", mUnits.get(0).getPurchaseRate() != null ? mUnits.get(0).getPurchaseRate() : 0.0);
                        mObject.addProperty("cost", mUnits.get(0).getCosting() != null ? mUnits.get(0).getCosting() : 0.0);
                    } else {
                        mObject.addProperty("purchase_rate", 0.00);
                        mObject.addProperty("cost", 0.00);
                    }
                }
                mObject.addProperty("productName", product.getProductName() != null ? product.getProductName() : "");
                mObject.addProperty("package", product.getPackingMaster().getPackName());
                mObject.addProperty("brand", product.getBrand().getBrandName() != null ? product.getBrand().getBrandName() : "");
                mObject.addProperty("group", product.getGroup() != null ? product.getGroup().getGroupName() : "");
                mObject.addProperty("subgroup", product.getSubgroup() != null ? product.getSubgroup().getSubgroupName() : "");
                mObject.addProperty("category", product.getCategory() != null ? product.getCategory().getCategoryName() : "");
                mObject.addProperty("hsn", product.getProductHsn() != null ? product.getProductHsn().getHsnNumber() : "");
                mObject.addProperty("tax_type", product.getTaxType() != null ? product.getTaxType() : "");
                mObject.addProperty("tax_per", product.getTaxMaster() != null ? product.getTaxMaster().getIgst() : 0);
                mObject.addProperty("igst", product.getTaxMaster() != null ? product.getTaxMaster().getIgst() : 0);
                mObject.addProperty("cgst", product.getTaxMaster() != null ? product.getTaxMaster().getCgst() : 0);
                mObject.addProperty("sgst", product.getTaxMaster() != null ? product.getTaxMaster().getSgst() : 0);
                mObject.addProperty("margin_per", product.getMarginPer() != null ? product.getMarginPer() : 0);
                mObject.addProperty("shelf_id", product.getShelfId() != null ? product.getShelfId() : "");
                mObject.addProperty("min_stocks", product.getMinStock() != null ? product.getMinStock() : 0);
                mObject.addProperty("max_stocks", product.getMaxStock() != null ? product.getMaxStock() : 0);
                mObject.addProperty("is_batch", product.getIsBatchNumber() != null ? product.getIsBatchNumber().toString() : "");
                mObject.addProperty("is_serial", product.getIsSerialNumber() != null ? product.getIsSerialNumber() : false);

                mObject.addProperty("supplier", "");
                mObject.addProperty("barcode", product.getBarcodeNo() != null ? product.getBarcodeNo() : "");
                TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(product.getId());
                if (tranxPurInvoiceDetailsUnits != null) {
                    mObject.addProperty("supplier", tranxPurInvoiceDetailsUnits.getPurchaseTransaction().getSundryCreditors().getLedgerName());
                }
                ProductUnitPacking unitPacking = productUnitRepository.findByProductIdAndIsRate(product.getId(), true);

                mObject.addProperty("unit", unitPacking.getUnits().getUnitName());
                mObject.addProperty("unit_id", unitPacking.getUnits() != null ? unitPacking.getUnits().getId() : 0);

                mObject.addProperty("fsrmh", unitPacking.getFsrmh() != null ? unitPacking.getFsrmh() : 0.0);
                mObject.addProperty("fsrai", unitPacking.getFsrai() != null ? unitPacking.getFsrai() : 0.0);
                mObject.addProperty("csrmh", unitPacking.getCsrmh() != null ? unitPacking.getCsrmh() : 0.0);
                mObject.addProperty("csrai", unitPacking.getCsrai() != null ? unitPacking.getCsrai() : 0.0);
                mObject.addProperty("mrp", unitPacking.getMrp() != null ? unitPacking.getMrp() : 0);
                mObject.addProperty("purchaseRate", unitPacking.getPurchaseRate() != null ? unitPacking.getPurchaseRate() : 0);
                response.addProperty("message", "success");
                response.addProperty("responseStatus", HttpStatus.OK.value());
                response.add("result", mObject);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Transaction Details:" + exceptionAsString);
        }
        return response;
    }


    public Object productDetailsLevelB(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Long levelAId = null;
        if (!request.getParameter("level_a_id").equalsIgnoreCase(""))
            levelAId = Long.parseLong(request.getParameter("level_a_id"));
        List<Long> levelBArray = productUnitRepository.findLevelBIdDistinct(productId, levelAId);
        JsonArray levelBJsonArray = new JsonArray();
        for (Long mLevelB : levelBArray) {
            if (mLevelB != null) {
                LevelB levelB = levelBRepository.findByIdAndStatus(mLevelB, true);
                if (levelB != null) {
                    JsonObject levelBJsonObject = new JsonObject();
                    levelBJsonObject.addProperty("levelb_id", levelB.getId());
                    levelBJsonObject.addProperty("levelb_name", levelB.getLevelName());
                    levelBJsonArray.add(levelBJsonObject);
                }
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("levelBOpt", levelBJsonArray);
        return response;
    }

    public Object productDetailsLevelC(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Long levelAId = null;
        if (!request.getParameter("level_a_id").equalsIgnoreCase(""))
            levelAId = Long.parseLong(request.getParameter("level_a_id"));
        Long levelBId = null;
        if (!request.getParameter("level_b_id").equalsIgnoreCase(""))
            levelBId = Long.parseLong(request.getParameter("level_b_id"));
        List<Long> levelCArray = productUnitRepository.findLevelCIdDistinct(productId, levelAId, levelBId);
        JsonArray levelCJsonArray = new JsonArray();
        for (Long mLevelC : levelCArray) {
            if (mLevelC != null) {
                LevelC levelC = levelCRepository.findByIdAndStatus(mLevelC, true);
                if (levelC != null) {
                    JsonObject levelCJsonObject = new JsonObject();
                    levelCJsonObject.addProperty("levelc_id", levelC.getId());
                    levelCJsonObject.addProperty("levelc_name", levelC.getLevelName());
                    levelCJsonArray.add(levelCJsonObject);
                }
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("levelCOpt", levelCJsonArray);
        return response;
    }

    public Object productTransactionsListByBarcode(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<Product> productDetails = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String barcode = request.getParameter("barcode");

        String query = "SELECT * FROM `product_tbl` LEFT JOIN packing_master_tbl ON product_tbl.packing_master_id=packing_master_tbl.id" + " WHERE product_tbl.outlet_id=" + users.getOutlet().getId() + " AND product_tbl.status=1";

        if (users.getBranch() != null) {
            query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
        }
        if (!barcode.equalsIgnoreCase("")) {
            query = query + " AND barcode_no=" + barcode;
        }
        System.out.println("query " + query);
        Query q = entityManager.createNativeQuery(query, Product.class);
        productDetails = q.getResultList();
        if (productDetails != null && productDetails.size() > 0) {
            for (Product mDetails : productDetails) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("code", mDetails.getProductCode());
                mObject.addProperty("product_name", mDetails.getProductName());
                mObject.addProperty("packing", mDetails.getPackingMaster().getPackName());
                mObject.addProperty("barcode", mDetails.getBarcodeNo());
                // ProductBatchNo batchNo = productBatchNoRepository.findByIdAndStatus()
                mObject.addProperty("mrp", 0);
                mObject.addProperty("sales_rate", 0);
                mObject.addProperty("current_stock", 0);

                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public Object productUnits(HttpServletRequest request) {
        JsonObject response = new JsonObject();

        try {
            JsonArray jsonArray = new JsonArray();

            String productId = request.getParameter("product_id");
            Long level_a_id = Long.valueOf(request.getParameter("level_a_id"));
            Long level_b_id = request.getParameter("level_b_id").equalsIgnoreCase("null") ? null : Long.valueOf(request.getParameter("level_b_id"));
            Long level_c_id = request.getParameter("level_c_id").equalsIgnoreCase("null") ? null : Long.valueOf(request.getParameter("level_c_id"));
            List<Object[]> unitList = productUnitRepository.findUniqueUnitsByProductId(Long.valueOf(productId), level_a_id, level_b_id, level_c_id);

            for (int i = 0; i < unitList.size(); i++) {
                Object[] objects = unitList.get(i);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("unitId", objects[0].toString());
                jsonObject.addProperty("unitName", objects[1].toString());
                jsonObject.addProperty("unitCode", objects[2].toString());
                jsonObject.addProperty("unitConversion", objects[3].toString());
                jsonArray.add(jsonObject);
            }

            response.add("response", jsonArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            productLogger.error("error in create product:" + e.getMessage());
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonArray getUnitBrandsFlavourPackageUnitsCommonNewProductEdit(Long product_id) {
        JsonArray LevelAJsonArray = new JsonArray();
        List<Long> levelaArray = new ArrayList<>();
        levelaArray = productUnitRepository.findLevelAIdDistinct(product_id);
        for (Long mLeveA : levelaArray) {
            JsonObject levelaJsonObject = new JsonObject();
            LevelA levelA = null;
            if (mLeveA != null) {
                levelA = levelARepository.findByIdAndStatus(mLeveA, true);
                levelaJsonObject.addProperty("levela_id", levelA.getId());
                levelaJsonObject.addProperty("levela_name", levelA.getLevelName());
            } else {
                levelaJsonObject.addProperty("levela_id", "");
                levelaJsonObject.addProperty("levela_name", "");
            }
            List<Long> levelBArray = new ArrayList<>();
            levelBArray = productUnitRepository.findLevelBIdDistinct(product_id, mLeveA);
            JsonArray levelBJsonArray = new JsonArray();
            for (Long mLevelB : levelBArray) {
                JsonObject levelBJsonObject = new JsonObject();
                LevelB levelB = null;
                if (mLevelB != null) {
                    levelB = levelBRepository.findByIdAndStatus(mLevelB, true);
                    levelBJsonObject.addProperty("levelb_id", levelB.getId());
                    levelBJsonObject.addProperty("levelb_name", levelB.getLevelName());
                } else {
                    levelBJsonObject.addProperty("levelb_id", "");
                    levelBJsonObject.addProperty("levelb_name", "");
                }
                JsonArray levelCJsonArray = new JsonArray();
                List<Long> levelCArray = new ArrayList<>();
                levelCArray = productUnitRepository.findLevelCIdDistinct(product_id, mLeveA, mLevelB);
                for (Long mLevelC : levelCArray) {
                    JsonObject levelCJsonObject = new JsonObject();
                    LevelC levelC = null;
                    if (mLevelC != null) {
                        levelC = levelCRepository.findByIdAndStatus(mLevelC, true);
                        levelCJsonObject.addProperty("levelc_id", levelC.getId());
                        levelCJsonObject.addProperty("levelc_name", levelC.getLevelName());
                    } else {
                        levelCJsonObject.addProperty("levelc_id", "");
                        levelCJsonObject.addProperty("levelc_name", "");
                    }
                    JsonArray unitJsonArray = new JsonArray();
                    List<ProductUnitPacking> unitPackingList = new ArrayList<>();
                    unitPackingList = productUnitRepository.findByPackingUnits(product_id, mLeveA, mLevelB, mLevelC);
                    for (ProductUnitPacking mUnits : unitPackingList) {
                        JsonObject mUnitObject = new JsonObject();
                        mUnitObject.addProperty("isNegativeStocks", mUnits.getIsNegativeStocks() == true ? 1 : 0);
                        mUnitObject.addProperty("unit_id", mUnits.getUnits().getId());
                        mUnitObject.addProperty("details_id", mUnits.getId());
                        mUnitObject.addProperty("unit_name", mUnits.getUnits().getUnitName());
                        mUnitObject.addProperty("unit_conv", mUnits.getUnitConversion());
                        mUnitObject.addProperty("unit_marg", mUnits.getUnitConvMargn());
                        mUnitObject.addProperty("mrp", mUnits.getMrp());
                        mUnitObject.addProperty("purchase_rate", mUnits.getPurchaseRate());
                        mUnitObject.addProperty("rateA", mUnits.getMinRateA() != null ? mUnits.getMinRateA() : 0);
                        mUnitObject.addProperty("rateB", mUnits.getMinRateB() != null ? mUnits.getMinRateB() : 0);
                        mUnitObject.addProperty("rateC", mUnits.getMinRateC() != null ? mUnits.getMinRateC() : 0);
                        mUnitObject.addProperty("min_qty", mUnits.getMinQty() != null ? mUnits.getMinQty() : 0.0);
                        mUnitObject.addProperty("max_qty", mUnits.getMaxQty() != null ? mUnits.getMaxQty() : 0.0);
                        mUnitObject.addProperty("levelAId", mUnits.getLevelA() != null ? mUnits.getLevelA().getId() : null);
                        mUnitObject.addProperty("levelBId", mUnits.getLevelB() != null ? mUnits.getLevelB().getId() : null);
                        mUnitObject.addProperty("levelCId", mUnits.getLevelC() != null ? mUnits.getLevelC().getId() : null);
                        JsonArray batchJsonArray = new JsonArray();
                        Long levelaUnit = mUnits.getLevelA() != null ? mUnits.getLevelA().getId() : null;
                        Long levelbUnit = mUnits.getLevelB() != null ? mUnits.getLevelB().getId() : null;
                        Long levelcUnit = mUnits.getLevelC() != null ? mUnits.getLevelC().getId() : null;
                        List<ProductOpeningStocks> openingStocks = openingStocksRepository.findByProductOpening(mUnits.getProduct().getId(), mUnits.getUnits().getId(), levelaUnit, levelbUnit, levelcUnit);
                        for (ProductOpeningStocks mOpeningStocks : openingStocks) {
                            JsonObject mObject = new JsonObject();
                            mObject.addProperty("id", mOpeningStocks.getId());
                            mObject.addProperty("b_no", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getBatchNo() : "");
                            mObject.addProperty("batch_id", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getId().toString() : "");
                            mObject.addProperty("opening_qty", mOpeningStocks.getOpeningStocks());
                            mObject.addProperty("b_free_qty", mOpeningStocks.getFreeOpeningQty());
                            mObject.addProperty("b_mrp", mOpeningStocks.getMrp());
                            mObject.addProperty("b_sale_rate", mOpeningStocks.getSalesRate());
                            mObject.addProperty("b_purchase_rate", mOpeningStocks.getPurchaseRate());
                            mObject.addProperty("b_costing", mOpeningStocks.getCosting());
                            mObject.addProperty("b_expiry", mOpeningStocks.getExpiryDate() != null ? mOpeningStocks.getExpiryDate().toString() : "");
                            mObject.addProperty("b_manufacturing_date", mOpeningStocks.getManufacturingDate() != null ? mOpeningStocks.getManufacturingDate().toString() : "");
                            batchJsonArray.add(mObject);
                        }
                        mUnitObject.add("batchList", batchJsonArray);
                        unitJsonArray.add(mUnitObject);
                    }
                    levelCJsonObject.add("units", unitJsonArray);
                    levelCJsonArray.add(levelCJsonObject);
                }
                levelBJsonObject.add("levelc", levelCJsonArray);
                levelBJsonArray.add(levelBJsonObject);
            }
            levelaJsonObject.add("levelb", levelBJsonArray);
            LevelAJsonArray.add(levelaJsonObject);
        }
        return LevelAJsonArray;
    }

    public Object getLastproductData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Product mProduct = productRepository.findTopByStatusOrderByIdDesc(true);
        JsonObject result = new JsonObject();
        JsonObject response = new JsonObject();
        if (mProduct != null) {
            response.addProperty("isBatchNo", mProduct.getIsBatchNumber());
            response.addProperty("isInventory", mProduct.getIsInventory());
//            response.addProperty("shelfId", mProduct.getShelfId());
            response.addProperty("brandId", mProduct.getBrand() != null ? mProduct.getBrand().getId() : null);
            response.addProperty("groupId", mProduct.getGroup() != null ? mProduct.getGroup().getId() : null);
            response.addProperty("subgroupId", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getId() : null);
            response.addProperty("categoryId", mProduct.getCategory() != null ? mProduct.getCategory().getId() : null);
            response.addProperty("subcategoryId", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getId() : null);
            response.addProperty("hsnNo", mProduct.getProductHsn() != null ? mProduct.getProductHsn().getId() : null);
            response.addProperty("tax", mProduct.getTaxMaster() != null ? mProduct.getTaxMaster().getId() : null);
            response.addProperty("taxApplicableDate", mProduct.getApplicableDate() != null ? mProduct.getApplicableDate().toString() : null);
            response.addProperty("taxType", mProduct.getTaxType() != null ? mProduct.getTaxType() : null);
            response.addProperty("igst", mProduct.getIgst() != null ? mProduct.getIgst() : null);
            response.addProperty("cgst", mProduct.getCgst() != null ? mProduct.getCgst() : null);
            response.addProperty("sgst", mProduct.getSgst() != null ? mProduct.getSgst() : null);
//            response.addProperty("minStock", mProduct.getMinStock() != null ? mProduct.getMinStock() : 0.0);
//            response.addProperty("maxStock", mProduct.getMaxStock() != null ? mProduct.getMaxStock() : 0.0);
            List<ProductUnitPacking> mUnits = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
            if (mUnits != null && mUnits.size() > 0) {
                response.addProperty("selectedUnit", mUnits.get(0).getUnits().getId());
            }
            result.addProperty("messege", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("messege", "empty");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            result.add("responseObject", response);
        }
        return result;
    }

    public Object getPurchaseRateProduct(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            Long productId = Long.parseLong(request.getParameter("product_id"));
            Long levelAId = null;
            if (!request.getParameter("level_a_id").equalsIgnoreCase(""))
                levelAId = Long.parseLong(request.getParameter("level_a_id"));
            Long levelBId = null;
            if (!request.getParameter("level_b_id").equalsIgnoreCase(""))
                levelBId = Long.valueOf(request.getParameter("level_b_id"));
            Long levelCId = null;
            if (!request.getParameter("level_c_id").equalsIgnoreCase(""))
                levelCId = Long.valueOf(request.getParameter("level_c_id"));
            Long unitId = Long.parseLong(request.getParameter("unit_id"));
            ProductUnitPacking mUnitPackaging = productUnitRepository.findRate(productId, levelAId, levelBId, levelCId, unitId, true);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("purchase_rate", mUnitPackaging.getPurchaseRate());
            jsonObject.addProperty("mrp", mUnitPackaging.getMrp());
            jsonObject.addProperty("rate_a", mUnitPackaging.getMinRateA());
            jsonObject.addProperty("rate_b", mUnitPackaging.getMinRateB());
            jsonObject.addProperty("rate_c", mUnitPackaging.getMinRateC());
            response.addProperty("message", "Successs");
            response.add("data", jsonObject);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            productLogger.error("error in create product:" + e.getMessage());
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    /***** Optimising Code for Product Creation (Kiran visit Solapur) ******/
    public Object createProductNew(MultipartHttpServletRequest request) {
        Product product = new Product();
        Product newProduct = new Product();
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseObject = new ResponseMessage();
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        try {
            if (users.getBranch() != null) branch = users.getBranch();
            product.setBranch(branch);
            product.setOutlet(outlet);
            product.setProductName(request.getParameter("productName").trim());
            if (paramMap.containsKey("productCode")) product.setProductCode(request.getParameter("productCode"));
            if (paramMap.containsKey("productDescription"))
                product.setDescription(request.getParameter("productDescription"));
            product.setStatus(true);
            if (paramMap.containsKey("barcodeNo")) product.setBarcodeNo(request.getParameter("barcodeNo"));
            if (paramMap.containsKey("isSerialNo"))
                product.setIsSerialNumber(Boolean.parseBoolean(request.getParameter("isSerialNo")));
            product.setIsBatchNumber(Boolean.parseBoolean(request.getParameter("isBatchNo")));
            product.setIsInventory(Boolean.parseBoolean(request.getParameter("isInventory")));
            if (paramMap.containsKey("isWarranty")) {
                product.setIsWarrantyApplicable(Boolean.parseBoolean(request.getParameter("isWarranty")));
                if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                    product.setWarrantyDays(Integer.parseInt(request.getParameter("nodays")));
                } else {
                    product.setWarrantyDays(0);
                }
            } else {
                product.setIsWarrantyApplicable(false);
            }
            if (paramMap.containsKey("drugType")) product.setDrugType(request.getParameter("drugType"));
            if (paramMap.containsKey("drug_contents")) product.setDrugContents(request.getParameter("drug_contents"));
            if (paramMap.containsKey("productType")) product.setProductType(request.getParameter("productType"));
            if (paramMap.containsKey("isGroup"))
                product.setIsGroup(Boolean.parseBoolean(request.getParameter("isGroup")));
            if (paramMap.containsKey("isFormulation"))
                product.setIsFormulation(Boolean.parseBoolean(request.getParameter("isFormulation")));
            if (paramMap.containsKey("isCategory"))
                product.setIsCategory(Boolean.parseBoolean(request.getParameter("isCategory")));
            if (paramMap.containsKey("isSubCategory"))
                product.setIsSubCategory(Boolean.parseBoolean(request.getParameter("isSubCategory")));
            if (paramMap.containsKey("isMIS")) product.setIsMIS(Boolean.parseBoolean(request.getParameter("isMIS")));
            if (paramMap.containsKey("isPrescription"))
                product.setIsPrescription(Boolean.parseBoolean(request.getParameter("isPrescription")));
//            if (paramMap.containsKey("uploadImage")) product.setUploadImage(request.getParameter("uploadImage"));
            if (request.getFile("uploadImage") != null) {
                MultipartFile image = request.getFile("uploadImage");
                fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                if (imagePath != null) {
                    product.setUploadImage(File.separator + "uploads" + File.separator + imagePath);
                }
            }
            if (paramMap.containsKey("isCommision"))
                product.setIsCommision(Boolean.parseBoolean(request.getParameter("isCommision")));
            if (paramMap.containsKey("isGVProducts")) {
                product.setIsGVProducts(Boolean.parseBoolean(request.getParameter("isGVProducts")));
                if (Boolean.parseBoolean(request.getParameter("isGVProducts"))) {
                    if (paramMap.containsKey("gvOfProducts"))
                        product.setGvOfProducts(request.getParameter("gvOfProducts"));
                }
            }
            /*product.setFsrmh(Double.parseDouble(request.getParameter("fsrmh")));
            product.setFsrai(Double.parseDouble(request.getParameter("fsrai")));
            product.setCsrmh(Double.parseDouble(request.getParameter("csrmh")));
            product.setCsrai(Double.parseDouble(request.getParameter("csrai")));
*/
            product.setCreatedBy(users.getId());
            /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
            if (paramMap.containsKey("shelfId")) product.setShelfId(request.getParameter("shelfId"));
            if (paramMap.containsKey("barcodeSaleQuantity"))
                product.setBarcodeSalesQty(Double.parseDouble(request.getParameter("barcodeSaleQuantity")));
            if (paramMap.containsKey("purchaseRate"))
                product.setPurchaseRate(Double.parseDouble(request.getParameter("purchaseRate")));
            if (paramMap.containsKey("margin"))
                product.setMarginPer(Double.parseDouble(request.getParameter("margin")));
            PackingMaster mPackingMaster = null;
            Group mGroupMaster = null;
            Brand mBrandMaster = null;
            Category mCategoryMaster = null;
            Subcategory msubCategory = null;
            Subgroup mSubgroup = null;
            if (paramMap.containsKey("brandId")) {
                mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
                product.setBrand(mBrandMaster);
            }
            if (paramMap.containsKey("packagingId")) {
                mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
                product.setPackingMaster(mPackingMaster);
            }
            if (paramMap.containsKey("groupId")) {
                mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
                product.setGroup(mGroupMaster);
            }
            if (paramMap.containsKey("subgroupId")) {
                mSubgroup = subgroupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subgroupId")), true);
                product.setSubgroup(mSubgroup);
            }
            if (paramMap.containsKey("categoryId")) {
                mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
                product.setCategory(mCategoryMaster);
            }
            if (paramMap.containsKey("subcategoryId")) {
                msubCategory = subcategoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subcategoryId")), true);
                product.setSubcategory(msubCategory);
            }
            if (paramMap.containsKey("weight")) product.setWeight(Double.parseDouble(request.getParameter("weight")));
            if (paramMap.containsKey("weightUnit")) product.setWeightUnit(request.getParameter("weightUnit"));
            if (paramMap.containsKey("disPer1"))
                product.setDiscountInPer(Double.parseDouble(request.getParameter("disPer1")));
            if (paramMap.containsKey("hsnNo")) {
                ProductHsn productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
                if (productHsn != null) {
                    product.setProductHsn(productHsn);
                }
            }
            if (paramMap.containsKey("tax")) {
                LocalDate applicableDate = null;
                TaxMaster taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
                if (taxMaster != null) {
                    product.setTaxMaster(taxMaster);
                }
                if (paramMap.containsKey("taxApplicableDate"))
                    product.setApplicableDate(LocalDate.parse(request.getParameter("taxApplicableDate")));
          /* inserting into ProductTax Master to maintain tax information of Product,
            if (applicableDate != null) {
                try {
                    ProductTaxDateMaster productTaxDateMaster = new ProductTaxDateMaster();
                    //   productTaxDateMaster = productTaxDateMasterRepository.findTax();
                    productTaxDateMaster.setProduct(newProduct);
                    productTaxDateMaster.setProductHsn(productUnitPacking.getProductHsn());
                    productTaxDateMaster.setTaxMaster(productUnitPacking.getTaxMaster());
                    productTaxDateMaster.setApplicableDate(productUnitPacking.getTaxApplicableDate());
                    productTaxDateMaster.setStatus(true);
                    productTaxDateMaster.setUpdatedBy(users.getId());
                    productTaxDateMasterRepository.save(productTaxDateMaster);
                } catch (Exception e) {
                    productLogger.error("Error in Product Creation-> ProductTaxDateMaster Creation-> " + e.getMessage());
                }
            }
            /***** End of inserting into ProductTax Master  *****/
            }
            if (paramMap.containsKey("taxType")) product.setTaxType(request.getParameter("taxType"));
            if (paramMap.containsKey("igst")) product.setIgst(Double.parseDouble(request.getParameter("igst")));
            if (paramMap.containsKey("cgst")) product.setCgst(Double.parseDouble(request.getParameter("cgst")));
            if (paramMap.containsKey("sgst")) product.setSgst(Double.parseDouble(request.getParameter("sgst")));
            if (paramMap.containsKey("minStock"))
                product.setMinStock(Double.parseDouble(request.getParameter("minStock")));
            if (paramMap.containsKey("maxStock"))
                product.setMaxStock(Double.parseDouble(request.getParameter("maxStock")));

            if (paramMap.containsKey("ecomType") && !request.getParameter("ecomType").isEmpty())
                product.setEcommerceTypeId(Long.valueOf(request.getParameter("ecomType")));
            if (paramMap.containsKey("ecomPrice") && !request.getParameter("ecomPrice").isEmpty())
                product.setSellingPrice(Double.parseDouble(request.getParameter("ecomPrice")));
            if (paramMap.containsKey("ecomDiscount") && !request.getParameter("ecomDiscount").isEmpty())
                product.setDiscountPer(Double.parseDouble(request.getParameter("ecomDiscount")));
            if (paramMap.containsKey("ecomAmount") && !request.getParameter("ecomAmount").isEmpty())
                product.setAmount(Double.parseDouble(request.getParameter("ecomAmount")));
            if (paramMap.containsKey("ecomLoyality") && !request.getParameter("ecomLoyality").isEmpty())
                product.setLoyalty(Double.parseDouble(request.getParameter("ecomLoyality")));
            if (request.getFile("image1") != null) {
                MultipartFile image = request.getFile("image1");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage1("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }
            if (request.getFile("image2") != null) {
                MultipartFile image = request.getFile("image2");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage2("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }
            if (request.getFile("image3") != null) {
                MultipartFile image = request.getFile("image3");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage3("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }
            if (request.getFile("image4") != null) {
                MultipartFile image = request.getFile("image4");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage4("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }
            if (request.getFile("image5") != null) {
                MultipartFile image = request.getFile("image5");
                fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

                if (imagePath != null) {
                    product.setImage5("/uploads" + File.separator + "product" + File.separator + imagePath);
                } else {
                    responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    responseObject.setMessage("Failed to upload documents. Please try again!");

                }
            }

            product.setIsDelete(true);
            newProduct = productRepository.save(product);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Creation:" + exceptionAsString);
        }
        if (paramMap.containsKey("cantentMapData")) {
            insertIntoProductContentMaster(product, request);
//            else updateShippingDetails(mLedger, request);
        }
        /**** END ****/
        try {
            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("productrows");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                LevelB levelB = null; //group
                LevelA levelA = null;//brand
                LevelC levelC = null;//Category
                if (!object.get("selectedLevelA").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("selectedLevelA").getAsLong(), true);
                }
                if (!object.get("selectedLevelB").getAsString().equalsIgnoreCase("")) {
                    levelB = levelBRepository.findByIdAndStatus(object.get("selectedLevelB").getAsLong(), true);
                }
                if (!object.get("selectedLevelC").getAsString().equalsIgnoreCase("")) {
                    levelC = levelCRepository.findByIdAndStatus(object.get("selectedLevelC").getAsLong(), true);
                }
                Units unit = unitsRepository.findByIdAndStatus(object.get("selectedUnit").getAsLong(), true);
                ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                productUnitPacking.setUnits(unit);
                if (object.has("conv"))
                    productUnitPacking.setUnitConversion(object.get("conv").getAsDouble());
                else
                    productUnitPacking.setUnitConversion(1.0);

                if (object.has("unit_marg")) productUnitPacking.setUnitConvMargn(object.get("unit_marg").getAsDouble());
                if (object.has("is_negetive") && object.get("is_negetive").getAsBoolean()) {
                    productUnitPacking.setIsNegativeStocks(true);
                } else {
                    productUnitPacking.setIsNegativeStocks(false);
                }
                productUnitPacking.setMrp(object.get("mrp").getAsDouble());
                productUnitPacking.setPurchaseRate(object.get("pur_rate").getAsDouble());
                if (object.has("min_margin")) productUnitPacking.setMinMargin(object.get("min_margin").getAsDouble());
                productUnitPacking.setMinRateA(object.get("rate_1").getAsDouble());//sales Rate
                productUnitPacking.setMinRateB(object.get("rate_2").getAsDouble());
                productUnitPacking.setMinRateC(object.get("rate_3").getAsDouble());
                productUnitPacking.setIsRate(object.get("is_rate").getAsBoolean());
                if (object.get("is_rate").getAsBoolean()) {
                    productUnitPacking.setFsrmh(object.get("rate_1").getAsDouble());
                    productUnitPacking.setFsrai(object.get("rate_2").getAsDouble());
                    productUnitPacking.setCsrmh(object.get("rate_3").getAsDouble());
                    productUnitPacking.setCsrai(object.get("rate_4").getAsString().isEmpty() ? 0.0 : object.get("rate_4").getAsDouble());
                } else {
                    productUnitPacking.setFsrmh(0.0);
                    productUnitPacking.setFsrai(0.0);
                    productUnitPacking.setCsrmh(0.0);
                    productUnitPacking.setCsrai(0.0);
                }

                productUnitPacking.setStatus(true);
                productUnitPacking.setProduct(newProduct);
                productUnitPacking.setCreatedBy(users.getId());
                /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                if (object.has("min_qty"))
                    productUnitPacking.setMinQty(object.get("min_qty").getAsDouble());
                if (object.has("max_qty"))
                    productUnitPacking.setMaxQty(object.get("max_qty").getAsDouble());
                productUnitPacking.setLevelA(levelA);
                productUnitPacking.setLevelB(levelB);
                productUnitPacking.setLevelC(levelC);
                productUnitRepository.save(productUnitPacking);
                /****** Inserting Product Opening Stocks ******/
                JsonArray mBatchJsonArray;
                JsonElement batchListElement = object.get("batchList");

                if (batchListElement != null && !batchListElement.isJsonNull()) {
                    mBatchJsonArray = batchListElement.getAsJsonArray();
                } else {
                    mBatchJsonArray = new JsonArray();
                }

                /* fiscal year mapping */
                LocalDate mDate = LocalDate.now();
                FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(mDate);
                if (mBatchJsonArray.size() > 0) {
                    for (JsonElement mBatchElement : mBatchJsonArray) {
                        ProductBatchNo productBatchNo = null;
                        JsonObject mBatchJsonObject = mBatchElement.getAsJsonObject();
                        ProductBatchNo mproductBatchNo = null;
                        Double costing = 0.0;
                        Long id = mBatchJsonObject.get("id").getAsLong();
                        if (mBatchJsonObject.get("isOpeningbatch").getAsBoolean()) {
                            mproductBatchNo = new ProductBatchNo();
                            if (fiscalYear != null) {
                                mproductBatchNo.setFiscalYear(fiscalYear);
                            }
                            if (mBatchJsonObject.has("b_no"))
                                mproductBatchNo.setBatchNo(mBatchJsonObject.get("b_no").getAsString());
                            if (mBatchJsonObject.has("b_mrp") && !mBatchJsonObject.get("b_mrp").getAsString().isEmpty())
                                mproductBatchNo.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                            if (mBatchJsonObject.has("b_purchase_rate") && !mBatchJsonObject.get("b_purchase_rate").getAsString().isEmpty())
                                mproductBatchNo.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                            if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().isEmpty()) {
                                costing = mBatchJsonObject.get("b_costing").getAsDouble();
                            }
                            mproductBatchNo.setCosting(costing);
                            mproductBatchNo.setSalesRate(0.0);
                            mproductBatchNo.setMinRateA(0.0);
                            if (mBatchJsonObject.has("b_sale_rate") && !mBatchJsonObject.get("b_sale_rate").isJsonNull() && !mBatchJsonObject.get("b_sale_rate").getAsString().isEmpty()) {
                                mproductBatchNo.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                mproductBatchNo.setMinRateA(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                            }
                            mproductBatchNo.setQnty(Integer.parseInt(mBatchJsonObject.get("opening_qty").getAsString()));
                            mproductBatchNo.setOpeningQty(mBatchJsonObject.get("opening_qty").getAsDouble());
                            if (mBatchJsonObject.has("b_free_qty") && !mBatchJsonObject.get("b_free_qty").getAsString().isEmpty())
                                mproductBatchNo.setFreeQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                            if (mBatchJsonObject.has("b_manufacturing_date") &&
                                    !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase("")
                                    && !mBatchJsonObject.get("b_manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                            if (mBatchJsonObject.has("b_expiry") &&
                                    !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase("")
                                    && !mBatchJsonObject.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                            mproductBatchNo.setStatus(true);
                            mproductBatchNo.setProduct(newProduct);
                            mproductBatchNo.setOutlet(outlet);
                            mproductBatchNo.setBranch(branch);
                            mproductBatchNo.setUnits(unit);

                            productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                        } else {
                            List<ProductUnitPacking> mUnitPackaging = productUnitRepository.findByProductIdAndStatus(newProduct.getId(), true);
                            if (mUnitPackaging != null) {
                                if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().equalsIgnoreCase(""))
                                    costing = mBatchJsonObject.get("b_costing").getAsDouble();
                                mUnitPackaging.get(0).setCosting(costing);
                                productUnitRepository.save(mUnitPackaging.get(0));
                            }
                        }
                        try {
                            ProductOpeningStocks newOpeningStock = new ProductOpeningStocks();
                            newOpeningStock.setOpeningStocks(Double.parseDouble(mBatchJsonObject.get("opening_qty").getAsString()));
                            newOpeningStock.setProduct(newProduct);
                            newOpeningStock.setUnits(unit);
                            newOpeningStock.setBranch(branch);
                            newOpeningStock.setOutlet(outlet);
                            newOpeningStock.setProductBatchNo(productBatchNo);
                            newOpeningStock.setFiscalYear(fiscalYear);
                            if (mBatchJsonObject.has("b_free_qty") && !mBatchJsonObject.get("b_free_qty").getAsString().isEmpty())
                                newOpeningStock.setFreeOpeningQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                            if (mBatchJsonObject.has("b_mrp") && !mBatchJsonObject.get("b_mrp").getAsString().isEmpty())
                                newOpeningStock.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                            if (mBatchJsonObject.has("b_purchase_rate") && !mBatchJsonObject.get("b_purchase_rate").getAsString().isEmpty())
                                newOpeningStock.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                            if (mBatchJsonObject.has("b_sale_rate") && !mBatchJsonObject.get("b_sale_rate").getAsString().isEmpty())
                                newOpeningStock.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                            newOpeningStock.setLevelA(levelA);
                            newOpeningStock.setLevelB(levelB);
                            newOpeningStock.setLevelC(levelC);
                            newOpeningStock.setStatus(true);
                            newOpeningStock.setCosting(costing);
                            if (mBatchJsonObject.has("b_manufacturing_date") && !mBatchJsonObject.get("b_manufacturing_date").getAsString().equalsIgnoreCase(""))
                                newOpeningStock.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                            if (mBatchJsonObject.has("b_expiry") && !mBatchJsonObject.get("b_expiry").getAsString().equalsIgnoreCase(""))
                                newOpeningStock.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                            try {
                                openingStocksRepository.save(newOpeningStock);
                            } catch (Exception e) {
                                productLogger.error("Exception:" + e.getMessage());
                            }
                        } catch (Exception e) {
                            responseObject.setMessage("Error in Product Creation");
                            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            productLogger.error("Error in Product Creation:" + exceptionAsString);
                        }
                    }
                    /**
                     * @implNote validation of Product Delete , if any tranx done for this product, user cant delete this product **
                     * @auther ashwins@opethic.com
                     * @version sprint 21
                     **/
                    if (newProduct != null && newProduct.getIsDelete()) {
                        newProduct.setIsDelete(false);
                        productRepository.save(newProduct);

                    }
                }
                /****** END ******/
            }
            /***** set Rate conversion for MultiUnit *****/
            List<ProductUnitPacking> unitPackingList = productUnitRepository.findByProductId(newProduct.getId());
            ProductUnitPacking pu = null;
            ProductUnitPacking prunit = unitPackingList.stream()
                    // Check if the unitPackingList list contains a Product with this ID
                    .filter(p -> p.getIsRate())
                    .findFirst().get();
            unitConversion.convertToMultiUnitRate(unitPackingList, prunit);
            /**** END ****/
            createProductForFranchise(request);
            responseObject.setMessage("Product Created Successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            responseObject.setData(newProduct.getId().toString());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Creation:" + exceptionAsString);
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Internal Server Error");
        }
        return responseObject;
    }


    @Async
    public void createProductForFranchise(MultipartHttpServletRequest request) {
        try {

            Map<String, String[]> paramMap = request.getParameterMap();
            LinkedMultiValueMap body = new LinkedMultiValueMap();
            body.add("productName", request.getParameter("productName").trim());
            if (paramMap.containsKey("productCode")) body.add("productCode", request.getParameter("productCode"));
            if (paramMap.containsKey("productDescription"))
                body.add("productDescription", request.getParameter("productDescription"));
            body.add("status", true);
            if (paramMap.containsKey("barcodeNo")) body.add("barcodeNo", request.getParameter("barcodeNo"));
            if (paramMap.containsKey("isSerialNo"))
                body.add("isSerialNo", Boolean.parseBoolean(request.getParameter("isSerialNo")));
            body.add("isBatchNo", Boolean.parseBoolean(request.getParameter("isBatchNo")));
            body.add("isInventory", Boolean.parseBoolean(request.getParameter("isInventory")));
            if (paramMap.containsKey("isWarranty")) {
                body.add("isWarranty", Boolean.parseBoolean(request.getParameter("isWarranty")));
                if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                    body.add("nodays", Integer.parseInt(request.getParameter("nodays")));
                } else {
                    body.add("nodays", 0);
                }
            } else {
                body.add("isWarranty", false);
            }
            if (paramMap.containsKey("drugType")) body.add("drugType", request.getParameter("drugType"));
            if (paramMap.containsKey("drug_contents")) body.add("drug_contents", request.getParameter("drug_contents"));
            if (paramMap.containsKey("productType")) body.add("productType", request.getParameter("productType"));
            if (paramMap.containsKey("isGroup"))
                body.add("isGroup", Boolean.parseBoolean(request.getParameter("isGroup")));
            if (paramMap.containsKey("isFormulation"))
                body.add("isFormulation", Boolean.parseBoolean(request.getParameter("isFormulation")));
            if (paramMap.containsKey("isCategory"))
                body.add("isCategory", Boolean.parseBoolean(request.getParameter("isCategory")));
            if (paramMap.containsKey("isSubCategory"))
                body.add("isSubCategory", Boolean.parseBoolean(request.getParameter("isSubCategory")));
            if (paramMap.containsKey("isMIS")) body.add("isMIS", Boolean.parseBoolean(request.getParameter("isMIS")));
            if (paramMap.containsKey("isPrescription"))
                body.add("isPrescription", Boolean.parseBoolean(request.getParameter("isPrescription")));

            if (request.getFile("uploadImage") != null) {
                body.add("uploadImage", request.getFile("uploadImage"));
            }
            if (paramMap.containsKey("isCommision"))
                body.add("isCommision", Boolean.parseBoolean(request.getParameter("isCommision")));
            if (Boolean.parseBoolean(request.getParameter("isGVProducts"))) {
                body.add("isGVProducts", true);
                body.add("gvOfProducts", request.getParameter("gvOfProducts"));
            } else {
                body.add("isGVProducts", false);
            }


//            body.add("createdBy",users.getUsername());
            /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
            if (paramMap.containsKey("shelfId")) body.add("shelfId", request.getParameter("shelfId"));
            if (paramMap.containsKey("barcodeSaleQuantity"))
                body.add("barcodeSaleQuantity", request.getParameter("barcodeSaleQuantity"));

            if (paramMap.containsKey("purchaseRate"))
                body.add("purchaseRate", Double.parseDouble(request.getParameter("purchaseRate")));

            if (paramMap.containsKey("margin"))
                body.add("margin", request.getParameter("margin"));


            PackingMaster mPackingMaster = null;
            Group mGroupMaster = null;
            Brand mBrandMaster = null;
            Category mCategoryMaster = null;
            Subcategory msubCategory = null;
            Subgroup mSubgroup = null;
            if (paramMap.containsKey("brandId")) {
                mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
                body.add("brandName", mBrandMaster.getBrandName());
            }
            if (paramMap.containsKey("packagingId")) {
                mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
                body.add("packingMaster", mPackingMaster.getPackName());
            }
            if (paramMap.containsKey("groupId")) {
                mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
                body.add("groupName", mGroupMaster.getGroupName());
            }
            if (paramMap.containsKey("subgroupId")) {
                mSubgroup = subgroupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subgroupId")), true);
                body.add("subGroupName", mSubgroup.getSubgroupName());

            }
            if (paramMap.containsKey("categoryId")) {
                mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
                body.add("categoryName", mCategoryMaster.getCategoryName());
            }
            if (paramMap.containsKey("subcategoryId")) {
                msubCategory = subcategoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subcategoryId")), true);
                body.add("subcategoryName", msubCategory.getSubcategoryName());
            }
            if (paramMap.containsKey("weight")) body.add("weight", Double.parseDouble(request.getParameter("weight")));
            if (paramMap.containsKey("weightUnit")) body.add("weightUnit", request.getParameter("weightUnit"));
            if (paramMap.containsKey("disPer1"))
                body.add("disPer1", Double.parseDouble(request.getParameter("disPer1")));
            if (paramMap.containsKey("hsnNo")) {
                ProductHsn productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
                if (productHsn != null) {
                    body.add("hsnNo", productHsn.getHsnNumber());
                }
            }
            if (paramMap.containsKey("tax")) {
                LocalDate applicableDate = null;
                TaxMaster taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
                if (taxMaster != null) {
                    body.add("taxPercentage", taxMaster.getIgst());
                }
                if (paramMap.containsKey("taxApplicableDate"))
                    body.add("taxApplicableDate", request.getParameter("taxApplicableDate"));
            }

            if (paramMap.containsKey("taxType")) body.add("taxType", request.getParameter("taxType"));
            if (paramMap.containsKey("igst")) body.add("igst", Double.parseDouble(request.getParameter("igst")));
            if (paramMap.containsKey("cgst")) body.add("cgst", Double.parseDouble(request.getParameter("cgst")));
            if (paramMap.containsKey("sgst")) body.add("sgst", Double.parseDouble(request.getParameter("sgst")));
            if (paramMap.containsKey("minStock"))
                body.add("minStock", Double.parseDouble(request.getParameter("minStock")));
            if (paramMap.containsKey("maxStock"))
                body.add("maxStock", Double.parseDouble(request.getParameter("maxStock")));

            if (paramMap.containsKey("ecomType") && !request.getParameter("ecomType").isEmpty())
                body.add("ecomType", Long.valueOf(request.getParameter("ecomType")));
            if (paramMap.containsKey("ecomPrice") && !request.getParameter("ecomPrice").isEmpty())
                body.add("ecomPrice", Double.parseDouble(request.getParameter("ecomPrice")));
            if (paramMap.containsKey("ecomDiscount") && !request.getParameter("ecomDiscount").isEmpty())
                body.add("ecomDiscount", Double.parseDouble(request.getParameter("ecomDiscount")));
            if (paramMap.containsKey("ecomAmount") && !request.getParameter("ecomAmount").isEmpty())
                body.add("ecomAmount", Double.parseDouble(request.getParameter("ecomAmount")));
            if (paramMap.containsKey("ecomLoyality") && !request.getParameter("ecomLoyality").isEmpty())
                body.add("ecomLoyality", Double.parseDouble(request.getParameter("ecomLoyality")));
            if (request.getFile("image1") != null) {
                MultipartFile image = request.getFile("image1");
                body.add("image1", image);
            }
            if (request.getFile("image2") != null) {
                MultipartFile image = request.getFile("image2");
                body.add("image2", image);
            }
            if (request.getFile("image3") != null) {
                MultipartFile image = request.getFile("image3");
                body.add("image3", image);
            }
            if (request.getFile("image4") != null) {
                MultipartFile image = request.getFile("image4");
                body.add("image4", image);
            }
            if (request.getFile("image5") != null) {
                MultipartFile image = request.getFile("image5");
                body.add("image5", image);
            }

            body.add("isDelete", true);

            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("productrows");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            JsonArray productRows = new JsonArray();
            if (array.size() > 0) {
                JsonObject jsonObject = array.get(0).getAsJsonObject();
                if (jsonObject.has("rate_1")) {
                    body.add("fsr", jsonObject.get("rate_1").getAsDouble());
                } else {
                    body.add("fsr", 0);
                }
                if (jsonObject.has("rate_2")) {
                    body.add("csr", jsonObject.get("rate_2").getAsDouble());
                } else {
                    body.add("csr", 0);
                }
            }
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                JsonObject productRow = new JsonObject();
                LevelB levelB = null; //group
                LevelA levelA = null;//brand
                LevelC levelC = null;//Category


                if (!object.get("selectedLevelA").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("selectedLevelA").getAsLong(), true);
                    productRow.addProperty("selectedLevelA", levelA.getLevelName());

                } else {
                    productRow.addProperty("selectedLevelA", "");
                }


                String selectedLevelBValue = object.get("selectedLevelB").getAsString();
                if (selectedLevelBValue != null && !selectedLevelBValue.isEmpty()) {
                    levelB = levelBRepository.findByIdAndStatus(Long.parseLong(selectedLevelBValue), true);
                    productRow.addProperty("selectedLevelB", levelB.getLevelName());
                } else {
                    productRow.addProperty("selectedLevelB", "");
                }

                if (!object.get("selectedLevelC").getAsString().equalsIgnoreCase("")) {
                    levelC = levelCRepository.findByIdAndStatus(object.get("selectedLevelC").getAsLong(), true);
                    productRow.addProperty("selectedLevelC", levelC.getLevelName());
                } else {
                    productRow.addProperty("selectedLevelC", "");
                }

                Units unit = unitsRepository.findByIdAndStatus(object.get("selectedUnit").getAsLong(), true);
                productRow.addProperty("selectedUnit", unit.getUnitName());

                if (object.has("conv")) productRow.addProperty("conv", object.get("conv").getAsDouble());
                if (object.has("unit_marg")) productRow.addProperty("unit_marg", object.get("unit_marg").getAsDouble());
                if (object.has("is_negetive"))
                    productRow.addProperty("is_negetive", object.get("is_negetive").getAsBoolean());
                if (object.has("mrp")) productRow.addProperty("mrp", object.get("mrp").getAsDouble());
                if (object.has("pur_rate")) productRow.addProperty("pur_rate", object.get("pur_rate").getAsDouble());
                if (object.has("min_margin"))
                    productRow.addProperty("min_margin", object.get("min_margin").getAsDouble());
                productRow.addProperty("is_rate", object.get("is_rate").getAsBoolean());

                if (object.has("rate_1")) {
                    productRow.addProperty("rate_1", object.get("rate_1").getAsDouble());//fsr
                    productRow.addProperty("fsrmh", object.get("rate_1").getAsDouble());
                }
                if (object.has("rate_2")) {
                    productRow.addProperty("rate_2", object.get("rate_2").getAsDouble());//csr
                    productRow.addProperty("fsrai", object.get("rate_2").getAsDouble());
                }
                if (object.has("rate_3")) {
                    productRow.addProperty("rate_3", object.get("rate_3").getAsDouble());
                    productRow.addProperty("csrmh", object.get("rate_3").getAsDouble());
                }
                if (object.has("rate_4") && !object.get("rate_4").getAsString().isEmpty()) {
                    productRow.addProperty("rate_4", object.get("rate_4").getAsDouble());
                    productRow.addProperty("csrai", object.get("rate_4").getAsDouble());
                }
                if (object.has("min_qty")) productRow.addProperty("min_qty", object.get("min_qty").getAsDouble());
                if (object.has("max_qty")) productRow.addProperty("max_qty", object.get("max_qty").getAsDouble());
                productRows.add(productRow);
            }

            body.add("productrows", productRows.toString());
//            product.setFsrmh(Double.parseDouble(request.getParameter("fsrmh")));
//            product.setFsrai(Double.parseDouble(request.getParameter("fsrai")));
//            product.setCsrmh(Double.parseDouble(request.getParameter("csrmh")));
//            product.setCsrai(Double.parseDouble(request.getParameter("csrai")));
            //? For Rate management fsr
           /* body.add("fsrmh", request.getParameter("fsrmh"));
            body.add("fsrai", request.getParameter("fsrai"));
            body.add("csrmh", request.getParameter("csrmh"));
            body.add("csrai", request.getParameter("csrai"));*/
            List<FranchiseMaster> franchiseMasters = franchiseMasterRepository.findByStatus(true);
            if (franchiseMasters != null) {
                System.out.println("Franchise Size : " + franchiseMasters.size());
                for (FranchiseMaster franchiseMaster : franchiseMasters) {
                    System.out.println("Franchise : " + franchiseMaster.getFranchiseCode());

                    HttpHeaders frHdr = new HttpHeaders();
                    frHdr.setContentType(MediaType.MULTIPART_FORM_DATA);
                    frHdr.add("branch", franchiseMaster.getFranchiseCode());
                    frHdr.add("stateCode", franchiseMaster.getStateCode());


                    HttpEntity frEntity = new HttpEntity<>(body, frHdr);

                    String resData = restTemplate.exchange(
                            frUrl + "/create_product_from_gv", HttpMethod.POST, frEntity, String.class).getBody();
                    System.out.println("frCreateProductResponse => " + resData);


                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }


    }

    @Async
    public void updateProductForFranchise(MultipartHttpServletRequest request) {
        try {

            Map<String, String[]> paramMap = request.getParameterMap();
            LinkedMultiValueMap body = new LinkedMultiValueMap();
            body.add("productId", request.getParameter("productId").trim());
            body.add("productName", request.getParameter("productName").trim());
            if (paramMap.containsKey("productCode")) body.add("productCode", request.getParameter("productCode"));
            if (paramMap.containsKey("productDescription"))
                body.add("productDescription", request.getParameter("productDescription"));
            body.add("status", true);
            if (paramMap.containsKey("barcodeNo")) body.add("barcodeNo", request.getParameter("barcodeNo"));
            if (paramMap.containsKey("isSerialNo"))
                body.add("isSerialNo", Boolean.parseBoolean(request.getParameter("isSerialNo")));
            body.add("isBatchNo", Boolean.parseBoolean(request.getParameter("isBatchNo")));
            body.add("isInventory", Boolean.parseBoolean(request.getParameter("isInventory")));
            if (paramMap.containsKey("isWarranty")) {
                body.add("isWarranty", Boolean.parseBoolean(request.getParameter("isWarranty")));
                if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                    body.add("nodays", Integer.parseInt(request.getParameter("nodays")));
                } else {
                    body.add("nodays", 0);
                }
            } else {
                body.add("isWarranty", false);
            }
            if (paramMap.containsKey("drugType")) body.add("drugType", request.getParameter("drugType"));
            if (paramMap.containsKey("drug_contents")) body.add("drug_contents", request.getParameter("drug_contents"));
            if (paramMap.containsKey("productType")) body.add("productType", request.getParameter("productType"));
            if (paramMap.containsKey("isGroup"))
                body.add("isGroup", Boolean.parseBoolean(request.getParameter("isGroup")));
            if (paramMap.containsKey("isFormulation"))
                body.add("isFormulation", Boolean.parseBoolean(request.getParameter("isFormulation")));
            if (paramMap.containsKey("isCategory"))
                body.add("isCategory", Boolean.parseBoolean(request.getParameter("isCategory")));
            if (paramMap.containsKey("isSubCategory"))
                body.add("isSubCategory", Boolean.parseBoolean(request.getParameter("isSubCategory")));
            if (paramMap.containsKey("isMIS")) body.add("isMIS", Boolean.parseBoolean(request.getParameter("isMIS")));
            if (paramMap.containsKey("isPrescription"))
                body.add("isPrescription", Boolean.parseBoolean(request.getParameter("isPrescription")));

            if (request.getFile("uploadImage") != null) {
                body.add("uploadImage", request.getFile("uploadImage"));
            }
            if (paramMap.containsKey("isCommision"))
                body.add("isCommision", Boolean.parseBoolean(request.getParameter("isCommision")));
            if (Boolean.parseBoolean(request.getParameter("isGVProducts"))) {
                body.add("isGVProducts", true);
                body.add("gvOfProducts", request.getParameter("gvOfProducts"));
            } else {
                body.add("isGVProducts", false);
            }


//            body.add("createdBy",users.getUsername());
            /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
            if (paramMap.containsKey("shelfId")) body.add("shelfId", request.getParameter("shelfId"));
            if (paramMap.containsKey("barcodeSaleQuantity"))
                body.add("barcodeSaleQuantity", request.getParameter("barcodeSaleQuantity"));

            if (paramMap.containsKey("purchaseRate"))
                body.add("purchaseRate", Double.parseDouble(request.getParameter("purchaseRate")));

            if (paramMap.containsKey("margin"))
                body.add("margin", request.getParameter("margin"));


            PackingMaster mPackingMaster = null;
            Group mGroupMaster = null;
            Brand mBrandMaster = null;
            Category mCategoryMaster = null;
            Subcategory msubCategory = null;
            Subgroup mSubgroup = null;
            if (paramMap.containsKey("brandId")) {
                mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
                body.add("brandName", mBrandMaster.getBrandName());
            }
            if (paramMap.containsKey("packagingId")) {
                mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
                body.add("packingMaster", mPackingMaster.getPackName());
            }
            if (paramMap.containsKey("groupId")) {
                mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
                body.add("groupName", mGroupMaster.getGroupName());
            }
            if (paramMap.containsKey("subgroupId")) {
                mSubgroup = subgroupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subgroupId")), true);
                body.add("subGroupName", mSubgroup.getSubgroupName());

            }
            if (paramMap.containsKey("categoryId")) {
                mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
                body.add("categoryName", mCategoryMaster.getCategoryName());
            }
            if (paramMap.containsKey("subcategoryId")) {
                msubCategory = subcategoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subcategoryId")), true);
                body.add("subcategoryName", msubCategory.getSubcategoryName());
            }
            if (paramMap.containsKey("weight")) body.add("weight", Double.parseDouble(request.getParameter("weight")));
            if (paramMap.containsKey("weightUnit")) body.add("weightUnit", request.getParameter("weightUnit"));
            if (paramMap.containsKey("disPer1"))
                body.add("disPer1", Double.parseDouble(request.getParameter("disPer1")));
            if (paramMap.containsKey("hsnNo")) {
                ProductHsn productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
                if (productHsn != null) {
                    body.add("hsnNo", productHsn.getHsnNumber());
                }
            }
            if (paramMap.containsKey("tax")) {
                LocalDate applicableDate = null;
                TaxMaster taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
                if (taxMaster != null) {
                    body.add("taxPercentage", taxMaster.getIgst());
                }
                if (paramMap.containsKey("taxApplicableDate"))
                    body.add("taxApplicableDate", request.getParameter("taxApplicableDate"));
            }

            if (paramMap.containsKey("taxType")) body.add("taxType", request.getParameter("taxType"));
            if (paramMap.containsKey("igst")) body.add("igst", Double.parseDouble(request.getParameter("igst")));
            if (paramMap.containsKey("cgst")) body.add("cgst", Double.parseDouble(request.getParameter("cgst")));
            if (paramMap.containsKey("sgst")) body.add("sgst", Double.parseDouble(request.getParameter("sgst")));
            if (paramMap.containsKey("minStock"))
                body.add("minStock", Double.parseDouble(request.getParameter("minStock")));
            if (paramMap.containsKey("maxStock"))
                body.add("maxStock", Double.parseDouble(request.getParameter("maxStock")));

            if (paramMap.containsKey("ecomType") && !request.getParameter("ecomType").isEmpty())
                body.add("ecomType", Long.valueOf(request.getParameter("ecomType")));
            if (paramMap.containsKey("ecomPrice") && !request.getParameter("ecomPrice").isEmpty())
                body.add("ecomPrice", Double.parseDouble(request.getParameter("ecomPrice")));
            if (paramMap.containsKey("ecomDiscount") && !request.getParameter("ecomDiscount").isEmpty())
                body.add("ecomDiscount", Double.parseDouble(request.getParameter("ecomDiscount")));
            if (paramMap.containsKey("ecomAmount") && !request.getParameter("ecomAmount").isEmpty())
                body.add("ecomAmount", Double.parseDouble(request.getParameter("ecomAmount")));
            if (paramMap.containsKey("ecomLoyality") && !request.getParameter("ecomLoyality").isEmpty())
                body.add("ecomLoyality", Double.parseDouble(request.getParameter("ecomLoyality")));
            if (request.getFile("image1") != null) {
                MultipartFile image = request.getFile("image1");
                body.add("image1", image);
            }
            if (request.getFile("image2") != null) {
                MultipartFile image = request.getFile("image2");
                body.add("image2", image);
            }
            if (request.getFile("image3") != null) {
                MultipartFile image = request.getFile("image3");
                body.add("image3", image);
            }
            if (request.getFile("image4") != null) {
                MultipartFile image = request.getFile("image4");
                body.add("image4", image);
            }
            if (request.getFile("image5") != null) {
                MultipartFile image = request.getFile("image5");
                body.add("image5", image);
            }

            body.add("isDelete", true);

            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("productrows");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            JsonArray productRows = new JsonArray();

            if (array.size() > 0) {
                JsonObject jsonObject = array.get(0).getAsJsonObject();
                if (jsonObject.has("rate_1")) {
                    body.add("fsr", jsonObject.get("rate_1").getAsDouble());
                } else {
                    body.add("fsr", 0);
                }
                if (jsonObject.has("rate_2")) {
                    body.add("csr", jsonObject.get("rate_2").getAsDouble());
                } else {
                    body.add("csr", 0);
                }
            }
            //? For Rate management fsr
           /* body.add("fsrmh", request.getParameter("fsrmh"));
            body.add("fsrai", request.getParameter("fsrai"));
            body.add("csrmh", request.getParameter("csrmh"));
            body.add("csrai", request.getParameter("csrai"));*/
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                JsonObject productRow = new JsonObject();
                LevelB levelB = null; //group
                LevelA levelA = null;//brand
                LevelC levelC = null;//Category
                if (!object.get("selectedLevelA").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("selectedLevelA").getAsLong(), true);
                    productRow.addProperty("selectedLevelA", levelA.getLevelName());

                } else {
                    productRow.addProperty("selectedLevelA", "");
                }
                if (!object.get("selectedLevelB").getAsString().equalsIgnoreCase("")) {
                    levelB = levelBRepository.findByIdAndStatus(object.get("selectedLevelB").getAsLong(), true);
                    productRow.addProperty("selectedLevelB", levelB.getLevelName());
                } else {
                    productRow.addProperty("selectedLevelB", "");
                }

                if (!object.get("selectedLevelC").getAsString().equalsIgnoreCase("")) {
                    levelC = levelCRepository.findByIdAndStatus(object.get("selectedLevelC").getAsLong(), true);
                    productRow.addProperty("selectedLevelC", levelC.getLevelName());
                } else {
                    productRow.addProperty("selectedLevelC", "");
                }

                Units unit = unitsRepository.findByIdAndStatus(object.get("selectedUnit").getAsLong(), true);
                productRow.addProperty("selectedUnit", unit.getUnitName());

                if (object.has("conv")) productRow.addProperty("conv", object.get("conv").getAsDouble());
                if (object.has("unit_marg")) productRow.addProperty("unit_marg", object.get("unit_marg").getAsDouble());
                if (object.has("is_negetive"))
                    productRow.addProperty("is_negetive", object.get("is_negetive").getAsBoolean());
                if (object.has("mrp")) productRow.addProperty("mrp", object.get("mrp").getAsDouble());
                if (object.has("pur_rate")) productRow.addProperty("pur_rate", object.get("pur_rate").getAsDouble());
                if (object.has("min_margin"))
                    productRow.addProperty("min_margin", object.get("min_margin").getAsDouble());
                productRow.addProperty("is_rate", object.get("is_rate").getAsBoolean());
                if (object.has("rate_1")) {
                    productRow.addProperty("rate_1", object.get("rate_1").getAsDouble());//fsr
                    productRow.addProperty("fsrmh", object.get("rate_1").getAsDouble());
                }
                if (object.has("rate_2")) {
                    productRow.addProperty("rate_2", object.get("rate_2").getAsDouble());//csr
                    productRow.addProperty("fsrai", object.get("rate_2").getAsDouble());
                }
                if (object.has("rate_3")) {
                    productRow.addProperty("rate_3", object.get("rate_3").getAsDouble());
                    productRow.addProperty("csrmh", object.get("rate_3").getAsDouble());
                }
                if (object.has("rate_4") && !object.get("rate_4").getAsString().isEmpty()) {
                    productRow.addProperty("rate_4", object.get("rate_4").getAsDouble());
                    productRow.addProperty("csrai", object.get("rate_4").getAsDouble());
                }


                if (object.has("min_qty")) productRow.addProperty("min_qty", object.get("min_qty").getAsDouble());
                if (object.has("max_qty")) productRow.addProperty("max_qty", object.get("max_qty").getAsDouble());
                productRows.add(productRow);
            }

            body.add("productrows", productRows.toString());


            List<FranchiseMaster> franchiseMasters = franchiseMasterRepository.findByStatus(true);
            if (franchiseMasters != null) {
                System.out.println("Franchise Size : " + franchiseMasters.size());
                for (FranchiseMaster franchiseMaster : franchiseMasters) {
                    System.out.println("Franchise : " + franchiseMaster.getFranchiseCode());

                    HttpHeaders frHdr = new HttpHeaders();
                    frHdr.setContentType(MediaType.MULTIPART_FORM_DATA);
                    frHdr.add("branch", franchiseMaster.getFranchiseCode());
                    frHdr.add("stateCode", franchiseMaster.getStateCode());

                    HttpEntity frEntity = new HttpEntity<>(body, frHdr);

                    String resData = restTemplate.exchange(
                            frUrl + "/update_product_from_gv", HttpMethod.POST, frEntity, String.class).getBody();
                    System.out.println("frUpdateProductResponse => " + resData);


                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }


    }

    public Object importProduct(MultipartHttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        ContentMaster contentMaster;

        try {
            MultipartFile excelFile = request.getFile("productfile");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            if (excelFile != null) {
                XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
                XSSFSheet sheet = workbook.getSheetAt(0);
                productLogger.error("Total Rows : " + sheet.getPhysicalNumberOfRows() + sdf.format(new Date()));
                int productCode = 0, productName = 1, pkg = 2, brandName = 3, salesTax = 4, hsn = 5, discount = 6, margin = 7, shelfId = 8, misMfg = 9, formation = 10, misCategory = 11, misSubCategory = 12, minQty = 13, maxQty = 14, isBatchRequired = 13, drug = 21, mrp = 35, csrMh = 36, csrAi = 37, fsrMh = 38, fsrAi = 39, purchaseTax = 4, isPrescription = 17, isVaccine = 41, isActive = 42, isDeleted = 43, inStock = 44, scheduledH = 18, scheduledH1 = 19, narcotic = 20, commission = 22, mainCategoryName = 11, subCategoryName = 12, isRate = 26, lowestUnitName = 27, lowestUnitConversion = 28, mediumUnitName = 29, mediumUnitConversion = 30, highestUnitName = 31, highestUnitConversion = 32, isGVProduct = 23, gvProductType = 24;

                int records = sheet.getPhysicalNumberOfRows() - 1;
                System.out.println("Total Records : " + records);
                for (int s = 1; s < records; s++) {
                    productLogger.error("Record:" + s + " Current time: " + sdf.format(new Date()));
                    Product product;
                    Product newProduct;
                    XSSFRow row = sheet.getRow(s);
                    Outlet outlet = users.getOutlet();
                    productLogger.error("Before Product Find Execution... " + sdf.format(new Date()));

                    String productcode = row.getCell(productCode).getStringCellValue().trim();
                    String mismfg = row.getCell(misMfg).getStringCellValue();
                    String productname = row.getCell(productName).getStringCellValue().trim();
                    String packaging = row.getCell(pkg).getStringCellValue().trim();
//                    Constants.decimalFormat.format(row.getCell(purchaseTax).getStringCellValue());

                    double tax = Double.parseDouble(row.getCell(purchaseTax).getStringCellValue());
                    String formationinfo = row.getCell(formation).getStringCellValue().trim();
                    int isprescriptionrequired = (int) row.getCell(isPrescription).getNumericCellValue();
                    int scheduledh = (int) row.getCell(scheduledH).getNumericCellValue();
                    int scheduledh1 = (int) row.getCell(scheduledH1).getNumericCellValue();
                    int narcoticvalue = (int) row.getCell(narcotic).getNumericCellValue();
                    int commissionvalue = (int) row.getCell(commission).getNumericCellValue();
                    int hsnvalue = Integer.parseInt(row.getCell(hsn).getStringCellValue());
                    String maincategoryname = row.getCell(mainCategoryName).getStringCellValue().trim();
                    String subcategoryname = row.getCell(subCategoryName).getStringCellValue().trim();
                    String highunitname = row.getCell(highestUnitName).getStringCellValue().trim();
                    double highunitconversion = 0.0;
                    if (highunitname != null && !highunitname.isEmpty())
                        highunitconversion = row.getCell(highestUnitConversion).getNumericCellValue();
                    String mediumunitname = row.getCell(mediumUnitName).getStringCellValue();

                    double mediumunitconversion = 0.0;
                    if (mediumunitname != null && !mediumunitname.isEmpty())
                        mediumunitconversion = row.getCell(mediumUnitConversion).getNumericCellValue();

                    String israte = row.getCell(isRate).getStringCellValue().trim();
                    String lowestunitname = row.getCell(lowestUnitName).getStringCellValue().trim();
                    double lowestunitconversion = row.getCell(lowestUnitConversion).getNumericCellValue();
                    String brandname = row.getCell(brandName).getStringCellValue().trim();
                    double gvProduct = row.getCell(isGVProduct).getNumericCellValue();
                    String gvProductTypeValue = row.getCell(gvProductType).getStringCellValue().trim();
                    String drugValue = row.getCell(drug).getStringCellValue().trim();
                    double csrmh = row.getCell(csrMh).getNumericCellValue();
                    double csrai = row.getCell(csrAi).getNumericCellValue();
                    double fsrmh = row.getCell(fsrMh).getNumericCellValue();
                    double fsrai = row.getCell(fsrAi).getNumericCellValue();
                    double mrprate = row.getCell(mrp).getNumericCellValue();


                    product = productRepository.findFirstByProductCodeAndProductNameAndOutletId(productcode, productname, outlet.getId());

                    productLogger.error("After Product Find Execution... " + sdf.format(new Date()));
                    if (product == null) {
                        product = new Product();
                        if (users.getBranch() != null) branch = users.getBranch();
                        product.setBranch(branch);
                        product.setOutlet(outlet);
                        product.setProductName(productname);
                        product.setProductCode(productcode);
                        product.setIsSerialNumber(false);
                        product.setIsBatchNumber(true);
                        product.setIsInventory(true);
                        product.setStatus(true);
                        product.setIsDelete(true);
                        product.setCreatedBy(users.getId());
                        product.setMrp(mrprate);
                        product.setCsrmh(csrmh);
                        product.setCsrai(csrai);
                        product.setFsrmh(fsrmh);
                        product.setFsrai(fsrai);
                        PackingMaster mPackingMaster = null;
                        Group mGroupMaster = null;
                        Subgroup mSubGroup = null;
                        Category mCategory = null;
                        Subcategory mSubCategory = null;
                        Brand mBrandMaster = null;
                        Units mUnit = null;
                        productLogger.error("Before Brand Find Execution... " + sdf.format(new Date()));

                        mBrandMaster = brandRepository.findFirstByBrandName(brandname);
                        productLogger.error("After Brand Find Execution... " + sdf.format(new Date()));
                        if (mBrandMaster == null) {
                            mBrandMaster = new Brand();
                            mBrandMaster.setBrandName(brandname);
                            mBrandMaster.setBranch(branch);
                            mBrandMaster.setOutlet(outlet);
                            mBrandMaster.setStatus(true);
                            mBrandMaster.setCreatedBy(users.getId());
                            productLogger.error("Before Saving Brand  " + sdf.format(new Date()));
                            brandRepository.save(mBrandMaster);
                            productLogger.error("After Succesfully Saving Brand  " + sdf.format(new Date()));

                        }

                        product.setBrand(mBrandMaster);
                        product.setIsBrand(true);
                        productLogger.error("Before Packing Find Execution... " + sdf.format(new Date()));
                        mPackingMaster = packingMasterRepository.findFirstByPackNameIgnoreCase(packaging);
                        productLogger.error("After Packing Find Execution... " + sdf.format(new Date()));
                        if (mPackingMaster == null) {
                            mPackingMaster = new PackingMaster();
                            mPackingMaster.setPackName(packaging);
                            mPackingMaster.setBranch(branch);
                            mPackingMaster.setOutlet(outlet);
                            mPackingMaster.setStatus(true);
                            mPackingMaster.setCreatedBy(users.getId());
                            productLogger.error("Before Saving Packing  " + sdf.format(new Date()));
                            packingMasterRepository.save(mPackingMaster);
                            productLogger.error("After Succesfully Saving Packing  " + sdf.format(new Date()));
                        }
                        product.setPackingMaster(mPackingMaster);
                        product.setIsPackage(true);
                        productLogger.error("Before Group Find Execution... " + sdf.format(new Date()));
                        if (mismfg != null) {
                            mGroupMaster = groupRepository.findFirstByGroupNameIgnoreCase(mismfg);
                            if (mGroupMaster == null) {
                                mGroupMaster = new Group();
                                mGroupMaster.setGroupName(mismfg);
                                mGroupMaster.setBranch(branch);
                                mGroupMaster.setOutlet(outlet);
                                mGroupMaster.setStatus(true);
                                mGroupMaster.setCreatedBy(users.getId());
                                productLogger.error("Before Saving Group  " + sdf.format(new Date()));
                                groupRepository.save(mGroupMaster);
                                productLogger.error("After Succesfully Saving Group  " + sdf.format(new Date()));
                            }
                            product.setGroup(mGroupMaster);
                            product.setIsGroup(true);
                            product.setIsMIS(true);
                        } else
                            product.setGroup(null);

                        if (formationinfo != null) {
                            mSubGroup = subgroupRepository.findFirstBySubgroupNameIgnoreCase(formationinfo);
                            if (mSubGroup == null) {
                                mSubGroup = new Subgroup();
                                mSubGroup.setSubgroupName(formationinfo);
                                mSubGroup.setBranch(branch);
                                mSubGroup.setOutlet(outlet);
                                mSubGroup.setStatus(true);
                                mSubGroup.setCreatedBy(users.getId());
                                productLogger.error("Before Saving SubGroup  " + sdf.format(new Date()));
                                subgroupRepository.save(mSubGroup);
                                productLogger.error("After Succesfully Saving Group  " + sdf.format(new Date()));
                            }
                            product.setSubgroup(mSubGroup);
                            product.setIsFormulation(true);
                        } else
                            product.setSubgroup(null);

                        if (maincategoryname != null) {
                            mCategory = categoryRepository.findFirstByCategoryNameIgnoreCase(maincategoryname);
                            if (mCategory == null) {
                                mCategory = new Category();
                                mCategory.setCategoryName(maincategoryname);
                                mSubGroup.setBranch(branch);
                                mSubGroup.setOutlet(outlet);
                                mSubGroup.setStatus(true);
                                mSubGroup.setCreatedBy(users.getId());
                                productLogger.error("Before Saving Category  " + sdf.format(new Date()));
                                categoryRepository.save(mCategory);
                                productLogger.error("After Succesfully Saving Category  " + sdf.format(new Date()));
                            }
                            product.setCategory(mCategory);
                            product.setIsCategory(true);

                        } else {
                            product.setCategory(null);
                        }


                        if (subcategoryname != null) {
                            mSubCategory = subcategoryRepository.findFirstBySubcategoryNameIgnoreCase(subcategoryname);
                            if (mSubCategory == null) {
                                mSubCategory = new Subcategory();
                                mSubCategory.setSubcategoryName(subcategoryname);
                                mSubCategory.setBranch(branch);
                                mSubCategory.setOutlet(outlet);
                                mSubCategory.setStatus(true);
                                mSubCategory.setCreatedBy(users.getId());
                                productLogger.error("Before Saving SubCategory  " + sdf.format(new Date()));
                                subcategoryRepository.save(mSubCategory);
                                productLogger.error("After Succesfully Saving SubCategory  " + sdf.format(new Date()));
                            }
                            product.setSubcategory(mSubCategory);
                            product.setIsSubCategory(true);
                        } else {
                            product.setSubcategory(null);
                        }


                        product.setIsPrescription(isPrescription == 0 ? true : false);
                        String drugType = "";
                        if (scheduledh == 1)
                            drugType = "H";
                        if (scheduledh1 == 1)
                            drugType = "H1";
                        if (narcoticvalue == 1)
                            drugType = "Narcotic";

                        if (!drugType.isEmpty()) {
                            DrugType dt = drugTypeRepository.findFirstByDrugNameIgnoreCase(drugType);
                            if (dt != null) {
                                product.setDrugType(dt.getId() + "");
                            }
                        }

                        if (gvProduct == 1) {
                            product.setIsGVProducts(true);
                            product.setGvOfProducts(gvProductTypeValue);
                        }

                        product.setIsPrescription(isprescriptionrequired == 1 ? true : false);
                        product.setIsCommision(commissionvalue == 1 ? true : false);

                        productLogger.error("Before ProductHsn Find Execution... " + sdf.format(new Date()));
                        ProductHsn productHsn = productHsnRepository.findByHsnNumber(hsnvalue + "");
                        productLogger.error("After ProductHsn Find Execution... " + sdf.format(new Date()));
                        if (productHsn == null) {
                            productHsn = new ProductHsn();
                            productHsn.setHsnNumber(hsnvalue + "");
                            productHsn.setIgst(tax);
                            productHsn.setSgst(tax / 2);
                            productHsn.setCgst(tax / 2);
                            productHsn.setBranch(branch);
                            productHsn.setOutlet(outlet);
                            productHsn.setStatus(true);
                            productHsn.setCreatedBy(users.getId());
                            productLogger.error("Before Saving ProductHsn  " + sdf.format(new Date()));
                            productHsnRepository.save(productHsn);
                            productLogger.error("After Succesfully Saving ProductHsn  " + sdf.format(new Date()));
                        }
                        product.setProductHsn(productHsn);
                        product.setTaxType("Taxable");


                        //String igst = row.getCell(11).getRawValue();
//                        productLogger.error("Before TaxMaster Find Execution... " + sdf.format(new Date()));
//                        TaxMaster taxMaster = taxMasterRepository.findDuplicateGSTWithOutlet(outlet.getId(), Integer.parseInt(tax+"")+"", true);
//                        productLogger.error("After TaxMaster Find Execution... " + sdf.format(new Date()));
                        TaxMaster taxMaster = null;
                        if (branch != null)
                            taxMaster = taxMasterRepository.findDuplicateGSTWithBranch(outlet.getId(), branch.getId(), (decimalFormat.format(tax)), true);
                        else
                            taxMaster = taxMasterRepository.findDuplicateGSTWithOutlet(outlet.getId(), (decimalFormat.format(tax)), true);

                        /*if (taxMaster == null) {
                            taxMaster = new TaxMaster();
                            taxMaster.setGst_per(tax + "");
                            taxMaster.setIgst(Double.parseDouble(tax + ""));
                            taxMaster.setCgst(tax / 2);
                            taxMaster.setSgst(tax / 2);
                            taxMaster.setSratio(Double.parseDouble("50"));
                            taxMaster.setCreatedBy(outlet.getId());
                            taxMaster.setCreatedAt(LocalDateTime.now());
                            taxMaster.setStatus(true);
                            taxMaster.setOutlet(outlet);
                            if (branch != null)
                                taxMaster.setBranch(branch);

                            taxMasterRepository.save(taxMaster);

                        }*/
                        product.setTaxMaster(taxMaster);

//                        product.setTaxMaster(taxMaster);
                        product.setIgst(tax);
                        product.setSgst(tax / 2);
                        product.setCgst(tax / 2);
                        productLogger.error("Before Saving Product " + sdf.format(new Date()));
                        newProduct = productRepository.save(product);

                        String[] mContent = drugValue.split(";");

                        for (String singleContent : mContent) {
                            String[] contentData = singleContent.split(":");
                            ProductContentMaster mContentMaster = new ProductContentMaster();
                            int contentLength = contentData.length;
                            ContentMaster cm = contentMasterRepository.findByContentNameAndStatus(contentData[0], true);
                            if (cm == null) {
                                cm = new ContentMaster();
                                cm.setStatus(true);
                                cm.setContentName(contentData[0]);
                                contentMasterRepository.save(cm);
                            }
                            switch (contentLength) {
                                case 1:
                                    mContentMaster.setContentType(contentData[0]);
                                    break;
                                case 2:
                                    if (contentData[1].equalsIgnoreCase("IP")) {
                                        mContentMaster.setContentType(contentData[0] + " " + contentData[1]);
                                    } else {
                                        mContentMaster.setContentType(contentData[0]);
                                        mContentMaster.setContentPower(contentData[1]);
                                    }

                                    break;
                                case 3:
                                    if (contentData[1].equalsIgnoreCase("IP")) {
                                        mContentMaster.setContentType(contentData[0] + " " + contentData[1]);
                                        mContentMaster.setContentPower(contentData[2]);
                                    } else {
                                        mContentMaster.setContentType(contentData[0]);
                                        mContentMaster.setContentPower(contentData[1]);
                                        mContentMaster.setContentPackage(contentData[2]);
                                    }
                                    break;
                                case 4:
                                    if (contentData[1].equalsIgnoreCase("IP")) {
                                        mContentMaster.setContentType(contentData[0] + " " + contentData[1]);
                                        mContentMaster.setContentPower(contentData[2]);
                                        mContentMaster.setContentPackage(contentData[3]);
                                    } else {
                                        mContentMaster.setContentType(contentData[0]);
                                        mContentMaster.setContentPower(contentData[1]);
                                        mContentMaster.setContentPackage(contentData[2]);
                                        mContentMaster.setContentTypeDose(contentData[3]);
                                    }
                                    break;
                                case 5:
                                    if (contentData[1].equalsIgnoreCase("IP")) {
                                        mContentMaster.setContentType(contentData[0] + " " + contentData[1]);
                                        mContentMaster.setContentPower(contentData[2]);
                                        mContentMaster.setContentPackage(contentData[3]);
                                        mContentMaster.setContentTypeDose(contentData[4]);
                                    } else {
                                        mContentMaster.setContentType(contentData[0]);
                                        mContentMaster.setContentPower(contentData[1]);
                                        mContentMaster.setContentPackage(contentData[2]);
                                        mContentMaster.setContentTypeDose(contentData[3]);
                                    }
                                    break;
                            }

                            mContentMaster.setStatus(true);
                            if (users.getBranch() != null)
                                mContentMaster.setBranchId(users.getBranch().getId());
                            if (users.getOutlet() != null)
                                mContentMaster.setOutletId(users.getOutlet().getId());

                            mContentMaster.setProduct(newProduct);
                            productContentMasterRepository.save(mContentMaster);
                        }
                        /*String[] contents = row.getCell(7).getStringCellValue().split("#");
                        for (String content : contents) {
                            String[] mContents = content.split(",");

                            contentMaster = contentMasterRepository.findByContentNameAndStatus(mContents[0], true);
//                            productLogger.error("After ProductContentMaster find Execution... " + sdf.format(new Date()));
                            if (contentMaster == null) {
                                contentMaster = new ContentMaster();
                                contentMaster.setContentName(mContents[0]);
                                contentMaster.setStatus(true);
                                contentMaster = contentMasterRepository.save(contentMaster);
                            }
                                ProductContentMaster productContentMaster = new ProductContentMaster();
                                productContentMaster.setProduct(newProduct);
                                productContentMaster.setContentType(mContents[0]);
                                if (mContents.length > 1)
                                    productContentMaster.setContentPower(mContents[1]);
                                if (mContents.length > 2)
                                    productContentMaster.setContentPackage(mContents[2]);
                                productContentMaster.setStatus(true);
                                productContentMaster.setBranchId(users.getBranch()!=null?users.getBranch().getId():null);
                                productContentMasterRepository.save(productContentMaster);

                        }*/


                        productLogger.error("After Successfully Saving Product " + sdf.format(new Date()));
                        productLogger.error("Product : " + row.getCell(productName).toString() + " Saved Successfully!" + sdf.format(new Date()));

                       /* if(unitname!=null){
                            mUnit= unitsRepository.findFirstByUnitNameIgnoreCase(unitname);
                            if(mUnit==null){
                                mUnit=new Units();
                                mUnit.setUnitName(unitname);
                                mUnit.setStatus(true);
                                mUnit.setBranch(branch);
                                mUnit.setOutlet(outlet);
                                mUnit.setCreatedBy(users.getId());
                                mUnit.setCreatedAt(LocalDateTime.now());
                            }
                            Units u= unitsRepository.save(mUnit);
                            if(u!=null){
                                ProductUnitPacking productUnitPacking=new ProductUnitPacking();
                                productUnitPacking.setUnits(u);
                                productUnitPacking.setStatus(true);
                                productUnitPacking.setCreatedBy(users.getId());
                                productUnitPacking.setCreatedAt(LocalDateTime.now());

                            }
                        }*/

                        productLogger.error("Before High Units Find Execution... " + sdf.format(new Date()));
                        double mFsrai = 0.0, mFsrmh = 0.0, mCsrai = 0.0, mCsrmh = 0.0;
                        if (highunitname != null && !highunitname.isEmpty()) {
                            if (israte.equalsIgnoreCase("H")) {
                                mFsrai = fsrai;
                                mFsrmh = fsrmh;
                                mCsrai = csrai;
                                mCsrmh = csrmh;
                            } else if (israte.equalsIgnoreCase("M")) {
                                mFsrai = fsrai * highunitconversion;
                                mFsrmh = fsrmh * highunitconversion;
                                mCsrai = csrai * highunitconversion;
                                mCsrmh = csrmh * highunitconversion;
                            } else if (israte.equalsIgnoreCase("L")) {
                                mFsrai = fsrai * (highunitconversion * mediumunitconversion * lowestUnitConversion);
                                mFsrmh = fsrmh * (highunitconversion * mediumunitconversion * lowestUnitConversion);
                                mCsrai = csrai * (highunitconversion * mediumunitconversion * lowestUnitConversion);
                                mCsrmh = csrmh * (highunitconversion * mediumunitconversion * lowestUnitConversion);
                            }
                            Units unit = unitsRepository.findFirstByUnitNameIgnoreCase(highunitname);
                            productLogger.error("After High Units Find Execution... " + sdf.format(new Date()));
                            if (unit == null) {
                                unit = new Units();
                                unit.setUnitName(highunitname);
                                unit.setUnitCode(highunitname);
                                unit.setBranch(branch);
                                unit.setOutlet(outlet);
                                unit.setStatus(true);
                                unit.setCreatedBy(users.getId());
                                productLogger.error("Before Saving Units  " + sdf.format(new Date()));
                                unitsRepository.save(unit);
                                productLogger.error("After Successfully Saving Units  " + sdf.format(new Date()));
                                productLogger.error("New unit : " + highunitname + " found and saved!" + sdf.format(new Date()));
                            }
                            ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                            productUnitPacking.setUnits(unit);
                            productUnitPacking.setUnitConversion(highunitconversion);
                            productUnitPacking.setIsNegativeStocks(false);
                            productUnitPacking.setFsrai(mFsrai);
                            productUnitPacking.setFsrmh(mFsrmh);
                            productUnitPacking.setCsrai(mCsrai);
                            productUnitPacking.setCsrmh(mCsrmh);
                            productUnitPacking.setStatus(true);
                            productUnitPacking.setProduct(newProduct);
                            productUnitPacking.setCreatedBy(users.getId());
                            productUnitPacking.setBrand(mBrandMaster);
                            productLogger.error("Before Saving productUnitPacking  " + sdf.format(new Date()));
                            productUnitRepository.save(productUnitPacking);
                            productLogger.error("After Successfully Saving productUnitPacking  " + sdf.format(new Date()));
                            productLogger.error(s + "Product Unit : " + highunitname + " Saved Successfully!" + sdf.format(new Date()));

                        }

                        if (mediumunitname != null && !mediumunitname.isEmpty()) {

                            if (israte.equalsIgnoreCase("H")) {
                                mFsrai = fsrai / highunitconversion;
                                mFsrmh = fsrmh / highunitconversion;
                                mCsrai = csrai / highunitconversion;
                                mCsrmh = csrmh / highunitconversion;
                            } else if (israte.equalsIgnoreCase("M")) {
                                mFsrai = fsrai;
                                mFsrmh = fsrmh;
                                mCsrai = csrai;
                                mCsrmh = csrmh;
                            } else if (israte.equalsIgnoreCase("L")) {
                                mFsrai = fsrai * (mediumunitconversion * lowestUnitConversion);
                                mFsrmh = fsrmh * (mediumunitconversion * lowestUnitConversion);
                                mCsrai = csrai * (mediumunitconversion * lowestUnitConversion);
                                mCsrmh = csrmh * (mediumunitconversion * lowestUnitConversion);
                            }

                            Units unit = unitsRepository.findFirstByUnitNameIgnoreCase(mediumunitname);
                            productLogger.error("After High Units Find Execution... " + sdf.format(new Date()));
                            if (unit == null) {
                                unit = new Units();
                                unit.setUnitName(mediumunitname);
                                unit.setUnitCode(mediumunitname);
                                unit.setBranch(branch);
                                unit.setOutlet(outlet);
                                unit.setStatus(true);
                                unit.setCreatedBy(users.getId());
                                productLogger.error("Before Saving Units  " + sdf.format(new Date()));
                                unitsRepository.save(unit);
                                productLogger.error("After Successfully Saving Units  " + sdf.format(new Date()));
                                productLogger.error("New unit : " + mediumunitname + " found and saved!" + sdf.format(new Date()));
                            }
                            ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                            productUnitPacking.setUnits(unit);
                            productUnitPacking.setUnitConversion(mediumunitconversion);
                            productUnitPacking.setIsNegativeStocks(false);
                            productUnitPacking.setFsrai(mFsrai);
                            productUnitPacking.setFsrmh(mFsrmh);
                            productUnitPacking.setCsrai(mCsrai);
                            productUnitPacking.setCsrmh(mCsrmh);
                            productUnitPacking.setStatus(true);
                            productUnitPacking.setProduct(newProduct);
                            productUnitPacking.setCreatedBy(users.getId());
                            productUnitPacking.setBrand(mBrandMaster);
                            productLogger.error("Before Saving productUnitPacking  " + sdf.format(new Date()));
                            productUnitRepository.save(productUnitPacking);
                            productLogger.error("After Successfully Saving productUnitPacking  " + sdf.format(new Date()));
                            productLogger.error(s + "Product Unit : " + mediumunitname + " Saved Successfully!" + sdf.format(new Date()));

                        }
                        if (lowestunitname != null && !lowestunitname.isEmpty()) {

                            if (israte.equalsIgnoreCase("H")) {
                                mFsrai = fsrai / (highunitconversion * mediumUnitConversion * lowestUnitConversion);
                                mFsrmh = fsrmh / (highunitconversion * mediumUnitConversion * lowestUnitConversion);
                                mCsrai = csrai / (highunitconversion * mediumUnitConversion * lowestUnitConversion);
                                mCsrmh = csrmh / (highunitconversion * mediumUnitConversion * lowestUnitConversion);
                            } else if (israte.equalsIgnoreCase("M")) {
                                mFsrai = fsrai / (mediumunitconversion * lowestUnitConversion);
                                mFsrmh = fsrmh / (mediumunitconversion * lowestUnitConversion);
                                mCsrai = csrai / (mediumunitconversion * lowestUnitConversion);
                                mCsrmh = csrmh / (mediumunitconversion * lowestUnitConversion);
                            } else if (israte.equalsIgnoreCase("L")) {
                                mFsrai = fsrai;
                                mFsrmh = fsrmh;
                                mCsrai = csrai;
                                mCsrmh = csrmh;
                            }


                            Units unit = unitsRepository.findFirstByUnitNameIgnoreCase(lowestunitname);
                            productLogger.error("After High Units Find Execution... " + sdf.format(new Date()));
                            if (unit == null) {
                                unit = new Units();
                                unit.setUnitName(lowestunitname);
                                unit.setUnitCode(lowestunitname);
                                unit.setBranch(branch);
                                unit.setOutlet(outlet);
                                unit.setStatus(true);
                                unit.setCreatedBy(users.getId());
                                productLogger.error("Before Saving Units  " + sdf.format(new Date()));
                                unitsRepository.save(unit);
                                productLogger.error("After Successfully Saving Units  " + sdf.format(new Date()));
                                productLogger.error("New unit : " + lowestunitname + " found and saved!" + sdf.format(new Date()));
                            }
                            ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                            productUnitPacking.setUnits(unit);
                            productUnitPacking.setUnitConversion(lowestunitconversion);
                            productUnitPacking.setIsNegativeStocks(false);
                            productUnitPacking.setFsrai(mFsrai);
                            productUnitPacking.setFsrmh(mFsrmh);
                            productUnitPacking.setCsrai(mCsrai);
                            productUnitPacking.setCsrmh(mCsrmh);
                            productUnitPacking.setStatus(true);
                            productUnitPacking.setProduct(newProduct);
                            productUnitPacking.setCreatedBy(users.getId());
                            productUnitPacking.setBrand(mBrandMaster);
                            productLogger.error("Before Saving productUnitPacking  " + sdf.format(new Date()));
                            productUnitRepository.save(productUnitPacking);
                            productLogger.error("After Successfully Saving productUnitPacking  " + sdf.format(new Date()));
                            productLogger.error(s + "Product Unit : " + lowestunitname + " Saved Successfully!" + sdf.format(new Date()));

                        }

                    } else {
                        productLogger.error("Product : " + productname + " is already available!");
                    }


                    // No.of rows end
                }

                responseObject.setResponseStatus(200);
                responseObject.setMessage("Products Imported Successfully");
            } else {
                responseObject.setResponseStatus(400);
                responseObject.setMessage("Product import failed!");
            }
        } catch (Exception x) {
            responseObject.setResponseStatus(400);
            responseObject.setMessage("Product import failed!");

            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Import:" + exceptionAsString);

        }


        return responseObject;
    }

    public Object importEthiqProduct(MultipartHttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        ContentMaster contentMaster;

        try {
            MultipartFile excelFile = request.getFile("productfile");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            if (excelFile != null) {
                XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
                XSSFSheet sheet = workbook.getSheetAt(0);
                productLogger.error("Total Rows : " + sheet.getPhysicalNumberOfRows() + sdf.format(new Date()));
                for (int s = 1; s < sheet.getPhysicalNumberOfRows(); s++) {
                    productLogger.error("Record:" + s + " Current time: " + sdf.format(new Date()));
                    Product product;
                    Product newProduct;
                    XSSFRow row = sheet.getRow(s);
                    Outlet outlet = users.getOutlet();
                    productLogger.error("Before Product Find Execution... " + sdf.format(new Date()));

                    String pcode = row.getCell(0).getStringCellValue();

                    product = productRepository.findFirstByProductCodeAndProductNameAndOutletId(pcode, row.getCell(1).toString(), outlet.getId());

                    productLogger.error("After Product Find Execution... " + sdf.format(new Date()));
                    if (product == null) {
                        product = new Product();
                        if (users.getBranch() != null) branch = users.getBranch();
                        product.setBranch(branch);
                        product.setOutlet(outlet);
                        product.setProductName(row.getCell(1).toString());
                        product.setProductCode(pcode);
                        product.setIsSerialNumber(false);
                        product.setIsBatchNumber(true);
                        product.setIsInventory(true);
                        product.setStatus(true);
                        product.setIsDelete(true);
                        product.setCreatedBy(users.getId());
                        PackingMaster mPackingMaster = null;
                        Group mGroupMaster = null;
                        Brand mBrandMaster = null;
                        productLogger.error("Before Brand Find Execution... " + sdf.format(new Date()));

                        mBrandMaster = brandRepository.findFirstByBrandName(row.getCell(5).toString());
                        productLogger.error("After Brand Find Execution... " + sdf.format(new Date()));
                        if (mBrandMaster == null) {
                            mBrandMaster = new Brand();
                            mBrandMaster.setBrandName(row.getCell(5).toString());
                            mBrandMaster.setBranch(branch);
                            mBrandMaster.setOutlet(outlet);
                            mBrandMaster.setStatus(true);
                            mBrandMaster.setCreatedBy(users.getId());
                            productLogger.error("Before Saving Brand  " + sdf.format(new Date()));
                            brandRepository.save(mBrandMaster);
                            productLogger.error("After Succesfully Saving Brand  " + sdf.format(new Date()));

                        }

                        product.setBrand(mBrandMaster);
                        productLogger.error("Before Packing Find Execution... " + sdf.format(new Date()));
                        mPackingMaster = packingMasterRepository.findFirstByPackNameIgnoreCase(row.getCell(2).toString());
                        productLogger.error("After Packing Find Execution... " + sdf.format(new Date()));
                        if (mPackingMaster == null) {
                            mPackingMaster = new PackingMaster();
                            mPackingMaster.setPackName(row.getCell(2).toString());
                            mPackingMaster.setBranch(branch);
                            mPackingMaster.setOutlet(outlet);
                            mPackingMaster.setStatus(true);
                            mPackingMaster.setCreatedBy(users.getId());
                            productLogger.error("Before Saving Packing  " + sdf.format(new Date()));
                            packingMasterRepository.save(mPackingMaster);
                            productLogger.error("After Succesfully Saving Packing  " + sdf.format(new Date()));

                        }
                        product.setPackingMaster(mPackingMaster);
                        productLogger.error("Before Group Find Execution... " + sdf.format(new Date()));
                        if (row.getCell(6) != null) {
                            mGroupMaster = groupRepository.findFirstByGroupNameIgnoreCase(row.getCell(6).getRawValue());
                            if (mGroupMaster == null) {
                                mGroupMaster = new Group();
                                mGroupMaster.setGroupName(row.getCell(6).toString());
                                mGroupMaster.setBranch(branch);
                                mGroupMaster.setOutlet(outlet);
                                mGroupMaster.setStatus(true);
                                mGroupMaster.setCreatedBy(users.getId());
                                productLogger.error("Before Saving Group  " + sdf.format(new Date()));
                                groupRepository.save(mGroupMaster);
                                productLogger.error("After Succesfully Saving Group  " + sdf.format(new Date()));
                            }
                            product.setGroup(mGroupMaster);
                        } else
                            product.setGroup(null);


                        productLogger.error("Before ProductHsn Find Execution... " + sdf.format(new Date()));
                        ProductHsn productHsn = productHsnRepository.findByHsnNumber(row.getCell(8).getRawValue());
                        productLogger.error("After ProductHsn Find Execution... " + sdf.format(new Date()));
                        if (productHsn == null) {
                            productHsn = new ProductHsn();
                            productHsn.setHsnNumber(row.getCell(8).getRawValue());
                            productHsn.setIgst(Double.valueOf(row.getCell(11).toString()));
                            productHsn.setSgst(Double.valueOf(row.getCell(9).toString()));
                            productHsn.setCgst(Double.valueOf(row.getCell(10).toString()));
                            productHsn.setBranch(branch);
                            productHsn.setOutlet(outlet);
                            productHsn.setStatus(true);
                            productHsn.setCreatedBy(users.getId());
                            productLogger.error("Before Saving ProductHsn  " + sdf.format(new Date()));
                            productHsnRepository.save(productHsn);
                            productLogger.error("After Succesfully Saving ProductHsn  " + sdf.format(new Date()));
                        }
                        product.setProductHsn(productHsn);
                        product.setTaxType("Taxable");


                        String igst = row.getCell(11).getRawValue();
                        productLogger.error("Before TaxMaster Find Execution... " + sdf.format(new Date()));
                        TaxMaster taxMaster = taxMasterRepository.findDuplicateGSTWithOutlet(outlet.getId(), igst, true);
                        productLogger.error("After TaxMaster Find Execution... " + sdf.format(new Date()));
/*
                        TaxMaster taxMaster;

                        if (branch != null)
                            taxMaster = taxMasterRepository.findDuplicateGSTWithBranch(outlet.getId(), branch.getId(), row.getCell(12).toString(), true);
                        else
                            taxMaster = taxMasterRepository.findDuplicateGSTWithOutlet(outlet.getId(), row.getCell(12).toString(), true);

                        if (taxMaster == null) {
                            taxMaster = new TaxMaster();
                            taxMaster.setGst_per(row.getCell(12).toString());
                            taxMaster.setIgst(Double.valueOf(row.getCell(12).toString()));
                            taxMaster.setCgst(Double.valueOf(row.getCell(11).toString()));
                            taxMaster.setSgst(Double.valueOf(row.getCell(10).toString()));
                            taxMaster.setSratio(Double.valueOf("50"));
                            taxMaster.setCreatedBy(outlet.getId());
                            taxMaster.setCreatedAt(LocalDateTime.now());
                            taxMaster.setStatus(true);
                            taxMaster.setOutlet(outlet);
                            if (branch != null)
                                taxMaster.setBranch(branch);

                            taxMasterRepository.save(taxMaster);

                        }
                        product.setTaxMaster(taxMaster);*/

                        product.setTaxMaster(taxMaster);
                        product.setIgst(Double.valueOf(row.getCell(11).toString()));
                        product.setSgst(Double.valueOf(row.getCell(9).toString()));
                        product.setCgst(Double.valueOf(row.getCell(10).toString()));
                        productLogger.error("Before Saving Product " + sdf.format(new Date()));
                        newProduct = productRepository.save(product);

                        String[] contents = row.getCell(7).getStringCellValue().split("#");
                        for (String content : contents) {
                            String[] mContents = content.split(",");

                            contentMaster = contentMasterRepository.findByContentNameAndStatus(mContents[0], true);
//                            productLogger.error("After ProductContentMaster find Execution... " + sdf.format(new Date()));
                            if (contentMaster == null) {
                                contentMaster = new ContentMaster();
                                contentMaster.setContentName(mContents[0]);
                                contentMaster.setStatus(true);
                                contentMaster = contentMasterRepository.save(contentMaster);
                            }
                            ProductContentMaster productContentMaster = new ProductContentMaster();
                            productContentMaster.setProduct(newProduct);
                            productContentMaster.setContentType(mContents[0]);
                            if (mContents.length > 1)
                                productContentMaster.setContentPower(mContents[1]);
                            if (mContents.length > 2)
                                productContentMaster.setContentPackage(mContents[2]);
                            productContentMaster.setStatus(true);
                            productContentMaster.setBranchId(users.getBranch() != null ? users.getBranch().getId() : null);
                            productContentMasterRepository.save(productContentMaster);

                        }


                        productLogger.error("After Successfully Saving Product " + sdf.format(new Date()));
                        productLogger.error("Product : " + row.getCell(1).toString() + " Saved Successfully!" + sdf.format(new Date()));

                        productLogger.error("Before Units Find Execution... " + sdf.format(new Date()));
                        Units unit = unitsRepository.findFirstByUnitNameIgnoreCase(row.getCell(4).toString());
                        productLogger.error("After Units Find Execution... " + sdf.format(new Date()));
                        if (unit == null) {
                            unit = new Units();
                            unit.setUnitName(row.getCell(4).toString());
                            unit.setUnitCode(row.getCell(4).toString());
                            unit.setBranch(branch);
                            unit.setOutlet(outlet);
                            unit.setStatus(true);
                            unit.setCreatedBy(users.getId());
                            productLogger.error("Before Saving Units  " + sdf.format(new Date()));
                            unitsRepository.save(unit);
                            productLogger.error("After Successfully Saving Units  " + sdf.format(new Date()));
                            productLogger.error("New unit : " + row.getCell(4).toString() + " found and saved!" + sdf.format(new Date()));
                        }
                        ProductUnitPacking productUnitPacking = new ProductUnitPacking();
                        productUnitPacking.setUnits(unit);
                        productUnitPacking.setUnitConversion(Double.valueOf(row.getCell(3).toString()));
                        productUnitPacking.setIsNegativeStocks(false);
                        productUnitPacking.setStatus(true);
                        productUnitPacking.setProduct(newProduct);
                        productUnitPacking.setCreatedBy(users.getId());
                        productUnitPacking.setBrand(mBrandMaster);
                        productLogger.error("Before Saving productUnitPacking  " + sdf.format(new Date()));
                        productUnitRepository.save(productUnitPacking);
                        productLogger.error("After Successfully Saving productUnitPacking  " + sdf.format(new Date()));
                        productLogger.error(s + "Product Unit : " + row.getCell(4).toString() + " Saved Successfully!" + sdf.format(new Date()));


                    } else {
                        productLogger.error("Product : " + row.getCell(1).toString() + " is already available!");
                    }


                    // No.of rows end
                }

                responseObject.setResponseStatus(200);
                responseObject.setMessage("Products Imported Successfully");
            } else {
                responseObject.setResponseStatus(400);
                responseObject.setMessage("Product import failed!");
            }
        } catch (Exception x) {
            responseObject.setResponseStatus(400);
            responseObject.setMessage("Product import failed!");

            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Import:" + exceptionAsString);

        }


        return responseObject;
    }


    public Object importContentMaster(MultipartHttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        try {
            MultipartFile excelFile = request.getFile("contentfile");
            if (excelFile != null) {
                XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
                XSSFSheet sheet = workbook.getSheetAt(0);
                int srNo = 0, mfg = 1, productName = 2, contentName = 3, productCode = 4;

                for (int s = 1; s < sheet.getPhysicalNumberOfRows(); s++) {

                    Product product = null;
                    Product newProduct;
                    XSSFRow row = sheet.getRow(s);
                    Outlet outlet = users.getOutlet();
                    int mSrNo = (int) row.getCell(srNo).getNumericCellValue();
                    String mMFG = row.getCell(mfg).getStringCellValue();
                    String mProductName = row.getCell(productName).getStringCellValue();
                    String mContentName = row.getCell(contentName).getStringCellValue();
                    String mProductCode = row.getCell(productCode).getStringCellValue();


                    System.out.println("Before,Product Code:" + outlet.getId());
                    product = productRepository.findFirstByProductCode(mProductCode);
//                    product = productRepository.findFirstByProductCodeAndProductNameAndOutletId(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue(), outlet.getId());
//                    System.out.println("After");

                    if (product != null) {
                        System.out.println("Product Found at " + s);
//                        product = new Product();
                        if (users.getBranch() != null) branch = users.getBranch();

                        String[] mContent = mContentName.split(";");

                        for (String singleContent : mContent) {
                            String[] contentData = singleContent.split(":");
                            ProductContentMaster contentMaster = new ProductContentMaster();
                            int contentLength = contentData.length;
                            switch (contentLength) {
                                case 1:
                                    contentMaster.setContentType(contentData[0]);
                                    break;
                                case 2:
                                    if (contentData[1].equalsIgnoreCase("IP")) {
                                        contentMaster.setContentType(contentData[0] + " " + contentData[1]);
                                    } else {
                                        contentMaster.setContentType(contentData[0]);
                                        contentMaster.setContentPower(contentData[1]);
                                    }

                                    break;
                                case 3:
                                    if (contentData[1].equalsIgnoreCase("IP")) {
                                        contentMaster.setContentType(contentData[0] + " " + contentData[1]);
                                        contentMaster.setContentPower(contentData[2]);
                                    } else {
                                        contentMaster.setContentType(contentData[0]);
                                        contentMaster.setContentPower(contentData[1]);
                                        contentMaster.setContentPackage(contentData[2]);
                                    }
                                    break;
                                case 4:
                                    if (contentData[1].equalsIgnoreCase("IP")) {
                                        contentMaster.setContentType(contentData[0] + " " + contentData[1]);
                                        contentMaster.setContentPower(contentData[2]);
                                        contentMaster.setContentPackage(contentData[3]);
                                    } else {
                                        contentMaster.setContentType(contentData[0]);
                                        contentMaster.setContentPower(contentData[1]);
                                        contentMaster.setContentPackage(contentData[2]);
                                        contentMaster.setContentTypeDose(contentData[3]);
                                    }
                                    break;
                                case 5:
                                    if (contentData[1].equalsIgnoreCase("IP")) {
                                        contentMaster.setContentType(contentData[0] + " " + contentData[1]);
                                        contentMaster.setContentPower(contentData[2]);
                                        contentMaster.setContentPackage(contentData[3]);
                                        contentMaster.setContentTypeDose(contentData[4]);
                                    } else {
                                        contentMaster.setContentType(contentData[0]);
                                        contentMaster.setContentPower(contentData[1]);
                                        contentMaster.setContentPackage(contentData[2]);
                                        contentMaster.setContentTypeDose(contentData[3]);
                                    }
                                    break;
                            }

                            contentMaster.setStatus(true);
                            if (users.getBranch() != null)
                                contentMaster.setBranchId(users.getBranch().getId());
                            if (users.getOutlet() != null)
                                contentMaster.setOutletId(users.getOutlet().getId());

                            contentMaster.setProduct(product);
                            productContentMasterRepository.save(contentMaster);
                        }

                    } else {
                        System.out.println("Product : " + row.getCell(1).toString() + " not available!");
                    }


                    // No.of rows end
                }

                responseObject.setResponseStatus(HttpStatus.OK.value());
                responseObject.setMessage("Product content import completed successfully!");
            } else {
                responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseObject.setMessage("Stock Import Failed!");
            }
        } catch (Exception x) {
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Product content import failed!");
            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Importing Opening Stock at:" + exceptionAsString);
        }


        return responseObject;
    }

    public Object importProductStock(MultipartHttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        try {
            MultipartFile excelFile = request.getFile("productstockfile");
            if (excelFile != null) {
                XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
                XSSFSheet sheet = workbook.getSheetAt(0);
                int productCodeIndex = 0, productNameIndex = 1, currentStockIndex = 2, stockUnitIndex = 3, costingPriceIndex = 4, mrpRateIndex = 5, fsrMhIndex = 7, fsrAiIndex = 8, csrMhIndex = 9, csrAiIndex = 10, batchNoIndex = 12, mfgIndex = 13, expIndex = 14, purchasePriceIndex = 6, salesPriceIndex = 11;
                for (int s = 1; s < sheet.getPhysicalNumberOfRows(); s++) {

                    Product product = null;
                    Product newProduct;
                    XSSFRow row = sheet.getRow(s);
                    Outlet outlet = users.getOutlet();
                    System.out.println("Before,Product Code:" + outlet.getId());
                    product = productRepository.findFirstByProductCode(row.getCell(productCodeIndex).toString());
                    String productCode = row.getCell(productCodeIndex).getStringCellValue();
                    String productName = row.getCell(productNameIndex).getStringCellValue();

//                    product = productRepository.findFirstByProductCodeAndProductNameAndOutletId(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue(), outlet.getId());
//                    System.out.println("After");

                    if (product != null) {
                        System.out.println("Product Found at " + s);
                        double currentStock = row.getCell(currentStockIndex).getNumericCellValue();
                        String unitName = row.getCell(stockUnitIndex).getStringCellValue();
                        double costingPrice = row.getCell(costingPriceIndex).getNumericCellValue();
                        double mrpRate = row.getCell(mrpRateIndex).getNumericCellValue();
                        double fsrMh = row.getCell(fsrMhIndex).getNumericCellValue();
                        double fsrAi = row.getCell(fsrAiIndex).getNumericCellValue();
                        double csrMh = row.getCell(csrMhIndex).getNumericCellValue();
                        double csrAi = row.getCell(csrAiIndex).getNumericCellValue();
                        String batchNo = row.getCell(batchNoIndex).getStringCellValue();
                        String mfgDate = row.getCell(mfgIndex).getStringCellValue();
                        String expDate = row.getCell(expIndex).getStringCellValue();
                        double purchasePrice = row.getCell(purchasePriceIndex).getNumericCellValue();
                        double salesPrice = row.getCell(salesPriceIndex).getNumericCellValue();

//                        product = new Product();
                        if (users.getBranch() != null) branch = users.getBranch();

                        ProductBatchNo productBatchNo = new ProductBatchNo();

                        if (batchNo.isEmpty()) {
                            System.out.println("No batch availabe, skipped at " + s);
                        } else {

                            productBatchNo.setBatchNo(batchNo);
                            productBatchNo.setMrp(mrpRate);
                            productBatchNo.setSalesRate(salesPrice);
                            productBatchNo.setPurchaseRate(purchasePrice);
                            if (!mfgDate.isEmpty())
                                productBatchNo.setManufacturingDate(LocalDate.parse(mfgDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            if (!expDate.isEmpty())
                                productBatchNo.setExpiryDate(LocalDate.parse(expDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            productBatchNo.setOpeningQty(currentStock);
                            productBatchNo.setStatus(true);
                            productBatchNo.setCreatedBy(users.getId());
                            if (branch != null) productBatchNo.setBranch(branch);
                            productBatchNo.setOutlet(outlet);
                            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
                            productBatchNo.setFiscalYear(fiscalYear);
                            productBatchNo.setProduct(product);
                            productBatchNo.setPackingMaster(product.getPackingMaster());
                            productBatchNo.setGroup(product.getGroup());
                            productBatchNo.setBrand(product.getBrand());
                            productBatchNo.setCosting(costingPrice);
                            List<ProductUnitPacking> unit = productUnitRepository.findByProductIdAndStatus(product.getId(), true);
                            for (ProductUnitPacking pup :
                                    unit) {
                                if (pup.getUnits().getUnitName().equalsIgnoreCase(unitName)) {
                                    productBatchNo.setUnits(pup.getUnits());
                                    break;
                                }
                            }

                            ProductBatchNo newProductBatchNo = productBatchNoRepository.save(productBatchNo);
                            System.out.println("Batch No : " + batchNo + " Saved Successfully!");
                            try {
                                ProductOpeningStocks newOpeningStock = new ProductOpeningStocks();
                                newOpeningStock.setOpeningStocks(currentStock);
                                newOpeningStock.setProduct(product);
                                if (branch != null) newOpeningStock.setBranch(branch);
                                newOpeningStock.setOutlet(outlet);
                                newOpeningStock.setProductBatchNo(newProductBatchNo);
                                newOpeningStock.setFiscalYear(fiscalYear);
                                newOpeningStock.setMrp(mrpRate);
                                newOpeningStock.setPurchaseRate(purchasePrice);
                                newOpeningStock.setSalesRate(salesPrice);
                                newOpeningStock.setStatus(true);
                                if (!mfgDate.isEmpty())
                                    newOpeningStock.setManufacturingDate(LocalDate.parse(mfgDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                if (!expDate.isEmpty())
                                    newOpeningStock.setExpiryDate(LocalDate.parse(expDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                newOpeningStock.setCosting(costingPrice);
                                newOpeningStock.setUnits(productBatchNo.getUnits());
                                newOpeningStock.setPackingMaster(product.getPackingMaster());

                                openingStocksRepository.save(newOpeningStock);
                                System.out.println(s + "Product Name : " + productName + " Opening set Successfully!");
                            } catch (Exception e) {
                                responseObject.setMessage("Error in importing Opening Stock at " + s);
                                responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                                StringWriter sw = new StringWriter();
                                e.printStackTrace(new PrintWriter(sw));
                                String exceptionAsString = sw.toString();
                                productLogger.error("Error in Importing Opening Stock at:" + exceptionAsString);
                            }
                        }
                    } else {
                        System.out.println("Product : " + productName + " not available!");
                    }
                    // No.of rows end
                }

                responseObject.setResponseStatus(200);
                responseObject.setMessage("Stock Imported Successfully");
            } else {
                responseObject.setResponseStatus(400);
                responseObject.setMessage("Stock Import Failed!");
            }
        } catch (Exception x) {
            responseObject.setResponseStatus(400);
            responseObject.setMessage("Stock Import Failed!");
            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Importing Opening Stock at:" + exceptionAsString);
        }


        return responseObject;
    }

    public JsonObject getProductByIdEditNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Product mProduct = productRepository.findByIdAndStatus(productId, true);
        JsonObject result = new JsonObject();
        JsonArray result1 = new JsonArray();
        //   List<ProductUnitPacking> units = productUnitRepository.findByProductIdAndStatus(mProduct.getId(), true);
        JsonObject response = new JsonObject();
        if (mProduct != null) {
            /*DataLockModel dataLockModel = DataLockModel.getInstance();
            if (dataLockModel.isPresent("productMaster_" + mProduct.getId())) {
                result.addProperty("message", "Selected row already in use");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {*/
            //  dataLockModel.addObject("productMaster_" + mProduct.getId(), mProduct);
            response.addProperty("productName", mProduct.getProductName());
            response.addProperty("id", mProduct.getId());
            response.addProperty("description", mProduct.getDescription());
            response.addProperty("productCode", mProduct.getProductCode());
            response.addProperty("isBatchNo", mProduct.getIsBatchNumber());
            response.addProperty("isInventory", mProduct.getIsInventory());
            response.addProperty("isSerialNo", mProduct.getIsSerialNumber());
            response.addProperty("barcodeNo", mProduct.getBarcodeNo());
            response.addProperty("shelfId", mProduct.getShelfId());
            response.addProperty("barcodeSalesQty", mProduct.getBarcodeSalesQty());
            response.addProperty("purchaseRate", mProduct.getPurchaseRate());
            response.addProperty("margin", mProduct.getMarginPer());
            response.addProperty("brandId", mProduct.getBrand() != null ? mProduct.getBrand().getId() : null);
            response.addProperty("packagingId", mProduct.getPackingMaster() != null ? mProduct.getPackingMaster().getId() : null);
            response.addProperty("groupId", mProduct.getGroup() != null ? mProduct.getGroup().getId() : null);
            response.addProperty("subgroupId", mProduct.getSubgroup() != null ? mProduct.getSubgroup().getId() : null);
            response.addProperty("categoryId", mProduct.getCategory() != null ? mProduct.getCategory().getId() : null);
            response.addProperty("subcategoryId", mProduct.getSubcategory() != null ? mProduct.getSubcategory().getId() : null);
            response.addProperty("weight", mProduct.getWeight());
            response.addProperty("weightUnit", mProduct.getWeightUnit());
            response.addProperty("disPer1", mProduct.getDiscountInPer());
            response.addProperty("hsnNo", mProduct.getProductHsn() != null ? mProduct.getProductHsn().getId() : null);
            response.addProperty("tax", mProduct.getTaxMaster() != null ? mProduct.getTaxMaster().getId() : null);
            response.addProperty("taxApplicableDate", mProduct.getApplicableDate() != null ? mProduct.getApplicableDate().toString() : null);
            response.addProperty("taxType", mProduct.getTaxType() != null ? mProduct.getTaxType() : null);
            response.addProperty("igst", mProduct.getIgst() != null ? mProduct.getIgst() : null);
            response.addProperty("cgst", mProduct.getCgst() != null ? mProduct.getCgst() : null);
            response.addProperty("sgst", mProduct.getSgst() != null ? mProduct.getSgst() : null);
            response.addProperty("minStock", mProduct.getMinStock() != null ? mProduct.getMinStock() : 0.0);
            response.addProperty("maxStock", mProduct.getMaxStock() != null ? mProduct.getMaxStock() : 0.0);
            response.addProperty("isWarranty", mProduct.getIsWarrantyApplicable());
            response.addProperty("nodays", mProduct.getWarrantyDays());
            response.addProperty("isCommision", mProduct.getIsCommision());
            response.addProperty("isGVProducts", mProduct.getIsGVProducts());
            response.addProperty("gvOfProducts", mProduct.getGvOfProducts());
            response.addProperty("drugType", mProduct.getDrugType() != null ? mProduct.getDrugType() : null);
            response.addProperty("isMIS", mProduct.getIsMIS() != null ? mProduct.getIsMIS() : false);
            response.addProperty("isGroup", mProduct.getIsGroup() != null ? mProduct.getIsGroup() : false);
            response.addProperty("isFormulation", mProduct.getIsFormulation() != null ? mProduct.getIsFormulation() : false);
            response.addProperty("isCategory", mProduct.getIsCategory() != null ? mProduct.getIsCategory() : false);
            response.addProperty("isSubcategory", mProduct.getIsSubCategory() != null ? mProduct.getIsSubCategory() : false);
            response.addProperty("isPrescription", mProduct.getIsPrescription() != null ? mProduct.getIsPrescription() : false);
            response.addProperty("drugContent", mProduct.getDrugContents() != null ? mProduct.getDrugContents() : "");
            response.addProperty("uploadImage", mProduct.getUploadImage() != null ? mProduct.getUploadImage() : "");

            response.addProperty("isEcom", mProduct.getEcommerceTypeId() != null ? true : false);
            response.addProperty("ecomType", mProduct.getEcommerceTypeId() != null ? mProduct.getEcommerceTypeId().toString() : "");
            response.addProperty("ecomPrice", mProduct.getSellingPrice() != null ? mProduct.getSellingPrice().toString() : "");
            response.addProperty("ecomDiscount", mProduct.getDiscountPer() != null ? mProduct.getDiscountPer().toString() : "");
            response.addProperty("ecomAmount", mProduct.getAmount() != null ? mProduct.getAmount().toString() : "");
            response.addProperty("ecomLoyality", mProduct.getLoyalty() != null ? mProduct.getLoyalty().toString() : "");

            /*response.addProperty("fsrmh", mProduct.getFsrmh() != null ? mProduct.getFsrmh().toString() : "");
            response.addProperty("fsrai", mProduct.getFsrai() != null ? mProduct.getFsrai().toString() : "");
            response.addProperty("csrmh", mProduct.getCsrmh() != null ? mProduct.getCsrmh().toString() : "");
            response.addProperty("csrai", mProduct.getCsrai() != null ? mProduct.getCsrai().toString() : "");
*/
            response.addProperty("imageExists", false);
            if (mProduct.getImage1() != null || mProduct.getImage2() != null || mProduct.getImage3() != null || mProduct.getImage4() != null
                    || mProduct.getImage5() != null)
                response.addProperty("imageExists", true);
            response.addProperty("prevImage1", mProduct.getImage1() != null ? serverUrl + mProduct.getImage1() : "");
            response.addProperty("prevImage2", mProduct.getImage2() != null ? serverUrl + mProduct.getImage2() : "");
            response.addProperty("prevImage3", mProduct.getImage3() != null ? serverUrl + mProduct.getImage3() : "");
            response.addProperty("prevImage4", mProduct.getImage4() != null ? serverUrl + mProduct.getImage4() : "");
            response.addProperty("prevImage5", mProduct.getImage5() != null ? serverUrl + mProduct.getImage5() : "");

//            response.addProperty("drugType",mProduct.getDrugType());
            response.addProperty("productType", mProduct.getProductType());


            List<ProductContentMaster> bankList = productContentMasterRepository.findByProductIdAndStatus(mProduct.getId(), true);
            ;
            for (ProductContentMaster mList : bankList) {
                JsonObject jsonObject_ = new JsonObject();
                jsonObject_.addProperty("id", mList.getId());
                jsonObject_.addProperty("contentType", mList.getContentType());
                jsonObject_.addProperty("content_power", mList.getContentPower());
                jsonObject_.addProperty("content_package", mList.getContentPackage());
                jsonObject_.addProperty("contentTypeDose", mList.getContentTypeDose());

                result1.add(jsonObject_);
            }


            response.add("contentMap", result1);
            /* getting Level A, Level B, Level C and its Units from Product Id */
            JsonArray unitArray = new JsonArray();
            unitArray = getUnitRowsNewProductEdit(mProduct.getId());
            response.add("productrows", unitArray);
            //  array.add(response);
            result.addProperty("messege", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
            // }
        } else {
            result.addProperty("messege", "empty");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            result.add("responseObject", response);
        }
        return result;
    }

    private JsonArray getUnitRowsNewProductEdit(Long id) {
        List<ProductUnitPacking> mUnits = productUnitRepository.findByProductId(id);
        JsonArray unitsArray = new JsonArray();
        for (ProductUnitPacking munitPacking : mUnits) {
            if (munitPacking.getStatus() == true) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", munitPacking.getId());
                mObject.addProperty("selectedLevelA", munitPacking.getLevelA() != null ? munitPacking.getLevelA().getId().toString() : "");
                mObject.addProperty("selectedLevelB", munitPacking.getLevelB() != null ? munitPacking.getLevelB().getId().toString() : "");
                mObject.addProperty("selectedLevelB", munitPacking.getLevelB() != null ? munitPacking.getLevelB().getId().toString() : "");
                mObject.addProperty("selectedLevelC", munitPacking.getLevelC() != null ? munitPacking.getLevelC().getId().toString() : "");
                mObject.addProperty("selectedUnit", munitPacking.getUnits() != null ? munitPacking.getUnits().getId().toString() : "");
                mObject.addProperty("conv", munitPacking.getUnitConversion() != null ? munitPacking.getUnitConversion() : 0);
                mObject.addProperty("unit_marg", munitPacking.getUnitConvMargn() != null ? munitPacking.getUnitConvMargn() : 0);
                mObject.addProperty("mrp", munitPacking.getMrp() != null ? munitPacking.getMrp() : 0);
                mObject.addProperty("pur_rate", munitPacking.getPurchaseRate() != null ? munitPacking.getPurchaseRate() : 0);
//            mObject.addProperty("min_margin", munitPacking.getMinMargin());
                /*mObject.addProperty("rate_1", munitPacking.getMinRateA() != null ? munitPacking.getMinRateA() : 0);
                mObject.addProperty("rate_2", munitPacking.getMinRateB() != null ? munitPacking.getMinRateB() : 0);
                mObject.addProperty("rate_3", munitPacking.getMinRateC() != null ? munitPacking.getMinRateC() : 0);
                mObject.addProperty("rate_3", munitPacking.getMinRateC() != null ? munitPacking.getMinRateC() : 0);
                */
                mObject.addProperty("is_rate", munitPacking.getIsRate() != null ? munitPacking.getIsRate() : false);
                mObject.addProperty("rate_1", munitPacking.getFsrmh() != null ? munitPacking.getFsrmh() : 0);
                mObject.addProperty("rate_2", munitPacking.getFsrai() != null ? munitPacking.getFsrai() : 0);
                mObject.addProperty("rate_3", munitPacking.getCsrmh() != null ? munitPacking.getCsrmh() : 0);
                mObject.addProperty("rate_4", munitPacking.getCsrai() != null ? munitPacking.getCsrai() : 0);
                mObject.addProperty("min_qty", munitPacking.getMinQty() != null ? munitPacking.getMinQty() : 0);
                mObject.addProperty("max_qty", munitPacking.getMaxQty() != null ? munitPacking.getMaxQty() : 0);
//            mObject.addProperty("is_negetive", munitPacking.getIsNegativeStocks());
                mObject.addProperty("is_negetive", munitPacking.getIsNegativeStocks() != null ? munitPacking.getIsNegativeStocks() : false);


                /******** Batch List *****/
                JsonArray batchJsonArray = new JsonArray();
                Long levelaUnit = munitPacking.getLevelA() != null ? munitPacking.getLevelA().getId() : null;
                Long levelbUnit = munitPacking.getLevelB() != null ? munitPacking.getLevelB().getId() : null;
                Long levelcUnit = munitPacking.getLevelC() != null ? munitPacking.getLevelC().getId() : null;
                List<ProductOpeningStocks> openingStocks = openingStocksRepository.findByProductOpening(id, munitPacking.getUnits().getId(), levelaUnit, levelbUnit, levelcUnit);
                for (ProductOpeningStocks mOpeningStocks : openingStocks) {
                    JsonObject mBatchObject = new JsonObject();
                    mBatchObject.addProperty("id", mOpeningStocks.getId());
                    mBatchObject.addProperty("b_no", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getBatchNo() : "");
                    mBatchObject.addProperty("batch_id", mOpeningStocks.getProductBatchNo() != null ? mOpeningStocks.getProductBatchNo().getId().toString() : "");
                    mBatchObject.addProperty("opening_qty", mOpeningStocks.getOpeningStocks());
                    mBatchObject.addProperty("b_free_qty", mOpeningStocks.getFreeOpeningQty() != null ? mOpeningStocks.getFreeOpeningQty().toString() : "");
                    mBatchObject.addProperty("b_mrp", mOpeningStocks.getMrp() != null ? mOpeningStocks.getMrp().toString() : "");
                    mBatchObject.addProperty("b_sale_rate", mOpeningStocks.getSalesRate() != null ? mOpeningStocks.getSalesRate().toString() : "");
                    mBatchObject.addProperty("b_purchase_rate", mOpeningStocks.getPurchaseRate() != null ? mOpeningStocks.getPurchaseRate().toString() : "");
                    mBatchObject.addProperty("b_costing", mOpeningStocks.getCosting());
                    mBatchObject.addProperty("b_expiry", mOpeningStocks.getExpiryDate() != null ? mOpeningStocks.getExpiryDate().toString() : "");
                    mBatchObject.addProperty("b_manufacturing_date", mOpeningStocks.getManufacturingDate() != null ? mOpeningStocks.getManufacturingDate().toString() : "");
                    batchJsonArray.add(mBatchObject);
                }

                mObject.add("batchList", batchJsonArray);
                unitsArray.add(mObject);

            }
        }
        return unitsArray;
    }

    public Object updateProduct_new(MultipartHttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        Product product = productRepository.findByIdAndStatus(Long.parseLong(request.getParameter("productId")), true);
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        product.setProductName(request.getParameter("productName").trim());
        if (paramMap.containsKey("productCode")) product.setProductCode(request.getParameter("productCode"));
        else product.setProductCode("");
        if (paramMap.containsKey("productDescription"))
            product.setDescription(request.getParameter("productDescription"));
        else product.setDescription("");
        product.setStatus(true);
        if (paramMap.containsKey("barcodeNo")) product.setBarcodeNo(request.getParameter("barcodeNo"));
        else product.setBarcodeNo("");
        if (paramMap.containsKey("isSerialNo"))
            product.setIsSerialNumber(Boolean.parseBoolean(request.getParameter("isSerialNo")));
        product.setIsBatchNumber(Boolean.parseBoolean(request.getParameter("isBatchNo")));
        product.setIsInventory(Boolean.parseBoolean(request.getParameter("isInventory")));
        if (paramMap.containsKey("isWarranty")) {
            product.setIsWarrantyApplicable(Boolean.parseBoolean(request.getParameter("isWarranty")));
            if (Boolean.parseBoolean(request.getParameter("isWarranty"))) {
                product.setWarrantyDays(Integer.parseInt(request.getParameter("nodays")));
            } else {
                product.setWarrantyDays(0);
            }
        }
        if (paramMap.containsKey("drugType")) product.setDrugType(request.getParameter("drugType"));
        if (paramMap.containsKey("drug_contents")) product.setDrugContents(request.getParameter("drug_contents"));
        if (paramMap.containsKey("productType")) product.setProductType(request.getParameter("productType"));
        if (paramMap.containsKey("isPrescription"))
            product.setIsPrescription(Boolean.parseBoolean(request.getParameter("isPrescription")));
        if (paramMap.containsKey("isMIS")) product.setIsMIS(Boolean.parseBoolean(request.getParameter("isMIS")));
        if (paramMap.containsKey("isGroup")) product.setIsGroup(Boolean.parseBoolean(request.getParameter("isGroup")));
        if (paramMap.containsKey("isFormulation"))
            product.setIsFormulation(Boolean.parseBoolean(request.getParameter("isFormulation")));
        if (paramMap.containsKey("isCategory"))
            product.setIsCategory(Boolean.parseBoolean(request.getParameter("isCategory")));
        if (paramMap.containsKey("isSubCategory"))
            product.setIsSubCategory(Boolean.parseBoolean(request.getParameter("isSubCategory")));
        if (request.getFile("uploadImage") != null) {
            MultipartFile image = request.getFile("uploadImage");
            fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
            if (imagePath != null) {
                product.setUploadImage(File.separator + "uploads" + File.separator + imagePath);
            }
        }
        if (paramMap.containsKey("isCommision"))
            product.setIsCommision(Boolean.parseBoolean(request.getParameter("isCommision")));
        if (paramMap.containsKey("isGVProducts")) {
            product.setIsGVProducts(Boolean.parseBoolean(request.getParameter("isGVProducts")));
            if (Boolean.parseBoolean(request.getParameter("isGVProducts"))) {
                if (paramMap.containsKey("gvOfProducts")) product.setGvOfProducts(request.getParameter("gvOfProducts"));
            }
        }
        product.setCreatedBy(users.getId());
        /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
        if (paramMap.containsKey("shelfId")) product.setShelfId(request.getParameter("shelfId"));
        else product.setShelfId("");
        if (paramMap.containsKey("barcodeSaleQuantity"))
            product.setBarcodeSalesQty(Double.parseDouble(request.getParameter("barcodeSaleQuantity")));
        if (paramMap.containsKey("margin")) product.setMarginPer(Double.parseDouble(request.getParameter("margin")));
        else product.setMarginPer(0.0);
        PackingMaster mPackingMaster = null;
        Group mGroupMaster = null;
        Brand mBrandMaster = null;
        Category mCategoryMaster = null;
        Subcategory msubCategory = null;
        Subgroup mSubgroup = null;
        if (paramMap.containsKey("brandId")) {
            mBrandMaster = brandRepository.findByIdAndStatus(Long.parseLong(request.getParameter("brandId")), true);
            product.setBrand(mBrandMaster);
        } else {
            product.setBrand(null);
        }
        if (paramMap.containsKey("packagingId")) {
            mPackingMaster = packingMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("packagingId")), true);
            product.setPackingMaster(mPackingMaster);
        } else {
            product.setPackingMaster(null);
        }
        if (paramMap.containsKey("groupId")) {
            mGroupMaster = groupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("groupId")), true);
            product.setGroup(mGroupMaster);
        } else {
            product.setGroup(null);
        }
        if (paramMap.containsKey("subgroupId")) {
            mSubgroup = subgroupRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subgroupId")), true);
            product.setSubgroup(mSubgroup);
        } else {
            product.setSubgroup(null);
        }
        if (paramMap.containsKey("categoryId")) {
            mCategoryMaster = categoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("categoryId")), true);
            product.setCategory(mCategoryMaster);
        } else {
            product.setCategory(null);
        }
        if (paramMap.containsKey("subcategoryId")) {
            msubCategory = subcategoryRepository.findByIdAndStatus(Long.parseLong(request.getParameter("subcategoryId")), true);
            product.setSubcategory(msubCategory);
        } else {
            product.setSubcategory(null);
        }
        if (paramMap.containsKey("weight")) product.setWeight(Double.parseDouble(request.getParameter("weight")));
        else product.setWeight(0.0);

        if (paramMap.containsKey("weightUnit")) product.setWeightUnit(request.getParameter("weightUnit"));
        else product.setWeightUnit("");
        if (paramMap.containsKey("disPer1"))
            product.setDiscountInPer(Double.parseDouble(request.getParameter("disPer1")));
        else product.setDiscountInPer(0.0);
        if (paramMap.containsKey("hsnNo")) {
            ProductHsn productHsn = productHsnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("hsnNo")), true);
            if (productHsn != null) {
                product.setProductHsn(productHsn);
            }
        }
        if (paramMap.containsKey("tax")) {
            LocalDate applicableDate = null;
            TaxMaster taxMaster = taxMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("tax")), true);
            if (taxMaster != null) {
                product.setTaxMaster(taxMaster);
            }
            if (paramMap.containsKey("taxApplicableDate"))
                product.setApplicableDate(LocalDate.parse(request.getParameter("taxApplicableDate")));
          /* inserting into ProductTax Master to maintain tax information of Product,
            /***** End of inserting into ProductTax Master  *****/
        }
        if (paramMap.containsKey("taxType")) product.setTaxType(request.getParameter("taxType"));
        if (paramMap.containsKey("igst")) product.setIgst(Double.parseDouble(request.getParameter("igst")));
        if (paramMap.containsKey("cgst")) product.setCgst(Double.parseDouble(request.getParameter("cgst")));
        if (paramMap.containsKey("sgst")) product.setSgst(Double.parseDouble(request.getParameter("sgst")));
        if (paramMap.containsKey("minStock")) product.setMinStock(Double.parseDouble(request.getParameter("minStock")));
        if (paramMap.containsKey("maxStock")) product.setMaxStock(Double.parseDouble(request.getParameter("maxStock")));
        if (paramMap.containsKey("cantentMapData")) {
            insertIntoProductContentMaster(product, request);
//            else updateShippingDetails(mLedger, request);
        }
       /* if (paramMap.containsKey("fsrmh")) product.setFsrmh(Double.parseDouble(request.getParameter("fsrmh")));
        if (paramMap.containsKey("fsrai")) product.setFsrai(Double.parseDouble(request.getParameter("fsrai")));
        if (paramMap.containsKey("csrmh")) product.setCsrmh(Double.parseDouble(request.getParameter("csrmh")));
        if (paramMap.containsKey("csrai")) product.setCsrai(Double.parseDouble(request.getParameter("csrai")));
*/
        /**** END ****/

        product.setEcommerceTypeId(null);
        product.setSellingPrice(null);
        product.setDiscountPer(null);
        product.setAmount(null);
        product.setLoyalty(null);
        product.setEcommerceTypeId(null);
        if (paramMap.containsKey("ecomType") && !request.getParameter("ecomType").isEmpty())
            product.setEcommerceTypeId(Long.valueOf(request.getParameter("ecomType")));
        if (paramMap.containsKey("ecomPrice") && !request.getParameter("ecomPrice").isEmpty())
            product.setSellingPrice(Double.parseDouble(request.getParameter("ecomPrice")));
        if (paramMap.containsKey("ecomDiscount") && !request.getParameter("ecomDiscount").isEmpty())
            product.setDiscountPer(Double.parseDouble(request.getParameter("ecomDiscount")));
        if (paramMap.containsKey("ecomAmount") && !request.getParameter("ecomAmount").isEmpty())
            product.setAmount(Double.parseDouble(request.getParameter("ecomAmount")));
        if (paramMap.containsKey("ecomLoyality") && !request.getParameter("ecomLoyality").isEmpty())
            product.setLoyalty(Double.parseDouble(request.getParameter("ecomLoyality")));
        if (request.getFile("image1") != null) {
            if (product.getImage1() != null) {
                System.out.println("product.getImage1() " + product.getImage1());
                File oldFile = new File("." + product.getImage1());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage1(null);
            }
            MultipartFile image = request.getFile("image1");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage1("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }
        if (request.getFile("image2") != null) {
            if (product.getImage2() != null) {
                System.out.println("product.getImage2() " + product.getImage2());
                File oldFile = new File("." + product.getImage2());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage2(null);
            }
            MultipartFile image = request.getFile("image2");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage2("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }
        if (request.getFile("image3") != null) {
            if (product.getImage3() != null) {
                System.out.println("product.getImage3() " + product.getImage3());
                File oldFile = new File("." + product.getImage3());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage3(null);
            }
            MultipartFile image = request.getFile("image3");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage3("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }
        if (request.getFile("image4") != null) {
            if (product.getImage4() != null) {
                System.out.println("product.getImage4() " + product.getImage4());
                File oldFile = new File("." + product.getImage4());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage4(null);
            }
            MultipartFile image = request.getFile("image4");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage4("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }
        if (request.getFile("image5") != null) {
            if (product.getImage5() != null) {
                System.out.println("product.getImage5() " + product.getImage5());
                File oldFile = new File("." + product.getImage5());
                if (oldFile.exists()) {
                    System.out.println("Document Deleted");
                    //remove file from local directory
                    if (!oldFile.delete()) {
                        System.out.println("Failed to delete document. Please try again!");
                    }
                }
                product.setImage5(null);
            }
            MultipartFile image = request.getFile("image5");
            fileStorageProperties.setUploadDir("./uploads" + File.separator + "product" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);

            if (imagePath != null) {
                product.setImage5("/uploads" + File.separator + "product" + File.separator + imagePath);
            } else {
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseMessage.setMessage("Failed to upload documents. Please try again!");
            }
        }

        try {
            Product newProduct = productRepository.save(product);
            JsonParser parser = new JsonParser();
            String jsonStr = request.getParameter("productrows");
            JsonElement tradeElement = parser.parse(jsonStr);
            JsonArray array = tradeElement.getAsJsonArray();
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                LevelB levelB = null; //group
                LevelA levelA = null;//brand
                LevelC levelC = null;//Category
                if (!object.get("selectedLevelA").getAsString().equalsIgnoreCase("")) {
                    levelA = levelARepository.findByIdAndStatus(object.get("selectedLevelA").getAsLong(), true);
                }
                if (!object.get("selectedLevelB").getAsString().equalsIgnoreCase("")) {
                    levelB = levelBRepository.findByIdAndStatus(object.get("selectedLevelB").getAsLong(), true);
                }
                if (!object.get("selectedLevelC").getAsString().equalsIgnoreCase("")) {
                    levelC = levelCRepository.findByIdAndStatus(object.get("selectedLevelC").getAsLong(), true);
                }
                Units unit = unitsRepository.findByIdAndStatus(object.get("selectedUnit").getAsLong(), true);
                ProductUnitPacking productUnitPacking = null;
                // Long details_id = object.get("details_id").getAsLong();

                JsonElement detailsIdElement = object.get("details_id");
                long details_id;
                if (detailsIdElement != null && !detailsIdElement.isJsonNull()) {
                    details_id = detailsIdElement.getAsLong();
                } else {
                    details_id = object.get("id").getAsLong();
                }

                if (details_id != 0) {
                    productUnitPacking = productUnitRepository.findByIdAndStatus(details_id, true);
                } else {
                    productUnitPacking = new ProductUnitPacking();
                    productUnitPacking.setStatus(true);
                }
                productUnitPacking.setUnits(unit);
                if (object.has("conv")) productUnitPacking.setUnitConversion(object.get("conv").getAsDouble());
                if (object.has("unit_marg")) productUnitPacking.setUnitConvMargn(object.get("unit_marg").getAsDouble());
                if (object.has("is_negetive") && object.get("is_negetive").getAsBoolean()) {
                    productUnitPacking.setIsNegativeStocks(true);
                } else {
                    productUnitPacking.setIsNegativeStocks(false);
                }
                productUnitPacking.setMrp(object.get("mrp").getAsDouble());
                productUnitPacking.setPurchaseRate(object.get("pur_rate").getAsDouble());
                if (object.has("min_margin")) productUnitPacking.setMinMargin(object.get("min_margin").getAsDouble());
                productUnitPacking.setMinRateA(object.get("rate_1").getAsDouble());//sales Rate
                productUnitPacking.setMinRateB(object.get("rate_2").getAsDouble());
                productUnitPacking.setMinRateC(object.get("rate_3").getAsDouble());
                productUnitPacking.setStatus(true);
                productUnitPacking.setProduct(newProduct);
                productUnitPacking.setCreatedBy(users.getId());
                /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                JsonElement minQtyElement = object.get("min_qty");
                double minQty = minQtyElement != null && !minQtyElement.isJsonNull() ? minQtyElement.getAsDouble() : 0.0;
                productUnitPacking.setMinQty(minQty);

                JsonElement maxQtyElement = object.get("max_qty");
                double maxQty = maxQtyElement != null && !maxQtyElement.isJsonNull() ? maxQtyElement.getAsDouble() : 0.0;
                productUnitPacking.setMaxQty(maxQty);

                productUnitPacking.setLevelA(levelA);
                productUnitPacking.setLevelB(levelB);
                productUnitPacking.setLevelC(levelC);
                productUnitPacking.setUpdatedBy(users.getId());
                productUnitPacking.setFsrmh(object.get("rate_1").getAsDouble());
                productUnitPacking.setFsrai(object.get("rate_2").getAsDouble());
                productUnitPacking.setCsrmh(object.get("rate_3").getAsDouble());
                productUnitPacking.setCsrai(object.get("rate_4").getAsDouble());
                productUnitPacking.setIsRate(object.get("is_rate").getAsBoolean());
                productUnitRepository.save(productUnitPacking);


                /****** Inserting Product Opening Stocks ******/
                JsonArray mBatchJsonArray = null;
                JsonElement batchListElement = object.get("batchList");
                if (batchListElement != null && !batchListElement.isJsonNull() && batchListElement.isJsonArray()) {
                    mBatchJsonArray = batchListElement.getAsJsonArray();
                }
                LocalDate mDate = LocalDate.now();
                FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(mDate);
                if (mBatchJsonArray != null) {
                    for (JsonElement mBatchElement : mBatchJsonArray) {
                        ProductBatchNo mproductBatchNo = null;
                        ProductBatchNo productBatchNo = null;
                        JsonObject mBatchJsonObject = mBatchElement.getAsJsonObject();
                        Long id = mBatchJsonObject.get("id").getAsLong();
                        if (mBatchJsonObject.get("isOpeningbatch").getAsBoolean()) {
                            String batch_id = mBatchJsonObject.get("batch_id").getAsString();
                            if (batch_id.equalsIgnoreCase("") && id == 0) {
                                mproductBatchNo = new ProductBatchNo();
                                mproductBatchNo.setStatus(true);
                                mproductBatchNo.setFiscalYear(fiscalYear);
                            } else
                                mproductBatchNo = productBatchNoRepository.findByIdAndStatus(Long.parseLong(batch_id), true);
                            if (mBatchJsonObject.has("b_no"))
                                mproductBatchNo.setBatchNo(mBatchJsonObject.get("b_no").getAsString());
                            if (mBatchJsonObject.has("b_mrp") && !mBatchJsonObject.get("b_mrp").isJsonNull() &&
                                    !mBatchJsonObject.get("b_mrp").getAsString().isEmpty())
                                mproductBatchNo.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                            if (mBatchJsonObject.has("b_purchase_rate") &&
                                    !mBatchJsonObject.get("b_purchase_rate").isJsonNull() &&
                                    !mBatchJsonObject.get("b_purchase_rate").getAsString().isEmpty())
                                mproductBatchNo.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                            if (mBatchJsonObject.has("b_sale_rate") &&
                                    !mBatchJsonObject.get("b_sale_rate").isJsonNull() &&
                                    !mBatchJsonObject.get("b_sale_rate").getAsString().isEmpty()) {
                                mproductBatchNo.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                                mproductBatchNo.setMinRateA(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                            }
                            if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().isEmpty())
                                mproductBatchNo.setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                            if (mBatchJsonObject.has("b_free_qty") &&
                                    !mBatchJsonObject.get("b_free_qty").isJsonNull() &&
                                    !mBatchJsonObject.get("b_free_qty").getAsString().isEmpty())
                                mproductBatchNo.setFreeQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                            if (mBatchJsonObject.has("b_manufacturing_date") &&
                                    !mBatchJsonObject.get("b_manufacturing_date").getAsString().isEmpty()
                                    && !mBatchJsonObject.get("b_manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                            if (mBatchJsonObject.has("b_expiry") &&
                                    !mBatchJsonObject.get("b_expiry").getAsString().isEmpty()
                                    && !mBatchJsonObject.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));

                            mproductBatchNo.setStatus(true);
                            mproductBatchNo.setProduct(newProduct);
                            mproductBatchNo.setOutlet(newProduct.getOutlet());
                            mproductBatchNo.setBranch(newProduct.getBranch());
                            mproductBatchNo.setUnits(unit);
                            productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                        } else {

                            List<ProductUnitPacking> mUnitPackaging = productUnitRepository.findByProductIdAndStatus(newProduct.getId(), true);
                            if (mUnitPackaging != null) {
                                if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().equalsIgnoreCase("")) {
                                    mUnitPackaging.get(0).setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                                    productUnitRepository.save(mUnitPackaging.get(0));
                                }
                            }
                        }

                        try {

                            ProductOpeningStocks newOpeningStock = null;
                            if (id != 0) {
                                newOpeningStock = openingStocksRepository.findByIdAndStatus(id, true);
                            } else {
                                newOpeningStock = new ProductOpeningStocks();
                                newOpeningStock.setProduct(newProduct);
                                newOpeningStock.setUnits(unit);
                                newOpeningStock.setPackingMaster(mPackingMaster);
                                newOpeningStock.setBrand(mBrandMaster);
                                newOpeningStock.setGroup(mGroupMaster);
                                newOpeningStock.setCategory(mCategoryMaster);
                                newOpeningStock.setLevelA(levelA);
                                newOpeningStock.setLevelB(levelB);
                                newOpeningStock.setLevelC(levelC);
                                newOpeningStock.setStatus(true);
                                newOpeningStock.setOutlet(newProduct.getOutlet());
                                newOpeningStock.setFiscalYear(fiscalYear);
                            }
                            newOpeningStock.setOpeningStocks(Double.parseDouble(mBatchJsonObject.get("opening_qty").getAsString()));
                            newOpeningStock.setProductBatchNo(productBatchNo);
                            if (mBatchJsonObject.has("b_free_qty") && !mBatchJsonObject.get("b_free_qty").getAsString().isEmpty())
                                newOpeningStock.setFreeOpeningQty(mBatchJsonObject.get("b_free_qty").getAsDouble());
                            if (mBatchJsonObject.has("b_mrp") && !mBatchJsonObject.get("b_mrp").getAsString().isEmpty())
                                newOpeningStock.setMrp(mBatchJsonObject.get("b_mrp").getAsDouble());
                            if (mBatchJsonObject.has("b_purchase_rate") &&
                                    !mBatchJsonObject.get("b_purchase_rate").getAsString().isEmpty())
                                newOpeningStock.setPurchaseRate(mBatchJsonObject.get("b_purchase_rate").getAsDouble());
                            if (mBatchJsonObject.has("b_sale_rate") && !mBatchJsonObject.get("b_sale_rate").isJsonNull() &&
                                    !mBatchJsonObject.get("b_sale_rate").getAsString().isEmpty())
                                newOpeningStock.setSalesRate(mBatchJsonObject.get("b_sale_rate").getAsDouble());
                            newOpeningStock.setLevelA(levelA);
                            newOpeningStock.setLevelB(levelB);
                            newOpeningStock.setLevelC(levelC);
                            newOpeningStock.setStatus(true);
                            if (mBatchJsonObject.has("b_manufacturing_date") &&
                                    !mBatchJsonObject.get("b_manufacturing_date").getAsString().isEmpty() &&
                                    !mBatchJsonObject.get("b_manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                                newOpeningStock.setManufacturingDate(LocalDate.parse(mBatchJsonObject.get("b_manufacturing_date").getAsString()));
                            if (mBatchJsonObject.has("b_expiry") &&
                                    !mBatchJsonObject.get("b_expiry").getAsString().isEmpty() &&
                                    !mBatchJsonObject.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                                newOpeningStock.setExpiryDate(LocalDate.parse(mBatchJsonObject.get("b_expiry").getAsString()));
                            if (mBatchJsonObject.has("b_costing") && !mBatchJsonObject.get("b_costing").getAsString().isEmpty())
                                newOpeningStock.setCosting(mBatchJsonObject.get("b_costing").getAsDouble());
                            try {
                                openingStocksRepository.save(newOpeningStock);
                            } catch (Exception e) {
                                productLogger.error("Exception:" + e.getMessage());
                            }
                        } catch (Exception e) {
                            responseMessage.setMessage("Error in Product Updation");
                            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            productLogger.error("Error in Product Updation:" + exceptionAsString);
                        }
                    }
                }
                /****** Inserting Product Opening Stocks ******/
            }
            /*** delete Product units while updating product ***/
            String delJsonStr = request.getParameter("rowDelDetailsIds");
            JsonElement delElement = new JsonArray();
            if (delJsonStr != null) {
                delElement = parser.parse(delJsonStr);
            }

            JsonArray delArray = delElement.getAsJsonArray();
            for (JsonElement mDelList : delArray) {
                JsonObject mDelObject = mDelList.getAsJsonObject();
                Long delId = mDelObject.get("del_id").getAsLong();
                ProductUnitPacking mUnitDel = productUnitRepository.findByIdAndStatus(delId, true);
                mUnitDel.setStatus(false);
                productUnitRepository.save(mUnitDel);

            }
            /***** set Rate conversion for MultiUnit *****/
            List<ProductUnitPacking> unitPackingList = productUnitRepository.findByProductId(newProduct.getId());
            ProductUnitPacking pu = null;
            ProductUnitPacking prunit = unitPackingList.stream()
                    // Check if the unitPackingList list contains a Product with this ID
                    .filter(p -> p.getIsRate())
                    .findFirst().get();
            unitConversion.convertToMultiUnitRate(unitPackingList, prunit);

          /*  DataLockModel dataLockModel = DataLockModel.getInstance();
            dataLockModel.removeObject("productMaster_" + product.getId());*/
            updateProductForFranchise(request);
            responseMessage.setMessage("Product Updated Successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            responseMessage.setData(newProduct.getId().toString());
        } catch (Exception e) {
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseMessage.setMessage("Internal Server Error");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Product Updation:" + exceptionAsString);
        }
        return responseMessage;
    }

    public Object productStockList() {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<Product> productDetails = new ArrayList<>();
        String searchKey = "";
        String barcodeKey = "";
        String query = "SELECT * FROM `product_tbl` LEFT JOIN packing_master_tbl ON product_tbl.packing_master_id=packing_master_tbl.id" + " WHERE product_tbl.status=1";

        if (!searchKey.equalsIgnoreCase("")) {
            query = query + " AND (product_name LIKE '%" + searchKey + "%' OR product_code LIKE '%" + searchKey + "%' OR barcode_no LIKE '%" + searchKey + "%' OR  packing_master_tbl.pack_name LIKE '%" + searchKey + "%')";
        }
        if (!barcodeKey.equalsIgnoreCase("")) {
            query = query + " AND barcode_no=" + barcodeKey;
        }

        Query q = entityManager.createNativeQuery(query, Product.class);
        productDetails = q.getResultList();
        if (productDetails != null && productDetails.size() > 0) {
            for (Product mDetails : productDetails) {
                List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
                ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
                JsonObject mObject = new JsonObject();
                if (productBatchNo != null) {
                    if (productBatchNo.getExpiryDate() != null) {
                        mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                    } else {
                        mObject.addProperty("batch_expiry", "");
                    }
                }
                mObject.addProperty("hsn", mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
                mObject.addProperty("tax_type", mDetails.getTaxType());
                mObject.addProperty("tax_per", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                mObject.addProperty("igst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                mObject.addProperty("cgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
                mObject.addProperty("sgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("code", mDetails.getProductCode() != null ? mDetails.getProductCode() : "");
                mObject.addProperty("product_name", mDetails.getProductName());
                mObject.addProperty("packing", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
                mObject.addProperty("barcode", mDetails.getBarcodeNo() != null ? mDetails.getBarcodeNo() : "");
                mObject.addProperty("mrp", 0.0);
                mObject.addProperty("purchaserate", 0.0);
                mObject.addProperty("sales_rate", 0.0);
                mObject.addProperty("current_stock", "0.0");
                mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
                mObject.addProperty("is_inventory", mDetails.getIsInventory());
                mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
                mObject.addProperty("brandName", mDetails.getBrand().getBrandName());
                mObject.addProperty("product_category", mDetails.getCategory() != null ? mDetails.getCategory().getCategoryName() : "");
                if (mDetails.getIsBatchNumber()) {
                    TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
                    if (tranxPurInvoiceDetailsUnits != null) {
                        mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.0);
                        mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.0);
                        mObject.addProperty("purchaserate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.0);
                        Double closingStocks = inventoryCommonPostings.getmobileClosingStockProduct(mDetails.getId(), tranxPurInvoiceDetailsUnits.getPurchaseTransaction().getFiscalYear());
                        mObject.addProperty("current_stock", closingStocks > 0 ? closingStocks.toString() : "0.0");
                        if (productBatchNo != null) {
                            mObject.addProperty("costing", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0.0);
                        } else {
                            mObject.addProperty("costing", 0.0);
                        }
                    } else {
                        mObject.addProperty("costing", 0.0);
                    }

                } else if (mDetails.getIsSerialNumber()) {
                    System.out.println("Is Serial number:");
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "");
                        mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                        mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                        mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                        mObject.addProperty("costing", productUnitPacking.get(0).getCosting() != null ? productUnitPacking.get(0).getCosting() : 0.0);
                    } else {
                        mObject.addProperty("mrp", 0.0);
                        mObject.addProperty("purchaserate", 0.0);
                        mObject.addProperty("sales_rate", 0.0);
                        mObject.addProperty("costing", 0.0);
                        mObject.addProperty("unit", "");
                    }
                } else {
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "");
                        mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                        mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                        mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                        mObject.addProperty("costing", productUnitPacking.get(0).getCosting() != null ? productUnitPacking.get(0).getCosting() : 0.0);
                    } else {
                        mObject.addProperty("mrp", 0.0);
                        mObject.addProperty("purchaserate", 0.0);
                        mObject.addProperty("sales_rate", 0.0);
                        mObject.addProperty("costing", 0.0);
                        mObject.addProperty("unit", "");
                    }
                }
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject validateProductCode(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        String productCode = "";
        Product product = null;
        JsonObject result = new JsonObject();
        try {
            if (paramMap.containsKey("productCode") && !request.getParameter("productCode").equalsIgnoreCase(""))
                productCode = request.getParameter("productCode");
            if (users.getBranch() != null)
                product = productRepository.findByduplicateProductWithBranch(users.getOutlet().getId(), users.getBranch().getId(), productCode, true);
            else {
                product = productRepository.findByduplicateProduct(users.getOutlet().getId(), productCode, true);
            }
            if (product != null) {
                result.addProperty("message", "Duplicate Product Code");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "new product");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in ProductCode validations:" + exceptionAsString);
        }
        return result;
    }

    public JsonObject validateProductCodeUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        String productCode = "";
        Long productId = null;
        Product product = null;
        JsonObject result = new JsonObject();
        try {
            if (paramMap.containsKey("productCode") && !request.getParameter("productCode").equalsIgnoreCase(""))
                productCode = request.getParameter("productCode");
            if (paramMap.containsKey("productId")) productId = Long.parseLong(request.getParameter("productId"));
            if (users.getBranch() != null) {
                product = productRepository.findByProductCodeAndStatusAndOutletIdAndBranchId(productCode, true, users.getOutlet().getId(), users.getBranch().getId());
            } else {
                product = productRepository.findByProductCodeAndStatusAndOutletIdAndBranchIsNull(productCode, true, users.getOutlet().getId());
            }
            if (product != null && product.getId().longValue() != productId.longValue()) {
                result.addProperty("message", "Duplicate Product Code");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "new product");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in ProductCode validation update:" + exceptionAsString);
        }
        return result;
    }

    public JsonObject validateProductUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Long packageId = null;
        String productName = "";
        Product product = null;
        Long productId = null;
        JsonObject result = new JsonObject();
        try {
            if (paramMap.containsKey("packageId") && !request.getParameter("packageId").equalsIgnoreCase("")) {
                packageId = Long.parseLong(request.getParameter("packageId"));
            }
            if (paramMap.containsKey("productName") && !request.getParameter("productName").equalsIgnoreCase(""))
                productName = request.getParameter("productName");
            if (paramMap.containsKey("productId")) productId = Long.parseLong(request.getParameter("productId"));
            if (users.getBranch() != null) {
                if (packageId != null) {
                    product = productRepository.findByProductNameAndPackingMasterIdAndOutletIdAndBranchIdAndStatus(productName, packageId, users.getOutlet().getId(), users.getBranch().getId(), true);
                } else {
                    product = productRepository.findByProductNameAndOutletIdAndBranchIdAndStatus(productName, users.getOutlet().getId(), users.getBranch().getId(), true);
                }
            } else {
                if (packageId != null) {
                    product = productRepository.findByProductNameAndPackingMasterIdAndOutletIdAndStatusAndBranchIsNull(productName, packageId, users.getOutlet().getId(), true);
                } else {
                    product = productRepository.findByProductNameAndOutletIdAndStatusAndBranchIsNull(productName, users.getOutlet().getId(), true);
                }
            }
            if (product.getId() != productId) {
                result.addProperty("message", "Duplicate Product");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "new product");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in ProductValidationUpdate:" + exceptionAsString);
        }
        return result;
    }

    public JsonObject productTransactionList(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<Product> productDetails = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String searchKey = "";
        String barcodeKey = "";
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
        if (paramMap.containsKey("search")) searchKey = request.getParameter("search");
        if (paramMap.containsKey("barcode")) barcodeKey = request.getParameter("barcode");

        String query = "SELECT * FROM `product_tbl` LEFT JOIN packing_master_tbl ON " + "product_tbl.packing_master_id=packing_master_tbl.id" + " " + "WHERE product_tbl.outlet_id=" + users.getOutlet().getId() + " AND product_tbl.status=1";

        if (users.getBranch() != null) {
            query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
        }
        if (!searchKey.equalsIgnoreCase("")) {
            query = query + " AND (product_name LIKE '%" + searchKey + "%' OR product_code LIKE '%" + searchKey + "%' OR barcode_no LIKE '%" + searchKey + "%' OR  packing_master_tbl.pack_name LIKE '%" + searchKey + "%')";
        }
        if (!barcodeKey.equalsIgnoreCase("")) {
            query = query + " AND barcode_no=" + barcodeKey;
        }
        query = query + " LIMIT 50 ";
        System.out.println("query " + query);
        Query q = entityManager.createNativeQuery(query, Product.class);
        productDetails = q.getResultList();
        if (productDetails != null && productDetails.size() > 0) {
            for (Product mDetails : productDetails) {
                ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
                List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
                JsonObject mObject = new JsonObject();
                if (productBatchNo != null) {
                    if (productBatchNo.getExpiryDate() != null) {
                        mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                    } else {
                        mObject.addProperty("batch_expiry", "");
                    }
                    mObject.addProperty("mrp", productBatchNo.getMrp());
                    mObject.addProperty("sales_rate", productBatchNo.getSalesRate());
                    //   mObject.addProperty("current_stock", productBatchNo.getQnty());
                }
                if (productUnitPacking != null && productUnitPacking.size() > 0) {
                    mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "PCS");
                    mObject.addProperty("is_negative", productUnitPacking.get(0).getIsNegativeStocks() != null ? productUnitPacking.get(0).getIsNegativeStocks() : false);

                } else {
                    mObject.addProperty("unit", "PCS");
                }
                mObject.addProperty("hsn", mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
                mObject.addProperty("tax_type", mDetails.getTaxType());
                mObject.addProperty("tax_per", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                mObject.addProperty("igst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                mObject.addProperty("cgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
                mObject.addProperty("sgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("code", mDetails.getProductCode());
                mObject.addProperty("product_name", mDetails.getProductName());
                mObject.addProperty("packing", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
                mObject.addProperty("barcode", mDetails.getBarcodeNo());
                mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
                mObject.addProperty("is_inventory", mDetails.getIsInventory());
                mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
                mObject.addProperty("brand", mDetails.getBrand().getBrandName());
                if (mDetails.getIsBatchNumber()) {
                    /**** with Transaction Purchase Invoice  *****/
                    TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
                    if (tranxPurInvoiceDetailsUnits != null) {
                        mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                        mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
                        mObject.addProperty("purchaserate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.00);
                    }
                } else if (mDetails.getIsSerialNumber()) {

                } else {
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                        mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                        mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                    } else {
                        mObject.addProperty("mrp", 0);
                        mObject.addProperty("purchaserate", 0);
                        mObject.addProperty("sales_rate", 0);
                    }
                }
                Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(), mDetails.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
                Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                mObject.addProperty("current_stock", (productOpeningStocks + closingStocks));
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    /*** Product , Barcode and Company Barcode Search functionality of Product selection in Tranx Perticular *****/

    public JsonObject productTransactionListNew(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List productDetails = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String searchKey = "";
        String barcodeKey = "";
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
        try {
            if (paramMap.containsKey("search")) searchKey = request.getParameter("search");
            String query = "SELECT product_tbl.id FROM `product_tbl` LEFT JOIN product_barcode_tbl ON " +
                    "product_tbl.id=product_barcode_tbl.id WHERE product_tbl.outlet_id=" + users.getOutlet().getId() +
                    " AND product_tbl.status=1";

            if (users.getBranch() != null) {
                query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND product_tbl.branch_id IS NULL";

            }
            if (!searchKey.equalsIgnoreCase("")) {
                query = query + " AND (product_tbl.product_name LIKE '%" + searchKey + "%' OR " +
                        "product_tbl.product_code LIKE '%" + searchKey + "%' " + "OR " +
                        "product_barcode_tbl.barcode_unique_code LIKE '%" + searchKey + "%' OR " +
                        "product_barcode_tbl.company_barcode LIKE '%" + searchKey + "%')";
            }

            query = query + " LIMIT 50 ";
            Query q = entityManager.createNativeQuery(query);
            productDetails = q.getResultList();
            if (productDetails != null && productDetails.size() > 0) {
                for (Object mList : productDetails) {
                    Product mDetails = productRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                    ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
                    List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
                    JsonObject mObject = new JsonObject();
                    if (productBatchNo != null) {
                        if (productBatchNo.getExpiryDate() != null) {
                            mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                        } else {
                            mObject.addProperty("batch_expiry", "");
                        }
                        mObject.addProperty("mrp", productBatchNo.getMrp());
                        mObject.addProperty("sales_rate", productBatchNo.getSalesRate());
                        //   mObject.addProperty("current_stock", productBatchNo.getQnty());
                    } else {
                        mObject.addProperty("mrp", mDetails.getMrp());
                        mObject.addProperty("sales_rate", 0.0);
                    }
                    //
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("unit_id", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getId() : 0);
                        mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "PCS");
                        mObject.addProperty("is_negative", productUnitPacking.get(0).getIsNegativeStocks() != null ? productUnitPacking.get(0).getIsNegativeStocks() : false);

                    } else {
                        mObject.addProperty("unit_id", 0);
                        mObject.addProperty("unit", "PCS");
                    }
                    mObject.addProperty("hsn", mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
                    mObject.addProperty("tax_type", mDetails.getTaxType());
                    mObject.addProperty("tax_per", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("igst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("cgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
                    mObject.addProperty("sgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
                    mObject.addProperty("id", mDetails.getId());
                    mObject.addProperty("code", mDetails.getProductCode());
                    mObject.addProperty("product_name", mDetails.getProductName());
                    mObject.addProperty("packing", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
                    System.out.println("barcode" + mDetails.getBarcodeNo());
                   /* mObject.addProperty("fsrmh", mDetails.getFsrmh() != null ? mDetails.getFsrmh().toString() : "");
                    mObject.addProperty("fsrai", mDetails.getFsrai() != null ? mDetails.getFsrai().toString() : "");
                    mObject.addProperty("csrmh", mDetails.getCsrmh() != null ? mDetails.getCsrmh().toString() : "");
                    mObject.addProperty("csrai", mDetails.getCsrai() != null ? mDetails.getCsrai().toString() : "");
*/
                    mObject.addProperty("barcode", mDetails.getBarcodeNo() != null ? mDetails.getBarcodeNo() : "");
                    mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
                    mObject.addProperty("is_inventory", mDetails.getIsInventory());
                    mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
                    mObject.addProperty("brand", mDetails.getBrand().getBrandName() != null ? mDetails.getBrand().getBrandName() : "");
                    mObject.addProperty("productType", mDetails.getProductType());
                    if (mDetails.getIsBatchNumber()) {
                        /**** with Transaction Purchase Invoice  *****/
                        TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
                        if (tranxPurInvoiceDetailsUnits != null) {
                            mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                            mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
                            mObject.addProperty("purchaserate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.00);
                        }
                    } else if (mDetails.getIsSerialNumber()) {

                    } else {
                        if (productUnitPacking != null && productUnitPacking.size() > 0) {
                            mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                            mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                            mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                        } else {
                            mObject.addProperty("mrp", 0);
                            mObject.addProperty("purchaserate", 0);
                            mObject.addProperty("sales_rate", 0);
                        }
                    }
                    Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(), mDetails.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
                    Double freeQty = inventoryCommonPostings.calculateFreeQty(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double currentStock = closingStocks + productOpeningStocks + freeQty;
                    mObject.addProperty("current_stock", currentStock);
                    result.add(mObject);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in productTransactionListNew:" + exceptionAsString);
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject deleteProduct(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        Long productId = Long.parseLong(request.getParameter("id"));
        String source = request.getParameter("source");
        Product mProduct = productRepository.findByIdAndStatusAndIsDelete(productId, true, true);
        if (mProduct != null) {
            jsonObject.addProperty("message", "Product deleted successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            if (!source.equalsIgnoreCase("product_edit")) mProduct.setStatus(false);
            productRepository.save(mProduct);
        } else {
            jsonObject.addProperty("message", "Product is used in transaction ,first delete transaction");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return jsonObject;
    }

    public Object productTransactionListByContent(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List productDetails = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String searchContent = request.getParameter("searchContent");
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
        try {
            String query = "SELECT DISTINCT(product_tbl.id) FROM `product_tbl` LEFT JOIN product_barcode_tbl ON " +
                    " product_tbl.id=product_barcode_tbl.id LEFT JOIN product_content_master_tbl ON" +
                    " product_tbl.id=product_content_master_tbl.product_id WHERE product_tbl.outlet_id="
                    + users.getOutlet().getId() + " AND product_tbl.status=1";

            if (users.getBranch() != null) {
                query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND product_tbl.branch_id IS NULL";
            }
            if (!searchContent.equalsIgnoreCase("")) {
                query = query + " AND content_type LIKE '%" + searchContent + "%'";
            }
            query = query + " LIMIT 50 ";
            System.out.println("query :" + query);
            Query q = entityManager.createNativeQuery(query);
            productDetails = q.getResultList();
            if (productDetails != null && productDetails.size() > 0) {
                for (Object mList : productDetails) {
                    Product mDetails = productRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                    ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
                    List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
                    JsonObject mObject = new JsonObject();
                    if (productBatchNo != null) {
                        if (productBatchNo.getExpiryDate() != null) {
                            mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                        } else {
                            mObject.addProperty("batch_expiry", "");
                        }
                        mObject.addProperty("mrp", productBatchNo.getMrp());
                        mObject.addProperty("sales_rate", productBatchNo.getSalesRate());
                        //   mObject.addProperty("current_stock", productBatchNo.getQnty());
                    }
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "PCS");
                        mObject.addProperty("is_negative", productUnitPacking.get(0).getIsNegativeStocks() != null ? productUnitPacking.get(0).getIsNegativeStocks() : false);

                    } else {
                        mObject.addProperty("unit", "PCS");
                    }
                    mObject.addProperty("hsn", mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
                    mObject.addProperty("tax_type", mDetails.getTaxType());
                    mObject.addProperty("tax_per", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("igst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("cgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
                    mObject.addProperty("sgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
                    mObject.addProperty("id", mDetails.getId());
                    mObject.addProperty("code", mDetails.getProductCode());
                    mObject.addProperty("product_name", mDetails.getProductName());
                    mObject.addProperty("packing", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
                    mObject.addProperty("barcode", mDetails.getBarcodeNo());
                    mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
                    mObject.addProperty("is_inventory", mDetails.getIsInventory());
                    mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
                    mObject.addProperty("brand", mDetails.getBrand().getBrandName());
                    mObject.addProperty("productType", mDetails.getProductType());
                    if (mDetails.getIsBatchNumber()) {
                        /**** with Transaction Purchase Invoice  *****/
                        TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
                        if (tranxPurInvoiceDetailsUnits != null) {
                            mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                            mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
                            mObject.addProperty("purchaserate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.00);
                        }
                    } else if (mDetails.getIsSerialNumber()) {

                    } else {
                        if (productUnitPacking != null && productUnitPacking.size() > 0) {
                            mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                            mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                            mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                        } else {
                            mObject.addProperty("mrp", 0);
                            mObject.addProperty("purchaserate", 0);
                            mObject.addProperty("sales_rate", 0);
                        }
                    }
                    Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(), mDetails.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
                    Double freeQty = inventoryCommonPostings.calculateFreeQty(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double currentStock = closingStocks + productOpeningStocks + freeQty;
                    mObject.addProperty("current_stock", currentStock);
                    result.add(mObject);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in productTransactionListNew:" + exceptionAsString);
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public Object productListByContentDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List productDetails = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String contentType = request.getParameter("contentType");
        String contentPower = request.getParameter("contentPower");
        String contentPkg = request.getParameter("contentPkg");
        String contentDose = request.getParameter("contentDose");
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
        try {
            String query = "SELECT DISTINCT(product_id) FROM product_tbl LEFT JOIN product_content_master_tbl" +
                    " ON product_tbl.id=product_content_master_tbl.product_id WHERE product_tbl.outlet_id="
                    + users.getOutlet().getId() + " AND product_tbl.status=1";

            if (users.getBranch() != null) {
                query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND product_tbl.branch_id IS NULL";
            }
            if (!contentType.equalsIgnoreCase("")) {
                query = query + " AND product_content_master_tbl.content_type='" + contentType + "'";
            }
            if (!contentPower.equalsIgnoreCase("")) {
                query = query + " AND product_content_master_tbl.content_power='" + contentPower + "'";
            }
            if (!contentPkg.equalsIgnoreCase("")) {
                query = query + " AND product_content_master_tbl.content_package='" + contentPkg + "'";
            }
            if (!contentDose.equalsIgnoreCase("")) {
                query = query + " AND product_content_master_tbl.content_type_dose='" + contentDose + "'";
            }
//            query = query + " LIMIT 50 ";
            System.out.println("query :" + query);
            Query q = entityManager.createNativeQuery(query);
            productDetails = q.getResultList();
            if (productDetails != null && productDetails.size() > 0) {
                for (Object mList : productDetails) {
                    Product mDetails = productRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                    ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
                    List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
                    JsonObject mObject = new JsonObject();
                    if (productBatchNo != null) {
                        if (productBatchNo.getExpiryDate() != null) {
                            mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                        } else {
                            mObject.addProperty("batch_expiry", "");
                        }
                        mObject.addProperty("mrp", productBatchNo.getMrp());
                        mObject.addProperty("sales_rate", productBatchNo.getSalesRate());
                        //   mObject.addProperty("current_stock", productBatchNo.getQnty());
                    }
                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        mObject.addProperty("unit", productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "PCS");
                        mObject.addProperty("is_negative", productUnitPacking.get(0).getIsNegativeStocks() != null ? productUnitPacking.get(0).getIsNegativeStocks() : false);

                    } else {
                        mObject.addProperty("unit", "PCS");
                    }
                    mObject.addProperty("hsn", mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
                    mObject.addProperty("tax_type", mDetails.getTaxType());
                    mObject.addProperty("tax_per", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("igst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("cgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
                    mObject.addProperty("sgst", mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
                    mObject.addProperty("id", mDetails.getId());
                    mObject.addProperty("code", mDetails.getProductCode());
                    mObject.addProperty("product_name", mDetails.getProductName());
                    mObject.addProperty("packing", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
                    mObject.addProperty("barcode", mDetails.getBarcodeNo());
                    mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
                    mObject.addProperty("is_inventory", mDetails.getIsInventory());
                    mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
                    mObject.addProperty("brand", mDetails.getBrand().getBrandName());
                    mObject.addProperty("productType", mDetails.getProductType());
                    if (mDetails.getIsBatchNumber()) {
                        /**** with Transaction Purchase Invoice  *****/
                        TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
                        if (tranxPurInvoiceDetailsUnits != null) {
                            mObject.addProperty("mrp", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                            mObject.addProperty("sales_rate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
                            mObject.addProperty("purchaserate", tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.00);
                        }
                    } else if (mDetails.getIsSerialNumber()) {

                    } else {
                        if (productUnitPacking != null && productUnitPacking.size() > 0) {
                            mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                            mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                            mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                        } else {
                            mObject.addProperty("mrp", 0);
                            mObject.addProperty("purchaserate", 0);
                            mObject.addProperty("sales_rate", 0);
                        }
                    }
                    Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(), mDetails.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
                    Double freeQty = inventoryCommonPostings.calculateFreeQty(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(), users.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double currentStock = closingStocks + productOpeningStocks + freeQty;
                    mObject.addProperty("current_stock", currentStock);
                    result.add(mObject);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in productTransactionListNew:" + exceptionAsString);
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public Object GVProductList(Map<String, String> request, HttpServletRequest req) {
//        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
//        String franchiseUser=request.get("franchiseUser");
//        System.out.println("franchiseUser=>"+franchiseUser);
//        JsonObject jsonObject = new JsonParser().parse(franchiseUser).getAsJsonObject();
        Users cadmin = usersRepository.findTop1ByUserRoleIgnoreCaseAndCompanyCode("cadmin", "gvmh001");
//        Users cadmin = usersRepository.findById(3L).get();
//            System.out.println("cadmin :"+cadmin.toString());
        String cadminToken = jwtRequestFilter.getTokenFromUsername(cadmin.getUsername());
        Long outletId = cadmin.getOutlet().getId();
        ResponseMessage responseMessage = ResponseMessage.getInstance();

        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText").trim();
        String barcodeText = request.get("barcode").trim();
        Boolean flag = false;
        List<Product> productDetails = new ArrayList<>();
        List<Product> productArrayList = new ArrayList<>();
        List<FRProductDTO> productDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
//            String query = "SELECT * FROM `product_tbl` LEFT JOIN packing_master_tbl ON product_tbl.packing_master_id=packing_master_tbl.id WHERE product_tbl.outlet_id=" + users.getOutlet().getId() + " AND product_tbl.status=1";
            String query = "SELECT * FROM `product_tbl` WHERE product_tbl.outlet_id=" + outletId + " AND product_tbl.status=1";
//            if (users.getBranch() != null) {
//                query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
//            } else {
//                query = query + " AND product_tbl.branch_id IS NULL";
//            }

//            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
//                query += " AND invoice_date BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
//            !    OR  packing_master_tbl.pack_name LIKE '%" + searchKey + "%'
                query = query + " AND (product_name LIKE '%" + searchText + "%' OR product_code LIKE '%" + searchText + "%' OR barcode_no LIKE '%" + searchText + "%')";
            }
            if (!barcodeText.equalsIgnoreCase("")) {
                query = query + " AND barcode_no=" + barcodeText;
            }
           /* String jsonToStr = request.get("sort");
            System.out.println(" sort " + jsonToStr);
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") &&
                    jsonObject.get("colId").getAsString() != null) {
                String sortBy = jsonObject.get("colId").getAsString();
                query = query + " ORDER BY " + sortBy;
                if (jsonObject.get("isAsc").getAsBoolean() == true) {
                    query = query + " ASC";
                } else {
                    query = query + " DESC";
                }
            } else {*/
            query = query + " ORDER BY product_tbl.id DESC";
//            }
            String query1 = query;       //we get all lists in this list
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;
            System.out.println("query => " + query);
            Query q = entityManager.createNativeQuery(query, Product.class);

            productDetails = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, Product.class);

            productArrayList = q1.getResultList();
            Integer total_pages = (productArrayList.size() / pageSize);
            if ((productArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            for (Product mDetails : productDetails) {
                productDTOList.add(convertProductToFRDTD(mDetails));
            }
            System.out.println("actProductList =>" + productDTOList);
            GenericDatatable<FRProductDTO> data = new GenericDatatable<>(productDTOList, productArrayList.size(),
                    pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            genericDTData.setRows(productDTOList);
            genericDTData.setTotalRows(0);
        }

        return responseMessage;
    }

    private FRProductDTO convertProductToFRDTD(Product mDetails) {
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
        FRProductDTO frProductDTO = new FRProductDTO();
        ProductBatchNo productBatchNo = productBatchNoRepository.findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
        List<ProductUnitPacking> productUnitPacking = productUnitRepository.findByProductIdAndStatus(mDetails.getId(), true);
        if (productBatchNo != null) {
            if (productBatchNo.getExpiryDate() != null) {
                frProductDTO.setBatch_expiry(productBatchNo.getExpiryDate().toString());
            } else {
                frProductDTO.setBatch_expiry("");
            }
            frProductDTO.setMrp(productBatchNo.getMrp());
            frProductDTO.setSales_rate(productBatchNo.getSalesRate());
        }
        if (productUnitPacking != null && productUnitPacking.size() > 0) {
            frProductDTO.setUnit(productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getUnitName() : "PCS");
            frProductDTO.setUnit_id(productUnitPacking.get(0).getUnits() != null ? productUnitPacking.get(0).getUnits().getId() : 1L);
            frProductDTO.setIs_negative(productUnitPacking.get(0).getIsNegativeStocks() != null ? productUnitPacking.get(0).getIsNegativeStocks() : false);
        } else {
            frProductDTO.setUnit_id(1L);
            frProductDTO.setUnit("PCS");
            frProductDTO.setIs_negative(false);
        }
        frProductDTO.setHsn(mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
        frProductDTO.setHsn_id(mDetails.getProductHsn() != null ? mDetails.getProductHsn().getId() : 0L);
        frProductDTO.setTax_type(mDetails.getTaxType());
        frProductDTO.setTax_per(mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
        frProductDTO.setIgst(mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
        frProductDTO.setCgst(mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
        frProductDTO.setSgst(mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
        frProductDTO.setId(mDetails.getId());
        frProductDTO.setCode(mDetails.getProductCode());
        frProductDTO.setProduct_name(mDetails.getProductName());
        frProductDTO.setPacking(mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
        frProductDTO.setPacking_id(mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getId() : 0L);
        frProductDTO.setBarcode(mDetails.getBarcodeNo());
        frProductDTO.setBrand(mDetails.getBrand().getBrandName());
        if (mDetails.getIsBatchNumber()) {
            /**** with Transaction Purchase Invoice  *****/
            TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductIdOrderByIdDesc(mDetails.getId());
            if (tranxPurInvoiceDetailsUnits != null) {
                frProductDTO.setMrp(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.00);
                frProductDTO.setSales_rate(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() : 0.00);
                frProductDTO.setPurchaserate(tranxPurInvoiceDetailsUnits.getProductBatchNo() != null && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() : 0.00);
            }
        } else {
            if (productUnitPacking != null && productUnitPacking.size() > 0) {
                frProductDTO.setMrp(productUnitPacking.get(0).getMrp());
                frProductDTO.setPurchaserate(productUnitPacking.get(0).getPurchaseRate());
                frProductDTO.setSales_rate(productUnitPacking.get(0).getMinRateA());
            } else {
                frProductDTO.setMrp(0.0);
                frProductDTO.setPurchaserate(0.0);
                frProductDTO.setSales_rate(0.0);

            }
        }
//        jsonObject.get("companyCode").getAsString()
//        Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(), mDetails.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
//        Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(), mDetails.getOutlet().getId(), mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
//        frProductDTO.setCurrent_stock((productOpeningStocks + closingStocks));

        Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(),
                mDetails.getOutlet().getId(),
                mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
        Double freeQty = inventoryCommonPostings.calculateFreeQty(mDetails.getId(),
                mDetails.getOutlet().getId(),
                mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
        Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(),
                mDetails.getOutlet().getId(),
                mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
        Double currentStock = closingStocks + productOpeningStocks + freeQty;
        frProductDTO.setCurrent_stock(currentStock);
        frProductDTO.setUnitList(unitConversion.FRshowStocks(mDetails.getId()));
        return frProductDTO;
    }

    public Object productTransactionListMultiUnit(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List productDetails = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String searchKey = "";
        String barcodeKey = "";
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
        try {
            if (paramMap.containsKey("search"))
                searchKey = request.getParameter("search");
            String query = "SELECT product_tbl.id FROM `product_tbl` LEFT JOIN product_barcode_tbl ON " +
                    "product_tbl.id=product_barcode_tbl.id WHERE product_tbl.outlet_id=" + users.getOutlet().getId() +
                    " AND product_tbl.status=1";

            if (users.getBranch() != null) {
                query = query + " AND product_tbl.branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND product_tbl.branch_id IS NULL";
            }
            if (!searchKey.equalsIgnoreCase("")) {
                query = query + " AND (product_tbl.product_name LIKE '%" + searchKey + "%' OR " +
                        "product_tbl.product_code LIKE '%" + searchKey + "%' " + "OR " +
                        "product_barcode_tbl.barcode_unique_code LIKE '" + searchKey + "' OR " +
                        "product_barcode_tbl.company_barcode LIKE '" + searchKey + "')";
            }
            query = query + " ORDER BY ID DESC";

            query = query + " LIMIT 50 ";
            Query q = entityManager.createNativeQuery(query);
            productDetails = q.getResultList();
            if (productDetails != null && productDetails.size() > 0) {
                for (Object mList : productDetails) {
                    Product mDetails = productRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                    ProductBatchNo productBatchNo = productBatchNoRepository
                            .findTop1ByProductIdAndStatusOrderByIdDesc(mDetails.getId(), true);
                    List<ProductUnitPacking> productUnitPacking = productUnitRepository
                            .findByProductIdAndStatus(mDetails.getId(), true);
                    JsonObject mObject = new JsonObject();
                    if (productBatchNo != null) {
                        if (productBatchNo.getExpiryDate() != null) {
                            mObject.addProperty("batch_expiry", productBatchNo.getExpiryDate().toString());
                        } else {
                            mObject.addProperty("batch_expiry", "");
                        }
                        mObject.addProperty("mrp", productBatchNo.getMrp() != null ? productBatchNo.getMrp() : 0.0);
                        mObject.addProperty("sales_rate", productBatchNo.getSalesRate());
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("batch_no", productBatchNo.getBatchNo());
                        jsonObject.addProperty("batch_id", productBatchNo.getId());
                        mObject.add("batch_data", jsonObject);
                    }
                    JsonArray jsonArray = new JsonArray();

                    if (productUnitPacking != null && productUnitPacking.size() > 0) {
                        Boolean isNegetive = false;
                        for (ProductUnitPacking unitPacking :
                                productUnitPacking) {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("unit_name", unitPacking.getUnits().getUnitName());
                            jsonObject.addProperty("unit_packing_id", unitPacking.getId());
                            jsonObject.addProperty("unit_id", unitPacking.getUnits().getId());
                            if (unitPacking.getIsNegativeStocks() == true) {
                                isNegetive = true;
                            }
                            jsonArray.add(jsonObject);
                        }
                        mObject.add("unit_data", jsonArray);

                        mObject.addProperty("unit_id",
                                productUnitPacking.get(0).getUnits() != null
                                        ? productUnitPacking.get(0).getUnits().getId()
                                        : 0);
                        mObject.addProperty("unit",
                                productUnitPacking.get(0).getUnits() != null
                                        ? productUnitPacking.get(0).getUnits().getUnitName()
                                        : "PCS");
                        mObject.addProperty("is_negative",
                                isNegetive);

                    } else {
                        mObject.addProperty("unit_id", 0);
                        mObject.addProperty("unit", "PCS");
                        mObject.addProperty("is_negative",
                                false);
                    }
                    mObject.addProperty("hsn",
                            mDetails.getProductHsn() != null ? mDetails.getProductHsn().getHsnNumber() : "");
                    mObject.addProperty("tax_type", mDetails.getTaxType());
                    mObject.addProperty("tax_per",
                            mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("igst",
                            mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getIgst() : 0);
                    mObject.addProperty("cgst",
                            mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getCgst() : 0);
                    mObject.addProperty("sgst",
                            mDetails.getTaxMaster() != null ? mDetails.getTaxMaster().getSgst() : 0);
                    mObject.addProperty("id", mDetails.getId());
                    mObject.addProperty("code", mDetails.getProductCode());
                    mObject.addProperty("product_name", mDetails.getProductName());
                    mObject.addProperty("packing",
                            mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getPackName() : "");
                    System.out.println("barcode" + mDetails.getBarcodeNo());
                    /*
                     * mObject.addProperty("fsrmh", mDetails.getFsrmh() != null ?
                     * mDetails.getFsrmh().toString() : "");
                     * mObject.addProperty("fsrai", mDetails.getFsrai() != null ?
                     * mDetails.getFsrai().toString() : "");
                     * mObject.addProperty("csrmh", mDetails.getCsrmh() != null ?
                     * mDetails.getCsrmh().toString() : "");
                     * mObject.addProperty("csrai", mDetails.getCsrai() != null ?
                     * mDetails.getCsrai().toString() : "");
                     */
                    mObject.addProperty("barcode", mDetails.getBarcodeNo() != null ? mDetails.getBarcodeNo() : "");
                    mObject.addProperty("is_batch", mDetails.getIsBatchNumber());
                    mObject.addProperty("is_inventory", mDetails.getIsInventory());
                    mObject.addProperty("is_serial", mDetails.getIsSerialNumber());
                    mObject.addProperty("is_gvproduct", mDetails.getIsGVProducts()); //! Specifically added for gv product
                    mObject.addProperty("brand",
                            mDetails.getBrand().getBrandName() != null ? mDetails.getBrand().getBrandName() : "");
                    mObject.addProperty("productType", mDetails.getProductType());
                    mObject.addProperty("drugType", mDetails.getDrugType() != null ? mDetails.getDrugType() : "");
                    if (mDetails.getIsBatchNumber()) {
                        /**** with Transaction Purchase Invoice *****/
                        TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository
                                .findTop1ByProductIdOrderByIdDesc(mDetails.getId());
                        if (tranxPurInvoiceDetailsUnits != null) {
                            mObject.addProperty("mrp",
                                    tranxPurInvoiceDetailsUnits.getProductBatchNo() != null
                                            ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() != null ?
                                            tranxPurInvoiceDetailsUnits.getProductBatchNo().getMrp() : 0.0
                                            : 0.00);
                            mObject.addProperty("sales_rate",
                                    tranxPurInvoiceDetailsUnits.getProductBatchNo() != null
                                            && tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate() != null
                                            ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getSalesRate()
                                            : 0.00);
                            mObject.addProperty("purchaserate",
                                    tranxPurInvoiceDetailsUnits.getProductBatchNo() != null
                                            && tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate() != null
                                            ? tranxPurInvoiceDetailsUnits.getProductBatchNo().getPurchaseRate()
                                            : 0.00);
                        }
                    } else if (mDetails.getIsSerialNumber()) {

                    } else {
                        if (productUnitPacking != null && productUnitPacking.size() > 0) {
                            mObject.addProperty("mrp", productUnitPacking.get(0).getMrp());
                            mObject.addProperty("purchaserate", productUnitPacking.get(0).getPurchaseRate());
                            mObject.addProperty("sales_rate", productUnitPacking.get(0).getMinRateA());
                        } else {
                            mObject.addProperty("mrp", 0);
                            mObject.addProperty("purchaserate", 0);
                            mObject.addProperty("sales_rate", 0);
                        }
                    }
                    Double productOpeningStocks = openingStocksRepository.findSumProductOpeningStocks(mDetails.getId(),
                            mDetails.getOutlet().getId(),
                            mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear.getId());
                    Double freeQty = inventoryCommonPostings.calculateFreeQty(mDetails.getId(),
                            users.getOutlet().getId(),
                            mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double closingStocks = inventoryCommonPostings.getClosingStockProduct(mDetails.getId(),
                            users.getOutlet().getId(),
                            mDetails.getBranch() != null ? mDetails.getBranch().getId() : null, fiscalYear);
                    Double currentStock = closingStocks + productOpeningStocks + freeQty;
                    mObject.addProperty("current_stock", currentStock);
                    JsonArray stkArray = unitConversion.showStocks(mDetails.getId());
                    mObject.add("unit_lst", stkArray);
                    result.add(mObject);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in productTransactionListNew:" + exceptionAsString);
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject validateProductBarcode(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        String productBarcode = "";
        Product product = null;
        JsonObject result = new JsonObject();
        try {
            if (paramMap.containsKey("productBarcode") && !request.getParameter("productBarcode").equalsIgnoreCase(""))
                productBarcode = request.getParameter("productBarcode");

            if (!productBarcode.isEmpty()) {
                if (users.getBranch() != null) {
                    product = productRepository.findByduplicateProductBarcodeWithBranch(users.getOutlet().getId(), users.getBranch().getId(), productBarcode, true);
                } else {
                    product = productRepository.findByduplicateProductBarcode(users.getOutlet().getId(), productBarcode, true);
                }
            }
            if (product != null) {
                result.addProperty("message", "Duplicate Product Barcode");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            } else {
                result.addProperty("message", "new product");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in ProductCode validations:" + exceptionAsString);
        }
        return result;
    }

    public Object getProductInfo(HttpServletRequest request) {
        JsonObject response = new JsonObject();

        try {
            JsonObject product_json = new JsonObject();
            Product product = productRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);

            product_json.addProperty("product_id", product.getId());
            product_json.addProperty("product_name", product.getProductName());

            List<ProductUnitPacking> list = productUnitRepository.findByProductIdAndStatus(
                    Long.valueOf(request.getParameter("id")), true);

            JsonArray jsonArray = new JsonArray();
            for (ProductUnitPacking productUnitPacking : list) {
                JsonObject productJson = new JsonObject();
                productJson.addProperty("unit_id", productUnitPacking.getUnits().getId());
                productJson.addProperty("unit_name", productUnitPacking.getUnits().getUnitName());
                jsonArray.add(productJson);
            }

            response.add("product", product_json);
            response.add("list", jsonArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());

        } catch (NumberFormatException | NullPointerException e) {
            response.addProperty("error", "Invalid request parameter");
            productLogger.error("Error in getProductInfo: " + e.getMessage());
        } catch (Exception e) {
            response.addProperty("error", "Internal server error");
            productLogger.error("Error in getProductInfo: " + e.getMessage());
        }
        return response;
    }

    public Object createOpeningStock(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();

        JsonObject response = new JsonObject();

        try {
            Product product = productRepository.findByIdAndStatus(Long.parseLong(request.getParameter("product_id")), true);
            Units unit = unitsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("unit_id")), true);
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());

            ProductBatchNo mproductBatchNo = new ProductBatchNo();
            ProductBatchNo productBatchNo = new ProductBatchNo();

            if (paramMap.containsKey("batch_no"))
                mproductBatchNo.setBatchNo(request.getParameter("batch_no"));
            if (paramMap.containsKey("mrp"))
                mproductBatchNo.setMrp(Double.valueOf(request.getParameter("mrp")));
            if (paramMap.containsKey("purchase_rate"))
                mproductBatchNo.setPurchaseRate(Double.valueOf(request.getParameter("purchase_rate")));

            if (paramMap.containsKey("sale_rate"))
                mproductBatchNo.setSalesRate(Double.valueOf(request.getParameter("sale_rate")));
            mproductBatchNo.setMinRateA(Double.valueOf(request.getParameter("sale_rate")));
            if (paramMap.containsKey("free_qty"))
                mproductBatchNo.setFreeQty(Double.valueOf(request.getParameter("free_qty")));
            if (paramMap.containsKey("mfg_date")) {
                if (!request.getParameter("mfg_date").isEmpty() && request.getParameter("mfg_date") != null) {
                    mproductBatchNo.setManufacturingDate(LocalDate.parse(request.getParameter("mfg_date")));
                }
            }
            if (paramMap.containsKey("expire_date")) {
                if (!request.getParameter("expire_date").isEmpty() && request.getParameter("expire_date") != null) {
                    mproductBatchNo.setExpiryDate(LocalDate.parse(request.getParameter("expire_date")));
                }
            }

            mproductBatchNo.setQnty(Integer.parseInt(request.getParameter("opening_qty")));
            mproductBatchNo.setStatus(true);
            mproductBatchNo.setProduct(product);
            mproductBatchNo.setOutlet(users.getOutlet());
            mproductBatchNo.setBranch(users.getBranch());
            mproductBatchNo.setUnits(unit);
            if (fiscalYear != null) mproductBatchNo.setFiscalYear(fiscalYear);
            productBatchNo = productBatchNoRepository.save(mproductBatchNo);


            ProductOpeningStocks newOpeningStock = new ProductOpeningStocks();

            newOpeningStock.setOpeningStocks(Double.parseDouble(request.getParameter("opening_qty")));
            newOpeningStock.setOpeningQty(Double.parseDouble(request.getParameter("opening_qty")));

            newOpeningStock.setProduct(product);
            newOpeningStock.setUnits(unit);
            newOpeningStock.setBranch(users.getBranch());
            newOpeningStock.setOutlet(users.getOutlet());
            newOpeningStock.setProductBatchNo(productBatchNo);


            if (paramMap.containsKey("free_qty"))
                newOpeningStock.setFreeOpeningQty(Double.valueOf(request.getParameter("free_qty")));
            if (paramMap.containsKey("mrp"))
                newOpeningStock.setMrp(Double.valueOf(request.getParameter("mrp")));
            if (paramMap.containsKey("purchase_rate"))
                newOpeningStock.setPurchaseRate(Double.valueOf(request.getParameter("purchase_rate")));
            if (paramMap.containsKey("sale_rate"))
                newOpeningStock.setSalesRate(Double.valueOf(request.getParameter("sale_rate")));
            if (paramMap.containsKey("mfg_date")) {
                if (!request.getParameter("mfg_date").isEmpty() && request.getParameter("mfg_date") != null) {
                    newOpeningStock.setManufacturingDate(LocalDate.parse(request.getParameter("mfg_date")));
                }
            }
            if (paramMap.containsKey("expire_date")) {
                if (!request.getParameter("expire_date").isEmpty() && request.getParameter("expire_date") != null) {
                    newOpeningStock.setExpiryDate(LocalDate.parse(request.getParameter("expire_date")));
                }
            }
            if (paramMap.containsKey("costing"))
                newOpeningStock.setCosting(Double.valueOf(request.getParameter("costing")));
            if (fiscalYear != null)
                newOpeningStock.setFiscalYear(fiscalYear);
            //            newOpeningStock.setLevelA(levelA);
//            newOpeningStock.setLevelB(levelB);
//            newOpeningStock.setLevelC(levelC);

            newOpeningStock.setStatus(true);

            openingStocksRepository.save(newOpeningStock);


        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            productLogger.error("Error in Create Opening Stock:" + exceptionAsString);
        }

        response.addProperty("message", "Stock Created Successfully ");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        return response;
    }

    public Object getProAndUnis(HttpServletRequest request) {
        JsonObject response = new JsonObject();

        try {

            List<ProductOpeningStocks> list = openingStocksRepository.findByProductIdAndStatus(Long.parseLong(request.getParameter("id")), true);

            JsonArray jsonArray = new JsonArray();
            for (ProductOpeningStocks productOpeningStocks : list) {
                JsonObject productJson = new JsonObject();
                productJson.addProperty("product_id", productOpeningStocks.getProduct().getId());
                productJson.addProperty("product_name", productOpeningStocks.getProduct().getProductName());
                productJson.addProperty("unit_id", productOpeningStocks.getUnits().getId());
                productJson.addProperty("unit_name", productOpeningStocks.getUnits().getUnitName());
                productJson.addProperty("batch_id", productOpeningStocks.getProductBatchNo().getId());
                productJson.addProperty("batch_no", productOpeningStocks.getProductBatchNo().getBatchNo());

                productJson.addProperty("mrp", productOpeningStocks.getMrp());
                productJson.addProperty("purchase_rate", productOpeningStocks.getPurchaseRate());
                productJson.addProperty("sale_rate", productOpeningStocks.getSalesRate());
                productJson.addProperty("free_qty", productOpeningStocks.getFreeOpeningQty());
                productJson.addProperty("mfg_date", String.valueOf(productOpeningStocks.getManufacturingDate()));
                productJson.addProperty("expire_date", String.valueOf(productOpeningStocks.getExpiryDate()));
                productJson.addProperty("opening_qty", productOpeningStocks.getOpeningStocks());
                productJson.addProperty("costing", productOpeningStocks.getCosting());

                jsonArray.add(productJson);
            }

            response.add("list", jsonArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());

        } catch (NumberFormatException | NullPointerException e) {
            response.addProperty("error", "Invalid request parameter");
            productLogger.error("Error in getProductInfo: " + e.getMessage());
        } catch (Exception e) {
            response.addProperty("error", "Internal server error");
            productLogger.error("Error in getProductInfo: " + e.getMessage());
        }
        return response;
    }

}