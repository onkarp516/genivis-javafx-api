package in.truethics.ethics.ethicsapiv10.controller.tranx.sales;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.sales.TranxSalesInvoiceService;
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
public class TranxSalesInvoiceController {

    @Autowired
    private TranxSalesInvoiceService service;

    /* getting pending amount of sundry creditors againt purchase return(new reference)   */
    /*@PostMapping(path = "/get_outstanding_sales_return_amt")
    public Object getOutStandingSalesReturnAmt(HttpServletRequest request) {
        JsonObject result = service.getOutStandingSalesReturnAmt(request);
        return result.toString();
    }*/

    /* Create Sales Scree */
    @PostMapping(path = "/create_sales_invoices")
    public ResponseEntity<?> createTranxSalesInvoices(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.createTranxSalesInvoices(request));
    }


    @PostMapping(path = "/si_to_pi_invoices")
    public ResponseEntity<?> createScToPcInvoice(HttpServletRequest request) {
        return ResponseEntity.ok(service.createSiToPiInvoice(request));
    }

    /***** Create Consumer Sales Invoice *****/
    @PostMapping(path = "/create_sales_comp_invoices")
    public ResponseEntity<?> createTranxSalesCompInvoices(MultipartHttpServletRequest request) throws Exception {
        /*
        Consumer Sales Name and Code
        Name : Consumer Sales
        Code : CONS
        */
        return ResponseEntity.ok(service.createTranxSalesCompInvoices(request));
    }
    /***** Update Consumer Sales Invoice *****/
    @PostMapping(path = "/update_sales_comp_invoices")
    public ResponseEntity<?> updateTranxSalesCompInvoices(MultipartHttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.updateTranxSalesCompInvoices(request));
    }
    /***** Validate duplicate Sales Invoices  *****/
    @PostMapping(path = "/validate_sales_invoices")
    public ResponseEntity<?> validateSalesInvoices(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesInvoices(request));
    }

    /***** Validate duplicate Sales Invoices while update invoice *****/
    @PostMapping(path = "/validate_sales_invoices_update")
    public ResponseEntity<?> validateSalesInvoicesUpdate(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(service.validateSalesInvoicesUpdate(request));
    }


    /* edit functionality of sales  */
    @PostMapping(path = "/edit_sales_invoices")
    public ResponseEntity<?> salesEdit(HttpServletRequest request) {
        return ResponseEntity.ok(service.editSalesInvoice(request));
    }

    @PostMapping(path = "/edit_sales_comp_invoices")
    public ResponseEntity<?> salesEditComp(HttpServletRequest request) {
        return ResponseEntity.ok(service.editSalesCompInvoice(request));
    }

    /* get Tranx Purchase invoice by id for edit*/
    @PostMapping(path = "/get_sales_invoice_by_id")
    public Object getSalesInvoiceById(HttpServletRequest request) {
        JsonObject result = service.getSalesInvoiceById(request);
        return result.toString();
    }

    @PostMapping(path = "/get_sales_comp_invoice_by_id")
    public Object getSalesCompInvoiceByIdNew(HttpServletRequest request) {
        JsonObject result = service.getSalesCompInvoiceByIdNew(request);
        return result.toString();
    }
    /*** getProduct By Id for Edit Transactions : new Architecture (multi brand,category,flavour,package,and units)****/
    @PostMapping(path = "/get_sales_invoice_by_id_new")
    public Object getSalesInvoiceByIdNew(HttpServletRequest request) {
        JsonObject result = service.getSalesInvoiceByIdNew(request);
        return result.toString();
    }

    /* Create Counter Sale */
    @PostMapping(path = "/create_counter_sales_invoice")
    public Object createCounterSales(HttpServletRequest request) {
        return ResponseEntity.ok(service.createCounterSales(request));
    }

    /* Create Counter Sale */
    @PostMapping(path = "/update_counter_sales_invoices")
    public Object updateCounterSales(HttpServletRequest request) {
        return ResponseEntity.ok(service.updateCounterSales(request));

    }

    /* get Counter Sale List */
