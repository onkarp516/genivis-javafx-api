package in.truethics.ethics.ethicsapiv10.controller.tranx.payment;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.payment.TranxPaymentNewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxPaymentController {

    @Autowired
    private TranxPaymentNewService service;

    /* Count purchase invoices */
    @GetMapping(path = "/get_payment_invoice_last_records")
    public Object paymentLastRecord(HttpServletRequest request) {
        JsonObject result = service.paymentLastRecord(request);
        return result.toString();
    }

    /* get sundry creditors list and indirect expenses for Payment */
    @GetMapping(path = "/get_sundry_creditors_indirect_expenses")
    public Object getSundryCreditorAndIndirectExpenses(HttpServletRequest request) {
        JsonObject result = service.getSundryCreditorAndIndirectExpenses(request);
        return result.toString();
    }

    /* Sundry creditors pending Bills */
   /* @PostMapping(path = "/get_creditors_pending_bills")
    public Object getCreditorsPendingBills(HttpServletRequest request) {
        JsonObject array = service.getCreditorsPendingBills(request);
        return array.toString();
    }*/
    @PostMapping(path = "/get_creditors_pending_bills")
    public Object getCreditorsPendingBillsNew(HttpServletRequest request) {
        JsonObject array = service.getCreditorsPendingBillsNew(request);
        return array.toString();
    }

    /* Get Cash-In-Hand and Bank Account Ledger for Payments   */
    @GetMapping(path = "/get_cashAc_bank_account_details")
    public Object getCashAcBankAccountDetails(HttpServletRequest request) {
        JsonObject object = service.getCashAcBankAccountDetails(request);
        return object.toString();
    }

    /* Get List of Payments   */
    @GetMapping(path = "/get_all_payment_list_by_outlet")
    public Object getAllPaymentListbyOutlet(HttpServletRequest request) {
        JsonObject object = service.getAllPaymentListbyOutlet(request);
        return object.toString();
    }

    @PostMapping(path = "/get_payment_list_by_outlet")
    public Object paymentListbyOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return service.paymentListbyOutlet(request, req);
    }

    /* Create Payments */
    @PostMapping(path = "/create_payments")
    public Object createPayments(HttpServletRequest request) {
        JsonObject array = service.createPayments(request);
        return array.toString();
    }

    /*Update Payments*/
    @PostMapping(path = "/update_payments")
    public Object upadatePayments(HttpServletRequest request) {
        JsonObject array = service.upadatePayments(request);
        return array.toString();
    }

    @PostMapping(path = "/get_payments_by_id")
    public Object getPaymentById(HttpServletRequest request) {
        JsonObject array = service.getPaymentById(request);
        return array.toString();
    }

    /***** Delete Payment  ****/
    @PostMapping(path = "/delete_payment")
    public Object deletePayment(HttpServletRequest request) {
        JsonObject object = service.deletePayment(request);
        return object.toString();
    }

    /***** Validate duplicate Receipt Voucher  *****/
    @PostMapping(path = "/validate_payment")
    public ResponseEntity<?> validatePayment(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validatePayment(request));
    }

    /***** Validate duplicate Receipt while update invoice *****/
    @PostMapping(path = "/validate_payment_update")
    public ResponseEntity<?> validatePaymentUpdate(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validatePaymentUpdate(request));
    }

    @PostMapping(path = "/mobile/payment_list")
    public Object mobilePaymentList(@RequestBody Map<String, String> request) {
        JsonObject result = service.mobilePaymentList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/payment_invoice_list")
    public Object mobilePaymentInvoiceList(@RequestBody Map<String, String> request) {
        JsonObject result = service.mobilePaymentInvoiceList(request);
        return result.toString();
    }
}

