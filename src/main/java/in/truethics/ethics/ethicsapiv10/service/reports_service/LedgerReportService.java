package in.truethics.ethics.ethicsapiv10.service.reports_service;

import antlr.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.LedgerCommonPostings;
import in.truethics.ethics.ethicsapiv10.common.NumFormat;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.master.TransactionTypeMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.debit_note.TranxDebitNoteDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoice;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.InventoryDetailsPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerBalanceSummaryRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerOpeningClosingDetailRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionTypeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.contra_repository.TranxContraDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.debitnote_repository.TranxDebitNoteDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.journal_repository.TranxJournalDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.payment_repository.TranxPaymentPerticularsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurReturnsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesReturnRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.PurRegisterReportService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.GDuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static in.truethics.ethics.ethicsapiv10.service.reports_service.account_reports.PaymentReportService.stockLogger;

@Service
public class LedgerReportService {

    @Autowired
    FiscalYearRepository fiscalYearRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;

    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;
    @Autowired
    LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    LedgerTransactionPostingsRepository transactionDetailsRepository;
    @Autowired
    LedgerBalanceSummaryRepository ledgerBalanceSummaryRepository;
    @Autowired
    TranxPurInvoiceRepository tranxPurInvoiceRepository;
    @Autowired
    TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    TranxSalesReturnRepository tranxSalesReturnRepository;
    @Autowired
    TranxReceiptPerticularsDetailsRepository tranxReceiptPerticularsDetailsRepository;
    @Autowired
    TranxPurReturnsRepository tranxPurReturnsRepository;
    @Autowired
    TranxPaymentPerticularsDetailsRepository tranxPaymentPerticularsDetailsRepository;
    @Autowired
    TranxJournalDetailsRepository tranxJournalDetailsRepository;
    @Autowired
    TranxContraDetailsRepository tranxContraDetailsRepository;
    @Autowired
    TranxCreditNoteDetailsRepository tranxCreditNoteDetailsRepository;

    @Autowired
    LedgerTransactionPostingsRepository postingsRepository;
    @Autowired
    TranxDebitNoteDetailsRepository tranxDebitNoteDetailsRepository;

    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    @Autowired
    private TranxPaymentPerticularsRepository tranxPaymentPerticularsRepository;
    @Autowired
    private TranxPaymentMasterRepository tranxPaymentMasterRepository;
    @Autowired
    private TranxReceiptPerticularsRepository tranxReceiptPerticularsRepository;
    @Autowired
    private NumFormat numFormat;
    @Autowired
    private TransactionTypeMasterRepository transactionTypeMasterRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    private static final Logger productLogger = LogManager.getLogger(ProductService.class);
    static final Logger stockLogger = LogManager.getLogger(PurRegisterReportService.class);
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;

