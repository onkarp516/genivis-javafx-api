package in.truethics.ethics.ethicsapiv10.service.tranx_service.sales;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.GenerateDates;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.GenerateSlugs;
import in.truethics.ethics.ethicsapiv10.common.LedgerCommonPostings;
import in.truethics.ethics.ethicsapiv10.dto.accountentrydto.CreditNoteDTO;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerBalanceSummary;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCNParticularBillDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCNBillDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TranxCreditNoteNewReferenceService {
    @Autowired
    private TranxCreditNoteNewReferenceRepository repository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private TranxCreditNoteDetailsRepository tranxCreditNoteDetailsRepository;
    @Autowired
    private LedgerBalanceSummaryRepository balanceSummaryRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    private static final Logger creditNewLogger = LogManager.getLogger(TranxCreditNoteNewReferenceService.class);
    @Autowired
    private FiscalYearRepository fiscalYearRepository;
    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private LedgerOpeningBalanceRepository ledgerOpeningBalanceRepository;
    @Autowired
    private TranxCNBillDetailsRepository tranxCNBillDetailsRepository;
    @Autowired
    private GenerateSlugs generateSlugs;
    @Autowired
    private TranxDebitNoteNewReferenceRepository tranxDebitNoteNewReferenceRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private PostingUtility postingUtility;

    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    List<Long> ledgerInputList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;

    public JsonObject tranxCreditNoteList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject finalResult = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List list = new ArrayList<>();
        Long sundryDebtorsId = Long.parseLong(request.getParameter("sundry_debtors_id"));
        String query = "SELECT id FROM tranx_credit_note_new_reference_tbl WHERE sundry_debtors_id=" + sundryDebtorsId + " AND status=1 AND transaction_status_id=1 AND adjustment_status='credit'" + " AND outlet_id = " + users.getOutlet().getId();
        if (users.getBranch() != null) {
            query = query + " AND branch_id=" + users.getBranch().getId();
        } else {
            query = query + " AND branch_id IS NULL";
        }
        Query q = entityManager.createNativeQuery(query);
        list = q.getResultList();
        // list = repository.findCreditNoteList(sundryDebtorsId, true, 1L, "credit", users.getOutlet().getId());
        if (list != null && list.size() > 0) {
            for (Object mList : list) {
                TranxCreditNoteNewReferenceMaster mTranxCreditNote = repository.findByIdAndStatus(Long.parseLong(mList.toString()), true);
                if (mTranxCreditNote.getBalance() != 0.0) {
                    JsonObject data = new JsonObject();
                    data.addProperty("id", mTranxCreditNote.getId());
                    if (mTranxCreditNote.getSalesInvoiceId() != null) {
                        data.addProperty("source", "sales_invoice");
                        data.addProperty("invoice_id", mTranxCreditNote.getSalesInvoiceId());
                    } else {
                        data.addProperty("source", "sales_challan");
                        data.addProperty("invoice_id", mTranxCreditNote.getSalesChallanId());
                    }
                    data.addProperty("credit_note_no", mTranxCreditNote.getCreditnoteNewReferenceNo());
                    data.addProperty("credit_note_date", mTranxCreditNote.getCreatedAt().toString());
                    data.addProperty("Total_amt", mTranxCreditNote.getBalance());
                    result.add(data);
                }
            }
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("list", result);
        } else {
            finalResult.addProperty("message", "empty list");
            finalResult.addProperty("responseStatus", HttpStatus.NO_CONTENT.value());
            finalResult.add("list", result);
        }
        return finalResult;
    }

    public JsonObject creditNoteLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = repository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = repository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String creditNote = "CRDTN" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("creditnoteNo", creditNote);
        return result;
    }

    public JsonObject getAllLedgers(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        List<LedgerBalanceSummary> balanceSummaries = balanceSummaryRepository.findByOutletIdOrderByIdDesc(users.getOutlet().getId());
        for (LedgerBalanceSummary balanceSummary : balanceSummaries) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", balanceSummary.getId());

            if (balanceSummary.getPrinciples() != null) {
                jsonObject.addProperty("principle_name", balanceSummary.getPrinciples().getPrincipleName());
            }
            if (balanceSummary.getPrincipleGroups() != null) {
                jsonObject.addProperty("subprinciple_name", balanceSummary.getPrincipleGroups().getGroupName());
            } else {
                jsonObject.addProperty("subprinciple_name", "");
            }
            jsonObject.addProperty("default_ledger", balanceSummary.getLedgerMaster().getIsDefaultLedger());
            jsonObject.addProperty("ledger_form_parameter_slug", balanceSummary.getLedgerMaster().getSlugName());
            jsonObject.addProperty("cr", Math.abs(balanceSummary.getCredit()));
            jsonObject.addProperty("dr", Math.abs(balanceSummary.getDebit()));
            jsonObject.addProperty("balance", balanceSummary.getBalance());
            jsonObject.addProperty("ledger_name", balanceSummary.getLedgerMaster().getLedgerName());
            result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public JsonObject createcredit(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        TranxCreditNoteNewReferenceMaster creditMaster = new TranxCreditNoteNewReferenceMaster();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            creditMaster.setBranchId(branch.getId());
        }
        Outlet outlet = users.getOutlet();
        creditMaster.setOutletId(outlet.getId());
        creditMaster.setStatus(true);
        creditMaster.setCreatedBy(users.getId());
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        /* fiscal year mapping */
        FiscalYear fiscalYear = null;
        fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            creditMaster.setFiscalYearId(fiscalYear.getId());
            creditMaster.setFinancialYear(fiscalYear.getFiscalYear());
        }

        String requestDate = request.getParameter("transaction_dt");
        Date strDt = DateConvertUtil.convertStringToDate(requestDate);
