package in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.puarchasedto.PurReturnDTO;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventoryDetailsPostings;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.BarcodeRepository;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.InventoryDetailsPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.StockTranxDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerOpeningClosingDetailRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.*;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.service.master_service.BranchService;
import in.truethics.ethics.ethicsapiv10.util.*;
import in.truethics.ethics.ethicsapiv10.util.ClosingUtility;
import in.truethics.ethics.ethicsapiv10.util.DateConvertUtil;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import in.truethics.ethics.ethicsapiv10.util.TranxCodeUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

@Service
public class TranxPurReturnsService {

    private static final Logger purchaseReturnLogger = LogManager.getLogger(BranchService.class);
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TranxPurReturnsRepository tranxPurReturnsRepository;
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private TranxPurReturnDutiesTaxesRepository tranxPurReturnDutiesTaxesRepository;
    @Autowired
    private TranxPurReturnAddChargesRepository tranxPurReturnAddChargesRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TranxPurReturnDetailsRepository tranxPurReturnDetailsRepository;
    @Autowired
    private TranxPurReturnProductSrNoRepository tranxPurReturnProdSrNoRepository;
    @Autowired
    private TranxPurChallanRepository tranxPurChallanRepository;
    @Autowired
    private PurchaseInvoiceDetailsRepository invoiceDetailsRepository;
    @Autowired
    private PurchaseInvoiceProductSrNumberRepository serialNumberRepository;
    @Autowired
    private PurInvoiceAdditionalChargesRepository purInvoiceAdditionalChargesRepository;
    @Autowired
    private TranxDebitNoteNewReferenceRepository tranxDebitNoteNewReferenceRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private TranxDebitNoteDetailsRepository tranxDebitNoteDetailsRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private TranxPurReturnDetailsUnitRepository tranxPurReturnDetailsUnitRepository;
    @Autowired
    private TranxPurInvoiceDetailsUnitsRepository tranxPurInvoiceUnitsRepository;
    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private ProductData productData;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;
    @Autowired
    private LedgerMasterRepository ledgerRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice
    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice
    List<Long> mInputList = new ArrayList<>();
    List<Long> ledgerInputList = new ArrayList<>();
    @Autowired
    private FlavourMasterRepository flavourMasterRepository;
    @Autowired
    private TranxPurReturnAdjBillsRepository tranxPurReturnAdjBillsRepository;
    @Autowired
    private NumFormat numFormat;

    @Autowired
    private ClosingUtility closingUtility;
    @Autowired
    private StockTranxDetailsRepository stkTranxDetailsRepository;


    @Autowired
    private BarcodeRepository barcodeRepository;
    @Autowired
    private PostingUtility postingUtility;
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;

