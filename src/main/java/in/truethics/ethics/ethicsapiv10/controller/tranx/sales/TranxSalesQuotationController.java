package in.truethics.ethics.ethicsapiv10.controller.tranx.sales;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.sales.TranxSalesQuotationService;
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
public class TranxSalesQuotationController {

    @Autowired
    private TranxSalesQuotationService service;

    /* Create Sales Quotation Screen */
    @PostMapping(path = "/create_sales_quotation")
    public Object createSalesInvoices(HttpServletRequest request) throws Exception {
        JsonObject object =service.saveSalesQuotation(request);
        return object.toString();

    }

    /* update Sales Quotation Screen */
    @PostMapping(path = "/update_sales_quotation")
    public ResponseEntity<?> updateSalesQuotation(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.updateSalesQuotation(request));
    }

    /* Count sales quotation invoices : Last Quotation record */
    @GetMapping(path = "/get_last_sales_quotation_record")
    public Object salesQuotationLastRecord(HttpServletRequest request) {
        JsonObject result = service.salesQuotationLastRecord(request);
        return result.toString();
    }

    /* List of Sales Quotations outletwise */
    @PostMapping(path = "/all_list_sales_quotations")
    public Object AllsQInvoiceList(HttpServletRequest request) {
        JsonObject result = service.AllsQInvoiceList(request);
        return result.toString();
    }

    /**** Paginations ***/
    @PostMapping(path = "/list_sales_quotations")
    public Object sQInvoiceList(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return service.sQInvoiceList( request, req);
    }



   /* @GetMapping(path = "/list_sale_quotation")
    public Object saleQuotationList(HttpServletRequest request) {
        JsonObject result = service.saleQuotationList(request);
        return result.toString();
    }*/

    /* conversions of sales quoatations  into order,challan or invoice */
    @PostMapping(path = "/get_sale_quotation_with_ids")
    public Object getSaleQuotationWithIds(HttpServletRequest request) {
        JsonObject result = service.getSaleQuotationWithIds(request);
        return result.toString();
    }

    /*@PostMapping(path = "/migrate_quotation_to_order")
    public Object quotationToOrder(HttpServletRequest request) throws Exception {

        JsonObject jsonObject=service.migrateQuotationToOrder(request);

        return jsonObject.toString();
    }*/


    /***** Delete sales Quotation ****/
    @PostMapping(path = "/delete_sales_quotation")
    public Object salesQuotationDelete(HttpServletRequest request) {
        JsonObject object = service.salesQuotationDelete(request);
        return object.toString();
    }

    /* get Sales Quotation with id for edit*/
    @PostMapping(path = "/get_sales_quotation_by_id")
    public Object getSalesQuotation(HttpServletRequest request) throws Exception {
        JsonObject output = service.getSalesQuotation(request);
        return output.toString();
    }

    /*** getProduct By Id for Edit Transactions : new Architecture (multi brand,category,flavour,package,and units)****/
    @PostMapping(path = "/get_sales_quotation_by_id_new")
    public Object getSalesQuotationByIdNew(HttpServletRequest request) {
        JsonObject result = service.getSalesQuotationByIdNew(request);
        return result.toString();
    }

    @GetMapping(path = "/get_all_sales_quotations")
    public Object getAllSalesQuotations(HttpServletRequest request) throws Exception {
        //System.out.println(jsonObject);
        JsonObject output = new JsonObject();
        JsonArray result = service.getAllSalesQuotations(request);
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

    @PostMapping(path = "/get_sales_quotation_product_fpu_by_id")
    public Object getProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdByFPU(request);
        return result.toString();
    }

    /****** if multiple quotation ids are aavailable for conversions *******/
    @PostMapping(path = "/get_sales_quotation_product_fpu_by_ids")
    public Object getProductEditByIdsByFPU(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdsByFPU(request);
        return result.toString();
    }
    @PostMapping(path = "/get_sales_quotation_supplierlist_by_productid")
    public Object getSalesQuotationSupplierListByProductId(HttpServletRequest request){
        return service.getSalesQuotationSupplierListByProductId(request).toString();
    }

   @PostMapping(path = "/get_quotation_bill_print")
    public Object getQuotationBillPrint(HttpServletRequest request) {
        JsonObject object = service.getQuotationBillPrint(request);
        return object.toString();
    }
    @PostMapping(path = "/sq_pending_list")
    public Object saleQuotationPendingList(HttpServletRequest request) {
        JsonObject result = service.saleQuotationPendingList(request);
        return result.toString();
    }
    @PostMapping(path = "/validate_sales_quotation")
    public ResponseEntity<?> validateSalesQuotation(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesQuotation(request));
    }

    /***** Validate duplicate Sales Quotation while update invoice *****/
    @PostMapping(path = "/validate_sales_quotation_update")
    public ResponseEntity<?> validateSalesQuotationUpdate(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesQuotationUpdate(request));
    }


}
