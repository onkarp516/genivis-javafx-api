package in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.puarchasedto.PurInvoiceDTO;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageService;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventoryDetailsPostings;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerBalanceSummaryRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerOpeningClosingDetailRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.product_barcode.ProductBarcodeRepository;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentPerticularsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.*;
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
import org.springframework.scheduling.annotation.Async;
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
import javax.transaction.Transactional;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

@Service
public class TranxPurInvoiceService {

    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private TranxPurOrderRepository tranxPurOrderRepository;
    @Autowired
    private TranxPurChallanRepository tranxPurChallanRepository;
    @Autowired
    private PurInvoiceDutiesTaxesRepository purInvoiceDutiesTaxesRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PurchaseInvoiceDetailsRepository invoiceDetailsRepository;
    @Autowired
    private PurchaseInvoiceProductSrNumberRepository serialNumberRepository;
    @Autowired
    private PurInvoiceAdditionalChargesRepository purInvoiceAdditionalChargesRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private TranxDebitNoteNewReferenceRepository tranxDebitNoteNewReferenceRepository;
    @Autowired
    private TranxDebitNoteDetailsRepository tranxDebitNoteDetailsRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private TranxPurInvoiceDetailsUnitsRepository tranxPurInvoiceUnitsRepository;
    @Autowired
    private PackingMasterRepository packingMasterRepository;
    @Autowired
    private ProductBarcodeRepository barcodeRepository;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    private FlavourMasterRepository flavourMasterRepository;
    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private ProductData productData;
    @Autowired
    private ProductTaxDateMasterRepository productTaxDateMasterRepository;
    @Autowired
    private TranxPurOrderDetailsUnitRepository tranxPurOrderDetailsUnitRepository;
    @Autowired
    private TranxPurChallanDetailsUnitRepository tranxPurChallanDetailsUnitRepository;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    @Autowired
    private PostingUtility postingUtility;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private TranxPurReturnsRepository tranxPurReturnsRepository;
    @Autowired
    private TranxPurReturnDutiesTaxesRepository tranxPurReturnDutiesTaxesRepository;
    @Autowired
    private TranxPurReturnAddChargesRepository tranxPurReturnAddChargesRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private NumFormat numFormat;
    @Autowired
    private StockTranxDetailsRepository stkTranxDetailsRepository;
    @Autowired
    private ClosingUtility closingUtility;
    private static final Logger purInvoiceLogger = LogManager.getLogger(TranxPurInvoiceService.class);
    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice from DB
    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice from DB
    List<Long> mInputList = new ArrayList<>(); // input all ledgers Id against Purchase invoice from request
    List<Long> ledgerInputList = new ArrayList<>(); // input all ledgers Id against Purchase invoice from request
    @Autowired
    private TranxPurReturnDetailsUnitRepository tranxPurReturnDetailsUnitRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;

    @Autowired
    private LedgerMasterRepository ledgerRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private TranxPaymentMasterRepository tranxPaymentMasterRepository;
    @Autowired
    private TranxPaymentPerticularsRepository tranxPaymentPerticularRepository;
    @Autowired
    private TranxPaymentPerticularsDetailsRepository tranxPaymentPerticularsDetailsRepository;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Autowired
    private FranchiseMasterRepository franchiseMasterRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${spring.serversource.frurl}")
    private String frUrl;

    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;
    @Autowired
    private LedgerBalanceSummaryRepository ledgerBalanceSummaryRepository;
    @Autowired
    private UnitConversion unitConversion;


    public Object insertPurchaseInvoices(MultipartHttpServletRequest request) {
        TranxPurInvoice mPurchaseTranx = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseMessage = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            mPurchaseTranx = saveIntoPurchaseInvoice(request);
            if (mPurchaseTranx != null) {
                // Accounting Postings
                insertIntoLedgerTranxDetails(mPurchaseTranx, request);
                saveIntoDayBook(mPurchaseTranx);
                if (paramMap.containsKey("paymentMode")) {
                    String paymentMode = request.getParameter("paymentMode");
                    if (paymentMode.equalsIgnoreCase("cash")) {
                        createPaymentInvoice(mPurchaseTranx, users, paymentMode, mPurchaseTranx.getTotalAmount(),
                                0.0, paymentMode, "create");
                        /*** set the balance amount into sales invoice after receipt voucher generated ****/
                        mPurchaseTranx.setBalance(0.0);
                        tranxPurInvoiceRepository.save(mPurchaseTranx);
                    }
                }
                responseMessage.setMessage("Purchase invoice created successfully");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
                /**
                 * @implNote validation of Ledger Delete , if any tranx done for this ledger, user cant delete this ledger **
                 * @auther ashwins@opethic.com
                 * @version sprint 21
                 **/
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(mPurchaseTranx.getSundryCreditors().getId(), true);
                ledgerMaster.setIsDeleted(false);
                ledgerMasterRepository.save(ledgerMaster);

                // updateFsrCsrAtFranchise(request.getParameter("row"));
            } else {
                responseMessage.setMessage("Error in purchase invoice creation");
                responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Error in Purchase Creation:" + exceptionAsString);
        }
        return responseMessage;
    }

    @Async
    public void updateFsrCsrAtFranchise(String mRows) {

        List<FranchiseMaster> franchiseMasters = franchiseMasterRepository.findByStatus(true);
        if (franchiseMasters != null) {
            System.out.println("Franchise Size : " + franchiseMasters.size());
            for (FranchiseMaster franchiseMaster : franchiseMasters) {
                System.out.println("Franchise : " + franchiseMaster.getFranchiseCode());
                JsonArray rows = new Gson().fromJson(mRows, JsonArray.class);
                HttpHeaders frHdr = new HttpHeaders();
                frHdr.setContentType(MediaType.MULTIPART_FORM_DATA);
                frHdr.add("branch", franchiseMaster.getFranchiseCode());

                for (JsonElement row :
                        rows) {
                    JsonObject rowInfo = row.getAsJsonObject();
                    Product mProduct = productRepository.findByIdAndStatus(rowInfo.get("productId").getAsLong(), true);
                    if (mProduct.getIsGVProducts()) {
                        Double fsr = 0D;
                        Double csr = 0D;
                        Double rate3 = 0D;
                        String productCode = mProduct.getProductCode();
                        String productName = mProduct.getProductName();

                        if (rowInfo.has("rate_a")) {
                            fsr = rowInfo.get("rate_a").getAsDouble();
                        }
                        if (rowInfo.has("rate_b")) {
                            csr = rowInfo.get("rate_b").getAsDouble();
                        }

                        LinkedMultiValueMap body = new LinkedMultiValueMap();
                        body.add("productCode", productCode);
                        body.add("productName", productName);
                        body.add("fsr", fsr);
                        body.add("csr", csr);


                        HttpEntity frEntity = new HttpEntity<>(body, frHdr);

                        String resData = restTemplate.exchange(
                                frUrl + "/update_fsr_csr", HttpMethod.POST, frEntity, String.class).getBody();
                        System.out.println("frUpdateProductResponse => " + resData);
                    }
                }

            }
        }

    }

    private void createPaymentInvoice(TranxPurInvoice newInvoice, Users users, String paymentType, Double totalPaidAmt, Double returnAmt, String paymentMode, String key) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PMT");
        TranxPaymentMaster tranxPaymentMaster = new TranxPaymentMaster();
        Long count = 0L;
        if (newInvoice.getBranch() != null) {
            count = tranxPaymentMasterRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxPaymentMasterRepository.findLastRecord(users.getOutlet().getId());
        }
        tranxPaymentMaster.setStatus(true);
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String receiptCode = "PMT" + currentMonth + serailNo;
        tranxPaymentMaster.setPaymentNo(receiptCode);
        tranxPaymentMaster.setPaymentSrNo(Double.parseDouble(serailNo));
        if (newInvoice.getBranch() != null)
            tranxPaymentMaster.setBranch(newInvoice.getBranch());
        tranxPaymentMaster.setOutlet(newInvoice.getOutlet());
        tranxPaymentMaster.setTranxId(newInvoice.getId());
         /*else {
            tranxReceiptMaster = tranxReceiptMasterRepository.findByTranxSalesInvoiceIdAndStatus(newInvoice.getId(), true);
        }*/
        if (newInvoice.getFiscalYear() != null) tranxPaymentMaster.setFiscalYear(newInvoice.getFiscalYear());
        tranxPaymentMaster.setTranscationDate(newInvoice.getInvoiceDate());
        tranxPaymentMaster.setTotalAmt(totalPaidAmt);
        tranxPaymentMaster.setCreatedBy(newInvoice.getCreatedBy());
        // tranxPaymentMaster.set(returnAmt);
        TranxPaymentMaster newTranxPaymentMaster = tranxPaymentMasterRepository.save(tranxPaymentMaster);
        LedgerMaster sundryDebtors = newInvoice.getSundryCreditors();
        if (key.equalsIgnoreCase("create")) {
            insertIntoPaymentPerticualrs(newTranxPaymentMaster, sundryDebtors, "SC", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
            /**** Sales Invoice by Cash or Bank : Receipt Details of Cash Account or Bank account *****/
            if (paymentType != null) {
//                for (JsonElement mList : paymentType) {
//                    JsonObject object = mList.getAsJsonObject();
//                    String payMode = "";
//                    if (object.get("type").getAsString().equals("others")) payMode = "Cash";
//                    else {
//                        payMode = "Bank Account";
//                    }
//                    LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
//                    insertIntoPaymentPerticualrs(newTranxPaymentMaster, ledgerMaster, object.get("type").getAsString(), object.get("amount").getAsDouble(), key, payMode, tranxType, newInvoice);
//                }
            }
            /***** Sales Invoice by Cash Only : Receipt Details of Cash Account only  ******/
            else {
                LedgerMaster ledgerMaster = null;
                if (newInvoice.getBranch() != null) {
                    ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndBranchIdAndStatus("CAIH", newInvoice.getOutlet().getId(), newInvoice.getBranch().getId(), true);
                } else {
                    ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull("CAIH", newInvoice.getOutlet().getId(), true);
                }
                insertIntoPaymentPerticualrs(newTranxPaymentMaster, ledgerMaster, "others", totalPaidAmt, key, "cash", tranxType, newInvoice);
            }
        } /*else {
            updateIntoReceiptPerticualrs(newTranxReceiptMaster,
                    sundryDebtors, "SD", totalPaidAmt, key, paymentMode, tranxType, newInvoice);
        }*/
    }

    private void insertIntoPaymentPerticualrs(TranxPaymentMaster newTranxPaymentMaster, LedgerMaster ledgerMaster, String type, double amount, String key, String payMode, TransactionTypeMaster tranxType, TranxPurInvoice newInvoice) {
        TranxPaymentPerticulars tranxPaymentPerticulars = null;
        tranxPaymentPerticulars = new TranxPaymentPerticulars();
        String ledgerType = "";
        tranxPaymentPerticulars.setStatus(true);
        tranxPaymentPerticulars.setBranch(newTranxPaymentMaster.getBranch());
        tranxPaymentPerticulars.setOutlet(newTranxPaymentMaster.getOutlet());
        tranxPaymentPerticulars.setLedgerMaster(ledgerMaster);
        tranxPaymentPerticulars.setTranxPaymentMaster(newTranxPaymentMaster);
        tranxPaymentPerticulars.setLedgerType(type);
        tranxPaymentPerticulars.setLedgerName(ledgerMaster.getLedgerName());
        if (type.equalsIgnoreCase("SC")) {
            ledgerType = "DR";
            tranxPaymentPerticulars.setDr(amount);
            tranxPaymentPerticulars.setCr(0.0);
            tranxPaymentPerticulars.setType("dr");
            tranxPaymentPerticulars.setPayableAmt(amount);
            tranxPaymentPerticulars.setSelectedAmt(amount);
            tranxPaymentPerticulars.setRemainingAmt(newInvoice.getTotalAmount() - amount);
            tranxPaymentPerticulars.setIsAdvance(false);
        } else {
            ledgerType = "CR";
            tranxPaymentPerticulars.setDr(0.0);
            tranxPaymentPerticulars.setCr(amount);
            tranxPaymentPerticulars.setType("cr");
        }
        tranxPaymentPerticulars.setPaymentMethod(payMode);
        tranxPaymentPerticulars.setTransactionDate(DateConvertUtil.convertDateToLocalDate(newTranxPaymentMaster.getTranscationDate()));
        tranxPaymentPerticulars.setCreatedBy(newTranxPaymentMaster.getCreatedBy());
        TranxPaymentPerticulars paymentPerticulars = tranxPaymentPerticularRepository.save(tranxPaymentPerticulars);
        /*** Insert into Bill Details of Receipt: receipt against the sales invoice *****/
        if (type.equalsIgnoreCase("SC")) {
            InsertIntoBillDetails(paymentPerticulars, key, newInvoice);
        }
      /*  ledgerCommonPostings.callToPostings(amount, ledgerMaster, tranxType, ledgerMaster.getAssociateGroups(), newTranxPaymentMaster.getFiscalYear(), newTranxPaymentMaster.getBranch(), newTranxPaymentMaster.getOutlet(),
                newTranxPaymentMaster.getTranscationDate(), newTranxPaymentMaster.getId(), newTranxPaymentMaster.getPaymentNo(), ledgerType, true, tranxType.getTransactionCode(), "Insert");
*/
        ledgerCommonPostings.callToPostingsTranxCode(amount, ledgerMaster, tranxType, ledgerMaster.getAssociateGroups(), newTranxPaymentMaster.getFiscalYear(), newTranxPaymentMaster.getBranch(), newTranxPaymentMaster.getOutlet(),
                newTranxPaymentMaster.getTranscationDate(), newTranxPaymentMaster.getId(), newTranxPaymentMaster.getPaymentNo(), ledgerType, true, tranxType.getTransactionCode(), "Insert", newTranxPaymentMaster.getTranxCode());

        /**** NEW METHOD FOR LEDGER POSTING ****/
        postingUtility.callToPostingLedger(tranxType, type, amount, newTranxPaymentMaster.getFiscalYear(), ledgerMaster,
                newTranxPaymentMaster.getTranscationDate(), newTranxPaymentMaster.getId(), newTranxPaymentMaster.getOutlet(),
                newTranxPaymentMaster.getBranch(), newTranxPaymentMaster.getTranxCode());
    }

    private void InsertIntoBillDetails(TranxPaymentPerticulars paymentPerticulars, String key, TranxPurInvoice newInvoice) {
        TranxPaymentPerticularsDetails tranxPaymentDetails = new TranxPaymentPerticularsDetails();
        if (key.equalsIgnoreCase("update")) {
            tranxPaymentDetails = tranxPaymentPerticularsDetailsRepository.findByStatusAndTranxPaymentPerticularsId(true, paymentPerticulars.getId());
        }
        if (key.equalsIgnoreCase("create")) {
            tranxPaymentDetails.setBranch(paymentPerticulars.getBranch());
            tranxPaymentDetails.setOutlet(paymentPerticulars.getOutlet());
            tranxPaymentDetails.setStatus(true);
            tranxPaymentDetails.setCreatedBy(paymentPerticulars.getCreatedBy());
            tranxPaymentDetails.setType("purchase_invoice");
        }
        tranxPaymentDetails.setLedgerMaster(paymentPerticulars.getLedgerMaster());
        tranxPaymentDetails.setTranxPaymentMaster(paymentPerticulars.getTranxPaymentMaster());
        tranxPaymentDetails.setTranxPaymentPerticulars(paymentPerticulars);
        tranxPaymentDetails.setTotalAmt(paymentPerticulars.getCr());
        tranxPaymentDetails.setTransactionDate(paymentPerticulars.getTransactionDate());
        tranxPaymentDetails.setAmount(paymentPerticulars.getCr());
        tranxPaymentDetails.setPaidAmt(paymentPerticulars.getCr());
        tranxPaymentDetails.setTranxNo(newInvoice.getVendorInvoiceNo());
        tranxPaymentDetails.setTranxInvoiceId(newInvoice.getId());
        tranxPaymentDetails.setRemainingAmt(newInvoice.getTotalAmount() - paymentPerticulars.getCr());
        tranxPaymentPerticularsDetailsRepository.save(tranxPaymentDetails);
    }


    private void insertIntoLedgerTranxDetails(TranxPurInvoice mPurchaseTranx, HttpServletRequest request) {
        /* start of ledger trasaction details */
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRS");
        try {
            insertIntoTranxDetailSC(mPurchaseTranx, tranxType, "CR", "Insert");// for Sundry Creditors : cr
            insertIntoTranxDetailPA(mPurchaseTranx, tranxType, "DR", "Insert"); // for Purchase Accounts : dr
            //     insertIntoTranxDetailPD(mPurchaseTranx, tranxType, "CR", "Insert"); // for Purchase Discounts : cr
            insertIntoTranxDetailRO(mPurchaseTranx, tranxType); // for Round Off : cr or dr
            //   insertDB(mPurchaseTranx, "AC", tranxType, "DR", "Insert"); // for Additional Charges : dr
            insertDB(mPurchaseTranx, "DT", tranxType, "DR", "Insert"); // for Duties and Taxes : dr
            if (mPurchaseTranx.getTcsMode().equalsIgnoreCase("tcs"))
                insertIntoTCS(mPurchaseTranx, "TCS", tranxType, "DR", "Insert");
            else insertIntoTCS(mPurchaseTranx, "TDS", tranxType, "DR", "Insert");
            /* end of ledger transaction details */
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertIntoLedgerTranxDetails(method) :" + exceptionAsString);
        }
    }

