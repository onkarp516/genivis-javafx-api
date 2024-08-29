package in.truethics.ethics.ethicsapiv10.service.tranx_service.sales;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.salesdto.SalesReturnDTO;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.StockTranxDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerOpeningClosingDetailRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurReturnDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.*;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.Constants;
import in.truethics.ethics.ethicsapiv10.util.ClosingUtility;
import in.truethics.ethics.ethicsapiv10.util.DateConvertUtil;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import in.truethics.ethics.ethicsapiv10.util.PostingUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

@Service
public class TranxSalesReturnService {
    @Autowired
    private NumFormat numFormat;
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
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private TranxSalesReturnRepository tranxSalesReturnRepository;
    @Autowired
    private TranxSalesReturnDutiesTaxesRepository tranxSalesReturnTaxesRepository;
    @Autowired
    private TranxSalesReturnAddiChargesRepository tranxSalesReturnAddiChargesRepository;
    @Autowired
    private TranxSalesChallanRepository tranxSalesChallanRepository;
    @Autowired
    private TranxSalesReturnRepository tranxSalesReturnsRepository;
    @Autowired
    private TranxCreditNoteNewReferenceRepository tranxCreditNoteNewReferenceRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private TranxCreditNoteDetailsRepository tranxCreditNoteDetailsRepository;
    @Autowired
    private TranxSalesInvoicePrSrNoRepository tranxSalesInvoicePrSrNoRepository;
    @Autowired
    private TranxSalesInvoiceAdditionalChargesRepository salesInvoiceAdditionalChargesRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private TranxSalesReturnDetailsUnitsRepository tranxSalesReturnDetailsUnitsRepository;
    @Autowired
    private TranxSalesInvoiceDetailsUnitRepository tranxSalesInvoiceDetailsUnitRepository;
    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private ProductData productData;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
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
    private TranxSalesReturnAdjBillsRepository tranxSalesReturnAdjBillsRepository;

    @Autowired
    private ClosingUtility closingUtility;
    @PersistenceContext
    private EntityManager entityManager;

    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice
    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice
    List<Long> mInputList = new ArrayList<>();
    List<Long> ledgerInputList = new ArrayList<>();
    private static final Logger salesReturnLogger = LogManager.getLogger(TranxSalesReturnService.class);
    @Autowired
    private TranxPurReturnDetailsUnitRepository tranxPurReturnDetailsUnitRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private TranxSalesChallanProductSerialNumberRepository tranxSalesChallanProductSerialNumberRepository;
    @Autowired
    private TranxSalesReturnPrSrNoRepository tranxSalesReturnPrSrNoRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Autowired
    private PostingUtility postingUtility;
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;


    @Autowired
    private StockTranxDetailsRepository stkTranxDetailsRepository;