//        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
//        Date strDt = null;
//        try {
//            strDt = sdf.parse(requestDate);
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
//        strDt.setTime(System.currentTimeMillis());
        creditMaster.setTranscationDate(strDt);
        creditMaster.setSrno(Long.parseLong(request.getParameter("voucher_credit_sr_no")));
        creditMaster.setCreditnoteNewReferenceNo(request.getParameter("voucher_credit_no"));
        creditMaster.setTotalAmount(Double.parseDouble(request.getParameter("total_amt")));
        creditMaster.setSource("voucher");
        if (paramMap.containsKey("narration")) creditMaster.setNarrations(request.getParameter("narration"));
        else {
            creditMaster.setNarrations("");
        }
        creditMaster.setCreatedBy(users.getId());
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CRDTN");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        creditMaster.setTranxCode(tranxCode);
        TranxCreditNoteNewReferenceMaster tranxcreditMaster = repository.save(creditMaster);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("rows");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                String crdrType = "";
                String srcType = "";
                JsonObject debitRow = row.get(i).getAsJsonObject();
                TranxCreditNoteDetails tranxCreditDetails = new TranxCreditNoteDetails();
                LedgerMaster ledgerMaster = null;
                JsonObject perticulars = debitRow.get("perticulars").getAsJsonObject();
                if (branch != null)
                    tranxCreditDetails.setBranchId(branch.getId());
                tranxCreditDetails.setOutletId(outlet.getId());
                tranxCreditDetails.setStatus(true);
                Long ledgerId = debitRow.get("perticulars").getAsJsonObject().get("id").getAsLong();
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledgerId, true);
                if (ledgerMaster != null) tranxCreditDetails.setLedgerMasterId(ledgerMaster.getId());
                tranxCreditDetails.setTranxCreditNoteMasterId(tranxcreditMaster.getId());
                tranxCreditDetails.setType(debitRow.get("type").getAsString());
                tranxCreditDetails.setType(debitRow.get("type").getAsString());
                if (perticulars.has("type")) tranxCreditDetails.setLedgerType(perticulars.get("type").getAsString());
                else {
                    tranxCreditDetails.setLedgerType("");
                }
                if (debitRow.has("type")) crdrType = debitRow.get("type").getAsString();

                if (!debitRow.get("paid_amt").getAsString().equalsIgnoreCase(""))
                    total_amt = debitRow.get("paid_amt").getAsDouble();
                else total_amt = 0.0;
                tranxCreditDetails.setCreatedBy(users.getId());
                tranxCreditDetails.setPaidAmt(total_amt);
                if (crdrType.equalsIgnoreCase("dr")) {
                    tranxCreditDetails.setDr(total_amt);
                }
                if (crdrType.equalsIgnoreCase("cr")) {
                    tranxCreditDetails.setCr(total_amt);
                }
                if (debitRow.has("payment_date") && !debitRow.get("payment_date").getAsString().equalsIgnoreCase(""))
