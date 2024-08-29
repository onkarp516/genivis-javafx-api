package in.truethics.ethics.ethicsapiv10.service.tranx_service.sales;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.salesdto.SalesOrderDTO;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerGstDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.receipt_repository.TranxReceiptPerticularsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.*;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.DateConvertUtil;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import in.truethics.ethics.ethicsapiv10.util.TranxCodeUtility;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TranxSalesOrderService {

    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private TranxSalesOrderRepository tranxSalesOrderRepository;
    private TranxSalesOrder mSalesOrderTransaction;
    @Autowired
    private TranxSalesOrderDutiesTaxesRepository salesOrderDutiesTaxesRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TranxSalesOrderDetailsRepository salesOrderInvoiceDetailsRepository;
    @Autowired
    private TranxSalesOrderDetailsUnitsRepository tranxSalesOrderDetailsUnitsRepository;
    @Autowired
    private TranxSalesQuotationRepository salesQuotationRepository;
    @Autowired
    private TranxSalesQuotationDetailsRepository tranxSalesQuotationDetailsRepository;
    @Autowired
    private NumFormat numFormat;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private PackingMasterRepository packingMasterRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private PrincipleRepository principleRepository;
    @Autowired
    private PrincipleGroupsRepository principleGroupsRepository;
    @Autowired
    private TranxReceiptMasterRepository tranxReceiptMasterRepository;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private TranxReceiptPerticularsRepository tranxReceiptPerticularRepository;
    @Autowired
    private BalancingMethodRepository balancingMethodRepository;
    @Autowired
    private TranxSalesQuotaionDetailsUnitsRepository tranxSalesQuotaionDetailsUnitsRepository;
    @Autowired
    private ProductData productData;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice from DB
    List<Long> mInputList = new ArrayList<>(); // input all ledgers Id against Purchase invoice from request
    private static final Logger salesOrderLogger = LogManager.getLogger(TranxSalesOrderService.class);
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;
    @Autowired
    private TranxReceiptPerticularsDetailsRepository tranxReceiptPerticularsDetailsRepository;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    private LedgerGstDetailsRepository ledgerGstDetailsRepository;

    public JsonObject createSalesOrder(HttpServletRequest request) {
        JsonObject object = new JsonObject();

        ResponseMessage responseMessage = ResponseMessage.getInstance();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        TranxSalesOrder salesOrderTransaction = saveSalesOrderRequest(request, users);

        if (salesOrderTransaction != null) {
            object.addProperty("message", "Sales Order created successfully");
            object.addProperty("responseStatus", HttpStatus.OK.value());
            object.addProperty("id", salesOrderTransaction.getId().toString());
            /**
             * @implNote validation of Ledger Delete , if any tranx done for this ledger, user cant delete this ledger **
             * @auther ashwins@opethic.com
             * @version sprint 21
             **/
            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(salesOrderTransaction.getSundryDebtors().getId(), true);
            ledgerMaster.setIsDeleted(false);
            ledgerMasterRepository.save(ledgerMaster);

        } else {
            object.addProperty("message", "Error in Sales Order creation");
            object.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return object;
    }

    private TranxSalesOrder saveSalesOrderRequest(HttpServletRequest request, Users users) {

        TranxSalesOrder salesOrderTransaction = new TranxSalesOrder();
        Map<String, String[]> paramMap = request.getParameterMap();
        LedgerMaster sundryDebtors = null;
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            salesOrderTransaction.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        salesOrderTransaction.setOutlet(outlet);
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        salesOrderTransaction.setTransactionStatus(transactionStatus);
        salesOrderTransaction.setSo_bill_no(request.getParameter("bill_no"));
        salesOrderTransaction.setSalesOrderSrNo(Long.parseLong(request.getParameter("sales_sr_no")));
        if (paramMap.containsKey("reference_so_id")) {
            salesOrderTransaction.setSq_ref_id(request.getParameter("reference_so_id"));
        }
        if (paramMap.containsKey("order_reference_no")) {
            salesOrderTransaction.setReference(request.getParameter("order_reference_no"));
        }
        if (paramMap.containsKey("paymentMode")) {
            salesOrderTransaction.setPaymentMode(request.getParameter("paymentMode"));
            salesOrderTransaction.setAdvancedAmount(Double.parseDouble(request.getParameter("amount")));
        } else {
            salesOrderTransaction.setPaymentMode("");
        }
        String custName = "";
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        salesOrderTransaction.setBillDate(DateConvertUtil.convertStringToDate(request.getParameter("bill_dt")));
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            salesOrderTransaction.setFiscalYear(fiscalYear);
            salesOrderTransaction.setFinancialYear(fiscalYear.getFiscalYear());
        }
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                salesOrderTransaction.setGstNumber(request.getParameter("gstNo"));
            }
        }
        LedgerMaster salesAccounts = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), true);
        sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), true);
        salesOrderTransaction.setSundryDebtors(sundryDebtors);


        /*** this scenario is for Upahar Trading only , order cakes and payment made from client as an advance ****/