    private void insertIntoTCS(TranxPurInvoice mPurchaseTranx, String type, TransactionTypeMaster tranxType, String crdr, String operation) {
        try {
            /**** New Postings Logic *****/
            LedgerMaster mLedger = null;
            if (type.equalsIgnoreCase("tcs")) {
                if (mPurchaseTranx.getBranch() != null)
                    mLedger = ledgerMasterRepository.findByLedgerCodeAndStatusAndOutletIdAndBranchId("tcs", true, mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId());
                else {
                    mLedger = ledgerMasterRepository.findByLedgerCodeAndStatusAndOutletIdAndBranchIsNull("tcs", true, mPurchaseTranx.getOutlet().getId());
                }
            } else if (type.equalsIgnoreCase("tds")) {
                if (mPurchaseTranx.getBranch() != null)
                    mLedger = ledgerMasterRepository.findByLedgerCodeAndStatusAndOutletIdAndBranchId("tds", true, mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId());
                else {
                    mLedger = ledgerMasterRepository.findByLedgerCodeAndStatusAndOutletIdAndBranchIsNull("tds", true, mPurchaseTranx.getOutlet().getId());
                }
            }
            ledgerCommonPostings.callToPostingsTranxCode(mPurchaseTranx.getTcsAmt(), mLedger, tranxType, mLedger.getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), crdr, true, tranxType.getTransactionCode(), operation, mPurchaseTranx.getTranxCode());

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertFromDutiesTaxes(method) :" + exceptionAsString);
        }
    }


    private void deleteIntoLedgerTranxDetails(TranxPurInvoice mPurchaseTranx) {
        /* start of ledger trasaction details */
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRS");
//        generateTransactions.insertIntoTranxsDetails(mPurchaseTranx,tranxType);
        try {

            insertIntoTranxDetailSC(mPurchaseTranx, tranxType, "DR", "Delete");// for Sundry Creditors : dr
            insertIntoTranxDetailPA(mPurchaseTranx, tranxType, "CR", "Delete"); // for Purchase Accounts : cr
            //  insertIntoTranxDetailPD(mPurchaseTranx, tranxType, "DR", "Delete"); // for Purchase Discounts : dr
            deleteIntoTranxDetailRO(mPurchaseTranx, tranxType); // for Round Off : cr or dr
            insertDB(mPurchaseTranx, "AC", tranxType, "CR", "Delete"); // for Additional Charges : cr
            insertDB(mPurchaseTranx, "DT", tranxType, "CR", "Delete"); // for Duties and Taxes : cr
            /* end of ledger transaction details */
            /**** make Status 0 to postings of corresponding ledgers , Its is used when we are displyaing ledger details list ****/
            List<LedgerTransactionPostings> mList = new ArrayList<>();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertIntoLedgerTranxDetails(method):" + exceptionAsString);
        }
    }

    private void deleteIntoLedgerTranxDetailsDebitNote(TranxPurReturnInvoice mPurchaseTranx) {
        /* start of ledger trasaction details */
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
        try {

            insertIntoTranxDetailSC(mPurchaseTranx, tranxType, "CR", "Delete");// for Sundry Creditors : CR
            insertIntoTranxDetailPA(mPurchaseTranx, tranxType, "DR", "Delete"); // for Purchase Accounts : DR
            insertIntoTranxDetailPD(mPurchaseTranx, tranxType, "CR", "Delete"); // for Purchase Discounts : CR
            deleteIntoTranxDetailRODebitNote(mPurchaseTranx, tranxType); // for Round Off : cr or dr
            insertDB(mPurchaseTranx, "AC", tranxType, "DR", "Delete"); // for Additional Charges : DR
            insertDB(mPurchaseTranx, "DT", tranxType, "DR", "Delete"); // for Duties and Taxes : DR
            /* end of ledger transaction details */

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->deleteIntoLedgerTranxDetailsDebitNote(method) :" + exceptionAsString);

        }
    }

    /*** Purchas Return  ***/
    public void insertDB(TranxPurReturnInvoice mPurchaseTranx, String ledgerName, TransactionTypeMaster tranxType, String crdrType, String operation) {

        /* Purchase Duties Taxes */
        if (ledgerName.equalsIgnoreCase("DT")) {
            List<TranxPurReturnInvoiceDutiesTaxes> list = new ArrayList<>();
            list = tranxPurReturnDutiesTaxesRepository.findByPurReturnInvoiceAndStatus(mPurchaseTranx, true);
            if (list.size() > 0) {
                for (TranxPurReturnInvoiceDutiesTaxes mDuties : list) {
                    insertFromDutiesTaxes(mDuties, mPurchaseTranx, tranxType, crdrType, operation);
                }
            }
        } else if (ledgerName.equalsIgnoreCase("AC")) {
            /* Purchase Additional Charges */
            List<TranxPurReturnInvoiceAddCharges> list = new ArrayList<>();
            list = tranxPurReturnAddChargesRepository.findByPurReturnInvoiceIdAndStatus(mPurchaseTranx.getId(), true);
            if (list.size() > 0) {
                for (TranxPurReturnInvoiceAddCharges mAdditinoalCharges : list) {
                    insertFromAdditionalCharges(mAdditinoalCharges, mPurchaseTranx, tranxType, crdrType, operation);
                }
            }
        }
    }

    public void insertFromDutiesTaxes(TranxPurReturnInvoiceDutiesTaxes mDuties, TranxPurReturnInvoice mPurchaseTranx,
                                      TransactionTypeMaster tranxType, String crdrType, String operation) {
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mDuties.getAmount(), mDuties.getDutiesTaxes(), tranxType,
                mDuties.getDutiesTaxes().getAssociateGroups(), mPurchaseTranx.getFiscalYear(),
                mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(),
                mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), crdrType, true, "Purchase Return", operation);


        if (operation.equalsIgnoreCase("delete")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                    mDuties.getDutiesTaxes().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
            if (ledgerDetail != null) {
                Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                ledgerDetail.setAmount(0.0);
                ledgerDetail.setClosingAmount(closing);
                ledgerDetail.setStatus(false);
                LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.updateLedgerPostings(mDuties.getDutiesTaxes(), mPurchaseTranx.getPurReturnDate(),
                        tranxType, mPurchaseTranx.getFiscalYear(), detail);
            }
        }
    }

    public void insertFromAdditionalCharges(TranxPurReturnInvoiceAddCharges mAdditinoalCharges, TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mAdditinoalCharges.getAmount(), mAdditinoalCharges.getAdditionalCharges(), tranxType, mAdditinoalCharges.getAdditionalCharges().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), crdrType, true, "Purchase Return", operation);


        if (operation.equalsIgnoreCase("delete")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                    mAdditinoalCharges.getAdditionalCharges().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
            if (ledgerDetail != null) {
                Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                ledgerDetail.setAmount(0.0);
                ledgerDetail.setClosingAmount(closing);
                ledgerDetail.setStatus(false);
                LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.updateLedgerPostings(mAdditinoalCharges.getAdditionalCharges(), mPurchaseTranx.getPurReturnDate(),
                        tranxType, mPurchaseTranx.getFiscalYear(), detail);
            }
        }

    }

    /* Insertion into Transaction Details Table of Sundry Creditors Ledgers for Purchase Invoice */
    public void insertIntoTranxDetailSC(TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        try {

            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mPurchaseTranx.getTotalAmount(),
                    mPurchaseTranx.getSundryCreditors(), tranxType,
                    mPurchaseTranx.getSundryCreditors().getAssociateGroups(),
                    mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(),
                    mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), crdrType, true, "Purchase Return", operation);

            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mPurchaseTranx.getSundryCreditors().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mPurchaseTranx.getSundryCreditors(), mPurchaseTranx.getPurReturnDate(),
                            tranxType, mPurchaseTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception in insertIntoTranxDetailSC:" + exceptionAsString);

        }
    }

    /* Insertion into Transaction Details Table of Purchase Accounts Ledgers for Purchase Invoice*/
    public void insertIntoTranxDetailPA(TranxPurReturnInvoice mPurchaseTranx,
                                        TransactionTypeMaster tranxType, String crdrType, String operation) {
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mPurchaseTranx.getTotalBaseAmount(),
                mPurchaseTranx.getPurchaseAccountLedger(), tranxType, mPurchaseTranx.getPurchaseAccountLedger().getAssociateGroups(),
                mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(),
                mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(),
                mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(),
                crdrType, true, "Purchase Return", operation);


        if (operation.equalsIgnoreCase("delete")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                    mPurchaseTranx.getPurchaseAccountLedger().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
            if (ledgerDetail != null) {
                Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                ledgerDetail.setAmount(0.0);
                ledgerDetail.setClosingAmount(closing);
                ledgerDetail.setStatus(false);
                LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.updateLedgerPostings(mPurchaseTranx.getPurchaseAccountLedger(), mPurchaseTranx.getPurReturnDate(),
                        tranxType, mPurchaseTranx.getFiscalYear(), detail);
            }
        }
    }

    /* Insertion into Transaction Details Table of Purchase Discount Ledgers for Purchase Invoice*/
    public void insertIntoTranxDetailPD(TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType,
                                        String crdrTyep, String operation) {
        try {
            if (mPurchaseTranx.getPurchaseDiscountLedger() != null) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(mPurchaseTranx.getTotalPurchaseDiscountAmt(),
                        mPurchaseTranx.getPurchaseDiscountLedger(), tranxType,
                        mPurchaseTranx.getPurchaseDiscountLedger().getAssociateGroups(),
                        mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(),
                        mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(),
                        mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), crdrTyep, true,
                        "Purchase Return", operation);


                if (operation.equalsIgnoreCase("delete")) {
                    /**** NEW METHOD FOR LEDGER POSTING ****/
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mPurchaseTranx.getPurchaseDiscountLedger().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(mPurchaseTranx.getPurchaseDiscountLedger(), mPurchaseTranx.getPurReturnDate(),
                                tranxType, mPurchaseTranx.getFiscalYear(), detail);
                    }
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception in insertIntoTranxDetailPD:" + exceptionAsString);

        }
    }

    /* Insertion into Transaction Details Table of Sundry Creditors Ledgers for Purchase Invoice */
    public void insertIntoTranxDetailSC(TranxPurInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String type, String operation) {
        try {
            /**** New Postings Logic *****/
            Double amt = 0.0;
            if (operation.equalsIgnoreCase("Insert")) amt = mPurchaseTranx.getBalance();
            else amt = mPurchaseTranx.getTotalAmount();
//            ledgerCommonPostings.callToPostings(amt, mPurchaseTranx.getSundryCreditors(), tranxType, mPurchaseTranx.getSundryCreditors().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), type, true, tranxType.getTransactionCode(), operation);
            ledgerCommonPostings.callToPostingsTranxCode(amt, mPurchaseTranx.getSundryCreditors(), tranxType, mPurchaseTranx.getSundryCreditors().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), type, true, tranxType.getTransactionCode(), operation, mPurchaseTranx.getTranxCode());

            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, type, amt, mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getSundryCreditors(),
                        mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(), mPurchaseTranx.getTranxCode());
            }
            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mPurchaseTranx.getSundryCreditors().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mPurchaseTranx.getSundryCreditors(), mPurchaseTranx.getInvoiceDate(),
                            tranxType, mPurchaseTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertIntoTranxDetailSC(method) :" + exceptionAsString);

        }
    }

    /* Deletion of Transaction Details Table of Purchase RoundOff Ledgers for Purchase Invoice*/
    private void deleteIntoTranxDetailRO(TranxPurInvoice mPurchaseTranx, TransactionTypeMaster tranxType) {
        try {
            if (mPurchaseTranx.getRoundOff() >= 0) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), "CR", true, "Purchase Invoice", "Delete");
            } else if (mPurchaseTranx.getRoundOff() < 0) {
                ledgerCommonPostings.callToPostings(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Delete");
            }

            String operation = "Delete";
            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mPurchaseTranx.getPurchaseRoundOff().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mPurchaseTranx.getPurchaseRoundOff(), mPurchaseTranx.getInvoiceDate(),
                            tranxType, mPurchaseTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertIntoTranxDetailRO(method) :" + exceptionAsString);

        }
    }

    private void deleteIntoTranxDetailRODebitNote(TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType) {
        try {
            if (mPurchaseTranx.getRoundOff() >= 0) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), "DR", true, "Purchase Invoice", "Delete");
            } else if (mPurchaseTranx.getRoundOff() < 0) {
                ledgerCommonPostings.callToPostings(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), "CR", true, "Purchase Invoice", "Delete");
            }

            String operation = "Delete";
            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mPurchaseTranx.getPurchaseRoundOff().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mPurchaseTranx.getPurchaseRoundOff(), mPurchaseTranx.getPurReturnDate(),
                            tranxType, mPurchaseTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertIntoTranxDetailRO(method) :" + exceptionAsString);

        }
    }


    /* Insertion into Transaction Details Table of Purchase Accounts Ledgers for Purchase Invoice*/
    public void insertIntoTranxDetailPA(TranxPurInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String type, String operation) {
        try {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostingsTranxCode(mPurchaseTranx.getTaxableAmount(), mPurchaseTranx.getPurchaseAccountLedger(), tranxType, mPurchaseTranx.getPurchaseAccountLedger().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), type, true, tranxType.getTransactionCode(), operation, mPurchaseTranx.getTranxCode());
            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, type, mPurchaseTranx.getTaxableAmount(),
                        mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getPurchaseAccountLedger(), mPurchaseTranx.getInvoiceDate(),
                        mPurchaseTranx.getId(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(), mPurchaseTranx.getTranxCode());
            }

            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail =
                        ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                                mPurchaseTranx.getPurchaseAccountLedger().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mPurchaseTranx.getPurchaseAccountLedger(), mPurchaseTranx.getInvoiceDate(),
                            tranxType, mPurchaseTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertIntoTranxDetailPA(method) :" + exceptionAsString);

        }
    }


    /* Insertion into Transaction Details Table of Purchase Discount Ledgers for Purchase Invoice*/
    public void insertIntoTranxDetailPD(TranxPurInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String cr, String operation) {
        try {
            if (mPurchaseTranx.getPurchaseDiscountLedger() != null) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(mPurchaseTranx.getPurchaseDiscountAmount(), mPurchaseTranx.getPurchaseDiscountLedger(), tranxType, mPurchaseTranx.getPurchaseDiscountLedger().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), cr, true, tranxType.getTransactionCode(), operation);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertIntoTranxDetailPD(method) :" + exceptionAsString);

        }
    }

    /* Insertion into Transaction Details Table of Purchase RoundOff Ledgers for Purchase Invoice*/
    private void insertIntoTranxDetailRO(TranxPurInvoice mPurchaseTranx, TransactionTypeMaster tranxType) {
        try {
            String tranxAction = "CR";
            if (mPurchaseTranx.getRoundOff() >= 0) {
                tranxAction = "DR";
                /**** New Postings Logic *****/
//                ledgerCommonPostings.callToPostings(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), "DR", true, tranxType.getTransactionCode(), "Insert");
                ledgerCommonPostings.callToPostingsTranxCode(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(),
                        tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(),
                        mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(),
                        mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), "DR", true,
                        tranxType.getTransactionCode(), "Insert", mPurchaseTranx.getTranxCode());
            } else if (mPurchaseTranx.getRoundOff() < 0) {
//                ledgerCommonPostings.callToPostings(Math.abs(mPurchaseTranx.getRoundOff()), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
                ledgerCommonPostings.callToPostingsTranxCode(Math.abs(mPurchaseTranx.getRoundOff()), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert", mPurchaseTranx.getTranxCode());
            }

            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedger(tranxType, tranxAction, Math.abs(mPurchaseTranx.getRoundOff()),
                    mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getPurchaseRoundOff(), mPurchaseTranx.getInvoiceDate(),
                    mPurchaseTranx.getId(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(), mPurchaseTranx.getTranxCode());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertIntoTranxDetailRO(method) :" + exceptionAsString);

        }
    }

    public void insertDB(TranxPurInvoice mPurchaseTranx, String ledgerName, TransactionTypeMaster tranxType, String type, String operation) {

        /* Purchase Duties Taxes */
        if (ledgerName.equalsIgnoreCase("DT")) {
            List<TranxPurInvoiceDutiesTaxes> list = new ArrayList<>();
            list = purInvoiceDutiesTaxesRepository.findByPurchaseTransactionAndStatus(mPurchaseTranx, true);
            if (list.size() > 0) {
                for (TranxPurInvoiceDutiesTaxes mDuties : list) {
                    insertFromDutiesTaxes(mDuties, mPurchaseTranx, tranxType, type, operation);
                }
            }
        } else if (ledgerName.equalsIgnoreCase("AC")) {
            /* Purchase Additional Charges */
            List<TranxPurInvoiceAdditionalCharges> list = new ArrayList<>();
            list = purInvoiceAdditionalChargesRepository.findByPurchaseTransaction(mPurchaseTranx);
            if (list.size() > 0) {
                for (TranxPurInvoiceAdditionalCharges mAdditinoalCharges : list) {
                    insertFromAdditionalCharges(mAdditinoalCharges, mPurchaseTranx, tranxType, type, operation);
                }
            }
            if (mPurchaseTranx.getAdditionLedger3() != null) {
                ledgerCommonPostings.callToPostingsTranxCode(mPurchaseTranx.getAdditionLedgerAmt3(), mPurchaseTranx.getAdditionLedger3(), tranxType, mPurchaseTranx.getAdditionLedger3().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), mPurchaseTranx.getAdditionLedgerAmt3() > 0 ? "DR" : "CR", true, tranxType.getTransactionCode(), operation, mPurchaseTranx.getTranxCode());
            }
        }
    }

    public void insertFromDutiesTaxes(TranxPurInvoiceDutiesTaxes mDuties, TranxPurInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String type, String operation) {
        try {
            /**** New Postings Logic *****/
//            ledgerCommonPostings.callToPostings(mDuties.getAmount(), mDuties.getDutiesTaxes(), tranxType, mDuties.getDutiesTaxes().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), type, true, tranxType.getTransactionCode(), operation);
            ledgerCommonPostings.callToPostingsTranxCode(mDuties.getAmount(), mDuties.getDutiesTaxes(), tranxType, mDuties.getDutiesTaxes().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), type, true, tranxType.getTransactionCode(), operation, mPurchaseTranx.getTranxCode());

            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, type, mDuties.getAmount(),
                        mPurchaseTranx.getFiscalYear(), mDuties.getDutiesTaxes(), mPurchaseTranx.getInvoiceDate(),
                        mPurchaseTranx.getId(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(), mPurchaseTranx.getTranxCode());
            }

            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mDuties.getDutiesTaxes().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mDuties.getDutiesTaxes(), mPurchaseTranx.getInvoiceDate(),
                            tranxType, mPurchaseTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertFromDutiesTaxes(method) :" + exceptionAsString);

        }
    }

    public void insertFromAdditionalCharges(TranxPurInvoiceAdditionalCharges mAdditinoalCharges, TranxPurInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String type, String operation) {
        try {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostingsTranxCode(mAdditinoalCharges.getAmount(), mAdditinoalCharges.getAdditionalCharges(), tranxType, mAdditinoalCharges.getAdditionalCharges().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), type, true, tranxType.getTransactionCode(), operation, mPurchaseTranx.getTranxCode());

            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, type, mAdditinoalCharges.getAmount(),
                        mPurchaseTranx.getFiscalYear(), mAdditinoalCharges.getAdditionalCharges(), mPurchaseTranx.getInvoiceDate(),
                        mPurchaseTranx.getId(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(), mPurchaseTranx.getTranxCode());
            }

            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mAdditinoalCharges.getAdditionalCharges().getId(), tranxType.getId(), mPurchaseTranx.getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mAdditinoalCharges.getAdditionalCharges(), mPurchaseTranx.getInvoiceDate(),
                            tranxType, mPurchaseTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception->insertFromAdditionalCharges(method) :" + exceptionAsString);

        }
    }

    /******* save into  Purchase Invoice *******/
    @Transactional
    public TranxPurInvoice saveIntoPurchaseInvoice(MultipartHttpServletRequest request) {
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurInvoice mPurchaseTranx = null;
        TransactionTypeMaster tranxType = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        TranxPurInvoice invoiceTranx = new TranxPurInvoice();
        if (users.getBranch() != null) {
            branch = users.getBranch();
            invoiceTranx.setBranch(branch);
        }
        invoiceTranx.setOutlet(outlet);
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRS");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        invoiceTranx.setTranxCode(tranxCode);
        try {
            if (paramMap.containsKey("invoice_date")) {
                LocalDate invoiceDate = LocalDate.parse(request.getParameter("invoice_date"));
                Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("invoice_date"));
                invoiceTranx.setInvoiceDate(strDt);
                /* fiscal year mapping */
                FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(invoiceDate);
                if (fiscalYear != null) {
                    invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
                    invoiceTranx.setFiscalYear(fiscalYear);
                }
            }
            if (paramMap.containsKey("gstNo")) {
                if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                    invoiceTranx.setGstNumber(request.getParameter("gstNo"));
                }
            }
            /* End of fiscal year mapping */

            if (paramMap.containsKey("reference_po_ids")) {
                String jsonRef = request.getParameter("reference_po_ids");
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
                invoiceTranx.setPoId(id);
                //   setCloseSO(request.getParameter("reference_so_id"));
            }
            /* convertions of Sales challan to invoice */
            if (paramMap.containsKey("reference_pc_ids")) {
                String jsonRef = request.getParameter("reference_pc_ids");
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
                invoiceTranx.setPcId(id);
                //   setCloseSO(request.getParameter("reference_so_id"));
            }


            if (paramMap.containsKey("invoice_no")) invoiceTranx.setVendorInvoiceNo(request.getParameter("invoice_no"));
            LedgerMaster purchaseAccount = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("purchase_id")), users.getOutlet().getId(), true);
            invoiceTranx.setPurchaseAccountLedger(purchaseAccount);
            invoiceTranx.setTransactionStatus(1L);
            LedgerMaster discountLedger = null;
            if (users.getBranch() == null)
                discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("purchase discount", users.getOutlet().getId(), true);
            else
                discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("purchase discount", users.getOutlet().getId(), users.getBranch().getId(), true);
            if (discountLedger != null) {
                invoiceTranx.setPurchaseDiscountLedger(discountLedger);
            }
            LedgerMaster sundryCreditors = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), users.getOutlet().getId(), true);
            invoiceTranx.setSundryCreditors(sundryCreditors);
            LocalDate mDate = LocalDate.now();
            invoiceTranx.setTransactionDate(mDate);
            invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
            invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
            LedgerMaster roundoff = null;
            if (users.getBranch() != null)
                roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
            else
                roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
            invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
            invoiceTranx.setPurchaseRoundOff(roundoff);
            invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
            invoiceTranx.setBalance(Double.parseDouble(request.getParameter("bill_amount")));
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
            if (paramMap.containsKey("tcs")) invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
            invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
            invoiceTranx.setPurchaseDiscountPer(Double.parseDouble(request.getParameter("purchase_discount")));
            invoiceTranx.setPurchaseDiscountAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
            invoiceTranx.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
            invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
            invoiceTranx.setSrno(Long.parseLong(request.getParameter("purchase_sr_no")));
            invoiceTranx.setCreatedBy(users.getId());
            invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
            invoiceTranx.setStatus(true);
            invoiceTranx.setCreatedBy(users.getId());
            invoiceTranx.setOperations("insert");
            if (paramMap.containsKey("paymentMode")) invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
            else invoiceTranx.setPaymentMode("");
            if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
            else invoiceTranx.setNarration("");
            if (paramMap.containsKey("reference")) invoiceTranx.setReference(request.getParameter("reference"));
            else invoiceTranx.setReference("");

            if (paramMap.containsKey("transport")) invoiceTranx.setTransportName(request.getParameter("transport"));
            else invoiceTranx.setTransportName("");
            if (paramMap.containsKey("additionalChgLedger3")) {
                LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
                if (additionalChgLedger3 != null) {
                    invoiceTranx.setAdditionLedger3(additionalChgLedger3);
                    invoiceTranx.setAdditionLedgerAmt3(Double.valueOf(request.getParameter("addChgLedgerAmt3")));
                }
            }
            if (paramMap.containsKey("isRoundOffCheck"))
                invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));
            /**** Upload Transaction Invoice for future reference *****/
            if (request.getFile("image") != null) {
                MultipartFile image = request.getFile("image");
                fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                if (imagePath != null) {
                    invoiceTranx.setImagePath(File.separator + "uploads" + File.separator + imagePath);
                }
            }
            if (paramMap.containsKey("debitNoteReference")) {
                invoiceTranx.setIsDebitNoteRef(Boolean.parseBoolean(request.getParameter("debitNoteReference")));
            }
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
            } else {
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
                invoiceTranx.setTcsMode("");
            }

            if (request.getParameterMap().containsKey("transactionTrackingNo"))
                invoiceTranx.setTransactionTrackingNo(request.getParameter("transactionTrackingNo"));
            else
                invoiceTranx.setTransactionTrackingNo(String.valueOf(new Date().getTime()));

            mPurchaseTranx = tranxPurInvoiceRepository.save(invoiceTranx);
            if (mPurchaseTranx != null) {
                /* adjust debit note bill against purchase invoice */
                if (paramMap.containsKey("debitNoteReference")) {
                    if (Boolean.parseBoolean(request.getParameter("debitNoteReference"))) {
                        String jsonStr = request.getParameter("bills");
                        JsonParser parser = new JsonParser();
                        JsonElement debitNoteBills = parser.parse(jsonStr);
                        JsonArray debitNotes = debitNoteBills.getAsJsonArray();
                        Double totalBalance = 0.0;
                        for (JsonElement mBill : debitNotes) {
                            TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = null;
                            JsonObject mDebitNote = mBill.getAsJsonObject();
                            totalBalance += mDebitNote.get("debitNotePaidAmt").getAsDouble();
                            tranxDebitNoteNewReference = tranxDebitNoteNewReferenceRepository.findByIdAndStatus(mDebitNote.get("debitNoteId").getAsLong(), true);
                            TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                            tranxDebitNoteNewReference.setTotalAmount(mDebitNote.get("debitNotePaidAmt").getAsDouble());
                            if (mDebitNote.get("debitNotePaidAmt").getAsDouble() > 0) {
                                tranxDebitNoteNewReference.setAdjustedId(mPurchaseTranx.getId());
                                tranxDebitNoteNewReference.setIsSelected(true);
                            }
                            if (mDebitNote.get("debitNoteRemaningAmt").getAsDouble() == 0) {
                                tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
                            }
                            tranxDebitNoteNewReference.setBalance(mDebitNote.get("debitNoteRemaningAmt").getAsDouble());
                            TranxDebitNoteNewReferenceMaster newReferenceMaster = tranxDebitNoteNewReferenceRepository.save(tranxDebitNoteNewReference);
                            /* Adding into Debit Note Details */
                            TranxDebitNoteDetails mDetails = new TranxDebitNoteDetails();
                            mDetails.setBranch(newReferenceMaster.getBranch());
                            mDetails.setOutlet(newReferenceMaster.getOutlet());
                            mDetails.setSundryCreditor(newReferenceMaster.getSundryCreditor());
                            mDetails.setTotalAmount(newReferenceMaster.getTotalAmount());
                            mDetails.setPaidAmt(mDebitNote.get("debitNotePaidAmt").getAsDouble());
                            mDetails.setAdjustedId(mPurchaseTranx.getId());
                            mDetails.setAdjustedSource("purchase_invoice");
                            mDetails.setOperations("adjust");
                            mDetails.setTranxDebitNoteMaster(newReferenceMaster);
                            mDetails.setStatus(true);
                            mDetails.setAdjustmentStatus(newReferenceMaster.getAdjustmentStatus());
                            // immediate
                            tranxDebitNoteDetailsRepository.save(mDetails);
                        }
                        mPurchaseTranx.setBalance(mPurchaseTranx.getTotalAmount() - totalBalance);
                        tranxPurInvoiceRepository.save(mPurchaseTranx);
                    }
                }
                /* Save into Duties and Taxes */
                String taxStr = request.getParameter("taxCalculation");
                //  JsonObject duties_taxes = new JsonObject(taxStr);
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoPurchaseDutiesTaxes(duties_taxes, mPurchaseTranx, taxFlag);
                /**** Save into Additional Charges ****/
                if (paramMap.containsKey("additionalCharges")) {
                    String strJson = request.getParameter("additionalCharges");
                    JsonElement tradeElement = new JsonParser().parse(strJson);
                    JsonArray additionalCharges = tradeElement.getAsJsonArray();
                    saveIntoPurchaseAdditionalCharges(additionalCharges, mPurchaseTranx, tranxType.getId(), users.getOutlet().getId());
                }
                JsonParser parser = new JsonParser();
                /* save into Purchase Invoice Details */
                String jsonStr = request.getParameter("row");
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                String referenceObject = request.getParameter("refObject");
                saveIntoPurchaseInvoiceDetails(array, mPurchaseTranx, branch, outlet, users.getId(), tranxType, referenceObject);
            }
        } catch (DataIntegrityViolationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Error in saveIntoPurchaseInvoice :" + exceptionAsString);
        } catch (Exception e1) {
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Error in saveIntoPurchaseInvoice :" + exceptionAsString);
        }
        return mPurchaseTranx;
    }

    private void saveIntoDayBook(TranxPurInvoice mPurchaseTranx) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(mPurchaseTranx.getOutlet());
        if (mPurchaseTranx.getBranch() != null) dayBook.setBranch(mPurchaseTranx.getBranch());
        dayBook.setAmount(mPurchaseTranx.getTotalAmount());
        dayBook.setTranxDate(mPurchaseTranx.getTransactionDate());
        dayBook.setParticulars(mPurchaseTranx.getSundryCreditors().getLedgerName());
        dayBook.setVoucherNo(mPurchaseTranx.getVendorInvoiceNo());
        dayBook.setVoucherType("Purchase Invoice");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }

    public void setClosePO(String poIds) {
        Boolean flag = false;
        String idList[];
        idList = poIds.split(",");
        for (String mId : idList) {
            TranxPurOrder tranxPurOrder = tranxPurOrderRepository.findByIdAndStatus(Long.parseLong(mId), true);
            if (tranxPurOrder != null) {
                tranxPurOrder.setStatus(false);
                tranxPurOrderRepository.save(tranxPurOrder);
            }
        }
    }

    public void setClosePC(String poIds) {
        Boolean flag = false;
        String idList[];
        idList = poIds.split(",");
        for (String mId : idList) {
            TranxPurChallan tranxPurChallan = tranxPurChallanRepository.findByIdAndStatus(Long.parseLong(mId), true);
            if (tranxPurChallan != null) {
                tranxPurChallan.setStatus(false);
                tranxPurChallanRepository.save(tranxPurChallan);
            }
        }
    }
    /* End of creation of Purchase Invoice */

    /****** Save into Duties and Taxes ******/
    public void saveIntoPurchaseDutiesTaxes(JsonObject duties_taxes, TranxPurInvoice mPurchaseTranx, Boolean taxFlag) {
        List<TranxPurInvoiceDutiesTaxes> purchaseDutiesTaxes = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            //JsonArray cgstList = null;
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (JsonElement mList : cgstList) {
                    JsonObject cgstObject = mList.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT CGST " + inputGst;
                    if (mPurchaseTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPurchaseTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(cgstObject.get("amt").getAsString()));
                        taxes.setPurchaseTransaction(mPurchaseTranx);
                        taxes.setSundryCreditors(mPurchaseTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (JsonElement mList : sgstList) {
                    JsonObject sgstObject = mList.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //   int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT SGST " + inputGst;
                    if (mPurchaseTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPurchaseTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(sgstObject.get("amt").getAsString()));
                        taxes.setPurchaseTransaction(mPurchaseTranx);
                        taxes.setSundryCreditors(mPurchaseTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
            }
        } else {
            if (duties_taxes.has("igst")) {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mList : igstList) {
                        JsonObject igstObject = mList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        if (mPurchaseTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPurchaseTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
                            taxes.setDutiesTaxes(dutiesTaxes);

                            taxes.setAmount(Double.parseDouble(igstObject.get("amt").getAsString()));
                            taxes.setPurchaseTransaction(mPurchaseTranx);
                            taxes.setSundryCreditors(mPurchaseTranx.getSundryCreditors());
                            taxes.setIntra(taxFlag);
                            taxes.setStatus(true);
                            purchaseDutiesTaxes.add(taxes);
                        }
                    }
                }
            }
        }
        try {
            /* save all Duties and Taxes into purchase Invoice Duties taxes table */
            purInvoiceDutiesTaxesRepository.saveAll(purchaseDutiesTaxes);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            System.out.println("Exception " + e1.getMessage());
            purInvoiceLogger.error("Error in saveIntoPurchaseDutiesTaxes" + e1.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            purInvoiceLogger.error("Error in saveIntoPurchaseDutiesTaxes" + e.getMessage());
        }
    }
    /* End of Purchase Duties and Taxes Ledger */

    /****    Save Into Purchase Additional Charges    *****/
    public void saveIntoPurchaseAdditionalCharges(JsonArray additionalCharges, TranxPurInvoice mPurchaseTranx, Long tranxId, Long outletId) {
        List<TranxPurInvoiceAdditionalCharges> chargesList = new ArrayList<>();
        if (mPurchaseTranx.getAdditionalChargesTotal() > 0) {
            try {
                for (JsonElement mList : additionalCharges) {
                    JsonObject object = mList.getAsJsonObject();
                    if (object.has("amt") && !object.get("amt").getAsString().equalsIgnoreCase("")) {
                        Double amount = 0.0;
                        Double percent = 0.0;
                        Long ledgerId = 0L;

                        LedgerMaster addcharges = null;
                        TranxPurInvoiceAdditionalCharges charges = new TranxPurInvoiceAdditionalCharges();
                        amount = object.get("amt").getAsDouble();
                        percent = object.get("percent").getAsDouble();
                        if (object.has("ledgerId") && !object.get("ledgerId").getAsString().equalsIgnoreCase("")) {
                            ledgerId = object.get("ledgerId").getAsLong();
                            addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                        }
                        charges.setAmount(amount);
                        charges.setPercent(percent);
                        charges.setAdditionalCharges(addcharges);
                        charges.setPurchaseTransaction(mPurchaseTranx);
                        charges.setStatus(true);
                        charges.setOperation("inserted");
                        charges.setCreatedBy(mPurchaseTranx.getCreatedBy());
                        chargesList.add(charges);
                        purInvoiceAdditionalChargesRepository.save(charges);
                    }
                }
            } catch (DataIntegrityViolationException e1) {
                StringWriter sw = new StringWriter();
                e1.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Error in saveIntoPurchaseAdditionalCharges :" + exceptionAsString);
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Error in saveIntoPurchaseAdditionalCharges :" + exceptionAsString);
            }
        }
    }
    /* End of Purchase Additional Charges */

    /****** save into Purchase Invoice Details ******/

    public void saveIntoPurchaseInvoiceDetails(JsonArray array, TranxPurInvoice mPurchaseTranx, Branch branch,
                                               Outlet outlet, Long userId, TransactionTypeMaster tranxType,
                                               String referenceObject) {
        /* Purchase Product Details Start here */
        try {
            String referenceType = "";
            boolean flag_status = false;
            Long referenceId = 0L;
            List<TranxPurchaseInvoiceProductSrNumber> newSerialNumbers = new ArrayList<>();
            for (JsonElement mList : array) {
                JsonObject object = mList.getAsJsonObject();
                Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
                referenceType = object.get("reference_type").getAsString();
                Long levelAId = null;
                Long levelBId = null;
                Long levelCId = null;
                String batchNo = null;
                String serialNo = null;
                ProductBatchNo productBatchNo = null;
                LevelA levelA = null;
                LevelB levelB = null;
                LevelC levelC = null;
                double free_qty = 0.0;
                double tranxQty = 0.0;
                Double stkQty = 0.0;
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
                TranxPurInvoiceDetailsUnits invoiceUnits = new TranxPurInvoiceDetailsUnits();
                invoiceUnits.setPurchaseTransaction(mPurchaseTranx);
                invoiceUnits.setProduct(mProduct);
                invoiceUnits.setUnits(units);
                invoiceUnits.setQty(object.get("qty").getAsDouble());
                tranxQty = object.get("qty").getAsDouble();
                if (object.has("free_qty") &&
                        !object.get("free_qty").getAsString().equalsIgnoreCase("")) {
                    free_qty = object.get("free_qty").getAsDouble();
                    invoiceUnits.setFreeQty(free_qty);
                } else {
                    invoiceUnits.setFreeQty(0.0);
                }
                invoiceUnits.setRate(object.get("rate").getAsDouble());

                if (levelA != null) invoiceUnits.setLevelA(levelA);
                if (levelB != null) invoiceUnits.setLevelB(levelB);
                if (levelC != null) invoiceUnits.setLevelC(levelC);

                invoiceUnits.setStatus(true);
                if (object.has("base_amt")) invoiceUnits.setBaseAmt(object.get("base_amt").getAsDouble());
                if (object.has("unit_conv") && !object.get("unit_conv").getAsString().equalsIgnoreCase(""))
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
                invoiceUnits.setReturnQty(0.0);
                TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
                invoiceUnits.setTransactionStatus(transactionStatus);
                /******* Insert into Product Batch No *******/
                boolean flag = false;
                try {
                    if (object.get("is_batch").getAsBoolean()) {
                        flag = true;
                        double net_amt = object.get("final_amt").getAsDouble();
                        double costing = 0;
                        double costing_with_tax = 0;

                        /*if (outlet.getGstTypeMaster().getId() == 1L) { //Registered
                            net_amt = object.get("total_amt").getAsDouble();
                            costing = net_amt / (qty + free_qty);
                            costing_with_tax = costing + object.get("total_igst").getAsDouble();
                        }*/ /*else { // composition or un-registered
                            costing = net_amt / (qty + free_qty);
                            costing_with_tax = costing;
                        }*/
                        /**** Creating new batch senarios
                         * case 1 : if supplier is changed ,create new batch even batch number is same or different
                         * against that supplier so that we can maintain valuation of that product supplier wise
                         * case 2 : if anything changes in purchase rate or costing ,create new batch even
                         * batch number is same or different
                         */
                        if (object.get("b_details_id").getAsLong() == 0) {
                            ProductBatchNo mproductBatchNo = new ProductBatchNo();
                            if (object.has("b_no")) mproductBatchNo.setBatchNo(object.get("b_no").getAsString());
                            mproductBatchNo.setMrp(0.0);
                            if (object.has("b_rate") && !object.get("b_rate").getAsString().equalsIgnoreCase(""))
                                mproductBatchNo.setMrp(object.get("b_rate").getAsDouble());
                            mproductBatchNo.setPurchaseRate(0.0);
                            if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").getAsString().equals(""))
                                mproductBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                            if (object.has("b_expiry") &&
                                    !object.get("b_expiry").getAsString().equalsIgnoreCase("") &&
                                    !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                            mproductBatchNo.setQnty(object.get("qty").getAsInt());
                            mproductBatchNo.setFreeQty(free_qty);
                            mproductBatchNo.setSalesRate(0.0);
                            if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equals(""))
                                mproductBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                            if (object.has("costing") && !object.get("costing").isJsonNull())
                                mproductBatchNo.setCosting(object.get("costing").getAsDouble());
                            else mproductBatchNo.setCosting(0.0);
                            if (object.has("costing_with_tax") && !object.get("costing_with_tax").isJsonNull())
                                mproductBatchNo.setCostingWithTax(object.get("costing_with_tax").getAsDouble());
                            else mproductBatchNo.setCostingWithTax(0.0);
                            mproductBatchNo.setMinRateA(0.0);
                            mproductBatchNo.setMinRateB(0.0);
                            mproductBatchNo.setMinRateC(0.0);
                            if (object.has("rate_a") && !object.get("rate_a").getAsString().equalsIgnoreCase(""))
                                mproductBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                            if (object.has("rate_b") && !object.get("rate_b").getAsString().equalsIgnoreCase(""))
                                mproductBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                            if (object.has("rate_c") && !object.get("rate_c").getAsString().equalsIgnoreCase(""))
                                mproductBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                            if (object.has("margin_per") &&
                                    !object.get("margin_per").getAsString().equals(""))
                                mproductBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
                            if (object.has("manufacturing_date") &&
                                    !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") &&
                                    !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setManufacturingDate(LocalDate.parse(
                                        object.get("manufacturing_date").getAsString()));
                            mproductBatchNo.setStatus(true);
                            mproductBatchNo.setProduct(mProduct);
                            mproductBatchNo.setOutlet(outlet);
                            mproductBatchNo.setBranch(branch);
                            if (levelA != null) mproductBatchNo.setLevelA(levelA);
                            if (levelB != null) mproductBatchNo.setLevelB(levelB);
                            if (levelC != null) mproductBatchNo.setLevelC(levelC);
                            mproductBatchNo.setUnits(units);
                            mproductBatchNo.setSupplierId(mPurchaseTranx.getSundryCreditors().getId());

                            productBatchNo = productBatchNoRepository.save(mproductBatchNo);


                        } else {
                            productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                            Double qnty = object.get("qty").getAsDouble();
                            productBatchNo.setQnty(qnty.intValue());
                            if (object.has("costing") && !object.get("costing").isJsonNull())
                                productBatchNo.setCosting(object.get("costing").getAsDouble());
                            if (object.has("costing_with_tax") && !object.get("costing_with_tax").isJsonNull())
                                productBatchNo.setCostingWithTax(object.get("costing_with_tax").getAsDouble());
                            productBatchNo = productBatchNoRepository.save(productBatchNo);
                        }
                        batchNo = productBatchNo.getBatchNo();
                        batchId = productBatchNo.getId();
                    }
                    invoiceUnits.setProductBatchNo(productBatchNo);
                    TranxPurInvoiceDetailsUnits mTranxUnitDetails = tranxPurInvoiceUnitsRepository.save(invoiceUnits);
                    /**
                     * @implNote validation of Product Delete , if any tranx done for this product, user cant delete this product **
                     * @auther ashwins@opethic.com
                     * @version sprint 21
                     **/
                    if (mProduct != null && mProduct.getIsDelete()) {
                        mProduct.setIsDelete(false);
                        productRepository.save(mProduct);
                    }

                    /**** Save this rate into Product Master if purchase rate has been changed by customer
                     (non batch case only) ****/
                    Double prrate = object.get("rate").getAsDouble();
                    Double costing = object.get("costing").getAsDouble();
                    Double costingWithTax = object.get("costing_with_tax").getAsDouble();
                    ProductUnitPacking mUnitPackaging = productUnitRepository.findRate(mProduct.getId(), levelAId, levelBId, levelCId, units.getId(), true);
                    if (mUnitPackaging != null) {
                        if (mUnitPackaging.getPurchaseRate() != null && mUnitPackaging.getPurchaseRate() != 0 && prrate != mUnitPackaging.getPurchaseRate()) {
                            mUnitPackaging.setPurchaseRate(prrate);
                            mUnitPackaging.setCosting(costing);
                            mUnitPackaging.setCostingWithTax(costingWithTax);
                            productUnitRepository.save(mUnitPackaging);
                        }
                    }
                    /******* Insert into Tranx Product Serial Numbers  ******/
                    JsonArray jsonArray = object.getAsJsonArray("serialNo");
                    if (jsonArray != null && jsonArray.size() > 0) {
                        flag = false;
                        List<TranxPurchaseInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            TranxPurchaseInvoiceProductSrNumber productSerialNumber = new TranxPurchaseInvoiceProductSrNumber();
                            productSerialNumber.setProduct(mProduct);
                            productSerialNumber.setSerialNo(serialNo);
                            productSerialNumber.setTransactionStatus("Purchase");
                            productSerialNumber.setStatus(true);
                            productSerialNumber.setCreatedBy(userId);
                            productSerialNumber.setOperations("Inserted");
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setBranch(mPurchaseTranx.getBranch());
                            productSerialNumber.setOutlet(mPurchaseTranx.getOutlet());
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setUnits(units);
                            productSerialNumber.setTranxPurInvoiceDetailsUnits(mTranxUnitDetails);
                            productSerialNumber.setLevelA(levelA);
                            productSerialNumber.setLevelB(levelB);
                            productSerialNumber.setLevelC(levelC);
                            productSerialNumber.setUnits(units);
                            TranxPurchaseInvoiceProductSrNumber mSerialNo = serialNumberRepository.save(productSerialNumber);
                            if (mProduct.getIsInventory() && !referenceType.equalsIgnoreCase("PRSCHN")) {
                                stkQty = unitConversion.convertToLowerUnit(mProduct.getId(), units.getId(), (tranxQty + free_qty));
                                inventoryCommonPostings.callToInventoryPostings("CR",
                                        mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(),
                                        stkQty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear(), serialNo);
                            }
                        }
                    } else {
                        /**** Inventory Postings *****/
                        if (mProduct.getIsInventory() && !referenceType.equalsIgnoreCase("PRSCHN")) {
                            /***** new architecture of Inventory Postings *****/
                            stkQty = unitConversion.convertToLowerUnit(mProduct.getId(), units.getId(), (tranxQty + free_qty));
                            inventoryCommonPostings.callToInventoryPostings("CR",
                                    mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(),
                                    stkQty, branch, outlet, mProduct, tranxType, levelA,
                                    levelB, levelC, units, productBatchNo, batchNo,
                                    mPurchaseTranx.getFiscalYear(), serialNo);
                            /***** End of new architecture of Inventory Postings *****/

                            /**
                             * @implNote New Logic of opening and closing Inventory posting
                             * @auther ashwins@opethic.com
                             * @version sprint 1
                             **/
                            closingUtility.stockPosting(outlet, branch, mPurchaseTranx.getFiscalYear().getId(), batchId,
                                    mProduct, tranxType.getId(), mPurchaseTranx.getInvoiceDate(), stkQty, 0.0,
                                    mPurchaseTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                    mPurchaseTranx.getTranxCode(), userId, "IN", mProduct.getPackingMaster().getId());

                            closingUtility.stockPostingBatchWise(outlet, branch, mPurchaseTranx.getFiscalYear().getId(), batchId,
                                    mProduct, tranxType.getId(), mPurchaseTranx.getInvoiceDate(), stkQty, 0.0,
                                    mPurchaseTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                    mPurchaseTranx.getTranxCode(), userId, "IN", mProduct.getPackingMaster().getId());

                            /***** End of new logic of Inventory Postings *****/
                        }

                    }
                    flag = true;

                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    purInvoiceLogger.error("Error in saveIntoPurchaseInvoiceDetails :" + exceptionAsString);
                }
                /* Inserting into Barcode */
//                ProductBarcode barcode = null;
//                barcode = barcodeRepository.findByProductIdAndOutletIdAndStatus(mProduct.getId(), outlet.getId(), true);
                ProductBarcode barcode = new ProductBarcode();
                barcode.setFiscalYear(mPurchaseTranx.getFiscalYear());
                barcode.setOutlet(mPurchaseTranx.getOutlet());
                barcode.setStatus(true);
                barcode.setBranch(mPurchaseTranx.getBranch());
                barcode.setMrp(object.get("rate").getAsDouble());
                if (object.has("companybarcode")) barcode.setCompanyBarcode(object.get("companybarcode").getAsString());
                barcode.setTranxDate(mPurchaseTranx.getTransactionDate());
                barcode.setEnable(true);
                barcode.setProduct(mProduct);
                barcode.setQnty(object.get("qty").getAsInt());
                if (levelA != null) barcode.setLevelA(levelA);
                if (levelB != null) barcode.setLevelB(levelB);
                if (levelC != null) barcode.setLevelC(levelC);
                barcode.setUnits(units);
                barcode.setTransactionTypeMaster(tranxType);
                String cName = mPurchaseTranx.getOutlet().getCompanyName();
                String firstLetter = cName.substring(0, 3);
                GenerateDates generateDates = new GenerateDates();
                String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
                Long lastRecord = barcodeRepository.findLastRecord(mPurchaseTranx.getOutlet().getId());
                if (lastRecord != null) {
                    String serailNo = String.format("%05d", lastRecord + 1);// 5 digit serial number
                    String bCode = firstLetter + currentMonth + serailNo;
                    barcode.setBarcodeUniqueCode(bCode);
                }
                barcode.setBatchNo(object.get("b_no").getAsString());
                if (productBatchNo != null) barcode.setProductBatch(productBatchNo);
                barcode.setTransactionId(mPurchaseTranx.getId());
                barcodeRepository.save(barcode);
                /**** end of Barcode ****/


                /* End of inserting into TranxPurchaseInvoiceDetailsUnits */
                /* closing of purchase orders while converting into purchase invoice using its qnt */
                double qty = object.get("qty").getAsDouble();
                if (!object.get("reference_id").getAsString().equalsIgnoreCase(""))
                    referenceId = object.get("reference_id").getAsLong();
                if (referenceType.equalsIgnoreCase("PRSORD")) {
                    TranxPurOrderDetailsUnits orderDetails = tranxPurOrderDetailsUnitRepository.findByProductDetailsLevel(referenceId, mProduct.getId(), units.getId(), levelAId, levelBId, levelCId, true);
                    if (orderDetails != null) {
                        if (qty != orderDetails.getQty().doubleValue()) {
                            flag_status = true;
                            double totalQty = orderDetails.getQty().doubleValue() - qty;
                            orderDetails.setQty(totalQty);//push data into History table before update(remainding)
                            tranxPurOrderDetailsUnitRepository.save(orderDetails);
                        } else {
                            orderDetails.setTransactionStatus(2L);
                            tranxPurOrderDetailsUnitRepository.save(orderDetails);
                        }
                    }
                } /* End of  closing of purchase order while converting into purchase invoice
            using its qnt */

                /* closing of purchase orders while converting into purchase invoice using its qnt */
                else {
                    TranxPurChallanDetailsUnits challanDetails = tranxPurChallanDetailsUnitRepository.findByProductDetailsLevel(referenceId, mProduct.getId(), units.getId(), levelAId, levelBId, levelCId, true);
                    if (challanDetails != null) {
                        if (qty != challanDetails.getQty().doubleValue()) {
                            flag_status = true;
                            double totalQty = challanDetails.getQty().doubleValue() - qty;
                            challanDetails.setQty(totalQty);//push data into History table before update(remainding)
                            tranxPurChallanDetailsUnitRepository.save(challanDetails);
                        } else {
                            challanDetails.setTransactionStatus(2L);
                            tranxPurChallanDetailsUnitRepository.save(challanDetails);
                        }
                    }
                }
            /* End of  closing of purchase challan while converting into purchase invoice
            using its qnt */
            }
            if (referenceType.equalsIgnoreCase("PRSORD")) {
                TranxPurOrder tranxPurOrder = tranxPurOrderRepository.findByIdAndStatus(referenceId, true);
                if (tranxPurOrder != null) {
                    if (flag_status) {
                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
                        tranxPurOrder.setTransactionStatus(transactionStatus);
                        tranxPurOrderRepository.save(tranxPurOrder);
                    } else {
                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                        tranxPurOrder.setTransactionStatus(transactionStatus);
                        tranxPurOrderRepository.save(tranxPurOrder);
                    }
                }
            } else {
                TranxPurChallan tranxPurChallan = tranxPurChallanRepository.findByIdAndStatus(referenceId, true);
                if (tranxPurChallan != null) {
                    if (flag_status) {
                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
                        tranxPurChallan.setTransactionStatus(transactionStatus);
                        tranxPurChallanRepository.save(tranxPurChallan);
                    } else {
                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                        tranxPurChallan.setTransactionStatus(transactionStatus);
                        tranxPurChallanRepository.save(tranxPurChallan);
                    }
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Error in saveIntoPurchaseInvoiceDetails :" + exceptionAsString);
        }
    }
    /* End of Purchase Invoice Details */


    public JsonObject purchaseLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxPurInvoiceRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxPurInvoiceRepository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
       /* String companyName = users.getOutlet().getCompanyName();
        companyName = companyName.substring(0, 3);*/ // fetching first 3 digits from company names
        /* getting Start and End year from fiscal Year */
//        String startYear = generateFiscalYear.getStartYear();
//        String endYear = generateFiscalYear.getEndYear();
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
       /* String piCode = companyName.toUpperCase() + "-" + startYear + endYear
                + "-" + "PI" + currentMonth + "-" + serailNo;*/
        String piCode = "PI" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("serialNo", piCode);
        return result;
    }

    /* find all purchase invoices outletwise */
    public JsonObject AllpurchaseList(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        Map<String, String[]> paramMap = request.getParameterMap();
        List<TranxPurInvoice> purInvoice = new ArrayList<>();
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
                purInvoice = tranxPurInvoiceRepository.findPurchaseInvoicesListWithDate(users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, true);
            } else {
                purInvoice = tranxPurInvoiceRepository.findPurchaseInvoicesListWithDateNoBr(users.getOutlet().getId(), startDatep, endDatep, true);
            }
        } else {
            if (users.getBranch() != null) {
                purInvoice = tranxPurInvoiceRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                purInvoice = tranxPurInvoiceRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
            }
        }

        for (TranxPurInvoice invoices : purInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getVendorInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(invoices.getInvoiceDate()).toString());
            response.addProperty("transaction_date", invoices.getTransactionDate().toString());
            response.addProperty("purchase_serial_number", invoices.getSrno());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("narration", invoices.getNarration());
            response.addProperty("supplier_code", invoices.getSundryCreditors().getUniqueCode());
            response.addProperty("sundry_creditor_name", invoices.getSundryCreditors().getLedgerName());
            response.addProperty("supplier_code", invoices.getSundryCreditors().getLedgerCode());
            response.addProperty("narration", invoices.getNarration());
            response.addProperty("sundry_creditor_id", invoices.getSundryCreditors().getId());
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

    //Start of purchase invoice list with pagination
    public Object purchaseList(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        Boolean flag = false;
        List purchaseInvoice = new ArrayList<>();
        List<TranxPurInvoice> purchaseArrayList = new ArrayList<>();
        List<PurInvoiceDTO> purInvoiceDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT tranx_purchase_invoice_tbl.id FROM `tranx_purchase_invoice_tbl` LEFT JOIN ledger_master_tbl ON " + "tranx_purchase_invoice_tbl.sundry_creditors_id=ledger_master_tbl.id WHERE " + "tranx_purchase_invoice_tbl.outlet_id=" + users.getOutlet().getId() + " AND tranx_purchase_invoice_tbl.status=1";
            if (users.getBranch() != null) {
                query = query + " AND tranx_purchase_invoice_tbl.branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND tranx_purchase_invoice_tbl.branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += "  AND DATE(tranx_purchase_invoice_tbl.invoice_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

           /* if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND narration LIKE '%" + searchText + "%'";
            }*/

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND (vendor_invoice_no LIKE '%" + searchText + "%' OR DATE(invoice_date)=" + searchText + " OR total_amount=" + searchText + " OR taxable_amount=" + searchText + ")";
            }
            String jsonToStr = request.get("sort");
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
            //  String query1 = query;       //we get all lists in this list

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;
          /*  Query q = entityManager.createNativeQuery(query, TranxPurInvoice.class);
            purchaseInvoice = q.getResultList();*/
            Query q = entityManager.createNativeQuery(query);
            purchaseInvoice = q.getResultList();
          /*  Query q1 = entityManager.createNativeQuery(query1, TranxPurInvoice.class);
            purchaseArrayList = q1.getResultList();
            System.out.println("Limit total rows " + purchaseArrayList.size());
          */
            /**** *****/
            String query1 = "SELECT COUNT(tranx_purchase_invoice_tbl.id) as totalcount FROM tranx_purchase_invoice_tbl " + "WHERE tranx_purchase_invoice_tbl.status=? AND tranx_purchase_invoice_tbl.outlet_id=?";
            if (users.getBranch() != null) {
                query1 = query1 + " AND tranx_purchase_invoice_tbl.branch_id=?";
            } else {
                query1 = query1 + " AND tranx_purchase_invoice_tbl.branch_id IS NULL";
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
            for (Object mList : purchaseInvoice) {
                TranxPurInvoice invoiceListView = tranxPurInvoiceRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                purInvoiceDTOList.add(convertToDTDTO(invoiceListView));
            }

            List<TranxPurInvoice> purInList = new ArrayList<>();
          /*  purInList = q1.getResultList();
            System.out.println("total rows " + purInList.size());*/
            GenericDatatable<PurInvoiceDTO> data = new GenericDatatable<>(purInvoiceDTOList, totalProducts, pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            genericDTData.setRows(purInvoiceDTOList);
            genericDTData.setTotalRows(0);

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Error  :" + exceptionAsString);


        }
        return responseMessage;
    }

    //End of purchase invoice list with pagination
    //Start of DTO for purchase invoice
    private PurInvoiceDTO convertToDTDTO(TranxPurInvoice purInvoice) {
        PurInvoiceDTO purInvoiceDTO = new PurInvoiceDTO();
        purInvoiceDTO.setId(purInvoice.getId());
        purInvoiceDTO.setInvoice_no(purInvoice.getVendorInvoiceNo());
        purInvoiceDTO.setInvoice_date(DateConvertUtil.convertDateToLocalDate(purInvoice.getInvoiceDate()).toString());
        purInvoiceDTO.setTransaction_date(purInvoice.getTransactionDate().toString());
        purInvoiceDTO.setPurchase_serial_number(purInvoice.getSrno());
        purInvoiceDTO.setTotal_amount(purInvoice.getTotalAmount());
        purInvoiceDTO.setNarration(purInvoice.getNarration() != null ? purInvoice.getNarration() : "");
        purInvoiceDTO.setSundry_creditor_id(purInvoice.getSundryCreditors().getId());
        purInvoiceDTO.setSundry_creditor_name(purInvoice.getSundryCreditors().getLedgerName());
        purInvoiceDTO.setPurchase_account_name(purInvoice.getPurchaseAccountLedger().getLedgerName());
        purInvoiceDTO.setTax_amt(purInvoice.getTotalTax());
        purInvoiceDTO.setTaxable_amt(purInvoice.getTotalBaseAmount());
        purInvoiceDTO.setTranxCode(purInvoice.getTranxCode());
        purInvoiceDTO.setSupplier_code(purInvoice.getSundryCreditors().getLedgerCode() != null ?
                purInvoice.getSundryCreditors().getLedgerCode() : "");
        String idList[];
        String referenceNo = "";
        purInvoiceDTO.setReferenceType(purInvoice.getReference());
        if (purInvoice.getReference() != null && !purInvoice.getReference().isEmpty()) {
            if (purInvoice.getReference().equalsIgnoreCase("PRSORD") && purInvoice.getPoId() != null) {
                idList = purInvoice.getPoId().split(",");
                for (int i = 0; i < idList.length; i++) {
                    TranxPurOrder tranxPurOrder = tranxPurOrderRepository.findByIdAndStatus(Long.parseLong(idList[i]), true);
                    if (tranxPurOrder != null) {
                        referenceNo = referenceNo + tranxPurOrder.getVendorInvoiceNo();
                        if (i < idList.length - 1)
                            referenceNo = referenceNo + ",";
                    }
                }
                purInvoiceDTO.setReferenceNo(referenceNo);
            } else if (purInvoice.getReference().equalsIgnoreCase("PRSCHN") && purInvoice.getPoId() != null) {
                idList = purInvoice.getPoId().split(",");
                for (int i = 0; i < idList.length; i++) {
                    TranxPurChallan tranxPurChallan = tranxPurChallanRepository.findByIdAndStatus(Long.parseLong(idList[i]), true);
                    if (tranxPurChallan != null) {
                        referenceNo = referenceNo + tranxPurChallan.getVendorInvoiceNo();
                        if (i < idList.length - 1)
                            referenceNo = referenceNo + ",";
                    }
                }
                purInvoiceDTO.setReferenceNo(referenceNo);
            }
        } else {
            purInvoiceDTO.setReferenceNo("");
        }
        return purInvoiceDTO;
    }
    //End of DTO for purchase invoice

    public JsonObject getPurchaseInvoiceById(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurInvoiceDetails> list = new ArrayList<>();
        JsonArray units = new JsonArray();
        List<TranxPurchaseInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxPurInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxPurInvoice purchaseInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            list = invoiceDetailsRepository.findByPurchaseTransactionIdAndStatus(id, true);
           /* serialNumbers = serialNumberRepository.findByPurchaseTransactionIdAndStatus(purchaseInvoice.getId(), true);
            additionalCharges = purInvoiceAdditionalChargesRepository.findByPurchaseTransactionIdAndStatus(purchaseInvoice.getId(), true);*/
            finalResult.addProperty("tcs", purchaseInvoice.getTcs());
            finalResult.addProperty("narration", purchaseInvoice.getNarration() != null ? purchaseInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", purchaseInvoice.getPurchaseDiscountLedger() != null ? purchaseInvoice.getPurchaseDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", purchaseInvoice.getPurchaseDiscountAmount());
            finalResult.addProperty("discountInPer", purchaseInvoice.getPurchaseDiscountPer());
            finalResult.addProperty("totalPurchaseDiscountAmt", purchaseInvoice.getTotalPurchaseDiscountAmt());

            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", purchaseInvoice.getId());
            result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getInvoiceDate()).toString());
            result.addProperty("invoice_no", purchaseInvoice.getVendorInvoiceNo().toString());
            result.addProperty("purchase_sr_no", purchaseInvoice.getSrno());
            result.addProperty("purchase_account_ledger_id", purchaseInvoice.getPurchaseAccountLedger().getId());
            result.addProperty("supplierId", purchaseInvoice.getSundryCreditors().getId());
            result.addProperty("transaction_dt", purchaseInvoice.getTransactionDate().toString());
            result.addProperty("additional_charges_total", purchaseInvoice.getAdditionalChargesTotal());
            result.addProperty("gstNo", purchaseInvoice.getGstNumber() != null ? purchaseInvoice.getGstNumber() : "");
            /* End of Purchase Invoice Data */

            /* Purchase Invoice Details */
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxPurInvoiceDetails mDetails : list) {
                    JsonObject prDetails = new JsonObject();
                    prDetails.addProperty("details_id", mDetails.getId());
                    prDetails.addProperty("product_id", mDetails.getProduct().getId());
                    /* getting Units of Purcase Invoice*/
                    List<TranxPurInvoiceDetailsUnits> unitDetails = tranxPurInvoiceUnitsRepository.findByPurInvoiceDetailsIdAndStatus(mDetails.getId(), true);

                    JsonArray productDetails = new JsonArray();
                    unitDetails.forEach(mUnit -> {
                        JsonObject mObject = new JsonObject();
                        if (mUnit.getBrand() != null) {
                            mObject.addProperty("brandId", mUnit.getBrand().getId());
                            mObject.addProperty("brand_name", mUnit.getBrand().getBrandName());
                        } else {
                            mObject.addProperty("brandId", "");
                            mObject.addProperty("brand_name", "");
                        }
                        if (mUnit.getCategory() != null) {
                            mObject.addProperty("categoryId", mUnit.getCategory().getId());
                            mObject.addProperty("category_name", mUnit.getCategory().getCategoryName());
                        } else {
                            mObject.addProperty("categoryId", "");
                            mObject.addProperty("category_name", "");
                        }
                        if (mUnit.getSubcategory() != null) {
                            mObject.addProperty("subcategoryId", mUnit.getSubcategory().getId());
                            mObject.addProperty("subcategory_name", mUnit.getSubcategory().getSubcategoryName());
                        } else {
                            mObject.addProperty("subcategoryId", "");
                            mObject.addProperty("subcategory_name", "");
                        }
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
                        if (mUnit.getProductBatchNo() != null) {
                            mObject.addProperty("b_details_id", mUnit.getProductBatchNo().getId());
                            mObject.addProperty("b_no", mUnit.getProductBatchNo().getBatchNo());
                            mObject.addProperty("is_batch", true);

                        } else {
                            mObject.addProperty("b_details_id", "");
                            mObject.addProperty("b_no", "");
                            mObject.addProperty("is_batch", false);
                        }
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
            }
            // System.out.println("Row  " + row);
            /* End of Purchase Invoice Details */

            /* Purchase Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            if (additionalCharges.size() > 0) {
                for (TranxPurInvoiceAdditionalCharges mAdditionalCharges : additionalCharges) {
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
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Error in getPurchaseInvoiceById  :" + exceptionAsString);

            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Error in getPurchaseInvoiceById  :" + exceptionAsString);
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject getPurchaseInvoiceByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        JsonObject result = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxPurInvoice purchaseInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            if (purchaseInvoice != null) {
                finalResult.addProperty("tcs_mode", purchaseInvoice.getTcsMode());
                if (purchaseInvoice.getTcsMode().equalsIgnoreCase("tcs")) {
                    finalResult.addProperty("tcs_per", purchaseInvoice.getTcs());
                    finalResult.addProperty("tcs_amt", purchaseInvoice.getTcsAmt());
                } else if (purchaseInvoice.getTcsMode().equalsIgnoreCase("tds")) {
                    finalResult.addProperty("tcs_per", purchaseInvoice.getTdsPer());
                    finalResult.addProperty("tcs_amt", purchaseInvoice.getTdsAmt());
                } else {
                    finalResult.addProperty("tcs_amt", 0.0);
                    finalResult.addProperty("tcs_per", 0.0);
                }
                finalResult.addProperty("narration", purchaseInvoice.getNarration() != null ? purchaseInvoice.getNarration() : "");
                finalResult.addProperty("paymentMode", purchaseInvoice.getPaymentMode() != null ? purchaseInvoice.getPaymentMode() : "");
                finalResult.addProperty("discountLedgerId", purchaseInvoice.getPurchaseDiscountLedger() != null ? purchaseInvoice.getPurchaseDiscountLedger().getId() : 0);
                finalResult.addProperty("discountInAmt", purchaseInvoice.getPurchaseDiscountAmount());
                finalResult.addProperty("discountInPer", purchaseInvoice.getPurchaseDiscountPer());
                finalResult.addProperty("totalPurchaseDiscountAmt", purchaseInvoice.getTotalPurchaseDiscountAmt());
                finalResult.addProperty("totalQty", purchaseInvoice.getTotalqty());
                finalResult.addProperty("totalFreeQty", purchaseInvoice.getFreeQty());
                finalResult.addProperty("grossTotal", purchaseInvoice.getGrossAmount());
                finalResult.addProperty("totalAmount", purchaseInvoice.getTotalAmount());
                finalResult.addProperty("total_row_gross_amt", purchaseInvoice.getTotalBaseAmount());
                finalResult.addProperty("totalTax", purchaseInvoice.getTotalTax());
                finalResult.addProperty("additionLedger1", purchaseInvoice.getAdditionLedger1() != null ? purchaseInvoice.getAdditionLedger1().getId() : 0);
                finalResult.addProperty("additionLedgerAmt1", purchaseInvoice.getAdditionLedgerAmt1() != null ? purchaseInvoice.getAdditionLedgerAmt1() : 0);
                finalResult.addProperty("additionLedger2", purchaseInvoice.getAdditionLedger2() != null ? purchaseInvoice.getAdditionLedger2().getId() : 0);
                finalResult.addProperty("additionLedgerAmt2", purchaseInvoice.getAdditionLedgerAmt2() != null ? purchaseInvoice.getAdditionLedgerAmt2() : 0);
                finalResult.addProperty("additionLedger3", purchaseInvoice.getAdditionLedger3() != null ? purchaseInvoice.getAdditionLedger3().getId() : 0);
                finalResult.addProperty("additionLedgerAmt3", purchaseInvoice.getAdditionLedgerAmt3() != null ? purchaseInvoice.getAdditionLedgerAmt3() : 0);
                finalResult.addProperty("debitNoteReference", purchaseInvoice.getIsDebitNoteRef() != null ? purchaseInvoice.getIsDebitNoteRef() : false);
                /* Purchase Invoice Data */
                result.addProperty("id", purchaseInvoice.getId());
                result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getInvoiceDate()).toString());
                result.addProperty("invoice_no", purchaseInvoice.getVendorInvoiceNo());
                result.addProperty("tranx_unique_code", purchaseInvoice.getTranxCode());
                result.addProperty("purchase_sr_no", purchaseInvoice.getSrno());
                result.addProperty("purchase_account_ledger_id", purchaseInvoice.getPurchaseAccountLedger().getId());
                result.addProperty("supplierId", purchaseInvoice.getSundryCreditors().getId());
                result.addProperty("supplierName", purchaseInvoice.getSundryCreditors().getLedgerName());
                result.addProperty("transaction_dt", purchaseInvoice.getTransactionDate().toString());
                result.addProperty("additional_charges_total", purchaseInvoice.getAdditionalChargesTotal());
                result.addProperty("gstNo", purchaseInvoice.getGstNumber() != null ? purchaseInvoice.getGstNumber() : "");
                result.addProperty("isRoundOffCheck", purchaseInvoice.getIsRoundOff());
                result.addProperty("roundoff", purchaseInvoice.getRoundOff());
                result.addProperty("mode", purchaseInvoice.getPaymentMode() != "" ? purchaseInvoice.getPaymentMode() : "");
                result.addProperty("image", purchaseInvoice.getImagePath() != null ? serverUrl + purchaseInvoice.getImagePath() : "");
                result.addProperty("ledgerStateCode", purchaseInvoice.getSundryCreditors().getStateCode());
                /* End of Purchase Invoice Data */
            }

            /* Purchase Invoice Details */
            JsonArray row = new JsonArray();
            List<TranxPurInvoiceDetailsUnits> unitsArray = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatus(purchaseInvoice.getId(), true);
            for (TranxPurInvoiceDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                JsonObject selectedProduct = new JsonObject();
                selectedProduct.addProperty("barcode", "null");
                selectedProduct.addProperty("id", mUnits.getProduct().getId());
                selectedProduct.addProperty("cgst", mUnits.getProduct().getTaxMaster().getCgst());
                selectedProduct.addProperty("code", mUnits.getProduct().getProductCode());
                selectedProduct.addProperty("current_stock", 0);
                selectedProduct.addProperty("hsn", mUnits.getProduct().getProductHsn().getHsnNumber());
                selectedProduct.addProperty("hsn", mUnits.getProduct().getTaxMaster().getIgst());
                selectedProduct.addProperty("is_batch", mUnits.getProduct().getIsBatchNumber());
                selectedProduct.addProperty("is_inventory", mUnits.getProduct().getIsInventory());
                selectedProduct.addProperty("is_serial", mUnits.getProduct().getIsSerialNumber());
                selectedProduct.addProperty("packing", mUnits.getProduct().getPackingMaster() != null ?
                        mUnits.getProduct().getPackingMaster().getPackName() : "");
                selectedProduct.addProperty("product_name", mUnits.getProduct().getProductName());
                selectedProduct.addProperty("sgst", mUnits.getProduct().getTaxMaster().getSgst());
                selectedProduct.addProperty("tax_type", mUnits.getProduct().getTaxType());
                selectedProduct.addProperty("unit", mUnits.getUnits().getUnitName());
                unitsJsonObjects.add("selectedProduct", selectedProduct);
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ? mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ? mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ? mUnits.getLevelC().getId().toString() : "");
                unitsJsonObjects.addProperty("packing", mUnits.getProduct() != null ? (mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "") : "");
                unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                Double returnqty = 0.0;
                unitsJsonObjects.addProperty("qty", mUnits.getQty());
                unitsJsonObjects.addProperty("returnable_qty", mUnits.getQty() - mUnits.getReturnQty());
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
                unitsJsonObjects.addProperty("transaction_status", mUnits.getTransactionStatus() != null ? mUnits.getTransactionStatus().getId().toString() : "");
                unitsJsonObjects.addProperty("inventoryId", 0);
                InventoryDetailsPostings inventoryDetailsPostings = inventoryDetailsPostingsRepository.findByPurInventoryRow(mUnits.getProduct().getId(), purchaseInvoice.getFiscalYear().getId(), purchaseInvoice.getOutlet().getId(), purchaseInvoice.getBranch() != null ? purchaseInvoice.getBranch().getId() : null, 1L, id, mUnits.getLevelA() != null ? mUnits.getLevelA().getId() : null, mUnits.getLevelB() != null ? mUnits.getLevelB().getId() : null, mUnits.getLevelC() != null ? mUnits.getLevelC().getId() : null, mUnits.getProductBatchNo() != null ? mUnits.getProductBatchNo().getId() : null, mUnits.getUnits().getId(), "CR");
                if (inventoryDetailsPostings != null) {
                    unitsJsonObjects.addProperty("inventoryId", inventoryDetailsPostings.getId());
                }
                if (mUnits.getProductBatchNo() != null) {
                    if (mUnits.getProductBatchNo().getExpiryDate() != null) {
                        LocalDate invDate = DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getInvoiceDate());
                        if (invDate.isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
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
                    unitsJsonObjects.addProperty("min_discount",
                            mUnits.getProductBatchNo().getMinDiscount() != null ? mUnits.getProductBatchNo().getMinDiscount().toString() : "");
                    unitsJsonObjects.addProperty("max_discount",
                            mUnits.getProductBatchNo().getMaxDiscount() != null ? mUnits.getProductBatchNo().getMaxDiscount().toString() : "");
                    unitsJsonObjects.addProperty("manufacturing_date", mUnits.getProductBatchNo().getManufacturingDate() != null ? mUnits.getProductBatchNo().getManufacturingDate().toString() : "");
                    unitsJsonObjects.addProperty("margin_per", mUnits.getProductBatchNo().getMinMargin() != null
                            ? mUnits.getProductBatchNo().getMinMargin().toString() : "");
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
                    unitsJsonObjects.addProperty("margin_per", "");
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
            /* End of Purchase Invoice Details */

            /***** Debitnote List of Purchase Invoice *****/
            List<TranxDebitNoteNewReferenceMaster> selectedBillList = new ArrayList<>();
            JsonArray billArray = new JsonArray();
            Long sundryCreditorId = purchaseInvoice.getSundryCreditors().getId();
            selectedBillList = tranxDebitNoteNewReferenceRepository.findSelectedBills(sundryCreditorId, true, "credit", purchaseInvoice.getId());
            if (selectedBillList != null && selectedBillList.size() > 0) {
                for (TranxDebitNoteNewReferenceMaster mTranxDebitNote : selectedBillList) {
                    if (mTranxDebitNote.getBalance() != 0.0) {
                        JsonObject data = new JsonObject();
                        data.addProperty("Total_amt", mTranxDebitNote.getTotalAmount());
                        data.addProperty("id", mTranxDebitNote.getId());
                        data.addProperty("debit_note_no", mTranxDebitNote.getDebitnoteNewReferenceNo());
                        data.addProperty("source", mTranxDebitNote.getSource());
                        data.addProperty("debit_remaining_amt", mTranxDebitNote.getBalance());
                        data.addProperty("debit_paid_amt", mTranxDebitNote.getTotalAmount());
                        data.addProperty("isSelected", mTranxDebitNote.getIsSelected() != null ? mTranxDebitNote.getIsSelected() : false);
                        data.addProperty("purchase_id", purchaseInvoice.getId());
                        billArray.add(data);
                    }
                }
            }
            /**** Barcode Data *****/
            List<ProductBarcode> barcodeList = barcodeRepository.findByTransactionIdAndStatus(purchaseInvoice.getId(), true);
            JsonArray barcodeJsonList = new JsonArray();
            for (ProductBarcode mBarcode : barcodeList) {
                JsonObject barcodeJsonObject = new JsonObject();
                barcodeJsonObject.addProperty("product_id", mBarcode.getProduct().getId());
                barcodeJsonObject.addProperty("product_name", mBarcode.getProduct().getProductName());
                barcodeJsonObject.addProperty("barcode_id", mBarcode.getId());
                barcodeJsonObject.addProperty("barcode_no", mBarcode.getBarcodeUniqueCode());
                barcodeJsonObject.addProperty("batch_id", mBarcode.getProductBatch() != null ? mBarcode.getProductBatch().getId().toString() : "");
                barcodeJsonObject.addProperty("batch_no", mBarcode.getProductBatch() != null ? mBarcode.getProductBatch().getBatchNo() : "");
                barcodeJsonObject.addProperty("mrp", mBarcode.getMrp());
                barcodeJsonObject.addProperty("tranx_date", mBarcode.getTranxDate().toString());
                barcodeJsonObject.addProperty("transaction_id", mBarcode.getTransactionId());
                barcodeJsonObject.addProperty("packing_id", mBarcode.getPackingMaster() != null ? mBarcode.getPackingMaster().getId().toString() : "");
                barcodeJsonObject.addProperty("packing_name", mBarcode.getProduct().getPackingMaster() != null ? mBarcode.getProduct().getPackingMaster().getPackName() : "");
                barcodeJsonObject.addProperty("units_id", mBarcode.getUnits() != null ? mBarcode.getUnits().getId().toString() : "");
                barcodeJsonObject.addProperty("units_name", mBarcode.getUnits() != null ? mBarcode.getUnits().getUnitName() : "");

                barcodeJsonObject.addProperty("levela_id", mBarcode.getLevelA() != null ? mBarcode.getLevelA().getId().toString() : "");
                barcodeJsonObject.addProperty("levela_name", mBarcode.getLevelA() != null ? mBarcode.getLevelA().getLevelName() : "");

                barcodeJsonObject.addProperty("levelb_id", mBarcode.getLevelB() != null ? mBarcode.getLevelB().getId().toString() : "");

                barcodeJsonObject.addProperty("levelb_name", mBarcode.getLevelB() != null ? mBarcode.getLevelB().getLevelName() : "");

                barcodeJsonObject.addProperty("levelc_id", mBarcode.getLevelC() != null ? mBarcode.getLevelC().getId().toString() : "");

                barcodeJsonObject.addProperty("levelc_name", mBarcode.getLevelC() != null ? mBarcode.getLevelC().getLevelName() : "");
                barcodeJsonObject.addProperty("print_qty", 0);
                barcodeJsonObject.addProperty("product_qty", mBarcode.getQnty() != null ? mBarcode.getQnty() : 0);
                barcodeJsonList.add(barcodeJsonObject);
            }
            /* Purchase Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            additionalCharges = purInvoiceAdditionalChargesRepository.findByPurchaseTransactionIdAndStatus(purchaseInvoice.getId(), true);
            if (additionalCharges.size() > 0) {
                for (TranxPurInvoiceAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledgerId", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    json_charges.addProperty("percent", mAdditionalCharges.getPercent() != null ? mAdditionalCharges.getPercent() : 0.0);
                    jsonAdditionalList.add(json_charges);
                }
            }
            finalResult.add("row", row);
            finalResult.add("invoice_data", result);
            finalResult.add("bills", billArray);
            finalResult.add("barcode_list", barcodeJsonList);
            finalResult.add("additionalCharges", jsonAdditionalList);
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Error in getPurchaseInvoiceById  :" + exceptionAsString);

        } catch (Exception e1) {
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Error in getPurchaseInvoiceById  :" + exceptionAsString);
        }
        return finalResult;
    }

    public Object editPurchaseInvoice(MultipartHttpServletRequest request) {
        return saveIntoPurchaseInvoiceEdit(request, "edit");
    }

    private ResponseMessage saveIntoPurchaseInvoiceEdit(MultipartHttpServletRequest request, String key) {
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseMessage = new ResponseMessage();
        TransactionTypeMaster tranxType = null;
        TranxPurInvoice invoiceTranx = null;
        LedgerMaster discountLedger = null;
        LedgerMaster sundryCreditors = null;
        LedgerMaster purchaseAccount = null;
        LedgerMaster roundoff = null;
        TranxPurInvoice mPurchaseTranx = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRS");
        invoiceTranx = tranxPurInvoiceRepository.findByIdAndOutletIdAndStatus(Long.parseLong(
                request.getParameter("id")), users.getOutlet().getId(), true);
        Date purDate = invoiceTranx.getInvoiceDate();
        LocalDate mDate = LocalDate.parse(request.getParameter("transaction_date"));
        if (invoiceTranx != null) {
            dbList = ledgerTransactionPostingsRepository.findByTransactionId(invoiceTranx.getId(), tranxType.getId());
            ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(invoiceTranx.getId(),
                    tranxType.getId(), true);
            invoiceTranx.setOperations("updated");
            Boolean taxFlag;

            LocalDate invoiceDate = DateConvertUtil.convertStringToLocalDate(request.getParameter("invoice_date"));
            Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("invoice_date"));
            if (invoiceDate.isEqual(DateConvertUtil.convertDateToLocalDate(invoiceTranx.getInvoiceDate()))) {
                strDt = invoiceTranx.getInvoiceDate();
            }
            invoiceTranx.setInvoiceDate(strDt);

            /* fiscal year mapping */
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(invoiceDate);
            if (fiscalYear != null) {
                invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
            }
            /* End of fiscal year mapping */
            invoiceTranx.setVendorInvoiceNo(request.getParameter("invoice_no"));
            purchaseAccount = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("purchase_id")), users.getOutlet().getId(), true);
            /* calling store procedure for updating opening and closing  of Purchase Account  */
            if (purchaseAccount.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {
                Boolean isContains = dbList.contains(purchaseAccount.getId());
                Boolean isLedgerContains = ledgerList.contains(purchaseAccount.getId());
                mInputList.add(purchaseAccount.getId());
                ledgerInputList.add(purchaseAccount.getId());
                if (isContains) {
                    /* edit ledger tranx if same ledger is modified */
                    /**** New Postings Logic *****/
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(purchaseAccount.getId(), tranxType.getId(), invoiceTranx.getId());
                    if (mLedger != null) {
                        //mLedger.setAmount(Double.parseDouble(request.getParameter("total_base_amt")));
                        mLedger.setAmount(Double.parseDouble(request.getParameter("taxable_amount")));
                        mLedger.setTransactionDate(strDt);
                        mLedger.setOperations("Updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    /* insert ledger tranx if ledger is changed */
                    /**** New Postings Logic *****/
                    //  ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("total_base_amt")), purchaseAccount, tranxType, purchaseAccount.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), date, invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");
                    ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("taxable_amount")),
                            purchaseAccount, tranxType, purchaseAccount.getAssociateGroups(), fiscalYear,
                            invoiceTranx.getBranch(), invoiceTranx.getOutlet(), strDt, invoiceTranx.getId(),
                            invoiceTranx.getVendorInvoiceNo(), "DR", true,
                            "Purchase Invoice", "Insert");
                }

                Double amount = Double.parseDouble(request.getParameter("taxable_amount"));
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getPurchaseAccountLedger().getId(),
                        tranxType, "DR", invoiceTranx.getId(), purchaseAccount, strDt, fiscalYear, invoiceTranx.getOutlet(),
                        invoiceTranx.getBranch(), invoiceTranx.getTranxCode());

            }
            /* end of calling  procedure for Purchase Account updating opening and closing */
            if (Double.parseDouble(request.getParameter("purchase_discount_amt")) > 0) {
                if (users.getBranch() == null)
                    discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("purchase discount", users.getOutlet().getId(), true);
                else
                    discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("purchase discount", users.getOutlet().getId(), users.getBranch().getId(), true);
                if (discountLedger != null) {
                    invoiceTranx.setPurchaseDiscountLedger(discountLedger);
                }
                if (discountLedger != null) {
                    invoiceTranx.setPurchaseDiscountLedger(discountLedger);
                    /* calling store procedure for updating opening and closing of Purchase Discount  */
                  /*  if (discountLedger.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {
                        Boolean isContains = dbList.contains(discountLedger.getId());
                        mInputList.add(discountLedger.getId());
                        if (isContains) {
                            //transactionDetailsRepository.ledgerPostingEdit(discountLedger.getId(), invoiceTranx.getId(), tranxType.getId(), "CR", Double.parseDouble(request.getParameter("total_purchase_discount_amt")));
                            *//**** New Postings Logic *****//*
                            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                                    findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(discountLedger.getId(), tranxType.getId(), invoiceTranx.getId());
                            if (mLedger != null) {
                                mLedger.setAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
                                mLedger.setTransactionDate(date);
                                mLedger.setOperations("updated");
                                ledgerTransactionPostingsRepository.save(mLedger);
                            }
                        } else {
                            *//**** New Postings Logic *****//*
                            ledgerCommonPostings.callToPostings(Double.parseDouble(
                                            request.getParameter("purchase_discount_amt")), discountLedger,
                                    tranxType, discountLedger.getAssociateGroups(), fiscalYear,
                                    invoiceTranx.getBranch(), invoiceTranx.getOutlet(), date, invoiceTranx.getId(),
                                    invoiceTranx.getVendorInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
                        }
                    }*/
                    /* end of calling store procedure for updating opening and closing of Purchase Discount */
                }
            }
            System.out.println("supplierCodeId " + request.getParameter("supplier_code_id"));
            sundryCreditors = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), users.getOutlet().getId(), true);
            /* calling store procedure for updating opening and closing balance of Sundry Creditors  */
            if (sundryCreditors.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {

                Boolean isContains = dbList.contains(sundryCreditors.getId());
                Boolean isLedgerContains = ledgerList.contains(sundryCreditors.getId());
                mInputList.add(sundryCreditors.getId());
                ledgerInputList.add(sundryCreditors.getId());
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(sundryCreditors.getId(), tranxType.getId(), invoiceTranx.getId());
                if (isContains) {
                    //    transactionDetailsRepository.ledgerPostingEdit(sundryCreditors.getId(), invoiceTranx.getId(), tranxType.getId(), "CR", Double.parseDouble(request.getParameter("totalamt")));
                    /**** New Postings Logic *****/
                  /*  LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                            findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(sundryCreditors.getId(), tranxType.getId(), invoiceTranx.getId());*/
                    if (mLedger != null) {
                        mLedger.setAmount(Double.parseDouble(request.getParameter("bill_amount")));
                        mLedger.setTransactionDate(strDt);
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("bill_amount")), sundryCreditors, tranxType, sundryCreditors.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), strDt, invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
                    if (mLedger != null) {
                        mLedger.setAmount(mLedger.getAmount() - Double.parseDouble(request.getParameter("bill_amount")));
                        mLedger.setTransactionDate(strDt);
                        mLedger.setOperations("replaced");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                }


                Double amount = Double.parseDouble(request.getParameter("bill_amount"));
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSundryCreditors().getId(),
                        tranxType, "CR", invoiceTranx.getId(), sundryCreditors, strDt, fiscalYear, invoiceTranx.getOutlet(),
                        invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
            }  /* end of calling store procedure for updating opening and closing of Sundry Creditors  */

            invoiceTranx.setPurchaseAccountLedger(purchaseAccount);
            invoiceTranx.setSundryCreditors(sundryCreditors);
//            LocalDate mDate = LocalDate.parse(request.getParameter("transaction_date"));
            invoiceTranx.setTransactionDate(mDate);

            if (paramMap.containsKey("gstNo")) {
                if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                    invoiceTranx.setGstNumber(request.getParameter("gstNo"));
                }
            }
            invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
            invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
            //   roundoff = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId("Round off", users.getOutlet().getId());
            if (users.getBranch() != null)
                roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
            else roundoff = ledgerMasterRepository.findRoundOff(users.getOutlet().getId(), "Round off");
            invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
            invoiceTranx.setPurchaseRoundOff(roundoff);
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
                mInputList.add(roundoff.getId());
                //     transactionDetailsRepository.ledgerPostingEdit(roundoff.getId(), invoiceTranx.getId(), tranxType.getId(), crdr, rf * -1);
                /**** New Postings Logic *****/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(roundoff.getId(), tranxType.getId(), invoiceTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(Math.abs(rf));
                    mLedger.setTransactionDate(strDt);
                    mLedger.setLedgerType(crdr);
                    mLedger.setOperations("updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                if (rf >= 0) {
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(Math.abs(rf), roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), strDt, invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "DR", true, tranxType.getTransactionCode(), "Insert");
                } else {
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(Math.abs(rf), roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), strDt, invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "CR", true, tranxType.getTransactionCode(), "Insert");
                }
            }


            /**** NEW METHOD FOR LEDGER POSTING ****/
            String tranxAction = "CR";
            if (rf >= 0)
                tranxAction = "DR";
            Double amount = Math.abs(rf);
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getPurchaseRoundOff().getId(),
                    tranxType, tranxAction, invoiceTranx.getId(), roundoff, strDt, fiscalYear, invoiceTranx.getOutlet(),
                    invoiceTranx.getBranch(), invoiceTranx.getTranxCode());

            /* end of inserting into Sundry Creditors JSON Object */

            invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
            invoiceTranx.setBalance(Double.parseDouble(request.getParameter("bill_amount")));
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

            invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
            invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
            if (paramMap.containsKey("tcs")) invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
            invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
            invoiceTranx.setPurchaseDiscountPer(Double.parseDouble(request.getParameter("purchase_discount")));
            invoiceTranx.setPurchaseDiscountAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
            invoiceTranx.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
            invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
            invoiceTranx.setSrno(Long.parseLong(request.getParameter("purchase_sr_no")));
            invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
            invoiceTranx.setUpdatedBy(users.getId());
            invoiceTranx.setStatus(true);
            if (paramMap.containsKey("paymentMode")) invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
            else invoiceTranx.setPaymentMode("");
            invoiceTranx.setNarration(request.getParameter("narration"));
           /* if (paramMap.containsKey("additionalChgLedger1")) {
                LedgerMaster additionalChgLedger1 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger1")), users.getOutlet().getId(), true);
                if (additionalChgLedger1 != null) {
                    Double addCharges = Double.parseDouble(request.getParameter("addChgLedgerAmt1"));
                    invoiceTranx.setAdditionLedger1(additionalChgLedger1);
                    invoiceTranx.setAdditionLedgerAmt1(addCharges);
                }
            }
            if (paramMap.containsKey("additionalChgLedger2")) {
                LedgerMaster additionalChgLedger2 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger2")), users.getOutlet().getId(), true);
                if (additionalChgLedger2 != null) {
                    Double addCharges = Double.valueOf(request.getParameter("addChgLedgerAmt2"));
                    mInputList.add(additionalChgLedger2.getId());
                    invoiceTranx.setAdditionLedger2(additionalChgLedger2);
                    invoiceTranx.setAdditionLedgerAmt2(addCharges);
                }
            }*/
            if (paramMap.containsKey("additionalChgLedger3")) {
                LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
                if (additionalChgLedger3 != null) {
                    Double addCharges = Double.valueOf(request.getParameter("addChgLedgerAmt3"));
                    mInputList.add(additionalChgLedger3.getId());
                    invoiceTranx.setAdditionLedger3(additionalChgLedger3);
                    invoiceTranx.setAdditionLedgerAmt3(addCharges);

                    isContains = dbList.contains(additionalChgLedger3.getId());
                    mInputList.add(additionalChgLedger3.getId());
                    if (isContains) {
                        /**** New Postings Logic *****/
                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(additionalChgLedger3.getId(), tranxType.getId(), invoiceTranx.getId());
                        if (mLedger != null) {
                            mLedger.setAmount(addCharges);
                            mLedger.setTransactionDate(strDt);
                            mLedger.setOperations("updated");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    } else {
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("addChgLedgerAmt3")), additionalChgLedger3, tranxType, additionalChgLedger3.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), strDt, invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), addCharges > 0 ? "DR" : "CR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
            if (paramMap.containsKey("isRoundOffCheck"))
                invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));
            /**** Upload Transaction Invoice for future reference *****/
            if (request.getFile("image") != null) {
                MultipartFile image = request.getFile("image");
                fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                if (imagePath != null) {
                    invoiceTranx.setImagePath(File.separator + "uploads" + File.separator + imagePath);
                }
            }
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
            } else {
                invoiceTranx.setTcsAmt(0.0);
                invoiceTranx.setTcs(0.0);
                invoiceTranx.setTdsAmt(0.0);
                invoiceTranx.setTdsPer(0.0);
                invoiceTranx.setTcsMode("");
            }
            try {
                mPurchaseTranx = tranxPurInvoiceRepository.save(invoiceTranx);
                if (mPurchaseTranx != null) {
                    /* adjust debit note bill against purchase invoice */
                    if (paramMap.containsKey("debitNoteReference")) {
                        if (Boolean.parseBoolean(request.getParameter("debitNoteReference"))) {
                            String jsonStr = request.getParameter("bills");
                            JsonParser parser = new JsonParser();
                            JsonElement debitNoteBills = parser.parse(jsonStr);
                            JsonArray debitNotes = debitNoteBills.getAsJsonArray();
                            Double totalBalance = 0.0;
                            for (JsonElement mBill : debitNotes) {
                                TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = null;
                                JsonObject mDebitNote = mBill.getAsJsonObject();
                                totalBalance += mDebitNote.get("debitNotePaidAmt").getAsDouble();
                                tranxDebitNoteNewReference = tranxDebitNoteNewReferenceRepository.findByIdAndStatus(mDebitNote.get("debitNoteId").getAsLong(), true);
                                TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                tranxDebitNoteNewReference.setTotalAmount(mDebitNote.get("debitNotePaidAmt").getAsDouble());
                                if (mDebitNote.get("debitNotePaidAmt").getAsDouble() > 0) {
                                    tranxDebitNoteNewReference.setAdjustedId(mPurchaseTranx.getId());
                                    tranxDebitNoteNewReference.setIsSelected(true);
                                }
                                if (mDebitNote.get("debitNoteRemaningAmt").getAsDouble() == 0) {
                                    tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
                                }
                                tranxDebitNoteNewReference.setBalance(mDebitNote.get("debitNoteRemaningAmt").getAsDouble());
                                TranxDebitNoteNewReferenceMaster newReferenceMaster = tranxDebitNoteNewReferenceRepository.save(tranxDebitNoteNewReference);
                                /* Adding int Debit Note Details */
                                TranxDebitNoteDetails mDetails = new TranxDebitNoteDetails();
                                mDetails.setBranch(newReferenceMaster.getBranch());
                                mDetails.setOutlet(newReferenceMaster.getOutlet());
                                mDetails.setSundryCreditor(newReferenceMaster.getSundryCreditor());
                                mDetails.setTotalAmount(newReferenceMaster.getTotalAmount());
                                mDetails.setPaidAmt(mDebitNote.get("debitNotePaidAmt").getAsDouble());
                                mDetails.setAdjustedId(mPurchaseTranx.getId());
                                mDetails.setAdjustedSource("purchase_invoice");
                                mDetails.setOperations("adjust");
                                mDetails.setTranxDebitNoteMaster(newReferenceMaster);
                                mDetails.setStatus(true);
                                mDetails.setAdjustmentStatus(newReferenceMaster.getAdjustmentStatus());
                                // immediate
                                tranxDebitNoteDetailsRepository.save(mDetails);
                            }
                            mPurchaseTranx.setBalance(mPurchaseTranx.getTotalAmount() - totalBalance);
                            tranxPurInvoiceRepository.save(mPurchaseTranx);

                        }
                    }
                    /**** for PurchaseInvoice Details Edit ****/
                    String jsonStr = request.getParameter("row");
                    JsonParser parser = new JsonParser();
                    JsonElement purDetailsJson = parser.parse(jsonStr);
                    JsonArray array = purDetailsJson.getAsJsonArray();
                    String rowsDeleted = "";
                    if (paramMap.containsKey("rowDelDetailsIds"))
                        rowsDeleted = request.getParameter("rowDelDetailsIds");
                    saveIntoPurchaseInvoiceDetailsEdit(array, mPurchaseTranx, users.getBranch() != null ?
                            users.getBranch() : null, users.getOutlet(), users.getId(), rowsDeleted, tranxType, purDate);
                    /**** end of PurchaseInvoice DetailsEdit ****/

                    /**** for Purchase Duties and Texes Edit ****/
                    String taxStr = request.getParameter("taxCalculation");
                    JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                    saveIntoPurchaseDutiesTaxesEdit(duties_taxes, mPurchaseTranx, taxFlag, tranxType, users.getOutlet().getId(), users.getId());
                    /**** end of Purchase Duties and Texes Edit ****/

                    /* Remove all ledgers from DB if we found new input ledger id's while updating */
                    for (Long mDblist : dbList) {
                        if (!mInputList.contains(mDblist)) {
                            /**** New Postings Logic *****/
                            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mDblist, tranxType.getId(), invoiceTranx.getId());
                            if (mLedger != null) {
                                mLedger.setAmount(0.0);
                                mLedger.setTransactionDate(strDt);
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
                            saveIntoPurchaseAdditionalChargesEdit(additionalCharges, mPurchaseTranx, tranxType, users.getOutlet().getId(), acRowsDeleted);
                        }
                    }
                    /*** delete additional charges if removed from frontend ****/
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
                    /**** Additional Charges Update End ***/
                    /* Remove all ledgers from DB if we found new input ledger id's while updating */
                    for (Long mDblist : dbList) {
                        if (!mInputList.contains(mDblist)) {
                            // transactionDetailsRepository.ledgerPostingRemove(mDblist, invoiceTranx.getId(), tranxType.getId());
                            /**** New Postings Logic *****/
                            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mDblist, tranxType.getId(), invoiceTranx.getId());
                            if (mLedger != null) {
                                mLedger.setAmount(0.0);
                                mLedger.setTransactionDate(strDt);
                                mLedger.setOperations("removed");
                                ledgerTransactionPostingsRepository.save(mLedger);
                            }
                        }
                    }

                    /* Remove all ledgers from DB if we found new input ledger id's while updating */
                    for (Long mDblist : ledgerList) {
                        if (!ledgerInputList.contains(mDblist)) {
                            purInvoiceLogger.info("removing unused previous ledger ::" + mDblist);
                            LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                                    mDblist, tranxType.getId(), mPurchaseTranx.getId(), true);
                            if (ledgerDetail != null) {
                                Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                                ledgerDetail.setAmount(0.0);
                                ledgerDetail.setClosingAmount(closing);
                                ledgerDetail.setStatus(false);
                                LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                                /***** NEW METHOD FOR LEDGER POSTING *****/
                                postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), mPurchaseTranx.getInvoiceDate(), tranxType, mPurchaseTranx.getFiscalYear(),
                                        detail);
                            }
                            purInvoiceLogger.info("removing unused previous ledger update done");
                        }
                    }


                    /***** update Payment against the Purchase ****/
                    if (mPurchaseTranx.getPaymentMode().equalsIgnoreCase("cash")) {
                        Double cashAmt = mPurchaseTranx.getTotalAmount();
                        updatePaymentInvoice(mPurchaseTranx, users, cashAmt, 0.0, "cash", "update");

                    }
                }
                responseMessage.setMessage("Purchase invoice updated successfully");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            } catch (DataIntegrityViolationException e1) {
                responseMessage.setMessage("error");
                responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());

                StringWriter sw = new StringWriter();
                e1.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Error in saveIntoPurchaseInvoiceEdit  :" + exceptionAsString);
            } catch (Exception e) {
                responseMessage.setMessage("error");
                responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Error in saveIntoPurchaseInvoiceEdit  :" + exceptionAsString);
            }
        }
        return responseMessage;

    }

    private void updatePaymentInvoice(TranxPurInvoice mPurchaseTranx, Users users, Double totalAmount, double v,
                                      String cash, String key) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PMT");
        TranxPaymentMaster tranxPaymentMaster = tranxPaymentMasterRepository.findByTranxIdAndStatus(mPurchaseTranx.getId(), true);
        Long count = 0L;
        if (mPurchaseTranx.getBranch() != null) tranxPaymentMaster.setBranch(mPurchaseTranx.getBranch());
        if (mPurchaseTranx.getFiscalYear() != null) tranxPaymentMaster.setFiscalYear(mPurchaseTranx.getFiscalYear());
        tranxPaymentMaster.setTranscationDate(mPurchaseTranx.getInvoiceDate());
        tranxPaymentMaster.setTotalAmt(totalAmount);

        TranxPaymentMaster newTranxReceiptMaster = tranxPaymentMasterRepository.save(tranxPaymentMaster);
        LedgerMaster sundryCreditor = mPurchaseTranx.getSundryCreditors();
        updateIntoPaymentPerticualrs(newTranxReceiptMaster, sundryCreditor, "SC", totalAmount, key, cash,
                tranxType, mPurchaseTranx);
        String payMode = "Cash";
        if (totalAmount > 0.0) {
            payMode = "Cash";
            LedgerMaster ledgerMaster = null;
            if (mPurchaseTranx.getBranch() != null) {
                ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndBranchIdAndStatus("CAIH", mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId(), true);
            } else {
                ledgerMaster = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull("CAIH", mPurchaseTranx.getOutlet().getId(), true);
            }
            updateIntoPaymentPerticualrs(newTranxReceiptMaster, ledgerMaster, "others", totalAmount, key, cash, tranxType, mPurchaseTranx);
        }

    }

    private void updateIntoPaymentPerticualrs(TranxPaymentMaster newTranxReceiptMaster,
                                              LedgerMaster ledgerMaster, String type, Double totalAmount, String key,
                                              String cash, TransactionTypeMaster tranxType, TranxPurInvoice mPurchaseTranx) {
        String ledgerType = "";
       /* TranxPaymentPerticulars mTranxPerticular = tranxPaymentPerticularRepository.
                findByTranxPaymentMasterIdAndOutletIdAndStatusAndTypeIgnoreCase(newTranxReceiptMaster.getId(),
                        newTranxReceiptMaster.getOutlet().getId(),true, type);*/
        TranxPaymentPerticulars mTranxPerticular = tranxPaymentPerticularRepository.
                findByTranxPaymentMasterIdAndStatusAndLedgerTypeIgnoreCaseAndLedgerMasterId(newTranxReceiptMaster.getId(), true, type, ledgerMaster.getId());
        if (mTranxPerticular != null) {
            mTranxPerticular.setLedgerMaster(ledgerMaster);
            mTranxPerticular.setLedgerType(type);
            mTranxPerticular.setLedgerName(ledgerMaster.getLedgerName());
            if (type.equalsIgnoreCase("SC")) {
                ledgerType = "CR";
                mTranxPerticular.setDr(0.0);
                mTranxPerticular.setCr(totalAmount);
                mTranxPerticular.setType("cr");
                mTranxPerticular.setPayableAmt(totalAmount);
                mTranxPerticular.setSelectedAmt(totalAmount);
                mTranxPerticular.setRemainingAmt(newTranxReceiptMaster.getTotalAmt() - totalAmount);
                mTranxPerticular.setIsAdvance(false);
            } else {
                ledgerType = "DR";
                mTranxPerticular.setDr(totalAmount);
                mTranxPerticular.setCr(0.0);
                mTranxPerticular.setType("dr");
            }
            mTranxPerticular.setPaymentMethod(cash);
            mTranxPerticular.setTransactionDate(DateConvertUtil.convertDateToLocalDate(newTranxReceiptMaster.getTranscationDate()));
            mTranxPerticular.setCreatedBy(newTranxReceiptMaster.getCreatedBy());
            TranxPaymentPerticulars receiptPerticulars = tranxPaymentPerticularRepository.save(mTranxPerticular);
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(ledgerMaster.getId(), tranxType.getId(), newTranxReceiptMaster.getId());
            if (mLedger != null) {
                mLedger.setAmount(totalAmount);
                mLedger.setTransactionDate(newTranxReceiptMaster.getTranscationDate());
                mLedger.setOperations("updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            } else {
                ledgerCommonPostings.callToPostings(totalAmount, ledgerMaster, tranxType, ledgerMaster.getAssociateGroups(), newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getBranch(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getTranscationDate(), newTranxReceiptMaster.getId(), newTranxReceiptMaster.getPaymentNo(), ledgerType, true, tranxType.getTransactionCode(), "Insert");
            }

            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.callToPostingLedgerForUpdateByDetailsId(totalAmount, ledgerMaster.getId(), tranxType,
                    ledgerType, newTranxReceiptMaster.getId(), ledgerMaster, newTranxReceiptMaster.getTranscationDate(),
                    newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getBranch(),
                    newTranxReceiptMaster.getTranxCode());

            /*** Insert into Bill Details of Receipt: receipt against the sales invoice *****/
            if (type.equalsIgnoreCase("SC")) {
                InsertIntoBillDetails(receiptPerticulars, key, mPurchaseTranx);
            }
        }
    }

    /* for Purchase Invoice Details Edit */
    public void saveIntoPurchaseInvoiceDetailsEdit(JsonArray array,
                                                   TranxPurInvoice mPurchaseTranx,
                                                   Branch branch, Outlet outlet, Long userId, String rowsDeleted,
                                                   TransactionTypeMaster tranxType, Date purDate) {

        List<TranxPurchaseInvoiceProductSrNumber> newSerialNumbers = new ArrayList<>();
        /* Purchase Product Details Start here */
        for (JsonElement mList : array) {
            JsonObject object = mList.getAsJsonObject();
            Double tranxQty = 0.0;
            /* Purchase Invoice Unit Edit */
            Long details_id = object.get("details_id").getAsLong();
            TranxPurInvoiceDetailsUnits invoiceUnits = new TranxPurInvoiceDetailsUnits();
            if (details_id != 0) {
                invoiceUnits = tranxPurInvoiceUnitsRepository.findByIdAndStatus(details_id, true);
                tranxQty = invoiceUnits.getQty();
            } else {
                invoiceUnits.setReturnQty(0.0);
                TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
                invoiceUnits.setTransactionStatus(transactionStatus);
                tranxQty = object.get("qty").getAsDouble();
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
            Double free_qty = 0.0;
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
            invoiceUnits.setPurchaseTransaction(mPurchaseTranx);
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

            /******* Insert into Product Batch No ****/
            boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    Double qty = object.get("qty").getAsDouble();


                    double net_amt = object.get("final_amt").getAsDouble();
                    double costing = 0;
                    double costing_with_tax = 0;
                    /***** if org_b_details_id !=0 then reverese the inventory postings against the org_b_details_id *****/
                    if (object.has("org_b_details_id")) {
                        if (object.get("org_b_details_id").getAsLong() != 0) {
                            /***** new architecture of Inventory Postings *****/
                            inventoryCommonPostings.callToInventoryPostings("DR", mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), invoiceUnits.getQty(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), invoiceUnits.getProduct(), tranxType, levelA, levelB, levelC, invoiceUnits.getUnits(), invoiceUnits.getProductBatchNo(), invoiceUnits.getProductBatchNo() != null ? invoiceUnits.getProductBatchNo().getBatchNo() : null, mPurchaseTranx.getFiscalYear(), null);
                        }
                    }
                    productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                    // productBatchNo.setQnty(object.get("qty").getAsInt());
                    productBatchNo.setQnty(qty.intValue());
                    productBatchNo.setFreeQty(free_qty);
                    if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equalsIgnoreCase(""))
                        productBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                    if (object.has("b_no")) productBatchNo.setBatchNo(object.get("b_no").getAsString());
                    if (object.has("b_rate") && !object.get("b_rate").getAsString().equalsIgnoreCase(""))
                        productBatchNo.setMrp(object.get("b_rate").getAsDouble());
                    //   if (object.has("b_sale_rate")) productBatchNo.setSalesRate(object.get("b_sale_rate").getAsDouble());
                    if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").getAsString().equalsIgnoreCase(""))
                        productBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());

                    if (object.has("b_expiry") && object.get("b_expiry")!=null &&
                    !object.get("b_expiry").getAsString().equalsIgnoreCase("") &&
                            !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                        productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                    if (object.has("manufacturing_date") &&
                            !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") &&
                            !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                        productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                    if (object.has("costing") && !object.get("costing").isJsonNull())
                        productBatchNo.setCosting(object.get("costing").getAsDouble());
                    if (object.has("costing_with_tax") && !object.get("costing_with_tax").isJsonNull())
                        productBatchNo.setCostingWithTax(object.get("costing_with_tax").getAsDouble());
//                    if (object.has("rate_a") && !object.get("rate_a").getAsString().equalsIgnoreCase(""))
//                        productBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                 /*   if (object.has("rate_b") && !object.get("rate_b").getAsString().equalsIgnoreCase(""))
                        productBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                    if (object.has("rate_c") && !object.get("rate_c").getAsString().equalsIgnoreCase(""))
                        productBatchNo.setMinRateC(object.get("rate_c").getAsDouble());*/
                    if (object.has("margin_per") &&
                            !object.get("margin_per").getAsString().equals(""))
                        productBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
                    productBatchNo.setProduct(mProduct);
                    productBatchNo.setOutlet(outlet);
                    productBatchNo.setBranch(branch);
                    productBatchNo.setUnits(units);
                    if (levelA != null) productBatchNo.setLevelA(levelA);
                    if (levelB != null) productBatchNo.setLevelB(levelB);
                    if (levelC != null) productBatchNo.setLevelC(levelC);
                    productBatchNo.setSupplierId(mPurchaseTranx.getSundryCreditors().getId());
                    productBatchNo = productBatchNoRepository.save(productBatchNo);
                    batchNo = productBatchNo.getBatchNo();
                    batchId = productBatchNo.getId();
                }
                invoiceUnits.setProductBatchNo(productBatchNo);
                tranxPurInvoiceUnitsRepository.save(invoiceUnits);

                /**** Save this rate into Product Master if purchase rate has been changed by customer
                 (non batch case only) ****/
                Double prrate = object.get("rate").getAsDouble();
                Double costing = object.get("costing").getAsDouble();
                Double costingWithTax = object.get("costing_with_tax").getAsDouble();
                ProductUnitPacking mUnitPackaging = productUnitRepository.findRate(mProduct.getId(), levelAId, levelBId, levelCId, units.getId(), true);
                if (mUnitPackaging != null) {
                    if (mUnitPackaging.getPurchaseRate() != null && mUnitPackaging.getPurchaseRate() != 0 && prrate != mUnitPackaging.getPurchaseRate()) {
                        mUnitPackaging.setPurchaseRate(prrate);
                        mUnitPackaging.setCosting(costing);
                        mUnitPackaging.setCostingWithTax(costingWithTax);
                        productUnitRepository.save(mUnitPackaging);
                    }
                }
                /******* Insert into Tranx Product Serial Numbers  ******/
                JsonArray jsonArray = object.getAsJsonArray("serialNo");
                if (jsonArray != null && jsonArray.size() > 0) {
                    List<TranxPurchaseInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        JsonObject jsonSrno = jsonElement.getAsJsonObject();
                        serialNo = jsonSrno.get("serial_no").getAsString();
                        Long detailsId = jsonSrno.get("serial_detail_id").getAsLong();
                        if (detailsId == 0) {
                            TranxPurchaseInvoiceProductSrNumber productSerialNumber = new TranxPurchaseInvoiceProductSrNumber();
                            productSerialNumber.setProduct(mProduct);
                            productSerialNumber.setSerialNo(serialNo);
                            // productSerialNumber.setPurchaseTransaction(mPurchaseTranx);
                            productSerialNumber.setTransactionStatus("Purchase");
                            productSerialNumber.setStatus(true);
                            productSerialNumber.setCreatedBy(userId);
                            productSerialNumber.setOperations("Inserted");
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setBranch(mPurchaseTranx.getBranch());
                            productSerialNumber.setOutlet(mPurchaseTranx.getOutlet());
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setUnits(units);
                            productSerialNumber.setTranxPurInvoiceDetailsUnits(invoiceUnits);
                            productSerialNumber.setLevelA(levelA);
                            productSerialNumber.setLevelB(levelB);
                            productSerialNumber.setLevelC(levelC);
                            productSerialNumber.setUnits(units);
                            TranxPurchaseInvoiceProductSrNumber mSerialNo = serialNumberRepository.save(productSerialNumber);
                            if (mProduct.getIsInventory()) {
                                inventoryCommonPostings.callToInventoryPostings("CR", mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), tranxQty + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear(), serialNo);
                            }
                        } else {
                            TranxPurchaseInvoiceProductSrNumber productSerialNumber1 = serialNumberRepository.findByIdAndStatus(detailsId, true);
                            productSerialNumber1.setSerialNo(serialNo);
                            productSerialNumber1.setUpdatedBy(userId);
                            productSerialNumber1.setOperations("Updated");
                            TranxPurchaseInvoiceProductSrNumber mSerialNo = serialNumberRepository.save(productSerialNumber1);
                            inventoryCommonPostings.callToEditInventoryPostings(mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), tranxQty + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear());
                        }

                    }
                } else {
                    /**** Inventory Postings *****/
                    if (mProduct.getIsInventory() && flag) {
                        /***** new architecture of Inventory Postings *****/
                        if (details_id != 0) {
                            inventoryCommonPostings.callToEditInventoryPostings(mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), tranxQty + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear());
                            /**
                             * @implNote New Logic of Inventory Posting
                             * @auther ashwins@opethic.com
                             * @version sprint 2
                             * Case 1: Modify QTY
                             **/
                            InventorySummaryTransactionDetails productRow = stkTranxDetailsRepository.
                                    findByProductIdAndTranxTypeIdAndTranxId(
                                            mProduct.getId(), tranxType.getId(), mPurchaseTranx.getId());
                            if (productRow != null) {
                                if (mPurchaseTranx.getInvoiceDate().compareTo(productRow.getTranxDate()) == 0 &&
                                        tranxQty.doubleValue() != object.get("qty").getAsDouble()) { //DATE SAME AND QTY DIFFERENT
                                    Double closingStk = closingUtility.CAL_CR_STOCK(productRow.getOpeningStock(), object.get("qty").getAsDouble(), free_qty);
                                    productRow.setQty(object.get("qty").getAsDouble() + free_qty);
                                    productRow.setClosingStock(closingStk);
                                    InventorySummaryTransactionDetails mInventory =
                                            stkTranxDetailsRepository.save(productRow);
                                    closingUtility.updatePosting(mInventory, mProduct.getId(), mPurchaseTranx.getInvoiceDate());
                                } else if (mPurchaseTranx.getInvoiceDate().compareTo(productRow.getTranxDate()) != 0) { // DATE IS DIFFERENT
                                    Date oldDate = productRow.getTranxDate();
                                    Date newDate = mPurchaseTranx.getInvoiceDate();
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
                                    closingUtility.stockPosting(outlet, branch, mPurchaseTranx.getFiscalYear().getId(), batchId,
                                            mProduct, tranxType.getId(), newDate, invoiceUnits.getQty(), free_qty,
                                            mPurchaseTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                            mPurchaseTranx.getTranxCode(), userId, "IN", mProduct.getPackingMaster().getId());

                                    closingUtility.stockPostingBatchWise(outlet, branch, mPurchaseTranx.getFiscalYear().getId(), batchId,
                                            mProduct, tranxType.getId(), newDate, invoiceUnits.getQty(), free_qty,
                                            mPurchaseTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                            mPurchaseTranx.getTranxCode(), userId, "IN", mProduct.getPackingMaster().getId());

                                }
                            }
                        } else {
                            inventoryCommonPostings.callToInventoryPostings("CR", mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), tranxQty + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear(), serialNo);
                        }
                        /***** End of new architecture of Inventory Postings *****/
                    }
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Error in saveIntoPurchaseInvoiceDetails  :" + exceptionAsString);
            }
            /* Inserting into Barcode */
            ProductBarcode barcode = null;
            if (productBatchNo != null) {
                barcode = barcodeRepository.
                        findByProductIdAndOutletIdAndStatusAndTransactionIdAndTransactionTypeMasterIdAndProductBatchId(
                                mProduct.getId(), outlet.getId(), true, mPurchaseTranx.getId(), tranxType.getId(), productBatchNo.getId());
            }
            if (barcode == null) {
                barcode = new ProductBarcode();
                barcode.setMrp(object.get("rate").getAsDouble());
                barcode.setTranxDate(mPurchaseTranx.getTransactionDate());
                barcode.setEnable(true);
                barcode.setStatus(true);
                barcode.setBranch(mPurchaseTranx.getBranch());
                barcode.setFiscalYear(mPurchaseTranx.getFiscalYear());
                barcode.setOutlet(mPurchaseTranx.getOutlet());
                barcode.setProduct(mProduct);
                if (levelA != null) barcode.setLevelA(levelA);
                if (levelB != null) barcode.setLevelB(levelB);
                if (levelC != null) barcode.setLevelC(levelC);
                barcode.setUnits(units);
                barcode.setTransactionTypeMaster(tranxType);
                String cName = mPurchaseTranx.getOutlet().getCompanyName();
                String firstLetter = cName.substring(0, 3);
                GenerateDates generateDates = new GenerateDates();
                String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
                Long lastRecord = barcodeRepository.findLastRecord(mPurchaseTranx.getOutlet().getId());
                if (lastRecord != null) {
                    String serailNo = String.format("%05d", lastRecord + 1);// 5 digit serial number
                    String bCode = firstLetter + currentMonth + serailNo;
                    barcode.setBarcodeUniqueCode(bCode);
                }
                barcode.setBatchNo(object.get("b_no").getAsString());
                if (productBatchNo != null) barcode.setProductBatch(productBatchNo);
                barcode.setTransactionId(mPurchaseTranx.getId());
                if (object.has("companybarcode")) barcode.setCompanyBarcode(object.get("companybarcode").getAsString());
                barcode.setQnty(object.get("qty").getAsInt());
                barcodeRepository.save(barcode);
            } else {
                barcode.setMrp(object.get("rate").getAsDouble());
                barcode.setTranxDate(mPurchaseTranx.getTransactionDate());
                barcode.setFiscalYear(mPurchaseTranx.getFiscalYear());
                if (levelA != null) barcode.setLevelA(levelA);
                if (levelB != null) barcode.setLevelB(levelB);
                if (levelC != null) barcode.setLevelC(levelC);
                barcode.setUnits(units);
                barcode.setBatchNo(object.get("b_no").getAsString());
                if (productBatchNo != null) barcode.setProductBatch(productBatchNo);
                if (object.has("companybarcode")) barcode.setCompanyBarcode(object.get("companybarcode").getAsString());
                barcode.setQnty(object.get("qty").getAsInt());
                barcodeRepository.save(barcode);
            }
            /**** end of Barcode ****/

            /**** Inventory Postings *****/
            try {
               /* if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                    flag = true;
                }*/
                /**** Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    if (details_id != 0) {
                        inventoryCommonPostings.callToEditInventoryPostings(mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear());
                    } else {
                        inventoryCommonPostings.callToInventoryPostings("CR", mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear(), serialNo);
                    }
                    /***** End of new architecture of Inventory Postings *****/
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Exception in Postings of Inventory:" + exceptionAsString);

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
                TranxPurInvoiceDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    if (deletedRowsId.size() > 0) {
                        if (deletedRowsId.has("del_id")) {
                            mDeletedInvoices = tranxPurInvoiceUnitsRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                            if (mDeletedInvoices != null) {
                                mDeletedInvoices.setStatus(false);
                                try {
                                    tranxPurInvoiceUnitsRepository.save(mDeletedInvoices);
                                    /***** inventory effects of deleted rows *****/
                                    inventoryCommonPostings.callToInventoryPostings("DR",
                                            mDeletedInvoices.getPurchaseTransaction().getInvoiceDate(),
                                            mDeletedInvoices.getPurchaseTransaction().getId(),
                                            mDeletedInvoices.getQty() + mDeletedInvoices.getFreeQty(), branch, outlet,
                                            mDeletedInvoices.getProduct(), tranxType,
                                            mDeletedInvoices.getLevelA(), mDeletedInvoices.getLevelB(),
                                            mDeletedInvoices.getLevelC(), mDeletedInvoices.getUnits(),
                                            mDeletedInvoices.getProductBatchNo(),
                                            mDeletedInvoices.getProductBatchNo() != null ?
                                                    mDeletedInvoices.getProductBatchNo().getBatchNo() : null,
                                            mDeletedInvoices.getPurchaseTransaction().getFiscalYear(), null);
                                    /***** End of new architecture of Inventory Postings *****/

                                    /***** NEW METHOD FOR LEDGER POSTING *****/
                                    closingUtility.stockPosting(outlet, branch, mPurchaseTranx.getFiscalYear().getId(),
                                            mDeletedInvoices.getProductBatchNo().getId(),
                                            mDeletedInvoices.getProduct(), tranxType.getId(),
                                            mDeletedInvoices.getPurchaseTransaction().getInvoiceDate(),
                                            mDeletedInvoices.getQty(), mDeletedInvoices.getFreeQty(),
                                            mPurchaseTranx.getId(), mDeletedInvoices.getUnits().getId(),
                                            mDeletedInvoices.getLevelA() != null ?
                                                    mDeletedInvoices.getLevelA().getId() : null,
                                            mDeletedInvoices.getLevelB() != null ? mDeletedInvoices.getLevelB().getId() : null,
                                            mDeletedInvoices.getLevelC() != null ?
                                                    mDeletedInvoices.getLevelC().getId() : null, mDeletedInvoices.getProductBatchNo() != null ?
                                                    mDeletedInvoices.getProductBatchNo() : null,
                                            mPurchaseTranx.getTranxCode(), userId, "OUT",
                                            mDeletedInvoices.getProduct().getPackingMaster().getId());

                                    closingUtility.stockPostingBatchWise(outlet, branch, mPurchaseTranx.getFiscalYear().getId(),
                                            mDeletedInvoices.getProductBatchNo().getId(),
                                            mDeletedInvoices.getProduct(), tranxType.getId(),
                                            mDeletedInvoices.getPurchaseTransaction().getInvoiceDate(),
                                            mDeletedInvoices.getQty(), mDeletedInvoices.getFreeQty(),
                                            mPurchaseTranx.getId(), mDeletedInvoices.getUnits().getId(),
                                            mDeletedInvoices.getLevelA() != null ?
                                                    mDeletedInvoices.getLevelA().getId() : null,
                                            mDeletedInvoices.getLevelB() != null ? mDeletedInvoices.getLevelB().getId() : null,
                                            mDeletedInvoices.getLevelC() != null ?
                                                    mDeletedInvoices.getLevelC().getId() : null, mDeletedInvoices.getProductBatchNo() != null ?
                                                    mDeletedInvoices.getProductBatchNo() : null,
                                            mPurchaseTranx.getTranxCode(), userId, "OUT",
                                            mDeletedInvoices.getProduct().getPackingMaster().getId());


                                } catch (DataIntegrityViolationException de) {
                                    StringWriter sw = new StringWriter();
                                    de.printStackTrace(new PrintWriter(sw));
                                    String exceptionAsString = sw.toString();
                                    purInvoiceLogger.error("Exception in saveInto PurchaseInvoiceDetails Edit:" + exceptionAsString);
                                } catch (Exception ex) {
                                    StringWriter sw = new StringWriter();
                                    ex.printStackTrace(new PrintWriter(sw));
                                    String exceptionAsString = sw.toString();
                                    purInvoiceLogger.error("Exception in saveInto PurchaseInvoiceDetails Edit:" + exceptionAsString);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /* for Purchase Details Edit */
    private void insertIntoPurchaseInvoiceDetailsHistory(List<TranxPurInvoiceDetails> row) {
        for (TranxPurInvoiceDetails mRow : row) {
            mRow.setStatus(false);
            mRow.setOperations("updated");
            invoiceDetailsRepository.save(mRow);
        }
    }

    /* for Purchase Invoice Edit */
    public void saveIntoPurchaseDutiesTaxesEdit(JsonObject duties_taxes, TranxPurInvoice invoiceTranx, Boolean taxFlag, TransactionTypeMaster tranxType, Long outletId, Long userId) {
        List<TranxPurInvoiceDutiesTaxes> purchaseDutiesTaxes = new ArrayList<>();
        List<Long> db_dutiesLedgerIds = purInvoiceDutiesTaxesRepository.findByDutiesAndTaxesId(invoiceTranx.getId());
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null);
        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            if (db_dutiesLedgerIds.size() > 0) {
                purchaseDutiesTaxes = purInvoiceDutiesTaxesRepository.findByPurchaseTransactionAndStatus(invoiceTranx, true);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            ledgerInputList.add(dutiesTaxes.getId());
                            Double amt = cgstObject.get("amt").getAsDouble();
                            if (isContains) {
                                /**** New Postings Logic *****/
                                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                if (mLedger != null) {
                                    mLedger.setAmount(amt);
                                    mLedger.setTransactionDate(invoiceTranx.getInvoiceDate());
                                    mLedger.setOperations("updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType,
                                    "DR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getInvoiceDate(),
                                    invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());

                            taxes.setAmount(amt);
                            taxes.setStatus(true);
                            taxes.setPurchaseTransaction(invoiceTranx);
                            taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                            taxes.setIntra(taxFlag);
                            purchaseDutiesTaxes.add(taxes);
                        }
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {

                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT SGST " + inputGst;
                        if (invoiceTranx.getBranch() != null) {
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        } else {
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);
                        }

                        if (dutiesTaxes != null) {
                            TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            ledgerInputList.add(dutiesTaxes.getId());
                            Double amt = sgstObject.get("amt").getAsDouble();
                            if (isContains) {
                                /**** New Postings Logic *****/
                                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                if (mLedger != null) {
                                    mLedger.setAmount(amt);
                                    mLedger.setTransactionDate(invoiceTranx.getInvoiceDate());
                                    mLedger.setOperations("updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType,
                                    "DR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getInvoiceDate(),
                                    invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());

                            taxes.setAmount(amt);
                            taxes.setPurchaseTransaction(invoiceTranx);
                            taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                            taxes.setIntra(taxFlag);
                            taxes.setStatus(true);
                            purchaseDutiesTaxes.add(taxes);
                        }
                    }
                }
            } else {
                if (duties_taxes.has("igst")) {
                    JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                    /* this is for Igst creation */
                    if (igstList != null && igstList.size() > 0) {
                        for (JsonElement mIgst : igstList) {
                            JsonObject igstObject = mIgst.getAsJsonObject();
                            LedgerMaster dutiesTaxes = null;
                            //int inputGst = (int) igstObject.get("gst").getAsDouble();
                            String inputGst = igstObject.get("gst").getAsString();
                            String ledgerName = "INPUT IGST " + inputGst;
                            if (invoiceTranx.getBranch() != null) {
                                dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                            } else {
                                dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);
                            }

                            if (dutiesTaxes != null) {
                                TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
                                taxes.setDutiesTaxes(dutiesTaxes);
                                travelledArray.add(dutiesTaxes.getId());
                                Boolean isContains = dbList.contains(dutiesTaxes.getId());
                                Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                                mInputList.add(dutiesTaxes.getId());
                                ledgerInputList.add(dutiesTaxes.getId());
                                Double amt = igstObject.get("amt").getAsDouble();
                                if (isContains) {
                                    /**** New Postings Logic *****/
                                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                    if (mLedger != null) {
                                        mLedger.setAmount(amt);
                                        mLedger.setTransactionDate(invoiceTranx.getInvoiceDate());
                                        mLedger.setOperations("Updated");
                                        ledgerTransactionPostingsRepository.save(mLedger);
                                    }
                                } else {
                                    /**** New Postings Logic *****/
                                    ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");
                                }

                                /***** NEW METHOD FOR LEDGER POSTING *****/
                                postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType,
                                        "DR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getInvoiceDate(),
                                        invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                        invoiceTranx.getTranxCode());

                                taxes.setAmount(amt);
                                taxes.setPurchaseTransaction(invoiceTranx);
                                taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                                taxes.setIntra(taxFlag);
                                taxes.setStatus(true);
                                purchaseDutiesTaxes.add(taxes);
                            }
                        }
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

                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //   int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;

                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
                            Double amt = cgstObject.get("amt").getAsDouble();
                            taxes.setDutiesTaxes(dutiesTaxes);
                            taxes.setAmount(amt);
                            taxes.setPurchaseTransaction(invoiceTranx);
                            taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                            taxes.setIntra(taxFlag);
                            purchaseDutiesTaxes.add(taxes);
                            /**** New Postings Logic *****/
                            ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedger(tranxType, "DR", amt, invoiceTranx.getFiscalYear(),
                                    dutiesTaxes, invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());
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
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
                            Double amt = sgstObject.get("amt").getAsDouble();
                            taxes.setDutiesTaxes(dutiesTaxes);
                            taxes.setAmount(amt);
                            taxes.setPurchaseTransaction(invoiceTranx);
                            taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                            taxes.setIntra(taxFlag);
                            purchaseDutiesTaxes.add(taxes);
                            /**** New Postings Logic *****/
                            ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");


                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedger(tranxType, "DR", amt, invoiceTranx.getFiscalYear(),
                                    dutiesTaxes, invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());
                            /**** NEW METHOD FOR LEDGER POSTING ****/
                        }
                    }
                }
            } else {
                if (duties_taxes.has("igst")) {
                    JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                    /* this is for Igst creation */
                    if (igstList.size() > 0) {
                        for (JsonElement mIgst : igstList) {
                            JsonObject igstObject = igstList.getAsJsonObject();
                            LedgerMaster dutiesTaxes = null;
                            //int inputGst = (int) igstObject.get("gst").getAsDouble();
                            String inputGst = igstObject.get("gst").getAsString();
                            String ledgerName = "INPUT IGST " + inputGst;

                            if (invoiceTranx.getBranch() != null)
                                dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                            else
                                dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                            if (dutiesTaxes != null) {
                                TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
                                Double amt = igstObject.get("amt").getAsDouble();
                                taxes.setDutiesTaxes(dutiesTaxes);
                                taxes.setAmount(amt);
                                taxes.setPurchaseTransaction(invoiceTranx);
                                taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                                taxes.setIntra(taxFlag);
                                purchaseDutiesTaxes.add(taxes);
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), invoiceTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");


                                /***** NEW METHOD FOR LEDGER POSTING *****/
                                postingUtility.callToPostingLedger(tranxType, "DR", amt, invoiceTranx.getFiscalYear(),
                                        dutiesTaxes, invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                        invoiceTranx.getTranxCode());
                                /**** NEW METHOD FOR LEDGER POSTING ****/
                            }
                        }
                    }
                }
            }
        }
        purInvoiceDutiesTaxesRepository.saveAll(purchaseDutiesTaxes);
    }

    /* private List<Long> getInputLedgerIds(Boolean taxFlag, JsonObject duties_taxes, Long outletId) {
         List<Long> returnLedgerIds = new ArrayList<>();
         if (taxFlag) {
             JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
             JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
             *//* this is for Cgst creation *//*
            if (cgstList.size() > 0) {
                for (JsonElement mCgst : cgstList) {
                    TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
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
                    TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
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
                    /*   TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();*/
                    JsonObject cgstObject = mCgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
