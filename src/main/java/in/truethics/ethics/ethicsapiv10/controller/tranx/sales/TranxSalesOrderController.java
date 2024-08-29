package in.truethics.ethics.ethicsapiv10.controller.tranx.sales;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.sales.TranxSalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxSalesOrderController {

    @Autowired
    private TranxSalesOrderService service;

    /* Create Sales order */
    @PostMapping(path = "/create_sales_order_invoice")
    public Object createSalesOrder(HttpServletRequest request) {
        JsonObject object =service.createSalesOrder(request);
        return object.toString();
//        return ResponseEntity.ok(service.createSalesOrder(request));
    }

    @PostMapping(path = "/update_sales_order")
    public ResponseEntity<?> editSalesOrder(HttpServletRequest request) {
        return ResponseEntity.ok(service.editSalesOrder(request));
    }

    /* Count Sales orders */
    @GetMapping(path = "/get_last_sales_order_record")
    public Object salesOrderLastRecord(HttpServletRequest request) {
        JsonObject result = service.salesOrderLastRecord(request);
        return result.toString();
    }

    @GetMapping(path = "/get_all_sales_orders")
    public Object getAllSalesOrders(HttpServletRequest request) throws Exception {
        JsonObject output = new JsonObject();
        JsonArray result = service.getAllSalesOrders(request);
        if (result.size() > 0) {
            output.addProperty("message", "success");
            output.addProperty("responseStatus", HttpStatus.OK.value());
            output.add("data", result);
        } else {
            output.addProperty("message", "No data found");
            output.addProperty("responseStatus", "" + HttpStatus.NOT_FOUND);
        }

        return output.toString();
    }

    /* get all sales orders of Outlet*/
    @PostMapping(path = "/all_list_sale_orders")
    public Object AllsaleOrderList(HttpServletRequest request) {
        JsonObject result = service.AllsaleOrderList(request);
        return result.toString();
    }
    @PostMapping(path = "/list_sale_orders")
    public Object saleQuotationList(@RequestBody Map<String, String> request, HttpServletRequest req) {
        return  service.saleOrdersList(request, req);
    }

    @PostMapping(path = "/saleOrdersPendingList")
    public Object saleOrdersPendingList(HttpServletRequest request) {
        JsonObject result = service.saleOrdersPendingList(request);
        return result.toString();
    }

    /* get all sales orders of Outlet with id for Convesions*/
    @PostMapping(path = "/get_sale_orders_with_ids")
    public Object getSaleOrdersWithIds(HttpServletRequest request) {
        JsonObject result = service.getSaleOrdersWithIds(request);
        return result.toString();
    }

    /* get Sales Order by Id for Edit */
    @PostMapping(path = "/get_sales_order_by_id")
    public Object getSalesOrder(HttpServletRequest request) throws Exception {
        JsonObject output = service.getSalesOrder(request);
        return output.toString();
    }
    /*** getProduct By Id for Edit Transactions : new Architecture (multi brand,category,flavour,package,and units)****/
    @PostMapping(path = "/get_sales_order_by_id_new")
    public Object getSalesOrderByIdNew(HttpServletRequest request) {
        JsonObject result = service.getSalesOrderByIdNew(request);
        return result.toString();
    }

    @PostMapping(path = "/get_order_bill_print")
    public Object getOrderBillPrint(HttpServletRequest request) {
        JsonObject object = service.getOrderBillPrint(request);
        return object.toString();
    }

    /***** Delete sales Order ****/
    @PostMapping(path = "/delete_sales_order")
    public Object salesOrderDelete(HttpServletRequest request) {
        JsonObject object = service.salesOrderDelete(request);
        return object.toString();
    }
    @PostMapping(path = "/get_sales_order_product_fpu_by_id")
    public Object getProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdByFPU(request);
        return result.toString();
    }
    /****** if multiple order ids are aavailable for conversions *******/
    @PostMapping(path = "/get_sales_order_product_fpu_by_ids")
    public Object getProductEditByIdsByFPU(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdsByFPU(request);
        return result.toString();
    }
    @PostMapping(path = "/get_sales_order_supplierlist_by_productid")
    public Object getSalesOrderSupplierListByProductId(HttpServletRequest request){
        return service.getSalesOrderSupplierListByProductId(request).toString();
    }

    @PostMapping(path = "/validate_sales_order")
    public ResponseEntity<?> validateSalesOrder(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesOrder(request));
    }


    @PostMapping(path = "/validate_sales_order_update")
    public ResponseEntity<?> validateSalesOrderUpdate(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesOrderUpdate(request));
    }

}
