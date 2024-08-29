package in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.puarchasedto.PurOrderDTO;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerGstDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderDutiesTaxesRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesOrderRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Query;

@Service
public class TranxPurOrderService {

    @Autowired
    private NumFormat numFormat;
    @Autowired
    private TranxPurOrderRepository tranxPurOrderRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TranxPurOrderDutiesTaxesRepository tranxPurOrderDutiesTaxesRepository;
    @Autowired
    private TranxPurOrderDetailsRepository tranxPurOrderDetailsRepository;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private TranxPurOrderDetailsUnitRepository tranxPurOrderDetailsUnitRepository;
    @Autowired
    private ProductData productData;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;
    private static final Logger purOrderLogger = LogManager.getLogger(TranxPurOrderService.class);

    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice
    List<Long> mInputList = new ArrayList<>();
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TranxSalesOrderRepository tranxSalesOrderRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private LedgerGstDetailsRepository ledgerGstDetailsRepository;

    /* creating purchase order */
    public Object insertPOInvoice(HttpServletRequest request) {
        TranxPurOrder mPurchaseTranx = null;
        ResponseMessage responseMessage = new ResponseMessage();
        mPurchaseTranx = saveIntoPOInvoice(request);
        if (mPurchaseTranx != null) {


            if (!request.getHeader("branch").equalsIgnoreCase("gvmh001")) {
                //call PoToSo api
                Users franchiseUser = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

                HttpHeaders gvHdr = new HttpHeaders();
                gvHdr.setContentType(MediaType.MULTIPART_FORM_DATA);
                gvHdr.add("branch", "gvmh001");

                LinkedMultiValueMap gvBody = new LinkedMultiValueMap();
                gvBody.add("usercode", "gvmh001");
                gvBody.add("franchiseUser", franchiseUser);
                gvBody.add("transactionTrackingNo", mPurchaseTranx.getTransactionTrackingNo());
                gvBody.add("invoice_date", request.getParameter("invoice_date"));
                gvBody.add("transaction_date", request.getParameter("transaction_date"));
                gvBody.add("roundoff", request.getParameter("roundoff"));
                gvBody.add("narration", request.getParameter("narration"));
                gvBody.add("totalamt", request.getParameter("totalamt"));
                gvBody.add("totalcgst", request.getParameter("totalcgst"));
                gvBody.add("totalsgst", request.getParameter("totalsgst"));
                gvBody.add("totaligst", request.getParameter("totaligst"));
                gvBody.add("tcs", request.getParameter("tcs"));
                gvBody.add("row", request.getParameter("row"));
                gvBody.add("additionalChargesTotal", request.getParameter("additionalChargesTotal"));
                gvBody.add("total_qty", request.getParameter("total_qty"));
                gvBody.add("total_free_qty", request.getParameter("total_free_qty"));
                gvBody.add("total_row_gross_amt", request.getParameter("total_row_gross_amt"));
                gvBody.add("total_base_amt", request.getParameter("total_base_amt"));
                gvBody.add("taxable_amount", request.getParameter("taxable_amount"));
                gvBody.add("total_tax_amt", request.getParameter("total_tax_amt"));
                gvBody.add("bill_amount", request.getParameter("bill_amount"));
                gvBody.add("taxCalculation", request.getParameter("taxCalculation"));

                HttpEntity gvEntity = new HttpEntity<>(gvBody, gvHdr);

                /* Cross API call commented for testing
                String gvData = restTemplate.exchange(
                        serverUrl + "/po_to_so_invoices", HttpMethod.POST, gvEntity, String.class).getBody();

                System.out.println("gvData Response => " + gvData);*/

            }

            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(mPurchaseTranx.getSundryCreditors().getId(), true);
            ledgerMaster.setIsDeleted(false);
            ledgerMasterRepository.save(ledgerMaster);

            /**
             * @implNote validation of Ledger Delete , if any tranx done for this ledger, user cant delete this ledger **
             * @auther ashwins@opethic.com
             * @version sprint 21
             **/

            responseMessage.setMessage("Purchase order created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());

        } else {
            responseMessage.setMessage("Error in purchase order creation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }


        return responseMessage;
    }

    public Object insertPoToSoInvoiceOrg(HttpServletRequest request) {

        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();

        try {

            String franchiseUser = request.getParameter("franchiseUser");
            System.out.println("franchiseUser=>" + franchiseUser);
            JsonObject jsonObject = new JsonParser().parse(franchiseUser).getAsJsonObject();
//            System.out.println("jsonObject "+jsonObject.get("companyCode").getAsString());

            Users cadmin = usersRepository.findTop1ByUserRoleIgnoreCaseAndCompanyCode("cadmin", "gvmh001");
//        Users cadmin = usersRepository.findById(3L).get();
//            System.out.println("cadmin :"+cadmin.toString());
            String cadminToken = jwtRequestFilter.getTokenFromUsername(cadmin.getUsername());
            System.out.println("cadminToken :" + cadminToken);

//            System.out.println("getUserToken API Response => " + gvData.toString());
//            JsonObject jsonObject1 = new JsonParser().parse(gvData.toString()).getAsJsonObject();
//            System.out.println("jsonObject1 data "+jsonObject1.get("response").getAsString());
//
//            String cadminToken = jsonObject1.get("response").getAsString();
//            JsonObject cadmin = jsonObject1.get("responseObject").getAsJsonObject();
//            System.out.println("cadmin "+cadmin.toString());

            Long outletId = cadmin.getOutlet().getId();
            Long branchId = cadmin.getBranch() != null ? cadmin.getBranch().getId() : null;

//            if(!cadmin.get("outlet").isJsonNull())
//                outletId = cadmin.get("outlet").getAsJsonObject().get("id").getAsLong();
//            System.out.println("outletId "+outletId);
//
//            if(!cadmin.get("branch").isJsonNull())
//                branchId = cadmin.get("branch").getAsJsonObject().get("id").getAsLong();
//            System.out.println("branchId "+branchId);

            if (cadmin != null) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    headers.add("branch", "gvmh001");
                    headers.add("Authorization", "Bearer " + cadminToken);

//                        String uname = jwtRequestFilter.getUsernameFromToken("gvData");
//                        Users users = usersRepository.findByUsername(uname);
                    Long count = 0L;
                    if (branchId != null) {
                        count = tranxSalesOrderRepository.findBranchLastRecord(outletId, branchId);
                    } else {
                        count = tranxSalesOrderRepository.findLastRecord(outletId);
                    }
                    String serailNo = String.format("%05d", count + 1);
                    GenerateDates generateDates = new GenerateDates();
                    String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
                    String csCode = "SO" + currentMonth + serailNo;

                    LedgerMaster salesAcc = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatus("SLAC",
                            outletId, true);
                    LedgerMaster franchiseLedger = ledgerMasterRepository.findByLedgerCodeAndUniqueCodeAndOutletIdAndStatus(
                            jsonObject.get("companyCode").getAsString(), "SUDR", outletId, true);

                    LinkedMultiValueMap body = new LinkedMultiValueMap();
                    body.add("transactionTrackingNo", request.getParameter("transactionTrackingNo"));
                    body.add("bill_dt", request.getParameter("invoice_date"));
                    body.add("newReference", false);
                    body.add("bill_no", csCode);
                    body.add("sales_acc_id", salesAcc.getId());
                    body.add("sales_sr_no", count + 1);
                    body.add("transaction_date", request.getParameter("transaction_date"));
                    body.add("debtors_id", franchiseLedger.getId());
                    body.add("roundoff", request.getParameter("roundoff"));
                    body.add("narration", request.getParameter("narration"));
                    if (paramMap.containsKey("narration"))
                        body.add("narration", request.getParameter("narration"));
                    else {
                        body.add("narration", "");
                    }
                    body.add("totalamt", request.getParameter("totalamt"));
                    body.add("total_purchase_discount_amt", 0);
                    body.add("gstNo", request.getParameter("name"));
                    body.add("totalcgst", request.getParameter("totalcgst"));
                    body.add("totalsgst", request.getParameter("totalsgst"));
                    body.add("totaligst", request.getParameter("totaligst"));
                    body.add("tcs", request.getParameter("tcs"));
                    body.add("sales_discount", 0);
                    body.add("sales_discount_amt", 0);
                    body.add("total_sales_discount_amt", 0);
                    body.add("row", request.getParameter("row"));
                    body.add("additionalChargesTotal", request.getParameter("additionalChargesTotal"));
                    body.add("total_qty", request.getParameter("total_qty"));
                    body.add("total_free_qty", request.getParameter("total_free_qty"));
                    body.add("total_row_gross_amt", request.getParameter("total_row_gross_amt"));
                    body.add("total_base_amt", request.getParameter("total_base_amt"));
                    body.add("total_invoice_dis_amt", 0);
                    body.add("taxable_amount", request.getParameter("taxable_amount"));
                    body.add("total_tax_amt", request.getParameter("total_tax_amt"));
                    body.add("bill_amount", request.getParameter("bill_amount"));
                    body.add("taxFlag", false);
                    body.add("taxCalculation", request.getParameter("taxCalculation"));
                    HttpEntity entity = new HttpEntity<>(body, headers);

                    String response = restTemplate.exchange(
                            serverUrl + "/create_sales_order_invoice", HttpMethod.POST, entity, String.class).getBody();
                    System.out.println("create_sales_order_invoice API Response => " + response);


                    responseMessage.setMessage("Sales order created successfully");
                    responseMessage.setResponseStatus(HttpStatus.OK.value());

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

    public Object insertPoToSoInvoice(HttpServletRequest request) {

        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();

        try {

            String franchiseUser = request.getParameter("franchiseUser");
            System.out.println("franchiseUser=>" + franchiseUser);
            JsonObject jsonObject = new JsonParser().parse(franchiseUser).getAsJsonObject();
//            System.out.println("jsonObject "+jsonObject.get("companyCode").getAsString());

            Users cadmin = usersRepository.findTop1ByUserRoleIgnoreCaseAndCompanyCode("cadmin", "gvmh001");
//        Users cadmin = usersRepository.findById(3L).get();
//            System.out.println("cadmin :"+cadmin.toString());
            String cadminToken = jwtRequestFilter.getTokenFromUsername(cadmin.getUsername());
            System.out.println("cadminToken :" + cadminToken);

//            System.out.println("getUserToken API Response => " + gvData.toString());
//            JsonObject jsonObject1 = new JsonParser().parse(gvData.toString()).getAsJsonObject();
//            System.out.println("jsonObject1 data "+jsonObject1.get("response").getAsString());
//
//            String cadminToken = jsonObject1.get("response").getAsString();
//            JsonObject cadmin = jsonObject1.get("responseObject").getAsJsonObject();
//            System.out.println("cadmin "+cadmin.toString());

            Long outletId = cadmin.getOutlet().getId();
            Long branchId = cadmin.getBranch() != null ? cadmin.getBranch().getId() : null;

//            if(!cadmin.get("outlet").isJsonNull())
//                outletId = cadmin.get("outlet").getAsJsonObject().get("id").getAsLong();
//            System.out.println("outletId "+outletId);
//
//            if(!cadmin.get("branch").isJsonNull())
//                branchId = cadmin.get("branch").getAsJsonObject().get("id").getAsLong();
//            System.out.println("branchId "+branchId);

            if (cadmin != null) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    headers.add("branch", "gvmh001");
                    headers.add("Authorization", "Bearer " + cadminToken);

//                        String uname = jwtRequestFilter.getUsernameFromToken("gvData");
//                        Users users = usersRepository.findByUsername(uname);
                    Long count = 0L;
                    if (branchId != null) {
                        count = tranxSalesOrderRepository.findBranchLastRecord(outletId, branchId);
                    } else {
                        count = tranxSalesOrderRepository.findLastRecord(outletId);
                    }
                    String serailNo = String.format("%05d", count + 1);
                    GenerateDates generateDates = new GenerateDates();
                    String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
                    String csCode = "SO" + currentMonth + serailNo;

                    LedgerMaster salesAcc = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatus("SLAC",
                            outletId, true);
                    LedgerMaster franchiseLedger = ledgerMasterRepository.findByLedgerCodeAndUniqueCodeAndOutletIdAndStatus(
                            jsonObject.get("companyCode").getAsString().toLowerCase(), "SUDR", outletId, true);

                    LinkedMultiValueMap body = new LinkedMultiValueMap();
                    body.add("transactionTrackingNo", request.getParameter("transactionTrackingNo"));
                    body.add("bill_dt", request.getParameter("invoice_date"));
                    body.add("newReference", false);
                    body.add("bill_no", csCode);
                    body.add("sales_acc_id", salesAcc.getId());
                    body.add("sales_sr_no", count + 1);
                    body.add("transaction_date", request.getParameter("transaction_date"));
                    body.add("debtors_id", franchiseLedger.getId());
                    body.add("roundoff", request.getParameter("roundoff"));
                    body.add("narration", request.getParameter("narration"));
                    if (paramMap.containsKey("narration"))
                        body.add("narration", request.getParameter("narration"));
                    else {
                        body.add("narration", "");
                    }
                    body.add("totalamt", request.getParameter("totalamt"));
                    body.add("total_purchase_discount_amt", 0);
                    //*
                    body.add("gstNo", request.getParameter("name"));
                    body.add("totalcgst", request.getParameter("totalcgst"));
                    body.add("totalsgst", request.getParameter("totalsgst"));
                    body.add("totaligst", request.getParameter("totaligst"));
                    body.add("tcs", request.getParameter("tcs"));
                    body.add("sales_discount", 0);
                    body.add("sales_discount_amt", 0);
                    body.add("total_sales_discount_amt", 0);
                    JsonArray jsonArray = new JsonParser().parse(request.getParameter("row")).getAsJsonArray();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Product mProduct = productRepository.findByProductCode(jsonArray.get(i).getAsJsonObject().get("productCode").getAsString());
                        jsonArray.get(i).getAsJsonObject().remove("productId");
                        jsonArray.get(i).getAsJsonObject().addProperty("productId", mProduct.getId());

                        Units units = unitsRepository.findFirstByUnitNameIgnoreCase(jsonArray.get(i).getAsJsonObject().get("productUnit").getAsString());
                        jsonArray.get(i).getAsJsonObject().remove("unitId");
                        jsonArray.get(i).getAsJsonObject().addProperty("unitId", units.getId());
                    }
                    //*
                    body.add("row", jsonArray.toString());
                    body.add("additionalChargesTotal", request.getParameter("additionalChargesTotal"));
                    body.add("total_qty", request.getParameter("total_qty"));
                    body.add("total_free_qty", request.getParameter("total_free_qty"));
                    body.add("total_row_gross_amt", request.getParameter("total_row_gross_amt"));
                    body.add("total_base_amt", request.getParameter("total_base_amt"));
                    body.add("total_invoice_dis_amt", 0);
                    body.add("taxable_amount", request.getParameter("taxable_amount"));
                    body.add("total_tax_amt", request.getParameter("total_tax_amt"));
                    body.add("bill_amount", request.getParameter("bill_amount"));
                    body.add("taxFlag", request.getParameter("taxFlag"));
                    body.add("taxCalculation", request.getParameter("taxCalculation"));
                    if (paramMap.containsKey("order_reference_no")) {
                        body.add("order_reference_no", request.getParameter("order_reference_no"));
                    }
                    HttpEntity entity = new HttpEntity<>(body, headers);
                    System.out.println("body" + body);

                    String response = restTemplate.exchange(
                            serverUrl + "/create_sales_order_invoice", HttpMethod.POST, entity, String.class).getBody();
                    System.out.println("create_sales_order_invoice API Response => " + response);


                    responseMessage.setMessage("Sales order created successfully");
                    responseMessage.setResponseStatus(HttpStatus.OK.value());

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

    public Object validatePurchaseOrder(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurOrder purOrder = null;
        Long sundryCreditorId = Long.parseLong(request.getParameter("supplier_id"));
        if (users.getBranch() != null) {
            purOrder = tranxPurOrderRepository.findByOutletIdAndBranchIdAndSundryCreditorsIdAndStatusAndVendorInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), sundryCreditorId, true, request.getParameter("bill_no"));
        } else {
            purOrder = tranxPurOrderRepository.findByOutletIdAndSundryCreditorsIdAndStatusAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), sundryCreditorId, true, request.getParameter("bill_no"));
        }
        if (purOrder != null) {
            responseMessage.setMessage("Duplicate purchase order number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New purchase order number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object validateOrderNoUpdateNo(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurOrder purOrder = null;
        Long sundryCreditorId = Long.parseLong(request.getParameter("supplier_id"));
        Long invoiceId = Long.parseLong(request.getParameter("invoice_id"));
        if (users.getBranch() != null) {



            purOrder = tranxPurOrderRepository.findByOutletIdAndBranchIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), sundryCreditorId, request.getParameter("bill_no"));
        } else {
            purOrder = tranxPurOrderRepository.findByOutletIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), sundryCreditorId, request.getParameter("bill_no"));
        }
        if (purOrder != null && invoiceId != purOrder.getId()) {
            responseMessage.setMessage("Duplicate purchase invoice number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New purchase invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    /******* Save into Purchase Order
     * @return*******/
    public TranxPurOrder saveIntoPOInvoice(HttpServletRequest request) {
        TranxPurOrder mPurchaseTranx = null;
        TransactionTypeMaster tranxType = null;
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        TranxPurOrder tranxPurOrder = new TranxPurOrder();
        if (users.getBranch() != null) {
            branch = users.getBranch();
            tranxPurOrder.setBranch(branch);
        }
        tranxPurOrder.setOutlet(outlet);
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        tranxPurOrder.setTransactionStatus(transactionStatus);
        tranxPurOrder.setCreatedBy(users.getId());
        tranxPurOrder.setOrderReference(request.getParameter("pur_order_no"));
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSORD");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        tranxPurOrder.setTranxCode(tranxCode);
//        LocalDate mDate = LocalDate.parse(request.getParameter("invoice_date"));
        LocalDate mTranxDate = DateConvertUtil.convertStringToLocalDate(request.getParameter("invoice_date"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("invoice_date"));

        tranxPurOrder.setInvoiceDate(strDt);
        tranxPurOrder.setTransactionDate(mTranxDate);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(mTranxDate);
        if (fiscalYear != null) {
            tranxPurOrder.setFiscalYear(fiscalYear);
            tranxPurOrder.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        tranxPurOrder.setPurOrdSrno(Long.parseLong(request.getParameter("purchase_sr_no")));
        tranxPurOrder.setVendorInvoiceNo(request.getParameter("invoice_no"));
        if (paramMap.containsKey("transport_name"))
            tranxPurOrder.setTransportName(request.getParameter("transport_name"));
        else {
            tranxPurOrder.setTransportName("");
        }
        if (paramMap.containsKey("reference"))
            tranxPurOrder.setReference(request.getParameter("reference"));
        else {
            tranxPurOrder.setReference("");
        }
        LedgerMaster purchaseAccount = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("purchase_id")), true);
        LedgerMaster sundryCreditors = ledgerMasterRepository.findByIdAndStatus(
                Long.parseLong(request.getParameter("supplier_code_id")),
                true);
        tranxPurOrder.setPurchaseAccountLedger(purchaseAccount);
        tranxPurOrder.setSundryCreditors(sundryCreditors);

        tranxPurOrder.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        //  LedgerMaster roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCase(outlet.getId(), "Round off");
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        tranxPurOrder.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        tranxPurOrder.setPurchaseRoundOff(roundoff);
        tranxPurOrder.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
        if (taxFlag) {
            tranxPurOrder.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            tranxPurOrder.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            tranxPurOrder.setTotaligst(0.0);
        }
        /* if false : igst i.e inter state */
        else {
            tranxPurOrder.setTotalcgst(0.0);
            tranxPurOrder.setTotalsgst(0.0);
            tranxPurOrder.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }
        if (paramMap.containsKey("totalqty"))
            tranxPurOrder.setTotalqty(Long.parseLong(request.getParameter("totalqty")));
        if (paramMap.containsKey("tcs"))
            tranxPurOrder.setTcs(Double.parseDouble(request.getParameter("tcs")));
        tranxPurOrder.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        tranxPurOrder.setStatus(true);
        tranxPurOrder.setCreatedBy(users.getId());
        tranxPurOrder.setOperations("inserted");
        if (paramMap.containsKey("narration"))
            tranxPurOrder.setNarration(request.getParameter("narration"));
        else {
            tranxPurOrder.setNarration("");
        }
        tranxPurOrder.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                tranxPurOrder.setGstNumber(request.getParameter("gstNo"));
            }
        }
        tranxPurOrder.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        tranxPurOrder.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        if (paramMap.containsKey("tcs")) tranxPurOrder.setTcs(Double.parseDouble(request.getParameter("tcs")));
        tranxPurOrder.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        tranxPurOrder.setPurchaseDiscountPer(Double.parseDouble(request.getParameter("purchase_discount")));
        tranxPurOrder.setPurchaseDiscountAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
        tranxPurOrder.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        tranxPurOrder.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        tranxPurOrder.setPurOrdSrno(Long.parseLong(request.getParameter("purchase_sr_no")));

        tranxPurOrder.setTransactionTrackingNo(String.valueOf(new Date().getTime()));
        try {
            mPurchaseTranx = tranxPurOrderRepository.save(tranxPurOrder);
            if (mPurchaseTranx != null) {
                /* Save into Duties and Taxes */
                String taxStr = request.getParameter("taxCalculation");
                // JsonObject duties_taxes = new JsonObject(taxStr);
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoPOInvoiceDutiesTaxes(duties_taxes, mPurchaseTranx, taxFlag);
                /* save into Details  */
                String jsonStr = request.getParameter("row");
                JsonParser parser = new JsonParser();
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                //JsonArray array = new JsonArray(jsonStr);
                saveIntoPOInvoiceDetails(array, mPurchaseTranx,
                        branch, outlet, users.getId(), tranxType, "create", "");
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purOrderLogger.error("Error in saveIntoPOInvoice" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            purOrderLogger.error("Error in saveIntoPOInvoice " + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mPurchaseTranx;
    }
    /* End of Purchase Invoice */

    /****** Save into Duties and Taxes ******/
    public void saveIntoPOInvoiceDutiesTaxes(
            JsonObject duties_taxes, TranxPurOrder mPurchaseTranx, Boolean taxFlag) {
        List<TranxPurOrderDutiesTaxes> tranxPurOrderDutiesTaxes = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (JsonElement mList : cgstList) {
                    TranxPurOrderDutiesTaxes tranxPurOrderDutiesTaxes1 = null;
                    JsonObject cgstObject = mList.getAsJsonObject();
                    // JsonObject cgstObject = cgstList.getAsJsonObject(i);
                    LedgerMaster dutiesTaxes = null;
                  /*  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String ledgerName = "INPUT CGST " + inputGst;*/
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT CGST " + inputGst;
                   /* dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCase(
                            mPurchaseTranx.getOutlet().getId(), ledgerName);*/
                    if (mPurchaseTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPurchaseTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        tranxPurOrderDutiesTaxes1 = new TranxPurOrderDutiesTaxes();

                        //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                        tranxPurOrderDutiesTaxes1.setDutiesTaxes(dutiesTaxes);
                        tranxPurOrderDutiesTaxes1.setAmount(Double.parseDouble(cgstObject.get("amt").getAsString()));
                        tranxPurOrderDutiesTaxes1.setSundryCreditors(mPurchaseTranx.getSundryCreditors());
                        tranxPurOrderDutiesTaxes1.setTranxPurOrder(mPurchaseTranx);
                        tranxPurOrderDutiesTaxes1.setIntra(taxFlag);
                        tranxPurOrderDutiesTaxes1.setStatus(true);
                        tranxPurOrderDutiesTaxes1.setCreatedBy(mPurchaseTranx.getCreatedBy());
                        tranxPurOrderDutiesTaxes.add(tranxPurOrderDutiesTaxes1);
                    }

                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (JsonElement mList : sgstList) {


                    //TranxPurOrderDutiesTaxes taxes = new TranxPurOrderDutiesTaxes();
                    JsonObject sgstObject = mList.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) sgstObject.get("gst").getAsDouble();
              /*      int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String ledgerName = "INPUT SGST " + inputGst;*/
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT SGST " + inputGst;
                /*    dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCase(
                            mPurchaseTranx.getOutlet().getId(), ledgerName);*/
                    if (mPurchaseTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPurchaseTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxPurOrderDutiesTaxes taxes = null;

                        taxes = new TranxPurOrderDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(sgstObject.get("amt").getAsString()));
                        taxes.setTranxPurOrder(mPurchaseTranx);
                        taxes.setSundryCreditors(mPurchaseTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        taxes.setCreatedBy(mPurchaseTranx.getCreatedBy());
                        tranxPurOrderDutiesTaxes.add(taxes);
                    }

                }
            }
        } else {
            JsonArray igstList = duties_taxes.getAsJsonArray("igst");
            /* this is for Igst creation */
            if (igstList.size() > 0) {
                for (JsonElement mList : igstList) {

                    JsonObject igstObject = mList.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //    int inputGst = (int) igstObject.get("gst").getAsDouble();
                    //int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String inputGst = igstObject.get("gst").getAsString();
                    String ledgerName = "INPUT IGST " + inputGst;
                   /* dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCase(
                            mPurchaseTranx.getOutlet().getId(), ledgerName);*/
                    if (mPurchaseTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPurchaseTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxPurOrderDutiesTaxes taxes = null;

                        taxes = new TranxPurOrderDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(igstObject.get("amt").getAsString()));
                        taxes.setTranxPurOrder(mPurchaseTranx);
                        taxes.setSundryCreditors(mPurchaseTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        taxes.setCreatedBy(mPurchaseTranx.getCreatedBy());
                        tranxPurOrderDutiesTaxes.add(taxes);
                    }

                }
            }
        }
        try {
            /* save all Duties and Taxes into purchase Invoice Duties taxes table */
            tranxPurOrderDutiesTaxesRepository.saveAll(tranxPurOrderDutiesTaxes);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            purOrderLogger.error("Error in saveIntoPOInvoiceDutiesTaxes" + e1.getMessage());
            System.out.println(e1.getMessage());
        } catch (Exception e) {
            purOrderLogger.error("Error in saveIntoPOInvoiceDutiesTaxes" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    /* End of Purchase Duties and Taxes Ledger */


    /****** save into Purchase Invoice Details ******/
    public void saveIntoPOInvoiceDetails(JsonArray array, TranxPurOrder mTranxPurOrder,
                                         Branch branch, Outlet outlet,
                                         Long userId, TransactionTypeMaster tranxType, String key, String rowsDeleted) {
        for (JsonElement mList : array) {
            JsonObject object = mList.getAsJsonObject();
            /* Purchase Invoice Unit Edit */
            Long details_id = object.get("details_id").getAsLong();
            TranxPurOrderDetailsUnits invoiceUnits = new TranxPurOrderDetailsUnits();
            if (details_id != 0) {
                invoiceUnits = tranxPurOrderDetailsUnitRepository.findByIdAndStatus(details_id, true);
            } else {
                invoiceUnits.setTransactionStatus(1L);
            }
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);

            String batchNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
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
            invoiceUnits.setTranxPurOrder(mTranxPurOrder);
            invoiceUnits.setProduct(mProduct);
            invoiceUnits.setUnits(units);
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            if (!object.get("free_qty").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setFreeQty(object.get("free_qty").getAsDouble());
            invoiceUnits.setRate(object.get("rate").getAsDouble());

            if (levelA != null) invoiceUnits.setLevelA(levelA);
            if (levelB != null) invoiceUnits.setLevelB(levelB);
            if (levelC != null) invoiceUnits.setLevelC(levelC);

            invoiceUnits.setStatus(true);
            if (object.has("base_amt"))
                invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv"))
                invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            invoiceUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            invoiceUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            invoiceUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            invoiceUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
            invoiceUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
            invoiceUnits.setGrossAmt1(object.get("gross_amt1").getAsDouble());
            invoiceUnits.setInvoiceDisAmt(object.get("invoice_dis_amt").getAsDouble());
            invoiceUnits.setDiscountPerCal(object.get("dis_per_cal").getAsDouble());
            invoiceUnits.setDiscountAmountCal(object.get("dis_amt_cal").getAsDouble());
            invoiceUnits.setTotalAmount(object.get("total_amt").getAsDouble());
            invoiceUnits.setIgst(object.get("igst").getAsDouble());
            invoiceUnits.setSgst(object.get("sgst").getAsDouble());
            invoiceUnits.setCgst(object.get("cgst").getAsDouble());
            invoiceUnits.setTotalIgst(object.get("total_igst").getAsDouble());
            invoiceUnits.setTotalSgst(object.get("total_sgst").getAsDouble());
            invoiceUnits.setTotalCgst(object.get("total_cgst").getAsDouble());
            invoiceUnits.setFinalAmount(object.get("final_amt").getAsDouble());
            try {
                mProduct.setIsDelete(false);
                productRepository.save(mProduct);
            } catch (Exception e) {

            }
            /******* Insert into Product Batch No ****/
            try {
                tranxPurOrderDetailsUnitRepository.save(invoiceUnits);
                /**
                 * @implNote validation of Product Delete , if any tranx done for this product, user cant delete this product **
                 * @auther ashwins@opethic.com
                 * @version sprint 21
                 **/
                if (mProduct != null && mProduct.getIsDelete()) {
                    mProduct.setIsDelete(false);
                    productRepository.save(mProduct);
                }

            } catch (Exception e) {
                purOrderLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            /* end of Purchase Invoice Units Edit */

        }
        /* if product is deleted from details table from front end, while user edit the purchase */
        Long purchaseInvoiceId = null;
        HashSet<Long> purchaseDetailsId = new HashSet<>();
        JsonParser parser = new JsonParser();
        JsonElement purDetailsJson;
        if (!rowsDeleted.equalsIgnoreCase("")) {
            purDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = purDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxPurOrderDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    if (deletedArrays.size() > 0) {
                        if (deletedRowsId.has("del_id")) {
                            mDeletedInvoices = tranxPurOrderDetailsUnitRepository.findByIdAndStatus(
                                    deletedRowsId.get("del_id").getAsLong(), true);
                            if (mDeletedInvoices != null) {
                                mDeletedInvoices.setStatus(false);
                                try {
                                    tranxPurOrderDetailsUnitRepository.save(mDeletedInvoices);
                                } catch (DataIntegrityViolationException de) {
                                    purOrderLogger.error("Error in saveInto Purchase Invoice Details Edit" + de.getMessage());
                                    de.printStackTrace();
                                    System.out.println("Exception:" + de.getMessage());
                                } catch (Exception ex) {
                                    purOrderLogger.error("Error in saveInto Purchase Invoice Details Edit" + ex.getMessage());
                                    ex.printStackTrace();
                                    System.out.println("Exception save Into Purchase Invoice Details Edit:" + ex.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //
    /* List of Purchase orders :outlet wise */
    public JsonObject poInvoiceList(HttpServletRequest request) {

        JsonArray result = new JsonArray();
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        List<TranxPurOrder> tranxPurOrders = new ArrayList<>();
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
                tranxPurOrders = tranxPurOrderRepository.findPurchaseOrderListWithDate(users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, true);
            } else {
                tranxPurOrders = tranxPurOrderRepository.findPurchaseOrderListWithDateNoBr(users.getOutlet().getId(), startDatep, endDatep, true);
            }
        } else {
            if (users.getBranch() != null) {
                tranxPurOrders = tranxPurOrderRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                tranxPurOrders = tranxPurOrderRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
            }
        }

        for (TranxPurOrder invoices : tranxPurOrders) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getVendorInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(
                    invoices.getInvoiceDate()).toString());
            response.addProperty("transaction_date", invoices.getTransactionDate().toString());
            //response.addProperty("order_serial_number", invoices.getTranxPurchaseOrderProductSrNumbers().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_creditor_name", invoices.getSundryCreditors().getLedgerName());
            response.addProperty("sundry_creditor_id", invoices.getSundryCreditors().getId());
            response.addProperty("supplier_code", invoices.getSundryCreditors().getLedgerCode());
            response.addProperty("narration", invoices.getNarration());
            response.addProperty("purchase_order_status", invoices.getTransactionStatus().getStatusName());
            response.addProperty("purchase_account_name", invoices.getPurchaseAccountLedger().getLedgerName());
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTotalBaseAmount());
            // response.put("purchase_account_name", invoices.getPurchaseAccountLedger().getLedgerName());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //Start of purchase order list with pagination
    public Object poInvoiceList(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
//        System.out.println("request " + request + "  req=" + req);
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        Boolean flag = false;
        System.out.println("startdate " + startDatep + "  endDate " + endDatep);
        List<TranxPurOrder> purchaseOrder = new ArrayList<>();
        List<TranxPurOrder> purchaseArrayList = new ArrayList<>();
        List<PurOrderDTO> purOrderDTOList = new ArrayList<>();
        Map<String, String[]> paramMap = req.getParameterMap();
        String jsonToStr = "";
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_purchase_order_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += " AND DATE(invoice_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND narration LIKE '%" + searchText + "%'";
            }
            if (paramMap.containsKey("sort"))

                jsonToStr = request.get("sort");


            if (!jsonToStr.isEmpty()) {
                JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
                if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") &&
                        jsonObject.get("colId").getAsString() != null) {
                    String sortBy = jsonObject.get("colId").getAsString();
                    query = query + " ORDER BY " + sortBy;
                    if (jsonObject.get("isAsc").getAsBoolean() == true) {
                        query = query + " ASC";
                    } else {
                        query = query + " DESC";
                    }
                }
            } else {
                query = query + " ORDER BY id DESC";
            }
            String query1 = query;       //we get all lists in this list
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;
            Query q = entityManager.createNativeQuery(query, TranxPurOrder.class);
            System.out.println("q ==" + q + "  purchaseOrder " + purchaseOrder);
            purchaseOrder = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxPurOrder.class);

            purchaseArrayList = q1.getResultList();
            Integer total_pages = (purchaseArrayList.size() / pageSize);
            if ((purchaseArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            for (TranxPurOrder invoiceListView : purchaseOrder) {
                purOrderDTOList.add(convertToDTDTO(invoiceListView));
            }

            List<TranxPurOrder> salesInList = new ArrayList<>();
            salesInList = q1.getResultList();
            System.out.println("total rows " + salesInList.size());
            GenericDatatable<PurOrderDTO> data = new GenericDatatable<>(purOrderDTOList, purchaseArrayList.size(),
                    pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            genericDTData.setRows(purOrderDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }
    //End of purchase order list with pagination

    //Start of DTO for purchase order
    private PurOrderDTO convertToDTDTO(TranxPurOrder purOrder) {
        PurOrderDTO purOrderDTO = new PurOrderDTO();
        purOrderDTO.setId(purOrder.getId());
        purOrderDTO.setInvoice_no(purOrder.getVendorInvoiceNo());
        purOrderDTO.setInvoice_date(DateConvertUtil.convertDateToLocalDate(purOrder.getInvoiceDate()).toString());
        purOrderDTO.setTransaction_date(purOrder.getTransactionDate().toString());
        purOrderDTO.setTotal_amount(purOrder.getTotalAmount());
        purOrderDTO.setSundry_creditor_name(purOrder.getSundryCreditors().getLedgerName());
        purOrderDTO.setSundry_creditor_id(purOrder.getSundryCreditors().getId());
        purOrderDTO.setSupplier_code(purOrder.getSundryCreditors().getLedgerCode());
        purOrderDTO.setNarration(purOrder.getNarration());
        purOrderDTO.setTotaligst(purOrder.getTotaligst());
        purOrderDTO.setTotaligst(purOrder.getTotalcgst());
        purOrderDTO.setTotaligst(purOrder.getTotalsgst());
        purOrderDTO.setPurchase_order_status(purOrder.getTransactionStatus().getStatusName());
        purOrderDTO.setPurchase_account_name(purOrder.getPurchaseAccountLedger().getLedgerName());
        purOrderDTO.setTax_amt(purOrder.getTotalTax() != null ? purOrder.getTotalTax() : 0.0);
        purOrderDTO.setTaxable_amt(purOrder.getTotalBaseAmount());
        purOrderDTO.setTransactionTrackingNo(purOrder.getTransactionTrackingNo());
        purOrderDTO.setTranxCode(purOrder.getTranxCode());
        purOrderDTO.setOrderStatus(purOrder.getOrderStatus());
        return purOrderDTO;

    }
    //End of DTO for purchase order

    public JsonObject poLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxPurOrderRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxPurOrderRepository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
      /*  String companyName = users.getOutlet().getCompanyName();
        companyName = companyName.substring(0, 3);*/ // fetching first 3 digits from company names
        /* getting Start and End year from fiscal Year */
 /*       String startYear = generateFiscalYear.getStartYear();
        String endYear = generateFiscalYear.getEndYear();*/
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        /*String poCode = companyName.toUpperCase() + "-" + startYear + endYear
                + "-" + "PO" + currentMonth + "-" + serailNo;*/
        String poCode = "PO" + currentMonth + serailNo;

        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("serialNo", poCode);
        return result;
    }

    public Object getAllPo() {
        List<TranxPurOrder> list = tranxPurOrderRepository.findAllByStatus(true);
        return list;
    }

    public JsonObject purchaseOrderDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxPurOrder invoiceTranx = tranxPurOrderRepository.findByIdAndStatusAndTransactionStatusId(
                Long.parseLong(request.getParameter("id")), true, 1L);
        try {
            if (invoiceTranx != null) {
                invoiceTranx.setStatus(false);
                invoiceTranx.setOperations("deletion");
                tranxPurOrderRepository.save(invoiceTranx);
                jsonObject.addProperty("message", "Purchase order deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Cant delete closed purchase order");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }
        } catch (Exception e) {
            purOrderLogger.error("Error in purchaseDelete()->" + e.getMessage());
        }
        return jsonObject;
    }


    public JsonObject getPOInvoiceWithIds(HttpServletRequest request) {
        JsonObject output = new JsonObject();
        String str = request.getParameter("purchase_order_id");
        JsonParser parser = new JsonParser();
        JsonElement purDetailsJson = parser.parse(str);
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSORD");
        JsonArray jsonArray = purDetailsJson.getAsJsonArray();
        JsonArray row = new JsonArray();
        JsonObject invoiceData = new JsonObject();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            /* getting Units of Purchase Orders */
            JsonArray unitsJsonArray = new JsonArray();
            List<TranxPurOrderDetailsUnits> unitsArray =
                    tranxPurOrderDetailsUnitRepository.findByTranxPurOrderIdAndTransactionStatusAndStatus(object.get("id").getAsLong(), 1L, true);
            for (TranxPurOrderDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("is_batch", mUnits.getProduct().getIsBatchNumber());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ?
                        mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ?
                        mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ?
                        mUnits.getLevelC().getId().toString() : "");
                unitsJsonObjects.addProperty("pack_name", mUnits.getProduct() != null ?
                        (mUnits.getProduct().getPackingMaster() != null ?
                                mUnits.getProduct().getPackingMaster().getPackName() : "") : "");
                unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                unitsJsonObjects.addProperty("qty", mUnits.getQty());
                unitsJsonObjects.addProperty("rate", mUnits.getRate());
                unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ?
                        mUnits.getProduct().getPackingMaster().getPackName() : "");
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
                unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty() != null ? mUnits.getFreeQty() : 0.0);
                unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer());
                unitsJsonObjects.addProperty("row_dis_amt", mUnits.getTotalDiscountInAmt());
                unitsJsonObjects.addProperty("gross_amt", mUnits.getGrossAmt());
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1());
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt());
                unitsJsonObjects.addProperty("reference_id", mUnits.getTranxPurOrder().getId());
                unitsJsonObjects.addProperty("reference_type", tranxType.getTransactionCode());
                unitsJsonObjects.addProperty("b_detailsId", "");
                row.add(unitsJsonObjects);
                invoiceData.addProperty("id", mUnits.getTranxPurOrder().getId());
                invoiceData.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(
                        mUnits.getTranxPurOrder().getInvoiceDate()).toString());
                invoiceData.addProperty("purchase_order", mUnits.getTranxPurOrder().getVendorInvoiceNo());
                invoiceData.addProperty("purchase_id", mUnits.getTranxPurOrder().getPurchaseAccountLedger().getId());
                invoiceData.addProperty("purchase_name", mUnits.getTranxPurOrder().getPurchaseAccountLedger().getLedgerName());
                invoiceData.addProperty("po_sr_no", mUnits.getTranxPurOrder().getId());
                invoiceData.addProperty("pi_transaction_dt", mUnits.getTranxPurOrder().getTransactionDate().toString());
                invoiceData.addProperty("transport_name", mUnits.getTranxPurOrder().getTransportName());
                invoiceData.addProperty("reference", mUnits.getTranxPurOrder().getReference());
                invoiceData.addProperty("supplier_id", mUnits.getTranxPurOrder().getSundryCreditors().getId());
                invoiceData.addProperty("supplier_name", mUnits.getTranxPurOrder().getSundryCreditors().getLedgerName());
                invoiceData.addProperty("narration", mUnits.getTranxPurOrder().getNarration());
                invoiceData.addProperty("gstNo", mUnits.getTranxPurOrder().getGstNumber());
            }
        }
        JsonArray jsonAdditionalList = new JsonArray();
        output.addProperty("discountLedgerId", 0);
        output.addProperty("discountInAmt", 0);
        output.addProperty("discountInPer", 0);
        output.addProperty("totalPurchaseDiscountAmt", 0);
        output.add("additional_charges", jsonAdditionalList);
        output.addProperty("reference_type", tranxType.getTransactionCode());
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("invoice_data", invoiceData);
        output.add("row", row);
        return output;
    }

    public JsonObject poPendingOrder(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        LedgerMaster sundryCreditors = ledgerMasterRepository.findByIdAndStatus(
                Long.parseLong(request.getParameter("supplier_code_id")),
                true);
        List<TranxPurOrder> tranxPurOrders = tranxPurOrderRepository.findBySundryCreditorsIdAndStatusAndTransactionStatusId(sundryCreditors.getId(), true, 1L);
        for (TranxPurOrder invoices : tranxPurOrders) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getVendorInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(invoices.getInvoiceDate()).toString());
            response.addProperty("transaction_date", invoices.getTransactionDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_creditor_name", invoices.getSundryCreditors().getLedgerName());
            response.addProperty("sundry_creditor_id", invoices.getSundryCreditors().getId());
            response.addProperty("supplier_code", invoices.getSundryCreditors().getLedgerCode());
            response.addProperty("narration", invoices.getNarration());
            response.addProperty("purchase_order_status", invoices.getTransactionStatus().getStatusName());
            response.addProperty("purchase_account_name", invoices.getPurchaseAccountLedger().getLedgerName());
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTotalBaseAmount());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public JsonObject getPurchaseOrderById(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        List<TranxPurOrderDetails> list = new ArrayList<>();
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxPurOrder purchaseInvoice = tranxPurOrderRepository.findByIdAndOutletIdAndStatus(
                    id, users.getOutlet().getId(), true);
            list = tranxPurOrderDetailsRepository.findByTranxPurOrderIdAndStatus(id, true);
            finalResult.addProperty("narration", purchaseInvoice.getNarration() != null ? purchaseInvoice.getNarration() : "");
            JsonObject result = new JsonObject();
            /* Purchase Order Data */
            result.addProperty("id", purchaseInvoice.getId());
            result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getInvoiceDate()).toString());
            result.addProperty("purchase_order", purchaseInvoice.getVendorInvoiceNo());
            result.addProperty("purchase_id", purchaseInvoice.getPurchaseAccountLedger().getId());
            result.addProperty("po_sr_no", purchaseInvoice.getId());
            result.addProperty("po_date", purchaseInvoice.getTransactionDate().toString());
            result.addProperty("transport_name", purchaseInvoice.getTransportName());
            result.addProperty("reference", purchaseInvoice.getReference());
            result.addProperty("supplier_id", purchaseInvoice.getSundryCreditors().getId());
            result.addProperty("supplier_name", purchaseInvoice.getSundryCreditors().getLedgerName());
            result.addProperty("narration", purchaseInvoice.getNarration());
            /* End of Purchase Order Data */

            /* Purchase ORDER Details */
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxPurOrderDetails mDetails : list) {
                    JsonObject prDetails = new JsonObject();
                    prDetails.addProperty("details_id", mDetails.getId());
                    prDetails.addProperty("product_id", mDetails.getProduct().getId());
                    /* getting Units of Purchase Orders */
                    List<TranxPurOrderDetailsUnits> unitDetails = tranxPurOrderDetailsUnitRepository.
                            findByTranxPurOrderDetailsIdAndStatus(
                                    mDetails.getId(), true);
                    JsonArray productDetails = new JsonArray();
                    unitDetails.forEach(mUnit -> {
                        JsonObject mObject = new JsonObject();
                        JsonObject mUnitsObj = new JsonObject();
                        if (mUnit.getPackingMaster() != null) {
                            mObject.addProperty("packageId", mUnit.getPackingMaster().getId());
                            mObject.addProperty("pack_name", mUnit.getPackingMaster().getPackName());
                        } else {
                            mObject.addProperty("packageId", "");
                            mObject.addProperty("pack_name", "");
                        }
                        if (mUnit.getFlavourMaster() != null) {
                            mObject.addProperty("flavourId", mUnit.getFlavourMaster().getId());
                            mObject.addProperty("flavour_name", mUnit.getFlavourMaster().getFlavourName());
                        } else {
                            mObject.addProperty("flavourId", "");
                            mObject.addProperty("flavour_name", "");
                        }
                        mObject.addProperty("details_id", mUnit.getId());
                        mObject.addProperty("unit_conv", mUnit.getUnitConversions());

                        mObject.addProperty("unitId", mUnit.getUnits().getId());
                        mObject.addProperty("unit_name", mUnit.getUnits().getUnitName());
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
            } /* End of Purchase Order Details */
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);

        } catch (DataIntegrityViolationException e) {
            //e1.printStackTrace();
            purOrderLogger.error("Error in getPurchaseOrderById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            //e1.printStackTrace();
            purOrderLogger.error("Error in getPurchaseOrderById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public Object editPOInvoice(HttpServletRequest request) {
        TranxPurOrder mPurchaseTranx = null;
        ResponseMessage responseMessage = new ResponseMessage();
        mPurchaseTranx = saveIntoPOEdit(request);
        if (mPurchaseTranx != null) {
            //insertIntoLedgerTranxDetails(mPurchaseTranx);
            responseMessage.setMessage("Purchase order updated successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Error in purchase order updatation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    public TranxPurOrder saveIntoPOEdit(HttpServletRequest request) {
        TranxPurOrder mPurchaseTranx = null;
        TransactionTypeMaster tranxType = null;
        LedgerMaster purchaseAccount = null;
        LedgerMaster sundryCreditors = null;
        LedgerMaster roundoff = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        TranxPurOrder tranxPurOrder = new TranxPurOrder();
        tranxPurOrder = tranxPurOrderRepository.findByIdAndOutletIdAndStatus(Long.parseLong(
                request.getParameter("id")), users.getOutlet().getId(), true);
        if (users.getBranch() != null) {
            branch = users.getBranch();
            tranxPurOrder.setBranch(branch);
        }
        tranxPurOrder.setOutlet(outlet);
        //tranxType = tranxRepository.findByTransactionNameIgnoreCase("purchase order");
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSORD");
//        LocalDate date = LocalDate.parse(request.getParameter("invoice_date"));

        LocalDate invoiceDate = DateConvertUtil.convertStringToLocalDate(request.getParameter("invoice_date"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("invoice_date"));
        tranxPurOrder.setInvoiceDate(strDt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(invoiceDate);
        if (fiscalYear != null) {
            tranxPurOrder.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        tranxPurOrder.setVendorInvoiceNo(request.getParameter("invoice_no"));
        purchaseAccount = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("purchase_id")), users.getOutlet().getId(), true);
        sundryCreditors = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), users.getOutlet().getId(), true);

        tranxPurOrder.setPurchaseAccountLedger(purchaseAccount);
        tranxPurOrder.setSundryCreditors(sundryCreditors);
        LocalDate mDate = LocalDate.parse(request.getParameter("transaction_date"));
        tranxPurOrder.setTransactionDate(mDate);

        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                tranxPurOrder.setGstNumber(request.getParameter("gstNo"));
            }
        }
        tranxPurOrder.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        tranxPurOrder.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        //   roundoff = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId("Round off", users.getOutlet().getId());
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(
                    users.getOutlet().getId(), "Round off");
        tranxPurOrder.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        tranxPurOrder.setPurchaseRoundOff(roundoff);
        tranxPurOrder.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
        if (taxFlag) {
            tranxPurOrder.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            tranxPurOrder.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            tranxPurOrder.setTotaligst(0.0);
        }
        /* if false : igst i.e inter state */
        else {
            tranxPurOrder.setTotalcgst(0.0);
            tranxPurOrder.setTotalsgst(0.0);
            tranxPurOrder.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }

        tranxPurOrder.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        tranxPurOrder.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        if (paramMap.containsKey("tcs")) tranxPurOrder.setTcs(Double.parseDouble(request.getParameter("tcs")));
        tranxPurOrder.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        tranxPurOrder.setPurchaseDiscountPer(Double.parseDouble(request.getParameter("purchase_discount")));
        tranxPurOrder.setPurchaseDiscountAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
        tranxPurOrder.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        tranxPurOrder.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        tranxPurOrder.setPurOrdSrno(Long.parseLong(request.getParameter("purchase_sr_no")));
        tranxPurOrder.setUpdatedBy(users.getId());
        tranxPurOrder.setStatus(true);
        tranxPurOrder.setNarration(request.getParameter("narration"));
        try {
            mPurchaseTranx = tranxPurOrderRepository.save(tranxPurOrder);
            if (mPurchaseTranx != null) {
                /* Save into Duties and Taxes */
                String taxStr = request.getParameter("taxCalculation");
                // JsonObject duties_taxes = new JsonObject(taxStr);
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoPurOrderDutiesTaxesEdit(duties_taxes, mPurchaseTranx, taxFlag, tranxType, users.getOutlet().getId(), users.getId());
                /* save into Additional Charges  */
                String jsonStr = request.getParameter("row");
                JsonParser parser = new JsonParser();
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) {
                    rowsDeleted = request.getParameter("rowDelDetailsIds");
                }
                saveIntoPOInvoiceDetails(array, mPurchaseTranx,
                        branch, outlet, users.getId(), tranxType, "update", rowsDeleted);
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purOrderLogger.error("Error in saveIntoPOEdit" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            System.out.println("Exception: saveIntoPOEdit" + e1.getMessage());
        }
        return mPurchaseTranx;
    }

    public void saveIntoPurOrderDutiesTaxesEdit(JsonObject duties_taxes, TranxPurOrder invoiceTranx, Boolean taxFlag,
                                                TransactionTypeMaster tranxType, Long outletId, Long userId) {
        /* Purchase Duties and Taxes */
        List<TranxPurOrderDutiesTaxes> purchaseDutiesTaxes = new ArrayList<>();
        /* getting duties_taxes_ledger_id  */
        List<Long> db_dutiesLedgerIds = tranxPurOrderDutiesTaxesRepository.findByDutiesAndTaxesId(invoiceTranx.getId());
        // List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId);
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null);

        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
//                System.out.println("travelArray"+travelArray);
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            //Updation into Purchase Duties and Taxes
            if (db_dutiesLedgerIds.size() > 0) {
                //insert old records in history
                purchaseDutiesTaxes = tranxPurOrderDutiesTaxesRepository.findByTranxPurOrderIdAndStatus(invoiceTranx.getId(), true);
                // insertIntoDutiesAndTaxesHistory(purchaseDutiesTaxes);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxPurOrderDutiesTaxes taxes = new TranxPurOrderDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                    /*    int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String ledgerName = "INPUT CGST " + inputGst;*/
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;
                        //dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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
                        }
                        taxes.setAmount(amt);
                        taxes.setStatus(true);
                        taxes.setTranxPurOrder(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxPurOrderDutiesTaxes taxes = new TranxPurOrderDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        //dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());

                        }
                        taxes.setAmount(amt);
                        taxes.setTranxPurOrder(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxPurOrderDutiesTaxes taxes = new TranxPurOrderDutiesTaxes();
                        JsonObject igstObject = mIgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        //       dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());

                        }
                        taxes.setAmount(amt);
                        taxes.setTranxPurOrder(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
            }
        } else {
            //Insertion into Purchase Duties and Taxes
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxPurOrderDutiesTaxes taxes = new TranxPurOrderDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //     int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;
                        Double amt = cgstObject.get("amt").getAsDouble();
                        // dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxPurOrder(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxPurOrderDutiesTaxes taxes = new TranxPurOrderDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //   int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        //  dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxPurOrder(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxPurOrderDutiesTaxes taxes = new TranxPurOrderDutiesTaxes();
                        JsonObject igstObject = igstList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        //dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxPurOrder(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
            }
        }
        tranxPurOrderDutiesTaxesRepository.saveAll(purchaseDutiesTaxes);
    }

    private void insertIntoDutiesAndTaxesHistory(List<TranxPurOrderDutiesTaxes> purchaseDutiesTaxes) {
        for (TranxPurOrderDutiesTaxes mList : purchaseDutiesTaxes) {
            mList.setStatus(false);
            tranxPurOrderDutiesTaxesRepository.save(mList);
        }
    }

    private List<Long> getInputLedgerIds(Boolean taxFlag, JsonObject duties_taxes, Long outletId, Long branchId) {
        List<Long> returnLedgerIds = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (JsonElement mCgst : cgstList) {
                    TranxPurOrderDutiesTaxes taxes = new TranxPurOrderDutiesTaxes();
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
                    TranxPurOrderDutiesTaxes taxes = new TranxPurOrderDutiesTaxes();
                    JsonObject sgstObject = mSgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //    int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT SGST " + inputGst;
                    //     dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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
                    //    int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String inputGst = igstObject.get("gst").getAsString();
                    String ledgerName = "INPUT IGST " + inputGst;
                    // dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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

    public JsonObject getPOInvoiceIds(HttpServletRequest request) {
        JsonArray array = new JsonArray();
        JsonObject result = new JsonObject();
        try {
            String json = request.getParameter("ids");
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(json);
            array = tradeElement.getAsJsonArray();
            for (JsonElement mList : array) {
                Long id = mList.getAsLong();
                TranxPurOrder mOrder = tranxPurOrderRepository.findByIdAndStatus(id, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // e.printStackTrace();
            purOrderLogger.error("Error in getPOInvoiceIds" + e.getMessage());
            System.out.println("Error:" + e.getMessage());
        }
        return result;
    }

    public JsonObject getProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxPurOrder invoiceTranx = tranxPurOrderRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        productIds = tranxPurOrderDetailsUnitRepository.findByTranxPurId(invoiceTranx.getId(), true);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getInvoiceDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getProductEditByIdsByFPU(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String str = request.getParameter("purchase_order_id");
        JsonParser parser = new JsonParser();
        JsonElement purDetailsJson = parser.parse(str);
        JsonArray jsonArray = purDetailsJson.getAsJsonArray();
        JsonArray productArray = new JsonArray();
        JsonObject output = new JsonObject();
        JsonObject result = new JsonObject();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            TranxPurOrder invoiceTranx = tranxPurOrderRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
            List<Object[]> productIds = new ArrayList<>();
            productIds = tranxPurOrderDetailsUnitRepository.findByTranxPurId(invoiceTranx.getId(), true);
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
                        List<Long> levelCunits = productUnitRepository.findByProductsLevelC(
                                mProduct.getId(), mLeveA, mLeveB);
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
                            List<Object[]> unitList = productUnitRepository.
                                    findUniqueUnitsByProductId(mProduct.getId(), mLeveA,
                                            mLeveB, mLeveC);
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
                                    List<ProductBatchNo> batchNos = productBatchNoRepository.findByUniqueBatchProductIdAndStatus(mProduct.getId(),
                                            levelAId, levelBId, levelCId, unitId, true);
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
                                            batchObject.addProperty("costing_with_tax", mBatch.getCostingWithTax() != null ?
                                                    mBatch.getCostingWithTax() : 0.00);
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
                                                salesRateWithTax = mBatch.getSalesRate() +
                                                        ((mBatch.getSalesRate() * mBatch.getProduct().getTaxMaster().getIgst()) / 100);
                                            batchObject.addProperty("sales_rate_with_tax", salesRateWithTax);
                                            if (mBatch.getExpiryDate() != null) {

                                                LocalDate invoiceDt = DateConvertUtil.convertDateToLocalDate(invoiceTranx.getInvoiceDate());
                                                if (invoiceDt.isAfter(mBatch.getExpiryDate())) {
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
        //  productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getInvoiceDate(), productIds);
        // result.add("invoice_list", productArray);
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);

        return output;
    }

    public JsonObject getPurchaseOrderByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        JsonObject finalResult = new JsonObject();
        JsonObject result = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            System.out.println("PO EDIT ID => "+id);
            TranxPurOrder purchaseInvoice = tranxPurOrderRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            if (purchaseInvoice != null) {
                finalResult.addProperty("tcs", purchaseInvoice.getTcs());
                finalResult.addProperty("narration", purchaseInvoice.getNarration() != null ? purchaseInvoice.getNarration() : "");
                //  finalResult.addProperty("discountLedgerId", purchaseInvoice.getPurchaseDiscountLedger() != null ? purchaseInvoice.getPurchaseDiscountLedger().getId() : 0);
                finalResult.addProperty("discountInAmt", purchaseInvoice.getPurchaseDiscountAmount());
                finalResult.addProperty("discountInPer", purchaseInvoice.getPurchaseDiscountPer());
                finalResult.addProperty("totalPurchaseDiscountAmt", purchaseInvoice.getTotalPurchaseDiscountAmt());
                finalResult.addProperty("totalQty", purchaseInvoice.getTotalqty() != null ? purchaseInvoice.getTotalqty() : 0);
                finalResult.addProperty("totalFreeQty", purchaseInvoice.getFreeQty() != null ? purchaseInvoice.getFreeQty() : 0);
                finalResult.addProperty("grossTotal", purchaseInvoice.getGrossAmount() != null ? purchaseInvoice.getGrossAmount() : 0);
                finalResult.addProperty("totalTax", purchaseInvoice.getTotalTax() != null ? purchaseInvoice.getTotalTax() : 0);
                /* Purchase Invoice Data */
                result.addProperty("id", purchaseInvoice.getId());
                result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getInvoiceDate()).toString());
                result.addProperty("invoice_no", purchaseInvoice.getVendorInvoiceNo());
                result.addProperty("tranx_unique_code", purchaseInvoice.getTranxCode());
                result.addProperty("po_sr_no", purchaseInvoice.getId());
                result.addProperty("purchase_account_ledger_id", purchaseInvoice.getPurchaseAccountLedger().getId());
                result.addProperty("supplierId", purchaseInvoice.getSundryCreditors().getId());
                result.addProperty("supplierName", purchaseInvoice.getSundryCreditors().getLedgerName());
                result.addProperty("transaction_dt", purchaseInvoice.getTransactionDate().toString());
                result.addProperty("gstNo", purchaseInvoice.getGstNumber() != null ? purchaseInvoice.getGstNumber() : "");
                result.addProperty("ledgerStateCode", purchaseInvoice.getSundryCreditors().getStateCode());
                /* End of Purchase Invoice Data */
            }

            /* Purchase Invoice Details */
            JsonArray row = new JsonArray();
            JsonArray unitsJsonArray = new JsonArray();
            List<TranxPurOrderDetailsUnits> unitsArray =
                    tranxPurOrderDetailsUnitRepository.findByTranxPurOrderIdAndTransactionStatusAndStatus(
                            purchaseInvoice.getId(), 1L, true);
            for (TranxPurOrderDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ?
                        mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ?
                        mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ?
                        mUnits.getLevelC().getId().toString() : "");
                unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "");
                unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
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
//                unitsJsonObjects.addProperty("add_chg_amt", mUnits.getAdditionChargesAmt() != null ? mUnits.getAdditionChargesAmt() : 0.0);
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1() != null ? mUnits.getGrossAmt1() : 0.0);
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt() != null ? mUnits.getInvoiceDisAmt() : 0.0);

                JsonArray mLevelArray = new JsonArray();
                List<Long> levelaArray = productUnitRepository.findLevelAIdDistinct(mUnits.getProduct().getId());
                for (Long mLeveA : levelaArray) {
                    JsonObject levelaJsonObject = new JsonObject();
                    LevelA levelA = null;
                    if (mLeveA != null) {
                        levelA = levelARepository.findByIdAndStatus(mLeveA, true);
                        if (levelA != null) {
                            levelaJsonObject.addProperty("levela_id", levelA.getId());
                            levelaJsonObject.addProperty("levela_name", levelA.getLevelName());
                        }
                    }
                    mLevelArray.add(levelaJsonObject);
                }
                unitsJsonObjects.add("levelAOpt", mLevelArray);
                row.add(unitsJsonObjects);
            }
            List<LedgerGstDetails> gstDetails = new ArrayList<>();
            gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(purchaseInvoice.getSundryCreditors().getId(), true);
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
            /* End of Purchase Invoice Details */
            System.out.println("Row  " + row);
            finalResult.add("row", row);
            finalResult.add("invoice_data", result);
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purOrderLogger.error("Error in getPurchaseInvoiceById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            purOrderLogger.error("Error in getPurchaseInvoiceById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject getOrderSupplierListByProductId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        Long productId = Long.parseLong(request.getParameter("productId"));

        List<TranxPurOrderDetailsUnits> tranxPurOrderDetailsUnits = tranxPurOrderDetailsUnitRepository.findByProductIdAndStatusOrderByIdDesc(productId, true);

        for (TranxPurOrderDetailsUnits obj : tranxPurOrderDetailsUnits) {
            if (obj.getTranxPurOrder().getStatus()) {
                JsonObject response = new JsonObject();
                response.addProperty("supplier_name", obj.getTranxPurOrder().getSundryCreditors().getLedgerName());
                response.addProperty("invoice_no", obj.getTranxPurOrder().getId());
                response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(
                        obj.getTranxPurOrder().getInvoiceDate()).toString());
//            response.addProperty("batch",obj.getProductBatchNo().getBatchNo());
//            response.addProperty("mrp",obj.getProductBatchNo().getMrp());
                response.addProperty("quantity", obj.getQty());
                response.addProperty("rate", obj.getRate());
//            response.addProperty("cost",obj.getProductBatchNo().getCosting());
                response.addProperty("dis_per", obj.getDiscountPer());
                response.addProperty("dis_amt", obj.getDiscountAmount());
                result.add(response);
            }
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public JsonObject purchasePrintOrder(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurOrderDetailsUnits> list = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        TranxPurOrder purOrder = null;
        String source = request.getParameter("source");
        String key = request.getParameter("print_type"); //check whether printbill is calling from create page or from list page
        /***** if  print_type is create, then use serialnumber of invoice to fetch invoice details ,
         * if print_type is list then use invoice id to fetch invoice details *****/
        try {
            String invoiceNo = request.getParameter("id");
            Long id = 0L;
            if (source.equalsIgnoreCase("purchase_order")) {
                if (users.getBranch() != null) {

                    id = Long.parseLong(invoiceNo);
                    purOrder = tranxPurOrderRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);

                } else {

                    id = Long.parseLong(invoiceNo);
                    purOrder = tranxPurOrderRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);

                }
            } else {
               /* if (users.getBranch() != null) {
                    if (key.equalsIgnoreCase("create")) {
                        counterSales = counterSaleRepository.findByCounterSaleNoAndOutletIdAndBranchIdAndStatus(invoiceNo, users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        counterSales = counterSaleRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    }
                } else {
                    if (key.equalsIgnoreCase("create")) {
                        counterSales = counterSaleRepository.findByCounterSaleNoAndOutletIdAndStatusAndBranchIsNull(invoiceNo, users.getOutlet().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        counterSales = counterSaleRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);
                    }
                }*/
            }
            if (purOrder != null) {
                //   list = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatus(purInvoice.getId(), true);
                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                debtorsObject.addProperty("supplier_name", purOrder.getSundryCreditors().getLedgerName());
                debtorsObject.addProperty("supplier_address", purOrder.getSundryCreditors().getAddress());
                debtorsObject.addProperty("supplier_gstin", purOrder.getSundryCreditors().getGstin());
                debtorsObject.addProperty("supplier_phone", purOrder.getSundryCreditors().getMobile());
                JsonObject invoiceObject = new JsonObject();
                /* Sales Invoice Data */
                invoiceObject.addProperty("id", purOrder.getId());
                invoiceObject.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(
                        purOrder.getInvoiceDate()).toString());
                invoiceObject.addProperty("invoice_no", purOrder.getVendorInvoiceNo());
                invoiceObject.addProperty("state_code", purOrder.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", purOrder.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(purOrder.getTaxableAmount()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(purOrder.getTotaligst()));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(purOrder.getTotalcgst()));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(purOrder.getTotalsgst()));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(purOrder.getTotalBaseAmount()));
                invoiceObject.addProperty("total_discount", numFormat.numFormat(purOrder.getTotalPurchaseDiscountAmt()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(purOrder.getTotalAmount()));
                // invoiceObject.addProperty("advanced_amount", numFormat.numFormat(purInvoice.getAd() != null ? purInvoice.getAdvancedAmount() : 0.0));
                // invoiceObject.addProperty("payment_mode", purInvoice.getPaymentMode());

                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();

                /* getting Units of Sales Quotations*/
                List<TranxPurOrderDetailsUnits> unitDetails = tranxPurOrderDetailsUnitRepository.findByTranxPurOrderIdAndStatus(purOrder.getId(), true);
                JsonArray productDetails = new JsonArray();
                unitDetails.forEach(mUnit -> {
                    JsonObject mObject = new JsonObject();
                    mObject.addProperty("product_id", mUnit.getProduct().getId());
                    mObject.addProperty("product_name", mUnit.getProduct().getProductName());
                    mObject.addProperty("details_id", mUnit.getId());
                    mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                    mObject.addProperty("qty", mUnit.getQty());
                    mObject.addProperty("rate", mUnit.getRate());
                    mObject.addProperty("base_amt", mUnit.getBaseAmt());
                    mObject.addProperty("final_amt", mUnit.getFinalAmount());
                    mObject.addProperty("Gst", mUnit.getIgst());
                    if (mUnit.getProduct().getPackingMaster() != null) {
                        mObject.addProperty("packageId", mUnit.getProduct().getPackingMaster().getId());
                        mObject.addProperty("pack_name", mUnit.getProduct().getPackingMaster().getPackName());
                    } else {
                        mObject.addProperty("packageId", "");
                        mObject.addProperty("pack_name", "");
                    }
                    if (mUnit.getFlavourMaster() != null) {
                        mObject.addProperty("flavourId", mUnit.getFlavourMaster().getId());
                        mObject.addProperty("flavour_name", mUnit.getFlavourMaster().getFlavourName());
                    } else {
                        mObject.addProperty("flavourId", "");
                        mObject.addProperty("flavour_name", "");
                    }
//                    if (mUnit.getProductBatchNo() != null) {
//                        mObject.addProperty("b_details_id", mUnit.getProductBatchNo().getId());
//                        mObject.addProperty("b_no", mUnit.getProductBatchNo().getBatchNo());
//                        mObject.addProperty("is_batch", true);
//                    } else {
                    mObject.addProperty("b_details_id", "");
                    mObject.addProperty("b_no", "");
                    mObject.addProperty("is_batch", false);
//                    }
                    mObject.addProperty("details_id", mUnit.getId());
                    mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                    mObject.addProperty("unitId", mUnit.getUnits().getId());
                    mObject.addProperty("unit_name", mUnit.getUnits().getUnitName());
                    productDetails.add(mObject);
                });
                //  productObject.add(productDetails);
                finalResult.add("product_details", productDetails);
                finalResult.add("supplier_data", companyObject);
                finalResult.add("customer_data", debtorsObject);
                finalResult.add("invoice_data", invoiceObject);
            }
          /*  else {
                listcs = tranxCSDetailsUnitsRepository.findByCounterSalesIdAndStatus(counterSales.getId(), true);
                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                debtorsObject.addProperty("supplier_name", counterSales.getCustomerName());

                JsonObject invoiceObject = new JsonObject();
                *//* Sales Invoice Data *//*
                invoiceObject.addProperty("id", counterSales.getId());
                invoiceObject.addProperty("invoice_dt", counterSales.getCounterSalesDate().toString());
                invoiceObject.addProperty("invoice_no", counterSales.getCounterSaleNo());
                invoiceObject.addProperty("state_code", counterSales.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", counterSales.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(counterSales.getTotalBaseAmt()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(counterSales.getTotaligst()));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(counterSales.getTotalcgst()));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(counterSales.getTotalsgst()));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(counterSales.getTotalBaseAmt()));
                invoiceObject.addProperty("total_discount", numFormat.numFormat(counterSales.getTotalDiscount()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(counterSales.getTotalBill()));
                invoiceObject.addProperty("advanced_amount", numFormat.numFormat(counterSales.getAdvancedAmount() != null ? salesInvoice.getAdvancedAmount() : 0.0));
                invoiceObject.addProperty("payment_mode", counterSales.getPaymentMode());


                *//* End of Sales Invoice Data *//*

             *//* Sales Invoice Details *//*
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();
                JsonArray units = new JsonArray();
                if (list.size() > 0) {

                    for (TranxCounterSalesDetailsUnits mDetails : listcs) {
                        JsonObject prDetails = new JsonObject();
                        prDetails.addProperty("product_id", mDetails.getProduct().getId());
                        prDetails.addProperty("product_name", mDetails.getProduct().getProductName());
                        //   prDetails.addProperty("product_hsn", mDetails.getProduct().getProductHsn().getHsnNumber());
                        //     prDetails.addProperty("Gst", mDetails.getIgst());

                        *//* getting Units of Sales Quotations*//*
                        List<TranxCounterSalesDetailsUnits> unitDetails = tranxCSDetailsUnitsRepository.findByCounterSalesIdAndStatus(mDetails.getId(), true);
                        JsonArray productDetails = new JsonArray();
                        unitDetails.forEach(mUnit -> {
                            JsonObject mObject = new JsonObject();
                            JsonObject mUnitsObj = new JsonObject();
                            mObject.addProperty("details_id", mUnit.getId());
                            mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                            mObject.addProperty("qty", mUnit.getQty());
                            mObject.addProperty("rate", mUnit.getRate());
                            mObject.addProperty("base_amt", mUnit.getBaseAmt());
                            mObject.addProperty("details_id", mUnit.getId());
                            mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                            mObject.addProperty("unitId", mUnit.getUnits().getId());
                            mObject.addProperty("unit_name", mUnit.getUnits().getUnitName());
                            productDetails.add(mObject);
                        });
                        prDetails.add("productDetails", productDetails);
                        row.add(prDetails);
                    }
                }
                productObject.add("product_details", row);
                finalResult.add("supplier_data", companyObject);
                finalResult.add("customer_data", debtorsObject);
                finalResult.add("invoice_data", invoiceObject);
                finalResult.add("invoice_details", productObject);

            }*/

            /* End of Sales Invoice Details */
            /* End of Purchase Additional Charges */
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());

        } catch (DataIntegrityViolationException e) {
            purOrderLogger.error("Error in getInvoiceBillPrint :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            purOrderLogger.error("Error in getInvoiceBillPrint :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject poPendingProductOrder(HttpServletRequest request) {
        JsonObject output = new JsonObject();
        String str = request.getParameter("PendingOrderIdsList");
        JsonParser parser = new JsonParser();
        JsonElement purDetailsJson = parser.parse(str);
        JsonArray poJsonList = purDetailsJson.getAsJsonArray();
        JsonArray unitsJsonArray = new JsonArray();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSORD");
        for (JsonElement mList : poJsonList) {
            Long object = mList.getAsLong();
            /* getting Units of Purchase Orders */
            List<TranxPurOrderDetailsUnits> unitsArray =
                    tranxPurOrderDetailsUnitRepository.findByTranxPurOrderIdAndTransactionStatusAndStatus(object, 1L, true);
            for (TranxPurOrderDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ?
                        mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ?
                        mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ?
                        mUnits.getLevelC().getId().toString() : "");
                unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                unitsJsonObjects.addProperty("qty", mUnits.getQty());
                unitsJsonObjects.addProperty("rate", mUnits.getRate());
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
                unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty());
                unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer());
                unitsJsonObjects.addProperty("row_dis_amt", mUnits.getTotalDiscountInAmt());
                unitsJsonObjects.addProperty("gross_amt", mUnits.getGrossAmt());
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1());
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt());
                unitsJsonObjects.addProperty("reference_id", mUnits.getTranxPurOrder().getId());
                unitsJsonObjects.addProperty("reference_type", tranxType.getTransactionCode());

                unitsJsonObjects.addProperty("b_detailsId", "");
                unitsJsonArray.add(unitsJsonObjects);
            }
        }
        output.add("row", unitsJsonArray);
        output.addProperty("responseStatus", HttpStatus.OK.value());
        return output;
    }
}