    public Object createPurReturnsInvoices(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurReturnInvoice mPurchaseTranx = null;
        ResponseMessage responseMessage = new ResponseMessage();
        mPurchaseTranx = saveIntoPurchaseReturnsInvoice(request);
        if (mPurchaseTranx != null) {
            //
            insertIntoLedgerTranxDetails(mPurchaseTranx);// Accounting Postings-
            /*** creating new reference while adjusting
             return amount into next purchase invoice bill ***/
            if (request.getParameter("paymentMode").equalsIgnoreCase("credit")) {
                TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
                insertIntoNewReference(mPurchaseTranx, request, "create", tranxType.getTransactionCode());
            } else if (paramMap.containsKey("billLst")) {
                /***** dont allow the invoice next time in bill selection module  if invoice is adjusted *****/
                TranxPurInvoice invoice = tranxPurInvoiceRepository.findByIdAndStatus(Long.parseLong(request.getParameter("pur_invoice_id")), true);
                invoice.setTransactionStatus(2L);
                tranxPurInvoiceRepository.save(invoice);
                /* Adjust retun amount into selected purchase invoices */
                String jsonStr = request.getParameter("billLst");
                JsonElement purDetailsJson = new JsonParser().parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                for (JsonElement mElement : array) {
                    JsonObject mObject = mElement.getAsJsonObject();
                    Long invoiceId = 0L;
                    TranxPurInvoice mInvoice = null;
                    if (mObject.has("invoice_id")) invoiceId = mObject.get("invoice_id").getAsLong();
                    if (invoiceId > 0) {
                        mInvoice = tranxPurInvoiceRepository.findByIdAndStatus(invoiceId, true);
                        Double paidAmt = mObject.get("paid_amt").getAsDouble();
                        if (mInvoice != null) {
                            try {
                                // mInvoice.setBalance(mPurchaseTranx.getTranxPurInvoice().getBalance() - paidAmt);
                                mInvoice.setBalance(mObject.get("remaianing_amt").getAsDouble());
                                tranxPurInvoiceRepository.save(mInvoice);
                            } catch (Exception e) {
                                e.printStackTrace();
                                purchaseReturnLogger.error("Exception in Purchase Return:" + e.getMessage());
                            }
                        }
                        /***** Save Into Tranx Purchase Return Adjument Bills Table ******/
                        TranxPurReturnAdjustmentBills mBills = new TranxPurReturnAdjustmentBills();
                        if (mObject.get("source").getAsString().equalsIgnoreCase("pur_invoice"))
                            mBills.setTranxPurInvoice(mInvoice);
                        mBills.setSource(mObject.get("source").getAsString());
                        mBills.setPaidAmt(paidAmt);
                        mBills.setRemainingAmt(mObject.get("remaining_amt").getAsDouble());
                        mBills.setTotalAmt(mObject.get("total_amt").getAsDouble());
                        mBills.setTranxPurReturnId(mPurchaseTranx.getId());
                        mBills.setStatus(true);
                        mBills.setCreatedBy(mPurchaseTranx.getCreatedBy());
                        tranxPurReturnAdjBillsRepository.save(mBills);
                    }
                }
            }


            String strJson = request.getParameter("additionalCharges");
            JsonElement tradeElement = new JsonParser().parse(strJson);
            JsonArray additionalCharges = tradeElement.getAsJsonArray();
            saveIntoPurchaseAdditionalCharges(additionalCharges, mPurchaseTranx, users.getOutlet().getId());

            /***** Insert into DayBook *****/
            saveIntoDayBook(mPurchaseTranx);
            responseMessage.setMessage("Purchase return invoice created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            /**
             * @implNote validation of Ledger Delete , if any tranx done for this ledger, user cant delete this ledger **
             * @auther ashwins@opethic.com
             * @version sprint 21
             **/
            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(mPurchaseTranx.getSundryCreditors().getId(), true);
            ledgerMaster.setIsDeleted(false);
            ledgerMasterRepository.save(ledgerMaster);
        } else {
            responseMessage.setMessage("Error in purchase invoice creation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }


    public void saveIntoPurchaseAdditionalCharges(JsonArray additionalCharges, TranxPurReturnInvoice mPurchaseTranx, Long outletId) {
        List<TranxPurReturnInvoiceAddCharges> chargesList = new ArrayList<>();
        if (mPurchaseTranx.getAdditionalChargesTotal() > 0) {
            for (JsonElement mList : additionalCharges) {
                TranxPurReturnInvoiceAddCharges charges = new TranxPurReturnInvoiceAddCharges();
                JsonObject object = mList.getAsJsonObject();
                Double amount = object.get("amt").getAsDouble();
                Long ledgerId = object.get("ledgerId").getAsLong();
                LedgerMaster addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges.setAmount(amount);
                charges.setAdditionalCharges(addcharges);
                charges.setPurReturnInvoice(mPurchaseTranx);
                //charges.setTranxPurInvoice(mPurchaseTranx.getTranxPurInvoice());
                charges.setStatus(true);
                charges.setOperation("inserted");
                charges.setPercent(object.get("percent").getAsDouble());
                chargesList.add(charges);
            }
        }
        try {
            tranxPurReturnAddChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseAdditionalCharges:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseAdditionalCharges:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveIntoDayBook(TranxPurReturnInvoice mPurchaseTranx) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(mPurchaseTranx.getOutlet());
        if (mPurchaseTranx.getBranch() != null) dayBook.setBranch(mPurchaseTranx.getBranch());
        dayBook.setAmount(mPurchaseTranx.getTotalAmount());
        dayBook.setTranxDate(DateConvertUtil.convertDateToLocalDate(mPurchaseTranx.getTransactionDate()));
        dayBook.setParticulars(mPurchaseTranx.getSundryCreditors().getLedgerName());
        dayBook.setVoucherNo(mPurchaseTranx.getPurRtnNo());
        dayBook.setVoucherType("Purchase Return Invoice");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }

    /*** creating debit note (creating new reference automatically) while adjusting
     return amount ***/
    private void insertIntoNewReference(TranxPurReturnInvoice mPurchaseTranx, HttpServletRequest request, String key, String source) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        if (key.equalsIgnoreCase("create")) {
            tranxDebitNoteNewReference = new TranxDebitNoteNewReferenceMaster();
            tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
            Long count = tranxDebitNoteNewReferenceRepository.findLastRecord(users.getOutlet().getId());
            //SQDEC00001
            String serailNo = String.format("%05d", count + 1);// 5 digit serial number
            //first 3 digits of Current month
            GenerateDates generateDates = new GenerateDates();
            String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
            String dbtnCode = "DBTN" + currentMonth + serailNo;
            tranxDebitNoteNewReference.setDebitnoteNewReferenceNo(dbtnCode);
            tranxDebitNoteNewReference.setStatus(true);
        }
        /****** change adjust to credit while editing purchase return *******/
        if (request.getParameter("paymentMode").equalsIgnoreCase("credit")) {
            tranxDebitNoteNewReference = tranxDebitNoteNewReferenceRepository.findByIdAndStatus(mPurchaseTranx.getId(), true);
            if (tranxDebitNoteNewReference == null) {
                tranxDebitNoteNewReference = new TranxDebitNoteNewReferenceMaster();
                tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
                Long count = tranxDebitNoteNewReferenceRepository.findLastRecord(users.getOutlet().getId());
                //SQDEC00001
                String serailNo = String.format("%05d", count + 1);// 5 digit serial number
                //first 3 digits of Current month
                GenerateDates generateDates = new GenerateDates();
                String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
                String dbtnCode = "DBTN" + currentMonth + serailNo;
                tranxDebitNoteNewReference.setDebitnoteNewReferenceNo(dbtnCode);
                tranxDebitNoteNewReference.setStatus(true);
            }
        }
        if (mPurchaseTranx.getBranch() != null) tranxDebitNoteNewReference.setBranch(mPurchaseTranx.getBranch());
        tranxDebitNoteNewReference.setOutlet(mPurchaseTranx.getOutlet());
        tranxDebitNoteNewReference.setSundryCreditor(mPurchaseTranx.getSundryCreditors());
        /* this parameter segregates whether debit note is from purchase invoice
        or purchase challan*/
        if (paramMap.containsKey("source")) {
            tranxDebitNoteNewReference.setSource(request.getParameter("source"));
            if (request.getParameter("source").equalsIgnoreCase("pur_invoice")) {
                tranxDebitNoteNewReference.setPurchaseInvoiceId(mPurchaseTranx.getTranxPurInvoice().getId());
            } else {
                tranxDebitNoteNewReference.setPurchaseChallanId(mPurchaseTranx.getTranxPurChallan().getId());
            }
        }
        tranxDebitNoteNewReference.setTranxPurReturnInvoice(mPurchaseTranx);

        tranxDebitNoteNewReference.setAdjustmentStatus(request.getParameter("paymentMode"));
        tranxDebitNoteNewReference.setRoundOff(mPurchaseTranx.getRoundOff());
        tranxDebitNoteNewReference.setTotalBaseAmount(mPurchaseTranx.getTotalBaseAmount());
        tranxDebitNoteNewReference.setTaxableAmount(mPurchaseTranx.getTaxableAmount());
        tranxDebitNoteNewReference.setTotalgst(mPurchaseTranx.getTotaligst());
        tranxDebitNoteNewReference.setPurchaseDiscountAmount(mPurchaseTranx.getPurchaseDiscountAmount());
        tranxDebitNoteNewReference.setPurchaseDiscountPer(mPurchaseTranx.getPurchaseDiscountPer());
        tranxDebitNoteNewReference.setTotalPurchaseDiscountAmt(mPurchaseTranx.getTotalPurchaseDiscountAmt());
//        tranxDebitNoteNewReference.setAdditionalChargesTotal(mPurchaseTranx.getAdditionalChargesTotal());
        tranxDebitNoteNewReference.setFinancialYear(mPurchaseTranx.getFinancialYear());
        tranxDebitNoteNewReference.setBalance(mPurchaseTranx.getTotalAmount());
        tranxDebitNoteNewReference.setTotalAmount(mPurchaseTranx.getTotalAmount());
        tranxDebitNoteNewReference.setFiscalYear(mPurchaseTranx.getFiscalYear());
        tranxDebitNoteNewReference.setTranscationDate(mPurchaseTranx.getPurReturnDate());
        try {
            TranxDebitNoteNewReferenceMaster newDebitNote = tranxDebitNoteNewReferenceRepository.save(tranxDebitNoteNewReference);
            TranxDebitNoteDetails mDetails = null;
            if (key.equalsIgnoreCase("create")) {
                mDetails = new TranxDebitNoteDetails();
                mDetails.setOutlet(newDebitNote.getOutlet());
                mDetails.setStatus(true);
                mDetails.setTransactionStatus(transactionStatus);
                mDetails.setSource(source);
            } else {
                mDetails = tranxDebitNoteDetailsRepository.findByStatusAndTranxDebitNoteMasterId(true, newDebitNote.getId());
            }
            if (newDebitNote.getBranch() != null) mDetails.setBranch(newDebitNote.getBranch());
            mDetails.setSundryCreditor(newDebitNote.getSundryCreditor());
            mDetails.setLedgerMaster(newDebitNote.getSundryCreditor());
            mDetails.setTotalAmount(newDebitNote.getTotalAmount());
            mDetails.setPaidAmt(newDebitNote.getTotalAmount());
            mDetails.setAdjustedId(newDebitNote.getPurchaseInvoiceId());
            mDetails.setAdjustedSource("purchase_return");
            mDetails.setBalance(0.0);
            mDetails.setOperations("adjust");
            mDetails.setTranxDebitNoteMaster(newDebitNote);
            tranxDebitNoteDetailsRepository.save(mDetails);
        } catch (Exception e) {
            purchaseReturnLogger.error("Exception in insertIntoNewReference:" + e.getMessage());
        }
    }

    private void insertIntoTranxDebitNoteDetails(TranxDebitNoteNewReferenceMaster newDebitNote) {
        TranxDebitNoteDetails mDetails = new TranxDebitNoteDetails();
        if (newDebitNote.getBranch() != null) mDetails.setBranch(newDebitNote.getBranch());
        mDetails.setOutlet(newDebitNote.getOutlet());
        mDetails.setSundryCreditor(newDebitNote.getSundryCreditor());
        mDetails.setTransactionStatus(newDebitNote.getTransactionStatus());
        mDetails.setTotalAmount(newDebitNote.getTotalAmount());
        mDetails.setBalance(newDebitNote.getTotalAmount());
        if (newDebitNote.getAdjustmentStatus().equalsIgnoreCase("immediate")) {
            mDetails.setAdjustedId(newDebitNote.getPurchaseInvoiceId());
            mDetails.setAdjustedSource("purchase_invoice");
        } else {
            mDetails.setAdjustedSource("Not adjusted yet");
        }
        mDetails.setStatus(true);
        mDetails.setAdjustmentStatus(newDebitNote.getAdjustmentStatus());
        mDetails.setOperations("create");
        tranxDebitNoteDetailsRepository.save(mDetails);
    }

    /******* save into Purchase Return Invoice *******/
    private TranxPurReturnInvoice saveIntoPurchaseReturnsInvoice(HttpServletRequest request) {

        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurReturnInvoice mPurchaseTranx = null;
        TransactionTypeMaster tranxType = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        TranxPurReturnInvoice invoiceTranx = new TranxPurReturnInvoice();
        if (users.getBranch() != null) {
            branch = users.getBranch();
            invoiceTranx.setBranch(branch);
        }
        invoiceTranx.setOutlet(outlet);
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        invoiceTranx.setTranxCode(tranxCode);
        LocalDate mDate = LocalDate.now();
        Date trDt = DateConvertUtil.convertStringToDate(mDate.toString());
        invoiceTranx.setTransactionDate(trDt);
        LocalDate returnDate = LocalDate.parse(request.getParameter("purchase_return_date"));
        Date rtnDate = DateConvertUtil.convertStringToDate(request.getParameter("purchase_return_date"));
        invoiceTranx.setPurReturnDate(rtnDate);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(returnDate);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setPurRtnNo(request.getParameter("pur_return_invoice_no"));
        LedgerMaster purchaseAccount = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("purchase_account_id")), users.getOutlet().getId(), true);
        invoiceTranx.setPurchaseAccountLedger(purchaseAccount);
        LedgerMaster discountLedger = null;
        if (users.getBranch() == null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("purchase discount", users.getOutlet().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("purchase discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        if (discountLedger != null) {
            invoiceTranx.setPurchaseDiscountLedger(discountLedger);
        }
       /* this parameter segregates whether pur return is from purchase invoice
        or purchase challan*/
        if (paramMap.containsKey("source")) {
            if (request.getParameter("source").equalsIgnoreCase("pur_invoice")) {
                if (paramMap.containsKey("pur_invoice_id")) {
                    TranxPurInvoice tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(Long.parseLong(request.getParameter("pur_invoice_id")), true);
                    invoiceTranx.setTranxPurInvoice(tranxPurInvoice);
                }
            } else {
                if (paramMap.containsKey("pur_challan_id")) {
                    TranxPurChallan tranxPurChallan = tranxPurChallanRepository.findByIdAndStatus(Long.parseLong(request.getParameter("pur_challan_id")), true);
                    invoiceTranx.setTranxPurChallan(tranxPurChallan);
                }
            }
        }
        LedgerMaster sundryCreditors = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), users.getOutlet().getId(), true);
        invoiceTranx.setSundryCreditors(sundryCreditors);
        //invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setPurchaseRoundOff(roundoff);
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
        //   invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("value")));
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
        Double totalQty = Double.parseDouble(request.getParameter("total_qty"));

        invoiceTranx.setTotalqty(totalQty.longValue());
//        invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setPurchaseDiscountPer(Double.parseDouble(request.getParameter("purchase_discount")));
        invoiceTranx.setPurchaseDiscountAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
        invoiceTranx.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_purchase_discount_amt")));
        invoiceTranx.setPurReturnSrno(Long.parseLong(request.getParameter("purchase_return_sr_no")));
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setOperations("insert");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
        else invoiceTranx.setNarration("");
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("purchase_return_date"));
        invoiceTranx.setPurReturnDate(strDt);
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                invoiceTranx.setGstNumber(request.getParameter("gstNo"));
            }
        }
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        invoiceTranx.setPaymentMode(request.getParameter("paymentMode"));
        if (paramMap.containsKey("isRoundOffCheck"))
            invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));
        else {
            invoiceTranx.setIsRoundOff(false);
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
        }
        if (request.getParameterMap().containsKey("transactionTrackingNo"))
            invoiceTranx.setTransactionTrackingNo(request.getParameter("transactionTrackingNo"));
        else invoiceTranx.setTransactionTrackingNo(String.valueOf(new Date().getTime()));
        try {
            mPurchaseTranx = tranxPurReturnsRepository.save(invoiceTranx);
            if (mPurchaseTranx != null) {
                /* Save into Duties and Taxes */
                String taxStr = request.getParameter("taxCalculation");
                //  JsonObject duties_taxes = new JsonObject(taxStr);
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoPurchaseDutiesTaxes(duties_taxes, mPurchaseTranx, taxFlag);
                JsonParser parser = new JsonParser();
                /* save into Purchase Invoice Details */
                String jsonStr = request.getParameter("row");
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                saveIntoPurchaseInvoiceDetails(array, mPurchaseTranx, branch, outlet, users.getId(), tranxType, request);
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseReturnsInvoice:" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseReturnsInvoice:" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mPurchaseTranx;
    }

    /****** Save into Duties and Taxes ******/
    public void saveIntoPurchaseDutiesTaxes(JsonObject duties_taxes, TranxPurReturnInvoice mPurchaseTranx, Boolean taxFlag) {
        List<TranxPurReturnInvoiceDutiesTaxes> purchaseDutiesTaxes = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (JsonElement mList : cgstList) {
                    TranxPurReturnInvoiceDutiesTaxes taxes = new TranxPurReturnInvoiceDutiesTaxes();
                    JsonObject cgstObject = mList.getAsJsonObject();
                    //   JsonObject cgstObject = cgstList.getJSONObject(i);
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT CGST " + inputGst;
                   /* dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCase(
                            mPurchaseTranx.getOutlet().getId(), ledgerName);*/
                    if (mPurchaseTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPurchaseTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                        taxes.setDutiesTaxes(dutiesTaxes);
                    }
                    taxes.setAmount(Double.parseDouble(cgstObject.get("amt").getAsString()));
                    taxes.setPurReturnInvoice(mPurchaseTranx);
                    //taxes.setTranxPurInvoice(mPurchaseTranx.getTranxPurInvoice());
                    taxes.setSundryCreditors(mPurchaseTranx.getSundryCreditors());
                    taxes.setIntra(taxFlag);
                    taxes.setStatus(true);
                    purchaseDutiesTaxes.add(taxes);
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (JsonElement mList : sgstList) {
                    TranxPurReturnInvoiceDutiesTaxes taxes = new TranxPurReturnInvoiceDutiesTaxes();
                    JsonObject sgstObject = mList.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //  int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT SGST " + inputGst;
                  /*  dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCase(
                            mPurchaseTranx.getOutlet().getId(), ledgerName);*/
                    if (mPurchaseTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPurchaseTranx.getOutlet().getId(), mPurchaseTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPurchaseTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        taxes.setDutiesTaxes(dutiesTaxes);
                    }
                    taxes.setAmount(Double.parseDouble(sgstObject.get("amt").getAsString()));
                    taxes.setPurReturnInvoice(mPurchaseTranx);
                    //taxes.setTranxPurInvoice(mPurchaseTranx.getTranxPurInvoice());
                    taxes.setSundryCreditors(mPurchaseTranx.getSundryCreditors());
                    taxes.setIntra(taxFlag);
                    taxes.setStatus(true);
                    purchaseDutiesTaxes.add(taxes);
                }
            }
        } else {
            JsonArray igstList = duties_taxes.getAsJsonArray("igst");
            /* this is for Igst creation */
            if (igstList != null && igstList.size() > 0) {
                for (JsonElement mList : igstList) {
                    TranxPurReturnInvoiceDutiesTaxes taxes = new TranxPurReturnInvoiceDutiesTaxes();
                    JsonObject igstObject = mList.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
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
                        taxes.setDutiesTaxes(dutiesTaxes);
                    }
                    taxes.setAmount(Double.parseDouble(igstObject.get("amt").getAsString()));
                    taxes.setPurReturnInvoice(mPurchaseTranx);
                    // taxes.setTranxPurInvoice(mPurchaseTranx.getTranxPurInvoice());
                    taxes.setSundryCreditors(mPurchaseTranx.getSundryCreditors());
                    taxes.setIntra(taxFlag);
                    taxes.setStatus(true);
                    purchaseDutiesTaxes.add(taxes);
                }
            }
        }
        try {
            /* save all Duties and Taxes into purchase Invoice Duties taxes table */
            tranxPurReturnDutiesTaxesRepository.saveAll(purchaseDutiesTaxes);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            purchaseReturnLogger.error("Exception saveIntoPurchaseDutiesTaxes:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            purchaseReturnLogger.error("Exception saveIntoPurchaseDutiesTaxes:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    /* End of Purchase return Duties and Taxes Ledger */

    /**** Save Into Purchase Return Additional Charges    *****/
    public void saveIntoPurchaseAdditionalCharges(JsonArray additionalCharges, TranxPurReturnInvoice mPurchaseTranx,
                                                  Long tranxId, Long outletId) {
        List<TranxPurReturnInvoiceAddCharges> chargesList = new ArrayList<>();
        if (mPurchaseTranx.getAdditionalChargesTotal() > 0) {
            for (JsonElement mList : additionalCharges) {
                TranxPurReturnInvoiceAddCharges charges = new TranxPurReturnInvoiceAddCharges();
                JsonObject object = mList.getAsJsonObject();
                Double amount = object.get("amt").getAsDouble();
                Long ledgerId = object.get("ledger").getAsLong();
                LedgerMaster addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges.setAmount(amount);
                charges.setAdditionalCharges(addcharges);
                charges.setPurReturnInvoice(mPurchaseTranx);
                //charges.setTranxPurInvoice(mPurchaseTranx.getTranxPurInvoice());
                charges.setStatus(true);
                charges.setOperation("inserted");
                chargesList.add(charges);
            }
        }
        try {
            tranxPurReturnAddChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseAdditionalCharges:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseAdditionalCharges:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    /* End of Purchase return Additional Charges */

    /****** save into Purchase Return Invoice Details ******/
    public void saveIntoPurchaseInvoiceDetails(JsonArray array, TranxPurReturnInvoice mPurchaseTranx,
                                               Branch branch, Outlet outlet, Long userId,
                                               TransactionTypeMaster tranxType, HttpServletRequest request) {
        /* Purchase Product Details Start here */
        Map<String, String[]> paramMap = request.getParameterMap();
        TransactionStatus status = transactionStatusRepository.findByIdAndStatus(2L, true);
        List<TranxPurReturnInvoiceProductSrNo> newSerialNumbers = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            TranxPurReturnDetailsUnits invoiceUnits = new TranxPurReturnDetailsUnits();
            /* inserting into TranxSalesInvoiceDetailsUnits */
            // JsonArray productDetails = object.get("brandDetails").getAsJsonArray();
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
            invoiceUnits.setTranxPurReturnInvoice(mPurchaseTranx);
            invoiceUnits.setProduct(mProduct);
            invoiceUnits.setUnits(units);
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            tranxQty = object.get("qty").getAsDouble();
            if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase("")) {
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
            if (object.has("unit_conv")) invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
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
            TranxPurInvoiceDetailsUnits mUnits = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatusAndProductId(mPurchaseTranx.getTranxPurInvoice().getId(), true, mProduct.getId());
            Double total_qty = mUnits.getReturnQty() + object.get("qty").getAsDouble();
            if (mUnits.getQty().doubleValue() == total_qty.doubleValue()) {
                mUnits.setTransactionStatus(status);
            }
            mUnits.setReturnQty(total_qty);
            tranxPurInvoiceUnitsRepository.save(mUnits);
            boolean flag = false;
            try {
                if (object.has("is_batch")) {
                    if (object.get("is_batch").getAsBoolean()) {
                        flag = true;
                 /*   int qty = object.get("qty").getAsInt();
                    int free_qty = 0;
                    if (!object.get("free_qty").getAsString().equalsIgnoreCase(""))
                        free_qty = object.get("free_qty").getAsInt();
                    double net_amt = object.get("final_amt").getAsDouble();
                    double costing = 0;
                    double costing_with_tax = 0;*/

                  /*  if (outlet.getGstTypeMaster().getId() == 1L) { //Registered
                        net_amt = object.get("total_amt").getAsDouble();
                        costing = net_amt / (qty + free_qty);
                        costing_with_tax = costing + object.get("total_igst").getAsDouble();
                    } else { // composition or un-registered
                        costing = net_amt / (qty + free_qty);
                        costing_with_tax = costing;
                    }*/


                        if (object.get("b_details_id").getAsLong() == 0) {
                            ProductBatchNo mproductBatchNo = new ProductBatchNo();
                            if (object.has("b_no")) mproductBatchNo.setBatchNo(object.get("b_no").getAsString());
                            mproductBatchNo.setMrp(0.0);
                            if (object.has("b_rate") && !object.get("b_rate").getAsString().equalsIgnoreCase(""))
                                mproductBatchNo.setMrp(object.get("b_rate").getAsDouble());
                            mproductBatchNo.setPurchaseRate(0.0);
                            if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").getAsString().equals(""))
                                mproductBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                            if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                            Double qnty = object.get("qty").getAsDouble();
                            mproductBatchNo.setQnty(qnty.intValue());
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
                            if (object.has("min_margin") && !object.get("min_margin").getAsString().equals(""))
                                mproductBatchNo.setMinMargin(object.get("min_margin").getAsDouble());
                            if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") && !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                                mproductBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                            mproductBatchNo.setStatus(true);
                            mproductBatchNo.setProduct(mProduct);
                            mproductBatchNo.setOutlet(outlet);
                            mproductBatchNo.setBranch(branch);
                            if (levelA != null) mproductBatchNo.setLevelA(levelA);
                            if (levelB != null) mproductBatchNo.setLevelB(levelB);
                            if (levelC != null) mproductBatchNo.setLevelC(levelC);
                            mproductBatchNo.setUnits(units);
                            mproductBatchNo.setSupplierId(mPurchaseTranx.getSundryCreditors().getId());
                            try {
                                productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("Exception " + e.getMessage());
                            }
                        } else {
                            productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                            Double qnty = object.get("qty").getAsDouble();
                            productBatchNo.setQnty(qnty.intValue());
                            productBatchNo.setFreeQty(free_qty);
                            if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equalsIgnoreCase(""))
                                productBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                            if (object.has("costing") && !object.get("costing").isJsonNull())
                                productBatchNo.setCosting(object.get("costing").getAsDouble());
                            if (object.has("costing_with_tax") && !object.get("costing_with_tax").isJsonNull())
                                productBatchNo.setCostingWithTax(object.get("costing_with_tax").getAsDouble());
                            if (object.has("b_no")) productBatchNo.setBatchNo(object.get("b_no").getAsString());
                            if (object.has("b_rate") && !object.get("b_rate").getAsString().equals(""))
                                productBatchNo.setMrp(object.get("b_rate").getAsDouble());
                            if (object.has("b_sale_rate") && !object.get("b_sale_rate").getAsString().equals(""))
                                productBatchNo.setSalesRate(object.get("b_sale_rate").getAsDouble());
                            if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").getAsString().equals(""))
                                productBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                            if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                                productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                            if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") && !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                                productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                            if (object.has("rate_a") && !object.get("rate_a").getAsString().equalsIgnoreCase(""))
                                productBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                            if (object.has("rate_b") && !object.get("rate_b").getAsString().equalsIgnoreCase(""))
                                productBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                            if (object.has("rate_c") && !object.get("rate_c").getAsString().equalsIgnoreCase(""))
                                productBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                            if (object.has("min_margin") && !object.get("min_margin").getAsString().equalsIgnoreCase(""))
                                productBatchNo.setMinMargin(object.get("min_margin").getAsDouble());
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
                }
                invoiceUnits.setProductBatchNo(productBatchNo);
                TranxPurReturnDetailsUnits mTranxUnitDetails = tranxPurReturnDetailsUnitRepository.save(invoiceUnits);
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
                    /*Double prrate = object.get("rate").getAsDouble();
                    ProductUnitPacking mUnitPackaging = productUnitRepository.findRate(
                            mProduct.getId(), levelAId, levelBId, levelCId, units.getId(), true);
                    if (mUnitPackaging != null) {
                        if (mUnitPackaging.getPurchaseRate() != 0 && prrate != mUnitPackaging.getPurchaseRate()) {
                            mUnitPackaging.setPurchaseRate(prrate);
                            productUnitRepository.save(mUnitPackaging);
                        }
                    }*/
                    /******* Insert into Tranx Product Seial Numbers  ******/
                    JsonArray jsonArray = object.getAsJsonArray("serialNo");
                    if (jsonArray != null && jsonArray.size() > 0) {
                        List<TranxPurReturnInvoiceProductSrNo> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            TranxPurReturnInvoiceProductSrNo productSerialNumber = new TranxPurReturnInvoiceProductSrNo();
                            productSerialNumber.setProduct(mProduct);
                            productSerialNumber.setSerialNo(serialNo);
                            // productSerialNumber.setPurReturnInvoice(mPurchaseTranx);
                            productSerialNumber.setTransactionStatus("Purchase Return");
                            productSerialNumber.setStatus(true);
                            productSerialNumber.setCreatedBy(userId);
                            productSerialNumber.setOperations("Inserted");
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setBranch(mPurchaseTranx.getBranch());
                            productSerialNumber.setOutlet(mPurchaseTranx.getOutlet());
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setUnits(units);
                            productSerialNumber.setTranxPurReturnDetailsUnits(mTranxUnitDetails);
                            productSerialNumber.setLevelA(levelA);
                            productSerialNumber.setLevelB(levelB);
                            productSerialNumber.setLevelC(levelC);
                            productSerialNumber.setUnits(units);
                            TranxPurReturnInvoiceProductSrNo mSerialNo = tranxPurReturnProdSrNoRepository.save(productSerialNumber);
                            if (mProduct.getIsInventory()) {
                                inventoryCommonPostings.callToInventoryPostings("DR", mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear(), serialNo);
                            }
                        }
                    }
                    flag = true;
                }

            } catch (Exception e) {
                purchaseReturnLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            try {
               /* if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                    flag = true;
                }*/
                /**** Inventory Postings *****/

                /***** new architecture of Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("DR", mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear(), serialNo);
                    /***** End of new architecture of Inventory Postings *****/

                    /**
                     * @implNote New Logic of opening and closing Inventory posting
                     * @auther ashwins@opethic.com
                     * @version sprint 1
                     **/
                    closingUtility.stockPosting(outlet, branch, mPurchaseTranx.getFiscalYear().getId(), batchId,
                            mProduct, tranxType.getId(), mPurchaseTranx.getPurReturnDate(), tranxQty, free_qty,
                            mPurchaseTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                            mPurchaseTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());

                    closingUtility.stockPostingBatchWise(outlet, branch, mPurchaseTranx.getFiscalYear().getId(), batchId,
                            mProduct, tranxType.getId(), mPurchaseTranx.getPurReturnDate(), tranxQty, free_qty,
                            mPurchaseTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                            mPurchaseTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());
                    /***** End of new logic of Inventory Postings *****/
                }
                /***** End of new architecture of Inventory Postings *****/
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            }

        }


    }
    /* End of Purchase return Details */

    /* Purchase Returns Last Records */
    public JsonObject purReturnsLastRecord(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxPurReturnsRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxPurReturnsRepository.findLastRecord(users.getOutlet().getId());
        }
        //SQDEC00001
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
       /* String compan+yName = users.getOutlet().getCompanyName();
        companyName = companyName.substring(0, 3);*/ // fetching first 3 digits from company names
        /* getting Start and End year from fiscal Year */
    /*   String startYear = generateFiscalYear.getStartYear();
        String endYear = generateFiscalYear.getEndYear();*/
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
     /*   String csCode = companyName.toUpperCase() + "-" + startYear + endYear
                + "-" + "PR" + currentMonth + "-" + serailNo;*/
        String csCode = "PR" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("purReturnNo", csCode);
        return result;
    }

    /* Postings of Ledgers while purchase returns*/
    private void insertIntoLedgerTranxDetails(TranxPurReturnInvoice mPurchaseTranx) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
        try {
            insertIntoTranxDetailSC(mPurchaseTranx, tranxType, "DR", "Insert"); // for Sundry Creditors : dr
            insertIntoTranxDetailPA(mPurchaseTranx, tranxType, "CR", "Insert"); // for Purchase Accounts : cr
            insertIntoTranxDetailPD(mPurchaseTranx, tranxType, "DR", "Insert"); // for Purchase Discounts : dr
            insertIntoTranxDetailRO(mPurchaseTranx, tranxType); // for Round Off : cr or dr
            //   insertDB(mPurchaseTranx, "AC", tranxType, "CR", "Insert"); // for Additional Charges : cr
            insertDB(mPurchaseTranx, "DT", tranxType, "CR", "Insert"); // for Duties and Taxes : cr
        } catch (Exception e) {
            //e.printStackTrace();
            purchaseReturnLogger.error("Exception in insertIntoLedgerTranxDetails:" + e.getMessage());
            System.out.println("Posting Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /* Insertion into Transaction Details Table of Sundry Creditors Ledgers for Purchase Invoice */
    public void insertIntoTranxDetailSC(TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        try {

            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mPurchaseTranx.getTotalAmount(), mPurchaseTranx.getSundryCreditors(), tranxType, mPurchaseTranx.getSundryCreditors().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), crdrType, true, "Purchase Return", operation);


            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, crdrType, mPurchaseTranx.getTotalAmount(), mPurchaseTranx.getFiscalYear(),
                        mPurchaseTranx.getSundryCreditors(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(),
                        mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(), mPurchaseTranx.getTranxCode());
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
                    postingUtility.updateLedgerPostings(mPurchaseTranx.getSundryCreditors(), mPurchaseTranx.getPurReturnDate(),
                            tranxType, mPurchaseTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            purchaseReturnLogger.error("Exception in insertIntoTranxDetailSC:" + e.getMessage());
            //e.printStackTrace();
            System.out.println("Store Procedure Error " + e.getMessage());
        }
    }

    /* Insertion into Transaction Details Table of Purchase Accounts Ledgers for Purchase Invoice*/
    public void insertIntoTranxDetailPA(TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mPurchaseTranx.getTotalBaseAmount(), mPurchaseTranx.getPurchaseAccountLedger(), tranxType, mPurchaseTranx.getPurchaseAccountLedger().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), crdrType, true, "Purchase Return", operation);


        if (operation.equalsIgnoreCase("insert")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedger(tranxType, crdrType, mPurchaseTranx.getTotalBaseAmount(),
                    mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getPurchaseAccountLedger(), mPurchaseTranx.getPurReturnDate(),
                    mPurchaseTranx.getId(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(), mPurchaseTranx.getTranxCode());
        }

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
    public void insertIntoTranxDetailPD(TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        try {
            if (mPurchaseTranx.getPurchaseDiscountLedger() != null) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(mPurchaseTranx.getTotalPurchaseDiscountAmt(), mPurchaseTranx.getPurchaseDiscountLedger(), tranxType, mPurchaseTranx.getPurchaseDiscountLedger().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), crdrType, true, "Purchase Return", operation);
            }

            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                if (mPurchaseTranx.getPurchaseDiscountLedger() != null) {
                    postingUtility.callToPostingLedger(tranxType, crdrType, mPurchaseTranx.getTotalPurchaseDiscountAmt(),
                            mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getPurchaseDiscountLedger(), mPurchaseTranx.getPurReturnDate(),
                            mPurchaseTranx.getId(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(), mPurchaseTranx.getTranxCode());
                }
            }

            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                if (mPurchaseTranx.getPurchaseDiscountLedger() != null) {
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
            // e.printStackTrace();
            purchaseReturnLogger.error("Exception in insertIntoTranxDetailPD:" + e.getMessage());
            System.out.println("Posting Discount Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /* Insertion into Transaction Details Table of Purchase RoundOff Ledgers for Purchase Invoice*/
    private void insertIntoTranxDetailRO(TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType) {
        String tranxAction = "CR";
        if (mPurchaseTranx.getRoundOff() >= 0) {
            tranxAction = "DR";
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), "CR", true, "Purchase Return", "Insert");
        } else if (mPurchaseTranx.getRoundOff() < 0) {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), "DR", true, "Purchase Return", "Insert");
        }

        /**** NEW METHOD FOR LEDGER POSTING ****/
        postingUtility.callToPostingLedger(tranxType, tranxAction, Math.abs(mPurchaseTranx.getRoundOff()),
                mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getPurchaseRoundOff(), mPurchaseTranx.getPurReturnDate(),
                mPurchaseTranx.getId(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(), mPurchaseTranx.getTranxCode());
    }

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

    public void insertFromDutiesTaxes(TranxPurReturnInvoiceDutiesTaxes mDuties, TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {

        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mDuties.getAmount(), mDuties.getDutiesTaxes(), tranxType, mDuties.getDutiesTaxes().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), crdrType, true, "Purchase Return", operation);

        if (operation.equalsIgnoreCase("insert")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedger(tranxType, crdrType, mDuties.getAmount(),
                    mPurchaseTranx.getFiscalYear(), mDuties.getDutiesTaxes(), mPurchaseTranx.getPurReturnDate(),
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
                postingUtility.updateLedgerPostings(mDuties.getDutiesTaxes(), mPurchaseTranx.getPurReturnDate(),
                        tranxType, mPurchaseTranx.getFiscalYear(), detail);
            }
        }
    }

    public void insertFromAdditionalCharges(TranxPurReturnInvoiceAddCharges mAdditinoalCharges, TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mAdditinoalCharges.getAmount(), mAdditinoalCharges.getAdditionalCharges(), tranxType, mAdditinoalCharges.getAdditionalCharges().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), crdrType, true, "Purchase Return", operation);


        if (operation.equalsIgnoreCase("insert")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedger(tranxType, crdrType, mAdditinoalCharges.getAmount(),
                    mPurchaseTranx.getFiscalYear(), mAdditinoalCharges.getAdditionalCharges(), mPurchaseTranx.getPurReturnDate(),
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
                postingUtility.updateLedgerPostings(mAdditinoalCharges.getAdditionalCharges(), mPurchaseTranx.getPurReturnDate(),
                        tranxType, mPurchaseTranx.getFiscalYear(), detail);
            }
        }
    }

    /* find all Purchase Invoices and Purchase Challans of Sundry Creditors/Suppliers wise , for Purchase Returns */
    public JsonObject purchaseListSupplierWise(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonArray result = new JsonArray();
        List<TranxPurInvoice> purInvoice = new ArrayList<>();
        List<TranxPurChallan> purChallans = new ArrayList<>();
        Long ledgerId = Long.parseLong(request.getParameter("sundry_creditor_id"));
        if (paramMap.containsKey("dateFrom") && paramMap.containsKey("dateTo")) {
            if (users.getBranch() != null) {
                purInvoice = tranxPurInvoiceRepository.findByBranchSuppliersWithDates(users.getOutlet().getId(), true, ledgerId, request.getParameter("dateFrom"), request.getParameter("dateTo"), users.getBranch().getId());
            } else {
                purInvoice = tranxPurInvoiceRepository.findBySuppliersWithDates(users.getOutlet().getId(), true, ledgerId, request.getParameter("dateFrom"), request.getParameter("dateTo"));
            }
        } else {
            if (users.getBranch() != null) {
                purInvoice = tranxPurInvoiceRepository.findByOutletIdAndBranchIdAndStatusAndSundryCreditorsId(users.getOutlet().getId(), users.getBranch().getId(), true, ledgerId);
            } else {
                purInvoice = tranxPurInvoiceRepository.findByOutletIdAndStatusAndSundryCreditorsIdAndBranchIsNull(users.getOutlet().getId(), true, ledgerId);
            }
        }
        if (purInvoice != null && purInvoice.size() > 0) {
            for (TranxPurInvoice invoices : purInvoice) {
                List<TranxPurInvoiceDetailsUnits> unitList = tranxPurInvoiceUnitsRepository.findByClosedStatus(invoices.getId());
                int cnt = 0;
                for (TranxPurInvoiceDetailsUnits mDetails : unitList) {
                    if (mDetails.getTransactionStatus().getId() == 1L) {
                        cnt++;
                        break;
                    }
                }
                if (cnt != 0) {
                    JsonObject response = new JsonObject();
                    response.addProperty("source", "pur_invoice");
                    response.addProperty("id", invoices.getId());
                    response.addProperty("invoice_no", invoices.getVendorInvoiceNo());
                    response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(
                            invoices.getInvoiceDate()).toString());
                    response.addProperty("transaction_date", invoices.getTransactionDate().toString());
                    response.addProperty("purchase_serial_number", invoices.getSrno());
                    response.addProperty("total_amount", Math.abs(invoices.getTotalAmount()));
                    if (invoices.getTotalAmount() > 0) {
                        response.addProperty("balance_type", "CR");
                    } else {
                        response.addProperty("balance_type", "DR");
                    }
                    response.addProperty("sundry_creditor_name", invoices.getSundryCreditors().getLedgerName());
                    response.addProperty("sundry_creditor_id", ledgerId);
                    result.add(response);
                }
            }
        }
        /* challan list */
        /*if (paramMap.containsKey("dateFrom") && paramMap.containsKey("dateTo")) {
            purChallans = tranxPurChallanRepository.findBySuppliersWithDates(
                    users.getOutlet().getId(), true,
                    ledgerId, request.getParameter("dateFrom"), request.getParameter("dateTo"));
        } else {
            purChallans = tranxPurChallanRepository.
                    findBySundryCreditorsIdAndOutletIdAndTransactionStatusIdAndStatus(
                            ledgerId, users.getOutlet().getId(), 1L, true);
        }
        if (purChallans != null && purChallans.size() > 0) {
            for (TranxPurChallan invoices : purChallans) {
                JsonObject response = new JsonObject();
                response.addProperty("source", "pur_challan");
                response.addProperty("id", invoices.getId());
                response.addProperty("invoice_no", invoices.getVendorInvoiceNo());
                response.addProperty("invoice_date", invoices.getInvoiceDate().toString());
                response.addProperty("transaction_date", invoices.getTransactionDate().toString());
                response.addProperty("purchase_serial_number", invoices.getPurChallanSrno());
                response.addProperty("total_amount", Math.abs(invoices.getTotalAmount()));
                if (invoices.getTotalAmount() > 0) {
                    response.addProperty("balance_type", "CR");
                } else {
                    response.addProperty("balance_type", "DR");
                }
                response.addProperty("sundry_creditor_name",
                        invoices.getSundryCreditors().getLedgerName());
                response.addProperty("sundry_creditor_id", ledgerId);
                result.add(response);
            }
        }*/

        JsonObject output = new JsonObject();
        if (result.size() > 0) {
            output.addProperty("message", "success");
            output.addProperty("responseStatus", HttpStatus.OK.value());
            output.add("data", result);
        } else {
            output.addProperty("message", "No Bills Found");
            output.addProperty("responseStatus", HttpStatus.NO_CONTENT.value());
            output.add("data", result);
        }
        return output;
    }

    /**
     * Delete Purchase Return
     **/
    public JsonObject purchaseReturnDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxPurReturnInvoice invoiceTranx = tranxPurReturnsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TranxPurReturnInvoice mPurchaseTranx;
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
        try {
            invoiceTranx.setStatus(false);
            invoiceTranx.setOperations("deletion");
            /**** Reverse Postings of Accounting *****/
            deleteAccountingPostings(invoiceTranx);
            /*** delete the debit note reference table ****/
            TranxDebitNoteNewReferenceMaster debitNoteMaster = tranxDebitNoteNewReferenceRepository.findByTranxPurReturnInvoiceIdAndStatus(invoiceTranx.getId(), true);
            if (debitNoteMaster != null) {
                debitNoteMaster.setStatus(false);
                tranxDebitNoteNewReferenceRepository.save(debitNoteMaster);
            }
            /**** Reverse the Ineventory Postings *****/
            List<TranxPurReturnDetailsUnits> unitsList = new ArrayList<>();
            unitsList = tranxPurReturnDetailsUnitRepository.findByTranxPurReturnInvoiceIdAndStatus(invoiceTranx.getId(), true);
            if (unitsList != null && unitsList.size() > 0) {
                for (TranxPurReturnDetailsUnits mUnitObjects : unitsList) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("CR", invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), mUnitObjects.getQty(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), mUnitObjects.getProduct(), tranxType, null, null, null, mUnitObjects.getUnits(), mUnitObjects.getProductBatchNo(), mUnitObjects.getProductBatchNo() != null ? mUnitObjects.getProductBatchNo().getBatchNo() : null, invoiceTranx.getFiscalYear(), null);
                    /***** End of new architecture of Inventory Postings *****/
                }
            }
            /**** make status=0 to all ledgers of respective purchase invoice id, due to this we wont get
             details of deleted invoice when we want get details of respective ledger ****/
            List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
            mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(tranxType.getId(), invoiceTranx.getId(), true);
            for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                try {
                    mPostings.setStatus(false);
                    ledgerTransactionPostingsRepository.save(mPostings);
                } catch (Exception e) {
                    purchaseReturnLogger.error("Exception in Delete functionality for all ledgers of " + "deleted purchase return invoice->" + e.getMessage());
                }
            }

            /***** NEW METHOD FOR LEDGER POSTING *****/
            /**** make status=0 to all ledgers of respective purchase invoice id, due to this we wont get
             details of deleted invoice when we want get details of respective ledger ****/
            List<LedgerOpeningClosingDetail> detailList = ledgerOpeningClosingDetailRepository.findByTranxTypeIdAndTranxIdAndStatus(
                    tranxType.getId(), invoiceTranx.getId(), true);
            for (LedgerOpeningClosingDetail ledgerDetail : detailList) {
                try {
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), invoiceTranx.getPurReturnDate(),
                                tranxType, invoiceTranx.getFiscalYear(), detail);
                    }
                } catch (Exception e) {
                    purchaseReturnLogger.error("Exception in Delete functionality for all ledgers of " + "deleted purchase return invoice->" + e.getMessage());
                }
            }
            if (invoiceTranx != null) {
                mPurchaseTranx = tranxPurReturnsRepository.save(invoiceTranx);
                jsonObject.addProperty("message", "Purchase return invoice deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in purchase deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            purchaseReturnLogger.error("Error in purchaseDelete()->" + e.getMessage());
        }
        return jsonObject;
    }

    private void deleteAccountingPostings(TranxPurReturnInvoice mPurchaseTranx) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
        try {
            insertIntoTranxDetailSC(mPurchaseTranx, tranxType, "CR", "Delete"); // for Sundry Creditors : CR
            insertIntoTranxDetailPA(mPurchaseTranx, tranxType, "DR", "Delete"); // for Purchase Accounts : DR
            insertIntoTranxDetailPD(mPurchaseTranx, tranxType, "CR", "Delete"); // for Purchase Discounts : CR
            deleteIntoTranxDetailRO(mPurchaseTranx, tranxType); // for Round Off : cr or dr
            insertDB(mPurchaseTranx, "AC", tranxType, "DR", "Delete"); // for Additional Charges : DR
            insertDB(mPurchaseTranx, "DT", tranxType, "DR", "Delete"); // for Duties and Taxes : DR
        } catch (Exception e) {
            //e.printStackTrace();
            purchaseReturnLogger.error("Exception in deleteLedgerTranxDetails:" + e.getMessage());
            System.out.println("Posting Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteIntoTranxDetailRO(TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType) {

        if (mPurchaseTranx.getRoundOff() >= 0) {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), "DR", true, tranxType.getTransactionName(), "Delete");
        } else if (mPurchaseTranx.getRoundOff() < 0) {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mPurchaseTranx.getRoundOff(), mPurchaseTranx.getPurchaseRoundOff(), tranxType, mPurchaseTranx.getPurchaseRoundOff().getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), "CR", true, "Purchase Return", "Delete");
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
    }

    /* Purchase Returns:  find all products of selected purchase invoice bill of sundry creditor */
    public JsonObject productListPurInvoice(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurInvoiceDetails> list = new ArrayList<>();
        List<TranxPurchaseInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxPurInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("pur_invoice_id"));
            TranxPurInvoice purchaseInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            list = invoiceDetailsRepository.findByPurchaseTransactionIdAndStatus(id, true);
         /*   serialNumbers = serialNumberRepository.findByPurchaseTransactionIdAndStatus(purchaseInvoice.getId(),true);
            additionalCharges = purInvoiceAdditionalChargesRepository.findByPurchaseTransactionIdAndStatus(
                    purchaseInvoice.getId(), true);*/
            finalResult.addProperty("tcs", purchaseInvoice.getTcs());
            finalResult.addProperty("narration", purchaseInvoice.getNarration() != null ? purchaseInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", purchaseInvoice.getPurchaseDiscountLedger() != null ? purchaseInvoice.getPurchaseDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", purchaseInvoice.getPurchaseDiscountAmount());
            finalResult.addProperty("discountInPer", purchaseInvoice.getPurchaseDiscountPer());
            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", purchaseInvoice.getId());
            result.addProperty("invoice_dt", purchaseInvoice.getInvoiceDate().toString());
            result.addProperty("invoice_no", purchaseInvoice.getVendorInvoiceNo());
            result.addProperty("purchase_sr_no", purchaseInvoice.getSrno());
            result.addProperty("purchase_account_ledger_id", purchaseInvoice.getPurchaseAccountLedger().getId());
            result.addProperty("supplierId", purchaseInvoice.getSundryCreditors().getId());
            result.addProperty("supplier_name", purchaseInvoice.getSundryCreditors().getLedgerName());
            result.addProperty("transaction_dt", purchaseInvoice.getTransactionDate().toString());
            result.addProperty("gstNo", purchaseInvoice.getGstNumber());
            /* End of Purchase Invoice Data */

            /* Purchase Invoice Details */
            JsonArray row = new JsonArray();
            List<TranxPurInvoiceDetailsUnits> unitsArray = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatus(purchaseInvoice.getId(), true);
            for (TranxPurInvoiceDetailsUnits mUnits : unitsArray) {
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
                if (mUnits.getProductBatchNo() != null) {
                    if (mUnits.getProductBatchNo().getExpiryDate() != null) {
//                        if (purchaseInvoice.getInvoiceDate().isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
                        if (purchaseInvoice.getInvoiceDate().after(DateConvertUtil.convertStringToDate(mUnits.getProductBatchNo().getExpiryDate().toString()))) {
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

            /* End of Purchase Invoice Details */

            /* sales Additional Charges */
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
            //e.printStackTrace();
            purchaseReturnLogger.error("Exception in productListPurInvoice:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            //e.printStackTrace();
            purchaseReturnLogger.error("Exception in productListPurInvoice:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    /* list of all selected products against purchase invoice bill for purchase returns */
  /*  public JsonObject getInvoiceByIdWithProductsId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurInvoiceDetails> list = new ArrayList<>();
        List<TranxPurchaseInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxPurInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("invoice_id"));
            TranxPurInvoice purchaseInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            String str = request.getParameter("product_ids");
            list = invoiceDetailsRepository.findInvoiceByIdWithProductsId(id, true, str);
         */
    /*   serialNumbers = serialNumberRepository.findByPurchaseTransactionIdAndStatus(purchaseInvoice.getId(),true);
            additionalCharges = purInvoiceAdditionalChargesRepository.findByPurchaseTransactionIdAndStatus(
                    purchaseInvoice.getId(), true);*//*
            finalResult.addProperty("tcs", purchaseInvoice.getTcs());
            finalResult.addProperty("narration", purchaseInvoice.getNarration() != null ? purchaseInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", purchaseInvoice.getPurchaseDiscountLedger() != null ? purchaseInvoice.getPurchaseDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", purchaseInvoice.getPurchaseDiscountAmount());
            finalResult.addProperty("discountInPer", purchaseInvoice.getPurchaseDiscountPer());
            JsonObject result = new JsonObject();
            *//* Purchase Invoice Data *//*
            result.addProperty("id", purchaseInvoice.getId());
            result.addProperty("invoice_dt", purchaseInvoice.getInvoiceDate().toString());
            result.addProperty("invoice_no", purchaseInvoice.getVendorInvoiceNo());
            result.addProperty("purchase_sr_no", purchaseInvoice.getSrno());
            result.addProperty("purchase_account_ledger_id", purchaseInvoice.getPurchaseAccountLedger().getId());
            result.addProperty("supplierId", purchaseInvoice.getSundryCreditors().getId());
            result.addProperty("transaction_dt", purchaseInvoice.getTransactionDate().toString());
            *//* End of Purchase Invoice Data *//*

     *//* Purchase Invoice Details *//*
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxPurInvoiceDetails mDetails : list) {
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
                        for (TranxPurchaseInvoiceProductSrNumber mProductSerials : serialNumbers) {
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
            *//* End of Purchase Invoice Details *//*

     *//* Purchase Additional Charges *//*
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
            *//* End of Purchase Additional Charges *//*
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);
            finalResult.add("additional_charges", jsonAdditionalList);

        } catch (DataIntegrityViolationException e) {
            //e.printStackTrace();
            purchaseReturnLogger.error("Exception in getInvoiceByIdWithProductsId:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            //e.printStackTrace();
            purchaseReturnLogger.error("Exception in getInvoiceByIdWithProductsId:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    } */
    public JsonObject getInvoiceByIdWithProductsId(HttpServletRequest request) {
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
                result.addProperty("transaction_dt", purchaseInvoice.getTransactionDate().toString());
                result.addProperty("additional_charges_total", purchaseInvoice.getAdditionalChargesTotal());
                result.addProperty("gstNo", purchaseInvoice.getGstNumber() != null ? purchaseInvoice.getGstNumber() : "");
                result.addProperty("isRoundOffCheck", purchaseInvoice.getIsRoundOff());
                result.addProperty("roundoff", purchaseInvoice.getRoundOff());
                result.addProperty("mode", purchaseInvoice.getPaymentMode() != "" ? purchaseInvoice.getPaymentMode() : "");
                result.addProperty("image", purchaseInvoice.getImagePath() != null ? serverUrl + purchaseInvoice.getImagePath() : "");
                /* End of Purchase Invoice Data */
            }

            /* Purchase Invoice Details */
            JsonArray row = new JsonArray();
            // List<TranxPurInvoiceDetailsUnits> unitsArray = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatus(purchaseInvoice.getId(), true);
            String str = request.getParameter("product_ids");
            List<TranxPurInvoiceDetailsUnits> unitsArray = tranxPurInvoiceUnitsRepository.
                    findInvoiceByIdWithProductsId(id, true, str);

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
                unitsJsonObjects.addProperty("packing", mUnits.getProduct() != null ?
                        (mUnits.getProduct().getPackingMaster() != null ?
                                mUnits.getProduct().getPackingMaster().getPackName() : "") : "");
                unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                Double returnqty = 0.0;
                unitsJsonObjects.addProperty("qty", mUnits.getQty());
                unitsJsonObjects.addProperty("returnable_qty", mUnits.getQty() - mUnits.getReturnQty());
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
                unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty() != null ? mUnits.getFreeQty().toString() : "");
                unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer());
                unitsJsonObjects.addProperty("row_dis_amt", mUnits.getTotalDiscountInAmt());
                unitsJsonObjects.addProperty("gross_amt", mUnits.getGrossAmt());
                unitsJsonObjects.addProperty("add_chg_amt", mUnits.getAdditionChargesAmt());
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1());
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt());
                unitsJsonObjects.addProperty("transaction_status", mUnits.getTransactionStatus() != null ? mUnits.getTransactionStatus().getId().toString() : "");
                unitsJsonObjects.addProperty("inventoryId", 0);
                InventoryDetailsPostings inventoryDetailsPostings =
                        inventoryDetailsPostingsRepository.findByPurInventoryRow(mUnits.getProduct().getId(),
                                purchaseInvoice.getFiscalYear().getId(), purchaseInvoice.getOutlet().getId(),
                                purchaseInvoice.getBranch() != null ? purchaseInvoice.getBranch().getId() : null, 1L,
                                id, mUnits.getLevelA() != null ? mUnits.getLevelA().getId() : null,
                                mUnits.getLevelB() != null ? mUnits.getLevelB().getId() : null,
                                mUnits.getLevelC() != null ? mUnits.getLevelC().getId() : null,
                                mUnits.getProductBatchNo() != null ? mUnits.getProductBatchNo().getId() : null,
                                mUnits.getUnits().getId(), "CR");
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
            purchaseReturnLogger.error("Error in getInvoiceByIdWithProductsId  :" + exceptionAsString);

        } catch (Exception e1) {
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purchaseReturnLogger.error("Error in getInvoiceByIdWithProductsId  :" + exceptionAsString);
        }
        return finalResult;
    }

    public JsonObject purReturnsByOutlet(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        List<TranxPurReturnInvoice> purInvoice = new ArrayList<>();
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
                purInvoice = tranxPurReturnsRepository.findPurchaseReturnListWithDate(users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, true);
            } else {
                purInvoice = tranxPurReturnsRepository.findPurchaseReturnListWithDateNoBr(users.getOutlet().getId(), startDatep, endDatep, true);
            }
        } else {
            if (users.getBranch() != null) {
                purInvoice = tranxPurReturnsRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                purInvoice = tranxPurReturnsRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
            }
        }

        for (TranxPurReturnInvoice invoices : purInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("pur_return_no", invoices.getPurRtnNo());
            response.addProperty("transaction_date", invoices.getTransactionDate().toString());
            response.addProperty("purchase_return_date", invoices.getPurReturnDate() != null ? invoices.getPurReturnDate().toString() : "");
            response.addProperty("purchase_return_serial_number", invoices.getPurReturnSrno());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_creditor_name", invoices.getSundryCreditors().getLedgerName());
            response.addProperty("sundry_creditor_id", invoices.getSundryCreditors().getId());
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTotalBaseAmount());
            response.addProperty("purchase_account_name", invoices.getPurchaseAccountLedger().getLedgerName());
            if (invoices.getTranxPurInvoice() != null)
                response.addProperty("invoice_no", invoices.getTranxPurInvoice().getVendorInvoiceNo());
            if (invoices.getTranxPurChallan() != null)
                response.addProperty("invoice_no", invoices.getTranxPurChallan().getVendorInvoiceNo());
            response.addProperty("narration", invoices.getNarration());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //Start of purchase return list with pagination
    public Object purReturnsByOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {
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
        List<TranxPurReturnInvoice> purchaseReturn = new ArrayList<>();
        List<TranxPurReturnInvoice> purchaseArrayList = new ArrayList<>();
        List<PurReturnDTO> purReturnDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_pur_return_invoice_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += " AND transaction_date BETWEEN '" + startDate + "' AND '" + endDate + "'";

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
                query = query + " ORDER BY transaction_date ASC";
            }
            String query1 = query;       //we get all lists in this list
            System.out.println("query== " + query);

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            Query q = entityManager.createNativeQuery(query, TranxPurReturnInvoice.class);
            System.out.println("q ==" + q + "  purchaseReturn " + purchaseReturn);
            purchaseReturn = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxPurReturnInvoice.class);

            purchaseArrayList = q1.getResultList();
            System.out.println("Limit total rows " + purchaseArrayList.size());
            Integer total_pages = (purchaseArrayList.size() / pageSize);
            if ((purchaseArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (TranxPurReturnInvoice invoiceListView : purchaseReturn) {
                purReturnDTOList.add(convertToDTDTO(invoiceListView));
            }

            List<TranxPurReturnInvoice> purReturnList = new ArrayList<>();
            purReturnList = q1.getResultList();
            System.out.println("total rows " + purReturnList.size());
            GenericDatatable<PurReturnDTO> data = new GenericDatatable<>(purReturnDTOList, purchaseArrayList.size(), pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(purReturnDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }
    //End of purchase return list with pagination

    //Start of DTO for purchase return

    private PurReturnDTO convertToDTDTO(TranxPurReturnInvoice purReturnInvoice) {
        PurReturnDTO purReturnDTO = new PurReturnDTO();
        purReturnDTO.setId(purReturnInvoice.getId());
        purReturnDTO.setPur_return_no(purReturnInvoice.getPurRtnNo());
        purReturnDTO.setTransaction_date(purReturnInvoice.getTransactionDate().toString());
        purReturnDTO.setPurchase_return_date(DateConvertUtil.convertDateToLocalDate(purReturnInvoice.getPurReturnDate()).toString());
        purReturnDTO.setPurchase_return_serial_number(purReturnInvoice.getPurReturnSrno());
        purReturnDTO.setTotal_amount(purReturnInvoice.getTotalAmount());
        purReturnDTO.setSundry_creditor_name(purReturnInvoice.getSundryCreditors().getLedgerName());
        purReturnDTO.setSundry_creditor_id(purReturnInvoice.getSundryCreditors().getId());
        purReturnDTO.setTax_amt(purReturnInvoice.getTotalTax());
        purReturnDTO.setTaxable_amt(purReturnInvoice.getTotalBaseAmount());
        purReturnDTO.setPurchase_account_name(purReturnInvoice.getPurchaseAccountLedger().getLedgerName());
        if (purReturnInvoice.getTranxPurInvoice() != null)
            purReturnDTO.setInvoice_no(purReturnInvoice.getTranxPurInvoice().getVendorInvoiceNo());
        if (purReturnInvoice.getTranxPurChallan() != null)
            purReturnDTO.setInvoice_no(purReturnInvoice.getTranxPurChallan().getVendorInvoiceNo());
        purReturnDTO.setNarration(purReturnInvoice.getNarration());
        purReturnDTO.setTranxCode(purReturnInvoice.getTranxCode());


        return purReturnDTO;

    }

    //End of DTO for purchase return
    public Object createPurchaseReturns(HttpServletRequest request) {
        TranxPurReturnInvoice mPurchaseTranx = null;
        ResponseMessage responseMessage = new ResponseMessage();
        mPurchaseTranx = saveIntoPurchaseReturnsInvoice(request);
        if (mPurchaseTranx != null) {
            //
            insertIntoLedgerTranxDetails(mPurchaseTranx);// Accounting Postings
            /*** creating new reference while adjusting
             return amount into next purchase invoice bill ***/
            TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
            insertIntoNewReference(mPurchaseTranx, request, "create", tranxType.getTransactionCode());
            responseMessage.setMessage("Purchase return invoice created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Error in purchase invoice creation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    public JsonObject getProductEditByIdByFPU(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxPurReturnInvoice invoiceTranx = tranxPurReturnsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        //   productIds = tranxPurReturnDetailsRepository.findByTranxPurId(invoiceTranx.getId(), true);
        productIds = tranxPurReturnDetailsUnitRepository.findByTranxPurId(invoiceTranx.getId(), true);
        //productArray = productData.getProductByBFPUCommon_Return(invoiceTranx.getPurReturnDate(), productIds);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getPurReturnDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getPurchaseReturnById(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurReturnInvoiceDetails> list = new ArrayList<>();
        JsonArray units = new JsonArray();
        List<TranxPurReturnInvoiceProductSrNo> serialNumbers = new ArrayList<>();
        List<TranxPurReturnInvoiceAddCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxPurReturnInvoice purchaseInvoice = tranxPurReturnsRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            list = tranxPurReturnDetailsRepository.findByPurReturnInvoiceIdAndStatus(id, true);
           /* serialNumbers = tranxPurReturnProdSrNoRepository.findByPurReturnInvoiceId(purchaseInvoice.getId());
            additionalCharges = tranxPurReturnAddChargesRepository.findByPurReturnInvoiceIdAndStatus(purchaseInvoice.getId(), true);*/
            finalResult.addProperty("tcs", purchaseInvoice.getTcs());
            finalResult.addProperty("narration", purchaseInvoice.getNarration() != null ? purchaseInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", purchaseInvoice.getPurchaseDiscountLedger() != null ? purchaseInvoice.getPurchaseDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", purchaseInvoice.getPurchaseDiscountAmount());
            finalResult.addProperty("discountInPer", purchaseInvoice.getPurchaseDiscountPer());
            finalResult.addProperty("totalPurchaseDiscountAmt", purchaseInvoice.getTotalPurchaseDiscountAmt());
            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", purchaseInvoice.getId());
            result.addProperty("invoice_dt", purchaseInvoice.getPurReturnDate().toString());
            result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getPurReturnDate()).toString());
            result.addProperty("invoice_no", purchaseInvoice.getPurRtnNo().toString());
            result.addProperty("purchase_sr_no", purchaseInvoice.getPurReturnSrno());
            result.addProperty("purchase_account_ledger_id", purchaseInvoice.getPurchaseAccountLedger().getId());
            result.addProperty("supplierId", purchaseInvoice.getSundryCreditors().getId());
            result.addProperty("transaction_dt", purchaseInvoice.getTransactionDate().toString());
            result.addProperty("additional_charges_total", purchaseInvoice.getAdditionalChargesTotal());
            /* End of Purchase Invoice Data */
            /* Purchase Invoice Details */
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxPurReturnInvoiceDetails mDetails : list) {
                    JsonObject prDetails = new JsonObject();
                    prDetails.addProperty("details_id", mDetails.getId());
                    prDetails.addProperty("product_id", mDetails.getProduct().getId());
                 /*   JsonArray serialNo = new JsonArray();
                    if (serialNumbers.size() > 0) {
                        for (TranxPurReturnInvoiceProductSrNo mProductSerials : serialNumbers) {
                            JsonObject jsonSerailNo = new JsonObject();
                            jsonSerailNo.addProperty("details_id", mProductSerials.getId());
                            jsonSerailNo.addProperty("product_id", mDetails.getProduct().getId());
                            jsonSerailNo.addProperty("serial_no", mProductSerials.getSerialNo());
                            serialNo.add(jsonSerailNo);
                        }
                        prDetails.add("serialNo", serialNo);
                    }*/
                    /* getting Units of Purcase Invoice*/
                  /*  List<TranxPurReturnDetailsUnits> unitDetails = tranxPurReturnDetailsUnitRepository.
                            findByTranxPurReturnInvoiceDetailsIdAndStatus(mDetails.getId(), true);*/
                    JsonArray productDetails = new JsonArray();
/*                    unitDetails.forEach(mUnit -> {
                        JsonObject mObject = new JsonObject();
                        JsonObject mUnitsObj = new JsonObject();
                     *//*   if (mUnit.getPackingMaster() != null) {
                            JsonObject package_obj = new JsonObject();
                            package_obj.addProperty("id", mUnit.getPackingMaster().getId());
                            package_obj.addProperty("pack_name", mUnit.getPackingMaster().getPackName());
                            package_obj.addProperty("label", mUnit.getPackingMaster().getPackName());
                            package_obj.addProperty("value", mUnit.getPackingMaster().getId());
                            mObject.add("package_id", package_obj);
                        } else {
                            mObject.addProperty("package_id", "");
                        }*//*
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
                        *//*mUnitsObj.addProperty("units_id", mUnit.getUnits().getId());
                        mUnitsObj.addProperty("value", mUnit.getUnits().getId());
                        mUnitsObj.addProperty("label", mUnit.getUnits().getUnitName());
                        mUnitsObj.addProperty("unit_name", mUnit.getUnits().getUnitName());*//*
                        // mObject.add("unitId", mUnitsObj);
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
                    });*/
                    prDetails.add("productDetails", productDetails);
                    row.add(prDetails);
                }
            }
            // System.out.println("Row  " + row);
            /* End of Purchase Invoice Details */

            /* Purchase Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            if (additionalCharges.size() > 0) {
                for (TranxPurReturnInvoiceAddCharges mAdditionalCharges : additionalCharges) {
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
            e.printStackTrace();
            purchaseReturnLogger.error("Error in getPurchaseReturnById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            purchaseReturnLogger.error("Error in getPurchaseReturnById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject purchaseReturnEdit(HttpServletRequest request) {

        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TransactionTypeMaster tranxType = null;
        TranxPurReturnInvoice mPurchaseTranx = saveIntoPurchaseReturnEdit(request);
        JsonObject object = new JsonObject();
        if (mPurchaseTranx != null) {
            if (request.getParameter("paymentMode").equalsIgnoreCase("credit"))
                insertIntoNewReference(mPurchaseTranx, request, "edit", "");
            else {
                /***** dont allow the invoice next time in bill selection module next time if invoice is adjusted *****/
                TranxPurInvoice invoice = tranxPurInvoiceRepository.findByIdAndStatus(Long.parseLong(request.getParameter("pur_invoice_id")), true);
                invoice.setTransactionStatus(2L);
                tranxPurInvoiceRepository.save(invoice);
                /* Adjust retun amount into selected purchase invoices */
                String jsonStr = request.getParameter("billLst");
                JsonElement purDetailsJson = new JsonParser().parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                for (JsonElement mElement : array) {
                    JsonObject mObject = mElement.getAsJsonObject();
                    Long invoiceId = mObject.get("invoice_id").getAsLong();
                    TranxPurInvoice mInvoice = tranxPurInvoiceRepository.findByIdAndStatus(invoiceId, true);
                    Double paidAmt = mObject.get("paid_amt").getAsDouble();
                    if (mInvoice != null) {
                        try {
                            //mInvoice.setBalance(mPurchaseTranx.getTranxPurInvoice().getBalance() - paidAmt);
                            mInvoice.setBalance(mObject.get("remaianing_amt").getAsDouble());
                            tranxPurInvoiceRepository.save(mInvoice);
                        } catch (Exception e) {
                            e.printStackTrace();
                            purchaseReturnLogger.error("Exception in Purchase Return:" + e.getMessage());
                        }
                    }
                    /***** Save Into Tranx Purchase Return Adjument Bills Table ******/
                    TranxPurReturnAdjustmentBills mBills = null;
                    mBills = tranxPurReturnAdjBillsRepository.findByIdAndStatus(mObject.get("id").getAsLong(), true);
                    if (mObject.get("id").getAsLong() != 0) {
                        if (mBills != null) {
                            if (mObject.get("source").getAsString().equalsIgnoreCase("pur_invoice"))
                                mBills.setTranxPurInvoice(mInvoice);
                            mBills.setSource(mObject.get("source").getAsString());
                            mBills.setPaidAmt(paidAmt);
                            mBills.setRemainingAmt(mObject.get("remaining_amt").getAsDouble());
                            mBills.setTotalAmt(mObject.get("total_amt").getAsDouble());
                            mBills.setTranxPurReturnId(mPurchaseTranx.getId());
                        }
                    } else {
                        mBills = new TranxPurReturnAdjustmentBills();
                        if (mObject.get("source").getAsString().equalsIgnoreCase("pur_invoice"))
                            mBills.setTranxPurInvoice(mInvoice);
                        mBills.setSource(mObject.get("source").getAsString());
                        mBills.setPaidAmt(paidAmt);
                        mBills.setRemainingAmt(mObject.get("remaining_amt").getAsDouble());
                        mBills.setTotalAmt(mObject.get("total_amt").getAsDouble());
                        mBills.setTranxPurReturnId(mPurchaseTranx.getId());
                        mBills.setStatus(true);
                        mBills.setCreatedBy(mPurchaseTranx.getCreatedBy());
                    }
                    tranxPurReturnAdjBillsRepository.save(mBills);
                }
            }

            tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");

            /* save into Additional Charges  */
            String acRowsDeleted = "";
            String strJson = request.getParameter("additionalCharges");
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(strJson);
            JsonArray additionalCharges = tradeElement.getAsJsonArray();
            saveIntoPurchaseAdditionalChargesEdit(additionalCharges, mPurchaseTranx, tranxType, users.getOutlet().getId());
            /*** delete additional charges if removed from frontend ****/
            if (paramMap.containsKey("acDelDetailsIds")) acRowsDeleted = request.getParameter("acDelDetailsIds");
            JsonElement purAChargesJson;
            if (!acRowsDeleted.equalsIgnoreCase("")) {
                purAChargesJson = new JsonParser().parse(acRowsDeleted);
                JsonArray deletedArrays = purAChargesJson.getAsJsonArray();
                if (deletedArrays.size() > 0) {
                    delAddCharges(deletedArrays);
                }
            }

            /* Remove all ledgers from DB if we found new input ledger id's while updating */
            for (Long mDblist : ledgerList) {
                if (!ledgerInputList.contains(mDblist)) {
                    purchaseReturnLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mDblist, tranxType.getId(), mPurchaseTranx.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), mPurchaseTranx.getPurReturnDate(),
                                tranxType, mPurchaseTranx.getFiscalYear(), detail);
                    }
                    purchaseReturnLogger.info("removing unused previous ledger update done");
                }
            }

            object.addProperty("message", "purchase return updated successfully");
            object.addProperty("responseStatus", HttpStatus.OK.value());
        } else {
            object.addProperty("message", "Erro in purchase return update");
            object.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return object;
    }

    private TranxPurReturnInvoice saveIntoPurchaseReturnEdit(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurReturnInvoice mPurchaseTranx = null;
        TransactionTypeMaster tranxType = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
        TranxPurReturnInvoice invoiceTranx = tranxPurReturnsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        dbList = ledgerTransactionPostingsRepository.findByTransactionId(invoiceTranx.getId(), tranxType.getId());
        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(invoiceTranx.getId(),
                tranxType.getId(), true);
        if (paramMap.containsKey("branch_id")) {
            branch = branchRepository.findByIdAndStatus(Long.parseLong(request.getParameter("branch_id")), true);
        }
        invoiceTranx.setBranch(branch);
        invoiceTranx.setOutlet(outlet);

        LocalDate mDate = LocalDate.now();
        Date trDt = DateConvertUtil.convertStringToDate(mDate.toString());
        if (mDate.isEqual(DateConvertUtil.convertDateToLocalDate(invoiceTranx.getPurReturnDate()))) {
            trDt = invoiceTranx.getPurReturnDate();
        }
        invoiceTranx.setTransactionDate(trDt);
        LocalDate returnDate = LocalDate.parse(request.getParameter("purchase_return_date"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("purchase_return_date"));
        if (returnDate.isEqual(DateConvertUtil.convertDateToLocalDate(invoiceTranx.getPurReturnDate()))) {
            strDt = invoiceTranx.getPurReturnDate();
        }
        invoiceTranx.setPurReturnDate(strDt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(returnDate);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setPurRtnNo(request.getParameter("pur_return_invoice_no"));
        LedgerMaster purchaseAccount = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("purchase_account_id")), users.getOutlet().getId(), true);
        invoiceTranx.setPurchaseAccountLedger(purchaseAccount);
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
                    mLedger.setAmount(Double.parseDouble(request.getParameter("total_base_amt")));
                    mLedger.setTransactionDate(invoiceTranx.getPurReturnDate());
                    mLedger.setOperations("updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                /* insert ledger tranx if ledger is changed */
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("total_base_amt")), purchaseAccount, tranxType, purchaseAccount.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "CR", true, tranxType.getTransactionName(), "Update");
            }

            Double amount = Double.parseDouble(request.getParameter("total_base_amt"));
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getPurchaseAccountLedger().getId(),
                    tranxType, "CR", invoiceTranx.getId(), purchaseAccount, trDt, fiscalYear, invoiceTranx.getOutlet(),
                    invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
        }
        /* end of calling store procedure for Purchase Account updating opening and closing */

        if (!request.getParameter("purchase_disc_ledger").equalsIgnoreCase("")) {
            LedgerMaster discountLedger = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("purchase_disc_ledger")), users.getOutlet().getId(), true);
            if (discountLedger != null) {
                invoiceTranx.setPurchaseDiscountLedger(discountLedger);
            }
            /* calling store procedure for updating opening and closing of Purchase Discount  */
            Boolean isContains = dbList.contains(discountLedger.getId());
            Boolean isLedgerContains = ledgerList.contains(discountLedger.getId());
            mInputList.add(discountLedger.getId());
            if (isContains) {
                //transactionDetailsRepository.ledgerPostingEdit(discountLedger.getId(), invoiceTranx.getId(), tranxType.getId(), "CR", Double.parseDouble(request.getParameter("total_purchase_discount_amt")));
                /**** New Postings Logic *****/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(discountLedger.getId(), tranxType.getId(), invoiceTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(Double.parseDouble(request.getParameter("total_purchase_discount_amt")));
                    mLedger.setTransactionDate(invoiceTranx.getPurReturnDate());
                    mLedger.setOperations("Updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("total_purchase_discount_amt")), discountLedger, tranxType, discountLedger.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "DR", true, tranxType.getTransactionName(), "Insert");
            }

            Double amount = Double.parseDouble(request.getParameter("total_purchase_discount_amt"));
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getPurchaseDiscountLedger().getId(),
                    tranxType, "DR", invoiceTranx.getId(), discountLedger, trDt, fiscalYear, invoiceTranx.getOutlet(),
                    invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
            /* end of calling store procedure for updating opening and closing of Purchase Discount */
        }
       /* this parameter segregates whether pur return is from purchase invoice
        or purchase challan*/
        if (paramMap.containsKey("source")) {
            if (request.getParameter("source").equalsIgnoreCase("pur_invoice")) {
                if (paramMap.containsKey("pur_invoice_id")) {
                    TranxPurInvoice tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(Long.parseLong(request.getParameter("pur_invoice_id")), true);
                    invoiceTranx.setTranxPurInvoice(tranxPurInvoice);
                }
            } else {
                if (paramMap.containsKey("pur_challan_id")) {
                    TranxPurChallan tranxPurChallan = tranxPurChallanRepository.findByIdAndStatus(Long.parseLong(request.getParameter("pur_challan_id")), true);
                    invoiceTranx.setTranxPurChallan(tranxPurChallan);
                }
            }
        }
        LedgerMaster sundryCreditors = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), users.getOutlet().getId(), true);
        invoiceTranx.setSundryCreditors(sundryCreditors);
        /* calling store procedure for updating opening and closing balance of Sundry Creditors  */
        if (sundryCreditors.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {

            Boolean isContains = dbList.contains(sundryCreditors.getId());
            Boolean isLedgerContains = ledgerList.contains(sundryCreditors.getId());
            mInputList.add(sundryCreditors.getId());
            ledgerInputList.add(sundryCreditors.getId());
            if (isContains) {
                /**** New Postings Logic *****/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(sundryCreditors.getId(), tranxType.getId(), invoiceTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(Double.parseDouble(request.getParameter("totalamt")));
                    mLedger.setTransactionDate(invoiceTranx.getPurReturnDate());
                    mLedger.setOperations("Updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("totalamt")), sundryCreditors, tranxType, sundryCreditors.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "DR", true, tranxType.getTransactionName(), "Insert");
            }

            Double amount = Double.parseDouble(request.getParameter("totalamt"));
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSundryCreditors().getId(),
                    tranxType, "DR", invoiceTranx.getId(), sundryCreditors, trDt, fiscalYear, invoiceTranx.getOutlet(),
                    invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
        }  /* end of calling store procedure for updating opening and closing of Sundry Creditors  */

        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setPurchaseRoundOff(roundoff);
        Boolean isContains = dbList.contains(roundoff.getId());
        Boolean isLedgerContains = ledgerList.contains(roundoff.getId());
        ledgerInputList.add(roundoff.getId());
        Double rf = Double.parseDouble(request.getParameter("roundoff"));
        if (isContains) {
            /**** New Postings Logic *****/
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(roundoff.getId(), tranxType.getId(), invoiceTranx.getId());
            if (mLedger != null) {
                mLedger.setAmount(rf);
                mLedger.setTransactionDate(invoiceTranx.getPurReturnDate());
                mLedger.setOperations("Updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            }
        } else {
            if (rf >= 0) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(rf, roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "CR", true, tranxType.getTransactionName(), "Insert");
            } else {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(rf, roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "DR", true, tranxType.getTransactionName(), "Insert");
            }
        }

        /**** NEW METHOD FOR LEDGER POSTING ****/
        String tranxAction = "DR";
        if (rf >= 0)
            tranxAction = "CR";
        Double amount = Math.abs(rf);
        /**** NEW METHOD FOR LEDGER POSTING ****/
        postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getPurchaseRoundOff().getId(),
                tranxType, tranxAction, invoiceTranx.getId(), roundoff, trDt, fiscalYear, invoiceTranx.getOutlet(),
                invoiceTranx.getBranch(), invoiceTranx.getTranxCode());

        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
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
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setPurchaseDiscountPer(Double.parseDouble(request.getParameter("purchase_discount")));
        invoiceTranx.setPurchaseDiscountAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
        invoiceTranx.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_purchase_discount_amt")));
        invoiceTranx.setPurReturnSrno(Long.parseLong(request.getParameter("purchase_return_sr_no")));
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        invoiceTranx.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setOperations("insert");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));

        invoiceTranx.setPurReturnDate(DateConvertUtil.convertStringToDate(returnDate.toString()));
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
                invoiceTranx.setAdditionLedgerAmt1(Double.valueOf(request.getParameter("addChgLedgerAmt1")));
                isContains = dbList.contains(additionalChgLedger1.getId());
                mInputList.add(additionalChgLedger1.getId());
                if (isContains) {
                    *//**** New Postings Logic *****//*
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(additionalChgLedger1.getId(), tranxType.getId(), invoiceTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(Double.parseDouble(request.getParameter("addChgLedgerAmt1")));
                        mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                        *//**** New Postings Logic *****//*
                        ledgerCommonPostings.callToPostings(invoiceTranx.getAdditionLedgerAmt1(), invoiceTranx.getAdditionLedger1(), tranxType, invoiceTranx.getAdditionLedger1().getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), invoiceTranx.getAdditionLedgerAmt1() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
        }
        if (paramMap.containsKey("additionalChgLedger2")) {
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
                        mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                        *//**** New Postings Logic *****//*
                        ledgerCommonPostings.callToPostings(invoiceTranx.getAdditionLedgerAmt2(), invoiceTranx.getAdditionLedger2(), tranxType, invoiceTranx.getAdditionLedger2().getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), invoiceTranx.getAdditionLedgerAmt2() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), "Insert");
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
                        mLedger.setTransactionDate(invoiceTranx.getPurReturnDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(invoiceTranx.getAdditionLedgerAmt3(), invoiceTranx.getAdditionLedger2(), tranxType, invoiceTranx.getAdditionLedger3().getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), invoiceTranx.getAdditionLedgerAmt3() > 0 ? "CR" : "DR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
        }
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
            mPurchaseTranx = tranxPurReturnsRepository.save(invoiceTranx);
            if (mPurchaseTranx != null) {
                /* Save into Duties and Taxes */
                String taxStr = request.getParameter("taxCalculation");
                //  JsonObject duties_taxes = new JsonObject(taxStr);
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoPurchaseDutiesTaxesEdit(duties_taxes, mPurchaseTranx, taxFlag, outlet.getId());
                JsonParser parser = new JsonParser();
                /* save into Purchase Return Details */
                String jsonStr = request.getParameter("row");
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) {
                    rowsDeleted = request.getParameter("rowDelDetailsIds");
                }
                saveIntoPurchaseInvoiceDetailsEdit(array, mPurchaseTranx, branch, outlet, users.getId(), tranxType, request, rowsDeleted);
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseReturnsInvoice:" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseReturnsInvoice:" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mPurchaseTranx;
    }

    private void saveIntoPurchaseAdditionalChargesEdit(JsonArray additionalCharges, TranxPurReturnInvoice mPurchaseTranx, TransactionTypeMaster tranxType, Long outletId) {
        List<TranxPurReturnInvoiceAddCharges> chargesList = new ArrayList<>();
        for (JsonElement mAddCharges : additionalCharges) {
            JsonObject object = mAddCharges.getAsJsonObject();
            Double amount = object.get("amt").getAsDouble();
            Long ledgerId = object.get("ledgerId").getAsLong();
//            Long detailsId = object.get("additional_charges_details_id").getAsLong();
            LedgerMaster addcharges = null;
            TranxPurReturnInvoiceAddCharges charges = null;

            charges = tranxPurReturnAddChargesRepository.findByAdditionalChargesIdAndPurReturnInvoiceIdAndStatus(ledgerId, mPurchaseTranx.getId(), true);
            if (charges == null) {
                charges = new TranxPurReturnInvoiceAddCharges();
            }
            addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
            charges.setAmount(amount);
            charges.setAdditionalCharges(addcharges);
            charges.setPercent(object.get("percent").getAsDouble());
            charges.setPurReturnInvoice(mPurchaseTranx);
            charges.setStatus(true);

            chargesList.add(charges);
            Boolean isContains = dbList.contains(addcharges.getId());
            Boolean isLedgerContains = dbList.contains(addcharges.getId());
            mInputList.add(addcharges.getId());
            ledgerInputList.add(addcharges.getId());
            if (isContains) {
                //  transactionDetailsRepository.ledgerPostingEdit(addcharges.getId(), mPurchaseTranx.getId(), tranxType.getId(), "DR", amount * -1);
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(addcharges.getId(), tranxType.getId(), mPurchaseTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(amount);
//                    mLedger.setTransactionDate(mPurchaseTranx.getTransactionDate());
                    mLedger.setTransactionDate(mPurchaseTranx.getPurReturnDate());
                    mLedger.setOperations("Updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                /* insert */
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(amount, addcharges, tranxType, addcharges.getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), mPurchaseTranx.getPurRtnNo(), "CR", true, tranxType.getTransactionName(), "Insert");
            }

            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, addcharges.getId(), tranxType,
                    "CR", mPurchaseTranx.getId(), addcharges, mPurchaseTranx.getPurReturnDate(),
                    mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getBranch(),
                    mPurchaseTranx.getTranxCode());
        }

        try {
            tranxPurReturnAddChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            purchaseReturnLogger.error("Error in saveIntoPurchaseAdditionalChargesEdit" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            purchaseReturnLogger.error("Error in saveIntoPurchaseAdditionalChargesEdit" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveIntoPurchaseInvoiceDetailsEdit(JsonArray array, TranxPurReturnInvoice mPurchaseTranx, Branch branch, Outlet outlet, Long userId, TransactionTypeMaster tranxType, HttpServletRequest request, String rowsDeleted) {
        List<TranxPurReturnInvoiceProductSrNo> newSerialNumbers = new ArrayList<>();
        /* Purchase Product Details Start here */
        TransactionStatus status = transactionStatusRepository.findByIdAndStatus(2L, true);
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            Long detailsId = object.get("details_id").getAsLong();
            Double tranxQty = 0.0;
            TranxPurReturnDetailsUnits invoiceUnits = new TranxPurReturnDetailsUnits();
            if (detailsId != 0) {
                invoiceUnits = tranxPurReturnDetailsUnitRepository.findByIdAndStatus(detailsId, true);
                tranxQty = invoiceUnits.getQty();
            } else {
                invoiceUnits.setStatus(true);
                tranxQty = object.get("qty").getAsDouble();
            }
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            /* inserting into TranxSalesInvoiceDetailsUnits */
            //    JsonArray productDetails = object.get("brandDetails").getAsJsonArray();
            String batchNo = null;
            String serialNo = null;
            ProductBatchNo productBatchNo = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
            double free_qty = 0.0;

            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
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
            invoiceUnits.setTranxPurReturnInvoice(mPurchaseTranx);
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
            TranxPurInvoiceDetailsUnits mUnits = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatusAndProductId(mPurchaseTranx.getTranxPurInvoice().getId(), true, mProduct.getId());
            Double total_qty = mUnits.getReturnQty() + object.get("qty").getAsDouble();
            if (mUnits.getQty().doubleValue() == total_qty.doubleValue()) {
                mUnits.setTransactionStatus(status);
            }
            mUnits.setReturnQty(total_qty);
            tranxPurInvoiceUnitsRepository.save(mUnits);

            boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    Double qnty = object.get("qty").getAsDouble();
                    flag = true;

                    if (object.get("b_details_id").getAsLong() == 0) {
                        ProductBatchNo mproductBatchNo = new ProductBatchNo();
                        if (object.has("b_no")) mproductBatchNo.setBatchNo(object.get("b_no").getAsString());
                        if (object.has("b_rate") && !object.get("b_rate").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").getAsString().equals(""))
                            mproductBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                            mproductBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));

                        mproductBatchNo.setQnty(qnty.intValue());
                        mproductBatchNo.setFreeQty(free_qty);
                        if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equals(""))
                            mproductBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                        if (object.has("costing") && !object.get("costing").isJsonNull())
                            mproductBatchNo.setCosting(object.get("costing").getAsDouble());
                        if (object.has("costing_with_tax") && !object.get("costing_with_tax").isJsonNull())
                            mproductBatchNo.setCostingWithTax(object.get("costing_with_tax").getAsDouble());
                        if (object.has("rate_a") && !object.get("rate_a").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        if (object.has("rate_b") && !object.get("rate_b").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                        if (object.has("rate_c") && !object.get("rate_c").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                        if (object.has("margin_per") && !object.get("margin_per").getAsString().equals(""))
                            mproductBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") && !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                            mproductBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        mproductBatchNo.setStatus(true);
                        mproductBatchNo.setProduct(mProduct);
                        mproductBatchNo.setOutlet(outlet);
                        mproductBatchNo.setBranch(branch);
                        if (levelA != null) mproductBatchNo.setLevelA(levelA);
                        if (levelB != null) mproductBatchNo.setLevelB(levelB);
                        if (levelC != null) mproductBatchNo.setLevelC(levelC);
                        mproductBatchNo.setUnits(units);
                        mproductBatchNo.setSupplierId(mPurchaseTranx.getSundryCreditors().getId());
                        try {
                            productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Exception " + e.getMessage());
                        }
                    } else {
                        productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                        productBatchNo.setQnty(qnty.intValue());
                        productBatchNo.setFreeQty(free_qty);
                        if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                        if (object.has("costing") && !object.get("costing").isJsonNull())
                            productBatchNo.setCosting(object.get("costing").getAsDouble());
                        if (object.has("costing_with_tax") && !object.get("costing_with_tax").isJsonNull())
                            productBatchNo.setCostingWithTax(object.get("costing_with_tax").getAsDouble());
                        if (object.has("b_no")) productBatchNo.setBatchNo(object.get("b_no").getAsString());
                        if (object.has("b_rate") && !object.get("b_rate").getAsString().isEmpty())
                            productBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        if (object.has("b_sale_rate") && !object.get("b_sale_rate").getAsString().isEmpty())
                            productBatchNo.setSalesRate(object.get("b_sale_rate").getAsDouble());
                        if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").getAsString().isEmpty())
                            productBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().contains("invalid"))
                            productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") && !object.get("manufacturing_date").getAsString().toLowerCase().contains("invalid"))
                            productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        if (object.has("rate_a") && !object.get("rate_a").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        if (object.has("rate_b") && !object.get("rate_b").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                        if (object.has("rate_c") && !object.get("rate_c").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                        if (object.has("margin_per") && !object.get("margin_per").getAsString().isEmpty())
                            productBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
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
                invoiceUnits.setProductBatchNo(productBatchNo);
                TranxPurReturnDetailsUnits mPurReturn = tranxPurReturnDetailsUnitRepository.save(invoiceUnits);
                if (flag == false) {

                }

            } catch (Exception e) {
                purchaseReturnLogger.error("Exception in saveIntoPurchaseInvoiceDetailsEdit:" + e.getMessage());
            }
            try {
               /* if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                    flag = true;
                }*/
                /**** Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    if (detailsId != 0) {
                        inventoryCommonPostings.callToEditInventoryPostings(mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear());

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
                            if (mPurchaseTranx.getPurReturnDate().compareTo(productRow.getTranxDate()) == 0 &&
                                    tranxQty.doubleValue() != object.get("qty").getAsDouble()) { //DATE SAME AND QTY DIFFERENT
                                Double closingStk = closingUtility.CAL_CR_STOCK(productRow.getOpeningStock(), object.get("qty").getAsDouble(), free_qty);
                                productRow.setQty(object.get("qty").getAsDouble() + free_qty);
                                productRow.setClosingStock(closingStk);
                                InventorySummaryTransactionDetails mInventory =
                                        stkTranxDetailsRepository.save(productRow);
                                closingUtility.updatePosting(mInventory, mProduct.getId(), mPurchaseTranx.getPurReturnDate());
                            } else if (mPurchaseTranx.getPurReturnDate().compareTo(productRow.getTranxDate()) != 0) { // DATE IS DIFFERENT
                                Date oldDate = productRow.getTranxDate();
                                Date newDate = mPurchaseTranx.getPurReturnDate();
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
                                        mPurchaseTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());

                                closingUtility.stockPostingBatchWise(outlet, branch, mPurchaseTranx.getFiscalYear().getId(), batchId,
                                        mProduct, tranxType.getId(), newDate, invoiceUnits.getQty(), free_qty,
                                        mPurchaseTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                        mPurchaseTranx.getTranxCode(), userId, "OUT", mProduct.getPackingMaster().getId());
                            }
                        }
                    } else {
                        inventoryCommonPostings.callToInventoryPostings("DR", mPurchaseTranx.getPurReturnDate(), mPurchaseTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear(), serialNo);
                    }
                    /***** End of new architecture of Inventory Postings *****/
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            }
            /*if (newSerialNumbers != null && newSerialNumbers.size() > 0) {
                inventoryDTO.saveIntoSerialTranxSummaryDetailsSalesReturns(newSerialNumbers, tranxType.getTransactionName());
            }*/
        }
        /* if product is deleted from details table from front end, when user edit the purchase */

        //     List<PurchaseInvoiceDetails> list = invoiceDetailsRepository.saveAll(row);
        Long purchaseInvoiceId = null;
        HashSet<Long> purchaseDetailsId = new HashSet<>();
        if (!rowsDeleted.isEmpty()) {
            JsonParser parser = new JsonParser();
            JsonElement purDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = purDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxPurReturnDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    if (deletedArrays.size() > 0) {
                        if (deletedRowsId.has("del_id")) {
                            mDeletedInvoices = tranxPurReturnDetailsUnitRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                            if (mDeletedInvoices != null) {
                                mDeletedInvoices.setStatus(false);
                                try {
                                    tranxPurReturnDetailsUnitRepository.save(mDeletedInvoices);
                                    inventoryCommonPostings.callToInventoryPostings("DR", mDeletedInvoices.getTranxPurReturnInvoice().getPurReturnDate(), mDeletedInvoices.getTranxPurReturnInvoice().getId(), mDeletedInvoices.getQty() + mDeletedInvoices.getFreeQty(), branch, outlet, mDeletedInvoices.getProduct(), tranxType, mDeletedInvoices.getLevelA(), mDeletedInvoices.getLevelB(), mDeletedInvoices.getLevelC(), mDeletedInvoices.getUnits(), mDeletedInvoices.getProductBatchNo(), mDeletedInvoices.getProductBatchNo().getBatchNo(), mDeletedInvoices.getTranxPurReturnInvoice().getFiscalYear(), null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    purchaseReturnLogger.error("Error in saveIntoPurChallanDetailsEdit :->" + e.getMessage());
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    private void saveIntoPurchaseDutiesTaxesEdit(JsonObject duties_taxes, TranxPurReturnInvoice invoiceTranx, Boolean taxFlag, Long outletId) {
        List<TranxPurReturnInvoiceDutiesTaxes> purchaseDutiesTaxes = new ArrayList<>();
        List<Long> db_dutiesLedgerIds = tranxPurReturnDutiesTaxesRepository.findByDutiesAndTaxesId(invoiceTranx.getId());
        // List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId);
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null);
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            //Updation into Purchase challan Duties and Taxes
            if (db_dutiesLedgerIds.size() > 0) {
                //insert old records in history
                purchaseDutiesTaxes = tranxPurReturnDutiesTaxesRepository.findByPurReturnInvoiceAndStatus(invoiceTranx, true);
                //  insertIntoDutiesAndTaxesHistory(purchaseDutiesTaxes);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxPurReturnInvoiceDutiesTaxes taxes = new TranxPurReturnInvoiceDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        int inputGst = (int) cgstObject.get("gst").getAsDouble();
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
                            Boolean isLedgerContains = ledgerList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                            ledgerInputList.add(dutiesTaxes.getId());
                            if (isContains) {
                                /**** New Postings Logic *****/
                                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(dutiesTaxes.getId(), tranxType.getId(), invoiceTranx.getId());
                                if (mLedger != null) {
                                    mLedger.setAmount(amt);
//                                    mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                                    mLedger.setTransactionDate(invoiceTranx.getPurReturnDate());
                                    mLedger.setOperations("Updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "CR", true, tranxType.getTransactionName(), "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType,
                                    "CR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getPurReturnDate(),
                                    invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());
                        }
                        taxes.setAmount(amt);
                        taxes.setStatus(true);
                        taxes.setPurReturnInvoice(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxPurReturnInvoiceDutiesTaxes taxes = new TranxPurReturnInvoiceDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String ledgerName = "INPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        // dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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
//                                    mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                                    mLedger.setTransactionDate(invoiceTranx.getPurReturnDate());
                                    mLedger.setOperations("Updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "CR", true, tranxType.getTransactionName(), "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType,
                                    "CR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getPurReturnDate(),
                                    invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());
                        }
                        taxes.setAmount(amt);
                        taxes.setPurReturnInvoice(invoiceTranx);
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
                        TranxPurReturnInvoiceDutiesTaxes taxes = new TranxPurReturnInvoiceDutiesTaxes();
                        JsonObject igstObject = mIgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        //dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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
//                                    mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                                    mLedger.setTransactionDate(invoiceTranx.getPurReturnDate());
                                    mLedger.setOperations("Updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "CR", true, tranxType.getTransactionName(), "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType,
                                    "CR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getPurReturnDate(),
                                    invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());
                        }
                        taxes.setAmount(amt);
                        taxes.setPurReturnInvoice(invoiceTranx);
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
                        TranxPurReturnInvoiceDutiesTaxes taxes = new TranxPurReturnInvoiceDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        int inputGst = (int) cgstObject.get("gst").getAsDouble();
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
                        taxes.setPurReturnInvoice(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "CR", true, tranxType.getTransactionName(), "Insert");


                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.callToPostingLedger(tranxType, "CR", amt, invoiceTranx.getFiscalYear(),
                                dutiesTaxes, invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                invoiceTranx.getTranxCode());
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxPurReturnInvoiceDutiesTaxes taxes = new TranxPurReturnInvoiceDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String ledgerName = "INPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        //dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setPurReturnInvoice(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "CR", true, tranxType.getTransactionName(), "Insert");


                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.callToPostingLedger(tranxType, "CR", amt, invoiceTranx.getFiscalYear(),
                                dutiesTaxes, invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                invoiceTranx.getTranxCode());
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxPurReturnInvoiceDutiesTaxes taxes = new TranxPurReturnInvoiceDutiesTaxes();
                        JsonObject igstObject = igstList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        int inputGst = (int) igstObject.get("gst").getAsDouble();
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
                        taxes.setPurReturnInvoice(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getPurRtnNo(), "CR", true, tranxType.getTransactionName(), "Insert");

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.callToPostingLedger(tranxType, "CR", amt, invoiceTranx.getFiscalYear(),
                                dutiesTaxes, invoiceTranx.getPurReturnDate(), invoiceTranx.getId(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                invoiceTranx.getTranxCode());
                    }
                }
            }
        }
        tranxPurReturnDutiesTaxesRepository.saveAll(purchaseDutiesTaxes);
    }

    public Object editPurchaseReturns(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurReturnInvoice mPurchaseTranx = null;
        TransactionTypeMaster tranxType = null;
        ResponseMessage responseMessage = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        TranxPurReturnInvoice invoiceTranx = tranxPurReturnsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (paramMap.containsKey("branch_id")) {
            branch = branchRepository.findByIdAndStatus(Long.parseLong(request.getParameter("branch_id")), true);
        }
        invoiceTranx.setBranch(branch);
        invoiceTranx.setOutlet(outlet);
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
        LocalDate mDate = LocalDate.now();

        LocalDate returnDate = LocalDate.parse(request.getParameter("purchase_return_date"));
        Date trDt = DateConvertUtil.convertStringToDate(mDate.toString());
        invoiceTranx.setTransactionDate(trDt);

        LocalDate invoiceDate = DateConvertUtil.convertStringToLocalDate(request.getParameter("purchase_return_date"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("purchase_return_date"));
        if (invoiceDate.isEqual(DateConvertUtil.convertDateToLocalDate(invoiceTranx.getPurReturnDate()))) {
            strDt = invoiceTranx.getPurReturnDate();
        }
        invoiceTranx.setPurReturnDate(strDt);

        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(returnDate);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setPurRtnNo(request.getParameter("pur_return_invoice_no"));
        LedgerMaster purchaseAccount = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("purchase_account_id")), users.getOutlet().getId(), true);
        invoiceTranx.setPurchaseAccountLedger(purchaseAccount);
        // Long discountLedgerId = Long.parseLong(request.getParameter("purchase_disc_ledger"));
        if (!request.getParameter("purchase_disc_ledger").equalsIgnoreCase("")) {
            LedgerMaster discountLedger = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("purchase_disc_ledger")), users.getOutlet().getId(), true);
            if (discountLedger != null) {
                invoiceTranx.setPurchaseDiscountLedger(discountLedger);
            }
        }
       /* this parameter segregates whether pur return is from purchase invoice
        or purchase challan*/
        /* if (paramMap.containsKey("source")) {
            if (request.getParameter("source").equalsIgnoreCase("pur_invoice")) {
                if (paramMap.containsKey("pur_invoice_id")) {
                    TranxPurInvoice tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(
                            Long.parseLong(request.getParameter("pur_invoice_id")), true);
                    invoiceTranx.setTranxPurInvoice(tranxPurInvoice);
                }
            } else {
                if (paramMap.containsKey("pur_challan_id")) {
                    TranxPurChallan tranxPurChallan = tranxPurChallanRepository.findByIdAndStatus(
                            Long.parseLong(request.getParameter("pur_challan_id")), true);
                    invoiceTranx.setTranxPurChallan(tranxPurChallan);
                }
            }
        }*/
        LedgerMaster sundryCreditors = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), users.getOutlet().getId(), true);
        invoiceTranx.setSundryCreditors(sundryCreditors);
        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        // LedgerMaster roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), "Round off");
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setPurchaseRoundOff(roundoff);
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
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
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("totalqty")));
        invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setPurchaseDiscountPer(Double.parseDouble(request.getParameter("purchase_discount")));
        invoiceTranx.setPurchaseDiscountAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
        invoiceTranx.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_purchase_discount_amt")));
        invoiceTranx.setPurReturnSrno(Long.parseLong(request.getParameter("purchase_return_sr_no")));
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setOperations("insert");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
        else invoiceTranx.setNarration("");
        invoiceTranx.setPurReturnDate(DateConvertUtil.convertStringToDate(request.getParameter("purchase_return_date")));
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                invoiceTranx.setGstNumber(request.getParameter("gstNo"));
            }
        }
        try {
            mPurchaseTranx = tranxPurReturnsRepository.save(invoiceTranx);
            if (mPurchaseTranx != null) {
                /* Save into Duties and Taxes */
                String taxStr = request.getParameter("taxCalculation");
                //  JsonObject duties_taxes = new JsonObject(taxStr);
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoPurchaseDutiesTaxesEdit(duties_taxes, mPurchaseTranx, taxFlag, outlet.getId());
                /* save into Additional Charges  */
                String acRowsDeleted = "";
                String strJson = request.getParameter("additionalCharges");
                JsonParser parser = new JsonParser();
                JsonElement tradeElement = parser.parse(strJson);
                JsonArray additionalCharges = tradeElement.getAsJsonArray();
                saveIntoPurchaseAdditionalChargesEdit(additionalCharges, mPurchaseTranx, tranxType, users.getOutlet().getId());
                /*** delete additional charges if removed from frontend ****/
                if (paramMap.containsKey("acDelDetailsIds")) acRowsDeleted = request.getParameter("acDelDetailsIds");
                JsonElement purAChargesJson;
                if (!acRowsDeleted.equalsIgnoreCase("")) {
                    purAChargesJson = new JsonParser().parse(acRowsDeleted);
                    JsonArray deletedArrays = purAChargesJson.getAsJsonArray();
                    if (deletedArrays.size() > 0) {
                        delAddCharges(deletedArrays);
                    }
                }
                /* save into Purchase Invoice Details */
                String jsonStr = request.getParameter("row");
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) {
                    rowsDeleted = request.getParameter("rowDelDetailsIds");
                }
                saveIntoPurchaseInvoiceDetailsEdit(array, mPurchaseTranx, branch, outlet, users.getId(), tranxType, request, rowsDeleted);
                responseMessage.setMessage("purchase return updated successfully");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            } else {
                responseMessage.setMessage("Error in return update");
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseReturnsInvoice:" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
            responseMessage.setMessage("Error in return update");
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        } catch (Exception e1) {
            e1.printStackTrace();
            purchaseReturnLogger.error("Exception in saveIntoPurchaseReturnsInvoice:" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
            responseMessage.setMessage("Error in return update");
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return responseMessage;
    }

    public void delAddCharges(JsonArray deletedArrays) {
        TranxPurReturnInvoiceAddCharges mDeletedInvoices = null;
        for (JsonElement element : deletedArrays) {
            JsonObject deletedRowsId = element.getAsJsonObject();
            if (deletedRowsId.has("del_id")) {
                mDeletedInvoices = tranxPurReturnAddChargesRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                if (mDeletedInvoices != null) {
                    mDeletedInvoices.setStatus(false);
                    try {
                        tranxPurReturnAddChargesRepository.save(mDeletedInvoices);
                    } catch (DataIntegrityViolationException de) {
                        purchaseReturnLogger.error("Error in saveInto Purchase return invoice Add.Charges delete" + de.getMessage());
                        de.printStackTrace();
                        System.out.println("Exception:" + de.getMessage());

                    } catch (Exception ex) {
                        purchaseReturnLogger.error("Error in saveInto Purchase return invoice Add.Charges delete" + ex.getMessage());
                        ex.printStackTrace();
                        System.out.println("Error in saveInto Purchase return invoice Add.Charges delete:" + ex.getMessage());
                    }
                }
            }
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
                    /*  TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();*/
                    JsonObject cgstObject = mCgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
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
                    /* TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();*/
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
            if (igstList.size() > 0) {
                for (JsonElement mIgst : igstList) {
                    JsonObject igstObject = mIgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) igstObject.get("gst").getAsDouble();
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

    public JsonObject getPurchaseReturnByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        List<TranxPurReturnInvoiceProductSrNo> serialNumbers = new ArrayList<>();
        List<TranxPurReturnInvoiceAddCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxPurReturnInvoice purchaseInvoice = tranxPurReturnsRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
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

            finalResult.addProperty("paymentMode", purchaseInvoice.getPaymentMode() != null ? purchaseInvoice.getPaymentMode() : "");
            finalResult.addProperty("narration", purchaseInvoice.getNarration() != null ? purchaseInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", purchaseInvoice.getPurchaseDiscountLedger() != null ? purchaseInvoice.getPurchaseDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", purchaseInvoice.getPurchaseDiscountAmount());
            finalResult.addProperty("discountInPer", purchaseInvoice.getPurchaseDiscountPer());
            finalResult.addProperty("totalPurchaseDiscountAmt", purchaseInvoice.getTotalPurchaseDiscountAmt());
            finalResult.addProperty("totalQty", purchaseInvoice.getTotalqty());
            finalResult.addProperty("totalFreeQty", purchaseInvoice.getFreeQty());
            finalResult.addProperty("grossTotal", purchaseInvoice.getGrossAmount());
            finalResult.addProperty("totalTax", purchaseInvoice.getTotalTax());
            finalResult.addProperty("additionLedger1", purchaseInvoice.getAdditionLedger1() != null ? purchaseInvoice.getAdditionLedger1().getId() : 0);
            finalResult.addProperty("additionLedgerAmt1", purchaseInvoice.getAdditionLedgerAmt1() != null ? purchaseInvoice.getAdditionLedgerAmt1() : 0);
            finalResult.addProperty("additionLedger2", purchaseInvoice.getAdditionLedger2() != null ? purchaseInvoice.getAdditionLedger2().getId() : 0);
            finalResult.addProperty("additionLedgerAmt2", purchaseInvoice.getAdditionLedgerAmt2() != null ? purchaseInvoice.getAdditionLedgerAmt2() : 0);
            finalResult.addProperty("additionLedger3", purchaseInvoice.getAdditionLedger3() != null ? purchaseInvoice.getAdditionLedger3().getId() : 0);
            finalResult.addProperty("additionLedgerAmt3", purchaseInvoice.getAdditionLedgerAmt3() != null ? purchaseInvoice.getAdditionLedgerAmt3() : 0);
            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", purchaseInvoice.getId());
            result.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getPurReturnDate()).toString());
            result.addProperty("transaction_dt", DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getTransactionDate()).toString());
            result.addProperty("invoice_no", purchaseInvoice.getPurRtnNo());
            result.addProperty("tranx_unique_code", purchaseInvoice.getTranxCode());
            result.addProperty("purchase_sr_no", purchaseInvoice.getPurReturnSrno());
            result.addProperty("purchase_account_ledger_id", purchaseInvoice.getPurchaseAccountLedger().getId());
            result.addProperty("supplierId", purchaseInvoice.getSundryCreditors().getId());
            result.addProperty("transaction_dt", DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getTransactionDate()).toString());
            result.addProperty("additional_charges_total", purchaseInvoice.getAdditionalChargesTotal());
            result.addProperty("gstNo", purchaseInvoice.getGstNumber() != null ? purchaseInvoice.getGstNumber() : "");
            result.addProperty("paymentMode", purchaseInvoice.getPaymentMode());
            result.addProperty("isRoundOffCheck", purchaseInvoice.getIsRoundOff());
            result.addProperty("roundoff", purchaseInvoice.getRoundOff());
            result.addProperty("ledgerStateCode", purchaseInvoice.getSundryCreditors().getStateCode());

            /* End of Purchase Invoice Data */

            /* Purchase Invoice Details */
            JsonArray row = new JsonArray();
            List<TranxPurReturnDetailsUnits> unitsArray = tranxPurReturnDetailsUnitRepository.findByTranxPurReturnInvoiceIdAndStatus(purchaseInvoice.getId(), true);
            for (TranxPurReturnDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ? mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ? mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ? mUnits.getLevelC().getId().toString() : "");
                unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "");
                unitsJsonObjects.addProperty("packing", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "");
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
                unitsJsonObjects.addProperty("add_chg_amt", mUnits.getAdditionChargesAmt() != null ? mUnits.getAdditionChargesAmt() : 0.0);
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1() != null ? mUnits.getGrossAmt1() : 0.0);
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt() != null ? mUnits.getInvoiceDisAmt() : 0.0);
                if (mUnits.getTranxPurReturnInvoice().getTranxPurInvoice() != null) {
                    unitsJsonObjects.addProperty("reference_id", mUnits.getTranxPurReturnInvoice().getTranxPurInvoice().getId());
                    unitsJsonObjects.addProperty("reference_type", "pur_invoice");
                } else if (mUnits.getTranxPurReturnInvoice().getTranxPurChallan() != null) {
                    unitsJsonObjects.addProperty("reference_id", mUnits.getTranxPurReturnInvoice().getTranxPurChallan().getId());
                    unitsJsonObjects.addProperty("reference_type", "pur_challan");
                } else {
                    unitsJsonObjects.addProperty("reference_id", "");
                    unitsJsonObjects.addProperty("reference_type", "");
                }