    public Object getLedgerTransactionsDetails(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject finalResponse = new JsonObject();

        JsonArray response = new JsonArray();
        List<LedgerTransactionPostings> mlist = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Long ledger_master_id = Long.valueOf(request.getParameter("id"));
            String startDate = "";
            String endDate = "";
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            Long branchId = null;
            FiscalYear fiscalYear = null;
            Boolean flag = false;
            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledger_master_id, true);
            if (paramMap.containsKey("startDate") && paramMap.containsKey("endDate")) {
                startDate = request.getParameter("startDate");
                startDatep = LocalDate.parse(startDate);
                endDate = request.getParameter("endDate");
                endDatep = LocalDate.parse(endDate);
                flag = true;
            } else {
                List<Object[]> list = new ArrayList<>();
                fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
                flag = false;
            }
            if (flag == true) {
                if (users.getBranch() != null) {
                    mlist = transactionDetailsRepository.findByDetailsBetweenDates(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id, startDatep, endDatep);
                } else {
                    mlist = transactionDetailsRepository.findByDetails(users.getOutlet().getId(), true, ledger_master_id, startDatep, endDatep);
                }
            } else {
                if (users.getBranch() != null) {
                    mlist = transactionDetailsRepository.findByDetailsBranch(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id);

                } else {
                    mlist = transactionDetailsRepository.findByDetailsFisc(users.getOutlet().getId(), true, ledger_master_id);
                }
            }
            JsonArray innerArr = new JsonArray();
            innerArr = getCommonDetails(mlist, users);
            Double openingStock = 0.0;
            openingStock = ledgerCommonPostings.getOpeningStock(ledger_master_id, users.getOutlet().getId(), branchId, startDatep, endDatep, flag, fiscalYear);
            finalResponse.addProperty("crdrType", ledgerMaster.getOpeningBalType().toLowerCase());
            finalResponse.add("response", innerArr);
            finalResponse.addProperty("opening_stock", Math.abs(openingStock));
            finalResponse.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            finalResponse.addProperty("message", "Failed To Load Data");
            finalResponse.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return finalResponse;
    }

    public Object getLedgerTransactionsDetailsWithDates(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<LedgerTransactionPostings> mlist = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Long ledger_master_id = Long.valueOf(request.getParameter("id"));
            LocalDate startDate = null;
            LocalDate endDate = null;
            Long branchId = null;
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
            }
            if (paramMap.containsKey("start_date") && paramMap.containsKey("end_date")) {
                startDate = LocalDate.parse(request.getParameter("start_date"));
                endDate = LocalDate.parse(request.getParameter("end_date"));
                mlist = transactionDetailsRepository.findByDetailsBetweenDates(users.getOutlet().getId(), branchId, true, ledger_master_id, startDate, endDate);
            }
            JsonArray innerArr = new JsonArray();
            innerArr = getCommonDetails(mlist, users);
            res.addProperty("d_start_date", startDate.toString());
            res.addProperty("d_end_date", endDate.toString());
            res.add("response", innerArr);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public JsonArray getCommonDetails(List<LedgerTransactionPostings> mlist, Users users) {
        JsonArray innerArr = new JsonArray();
        for (LedgerTransactionPostings ledgerTransactionDetails : mlist) {
            Double amt = 0.0;
            if (!ledgerTransactionDetails.getOperations().equalsIgnoreCase("Delete")) {
                JsonObject inside = new JsonObject();
                inside.addProperty("transaction_date", ledgerTransactionDetails.getTransactionDate().toString());
                inside.addProperty("invoice_no", ledgerTransactionDetails.getInvoiceNo());
                inside.addProperty("invoice_id", ledgerTransactionDetails.getTransactionId());// Invoice Id : 1 or 2
                Long tranx_type = ledgerTransactionDetails.getTransactionType().getId(); // Transactions Id : 1:Pur 3: Sales
                inside.addProperty("transaction_type", tranx_type);
                if (tranx_type == 1) {
                    TranxPurInvoice tranxPurInvoice;
                    if (users.getBranch() != null) {
                        tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true);
                    }
                    if (tranxPurInvoice != null) {
                        inside.addProperty("particulars", tranxPurInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurInvoice.getId());
                    }
                } else if (tranx_type == 2) {
                    TranxPurReturnInvoice tranxPurReturnInvoice;
                    if (users.getBranch() != null) {
                        tranxPurReturnInvoice = tranxPurReturnsRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        tranxPurReturnInvoice = tranxPurReturnsRepository.findByIdAndOutletIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true);
                    }
                    if (tranxPurReturnInvoice != null) {
                        inside.addProperty("particulars", tranxPurReturnInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurReturnInvoice.getId());
                    }
                } else if (tranx_type == 3) {
                    TranxSalesInvoice tranxSalesInvoice;
                    if (users.getBranch() != null) {
                        tranxSalesInvoice = tranxSalesInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        tranxSalesInvoice = tranxSalesInvoiceRepository.findByIdAndOutletIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true);
                    }
                    if (tranxSalesInvoice != null) {
                        inside.addProperty("particulars", tranxSalesInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesInvoice.getId());
                    }
                } else if (tranx_type == 4) {
                    TranxSalesReturnInvoice tranxSalesReturnInvoice;
                    if (users.getBranch() != null) {
                        tranxSalesReturnInvoice = tranxSalesReturnRepository.findByIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        tranxSalesReturnInvoice = tranxSalesReturnRepository.findByIdAndOutletIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true);
                    }
                    if (tranxSalesReturnInvoice != null) {
                        inside.addProperty("particulars", tranxSalesReturnInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesReturnInvoice.getId());
                    }
                } else if (tranx_type == 5) {
                    List<TranxReceiptPerticulars> tranxReceiptPerticulars;
                    if (users.getBranch() != null) {
                        tranxReceiptPerticulars = tranxReceiptPerticularsRepository.findByTranxReceiptMasterIdAndOutletIdAndBranchIdAndStatusAndTypeIgnoreCase(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true, "dr");
                    } else {
                        tranxReceiptPerticulars = tranxReceiptPerticularsRepository.findByTranxReceiptMasterIdAndOutletIdAndStatusAndTypeIgnoreCase(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true, "dr");
                    }
                    if (tranxReceiptPerticulars != null) {
                        inside.addProperty("particulars", tranxReceiptPerticulars.get(0).getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxReceiptPerticulars.get(0).getTranxReceiptMaster().getId());
                    }
                } else if (tranx_type == 6) {
                    List<TranxPaymentPerticulars> tranxPaymentPerticulars;
                    if (users.getBranch() != null) {
                        tranxPaymentPerticulars = tranxPaymentPerticularsRepository.findByTranxPaymentMasterIdAndOutletIdAndBranchIdAndStatusAndTypeIgnoreCase(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true, "cr");
                    } else {
                        tranxPaymentPerticulars = tranxPaymentPerticularsRepository.findByTranxPaymentMasterIdAndOutletIdAndStatusAndTypeIgnoreCase(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true, "cr");
                    }
                    if (tranxPaymentPerticulars != null && tranxPaymentPerticulars.size() > 0) {
                        inside.addProperty("particulars", tranxPaymentPerticulars.get(0).getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxPaymentPerticulars.get(0).getTranxPaymentMaster().getId());
                    }
                } else if (tranx_type == 7) {
                    List<TranxDebitNoteDetails> tranxDebitNoteDetails;
                    if (users.getBranch() != null) {
                        tranxDebitNoteDetails = tranxDebitNoteDetailsRepository.findByTranxDebitNoteMasterIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        tranxDebitNoteDetails = tranxDebitNoteDetailsRepository.findByTranxDebitNoteMasterIdAndOutletIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true);
                    }
                    if (tranxDebitNoteDetails != null) {
                        inside.addProperty("particulars", tranxDebitNoteDetails.get(0).getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxDebitNoteDetails.get(0).getTranxDebitNoteMaster().getId());
                    }
                } else if (tranx_type == 8) {
                    List<TranxCreditNoteDetails> tranxCreditNoteDetails;
                    if (users.getBranch() != null) {
                        tranxCreditNoteDetails = tranxCreditNoteDetailsRepository.findByTranxCreditNoteMasterIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        tranxCreditNoteDetails = tranxCreditNoteDetailsRepository.findByTranxCreditNoteMasterIdAndOutletIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true);
                    }
                    if (tranxCreditNoteDetails != null) {
                        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(tranxCreditNoteDetails.get(0).getLedgerMasterId(), true);
                        inside.addProperty("particulars", mLedger != null ? mLedger.getLedgerName() : "");
                        inside.addProperty("id", tranxCreditNoteDetails.get(0).getTranxCreditNoteMasterId());
                    }
                } else if (tranx_type == 9) {
                    List<TranxContraDetails> tranxContraDetails;
                    if (users.getBranch() != null) {
                        tranxContraDetails = tranxContraDetailsRepository.findByTranxContraMasterIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        tranxContraDetails = tranxContraDetailsRepository.findByTranxContraMasterIdAndOutletIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true);
                    }
                    if (tranxContraDetails != null) {
                        inside.addProperty("particulars", tranxContraDetails.get(0).getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxContraDetails.get(0).getTranxContraMaster().getId());
                    }
                } else if (tranx_type == 10) {
                    List<TranxJournalDetails> tranxJournalDetails;
                    if (users.getBranch() != null) {
                        tranxJournalDetails = tranxJournalDetailsRepository.findByTranxJournalMasterIdAndOutletIdAndBranchIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                    } else {
                        tranxJournalDetails = tranxJournalDetailsRepository.findByTranxJournalMasterIdAndOutletIdAndStatus(ledgerTransactionDetails.getTransactionId(), users.getOutlet().getId(), true);
                    }
                    if (tranxJournalDetails != null) {
                        inside.addProperty("particulars", tranxJournalDetails.get(0).getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxJournalDetails.get(0).getTranxJournalMaster().getId());
                    }
                }
                inside.addProperty("voucher_type", ledgerTransactionDetails.getTransactionType().getTransactionName());
                if (ledgerTransactionDetails.getLedgerType().equalsIgnoreCase("CR")) {
                    inside.addProperty("credit", numFormat.numFormat(Math.abs(ledgerTransactionDetails.getAmount())));
                    inside.addProperty("debit", 0);
                    amt = ledgerTransactionDetails.getAmount();

                } else {
                    inside.addProperty("credit", 0);
                    inside.addProperty("debit", numFormat.numFormat(Math.abs(ledgerTransactionDetails.getAmount())));
                    amt = ledgerTransactionDetails.getAmount();
                }
                if (amt != 0.0) innerArr.add(inside);
            }
        }
        return innerArr;
    }

    public JsonArray getMobileCommonDetails(List<LedgerTransactionPostings> mlist) {
        JsonArray innerArr = new JsonArray();
        for (LedgerTransactionPostings ledgerTransactionDetails : mlist) {
            if (!ledgerTransactionDetails.getOperations().equalsIgnoreCase("Delete")) {
                JsonObject inside = new JsonObject();
                inside.addProperty("transaction_date", ledgerTransactionDetails.getTransactionDate().toString());
                inside.addProperty("invoice_no", ledgerTransactionDetails.getInvoiceNo());
                inside.addProperty("invoice_id", ledgerTransactionDetails.getTransactionId());// Invoice Id : 1 or 2
                Long tranx_type = ledgerTransactionDetails.getTransactionType().getId();// Transactions Id : 1:Pur 3: Sales
                if (tranx_type == 1) {
                    TranxPurInvoice tranxPurInvoice;
                    tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndStatus(ledgerTransactionDetails.getTransactionId(), true);
                    if (tranxPurInvoice != null) {
                        inside.addProperty("particulars", tranxPurInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurInvoice.getId());
                    }
                } else if (tranx_type == 2) {
                    TranxPurReturnInvoice tranxPurReturnInvoice;

                    tranxPurReturnInvoice = tranxPurReturnsRepository.findByIdAndStatus(ledgerTransactionDetails.getTransactionId(), true);

                    if (tranxPurReturnInvoice != null) {
                        inside.addProperty("particulars", tranxPurReturnInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurReturnInvoice.getId());
                    }
                } else if (tranx_type == 3) {
                    TranxSalesInvoice tranxSalesInvoice;

                    tranxSalesInvoice = tranxSalesInvoiceRepository.findByIdAndStatus(ledgerTransactionDetails.getTransactionId(), true);

                    if (tranxSalesInvoice != null) {
                        inside.addProperty("particulars", tranxSalesInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesInvoice.getId());
                    }
                } else if (tranx_type == 4) {
                    TranxSalesReturnInvoice tranxSalesReturnInvoice;

                    tranxSalesReturnInvoice = tranxSalesReturnRepository.findByIdAndStatus(ledgerTransactionDetails.getTransactionId(), true);

                    if (tranxSalesReturnInvoice != null) {
                        inside.addProperty("particulars", tranxSalesReturnInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesReturnInvoice.getId());
                    }
                } else if (tranx_type == 5) {
                    TranxReceiptPerticularsDetails tranxReceiptPerticularsDetails;

                    tranxReceiptPerticularsDetails = tranxReceiptPerticularsDetailsRepository.findBylistStatus(ledgerTransactionDetails.getTransactionId(), true);

                    if (tranxReceiptPerticularsDetails != null) {
                        inside.addProperty("particulars", tranxReceiptPerticularsDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxReceiptPerticularsDetails.getId());
                    }
                } else if (tranx_type == 6) {
                    TranxPaymentPerticularsDetails tranxPaymentPerticulars = null;

                    tranxPaymentPerticulars = tranxPaymentPerticularsDetailsRepository.findListStatus(ledgerTransactionDetails.getTransactionId(), true);

                    if (tranxPaymentPerticulars != null) {
                        inside.addProperty("particulars", tranxPaymentPerticulars.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxPaymentPerticulars.getId());
                    }
                } else if (tranx_type == 7) {
                    TranxDebitNoteDetails tranxDebitNoteDetails = null;

                    tranxDebitNoteDetails = tranxDebitNoteDetailsRepository.findByIdAndStatus(ledgerTransactionDetails.getTransactionId(), true);

                    if (tranxDebitNoteDetails != null) {
                        inside.addProperty("particulars", tranxDebitNoteDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxDebitNoteDetails.getId());
                    }
                } else if (tranx_type == 8) {
                    TranxCreditNoteDetails tranxCreditNoteDetails = null;

                    tranxCreditNoteDetails = tranxCreditNoteDetailsRepository.findByIdAndStatus(ledgerTransactionDetails.getTransactionId(), true);

                    if (tranxCreditNoteDetails != null) {
                        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(tranxCreditNoteDetails.getLedgerMasterId(), true);
                        inside.addProperty("particulars", mLedger != null ? mLedger.getLedgerName() : "");
                        inside.addProperty("id", tranxCreditNoteDetails.getId());
                    }
                } else if (tranx_type == 9) {
                    TranxContraDetails tranxContraDetails = null;

                    tranxContraDetails = tranxContraDetailsRepository.findByIdAndStatus(ledgerTransactionDetails.getTransactionId(), true);

                    if (tranxContraDetails != null) {
                        inside.addProperty("particulars", tranxContraDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxContraDetails.getId());
                    }
                } else if (tranx_type == 10) {
                    TranxJournalDetails tranxJournalDetails = null;

                    tranxJournalDetails = tranxJournalDetailsRepository.findByIdAndStatus(ledgerTransactionDetails.getTransactionId(), true);

                    if (tranxJournalDetails != null) {
                        inside.addProperty("particulars", tranxJournalDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxJournalDetails.getId());
                    }
                }
                inside.addProperty("voucher_type", ledgerTransactionDetails.getTransactionType().getTransactionName());
                if (ledgerTransactionDetails.getLedgerType().equalsIgnoreCase("CR")) {
                    inside.addProperty("credit", numFormat.numFormat(ledgerTransactionDetails.getAmount()));
                    inside.addProperty("debit", 0);
                    inside.addProperty("TransactionType", "CR");
                } else {
                    inside.addProperty("credit", 0);
                    inside.addProperty("debit", numFormat.numFormat(ledgerTransactionDetails.getAmount()));
                    inside.addProperty("TransactionType", "DR");
                }
                innerArr.add(inside);
            }
        }
        return innerArr;
    }

    public Object getTransactionsDetailsReports(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        List<LedgerTransactionPostings> mlist = new ArrayList<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        Long tranx_type = Long.parseLong(request.getParameter("transaction_type"));
        Long invoice_id = Long.parseLong(request.getParameter("id"));
        return res;
    }


    public Object getMonthwiseTranscationDetails(HttpServletRequest request) {
        JsonObject res = new JsonObject();
        Long id = 0L;
        Double total_month_sum = 0.0;
        Double credit_total = 0.0;
        JsonObject closOpen = new JsonObject();
        List<Object[]> list = new ArrayList<>();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            String endDate = null;
            LocalDate endDatep = null;
            String startDate = null;
            LocalDate startDatep = null;
            Double opening_bal = 0.0;
            Long ledger_master_id = Long.valueOf(request.getParameter("ledger_master_id"));
            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledger_master_id, true);
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;
            Double sumCR = 0.0;
            Double sumDR = 0.0;
            Double closingBalance = 0.0;
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

            //****This Code For Users Dates Selection Between Start And End Date Manually****//
            if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
                endDate = request.getParameter("end_date");
                endDatep = LocalDate.parse(endDate);
                startDate = request.getParameter("start_date");
                startDatep = LocalDate.parse(startDate);

            } else {
                //****This Code For Load Data Default Current Year From Automatically load And Select Fiscal Year From Fiscal Year Table****//
                List<Object[]> nlist = new ArrayList<>();
                nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();
                for (int i = 0; i < nlist.size(); i++) {
                    Object obj[] = nlist.get(i);
//                    System.out.println("start Date:" + obj[0].toString());
//                    System.out.println("end Date:" + obj[1].toString());
                    startDatep = LocalDate.parse(obj[0].toString());
                    endDatep = LocalDate.parse(obj[1].toString());
                }

                //**Openig Balance for Fiscal Year**//
                if (users.getBranch() != null) {
                    opening_bal = ledgerMasterRepository.findByIdAndOutletIdAndBranchIdAndStatuslm(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id);
                } else {
                    opening_bal = ledgerMasterRepository.findByIdAndOutletIdAndStatuslm(users.getOutlet().getId(), true, ledger_master_id);
                }
            }

            currentStartDate = startDatep;
            currentEndDate = endDatep;
            if (startDatep.isAfter(endDatep)) {
                System.out.println("Start Date Should not be After");
                return 0;
            }
            JsonArray innerArr = new JsonArray();
            while (startDatep.isBefore(endDatep)) {
                Double closing_bal = 0.0;
                String month = startDatep.getMonth().name();
                int monthnum = startDatep.getMonth().getValue();
                System.out.println();
                LocalDate startMonthDate = startDatep;
                LocalDate endMonthDate = startDatep.withDayOfMonth(startDatep.lengthOfMonth());
                startDatep = endMonthDate.plusDays(1);

                if (endDate != null) {
                    //****This Code For Users Dates Selection Between Start And End Date Manually****//
                    if (users.getBranch() != null) {
                        list = ledgerOpeningClosingDetailRepository.findByTotalAmountByMonthStartDateAndEndDateAndBranchAndOutletAndStatus31(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
                    } else {
                        list = ledgerOpeningClosingDetailRepository.findByTotalAmountByMonthStartDateAndEndDateAndOutletAndStatus31(users.getOutlet().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
                    }
                } else {
                    //****This Code For Load Data Default Current Year From Automatically load And Select Fiscal Year From Fiscal Year Table****//
                    try {
                        if (users.getBranch() != null) {
                            list = ledgerOpeningClosingDetailRepository.findByTotalAmountByMonthStartDateAndEndDateAndBranchAndOutletAndStatus31(users.getOutlet().getId(), users.getBranch().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
                        } else {
                            list = ledgerOpeningClosingDetailRepository.findByTotalAmountByMonthStartDateAndEndDateAndOutletAndStatus31(users.getOutlet().getId(), true, ledger_master_id, startMonthDate, endMonthDate);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Exception:" + e.getMessage());
                    }
                }

                sumCR = ledgerOpeningClosingDetailRepository.findsumCR1(ledger_master_id, monthnum);
                System.out.println("sumCR>>>>>>>>>" + monthnum);
                JsonObject inside = new JsonObject();
                for (int i = 0; i < list.size(); i++) {
                    System.out.println("list" + list);
                    Object[] objp = list.get(i);
                    credit_total = Double.parseDouble(objp[0].toString());
//                    sumCR = Double.parseDouble(objp[0].toString());

                    if (objp[1] != null) ledger_master_id = Long.parseLong(objp[1].toString());
                    LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(ledger_master_id, true);
                    inside.addProperty("total_balance",  credit_total!=null ? credit_total : 0.00);
                    inside.addProperty("closing_balance", sumCR!=null ? sumCR : 0.00);

                    if ( (ledgerMaster!= null && ledgerMaster.getFoundations().getId() == 1) || (ledgerMaster!=null && ledgerMaster.getFoundations().getId() == 4)) {
                        if (credit_total > 0) {
                            inside.addProperty("type", "DR");
                        } else {
                            inside.addProperty("type", "");
                        }
                    } else {
                        if (credit_total > 0) {
                            inside.addProperty("type", "CR");
                        } else {
                            inside.addProperty("type", "");
                        }

                    }
                    if (objp[2] != null) {
                        if (objp[2].toString().equalsIgnoreCase("DR")) {
                            inside.addProperty("debit", credit_total);
                            inside.addProperty("credit", 0.0);

                        } else {
                            inside.addProperty("credit", credit_total);
                            inside.addProperty("debit", 0.0);
                        }
                    } else {
                        inside.addProperty("debit", 0.0);
                        inside.addProperty("credit", 0.0);
                    }

                    inside.addProperty("month_name", month);
                    inside.addProperty("closing_balance", sumCR!=null? sumCR :0.00);
                    inside.addProperty("start_date", startMonthDate.toString());
                    inside.addProperty("end_date", endMonthDate.toString());
                    innerArr.add(inside);
                }
            }

            System.out.println("closingBalance " + closingBalance + " " + sumDR + " " + sumCR);

            res.addProperty("company_name", ledgerMaster.getOutlet().getCompanyName());
            res.addProperty("d_start_date", currentStartDate.toString());
            res.addProperty("d_end_date", currentEndDate.toString());
            res.add("response", innerArr);
            res.addProperty("opening_bal", sumCR);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;

    }


    public Object getTranxDetailofMonth(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Double sumCR = 0.0;
        Double sumDR = 0.0;
        Double closingBalance = 0.0;
        List<LedgerOpeningClosingDetail> clOpening = new ArrayList<>();

        List<LedgerOpeningClosingDetail> mlist = new ArrayList<>();
        List<String> mopening = new ArrayList<>();
        Map<String, String[]> paraMap = request.getParameterMap();
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            Long ledger_master_id = Long.valueOf(request.getParameter("ledger_master_id"));

            LocalDate startDate = null;
            LocalDate endDate = null;

            String durations = null;
            Long branchId = null;
            if (paraMap.containsKey("start_date") && paraMap.containsKey("end_date")) {
                startDate = LocalDate.parse(request.getParameter("start_date"));

                endDate = LocalDate.parse(request.getParameter("end_date"));
            } else if (paraMap.containsKey("duration")) {
                durations = request.getParameter("duration");
                if (durations.equalsIgnoreCase("month")) {
                    //for finding first and last day of current month
                    LocalDate thisMonth = LocalDate.now();
                    String fDay = thisMonth.withDayOfMonth(1).toString();
                    String lDay = thisMonth.withDayOfMonth(thisMonth.lengthOfMonth()).toString();
                    startDate = LocalDate.parse(fDay);
                    endDate = LocalDate.parse(lDay);

                } else if (durations.equalsIgnoreCase("lastMonth")) {
                    //for finding first day and last day of previous month
                    Calendar aCalendar = Calendar.getInstance();
                    // add -1 month to current month
                    aCalendar.add(Calendar.MONTH, -1);
                    // set DATE to 1, so first date of previous month
                    aCalendar.set(Calendar.DATE, 1);
                    Date firstDateOfPreviousMonth = aCalendar.getTime();
                    // set actual maximum date of previous month
                    aCalendar.set(Calendar.DATE, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    //read it
                    Date lastDateOfPreviousMonth = aCalendar.getTime();

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String firstDay = df.format(firstDateOfPreviousMonth);  //here we get the first day of last month
                    String lastDay = df.format(lastDateOfPreviousMonth);    //here we get the last day of last month
                    startDate = LocalDate.parse(firstDay);
                    endDate = LocalDate.parse(lastDay);

                } else if (durations.equalsIgnoreCase("halfYear")) {
                    //for finding first and second half year start day and end day
                    LocalDate currentDate = LocalDate.now();
                    //for first half-year
                    LocalDate lastYear = currentDate.minusYears(1);
                    LocalDate firstDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 1, 1);
                    LocalDate lastDayOfFirstHalf = LocalDate.of(lastYear.getYear(), 6, 30);

                    // Second half-year
                    LocalDate firstDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 7, 1);
                    LocalDate lastDayOfSecondHalf = LocalDate.of(lastYear.getYear(), 12, 31);

                    // Format the dates in dd-MM-yyyy format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    String firstDayFirstHalfFormatted = firstDayOfFirstHalf.format(formatter);
                    String lastDayFirstHalfFormatted = lastDayOfFirstHalf.format(formatter);

                    String firstDaySecondHalfFormatted = firstDayOfSecondHalf.format(formatter);
                    String lastDaySecondHalfFormatted = lastDayOfSecondHalf.format(formatter);
                    System.out.println("firstDayFirstHalfFormatted " + firstDayFirstHalfFormatted + " lastDayFirstHalfFormatted " + lastDayFirstHalfFormatted);
                    System.out.println("firstDaySecondHalfFormatted " + firstDaySecondHalfFormatted + "  lastDaySecondHalfFormatted " + lastDaySecondHalfFormatted);
                    startDate = LocalDate.parse(firstDaySecondHalfFormatted);
                    endDate = LocalDate.parse(lastDaySecondHalfFormatted);
                } else if (durations.equalsIgnoreCase("fullYear")) {
                    List<Object[]> nlist = new ArrayList<>();
                    nlist = fiscalYearRepository.findByStartDateAndEndDateOutletIdAndBranchIdAndStatusLimit();
                    for (int i = 0; i < nlist.size(); i++) {
                        Object obj[] = nlist.get(i);
//                    System.out.println("start Date:" + obj[0].toString());
//                    System.out.println("end Date:" + obj[1].toString());
                        startDate = LocalDate.parse(obj[0].toString());
                        endDate = LocalDate.parse(obj[1].toString());
                    }
                }
            }

            System.out.println("startDate " + startDate + " endDayt " + endDate);

            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
            }
            mopening = ledgerMasterRepository.findOpeningByIdAndStatus(ledger_master_id, true);
            System.out.println("mopening" + mopening);
            String data[] = mopening.get(0).split(",");
            System.out.println("closingBalance " + closingBalance + " " + sumDR + " " + sumCR);


            mlist = ledgerOpeningClosingDetailRepository.findLedgerByIdAndDate(ledger_master_id, startDate, endDate);

            System.out.println("mlist" + mlist.size());
            JsonArray innerArr = new JsonArray();

            for (LedgerOpeningClosingDetail LedgerOpeningClosingDetail : mlist) {
                JsonObject inside = new JsonObject();
                inside.addProperty("transaction_date", LedgerOpeningClosingDetail.getTranxDate().toString());
                inside.addProperty("invoice_no", LedgerOpeningClosingDetail.getTranxId());
                inside.addProperty("invoice_id", LedgerOpeningClosingDetail.getTranxId());
                inside.addProperty("Closing_balance", LedgerOpeningClosingDetail.getClosingAmount());
                inside.addProperty("unique_code", LedgerOpeningClosingDetail.getLedgerMaster().getUniqueCode());
                Long tranx_type = LedgerOpeningClosingDetail.getTranxTypeId();
                TransactionTypeMaster typeMaster = transactionTypeMasterRepository.findById(tranx_type).get();
                if (tranx_type == 1) {
                    TranxPurInvoice tranxPurInvoice;
                    if (users.getBranch() != null) {
                        tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxPurInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurInvoice.getId());
                        inside.addProperty("invoice_no", tranxPurInvoice.getVendorInvoiceNo());

                    } else {
                        tranxPurInvoice = tranxPurInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), branchId, true);
                        inside.addProperty("particulars", tranxPurInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurInvoice.getId());
                        inside.addProperty("invoice_no", tranxPurInvoice.getVendorInvoiceNo());

//                        inside.addProperty("closing_balance",tranxPurInvoice.getBalance());
                    }
                } else if (tranx_type == 2) {
                    TranxPurReturnInvoice tranxPurReturnInvoice;
                    if (users.getBranch() != null) {
                        tranxPurReturnInvoice = tranxPurReturnsRepository.findByIdAndOutletIdAndBranchIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxPurReturnInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurReturnInvoice.getId());
                        inside.addProperty("invoice_no", tranxPurReturnInvoice.getPurRtnNo());

                    } else {
                        tranxPurReturnInvoice = tranxPurReturnsRepository.findByIdAndOutletIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxPurReturnInvoice.getSundryCreditors().getLedgerName());
                        inside.addProperty("id", tranxPurReturnInvoice.getId());
                        inside.addProperty("invoice_no", tranxPurReturnInvoice.getPurRtnNo());

                    }
                } else if (tranx_type == 3) {
                    TranxSalesInvoice tranxSalesInvoice;
                    if (users.getBranch() != null) {
                        tranxSalesInvoice = tranxSalesInvoiceRepository.findByIdAndOutletIdAndBranchIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxSalesInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesInvoice.getId());
                        inside.addProperty("invoice_no", tranxSalesInvoice.getSalesInvoiceNo());

                    } else {
                        tranxSalesInvoice = tranxSalesInvoiceRepository.findByIdAndOutletIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxSalesInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesInvoice.getId());
                        inside.addProperty("invoice_no", tranxSalesInvoice.getSalesInvoiceNo());

                    }
                } else if (tranx_type == 4) {
                    TranxSalesReturnInvoice tranxSalesReturnInvoice;
                    if (users.getBranch() != null) {
                        tranxSalesReturnInvoice = tranxSalesReturnRepository.findByIdAndOutletIdAndBranchIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxSalesReturnInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesReturnInvoice.getId());
                        inside.addProperty("invoice_no", tranxSalesReturnInvoice.getSalesReturnNo());

                    } else {
                        tranxSalesReturnInvoice = tranxSalesReturnRepository.findByIdAndOutletIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxSalesReturnInvoice.getSundryDebtors().getLedgerName());
                        inside.addProperty("id", tranxSalesReturnInvoice.getId());
                        inside.addProperty("invoice_no", tranxSalesReturnInvoice.getSalesReturnNo());

                    }
                } else if (tranx_type == 5) {
                    TranxReceiptPerticularsDetails tranxReceiptPerticularsDetails;
                    if (users.getBranch() != null) {
                        tranxReceiptPerticularsDetails = tranxReceiptPerticularsDetailsRepository.findByTranxReceiptMasterIdAndOutletIdAndBranchIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxReceiptPerticularsDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxReceiptPerticularsDetails.getId());
                        inside.addProperty("invoice_no", tranxReceiptPerticularsDetails.getTranxNo());

                    } else {
                        tranxReceiptPerticularsDetails = tranxReceiptPerticularsDetailsRepository.findByTranxReceiptMasterIdAndOutletIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxReceiptPerticularsDetails != null ? tranxReceiptPerticularsDetails.getLedgerMaster().getLedgerName() : "");
                        inside.addProperty("id", tranxReceiptPerticularsDetails.getId());
                        inside.addProperty("invoice_no", tranxReceiptPerticularsDetails.getTranxNo());

                    }
                } else if (tranx_type == 6) {
                    TranxPaymentPerticularsDetails tranxPaymentPerticulars;
                    if (users.getBranch() != null) {
                        tranxPaymentPerticulars = tranxPaymentPerticularsDetailsRepository.findByTranxPaymentMasterIdAndOutletIdAndBranchIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId(), true);
                        inside.addProperty("particulars", tranxPaymentPerticulars.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxPaymentPerticulars.getId());
                        inside.addProperty("invoice_no", tranxPaymentPerticulars.getTranxNo());

                    } else {
                        tranxPaymentPerticulars = tranxPaymentPerticularsDetailsRepository.findByTranxPaymentMasterIdAndOutletIdAndStatus(LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), true);
                        inside.addProperty("particulars", tranxPaymentPerticulars.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxPaymentPerticulars.getId());
                        inside.addProperty("invoice_no", tranxPaymentPerticulars.getTranxNo());

                    }
                } else if (tranx_type == 7) {
                    TranxDebitNoteDetails tranxDebitNoteDetails;
                    if (users.getBranch() != null) {
                        tranxDebitNoteDetails = tranxDebitNoteDetailsRepository.findByStatusAndTranxDebitNoteMasterIdAndOutletIdAndBranchId(true,LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId());
                        inside.addProperty("particulars", tranxDebitNoteDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxDebitNoteDetails.getId());
                        inside.addProperty("invoice_no", tranxDebitNoteDetails.getPaymentTranxNo());

                    } else {
                        tranxDebitNoteDetails = tranxDebitNoteDetailsRepository.findByStatusAndTranxDebitNoteMasterIdAndOutletId(true,LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId());
                        inside.addProperty("particulars", tranxDebitNoteDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxDebitNoteDetails.getId());
                        inside.addProperty("invoice_no", tranxDebitNoteDetails.getPaymentTranxNo());

                    }
                } else if (tranx_type == 8) {
                    TranxCreditNoteDetails tranxCreditNoteDetails = null;
                    if (users.getBranch() != null) {
                        tranxCreditNoteDetails = tranxCreditNoteDetailsRepository.findByStatusAndTranxCreditNoteMasterIdAndOutletIdAndBranchId(true,LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId());
                        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                                tranxCreditNoteDetails.getLedgerMasterId(), true);
                        inside.addProperty("particulars", mLedger != null ? mLedger.getLedgerName() : "");
                        inside.addProperty("id", tranxCreditNoteDetails.getId());
                        inside.addProperty("invoice_no", tranxCreditNoteDetails.getPaymentTranxNo());

                    } else {
                        tranxCreditNoteDetails = tranxCreditNoteDetailsRepository.findByStatusAndTranxCreditNoteMasterIdAndOutletId(true,LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId());
                        LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(
                                tranxCreditNoteDetails.getLedgerMasterId(), true);
                        inside.addProperty("particulars", mLedger != null ? mLedger.getLedgerName() : "");
                        inside.addProperty("id", tranxCreditNoteDetails.getId());
                        inside.addProperty("invoice_no", tranxCreditNoteDetails.getPaymentTranxNo());

                    }
                } else if (tranx_type == 9) {
                    TranxContraDetails tranxContraDetails;
                    if (users.getBranch() != null) {
                        tranxContraDetails = tranxContraDetailsRepository.findByStatusAndTranxContraMasterIdAndOutletIdAndBranchId(true, LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId());
                        inside.addProperty("particulars", tranxContraDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxContraDetails.getId());
                        inside.addProperty("invoice_no", tranxContraDetails.getTranxContraMaster().getContraNo());

                    } else {
                        tranxContraDetails = tranxContraDetailsRepository.findByStatusAndTranxContraMasterIdAndOutletId(true,LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId());
                        inside.addProperty("particulars", tranxContraDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxContraDetails.getId());
                        inside.addProperty("invoice_no", tranxContraDetails.getTranxContraMaster().getContraNo());

                    }
                } else if (tranx_type == 10) {
                    TranxJournalDetails tranxJournalDetails;
                    if (users.getBranch() != null) {
                        tranxJournalDetails = tranxJournalDetailsRepository.findByStatusAndTranxJournalMasterIdAndOutletIdAndBranchId(true, LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId(), users.getBranch().getId());
                        inside.addProperty("particulars", tranxJournalDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxJournalDetails.getId());
                        inside.addProperty("invoice_no", tranxJournalDetails.getTranxJournalMaster().getJournalNo());

                    } else {
                        tranxJournalDetails = tranxJournalDetailsRepository.findByStatusAndTranxJournalMasterIdAndOutletId(true,LedgerOpeningClosingDetail.getTranxId(), users.getOutlet().getId());
                        inside.addProperty("particulars", tranxJournalDetails.getLedgerMaster().getLedgerName());
                        inside.addProperty("id", tranxJournalDetails.getId());
                        inside.addProperty("invoice_no", tranxJournalDetails.getTranxJournalMaster().getJournalNo());

                    }
                }
                inside.addProperty("voucher_type", typeMaster != null ? typeMaster.getTransactionName() : "");
