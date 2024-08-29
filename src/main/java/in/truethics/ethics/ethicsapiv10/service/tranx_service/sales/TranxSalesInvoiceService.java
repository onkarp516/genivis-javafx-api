package in.truethics.ethics.ethicsapiv10.service.tranx_service.sales;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.salesdto.SalesCounterDTO;
import in.truethics.ethics.ethicsapiv10.dto.salesdto.SalesInvoiceDTO;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageService;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrder;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CommissionMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerOpeningClosingDetailRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository.TranxJournalDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository.TranxJournalMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurChallanRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.*;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TranxSalesInvoiceService {
    @Autowired
    private TranxSalesInvoiceRepository salesTransactionRepository;
    @Autowired
    private TranxSalesCompInvoiceRepository salesCompTransactionRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TranxSalesInvoiceDutiesTaxesRepository salesDutiesTaxesRepository;
    @Autowired
    private TranxCompSalesInvoiceDutiesTaxesRepository salesCompDutiesTaxesRepository;
    @Autowired
    private TranxSalesInvoiceAdditionalChargesRepository salesAdditionalChargesRepository;
    @Autowired
    private TranxSalesInvoiceDetailsRepository salesInvoiceDetailsRepository;
    @Autowired
    private TranxSalesCompInvoiceDetailsRepository salesCompInvoiceDetailsRepository;
    @Autowired
    private TranxSalesCompInvoiceDetailsUnitRepository tranxSalesCompInvoiceDetailsUnitRepository;
    @Autowired
    private TranxSalesInvoicePrSrNoRepository serialNumberRepository;
    @Autowired
    private TranxSalesCompInvoicePrSrNoRepository serialCompNumberRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private TranxSalesQuotationRepository tranxSalesQuotationRepository;
    @Autowired
    private TranxSalesOrderRepository tranxSalesOrderRepository;
    @Autowired
    private TranxSalesChallanRepository tranxSalesChallanRepository;
    @Autowired
    private TranxCounterSalesRepository counterSaleRepository;
    @Autowired
    private TranxCounterSalesDetailsRepository detailsRepository;
    @Autowired
    private TranxCounterSalesPrSrNoRepository tranxCSPrSrRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private NumFormat numFormat;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private TranxSalesInvoiceDetailsUnitRepository tranxSalesInvoiceDetailsUnitRepository;
    @Autowired
    private TranxSalesCompInvoiceDetailsUnitRepository tranxSalescompInvoiceDetailsUnitRepository;
    @Autowired
    private TranxCSDetailsUnitsRepository tranxCSDetailsUnitsRepository;
    @Autowired
    private TranxCreditNoteNewReferenceRepository tranxCreditNoteNewReferenceRepository;
    @Autowired
    private TranxCreditNoteDetailsRepository tranxCreditNoteDetailsRepository;
    @Autowired
    private TranxReceiptPerticularsRepository tranxReceiptPerticularRepository;
    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    private TranxSalesQuotaionDetailsUnitsRepository tranxSalesQuotaionDetailsUnitsRepository;
    @Autowired
    private TranxSalesOrderDetailsUnitsRepository tranxSalesOrderDetailsUnitsRepository;
    @Autowired
    private TranxSalesChallanDetailsUnitsRepository tranxSalesChallanDetailsUnitsRepository;
    @Autowired
    private ProductData productData;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private TranxSalesReturnDetailsUnitsRepository tranxSalesReturnDetailsUnitsRepository;
    @Autowired
    private TranxSalesReturnDutiesTaxesRepository tranxSalesReturnTaxesRepository;
    @Autowired
    private TranxSalesReturnAddiChargesRepository tranxSalesReturnAddiChargesRepository;
    @Autowired
    private TransSalesPaymentTypeRepository transSalesPaymentTypeRepository;
    @Autowired
    private PostingUtility postingUtility;

    @Autowired
    private ClosingUtility closingUtility;
    @Autowired
    private TranxSalesChallanRepository salesTransactionChallanRepository;
    @Autowired
    private StockTranxDetailsRepository stkTranxDetailsRepository;


    private static final Logger salesInvoiceLogger = LogManager.getLogger(TranxSalesInvoiceService.class);

    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice from DB
    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice from DB
    List<Long> mInputList = new ArrayList<>(); // input all ledgers Id against Purchase invoice from request
    List<Long> ledgerInputList = new ArrayList<>(); // input all ledgers Id against Purchase invoice from request
    @Autowired
    private TranxSalesReturnRepository tranxSalesReturnRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;

    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;
    @Autowired
    private TranxReceiptMasterRepository tranxReceiptMasterRepository;

    @Autowired
    private LedgerMasterRepository ledgerRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Autowired
    private SalesmanMasterRepository salesmanMasterRepository;
    @Autowired
    private ProductOpeningStocksRepository productOpeningStocksRepository;
    @Autowired
    private TranxPaymentPerticularsDetailsRepository tranxPaymentPerticularsDetailsRepository;
    @Autowired
    private TranxReceiptPerticularsDetailsRepository tranxReceiptPerticularsDetailsRepository;
    @Autowired
    private FiscalYearRepository fiscalYearRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Value("${spring.serversource.frurl}")
    private String serverFrUrl;
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private TranxPurChallanRepository tranxPurChallanRepository;
    @Autowired
    private TranxPurOrderRepository tranxPurOrderRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private PatientMasterRepository patientMasterRepository;
    @Autowired
    private PrincipleRepository principleRepository;
    @Autowired
    private PrincipleGroupsRepository principleGroupsRepository;
    @Autowired
    private FoundationRepository foundationRepository;
    @Autowired
    private BalancingMethodRepository balancingMethodRepository;
    @Autowired
    private TranxSalesCompInvoiceRepository compInvoiceRepository;
    @Autowired
    private AreaHeadRepository areaHeadRepository;
    @Autowired
    private AreaheadCommissionRepository areaheadCommissionRepository;
    @Autowired
    private CommissionMasterRepository commissionMasterRepository;
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;
    @Autowired
    private FranchiseMasterRepository franchiseMasterRepository;
    @Autowired
    private TranxJournalMasterRepository tranxJournalMasterRepository;
    @Autowired
    private TranxJournalDetailsRepository tranxJournalDetailsRepository;
    @Autowired
    private UnitConversion unitConversion;


    public Object createTranxSalesCompInvoices(MultipartHttpServletRequest request) throws Exception {
        ResponseMessage responseMessage = new ResponseMessage();
        JsonObject object = new JsonObject();
        TranxSalesCompInvoice mSalesTranx = null;
        TranxSalesCompInvoice salesInvoice = null;
        TransactionTypeMaster tranxType = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String salesType = request.getParameter("sale_type");
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CONS");
        /* save into sales invoices  */
        if (users.getBranch() != null) {
            salesInvoice = salesCompTransactionRepository.findByOutletIdAndBranchIdAndSalesInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("bill_no"));
        } else {
            salesInvoice = salesCompTransactionRepository.findByOutletIdAndSalesInvoiceNoIgnoreCaseAndBranchIdIsNull(users.getOutlet().getId(), request.getParameter("bill_no"));
        }
        //pending
        if (salesInvoice == null) mSalesTranx = saveIntoCompInvoice(users, request, tranxType, salesType);
        if (mSalesTranx != null) {
            /** Accounting Postings  **/
            insertIntoCompTranxDetailSD(mSalesTranx, tranxType, request.getParameter("newReference"), request.getParameter("outstanding_sales_return_amt")); //for sundry Debtors : dr
            insertIntoCompTranxDetailSA(mSalesTranx, tranxType, "CR", "Insert"); // for Sales Accounts : cr
            insertCompDB(mSalesTranx, "AC", tranxType, "CR", "Insert"); // for Additional Charges : cr
            if (users.getOutlet().getGstApplicable())
                insertCompDB(mSalesTranx, "DT", tranxType, "CR", "Insert"); // for Duties and Taxes : cr
            /*** Insert into Day Book : Reporting ***/
            saveCompIntoDayBook(mSalesTranx);
            String paymentMode = request.getParameter("paymentMode");
            String salesOrderId = "";
            if (paramMap.containsKey("reference_so_id")) salesOrderId = request.getParameter("reference_so_id");
            Double paidAmt = 0.0;
            if (paramMap.containsKey("paidAmount")) paidAmt = Double.parseDouble(request.getParameter("paidAmount"));
            JsonArray paymentType = new JsonArray();
            if (paymentMode.equalsIgnoreCase("multi")) {
                String jsonStr = request.getParameter("payment_type");
                paymentType = new JsonParser().parse(jsonStr).getAsJsonArray();
                Double totalPaidAmt = Double.parseDouble(request.getParameter("p_totalAmount"));
                Double returnAmt = Double.parseDouble(request.getParameter("p_returnAmount"));
                Double pendingAmt = Double.parseDouble(request.getParameter("p_pendingAmount"));

                createCompReceiptInvoice(mSalesTranx, users, paymentType, mSalesTranx.getTotalAmount(), returnAmt, paymentMode, "create");
                /*** set the balance amount into sales invoice after receipt voucher generated ****/
                mSalesTranx.setBalance(pendingAmt);
                salesCompTransactionRepository.save(mSalesTranx);
                /****** insert into payment type against sales invoice *****/

                insertIntoCompPaymentType(mSalesTranx, paymentType, paymentMode, "create");
            } else if (paymentMode.equalsIgnoreCase("cash")) {
                createCompReceiptInvoice(mSalesTranx, users, paymentType, mSalesTranx.getTotalAmount(), 0.0, paymentMode, "create");
                /*** set the balance amount into sales invoice after receipt voucher generated ****/
                mSalesTranx.setBalance(0.0);
                salesCompTransactionRepository.save(mSalesTranx);
            }
            /**** Close all Counter sales if any *****/
           /* try {
                String str = request.getParameter("cs_ids");
                JsonParser parser = new JsonParser();
                JsonElement jsonParser = parser.parse(str);
                JsonArray jsonArray = jsonParser.getAsJsonArray();
                for (JsonElement mList : jsonArray) {
                    JsonObject mObject = mList.getAsJsonObject();
                    TranxCounterSales invoiceTranx = counterSaleRepository.findByIdAndStatus(mObject.get("id").getAsLong(), true);
                    TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                    invoiceTranx.setTransactionStatus(transactionStatus.getId());
                    invoiceTranx.setIsBillConverted(true);
                    counterSaleRepository.save(invoiceTranx);
                }

            } catch (Exception e) {
                salesInvoiceLogger.error("Error while Converting Sales Order to Invoice:" + e.getMessage());
            }*/

            responseMessage.setResponseObject(mSalesTranx.getId());
            responseMessage.setMessage("Consumer sales created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            /**
             * @implNote validation of Ledger Delete , if any tranx done for this ledger, user cant delete this ledger **
             * @auther ashwins@opethic.com
             * @version sprint 21
             **/
            /*@Shrikant
            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getSundryDebtors().getId(), true);
            ledgerMaster.setIsDeleted(false);
            ledgerMasterRepository.save(ledgerMaster);*/
        } else {
            responseMessage.setMessage("Duplicate Consumer Sales");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object createTranxSalesInvoices(HttpServletRequest request) throws Exception {
        ResponseMessage responseMessage = new ResponseMessage();
        JsonObject object = new JsonObject();
        TranxSalesInvoice mSalesTranx = null;
        TranxSalesInvoice salesInvoice = null;
        TransactionTypeMaster tranxType = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String salesType = request.getParameter("sale_type");
        int gstType = 1;
        if (paramMap.containsKey("gstType")) gstType = Integer.parseInt(request.getParameter("gstType"));
        if (salesType.equalsIgnoreCase("counter_sales")) {
            tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CNTS");
        } else {
            tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLS");
        }
        /* save into sales invoices  */
        if (users.getBranch() != null) {
            salesInvoice = salesTransactionRepository.findByOutletIdAndBranchIdAndSalesInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("bill_no"));
        } else {
            salesInvoice = salesTransactionRepository.findByOutletIdAndSalesInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), request.getParameter("bill_no"));
        }
        if (salesInvoice == null) mSalesTranx = saveIntoInvoice(users, request, tranxType, salesType);
        if (mSalesTranx != null) {
            /** Accounting Postings  **/
            insertIntoTranxDetailSD(mSalesTranx, tranxType, request.getParameter("newReference"), request.getParameter("outstanding_sales_return_amt")); //for sundry Debtors : dr
            insertIntoTranxDetailSA(mSalesTranx, tranxType, "CR", "Insert", gstType); // for Sales Accounts : cr
            //    insertIntoTranxDetailsSalesDiscount(mSalesTranx, tranxType, "DR", "Insert"); // for Sales Discount : dr
            insertIntoTranxDetailRO(mSalesTranx, tranxType); // for Round Off : cr or dr
            insertDB(mSalesTranx, "AC", tranxType, "CR", "Insert"); // for Additional Charges : cr
            if (users.getOutlet().getGstApplicable())
                insertDB(mSalesTranx, "DT", tranxType, "CR", "Insert"); // for Duties and Taxes : cr
            /*** Insert into Day Book : Reporting ***/
            saveIntoDayBook(mSalesTranx);
            String paymentMode = request.getParameter("paymentMode");
         /*   String salesOrderId = "";
            String salesChallanId ="";
            if (paramMap.containsKey("reference_so_id")) salesOrderId = request.getParameter("reference_so_id");
            if (paramMap.containsKey("reference_sc_id")) salesChallanId = request.getParameter("reference_sc_id");*/


            Double paidAmt = 0.0;
            if (paramMap.containsKey("paidAmount")) paidAmt = Double.parseDouble(request.getParameter("paidAmount"));
            //createReceiptInvoice(mSalesTranx, users, salesOrderId, paidAmt);
            JsonArray paymentType = new JsonArray();
            Double cashAmt = 0.0;
            if (paymentMode.equalsIgnoreCase("multi")) {
                if (!request.getParameter("cashAmt").isEmpty())
                    cashAmt = Double.parseDouble(request.getParameter("cashAmt"));
                String jsonStr = request.getParameter("payment_type");
                paymentType = new JsonParser().parse(jsonStr).getAsJsonArray();
                Double totalPaidAmt = Double.parseDouble(request.getParameter("p_totalAmount"));
                Double returnAmt = Double.parseDouble(request.getParameter("p_returnAmount"));
                Double pendingAmt = Double.parseDouble(request.getParameter("p_pendingAmount"));
                /****** insert into payment type against sales invoice *****/
                insertIntoPaymentType(mSalesTranx, paymentType, paymentMode, "create");
                createReceiptInvoice(mSalesTranx, users, paymentType, mSalesTranx.getTotalAmount(), returnAmt, paymentMode, "create", cashAmt);
                /*** set the balance amount into sales invoice after receipt voucher generated ****/
                mSalesTranx.setBalance(pendingAmt);
                salesTransactionRepository.save(mSalesTranx);
            } else if (paymentMode.equalsIgnoreCase("cash")) {
                createReceiptInvoice(mSalesTranx, users, paymentType, mSalesTranx.getTotalAmount(), 0.0, paymentMode, "create", cashAmt);
                /*** set the balance amount into sales invoice after receipt voucher generated ****/
                mSalesTranx.setBalance(0.0);
                salesTransactionRepository.save(mSalesTranx);
            }

                /*if (!salesOrderId.equalsIgnoreCase("")) {
                    Long sOrderId = Long.parseLong(salesOrderId);
                    TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                    TranxSalesOrder salesOrder = tranxSalesOrderRepository.findByIdAndStatus(sOrderId, true);
                    salesOrder.setTransactionStatus(transactionStatus);
                    tranxSalesOrderRepository.save(salesOrder);
                }
                if (!salesChallanId.equalsIgnoreCase("")) {

                    Long sChallanId = Long.parseLong(salesOrderId);
                    TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                    TranxSalesChallan salesChallan = tranxSalesChallanRepository.findByIdAndStatus(sChallanId, true);
                    salesChallan.setTransactionStatus(transactionStatus);
                    tranxSalesChallanRepository.save(salesChallan);
                }*/
            LedgerMaster frLedger = ledgerMasterRepository.findByIdAndStatus(Long.valueOf(request.getParameter("debtors_id")), true);
            if (frLedger != null) {
                AreaHead stateHead = null;
                AreaHead zoneHead = null;
                AreaHead regionHead = null;
                AreaHead districtHead = null;
                if (frLedger.getStateHeadId() != null)
                    stateHead = areaHeadRepository.findByIdAndStatus(frLedger.getStateHeadId(), true);
                if (frLedger.getZonalHeadId() != null)
                    zoneHead = areaHeadRepository.findByIdAndStatus(frLedger.getZonalHeadId(), true);
                if (frLedger.getRegionalHeadId() != null)
                    regionHead = areaHeadRepository.findByIdAndStatus(frLedger.getRegionalHeadId(), true);
                if (frLedger.getDistrictHeadId() != null)
                    districtHead = areaHeadRepository.findByIdAndStatus(frLedger.getDistrictHeadId(), true);


                String jsonStr = request.getParameter("row");
                JsonArray invoiceDetails = new JsonParser().parse(jsonStr).getAsJsonArray();


                double taxableAmount = mSalesTranx.getTaxableAmount();
                System.out.println("TaxableAmount==>" + taxableAmount);
                LocalDate invoiceDate = DateConvertUtil.convertDateToLocalDate(mSalesTranx.getBillDate());
                if (stateHead != null) {
                    CommissionMaster stCmsn = commissionMasterRepository.findByRoleTypeIgnoreCase(stateHead.getAreaRole());
                    double stateCommissionPercentage = stCmsn != null ? Double.parseDouble(stCmsn.getProductLevel()) : 0;
                    long areaHeadId = frLedger.getStateHeadId();
                    String areaRole = stateHead.getAreaRole();
                    String salesInvoiceNo = mSalesTranx.getSalesInvoiceNo();
                    double invoiceAmount = mSalesTranx.getTotalAmount();
                    double invoiceBaseAmount = 0;

                    Long createdBy = users.getId();
                    double commissionAmount = 0;

                    for (int i = 0; i < invoiceDetails.size(); i++) {
                        JsonObject obj = invoiceDetails.get(i).getAsJsonObject();
                        Product mProduct = productRepository.findByIdAndStatus(obj.get("productId").getAsLong(), true);
                        if (mProduct.getIsCommision()) {
                            double amt = mSalesTranx.getTotalBaseAmount();
                            invoiceBaseAmount += amt;
                            commissionAmount += (amt / 100) * stateCommissionPercentage;
                        }
                    }

                    if (commissionAmount > 0) {
                        AreaheadCommission areaheadCommission = new AreaheadCommission();
                        areaheadCommission.setAreaheadId(areaHeadId);
                        areaheadCommission.setAreaheadRole(areaRole);
                        areaheadCommission.setSalesInvoiceNumber(salesInvoiceNo);
                        areaheadCommission.setSalesInvoiceAmount(invoiceAmount);
                        areaheadCommission.setInvoiceDate(invoiceDate);
                        areaheadCommission.setInvoiceBaseAmount(invoiceBaseAmount);
                        areaheadCommission.setCreatedBy(createdBy);
                        areaheadCommission.setCommissionPercentage(stateCommissionPercentage);
                        areaheadCommission.setCommissionAmount(commissionAmount);
                        areaheadCommission.setFranchiseCode(frLedger.getLedgerCode());
                        areaheadCommission.setStatus(true);
                        areaheadCommissionRepository.save(areaheadCommission);
                    }
                }

                if (zoneHead != null) {
                    CommissionMaster znCmsn = commissionMasterRepository.findByRoleTypeIgnoreCase(zoneHead.getAreaRole());
                    double zoneCommissionPercentage = znCmsn != null ? Double.parseDouble(znCmsn.getProductLevel()) : 0;
                    long areaHeadId = frLedger.getZonalHeadId();
                    String areaRole = zoneHead.getAreaRole();
                    String salesInvoiceNo = mSalesTranx.getSalesInvoiceNo();
                    double invoiceAmount = mSalesTranx.getTotalAmount();
                    double invoiceBaseAmount = 0;
                    Long createdBy = users.getId();
                    double commissionAmount = 0;

                    for (int i = 0; i < invoiceDetails.size(); i++) {
                        JsonObject obj = invoiceDetails.get(i).getAsJsonObject();
                        Product mProduct = productRepository.findByIdAndStatus(obj.get("productId").getAsLong(), true);
                        if (mProduct.getIsCommision()) {
                            double amt = mSalesTranx.getTotalBaseAmount();
                            invoiceBaseAmount += amt;
                            commissionAmount += (amt / 100) * zoneCommissionPercentage;
                        }
                    }
                    if (commissionAmount > 0) {
                        AreaheadCommission areaheadCommission = new AreaheadCommission();
                        areaheadCommission.setAreaheadId(areaHeadId);
                        areaheadCommission.setAreaheadRole(areaRole);
                        areaheadCommission.setSalesInvoiceNumber(salesInvoiceNo);
                        areaheadCommission.setSalesInvoiceAmount(invoiceAmount);
                        areaheadCommission.setInvoiceDate(invoiceDate);
                        areaheadCommission.setInvoiceBaseAmount(invoiceBaseAmount);
                        areaheadCommission.setCreatedBy(createdBy);
                        areaheadCommission.setCommissionPercentage(zoneCommissionPercentage);
                        areaheadCommission.setCommissionAmount(commissionAmount);
                        areaheadCommission.setFranchiseCode(frLedger.getLedgerCode());
                        areaheadCommission.setStatus(true);
                        areaheadCommissionRepository.save(areaheadCommission);
                    }

                }

                if (regionHead != null) {
                    CommissionMaster rgCmsn = commissionMasterRepository.findByRoleTypeIgnoreCase(regionHead.getAreaRole());
                    double regionCommissionPercentage = rgCmsn != null ? Double.parseDouble(rgCmsn.getProductLevel()) : 0;
                    long areaHeadId = frLedger.getRegionalHeadId();
                    String areaRole = regionHead.getAreaRole();
                    String salesInvoiceNo = mSalesTranx.getSalesInvoiceNo();
                    double invoiceAmount = mSalesTranx.getTotalAmount();
                    double invoiceBaseAmount = 0;
                    Long createdBy = users.getId();
                    double commissionAmount = 0;

                    for (int i = 0; i < invoiceDetails.size(); i++) {
                        JsonObject obj = invoiceDetails.get(i).getAsJsonObject();
                        Product mProduct = productRepository.findByIdAndStatus(obj.get("productId").getAsLong(), true);
                        if (mProduct.getIsCommision()) {
                            double amt = mSalesTranx.getTotalBaseAmount();
                            invoiceBaseAmount += amt;
                            commissionAmount += (amt / 100) * regionCommissionPercentage;
                        }
                    }

                    if (commissionAmount > 0) {
                        AreaheadCommission areaheadCommission = new AreaheadCommission();
                        areaheadCommission.setAreaheadId(areaHeadId);
                        areaheadCommission.setAreaheadRole(areaRole);
                        areaheadCommission.setSalesInvoiceNumber(salesInvoiceNo);
                        areaheadCommission.setSalesInvoiceAmount(invoiceAmount);
                        areaheadCommission.setInvoiceDate(invoiceDate);
                        areaheadCommission.setInvoiceBaseAmount(invoiceBaseAmount);
                        areaheadCommission.setCreatedBy(createdBy);
                        areaheadCommission.setCommissionPercentage(regionCommissionPercentage);
                        areaheadCommission.setCommissionAmount(commissionAmount);
                        areaheadCommission.setFranchiseCode(frLedger.getLedgerCode());
                        areaheadCommission.setStatus(true);
                        areaheadCommissionRepository.save(areaheadCommission);
                    }

                }

                if (districtHead != null) {
                    CommissionMaster dtCmsn = commissionMasterRepository.findByRoleTypeIgnoreCase(districtHead.getAreaRole());
                    double districtCommissionPercentage = dtCmsn != null ? Double.parseDouble(dtCmsn.getProductLevel()) : 0;
                    long areaHeadId = frLedger.getDistrictHeadId();
                    String areaRole = districtHead.getAreaRole();
                    String salesInvoiceNo = mSalesTranx.getSalesInvoiceNo();
                    double invoiceAmount = mSalesTranx.getTotalAmount();
                    double invoiceBaseAmount = 0;
                    Long createdBy = users.getId();
                    double commissionAmount = 0;

                    for (int i = 0; i < invoiceDetails.size(); i++) {
                        JsonObject obj = invoiceDetails.get(i).getAsJsonObject();
                        Product mProduct = productRepository.findByIdAndStatus(obj.get("productId").getAsLong(), true);
                        if (mProduct.getIsCommision()) {
                            double amt = mSalesTranx.getTotalBaseAmount();
                            invoiceBaseAmount += amt;
                            commissionAmount += (amt / 100) * districtCommissionPercentage;
                        }
                    }

                    if (commissionAmount > 0) {
                        AreaheadCommission areaheadCommission = new AreaheadCommission();
                        areaheadCommission.setAreaheadId(areaHeadId);
                        areaheadCommission.setAreaheadRole(areaRole);
                        areaheadCommission.setSalesInvoiceNumber(salesInvoiceNo);
                        areaheadCommission.setSalesInvoiceAmount(invoiceAmount);
                        areaheadCommission.setInvoiceDate(invoiceDate);
                        areaheadCommission.setInvoiceBaseAmount(invoiceBaseAmount);
                        areaheadCommission.setCreatedBy(createdBy);
                        areaheadCommission.setCommissionPercentage(districtCommissionPercentage);
                        areaheadCommission.setCommissionAmount(commissionAmount);
                        areaheadCommission.setFranchiseCode(frLedger.getLedgerCode());
                        areaheadCommission.setStatus(true);
                        areaheadCommissionRepository.save(areaheadCommission);
                    }
                }

            }

            try {
                /****** Auto Creation of JV for Posting the Partner Commisions during FR Sales invoice Only *****/
                String frCode = mSalesTranx.getSundryDebtors().getLedgerCode();
                FranchiseMaster franchiseMaster = franchiseMasterRepository.findByFranchiseCodeAndStatus(frCode, true);
                if (franchiseMaster != null && franchiseMaster.getIsFunded() == false) {
                    List<AreaheadCommission> mCommissionsList = areaheadCommissionRepository.findBySalesInvoiceNumberAndStatus(mSalesTranx.getSalesInvoiceNo(), true);
                    if (mCommissionsList != null && mCommissionsList.size() > 0) {
                        for (AreaheadCommission mCommission : mCommissionsList) {
                            createJournal(mCommission, mSalesTranx);
                        }
                    }
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                salesInvoiceLogger.error("Error in createJV :" + exceptionAsString);
            }
            if (request.getParameterMap().containsKey("transactionTrackingNo") && request.getHeader("branch").equalsIgnoreCase("gvmh001") && frLedger.getLedgerCode().startsWith("gvmh")) {
                //call SiToPi api
                HttpHeaders gvHdr = new HttpHeaders();
                gvHdr.setContentType(MediaType.MULTIPART_FORM_DATA);
                gvHdr.add("branch", frLedger.getLedgerCode());
                LinkedMultiValueMap gvBody = new LinkedMultiValueMap();
                gvBody.add("usercode", frLedger.getLedgerCode());
                gvBody.add("transactionTrackingNo", request.getParameter("transactionTrackingNo"));
                gvBody.add("frLedgerCode", frLedger.getLedgerCode());
                gvBody.add("order_status", OrderProcessMessage.SCTOSI);


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
                gvBody.add("sale_type", request.getParameter("sale_type"));
                gvBody.add("paymentMode", request.getParameter("paymentMode"));
                gvBody.add("reference_sc_id", request.getParameter("reference_so_id"));
                gvBody.add("reference", request.getParameter("reference"));*/

                try {
                    HttpEntity gvEntity = new HttpEntity<>(gvBody, gvHdr);
                    String gvData = restTemplate.exchange(serverFrUrl + "/si_to_pi_invoices", HttpMethod.POST, gvEntity, String.class).getBody();
                    System.out.println("gvData Response => " + gvData);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    salesInvoiceLogger.error("Error in si_to_pi_invoices FR API CALL :" + exceptionAsString);
                }
            }


            responseMessage.setResponseObject(mSalesTranx.getId());
            responseMessage.setMessage("Sales invoice created successfully");
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
            responseMessage.setMessage("Duplicate Sales invoice");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

   /* private void createJV(TranxSalesInvoice mSalesTranx) {

        Long count = 0L;
        if (mSalesTranx.getBranch() != null) {
            count = tranxJournalMasterRepository.findBranchLastRecord(mSalesTranx.getOutlet().getId(),
                    mSalesTranx.getBranch().getId());
        } else {
            count = tranxJournalMasterRepository.findLastRecord(mSalesTranx.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String csCode = "JRNL" + currentMonth + serailNo;
    }*/

    public JsonObject createJournal(AreaheadCommission mCommission, TranxSalesInvoice mSalesTranx) {

        JsonObject response = new JsonObject();
        TranxJournalMaster journalMaster = new TranxJournalMaster();
        Branch branch = null;
        Long count = 0L;
        if (mSalesTranx.getBranch() != null) branch = mSalesTranx.getBranch();
        Outlet outlet = mSalesTranx.getOutlet();
        journalMaster.setBranch(branch);
        journalMaster.setOutlet(outlet);
        journalMaster.setStatus(true);
        if (branch != null) {
            count = tranxJournalMasterRepository.findBranchLastRecord(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId());
        } else {
            count = tranxJournalMasterRepository.findLastRecord(mSalesTranx.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String csCode = "JRNL" + currentMonth + serailNo;
        LocalDate tranxDate = DateConvertUtil.convertDateToLocalDate(mSalesTranx.getBillDate());
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            journalMaster.setFiscalYear(fiscalYear);
            journalMaster.setFinancialYear(fiscalYear.getFiscalYear());
        }
        journalMaster.setTranscationDate(mSalesTranx.getBillDate());
        journalMaster.setJournalSrNo(count + 1);
        journalMaster.setJournalNo(serailNo);
        journalMaster.setNarrations("Auto JV Entry of Partner Commisions During Sales Invoice");
        journalMaster.setCreatedBy(mSalesTranx.getCreatedBy());
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("JRNL");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        journalMaster.setTranxCode(tranxCode);
        journalMaster.setTotalAmt(mCommission.getCommissionAmount());
        Double incentiveAmt = mCommission.getCommissionAmount();
        CommissionMaster commissionMaster = commissionMasterRepository.findByRoleTypeIgnoreCase(mCommission.getAreaheadRole());
        Double tdsPer = commissionMaster.getTdsPer();
        Double tdsVal = incentiveAmt * tdsPer / 100.00;
        Double partnerCommisionAmt = incentiveAmt - tdsVal;
        AreaHead areaHead = areaHeadRepository.findByIdAndStatus(mCommission.getAreaheadId(), true);
        TranxJournalMaster tranxJournalMaster = tranxJournalMasterRepository.save(journalMaster);
        try {
            LedgerMaster partnerLedger = null;
            LedgerMaster partnerIncentiveLedger = null;
            if (areaHead.getAreaRole().equalsIgnoreCase("state")) {
                partnerLedger = ledgerMasterRepository.findByStateHeadId(areaHead.getId(), true, "gvmh");
                partnerIncentiveLedger = ledgerMasterRepository.findByLedgerCodeAndStatus("sh_inc", true);
            } else if (areaHead.getAreaRole().equalsIgnoreCase("region")) {
                partnerLedger = ledgerMasterRepository.findByRegionHead(areaHead.getId(), true, "gvmh");
                partnerIncentiveLedger = ledgerMasterRepository.findByLedgerCodeAndStatus("rh_inc", true);

            } else if (areaHead.getAreaRole().equalsIgnoreCase("district")) {
                partnerLedger = ledgerMasterRepository.findByDistrictHead(areaHead.getId(), true, "gvmh");
                partnerIncentiveLedger = ledgerMasterRepository.findByLedgerCodeAndStatus("dh_inc", true);
            } else if (areaHead.getAreaRole().equalsIgnoreCase("zonal")) {
                partnerLedger = ledgerMasterRepository.findByZonalHead(areaHead.getId(), true, "gvmh");
                partnerIncentiveLedger = ledgerMasterRepository.findByLedgerCodeAndStatus("zh_inc", true);
            }
            /*** Posting to Partners-----> CR ****/
            saveIntoJournalParticular(partnerLedger, partnerCommisionAmt, areaHead, tranxJournalMaster, "CR", "SC");
            /*** Posting to Incentive Ledgers (SH,RH,ZH,DH incentive)----> DR ****/
            saveIntoJournalParticular(partnerIncentiveLedger, incentiveAmt, areaHead, tranxJournalMaster, "DR", "other");
            /*** Posting to TDS Incentive Commission Ledgers (TDS 194H)-----> CR ****/
            LedgerMaster tdsCommissionLedger = ledgerMasterRepository.findByLedgerCodeAndStatus("tds194h", true);
            saveIntoJournalParticular(tdsCommissionLedger, tdsVal, areaHead, tranxJournalMaster, "CR", "other");

            response.addProperty("message", "Journal created successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            response.addProperty("message", "Error in Journal creation");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            salesInvoiceLogger.error("Error in createJournal :->" + exceptionAsString);
        }
        return response;
    }

    public void saveIntoJournalParticular(LedgerMaster ledgerMaster, Double partnerCommisionAmt, AreaHead areaHead, TranxJournalMaster tranxJournalMaster, String type, String ledgerType) {

        Double total_amt = partnerCommisionAmt;
        JsonParser parser = new JsonParser();
        String crdrType = type;
        TranxJournalDetails tranxJournalDetails = new TranxJournalDetails();
        tranxJournalDetails.setBranch(tranxJournalMaster.getBranch());
        tranxJournalDetails.setOutlet(tranxJournalMaster.getOutlet());
        tranxJournalDetails.setStatus(true);
        if (ledgerMaster != null) tranxJournalDetails.setLedgerMaster(ledgerMaster);
        tranxJournalDetails.setTranxJournalMaster(tranxJournalMaster);
        tranxJournalDetails.setType(crdrType);
        tranxJournalDetails.setLedgerType(ledgerType);
        if (crdrType.equalsIgnoreCase("dr")) {
            tranxJournalDetails.setDr(total_amt);
        }
        if (crdrType.equalsIgnoreCase("cr")) {
            tranxJournalDetails.setCr(total_amt);
        }
//        tranxJournalDetails.setPaymentDate(LocalDate.parse(journalRow.get("payment_date").getAsString()));
        tranxJournalDetails.setCreatedBy(tranxJournalMaster.getCreatedBy());
        tranxJournalDetails.setTransactionDate(DateConvertUtil.convertDateToLocalDate(tranxJournalMaster.getTranscationDate()));
        tranxJournalDetails.setPaidAmount(total_amt);
        tranxJournalDetails.setPayableAmt(0.0);
        tranxJournalDetails.setSelectedAmt(total_amt);
        tranxJournalDetails.setRemainingAmt(0.0);
        tranxJournalDetails.setIsAdvance(false);
        TranxJournalDetails mJournal = tranxJournalDetailsRepository.save(tranxJournalDetails);
        insertIntoPostings(mJournal, total_amt, type, "Insert");//Accounting Postings
    }

    private void insertIntoPostings(TranxJournalDetails mjournal, double total_amt, String crdrType, String operation) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("JRNL");
        try {

            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(total_amt, mjournal.getLedgerMaster(), tranxType, mjournal.getLedgerMaster().getAssociateGroups(), mjournal.getTranxJournalMaster().getFiscalYear(), mjournal.getBranch(), mjournal.getOutlet(), mjournal.getTranxJournalMaster().getTranscationDate(), mjournal.getTranxJournalMaster().getId(), mjournal.getTranxJournalMaster().getJournalNo(), crdrType, true, "Journal", operation);

            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, crdrType, total_amt, mjournal.getTranxJournalMaster().getFiscalYear(), mjournal.getLedgerMaster(), mjournal.getTranxJournalMaster().getTranscationDate(), mjournal.getTranxJournalMaster().getId(), mjournal.getOutlet(), mjournal.getBranch(), mjournal.getTranxJournalMaster().getTranxCode());
            }
            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING OPENING and CLOSING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(mjournal.getLedgerMaster().getId(), tranxType.getId(), mjournal.getTranxJournalMaster().getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /**** NEW METHOD FOR LEDGER POSTING OPENING and CLOSING ****/
                    postingUtility.updateLedgerPostings(mjournal.getLedgerMaster(), mjournal.getTranxJournalMaster().getTranscationDate(), tranxType, mjournal.getTranxJournalMaster().getFiscalYear(), detail);
                }
            }


            /**** Save into Day Book ****/
            if (crdrType.equalsIgnoreCase("dr") && operation.equalsIgnoreCase("Insert")) {
                saveIntoDayBook(mjournal);
            }

        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in journal insertIntoPostings :->" + e.getMessage());
        }
    }

    public Object createSiToPiInvoice(HttpServletRequest request) {
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
                        count = tranxPurInvoiceRepository.findBranchLastRecord(outletId, branchId);
                    } else {
                        count = tranxPurInvoiceRepository.findLastRecord(outletId);
                    }

                    String serailNo = String.format("%05d", count + 1);// 5 digit serial number
                    //first 3 digits of Current month
                    GenerateDates generateDates = new GenerateDates();
                    String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
                    String pcCode = "PC" + currentMonth + serailNo;

                    TranxPurOrder tranxPurOrder = tranxPurOrderRepository.findByTransactionTrackingNoAndStatus(request.getParameter("transactionTrackingNo"), true);
                    if (tranxPurOrder != null) {
                        tranxPurOrder.setOrderStatus("Ready For Delivery");
                        tranxPurOrderRepository.save(tranxPurOrder);
                        responseMessage.setMessage("purchase invoice created successfully");
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


    private void insertIntoPaymentType(TranxSalesInvoice mSalesTranx, JsonArray paymentType, String paymentMode, String key) {
        if (key.equalsIgnoreCase("create")) {
            if (paymentType != null && paymentType.size() > 0) {
                for (JsonElement mList : paymentType) {
                    JsonObject object = mList.getAsJsonObject();
                    String bankId = object.get("bankId").getAsString();
                    String bank_name = object.get("bank_name").getAsString();
                    JsonArray payment_modes = object.get("payment_modes").getAsJsonArray();
                    for (JsonElement mPaymentModes : payment_modes) {
                        Double amount = 0.0;
                        JsonObject mPaymentObject = mPaymentModes.getAsJsonObject();
                        amount = mPaymentObject.get("amount").getAsDouble();
                        if (amount > 0.0) {
                            TranxSalesPaymentType salesPaymentType = new TranxSalesPaymentType();
                            salesPaymentType.setBranch(mSalesTranx.getBranch());
                            salesPaymentType.setOutlet(mSalesTranx.getOutlet());
                            salesPaymentType.setFiscalYear(mSalesTranx.getFiscalYear());
                            salesPaymentType.setTranxSalesInvoice(mSalesTranx);
                            salesPaymentType.setType(bankId); //Bank Ledger Id
                            LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(bankId), true);
                            salesPaymentType.setLedgerMaster(mLedger);
                            salesPaymentType.setLabel(bank_name);//Bank Name
                            salesPaymentType.setPaymentMasterId(mPaymentObject.get("modeId").getAsLong());
                            salesPaymentType.setPaymentAmount(amount);
                            salesPaymentType.setPaymentMode(mPaymentObject.get("label").getAsString());
                            salesPaymentType.setReferenceId(mPaymentObject.get("refId").getAsString());
                            salesPaymentType.setStatus(true);
                            salesPaymentType.setCreatedBy(mSalesTranx.getCreatedBy());
                            transSalesPaymentTypeRepository.save(salesPaymentType);
                        }
                    }
                }
            }
        } else {
            if (paymentType != null && paymentType.size() > 0) {
                for (JsonElement mList : paymentType) {
                    JsonObject object = mList.getAsJsonObject();
                    String bankId = object.get("bankId").getAsString();
                    String bank_name = object.get("bank_name").getAsString();
                    JsonArray payment_modes = object.get("payment_modes").getAsJsonArray();
                    for (JsonElement mPaymentModes : payment_modes) {
                        Double amount = 0.0;
                        JsonObject mPaymentObject = mPaymentModes.getAsJsonObject();
                        amount = mPaymentObject.get("amount").getAsDouble();
                        if (amount > 0.0) {
                            Long detailsId = mPaymentObject.get("details_id").getAsLong();
                            TranxSalesPaymentType salesPaymentType = null;
                            if (detailsId != 0L) {
                                salesPaymentType = transSalesPaymentTypeRepository.findByIdAndStatus(detailsId, true);
                            } else {
                                salesPaymentType = new TranxSalesPaymentType();
                                salesPaymentType.setBranch(mSalesTranx.getBranch());
                                salesPaymentType.setOutlet(mSalesTranx.getOutlet());
                                salesPaymentType.setStatus(true);
                            }
                            salesPaymentType.setFiscalYear(mSalesTranx.getFiscalYear());
                            salesPaymentType.setTranxSalesInvoice(mSalesTranx);
                            salesPaymentType.setType(bankId); //Bank Ledger Id
                            LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(bankId), true);
                            salesPaymentType.setLedgerMaster(mLedger);
                            salesPaymentType.setLabel(bank_name);//Bank Name
                            salesPaymentType.setPaymentMasterId(mPaymentObject.get("modeId").getAsLong());
                            salesPaymentType.setPaymentAmount(amount);
                            salesPaymentType.setPaymentMode(mPaymentObject.get("label").getAsString());
                            salesPaymentType.setReferenceId(mPaymentObject.get("refId").getAsString());
                            salesPaymentType.setCreatedBy(mSalesTranx.getCreatedBy());
                            transSalesPaymentTypeRepository.save(salesPaymentType);
                        }
                    }
                }
            }
        }
    }

    private void insertIntoCompPaymentType(TranxSalesCompInvoice mSalesTranx, JsonArray paymentType, String paymentMode, String key) {
        if (key.equalsIgnoreCase("create")) {
            if (paymentType != null && paymentType.size() > 0) {
                for (JsonElement mList : paymentType) {
                    TranxSalesPaymentType salesPaymentType = new TranxSalesPaymentType();
                    Branch branch = null;
                    Long branchId = null;
                    if (mSalesTranx.getBranchId() != null) {
                        branch = branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(), true);
                        branchId = branch.getId();
                    }
                    Outlet outlet = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);
                    FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
                    JsonObject object = mList.getAsJsonObject();
                    LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
                    salesPaymentType.setLedgerMaster(ledgerMaster);
                    salesPaymentType.setBranch(branch);
                    salesPaymentType.setOutlet(outlet);
                    salesPaymentType.setFiscalYear(fiscalYear);
                    salesPaymentType.setTranxSalesCompInvoice(mSalesTranx);
                    salesPaymentType.setType(object.get("type").getAsString());
                    salesPaymentType.setLabel(object.get("label").getAsString());
                    salesPaymentType.setPaymentAmount(object.get("amount").getAsDouble());
                    salesPaymentType.setStatus(true);
                    salesPaymentType.setCreatedBy(mSalesTranx.getCreatedBy());
                    transSalesPaymentTypeRepository.save(salesPaymentType);
                }
            }
        } else {
            if (paymentType != null && paymentType.size() > 0) {
                for (JsonElement mList : paymentType) {
                    Branch branch = branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(), true);
                    Outlet outlet = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);
                    FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
                    LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getSundryDebtorsId(), true);

                    JsonObject object = mList.getAsJsonObject();
                    TranxSalesPaymentType salesPaymentType = transSalesPaymentTypeRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
                    salesPaymentType.setLedgerMaster(sundryDebtors);
                    salesPaymentType.setFiscalYear(fiscalYear);
                    salesPaymentType.setTranxSalesCompInvoice(mSalesTranx);
                    salesPaymentType.setType(object.get("type").getAsString());
                    salesPaymentType.setLabel(object.get("label").getAsString());
                    salesPaymentType.setPaymentAmount(object.get("amount").getAsDouble());
                    salesPaymentType.setCreatedBy(mSalesTranx.getCreatedBy());
                    transSalesPaymentTypeRepository.save(salesPaymentType);
                }
            }
        }
    }

    private void createReceiptInvoice(TranxSalesInvoice newInvoice, Users users, JsonArray paymentType, Double totalPaidAmt, Double returnAmt, String paymentMode, String key, Double cashAmt) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        TranxReceiptMaster tranxReceiptMaster = new TranxReceiptMaster();
        Long count = 0L;
        if (newInvoice.getBranch() != null) {
            count = tranxReceiptMasterRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxReceiptMasterRepository.findLastRecord(users.getOutlet().getId());
        }
        tranxReceiptMaster.setStatus(true);
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String receiptCode = "RCPT" + currentMonth + serailNo;
        tranxReceiptMaster.setReceiptNo(receiptCode);
        tranxReceiptMaster.setReceiptSrNo(Double.parseDouble(serailNo));
        if (newInvoice.getBranch() != null) tranxReceiptMaster.setBranch(newInvoice.getBranch());
        tranxReceiptMaster.setOutlet(newInvoice.getOutlet());
        tranxReceiptMaster.setTranxSalesInvoice(newInvoice);
         /*else {
            tranxReceiptMaster = tranxReceiptMasterRepository.findByTranxSalesInvoiceIdAndStatus(newInvoice.getId(), true);
        }*/
        if (newInvoice.getFiscalYear() != null) tranxReceiptMaster.setFiscalYear(newInvoice.getFiscalYear());
        tranxReceiptMaster.setTranscationDate(newInvoice.getBillDate());
        tranxReceiptMaster.setTotalAmt(totalPaidAmt);
        tranxReceiptMaster.setCreatedBy(newInvoice.getCreatedBy());
        tranxReceiptMaster.setReturnAmt(returnAmt);
        TranxReceiptMaster newTranxReceiptMaster = tranxReceiptMasterRepository.save(tranxReceiptMaster);
        LedgerMaster sundryDebtors = newInvoice.getSundryDebtors();
        if (key.equalsIgnoreCase("create")) {
            insertIntoReceiptPerticualrs(newTranxReceiptMaster, sundryDebtors, "SD", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
            String payMode = "";
            if (cashAmt > 0.0) {
                payMode = "Cash";
                LedgerMaster ledgerMaster = null;
                if (newInvoice.getBranch() != null) {
                    ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndBranchIdAndStatus("CAIH", newInvoice.getOutlet().getId(), newInvoice.getBranch().getId(), true);
                } else {
                    ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull("CAIH", newInvoice.getOutlet().getId(), true);
                }
                insertIntoReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", cashAmt, key, "cash", tranxType, newInvoice);
            } else payMode = "Bank Account";
            /**** Sales Invoice by Cash or Bank : Receipt Details of Cash Account or Bank account *****/
            if (paymentType != null && paymentType.size() > 0) {
                for (JsonElement mList : paymentType) {
                    JsonObject object = mList.getAsJsonObject();
                    LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(object.get("bankId").getAsLong(), true);
                    Double amount = transSalesPaymentTypeRepository.findAmount(newInvoice.getId(), ledgerMaster.getId());
                    payMode = "Bank Account";
                    if (amount != null && amount != 0.0)
                        insertIntoReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", amount, key, payMode, tranxType, newInvoice);
                }
            }
            /***** Sales Invoice by Cash Only : Receipt Details of Cash Account only  ******/
            else {
                LedgerMaster ledgerMaster = null;
                if (newInvoice.getBranch() != null) {
                    ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndBranchIdAndStatus("CAIH", newInvoice.getOutlet().getId(), newInvoice.getBranch().getId(), true);
                } else {
                    ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull("CAIH", newInvoice.getOutlet().getId(), true);
                }
                insertIntoReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", totalPaidAmt, key, "Cash", tranxType, newInvoice);
            }
        } /*else {
            updateIntoReceiptPerticualrs(newTranxReceiptMaster,
                    sundryDebtors, "SD", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
        }*/
    }

    private void createCompReceiptInvoice(TranxSalesCompInvoice newInvoice, Users users, JsonArray paymentType, Double totalPaidAmt, Double returnAmt, String paymentMode, String key) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        TranxReceiptMaster tranxReceiptMaster = new TranxReceiptMaster();
        Long count = 0L;
        Branch branch = null;
        if (newInvoice.getBranchId() != null) branch = branchRepository.findById(newInvoice.getBranchId()).get();
        Outlet outlet = outletRepository.findById(newInvoice.getOutletId()).get();

        if (branch != null) {
            count = tranxReceiptMasterRepository.findBranchLastRecord(outlet.getId(), branch.getId());
        } else {
            count = tranxReceiptMasterRepository.findLastRecord(users.getOutlet().getId());
        }
        tranxReceiptMaster.setStatus(true);
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String receiptCode = "RCPT" + currentMonth + serailNo;
        tranxReceiptMaster.setReceiptNo(receiptCode);
        tranxReceiptMaster.setReceiptSrNo(Double.parseDouble(serailNo));
        if (branch != null) tranxReceiptMaster.setBranch(branch);
        tranxReceiptMaster.setOutlet(outlet);
        tranxReceiptMaster.setTranxSalesCompInvoice(newInvoice);
         /*else {
            tranxReceiptMaster = tranxReceiptMasterRepository.findByTranxSalesInvoiceIdAndStatus(newInvoice.getId(), true);
        }*/
        FiscalYear fiscalYear = fiscalYearRepository.findById(newInvoice.getFiscalYearId()).get();
        if (fiscalYear != null) tranxReceiptMaster.setFiscalYear(fiscalYear);
        tranxReceiptMaster.setTranscationDate(newInvoice.getBillDate());
        tranxReceiptMaster.setTotalAmt(totalPaidAmt);
        tranxReceiptMaster.setCreatedBy(newInvoice.getCreatedBy());
        tranxReceiptMaster.setReturnAmt(returnAmt);
        TranxReceiptMaster newTranxReceiptMaster = tranxReceiptMasterRepository.save(tranxReceiptMaster);
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(newInvoice.getSundryDebtorsId(), true);

        if (key.equalsIgnoreCase("create")) {
            insertIntoCompReceiptPerticualrs(newTranxReceiptMaster, sundryDebtors, "SD", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
            /**** Sales Invoice by Cash or Bank : Receipt Details of Cash Account or Bank account *****/
            if (paymentType != null && paymentType.size() > 0) {
                for (JsonElement mList : paymentType) {
                    JsonObject object = mList.getAsJsonObject();
                    String payMode = "";
                    if (object.get("type").getAsString().equals("others")) payMode = "Cash";
                    else {
                        payMode = "Bank Account";
                    }
                    LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
                    insertIntoCompReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, object.get("type").getAsString(), object.get("amount").getAsDouble(), key, payMode, tranxType, newInvoice);
                }
            }
            /***** Sales Invoice by Cash Only : Receipt Details of Cash Account only  ******/
            else {
                LedgerMaster ledgerMaster = null;

                if (branch != null) {
                    ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndBranchIdAndStatus("CAIH", outlet.getId(), branch.getId(), true);
                } else {
                    ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull("CAIH", outlet.getId(), true);
                }
                insertIntoCompReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", totalPaidAmt, key, "cash", tranxType, newInvoice);
            }
        } /*else {
            updateIntoReceiptPerticualrs(newTranxReceiptMaster,
                    sundryDebtors, "SD", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
        }*/
    }

    private void updateCompReceiptInvoice(TranxSalesCompInvoice newInvoice, Users users, JsonArray paymentType, Double totalPaidAmt, Double returnAmt, String paymentMode, String key) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        TranxReceiptMaster tranxReceiptMaster = tranxReceiptMasterRepository.findByTranxSalesCompInvoiceIdAndStatus(newInvoice.getId(), true);
        Long count = 0L;
        Branch branch = null;
        if (newInvoice.getBranchId() != null) branch = branchRepository.findById(newInvoice.getBranchId()).get();
        Outlet outlet = outletRepository.findByIdAndStatus(newInvoice.getOutletId(), true);
        LedgerMaster sundryDebtor = ledgerMasterRepository.findByIdAndStatus(newInvoice.getSundryDebtorsId(), true);
        FiscalYear fiscalYear = fiscalYearRepository.findById(newInvoice.getFiscalYearId()).get();

        if (branch != null) tranxReceiptMaster.setBranch(branch);
        tranxReceiptMaster.setTranxSalesCompInvoice(newInvoice);
        if (fiscalYear != null) tranxReceiptMaster.setFiscalYear(fiscalYear);
        tranxReceiptMaster.setTranscationDate(newInvoice.getBillDate());
        tranxReceiptMaster.setTotalAmt(totalPaidAmt);
        tranxReceiptMaster.setReturnAmt(returnAmt);
        TranxReceiptMaster newTranxReceiptMaster = tranxReceiptMasterRepository.save(tranxReceiptMaster);
        LedgerMaster sundryDebtors = sundryDebtor;
        updateIntoCompReceiptPerticualrs(newTranxReceiptMaster, sundryDebtors, "SD", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
        /**** Sales Invoice by Cash or Bank : Receipt Details of Cash Account or Bank account *****/
        if (paymentType != null && paymentType.size() > 0) {
            for (JsonElement mList : paymentType) {
                JsonObject object = mList.getAsJsonObject();
                String payMode = "";
                if (object.get("type").getAsString().equals("others")) payMode = "Cash";
                else {
                    payMode = "Bank Account";
                }
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
                updateIntoCompReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, object.get("type").getAsString(), object.get("amount").getAsDouble(), key, payMode, tranxType, newInvoice);
            }
        }
        /***** Sales Invoice by Cash Only : Receipt Details of Cash Account only  ******/
        else {
            LedgerMaster ledgerMaster = null;
            if (branch != null) {
                ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndBranchIdAndStatus("CAIH", newInvoice.getOutletId(), newInvoice.getBranchId(), true);
            } else {
                ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull("CAIH", newInvoice.getOutletId(), true);
            }
            updateIntoCompReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", totalPaidAmt, key, "cash", tranxType, newInvoice);
        }
         /*else {
            updateIntoReceiptPerticualrs(newTranxReceiptMaster,
                    sundryDebtors, "SD", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
        }*/
    }

    private void updateReceiptInvoice(TranxSalesInvoice newInvoice, Users users, JsonArray paymentType, Double totalPaidAmt, Double returnAmt, String paymentMode, String key, Double cashAmt) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        TranxReceiptMaster tranxReceiptMaster = tranxReceiptMasterRepository.findByTranxSalesInvoiceIdAndStatus(newInvoice.getId(), true);
        Long count = 0L;
        if (newInvoice.getBranch() != null) tranxReceiptMaster.setBranch(newInvoice.getBranch());
        tranxReceiptMaster.setTranxSalesInvoice(newInvoice);
        if (newInvoice.getFiscalYear() != null) tranxReceiptMaster.setFiscalYear(newInvoice.getFiscalYear());
        tranxReceiptMaster.setTranscationDate(newInvoice.getBillDate());
        tranxReceiptMaster.setTotalAmt(totalPaidAmt);
        tranxReceiptMaster.setReturnAmt(returnAmt);
        TranxReceiptMaster newTranxReceiptMaster = tranxReceiptMasterRepository.save(tranxReceiptMaster);
        LedgerMaster sundryDebtors = newInvoice.getSundryDebtors();
        updateIntoReceiptPerticualrs(newTranxReceiptMaster, sundryDebtors, "SD", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
        String payMode = "";
        if (cashAmt > 0.0) {
            payMode = "Cash";
            LedgerMaster ledgerMaster = null;
            if (newInvoice.getBranch() != null) {
                ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndBranchIdAndStatus("CAIH", newInvoice.getOutlet().getId(), newInvoice.getBranch().getId(), true);
            } else {
                ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull("CAIH", newInvoice.getOutlet().getId(), true);
            }
//            insertIntoReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", cashAmt, key, "cash", tranxType, newInvoice);
            updateIntoReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", cashAmt, key, payMode, tranxType, newInvoice);
        } else payMode = "Bank Account";
        /**** Sales Invoice by Cash or Bank : Receipt Details of Cash Account or Bank account *****/
        if (paymentType != null && paymentType.size() > 0) {
            for (JsonElement mList : paymentType) {
                JsonObject object = mList.getAsJsonObject();
                    /*if (object.get("type").getAsString().equals("others")) payMode = "Cash";
                    else {
                        payMode = "Bank Account";
                    }*/
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(object.get("bankId").getAsLong(), true);
                Double amount = transSalesPaymentTypeRepository.findAmount(newInvoice.getId(), ledgerMaster.getId());
                if (amount != null && amount != 0.0)
                    updateIntoReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", amount, key, payMode, tranxType, newInvoice);
            }
        }
        /***** Sales Invoice by Cash Only : Receipt Details of Cash Account only  ******/
        else {
            LedgerMaster ledgerMaster = null;
            if (newInvoice.getBranch() != null) {
                ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndBranchIdAndStatus("CAIH", newInvoice.getOutlet().getId(), newInvoice.getBranch().getId(), true);
            } else {
                ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull("CAIH", newInvoice.getOutlet().getId(), true);
            }
            updateIntoReceiptPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", totalPaidAmt, key, "cash", tranxType, newInvoice);
        }
         /*else {
            updateIntoReceiptPerticualrs(newTranxReceiptMaster,
                    sundryDebtors, "SD", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
        }*/
    }

    /****** Save into sales invoices  ******/
    private TranxSalesInvoice saveIntoInvoice(Users users, HttpServletRequest request, TransactionTypeMaster tranxType, String salesType) throws Exception {
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesInvoice mSalesTranx = null;
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        Outlet outlet = users.getOutlet();
        TranxSalesInvoice invoiceTranx = new TranxSalesInvoice();
        invoiceTranx.setBranch(branch);
        invoiceTranx.setOutlet(outlet);
//        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date date = new Date();
        /*  invoiceTranx.setBillDate(date);*/
//        Date dt = DateConvertUtil.convertStringToDate(request.getParameter("bill_dt"));
        invoiceTranx.setBillDate(date);
        invoiceTranx.setGstNumber(request.getParameter("gstNo"));
        invoiceTranx.setTransactionStatus(1L);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(DateConvertUtil.convertDateToLocalDate(date));
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setSalesSerialNumber(Long.parseLong(request.getParameter("sales_sr_no")));
        invoiceTranx.setSalesInvoiceNo(request.getParameter("bill_no"));
        LedgerMaster salesAccounts = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), true);
        LedgerMaster discountLedger = null;
        if (users.getBranch() != null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales discount", users.getOutlet().getId(), true);
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
        invoiceTranx.setBalance(Double.parseDouble(request.getParameter("bill_amount")));
        if (paramMap.containsKey("salesmanId") && !request.getParameter("salesmanId").equalsIgnoreCase("")) {
            SalesManMaster salesManMaster = salesmanMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("salesmanId")), true);
            invoiceTranx.setSalesmanUser(salesManMaster.getId());
        }
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
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        invoiceTranx.setSalesDiscountAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        if (paramMap.containsKey("paymentMode")) invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        if (paramMap.containsKey("advancedAmount"))
            invoiceTranx.setAdvancedAmount(Double.parseDouble(request.getParameter("advancedAmount")));
        if (paramMap.containsKey("p_totalAmount"))
            invoiceTranx.setPaymentAmount(Double.parseDouble(request.getParameter("p_totalAmount")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setOrderStatus("Ready For Delivery");
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setOperations("inserted");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
        else invoiceTranx.setNarration("");
        invoiceTranx.setIsCounterSale(false);
        if (request.getParameterMap().containsKey("transactionTrackingNo"))
            invoiceTranx.setTransactionTrackingNo(request.getParameter("transactionTrackingNo"));
       /* if (salesType.equalsIgnoreCase("sales")) {
            invoiceTranx.setIsCounterSale(false);
        } else {
            invoiceTranx.setIsCounterSale(true);
        }*/
        if (paramMap.containsKey("reference")) invoiceTranx.setReference(request.getParameter("reference"));
        /* convertions of Sales Quotions to invoice */
        if (paramMap.containsKey("reference_sq_id")) {
            String jsonRef = request.getParameter("reference_sq_id");
            JsonParser parser = new JsonParser();
            JsonElement referenceDetailsJson = parser.parse(jsonRef);
            JsonArray referenceDetails = referenceDetailsJson.getAsJsonArray();
            String id = "";
            for (int i = 0; i < referenceDetails.size(); i++) {
                JsonObject object = referenceDetails.get(i).getAsJsonObject();
                id = id + object.get("id").getAsString();
                if (i < referenceDetails.size() - 1) id = id + ",";
            }
            invoiceTranx.setReferenceSqId(id);
            // setCloseSQ(request.getParameter("reference_sq_id"));
        }
        /* convertions of Sales Order to invoice */
        if (paramMap.containsKey("reference_so_id")) {
            String jsonRef = request.getParameter("reference_so_id");
            JsonParser parser = new JsonParser();
            JsonElement referenceDetailsJson = parser.parse(jsonRef);
            JsonArray referenceDetails = referenceDetailsJson.getAsJsonArray();
            String id = "";
            for (int i = 0; i < referenceDetails.size(); i++) {
                JsonObject object = referenceDetails.get(i).getAsJsonObject();
                id = id + object.get("id").getAsString();
                if (i < referenceDetails.size() - 1) id = id + ",";
            }
            invoiceTranx.setReferenceSoId(id);
            //   setCloseSO(request.getParameter("reference_so_id"));
        }
        /* convertions of Sales challan to invoice */
        if (paramMap.containsKey("reference_sc_id")) {
            String jsonRef = request.getParameter("reference_sc_id");
            JsonParser parser = new JsonParser();
            JsonElement referenceDetailsJson = parser.parse(jsonRef);
            JsonArray referenceDetails = referenceDetailsJson.getAsJsonArray();
            String id = "";
            for (int i = 0; i < referenceDetails.size(); i++) {
                JsonObject object = referenceDetails.get(i).getAsJsonObject();
                id = id + object.get("id").getAsString();
                if (i < referenceDetails.size() - 1) id = id + ",";
            }
            invoiceTranx.setReferenceScId(id);
            //   setCloseSO(request.getParameter("reference_so_id"));
        }

        if (salesType.equalsIgnoreCase("counter_sales")) {
            if (paramMap.containsKey("counter_sales_ids")) {
                String s = "";
                String str = request.getParameter("counter_sales_ids");
                JsonParser parser = new JsonParser();
                JsonElement jsonElement = parser.parse(str);
                JsonArray array = jsonElement.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    JsonObject jsonObject = array.get(i).getAsJsonObject();
                    s = s + jsonObject.get("id").getAsString();
                    if (i != array.size()) s = s + ",";
                    TranxCounterSales csSale = counterSaleRepository.findByIdAndStatus(Long.parseLong(jsonObject.get("id").getAsString()), true);
                    csSale.setIsBillConverted(true);
                    counterSaleRepository.save(csSale);
                }
                invoiceTranx.setCounterSaleId(s);
                invoiceTranx.setIsCounterSale(false);
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
        if (paramMap.containsKey("additionalChgLedger3") && !request.getParameter("additionalChgLedger3").equals("")) {
            LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
            if (additionalChgLedger3 != null) {
                invoiceTranx.setAdditionLedger3(additionalChgLedger3);
                invoiceTranx.setAdditionLedgerAmt3(Double.valueOf(request.getParameter("addChgLedgerAmt3")));
            }
        }
        invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        if (paramMap.containsKey("isRoundOffCheck"))
            invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));
        /***** TCS and TDS *****/
        if (paramMap.containsKey("tcs_mode")) {
            if (request.getParameter("tcs_mode").equalsIgnoreCase("tcs")) {
                invoiceTranx.setTcsAmt(Double.parseDouble(request.getParameter("tcs_amt")));
                invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs_per")));
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
            }
            if (request.getParameter("tcs_mode").equalsIgnoreCase("tds")) {
                invoiceTranx.setTdsAmt(Double.parseDouble(request.getParameter("tcs_amt")));
                invoiceTranx.setTdsPer(Double.parseDouble(request.getParameter("tcs_per")));
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
            } else if (request.getParameter("tcs_mode").equalsIgnoreCase("")) {
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
            }
            invoiceTranx.setTcsMode(request.getParameter("tcs_mode"));
        }
        try {
            invoiceTranx.setStateId(sundryDebtors.getStateHeadId());
            invoiceTranx.setZoneId(sundryDebtors.getZonalHeadId());
            invoiceTranx.setRegionalId(sundryDebtors.getRegionalHeadId());
            invoiceTranx.setDistrictId(sundryDebtors.getDistrictHeadId());
            mSalesTranx = salesTransactionRepository.save(invoiceTranx);
            /* Save into Sales Duties and Taxes */
            if (mSalesTranx != null) {
                if (Boolean.parseBoolean(request.getParameter("creditNoteReference"))) {
                    String jsonStr = request.getParameter("bills");
                    JsonParser parser = new JsonParser();
                    JsonElement creditNoteBills = parser.parse(jsonStr);
                    JsonArray creditNotes = creditNoteBills.getAsJsonArray();
                    Double totalBalance = 0.0;
                    for (JsonElement mBill : creditNotes) {
                        TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReference = null;
                        JsonObject mcreditNote = mBill.getAsJsonObject();
                        totalBalance += mcreditNote.get("creditNotePaidAmt").getAsDouble();
                        tranxCreditNoteNewReference = tranxCreditNoteNewReferenceRepository.findByIdAndStatus(mcreditNote.get("creditNoteId").getAsLong(), true);
                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                        tranxCreditNoteNewReference.setTotalAmount(mcreditNote.get("creditNotePaidAmt").getAsDouble());
                        if (mcreditNote.get("creditNoteRemaningAmt").getAsDouble() == 0) {
                            tranxCreditNoteNewReference.setTransactionStatusId(transactionStatus.getId());
                        }
                        tranxCreditNoteNewReference.setBalance(mcreditNote.get("creditNoteRemaningAmt").getAsDouble());
                        TranxCreditNoteNewReferenceMaster newReferenceMaster = tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteNewReference);

                        /* Adding into Debit Note Details */
                        TranxCreditNoteDetails mDetails = new TranxCreditNoteDetails();
                        if (newReferenceMaster.getBranchId() != null)
                            mDetails.setBranchId(newReferenceMaster.getBranchId());
                        Outlet mOutlet = outletRepository.findByIdAndStatus(newReferenceMaster.getOutletId(), true);
                        mDetails.setOutletId(mOutlet.getId());
                        mDetails.setSundryDebtorsId(newReferenceMaster.getSundryDebtorsId());
                        mDetails.setTotalAmount(newReferenceMaster.getTotalAmount());
                        mDetails.setPaidAmt(mcreditNote.get("creditNotePaidAmt").getAsDouble());
                        mDetails.setAdjustedId(mSalesTranx.getId());
                        mDetails.setAdjustedSource("sales_invoice");
                        mDetails.setOperations("adjust");
                        mDetails.setTranxCreditNoteMasterId(newReferenceMaster.getId());
                        mDetails.setStatus(true);
                        mDetails.setCreatedBy(newReferenceMaster.getCreatedBy());
                        mDetails.setAdjustmentStatus(newReferenceMaster.getAdjustmentStatus());
                        // immediate
                        tranxCreditNoteDetailsRepository.save(mDetails);
                    }
                    mSalesTranx.setBalance(mSalesTranx.getTotalAmount() - totalBalance);
                    try {
                        salesTransactionRepository.save(mSalesTranx);
                    } catch (Exception e) {
                        e.printStackTrace();
                        salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e.getMessage());
                        salesInvoiceLogger.error("Error while creating adjustment of credit note bill against sales invoice" + e.getMessage());
                    }
                }
                /**** if Company is Unregister or Composition , for registered company uncomment below saveInoDutiesAndTaxes ****/
                if (mSalesTranx.getOutlet().getGstApplicable()) {
                    String taxStr = request.getParameter("taxCalculation");
                    JsonObject duties_taxes = new JsonParser().parse(taxStr).getAsJsonObject();
                    saveInoDutiesAndTaxes(duties_taxes, mSalesTranx, taxFlag);
                }
                /**** Save into Additional Charges ****/
                if (paramMap.containsKey("additionalCharges")) {
                    String strJson = request.getParameter("additionalCharges");
                    JsonElement tradeElement = new JsonParser().parse(strJson);
                    JsonArray additionalCharges = tradeElement.getAsJsonArray();
                    saveIntoAdditionalCharges(additionalCharges, mSalesTranx, tranxType.getId(), users.getOutlet().getId());
                }
                /* Save into Sales Invoice Details */
                String jsonStr = request.getParameter("row");
                JsonArray invoiceDetails = new JsonParser().parse(jsonStr).getAsJsonArray();
                String referenceObj = "";
                if (paramMap.containsKey("reference")) referenceObj = request.getParameter("reference");
                saveIntoSalesInvoiceDetails(invoiceDetails, mSalesTranx, branch, outlet, users.getId(), tranxType, salesType, referenceObj);
            }

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
            // throw new Exception(e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
            // throw new Exception(e1.getMessage());
        }
        return mSalesTranx;
    }

  /*  private TranxSalesCompInvoice saveIntoCompInvoice(Users users, MultipartHttpServletRequest request, TransactionTypeMaster tranxType, String salesType) throws Exception {
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesCompInvoice mSalesTranx = null;
        Outlet outlet = users.getOutlet();
        TranxSalesCompInvoice invoiceTranx = new TranxSalesCompInvoice();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            invoiceTranx.setBranchId(branch.getId());
        }
        FileStorageProperties fileStorageProperties = new FileStorageProperties();

        invoiceTranx.setOutletId(outlet.getId());
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        invoiceTranx.setBillDate(date);
        invoiceTranx.setGstNumber(request.getParameter("gstNo"));
        *//* fiscal year mapping *//*
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYearId(fiscalYear.getId());
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        *//* End of fiscal year mapping *//*
//        invoiceTranx.setSalesSerialNumber(Long.parseLong(request.getParameter("sales_sr_no")));


        Long count = 0L;
        if (branch != null) {
            count = salesCompTransactionRepository.findBranchLastRecord(users.getOutlet().getId(), branch.getId());
        } else {
            count = salesCompTransactionRepository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String siCode = "CNTS" + currentMonth + serailNo;
        invoiceTranx.setSalesSerialNumber(count + 1);
        invoiceTranx.setSalesInvoiceNo(siCode);

        LedgerMaster salesAccounts = null;
//        ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), true);
        if (users.getBranch() != null)
            salesAccounts = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales a/c", users.getOutlet().getId(), users.getBranch().getId(), true);
        else
            salesAccounts = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales a/c", users.getOutlet().getId(), true);
        if (salesAccounts != null) {
            invoiceTranx.setSalesAccountLedgerId(salesAccounts.getId());
        }
        LedgerMaster discountLedger = null;
        if (users.getBranch() != null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales discount", users.getOutlet().getId(), true);
        if (discountLedger != null) {
            invoiceTranx.setSalesDiscountLedgerId(discountLedger.getId());
        }
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), true);
        invoiceTranx.setSundryDebtorsId(sundryDebtors.getId());

        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setSalesRoundOffId(roundoff.getId());
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
        invoiceTranx.setBalance(Double.parseDouble(request.getParameter("bill_amount")));
        if (paramMap.containsKey("salesmanId") && !request.getParameter("salesmanId").equalsIgnoreCase("")) {
            SalesManMaster salesManMaster = salesmanMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("salesmanId")), true);
            invoiceTranx.setSalesmanUser(salesManMaster.getId());
        }
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        *//* if true : cgst and sgst i.e intra state *//*
        if (taxFlag) {
            invoiceTranx.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            invoiceTranx.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            invoiceTranx.setTotaligst(0.0);
        }
        *//* if false : igst i.e inter state *//*
        else {
            invoiceTranx.setTotalcgst(0.0);
            invoiceTranx.setTotalsgst(0.0);
            invoiceTranx.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        if (paramMap.containsKey("doctorsId")) {
            invoiceTranx.setDoctorId(Long.parseLong(request.getParameter("doctorsId")));
        }
      *//*  if (paramMap.containsKey("client_name")) {
            invoiceTranx.setClientName(request.getParameter("client_name"));
        }
        if (paramMap.containsKey("client_address")) {
            invoiceTranx.setClientAddress(request.getParameter("client_address"));
        }*//*
        if (paramMap.containsKey("mobile_number")) {
            invoiceTranx.setMobileNumber(request.getParameter("mobile_number"));
        }
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setDoctorAddress(request.getParameter("drAddress"));
        invoiceTranx.setPatientName(request.getParameter("patientName"));
        if (request.getFile("prescFile") != null) {
            MultipartFile image = request.getFile("prescFile");
            fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
            if (imagePath != null) {
                invoiceTranx.setImageUpload(File.separator + "uploads" + File.separator + imagePath);
            }
        }

//        invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        invoiceTranx.setSalesDiscountAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        if (paramMap.containsKey("paymentMode")) invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        if (paramMap.containsKey("advancedAmount"))
            invoiceTranx.setAdvancedAmount(Double.parseDouble(request.getParameter("advancedAmount")));
        if (paramMap.containsKey("p_totalAmount"))
            invoiceTranx.setPaymentAmount(Double.parseDouble(request.getParameter("p_totalAmount")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setOperations("inserted");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
        else invoiceTranx.setNarration("");
        invoiceTranx.setIsCounterSale(false);

        if (paramMap.containsKey("additionalChgLedger3") && !request.getParameter("additionalChgLedger3").equals("")) {
            LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
            if (additionalChgLedger3 != null) {
                invoiceTranx.setAdditionLedger3Id(additionalChgLedger3.getId());
                invoiceTranx.setAdditionLedgerAmt3(Double.valueOf(request.getParameter("addChgLedgerAmt3")));
            }
        }
        invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        if (paramMap.containsKey("isRoundOffCheck"))
            invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));
        *//***** TCS and TDS *****//*
        if (paramMap.containsKey("tcs_mode")) {
            if (request.getParameter("tcs_mode").equalsIgnoreCase("tcs")) {
                invoiceTranx.setTcsAmt(Double.parseDouble(request.getParameter("tcs_amt")));
                invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs_per")));
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
            }
            if (request.getParameter("tcs_mode").equalsIgnoreCase("tds")) {
                invoiceTranx.setTdsAmt(Double.parseDouble(request.getParameter("tcs_amt")));
                invoiceTranx.setTdsPer(Double.parseDouble(request.getParameter("tcs_per")));
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
            } else if (request.getParameter("tcs_mode").equalsIgnoreCase("")) {
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
            }
            invoiceTranx.setTcsMode(request.getParameter("tcs_mode"));
        }
        try {
            mSalesTranx = salesCompTransactionRepository.save(invoiceTranx);
            *//* Save into Sales Duties and Taxes *//*
            if (mSalesTranx != null) {
                if (Boolean.parseBoolean(request.getParameter("creditNoteReference"))) {
                    String jsonStr = request.getParameter("bills");
                    JsonParser parser = new JsonParser();
                    JsonElement creditNoteBills = parser.parse(jsonStr);
                    JsonArray creditNotes = creditNoteBills.getAsJsonArray();
                    Double totalBalance = 0.0;
                    for (JsonElement mBill : creditNotes) {
                        TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReference = null;
                        JsonObject mcreditNote = mBill.getAsJsonObject();
                        totalBalance += mcreditNote.get("creditNotePaidAmt").getAsDouble();
                        tranxCreditNoteNewReference = tranxCreditNoteNewReferenceRepository.findByIdAndStatus(mcreditNote.get("creditNoteId").getAsLong(), true);
                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                        tranxCreditNoteNewReference.setTotalAmount(mcreditNote.get("creditNotePaidAmt").getAsDouble());
                        if (mcreditNote.get("creditNoteRemaningAmt").getAsDouble() == 0) {
                            tranxCreditNoteNewReference.setTransactionStatusId(transactionStatus.getId());
                        }
                        tranxCreditNoteNewReference.setBalance(mcreditNote.get("creditNoteRemaningAmt").getAsDouble());
                        TranxCreditNoteNewReferenceMaster newReferenceMaster = tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteNewReference);

                        *//* Adding into Debit Note Details *//*
                        TranxCreditNoteDetails mDetails = new TranxCreditNoteDetails();
//                        Branch mBranch = branchRepository.findByIdAndStatus(newReferenceMaster.getBranchId(), true);
                        if (newReferenceMaster.getBranchId() != null)
                            mDetails.setBranchId(newReferenceMaster.getBranchId());
                        Outlet mOutlet = outletRepository.findByIdAndStatus(newReferenceMaster.getOutletId(), true);
                        mDetails.setOutletId(mOutlet.getId());
                        mDetails.setSundryDebtorsId(newReferenceMaster.getSundryDebtorsId());
                        mDetails.setTotalAmount(newReferenceMaster.getTotalAmount());
                        mDetails.setPaidAmt(mcreditNote.get("creditNotePaidAmt").getAsDouble());
                        mDetails.setAdjustedId(mSalesTranx.getId());
                        mDetails.setAdjustedSource("consumer_invoice");
                        mDetails.setOperations("adjust");
                        mDetails.setTranxCreditNoteMasterId(newReferenceMaster.getId());
                        mDetails.setStatus(true);
                        mDetails.setCreatedBy(newReferenceMaster.getCreatedBy());
                        mDetails.setAdjustmentStatus(newReferenceMaster.getAdjustmentStatus());
                        // immediate
                        tranxCreditNoteDetailsRepository.save(mDetails);
                    }
                    mSalesTranx.setBalance(mSalesTranx.getTotalAmount() - totalBalance);
                    try {
                        salesCompTransactionRepository.save(mSalesTranx);
                    } catch (Exception e) {
                        e.printStackTrace();
                        salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e.getMessage());
                        salesInvoiceLogger.error("Error while creating adjustment of credit note bill against sales invoice" + e.getMessage());
                    }
                }
                */

    /**** if Company is Unregister or Composition , for registered company uncomment below saveInoDutiesAndTaxes ****//*
                Outlet outl = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);
                if (outl.getGstApplicable()) {
                    String taxStr = request.getParameter("taxCalculation");
                    JsonObject duties_taxes = new JsonParser().parse(taxStr).getAsJsonObject();
                    saveIntoCompDutiesAndTaxes(duties_taxes, mSalesTranx, taxFlag);
                }
                *//* Save into Sales Invoice Details *//*
                String jsonStr = request.getParameter("row");
                JsonArray invoiceDetails = new JsonParser().parse(jsonStr).getAsJsonArray();
                String referenceObj = request.getParameter("refObject");
                //112
                saveIntoCompSalesInvoiceDetails(invoiceDetails, mSalesTranx, branch, outlet, users.getId(), tranxType, salesType, referenceObj);
            }

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
            // throw new Exception(e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
            // throw new Exception(e1.getMessage());
        }
        return mSalesTranx;
    }
*/
    private TranxSalesCompInvoice saveIntoCompInvoice(Users users, MultipartHttpServletRequest request, TransactionTypeMaster tranxType, String salesType) throws Exception {
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesCompInvoice mSalesTranx = null;
        Outlet outlet = users.getOutlet();
        TranxSalesCompInvoice invoiceTranx = new TranxSalesCompInvoice();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            invoiceTranx.setBranchId(branch.getId());
        }
        FileStorageProperties fileStorageProperties = new FileStorageProperties();

        invoiceTranx.setOutletId(outlet.getId());
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date dt = DateConvertUtil.convertStringToDate(request.getParameter("bill_dt"));
        invoiceTranx.setBillDate(dt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYearId(fiscalYear.getId());
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
//        invoiceTranx.setSalesSerialNumber(Long.parseLong(request.getParameter("sales_sr_no")));


        Long count = 0L;
        if (branch != null) {
            count = salesCompTransactionRepository.findBranchLastRecord(users.getOutlet().getId(), branch.getId());
        } else {
            count = salesCompTransactionRepository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String siCode = "CONS" + currentMonth + serailNo;
        invoiceTranx.setSalesSerialNumber(count + 1);
        invoiceTranx.setSalesInvoiceNo(siCode);

        LedgerMaster salesAccounts = null;
//        ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), true);
        if (users.getBranch() != null)
            salesAccounts = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales a/c", users.getOutlet().getId(), users.getBranch().getId(), true);
        else
            salesAccounts = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales a/c", users.getOutlet().getId(), true);
        if (salesAccounts != null) {
            invoiceTranx.setSalesAccountLedgerId(salesAccounts.getId());
        }
        LedgerMaster discountLedger = null;
        if (users.getBranch() != null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales discount", users.getOutlet().getId(), true);
        if (discountLedger != null) {
            invoiceTranx.setSalesDiscountLedgerId(discountLedger.getId());
        }
        /****** creating ledgers against the patient master *****/
        PatientMaster patientMaster = patientMasterRepository.findByPatientCodeAndStatus(request.getParameter("debtors_id"), true);
        LedgerMaster sundryDebtors = null;
        sundryDebtors = ledgerMasterRepository.findByLedgerCodeAndStatus(patientMaster.getPatientCode(), true);
        if (sundryDebtors == null) {
            sundryDebtors = createLedger(patientMaster);
        }
        invoiceTranx.setSundryDebtorsId(sundryDebtors.getId());
        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
        invoiceTranx.setBalance(Double.parseDouble(request.getParameter("bill_amount")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("totalqty")));
        if (paramMap.containsKey("sales_discount")) {
            invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        }
        if (paramMap.containsKey("doctorsId")) {
            invoiceTranx.setDoctorId(Long.parseLong(request.getParameter("doctorsId")));
        }
        if (paramMap.containsKey("client_name")) {
            invoiceTranx.setClientName(request.getParameter("client_name"));
        }
        if (paramMap.containsKey("client_address")) {
            invoiceTranx.setClientAddress(request.getParameter("client_address"));
        }
        if (paramMap.containsKey("mobile_number")) {
            invoiceTranx.setMobileNumber(request.getParameter("mobile_number"));
        }
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setDoctorAddress(request.getParameter("drAddress"));
        invoiceTranx.setPatientName(request.getParameter("patientName"));
        if (request.getFile("prescFile") != null) {
            MultipartFile image = request.getFile("prescFile");
            fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
            if (imagePath != null) {
                invoiceTranx.setImageUpload(File.separator + "uploads" + File.separator + imagePath);
            }
        }

        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        // invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setCreatedBy(users.getId());
        // invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        if (paramMap.containsKey("paymentMode")) invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        invoiceTranx.setStatus(true);
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setOperations("inserted");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
        else invoiceTranx.setNarration("");
        invoiceTranx.setIsCounterSale(false);

        try {
            mSalesTranx = salesCompTransactionRepository.save(invoiceTranx);
            /* Save into Sales Duties and Taxes */
            if (mSalesTranx != null) {
                if (paramMap.containsKey("creditNoteReference") && Boolean.parseBoolean(request.getParameter("creditNoteReference"))) {
                    String jsonStr = request.getParameter("bills");
                    JsonParser parser = new JsonParser();
                    JsonElement creditNoteBills = parser.parse(jsonStr);
                    JsonArray creditNotes = creditNoteBills.getAsJsonArray();
                    Double totalBalance = 0.0;
                    for (JsonElement mBill : creditNotes) {
                        TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReference = null;
                        JsonObject mcreditNote = mBill.getAsJsonObject();
                        totalBalance += mcreditNote.get("creditNotePaidAmt").getAsDouble();
                        tranxCreditNoteNewReference = tranxCreditNoteNewReferenceRepository.findByIdAndStatus(mcreditNote.get("creditNoteId").getAsLong(), true);
                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                        tranxCreditNoteNewReference.setTotalAmount(mcreditNote.get("creditNotePaidAmt").getAsDouble());
                        if (mcreditNote.get("creditNoteRemaningAmt").getAsDouble() == 0) {
                            tranxCreditNoteNewReference.setTransactionStatusId(transactionStatus.getId());
                        }
                        tranxCreditNoteNewReference.setBalance(mcreditNote.get("creditNoteRemaningAmt").getAsDouble());
                        TranxCreditNoteNewReferenceMaster newReferenceMaster = tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteNewReference);

                        /* Adding into Debit Note Details */
                        TranxCreditNoteDetails mDetails = new TranxCreditNoteDetails();
//                        Branch mBranch = branchRepository.findByIdAndStatus(newReferenceMaster.getBranchId(), true);
                        if (newReferenceMaster.getBranchId() != null)
                            mDetails.setBranchId(newReferenceMaster.getBranchId());
                        Outlet mOutlet = outletRepository.findByIdAndStatus(newReferenceMaster.getOutletId(), true);
                        mDetails.setOutletId(mOutlet.getId());
                        mDetails.setSundryDebtorsId(newReferenceMaster.getSundryDebtorsId());
                        mDetails.setTotalAmount(newReferenceMaster.getTotalAmount());
                        mDetails.setPaidAmt(mcreditNote.get("creditNotePaidAmt").getAsDouble());
                        mDetails.setAdjustedId(mSalesTranx.getId());
                        mDetails.setAdjustedSource("consumer_invoice");
                        mDetails.setOperations("adjust");
                        mDetails.setTranxCreditNoteMasterId(newReferenceMaster.getId());
                        mDetails.setStatus(true);
                        mDetails.setCreatedBy(newReferenceMaster.getCreatedBy());
                        mDetails.setAdjustmentStatus(newReferenceMaster.getAdjustmentStatus());
                        // immediate
                        tranxCreditNoteDetailsRepository.save(mDetails);
                    }
                    mSalesTranx.setBalance(mSalesTranx.getTotalAmount() - totalBalance);
                    try {
                        salesCompTransactionRepository.save(mSalesTranx);
                    } catch (Exception e) {
                        e.printStackTrace();
                        salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e.getMessage());
                        salesInvoiceLogger.error("Error while creating adjustment of credit note bill against sales invoice" + e.getMessage());
                    }
                }
                /**** if Company is Unregister or Composition , for registered company uncomment below saveInoDutiesAndTaxes ****/
                Outlet outl = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);
                if (paramMap.containsKey("taxCalculation") && outl.getGstApplicable()) {
                    String taxStr = request.getParameter("taxCalculation");
                    JsonObject duties_taxes = new JsonParser().parse(taxStr).getAsJsonObject();
                    saveIntoCompDutiesAndTaxes(duties_taxes, mSalesTranx, taxFlag);
                }
                /* Save into Sales Invoice Details */
                String jsonStr = request.getParameter("row");
                JsonArray invoiceDetails = new JsonParser().parse(jsonStr).getAsJsonArray();
                String referenceObj = "";
                if (paramMap.containsKey("refObject")) referenceObj = request.getParameter("refObject");
                //112
                saveIntoCompSalesInvoiceDetails(invoiceDetails, mSalesTranx, branch, outlet, users.getId(), tranxType, salesType, referenceObj);
            }

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
            // throw new Exception(e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
            // throw new Exception(e1.getMessage());
        }
        return mSalesTranx;
    }

    /**** Creation of Patient Ledgers *****/
    private LedgerMaster createLedger(PatientMaster patientMaster) {
        LedgerMaster ledgerMaster = new LedgerMaster();
        ledgerMaster.setLedgerName(patientMaster.getPatientName());
        ledgerMaster.setLedgerCode(patientMaster.getPatientCode());
        ledgerMaster.setUniqueCode("SUDR");
        ledgerMaster.setMailingName(patientMaster.getPatientName());
        ledgerMaster.setOpeningBal(0.0);
        ledgerMaster.setOpeningBalType("DR");
        ledgerMaster.setAddress(patientMaster.getPatientAddress());
        ledgerMaster.setTaxable(false);
        ledgerMaster.setSlugName("sundry_debtors");
        ledgerMaster.setStatus(true);
        ledgerMaster.setIsDeleted(true);
        ledgerMaster.setIsPrivate(false);
        ledgerMaster.setIsDefaultLedger(false);
        ledgerMaster.setIsCredit(false);
        ledgerMaster.setIsLicense(false);
        ledgerMaster.setIsShippingDetails(false);
        ledgerMaster.setIsDepartment(false);
        ledgerMaster.setIsBankDetails(false);
        Principles principles = principleRepository.findByIdAndStatus(3L, true);
        ledgerMaster.setPrinciples(principles);
        PrincipleGroups principleGroups = principleGroupsRepository.findByIdAndStatus(1L, true);
        ledgerMaster.setPrincipleGroups(principleGroups);
        Foundations foundations = foundationRepository.findByIdAndStatus(1L, true);
        ledgerMaster.setFoundations(foundations);
        Outlet outlet = outletRepository.findByIdAndStatus(patientMaster.getOutletId(), true);
        ledgerMaster.setOutlet(outlet);
        BalancingMethod balancingMethod = balancingMethodRepository.findByIdAndStatus(2L, true);
        ledgerMaster.setBalancingMethod(balancingMethod);
        ledgerMaster.setUnderPrefix("AG#" + patientMaster.getId());
        LedgerMaster mLedger = ledgerRepository.save(ledgerMaster);
        return mLedger;
    }

    private void saveIntoDayBook(TranxSalesInvoice mSalesTranx) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(mSalesTranx.getOutlet());
        if (mSalesTranx.getBranch() != null) dayBook.setBranch(mSalesTranx.getBranch());
        dayBook.setAmount(mSalesTranx.getTotalAmount());
        dayBook.setTranxDate(DateConvertUtil.convertDateToLocalDate(mSalesTranx.getBillDate()));
        dayBook.setParticulars(mSalesTranx.getSundryDebtors().getLedgerName());
        dayBook.setVoucherNo(mSalesTranx.getSalesInvoiceNo());
        dayBook.setVoucherType("Sales Invoice");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }

    public void saveIntoDayBook(TranxJournalDetails mjournal) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(mjournal.getOutlet());
        if (mjournal.getBranch() != null) dayBook.setBranch(mjournal.getBranch());
        dayBook.setAmount(mjournal.getPaidAmount());
        LocalDate trDt = DateConvertUtil.convertDateToLocalDate(mjournal.getTranxJournalMaster().getTranscationDate());
        dayBook.setTranxDate(trDt);
        dayBook.setParticulars(mjournal.getLedgerMaster().getLedgerName());
        dayBook.setVoucherNo(mjournal.getTranxJournalMaster().getJournalNo());
        dayBook.setVoucherType("Journal");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }
    /* End of Sales Invoice */

    private void saveCompIntoDayBook(TranxSalesCompInvoice mSalesTranx) {
        Branch branch = null;
        if (mSalesTranx.getBranchId() != null)
            branch = branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(), true);
        Outlet outlet = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);
        LedgerMaster sundryDebtor = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getSundryDebtorsId(), true);
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(outlet);
        if (branch != null) dayBook.setBranch(branch);
        dayBook.setAmount(mSalesTranx.getTotalAmount());
        dayBook.setTranxDate(DateConvertUtil.convertDateToLocalDate(mSalesTranx.getBillDate()));
        dayBook.setParticulars(sundryDebtor.getLedgerName());
        dayBook.setVoucherNo(mSalesTranx.getSalesInvoiceNo());
        dayBook.setVoucherType("Consumer Sales");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }


    private void insertIntoReceiptPerticualrs(TranxReceiptMaster newTranxReceiptMaster, LedgerMaster ledgerMaster, String type, Double amount, String key, String paymentMode, TransactionTypeMaster tranxType, TranxSalesInvoice newInvoice) {
        TranxReceiptPerticulars tranxReceiptPerticulars = null;
        tranxReceiptPerticulars = new TranxReceiptPerticulars();
        String ledgerType = "";
        tranxReceiptPerticulars.setStatus(true);
        tranxReceiptPerticulars.setBranch(newTranxReceiptMaster.getBranch());
        tranxReceiptPerticulars.setOutlet(newTranxReceiptMaster.getOutlet());
        tranxReceiptPerticulars.setLedgerMaster(ledgerMaster);
        tranxReceiptPerticulars.setTranxReceiptMaster(newTranxReceiptMaster);
        tranxReceiptPerticulars.setLedgerType(type);
        tranxReceiptPerticulars.setLedgerName(ledgerMaster.getLedgerName());
        if (type.equalsIgnoreCase("SD")) {
            ledgerType = "CR";
            tranxReceiptPerticulars.setDr(0.0);
            tranxReceiptPerticulars.setCr(amount);
            tranxReceiptPerticulars.setType("cr");
            tranxReceiptPerticulars.setPayableAmt(amount);
            tranxReceiptPerticulars.setSelectedAmt(amount);
            tranxReceiptPerticulars.setRemainingAmt(newInvoice.getTotalAmount() - amount);
            tranxReceiptPerticulars.setIsAdvance(false);
        } else {
            ledgerType = "DR";
            tranxReceiptPerticulars.setDr(amount);
            tranxReceiptPerticulars.setCr(0.0);
            tranxReceiptPerticulars.setType("dr");
        }
        tranxReceiptPerticulars.setPaymentMethod(paymentMode);
        tranxReceiptPerticulars.setTransactionDate(DateConvertUtil.convertDateToLocalDate(newTranxReceiptMaster.getTranscationDate()));
        tranxReceiptPerticulars.setCreatedBy(newTranxReceiptMaster.getCreatedBy());
        TranxReceiptPerticulars receiptPerticulars = tranxReceiptPerticularRepository.save(tranxReceiptPerticulars);
        /*** Insert into Bill Details of Receipt: receipt against the sales invoice *****/
        if (type.equalsIgnoreCase("SD")) {
            InsertIntoBillDetails(receiptPerticulars, key, newInvoice);
        }
/*
        ledgerCommonPostings.callToPostings(amount, ledgerMaster, tranxType, ledgerMaster.getAssociateGroups(), newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getId(), newTranxReceiptMaster.getReceiptNo(), ledgerType, true, tranxType.getTransactionCode(), "Insert");
*/
        ledgerCommonPostings.callToPostingsTranxCode(amount, ledgerMaster, tranxType, ledgerMaster.getAssociateGroups(), newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getId(), newTranxReceiptMaster.getReceiptNo(), ledgerType, true, tranxType.getTransactionCode(), "Insert", newTranxReceiptMaster.getTranxCode());

        /***** NEW METHOD FOR LEDGER POSTING *****/
        postingUtility.callToPostingLedger(tranxType, type, amount, newTranxReceiptMaster.getFiscalYear(), ledgerMaster, newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getId(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getTranxCode());
    }

    private void insertIntoCompReceiptPerticualrs(TranxReceiptMaster newTranxReceiptMaster, LedgerMaster ledgerMaster, String type, Double amount, String key, String paymentMode, TransactionTypeMaster tranxType, TranxSalesCompInvoice newInvoice) {
        TranxReceiptPerticulars tranxReceiptPerticulars = null;
        tranxReceiptPerticulars = new TranxReceiptPerticulars();
        String ledgerType = "";
        tranxReceiptPerticulars.setStatus(true);
        tranxReceiptPerticulars.setBranch(newTranxReceiptMaster.getBranch());
        tranxReceiptPerticulars.setOutlet(newTranxReceiptMaster.getOutlet());
        tranxReceiptPerticulars.setLedgerMaster(ledgerMaster);
        tranxReceiptPerticulars.setTranxReceiptMaster(newTranxReceiptMaster);
        tranxReceiptPerticulars.setLedgerType(type);
        tranxReceiptPerticulars.setLedgerName(ledgerMaster.getLedgerName());
        if (type.equalsIgnoreCase("SD")) {
            ledgerType = "CR";
            tranxReceiptPerticulars.setDr(0.0);
            tranxReceiptPerticulars.setCr(amount);
            tranxReceiptPerticulars.setType("cr");
            tranxReceiptPerticulars.setPayableAmt(amount);
            tranxReceiptPerticulars.setSelectedAmt(amount);
            tranxReceiptPerticulars.setRemainingAmt(newInvoice.getTotalAmount() - amount);
            tranxReceiptPerticulars.setIsAdvance(false);
        } else {
            ledgerType = "DR";
            tranxReceiptPerticulars.setDr(amount);
            tranxReceiptPerticulars.setCr(0.0);
            tranxReceiptPerticulars.setType("dr");
        }
        tranxReceiptPerticulars.setPaymentMethod(paymentMode);
        tranxReceiptPerticulars.setTransactionDate(DateConvertUtil.convertDateToLocalDate(newTranxReceiptMaster.getTranscationDate()));
        tranxReceiptPerticulars.setCreatedBy(newTranxReceiptMaster.getCreatedBy());
        TranxReceiptPerticulars receiptPerticulars = tranxReceiptPerticularRepository.save(tranxReceiptPerticulars);
        /*** Insert into Bill Details of Receipt: receipt against the sales invoice *****/
        if (type.equalsIgnoreCase("SD")) {
            InsertIntoCompBillDetails(receiptPerticulars, key, newInvoice);
        }
        ledgerCommonPostings.callToPostings(amount, ledgerMaster, tranxType, ledgerMaster.getAssociateGroups(), newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getId(), newTranxReceiptMaster.getReceiptNo(), ledgerType, true, tranxType.getTransactionCode(), "Insert");


        /***** NEW METHOD FOR LEDGER POSTING *****/
        postingUtility.callToPostingLedger(tranxType, ledgerType, amount, newTranxReceiptMaster.getFiscalYear(), ledgerMaster, newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getId(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getTranxCode());
    }

    private void updateIntoReceiptPerticualrs(TranxReceiptMaster newTranxReceiptMaster, LedgerMaster ledgerMaster, String type, Double amount, String key, String paymentMode, TransactionTypeMaster tranxType, TranxSalesInvoice newInvoice) {

        String ledgerType = "";
        TranxReceiptPerticulars mTranxPerticular = tranxReceiptPerticularRepository.findByTranxReceiptMasterIdAndStatusAndLedgerTypeIgnoreCaseAndLedgerMasterId(newTranxReceiptMaster.getId(), true, type, ledgerMaster.getId());
        if (mTranxPerticular != null) {
            mTranxPerticular.setLedgerMaster(ledgerMaster);
            mTranxPerticular.setLedgerType(type);
            mTranxPerticular.setLedgerName(ledgerMaster.getLedgerName());
            if (type.equalsIgnoreCase("SD")) {
                ledgerType = "CR";
                mTranxPerticular.setDr(0.0);
                mTranxPerticular.setCr(amount);
                mTranxPerticular.setType("cr");
                mTranxPerticular.setPayableAmt(amount);
                mTranxPerticular.setSelectedAmt(amount);
                mTranxPerticular.setRemainingAmt(newInvoice.getTotalAmount() - amount);
                mTranxPerticular.setIsAdvance(false);
            } else {
                ledgerType = "DR";
                mTranxPerticular.setDr(amount);
                mTranxPerticular.setCr(0.0);
                mTranxPerticular.setType("dr");
            }
            mTranxPerticular.setPaymentMethod(paymentMode);
            mTranxPerticular.setTransactionDate(DateConvertUtil.convertDateToLocalDate(newTranxReceiptMaster.getTranscationDate()));
            mTranxPerticular.setCreatedBy(newTranxReceiptMaster.getCreatedBy());
            TranxReceiptPerticulars receiptPerticulars = tranxReceiptPerticularRepository.save(mTranxPerticular);
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(ledgerMaster.getId(), tranxType.getId(), newTranxReceiptMaster.getId());
            if (mLedger != null) {
                mLedger.setAmount(amount);
                mLedger.setTransactionDate(newTranxReceiptMaster.getTranscationDate());
                mLedger.setOperations("updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            } else {
                ledgerCommonPostings.callToPostings(amount, ledgerMaster, tranxType, ledgerMaster.getAssociateGroups(), newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getId(), newTranxReceiptMaster.getReceiptNo(), ledgerType, true, tranxType.getTransactionCode(), "Insert");
            }

            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.callToPostingLedgerForUpdateByDetailsId(amount, ledgerMaster.getId(), tranxType, ledgerType, newTranxReceiptMaster.getId(), ledgerMaster, newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getTranxCode());

            /*** Insert into Bill Details of Receipt: receipt against the sales invoice *****/
            if (type.equalsIgnoreCase("SD")) {
                InsertIntoBillDetails(receiptPerticulars, key, newInvoice);
            }
        }
    }

    private void updateIntoCompReceiptPerticualrs(TranxReceiptMaster newTranxReceiptMaster, LedgerMaster ledgerMaster, String type, Double amount, String key, String paymentMode, TransactionTypeMaster tranxType, TranxSalesCompInvoice newInvoice) {

        String ledgerType = "";
        TranxReceiptPerticulars mTranxPerticular = tranxReceiptPerticularRepository.findByTranxReceiptMasterIdAndStatusAndLedgerTypeIgnoreCaseAndLedgerMasterId(newTranxReceiptMaster.getId(), true, type, ledgerMaster.getId());
        if (mTranxPerticular != null) {
            mTranxPerticular.setLedgerMaster(ledgerMaster);
            mTranxPerticular.setLedgerType(type);
            mTranxPerticular.setLedgerName(ledgerMaster.getLedgerName());
            if (type.equalsIgnoreCase("SD")) {
                ledgerType = "CR";
                mTranxPerticular.setDr(0.0);
                mTranxPerticular.setCr(amount);
                mTranxPerticular.setType("cr");
                mTranxPerticular.setPayableAmt(amount);
                mTranxPerticular.setSelectedAmt(amount);
                mTranxPerticular.setRemainingAmt(newInvoice.getTotalAmount() - amount);
                mTranxPerticular.setIsAdvance(false);
            } else {
                ledgerType = "DR";
                mTranxPerticular.setDr(amount);
                mTranxPerticular.setCr(0.0);
                mTranxPerticular.setType("dr");
            }
            mTranxPerticular.setPaymentMethod(paymentMode);
            mTranxPerticular.setTransactionDate(DateConvertUtil.convertDateToLocalDate(newTranxReceiptMaster.getTranscationDate()));
            mTranxPerticular.setCreatedBy(newTranxReceiptMaster.getCreatedBy());
            TranxReceiptPerticulars receiptPerticulars = tranxReceiptPerticularRepository.save(mTranxPerticular);
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(ledgerMaster.getId(), tranxType.getId(), newTranxReceiptMaster.getId());
            if (mLedger != null) {
                mLedger.setAmount(amount);
                mLedger.setTransactionDate(newTranxReceiptMaster.getTranscationDate());
                mLedger.setOperations("updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            } else {
                ledgerCommonPostings.callToPostings(amount, ledgerMaster, tranxType, ledgerMaster.getAssociateGroups(), newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getId(), newTranxReceiptMaster.getReceiptNo(), ledgerType, true, tranxType.getTransactionCode(), "Insert");
            }

            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.callToPostingLedgerForUpdateByDetailsId(amount, ledgerMaster.getId(), tranxType, ledgerType, newTranxReceiptMaster.getId(), ledgerMaster, newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getTranxCode());
            /*** Insert into Bill Details of Receipt: receipt against the sales invoice *****/
            if (type.equalsIgnoreCase("SD")) {
                InsertIntoCompBillDetails(receiptPerticulars, key, newInvoice);
            }
        }
    }

    private void InsertIntoBillDetails(TranxReceiptPerticulars receiptPerticulars, String key, TranxSalesInvoice newInvoice) {
        TranxReceiptPerticularsDetails tranxRptDetails = new TranxReceiptPerticularsDetails();
        if (key.equalsIgnoreCase("update")) {
            tranxRptDetails = tranxReceiptPerticularsDetailsRepository.findByStatusAndTranxReceiptPerticularsId(true, receiptPerticulars.getId());
        }
        if (key.equalsIgnoreCase("create")) {
            tranxRptDetails.setBranch(receiptPerticulars.getBranch());
            tranxRptDetails.setOutlet(receiptPerticulars.getOutlet());
            tranxRptDetails.setStatus(true);
            tranxRptDetails.setCreatedBy(receiptPerticulars.getCreatedBy());
            tranxRptDetails.setType("sales_invoice");
        }
        tranxRptDetails.setLedgerMaster(receiptPerticulars.getLedgerMaster());
        tranxRptDetails.setTranxReceiptMaster(receiptPerticulars.getTranxReceiptMaster());
        tranxRptDetails.setTranxReceiptPerticulars(receiptPerticulars);
        tranxRptDetails.setTotalAmt(receiptPerticulars.getCr());
        tranxRptDetails.setTransactionDate(receiptPerticulars.getTransactionDate());
        tranxRptDetails.setAmount(receiptPerticulars.getCr());
        tranxRptDetails.setPaidAmt(receiptPerticulars.getCr());
        tranxRptDetails.setTranxNo(newInvoice.getSalesInvoiceNo());
        tranxRptDetails.setTranxInvoiceId(newInvoice.getId());
        tranxRptDetails.setRemainingAmt(newInvoice.getTotalAmount() - receiptPerticulars.getCr());
        tranxReceiptPerticularsDetailsRepository.save(tranxRptDetails);
    }

    private void InsertIntoCompBillDetails(TranxReceiptPerticulars receiptPerticulars, String key, TranxSalesCompInvoice newInvoice) {
        TranxReceiptPerticularsDetails tranxRptDetails = new TranxReceiptPerticularsDetails();
        if (key.equalsIgnoreCase("update")) {
            tranxRptDetails = tranxReceiptPerticularsDetailsRepository.findByStatusAndTranxReceiptPerticularsId(true, receiptPerticulars.getId());
        }
        if (key.equalsIgnoreCase("create")) {
            tranxRptDetails.setBranch(receiptPerticulars.getBranch());
            tranxRptDetails.setOutlet(receiptPerticulars.getOutlet());
            tranxRptDetails.setStatus(true);
            tranxRptDetails.setCreatedBy(receiptPerticulars.getCreatedBy());
            tranxRptDetails.setType("consumer_sales");
        }
        tranxRptDetails.setLedgerMaster(receiptPerticulars.getLedgerMaster());
        tranxRptDetails.setTranxReceiptMaster(receiptPerticulars.getTranxReceiptMaster());
        tranxRptDetails.setTranxReceiptPerticulars(receiptPerticulars);
        tranxRptDetails.setTotalAmt(receiptPerticulars.getCr());
        tranxRptDetails.setTransactionDate(receiptPerticulars.getTransactionDate());
        tranxRptDetails.setAmount(receiptPerticulars.getCr());
        tranxRptDetails.setPaidAmt(receiptPerticulars.getCr());
        tranxRptDetails.setTranxNo(newInvoice.getSalesInvoiceNo());
        tranxRptDetails.setTranxInvoiceId(newInvoice.getId());
        tranxRptDetails.setRemainingAmt(newInvoice.getTotalAmount() - receiptPerticulars.getCr());
        tranxReceiptPerticularsDetailsRepository.save(tranxRptDetails);
    }

    /****** Save into Sales Duties and Taxes ******/
    private void saveInoDutiesAndTaxes(JsonObject duties_taxes, TranxSalesInvoice mSalesTranx, Boolean taxFlag) {
        List<TranxSalesInvoiceDutiesTaxes> salesDutiesTaxes = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (int i = 0; i < cgstList.size(); i++) {
                    JsonObject cgstObject = cgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT CGST " + inputGst;
                    if (mSalesTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesInvoiceDutiesTaxes taxes = new TranxSalesInvoiceDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(cgstObject.get("amt").getAsDouble());
                        taxes.setSalesTransaction(mSalesTranx);
                        taxes.setSundryDebtors(mSalesTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        taxes.setCreatedBy(mSalesTranx.getCreatedBy());
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (int i = 0; i < sgstList.size(); i++) {
                    JsonObject sgstObject = sgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //     int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT SGST " + inputGst;
                    if (mSalesTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesInvoiceDutiesTaxes taxes = new TranxSalesInvoiceDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(sgstObject.get("amt").getAsDouble());
                        taxes.setSalesTransaction(mSalesTranx);
                        taxes.setSundryDebtors(mSalesTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        taxes.setCreatedBy(mSalesTranx.getCreatedBy());
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        } else {
            JsonArray igstList = duties_taxes.getAsJsonArray("igst");
            /* this is for Igst creation */
            if (igstList.size() > 0) {
                for (int i = 0; i < igstList.size(); i++) {
                    JsonObject igstObject = igstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    // int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String inputGst = igstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT IGST " + inputGst;
                    if (mSalesTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesInvoiceDutiesTaxes taxes = new TranxSalesInvoiceDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(igstObject.get("amt").getAsDouble());
                        taxes.setSalesTransaction(mSalesTranx);
                        taxes.setSundryDebtors(mSalesTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        taxes.setCreatedBy(mSalesTranx.getCreatedBy());
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
            salesInvoiceLogger.error("Error in saveInoDutiesAndTaxes :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in saveInoDutiesAndTaxes :->" + e1.getMessage());
            System.out.println(e1.getMessage());
        }
    }
    /* End of  Sales Duties and Taxes Ledger */

    /****** save into Consumer Sales Duties and Taxes *****/
    private void saveIntoCompDutiesAndTaxes(JsonObject duties_taxes, TranxSalesCompInvoice mSalesTranx, Boolean taxFlag) {
        List<TranxSalesCompInvoiceDutiesTaxes> salesDutiesTaxes = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (int i = 0; i < cgstList.size(); i++) {
                    JsonObject cgstObject = cgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT CGST " + inputGst;


                    if (mSalesTranx.getBranchId() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutletId(), mSalesTranx.getBranchId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutletId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesCompInvoiceDutiesTaxes taxes = new TranxSalesCompInvoiceDutiesTaxes();
                        taxes.setDutiesTaxesId(dutiesTaxes.getId());
                        taxes.setAmount(cgstObject.get("amt").getAsDouble());
                        taxes.setSalesTransactionId(mSalesTranx.getId());
                        taxes.setSundryDebtorsId(mSalesTranx.getSundryDebtorsId());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        taxes.setCreatedBy(mSalesTranx.getCreatedBy());
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (int i = 0; i < sgstList.size(); i++) {
                    JsonObject sgstObject = sgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //     int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT SGST " + inputGst;
//                    Branch branch=branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(),true);
                    if (mSalesTranx.getBranchId() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutletId(), mSalesTranx.getBranchId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutletId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesCompInvoiceDutiesTaxes taxes = new TranxSalesCompInvoiceDutiesTaxes();
                        taxes.setDutiesTaxesId(dutiesTaxes.getId());
                        taxes.setAmount(sgstObject.get("amt").getAsDouble());
                        taxes.setSalesTransactionId(mSalesTranx.getId());
                        taxes.setSundryDebtorsId(mSalesTranx.getSundryDebtorsId());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        taxes.setCreatedBy(mSalesTranx.getCreatedBy());
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        } else {
            JsonArray igstList = duties_taxes.getAsJsonArray("igst");
            /* this is for Igst creation */
            if (igstList.size() > 0) {
                for (int i = 0; i < igstList.size(); i++) {
                    JsonObject igstObject = igstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    // int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String inputGst = igstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT IGST " + inputGst;
//                    Branch branch=branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(),true);
                    if (mSalesTranx.getBranchId() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutletId(), mSalesTranx.getBranchId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutletId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesCompInvoiceDutiesTaxes taxes = new TranxSalesCompInvoiceDutiesTaxes();
                        taxes.setDutiesTaxesId(dutiesTaxes.getId());
                        taxes.setAmount(igstObject.get("amt").getAsDouble());
                        taxes.setSalesTransactionId(mSalesTranx.getId());
                        taxes.setSundryDebtorsId(mSalesTranx.getSundryDebtorsId());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        taxes.setCreatedBy(mSalesTranx.getCreatedBy());
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        }
        try {
            /* save all Duties and Taxes into Sales Invoice Duties taxes table */
            salesCompDutiesTaxesRepository.saveAll(salesDutiesTaxes);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in saveInoDutiesAndTaxes :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in saveInoDutiesAndTaxes :->" + e1.getMessage());
            System.out.println(e1.getMessage());
        }
    }

    /****** Save into Sales AdditionalCharges ******/
    public void saveIntoAdditionalCharges(JsonArray additionalCharges, TranxSalesInvoice mSalesTranx, Long tranxId, Long outletId) {
        List<TranxSalesInvoiceAdditionalCharges> chargesList = new ArrayList<>();
        if (mSalesTranx.getAdditionalChargesTotal() > 0) {
            for (int j = 0; j < additionalCharges.size(); j++) {
                TranxSalesInvoiceAdditionalCharges charges = new TranxSalesInvoiceAdditionalCharges();
                JsonObject object = additionalCharges.get(j).getAsJsonObject();
                Double amount = object.get("amt").getAsDouble();
                Double percent = object.get("percent").getAsDouble();
                Long ledgerId = object.get("ledgerId").getAsLong();
                LedgerMaster addcharges = ledgerMasterRepository.findByIdAndStatus(ledgerId, true);
                charges.setAmount(amount);
                charges.setPercent(percent);
                charges.setAdditionalCharges(addcharges);
                charges.setSalesTransaction(mSalesTranx);
                charges.setStatus(true);
                charges.setCreatedBy(mSalesTranx.getCreatedBy());
                chargesList.add(charges);
            }
        }
        try {
            salesAdditionalChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException de) {
            salesInvoiceLogger.error("Error in saveIntoAdditionalCharges :->" + de.getMessage());
            System.out.println(de.getMessage());
            de.printStackTrace();
        } catch (Exception e1) {
            salesInvoiceLogger.error("Error in saveIntoAdditionalCharges :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }
    }
    /* End of Sales AdditionalCharges */

    /****** Save into Sales Invoice Details ******/
    public void saveIntoSalesInvoiceDetails(JsonArray invoiceDetails, TranxSalesInvoice mSalesTranx, Branch branch, Outlet outlet, Long userId, TransactionTypeMaster tranxType, String salesType, String referenceObj) {
        String refType = "";
        boolean flag_status = false;
        Long referenceId = 0L;
        /* Sales Product Details Start here */
        List<TranxSalesInvoiceProductSrNumber> newSerialNumbers = new ArrayList<>();
        for (int i = 0; i < invoiceDetails.size(); i++) {
            JsonObject object = invoiceDetails.get(i).getAsJsonObject();
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            if (object.has("reference_type")) refType = object.get("reference_type").getAsString();
            if (object.has("counterNo")) {
                refType = object.get("counterNo").getAsString();
            }
            /* inserting into TranxSalesInvoiceDetailsUnits */
            String batchNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
            String serialNo = null;
            double free_qty = 0.0;
            double tranxQty = 0.0;
            Long batchId = null;
            Double stkQty = 0.0;


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
            TranxSalesInvoiceDetailsUnits invoiceUnits = new TranxSalesInvoiceDetailsUnits();
            invoiceUnits.setSalesInvoice(mSalesTranx);
            invoiceUnits.setProduct(mProduct);
            invoiceUnits.setUnits(units);
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            tranxQty = object.get("qty").getAsDouble();
            invoiceUnits.setReturnQty(0.0);
            invoiceUnits.setTransactionStatusId(1L);
            invoiceUnits.setFreeQty(0.0);
            if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase("")) {
                free_qty = object.get("free_qty").getAsDouble();
                invoiceUnits.setFreeQty(free_qty);
            }
            invoiceUnits.setRate(0.0);
            if (object.has("rate") && !object.get("rate").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setRate(object.get("rate").getAsDouble());
            invoiceUnits.setStatus(true);
            if (levelA != null) invoiceUnits.setLevelAId(levelA.getId());
            if (levelB != null) invoiceUnits.setLevelBId(levelB.getId());
            if (levelC != null) invoiceUnits.setLevelCId(levelC.getId());
            invoiceUnits.setBaseAmt(0.0);
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
            /******* Insert into Product Batch No ****/
            boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    Double quantity = Double.parseDouble(object.get("qty").getAsString());
                    int qty = quantity.intValue();

                    double net_amt = object.get("final_amt").getAsDouble();
                    double costing = 0;
                    double costing_with_tax = 0;

                    /*if (outlet.getGstTypeMaster().getId() == 1L) { //Registered
                        net_amt = object.get("total_amt").getAsDouble();
                        costing = net_amt / (qty + free_qty);
                        costing_with_tax = costing + object.get("total_igst").getAsDouble();
                    } else { // composition or un-registered
                        costing = net_amt / (qty + free_qty);
                        costing_with_tax = costing;
                    }*/

                    if (object.get("b_details_id").getAsLong() == 0) {
                        ProductBatchNo mproductBatchNo = new ProductBatchNo();
                        Double qnty = object.get("qty").getAsDouble();
                        mproductBatchNo.setQnty(qnty.intValue());
                        mproductBatchNo.setFreeQty(free_qty);
                        if (object.has("b_no")) mproductBatchNo.setBatchNo(object.get("b_no").getAsString());
                        mproductBatchNo.setMrp(0.0);
                        if (object.has("b_rate") && !object.get("b_rate").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        mproductBatchNo.setPurchaseRate(0.0);
                        if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                            mproductBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") && !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                            mproductBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        mproductBatchNo.setSalesRate(0.0);
                        if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equals(""))
                            mproductBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                        if (object.has("costing") && !object.get("costing").isJsonNull())
                            mproductBatchNo.setCosting(object.get("costing").getAsDouble());
                        else mproductBatchNo.setCosting(0.0);
                        if (object.has("costing_with_tax") && !object.get("costing_with_tax").isJsonNull())
                            mproductBatchNo.setCostingWithTax(object.get("costing_with_tax").getAsDouble());
                        productBatchNo.setMinRateA(0.0);
                        productBatchNo.setMinRateB(0.0);
                        productBatchNo.setMinRateC(0.0);
                        mproductBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        mproductBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                        mproductBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                        if (object.has("margin_per"))
                            mproductBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
                        mproductBatchNo.setStatus(true);
                        mproductBatchNo.setProduct(mProduct);
                        mproductBatchNo.setOutlet(outlet);
                        mproductBatchNo.setBranch(branch);
                        mproductBatchNo.setUnits(units);
                        if (levelA != null) mproductBatchNo.setLevelA(levelA);
                        if (levelB != null) mproductBatchNo.setLevelB(levelB);
                        if (levelC != null) mproductBatchNo.setLevelC(levelC);
                        productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                    } else {
                        productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                       /* productBatchNo.setQnty(object.get("qty").getAsInt());
                        productBatchNo.setFreeQty(free_qty);
                        productBatchNo.setCosting(costing);
                        productBatchNo.setCostingWithTax(costing_with_tax);
                        if (object.has("b_no")) productBatchNo.setBatchNo(object.get("b_no").getAsString());
                        if (object.has("b_rate")) productBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equals(""))
                            productBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                        if (object.has("b_purchase_rate"))
                            productBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") &&
                                !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                            productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") &&
                                !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                            productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        productBatchNo.setMinRateA(0.0);
                        productBatchNo.setMinRateB(0.0);
                        productBatchNo.setMinRateC(0.0);
                        productBatchNo.setMinMargin(0.0);
                        if (object.has("rate_a") && !object.get("rate_a").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        if (object.has("rate_b") && !object.get("rate_b").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                        if (object.has("rate_c") && !object.get("rate_c").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                        if (object.has("margin_per"))
                            productBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
                        productBatchNo.setStatus(true);
                        productBatchNo.setProduct(mProduct);
                        productBatchNo.setOutlet(outlet);
                        productBatchNo.setBranch(branch);
                        if (levelA != null) productBatchNo.setLevelA(levelA);
                        if (levelB != null) productBatchNo.setLevelB(levelB);
                        if (levelC != null) productBatchNo.setLevelC(levelC);
                        productBatchNo.setUnits(units);
                        productBatchNo = productBatchNoRepository.save(productBatchNo);*/
                    }
                    batchNo = productBatchNo.getBatchNo();
                    batchId = productBatchNo.getId();
                }
                invoiceUnits.setProductBatchNo(productBatchNo);
                TranxSalesInvoiceDetailsUnits tranxSalesInvoiceDetailsUnits = tranxSalesInvoiceDetailsUnitRepository.save(invoiceUnits);
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
                        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            TranxSalesInvoiceProductSrNumber productSerialNumber = new TranxSalesInvoiceProductSrNumber();
                            productSerialNumber.setProduct(mProduct);
                            productSerialNumber.setSerialNo(serialNo);
                            //productSerialNumber.setPurchaseTransaction(mPurchaseTranx);
                            productSerialNumber.setTransactionStatus("Sales");
                            productSerialNumber.setStatus(true);
                            productSerialNumber.setCreatedBy(userId);
                            productSerialNumber.setOperations("Inserted");
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setBranch(mSalesTranx.getBranch());
                            productSerialNumber.setOutlet(mSalesTranx.getOutlet());
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setUnits(units);
                            productSerialNumber.setTranxSalesInvoiceDetailsUnits(tranxSalesInvoiceDetailsUnits);
                            productSerialNumber.setLevelA(levelA);
                            productSerialNumber.setLevelB(levelB);
                            productSerialNumber.setLevelC(levelC);
                            productSerialNumber.setUnits(units);
                            TranxSalesInvoiceProductSrNumber mSerialNo = serialNumberRepository.save(productSerialNumber);
                            if (mProduct.getIsInventory() && !refType.equalsIgnoreCase("SLSCHN")) {
                                inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), serialNo);
                            }
                        }
                    }
                    flag = true;
                }
            } catch (Exception e) {
                salesInvoiceLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            /******* End of insert into Product Batch No ****/
            try {
               /* ProductUnitPacking productUnitPacking = productUnitRepository.findRate(mProduct.getId(),
                        levelAId, levelBId, levelCId, units.getId(), true);*/
                /**** Inventory Postings *****/
                stkQty = unitConversion.convertToLowerUnit(mProduct.getId(), units.getId(), (tranxQty + free_qty));

                if (mProduct.getIsInventory() && flag && !refType.equalsIgnoreCase("SLSCHN")) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), stkQty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), null);
                    /***** End of new architecture of Inventory Postings *****/

                    /**
                     * @implNote New Logic of opening and closing Inventory posting
                     * @auther ashwins@opethic.com
                     * @version sprint 1
                     **/
                    closingUtility.stockPosting(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId, mProduct, tranxType.getId(), mSalesTranx.getBillDate(), stkQty, 0.0, mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo, mSalesTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());
                    closingUtility.stockPostingBatchWise(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId, mProduct, tranxType.getId(), mSalesTranx.getBillDate(), stkQty, 0.0, mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo, mSalesTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());


                    /***** End of new logic of Inventory Postings *****/
                }
            } catch (Exception e) {
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            } /* End of inserting into TranxSalesInvoiceDetailsUnits */

            /* closing of sales quotation while converting into sales invoice using its qnt */
            double qty = object.get("qty").getAsDouble();
            if (object.has("reference_id") && !object.get("reference_id").getAsString().equalsIgnoreCase(""))
                referenceId = object.get("reference_id").getAsLong();
            if (referenceObj.equalsIgnoreCase("SLSQTN")) {
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
            } /* End of closing of sales quotation while converting into sales invoice
            using its qnt */

            /* closing of sales challan while converting into sales challan using its qnt */

            else if (referenceObj.equalsIgnoreCase("SLSORD")) {
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
            }
            /* End of  closing of sales order while converting into sales invoice
            using its qnt */

            /* closing of sales challan while converting into sales invoice using its qnt */

            else if (referenceObj.equalsIgnoreCase("SLSCHN")) {
                TranxSalesChallanDetailsUnits challanDetailsUnits = tranxSalesChallanDetailsUnitsRepository.findByProductDetailsLevel(referenceId, mProduct.getId(), units.getId(), levelAId, levelBId, levelCId, true);
                if (challanDetailsUnits != null) {
                    if (qty != challanDetailsUnits.getQty().doubleValue()) {
                        flag_status = true;
                        double totalQty = challanDetailsUnits.getQty().doubleValue() - qty;
                        challanDetailsUnits.setQty(totalQty);//push data into History table before update(remainding)
                        tranxSalesChallanDetailsUnitsRepository.save(challanDetailsUnits);
                    } else {
                        TransactionStatus transactionStatus = transactionStatusRepository.findById(2L).get();
                        challanDetailsUnits.setTransactionStatus(transactionStatus);
                        tranxSalesChallanDetailsUnitsRepository.save(challanDetailsUnits);
                    }
                }
            }
            /* End of  closing of sales challan while converting into sales invoice
            using its qnt */
        }

        if (referenceObj.equalsIgnoreCase("SLSQTN")) {
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
        } else if (referenceObj.equalsIgnoreCase("SLSORD")) {
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
        } else if (referenceObj.equalsIgnoreCase("SLSCHN")) {
            TranxSalesChallan tranxInvoice = tranxSalesChallanRepository.findByIdAndStatus(referenceId, true);
            if (tranxInvoice != null) {
                if (flag_status) {
                    TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
                    tranxInvoice.setTransactionStatus(transactionStatus);
                    tranxSalesChallanRepository.save(tranxInvoice);
                } else {
                    TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                    tranxInvoice.setTransactionStatus(transactionStatus);
                    tranxSalesChallanRepository.save(tranxInvoice);
                }
            }
        }
        /*** Closing of Counter Sales Invoice ****/
        else {
            TranxCounterSales tranxInvoice = counterSaleRepository.findByIdAndStatus(referenceId, true);
            if (tranxInvoice != null) {
                TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                tranxInvoice.setTransactionStatus(transactionStatus.getId());
                tranxInvoice.setIsBillConverted(true);
                counterSaleRepository.save(tranxInvoice);
            }
        }
    }

    /****** Save into Sales Comp Invoice Details ****/
    public void saveIntoCompSalesInvoiceDetails(JsonArray invoiceDetails, TranxSalesCompInvoice mSalesTranx, Branch branch, Outlet outlet, Long userId, TransactionTypeMaster tranxType, String salesType, String referenceObj) {
        String refType = "";
        boolean flag_status = false;
        Long referenceId = 0L;
        /* Sales Product Details Start here */
        List<TranxSalesCompInvoiceProductSrNumber> newSerialNumbers = new ArrayList<>();
        for (int i = 0; i < invoiceDetails.size(); i++) {
            JsonObject object = invoiceDetails.get(i).getAsJsonObject();
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);

            if (object.has("counterNo")) {
                refType = object.get("counterNo").getAsString();
                referenceId = object.get("details_id").getAsLong();
            }
            /* inserting into TranxSalesInvoiceDetailsUnits */
            String batchNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
            String serialNo = null;
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
            TranxSalesCompInvoiceDetailsUnits invoiceUnits = new TranxSalesCompInvoiceDetailsUnits();
            invoiceUnits.setSalesInvoiceId(mSalesTranx.getId());
            invoiceUnits.setProductId(mProduct.getId());
            invoiceUnits.setUnitsId(units.getId());
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            invoiceUnits.setFreeQty(0.0);
            if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setFreeQty(object.get("free_qty").getAsDouble());
            invoiceUnits.setRate(0.0);
            if (object.has("rate") && !object.get("rate").getAsString().equals(""))
                invoiceUnits.setRate(object.get("rate").getAsDouble());
            invoiceUnits.setStatus(true);
            if (levelA != null) invoiceUnits.setLevelAId(levelA.getId());
            if (levelB != null) invoiceUnits.setLevelBId(levelB.getId());
            if (levelC != null) invoiceUnits.setLevelCId(levelC.getId());
            invoiceUnits.setBaseAmt(0.0);
            if (object.has("base_amt") && !object.get("base_amt").getAsString().equals(""))
                invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv") && !object.get("unit_conv").getAsString().equals(""))
                invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            if (object.has("dis_amt")) invoiceUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            if (object.has("dis_per")) invoiceUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            if (object.has("dis_per2")) invoiceUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            invoiceUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
            invoiceUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
            if (object.has("add_chg_amt")) invoiceUnits.setAdditionChargesAmt(object.get("add_chg_amt").getAsDouble());
            invoiceUnits.setGrossAmt1(object.get("gross_amt1").getAsDouble());
            invoiceUnits.setInvoiceDisAmt(object.get("invoice_dis_amt").getAsDouble());
            if (object.has("dis_per_cal")) invoiceUnits.setDiscountPerCal(object.get("dis_per_cal").getAsDouble());
            if (object.has("dis_amt_cal")) invoiceUnits.setDiscountAmountCal(object.get("dis_amt_cal").getAsDouble());
            invoiceUnits.setTotalAmount(object.get("total_amt").getAsDouble());
            if (object.has("igst")) invoiceUnits.setIgst(object.get("igst").getAsDouble());
            if (object.has("sgst")) invoiceUnits.setSgst(object.get("sgst").getAsDouble());
            if (object.has("cgst")) invoiceUnits.setCgst(object.get("cgst").getAsDouble());
            if (object.has("total_igst")) invoiceUnits.setTotalIgst(object.get("total_igst").getAsDouble());
            if (object.has("total_sgst")) invoiceUnits.setTotalSgst(object.get("total_sgst").getAsDouble());
            if (object.has("total_cgst")) invoiceUnits.setTotalCgst(object.get("total_cgst").getAsDouble());
            if (object.has("final_amt")) invoiceUnits.setFinalAmount(object.get("final_amt").getAsDouble());
            invoiceUnits.setReturnQty(0.0);
            /******* Insert into Product Batch No ****/
            boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    Double free_qty = 0.0;
                    if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase(""))
                        free_qty = object.get("free_qty").getAsDouble();
                    double net_amt = object.get("final_amt").getAsDouble();
                    double costing = 0;
                    double costing_with_tax = 0;
                    if (object.get("b_details_id").getAsLong() == 0) {
                        ProductBatchNo mproductBatchNo = new ProductBatchNo();
                        Double qnty = object.get("qty").getAsDouble();
                        mproductBatchNo.setQnty(qnty.intValue());
                        mproductBatchNo.setFreeQty(free_qty);
                        if (object.has("b_no")) mproductBatchNo.setBatchNo(object.get("b_no").getAsString());
                        mproductBatchNo.setMrp(0.0);
                        if (object.has("b_rate") && !object.get("b_rate").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        mproductBatchNo.setPurchaseRate(0.0);
                        if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").getAsString().equals(""))
                            mproductBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase(""))
                            if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        mproductBatchNo.setSalesRate(0.0);
                        if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equals(""))
                            mproductBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                        if (object.has("costing") && !object.get("costing").isJsonNull())
                            mproductBatchNo.setCosting(object.get("costing").getAsDouble());
                        else mproductBatchNo.setCosting(0.0);
                        if (object.has("costing_with_tax") && !object.get("costing_with_tax").isJsonNull())
                            mproductBatchNo.setCostingWithTax(object.get("costing_with_tax").getAsDouble());
                        productBatchNo.setMinRateA(0.0);
                        productBatchNo.setMinRateB(0.0);
                        productBatchNo.setMinRateC(0.0);
                        mproductBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        mproductBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                        mproductBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                        if (object.has("margin_per"))
                            mproductBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
                        mproductBatchNo.setStatus(true);
                        mproductBatchNo.setProduct(mProduct);
                        mproductBatchNo.setOutlet(outlet);
                        mproductBatchNo.setBranch(branch);
                        mproductBatchNo.setUnits(units);
                        if (levelA != null) mproductBatchNo.setLevelA(levelA);
                        if (levelB != null) mproductBatchNo.setLevelB(levelB);
                        if (levelC != null) mproductBatchNo.setLevelC(levelC);
                        productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                    } else {
                        productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                        productBatchNo.setQnty(object.get("qty").getAsInt());
                        if (!object.get("free_qty").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setFreeQty(object.get("free_qty").getAsDouble());
                        productBatchNo.setCosting(costing);
                        productBatchNo.setCostingWithTax(costing_with_tax);
                        if (object.has("b_no")) productBatchNo.setBatchNo(object.get("b_no").getAsString());
                        if (object.has("b_rate")) productBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equals(""))
                            productBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                        if (object.has("b_purchase_rate"))
                            productBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                            productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") && !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                            productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        productBatchNo.setMinRateA(0.0);
                        productBatchNo.setMinRateB(0.0);
                        productBatchNo.setMinRateC(0.0);
                        productBatchNo.setMinMargin(0.0);
                        if (object.has("rate_a") && !object.get("rate_a").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        if (object.has("rate_b") && !object.get("rate_b").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                        if (object.has("rate_c") && !object.get("rate_c").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                        if (object.has("margin_per"))
                            productBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
                        productBatchNo.setStatus(true);
                        productBatchNo.setProduct(mProduct);
                        productBatchNo.setOutlet(outlet);
                        productBatchNo.setBranch(branch);
                        if (levelA != null) productBatchNo.setLevelA(levelA);
                        if (levelB != null) productBatchNo.setLevelB(levelB);
                        if (levelC != null) productBatchNo.setLevelC(levelC);
                        productBatchNo.setUnits(units);
                        productBatchNo = productBatchNoRepository.save(productBatchNo);
                    }
                    batchNo = productBatchNo.getBatchNo();
                    batchId = productBatchNo.getId();
                }
                invoiceUnits.setProductBatchNoId(productBatchNo.getId());
                TranxSalesCompInvoiceDetailsUnits tranxSalesCompInvoiceDetailsUnits = tranxSalesCompInvoiceDetailsUnitRepository.save(invoiceUnits);
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
                    /******* Insert into Tranx Product Serial Numbers  ******/
                    JsonArray jsonArray = object.getAsJsonArray("serialNo");
                    if (jsonArray != null && jsonArray.size() > 0) {
                        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            TranxSalesCompInvoiceProductSrNumber productSerialNumber = new TranxSalesCompInvoiceProductSrNumber();
                            productSerialNumber.setProductId(mProduct.getId());
                            productSerialNumber.setSerialNo(serialNo);
                            productSerialNumber.setTransactionStatus("Sales");
                            productSerialNumber.setStatus(true);
                            productSerialNumber.setCreatedBy(userId);
                            productSerialNumber.setOperations("Inserted");
                            productSerialNumber.setTransactionTypeMasterId(tranxType.getId());
                            productSerialNumber.setBranchId(mSalesTranx.getBranchId());
                            productSerialNumber.setOutletId(mSalesTranx.getOutletId());
                            productSerialNumber.setTransactionTypeMasterId(tranxType.getId());
                            productSerialNumber.setUnitsId(units.getId());
                            productSerialNumber.setTranxSalesCompInvoiceDetailsUnitsId(tranxSalesCompInvoiceDetailsUnits.getId());
                            productSerialNumber.setLevelAId(levelA.getId());
                            productSerialNumber.setLevelBId(levelB.getId());
                            productSerialNumber.setLevelCId(levelC.getId());
                            productSerialNumber.setUnitsId(units.getId());
                            TranxSalesCompInvoiceProductSrNumber mSerialNo = serialCompNumberRepository.save(productSerialNumber);
                            if (mProduct.getIsInventory()) {
                                FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
                                inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble(), branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, fiscalYear, serialNo);
                            }
                        }
                    }
                    flag = true;
                }
            } catch (Exception e) {
                salesInvoiceLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            /******* End of insert into Product Batch No ****/
            try {
                /**** Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
                    inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble(), branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, fiscalYear, null);
                    /***** End of new architecture of Inventory Postings *****/
                }
            } catch (Exception e) {
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            } /* End of inserting into TranxSalesInvoiceDetailsUnits */
            if (referenceId != 0L) {
                TranxCounterSalesDetailsUnits detailsUnits = tranxCSDetailsUnitsRepository.findByIdAndStatus(referenceId, true);
                detailsUnits.setTransactionStatus(2L);
                tranxCSDetailsUnitsRepository.save(detailsUnits);
            }
        }
    }


    /****** update into Sales Comp Invoice Details ****/
    public void updateIntoCompSalesInvoiceDetails(JsonArray invoiceDetails, TranxSalesCompInvoice mSalesTranx, Branch branch, Outlet outlet, Long userId, TransactionTypeMaster tranxType, String salesType, String referenceObj) {
        String refType = "";
        boolean flag_status = false;
        Long referenceId = 0L;
        /* Sales Product Details Start here */
        List<TranxSalesCompInvoiceProductSrNumber> newSerialNumbers = new ArrayList<>();
        for (int i = 0; i < invoiceDetails.size(); i++) {
            JsonObject object = invoiceDetails.get(i).getAsJsonObject();
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);

            if (object.has("reference_type")) {
                refType = object.get("reference_type").getAsString();
            }
            /* inserting into TranxSalesInvoiceDetailsUnits */
            String batchNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
            String serialNo = null;
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
            TranxSalesCompInvoiceDetailsUnits invoiceUnits = null;

            if (object.has("details_id") && object.get("details_id").getAsLong() != 0)
                invoiceUnits = tranxSalesCompInvoiceDetailsUnitRepository.findByIdAndStatus(object.get("details_id").getAsLong(), true);
            else invoiceUnits = new TranxSalesCompInvoiceDetailsUnits();
            invoiceUnits.setSalesInvoiceId(mSalesTranx.getId());
            invoiceUnits.setProductId(mProduct.getId());
            invoiceUnits.setUnitsId(units.getId());
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            invoiceUnits.setFreeQty(0.0);
            if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setFreeQty(object.get("free_qty").getAsDouble());
            invoiceUnits.setRate(0.0);
            if (object.has("rate") && !object.get("rate").getAsString().equals(""))
                invoiceUnits.setRate(object.get("rate").getAsDouble());
            invoiceUnits.setStatus(true);
            if (levelA != null) invoiceUnits.setLevelAId(levelA.getId());
            if (levelB != null) invoiceUnits.setLevelBId(levelB.getId());
            if (levelC != null) invoiceUnits.setLevelCId(levelC.getId());
            invoiceUnits.setBaseAmt(0.0);
            if (object.has("base_amt") && !object.get("base_amt").getAsString().equals(""))
                invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv") && !object.get("unit_conv").getAsString().equals(""))
                invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            invoiceUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            invoiceUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            if (object.has("dis_per2")) invoiceUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            invoiceUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
            invoiceUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
            if (object.has("add_chg_amt")) invoiceUnits.setAdditionChargesAmt(object.get("add_chg_amt").getAsDouble());
            invoiceUnits.setGrossAmt1(object.get("gross_amt1").getAsDouble());
            invoiceUnits.setInvoiceDisAmt(object.get("invoice_dis_amt").getAsDouble());
            if (object.has("dis_per_cal")) invoiceUnits.setDiscountPerCal(object.get("dis_per_cal").getAsDouble());
            if (object.has("dis_amt_cal")) invoiceUnits.setDiscountAmountCal(object.get("dis_amt_cal").getAsDouble());
            invoiceUnits.setTotalAmount(object.get("total_amt").getAsDouble());
            invoiceUnits.setIgst(object.get("igst").getAsDouble());
            invoiceUnits.setSgst(object.get("sgst").getAsDouble());
            invoiceUnits.setCgst(object.get("cgst").getAsDouble());
            invoiceUnits.setTotalIgst(object.get("total_igst").getAsDouble());
            invoiceUnits.setTotalSgst(object.get("total_sgst").getAsDouble());
            invoiceUnits.setTotalCgst(object.get("total_cgst").getAsDouble());
            invoiceUnits.setFinalAmount(object.get("final_amt").getAsDouble());
            invoiceUnits.setReturnQty(0.0);
            /******* Insert into Product Batch No ****/
            boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    int qty = object.get("qty").getAsInt();
                    double free_qty = 0.0;
                    if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase(""))
                        free_qty = object.get("free_qty").getAsInt();
                    double net_amt = object.get("final_amt").getAsDouble();
                    double costing = 0;
                    double costing_with_tax = 0;

                    if (object.get("b_details_id").getAsLong() == 0) {
                        ProductBatchNo mproductBatchNo = new ProductBatchNo();
                        mproductBatchNo.setQnty(object.get("qty").getAsInt());
                        mproductBatchNo.setFreeQty(free_qty);
                        if (object.has("b_no")) mproductBatchNo.setBatchNo(object.get("b_no").getAsString());
                        mproductBatchNo.setMrp(0.0);
                        if (object.has("b_rate") && !object.get("b_rate").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        mproductBatchNo.setPurchaseRate(0.0);
                        if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").getAsString().equals(""))
                            mproductBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase(""))
                            if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        mproductBatchNo.setSalesRate(0.0);
                        if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equals(""))
                            mproductBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                        if (object.has("costing") && !object.get("costing").isJsonNull())
                            mproductBatchNo.setCosting(object.get("costing").getAsDouble());
                        else mproductBatchNo.setCosting(0.0);
                        if (object.has("costing_with_tax") && !object.get("costing_with_tax").isJsonNull())
                            mproductBatchNo.setCostingWithTax(object.get("costing_with_tax").getAsDouble());
                        productBatchNo.setMinRateA(0.0);
                        productBatchNo.setMinRateB(0.0);
                        productBatchNo.setMinRateC(0.0);
                        mproductBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        mproductBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                        mproductBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                        if (object.has("margin_per"))
                            mproductBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
                        mproductBatchNo.setStatus(true);
                        mproductBatchNo.setProduct(mProduct);
                        mproductBatchNo.setOutlet(outlet);
                        mproductBatchNo.setBranch(branch);
                        mproductBatchNo.setUnits(units);
                        if (levelA != null) mproductBatchNo.setLevelA(levelA);
                        if (levelB != null) mproductBatchNo.setLevelB(levelB);
                        if (levelC != null) mproductBatchNo.setLevelC(levelC);
                        productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                    } else {
                        productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                        productBatchNo.setQnty(object.get("qty").getAsInt());
                        if (!object.get("free_qty").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setFreeQty(object.get("free_qty").getAsDouble());
                        productBatchNo.setCosting(costing);
                        productBatchNo.setCostingWithTax(costing_with_tax);
                        if (object.has("b_no")) productBatchNo.setBatchNo(object.get("b_no").getAsString());
                        if (object.has("b_rate")) productBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equals(""))
                            productBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                        if (object.has("b_purchase_rate"))
                            productBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                            productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") && !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                            productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        productBatchNo.setMinRateA(0.0);
                        productBatchNo.setMinRateB(0.0);
                        productBatchNo.setMinRateC(0.0);
                        productBatchNo.setMinMargin(0.0);
                        if (object.has("rate_a") && !object.get("rate_a").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        if (object.has("rate_b") && !object.get("rate_b").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                        if (object.has("rate_c") && !object.get("rate_c").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                        if (object.has("margin_per"))
                            productBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
                        productBatchNo.setStatus(true);
                        productBatchNo.setProduct(mProduct);
                        productBatchNo.setOutlet(outlet);
                        productBatchNo.setBranch(branch);
                        if (levelA != null) productBatchNo.setLevelA(levelA);
                        if (levelB != null) productBatchNo.setLevelB(levelB);
                        if (levelC != null) productBatchNo.setLevelC(levelC);
                        productBatchNo.setUnits(units);
                        productBatchNo = productBatchNoRepository.save(productBatchNo);
                    }
                    batchNo = productBatchNo.getBatchNo();
                    batchId = productBatchNo.getId();
                }
                invoiceUnits.setProductBatchNoId(productBatchNo.getId());
                TranxSalesCompInvoiceDetailsUnits tranxSalesCompInvoiceDetailsUnits = tranxSalesCompInvoiceDetailsUnitRepository.save(invoiceUnits);
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
                        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            TranxSalesCompInvoiceProductSrNumber productSerialNumber = new TranxSalesCompInvoiceProductSrNumber();
                            productSerialNumber.setProductId(mProduct.getId());
                            productSerialNumber.setSerialNo(serialNo);
                            //productSerialNumber.setPurchaseTransaction(mPurchaseTranx);
                            productSerialNumber.setTransactionStatus("Sales");
                            productSerialNumber.setStatus(true);
                            productSerialNumber.setCreatedBy(userId);
                            productSerialNumber.setOperations("Inserted");
                            productSerialNumber.setTransactionTypeMasterId(tranxType.getId());
                            productSerialNumber.setBranchId(mSalesTranx.getBranchId());
                            productSerialNumber.setOutletId(mSalesTranx.getOutletId());
                            productSerialNumber.setTransactionTypeMasterId(tranxType.getId());
                            productSerialNumber.setUnitsId(units.getId());
                            productSerialNumber.setTranxSalesCompInvoiceDetailsUnitsId(tranxSalesCompInvoiceDetailsUnits.getId());
                            productSerialNumber.setLevelAId(levelA.getId());
                            productSerialNumber.setLevelBId(levelB.getId());
                            productSerialNumber.setLevelCId(levelC.getId());
                            productSerialNumber.setUnitsId(units.getId());
                            TranxSalesCompInvoiceProductSrNumber mSerialNo = serialCompNumberRepository.save(productSerialNumber);
                            if (mProduct.getIsInventory()) {
                                FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
                                inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble(), branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, fiscalYear, serialNo);
                            }
                        }
                    }
                    flag = true;
                }
            } catch (Exception e) {
                salesInvoiceLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            /******* End of insert into Product Batch No ****/
            try {
                /**** Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
                    inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble(), branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, fiscalYear, null);
                    /***** End of new architecture of Inventory Postings *****/
                }
            } catch (Exception e) {
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            } /* End of inserting into TranxSalesInvoiceDetailsUnits */

        }
    }

    /* Close All Sales Quotations which are converted into Invoice */
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
    } /* End of Closing all Sales Quotations to invoice  */

    /* Close All Sales Orders which are converted into Invoice */
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
    } /* End of Closing all Sales Orders to invoice  */

    /* Close All Sales Orders which are converted into Invoice */
    public void setCloseSC(String sqIds) {
        Boolean flag = false;
        String idList[];
        idList = sqIds.split(",");
        for (String mId : idList) {
            TranxSalesChallan tranxSalesOrder = tranxSalesChallanRepository.findByIdAndStatus(Long.parseLong(mId), true);
            if (tranxSalesOrder != null) {
                tranxSalesOrder.setStatus(false);
                tranxSalesChallanRepository.save(tranxSalesOrder);
            }
        }
    } /* End of Closing all Sales Challans to invoice  */

    /* Posting into Sundry Debtors */
    private void insertIntoTranxDetailSD(TranxSalesInvoice mSalesTranx, TransactionTypeMaster tranxType, String newReference, String balance) {
        try {
            if (newReference != null && newReference.equalsIgnoreCase("yes")) {
                double bal = Double.parseDouble(balance);
                double closingBalance = mSalesTranx.getTotalAmount() - bal;
                String tranxAction = "CR";
                if (closingBalance >= 0.0) {
                    tranxAction = "DR";
                    /**** New Postings Logic *****/
                    /*    ledgerCommonPostings.callToPostings(closingBalance, mSalesTranx.getSundryDebtors(), tranxType, mSalesTranx.getSundryDebtors().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "DR", true, "Sales Invoice", "Insert");*/
                    ledgerCommonPostings.callToPostingsTranxCode(closingBalance, mSalesTranx.getSundryDebtors(), tranxType, mSalesTranx.getSundryDebtors().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "DR", true, "Sales Invoice", "Insert", mSalesTranx.getTranxCode());

                } else {
                    /**** New Postings Logic *****/
//                    ledgerCommonPostings.callToPostings(closingBalance, mSalesTranx.getSundryDebtors(), tranxType, mSalesTranx.getSundryDebtors().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Insert");
                    ledgerCommonPostings.callToPostingsTranxCode(closingBalance, mSalesTranx.getSundryDebtors(), tranxType, mSalesTranx.getSundryDebtors().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Insert", mSalesTranx.getTranxCode());
                }

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.callToPostingLedger(tranxType, tranxAction, closingBalance, mSalesTranx.getFiscalYear(), mSalesTranx.getSundryDebtors(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
            } else {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostingsTranxCode(mSalesTranx.getTotalAmount(), mSalesTranx.getSundryDebtors(), tranxType, mSalesTranx.getSundryDebtors().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "DR", true, "Sales Invoice", "Insert", mSalesTranx.getTranxCode());

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.callToPostingLedger(tranxType, "DR", mSalesTranx.getTotalAmount(), mSalesTranx.getFiscalYear(), mSalesTranx.getSundryDebtors(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in insertIntoTranxDetailSD :->" + e.getMessage());

            System.out.println("Store Procedure Error " + e.getMessage());
        }
    }/* End of Posting into Sundry Debtors */


    private void insertIntoCompTranxDetailSD(TranxSalesCompInvoice mSalesTranx, TransactionTypeMaster tranxType, String newReference, String balance) {
        try {
            LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getSundryDebtorsId(), true);
            Branch branch = null;
            if (mSalesTranx.getBranchId() != null)
                branch = branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(), true);

            Outlet outlet = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);
            FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();

            if (newReference != null && newReference.equalsIgnoreCase("yes")) {
                double bal = Double.parseDouble(balance);
                double closingBalance = mSalesTranx.getTotalAmount() - bal;
                fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
                String tranxAction = "CR";
                if (closingBalance >= 0.0) {
                    tranxAction = "DR";
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(closingBalance, sundryDebtors, tranxType, sundryDebtors.getAssociateGroups(), fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "DR", true, "Consumer Sales", "Insert");

                } else {
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(closingBalance, sundryDebtors, tranxType, sundryDebtors.getAssociateGroups(), fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "CR", true, "Consumer Sales", "Insert");
                }

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.callToPostingLedger(tranxType, tranxAction, closingBalance, fiscalYear, sundryDebtors, mSalesTranx.getBillDate(), mSalesTranx.getId(), outlet, branch, mSalesTranx.getTranxCode());
            } else {

                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(mSalesTranx.getTotalAmount(), sundryDebtors, tranxType, sundryDebtors.getAssociateGroups(), fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "DR", true, "Consumer Sales", "Insert");


                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.callToPostingLedger(tranxType, "DR", mSalesTranx.getTotalAmount(), fiscalYear, sundryDebtors, mSalesTranx.getBillDate(), mSalesTranx.getId(), outlet, branch, mSalesTranx.getTranxCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in insertIntoTranxDetailSD :->" + e.getMessage());

            System.out.println("Store Procedure Error " + e.getMessage());
        }
    }

    /* Posting into Sales Accounts */
    private void insertIntoTranxDetailSA(TranxSalesInvoice mSalesTranx, TransactionTypeMaster tranxType, String type, String operation, int gstType) {
        try {
            Double amount = mSalesTranx.getTaxableAmount();
            // transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(mSalesTranx.getSalesAccountLedger().getFoundations().getId(), mSalesTranx.getSalesAccountLedger().getPrinciples().getId(), mSalesTranx.getSalesAccountLedger().getPrincipleGroups() != null ? mSalesTranx.getSalesAccountLedger().getPrincipleGroups().getId() : null, mSalesTranx.getAssociateGroups() != null ? mSalesTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, mSalesTranx.getBranch() != null ? mSalesTranx.getBranch().getId() : null, mSalesTranx.getOutlet().getId(), "pending", 0.0, mSalesTranx.getTotalBaseAmount(), mSalesTranx.getBillDate(), null, mSalesTranx.getId(), tranxType.getTransactionName() + " Invoice", mSalesTranx.getSalesAccountLedger().getUnderPrefix(), mSalesTranx.getFinancialYear(), mSalesTranx.getCreatedBy(), mSalesTranx.getSalesAccountLedger().getId(), mSalesTranx.getSalesInvoiceNo());
            /**** New Postings Logic *****/
            if (gstType == 1) {
//                ledgerCommonPostings.callToPostings(mSalesTranx.getTaxableAmount(), mSalesTranx.getSalesAccountLedger(), tranxType, mSalesTranx.getSalesAccountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Sales Invoice", operation);
                ledgerCommonPostings.callToPostingsTranxCode(mSalesTranx.getTaxableAmount(), mSalesTranx.getSalesAccountLedger(), tranxType, mSalesTranx.getSalesAccountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Sales Invoice", operation, mSalesTranx.getTranxCode());

            } else if (gstType == 3) {
                amount = mSalesTranx.getTotalAmount();
//                ledgerCommonPostings.callToPostings(mSalesTranx.getTotalAmount(), mSalesTranx.getSalesAccountLedger(), tranxType, mSalesTranx.getSalesAccountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Sales Invoice", operation);
                ledgerCommonPostings.callToPostingsTranxCode(mSalesTranx.getTotalAmount(), mSalesTranx.getSalesAccountLedger(), tranxType, mSalesTranx.getSalesAccountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Sales Invoice", operation, mSalesTranx.getTranxCode());

            }

            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.callToPostingLedger(tranxType, type, amount, mSalesTranx.getFiscalYear(), mSalesTranx.getSalesAccountLedger(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in insertIntoTranxDetailSA :->" + e.getMessage());
        }

    }/* End of Posting into Sales Accounts */

    /* Posting into Sales Accounts */
    private void delPostingTranxDetailSA(TranxSalesInvoice mSalesTranx, TransactionTypeMaster tranxType, String type, String operation) {
        try {

            // transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(mSalesTranx.getSalesAccountLedger().getFoundations().getId(), mSalesTranx.getSalesAccountLedger().getPrinciples().getId(), mSalesTranx.getSalesAccountLedger().getPrincipleGroups() != null ? mSalesTranx.getSalesAccountLedger().getPrincipleGroups().getId() : null, mSalesTranx.getAssociateGroups() != null ? mSalesTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, mSalesTranx.getBranch() != null ? mSalesTranx.getBranch().getId() : null, mSalesTranx.getOutlet().getId(), "pending", 0.0, mSalesTranx.getTotalBaseAmount(), mSalesTranx.getBillDate(), null, mSalesTranx.getId(), tranxType.getTransactionName() + " Invoice", mSalesTranx.getSalesAccountLedger().getUnderPrefix(), mSalesTranx.getFinancialYear(), mSalesTranx.getCreatedBy(), mSalesTranx.getSalesAccountLedger().getId(), mSalesTranx.getSalesInvoiceNo());
            /**** New Postings Logic *****/
            if (mSalesTranx.getOutlet().getGstTypeMaster().getId() == 1)
                ledgerCommonPostings.callToPostings(mSalesTranx.getTaxableAmount(), mSalesTranx.getSalesAccountLedger(), tranxType, mSalesTranx.getSalesAccountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Sales Invoice", operation);
            else if (mSalesTranx.getOutlet().getGstTypeMaster().getId() == 3)
                ledgerCommonPostings.callToPostings(mSalesTranx.getTotalAmount(), mSalesTranx.getSalesAccountLedger(), tranxType, mSalesTranx.getSalesAccountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Sales Invoice", operation);
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in insertIntoTranxDetailSA :->" + e.getMessage());
        }

    }/* End of Posting into Sales Accounts */

    private void insertIntoCompTranxDetailSA(TranxSalesCompInvoice mSalesTranx, TransactionTypeMaster tranxType, String type, String operation) {
        try {

            // transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(mSalesTranx.getSalesAccountLedger().getFoundations().getId(), mSalesTranx.getSalesAccountLedger().getPrinciples().getId(), mSalesTranx.getSalesAccountLedger().getPrincipleGroups() != null ? mSalesTranx.getSalesAccountLedger().getPrincipleGroups().getId() : null, mSalesTranx.getAssociateGroups() != null ? mSalesTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, mSalesTranx.getBranch() != null ? mSalesTranx.getBranch().getId() : null, mSalesTranx.getOutlet().getId(), "pending", 0.0, mSalesTranx.getTotalBaseAmount(), mSalesTranx.getBillDate(), null, mSalesTranx.getId(), tranxType.getTransactionName() + " Invoice", mSalesTranx.getSalesAccountLedger().getUnderPrefix(), mSalesTranx.getFinancialYear(), mSalesTranx.getCreatedBy(), mSalesTranx.getSalesAccountLedger().getId(), mSalesTranx.getSalesInvoiceNo());
            LedgerMaster salesAccountLedger = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getSalesAccountLedgerId(), true);
            FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
            Branch branch = null;
            if (mSalesTranx.getBranchId() != null)
                branch = branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(), true);
            Outlet outlet = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getTaxableAmount(), salesAccountLedger, tranxType, salesAccountLedger.getAssociateGroups(), fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Consumer Sales", operation);

            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.callToPostingLedger(tranxType, type, mSalesTranx.getTaxableAmount(), fiscalYear, salesAccountLedger, mSalesTranx.getBillDate(), mSalesTranx.getId(), outlet, branch, mSalesTranx.getTranxCode());

        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in insertIntoTranxDetailSA :->" + e.getMessage());
        }

    }/* End of Posting into Sales Accounts */

    /* Posting into Sales Discount */
    private void insertIntoTranxDetailsSalesDiscount(TranxSalesInvoice mSalesTranx, TransactionTypeMaster tranxType, String type, String operation) {
        try {
            if (mSalesTranx.getSalesDiscountLedger() != null) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(mSalesTranx.getSalesDiscountAmount(), mSalesTranx.getSalesDiscountLedger(), tranxType, mSalesTranx.getSalesDiscountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Sales Invoice", operation);
            }
        } catch (Exception e) {
            salesInvoiceLogger.error("Error in insertIntoTranxDetailsSalesDiscount :->" + e.getMessage());
            System.out.println("Posting Discount Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }/* End of Posting into Sales Discount */

    /*  Posting into Sales Round off */
    private void insertIntoCompTranxDetailRO(TranxSalesCompInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        /**** New Postings Logic *****/
        LedgerMaster salesRoundOff = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getSalesRoundOffId(), true);
        FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
        Branch branch = null;
        if (mSalesTranx.getBranchId() != null)
            branch = branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(), true);
        Outlet outlet = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);

        if (mSalesTranx.getRoundOff() >= 0) {
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), salesRoundOff, tranxType, salesRoundOff != null ? salesRoundOff.getAssociateGroups() : null, fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "CR", true, "Consumer Sales", "Insert");
        } else if (mSalesTranx.getRoundOff() < 0) {
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), salesRoundOff, tranxType, salesRoundOff != null ? salesRoundOff.getAssociateGroups() : null, fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "DR", true, "Consumer Sales", "Insert");
        }
    }/* End Posting into Sales Round off */

    private void insertIntoTranxDetailRO(TranxSalesInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        String tranxAction = "CR";
        /**** New Postings Logic *****/
        if (mSalesTranx.getRoundOff() >= 0) {
//            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff() != null ? mSalesTranx.getSalesRoundOff().getAssociateGroups() : null, mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Insert");
            ledgerCommonPostings.callToPostingsTranxCode(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff() != null ? mSalesTranx.getSalesRoundOff().getAssociateGroups() : null, mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Insert", mSalesTranx.getTranxCode());
        } else if (mSalesTranx.getRoundOff() < 0) {
            tranxAction = "DR";
//            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff() != null ? mSalesTranx.getSalesRoundOff().getAssociateGroups() : null, mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "DR", true, "Sales Invoice", "Insert");
            ledgerCommonPostings.callToPostingsTranxCode(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff() != null ? mSalesTranx.getSalesRoundOff().getAssociateGroups() : null, mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "DR", true, "Sales Invoice", "Insert", mSalesTranx.getTranxCode());
        }

        /***** NEW METHOD FOR LEDGER POSTING *****/
        postingUtility.callToPostingLedger(tranxType, tranxAction, Math.abs(mSalesTranx.getRoundOff()), mSalesTranx.getFiscalYear(), mSalesTranx.getSalesRoundOff(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
    }/* End Posting into Sales Round off */

    /* Posting into Sales duties and taxes off */
    public void insertDB(TranxSalesInvoice mSalesTranx, String ledgerName, TransactionTypeMaster tranxType, String type, String operation) {
        try {
            /* Sale Duties Taxes */
            if (ledgerName.equalsIgnoreCase("DT")) {
                List<TranxSalesInvoiceDutiesTaxes> list = salesDutiesTaxesRepository.findBySalesTransactionAndStatus(mSalesTranx, true);
                for (TranxSalesInvoiceDutiesTaxes mDuties : list) {
                    insertFromDutiesTaxes(mDuties, mSalesTranx, tranxType, type, operation);
                }
            } else if (ledgerName.equalsIgnoreCase("AC")) {
                if (mSalesTranx.getAdditionLedger1() != null) {
                    ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt1(), mSalesTranx.getAdditionLedger1(), tranxType, mSalesTranx.getAdditionLedger1().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt1() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), operation);
                }
                if (mSalesTranx.getAdditionLedger2() != null) {
                    ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt2(), mSalesTranx.getAdditionLedger2(), tranxType, mSalesTranx.getAdditionLedger2().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt2() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), operation);
                }
                if (mSalesTranx.getAdditionLedger3() != null) {
                    ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt3(), mSalesTranx.getAdditionLedger3(), tranxType, mSalesTranx.getAdditionLedger3().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt3() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), operation);
                }
            }
        } catch (DataIntegrityViolationException e1) {
            salesInvoiceLogger.error("Error in insertIntoTranxDetailRO :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            salesInvoiceLogger.error("Error in insertIntoTranxDetailRO :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    } /* Posting into Sales duties and taxes off */

    public void insertCompDB(TranxSalesCompInvoice mSalesTranx, String ledgerName, TransactionTypeMaster tranxType, String type, String operation) {
        try {
            /* Sale Duties Taxes */
            if (ledgerName.equalsIgnoreCase("DT")) {
                List<TranxSalesCompInvoiceDutiesTaxes> list = salesCompDutiesTaxesRepository.findBySalesTransactionIdAndStatus(mSalesTranx.getId(), true);

                for (TranxSalesCompInvoiceDutiesTaxes mDuties : list) {
                    insertCompFromDutiesTaxes(mDuties, mSalesTranx, tranxType, type, operation);
                }
            } else if (ledgerName.equalsIgnoreCase("AC")) {
                LedgerMaster additionalLedger1 = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getAdditionLedger1Id(), true);
                LedgerMaster additionalLedger2 = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getAdditionLedger2Id(), true);
                LedgerMaster additionalLedger3 = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getAdditionLedger3Id(), true);
                FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
                Branch branch = null;
                if (mSalesTranx.getBranchId() != null)
                    branch = branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(), true);
                Outlet outlet = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);
                if (additionalLedger1 != null) {
                    ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt1(), additionalLedger1, tranxType, additionalLedger1.getAssociateGroups(), fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt1() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), operation);
                }
                if (additionalLedger2 != null) {
                    ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt2(), additionalLedger2, tranxType, additionalLedger2.getAssociateGroups(), fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt2() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), operation);
                }
                if (additionalLedger3 != null) {
                    ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt3(), additionalLedger3, tranxType, additionalLedger3.getAssociateGroups(), fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt3() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), operation);
                }
            }
        } catch (DataIntegrityViolationException e1) {
            salesInvoiceLogger.error("Error in insertIntoTranxDetailRO :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            salesInvoiceLogger.error("Error in insertIntoTranxDetailRO :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    } /* Posting into Sales duties and taxes off */

    private void insertFromDutiesTaxes(TranxSalesInvoiceDutiesTaxes mDuties, TranxSalesInvoice mSalesTranx, TransactionTypeMaster tranxType, String type, String operation) {
        /**** New Postings Logic *****/
//        ledgerCommonPostings.callToPostings(mDuties.getAmount(), mDuties.getDutiesTaxes(), tranxType, mDuties.getDutiesTaxes().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Sales Invoice", operation);
        ledgerCommonPostings.callToPostingsTranxCode(mDuties.getAmount(), mDuties.getDutiesTaxes(), tranxType, mDuties.getDutiesTaxes().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Sales Invoice", operation, mSalesTranx.getTranxCode());

        if (operation.equalsIgnoreCase("insert")) {
            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.callToPostingLedger(tranxType, type, mDuties.getAmount(), mSalesTranx.getFiscalYear(), mDuties.getDutiesTaxes(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
        }
    }

    private void insertCompFromDutiesTaxes(TranxSalesCompInvoiceDutiesTaxes mDuties, TranxSalesCompInvoice mSalesTranx, TransactionTypeMaster tranxType, String type, String operation) {
        /**** New Postings Logic *****/
        LedgerMaster dutiesTaxes = ledgerMasterRepository.findByIdAndStatus(mDuties.getDutiesTaxesId(), true);
        FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
        Branch branch = null;
        if (mSalesTranx.getBranchId() != null)
            branch = branchRepository.findByIdAndStatus(mSalesTranx.getBranchId(), true);
        Outlet outlet = outletRepository.findByIdAndStatus(mSalesTranx.getOutletId(), true);
        ledgerCommonPostings.callToPostings(mDuties.getAmount(), dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), fiscalYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, "Consumer Sales", operation);

        /***** NEW METHOD FOR LEDGER POSTING *****/
        postingUtility.callToPostingLedger(tranxType, type, mDuties.getAmount(), fiscalYear, dutiesTaxes, mSalesTranx.getBillDate(), mSalesTranx.getId(), outlet, branch, mSalesTranx.getTranxCode());

    }

    private void insertFromAdditionalCharges(TranxSalesInvoiceAdditionalCharges mAdditinoalCharges, TranxSalesInvoice mSalesTranx, TransactionTypeMaster tranxType, String type, String operation) {
        /**** New Postings Logic *****/
        /* Purchase Additional Charges */
        if (mSalesTranx.getAdditionLedger1() != null) {
            ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt1(), mSalesTranx.getAdditionLedger1(), tranxType, mSalesTranx.getAdditionLedger1().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, tranxType.getTransactionCode(), operation);
        }
        if (mSalesTranx.getAdditionLedger2() != null) {
            ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt2(), mSalesTranx.getAdditionLedger2(), tranxType, mSalesTranx.getAdditionLedger2().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, tranxType.getTransactionCode(), operation);
        }
        if (mSalesTranx.getAdditionLedger3() != null) {
            ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt3(), mSalesTranx.getAdditionLedger3(), tranxType, mSalesTranx.getAdditionLedger3().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), type, true, tranxType.getTransactionCode(), operation);
        }
    }

    public JsonObject saleInvoiceList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesInvoice> tranxSalesInvoices = salesTransactionRepository.findByOutletIdAndStatus(users.getOutlet().getId(), true);
        for (TranxSalesInvoice invoices : tranxSalesInvoices) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("bill_no", invoices.getSalesInvoiceNo());
            response.addProperty("bill_date", invoices.getBillDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("total_base_amount", invoices.getTotalBaseAmount());
            response.addProperty("sundry_debtors_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtors_id", invoices.getSundryDebtors().getId());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

//    public JsonObject getCounterSales(HttpServletRequest request) {
//        List<TranxCounterSales> cList = new ArrayList<>();
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        JsonObject newResponse = new JsonObject();
//        Map<String, String[]> paramMap = request.getParameterMap();
//        String startDate = "";
//        String endDate = "";
//        LocalDate endDatep = null;
//        LocalDate startDatep = null;
//        Boolean flag = false;
//        if (paramMap.containsKey("startDate") && paramMap.containsKey("endDate")) {
//            startDate = request.getParameter("startDate");
//            startDatep = LocalDate.parse(startDate);
//            endDate = request.getParameter("endDate");
//            endDatep = LocalDate.parse(endDate);
//            flag = true;
//        }
//        if (flag == true) {
//
//            if (users.getBranch() != null) {
//                cList = counterSaleRepository.findXounterSaleListWithDate(users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, false, true);
//            } else {
//                cList = counterSaleRepository.findXounterSaleListWithDateNoBr(users.getOutlet().getId(), startDatep, endDatep, false, true);
//            }
//
//
//        } else {
//
//            if (users.getBranch() != null) {
//                cList = counterSaleRepository.findByIsBillConvertedAndOutletIdAndBranchIdAndStatus(false, users.getOutlet().getId(), users.getBranch().getId(), true);
//            } else {
//                cList = counterSaleRepository.findByIsBillConvertedAndOutletIdAndStatusAndBranchIsNull(false, users.getOutlet().getId(), true);
//            }
//
//        }
//
//        JsonArray result = new JsonArray();
//        if (cList.size() > 0) {
//            for (TranxCounterSales mList : cList) {
//                JsonObject response = new JsonObject();
//                response.addProperty("id", mList.getId());
//                response.addProperty("invoice_no", mList.getCounterSaleNo());
//                response.addProperty("customer_name", mList.getCustomerName() != null ? mList.getCustomerName() : "");
//                response.addProperty("total_amount", mList.getTotalBill());
//                response.addProperty("transaction_date", mList.getTransactionDate().toString());
//                response.addProperty("mobile_no", mList.getMobileNumber());
//                response.addProperty("narrations", mList.getNarrations());
//                Long totalProducts = tranxCSDetailsUnitsRepository.findTotalProducts(mList.getId());
//                response.addProperty("total_products", totalProducts);
//                response.addProperty("taxable", mList.getTaxableAmt());
//                response.addProperty("gst", mList.getTotaligst());
//
//                result.add(response);
//            }
//            newResponse.addProperty("message", "success");
//            newResponse.addProperty("responseStatus", HttpStatus.OK.value());
//            newResponse.add("list", result);
//        } else {
//            newResponse.addProperty("message", "empty list");
//            newResponse.addProperty("responseStatus", HttpStatus.OK.value());
//            newResponse.add("list", result);
//        }
//        return newResponse;
//    }

    //start of Counter sale list with pagination
    public Object getCounterSales(@RequestBody Map<String, String> request, HttpServletRequest req) {
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
        List<TranxCounterSales> saleCounter = new ArrayList<>();
        List<TranxCounterSales> saleArrayList = new ArrayList<>();
        List<SalesCounterDTO> salesCounterDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_counter_sales_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += " AND DATE(bill_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND narration LIKE '%" + searchText + " AND sales_invoice_no LIKE '%" + searchText + " AND bill_date LIKE '%" + searchText + " AND total_amount LIKE '%" + searchText + "%'";
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
                query = query + " ORDER BY counter_sale_no ASC";
            }
            String query1 = query;       //we get all lists in this list
            System.out.println("query== " + query);
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            Query q = entityManager.createNativeQuery(query, TranxCounterSales.class);
            System.out.println("q ==" + q + "  saleInvoice " + saleCounter);
            saleCounter = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxCounterSales.class);

            saleArrayList = q1.getResultList();
            System.out.println("Limit total rows " + saleArrayList.size());
            Integer total_pages = (saleArrayList.size() / pageSize);
            if ((saleArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (TranxCounterSales orderListView : saleCounter) {
                salesCounterDTOList.add(convertToDTDTO(orderListView));
            }
            GenericDatatable<SalesCounterDTO> data = new GenericDatatable<>(salesCounterDTOList, saleArrayList.size(), pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(salesCounterDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }
    //end of counter sale list with pagination

    //Start of DTO for Counter sale
    private SalesCounterDTO convertToDTDTO(TranxCounterSales tranxCounterSales) {
        SalesCounterDTO salesCounterDTO = new SalesCounterDTO();

        salesCounterDTO.setId(tranxCounterSales.getId());
        salesCounterDTO.setInvoice_no(tranxCounterSales.getCounterSaleNo());
        salesCounterDTO.setCustomer_name(tranxCounterSales.getCustomerName());
        salesCounterDTO.setTotal_amount(tranxCounterSales.getTotalBill());
        salesCounterDTO.setTransaction_date(tranxCounterSales.getTransactionDate().toString());

        salesCounterDTO.setMobile_no(tranxCounterSales.getMobileNumber());
        salesCounterDTO.setNarrations(tranxCounterSales.getNarrations());
//       salesCounterDTO.setTotal_products(tranxCounterSales.getTotalqty());     //for total quantity
        salesCounterDTO.setTaxable(tranxCounterSales.getTaxableAmt());
        salesCounterDTO.setGst(tranxCounterSales.getTotaligst());

        return salesCounterDTO;

    }
    //End of DTO for COunter sale

    /* getting last records of Sales Invoices */
    public JsonObject salesInvoiceLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        Long branchId = null;
        if (users.getBranch() != null) {
            branchId = users.getBranch().getId();
        }
        if (branchId != null) {
            count = salesTransactionRepository.findLastRecordWithBranch(users.getOutlet().getId(), branchId);
        } else {
            count = salesTransactionRepository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String siCode = "SI" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("serialNo", siCode);
        return result;
    }


    /* getting the last record of Consumer Sales Invoice */
    public JsonObject consumerSalesLastRecord(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = salesCompTransactionRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = salesCompTransactionRepository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
       /* String companyName = users.getOutlet().getCompanyName();
        companyName = companyName.substring(0, 3);*/ // fetching first 3 digits from company names
        /* getting Start and End year from fiscal Year */
/*
        String startYear = generateFiscalYear.getStartYear();
        String endYear = generateFiscalYear.getEndYear();
*/
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
    /*    String csCode = companyName.toUpperCase() + "-" + startYear + endYear
                + "-" + "CS" + currentMonth + "-" + serailNo;*/
        String csCode = "CS" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("serialNo", csCode);
        return result;
    }

    //API
    public JsonObject AllTransactionsaleList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        Integer from = Integer.parseInt(request.getParameter("from"));
//        Integer to = Integer.parseInt(request.getParameter("to"));
        List<TranxSalesInvoice> saleInvoice = new ArrayList<>();
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

            saleInvoice = salesTransactionRepository.findSaleListWithDate(users.getOutlet().getId(), startDatep, endDatep, true);


        } else {

            saleInvoice = salesTransactionRepository.findByOutletIdAndIsCounterSaleAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), false, true);

        }

        for (TranxSalesInvoice invoices : saleInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getSalesInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(invoices.getBillDate()).toString());
            response.addProperty("sale_serial_number", invoices.getSalesSerialNumber());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_debtor_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtor_id", invoices.getSundryDebtors().getId());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration() != null ? invoices.getNarration() : "");
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTaxableAmount());
            response.addProperty("payment_mode", invoices.getPaymentMode());
            response.addProperty("invoice_id", invoices.getId());
            response.addProperty("orderStatus", invoices.getOrderStatus());
            Double invoiceDetailsUnits = tranxSalesInvoiceDetailsUnitRepository.totalinvoiceNumberOfProduct(invoices.getId(), 1, true);
            response.addProperty("product_count", invoiceDetailsUnits);
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }


    //API All Dispatch Mangement List
    public JsonObject AllDispatchMangList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonArray result1 = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesChallan> tranxSalesChallans = new ArrayList<>();
        //        Integer from = Integer.parseInt(request.getParameter("from"));
//        Integer to = Integer.parseInt(request.getParameter("to"));
        List<TranxSalesInvoice> saleInvoice = new ArrayList<>();
        JsonObject output = new JsonObject();
        Map<String, String[]> paramMap = request.getParameterMap();
        String startDate = "";
        String endDate = "";
        LocalDate endDatep = null;
        LocalDate startDatep = null;

        Branch branch = null;
        Outlet outlet = null;
        LedgerMaster sundryDebtors = null;
        LedgerMaster salesAccountLedger = null;
        LedgerMaster salesRoundOff = null;
        Boolean flag = false;
        if (paramMap.containsKey("startDate") && paramMap.containsKey("endDate")) {
            startDate = request.getParameter("startDate");
            startDatep = LocalDate.parse(startDate);
            endDate = request.getParameter("endDate");
            endDatep = LocalDate.parse(endDate);
            flag = true;
        }

        if (flag == true) {
            //sales invoice
            saleInvoice = salesTransactionRepository.findSaleListWithDate(users.getOutlet().getId(), startDatep, endDatep, true);
            //sales challan
            if (users.getBranch() != null) {
                tranxSalesChallans = salesTransactionChallanRepository.findSaleChallanListWithDateWithBr(users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, true);
            } else {
                tranxSalesChallans = salesTransactionChallanRepository.findSaleChallanListWithDate(users.getOutlet().getId(), startDatep, endDatep, true);
            }


        } else {
            //sales invoice
            saleInvoice = salesTransactionRepository.findByOutletIdAndIsCounterSaleAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), false, true);

            //sales challan
            if (users.getBranch() != null) {
                tranxSalesChallans = salesTransactionChallanRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                tranxSalesChallans = salesTransactionChallanRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
            }

        }

        //Sales Challan
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
            result1.add(response);
        }
        output.add("ChallanData", result1);

        //sales Order
        List<TranxSalesOrder> quotations = tranxSalesOrderRepository.findAll();
        JsonArray jsonArrayOrder = new JsonArray();
        if (!quotations.isEmpty()) {

            for (int i = 0; i < quotations.size(); i++) {
                JsonObject jsonObject = new JsonObject();
                TranxSalesOrder salesOrderTransaction = quotations.get(i);

                if (salesOrderTransaction.getBranch() != null) branch = salesOrderTransaction.getBranch();
                outlet = salesOrderTransaction.getOutlet();
                sundryDebtors = salesOrderTransaction.getSundryDebtors();
                salesAccountLedger = salesOrderTransaction.getSalesAccountLedger();
                salesRoundOff = salesOrderTransaction.getSalesRoundOff();
//                salesQuotationInvoiceDetails = salesQuotationInvoiceDetailsRepository.findByTranxSalesQuotationId(salesOrderTransaction.getId());
//                salesQuotationDutiesTaxes = salesOrderTransaction.getSalesQuotationDutiesTaxes();

                jsonObject.addProperty("bill_date", DateConvertUtil.convertDateToLocalDate(salesOrderTransaction.getBillDate()).toString());
                jsonObject.addProperty("bill_no", salesOrderTransaction.getSo_bill_no());
                jsonObject.addProperty("sales_acc_id", salesAccountLedger.getId());
                jsonObject.addProperty("transaction_dt", DateConvertUtil.convertDateToLocalDate(salesOrderTransaction.getBillDate()).toString());
                jsonObject.addProperty("debtors_id", sundryDebtors.getId());
                jsonObject.addProperty("sundry_debtor_name", sundryDebtors.getLedgerName());
                jsonObject.addProperty("roundoff", salesOrderTransaction.getRoundOff());
                jsonObject.addProperty("narration", "NA");
                jsonObject.addProperty("total_base_amt", salesOrderTransaction.getTotalBaseAmount());
                jsonObject.addProperty("total_amount", salesOrderTransaction.getTotalAmount());
                jsonObject.addProperty("taxable_amount", salesOrderTransaction.getTaxableAmount());
                jsonObject.addProperty("totalcgst", salesOrderTransaction.getTotalcgst());
                jsonObject.addProperty("totalsgst", salesOrderTransaction.getTotalsgst());
                jsonObject.addProperty("totaligst", salesOrderTransaction.getTotaligst());
                jsonObject.addProperty("totalqty", salesOrderTransaction.getTotalqty());
                jsonObject.addProperty("tcs", salesOrderTransaction.getTcs());
                jsonObject.addProperty("additionalChargesTotal", salesOrderTransaction.getAdditionalChargesTotal());
                jsonObject.addProperty("totalqty", salesOrderTransaction.getTotalqty());
                jsonObject.addProperty("sale_type", "sales quotation");
                jsonObject.addProperty("order_id", salesOrderTransaction.getId());
                jsonObject.addProperty("orderStatus", salesOrderTransaction.getOrderStatus());
                Double orderDetailsUnits = tranxSalesOrderDetailsUnitsRepository.totalNumberOfProduct(salesOrderTransaction.getId(), 1, true);
                jsonObject.addProperty("product_count", orderDetailsUnits);
                jsonArrayOrder.add(jsonObject);
            }
        }
        output.add("OrderData", jsonArrayOrder);

        //sales Invoice
        for (TranxSalesInvoice invoices : saleInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getSalesInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(invoices.getBillDate()).toString());
            response.addProperty("sale_serial_number", invoices.getSalesSerialNumber());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_debtor_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtor_id", invoices.getSundryDebtors().getId());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration() != null ? invoices.getNarration() : "");
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTaxableAmount());
            response.addProperty("payment_mode", invoices.getPaymentMode());
            response.addProperty("invoice_id", invoices.getId());
            response.addProperty("orderStatus", invoices.getOrderStatus());
            Double invoiceDetailsUnits = tranxSalesInvoiceDetailsUnitRepository.totalinvoiceNumberOfProduct(invoices.getId(), 1, true);
            response.addProperty("product_count", invoiceDetailsUnits);
            result.add(response);
        }

        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //    Api for GSTR1 B2C sales outward data
    public Object GSTR1SaleInvoiceDetails(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        System.out.println("request " + request + "  req=" + req);
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");
        Long debtoryId = Long.parseLong(request.get("debtor_id"));
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        Boolean flag = false;
        List saleInvoice = new ArrayList<>();
        List<TranxSalesInvoice> saleArrayList = new ArrayList<>();
        List<SalesInvoiceDTO> salesInvoiceDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT id FROM `tranx_sales_invoice_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND " + "status=1 AND sundry_debtors_id=" + debtoryId;
//        String query = "SELECT id FROM `tranx_sales_invoice_tbl";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += " AND DATE(bill_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND (bill_date LIKE '%" + searchText + "%'" + "OR sales_invoice_no LIKE '%" + searchText + "%'" + "OR total_amount LIKE '%" + searchText + "%'" + "OR total_base_amount LIKE '%" + searchText + "%'" + "OR total_tax LIKE '%" + searchText + "%'" + "OR totaligst LIKE '%" + searchText + "%'" + "OR totalcgst LIKE '%" + searchText + "%'" + "OR totalsgst LIKE '%" + searchText + "%')";
            }
            String jsonToStr = request.get("sort");
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
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

            Query q = entityManager.createNativeQuery(query);
            saleInvoice = q.getResultList();

            for (Object mList : saleInvoice) {
                TranxSalesInvoice invoiceListView = salesTransactionRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                salesInvoiceDTOList.add(convertToDTDTO(invoiceListView));
            }
            responseMessage.setResponseObject(salesInvoiceDTOList);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            genericDTData.setRows(salesInvoiceDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    public Object GSTR1B2CLSaleInvoiceDetails(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        System.out.println("request " + request + "  req=" + req);
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");
        Long state_id = Long.parseLong(request.get("state_id"));
        Double tax_rate = Double.parseDouble(request.get("tax_rate"));
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        Boolean flag = false;
        List saleInvoice = new ArrayList<>();
        List<TranxSalesInvoice> saleArrayList = new ArrayList<>();
        List<SalesInvoiceDTO> salesInvoiceDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT tsi.id FROM `tranx_sales_invoice_tbl` AS tsi LEFT JOIN ledger_master_tbl" + " ON tsi.sundry_debtors_id = ledger_master_tbl.id LEFT JOIN" + " tranx_sales_invoice_details_units_tbl AS tsidu ON tsi.id = tsidu.sales_invoice_id" + " WHERE tsi.status=1 AND tsi.total_amount > 250000 AND ledger_master_tbl.taxable=0 AND tsidu.igst=" + tax_rate + " AND ledger_master_tbl.state_id=" + state_id;

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += " AND DATE(bill_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
//
                query = query + " AND (bill_date LIKE '%" + searchText + "%'" + "OR sales_invoice_no LIKE '%" + searchText + "%'" + "OR ledger_master_tbl.ledger_name LIKE '%" + searchText + "%'" + "OR tsi.total_amount LIKE '%" + searchText + "%'" + "OR tsi.total_base_amount LIKE '%" + searchText + "%'" + "OR tsi.total_tax LIKE '%" + searchText + "%'" + "OR tsi.totaligst LIKE '%" + searchText + "%'" + "OR tsi.totalcgst LIKE '%" + searchText + "%'" + "OR tsi.totalsgst LIKE '%" + searchText + "%'" + "OR tsi.total_amount LIKE '%" + searchText + "%' )";
            }
            String jsonToStr = request.get("sort");
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
                String sortBy = jsonObject.get("colId").getAsString();
                query = query + " ORDER BY " + sortBy;
                if (jsonObject.get("isAsc").getAsBoolean() == true) {
                    query = query + " ASC";
                } else {
                    query = query + " DESC";
                }
            } else {
                query = query + " ORDER BY tsidu.sales_invoice_id DESC";
            }
            salesInvoiceLogger.info("sql :" + query);
            System.out.println("sql :" + query);
            Query q = entityManager.createNativeQuery(query);
//            q.setParameter(1, tax_rate);
//            q.setParameter(2, state_code);
//            q.setParameter(3, 3);
            saleInvoice = q.getResultList();

            for (Object mList : saleInvoice) {
                TranxSalesInvoice invoiceListView = salesTransactionRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                salesInvoiceDTOList.add(convertToDTDTO(invoiceListView));
            }
            responseMessage.setResponseObject(salesInvoiceDTOList);
            System.out.println("salesInvoiceDTOList" + salesInvoiceDTOList);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            genericDTData.setRows(salesInvoiceDTOList);
            genericDTData.setTotalRows(0);
            responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
        }
        return responseMessage;
    }

    public Object GSTR1B2CSSaleInvoiceDetails(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        System.out.println("request " + request + "  req=" + req);
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");
        Long state_id = Long.parseLong(request.get("state_id"));
        Double tax_rate = Double.parseDouble(request.get("tax_rate"));
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        Boolean flag = false;
        List saleInvoice = new ArrayList<>();
        List<TranxSalesInvoice> saleArrayList = new ArrayList<>();
        List<SalesInvoiceDTO> salesInvoiceDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT tsi.id FROM `tranx_sales_invoice_tbl` AS tsi LEFT JOIN ledger_master_tbl" + " ON tsi.sundry_debtors_id = ledger_master_tbl.id LEFT JOIN" + " tranx_sales_invoice_details_units_tbl AS tsidu ON tsi.id = tsidu.sales_invoice_id" + " WHERE tsi.status=1 AND tsi.total_amount <= 250000 AND ledger_master_tbl.taxable=0 AND tsidu.igst=" + tax_rate + " AND ledger_master_tbl.state_id=" + state_id;

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += " AND DATE(bill_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
//
                query = query + " AND (bill_date LIKE '%" + searchText + "%'" + "OR sales_invoice_no LIKE '%" + searchText + "%'" + "OR ledger_master_tbl.ledger_name LIKE '%" + searchText + "%'" + "OR tsi.total_amount LIKE '%" + searchText + "%'" + "OR tsi.total_base_amount LIKE '%" + searchText + "%'" + "OR tsi.total_tax LIKE '%" + searchText + "%'" + "OR tsi.totaligst LIKE '%" + searchText + "%'" + "OR tsi.totalcgst LIKE '%" + searchText + "%'" + "OR tsi.totalsgst LIKE '%" + searchText + "%' )";
            }
            String jsonToStr = request.get("sort");
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
                String sortBy = jsonObject.get("colId").getAsString();
                query = query + " ORDER BY " + sortBy;
                if (jsonObject.get("isAsc").getAsBoolean() == true) {
                    query = query + " ASC";
                } else {
                    query = query + " DESC";
                }
            } else {
                query = query + " ORDER BY tsidu.sales_invoice_id DESC";
            }
            salesInvoiceLogger.info("sql :" + query);
            System.out.println("sql :" + query);
            Query q = entityManager.createNativeQuery(query);
//            q.setParameter(1, tax_rate);
//            q.setParameter(2, state_code);
//            q.setParameter(3, 3);
            saleInvoice = q.getResultList();

            for (Object mList : saleInvoice) {
                TranxSalesInvoice invoiceListView = salesTransactionRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                salesInvoiceDTOList.add(convertToDTDTO(invoiceListView));
            }
            responseMessage.setResponseObject(salesInvoiceDTOList);
            System.out.println("salesInvoiceDTOList" + salesInvoiceDTOList);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            genericDTData.setRows(salesInvoiceDTOList);
            genericDTData.setTotalRows(0);
            responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
        }
        return responseMessage;
    }

//    public Object GSTR1SaleInvoiceDetails(@RequestBody Map<String, String> request, HttpServletRequest req) {
//        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
//        ResponseMessage responseMessage = new ResponseMessage();
//        System.out.println("request " + request + "  req=" + req);
//        String searchText = request.get("searchText");
//        String startDate = request.get("startDate");
//        String endDate = request.get("endDate");
//        Long debtoryId = Long.parseLong(request.get("debtor_id"));
//        LocalDate endDatep = null;
//        LocalDate startDatep = null;
//        Boolean flag = false;
//        List saleInvoice = new ArrayList<>();
//        List<TranxSalesInvoice> saleArrayList = new ArrayList<>();
//        List<SalesInvoiceDTO> salesInvoiceDTOList = new ArrayList<>();
//        GenericDTData genericDTData = new GenericDTData();
//        try {
//            String query = "SELECT id FROM `tranx_sales_invoice_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND " + "status=1 AND sundry_debtors_id=" + debtoryId;
////        String query = "SELECT id FROM `tranx_sales_invoice_tbl";
//            if (users.getBranch() != null) {
//                query = query + " AND branch_id=" + users.getBranch().getId();
//            } else {
//                query = query + " AND branch_id IS NULL";
//            }
//
//            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
//                query += " AND DATE(bill_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";
//
//            if (!searchText.equalsIgnoreCase("")) {
//                query = query + " AND narration LIKE '%" + searchText + "%'";
//            }
//            String jsonToStr = request.get("sort");
//            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
//            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
//                String sortBy = jsonObject.get("colId").getAsString();
//                query = query + " ORDER BY " + sortBy;
//                if (jsonObject.get("isAsc").getAsBoolean() == true) {
//                    query = query + " ASC";
//                } else {
//                    query = query + " DESC";
//                }
//            } else {
//                query = query + " ORDER BY id DESC";
//            }
//
//            Query q = entityManager.createNativeQuery(query);
//            saleInvoice = q.getResultList();
//
//            for (Object mList : saleInvoice) {
//                TranxSalesInvoice invoiceListView = salesTransactionRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
//                salesInvoiceDTOList.add(convertToDTDTO(invoiceListView));
//            }
//            responseMessage.setResponseObject(salesInvoiceDTOList);
//            responseMessage.setResponseStatus(HttpStatus.OK.value());
//        } catch (Exception e) {
//            e.printStackTrace();
//            genericDTData.setRows(salesInvoiceDTOList);
//            genericDTData.setTotalRows(0);
//        }
//        return responseMessage;
//    }

    //for pagination of sales invoice list start
    public Object saleList(@RequestBody Map<String, String> request, HttpServletRequest req) {
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
        List saleInvoice = new ArrayList<>();
        List<TranxSalesInvoice> saleArrayList = new ArrayList<>();
        List<SalesInvoiceDTO> salesInvoiceDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT id FROM `tranx_sales_invoice_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += " AND DATE(bill_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND narration LIKE '%" + searchText + "%'";
            }
            String jsonToStr = request.get("sort");
            if (!jsonToStr.isEmpty()) {
                JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
                if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
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
            } else {
                query = query + " ORDER BY id DESC";
            }
            //we get all lists here
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;
            Query q = entityManager.createNativeQuery(query);
            saleInvoice = q.getResultList();
            String query1 = "SELECT COUNT(tranx_sales_invoice_tbl.id) as totalcount FROM tranx_sales_invoice_tbl " + "WHERE tranx_sales_invoice_tbl.status=? AND tranx_sales_invoice_tbl.outlet_id=?";
            if (users.getBranch() != null) {
                query1 = query1 + " AND tranx_sales_invoice_tbl.branch_id=?";
            } else {
                query1 = query1 + " AND tranx_sales_invoice_tbl.branch_id IS NULL";
            }
            Query q1 = entityManager.createNativeQuery(query1);
            q1.setParameter(1, true);
            q1.setParameter(2, users.getOutlet().getId());
            if (users.getBranch() != null) q1.setParameter(3, users.getOutlet().getId());
            int totalProducts = ((BigInteger) q1.getSingleResult()).intValue();
            Integer total_pages = (totalProducts / pageSize);
            if ((totalProducts % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            for (Object mList : saleInvoice) {
                TranxSalesInvoice invoiceListView = salesTransactionRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                salesInvoiceDTOList.add(convertToDTDTO(invoiceListView));
            }
            GenericDatatable<SalesInvoiceDTO> data = new GenericDatatable<>(salesInvoiceDTOList, saleArrayList.size(), pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            genericDTData.setRows(salesInvoiceDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    /* @Shrikant to get consumer sales invoice list limit 50*/
    public Object saleCompList(@RequestBody Map<String, String> request, HttpServletRequest req) {
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
        List saleInvoice = new ArrayList<>();
        List<TranxSalesCompInvoice> saleArrayList = new ArrayList<>();
        List<SalesInvoiceDTO> salesInvoiceDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT id FROM `tranx_sales_comp_invoice_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += " AND DATE(bill_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND narration LIKE '%" + searchText + "%'";
            }
            String jsonToStr = request.get("sort");
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
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
            //we get all lists here
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;
            Query q = entityManager.createNativeQuery(query);
            saleInvoice = q.getResultList();
            String query1 = "SELECT COUNT(tranx_sales_comp_invoice_tbl.id) as totalcount FROM tranx_sales_comp_invoice_tbl " + "WHERE tranx_sales_comp_invoice_tbl.status=? AND tranx_sales_comp_invoice_tbl.outlet_id=?";
            if (users.getBranch() != null) {
                query1 = query1 + " AND tranx_sales_comp_invoice_tbl.branch_id=?";
            } else {
                query1 = query1 + " AND tranx_sales_comp_invoice_tbl.branch_id IS NULL";
            }
            Query q1 = entityManager.createNativeQuery(query1);
            q1.setParameter(1, true);
            q1.setParameter(2, users.getOutlet().getId());
            if (users.getBranch() != null) q1.setParameter(3, users.getOutlet().getId());
            int totalProducts = ((BigInteger) q1.getSingleResult()).intValue();
            Integer total_pages = (totalProducts / pageSize);
            if ((totalProducts % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            for (Object mList : saleInvoice) {
                TranxSalesCompInvoice invoiceListView = salesCompTransactionRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                salesInvoiceDTOList.add(convertToCompDTDTO(invoiceListView));
            }
            GenericDatatable<SalesInvoiceDTO> data = new GenericDatatable<>(salesInvoiceDTOList, saleArrayList.size(), pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            genericDTData.setRows(salesInvoiceDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    private SalesInvoiceDTO convertToCompDTDTO(TranxSalesCompInvoice tranxSaleInvoiceList) {
        LedgerMaster salesAccountLedger = ledgerMasterRepository.findByIdAndStatus(tranxSaleInvoiceList.getSalesAccountLedgerId(), true);
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(tranxSaleInvoiceList.getSundryDebtorsId(), true);

        SalesInvoiceDTO salesInvoiceDTO = new SalesInvoiceDTO();
        salesInvoiceDTO.setId(tranxSaleInvoiceList.getId());
        salesInvoiceDTO.setInvoice_date(DateConvertUtil.convertDateToLocalDate(tranxSaleInvoiceList.getBillDate()).toString());
        salesInvoiceDTO.setInvoice_no(tranxSaleInvoiceList.getSalesInvoiceNo());
        salesInvoiceDTO.setNarration(tranxSaleInvoiceList.getNarration());
        salesInvoiceDTO.setPayment_mode(tranxSaleInvoiceList.getPaymentMode());
        salesInvoiceDTO.setSale_account_name(salesAccountLedger.getLedgerName());
        salesInvoiceDTO.setSale_serial_number(tranxSaleInvoiceList.getSalesSerialNumber());
        salesInvoiceDTO.setSundry_debtor_id(tranxSaleInvoiceList.getSundryDebtorsId());
        salesInvoiceDTO.setSundry_debtor_name(sundryDebtors.getLedgerName());

        salesInvoiceDTO.setTax_amt(tranxSaleInvoiceList.getTotalTax());
        salesInvoiceDTO.setTaxable_amt(tranxSaleInvoiceList.getTaxableAmount());
        salesInvoiceDTO.setTotal_amount(tranxSaleInvoiceList.getTotalAmount());
//        salesInvoiceDTO.setTotalcgst(tranxSaleInvoiceList.getTotalcgst());
//        salesInvoiceDTO.setTotalsgst(tranxSaleInvoiceList.getTotalsgst());
//        salesInvoiceDTO.setTotaligst(tranxSaleInvoiceList.getTotaligst());
        return salesInvoiceDTO;

    }

    //pagination of sale invoice list end
    private SalesInvoiceDTO convertToDTDTO(TranxSalesInvoice tranxSaleInvoiceList) {
        SalesInvoiceDTO salesInvoiceDTO = new SalesInvoiceDTO();
        salesInvoiceDTO.setId(tranxSaleInvoiceList.getId());
        salesInvoiceDTO.setInvoice_date(DateConvertUtil.convertDateToLocalDate(tranxSaleInvoiceList.getBillDate()).toString());
        salesInvoiceDTO.setInvoice_no(tranxSaleInvoiceList.getSalesInvoiceNo());
        salesInvoiceDTO.setNarration(tranxSaleInvoiceList.getNarration());
        salesInvoiceDTO.setPayment_mode(tranxSaleInvoiceList.getPaymentMode());
        salesInvoiceDTO.setSale_account_name(tranxSaleInvoiceList.getSalesAccountLedger().getLedgerName());
        salesInvoiceDTO.setSale_serial_number(tranxSaleInvoiceList.getSalesSerialNumber());
        salesInvoiceDTO.setSundry_debtor_id(tranxSaleInvoiceList.getSundryDebtors().getId());
        salesInvoiceDTO.setSundry_debtor_name(tranxSaleInvoiceList.getSundryDebtors().getLedgerName());
        salesInvoiceDTO.setTax_amt(tranxSaleInvoiceList.getTotalTax());
        salesInvoiceDTO.setTaxable_amt(tranxSaleInvoiceList.getTaxableAmount());
        salesInvoiceDTO.setTotal_amount(tranxSaleInvoiceList.getTotalAmount());
//        <<<<<<<<<<<
        salesInvoiceDTO.setTotalcgst(tranxSaleInvoiceList.getTotalcgst());
        salesInvoiceDTO.setTotalsgst(tranxSaleInvoiceList.getTotalsgst());
        salesInvoiceDTO.setTotaligst(tranxSaleInvoiceList.getTotaligst());
//        >>>>>>>>>>>
        String idList[];
        String referenceNo = "";
        salesInvoiceDTO.setReferenceType(tranxSaleInvoiceList.getReference());
        if (tranxSaleInvoiceList.getReference() != null) {
            if (tranxSaleInvoiceList.getReference().equalsIgnoreCase("SLSQTN")) {
                idList = tranxSaleInvoiceList.getReferenceSqId().split(",");
                for (int i = 0; i < idList.length; i++) {
                    TranxSalesQuotation tranxSalesQuotation = tranxSalesQuotationRepository.findByIdAndStatus(Long.parseLong(idList[i]), true);
                    if (tranxSalesQuotation != null) {
                        referenceNo = referenceNo + tranxSalesQuotation.getSq_bill_no();
                        if (i < idList.length - 1) referenceNo = referenceNo + ",";
                    }
                }
                salesInvoiceDTO.setReferenceNo(referenceNo);
            } else if (tranxSaleInvoiceList.getReference().equalsIgnoreCase("SLSORD")) {
                idList = tranxSaleInvoiceList.getReferenceSoId().split(",");
                for (int i = 0; i < idList.length; i++) {
                    TranxSalesOrder tranxSalesOrder = tranxSalesOrderRepository.findByIdAndStatus(Long.parseLong(idList[i]), true);
                    if (tranxSalesOrder != null) {
                        referenceNo = referenceNo + tranxSalesOrder.getSo_bill_no();
                        if (i < idList.length - 1) referenceNo = referenceNo + ",";
                    }
                }
                salesInvoiceDTO.setReferenceNo(referenceNo);
            } else if (tranxSaleInvoiceList.getReference().equalsIgnoreCase("SLSCHN")) {
                idList = tranxSaleInvoiceList.getReferenceScId().split(",");
                for (int i = 0; i < idList.length; i++) {
                    TranxSalesChallan tranxSalesChallan = tranxSalesChallanRepository.findByIdAndStatus(Long.parseLong(idList[i]), true);
                    if (tranxSalesChallan != null) {
                        referenceNo = referenceNo + tranxSalesChallan.getSc_bill_no();
                        if (i < idList.length - 1) referenceNo = referenceNo + ",";
                    }
                }
                salesInvoiceDTO.setReferenceNo(referenceNo);
            }
        } else {
            salesInvoiceDTO.setReferenceNo("");
        }
        salesInvoiceDTO.setTranxCode(tranxSaleInvoiceList.getTranxCode());
        salesInvoiceDTO.setTransactionTrackingNo(tranxSaleInvoiceList.getTransactionTrackingNo());


        return salesInvoiceDTO;
    }


    public JsonObject getCounterSalesWithId(HttpServletRequest request) {
        JsonObject output = new JsonObject();
        TranxCounterSales csData = null;
        Long csId = Long.parseLong(request.getParameter("counter_sales_id"));
        csData = counterSaleRepository.findByIdAndStatus(csId, true);
        JsonObject counter_sales_data = new JsonObject();
        JsonArray newList = new JsonArray();
        if (csData != null) {
            counter_sales_data.addProperty("id", csData.getId());
            counter_sales_data.addProperty("counter_sales_no", csData.getCounterSaleNo());
            counter_sales_data.addProperty("transaction_dt", csData.getTransactionDate().toString());
            counter_sales_data.addProperty("customer_name", csData.getCustomerName());
            counter_sales_data.addProperty("customer_mobile", csData.getMobileNumber());
            List<TranxCounterSalesDetails> details = detailsRepository.findByCounterSaleIdAndStatus(csId, true);
            if (details.size() > 0) {
                for (TranxCounterSalesDetails mDetails : details) {
                    JsonObject result = new JsonObject();
                    result.addProperty("id", mDetails.getId());
                    result.addProperty("product_id", mDetails.getProduct().getId());
                    result.addProperty("unitId", "");
                    result.addProperty("qtyH", mDetails.getQtyHigh());
                    result.addProperty("qtyM", mDetails.getQtyMedium() != null ? mDetails.getQtyMedium().toString() : "");
                    result.addProperty("qtyL", mDetails.getQtyLow() != null ? mDetails.getQtyLow().toString() : "");
                    result.addProperty("rateH", mDetails.getRateHigh() != null ? mDetails.getRateHigh().toString() : "");
                    result.addProperty("rateM", mDetails.getRateMedium() != null ? mDetails.getRateMedium().toString() : "");
                    result.addProperty("rateL", mDetails.getRateLow() != null ? mDetails.getRateLow().toString() : "");
                    result.addProperty("base_amt_H", mDetails.getBaseAmtHigh());
                    result.addProperty("base_amt_M", mDetails.getBaseAmtMedium() != null ? mDetails.getBaseAmtMedium().toString() : "");
                    result.addProperty("base_amt_L", mDetails.getBaseAmtLow() != null ? mDetails.getBaseAmtLow().toString() : "");
                    result.addProperty("base_amt", "");
                    result.addProperty("dis_amt", "");
                    result.addProperty("dis_per", "");
                    result.addProperty("dis_per_cal", "");
                    result.addProperty("dis_amt_cal", "");
                    result.addProperty("total_amt", "");
                    result.addProperty("gst", "");
                    result.addProperty("igst", "");
                    result.addProperty("cgst", "");
                    result.addProperty("sgst", "");
                    result.addProperty("total_igst", "");
                    result.addProperty("total_cgst", "");
                    result.addProperty("total_sgst", "");
                    result.addProperty("final_amt", "");
                    result.addProperty("discount_proportional_cal", "0");
                    result.addProperty("additional_charges_proportional_cal", "0");
                    result.addProperty("package_id", mDetails.getPackingMaster() != null ? mDetails.getPackingMaster().getId().toString() : "");
                    JsonArray serialNo = new JsonArray();
                    List<TranxCounterSalesProdSrNo> serialNumbers = tranxCSPrSrRepository.findByProductIdAndStatus(mDetails.getProduct().getId(), true);
                    if (serialNumbers.size() > 0) {
                        for (TranxCounterSalesProdSrNo mSerail : serialNumbers) {
                            JsonObject serailNObject = new JsonObject();
                            serailNObject.addProperty("no", mSerail.getSerialNo());
                            serialNo.add(serailNObject);
                        }
                        result.add("serialNo", serialNo);
                    }

                    /* getting Units of Sales Order*/
                    /*List<TranxCounterSalesDetailsUnits> unitDetails = tranxSalesOrderDetailsUnitsRepository.findBySalesOrderDetailsIdAndStatus(
                            mDetails.getId(), true);
                    unitDetails.forEach(mUnit -> {
                        JsonObject mObject = new JsonObject();
                        mObject.addProperty("qty", mUnit.getQty());
                        mObject.addProperty("rate", mUnit.getRate());
                        mObject.addProperty("base_amt", mUnit.getBaseAmt());
                        mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                        mObject.addProperty("unit_id", mUnit.getUnits().getId());
                        units.add(mObject);
                    });
                    result.add("units", units);*/
                    newList.add(result);
                }
            }
        }

        if (newList.size() > 0) {
            JsonObject data = new JsonObject();
            data.add("productList", newList);
            data.add("counter_sales_data", counter_sales_data);
            output.addProperty("message", "success");
            output.addProperty("responseStatus", HttpStatus.OK.value());
            output.add("data", data);
        } else {
            output.addProperty("message", "empty list");
            output.addProperty("responseStatus", HttpStatus.OK.value());
            output.addProperty("data", "");
        }
        return output;
    }

    /* find all Sales Invoices of Sundry Debtors/Clients wise , for Sales Returns*/
    public JsonObject salesListClientsWise(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonArray result = new JsonArray();
        List<TranxSalesInvoice> salesInvoice = new ArrayList<>();
        if (paramMap.containsKey("dateFrom") && paramMap.containsKey("dateTo")) {
            salesInvoice = salesTransactionRepository.findByClientsWithDates(users.getOutlet().getId(), true, Long.parseLong(request.getParameter("sundry_debtors_id")), request.getParameter("dateFrom"), request.getParameter("dateTo"));
        } else {
            salesInvoice = salesTransactionRepository.findByOutletIdAndStatusAndSundryDebtorsId(users.getOutlet().getId(), true, Long.parseLong(request.getParameter("sundry_debtors_id")));
        }
        if (salesInvoice.size() > 0) {
            for (TranxSalesInvoice invoices : salesInvoice) {
                JsonObject response = new JsonObject();
                response.addProperty("id", invoices.getId());
                response.addProperty("invoice_no", invoices.getSalesInvoiceNo());

                response.addProperty("invoice_date", invoices.getBillDate().toString());
                response.addProperty("sales_serial_number", invoices.getSalesSerialNumber());
                response.addProperty("total_amount", invoices.getTotalAmount());
                response.addProperty("sundry_debtors_name", invoices.getSundryDebtors().getLedgerName());
                response.addProperty("sundry_creditor_id", invoices.getSundryDebtors().getId());
                result.add(response);
            }
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    /**
     * Sales Returns : find all products of selected sales invoice bills of sundry debtors
     **/
    public JsonObject productListSalesInvoice(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
   /*     JsonArray result = new JsonArray();
        TranxSalesInvoice purInvoice = salesTransactionRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("sales_invoice_id")), true);
        List<TranxSalesInvoiceDetails> productList = salesInvoiceDetailsRepository.findBySalesInvoiceIdAndStatus(
                Long.parseLong(request.getParameter("sales_invoice_id")), true);
        for (TranxSalesInvoiceDetails prodList : productList) {
            JsonObject response = new JsonObject();
            response.addProperty("id", prodList.getId());
            response.addProperty("invoice_id", prodList.getSalesInvoice().getId());
            response.addProperty("product_id", prodList.getProduct().getId());
            response.addProperty("product_name",
                    prodList.getProduct().getProductName());
            List<ProductUnit> productUnits = productUnitRepository.
                    findByProductId(prodList.getProduct().getId());
            response.addProperty("High Unit",
                    prodList.getQtyHigh() != 0 ? productUnits.get(0).getUnitType() : "NA");
            response.addProperty("Medium Unit",
                    prodList.getQtyMedium() != 0 ? productUnits.get(1).getUnitType() : "NA");
            response.addProperty("Low Unit",
                    prodList.getQtyLow() != 0 ? productUnits.get(2).getUnitType() : "NA");
            response.addProperty("Total Price", purInvoice.getTotalAmount());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;*/
        List<TranxSalesInvoiceDetails> list = new ArrayList<>();
        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxSalesInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("sales_invoice_id"));
            TranxSalesInvoice salesInvoice = salesTransactionRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            list = salesInvoiceDetailsRepository.findBySalesInvoiceIdAndStatus(id, true);
           /* serialNumbers = serialNumberRepository.findByTranxSalesInvoiceId(salesInvoice.getId());
            additionalCharges = salesAdditionalChargesRepository.findBySalesTransactionIdAndStatus(salesInvoice.getId(), true);
           */
            finalResult.addProperty("tcs", salesInvoice.getTcs());
            finalResult.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", salesInvoice.getSalesDiscountLedger() != null ? salesInvoice.getSalesDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", salesInvoice.getSalesDiscountAmount());
            finalResult.addProperty("discountInPer", salesInvoice.getSalesDiscountPer());
            JsonObject result = new JsonObject();
            /* Sales Invoice Data */
            result.addProperty("id", salesInvoice.getId());
            result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
            result.addProperty("invoice_no", salesInvoice.getSalesInvoiceNo().toString());
            result.addProperty("sales_sr_no", salesInvoice.getSalesSerialNumber());
            result.addProperty("sales_account_ledger_id", salesInvoice.getSalesAccountLedger().getId());
            result.addProperty("debtors_id", salesInvoice.getSundryDebtors().getId());
            result.addProperty("debtors_name", salesInvoice.getSundryDebtors().getLedgerName());
            /* End of Sales Invoice Data */

            /* Sales Invoice Details */
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxSalesInvoiceDetails mDetails : list) {
                    JsonObject prDetails = new JsonObject();
                    prDetails.addProperty("details_id", mDetails.getId());
                    prDetails.addProperty("product_name", mDetails.getProduct().getProductName());
                    prDetails.addProperty("product_id", mDetails.getProduct().getId());
                    prDetails.addProperty("qtyH", mDetails.getQtyHigh());
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
                    JsonArray serialNo = new JsonArray();
                    if (serialNumbers.size() > 0) {
                        for (TranxSalesInvoiceProductSrNumber mProductSerials : serialNumbers) {
                            JsonObject jsonSerailNo = new JsonObject();
                            jsonSerailNo.addProperty("product_id", mDetails.getProduct().getId());
                            jsonSerailNo.addProperty("serial_no", mProductSerials.getSerialNo());
                            serialNo.add(jsonSerailNo);
                        }
                        prDetails.add("serialNo", serialNo);
                    }
                    row.add(prDetails);
                }
            } /* End of Sales Invoice Details */

            /* Sales Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            if (additionalCharges.size() > 0) {
                for (TranxSalesInvoiceAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledger_id", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    jsonAdditionalList.add(json_charges);
                }
            }
            /* End of Purchase Additional Charges */
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);
            finalResult.add("additional_charges", jsonAdditionalList);

        } catch (DataIntegrityViolationException e) {
            salesInvoiceLogger.error("Error in productListSalesInvoice :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesInvoiceLogger.error("Error in productListSalesInvoice :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    /* Sales Returns:  list of all selected products against sales invoice bills */
    public JsonObject getSalesBillsByIdWithProductsId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesInvoiceDetails> list = new ArrayList<>();
        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxSalesInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("sales_invoice_id"));
            TranxSalesInvoice salesInvoice = salesTransactionRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            String str = request.getParameter("product_ids");
            list = salesInvoiceDetailsRepository.findSalesBillByIdWithProductsId(id, true, str);
           /* serialNumbers = serialNumberRepository.findByTranxSalesInvoiceId(salesInvoice.getId());
            additionalCharges = salesAdditionalChargesRepository.findBySalesTransactionIdAndStatus(salesInvoice.getId(), true);*/
            finalResult.addProperty("tcs", salesInvoice.getTcs());
            finalResult.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", salesInvoice.getSalesDiscountLedger() != null ? salesInvoice.getSalesDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", salesInvoice.getSalesDiscountAmount());
            finalResult.addProperty("discountInPer", salesInvoice.getSalesDiscountPer());
            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", salesInvoice.getId());
            result.addProperty("bill_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
            result.addProperty("invoice_no", salesInvoice.getSalesInvoiceNo().toString());
            result.addProperty("sales_sr_no", salesInvoice.getSalesSerialNumber());
            result.addProperty("sales_account_ledger_id", salesInvoice.getSalesAccountLedger().getId());
            result.addProperty("supplierId", salesInvoice.getSundryDebtors().getId());
            /* End of Sales Bill Data */

            /* Sales Invoice Details */
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxSalesInvoiceDetails mDetails : list) {
                    JsonObject prDetails = new JsonObject();
                    prDetails.addProperty("details_id", mDetails.getId());
                    prDetails.addProperty("product_id", mDetails.getProduct().getId());
                    prDetails.addProperty("qtyH", mDetails.getQtyHigh());
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
                    prDetails.addProperty("reference_id", mDetails.getReferenceId());
                    prDetails.addProperty("reference_type", mDetails.getReferenceType());
                    JsonArray serialNo = new JsonArray();
                    if (serialNumbers.size() > 0) {
                        for (TranxSalesInvoiceProductSrNumber mProductSerials : serialNumbers) {
                            JsonObject jsonSerailNo = new JsonObject();
                            jsonSerailNo.addProperty("product_id", mDetails.getProduct().getId());
                            jsonSerailNo.addProperty("serial_no", mProductSerials.getSerialNo());
                            serialNo.add(jsonSerailNo);
                        }
                        prDetails.add("serialNo", serialNo);
                    }
                    row.add(prDetails);
                }
            }
            /* End of Sales bill Details */

            /* Sales Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            if (additionalCharges.size() > 0) {
                for (TranxSalesInvoiceAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledger_id", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    jsonAdditionalList.add(json_charges);
                }
            }
            /* End of Sales Additional Charges */
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);
            finalResult.add("additional_charges", jsonAdditionalList);

        } catch (DataIntegrityViolationException e) {
            salesInvoiceLogger.error("Error in getSalesBillsByIdWithProductsId :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesInvoiceLogger.error("Error in getSalesBillsByIdWithProductsId :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject getSalesInvoiceById(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesInvoiceDetails> list = new ArrayList<>();
        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxSalesInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();

        JsonObject finalResult = new JsonObject();
        try {
            long id = Long.parseLong(request.getParameter("id"));
            TranxSalesInvoice salesInvoice = salesTransactionRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            list = salesInvoiceDetailsRepository.findBySalesInvoiceIdAndStatus(id, true);
            /*serialNumbers = serialNumberRepository.findByTranxSalesInvoiceId(salesInvoice.getId());
            additionalCharges = salesAdditionalChargesRepository.findBySalesTransactionIdAndStatus(salesInvoice.getId(), true);*/
            finalResult.addProperty("tcs", salesInvoice.getTcs());
            finalResult.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", salesInvoice.getSalesDiscountLedger() != null ? salesInvoice.getSalesDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", salesInvoice.getSalesDiscountAmount());
            finalResult.addProperty("discountInPer", salesInvoice.getSalesDiscountPer());
            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", salesInvoice.getId());
            result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
            result.addProperty("invoice_no", salesInvoice.getSalesInvoiceNo());
            result.addProperty("sales_sr_no", salesInvoice.getSalesSerialNumber());
            result.addProperty("sales_account_ledger_id", salesInvoice.getSalesAccountLedger().getId());
            result.addProperty("supplierId", salesInvoice.getSundryDebtors().getId());
            result.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            result.addProperty("total_cgst", salesInvoice.getTotalcgst());
            result.addProperty("total_sgst", salesInvoice.getTotalsgst());
            result.addProperty("total_igst", salesInvoice.getTotaligst());
            result.addProperty("total_qty", salesInvoice.getTotalqty());
            result.addProperty("taxable_amount", salesInvoice.getTaxableAmount());
            result.addProperty("tcs", salesInvoice.getTcs());
            result.addProperty("status", salesInvoice.getStatus());
            result.addProperty("financial_year", salesInvoice.getFinancialYear());
            result.addProperty("debtor_id", salesInvoice.getSundryDebtors().getId());
            result.addProperty("debtor_name", salesInvoice.getSundryDebtors().getLedgerName());


            /* End of Sales Invoice Data */

            /* Sales Invoice Details */
            JsonArray row = new JsonArray();
            JsonArray units = new JsonArray();

            if (list.size() > 0) {

                JsonObject prDetails = new JsonObject();
                /* getting Units of Sales Order*/
                List<TranxSalesInvoiceDetailsUnits> unitDetails = tranxSalesInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(salesInvoice.getId(), true);
                JsonArray productDetails = new JsonArray();
                unitDetails.forEach(mUnit -> {
                    JsonObject mObject = new JsonObject();
                    prDetails.addProperty("details_id", mUnit.getId());
                    prDetails.addProperty("product_id", mUnit.getProduct().getId());
                    /*if (mUnit.getPackingMaster() != null) {
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
                    }*/
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
            /* End of Purchase Invoice Details */
            /* Sales Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            if (additionalCharges.size() > 0) {
                for (TranxSalesInvoiceAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledger_id", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    jsonAdditionalList.add(json_charges);
                }
            }
            /* End of Purchase Additional Charges */
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);
            finalResult.add("additional_charges", jsonAdditionalList);

        } catch (DataIntegrityViolationException e) {
            salesInvoiceLogger.error("Error in getSalesInvoiceById :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesInvoiceLogger.error("Error in getSalesInvoiceById :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }


    public JsonObject getSalesCompInvoiceByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxSalesCompInvoice salesInvoice = salesCompTransactionRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            finalResult.addProperty("tcs_mode", salesInvoice.getTcsMode());
            if (salesInvoice.getTcsMode() != null && salesInvoice.getTcsMode().equalsIgnoreCase("tcs")) {
                finalResult.addProperty("tcs_per", salesInvoice.getTcs());
                finalResult.addProperty("tcs_amt", salesInvoice.getTcsAmt());
            } else if (salesInvoice.getTcsMode() != null && salesInvoice.getTcsMode().equalsIgnoreCase("tds")) {
                finalResult.addProperty("tcs_per", salesInvoice.getTdsPer());
                finalResult.addProperty("tcs_amt", salesInvoice.getTdsAmt());
            } else {
                finalResult.addProperty("tcs_amt", 0.0);
                finalResult.addProperty("tcs_per", 0.0);
            }

            finalResult.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", salesInvoice.getSalesDiscountLedgerId() != null ? salesInvoice.getSalesDiscountLedgerId() : 0);
            finalResult.addProperty("discountInAmt", salesInvoice.getSalesDiscountAmount());
            finalResult.addProperty("discountInPer", salesInvoice.getSalesDiscountPer());
            finalResult.addProperty("totalSalesDiscountAmt", salesInvoice.getTotalSalesDiscountAmt());
            finalResult.addProperty("sales_discount", salesInvoice.getSalesDiscountPer());
            finalResult.addProperty("totalQty", salesInvoice.getTotalqty());
            finalResult.addProperty("totalFreeQty", salesInvoice.getFreeQty());
            finalResult.addProperty("grossTotal", salesInvoice.getGrossAmount());
            finalResult.addProperty("totalTax", salesInvoice.getTotalTax());
            finalResult.addProperty("additionLedger1", salesInvoice.getAdditionLedger1Id() != null ? salesInvoice.getAdditionLedger1Id() : 0);
            finalResult.addProperty("additionLedgerAmt1", salesInvoice.getAdditionLedgerAmt1() != null ? salesInvoice.getAdditionLedgerAmt1() : 0);
            finalResult.addProperty("additionLedger2", salesInvoice.getAdditionLedger2Id() != null ? salesInvoice.getAdditionLedger2Id() : 0);
            finalResult.addProperty("additionLedgerAmt2", salesInvoice.getAdditionLedgerAmt2() != null ? salesInvoice.getAdditionLedgerAmt2() : 0);
            finalResult.addProperty("additionLedger3", salesInvoice.getAdditionLedger3Id() != null ? salesInvoice.getAdditionLedger3Id() : 0);
            finalResult.addProperty("additionLedgerAmt3", salesInvoice.getAdditionLedgerAmt3() != null ? salesInvoice.getAdditionLedgerAmt3() : 0);
            finalResult.addProperty("totalamt", salesInvoice.getTotalAmount() != null ? salesInvoice.getTotalAmount() : 0);

            LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(salesInvoice.getSundryDebtorsId(), true);

            JsonObject result = new JsonObject();
            /* Purchase Order Data */
            result.addProperty("id", salesInvoice.getId());
            result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
            result.addProperty("transaction_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
            result.addProperty("invoice_no", salesInvoice.getSalesInvoiceNo());
            result.addProperty("sales_sr_no", salesInvoice.getSalesSerialNumber());
            result.addProperty("sales_account_ledger_id", salesInvoice.getSalesAccountLedgerId());
            result.addProperty("supplierId", salesInvoice.getSundryDebtorsId());
            result.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            result.addProperty("total_cgst", salesInvoice.getTotalcgst());
            result.addProperty("total_sgst", salesInvoice.getTotalsgst());
            result.addProperty("total_igst", salesInvoice.getTotaligst());
            result.addProperty("total_qty", salesInvoice.getTotalqty());
            result.addProperty("taxable_amount", salesInvoice.getTaxableAmount());
            result.addProperty("tcs", salesInvoice.getTcs());
            result.addProperty("status", salesInvoice.getStatus());
            result.addProperty("financial_year", salesInvoice.getFinancialYear());
            result.addProperty("debtor_id", salesInvoice.getSundryDebtorsId());
            result.addProperty("debtor_name", sundryDebtors.getLedgerName());
            result.addProperty("additional_charges_total", salesInvoice.getAdditionalChargesTotal());
            result.addProperty("gstNo", salesInvoice.getGstNumber());
            result.addProperty("paymentMode", salesInvoice.getPaymentMode());
            result.addProperty("salesmanId", salesInvoice.getSalesmanUser() != null ? salesInvoice.getSalesmanUser().toString() : "");
            result.addProperty("p_totalAmount", salesInvoice.getPaymentAmount() != null ? salesInvoice.getPaymentAmount() : 0);
            result.addProperty("isRoundOffCheck", salesInvoice.getIsRoundOff());
            result.addProperty("roundoff", salesInvoice.getRoundOff());
            result.addProperty("doctorsId", salesInvoice.getDoctorId() != null ? salesInvoice.getDoctorId().toString() : "");
            result.addProperty("clientName", salesInvoice.getClientName());
            result.addProperty("clientAddress", salesInvoice.getClientAddress());
            result.addProperty("mobileNumber", salesInvoice.getMobileNumber());


            double pendingAmt = 0.0;
            if (salesInvoice.getPaymentAmount() != null)
                pendingAmt = salesInvoice.getTotalAmount() - salesInvoice.getPaymentAmount();
            if (salesInvoice.getPaymentAmount() != null && pendingAmt > 0) {
                result.addProperty("p_pendingAmount", pendingAmt);
            } else {
                result.addProperty("p_pendingAmount", 0);
            }
            double returnAmt = 0.0;
            if (salesInvoice.getPaymentAmount() != null && salesInvoice.getPaymentAmount() > salesInvoice.getTotalAmount()) {
                returnAmt = salesInvoice.getPaymentAmount() - salesInvoice.getTotalAmount();
                result.addProperty("p_returnAmount", returnAmt);
            } else {
                result.addProperty("p_returnAmount", 0);
            }
            /* End of Sales invoice Data */
            /* Sales invoice Details */
            JsonArray row = new JsonArray();
            List<TranxSalesCompInvoiceDetailsUnits> unitsArray = tranxSalesCompInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(salesInvoice.getId(), true);
            for (TranxSalesCompInvoiceDetailsUnits mUnits : unitsArray) {
                Product product = productRepository.findByIdAndStatus(mUnits.getProductId(), true);
                Units unit = unitsRepository.findByIdAndStatus(mUnits.getUnitsId(), true);

                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProductId());
                unitsJsonObjects.addProperty("product_name", product.getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelAId() != null ? mUnits.getLevelAId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelBId() != null ? mUnits.getLevelBId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelCId() != null ? mUnits.getLevelCId().toString() : "");
                unitsJsonObjects.addProperty("pack_name", product.getPackingMaster() != null ? product.getPackingMaster().getPackName() : "");
                unitsJsonObjects.addProperty("unit_name", unit.getUnitName());
                unitsJsonObjects.addProperty("unitId", unit.getId());
                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                unitsJsonObjects.addProperty("qty", mUnits.getQty());
                unitsJsonObjects.addProperty("returnable_qty", mUnits.getReturnQty() != null ? mUnits.getQty() - mUnits.getReturnQty() : mUnits.getQty() - 0);
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
                unitsJsonObjects.addProperty("add_chg_amt", mUnits.getAdditionChargesAmt());
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1());
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt());
                if (mUnits.getProductBatchNoId() != null) {
                    ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(mUnits.getProductBatchNoId(), true);
                    if (productBatchNo.getExpiryDate() != null) {
                        if (DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).isAfter(productBatchNo.getExpiryDate())) {
                            unitsJsonObjects.addProperty("is_expired", true);
                        } else {
                            unitsJsonObjects.addProperty("is_expired", false);
                        }
                    } else {
                        unitsJsonObjects.addProperty("is_expired", false);
                    }
                    unitsJsonObjects.addProperty("b_detailsId", mUnits.getProductBatchNoId());
                    unitsJsonObjects.addProperty("batch_no", productBatchNo.getBatchNo());
                    unitsJsonObjects.addProperty("b_expiry", productBatchNo.getExpiryDate() != null ? productBatchNo.getExpiryDate().toString() : "");
                    unitsJsonObjects.addProperty("purchase_rate", productBatchNo.getPurchaseRate());
                    unitsJsonObjects.addProperty("is_batch", true);
                    unitsJsonObjects.addProperty("min_rate_a", productBatchNo.getMinRateA());
                    unitsJsonObjects.addProperty("min_rate_b", productBatchNo.getMinRateB());
                    unitsJsonObjects.addProperty("min_rate_c", productBatchNo.getMinRateC());
                    unitsJsonObjects.addProperty("min_discount", productBatchNo.getMinDiscount());
                    unitsJsonObjects.addProperty("max_discount", productBatchNo.getMaxDiscount());
                    unitsJsonObjects.addProperty("manufacturing_date", productBatchNo.getManufacturingDate() != null ? productBatchNo.getManufacturingDate().toString() : "");
                    unitsJsonObjects.addProperty("min_margin", productBatchNo.getMinMargin());
                    unitsJsonObjects.addProperty("b_rate", productBatchNo.getMrp());
                    unitsJsonObjects.addProperty("sales_rate", productBatchNo.getSalesRate());
                    unitsJsonObjects.addProperty("costing", productBatchNo.getCosting());
                    unitsJsonObjects.addProperty("costingWithTax", productBatchNo.getCostingWithTax());
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
                /**** Serial Number ****/
                List<Object[]> serialNum = new ArrayList<>();
                JsonArray serialNumJson = new JsonArray();
                serialNum = serialNumberRepository.findSerialnumbers(mUnits.getProductId(), mUnits.getId(), true);
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
            /* End of Sales Quotations Details */
            /**** Tranx Sales Payment Type *****/
            JsonArray paymentType = new JsonArray();
            List<TranxSalesPaymentType> paymentArray = transSalesPaymentTypeRepository.findByTranxSalesInvoiceIdAndStatus(salesInvoice.getId(), true);
            for (TranxSalesPaymentType mPayment : paymentArray) {
                JsonObject paymentJsonObjects = new JsonObject();
                paymentJsonObjects.addProperty("id", mPayment.getId());
                paymentJsonObjects.addProperty("ledger_id", mPayment.getLedgerMaster().getId());
                paymentJsonObjects.addProperty("type", mPayment.getType());
                paymentJsonObjects.addProperty("label", mPayment.getLabel());
                paymentJsonObjects.addProperty("amount", mPayment.getPaymentAmount());
                paymentType.add(paymentJsonObjects);
            }
            finalResult.add("row", row);
            finalResult.add("invoice_data", result);
            finalResult.add("payment_type", paymentType);
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    /*Sales   invoice  edit*/
    public Object editSalesInvoice(HttpServletRequest request) {
        return saveIntoSalesInvoiceEdit(request, "edit");
    }

    public Object editSalesCompInvoice(HttpServletRequest request) {
        return saveIntoSalesCompInvoiceEdit(request, "edit");
    }

    private ResponseMessage saveIntoSalesCompInvoiceEdit(HttpServletRequest request, String key) {
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseMessage = new ResponseMessage();
        TransactionTypeMaster tranxType = null;
        TranxSalesCompInvoice invoiceTranx = null;
        LedgerMaster discountLedger = null;
        LedgerMaster sundryDebtors = null;
        LedgerMaster salesAccount = null;
        LedgerMaster roundoff = null;
        TranxSalesCompInvoice mSalesTranx = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CONS");
        invoiceTranx = salesCompTransactionRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("id")), users.getOutlet().getId(), true);
        dbList = ledgerTransactionPostingsRepository.findByTransactionId(invoiceTranx.getId(), tranxType.getId());
        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(invoiceTranx.getId(), tranxType.getId(), true);
        invoiceTranx.setOperations("Updated");
        Branch branch = null;
        if (invoiceTranx.getBranchId() != null)
            branch = branchRepository.findByIdAndStatus(invoiceTranx.getBranchId(), true);
        Outlet outlet = outletRepository.findByIdAndStatus(invoiceTranx.getOutletId(), true);
        Boolean taxFlag;

        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date dt = DateConvertUtil.convertStringToDate(request.getParameter("bill_dt"));

        if (date.isEqual(DateConvertUtil.convertDateToLocalDate(invoiceTranx.getBillDate()))) {
            dt = invoiceTranx.getBillDate();
        }
        invoiceTranx.setBillDate(dt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setSalesInvoiceNo(request.getParameter("bill_no"));
        invoiceTranx.setSalesSerialNumber(Long.parseLong(request.getParameter("sales_sr_no")));
        salesAccount = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), users.getOutlet().getId(), true);

        /* calling store procedure for updating opening and closing  of Sales Account  */
        if (salesAccount.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {
            Boolean isContains = dbList.contains(salesAccount.getId());
            Boolean isLedgerContains = ledgerList.contains(salesAccount.getId());
            mInputList.add(salesAccount.getId());
            ledgerInputList.add(salesAccount.getId());
            if (isContains) {
                /* edit ledger tranx if same ledger is modified */
                /**** New Postings Logic *****/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(salesAccount.getId(), tranxType.getId(), invoiceTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(Double.parseDouble(request.getParameter("taxable_amount")));
                    mLedger.setTransactionDate(dt);
                    mLedger.setOperations("updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                /* insert ledger tranx if ledger is changed */
                /**** New Postings Logic *****/
                //   ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("total_base_amt")), salesAccount, tranxType, salesAccount.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), date, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
                ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("taxable_amount")), salesAccount, tranxType, salesAccount.getAssociateGroups(), fiscalYear, branch, outlet, dt, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
            }


            Double amount = Double.parseDouble(request.getParameter("taxable_amount"));
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSalesAccountLedgerId(), tranxType, "CR", invoiceTranx.getId(), salesAccount, dt, fiscalYear, outlet, branch, invoiceTranx.getTranxCode());
        }/* end of calling store procedure for Sales Account updating opening and closing */

        Long discountLedgerId = Long.parseLong(request.getParameter("sales_disc_ledger"));
        if (discountLedgerId > 0) {
            discountLedger = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("sales_disc_ledger")), users.getOutlet().getId(), true);
            if (discountLedger != null) {
                invoiceTranx.setSalesDiscountLedgerId(discountLedger.getId());
                /* calling store procedure for updating opening and closing of Purchase Discount  */
                /*if (discountLedger.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {
                    Boolean isContains = dbList.contains(discountLedger.getId());
                    mInputList.add(discountLedger.getId());
                    if (isContains) {
                        *//**** New Postings Logic *****//*
                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(discountLedger.getId(), tranxType.getId(), invoiceTranx.getId());
                        if (mLedger != null) {
                            mLedger.setAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
                            mLedger.setTransactionDate(date);
                            mLedger.setOperations("updated");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    } else {
                        *//**** New Postings Logic *****//*
                        ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("sales_discount_amt")), discountLedger, tranxType, discountLedger.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), date, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }*/
                /* end of calling store procedure for updating opening and closing of Purchase Discount */
            }
        } else {
            if (discountLedger != null) invoiceTranx.setSalesDiscountLedgerId(discountLedger.getId());
        }
        sundryDebtors = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), users.getOutlet().getId(), true);
        /* calling store procedure for updating opening and closing of Sundry Creditors  */
        if (sundryDebtors.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {

            Boolean isContains = dbList.contains(sundryDebtors.getId());
            Boolean isLedgerContains = ledgerList.contains(sundryDebtors.getId());
            mInputList.add(sundryDebtors.getId());
            ledgerInputList.add(sundryDebtors.getId());
            if (isContains) {
                /**** New Postings Logic *****/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(sundryDebtors.getId(), tranxType.getId(), invoiceTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(Double.parseDouble(request.getParameter("totalamt")));
                    mLedger.setTransactionDate(dt);
                    mLedger.setOperations("updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                /* insert ledger tranx if ledger is changed */
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("totalamt")), sundryDebtors, tranxType, sundryDebtors.getAssociateGroups(), fiscalYear, branch, outlet, dt, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "DR", true, tranxType.getTransactionCode(), "Insert");
            }

            Double amount = Double.parseDouble(request.getParameter("totalamt"));
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSundryDebtorsId(), tranxType, "DR", invoiceTranx.getId(), sundryDebtors, dt, fiscalYear, outlet, branch, invoiceTranx.getTranxCode());

        }  /* end of calling store procedure for updating opening and closing of Sundry Creditors  */
        invoiceTranx.setSalesAccountLedgerId(salesAccount.getId());
        invoiceTranx.setSundryDebtorsId(sundryDebtors.getId());
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setSalesRoundOffId(roundoff.getId());
        Boolean isContains = dbList.contains(roundoff.getId());
        Boolean isLedgerContains = ledgerList.contains(roundoff.getId());
        ledgerInputList.add(roundoff.getId());
        String crdr = "";
        Double rf = Double.parseDouble(request.getParameter("roundoff"));
        /* inserting into Round off  JSON Object */
        if (isContains) {
            if (rf >= 0) {
                crdr = "DR";
            } else if (Double.parseDouble(request.getParameter("roundoff")) < 0) {
                crdr = "CR";
            }
            /**** New Postings Logic *****/
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(roundoff.getId(), tranxType.getId(), invoiceTranx.getId());
            if (mLedger != null) {
                mLedger.setAmount(rf);
                mLedger.setTransactionDate(dt);
                mLedger.setOperations("updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            }
        } else {
            if (rf >= 0) {

                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(rf, roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, branch, outlet, dt, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "DR", true, tranxType.getTransactionCode(), "Insert");
            } else {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(rf, roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, branch, outlet, dt, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
            }
        }

        String tranxAction = "CR";
        if (rf >= 0) tranxAction = "DR";
        Double amount = rf;
        /**** NEW METHOD FOR LEDGER POSTING ****/
        postingUtility.callToPostingLedgerForUpdate(isLedgerContains, Math.abs(amount), invoiceTranx.getSalesRoundOffId(), tranxType, tranxAction, invoiceTranx.getId(), roundoff, dt, fiscalYear, outlet, branch, invoiceTranx.getTranxCode());

        /* end of inserting into Sundry Creditors JSON Object */
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
        invoiceTranx.setBalance(Double.parseDouble(request.getParameter("totalamt")));

        taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
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
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("totalqty")));

//        invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
        if (paramMap.containsKey("doctorsId")) {
            invoiceTranx.setDoctorId(Long.parseLong(request.getParameter("doctorsId")));
        }
        if (paramMap.containsKey("client_name")) {
            invoiceTranx.setClientName(request.getParameter("client_name"));
        }
        if (paramMap.containsKey("client_address")) {
            invoiceTranx.setClientAddress(request.getParameter("client_address"));
        }
        if (paramMap.containsKey("mobile_number")) {
            invoiceTranx.setMobileNumber(request.getParameter("mobile_number"));
        }
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        invoiceTranx.setSalesDiscountAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_sales_discount_amt")));
        invoiceTranx.setUpdatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setNarration(request.getParameter("narration"));
        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                invoiceTranx.setGstNumber(request.getParameter("gstNo"));
            }
        }
       /* if (paramMap.containsKey("additionalChgLedger1")) {
            LedgerMaster additionalChgLedger1 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger1")), users.getOutlet().getId(), true);
            if (additionalChgLedger1 != null) {
                invoiceTranx.setAdditionLedger1(additionalChgLedger1);
                invoiceTranx.setAdditionLedgerAmt1(Double.valueOf(request.getParameter("addChgLedgerAmt1")));
                isContains = dbList.contains(additionalChgLedger1.getId());
                mInputList.add(additionalChgLedger1.getId());
                if (isContains) {
                   *//**** New Postings Logic *****//*
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(additionalChgLedger1.getId(), tranxType.getId(), invoiceTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(Double.parseDouble(request.getParameter("addChgLedgerAmt1")));
                        mLedger.setTransactionDate(date);
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                        *//**** New Postings Logic *****//*
                        ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt1(), mSalesTranx.getAdditionLedger1(), tranxType, mSalesTranx.getAdditionLedger1().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt1() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
        }*/
        /*if (paramMap.containsKey("additionalChgLedger2")) {
            LedgerMaster additionalChgLedger2 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger2")), users.getOutlet().getId(), true);
            if (additionalChgLedger2 != null) {
                invoiceTranx.setAdditionLedger2(additionalChgLedger2);
                invoiceTranx.setAdditionLedgerAmt2(Double.valueOf(request.getParameter("addChgLedgerAmt2")));
                isContains = dbList.contains(additionalChgLedger2.getId());
                mInputList.add(additionalChgLedger2.getId());
                 if (isContains) {
                *//**** New Postings Logic *****//*
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(additionalChgLedger2.getId(), tranxType.getId(), invoiceTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(Double.parseDouble(request.getParameter("addChgLedgerAmt2")));
                        mLedger.setTransactionDate(date);
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                       *//* *** New Postings Logic *****//*
                        ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt2(), mSalesTranx.getAdditionLedger2(), tranxType, mSalesTranx.getAdditionLedger2().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt2() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
        }*/
        if (paramMap.containsKey("additionalChgLedger3")) {
            LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
            if (additionalChgLedger3 != null) {
                invoiceTranx.setAdditionLedger3Id(additionalChgLedger3.getId());
                invoiceTranx.setAdditionLedgerAmt3(Double.valueOf(request.getParameter("addChgLedgerAmt3")));
                isContains = dbList.contains(additionalChgLedger3.getId());
                mInputList.add(additionalChgLedger3.getId());
                if (isContains) {
                    /**** New Postings Logic *****/
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(additionalChgLedger3.getId(), tranxType.getId(), invoiceTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(Double.parseDouble(request.getParameter("addChgLedgerAmt3")));
                        mLedger.setTransactionDate(dt);
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                        /**** New Postings Logic *****/
                        LedgerMaster additionalLedger3 = ledgerMasterRepository.findByIdAndStatus(mSalesTranx.getAdditionLedger3Id(), true);
                        FiscalYear fYear = fiscalYearRepository.getById(mSalesTranx.getFiscalYearId());

                        ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt3(), additionalLedger3, tranxType, additionalLedger3.getAssociateGroups(), fYear, branch, outlet, mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt3() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
        }
        if (paramMap.containsKey("salesmanId") && !request.getParameter("salesmanId").equalsIgnoreCase("")) {
            SalesManMaster salesManMaster = salesmanMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("salesmanId")), true);
            invoiceTranx.setSalesmanUser(salesManMaster.getId());
        }
        if (paramMap.containsKey("paymentMode")) {
            invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        }
        if (paramMap.containsKey("advancedAmount"))
            invoiceTranx.setAdvancedAmount(Double.parseDouble(request.getParameter("advancedAmount")));
        if (paramMap.containsKey("p_totalAmount"))
            invoiceTranx.setPaymentAmount(Double.parseDouble(request.getParameter("p_totalAmount")));
        if (paramMap.containsKey("isRoundOffCheck"))
            invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));
        /***** TCS and TDS *****/
        if (paramMap.containsKey("tcs_mode")) {
            if (request.getParameter("tcs_mode").equalsIgnoreCase("tcs")) {
                invoiceTranx.setTcsAmt(Double.parseDouble(request.getParameter("tcs_amt")));
                invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs_per")));
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
            }
            if (request.getParameter("tcs_mode").equalsIgnoreCase("tds")) {
                invoiceTranx.setTdsAmt(Double.parseDouble(request.getParameter("tcs_amt")));
                invoiceTranx.setTdsPer(Double.parseDouble(request.getParameter("tcs_per")));
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
            } else if (request.getParameter("tcs_mode").equalsIgnoreCase("")) {
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
            }
            invoiceTranx.setTcsMode(request.getParameter("tcs_mode"));
        }
        try {
            mSalesTranx = salesCompTransactionRepository.save(invoiceTranx);
            if (mSalesTranx != null) {

                /**** for PurchaseInvoice Details Edit ****/
                String jsonStr = request.getParameter("row");
                JsonParser parser = new JsonParser();
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) rowsDeleted = request.getParameter("rowDelDetailsIds");
                saveIntoSalesCompInvoiceDetailsEdit(array, mSalesTranx, users.getBranch(), users.getOutlet(), users.getId(), rowsDeleted, tranxType);
                /**** end of PurchaseInvoice DetailsEdit ****/

                /**** for Purchase Duties and Texes Edit ****/
                /**** for unregister or composition company, for registered company uncomment below saveIntoSalesDutiesTaxesEdit ****/
                String taxStr = request.getParameter("taxCalculation");
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoSalesCompDutiesTaxesEdit(duties_taxes, mSalesTranx, taxFlag, tranxType, users.getOutlet().getId(), users.getId());
                /**** end of Purchase Duties and Texes Edit ****/
                /* Remove all ledgers from DB if we found new input ledger id's while updating */
                for (Long mDblist : dbList) {
                    if (!mInputList.contains(mDblist)) {
                        /**** New Postings Logic *****/
                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mDblist, tranxType.getId(), invoiceTranx.getId());
                        if (mLedger != null) {
                            mLedger.setAmount(0.0);
                            mLedger.setTransactionDate(invoiceTranx.getBillDate());
                            mLedger.setOperations("removed");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    }
                }

            }
            JsonArray paymentType = new JsonArray();
            String paymentMode = request.getParameter("paymentMode");
            /****** Changing the paymentMode from cash or bank account to credit *****/
            if (paymentMode.equalsIgnoreCase("credit")) {
                /**** make the status -> 0 for receipt generated against the invoice ,and reverse the
                 * posting of cash or bank account *****/
                // reversPostings(mSalesTranx,users,totalPaidAmt,paymentMode);
            }
            if (paymentMode.equalsIgnoreCase("multi")) {
                String jsonStr = request.getParameter("payment_type");
                paymentType = new JsonParser().parse(jsonStr).getAsJsonArray();
                Double totalPaidAmt = Double.parseDouble(request.getParameter("p_totalAmount"));
                Double returnAmt = Double.parseDouble(request.getParameter("p_returnAmount"));
                updateCompReceiptInvoice(mSalesTranx, users, paymentType, totalPaidAmt, returnAmt, paymentMode, "update");
                /****** insert into payment type against sales invoice *****/
                insertIntoCompPaymentType(mSalesTranx, paymentType, paymentMode, "update");
            } else if (paymentMode.equalsIgnoreCase("cash")) {
                updateCompReceiptInvoice(mSalesTranx, users, paymentType, mSalesTranx.getTotalAmount(), 0.0, paymentMode, "update");
            }

            /* Remove all ledgers from DB if we found new input ledger id's while updating */
            for (Long mDblist : ledgerList) {
                if (!ledgerInputList.contains(mDblist)) {
                    salesInvoiceLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(mDblist, tranxType.getId(), mSalesTranx.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), mSalesTranx.getBillDate(), tranxType, fiscalYear, detail);
                    }
                    salesInvoiceLogger.info("removing unused previous ledger update done");
                }
            }
            responseMessage.setMessage("Sales invoice updated successfully ");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            /****** Update into Receipt  *****/
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoSalesInvoiceEdit :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
            responseMessage.setMessage("Unable to update sales invoice");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoSalesInvoiceEdit :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
            responseMessage.setMessage("Unable to update sales invoice ");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }


    private ResponseMessage saveIntoSalesInvoiceEdit(HttpServletRequest request, String key) {
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseMessage = new ResponseMessage();
        TransactionTypeMaster tranxType = null;
        TranxSalesInvoice invoiceTranx = null;
        LedgerMaster discountLedger = null;
        LedgerMaster sundryDebtors = null;
        LedgerMaster salesAccount = null;
        LedgerMaster roundoff = null;
        TranxSalesInvoice mSalesTranx = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLS");
        invoiceTranx = salesTransactionRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("id")), users.getOutlet().getId(), true);
        dbList = ledgerTransactionPostingsRepository.findByTransactionId(invoiceTranx.getId(), tranxType.getId());
        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(invoiceTranx.getId(), tranxType.getId(), true);
        invoiceTranx.setOperations("Updated");
        int gstType = 1;
        if (paramMap.containsKey("gstType")) gstType = Integer.parseInt(request.getParameter("gstType"));
        Boolean taxFlag;
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date dt = DateConvertUtil.convertStringToDate(request.getParameter("bill_dt"));

        if (date.isEqual(DateConvertUtil.convertDateToLocalDate(invoiceTranx.getBillDate()))) {
            dt = invoiceTranx.getBillDate();
        }
        invoiceTranx.setBillDate(dt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setSalesInvoiceNo(request.getParameter("bill_no"));
        invoiceTranx.setSalesSerialNumber(Long.parseLong(request.getParameter("sales_sr_no")));
        salesAccount = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), users.getOutlet().getId(), true);

        /* calling store procedure for updating opening and closing  of Sales Account  */
        if (salesAccount.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {
            Boolean isContains = dbList.contains(salesAccount.getId());
            Boolean isLedgerContains = ledgerList.contains(salesAccount.getId());
            mInputList.add(salesAccount.getId());
            ledgerInputList.add(salesAccount.getId());
            if (isContains) {
                /* edit ledger tranx if same ledger is modified */
                /**** New Postings Logic *****/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(salesAccount.getId(), tranxType.getId(), invoiceTranx.getId());
                if (mLedger != null) {
                    if (gstType == 3) mLedger.setAmount(Double.parseDouble(request.getParameter("total_base_amt")));
                    else if (gstType == 1)
                        mLedger.setAmount(Double.parseDouble(request.getParameter("taxable_amount")));
                    mLedger.setTransactionDate(dt);
                    mLedger.setOperations("updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                /* insert ledger tranx if ledger is changed */
                /**** New Postings Logic *****/
                if (gstType == 3)
                    ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("total_base_amt")), salesAccount, tranxType, salesAccount.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), dt, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
                else if (gstType == 1)
                    ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("taxable_amount")), salesAccount, tranxType, salesAccount.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), dt, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
            }

            Double amount = Double.parseDouble(request.getParameter("taxable_amount"));
            if (gstType == 3) amount = Double.parseDouble(request.getParameter("total_base_amt"));
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSalesAccountLedger().getId(), tranxType, "CR", invoiceTranx.getId(), salesAccount, dt, fiscalYear, invoiceTranx.getOutlet(), invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
        }/* end of calling store procedure for Sales Account updating opening and closing */

        Long discountLedgerId = Long.parseLong(request.getParameter("sales_disc_ledger"));
        if (discountLedgerId > 0) {
            discountLedger = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("sales_disc_ledger")), users.getOutlet().getId(), true);
            if (discountLedger != null) {
                invoiceTranx.setSalesDiscountLedger(discountLedger);
                /* calling store procedure for updating opening and closing of Purchase Discount  */
                /*if (discountLedger.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {
                    Boolean isContains = dbList.contains(discountLedger.getId());
                    mInputList.add(discountLedger.getId());
                    if (isContains) {
                        *//**** New Postings Logic *****//*
                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(discountLedger.getId(), tranxType.getId(), invoiceTranx.getId());
                        if (mLedger != null) {
                            mLedger.setAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
                            mLedger.setTransactionDate(date);
                            mLedger.setOperations("updated");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    } else {
                        *//**** New Postings Logic *****//*
                        ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("sales_discount_amt")), discountLedger, tranxType, discountLedger.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), date, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }*/
                /* end of calling store procedure for updating opening and closing of Purchase Discount */
            }
        } else {
            invoiceTranx.setSalesDiscountLedger(discountLedger);
        }
        sundryDebtors = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), users.getOutlet().getId(), true);
        /* calling store procedure for updating opening and closing of Sundry Creditors  */
        if (sundryDebtors.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {

            Boolean isContains = dbList.contains(sundryDebtors.getId());
            Boolean isLedgerContains = ledgerList.contains(sundryDebtors.getId());
            mInputList.add(sundryDebtors.getId());
            ledgerInputList.add(sundryDebtors.getId());
            if (isContains) {
                /**** New Postings Logic *****/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(sundryDebtors.getId(), tranxType.getId(), invoiceTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(Double.parseDouble(request.getParameter("totalamt")));
                    mLedger.setTransactionDate(dt);
                    mLedger.setOperations("updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                /* insert ledger tranx if ledger is changed */
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("totalamt")), sundryDebtors, tranxType, sundryDebtors.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), dt, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "DR", true, tranxType.getTransactionCode(), "Insert");
            }

            Double amount = Double.parseDouble(request.getParameter("totalamt"));
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSundryDebtors().getId(), tranxType, "DR", invoiceTranx.getId(), sundryDebtors, dt, fiscalYear, invoiceTranx.getOutlet(), invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
        }  /* end of calling store procedure for updating opening and closing of Sundry Creditors  */
        invoiceTranx.setSalesAccountLedger(salesAccount);
        invoiceTranx.setSundryDebtors(sundryDebtors);
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setSalesRoundOff(roundoff);
        Boolean isContains = dbList.contains(roundoff.getId());
        Boolean isLedgerContains = ledgerList.contains(roundoff.getId());
        ledgerInputList.add(roundoff.getId());
        String crdr = "";
        Double rf = Double.parseDouble(request.getParameter("roundoff"));
        /* inserting into Round off  JSON Object */
        if (isContains) {
            if (rf >= 0) {
                crdr = "DR";
            } else if (Double.parseDouble(request.getParameter("roundoff")) < 0) {
                crdr = "CR";
            }
            /**** New Postings Logic *****/
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(roundoff.getId(), tranxType.getId(), invoiceTranx.getId());
            if (mLedger != null) {
                mLedger.setAmount(rf);
                mLedger.setTransactionDate(dt);
                mLedger.setOperations("updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            }
        } else {
            if (rf >= 0) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(rf, roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), dt, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "DR", true, tranxType.getTransactionCode(), "Insert");
            } else {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(rf, roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), dt, invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
            }
        }

        String tranxAction = "CR";
        if (rf >= 0) tranxAction = "DR";
        Double amount = rf;
        /**** NEW METHOD FOR LEDGER POSTING ****/
        postingUtility.callToPostingLedgerForUpdate(isLedgerContains, Math.abs(amount), invoiceTranx.getSalesRoundOff().getId(), tranxType, tranxAction, invoiceTranx.getId(), roundoff, dt, fiscalYear, invoiceTranx.getOutlet(), invoiceTranx.getBranch(), invoiceTranx.getTranxCode());

        /* end of inserting into Sundry Creditors JSON Object */
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
        invoiceTranx.setBalance(Double.parseDouble(request.getParameter("totalamt")));

        taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
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
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("totalqty")));
//        invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        invoiceTranx.setSalesDiscountAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_sales_discount_amt")));
        invoiceTranx.setUpdatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setNarration(request.getParameter("narration"));
        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                invoiceTranx.setGstNumber(request.getParameter("gstNo"));
            }
        }
       /* if (paramMap.containsKey("additionalChgLedger1")) {
            LedgerMaster additionalChgLedger1 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger1")), users.getOutlet().getId(), true);
            if (additionalChgLedger1 != null) {
                invoiceTranx.setAdditionLedger1(additionalChgLedger1);
                invoiceTranx.setAdditionLedgerAmt1(Double.valueOf(request.getParameter("addChgLedgerAmt1")));
                isContains = dbList.contains(additionalChgLedger1.getId());
                mInputList.add(additionalChgLedger1.getId());
                if (isContains) {
                   *//**** New Postings Logic *****//*
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(additionalChgLedger1.getId(), tranxType.getId(), invoiceTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(Double.parseDouble(request.getParameter("addChgLedgerAmt1")));
                        mLedger.setTransactionDate(date);
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                        *//**** New Postings Logic *****//*
                        ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt1(), mSalesTranx.getAdditionLedger1(), tranxType, mSalesTranx.getAdditionLedger1().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt1() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
        }*/
        /*if (paramMap.containsKey("additionalChgLedger2")) {
            LedgerMaster additionalChgLedger2 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger2")), users.getOutlet().getId(), true);
            if (additionalChgLedger2 != null) {
                invoiceTranx.setAdditionLedger2(additionalChgLedger2);
                invoiceTranx.setAdditionLedgerAmt2(Double.valueOf(request.getParameter("addChgLedgerAmt2")));
                isContains = dbList.contains(additionalChgLedger2.getId());
                mInputList.add(additionalChgLedger2.getId());
                 if (isContains) {
                *//**** New Postings Logic *****//*
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(additionalChgLedger2.getId(), tranxType.getId(), invoiceTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(Double.parseDouble(request.getParameter("addChgLedgerAmt2")));
                        mLedger.setTransactionDate(date);
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                       *//* *** New Postings Logic *****//*
                        ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt2(), mSalesTranx.getAdditionLedger2(), tranxType, mSalesTranx.getAdditionLedger2().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt2() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
        }*/
        if (paramMap.containsKey("additionalChgLedger3")) {
            LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
            if (additionalChgLedger3 != null) {
                invoiceTranx.setAdditionLedger3(additionalChgLedger3);
                invoiceTranx.setAdditionLedgerAmt3(Double.valueOf(request.getParameter("addChgLedgerAmt3")));
                isContains = dbList.contains(additionalChgLedger3.getId());
                mInputList.add(additionalChgLedger3.getId());
                if (isContains) {
                    /**** New Postings Logic *****/
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(additionalChgLedger3.getId(), tranxType.getId(), invoiceTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(Double.parseDouble(request.getParameter("addChgLedgerAmt3")));
                        mLedger.setTransactionDate(dt);
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt3(), mSalesTranx.getAdditionLedger3(), tranxType, mSalesTranx.getAdditionLedger3().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), mSalesTranx.getAdditionLedgerAmt3() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
        }
        if (paramMap.containsKey("salesmanId") && !request.getParameter("salesmanId").equalsIgnoreCase("")) {
            SalesManMaster salesManMaster = salesmanMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("salesmanId")), true);
            invoiceTranx.setSalesmanUser(salesManMaster.getId());
        }
        if (paramMap.containsKey("paymentMode")) {
            invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        }
        if (paramMap.containsKey("cashAmt") && !request.getParameter("cashAmt").isEmpty())
            invoiceTranx.setCash(Double.parseDouble(request.getParameter("cashAmt")));
        if (paramMap.containsKey("advancedAmount"))
            invoiceTranx.setAdvancedAmount(Double.parseDouble(request.getParameter("advancedAmount")));
        if (paramMap.containsKey("p_totalAmount"))
            invoiceTranx.setPaymentAmount(Double.parseDouble(request.getParameter("p_totalAmount")));
        if (paramMap.containsKey("isRoundOffCheck"))
            invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));
        /***** TCS and TDS *****/
        if (paramMap.containsKey("tcs_mode")) {
            if (request.getParameter("tcs_mode").equalsIgnoreCase("tcs")) {
                invoiceTranx.setTcsAmt(Double.parseDouble(request.getParameter("tcs_amt")));
                invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs_per")));
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
            }
            if (request.getParameter("tcs_mode").equalsIgnoreCase("tds")) {
                invoiceTranx.setTdsAmt(Double.parseDouble(request.getParameter("tcs_amt")));
                invoiceTranx.setTdsPer(Double.parseDouble(request.getParameter("tcs_per")));
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
            } else if (request.getParameter("tcs_mode").equalsIgnoreCase("")) {
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
            }
            invoiceTranx.setTcsMode(request.getParameter("tcs_mode"));
        }
        try {
            mSalesTranx = salesTransactionRepository.save(invoiceTranx);
            if (mSalesTranx != null) {

                /**** for PurchaseInvoice Details Edit ****/
                String jsonStr = request.getParameter("row");
                JsonParser parser = new JsonParser();
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) rowsDeleted = request.getParameter("rowDelDetailsIds");
                saveIntoSalesInvoiceDetailsEdit(array, mSalesTranx, users.getBranch(), users.getOutlet(), users.getId(), rowsDeleted, tranxType);
                /**** end of PurchaseInvoice DetailsEdit ****/

                /**** for Purchase Duties and Texes Edit ****/
                /**** for unregister or composition company, for registered company uncomment below saveIntoSalesDutiesTaxesEdit ****/
                String taxStr = request.getParameter("taxCalculation");
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoSalesDutiesTaxesEdit(duties_taxes, mSalesTranx, taxFlag, tranxType, users.getOutlet().getId(), users.getId());
                /**** end of Purchase Duties and Texes Edit ****/
                /* Remove all ledgers from DB if we found new input ledger id's while updating */
                for (Long mDblist : dbList) {
                    if (!mInputList.contains(mDblist)) {
                        /**** New Postings Logic *****/
                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mDblist, tranxType.getId(), invoiceTranx.getId());
                        if (mLedger != null) {
                            mLedger.setAmount(0.0);
                            mLedger.setTransactionDate(invoiceTranx.getBillDate());
                            mLedger.setOperations("removed");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    }
                }
                /**** Additional Charges Update start ***/
                // saveHistoryPurchaseAdditionalCharges(mPurchaseTranx);
                String acRowsDeleted = "";
                if (paramMap.containsKey("additionalCharges")) {
                    String strJson = request.getParameter("additionalCharges");
                    JsonElement purAddChargesJson = parser.parse(strJson);
                    JsonArray additionalCharges = purAddChargesJson.getAsJsonArray();
                    if (additionalCharges.size() > 0) {
                        saveIntoSalesAdditionalChargesEdit(additionalCharges, mSalesTranx, tranxType, users.getOutlet().getId(), acRowsDeleted);
                    }
                }


                if (paramMap.containsKey("acDelDetailsIds")) acRowsDeleted = request.getParameter("acDelDetailsIds");


                JsonElement purAChargesJson;
                if (!acRowsDeleted.equalsIgnoreCase("")) {
                    purAChargesJson = new JsonParser().parse(acRowsDeleted);
                    JsonArray deletedArrays = purAChargesJson.getAsJsonArray();
                    if (deletedArrays.size() > 0) {
                        delAddCharges(deletedArrays);
                    }
                }
            }
            JsonArray paymentType = new JsonArray();

            String paymentMode = request.getParameter("paymentMode");
            /****** Changing the paymentMode from cash or bank account to credit *****/
            if (paymentMode.equalsIgnoreCase("credit")) {
                /**** make the status -> 0 for receipt generated against the invoice ,and reverse the
                 * posting of cash or bank account *****/
                // reversPostings(mSalesTranx,users,totalPaidAmt,paymentMode);
            }
            Double cashAmt = 0.0;

            if (paymentMode.equalsIgnoreCase("multi")) {

                String jsonStr = request.getParameter("payment_type");
                paymentType = new JsonParser().parse(jsonStr).getAsJsonArray();
                Double totalPaidAmt = Double.parseDouble(request.getParameter("p_totalAmount"));
                Double returnAmt = Double.parseDouble(request.getParameter("p_returnAmount"));
                if (!request.getParameter("cashAmt").isEmpty())
                    cashAmt = Double.parseDouble(request.getParameter("cashAmt"));
                /****** update payment type against sales invoice *****/
                insertIntoPaymentType(mSalesTranx, paymentType, paymentMode, "update");
                updateReceiptInvoice(mSalesTranx, users, paymentType, totalPaidAmt, returnAmt, paymentMode, "update", cashAmt);

            } else if (paymentMode.equalsIgnoreCase("cash")) {
                updateReceiptInvoice(mSalesTranx, users, paymentType, mSalesTranx.getTotalAmount(), 0.0, paymentMode, "update", cashAmt);
            }

            /* Remove all ledgers from DB if we found new input ledger id's while updating */
            for (Long mDblist : ledgerList) {
                if (!ledgerInputList.contains(mDblist)) {
                    salesInvoiceLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(mDblist, tranxType.getId(), mSalesTranx.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), mSalesTranx.getBillDate(), tranxType, fiscalYear, detail);
                    }
                    salesInvoiceLogger.info("removing unused previous ledger update done");
                }
            }
            responseMessage.setMessage("Sales invoice updated successfully ");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            /****** Update into Receipt  *****/
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoSalesInvoiceEdit :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
            responseMessage.setMessage("Unable to update sales invoice");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoSalesInvoiceEdit :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
            responseMessage.setMessage("Unable to update sales invoice ");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }


    public void saveIntoSalesAdditionalChargesEdit(JsonArray additionalCharges, TranxSalesInvoice mSalesInvoie, TransactionTypeMaster tranxTypeMater, Long outletId, String acRowsDeleted) {

        List<TranxSalesInvoiceAdditionalCharges> chargesList = new ArrayList<>();
        for (JsonElement mAddCharges : additionalCharges) {
            JsonObject object = mAddCharges.getAsJsonObject();
            Double amount = 0.0;
            Long detailsId = 0L;
            if (object.has("amt") && !object.get("amt").getAsString().equalsIgnoreCase("")) {
                amount = object.get("amt").getAsDouble();
                Long ledgerId = object.get("ledgerId").getAsLong();
                if (object.has("additional_charges_details_id"))
                    detailsId = object.get("additional_charges_details_id").getAsLong();
                LedgerMaster addcharges = null;
                TranxSalesInvoiceAdditionalCharges charges = null;
                charges = salesAdditionalChargesRepository.findByAdditionalChargesIdAndSalesTransactionIdAndStatus(ledgerId, mSalesInvoie.getId(), true);
                if (detailsId == 0L) {
                    charges = new TranxSalesInvoiceAdditionalCharges();
                }
                addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges.setAmount(amount);
                charges.setAdditionalCharges(addcharges);
                charges.setSalesTransaction(mSalesInvoie);
                charges.setStatus(true);

                chargesList.add(charges);
                salesAdditionalChargesRepository.save(charges);
                Boolean isContains = dbList.contains(addcharges.getId());
                Boolean isLedgerContains = ledgerList.contains(addcharges.getId());
                mInputList.add(addcharges.getId());
                ledgerInputList.add(addcharges.getId());
                if (isContains) {
                    //      transactionDetailsRepository.ledgerPostingEdit(addcharges.getId(), mPurchaseTranx.getId(), tranxTypeMater.getId(), "DR", amount * -1);
                    /**** New Postings Logic *****/
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(addcharges.getId(), tranxTypeMater.getId(), mSalesInvoie.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(amount);
                        mLedger.setTransactionDate(mSalesInvoie.getBillDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    /* insert */
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(amount, addcharges, tranxTypeMater, addcharges.getAssociateGroups(), mSalesInvoie.getFiscalYear(), mSalesInvoie.getBranch(), mSalesInvoie.getOutlet(), mSalesInvoie.getBillDate(), mSalesInvoie.getId(), mSalesInvoie.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Insert");
                }

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.callToPostingLedgerForUpdateByDetailsId(amount, addcharges.getId(), tranxTypeMater, "CR", mSalesInvoie.getId(), addcharges, mSalesInvoie.getBillDate(), mSalesInvoie.getFiscalYear(), mSalesInvoie.getOutlet(), mSalesInvoie.getBranch(), mSalesInvoie.getTranxCode());
            }
        }

    }


    public void delAddCharges(JsonArray deletedArrays) {

        TranxSalesInvoiceAdditionalCharges mDeletedInvoices = null;
        for (JsonElement element : deletedArrays) {
            JsonObject deletedRowsId = element.getAsJsonObject();
            mDeletedInvoices = salesAdditionalChargesRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
            if (mDeletedInvoices != null) {
                mDeletedInvoices.setStatus(false);
                try {
                    salesAdditionalChargesRepository.save(mDeletedInvoices);
                } catch (DataIntegrityViolationException de) {
                    salesInvoiceLogger.error("Error into Sales Invoice Add.Charges Edit" + de.getMessage());
                    de.printStackTrace();
                    System.out.println("Exception:" + de.getMessage());

                } catch (Exception ex) {
                    salesInvoiceLogger.error("Error into Sales Invoice Add.Charges Edit" + ex.getMessage());
                    ex.printStackTrace();
                    System.out.println("Exception into Sales Invoice Add.Charges Edit:" + ex.getMessage());
                }
            }
        }
    }


    /* For Consumer Sales Details Edit */
    public void saveIntoSalesCompInvoiceDetailsEdit(JsonArray array, TranxSalesCompInvoice mSalesTranx, Branch branch, Outlet outlet, Long userId, String rowsDeleted, TransactionTypeMaster tranxType) {

        for (JsonElement mList : array) {
            JsonObject object = mList.getAsJsonObject();
            /* Purchase Invoice Unit Edit */
            Long details_id = object.get("details_id").getAsLong();
            TranxSalesCompInvoiceDetailsUnits invoiceUnits = new TranxSalesCompInvoiceDetailsUnits();
            FiscalYear fiscalYear = fiscalYearRepository.findById(mSalesTranx.getFiscalYearId()).get();
            if (details_id != 0) {
                invoiceUnits = tranxSalesCompInvoiceDetailsUnitRepository.findByIdAndStatus(details_id, true);
            } else {
                invoiceUnits.setReturnQty(0.0);
                invoiceUnits.setTransactionStatusId(1L);
            }
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            String batchNo = null;
            String serialNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            double free_qty = 0.0;
            Long batchId = null;
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
            invoiceUnits.setSalesInvoiceId(mSalesTranx.getId());
            invoiceUnits.setProductId(mProduct.getId());
            invoiceUnits.setUnitsId(units.getId());
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase("")) {
                free_qty = object.get("free_qty").getAsDouble();
                invoiceUnits.setFreeQty(free_qty);
            }
            invoiceUnits.setRate(object.get("rate").getAsDouble());
            if (levelA != null) invoiceUnits.setLevelAId(levelA.getId());
            if (levelB != null) invoiceUnits.setLevelBId(levelB.getId());
            if (levelC != null) invoiceUnits.setLevelCId(levelC.getId());
            invoiceUnits.setStatus(true);
            if (object.has("base_amt")) invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv") && !object.get("unit_conv").getAsString().equals(""))
                invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            invoiceUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            invoiceUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            invoiceUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            invoiceUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
            invoiceUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
            invoiceUnits.setAdditionChargesAmt(object.get("add_chg_amt").getAsDouble());
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
            boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                    Double qnty = object.get("qty").getAsDouble();
                    productBatchNo.setQnty(qnty.intValue());
                    productBatchNo.setSalesRate(object.get("rate").getAsDouble());
                    productBatchNoRepository.save(productBatchNo);
                    batchNo = productBatchNo.getBatchNo();
                    batchId = productBatchNo.getId();
                }
                invoiceUnits.setProductBatchNoId(productBatchNo.getId());
                TranxSalesCompInvoiceDetailsUnits tranxSalesInvoiceDetailsUnits = tranxSalesCompInvoiceDetailsUnitRepository.save(invoiceUnits);
                if (flag == false) {
                    JsonArray jsonArray = object.getAsJsonArray("serialNo");
                    if (jsonArray != null && jsonArray.size() > 0) {
                        List<TranxSalesCompInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            Long detailsId = jsonSrno.get("serial_detail_id").getAsLong();
                            if (detailsId == 0) {
                                TranxSalesCompInvoiceProductSrNumber productSerialNumber = new TranxSalesCompInvoiceProductSrNumber();
                                productSerialNumber.setProductId(mProduct.getId());
                                productSerialNumber.setSerialNo(serialNo);
                                // productSerialNumber.setPurchaseTransaction(mPurchaseTranx);
                                productSerialNumber.setTransactionStatus("Sales");
                                productSerialNumber.setStatus(true);
                                productSerialNumber.setCreatedBy(userId);
                                productSerialNumber.setOperations("Inserted");
                                productSerialNumber.setTransactionTypeMasterId(tranxType.getId());
                                productSerialNumber.setBranchId(mSalesTranx.getBranchId());
                                productSerialNumber.setOutletId(mSalesTranx.getOutletId());
                                productSerialNumber.setTransactionTypeMasterId(tranxType.getId());
                                productSerialNumber.setUnitsId(units.getId());
                                productSerialNumber.setTranxSalesCompInvoiceDetailsUnitsId(tranxSalesInvoiceDetailsUnits.getId());
                                productSerialNumber.setLevelAId(levelA.getId());
                                productSerialNumber.setLevelBId(levelB.getId());
                                productSerialNumber.setLevelCId(levelC.getId());
                                productSerialNumber.setUnitsId(units.getId());
                                TranxSalesCompInvoiceProductSrNumber mSerialNo = serialCompNumberRepository.save(productSerialNumber);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, fiscalYear, serialNo);
                                }
                            } else {
                                TranxSalesInvoiceProductSrNumber productSerialNumber1 = serialNumberRepository.findByIdAndStatus(detailsId, true);
                                productSerialNumber1.setSerialNo(serialNo);
                                productSerialNumber1.setUpdatedBy(userId);
                                productSerialNumber1.setOperations("Updated");
                                TranxSalesInvoiceProductSrNumber mSerialNo = serialNumberRepository.save(productSerialNumber1);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToEditInventoryPostings(mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, fiscalYear);
                                }
                            }
                        }
                    }
                    flag = true;
                }
            } catch (Exception e) {
                salesInvoiceLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            try {
               /* if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                    flag = true;
                }*/
                /**** Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    if (details_id != 0) {
                        inventoryCommonPostings.callToEditInventoryPostings(mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, fiscalYear);
                    } else {
                        inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, fiscalYear, serialNo);
                    }
                    /***** End of new architecture of Inventory Postings *****/
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            }
        }
        /* if product is deleted from details table from front end, while user edit the sale */
        JsonParser parser = new JsonParser();
        JsonElement salesDetailsJson;
        if (!rowsDeleted.equalsIgnoreCase("")) {
            salesDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = salesDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxSalesCompInvoiceDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    mDeletedInvoices = tranxSalesCompInvoiceDetailsUnitRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                    if (mDeletedInvoices != null) {
                        mDeletedInvoices.setStatus(false);
                        try {
                            tranxSalesCompInvoiceDetailsUnitRepository.save(mDeletedInvoices);
                            /***** inventory effects of deleted rows *****/
                            LevelA levelA = null;
                            LevelB levelB = null;
                            LevelC levelC = null;
                            levelA = levelARepository.findByIdAndStatus(mDeletedInvoices.getLevelAId(), true);
                            levelB = levelBRepository.findByIdAndStatus(mDeletedInvoices.getLevelBId(), true);
                            levelC = levelCRepository.findByIdAndStatus(mDeletedInvoices.getLevelCId(), true);
                            TranxSalesCompInvoice salesInvoice = salesCompTransactionRepository.findById(mDeletedInvoices.getSalesInvoiceId()).get();
                            Product product = productRepository.findByIdAndStatus(mDeletedInvoices.getProductId(), true);
                            Units unit = unitsRepository.findByIdAndStatus(mDeletedInvoices.getUnitsId(), true);
                            ProductBatchNo batchNo = productBatchNoRepository.findByIdAndStatus(mDeletedInvoices.getProductBatchNoId(), true);
                            FiscalYear fiscalYear = fiscalYearRepository.findById(salesInvoice.getFiscalYearId()).get();

                            inventoryCommonPostings.callToInventoryPostings("CR", salesInvoice.getBillDate(), mDeletedInvoices.getSalesInvoiceId(), mDeletedInvoices.getQty() + mDeletedInvoices.getFreeQty(), branch, outlet, product, tranxType, levelA, levelB, levelC, unit, batchNo, batchNo.getBatchNo(), fiscalYear, null);
                            /***** End of new architecture of Inventory Postings *****/
                        } catch (DataIntegrityViolationException de) {
                            salesInvoiceLogger.error("Error in saveInto Consumer Sales Invoice Details Edit" + de.getMessage());
                            de.printStackTrace();
                            System.out.println("Exception:" + de.getMessage());
                        } catch (Exception ex) {
                            salesInvoiceLogger.error("Error in saveInto Consumer Sales Invoice Details Edit" + ex.getMessage());
                            ex.printStackTrace();
                            System.out.println("Exception save Into Consumer Sales Invoice Details Edit:" + ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    /* for Sales Edit */
    public void saveIntoSalesInvoiceDetailsEdit(JsonArray array, TranxSalesInvoice mSalesTranx, Branch branch, Outlet outlet, Long userId, String rowsDeleted, TransactionTypeMaster tranxType) {

        for (JsonElement mList : array) {
            JsonObject object = mList.getAsJsonObject();
            /* Purchase Invoice Unit Edit */
            Long details_id = object.get("details_id").getAsLong();
            TranxSalesInvoiceDetailsUnits invoiceUnits = new TranxSalesInvoiceDetailsUnits();
            if (details_id != 0) {
                invoiceUnits = tranxSalesInvoiceDetailsUnitRepository.findByIdAndStatus(details_id, true);
            } else {
                invoiceUnits.setReturnQty(0.0);
                invoiceUnits.setTransactionStatusId(1L);
            }
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            String batchNo = null;
            String serialNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
            Double tranxQty = 0.0;
            Long batchId = null;
            tranxQty = object.get("qty").getAsDouble();
            double free_qty = 0.0;
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
            invoiceUnits.setSalesInvoice(mSalesTranx);
            invoiceUnits.setProduct(mProduct);
            invoiceUnits.setUnits(units);
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase("")) {
                free_qty = object.get("free_qty").getAsDouble();
                invoiceUnits.setFreeQty(free_qty);
            }
            invoiceUnits.setRate(object.get("rate").getAsDouble());
            if (levelA != null) invoiceUnits.setLevelAId(levelA.getId());
            if (levelB != null) invoiceUnits.setLevelBId(levelB.getId());
            if (levelC != null) invoiceUnits.setLevelCId(levelC.getId());
            invoiceUnits.setStatus(true);
            if (object.has("base_amt")) invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv") && !object.get("unit_conv").getAsString().equals(""))
                invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            invoiceUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            invoiceUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            invoiceUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            invoiceUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
            invoiceUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
            invoiceUnits.setAdditionChargesAmt(object.get("add_chg_amt").getAsDouble());
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
            boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                    /*productBatchNo.setQnty(object.get("qty").getAsInt());
                    productBatchNo.setSalesRate(object.get("rate").getAsDouble());*/
                    productBatchNoRepository.save(productBatchNo);
                    batchNo = productBatchNo.getBatchNo();
                    batchId = productBatchNo.getId();
                }
                invoiceUnits.setProductBatchNo(productBatchNo);
                TranxSalesInvoiceDetailsUnits tranxSalesInvoiceDetailsUnits = tranxSalesInvoiceDetailsUnitRepository.save(invoiceUnits);
                if (flag == false) {
                    JsonArray jsonArray = object.getAsJsonArray("serialNo");
                    if (jsonArray != null && jsonArray.size() > 0) {
                        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            Long detailsId = jsonSrno.get("serial_detail_id").getAsLong();
                            if (detailsId == 0) {
                                TranxSalesInvoiceProductSrNumber productSerialNumber = new TranxSalesInvoiceProductSrNumber();
                                productSerialNumber.setProduct(mProduct);
                                productSerialNumber.setSerialNo(serialNo);
                                // productSerialNumber.setPurchaseTransaction(mPurchaseTranx);
                                productSerialNumber.setTransactionStatus("Sales");
                                productSerialNumber.setStatus(true);
                                productSerialNumber.setCreatedBy(userId);
                                productSerialNumber.setOperations("Inserted");
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setBranch(mSalesTranx.getBranch());
                                productSerialNumber.setOutlet(mSalesTranx.getOutlet());
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setUnits(units);
                                productSerialNumber.setTranxSalesInvoiceDetailsUnits(tranxSalesInvoiceDetailsUnits);
                                productSerialNumber.setLevelA(levelA);
                                productSerialNumber.setLevelB(levelB);
                                productSerialNumber.setLevelC(levelC);
                                productSerialNumber.setUnits(units);
                                TranxSalesInvoiceProductSrNumber mSerialNo = serialNumberRepository.save(productSerialNumber);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), serialNo);
                                }
                            } else {
                                TranxSalesInvoiceProductSrNumber productSerialNumber1 = serialNumberRepository.findByIdAndStatus(detailsId, true);
                                productSerialNumber1.setSerialNo(serialNo);
                                productSerialNumber1.setUpdatedBy(userId);
                                productSerialNumber1.setOperations("Updated");
                                TranxSalesInvoiceProductSrNumber mSerialNo = serialNumberRepository.save(productSerialNumber1);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToEditInventoryPostings(mSalesTranx.getBillDate(), mSalesTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear());
                                }
                            }
                        }
                    }
                    flag = true;
                }
            } catch (Exception e) {
                salesInvoiceLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            try {
               /* if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
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
                        InventorySummaryTransactionDetails productRow = stkTranxDetailsRepository.findByProductIdAndTranxTypeIdAndTranxId(mProduct.getId(), tranxType.getId(), mSalesTranx.getId());
                        if (productRow != null) {
                            if (mSalesTranx.getBillDate().compareTo(productRow.getTranxDate()) == 0 && tranxQty != invoiceUnits.getQty()) { //DATE SAME AND QTY DIFFERENT
                                Double closingStk = closingUtility.CAL_CR_STOCK(productRow.getOpeningStock(), tranxQty, free_qty);
                                productRow.setQty(tranxQty + free_qty);
                                productRow.setClosingStock(closingStk);
                                InventorySummaryTransactionDetails mInventory = stkTranxDetailsRepository.save(productRow);
                                closingUtility.updatePosting(mInventory, mProduct.getId(), mSalesTranx.getBillDate());
                            } else if (mSalesTranx.getBillDate().compareTo(productRow.getTranxDate()) != 0) { // DATE IS DIFFERENT
                                Date oldDate = productRow.getTranxDate();
                                Date newDate = mSalesTranx.getBillDate();
                                if (newDate.after(oldDate)) { //FORWARD INSERT
                                    productRow.setStatus(false);
                                    stkTranxDetailsRepository.save(productRow);
                                    Double opening = productRow.getOpeningStock();
                                    Double closing = 0.0;
                                    List<InventorySummaryTransactionDetails> openingClosingList = stkTranxDetailsRepository.getBetweenDateProductId(mProduct.getId(), oldDate, newDate, productRow.getId(), true);
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
                                closingUtility.stockPosting(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId, mProduct, tranxType.getId(), newDate, invoiceUnits.getQty(), free_qty, mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo, mSalesTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());
                                closingUtility.stockPostingBatchWise(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId, mProduct, tranxType.getId(), newDate, invoiceUnits.getQty(), free_qty, mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo, mSalesTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());
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
        /* if product is deleted from details table from front end, while user edit the sale */
        JsonParser parser = new JsonParser();
        JsonElement salesDetailsJson;
        if (!rowsDeleted.equalsIgnoreCase("")) {
            salesDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = salesDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxSalesInvoiceDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    mDeletedInvoices = tranxSalesInvoiceDetailsUnitRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                    if (mDeletedInvoices != null) {
                        mDeletedInvoices.setStatus(false);
                        try {
                            tranxSalesInvoiceDetailsUnitRepository.save(mDeletedInvoices);
                            /***** inventory effects of deleted rows *****/
                            LevelA levelA = null;
                            LevelB levelB = null;
                            LevelC levelC = null;
                            levelA = levelARepository.findByIdAndStatus(mDeletedInvoices.getLevelAId(), true);
                            levelB = levelBRepository.findByIdAndStatus(mDeletedInvoices.getLevelBId(), true);
                            levelC = levelCRepository.findByIdAndStatus(mDeletedInvoices.getLevelCId(), true);
                            inventoryCommonPostings.callToInventoryPostings("CR", mDeletedInvoices.getSalesInvoice().getBillDate(), mDeletedInvoices.getSalesInvoice().getId(), mDeletedInvoices.getQty() + mDeletedInvoices.getFreeQty(), branch, outlet, mDeletedInvoices.getProduct(), tranxType, levelA, levelB, levelC, mDeletedInvoices.getUnits(), mDeletedInvoices.getProductBatchNo(), mDeletedInvoices.getProductBatchNo().getBatchNo(), mDeletedInvoices.getSalesInvoice().getFiscalYear(), null);
                            /***** End of new architecture of Inventory Postings *****/
                        } catch (DataIntegrityViolationException de) {
                            salesInvoiceLogger.error("Error in saveInto Sales Invoice Details Edit" + de.getMessage());
                            de.printStackTrace();
                            System.out.println("Exception:" + de.getMessage());
                        } catch (Exception ex) {
                            salesInvoiceLogger.error("Error in saveInto Sales Invoice Details Edit" + ex.getMessage());
                            ex.printStackTrace();
                            System.out.println("Exception save Into Sales Invoice Details Edit:" + ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    /* for Sales Details Edit */
    private void insertIntoSalesInvoiceDetailsHistory(List<TranxSalesInvoiceDetails> row) {
        for (TranxSalesInvoiceDetails mRow : row) {
            mRow.setStatus(false);
            mRow.setOperations("updated");
            salesInvoiceDetailsRepository.save(mRow);
        }
    }

    /* End of Sales Additional Charges Edit */
    public void insertIntoSalesInvoiceHistory(TranxSalesInvoice invoiceTranx) {
        invoiceTranx.setStatus(false);
        salesTransactionRepository.save(invoiceTranx);
    }


    /* for Consumer sales duties and taxes edit */
    public void saveIntoSalesCompDutiesTaxesEdit(JsonObject duties_taxes, TranxSalesCompInvoice invoiceTranx, Boolean taxFlag, TransactionTypeMaster tranxType, Long outletId, Long userId) {

        Branch branch = null;
        if (invoiceTranx.getBranchId() != null)
            branch = branchRepository.findByIdAndStatus(invoiceTranx.getBranchId(), true);
        Outlet outlet = outletRepository.findByIdAndStatus(invoiceTranx.getOutletId(), true);
        FiscalYear fiscalYear = fiscalYearRepository.findById(invoiceTranx.getFiscalYearId()).get();

        /* sales Duties and Taxes */
        List<TranxSalesCompInvoiceDutiesTaxes> salesDutiesTaxes = new ArrayList<>();
        List<Long> db_dutiesLedgerIds = salesCompDutiesTaxesRepository.findByDutiesAndTaxesId(invoiceTranx.getId());
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId, branch != null ? branch.getId() : null);
        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            if (db_dutiesLedgerIds.size() > 0) {
                salesDutiesTaxes = salesCompDutiesTaxesRepository.findBySalesTransactionIdAndStatus(invoiceTranx.getId(), true);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesCompInvoiceDutiesTaxes taxes = new TranxSalesCompInvoiceDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT CGST " + inputGst;
                        if (branch != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutletId(), invoiceTranx.getBranchId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutletId(), ledgerName);

                        Double amt = cgstObject.get("amt").getAsDouble();
                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxesId(dutiesTaxes.getId());
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            ledgerInputList.add(dutiesTaxes.getId());
                            if (isContains) {
                                /**** New Postings Logic *****/
                                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                if (mLedger != null) {
                                    mLedger.setAmount(amt);
                                    mLedger.setTransactionDate(invoiceTranx.getBillDate());
                                    mLedger.setOperations("updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /* insert */
//                                transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(dutiesTaxes.getPrinciples().getFoundations().getId(), dutiesTaxes.getPrinciples().getId(), dutiesTaxes.getPrincipleGroups() != null ? dutiesTaxes.getPrincipleGroups().getId() : null, invoiceTranx.getAssociateGroups() != null ? invoiceTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null, invoiceTranx.getOutlet().getId(), "pending", amt * -1, 0.0, invoiceTranx.getBillDate(), null, invoiceTranx.getId(), tranxType.getTransactionName(), dutiesTaxes.getUnderPrefix(), invoiceTranx.getFinancialYear(), invoiceTranx.getCreatedBy(), dutiesTaxes.getId(), invoiceTranx.getSalesInvoiceNo());
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), fiscalYear, branch, outlet, invoiceTranx.getBillDate(), invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType, "CR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getBillDate(), fiscalYear, outlet, branch, invoiceTranx.getTranxCode());
                        }

                        taxes.setAmount(amt);
                        taxes.setStatus(true);
                        taxes.setSalesTransactionId(invoiceTranx.getId());
                        taxes.setSundryDebtorsId(invoiceTranx.getSundryDebtorsId());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesCompInvoiceDutiesTaxes taxes = new TranxSalesCompInvoiceDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //     int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        if (branch != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutletId(), invoiceTranx.getBranchId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutletId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxesId(dutiesTaxes.getId());
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            ledgerInputList.add(dutiesTaxes.getId());
                            if (isContains) {
                                /**** New Postings Logic *****/
                                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                if (mLedger != null) {
                                    mLedger.setAmount(amt);
                                    mLedger.setTransactionDate(invoiceTranx.getBillDate());
                                    mLedger.setOperations("updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /* insert */
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), fiscalYear, branch, outlet, invoiceTranx.getBillDate(), invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType, "CR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getBillDate(), fiscalYear, outlet, branch, invoiceTranx.getTranxCode());
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransactionId(invoiceTranx.getId());
                        taxes.setSundryDebtorsId(invoiceTranx.getSundryDebtorsId());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxSalesCompInvoiceDutiesTaxes taxes = new TranxSalesCompInvoiceDutiesTaxes();
                        JsonObject igstObject = mIgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        if (branch != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutletId(), invoiceTranx.getBranchId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutletId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxesId(dutiesTaxes.getId());
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            ledgerInputList.add(dutiesTaxes.getId());
                            if (isContains) {
                                /**** New Postings Logic *****/
                                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                if (mLedger != null) {
                                    mLedger.setAmount(amt);
                                    mLedger.setTransactionDate(invoiceTranx.getBillDate());
                                    mLedger.setOperations("updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), fiscalYear, branch, outlet, invoiceTranx.getBillDate(), invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType, "CR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getBillDate(), fiscalYear, outlet, branch, invoiceTranx.getTranxCode());

                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransactionId(invoiceTranx.getId());
                        taxes.setSundryDebtorsId(invoiceTranx.getSundryDebtorsId());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        } else {
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesCompInvoiceDutiesTaxes taxes = new TranxSalesCompInvoiceDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT CGST " + inputGst;
                        Double amt = cgstObject.get("amt").getAsDouble();
                        if (branch != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutletId(), invoiceTranx.getBranchId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutletId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxesId(dutiesTaxes.getId());
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransactionId(invoiceTranx.getId());
                        taxes.setSundryDebtorsId(invoiceTranx.getSundryDebtorsId());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesCompInvoiceDutiesTaxes taxes = new TranxSalesCompInvoiceDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        if (branch != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutletId(), invoiceTranx.getBranchId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutletId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxesId(dutiesTaxes.getId());
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransactionId(invoiceTranx.getId());
                        taxes.setSundryDebtorsId(invoiceTranx.getSundryDebtorsId());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxSalesCompInvoiceDutiesTaxes taxes = new TranxSalesCompInvoiceDutiesTaxes();
                        JsonObject igstObject = igstList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //     int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        if (branch != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutletId(), invoiceTranx.getBranchId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutletId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxesId(dutiesTaxes.getId());
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesTransactionId(invoiceTranx.getId());
                        taxes.setSundryDebtorsId(invoiceTranx.getSundryDebtorsId());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        }
        salesCompDutiesTaxesRepository.saveAll(salesDutiesTaxes);
    }


    /* for Sales Invoice Edit */
    public void saveIntoSalesDutiesTaxesEdit(JsonObject duties_taxes, TranxSalesInvoice invoiceTranx, Boolean taxFlag, TransactionTypeMaster tranxType, Long outletId, Long userId) {
        /* sales Duties and Taxes */
        List<TranxSalesInvoiceDutiesTaxes> salesDutiesTaxes = new ArrayList<>();
        List<Long> db_dutiesLedgerIds = salesDutiesTaxesRepository.findByDutiesAndTaxesId(invoiceTranx.getId());
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null);
        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            if (db_dutiesLedgerIds.size() > 0) {
                salesDutiesTaxes = salesDutiesTaxesRepository.findBySalesTransactionAndStatus(invoiceTranx, true);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesInvoiceDutiesTaxes taxes = new TranxSalesInvoiceDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT CGST " + inputGst;
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        Double amt = cgstObject.get("amt").getAsDouble();
                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            ledgerInputList.add(dutiesTaxes.getId());
                            if (isContains) {
                                /**** New Postings Logic *****/
                                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                if (mLedger != null) {
                                    mLedger.setAmount(amt);
                                    mLedger.setTransactionDate(invoiceTranx.getBillDate());
                                    mLedger.setOperations("updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /* insert */
//                                transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(dutiesTaxes.getPrinciples().getFoundations().getId(), dutiesTaxes.getPrinciples().getId(), dutiesTaxes.getPrincipleGroups() != null ? dutiesTaxes.getPrincipleGroups().getId() : null, invoiceTranx.getAssociateGroups() != null ? invoiceTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null, invoiceTranx.getOutlet().getId(), "pending", amt * -1, 0.0, invoiceTranx.getBillDate(), null, invoiceTranx.getId(), tranxType.getTransactionName(), dutiesTaxes.getUnderPrefix(), invoiceTranx.getFinancialYear(), invoiceTranx.getCreatedBy(), dutiesTaxes.getId(), invoiceTranx.getSalesInvoiceNo());
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getBillDate(), invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType, "CR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getBillDate(), invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(), invoiceTranx.getTranxCode());

                        }
                        taxes.setAmount(amt);
                        taxes.setStatus(true);
                        taxes.setSalesTransaction(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesInvoiceDutiesTaxes taxes = new TranxSalesInvoiceDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //     int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            ledgerInputList.add(dutiesTaxes.getId());
                            if (isContains) {
                                /**** New Postings Logic *****/
                                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                if (mLedger != null) {
                                    mLedger.setAmount(amt);
                                    mLedger.setTransactionDate(invoiceTranx.getBillDate());
                                    mLedger.setOperations("updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /* insert */
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getBillDate(), invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType, "CR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getBillDate(), invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(), invoiceTranx.getTranxCode());

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
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxSalesInvoiceDutiesTaxes taxes = new TranxSalesInvoiceDutiesTaxes();
                        JsonObject igstObject = mIgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            ledgerInputList.add(dutiesTaxes.getId());
                            if (isContains) {
                                /**** New Postings Logic *****/
                                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                if (mLedger != null) {
                                    mLedger.setAmount(amt);
                                    mLedger.setTransactionDate(invoiceTranx.getBillDate());
                                    mLedger.setOperations("updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getBillDate(), invoiceTranx.getId(), invoiceTranx.getSalesInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType, "CR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getBillDate(), invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(), invoiceTranx.getTranxCode());

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
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesInvoiceDutiesTaxes taxes = new TranxSalesInvoiceDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT CGST " + inputGst;
                        Double amt = cgstObject.get("amt").getAsDouble();
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
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesInvoiceDutiesTaxes taxes = new TranxSalesInvoiceDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
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
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxSalesInvoiceDutiesTaxes taxes = new TranxSalesInvoiceDutiesTaxes();
                        JsonObject igstObject = igstList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //     int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
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

    private void insertIntoDutiesAndTaxesHistory(List<TranxSalesInvoiceDutiesTaxes> salesDutiesTaxes) {
        for (TranxSalesInvoiceDutiesTaxes mList : salesDutiesTaxes) {
            mList.setStatus(false);
            salesDutiesTaxesRepository.save(mList);
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
                        returnLedgerIds.add(dutiesTaxes.getId());
                    }
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (JsonElement mSgst : sgstList) {
                    JsonObject sgstObject = mSgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //  int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT SGST " + inputGst;
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
                    // int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String inputGst = igstObject.get("gst").getAsString();
                    String ledgerName = "INPUT IGST " + inputGst;
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

    public JsonObject salesInvoiceDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesInvoice salesTranx = salesTransactionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TranxSalesInvoice mSales;
        TransactionTypeMaster invoiceTranx = tranxRepository.findByTransactionCodeIgnoreCase("SLS");
        TransactionTypeMaster returnTranx = tranxRepository.findByTransactionCodeIgnoreCase("SLSRT");
        try {
            salesTranx.setStatus(false);
            salesTranx.setOperations("deletion");
            /***** check whether the reference of sales invoice is available in sales return table or not,
             if available then delete all the sales returns and reverse the postings
             *****/
            List<TranxSalesReturnInvoice> salesReturnList = new ArrayList<>();
            salesReturnList = tranxSalesReturnRepository.findByTranxSalesInvoiceIdAndStatus(salesTranx.getId(), true);
            for (TranxSalesReturnInvoice mReturnInvoice : salesReturnList) {
                try {
                    mReturnInvoice.setStatus(false);
                    tranxSalesReturnRepository.save(mReturnInvoice);
                } catch (Exception e) {
                    salesInvoiceLogger.error("Exception in Deleting the Sales Return of Sales Invoice ->" + e.getMessage());
                    System.out.println("Exception in Deleting the Sales Return of Sales Invoice ->" + e.getMessage());
                }
            }
            List<TranxCreditNoteNewReferenceMaster> noteNewReferenceMasters = new ArrayList<>();
            noteNewReferenceMasters = tranxCreditNoteNewReferenceRepository.findBySalesInvoiceIdAndStatus(salesTranx.getId(), true);
            if (noteNewReferenceMasters != null && noteNewReferenceMasters.size() > 0) {
                for (TranxCreditNoteNewReferenceMaster mdebitNote : noteNewReferenceMasters) {
                    try {
                        mdebitNote.setStatus(false);
                        TranxSalesReturnInvoice returnInvoice = tranxSalesReturnRepository.findByIdAndStatus(mdebitNote.getTranxSalesReturnInvoiceId(), true);
                        deleteIntoLedgerTranxDetailsCreditNote(returnInvoice);//Accounting Postings
                        tranxCreditNoteNewReferenceRepository.save(mdebitNote);
                    } catch (Exception e) {
                        salesInvoiceLogger.error("Exception in Deleting the NewReference of Sales Invoice ->" + e.getMessage());
                        System.out.println("Exception in Deleting the NewReference of Sales Invoice ->" + e.getMessage());
                    }
                }
            }
            mSales = salesTransactionRepository.save(salesTranx);
            if (mSales != null) {
                deleteIntoLedgerTranxDetails(mSales);// Accounting Postings
                /**** make status=0 to all ledgers of respective sales invoice id, due to this we wont get
                 details of deleted invoice when we want get details of respective ledger ****/
                List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
                mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(3L, mSales.getId(), true);
                for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                    try {
                        mPostings.setStatus(false);
                        ledgerTransactionPostingsRepository.save(mPostings);
                    } catch (Exception e) {
                        salesInvoiceLogger.error("Exception in Delete functionality for all ledgers of" + " deleted sales invoice->" + e.getMessage());
                    }
                }
                /**** make status=0 to all ledgers of respective sales return id, due to this we won't get
                 details of deleted invoice when we want get details of respective ledger ****/
                for (TranxSalesReturnInvoice mList : salesReturnList) {
                    List<LedgerTransactionPostings> mReturnList = new ArrayList<>();
                    mReturnList = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(4L, mList.getId(), true);
                    for (LedgerTransactionPostings mPostings : mReturnList) {
                        try {
                            mPostings.setStatus(false);
                            ledgerTransactionPostingsRepository.save(mPostings);
                        } catch (Exception e) {
                            salesInvoiceLogger.error("Exception in Delete functionality for all ledgers of" + " deleted sales return invoice->" + e.getMessage());
                        }
                    }
                }
                /**** Inventory Postings of Sales Return Invoice Delete ****/

                for (TranxSalesReturnInvoice returnInvoice : salesReturnList) {
                    List<TranxSalesReturnDetailsUnits> returnList = new ArrayList<>();
                    returnList = tranxSalesReturnDetailsUnitsRepository.findBySalesReturnInvoiceIdAndStatus(returnInvoice.getId(), true);
                    for (TranxSalesReturnDetailsUnits mUnitObjects : returnList) {
                        /***** new architecture of Inventory Postings *****/
                        inventoryCommonPostings.callToInventoryPostings("DR", returnInvoice.getTransactionDate(), returnInvoice.getId(), mUnitObjects.getQty(), returnInvoice.getBranch(), returnInvoice.getOutlet(), mUnitObjects.getProduct(), returnTranx, null, null, null, mUnitObjects.getUnits(), mUnitObjects.getProductBatchNo(), mUnitObjects.getProductBatchNo() != null ? mUnitObjects.getProductBatchNo().getBatchNo() : null, returnInvoice.getFiscalYear(), null);
                        /***** End of new architecture of Inventory Postings *****/
                    }
                }
                /**** Inventory Postings of Purchase Invoice Delete ****/
                List<TranxSalesInvoiceDetailsUnits> unitsList = new ArrayList<>();
                unitsList = tranxSalesInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(mSales.getId(), true);
                for (TranxSalesInvoiceDetailsUnits mUnitObjects : unitsList) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("CR", mSales.getBillDate(), mSales.getId(), mUnitObjects.getQty(), mSales.getBranch(), mSales.getOutlet(), mUnitObjects.getProduct(), invoiceTranx, null, null, null, mUnitObjects.getUnits(), mUnitObjects.getProductBatchNo(), mUnitObjects.getProductBatchNo() != null ? mUnitObjects.getProductBatchNo().getBatchNo() : null, mSales.getFiscalYear(), null);
                    /***** End of new architecture of Inventory Postings *****/
                }
                jsonObject.addProperty("message", "sales invoice deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in purchase deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            salesInvoiceLogger.error("Error in salesDelete()->" + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject getInvoiceBillPrint(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesInvoiceDetailsUnits> list = new ArrayList<>();
        List<TranxCounterSalesDetailsUnits> listcs = new ArrayList<>();
        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxSalesInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        TranxSalesInvoice salesInvoice = null;
        TranxCounterSales counterSales = null;
        TranxSalesChallan salesChallan = null;
        TranxSalesCompInvoice compInvoice = null;


        String source = request.getParameter("source");
        String key = request.getParameter("print_type"); //check whether printbill is calling from create page or from list page
        /***** if  print_type is create, then use serialnumber of invoice to fetch invoice details ,
         * if print_type is list then use invoice id to fetch invoice details *****/
        try {
            String invoiceNo = request.getParameter("id");
            Long id = 0L;
            if (source.equalsIgnoreCase("sales_invoice")) { //counter_sales
                if (users.getBranch() != null) {
                    if (key.equalsIgnoreCase("create")) {
                        salesInvoice = salesTransactionRepository.findBySalesInvoiceNoAndOutletIdAndBranchIdAndStatus(invoiceNo, users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesInvoice = salesTransactionRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    }
                } else {
                    if (key.equalsIgnoreCase("create")) {
                        salesInvoice = salesTransactionRepository.findByOutletIdAndSalesInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), invoiceNo);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesInvoice = salesTransactionRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);
                    }
                }
            } else if (source.equalsIgnoreCase("sales_challan")) { //counter_sales
                if (users.getBranch() != null) {
                    if (key.equalsIgnoreCase("create")) {
                        salesChallan = tranxSalesChallanRepository.findBySalesChallanInvoiceNoAndOutletIdAndBranchIdAndStatus(invoiceNo, users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesChallan = tranxSalesChallanRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    }
                } else {
                    if (key.equalsIgnoreCase("create")) {
                        salesChallan = tranxSalesChallanRepository.findByOutletIdAndSalesChallanInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), invoiceNo);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesChallan = tranxSalesChallanRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);
                    }
                }
            } else if (key.equalsIgnoreCase("counter_sales")) {
                if (users.getBranch() != null) {
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
                }
            } else {
                if (users.getBranch() != null) {
                    if (key.equalsIgnoreCase("create")) {
                        compInvoice = compInvoiceRepository.findBySalesInvoiceNoAndOutletIdAndBranchIdAndStatus(invoiceNo, users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        compInvoice = compInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    }
                } else {
                    if (key.equalsIgnoreCase("create")) {
                        compInvoice = compInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        compInvoice = compInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    }
                }
            }
            if (salesInvoice != null) {
                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                debtorsObject.addProperty("supplier_name", salesInvoice.getSundryDebtors().getLedgerName());
                debtorsObject.addProperty("supplier_address", salesInvoice.getSundryDebtors().getAddress());
                debtorsObject.addProperty("supplier_state", String.valueOf(salesInvoice.getSundryDebtors().getState().getName()));
                debtorsObject.addProperty("supplier_gstin", salesInvoice.getSundryDebtors().getGstin());
                debtorsObject.addProperty("supplier_phone", salesInvoice.getSundryDebtors().getMobile());

                JsonObject invoiceObject = new JsonObject();
                /* Sales Invoice Data */
                invoiceObject.addProperty("id", salesInvoice.getId());
                invoiceObject.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
                invoiceObject.addProperty("invoice_no", salesInvoice.getSalesInvoiceNo());
                invoiceObject.addProperty("state_code", salesInvoice.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", salesInvoice.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(salesInvoice.getTaxableAmount()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(salesInvoice.getTotaligst()));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(salesInvoice.getTotalcgst()));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(salesInvoice.getTotalsgst()));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(salesInvoice.getTotalBaseAmount()));
                invoiceObject.addProperty("total_discount", numFormat.numFormat(salesInvoice.getTotalSalesDiscountAmt()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(salesInvoice.getTotalAmount()));
                invoiceObject.addProperty("advanced_amount", numFormat.numFormat(salesInvoice.getAdvancedAmount() != null ? salesInvoice.getAdvancedAmount() : 0.0));
                invoiceObject.addProperty("payment_mode", salesInvoice.getPaymentMode());


                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();
                /* getting Units of Sales Quotations*/
                List<TranxSalesInvoiceDetailsUnits> unitDetails = tranxSalesInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(salesInvoice.getId(), true);
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
                    mObject.addProperty("hsn", mUnit.getProduct().getProductHsn() != null ? mUnit.getProduct().getProductHsn().getHsnNumber() : "");
                    mObject.addProperty("mfg", mUnit.getProduct().getBrand() != null ? mUnit.getProduct().getBrand().getBrandName() : "");
                    mObject.addProperty("disc_per", mUnit.getDiscountPer() != null ? mUnit.getDiscountPer() : 0.00);
                    mObject.addProperty("tax_per", mUnit.getIgst() != null ? mUnit.getIgst() : 0.00);
                    mObject.addProperty("final_amt", mUnit.getFinalAmount());
                    mObject.addProperty("Gst", mUnit.getIgst());
                    mObject.addProperty("Cgst", mUnit.getSgst());
                    mObject.addProperty("Sgst", mUnit.getCgst());
                    if (mUnit.getProduct().getPackingMaster() != null) {
                        mObject.addProperty("packageId", mUnit.getProduct().getPackingMaster().getId());
                        mObject.addProperty("pack_name", mUnit.getProduct().getPackingMaster().getPackName());
                    } else {
                        mObject.addProperty("packageId", "");
                        mObject.addProperty("pack_name", "");
                    }
                   /* if (mUnit.getFlavourMaster() != null) {
                        mObject.addProperty("flavourId", mUnit.getFlavourMaster().getId());
                        mObject.addProperty("flavour_name", mUnit.getFlavourMaster().getFlavourName());
                    } else {
                        mObject.addProperty("flavourId", "");
                        mObject.addProperty("flavour_name", "");
                    }*/
                    if (mUnit.getProductBatchNo() != null) {
                        mObject.addProperty("b_details_id", mUnit.getProductBatchNo().getId());
                        mObject.addProperty("b_no", mUnit.getProductBatchNo().getBatchNo());
                        mObject.addProperty("is_batch", true);

                    } else {
                        mObject.addProperty("b_details_id", "");
                        mObject.addProperty("b_no", "");
                        mObject.addProperty("is_batch", false);
                    }

                    mObject.addProperty("b_no", mUnit.getProductBatchNo() != null ? mUnit.getProductBatchNo().getBatchNo() : "");
                    mObject.addProperty("mfg_date", mUnit.getProductBatchNo() != null ? (mUnit.getProductBatchNo().getManufacturingDate() != null ? mUnit.getProductBatchNo().getManufacturingDate().toString() : "") : "");
                    mObject.addProperty("exp_date", mUnit.getProductBatchNo() != null ? (mUnit.getProductBatchNo().getExpiryDate() != null ? mUnit.getProductBatchNo().getExpiryDate().toString() : "") : "");
                    mObject.addProperty("mrp", mUnit.getProductBatchNo() != null ? mUnit.getProductBatchNo().getMrp() : 0.00);

                    mObject.addProperty("details_id", mUnit.getId());
                    mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                    mObject.addProperty("unitId", mUnit.getUnits().getId());
                    mObject.addProperty("unit_name", mUnit.getUnits().getUnitName());
                    productDetails.add(mObject);
                });
                finalResult.add("product_details", productDetails);
                finalResult.add("supplier_data", companyObject);
                finalResult.add("customer_data", debtorsObject);
                finalResult.add("invoice_data", invoiceObject);
            } else if (salesChallan != null) {
                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                debtorsObject.addProperty("supplier_name", salesChallan.getSundryDebtors().getLedgerName());
                debtorsObject.addProperty("supplier_address", salesChallan.getSundryDebtors().getAddress());
                debtorsObject.addProperty("supplier_state", String.valueOf(salesChallan.getSundryDebtors().getState().getName()));
                debtorsObject.addProperty("supplier_gstin", salesChallan.getSundryDebtors().getGstin());
                debtorsObject.addProperty("supplier_phone", salesChallan.getSundryDebtors().getMobile());

                JsonObject invoiceObject = new JsonObject();
                /* Sales Invoice Data */
                invoiceObject.addProperty("id", salesChallan.getId());
                invoiceObject.addProperty("invoice_dt", salesChallan.getBillDate().toString());
                invoiceObject.addProperty("invoice_no", salesChallan.getSalesChallanInvoiceNo());
                invoiceObject.addProperty("state_code", salesChallan.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", salesChallan.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(salesChallan.getTaxableAmount()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(salesChallan.getTotaligst()));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(salesChallan.getTotalcgst()));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(salesChallan.getTotalsgst()));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(salesChallan.getTotalBaseAmount()));
                invoiceObject.addProperty("total_discount", numFormat.numFormat(salesChallan.getTotalSalesDiscountAmt()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(salesChallan.getTotalAmount()));
//                invoiceObject.addProperty("advanced_amount", numFormat.numFormat(salesChallan.getAdvancedAmount() != null ? salesChallan.getAdvancedAmount() : 0.0));
//                invoiceObject.addProperty("payment_mode", salesChallan.getPaymentMode());


                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();
                /* getting Units of Sales Quotations*/
                List<TranxSalesChallanDetailsUnits> unitDetails = tranxSalesChallanDetailsUnitsRepository.findBySalesChallanIdAndStatus(salesChallan.getId(), true);
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
                    mObject.addProperty("hsn", mUnit.getProduct().getProductHsn() != null ? mUnit.getProduct().getProductHsn().getHsnNumber() : "");
                    mObject.addProperty("mfg", mUnit.getProduct().getBrand() != null ? mUnit.getProduct().getBrand().getBrandName() : "");
                    mObject.addProperty("disc_per", mUnit.getDiscountPer() != null ? mUnit.getDiscountPer() : 0.00);
                    mObject.addProperty("tax_per", mUnit.getIgst() != null ? mUnit.getIgst() : 0.00);
                    mObject.addProperty("final_amt", mUnit.getFinalAmount());
                    mObject.addProperty("Gst", mUnit.getIgst());
                    mObject.addProperty("Cgst", mUnit.getSgst());
                    mObject.addProperty("Sgst", mUnit.getCgst());
                    if (mUnit.getProduct().getPackingMaster() != null) {
                        mObject.addProperty("packageId", mUnit.getProduct().getPackingMaster().getId());
                        mObject.addProperty("pack_name", mUnit.getProduct().getPackingMaster().getPackName());
                    } else {
                        mObject.addProperty("packageId", "");
                        mObject.addProperty("pack_name", "");
                    }
                   /* if (mUnit.getFlavourMaster() != null) {
                        mObject.addProperty("flavourId", mUnit.getFlavourMaster().getId());
                        mObject.addProperty("flavour_name", mUnit.getFlavourMaster().getFlavourName());
                    } else {
                        mObject.addProperty("flavourId", "");
                        mObject.addProperty("flavour_name", "");
                    }*/
                    if (mUnit.getProductBatchNo() != null) {
                        mObject.addProperty("b_details_id", mUnit.getProductBatchNo().getId());
                        mObject.addProperty("b_no", mUnit.getProductBatchNo().getBatchNo());
                        mObject.addProperty("is_batch", true);

                    } else {
                        mObject.addProperty("b_details_id", "");
                        mObject.addProperty("b_no", "");
                        mObject.addProperty("is_batch", false);
                    }

                    mObject.addProperty("b_no", mUnit.getProductBatchNo() != null ? mUnit.getProductBatchNo().getBatchNo() : "");
                    mObject.addProperty("mfg_date", mUnit.getProductBatchNo() != null ? (mUnit.getProductBatchNo().getManufacturingDate() != null ? mUnit.getProductBatchNo().getManufacturingDate().toString() : "") : "");
                    mObject.addProperty("exp_date", mUnit.getProductBatchNo() != null ? (mUnit.getProductBatchNo().getExpiryDate() != null ? mUnit.getProductBatchNo().getExpiryDate().toString() : "") : "");
                    mObject.addProperty("mrp", mUnit.getProductBatchNo() != null ? mUnit.getProductBatchNo().getMrp() : 0.00);

                    mObject.addProperty("details_id", mUnit.getId());
                    mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                    mObject.addProperty("unitId", mUnit.getUnits().getId());
                    mObject.addProperty("unit_name", mUnit.getUnits().getUnitName());
                    productDetails.add(mObject);
                });
                finalResult.add("product_details", productDetails);
                finalResult.add("supplier_data", companyObject);
                finalResult.add("customer_data", debtorsObject);
                finalResult.add("invoice_data", invoiceObject);
            } else if (counterSales != null) {
//                listcs = tranxCSDetailsUnitsRepository.findByCounterSalesIdAndStatus(counterSales.getId(), true);
                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                debtorsObject.addProperty("supplier_name", counterSales.getCustomerName());
//                debtorsObject.addProperty("supplier_address", counterSales.getCustomerName());
//                debtorsObject.addProperty("supplier_gstin", counterSales.getSundryDebtors().getGstin());
//                debtorsObject.addProperty("supplier_phone", counterSales.getSundryDebtors().getMobile());

                JsonObject invoiceObject = new JsonObject();
                /* Sales Invoice Data */
                invoiceObject.addProperty("id", counterSales.getId());
                invoiceObject.addProperty("invoice_dt", counterSales.getCounterSalesDate() != null ? counterSales.getCounterSalesDate().toString() : "");
                invoiceObject.addProperty("invoice_no", counterSales.getCounterSaleNo());
                invoiceObject.addProperty("state_code", counterSales.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", counterSales.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(counterSales.getTotalBaseAmt()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(counterSales.getTotaligst() != null ? counterSales.getTotaligst() : 0));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(counterSales.getTotalcgst() != null ? counterSales.getTotalcgst() : 0));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(counterSales.getTotalsgst() != null ? counterSales.getTotalsgst() : 0));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(counterSales.getTotalBaseAmt()));
                invoiceObject.addProperty("total_discount", numFormat.numFormat(counterSales.getTotalDiscount()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(counterSales.getTotalBill()));
                invoiceObject.addProperty("advanced_amount", numFormat.numFormat(counterSales.getAdvancedAmount() != null ? salesInvoice.getAdvancedAmount() : 0.0));
                invoiceObject.addProperty("payment_mode", counterSales.getPaymentMode());
                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                /* getting Units of Sales Quotations*/
                List<TranxCounterSalesDetailsUnits> unitDetails = tranxCSDetailsUnitsRepository.findByCounterSalesIdAndStatus(counterSales.getId(), true);
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
                    mObject.addProperty("details_id", mUnit.getId());
                    mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                    mObject.addProperty("unitId", mUnit.getUnits().getId());
                    mObject.addProperty("unit_name", mUnit.getUnits().getUnitName());
                    mObject.addProperty("pack_name", mUnit.getProduct().getPackingMaster().getPackName());

                    mObject.addProperty("hsn", mUnit.getProduct().getProductHsn() != null ? mUnit.getProduct().getProductHsn().getHsnNumber() : "");
                    mObject.addProperty("mfg", mUnit.getProduct().getBrand() != null ? mUnit.getProduct().getBrand().getBrandName() : "");

                    mObject.addProperty("disc_per", mUnit.getDiscountAmount() != null ? mUnit.getDiscountAmount() : 0.00);
                    mObject.addProperty("tax_per", "");
                    mObject.addProperty("final_amt", mUnit.getNetAmount());

                    mObject.addProperty("b_no", mUnit.getProductBatchNo() != null ? mUnit.getProductBatchNo().getBatchNo() : "");
                    mObject.addProperty("mfg_date", mUnit.getProductBatchNo() != null ? (mUnit.getProductBatchNo().getManufacturingDate() != null ? mUnit.getProductBatchNo().getManufacturingDate().toString() : "") : "");
                    mObject.addProperty("exp_date", mUnit.getProductBatchNo() != null ? (mUnit.getProductBatchNo().getExpiryDate() != null ? mUnit.getProductBatchNo().getExpiryDate().toString() : "") : "");
                    mObject.addProperty("mrp", mUnit.getProductBatchNo() != null ? mUnit.getProductBatchNo().getMrp() : 0.00);

                    productDetails.add(mObject);
                });

                finalResult.add("product_details", productDetails);
                finalResult.add("supplier_data", companyObject);
                finalResult.add("customer_data", debtorsObject);
                finalResult.add("invoice_data", invoiceObject);

            } else if (compInvoice != null) {
                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(compInvoice.getSundryDebtorsId(), true);
                debtorsObject.addProperty("supplier_name", sundryDebtors.getLedgerName());
                debtorsObject.addProperty("supplier_address", sundryDebtors.getAddress());
                debtorsObject.addProperty("supplier_state", String.valueOf(sundryDebtors.getState().getName()));
                debtorsObject.addProperty("supplier_gstin", sundryDebtors.getGstin());
                debtorsObject.addProperty("supplier_phone", sundryDebtors.getMobile());
                JsonObject invoiceObject = new JsonObject();
                /* Sales Composition Invoice Data */
                invoiceObject.addProperty("id", compInvoice.getId());
                invoiceObject.addProperty("invoice_dt", compInvoice.getBillDate().toString());
                invoiceObject.addProperty("invoice_no", compInvoice.getSalesInvoiceNo());
                Outlet outlet = outletRepository.findByIdAndStatus(compInvoice.getOutletId(), true);
                invoiceObject.addProperty("state_code", outlet.getStateCode());
                invoiceObject.addProperty("state_name", outlet.getState().getName());
                invoiceObject.addProperty("taxable_amt", compInvoice.getTaxableAmount() != null ? numFormat.numFormat(compInvoice.getTaxableAmount()) : 0.00);
                invoiceObject.addProperty("tax_amount", compInvoice.getTotaligst() != null ? numFormat.numFormat(compInvoice.getTotaligst()) : 0.00);
                invoiceObject.addProperty("total_cgst", compInvoice.getTotalcgst() != null ? numFormat.numFormat(compInvoice.getTotalcgst()) : 0.00);
                invoiceObject.addProperty("total_sgst", compInvoice.getTotalsgst() != null ? numFormat.numFormat(compInvoice.getTotalsgst()) : 0.00);
                invoiceObject.addProperty("net_amount", compInvoice.getTotalBaseAmount() != null ? numFormat.numFormat(compInvoice.getTotalBaseAmount()) : 0.00);
                invoiceObject.addProperty("total_discount", compInvoice.getTotalSalesDiscountAmt() != null ? numFormat.numFormat(compInvoice.getTotalSalesDiscountAmt()) : 0.00);
                invoiceObject.addProperty("total_amount", numFormat.numFormat(compInvoice.getTotalAmount()));
                invoiceObject.addProperty("advanced_amount", numFormat.numFormat(compInvoice.getAdvancedAmount() != null ? compInvoice.getAdvancedAmount() : 0.0));
                invoiceObject.addProperty("payment_mode", compInvoice.getPaymentMode());
                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();
                /* getting Units of Sales Quotations*/
                List<TranxSalesCompInvoiceDetailsUnits> unitDetails = tranxSalescompInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(compInvoice.getId(), true);
                JsonArray productDetails = new JsonArray();
                unitDetails.forEach(mUnit -> {
                    JsonObject mObject = new JsonObject();
                    Product mProduct = productRepository.findByIdAndStatus(mUnit.getProductId(), true);
                    mObject.addProperty("product_id", mUnit.getProductId());
                    mObject.addProperty("product_name", mProduct.getProductName());
                    mObject.addProperty("details_id", mUnit.getId());
                    mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                    mObject.addProperty("qty", mUnit.getQty());
                    mObject.addProperty("rate", mUnit.getRate());
                    mObject.addProperty("base_amt", mUnit.getBaseAmt());
                    mObject.addProperty("hsn", mProduct.getProductHsn() != null ? mProduct.getProductHsn().getHsnNumber() : "");
                    mObject.addProperty("mfg", mProduct.getBrand() != null ? mProduct.getBrand().getBrandName() : "");
                    mObject.addProperty("disc_per", mUnit.getDiscountPer() != null ? mUnit.getDiscountPer() : 0.00);
                    mObject.addProperty("tax_per", mUnit.getIgst() != null ? mUnit.getIgst() : 0.00);
                    mObject.addProperty("final_amt", mUnit.getFinalAmount());
                    mObject.addProperty("Gst", mUnit.getIgst());
                    mObject.addProperty("Cgst", mUnit.getSgst());
                    mObject.addProperty("Sgst", mUnit.getCgst());
                    if (mProduct.getPackingMaster() != null) {
                        mObject.addProperty("packageId", mProduct.getPackingMaster().getId());
                        mObject.addProperty("pack_name", mProduct.getPackingMaster().getPackName());
                    } else {
                        mObject.addProperty("packageId", "");
                        mObject.addProperty("pack_name", "");
                    }
                    ProductBatchNo mBatch = productBatchNoRepository.findByIdAndStatus(mUnit.getProductBatchNoId(), true);
                    if (mUnit.getProductBatchNoId() != null) {
                        mObject.addProperty("b_details_id", mUnit.getProductBatchNoId());
                        mObject.addProperty("b_no", mBatch.getBatchNo());
                        mObject.addProperty("is_batch", true);

                    } else {
                        mObject.addProperty("b_details_id", "");
                        mObject.addProperty("b_no", "");
                        mObject.addProperty("is_batch", false);
                    }

                    mObject.addProperty("b_no", mBatch != null ? mBatch.getBatchNo() : "");
                    mObject.addProperty("mfg_date", mBatch != null ? (mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "") : "");
                    mObject.addProperty("exp_date", mBatch != null ? (mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "") : "");
                    mObject.addProperty("mrp", mBatch != null ? mBatch.getMrp() : 0.00);

                    mObject.addProperty("details_id", mUnit.getId());
                    mObject.addProperty("unit_conv", mUnit.getUnitConversions());
                    Units units = unitsRepository.findByIdAndStatus(mUnit.getUnitsId(), true);
                    mObject.addProperty("unitId", units.getId());
                    mObject.addProperty("unit_name", units.getUnitName());
                    productDetails.add(mObject);
                });
                finalResult.add("product_details", productDetails);
                finalResult.add("supplier_data", companyObject);
                finalResult.add("customer_data", debtorsObject);
                finalResult.add("invoice_data", invoiceObject);
            }

            /* End of Sales Invoice Details */
            /* End of Purchase Additional Charges */
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());

        } catch (DataIntegrityViolationException e) {
            salesInvoiceLogger.error("Error in getInvoiceBillPrint :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesInvoiceLogger.error("Error in getInvoiceBillPrint :->" + e1);
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject AllListCounterSale(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesInvoice> saleInvoice = new ArrayList<>();
        if (users.getBranch() != null) {
            saleInvoice = salesTransactionRepository.findByOutletIdAndBranchIdAndIsCounterSaleAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true, true);
        } else {
            saleInvoice = salesTransactionRepository.findByOutletIdAndIsCounterSaleAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true, true);
        }
        for (TranxSalesInvoice invoices : saleInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getSalesInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(invoices.getBillDate()).toString());
            response.addProperty("sale_serial_number", invoices.getSalesSerialNumber());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_debtor_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtor_id", invoices.getSundryDebtors().getId());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }


    public JsonObject CounterSaleLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        Long branchId = null;
        if (users.getBranch() != null) {
            branchId = users.getBranch().getId();
        }
        if (branchId != null) {
            count = counterSaleRepository.findLastRecordWithBranch(users.getOutlet().getId(), branchId, true);
        } else {
            count = counterSaleRepository.findLastRecord(users.getOutlet().getId(), true);
        }
        String serailNo = String.format("%05d", count + 1);
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String siCode = "CNTS" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("serialNo", siCode);
        return result;
    }


    public JsonObject getCompProductEditByIdByFPU(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesCompInvoice invoiceTranx = salesCompTransactionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        productIds = tranxSalesCompInvoiceDetailsUnitRepository.findByTranxPurId(invoiceTranx.getId(), true);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getProductEditByIdByFPU(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesInvoice invoiceTranx = salesTransactionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        productIds = tranxSalesInvoiceDetailsUnitRepository.findByTranxPurId(invoiceTranx.getId(), true);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getSalesInvoiceByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();

        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxSalesInvoice salesInvoice = salesTransactionRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            finalResult.addProperty("tcs_mode", salesInvoice.getTcsMode());
            if (salesInvoice.getTcsMode() != null && salesInvoice.getTcsMode().equalsIgnoreCase("tcs")) {
                finalResult.addProperty("tcs_per", salesInvoice.getTcs());
                finalResult.addProperty("tcs_amt", salesInvoice.getTcsAmt());
            } else if (salesInvoice.getTcsMode() != null && salesInvoice.getTcsMode().equalsIgnoreCase("tds")) {
                finalResult.addProperty("tcs_per", salesInvoice.getTdsPer());
                finalResult.addProperty("tcs_amt", salesInvoice.getTdsAmt());
            } else {
                finalResult.addProperty("tcs_amt", 0.0);
                finalResult.addProperty("tcs_per", 0.0);
            }
            finalResult.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", salesInvoice.getSalesDiscountLedger() != null ? salesInvoice.getSalesDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", salesInvoice.getSalesDiscountAmount() != null ? salesInvoice.getSalesDiscountAmount() : 0.0);
            finalResult.addProperty("discountInPer", salesInvoice.getSalesDiscountPer() != null ? salesInvoice.getSalesDiscountPer() : 0.0);
            finalResult.addProperty("totalSalesDiscountAmt", salesInvoice.getTotalSalesDiscountAmt() != null ? salesInvoice.getTotalSalesDiscountAmt() : 0.0);
            finalResult.addProperty("totalQty", salesInvoice.getTotalqty());
            finalResult.addProperty("totalFreeQty", salesInvoice.getFreeQty());
            finalResult.addProperty("grossTotal", salesInvoice.getGrossAmount() != null ? salesInvoice.getGrossAmount() : 0.0);
            finalResult.addProperty("totalTax", salesInvoice.getTotalTax() != null ? salesInvoice.getTotalTax() : 0.0);
            finalResult.addProperty("additionLedger1", salesInvoice.getAdditionLedger1() != null ? salesInvoice.getAdditionLedger1().getId() : 0);
            finalResult.addProperty("additionLedgerAmt1", salesInvoice.getAdditionLedgerAmt1() != null ? salesInvoice.getAdditionLedgerAmt1() : 0);
            finalResult.addProperty("additionLedger2", salesInvoice.getAdditionLedger2() != null ? salesInvoice.getAdditionLedger2().getId() : 0);
            finalResult.addProperty("additionLedgerAmt2", salesInvoice.getAdditionLedgerAmt2() != null ? salesInvoice.getAdditionLedgerAmt2() : 0);
            finalResult.addProperty("additionLedger3", salesInvoice.getAdditionLedger3() != null ? salesInvoice.getAdditionLedger3().getId() : 0);
            finalResult.addProperty("additionLedgerAmt3", salesInvoice.getAdditionLedgerAmt3() != null ? salesInvoice.getAdditionLedgerAmt3() : 0);
            finalResult.addProperty("totalamt", salesInvoice.getTotalAmount() != null ? salesInvoice.getTotalAmount() : 0);

            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", salesInvoice.getId());
            result.addProperty("invoice_id", salesInvoice.getId());
            result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
            result.addProperty("transaction_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
            result.addProperty("invoice_no", salesInvoice.getSalesInvoiceNo());
            result.addProperty("tranx_unique_code", salesInvoice.getTranxCode());
            result.addProperty("sales_sr_no", salesInvoice.getSalesSerialNumber());
            result.addProperty("sales_account_ledger_id", salesInvoice.getSalesAccountLedger().getId());
            result.addProperty("supplierId", salesInvoice.getSundryDebtors().getId());
            result.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            result.addProperty("total_cgst", salesInvoice.getTotalcgst());
            result.addProperty("total_sgst", salesInvoice.getTotalsgst());
            result.addProperty("total_igst", salesInvoice.getTotaligst());
            result.addProperty("total_qty", salesInvoice.getTotalqty());
            result.addProperty("taxable_amount", salesInvoice.getTaxableAmount());
            result.addProperty("tcs", salesInvoice.getTcs());
            result.addProperty("status", salesInvoice.getStatus());
            result.addProperty("financial_year", salesInvoice.getFinancialYear());
            result.addProperty("debtor_id", salesInvoice.getSundryDebtors().getId());
            result.addProperty("debtor_name", salesInvoice.getSundryDebtors().getLedgerName());
            result.addProperty("additional_charges_total", salesInvoice.getAdditionalChargesTotal() != null ? salesInvoice.getAdditionalChargesTotal() : 0.0);
            result.addProperty("gstNo", salesInvoice.getGstNumber());
            result.addProperty("paymentMode", salesInvoice.getPaymentMode());
            result.addProperty("salesmanId", salesInvoice.getSalesmanUser() != null ? salesInvoice.getSalesmanUser().toString() : "");
            result.addProperty("p_totalAmount", salesInvoice.getPaymentAmount() != null ? salesInvoice.getPaymentAmount() : 0);
            result.addProperty("isRoundOffCheck", salesInvoice.getIsRoundOff());
            result.addProperty("roundoff", salesInvoice.getRoundOff());
            result.addProperty("cashAmt", salesInvoice.getCash() != null ? salesInvoice.getCash() : 0.0);
            result.addProperty("ledgerStateCode", salesInvoice.getSundryDebtors().getStateCode());


            double pendingAmt = 0.0;
            if (salesInvoice.getPaymentAmount() != null)
                pendingAmt = salesInvoice.getTotalAmount() - salesInvoice.getPaymentAmount();
            if (salesInvoice.getPaymentAmount() != null && pendingAmt > 0) {
                result.addProperty("p_pendingAmount", pendingAmt);
            } else {
                result.addProperty("p_pendingAmount", 0);
            }
            double returnAmt = 0.0;
            if (salesInvoice.getPaymentAmount() != null && salesInvoice.getPaymentAmount() > salesInvoice.getTotalAmount()) {
                returnAmt = salesInvoice.getPaymentAmount() - salesInvoice.getTotalAmount();
                result.addProperty("p_returnAmount", returnAmt);
            } else {
                result.addProperty("p_returnAmount", 0);
            }
            /* End of Sales invoice Data */
            /* Sales invoice Details */
            JsonArray row = new JsonArray();
            List<TranxSalesInvoiceDetailsUnits> unitsArray = tranxSalesInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(salesInvoice.getId(), true);
            for (TranxSalesInvoiceDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelAId() != null ? mUnits.getLevelAId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelBId() != null ? mUnits.getLevelBId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelCId() != null ? mUnits.getLevelCId().toString() : "");
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
                        if (DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
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
                    unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA());
                    unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB());
                    unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC());
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

            /* Purchase Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            additionalCharges = salesAdditionalChargesRepository.findBySalesTransactionIdAndStatus(salesInvoice.getId(), true);
            if (additionalCharges.size() > 0) {
                for (TranxSalesInvoiceAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledgerId", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    json_charges.addProperty("percent", mAdditionalCharges.getPercent() != null ? mAdditionalCharges.getPercent() : 0.0);
                    jsonAdditionalList.add(json_charges);
                }
            }
            /* End of Sales Quotations Details */
            /**** Tranx Sales Payment Type *****/
            JsonArray paymentType = new JsonArray();
            List<TranxSalesPaymentType> paymentArray = transSalesPaymentTypeRepository.findPaymentModes(salesInvoice.getId(), true);
            if (paymentArray != null && paymentArray.size() > 0) {
                for (TranxSalesPaymentType mPayment : paymentArray) {
                    JsonObject paymentJsonObjects = new JsonObject();
                    LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(mPayment.getType()), true);
                    paymentJsonObjects.addProperty("bank_name", mLedger.getLedgerName());
                    paymentJsonObjects.addProperty("bank_Id", mLedger.getId());
                    List<TranxSalesPaymentType> salesTypes = transSalesPaymentTypeRepository.findByTranxSalesInvoiceIdAndStatusAndType(salesInvoice.getId(), true, mPayment.getType());
                    JsonArray mArray = new JsonArray();
                    for (TranxSalesPaymentType mPaymentTypes : salesTypes) {
                        JsonObject mObject = new JsonObject();
                        mObject.addProperty("details_id", mPaymentTypes.getId());
                        mObject.addProperty("modeId", mPaymentTypes.getPaymentMasterId());
                        mObject.addProperty("label", mPaymentTypes.getPaymentMode());
                        mObject.addProperty("amount", mPaymentTypes.getPaymentAmount());
                        mObject.addProperty("refId", mPaymentTypes.getReferenceId() != null ? mPaymentTypes.getReferenceId() : "");
                        mObject.addProperty("custBank", mPaymentTypes.getCustomerBank() != null ? mPaymentTypes.getCustomerBank() : "");
                        mArray.add(mObject);
                    }
                    paymentJsonObjects.add("payment_modes", mArray);
                    paymentType.add(paymentJsonObjects);
                }
            }
            finalResult.add("row", row);
            finalResult.add("invoice_data", result);
            finalResult.add("payment_type", paymentType);
            finalResult.add("additionalCharges", jsonAdditionalList);
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public Object validateSalesInvoices(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesInvoice salesInvoice = null;
        if (users.getBranch() != null) {
            salesInvoice = salesTransactionRepository.findByOutletIdAndBranchIdAndSalesInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesInvoiceNo"));
        } else {
            salesInvoice = salesTransactionRepository.findByOutletIdAndSalesInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), request.getParameter("salesInvoiceNo"));
        }
        if (salesInvoice != null) {
            // System.out.println("Already Ledger created with this name or code");
            responseMessage.setMessage("Duplicate sales invoice number exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    private void deleteIntoLedgerTranxDetails(TranxSalesInvoice salesTranx) {
        /* start of ledger trasaction details */
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLS");
//        generateTransactions.insertIntoTranxsDetails(mPurchaseTranx,tranxType);
        try {
            deleteIntoTranxDetailSD(salesTranx, tranxType);// for Sundry Debtors : CR
            delPostingTranxDetailSA(salesTranx, tranxType, "DR", "Delete"); // for Sales Accounts : DR
            //  insertIntoTranxDetailsSalesDiscount(salesTranx, tranxType, "CR", "Delete"); // for Sales Discounts : CR
            deleteIntoTranxDetailRO(salesTranx, tranxType); // for Round Off : cr or dr
            insertDB(salesTranx, "AC", tranxType, "DR", "Delete"); // for Additional Charges : DR
            insertDB(salesTranx, "DT", tranxType, "DR", "Delete"); // for Duties and Taxes : DR
            /* end of ledger transaction details */
        } catch (Exception e) {
            salesInvoiceLogger.error("Exception->delete Ledger TranxDetails(method) : " + e.getMessage());
            System.out.println("Posting Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteIntoTranxDetailRO(TranxSalesInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        /**** New Postings Logic *****/
        if (mSalesTranx.getRoundOff() >= 0) {
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "DR", true, "Sales Invoice", "Delete");
        } else if (mSalesTranx.getRoundOff() < 0) {
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Delete");
        }
    }

    private void deleteIntoTranxDetailSD(TranxSalesInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mSalesTranx.getTotalAmount(), mSalesTranx.getSundryDebtors(), tranxType, mSalesTranx.getSundryDebtors().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getBillDate(), mSalesTranx.getId(), mSalesTranx.getSalesInvoiceNo(), "CR", true, "Sales Invoice", "Delete");

    }

    private void deleteIntoLedgerTranxDetailsCreditNote(TranxSalesReturnInvoice mSalesTranx) {
        /* start of ledger trasaction details */
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSRT");
        try {
            insertIntoTranxDetailSD(mSalesTranx, tranxType);// for Sundry Debtor : CR
            insertIntoTranxDetailSA(mSalesTranx, tranxType); // for Sales Accounts : DR
            insertIntoTranxDetailsSalesDiscount(mSalesTranx, tranxType); // for Sales Discounts : DR
            deleteIntoTranxDetailROCreditNote(mSalesTranx, tranxType); // for Round Off : cr or dr
            insertDB(mSalesTranx, "AC", tranxType); // for Additional Charges : CR
            insertDB(mSalesTranx, "DT", tranxType); // for Duties and Taxes : CR
            /* end of ledger transaction details */

        } catch (Exception e) {
            salesInvoiceLogger.error("Exception->deleteIntoLedgerTranxDetailsCreditNote(method) : " + e.getMessage());
            System.out.println("Posting Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertIntoTranxDetailSD(TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        try {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getTotalAmount(), mSalesTranx.getSundryDebtors(), tranxType, mSalesTranx.getSundryDebtors().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Delete");
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Exception in insertIntoTranxDetailSD(TranxSalesReturnInvoice) :" + e.getMessage());
            System.out.println("Store Procedure Error " + e.getMessage());
        }
    }/* End of Posting into Sundry Debtors */

    private void insertIntoTranxDetailSA(TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        try {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getTotalBaseAmount(), mSalesTranx.getSalesAccountLedger(), tranxType, mSalesTranx.getSalesAccountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "CR", true, tranxType.getTransactionName(), "Delete");
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Exception in insertIntoTranxDetailSA(TranxSalesReturnInvoice):" + e.getMessage());
        }
    }/* End of Posting into Sales Accounts */

    private void insertIntoTranxDetailsSalesDiscount(TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        try {
            if (mSalesTranx.getSalesDiscountLedger() != null) {
                //  ledgerTransactionDetailsRepository.insertIntoLegerTranxDetailsPosting(mSalesTranx.getSalesDiscountLedger().getPrinciples().getFoundations().getId(), mSalesTranx.getSalesDiscountLedger().getPrinciples().getId(), mSalesTranx.getSalesDiscountLedger().getPrincipleGroups() != null ? mSalesTranx.getSalesDiscountLedger().getPrincipleGroups().getId() : null, mSalesTranx.getAssociateGroups() != null ? mSalesTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, mSalesTranx.getBranch() != null ? mSalesTranx.getBranch().getId() : null, mSalesTranx.getOutlet().getId(), "pending", 0.0, mSalesTranx.getTotalSalesDiscountAmt(), mSalesTranx.getTransactionDate(), null, mSalesTranx.getId(), tranxType.getTransactionName(), mSalesTranx.getSalesDiscountLedger().getUnderPrefix(), mSalesTranx.getFinancialYear(), mSalesTranx.getCreatedBy(), mSalesTranx.getSalesDiscountLedger().getId(), mSalesTranx.getSalesReturnNo());
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(mSalesTranx.getTotalSalesDiscountAmt(), mSalesTranx.getSalesDiscountLedger(), tranxType, mSalesTranx.getSalesDiscountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Delete");
            }
        } catch (Exception e) {
            salesInvoiceLogger.error("Exception in insertIntoTranxDetailsSalesDiscount(TranxSalesReturnInvoice):" + e.getMessage());
            System.out.println("Posting Discount Exception:" + e.getMessage());
            e.printStackTrace();

        }
    }/* End of Posting into Sales Discount */

    private void deleteIntoTranxDetailROCreditNote(TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        if (mSalesTranx.getRoundOff() >= 0) {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "CR", true, tranxType.getTransactionName(), "Delete");
        } else if (mSalesTranx.getRoundOff() < 0) {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Delete");
        }
    }/* End Posting into Sales Round off */

    public void insertDB(TranxSalesReturnInvoice mSalesTranx, String ledgerName, TransactionTypeMaster tranxType) {
        try {
            /* Sale Duties Taxes */
            if (ledgerName.equalsIgnoreCase("DT")) {
                List<TranxSalesReturnInvoiceDutiesTaxes> list = tranxSalesReturnTaxesRepository.findByTranxSalesReturnInvoiceIdAndStatus(mSalesTranx.getId(), true);
                for (TranxSalesReturnInvoiceDutiesTaxes mDuties : list) {
                    insertFromDutiesTaxes(mDuties, mSalesTranx, tranxType);
                }
            } else if (ledgerName.equalsIgnoreCase("AC")) {
                /* Sale Additional Charges */
                List<TranxSalesReturnInvoiceAddCharges> list = new ArrayList<>();
                list = tranxSalesReturnAddiChargesRepository.findByTranxSalesReturnInvoiceIdAndStatus(mSalesTranx.getId(), true);
                if (list.size() > 0) {
                    for (TranxSalesReturnInvoiceAddCharges mAdditinoalCharges : list) {
                        insertFromAdditionalCharges(mAdditinoalCharges, mSalesTranx, tranxType);
                    }
                }
            }
        } catch (DataIntegrityViolationException e1) {
            salesInvoiceLogger.error("Exception in insertDB:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            salesInvoiceLogger.error("Exception in insertDB:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertFromDutiesTaxes(TranxSalesReturnInvoiceDutiesTaxes mDuties, TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mDuties.getAmount(), mDuties.getDutiesTaxes(), tranxType, mDuties.getDutiesTaxes().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "CR", true, tranxType.getTransactionName(), "Delete");
    }

    private void insertFromAdditionalCharges(TranxSalesReturnInvoiceAddCharges mAdditinoalCharges, TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mAdditinoalCharges.getAmount(), mAdditinoalCharges.getAdditionalCharges(), tranxType, mAdditinoalCharges.getAdditionalCharges().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "CR", true, tranxType.getTransactionName(), "Delete");
    }

    public JsonObject getProductStocksFilter(HttpServletRequest request) {
        JsonObject object = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long productId = Long.parseLong(request.getParameter("product_id"));
        Map<String, String[]> paramMap = request.getParameterMap();
        Long batchId = null;
        if (paramMap.containsKey("batch_id")) batchId = Long.parseLong(request.getParameter("batch_id"));
        Long level_a_id = null;
        if (paramMap.containsKey("level_a_id")) level_a_id = Long.parseLong(request.getParameter("level_a_id"));
        Long level_b_id = null;
        if (paramMap.containsKey("level_b_id")) level_b_id = Long.parseLong(request.getParameter("level_b_id"));
        Long level_c_id = null;
        if (paramMap.containsKey("level_c_id")) level_c_id = Long.parseLong(request.getParameter("level_c_id"));
        Long unitsId = null;
        if (paramMap.containsKey("unit_id")) unitsId = Long.parseLong(request.getParameter("unit_id"));
        Double qty = Double.parseDouble(request.getParameter("qty"));
        Product mProduct = productRepository.findByIdAndStatus(productId, true);
        Long outletId = users.getOutlet().getId();
        ProductUnitPacking unitPack = null;
        Long branchId = null;
        Long brandId = null;
        Long groupId = null;
        Long subgroupId = null;
        Long categoryId = null;
        Long subcategoryId = null;
        Long packagingId = null;
        Long fiscalId = null;
        LocalDate currentDate = LocalDate.now();
        /*   fiscal year mapping  */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        if (users.getBranch() != null) {
            branchId = users.getBranch().getId();
        }
        if (fiscalYear != null) fiscalId = fiscalYear.getId();
        Double closing = 0.0;
        /*Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId, outletId, productId, brandId,
                groupId, categoryId, subcategoryId, packagingId, unitsId, batchId, fiscalId);*/
        if (mProduct.getBrand() != null) brandId = mProduct.getBrand().getId();
        if (mProduct.getGroup() != null) groupId = mProduct.getGroup().getId();
        if (mProduct.getSubgroup() != null) subgroupId = mProduct.getSubgroup().getId();
        if (mProduct.getCategory() != null) categoryId = mProduct.getCategory().getId();
        if (mProduct.getSubcategory() != null) subcategoryId = mProduct.getSubcategory().getId();
        if (mProduct.getPackingMaster() != null) packagingId = mProduct.getPackingMaster().getId();

        unitPack = productUnitRepository.findNegativeStatus(mProduct.getId(), brandId, groupId, categoryId, subcategoryId, packagingId, unitsId, true, subgroupId);
        if (unitPack != null) {
            if (closing > 0) {
                object.addProperty("closingStocks", closing);
                object.addProperty("message", "success");
                object.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                if (unitPack.getIsNegativeStocks()) {
                    object.addProperty("closingStocks", closing);
                    object.addProperty("message", "success");
                    object.addProperty("responseStatus", HttpStatus.OK.value());
                } else {
                    Double opening = productOpeningStocksRepository.findSumProductOpeningStocksBatchwise(mProduct.getId(), outletId, branchId, fiscalId, batchId);
                    closing = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(), mProduct.getId(), level_a_id, level_b_id, level_c_id, unitsId, batchId, fiscalId);
                    if (qty > (opening + closing)) {
                        object.addProperty("closingStocks", opening + closing);
                        object.addProperty("message", "Out of stocks");
                        object.addProperty("responseStatus", HttpStatus.OK.value());
                    }
                }
            }
        }
        return object;
    }

    public JsonObject getInvoiceSupplierListByProductId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long productId = Long.parseLong(request.getParameter("productId"));
        List<TranxSalesInvoiceDetailsUnits> tranxSalesInvoiceDetailsUnits = tranxSalesInvoiceDetailsUnitRepository.findByProductIdAndStatusOrderByIdDesc(productId, true);
        JsonArray result = new JsonArray();
        for (TranxSalesInvoiceDetailsUnits obj : tranxSalesInvoiceDetailsUnits) {
            JsonObject response = new JsonObject();
            response.addProperty("supplier_name", obj.getSalesInvoice().getSundryDebtors().getLedgerName());
            response.addProperty("invoice_no", obj.getSalesInvoice().getSalesInvoiceNo());
            response.addProperty("batch", obj.getProductBatchNo() != null ? obj.getProductBatchNo().getBatchNo() : "");
            response.addProperty("mrp", obj.getProductBatchNo() != null ? obj.getProductBatchNo().getMrp().toString() : "");
            response.addProperty("quantity", obj.getQty());
            response.addProperty("rate", obj.getRate());
            response.addProperty("cost", obj.getProductBatchNo() != null ? obj.getProductBatchNo().getCosting().toString() : "");
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

    public ResponseMessage createCounterSales(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CNTS");
        Branch branch = null;
        Long branchId = null;
        TranxCounterSales mSalesTranx = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        if (users.getBranch() != null) {
            branch = users.getBranch();
            branchId = users.getBranch().getId();
        }
        Outlet outlet = users.getOutlet();
        TranxCounterSales invoiceTranx = new TranxCounterSales();
        invoiceTranx.setBranch(branch);
        invoiceTranx.setOutlet(outlet);
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("bill_dt"));
        invoiceTranx.setTransactionDate(strDt);
        invoiceTranx.setCounterSalesDate(date);
        /* fiscal year mapping*/
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping*/
        Long count = 0L;
        if (branchId != null) {
            count = counterSaleRepository.findLastRecordWithBranch(users.getOutlet().getId(), branchId, true);
        } else {
            count = counterSaleRepository.findLastRecord(users.getOutlet().getId(), true);
        }
        String serailNo = String.format("%05d", count + 1);
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String siCode = "CNTS" + currentMonth + serailNo;
        invoiceTranx.setCounterSaleSrNo(count + 1);
        invoiceTranx.setCounterSaleNo(siCode);
        invoiceTranx.setNarrations(request.getParameter("narration"));
        invoiceTranx.setTotalBaseAmt(Double.parseDouble(request.getParameter("total_base_amt")));
        invoiceTranx.setTotalBill(Double.parseDouble(request.getParameter("bill_amount")));
        invoiceTranx.setTaxableAmt(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setTotalqty(Double.parseDouble(request.getParameter("totalqty")));
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setTotalDiscount(Double.valueOf(request.getParameter("total_invoice_dis_amt")));
        invoiceTranx.setCreatedBy(users.getId());
        if (paramMap.containsKey("mobile_number"))
            invoiceTranx.setMobileNumber(Long.parseLong(request.getParameter("mobile_number")));
        if (paramMap.containsKey("doctorId"))
            invoiceTranx.setDoctorId(Long.parseLong(request.getParameter("doctorId")));
        if (paramMap.containsKey("patientName")) invoiceTranx.setCustomerName(request.getParameter("patientName"));
        if (paramMap.containsKey("paymentMode")) invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        if (paramMap.containsKey("advancedAmount"))
            invoiceTranx.setAdvancedAmount(Double.parseDouble(request.getParameter("advancedAmount")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setOperations("inserted");
        invoiceTranx.setIsBillConverted(false);
        // invoiceTranx.setTotalDiscount(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        invoiceTranx.setTransactionStatus(1L);
        try {
            mSalesTranx = counterSaleRepository.save(invoiceTranx);
            if (mSalesTranx != null) {
                /* Save into Sales Invoice Details*/
                String jsonStr = request.getParameter("row");
                JsonArray invoiceDetails = new JsonParser().parse(jsonStr).getAsJsonArray();
                saveIntoCounterSalesDetails(invoiceDetails, mSalesTranx, branch, outlet, users.getId(), tranxType);
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
            // throw new Exception(e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
            // throw new Exception(e1.getMessage());
        }
        if (mSalesTranx != null) {
            responseMessage.setResponse(mSalesTranx.getCounterSaleNo());
            responseMessage.setMessage("Counter sales created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Error in Counter Sales");
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return responseMessage;
    }

    public void saveIntoCounterSalesDetails(JsonArray invoiceDetails, TranxCounterSales mSalesTranx, Branch branch, Outlet outlet, Long userId, TransactionTypeMaster tranxType) {
        String refType = "";
        boolean flag_status = false;
        Long referenceId = 0L;
        for (int i = 0; i < invoiceDetails.size(); i++) {
            JsonObject object = invoiceDetails.get(i).getAsJsonObject();
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            /* inserting into TranxSalesInvoiceDetailsUnits */
            String batchNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
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
            TranxCounterSalesDetailsUnits invoiceUnits = new TranxCounterSalesDetailsUnits();
            invoiceUnits.setTransactionStatus(1L);
            invoiceUnits.setCounterSales(mSalesTranx);
            invoiceUnits.setProduct(mProduct);
            invoiceUnits.setUnits(units);
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            if (!object.get("free_qty").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setFreeQty(object.get("free_qty").getAsDouble());
            invoiceUnits.setRate(object.get("rate").getAsDouble());
            invoiceUnits.setStatus(true);
            if (levelA != null) invoiceUnits.setLevelA(levelA);
            if (levelB != null) invoiceUnits.setLevelB(levelB);
            if (levelC != null) invoiceUnits.setLevelC(levelC);
            if (object.has("base_amt")) invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv")) invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            invoiceUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            if (object.has("dis_per") && !object.get("dis_per").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            if (object.has("dis_per2") && !object.get("dis_per2").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            invoiceUnits.setRowDiscountamt(object.get("row_dis_amt").getAsDouble());
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

            //            invoiceUnits.setNetAmount(object.get("total_amt").getAsDouble());
            invoiceUnits.setNetAmount(object.get("final_amt").getAsDouble());
            /******* Insert into Product Batch No ****/
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                    Double qnty = object.get("qty").getAsDouble();
                    productBatchNo.setQnty(qnty.intValue());
                    productBatchNo.setSalesRate(object.get("b_rate").getAsDouble());
                    productBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                    if (!object.get("b_expiry").getAsString().equalsIgnoreCase(""))
                        productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                    if (!object.get("manufacturing_date").getAsString().equalsIgnoreCase(""))
                        productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                    productBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                    productBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                    productBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                    productBatchNo.setMinMargin(object.get("min_margin").getAsDouble());
                    productBatchNoRepository.save(productBatchNo);
                    batchNo = productBatchNo.getBatchNo();
                    invoiceUnits.setProductBatchNo(productBatchNo);
                }
                TranxCounterSalesDetailsUnits tranxSalesInvoiceDetailsUnits = tranxCSDetailsUnitsRepository.save(invoiceUnits);
            } catch (Exception e) {
                salesInvoiceLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            /******* End of insert into Product Batch No ****/
            try {
                if (mProduct.getIsInventory()) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("DR", mSalesTranx.getTransactionDate(), mSalesTranx.getId(), object.get("qty").getAsDouble(), branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), null);
                    /***** End of new architecture of Inventory Postings *****/
                }
            } catch (Exception e) {
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            } /* End of inserting into TranxSalesInvoiceDetailsUnits */
        }
    }


    public ResponseMessage updateCounterSales(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseMessage = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CNTS");
        TranxCounterSales mSalesTranx = null;
        TranxCounterSales invoiceTranx = counterSaleRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        invoiceTranx.setOperations("Updated");
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("bill_dt"));

        invoiceTranx.setTransactionDate(strDt);
        /* fiscal year mapping*/
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping*/
        if (paramMap.containsKey("mobile_number"))
            invoiceTranx.setMobileNumber(Long.parseLong(request.getParameter("mobile_number")));
        if (paramMap.containsKey("doctorId"))
            invoiceTranx.setDoctorId(Long.parseLong(request.getParameter("doctorId")));
        invoiceTranx.setTotalBaseAmt(Double.parseDouble(request.getParameter("total_base_amt")));
        invoiceTranx.setTotalBill(Double.parseDouble(request.getParameter("bill_amount")));
        invoiceTranx.setTaxableAmt(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setTotalqty(Double.parseDouble(request.getParameter("totalqty")));
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setTotalDiscount(Double.valueOf(request.getParameter("total_invoice_dis_amt")));
        if (paramMap.containsKey("paymentMode")) invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        if (paramMap.containsKey("advancedAmount"))
            invoiceTranx.setAdvancedAmount(Double.parseDouble(request.getParameter("advancedAmount")));
        invoiceTranx.setIsBillConverted(false);
        invoiceTranx.setUpdatedBy(users.getId());
        // invoiceTranx.setTotalDiscount(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        try {
            mSalesTranx = counterSaleRepository.save(invoiceTranx);
            if (mSalesTranx != null) {
                /* Save into Sales Invoice Details*/
                String jsonStr = request.getParameter("row");
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) rowsDeleted = request.getParameter("rowDelDetailsIds");
                JsonArray invoiceDetails = new JsonParser().parse(jsonStr).getAsJsonArray();
                saveIntoCounterSalesDetailsEdit(invoiceDetails, mSalesTranx, users.getBranch(), users.getOutlet(), users.getId(), tranxType, rowsDeleted);
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
            // throw new Exception(e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in saveIntoInvoice :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
            // throw new Exception(e1.getMessage());
        }
        if (mSalesTranx != null) {
            responseMessage.setResponse(mSalesTranx.getCounterSaleNo());
            responseMessage.setMessage("Counter sales created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Error in Counter Sales");
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return responseMessage;
    }

    private void saveIntoCounterSalesDetailsEdit(JsonArray invoiceDetails, TranxCounterSales mSalesTranx, Branch branch, Outlet outlet, Long id, TransactionTypeMaster tranxType, String rowsDeleted) {
        for (JsonElement mList : invoiceDetails) {
            JsonObject object = mList.getAsJsonObject();
            /* Purchase Invoice Unit Edit */
            Long details_id = object.get("details_id").getAsLong();
            TranxCounterSalesDetailsUnits invoiceUnits = new TranxCounterSalesDetailsUnits();
            if (details_id != 0) {
                invoiceUnits = tranxCSDetailsUnitsRepository.findByIdAndStatus(details_id, true);
            }
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            String batchNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;

            /* Sales Invoice Unit Edit */
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
            invoiceUnits.setCounterSales(mSalesTranx);
            invoiceUnits.setProduct(mProduct);
            invoiceUnits.setUnits(units);
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            if (!object.get("free_qty").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setFreeQty(object.get("free_qty").getAsDouble());
            invoiceUnits.setRate(object.get("rate").getAsDouble());
            invoiceUnits.setStatus(true);

            if (levelA != null) invoiceUnits.setLevelA(levelA);
            if (levelB != null) invoiceUnits.setLevelB(levelB);
            if (levelC != null) invoiceUnits.setLevelC(levelC);

            if (object.has("base_amt")) invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv")) invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            invoiceUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            if (object.has("dis_per") && !object.get("dis_per").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            if (object.has("dis_per2") && !object.get("dis_per2").getAsString().equalsIgnoreCase(""))
                invoiceUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            invoiceUnits.setRowDiscountamt(object.get("row_dis_amt").getAsDouble());
            invoiceUnits.setBaseAmt(object.get("total_amt").getAsDouble());
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
            invoiceUnits.setNetAmount(object.get("final_amt").getAsDouble());
            /******* Insert into Product Batch No ****/
            boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                    Double qnty = object.get("qty").getAsDouble();
                    productBatchNo.setQnty(qnty.intValue());
                    productBatchNo.setSalesRate(object.get("rate").getAsDouble());
                    productBatchNoRepository.save(productBatchNo);
                    batchNo = productBatchNo.getBatchNo();
                }
                invoiceUnits.setProductBatchNo(productBatchNo);
                TranxCounterSalesDetailsUnits tranxSalesInvoiceDetailsUnits = tranxCSDetailsUnitsRepository.save(invoiceUnits);
            } catch (Exception e) {
                salesInvoiceLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            /******* End of insert into Product Batch No ****/
            try {
                if (mProduct.getIsInventory()) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToEditInventoryPostings(mSalesTranx.getTransactionDate(), mSalesTranx.getId(), object.get("qty").getAsDouble(), branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear());
                    /***** End of new architecture of Inventory Postings *****/
                }
            } catch (Exception e) {
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            } /* End of inserting into TranxSalesInvoiceDetailsUnits */
        }

        /* if product is deleted from details table from front end, while user edit the purchase */
        JsonParser parser = new JsonParser();
        JsonElement salesDetailsJson;
        if (!rowsDeleted.equalsIgnoreCase("")) {
            salesDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = salesDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxCounterSalesDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    mDeletedInvoices = tranxCSDetailsUnitsRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                    if (mDeletedInvoices != null) {
                        mDeletedInvoices.setStatus(false);
                        try {
                            tranxCSDetailsUnitsRepository.save(mDeletedInvoices);
                            /***** inventory effects of deleted rows *****/
                            inventoryCommonPostings.callToInventoryPostings("CR", mDeletedInvoices.getCounterSales().getTransactionDate(), mDeletedInvoices.getCounterSales().getId(), mDeletedInvoices.getQty(), branch, outlet, mDeletedInvoices.getProduct(), tranxType, mDeletedInvoices.getLevelA(), mDeletedInvoices.getLevelB(), mDeletedInvoices.getLevelC(), mDeletedInvoices.getUnits(), mDeletedInvoices.getProductBatchNo(), mDeletedInvoices.getProductBatchNo().getBatchNo(), mDeletedInvoices.getCounterSales().getFiscalYear(), null);
                            /***** End of new architecture of Inventory Postings *****/
                        } catch (DataIntegrityViolationException de) {
                            salesInvoiceLogger.error("Error in saveInto Sales Invoice Details Edit" + de.getMessage());
                            de.printStackTrace();
                            System.out.println("Exception:" + de.getMessage());
                        } catch (Exception ex) {
                            salesInvoiceLogger.error("Error in saveInto Sales Invoice Details Edit" + ex.getMessage());
                            ex.printStackTrace();
                            System.out.println("Exception save Into Sales Invoice Details Edit:" + ex.getMessage());
                        }
                    }
                }
            }
        }

    }

    public JsonObject getCounterSalesByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalResult = new JsonObject();
        JsonArray row = new JsonArray();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxCounterSales salesInvoice = counterSaleRepository.findByIdAndStatus(id, true);
            row = getCounterSalesData(salesInvoice);
            finalResult.add("row", row);
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonArray getCounterSalesData(TranxCounterSales salesInvoice) {
        JsonObject finalResult = new JsonObject();
        JsonArray row = new JsonArray();
        finalResult.addProperty("id", salesInvoice.getId());
        finalResult.addProperty("narration", salesInvoice.getNarrations() != null ? salesInvoice.getNarrations() : "");
        finalResult.addProperty("branch", salesInvoice.getBranch() != null ? salesInvoice.getBranch().getBranchName() : "");
        finalResult.addProperty("outlet", salesInvoice.getOutlet() != null ? salesInvoice.getOutlet().getCompanyName() : "");
        finalResult.addProperty("counterSaleSrNo", salesInvoice.getCounterSaleSrNo());
        finalResult.addProperty("counterSaleNo", salesInvoice.getCounterSaleNo());
        finalResult.addProperty("billDate", salesInvoice.getTransactionDate().toString());
        finalResult.addProperty("customerName", salesInvoice.getCustomerName() != null ? salesInvoice.getCustomerName() : "");
        finalResult.addProperty("mobileNumber", salesInvoice.getMobileNumber() != null ? salesInvoice.getMobileNumber().toString() : "");
        finalResult.addProperty("totalSalesDiscountAmt", salesInvoice.getTotalDiscount());
        finalResult.addProperty("totalQty", salesInvoice.getTotalqty());
        finalResult.addProperty("totalFreeQty", salesInvoice.getFreeQty());
        finalResult.addProperty("totalBill", salesInvoice.getTotalBill());
        finalResult.addProperty("totalBaseAmt", salesInvoice.getTotalBaseAmt());
        finalResult.addProperty("roundoff", salesInvoice.getRoundoff());
        finalResult.addProperty("paymentMode", salesInvoice.getPaymentMode());
        List<TranxCounterSalesDetailsUnits> unitsArray = tranxCSDetailsUnitsRepository.findByCounterSalesIdAndStatus(salesInvoice.getId(), true);
        for (TranxCounterSalesDetailsUnits mUnits : unitsArray) {
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
            unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
            unitsJsonObjects.addProperty("dis_amt", mUnits.getDiscountAmount());
            unitsJsonObjects.addProperty("dis_per", mUnits.getDiscountPer());
            unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer());
            unitsJsonObjects.addProperty("total_amt", mUnits.getBaseAmt());
            unitsJsonObjects.addProperty("final_amt", mUnits.getNetAmount());
            unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
            unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty());
            unitsJsonObjects.addProperty("row_dis_amt", mUnits.getRowDiscountamt());
            if (mUnits.getProductBatchNo() != null) {
                if (mUnits.getProductBatchNo().getExpiryDate() != null) {
                    if (DateConvertUtil.convertDateToLocalDate(salesInvoice.getTransactionDate()).isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
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
                unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA());
                unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB());
                unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC());
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
            row.add(unitsJsonObjects);
        }
        /* End of Sales Quotations Details */
        return row;
    }

    public JsonObject getCSProductEditByIdByFPU(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String str = request.getParameter("cs_ids");
        JsonParser parser = new JsonParser();
        JsonElement jsonParser = parser.parse(str);
        JsonArray jsonArray = jsonParser.getAsJsonArray();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            TranxCounterSales invoiceTranx = counterSaleRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
            List<Object[]> productIds = new ArrayList<>();
            productIds = tranxCSDetailsUnitsRepository.findByTranxPurId(invoiceTranx.getId(), true);
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
                                                if (DateConvertUtil.convertDateToLocalDate(invoiceTranx.getTransactionDate()).isAfter(mBatch.getExpiryDate())) {
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

        //   productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getTransactionDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getSalesVerificationById(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Double crqty, drqty;
        JsonObject op = new JsonObject();
        Long levelBId = null;
        Long levelCId = null;
        Long levelAId = null;
        Long batchId = null;
        Boolean flag = false;
        Long id = Long.parseLong(request.getParameter("product_id"));
        Product product = productRepository.findByIdAndStatus(id, true);
        if (!request.getParameter("levelAId").equalsIgnoreCase(""))
            levelAId = Long.parseLong(request.getParameter("levelAId"));
        if (!request.getParameter("levelBId").equalsIgnoreCase(""))
            levelBId = Long.parseLong(request.getParameter("levelBId"));
        if (!request.getParameter("levelCId").equalsIgnoreCase(""))
            levelCId = Long.parseLong(request.getParameter("levelCId"));
        Long unitId = Long.parseLong(request.getParameter("unitId"));
        if (!request.getParameter("batchId").equalsIgnoreCase(""))
            batchId = Long.parseLong(request.getParameter("batchId"));
        Long qty = Long.parseLong(request.getParameter("qty"));
        LocalDate mDate = LocalDate.now();
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(mDate);
        crqty = inventoryDetailsPostingsRepository.findClosingWithoutBranchFilter(users.getOutlet().getId(), id, levelAId, levelBId, levelCId, unitId, batchId, fiscalYear.getId(), "CR");
        drqty = inventoryDetailsPostingsRepository.findClosingWithoutBranchFilter(users.getOutlet().getId(), id, levelAId, levelBId, levelCId, unitId, batchId, fiscalYear.getId(), "DR");
        Double closing = crqty - drqty;
        ProductUnitPacking productUnitPacking = productUnitRepository.findRate(product.getId(), levelAId, levelBId, levelCId, unitId, true);
        /*if (productUnitPacking != null) {
            flag = productUnitPacking.getIsNegativeStocks();
        }*/
        if (qty > closing) {
            op.addProperty("message", "Exceeding Sales Quantity" + ", " + "Available Quantity is: " + closing.intValue());
            op.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            op.addProperty("product_id", id);
            op.addProperty("is_negative", productUnitPacking.getIsNegativeStocks());

        } else {
            op.addProperty("message", "success");
            op.addProperty("responseStatus", HttpStatus.OK.value());
            op.addProperty("product_id", id);
            op.addProperty("is_negative", productUnitPacking.getIsNegativeStocks());
        }
        return op;
    }

    public JsonObject salesCounterDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxCounterSales salesTranx = counterSaleRepository.findByIdAndStatusAndIsBillConverted(Long.parseLong(request.getParameter("id")), true, false);


        TransactionTypeMaster invoiceTranx = tranxRepository.findByTransactionCodeIgnoreCase("SLS");
        TransactionTypeMaster returnTranx = tranxRepository.findByTransactionCodeIgnoreCase("SLSRT");
        try {
            if (salesTranx != null) {
                salesTranx.setStatus(false);
                salesTranx.setOperations("deletion");
                counterSaleRepository.save(salesTranx);
                jsonObject.addProperty("message", "counter sales deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
                List<TranxCounterSalesDetailsUnits> unitsList = new ArrayList<>();
                unitsList = tranxCSDetailsUnitsRepository.findByCounterSalesIdAndStatus(salesTranx.getId(), true);
                for (TranxCounterSalesDetailsUnits mUnitObjects : unitsList) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("CR", salesTranx.getTransactionDate(), salesTranx.getId(), mUnitObjects.getQty(), salesTranx.getBranch(), salesTranx.getOutlet(), mUnitObjects.getProduct(), invoiceTranx, null, null, null, mUnitObjects.getUnits(), mUnitObjects.getProductBatchNo(), mUnitObjects.getProductBatchNo() != null ? mUnitObjects.getProductBatchNo().getBatchNo() : null, salesTranx.getFiscalYear(), null);
                    /***** End of new architecture of Inventory Postings *****/
                }
            } else {
                jsonObject.addProperty("message", "Cant delete counter sales");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }
        } catch (Exception e) {
            salesInvoiceLogger.error("Error in salesDelete()->" + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject validateSalesInvoice(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long debtorsId = Long.parseLong(request.getParameter("debtors_id"));
        LocalDate currentDate = LocalDate.parse(request.getParameter("invoice_date"));
        LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(debtorsId, true);
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        TranxSalesInvoice tranxSalesInvoice = null;
        Long branchId = null;
        boolean creditDays = false;
        boolean creditBills = false;
        boolean creditBillValue = false;
        LocalDate compareDate = currentDate.minusDays(ledgerMaster.getCreditDays().longValue());
        /****** Checking for Credit Days Limit *****/
        if (users.getBranch() != null) branchId = users.getBranch().getId();
        if (branchId != null) {
            tranxSalesInvoice = salesTransactionRepository.validateCreditdaysWBr(users.getOutlet().getId(), users.getBranch().getId(), debtorsId, compareDate, fiscalYear);
        } else {
            tranxSalesInvoice = salesTransactionRepository.validateCreditdaysWtBr(users.getOutlet().getId(), debtorsId, compareDate, fiscalYear);
        }
        if (tranxSalesInvoice != null) {
            creditDays = true;
            jsonObject.addProperty("message", "Credit Days Limit Exceedes");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }
        /****** Checking for Credit Bill(Number of Bills) Limit *****/
        if (creditDays == false) {
            Long numBills = null;
            if (branchId != null) {

                numBills = salesTransactionRepository.validateCreditbillsWBr(users.getOutlet().getId(), users.getBranch().getId(), debtorsId, currentDate, fiscalYear);
            } else {
                numBills = salesTransactionRepository.validateCreditbillsWtBr(users.getOutlet().getId(), debtorsId, currentDate, fiscalYear);
            }
            if (numBills != null && numBills >= ledgerMaster.getCreditNumBills().longValue()) {
                creditBills = true;
                jsonObject.addProperty("message", "Credit Number of Bills Limit Exceedes");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        }
        /****** Checking for Credit Value(Credit Amounts) Limit *****/
        if (creditBills == false) {
            Double billValue = 0.0;
            if (branchId != null) {
                billValue = salesTransactionRepository.validateCreditvaluesWBr(users.getOutlet().getId(), users.getBranch().getId(), debtorsId, currentDate, fiscalYear);
            } else {
                billValue = salesTransactionRepository.validateCreditvaluesWtBr(users.getOutlet().getId(), debtorsId, currentDate, fiscalYear);
            }
            if (billValue >= ledgerMaster.getCreditBillValue()) {
                creditBillValue = true;
                jsonObject.addProperty("message", "Credit Bill Value Limit Exceedes");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        }
        if (creditDays == false && creditBills == false && creditBillValue == false) {
            jsonObject.addProperty("message", "Allow to Proceedes Invoice");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }

        return jsonObject;
    }

    public JsonObject mobileSDOutstandingList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        String flag = "saleList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<LedgerMaster> balanceSummaries = new ArrayList<>();
        balanceSummaries = ledgerRepository.findByPrincipleGroupsIdAndStatus(1L, true);
        for (LedgerMaster balanceSummary : balanceSummaries) {
            Long ledgerId = balanceSummary.getId();
            JsonObject jsonObject = new JsonObject();
            LocalDate endDate = null;
            LocalDate startDate = null;
           /* if (!request.get("end_date").equalsIgnoreCase("") && !request.get("start_date").equalsIgnoreCase("")) {
                System.out.println("End Date:"+request.get("end_date"));
                endDate = LocalDate.parse(request.get("end_date").toString());
                startDate = LocalDate.parse(request.get("start_date"));
            }else {

                LocalDate today = LocalDate.now();
                System.out.println("First day: " + today.withDayOfMonth(1));
                System.out.println("Last day: " + today.withDayOfMonth(today.lengthOfMonth()));
                startDate = today.withDayOfMonth(1);
                endDate = today.withDayOfMonth(today.lengthOfMonth());
            }*/
            try {
                endDate = LocalDate.parse(request.get("end_date"));
                startDate = LocalDate.parse(request.get("start_date"));
                list = salesTransactionRepository.findmobilesumTotalAmt(ledgerId, startDate, endDate, true);
                JsonArray innerArr = new JsonArray();
                for (int i = 0; i < list.size(); i++) {
                    JsonObject inside = new JsonObject();
                    Object[] objp = list.get(i);
                    sumTotal = Double.parseDouble(objp[0].toString());
                    sumBase = Double.parseDouble(objp[1].toString());
                    sumTax = Double.parseDouble(objp[2].toString());
                    totalInvoice = Integer.parseInt(objp[3].toString());
                    jsonObject.addProperty("TotalAmt", sumTotal);
                    jsonObject.addProperty("TotalBase", sumBase);
                    jsonObject.addProperty("TotalTax", sumTax);
                    jsonObject.addProperty("TotalInvoiceCount", totalInvoice);
                    jsonObject.addProperty("DebtorsId", ledgerId);
                    jsonObject.addProperty("flag", flag);
                    closingBalance = closingBalance + sumTotal;
                }


            } catch (Exception e) {
                salesInvoiceLogger.error("Error in salesDelete()->" + e.getMessage());
            }

            jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());
            if (sumTotal > 0) result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        json.addProperty("closingBalance", closingBalance);
        json.addProperty("todayDate", String.valueOf(LocalDate.now()));
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public JsonObject mobilesaleList(Map<String, String> request) {
        JsonArray result = new JsonArray();
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesInvoice> saleInvoice = new ArrayList<>();

//        Map<String, String[]> paramMap = request.getParameterMap();

        LocalDate endDatep = null;

        LocalDate startDatep = null;
        if (!request.get("end_date").equalsIgnoreCase("") && !request.get("start_date").equalsIgnoreCase("")) {
            endDatep = LocalDate.parse(request.get("end_date"));

            startDatep = LocalDate.parse(request.get("start_date"));

        } else {

            LocalDate today = LocalDate.now();
            System.out.println("First day: " + today.withDayOfMonth(1));
            System.out.println("Last day: " + today.withDayOfMonth(today.lengthOfMonth()));
            startDatep = today.withDayOfMonth(1);
            endDatep = today.withDayOfMonth(today.lengthOfMonth());

        }
        System.out.println(("Debtors Id:" + request.get("sundry_debtor_id")));
        saleInvoice = salesTransactionRepository.findSaleListForMobile(Long.valueOf(request.get("sundry_debtor_id")), startDatep, endDatep, true);

        for (TranxSalesInvoice invoices : saleInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("invoice_id", invoices.getId());
            response.addProperty("invoice_no", invoices.getSalesInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(invoices.getBillDate()).toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_debtor_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtor_id", invoices.getSundryDebtors().getId());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("transaction_date", invoices.getBillDate().toString());
            response.addProperty("baseAmt", invoices.getTotalBaseAmount());
            response.addProperty("taxAmt", invoices.getTotalTax());
            response.addProperty("baseAmt", invoices.getTotalBaseAmount());
            response.addProperty("taxAmt", invoices.getTotalTax());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public Object saleinvoicedetails(Map<String, String> request) {
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        Map<String, String[]> paramMap = request.get();

        JsonArray result = new JsonArray();
        List<TranxSalesInvoiceDetailsUnits> saleInvoice = new ArrayList<>();

        saleInvoice = tranxSalesInvoiceDetailsUnitRepository.findSalesInvoicesDetails(Long.valueOf(request.get("sales_invoice_id")), true);

        for (TranxSalesInvoiceDetailsUnits invoices : saleInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_id", invoices.getSalesInvoice().getId());
            response.addProperty("product_id", invoices.getProduct().getId());
            response.addProperty("product_name", invoices.getProduct().getProductName());
            response.addProperty("unit_id", invoices.getUnits().getId());
            response.addProperty("unit_name", invoices.getUnits().getUnitName());
            response.addProperty("qty", invoices.getQty());
            response.addProperty("rate", invoices.getRate());
            response.addProperty("totalAmt", invoices.getFinalAmount());
            response.addProperty("igst", invoices.getIgst());
            response.addProperty("total_igst", invoices.getTotalIgst());
            response.addProperty("discount_amt", invoices.getDiscountAmount());
            response.addProperty("discount_per", invoices.getDiscountPer());
            result.add(response);
        }

        TranxSalesInvoice tranxSalesInvoice = salesTransactionRepository.findByIdAndStatus(Long.parseLong(request.get("sales_invoice_id")), true);

        System.out.println(result);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.addProperty("totalBaseAmt", tranxSalesInvoice.getTotalBaseAmount());
        output.addProperty("roundoff", tranxSalesInvoice.getRoundOff());
        output.addProperty("finalAmt", tranxSalesInvoice.getTotalAmount());
        output.addProperty("taxAmt", tranxSalesInvoice.getTotalTax());
        output.addProperty("totalDisAmt", tranxSalesInvoice.getTotalSalesDiscountAmt());
        output.add("data", result);
        return output;
    }

    public JsonObject mobileSaleOnDateList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        String flag = "saleList";


        if (request.get("dateflag").equalsIgnoreCase("next")) {
            LocalDate nextDate = LocalDate.parse(request.get("date"));
            LocalDate oneMonthLater = nextDate.plusMonths(1);
            startDatep = oneMonthLater.withDayOfMonth(1);
            endDatep = oneMonthLater.withDayOfMonth(oneMonthLater.lengthOfMonth());

        } else if (request.get("dateflag").equalsIgnoreCase("prev")) {
            LocalDate backDate = LocalDate.parse(request.get("date").toString());
            System.out.println("todaydate" + backDate);
            LocalDate oneMonthLater = backDate.minusMonths(1);
            startDatep = oneMonthLater.withDayOfMonth(1);
            endDatep = oneMonthLater.withDayOfMonth(oneMonthLater.lengthOfMonth());

        }

        System.out.println("dateFlag" + request.get("dateflag"));
        System.out.println("startDate" + startDatep);
        System.out.println("endDate" + endDatep);

//        String flag = "purchaseList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<LedgerMaster> balanceSummaries = new ArrayList<>();
        balanceSummaries = ledgerRepository.findByPrincipleGroupsIdAndStatus(5L, true);
        for (LedgerMaster balanceSummary : balanceSummaries) {
            Long ledgerId = balanceSummary.getId();
            JsonObject jsonObject = new JsonObject();


            try {
                list = salesTransactionRepository.findmobilesumTotalAmt(ledgerId, startDatep, endDatep, true);
                JsonArray innerArr = new JsonArray();
                for (int i = 0; i < list.size(); i++) {
                    JsonObject inside = new JsonObject();
                    Object[] objp = list.get(i);
                    sumTotal = Double.parseDouble(objp[0].toString());
                    sumBase = Double.parseDouble(objp[1].toString());
                    sumTax = Double.parseDouble(objp[2].toString());
                    totalInvoice = Integer.parseInt(objp[3].toString());
                    jsonObject.addProperty("TotalAmt", sumTotal);
                    jsonObject.addProperty("TotalBase", sumBase);
                    jsonObject.addProperty("TotalTax", sumTax);
                    jsonObject.addProperty("TotalInvoiceCount", totalInvoice);
                    jsonObject.addProperty("CreditorsId", ledgerId);
                    jsonObject.addProperty("flag", flag);
                    closingBalance = closingBalance + sumTotal;
                    //   innerArr.add(inside);
                }


            } catch (Exception e) {
                salesInvoiceLogger.error("Error in salesDelete()->" + e.getMessage());
            }
            jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());
            if (sumTotal > 0) result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        LocalDate today = LocalDate.now();
        json.addProperty("closingBalance", closingBalance);
        json.addProperty("todayDate", String.valueOf(startDatep));
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public Object mobileReceivableList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        String flag = "saleList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<LedgerMaster> balanceSummaries = new ArrayList<>();
        balanceSummaries = ledgerRepository.findByPrincipleGroupsIdAndStatus(1L, true);
        for (LedgerMaster balanceSummary : balanceSummaries) {
            Long ledgerId = balanceSummary.getId();
            JsonObject jsonObject = new JsonObject();
            LocalDate endDate = null;
            LocalDate startDate = null;
            try {
                endDate = LocalDate.parse(request.get("end_date"));
                startDate = LocalDate.parse(request.get("start_date"));
                list = salesTransactionRepository.findReceivableTotalAmt(ledgerId, startDate, endDate, true);
                for (int i = 0; i < list.size(); i++) {
                    Object[] objp = list.get(i);
                    sumTotal = Double.parseDouble(objp[0].toString());
                    totalInvoice = Integer.parseInt(objp[1].toString());
                    if (sumTotal > 0) {
                        jsonObject.addProperty("TotalAmt", sumTotal);
                        jsonObject.addProperty("TotalInvoiceCount", totalInvoice);
                        jsonObject.addProperty("DebtorsId", ledgerId);
                        jsonObject.addProperty("flag", flag);
                        closingBalance = closingBalance + sumTotal;
                    }
                }

            } catch (Exception e) {
                salesInvoiceLogger.error("Error in salesDelete()->" + e.getMessage());
            }
            if (sumTotal > 0) {
                jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());
                result.add(jsonObject);
            }
        }
        JsonObject json = new JsonObject();
        json.addProperty("closingBalance", closingBalance);
        json.addProperty("todayDate", String.valueOf(LocalDate.now()));
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public Object validateSalesInvoicesUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        TranxSalesInvoice salesInvoice = null;
        Long invoiceId = Long.parseLong(request.getParameter("invoice_id"));
        if (users.getBranch() != null) {
            salesInvoice = salesTransactionRepository.findByOutletIdAndBranchIdAndSalesInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesInvoiceNo"));
        } else {
            salesInvoice = salesTransactionRepository.findByOutletIdAndSalesInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), request.getParameter("salesInvoiceNo"));
        }
        if (salesInvoice != null && invoiceId != salesInvoice.getId()) {
            responseMessage.setMessage("Duplicate sales invoice number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public JsonObject getCounterSalesDetailsData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        JsonObject finalResult = new JsonObject();
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject result = new JsonObject();
        LocalDate date = LocalDate.now();
        List<TranxCounterSales> counterSales = new ArrayList<>();
        if (paramMap.containsKey("payment_mode")) {
            String paymentMode = request.getParameter("payment_mode");
            if (paymentMode.equalsIgnoreCase("all")) {
                if (branch != null)
                    counterSales = counterSaleRepository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), branch.getId(), true);
                else {
                    counterSales = counterSaleRepository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
                }
            } else {
                if (branch != null)
                    counterSales = counterSaleRepository.findByOutletIdAndBranchIdAndStatusAndPaymentModeIgnoreCase(users.getOutlet().getId(), branch.getId(), true, paymentMode);
                else
                    counterSales = counterSaleRepository.findByOutletIdAndStatusAndPaymentModeIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), true, paymentMode);
            }
        }
        JsonArray row = new JsonArray();

        for (TranxCounterSales mSales : counterSales) {
            result.addProperty("paymentMode", mSales.getPaymentMode());
            int i = 0;
            Double totalAmt = 0.0;
            List<TranxCounterSalesDetailsUnits> detailsUnits = tranxCSDetailsUnitsRepository.findByCounterSalesIdAndStatusAndTransactionStatus(mSales.getId(), true, 1L);
            for (TranxCounterSalesDetailsUnits mUnits : detailsUnits) {
                JsonObject object = new JsonObject();
                object.addProperty("id", mSales.getId());
                object.addProperty("details_id", mUnits.getId());
                object.addProperty("counterId", mSales.getId());
                object.addProperty("countersrNo", mSales.getCounterSaleSrNo());
                object.addProperty("counterNo", mSales.getCounterSaleNo());
                object.addProperty("invoiceDate", mSales.getCounterSalesDate().toString());
                object.addProperty("payment_mode", mSales.getPaymentMode());
                object.addProperty("mobile_number", mSales.getMobileNumber());
                object.addProperty("productId", mUnits.getProduct().getId());
                object.addProperty("productName", mUnits.getProduct().getProductName());
                object.addProperty("levelaId", mUnits.getLevelA() != null ? mUnits.getLevelA().getId().toString() : "");
                object.addProperty("levelbId", mUnits.getLevelB() != null ? mUnits.getLevelB().getId().toString() : "");
                object.addProperty("levelcId", mUnits.getLevelC() != null ? mUnits.getLevelC().getId().toString() : "");
                object.addProperty("unitId", mUnits.getUnits() != null ? mUnits.getUnits().getId().toString() : "");
                object.addProperty("unitName", mUnits.getUnits() != null ? mUnits.getUnits().getUnitName() : "");
                object.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "");
                object.addProperty("qty", mUnits.getQty());
                object.addProperty("free_qty", mUnits.getFreeQty() != null ? mUnits.getFreeQty() : 0);
                object.addProperty("unit_conv", mUnits.getUnitConversions() != null ? mUnits.getUnitConversions() : 0.0);
                object.addProperty("rate", mUnits.getRate() != null ? mUnits.getRate() : 0.0);
                object.addProperty("dis_amt", mUnits.getDiscountAmount() != null ? mUnits.getDiscountAmount() : 0.0);
                object.addProperty("dis_per", mUnits.getDiscountPer() != null ? mUnits.getDiscountPer() : 0.0);
                object.addProperty("dis_per2", mUnits.getDiscountBInPer() != null ? mUnits.getDiscountBInPer() : 0.0);
                object.addProperty("row_dis_amt", mUnits.getRowDiscountamt() != null ? mUnits.getRowDiscountamt() : 0.0);
                object.addProperty("total_amt", mUnits.getNetAmount() != null ? mUnits.getNetAmount() : 0.0);
                object.addProperty("igst", 0.0);
                object.addProperty("sgst", 0.0);
                object.addProperty("cgst", 0.0);
                object.addProperty("total_igst", 0.0);
                object.addProperty("total_sgst", 0.0);
                object.addProperty("total_cgst", 0.0);
                totalAmt = totalAmt + mUnits.getNetAmount();
                object.addProperty("is_batch", mUnits.getProduct().getIsBatchNumber() != null ? mUnits.getProduct().getIsBatchNumber() : false);
                if (mUnits.getProductBatchNo() != null) {
                    object.addProperty("b_detailsId", mUnits.getProductBatchNo().getId());
                    object.addProperty("batch_no", mUnits.getProductBatchNo().getBatchNo());
                    object.addProperty("b_expiry", mUnits.getProductBatchNo().getExpiryDate() != null ? mUnits.getProductBatchNo().getExpiryDate().toString() : "");
                    object.addProperty("purchase_rate", mUnits.getProductBatchNo().getPurchaseRate());
                    object.addProperty("is_batch", true);
                    object.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA());
                    object.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB());
                    object.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC());
                    object.addProperty("min_discount", mUnits.getProductBatchNo().getMinDiscount());
                    object.addProperty("max_discount", mUnits.getProductBatchNo().getMaxDiscount());
                    object.addProperty("manufacturing_date", mUnits.getProductBatchNo().getManufacturingDate() != null ? mUnits.getProductBatchNo().getManufacturingDate().toString() : "");
                    object.addProperty("min_margin", mUnits.getProductBatchNo().getMinMargin());
                    object.addProperty("b_rate", mUnits.getProductBatchNo().getMrp());
                    object.addProperty("sales_rate", mUnits.getProductBatchNo().getSalesRate());
                    object.addProperty("costing", mUnits.getProductBatchNo().getCosting());
                    object.addProperty("costingWithTax", mUnits.getProductBatchNo().getCostingWithTax());
                } else {
                    object.addProperty("b_detailsId", "");
                    object.addProperty("batch_no", "");
                    object.addProperty("b_expiry", "");
                    object.addProperty("purchase_rate", "");
                    object.addProperty("is_batch", "");
                    object.addProperty("min_rate_a", "");
                    object.addProperty("min_rate_b", "");
                    object.addProperty("min_rate_c", "");
                    object.addProperty("min_discount", "");
                    object.addProperty("max_discount", "");
                    object.addProperty("manufacturing_date", "");
                    object.addProperty("mrp", "");
                    object.addProperty("min_margin", "");
                    object.addProperty("b_rate", "");
                    object.addProperty("costing", "");
                    object.addProperty("costingWithTax", "");
                }
                if (i == detailsUnits.size() - 1) object.addProperty("totalNetAmt", totalAmt);
                i++;
                row.add(object);
            }
        }
        result.add("row", row);
        result.addProperty("messgae", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        return result;
    }

    public JsonObject getProductEditByIdByFPUCS(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxCounterSales> invoiceTranx = counterSaleRepository.findByStatusAndTransactionDate(true, LocalDate.now());
        for (TranxCounterSales mCs : invoiceTranx) {
            List<Object[]> productIds = new ArrayList<>();
            productIds = tranxCSDetailsUnitsRepository.findByTranxPurId(mCs.getId(), true);
            productArray.addAll(productData.getProductByBFPUCommonNew(mCs.getTransactionDate(), productIds));
        }
        // productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }


    public JsonObject getCSByNo(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalResult = new JsonObject();
        JsonArray row = new JsonArray();
        try {
            String csno = request.getParameter("csno");
            JsonArray invoiceDetails = new JsonParser().parse(csno).getAsJsonArray();
            for (JsonElement mList : invoiceDetails) {
                JsonObject object = mList.getAsJsonObject();
                TranxCounterSales salesInvoice = counterSaleRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
                finalResult.addProperty("paymentMode", salesInvoice.getPaymentMode());
                List<TranxCounterSalesDetailsUnits> unitsArray = tranxCSDetailsUnitsRepository.findByCounterSalesIdAndStatusAndTransactionStatus(salesInvoice.getId(), true, 1l);
                for (TranxCounterSalesDetailsUnits mUnits : unitsArray) {
                    JsonObject unitsJsonObjects = new JsonObject();
                    unitsJsonObjects.addProperty("details_id", mUnits.getId());
                    unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                    unitsJsonObjects.addProperty("reference_type", "CS");
                    unitsJsonObjects.addProperty("reference_id", salesInvoice.getId());
                    unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                    unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ? mUnits.getLevelA().getId().toString() : "");
                    unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ? mUnits.getLevelB().getId().toString() : "");
                    unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ? mUnits.getLevelC().getId().toString() : "");
                    unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                    unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "");
                    unitsJsonObjects.addProperty("pack_id", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getId().toString() : "");
                    unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                    unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                    unitsJsonObjects.addProperty("qty", mUnits.getQty());
                    unitsJsonObjects.addProperty("rate", mUnits.getRate());
                    unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
                    unitsJsonObjects.addProperty("dis_amt", mUnits.getDiscountAmount());
                    unitsJsonObjects.addProperty("dis_per", mUnits.getDiscountPer());
                    unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer());
                    unitsJsonObjects.addProperty("total_amt", mUnits.getBaseAmt());
                    unitsJsonObjects.addProperty("final_amt", mUnits.getNetAmount());
                    unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
                    unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty());
                    unitsJsonObjects.addProperty("row_dis_amt", mUnits.getRowDiscountamt());
                    unitsJsonObjects.addProperty("tranxStatus", mUnits.getTransactionStatus());
                    unitsJsonObjects.addProperty("igst", mUnits.getIgst());
                    unitsJsonObjects.addProperty("cgst", mUnits.getCgst());
                    unitsJsonObjects.addProperty("sgst", mUnits.getSgst());
                    unitsJsonObjects.addProperty("gst", mUnits.getTotalIgst());
                    unitsJsonObjects.addProperty("total_igst", mUnits.getTotalIgst());
                    unitsJsonObjects.addProperty("total_cgst", mUnits.getTotalCgst());
                    unitsJsonObjects.addProperty("total_sgst", mUnits.getTotalSgst());

                    if (mUnits.getProductBatchNo() != null) {
                        if (mUnits.getProductBatchNo().getExpiryDate() != null) {
                            if (DateConvertUtil.convertDateToLocalDate(salesInvoice.getTransactionDate()).isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
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
                        unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA());
                        unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB());
                        unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC());
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
                    row.add(unitsJsonObjects);
                }
            }
            finalResult.add("row", row);
            LedgerMaster counterCustomer = null;
            if (users.getBranch() != null)
                counterCustomer = ledgerMasterRepository.findByLedgerCodeAndStatusAndOutletIdAndBranchId("CNCS", true, users.getOutlet().getId(), users.getBranch().getId());
            else {
                counterCustomer = ledgerMasterRepository.findByLedgerCodeAndStatusAndOutletIdAndBranchIsNull("CNCS", true, users.getOutlet().getId());
            }
            finalResult.addProperty("ledger_id", counterCustomer.getId());
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }


    public Object salesStatusUpdateById(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        TranxSalesInvoice salesInvoice = salesTransactionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("invoiceId")), true);
        if (salesInvoice != null) {
            salesInvoice.setOrderStatus(request.getParameter("invoiceStatus"));
            salesTransactionRepository.save(salesInvoice);
            responseMessage.setMessage("Invoice Order Updated");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public JsonObject AllSalesDeliveryList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesInvoice> saleInvoice = new ArrayList<>();
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

            saleInvoice = salesTransactionRepository.findSaleListWithDate(users.getOutlet().getId(), startDatep, endDatep, true);


        } else {

            saleInvoice = salesTransactionRepository.findByOrderStatusAndStatus("delivered", true);

        }

        for (TranxSalesInvoice invoices : saleInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getSalesInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(invoices.getBillDate()).toString());
            response.addProperty("sale_serial_number", invoices.getSalesSerialNumber());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_debtor_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtor_id", invoices.getSundryDebtors().getId());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration() != null ? invoices.getNarration() : "");
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTaxableAmount());
            response.addProperty("payment_mode", invoices.getPaymentMode());
            response.addProperty("invoice_id", invoices.getId());
            response.addProperty("orderStatus", invoices.getOrderStatus());
            Double invoiceDetailsUnits = tranxSalesInvoiceDetailsUnitRepository.totalinvoiceNumberOfProduct(invoices.getId(), 1, true);
            response.addProperty("product_count", invoiceDetailsUnits);
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public JsonObject AllSalesCancelledList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesInvoice> saleInvoice = new ArrayList<>();
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

            saleInvoice = salesTransactionRepository.findSaleListWithDate(users.getOutlet().getId(), startDatep, endDatep, true);


        } else {

            saleInvoice = salesTransactionRepository.findByOrderStatusAndStatus("cancelled", true);

        }

        for (TranxSalesInvoice invoices : saleInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getSalesInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(invoices.getBillDate()).toString());
            response.addProperty("sale_serial_number", invoices.getSalesSerialNumber());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_debtor_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtor_id", invoices.getSundryDebtors().getId());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration() != null ? invoices.getNarration() : "");
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTaxableAmount());
            response.addProperty("payment_mode", invoices.getPaymentMode());
            response.addProperty("invoice_id", invoices.getId());
            response.addProperty("orderStatus", invoices.getOrderStatus());
            Double invoiceDetailsUnits = tranxSalesInvoiceDetailsUnitRepository.totalinvoiceNumberOfProduct(invoices.getId(), 1, true);
            response.addProperty("product_count", invoiceDetailsUnits);
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public Object listOfCounterSales(HttpServletRequest req) {
        JsonObject response = new JsonObject();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
            List salesList = new ArrayList();
            String sql = "SELECT distinct(counter_sales_id) FROM `tranx_counter_sales_details_units_tbl` LEFT JOIN " + "tranx_counter_sales_tbl ON tranx_counter_sales_details_units_tbl.counter_sales_id=tranx_counter_sales_tbl.id " + "WHERE tranx_counter_sales_details_units_tbl.status=1 AND tranx_counter_sales_details_units_tbl.transaction_status=1 AND " + "tranx_counter_sales_tbl.outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) {
                sql += " AND tranx_counter_sales_tbl.branch_id=" + users.getBranch().getId();
            } else {
                sql = sql + " AND tranx_counter_sales_tbl.branch_id IS NULL";
            }
            System.out.println("sql :" + sql);
            Query query = entityManager.createNativeQuery(sql);
            salesList = query.getResultList();
            JsonArray saleArray = new JsonArray();
            for (Object mSalesList : salesList) {
                JsonObject object = new JsonObject();
                TranxCounterSales counterSale = counterSaleRepository.findByIdAndStatus(Long.parseLong(mSalesList.toString()), true);
                object.addProperty("id", counterSale.getId());
                object.addProperty("saleSrNo", counterSale.getCounterSaleSrNo());
                object.addProperty("saleNo", counterSale.getCounterSaleNo());
                object.addProperty("saleDate", counterSale.getCounterSalesDate() != null ? counterSale.getCounterSalesDate().toString() : "");
                object.addProperty("mobileNo", counterSale.getMobileNumber());
                object.addProperty("totalQty", counterSale.getTotalqty());
                object.addProperty("paymentMode", counterSale.getPaymentMode());
                object.addProperty("totalBaseAmount", counterSale.getTotalBaseAmt());
                object.addProperty("totalDiscount", counterSale.getTotalDiscount());
                object.addProperty("totalBill", counterSale.getTotalBill());
                object.addProperty("isConversion", counterSale.getIsBillConverted());
                saleArray.add(object);
            }
            response.add("response", saleArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Exception in listOfCounterSales" + e);
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response.toString();
    }

    public JsonObject findCounterSalesById(HttpServletRequest request) {
        JsonObject response = new JsonObject();

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxCounterSales salesInvoice = counterSaleRepository.findByIdAndStatus(id, true);

            if (salesInvoice != null) {
                JsonArray row = new JsonArray();
                finalResult.addProperty("id", salesInvoice.getId());
                finalResult.addProperty("narration", salesInvoice.getNarrations() != null ? salesInvoice.getNarrations() : "");
                finalResult.addProperty("branch", salesInvoice.getBranch() != null ? salesInvoice.getBranch().getBranchName() : "");
                finalResult.addProperty("outlet", salesInvoice.getOutlet() != null ? salesInvoice.getOutlet().getCompanyName() : "");
                finalResult.addProperty("counterSaleSrNo", salesInvoice.getCounterSaleSrNo());
                finalResult.addProperty("counterSaleNo", salesInvoice.getCounterSaleNo());
                finalResult.addProperty("billDate", salesInvoice.getTransactionDate().toString());
                finalResult.addProperty("customerName", salesInvoice.getCustomerName() != null ? salesInvoice.getCustomerName() : "");
                finalResult.addProperty("mobileNumber", salesInvoice.getMobileNumber() != null ? salesInvoice.getMobileNumber().toString() : "");
                finalResult.addProperty("totalSalesDiscountAmt", salesInvoice.getTotalDiscount());
                finalResult.addProperty("totalQty", salesInvoice.getTotalqty());
                finalResult.addProperty("totalFreeQty", salesInvoice.getFreeQty());
                finalResult.addProperty("totalBill", salesInvoice.getTotalBill());
                finalResult.addProperty("totalBaseAmt", salesInvoice.getTotalBaseAmt());
                finalResult.addProperty("totalTaxableAmt", salesInvoice.getTaxableAmt());
                finalResult.addProperty("roundoff", salesInvoice.getRoundoff());
                finalResult.addProperty("paymentMode", salesInvoice.getPaymentMode());
                List<TranxCounterSalesDetailsUnits> unitsArray = tranxCSDetailsUnitsRepository.findByCounterSalesIdAndStatusAndTransactionStatus(salesInvoice.getId(), true, 1L);
                for (TranxCounterSalesDetailsUnits mUnits : unitsArray) {
                    JsonObject unitsJsonObjects = new JsonObject();
                    unitsJsonObjects.addProperty("details_id", mUnits.getId());
                    unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                    unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                    unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ? mUnits.getLevelA().getId().toString() : "");
                    unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ? mUnits.getLevelB().getId().toString() : "");
                    unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ? mUnits.getLevelC().getId().toString() : "");
                    unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                    unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                    unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "");
                    unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                    unitsJsonObjects.addProperty("qty", mUnits.getQty());
                    unitsJsonObjects.addProperty("rate", mUnits.getRate());
                    unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
                    unitsJsonObjects.addProperty("dis_amt", mUnits.getDiscountAmount());
                    unitsJsonObjects.addProperty("dis_per", mUnits.getDiscountPer());
                    unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer());
                    unitsJsonObjects.addProperty("total_amt", mUnits.getBaseAmt());
                    unitsJsonObjects.addProperty("final_amt", mUnits.getNetAmount());
                    unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
                    unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty());
                    unitsJsonObjects.addProperty("row_dis_amt", mUnits.getRowDiscountamt());
                    unitsJsonObjects.addProperty("igst", mUnits.getIgst());
                    unitsJsonObjects.addProperty("cgst", mUnits.getCgst());
                    unitsJsonObjects.addProperty("sgst", mUnits.getSgst());
                    unitsJsonObjects.addProperty("gst", mUnits.getTotalIgst());
                    unitsJsonObjects.addProperty("total_igst", mUnits.getTotalIgst());
                    unitsJsonObjects.addProperty("total_cgst", mUnits.getTotalCgst());
                    unitsJsonObjects.addProperty("total_sgst", mUnits.getTotalSgst());
                    if (mUnits.getProductBatchNo() != null) {
                        if (mUnits.getProductBatchNo().getExpiryDate() != null) {
                            if (salesInvoice.getTransactionDate().after(DateConvertUtil.convertStringToDate(mUnits.getProductBatchNo().getExpiryDate().toString()))) {
//                                if (salesInvoice.getTransactionDate().isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
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
                        unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA());
                        unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB());
                        unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC());
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
                    row.add(unitsJsonObjects);
                }

                finalResult.add("row", row);
                finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            }
            response.add("response", finalResult);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            response.addProperty("message", "Failed to get data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject findCounterSalesPrdouctsPkgUnit(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long id = Long.valueOf(request.getParameter("id"));
        TranxCounterSales invoiceTranx = counterSaleRepository.findByIdAndStatus(id, true);
        if (invoiceTranx != null) {
            List<Object[]> productIds = new ArrayList<>();
            productIds = tranxCSDetailsUnitsRepository.findByTranxPurId(invoiceTranx.getId(), true);
            productArray.addAll(productData.getProductByBFPUCommonNew(invoiceTranx.getTransactionDate(), productIds));
        }
        // productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public Object listOfConsumerSales(HttpServletRequest req) {
        JsonObject response = new JsonObject();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
            List<TranxSalesCompInvoice> salesList = new ArrayList<>();


            String sql = "SELECT * FROM `tranx_sales_comp_invoice_tbl` WHERE status=1 AND outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) sql += " AND branch_id=" + users.getBranch().getId();
            Query query = entityManager.createNativeQuery(sql, TranxSalesCompInvoice.class);
            salesList = query.getResultList();

            JsonArray saleArray = new JsonArray();
            for (TranxSalesCompInvoice consInvoice : salesList) {
                JsonObject object = new JsonObject();
                object.addProperty("id", consInvoice.getId());
                object.addProperty("saleSrNo", consInvoice.getSalesSerialNumber());
                object.addProperty("saleNo", consInvoice.getSalesInvoiceNo());
                object.addProperty("saleDate", consInvoice.getBillDate() != null ? consInvoice.getBillDate().toString() : "");
                object.addProperty("clientName", consInvoice.getClientName());
                object.addProperty("clientAddress", consInvoice.getClientAddress());
                object.addProperty("mobileNo", consInvoice.getMobileNumber());
                object.addProperty("totalQty", consInvoice.getTotalqty());
                object.addProperty("paymentMode", consInvoice.getPaymentMode());
                object.addProperty("totalBaseAmount", consInvoice.getTotalBaseAmount());
                object.addProperty("totalDiscount", consInvoice.getTotalSalesDiscountAmt());
                object.addProperty("totalsDiscountPer", consInvoice.getSalesDiscountPer());
                object.addProperty("totalBill", consInvoice.getTotalAmount());

                saleArray.add(object);
            }
            response.add("response", saleArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Exception in listOfConsumerSales" + e);
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response.toString();
    }

    public Object customerProductsHistory(Map<String, String> jsonReq, HttpServletRequest req) {
        JsonObject response = new JsonObject();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
            Long mobileNo = Long.valueOf(jsonReq.get("mobileNo"));

            List<TranxSalesCompInvoiceDetailsUnits> salesList = new ArrayList<>();
            String sql = "SELECT * FROM `tranx_sales_comp_details_units_tbl` AS tscdut LEFT JOIN " + "tranx_sales_comp_invoice_tbl AS tscit ON" + " sales_invoice_id=tscit.id WHERE " + "tscit.mobile_number=" + mobileNo + " AND tscit.status=1 AND tscit.outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) sql += " AND tscit.branch_id=" + users.getBranch().getId();
            System.out.println("sql :" + sql);
            Query query = entityManager.createNativeQuery(sql, TranxSalesCompInvoiceDetailsUnits.class);
            salesList = query.getResultList();

            JsonArray saleArray = new JsonArray();
            for (TranxSalesCompInvoiceDetailsUnits details : salesList) {
                JsonObject object = new JsonObject();
                TranxSalesCompInvoice compInvoice = salesCompTransactionRepository.findByIdAndStatus(details.getSalesInvoiceId(), true);
                Product product = productRepository.findByIdAndStatus(details.getProductId(), true);
                Units units = unitsRepository.findByIdAndStatus(details.getUnitsId(), true);
                ProductBatchNo batchNo = productBatchNoRepository.findByIdAndStatus(details.getProductBatchNoId(), true);


                object.addProperty("productName", product.getProductName());
                object.addProperty("packName", product.getPackingMaster() != null ? product.getPackingMaster().getPackName() : "");
                object.addProperty("unitName", units.getUnitName());
                object.addProperty("batchNo", batchNo.getBatchNo());
                object.addProperty("qty", details.getQty());
                object.addProperty("rate", details.getRate());
                object.addProperty("totalAmount", details.getTotalAmount());
                object.addProperty("disc", details.getTotalDiscountInAmt());
                object.addProperty("netAmount", details.getFinalAmount());
                object.addProperty("paymentMode", compInvoice.getPaymentMode());

                saleArray.add(object);
            }
            response.add("response", saleArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Exception in listOfConsumerSales" + e);
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response.toString();
    }

    public JsonObject findConsumerSalesById(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxSalesCompInvoice consInvoice = salesCompTransactionRepository.findByIdAndStatus(id, true);

            if (consInvoice != null) {
                JsonArray row = new JsonArray();
                finalResult.addProperty("id", consInvoice.getId());
                finalResult.addProperty("narration", consInvoice.getNarration() != null ? consInvoice.getNarration() : "");
                finalResult.addProperty("consumerSaleSrNo", consInvoice.getSalesSerialNumber());
                finalResult.addProperty("consumerSaleNo", consInvoice.getSalesInvoiceNo());
                finalResult.addProperty("billDate", consInvoice.getBillDate().toString());
                finalResult.addProperty("clientName", consInvoice.getClientName() != null ? consInvoice.getClientName() : "");
                finalResult.addProperty("clientAddress", consInvoice.getClientAddress() != null ? consInvoice.getClientAddress() : "");
                finalResult.addProperty("mobileNumber", consInvoice.getMobileNumber() != null ? consInvoice.getMobileNumber() : "");
                finalResult.addProperty("doctorId", consInvoice.getDoctorId() != null ? consInvoice.getDoctorId().toString() : "");
                finalResult.addProperty("doctorAddress", consInvoice.getDoctorAddress() != null ? consInvoice.getDoctorAddress() : "");
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(consInvoice.getSundryDebtorsId(), true);
                PatientMaster patientMaster = patientMasterRepository.findByPatientCodeAndStatus(ledgerMaster.getLedgerCode(), true);
                finalResult.addProperty("debtorId", patientMaster != null ? patientMaster.getId().toString() : "");
                finalResult.addProperty("totalSalesDiscountAmt", consInvoice.getTotalSalesDiscountAmt());
                finalResult.addProperty("sales_discount", consInvoice.getSalesDiscountPer());
                finalResult.addProperty("totalQty", consInvoice.getTotalqty());
                finalResult.addProperty("totalFreeQty", consInvoice.getFreeQty());
                finalResult.addProperty("totalBill", consInvoice.getTotalAmount());
                finalResult.addProperty("totalBaseAmt", consInvoice.getTotalBaseAmount());
                finalResult.addProperty("totalTaxableAmt", consInvoice.getTaxableAmount());
                finalResult.addProperty("roundoff", consInvoice.getRoundOff());
                finalResult.addProperty("paymentMode", consInvoice.getPaymentMode());
                List<TranxSalesCompInvoiceDetailsUnits> unitsArray = tranxSalesCompInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(consInvoice.getId(), true);
                for (TranxSalesCompInvoiceDetailsUnits mUnits : unitsArray) {
                    JsonObject unitsJsonObjects = new JsonObject();

                    Product product = productRepository.findByIdAndStatus(mUnits.getProductId(), true);
                    Units units = unitsRepository.findByIdAndStatus(mUnits.getUnitsId(), true);

                    unitsJsonObjects.addProperty("details_id", mUnits.getId());
                    unitsJsonObjects.addProperty("product_id", mUnits.getProductId());
                    unitsJsonObjects.addProperty("product_name", product.getProductName());
                    unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelAId() != null ? mUnits.getLevelAId().toString() : "");
                    unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelBId() != null ? mUnits.getLevelBId().toString() : "");
                    unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelCId() != null ? mUnits.getLevelCId().toString() : "");
                    unitsJsonObjects.addProperty("unit_name", units.getUnitName());
                    unitsJsonObjects.addProperty("unitId", mUnits.getUnitsId());
                    unitsJsonObjects.addProperty("pack_name", product.getPackingMaster() != null ? product.getPackingMaster().getPackName() : "");
                    unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                    unitsJsonObjects.addProperty("qty", mUnits.getQty());
                    unitsJsonObjects.addProperty("rate", mUnits.getRate());
                    unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
                    unitsJsonObjects.addProperty("dis_amt", mUnits.getDiscountAmount());
                    unitsJsonObjects.addProperty("dis_per", mUnits.getDiscountPer());
                    unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer());
                    unitsJsonObjects.addProperty("total_amt", mUnits.getBaseAmt());
                    unitsJsonObjects.addProperty("final_amt", mUnits.getFinalAmount());
                    unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
                    unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty());
                    unitsJsonObjects.addProperty("row_dis_amt", mUnits.getTotalDiscountInAmt());
                    unitsJsonObjects.addProperty("igst", mUnits.getIgst() != null ? mUnits.getIgst() : 0.0);
                    unitsJsonObjects.addProperty("sgst", mUnits.getSgst() != null ? mUnits.getSgst() : 0.0);
                    unitsJsonObjects.addProperty("cgst", mUnits.getCgst() != null ? mUnits.getCgst() : 0.0);
                    unitsJsonObjects.addProperty("total_igst", mUnits.getTotalIgst() != null ? mUnits.getTotalIgst() : 0.0);
                    unitsJsonObjects.addProperty("total_sgst", mUnits.getTotalSgst() != null ? mUnits.getTotalSgst() : 0.0);
                    unitsJsonObjects.addProperty("total_cgst", mUnits.getTotalCgst() != null ? mUnits.getTotalCgst() : 0.0);
                    unitsJsonObjects.addProperty("dis_per_cal", mUnits.getDiscountPerCal());
                    unitsJsonObjects.addProperty("dis_amt_cal", mUnits.getDiscountAmountCal());
                    unitsJsonObjects.addProperty("gst", mUnits.getIgst());
                    unitsJsonObjects.addProperty("final_amt", mUnits.getFinalAmount());
                    unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty());
                    unitsJsonObjects.addProperty("gross_amt", mUnits.getGrossAmt());
                    unitsJsonObjects.addProperty("add_chg_amt", mUnits.getAdditionChargesAmt());
                    unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1());
                    unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt());


                    if (mUnits.getProductBatchNoId() != null) {
                        ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(mUnits.getProductBatchNoId(), true);
                        if (productBatchNo.getExpiryDate() != null) {
                            if (DateConvertUtil.convertDateToLocalDate(consInvoice.getBillDate()).isAfter(productBatchNo.getExpiryDate())) {
                                unitsJsonObjects.addProperty("is_expired", true);
                            } else {
                                unitsJsonObjects.addProperty("is_expired", false);
                            }
                        } else {
                            unitsJsonObjects.addProperty("is_expired", false);
                        }
                        unitsJsonObjects.addProperty("b_detailsId", productBatchNo.getId());
                        unitsJsonObjects.addProperty("batch_no", productBatchNo.getBatchNo());
                        unitsJsonObjects.addProperty("b_expiry", productBatchNo.getExpiryDate() != null ? productBatchNo.getExpiryDate().toString() : "");
                        unitsJsonObjects.addProperty("purchase_rate", productBatchNo.getPurchaseRate());
                        unitsJsonObjects.addProperty("is_batch", true);
                        unitsJsonObjects.addProperty("min_rate_a", productBatchNo.getMinRateA());
                        unitsJsonObjects.addProperty("min_rate_b", productBatchNo.getMinRateB());
                        unitsJsonObjects.addProperty("min_rate_c", productBatchNo.getMinRateC());
                        unitsJsonObjects.addProperty("min_discount", productBatchNo.getMinDiscount());
                        unitsJsonObjects.addProperty("max_discount", productBatchNo.getMaxDiscount());
                        unitsJsonObjects.addProperty("manufacturing_date", productBatchNo.getManufacturingDate() != null ? productBatchNo.getManufacturingDate().toString() : "");
                        unitsJsonObjects.addProperty("min_margin", productBatchNo.getMinMargin());
                        unitsJsonObjects.addProperty("b_rate", productBatchNo.getMrp());
                        unitsJsonObjects.addProperty("sales_rate", productBatchNo.getSalesRate());
                        unitsJsonObjects.addProperty("costing", productBatchNo.getCosting());
                        unitsJsonObjects.addProperty("costingWithTax", productBatchNo.getCostingWithTax());
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
                    row.add(unitsJsonObjects);
                }

                finalResult.add("row", row);
                finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            }
            response.add("response", finalResult);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in findConsumerSalesById" + e1.getMessage());
            System.out.println(e1.getMessage());
            response.addProperty("message", "Failed to get data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject findConsumerSalesPrdouctsPkgUnit(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long id = Long.valueOf(request.getParameter("id"));
        TranxSalesCompInvoice invoiceTranx = salesCompTransactionRepository.findByIdAndStatus(id, true);
        if (invoiceTranx != null) {
            List<Object[]> productIds = new ArrayList<>();
            productIds = tranxSalesCompInvoiceDetailsUnitRepository.findByTranxPurId(invoiceTranx.getId(), true);
            productArray.addAll(productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds));
        }
        // productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public Object updateTranxSalesCompInvoices(MultipartHttpServletRequest request) throws Exception {
        ResponseMessage responseMessage = new ResponseMessage();
        TranxSalesCompInvoice invoiceTranx = salesCompTransactionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TranxSalesCompInvoice salesInvoice = null;
        TransactionTypeMaster tranxType = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String salesType = request.getParameter("sale_type");
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CONS");

//        if (salesInvoice == null) mSalesTranx = saveIntoCompInvoice(users, request, tranxType, salesType);

        Outlet outlet = users.getOutlet();
//        TranxSalesCompInvoice invoiceTranx = new TranxSalesCompInvoice();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            invoiceTranx.setBranchId(branch.getId());
        }
        FileStorageProperties fileStorageProperties = new FileStorageProperties();

        invoiceTranx.setOutletId(outlet.getId());
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date dt = DateConvertUtil.convertStringToDate(request.getParameter("bill_dt"));
        invoiceTranx.setBillDate(dt);
        // invoiceTranx.setGstNumber(request.getParameter("gstNo"));
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYearId(fiscalYear.getId());
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }

        LedgerMaster discountLedger = null;
        if (users.getBranch() != null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales discount", users.getOutlet().getId(), true);
        if (discountLedger != null) {
            invoiceTranx.setSalesDiscountLedgerId(discountLedger.getId());
        }
        /****** creating ledgers against the patient master *****/
        PatientMaster patientMaster = patientMasterRepository.findByPatientCodeAndStatus(request.getParameter("debtors_id"), true);
        LedgerMaster sundryDebtors = null;
        sundryDebtors = ledgerMasterRepository.findByLedgerCodeAndStatus(patientMaster.getPatientCode(), true);
        if (sundryDebtors == null) {
            sundryDebtors = createLedger(patientMaster);
        }
        invoiceTranx.setSundryDebtorsId(sundryDebtors.getId());
        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
        invoiceTranx.setBalance(Double.parseDouble(request.getParameter("bill_amount")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("totalqty")));
        if (paramMap.containsKey("sales_discount")) {
            invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        }
        invoiceTranx.setDoctorId(null);
        invoiceTranx.setClientName(null);
        invoiceTranx.setClientAddress(null);
        invoiceTranx.setMobileNumber(null);
        if (paramMap.containsKey("doctorsId")) {
            invoiceTranx.setDoctorId(Long.parseLong(request.getParameter("doctorsId")));
        }
        if (paramMap.containsKey("client_name")) {
            invoiceTranx.setClientName(request.getParameter("client_name"));
        }
        if (paramMap.containsKey("client_address")) {
            invoiceTranx.setClientAddress(request.getParameter("client_address"));
        }
        if (paramMap.containsKey("mobile_number")) {
            invoiceTranx.setMobileNumber(request.getParameter("mobile_number"));
        }
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setDoctorAddress(request.getParameter("drAddress"));
        invoiceTranx.setPatientName(request.getParameter("patientName"));
        if (request.getFile("prescFile") != null) {
            MultipartFile image = request.getFile("prescFile");
            fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
            String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
            if (imagePath != null) {
                invoiceTranx.setImageUpload(File.separator + "uploads" + File.separator + imagePath);
            }
        }

        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        // invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setCreatedBy(users.getId());
        // invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        if (paramMap.containsKey("paymentMode")) invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        invoiceTranx.setStatus(true);
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setOperations("updated");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
        else invoiceTranx.setNarration("");
        invoiceTranx.setIsCounterSale(false);

        salesInvoice = salesCompTransactionRepository.save(invoiceTranx);

        if (salesInvoice != null) {
            /* Save into Sales Invoice Details */
            String jsonStr = request.getParameter("row");
            JsonArray invoiceDetails = new JsonParser().parse(jsonStr).getAsJsonArray();
            String referenceObj = "";
            if (paramMap.containsKey("refObject")) referenceObj = request.getParameter("refObject");
            updateIntoCompSalesInvoiceDetails(invoiceDetails, salesInvoice, branch, outlet, users.getId(), tranxType, salesType, referenceObj);

            /** Accounting Postings  **/
            insertIntoCompTranxDetailSD(salesInvoice, tranxType, request.getParameter("newReference"), request.getParameter("outstanding_sales_return_amt")); //for sundry Debtors : dr
            insertIntoCompTranxDetailSA(salesInvoice, tranxType, "CR", "Insert"); // for Sales Accounts : cr
            //    insertIntoTranxDetailsSalesDiscount(salesInvoice, tranxType, "DR", "Insert"); // for Sales Discount : dr
//            insertIntoCompTranxDetailRO(salesInvoice, tranxType); // for Round Off : cr or dr
            insertCompDB(salesInvoice, "AC", tranxType, "CR", "Insert"); // for Additional Charges : cr
            if (users.getOutlet().getGstApplicable())
                insertCompDB(salesInvoice, "DT", tranxType, "CR", "Insert"); // for Duties and Taxes : cr
            /*** Insert into Day Book : Reporting ***/
            saveCompIntoDayBook(salesInvoice);
            String paymentMode = request.getParameter("paymentMode");
            String salesOrderId = "";
            if (paramMap.containsKey("reference_so_id")) salesOrderId = request.getParameter("reference_so_id");
            Double paidAmt = 0.0;
            if (paramMap.containsKey("paidAmount")) paidAmt = Double.parseDouble(request.getParameter("paidAmount"));
            //createReceiptInvoice(mSalesTranx, users, salesOrderId, paidAmt);
            JsonArray paymentType = new JsonArray();
            if (paymentMode.equalsIgnoreCase("multi")) {
                String jsonStr1 = request.getParameter("payment_type");
                paymentType = new JsonParser().parse(jsonStr1).getAsJsonArray();
                Double totalPaidAmt = Double.parseDouble(request.getParameter("p_totalAmount"));
                Double returnAmt = Double.parseDouble(request.getParameter("p_returnAmount"));
                Double pendingAmt = Double.parseDouble(request.getParameter("p_pendingAmount"));

                createCompReceiptInvoice(salesInvoice, users, paymentType, salesInvoice.getTotalAmount(), returnAmt, paymentMode, "create");
                /*** set the balance amount into sales invoice after receipt voucher generated ****/
                salesInvoice.setBalance(pendingAmt);
                salesCompTransactionRepository.save(salesInvoice);
                /****** insert into payment type against sales invoice *****/

                insertIntoCompPaymentType(salesInvoice, paymentType, paymentMode, "create");
            } else if (paymentMode.equalsIgnoreCase("cash")) {
                createCompReceiptInvoice(salesInvoice, users, paymentType, salesInvoice.getTotalAmount(), 0.0, paymentMode, "create");
                /*** set the balance amount into sales invoice after receipt voucher generated ****/
                salesInvoice.setBalance(0.0);
                salesCompTransactionRepository.save(salesInvoice);
            }


            responseMessage.setResponseObject(salesInvoice.getId());
            responseMessage.setMessage("Consumer sales updated successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Duplicate Consumer Sales");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    /***** get Batch Details of Sales Invoice only for FR while converting Pur ord to Pur Invoice *****/
    public JsonObject getGVSalesBatchData(HttpServletRequest request) {
        JsonObject object = new JsonObject();
        try {
            String trackingNo = request.getParameter("trackingNo");
            String userCode = request.getParameter("usercode");
            String companyCode = request.getParameter("companyCode");
            Product mProduct = productRepository.findByProductCode(request.getParameter("productId"));
            Long productId = 0L;
            if (mProduct != null) {
                productId = mProduct.getId();
            }
            TranxSalesInvoice salesInvoice = salesTransactionRepository.findByTransactionTrackingNoAndStatus(trackingNo, true);
            TranxSalesInvoiceDetailsUnits mBatch = tranxSalesInvoiceDetailsUnitRepository.findProductBatch(mProduct.getProductCode(), true, salesInvoice.getId());
            object.addProperty("is_batch", mBatch.getProduct().getIsBatchNumber());
            object.addProperty("b_details_id", mBatch.getId());
            object.addProperty("product_name", mBatch.getProduct().getProductName());
            object.addProperty("product_code", mBatch.getProduct().getProductCode());
            object.addProperty("batch_no", mBatch.getProductBatchNo().getBatchNo());
            object.addProperty("qty", mBatch.getQty());
            object.addProperty("free_qty", mBatch.getFreeQty() != null ? mBatch.getFreeQty().toString() : "");
            object.addProperty("expiry_date", mBatch.getProductBatchNo().getExpiryDate() != null ? mBatch.getProductBatchNo().getExpiryDate().toString() : "");
            object.addProperty("purchase_rate", mBatch.getProductBatchNo().getMinRateA() != null ? mBatch.getProductBatchNo().getMinRateA() : 0.0);
            object.addProperty("sales_rate", mBatch.getProductBatchNo().getMinRateB() != null ? mBatch.getProductBatchNo().getMinRateB() : 0.0);
            object.addProperty("mrp", mBatch.getProductBatchNo().getMrp() != null ? mBatch.getProductBatchNo().getMrp() : 0.0);
            object.addProperty("min_rate_a", mBatch.getProductBatchNo().getMinRateB() != null ? mBatch.getProductBatchNo().getMinRateB() : 0.0);
            object.addProperty("min_rate_b", 0.0);
            object.addProperty("min_rate_c", 0.0);
            object.addProperty("manufacturing_date", mBatch.getProductBatchNo().getManufacturingDate() != null ? mBatch.getProductBatchNo().getManufacturingDate().toString() : "");
            object.addProperty("min_margin", 0.0);
            object.addProperty("b_rate", mBatch.getProductBatchNo().getMinRateA() != null ? mBatch.getProductBatchNo().getMinRateA() : 0.0);
            object.addProperty("costing", 0.0);
            object.addProperty("costingWithTax", 0.0);
            object.addProperty("max_discount", mBatch.getProductBatchNo().getMaxDiscount() != null ? mBatch.getProductBatchNo().getMaxDiscount() : 0.0);
            object.addProperty("min_discount", mBatch.getProductBatchNo().getMinDiscount() != null ? mBatch.getProductBatchNo().getMinDiscount() : 0.0);
            object.addProperty("dis_per", mBatch.getProductBatchNo().getDisPer() != null ? mBatch.getProductBatchNo().getDisPer() : 0.0);
            object.addProperty("dis_amt", mBatch.getProductBatchNo().getDisAmt() != null ? mBatch.getProductBatchNo().getDisAmt() : 0.0);
            object.addProperty("cess_per", 0.0);
            object.addProperty("cess_amt", 0.0);
            object.addProperty("barcode", 0.0);
            object.addProperty("pur_date", mBatch.getProductBatchNo().getPurchaseDate() != null ? mBatch.getProductBatchNo().getPurchaseDate().toString() : "");
            object.addProperty("tax_per", mBatch.getProduct().getTaxMaster().getIgst());
            object.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            object.addProperty("message", "error");
            object.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            object.addProperty("message", "error");
            object.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return object;
    }

    public JsonObject getSalesByTrackNo(HttpServletRequest request) {
        List<TranxSalesInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            String trackingNo = request.getParameter("trackingNo");
            TranxSalesInvoice salesInvoice = salesTransactionRepository.findByTransactionTrackingNoAndStatus(trackingNo, true);
            finalResult.addProperty("tcs_mode", salesInvoice.getTcsMode());
            if (salesInvoice.getTcsMode() != null && salesInvoice.getTcsMode().equalsIgnoreCase("tcs")) {
                finalResult.addProperty("tcs_per", salesInvoice.getTcs());
                finalResult.addProperty("tcs_amt", salesInvoice.getTcsAmt());
            } else if (salesInvoice.getTcsMode() != null && salesInvoice.getTcsMode().equalsIgnoreCase("tds")) {
                finalResult.addProperty("tcs_per", salesInvoice.getTdsPer());
                finalResult.addProperty("tcs_amt", salesInvoice.getTdsAmt());
            } else {
                finalResult.addProperty("tcs_amt", 0.0);
                finalResult.addProperty("tcs_per", 0.0);
            }
            finalResult.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", salesInvoice.getSalesDiscountLedger() != null ? salesInvoice.getSalesDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", salesInvoice.getSalesDiscountAmount() != null ? salesInvoice.getSalesDiscountAmount() : 0.0);
            finalResult.addProperty("discountInPer", salesInvoice.getSalesDiscountPer() != null ? salesInvoice.getSalesDiscountPer() : 0.0);
            finalResult.addProperty("totalSalesDiscountAmt", salesInvoice.getTotalSalesDiscountAmt() != null ? salesInvoice.getTotalSalesDiscountAmt() : 0.0);
            finalResult.addProperty("totalQty", salesInvoice.getTotalqty());
            finalResult.addProperty("totalFreeQty", salesInvoice.getFreeQty());
            finalResult.addProperty("grossTotal", salesInvoice.getGrossAmount() != null ? salesInvoice.getGrossAmount() : 0.0);
            finalResult.addProperty("totalTax", salesInvoice.getTotalTax() != null ? salesInvoice.getTotalTax() : 0.0);
            finalResult.addProperty("additionLedger1", salesInvoice.getAdditionLedger1() != null ? salesInvoice.getAdditionLedger1().getId() : 0);
            finalResult.addProperty("additionLedgerAmt1", salesInvoice.getAdditionLedgerAmt1() != null ? salesInvoice.getAdditionLedgerAmt1() : 0);
            finalResult.addProperty("additionLedger2", salesInvoice.getAdditionLedger2() != null ? salesInvoice.getAdditionLedger2().getId() : 0);
            finalResult.addProperty("additionLedgerAmt2", salesInvoice.getAdditionLedgerAmt2() != null ? salesInvoice.getAdditionLedgerAmt2() : 0);
            finalResult.addProperty("additionLedger3", salesInvoice.getAdditionLedger3() != null ? salesInvoice.getAdditionLedger3().getId() : 0);
            finalResult.addProperty("additionLedgerAmt3", salesInvoice.getAdditionLedgerAmt3() != null ? salesInvoice.getAdditionLedgerAmt3() : 0);
            finalResult.addProperty("totalamt", salesInvoice.getTotalAmount() != null ? salesInvoice.getTotalAmount() : 0);

            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", salesInvoice.getId());
            result.addProperty("invoice_id", salesInvoice.getId());
            result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
            result.addProperty("transaction_dt", DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).toString());
            result.addProperty("invoice_no", salesInvoice.getSalesInvoiceNo());
            result.addProperty("tranx_unique_code", salesInvoice.getTranxCode());
            result.addProperty("sales_sr_no", salesInvoice.getSalesSerialNumber());
            result.addProperty("sales_account_ledger_id", salesInvoice.getSalesAccountLedger().getId());
            result.addProperty("supplierId", salesInvoice.getSundryDebtors().getId());
            result.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            result.addProperty("total_cgst", salesInvoice.getTotalcgst());
            result.addProperty("total_sgst", salesInvoice.getTotalsgst());
            result.addProperty("total_igst", salesInvoice.getTotaligst());
            result.addProperty("total_qty", salesInvoice.getTotalqty());
            result.addProperty("taxable_amount", salesInvoice.getTaxableAmount());
            result.addProperty("tcs", salesInvoice.getTcs());
            result.addProperty("status", salesInvoice.getStatus());
            result.addProperty("financial_year", salesInvoice.getFinancialYear());
            result.addProperty("debtor_id", salesInvoice.getSundryDebtors().getId());
            result.addProperty("debtor_name", salesInvoice.getSundryDebtors().getLedgerName());
            result.addProperty("additional_charges_total", salesInvoice.getAdditionalChargesTotal());
            result.addProperty("gstNo", salesInvoice.getGstNumber());
            result.addProperty("paymentMode", salesInvoice.getPaymentMode());
            result.addProperty("salesmanId", salesInvoice.getSalesmanUser() != null ? salesInvoice.getSalesmanUser().toString() : "");
            result.addProperty("p_totalAmount", salesInvoice.getPaymentAmount() != null ? salesInvoice.getPaymentAmount() : 0);
            result.addProperty("isRoundOffCheck", salesInvoice.getIsRoundOff());
            result.addProperty("roundoff", salesInvoice.getRoundOff());
            result.addProperty("cashAmt", salesInvoice.getCash() != null ? salesInvoice.getCash() : 0.0);
            result.addProperty("ledgerStateCode", salesInvoice.getSundryDebtors().getStateCode());
            double pendingAmt = 0.0;
            if (salesInvoice.getPaymentAmount() != null)
                pendingAmt = salesInvoice.getTotalAmount() - salesInvoice.getPaymentAmount();
            if (salesInvoice.getPaymentAmount() != null && pendingAmt > 0) {
                result.addProperty("p_pendingAmount", pendingAmt);
            } else {
                result.addProperty("p_pendingAmount", 0);
            }
            double returnAmt = 0.0;
            if (salesInvoice.getPaymentAmount() != null && salesInvoice.getPaymentAmount() > salesInvoice.getTotalAmount()) {
                returnAmt = salesInvoice.getPaymentAmount() - salesInvoice.getTotalAmount();
                result.addProperty("p_returnAmount", returnAmt);
            } else {
                result.addProperty("p_returnAmount", 0);
            }
            /* End of Sales invoice Data */
            /* Sales invoice Details */
            JsonArray row = new JsonArray();
            List<TranxSalesInvoiceDetailsUnits> unitsArray = tranxSalesInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(salesInvoice.getId(), true);
            for (TranxSalesInvoiceDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_code", mUnits.getProduct().getProductCode());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelAId() != null ? mUnits.getLevelAId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelBId() != null ? mUnits.getLevelBId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelCId() != null ? mUnits.getLevelCId().toString() : "");
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
                        if (DateConvertUtil.convertDateToLocalDate(salesInvoice.getBillDate()).isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
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
                    unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA());
                    unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB());
                    unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC());
                    unitsJsonObjects.addProperty("mrp", mUnits.getProductBatchNo().getMrp());
                    unitsJsonObjects.addProperty("min_discount",
                            mUnits.getProductBatchNo().getMinDiscount() != null ? mUnits.getProductBatchNo().getMinDiscount() : 0.0);
                    unitsJsonObjects.addProperty("max_discount",
                            mUnits.getProductBatchNo().getMaxDiscount() != null ? mUnits.getProductBatchNo().getMaxDiscount() : 0.0);
                    unitsJsonObjects.addProperty("manufacturing_date", mUnits.getProductBatchNo().getManufacturingDate() != null ? mUnits.getProductBatchNo().getManufacturingDate().toString() : "");
                    unitsJsonObjects.addProperty("min_margin", mUnits.getProductBatchNo().getMinMargin() != null ? mUnits.getProductBatchNo().getMinMargin() : 0.0);
                    unitsJsonObjects.addProperty("b_rate", mUnits.getProductBatchNo().getMrp() != null ? mUnits.getProductBatchNo().getMrp() : 0.0);
                    unitsJsonObjects.addProperty("sales_rate", mUnits.getProductBatchNo().getSalesRate() != null ? mUnits.getProductBatchNo().getSalesRate() : 0.0);
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

            /* Purchase Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            additionalCharges = salesAdditionalChargesRepository.findBySalesTransactionIdAndStatus(salesInvoice.getId(), true);
            if (additionalCharges.size() > 0) {
                for (TranxSalesInvoiceAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledgerId", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    json_charges.addProperty("percent", mAdditionalCharges.getPercent() != null ? mAdditionalCharges.getPercent() : 0.0);
                    jsonAdditionalList.add(json_charges);
                }
            }
            /* End of Sales Quotations Details */
            /**** Tranx Sales Payment Type *****/
            JsonArray paymentType = new JsonArray();
            List<TranxSalesPaymentType> paymentArray = transSalesPaymentTypeRepository.findPaymentModes(salesInvoice.getId(), true);
            if (paymentArray != null && paymentArray.size() > 0) {
                for (TranxSalesPaymentType mPayment : paymentArray) {
                    JsonObject paymentJsonObjects = new JsonObject();
                    LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(mPayment.getType()), true);
                    paymentJsonObjects.addProperty("bank_name", mLedger.getLedgerName());
                    paymentJsonObjects.addProperty("bank_Id", mLedger.getId());
                    List<TranxSalesPaymentType> salesTypes = transSalesPaymentTypeRepository.findByTranxSalesInvoiceIdAndStatusAndType(salesInvoice.getId(), true, mPayment.getType());
                    JsonArray mArray = new JsonArray();
                    for (TranxSalesPaymentType mPaymentTypes : salesTypes) {
                        JsonObject mObject = new JsonObject();
                        mObject.addProperty("details_id", mPaymentTypes.getId());
                        mObject.addProperty("modeId", mPaymentTypes.getPaymentMasterId());
                        mObject.addProperty("l0.abel", mPaymentTypes.getPaymentMode());
                        mObject.addProperty("amount", mPaymentTypes.getPaymentAmount());
                        mObject.addProperty("refId", mPaymentTypes.getReferenceId() != null ? mPaymentTypes.getReferenceId() : "");
                        mObject.addProperty("custBank", mPaymentTypes.getCustomerBank() != null ? mPaymentTypes.getCustomerBank() : "");
                        mArray.add(mObject);
                    }
                    paymentJsonObjects.add("payment_modes", mArray);
                    paymentType.add(paymentJsonObjects);
                }
            }
            finalResult.add("row", row);
            finalResult.add("invoice_data", result);
            finalResult.add("payment_type", paymentType);
            finalResult.add("additionalCharges", jsonAdditionalList);
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e.getMessage());
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesInvoiceLogger.error("Error in getSalesInvoiceByIdNew" + e1.getMessage());

            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }
}
