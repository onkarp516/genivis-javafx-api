package in.truethics.ethics.ethicsapiv10.controller.tranx.sales;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.sales.TranxSalesReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxSalesReturnsController {
    @Autowired
    private TranxSalesReturnService service;

    /* create Tranx Sales returns Invoice  */
    @PostMapping(path = "/create_sales_returns_invoices")
    public ResponseEntity<?> createSalesReturnsInvoices(HttpServletRequest request) {
        return ResponseEntity.ok(service.createSalesReturnsInvoices(request));
    }
    @PostMapping(path = "/edit_sales_returns_invoices")
    public ResponseEntity<?> editSalesReturnsInvoices(HttpServletRequest request) {
        return ResponseEntity.ok(service.editSalesReturnsInvoices(request));
    }

    /* get last records of Sales Returns  */
    @GetMapping(path = "/get_last_sales_returns_record")
    public Object salesReturnsLastRecord(HttpServletRequest request) {
        JsonObject result = service.salesReturnsLastRecord(request);
        return result.toString();
    }

    /* find all Sales Invoices of Sundry Debtors/Clients wise , for Sales Returns*/
    @PostMapping(path = "/list_sales_invoice_clients_wise")
    public Object salesListSupplierWise(HttpServletRequest request) {
        JsonObject result = service.salesListSupplierWise(request);
        return result.toString();
    }

//    /* get all sales returns of Outlet */
//    @PostMapping(path = "/get_all_sales_returns_by_outlet")
//    public Object AllsalesReturnsByOutlet(HttpServletRequest request) {
//        JsonObject result = service.AllsalesReturnsByOutlet(request);
//        return result.toString();
//    }
    @PostMapping(path = "/get_all_sales_returns_by_outlet")
    public Object AllsalesReturnsByOutlet(HttpServletRequest request) {
        JsonObject result = service.AllsalesReturnsByOutlet(request);
        return result.toString();
    }
    @PostMapping(path = "/get_sales_returns_by_outlet")
    public Object salesReturnsByOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return   service.salesReturnsByOutlet(request, req);
    }
    /*** getProduct By Id for Edit Transactions : new Architecture (multi brand,category,flavour,package,and units)****/
    @PostMapping(path = "/get_sales_return_by_id_new")
    public Object getSalesReturnByIdNew(HttpServletRequest request) {
        JsonObject result = service.getSalesReturnByIdNew(request);
        return result.toString();
    }


    //    /*............ sales Returns ................  */
    /* sales Returns:  find all products of selected sales invoice bill of sundry debtor */
    @PostMapping(path = "/list_sales_invoice_product_list")
    public Object productListSalesInvoice(HttpServletRequest request) {
        JsonObject result = service.productListSalesInvoice(request);
        return result.toString();
    }

    /***** Delete sales return ****/
    @PostMapping(path = "/delete_sales_return")
    public Object salesReturnDelete(HttpServletRequest request) {
        JsonObject object = service.salesReturnDelete(request);
        return object.toString();
    }
    /**** this end point is used while edit ****/
    @PostMapping(path = "/get_sales_returns_product_fpu_by_id")
    public Object getProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdByFPU(request);
        return result.toString();
    }
//    @PostMapping(path = "/get_sales_return_supplierlist_by_productid")
//    public Object getSalesReturnSupplierListByProductId(HttpServletRequest request){
//        return service.getSalesReturnSupplierListByProductId(request).toString();
//    }

    @PostMapping(path = "/mobile/sales_return_list")
    public Object mobileSDReturnList(@RequestBody Map<String, String> request) {
        Object result = service.mobileSDReturnList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/saleReturn_invoice_list")
    public Object saleReturnMobileInvoiceList(@RequestBody Map<String, String> request) {
//        return  service.purchasemobileList(request);
        Object result = service.saleReturnMobileInvoiceList(request);
        return result.toString();
    }
    /******* List of Pending Bills for Sales Return ******/
    @PostMapping(path = "/get_credit_pending_bills")
    public Object getDebtorsPendingBills(HttpServletRequest request) {
        JsonObject array = service.getDebtorsPendingBills(request);
        return array.toString();
    }

    @PostMapping(path = "/mobile/saleReturn_invoice_details_list")
    public Object purReturnMobileInvoiceDetailsList(@RequestBody Map<String, String> request) {
        Object result = service.saleReturnMobileInvoiceDetailsList(request);
        return result.toString();
    }
    @PostMapping(path = "/get_salesReturn_bill_print")
    public Object getSalesReturnBillPrint(HttpServletRequest request) {
        JsonObject object = service.getSalesReturnBillPrint(request);
        return object.toString();
    }

    /***** Validate duplicate Sales Invoices  *****/
    @PostMapping(path = "/validate_sales_return")
    public ResponseEntity<?> validateSalesInvoices(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesReturn(request));
    }

    /***** Validate duplicate Sales Invoices while update invoice *****/
    @PostMapping(path = "/validate_sales_return_update")
    public ResponseEntity<?> validateSalesInvoicesUpdate(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesInvoicesReturn(request));
    }


}