//                inside.addProperty("tranx_unique_code", LedgerOpeningClosingDetail.getTranxTypeId().getTransactionCode());
                LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledger_master_id, true);
                inside.addProperty("ledgerName", ledgerMaster.getLedgerName());
                inside.addProperty("transc_type", LedgerOpeningClosingDetail.getTranxAction());

                res.addProperty("companyName", ledgerMaster.getOutlet().getCompanyName());
//                System.out.println("ledgerTransactionPostings.getLedgerType()"+ ledgerTransactionPostings.getLedgerType());
                if (LedgerOpeningClosingDetail.getTranxAction().equalsIgnoreCase("CR")) {
                    inside.addProperty("credit", LedgerOpeningClosingDetail.getAmount());
                    inside.addProperty("debit", 0.00);
                } else {
                    inside.addProperty("debit", LedgerOpeningClosingDetail.getAmount());
                    inside.addProperty("credit", 0.00);
                }
//                inside.addProperty("debit", ledgerTransactionPostings.getLedgerMaster().getDebit());
//                inside.addProperty("credit", ledgerTransactionPostings.getCredit());

                innerArr.add(inside);
            }
            Double openingAmt = ledgerOpeningClosingDetailRepository.findLedgerOpeningAmt(ledger_master_id, startDate);
            res.addProperty("Opening_bal", openingAmt != null ? openingAmt : 0.0);
