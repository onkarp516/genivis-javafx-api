package in.truethics.ethics.ethicsapiv10.controller.tranx.purchase;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase.TranxPurInvoiceService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxPurInvoiceController {
    @Autowired
    private TranxPurInvoiceService service;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;


    /* getting pending amount of sundry creditors againt purchase return(new reference)   */
   /* @PostMapping(path = "/get_outstanding_pur_return_amt")
    public Object getOutStandingPurchaseReturnAmt(HttpServletRequest request) {
        JsonObject result = service.getOutStandingPurchaseReturnAmt(request);
        return result.toString();
    }*/

    /* create Tranx Purchase Invoice  */
    @PostMapping(path = "/create_purchase_invoices")
    public ResponseEntity<?> createPurInvoices(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(service.insertPurchaseInvoices(request));
    }

    /***** Validate duplicate Purchase  Invoices  *****/
    @PostMapping(path = "/validate_purchase_invoices")
    public ResponseEntity<?> validatePurchaseInvoices(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validatePurchaseInvoices(request));
    }

    /***** Validate duplicate Purchase  Invoices  while update *****/
    @PostMapping(path = "/validate_purchase_invoices_update")
    public ResponseEntity<?> validateInvoiceNoUpdateNo(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateInvoiceNoUpdateNo(request));
    }

    /* edit functionality of Purchase  */
    @PostMapping(path = "/edit_purchase_invoices")
    public ResponseEntity<?> purchaseEdit(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(service.editPurchaseInvoice(request));
    }

    /***** Delete Purchase Invoice ****/
    @PostMapping(path = "/delete_purchase_invoices")
    public Object purchaseDelete(HttpServletRequest request) {
        JsonObject object = service.purchaseDelete(request);
        return object.toString();
    }

    /* Count purchase invoices */
    @GetMapping(path = "/get_last_invoice_record")
    public Object purchaseLastRecord(HttpServletRequest request) {
        JsonObject result = service.purchaseLastRecord(request);
        return result.toString();
    }

    /* find all purchase invoices outletwise */
    @PostMapping(path = "/all_list_purchase_invoice")
    public Object AllpurchaseList(HttpServletRequest request) {
        JsonObject result = service.AllpurchaseList(request);
        return result.toString();
    }

    @PostMapping(path = "/list_purchase_invoice")
    public Object purchaseList(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return service.purchaseList(request, req);
    }

    /* get Tranx Purchase invoice by id for edit */
    @PostMapping(path = "/get_purchase_invoice_by_id")
    public Object getPurchaseInvoiceById(HttpServletRequest request) {
        JsonObject result = service.getPurchaseInvoiceById(request);
        return result.toString();
    }

    /*** getProduct By Id for Edit Transactions : new Architecture (multi brand,category,flavour,package,and units)****/
    @PostMapping(path = "/get_purchase_invoice_by_id_new")
    public Object getPurchaseInvoiceByIdNew(HttpServletRequest request) {
        JsonObject result = service.getPurchaseInvoiceByIdNew(request);
        return result.toString();
    }

    /******* get Product Id for Purchase Invoice Edit *******/
    /*@PostMapping(path = "/get_purchase_ids")
    public Object getPurchaseIds(HttpServletRequest request) {
        JsonObject result = service.getPurchaseIds(request);
        return result.toString();
    }*/

    /******* get Product Id for Purchase Invoice Edit(multiple brands architecture) *******/
    @PostMapping(path = "/get_product_flavor_package_unit_by_id")
    public Object getProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdByFPU(request);
        return result.toString();
    }
    /***** Validations of Tax Applicable Date if product has multiple Taxes *****/

    /******* update product stock while purchase tranx network scenario  ********/
    @PostMapping(path = "/updateProductStock")
    public Object updateProductStock(HttpServletRequest request) {
        return service.updateProductStock(request).toString();
    }
    /******* update product stock while purchase tranx network scenario  ********/

    /****** get last five records of transaction of product supplier ********/
    @PostMapping(path = "/get_supplierlist_by_productid")
    public Object getSupplierListByProductId(HttpServletRequest request) {
        return service.getSupplierListByProductId(request).toString();
    }

    @PostMapping(path = "/mobile/sc_outstandng_list")
    public Object mobileSCOutstandingList(@RequestBody Map<String, String> request) {
        JsonObject result = service.mobileSCOutstandingList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/sc_ondate_list")
    public Object mobileOnDateList(@RequestBody Map<String, String> request) {
        JsonObject result = service.mobileOnDateList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/purchase_list")
    public Object purchasemobileList(@RequestBody Map<String, String> request) {
//        return  service.purchasemobileList(request);
        Object result = service.purchasemobileList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/purchase_invoice_details")
    public Object purchaseinvoicedetails(@RequestBody Map<String, String> request) {
//        return  service.purchasemobileList(request);
        Object result = service.purchaseinvoicedetails(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/payable_list")
    public Object mobilePayableList(@RequestBody Map<String, String> request) {
        JsonObject result = service.mobilePayableList(request);
        return result.toString();
    }

    @PostMapping(path = "/purchase_print_invoice")
    public Object purchasePrintInvoice(HttpServletRequest request) {
        JsonObject result = service.purchasePrintInvoice(request);
        return result.toString();
    }

    @PostMapping(path = "/GSTR2_purchase_invoice_details")
    public Object GSTR2purchaseInvoiceDetails(@RequestBody Map<String, String> request, HttpServletRequest req) {
        return service.GSTR2purchaseInvoiceDetails(request, req);
    }
}
