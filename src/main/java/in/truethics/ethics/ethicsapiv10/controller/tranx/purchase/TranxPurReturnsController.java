package in.truethics.ethics.ethicsapiv10.controller.tranx.purchase;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase.TranxPurReturnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxPurReturnsController {
    @Autowired
    private TranxPurReturnsService service;

    /* create Tranx Purchase returns Invoice  */
    @PostMapping(path = "/create_purchase_returns_invoices")
    public ResponseEntity<?> createPurReturnsInvoices(HttpServletRequest request) {
        return ResponseEntity.ok(service.createPurReturnsInvoices(request));
    }

    /**** Start ->>> Creating Purchase and editing Return for Upahar Manufacturing Unit BTOC *****/
    @PostMapping(path = "/create_pur_returns")
    public ResponseEntity<?> createPurchaseReturns(HttpServletRequest request) {
        return ResponseEntity.ok(service.createPurchaseReturns(request));
    }

    @PostMapping(path = "/edit_pur_returns")
    public ResponseEntity<?> editPurchaseReturns(HttpServletRequest request) {
        return ResponseEntity.ok(service.editPurchaseReturns(request));
    }

    /**** End ->>> Creating and editing Purchase Return for Upahar Manufacturing Unit *****/

    /* get last records of Purchase Returns  */
    @GetMapping(path = "/get_last_pur_returns_record")
    public Object purReturnsLastRecord(HttpServletRequest request) {
        JsonObject result = service.purReturnsLastRecord(request);
        return result.toString();
    }

    /* get all purchase returns of Outlet */
//    @PostMapping(path = "/get_pur_returns_by_outlet")
//    public Object purReturnsByOutlet(HttpServletRequest request) {
//        JsonObject result = service.purReturnsByOutlet(request);
//        return result.toString();
//    }
    @PostMapping(path = "/get_pur_returns_by_outlet")
    public Object purReturnsByOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return service.purReturnsByOutlet(request, req);
    }

    /* find all Purchase Invoices And Purchase Challans of Sundry Creditors/Suppliers wise , for Purchase Returns */
    @PostMapping(path = "/list_pur_invoice_supplier_wise")
    public Object purchaseListSupplierWise(HttpServletRequest request) {
        JsonObject result = service.purchaseListSupplierWise(request);
        return result.toString();
    }

    /***** Delete Purchase Returns ****/
    @PostMapping(path = "/delete_purchase_return")
    public Object purchaseReturnDelete(HttpServletRequest request) {
        JsonObject object = service.purchaseReturnDelete(request);
        return object.toString();
    }

    /*............ Purchase Returns ................  */
    /* Purchase Returns:  find all products of selected purchase invoice bill of sundry creditor */
    @PostMapping(path = "/list_pur_invoice_product_list")
    public Object productListPurInvoice(HttpServletRequest request) {
        JsonObject result = service.productListPurInvoice(request);
        return result.toString();
    }

    /* list of all selected products against purchase invoice bill for purchase returns */
    @PostMapping(path = "/get_pur_invoice_by_id_with_pr_ids")
    public Object getInvoiceByIdWithProductsId(HttpServletRequest request) {
        JsonObject result = service.getInvoiceByIdWithProductsId(request);
        return result.toString();
    }

    @PostMapping(path = "/get_pur_returns_product_fpu_by_id")
    public Object getProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdByFPU(request);
        return result.toString();
    }

    /* get Tranx Purchase invoice by id for edit */
    @PostMapping(path = "/get_purchase_return_by_id")
    public Object getPurchaseReturnById(HttpServletRequest request) {
        JsonObject result = service.getPurchaseReturnById(request);
        return result.toString();
    }

    /*** getProduct By Id for Edit Transactions : new Architecture (multi brand,category,flavour,package,and units)****/
    @PostMapping(path = "/get_purchase_return_by_id_new")
    public Object getPurchaseReturnByIdNew(HttpServletRequest request) {
        JsonObject result = service.getPurchaseReturnByIdNew(request);
        return result.toString();
    }

    @PostMapping(path = "/edit_purchase_returns_invoices")
    public Object purchaseReturnEdit(HttpServletRequest request) {
        JsonObject object = service.purchaseReturnEdit(request);
        return object.toString();
    }

    @PostMapping(path = "/get_purchase_return_supplierlist_by_productid")
    public Object getPurchaseReturnSupplierListByProductId(HttpServletRequest request) {
        return service.getPurchaseReturnSupplierListByProductId(request).toString();
    }

    @PostMapping(path = "/validate_purchase_return_invoices")
    public ResponseEntity<?> validatePurchaseReturnInvoices(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validatePurchaseReturnInvoices(request));
    }

    @PostMapping(path = "/validate_purchase_return_update")
    public ResponseEntity<?> validatePurchaseReturnUpdate(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validatePurchaseReturnUpdate(request));
    }

    @PostMapping(path = "/mobile/pur_return_list")
    public Object mobileSCReturnList(@RequestBody Map<String, String> request) {
        Object result = service.mobileSCReturnList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/purReturn_invoice_list")
    public Object purReturnMobileInvoiceList(@RequestBody Map<String, String> request) {
//        return  service.purchasemobileList(request);
        Object result = service.purReturnMobileInvoiceList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/purReturn_invoice_details_list")
    public Object purReturnMobileInvoiceDetailsList(@RequestBody Map<String, String> request) {
//        return  service.purchasemobileList(request);
        Object result = service.purReturnMobileInvoiceDetailsList(request);
        return result.toString();
    }
/******* List of Pending Bills for Purchase Return ******/
    @PostMapping(path = "/get_pending_bills")
    public Object getCreditorsPendingBills(HttpServletRequest request) {
        JsonObject array = service.getCreditorsPendingBills(request);
        return array.toString();
    }

    @PostMapping(path = "/purchase_print_return")
    public Object purchasePrintReturn(HttpServletRequest request) {
        JsonObject result = service.purchasePrintReturn(request);
        return result.toString();
    }
    /****** get last five records of transaction of product supplier ********/
//    @PostMapping(path = "/get_supplierlist_by_return")
//    public Object getSupplierListByReturn(HttpServletRequest request) {
//        return service.getSupplierListByReturn(request).toString();
//    }
}