/*        else {
            if (paramMap.containsKey("clientName")) {
                custName = request.getParameter("clientName");
                salesOrderTransaction.setCustomerName(custName);
            } else
                salesOrderTransaction.setCustomerName(custName);
            if (paramMap.containsKey("mobileNo"))
                salesOrderTransaction.setMobileNo(Long.parseLong(request.getParameter("mobileNo")));
            else
                salesOrderTransaction.setMobileNo(0L);
            sundryDebtors = createLedger(custName, Long.parseLong(request.getParameter("mobileNo")), outlet, branch);
        }*/
        salesOrderTransaction.setSalesAccountLedger(salesAccounts);
        salesOrderTransaction.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        // LedgerMaster roundoff = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId("Round off", users.getOutlet().getId());
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        salesOrderTransaction.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        salesOrderTransaction.setSalesRoundOff(roundoff);
        salesOrderTransaction.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
        if (taxFlag) {
            salesOrderTransaction.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            salesOrderTransaction.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            salesOrderTransaction.setTotaligst(0.0);
        }
        /* if false : igst i.e inter state */
        else {
            salesOrderTransaction.setTotalcgst(0.0);
            salesOrderTransaction.setTotalsgst(0.0);
            salesOrderTransaction.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }
        salesOrderTransaction.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        salesOrderTransaction.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        salesOrderTransaction.setTcs(Double.parseDouble(request.getParameter("tcs")));
        salesOrderTransaction.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        salesOrderTransaction.setTotalTax(Double.parseDouble(request.getParameter("total_tax_amt")));

        salesOrderTransaction.setNarration(request.getParameter("narration"));
        salesOrderTransaction.setCreatedBy(users.getId());
        salesOrderTransaction.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        salesOrderTransaction.setStatus(true);
        salesOrderTransaction.setOperations("inserted");
        salesOrderTransaction.setOrderStatus("pickUp");
        salesOrderTransaction.setCreatedBy(users.getId());
        // closing all references of purchase order ids
        /*salesOrderTransaction.setNarration(request.getParameter("narration"));
        salesOrderTransaction.setIsCounterSale(false);*/
        if (request.getParameterMap().containsKey("transactionTrackingNo"))
            salesOrderTransaction.setTransactionTrackingNo(request.getParameter("transactionTrackingNo"));
        else
            salesOrderTransaction.setTransactionTrackingNo(String.valueOf(new Date().getTime()));
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSORD");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        salesOrderTransaction.setTranxCode(tranxCode);
        try {
            mSalesOrderTransaction = tranxSalesOrderRepository.save(salesOrderTransaction);
            /* Save into Sales Duties and Taxes */
            if (mSalesOrderTransaction != null) {

                /***** for unregister or composition company, for registered company uncomment below saveIntoSalesDutiesTaxesEdit ****/
                String taxStr = request.getParameter("taxCalculation");
                System.out.println("Tax str--->" + taxStr);
                if (!taxStr.isEmpty()) {
                    JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                    saveIntoDutiesAndTaxes(duties_taxes, mSalesOrderTransaction, taxFlag, users.getOutlet().getId());
                }
                JsonParser parser = new JsonParser();
                String jsonStr = request.getParameter("row");
                JsonElement quotationDetailsJson = parser.parse(jsonStr);
                JsonArray invoiceDetails = quotationDetailsJson.getAsJsonArray();
                String referenceObj = request.getParameter("refObject");
                saveIntoTranxSalesOrderDetails(invoiceDetails, mSalesOrderTransaction, branch, outlet, users.getId(), tranxType, referenceObj, "create", "");
            }

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesOrderLogger.error("Error in saveSalesOrderRequest :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesOrderLogger.error("Error in saveSalesOrderRequest :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mSalesOrderTransaction;
    }

    private void createReceiptInvoice(TranxSalesOrder newInvoice, Users users, String key) {

        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("RCPT");
        TranxReceiptMaster tranxReceiptMaster = null;
        if (key.equalsIgnoreCase("create")) {
            tranxReceiptMaster = new TranxReceiptMaster();
        } else {
            tranxReceiptMaster = tranxReceiptMasterRepository.findByTranxSalesOrderIdAndStatus(newInvoice.getId(), true);
        }
        if (newInvoice.getBranch() != null)
            tranxReceiptMaster.setBranch(newInvoice.getBranch());
        tranxReceiptMaster.setOutlet(newInvoice.getOutlet());
        if (newInvoice.getFiscalYear() != null)
            tranxReceiptMaster.setFiscalYear(newInvoice.getFiscalYear());
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxReceiptMasterRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxReceiptMasterRepository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String receiptCode = "RCPT" + currentMonth + serailNo;
        tranxReceiptMaster.setReceiptNo(receiptCode);
        tranxReceiptMaster.setReceiptSrNo(newInvoice.getId());
        tranxReceiptMaster.setTranscationDate(newInvoice.getBillDate());
        tranxReceiptMaster.setTotalAmt(newInvoice.getTotalAmount());
        tranxReceiptMaster.setStatus(true);
        tranxReceiptMaster.setCreatedBy(newInvoice.getCreatedBy());
        TranxReceiptMaster newTranxReceiptMaster = tranxReceiptMasterRepository.save(tranxReceiptMaster);
        LedgerMaster sundryDebtors = newInvoice.getSundryDebtors();
        insertIntoReceiptPerticualrs(newTranxReceiptMaster, sundryDebtors, "SD", newInvoice.getAdvancedAmount(), key); //receipt details of Sundry Debtors
        //  TransactionTypeMaster tranxType = tranxRepository.findByTransactionNameIgnoreCase("receipt");
        /* Postings into Sundry Debtors */
        if (key.equalsIgnoreCase("create")) {
            /*transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(
                    sundryDebtors.getFoundations().getId(), sundryDebtors.getPrinciples().getId(), sundryDebtors.getPrincipleGroups().getId(), null,
                    tranxType.getId(), sundryDebtors.getBalancingMethod() != null ?
                            sundryDebtors.getBalancingMethod().getId() : null,
                    newInvoice.getBranch() != null ? newInvoice.getBranch().getId() : null,
                    newInvoice.getOutlet() != null ? newInvoice.getOutlet().getId() : null,
                    "pending", 0.0, newInvoice.getAdvancedAmount(),
                    newInvoice.getBillDate(), null, newInvoice.getId(),
                    tranxType.getTransactionName(), sundryDebtors.getUnderPrefix(), newTranxReceiptMaster.getFiscalYear() != null ? newTranxReceiptMaster.getFiscalYear().getFiscalYear() : null,
                    newInvoice.getCreatedBy(), sundryDebtors.getId(), newInvoice.getSo_bill_no());*/
            ledgerCommonPostings.callToPostings(newInvoice.getAdvancedAmount(), sundryDebtors, tranxType,
                    sundryDebtors.getAssociateGroups(),
                    newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getBranch(),
                    newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getTranscationDate(),
                    newTranxReceiptMaster.getId(),
                    newTranxReceiptMaster.getReceiptNo(),
                    "CR", true, "Receipt", "Insert");
        } else {
           /* transactionDetailsRepository.ledgerPostingEdit(sundryDebtors.getId(), newTranxReceiptMaster.getId(),
                    tranxType.getId(), tranxType.getTransactionName(), newInvoice.getAdvancedAmount());*/
            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                    findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(sundryDebtors.getId(),
                            tranxType.getId(), newTranxReceiptMaster.getId());
            if (mLedger != null) {
                mLedger.setAmount(newInvoice.getAdvancedAmount());
                mLedger.setTransactionDate(newTranxReceiptMaster.getTranscationDate());
                mLedger.setOperations("updated");
                ledgerTransactionPostingsRepository.save(mLedger);
            }
        }
        if (newInvoice.getPaymentMode().equalsIgnoreCase("cash")) {
            LedgerMaster cashInHand = null;
            if (users.getBranch() != null)
                cashInHand = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndBranchIdAndStatus("CAIH", newInvoice.getOutlet().getId(), newInvoice.getBranch().getId(), true);
            else {
                cashInHand = ledgerMasterRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIsNull("CAIH", newInvoice.getOutlet().getId(), true);
            }
            Double amount = newInvoice.getAdvancedAmount();
            insertIntoReceiptPerticualrs(newTranxReceiptMaster, cashInHand, "cash", amount, key); //receipt details of Cash In Hand
            /* Postings into Cash-in Hand */
            if (key.equalsIgnoreCase("create")) {
               /* transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(
                        cashInHand.getFoundations().getId(), cashInHand.getPrinciples() != null ? cashInHand.getPrinciples().getId() : null, cashInHand.getPrincipleGroups() != null ? cashInHand.getPrincipleGroups().getId() : null, null,
                        tranxType.getId(), cashInHand.getBalancingMethod() != null ?
                                cashInHand.getBalancingMethod().getId() : null,
                        newInvoice.getBranch() != null ? newInvoice.getBranch().getId() : null, newInvoice.getOutlet() != null ? newInvoice.getOutlet().getId() : null,
                        "pending", amount * -1, 0.0,
                        newInvoice.getBillDate(), null, newInvoice.getId(),
                        tranxType.getTransactionName(), cashInHand.getUnderPrefix(), newTranxReceiptMaster.getFiscalYear() != null ? newTranxReceiptMaster.getFiscalYear().getFiscalYear() : null,
                        newInvoice.getCreatedBy(), cashInHand.getId(), newInvoice.getSo_bill_no());*/

                ledgerCommonPostings.callToPostings(amount, cashInHand, tranxType,
                        cashInHand.getAssociateGroups(),
                        newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getBranch(),
                        newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getTranscationDate(),
                        newTranxReceiptMaster.getId(),
                        newTranxReceiptMaster.getReceiptNo(),
                        "DR", true, "Receipt", "Insert");
            } else {
               /* transactionDetailsRepository.ledgerPostingEdit(cashInHand.getId(), newTranxReceiptMaster.getId(),
                        tranxType.getId(), tranxType.getTransactionName(), amount * -1);*/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                        findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(cashInHand.getId(),
                                tranxType.getId(), newTranxReceiptMaster.getId());
                if (mLedger != null) {
                    mLedger.setAmount(amount);
                    mLedger.setTransactionDate(newTranxReceiptMaster.getTranscationDate());
                    mLedger.setOperations("updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }
            }
        }
        if (newInvoice.getPaymentMode().equalsIgnoreCase("online")) {
            List<LedgerMaster> bankAccounts = new ArrayList<>();
            if (users.getBranch() != null)
                bankAccounts = ledgerMasterRepository.findByUniqueCodeAndBranchIdAndOutletIdAndStatus("BAAC",
                        newInvoice.getBranch().getId(), newInvoice.getOutlet().getId(), true);
            else {
                bankAccounts = ledgerMasterRepository.findByUniqueCodeAndBranchIsNullAndOutletIdAndStatus("BAAC",
                        newInvoice.getOutlet().getId(), true);
            }
            Double amount = newInvoice.getAdvancedAmount();
            insertIntoReceiptPerticualrs(newTranxReceiptMaster, bankAccounts.get(0), "bank", amount, key); //receipt details of Bank Accounts
            /* Postings into Bank account */
            if (key.equalsIgnoreCase("create")) {
                /*transactionDetailsRepository.insertIntoLegerTranxDetailsPosting(
                        bankAccounts.getFoundations().getId(), bankAccounts.getPrinciples() != null ?
                                bankAccounts.getPrinciples().getId() : null, bankAccounts.getPrincipleGroups() != null ?
                                bankAccounts.getPrincipleGroups().getId() : null, null,
                        tranxType.getId(), bankAccounts.getBalancingMethod() != null ?
                                bankAccounts.getBalancingMethod().getId() : null,
                        newInvoice.getBranch() != null ? newInvoice.getBranch().getId() : null,
                        newInvoice.getOutlet() != null ? newInvoice.getOutlet().getId() : null,
                        "pending", amount * -1, 0.0,
                        newInvoice.getBillDate(), null, newInvoice.getId(),
                        tranxType.getTransactionName(), bankAccounts.getUnderPrefix(),
                        newTranxReceiptMaster.getFiscalYear() != null ?
                                newTranxReceiptMaster.getFiscalYear().getFiscalYear() : null,
                        newInvoice.getCreatedBy(), bankAccounts.getId(), newTranxReceiptMaster.getReceiptNo());*/
                ledgerCommonPostings.callToPostings(amount, bankAccounts.get(0), tranxType,
                        bankAccounts.get(0).getAssociateGroups(),
                        newTranxReceiptMaster.getFiscalYear(), newTranxReceiptMaster.getBranch(),
                        newTranxReceiptMaster.getOutlet(), newTranxReceiptMaster.getTranscationDate(),
                        newTranxReceiptMaster.getId(),
                        newTranxReceiptMaster.getReceiptNo(),
                        "DR", true, "Receipt", "Insert");
            } else {
               /* transactionDetailsRepository.ledgerPostingEdit(bankAccounts.getId(), newTranxReceiptMaster.getId(),
                        tranxType.getId(), tranxType.getTransactionName(), amount * -1);*/
                LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                        findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(bankAccounts.get(0).getId(),
                                tranxType.getId(), newTranxReceiptMaster.getId());
                if (mLedger != null) {
                    mLedger.setAmount(amount);
                    mLedger.setTransactionDate(newTranxReceiptMaster.getTranscationDate());
                    mLedger.setOperations("updated");
                    ledgerTransactionPostingsRepository.save(mLedger);
                }

            }
        }
    }

    private void insertIntoReceiptPerticualrs(TranxReceiptMaster newTranxReceiptMaster,
                                              LedgerMaster ledgerMaster, String key, Double amount, String operation) {
        TranxReceiptPerticulars tranxReceiptPerticulars = null;
        if (operation.equalsIgnoreCase("create")) {
            tranxReceiptPerticulars = new TranxReceiptPerticulars();
        } else {
            tranxReceiptPerticulars = tranxReceiptPerticularRepository.findByStatusAndTranxReceiptMasterId(true, newTranxReceiptMaster.getId());
        }
        tranxReceiptPerticulars.setBranch(newTranxReceiptMaster.getBranch());
        tranxReceiptPerticulars.setOutlet(newTranxReceiptMaster.getOutlet());
        tranxReceiptPerticulars.setLedgerMaster(ledgerMaster);
        tranxReceiptPerticulars.setTranxReceiptMaster(newTranxReceiptMaster);
        tranxReceiptPerticulars.setLedgerType(key);
        tranxReceiptPerticulars.setLedgerName(ledgerMaster.getLedgerName());
        if (key.equalsIgnoreCase("SD")) {
            tranxReceiptPerticulars.setDr(0.0);
            tranxReceiptPerticulars.setCr(amount);
        } else {
            tranxReceiptPerticulars.setDr(amount);
            tranxReceiptPerticulars.setCr(0.0);
        }
        tranxReceiptPerticulars.setTransactionDate(DateConvertUtil.convertDateToLocalDate(
                newTranxReceiptMaster.getTranscationDate()));
        tranxReceiptPerticulars.setStatus(true);
        tranxReceiptPerticulars.setCreatedBy(newTranxReceiptMaster.getCreatedBy());
        tranxReceiptPerticularRepository.save(tranxReceiptPerticulars);
    }

    private void InsertIntoBillDetails(TranxReceiptPerticulars receiptPerticulars, String key,
                                       TranxSalesInvoice newInvoice) {
        TranxReceiptPerticularsDetails tranxRptDetails = new TranxReceiptPerticularsDetails();
        if (key.equalsIgnoreCase("update")) {
            tranxRptDetails = tranxReceiptPerticularsDetailsRepository.findByStatusAndTranxReceiptPerticularsId(
                    true, receiptPerticulars.getId());
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

    private LedgerMaster createLedger(String clientName, Long mobileNo, Outlet outlet, Branch branch) {
        LedgerMaster mLedger = null;
        mLedger = ledgerMasterRepository.findByMobileAndStatus(mobileNo, true);
        if (mLedger == null) {
            mLedger = new LedgerMaster();
            mLedger.setLedgerName(clientName);
            mLedger.setMobile(mobileNo);
            mLedger.setUniqueCode("SUDR");
            mLedger.setOutlet(outlet);
            mLedger.setBranch(branch);
            mLedger.setMailingName(clientName);
            mLedger.setOpeningBalType("Dr");
            mLedger.setOpeningBal(0.0);
            mLedger.setAddress("NA");
            mLedger.setPincode(0L);
            mLedger.setEmail("NA");
            mLedger.setTaxable(false);
            mLedger.setPancard("NA");
            mLedger.setStatus(true);
            mLedger.setStateCode("27");
            mLedger.setBankName("NA");
            mLedger.setBankBranch("NA");
            mLedger.setAccountNumber("NA");
            mLedger.setIfsc("NA");
            mLedger.setBankBranch("NA");
            mLedger.setTaxType("NA");
            mLedger.setSlugName("sundry_debtors");
            mLedger.setUnderPrefix("PG#1");
            mLedger.setIsDeleted(true);
            mLedger.setIsDefaultLedger(false);
            mLedger.setIsPrivate(false);
            mLedger.setCreditDays(0);
            mLedger.setApplicableFrom("NA");
            mLedger.setFoodLicenseNo("NA");
            mLedger.setTds(false);
            mLedger.setTcs(false);
            BalancingMethod balancingMethod = balancingMethodRepository.findByIdAndStatus(2L, true);
            mLedger.setBalancingMethod(balancingMethod);
            Principles mPrinciple = principleRepository.findByIdAndStatus(3L, true);
            mLedger.setPrinciples(mPrinciple);
            PrincipleGroups mGroups = principleGroupsRepository.findByIdAndStatus(1L, true);
            mLedger.setPrincipleGroups(mGroups);
            mLedger.setFoundations(mPrinciple.getFoundations());
            LedgerMaster newLedger = ledgerMasterRepository.save(mLedger);
            return newLedger;
        } else {
            return mLedger;
        }
    }

    /* Close All Sales Quotations which are converted into Orders */
    public void setCloseSQ(String sqIds) {
        Boolean flag = false;
        String idList[];
        idList = sqIds.split(",");
        for (String mId : idList) {
            TranxSalesQuotation tranxSalesQuotation = salesQuotationRepository.findByIdAndStatus(Long.parseLong(mId), true);
            if (tranxSalesQuotation != null) {
                tranxSalesQuotation.setStatus(false);
                salesQuotationRepository.save(tranxSalesQuotation);
            }
        }
    }

    private void saveIntoTranxSalesOrderDetails(JsonArray array, TranxSalesOrder mSalesTranx, Branch branch,
                                                Outlet outlet, Long id, TransactionTypeMaster tranxType,
                                                String referenceObj, String type, String rowsDeleted) {
        String refType = "";
        Long referenceId = 0L;
        boolean flag_status = false;
        for (JsonElement mList : array) {
            JsonObject object = mList.getAsJsonObject();
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            if (object.has("reference_type")) refType = object.get("reference_type").getAsString();
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
            String batchNo = null;

            ProductBatchNo productBatchNo = null;
            TranxSalesOrderDetailsUnits orderUnits = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
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
            if (type.equalsIgnoreCase("create")) {
                orderUnits = new TranxSalesOrderDetailsUnits();
                orderUnits.setStatus(true);
                orderUnits.setTransactionStatus(1L);
            } else {
                Long detailsId = object.get("details_id").getAsLong();
                if (detailsId != 0) {
                    orderUnits = tranxSalesOrderDetailsUnitsRepository.findByIdAndStatus(detailsId, true);

                } else {
                    orderUnits = new TranxSalesOrderDetailsUnits();
                    orderUnits.setStatus(true);
                    orderUnits.setTransactionStatus(1L);
                }
            }
            orderUnits.setSalesOrder(mSalesTranx);
            orderUnits.setProduct(mProduct);
            orderUnits.setUnits(units);
            orderUnits.setQty(object.get("qty").getAsDouble());
            if (object.has("free_qty") && !object.get("free_qty").getAsString().equalsIgnoreCase(""))
                orderUnits.setFreeQty(object.get("free_qty").getAsDouble());
            else
                orderUnits.setFreeQty(0.0);
            orderUnits.setRate(object.get("rate").getAsDouble());

            if (levelA != null) orderUnits.setLevelA(levelA);
            if (levelB != null) orderUnits.setLevelB(levelB);
            if (levelC != null) orderUnits.setLevelC(levelC);

            orderUnits.setStatus(true);
            if (object.has("base_amt"))
                orderUnits.setBaseAmt(object.get("base_amt").getAsDouble());
            if (object.has("unit_conv"))
                orderUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
            orderUnits.setDiscountAmount(object.get("dis_amt").getAsDouble());
            orderUnits.setDiscountPer(object.get("dis_per").getAsDouble());
            if (object.has("dis_per2"))
                orderUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            else
                orderUnits.setDiscountBInPer(0.0);
            orderUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
            orderUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
            orderUnits.setAdditionChargesAmt(object.get("add_chg_amt").getAsDouble());
            orderUnits.setGrossAmt1(object.get("gross_amt1").getAsDouble());
            orderUnits.setInvoiceDisAmt(object.get("invoice_dis_amt").getAsDouble());
            orderUnits.setDiscountPerCal(object.get("dis_per_cal").getAsDouble());
            orderUnits.setDiscountAmountCal(object.get("dis_amt_cal").getAsDouble());
            orderUnits.setTotalAmount(object.get("total_amt").getAsDouble());
            orderUnits.setIgst(object.get("igst").getAsDouble());
            orderUnits.setSgst(object.get("sgst").getAsDouble());
            orderUnits.setCgst(object.get("cgst").getAsDouble());
            orderUnits.setTotalIgst(object.get("total_igst").getAsDouble());
            orderUnits.setTotalSgst(object.get("total_sgst").getAsDouble());
            orderUnits.setTotalCgst(object.get("total_cgst").getAsDouble());
            orderUnits.setFinalAmount(object.get("final_amt").getAsDouble());
            tranxSalesOrderDetailsUnitsRepository.save(orderUnits);
            /**
             * @implNote validation of Product Delete , if any tranx done for this product, user cant delete this product **
             * @auther ashwins@opethic.com
             * @version sprint 21
             **/
            if (mProduct != null && mProduct.getIsDelete()) {
                mProduct.setIsDelete(false);
                productRepository.save(mProduct);
            }

            /* closing of sales quotation while converting into sales challan using its qnt */
            double qty = object.get("qty").getAsDouble();
            if (object.has("reference_id") && !object.get("reference_id").getAsString().equalsIgnoreCase("")) {
                referenceId = object.get("reference_id").getAsLong();
            }
            if (refType.equalsIgnoreCase("SLSQTN")) {
                TranxSalesQuotationDetailsUnits quotationDetails =
                        tranxSalesQuotaionDetailsUnitsRepository.findByProductDetailsLevel(referenceId,
                                mProduct.getId(), units.getId(), levelAId, levelBId, levelCId, true);
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
            } /* End of closing of sales quotation while converting into sales challan
            using its qnt */
        }
        JsonParser parser = new JsonParser();
        JsonElement salesDetailsJson;
        if (!rowsDeleted.equalsIgnoreCase("")) {
            salesDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = salesDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxSalesOrderDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    mDeletedInvoices = tranxSalesOrderDetailsUnitsRepository.findByIdAndStatus(
                            deletedRowsId.get("del_id").getAsLong(), true);
                    if (mDeletedInvoices != null) {
                        mDeletedInvoices.setStatus(false);
                        try {
                            tranxSalesOrderDetailsUnitsRepository.save(mDeletedInvoices);
                            /***** inventory effects of deleted rows *****/

                            /***** End of new architecture of Inventory Postings *****/
                        } catch (DataIntegrityViolationException de) {
                            salesOrderLogger.error("Error in saveInto Sales Invoice Details Edit" + de.getMessage());
                            de.printStackTrace();
                            System.out.println("Exception:" + de.getMessage());
                        } catch (Exception ex) {
                            salesOrderLogger.error("Error in saveInto Sales Invoice Details Edit" + ex.getMessage());
                            ex.printStackTrace();
                            System.out.println("Exception save Into Sales Invoice Details Edit:" + ex.getMessage());
                        }
                    }
                }
            }
        }
        if (type.equalsIgnoreCase("create")) {
            if (refType.equalsIgnoreCase("SLSQTN")) {
                TranxSalesQuotation tranxInvoice = salesQuotationRepository.findByIdAndStatus(referenceId, true);
                if (tranxInvoice != null) {
                    if (flag_status) {
                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
                        tranxInvoice.setTransactionStatus(transactionStatus);
                        salesQuotationRepository.save(tranxInvoice);
                    } else {
                        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("closed", true);
                        tranxInvoice.setTransactionStatus(transactionStatus);
                        salesQuotationRepository.save(tranxInvoice);
                    }
                }
            }
        }
    }


    private void saveIntoDutiesAndTaxes(JsonObject duties_taxes, TranxSalesOrder mSalesOrderTransaction, Boolean taxFlag, Long outletId) throws Exception {
        List<TranxSalesOrderDutiesTaxes> salesOrderDutiesTaxes = new ArrayList<>();
        try {
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.get("cgst").getAsJsonArray();
                JsonArray sgstList = duties_taxes.get("sgst").getAsJsonArray();
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (int i = 0; i < cgstList.size(); i++) {
                        TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                        JsonObject cgstObject = cgstList.get(i).getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT CGST " + inputGst;
                        //      dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (mSalesOrderTransaction.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesOrderTransaction.getOutlet().getId(), mSalesOrderTransaction.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesOrderTransaction.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(Double.parseDouble(cgstObject.get("amt").getAsString()));
                        taxes.setSalesTransaction(mSalesOrderTransaction);
                        taxes.setSundryDebtors(mSalesOrderTransaction.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesOrderDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (int i = 0; i < sgstList.size(); i++) {
                        TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                        JsonObject sgstObject = sgstList.get(i).getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT SGST " + inputGst;
                        //  dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (mSalesOrderTransaction.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesOrderTransaction.getOutlet().getId(), mSalesOrderTransaction.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesOrderTransaction.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(Double.parseDouble(sgstObject.get("amt").getAsString()));
                        taxes.setSalesTransaction(mSalesOrderTransaction);
                        taxes.setSundryDebtors(mSalesOrderTransaction.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesOrderDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.get("igst").getAsJsonArray();
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (int i = 0; i < igstList.size(); i++) {
                        TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                        JsonObject igstObject = igstList.get(i).getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //   int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT IGST " + inputGst;
                        //  dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (mSalesOrderTransaction.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesOrderTransaction.getOutlet().getId(), mSalesOrderTransaction.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesOrderTransaction.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(Double.parseDouble(igstObject.get("amt").getAsString()));
                        taxes.setSalesTransaction(mSalesOrderTransaction);
                        taxes.setSundryDebtors(mSalesOrderTransaction.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesOrderDutiesTaxes.add(taxes);
                    }
                }
            }

            /* save all Duties and Taxes into Sales Invoice Duties taxes table */
            salesOrderDutiesTaxesRepository.saveAll(salesOrderDutiesTaxes);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesOrderLogger.error("Exception in saveIntoDutiesAndTaxes:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            salesOrderLogger.error("Exception in saveIntoDutiesAndTaxes:" + e1.getMessage());
            System.out.println(e1.getMessage());
        }
    }

   /* public String showCustomer(CustomerModel customerModel) {
        Gson gson = new Gson();
        String response = gson.toJson(customerModel);
//        String response = customerModel.getCustomerName() + "\n" + customerModel.getAddress() + "\n" + customerModel.getCity() + "\n" + customerModel.getCustomerCode();
        System.out.println(response);
        return response;
    }*/

    public JsonObject salesOrderLastRecord(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxSalesOrderRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxSalesOrderRepository.findLastRecord(users.getOutlet().getId());
        }

        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
     /*   String companyName = users.getOutlet().getCompanyName();
        companyName = companyName.substring(0, 3);*/ // fetching first 3 digits from company names
        /* getting Start and End year from fiscal Year */
       /* String startYear = generateFiscalYear.getStartYear();
        String endYear = generateFiscalYear.getEndYear();*/
        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
   /*     String csCode = companyName.toUpperCase() + "-" + startYear + endYear
                + "-" + "SO" + currentMonth + "-" + serailNo;*/
        String csCode = "SO" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("serialNo", csCode);

        return result;
    }

    public JsonArray getAllSalesOrders(HttpServletRequest request) {
        List<TranxSalesOrder> quotations = tranxSalesOrderRepository.findAll();

        Branch branch = null;
        Outlet outlet = null;
        LedgerMaster sundryDebtors = null;
        LedgerMaster salesAccountLedger = null;
        LedgerMaster salesRoundOff = null;
        List<TranxSalesQuotationDetails> salesQuotationInvoiceDetails = null;
        List<TranxSalesQuotationDutiesTaxes> salesQuotationDutiesTaxes = null;

        JsonArray jsonArray = new JsonArray();
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
                jsonObject.addProperty("debtors_name", sundryDebtors.getLedgerName());
                jsonObject.addProperty("roundoff", salesOrderTransaction.getRoundOff());
                jsonObject.addProperty("narration", "NA");
                jsonObject.addProperty("total_base_amt", salesOrderTransaction.getTotalBaseAmount());
                jsonObject.addProperty("totalamt", salesOrderTransaction.getTotalAmount());
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
                jsonArray.add(jsonObject);
            }
        }

        return jsonArray;
    }

    /* list all Sales Orders of Outlets */
    public JsonObject AllsaleOrderList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesOrder> tranxSalesOrders = new ArrayList<>();
//        String order_type = request.getParameter("order_type");
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
                tranxSalesOrders = tranxSalesOrderRepository.findSaleOrderListWithDateWithBr(users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, true);
            } else {
                tranxSalesOrders = tranxSalesOrderRepository.findSaleOrderListWithDate(users.getOutlet().getId(), startDatep, endDatep, true);
            }
        } else {
            if (users.getBranch() != null) {
                tranxSalesOrders = tranxSalesOrderRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                tranxSalesOrders = tranxSalesOrderRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
            }
        }

        for (TranxSalesOrder invoices : tranxSalesOrders) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId() != null ? invoices.getId() : 0);
            response.addProperty("bill_no", invoices.getSo_bill_no() != null ? invoices.getSo_bill_no() : "");
            response.addProperty("bill_date", invoices.getBillDate() != null ? DateConvertUtil.convertDateToLocalDate(invoices.getBillDate()).toString() : "");
            response.addProperty("total_amount", invoices.getTotalAmount().toString() != null ? invoices.getTotalAmount() : 0.0);
            response.addProperty("total_base_amount", invoices.getTotalBaseAmount() != null ? invoices.getTotalBaseAmount() : 0.0);
            response.addProperty("sundry_debtors_name", invoices.getSundryDebtors().getLedgerName() != null ? invoices.getSundryDebtors().getLedgerName() : "");
            response.addProperty("sundry_debtors_id", invoices.getSundryDebtors().getId() != null ? invoices.getSundryDebtors().getId() : 0);
            response.addProperty("sales_order_status", invoices.getTransactionStatus().getStatusName() != null ? invoices.getTransactionStatus().getStatusName() : "");
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName() != null ? invoices.getSalesAccountLedger().getLedgerName() : "");
            response.addProperty("narration", invoices.getNarration() != null ? invoices.getNarration() : "");
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTotalBaseAmount() != null ? invoices.getTotalBaseAmount() : 0.0);
            response.addProperty("transactionTrackingNo", invoices.getTransactionTrackingNo() != null ? invoices.getTransactionTrackingNo() : "");
