package in.truethics.ethics.ethicsapiv10.controller.masters;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ProductController {
    @Autowired
    private ProductService productService;

    /*** Multilvel Architecture PK Visit *****/
    @PostMapping(path = "/create_product")
    public ResponseEntity<?> createNewProduct(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(productService.createNewProduct(request));
    }

    /***** Optimising Code for Product Creation (Kiran visit Solapur) ******/
    @PostMapping(path = "/create_new_product")
    public ResponseEntity<?> createProductNew(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(productService.createProductNew(request));
    }


    @PostMapping(path = "/import_product")
    public ResponseEntity<?> importProduct(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(productService.importProduct(request));
    }

    @PostMapping(path = "/import_ethiq_product")
    public ResponseEntity<?> importEthiqProduct(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(productService.importEthiqProduct(request));
    }

    @PostMapping(path = "/import_product_stock")
    public ResponseEntity<?> importProductStock(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(productService.importProductStock(request));
    }

    @PostMapping(path = "/import_content_master")
    public ResponseEntity<?> importContentMaster(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(productService.importContentMaster(request));
    }

    /****** validate Product Name for Duplication *****/
    @PostMapping(path = "/validate_product")
    public Object validateProduct(HttpServletRequest request) {
        JsonObject object = productService.validateProduct(request);
        return object.toString();
    }

    /****** validate Product Name for Duplication for Update *****/
    @PostMapping(path = "/validate_product_update")
    public Object validateProductUpdate(HttpServletRequest request) {
        JsonObject object = productService.validateProductUpdate(request);
        return object.toString();
    }

    /****** validate Product Name for Duplication *****/
    @PostMapping(path = "/validate_product_code")
    public Object validateProductCode(HttpServletRequest request) {
        JsonObject object = productService.validateProductCode(request);
        return object.toString();
    }

    @PostMapping(path = "/validate_product_barcode")
    public Object validateProductBarcode(HttpServletRequest request) {
        JsonObject object = productService.validateProductBarcode(request);
        return object.toString();
    }

    /****** validate Product Code for Duplication for Update *****/
    @PostMapping(path = "/validate_product_code_update")
    public Object validateProductCodeUpdate(HttpServletRequest request) {
        JsonObject object = productService.validateProductCodeUpdate(request);
        return object.toString();
    }

    /* Get Product by id for edit  Multilvel Architecture PK Visit */
    @PostMapping(path = "/get_product_by_id_flavour")
    public Object getProductByIdEditFlavourNew(HttpServletRequest request) {
        JsonObject mObject = productService.getProductByIdEditFlavourNew(request);
        return mObject.toString();
    }

    /* Get Product by id for edit  Multilvel Architecture Optimising Code (kiran Visit Solapur) *****/
    @PostMapping(path = "/get_product_by_id_new")
    public Object getProductByIdEditNew(HttpServletRequest request) {
        JsonObject mObject = productService.getProductByIdEditNew(request);
        return mObject.toString();
    }

    @PostMapping(path = "/update_product")
    public ResponseEntity<?> updateProduct(HttpServletRequest request) {
        return ResponseEntity.ok(productService.updateProduct(request));
    }

    /* Get Product edit  Multilvel Architecture Optimising Code (kiran Visit Solapur) *****/
    @PostMapping(path = "/update_product_new")
    public ResponseEntity<?> updateProduct_new(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(productService.updateProduct_new(request));
    }

    /* Get Product by id for edit including package and Units* : new architecture */
    @GetMapping(path = "/get_product")
    public Object getProduct(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        result = productService.getProduct(request);
        return result.toString();
    }

    /* Get All Products by outletwise with barcodes */
    @GetMapping(path = "/get_all_product")
    public Object getProductsOfOutlet(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        result = productService.getProductsOfOutlet(request);
        return result.toString();
    }

    @PostMapping(path = "/get_all_product_new")
    public ResponseEntity<?> getProductsOfOutletNew(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return ResponseEntity.ok(productService.getProductsOfOutletNew(request, req));
    }

    @PostMapping(path = "/get_all_product_min_level")
    public ResponseEntity<?> getProductsOfOutletNew1(@RequestBody Map<String, String> request, HttpServletRequest req) {
        return ResponseEntity.ok(productService.getProductsOfOutletNew1(request, req));
    }

    /* Get all brands,category, subcategoey and flavour, packings and its all units of products: new architecture */
    @PostMapping(path = "/get_all_product_units_packings_flavour")
    public Object getUnitsPackingsFlavours(HttpServletRequest request) {
        JsonObject result = productService.getUnitsPackingsFlavours(request);
        return result.toString();
    }

    /***** get Product By Id, Multilvel Architecture PK Visit *****/
    @PostMapping(path = "/get_product_units_levels")
    public Object getByIdEdit(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        result = productService.getByIdEdit(request);
        return result.toString();
    }

    @PostMapping(path = "/delete_Product_list")
    public Object deleteProductList(HttpServletRequest request) {
        JsonObject result = productService.deleteProductList(request);
        return result.toString();
    }
    @PostMapping(path = "/delete_product")
    public Object deleteProduct(HttpServletRequest request) {
        JsonObject result = productService.deleteProduct(request);
        return result.toString();
    }

    /* Get Product : at Transactions */
    @PostMapping(path = "/transaction_product_list")
    public Object productTransactionList(HttpServletRequest request) {
        return productService.productTransactionList(request).toString();
    }


    /*** Product , Barcode and Company Barcode Search functionality of Product selection in Tranx Perticular *****/
    @PostMapping(path = "/transaction_product_list_new")
    public Object productTransactionListNew(HttpServletRequest request) {
        return productService.productTransactionListNew(request).toString();
    }
    /*** JAVAFX Product , Barcode and Company Barcode Search functionality of Product selection in Tranx Perticular *****/
    @PostMapping(path = "/transaction_product_list_multiunit")
    public Object productTransactionListMultiUnit(HttpServletRequest request) {
        return productService.productTransactionListMultiUnit(request).toString();
    }
    @PostMapping(path = "/transaction_product_list_by_content")
    public Object productTransactionListByContent(HttpServletRequest request) {
        return productService.productTransactionListByContent(request).toString();
    }
    @PostMapping(path = "/productListByContentDetails")
    public Object productListByContentDetails(HttpServletRequest request) {
        return productService.productListByContentDetails(request).toString();
    }

    /****** Product Details by id at Transactions ******/
    @PostMapping(path = "/transaction_product_details")
    public Object productTransactionsDetails(HttpServletRequest request) {
        return productService.productTransactionsDetails(request).toString();
    }

    /****** Product Details by product barcode at Transactions ******/
    @PostMapping(path = "/transaction_product_list_by_barcode")
    public Object productTransactionsListByBarcode(HttpServletRequest request) {
        return productService.productTransactionsListByBarcode(request).toString();
    }

    /****** Product LevelB details at transactions *****/
    @PostMapping(path = "/product_details_levelB")
    public Object productDetailsLevelB(HttpServletRequest request) {
        return productService.productDetailsLevelB(request).toString();
    }

    /****** Product LevelC details at transactions *****/
    @PostMapping(path = "/product_details_levelC")
    public Object productDetailsLevelC(HttpServletRequest request) {
        return productService.productDetailsLevelC(request).toString();
    }

    /****** Product Units details at transactions *****/
    @PostMapping(path = "/product_units")
    public Object productUnits(HttpServletRequest request) {
        return productService.productUnits(request).toString();
    }

    /****** get Last Product data for fast product creation(Vaasu input) *****/
    @GetMapping(path = "/get_last_product_data")
    public Object getLastproductData(HttpServletRequest request) {
        return productService.getLastproductData(request).toString();
    }

    /****** get Product Purchase rate for Non batch and non Serials Number product according to
     * levela,levelb,levelc, and unit *****/
    @PostMapping(path = "/get_purchase_rate")
    public Object getPurchaseRateProduct(HttpServletRequest request) {
        return productService.getPurchaseRateProduct(request).toString();
    }

    /**** Stock Report for Mobile APP *****/

    @PostMapping(path = "/mobile/stock_product_list")
    public Object productStockList() {
        return productService.productStockList().toString();
    }

    @PostMapping(path = "/mobile/v1/gv-live-prd-list")
    public ResponseEntity<?> GVLiveProductList(@RequestBody Map<String, String> request, HttpServletRequest req) {
//        System.out.println("productService.GVProductList(request,req).toString()"+productService.GVProductList(request,req).toString());
        Object res = productService.GVProductList(request,req);
        return ResponseEntity.ok(res);
    }

    @PostMapping(path = "/get_product_info")
    public Object getProductInfo(HttpServletRequest request){
        return productService.getProductInfo(request).toString();
    }

    @PostMapping(path = "/create_product_open_stock")
    public Object createProductOpenStock(HttpServletRequest request){
        return productService.createOpeningStock(request).toString();
    }

    @PostMapping(path = "/get_pro_and_unis")
    public Object getProAndUnis(HttpServletRequest request){
        return productService.getProAndUnis(request).toString();
    }

}