//                unitsJsonObjects.addProperty("transaction_status", mUnits.getSta() != null ? mUnits.getTransactionStatus().getId().toString() : "");
                if (mUnits.getProductBatchNo() != null) {
                    if (mUnits.getProductBatchNo().getExpiryDate() != null) {
//                        if (purchaseInvoice.getPurReturnDate().isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
                        if (purchaseInvoice.getPurReturnDate().after(DateConvertUtil.convertStringToDate(mUnits.getProductBatchNo().getExpiryDate().toString()))) {
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
                    unitsJsonObjects.addProperty("margin_per", mUnits.getProductBatchNo().getMinMargin());
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
            /* End of Purchase return Details */
            /***** get Tranx Purcase Return Adjustment Bills *****/
            JsonArray mArray = new JsonArray();
            List<TranxPurReturnAdjustmentBills> mBill = tranxPurReturnAdjBillsRepository.findByTranxPurReturnIdAndStatus(id, true);
            for (TranxPurReturnAdjustmentBills mAdjumentBill : mBill) {
                JsonObject mObject = new JsonObject();
                if (mAdjumentBill.getSource().equalsIgnoreCase("pur_invoice"))
                    mObject.addProperty("invoice_id", mAdjumentBill.getTranxPurInvoice().getId());
                mObject.addProperty("invoice_unique_id", "pur_invoice," + mAdjumentBill.getTranxPurInvoice().getId());
                mObject.addProperty("id", mAdjumentBill.getId());
                mObject.addProperty("total_amt", mAdjumentBill.getTotalAmt());
                mObject.addProperty("source", mAdjumentBill.getSource());
                mObject.addProperty("paid_amt", mAdjumentBill.getPaidAmt());
                mObject.addProperty("remaining_amt", mAdjumentBill.getRemainingAmt());
                mObject.addProperty("invoice_no", mAdjumentBill.getTranxPurInvoice().getVendorInvoiceNo());
                mArray.add(mObject);
            }
            /**** Barcode Data *****/
            List<ProductBarcode> barcodeList = new ArrayList<>();
            barcodeList = barcodeRepository.findByTransactionIdAndStatus(purchaseInvoice.getId(), true);
            JsonArray barcodeJsonList = new JsonArray();
            if (barcodeList != null && barcodeList.size() > 0) {
                for (ProductBarcode mBarcode : barcodeList) {
                    JsonObject barcodeJsonObject = new JsonObject();
                    barcodeJsonObject.addProperty("product_id", mBarcode.getProduct().getId());
                    barcodeJsonObject.addProperty("product_name", mBarcode.getProduct().getProductName());
                    barcodeJsonObject.addProperty("barcode_id", mBarcode.getId());
                    barcodeJsonObject.addProperty("barcode_no", mBarcode.getBarcodeUniqueCode());
                    barcodeJsonObject.addProperty("batch_id", mBarcode.getProductBatch() != null ? mBarcode.getProductBatch().getId().toString() : "");
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
            }
            /* Purchase Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            additionalCharges = tranxPurReturnAddChargesRepository.findByPurReturnInvoiceIdAndStatus(purchaseInvoice.getId(), true);
            if (additionalCharges.size() > 0) {
                for (TranxPurReturnInvoiceAddCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledgerId", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    // Check if percent is null, if so, set it to 0.0
                    Double percent = mAdditionalCharges.getPercent() != null ? mAdditionalCharges.getPercent() : 0.0;
                    json_charges.addProperty("percent", percent);
                    jsonAdditionalList.add(json_charges);
                }
            }


            /*   End of Purchase Additional Charges*/
            finalResult.add("barcode_list", barcodeJsonList);
            finalResult.add("additionalCharges", jsonAdditionalList);
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);
            finalResult.add("billLst", mArray);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purchaseReturnLogger.error("Error in getPurchaseReturnByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            purchaseReturnLogger.error("Error in getPurchaseReturnByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject getPurchaseReturnSupplierListByProductId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        Long productId = Long.parseLong(request.getParameter("productId"));

        List<TranxPurReturnDetailsUnits> tranxPurReturnDetailsUnits = tranxPurReturnDetailsUnitRepository.findByProductIdAndStatusOrderByIdDesc(productId, true);

        for (TranxPurReturnDetailsUnits obj : tranxPurReturnDetailsUnits) {
            JsonObject response = new JsonObject();
            response.addProperty("supplier_name", obj.getTranxPurReturnInvoice().getSundryCreditors().getLedgerName());
            response.addProperty("invoice_no", obj.getTranxPurReturnInvoice().getId());
            response.addProperty("invoice_date", obj.getTranxPurReturnInvoice().getPurReturnDate().toString());
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

    public Object validatePurchaseReturnInvoices(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurReturnInvoice purInvoice = null;
        Long sundryCreditorId = Long.parseLong(request.getParameter("supplier_id"));
        if (users.getBranch() != null) {
            purInvoice = tranxPurReturnsRepository.findByOutletIdAndBranchIdAndSundryCreditorsIdAndPurRtnNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), sundryCreditorId, request.getParameter("bill_no"));
        } else {
            purInvoice = tranxPurReturnsRepository.findByOutletIdAndSundryCreditorsIdAndPurRtnNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), sundryCreditorId, request.getParameter("bill_no"));
        }
        if (purInvoice != null) {
            responseMessage.setMessage("Duplicate purchase return invoice number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New purchase return invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }


    public JsonObject mobileSCReturnList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        String flag = "purReturnList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<LedgerMaster> balanceSummaries = new ArrayList<>();
        balanceSummaries = ledgerRepository.findByPrincipleGroupsIdAndStatus(5L, true);
        for (LedgerMaster balanceSummary : balanceSummaries) {
            Long ledgerId = balanceSummary.getId();
            JsonObject jsonObject = new JsonObject();
            LocalDate endDate = null;
            LocalDate startDate = null;
          /*  if (!request.get("end_date").equalsIgnoreCase("") && !request.get("start_date").equalsIgnoreCase("")) {
                System.out.println("End Date:" + request.get("end_date"));
                endDate = LocalDate.parse(request.get("end_date").toString());
                startDate = LocalDate.parse(request.get("start_date"));
            } else {

                LocalDate today = LocalDate.now();
                System.out.println("First day: " + today.withDayOfMonth(1));
                System.out.println("Last day: " + today.withDayOfMonth(today.lengthOfMonth()));
                startDate = today.withDayOfMonth(1);
                endDate = today.withDayOfMonth(today.lengthOfMonth());
            }*/
            try {
                endDate = LocalDate.parse(request.get("end_date"));
                startDate = LocalDate.parse(request.get("start_date"));
                list = tranxPurReturnsRepository.findmobilePurReturnTotalAmt(ledgerId, startDate, endDate, true);
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
                }


            } catch (Exception e) {
                purchaseReturnLogger.error("Error in salesDelete()->" + e.getMessage());
            }

            jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());
            if (sumTotal > 0) result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        json.addProperty("closingBalance", closingBalance);
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public Object purReturnMobileInvoiceList(Map<String, String> request) {
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

//        Map<String, String[]> paramMap = request.get();

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
        List<TranxPurReturnInvoice> purInvoice = new ArrayList<>();

        purInvoice = tranxPurReturnsRepository.findPurReturnInvoicesListNoBr(Long.valueOf(request.get("sundry_creditor_id")), startDatep, endDatep, true);

        for (TranxPurReturnInvoice invoices : purInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("return_id", invoices.getId());
            response.addProperty("return_no", invoices.getPurRtnNo());
            response.addProperty("transaction_date", invoices.getTransactionDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_creditor_name", invoices.getSundryCreditors().getLedgerName());
            response.addProperty("purReturn_account_name", invoices.getPurchaseAccountLedger().getLedgerName());
            response.addProperty("sundry_creditor_id", invoices.getSundryCreditors().getId());
            response.addProperty("baseAmt", invoices.getTotalBaseAmount());
            response.addProperty("taxAmt", invoices.getTotalTax());
            response.addProperty("flag", "purReturn");
            result.add(response);
        }
        System.out.println(result);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public Object purReturnMobileInvoiceDetailsList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        List<TranxPurReturnDetailsUnits> purInvoice = new ArrayList<>();
        purInvoice = tranxPurReturnDetailsUnitRepository.findByTranxPurReturnInvoiceIdAndStatus(Long.valueOf(request.get("purReturn_id")), true);
        for (TranxPurReturnDetailsUnits invoices : purInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_id", invoices.getTranxPurReturnInvoice().getId());
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
        TranxPurReturnInvoice tranxPurReturnInvoice = tranxPurReturnsRepository.findByIdAndStatus(Long.parseLong(request.get("purReturn_id")), true);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.addProperty("totalBaseAmt", tranxPurReturnInvoice.getTotalBaseAmount());
        output.addProperty("roundoff", tranxPurReturnInvoice.getRoundOff());
        output.addProperty("finalAmt", tranxPurReturnInvoice.getTotalAmount());
        output.addProperty("taxAmt", tranxPurReturnInvoice.getTotalTax());
        output.addProperty("totalDisAmt", tranxPurReturnInvoice.getTotalPurchaseDiscountAmt());
        output.add("data", result);
        return output;
    }

    public JsonObject getCreditorsPendingBills(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long ledgerId = Long.parseLong(request.getParameter("ledger_id"));
        String type = request.getParameter("type");
        List<TranxPurInvoice> mInput = new ArrayList<>();
        List<TranxPurInvoice> purInvoice = new ArrayList<>();
        JsonArray result = new JsonArray();
        JsonObject finalResult = new JsonObject();
        try {
            /* start of SC of bill by bill */
            if (type.equalsIgnoreCase("SC")) {
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledgerId, true);
                /* checking for Bill by bill (bill by bill id: 1) */
                if (ledgerMaster.getBalancingMethod().getId() == 1) {
                    /* find all purchase invoices against sundry creditor */
                    if (users.getBranch() != null) {
                        purInvoice = tranxPurInvoiceRepository.findPendingBillsByBranch(users.getOutlet().getId(), users.getBranch().getId(), true, ledgerId);
                    } else {
                        purInvoice = tranxPurInvoiceRepository.findPRPendingBills(users.getOutlet().getId(), true, ledgerId);
                    }
                    if (purInvoice.size() > 0) {
                        for (TranxPurInvoice newPurInvoice : purInvoice) {
                            JsonObject response = new JsonObject();
                            response.addProperty("invoice_id", newPurInvoice.getId());
                            response.addProperty("amount", newPurInvoice.getBalance());
                            response.addProperty("total_amt", newPurInvoice.getTotalAmount());
                            response.addProperty("invoice_date", newPurInvoice.getInvoiceDate().toString());
                            response.addProperty("invoice_no", newPurInvoice.getVendorInvoiceNo());
                            response.addProperty("ledger_id", ledgerId);
                            response.addProperty("source", "pur_invoice");
                            result.add(response);
                        }
                    }
                } else {
                    /*  supplier :  on Account  */
                    Double sumCR = 0.0;
                    Double sumDR = 0.0, closingBalance = 0.0;
                    sumCR = ledgerTransactionPostingsRepository.findsumCR(ledgerId);
                    sumDR = ledgerTransactionPostingsRepository.findsumDR(ledgerId);
                    closingBalance = sumCR - sumDR;//0-(-0.40)-0.20
                    if (closingBalance != 0) {
                        JsonObject response = new JsonObject();
                        response.addProperty("amount", closingBalance);
                        response.addProperty("ledger_id", ledgerId);
                        result.add(response);
                    }
                }

            }

        } catch (Exception e) {
            purchaseReturnLogger.error("Exception in: getCreditorsPendingBillsNew ->" + e.getMessage());
            System.out.println("Exception in: get_creditors_pending_bills ->" + e.getMessage());
            e.printStackTrace();
        }
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("list", result);
        return finalResult;
    }

    public Object validatePurchaseReturnUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurReturnInvoice purReturn = null;
        Long sundryCreditorId = Long.parseLong(request.getParameter("supplier_id"));
        Long invoiceId = Long.parseLong(request.getParameter("return_id"));
        System.out.println("Bill no = " + request.getParameter("bill_no"));
        if (users.getBranch() != null) {
            purReturn = tranxPurReturnsRepository.findByOutletIdAndBranchIdAndSundryCreditorsIdAndPurRtnNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), sundryCreditorId, request.getParameter("bill_no"));
        } else {
            purReturn = tranxPurReturnsRepository.findByOutletIdAndSundryCreditorsIdAndPurRtnNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), sundryCreditorId, request.getParameter("bill_no"));
        }
        if (purReturn != null && invoiceId != purReturn.getId()) {
            responseMessage.setMessage("Duplicate purchase return number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New purchase return number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public JsonObject purchasePrintReturn(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalResult = new JsonObject();
        TranxPurReturnInvoice purReturn = null;
        String source = request.getParameter("source");
        String key = request.getParameter("print_type"); //check whether printbill is calling from create page or from list page
        /***** if  print_type is create, then use serialnumber of invoice to fetch invoice details ,
         * if print_type is list then use invoice id to fetch invoice details *****/
        try {
            String invoiceNo = request.getParameter("id");
            Long id = 0L;
            if (source.equalsIgnoreCase("purchase_return")) {
                if (users.getBranch() != null) {

                    id = Long.parseLong(invoiceNo);
                    purReturn = tranxPurReturnsRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);

                } else {

                    id = Long.parseLong(invoiceNo);
                    purReturn = tranxPurReturnsRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);

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
            if (purReturn != null) {
                //   list = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatus(purInvoice.getId(), true);
                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                debtorsObject.addProperty("supplier_name", purReturn.getSundryCreditors().getLedgerName());
                debtorsObject.addProperty("supplier_address", purReturn.getSundryCreditors().getAddress());
                debtorsObject.addProperty("supplier_state", String.valueOf(purReturn.getSundryCreditors().getState().getName()));
                debtorsObject.addProperty("supplier_gstin", purReturn.getSundryCreditors().getGstin());
                debtorsObject.addProperty("supplier_phone", purReturn.getSundryCreditors().getMobile());
                JsonObject invoiceObject = new JsonObject();
                /* Sales Invoice Data */
                invoiceObject.addProperty("id", purReturn.getId());
                invoiceObject.addProperty("invoice_dt", purReturn.getPurReturnDate().toString());
                invoiceObject.addProperty("invoice_no", purReturn.getPurRtnNo());
                invoiceObject.addProperty("state_code", purReturn.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", purReturn.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(purReturn.getTaxableAmount()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(purReturn.getTotaligst()));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(purReturn.getTotalcgst()));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(purReturn.getTotalsgst()));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(purReturn.getTotalBaseAmount()));
                invoiceObject.addProperty("total_discount", numFormat.numFormat(purReturn.getTotalPurchaseDiscountAmt()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(purReturn.getTotalAmount()));
                // invoiceObject.addProperty("advanced_amount", numFormat.numFormat(purInvoice.getAd() != null ? purInvoice.getAdvancedAmount() : 0.0));
                // invoiceObject.addProperty("payment_mode", purInvoice.getPaymentMode());

                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();

                /* getting Units of Sales Quotations*/
                List<TranxPurReturnDetailsUnits> unitDetails = tranxPurReturnDetailsUnitRepository.findByTranxPurReturnInvoiceIdAndStatus(purReturn.getId(), true);
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
            purchaseReturnLogger.error("Error in getInvoiceBillPrint :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            purchaseReturnLogger.error("Error in getInvoiceBillPrint :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

//    public Object getSupplierListByReturn(HttpServletRequest request) {
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        JsonArray result = new JsonArray();
//        Long ledgerId = Long.parseLong(request.getParameter("ledgerId"));
//        List<TranxPurInvoiceDetailsUnits> tranxPurInvoiceDetailsUnits =
//                tranxPurReturnDetailsUnitRepository.findInvoices(ledgerId, true);
//        for (TranxPurInvoiceDetailsUnits obj : tranxPurInvoiceDetailsUnits) {
//            JsonObject response = new JsonObject();
////            response.addProperty("supplier_name", obj.getSundryCreditors().getLedgerName());
////            response.addProperty("invoice_no", obj.getPurRtnNo());
////            response.addProperty("invoice_date", obj.getPurReturnDate().toString());
//            response.addProperty("batch", obj.getProductBatchNo() != null ? obj.getProductBatchNo().getBatchNo() : "");
//            response.addProperty("mrp", obj.getProductBatchNo() != null ? obj.getProductBatchNo().getMrp().toString() : "");
//            response.addProperty("quantity", obj.getQty());
//            response.addProperty("cost", obj.getProductBatchNo() != null ?
//                    obj.getProductBatchNo().getCosting().toString() : "");
//            response.addProperty("dis_per", obj.getDiscountPer());
//            response.addProperty("dis_amt", obj.getDiscountAmount());
//            result.add(response);
//        }
//       JsonObject output = new JsonObject();
//        output.addProperty("message", "success");
//        output.addProperty("responseStatus", HttpStatus.OK.value());
//        output.add("data", result);
//        return output;
//    }
}
