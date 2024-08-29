package in.truethics.ethics.ethicsapiv10.service.tranx_service.receipt;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.accountentrydto.ReceiptDTO;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.PaymentModeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionStatusRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionTypeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentPerticularsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.*;
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
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TranxReceiptService {
    @Autowired
    private TranxReceiptMasterRepository repository;
    @Autowired
    private TranxReceiptPerticularsRepository tranxReceiptPerticularsRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TranxReceiptPerticularsDetailsRepository tranxReceiptPerticularsDetailsRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private GenerateSlugs generateSlugs;
    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private LedgerTransactionDetailsRepository transactionDetailsRepository;
    @Autowired
    private TranxCreditNoteNewReferenceRepository tranxCreditNoteNewReferenceRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private TranxDebitNoteNewReferenceRepository tranxDebitNoteNewReferenceRepository;
    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;

    @Autowired
    private TranxReceiptMasterRepository tranxReceiptMasterRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    List<Long> ledgerInputList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB

    private static final Logger receiptLogger = LogManager.getLogger(TranxReceiptService.class);
    @Autowired
    private TranxPaymentPerticularsRepository tranxPaymentPerticularsRepository;

    @Autowired
    private LedgerMasterRepository ledgerRepository;
    @Autowired
    private TranxCreditNoteDetailsRepository tranxCreditNoteDetailsRepository;
    @Autowired
    private LedgerOpeningBalanceRepository ledgerOpeningBalanceRepository;
    @Autowired
    private PaymentModeMasterRepository paymentModeMasterRepository;
    @Autowired
    private LedgerBankDetailsRepository ledgerBankDetailsRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private PostingUtility postingUtility;
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;

    public JsonObject receiptLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        Long count = repository.findLastRecord(users.getOutlet().getId());

        Long count = 0L;
        if (users.getBranch() != null) {
            count = repository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = repository.findLastRecord(users.getOutlet().getId());
        }


        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String receiptCode = "RCPT" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("receipt_sr_no", count + 1);
        result.addProperty("receipt_code", receiptCode);
        return result;
    }

    public JsonObject getSundryDebtorsAndIndirectIncomes(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        JsonArray result = new JsonArray();
        JsonObject finalResult = new JsonObject();
        List<LedgerMaster> sundryDebtors = new ArrayList<>();
        List<LedgerMaster> sundryCreditors = new ArrayList<>();

        if (users.getBranch() != null) {
            sundryCreditors = ledgerMasterRepository.findByOutletIdAndBranchIdAndPrincipleGroupsIdAndStatus(
                    users.getOutlet().getId(), users.getBranch().getId(), 5L, true);
            sundryDebtors = ledgerMasterRepository.findByOutletIdAndBranchIdAndPrincipleGroupsIdAndStatus(
                    users.getOutlet().getId(), users.getBranch().getId(), 1L, true);

        } else {
            sundryCreditors = ledgerMasterRepository.findByOutletIdAndPrincipleGroupsIdAndStatusAndBranchIsNull(users.getOutlet().getId(), 5L, true);
            sundryDebtors = ledgerMasterRepository.findByOutletIdAndPrincipleGroupsIdAndStatusAndBranchIsNull(users.getOutlet().getId(), 1L, true);
        }
//        sundryDebtors = ledgerMasterRepository.findByOutletIdAndPrincipleGroupsId(
//                users.getOutlet().getId(), 1L);
//        sundryCreditors = ledgerMasterRepository.findByOutletIdAndPrincipleGroupsId(
//                users.getOutlet().getId(), 5L);

        /* for Sundry Creditors List */
        if (sundryDebtors.size() > 0) {
            for (LedgerMaster mLedger : sundryDebtors) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mLedger.getId());
                response.addProperty("ledger_name", mLedger.getLedgerName());
                response.addProperty("balancing_method", mLedger.getBalancingMethod() != null ? generateSlugs.getSlug(mLedger.getBalancingMethod().getBalancingMethod()) : "");
                response.addProperty("type", "SD");

                result.add(response);
            }
        }
        if (sundryCreditors.size() > 0) {
            for (LedgerMaster mLedger : sundryCreditors) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mLedger.getId());
                response.addProperty("ledger_name", mLedger.getLedgerName());
                response.addProperty("balancing_method", generateSlugs.getSlug(mLedger.getBalancingMethod().getBalancingMethod()));
                response.addProperty("type", "SC");
                result.add(response);
            }
        }
        List<LedgerMaster> indirectIncomes = new ArrayList<>();
        indirectIncomes = ledgerMasterRepository.findByOutletIdAndPrinciplesIdAndStatus(users.getOutlet().getId(), 9L, true);
        if (indirectIncomes.size() > 0) {
            for (LedgerMaster mLedger : indirectIncomes) {
                if (!mLedger.getLedgerName().equalsIgnoreCase("Purchase Discount")) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mLedger.getId());
                    response.addProperty("ledger_name", mLedger.getLedgerName());
                    response.addProperty("balancing_method", "NA");
                    response.addProperty("type", "IC");
                    result.add(response);
                }
            }
        }
        /***** Current assests ****/
        List<LedgerMaster> currentAssets = new ArrayList<>();
        currentAssets = ledgerMasterRepository.findByOutletIdAndPrinciplesIdAndStatus(users.getOutlet().getId(), 3L, true);
        if (currentAssets.size() > 0) {
            for (LedgerMaster mLedger : currentAssets) {
                if (!mLedger.getUniqueCode().equalsIgnoreCase("SUDR")) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mLedger.getId());
                    response.addProperty("ledger_name", mLedger.getLedgerName());
                    response.addProperty("balancing_method", "on-account");
                    response.addProperty("type", "CA");
                    result.add(response);
                }
            }
        }
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("list", result);
        return finalResult;
    }

    public JsonObject getDebtorsPendingBills(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        Long ledgerId = Long.parseLong(request.getParameter("ledger_id"));
        String type = request.getParameter("type");
        List<TranxSalesInvoice> mInput = new ArrayList<>();
        List<TranxSalesInvoice> salesInvoice = new ArrayList<>();
        List<TranxCreditNoteNewReferenceMaster> list = new ArrayList<>();
        List<TranxDebitNoteNewReferenceMaster> listdebit = new ArrayList<>();
        JsonArray result = new JsonArray();
        JsonObject finalResult = new JsonObject();
        LocalDate endDate = null;
        LocalDate startDate = null;
        Boolean flag = false;
        if (paramMap.containsKey("start_date") && paramMap.containsKey("end_date")) {
            startDate = LocalDate.parse(request.getParameter("start_date"));
            endDate = LocalDate.parse(request.getParameter("end_date"));
            flag = true;
        }
        try {
            /* start of SD of bill by bill */
            if (type.equalsIgnoreCase("SD")) {
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledgerId, true);
                /* checking for Bill by bill (bill by bill id: 1) */
                if (ledgerMaster.getBalancingMethod().getId() == 1) {
                    /* find all sales invoices against sundry debtors */
                    if (users.getBranch() != null) {
                        if (flag) {
                            salesInvoice = tranxSalesInvoiceRepository.findPendingBillsByBranchIdWithDates(
                                    users.getOutlet().getId(), users.getBranch().getId(), true, ledgerId, startDate, endDate);
                        } else {
                            salesInvoice = tranxSalesInvoiceRepository.findPendingBillsByBranchId(
                                    users.getOutlet().getId(), users.getBranch().getId(), true, ledgerId);
                        }
                    } else {
                        if (flag) {
                            salesInvoice = tranxSalesInvoiceRepository.findPendingBillsWithDate(users.getOutlet().getId(),
                                    true, ledgerId, startDate, endDate);
                        } else {
                            salesInvoice = tranxSalesInvoiceRepository.findPendingBills(users.getOutlet().getId(), true, ledgerId);
                        }
                    }
                    if (salesInvoice.size() > 0) {
                        for (TranxSalesInvoice newSalesInvoice : salesInvoice) {
                            JsonObject response = new JsonObject();
                            response.addProperty("invoice_id", newSalesInvoice.getId());
                            response.addProperty("id", newSalesInvoice.getId());
                            response.addProperty("invoice_unique_id", "sales_invoice," + newSalesInvoice.getId());
                            response.addProperty("amount", Math.abs(newSalesInvoice.getBalance()));
                            response.addProperty("total_amt", Math.abs(newSalesInvoice.getTotalAmount()));
                            response.addProperty("invoice_date",DateConvertUtil.convertDateToLocalDate(newSalesInvoice.getBillDate()).toString());
                            response.addProperty("invoice_no", newSalesInvoice.getSalesInvoiceNo());
                            response.addProperty("ledger_id", ledgerId);
                            response.addProperty("source", "sales_invoice");
                            response.addProperty("due_days", ledgerMaster.getCreditDays());
                            response.addProperty("balancing_type", "Dr");
                           /* TranxReceiptPerticularsDetails details = tranxReceiptPerticularsDetailsRepository.
                                    findByLedgerMasterIdAndTranxInvoiceIdAndStatus(
                                            newSalesInvoice.getSundryDebtors().getId(), newSalesInvoice.getId(), true);
                            if (details != null) {
                                response.addProperty("paid_amt", 0.0);
                                response.addProperty("bill_details_id", details.getId());
                                response.addProperty("remaining_amt", details.getRemainingAmt());
                            } else {
                                response.addProperty("paid_amt", 0.0);
                                response.addProperty("bill_details_id", 0);
                                response.addProperty("remaining_amt", 0.0);
                            }*/
                            response.addProperty("paid_amt", 0.0);
                            response.addProperty("bill_details_id", 0);
                            response.addProperty("remaining_amt", 0.0);
                            result.add(response);
                        }
                    }

                }
                /*  Debtors :  on Account  */
                else {
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
                if (flag)
                    list = tranxCreditNoteNewReferenceRepository.findByCreditnoteListReceiptWithDate(ledgerId, true, 1L,
                            "credit", "advance_receipt", users.getOutlet().getId(), startDate, endDate);
                else {
                    list = tranxCreditNoteNewReferenceRepository.findByCreditnoteListReceipt(ledgerId, true, 1L,
                            "credit", "advance_receipt", users.getOutlet().getId());
                }

                if (list != null && list.size() > 0) {
                    for (TranxCreditNoteNewReferenceMaster mTranxCreditNote : list) {
                        if (mTranxCreditNote.getBalance() != 0.0) {
                            JsonObject data = new JsonObject();
                            data.addProperty("invoice_id", mTranxCreditNote.getId());
                            data.addProperty("invoice_unique_id", "credit_note," + mTranxCreditNote.getId());
                            data.addProperty("amount", mTranxCreditNote.getBalance());
                            if (mTranxCreditNote.getAdjustmentStatus().toLowerCase().contains("advance"))
                                data.addProperty("invoice_no", "new_ref " + mTranxCreditNote.getCreditnoteNewReferenceNo());
                            else
                                data.addProperty("invoice_no", mTranxCreditNote.getCreditnoteNewReferenceNo());
                            data.addProperty("invoice_date", mTranxCreditNote.getTranscationDate().toString());
                            data.addProperty("total_amt", mTranxCreditNote.getTotalAmount());
                            data.addProperty("source", "credit_note");
                            data.addProperty("balancing_type", "CR");
                            result.add(data);
                        }
                    }
                }
            }

            if (type.equalsIgnoreCase("SC")) {
                listdebit = tranxDebitNoteNewReferenceRepository.
                        findBySundryCreditorIdAndStatusAndTransactionStatusIdAndAdjustmentStatusAndOutletId(ledgerId, true, 1L, "refund", users.getOutlet().getId());
                if (listdebit != null && listdebit.size() > 0) {
                    for (TranxDebitNoteNewReferenceMaster mTranxDebitNote : listdebit) {
                        if (mTranxDebitNote.getBalance() != 0.0) {
                            JsonObject data = new JsonObject();
                            data.addProperty("invoice_id", mTranxDebitNote.getId());
                            data.addProperty("invoice_unique_id", "debit_note," + mTranxDebitNote.getId());
                            data.addProperty("invoice_no", mTranxDebitNote.getDebitnoteNewReferenceNo());
                            data.addProperty("invoice_date", mTranxDebitNote.getCreatedAt().toString());
                            data.addProperty("Total_amt", mTranxDebitNote.getTotalAmount());
                            data.addProperty("source", "debit_note");
                            result.add(data);
                        }
                    }
                }
            }
            if (type.equalsIgnoreCase("CA")) {
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
            /**** Ledger Opening Balance ****/
            List<LedgerOpeningBalance> openingBalances = ledgerOpeningBalanceRepository.findByLedgerIdAndStatus(ledgerId, true);
            if (openingBalances.size() > 0) {
                for (LedgerOpeningBalance mBalance : openingBalances) {
                    if (mBalance.getInvoice_bal_amt() != 0.0) {
                        JsonObject openingObject = new JsonObject();
                        openingObject.addProperty("invoice_no", mBalance.getInvoice_no());
                        openingObject.addProperty("invoice_id", mBalance.getId());
                        openingObject.addProperty("invoice_unique_id", "opening_invoice," + mBalance.getId());
                        openingObject.addProperty("total_amt", mBalance.getBill_amt() != null ? mBalance.getBill_amt() : 0.00);
                        openingObject.addProperty("amount", mBalance.getInvoice_bal_amt() != null ? mBalance.getInvoice_bal_amt() : 0.00);
                        openingObject.addProperty("invoice_date", mBalance.getInvoice_date().toString());
                        openingObject.addProperty("invoice_bal_type", mBalance.getInvoiceBalType());
                        openingObject.addProperty("ledger_id", ledgerId);
                        openingObject.addProperty("balancing_type", mBalance.getBalancingType());
                        openingObject.addProperty("source", "opening_balance");
                        openingObject.addProperty("due_days", mBalance.getDue_days());
                        result.add(openingObject);
                    }
                }
            }

        } catch (Exception e) {
            receiptLogger.error("Exception in: getDebtorsPendingBills ->" + e.getMessage());
            System.out.println("Exception in: get_debtors_pending_bills ->" + e.getMessage());
            e.printStackTrace();
        }
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        finalResult.add("list", result);
        return finalResult;
    }

    public JsonObject createReceipt(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        TranxReceiptMaster tranxReceipt = new TranxReceiptMaster();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            tranxReceipt.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        tranxReceipt.setOutlet(outlet);
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("transaction_dt"));
        tranxReceipt.setTranscationDate(strDt);
        tranxReceipt.setStatus(true);
        /*     fiscal year mapping  */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            tranxReceipt.setFiscalYear(fiscalYear);
            tranxReceipt.setFinancialYear(fiscalYear.getFiscalYear());
        }
        tranxReceipt.setReceiptSrNo(Long.parseLong(request.getParameter("receipt_sr_no")));
        if (paramMap.containsKey("narration")) tranxReceipt.setNarrations(request.getParameter("narration"));
        else {
            tranxReceipt.setNarrations(request.getParameter(""));
        }
        tranxReceipt.setReceiptNo(request.getParameter("receipt_code"));
        tranxReceipt.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
        tranxReceipt.setCreatedBy(users.getId());
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        tranxReceipt.setTranxCode(tranxCode);
        TranxReceiptMaster tranxReceiptMaster = repository.save(tranxReceipt);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("row");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                String crdrType = "";
                /*Receipt Master */
                JsonObject receiptRow = row.get(i).getAsJsonObject();
                /*Receipt Perticulars */
                TranxReceiptPerticulars tranxReceiptPerticulars = new TranxReceiptPerticulars();
                LedgerMaster ledgerMaster = null;
                tranxReceiptPerticulars.setBranch(branch);
                tranxReceiptPerticulars.setOutlet(outlet);
                tranxReceiptPerticulars.setStatus(true);
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(
                        receiptRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null) tranxReceiptPerticulars.setLedgerMaster(ledgerMaster);
                tranxReceiptPerticulars.setTranxReceiptMaster(tranxReceiptMaster);
                tranxReceiptPerticulars.setType(receiptRow.get("type").getAsString());
                tranxReceiptPerticulars.setLedgerType(receiptRow.get("perticulars").getAsJsonObject().get("type").getAsString());
                tranxReceiptPerticulars.setLedgerName(receiptRow.get("perticulars").getAsJsonObject().get("ledger_name").getAsString());
                tranxReceiptPerticulars.setTransactionDate(tranxDate);
                if (receiptRow.has("type"))
                    crdrType = receiptRow.get("type").getAsString();
                JsonObject perticulars = receiptRow.get("perticulars").getAsJsonObject();
                if (!receiptRow.get("paid_amt").getAsString().equalsIgnoreCase(""))
                    total_amt = receiptRow.get("paid_amt").getAsDouble();
                else {
                    total_amt = 0.0;
                }
                if (crdrType.equalsIgnoreCase("dr") && !receiptRow.get("paid_amt").getAsString().equalsIgnoreCase("")) {
                    tranxReceiptPerticulars.setDr(total_amt);
                }
                if (crdrType.equalsIgnoreCase("cr") && !receiptRow.get("paid_amt").getAsString().equalsIgnoreCase("")) {
                    tranxReceiptPerticulars.setCr(total_amt);
                }
                if (receiptRow.has("bank_payment_no") &&
                        !receiptRow.get("bank_payment_no").getAsString().equalsIgnoreCase("")) {
                    tranxReceiptPerticulars.setPaymentTranxNo(receiptRow.get("bank_payment_no").getAsString());
                } else {
                    tranxReceiptPerticulars.setPaymentTranxNo("");

                }
                if (receiptRow.has("bank_payment_type") &&
                        !receiptRow.get("bank_payment_type").getAsString().equalsIgnoreCase("")) {
                    PaymentModeMaster paymentModeMaster = paymentModeMasterRepository.findById(
                            receiptRow.get("bank_payment_type").getAsLong()).get();
                    tranxReceiptPerticulars.setPaymentMethod(paymentModeMaster.getPaymentMode());
                }
                if (receiptRow.has("bank_name") &&
                        !receiptRow.get("bank_name").getAsString().equalsIgnoreCase("")) {
                    LedgerBankDetails bankDetails = ledgerBankDetailsRepository.
                            findByIdAndStatus(receiptRow.get("bank_name").getAsLong(), true);
                    tranxReceiptPerticulars.setBankName(bankDetails.getBankName());
                }
                if (receiptRow.has("payment_date") &&
                        !receiptRow.get("payment_date").getAsString().equalsIgnoreCase("") &&
                        !receiptRow.get("payment_date").getAsString().toLowerCase().contains("invalid"))
                    tranxReceiptPerticulars.setPaymentDate(LocalDate.parse(receiptRow.get("payment_date").getAsString()));

                tranxReceiptPerticulars.setCreatedBy(users.getId());
                //    tranxReceipt.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
                if (perticulars.has("payableAmt"))
                    tranxReceiptPerticulars.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxReceiptPerticulars.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxReceiptPerticulars.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxReceiptPerticulars.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                TranxReceiptPerticulars mParticular = tranxReceiptPerticularsRepository.save(tranxReceiptPerticulars);

                /*Receipt Perticulars Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            TranxReceiptPerticularsDetails tranxRptDetails = new TranxReceiptPerticularsDetails();
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxSalesInvoice mSaleInvoice = null;
                            tranxRptDetails.setBranch(branch);
                            tranxRptDetails.setOutlet(outlet);
                            if (ledgerMaster != null) tranxRptDetails.setLedgerMaster(ledgerMaster);
                            tranxRptDetails.setTranxReceiptMaster(tranxReceiptMaster);
                            tranxRptDetails.setTranxReceiptPerticulars(mParticular);
                            tranxRptDetails.setStatus(true);
                            tranxRptDetails.setCreatedBy(users.getId());
                            tranxRptDetails.setType(jsonBill.get("source").getAsString());
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxRptDetails.setType("new_reference");
                            }
                            tranxRptDetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());

                            tranxRptDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString().contains(" ") ? jsonBill.get("invoice_date").getAsString().split(" ")[0] : jsonBill.get("invoice_date").getAsString()));
                            tranxRptDetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxRptDetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            tranxRptDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString().contains(" ") ? jsonBill.get("invoice_date").getAsString().split(" ")[0] : jsonBill.get("invoice_date").getAsString()));
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    createCreditNote(tranxReceiptMaster, jsonBill.get("amount").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "create");
                                } else {
                                    mSaleInvoice = tranxSalesInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mSaleInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxSalesInvoiceRepository.save(mSaleInvoice);
                                    }
                                }

                            } else if (jsonBill.get("source").getAsString().equalsIgnoreCase("credit_note")) {
                                TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReference = tranxCreditNoteNewReferenceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxCreditNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxCreditNoteNewReference.setTransactionStatusId(transactionStatus.getId());
                                        tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteNewReference);
                                    }
                                }
                            } /*else if (jsonBill.get("source").getAsString().equalsIgnoreCase("debit_note")) {
                                TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = tranxDebitNoteNewReferenceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxDebitNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
                                        tranxDebitNoteNewReferenceRepository.save(tranxDebitNoteNewReference);
                                    }
                                }
                            }*/ else if (jsonBill.get("source").getAsString().equalsIgnoreCase("opening_balance")) {
                                LedgerOpeningBalance mOpening = ledgerOpeningBalanceRepository.findByOpeningBalInvoice(ledgerMaster.getId(),
                                        jsonBill.get("invoice_no").getAsString(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    mOpening.setInvoice_bal_amt(jsonBill.get("remaining_amt").getAsDouble());
                                    ledgerOpeningBalanceRepository.save(mOpening);
                                }

                            }
                            tranxRptDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                            tranxRptDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                            tranxRptDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                            tranxRptDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            tranxReceiptPerticularsDetailsRepository.save(tranxRptDetails);
                        }
                    }
                }

                // TranxReceiptPerticulars mReceipt = tranxReceiptPerticularsRepository.save(tranxReceiptPerticulars);
                insertIntoPostings(mParticular, total_amt, crdrType, "Insert");//Accounting Postings
            }
            response.addProperty("message", "Receipt successfully done..");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            receiptLogger.error("Error in createReceipt :->" + e.getMessage());
            response.addProperty("message", "Error in Receipt creation");
            response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return response;
    }

    private void createCreditNote(TranxReceiptMaster tranxReceiptMaster, double total_amt, LedgerMaster ledgerMaster,
                                  Long invoiceId, String key) {
        TranxCreditNoteNewReferenceMaster tranxCreditNoteMaster = null;
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        if (key.equalsIgnoreCase("create")) {
            tranxCreditNoteMaster = new TranxCreditNoteNewReferenceMaster();
            Long count = tranxCreditNoteNewReferenceRepository.findLastRecord(tranxReceiptMaster.getOutlet().getId());
            String serailNo = String.format("%05d", count + 1);// 5 digit serial number
            GenerateDates generateDates = new GenerateDates();
            String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
            String dbtnCode = "CDTN" + currentMonth + serailNo;
            tranxCreditNoteMaster.setCreditnoteNewReferenceNo(dbtnCode);
            tranxCreditNoteMaster.setStatus(true);
            tranxCreditNoteMaster.setTransactionStatusId(transactionStatus.getId());
            tranxCreditNoteMaster.setSalesInvoiceId(invoiceId);
        } else
            tranxCreditNoteMaster = tranxCreditNoteNewReferenceRepository.findByReceiptIdAndStatus(
                    tranxReceiptMaster.getId(), true);
        if (tranxReceiptMaster.getBranch() != null)
            tranxCreditNoteMaster.setBranchId(tranxReceiptMaster.getBranch().getId());
        tranxCreditNoteMaster.setOutletId(tranxReceiptMaster.getOutlet().getId());
        tranxCreditNoteMaster.setSundryDebtorsId(ledgerMaster.getId());
        tranxCreditNoteMaster.setReceiptId(tranxReceiptMaster.getId());
        /* this parameter segregates whether debit note is from purchase invoice
        or purchase challan*/
        tranxCreditNoteMaster.setSource("sales_invoice");
        tranxCreditNoteMaster.setAdjustmentStatus("advance_receipt");
        tranxCreditNoteMaster.setFinancialYear(tranxReceiptMaster.getFinancialYear());
        tranxCreditNoteMaster.setBalance(tranxReceiptMaster.getTotalAmt());
        tranxCreditNoteMaster.setFiscalYearId(tranxReceiptMaster.getFiscalYear().getId());
        tranxCreditNoteMaster.setTranscationDate(tranxReceiptMaster.getTranscationDate());

        tranxCreditNoteMaster.setTotalAmount(total_amt);
        tranxCreditNoteMaster.setBalance(total_amt);
        try {
            TranxCreditNoteNewReferenceMaster newCreditNote = tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteMaster);
            TranxCreditNoteDetails mDetails = null;
            if (key.equalsIgnoreCase("create")) {
                mDetails = new TranxCreditNoteDetails();
                mDetails.setOutletId(tranxReceiptMaster.getOutlet().getId());
                mDetails.setStatus(true);
                mDetails.setTransactionStatusId(transactionStatus.getId());
                if (tranxReceiptMaster.getBranch() != null)
                    mDetails.setBranchId(tranxReceiptMaster.getBranch().getId());
            } else
                mDetails = tranxCreditNoteDetailsRepository.findByStatusAndTranxCreditNoteMasterId(true, newCreditNote.getId());

            mDetails.setSundryDebtorsId(newCreditNote.getSundryDebtorsId());
            mDetails.setTotalAmount(newCreditNote.getTotalAmount());
            mDetails.setPaidAmt(newCreditNote.getTotalAmount());
            mDetails.setAdjustedId(newCreditNote.getSalesInvoiceId());
            mDetails.setAdjustedSource("advance_receipt");
            mDetails.setBalance(0.0);
            mDetails.setOperations("advance_receipt");
            mDetails.setTranxCreditNoteMasterId(newCreditNote.getId());
            tranxCreditNoteDetailsRepository.save(mDetails);
        } catch (Exception e) {
            receiptLogger.error("Exception in insertIntoNewReference:" + e.getMessage());
        }
    }

    /* Accounting Postings of Receipt Vouchers  */
    private void insertIntoPostings(TranxReceiptPerticulars mReceipt, double total_amt, String crdrType, String operation) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        try {
            /* for Sundry Debtors  */

            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(total_amt, mReceipt.getLedgerMaster(), tranxType,
                    mReceipt.getLedgerMaster().getAssociateGroups(),
                    mReceipt.getTranxReceiptMaster().getFiscalYear(), mReceipt.getBranch(),
                    mReceipt.getOutlet(), mReceipt.getTranxReceiptMaster().getTranscationDate(),
                    mReceipt.getTranxReceiptMaster().getId(),
                    mReceipt.getTranxReceiptMaster().getReceiptNo(), crdrType, true,
                    "Receipt", operation);

            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, crdrType, total_amt, mReceipt.getTranxReceiptMaster().getFiscalYear(),
                        mReceipt.getLedgerMaster(), mReceipt.getTranxReceiptMaster().getTranscationDate(), mReceipt.getTranxReceiptMaster().getId(),
                        mReceipt.getOutlet(), mReceipt.getBranch(), mReceipt.getTranxReceiptMaster().getTranxCode());
            }
            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mReceipt.getLedgerMaster().getId(), tranxType.getId(), mReceipt.getTranxReceiptMaster().getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mReceipt.getLedgerMaster(), mReceipt.getTranxReceiptMaster().getTranscationDate(),
                            tranxType, mReceipt.getTranxReceiptMaster().getFiscalYear(), detail);
                }
            }

            /**** Save into Day Book ****/
            if (crdrType.equalsIgnoreCase("cr")) {
                saveIntoDayBook(mReceipt);
            }

        } catch (Exception e) {
            e.printStackTrace();
            receiptLogger.error("Error in insertIntoPostings :->" + e.getMessage());
        }
    }

    private void saveIntoDayBook(TranxReceiptPerticulars mReceipt) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(mReceipt.getOutlet());
        if (mReceipt.getBranch() != null) dayBook.setBranch(mReceipt.getBranch());
        dayBook.setAmount(mReceipt.getCr());
        LocalDate trDt = DateConvertUtil.convertDateToLocalDate(mReceipt.getTranxReceiptMaster().getTranscationDate());
        dayBook.setTranxDate(trDt);
        dayBook.setParticulars(mReceipt.getLedgerMaster().getLedgerName());
        dayBook.setVoucherNo(mReceipt.getTranxReceiptMaster().getReceiptNo());
        dayBook.setVoucherType("Receipt");
        dayBook.setStatus(true);
        try {
            daybookRepository.save(dayBook);
        } catch (Exception e) {
            receiptLogger.error("Error in Save into DayBook->" + e.getMessage());
        }
    }

    public JsonObject receiptListbyOutlet(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxReceiptMaster> payment = new ArrayList<>();
        if (users.getBranch() != null) {
            payment = repository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            payment = repository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
        }

        for (TranxReceiptMaster invoices : payment) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("receipt_code", invoices.getReceiptNo());
            response.addProperty("transaction_dt", invoices.getTranscationDate().toString());
            response.addProperty("narration", invoices.getNarrations() != null ? invoices.getNarrations() : "");
            response.addProperty("receipt_sr_no", invoices.getReceiptSrNo());
            List<TranxReceiptPerticulars> tranxReceiptPerticulars = tranxReceiptPerticularsRepository.findLedgerName(invoices.getId(), users.getOutlet().getId(), true);
            response.addProperty("total_amount", invoices.getTotalAmt());
            response.addProperty("ledger_name", tranxReceiptPerticulars != null &&
                    tranxReceiptPerticulars.size() > 0 ? tranxReceiptPerticulars.get(0).getLedgerName() : "");
            response.addProperty("narriation", invoices.getNarrations());
            response.addProperty("auto_generated", invoices.getTranxSalesInvoice() != null ? true : false);
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //start of receipt list with pagination
    public Object receiptListbyOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        System.out.println("startdate " + startDatep + "  endDate " + endDatep);
        List receipt = new ArrayList<>();
        List<TranxReceiptMaster> receiptArrayList = new ArrayList<>();
        List<ReceiptDTO> receiptDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT id FROM `tranx_receipt_master_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }
            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND narration LIKE '%" + searchText + "%'";
            }
            String jsonToStr = request.get("sort");
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
            } else {
                query = query + " ORDER BY id ASC";
            }
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;
            Query q = entityManager.createNativeQuery(query);
            receipt = q.getResultList();
           /* Query q1 = entityManager.createNativeQuery(query1, TranxReceiptMaster.class);
            receiptArrayList = q1.getResultList();
            Integer total_pages = (receiptArrayList.size() / pageSize);
            if ((receiptArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
*/

            String query1 = "SELECT COUNT(tranx_receipt_master_tbl.id) as totalcount FROM tranx_receipt_master_tbl " +
                    "WHERE tranx_receipt_master_tbl.status=? " + "AND tranx_receipt_master_tbl.outlet_id=?";
            if (users.getBranch() != null) {
                query1 = query1 + " AND tranx_receipt_master_tbl.branch_id=?";
            } else {
                query1 = query1 + " AND tranx_receipt_master_tbl.branch_id IS NULL";
            }
            Query qv = entityManager.createNativeQuery(query1);
            qv.setParameter(1, true);
            qv.setParameter(2, users.getOutlet().getId());
            if (users.getBranch() != null)
                qv.setParameter(3, users.getOutlet().getId());
            int totalresult = ((BigInteger) qv.getSingleResult()).intValue();
            Integer total_pages = (totalresult / pageSize);
            if ((totalresult % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            for (Object mObject : receipt) {
                TranxReceiptMaster invoiceListView = tranxReceiptMasterRepository.findByIdAndStatus(Long.parseLong(mObject.toString()), true);
                receiptDTOList.add(convertToDTDTO(invoiceListView));
            }
            GenericDatatable<ReceiptDTO> data = new GenericDatatable<>(receiptDTOList, receiptArrayList.size(),
                    pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            genericDTData.setRows(receiptDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    //end of receipt list with pagination
    //start of DTO for receipt list
    private ReceiptDTO convertToDTDTO(TranxReceiptMaster tranxReceiptMaster) {
        ReceiptDTO receiptDTO = new ReceiptDTO();
        try {
            receiptDTO.setId(tranxReceiptMaster.getId());
            receiptDTO.setReceipt_code(tranxReceiptMaster.getReceiptNo());
            receiptDTO.setTransaction_dt(DateConvertUtil.convertDateToLocalDate(tranxReceiptMaster.getTranscationDate()).toString());
            receiptDTO.setNarration(tranxReceiptMaster.getNarrations() != null ? tranxReceiptMaster.getNarrations() : "");
            receiptDTO.setReceipt_sr_no(tranxReceiptMaster.getReceiptSrNo());
            receiptDTO.setTotal_amount(tranxReceiptMaster.getTotalAmt());
            receiptDTO.setAuto_generated(tranxReceiptMaster.getTranxSalesInvoice() != null ? true : false);
            receiptDTO.setIsFrReceipt(tranxReceiptMaster.getIsFrReceipt() != null ? tranxReceiptMaster.getIsFrReceipt() : false);


            List<TranxReceiptPerticulars> tranxReceiptPerticulars = tranxReceiptPerticularsRepository.findLedgerName(tranxReceiptMaster.getId(), tranxReceiptMaster.getOutlet().getId(), true);
            receiptDTO.setLedger_name(tranxReceiptPerticulars != null && tranxReceiptPerticulars.size() > 0 ? tranxReceiptPerticulars.get(0).getLedgerName() : "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return receiptDTO;

    }
    //end of DTO for receipt list

    public JsonObject updateReceipt(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        TranxReceiptMaster tranxReceipt = repository.findByIdAndStatus(Long.parseLong(request.getParameter("receiptId")), true);
        dbList = transactionDetailsRepository.findByTransactionId(tranxReceipt.getId(), tranxType.getId());
        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(
                tranxReceipt.getId(), tranxType.getId(), true);
        JsonObject response = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            tranxReceipt.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("transaction_dt"));
        if (tranxDate.isEqual(DateConvertUtil.convertDateToLocalDate(tranxReceipt.getTranscationDate()))) {
            strDt = tranxReceipt.getTranscationDate();
        }
        tranxReceipt.setTranscationDate(strDt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            tranxReceipt.setFiscalYear(fiscalYear);
            tranxReceipt.setFinancialYear(fiscalYear.getFiscalYear());
        }
        tranxReceipt.setReceiptSrNo(Long.parseLong(request.getParameter("receipt_sr_no")));
        if (paramMap.containsKey("narration")) tranxReceipt.setNarrations(request.getParameter("narration"));
        tranxReceipt.setReceiptNo(request.getParameter("receipt_code"));
        tranxReceipt.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
        TranxReceiptMaster tranxReceiptMaster = repository.save(tranxReceipt);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("row");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                JsonObject receiptRow = row.get(i).getAsJsonObject();
                /*Receipt Perticulars */
                TranxReceiptPerticulars tranxReceiptPerticulars = null;
                Long detailsId = 0L;
                if (receiptRow.get("perticulars").getAsJsonObject().has("details_id"))
                    detailsId = receiptRow.get("perticulars").getAsJsonObject().get("details_id").getAsLong();
                if (detailsId != 0) {
                    tranxReceiptPerticulars = tranxReceiptPerticularsRepository.findByIdAndStatus(detailsId, true);
                } else {
                    tranxReceiptPerticulars = new TranxReceiptPerticulars();
                    tranxReceiptPerticulars.setStatus(true);
                }
                LedgerMaster ledgerMaster = null;
                tranxReceiptPerticulars.setBranch(branch);
                tranxReceiptPerticulars.setOutlet(outlet);
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(receiptRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null) tranxReceiptPerticulars.setLedgerMaster(ledgerMaster);
                tranxReceiptPerticulars.setTranxReceiptMaster(tranxReceiptMaster);
                tranxReceiptPerticulars.setType(receiptRow.get("type").getAsString());
                tranxReceiptPerticulars.setLedgerType(receiptRow.get("perticulars").getAsJsonObject().get("type").getAsString());
                tranxReceiptPerticulars.setLedgerName(receiptRow.get("perticulars").getAsJsonObject().get("ledger_name").getAsString());
                JsonObject perticulars = receiptRow.get("perticulars").getAsJsonObject();
                if (receiptRow.get("type").getAsString().equalsIgnoreCase("dr")) {
                    tranxReceiptPerticulars.setDr(receiptRow.get("paid_amt").getAsDouble());
                }
                if (receiptRow.get("type").getAsString().equalsIgnoreCase("cr")) {
                    tranxReceiptPerticulars.setCr(receiptRow.get("paid_amt").getAsDouble());
                }
                if (receiptRow.has("bank_payment_no") &&
                        !receiptRow.get("bank_payment_no").getAsString().equalsIgnoreCase("")) {
                    tranxReceiptPerticulars.setPaymentTranxNo(receiptRow.get("bank_payment_no").getAsString());
                } else {
                    tranxReceiptPerticulars.setPaymentTranxNo("");
                }
                if (receiptRow.has("bank_payment_type") &&
                        !receiptRow.get("bank_payment_type").getAsString().equalsIgnoreCase("")) {
                    PaymentModeMaster paymentModeMaster = paymentModeMasterRepository.findById(
                            receiptRow.get("bank_payment_type").getAsLong()).get();
                    tranxReceiptPerticulars.setPaymentMethod(paymentModeMaster.getPaymentMode());
                }
                if (receiptRow.has("bank_name") &&
                        !receiptRow.get("bank_name").getAsString().equalsIgnoreCase("")) {
                    LedgerBankDetails bankDetails = ledgerBankDetailsRepository.
                            findByIdAndStatus(receiptRow.get("bank_name").getAsLong(), true);
                    tranxReceiptPerticulars.setBankName(bankDetails.getBankName());
                }
                if (receiptRow.has("payment_date") &&
                        !receiptRow.get("payment_date").getAsString().equalsIgnoreCase("") &&
                        !receiptRow.get("payment_date").getAsString().toLowerCase().contains("invalid"))
                    tranxReceiptPerticulars.setPaymentDate(LocalDate.parse(receiptRow.get("payment_date").getAsString()));
                tranxReceiptPerticulars.setTransactionDate(tranxDate);
                if (perticulars.has("payableAmt"))
                    tranxReceiptPerticulars.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxReceiptPerticulars.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxReceiptPerticulars.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxReceiptPerticulars.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                TranxReceiptPerticulars mParticular = tranxReceiptPerticularsRepository.save(tranxReceiptPerticulars);
                total_amt = receiptRow.get("paid_amt").getAsDouble();
                /*Receipt Bills Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxSalesInvoice mSaleInvoice = null;
                            Long bill_details_id = 0L;
                            TranxReceiptPerticularsDetails tranxRptDetails = null;
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("sales_invoice")) {
                                bill_details_id = jsonBill.get("bill_details_id").getAsLong();
                                if (bill_details_id == 0) {
                                    tranxRptDetails = new TranxReceiptPerticularsDetails();
                                    tranxRptDetails.setStatus(true);
                                    tranxRptDetails.setCreatedBy(users.getId());
                                } else {
                                    tranxRptDetails = tranxReceiptPerticularsDetailsRepository.
                                            findByStatusAndId(true, bill_details_id);
                                }
                            }
                            if (ledgerMaster != null) tranxRptDetails.setLedgerMaster(ledgerMaster);
                            tranxRptDetails.setTranxReceiptMaster(tranxReceiptMaster);
                            tranxRptDetails.setTranxReceiptPerticulars(mParticular);
                            tranxRptDetails.setType(jsonBill.get("source").getAsString());
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxRptDetails.setType("new_reference");
                            }

                            tranxRptDetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());
                            tranxRptDetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxRptDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString()));
                            tranxRptDetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    createCreditNote(tranxReceiptMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "update");
                                } else {
                                    mSaleInvoice = tranxSalesInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mSaleInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxSalesInvoiceRepository.save(mSaleInvoice);
                                    }
                                }
                                tranxRptDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxRptDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxRptDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxRptDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            } else if (jsonBill.get("source").getAsString().equalsIgnoreCase("credit_note")) {
                                TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReference =
                                        tranxCreditNoteNewReferenceRepository.findByIdAndStatus(
                                                jsonBill.get("invoice_id").getAsLong(), true);
                                tranxRptDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxRptDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxRptDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxRptDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxCreditNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxCreditNoteNewReference.setTransactionStatusId(transactionStatus.getId());
                                        tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteNewReference);
                                    }
                                }
                            } else if (jsonBill.get("source").getAsString().equalsIgnoreCase("debit_note")) {
                                TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = tranxDebitNoteNewReferenceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxDebitNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
                                        tranxDebitNoteNewReferenceRepository.save(tranxDebitNoteNewReference);
                                    }
                                }
                            } else if (jsonBill.get("source").getAsString().equalsIgnoreCase("opening_balance")) {
                                LedgerOpeningBalance mOpening = ledgerOpeningBalanceRepository.findByOpeningBalInvoice(ledgerMaster.getId(),
                                        jsonBill.get("invoice_no").getAsString(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    mOpening.setInvoice_bal_amt(jsonBill.get("remaining_amt").getAsDouble());
                                    ledgerOpeningBalanceRepository.save(mOpening);
                                }
                                tranxRptDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxRptDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxRptDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxRptDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            }

                            // save into tranxRptDetails
                            tranxReceiptPerticularsDetailsRepository.save(tranxRptDetails);
                        }
                    }
                }
                if (perticulars.has("deleteRow")) {
                    JsonArray deletedInvoicesJson = perticulars.get("deleteRow").getAsJsonArray();
                    for (JsonElement mInvoiceDel : deletedInvoicesJson) {
                        String data[] = mInvoiceDel.getAsString().split(",");
                        Long invoiceId = Long.parseLong(data[1]);
                        if (invoiceId != 0) {
                            TranxSalesInvoice salesInvoice = tranxSalesInvoiceRepository.findByIdAndStatus(
                                    invoiceId, true);
                            if (salesInvoice != null) {
                                salesInvoice.setBalance(salesInvoice.getTotalAmount());
                                tranxSalesInvoiceRepository.save(salesInvoice);
                                /**** make status=0 against the purchase invoice id,and payment masterid
                                 in TranxPayment Details table so that in bills list we don't get the unselected bill again ****/
                                TranxReceiptPerticularsDetails details = tranxReceiptPerticularsDetailsRepository.
                                        findByTranxReceiptMasterIdAndTranxInvoiceIdAndStatus(tranxReceiptMaster.getId(),
                                                invoiceId, true);
                                details.setStatus(false);
                                tranxReceiptPerticularsDetailsRepository.save(details);
                            }
                        } else {
                            TranxCreditNoteNewReferenceMaster tranxCreditNoteMaster =
                                    tranxCreditNoteNewReferenceRepository.findByReceiptIdAndStatus(
                                            tranxReceiptMaster.getId(), true);
                            if (tranxCreditNoteMaster != null) {
                                tranxCreditNoteMaster.setBalance(tranxCreditNoteMaster.getTotalAmount());
                                tranxCreditNoteMaster.setStatus(false);
                                tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteMaster);

                            }
                        }
                    }
                }
                if (perticulars.has("creditdeleteRow")) {
                    JsonArray deletedCreditJson = perticulars.get("creditdeleteRow").getAsJsonArray();
                    for (JsonElement mCreditNoteDel : deletedCreditJson) {
                        String data[] = mCreditNoteDel.getAsString().split(",");
                        Long invoiceId = Long.parseLong(data[1]);
                        TranxCreditNoteNewReferenceMaster creditNote = tranxCreditNoteNewReferenceRepository.findByIdAndStatus(
                                invoiceId, true);
                        if (creditNote != null) {
                            creditNote.setBalance(creditNote.getTotalAmount());
                            TransactionStatus transactionStatus = transactionStatusRepository.findByIdAndStatus(1L, true);
                            creditNote.setTransactionStatusId(transactionStatus.getId());
                            tranxCreditNoteNewReferenceRepository.save(creditNote);
                            /**** make status=0 against the creditnote invoice id, and payment master id ,
                             in TranxReceipt Details table so that in bills list we don't get the unselected bill again ****/
                            TranxReceiptPerticularsDetails details = tranxReceiptPerticularsDetailsRepository.
                                    findByTranxReceiptMasterIdAndTranxInvoiceIdAndStatus(tranxReceiptMaster.getId(),
                                            mCreditNoteDel.getAsLong(), true);
                            details.setStatus(false);
                            tranxReceiptPerticularsDetailsRepository.save(details);
                        }
                    }
                }
                if (perticulars.has("OpeningDeleteRow")) {
                    JsonArray deletedDebitJson = perticulars.get("OpeningDeleteRow").getAsJsonArray();
                    for (JsonElement mDebitNoteDel : deletedDebitJson) {
                        String data[] = mDebitNoteDel.getAsString().split(",");
                        Long invoiceId = Long.parseLong(data[1]);
                        LedgerOpeningBalance openingMaster = ledgerOpeningBalanceRepository.findByIdAndStatus(
                                invoiceId, true);
                        if (openingMaster != null) {
//                            openingMaster.setBalance(openingMaster.getTotalAmount());
                            openingMaster.setStatus(false);

                            TransactionStatus transactionStatus = transactionStatusRepository.findByIdAndStatus(1L, true);
//                            openingMaster.setTransactionStatus(transactionStatus);
                            ledgerOpeningBalanceRepository.save(openingMaster);
                            /**** make status=0 against the debinote invoice id, and payment master id ,
                             in TranxPayment Details table so that in bills list we don't get the unselected bill again ****/
//                            TranxPaymentPerticularsDetails details = tranxPaymentPerticularsDetailsRepository.
//                                    findByTranxPaymentMasterIdAndTranxInvoiceIdAndStatus(tranxPaymentMaster.getId(),
//                                            mDebitNoteDel.getAsLong(), true);
//                            details.setStatus(false);
//                            tranxPaymentPerticularsDetailsRepository.save(details);
                        }
                    }
                }
                updateIntoPostings(mParticular, total_amt, detailsId);
            }


            /* Remove all ledgers from DB if we found new input ledger id's while updating */
            for (Long mDblist : ledgerList) {
                if (!ledgerInputList.contains(mDblist)) {
                    receiptLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mDblist, tranxType.getId(), tranxReceipt.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), tranxReceipt.getTranscationDate(),
                                tranxType, tranxReceipt.getFiscalYear(), detail);
                    }
                    receiptLogger.info("removing unused previous ledger update done");
                }
            }
            response.addProperty("message", "Receipt successfully updated..");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            receiptLogger.error("Error in update Receipt :->" + e.getMessage());
            response.addProperty("message", "Error in receipt updation");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return response;
    }

    public JsonObject getReceiptById(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxReceiptPerticulars> list = new ArrayList<>();
        JsonArray units = new JsonArray();
        List<TranxReceiptPerticularsDetails> detailsList = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        Long ledgerSDId = 0L;
        try {
            Long receiptId = Long.parseLong(request.getParameter("receipt_id"));
            TranxReceiptMaster tranxReceiptMaster = repository.findByIdAndOutletIdAndStatus(receiptId, users.getOutlet().getId(), true);
            list = tranxReceiptPerticularsRepository.findByTranxReceiptMasterIdAndStatus(tranxReceiptMaster.getId(), true);
            finalResult.addProperty("receipt_id", tranxReceiptMaster.getId());
            finalResult.addProperty("receipt_no", tranxReceiptMaster.getReceiptNo());
            finalResult.addProperty("receipt_sr_no", Long.valueOf((long) tranxReceiptMaster.getReceiptSrNo()));
            finalResult.addProperty("tranx_date", DateConvertUtil.convertDateToLocalDate(tranxReceiptMaster.getTranscationDate()).toString());
            finalResult.addProperty("total_amt", tranxReceiptMaster.getTotalAmt());
            finalResult.addProperty("narrations", tranxReceiptMaster.getNarrations());
            JsonArray row = new JsonArray();
            JsonArray billsArray = new JsonArray();
            if (list.size() > 0) {
                for (TranxReceiptPerticulars mdetails : list) {
                    JsonObject rpdetails = new JsonObject();
                    ledgerSDId = mdetails.getLedgerMaster().getId();
                    rpdetails.addProperty("details_id", mdetails.getId());
                    rpdetails.addProperty("type", mdetails.getType());
                    rpdetails.addProperty("ledger_type", mdetails.getLedgerType());
                    rpdetails.addProperty("ledger_name", mdetails.getLedgerName());
                    rpdetails.addProperty("dr", mdetails.getDr());
                    rpdetails.addProperty("cr", mdetails.getCr());
                    JsonObject paymentType = new JsonObject();
                    if (mdetails.getPaymentMethod() != null && !mdetails.getPaymentMethod().equalsIgnoreCase("")) {
                        PaymentModeMaster paymentMode = paymentModeMasterRepository.
                                findByPaymentModeIgnoreCaseAndStatus(mdetails.getPaymentMethod(), true);
                        paymentType.addProperty("label", mdetails.getPaymentMethod());
                        paymentType.addProperty("value", paymentMode.getId());
                        rpdetails.add("bank_payment_type", paymentType);
                    } else {
                        rpdetails.add("bank_payment_type", paymentType);
                    }
                    JsonObject bankName = new JsonObject();
                    if (mdetails.getBankName() != null) {
//                        LedgerMaster ledgerBankDetails = ledgerRepository.findByIdAndStatus(ledgerSDId,  true);
                        bankName.addProperty("label", mdetails.getBankName());
                        bankName.addProperty("value", ledgerSDId);
                        rpdetails.add("bank_name", bankName);
                    } else {
                        rpdetails.addProperty("bank_name", "");
                    }
                    rpdetails.addProperty("paymentTranxNo", mdetails.getPaymentTranxNo());
                    rpdetails.addProperty("payment_date",
                            mdetails.getPaymentDate() != null ? mdetails.getPaymentDate().toString() : "");
                    rpdetails.addProperty("ledger_id", mdetails.getLedgerMaster().getId());
                    rpdetails.addProperty("balancingMethod", mdetails.getLedgerMaster().getBalancingMethod() != null ?
                            generateSlugs.getSlug(mdetails.getLedgerMaster().getBalancingMethod().getBalancingMethod()) : "");
                    rpdetails.addProperty("payableAmt", mdetails.getPayableAmt() != null ? mdetails.getPayableAmt() : 0.0);
                    rpdetails.addProperty("selectedAmt", mdetails.getSelectedAmt() != null ? mdetails.getSelectedAmt() : 0.0);
                    rpdetails.addProperty("remainingAmt", mdetails.getRemainingAmt() != null ? mdetails.getRemainingAmt() : 0.0);
                    rpdetails.addProperty("isAdvanceCheck", mdetails.getIsAdvance()!= null ? mdetails.getIsAdvance() : false);
                    if (mdetails.getType().equalsIgnoreCase("cr")) {
                        detailsList = tranxReceiptPerticularsDetailsRepository.
                                findByTranxReceiptPerticularsIdAndStatus(mdetails.getId(), true);
                        for (TranxReceiptPerticularsDetails mPerticular : detailsList) {
                            JsonObject mBill = new JsonObject();
                            mBill.addProperty("bill_details_id", mPerticular.getId());
                            /*if (mPerticular.getType().equalsIgnoreCase("credit_note")) {
                                mBill.addProperty("credit_note_id", mPerticular.getTranxInvoiceId());
                                mBill.addProperty("credit_note_no", mPerticular.getTranxNo());
                                mBill.addProperty("credit_paid_amt", mPerticular.getPaidAmt());
                                mBill.addProperty("credit_remaining_amt", mPerticular.getRemainingAmt());
                            } else {
                                mBill.addProperty("paid_amt", mPerticular.getPaidAmt());
                                mBill.addProperty("remaining_amt", mPerticular.getRemainingAmt());
                                mBill.addProperty("invoice_id", mPerticular.getTranxInvoiceId());
                                mBill.addProperty("invoice_no", mPerticular.getTranxNo());
                            }*/
                            mBill.addProperty("paid_amt", mPerticular.getPaidAmt() != null  ? mPerticular.getPaidAmt() : 0.0);
                            mBill.addProperty("remaining_amt", mPerticular.getRemainingAmt() != null  ? mPerticular.getRemainingAmt() : 0.0);
                            mBill.addProperty("invoice_id", mPerticular.getTranxInvoiceId());
                            mBill.addProperty("invoice_no", mPerticular.getTranxNo());
                            mBill.addProperty("ledger_id", mdetails.getLedgerMaster().getId());
                            mBill.addProperty("balancing_type", mPerticular.getBalancingType());
                            mBill.addProperty("invoice_date", mPerticular.getTransactionDate().toString());
                            mBill.addProperty("total_amt", mPerticular.getTotalAmt()!= null  ? mPerticular.getTotalAmt() : 0.0);
                            if (mPerticular.getType().equalsIgnoreCase("new_reference"))
                                mBill.addProperty("source", "sales_invoice");
                            else
                                mBill.addProperty("source", mPerticular.getType());
                            if (mPerticular.getType().equalsIgnoreCase("sales_invoice"))
                                mBill.addProperty("invoice_unique_id", "sales_invoice," + mPerticular.getTranxInvoiceId());
                            else if (mPerticular.getType().equalsIgnoreCase("opening_balance"))
                                mBill.addProperty("invoice_unique_id", "opening_balance," + mPerticular.getTranxInvoiceId());
                            else if (mPerticular.getType().equalsIgnoreCase("debit_note"))
                                mBill.addProperty("invoice_unique_id", "debit_note," + mPerticular.getTranxInvoiceId());
                            else if (mPerticular.getType().equalsIgnoreCase("new_reference"))
                                mBill.addProperty("invoice_unique_id", "new-ref," + mPerticular.getId());

                            mBill.addProperty("amount", mPerticular.getAmount());
                            billsArray.add(mBill);
                        }
                        rpdetails.add("bills", billsArray);
                    }else {
                        JsonArray billsArrayDr = new JsonArray();
                        rpdetails.add("bills", billsArrayDr);
                    }
                    row.add((rpdetails));
                }
            }

            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("perticulars", row);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            receiptLogger.error("Error in getReceiptById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            receiptLogger.error("Error in getReceiptById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    private void updateIntoPostings(TranxReceiptPerticulars mReceipt, double total_amt, Long detailsId) {
        LedgerMaster sundryCreditors = null;
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        try {
            Boolean isLedgerContains = false;
            String tranxAction = "CR";
            /* for Sundry Debtors  */
            if (mReceipt.getType().equalsIgnoreCase("cr")) {
                if (detailsId != 0) {
                    isLedgerContains = ledgerList.contains(mReceipt.getLedgerMaster().getId());
                    ledgerInputList.add(mReceipt.getLedgerMaster().getId());
                    isLedgerContains = true;
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                            findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mReceipt.getLedgerMaster().getId(),
                                    tranxType.getId(),
                                    mReceipt.getTranxReceiptMaster().getId());
                    if (mLedger != null) {
                        mLedger.setAmount(total_amt);
                        mLedger.setTransactionDate(mReceipt.getTranxReceiptMaster().getTranscationDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    //   transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(mReceipt.getLedgerMaster().getFoundations().getId(), mReceipt.getLedgerMaster().getPrinciples() != null ? mReceipt.getLedgerMaster().getPrinciples().getId() : null, mReceipt.getLedgerMaster().getPrincipleGroups() != null ? mReceipt.getLedgerMaster().getPrincipleGroups().getId() : null, null, tranxType.getId(), mReceipt.getLedgerMaster().getBalancingMethod() != null ? mReceipt.getLedgerMaster().getBalancingMethod().getId() : null, mReceipt.getBranch() != null ? mReceipt.getBranch().getId() : null, mReceipt.getOutlet().getId(), "NA", 0.0, total_amt, mReceipt.getTranxReceiptMaster().getTranscationDate(), null, mReceipt.getId(), tranxType.getTransactionName(), mReceipt.getLedgerMaster().getUnderPrefix(), mReceipt.getTranxReceiptMaster().getFinancialYear(), mReceipt.getCreatedBy(), mReceipt.getLedgerMaster().getId(), mReceipt.getTranxReceiptMaster().getReceiptNo());
                    ledgerCommonPostings.callToPostings(total_amt, mReceipt.getLedgerMaster(), tranxType,
                            mReceipt.getLedgerMaster().getAssociateGroups(),
                            mReceipt.getTranxReceiptMaster().getFiscalYear(), mReceipt.getBranch(),
                            mReceipt.getOutlet(), mReceipt.getTranxReceiptMaster().getTranscationDate(),
                            mReceipt.getTranxReceiptMaster().getId(),
                            mReceipt.getTranxReceiptMaster().getReceiptNo(),
                            "CR", true, "Receipt", "Insert");
                }


            } else {
                tranxAction = "DR";
                if (detailsId != 0) {
                    isLedgerContains = ledgerList.contains(mReceipt.getLedgerMaster().getId());
                    ledgerInputList.add(mReceipt.getLedgerMaster().getId());
                    isLedgerContains = true;
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                            findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mReceipt.getLedgerMaster().getId(),
                                    tranxType.getId(), mReceipt.getTranxReceiptMaster().getId());
                    if (mLedger != null) {
                        mLedger.setAmount(total_amt);
                        mLedger.setTransactionDate(mReceipt.getTranxReceiptMaster().getTranscationDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    /* for Cash and Bank Account  */
                    ledgerCommonPostings.callToPostings(total_amt, mReceipt.getLedgerMaster(), tranxType,
                            mReceipt.getLedgerMaster().getAssociateGroups(),
                            mReceipt.getTranxReceiptMaster().getFiscalYear(), mReceipt.getBranch(),
                            mReceipt.getOutlet(), mReceipt.getTranxReceiptMaster().getTranscationDate(),
                            mReceipt.getTranxReceiptMaster().getId(),
                            mReceipt.getTranxReceiptMaster().getReceiptNo(),
                            "DR", true, "Receipt", "Insert");
                }
            }

            Double amount = total_amt;
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, mReceipt.getLedgerMaster().getId(),
                    tranxType, tranxAction, mReceipt.getTranxReceiptMaster().getId(), mReceipt.getLedgerMaster(),
                    mReceipt.getTranxReceiptMaster().getTranscationDate(), mReceipt.getTranxReceiptMaster().getFiscalYear(),
                    mReceipt.getOutlet(), mReceipt.getBranch(), mReceipt.getTranxReceiptMaster().getTranxCode());
        } catch (Exception e) {
            e.printStackTrace();
            receiptLogger.error("Error in insertIntoPostings :->" + e.getMessage());
        }
    }

    public JsonObject deleteReceipt(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxReceiptMaster receiptMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        try {
            receiptMaster.setStatus(false);
            repository.save(receiptMaster);
            if (receiptMaster != null) {
                List<TranxReceiptPerticulars> tranxReceiptPerticulars = tranxReceiptPerticularsRepository.
                        findByTranxReceiptMasterIdAndStatus(receiptMaster.getId(), true);
                for (TranxReceiptPerticulars mDetail : tranxReceiptPerticulars) {
                    if (mDetail.getType().equalsIgnoreCase("CR"))
                        insertIntoPostings(mDetail, mDetail.getCr(), "DR", "Delete");// Accounting Postings
                    else
                        insertIntoPostings(mDetail, mDetail.getDr(), "CR", "Delete");// Accounting Postings
                }
                /**** make status=0 to all ledgers of respective Receipt voucher id, due to this we wont get
                 details of deleted invoice when we want get details of respective ledger ****/
                List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
                mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(tranxType.getId(), receiptMaster.getId(), true);
                for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                    try {
                        mPostings.setStatus(false);
                        ledgerTransactionPostingsRepository.save(mPostings);
                    } catch (Exception e) {
                        receiptLogger.error("Exception in Delete functionality for all ledgers of" + " deleted purchase invoice->" + e.getMessage());
                    }
                }
                jsonObject.addProperty("message", "Receipt invoice deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in receipt invoice deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            receiptLogger.error("Error in receipt invoice Delete()->" + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject mobileReceiptList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        String flag = "paymentList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<TranxReceiptPerticulars> balanceSummaries = new ArrayList<>();
        balanceSummaries = tranxReceiptPerticularsRepository.findByTypeAndStatus("cr", true);
        for (TranxReceiptPerticulars balanceSummary : balanceSummaries) {
            String type = balanceSummary.getType();
            Long ledgerId = balanceSummary.getLedgerMaster().getId();
            JsonObject jsonObject = new JsonObject();
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            endDatep = LocalDate.parse(request.get("end_date"));
            startDatep = LocalDate.parse(request.get("start_date"));

            try {
                list = tranxReceiptPerticularsRepository.findmobileReceiptTotalAmt(type, startDatep, endDatep, true);
                JsonArray innerArr = new JsonArray();
                for (int i = 0; i < list.size(); i++) {
                    JsonObject inside = new JsonObject();
                    Object[] objp = list.get(i);
                    sumTotal = Double.parseDouble(objp[0].toString());
                    totalInvoice = Integer.parseInt(objp[1].toString());
                    jsonObject.addProperty("TotalAmt", sumTotal);
                    jsonObject.addProperty("TotalInvoiceCount", totalInvoice);
                    jsonObject.addProperty("DebtorsId", ledgerId);
                    jsonObject.addProperty("flag", flag);
                    closingBalance = closingBalance + sumTotal;
                    //   innerArr.add(inside);
                }


            } catch (Exception e) {
                receiptLogger.error("Error in salesDelete()->" + e.getMessage());
            }
            jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());
            jsonObject.addProperty("date", balanceSummary.getTransactionDate().toString());
            if (sumTotal > 0)
                result.add(jsonObject);
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

    public JsonObject mobileReceiptInvoiceList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        String flag = "receiptList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<TranxReceiptPerticulars> balanceSummaries = new ArrayList<>();
        LocalDate endDatep = null;
        LocalDate startDatep = null;
        endDatep = LocalDate.parse(request.get("end_date"));
        startDatep = LocalDate.parse(request.get("start_date"));
        Long ledgerId = Long.valueOf(request.get("DebtorsId"));

        balanceSummaries = tranxReceiptPerticularsRepository.findByLedgerMasterAndStatus(ledgerId, startDatep, endDatep, true);
        for (TranxReceiptPerticulars balanceSummary : balanceSummaries) {
            String type = balanceSummary.getType();
            Long masterId = balanceSummary.getTranxReceiptMaster().getId();
            JsonObject jsonObject = new JsonObject();

            try {
                TranxReceiptMaster tranxReceiptMaster = tranxReceiptMasterRepository.findByIdAndStatus(masterId, true);
                jsonObject.addProperty("Receipt_No", tranxReceiptMaster.getReceiptNo());
                jsonObject.addProperty("Total_Amt", tranxReceiptMaster.getTotalAmt());
                jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());


                TranxReceiptPerticulars tranxReceiptPerticulars = tranxReceiptPerticularsRepository.findByTranxReceiptMasterIdAndTypeAndStatus(masterId, "dr", true);

                jsonObject.addProperty("cashAcc", tranxReceiptPerticulars.getLedgerName());
                jsonObject.addProperty("cashAmount", tranxReceiptPerticulars.getDr());

//            if (sumTotal > 0)
                result.add(jsonObject);

                //   innerArr.add(inside);


            } catch (Exception e) {
                receiptLogger.error("Error in salesDelete()->" + e.getMessage());
            }

        }
        JsonObject json = new JsonObject();
        LocalDate today = LocalDate.now();
//        json.addProperty("closingBalance", closingBalance);
        json.addProperty("todayDate", String.valueOf(today));
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public Object validateReceipt(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxReceiptMaster receiptMaster = null;
        if (users.getBranch() != null) {
            receiptMaster = repository.findByOutletIdAndBranchIdAndReceiptNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("receiptNo"));
        } else {
            receiptMaster = repository.findByOutletIdAndReceiptNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), request.getParameter("receiptNo"));
        }
        if (receiptMaster != null) {
            // System.out.println("Already Ledger created with this name or code");
            responseMessage.setMessage("Duplicate receipt invoice number exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New receipt invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object validateReceiptUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Long invoiceId = Long.parseLong(request.getParameter("voucher_id"));
        TranxReceiptMaster receiptMaster = null;
        if (users.getBranch() != null) {
            receiptMaster = repository.findByOutletIdAndBranchIdAndReceiptNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("receiptNo"));
        } else {
            receiptMaster = repository.findByOutletIdAndReceiptNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), request.getParameter("receiptNo"));
        }
        if (receiptMaster != null && invoiceId != receiptMaster.getId()) {
            responseMessage.setMessage("Duplicate receipt invoice number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales receipt number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public JsonObject createReceipAgainstFR(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        String franchiseUser = request.getParameter("companyCode");

        JsonObject response = new JsonObject();
        TranxReceiptMaster tranxReceipt = new TranxReceiptMaster();
        Users cadmin = usersRepository.findTop1ByUserRoleIgnoreCaseAndCompanyCode("cadmin", "gvmh001");
        String cadminToken = jwtRequestFilter.getTokenFromUsername(cadmin.getUsername());
        Branch branch = null;
        if (cadmin.getBranch() != null) {
            tranxReceipt.setBranch(cadmin.getBranch());
            branch = cadmin.getBranch();
        }
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        Outlet outlet = cadmin.getOutlet();
        tranxReceipt.setOutlet(outlet);
      /*  HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("branch", "gvmh001");
        headers.add("Authorization", "Bearer " + cadminToken);*/
        Long count = 0L;

        if (cadmin.getBranch() != null) {
            count = repository.findBranchLastRecord(cadmin.getOutlet().getId(), cadmin.getBranch().getId());
        } else {
            count = repository.findLastRecord(cadmin.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String receiptCode = "RCPT" + currentMonth + serailNo;
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("transaction_dt"));
        tranxReceipt.setTranscationDate(strDt);
        tranxReceipt.setStatus(true);
        /*     fiscal year mapping  */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            tranxReceipt.setFiscalYear(fiscalYear);
            tranxReceipt.setFinancialYear(fiscalYear.getFiscalYear());
        }
        tranxReceipt.setReceiptSrNo(count + 1);
        if (paramMap.containsKey("narration")) tranxReceipt.setNarrations(request.getParameter("narration"));
        else {
            tranxReceipt.setNarrations(request.getParameter(""));
        }
        tranxReceipt.setReceiptNo(receiptCode);
        tranxReceipt.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
        tranxReceipt.setCreatedBy(cadmin.getId());
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        tranxReceipt.setTranxCode(tranxCode);
        tranxReceipt.setIsFrReceipt(true);
        tranxReceipt.setGvPaymentDate(LocalDate.parse(request.getParameter("payment_dt")));
        tranxReceipt.setGvPaymentTranxNo(request.getParameter("gv_payment_tranx_no"));
        PaymentModeMaster mPaymentMode = paymentModeMasterRepository.findByPaymentModeIgnoreCaseAndStatus(request.getParameter("gv_bank_payment_type_name"), true);
        if (mPaymentMode == null) {
            mPaymentMode = new PaymentModeMaster();
            mPaymentMode.setPaymentMode(request.getParameter("gv_bank_payment_type_name"));
            mPaymentMode.setStatus(true);
            mPaymentMode.setOutletId(outlet.getId());
            if (branch != null)
                mPaymentMode.setBranchId(branch.getId());
            paymentModeMasterRepository.save(mPaymentMode);

        }
        tranxReceipt.setGvPaymentMode(mPaymentMode.getPaymentMode());
        /***** finding GV Bank Ledger from Ledger Master ******/
        LedgerMaster bankLedgerMaster = ledgerMasterRepository.findByAccountNumberAndStatus(request.getParameter("gv_bank_name"), true);
        tranxReceipt.setGvBankName(bankLedgerMaster != null ? bankLedgerMaster.getLedgerName() : "");
        tranxReceipt.setGvBankLedgerId(bankLedgerMaster.getId());


//        tranxReceipt.sets
        TranxReceiptMaster tranxReceiptMaster = repository.save(tranxReceipt);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("row");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < (row.size() - 1); i++) {
                String crdrType = "";
                /*Receipt Master */
                JsonObject receiptRow = row.get(i).getAsJsonObject();
                /*Receipt Perticulars */
                TranxReceiptPerticulars tranxReceiptPerticulars = new TranxReceiptPerticulars();
                LedgerMaster ledgerMaster = null;
                tranxReceiptPerticulars.setBranch(branch);
                tranxReceiptPerticulars.setOutlet(outlet);
                tranxReceiptPerticulars.setStatus(true);
              /*  ledgerMaster = ledgerMasterRepository.findByIdAndStatus(
                        receiptRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);*/
                /**** getting Franchise Ledger using Company Code ****/
                ledgerMaster = ledgerMasterRepository.findByLedgerCodeAndUniqueCodeAndOutletIdAndStatus(
                        franchiseUser, "SUDR", outlet.getId(), true);
                if (ledgerMaster != null) tranxReceiptPerticulars.setLedgerMaster(ledgerMaster);
                tranxReceiptPerticulars.setTranxReceiptMaster(tranxReceiptMaster);
                tranxReceiptPerticulars.setType("CR");
                tranxReceiptPerticulars.setLedgerType("SD");
                tranxReceiptPerticulars.setLedgerName(ledgerMaster.getLedgerName());
                tranxReceiptPerticulars.setTransactionDate(tranxDate);
                if (receiptRow.has("type"))
                    crdrType = receiptRow.get("type").getAsString();
                JsonObject perticulars = receiptRow.get("perticulars").getAsJsonObject();
                if (!receiptRow.get("paid_amt").getAsString().equalsIgnoreCase(""))
                    total_amt = receiptRow.get("paid_amt").getAsDouble();
                else {
                    total_amt = 0.0;
                }
                if (crdrType.equalsIgnoreCase("dr") && !receiptRow.get("paid_amt").getAsString().equalsIgnoreCase("")) {
                    tranxReceiptPerticulars.setCr(total_amt);
                }
                /***** saving Bank ledger Details from row *****/
                if (crdrType.equalsIgnoreCase("cr") && !receiptRow.get("paid_amt").getAsString().equalsIgnoreCase("")) {
                    tranxReceiptPerticulars.setCr(total_amt);
                }
                if (receiptRow.has("bank_payment_no") &&
                        !receiptRow.get("bank_payment_no").getAsString().equalsIgnoreCase("")) {
                    tranxReceiptPerticulars.setPaymentTranxNo(receiptRow.get("bank_payment_no").getAsString());
                } else {
                    tranxReceiptPerticulars.setPaymentTranxNo("");
                }
                if (receiptRow.has("bank_payment_type") &&
                        !receiptRow.get("bank_payment_type").getAsString().equalsIgnoreCase("")) {
                    PaymentModeMaster paymentModeMaster = paymentModeMasterRepository.findById(
                            receiptRow.get("bank_payment_type").getAsLong()).get();
                    if (paymentModeMaster != null)
                        tranxReceiptPerticulars.setPaymentMethod(paymentModeMaster.getPaymentMode());
                }
                if (receiptRow.has("bank_name") &&
                        !receiptRow.get("bank_name").getAsString().equalsIgnoreCase("")) {
                    LedgerBankDetails bankDetails = ledgerBankDetailsRepository.
                            findByIdAndStatus(receiptRow.get("bank_name").getAsLong(), true);
                    if (bankDetails != null)
                        tranxReceiptPerticulars.setBankName(bankDetails.getBankName());
                }
                if (receiptRow.has("payment_date") &&
                        !receiptRow.get("payment_date").getAsString().equalsIgnoreCase("") &&
                        !receiptRow.get("payment_date").getAsString().toLowerCase().contains("invalid"))
                    tranxReceiptPerticulars.setPaymentDate(LocalDate.parse(receiptRow.get("payment_date").getAsString()));
                /***** End saving Bank ledger Details from row *****/
                tranxReceiptPerticulars.setCreatedBy(cadmin.getId());
                if (perticulars.has("payableAmt"))
                    tranxReceiptPerticulars.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxReceiptPerticulars.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxReceiptPerticulars.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxReceiptPerticulars.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                TranxReceiptPerticulars mParticular = tranxReceiptPerticularsRepository.save(tranxReceiptPerticulars);

                /*Receipt Perticulars Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            TranxReceiptPerticularsDetails tranxRptDetails = new TranxReceiptPerticularsDetails();
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxSalesInvoice mSaleInvoice = null;
                            tranxRptDetails.setBranch(branch);
                            tranxRptDetails.setOutlet(outlet);
                            if (ledgerMaster != null) tranxRptDetails.setLedgerMaster(ledgerMaster);
                            tranxRptDetails.setTranxReceiptMaster(tranxReceiptMaster);
                            tranxRptDetails.setTranxReceiptPerticulars(mParticular);
                            tranxRptDetails.setStatus(true);
                            tranxRptDetails.setCreatedBy(cadmin.getId());
                            tranxRptDetails.setType("sales_invoice");
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxRptDetails.setType("new_reference");
                            }
                            tranxRptDetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());
                            tranxRptDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString()));
                            tranxRptDetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxRptDetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            tranxRptDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString()));
                            /**** this is auto convert into receipt from payment of franchise therefore source is pur_invoice ****/
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    createCreditNote(tranxReceiptMaster, jsonBill.get("amount").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "create");
                                } else {
                                    String tranxTrackCode = jsonBill.get("tranx_tracking_code").getAsString();
                                    mSaleInvoice = tranxSalesInvoiceRepository.findByTransactionTrackingNoAndStatus(tranxTrackCode, true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mSaleInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxSalesInvoiceRepository.save(mSaleInvoice);
                                    }
                                }
                                tranxRptDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxRptDetails.setTranxInvoiceId(mSaleInvoice.getId());
                                tranxRptDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxRptDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                                tranxReceiptPerticularsDetailsRepository.save(tranxRptDetails);
                            } /*else if (jsonBill.get("source").getAsString().equalsIgnoreCase("debit_note")) {
                                TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReference = tranxCreditNoteNewReferenceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxCreditNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxCreditNoteNewReference.setTransactionStatusId(transactionStatus.getId());
                                        tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteNewReference);
                                    }
                                }
                            }*/ /*else if (jsonBill.get("source").getAsString().equalsIgnoreCase("opening_balance")) {
                                LedgerOpeningBalance mOpening = ledgerOpeningBalanceRepository.findByOpeningBalInvoice(ledgerMaster.getId(),
                                        jsonBill.get("invoice_no").getAsString(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    mOpening.setInvoice_bal_amt(jsonBill.get("remaining_amt").getAsDouble());
                                    ledgerOpeningBalanceRepository.save(mOpening);
                                }
                            }*/

                        }
                    }
                }
                //  insertIntoPostings(mParticular, total_amt, crdrType, "Insert");//Accounting Postings
            }

            /**** saving Bank Ledger Details of GV into Receipt Particular Table *****/
            TranxReceiptPerticulars tranxReceiptPerticulars = new TranxReceiptPerticulars();
//            LedgerMaster bankLedger = null;
            tranxReceiptPerticulars.setBranch(branch);
            tranxReceiptPerticulars.setOutlet(outlet);
            tranxReceiptPerticulars.setStatus(true);
//            LedgerBankDetails bankDetails = ledgerBankDetailsRepository.findByIdAndStatus(tranxReceiptMaster.getGvBankLedgerId(), true);
            LedgerMaster mLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndStatus(tranxReceipt.getGvBankName(), true);
            if (mLedger != null) {
                tranxReceiptPerticulars.setBankName(mLedger.getBankName());
            }
            if (mLedger != null) {
                tranxReceiptPerticulars.setLedgerMaster(mLedger);
                tranxReceiptPerticulars.setLedgerName(mLedger.getLedgerName());
            }
            tranxReceiptPerticulars.setTranxReceiptMaster(tranxReceiptMaster);
            tranxReceiptPerticulars.setType("DR");
            tranxReceiptPerticulars.setLedgerType("others");

            tranxReceiptPerticulars.setTransactionDate(tranxDate);
            tranxReceiptPerticulars.setDr(total_amt);
            tranxReceiptPerticulars.setCreatedBy(cadmin.getId());
            TranxReceiptPerticulars mParticular = tranxReceiptPerticularsRepository.save(tranxReceiptPerticulars);
            response.addProperty("message", "Receipt successfully Done..");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            receiptLogger.error("Error in createReceipt :->" + e.getMessage());
            response.addProperty("message", "Error in Receipt creation");
            response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return response;
    }

    public JsonObject receiptPosting(HttpServletRequest request) {
        ResponseMessage message = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String receiptId = request.getParameter("receiptId");
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<TranxReceiptPerticulars> receiptPerticulars = tranxReceiptPerticularsRepository.findByReceiptIdAndStatus(receiptId, true);
        int cnt = 0;
        for (TranxReceiptPerticulars rObject : receiptPerticulars) {
            /*JsonObject jsonObject=new JsonObject();

            jsonObject.addProperty("ledger_id",rObject.getLedgerMaster().getId());
            jsonObject.addProperty("type",rObject.getType());
            jsonObject.addProperty("ledger_type",rObject.getLedgerType());
            jsonObject.addProperty("ledger_name",rObject.getLedgerName());*/

//            result.add(jsonObject);
            TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
            try {
                /* for Sundry Debtors  */

                /**** New Postings Logic *****/
                if (rObject.getLedgerType().equalsIgnoreCase("SD")) {
                    ledgerCommonPostings.callToPostings(rObject.getCr(), rObject.getLedgerMaster(), tranxType,
                            rObject.getLedgerMaster().getAssociateGroups(),
                            rObject.getTranxReceiptMaster().getFiscalYear(), rObject.getBranch(),
                            rObject.getOutlet(), rObject.getTranxReceiptMaster().getTranscationDate(),
                            rObject.getTranxReceiptMaster().getId(),
                            rObject.getTranxReceiptMaster().getReceiptNo(), "CR", true,
                            tranxType.getTransactionCode(), "Insert");
                    cnt++;
                } else {
                    ledgerCommonPostings.callToPostings(rObject.getDr(), rObject.getLedgerMaster(), tranxType,
                            rObject.getLedgerMaster().getAssociateGroups(),
                            rObject.getTranxReceiptMaster().getFiscalYear(), rObject.getBranch(),
                            rObject.getOutlet(), rObject.getTranxReceiptMaster().getTranscationDate(),
                            rObject.getTranxReceiptMaster().getId(),
                            rObject.getTranxReceiptMaster().getReceiptNo(), "DR", true,
                            tranxType.getTransactionCode(), "Insert");
                    cnt++;
                }
                saveIntoDayBook(rObject);


            } catch (Exception e) {
                e.printStackTrace();
                receiptLogger.error("Error in insertIntoPostings :->" + e.getMessage());
            }
            if (cnt != 0) {
                res.addProperty("message", "Posting Success");
                res.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                res.addProperty("message", "Posting Error");
                res.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }

        }
        return res;
    }
}