//                    int inputGst = (int) cgstObject.get("gst").getAsDouble();
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
                    /*   TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();*/
                    JsonObject sgstObject = mSgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    // int inputGst = (int) sgstObject.get("gst").getAsDouble();
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
            if (igstList != null && igstList.size() > 0) {
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

    /**** Save Into Purchase Additional Charges Edit *****/
    public void saveIntoPurchaseAdditionalChargesEdit(JsonArray additionalCharges, TranxPurInvoice mPurchaseTranx, TransactionTypeMaster tranxTypeMater, Long outletId, String acRowsDeleted) {

        List<TranxPurInvoiceAdditionalCharges> chargesList = new ArrayList<>();
        for (JsonElement mAddCharges : additionalCharges) {
            JsonObject object = mAddCharges.getAsJsonObject();
            Double amount = 0.0;
            Double percent = 0.0;
            Long detailsId = 0L;
            if (object.has("amt") && !object.get("amt").getAsString().equalsIgnoreCase("")) {
                amount = object.get("amt").getAsDouble();
                percent = object.get("percent").getAsDouble();
                Long ledgerId = object.get("ledgerId").getAsLong();
                if (object.has("additional_charges_details_id"))
                    detailsId = object.get("additional_charges_details_id").getAsLong();
                LedgerMaster addcharges = null;
                TranxPurInvoiceAdditionalCharges charges = null;
                charges = purInvoiceAdditionalChargesRepository.findByAdditionalChargesIdAndPurchaseTransactionIdAndStatus(ledgerId, mPurchaseTranx.getId(), true);
                if (detailsId == 0L) {
                    charges = new TranxPurInvoiceAdditionalCharges();
                }
                addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges.setAmount(amount);
                charges.setPercent(percent);
                charges.setAdditionalCharges(addcharges);
                charges.setPurchaseTransaction(mPurchaseTranx);
                charges.setStatus(true);

                chargesList.add(charges);
                purInvoiceAdditionalChargesRepository.save(charges);
                Boolean isContains = dbList.contains(addcharges.getId());
                Boolean isLedgerContains = ledgerList.contains(addcharges.getId());
                mInputList.add(addcharges.getId());
                ledgerInputList.add(addcharges.getId());
                if (isContains) {
                    //      transactionDetailsRepository.ledgerPostingEdit(addcharges.getId(), mPurchaseTranx.getId(), tranxTypeMater.getId(), "DR", amount * -1);
                    /**** New Postings Logic *****/
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(addcharges.getId(), tranxTypeMater.getId(), mPurchaseTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(amount);
                        mLedger.setTransactionDate(mPurchaseTranx.getInvoiceDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    /* insert */
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(amount, addcharges, tranxTypeMater, addcharges.getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");
                }


                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, addcharges.getId(), tranxTypeMater,
                        "DR", mPurchaseTranx.getId(), addcharges, mPurchaseTranx.getInvoiceDate(),
                        mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(),
                        mPurchaseTranx.getTranxCode());
                /**** NEW METHOD FOR LEDGER POSTING ****/
            }
        }
        /*try {
            purInvoiceAdditionalChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            purInvoiceLogger.error("Error in saveIntoPurchaseAdditionalChargesEdit" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            purInvoiceLogger.error("Error in saveIntoPurchaseAdditionalChargesEdit" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }*/
        /* if Purchace Account ledger is deleted from details table from front end, while edit the purchase invoice */
    }

    public void delAddCharges(JsonArray deletedArrays) {
        TranxPurInvoiceAdditionalCharges mDeletedInvoices = null;
        for (JsonElement element : deletedArrays) {
            JsonObject deletedRowsId = element.getAsJsonObject();
            if (deletedRowsId.has("del_id")) {
                mDeletedInvoices = purInvoiceAdditionalChargesRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                if (mDeletedInvoices != null) {
                    mDeletedInvoices.setStatus(false);
                    try {
                        purInvoiceAdditionalChargesRepository.save(mDeletedInvoices);
                    } catch (DataIntegrityViolationException de) {
                        purInvoiceLogger.error("Error in saveInto Purchase Invoice Add.Charges Edit" + de.getMessage());
                        de.printStackTrace();
                        System.out.println("Exception:" + de.getMessage());

                    } catch (Exception ex) {
                        StringWriter sw = new StringWriter();
                        ex.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        purInvoiceLogger.error("Exception in saveInto PurchaseInvoiceDetails Additional Charges Edit:" + exceptionAsString);
                    }
                }
            }
        }
    }


    /* End of Purchase Additional Charges Edit */
    public void insertIntoPurchaseInvoiceHistory(TranxPurInvoice invoiceTranx) {
        invoiceTranx.setStatus(false);
        tranxPurInvoiceRepository.save(invoiceTranx);
    }

    private void insertIntoDutiesAndTaxesHistory(List<TranxPurInvoiceDutiesTaxes> purchaseDutiesTaxes) {
        for (TranxPurInvoiceDutiesTaxes mList : purchaseDutiesTaxes) {
            mList.setStatus(false);
            purInvoiceDutiesTaxesRepository.save(mList);
        }
    }

    private void saveHistoryPurchaseAdditionalCharges(TranxPurInvoice mPurchaseTranx) {
        List<TranxPurInvoiceAdditionalCharges> purInvoiceAdditionalCharges = new ArrayList<>();
        purInvoiceAdditionalCharges = purInvoiceAdditionalChargesRepository.findByPurchaseTransactionIdAndStatus(mPurchaseTranx.getId(), true);
        if (purInvoiceAdditionalCharges.size() > 0) {
            for (TranxPurInvoiceAdditionalCharges mList : purInvoiceAdditionalCharges) {
                mList.setStatus(false);
                mList.setOperation("updated");
                purInvoiceAdditionalChargesRepository.save(mList);
            }
        }
    }

    public JsonObject purchaseDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxPurInvoice invoiceTranx = tranxPurInvoiceRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TransactionTypeMaster purTranx = tranxRepository.findByTransactionCodeIgnoreCase("PRS");
        TransactionTypeMaster returnTranx = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
        try {
            invoiceTranx.setStatus(false);
            invoiceTranx.setOperations("deletion");
            /***** check whether the reference of purchase invoice is available in purchase return table or not,
             if available then delete all the purchase returns and reverse the postings
             *****/
            List<TranxPurReturnInvoice> purReturnList = new ArrayList<>();
            purReturnList = tranxPurReturnsRepository.findByTranxPurInvoiceIdAndStatus(invoiceTranx.getId(), true);
            for (TranxPurReturnInvoice mReturnInvoice : purReturnList) {
                try {
                    mReturnInvoice.setStatus(false);
                    tranxPurReturnsRepository.save(mReturnInvoice);
                } catch (Exception e) {
                    purInvoiceLogger.error("Exception in Deleting the Purchase Return of Purchase Invoice ->" + e.getMessage());
                    System.out.println("Exception in Deleting the Purchase Return of Purchase Invoice ->" + e.getMessage());
                }
            }
            List<TranxDebitNoteNewReferenceMaster> noteNewReferenceMasters = new ArrayList<>();
            noteNewReferenceMasters = tranxDebitNoteNewReferenceRepository.findByPurchaseInvoiceIdAndStatus(invoiceTranx.getId(), true);
            if (noteNewReferenceMasters != null && noteNewReferenceMasters.size() > 0) {
                for (TranxDebitNoteNewReferenceMaster mdebitNote : noteNewReferenceMasters) {
                    try {
                        mdebitNote.setStatus(false);
                        deleteIntoLedgerTranxDetailsDebitNote(mdebitNote.getTranxPurReturnInvoice());
                        tranxDebitNoteNewReferenceRepository.save(mdebitNote);
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        purInvoiceLogger.error("Exception in Deleting  NewReference of Purchase Invoice :" + exceptionAsString);
                    }
                }
            }
            tranxPurInvoiceRepository.save(invoiceTranx);
            if (invoiceTranx != null) {
                deleteIntoLedgerTranxDetails(invoiceTranx);// Accounting Postings
                /**** make status=0 to all ledgers of respective purchase invoice id, due to this we wont get
                 details of deleted invoice when we want get details of respective ledger ****/
                List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
                mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(1L, invoiceTranx.getId(), true);
                for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                    try {
                        mPostings.setStatus(false);
                        ledgerTransactionPostingsRepository.save(mPostings);
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        purInvoiceLogger.error("Exception in Delete functionality for all ledgers of" + " deleted purchase invoice->" + exceptionAsString);
                    }
                }
                /**** make status=0 to all ledgers of respective purchase return id, due to this we wont get
                 details of deleted invoice when we want get details of respective ledger ****/
                for (TranxPurReturnInvoice mList : purReturnList) {
                    List<LedgerTransactionPostings> mReturnList = new ArrayList<>();
                    mReturnList = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(2L, mList.getId(), true);
                    for (LedgerTransactionPostings mPostings : mReturnList) {
                        try {
                            mPostings.setStatus(false);
                            ledgerTransactionPostingsRepository.save(mPostings);
                        } catch (Exception e) {
                            purInvoiceLogger.error("Exception in Delete functionality for all ledgers of" + " deleted purchase return invoice->" + e.getMessage());
                        }
                    }
                }
                /****  Reverse Postings of Inventory of Purchase Return Invoice Delete ****/

                for (TranxPurReturnInvoice returnInvoice : purReturnList) {
                    List<TranxPurReturnDetailsUnits> returnList = new ArrayList<>();
                    returnList = tranxPurReturnDetailsUnitRepository.findByTranxPurReturnInvoiceIdAndStatus(returnInvoice.getId(), true);
                    for (TranxPurReturnDetailsUnits mUnitObjects : returnList) {
                        /***** new architecture of Inventory Postings *****/
                        inventoryCommonPostings.callToInventoryPostings("CR", returnInvoice.getPurReturnDate(),
                                returnInvoice.getId(), mUnitObjects.getQty(), returnInvoice.getBranch(),
                                returnInvoice.getOutlet(), mUnitObjects.getProduct(), returnTranx, null,
                                null, null, mUnitObjects.getUnits(), mUnitObjects.getProductBatchNo(),
                                mUnitObjects.getProductBatchNo() != null ?
                                        mUnitObjects.getProductBatchNo().getBatchNo() : null,
                                returnInvoice.getFiscalYear(), null);
                        /***** End of new architecture of Inventory Postings *****/
                    }
                }
                /****  Reverse Postings of Inventory of Purchase Invoice Delete ****/
                List<TranxPurInvoiceDetailsUnits> unitsList = new ArrayList<>();
                unitsList = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatus(invoiceTranx.getId(), true);
                if (unitsList != null && unitsList.size() > 0) {
                    for (TranxPurInvoiceDetailsUnits mUnitObjects : unitsList) {
                        /***** new architecture of Inventory Postings *****/
                        inventoryCommonPostings.callToInventoryPostings("DR", invoiceTranx.getInvoiceDate(), invoiceTranx.getId(), mUnitObjects.getQty(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), mUnitObjects.getProduct(), purTranx, null, null, null, mUnitObjects.getUnits(), mUnitObjects.getProductBatchNo(), mUnitObjects.getProductBatchNo() != null ? mUnitObjects.getProductBatchNo().getBatchNo() : null, invoiceTranx.getFiscalYear(), null);
                        /***** End of new architecture of Inventory Postings *****/
                    }
                }
                jsonObject.addProperty("message", "Purchase invoice deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in purchase deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception purchaseDelete->" + exceptionAsString);
        }
        return jsonObject;
    }

    public JsonObject getProductEditByIdByFPU(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxPurInvoice invoiceTranx = tranxPurInvoiceRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        productIds = tranxPurInvoiceUnitsRepository.findByInvoiceIdAndStatus(invoiceTranx.getId(), true);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getInvoiceDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public Object validatePurchaseInvoices(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurInvoice purInvoice = null;
        String billNo = request.getParameter("bill_no");
        Long sundryCreditorId = Long.parseLong(request.getParameter("supplier_id"));
        if (users.getBranch() != null) {
            purInvoice = tranxPurInvoiceRepository.findByOutletIdAndBranchIdAndSundryCreditorsIdAndStatusAndVendorInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), sundryCreditorId, true, billNo);
        } else {
            purInvoice = tranxPurInvoiceRepository.findByOutletIdAndSundryCreditorsIdAndStatusAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), sundryCreditorId, true, billNo);
        }
        if (purInvoice != null) {
            responseMessage.setMessage("Purchase invoice number:" + billNo + " already exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New purchase invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object validateInvoiceNoUpdateNo(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurInvoice purInvoice = null;
        Long sundryCreditorId = Long.parseLong(request.getParameter("supplier_id"));
        Long invoiceId = Long.parseLong(request.getParameter("invoice_id"));
        if (users.getBranch() != null) {
            purInvoice = tranxPurInvoiceRepository.findByOutletIdAndBranchIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), sundryCreditorId, request.getParameter("bill_no"));
        } else {
            purInvoice = tranxPurInvoiceRepository.findByOutletIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), sundryCreditorId, request.getParameter("bill_no"));
        }
        if (purInvoice != null && invoiceId != purInvoice.getId()) {
//            responseMessage.setMessage("Duplicate purchase invoice number");
            responseMessage.setMessage("Purchase invoice number:" + invoiceId + " already exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New purchase invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    /******* update product stock while purchase tranx network scenario ********/
    public Object updateProductStock(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase(request.getParameter("tranxType")); // "PRS" => purchase
            String tranxAction = request.getParameter("tranxAction"); // CR or DR
//            LocalDate tranxDate = LocalDate.parse(request.getParameter("tranxDate")); // for purchase => invoice date, for sales => bill date

            LocalDate tranxDate = DateConvertUtil.convertStringToLocalDate(request.getParameter("tranxDate"));
            Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("tranxDate"));

            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
            Long inventoryId = Long.valueOf(request.getParameter("inventoryId"));
            Long productId = Long.valueOf(request.getParameter("productId"));
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
            Long unitId = null;
            String batchNo = null;

            if (!request.getParameter("levelAId").equalsIgnoreCase(""))
                levelAId = Long.valueOf(request.getParameter("levelAId"));
            if (!request.getParameter("levelBId").equalsIgnoreCase(""))
                levelBId = Long.valueOf(request.getParameter("levelBId"));
            if (!request.getParameter("levelCId").equalsIgnoreCase(""))
                levelCId = Long.valueOf(request.getParameter("levelCId"));
            if (!request.getParameter("unitId").equalsIgnoreCase(""))
                unitId = Long.valueOf(request.getParameter("unitId"));
            if (!request.getParameter("batchNo").equalsIgnoreCase("")) batchNo = request.getParameter("batchNo");

            Double qty = Double.valueOf(request.getParameter("qty"));
            Product product = productRepository.findByIdAndStatus(productId, true);
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            Units units = null;
            if (batchNo != null) {
                productBatchNo = productBatchNoRepository.findByBatchNo(batchNo);
            }
            if (levelAId != null) levelA = levelARepository.findByIdAndStatus(levelAId, true);
            if (levelBId != null) levelB = levelBRepository.findByIdAndStatus(levelBId, true);
            if (levelCId != null) levelC = levelCRepository.findByIdAndStatus(levelCId, true);
            if (unitId != null) units = unitsRepository.findByIdAndStatus(unitId, true);

            InventoryDetailsPostings inventoryDetailsPostingsNew = new InventoryDetailsPostings();
            if (inventoryId > 0)
                inventoryDetailsPostingsNew = inventoryDetailsPostingsRepository.findByIdAndStatus(inventoryId, true);

            inventoryDetailsPostingsNew.setTranxAction(tranxAction);
            inventoryDetailsPostingsNew.setTranxDate(strDt);
            inventoryDetailsPostingsNew.setFiscalYear(fiscalYear);
            inventoryDetailsPostingsNew.setTransactionType(tranxType);
            inventoryDetailsPostingsNew.setQty(qty);
            inventoryDetailsPostingsNew.setUniqueBatchNo(productBatchNo != null ? productBatchNo.getBatchNo() : null);
            inventoryDetailsPostingsNew.setProduct(product);
            inventoryDetailsPostingsNew.setUnits(units);
            inventoryDetailsPostingsNew.setProductBatch(productBatchNo);
            inventoryDetailsPostingsNew.setLevelA(levelA);
            inventoryDetailsPostingsNew.setLevelB(levelB);
            inventoryDetailsPostingsNew.setLevelC(levelC);
            inventoryDetailsPostingsNew.setBranch(users.getBranch());
            inventoryDetailsPostingsNew.setOutlet(users.getOutlet());
            inventoryDetailsPostingsNew.setStatus(true);

            try {
                InventoryDetailsPostings savedInventoryDetailsPostings = inventoryDetailsPostingsRepository.save(inventoryDetailsPostingsNew);

                response.addProperty("message", "inventory updated");
                response.addProperty("inventoryId", savedInventoryDetailsPostings.getId());
                response.addProperty("responseStatus", HttpStatus.OK.value());

                return response;
            } catch (Exception e) {
                response.addProperty("message", "Failed to update product stock");
                response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Exception in updateProductStock->" + exceptionAsString);
            }

        } catch (Exception e) {
            response.addProperty("message", "Failed to update product stock");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception in updateProductStock->" + exceptionAsString);
        }
        return response;
    }

    public JsonObject getSupplierListByProductId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        Long productId = Long.parseLong(request.getParameter("productId"));

        List<TranxPurInvoiceDetailsUnits> tranxPurInvoiceDetailsUnits = tranxPurInvoiceUnitsRepository.findTop5ByProductIdOrderByIdDesc(productId);

        for (TranxPurInvoiceDetailsUnits obj : tranxPurInvoiceDetailsUnits) {
            if (obj.getPurchaseTransaction().getStatus()) {
                JsonObject response = new JsonObject();
                response.addProperty("supplier_name", obj.getPurchaseTransaction().getSundryCreditors().getLedgerName());
                response.addProperty("invoice_no", obj.getPurchaseTransaction().getVendorInvoiceNo());
                response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(obj.getPurchaseTransaction().getInvoiceDate()).toString());
                response.addProperty("batch", obj.getProductBatchNo() != null ? obj.getProductBatchNo().getBatchNo() : "");
                response.addProperty("mrp", obj.getProductBatchNo() != null ? obj.getProductBatchNo().getMrp().toString() : "");
                response.addProperty("rate", obj.getProductBatchNo() != null ? obj.getProductBatchNo().getPurchaseRate().toString() : "");
                response.addProperty("quantity", obj.getQty());
                response.addProperty("cost", obj.getProductBatchNo() != null ? obj.getProductBatchNo().getCosting().toString() : "");
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

    public JsonObject mobileSCOutstandingList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        String flag = "purchaseList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<LedgerMaster> balanceSummaries = new ArrayList<>();
        balanceSummaries = ledgerRepository.findByPrincipleGroupsIdAndStatus(5L, true);
        for (LedgerMaster balanceSummary : balanceSummaries) {
            System.out.println("Ledger Master Name " + balanceSummary.getLedgerName());
            Long ledgerId = balanceSummary.getId();
            JsonObject jsonObject = new JsonObject();
            LocalDate endDatep = null;
            LocalDate startDatep = null;
  /*          LocalDate startDatep = null;
            if (!request.get("end_date").equalsIgnoreCase("") && !request.get("start_date").equalsIgnoreCase("")) {
                endDatep = LocalDate.parse(request.get("end_date").toString());
                startDatep = LocalDate.parse(request.get("start_date"));
            } else {*/
            //   LocalDate today = LocalDate.now();
           /* System.out.println("First day: " + today.withDayOfMonth(1));
            System.out.println("Last day: " + today.withDayOfMonth(today.lengthOfMonth()));*/
          /*  startDatep = today.withDayOfMonth(1);
            endDatep = today.withDayOfMonth(today.lengthOfMonth());*/
            endDatep = LocalDate.parse(request.get("end_date"));
            startDatep = LocalDate.parse(request.get("start_date"));
            try {
                list = tranxPurInvoiceRepository.findmobilePurchaseTotalAmt(ledgerId, startDatep, endDatep, true);
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
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Exception in salesDelete->" + exceptionAsString);
            }
            jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());
            if (sumTotal > 0) result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        LocalDate today = LocalDate.now();
        json.addProperty("closingBalance", closingBalance);
        json.addProperty("todayDate", String.valueOf(today));
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public Object purchasemobileList(Map<String, String> request) {

        LocalDate endDatep = null;
        LocalDate startDatep = null;
        if (!request.get("end_date").equalsIgnoreCase("") && !request.get("start_date").equalsIgnoreCase("")) {
            endDatep = LocalDate.parse(request.get("end_date").toString());

            startDatep = LocalDate.parse(request.get("start_date"));

        } else {
            LocalDate today = LocalDate.now();
            System.out.println("First day: " + today.withDayOfMonth(1));
            System.out.println("Last day: " + today.withDayOfMonth(today.lengthOfMonth()));
            startDatep = today.withDayOfMonth(1);
            endDatep = today.withDayOfMonth(today.lengthOfMonth());

        }
        JsonArray result = new JsonArray();
        List<TranxPurInvoice> purInvoice = new ArrayList<>();

        purInvoice = tranxPurInvoiceRepository.findPurchaseInvoicesListNoBr(Long.valueOf(request.get("sundry_creditor_id")), startDatep, endDatep, true);

        for (TranxPurInvoice invoices : purInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("invoice_id", invoices.getId());
            response.addProperty("invoice_no", invoices.getVendorInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(invoices.getInvoiceDate()).toString());
            response.addProperty("transaction_date", invoices.getTransactionDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_creditor_name", invoices.getSundryCreditors().getLedgerName());
            response.addProperty("pur_account_name", invoices.getPurchaseAccountLedger().getLedgerName());
            response.addProperty("sundry_creditor_id", invoices.getSundryCreditors().getId());
            response.addProperty("baseAmt", invoices.getTotalBaseAmount());
            response.addProperty("taxAmt", invoices.getTotalTax());
            result.add(response);
        }
        System.out.println(result);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }


    public Object purchaseinvoicedetails(Map<String, String> request) {
        JsonArray result = new JsonArray();

        List<TranxPurInvoiceDetailsUnits> purInvoice = new ArrayList<>();

        purInvoice = tranxPurInvoiceUnitsRepository.findPurchaseInvoicesDetails(Long.valueOf(request.get("purchase_invoice_id")), true);

        for (TranxPurInvoiceDetailsUnits invoices : purInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_id", invoices.getPurchaseTransaction().getId());
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
        TranxPurInvoice tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(Long.parseLong(request.get("purchase_invoice_id")), true);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.addProperty("totalBaseAmt", tranxPurInvoice.getTotalBaseAmount());
        output.addProperty("roundoff", tranxPurInvoice.getRoundOff());
        output.addProperty("finalAmt", tranxPurInvoice.getTotalAmount());
        output.addProperty("taxAmt", tranxPurInvoice.getTotalTax());
        output.addProperty("totalDisAmt", tranxPurInvoice.getTotalPurchaseDiscountAmt());

        output.add("data", result);
        return output;
    }

    public JsonObject mobileOnDateList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        String flag = "purchaseList";
        LocalDate localDate = null;
        if (request.get("dateflag").equalsIgnoreCase("next")) {
            localDate = LocalDate.parse(request.get("date"));
            //LocalDate oneMonthLater = nextDate.plusMonths(1);
            startDatep = localDate.withDayOfMonth(1);
            endDatep = localDate.withDayOfMonth(localDate.lengthOfMonth());

        } else if (request.get("dateflag").equalsIgnoreCase("prev")) {
            localDate = LocalDate.parse(request.get("date").toString());
            System.out.println("todaydate" + localDate);
            //LocalDate oneMonthLater = localDate.minusMonths(1);
            startDatep = localDate.withDayOfMonth(1);
            endDatep = localDate.withDayOfMonth(localDate.lengthOfMonth());
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
                list = tranxPurInvoiceRepository.findmobilePurchaseTotalAmt(ledgerId, startDatep, endDatep, true);
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
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Exception in salesDelete->" + exceptionAsString);
            }
            jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());
            if (sumTotal > 0) result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        LocalDate today = LocalDate.now();
        json.addProperty("closingBalance", closingBalance);
        json.addProperty("todayDate", String.valueOf(localDate));
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public JsonObject mobilePayableList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        String flag = "payableList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<LedgerMaster> balanceSummaries = new ArrayList<>();
        balanceSummaries = ledgerRepository.findByPrincipleGroupsIdAndStatus(5L, true);
        for (LedgerMaster balanceSummary : balanceSummaries) {
            System.out.println("Ledger Master Name " + balanceSummary.getLedgerName());
            Long ledgerId = balanceSummary.getId();
            JsonObject jsonObject = new JsonObject();
            LocalDate endDatep = null;
            LocalDate startDatep = null;

            endDatep = LocalDate.parse(request.get("end_date"));
            startDatep = LocalDate.parse(request.get("start_date"));
            try {
                list = tranxPurInvoiceRepository.findmobilePayableTotalAmt(ledgerId, startDatep, endDatep, true);
                for (int i = 0; i < list.size(); i++) {
                    Object[] objp = list.get(i);
                    sumTotal = Double.parseDouble(objp[0].toString());
                    totalInvoice = Integer.parseInt(objp[1].toString());
                    if (sumTotal > 0) {
                        jsonObject.addProperty("TotalAmt", sumTotal);
                        jsonObject.addProperty("TotalInvoiceCount", totalInvoice);
                        jsonObject.addProperty("CreditorsId", ledgerId);
                        jsonObject.addProperty("flag", flag);
                        closingBalance = closingBalance + sumTotal;
                    }
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                purInvoiceLogger.error("Exception in mobilePayableList->" + exceptionAsString);
            }
            if (sumTotal > 0) {
                jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());
                result.add(jsonObject);
            }
        }
        JsonObject json = new JsonObject();
        LocalDate today = LocalDate.now();
        json.addProperty("closingBalance", closingBalance);
        json.addProperty("todayDate", String.valueOf(today));
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public JsonObject purchasePrintInvoice(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurInvoiceDetailsUnits> list = new ArrayList<>();
        List<TranxPurchaseInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        TranxPurInvoice purInvoice = null;
        String source = request.getParameter("source");
        String key = request.getParameter("print_type"); //check whether printbill is calling from create page or from list page
        /***** if  print_type is create, then use serialnumber of invoice to fetch invoice details ,
         * if print_type is list then use invoice id to fetch invoice details *****/
        try {
            String invoiceNo = request.getParameter("id");
            Long id = 0L;
            if (source.equalsIgnoreCase("purchase_invoice")) {
                if (users.getBranch() != null) {
                    if (key.equalsIgnoreCase("create")) {
                        purInvoice = tranxPurInvoiceRepository.findByVendorInvoiceNoAndOutletIdAndBranchIdAndStatus(invoiceNo, users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        purInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    }
                } else {
                    if (key.equalsIgnoreCase("create")) {
                        purInvoice = tranxPurInvoiceRepository.findByVendorInvoiceNoAndOutletIdAndStatusAndBranchIsNull(invoiceNo, users.getOutlet().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        purInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);
                    }
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
            if (purInvoice != null) {
                //   list = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatus(purInvoice.getId(), true);
                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                debtorsObject.addProperty("supplier_name", purInvoice.getSundryCreditors().getLedgerName());
                debtorsObject.addProperty("supplier_address", purInvoice.getSundryCreditors().getAddress());
                debtorsObject.addProperty("supplier_gstin", purInvoice.getSundryCreditors().getGstin());
                debtorsObject.addProperty("supplier_phone", purInvoice.getSundryCreditors().getMobile());
                JsonObject invoiceObject = new JsonObject();
                /* Sales Invoice Data */
                invoiceObject.addProperty("id", purInvoice.getId());
                invoiceObject.addProperty("invoice_dt", purInvoice.getInvoiceDate().toString());
                invoiceObject.addProperty("invoice_no", purInvoice.getVendorInvoiceNo());
                invoiceObject.addProperty("state_code", purInvoice.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", purInvoice.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(purInvoice.getTaxableAmount()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(purInvoice.getTotaligst()));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(purInvoice.getTotalcgst()));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(purInvoice.getTotalsgst()));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(purInvoice.getTotalBaseAmount()));
                invoiceObject.addProperty("total_discount", numFormat.numFormat(purInvoice.getTotalPurchaseDiscountAmt()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(purInvoice.getTotalAmount()));
                // invoiceObject.addProperty("advanced_amount", numFormat.numFormat(purInvoice.getAd() != null ? purInvoice.getAdvancedAmount() : 0.0));
                // invoiceObject.addProperty("payment_mode", purInvoice.getPaymentMode());

                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();

                /* getting Units of Sales Quotations*/
                List<TranxPurInvoiceDetailsUnits> unitDetails = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatus(purInvoice.getId(), true);
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
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception in getInvoiceBillPrint->" + exceptionAsString);
        } catch (Exception e1) {
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purInvoiceLogger.error("Exception in getInvoiceBillPrint->" + exceptionAsString);
        }
        return finalResult;
    }

    public Object GSTR2purchaseInvoiceDetails(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        System.out.println("request " + request + "  req=" + req);
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");
        Long debtoryId = Long.parseLong(request.get("creditor_id"));
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        Boolean flag = false;
        List purInvoice = new ArrayList<>();
        List<TranxSalesInvoice> saleArrayList = new ArrayList<>();
        List<PurInvoiceDTO> purInvoiceDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT id FROM `tranx_purchase_invoice_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND " + "status=1 AND sundry_creditors_id=" + debtoryId;
//        String query = "SELECT id FROM `tranx_sales_invoice_tbl";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += " AND DATE(transaction_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND (transaction_date LIKE '%" + searchText + "%'" + "OR vendor_invoice_no LIKE '%" + searchText + "%'" + "OR total_amount LIKE '%" + searchText + "%'" + "OR total_base_amount LIKE '%" + searchText + "%'" + "OR total_tax LIKE '%" + searchText + "%'" + "OR totaligst LIKE '%" + searchText + "%'" + "OR totalcgst LIKE '%" + searchText + "%'" + "OR totalsgst LIKE '%" + searchText + "%')";
            }
            String jsonToStr = request.get("sort");
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (jsonObject != null && !jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
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
            purInvoice = q.getResultList();

            for (Object mList : purInvoice) {
                TranxPurInvoice invoiceListView = tranxPurInvoiceRepository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                purInvoiceDTOList.add(convertToDTDTO(invoiceListView));
            }
            responseMessage.setResponseObject(purInvoiceDTOList);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            genericDTData.setRows(purInvoiceDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }
}