//                    tranxCreditDetails.setPaymentDate(LocalDate.parse(debitRow.get("payment_date").getAsString()));
                    tranxCreditDetails.setPaymentDate(DateConvertUtil.convertStringToDate(debitRow.get("payment_date").getAsString()));

                tranxCreditDetails.setCreatedBy(users.getId());
                tranxCreditDetails.setTransactionDate(tranxDate);
                if (perticulars.has("payableAmt"))
                    tranxCreditDetails.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxCreditDetails.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxCreditDetails.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxCreditDetails.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                TranxCreditNoteDetails mParticular = tranxCreditNoteDetailsRepository.save(tranxCreditDetails);
                /*Credit Note Bill Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            TranxCNParticularBillDetails tranxBilldetails = new TranxCNParticularBillDetails();
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxSalesInvoice mSalesInvoice = null;
                            if (branch != null) tranxBilldetails.setBranchId(branch.getId());
                            tranxBilldetails.setOutletId(outlet.getId());
                            tranxBilldetails.setStatus(true);
                            if (ledgerMaster != null) tranxBilldetails.setLedgerMasterId(ledgerMaster.getId());
                            tranxBilldetails.setTranxCreditNoteMasterId(tranxcreditMaster.getId());
                            tranxBilldetails.setTranxCreditNoteDetailsId(mParticular.getId());
                            tranxBilldetails.setCreatedBy(users.getId());
                            srcType = "sales_invoice";
                            if (jsonBill.has("source")) {
                                tranxBilldetails.setType(jsonBill.get("source").getAsString());
                                srcType = jsonBill.get("source").getAsString();
                            }
                            if (srcType.equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxBilldetails.setType("new_reference");
                            }
                            tranxBilldetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());
                            tranxBilldetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString().contains(" ")?jsonBill.get("invoice_date").getAsString().split(" ")[0]:jsonBill.get("invoice_date").getAsString()));
                            tranxBilldetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxBilldetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            if (srcType.equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                  /*  createDebitNote(tranxPaymentMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "create");*/
                                } else {
                                    mSalesInvoice = tranxSalesInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mSalesInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxSalesInvoiceRepository.save(mSalesInvoice);
                                    }
                                }
                                tranxBilldetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxBilldetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxBilldetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxBilldetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            } else if (srcType.equalsIgnoreCase("debit_note")) {
                                TranxCreditNoteNewReferenceMaster tranxCreditnoteMaster = repository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                tranxBilldetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxBilldetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxBilldetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxBilldetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxCreditnoteMaster.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxCreditnoteMaster.setTransactionStatusId(transactionStatus.getId());
                                        repository.save(tranxCreditnoteMaster);
                                    }
                                }
                            } else if (srcType.equalsIgnoreCase("credit_note")) {
                                TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReference = repository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxCreditNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxCreditNoteNewReference.setTransactionStatusId(transactionStatus.getId());
                                        repository.save(tranxCreditNoteNewReference);
                                    }
                                }
                            } else if (srcType.equalsIgnoreCase("opening_balance")) {
                                LedgerOpeningBalance mOpening = ledgerOpeningBalanceRepository.findByOpeningBalInvoice(ledgerMaster.getId(), jsonBill.get("invoice_no").getAsString(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    mOpening.setInvoice_bal_amt(jsonBill.get("remaining_amt").getAsDouble());
                                    ledgerOpeningBalanceRepository.save(mOpening);
                                }
                                tranxBilldetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxBilldetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxBilldetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxBilldetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            }
                            tranxCNBillDetailsRepository.save(tranxBilldetails);
                        }
                    }
                }
                insertIntoPostings(mParticular, total_amt, srcType, "Insert", fiscalYear, strDt,
                        tranxcreditMaster.getCreditnoteNewReferenceNo(), branch, outlet);//Accounting Postings
            }
            response.addProperty("message", "credit note  created successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            creditNewLogger.error("Error in createcredit :->" + e.getMessage());
            response.addProperty("message", "Error in credit creation");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    private void insertIntoPostings(TranxCreditNoteDetails mcredit, double total_amt, String source, String op,
                                    FiscalYear fiscalYear, Date tranxDate, String creditnoteNo, Branch branch,
                                    Outlet outlet) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CRDTN");

        try {
            String tranxAction = "DR";
            if (mcredit.getType().equalsIgnoreCase("cr")) {
                tranxAction = "CR";
                if (source.equalsIgnoreCase("sales_invoice")) {
                    /**** New Postings Logic *****/
                    LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                            mcredit.getLedgerMasterId(), true);

                    ledgerCommonPostings.callToPostings(total_amt, mLedger, tranxType, mLedger.getAssociateGroups(),
                            fiscalYear, branch, outlet, tranxDate, mcredit.getTranxCreditNoteMasterId(), creditnoteNo, "CR", true, tranxType.getTransactionCode(), op);
                    saveIntoDayBook(mcredit, outlet, branch);
                } else {
                    /**** New Postings Logic *****/
                    LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(mcredit.getLedgerMasterId(), true);
                    ledgerCommonPostings.callToPostings(total_amt, mLedger, tranxType, mLedger.getAssociateGroups(),
                            fiscalYear, branch, outlet, tranxDate, mcredit.getTranxCreditNoteMasterId(), creditnoteNo, "CR", true, tranxType.getTransactionCode(), op);
                }
            } else {
                if (source.equalsIgnoreCase("voucher") || source.equals("")) {
                    tranxAction = "DR";
                    /**** New Postings Logic *****/
                    LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(mcredit.getLedgerMasterId(), true);
                    ledgerCommonPostings.callToPostings(total_amt, mLedger, tranxType, mLedger.getAssociateGroups(),
                            fiscalYear, branch, outlet, tranxDate, mcredit.getTranxCreditNoteMasterId(), creditnoteNo, "DR", true, tranxType.getTransactionCode(), op);
                    saveIntoDayBook(mcredit, outlet, branch);
                }
            }

            if (op.equalsIgnoreCase("insert")) {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(mcredit.getLedgerMasterId(), true);
                TranxCreditNoteNewReferenceMaster master = repository.findByIdAndStatus(mcredit.getTranxCreditNoteMasterId(), true);
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, tranxAction, total_amt, fiscalYear, mLedger,
                        tranxDate, mcredit.getTranxCreditNoteMasterId(), outlet, branch, master.getTranxCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            creditNewLogger.error("Error in insertIntoPostings :->" + e.getMessage());
        }
    }


    public void saveIntoDayBook(TranxCreditNoteDetails mcredit, Outlet outlet, Branch branch) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(outlet);
        if (branch != null)
            dayBook.setBranch(branch);
        dayBook.setAmount(mcredit.getPaidAmt());
        TranxCreditNoteNewReferenceMaster master = repository.findByIdAndStatus(mcredit.getTranxCreditNoteMasterId(), true);
        LocalDate trDate = LocalDate.parse(new SimpleDateFormat("YYYY-MM-dd").format(master.getTranscationDate()));
        dayBook.setTranxDate(trDate);
        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(mcredit.getLedgerMasterId(), true);
        dayBook.setParticulars(mLedger.getLedgerName());
        dayBook.setVoucherNo(master.getCreditnoteNewReferenceNo());
        dayBook.setVoucherType("Creditnote");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }

    public JsonObject creditListbyOutlet(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List list = new ArrayList<>();
        String query = "SELECT id FROM tranx_credit_note_new_reference_tbl WHERE status=1 AND outlet_id = " + users.getOutlet().getId();
        if (users.getBranch() != null) {
            query = query + " AND branch_id=" + users.getBranch().getId();
        } else {
            query = query + " AND branch_id IS NULL ORDER BY id DESC";
        }
        Query q = entityManager.createNativeQuery(query);
        list = q.getResultList();
        for (Object mvouchers : list) {
            TranxCreditNoteNewReferenceMaster vouchers = repository.findByIdAndStatus(Long.parseLong(mvouchers.toString()), true);
            JsonObject response = new JsonObject();
            response.addProperty("source", vouchers.getSource());
            response.addProperty("id", vouchers.getId());
            response.addProperty("credit_note_no", vouchers.getCreditnoteNewReferenceNo());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            response.addProperty("transaction_dt", vouchers.getTranscationDate() != null ? DateConvertUtil.convertDateToLocalDate(vouchers.getTranscationDate()).toString(): "");
            response.addProperty("narration", vouchers.getNarrations() != null ? vouchers.getNarrations() : "");
            List<TranxCreditNoteDetails> tranxCreditDetails = new ArrayList<>();
            tranxCreditDetails = tranxCreditNoteDetailsRepository.findLedgerName(vouchers.getId(), users.getOutlet().getId(), true);

            if (tranxCreditDetails != null && tranxCreditDetails.size() > 0) {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(tranxCreditDetails.get(0).getLedgerMasterId(), true);
                response.addProperty("ledger_name", mLedger != null ? mLedger.getLedgerName() : "");
                System.out.println("response in if cond " + response);
            } else {
                response.addProperty("ledger_name", "");
                System.out.println("response in else cond " + response);
            }

            response.addProperty("total_amount", vouchers.getTotalAmount());
           /* TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReferenceMaster = tranxDebitNoteDetailsRepository.
                    findLedgerName(
                            vouchers.getId(),users.getOutlet().getId(), true);
            response.addProperty("ledger_name",tranxDebitNoteNewReferenceMaster.getSundryCreditor().getLedgerName());
*/
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }
    //start of credit note list with pagination
    //end 0f credit note list with pagination

    //start of DTO for credit note
    private CreditNoteDTO convertToDTDTO(TranxCreditNoteNewReferenceMaster creditNote) {
        CreditNoteDTO creditNoteDTO = new CreditNoteDTO();
        creditNoteDTO.setSource(creditNote.getSource());
        creditNoteDTO.setId(creditNote.getId());
        creditNoteDTO.setCredit_note_no(creditNote.getCreditnoteNewReferenceNo());
        creditNoteDTO.setTransaction_dt(creditNote.getTranscationDate() != null ? creditNote.getTranscationDate().toString() : "");
        creditNoteDTO.setNarration(creditNote.getNarrations() != null ? creditNote.getNarrations() : "");

        List<TranxCreditNoteDetails> tranxCreditDetails = new ArrayList<>();
        tranxCreditDetails = tranxCreditNoteDetailsRepository.findLedgerName(creditNote.getId(), creditNote.getOutletId(), true);
        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(tranxCreditDetails.get(0).getLedgerMasterId(), true);
        creditNoteDTO.setLedger_name(tranxCreditDetails != null && tranxCreditDetails.size() > 0 ? mLedger.getLedgerName() : "");
        creditNoteDTO.setTotal_amount(creditNote.getTotalAmount());
        return creditNoteDTO;
    }
    //end of DTO for credit note

    /*get crdit note by id*/
    public JsonObject getCreditById(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxCreditNoteDetails> list = new ArrayList<>();
        List<TranxCNParticularBillDetails> detailsList = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long creditId = Long.parseLong(request.getParameter("credit_id"));
            TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReferenceMaster = repository.findByIdAndOutletIdAndStatus(creditId, users.getOutlet().getId(), true);
            list = tranxCreditNoteDetailsRepository.findByTranxCreditNoteMasterIdAndStatus(tranxCreditNoteNewReferenceMaster.getId(), true);
            finalResult.addProperty("credit_no", tranxCreditNoteNewReferenceMaster.getCreditnoteNewReferenceNo());
            finalResult.addProperty("tranx_unique_code", tranxCreditNoteNewReferenceMaster.getTranxCode());
            finalResult.addProperty("credit_sr_no", tranxCreditNoteNewReferenceMaster.getSrno());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            finalResult.addProperty("tranx_date", simpleDateFormat.format(tranxCreditNoteNewReferenceMaster.getTranscationDate()));
            finalResult.addProperty("total_amt", tranxCreditNoteNewReferenceMaster.getTotalAmount());
            finalResult.addProperty("narrations", tranxCreditNoteNewReferenceMaster.getNarrations());
            finalResult.addProperty("source", tranxCreditNoteNewReferenceMaster.getSource());
            JsonArray row = new JsonArray();
            JsonArray billsArray = new JsonArray();
            if (list.size() > 0) {
                for (TranxCreditNoteDetails mdetails : list) {
                    JsonObject rpdetails = new JsonObject();
                    LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                            mdetails.getLedgerMasterId(), true);
                    rpdetails.addProperty("details_id", mdetails.getId());
                    rpdetails.addProperty("type", mdetails.getType());
                    rpdetails.addProperty("ledger_type", mdetails.getLedgerType());
                    rpdetails.addProperty("total_amt", mdetails.getTotalAmount() != null ? mdetails.getTotalAmount() : 0.00);
                    rpdetails.addProperty("balance", mdetails.getBalance());
                    rpdetails.addProperty("paid_amt", mdetails.getPaidAmt() != null ? mdetails.getPaidAmt() : 0.00);
                    rpdetails.addProperty("adjusted_source", mdetails.getAdjustedSource() != null ? mdetails.getAdjustedSource() : "");
                    rpdetails.addProperty("adjustment_status", mdetails.getAdjustmentStatus() != null ? mdetails.getAdjustmentStatus() : "");
                    rpdetails.addProperty("operations", mdetails.getOperations()!= null ? mdetails.getOperations() : "");
                    rpdetails.addProperty("dr", mdetails.getDr());
                    rpdetails.addProperty("cr", mdetails.getCr());
                    rpdetails.addProperty("ledger_id", mdetails.getLedgerMasterId());
                    rpdetails.addProperty("ledgerName", mLedger.getLedgerName());
                    rpdetails.addProperty("debitnoteTranxNo", mdetails.getPaymentTranxNo()!= null ? mdetails.getAdjustmentStatus() : "");
                    rpdetails.addProperty("payment_date", mdetails.getPaymentDate() != null ? mdetails.getPaymentDate().toString() : "");
                    rpdetails.addProperty("balancingMethod", mLedger.getBalancingMethod() != null ? generateSlugs.getSlug(mLedger.getBalancingMethod().getBalancingMethod()) : "");
                    rpdetails.addProperty("payableAmt", mdetails.getPayableAmt() != null ? mdetails.getPayableAmt() : 0.00);
                    rpdetails.addProperty("selectedAmt", mdetails.getSelectedAmt()!= null ? mdetails.getSelectedAmt() : 0.00);
                    rpdetails.addProperty("remainingAmt", mdetails.getRemainingAmt() != null ? mdetails.getRemainingAmt() : 0.00 );
                    rpdetails.addProperty("isAdvanceCheck", mdetails.getIsAdvance() != null ? mdetails.getIsAdvance() :false);
                    if (mdetails.getType().equalsIgnoreCase("cr")) {
                        detailsList = tranxCNBillDetailsRepository.findByTranxCreditNoteDetailsIdAndStatus(mdetails.getId(), true);
                        for (TranxCNParticularBillDetails mPerticular : detailsList) {
                            JsonObject mBill = new JsonObject();
                            mBill.addProperty("bill_details_id", mPerticular.getId());
                            mBill.addProperty("paid_amt", mPerticular.getPaidAmt());
                            mBill.addProperty("remaining_amt", mPerticular.getRemainingAmt());
                            mBill.addProperty("invoice_id", mPerticular.getTranxInvoiceId());
                            mBill.addProperty("ledger_id", mdetails.getLedgerMasterId());
                            mBill.addProperty("invoice_no", mPerticular.getTranxNo());
                            mBill.addProperty("balancing_type", mPerticular.getBalancingType());
                            mBill.addProperty("invoice_date", mPerticular.getTransactionDate().toString());
                            mBill.addProperty("total_amt", mPerticular.getTotalAmt());
                            if (mPerticular.getType().equalsIgnoreCase("new_reference"))
                                mBill.addProperty("source", "sales_invoice");
                            else mBill.addProperty("source", mPerticular.getType());
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
                    }
                    row.add(rpdetails);
                }
            }

            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("credit_details", row);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            creditNewLogger.error("Error in getCreditById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            creditNewLogger.error("Error in getCreditById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }


    /*update credit*/
    public JsonObject updatecredit(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CRDTN");
        TranxCreditNoteNewReferenceMaster creditMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("credit_id")), true);

        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(
                creditMaster.getId(), tranxType.getId(), true);

        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            creditMaster.setBranchId(branch.getId());
        }
        Outlet outlet = users.getOutlet();
        creditMaster.setOutletId(outlet.getId());
        creditMaster.setStatus(true);
        creditMaster.setCreatedBy(users.getId());
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            creditMaster.setFiscalYearId(fiscalYear.getId());
            creditMaster.setFinancialYear(fiscalYear.getFiscalYear());
        }


        String invoiceDate = request.getParameter("transaction_dt");
        Date strDt = DateConvertUtil.convertStringToDate(invoiceDate);
        if (tranxDate.isEqual(DateConvertUtil.convertDateToLocalDate(creditMaster.getTranscationDate()))) {
            strDt = creditMaster.getTranscationDate();
        }
        creditMaster.setTranscationDate(strDt);
        creditMaster.setSrno(Long.parseLong(request.getParameter("voucher_credit_sr_no")));
        creditMaster.setCreditnoteNewReferenceNo(request.getParameter("voucher_credit_no"));
        creditMaster.setTotalAmount(Double.parseDouble(request.getParameter("total_amt")));
        creditMaster.setSource("voucher");
        if (paramMap.containsKey("narration")) creditMaster.setNarrations(request.getParameter("narration"));
        creditMaster.setCreatedBy(users.getId());
        TranxCreditNoteNewReferenceMaster tranxcreditMaster = repository.save(creditMaster);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("rows");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                JsonObject debitRow = row.get(i).getAsJsonObject();
                TranxCreditNoteDetails tranxCreditDetails = null;
                Long detailsId = 0L;
                if (debitRow.get("perticulars").getAsJsonObject().has("details_id"))
                    detailsId = debitRow.get("perticulars").getAsJsonObject().get("details_id").getAsLong();
                if (detailsId != 0) {
                    tranxCreditDetails = tranxCreditNoteDetailsRepository.findByIdAndStatus(detailsId, true);
                } else {
                    tranxCreditDetails = new TranxCreditNoteDetails();
                    tranxCreditDetails.setStatus(true);
                }
                LedgerMaster ledgerMaster = null;
                if (branch != null)
                    tranxCreditDetails.setBranchId(branch.getId());
                tranxCreditDetails.setOutletId(outlet.getId());
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(debitRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null) tranxCreditDetails.setLedgerMasterId(ledgerMaster.getId());
                tranxCreditDetails.setTranxCreditNoteMasterId(tranxcreditMaster.getId());
                tranxCreditDetails.setType(debitRow.get("type").getAsString());
                //tranxCreditDetails.setLedgerType(debitRow.get("perticulars").getAsJsonObject().get("type").getAsString());
                JsonObject perticulars = debitRow.get("perticulars").getAsJsonObject();
                tranxCreditDetails.setSource("voucher");
                //total_amt = debitRow.get("paid_amt")!=null ?debitRow.get("paid_amt").getAsDouble():0.0;
                if (debitRow.get("type").getAsString().equalsIgnoreCase("dr")) {
                    tranxCreditDetails.setDr(debitRow.get("paid_amt").getAsDouble());
                }
                if (debitRow.get("type").getAsString().equalsIgnoreCase("cr")) {
                    tranxCreditDetails.setCr(debitRow.get("paid_amt").getAsDouble());
                }

                if (debitRow.has("payment_date") && !debitRow.get("payment_date").getAsString().equalsIgnoreCase(""))