    /******* save into Sales Return Invoice *******/
    public Object createSalesReturnsInvoices(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesReturnInvoice mSalesTranx = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseMessage = new ResponseMessage();
        mSalesTranx = saveIntoSalesReturn(request);
        if (mSalesTranx != null) {
            /* Accounting Postings */
            insertIntoLedgerTranxDetails(mSalesTranx);// Accounting Postings
            /*** creating new reference while adjusting
             return amount into next sales invoice bill ***/
            if (request.getParameter("paymentMode").equalsIgnoreCase("credit"))
                insertIntoNewReference(mSalesTranx, request, "create");
            else {
                /***** dont allow the invoice next time in bill selection module next time if invoice is adjusted *****/
                TranxSalesInvoice invoice = tranxSalesInvoiceRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_invoice_id")), true);
                invoice.setTransactionStatus(2L);
                tranxSalesInvoiceRepository.save(invoice);
                /* Adjust retun amount into selected sales invoices */
                if (paramMap.containsKey("billLst")) {
                    String jsonStr = request.getParameter("billLst");
                    JsonElement purDetailsJson = new JsonParser().parse(jsonStr);
                    JsonArray array = purDetailsJson.getAsJsonArray();
                    for (JsonElement mElement : array) {
                        JsonObject mObject = mElement.getAsJsonObject();
                        Long invoiceId = mObject.get("invoice_id").getAsLong();
                        TranxSalesInvoice mInvoice = tranxSalesInvoiceRepository.findByIdAndStatus(invoiceId, true);
                        Double paidAmt = mObject.get("paid_amt").getAsDouble();
                        if (mInvoice != null) {
                            try {
//                            mInvoice.setBalance(mSalesTranx.getTranxSalesInvoice().getBalance() - paidAmt);
                                mInvoice.setBalance(mObject.get("remaining_amt").getAsDouble());
                                tranxSalesInvoiceRepository.save(mInvoice);
                            } catch (Exception e) {
                                e.printStackTrace();
                                salesReturnLogger.error("Exception in Purchase Return:" + e.getMessage());
                            }
                        }
                        /***** Save Into Tranx Sales Return Adjument Bills Table ******/
                        TranxSalesReturnAdjustmentBills mBills = new TranxSalesReturnAdjustmentBills();
                        if (mObject.get("source").getAsString().equalsIgnoreCase("sales_invoice"))
                            mBills.setTranxSalesInvoice(mInvoice);
                        mBills.setSource(mObject.get("source").getAsString());
                        mBills.setPaidAmt(paidAmt);
                        mBills.setRemainingAmt(mObject.get("remaining_amt").getAsDouble());
                        mBills.setTotalAmt(mObject.get("amount").getAsDouble());
                        mBills.setTranxSalesReturnId(mSalesTranx.getId());
                        mBills.setStatus(true);
                        mBills.setCreatedBy(mSalesTranx.getCreatedBy());
                        tranxSalesReturnAdjBillsRepository.save(mBills);
                    }
                }
            }
            String strJson = request.getParameter("additionalCharges");
            JsonElement tradeElement = new JsonParser().parse(strJson);
            JsonArray additionalCharges = tradeElement.getAsJsonArray();
            saveIntoPurchaseAdditionalCharges(additionalCharges, mSalesTranx, users.getOutlet().getId());

            /***** Insert into DayBook *****/
            saveIntoDayBook(mSalesTranx);
            responseMessage.setMessage("sales return invoice created successfully");
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
            responseMessage.setMessage("Error in sales invoice creation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    public void saveIntoPurchaseAdditionalCharges(JsonArray additionalCharges, TranxSalesReturnInvoice mPurchaseTranx, Long outletId) {
        List<TranxSalesReturnInvoiceAddCharges> chargesList = new ArrayList<>();
        if (mPurchaseTranx.getAdditionalChargesTotal() > 0) {
            for (JsonElement mList : additionalCharges) {
                TranxSalesReturnInvoiceAddCharges charges = new TranxSalesReturnInvoiceAddCharges();
                JsonObject object = mList.getAsJsonObject();
                Double amount = object.get("amt").getAsDouble();
                Long ledgerId = object.get("ledgerId").getAsLong();
                LedgerMaster addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges.setAmount(amount);
                charges.setAdditionalCharges(addcharges);
                charges.setTranxSalesReturnInvoice(mPurchaseTranx);
                //charges.setTranxPurInvoice(mPurchaseTranx.getTranxPurInvoice());
                charges.setStatus(true);
                charges.setOperation("inserted");
                charges.setPercent(object.get("percent").getAsDouble());
                chargesList.add(charges);
            }
        }
        try {
            tranxSalesReturnAddiChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            //purchaseReturnLogger.error("Exception in saveIntoPurchaseAdditionalCharges:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            //purchaseReturnLogger.error("Exception in saveIntoPurchaseAdditionalCharges:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveIntoDayBook(TranxSalesReturnInvoice mSalesTranx) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(mSalesTranx.getOutlet());
        if (mSalesTranx.getBranch() != null) dayBook.setBranch(mSalesTranx.getBranch());
        dayBook.setAmount(mSalesTranx.getTotalAmount());
        dayBook.setTranxDate(DateConvertUtil.convertDateToLocalDate(mSalesTranx.getTransactionDate()));
        dayBook.setParticulars(mSalesTranx.getSundryDebtors().getLedgerName());
        dayBook.setVoucherNo(mSalesTranx.getSalesReturnNo());
        dayBook.setVoucherType("Sales Return Invoice");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }


    /****** Save into sales returns  ******/
    private TranxSalesReturnInvoice saveIntoSalesReturn(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesReturnInvoice mSalesTranx = null;
        TranxSalesReturnInvoice invoiceTranx = null;
        TransactionTypeMaster tranxType = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        invoiceTranx = new TranxSalesReturnInvoice();
        if (users.getBranch() != null) {
            branch = users.getBranch();
            invoiceTranx.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        invoiceTranx.setOutlet(outlet);
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSRT");
        LocalDate mDate = LocalDate.now();
        invoiceTranx.setTransactionDate(DateConvertUtil.convertStringToDate(mDate.toString()));
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(mDate);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setSalesRtnSrNo(Long.parseLong(request.getParameter("sales_return_sr_no")));
        invoiceTranx.setSalesReturnNo(request.getParameter("sales_return_no"));
        LedgerMaster salesAccounts = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), users.getOutlet().getId(), true);
        invoiceTranx.setSalesAccountLedger(salesAccounts);
        LedgerMaster discountLedger = null;
        if (users.getBranch() == null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales discount", users.getOutlet().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        if (discountLedger != null) {
            invoiceTranx.setSalesDiscountLedger(discountLedger);
        }
        /* this parameter segregates whether sales return is from sales invoice
        or sales challan*/
        if (request.getParameter("source").equalsIgnoreCase("sales_invoice")) {
            if (paramMap.containsKey("sales_invoice_id")) {
                TranxSalesInvoice tranxSalesInvoice = tranxSalesInvoiceRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_invoice_id")), true);
                invoiceTranx.setTranxSalesInvoice(tranxSalesInvoice);
            }
        } else {
            if (paramMap.containsKey("sales_challan_id")) {
                TranxSalesChallan tranxSalesChallan = tranxSalesChallanRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_challan_id")), true);
                invoiceTranx.setTranxSalesChallan(tranxSalesChallan);
            }
        }
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), true);
        invoiceTranx.setSundryDebtors(sundryDebtors);
        //    invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setSalesRoundOff(roundoff);
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
        //invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        invoiceTranx.setSalesDiscountAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_sales_discount_amt")));
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setOperations("inserted");
        if (paramMap.containsKey("narration")) invoiceTranx.setNarration(request.getParameter("narration"));
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
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
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
            mSalesTranx = tranxSalesReturnRepository.save(invoiceTranx);
            /* Save into Sales returns Duties and Taxes */
            if (mSalesTranx != null) {
                String taxStr = request.getParameter("taxCalculation");
                JsonObject duties_taxes = new JsonParser().parse(taxStr).getAsJsonObject();
                saveInoDutiesAndTaxes(duties_taxes, mSalesTranx, taxFlag);
                /* Save into Sales returns Details */
                String jsonStr = request.getParameter("row");
                JsonArray invoiceDetails = new JsonParser().parse(jsonStr).getAsJsonArray();
                saveIntoSalesInvoiceDetails(invoiceDetails, mSalesTranx, branch, outlet, users.getId(), tranxType);
            }

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesReturnLogger.error("Exception in saveIntoSalesReturn:" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesReturnLogger.error("Exception in saveIntoSalesReturn:" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mSalesTranx;
    }

    private TranxSalesReturnInvoice saveIntoSalesReturnEdit(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesReturnInvoice mSalesTranx = null;
        TranxSalesReturnInvoice invoiceTranx = null;
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSRT");
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        invoiceTranx = tranxSalesReturnRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        dbList = ledgerTransactionPostingsRepository.findByTransactionId(invoiceTranx.getId(), tranxType.getId());
        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(invoiceTranx.getId(),
                tranxType.getId(), true);
        if (users.getBranch() != null) {
            branch = users.getBranch();
            invoiceTranx.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        invoiceTranx.setOutlet(outlet);
        LocalDate mDate = LocalDate.now();
        Date dt = DateConvertUtil.convertStringToDate(mDate.toString());
        if (mDate.isEqual(DateConvertUtil.convertDateToLocalDate(invoiceTranx.getTransactionDate()))) {
            dt = invoiceTranx.getTransactionDate();
        }
        invoiceTranx.setTransactionDate(dt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(mDate);
        if (fiscalYear != null) {
            invoiceTranx.setFiscalYear(fiscalYear);
            invoiceTranx.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        invoiceTranx.setSalesRtnSrNo(Long.parseLong(request.getParameter("sales_return_sr_no")));
        invoiceTranx.setSalesReturnNo(request.getParameter("sales_return_no"));
        LedgerMaster salesAccounts = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), users.getOutlet().getId(), true);
        invoiceTranx.setSalesAccountLedger(salesAccounts);
        /* calling store procedure for updating opening and closing  of Sales Account  */
        if (invoiceTranx.getId() != null && invoiceTranx.getId() != null && tranxType.getId() != null) {
            Boolean isContains = dbList.contains(invoiceTranx.getId());
            Boolean isLedgerContains = ledgerList.contains(invoiceTranx.getId());
            mInputList.add(invoiceTranx.getId());
            ledgerInputList.add(invoiceTranx.getId());
            if (isContains) {
                /* edit ledger tranx if same ledger is modified */
                /**** New Postings Logic *****/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(invoiceTranx.getId(), tranxType.getId(), invoiceTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(Double.parseDouble(request.getParameter("total_base_amt")));
                    mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                    mLedger.setOperations("updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {

                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("total_base_amt")), salesAccounts, tranxType, salesAccounts.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "DR", true, "Sales Invoice", "Insert");
            }

            Double amount = Double.parseDouble(request.getParameter("total_base_amt"));
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSalesAccountLedger().getId(),
                    tranxType, "DR", invoiceTranx.getId(), salesAccounts, dt, fiscalYear, invoiceTranx.getOutlet(),
                    invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
        }/* end of calling store procedure for Sales Account updating opening and closing */
        LedgerMaster discountLedger = null;
        if (users.getBranch() == null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("sales discount", users.getOutlet().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("sales discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        if (discountLedger != null) {
            invoiceTranx.setSalesDiscountLedger(discountLedger);
        }
        /* calling store procedure for updating opening and closing of sales returns Discount  */
        Boolean isContains = dbList.contains(discountLedger.getId());
        Boolean isLedgerContains = ledgerList.contains(discountLedger.getId());
        mInputList.add(discountLedger.getId());
        ledgerInputList.add(discountLedger.getId());
        if (isContains) {
            /**** New Postings Logic *****/
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(discountLedger.getId(), tranxType.getId(), invoiceTranx.getId());
            if (mLedger != null) {
                mLedger.setAmount(Double.parseDouble(request.getParameter("total_sales_discount_amt")));
                mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                mLedger.setOperations("Updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            }
        } else {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("total_sales_discount_amt")), discountLedger, tranxType, discountLedger.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "CR", true, tranxType.getTransactionName(), "Insert");
        }

        Double amount = Double.parseDouble(request.getParameter("total_sales_discount_amt"));
        /**** NEW METHOD FOR LEDGER POSTING ****/
        postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSalesDiscountLedger().getId(),
                tranxType, "CR", invoiceTranx.getId(), discountLedger, dt, fiscalYear, invoiceTranx.getOutlet(),
                invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
        /* end of calling store procedure for updating opening and closing of Purchase Discount */
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), true);
        invoiceTranx.setSundryDebtors(sundryDebtors);
        /* calling store procedure for updating opening and closing of Sundry Creditors  */
        Boolean isContains1 = dbList.contains(sundryDebtors.getId());
        isLedgerContains = ledgerList.contains(sundryDebtors.getId());
        mInputList.add(sundryDebtors.getId());
        ledgerInputList.add(sundryDebtors.getId());
        if (isContains1) {
            //    transactionDetailsRepository.ledgerPostingEdit(sundryDebtors.getId(), invoiceTranx.getId(), tranxType.getId(), "DR", Double.parseDouble(request.getParameter("totalamt")));
            /**** New Postings Logic *****/
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(sundryDebtors.getId(), tranxType.getId(), invoiceTranx.getId());
            if (mLedger != null) {
                mLedger.setAmount(Double.parseDouble(request.getParameter("bill_amount")));
                mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                mLedger.setOperations("updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            }
        } else {
            /* insert ledger tranx if ledger is changed */
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(Double.parseDouble(request.getParameter("bill_amount")), sundryDebtors, tranxType, sundryDebtors.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "CR", true, tranxType.getTransactionName(), "Insert");
        }

        amount = Double.parseDouble(request.getParameter("bill_amount"));
        /**** NEW METHOD FOR LEDGER POSTING ****/
        postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSundryDebtors().getId(),
                tranxType, "CR", invoiceTranx.getId(), sundryDebtors, dt, fiscalYear, invoiceTranx.getOutlet(),
                invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
        /* end of calling store procedure for updating opening and closing of Sundry Creditors  */
        // LedgerMaster roundoff = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId("Round off", outlet.getId());
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        invoiceTranx.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        invoiceTranx.setSalesRoundOff(roundoff);

        isContains = dbList.contains(roundoff.getId());
        isLedgerContains = ledgerList.contains(roundoff.getId());
        ledgerInputList.add(roundoff.getId());
        Double rf = Double.parseDouble(request.getParameter("roundoff"));
        /* inserting into Round off  JSON Object */
        if (isContains) {
            /**** New Postings Logic *****/
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(roundoff.getId(), tranxType.getId(), invoiceTranx.getId());
            if (mLedger != null) {
                mLedger.setAmount(rf);
                mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                mLedger.setOperations("Updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            }
        } else {
            if (rf >= 0) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(rf, roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Insert");
            } else {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(rf, roundoff, tranxType, roundoff.getAssociateGroups(), fiscalYear, invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "CR", true, tranxType.getTransactionName(), "Insert");
            }
        }

        /**** NEW METHOD FOR LEDGER POSTING ****/
        String tranxAction = "CR";
        if (rf >= 0)
            tranxAction = "DR";
        amount = Math.abs(rf);
        /**** NEW METHOD FOR LEDGER POSTING ****/
        postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, invoiceTranx.getSalesRoundOff().getId(),
                tranxType, tranxAction, invoiceTranx.getId(), roundoff, dt, fiscalYear, invoiceTranx.getOutlet(),
                invoiceTranx.getBranch(), invoiceTranx.getTranxCode());
        /* end of inserting into Sundry Creditors JSON Object */
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
        invoiceTranx.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        invoiceTranx.setTotalqty(Long.parseLong(request.getParameter("totalqty")));
//        invoiceTranx.setTcs(Double.parseDouble(request.getParameter("tcs")));
        invoiceTranx.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        invoiceTranx.setSalesDiscountPer(Double.parseDouble(request.getParameter("sales_discount")));
        invoiceTranx.setSalesDiscountAmount(Double.parseDouble(request.getParameter("sales_discount_amt")));
        invoiceTranx.setTotalSalesDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        invoiceTranx.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        invoiceTranx.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        invoiceTranx.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        invoiceTranx.setStatus(true);
        invoiceTranx.setCreatedBy(users.getId());
        invoiceTranx.setOperations("updated");
        invoiceTranx.setNarration(request.getParameter("narration"));
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
                        ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt1(), mSalesTranx.getAdditionLedger1(), tranxType, mSalesTranx.getAdditionLedger1().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), mSalesTranx.getAdditionLedgerAmt1() > 0 ? "DR" : "CR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
            }
        }*/
       /* if (paramMap.containsKey("additionalChgLedger2")) {
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
                        ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt2(), mSalesTranx.getAdditionLedger2(), tranxType, mSalesTranx.getAdditionLedger2().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), mSalesTranx.getAdditionLedgerAmt2() > 0 ? "DR" : "CR", true, tranxType.getTransactionCode(), "Insert");
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
                        mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    } else {
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(mSalesTranx.getAdditionLedgerAmt3(), mSalesTranx.getAdditionLedger2(), tranxType, mSalesTranx.getAdditionLedger3().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), mSalesTranx.getAdditionLedgerAmt3() > 0 ? "DR" : "CR", true, tranxType.getTransactionCode(), "Insert");
                    }
                }
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
        }
        if (paramMap.containsKey("isRoundOffCheck"))
            invoiceTranx.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));

        try {
            mSalesTranx = tranxSalesReturnRepository.save(invoiceTranx);
            /* Save into Sales returns Duties and Taxes */
            if (mSalesTranx != null) {
                String taxStr = request.getParameter("taxCalculation");
                JsonObject duties_taxes = new JsonParser().parse(taxStr).getAsJsonObject();
                saveInoDutiesAndTaxesEdit(duties_taxes, mSalesTranx, taxFlag, mSalesTranx.getOutlet().getId());

                /* Save into Sales returns Details */
                String jsonStr = request.getParameter("row");
                JsonArray invoiceDetails = new JsonParser().parse(jsonStr).getAsJsonArray();
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) rowsDeleted = request.getParameter("rowDelDetailsIds");
                saveIntoSalesInvoiceDetailsEdit(invoiceDetails, mSalesTranx, branch, outlet, users.getId(), tranxType, rowsDeleted);
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesReturnLogger.error("Exception in saveIntoSalesReturn:" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesReturnLogger.error("Exception in saveIntoSalesReturn:" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mSalesTranx;
    }

    public void delAddCharges(JsonArray deletedArrays) {
        TranxSalesReturnInvoiceAddCharges mDeletedInvoices = null;
        for (JsonElement element : deletedArrays) {
            JsonObject deletedRowsId = element.getAsJsonObject();
            mDeletedInvoices = tranxSalesReturnAddiChargesRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
            if (mDeletedInvoices != null) {
                mDeletedInvoices.setStatus(false);
                try {
                    tranxSalesReturnAddiChargesRepository.save(mDeletedInvoices);
                } catch (DataIntegrityViolationException de) {
                    salesReturnLogger.error("Error into Sales return invoice Add.Charges edit" + de.getMessage());
                    de.printStackTrace();
                    System.out.println("Exception:" + de.getMessage());
                } catch (Exception ex) {
                    salesReturnLogger.error("Error into Sales return invoice Add.Charges edit" + ex.getMessage());
                    ex.printStackTrace();
                    System.out.println("Exception into Sales Invoice Add.Charges edit:" + ex.getMessage());
                }
            }
        }
    }

    private void saveIntoSalesInvoiceDetailsEdit(JsonArray invoiceDetails,
                                                 TranxSalesReturnInvoice mSalesTranx,
                                                 Branch branch, Outlet outlet, Long id,
                                                 TransactionTypeMaster tranxType, String rowsDeleted) {

        List<TranxSalesReturnProductSrNo> newSerialNumbers = new ArrayList<>();
        for (int i = 0; i < invoiceDetails.size(); i++) {
            JsonObject object = invoiceDetails.get(i).getAsJsonObject();
            Long detailsId = object.get("details_id").getAsLong();
            TranxSalesReturnDetailsUnits invoiceUnits = new TranxSalesReturnDetailsUnits();
            if (detailsId != 0) {
                invoiceUnits = tranxSalesReturnDetailsUnitsRepository.findByIdAndStatus(detailsId, true);
            }
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            /* inserting into TranxSalesInvoiceDetailsUnits */
//            JsonArray productDetails = object.get("brandDetails").getAsJsonArray();
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
            Double tranxQty = 0.0;
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
            invoiceUnits.setSalesReturnInvoiceId(mSalesTranx.getId());
            invoiceUnits.setProduct(mProduct);
            invoiceUnits.setUnits(units);
            invoiceUnits.setQty(object.get("qty").getAsDouble());
            if (object.has("free_qty") &&
                    !object.get("free_qty").getAsString().equalsIgnoreCase("")) {
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
            TranxSalesInvoiceDetailsUnits mUnits = tranxSalesInvoiceDetailsUnitRepository.
                    findBySalesInvoiceIdAndStatusAndProductId(mSalesTranx.getTranxSalesInvoice().getId(),
                            true, mProduct.getId());
            Double total_qty = mUnits.getReturnQty() + object.get("qty").getAsDouble();
            if (mUnits.getQty().doubleValue() == total_qty.doubleValue()) {
                mUnits.setTransactionStatusId(2L);
            }
            mUnits.setReturnQty(total_qty);
            tranxSalesInvoiceDetailsUnitRepository.save(mUnits);
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
                TranxSalesReturnDetailsUnits tranxSalesReturnDetailsUnits = tranxSalesReturnDetailsUnitsRepository.save(invoiceUnits);
                if (flag == false) {
                    JsonArray jsonArray = object.getAsJsonArray("serialNo");
                    if (jsonArray != null && jsonArray.size() > 0) {
                        List<TranxSalesReturnProductSrNo> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            Long detailId = jsonSrno.get("serial_detail_id").getAsLong();
                            if (detailId == 0) {
                                TranxSalesReturnProductSrNo productSerialNumber = new TranxSalesReturnProductSrNo();
                                productSerialNumber.setProduct(mProduct);
                                productSerialNumber.setSerialNo(serialNo);
                                // productSerialNumber.setPurchaseTransaction(mPurchaseTranx);
                                productSerialNumber.setTransactionStatus("Purchase");
                                productSerialNumber.setStatus(true);
                                productSerialNumber.setCreatedBy(id);
                                productSerialNumber.setOperations("Inserted");
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setBranch(mSalesTranx.getBranch());
                                productSerialNumber.setOutlet(mSalesTranx.getOutlet());
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setUnits(units);
                                productSerialNumber.setTranxSalesReturnDetailsUnits(tranxSalesReturnDetailsUnits);
                                productSerialNumber.setLevelA(levelA);
                                productSerialNumber.setLevelB(levelB);
                                productSerialNumber.setLevelC(levelC);
                                productSerialNumber.setUnits(units);
                                TranxSalesReturnProductSrNo mSerialNo = tranxSalesReturnPrSrNoRepository.save(productSerialNumber);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToInventoryPostings("DR",
                                            mSalesTranx.getTransactionDate(), mSalesTranx.getId(),
                                            object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), serialNo);
                                }
                            } else {
                                TranxSalesReturnProductSrNo productSerialNumber1 = tranxSalesReturnPrSrNoRepository.findByIdAndStatus(detailsId, true);
                                productSerialNumber1.setSerialNo(serialNo);
                                productSerialNumber1.setOperations("Updated");
                                TranxSalesReturnProductSrNo mSerialNo = tranxSalesReturnPrSrNoRepository.save(productSerialNumber1);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToEditInventoryPostings(
                                            mSalesTranx.getTransactionDate(), mSalesTranx.getId(),
                                            object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear());
                                }
                            }
                        }
                    }
                    flag = true;
                }
            } catch (Exception e) {
                salesReturnLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            try {
                /*if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                    flag = true;
                }*/
                /**** Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    if (detailsId != 0) {
                        inventoryCommonPostings.callToEditInventoryPostings(
                                mSalesTranx.getTransactionDate(), mSalesTranx.getId(),
                                object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear());
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
                            if (mSalesTranx.getTransactionDate().compareTo(productRow.getTranxDate()) == 0 &&
                                    tranxQty != invoiceUnits.getQty()) { //DATE SAME AND QTY DIFFERENT
                                Double closingStk = closingUtility.CAL_CR_STOCK(productRow.getOpeningStock(), tranxQty, free_qty);
                                productRow.setQty(tranxQty + free_qty);
                                productRow.setClosingStock(closingStk);
                                InventorySummaryTransactionDetails mInventory =
                                        stkTranxDetailsRepository.save(productRow);
                                closingUtility.updatePosting(mInventory, mProduct.getId(), mSalesTranx.getTransactionDate());
                            } else if (mSalesTranx.getTransactionDate().compareTo(productRow.getTranxDate()) != 0) { // DATE IS DIFFERENT
                                Date oldDate = productRow.getTranxDate();
                                Date newDate = mSalesTranx.getTransactionDate();
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
                                        mSalesTranx.getTranxCode(), id, "IN", mProduct.getPackingMaster().getId());

                                closingUtility.stockPostingBatchWise(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId,
                                        mProduct, tranxType.getId(), newDate, invoiceUnits.getQty(), free_qty,
                                        mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                        mSalesTranx.getTranxCode(), id, "IN", mProduct.getPackingMaster().getId());
                            }
                        }
                    } else {
                        inventoryCommonPostings.callToInventoryPostings("CR",
                                mSalesTranx.getTransactionDate(), mSalesTranx.getId(),
                                object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), serialNo);
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
        /**** if product is deleted from details table from front end, when user edit the sales *****/
        Long purchaseInvoiceId = null;
        HashSet<Long> purchaseDetailsId = new HashSet<>();
        if (!rowsDeleted.isEmpty()) {
            JsonParser parser = new JsonParser();
            JsonElement salesDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = salesDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxSalesReturnDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    mDeletedInvoices = tranxSalesReturnDetailsUnitsRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                    if (mDeletedInvoices != null) {
                        mDeletedInvoices.setStatus(false);
                        LevelA levelA = null;
                        LevelB levelB = null;
                        LevelC levelC = null;
                        levelA = levelARepository.findByIdAndStatus(mDeletedInvoices.getLevelAId(), true);
                        levelB = levelBRepository.findByIdAndStatus(mDeletedInvoices.getLevelBId(), true);
                        levelC = levelCRepository.findByIdAndStatus(mDeletedInvoices.getLevelCId(), true);
                        TranxSalesReturnInvoice returnInvoice = tranxSalesReturnRepository.findByIdAndStatus(
                                mDeletedInvoices.getSalesReturnInvoiceId(), true);
                        tranxSalesReturnDetailsUnitsRepository.save(mDeletedInvoices);
                        inventoryCommonPostings.callToInventoryPostings("CR",
                                returnInvoice.getTransactionDate(),
                                mDeletedInvoices.getSalesReturnInvoiceId(),
                                mDeletedInvoices.getQty() + mDeletedInvoices.getFreeQty(), branch, outlet, mDeletedInvoices.getProduct(),
                                tranxType, levelA, levelB, levelC, mDeletedInvoices.getUnits(),
                                mDeletedInvoices.getProductBatchNo(), mDeletedInvoices.getProductBatchNo().getBatchNo(),
                                returnInvoice.getFiscalYear(), null);
                    }
                }
            }
        }
    }

    private void saveIntoAdditionalChargesEdit(JsonArray additionalCharges, TranxSalesReturnInvoice mSalesTranx,
                                               Long outletId, TransactionTypeMaster tranxType) {
        List<TranxSalesReturnInvoiceAddCharges> chargesList = new ArrayList<>();
        for (JsonElement mAddCharges : additionalCharges) {
            JsonObject object = mAddCharges.getAsJsonObject();
            Double amount = object.get("amt").getAsDouble();
            Long ledgerId = object.get("ledger").getAsLong();
            Long detailsId = object.get("additional_charges_details_id").getAsLong();
            LedgerMaster addcharges = null;
            TranxSalesReturnInvoiceAddCharges charges = null;
            if (detailsId != 0) {
                charges = tranxSalesReturnAddiChargesRepository.findByIdAndStatus(detailsId, true);
                charges.setOperation("updated");
            } else {
                charges = new TranxSalesReturnInvoiceAddCharges();
                charges.setOperation("inserted");
            }
            addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
            charges.setAmount(amount);
            charges.setAdditionalCharges(addcharges);
            charges.setTranxSalesReturnInvoice(mSalesTranx);
            charges.setStatus(true);
            chargesList.add(charges);
            Boolean isContains = dbList.contains(addcharges.getId());
            mInputList.add(addcharges.getId());
            if (isContains) {
                /**** New Postings Logic *****/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                        findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(addcharges.getId(),
                                tranxType.getId(), mSalesTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(amount);
                    mLedger.setTransactionDate(mSalesTranx.getTransactionDate());
                    mLedger.setOperations("Updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
/**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(amount, addcharges, tranxType, addcharges.getAssociateGroups(),
                        mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(),
                        mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(),
                        "DR", true, tranxType.getTransactionName(), "Insert");
            }
        }
        try {
            tranxSalesReturnAddiChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            salesReturnLogger.error("Error in saveIntoPurchaseAdditionalChargesEdit" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            salesReturnLogger.error("Error in saveIntoPurchaseAdditionalChargesEdit" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveInoDutiesAndTaxesEdit(JsonObject duties_taxes, TranxSalesReturnInvoice invoiceTranx, Boolean taxFlag, Long outletId) {
        List<TranxSalesReturnInvoiceDutiesTaxes> salesDutiesTaxes = new ArrayList<>();
        List<Long> db_dutiesLedgerIds = tranxSalesReturnTaxesRepository.findByDutiesAndTaxesId(invoiceTranx.getId());
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null);
        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSRT");
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            //Updation into Purchase challan Duties and Taxes
            if (db_dutiesLedgerIds.size() > 0) {
                //insert old records in history
                salesDutiesTaxes = tranxSalesReturnTaxesRepository.findByTranxSalesReturnInvoiceAndStatus(invoiceTranx, true);
                //  insertIntoDutiesAndTaxesHistory(salesDutiesTaxes);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;
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
                                    mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                                    mLedger.setOperations("Updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType,
                                    "DR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getTransactionDate(),
                                    invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());
                        }
                        taxes.setAmount(amt);
                        taxes.setStatus(true);
                        taxes.setTranxSalesReturnInvoice(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) sgstObject.get("gst").getAsDouble();
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
                                    mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                                    mLedger.setOperations("Updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType,
                                    "DR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getTransactionDate(),
                                    invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxSalesReturnInvoice(invoiceTranx);
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
                        TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
                        JsonObject igstObject = mIgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        //     dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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
                                    mLedger.setTransactionDate(invoiceTranx.getTransactionDate());
                                    mLedger.setOperations("Updated");
                                    ledgerTransactionPostingsRepository.save(mLedger);
                                }
                            } else {
                                /**** New Postings Logic *****/
                                ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Insert");
                            }

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amt, dutiesTaxes.getId(), tranxType,
                                    "DR", invoiceTranx.getId(), dutiesTaxes, invoiceTranx.getTransactionDate(),
                                    invoiceTranx.getFiscalYear(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                    invoiceTranx.getTranxCode());
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxSalesReturnInvoice(invoiceTranx);
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
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;
                        Double amt = cgstObject.get("amt").getAsDouble();
                        //dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxSalesReturnInvoice(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Insert");


                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.callToPostingLedger(tranxType, "DR", amt, invoiceTranx.getFiscalYear(),
                                dutiesTaxes, invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                invoiceTranx.getTranxCode());
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
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
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxSalesReturnInvoice(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Insert");

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.callToPostingLedger(tranxType, "DR", amt, invoiceTranx.getFiscalYear(),
                                dutiesTaxes, invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                invoiceTranx.getTranxCode());
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
                        JsonObject igstObject = igstList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        // dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxSalesReturnInvoice(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(amt, dutiesTaxes, tranxType, dutiesTaxes.getAssociateGroups(), invoiceTranx.getFiscalYear(), invoiceTranx.getBranch(), invoiceTranx.getOutlet(), invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getSalesReturnNo(), "DR", true, tranxType.getTransactionName(), "Insert");

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.callToPostingLedger(tranxType, "DR", amt, invoiceTranx.getFiscalYear(),
                                dutiesTaxes, invoiceTranx.getTransactionDate(), invoiceTranx.getId(), invoiceTranx.getOutlet(), invoiceTranx.getBranch(),
                                invoiceTranx.getTranxCode());
                    }
                }
            }
        }
        tranxSalesReturnTaxesRepository.saveAll(salesDutiesTaxes);
    }

    private List<Long> getInputLedgerIds(Boolean taxFlag, JsonObject duties_taxes, Long outletId, Long branchId) {
        List<Long> returnLedgerIds = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (JsonElement mCgst : cgstList) {
                    TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
                    JsonObject cgstObject = mCgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //   int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT CGST " + inputGst;
                    //        dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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
                    TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
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
                    //   int inputGst = (int) igstObject.get("gst").getAsDouble();
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

    /****** Save into Sales Returns Duties and Taxes ******/
    private void saveInoDutiesAndTaxes(JsonObject duties_taxes, TranxSalesReturnInvoice mSalesTranx, Boolean taxFlag) {
        List<TranxSalesReturnInvoiceDutiesTaxes> salesDutiesTaxes = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (int i = 0; i < cgstList.size(); i++) {
                    TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
                    JsonObject cgstObject = cgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT CGST " + inputGst;
                    // dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, mSalesTranx.getOutlet().getId());
                    if (mSalesTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                        taxes.setDutiesTaxes(dutiesTaxes);
                    }
                    taxes.setAmount(cgstObject.get("amt").getAsDouble());
                    taxes.setTranxSalesReturnInvoice(mSalesTranx);
                    //taxes.setTranxSalesInvoice(mSalesTranx.getTranxSalesInvoice());
                    taxes.setSundryDebtors(mSalesTranx.getSundryDebtors());
                    taxes.setIntra(taxFlag);
                    taxes.setStatus(true);
                    taxes.setCreatedBy(mSalesTranx.getCreatedBy());
                    salesDutiesTaxes.add(taxes);
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (int i = 0; i < sgstList.size(); i++) {
                    TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
                    JsonObject sgstObject = sgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //   int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT SGST " + inputGst;
                    // dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, mSalesTranx.getOutlet().getId());
                    if (mSalesTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        taxes.setDutiesTaxes(dutiesTaxes);
                    }
                    taxes.setAmount(sgstObject.get("amt").getAsDouble());
                    taxes.setTranxSalesReturnInvoice(mSalesTranx);
                    //   taxes.setTranxSalesInvoice(mSalesTranx.getTranxSalesInvoice());
                    taxes.setSundryDebtors(mSalesTranx.getSundryDebtors());
                    taxes.setIntra(taxFlag);
                    taxes.setStatus(true);
                    taxes.setCreatedBy(mSalesTranx.getCreatedBy());
                    salesDutiesTaxes.add(taxes);
                }
            }
        } else {
            JsonArray igstList = duties_taxes.getAsJsonArray("igst");
            /* this is for Igst creation */
            if (igstList.size() > 0) {
                for (int i = 0; i < igstList.size(); i++) {
                    TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();
                    JsonObject igstObject = igstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String inputGst = igstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT IGST " + inputGst;
                    //dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, mSalesTranx.getOutlet().getId());
                    if (mSalesTranx.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesTranx.getOutlet().getId(), mSalesTranx.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesTranx.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        taxes.setDutiesTaxes(dutiesTaxes);
                    }
                    taxes.setAmount(igstObject.get("amt").getAsDouble());
                    taxes.setTranxSalesReturnInvoice(mSalesTranx);
                    //   taxes.setTranxSalesInvoice(mSalesTranx.getTranxSalesInvoice());
                    taxes.setSundryDebtors(mSalesTranx.getSundryDebtors());
                    taxes.setIntra(taxFlag);
                    taxes.setStatus(true);
                    taxes.setCreatedBy(mSalesTranx.getCreatedBy());
                    salesDutiesTaxes.add(taxes);
                }
            }
        }
        try {
            /* save all Duties and Taxes into Sales Invoice Duties taxes table */
            tranxSalesReturnTaxesRepository.saveAll(salesDutiesTaxes);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesReturnLogger.error("Exception in saveInoDutiesAndTaxes:" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            salesReturnLogger.error("Exception in saveInoDutiesAndTaxes:" + e1.getMessage());
            System.out.println(e1.getMessage());
        }
    }/* End of  Sales return Duties and Taxes Ledger */


    public void setCloseSC(String poIds) {
        Boolean flag = false;
        String idList[];
        idList = poIds.split(",");
        for (String mId : idList) {
            TranxSalesChallan tranxSalesChallan = tranxSalesChallanRepository.findByIdAndStatus(Long.parseLong(mId), true);
            if (tranxSalesChallan != null) {
                tranxSalesChallan.setStatus(false);
                tranxSalesChallanRepository.save(tranxSalesChallan);
            }
        }
    }

    /****** Save into Sales AdditionalCharges ******/
    public void saveIntoAdditionalCharges(JsonArray additionalCharges, TranxSalesReturnInvoice mSalesTranx) {
        List<TranxSalesReturnInvoiceAddCharges> chargesList = new ArrayList<>();
        if (mSalesTranx.getAdditionalChargesTotal() > 0) {
            for (int j = 0; j < additionalCharges.size(); j++) {
                TranxSalesReturnInvoiceAddCharges charges = new TranxSalesReturnInvoiceAddCharges();
                JsonObject object = additionalCharges.get(j).getAsJsonObject();
                Double amount = object.get("amt").getAsDouble();
//                Long ledgerId = object.get("ledger").getAsLong();
                Long ledgerId = object.get("ledger_id").getAsJsonObject().get("value").getAsLong();
                LedgerMaster addcharges = ledgerMasterRepository.findByIdAndStatus(ledgerId, true);
                charges.setAmount(amount);
                charges.setAdditionalCharges(addcharges);
                charges.setTranxSalesReturnInvoice(mSalesTranx);
                // charges.setSalesTransaction(mSalesTranx.getTranxSalesInvoice());
                charges.setStatus(true);
                charges.setCreatedBy(mSalesTranx.getCreatedBy());
                chargesList.add(charges);
            }
        }
        try {
            tranxSalesReturnAddiChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException de) {
            salesReturnLogger.error("Exception in saveIntoAdditionalCharges:" + de.getMessage());
            System.out.println(de.getMessage());
            de.printStackTrace();
        } catch (Exception e1) {
            salesReturnLogger.error("Exception in saveIntoAdditionalCharges:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }
    }/* End of Sales Return Additional Charges */

    /****** Save into Sales Invoice Details ******/
    public void saveIntoSalesInvoiceDetails(JsonArray invoiceDetails, TranxSalesReturnInvoice mSalesTranx, Branch branch, Outlet outlet, Long userId, TransactionTypeMaster tranxType) {
        /* Purchase Product Details Start here */
        TransactionStatus status = transactionStatusRepository.findByIdAndStatus(2L, true);
        List<TranxSalesReturnProductSrNo> newSerialNumbers = new ArrayList<>();
        for (int i = 0; i < invoiceDetails.size(); i++) {
            JsonObject object = invoiceDetails.get(i).getAsJsonObject();
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            /* inserting into TranxSalesInvoiceDetailsUnits */
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
            }
            if (!object.get("levelbId").getAsString().equalsIgnoreCase("")) {
                levelB = levelBRepository.findByIdAndStatus(object.get("levelbId").getAsLong(), true);
            }
            if (!object.get("levelcId").getAsString().equalsIgnoreCase("")) {
                levelC = levelCRepository.findByIdAndStatus(object.get("levelcId").getAsLong(), true);
            }

            Units units = unitsRepository.findByIdAndStatus(object.get("unitId").getAsLong(), true);
            TranxSalesReturnDetailsUnits invoiceUnits = new TranxSalesReturnDetailsUnits();
            invoiceUnits.setSalesReturnInvoiceId(mSalesTranx.getId());
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
            invoiceUnits.setStatus(true);
            if (levelA != null) invoiceUnits.setLevelAId(levelA.getId());
            if (levelB != null) invoiceUnits.setLevelBId(levelB.getId());
            if (levelC != null) invoiceUnits.setLevelCId(levelC.getId());
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
            TranxSalesInvoiceDetailsUnits mUnits = tranxSalesInvoiceDetailsUnitRepository.
                    findBySalesInvoiceIdAndStatusAndProductId(mSalesTranx.getTranxSalesInvoice().getId(),
                            true, mProduct.getId());
            Double total_qty = mUnits.getReturnQty() + object.get("qty").getAsDouble();
            if (mUnits.getQty() == total_qty) {
                mUnits.setTransactionStatusId(status.getId());
            }
            mUnits.setReturnQty(total_qty);
            tranxSalesInvoiceDetailsUnitRepository.save(mUnits);
            /******* Insert into Product Batch No ****/
            Boolean flag = false;
            try {
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    /*int qty = object.get("qty").getAsInt();
                    int free_qty = 0;
                    if (!object.get("free_qty").getAsString().equalsIgnoreCase(""))
                        free_qty = object.get("free_qty").getAsInt();
                    double net_amt = object.get("final_amt").getAsDouble();
                    double costing = 0;
                    double costing_with_tax = 0;
                    if (outlet.getGstTypeMaster().getId() == 1L) { //Registered
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
                        if (object.has("b_rate")) mproductBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        if (object.has("b_sale_rate"))
                            mproductBatchNo.setSalesRate(object.get("b_sale_rate").getAsDouble());
                        if (object.has("b_purchase_rate"))
                            mproductBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        mproductBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
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
                        if (object.has("b_no")) productBatchNo.setBatchNo(object.get("b_no").getAsString());
                        if (object.has("b_rate")) productBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        if (object.has("b_sale_rate"))
                            productBatchNo.setSalesRate(object.get("b_sale_rate").getAsDouble());
                        if (object.has("b_purchase_rate"))
                            productBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase(""))
                            productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                        productBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        productBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
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
                TranxSalesReturnDetailsUnits tranxSalesInvoiceDetailsUnits = tranxSalesReturnDetailsUnitsRepository.save(invoiceUnits);
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
                        List<TranxSalesReturnProductSrNo> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            TranxSalesReturnProductSrNo productSerialNumber = new TranxSalesReturnProductSrNo();
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
                            productSerialNumber.setTranxSalesReturnDetailsUnits(tranxSalesInvoiceDetailsUnits);
                            productSerialNumber.setLevelA(levelA);
                            productSerialNumber.setLevelB(levelB);
                            productSerialNumber.setLevelC(levelC);
                            productSerialNumber.setUnits(units);
                            TranxSalesReturnProductSrNo mSerialNo = tranxSalesReturnPrSrNoRepository.save(productSerialNumber);
                            if (mProduct.getIsInventory()) {
                                inventoryCommonPostings.callToInventoryPostings("DR",
                                        mSalesTranx.getTransactionDate(), mSalesTranx.getId(),
                                        object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), serialNo);
                            }
                        }
                    }
                    flag = true;
                }
            } catch (Exception e) {
                salesReturnLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
            }
            /******* End of insert into Product Batch No ****/
            try {
                /*if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                    flag = true;
                }*/
                /**** Inventory Postings *****/
                if (mProduct.getIsInventory() && flag) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("CR",
                            mSalesTranx.getTransactionDate(), mSalesTranx.getId(),
                            object.get("qty").getAsDouble() + free_qty, branch,
                            outlet, mProduct, tranxType, levelA, levelB, levelC,
                            units, productBatchNo, batchNo, mSalesTranx.getFiscalYear(), null);
                    /***** End of new architecture of Inventory Postings *****/

                    /**
                     * @implNote New Logic of opening and closing Inventory posting
                     * @auther ashwins@opethic.com
                     * @version sprint 1
                     **/
                    closingUtility.stockPosting(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId,
                            mProduct, tranxType.getId(), mSalesTranx.getTransactionDate(), tranxQty, free_qty,
                            mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                            mSalesTranx.getTranxCode(), userId, "IN", mProduct.getPackingMaster().getId());

                    closingUtility.stockPostingBatchWise(outlet, branch, mSalesTranx.getFiscalYear().getId(), batchId,
                            mProduct, tranxType.getId(), mSalesTranx.getTransactionDate(), tranxQty, free_qty,
                            mSalesTranx.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                            mSalesTranx.getTranxCode(), userId, "IN", mProduct.getPackingMaster().getId());
                    /***** End of new logic of Inventory Postings *****/
                }
            } catch (Exception e) {
                System.out.println("Exception in Postings of Inventory:" + e.getMessage());
            }
            /* End of inserting into TranxSalesInvoiceDetailsUnits */
        }
    }

    public JsonObject salesReturnsLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxSalesReturnRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxSalesReturnRepository.findLastRecord(users.getOutlet().getId());
        }

        String serailNo = String.format("%05d", count + 1);// 5 digit serial number

        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);

        String csCode = "SR" + currentMonth + serailNo;

        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("salesReturnNo", csCode);
        return result;
    }

    /* Posting into Sundry Debtors */
    private void insertIntoTranxDetailSD(TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        try {

            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getTotalAmount(), mSalesTranx.getSundryDebtors(), tranxType, mSalesTranx.getSundryDebtors().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), crdrType, true, "Sales Return", operation);


            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, crdrType, mSalesTranx.getTotalAmount(), mSalesTranx.getFiscalYear(),
                        mSalesTranx.getSundryDebtors(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getOutlet(),
                        mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
            }

            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mSalesTranx.getSundryDebtors().getId(), tranxType.getId(), mSalesTranx.getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mSalesTranx.getSundryDebtors(), mSalesTranx.getTransactionDate(),
                            tranxType, mSalesTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            salesReturnLogger.error("Exception in insertIntoTranxDetailSD:" + e.getMessage());
            System.out.println("Store Procedure Error " + e.getMessage());
        }
    }/* End of Posting into Sundry Debtors */

