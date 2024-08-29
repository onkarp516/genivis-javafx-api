package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.ledger_service.LedgerMasterService;
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
public class LedgerMasterController {
    @Autowired
    private LedgerMasterService service;

    /* create ledger Masters */
    @PostMapping(path = "/create_ledger_master")
    public ResponseEntity<?> createLedgerMaster(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(service.createLedgerMaster(request));
    }

    @PostMapping(path = "/import_ledger_master")
    public ResponseEntity<?> importLedgerMaster(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(service.importLedgerMaster(request));
    }

    /***** validate duplicate Name and supplier code ****/
    @PostMapping(path = "/validate_ledger_master")
    public ResponseEntity<?> validateLedgerMaster(HttpServletRequest request) {
        return ResponseEntity.ok(service.validateLedgerMaster(request));
    }
    /***** validate duplicate Name and supplier code for update ****/
    @PostMapping(path = "/validate_ledger_master_update")
    public ResponseEntity<?> validateLedgerMasterUpdate(HttpServletRequest request) {
        return ResponseEntity.ok(service.validateLedgerMasterUpdate(request));
    }


    /* Edit Ledger Master */
    @PostMapping(path = "/edit_ledger_master")
    public ResponseEntity<?> editLedgerMaster(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(service.editLedgerMaster(request));
    }

    /* get Sundry Creditors Ledgers by outltwise */
    @GetMapping(path = "/get_sundry_creditors")
    public Object getSundryCreditors(HttpServletRequest request) {
        JsonObject result = service.getSundryCreditors(request);
        return result.toString();
    }

    //   Api for GSTR1 sundary debtors details
    @GetMapping(path = "/get_GSTR1_ledger_details")
    public Object getGSTR1LedgerDetails(HttpServletRequest request) {
        JsonObject result = service.getGSTR1LedgerDetails(request);
        return result.toString();
    }

    /* get Sundry Debtors Ledgers by outlet id */
    @GetMapping(path = "/get_sundry_debtors")
    public Object getSundryDebtors(HttpServletRequest request) {
        JsonObject result = service.getSundryDebtors(request);
        return result.toString();
    }

    /* get Sundry Debtors Ledgers by id */
    @PostMapping(path = "/get_sundry_debtors_by_id")
    public Object getSundryDebtorsById(HttpServletRequest request) {
        JsonObject result = service.getSundryDebtorsById(request);
        return result.toString();
    }

    /* get Sundry Debtors Ledgers by id */
    @PostMapping(path = "/get_sundry_creditors_by_id")
    public Object getSundryCreditorsById(HttpServletRequest request) {
        JsonObject result = service.getSundryCreditorsById(request);
        return result.toString();
    }

    /* get Purchase Account by outletid */
    @GetMapping(path = "/get_purchase_accounts")
    public Object getPurchaseAccount(HttpServletRequest request) {
        JsonObject result = service.getPurchaseAccount(request);
        return result.toString();
    }

    /* get Sales Account by outletid */
    @GetMapping(path = "/get_sales_accounts")
    public Object getSalesAccount(HttpServletRequest request) {
        JsonObject result = service.getSalesAccount(request);
        return result.toString();
    }

    /* get All Indirect incomes by principleId(here principle id: 9 is for indirect incomes) */
    @GetMapping(path = "/get_indirect_incomes")
    public Object getIndirectIncomes(HttpServletRequest request) {
        JsonObject result = service.getIndirectIncomes(request);
        return result.toString();
    }

    /* get All Indirect expenses by principleId(here principle id: 12 is for indirect expenses) */
    @GetMapping(path = "/get_indirect_expenses")
    public Object getIndirectExpenses(HttpServletRequest request) {
        JsonObject result = service.getIndirectExpenses(request);
        return result.toString();
    }