//            res.addProperty("opening_bal_type", (data[1] != null || data[1] != "") ? data[1] : "");
            res.addProperty("d_start_date", startDate != null ? startDate.toString() : "");
            res.addProperty("d_end_date", endDate != null ? endDate.toString() : "");
            res.add("response", innerArr);
            res.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("message", "Failed To Load Data");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public Object getMobileLedgerTransactionsDetails(Map<String, String> request) {
        JsonArray result = new JsonArray();
        JsonObject finalResponse = new JsonObject();

        JsonArray response = new JsonArray();
        List<LedgerTransactionPostings> mlist = new ArrayList<>();
//        Map<String, String[]> paramMap = request.get();
        try {
//            Users users = jwtRequestFilter.getUserDataFromToken(request.get("Authorization").substring(7));
            Long ledger_master_id = Long.valueOf(request.get("ledgerId"));
            String startDate = "";
            String endDate = "";
            LocalDate endDatep = null;
            LocalDate startDatep = null;
            Long branchId = null;
            FiscalYear fiscalYear = null;
            Boolean flag = false;
            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(ledger_master_id, true);

            if (!request.get("end_date").equalsIgnoreCase("") && !request.get("start_date").equalsIgnoreCase("")) {
                endDatep = LocalDate.parse(request.get("end_date").toString());

                startDatep = LocalDate.parse(request.get("start_date"));
                flag = true;
            } else {
                List<Object[]> list = new ArrayList<>();
                fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
                flag = false;
            }
            mlist = transactionDetailsRepository.findByMobileDetailsFisc(true, ledger_master_id);

            JsonArray innerArr = new JsonArray();
            innerArr = getMobileCommonDetails(mlist);

            Double openingStock = 0.0;
            openingStock = ledgerCommonPostings.getmobileOpeningStock(ledger_master_id, startDatep, endDatep, flag, fiscalYear);

            finalResponse.addProperty("crdrType", ledgerMaster.getOpeningBalType().toLowerCase());
            finalResponse.add("response", innerArr);
            finalResponse.addProperty("opening_stock", Math.abs(openingStock));
            finalResponse.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            finalResponse.addProperty("message", "Failed To Load Data");
            finalResponse.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return finalResponse;
    }

    //API for Export TO Excel for Ledger Report 1
    public InputStream exportLedgerReport1(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"LEDGER NAME", "PRINCIPLE NAME", "TYPE", "BALANCING METHOD", "DEBIT AMOUNT", "CREDIT AMOUNT"};

                    Sheet sheet = workbook.createSheet("ledger_report");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    double sumOfDr = 0;
                    double sumOfCr = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();

                        JsonObject prodUnit = null;
                        System.out.println("batchNo---" + batchNo);


                        Row row = sheet.createRow(rowIdx++);

                        prodUnit = null;
                        row.createCell(0).setCellValue(batchNo.get("ledger_name").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("principle_name").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("subprinciple_name").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("balancing_method").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("dr").getAsDouble());
                        row.createCell(5).setCellValue(batchNo.get("cr").getAsDouble());


                        sumOfDr += batchNo.get("dr").getAsDouble();
                        sumOfCr += batchNo.get("cr").getAsDouble();

                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    Cell cell1 = prow.createCell(5);
                    cell.setCellValue(sumOfDr);
                    cell1.setCellValue(sumOfCr);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public InputStream exportLedgerReport2(Map<String, String> jsonRequest, HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            String JsonToStr = jsonRequest.get("list");

            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"MONTH", "DEBIT AMOUNT", "CREDIT AMOUNT", "CLOSING BALANCE", "TYPE"};

                    Sheet sheet = workbook.createSheet("ledger_report");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    double sumOfDr = 0;
                    double sumOfCr = 0;
                    int rowIdx = 1;
                    for (int i = 0; i < productBatchNos.size(); i++) {

                        JsonObject batchNo = productBatchNos.get(i).getAsJsonObject();
                        JsonObject prodUnit = null;

                        Row row = sheet.createRow(rowIdx++);

                        prodUnit = null;
                        row.createCell(0).setCellValue(batchNo.get("month_name").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(2).setCellValue(batchNo.get("credit").getAsDouble());
                        row.createCell(3).setCellValue(batchNo.get("closing_balance").getAsDouble());
                        if (batchNo.keySet().contains("type"))    //It checks wheather the "type" is present or not in list
                            row.createCell(4).setCellValue(batchNo.get("type").getAsString());
                        else row.createCell(4).setCellValue("");

                        sumOfDr += batchNo.get("debit").getAsDouble();
                        sumOfCr += batchNo.get("credit").getAsDouble();

                    }

                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(1);
                    Cell cell1 = prow.createCell(2);
                    cell.setCellValue(sumOfDr);
                    cell1.setCellValue(sumOfCr);

                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
                    } else {
                        System.out.println("Empty");
                    }

                }
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            productLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    public InputStream exportToExcelLedgerReport3(Map<String, String> jsonRequest, HttpServletRequest request) throws IOException {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
//            Boolean mfgShow = Boolean.valueOf(request.getParameter("mfgShow"));
            String JsonToStr = jsonRequest.get("list");
            JsonArray productBatchNos = new JsonParser().parse(JsonToStr).getAsJsonArray();
            System.out.println("productBatchNos " + productBatchNos);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (productBatchNos.size() > 0) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    String[] headers = {"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT", "CLOSING BALANCE","TYPE"};

//                    if (mfgShow)
//                        headers = new String[]{"DATE", "LEDGER NAME", "VOUCHER TYPE", "VOUCHER NO.", "DEBIT", "CREDIT"};
                    Sheet sheet = workbook.createSheet("ledgerReport");

                    // Header
                    Row headerRow = sheet.createRow(0);
                    // Define header cell style
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = headerRow.createCell(col);
                        cell.setCellValue(headers[col]);
                        cell.setCellStyle(headerCellStyle);
                    }

                    long sumOfQty = 0;
                    long sumOfQty1 = 0;

                    int rowIdx = 1;
                    JsonObject batchNo = null;
                    for (int i = 0; i < productBatchNos.size(); i++) {
                        batchNo = productBatchNos.get(i).getAsJsonObject();

                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(batchNo.get("transaction_date").getAsString());
                        row.createCell(1).setCellValue(batchNo.get("particulars").getAsString());
                        row.createCell(2).setCellValue(batchNo.get("voucher_type").getAsString());
                        row.createCell(3).setCellValue(batchNo.get("invoice_no").getAsString());
                        row.createCell(4).setCellValue(batchNo.get("debit").getAsDouble());
                        row.createCell(5).setCellValue(batchNo.get("credit").getAsDouble());
                        row.createCell(6).setCellValue(batchNo.get("Closing_balance").getAsDouble());
                        row.createCell(7).setCellValue(batchNo.get("transc_type").getAsString());



//
                        sumOfQty += batchNo.get("debit").getAsDouble();
                        sumOfQty1 += batchNo.get("credit").getAsDouble();



                    }
                    Row prow = sheet.createRow(rowIdx++);
                    prow.createCell(0).setCellValue("Total");
                    Cell cell = prow.createCell(4);
                    cell.setCellValue(sumOfQty);
                    Cell cell1 = prow.createCell(5);
                    cell1.setCellValue(sumOfQty1);


                    workbook.write(out);
                    byte[] b = new ByteArrayInputStream(out.toByteArray()).readAllBytes();
                    if (b.length > 0) {
                        String s = new String(b);
//                        System.out.println("data ------> " + s);
                    } else {
                        System.out.println("Empty");
                    }


                }
            }
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            stockLogger.error("Failed to load near expiry of products data in excel " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


}
