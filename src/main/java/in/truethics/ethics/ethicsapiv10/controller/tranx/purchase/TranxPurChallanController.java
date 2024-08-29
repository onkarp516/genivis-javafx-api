package in.truethics.ethics.ethicsapiv10.controller.tranx.purchase;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase.TranxPurChallanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxPurChallanController {

    @Autowired
    private TranxPurChallanService tranxPurChallanService;

    @PostMapping(path = "/create_po_challan_invoices")
    public ResponseEntity<?> createPOChallanInvoice(HttpServletRequest request) {
        return ResponseEntity.ok(tranxPurChallanService.insertPOChallanInvoice(request));
    }
    /* edit functionality of Purchase  */
    @PostMapping(path = "/edit_purchase_challan")
    public ResponseEntity<?> editPurchaseChallan(HttpServletRequest request) {
        return ResponseEntity.ok(tranxPurChallanService.editPurchaseChallan(request));
    }
    /***** Validate duplicate Purchase  Challan  *****/
    @PostMapping(path = "/validate_purchase_challan")
    public ResponseEntity<?> validatePurchaseChallan(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(tranxPurChallanService.validatePurchaseChallan(request));
    }

    /***** Validate duplicate Purchase  Challan  while update *****/
    @PostMapping(path = "/validate_purchase_challan_update")
    public ResponseEntity<?> validateChallanUpdate(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(tranxPurChallanService.validateChallanUpdate(request));
    }
    /* list of Purchase challans outletwise*/
//    @PostMapping(path = "/list_po_challan_invoice")
//    public Object poChallanInvoiceList(HttpServletRequest request) {
//        JsonObject result = tranxPurChallanService.poChallanInvoiceList(request);
//        return result.toString();
//    }
    //for pagination
    @PostMapping(path = "/list_po_challan_invoice")
    public Object poChallanInvoiceList(@RequestBody Map<String, String> request,  HttpServletRequest req) {

        return tranxPurChallanService.poChallanInvoiceList(request, req);
    }

    /***** Delete Purchase challan ****/

    @PostMapping(path = "/delete_purchase_challan")
    public Object purchaseChallanDelete(HttpServletRequest request) {
        JsonObject object = tranxPurChallanService.purchaseChallanDelete(request);
        return object.toString();
    }
    /* Conversion to Purchase challan to invoice */
    @PostMapping(path = "/get_po_challan_invoices_with_ids")
    public Object getPOChallanInvoiceWithIds(HttpServletRequest request) {
        JsonObject result = tranxPurChallanService.getPOChallanInvoiceWithIds(request);
        return result.toString();
    }
    /* get Purchase Order by Id for edit */
    @PostMapping(path = "/get_pur_challan_by_id")
    public Object getChallan(HttpServletRequest request) {
        JsonObject result = tranxPurChallanService.getChallan(request);
        return result.toString();
    }
    /*** getProduct By Id for Edit Transactions : new Architecture (multi brand,category,flavour,package,and units)****/
    @PostMapping(path = "/get_purchase_challan_by_id_new")
    public Object getPurchaseChallanByIdNew(HttpServletRequest request) {
        JsonObject result = tranxPurChallanService.getPurchaseChallanByIdNew(request);
        return result.toString();
    }
    /* Count pc invoices */
    @GetMapping(path = "/get_last_po_challan_record")
    public Object getChallanRecord(HttpServletRequest request) {
        JsonObject result = tranxPurChallanService.getChallanRecord(request);
        return result.toString();
    }

    /* Pending Purchase chasetPurchaseChallanEditdatallan  */
    @PostMapping(path = "/pC_pending_challans")
    public Object pCPendingOrder(HttpServletRequest request) {
        JsonObject result = tranxPurChallanService.pCPendingOrder(request);
        return result.toString();
    }


    /*@PostMapping(path = "/get_purchase_challan_by_id")
    public Object getPurchaseChallanById(HttpServletRequest request) {
        JsonObject result = tranxPurChallanService.getPurchaseChallanById(request);
        return result.toString();
    }*/

    @PostMapping(path = "/get_pur_challan_product_fpu_by_id")
    public Object getProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject result = tranxPurChallanService.getProductEditByIdByFPU(request);
        return result.toString();
    }
    /****** if multiple order ids are aavailable for conversions *******/
    @PostMapping(path = "/get_pur_challan_product_fpu_by_ids")
    public Object getProductEditByIdsByFPU(HttpServletRequest request) {
        JsonObject result = tranxPurChallanService.getProductEditByIdsByFPU(request);
        return result.toString();
    }
    @PostMapping(path = "/get_challan_supplierlist_by_productid")
    public Object getSupplierListByProductId(HttpServletRequest request){
        return tranxPurChallanService.getChallanSupplierListByProductId(request).toString();
    }

    @PostMapping(path = "/purchase_print_challan")
    public Object purchasePrintChallan(HttpServletRequest request) {
        JsonObject result = tranxPurChallanService.purchasePrintChallan(request);
        return result.toString();
    }
}