//    @PostMapping(path = "/get_counter_sales_invoices")
//    public Object getCounterSales(HttpServletRequest request) {
//        JsonObject result = service.getCounterSales(request);
//        return result.toString();
//    }

    //for pagination
    @PostMapping(path = "/get_counter_sales_invoices")
    public Object getCounterSales(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return  service.getCounterSales(request, req);
    }

    @GetMapping(path = "/listOfCounterSales")
    public Object listOfCounterSales(HttpServletRequest req) {
        return  service.listOfCounterSales(req);
    }

    @GetMapping(path = "/listOfConsumerSales")
    public Object listOfConsumerSales(HttpServletRequest req) {
        return  service.listOfConsumerSales(req);
    }

    @PostMapping(path = "/customerProductsHistory")
    public Object customerProductsHistory(@RequestBody Map<String, String> jsonReq, HttpServletRequest req) {
        return  service.customerProductsHistory(jsonReq, req);
    }

    /* getting last records of Sales Invoices */
    @GetMapping(path = "/get_sales_last_invoice_record")
    public Object salesLastRecord(HttpServletRequest request) {
        JsonObject result = service.salesInvoiceLastRecord(request);
        return result.toString();
    }

    /* Consumer sale last record */
    @GetMapping(path = "/get_sales_comp_last_invoice_record")
    public Object salesCompLastRecord(HttpServletRequest request) {
        JsonObject result = service.consumerSalesLastRecord(request);
        return result.toString();
    }
    /*Counter sale last record*/

    /* getting last records of Counter Sales Invoices */
    @GetMapping(path = "/get_counter_sales_last_invoice_record")
    public Object counterSaleRecord(HttpServletRequest request) {
        JsonObject result = service.CounterSaleLastRecord(request);
        return result.toString();
    }

//    AllTransactionsaleList

    @GetMapping(path = "/all_transaction_sale_list")
    public Object AllTransactionsaleList(HttpServletRequest request) {
        JsonObject result = service.AllTransactionsaleList(request);
        return result.toString();
    }

    //Get all Dispatch list
    @GetMapping(path = "/all_disp_manage_list")
    public Object AllDispatchMangList(HttpServletRequest request) {
        JsonObject result = service.AllDispatchMangList(request);
        return result.toString();
    }