//                    tranxCreditDetails.setPaymentDate(LocalDate.parse(debitRow.get("payment_date").getAsString()));
                    tranxCreditDetails.setPaymentDate(DateConvertUtil.convertStringToDate(debitRow.get("payment_date").getAsString()));

                tranxCreditDetails.setTransactionDate(tranxDate);
                if (perticulars.has("payableAmt"))
                    tranxCreditDetails.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxCreditDetails.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxCreditDetails.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxCreditDetails.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                total_amt = debitRow.get("paid_amt").getAsDouble();
                tranxCreditDetails.setPaidAmt(total_amt);
                TranxCreditNoteDetails mcredit = tranxCreditNoteDetailsRepository.save(tranxCreditDetails);
                /*Creditnote Bills Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxSalesInvoice mSalesInvoice = null;
                            Long bill_details_id = 0L;
                            TranxCNParticularBillDetails tranxbillDetails = null;
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("sales_invoice")) {
                                bill_details_id = jsonBill.get("bill_details_id").getAsLong();
                                if (bill_details_id == 0) {
                                    tranxbillDetails = new TranxCNParticularBillDetails();
                                    tranxbillDetails.setStatus(true);
                                    tranxbillDetails.setCreatedBy(users.getId());
                                } else {
                                    tranxbillDetails = tranxCNBillDetailsRepository.findByIdAndStatus(bill_details_id, true);
                                }
                            }
                            if (ledgerMaster != null) tranxbillDetails.setLedgerMasterId(ledgerMaster.getId());
                            tranxbillDetails.setTranxCreditNoteMasterId(tranxcreditMaster.getId());
                            tranxbillDetails.setTranxCreditNoteDetailsId(mcredit.getId());
                            tranxbillDetails.setType(jsonBill.get("source").getAsString());
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxbillDetails.setType("new_reference");
                            }
                            tranxbillDetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());
                            tranxbillDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString()));
                            tranxbillDetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxbillDetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("sales_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    /*createDebitNote(tranxPaymentMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "update");*/
                                } else {
                                    mSalesInvoice = tranxSalesInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mSalesInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxSalesInvoiceRepository.save(mSalesInvoice);
                                    }
                                }
                                tranxbillDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxbillDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxbillDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxbillDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            } else if (jsonBill.get("source").getAsString().equalsIgnoreCase("debit_note")) {
                                TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = tranxDebitNoteNewReferenceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                tranxbillDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxbillDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxbillDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxbillDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
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
                                TranxCreditNoteNewReferenceMaster tranxCreditNoteNewReference = repository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxCreditNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxCreditNoteNewReference.setTransactionStatusId(transactionStatus.getId());
                                        repository.save(tranxCreditNoteNewReference);
                                    }
                                }
                            } else if (jsonBill.get("source").getAsString().equalsIgnoreCase("opening_balance")) {
                                LedgerOpeningBalance mOpening = ledgerOpeningBalanceRepository.findByOpeningBalInvoice(ledgerMaster.getId(), jsonBill.get("invoice_no").getAsString(), true);
                                if (jsonBill.has("remaining_amt")) {
                                    mOpening.setInvoice_bal_amt(jsonBill.get("remaining_amt").getAsDouble());
                                    ledgerOpeningBalanceRepository.save(mOpening);
                                }
                                tranxbillDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxbillDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxbillDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxbillDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            }
                            tranxCNBillDetailsRepository.save(tranxbillDetails);
                        }
                    }
                }
                updateintoPostings(mcredit, total_amt, tranxcreditMaster.getSource(), detailsId,
                        creditMaster.getTranscationDate(), creditMaster.getCreditnoteNewReferenceNo(),
                        fiscalYear, branch, outlet);
            }

            /* Remove all ledgers from DB if we found new input ledger id's while updating */
            for (Long mDblist : ledgerList) {
                if (!ledgerInputList.contains(mDblist)) {
                    creditNewLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mDblist, tranxType.getId(), creditMaster.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), creditMaster.getTranscationDate(),
                                tranxType, fiscalYear, detail);
                    }
                    creditNewLogger.info("removing unused previous ledger update done");
                }
            }
            response.addProperty("message", "credit note  created successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            creditNewLogger.error("Error in createcredit :->" + e.getMessage());
            response.addProperty("message", "Error in credit creation");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    private void updateintoPostings(TranxCreditNoteDetails mcredit, double total_amt, String source, Long detailsId,
                                    Date transcationDate, String creditnoteNo, FiscalYear mFiscalYear,
                                    Branch branch, Outlet outlet) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CRDTN");
        try {
            Boolean isLedgerContains = false;
            String tranxAction = "DR";
            if (mcredit.getType().equalsIgnoreCase("cr")) {
                if (source.equalsIgnoreCase("sales_invoice")) {
                    tranxAction = "CR";
                    if (detailsId != 0) {
                        isLedgerContains = ledgerList.contains(mcredit.getLedgerMasterId());
                        ledgerInputList.add(mcredit.getLedgerMasterId());

                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mcredit.getLedgerMasterId(), tranxType.getId(), mcredit.getTranxCreditNoteMasterId());
                        if (mLedger != null) {
                            mLedger.setAmount(total_amt);
                            mLedger.setTransactionDate(transcationDate);
                            mLedger.setOperations("updated");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }

                    } else {
                        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                                mcredit.getLedgerMasterId(), true);
                        ledgerCommonPostings.callToPostings(total_amt, mLedger, tranxType, mLedger.getAssociateGroups(),
                                mFiscalYear != null ? mFiscalYear : null, branch, outlet, transcationDate,
                                mcredit.getTranxCreditNoteMasterId(), creditnoteNo, "CR", true, tranxType.getTransactionName(), "Insert");
                    }
                } else {
                    tranxAction = "CR";
                    if (detailsId != 0) {
                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mcredit.getLedgerMasterId(), tranxType.getId(), mcredit.getTranxCreditNoteMasterId());
                        if (mLedger != null) {
                            mLedger.setAmount(total_amt);
                            mLedger.setTransactionDate(transcationDate);
                            mLedger.setOperations("updated");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    } else {
                        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                                mcredit.getLedgerMasterId(), true);
                        ledgerCommonPostings.callToPostings(total_amt, mLedger,
                                tranxType, mLedger.getAssociateGroups(),
                                mFiscalYear != null ? mFiscalYear : null, branch, outlet,
                                transcationDate, mcredit.getTranxCreditNoteMasterId(),
                                creditnoteNo, "CR", true, tranxType.getTransactionName(), "Insert");

                    }
                }
            } else {
                if (source.equalsIgnoreCase("voucher") || source.equals("")) {
                    tranxAction = "DR";
                    if (detailsId != 0) {
                        LedgerTransactionPostings mLedger =
                                ledgerTransactionPostingsRepository.
                                        findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(
                                                mcredit.getLedgerMasterId(), tranxType.getId(), mcredit.getTranxCreditNoteMasterId());
                        if (mLedger != null) {
                            mLedger.setAmount(total_amt);
                            mLedger.setTransactionDate(transcationDate);
                            mLedger.setOperations("updated");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    } else {
                        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                                mcredit.getLedgerMasterId(), true);
                        ledgerCommonPostings.callToPostings(total_amt, mLedger, tranxType, mLedger.getAssociateGroups(),
                                mFiscalYear != null ? mFiscalYear : null, branch, outlet, transcationDate,
                                mcredit.getTranxCreditNoteMasterId(), creditnoteNo, "DR", true,
                                tranxType.getTransactionName(), "Insert");
                    }
                }
            }

            Double amount = total_amt;
            LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                    mcredit.getLedgerMasterId(), true);
            TranxCreditNoteNewReferenceMaster master = repository.findByIdAndStatus(mcredit.getTranxCreditNoteMasterId(), true);
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, mcredit.getLedgerMasterId(),
                    tranxType, tranxAction, mcredit.getTranxCreditNoteMasterId(), mLedger,
                    master.getTranscationDate(), mFiscalYear, outlet, branch, master.getTranxCode());
        } catch (Exception e) {
            e.printStackTrace();
            creditNewLogger.error("Error in updateIntoPostings :->" + e.getMessage());
        }
    }

    public JsonObject deletecreditnote(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxCreditNoteNewReferenceMaster creditNoteMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CRDTN");
        try {
            creditNoteMaster.setStatus(false);
            repository.save(creditNoteMaster);
            if (creditNoteMaster != null) {
                List<TranxCreditNoteDetails> tranxCreditNoteDetails = tranxCreditNoteDetailsRepository.findByTranxCreditNoteMasterIdAndStatus(creditNoteMaster.getId(), true);
                for (TranxCreditNoteDetails mDetail : tranxCreditNoteDetails) {
                    if (mDetail.getType().equalsIgnoreCase("CR"))
                        deletePostings(mDetail, mDetail.getPaidAmt(), "DR", "Delete",
                                creditNoteMaster.getTranscationDate(), creditNoteMaster.getCreditnoteNewReferenceNo(),
                                creditNoteMaster.getFiscalYearId(), creditNoteMaster.getBranchId(), creditNoteMaster.getOutletId());// Accounting Postings
                    else
                        deletePostings(mDetail, mDetail.getPaidAmt(), "CR", "Delete",
                                creditNoteMaster.getTranscationDate(), creditNoteMaster.getCreditnoteNewReferenceNo(),
                                creditNoteMaster.getFiscalYearId(), creditNoteMaster.getBranchId(), creditNoteMaster.getOutletId());// Accounting Postings
                }
                /**** make status=0 to all ledgers of respective Creditnote voucher id, due to this we wont get
                 details of deleted invoice when we want get details of respective ledger ****/
                List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
                mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(tranxType.getId(), creditNoteMaster.getId(), true);
                for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                    try {
                        mPostings.setStatus(false);
                        ledgerTransactionPostingsRepository.save(mPostings);
                    } catch (Exception e) {
                        creditNewLogger.error("Exception in Delete functionality for all ledgers of" + " deleted purchase invoice->" + e.getMessage());
                    }
                }
                jsonObject.addProperty("message", "creditNote deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in creditNote deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            creditNewLogger.error("Error in creditNote invoice Delete()->" + e.getMessage());
        }
        return jsonObject;
    }

    private void deletePostings(TranxCreditNoteDetails mDetail, Double paidAmt, String crdrType, String operation,
                                Date transcationDate, String creditnoteNo, Long fiscalId,
                                Long branchId, Long outletId) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CRDTN");
        Branch branch = null;
        if (branchId != null)
            branch = branchRepository.findByIdAndStatus(branchId, true);
        Outlet outlet = outletRepository.findByIdAndStatus(outletId, true);
        FiscalYear mFiscalYear = null;
        if (fiscalId != null) mFiscalYear = fiscalYearRepository.findById(fiscalId).get();

        /**** New Postings Logic *****/
        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                mDetail.getLedgerMasterId(), true);

        ledgerCommonPostings.callToPostings(paidAmt, mLedger, tranxType, mLedger.getAssociateGroups(),
                mFiscalYear != null ? mFiscalYear : null, branch, outlet, transcationDate,
                mDetail.getTranxCreditNoteMasterId(), creditnoteNo, crdrType, true,
                tranxType.getTransactionName(), operation);


        /**** NEW METHOD FOR LEDGER POSTING ****/
        LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                mDetail.getLedgerMasterId(), tranxType.getId(), mDetail.getTranxCreditNoteMasterId(), true);
        if (ledgerDetail != null) {
            Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
            ledgerDetail.setAmount(0.0);
            ledgerDetail.setClosingAmount(closing);
            ledgerDetail.setStatus(false);
            LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.updateLedgerPostings(mLedger, transcationDate, tranxType, mFiscalYear, detail);
        }
    }
}
