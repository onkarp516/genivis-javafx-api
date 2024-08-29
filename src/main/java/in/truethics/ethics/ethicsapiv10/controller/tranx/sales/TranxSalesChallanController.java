package in.truethics.ethics.ethicsapiv10.controller.tranx.sales;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.sales.TranxSalesChallanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxSalesChallanController {
    @Autowired
    private TranxSalesChallanService service;

    /* Create Sales Challan Screen */
    @PostMapping(path = "/create_sales_challan")
    public ResponseEntity<?> createSalesChallanInvoices(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.createSalesChallanInvoice(request));
    }

    @PostMapping(path = "/sc_to_pc_invoices")
    public ResponseEntity<?> createScToPcInvoice(HttpServletRequest request) {
        return ResponseEntity.ok(service.createScToPcInvoice(request));
    }

    /* update Sales Challan */
    @PostMapping(path = "/edit_sales_challan")
    public ResponseEntity<?> updateSalesChallan(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.updateSalesChallan(request));
    }

    /* get last records of sales challan */
    @GetMapping(path = "/get_last_sales_challan_record")
    public Object salesChallanLastRecord(HttpServletRequest request) {
        JsonObject result = service.salesChallanLastRecord(request);
        return result.toString();
    }

    /* get all sales challan of Outlet*/
    @PostMapping(path = "/all_list_sale_challan")
    public Object AllSaleChallanList(HttpServletRequest request) {
        JsonObject result = service.AllSaleChallanList(request);
        return result.toString();
    }

    //for pagination start : for Loading 50 rows
    @PostMapping(path = "/list_sale_challan")
    public Object saleChallanList(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return service.saleChallanList(request, req);
    }
    //for pagination end

    @PostMapping(path = "/saleChallan_pending_list")
    public Object saleChallanPendingList(HttpServletRequest request) {
        JsonObject result = service.saleChallanPendingList(request);
        return result.toString();
    }

    /* get all sales challan of Outlet with id for Convesions*/
    @PostMapping(path = "/get_sale_challan_with_ids")
    public Object getSaleChallanWithIds(HttpServletRequest request) {
        JsonObject result = service.getSaleChallanWithIds(request);
        return result.toString();
    }

    /* get Sales Challan by Id for Edit */
    @PostMapping(path = "/get_sale_challan_with_id")
    public Object getSalesChallan(HttpServletRequest request) {
        JsonObject result = service.getSalesChallan(request);
        return result.toString();
    }

    /*** getProduct By Id for Edit Transactions : new Architecture (multi brand,category,flavour,package,and units)****/
    @PostMapping(path = "/get_sales_challan_by_id_new")
    public Object getSalesChallanByIdNew(HttpServletRequest request) {
        return service.getSalesChallanByIdNew(request).toString();
    }

    /***** Delete sales challan ****/
    @PostMapping(path = "/delete_sales_challan")
    public Object salesChallanDelete(HttpServletRequest request) {
        JsonObject object = service.salesChallanDelete(request);
        return object.toString();
    }

    @PostMapping(path = "/get_sales_challan_product_fpu_by_id")
    public Object getProductEditByIdByFPU(HttpServletRequest request) {
        return service.getProductEditByIdByFPU(request).toString();
    }

    /****** if multiple challan ids are aavailable for conversions *******/
    @PostMapping(path = "/get_sales_challan_product_fpu_by_ids")
    public Object getProductEditByIdsByFPU(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdsByFPU(request);
        return result.toString();
    }
    @PostMapping(path = "/get_sales_challan_supplierlist_by_productid")
    public Object getSalesChallanSupplierListByProductId(HttpServletRequest request){
        return service.getSalesChallanSupplierListByProductId(request).toString();
    }
    @PostMapping(path = "/validate_sales_challan")
    public ResponseEntity<?> validateSalesChallan(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesChallan(request));
    }
    @PostMapping(path = "/validate_sales_challan_update")
    public ResponseEntity<?> validateSalesChallanUpdate(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesChallanUpdate(request));
    }

}