    /* get All Indirect expenses by principleId(here principle id: 12 is for indirect expenses) for Purchase and Sales */
    @GetMapping(path = "/get_indirect_expenses_list")
    public Object getIndirectExpensesList(HttpServletRequest request) {
        JsonObject result = service.getIndirectExpensesList(request);
        return result.toString();
    }

    /* get All Ledgers of outlets with Dr and Cr */
    @GetMapping(path = "/get_all_ledgers")
    public Object getAllLedgers(HttpServletRequest request) {
        JsonObject result = service.getAllLedgers(request);
        return result.toString();
    }

    @PostMapping(path = "/get_all_ledgers_pagination")
    public ResponseEntity<?> getAllLedgersPagination(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return ResponseEntity.ok(service.getAllLedgersPagination(request, req));
    }

    @PostMapping(path = "/delete_ledger")
    public Object ledgerDelete(HttpServletRequest request) {
        JsonObject object = service.ledgerDelete(request);
        return object.toString();
    }

    /* Sundry creditors overdue for bil by bill */
    @PostMapping(path = "/get_creditors_total_amount_bill_by_bill")
    public Object getTotalAmountBillbyBillSC(HttpServletRequest request) {
        JsonObject array = service.getTotalAmountBillbyBill(request);
        return array.toString();
    }

    /* get total balance of each sundry creditors for Payment Vouchers  */
    @GetMapping(path = "/get_creditors_total_amount")
    public Object getTotalAmountSC(HttpServletRequest request) {
        JsonObject array = service.getTotalAmount(request, "sc");
        return array.toString();
    }

    /* get ledgers by id */
    @PostMapping(path = "/get_ledgers_by_id")
    public Object getLedgersById(HttpServletRequest request) {
        JsonObject result = service.getLedgersById(request);
        return result.toString();
    }
    /* get total balance of each sundry creditors for Payment Vouchers  */
  /*  @GetMapping(path = "/get_debtors_total_amount")
    public Object getTotalAmountSD(HttpServletRequest request) throws JSONException {
        JSONObject array = service.getTotalAmount(request, "sd");
        return array.toString();
    }*/

    /* Get Cash-In-Hand and Bank Account Ledger from ledger balance summary   */
    @GetMapping(path = "/get_cashAc_bank_account")
    public Object getCashAcBankAccount(HttpServletRequest request) {
        JsonObject object = service.getCashAcBankAccount(request);
        return object.toString();
    }

    /* get GST Details of ledgers by id */
    @PostMapping(path = "/get_gst_details")
    public Object getGstDetails(HttpServletRequest request) {
        JsonObject result = service.getGstDetails(request);
        return result.toString();
    }

    /* get Shipping Address Details of ledgers by id */
    @PostMapping(path = "/get_shipping_details")
    public Object getShippingDetails(HttpServletRequest request) {
        JsonObject result = service.getShippingDetails(request);
        return result.toString();
    }

    /* get Department Details of ledgers by id */
    @PostMapping(path = "/get_ledger_dept_details")
    public Object getDeptDetails(HttpServletRequest request) {
        JsonObject result = service.getDeptDetails(request);
        return result.toString();
    }

    /* get Billing address details of ledgers by id */
    @PostMapping(path = "/get_ledger_billing_details")
    public Object getBillingDetails(HttpServletRequest request) {
        JsonObject result = service.getBillingDetails(request);
        return result.toString();
    } /* get Bank Details details of ledgers by id, Sundry Debtors only */
    @PostMapping(path = "/get_ledger_bank_details")
    public Object getBankDetails(HttpServletRequest request) {
        JsonObject result = service.getBankDetails(request);
        return result.toString();
    }

    /* Get Purchase and Payment Details of Sundry Creditors by id from Ledger Transaction Details Table */
   /* @PostMapping(path = "/get_tranx_details_sundry_creditor")
    public Object getTranxDetailsSundryCreditors(HttpServletRequest request) throws JSONException {
        JSONObject array = service.getTranxDetailsSundryCreditors(request);
        return array.toString();
    }*/

