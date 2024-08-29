package in.truethics.ethics.ethicsapiv10.controller.barcode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.Barcode_service.ProductBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ProductBatchNoController {

    @Autowired
    private ProductBatchService service;

    @PostMapping(path = "/get_Product_batch")
    public Object getProductBatch(HttpServletRequest request) {
        JsonObject jsonObject = service.getProductBatch(request);
        return jsonObject.toString();
    }

    /**** JavaFX : get Product Batch for Multi Unit ****/
    @PostMapping(path = "/get_Product_batch_multitunit")
    public Object getProductBatchMultiUnit(HttpServletRequest request) {
        JsonObject jsonObject = service.getProductBatchMultiUnit(request);
        return jsonObject.toString();
    }

    @PostMapping(path = "/get_product_stock")
    public Object getProductStock(HttpServletRequest request) {
        JsonArray jsonArray = service.getProductStock(request);
        return jsonArray.toString();
    }

    @PostMapping(path = "/get_product_whole_stock")
    public Object getProductWholeStock(HttpServletRequest request) {
        JsonObject result=new JsonObject();
        JsonArray jsonArray = service.getProductWholeStock(request);
        if(!jsonArray.isJsonNull() && jsonArray.size()>0){
            result.addProperty("message", "Data found");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("resData", jsonArray);
        }else{
            result.addProperty("message", "No Data found");
            result.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.add("resData", new JsonArray());
        }
        return result.toString();
    }

    /****** get Batch Number *****/
    @PostMapping(path = "/fetch_Product_batch")
    public Object fetchProductBatchNo(HttpServletRequest request) {
        JsonObject jsonObject = service.fetchProductBatchNo(request);
        return jsonObject.toString();
    }

    /****** Batch Details by id at Transactions ******/
    @PostMapping(path = "/transaction_batch_details")
    public Object batchTransactionsDetails(HttpServletRequest request) {
        return service.batchTransactionsDetails(request).toString();
    }

    /****** Batch Details by id at Product List ******/
    @PostMapping(path = "/product_batch_details")
    public Object batchProductDetails(HttpServletRequest request) {
        return service.batchProductDetails(request).toString();
    }

    /****** Create Batch while creating purchase invoice transactions ******/
    @PostMapping(path = "/create_batch_details")
    public Object createBatchDetails(HttpServletRequest request) {
        return service.createBatchDetails(request).toString();
    }

    /****** Edit Batch while creating or editing purchase invoice transactions ******/
    @PostMapping(path = "/edit_batch_details")
    public Object editBatchDetails(HttpServletRequest request) {
        return service.editBatchDetails(request).toString();
    }
    /**** Delete Batch while cancelling the purchase trasaction,if it is already created ******/
    @PostMapping(path = "/delete_product_batch")
    public Object productBatchDelete(HttpServletRequest request) {
        JsonObject object = service.productBatchDelete(request);
        return object.toString();
    }

}
