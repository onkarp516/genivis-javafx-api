package in.truethics.ethics.ethicsapiv10.service.tranx_service.journal;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.accountentrydto.JournalDTO;
import in.truethics.ethics.ethicsapiv10.dto.accountentrydto.ReceiptDTO;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalBillDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.PaymentModeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionStatusRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionTypeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository.TranxJournalBillsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository.TranxJournalDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository.TranxJournalMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TranxJournalService {
    @Autowired
    private TranxJournalMasterRepository tranxJournalMasterRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired

    private TranxJournalDetailsRepository tranxJournalDetailsRepository;

    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    private static final Logger journalLogger = LogManager.getLogger(TranxJournalService.class);
    @Autowired
    private TranxDebitNoteNewReferenceRepository tranxDebitNoteNewReferenceRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private TranxCreditNoteNewReferenceRepository tranxCreditNoteNewReferenceRepository;
    @Autowired
    private LedgerOpeningBalanceRepository ledgerOpeningBalanceRepository;
    @Autowired
    private TranxJournalBillsDetailsRepository tranxJournalBillsDetailsRepository;
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private TranxDebitNoteDetailsRepository tranxDebitNoteDetailsRepository;
    @Autowired
    private TranxCreditNoteDetailsRepository tranxCreditNoteDetailsRepository;
    @Autowired
    private GenerateSlugs generateSlugs;
    @Autowired
    private PaymentModeMasterRepository paymentModeMasterRepository;
    @Autowired
    private LedgerBankDetailsRepository ledgerBankDetailsRepository;
    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private PostingUtility postingUtility;

    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    List<Long> ledgerInputList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;


    public JsonObject journalLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxJournalMasterRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxJournalMasterRepository.findLastRecord(users.getOutlet().getId());
        }