    /* Posting into Sales Accounts */
    private void insertIntoTranxDetailSA(TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        try {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getTotalBaseAmount(), mSalesTranx.getSalesAccountLedger(), tranxType, mSalesTranx.getSalesAccountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), crdrType, true, "Sales Return", operation);

            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, crdrType, mSalesTranx.getTotalBaseAmount(),
                        mSalesTranx.getFiscalYear(), mSalesTranx.getSalesAccountLedger(), mSalesTranx.getTransactionDate(),
                        mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
            }

            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mSalesTranx.getSalesAccountLedger().getId(), tranxType.getId(), mSalesTranx.getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mSalesTranx.getSalesAccountLedger(), mSalesTranx.getTransactionDate(),
                            tranxType, mSalesTranx.getFiscalYear(), detail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            salesReturnLogger.error("Exception in insertIntoTranxDetailSA:" + e.getMessage());
        }
    }/* End of Posting into Sales Accounts */

    /* Posting into Sales Discount */
    private void insertIntoTranxDetailsSalesDiscount(TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        try {
            if (mSalesTranx.getSalesDiscountLedger() != null) {
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(mSalesTranx.getTotalSalesDiscountAmt(), mSalesTranx.getSalesDiscountLedger(), tranxType, mSalesTranx.getSalesDiscountLedger().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), crdrType, true, "Sales Return", operation);
            }


            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                if (mSalesTranx.getSalesDiscountLedger() != null) {
                    postingUtility.callToPostingLedger(tranxType, crdrType, mSalesTranx.getTotalSalesDiscountAmt(),
                            mSalesTranx.getFiscalYear(), mSalesTranx.getSalesDiscountLedger(), mSalesTranx.getTransactionDate(),
                            mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
                }
            }

            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                if (mSalesTranx.getSalesDiscountLedger() != null) {
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mSalesTranx.getSalesDiscountLedger().getId(), tranxType.getId(), mSalesTranx.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(mSalesTranx.getSalesDiscountLedger(), mSalesTranx.getTransactionDate(),
                                tranxType, mSalesTranx.getFiscalYear(), detail);
                    }
                }
            }
        } catch (Exception e) {
            salesReturnLogger.error("Exception in insertIntoTranxDetailsSalesDiscount:" + e.getMessage());
            System.out.println("Posting Discount Exception:" + e.getMessage());
            e.printStackTrace();

        }
    }/* End of Posting into Sales Discount */

    /*  Posting into Sales Round off */
    private void insertIntoTranxDetailRO(TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        String tranxAction = "CR";
        if (mSalesTranx.getRoundOff() >= 0) {
            tranxAction = "DR";
            //     ledgerTransactionDetailsRepository.insertIntoLegerTranxDetailsPosting(mSalesTranx.getSalesRoundOff().getPrinciples().getFoundations().getId(), mSalesTranx.getSalesRoundOff().getPrinciples().getId(), mSalesTranx.getSalesRoundOff().getPrincipleGroups() != null ? mSalesTranx.getSalesRoundOff().getPrincipleGroups().getId() : null, mSalesTranx.getAssociateGroups() != null ? mSalesTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, mSalesTranx.getBranch() != null ? mSalesTranx.getBranch().getId() : null, mSalesTranx.getOutlet().getId(), "pending", mSalesTranx.getRoundOff() * -1, 0.0, mSalesTranx.getTransactionDate(), null, mSalesTranx.getId(), tranxType.getTransactionName(), mSalesTranx.getSalesRoundOff().getUnderPrefix(), mSalesTranx.getFinancialYear(), mSalesTranx.getCreatedBy(), mSalesTranx.getSalesRoundOff().getId(), mSalesTranx.getSalesReturnNo());
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "DR", true, "Sales Return", "Insert");
        } else if (mSalesTranx.getRoundOff() < 0) {
            // ledgerTransactionDetailsRepository.insertIntoLegerTranxDetailsPosting(mSalesTranx.getSalesRoundOff().getPrinciples().getFoundations().getId(), mSalesTranx.getSalesRoundOff().getPrinciples().getId(), mSalesTranx.getSalesRoundOff().getPrincipleGroups() != null ? mSalesTranx.getSalesRoundOff().getPrincipleGroups().getId() : null, mSalesTranx.getAssociateGroups() != null ? mSalesTranx.getAssociateGroups().getId() : null, tranxType.getId(), null, mSalesTranx.getBranch() != null ? mSalesTranx.getBranch().getId() : null, mSalesTranx.getOutlet().getId(), "pending", 0.0, Math.abs(mSalesTranx.getRoundOff()), mSalesTranx.getTransactionDate(), null, mSalesTranx.getId(), tranxType.getTransactionName(), mSalesTranx.getSalesRoundOff().getUnderPrefix(), mSalesTranx.getFinancialYear(), mSalesTranx.getCreatedBy(), mSalesTranx.getSalesRoundOff().getId(), mSalesTranx.getSalesReturnNo());
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "CR", true, "Sales Return", "Insert");
        }

        /**** NEW METHOD FOR LEDGER POSTING ****/
        postingUtility.callToPostingLedger(tranxType, tranxAction, Math.abs(mSalesTranx.getRoundOff()),
                mSalesTranx.getFiscalYear(), mSalesTranx.getSalesRoundOff(), mSalesTranx.getTransactionDate(),
                mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
    }/* End Posting into Sales Round off */

    /* Posting into Sales duties and taxes off */
    public void insertDB(TranxSalesReturnInvoice mSalesTranx, String ledgerName, TransactionTypeMaster tranxType, String crdrType, String operation) {
        try {
            /* Sale Duties Taxes */
            if (ledgerName.equalsIgnoreCase("DT")) {
                List<TranxSalesReturnInvoiceDutiesTaxes> list = tranxSalesReturnTaxesRepository.findByTranxSalesReturnInvoiceIdAndStatus(mSalesTranx.getId(), true);
                for (TranxSalesReturnInvoiceDutiesTaxes mDuties : list) {
                    insertFromDutiesTaxes(mDuties, mSalesTranx, tranxType, crdrType, operation);
                }
            } else if (ledgerName.equalsIgnoreCase("AC")) {
                /* Sale Additional Charges */
                List<TranxSalesReturnInvoiceAddCharges> list = new ArrayList<>();
                list = tranxSalesReturnAddiChargesRepository.findByTranxSalesReturnInvoiceIdAndStatus(mSalesTranx.getId(), true);
                if (list.size() > 0) {
                    for (TranxSalesReturnInvoiceAddCharges mAdditinoalCharges : list) {
                        insertFromAdditionalCharges(mAdditinoalCharges, mSalesTranx, tranxType, crdrType, operation);
                    }
                }
            }
        } catch (DataIntegrityViolationException e1) {
            salesReturnLogger.error("Exception in insertDB:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            salesReturnLogger.error("Exception in insertDB:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    } /* Posting into Sales duties and taxes off */

    private void insertFromDutiesTaxes(TranxSalesReturnInvoiceDutiesTaxes mDuties, TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        if (mSalesTranx.getSalesDiscountLedger() != null) {
            /**** New Postings Logic *****/
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mDuties.getAmount(), mDuties.getDutiesTaxes(), tranxType, mDuties.getDutiesTaxes().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), crdrType, true, "Sales Return", operation);
        }

        if (operation.equalsIgnoreCase("insert")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedger(tranxType, crdrType, mDuties.getAmount(),
                    mSalesTranx.getFiscalYear(), mDuties.getDutiesTaxes(), mSalesTranx.getTransactionDate(),
                    mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
        }

        if (operation.equalsIgnoreCase("delete")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                    mDuties.getDutiesTaxes().getId(), tranxType.getId(), mSalesTranx.getId(), true);
            if (ledgerDetail != null) {
                Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                ledgerDetail.setAmount(0.0);
                ledgerDetail.setClosingAmount(closing);
                ledgerDetail.setStatus(false);
                LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.updateLedgerPostings(mDuties.getDutiesTaxes(), mSalesTranx.getTransactionDate(),
                        tranxType, mSalesTranx.getFiscalYear(), detail);
            }
        }
    }

    private void insertFromAdditionalCharges(TranxSalesReturnInvoiceAddCharges mAdditinoalCharges, TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType, String crdrType, String operation) {
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(mAdditinoalCharges.getAmount(), mAdditinoalCharges.getAdditionalCharges(), tranxType, mAdditinoalCharges.getAdditionalCharges().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), crdrType, true, "Sales Return", operation);


        if (operation.equalsIgnoreCase("insert")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedger(tranxType, crdrType, mAdditinoalCharges.getAmount(),
                    mSalesTranx.getFiscalYear(), mAdditinoalCharges.getAdditionalCharges(), mSalesTranx.getTransactionDate(),
                    mSalesTranx.getId(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(), mSalesTranx.getTranxCode());
        }

        if (operation.equalsIgnoreCase("delete")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                    mAdditinoalCharges.getAdditionalCharges().getId(), tranxType.getId(), mSalesTranx.getId(), true);
            if (ledgerDetail != null) {
                Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                ledgerDetail.setAmount(0.0);
                ledgerDetail.setClosingAmount(closing);
                ledgerDetail.setStatus(false);
                LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.updateLedgerPostings(mAdditinoalCharges.getAdditionalCharges(), mSalesTranx.getTransactionDate(),
                        tranxType, mSalesTranx.getFiscalYear(), detail);
            }
        }
    }

    /*** creating new reference while adjusting return amount into next sales invoice bill ***/
    private void insertIntoNewReference(TranxSalesReturnInvoice mSalesTranx, HttpServletRequest request, String key) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReference = new TranxCreditNoteNewReferenceMaster();
        Map<String, String[]> paramMap = request.getParameterMap();
        if (key.equalsIgnoreCase("create")) {
            tranxCreditNoteNewReference.setBranchId(mSalesTranx.getBranch() != null ? mSalesTranx.getBranch().getId() : null);
            tranxCreditNoteNewReference.setOutletId(mSalesTranx.getOutlet().getId());
            tranxCreditNoteNewReference.setSundryDebtorsId(mSalesTranx.getSundryDebtors().getId());
            tranxCreditNoteNewReference.setStatus(true);
            tranxCreditNoteNewReference.setCreatedBy(mSalesTranx.getCreatedBy());
            tranxCreditNoteNewReference.setTransactionStatusId(transactionStatus.getId());
            //SQDEC00001
            Long count = tranxCreditNoteNewReferenceRepository.findLastRecord(users.getOutlet().getId());
            String serailNo = String.format("%05d", count + 1);// 5 digit serial number
            //first 3 digits of Current month
            GenerateDates generateDates = new GenerateDates();
            String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
            String dbtnCode = "CRDTN" + currentMonth + serailNo;
            tranxCreditNoteNewReference.setCreditnoteNewReferenceNo(dbtnCode);
        } else {
            tranxCreditNoteNewReference = tranxCreditNoteNewReferenceRepository.findByIdAndStatus(mSalesTranx.getId(), true);
        }
        /* this parameter segregates whether credit note is from sales invoice
        or sales challan*/
        tranxCreditNoteNewReference.setSource(request.getParameter("source"));
        if (request.getParameter("source").equalsIgnoreCase("sales_invoice")) {

            tranxCreditNoteNewReference.setSalesInvoiceId(mSalesTranx.getTranxSalesInvoice().getId());
        } else {
            tranxCreditNoteNewReference.setSalesChallanId(mSalesTranx.getTranxSalesChallan().getId());
        }
        tranxCreditNoteNewReference.setTranxSalesReturnInvoiceId(mSalesTranx.getId());

        tranxCreditNoteNewReference.setAdjustmentStatus(request.getParameter("paymentMode"));
        tranxCreditNoteNewReference.setRoundOff(mSalesTranx.getRoundOff());
        tranxCreditNoteNewReference.setTotalBaseAmount(mSalesTranx.getTotalBaseAmount());
        tranxCreditNoteNewReference.setTotalAmount(mSalesTranx.getTotalAmount());
        tranxCreditNoteNewReference.setTaxableAmount(mSalesTranx.getTaxableAmount());
        tranxCreditNoteNewReference.setTotalgst(mSalesTranx.getTotaligst());
        tranxCreditNoteNewReference.setSalesDiscountAmount(mSalesTranx.getSalesDiscountAmount());
        tranxCreditNoteNewReference.setSalesDiscountPer(mSalesTranx.getSalesDiscountPer());
        tranxCreditNoteNewReference.setTotalSalesDiscountAmt(mSalesTranx.getTotalSalesDiscountAmt());
        tranxCreditNoteNewReference.setAdditionalChargesTotal(mSalesTranx.getAdditionalChargesTotal());
        tranxCreditNoteNewReference.setFinancialYear(mSalesTranx.getFinancialYear());
        tranxCreditNoteNewReference.setBalance(mSalesTranx.getTotalAmount());
        tranxCreditNoteNewReference.setTranscationDate(mSalesTranx.getTransactionDate());
        tranxCreditNoteNewReference.setFiscalYearId(mSalesTranx.getFiscalYear().getId());
        try {
            TranxCreditNoteNewReferenceMaster newCreditNote = tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteNewReference);
            TranxCreditNoteDetails mDetails = null;
            if (key.equalsIgnoreCase("create")) {
                mDetails = new TranxCreditNoteDetails();
                mDetails.setStatus(true);
                mDetails.setTransactionStatusId(transactionStatus.getId());
                Branch branch = branchRepository.findByIdAndStatus(newCreditNote.getId(), true);
                if (branch != null)
                    mDetails.setBranchId(branch.getId());
                Outlet mOutlet = outletRepository.findByIdAndStatus(newCreditNote.getOutletId(), true);
                mDetails.setOutletId(mOutlet.getId());
            } else {
                mDetails = tranxCreditNoteDetailsRepository.findByStatusAndTranxCreditNoteMasterId(true, newCreditNote.getId());
            }
            mDetails.setSundryDebtorsId(newCreditNote.getSundryDebtorsId());
            mDetails.setTotalAmount(newCreditNote.getTotalAmount());
            mDetails.setPaidAmt(newCreditNote.getTotalAmount());
            mDetails.setAdjustedId(newCreditNote.getSalesInvoiceId());
            mDetails.setAdjustedSource("sales_invoice");
            mDetails.setBalance(0.0);
            mDetails.setOperations("adjust");
            mDetails.setTranxCreditNoteMasterId(newCreditNote.getId());
            mDetails.setCreatedBy(newCreditNote.getCreatedBy());
            mDetails.setAdjustmentStatus(newCreditNote.getAdjustmentStatus());
            // immediate
            // newCreditNote.setBalance(0.0);
            tranxCreditNoteDetailsRepository.save(mDetails);

        } catch (Exception e) {
            e.printStackTrace();
            salesReturnLogger.error("Exception in insertIntoNewReference:" + e.getMessage());
        }
    }


    /* Postings of Ledgers while purchase returns*/
    private void insertIntoLedgerTranxDetails(TranxSalesReturnInvoice mSalesTranx) {
        /* start of ledger trasaction details */

        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSRT");
//        generateTransactions.insertIntoTranxsDetails(mPurchaseTranx,tranxType);
        try {
            insertIntoTranxDetailSD(mSalesTranx, tranxType, "CR", "Insert"); // for Sundry Debtors : CR
            insertIntoTranxDetailSA(mSalesTranx, tranxType, "DR", "Insert"); // for Sales Accounts : DR
            insertIntoTranxDetailsSalesDiscount(mSalesTranx, tranxType, "CR", "Insert"); // for Sales Discounts : CR
            insertIntoTranxDetailRO(mSalesTranx, tranxType); // for Round Off : cr or dr
            insertDB(mSalesTranx, "AC", tranxType, "DR", "Insert"); // for Additional Charges : DR
            insertDB(mSalesTranx, "DT", tranxType, "DR", "Insert"); // for Duties and Taxes : DR
            /* end of ledger transaction details */
        } catch (Exception e) {
            salesReturnLogger.error("Exception in insertIntoLedgerTranxDetails:" + e.getMessage());
            System.out.println("Posting Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /* find all Sales Invoices and Purchase Challans of Sundry Creditors/Suppliers wise , for Purchase Returns */
    public JsonObject salesListSupplierWise(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonArray result = new JsonArray();
        List<TranxSalesInvoice> salesInvoice = new ArrayList<>();
        List<TranxSalesChallan> salesChallans = new ArrayList<>();
        Long ledgerId = Long.parseLong(request.getParameter("sundry_debtors_id"));
        JsonObject output = new JsonObject();
        if (paramMap.containsKey("dateFrom") && paramMap.containsKey("dateTo")) {
            if (users.getBranch() != null) {
                salesInvoice = tranxSalesInvoiceRepository.findByBranchSuppliersWithDates(users.getOutlet().getId(),
                        true, ledgerId, request.getParameter("dateFrom"), request.getParameter("dateTo"),
                        users.getBranch().getId());
            } else {
                salesInvoice = tranxSalesInvoiceRepository.findBySuppliersWithDates(
                        users.getOutlet().getId(), true, ledgerId, request.getParameter("dateFrom"),
                        request.getParameter("dateTo"));
            }
        } else {
            if (users.getBranch() != null) {
                salesInvoice = tranxSalesInvoiceRepository.findByOutletIdAndBranchIdAndStatusAndSundryDebtorsId(
                        users.getOutlet().getId(), users.getBranch().getId(), true, ledgerId);
            } else {
                salesInvoice = tranxSalesInvoiceRepository.findByOutletIdAndStatusAndSundryDebtorsIdAndBranchIsNull(
                        users.getOutlet().getId(), true, ledgerId);
            }
        }
        if (salesInvoice != null && salesInvoice.size() > 0) {
            for (TranxSalesInvoice invoices : salesInvoice) {
                JsonObject response = new JsonObject();
                int flag = 0;
                /* return qnt validations for invoice  */
                List<TranxSalesInvoiceDetailsUnits> mDetails = tranxSalesInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(invoices.getId(), true);
                if (mDetails != null && mDetails.size() > 0) {
                    for (TranxSalesInvoiceDetailsUnits details : mDetails) {
                        if (details.getTransactionStatusId() == 1L) {
                            flag++;
                            break;
                        }
                    }
                }
                if (flag != 0) {
                    response.addProperty("source", "sales_invoice");
                    response.addProperty("id", invoices.getId());
                    response.addProperty("invoice_id", invoices.getId());

                    response.addProperty("invoice_no", invoices.getSalesInvoiceNo());
                    response.addProperty("invoice_date", invoices.getBillDate().toString());
                    response.addProperty("sales_serial_number", invoices.getSalesSerialNumber());
                    response.addProperty("total_amount", Math.abs(invoices.getTotalAmount()));
                    if (invoices.getTotalAmount() > 0) {
                        response.addProperty("balance_type", "CR");
                    } else {
                        response.addProperty("balance_type", "DR");
                    }
                    response.addProperty("sundry_Debtor_name", invoices.getSundryDebtors().getLedgerName());
                    response.addProperty("sundry_Debtor_id", ledgerId);
                    result.add(response);
                }
            }
            output.addProperty("message", "success");
            output.addProperty("responseStatus", HttpStatus.OK.value());
            output.add("data", result);
        }
        /* challan list */
   /*     if (paramMap.containsKey("dateFrom") && paramMap.containsKey("dateTo")) {
            salesChallans = tranxSalesChallanRepository.findBySuppliersWithDates(users.getOutlet().getId(), true, ledgerId, request.getParameter("dateFrom"), request.getParameter("dateTo"));
        } else {
            salesChallans = tranxSalesChallanRepository.findBySundryDebtorsIdAndOutletIdAndTransactionStatusIdAndStatus(ledgerId, users.getOutlet().getId(), 1L, true);
        }


        if (salesChallans != null && salesChallans.size() > 0) {
            for (TranxSalesChallan invoices : salesChallans) {
                JsonObject response = new JsonObject();
                response.addProperty("source", "sales_challan");
                response.addProperty("id", invoices.getId());
                response.addProperty("invoice_no", invoices.getSalesChallanInvoiceNo());
                response.addProperty("invoice_date", invoices.getBillDate().toString());
//                response.addProperty("transaction_date", invoices.getTransactionDate().toString());
                response.addProperty("purchase_serial_number", invoices.getSalesChallanSerialNumber());
                response.addProperty("total_amount", Math.abs(invoices.getTotalAmount()));
                if (invoices.getTotalAmount() > 0) {
                    response.addProperty("balance_type", "CR");
                } else {
                    response.addProperty("balance_type", "DR");
                }
                response.addProperty("sundry_debtor_name", invoices.getSundryDebtors().getLedgerName());
                response.addProperty("sundry_Debtor_id", ledgerId);
                result.add(response);
            }
        }*/
        else {
            output.addProperty("message", "No Bills Found");
            output.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            output.add("data", result);
        }

        return output;
    }


    /* Sales Returns:  find all products of selected sales invoice bill of sundry creditor */
    public JsonObject productListSalesInvoice(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxSalesInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("sales_invoice_id"));
            TranxSalesInvoice salesInvoice = tranxSalesInvoiceRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);

            finalResult.addProperty("tcs", salesInvoice.getTcs());
            finalResult.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", salesInvoice.getSalesDiscountLedger() != null ? salesInvoice.getSalesDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", salesInvoice.getSalesDiscountAmount());
            finalResult.addProperty("discountInPer", salesInvoice.getSalesDiscountPer());
            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", salesInvoice.getId());
            result.addProperty("invoice_dt", salesInvoice.getBillDate().toString());
            result.addProperty("invoice_no", salesInvoice.getSalesInvoiceNo().toString());
            result.addProperty("sales_sr_no", salesInvoice.getSalesSerialNumber());
            result.addProperty("gstNo", salesInvoice.getGstNumber());
            result.addProperty("sales_account_ledger_id", salesInvoice.getSalesAccountLedger().getId());
            result.addProperty("supplierId", salesInvoice.getSundryDebtors().getId());
            result.addProperty("supplier_name", salesInvoice.getSundryDebtors().getLedgerName());
            result.addProperty("supplier_state", salesInvoice.getSundryDebtors().getStateCode());
//            result.addProperty("transaction_dt", salesInvoice.getTransactionDate().toString());
            /* End of Purchase Invoice Data */

            /* Purchase Invoice Details */
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
                        if (DateConvertUtil.convertStringToLocalDate(salesInvoice.getBillDate().toString()).isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
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
            salesReturnLogger.error("Exception in productListSalesInvoice:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesReturnLogger.error("Exception in productListSalesInvoice:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    /**
     * Delete sales return
     **/
    public JsonObject salesReturnDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesReturnInvoice salesTranx = tranxSalesReturnsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSRT");
        try {
            salesTranx.setStatus(false);
            salesTranx.setOperations("Delete");
            /**** Reverse Postings of Accounting *****/
            deleteAccountingPostings(salesTranx);
            /*** delete the debit note reference table ****/
            TranxCreditNoteNewReferenceMaster creditNoteMaster = tranxCreditNoteNewReferenceRepository.findByTranxSalesReturnInvoiceIdAndStatus(salesTranx.getId(), true);
            if (creditNoteMaster != null) {
                creditNoteMaster.setStatus(false);
                tranxCreditNoteNewReferenceRepository.save(creditNoteMaster);
            }

            /**** Reverse the Ineventory Postings *****/
            List<TranxSalesReturnDetailsUnits> unitsList = new ArrayList<>();
            unitsList = tranxSalesReturnDetailsUnitsRepository.findBySalesReturnInvoiceIdAndStatus(salesTranx.getId(), true);
            if (unitsList != null && unitsList.size() > 0) {
                for (TranxSalesReturnDetailsUnits mUnitObjects : unitsList) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("DR", salesTranx.getTransactionDate(), salesTranx.getId(), mUnitObjects.getQty(), salesTranx.getBranch(), salesTranx.getOutlet(), mUnitObjects.getProduct(), tranxType, null, null, null, mUnitObjects.getUnits(), mUnitObjects.getProductBatchNo(), mUnitObjects.getProductBatchNo() != null ? mUnitObjects.getProductBatchNo().getBatchNo() : null, salesTranx.getFiscalYear(), null);
                    /***** End of new architecture of Inventory Postings *****/
                }
            }
            /**** make status=0 to all ledgers of respective purchase invoice id, due to this we wont get
             details of deleted invoice when we want get details of respective ledger ****/
            List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
            mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(
                    tranxType.getId(), salesTranx.getId(), true);
            for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                try {
                    mPostings.setStatus(false);
                    ledgerTransactionPostingsRepository.save(mPostings);
                } catch (Exception e) {
                    salesReturnLogger.error("Exception in Delete functionality for all ledgers of " +
                            "deleted purchase return invoice->" + e.getMessage());
                }
            }


            /***** NEW METHOD FOR LEDGER POSTING *****/
            /**** make status=0 to all ledgers of respective purchase invoice id, due to this we wont get
             details of deleted invoice when we want get details of respective ledger ****/
            List<LedgerOpeningClosingDetail> detailList = ledgerOpeningClosingDetailRepository.findByTranxTypeIdAndTranxIdAndStatus(
                    tranxType.getId(), salesTranx.getId(), true);
            for (LedgerOpeningClosingDetail ledgerDetail : detailList) {
                try {
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), salesTranx.getTransactionDate(),
                                tranxType, salesTranx.getFiscalYear(), detail);
                    }
                } catch (Exception e) {
                    salesReturnLogger.error("Exception in Delete functionality for all ledgers of " + "deleted purchase return invoice->" + e.getMessage());
                }
            }
            if (salesTranx != null) {
                tranxSalesReturnsRepository.save(salesTranx);
                jsonObject.addProperty("message", "sales return invoice deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in sales return deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            salesReturnLogger.error("Error in sales return Delete()->" + e.getMessage());
        }
        return jsonObject;
    }

    private void deleteAccountingPostings(TranxSalesReturnInvoice salesTranx) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSRT");
        try {
            insertIntoTranxDetailSD(salesTranx, tranxType, "DR", "Delete"); // for Sundry Debtors : DR
            insertIntoTranxDetailSA(salesTranx, tranxType, "CR", "Delete"); // for Sales Accounts : CR
            insertIntoTranxDetailsSalesDiscount(salesTranx, tranxType, "DR", "Delete"); // for Sales Discounts : DR
            deleteIntoTranxDetailRO(salesTranx, tranxType); // for Round Off : CR or DR
            insertDB(salesTranx, "AC", tranxType, "CR", "Delete"); // for Additional Charges : CR
            insertDB(salesTranx, "DT", tranxType, "CR", "Delete"); // for Duties and Taxes : CR
        } catch (Exception e) {
            salesReturnLogger.error("Exception in deleteLedgerTranxDetails:" + e.getMessage());
            System.out.println("Posting Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteIntoTranxDetailRO(TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType) {
        if (mSalesTranx.getRoundOff() >= 0) {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "CR", true, "Sales Return", "Insert");
        } else if (mSalesTranx.getRoundOff() < 0) {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(mSalesTranx.getRoundOff(), mSalesTranx.getSalesRoundOff(), tranxType, mSalesTranx.getSalesRoundOff().getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "DR", true, "Sales Return", "Insert");
        }

        String operation = "Delete";
        if (operation.equalsIgnoreCase("delete")) {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                    mSalesTranx.getSalesRoundOff().getId(), tranxType.getId(), mSalesTranx.getId(), true);
            if (ledgerDetail != null) {
                Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                ledgerDetail.setAmount(0.0);
                ledgerDetail.setClosingAmount(closing);
                ledgerDetail.setStatus(false);
                LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                /***** NEW METHOD FOR LEDGER POSTING *****/
                postingUtility.updateLedgerPostings(mSalesTranx.getSalesRoundOff(), mSalesTranx.getTransactionDate(),
                        tranxType, mSalesTranx.getFiscalYear(), detail);
            }
        }
    }

    public JsonObject AllsalesReturnsByOutlet(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        List<TranxSalesReturnInvoice> salesInvoice = new ArrayList<>();
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
                salesInvoice = tranxSalesReturnsRepository.findSaleReturnListWithDateWithBr(users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, true);
            } else {
                salesInvoice = tranxSalesReturnsRepository.findSaleReturnListWithDate(users.getOutlet().getId(), startDatep, endDatep, true);
            }
        } else {
            if (users.getBranch() != null) {
                salesInvoice = tranxSalesReturnsRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                salesInvoice = tranxSalesReturnsRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
            }
        }


        for (TranxSalesReturnInvoice invoices : salesInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("sales_return_no", invoices.getSalesReturnNo());
            response.addProperty("transaction_date", DateConvertUtil.convertDateToLocalDate(invoices.getTransactionDate()).toString());
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTotalBaseAmount());
            response.addProperty("sales_return_serial_number", invoices.getSalesRtnSrNo());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_debtor_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtor_id", invoices.getSundryDebtors().getId());
            response.addProperty("sales_account_name", invoices.getSalesAccountLedger().getLedgerName());
            if (invoices.getTranxSalesInvoice() != null)
                response.addProperty("invoice_no", invoices.getTranxSalesInvoice().getSalesInvoiceNo());
            if (invoices.getTranxSalesChallan() != null)
                response.addProperty("invoice_no", invoices.getTranxSalesChallan().getSalesChallanInvoiceNo());
            response.addProperty("narration", invoices.getNarration());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //Start of Sales return list with pagination
    public Object salesReturnsByOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {
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
        List<TranxSalesReturnInvoice> saleReturn = new ArrayList<>();
        List<TranxSalesReturnInvoice> saleArrayList = new ArrayList<>();
        List<SalesReturnDTO> salesReturnDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_sales_return_invoice_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += "  AND DATE(transaction_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";

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

            Query q = entityManager.createNativeQuery(query, TranxSalesReturnInvoice.class);
            System.out.println("q ==" + q + "  saleReturn " + saleReturn);
            saleReturn = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxSalesReturnInvoice.class);

            saleArrayList = q1.getResultList();
            System.out.println("Limit total rows " + saleArrayList.size());
            Integer total_pages = (saleArrayList.size() / pageSize);
            if ((saleArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (TranxSalesReturnInvoice invoiceListView : saleReturn) {
                salesReturnDTOList.add(convertToDTDTO(invoiceListView));
            }

            GenericDatatable<SalesReturnDTO> data = new GenericDatatable<>(salesReturnDTOList, saleArrayList.size(), pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(salesReturnDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }
    //End of Sales return list with pagination
    //Start of DTO for sale return

    private SalesReturnDTO convertToDTDTO(TranxSalesReturnInvoice salesReturn) {
        SalesReturnDTO salesReturnDTO = new SalesReturnDTO();
        salesReturnDTO.setId(salesReturn.getId());
        salesReturnDTO.setSales_return_no(salesReturn.getSalesReturnNo());
        salesReturnDTO.setTransaction_date(salesReturn.getTransactionDate().toString());
        salesReturnDTO.setTax_amt(salesReturn.getTotalTax());
        salesReturnDTO.setTaxable_amt(salesReturn.getTotalBaseAmount());
        salesReturnDTO.setSales_return_serial_number(salesReturn.getSalesRtnSrNo());
        salesReturnDTO.setTotal_amount(salesReturn.getTotalAmount());
        salesReturnDTO.setSundry_debtor_name(salesReturn.getSundryDebtors().getLedgerName());
        salesReturnDTO.setSundry_debtor_id(salesReturn.getSundryDebtors().getId());
        salesReturnDTO.setSales_account_name(salesReturn.getSalesAccountLedger().getLedgerName());
//        salesReturnDTO.setInvoice_no(salesReturn.getTranxSalesInvoice().getSalesInvoiceNo());
        if (salesReturn.getTranxSalesInvoice() != null)
            salesReturnDTO.setInvoice_no(salesReturn.getTranxSalesInvoice().getSalesInvoiceNo());
        if (salesReturn.getTranxSalesChallan() != null)
            salesReturnDTO.setInvoice_no(salesReturn.getTranxSalesChallan().getSalesChallanInvoiceNo());

        salesReturnDTO.setNarration(salesReturn.getNarration());
        salesReturnDTO.setTranxCode(salesReturn.getTranxCode());

        return salesReturnDTO;
    }

    //End of DTO for sale return
    public JsonObject getProductEditByIdByFPU(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesReturnInvoice invoiceTranx = tranxSalesReturnsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        productIds = tranxSalesReturnDetailsUnitsRepository.findByTranxPurId(invoiceTranx.getId(), true);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getTransactionDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getSalesReturnByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesReturnInvoiceAddCharges> additionalCharges = new ArrayList<>();
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxSalesReturnInvoice salesInvoice = tranxSalesReturnsRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            finalResult.addProperty("tcs_mode", salesInvoice.getTcsMode());
            if (salesInvoice.getTcsMode().equalsIgnoreCase("tcs")) {
                finalResult.addProperty("tcs_per", salesInvoice.getTcs());
                finalResult.addProperty("tcs_amt", salesInvoice.getTcsAmt());
            } else if (salesInvoice.getTcsMode().equalsIgnoreCase("tds")) {
                finalResult.addProperty("tcs_per", salesInvoice.getTdsPer());
                finalResult.addProperty("tcs_amt", salesInvoice.getTdsAmt());
            } else {
                finalResult.addProperty("tcs_amt", 0.0);
                finalResult.addProperty("tcs_per", 0.0);
            }
            finalResult.addProperty("invoice_id", salesInvoice.getTranxSalesInvoice().getId());
            finalResult.addProperty("narration", salesInvoice.getNarration() != null ? salesInvoice.getNarration() : "");
            finalResult.addProperty("discountLedgerId", salesInvoice.getSalesDiscountLedger() != null ? salesInvoice.getSalesDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", salesInvoice.getSalesDiscountAmount());
            finalResult.addProperty("discountInPer", salesInvoice.getSalesDiscountPer());
            finalResult.addProperty("totalSalesDiscountAmt", salesInvoice.getTotalSalesDiscountAmt());
            finalResult.addProperty("totalQty", salesInvoice.getTotalqty());
            finalResult.addProperty("totalFreeQty", salesInvoice.getFreeQty());
            finalResult.addProperty("grossTotal", salesInvoice.getGrossAmount());
            finalResult.addProperty("totalTax", salesInvoice.getTotalTax());
            finalResult.addProperty("additionLedger1", salesInvoice.getAdditionLedger1() != null ? salesInvoice.getAdditionLedger1().getId() : 0);
            finalResult.addProperty("additionLedgerAmt1", salesInvoice.getAdditionLedgerAmt1() != null ? salesInvoice.getAdditionLedgerAmt1() : 0);
            finalResult.addProperty("additionLedger2", salesInvoice.getAdditionLedger2() != null ? salesInvoice.getAdditionLedger2().getId() : 0);
            finalResult.addProperty("additionLedgerAmt2", salesInvoice.getAdditionLedgerAmt2() != null ? salesInvoice.getAdditionLedgerAmt2() : 0);
            finalResult.addProperty("additionLedger3", salesInvoice.getAdditionLedger3() != null ? salesInvoice.getAdditionLedger3().getId() : 0);
            finalResult.addProperty("additionLedgerAmt3", salesInvoice.getAdditionLedgerAmt3() != null ? salesInvoice.getAdditionLedgerAmt3() : 0);
            finalResult.addProperty("totalamt", salesInvoice.getTotalAmount() != null ? salesInvoice.getTotalAmount() : 0);
            JsonObject result = new JsonObject();
            /* Purchase return Data */
            result.addProperty("id", salesInvoice.getId());
            result.addProperty("invoice_dt", salesInvoice.getTransactionDate().toString());
            result.addProperty("invoice_no", salesInvoice.getSalesReturnNo());
            result.addProperty("tranx_unique_code", salesInvoice.getTranxCode());
            result.addProperty("sales_sr_no", salesInvoice.getSalesRtnSrNo());
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
            result.addProperty("gstNo", salesInvoice.getGstNumber());
            result.addProperty("debtor_id", salesInvoice.getSundryDebtors().getId());
            result.addProperty("debtor_name", salesInvoice.getSundryDebtors().getLedgerName());
            result.addProperty("source", "sales_invoice");
            result.addProperty("additional_charges_total", salesInvoice.getAdditionalChargesTotal());
            result.addProperty("gstNo", salesInvoice.getGstNumber());
            result.addProperty("paymentMode", salesInvoice.getPaymentMode());
            result.addProperty("transaction_dt", salesInvoice.getTransactionDate().toString());
            result.addProperty("isRoundOffCheck", salesInvoice.getIsRoundOff());
            result.addProperty("roundoff", salesInvoice.getRoundOff());
            result.addProperty("ledgerStateCode", salesInvoice.getSundryDebtors().getStateCode());
            /* End of Sales Challan Data */

            /* Sales Challan Details */
            JsonArray row = new JsonArray();
            List<TranxSalesReturnDetailsUnits> unitsArray = tranxSalesReturnDetailsUnitsRepository.findBySalesReturnInvoiceIdAndStatus(salesInvoice.getId(), true);

            for (TranxSalesReturnDetailsUnits mUnits : unitsArray) {
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

                /**** Serial Number ****/
                List<Object[]> serialNum = new ArrayList<>();
                JsonArray serialNumJson = new JsonArray();
                serialNum = tranxSalesReturnPrSrNoRepository.findSerialnumbers(mUnits.getProduct().getId(), mUnits.getId(), true);
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
            /* End of Sales returns Details */

            /***** get Tranx Sales Return Adjustment Bills *****/
            JsonArray mArray = new JsonArray();
            List<TranxSalesReturnAdjustmentBills> mBill = tranxSalesReturnAdjBillsRepository.findByTranxSalesReturnIdAndStatus(id, true);
            for (TranxSalesReturnAdjustmentBills mAdjumentBill : mBill) {
                JsonObject mObject = new JsonObject();
                if (mAdjumentBill.getSource().equalsIgnoreCase("sales_invoice"))
                    mObject.addProperty("invoice_id", mAdjumentBill.getTranxSalesInvoice().getId());
                mObject.addProperty("id", mAdjumentBill.getId());
                mObject.addProperty("invoice_unique_id", "sales_invoice," + mAdjumentBill.getTranxSalesInvoice().getId());
                mObject.addProperty("total_amt", mAdjumentBill.getTotalAmt());
                mObject.addProperty("source", mAdjumentBill.getSource());
                mObject.addProperty("paid_amt", mAdjumentBill.getPaidAmt());
                mObject.addProperty("remaining_amt", mAdjumentBill.getRemainingAmt());
                mObject.addProperty("invoice_no", mAdjumentBill.getTranxSalesInvoice().getSalesInvoiceNo());

                mArray.add(mObject);
            }


            JsonArray jsonAdditionalList = new JsonArray();
            additionalCharges = tranxSalesReturnAddiChargesRepository.findByTranxSalesReturnInvoiceIdAndStatus(salesInvoice.getId(), true);
            if (additionalCharges.size() > 0) {
                for (TranxSalesReturnInvoiceAddCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledgerId", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    // Check if percent is null, if so, set it to 0.0
                    Double percent = mAdditionalCharges.getPercent();
                    if (percent == null) {
                        percent = 0.0; // or whatever default value you want to assign
                    }
                    json_charges.addProperty("percent", percent);
                    jsonAdditionalList.add(json_charges);
                }
            }


            finalResult.add("additionalCharges", jsonAdditionalList);
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);
            finalResult.add("billLst", mArray);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesReturnLogger.error("Error in getSalesReturnByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesReturnLogger.error("Error in getSalesReturnByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public Object editSalesReturnsInvoices(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TransactionTypeMaster tranxType = null;
        TranxSalesReturnInvoice mSalesTranx = null;
        Map<String, String[]> paramMap = request.getParameterMap();

        ResponseMessage responseMessage = new ResponseMessage();
        mSalesTranx = saveIntoSalesReturnEdit(request);
        if (mSalesTranx != null) {
            if (request.getParameter("paymentMode").equalsIgnoreCase("credit"))
                insertIntoNewReference(mSalesTranx, request, "edit");
            else {
                /***** dont allow the invoice next time in bill selection module next time if invoice is adjusted *****/
                TranxSalesInvoice invoice = tranxSalesInvoiceRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_invoice_id")), true);
                invoice.setTransactionStatus(2L);
                tranxSalesInvoiceRepository.save(invoice);
                /* Adjust retun amount into selected purchase invoices */
                if (paramMap.containsKey("billLst")) {
                    String jsonStr = request.getParameter("billLst");
                    JsonElement purDetailsJson = new JsonParser().parse(jsonStr);
                    JsonArray array = purDetailsJson.getAsJsonArray();
                    for (JsonElement mElement : array) {
                        JsonObject mObject = mElement.getAsJsonObject();
                        Long invoiceId = mObject.get("invoice_id").getAsLong();
                        TranxSalesInvoice mInvoice = tranxSalesInvoiceRepository.findByIdAndStatus(invoiceId, true);
                        Double paidAmt = mObject.get("paid_amt").getAsDouble();
                        if (mInvoice != null) {
                            try {
                                // mInvoice.setBalance(mSalesTranx.getTranxSalesInvoice().getBalance() - paidAmt);
                                mInvoice.setBalance(mObject.get("remaianing_amt").getAsDouble());
                                tranxSalesInvoiceRepository.save(mInvoice);
                            } catch (Exception e) {
                                e.printStackTrace();
                                salesReturnLogger.error("Exception in Purchase Return:" + e.getMessage());
                            }
                        }
                        /***** Save Into Tranx Purchase Return Adjument Bills Table ******/
                        TranxSalesReturnAdjustmentBills mBills = null;
                        mBills = tranxSalesReturnAdjBillsRepository.findByIdAndStatus(mObject.get("id").getAsLong(), true);
                        if (mObject.get("id").getAsLong() != 0) {
                            if (mBills != null) {
                                if (mObject.get("source").getAsString().equalsIgnoreCase("sales_invoice"))
                                    mBills.setTranxSalesInvoice(mInvoice);
                                mBills.setSource(mObject.get("source").getAsString());
                                mBills.setPaidAmt(paidAmt);
                                mBills.setRemainingAmt(mObject.get("remaining_amt").getAsDouble());
                                mBills.setTotalAmt(mObject.get("total_amt").getAsDouble());
                                mBills.setTranxSalesReturnId(mSalesTranx.getId());
                            }
                        } else {
                            mBills = new TranxSalesReturnAdjustmentBills();
                            mBills.setStatus(true);
                            mBills.setCreatedBy(mSalesTranx.getCreatedBy());
                            if (mObject.get("source").getAsString().equalsIgnoreCase("sales_invoice"))
                                mBills.setTranxSalesInvoice(mInvoice);
                            mBills.setSource(mObject.get("source").getAsString());
                            mBills.setPaidAmt(paidAmt);
                            mBills.setRemainingAmt(mObject.get("remaining_amt").getAsDouble());
                            mBills.setTotalAmt(mObject.get("total_amt").getAsDouble());
                            mBills.setTranxSalesReturnId(mSalesTranx.getId());
                        }
                        tranxSalesReturnAdjBillsRepository.save(mBills);
                    }
                }

            }

            tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSRT");
            /* save into Additional Charges  */
            String acRowsDeleted = "";
            String strJson = request.getParameter("additionalCharges");
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(strJson);
            JsonArray additionalCharges = tradeElement.getAsJsonArray();
            saveIntoPurchaseAdditionalChargesEdit(additionalCharges, mSalesTranx, tranxType, users.getOutlet().getId());
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
                    salesReturnLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mDblist, tranxType.getId(), mSalesTranx.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), mSalesTranx.getTransactionDate(),
                                tranxType, mSalesTranx.getFiscalYear(), detail);
                    }
                    salesReturnLogger.info("removing unused previous ledger update done");
                }
            }

            responseMessage.setMessage("sales return updated successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        if (mSalesTranx != null) {
            responseMessage.setMessage("sales return updated successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Error in sales invoice updatation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    private void saveIntoPurchaseAdditionalChargesEdit(JsonArray additionalCharges, TranxSalesReturnInvoice mSalesTranx, TransactionTypeMaster tranxType, Long outletId) {
        List<TranxSalesReturnInvoiceAddCharges> chargesList = new ArrayList<>();
        for (JsonElement mAddCharges : additionalCharges) {
            JsonObject object = mAddCharges.getAsJsonObject();
            Double amount = object.get("amt").getAsDouble();
            Long ledgerId = object.get("ledgerId").getAsLong();
//            Long detailsId = object.get("additional_charges_details_id").getAsLong();
            LedgerMaster addcharges = null;
            TranxSalesReturnInvoiceAddCharges charges = null;

            charges = tranxSalesReturnAddiChargesRepository.findByAdditionalChargesIdAndTranxSalesReturnInvoiceIdAndStatus(ledgerId, mSalesTranx.getId(), true);
            if (charges == null) {
                charges = new TranxSalesReturnInvoiceAddCharges();
            }
            addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
            charges.setAmount(amount);
            charges.setAdditionalCharges(addcharges);
            charges.setPercent(object.get("percent").getAsDouble());
            charges.setTranxSalesReturnInvoice(mSalesTranx);
            charges.setStatus(true);

            chargesList.add(charges);
            Boolean isContains = dbList.contains(addcharges.getId());
            Boolean isLedgerContains = ledgerList.contains(addcharges.getId());
            mInputList.add(addcharges.getId());
            ledgerInputList.add(addcharges.getId());
            if (isContains) {
                //  transactionDetailsRepository.ledgerPostingEdit(addcharges.getId(), mPurchaseTranx.getId(), tranxType.getId(), "DR", amount * -1);
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(addcharges.getId(), tranxType.getId(), mSalesTranx.getId());
                if (mLedger != null) {
                    mLedger.setAmount(amount);
                    mLedger.setTransactionDate(mSalesTranx.getTransactionDate());
                    mLedger.setOperations("Updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            } else {
                /* insert */
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(amount, addcharges, tranxType, addcharges.getAssociateGroups(), mSalesTranx.getFiscalYear(), mSalesTranx.getBranch(), mSalesTranx.getOutlet(), mSalesTranx.getTransactionDate(), mSalesTranx.getId(), mSalesTranx.getSalesReturnNo(), "CR", true, tranxType.getTransactionName(), "Insert");
            }


            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, addcharges.getId(), tranxType,
                    "CR", mSalesTranx.getId(), addcharges, mSalesTranx.getTransactionDate(),
                    mSalesTranx.getFiscalYear(), mSalesTranx.getOutlet(), mSalesTranx.getBranch(),
                    mSalesTranx.getTranxCode());
        }

        try {
            tranxSalesReturnAddiChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            //purchaseReturnLogger.error("Error in saveIntoPurchaseAdditionalChargesEdit" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            // purchaseReturnLogger.error("Error in saveIntoPurchaseAdditionalChargesEdit" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public JsonObject mobileSDReturnList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        String flag = "saleReturnList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<LedgerMaster> balanceSummaries = new ArrayList<>();
        balanceSummaries = ledgerRepository.findByPrincipleGroupsIdAndStatus(1L, true);
        for (LedgerMaster balanceSummary : balanceSummaries) {
            Long ledgerId = balanceSummary.getId();
            JsonObject jsonObject = new JsonObject();
            LocalDate endDate = null;
            LocalDate startDate = null;
      /*      if (!request.get("end_date").equalsIgnoreCase("") && !request.get("start_date").equalsIgnoreCase("")) {
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
                list = tranxSalesReturnsRepository.findmobilesumSaleReturnTotalAmt(ledgerId, startDate, endDate, true);
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
                salesReturnLogger.error("Error in salesDelete()->" + e.getMessage());
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

    public Object saleReturnMobileInvoiceList(Map<String, String> request) {
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        endDatep = LocalDate.parse(request.get("end_date"));
        startDatep = LocalDate.parse(request.get("start_date"));
        JsonArray result = new JsonArray();
        List<TranxSalesReturnInvoice> saleInvoice = new ArrayList<>();
        saleInvoice = tranxSalesReturnRepository.findSaleReturnInvoicesListNoBr(Long.valueOf(request.get("sundry_creditor_id")), startDatep, endDatep, true);
        for (TranxSalesReturnInvoice invoices : saleInvoice) {
            JsonObject response = new JsonObject();
            response.addProperty("return_id", invoices.getId());
            response.addProperty("return_no", invoices.getSalesReturnNo());
            response.addProperty("transaction_date", DateConvertUtil.convertDateToLocalDate(invoices.getTransactionDate()).toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_creditor_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("purReturn_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("sundry_debtors_id", invoices.getSundryDebtors().getId());
            response.addProperty("baseAmt", invoices.getTotalBaseAmount());
            response.addProperty("taxAmt", invoices.getTotalTax());
            response.addProperty("flag", "salesReturn");
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public Object saleReturnMobileInvoiceDetailsList(Map<String, String> request) {

        JsonArray result = new JsonArray();
        List<TranxSalesReturnDetailsUnits> saleInvoice = new ArrayList<>();
        try {
            saleInvoice = tranxSalesReturnDetailsUnitsRepository.findBySalesReturnInvoiceIdAndStatus(Long.valueOf(request.get("saleReturn_id")), true);

            for (TranxSalesReturnDetailsUnits invoices : saleInvoice) {
                JsonObject response = new JsonObject();
                response.addProperty("id", invoices.getId());
                response.addProperty("invoice_id", invoices.getSalesReturnInvoiceId());
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
        } catch (Exception e) {
            System.out.println("Exception e:" + e.getMessage());
        }
        TranxSalesReturnInvoice tranxSalesReturnInvoice = tranxSalesReturnRepository.findByIdAndStatus(Long.parseLong(request.get("saleReturn_id")), true);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.addProperty("totalBaseAmt", tranxSalesReturnInvoice.getTotalBaseAmount());
        output.addProperty("roundoff", tranxSalesReturnInvoice.getRoundOff());
        output.addProperty("finalAmt", tranxSalesReturnInvoice.getTotalAmount());
        output.addProperty("taxAmt", tranxSalesReturnInvoice.getTotalTax());
        output.addProperty("totalDisAmt", tranxSalesReturnInvoice.getTotalSalesDiscountAmt());
        output.add("data", result);
        return output;
    }


    public JsonObject getDebtorsPendingBills(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long ledgerId = Long.parseLong(request.getParameter("ledger_id"));
        String type = request.getParameter("type");
        List<TranxSalesInvoice> mInput = new ArrayList<>();
        List<TranxSalesInvoice> salesInvoice = new ArrayList<>();

        JsonArray result = new JsonArray();
        JsonObject finalResult = new JsonObject();
        try {
            /* start of SD of bill by bill */
            if (type.equalsIgnoreCase("SD")) {
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledgerId, true);
                /* checking for Bill by bill (bill by bill id: 1) */
                if (ledgerMaster.getBalancingMethod().getId() == 1) {
                    /* find all sales invoices against sundry debtors */
                    if (users.getBranch() != null) {
                        salesInvoice = tranxSalesInvoiceRepository.findPendingBillsByBranchId(users.getOutlet().getId(), users.getBranch().getId(), true, ledgerId);
                    } else {
                        salesInvoice = tranxSalesInvoiceRepository.findPendingBills(users.getOutlet().getId(), true, ledgerId);
                    }
                    if (salesInvoice.size() > 0) {
                        for (TranxSalesInvoice newSalesInvoice : salesInvoice) {
                            JsonObject response = new JsonObject();
                            response.addProperty("invoice_id", newSalesInvoice.getId());
                            response.addProperty("amount", Math.abs(newSalesInvoice.getBalance()));
                            response.addProperty("invoice_date", newSalesInvoice.getBillDate().toString());
                            response.addProperty("invoice_no", newSalesInvoice.getSalesInvoiceNo());
                            response.addProperty("ledger_id", ledgerId);
                            response.addProperty("source", "sales_invoice");

                            result.add(response);
                        }
                    }

                } else {
                    /*  Debtors :  on Account  */
                    Double sumCR = 0.0;
                    Double sumDR = 0.0, closingBalance = 0.0;
                    sumCR = ledgerTransactionPostingsRepository.findsumCR(ledgerId);
                    sumDR = ledgerTransactionPostingsRepository.findsumDR(ledgerId);
                    closingBalance = sumCR - sumDR;//0-(-0.40)-0.20
                    if (closingBalance != 0) {
                        JsonObject response = new JsonObject();
                        response.addProperty("amount", Math.abs(closingBalance));
                        response.addProperty("ledger_id", ledgerId);
                        result.add(response);
                    }
                }

            }

        } catch (Exception e) {
            salesReturnLogger.error("Exception in: getDebtorsPendingBills ->" + e.getMessage());
            System.out.println("Exception in: get_debtors_pending_bills ->" + e.getMessage());
            e.printStackTrace();
        }
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("list", result);
        return finalResult;
    }


    public JsonObject getSalesReturnBillPrint(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        JsonObject finalResult = new JsonObject();
        TranxSalesReturnInvoice salesInvoice = null;
        TranxCounterSales counterSales = null;

        String source = request.getParameter("source");
        String key = request.getParameter("print_type"); //check whether printbill is calling from create page or from list page
        /***** if  print_type is create, then use serialnumber of invoice to fetch invoice details ,
         * if print_type is list then use invoice id to fetch invoice details *****/
        try {
            String invoiceNo = request.getParameter("id");
            Long id = 0L;
            if (source.equalsIgnoreCase("sales_return")) { //counter_sales
                if (users.getBranch() != null) {
                    if (key.equalsIgnoreCase("create")) {
                        salesInvoice = tranxSalesReturnRepository.findBySalesReturnNoAndOutletIdAndBranchIdAndStatus(invoiceNo, users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesInvoice = tranxSalesReturnRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    }

                } else {
                    if (key.equalsIgnoreCase("create")) {
                        salesInvoice = tranxSalesReturnRepository.findBySalesReturnNoAndOutletIdAndStatusAndBranchIsNull(invoiceNo, users.getOutlet().getId(), true);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesInvoice = tranxSalesReturnRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);
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
                debtorsObject.addProperty("supplier_gstin", salesInvoice.getSundryDebtors().getGstin());
                debtorsObject.addProperty("supplier_phone", salesInvoice.getSundryDebtors().getMobile());

                JsonObject invoiceObject = new JsonObject();
                /* Sales Invoice Data */
                invoiceObject.addProperty("id", salesInvoice.getId());
                invoiceObject.addProperty("invoice_dt", salesInvoice.getTransactionDate().toString());
                invoiceObject.addProperty("invoice_no", salesInvoice.getSalesReturnNo());
                invoiceObject.addProperty("state_code", salesInvoice.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", salesInvoice.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(salesInvoice.getTaxableAmount()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(salesInvoice.getTotaligst()));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(salesInvoice.getTotalcgst()));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(salesInvoice.getTotalsgst()));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(salesInvoice.getTotalBaseAmount()));
                invoiceObject.addProperty("total_discount", numFormat.numFormat(salesInvoice.getTotalSalesDiscountAmt()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(salesInvoice.getTotalAmount()));
                invoiceObject.addProperty("payment_mode", salesInvoice.getPaymentMode());


                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();
                /* getting Units of Sales Quotations*/
                List<TranxSalesReturnDetailsUnits> unitDetails = tranxSalesReturnDetailsUnitsRepository.findBySalesReturnInvoiceIdAndStatus(salesInvoice.getId(), true);
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
                    /*if (mUnit.getFlavourMaster() != null) {
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
            salesReturnLogger.error("Error in getInvoiceBillPrint :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesReturnLogger.error("Error in getInvoiceBillPrint :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public Object validateSalesReturn(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesReturnInvoice salesInvoice = null;
        if (users.getBranch() != null) {
            salesInvoice = tranxSalesReturnRepository.findByOutletIdAndBranchIdAndSalesReturnNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesInvoiceNo"));
        } else {
            salesInvoice = tranxSalesReturnRepository.findByOutletIdAndSalesReturnNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), request.getParameter("salesInvoiceNo"));
        }
        if (salesInvoice != null) {
            // System.out.println("Already Ledger created with this name or code");
            responseMessage.setMessage("Duplicate sales return number exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales return number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object validateSalesInvoicesReturn(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Long invoiceId = Long.parseLong(request.getParameter("invoice_id"));
        TranxSalesReturnInvoice salesInvoice = null;
        if (users.getBranch() != null) {
            salesInvoice = tranxSalesReturnRepository.findByOutletIdAndBranchIdAndSalesReturnNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesInvoiceNo"));
        } else {
            salesInvoice = tranxSalesReturnRepository.findByOutletIdAndSalesReturnNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), request.getParameter("salesInvoiceNo"));
        }
        if (salesInvoice != null && invoiceId != salesInvoice.getId()) {
            responseMessage.setMessage("Duplicate sales return number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales return number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }
}