//      Api for GSTR1 B2C sales outward data
    @PostMapping(path = "/GSTR1_sale_invoice_details")
    public Object GSTR1SaleInvoiceDetails( @RequestBody Map<String, String> request, HttpServletRequest req) {
        return service.GSTR1SaleInvoiceDetails(request, req);
    }


    //GSTR1 B2C large Screen2 api
    @PostMapping(path = "/GSTR1_B2CL_sale_invoice_details")
    public Object GSTR1B2CLSaleInvoiceDetails( @RequestBody Map<String, String> request, HttpServletRequest req) {
        return service.GSTR1B2CLSaleInvoiceDetails(request, req);
    }

    //GSTR1 B2C small Screen2 api
    @PostMapping(path = "/GSTR1_B2CS_sale_invoice_details")
    public Object GSTR1B2CSSaleInvoiceDetails( @RequestBody Map<String, String> request, HttpServletRequest req) {
        return service.GSTR1B2CSSaleInvoiceDetails(request, req);
    }

    /* find all Sales invoices of outletwise limit 50 */
    @PostMapping(path = "/list_sale_invoice")
    public Object saleList( @RequestBody Map<String, String> request, HttpServletRequest req) {
        return service.saleList(request, req);
    }

    /* find all Consumer Sales invoices of outletwise limit 50 */
    @PostMapping(path = "/list_sale_comp_invoice")
    public Object saleCompList( @RequestBody Map<String, String> request, HttpServletRequest req) {
        return service.saleCompList(request, req);
    }

   @GetMapping(path = "/all_list_counter_sale")
    public Object AllListCounterSale(HttpServletRequest request) {
       JsonObject result = service.AllListCounterSale(request);
       return result.toString();
   }



    /* Get Counter sales invoices while converting to Bill, returns product list of counter sale details */
    /*@PostMapping(path = "/get_counter_sales_invoices_with_ids")
    public Object getCounterSalesWithIds(HttpServletRequest request) {
        JsonObject result = service.getCounterSalesWithIds(request);
        return result.toString();
    }*/

    /* Get Counter sales invoices while editing counter sale invoice */
    @PostMapping(path = "/get_counter_sales_invoices_with_id")
    public Object getCounterSalesWithId(HttpServletRequest request) {
        JsonObject result = service.getCounterSalesWithId(request);
        return result.toString();
    }
    /* Sales Returns:  list of all selected products against sales invoice bills */
    @PostMapping(path = "/get_sales_invoice_by_id_with_pr_ids")
    public Object getSalesBillsByIdWithProductsId(HttpServletRequest request) {
        JsonObject result = service.getSalesBillsByIdWithProductsId(request);
        return result.toString();
    }

    /**** for conversion ****/
   /* @PostMapping(path = "/get_sale_invoice_with_ids")
    public Object getSaleInvoicesWithIds(HttpServletRequest request) {
        JsonObject result = service.getSaleInvoicesWithIds(request);
        return result.toString();
    }*/


    /* get invoice bill details for printing the sales invoice */
    @PostMapping(path = "/get_invoice_bill_print")
    public Object getInvoiceBillPrint(HttpServletRequest request) {
        JsonObject object = service.getInvoiceBillPrint(request);
        return object.toString();
    }


    /***** Delete sales Quotation ****/
    @PostMapping(path = "/delete_sales_invoice")
    public Object salesInvoiceDelete(HttpServletRequest request) {
        JsonObject object = service.salesInvoiceDelete(request);
        return object.toString();
    }

    /**** this end point is used while edit ****/
    @PostMapping(path = "/get_sales_invoice_product_fpu_by_id")
    public Object getProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdByFPU(request);
        return result.toString();
    }

    @PostMapping(path = "/get_sales_comp_invoice_product_fpu_by_id")
    public Object getCompProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject result = service.getCompProductEditByIdByFPU(request);
        return result.toString();
    }
    /***** for counter sales dependency prodsuct data ****/
    @PostMapping(path = "/get_cs_invoice_product_fpu_by_id")
    public Object getProductEditByIdByFPUCS(HttpServletRequest request) {
        JsonObject result = service.getProductEditByIdByFPUCS(request);
        return result.toString();
    }

    /**** used to check the closing stocks of product including all combinations  ****/
    @PostMapping(path = "/get_product_stocks_for_sale")
    public Object getProductStocksFilter(HttpServletRequest request) {
        JsonObject result = service.getProductStocksFilter(request);
        return result.toString();
    }

    @PostMapping(path = "/get_sales_invoice_supplierlist_by_productid")
    public Object getInvoiceSupplierListByProductId(HttpServletRequest request) {
        JsonObject result = service.getInvoiceSupplierListByProductId(request);
        return result.toString();
    }

    /*** getProduct By Id for Edit Counter Sales Transactions : new Architecture (multi brand,category,flavour,package,and units) ****/
    @PostMapping(path = "/get_counter_sales_by_id_new")
    public Object getCounterSalesByIdNew(HttpServletRequest request) {
        JsonObject result = service.getCounterSalesByIdNew(request);
        return result.toString();
    }

    @PostMapping(path = "/findCounterSalesById")
    public Object findCounterSalesById(HttpServletRequest request) {
        JsonObject result = service.findCounterSalesById(request);
        return result.toString();
    }
    @PostMapping(path = "/findConsumerSalesById")
    public Object findConsumerSalesById(HttpServletRequest request) {
        JsonObject result = service.findConsumerSalesById(request);
        return result.toString();
    }
    @PostMapping(path = "/findCounterSalesPrdouctsPkgUnit")
    public Object findCounterSalesPrdouctsPkgUnit(HttpServletRequest request) {
        JsonObject result = service.findCounterSalesPrdouctsPkgUnit(request);
        return result.toString();
    }
    @PostMapping(path = "/findConsumerSalesPrdouctsPkgUnit")
    public Object findConsumerSalesPrdouctsPkgUnit(HttpServletRequest request) {
        JsonObject result = service.findConsumerSalesPrdouctsPkgUnit(request);
        return result.toString();
    }

    /*** get all counter sales data with product details : new Architecture (multi brand,category,flavour,package,and units) ****/
    @PostMapping(path = "/get_counter_sales_data")
    public Object getCounterSalesDetailsData(HttpServletRequest request) {
        JsonObject result = service.getCounterSalesDetailsData(request);
        return result.toString();
    }
    /*** getProduct By Counter Sales  for conversion of Counter Sales to Sales Invoice ****/
    @PostMapping(path = "/get_counter_sales_by_no")
    public Object getCSByNo(HttpServletRequest request) {
        JsonObject result = service.getCSByNo(request);
        return result.toString();
    }


    /**** this end point is used while edit ****/
    @PostMapping(path = "/get_counter_sales_product_fpu_by_ids")
    public Object getCSProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject result = service.getCSProductEditByIdByFPU(request);
        return result.toString();
    }


    // net quantity
    @PostMapping(path = "/sales_qty_verification_by_id")
    public Object quantityVerificationById(HttpServletRequest request) {
        JsonObject netQty = service.getSalesVerificationById(request);
        return netQty.toString();

    }

    @PostMapping(path = "/delete_counter_sales")
    public Object salesCounterDelete(HttpServletRequest request) {
        JsonObject object = service.salesCounterDelete(request);
        return object.toString();
    }

    /******* Validations of Sundry Debtors whiles creating sales invoice like fssai expiry,
     *
     drug license expiry or credit days limit exceedes etc
     *******/
    @PostMapping(path = "/validate_sundry_debtors")
    public Object validateSalesInvoice(HttpServletRequest request) {
        JsonObject object = service.validateSalesInvoice(request);
        return object.toString();
    }

    @PostMapping(path = "/mobile/sd_outstandng_list")
    public Object mobileSDOutstandingList(@RequestBody Map<String, String> request) {
        Object result = service.mobileSDOutstandingList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/sale_list")
    public Object mobilesaleList(@RequestBody Map<String, String> request) {
        JsonObject result = service.mobilesaleList(request);
        return result.toString();
    }


    @PostMapping(path = "/mobile/sale_invoice_details")
    public Object saleinvoicedetails(@RequestBody Map<String, String> request) {
//        return  service.purchasemobileList(request);
        Object result = service.saleinvoicedetails(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/sd_ondate_list")
    public Object mobileSaleOnDateList(@RequestBody Map<String, String> request) {
        JsonObject result = service.mobileSaleOnDateList(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/receivable_list")
    public Object mobileReceivableList(@RequestBody Map<String, String> request) {
        Object result = service.mobileReceivableList(request);
        return result.toString();
    }
    @PostMapping(path = "/sales_status_update_by_id")
    public Object salesStatusUpdateById(HttpServletRequest request) {
      return   service.salesStatusUpdateById(request);
    }
//    @PostMapping(path = "/list_sale_invoice_delivered")
//    public Object saleDeliveredList( @RequestBody Map<String, String> request, HttpServletRequest req) {
//        return service.saleDeliveredList(request, req);
//    }
    @GetMapping(path = "/list_sale_invoice_delivered")
    public Object AllSalesDeliveryList(HttpServletRequest request) {
        JsonObject result = service.AllSalesDeliveryList(request);
        return result.toString();
    }

    @GetMapping(path = "/list_sale_invoice_cancelled")
    public Object AllSalesCancelledList(HttpServletRequest request) {
        JsonObject result = service.AllSalesCancelledList(request);
        return result.toString();
    }

    /*** getProduct By Id for Edit Counter Sales Transactions : new Architecture (multi brand,category,flavour,package,and units) ****/
    @PostMapping(path = "/get_counter_sales_products")
    public Object getCounterSalesProducts(HttpServletRequest request) {
        JsonObject result = service.getCounterSalesByIdNew( request);
        return result.toString();
    }
    /***** get Batch Details of Sales Invoice only for FR while converting Pur ord to Pur Invoice *****/
    @PostMapping(path = "/get_gv_batch_data")
    public Object getGVSalesBatchData(HttpServletRequest request) {
        JsonObject result = service.getGVSalesBatchData(request);
        return result.toString();
    }
    /*** Pur Order to Invoice in FR while Order Process replaced get_gv_batch_data to get_sales_invoice_by_track_no  ****/
    @PostMapping(path = "/get_sales_invoice_by_track_no")
    public Object getSalesByTrackNo(HttpServletRequest request) {
        JsonObject result = service.getSalesByTrackNo(request);
        return result.toString();
    }



}