//            if (!order_type.equalsIgnoreCase("core_product")) {
//                response.addProperty("mobileNo", invoices.getMobileNo());
//                response.addProperty("customer_name", invoices.getCustomerName());
////                response.addProperty("advanceAmount", invoices.getAdvancedAmount().toString());
//            }
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //start of sales order list with pagination
    public Object saleOrdersList(@RequestBody Map<String, String> request, HttpServletRequest req) {
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
        List<TranxSalesOrder> saleOrder = new ArrayList<>();
        List<TranxSalesOrder> saleArrayList = new ArrayList<>();
        List<SalesOrderDTO> salesOrderDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_sales_order_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
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
            System.out.println(" sort " + jsonToStr);
            if (!jsonToStr.isEmpty()) {
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
                }
            } else {
                query = query + " ORDER BY id DESC";
            }
            String query1 = query;       //we get all lists in this list
            System.out.println("query== " + query);
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            Query q = entityManager.createNativeQuery(query, TranxSalesOrder.class);
            System.out.println("q ==" + q + "  saleOrder " + saleOrder);
            saleOrder = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxSalesOrder.class);

            saleArrayList = q1.getResultList();
            System.out.println("Limit total rows " + saleArrayList.size());
            Integer total_pages = (saleArrayList.size() / pageSize);
            if ((saleArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (TranxSalesOrder orderListView : saleOrder) {
                salesOrderDTOList.add(convertToDTDTO(orderListView));
            }
            GenericDatatable<SalesOrderDTO> data = new GenericDatatable<>(salesOrderDTOList, saleArrayList.size(),
                    pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(salesOrderDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    //End of sales order list with pagination
    //Start of DTO for order list
    private SalesOrderDTO convertToDTDTO(TranxSalesOrder tranxSalesOrder) {
        SalesOrderDTO salesOrderDTO = new SalesOrderDTO();
        salesOrderDTO.setId(tranxSalesOrder.getId() != null ? tranxSalesOrder.getId() : 0);
        salesOrderDTO.setBill_no(tranxSalesOrder.getSo_bill_no() != null ? tranxSalesOrder.getSo_bill_no() : "");
        salesOrderDTO.setBill_date(tranxSalesOrder.getBillDate() != null ? DateConvertUtil.convertDateToLocalDate(tranxSalesOrder.getBillDate()).toString() : "");
        salesOrderDTO.setTotal_amount(tranxSalesOrder.getTotalAmount() != null ? tranxSalesOrder.getTotalAmount() : 0.0);

        salesOrderDTO.setTotal_base_amount(tranxSalesOrder.getTotalBaseAmount() != null ? tranxSalesOrder.getTotalBaseAmount() : 0.0);
        salesOrderDTO.setSundry_debtors_name(tranxSalesOrder.getSundryDebtors().getLedgerName() != null ? tranxSalesOrder.getSundryDebtors().getLedgerName() : "");
        salesOrderDTO.setSundry_debtors_id(tranxSalesOrder.getSundryDebtors().getId() != null ? tranxSalesOrder.getSundryDebtors().getId() : 0);
        salesOrderDTO.setSales_order_status(tranxSalesOrder.getTransactionStatus().getStatusName() != null ? tranxSalesOrder.getTransactionStatus().getStatusName() : "");
        salesOrderDTO.setSale_account_name(tranxSalesOrder.getSalesAccountLedger().getLedgerName() != null ? tranxSalesOrder.getSalesAccountLedger().getLedgerName() : "");
        salesOrderDTO.setNarration(tranxSalesOrder.getNarration() != null ? tranxSalesOrder.getNarration() : "");
        salesOrderDTO.setTax_amt(tranxSalesOrder.getTotalTax() != null ? tranxSalesOrder.getTotalTax() : 0.0);
        salesOrderDTO.setTaxable_amt(tranxSalesOrder.getTaxableAmount() != null ? tranxSalesOrder.getTaxableAmount() : 0.0);
        salesOrderDTO.setTransactionTrackingNo(tranxSalesOrder.getTransactionTrackingNo() != null ? tranxSalesOrder.getTransactionTrackingNo() : "");
        salesOrderDTO.setTranxCode(tranxSalesOrder.getTranxCode() != null ? tranxSalesOrder.getTranxCode() : "");


        return salesOrderDTO;

    }
    //End of DTO for order list

    public JsonObject getSaleOrdersWithIds(HttpServletRequest request) {
        JsonObject output = new JsonObject();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSORD");

        String str = request.getParameter("sales_order_ids");
        JsonParser parser = new JsonParser();
        JsonElement purDetailsJson = parser.parse(str);
        JsonArray jsonArray = purDetailsJson.getAsJsonArray();
        JsonArray newList = new JsonArray();
        JsonObject invoiceData = new JsonObject();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            /* getting Units of Purchase Orders */
            JsonArray unitsJsonArray = new JsonArray();
            List<TranxSalesOrderDetailsUnits> unitsArray =
                    tranxSalesOrderDetailsUnitsRepository.findBySalesOrderIdAndTransactionStatusAndStatus(
                            object.get("id").getAsLong(), 1L, true);
            for (TranxSalesOrderDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ?
                        mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ?
                        mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ?
                        mUnits.getLevelC().getId().toString() : "");
                unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
                unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
                unitsJsonObjects.addProperty("qty", mUnits.getQty());
                unitsJsonObjects.addProperty("rate", mUnits.getRate());
                unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ?
                        mUnits.getProduct().getPackingMaster().getPackName() : "");
                unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt() != null ? mUnits.getBaseAmt() : 0.0);
                unitsJsonObjects.addProperty("dis_amt", mUnits.getDiscountAmount() != null ? mUnits.getDiscountAmount() : 0.0);
                unitsJsonObjects.addProperty("dis_per", mUnits.getDiscountPer() != null ? mUnits.getDiscountPer().toString() : "");
                unitsJsonObjects.addProperty("dis_per_cal", mUnits.getDiscountPerCal() != null ? mUnits.getDiscountPerCal().toString() : "");
                unitsJsonObjects.addProperty("dis_amt_cal", mUnits.getDiscountAmountCal() != null ? mUnits.getDiscountAmountCal().toString() : "");
                unitsJsonObjects.addProperty("total_amt", mUnits.getTotalAmount() != null ? mUnits.getTotalAmount() : 0.0);
                unitsJsonObjects.addProperty("gst", mUnits.getIgst() != null ? mUnits.getIgst() : 0.0);
                unitsJsonObjects.addProperty("igst", mUnits.getIgst() != null ? mUnits.getIgst() : 0.0);
                unitsJsonObjects.addProperty("cgst", mUnits.getCgst() != null ? mUnits.getCgst() : 0.0);
                unitsJsonObjects.addProperty("sgst", mUnits.getSgst() != null ? mUnits.getSgst() : 0.0);
                unitsJsonObjects.addProperty("total_igst", mUnits.getTotalIgst() != null ? mUnits.getTotalIgst() : 0.0);
                unitsJsonObjects.addProperty("total_cgst", mUnits.getTotalCgst() != null ? mUnits.getTotalCgst() : 0.0);
                unitsJsonObjects.addProperty("total_sgst", mUnits.getTotalSgst() != null ? mUnits.getTotalSgst() : 0.0);
                unitsJsonObjects.addProperty("final_amt", mUnits.getFinalAmount());
                unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty() != null ? mUnits.getFreeQty().toString() : "");
                unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer() != null ? mUnits.getDiscountBInPer().toString() : "");
                unitsJsonObjects.addProperty("row_dis_amt", mUnits.getTotalDiscountInAmt() != null ? mUnits.getTotalDiscountInAmt() : 0.0);
                unitsJsonObjects.addProperty("gross_amt", mUnits.getGrossAmt() != null ? mUnits.getGrossAmt() : 0.0);
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1() != null ? mUnits.getGrossAmt1() : 0.0);
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt() != null ? mUnits.getInvoiceDisAmt() : 0.0);

                unitsJsonObjects.addProperty("reference_id", mUnits.getSalesOrder().getId());
                unitsJsonObjects.addProperty("reference_type", tranxType.getTransactionCode());
                unitsJsonObjects.addProperty("b_detailsId", 0);
                unitsJsonObjects.addProperty("is_batch", mUnits.getProduct().getIsBatchNumber() != null ?
                        mUnits.getProduct().getIsBatchNumber() : false);
                newList.add(unitsJsonObjects);
                invoiceData.addProperty("id", mUnits.getSalesOrder().getId());
                invoiceData.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(mUnits.getSalesOrder().getBillDate()).toString());
                invoiceData.addProperty("sales_order_no", mUnits.getSalesOrder().getSo_bill_no());
                invoiceData.addProperty("sales_account_id", mUnits.getSalesOrder().getSalesAccountLedger().getId());
                invoiceData.addProperty("sales_account_name", mUnits.getSalesOrder().getSalesAccountLedger().getLedgerName());
                invoiceData.addProperty("sales_sr_no", mUnits.getSalesOrder().getSalesOrderSrNo());
                invoiceData.addProperty("so_sr_no", mUnits.getSalesOrder().getId());
                invoiceData.addProperty("so_transaction_dt", DateConvertUtil.convertDateToLocalDate(mUnits.getSalesOrder().getBillDate()).toString());
                invoiceData.addProperty("reference", mUnits.getSalesOrder().getReference() != null ? mUnits.getSalesOrder().getReference() : "");
                invoiceData.addProperty("debtors_id", mUnits.getSalesOrder().getSundryDebtors().getId());
                invoiceData.addProperty("debtors_name", mUnits.getSalesOrder().getSundryDebtors().getLedgerName());
                invoiceData.addProperty("narration", mUnits.getSalesOrder().getNarration() != null ? mUnits.getSalesOrder().getNarration() : "");
                invoiceData.addProperty("gstNo", mUnits.getSalesOrder().getGstNumber());
                invoiceData.addProperty("transactionTrackingNo", mUnits.getSalesOrder().getTransactionTrackingNo());
                invoiceData.addProperty("narration", mUnits.getSalesOrder().getNarration() != null ? mUnits.getSalesOrder().getNarration() : "");

            }
        }
        JsonArray jsonAdditionalList = new JsonArray();
        output.addProperty("discountLedgerId", 0);
        output.addProperty("discountInAmt", 0);
        output.addProperty("discountInPer", 0);
        output.addProperty("totalSalesDiscountAmt", 0);
        output.add("additional_charges", jsonAdditionalList);
        output.add("invoice_data", invoiceData);
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("row", newList);
        return output;
    }

    /* get Sales Order by Id for Edit */
    public JsonObject getSalesOrder(HttpServletRequest request) throws Exception {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesOrder tranxSalesOrder = tranxSalesOrderRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        List<TranxSalesOrderDetails> list = new ArrayList<>();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            list = salesOrderInvoiceDetailsRepository.findBySalesTransactionIdAndStatus(id, true);
            JsonObject result = new JsonObject();
            /* Sales Quotations Data */
            result.addProperty("id", tranxSalesOrder.getId());
            result.addProperty("sales_sr_no", tranxSalesOrder.getSalesOrderSrNo());
            result.addProperty("sales_account_id", tranxSalesOrder.getSalesAccountLedger().getId());
            result.addProperty("sales_account", tranxSalesOrder.getSalesAccountLedger().getLedgerName());
            result.addProperty("bill_date", DateConvertUtil.convertDateToLocalDate(tranxSalesOrder.getBillDate()).toString());
            result.addProperty("sq_bill_no", tranxSalesOrder.getSo_bill_no());
            result.addProperty("round_off", tranxSalesOrder.getRoundOff());
            result.addProperty("total_base_amount", tranxSalesOrder.getTotalBaseAmount());
            result.addProperty("total_amount", tranxSalesOrder.getTotalAmount());
            result.addProperty("total_cgst", tranxSalesOrder.getTotalcgst());
            result.addProperty("total_sgst", tranxSalesOrder.getTotalsgst());
            result.addProperty("total_igst", tranxSalesOrder.getTotaligst());
            result.addProperty("total_qty", tranxSalesOrder.getTotalqty());
            result.addProperty("taxable_amount", tranxSalesOrder.getTaxableAmount());
            result.addProperty("tcs", tranxSalesOrder.getTcs());
            result.addProperty("status", tranxSalesOrder.getStatus());
            result.addProperty("financial_year", tranxSalesOrder.getFinancialYear());
            result.addProperty("debtor_id", tranxSalesOrder.getSundryDebtors().getId());
            result.addProperty("debtor_name", tranxSalesOrder.getSundryDebtors().getLedgerName());
            result.addProperty("narration", tranxSalesOrder.getNarration() != null ? tranxSalesOrder.getNarration() : "");
            if (!request.getParameter("order_type").equalsIgnoreCase("core_product")) {
                //  result.addProperty("advanceAmount", tranxSalesOrder.getAdvancedAmount());
                result.addProperty("remainingAmount", tranxSalesOrder.getTotalAmount() - tranxSalesOrder.getAdvancedAmount());
                result.addProperty("customer_name", tranxSalesOrder.getCustomerName());
                result.addProperty("mobileNo", tranxSalesOrder.getMobileNo());
            }
            /* End of Sales Quotation Data */

            /* Sales Quotation Details */
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxSalesOrderDetails mDetails : list) {
                    JsonObject prDetails = new JsonObject();
                    prDetails.addProperty("product_id", mDetails.getProduct().getId());
                    prDetails.addProperty("details_id", mDetails.getId());
                    JsonArray serialNo = new JsonArray();
                    /* getting Units of Sales Quotations*/
                    List<TranxSalesOrderDetailsUnits> unitDetails = tranxSalesOrderDetailsUnitsRepository.findBySalesOrderDetailsIdAndStatus(mDetails.getId(), true);
                    JsonArray productDetails = new JsonArray();
                    unitDetails.forEach(mUnit -> {
                        JsonObject mObject = new JsonObject();
                        JsonObject mUnitsObj = new JsonObject();
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
            } /* End of Sales Quotations Details */
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);

        } catch (DataIntegrityViolationException e) {
            salesOrderLogger.error("Exception in getSalesOrder:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesOrderLogger.error("Exception in getSalesOrder:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public Object editSalesOrder(HttpServletRequest request) {
        TranxSalesOrder mSaleTraxOrder = null;
        ResponseMessage responseMessage = new ResponseMessage();
        mSaleTraxOrder = saveIntoSOEdit(request);
        if (mSaleTraxOrder != null) {
            //insertIntoLedgerTranxDetails(mPurchaseTranx);
            responseMessage.setMessage("Sales order updated successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Error in purchase order creation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    private TranxSalesOrder saveIntoSOEdit(HttpServletRequest request) {
        TranxSalesOrder salesOrderTransaction = new TranxSalesOrder();

        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        salesOrderTransaction = tranxSalesOrderRepository.findByIdAndOutletIdAndStatus(
                Long.parseLong(request.getParameter("id")), users.getOutlet().getId(), true);
        Branch branch = null;
        LedgerMaster sundryDebtors = null;
        String custName = "NA";
        if (branch != null) {
            branch = users.getBranch();
            salesOrderTransaction.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        salesOrderTransaction.setOutlet(outlet);
        salesOrderTransaction.setSo_bill_no(request.getParameter("bill_no"));
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        salesOrderTransaction.setBillDate(DateConvertUtil.convertStringToDate(request.getParameter("bill_dt")));
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            salesOrderTransaction.setFiscalYear(fiscalYear);
            salesOrderTransaction.setFinancialYear(fiscalYear.getFiscalYear());
        }
        LedgerMaster salesAccounts = ledgerMasterRepository.findByIdAndStatus(
                Long.parseLong(request.getParameter("sales_acc_id")), true);
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                salesOrderTransaction.setGstNumber(request.getParameter("gstNo"));
            }
        }
        sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), true);

        salesOrderTransaction.setSalesAccountLedger(salesAccounts);
        salesOrderTransaction.setSundryDebtors(sundryDebtors);
        salesOrderTransaction.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
       /* LedgerMaster roundoff = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId("Round off",
                users.getOutlet().getId());*/
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        salesOrderTransaction.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        salesOrderTransaction.setSalesRoundOff(roundoff);
        salesOrderTransaction.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
        if (taxFlag) {
            salesOrderTransaction.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            salesOrderTransaction.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            salesOrderTransaction.setTotaligst(0.0);
        }
        /* if false : igst i.e inter state */
        else {
            salesOrderTransaction.setTotalcgst(0.0);
            salesOrderTransaction.setTotalsgst(0.0);
            salesOrderTransaction.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }
        salesOrderTransaction.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        salesOrderTransaction.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        // salesOrderTransaction.setTcs(Double.parseDouble(request.getParameter("tcs")));
        salesOrderTransaction.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        salesOrderTransaction.setNarration(request.getParameter("narration"));
        salesOrderTransaction.setCreatedBy(users.getId());
//        salesOrderTransaction.setAdditionalChargesTotal(Double.parseDouble(
//                request.getParameter("additionalChargesTotal")));
        salesOrderTransaction.setStatus(true);
        salesOrderTransaction.setOperations("updated");
        salesOrderTransaction.setUpdatedBy(users.getId());
        try {
            mSalesOrderTransaction = tranxSalesOrderRepository.save(salesOrderTransaction);
            /* Save into Sales Duties and Taxes */
            if (mSalesOrderTransaction != null) {
                TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSORD");
                String taxStr = request.getParameter("taxCalculation");
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                // saveIntoDutiesAndTaxesEdit(duties_taxes, mSalesOrderTransaction, taxFlag, tranxType,
                //      users.getOutlet().getId(), users.getId());
                JsonParser parser = new JsonParser();
                String jsonStr = request.getParameter("row");
                JsonElement quotationDetailsJson = parser.parse(jsonStr);
                JsonArray invoiceDetails = quotationDetailsJson.getAsJsonArray();
                String referenceObj = request.getParameter("refObject");
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) rowsDeleted = request.getParameter("rowDelDetailsIds");
                saveIntoTranxSalesOrderDetails(invoiceDetails, mSalesOrderTransaction, branch,
                        outlet, users.getId(), tranxType, referenceObj, "update", rowsDeleted);
            }
            /***** this scenario is for Upahar Trading only , order cakes and payment made from client as an advance ****/
//            if (!request.getParameter("order_type").equalsIgnoreCase("core_product")) {
//                createReceiptInvoice(mSalesOrderTransaction, users, "update");
//            }
        } catch (DataIntegrityViolationException e) {
            salesOrderLogger.error("Exception in saveIntoSOEdit :" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
        } catch (Exception e1) {
            salesOrderLogger.error("Exception in saveIntoSOEdit :" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mSalesOrderTransaction;
    }

    public void saveIntoDutiesAndTaxesEdit(JsonObject duties_taxes, TranxSalesOrder invoiceTranx,
                                           Boolean taxFlag, TransactionTypeMaster tranxType,
                                           Long outletId, Long userId) {
        /* sales Duties and Taxes */
        List<TranxSalesOrderDutiesTaxes> salesDutiesTaxes = new ArrayList<>();
        /* getting duties_taxes_ledger_id  */
        List<Long> db_dutiesLedgerIds = salesOrderDutiesTaxesRepository.findByDutiesAndTaxesId(invoiceTranx.getId());
        //   List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId);
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null);

        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            /** Updation into Sales Duties and Taxes **/
            if (db_dutiesLedgerIds.size() > 0) {
                //insert old records in history
                salesDutiesTaxes = salesOrderDutiesTaxesRepository.findBySalesTransactionAndStatus(invoiceTranx, true);
                //insertIntoDutiesAndTaxesHistory(salesDutiesTaxes);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
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
                            mInputList.add(dutiesTaxes.getId());

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
                        TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) sgstObject.get("gst").getAsDouble();
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
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
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
                        TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                        JsonObject igstObject = mIgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) igstObject.get("gst").getAsDouble();
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
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
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
            //Insertion into Purchase Duties and Taxes
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //    int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;
                        Double amt = cgstObject.get("amt").getAsDouble();
                        //  dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
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
                        TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //    int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        // dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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
                        TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                        JsonObject igstObject = igstList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //    int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
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
                        taxes.setSalesTransaction(invoiceTranx);
                        taxes.setSundryDebtors(invoiceTranx.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesDutiesTaxes.add(taxes);
                    }
                }
            }
        }
        salesOrderDutiesTaxesRepository.saveAll(salesDutiesTaxes);
    }

    private void insertIntoDutiesAndTaxesHistory(List<TranxSalesOrderDutiesTaxes> salesDutiesTaxes) {
        for (TranxSalesOrderDutiesTaxes mList : salesDutiesTaxes) {
            mList.setStatus(false);
            salesOrderDutiesTaxesRepository.save(mList);
        }
    }

    /*    private List<Long> getInputLedgerIds(Boolean taxFlag, JsonObject duties_taxes, Long outletId) {
            List<Long> returnLedgerIds = new ArrayList<>();
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                *//* this is for Cgst creation *//*
            if (cgstList.size() > 0) {
                for (JsonElement mCgst : cgstList) {
                    TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
                    JsonObject cgstObject = mCgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String ledgerName = "INPUT CGST " + inputGst;
                    dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                    if (dutiesTaxes != null) {
                        returnLedgerIds.add(dutiesTaxes.getId());
                    }
                }
            }
            *//* this is for Sgst creation *//*
            if (sgstList.size() > 0) {
                for (JsonElement mSgst : sgstList) {
                    TranxSalesOrderDutiesTaxes taxes = new TranxSalesOrderDutiesTaxes();
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
    }*/
    private List<Long> getInputLedgerIds(Boolean taxFlag, JsonObject duties_taxes, Long outletId, Long branchId) {
        List<Long> returnLedgerIds = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
            JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (JsonElement mCgst : cgstList) {
                    /*  TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();*/
                    JsonObject cgstObject = mCgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT CGST " + inputGst;
                    dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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
                    /*  TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();*/
                    JsonObject sgstObject = mSgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    String inputGst = sgstObject.get("gst").getAsString();//  int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String ledgerName = "INPUT SGST " + inputGst;
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
                    //dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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

    public JsonObject getOrderBillPrint(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesOrderDetailsUnits> list = new ArrayList<>();
        List<TranxCounterSalesDetailsUnits> listcs = new ArrayList<>();
        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxSalesInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        TranxSalesOrder salesQuotation = null;
        TranxCounterSales counterSales = null;
        Long id = 0L;
        String source = request.getParameter("source");
        String key = request.getParameter("print_type"); //check whether printbill is calling from create page or from list page
        /***** if  print_type is create, then use serialnumber of invoice to fetch invoice details ,
         * if print_type is list then use invoice id to fetch invoice details *****/
        try {
            String invoiceNo = request.getParameter("id");
            if (source.equalsIgnoreCase("sales_order")) { //counter_sales
                if (users.getBranch() != null) {
                    if (key.equalsIgnoreCase("create")) {
                        salesQuotation = tranxSalesOrderRepository.findBySoBillWithBranch(users.getOutlet().getId(), users.getBranch().getId(), invoiceNo);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesQuotation = tranxSalesOrderRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    }
                } else {
                    if (key.equalsIgnoreCase("create")) {
                        salesQuotation = tranxSalesOrderRepository.findBySoBill(users.getOutlet().getId(), invoiceNo);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesQuotation = tranxSalesOrderRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);
                    }
                }
            }
            if (salesQuotation != null) {

                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                debtorsObject.addProperty("supplier_name", salesQuotation.getSundryDebtors().getLedgerName());
                debtorsObject.addProperty("supplier_address", salesQuotation.getSundryDebtors().getAddress());
                debtorsObject.addProperty("supplier_gstin", salesQuotation.getSundryDebtors().getGstin());
                debtorsObject.addProperty("supplier_phone", salesQuotation.getSundryDebtors().getMobile());

                JsonObject invoiceObject = new JsonObject();
                /* Sales Invoice Data */
                invoiceObject.addProperty("id", salesQuotation.getId());
                invoiceObject.addProperty("invoice_dt", salesQuotation.getBillDate().toString());
                invoiceObject.addProperty("invoice_no", salesQuotation.getSalesOrderSrNo());
                invoiceObject.addProperty("state_code", salesQuotation.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", salesQuotation.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(salesQuotation.getTaxableAmount()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(salesQuotation.getTotaligst()));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(salesQuotation.getTotalcgst()));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(salesQuotation.getTotalsgst()));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(salesQuotation.getTotalBaseAmount()));
//                invoiceObject.addProperty("total_discount", numFormat.numFormat(salesQuotation.getTotalSalesDiscountAmt()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(salesQuotation.getTotalAmount()));
//                invoiceObject.addProperty("advanced_amount", numFormat.numFormat(salesQuotation.getAdvancedAmount() != null ? salesQuotation.getAdvancedAmount() : 0.0));
//                invoiceObject.addProperty("payment_mode", salesQuotation.getPaymentMode());


                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();
                /* getting Units of Sales Quotations*/
                List<TranxSalesQuotationDetailsUnits> unitDetails = tranxSalesQuotaionDetailsUnitsRepository.findBySalesQuotationIdAndStatus(salesQuotation.getId(), true);
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
//                    if (mUnit.getProductBatchNo() != null) {
//                        mObject.addProperty("b_details_id", mUnit.getProductBatchNo().getId());
//                        mObject.addProperty("b_no", mUnit.getProductBatchNo().getBatchNo());
//                        mObject.addProperty("is_batch", true);
//
//                    } else {
                    mObject.addProperty("b_details_id", "");
                    mObject.addProperty("b_no", "");
                    mObject.addProperty("is_batch", false);
//                    }
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
            salesOrderLogger.error("Error in getInvoiceBillPrint :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesOrderLogger.error("Error in getInvoiceBillPrint :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    /**
     * Delete sales order
     **/
    public JsonObject salesOrderDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesOrder salesTranx = tranxSalesOrderRepository.findByIdAndStatusAndTransactionStatusId(Long.parseLong(request.getParameter("id")), true, 1L);
        TranxSalesOrder mPurchaseTranx;
        try {
            if (salesTranx != null) {
                salesTranx.setStatus(false);
                salesTranx.setOperations("deletion");
                mPurchaseTranx = tranxSalesOrderRepository.save(salesTranx);
                jsonObject.addProperty("message", "Sales order deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "cant delete closed orders");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }
        } catch (Exception e) {
            salesOrderLogger.error("Error in sales order Delete()->" + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject getProductEditByIdByFPU(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesOrder invoiceTranx = tranxSalesOrderRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        productIds = tranxSalesOrderDetailsUnitsRepository.findByTranxPurId(invoiceTranx.getId(), true);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getProductEditByIdsByFPU(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String str = request.getParameter("s_o_id");
        JsonParser parser = new JsonParser();
        JsonElement salesDetailsJson = parser.parse(str);
        JsonArray jsonArray = salesDetailsJson.getAsJsonArray();
        JsonArray productArray = new JsonArray();
        JsonObject output = new JsonObject();
        JsonObject result = new JsonObject();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            TranxSalesOrder invoiceTranx = tranxSalesOrderRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
            List<Object[]> productIds = new ArrayList<>();
            productIds = tranxSalesOrderDetailsUnitsRepository.findByTranxPurId(invoiceTranx.getId(), true);
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
                        List<Long> levelCunits = productUnitRepository.findByProductsLevelC(
                                mProduct.getId(), mLeveA, mLeveB);
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
                            List<Object[]> unitList = productUnitRepository.
                                    findUniqueUnitsByProductId(mProduct.getId(), mLeveA,
                                            mLeveB, mLeveC);
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
                                    List<ProductBatchNo> batchNos = productBatchNoRepository.findByUniqueBatchProductIdAndStatus(mProduct.getId(),
                                            levelAId, levelBId, levelCId, unitId, true);
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
                                            batchObject.addProperty("costing_with_tax", mBatch.getCostingWithTax() != null ?
                                                    mBatch.getCostingWithTax() : 0.00);
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
                                                salesRateWithTax = mBatch.getSalesRate() +
                                                        ((mBatch.getSalesRate() * mBatch.getProduct().getTaxMaster().getIgst()) / 100);
                                            batchObject.addProperty("sales_rate_with_tax", salesRateWithTax);
                                            if (mBatch.getExpiryDate() != null) {
                                                if (DateConvertUtil.convertDateToLocalDate(invoiceTranx.getBillDate()).isAfter(mBatch.getExpiryDate())) {
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
           /* productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);
            result.add("invoice_list", productArray);*/
        }
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);

        return output;
    }

    public JsonObject getSalesOrderByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesOrderDetails> list = new ArrayList<>();
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxSalesOrder tranxSalesOrder = tranxSalesOrderRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            list = salesOrderInvoiceDetailsRepository.findBySalesTransactionIdAndStatus(id, true);
            finalResult.addProperty("tcs", tranxSalesOrder.getTcs());
            finalResult.addProperty("narration", tranxSalesOrder.getNarration() != null ? tranxSalesOrder.getNarration() : "");
            JsonObject result = new JsonObject();
            /* Purchase Order Data */
            result.addProperty("id", tranxSalesOrder.getId());
            result.addProperty("sales_sr_no", tranxSalesOrder.getSalesOrderSrNo());
            result.addProperty("sales_account_id", tranxSalesOrder.getSalesAccountLedger().getId());
            result.addProperty("sales_account", tranxSalesOrder.getSalesAccountLedger().getLedgerName());
            result.addProperty("supplierGST", tranxSalesOrder.getSundryDebtors().getGstin());
            result.addProperty("transaction_dt", DateConvertUtil.convertDateToLocalDate(tranxSalesOrder.getBillDate()).toString());
            result.addProperty("bill_date", DateConvertUtil.convertDateToLocalDate(tranxSalesOrder.getBillDate()).toString());
            result.addProperty("so_bill_no", tranxSalesOrder.getSo_bill_no());
            result.addProperty("tranx_unique_code", tranxSalesOrder.getTranxCode());
            result.addProperty("round_off", tranxSalesOrder.getRoundOff());
            result.addProperty("total_base_amount", tranxSalesOrder.getTotalBaseAmount());
            result.addProperty("total_amount", tranxSalesOrder.getTotalAmount());
            result.addProperty("total_cgst", tranxSalesOrder.getTotalcgst());
            result.addProperty("total_sgst", tranxSalesOrder.getTotalsgst());
            result.addProperty("total_igst", tranxSalesOrder.getTotaligst());
            result.addProperty("total_qty", tranxSalesOrder.getTotalqty());
            result.addProperty("taxable_amount", tranxSalesOrder.getTaxableAmount());
            result.addProperty("tcs", tranxSalesOrder.getTcs());
            result.addProperty("status", tranxSalesOrder.getStatus());
            result.addProperty("financial_year", tranxSalesOrder.getFinancialYear());
            result.addProperty("debtor_id", tranxSalesOrder.getSundryDebtors().getId());
            result.addProperty("debtor_name", tranxSalesOrder.getSundryDebtors().getLedgerName());
            result.addProperty("narration", tranxSalesOrder.getNarration() != null ? tranxSalesOrder.getNarration() : "");
            result.addProperty("gstNo", tranxSalesOrder.getGstNumber());
            result.addProperty("ledgerStateCode", tranxSalesOrder.getSundryDebtors().getStateCode());
            /* End of Sales Quotations Data */
            JsonArray row = new JsonArray();
            JsonArray unitsJsonArray = new JsonArray();
            List<TranxSalesOrderDetailsUnits> unitsArray = tranxSalesOrderDetailsUnitsRepository.
                    findBySalesOrderIdAndTransactionStatusAndStatus(tranxSalesOrder.getId(), 1L, true);
            for (TranxSalesOrderDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ? mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ? mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ? mUnits.getLevelC().getId().toString() : "");
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


                JsonArray mLevelArray = new JsonArray();
                List<Long> levelaArray = productUnitRepository.findLevelAIdDistinct(mUnits.getProduct().getId());
                for (Long mLeveA : levelaArray) {
                    JsonObject levelaJsonObject = new JsonObject();
                    LevelA levelA = null;
                    if (mLeveA != null) {
                        levelA = levelARepository.findByIdAndStatus(mLeveA, true);
                        if (levelA != null) {
                            levelaJsonObject.addProperty("levela_id", levelA.getId());
                            levelaJsonObject.addProperty("levela_name", levelA.getLevelName());
                        }
                    }
                    mLevelArray.add(levelaJsonObject);
                }
                unitsJsonObjects.add("levelAOpt", mLevelArray);
                row.add(unitsJsonObjects);
            }
            List<LedgerGstDetails> gstDetails = new ArrayList<>();
            gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(tranxSalesOrder.getSundryDebtors().getId(), true);
            JsonArray gstArray = new JsonArray();
            if (gstDetails != null && gstDetails.size() > 0) {
                for (LedgerGstDetails mGstDetails : gstDetails) {
                    JsonObject mGstObject = new JsonObject();
                    mGstObject.addProperty("id", mGstDetails.getId());
                    mGstObject.addProperty("gstNo", mGstDetails.getGstin());
                    mGstObject.addProperty("state", mGstDetails.getStateCode() != null ? mGstDetails.getStateCode() : "");
                    gstArray.add(mGstObject);
                }
            }
            finalResult.add("gstDetails", gstArray);
            /* End of Purchase Invoice Details */
            System.out.println("Row  " + row);
            finalResult.add("row", row);
            finalResult.add("invoice_data", result);
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesOrderLogger.error("Error in getSalesOrderByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesOrderLogger.error("Error in getSalesOrderByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject getSalesOrderSupplierListByProductId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        Long productId = Long.parseLong(request.getParameter("productId"));

        List<TranxSalesOrderDetailsUnits> tranxSalesOrderDetailsUnits = tranxSalesOrderDetailsUnitsRepository.findByProductIdAndStatusOrderByIdDesc(productId, true);

        for (TranxSalesOrderDetailsUnits obj : tranxSalesOrderDetailsUnits) {
            JsonObject response = new JsonObject();
            response.addProperty("supplier_name", obj.getSalesOrder().getSundryDebtors().getLedgerName());
            response.addProperty("invoice_no", obj.getSalesOrder().getId());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(obj.getSalesOrder().getBillDate()).toString());
//            response.addProperty("batch",obj.getProductBatchNo().getBatchNo());
//            response.addProperty("mrp",obj.getProductBatchNo().getMrp());
            response.addProperty("quantity", obj.getQty());
            response.addProperty("rate", obj.getRate());
//            response.addProperty("cost",obj.getProductBatchNo().getCosting());
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

    public JsonObject saleOrdersPendingList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), true);
        List<TranxSalesOrder> tranxSalesOrders = tranxSalesOrderRepository.findBySundryDebtorsIdAndStatusAndTransactionStatusId(sundryDebtors.getId(), true, 1L);
        for (TranxSalesOrder invoices : tranxSalesOrders) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("bill_no", invoices.getSo_bill_no());
            response.addProperty("bill_date", DateConvertUtil.convertDateToLocalDate(invoices.getBillDate()).toString());
            response.addProperty("total_amount", invoices.getTotalAmount().toString());
            response.addProperty("total_base_amount", invoices.getTotalBaseAmount());
            response.addProperty("sundry_debtors_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtors_id", invoices.getSundryDebtors().getId());
            response.addProperty("sales_order_status", invoices.getTransactionStatus().getStatusName());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration());
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTotalBaseAmount());
//            if (!order_type.equalsIgnoreCase("core_product")) {
//                response.addProperty("mobileNo", invoices.getMobileNo());
//                response.addProperty("customer_name", invoices.getCustomerName());
////                response.addProperty("advanceAmount", invoices.getAdvancedAmount().toString());
//            }
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public Object validateSalesOrder(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesOrder salesOrder = null;
        if (users.getBranch() != null) {
            salesOrder = tranxSalesOrderRepository.findBySoBillWithBranch(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesOrderNo"));
        } else {
            salesOrder = tranxSalesOrderRepository.findBySoBill(users.getOutlet().getId(), request.getParameter("salesOrderNo"));
        }
        if (salesOrder != null) {
            // System.out.println("Already Ledger created with this name or code");
            responseMessage.setMessage("Duplicate sales order number exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object validateSalesOrderUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Long invoiceId = Long.parseLong(request.getParameter("invoice_id"));
        TranxSalesOrder salesOrder = null;
        if (users.getBranch() != null) {
            salesOrder = tranxSalesOrderRepository.findBySoBillWithBranch(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesOrderNo"));
        } else {
            salesOrder = tranxSalesOrderRepository.findBySoBill(users.getOutlet().getId(), request.getParameter("salesOrderNo"));
        }
        if (salesOrder != null && invoiceId != salesOrder.getId()) {
            responseMessage.setMessage("Duplicate sales order number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales order number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }
}