    /* Get Outstanding details of Sundry Creditors by id from Payment Transaction Details Table*/
    /*@PostMapping(path = "/get_payment_details_sundry_creditor")
    public Object getPaymentDetailsSundryCreditors(HttpServletRequest request) throws JSONException {
        JSONObject array = service.(request);
        return array.toString();getPaymentDetailsSundryCreditors
    }*/

    /* get sundry creditors, sundry debtors,cash account and  bank accounts*/
    @GetMapping(path = "/get_client_list_for_sale")
    public ResponseEntity<?> getClientList(HttpServletRequest request) {
        return ResponseEntity.ok(service.getClientList(request));
    }
    /* get all ledgers excepts cash account and bank accounts for payment and receipt */
    /*@GetMapping(path = "/get_ledgers_list")
    public Object getLedgersList(HttpServletRequest request) {
        JsonObject result = service.getLedgersList(request);
        return result.toString();
    }*/


    /* get Sundry Debtor of Counter Customer Ledger by outltwise */
    @GetMapping(path = "/get_counter_customer")
    public Object getCounterCustomer(HttpServletRequest request) {
        JsonObject result = service.getCounterCustomer(request);
        return result.toString();
    }

    /* get sundry debtor gst list by ledger id*/
    @PostMapping(path = "/getGSTListByLedgerId")
    public Object getGSTListByLedgerId(HttpServletRequest request) {
        return service.getGSTListByLedgerId(request).toString();
    }

    /*Check invoice date is less than drug_expiry & fssai_expiry */
    @PostMapping(path = "/checkLedgerDrugAndFssaiExpiryByLedgerId")
    public Object checkLedgerDrugAndFssaiExpiryByLedgerId(HttpServletRequest request) {
        return service.checkLedgerDrugAndFssaiExpiryByLedgerId(request).toString();
    }

    /****** Ledger Details at Transactions *****/
    @PostMapping(path = "/transaction_ledger_list")
    public Object ledgerTransactionsList(HttpServletRequest request) {
        return service.ledgerTransactionsList(request).toString();
    }

    /****** Ledger Details at Vouchers *****/
    @PostMapping(path = "/vouchers_ledger_list")
    public Object ledgerVouchersList(HttpServletRequest request) {
        return service.ledgerVouchersList(request).toString();
    }

    /****** Ledger Details by id at Transactions *****/
    @PostMapping(path = "/transaction_ledger_details")
    public Object ledgerTransactionsDetails(HttpServletRequest request) {
        return service.ledgerTransactionsDetails(request).toString();
    }

    @PostMapping(path = "/mobile/get_ledgers_report")
    public Object getMobileAllLedgers() {
        JsonObject result = service.getMobileAllLedgers();
        return result.toString();
    }

    @PostMapping(path = "/upload_document")
    public ResponseEntity<?> createOffer(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(service.uploadDocument(request));
    }

    /***** get Ledger postings List against the Trnansaction Code ******/
    @PostMapping(path = "/get_postings_by_tranxs_code")
    public Object getPostingsList(HttpServletRequest request) {
        JsonObject result = service.getPostingsList(request);
        return result.toString();
    }
    /***** get Default Banks List with associated UPI Links ******/
    @GetMapping(path = "/get_bank_payment_mode_list")
    public Object getBankPaymentModeList(HttpServletRequest request) {
        JsonObject result = service.getBankPaymentModeList(request);
        return result.toString();
    }

    @PostMapping(path = "/getGvBankLedgers")
    public Object getGvBankLedgers(HttpServletRequest request) {
        JsonObject result = service.getGvBankLedgers(request);
        return result.toString();
    }


    /***** get FR Ledgers into GV ******/
   /* @GetMapping(path = "/get_fr_ledger_list")
    public Object getFrLedgerList(HttpServletRequest request) {
        JsonObject result = service.getFrLedgerList(request);
        return result.toString();
    }*/
}
