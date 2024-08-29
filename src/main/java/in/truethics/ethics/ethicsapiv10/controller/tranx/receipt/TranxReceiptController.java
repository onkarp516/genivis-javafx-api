package in.truethics.ethics.ethicsapiv10.controller.tranx.receipt;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.receipt.TranxReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxReceiptController {

    @Autowired
    private TranxReceiptService service;

    /* Count purchase invoices */
    @GetMapping(path = "/get_receipt_invoice_last_records")
    public Object receiptLastRecord(HttpServletRequest request) {
        JsonObject result = service.receiptLastRecord(request);
        return result.toString();
    }

    /* get sundry debtors list and indirect incomes for receipt */
    @GetMapping(path = "/get_sundry_debtors_indirect_incomes")
    public Object getSundryDebtorsAndIndirectIncomes(HttpServletRequest request) {
        JsonObject result = service.getSundryDebtorsAndIndirectIncomes(request);
        return result.toString();
    }

    /* Sundry debtors pending Bills */
    @PostMapping(path = "/get_debtors_pending_bills")
    public Object getDebtorsPendingBills(HttpServletRequest request) {
        JsonObject array = service.getDebtorsPendingBills(request);
        return array.toString();
    }

    /* Create Receipt */
    @PostMapping(path = "/create_receipt")
    public Object createReceipt(HttpServletRequest request) {
        JsonObject array = service.createReceipt(request);
        return array.toString();
    }
 /* Create Receipt */
    @PostMapping(path = "/create_receipt_fr")
    public Object createReceipAgainstFR(HttpServletRequest request) {
        JsonObject array = service.createReceipAgainstFR(request);
        return array.toString();
    }

    /* Get List of receipts   */
//    @GetMapping(path = "/get_receipt_list_by_outlet")
//    public Object receiptListbyOutlet(HttpServletRequest request) {
//        JsonObject array = service.receiptListbyOutlet(request);
//        return array.toString();
//    }
    @PostMapping(path = "/get_receipt_list_by_outlet")
    public Object receiptListbyOutlet( @RequestBody Map<String, String> request, HttpServletRequest req) {

        return  service.receiptListbyOutlet(request, req);
    }

    @PostMapping(path = "/update_receipt")
    public Object updateReceipt(HttpServletRequest request) {
        JsonObject array = service.updateReceipt(request);
        return array.toString();
    }

    @PostMapping(path = "/get_receipt_by_id")
    public Object getReceiptById(HttpServletRequest request) {
        JsonObject array = service.getReceiptById(request);
        return array.toString();
    }
//    @GetMapping(path = "/get_receipt_list_by_outlet")
//    public void get_receipt_list_by_outlet(HttpServletRequest request){
//        System.out.println("called fun get_receipt_list_by_outlet");
//    }

    /***** Delete Receipt  ****/
    @PostMapping(path = "/delete_receipt")
    public Object deleteReceipt(HttpServletRequest request) {
        JsonObject object = service.deleteReceipt(request);
        return object.toString();
    }

    @PostMapping(path = "/receipt_posting")
    public Object receiptPosting(HttpServletRequest request){
        JsonObject object = service.receiptPosting(request);
        return  object.toString();
    }
    /***** Validate duplicate Receipt Voucher  *****/
    @PostMapping(path = "/validate_receipt")
    public ResponseEntity<?> validateReceipt(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateReceipt(request));
    }

    /***** Validate duplicate Receipt while update invoice *****/
    @PostMapping(path = "/validate_receipt_update")
    public ResponseEntity<?> validateReceiptUpdate(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateReceiptUpdate(request));
    }

    @PostMapping(path = "/mobile/receipt_list")
    public Object mobileReceiptList(@RequestBody Map<String, String> request) {
        JsonObject result = service.mobileReceiptList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/receipt_invoice_list")
    public Object mobileReceiptInvoiceList(@RequestBody Map<String, String> request) {
        JsonObject result = service.mobileReceiptInvoiceList(request);
        return result.toString();
    }

}
