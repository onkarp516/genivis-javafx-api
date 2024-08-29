package in.truethics.ethics.ethicsapiv10.service.tranx_service.sales;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.salesdto.SalesOrderDTO;
import in.truethics.ethics.ethicsapiv10.dto.salesdto.SalesChallanDTO;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrder;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.StockTranxDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerGstDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurChallanRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.ClosingUtility;
import in.truethics.ethics.ethicsapiv10.util.DateConvertUtil;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import in.truethics.ethics.ethicsapiv10.util.TranxCodeUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TranxSalesChallanService {

    @Autowired
    private TranxSalesChallanRepository repository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TranxSalesChallanRepository salesTransactionRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TranxSalesChallanDutiesTaxesRepository salesDutiesTaxesRepository;
    @Autowired
    private TranxSalesChallanAdditionalChargesRepository salesAdditionalChargesRepository;
    @Autowired
    private TranxSalesChallanProductSerialNumberRepository serialNumberRepository;
    @Autowired
    private TranxSalesChallanProductRepository productRepository;
    @Autowired
    private TranxSalesChallanDetailsRepository salesInvoiceDetailsRepository;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private TranxSalesQuotationRepository tranxSalesQuotationRepository;
    @Autowired
    private TranxSalesOrderRepository tranxSalesOrderRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private TranxSalesChallanDetailsUnitsRepository tranxSalesChallanDetailsUnitsRepository;
    @Autowired
    private ProductData productData;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private TranxSalesQuotaionDetailsUnitsRepository tranxSalesQuotaionDetailsUnitsRepository;
    @Autowired
    private TranxSalesOrderDetailsUnitsRepository tranxSalesOrderDetailsUnitsRepository;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private LevelARepository levelARepository;

    @Autowired
    private ClosingUtility closingUtility;

    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;

    @Autowired
    private TranxSalesChallanAdditionalChargesRepository tranxSalesChallanAdditionalChargesRepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;
    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice from DB
    List<Long> mInputList = new ArrayList<>(); // input all ledgers Id against Purchase invoice from request

    private static final Logger challanNewLogger = LogManager.getLogger(TranxSalesChallanService.class);
    @Autowired
    private RestTemplate restTemplate;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Value("${spring.serversource.frurl}")
    private String serverFrUrl;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private TranxPurOrderRepository tranxPurOrderRepository;
    @Autowired
    private TranxPurChallanRepository tranxPurChallanRepository;

    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;

    @Autowired
    private LedgerGstDetailsRepository ledgerGstDetailsRepository;

    @Autowired
    private StockTranxDetailsRepository stkTranxDetailsRepository;

    public Object createSalesChallanInvoice(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        TranxSalesChallan mSalesTranx = null;
        TransactionTypeMaster tranxType = null;

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String salesType = request.getParameter("sale_type");

        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSCHN");

        /* save into sales invoices  */
        mSalesTranx = saveIntoInvoice(users, request, tranxType, salesType);
        if (mSalesTranx != null) {
            //insertIntoLedgerTranxDetails(mSalesTranx, tranxType);
            try {
                LedgerMaster frLedger = ledgerMasterRepository.findByIdAndStatus(Long.valueOf(request.getParameter("debtors_id")), true);
                if (request.getParameterMap().containsKey("transactionTrackingNo") &&
                        request.getHeader("branch").equalsIgnoreCase("gvmh001") && frLedger.getLedgerCode().startsWith("gvmh")) {
                    //call ScToPc api
                    HttpHeaders gvHdr = new HttpHeaders();
                    gvHdr.setContentType(MediaType.MULTIPART_FORM_DATA);
                    gvHdr.add("branch", frLedger.getLedgerCode());

                    LinkedMultiValueMap gvBody = new LinkedMultiValueMap();
                    gvBody.add("usercode", frLedger.getLedgerCode());
                    gvBody.add("transactionTrackingNo", request.getParameter("transactionTrackingNo"));
                    gvBody.add("frLedgerCode", frLedger.getLedgerCode());
                    gvBody.add("order_status", OrderProcessMessage.SOTOSC);

                   /* gvBody.add("invoice_date", request.getParameter("bill_dt"));
                    gvBody.add("gstNo", request.getParameter("gstNo"));
                    gvBody.add("roundoff", request.getParameter("roundoff"));
                    gvBody.add("narration", request.getParameter("narration"));
                    gvBody.add("totalamt", request.getParameter("totalamt"));
                    gvBody.add("totalcgst", request.getParameter("totalcgst"));
                    gvBody.add("totalsgst", request.getParameter("totalsgst"));
                    gvBody.add("totaligst", request.getParameter("totaligst"));
                    gvBody.add("tcs", request.getParameter("tcs"));
                    gvBody.add("row", request.getParameter("row"));
                    gvBody.add("additionalCharges", request.getParameter("additionalCharges"));
                    gvBody.add("additionalChargesTotal", request.getParameter("additionalChargesTotal"));
                    gvBody.add("taxFlag", request.getParameter("taxFlag"));
                    gvBody.add("taxCalculation", request.getParameter("taxCalculation"));
                    gvBody.add("totalqty", request.getParameter("totalqty"));
                    gvBody.add("total_qty", request.getParameter("total_qty"));
                    gvBody.add("total_free_qty", request.getParameter("total_free_qty"));
                    gvBody.add("total_row_gross_amt", request.getParameter("total_row_gross_amt"));
                    gvBody.add("total_base_amt", request.getParameter("total_base_amt"));
                    gvBody.add("total_invoice_dis_amt", request.getParameter("total_invoice_dis_amt"));
                    gvBody.add("taxable_amount", request.getParameter("taxable_amount"));
                    gvBody.add("total_tax_amt", request.getParameter("total_tax_amt"));
                    gvBody.add("bill_amount", request.getParameter("bill_amount"));
                    gvBody.add("purchase_discount", request.getParameter("sales_discount"));
                    gvBody.add("purchase_discount_amt", request.getParameter("sales_discount_amt"));
                    gvBody.add("total_purchase_discount_amt", request.getParameter("total_sales_discount_amt"));
                    gvBody.add("reference_so_id", request.getParameter("reference_so_id"));
                    gvBody.add("reference", request.getParameter("reference"));
                    gvBody.add("rowDelDetailsIds", request.getParameter("rowDelDetailsIds"));
                    gvBody.add("acDelDetailsIds", request.getParameter("acDelDetailsIds"));
*/
                    System.out.println("gv body:" + gvBody.toString());

                    HttpEntity gvEntity = new HttpEntity<>(gvBody, gvHdr);

                    String gvData = restTemplate.exchange(
                            serverFrUrl + "/si_to_pi_invoices", HttpMethod.POST, gvEntity, String.class).getBody();
                    System.out.println("gvData Response => " + gvData);
                }

            } catch (Exception e) {
                challanNewLogger.debug("Exception while updating Sales Order to Challan ");
            }
            responseMessage.setMessage("Sales Challan created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            /**
             * @implNote validation of Ledger Delete , if any tranx done for this ledger, user cant delete this ledger **
             * @auther ashwins@opethic.com
             * @version sprint 21
             **/
            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getSundryDebtors().getId(), true);
            ledgerMaster.setIsDeleted(false);
            ledgerMasterRepository.save(ledgerMaster);
        } else {

        }
        return responseMessage;
    }

    public Object createScToPcInvoice(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();

        try {
            Users cadmin = usersRepository.findTop1ByUserRoleIgnoreCaseAndCompanyCode("cadmin", request.getParameter("frLedgerCode"));
//        Users cadmin = usersRepository.findById(3L).get();
//            System.out.println("cadmin :"+cadmin.toString());
            String cadminToken = jwtRequestFilter.getTokenFromUsername(cadmin.getUsername());
            System.out.println("cadminToken :" + cadminToken);

            Long outletId = cadmin.getOutlet().getId();
            Long branchId = cadmin.getBranch() != null ? cadmin.getBranch().getId() : null;

            if (cadmin != null) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    headers.add("branch", request.getParameter("frLedgerCode"));
                    headers.add("Authorization", "Bearer " + cadminToken);

                    Long count = 0L;
                    if (branchId != null) {
                        count = tranxPurChallanRepository.findBranchLastRecord(outletId, branchId);
                    } else {
                        count = tranxPurChallanRepository.findLastRecord(outletId);
                    }

                    String serailNo = String.format("%05d", count + 1);// 5 digit serial number
                    //first 3 digits of Current month
                    GenerateDates generateDates = new GenerateDates();
                    String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
                    String pcCode = "PC" + currentMonth + serailNo;

                    TranxPurOrder tranxPurOrder = tranxPurOrderRepository.findByTransactionTrackingNoAndStatus(
                            request.getParameter("transactionTrackingNo"), true);
                    if (tranxPurOrder != null) {
//                        LinkedMultiValueMap body = new LinkedMultiValueMap();
//
//                        JsonArray idsArr = new JsonArray();
//                        JsonObject idObj = new JsonObject();
//                        idObj.addProperty("id", tranxPurOrder.getId());
//                        idsArr.add(idObj);
//                        System.out.println(":idsArr.toString():"+idsArr.toString());
//
//                        body.add("reference_type", "PRSORD");
//                        body.add("reference", "PRSORD");
//                        body.add("reference_po_ids",idsArr.toString());
//                        body.add("transactionTrackingNo",request.getParameter("transactionTrackingNo"));
//                        body.add("invoice_date", request.getParameter("invoice_date"));
//                        body.add("newReference", false);
//                        body.add("invoice_no", pcCode);
//                        body.add("supplier_code_id", tranxPurOrder.getSundryCreditors().getId());
//                        body.add("purchase_sr_no", count + 1);
////                        body.add("transaction_date", LocalDate.now().toString());
//                        body.add("transaction_date", request.getParameter("invoice_date"));
//                        body.add("purchase_id", tranxPurOrder.getPurchaseAccountLedger().getId());
//                        body.add("roundoff", request.getParameter("roundoff"));
//                        body.add("narration", request.getParameter("narration"));
//                        body.add("totalamt", request.getParameter("totalamt"));
//                        body.add("total_purchase_discount_amt", 0);
//                        body.add("gstNo", request.getParameter("name"));
//                        body.add("totalcgst", request.getParameter("totalcgst"));
//                        body.add("totalsgst", request.getParameter("totalsgst"));
//                        body.add("totaligst", request.getParameter("totaligst"));
//                        body.add("tcs", request.getParameter("tcs"));
//                        body.add("purchase_discount",request.getParameter("purchase_discount"));
//                        body.add("purchase_discount_amt",request.getParameter("purchase_discount_amt"));
//                        body.add("total_purchase_discount_amt",request.getParameter("total_purchase_discount_amt"));
//                        body.add("row", request.getParameter("row"));
//                        body.add("additionalChargesTotal", request.getParameter("additionalChargesTotal"));
//                        body.add("total_qty", request.getParameter("total_qty"));
//                        body.add("total_free_qty", request.getParameter("total_free_qty"));
//                        body.add("total_row_gross_amt", request.getParameter("total_row_gross_amt"));
//                        body.add("total_base_amt", request.getParameter("total_base_amt"));
//                        body.add("total_invoice_dis_amt", request.getParameter("total_invoice_dis_amt"));
//                        body.add("taxable_amount", request.getParameter("taxable_amount"));
//                        body.add("total_tax_amt", request.getParameter("total_tax_amt"));
//                        body.add("bill_amount", request.getParameter("bill_amount"));
//                        body.add("taxFlag", false);
//                        body.add("taxCalculation", request.getParameter("taxCalculation"));
//                        HttpEntity entity = new HttpEntity<>(body, headers);

//                        String response = restTemplate.exchange(
//                                serverUrl + "/create_po_challan_invoices", HttpMethod.POST, entity, String.class).getBody();
//                        System.out.println("create_sales_order_invoice API Response => " + response);


//                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
//                        tranxPurOrder.setTransactionStatus(transactionStatus);
                        tranxPurOrder.setOrderStatus("Order Accepted");
                        tranxPurOrderRepository.save(tranxPurOrder);

                        responseMessage.setMessage("Sales order created successfully");
                        responseMessage.setResponseStatus(HttpStatus.OK.value());
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("EXception " + e.getMessage());
                    responseMessage.setMessage("Sales order failed to create");
                    responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                }
            }

        } catch (Exception x) {
            System.out.println("Error=>");
            x.printStackTrace();
        }

        return responseMessage;
    }

    /****** Save into sales invoices  ******/
    private TranxSalesChallan saveIntoInvoice(Users users, HttpServletRequest request, TransactionTypeMaster tranxType, String salesType) {
        TranxSalesChallan mSalesTranx = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        TranxSalesChallan invoiceTranx = new TranxSalesChallan();
        if (users.getBranch() != null) {
            branch = users.getBranch();
            invoiceTranx.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        invoiceTranx.setOutlet(outlet);
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        invoiceTranx.setTransactionStatus(transactionStatus);
//        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date date = new Date();
//        invoiceTranx.setBillDate(DateConvertUtil.convertStringToDate(request.getParameter("bill_dt")));
        invoiceTranx.setBillDate(date);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(DateConvertUtil.convertDateToLocalDate(date));
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setSalesChallanSerialNumber(Long.parseLong(request.getParameter("sales_sr_no")));
        invoiceTranx.setSalesChallanInvoiceNo(request.getParameter("bill_no"));
        invoiceTranx.setSc_bill_no(request.getParameter("bill_no"));

        LedgerMaster salesAccounts = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), true);
        LedgerMaster discountLedger = null;
        if (users.getBranch() == null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales discount", users.getOutlet().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        if (discountLedger != null) {
            invoiceTranx.setSalesDiscountLedger(discountLedger);
        }
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), true);
        invoiceTranx.setSalesAccountLedger(salesAccounts);
        invoiceTranx.setSundryDebtors(sundryDebtors);

        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setSalesRoundOff(roundoff);
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
        if (taxFlag) {
            invoiceTranx.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            invoiceTranx.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            invoiceTranx.setTotaligst(0.0);
        }
        /* if false : igst i.e inter state */
        else {
            invoiceTranx.setTotalcgst(0.0);
            invoiceTranx.setTotalsgst(0.0);
            invoiceTranx.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        invoiceTranx.setSalesDiscountAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_sales_discount_amt")));
        invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setOrderStatus("Packing");
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setOperations("inserted");
        invoiceTranx.setOrderStatus("packing");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
        invoiceTranx.setIsCounterSale(false);
        if (paramMap.containsKey("reference"))
            invoiceTranx.setReference(request.getParameter("reference"));
        /* convertions of Sales Quoations to Challan */
        if (paramMap.containsKey("reference_sq_id")) {
            String jsonRef = request.getParameter("reference_sq_id");
            JsonParser parser = new JsonParser();
            JsonElement referenceDetailsJson = parser.parse(jsonRef);
            JsonArray referenceDetails = referenceDetailsJson.getAsJsonArray();
            String id = "";
            for (int i = 0; i < referenceDetails.size(); i++) {
                JsonObject object = referenceDetails.get(i).getAsJsonObject();
                id = id + object.get("id").getAsString();
                if (i < referenceDetails.size() - 1)
                    id = id + ",";
            }
            invoiceTranx.setSq_ref_id(id);
            // setCloseSQ(request.getParameter("reference_sq_id"));
        }
        /* convertions of Sales Order to Challan */
        if (paramMap.containsKey("reference_so_id")) {
            String jsonRef = request.getParameter("reference_so_id");
            JsonParser parser = new JsonParser();
            JsonElement referenceDetailsJson = parser.parse(jsonRef);
            JsonArray referenceDetails = referenceDetailsJson.getAsJsonArray();
            String id = "";
            for (int i = 0; i < referenceDetails.size(); i++) {
                JsonObject object = referenceDetails.get(i).getAsJsonObject();
                id = id + object.get("id").getAsString();
                if (i < referenceDetails.size() - 1)
                    id = id + ",";
            }
            invoiceTranx.setSo_ref_id(id);
            //   setCloseSO(request.getParameter("reference_so_id"));
        }
        invoiceTranx.setCreatedBy(users.getId());
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                invoiceTranx.setGstNumber(request.getParameter("gstNo"));
            }
        }

        if (paramMap.containsKey("additionalChgLedger1")) {
            LedgerMaster additionalChgLedger1 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger1")), users.getOutlet().getId(), true);
            if (additionalChgLedger1 != null) {
                invoiceTranx.setAdditionLedger1(additionalChgLedger1);
                invoiceTranx.setAdditionLedgerAmt1(Double.valueOf(request.getParameter("addChgLedgerAmt1")));
            }
        }
        if (paramMap.containsKey("additionalChgLedger2")) {
            LedgerMaster additionalChgLedger2 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger2")), users.getOutlet().getId(), true);
            if (additionalChgLedger2 != null) {
                invoiceTranx.setAdditionLedger2(additionalChgLedger2);
                invoiceTranx.setAdditionLedgerAmt2(Double.valueOf(request.getParameter("addChgLedgerAmt2")));
            }
        }
        if (paramMap.containsKey("additionalChgLedger3")) {
            LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
            if (additionalChgLedger3 != null) {
                invoiceTranx.setAdditionLedger3(additionalChgLedger3);
                invoiceTranx.setAdditionLedgerAmt3(Double.valueOf(request.getParameter("addChgLedgerAmt3")));
            }
        }
        if (paramMap.containsKey("isRoundOffCheck"))
            invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));

        if (request.getParameterMap().containsKey("transactionTrackingNo"))
            invoiceTranx.setTransactionTrackingNo(request.getParameter("transactionTrackingNo"));
        else
            invoiceTranx.setTransactionTrackingNo(String.valueOf(new Date().getTime()));
        TransactionTypeMaster tranxTypeMaster = tranxRepository.findByTransactionCodeIgnoreCase("SLSCHN");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxTypeMaster.getTransactionCode());
        invoiceTranx.setTranxCode(tranxCode);
        try {
            mSalesTranx = salesTransactionRepository.save(invoiceTranx);
            /* Save into Sales Duties and Taxes */
            if (mSalesTranx != null) {
                String taxStr = request.getParameter("taxCalculation");
                if (!taxStr.isEmpty()) {
                    JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                    saveInoDutiesAndTaxes(duties_taxes, mSalesTranx, taxFlag);
                }
                JsonParser parser = new JsonParser();
                /* Save into Sales Invoice Details */
                String jsonStr = request.getParameter("row");
                JsonElement challanDetailsJson = parser.parse(jsonStr);
                JsonArray invoiceDetails = challanDetailsJson.getAsJsonArray();
                String referenceObj = request.getParameter("refObject");
                saveIntoSalesInvoiceDetails(invoiceDetails, mSalesTranx, branch, outlet, users.getId(), tranxType, salesType, referenceObj);


                /* save into Additional Charges  */
                /* save into Additional Charges  */
                String strJson = request.getParameter("additionalCharges");
                if (strJson != null) {
                    JsonElement tradeElement = new JsonParser().parse(strJson);
                    JsonArray additionalCharges = tradeElement.getAsJsonArray();
                    saveIntoSaleAdditionalCharges(additionalCharges, mSalesTranx, users.getOutlet().getId());
                }
            }

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            challanNewLogger.error("Error in saveIntoInvoice :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            challanNewLogger.error("Error in saveIntoInvoice :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mSalesTranx;
    }


    private void saveIntoSaleAdditionalCharges(JsonArray additionalCharges, TranxSalesChallan mSalesTranx, Long outletId) {

        List<TranxSalesChallanAdditionalCharges> chargesList = new ArrayList<>();
        if (mSalesTranx.getAdditionalChargesTotal() > 0) {
            for (JsonElement mList : additionalCharges) {
                TranxSalesChallanAdditionalCharges charges = new TranxSalesChallanAdditionalCharges();
                JsonObject object = mList.getAsJsonObject();
                Double amount = object.get("amt").getAsDouble();
                Long ledgerId = object.get("ledgerId").getAsLong();
                LedgerMaster addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges.setAmount(amount);
                charges.setAdditionalCharges(addcharges);
                charges.setSalesTransaction(mSalesTranx);
                charges.setStatus(true);
                charges.setPercent(object.get("percent").getAsDouble());
                charges.setCreatedBy(mSalesTranx.getCreatedBy());
                chargesList.add(charges);
            }
        }
        try {
            tranxSalesChallanAdditionalChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            //e1.printStackTrace();

            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            //e1.printStackTrace();

            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    /* End of Sales Invoice */

    /* Close All Sales Quotations which are converted into Challan */
    public void setCloseSQ(String sqIds) {
        Boolean flag = false;
        String idList[];
        idList = sqIds.split(",");
        for (String mId : idList) {
            TranxSalesQuotation tranxSalesQuotation = tranxSalesQuotationRepository.findByIdAndStatus(Long.parseLong(mId), true);
            if (tranxSalesQuotation != null) {
                tranxSalesQuotation.setStatus(false);
                tranxSalesQuotationRepository.save(tranxSalesQuotation);
            }
        }
    }

    /* Close All Sales Orders which are converted into Challan */
    public void setCloseSO(String sqIds) {
        Boolean flag = false;
        String idList[];
        idList = sqIds.split(",");
        for (String mId : idList) {
            TranxSalesOrder tranxSalesOrder = tranxSalesOrderRepository.findByIdAndStatus(Long.parseLong(mId), true);
            if (tranxSalesOrder != null) {
                tranxSalesOrder.setStatus(false);
                tranxSalesOrderRepository.save(tranxSalesOrder);
            }
        }
    }

    /****** Save into Sales Duties and Taxes ******/
    private void saveInoDutiesAndTaxes(JsonObject duties_taxes, TranxSalesChallan mSalesTranx, Boolean taxFlag) throws Exception {
        List<TranxSalesChallanDutiesTaxes> salesDutiesTaxes = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.get("cgst").getAsJsonArray();
            JsonArray sgstList = duties_taxes.get("sgst").getAsJsonArray();
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (int i = 0; i < cgstList.size(); i++) {
                    JsonObject cgstObject = cgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //      int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT CGST " + inputGst;
                    if (mSalesTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(cgstObject.get("amt").getAsString()));
                        taxes.setSalesTransaction(mSalesTranx);
                        taxes.setSundryDebtors(mSalesTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (int i = 0; i < sgstList.size(); i++) {
                    JsonObject sgstObject = sgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT SGST " + inputGst;
                    if (mSalesTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(sgstObject.get("amt").getAsString()));
                        taxes.setSalesTransaction(mSalesTranx);
                        taxes.setSundryDebtors(mSalesTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        } else {
            JsonArray igstList = duties_taxes.get("igst").getAsJsonArray();
            /* this is for Igst creation */
            if (igstList.size() > 0) {
                for (int i = 0; i < igstList.size(); i++) {
                    JsonObject igstObject = igstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String inputGst = igstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT IGST " + inputGst;
                    if (mSalesTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(igstObject.get("amt").getAsString()));
                        taxes.setSalesTransaction(mSalesTranx);
                        taxes.setSundryDebtors(mSalesTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        }
        try {
            /* save all Duties and Taxes into Sales Invoice Duties taxes table */
            salesDutiesTaxesRepository.saveAll(salesDutiesTaxes);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            challanNewLogger.error("Error in saveInoDutiesAndTaxes :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            challanNewLogger.error("Error in saveInoDutiesAndTaxes :->" + e1.getMessage());
            System.out.println(e1.getMessage());
        }
    }
    /* End of  Purchase Duties and Taxes Ledger */

    /****** Save into Sales AdditionalCharges ******/
    public void saveIntoAdditionalCharges(JsonArray additionalCharges, TranxSalesChallan mSalesTranx, String type) throws Exception {
        List<TranxSalesChallanAdditionalCharges> chargesList = new ArrayList<>();
        if (mSalesTranx.getAdditionalChargesTotal() > 0) {
            for (int j = 0; j < additionalCharges.size(); j++) {
                TranxSalesChallanAdditionalCharges charges = null;
                JsonObject object = additionalCharges.get(j).getAsJsonObject();
                Double amount = object.get("amt").getAsDouble();
                Long ledgerId = object.get("ledger").getAsLong();
                if (type.equalsIgnoreCase("create")) {
                    charges = new TranxSalesChallanAdditionalCharges();
                } else {
                    charges = salesAdditionalChargesRepository.findByAdditionalChargesIdAndSalesTransactionIdAndStatus(ledgerId, mSalesTranx.getId(), true);
                    if (charges == null) {
                        charges = new TranxSalesChallanAdditionalCharges();
                    }
                }
                LedgerMaster addcharges = ledgerMasterRepository.findByIdAndStatus(ledgerId, true);
                charges.setAmount(amount);
                charges.setAdditionalCharges(addcharges);
                charges.setSalesTransaction(mSalesTranx);
                charges.setCreatedBy(mSalesTranx.getCreatedBy());
                charges.setStatus(true);
                chargesList.add(charges);
            }
        }
        try {
            salesAdditionalChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException de) {
            //e.printStackTrace();
            challanNewLogger.error("Error in saveIntoAdditionalCharges :->" + de.getMessage());
            System.out.println(de.getMessage());
            de.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
            challanNewLogger.error("Error in saveIntoAdditionalCharges :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }
    }/* End of Sales Challan AdditionalCharges */

    /****** Save into Sales Challan Details ******/
    public void saveIntoSalesInvoiceDetails(JsonArray invoiceDetails, TranxSalesChallan mSalesTranx, Branch branch, Outlet outlet, Long userId, TransactionTypeMaster tranxType, String salesType, String referenceObj) {
        /* Sales Product Details Start here */
        String refType = "";
        Long referenceId = 0L;
        boolean flag_status = false;
        List<TranxSalesChallanProductSerialNumber> newSerialNumbers = new ArrayList<>();
        for (int i = 0; i < invoiceDetails.size(); i++) {
            JsonObject object = invoiceDetails.get(i).getAsJsonObject();
            if (object.has("reference_type")) refType = object.get("reference_type").getAsString();
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            /* inserting into TranxSalesChallanDetailsUnits */
            String batchNo = null;
            String serialNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
            double free_qty = 0.0;
            double tranxQty = 0.0;
            Long batchId = null;
            if (!object.get("levelaId").getAsString().equalsIgnoreCase("")) {
                levelA = levelARepository.findByIdAndStatus(object.get("levelaId").getAsLong(), true);
                levelAId = levelA.getId();
            }
            if (!object.get("levelbId").getAsString().equalsIgnoreCase("")) {
                levelB = levelBRepository.findByIdAndStatus(object.get("levelbId").getAsLong(), true);
                levelBId = levelB.getId();
            }
            if (!object.get("levelcId").getAsString().equalsIgnoreCase("")) {
                levelC = levelCRepository.findByIdAndStatus(object.get("levelcId").getAsLong(), true);
                levelCId = levelC.getId();
            }

            Units units = unitsRepository.findByIdAndStatus(object.get("unitId").getAsLong(), true);
            TranxSalesChallanDetailsUnits invoiceUnits = new TranxSalesChallanDetailsUnits();
            invoiceUnits.setSalesChallan(mSalesTranx);
            invoiceUnits.setProduct(mProduct);
            invoiceUnits.setUnits(units);
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            tranxQty = object.get("qty").getAsDouble();
            if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase("")) {
                free_qty = object.get("free_qty").getAsDouble();
                invoiceUnits.setFreeQty(free_qty);
            }
            invoiceUnits.setRate(object.get("rate").getAsDouble());
            invoiceUnits.setStatus(true);

            if (levelA != null) invoiceUnits.setLevelA(levelA);
            if (levelB != null) invoiceUnits.setLevelB(levelB);
            if (levelC != null) invoiceUnits.setLevelC(levelC);

            if (object.has("base_amt") && !object.get("base_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv") && !object.get("unit_conv").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            if (object.has("dis_amt") && !object.get("dis_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            if (object.has("dis_per") && !object.get("dis_per").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            if (object.has("dis_per2") && !object.get("dis_per2").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            if (object.has("row_dis_amt") && !object.get("row_dis_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
            if (object.has("gross_amt") && !object.get("gross_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
            if (object.has("add_chg_amt") && !object.get("add_chg_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setAdditionChargesAmt(object.get("add_chg_amt").getAsDouble());
            if (object.has("gross_amt1") && !object.get("gross_amt1").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setGrossAmt1(object.get("gross_amt1").getAsDouble());
            if (object.has("invoice_dis_amt") && !object.get("invoice_dis_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setInvoiceDisAmt(object.get("invoice_dis_amt").getAsDouble());
            if (object.has("dis_per_cal") && !object.get("dis_per_cal").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountPerCal(object.get("dis_per_cal").getAsDouble());
            if (object.has("dis_amt_cal") && !object.get("dis_amt_cal").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountAmountCal(object.get("dis_amt_cal").getAsDouble());
            if (object.has("total_amt") && !object.get("total_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalAmount(object.get("total_amt").getAsDouble());
            if (object.has("igst") && !object.get("igst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setIgst(object.get("igst").getAsDouble());
            if (object.has("sgst") && !object.get("sgst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setSgst(object.get("sgst").getAsDouble());
            if (object.has("cgst") && !object.get("cgst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setCgst(object.get("cgst").getAsDouble());
            if (object.has("total_igst") && !object.get("total_igst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalIgst(object.get("total_igst").getAsDouble());
            if (object.has("total_sgst") && !object.get("total_sgst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalSgst(object.get("total_sgst").getAsDouble());
            if (object.has("total_cgst") && !object.get("total_cgst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalCgst(object.get("total_cgst").getAsDouble());
            if (object.has("final_amt") && !object.get("final_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setFinalAmount(object.get("final_amt").getAsDouble());
            TransactionStatus transactionStatus = transactionStatusRepository.findById(1L).get();
            invoiceUnits.setTransactionStatus(transactionStatus);
            System.out.println("IS BATCH:" + object.get("is_batch").getAsBoolean());
            /******* Insert into Product Batch No ****/
            Boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    System.out.println("b_details_id-- " + object.get("b_details_id"));
                    flag = true;
                    productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                    if (productBatchNo != null) {
                        batchNo = productBatchNo.getBatchNo();
                        batchId = productBatchNo.getId();
                    }
                }
                invoiceUnits.setProductBatchNo(productBatchNo);
                TranxSalesChallanDetailsUnits tranxSalesInvoiceDetailsUnits = tranxSalesChallanDetailsUnitsRepository.save(invoiceUnits);
                /**
                 * @implNote validation of Product Delete , if any tranx done for this product, user cant delete this product **
                 * @auther ashwins@opethic.com
                 * @version sprint 21
                 **/
                if (mProduct != null && mProduct.getIsDelete()) {
                    mProduct.setIsDelete(false);
                    productRepository.save(mProduct);
                }
                if (flag == false) {
                    /**** Save this rate into Product Master if purchase rate has been changed by customer
                     (non batch case only) ****/
                   /* Double prrate = object.get("rate").getAsDouble();
                    ProductUnitPacking mUnitPackaging = productUnitRepository.findRate(
                            mProduct.getId(), levelAId, levelBId, levelCId, units.getId(), true);
                    if (mUnitPackaging != null) {
                        if (mUnitPackaging.getPurchaseRate() != 0 && prrate != mUnitPackaging.getPurchaseRate()) {
                            mUnitPackaging.setPurchaseRate(prrate);
                            productUnitRepository.save(mUnitPackaging);
                        }
                    }*/
                    /******* Insert into Tranx Product Serial Numbers  ******/
                    JsonArray jsonArray = object.getAsJsonArray("serialNo");
                    if (jsonArray != null && jsonArray.size() > 0) {
                        List<TranxSalesChallanProductSerialNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            TranxSalesChallanProductSerialNumber productSerialNumber = new TranxSalesChallanProductSerialNumber();
                            productSerialNumber.setProduct(mProduct);
                            productSerialNumber.setSerialNo(serialNo);
                            //productSerialNumber.setPurchaseTransaction(mPurchaseTranx);
                            productSerialNumber.setTransactionStatus("Purchase");
                            productSerialNumber.setStatus(true);
                            productSerialNumber.setCreatedBy(userId);
                            productSerialNumber.setOperations("Inserted");
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setBranch(mSalesTranx.getBranch());
                            productSerialNumber.setOutlet(mSalesTranx.getOutlet());
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setUnits(units);
                            productSerialNumber.setTranxSalesChallanDetailsUnits(tranxSalesInvoiceDetailsUnits);
                            productSerialNumber.setLevelA(levelA);
                            productSerialNumber.setLevelB(levelB);
                            productSerialNumber.setLevelC(levelC);
                            productSerialNumber.setUnits(units);
                            TranxSalesChallanProductSerialNumber mSerialNo = serialNumberRepository.save(productSerialNumber);
                            if (mProduct.getIsInventory()) {
                                inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), serialNo);
                            }
                        }
                    }
                    flag = true;
                }
            } catch (Exception e) {
            }
            try {
                /*if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                    flag = true;
                }*/
                /**** Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, null, null, null, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), null);
                    /***** End of new architecture of Inventory Postings *****/

                    /**
                     * @implNote New Logic of opening and closing Inventory posting
                     * @auther ashwins@opethic.com
                     * @version sprint 1
                     **/
                    closingUtility.stockPosting(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId,
                            mProduct, tranxType.getId(), mSalesTranx.getBillDate(), tranxQty, free_qty,
                            mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                            mSalesTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());
                    closingUtility.stockPostingBatchWise(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId,
                            mProduct, tranxType.getId(), mSalesTranx.getBillDate(), tranxQty, free_qty,
                            mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                            mSalesTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());
                    /***** End of new logic of Inventory Postings *****/
                }
            } catch (Exception e) {
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            }

            /* closing of sales quotation while converting into sales challan using its qnt */
            double qty = object.get("qty").getAsDouble();
            if (object.has("reference_id") && !object.get("reference_id").getAsString().equalsIgnoreCase(""))
                referenceId = object.get("reference_id").getAsLong();
            if (refType.equalsIgnoreCase("SLSQTN")) {
                TranxSalesQuotationDetailsUnits quotationDetails = tranxSalesQuotaionDetailsUnitsRepository.findByProductDetailsLevel(referenceId, mProduct.getId(), units.getId(), levelAId, levelBId, levelCId, true);
                if (quotationDetails != null) {
                    if (qty != quotationDetails.getQty().doubleValue()) {
                        flag_status = true;
                        double totalQty = quotationDetails.getQty().doubleValue() - qty;
                        quotationDetails.setQty(totalQty);//push data into History table before update(remainding)
                        tranxSalesQuotaionDetailsUnitsRepository.save(quotationDetails);
                    } else {
                        quotationDetails.setTransactionStatus(2L);
                        tranxSalesQuotaionDetailsUnitsRepository.save(quotationDetails);
                    }
                }
            } /* End of closing of sales quotation while converting into sales challan
            using its qnt */

            /* closing of sales order while converting into sales challan using its qnt */
            else if (refType.equalsIgnoreCase("SLSORD")) {
                TranxSalesOrderDetailsUnits orderDetailsUnits = tranxSalesOrderDetailsUnitsRepository.findByProductDetailsLevel(referenceId, mProduct.getId(), units.getId(), levelAId, levelBId, levelCId, true);
                if (orderDetailsUnits != null) {
                    if (qty != orderDetailsUnits.getQty().doubleValue()) {
                        flag_status = true;
                        double totalQty = orderDetailsUnits.getQty().doubleValue() - qty;
                        orderDetailsUnits.setQty(totalQty);//push data into History table before update(remainding)
                        tranxSalesOrderDetailsUnitsRepository.save(orderDetailsUnits);
                    } else {
                        orderDetailsUnits.setTransactionStatus(2L);
                        tranxSalesOrderDetailsUnitsRepository.save(orderDetailsUnits);
                    }
                }
            } /* End of  closing of sales order while converting into sales challan
            using its qnt */
        }
        if (refType.equalsIgnoreCase("SLSQTN")) {
            TranxSalesQuotation tranxInvoice = tranxSalesQuotationRepository.findByIdAndStatus(referenceId, true);
            if (tranxInvoice != null) {
                if (flag_status) {
                    TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
                    tranxInvoice.setTransactionStatus(transactionStatus);
                    tranxSalesQuotationRepository.save(tranxInvoice);
                } else {
                    TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                    tranxInvoice.setTransactionStatus(transactionStatus);
                    tranxSalesQuotationRepository.save(tranxInvoice);
                }
            }
        } else if (refType.equalsIgnoreCase("SLSORD")) {
            TranxSalesOrder tranxInvoice = tranxSalesOrderRepository.findByIdAndStatus(referenceId, true);
            if (tranxInvoice != null) {
                if (flag_status) {
                    TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
                    tranxInvoice.setTransactionStatus(transactionStatus);
                    tranxSalesOrderRepository.save(tranxInvoice);
                } else {
                    TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                    tranxInvoice.setTransactionStatus(transactionStatus);
                    tranxSalesOrderRepository.save(tranxInvoice);
                }
            }
        }
    }

    public JsonObject salesChallanLastRecord(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = repository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {

            count = repository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
       /* String companyName = users.getOutlet().getCompanyName();
        companyName = companyName.substring(0, 3); */ // fetching first 3 digits from company names
        /* getting Start and End year from fiscal Year */
     /*   String startYear = generateFiscalYear.getStartYear();
        String endYear = generateFiscalYear.getEndYear();*/
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
       /* String csCode = companyName.toUpperCase() + "-" + startYear + endYear
                + "-" + "SC" + currentMonth + "-" + serailNo;*/
        String csCode = "SC" + currentMonth + serailNo;

        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("serialNo", csCode);
        return result;
    }

    public JsonObject AllSaleChallanList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesChallan> tranxSalesChallans = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        String startDate = "";
        String endDate = "";
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        Boolean flag = false;
        if (paramMap.containsKey("startDate") && paramMap.containsKey("endDate")) {
            startDate = request.getParameter("startDate");
            startDatep = LocalDate.parse(startDate);
            endDate = request.getParameter("endDate");
            endDatep = LocalDate.parse(endDate);
            flag = true;
        }
        if (flag == true) {
            if (users.getBranch() != null) {
                tranxSalesChallans = salesTransactionRepository.findSaleChallanListWithDateWithBr(users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, true);
            } else {
                tranxSalesChallans = salesTransactionRepository.findSaleChallanListWithDate(users.getOutlet().getId(), startDatep, endDatep, true);
            }
        } else {
            if (users.getBranch() != null) {
                tranxSalesChallans = salesTransactionRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                tranxSalesChallans = salesTransactionRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
            }
        }

        for (TranxSalesChallan invoices : tranxSalesChallans) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("bill_no", invoices.getSalesChallanInvoiceNo());
            response.addProperty("bill_date", invoices.getBillDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("total_base_amount", invoices.getTotalBaseAmount());
            response.addProperty("sundry_debtors_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtors_id", invoices.getSundryDebtors().getId());
            response.addProperty("sales_challan_status", invoices.getTransactionStatus().getStatusName());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration());
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTaxableAmount());
            response.addProperty("invoice_id", invoices.getId());
            response.addProperty("challan_id", invoices.getId());
            response.addProperty("orderStatus", invoices.getOrderStatus());
            Double invoiceDetailsUnits = tranxSalesChallanDetailsUnitsRepository.totalinvoiceNumberOfProduct(invoices.getId(), 1, true);
            response.addProperty("product_count", invoiceDetailsUnits);
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //start of sale Challan list with pagination
    public Object saleChallanList(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        System.out.println("request " + request + "  req=" + req);
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        Boolean flag = false;
        System.out.println("startdate " + startDatep + "  endDate " + endDatep);
        List<TranxSalesChallan> saleChallan = new ArrayList<>();
        List<TranxSalesChallan> saleArrayList = new ArrayList<>();
        List<SalesChallanDTO> salesChallanDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_sales_challan_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += "  AND Date(bill_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND narration LIKE '%" + searchText + "%'";
            }
            String jsonToStr = request.get("sort");
            System.out.println(" sort " + jsonToStr);
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
                System.out.println(" ORDER BY " + jsonObject.get("colId").getAsString());
                String sortBy = jsonObject.get("colId").getAsString();
                query = query + " ORDER BY " + sortBy;
                if (jsonObject.get("isAsc").getAsBoolean() == true) {
                    query = query + " ASC";
                } else {
                    query = query + " DESC";
                }
            } else {
                query = query + " ORDER BY id DESC";
            }
            String query1 = query;       //we get all lists in this list
            System.out.println("query== " + query);
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            Query q = entityManager.createNativeQuery(query, TranxSalesChallan.class);
            System.out.println("q ==" + q + "  saleInvoice " + saleChallan);
            saleChallan = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxSalesChallan.class);
            saleArrayList = q1.getResultList();
            System.out.println("Limit total rows " + saleArrayList.size());
            Integer total_pages = (saleArrayList.size() / pageSize);
            if ((saleArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            for (TranxSalesChallan orderListView : saleChallan) {
                salesChallanDTOList.add(convertToDTDTO(orderListView));
            }
            GenericDatatable<SalesChallanDTO> data = new GenericDatatable<>(salesChallanDTOList, saleArrayList.size(), pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(salesChallanDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    //End of sale Challan list with pagination
    //Start of DTO for Challan list
    private SalesChallanDTO convertToDTDTO(TranxSalesChallan tranxSalesChallan) {
        SalesChallanDTO salesChallanDTO = new SalesChallanDTO();
        salesChallanDTO.setId(tranxSalesChallan.getId());
        salesChallanDTO.setBill_no(tranxSalesChallan.getSc_bill_no());
        salesChallanDTO.setBill_date(DateConvertUtil.convertDateToLocalDate(
                tranxSalesChallan.getBillDate()).toString());
        salesChallanDTO.setTotal_amount(tranxSalesChallan.getTotalAmount());
        salesChallanDTO.setTotal_base_amount(tranxSalesChallan.getTotalBaseAmount());
        salesChallanDTO.setSundry_debtors_name(tranxSalesChallan.getSundryDebtors().getLedgerName());
        salesChallanDTO.setSundry_debtors_id(tranxSalesChallan.getSundryDebtors().getId());
        salesChallanDTO.setSales_challan_status(tranxSalesChallan.getTransactionStatus().getStatusName());
        salesChallanDTO.setSale_account_name(tranxSalesChallan.getSalesAccountLedger().getLedgerName());
        salesChallanDTO.setNarration(tranxSalesChallan.getNarration() != null ? tranxSalesChallan.getNarration() : "");
        salesChallanDTO.setTax_amt(tranxSalesChallan.getTotalTax());
        salesChallanDTO.setTaxable_amt(tranxSalesChallan.getTaxableAmount());
        salesChallanDTO.setTransactionTrackingNo(tranxSalesChallan.getTransactionTrackingNo());
        String idList[];
        String referenceNo = "";
        salesChallanDTO.setReferenceType(tranxSalesChallan.getReference());
        if (tranxSalesChallan.getReference() != null) {
            if (tranxSalesChallan.getReference().equalsIgnoreCase("SLSQTN")) {
                idList = tranxSalesChallan.getSq_ref_id().split(",");
                for (int i = 0; i < idList.length; i++) {
                    TranxSalesQuotation tranxSalesQuotation = tranxSalesQuotationRepository.findByIdAndStatus(Long.parseLong(idList[i]), true);
                    if (tranxSalesQuotation != null) {
                        referenceNo = referenceNo + tranxSalesQuotation.getSq_bill_no();
                        if (i < idList.length - 1)
                            referenceNo = referenceNo + ",";
                    }
                }
                salesChallanDTO.setReferenceNo(referenceNo);
            } else if (tranxSalesChallan.getReference().equalsIgnoreCase("SLSORD")) {
                idList = tranxSalesChallan.getSo_ref_id().split(",");
                for (int i = 0; i < idList.length; i++) {
                    TranxSalesOrder tranxSalesOrder = tranxSalesOrderRepository.findByIdAndStatus(Long.parseLong(idList[i]), true);
                    if (tranxSalesOrder != null) {
                        referenceNo = referenceNo + tranxSalesOrder.getSo_bill_no();
                        if (i < idList.length - 1)
                            referenceNo = referenceNo + ",";
                    }
                }
                salesChallanDTO.setReferenceNo(referenceNo);
            }
        } else {
            salesChallanDTO.setReferenceNo("");
        }
        salesChallanDTO.setTranxCode(tranxSalesChallan.getTranxCode());

        return salesChallanDTO;
    }
    //End of DTO for Challan list

    public JsonObject getSaleChallanWithIds(HttpServletRequest request) {
        JsonObject output = new JsonObject();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSCHN");
        String str = request.getParameter("sales_challan_ids");
        JsonParser parser = new JsonParser();
        JsonElement purDetailsJson = parser.parse(str);
        JsonArray jsonArray = purDetailsJson.getAsJsonArray();
        JsonArray units = new JsonArray();
        JsonArray newList = new JsonArray();
        JsonObject invoiceData = new JsonObject();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            /* getting Units of Purchase Orders */
            JsonArray unitsJsonArray = new JsonArray();
            List<TranxSalesChallanDetailsUnits> unitsArray = tranxSalesChallanDetailsUnitsRepository.findBySalesChallanIdAndTransactionStatusIdAndStatus(object.get("id").getAsLong(), 1L, true);
            for (TranxSalesChallanDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ? mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ? mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ? mUnits.getLevelC().getId().toString() : "");
                unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                unitsJsonObjects.addProperty("qty", mUnits.getQty());
                unitsJsonObjects.addProperty("rate", mUnits.getRate());
                unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "");
                unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
                unitsJsonObjects.addProperty("dis_amt", mUnits.getDiscountAmount());
                unitsJsonObjects.addProperty("dis_per", mUnits.getDiscountPer());
                unitsJsonObjects.addProperty("dis_per_cal", mUnits.getDiscountPerCal());
                unitsJsonObjects.addProperty("dis_amt_cal", mUnits.getDiscountAmountCal());
                unitsJsonObjects.addProperty("total_amt", mUnits.getTotalAmount());
                unitsJsonObjects.addProperty("gst", mUnits.getIgst());
                unitsJsonObjects.addProperty("igst", mUnits.getIgst());
                unitsJsonObjects.addProperty("cgst", mUnits.getCgst());
                unitsJsonObjects.addProperty("sgst", mUnits.getSgst());
                unitsJsonObjects.addProperty("total_igst", mUnits.getTotalIgst());
                unitsJsonObjects.addProperty("total_cgst", mUnits.getTotalCgst());
                unitsJsonObjects.addProperty("total_sgst", mUnits.getTotalSgst());
                unitsJsonObjects.addProperty("final_amt", mUnits.getFinalAmount());
                unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty() != null ? mUnits.getFreeQty().toString() : "");
                unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer() != null ? mUnits.getDiscountBInPer().toString() : "");
                unitsJsonObjects.addProperty("row_dis_amt", mUnits.getTotalDiscountInAmt());
                unitsJsonObjects.addProperty("gross_amt", mUnits.getGrossAmt());
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1());
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt());
                unitsJsonObjects.addProperty("reference_id", mUnits.getSalesChallan().getId());
                unitsJsonObjects.addProperty("reference_type", tranxType.getTransactionCode());
                unitsJsonObjects.addProperty("b_detailsId", "");
                unitsJsonObjects.addProperty("is_batch", mUnits.getProduct().getIsBatchNumber() != null ? mUnits.getProduct().getIsBatchNumber() : false);
                if (mUnits.getProductBatchNo() != null) {
                    if (mUnits.getProductBatchNo().getExpiryDate() != null) {
                        if (mUnits.getSalesChallan().getBillDate().after(DateConvertUtil.convertStringToDate(String.valueOf(mUnits.getProductBatchNo().getExpiryDate())))) {
                            unitsJsonObjects.addProperty("is_expired", true);
                        } else {
                            unitsJsonObjects.addProperty("is_expired", false);
                        }
                    } else {
                        unitsJsonObjects.addProperty("is_expired", false);
                    }
                    unitsJsonObjects.addProperty("b_detailsId", mUnits.getProductBatchNo().getId());
                    unitsJsonObjects.addProperty("batch_no", mUnits.getProductBatchNo().getBatchNo());
                    unitsJsonObjects.addProperty("b_expiry", mUnits.getProductBatchNo().getExpiryDate() != null ? mUnits.getProductBatchNo().getExpiryDate().toString() : "");
                    unitsJsonObjects.addProperty("purchase_rate", mUnits.getProductBatchNo().getPurchaseRate());
                    unitsJsonObjects.addProperty("is_batch", true);
                    unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA() != null ? mUnits.getProductBatchNo().getMinRateA() : 0);
                    unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB() != null ? mUnits.getProductBatchNo().getMinRateB() : 0);
                    unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC() != null ? mUnits.getProductBatchNo().getMinRateC() : 0);
                    unitsJsonObjects.addProperty("min_discount", mUnits.getProductBatchNo().getMinDiscount());
                    unitsJsonObjects.addProperty("max_discount", mUnits.getProductBatchNo().getMaxDiscount());
                    unitsJsonObjects.addProperty("manufacturing_date", mUnits.getProductBatchNo().getManufacturingDate() != null ? mUnits.getProductBatchNo().getManufacturingDate().toString() : "");
                    unitsJsonObjects.addProperty("min_margin", mUnits.getProductBatchNo().getMinMargin());
                    unitsJsonObjects.addProperty("b_rate", mUnits.getProductBatchNo().getMrp());
                    unitsJsonObjects.addProperty("sales_rate", mUnits.getProductBatchNo().getSalesRate());
                    unitsJsonObjects.addProperty("costing", mUnits.getProductBatchNo().getCosting());
                    unitsJsonObjects.addProperty("costingWithTax", mUnits.getProductBatchNo().getCostingWithTax());
                } else {
                    unitsJsonObjects.addProperty("b_detailsId", "");
                    unitsJsonObjects.addProperty("batch_no", "");
                    unitsJsonObjects.addProperty("b_expiry", "");
                    unitsJsonObjects.addProperty("purchase_rate", "");
                    unitsJsonObjects.addProperty("is_batch", "");
                    unitsJsonObjects.addProperty("min_rate_a", "");
                    unitsJsonObjects.addProperty("min_rate_b", "");
                    unitsJsonObjects.addProperty("min_rate_c", "");
                    unitsJsonObjects.addProperty("min_discount", "");
                    unitsJsonObjects.addProperty("max_discount", "");
                    unitsJsonObjects.addProperty("manufacturing_date", "");
                    unitsJsonObjects.addProperty("mrp", "");
                    unitsJsonObjects.addProperty("min_margin", "");
                    unitsJsonObjects.addProperty("b_rate", "");
                    unitsJsonObjects.addProperty("costing", "");
                    unitsJsonObjects.addProperty("costingWithTax", "");
                }
                newList.add(unitsJsonObjects);
                invoiceData.addProperty("id", mUnits.getSalesChallan().getId());
                invoiceData.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(
                        mUnits.getSalesChallan().getBillDate()).toString());
                invoiceData.addProperty("sales_challan_no", mUnits.getSalesChallan().getSalesChallanInvoiceNo());
                invoiceData.addProperty("sales_account_id", mUnits.getSalesChallan().getSalesAccountLedger().getId());
                invoiceData.addProperty("sales_account_name", mUnits.getSalesChallan().getSalesAccountLedger().getLedgerName());
                invoiceData.addProperty("sales_sr_no", mUnits.getSalesChallan().getSalesChallanSerialNumber());
                invoiceData.addProperty("sc_sr_no", mUnits.getSalesChallan().getId());
                invoiceData.addProperty("sc_transaction_dt", mUnits.getSalesChallan().getBillDate().toString());
                invoiceData.addProperty("reference", mUnits.getSalesChallan().getReference());
                invoiceData.addProperty("debtors_id", mUnits.getSalesChallan().getSundryDebtors().getId());
                invoiceData.addProperty("debtors_name", mUnits.getSalesChallan().getSundryDebtors().getLedgerName());
                invoiceData.addProperty("ledgerStateCode", mUnits.getSalesChallan().getSundryDebtors().getStateCode());
                invoiceData.addProperty("gstNo", mUnits.getSalesChallan().getGstNumber());
                invoiceData.addProperty("transactionTrackingNo", mUnits.getSalesChallan().getTransactionTrackingNo());
                invoiceData.addProperty("narration", mUnits.getSalesChallan().getNarration() != null ? mUnits.getSalesChallan().getNarration() : "");
            }
        }
        JsonArray jsonAdditionalList = new JsonArray();
        output.addProperty("discountLedgerId", 0);
        output.addProperty("discountInAmt", 0);
        output.addProperty("discountInPer", 0);
        output.addProperty("totalSalesDiscountAmt", 0);
        output.add("additional_charges", jsonAdditionalList);
        output.add("invoice_data", invoiceData);
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("row", newList);
        return output;
    }

    /**
     * Delete sales challan
     **/
    public JsonObject salesChallanDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesChallan salesTranx = salesTransactionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TranxSalesChallan mSalesTranx;
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSCHN");
        try {
            salesTranx.setStatus(false);
            salesTranx.setOperations("deletion");
            /**** Reverse Postings of Inventory for Sales Challan ****/
            List<TranxSalesChallanDetailsUnits> unitsList = new ArrayList<>();
            unitsList = tranxSalesChallanDetailsUnitsRepository.findBySalesChallanIdAndStatus(salesTranx.getId(), true);
            for (TranxSalesChallanDetailsUnits mUnitObjects : unitsList) {
                /***** new architecture of Inventory Postings *****/
                inventoryCommonPostings.callToInventoryPostings("CR", salesTranx.getBillDate(), salesTranx.getId(), mUnitObjects.getQty(), salesTranx.getBranch(), salesTranx.getOutlet(), mUnitObjects.getProduct(), tranxType, null, null, null, mUnitObjects.getUnits(), mUnitObjects.getProductBatchNo(), mUnitObjects.getProductBatchNo() != null ? mUnitObjects.getProductBatchNo().getBatchNo() : null, salesTranx.getFiscalYear(), null);
                /***** End of new architecture of Inventory Postings *****/
            }
            mSalesTranx = salesTransactionRepository.save(salesTranx);
            if (mSalesTranx != null) {
                //            insertIntoLedgerTranxDetails(mPurchaseTranx, request);// Accounting Postings
                jsonObject.addProperty("message", "Sales Challan deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in sales challan deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            challanNewLogger.error("Error in sales challan Delete()->" + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject getSalesChallan(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesChallan tranxSalesChallan = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        List<TranxSalesChallanDetails> list = new ArrayList<>();
        List<TranxSalesChallanProductSerialNumber> serialNumbers = new ArrayList<>();
        List<TranxSalesChallanAdditionalCharges> additionalCharges = new ArrayList<>();

        try {
            Long id = Long.parseLong(request.getParameter("id"));
            list = salesInvoiceDetailsRepository.findBySalesTransactionIdAndStatus(id, true);
            //serialNumbers = serialNumberRepository.findBySalesTransactionId(tranxSalesChallan.getId());
//            additionalCharges = salesAdditionalChargesRepository.findBySalesTransactionIdAndStatus(tranxSalesChallan.getId(), true);
//            finalResult.addProperty("tcs", tranxSalesChallan.getTcs());
//            finalResult.addProperty("narration", tranxSalesChallan.getNarration() != null ? tranxSalesChallan.getNarration() : "");
//            finalResult.addProperty("discountLedgerId", tranxSalesChallan.getSalesDiscountLedger() != null ?
//                    tranxSalesChallan.getSalesDiscountLedger().getId() : 0);
//            finalResult.addProperty("discountInAmt", tranxSalesChallan.getSalesDiscountAmount());
//            finalResult.addProperty("discountInPer", tranxSalesChallan.getSalesDiscountPer());

            additionalCharges = salesAdditionalChargesRepository.findBySalesTransactionIdAndStatus(tranxSalesChallan.getId(), true);
            finalResult.addProperty("tcs", tranxSalesChallan.getTcs());
            finalResult.addProperty("narration", tranxSalesChallan.getNarration() != null ? tranxSalesChallan.getNarration() : "");
            finalResult.addProperty("discountLedgerId", tranxSalesChallan.getSalesDiscountLedger() != null ? tranxSalesChallan.getSalesDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", tranxSalesChallan.getSalesDiscountAmount());
            finalResult.addProperty("discountInPer", tranxSalesChallan.getSalesDiscountPer());
            finalResult.addProperty("totalPurchaseDiscountAmt", tranxSalesChallan.getTotalSalesDiscountAmt());

            JsonObject result = new JsonObject();
            /* Sales Quotations Data */
            result.addProperty("id", tranxSalesChallan.getId());
            result.addProperty("sales_sr_no", tranxSalesChallan.getSalesChallanSerialNumber());
            result.addProperty("sales_account_id", tranxSalesChallan.getSalesAccountLedger().getId());
            result.addProperty("sales_account", tranxSalesChallan.getSalesAccountLedger().getLedgerName());
            result.addProperty("bill_date", tranxSalesChallan.getBillDate().toString());
            result.addProperty("sq_bill_no", tranxSalesChallan.getSc_bill_no());
            result.addProperty("round_off", tranxSalesChallan.getRoundOff());
            result.addProperty("total_base_amount", tranxSalesChallan.getTotalBaseAmount());
            result.addProperty("total_amount", tranxSalesChallan.getTotalAmount());
            result.addProperty("total_cgst", tranxSalesChallan.getTotalcgst());
            result.addProperty("total_sgst", tranxSalesChallan.getTotalsgst());
            result.addProperty("total_igst", tranxSalesChallan.getTotaligst());
            result.addProperty("total_qty", tranxSalesChallan.getTotalqty());
            result.addProperty("taxable_amount", tranxSalesChallan.getTaxableAmount());
            result.addProperty("tcs", tranxSalesChallan.getTcs());
            result.addProperty("status", tranxSalesChallan.getStatus());
            result.addProperty("financial_year", tranxSalesChallan.getFinancialYear());
            result.addProperty("debtor_id", tranxSalesChallan.getSundryDebtors().getId());
            result.addProperty("debtor_name", tranxSalesChallan.getSundryDebtors().getLedgerName());
            result.addProperty("narration", tranxSalesChallan.getNarration() != null ? tranxSalesChallan.getNarration() : "");

            /* End of Sales Quotation Data */


            /* Sales Quotation Details */
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxSalesChallanDetails mDetails : list) {
                    JsonObject prDetails = new JsonObject();
                    prDetails.addProperty("product_id", mDetails.getProduct().getId());
                    prDetails.addProperty("details_id", mDetails.getId());
                   /* prDetails.addProperty("qtyH", mDetails.getQtyHigh());
                    prDetails.addProperty("qtyM", mDetails.getQtyMedium());
                    prDetails.addProperty("qtyL", mDetails.getQtyLow());
                    prDetails.addProperty("rateH", mDetails.getRateHigh());
                    prDetails.addProperty("rateM", mDetails.getRateMedium());
                    prDetails.addProperty("rateL", mDetails.getRateLow());
                    prDetails.addProperty("base_amt_H", mDetails.getBaseAmtHigh());
                    prDetails.addProperty("base_amt_M", mDetails.getBaseAmtMedium());
                    prDetails.addProperty("base_amt_L", mDetails.getBaseAmtLow());
                    prDetails.addProperty("base_amt", mDetails.getBase_amt());
                    prDetails.addProperty("dis_amt", mDetails.getDiscountAmount());
                    prDetails.addProperty("dis_amt_cal", mDetails.getDiscountAmountCal());
                    prDetails.addProperty("dis_per", mDetails.getDiscountPer());
                    prDetails.addProperty("dis_per_cal", mDetails.getDiscountPerCal());
                    prDetails.addProperty("dis_per_cal", mDetails.getDiscountPerCal());
                    prDetails.addProperty("dis_per_cal", mDetails.getDiscountPerCal());
                    if (mDetails.getPackingMaster() != null) {
                        JsonObject package_obj = new JsonObject();
                        package_obj.addProperty("id", mDetails.getPackingMaster().getId());
                        package_obj.addProperty("pack_name", mDetails.getPackingMaster().getPackName());
                        package_obj.addProperty("label", mDetails.getPackingMaster().getPackName());
                        package_obj.addProperty("value", mDetails.getPackingMaster().getId());
                        prDetails.add("package_id", package_obj);
                    } else {
                        prDetails.addProperty("package_id", "");
                    }*/
                    JsonArray serialNo = new JsonArray();
                    if (serialNumbers.size() > 0) {
                        for (TranxSalesChallanProductSerialNumber mProductSerials : serialNumbers) {
                            JsonObject jsonSerailNo = new JsonObject();
                            jsonSerailNo.addProperty("details_id", mProductSerials.getId());
                            jsonSerailNo.addProperty("product_id", mDetails.getProduct().getId());
                            jsonSerailNo.addProperty("serial_no", mProductSerials.getSerialNo());
                            serialNo.add(jsonSerailNo);
                        }
                        prDetails.add("serialNo", serialNo);
                    }
                    /* getting Units of Sales Quotations*/
                    List<TranxSalesChallanDetailsUnits> unitDetails = tranxSalesChallanDetailsUnitsRepository.findBySalesChallanDetailsIdAndStatus(mDetails.getId(), true);
                    JsonArray productDetails = new JsonArray();
                    unitDetails.forEach(mUnit -> {
                        JsonObject mObject = new JsonObject();
                        JsonObject mUnitsObj = new JsonObject();
                     /*   if (mUnit.getPackingMaster() != null) {
                            JsonObject package_obj = new JsonObject();
                            package_obj.addProperty("id", mUnit.getPackingMaster().getId());
                            package_obj.addProperty("pack_name", mUnit.getPackingMaster().getPackName());
                            package_obj.addProperty("label", mUnit.getPackingMaster().getPackName());
                            package_obj.addProperty("value", mUnit.getPackingMaster().getId());
                            mObject.add("package_id", package_obj);
                        } else {
                            mObject.addProperty("package_id", "");
                        }*/
                        if (mUnit.getPackingMaster() != null) {
                            mObject.addProperty("packageId", mUnit.getPackingMaster().getId());
                            mObject.addProperty("pack_name", mUnit.getPackingMaster().getPackName());
                        } else {
                            mObject.addProperty("packageId", "");
                            mObject.addProperty("pack_name", "");
                        }
//                        if (mUnit.getFlavourMaster() != null) {
//                            mObject.addProperty("flavourId", mUnit.getFlavourMaster().getId());
//                            mObject.addProperty("flavour_name", mUnit.getFlavourMaster().getFlavourName());
//                        } else {
//                            mObject.addProperty("flavourId", "");
//                            mObject.addProperty("flavour_name", "");
//                        }
                        if (mUnit.getProductBatchNo() != null) {
                            mObject.addProperty("b_details_id", mUnit.getProductBatchNo().getId());
                            mObject.addProperty("b_no", mUnit.getProductBatchNo().getBatchNo());
                            mObject.addProperty("is_batch", true);

                        } else {
                            mObject.addProperty("b_details_id", "");
                            mObject.addProperty("b_no", "");
                            mObject.addProperty("is_batch", false);
                        }
                        mObject.addProperty("details_id", mUnit.getId());
                        mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                        mObject.addProperty("unitId", mUnit.getUnits().getId());
                        mObject.addProperty("unit_name", mUnit.getUnits().getUnitName());
                       /* mUnitsObj.addProperty("units_id", mUnit.getUnits().getId());
                        mUnitsObj.addProperty("value", mUnit.getUnits().getId());
                        mUnitsObj.addProperty("label", mUnit.getUnits().getUnitName());
                        mUnitsObj.addProperty("unit_name", mUnit.getUnits().getUnitName());
                        mObject.add("unitId", mUnitsObj);*/
                        mObject.addProperty("qty", mUnit.getQty());
                        mObject.addProperty("rate", mUnit.getRate());
                        mObject.addProperty("base_amt", mUnit.getBaseAmt());
                        mObject.addProperty("dis_amt", mUnit.getDiscountAmount());
                        mObject.addProperty("dis_per", mUnit.getDiscountPer());
                        mObject.addProperty("dis_per_cal", mUnit.getDiscountPerCal());
                        mObject.addProperty("dis_amt_cal", mUnit.getDiscountAmountCal());
                        mObject.addProperty("total_amt", mUnit.getTotalAmount());
                        mObject.addProperty("gst", mUnit.getIgst());
                        mObject.addProperty("igst", mUnit.getIgst());
                        mObject.addProperty("cgst", mUnit.getCgst());
                        mObject.addProperty("sgst", mUnit.getSgst());
                        mObject.addProperty("total_igst", mUnit.getTotalIgst());
                        mObject.addProperty("total_cgst", mUnit.getTotalCgst());
                        mObject.addProperty("total_sgst", mUnit.getTotalSgst());
                        mObject.addProperty("final_amt", mUnit.getFinalAmount());
                        productDetails.add(mObject);
                    });
                    prDetails.add("productDetails", productDetails);
                    row.add(prDetails);
                }
            } /* End of Sales Quotations Details */
            /* Sales Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            if (additionalCharges.size() > 0) {
                for (TranxSalesChallanAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledger_id", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    jsonAdditionalList.add(json_charges);
                }
            }

            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);
            finalResult.add("additional_charges", jsonAdditionalList);


        } catch (DataIntegrityViolationException e) {
            challanNewLogger.error("Error in getSalesChallan :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            challanNewLogger.error("Error in getSalesChallan :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public Object updateSalesChallan(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesChallan mSaleTraxOrder = null;
        ResponseMessage responseMessage = new ResponseMessage();
        mSaleTraxOrder = saveIntoSCEdit(request, users);
        if (mSaleTraxOrder != null) {
            //insertIntoLedgerTranxDetails(mPurchaseTranx);
            responseMessage.setMessage("Sales order updated successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Error in purchase order creation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    private TranxSalesChallan saveIntoSCEdit(HttpServletRequest request, Users users) {
        TranxSalesChallan mSalesTranx = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        TransactionTypeMaster tranxType = null;
        Branch branch = null;
        TranxSalesChallan invoiceTranx = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (users.getBranch() != null) {
            branch = users.getBranch();
            invoiceTranx.setBranch(branch);
        }
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSCHN");
        Outlet outlet = users.getOutlet();
        invoiceTranx.setOutlet(outlet);
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        invoiceTranx.setBillDate(DateConvertUtil.convertStringToDate(request.getParameter("bill_dt")));
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setSalesChallanSerialNumber(Long.parseLong(request.getParameter("sales_sr_no")));
        invoiceTranx.setSalesChallanInvoiceNo(request.getParameter("bill_no"));
        invoiceTranx.setSc_bill_no(request.getParameter("bill_no"));

        LedgerMaster salesAccounts = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), true);
        LedgerMaster discountLedger = null;
        if (users.getBranch() == null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales discount", users.getOutlet().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        if (discountLedger != null) {
            invoiceTranx.setSalesDiscountLedger(discountLedger);
        }
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), true);
        invoiceTranx.setSalesAccountLedger(salesAccounts);
        invoiceTranx.setSundryDebtors(sundryDebtors);

        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setSalesRoundOff(roundoff);
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
        if (taxFlag) {
            invoiceTranx.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            invoiceTranx.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            invoiceTranx.setTotaligst(0.0);
        }
        /* if false : igst i.e inter state */
        else {
            invoiceTranx.setTotalcgst(0.0);
            invoiceTranx.setTotalsgst(0.0);
            invoiceTranx.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        invoiceTranx.setSalesDiscountAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_sales_discount_amt")));
        invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setOperations("inserted");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
        invoiceTranx.setIsCounterSale(false);
        /* convertions of Sales Quoations to Challan */
        if (paramMap.containsKey("reference_sq_id")) {
            invoiceTranx.setSq_ref_id(request.getParameter("reference_sq_id"));
            //     setCloseSQ(request.getParameter("reference_sq_id"));
        }
        /* convertions of Sales Order to Challan */
        if (paramMap.containsKey("reference_so_id")) {
            invoiceTranx.setSq_ref_id(request.getParameter("reference_so_id"));
            //   setCloseSO(request.getParameter("reference_so_id"));
        }
        invoiceTranx.setCreatedBy(users.getId());
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                invoiceTranx.setGstNumber(request.getParameter("gstNo"));
            }
        }

        /*if (paramMap.containsKey("additionalChgLedger1")) {
            LedgerMaster additionalChgLedger1 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger1")), users.getOutlet().getId(), true);
            if (additionalChgLedger1 != null) {
                invoiceTranx.setAdditionLedger1(additionalChgLedger1);
                invoiceTranx.setAdditionLedgerAmt1(Double.valueOf(request.getParameter("addChgLedgerAmt1")));
            }
        }
        if (paramMap.containsKey("additionalChgLedger2")) {
            LedgerMaster additionalChgLedger2 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger2")), users.getOutlet().getId(), true);
            if (additionalChgLedger2 != null) {
                invoiceTranx.setAdditionLedger2(additionalChgLedger2);
                invoiceTranx.setAdditionLedgerAmt2(Double.valueOf(request.getParameter("addChgLedgerAmt2")));
            }
        }*/
        if (paramMap.containsKey("additionalChgLedger3")) {
            LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
            if (additionalChgLedger3 != null) {
                invoiceTranx.setAdditionLedger3(additionalChgLedger3);
                invoiceTranx.setAdditionLedgerAmt3(Double.valueOf(request.getParameter("addChgLedgerAmt3")));
            }
        }
        if (paramMap.containsKey("isRoundOffCheck"))
            invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));

        try {
            mSalesTranx = salesTransactionRepository.save(invoiceTranx);
            /* Save into Sales Duties and Taxes */
            if (mSalesTranx != null) {
                String taxStr = request.getParameter("taxCalculation");
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveInoDutiesAndTaxesEdit(duties_taxes, mSalesTranx, taxFlag, tranxType, users.getOutlet().getId(), users.getId());
                JsonParser parser = new JsonParser();
                /* Save into Sales Invoice Details */
                String jsonStr = request.getParameter("row");
                JsonElement challanDetailsJson = parser.parse(jsonStr);
                JsonArray invoiceDetails = challanDetailsJson.getAsJsonArray();
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) rowsDeleted = request.getParameter("rowDelDetailsIds");

                /*** delete additional charges if removed from frontend ****/
                String acRowsDeleted = "";
                if (paramMap.containsKey("acDelDetailsIds"))
                    acRowsDeleted = request.getParameter("acDelDetailsIds");
                JsonElement purAChargesJson;
                if (!acRowsDeleted.equalsIgnoreCase("")) {
                    purAChargesJson = new JsonParser().parse(acRowsDeleted);
                    JsonArray deletedArrays = purAChargesJson.getAsJsonArray();
                    if (deletedArrays.size() > 0) {
                        delAddCharges(deletedArrays);
                    }
                }

                if (paramMap.containsKey("additionalCharges")) {
                    String strJson = request.getParameter("additionalCharges");
                    JsonElement purAddChargesJson = parser.parse(strJson);
                    JsonArray additionalCharges = purAddChargesJson.getAsJsonArray();
                    if (additionalCharges.size() > 0) {
                        saveIntoSalesChallanAdditionalChargesEdit(additionalCharges, mSalesTranx, tranxType, users.getOutlet().getId(), acRowsDeleted);
                    }
                }

                saveIntoSalesInvoiceDetailsEdit(invoiceDetails, mSalesTranx, branch, outlet, users.getId(), tranxType, rowsDeleted);
            }

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            challanNewLogger.error("Error in saveIntoSCEdit :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            challanNewLogger.error("Error in saveIntoSCEdit :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mSalesTranx;
    }


    private void saveIntoSalesChallanAdditionalChargesEdit(JsonArray additionalCharges, TranxSalesChallan mSalesTranx, TransactionTypeMaster tranxType, Long outletId, String acRowsDeleted) {


        List<TranxSalesChallanAdditionalCharges> chargesList = new ArrayList<>();
        for (JsonElement mAddCharges : additionalCharges) {
            JsonObject object = mAddCharges.getAsJsonObject();
            Double amount = 0.0;
            Double percent = 0.0;
            Long detailsId = 0L;
            if (object.has("amt") && !object.get("amt").getAsString().equalsIgnoreCase("")) {
                amount = object.get("amt").getAsDouble();

                if (object.has("percent")) {
                    // Retrieve the value associated with the key "percent" and convert it to a double
                    percent = object.get("percent").getAsDouble();
                } else {
                    // If the value is null or the key doesn't exist, assign 0.0 to the variable percent
                    percent = 0.0;
                }

                //percent = object.get("percent").getAsDouble();
                Long ledgerId = object.get("ledgerId").getAsLong();
                if (object.has("additional_charges_details_id"))
                    detailsId = object.get("additional_charges_details_id").getAsLong();
                LedgerMaster addcharges = null;
                TranxSalesChallanAdditionalCharges charges = null;
                charges = tranxSalesChallanAdditionalChargesRepository.findByAdditionalChargesIdAndSalesTransactionIdAndStatus(ledgerId, mSalesTranx.getId(), true);
                if (detailsId == 0L) {
                    charges = new TranxSalesChallanAdditionalCharges();
                }
                addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges.setAmount(amount);
                charges.setPercent(percent);
                charges.setAdditionalCharges(addcharges);
                charges.setSalesTransaction(mSalesTranx);

                charges.setStatus(true);

                chargesList.add(charges);
                tranxSalesChallanAdditionalChargesRepository.save(charges);
                Boolean isContains = dbList.contains(addcharges.getId());
                mInputList.add(addcharges.getId());
                if (isContains) {
                    //      transactionDetailsRepository.ledgerPostingEdit(addcharges.getId(), mPurchaseTranx.getId(), tranxTypeMater.getId(), "DR", amount * -1);
                    /**** New Postings Logic *****/
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(addcharges.getId(), tranxType.getId(), mSalesTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(amount);
                        mLedger.setTransactionDate(mSalesTranx.getBillDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    /* insert */
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(amount, addcharges, tranxType, addcharges.getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesChallanInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");
                }
            }
        }

    }


    private void saveIntoSalesInvoiceDetailsEdit(JsonArray array, TranxSalesChallan mSalesTranx, Branch branch, Outlet outlet, Long userId, TransactionTypeMaster tranxType, String rowsDeleted) {
        /* Sales Product Details Start here */
        List<TranxSalesChallanDetails> row = new ArrayList<>();

        List<TranxSalesChallanProductSerialNumber> newSerialNumbers = new ArrayList<>();
        for (JsonElement mList : array) {
            JsonObject object = mList.getAsJsonObject();
            /* Purchase Invoice Unit Edit */
            Long details_id = object.get("details_id").getAsLong();
            TranxSalesChallanDetailsUnits invoiceUnits = new TranxSalesChallanDetailsUnits();
            if (details_id != 0) {
                invoiceUnits = tranxSalesChallanDetailsUnitsRepository.findByIdAndStatus(details_id, true);
            } else {
                TransactionStatus transactionStatus = transactionStatusRepository.findById(1L).get();
                invoiceUnits.setTransactionStatus(transactionStatus);
            }
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            String batchNo = null;
            String serialNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            double free_qty = 0.0;
            Double tranxQty = 0.0;
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
            Long batchId = null;
            tranxQty = object.get("qty").getAsDouble();
            /* Sales Invoice Unit Edit */
            if (!object.get("levelaId").getAsString().equalsIgnoreCase("")) {
                levelA = levelARepository.findByIdAndStatus(object.get("levelaId").getAsLong(), true);
            }
            if (!object.get("levelbId").getAsString().equalsIgnoreCase("")) {
                levelB = levelBRepository.findByIdAndStatus(object.get("levelbId").getAsLong(), true);
            }
            if (!object.get("levelcId").getAsString().equalsIgnoreCase("")) {
                levelC = levelCRepository.findByIdAndStatus(object.get("levelcId").getAsLong(), true);
            }
            Units units = unitsRepository.findByIdAndStatus(object.get("unitId").getAsLong(), true);
            invoiceUnits.setSalesChallan(mSalesTranx);
            invoiceUnits.setProduct(mProduct);
            invoiceUnits.setUnits(units);
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase("")) {
                free_qty = object.get("free_qty").getAsDouble();
                invoiceUnits.setFreeQty(free_qty);
            }
            invoiceUnits.setRate(object.get("rate").getAsDouble());
            if (levelA != null) invoiceUnits.setLevelA(levelA);
            if (levelB != null) invoiceUnits.setLevelB(levelB);
            if (levelC != null) invoiceUnits.setLevelC(levelC);
            invoiceUnits.setStatus(true);
            if (object.has("base_amt") && !object.get("base_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv") && !object.get("unit_conv").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            if (object.has("dis_amt") && !object.get("dis_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            if (object.has("dis_per") && !object.get("dis_per").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            if (object.has("dis_per2") && !object.get("dis_per2").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            if (object.has("row_dis_amt") && !object.get("row_dis_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
            if (object.has("gross_amt") && !object.get("gross_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
            if (object.has("add_chg_amt") && !object.get("add_chg_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setAdditionChargesAmt(object.get("add_chg_amt").getAsDouble());
            if (object.has("gross_amt1") && !object.get("gross_amt1").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setGrossAmt1(object.get("gross_amt1").getAsDouble());
            if (object.has("invoice_dis_amt") && !object.get("invoice_dis_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setInvoiceDisAmt(object.get("invoice_dis_amt").getAsDouble());
            if (object.has("dis_per_cal") && !object.get("dis_per_cal").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountPerCal(object.get("dis_per_cal").getAsDouble());
            if (object.has("dis_amt_cal") && !object.get("dis_amt_cal").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountAmountCal(object.get("dis_amt_cal").getAsDouble());
            if (object.has("total_amt") && !object.get("total_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalAmount(object.get("total_amt").getAsDouble());
            if (object.has("igst") && !object.get("igst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setIgst(object.get("igst").getAsDouble());
            if (object.has("sgst") && !object.get("sgst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setSgst(object.get("sgst").getAsDouble());
            if (object.has("cgst") && !object.get("cgst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setCgst(object.get("cgst").getAsDouble());
            if (object.has("total_igst") && !object.get("total_igst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalIgst(object.get("total_igst").getAsDouble());
            if (object.has("total_sgst") && !object.get("total_sgst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalSgst(object.get("total_sgst").getAsDouble());
            if (object.has("total_cgst") && !object.get("total_cgst").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setTotalCgst(object.get("total_cgst").getAsDouble());
            if (object.has("final_amt") && !object.get("final_amt").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setFinalAmount(object.get("final_amt").getAsDouble());
            boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                   /* productBatchNo.setQnty(object.get("qty").getAsInt());
                    productBatchNo.setSalesRate(object.get("rate").getAsDouble());
                    productBatchNoRepository.save(productBatchNo);*/
                    batchNo = productBatchNo.getBatchNo();
                    batchId = productBatchNo.getId();
                }
                invoiceUnits.setProductBatchNo(productBatchNo);
                TranxSalesChallanDetailsUnits tranxSalesInvoiceDetailsUnits = tranxSalesChallanDetailsUnitsRepository.save(invoiceUnits);
                if (flag == false) {
                    JsonArray jsonArray = object.getAsJsonArray("serialNo");
                    if (jsonArray != null && jsonArray.size() > 0) {
                        List<TranxSalesChallanProductSerialNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            Long detailsId = jsonSrno.get("serial_detail_id").getAsLong();
                            if (detailsId == 0) {
                                TranxSalesChallanProductSerialNumber productSerialNumber = new TranxSalesChallanProductSerialNumber();
                                productSerialNumber.setProduct(mProduct);
                                productSerialNumber.setSerialNo(serialNo);
                                // productSerialNumber.setPurchaseTransaction(mPurchaseTranx);
                                productSerialNumber.setTransactionStatus("Purchase");
                                productSerialNumber.setStatus(true);
                                productSerialNumber.setCreatedBy(userId);
                                productSerialNumber.setOperations("Inserted");
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setBranch(mSalesTranx.getBranch());
                                productSerialNumber.setOutlet(mSalesTranx.getOutlet());
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setUnits(units);
                                productSerialNumber.setTranxSalesChallanDetailsUnits(tranxSalesInvoiceDetailsUnits);
                                productSerialNumber.setLevelA(levelA);
                                productSerialNumber.setLevelB(levelB);
                                productSerialNumber.setLevelC(levelC);
                                productSerialNumber.setUnits(units);
                                TranxSalesChallanProductSerialNumber mSerialNo = serialNumberRepository.save(productSerialNumber);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), serialNo);
                                }
                            } else {
                                TranxSalesChallanProductSerialNumber productSerialNumber1 = serialNumberRepository.findByIdAndStatus(detailsId, true);
                                productSerialNumber1.setSerialNo(serialNo);
                                productSerialNumber1.setUpdatedBy(userId);
                                productSerialNumber1.setOperations("Updated");
                                TranxSalesChallanProductSerialNumber mSerialNo = serialNumberRepository.save(productSerialNumber1);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToEditInventoryPostings(mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear());
                                }
                            }
                        }
                    }

                    flag = true;
                }
            } catch (Exception e) {
                challanNewLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            try {
                /*if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                    flag = true;
                }*/
                /**** Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    if (details_id != 0) {
                        inventoryCommonPostings.callToEditInventoryPostings(mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear());

                        /**
                         * @implNote New Logic of Inventory Posting
                         * @auther ashwins@opethic.com
                         * @version sprint 2
                         * Case 1: Modify QTY
                         **/
                        InventorySummaryTransactionDetails productRow = stkTranxDetailsRepository.
                                findByProductIdAndTranxTypeIdAndTranxId(
                                        mProduct.getId(), tranxType.getId(), mSalesTranx.getId());
                        if (productRow != null) {
                            if (mSalesTranx.getBillDate().compareTo(productRow.getTranxDate()) == 0 &&
                                    tranxQty != invoiceUnits.getQty()) { //DATE SAME AND QTY DIFFERENT
                                Double closingStk = closingUtility.CAL_CR_STOCK(productRow.getOpeningStock(), tranxQty, free_qty);
                                productRow.setQty(tranxQty + free_qty);
                                productRow.setClosingStock(closingStk);
                                InventorySummaryTransactionDetails mInventory =
                                        stkTranxDetailsRepository.save(productRow);
                                closingUtility.updatePosting(mInventory, mProduct.getId(), mSalesTranx.getBillDate());
                            } else if (mSalesTranx.getBillDate().compareTo(productRow.getTranxDate()) != 0) { // DATE IS DIFFERENT
                                Date oldDate = productRow.getTranxDate();
                                Date newDate = mSalesTranx.getBillDate();
                                if (newDate.after(oldDate)) { //FORWARD INSERT
                                    productRow.setStatus(false);
                                    stkTranxDetailsRepository.save(productRow);
                                    Double opening = productRow.getOpeningStock();
                                    Double closing = 0.0;
                                    List<InventorySummaryTransactionDetails> openingClosingList =
                                            stkTranxDetailsRepository.getBetweenDateProductId(
                                                    mProduct.getId(), oldDate, newDate, productRow.getId(), true);
                                    for (InventorySummaryTransactionDetails closingDetail : openingClosingList) {
                                        closingDetail.setOpeningStock(opening);
                                        if (closingDetail.getTranxAction().equalsIgnoreCase("IN"))
                                            closing = closingUtility.CAL_CR_STOCK(opening, closingDetail.getQty(), 0.0);
                                        else if (closingDetail.getTranxAction().equalsIgnoreCase("OUT"))
                                            closing = closingUtility.CAL_DR_STOCK(opening, 0.0, closingDetail.getQty());
                                        closingDetail.setClosingStock(closing);
                                        stkTranxDetailsRepository.save(closingDetail);
                                        opening = closing;
                                    }

                                } else if (newDate.before(oldDate)) { // BACKWARD INSERT
                                    // old date record update
                                    productRow.setStatus(false);
                                    InventorySummaryTransactionDetails detail = stkTranxDetailsRepository.save(productRow);
                                }
                                /***** NEW METHOD FOR LEDGER POSTING *****/
                                closingUtility.stockPosting(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId,
                                        mProduct, tranxType.getId(), newDate, invoiceUnits.getQty(), free_qty,
                                        mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                        mSalesTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());
                                closingUtility.stockPostingBatchWise(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId,
                                        mProduct, tranxType.getId(), newDate, invoiceUnits.getQty(), free_qty,
                                        mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                        mSalesTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());
                            }
                        }
                    } else {
                        inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), serialNo);
                    }
                    /***** End of new architecture of Inventory Postings *****/
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            }
        }
        /* if product is deleted from details table from front end, when user edit the purchase */
        if (!rowsDeleted.isEmpty()) {
            JsonParser parser = new JsonParser();
            JsonElement purDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = purDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxSalesChallanDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    mDeletedInvoices = tranxSalesChallanDetailsUnitsRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                    if (mDeletedInvoices != null) {
                        mDeletedInvoices.setStatus(false);
                        try {
                            tranxSalesChallanDetailsUnitsRepository.save(mDeletedInvoices);
                            /***** inventory effects of deleted rows *****/
                            inventoryCommonPostings.callToInventoryPostings("CR", mDeletedInvoices.getSalesChallan().getBillDate(), mDeletedInvoices.getSalesChallan().getId(), mDeletedInvoices.getQty() + mDeletedInvoices.getFreeQty(), branch, outlet, mDeletedInvoices.getProduct(), tranxType, mDeletedInvoices.getLevelA(), mDeletedInvoices.getLevelB(), mDeletedInvoices.getLevelC(), mDeletedInvoices.getUnits(), mDeletedInvoices.getProductBatchNo(), mDeletedInvoices.getProductBatchNo().getBatchNo(), mDeletedInvoices.getSalesChallan().getFiscalYear(), null);
                            /***** End of new architecture of Inventory Postings *****/
                        } catch (DataIntegrityViolationException de) {
                            de.printStackTrace();
                            challanNewLogger.error("Error in saveIntoSalesInvoiceDetailsEdit :->" + de.getMessage());
                            System.out.println("Exception:" + de.getMessage());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            challanNewLogger.error("Error in saveIntoSalesInvoiceDetailsEdit :->" + ex.getMessage());
                            System.out.println("Exception:" + ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    /* for Sales Challan Edit */
    private void insertIntoDutiesAndTaxesHistory(List<TranxSalesChallanDutiesTaxes> salesDutiesTaxes) {
        for (TranxSalesChallanDutiesTaxes mList : salesDutiesTaxes) {
            mList.setStatus(false);
            salesDutiesTaxesRepository.save(mList);
        }
    }

    public void delAddCharges(JsonArray deletedArrays) {

        TranxSalesChallanAdditionalCharges mDeletedInvoices = null;
        for (JsonElement element : deletedArrays) {
            JsonObject deletedRowsId = element.getAsJsonObject();
            mDeletedInvoices = salesAdditionalChargesRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
            if (mDeletedInvoices != null) {
                mDeletedInvoices.setStatus(false);
                try {
                    salesAdditionalChargesRepository.save(mDeletedInvoices);
                } catch (DataIntegrityViolationException de) {
                    challanNewLogger.error("Error into Sales Challan Add.Charges Edit" + de.getMessage());
                    de.printStackTrace();
                    System.out.println("Exception:" + de.getMessage());

                } catch (Exception ex) {
                    challanNewLogger.error("Error into Sales Challan Add.Charges Edit" + ex.getMessage());
                    ex.printStackTrace();
                    System.out.println("Exception into Sales Challan Add.Charges Edit:" + ex.getMessage());
                }
            }
        }
    }

    /* private List<Long> getInputLedgerIds(Boolean taxFlag, JsonObject duties_taxes, Long outletId) {
         List<Long> returnLedgerIds = new ArrayList<>();
         if (taxFlag) {
             JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
             JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
             *//* this is for Cgst creation *//*
            if (cgstList.size() > 0) {
                for (JsonElement mCgst : cgstList) {
                    TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                    JsonObject cgstObject = mCgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String ledgerName = "INPUT CGST " + inputGst;
                    dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                    if (dutiesTaxes != null) {
                        //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                        returnLedgerIds.add(dutiesTaxes.getId());
                    }
                }
            }
            *//* this is for Sgst creation *//*
            if (sgstList.size() > 0) {
                for (JsonElement mSgst : sgstList) {
                    TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                    JsonObject sgstObject = mSgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String ledgerName = "INPUT SGST " + inputGst;
                    dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                    if (dutiesTaxes != null) {
                        returnLedgerIds.add(dutiesTaxes.getId());
                    }
                }
            }
        } else {
            JsonArray igstList = duties_taxes.getAsJsonArray("igst");
            *//* this is for Igst creation *//*
            if (igstList.size() > 0) {
                for (JsonElement mIgst : igstList) {
                    JsonObject igstObject = mIgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String ledgerName = "INPUT IGST " + inputGst;
                    dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                    if (dutiesTaxes != null) {
                        returnLedgerIds.add(dutiesTaxes.getId());
                    }
                }
            }
        }
        return returnLedgerIds;
    }
*/
    private List<Long> getInputLedgerIds(Boolean taxFlag, JsonObject duties_taxes, Long outletId, Long branchId) {
        List<Long> returnLedgerIds = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (JsonElement mCgst : cgstList) {
                    /*TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();*/
                    JsonObject cgstObject = mCgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //   int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT CGST " + inputGst;
                    //      dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                    if (branchId != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(outletId, branchId, ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(outletId, ledgerName);
                    if (dutiesTaxes != null) {
                        //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                        returnLedgerIds.add(dutiesTaxes.getId());
                    }
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (JsonElement mSgst : sgstList) {
                    /*TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();*/
                    JsonObject sgstObject = mSgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT SGST " + inputGst;
                    //   dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                    if (branchId != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(outletId, branchId, ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(outletId, ledgerName);
                    if (dutiesTaxes != null) {
                        returnLedgerIds.add(dutiesTaxes.getId());
                    }
                }
            }
        } else {
            JsonArray igstList = duties_taxes.getAsJsonArray("igst");
            /* this is for Igst creation */
            if (igstList.size() > 0) {
                for (JsonElement mIgst : igstList) {
                    JsonObject igstObject = mIgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //     int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String inputGst = igstObject.get("gst").getAsString();
                    String ledgerName = "INPUT IGST " + inputGst;
                    //  dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                    if (branchId != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(outletId, branchId, ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(outletId, ledgerName);
                    if (dutiesTaxes != null) {
                        returnLedgerIds.add(dutiesTaxes.getId());
                    }
                }
            }
        }
        return returnLedgerIds;
    }


    public void saveInoDutiesAndTaxesEdit(JsonObject duties_taxes, TranxSalesChallan invoiceTranx, Boolean taxFlag, TransactionTypeMaster tranxType, Long outletId, Long userId) {
        /*sales Duties and Taxes*/
        List<TranxSalesChallanDutiesTaxes> salesDutiesTaxes = new ArrayList<>();
        List<Long> db_dutiesLedgerIds = salesDutiesTaxesRepository.findByDutiesAndTaxesId(invoiceTranx.getId());
        // List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId);
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null);

        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            //Updation into Purchase Duties and Taxes
            if (db_dutiesLedgerIds.size() > 0) {
                //insert old records in history
                salesDutiesTaxes = salesDutiesTaxesRepository.findBySalesTransactionAndStatus(invoiceTranx, true);
                // insertIntoDutiesAndTaxesHistory(salesDutiesTaxes);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation*/
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;
                        // dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        Double amt = cgstObject.get("amt").getAsDouble();
                        if (dutiesTaxes != null) {
                            //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            /*if (isContains) {
                                transactionDetailsRepository.ledgerPostingEdit(dutiesTaxes.getId(), invoiceTranx.getId(), tranxType.getId(), "DR", amt * -1);
                            } else {
                                //  insert
                                transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(dutiesTaxes.getPrinciples().getFoundations().getId(), dutiesTaxes.getPrinciples().getId(), dutiesTaxes.getPrincipleGroups() != null ? dutiesTaxes.getPrincipleGroups().getId() : null, invoiceTranx.getAssociateGroups() != null ? invoiceTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null, invoiceTranx.getOutlet().getId(), "pending", amt * -1, 0.0, invoiceTranx.getBillDate(), null, invoiceTranx.getId(), tranxType.getTransactionName(), dutiesTaxes.getUnderPrefix(), invoiceTranx.getFinancialYear(), invoiceTranx.getCreatedBy(), dutiesTaxes.getId(), invoiceTranx.getSalesInvoiceNo());
                            }*/
                        }
                        taxes.setAmount(amt);
                        taxes.setStatus(true);
                        taxes.setSalesTransaction(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation*/
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        //      dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            /*if (isContains) {
                                transactionDetailsRepository.ledgerPostingEdit(dutiesTaxes.getId(), invoiceTranx.getId(), tranxType.getId(), "DR", amt * -1);
                            } else {
                                //  insert
                                transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(dutiesTaxes.getPrinciples().getFoundations().getId(), dutiesTaxes.getPrinciples().getId(), dutiesTaxes.getPrincipleGroups() != null ? dutiesTaxes.getPrincipleGroups().getId() : null, invoiceTranx.getAssociateGroups() != null ? invoiceTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null, invoiceTranx.getOutlet().getId(), "pending", amt * -1, 0.0, invoiceTranx.getBillDate(), null, invoiceTranx.getId(), tranxType.getTransactionName(), dutiesTaxes.getUnderPrefix(), invoiceTranx.getFinancialYear(), invoiceTranx.getCreatedBy(), dutiesTaxes.getId(), invoiceTranx.getSalesInvoiceNo());
                            }*/
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransaction(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");

                /* this is for Igst creation*/
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                        JsonObject igstObject = mIgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        //  dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                          /*  if (isContains) {
                                transactionDetailsRepository.ledgerPostingEdit(dutiesTaxes.getId(), invoiceTranx.getId(), tranxType.getId(), "DR", amt * -1);
                            } else {
                                //insert
                                transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(dutiesTaxes.getPrinciples().getFoundations().getId(), dutiesTaxes.getPrinciples().getId(), dutiesTaxes.getPrincipleGroups() != null ? dutiesTaxes.getPrincipleGroups().getId() : null, invoiceTranx.getAssociateGroups() != null ? invoiceTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null, invoiceTranx.getOutlet().getId(), "pending", amt * -1, 0.0, invoiceTranx.getBillDate(), null, invoiceTranx.getId(), tranxType.getTransactionName(), dutiesTaxes.getUnderPrefix(), invoiceTranx.getFinancialYear(), invoiceTranx.getCreatedBy(), dutiesTaxes.getId(), invoiceTranx.getSalesInvoiceNo());
                            }*/
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransaction(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        } else {
            //Insertion into Purchase Duties and Taxes
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation*/
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;
                        Double amt = cgstObject.get("amt").getAsDouble();
                        //  dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransaction(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation*/
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //   int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        //   dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransaction(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation*/
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxSalesChallanDutiesTaxes taxes = new TranxSalesChallanDutiesTaxes();
                        JsonObject igstObject = igstList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //   int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        //  dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransaction(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        }
        salesDutiesTaxesRepository.saveAll(salesDutiesTaxes);
    }

    public JsonObject getProductEditByIdByFPU(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesChallan invoiceTranx = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        productIds = tranxSalesChallanDetailsUnitsRepository.findProductIdsBySalesChallanIdAndStatus(invoiceTranx.getId(), true);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getProductEditByIdsByFPU(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String str = request.getParameter("s_c_id");
        JsonParser parser = new JsonParser();
        JsonElement salesDetailsJson = parser.parse(str);
        JsonArray jsonArray = salesDetailsJson.getAsJsonArray();
        JsonArray productArray = new JsonArray();
        JsonObject output = new JsonObject();
        JsonObject result = new JsonObject();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            TranxSalesChallan invoiceTranx = repository.findByIdAndStatus(object.get("id").getAsLong(), true);
            List<Object[]> productIds = new ArrayList<>();
            productIds = tranxSalesChallanDetailsUnitsRepository.findByTranxPurId(invoiceTranx.getId(), true);
          /*  productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);
            result.add("invoice_list", productArray);*/
            for (int i = 0; i < productIds.size(); i++) {
                JsonObject response = new JsonObject();
                Object obj[] = productIds.get(i);
                Product mProduct = productRepository.findByIdAndStatus(Long.parseLong(obj[0].toString()), true);
                JsonArray mLevelArray = new JsonArray();
                List<Long> levelaArray = productUnitRepository.findLevelAIdDistinct(mProduct.getId());
                for (Long mLeveA : levelaArray) {
                    JsonObject levelaJsonObject = new JsonObject();
                    LevelA levelA = null;
                    Long levelAId = null;
                    if (mLeveA != null) {
                        levelA = levelARepository.findByIdAndStatus(mLeveA, true);
                        if (levelA != null) {
                            levelAId = levelA.getId();
                            levelaJsonObject.addProperty("value", levelA.getId());
                            levelaJsonObject.addProperty("label", levelA.getLevelName());
                        }
                    } else {
                        levelaJsonObject.addProperty("value", "");
                        levelaJsonObject.addProperty("label", "");
                    }
                    JsonArray levelBArray = new JsonArray();
                    List<Long> levelBunits = productUnitRepository.findByProductsLevelB(mProduct.getId(), mLeveA);
                    for (Long mLeveB : levelBunits) {
                        JsonObject levelbJsonObject = new JsonObject();
                        Long levelBId = null;
                        LevelB levelB = null;
                        if (mLeveB != null) {
                            levelB = levelBRepository.findByIdAndStatus(mLeveB, true);
                            if (levelB != null) {
                                levelBId = levelB.getId();
                                levelbJsonObject.addProperty("value", levelB.getId());
                                levelbJsonObject.addProperty("label", levelB.getLevelName());
                            }
                        } else {
                            levelbJsonObject.addProperty("value", "");
                            levelbJsonObject.addProperty("label", "");
                        }
                        JsonArray levelCArray = new JsonArray();
                        List<Long> levelCunits = productUnitRepository.findByProductsLevelC(mProduct.getId(), mLeveA, mLeveB);
                        for (Long mLeveC : levelCunits) {
                            JsonObject levelcJsonObject = new JsonObject();
                            LevelC levelC = null;
                            Long levelCId = null;
                            if (mLeveC != null) {
                                levelC = levelCRepository.findByIdAndStatus(mLeveC, true);
                                if (levelC != null) {
                                    levelCId = levelC.getId();
                                    levelcJsonObject.addProperty("value", levelC.getId());
                                    levelcJsonObject.addProperty("label", levelC.getLevelName());
                                }
                            } else {
                                levelcJsonObject.addProperty("value", "");
                                levelcJsonObject.addProperty("label", "");
                            }
                            List<Object[]> unitList = productUnitRepository.findUniqueUnitsByProductId(mProduct.getId(), mLeveA, mLeveB, mLeveC);
                            JsonArray unitArray = new JsonArray();
                            for (int j = 0; j < unitList.size(); j++) {
                                Object[] objects = unitList.get(j);
                                Long unitId = Long.parseLong(objects[0].toString());
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("value", Long.parseLong(objects[0].toString()));
                                jsonObject.addProperty("unitId", Long.parseLong(objects[0].toString()));
                                jsonObject.addProperty("label", objects[1].toString());
                                jsonObject.addProperty("unitName", objects[1].toString());
                                jsonObject.addProperty("unitCode", objects[2].toString());
                                jsonObject.addProperty("unitConversion", objects[3].toString());
                                jsonObject.addProperty("product_name", mProduct.getProductName());
                                /***** Batch Number against Product *****/
                                if (mProduct.getIsBatchNumber()) {
                                    JsonArray batchArray = new JsonArray();
                                    List<ProductBatchNo> batchNos = productBatchNoRepository.findByUniqueBatchProductIdAndStatus(mProduct.getId(), levelAId, levelBId, levelCId, unitId, true);
                                    if (batchNos != null && batchNos.size() > 0) {
                                        for (ProductBatchNo mBatch : batchNos) {
                                            JsonObject batchObject = new JsonObject();
                                            batchObject.addProperty("batch_no", mBatch.getBatchNo() != null ? mBatch.getBatchNo() : "");
                                            batchObject.addProperty("b_no", mBatch.getBatchNo() != null ? mBatch.getBatchNo() : "");
                                            batchObject.addProperty("b_details_id", mBatch.getId());
                                            batchObject.addProperty("qty", mBatch.getQnty());
                                            batchObject.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                                            batchObject.addProperty("b_expiry", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                                            batchObject.addProperty("purchase_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.00);
                                            batchObject.addProperty("b_purchase_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.00);
                                            batchObject.addProperty("min_rate_a", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.00);
                                            batchObject.addProperty("min_rate_b", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0.00);
                                            batchObject.addProperty("min_rate_c", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0.00);
                                            batchObject.addProperty("free_qty", mBatch.getFreeQty());
                                            batchObject.addProperty("costing_with_tax", mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.00);
                                            batchObject.addProperty("manufacturing_date", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
                                            batchObject.addProperty("mrp", mBatch.getMrp() != null ? mBatch.getMrp() : 0.00);
                                            batchObject.addProperty("b_rate", mBatch.getMrp() != null ? mBatch.getMrp() : 0.00);
                                            batchObject.addProperty("min_margin", mBatch.getMinMargin() != null ? mBatch.getMinMargin() : 0.00);
                                            batchObject.addProperty("id", mBatch.getId());
                                            batchObject.addProperty("net_rate", mBatch.getCosting() != null ? mBatch.getCosting() : 0.00);
                                            batchObject.addProperty("costing", mBatch.getCosting() != null ? mBatch.getCosting() : 0.00);
                                            batchObject.addProperty("sale_rate", mBatch.getSalesRate() != null ? mBatch.getSalesRate() : 0.00);
                                            batchObject.addProperty("b_sale_rate", mBatch.getSalesRate() != null ? mBatch.getSalesRate() : 0.00);
                                            double salesRateWithTax = mBatch.getSalesRate();
                                            if (mBatch.getProduct().getTaxMaster() != null)
                                                salesRateWithTax = mBatch.getSalesRate() + ((mBatch.getSalesRate() * mBatch.getProduct().getTaxMaster().getIgst()) / 100);
                                            batchObject.addProperty("sales_rate_with_tax", salesRateWithTax);
                                            if (mBatch.getExpiryDate() != null) {
                                                if (invoiceTranx.getBillDate().after(DateConvertUtil.convertStringToDate(String.valueOf(mBatch.getExpiryDate())))) {
                                                    batchObject.addProperty("is_expired", true);
                                                } else {
                                                    batchObject.addProperty("is_expired", false);
                                                }
                                            } else {
                                                batchObject.addProperty("is_expired", false);
                                            }
                                            batchArray.add(batchObject);
                                        }
                                    }
                                    jsonObject.add("batchOpt", batchArray);
                                }

                                unitArray.add(jsonObject);
                            }
                            levelcJsonObject.add("unitOpts", unitArray);
                            levelCArray.add(levelcJsonObject);
                        }
                        levelbJsonObject.add("levelCOpts", levelCArray);
                        levelBArray.add(levelbJsonObject);
                    }
                    levelaJsonObject.add("levelBOpts", levelBArray);
                    mLevelArray.add(levelaJsonObject);
                }

                response.addProperty("product_id", obj[0].toString());
                response.addProperty("value", obj[0].toString());
                response.add("levelAOpt", mLevelArray);
                productArray.add(response);
            }
        }
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getSalesChallanByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        List<TranxSalesChallanAdditionalCharges> additionalCharges = new ArrayList<>();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxSalesChallan tranxSalesChallan = repository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            finalResult.addProperty("tcs", tranxSalesChallan.getTcs());
            finalResult.addProperty("narration", tranxSalesChallan.getNarration() != null ? tranxSalesChallan.getNarration() : "");
            finalResult.addProperty("discountLedgerId", tranxSalesChallan.getSalesDiscountLedger() != null ? tranxSalesChallan.getSalesDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", tranxSalesChallan.getSalesDiscountAmount());
            finalResult.addProperty("discountInPer", tranxSalesChallan.getSalesDiscountPer());
            finalResult.addProperty("totalSalesDiscountAmt", tranxSalesChallan.getTotalSalesDiscountAmt());

            finalResult.addProperty("totalQty", tranxSalesChallan.getTotalqty());
            finalResult.addProperty("totalFreeQty", tranxSalesChallan.getFreeQty());
            finalResult.addProperty("grossTotal", tranxSalesChallan.getGrossAmount());
            finalResult.addProperty("totalTax", tranxSalesChallan.getTotalTax());
            finalResult.addProperty("additionLedger1", tranxSalesChallan.getAdditionLedger1() != null ? tranxSalesChallan.getAdditionLedger1().getId() : 0);
            finalResult.addProperty("additionLedgerAmt1", tranxSalesChallan.getAdditionLedgerAmt1() != null ? tranxSalesChallan.getAdditionLedgerAmt1() : 0);
            finalResult.addProperty("additionLedger2", tranxSalesChallan.getAdditionLedger2() != null ? tranxSalesChallan.getAdditionLedger2().getId() : 0);
            finalResult.addProperty("additionLedgerAmt2", tranxSalesChallan.getAdditionLedgerAmt2() != null ? tranxSalesChallan.getAdditionLedgerAmt2() : 0);
            finalResult.addProperty("additionLedger3", tranxSalesChallan.getAdditionLedger3() != null ? tranxSalesChallan.getAdditionLedger3().getId() : 0);
            finalResult.addProperty("additionLedgerAmt3", tranxSalesChallan.getAdditionLedgerAmt3() != null ? tranxSalesChallan.getAdditionLedgerAmt3() : 0);
            finalResult.addProperty("additional_charges_total", tranxSalesChallan.getAdditionalChargesTotal() != null ? tranxSalesChallan.getAdditionalChargesTotal() : 0);
            JsonObject result = new JsonObject();

            /* Purchase Order Data */
            result.addProperty("id", tranxSalesChallan.getId());
            result.addProperty("sales_sr_no", tranxSalesChallan.getSalesChallanSerialNumber());
            result.addProperty("sales_account_id", tranxSalesChallan.getSalesAccountLedger().getId());
            result.addProperty("sales_account", tranxSalesChallan.getSalesAccountLedger().getLedgerName());
            result.addProperty("bill_date", DateConvertUtil.convertDateToLocalDate(tranxSalesChallan.getBillDate()).toString());
            result.addProperty("gstNo", tranxSalesChallan.getGstNumber());
            result.addProperty("sc_bill_no", tranxSalesChallan.getSc_bill_no());
            result.addProperty("tranx_unique_code", tranxSalesChallan.getTranxCode());
            result.addProperty("round_off", tranxSalesChallan.getRoundOff());
            result.addProperty("total_base_amount", tranxSalesChallan.getTotalBaseAmount());
            result.addProperty("total_amount", tranxSalesChallan.getTotalAmount());
            result.addProperty("total_cgst", tranxSalesChallan.getTotalcgst());
            result.addProperty("total_sgst", tranxSalesChallan.getTotalsgst());
            result.addProperty("total_igst", tranxSalesChallan.getTotaligst());
            result.addProperty("total_qty", tranxSalesChallan.getTotalqty());
            result.addProperty("taxable_amount", tranxSalesChallan.getTaxableAmount());
            result.addProperty("tcs", tranxSalesChallan.getTcs());
            result.addProperty("status", tranxSalesChallan.getStatus());
            result.addProperty("financial_year", tranxSalesChallan.getFinancialYear());
            result.addProperty("debtor_id", tranxSalesChallan.getSundryDebtors().getId());
            result.addProperty("debtor_name", tranxSalesChallan.getSundryDebtors().getLedgerName());
            result.addProperty("narration", tranxSalesChallan.getNarration() != null ? tranxSalesChallan.getNarration() : "");
            result.addProperty("source", "sales_challan");
            result.addProperty("isRoundOffCheck", tranxSalesChallan.getIsRoundOff());
            result.addProperty("roundoff", tranxSalesChallan.getRoundOff());
            result.addProperty("ledgerStateCode", tranxSalesChallan.getSundryDebtors().getStateCode());


            /* Purchase Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            additionalCharges = tranxSalesChallanAdditionalChargesRepository.findBySalesTransactionIdAndStatus(tranxSalesChallan.getId(), true);
            if (additionalCharges.size() > 0) {
                for (TranxSalesChallanAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledgerId", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    json_charges.addProperty("percent", mAdditionalCharges.getPercent());
                    jsonAdditionalList.add(json_charges);
                }
            }

            finalResult.add("additionalCharges", jsonAdditionalList);


            /* End of Sales Challan Data */
            /* Sales Challan Details */
            JsonArray row = new JsonArray();
            List<TranxSalesChallanDetailsUnits> unitsList = tranxSalesChallanDetailsUnitsRepository.findBySalesChallanIdAndTransactionStatusIdAndStatus(tranxSalesChallan.getId(), 1L, true);
            for (TranxSalesChallanDetailsUnits mUnits : unitsList) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ? mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ? mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ? mUnits.getLevelC().getId().toString() : "");
                unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "");
                unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions() != null ? mUnits.getUnitConversions() : 1.0);
                unitsJsonObjects.addProperty("qty", mUnits.getQty());
                unitsJsonObjects.addProperty("rate", mUnits.getRate());
                unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt() != null ? mUnits.getBaseAmt() : 0.0);
                unitsJsonObjects.addProperty("dis_amt", mUnits.getDiscountAmount() != null ? mUnits.getDiscountAmount() : 0.0);
                unitsJsonObjects.addProperty("dis_per", mUnits.getDiscountPer() != null ? mUnits.getDiscountPer() : 0.0);
                unitsJsonObjects.addProperty("dis_per_cal", mUnits.getDiscountPerCal() != null ? mUnits.getDiscountPerCal() : 0.0);
                unitsJsonObjects.addProperty("dis_amt_cal", mUnits.getDiscountAmountCal() != null ? mUnits.getDiscountAmountCal() : 0.0);
                unitsJsonObjects.addProperty("total_amt", mUnits.getTotalAmount());
                unitsJsonObjects.addProperty("gst", mUnits.getIgst());
                unitsJsonObjects.addProperty("igst", mUnits.getIgst());
                unitsJsonObjects.addProperty("cgst", mUnits.getCgst());
                unitsJsonObjects.addProperty("sgst", mUnits.getSgst());
                unitsJsonObjects.addProperty("total_igst", mUnits.getTotalIgst());
                unitsJsonObjects.addProperty("total_cgst", mUnits.getTotalCgst());
                unitsJsonObjects.addProperty("total_sgst", mUnits.getTotalSgst());
                unitsJsonObjects.addProperty("final_amt", mUnits.getFinalAmount());
                unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty() != null ? mUnits.getFreeQty() : 0.0);
                unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer() != null ? mUnits.getDiscountBInPer() : 0.0);
                unitsJsonObjects.addProperty("row_dis_amt", mUnits.getTotalDiscountInAmt() != null ? mUnits.getTotalDiscountInAmt() : 0.0);
                unitsJsonObjects.addProperty("gross_amt", mUnits.getGrossAmt() != null ? mUnits.getGrossAmt() : 0.0);
                unitsJsonObjects.addProperty("add_chg_amt", mUnits.getAdditionChargesAmt() != null ? mUnits.getAdditionChargesAmt() : 0.0);
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1() != null ? mUnits.getGrossAmt1() : 0.0);
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt() != null ? mUnits.getInvoiceDisAmt() : 0.0);
                if (mUnits.getProductBatchNo() != null) {
                    if (mUnits.getProductBatchNo().getExpiryDate() != null) {
                        if (tranxSalesChallan.getBillDate().after(DateConvertUtil.convertStringToDate(String.valueOf(mUnits.getProductBatchNo().getExpiryDate())))) {
                            unitsJsonObjects.addProperty("is_expired", true);
                        } else {
                            unitsJsonObjects.addProperty("is_expired", false);
                        }
                    } else {
                        unitsJsonObjects.addProperty("is_expired", false);
                    }
                    unitsJsonObjects.addProperty("b_detailsId", mUnits.getProductBatchNo().getId());
                    unitsJsonObjects.addProperty("batch_no", mUnits.getProductBatchNo().getBatchNo());
//                    unitsJsonObjects.addProperty("qty", mUnits.getProductBatchNo().getQnty());
                    unitsJsonObjects.addProperty("b_expiry", mUnits.getProductBatchNo().getExpiryDate() != null ? mUnits.getProductBatchNo().getExpiryDate().toString() : "");
                    unitsJsonObjects.addProperty("purchase_rate", mUnits.getProductBatchNo().getPurchaseRate() != null ? mUnits.getProductBatchNo().getPurchaseRate() : 0.0);
                    unitsJsonObjects.addProperty("is_batch", true);
                    unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA() != null ? mUnits.getProductBatchNo().getMinRateA() : 0.0);
                    unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB() != null ? mUnits.getProductBatchNo().getMinRateB() : 0.0);
                    unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC() != null ? mUnits.getProductBatchNo().getMinRateC() : 0.0);
                    unitsJsonObjects.addProperty("min_discount", mUnits.getProductBatchNo().getMinDiscount() != null ? mUnits.getProductBatchNo().getMinDiscount() : 0.0);
                    unitsJsonObjects.addProperty("max_discount", mUnits.getProductBatchNo().getMaxDiscount() != null ? mUnits.getProductBatchNo().getMaxDiscount() : 0.0);
                    unitsJsonObjects.addProperty("manufacturing_date", mUnits.getProductBatchNo().getManufacturingDate() != null ? mUnits.getProductBatchNo().getManufacturingDate().toString() : "");
                    unitsJsonObjects.addProperty("b_rate", mUnits.getProductBatchNo().getMrp() != null ? mUnits.getProductBatchNo().getMrp() : 0.0);
                    unitsJsonObjects.addProperty("min_margin", mUnits.getProductBatchNo().getMinMargin() != null ? mUnits.getProductBatchNo().getMinMargin() : 0.0);
                    unitsJsonObjects.addProperty("sales_rate", mUnits.getProductBatchNo().getSalesRate() != null ? mUnits.getProductBatchNo().getSalesRate() : 0.0);
                    unitsJsonObjects.addProperty("costing", mUnits.getProductBatchNo().getCosting() != null ? mUnits.getProductBatchNo().getCosting() : 0.0);
                    unitsJsonObjects.addProperty("costingWithTax", mUnits.getProductBatchNo().getCostingWithTax() != null ? mUnits.getProductBatchNo().getCostingWithTax() : 0.0);
                } else {
                    unitsJsonObjects.addProperty("b_detailsId", "");
                    unitsJsonObjects.addProperty("batch_no", "");
                    unitsJsonObjects.addProperty("qty", mUnits.getQty());
                    unitsJsonObjects.addProperty("b_expiry", "");
                    unitsJsonObjects.addProperty("purchase_rate", "");
                    unitsJsonObjects.addProperty("is_batch", "");
                    unitsJsonObjects.addProperty("min_rate_a", "");
                    unitsJsonObjects.addProperty("min_rate_b", "");
                    unitsJsonObjects.addProperty("min_rate_c", "");
                    unitsJsonObjects.addProperty("min_discount", "");
                    unitsJsonObjects.addProperty("max_discount", "");
                    unitsJsonObjects.addProperty("manufacturing_date", "");
                    unitsJsonObjects.addProperty("mrp", "");
                    unitsJsonObjects.addProperty("b_rate", "");
                    unitsJsonObjects.addProperty("min_margin", "");
                    unitsJsonObjects.addProperty("costing", "");
                    unitsJsonObjects.addProperty("costingWithTax", "");
                }
                /**** Serial Number ****/
                List<Object[]> serialNum = new ArrayList<>();
                JsonArray serialNumJson = new JsonArray();
                serialNum = serialNumberRepository.findSerialnumbers(mUnits.getProduct().getId(), mUnits.getId(), true);
                for (int i = 0; i < serialNum.size(); i++) {
                    JsonObject jsonObject = new JsonObject();
                    Object obj[] = serialNum.get(i);
                    jsonObject.addProperty("serial_detail_id", Long.parseLong(obj[0].toString()));
                    jsonObject.addProperty("serial_no", obj[1].toString());
                    serialNumJson.add(jsonObject);
                }
                unitsJsonObjects.add("serialNo", serialNumJson);
                row.add(unitsJsonObjects);
            }
            List<LedgerGstDetails> gstDetails = new ArrayList<>();
            gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(tranxSalesChallan.getSundryDebtors().getId(), true);
            JsonArray gstArray = new JsonArray();
            if (gstDetails != null && gstDetails.size() > 0) {
                for (LedgerGstDetails mGstDetails : gstDetails) {
                    JsonObject mGstObject = new JsonObject();
                    mGstObject.addProperty("id", mGstDetails.getId());
                    mGstObject.addProperty("gstNo", mGstDetails.getGstin());
                    mGstObject.addProperty("state", mGstDetails.getStateCode() != null ? mGstDetails.getStateCode() : "");
                    gstArray.add(mGstObject);
                }
            }
            finalResult.add("gstDetails", gstArray);

            /* End of Sales Quotations Details */
            finalResult.add("row", row);
            finalResult.add("invoice_data", result);
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            challanNewLogger.error("Error in getSalesChallanByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            challanNewLogger.error("Error in getSalesChallanByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject getSalesChallanSupplierListByProductId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        Long productId = Long.parseLong(request.getParameter("productId"));

        List<TranxSalesChallanDetailsUnits> tranxSalesOrderDetailsUnits = tranxSalesChallanDetailsUnitsRepository.findByProductIdAndStatusOrderByIdDesc(productId, true);

        for (TranxSalesChallanDetailsUnits obj : tranxSalesOrderDetailsUnits) {
            JsonObject response = new JsonObject();
            response.addProperty("supplier_name", obj.getSalesChallan().getSundryDebtors().getLedgerName());
            response.addProperty("invoice_no", obj.getSalesChallan().getId());
            response.addProperty("invoice_date", obj.getSalesChallan().getBillDate().toString());

            if (obj.getProductBatchNo() != null) {
                response.addProperty("batch", obj.getProductBatchNo().getBatchNo());
                response.addProperty("mrp", obj.getProductBatchNo().getMrp());
                response.addProperty("cost", obj.getProductBatchNo().getCosting());

            } else {
                ProductUnitPacking productUnitPacking = productUnitRepository.findByIdAndStatus(productId, true);
                response.addProperty("mrp", productUnitPacking.getMrp());
                response.addProperty("cost", productUnitPacking.getCosting());
                response.addProperty("rate", productUnitPacking.getPurchaseRate());
            }

            response.addProperty("quantity", obj.getQty());
            response.addProperty("rate", obj.getRate());
            response.addProperty("dis_per", obj.getDiscountPer());
            response.addProperty("dis_amt", obj.getDiscountAmount());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public JsonObject saleChallanPendingList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), true);
        List<TranxSalesChallan> tranxSalesChallans = salesTransactionRepository.findBySundryDebtorsIdAndStatusAndTransactionStatusId(sundryDebtors.getId(), true, 1L);
        for (TranxSalesChallan invoices : tranxSalesChallans) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("bill_no", invoices.getSalesChallanInvoiceNo());
            response.addProperty("bill_date", DateConvertUtil.convertDateToLocalDate(invoices.getBillDate()).toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("total_base_amount", invoices.getTotalBaseAmount());
            response.addProperty("sundry_debtors_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtors_id", invoices.getSundryDebtors().getId());
            response.addProperty("sales_challan_status", invoices.getTransactionStatus().getStatusName());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration());
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTaxableAmount());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public Object validateSalesChallan(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesChallan salesChallan = null;
        if (users.getBranch() != null) {
            salesChallan = salesTransactionRepository.findByScBillWithBranch(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesChallanNo"));
        } else {
            salesChallan = salesTransactionRepository.findByScBill(users.getOutlet().getId(), request.getParameter("salesChallanNo"));
        }
        if (salesChallan != null) {
            // System.out.println("Already Ledger created with this name or code");
            responseMessage.setMessage("Duplicate sales challan number exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object validateSalesChallanUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Long invoiceId = Long.parseLong(request.getParameter("invoice_id"));
        TranxSalesChallan salesChallan = null;
        if (users.getBranch() != null) {
            salesChallan = salesTransactionRepository.findByScBillWithBranch(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesChallanNo"));
        } else {
            salesChallan = salesTransactionRepository.findByScBill(users.getOutlet().getId(), request.getParameter("salesChallanNo"));
        }
        if (salesChallan != null && invoiceId != salesChallan.getId()) {
            responseMessage.setMessage("Duplicate sales challan number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales challan number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }
}
