package in.truethics.ethics.ethicsapiv10.service.tranx_service.payment;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.accountentrydto.PaymentDTO;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.PaymentModeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionStatusRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionTypeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentPerticularsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceRepository;
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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service

public class TranxPaymentNewService {

    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private LedgerBalanceSummaryRepository ledgerBalanceSummaryRepository;
    @Autowired
    private GenerateSlugs generateSlugs;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;

    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private TranxDebitNoteNewReferenceRepository tranxDebitNoteNewReferenceRepository;

    @Autowired
    private TranxPaymentMasterRepository tranxPaymentMasterRepository;

    @Autowired
    private TranxPaymentPerticularsRepository tranxPaymentPerticularsRepository;

    @Autowired
    private TranxPaymentPerticularsDetailsRepository tranxPaymentPerticularsDetailsRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;

    @Autowired
    private TranxCreditNoteNewReferenceRepository tranxCreditNoteNewReferenceRepository;

    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;

    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;

    @Autowired
    private LedgerMasterRepository ledgerRepository;

    private static final Logger paymentLogger = LogManager.getLogger(TranxPaymentNewService.class);
    @Autowired
    private TranxDebitNoteDetailsRepository tranxDebitNoteDetailsRepository;
    @Autowired
    private LedgerOpeningBalanceRepository ledgerOpeningBalanceRepository;
    @Autowired
    private LedgerBankDetailsRepository ledgerBankDetailsRepository;
    @Autowired
    private PaymentModeMasterRepository paymentModeMasterRepository;
    @Autowired
    private PostingUtility postingUtility;
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;
    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    List<Long> ledgerInputList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB


    public JsonObject paymentLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxPaymentMasterRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxPaymentMasterRepository.findLastRecord(users.getOutlet().getId());
        }

        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String paymentCode = "PAYNT" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("payment_sr_no", count + 1);
        result.addProperty("payment_code", paymentCode);
        return result;
    }

    public JsonObject getSundryCreditorAndIndirectExpenses(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject finalResult = new JsonObject();
        List<LedgerMaster> sundryCreditors = new ArrayList<>();
        List<LedgerMaster> sundryDebtors = new ArrayList<>();
        if (users.getBranch() != null) {

            sundryCreditors = ledgerMasterRepository.findByOutletIdAndBranchIdAndPrincipleGroupsIdAndStatus(
                    users.getOutlet().getId(), users.getBranch().getId(), 5L, true);
            sundryDebtors = ledgerMasterRepository.findByOutletIdAndBranchIdAndPrincipleGroupsIdAndStatus(
                    users.getOutlet().getId(), users.getBranch().getId(), 1L, true);

        } else {
            sundryCreditors = ledgerMasterRepository.findByOutletIdAndPrincipleGroupsIdAndStatusAndBranchIsNull(users.getOutlet().getId(), 5L, true);
            sundryDebtors = ledgerMasterRepository.findByOutletIdAndPrincipleGroupsIdAndStatusAndBranchIsNull(users.getOutlet().getId(), 1L, true);
        }
        /* for Sundry Creditors List */
        if (sundryCreditors.size() > 0) {
            for (LedgerMaster mLedger : sundryCreditors) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mLedger.getId());
                response.addProperty("ledger_name", mLedger.getLedgerName());
                response.addProperty("balancing_method", generateSlugs.getSlug(mLedger.getBalancingMethod().getBalancingMethod()));
                response.addProperty("type", "SC");
             /*   LedgerBalanceSummary balanceSummary = ledgerBalanceSummaryRepository.findByLedgerMasterId(mLedger.getId());
                if (balanceSummary!= null) {
                    response.addProperty("balance", balanceSummary.getClosingBal());
                    if (balanceSummary.getClosingBal() > 0) response.addProperty("balance_typ", "CR");
                }
                else response.addProperty("balance_typ", "DR");*/
                result.add(response);
            }
        }  /* end of Sundry Creditors List */

        /* for Sundry debtor List*/
        if (sundryDebtors.size() > 0) {
            for (LedgerMaster mLedger : sundryDebtors) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mLedger.getId());
                response.addProperty("ledger_name", mLedger.getLedgerName());
                response.addProperty("balancing_method", mLedger.getBalancingMethod() != null ? generateSlugs.getSlug(mLedger.getBalancingMethod().getBalancingMethod()) : "");
                response.addProperty("type", "SD");
               /* LedgerBalanceSummary balanceSummary = ledgerBalanceSummaryRepository.findByLedgerMasterId(mLedger.getId());
                response.addProperty("balance", balanceSummary.getClosingBal());
                if (balanceSummary.getClosingBal() > 0) response.addProperty("balance_typ", "DR");
                else response.addProperty("balance_typ", "CR");*/
                result.add(response);
            }
        } /* end of Indirect Expenses List*/
        List<LedgerMaster> indirectExpenses = new ArrayList<>();
        indirectExpenses = ledgerMasterRepository.findByOutletIdAndPrinciplesIdAndStatus(users.getOutlet().getId(), 12L, true);
        if (indirectExpenses.size() > 0) {
            for (LedgerMaster mLedger : indirectExpenses) {
                if (!mLedger.getLedgerName().equalsIgnoreCase("Round Off") && !mLedger.getLedgerName().equalsIgnoreCase("Sales Discount")) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mLedger.getId());
                    response.addProperty("ledger_name", mLedger.getLedgerName());
                    response.addProperty("balancing_method", "NA");
                    response.addProperty("type", "IE");
                    result.add(response);
                }
            }
        }
        /**** Current Assets *****/
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


    public JsonObject getCashAcBankAccountDetails(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumCR = 0.0;
        Double sumDR = 0.0;
        List<LedgerMaster> ledgerMaster = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        if (users.getBranch() != null) {
            ledgerMaster = ledgerMasterRepository.findBranchBankAccountCashAccount(users.getOutlet().getId(), users.getBranch().getId());
        } else {

            ledgerMaster = ledgerMasterRepository.findBankAccountCashAccount(users.getOutlet().getId());
        }
        JsonObject response = new JsonObject();


        for (LedgerMaster mLedger : ledgerMaster) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", mLedger.getId());
            jsonObject.addProperty("name", mLedger.getLedgerName());
            jsonObject.addProperty("type", mLedger.getSlugName());

            Double openingBalance = ledgerRepository.findOpeningBalance(mLedger.getId());//1000
            sumCR = ledgerTransactionPostingsRepository.findsumCR(mLedger.getId());//-0.20
            sumDR = ledgerTransactionPostingsRepository.findsumDR(mLedger.getId());//-0.40
            closingBalance = openingBalance - sumDR + sumCR;//0-2
            jsonObject.addProperty("current_balance", Math.abs(closingBalance));
            if (mLedger.getFoundations().getId() == 1) { //DR
                if (closingBalance > 0) {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                    jsonObject.addProperty("ledger_type", "cr");

                } else {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("ledger_type", "dr");

                }

            } else if (mLedger.getFoundations().getId() == 2) { //cr
                if (closingBalance > 0) {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                    jsonObject.addProperty("ledger_type", "cr");

                } else {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("ledger_type", "dr");

                }

            } else if (mLedger.getFoundations().getId() == 3) {
                if (closingBalance > 0) {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                    jsonObject.addProperty("ledger_type", "cr");

                } else {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("ledger_type", "dr");

                }

            } else if (mLedger.getFoundations().getId() == 4) {
                if (closingBalance < 0) {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("ledger_type", "cr");

                } else {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                    jsonObject.addProperty("ledger_type", "dr");

                }
            }


            result.add(jsonObject);
        }
        if (result.size() > 0) {
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("message", "success");
            response.add("list", result);
        } else {
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("message", "empty list");
            response.add("list", result);
        }
        return response;
    }

    public JsonObject createPayments(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        TranxPaymentMaster tranxPayment = new TranxPaymentMaster();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            tranxPayment.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        tranxPayment.setOutlet(outlet);
        tranxPayment.setStatus(true);
        tranxPayment.setCreatedBy(users.getId());
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));

        /*     fiscal year mapping  */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            tranxPayment.setFiscalYear(fiscalYear);
            tranxPayment.setFinancialYear(fiscalYear.getFiscalYear());
        }

        tranxPayment.setTranscationDate(DateConvertUtil.convertStringToDate(request.getParameter("transaction_dt")));
        tranxPayment.setPaymentSrNo(Long.parseLong(request.getParameter("payment_sr_no")));
        if (paramMap.containsKey("narration"))
            tranxPayment.setNarrations(request.getParameter("narration"));
        else {
            tranxPayment.setNarrations(request.getParameter(""));
        }
        tranxPayment.setPaymentNo(request.getParameter("payment_code"));
        tranxPayment.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
        tranxPayment.setCreatedBy(users.getId());
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PMT");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        tranxPayment.setTranxCode(tranxCode);
        TranxPaymentMaster tranxPaymentMaster = tranxPaymentMasterRepository.save(tranxPayment);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("row");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                String crdrType = "";
                /*Payment Master */
                JsonObject paymentRow = row.get(i).getAsJsonObject();
                /*Payment Perticulars */
                TranxPaymentPerticulars tranxPaymentPerticulars = new TranxPaymentPerticulars();
                LedgerMaster ledgerMaster = null;
                tranxPaymentPerticulars.setBranch(branch);
                tranxPaymentPerticulars.setOutlet(outlet);
                tranxPaymentPerticulars.setStatus(true);
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(paymentRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null) tranxPaymentPerticulars.setLedgerMaster(ledgerMaster);
                tranxPaymentPerticulars.setTranxPaymentMaster(tranxPaymentMaster);
                tranxPaymentPerticulars.setType(paymentRow.get("type").getAsString());
                tranxPaymentPerticulars.setLedgerType(paymentRow.get("perticulars").getAsJsonObject().get("type").getAsString());
                tranxPaymentPerticulars.setLedgerName(paymentRow.get("perticulars").getAsJsonObject().get("ledger_name").getAsString());
                if (paymentRow.has("type"))
                    crdrType = paymentRow.get("type").getAsString();
                JsonObject perticulars = paymentRow.get("perticulars").getAsJsonObject();
                if (!paymentRow.get("paid_amt").getAsString().equalsIgnoreCase(""))
                    total_amt = paymentRow.get("paid_amt").getAsDouble();
                else
                    total_amt = 0.0;
                if (crdrType.equalsIgnoreCase("dr")) {
                    tranxPaymentPerticulars.setDr(total_amt);
                }
                if (crdrType.equalsIgnoreCase("cr")) {
                    tranxPaymentPerticulars.setCr(total_amt);
                }
                if (paymentRow.has("bank_payment_no") &&
                        !paymentRow.get("bank_payment_no").getAsString().equalsIgnoreCase("")) {
                    tranxPaymentPerticulars.setPaymentTranxNo(paymentRow.get("bank_payment_no").getAsString());
                } else {
                    tranxPaymentPerticulars.setPaymentTranxNo("");

                }
                if (paymentRow.has("bank_payment_type") &&
                        !paymentRow.get("bank_payment_type").getAsString().equalsIgnoreCase("")) {
                    PaymentModeMaster paymentModeMaster = paymentModeMasterRepository.findById(
                            paymentRow.get("bank_payment_type").getAsLong()).get();
                    tranxPaymentPerticulars.setPaymentMethod(paymentModeMaster.getPaymentMode());
                }
                /**** for Receipt, bank ledger Id *****/

                if (paymentRow.has("bank_name") &&
                        !paymentRow.get("bank_name").getAsString().equalsIgnoreCase("")) {
                    LedgerBankDetails bankDetails = ledgerBankDetailsRepository.
                            findByIdAndStatus(paymentRow.get("bank_name").getAsLong(), true);
                    tranxPaymentPerticulars.setBankName(bankDetails.getBankName());
                }
                /**** for Payment, bank name*****/
                if (paymentRow.has("bank_acc_name") &&
                        !paymentRow.get("bank_acc_name").getAsString().equalsIgnoreCase("")) {
                    tranxPaymentPerticulars.setBankName(paymentRow.get("bank_acc_name").getAsString());
                }
                if (paymentRow.has("payment_date") &&
                        !paymentRow.get("payment_date").getAsString().equalsIgnoreCase("") &&
                        !paymentRow.get("payment_date").getAsString().toLowerCase().contains("invalid"))
                    tranxPaymentPerticulars.setPaymentDate(LocalDate.parse(paymentRow.get("payment_date").getAsString()));
                tranxPaymentPerticulars.setCreatedBy(users.getId());
                tranxPaymentPerticulars.setTransactionDate(tranxDate);

                if (perticulars.has("payableAmt"))
                    tranxPaymentPerticulars.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxPaymentPerticulars.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxPaymentPerticulars.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxPaymentPerticulars.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                TranxPaymentPerticulars mParticular = tranxPaymentPerticularsRepository.save(tranxPaymentPerticulars);
                /*Payment Perticulars Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            TranxPaymentPerticularsDetails tranxPymtDetails = new TranxPaymentPerticularsDetails();
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxPurInvoice mPurInvoice = null;
                            tranxPymtDetails.setBranch(branch);
                            tranxPymtDetails.setOutlet(outlet);
                            tranxPymtDetails.setStatus(true);
                            if (ledgerMaster != null) tranxPymtDetails.setLedgerMaster(ledgerMaster);
                            tranxPymtDetails.setTranxPaymentMaster(tranxPaymentMaster);
                            tranxPymtDetails.setTranxPaymentPerticulars(mParticular);
                            tranxPymtDetails.setCreatedBy(users.getId());
                            String srcType = "pur_invoice";
                            if (jsonBill.has("source")) {
                                tranxPymtDetails.setType(jsonBill.get("source").getAsString());
                                srcType = jsonBill.get("source").getAsString();
                            }
                            if (srcType.equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxPymtDetails.setType("new_reference");
                            }
                            tranxPymtDetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());

                            tranxPymtDetails.setTransactionDate(tranxDate);
                            tranxPymtDetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxPymtDetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            if (srcType.equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    createDebitNote(tranxPaymentMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "create");
                                } else {
                                    mPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mPurInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxPurInvoiceRepository.save(mPurInvoice);
                                    }
                                }
                                tranxPymtDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxPymtDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxPymtDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxPymtDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            } else if (srcType.equalsIgnoreCase("debit_note")) {
                                TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = tranxDebitNoteNewReferenceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                tranxPymtDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxPymtDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxPymtDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxPymtDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxDebitNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
                                        tranxDebitNoteNewReferenceRepository.save(tranxDebitNoteNewReference);
                                    }
                                }
                            } else if (srcType.equalsIgnoreCase("credit_note")) {
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
                            } else if (srcType.equalsIgnoreCase("opening_balance")) {
                                LedgerOpeningBalance mOpening = ledgerOpeningBalanceRepository.findByOpeningBalInvoice(ledgerMaster.getId(),
                                        jsonBill.get("invoice_no").getAsString(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    mOpening.setInvoice_bal_amt(jsonBill.get("remaining_amt").getAsDouble());
                                    ledgerOpeningBalanceRepository.save(mOpening);
                                }
                                tranxPymtDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxPymtDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxPymtDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxPymtDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            }
                            tranxPaymentPerticularsDetailsRepository.save(tranxPymtDetails);
                        }
                    }
                }
                insertIntoPostings(mParticular, total_amt, crdrType.toUpperCase(), "Insert");//Accounting Postings
            }
            response.addProperty("message", "Payment created successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            paymentLogger.error("Error in createPayments :->" + e.getMessage());
            response.addProperty("message", "Error in Payment creation");
            response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return response;
    }

    private void createDebitNote(TranxPaymentMaster tranxPaymentMaster, double total_amt, LedgerMaster ledgerMaster,
                                 Long invoiceId, String key) {
        TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = null;
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        if (key.equalsIgnoreCase("create")) {
            tranxDebitNoteNewReference = new TranxDebitNoteNewReferenceMaster();
            Long count = tranxDebitNoteNewReferenceRepository.findLastRecord(tranxPaymentMaster.getOutlet().getId());
            //SQDEC00001
            String serailNo = String.format("%05d", count + 1);// 5 digit serial number
            //first 3 digits of Current month
            GenerateDates generateDates = new GenerateDates();
            String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
            String dbtnCode = "DBTN" + currentMonth + serailNo;
            tranxDebitNoteNewReference.setDebitnoteNewReferenceNo(dbtnCode);
            tranxDebitNoteNewReference.setStatus(true);
            tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
            tranxDebitNoteNewReference.setPurchaseInvoiceId(invoiceId);
        } else
            tranxDebitNoteNewReference = tranxDebitNoteNewReferenceRepository.findByPaymentIdAndStatus(
                    tranxPaymentMaster.getId(), true);

        if (tranxPaymentMaster.getBranch() != null)
            tranxDebitNoteNewReference.setBranch(tranxPaymentMaster.getBranch());
        tranxDebitNoteNewReference.setOutlet(tranxPaymentMaster.getOutlet());
        tranxDebitNoteNewReference.setSundryCreditor(ledgerMaster);
        tranxDebitNoteNewReference.setPaymentId(tranxPaymentMaster.getId());
        /* this parameter segregates whether debit note is from purchase invoice
        or purchase challan*/
        tranxDebitNoteNewReference.setSource("pur_invoice");
        tranxDebitNoteNewReference.setAdjustmentStatus("advance_payment");
        tranxDebitNoteNewReference.setFinancialYear(tranxPaymentMaster.getFinancialYear());
        tranxDebitNoteNewReference.setTotalAmount(total_amt);
        tranxDebitNoteNewReference.setBalance(total_amt);
        tranxDebitNoteNewReference.setFiscalYear(tranxPaymentMaster.getFiscalYear());
        tranxDebitNoteNewReference.setTranscationDate(tranxPaymentMaster.getTranscationDate());
        try {
            TranxDebitNoteNewReferenceMaster newDebitNote = tranxDebitNoteNewReferenceRepository.save(tranxDebitNoteNewReference);
            TranxDebitNoteDetails mDetails = null;
            if (key.equalsIgnoreCase("create")) {
                mDetails = new TranxDebitNoteDetails();
                mDetails.setOutlet(newDebitNote.getOutlet());
                mDetails.setStatus(true);
                mDetails.setTransactionStatus(transactionStatus);
                if (newDebitNote.getBranch() != null) mDetails.setBranch(newDebitNote.getBranch());
            } else
                mDetails = tranxDebitNoteDetailsRepository.findByStatusAndTranxDebitNoteMasterId(true, newDebitNote.getId());
            mDetails.setSundryCreditor(newDebitNote.getSundryCreditor());
            mDetails.setTotalAmount(total_amt);
            mDetails.setPaidAmt(total_amt);
            mDetails.setAdjustedId(newDebitNote.getPurchaseInvoiceId());
            mDetails.setAdjustedSource("advance_payment");
            mDetails.setBalance(total_amt);
            mDetails.setOperations("advance_payment");
            mDetails.setTranxDebitNoteMaster(newDebitNote);
            tranxDebitNoteDetailsRepository.save(mDetails);
        } catch (Exception e) {
            paymentLogger.error("Exception in insertIntoNewReference:" + e.getMessage());
        }
    }

    /* Accounting Postings of Payment Vouchers  */
    private void insertIntoPostings(TranxPaymentPerticulars paymentRows, double total_amt, String crdrType, String operation) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PMT");
        try {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(total_amt, paymentRows.getLedgerMaster(), tranxType, paymentRows.getLedgerMaster().getAssociateGroups(), paymentRows.getTranxPaymentMaster().getFiscalYear(), paymentRows.getBranch(), paymentRows.getOutlet(), paymentRows.getTranxPaymentMaster().getTranscationDate(), paymentRows.getTranxPaymentMaster().getId(), paymentRows.getTranxPaymentMaster().getPaymentNo(), crdrType, true, "Payment", operation);


            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, crdrType, total_amt, paymentRows.getTranxPaymentMaster().getFiscalYear(),
                        paymentRows.getLedgerMaster(), paymentRows.getTranxPaymentMaster().getTranscationDate(), paymentRows.getTranxPaymentMaster().getId(),
                        paymentRows.getOutlet(), paymentRows.getBranch(), paymentRows.getTranxPaymentMaster().getTranxCode());
            }
            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        paymentRows.getLedgerMaster().getId(), tranxType.getId(), paymentRows.getTranxPaymentMaster().getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(paymentRows.getLedgerMaster(), paymentRows.getTranxPaymentMaster().getTranscationDate(),
                            tranxType, paymentRows.getTranxPaymentMaster().getFiscalYear(), detail);
                }
            }

            /**** Save into Day Book ****/
            if (crdrType.equalsIgnoreCase("dr") && operation.equalsIgnoreCase("Insert")) {
                saveIntoDayBook(paymentRows);
            }
        } catch (Exception e) {
            e.printStackTrace();
            paymentLogger.error("Error in insert into payment postings :->" + e.getMessage());
        }
    }

    private void saveIntoDayBook(TranxPaymentPerticulars paymentRows) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(paymentRows.getOutlet());
        if (paymentRows.getBranch() != null) dayBook.setBranch(paymentRows.getBranch());
        dayBook.setAmount(paymentRows.getDr());
        dayBook.setTranxDate(paymentRows.getTransactionDate());
        dayBook.setParticulars(paymentRows.getLedgerMaster().getLedgerName());
        dayBook.setVoucherNo(paymentRows.getTranxPaymentMaster().getPaymentNo());
        dayBook.setVoucherType("Payment");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }

    public JsonObject getCreditorsPendingBillsNew(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long ledgerId = Long.parseLong(request.getParameter("ledger_id"));
        String type = request.getParameter("type");
        List<TranxPurInvoice> purInvoice = new ArrayList<>();
        List<TranxDebitNoteNewReferenceMaster> list = new ArrayList<>();
        List<TranxCreditNoteNewReferenceMaster> listcrd = new ArrayList<>();
        LocalDate endDate = null;
        LocalDate startDate = null;
        Boolean flag = false;
        Double totalBalance = 0.0;
        Double totalDnBalance = 0.0;
        if (paramMap.containsKey("start_date") && paramMap.containsKey("end_date")) {
            startDate = LocalDate.parse(request.getParameter("start_date"));
            endDate = LocalDate.parse(request.getParameter("end_date"));
            flag = true;
        }

        JsonArray result = new JsonArray();
        LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledgerId, true);
        JsonObject finalResult = new JsonObject();
        try {
            /* start of SC of bill by bill */
            if (type.equalsIgnoreCase("SC")) {
                /* checking for Bill by bill (bill by bill id: 1) */
                if (ledgerMaster.getBalancingMethod().getId() == 1) {
                    /* find all purchase invoices against sundry creditor */
                    if (users.getBranch() != null) {
                        if (flag)
                            purInvoice = tranxPurInvoiceRepository.findPendingBillsByBranchWithDates(
                                    users.getOutlet().getId(), users.getBranch().getId(), true, ledgerId,
                                    startDate, endDate);
                        else {
                            purInvoice = tranxPurInvoiceRepository.findPendingBillsByBranch(users.getOutlet().getId(),
                                    users.getBranch().getId(), true, ledgerId);
                        }
                    } else {
                        if (flag)
                            purInvoice = tranxPurInvoiceRepository.findPRPendingBillsWithDates(
                                    users.getOutlet().getId(), true, ledgerId, startDate, endDate);
                        else {
                            purInvoice = tranxPurInvoiceRepository.findPRPendingBills(users.getOutlet().getId(),
                                    true, ledgerId);
                        }
                    }
                    if (purInvoice.size() > 0) {
                        for (TranxPurInvoice newPurInvoice : purInvoice) {
                            JsonObject response = new JsonObject();
                            response.addProperty("invoice_id", newPurInvoice.getId());
                            response.addProperty("invoice_unique_id", "pur_invoice," + newPurInvoice.getId());
                            response.addProperty("amount", newPurInvoice.getBalance());
                            response.addProperty("total_amt", newPurInvoice.getTotalAmount());
                            response.addProperty("invoice_date",
                                    DateConvertUtil.convertDateToLocalDate(newPurInvoice.getInvoiceDate()).toString());
                            response.addProperty("invoice_no", newPurInvoice.getVendorInvoiceNo());
                            response.addProperty("ledger_id", ledgerId);
                            response.addProperty("source", "pur_invoice");
                            response.addProperty("due_days", ledgerMaster.getCreditDays());
                            response.addProperty("balancing_type", "Cr");
                            response.addProperty("paid_amt", 0.0);
                            response.addProperty("bill_details_id", 0);
                            response.addProperty("remaining_amt", 0.0);
                            result.add(response);
                        }
                    }
                }
                /*  supplier :  on Account  */
                else {
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
                if (flag)
                    list = tranxDebitNoteNewReferenceRepository.findByDebitNoteListPaymentWithDates(ledgerId, true, 1L,
                            "credit", "advance_payment", users.getOutlet().getId(), startDate, endDate);
                else {
                    list = tranxDebitNoteNewReferenceRepository.findByDebitNoteListPayment(ledgerId, true, 1L,
                            "credit", "advance_payment", users.getOutlet().getId());
                }

                if (list != null && list.size() > 0) {
                    for (TranxDebitNoteNewReferenceMaster mTranxDebitNote : list) {
                        if (mTranxDebitNote.getBalance() != 0.0) {
                            JsonObject data = new JsonObject();
                            data.addProperty("invoice_id", mTranxDebitNote.getId());
                            data.addProperty("invoice_unique_id", "debit_note," + mTranxDebitNote.getId());
                            data.addProperty("amount", mTranxDebitNote.getBalance());
                            data.addProperty("invoice_no", mTranxDebitNote.getDebitnoteNewReferenceNo());
                            data.addProperty("invoice_date",
                                    DateConvertUtil.convertDateToLocalDate(mTranxDebitNote.getTranscationDate()).toString());
                            data.addProperty("total_amt", mTranxDebitNote.getTotalAmount());
                            data.addProperty("source", "debit_note");
                            data.addProperty("balancing_type", "Dr");
                            result.add(data);

                        }
                    }
                }
            }

            if (type.equalsIgnoreCase("SD")) {
                listcrd = tranxCreditNoteNewReferenceRepository.findBySundryDebtorsIdAndStatusAndTransactionStatusIdAndAdjustmentStatusAndOutletId(ledgerId, true, 1L, "refund", users.getOutlet().getId());
                if (listcrd != null && listcrd.size() > 0) {
                    for (TranxCreditNoteNewReferenceMaster mTranxCreditNote : listcrd) {
                        if (mTranxCreditNote.getBalance() != 0.0) {
                            JsonObject data = new JsonObject();
                            data.addProperty("invoice_id", mTranxCreditNote.getId());
                            data.addProperty("invoice_unique_id", "credit_note," + mTranxCreditNote.getId());
                            data.addProperty("invoice_no", mTranxCreditNote.getCreditnoteNewReferenceNo());
                            data.addProperty("invoice_date",
                                    DateConvertUtil.convertDateToLocalDate(mTranxCreditNote.getTranscationDate()).toString());
                            data.addProperty("Total_amt", mTranxCreditNote.getBalance());
                            data.addProperty("source", "credit_note");
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
                        openingObject.addProperty("invoice_unique_id", "opening_balance," + mBalance.getId());
                        openingObject.addProperty("total_amt", mBalance.getBill_amt() != null ? mBalance.getBill_amt() : 0.00);
//                    openingObject.addProperty("amount", mBalance.getInvoice_paid_amt() != null ? mBalance.getInvoice_paid_amt() : 0.00);
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
            paymentLogger.error("Exception in: getCreditorsPendingBillsNew ->" + e.getMessage());
            System.out.println("Exception in: get_creditors_pending_bills ->" + e.getMessage());
            e.printStackTrace();
        }
//        finalResult.addProperty("openingSelected", ledgerMaster.getOpeningSelected() != null ? ledgerMaster.getOpeningSelected() : false);
        finalResult.addProperty("message", "success");
        finalResult.addProperty("responseStatus", HttpStatus.OK.value());

        finalResult.add("list", result);
        return finalResult;
    }

    //start of  all payment list
    public JsonObject getAllPaymentListbyOutlet(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPaymentMaster> payment = new ArrayList<>();
        if (users.getBranch() != null) {
            payment = tranxPaymentMasterRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            payment = tranxPaymentMasterRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
        }

        for (TranxPaymentMaster invoices : payment) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("payment_code", invoices.getPaymentNo());
            response.addProperty("transaction_dt", DateConvertUtil.convertDateToLocalDate(invoices.getTranscationDate()).toString());
            response.addProperty("payment_sr_no", invoices.getPaymentSrNo());
            List<TranxPaymentPerticulars> tranxPaymentPerticulars =
                    tranxPaymentPerticularsRepository.findLedgerName(invoices.getId(), users.getOutlet().getId(), true);
            response.addProperty("total_amount", invoices.getTotalAmt());
            response.addProperty("ledger_name", tranxPaymentPerticulars != null &&
                    tranxPaymentPerticulars.size() > 0 ? tranxPaymentPerticulars.get(0).getLedgerName() : "");
            response.addProperty("narration", invoices.getNarrations() != null ? invoices.getNarrations() : "");
            result.add(response);
        }

        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //end of  all payment list


    //start of payment list with pagination
    public Object paymentListbyOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
//
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");

        LocalDate endDatep = null;
        LocalDate startDatep = null;

        System.out.println("startdate " + startDatep + "  endDate " + endDatep);
        List<TranxPaymentMaster> payment = new ArrayList<>();
        List<TranxPaymentMaster> paymentArrayList = new ArrayList<>();
        List<PaymentDTO> paymentDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_payment_master_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

//            if(!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
//                query += " AND transaction_date BETWEEN '" + startDate +"' AND '" + endDate + "'";

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
                query = query + " ORDER BY id DESC";
            }
            String query1 = query;       //we get all lists here

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;
            Query q = entityManager.createNativeQuery(query, TranxPaymentMaster.class);
            payment = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxPaymentMaster.class);

            paymentArrayList = q1.getResultList();
//
            Integer total_pages = (paymentArrayList.size() / pageSize);
            if ((paymentArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (TranxPaymentMaster paymentListView : payment) {
                paymentDTOList.add(convertToDTDTO(paymentListView));
            }

            GenericDatatable<PaymentDTO> data = new GenericDatatable<>(paymentDTOList, paymentArrayList.size(),
                    pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(paymentDTOList);
            genericDTData.setTotalRows(0);
//            responseMessage.setMessage(HttpStatus.BAD_REQUEST.toString());
        }
        return responseMessage;
    }

    //end of paymeny list with pagination
    //start of DTO for payment list
    private PaymentDTO convertToDTDTO(TranxPaymentMaster tranxPaymentMaster) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setId(tranxPaymentMaster.getId());
        paymentDTO.setPayment_code(tranxPaymentMaster.getPaymentNo() != null ? tranxPaymentMaster.getPaymentNo() : "");
        paymentDTO.setTransaction_dt(DateConvertUtil.convertDateToLocalDate(tranxPaymentMaster.getTranscationDate()).toString());
        paymentDTO.setPayment_sr_no(tranxPaymentMaster.getPaymentSrNo());
        paymentDTO.setTotal_amount(tranxPaymentMaster.getTotalAmt());
        paymentDTO.setNarration(tranxPaymentMaster.getNarrations() != null ? tranxPaymentMaster.getNarrations() : "");

        List<TranxPaymentPerticulars> tranxPaymentPerticulars =
                tranxPaymentPerticularsRepository.findLedgerName(tranxPaymentMaster.getId(), tranxPaymentMaster.getOutlet().getId(), true);
        paymentDTO.setLedger_name(tranxPaymentPerticulars != null &&
                tranxPaymentPerticulars.size() > 0 ? tranxPaymentPerticulars.get(0).getLedgerName() : "");

        return paymentDTO;
    }

    //end of DTO for payment list

    public JsonObject getPaymentById(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPaymentPerticulars> list = new ArrayList<>();
        List<TranxPaymentPerticularsDetails> detailsList = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        Long ledgerSDId = 0L;
        try {
            Long paymentId = Long.parseLong(request.getParameter("payment_id"));
            TranxPaymentMaster tranxPaymentMaster = tranxPaymentMasterRepository.findByIdAndOutletIdAndStatus(
                    paymentId, users.getOutlet().getId(), true);
            list = tranxPaymentPerticularsRepository.findByTranxPaymentMasterIdAndStatus(tranxPaymentMaster.getId(), true);
            finalResult.addProperty("payment_id", tranxPaymentMaster.getId());
            finalResult.addProperty("payment_code", tranxPaymentMaster.getPaymentNo());
            finalResult.addProperty("payment_sr_no", tranxPaymentMaster.getPaymentSrNo());
            finalResult.addProperty("transaction_dt", DateConvertUtil.convertDateToLocalDate(tranxPaymentMaster.getTranscationDate()).toString());
            finalResult.addProperty("total_amt", tranxPaymentMaster.getTotalAmt());
            finalResult.addProperty("narrations", tranxPaymentMaster.getNarrations());
            JsonArray row = new JsonArray();
            JsonArray billsArray = new JsonArray();
            if (list.size() > 0) {
                for (TranxPaymentPerticulars mdetails : list) {

                    JsonObject rpdetails = new JsonObject();
                    if (mdetails.getType().equalsIgnoreCase("dr"))
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
                   /* JsonObject bankName = new JsonObject();
                    if (mdetails.getBankName() != null) {

                        LedgerBankDetails ledgerBankDetails = ledgerBankDetailsRepository.findByLedgerMasterIdAndBankNameIgnoreCaseAndStatus(ledgerSDId, mdetails.getBankName(), true);
                        bankName.addProperty("label", mdetails.getBankName());
                        bankName.addProperty("value", ledgerBankDetails.getId());
                        rpdetails.add("bank_name", bankName);
                    } else {
                        rpdetails.add("bank_name", bankName);
                    }*/
                    rpdetails.addProperty("bank_acc_name", mdetails.getBankName() != null ? mdetails.getBankName() : "");
                    rpdetails.addProperty("paymentTranxNo", mdetails.getPaymentTranxNo());
                    rpdetails.addProperty("payment_date",
                            mdetails.getPaymentDate() != null ? mdetails.getPaymentDate().toString() : "");
                    rpdetails.addProperty("ledger_id", mdetails.getLedgerMaster().getId());
                    rpdetails.addProperty("balancingMethod", mdetails.getLedgerMaster().getBalancingMethod() != null ?
                            generateSlugs.getSlug(mdetails.getLedgerMaster().getBalancingMethod().getBalancingMethod()) : "");
                    rpdetails.addProperty("payableAmt", mdetails.getPayableAmt() != null ? mdetails.getPayableAmt() : 0.0);
                    rpdetails.addProperty("selectedAmt", mdetails.getSelectedAmt() != null ? mdetails.getSelectedAmt() : 0.0);
                    rpdetails.addProperty("remainingAmt", mdetails.getRemainingAmt() != null ? mdetails.getRemainingAmt() : 0.0);
                    rpdetails.addProperty("isAdvanceCheck", mdetails.getIsAdvance());
                    if (mdetails.getType().equalsIgnoreCase("dr")) {
                        detailsList = tranxPaymentPerticularsDetailsRepository.
                                findByTranxPaymentPerticularsIdAndStatus(mdetails.getId(), true);
                        for (TranxPaymentPerticularsDetails mPerticular : detailsList) {
                            JsonObject mBill = new JsonObject();
                            mBill.addProperty("bill_details_id", mPerticular.getId());
                            mBill.addProperty("ledger_id", mPerticular.getLedgerMaster().getId());
                            mBill.addProperty("paid_amt", mPerticular.getPaidAmt());
                            mBill.addProperty("remaining_amt", mPerticular.getRemainingAmt());
                            mBill.addProperty("invoice_id", mPerticular.getTranxInvoiceId());
                            mBill.addProperty("invoice_no", mPerticular.getTranxNo());
                            mBill.addProperty("balancing_type", mPerticular.getBalancingType());
                            mBill.addProperty("invoice_date", mPerticular.getTransactionDate().toString());
                            mBill.addProperty("total_amt", mPerticular.getTotalAmt());
                            if (mPerticular.getType().equalsIgnoreCase("new_reference"))
                                mBill.addProperty("source", "pur_invoice");
                            else
                                mBill.addProperty("source", mPerticular.getType());
                            if (mPerticular.getType().equalsIgnoreCase("pur_invoice"))
                                mBill.addProperty("invoice_unique_id", "pur_invoice," + mPerticular.getTranxInvoiceId());
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
                    } else {
                        JsonArray billsArrayCr = new JsonArray();
                        rpdetails.add("bills", billsArrayCr);
                    }
                    row.add((rpdetails));
                }
            }
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("perticulars", row);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            paymentLogger.error("Error in getPaymentById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            paymentLogger.error("Error in getPaymentById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject upadatePayments(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PMT");
        TranxPaymentMaster tranxPayment = tranxPaymentMasterRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("payment_id")), true);

        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(
                tranxPayment.getId(), tranxType.getId(), true);
        JsonObject response = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            tranxPayment.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        tranxPayment.setOutlet(outlet);
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("transaction_dt"));
        if (tranxDate.isEqual(DateConvertUtil.convertDateToLocalDate(tranxPayment.getTranscationDate()))) {
            strDt = tranxPayment.getTranscationDate();
        }
//        tranxPayment.setTranscationDate(tranxDate);
        tranxPayment.setTranscationDate(strDt);
        /*     fiscal year mapping  */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            tranxPayment.setFiscalYear(fiscalYear);
            tranxPayment.setFinancialYear(fiscalYear.getFiscalYear());
        }
        tranxPayment.setPaymentSrNo(Long.parseLong(request.getParameter("payment_sr_no")));
        if (paramMap.containsKey("narration")) tranxPayment.setNarrations(request.getParameter("narration"));
        tranxPayment.setPaymentNo(request.getParameter("payment_code"));
        tranxPayment.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
        TranxPaymentMaster tranxPaymentMaster = tranxPaymentMasterRepository.save(tranxPayment);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("row");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                /*Payment Master */
                JsonObject paymentRow = row.get(i).getAsJsonObject();
                TranxPaymentPerticulars tranxPaymentPerticulars = null;
                Long detailsId = 0L;
                if (paymentRow.get("perticulars").getAsJsonObject().has("details_id"))
                    detailsId = paymentRow.get("perticulars").getAsJsonObject().get("details_id").getAsLong();
                if (detailsId != 0) {
                    tranxPaymentPerticulars = tranxPaymentPerticularsRepository.findByIdAndStatus(detailsId, true);
                } else {
                    tranxPaymentPerticulars = new TranxPaymentPerticulars();
                    tranxPaymentPerticulars.setStatus(true);
                }
                LedgerMaster ledgerMaster = null;
                tranxPaymentPerticulars.setBranch(branch);
                tranxPaymentPerticulars.setOutlet(outlet);
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(paymentRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null) tranxPaymentPerticulars.setLedgerMaster(ledgerMaster);
                tranxPaymentPerticulars.setTranxPaymentMaster(tranxPaymentMaster);
                tranxPaymentPerticulars.setType(paymentRow.get("type").getAsString());
                tranxPaymentPerticulars.setLedgerType(paymentRow.get("perticulars").getAsJsonObject().get("type").getAsString());
                tranxPaymentPerticulars.setLedgerName(paymentRow.get("perticulars").getAsJsonObject().get("ledger_name").getAsString());
                JsonObject perticulars = paymentRow.get("perticulars").getAsJsonObject();
                if (paymentRow.get("type").getAsString().equalsIgnoreCase("dr")) {
                    tranxPaymentPerticulars.setDr(paymentRow.get("paid_amt").getAsDouble());
                }
                if (paymentRow.get("type").getAsString().equalsIgnoreCase("cr")) {
                    tranxPaymentPerticulars.setCr(paymentRow.get("paid_amt").getAsDouble());
                }
                if (paymentRow.has("bank_payment_no") &&
                        !paymentRow.get("bank_payment_no").getAsString().equalsIgnoreCase("")) {
                    tranxPaymentPerticulars.setPaymentTranxNo(paymentRow.get("bank_payment_no").getAsString());
                } else {
                    tranxPaymentPerticulars.setPaymentTranxNo("");

                }
                if (paymentRow.has("bank_payment_type") &&
                        !paymentRow.get("bank_payment_type").getAsString().equalsIgnoreCase("")) {
                    PaymentModeMaster paymentModeMaster = paymentModeMasterRepository.findById(
                            paymentRow.get("bank_payment_type"). getAsLong()).get();
                    tranxPaymentPerticulars.setPaymentMethod(paymentModeMaster.getPaymentMode());
                }
                if (paymentRow.has("bank_name") &&
                        !paymentRow.get("bank_name").getAsString().equalsIgnoreCase("")) {
                    LedgerBankDetails bankDetails = ledgerBankDetailsRepository.
                            findByIdAndStatus(paymentRow.get("bank_name").getAsLong(), true);
                    tranxPaymentPerticulars.setBankName(bankDetails.getBankName());
                }
                if (paymentRow.has("payment_date") &&
                        !paymentRow.get("payment_date").getAsString().equalsIgnoreCase("") &&
                        !paymentRow.get("payment_date").getAsString().toLowerCase().contains("invalid"))
                    tranxPaymentPerticulars.setPaymentDate(LocalDate.parse(paymentRow.get("payment_date").getAsString()));
                tranxPaymentPerticulars.setTransactionDate(tranxDate);
                if (perticulars.has("payableAmt"))
                    tranxPaymentPerticulars.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxPaymentPerticulars.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxPaymentPerticulars.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxPaymentPerticulars.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                TranxPaymentPerticulars mParticular = tranxPaymentPerticularsRepository.save(tranxPaymentPerticulars);
                total_amt = paymentRow.get("paid_amt").getAsDouble();
                /*Payment Perticulars Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxPurInvoice mPurInvoice = null;
                            Long bill_details_id = 0L;
                            TranxPaymentPerticularsDetails tranxPymtDetails = null;
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("pur_invoice")) {
                                bill_details_id = jsonBill.get("bill_details_id").getAsLong();
                                if (bill_details_id == 0) {
                                    tranxPymtDetails = new TranxPaymentPerticularsDetails();
                                    tranxPymtDetails.setStatus(true);
                                    tranxPymtDetails.setCreatedBy(users.getId());
                                } else {
                                    tranxPymtDetails = tranxPaymentPerticularsDetailsRepository.
                                            findByIdAndStatus(bill_details_id, true);
                                }
                            }
                            if (ledgerMaster != null) tranxPymtDetails.setLedgerMaster(ledgerMaster);
                            tranxPymtDetails.setTranxPaymentMaster(tranxPaymentMaster);
                            tranxPymtDetails.setTranxPaymentPerticulars(mParticular);
                            tranxPymtDetails.setType(jsonBill.get("source").getAsString());
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxPymtDetails.setType("new_reference");
                            }
                            tranxPymtDetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());
                            tranxPymtDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString()));
                            tranxPymtDetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxPymtDetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    createDebitNote(tranxPaymentMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "update");
                                } else {
                                    mPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mPurInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxPurInvoiceRepository.save(mPurInvoice);
                                    }
                                }
                                tranxPymtDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxPymtDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxPymtDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxPymtDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            } else if (jsonBill.get("source").getAsString().equalsIgnoreCase("debit_note")) {
                                TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference =
                                        tranxDebitNoteNewReferenceRepository.
                                                findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                tranxPymtDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxPymtDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxPymtDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxPymtDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxDebitNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
                                        tranxDebitNoteNewReferenceRepository.save(tranxDebitNoteNewReference);
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
                            } else if (jsonBill.get("source").getAsString().equalsIgnoreCase("opening_balance")) {
                                LedgerOpeningBalance mOpening = ledgerOpeningBalanceRepository.findByOpeningBalInvoice(ledgerMaster.getId(),
                                        jsonBill.get("invoice_no").getAsString(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    mOpening.setInvoice_bal_amt(jsonBill.get("remaining_amt").getAsDouble());
                                    ledgerOpeningBalanceRepository.save(mOpening);
                                }
                                tranxPymtDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxPymtDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxPymtDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxPymtDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            }

                            tranxPaymentPerticularsDetailsRepository.save(tranxPymtDetails);
                        }
                    }
                }
                /*****  reverse the amount against the purchase invoice and debit note,
                 if invoice is unselected while updating the payment  *****/
                if (perticulars.has("deleteRow")) {
                    JsonArray deletedInvoicesJson = perticulars.get("deleteRow").getAsJsonArray();
                    for (JsonElement mInvoiceDel : deletedInvoicesJson) {
                        String data[] = mInvoiceDel.getAsString().split(",");
                        Long invoiceId = Long.parseLong(data[1]);
                        if (invoiceId != 0) {
                            TranxPurInvoice purInvoice = tranxPurInvoiceRepository.findByIdAndStatus(
                                    invoiceId, true);
                            if (purInvoice != null) {
                                purInvoice.setBalance(purInvoice.getTotalAmount());
                                tranxPurInvoiceRepository.save(purInvoice);
                                /**** make status=0 against the purchase invoice id,and payment masterid
                                 in TranxPayment Details table so that in bills list we don't get the unselected bill again ****/
                                TranxPaymentPerticularsDetails details = tranxPaymentPerticularsDetailsRepository.
                                        findByTranxPaymentMasterIdAndTranxInvoiceIdAndStatus(tranxPaymentMaster.getId(),
                                                invoiceId, true);
                                details.setStatus(false);
                                tranxPaymentPerticularsDetailsRepository.save(details);
                            }
                        } else {
                            TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference =
                                    tranxDebitNoteNewReferenceRepository.findByPaymentIdAndStatus(tranxPaymentMaster.getId(), true);
                            if (tranxDebitNoteNewReference != null) {
                                tranxDebitNoteNewReference.setBalance(tranxDebitNoteNewReference.getTotalAmount());
                                tranxDebitNoteNewReference.setStatus(false);
                                tranxDebitNoteNewReferenceRepository.save(tranxDebitNoteNewReference);
                            }
                        }
                    }
                }
                if (perticulars.has("debitDeleteRow")) {
                    JsonArray deletedDebitJson = perticulars.get("debitDeleteRow").getAsJsonArray();
                    for (JsonElement mDebitNoteDel : deletedDebitJson) {
                        String data[] = mDebitNoteDel.getAsString().split(",");
                        Long invoiceId = Long.parseLong(data[1]);
                        TranxDebitNoteNewReferenceMaster debitnoteMaster = tranxDebitNoteNewReferenceRepository.findByIdAndStatus(
                                invoiceId, true);
                        if (debitnoteMaster != null) {
                            debitnoteMaster.setBalance(debitnoteMaster.getTotalAmount());
                            TransactionStatus transactionStatus = transactionStatusRepository.findByIdAndStatus(1L, true);
                            debitnoteMaster.setTransactionStatus(transactionStatus);
                            tranxDebitNoteNewReferenceRepository.save(debitnoteMaster);
                            /**** make status=0 against the debinote invoice id, and payment master id ,
                             in TranxPayment Details table so that in bills list we don't get the unselected bill again ****/
                            TranxPaymentPerticularsDetails details = tranxPaymentPerticularsDetailsRepository.
                                    findByTranxPaymentMasterIdAndTranxInvoiceIdAndStatus(tranxPaymentMaster.getId(),
                                            mDebitNoteDel.getAsLong(), true);
                            details.setStatus(false);
                            tranxPaymentPerticularsDetailsRepository.save(details);
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
                            openingMaster.setStatus(false);

                            TransactionStatus transactionStatus = transactionStatusRepository.findByIdAndStatus(1L, true);
//                            openingMaster.setTransactionStatus(transactionStatus);
                            ledgerOpeningBalanceRepository.save(openingMaster);
                            /**** make status=0 against the debinote invoice id, and payment master id ,
                             in TranxPayment Details table so that in bills list we don't get the unselected bill again ****/
                        }
                    }
                }
                updateIntoPostings(mParticular, total_amt, detailsId);
            }


            /* Remove all ledgers from DB if we found new input ledger id's while updating */
            for (Long mDblist : ledgerList) {
                if (!ledgerInputList.contains(mDblist)) {
                    paymentLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mDblist, tranxType.getId(), tranxPayment.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), tranxPayment.getTranscationDate(),
                                tranxType, tranxPayment.getFiscalYear(), detail);
                    }
                    paymentLogger.info("removing unused previous ledger update done");
                }
            }
            response.addProperty("message", "Payment updated successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            paymentLogger.error("Error in createPayments :->" + e.getMessage());
            response.addProperty("message", "Error in Payment update");
            response.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return response;
    }

    private void updateIntoPostings(TranxPaymentPerticulars paymentRows, double total_amt, Long detailsId) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PMT");
        try {
            Boolean isLedgerContains = false;
            String tranxAction = "DR";
            /* for Sundry Creditors  */
            if (paymentRows.getType().equalsIgnoreCase("dr")) {
                if (detailsId != 0) {
                    isLedgerContains = ledgerList.contains(paymentRows.getLedgerMaster().getId());
                    ledgerInputList.add(paymentRows.getLedgerMaster().getId());
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(paymentRows.getLedgerMaster().getId(), tranxType.getId(), paymentRows.getTranxPaymentMaster().getId());
                    if (mLedger != null) {
                        mLedger.setAmount(total_amt);
                        mLedger.setTransactionDate(paymentRows.getTranxPaymentMaster().getTranscationDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    ledgerCommonPostings.callToPostings(total_amt, paymentRows.getLedgerMaster(), tranxType,
                            paymentRows.getLedgerMaster().getAssociateGroups(),
                            paymentRows.getTranxPaymentMaster().getFiscalYear(), paymentRows.getBranch(),
                            paymentRows.getOutlet(), paymentRows.getTranxPaymentMaster().getTranscationDate(),
                            paymentRows.getTranxPaymentMaster().getId(),
                            paymentRows.getTranxPaymentMaster().getPaymentNo(),
                            "DR", true, "Payment", "Insert");
                }
            } else {
                tranxAction = "CR";
                /* for Cash and Bank Account  */
                if (detailsId != 0) {
                    isLedgerContains = ledgerList.contains(paymentRows.getLedgerMaster().getId());
                    ledgerInputList.add(paymentRows.getLedgerMaster().getId());
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(paymentRows.getLedgerMaster().getId(), tranxType.getId(), paymentRows.getTranxPaymentMaster().getId());
                    if (mLedger != null) {
                        mLedger.setAmount(total_amt);
                        mLedger.setTransactionDate(paymentRows.getTranxPaymentMaster().getTranscationDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    // transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(paymentRows.getLedgerMaster().getFoundations().getId(), paymentRows.getLedgerMaster().getPrinciples() != null ? paymentRows.getLedgerMaster().getPrinciples().getId() : null, paymentRows.getLedgerMaster().getPrincipleGroups() != null ? paymentRows.getLedgerMaster().getPrincipleGroups().getId() : null, null, tranxType.getId(), paymentRows.getLedgerMaster().getBalancingMethod() != null ? paymentRows.getLedgerMaster().getBalancingMethod().getId() : null, paymentRows.getBranch() != null ? paymentRows.getBranch().getId() : null, paymentRows.getOutlet().getId(), "NA", 0.0, total_amt, paymentRows.getTranxPaymentMaster().getTranscationDate(), null, paymentRows.getId(), tranxType.getTransactionName(), paymentRows.getLedgerMaster().getUnderPrefix(), paymentRows.getTranxPaymentMaster().getFinancialYear(), paymentRows.getCreatedBy(), paymentRows.getLedgerMaster().getId(), paymentRows.getTranxPaymentMaster().getPaymentNo());
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(total_amt, paymentRows.getLedgerMaster(),
                            tranxType, paymentRows.getLedgerMaster().getAssociateGroups(),
                            paymentRows.getTranxPaymentMaster().getFiscalYear(), paymentRows.getBranch(),
                            paymentRows.getOutlet(), paymentRows.getTranxPaymentMaster().getTranscationDate(),
                            paymentRows.getTranxPaymentMaster().getId(),
                            paymentRows.getTranxPaymentMaster().getPaymentNo(),
                            "CR", true, "Payment", "Insert");
                }
            }

            Double amount = total_amt;
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, paymentRows.getLedgerMaster().getId(),
                    tranxType, tranxAction, paymentRows.getTranxPaymentMaster().getId(), paymentRows.getLedgerMaster(),
                    paymentRows.getTranxPaymentMaster().getTranscationDate(), paymentRows.getTranxPaymentMaster().getFiscalYear(),
                    paymentRows.getOutlet(), paymentRows.getBranch(), paymentRows.getTranxPaymentMaster().getTranxCode());
        } catch (Exception e) {
            e.printStackTrace();
            paymentLogger.error("Error in insertIntoPostings :->" + e.getMessage());
        }
    }

    public JsonObject deletePayment(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PMT");
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxPaymentMaster paymentMaster = tranxPaymentMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<TranxPaymentPerticularsDetails> mParticular = tranxPaymentPerticularsDetailsRepository.
                findByTranxPaymentMasterIdAndStatus(paymentMaster.getId(), true);
        try {
            paymentMaster.setStatus(false);
            tranxPaymentMasterRepository.save(paymentMaster);
            /**** setting balance reverse to Invoice Bill for Listing of Payment Invoice*****/
            for (TranxPaymentPerticularsDetails mDetails : mParticular) {
                if (mParticular != null) {
                    TranxPurInvoice mInvoice = tranxPurInvoiceRepository.findByIdAndStatus(mDetails.getTranxInvoiceId(), true);
                    if (mInvoice != null) {
                        mInvoice.setBalance(mDetails.getPaidAmt());
                        try {
                            tranxPurInvoiceRepository.save(mInvoice);
                        } catch (Exception e) {
                            paymentLogger.error("Exception in delete payment ->" + e.getMessage());
                        }
                    }
                }
            }
            if (paymentMaster != null) {
                List<TranxPaymentPerticulars> tranxPaymentPerticulars = tranxPaymentPerticularsRepository.
                        findByTranxPaymentMasterIdAndStatus(paymentMaster.getId(), true);
                for (TranxPaymentPerticulars mDetail : tranxPaymentPerticulars) {
                    if (mDetail.getType().equalsIgnoreCase("CR"))
                        insertIntoPostings(mDetail, mDetail.getCr(), "DR", "Delete");// Accounting Postings
                    else
                        insertIntoPostings(mDetail, mDetail.getDr(), "CR", "Delete");// Accounting Postings
                }
                /**** make status=0 to all ledgers of respective Payment voucher id, due to this we wont get
                 details of deleted invoice when we want get details of respective ledger ****/
                List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
                mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(tranxType.getId(), paymentMaster.getId(), true);
                for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                    try {
                        mPostings.setStatus(false);
                        ledgerTransactionPostingsRepository.save(mPostings);
                    } catch (Exception e) {
                        paymentLogger.error("Exception in Delete functionality for all ledgers of" + " deleted purchase invoice->" + e.getMessage());
                    }
                }
                jsonObject.addProperty("message", "Payment invoice deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in payment invoice deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            paymentLogger.error("Error in payment invoice Delete()->" + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject mobilePaymentList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        LocalDate localDate = null;
        String flag = "paymentList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<TranxPaymentPerticulars> balanceSummaries = new ArrayList<>();
        balanceSummaries = tranxPaymentPerticularsRepository.findByTypeAndStatus("dr", true);
        for (TranxPaymentPerticulars balanceSummary : balanceSummaries) {
//            System.out.println("Ledger Master Name "+balanceSummary.getLedgerName());
            Long ledgerId = balanceSummary.getLedgerMaster().getId();
            String type = balanceSummary.getType();
            JsonObject jsonObject = new JsonObject();
            LocalDate endDatep = null;
            LocalDate startDatep = null;


            endDatep = LocalDate.parse(request.get("end_date"));
            startDatep = LocalDate.parse(request.get("start_date"));


            try {
                list = tranxPaymentPerticularsRepository.findmobilePaymentTotalAmt(type, startDatep, endDatep, true);
                JsonArray innerArr = new JsonArray();
                for (int i = 0; i < list.size(); i++) {
                    JsonObject inside = new JsonObject();
                    Object[] objp = list.get(i);
                    sumTotal = Double.parseDouble(objp[0].toString());

                    totalInvoice = Integer.parseInt(objp[1].toString());
                    jsonObject.addProperty("TotalAmt", sumTotal);
                    jsonObject.addProperty("TotalInvoiceCount", totalInvoice);
                    jsonObject.addProperty("CreditorsId", ledgerId);
                    jsonObject.addProperty("flag", flag);
                    closingBalance = closingBalance + sumTotal;
                    //   innerArr.add(inside);
                }


            } catch (Exception e) {
                paymentLogger.error("Error in salesDelete()->" + e.getMessage());
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

    public JsonObject mobilePaymentInvoiceList(Map<String, String> request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumTotal = 0.0;
        Double sumBase = 0.0;
        Double sumTax = 0.0;
        Integer totalInvoice = 0;
        LocalDate localDate = null;
        String flag = "paymentList";
        List<Object[]> list = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        List<TranxPaymentPerticulars> balanceSummaries = new ArrayList<>();
        JsonObject jsonObject = new JsonObject();
        LocalDate endDatep = null;
        LocalDate startDatep = null;

        endDatep = LocalDate.parse(request.get("end_date"));
        startDatep = LocalDate.parse(request.get("start_date"));
        Long ledgerId = Long.valueOf(request.get("CreditorsId"));

        balanceSummaries = tranxPaymentPerticularsRepository.findByLedgerMasterAndStatus(ledgerId, startDatep, endDatep, true);
        for (TranxPaymentPerticulars balanceSummary : balanceSummaries) {
//            System.out.println("Ledger Master Name "+balanceSummary.getLedgerName());
            Long masterId = balanceSummary.getTranxPaymentMaster().getId();
            String type = balanceSummary.getType();

            try {
                TranxPaymentMaster tranxPaymentMaster = tranxPaymentMasterRepository.findByIdAndStatus(masterId, true);
                jsonObject.addProperty("Payment_No", tranxPaymentMaster.getPaymentNo());
                jsonObject.addProperty("Total_Amt", tranxPaymentMaster.getTotalAmt());
                jsonObject.addProperty("ledgerName", balanceSummary.getLedgerName());

                TranxPaymentPerticulars tranxPaymentPerticulars = tranxPaymentPerticularsRepository.findByTranxPaymentMasterIdAndTypeAndStatus(masterId, "cr", true);

                jsonObject.addProperty("cashAcc", tranxPaymentPerticulars.getLedgerName());
                jsonObject.addProperty("cashAmount", tranxPaymentPerticulars.getCr());

                result.add(jsonObject);

            } catch (Exception e) {
                paymentLogger.error("Error in salesDelete()->" + e.getMessage());
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

    public Object validatePayment(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPaymentMaster paymentMaster = null;
        if (users.getBranch() != null) {
            paymentMaster = tranxPaymentMasterRepository.findByOutletIdAndBranchIdAndPaymentNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("paymentNo"));
        } else {
            paymentMaster = tranxPaymentMasterRepository.findByOutletIdAndPaymentNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), request.getParameter("paymentNo"));
        }
        if (paymentMaster != null) {
            responseMessage.setMessage("Duplicate payment invoice number exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New payment invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object validatePaymentUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Long invoiceId = Long.parseLong(request.getParameter("voucher_id"));
        TranxPaymentMaster paymentMaster = null;
        if (users.getBranch() != null) {
            paymentMaster = tranxPaymentMasterRepository.findByOutletIdAndBranchIdAndPaymentNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("paymentNo"));
        } else {
            paymentMaster = tranxPaymentMasterRepository.findByOutletIdAndPaymentNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), request.getParameter("paymentNo"));
        }
        if (paymentMaster != null && invoiceId != paymentMaster.getId()) {
            responseMessage.setMessage("Duplicate payment invoice number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales payment number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }
}