//        Long count = tranxJournalMasterRepository.findLastRecord(users.getOutlet().getId());
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String csCode = "JRNL" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("journalNo", csCode);
        return result;
    }


    public JsonObject getledgerDetails(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        List<LedgerMaster> ledgerMaster = new ArrayList<>();
        if (users.getBranch() != null) {
            ledgerMaster = ledgerMasterRepository.findledgersByBranch(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            ledgerMaster = ledgerMasterRepository.findledgers(users.getOutlet().getId());
        }
        JsonObject response = new JsonObject();
        for (LedgerMaster mLedger : ledgerMaster) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", mLedger.getId());
            jsonObject.addProperty("name", mLedger.getLedgerName());
            jsonObject.addProperty("type", mLedger.getSlugName());
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

    public JsonObject createJournal(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        TranxJournalMaster journalMaster = new TranxJournalMaster();
        Branch branch = null;
        if (users.getBranch() != null)
            branch = users.getBranch();
        Outlet outlet = users.getOutlet();
        journalMaster.setBranch(branch);
        journalMaster.setOutlet(outlet);
        journalMaster.setStatus(true);
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            journalMaster.setFiscalYear(fiscalYear);
            journalMaster.setFinancialYear(fiscalYear.getFiscalYear());
        }


        journalMaster.setTranscationDate(DateConvertUtil.convertStringToDate(request.getParameter("transaction_dt")));
        journalMaster.setJournalSrNo(Long.parseLong(request.getParameter("voucher_journal_sr_no")));
        journalMaster.setJournalNo(request.getParameter("voucher_journal_no"));
        journalMaster.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
        if (paramMap.containsKey("narration"))
            journalMaster.setNarrations(request.getParameter("narration"));
        else {
            journalMaster.setNarrations("");
        }
        journalMaster.setCreatedBy(users.getId());
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("JRNL");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        journalMaster.setTranxCode(tranxCode);
        TranxJournalMaster tranxJournalMaster = tranxJournalMasterRepository.save(journalMaster);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("rows");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                String crdrType = "";
                JsonObject journalRow = row.get(i).getAsJsonObject();
                TranxJournalDetails tranxJournalDetails = new TranxJournalDetails();
                LedgerMaster ledgerMaster = null;

                tranxJournalDetails.setBranch(branch);
                tranxJournalDetails.setOutlet(outlet);
                tranxJournalDetails.setStatus(true);
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(journalRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null)
                    tranxJournalDetails.setLedgerMaster(ledgerMaster);
                tranxJournalDetails.setTranxJournalMaster(tranxJournalMaster);
                tranxJournalDetails.setType(journalRow.get("type").getAsString());
                tranxJournalDetails.setLedgerType(journalRow.get("perticulars").getAsJsonObject().get("type").getAsString());
                if (journalRow.has("type"))
                    crdrType = journalRow.get("type").getAsString();
                JsonObject perticulars = journalRow.get("perticulars").getAsJsonObject();
                if (!journalRow.get("paid_amt").getAsString().equalsIgnoreCase(""))
                    total_amt = journalRow.get("paid_amt").getAsDouble();
                else
                    total_amt = 0.0;
                if (crdrType.equalsIgnoreCase("dr")) {
                    tranxJournalDetails.setDr(total_amt);
                }
                if (crdrType.equalsIgnoreCase("cr")) {
                    tranxJournalDetails.setCr(total_amt);
                }
//                if (journalRow.has("bank_payment_no")) {
//                    tranxJournalDetails.setPaymentTranxNo(journalRow.get("bank_payment_no").getAsString());
//                }
//                if (journalRow.has("bank_payment_type")) {
//                    PaymentModeMaster paymentModeMaster = paymentModeMasterRepository.findById(
//                            journalRow.get("bank_payment_type").getAsLong()).get();
//                    tranxJournalDetails.setPaymentMethod(paymentModeMaster.getPaymentMode());
//                }
//                if (journalRow.has("bank_name")) {
//                    LedgerBankDetails bankDetails = ledgerBankDetailsRepository.
//                            findByIdAndStatus(journalRow.get("bank_name").getAsLong(), true);
//                    tranxJournalDetails.setBankName(bankDetails.getBankName());
//                }
//                if (journalRow.has("payment_date") && !journalRow.get("payment_date").getAsString().equalsIgnoreCase(""))
//                    tranxJournalDetails.setPaymentDate(LocalDate.parse(journalRow.get("payment_date").getAsString()));

                if (journalRow.has("payment_date") &&
                        !journalRow.get("payment_date").getAsString().equalsIgnoreCase("") &&
                        !journalRow.get("payment_date").getAsString().toLowerCase().contains("invalid"))
                    tranxJournalDetails.setPaymentDate(LocalDate.parse(journalRow.get("payment_date").getAsString()));
                tranxJournalDetails.setCreatedBy(users.getId());
                tranxJournalDetails.setTransactionDate(tranxDate);
                tranxJournalDetails.setPaidAmount(total_amt);
                if (perticulars.has("payableAmt"))
                    tranxJournalDetails.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxJournalDetails.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxJournalDetails.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxJournalDetails.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                TranxJournalDetails mJournal = tranxJournalDetailsRepository.save(tranxJournalDetails);
                /***** Journal Bill Details ******/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            TranxJournalBillDetails tranxBillDetails = new TranxJournalBillDetails();
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxPurInvoice mPurInvoice = null;
                            TranxSalesInvoice mSalesinvoice = null;
                            if (branch != null)
                                tranxBillDetails.setBranchId(Integer.valueOf(branch.getId().toString()));
                            tranxBillDetails.setOutletId(Integer.valueOf(outlet.getId().toString()));
                            tranxBillDetails.setStatus(true);
                            if (ledgerMaster != null) tranxBillDetails.setLedgerMasterId(ledgerMaster.getId());
                            tranxBillDetails.setTranxJournalMasterId(tranxJournalMaster.getId());
                            tranxBillDetails.setTranxJournalDetailsId(mJournal.getId());
                            tranxBillDetails.setCreatedBy(users.getId());
                            String srcType = "pur_invoice";
                            if (jsonBill.has("source")) {
                                tranxBillDetails.setType(jsonBill.get("source").getAsString());
                                srcType = jsonBill.get("source").getAsString();
                            }
                            if (srcType.equalsIgnoreCase("pur_invoice") ||
                                    srcType.equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxBillDetails.setType("new_reference");
                            }
                            tranxBillDetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());



                            tranxBillDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString().contains(" ")?jsonBill.get("invoice_date").getAsString().split(" ")[0]:jsonBill.get("invoice_date").getAsString()));

                            tranxBillDetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxBillDetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            if (srcType.equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    createDebitNote(tranxJournalMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "create");
                                } else {
                                    mPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mPurInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxPurInvoiceRepository.save(mPurInvoice);
                                    }
                                }
                            } else if (srcType.equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    createCreditNote(tranxJournalMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "create");
                                } else {
                                    mSalesinvoice = tranxSalesInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mSalesinvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxSalesInvoiceRepository.save(mSalesinvoice);
                                    }
                                }
                            } else if (srcType.equalsIgnoreCase("debit_note")) {
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
                            }
                            tranxBillDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                            tranxBillDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                            tranxBillDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                            tranxBillDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            tranxJournalBillsDetailsRepository.save(tranxBillDetails);
                        }
                    }
                }
                insertIntoPostings(mJournal, total_amt, journalRow.get("type").getAsString(), "Insert");//Accounting Postings
            }
            response.addProperty("message", "journal created successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            journalLogger.error("Error in createJournal :->" + e.getMessage());
            response.addProperty("message", "Error in Contra creation");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    private void createCreditNote(TranxJournalMaster tranxJournalMaster, double total_amt, LedgerMaster ledgerMaster,
                                  Long invoiceId, String key) {
        TranxCreditNoteNewReferenceMaster tranxCreditNoteMaster = null;
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        if (key.equalsIgnoreCase("create")) {
            tranxCreditNoteMaster = new TranxCreditNoteNewReferenceMaster();
            Long count = tranxCreditNoteNewReferenceRepository.findLastRecord(tranxJournalMaster.getOutlet().getId());
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
                    tranxJournalMaster.getId(), true);
        if (tranxJournalMaster.getBranch() != null)
            tranxCreditNoteMaster.setBranchId(tranxJournalMaster.getBranch().getId());
        tranxCreditNoteMaster.setOutletId(tranxJournalMaster.getOutlet().getId());
        tranxCreditNoteMaster.setSundryDebtorsId(ledgerMaster.getId());
        tranxCreditNoteMaster.setReceiptId(tranxJournalMaster.getId());
        /* this parameter segregates whether debit note is from purchase invoice
        or purchase challan*/
        tranxCreditNoteMaster.setSource("sales_invoice");
        tranxCreditNoteMaster.setAdjustmentStatus("advance_receipt");
        tranxCreditNoteMaster.setFinancialYear(tranxJournalMaster.getFinancialYear());
        tranxCreditNoteMaster.setBalance(tranxJournalMaster.getTotalAmt());
        tranxCreditNoteMaster.setFiscalYearId(tranxJournalMaster.getFiscalYear().getId());
        tranxCreditNoteMaster.setTranscationDate(tranxJournalMaster.getTranscationDate());
        tranxCreditNoteMaster.setTotalAmount(total_amt);
        tranxCreditNoteMaster.setBalance(total_amt);
        try {
            TranxCreditNoteNewReferenceMaster newCreditNote = tranxCreditNoteNewReferenceRepository.save(tranxCreditNoteMaster);
            TranxCreditNoteDetails mDetails = null;
            if (key.equalsIgnoreCase("create")) {
                mDetails = new TranxCreditNoteDetails();
                mDetails.setOutletId(tranxJournalMaster.getOutlet().getId());
                mDetails.setStatus(true);
                mDetails.setTransactionStatusId(transactionStatus.getId());
                if (tranxJournalMaster.getBranch() != null)
                    mDetails.setBranchId(tranxJournalMaster.getBranch().getId());
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
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            journalLogger.error("Exception in insertIntoNewReference:" + exceptionAsString);
        }
    }


    private void createDebitNote(TranxJournalMaster tranxJournalMaster, double total_amt, LedgerMaster ledgerMaster,
                                 Long invoiceId, String key) {
        TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = null;
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        if (key.equalsIgnoreCase("create")) {
            tranxDebitNoteNewReference = new TranxDebitNoteNewReferenceMaster();
            Long count = tranxDebitNoteNewReferenceRepository.findLastRecord(tranxJournalMaster.getOutlet().getId());
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
                    tranxJournalMaster.getId(), true);

        if (tranxJournalMaster.getBranch() != null)
            tranxDebitNoteNewReference.setBranch(tranxJournalMaster.getBranch());
        tranxDebitNoteNewReference.setOutlet(tranxJournalMaster.getOutlet());
        tranxDebitNoteNewReference.setSundryCreditor(ledgerMaster);
        tranxDebitNoteNewReference.setPaymentId(tranxJournalMaster.getId());
        /* this parameter segregates whether debit note is from purchase invoice
        or purchase challan*/
        tranxDebitNoteNewReference.setSource("pur_invoice");
        tranxDebitNoteNewReference.setAdjustmentStatus("advance_payment");
        tranxDebitNoteNewReference.setFinancialYear(tranxJournalMaster.getFinancialYear());
        tranxDebitNoteNewReference.setTotalAmount(total_amt);
        tranxDebitNoteNewReference.setBalance(total_amt);
        tranxDebitNoteNewReference.setFiscalYear(tranxJournalMaster.getFiscalYear());
        tranxDebitNoteNewReference.setTranscationDate(tranxJournalMaster.getTranscationDate());
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
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            journalLogger.error("Exception in insertIntoNewReference:" + exceptionAsString);
        }
    }

    private void insertIntoPostings(TranxJournalDetails mjournal, double total_amt, String crdrType,
                                    String operation) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("JRNL");
        try {

            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(total_amt, mjournal.getLedgerMaster(), tranxType,
                    mjournal.getLedgerMaster().getAssociateGroups(), mjournal.getTranxJournalMaster().getFiscalYear(),
                    mjournal.getBranch(), mjournal.getOutlet(), mjournal.getTranxJournalMaster().getTranscationDate(),
                    mjournal.getTranxJournalMaster().getId(), mjournal.getTranxJournalMaster().getJournalNo(),
                    crdrType, true, "Journal", operation);

            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, crdrType, total_amt, mjournal.getTranxJournalMaster().getFiscalYear(),
                        mjournal.getLedgerMaster(), mjournal.getTranxJournalMaster().getTranscationDate(), mjournal.getTranxJournalMaster().getId(),
                        mjournal.getOutlet(), mjournal.getBranch(), mjournal.getTranxJournalMaster().getTranxCode());
            }
            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mjournal.getLedgerMaster().getId(), tranxType.getId(), mjournal.getTranxJournalMaster().getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mjournal.getLedgerMaster(), mjournal.getTranxJournalMaster().getTranscationDate(),
                            tranxType, mjournal.getTranxJournalMaster().getFiscalYear(), detail);
                }
            }

            /**** Save into Day Book ****/
            if (crdrType.equalsIgnoreCase("dr") && operation.equalsIgnoreCase("Insert")) {
                saveIntoDayBook(mjournal);
            }

        } catch (Exception e) {
            e.printStackTrace();
            journalLogger.error("Error in journal insertIntoPostings :->" + e.getMessage());
        }
    }

    public void saveIntoDayBook(TranxJournalDetails mjournal) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(mjournal.getOutlet());
        if (mjournal.getBranch() != null)
            dayBook.setBranch(mjournal.getBranch());
        dayBook.setAmount(mjournal.getPaidAmount());
        LocalDate trDt = DateConvertUtil.convertDateToLocalDate(mjournal.getTranxJournalMaster().getTranscationDate());
        dayBook.setTranxDate(trDt);
        dayBook.setParticulars(mjournal.getLedgerMaster().getLedgerName());
        dayBook.setVoucherNo(mjournal.getTranxJournalMaster().getJournalNo());
        dayBook.setVoucherType("Journal");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }

    public JsonObject journalListbyOutlet(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        List<TranxJournalMaster> journal = new ArrayList<>();
        if (users.getBranch() != null) {
            journal = tranxJournalMasterRepository.
                    findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            journal = tranxJournalMasterRepository.
                    findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
        }
        for (TranxJournalMaster vouchers : journal) {
            JsonObject response = new JsonObject();
            response.addProperty("id", vouchers.getId());
            response.addProperty("journal_code", vouchers.getJournalNo());
            response.addProperty("transaction_dt", vouchers.getTranscationDate().toString());
            response.addProperty("journal_sr_no", vouchers.getJournalSrNo());
//             response.addProperty("ledger_name",vouchers.get );
            List<TranxJournalDetails> tranxJournalDetails = tranxJournalDetailsRepository.findByTranxJournalMasterIdAndTypeAndStatus(vouchers.getId(), "dr", true);
            response.addProperty("ledger_name", tranxJournalDetails != null && tranxJournalDetails.size() > 0 ? tranxJournalDetails.get(0).getLedgerMaster().getLedgerName() : "");
            response.addProperty("narration", vouchers.getNarrations());
            response.addProperty("total_amount", vouchers.getTotalAmt());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //start of journal list with pagination
    public Object journalListbyOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {
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
        List<TranxJournalMaster> journal = new ArrayList<>();
        List<TranxJournalMaster> journalArrayList = new ArrayList<>();
        List<JournalDTO> journalDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_journal_master_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
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
            System.out.println(" sort " + jsonToStr);
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") &&
                    jsonObject.get("colId").getAsString() != null) {
                System.out.println(" ORDER BY " + jsonObject.get("colId").getAsString());
                String sortBy = jsonObject.get("colId").getAsString();
                query = query + " ORDER BY " + sortBy;
                if (jsonObject.get("isAsc").getAsBoolean() == true) {
                    query = query + " ASC";
                } else {
                    query = query + " DESC";
                }
            } else {
                query = query + " ORDER BY journal_no ASC";
            }
            String query1 = query;       //we get all lists here
            System.out.println("query== " + query);

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            Query q = entityManager.createNativeQuery(query, TranxJournalMaster.class);

            journal = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxJournalMaster.class);

            journalArrayList = q1.getResultList();
            System.out.println("Limit total rows " + journalArrayList.size());
            Integer total_pages = (journalArrayList.size() / pageSize);
            if ((journalArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (TranxJournalMaster invoiceListView : journal) {
                journalDTOList.add(convertToDTDTO(invoiceListView));
            }

            GenericDatatable<JournalDTO> data = new GenericDatatable<>(journalDTOList, journalArrayList.size(),
                    pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(journalDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }
    //end of journal list with pagination

    //start of DTO for journal list
    private JournalDTO convertToDTDTO(TranxJournalMaster tranxJournalMaster) {
        JournalDTO journalDTO = new JournalDTO();
        journalDTO.setId(tranxJournalMaster.getId());
        journalDTO.setJournal_code(tranxJournalMaster.getJournalNo());
        journalDTO.setTransaction_dt(tranxJournalMaster.getTranscationDate().toString());
        journalDTO.setJournal_sr_no(tranxJournalMaster.getJournalSrNo());

        List<TranxJournalDetails> tranxJournalDetails = tranxJournalDetailsRepository.findByTranxJournalMasterIdAndTypeAndStatus(tranxJournalMaster.getId(), "dr", true);
        journalDTO.setLedger_name(tranxJournalDetails != null && tranxJournalDetails.size() > 0 ? tranxJournalDetails.get(0).getLedgerMaster().getLedgerName() : "");
        journalDTO.setNarration(tranxJournalMaster.getNarrations());
        journalDTO.setTotal_amount(tranxJournalMaster.getTotalAmt());

        return journalDTO;
    }
    //end of DTO for journal list

    /*update journal*/
    public JsonObject updateJournal(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("JRNL");
        TranxJournalMaster journalMaster = tranxJournalMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("journal_id")), true);
        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(
                journalMaster.getId(), tranxType.getId(), true);
        Branch branch = null;
        if (users.getBranch() != null)
            branch = users.getBranch();
        Outlet outlet = users.getOutlet();
        journalMaster.setBranch(branch);
        journalMaster.setOutlet(outlet);
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        Date dt = DateConvertUtil.convertStringToDate(request.getParameter("transaction_dt"));
        if (tranxDate.isEqual(DateConvertUtil.convertDateToLocalDate(journalMaster.getTranscationDate()))) {
            dt = journalMaster.getTranscationDate();
        }
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            journalMaster.setFiscalYear(fiscalYear);
            journalMaster.setFinancialYear(fiscalYear.getFiscalYear());
        }
        journalMaster.setTranscationDate(dt);
        journalMaster.setJournalSrNo(Long.parseLong(request.getParameter("voucher_journal_sr_no")));
        journalMaster.setJournalNo(request.getParameter("voucher_journal_no"));
        journalMaster.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
        if (paramMap.containsKey("narration"))
            journalMaster.setNarrations(request.getParameter("narration"));
        TranxJournalMaster tranxJournalMaster = tranxJournalMasterRepository.save(journalMaster);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("rows");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                JsonObject journalRow = row.get(i).getAsJsonObject();
                TranxJournalDetails tranxJournalDetails = null;
                Long detailsId = 0L;
                if (journalRow.get("perticulars").getAsJsonObject().has("details_id"))
                    detailsId = journalRow.get("perticulars").getAsJsonObject().get("details_id").getAsLong();
                if (detailsId != 0) {
                    tranxJournalDetails = tranxJournalDetailsRepository.findByIdAndStatus(detailsId, true);
                } else {
                    tranxJournalDetails = new TranxJournalDetails();
                    tranxJournalDetails.setStatus(true);
                }
                LedgerMaster ledgerMaster = null;
                tranxJournalDetails.setBranch(branch);
                tranxJournalDetails.setOutlet(outlet);
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(journalRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null)
                    tranxJournalDetails.setLedgerMaster(ledgerMaster);
                tranxJournalDetails.setTranxJournalMaster(tranxJournalMaster);
                tranxJournalDetails.setType(journalRow.get("type").getAsString());
                tranxJournalDetails.setLedgerType(journalRow.get("perticulars").getAsJsonObject().get("type").getAsString());
                JsonObject perticulars = journalRow.get("perticulars").getAsJsonObject();
                if (journalRow.get("type").getAsString().equalsIgnoreCase("dr")) {
                    tranxJournalDetails.setDr(journalRow.get("paid_amt").getAsDouble());
                }
                if (journalRow.get("type").getAsString().equalsIgnoreCase("cr")) {
                    tranxJournalDetails.setCr(journalRow.get("paid_amt").getAsDouble());
                }
                tranxJournalDetails.setPaidAmount(journalRow.get("paid_amt").getAsDouble());
                if (journalRow.has("bank_payment_no") &&
                        !journalRow.get("bank_payment_no").getAsString().equalsIgnoreCase("")) {
                    tranxJournalDetails.setPaymentTranxNo(journalRow.get("bank_payment_no").getAsString());
                } else {
                    tranxJournalDetails.setPaymentTranxNo("");

                }
                if (journalRow.has("bank_payment_type") &&
                        !journalRow.get("bank_payment_type").getAsString().equalsIgnoreCase("")) {
                    PaymentModeMaster paymentModeMaster = paymentModeMasterRepository.findById(
                            journalRow.get("bank_payment_type").getAsLong()).get();
                    tranxJournalDetails.setPaymentMethod(paymentModeMaster.getPaymentMode());
                }
                if (journalRow.has("bank_name") &&
                        !journalRow.get("bank_name").getAsString().equalsIgnoreCase("")) {
                    LedgerBankDetails bankDetails = ledgerBankDetailsRepository.
                            findByIdAndStatus(journalRow.get("bank_name").getAsLong(), true);
                    tranxJournalDetails.setBankName(bankDetails.getBankName());
                }
                if (journalRow.has("payment_date") &&
                        !journalRow.get("payment_date").getAsString().equalsIgnoreCase("")
                        && !journalRow.get("payment_date").getAsString().toLowerCase().contains("invalid"))
                    tranxJournalDetails.setPaymentDate(LocalDate.parse(journalRow.get("payment_date").getAsString()));
                tranxJournalDetails.setTransactionDate(tranxDate);
                if (perticulars.has("payableAmt"))
                    tranxJournalDetails.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxJournalDetails.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxJournalDetails.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxJournalDetails.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                total_amt = journalRow.get("paid_amt").getAsDouble();
                tranxJournalDetails.setCreatedBy(users.getId());
                TranxJournalDetails mJournalDetails = tranxJournalDetailsRepository.save(tranxJournalDetails);
                /*Journal Bills Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxPurInvoice mPurInvoice = null;
                            TranxSalesInvoice mSalesinvoice = null;
                            Long bill_details_id = 0L;
                            TranxJournalBillDetails tranxBillDetails = null;
                            bill_details_id = jsonBill.get("bill_details_id").getAsLong();
                            if (bill_details_id == 0) {
                                tranxBillDetails = new TranxJournalBillDetails();
                                tranxBillDetails.setStatus(true);
                                tranxBillDetails.setCreatedBy(users.getId());
                            } else {
                                tranxBillDetails = tranxJournalBillsDetailsRepository.
                                        findByIdAndStatus(bill_details_id, true);
                            }
                            if (ledgerMaster != null) tranxBillDetails.setLedgerMasterId(ledgerMaster.getId());
                            tranxBillDetails.setTranxJournalMasterId(tranxJournalMaster.getId());
                            tranxBillDetails.setTranxJournalDetailsId(mJournalDetails.getId());
                            String srcType = "pur_invoice";
                            if (jsonBill.has("source")) {
                                tranxBillDetails.setType(jsonBill.get("source").getAsString());
                                srcType = jsonBill.get("source").getAsString();
                            }
                            if (srcType.equalsIgnoreCase("pur_invoice") ||
                                    srcType.equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxBillDetails.setType("new_reference");
                            }
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxBillDetails.setType("new_reference");
                            }
                            tranxBillDetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());
                            tranxBillDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString()));
                            tranxBillDetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxBillDetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            tranxBillDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                            tranxBillDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                            tranxBillDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                            tranxBillDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            if (srcType.equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    createDebitNote(tranxJournalMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "create");
                                } else {
                                    mPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mPurInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxPurInvoiceRepository.save(mPurInvoice);
                                    }
                                }
                            } else if (srcType.equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    createCreditNote(tranxJournalMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "create");
                                } else {
                                    mSalesinvoice = tranxSalesInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mSalesinvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxSalesInvoiceRepository.save(mSalesinvoice);
                                    }
                                }
                            } else if (srcType.equalsIgnoreCase("debit_note")) {
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
                            }
                            tranxJournalBillsDetailsRepository.save(tranxBillDetails);
                        }
                    }
                }
                /*****  reverse the amount against the purchase invoice and debit note,
                 if invoice is unselected while updating the payment  *****/
                /*if (perticulars.has("deleteRow")) {
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
                                *//**** make status=0 against the purchase invoice id,and payment masterid
                 in TranxPayment Details table so that in bills list we don't get the unselected bill again ****//*
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
                                *//**** make status=0 against the purchase invoice id,and payment masterid
                 in TranxPayment Details table so that in bills list we don't get the unselected bill again ****//*
                 *//*TranxPaymentPerticularsDetails details = tranxPaymentPerticularsDetailsRepository.
                                        findByTranxPaymentMasterIdAndTranxInvoiceIdAndStatus(tranxPaymentMaster.getId(),
                                                mInvoiceDel.getAsLong(), true);
                                details.setStatus(false);
                                tranxPaymentPerticularsDetailsRepository.save(details);*//*
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
                            *//**** make status=0 against the debinote invoice id, and payment master id ,
                 in TranxPayment Details table so that in bills list we don't get the unselected bill again ****//*
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
                            *//**** make status=0 against the debinote invoice id, and payment master id ,
                 in TranxPayment Details table so that in bills list we don't get the unselected bill again ****//*
                        }
                    }
                }

*/
                updateIntoPostings(mJournalDetails, total_amt, detailsId);
            }

            /* Remove all ledgers from DB if we found new input ledger id's while updating */
            for (Long mDblist : ledgerList) {
                if (!ledgerInputList.contains(mDblist)) {
                    journalLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mDblist, tranxType.getId(), journalMaster.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), journalMaster.getTranscationDate(),
                                tranxType, journalMaster.getFiscalYear(), detail);
                    }
                    journalLogger.info("removing unused previous ledger update done");
                }
            }
            response.addProperty("message", "Journal updated successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            journalLogger.error("Error in createJournal :->" + e.getMessage());
            response.addProperty("message", "Error in Journal creation");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    private void updateIntoPostings(TranxJournalDetails mjournal, double total_amt, Long detailsId) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("JRNL");
        try {
            Boolean isLedgerContains = false;
            String tranxAction = "DR";
            if (mjournal.getType().equalsIgnoreCase("dr")) {
                if (detailsId != 0) {
                    isLedgerContains = ledgerList.contains(mjournal.getLedgerMaster().getId());
                    ledgerInputList.add(mjournal.getLedgerMaster().getId());
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                            findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mjournal.getLedgerMaster().getId(),
                                    tranxType.getId(), mjournal.getTranxJournalMaster().getId());
                    if (mLedger != null) {
                        mLedger.setAmount(total_amt);
                        mLedger.setTransactionDate(mjournal.getTranxJournalMaster().getTranscationDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    ledgerCommonPostings.callToPostings(total_amt, mjournal.getLedgerMaster(), tranxType,
                            mjournal.getLedgerMaster().getAssociateGroups(), mjournal.getTranxJournalMaster().getFiscalYear(),
                            mjournal.getBranch(), mjournal.getOutlet(), mjournal.getTranxJournalMaster().getTranscationDate(),
                            mjournal.getTranxJournalMaster().getId(), mjournal.getTranxJournalMaster().getJournalNo(),
                            "DR", true, "Journal", "Insert");
                }
            } else {
                tranxAction = "CR";
                if (detailsId != 0) {
                    isLedgerContains = ledgerList.contains(mjournal.getLedgerMaster().getId());
                    ledgerInputList.add(mjournal.getLedgerMaster().getId());
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                            findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mjournal.getLedgerMaster().getId(),
                                    tranxType.getId(), mjournal.getTranxJournalMaster().getId());
                    if (mLedger != null) {
                        mLedger.setAmount(total_amt);
                        mLedger.setTransactionDate(mjournal.getTranxJournalMaster().getTranscationDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(total_amt, mjournal.getLedgerMaster(), tranxType,
                            mjournal.getLedgerMaster().getAssociateGroups(), mjournal.getTranxJournalMaster().getFiscalYear(),
                            mjournal.getBranch(), mjournal.getOutlet(), mjournal.getTranxJournalMaster().getTranscationDate(),
                            mjournal.getTranxJournalMaster().getId(), mjournal.getTranxJournalMaster().getJournalNo(),
                            "CR", true, "Journal", "Insert");
                }
            }

            Double amount = total_amt;
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, mjournal.getLedgerMaster().getId(),
                    tranxType, tranxAction, mjournal.getTranxJournalMaster().getId(), mjournal.getLedgerMaster(),
                    mjournal.getTranxJournalMaster().getTranscationDate(), mjournal.getTranxJournalMaster().getFiscalYear(),
                    mjournal.getOutlet(), mjournal.getBranch(), mjournal.getTranxJournalMaster().getTranxCode());
        } catch (Exception e) {
            e.printStackTrace();
            journalLogger.error("Error in journal insertIntoPostings :->" + e.getMessage());
        }
    }

    /*get journal by id*/
    public JsonObject getjournalById(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxJournalDetails> list = new ArrayList<>();
        List<TranxJournalBillDetails> detailsList = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long journalId = Long.parseLong(request.getParameter("journal_id"));
            TranxJournalMaster journalMaster = tranxJournalMasterRepository.findByIdAndOutletIdAndStatus(journalId, users.getOutlet().getId(), true);
            list = tranxJournalDetailsRepository.findByTranxJournalMasterIdAndStatus(journalMaster.getId(), true);
            finalResult.addProperty("journal_id", journalMaster.getId());
            finalResult.addProperty("journal_no", journalMaster.getJournalNo());
            finalResult.addProperty("journal_sr_no", journalMaster.getJournalSrNo());
            finalResult.addProperty("tranx_date", journalMaster.getTranscationDate().toString());
            finalResult.addProperty("total_amt", journalMaster.getTotalAmt());
            finalResult.addProperty("narrations", journalMaster.getNarrations());
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxJournalDetails mdetails : list) {
                    JsonArray billsArray = new JsonArray();
                    JsonObject rpdetails = new JsonObject();
                    rpdetails.addProperty("details_id", mdetails.getId());
                    rpdetails.addProperty("type", mdetails.getType());
                    rpdetails.addProperty("ledger_type", mdetails.getLedgerType());
                    rpdetails.addProperty("paid_amt", mdetails.getPaidAmount());
                    rpdetails.addProperty("dr", mdetails.getDr()!=null?mdetails.getDr():0.0);
                    rpdetails.addProperty("cr", mdetails.getCr()!=null?mdetails.getCr():0.0);
//                    JsonObject paymentType = new JsonObject();
//                    if (mdetails.getPaymentMethod() != null && !mdetails.getPaymentMethod().equalsIgnoreCase("")) {
//                        PaymentModeMaster paymentMode = paymentModeMasterRepository.
//                                findByPaymentModeIgnoreCaseAndStatus(mdetails.getPaymentMethod(), true);
//                        paymentType.addProperty("label", mdetails.getPaymentMethod());
//                        paymentType.addProperty("value", paymentMode.getId());
//                        rpdetails.add("bank_payment_type", paymentType);
//                    } else {
//                        rpdetails.add("bank_payment_type", paymentType);
//                    }
//                    JsonObject bankName = new JsonObject();
//                    if (mdetails.getBankName() != null) {
//                        LedgerBankDetails ledgerBankDetails = ledgerBankDetailsRepository.
//                                findByLedgerMasterIdAndBankNameIgnoreCaseAndStatus(mdetails.getLedgerMaster().getId(),
//                                        mdetails.getBankName(), true);
//                        bankName.addProperty("label", mdetails.getBankName());
//                        bankName.addProperty("value", ledgerBankDetails.getId());
//                        rpdetails.add("bank_name", bankName);
//                    } else {
//                        rpdetails.add("bank_name", bankName);
//                    }
//                    rpdetails.addProperty("paymentTranxNo", mdetails.getPaymentTranxNo());
//                    rpdetails.addProperty("bank_name", mdetails.getBankName() != null ? mdetails.getBankName() : "");
//                    rpdetails.addProperty("payment_date",
//                            mdetails.getPaymentDate() != null ? mdetails.getPaymentDate().toString() : "");
                    rpdetails.addProperty("ledger_id", mdetails.getLedgerMaster().getId());
                    rpdetails.addProperty("ledgerName", mdetails.getLedgerMaster().getLedgerName());
                    rpdetails.addProperty("balancingMethod", mdetails.getLedgerMaster().getBalancingMethod() != null ?
                            generateSlugs.getSlug(mdetails.getLedgerMaster().getBalancingMethod().getBalancingMethod()) : "");
                    rpdetails.addProperty("payableAmt", mdetails.getPayableAmt());
                    rpdetails.addProperty("selectedAmt", mdetails.getSelectedAmt());
                    rpdetails.addProperty("remainingAmt", mdetails.getRemainingAmt());
                    rpdetails.addProperty("isAdvanceCheck", mdetails.getIsAdvance());
                    detailsList = tranxJournalBillsDetailsRepository.
                            findByTranxJournalDetailsIdAndStatus(mdetails.getId(), true);
                    for (TranxJournalBillDetails mPerticular : detailsList) {
                        JsonObject mBill = new JsonObject();
                        mBill.addProperty("bill_details_id", mPerticular.getId());
                        mBill.addProperty("paid_amt", mPerticular.getPaidAmt());
                        mBill.addProperty("remaining_amt", mPerticular.getRemainingAmt());
                        mBill.addProperty("invoice_id", mPerticular.getTranxInvoiceId());
                        mBill.addProperty("invoice_no", mPerticular.getTranxNo());
                        mBill.addProperty("balancing_type", mPerticular.getBalancingType());
                        mBill.addProperty("invoice_date", mPerticular.getTransactionDate().toString());
                        mBill.addProperty("total_amt", mPerticular.getTotalAmt());
                        if (mPerticular.getType().equalsIgnoreCase("new_reference")) {
                            if (mdetails.getLedgerType().equalsIgnoreCase("sc")) {
                                mBill.addProperty("source", "pur_invoice");

                            } else {
                                mBill.addProperty("source", "sales_invoice");

                            }
                        } else
                            mBill.addProperty("source", mPerticular.getType());
                        if (mPerticular.getType().equalsIgnoreCase("pur_invoice"))
                            mBill.addProperty("invoice_unique_id", "pur_invoice," + mPerticular.getTranxInvoiceId());
                        if (mPerticular.getType().equalsIgnoreCase("sales_invoice"))
                            mBill.addProperty("invoice_unique_id", "sales_invoice," + mPerticular.getTranxInvoiceId());
                        else if (mPerticular.getType().equalsIgnoreCase("opening_balance"))
                            mBill.addProperty("invoice_unique_id", "opening_balance," + mPerticular.getTranxInvoiceId());
                        else if (mPerticular.getType().equalsIgnoreCase("debit_note"))
                            mBill.addProperty("invoice_unique_id", "debit_note," + mPerticular.getTranxInvoiceId());
                        else if (mPerticular.getType().equalsIgnoreCase("credit_note"))
                            mBill.addProperty("invoice_unique_id", "credit_note," + mPerticular.getTranxInvoiceId());
                        else if (mPerticular.getType().equalsIgnoreCase("new_reference"))
                            mBill.addProperty("invoice_unique_id", "new-ref," + mPerticular.getTranxInvoiceId());

                        mBill.addProperty("amount", mPerticular.getAmount());
                        billsArray.add(mBill);
                    }
                    rpdetails.add("bills", billsArray);
                    row.add(rpdetails);
                }
            }
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("perticulars", row);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            journalLogger.error("Error in getJournalById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            journalLogger.error("Error in getJournalById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject deleteJournal(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxJournalMaster journalMaster = tranxJournalMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("JRNL");
        try {
            journalMaster.setStatus(false);
            tranxJournalMasterRepository.save(journalMaster);
            if (journalMaster != null) {
                List<TranxJournalDetails> tranxJournalDetails = tranxJournalDetailsRepository.
                        findByTranxJournalMasterIdAndStatus(journalMaster.getId(), true);
                for (TranxJournalDetails mDetail : tranxJournalDetails) {
                    if (mDetail.getType().equalsIgnoreCase("CR"))
                        insertIntoPostings(mDetail, mDetail.getPaidAmount(), "DR", "Delete");// Accounting Postings
                    else
                        insertIntoPostings(mDetail, mDetail.getPaidAmount(), "CR", "Delete");// Accounting Postings
                }
                /**** make status=0 to all ledgers of respective Journal voucher id, due to this we wont get
                 details of deleted invoice when we want get details of respective ledger ****/
                List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
                mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(tranxType.getId(), journalMaster.getId(), true);
                for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                    try {
                        mPostings.setStatus(false);
                        ledgerTransactionPostingsRepository.save(mPostings);
                    } catch (Exception e) {
                        journalLogger.error("Exception in Delete functionality for all ledgers of" + " deleted purchase invoice->" + e.getMessage());
                    }
                }
                jsonObject.addProperty("message", "Journal invoice deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in journal deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            journalLogger.error("Error in journal invoice Delete()->" + e.getMessage());
        }
        return jsonObject;
    }
}
