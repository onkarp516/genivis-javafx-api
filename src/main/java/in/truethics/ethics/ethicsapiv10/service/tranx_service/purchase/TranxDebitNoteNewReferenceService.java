package in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.accountentrydto.DebitNoteDTO;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDNParticularBillDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.PaymentModeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionStatusRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionTypeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDNBillDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteNewReferenceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.journal.TranxJournalService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TranxDebitNoteNewReferenceService {
    @Autowired
    private TranxDebitNoteNewReferenceRepository repository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TranxDebitNoteDetailsRepository tranxDebitNoteDetailsRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private LedgerTransactionDetailsRepository transactionDetailsRepository;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    private static final Logger debitnoteLogger = LogManager.getLogger(TranxDebitNoteNewReferenceService.class);
    @Autowired
    private PaymentModeMasterRepository paymentModeMasterRepository;
    @Autowired
    private LedgerBankDetailsRepository ledgerBankDetailsRepository;
    @Autowired
    private TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private TranxCreditNoteNewReferenceRepository tranxCreditNoteNewReferenceRepository;
    @Autowired
    private LedgerOpeningBalanceRepository ledgerOpeningBalanceRepository;
    @Autowired
    private TranxDNBillDetailsRepository tranxDNBillDetailsRepository;
    @Autowired
    private GenerateSlugs generateSlugs;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;

    @Autowired
    private TranxJournalService tranxJournalService;
    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private PostingUtility postingUtility;

    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    List<Long> ledgerInputList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;

    public JsonObject tranxDebitNoteList(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonArray result = new JsonArray();
        JsonObject finalResult = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxDebitNoteNewReferenceMaster> list = new ArrayList<>();
        Long sundryCreditorId = Long.parseLong(request.getParameter("sundry_creditor_id"));

        list = repository.findBySundryCreditorIdAndStatusAndTransactionStatusIdAndAdjustmentStatusAndOutletId(
                sundryCreditorId, true, 1L, "credit", users.getOutlet().getId());

        if (list != null && list.size() > 0) {
            for (TranxDebitNoteNewReferenceMaster mTranxDebitNote : list) {
                JsonObject data = new JsonObject();
                if (mTranxDebitNote.getBalance() != 0.0) {
                    data.addProperty("Total_amt", mTranxDebitNote.getBalance());

                    data.addProperty("id", mTranxDebitNote.getId());
                    if (mTranxDebitNote.getPurchaseInvoiceId() != null) {
                        data.addProperty("source", "pur_invoice");
                        data.addProperty("invoice_id", mTranxDebitNote.getPurchaseInvoiceId());
                    } else {
                        data.addProperty("source", "pur_challan");
                        data.addProperty("invoice_id", mTranxDebitNote.getPurchaseChallanId());
                    }
                    data.addProperty("debit_note_no", mTranxDebitNote.getDebitnoteNewReferenceNo());
                    data.addProperty("debit_note_date", mTranxDebitNote.getCreatedAt().toString());
                    data.addProperty("isSelected",
                            mTranxDebitNote.getIsSelected() != null ? mTranxDebitNote.getIsSelected() : false);
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

    public JsonObject tranxDebitNoteDetailsList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject finalResult = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxDebitNoteDetails> list = new ArrayList<>();
        Long sundryCreditorId = Long.parseLong(request.getParameter("sundry_creditor_id"));
        list = tranxDebitNoteDetailsRepository.findBySundryCreditorIdAndStatusAndTransactionStatusIdAndOutletId(sundryCreditorId, true, 1L, users.getOutlet().getId());
        if (list != null && list.size() > 0) {
            for (TranxDebitNoteDetails mTranxDebitNote : list) {

                JsonObject data = new JsonObject();
                data.addProperty("id", mTranxDebitNote.getId());
                if (mTranxDebitNote.getTranxDebitNoteMaster().getPurchaseInvoiceId() != null) {
                    data.addProperty("source", "pur_invoice");
                    data.addProperty("invoice_id", mTranxDebitNote.getTranxDebitNoteMaster().getPurchaseInvoiceId());
                } else if (mTranxDebitNote.getTranxDebitNoteMaster().getPurchaseChallanId() != null) {
                    data.addProperty("source", "pur_challan");
                    data.addProperty("invoice_id", mTranxDebitNote.getTranxDebitNoteMaster().getPurchaseChallanId());
                }
                data.addProperty("debit_note_no", mTranxDebitNote.getTranxDebitNoteMaster().getDebitnoteNewReferenceNo());
                data.addProperty("debit_note_date", mTranxDebitNote.getTranxDebitNoteMaster().getCreatedAt().toString());
                data.addProperty("Total_amt", mTranxDebitNote.getTotalAmount());
                result.add(data);
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

    public JsonObject debitNoteLastRecord(HttpServletRequest request) {
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
        String debitNote = "DBTN" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("debitnoteNo", debitNote);
        return result;
    }

    public JsonObject debitListbyOutlet(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxDebitNoteNewReferenceMaster> debitnote = new ArrayList<>();
        if (users.getBranch() != null) {
            debitnote = repository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            debitnote = repository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
        }
        for (TranxDebitNoteNewReferenceMaster vouchers : debitnote) {
            JsonObject response = new JsonObject();
            response.addProperty("source", vouchers.getSource());
            response.addProperty("id", vouchers.getId());
            response.addProperty("debit_note_no", vouchers.getDebitnoteNewReferenceNo());
            response.addProperty("transaction_dt", vouchers.getTranscationDate() != null ? vouchers.getTranscationDate().toString() : "");
            //  response.addProperty("debit_sr_no", vouchers.getSrno());
            response.addProperty("narration", vouchers.getNarrations() != null ? vouchers.getNarrations() : "");
            List<TranxDebitNoteDetails> tranxDebitDetails = new ArrayList<>();
            tranxDebitDetails = tranxDebitNoteDetailsRepository.findLedgerName(vouchers.getId(), users.getOutlet().getId(), true);
            if (tranxDebitDetails != null && tranxDebitDetails.size() > 0)
                response.addProperty("ledger_name", tranxDebitDetails.get(0).getLedgerMaster().getLedgerName());
            else response.addProperty("ledger_name", "");
            response.addProperty("total_amount", vouchers.getTotalAmount());
            result.add(response);
        }

        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //start of debit note voucher list with pagination
    public Object debitListbyOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
//        System.out.println("request "+request+  "  req="+req);
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");

        LocalDate endDatep = null;
        LocalDate startDatep = null;

        System.out.println("startdate " + startDatep + "  endDate " + endDatep);
        List<TranxDebitNoteNewReferenceMaster> debitNote = new ArrayList<>();
        List<TranxDebitNoteNewReferenceMaster> debitArrayList = new ArrayList<>();
        List<DebitNoteDTO> debitDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_debit_note_new_reference_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
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
                query = query + " ORDER BY debitnote_new_reference_no ASC";
            }
            String query1 = query;       //we get all lists here
            System.out.println("query== " + query);

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            Query q = entityManager.createNativeQuery(query, TranxDebitNoteNewReferenceMaster.class);

            debitNote = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxDebitNoteNewReferenceMaster.class);

            debitArrayList = q1.getResultList();

            Integer total_pages = (debitArrayList.size() / pageSize);
            if ((debitArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }

            for (TranxDebitNoteNewReferenceMaster invoiceListView : debitNote) {
                debitDTOList.add(convertToDTDTO(invoiceListView));
            }

            GenericDatatable<DebitNoteDTO> data = new GenericDatatable<>(debitDTOList, debitArrayList.size(),
                    pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(debitDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }
    //end of debit note voucher list with pagination

    //start of DTO for debit note list
    private DebitNoteDTO convertToDTDTO(TranxDebitNoteNewReferenceMaster debitNote) {
        DebitNoteDTO debitNoteDTO = new DebitNoteDTO();
        debitNoteDTO.setSource(debitNote.getSource());
        debitNoteDTO.setId(debitNote.getId());
        debitNoteDTO.setDebit_note_no(debitNote.getDebitnoteNewReferenceNo());
        debitNoteDTO.setTransaction_dt(debitNote.getTranscationDate() != null ? debitNote.getTranscationDate().toString() : "");
        debitNoteDTO.setNarration(debitNote.getNarrations()!=null ? debitNote.getNarrations().toString():"" );

        List<TranxDebitNoteDetails> tranxDebitDetails = new ArrayList<>();
        tranxDebitDetails = tranxDebitNoteDetailsRepository.findLedgerName(debitNote.getId(), debitNote.getOutlet().getId(), true);
        debitNoteDTO.setLedger_name(tranxDebitDetails != null && tranxDebitDetails.size() > 0 ? tranxDebitDetails.get(0).getLedgerMaster().getLedgerName() : "");
        debitNoteDTO.setTotal_amount(debitNote.getTotalAmount());

        return debitNoteDTO;
    }
    //end of DTO for debit note list

    public JsonObject createdebit(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        TranxDebitNoteNewReferenceMaster debitMaster = new TranxDebitNoteNewReferenceMaster();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("DBTN");
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        Outlet outlet = users.getOutlet();
        debitMaster.setBranch(branch);
        debitMaster.setOutlet(outlet);
        debitMaster.setStatus(true);
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            debitMaster.setFiscalYear(fiscalYear);
            debitMaster.setFinancialYear(fiscalYear.getFiscalYear());
        }


        String invoiceDate = request.getParameter("transaction_dt");
        Date strDt = DateConvertUtil.convertStringToDate(invoiceDate);
        debitMaster.setTranscationDate(strDt);
        debitMaster.setSrno(Long.parseLong(request.getParameter("voucher_debit_sr_no")));
        debitMaster.setDebitnoteNewReferenceNo(request.getParameter("voucher_debit_no"));
        debitMaster.setTotalAmount(Double.parseDouble(request.getParameter("total_amt")));
        debitMaster.setSource(tranxType.getTransactionCode());
        if (paramMap.containsKey("narration")) debitMaster.setNarrations(request.getParameter("narration"));
        else {
            debitMaster.setNarrations("");
        }
        debitMaster.setCreatedBy(users.getId());

        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        debitMaster.setTranxCode(tranxCode);
        TranxDebitNoteNewReferenceMaster tranxdebitMaster = repository.save(debitMaster);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("rows");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                String crdrType = "";
                String srcType = "";
                /*Debitnote Master */
                JsonObject debitnoteRow = row.get(i).getAsJsonObject();
                /*Debitnote Perticulars */
                TranxDebitNoteDetails trnaxDebitnotedetails = new TranxDebitNoteDetails();
                LedgerMaster ledgerMaster = null;
                JsonObject perticulars = debitnoteRow.get("perticulars").getAsJsonObject();
                trnaxDebitnotedetails.setBranch(branch);
                trnaxDebitnotedetails.setOutlet(outlet);
                trnaxDebitnotedetails.setStatus(true);
                Long ledgerId = debitnoteRow.get("perticulars").getAsJsonObject().get("id").getAsLong();
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledgerId, true);
                if (ledgerMaster != null) trnaxDebitnotedetails.setLedgerMaster(ledgerMaster);
                trnaxDebitnotedetails.setTranxDebitNoteMaster(tranxdebitMaster);
                trnaxDebitnotedetails.setType(debitnoteRow.get("type").getAsString());
                if (perticulars.has("type"))
                    trnaxDebitnotedetails.setLedgerType(perticulars.get("type").getAsString());
                else {
                    trnaxDebitnotedetails.setLedgerType("");
                }
                if (debitnoteRow.has("type"))
                    crdrType = debitnoteRow.get("type").getAsString();

                if (!debitnoteRow.get("paid_amt").getAsString().equalsIgnoreCase(""))
                    total_amt = debitnoteRow.get("paid_amt").getAsDouble();
                else
                    total_amt = 0.0;
                if (crdrType.equalsIgnoreCase("dr")) {
                    trnaxDebitnotedetails.setDr(total_amt);
                }
                if (crdrType.equalsIgnoreCase("cr")) {
                    trnaxDebitnotedetails.setCr(total_amt);
                }
                trnaxDebitnotedetails.setPaidAmt(total_amt);
                if (debitnoteRow.has("payment_date") &&
                        !debitnoteRow.get("payment_date").getAsString().equalsIgnoreCase("")) {
//                    trnaxDebitnotedetails.setPaymentDate(LocalDate.parse(debitnoteRow.get("payment_date").getAsString()));
                    trnaxDebitnotedetails.setPaymentDate(DateConvertUtil.convertStringToDate(debitnoteRow.get("payment_date").getAsString()));
                }

                trnaxDebitnotedetails.setCreatedBy(users.getId());
                trnaxDebitnotedetails.setTransactionDate(tranxDate);
                if (perticulars.has("payableAmt"))
                    trnaxDebitnotedetails.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    trnaxDebitnotedetails.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    trnaxDebitnotedetails.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    trnaxDebitnotedetails.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                TranxDebitNoteDetails mParticular = tranxDebitNoteDetailsRepository.save(trnaxDebitnotedetails);
                /*Debit Note Bill Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            TranxDNParticularBillDetails tranxBilldetails = new TranxDNParticularBillDetails();
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxPurInvoice mPurInvoice = null;
                            tranxBilldetails.setBranch(branch);
                            tranxBilldetails.setOutlet(outlet);
                            tranxBilldetails.setStatus(true);
                            if (ledgerMaster != null) tranxBilldetails.setLedgerMaster(ledgerMaster);
                            tranxBilldetails.setTranxDebitNoteMaster(tranxdebitMaster.getId());
                            tranxBilldetails.setTranxDebitNoteDetails(mParticular.getId());
                            tranxBilldetails.setCreatedBy(users.getId());
                            srcType = "pur_invoice";
                            if (jsonBill.has("source")) {
                                tranxBilldetails.setType(jsonBill.get("source").getAsString());
                                srcType = jsonBill.get("source").getAsString();
                            }
                            if (srcType.equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxBilldetails.setType("new_reference");
                            }
                            tranxBilldetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());
                            tranxBilldetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString().contains(" ")?jsonBill.get("invoice_date").getAsString().split(" ")[0]:jsonBill.get("invoice_date").getAsString()));
                            tranxBilldetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxBilldetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            if (srcType.equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                  /*  createDebitNote(tranxPaymentMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "create");*/
                                } else {
                                    mPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mPurInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxPurInvoiceRepository.save(mPurInvoice);
                                    }
                                }
                                tranxBilldetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxBilldetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxBilldetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxBilldetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            } else if (srcType.equalsIgnoreCase("debit_note")) {
                                TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference = repository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                tranxBilldetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxBilldetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxBilldetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxBilldetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                                if (jsonBill.has("remaining_amt")) {
                                    Double mbalance = jsonBill.get("remaining_amt").getAsDouble();
                                    tranxDebitNoteNewReference.setBalance(mbalance);
                                    if (mbalance == 0.0) {
                                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                                        tranxDebitNoteNewReference.setTransactionStatus(transactionStatus);
                                        repository.save(tranxDebitNoteNewReference);
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
                                tranxBilldetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxBilldetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxBilldetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxBilldetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            }
                            tranxDNBillDetailsRepository.save(tranxBilldetails);
                        }
                    }
                }
                insertIntoPostings(mParticular, total_amt, srcType, "Insert");//Accounting Postings
            }
            response.addProperty("message", "Debitnote  created successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            //e.printStackTrace();
            debitnoteLogger.error("Error in createdebit :->" + e.getMessage());
            //  debitLogger.error("Error in debit Creation :->" + e.getMessage());
            response.addProperty("message", "Error in debit creation");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    private void insertIntoPostings(TranxDebitNoteDetails mdebit, double total_amt, String source, String op) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("DBTN");
        try {
            String tranxAction = "DR";
            if (mdebit.getType().equalsIgnoreCase("dr")) {
                /* if (source.equalsIgnoreCase("pur_invoice")) {
                 *//**** New Postings Logic *****//*
                    ledgerCommonPostings.callToPostings(total_amt, mdebit.getLedgerMaster(), tranxType,
                            mdebit.getLedgerMaster().getAssociateGroups(),
                            mdebit.getTranxDebitNoteMaster().getFiscalYear(),
                            mdebit.getBranch(), mdebit.getOutlet(),
                            mdebit.getTranxDebitNoteMaster().getTranscationDate(),
                            mdebit.getTranxDebitNoteMaster().getId(),
                            mdebit.getTranxDebitNoteMaster().getDebitnoteNewReferenceNo(), "DR", true, tranxType.getTransactionCode(), op);
                    saveIntoDayBook(mdebit);

                } else {*/
                /**** New Postings Logic *****/
                ledgerCommonPostings.callToPostings(total_amt, mdebit.getLedgerMaster(), tranxType,
                        mdebit.getLedgerMaster().getAssociateGroups(),
                        mdebit.getTranxDebitNoteMaster().getFiscalYear(), mdebit.getBranch(), mdebit.getOutlet(),
                        mdebit.getTranxDebitNoteMaster().getTranscationDate(),
                        mdebit.getTranxDebitNoteMaster().getId(),
                        mdebit.getTranxDebitNoteMaster().getDebitnoteNewReferenceNo(), "DR", true,
                        tranxType.getTransactionCode(), op);
                saveIntoDayBook(mdebit);

            } else {
                if (source.equalsIgnoreCase("voucher") || source.equals("")) {
                    tranxAction = "CR";
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(total_amt, mdebit.getLedgerMaster(), tranxType, mdebit.getLedgerMaster().getAssociateGroups(), mdebit.getTranxDebitNoteMaster().getFiscalYear(), mdebit.getBranch(), mdebit.getOutlet(), mdebit.getTranxDebitNoteMaster().getTranscationDate(), mdebit.getTranxDebitNoteMaster().getId(), mdebit.getTranxDebitNoteMaster().getDebitnoteNewReferenceNo(), "CR", true, tranxType.getTransactionCode(), op);
                    saveIntoDayBook(mdebit);

                }
            }

            if (op.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, tranxAction, total_amt, mdebit.getTranxDebitNoteMaster().getFiscalYear(),
                        mdebit.getLedgerMaster(), mdebit.getTranxDebitNoteMaster().getTranscationDate(), mdebit.getTranxDebitNoteMaster().getId(),
                        mdebit.getOutlet(), mdebit.getBranch(), mdebit.getTranxDebitNoteMaster().getTranxCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            debitnoteLogger.error("Error in insertIntoPostings :->" + e.getMessage());
            // debitLogger.error("Error in debit Postings :->" + e.getMessage());
        }
    }

    public void saveIntoDayBook(TranxDebitNoteDetails mdebit) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(mdebit.getOutlet());
        if (mdebit.getBranch() != null)
            dayBook.setBranch(mdebit.getBranch());
        dayBook.setAmount(mdebit.getPaidAmt());
        LocalDate trDt = DateConvertUtil.convertDateToLocalDate(mdebit.getTranxDebitNoteMaster().getTranscationDate());
        dayBook.setTranxDate(trDt);
        dayBook.setParticulars(mdebit.getLedgerMaster().getLedgerName());
        dayBook.setVoucherNo(mdebit.getTranxDebitNoteMaster().getDebitnoteNewReferenceNo());
        dayBook.setVoucherType("Debitnote");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }

    public JsonObject updatedebit(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        TranxDebitNoteNewReferenceMaster debitMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);

        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("DBTN");
        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(
                debitMaster.getId(), tranxType.getId(), true);
        Branch branch = null;
        if (users.getBranch() != null) branch = users.getBranch();
        Outlet outlet = users.getOutlet();
        debitMaster.setBranch(branch);
        debitMaster.setOutlet(outlet);
        debitMaster.setStatus(true);
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            debitMaster.setFiscalYear(fiscalYear);
            debitMaster.setFinancialYear(fiscalYear.getFiscalYear());
        }

        String invoiceDate = request.getParameter("transaction_dt").contains(" ")?request.getParameter("transaction_dt").split(" ")[0]:request.getParameter("transaction_dt");
        /*SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
        Date strDt = null;
        try {
            strDt = sdf.parse(invoiceDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("transaction_dt"));
        if (tranxDate.isEqual(DateConvertUtil.convertDateToLocalDate(debitMaster.getTranscationDate()))) {
            strDt = debitMaster.getTranscationDate();
        }
        strDt.setTime(System.currentTimeMillis());*/
        Date strDt=DateConvertUtil.convertStringToDate(invoiceDate);
        debitMaster.setTranscationDate(strDt);
        debitMaster.setSrno(Long.parseLong(request.getParameter("voucher_debit_sr_no")));
        debitMaster.setDebitnoteNewReferenceNo(request.getParameter("voucher_debit_no"));
        debitMaster.setTotalAmount(Double.parseDouble(request.getParameter("total_amt")));
        debitMaster.setSource("voucher");
        if (paramMap.containsKey("narration")) debitMaster.setNarrations(request.getParameter("narration"));
        TranxDebitNoteNewReferenceMaster tranxdebitMaster = repository.save(debitMaster);
        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("rows");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                JsonObject debitRow = row.get(i).getAsJsonObject();
                TranxDebitNoteDetails tranxDebitDetails = null;
                Long detailsId = 0L;
                if (debitRow.get("perticulars").getAsJsonObject().has("details_id"))
                    detailsId = debitRow.get("perticulars").getAsJsonObject().get("details_id").getAsLong();
                if (detailsId != 0) {
                    tranxDebitDetails = tranxDebitNoteDetailsRepository.findByIdAndStatus(detailsId, true);
                } else {
                    tranxDebitDetails = new TranxDebitNoteDetails();
                    tranxDebitDetails.setStatus(true);
                }
                LedgerMaster ledgerMaster = null;
                if (branch != null)
                    tranxDebitDetails.setBranch(branch);
                tranxDebitDetails.setOutlet(outlet);
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(debitRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null) tranxDebitDetails.setLedgerMaster(ledgerMaster);
                tranxDebitDetails.setTranxDebitNoteMaster(tranxdebitMaster);
                tranxDebitDetails.setType(debitRow.get("type").getAsString());
                tranxDebitDetails.setLedgerType(debitRow.get("type").getAsString());
                JsonObject perticulars = debitRow.get("perticulars").getAsJsonObject();
                tranxDebitDetails.setSource("voucher");
                if (debitRow.get("type").getAsString().equalsIgnoreCase("dr")) {
                    tranxDebitDetails.setDr(debitRow.get("paid_amt").getAsString() != "" ? debitRow.get("paid_amt").getAsDouble() : 0.0);
                }
                if (debitRow.get("type").getAsString().equalsIgnoreCase("cr")) {
                    tranxDebitDetails.setCr(debitRow.get("paid_amt").getAsString() != "" ? debitRow.get("paid_amt").getAsDouble() : 0.0);
                }
                if (debitRow.has("payment_date") &&
                        !debitRow.get("payment_date").getAsString().equalsIgnoreCase(""))
//                    tranxDebitDetails.setPaymentDate(LocalDate.parse(debitRow.get("payment_date").getAsString()));
                    tranxDebitDetails.setPaymentDate(DateConvertUtil.convertStringToDate(debitRow.get("payment_date").getAsString()));

                tranxDebitDetails.setTransactionDate(tranxDate);
                if (perticulars.has("payableAmt"))
                    tranxDebitDetails.setPayableAmt(perticulars.get("payableAmt").getAsDouble());
                if (perticulars.has("selectedAmt"))
                    tranxDebitDetails.setSelectedAmt(perticulars.get("selectedAmt").getAsDouble());
                if (perticulars.has("remainingAmt"))
                    tranxDebitDetails.setRemainingAmt(perticulars.get("remainingAmt").getAsDouble());
                if (perticulars.has("isAdvanceCheck"))
                    tranxDebitDetails.setIsAdvance(perticulars.get("isAdvanceCheck").getAsBoolean());
                total_amt = debitRow.get("paid_amt").getAsDouble();
                tranxDebitDetails.setPaidAmt(total_amt);
                total_amt = debitRow.get("paid_amt").getAsDouble();
                TranxDebitNoteDetails mdebit = tranxDebitNoteDetailsRepository.save(tranxDebitDetails);
                /*DebitNote Bills Details*/
                JsonArray billList = new JsonArray();
                if (perticulars.has("billids")) {
                    billList = perticulars.get("billids").getAsJsonArray();
                    if (billList != null && billList.size() > 0) {
                        for (int j = 0; j < billList.size(); j++) {
                            JsonObject jsonBill = billList.get(j).getAsJsonObject();
                            TranxPurInvoice mPurInvoice = null;
                            Long bill_details_id = 0L;
                            TranxDNParticularBillDetails tranxbillDetails = null;
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("pur_invoice")) {
                                bill_details_id = jsonBill.get("bill_details_id").getAsLong();
                                if (bill_details_id == 0) {
                                    tranxbillDetails = new TranxDNParticularBillDetails();
                                    tranxbillDetails.setStatus(true);
                                    tranxbillDetails.setCreatedBy(users.getId());
                                } else {
                                    tranxbillDetails = tranxDNBillDetailsRepository.
                                            findByIdAndStatus(bill_details_id, true);
                                }
                            }
                            if (ledgerMaster != null) tranxbillDetails.setLedgerMaster(ledgerMaster);
                            tranxbillDetails.setTranxDebitNoteMaster(tranxdebitMaster.getId());
                            tranxbillDetails.setTranxDebitNoteDetails(mdebit.getId());
                            tranxbillDetails.setType(jsonBill.get("source").getAsString());
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsLong() == 0L)
                                    tranxbillDetails.setType("new_reference");
                            }
                            tranxbillDetails.setTotalAmt(jsonBill.get("total_amt").getAsDouble());
                            tranxbillDetails.setTransactionDate(LocalDate.parse(jsonBill.get("invoice_date").getAsString()));
                            tranxbillDetails.setAmount(jsonBill.get("amount").getAsDouble());
                            tranxbillDetails.setBalancingType(jsonBill.get("balancing_type").getAsString());
                            if (jsonBill.get("source").getAsString().equalsIgnoreCase("pur_invoice")) {
                                if (jsonBill.get("invoice_id").getAsString().equalsIgnoreCase("0")) {
                                    /**** creating New Reference if advanced amount is given *****/
                                    /*createDebitNote(tranxPaymentMaster, jsonBill.get("total_amt").getAsDouble(),
                                            ledgerMaster, jsonBill.get("invoice_id").getAsLong(), "update");*/
                                } else {
                                    mPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
                                    if (jsonBill.has("remaining_amt")) {
                                        mPurInvoice.setBalance(jsonBill.get("remaining_amt").getAsDouble());
                                        tranxPurInvoiceRepository.save(mPurInvoice);
                                    }
                                }
                                tranxbillDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxbillDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxbillDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxbillDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            } else if (jsonBill.get("source").getAsString().equalsIgnoreCase("debit_note")) {
                                TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference =
                                        repository.findByIdAndStatus(jsonBill.get("invoice_id").getAsLong(), true);
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
                                        repository.save(tranxDebitNoteNewReference);
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
                                tranxbillDetails.setTranxNo(jsonBill.get("invoice_no").getAsString());
                                tranxbillDetails.setTranxInvoiceId(jsonBill.get("invoice_id").getAsLong());
                                tranxbillDetails.setPaidAmt(jsonBill.get("paid_amt").getAsDouble());
                                tranxbillDetails.setRemainingAmt(jsonBill.get("remaining_amt").getAsDouble());
                            }
                            tranxDNBillDetailsRepository.save(tranxbillDetails);
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
                                TranxDNParticularBillDetails details = tranxDNBillDetailsRepository.
                                        findByTranxDebitNoteMasterAndTranxInvoiceIdAndStatus(debitMaster.getId(),
                                                invoiceId, true);
                                details.setStatus(false);
                                tranxDNBillDetailsRepository.save(details);
                            }
                        } else {
                            TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReference =
                                    repository.findByPaymentIdAndStatus(debitMaster.getId(), true);
                            if (tranxDebitNoteNewReference != null) {
                                tranxDebitNoteNewReference.setBalance(tranxDebitNoteNewReference.getTotalAmount());
                                tranxDebitNoteNewReference.setStatus(false);
                                repository.save(tranxDebitNoteNewReference);
                                /**** make status=0 against the purchase invoice id,and payment masterid
                                 in TranxPayment Details table so that in bills list we don't get the unselected bill again ****/

                            }
                        }
                    }
                }
                if (perticulars.has("debitDeleteRow")) {
                    JsonArray deletedDebitJson = perticulars.get("debitDeleteRow").getAsJsonArray();
                    for (JsonElement mDebitNoteDel : deletedDebitJson) {
                        String data[] = mDebitNoteDel.getAsString().split(",");
                        Long invoiceId = Long.parseLong(data[1]);
                        TranxDebitNoteNewReferenceMaster debitnoteMaster = repository.findByIdAndStatus(
                                invoiceId, true);
                        if (debitnoteMaster != null) {
                            debitnoteMaster.setBalance(debitnoteMaster.getTotalAmount());
                            TransactionStatus transactionStatus = transactionStatusRepository.findByIdAndStatus(1L, true);
                            debitnoteMaster.setTransactionStatus(transactionStatus);
                            repository.save(debitnoteMaster);
                            /**** make status=0 against the debinote invoice id, and payment master id ,
                             in TranxPayment Details table so that in bills list we don't get the unselected bill again ****/
                            TranxDNParticularBillDetails details = tranxDNBillDetailsRepository.
                                    findByTranxDebitNoteMasterAndTranxInvoiceIdAndStatus(debitMaster.getId(),
                                            invoiceId, true);
                            details.setStatus(false);
                            tranxDNBillDetailsRepository.save(details);
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
                            ledgerOpeningBalanceRepository.save(openingMaster);
                            /**** make status=0 against the debinote invoice id, and payment master id ,
                             in TranxPayment Details table so that in bills list we don't get the unselected bill again ****/
                        }
                    }
                }
                updateIntoPostings(mdebit, total_amt, tranxdebitMaster.getSource(), detailsId);
            }

            /* Remove all ledgers from DB if we found new input ledger id's while updating */
            for (Long mDblist : ledgerList) {
                if (!ledgerInputList.contains(mDblist)) {
                    debitnoteLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mDblist, tranxType.getId(), debitMaster.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), debitMaster.getTranscationDate(),
                                tranxType, debitMaster.getFiscalYear(), detail);
                    }
                    debitnoteLogger.info("removing unused previous ledger update done");
                }
            }
            response.addProperty("message", "Debitnote updated successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            //e.printStackTrace();
            debitnoteLogger.error("Error in upadate debit :->" + e.getMessage());
            //  debitLogger.error("Error in debit Creation :->" + e.getMessage());
            response.addProperty("message", "Error in debit update");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    private void updateIntoPostings(TranxDebitNoteDetails mdebit, double total_amt, String source, Long detailsId) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("DBTN");
        try {
            Boolean isLedgerContains = false;
            String tranxAction = "DR";
            if (mdebit.getType().equalsIgnoreCase("dr")) {

                if (source.equalsIgnoreCase("pur_invoice")) {
                    if (detailsId != 0) {
                        isLedgerContains = ledgerList.contains(mdebit.getLedgerMaster().getId());
                        ledgerInputList.add(mdebit.getLedgerMaster().getId());

                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                                findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(
                                        mdebit.getLedgerMaster().getId(), tranxType.getId(),
                                        mdebit.getTranxDebitNoteMaster().getId());
                        if (mLedger != null) {
                            mLedger.setAmount(total_amt);
                            mLedger.setTransactionDate(mdebit.getTranxDebitNoteMaster().getTranscationDate());
                            mLedger.setOperations("updated");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    } else {
                        ledgerCommonPostings.callToPostings(total_amt, mdebit.getLedgerMaster(), tranxType,
                                mdebit.getLedgerMaster().getAssociateGroups(),
                                mdebit.getTranxDebitNoteMaster().getFiscalYear(), mdebit.getBranch(),
                                mdebit.getOutlet(), mdebit.getTranxDebitNoteMaster().getTranscationDate(),
                                mdebit.getTranxDebitNoteMaster().getId(),
                                mdebit.getTranxDebitNoteMaster().getDebitnoteNewReferenceNo(),
                                "DR", true, "Debit note", "Insert");
                    }
                } else {
                    tranxAction = "CR";
                    if (detailsId != 0) {
                        isLedgerContains = ledgerList.contains(mdebit.getLedgerMaster().getId());
                        ledgerInputList.add(mdebit.getLedgerMaster().getId());

                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                                findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(
                                        mdebit.getLedgerMaster().getId(), tranxType.getId(),
                                        mdebit.getTranxDebitNoteMaster().getId());
                        if (mLedger != null) {
                            mLedger.setAmount(total_amt);
                            mLedger.setTransactionDate(mdebit.getTranxDebitNoteMaster().getTranscationDate());
                            mLedger.setOperations("updated");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    } else {
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(total_amt, mdebit.getLedgerMaster(),
                                tranxType, mdebit.getLedgerMaster().getAssociateGroups(),
                                mdebit.getTranxDebitNoteMaster().getFiscalYear(), mdebit.getBranch(),
                                mdebit.getOutlet(), mdebit.getTranxDebitNoteMaster().getTranscationDate(),
                                mdebit.getTranxDebitNoteMaster().getId(),
                                mdebit.getTranxDebitNoteMaster().getDebitnoteNewReferenceNo(),
                                "CR", true, "Debit Note", "Insert");
                    }
                }
            } else {
                if (source.equalsIgnoreCase("voucher") || source.equals("")) {
                    tranxAction = "CR";
                    if (detailsId != 0) {
                        isLedgerContains = ledgerList.contains(mdebit.getLedgerMaster().getId());
                        ledgerInputList.add(mdebit.getLedgerMaster().getId());

                        LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                                findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(
                                        mdebit.getLedgerMaster().getId(), tranxType.getId(),
                                        mdebit.getTranxDebitNoteMaster().getId());
                        if (mLedger != null) {
                            mLedger.setAmount(total_amt);
                            mLedger.setTransactionDate(mdebit.getTranxDebitNoteMaster().getTranscationDate());
                            mLedger.setOperations("updated");
                            ledgerTransactionPostingsRepository.save(mLedger);
                        }
                    } else {
                        /**** New Postings Logic *****/
                        ledgerCommonPostings.callToPostings(total_amt, mdebit.getLedgerMaster(),
                                tranxType, mdebit.getLedgerMaster().getAssociateGroups(),
                                mdebit.getTranxDebitNoteMaster().getFiscalYear(), mdebit.getBranch(),
                                mdebit.getOutlet(), mdebit.getTranxDebitNoteMaster().getTranscationDate(),
                                mdebit.getTranxDebitNoteMaster().getId(),
                                mdebit.getTranxDebitNoteMaster().getDebitnoteNewReferenceNo(),
                                "CR", true, "Debit Note", "Insert");
                    }
                }
            }

            Double amount = total_amt;
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, mdebit.getLedgerMaster().getId(),
                    tranxType, tranxAction, mdebit.getTranxDebitNoteMaster().getId(), mdebit.getLedgerMaster(),
                    mdebit.getTranxDebitNoteMaster().getTranscationDate(), mdebit.getTranxDebitNoteMaster().getFiscalYear(),
                    mdebit.getOutlet(), mdebit.getBranch(), mdebit.getTranxDebitNoteMaster().getTranxCode());
        } catch (Exception e) {
            e.printStackTrace();
            debitnoteLogger.error("Error in updateIntoPostings :->" + e.getMessage());
        }
    }

    /*get debit note by id*/
    public JsonObject getDebitById(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxDebitNoteDetails> list = new ArrayList<>();
        List<TranxDNParticularBillDetails> detailsList = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long debitId = Long.parseLong(request.getParameter("debit_id"));
            TranxDebitNoteNewReferenceMaster tranxDebitNoteNewReferenceMaster =
                    repository.findByIdAndOutletIdAndStatus(debitId, users.getOutlet().getId(), true);
            list = tranxDebitNoteDetailsRepository.findByTranxDebitNoteMasterIdAndStatus(tranxDebitNoteNewReferenceMaster.getId(), true);
            finalResult.addProperty("debit_no", tranxDebitNoteNewReferenceMaster.getDebitnoteNewReferenceNo());
            finalResult.addProperty("tranx_unique_code", tranxDebitNoteNewReferenceMaster.getTranxCode());
            finalResult.addProperty("debit_sr_no", tranxDebitNoteNewReferenceMaster.getSrno());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            finalResult.addProperty("tranx_date", simpleDateFormat.format(tranxDebitNoteNewReferenceMaster.getTranscationDate()));
            finalResult.addProperty("total_amt", tranxDebitNoteNewReferenceMaster.getTotalAmount());
            finalResult.addProperty("narrations", tranxDebitNoteNewReferenceMaster.getNarrations());
            finalResult.addProperty("source", tranxDebitNoteNewReferenceMaster.getSource());
            JsonArray row = new JsonArray();
            JsonArray billsArray = new JsonArray();
            if (list.size() > 0) {
                for (TranxDebitNoteDetails mdetails : list) {
                    JsonObject rpdetails = new JsonObject();
                    rpdetails.addProperty("details_id", mdetails.getId());
                    rpdetails.addProperty("type", mdetails.getType());
                    rpdetails.addProperty("ledger_type", mdetails.getLedgerType());
                    rpdetails.addProperty("total_amt", mdetails.getTotalAmount() != null ? mdetails.getTotalAmount() : 0.0 );
                    rpdetails.addProperty("balance", mdetails.getBalance());
                    rpdetails.addProperty("paid_amt", mdetails.getPaidAmt());
                    rpdetails.addProperty("adjusted_source", mdetails.getAdjustedSource() != null ? mdetails.getAdjustedSource() : "" );
                    rpdetails.addProperty("adjustment_status", mdetails.getAdjustmentStatus() != null ? mdetails.getAdjustmentStatus() : "");
                    rpdetails.addProperty("operations", mdetails.getOperations() != null ? mdetails.getOperations() : "");
                    rpdetails.addProperty("dr", mdetails.getDr());
                    rpdetails.addProperty("cr", mdetails.getCr());
                    rpdetails.addProperty("ledger_id", mdetails.getLedgerMaster().getId());
                    rpdetails.addProperty("ledgerName", mdetails.getLedgerMaster().getLedgerName());
                    rpdetails.addProperty("debitnoteTranxNo", mdetails.getPaymentTranxNo() != null ? mdetails.getPaymentTranxNo() : "");
                    rpdetails.addProperty("payment_date",
                            mdetails.getPaymentDate() != null ? mdetails.getPaymentDate().toString() : "");
                    rpdetails.addProperty("balancingMethod", mdetails.getLedgerMaster().getBalancingMethod() != null ?
                            generateSlugs.getSlug(mdetails.getLedgerMaster().getBalancingMethod().getBalancingMethod()) : "");
                    rpdetails.addProperty("payableAmt", mdetails.getPayableAmt() != null ? mdetails.getPayableAmt() : 0.0);
                    rpdetails.addProperty("selectedAmt",  mdetails.getSelectedAmt() != null ? mdetails.getSelectedAmt() : 0.0);
                    rpdetails.addProperty("remainingAmt",  mdetails.getRemainingAmt() != null ? mdetails.getRemainingAmt() : 0.0);
                    rpdetails.addProperty("isAdvanceCheck", mdetails.getIsAdvance()!= null ? mdetails.getIsAdvance() : false);
                    if (mdetails.getType() != null && mdetails.getType().equalsIgnoreCase("dr")) {
                        detailsList = tranxDNBillDetailsRepository.
                                findByTranxDebitNoteDetailsAndStatus(mdetails.getId(), true);
                        for (TranxDNParticularBillDetails mPerticular : detailsList) {
                            JsonObject mBill = new JsonObject();
                            mBill.addProperty("bill_details_id", mPerticular.getId());
                            mBill.addProperty("paid_amt", mPerticular.getPaidAmt());
                            mBill.addProperty("remaining_amt", mPerticular.getRemainingAmt());
                            mBill.addProperty("invoice_id", mPerticular.getTranxInvoiceId());
                            mBill.addProperty("invoice_no", mPerticular.getTranxNo());
                            mBill.addProperty("ledger_id", mdetails.getLedgerMaster().getId());
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
                    }
                    row.add(rpdetails);
                }
            }
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("debit_details", row);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            debitnoteLogger.error("Error in getDebitById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            debitnoteLogger.error("Error in getDebitById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject deletedebitnote(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxDebitNoteNewReferenceMaster debitNoteMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("DBTN");
        try {
            debitNoteMaster.setStatus(false);
            repository.save(debitNoteMaster);
            if (debitNoteMaster != null) {
                List<TranxDebitNoteDetails> tranxdebitNoteDetails = tranxDebitNoteDetailsRepository.findByTranxDebitNoteMasterIdAndStatus(debitNoteMaster.getId(), true);
                for (TranxDebitNoteDetails mDetail : tranxdebitNoteDetails) {
                    if (mDetail.getType().equalsIgnoreCase("CR"))
                        deletePostings(mDetail, mDetail.getPaidAmt(), "DR", "Delete");// Accounting Postings
                    else deletePostings(mDetail, mDetail.getPaidAmt(), "CR", "Delete");// Accounting Postings
                }
                /**** make status=0 to all ledgers of respective Debitnote voucher id, due to this we wont get
                 details of deleted invoice when we want get details of respective ledger ****/
                List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
                mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(tranxType.getId(), debitNoteMaster.getId(), true);
                for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                    try {
                        mPostings.setStatus(false);
                        ledgerTransactionPostingsRepository.save(mPostings);
                    } catch (Exception e) {
                        debitnoteLogger.error("Exception in Delete functionality for all ledgers of" + " deleted purchase invoice->" + e.getMessage());
                    }
                }
                jsonObject.addProperty("message", "DebitNote deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Error in debitNote deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            debitnoteLogger.error("Error in debitNote invoice Delete()->" + e.getMessage());
        }
        return jsonObject;
    }

    private void deletePostings(TranxDebitNoteDetails mDetail, Double paidAmt, String crdrType, String operation) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("DBTN");
        /**** New Postings Logic *****/
        ledgerCommonPostings.callToPostings(paidAmt, mDetail.getLedgerMaster(), tranxType, mDetail.getLedgerMaster().getAssociateGroups(), mDetail.getTranxDebitNoteMaster().getFiscalYear(), mDetail.getBranch(), mDetail.getOutlet(), mDetail.getTranxDebitNoteMaster().getTranscationDate(), mDetail.getTranxDebitNoteMaster().getId(), mDetail.getTranxDebitNoteMaster().getDebitnoteNewReferenceNo(), crdrType, true, tranxType.getTransactionName(), operation);

        /**** NEW METHOD FOR LEDGER POSTING ****/
        LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                mDetail.getLedgerMaster().getId(), tranxType.getId(), mDetail.getTranxDebitNoteMaster().getId(), true);
        if (ledgerDetail != null) {
            Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
            ledgerDetail.setAmount(0.0);
            ledgerDetail.setClosingAmount(closing);
            ledgerDetail.setStatus(false);
            LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

            /***** NEW METHOD FOR LEDGER POSTING *****/
            postingUtility.updateLedgerPostings(mDetail.getLedgerMaster(), mDetail.getTranxDebitNoteMaster().getTranscationDate(),
                    tranxType, mDetail.getTranxDebitNoteMaster().getFiscalYear(), detail);
        }
    }
}

