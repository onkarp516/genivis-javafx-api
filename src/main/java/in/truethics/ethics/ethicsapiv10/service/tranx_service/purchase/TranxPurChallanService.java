package in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.puarchasedto.PurChallanDTO;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesOrder;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.StockTranxDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerGstDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.product_barcode.ProductBarcodeRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesChallanProductSerialNumberRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.ClosingUtility;
import in.truethics.ethics.ethicsapiv10.util.DateConvertUtil;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import in.truethics.ethics.ethicsapiv10.util.TranxCodeUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.Query;

import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;

@Service
public class TranxPurChallanService {
    @Autowired
    private NumFormat numFormat;
    @Autowired
    private TranxPurOrderRepository tranxPurOrderRepository;
    @Autowired
    private TranxPurchaseChallanProductSrNumberRepository tranxPurchaseChallanProductSrNumberRepository;
    @Autowired
    private TranxPurChallanRepository tranxPurChallanRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TranxPurChallanDutiesTaxesRepository tranxPurChallanDutiesTaxesRepository;
    @Autowired
    private TranxPurChallanDetailsRepository tranxPurChallanDetailsRepository;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TranxPurChallanAddChargesRepository tranxPurChallanAddChargesRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private StockTranxDetailsRepository stkTranxDetailsRepository;

    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private TranxPurChallanDetailsUnitRepository tranxPurChallanDetailsUnitRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private ProductBarcodeRepository barcodeRepository;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    private ProductData productData;
    @Autowired
    private TranxPurOrderDetailsUnitRepository tranxPurOrderDetailsUnitRepository;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    private static final Logger purChallanLogger = LogManager.getLogger(TranxPurChallanService.class);
    @Autowired
    private LedgerGstDetailsRepository ledgerGstDetailsRepository;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;

    @Autowired
    private ClosingUtility closingUtility;

    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice
    List<Long> mInputList = new ArrayList<>();
    @Autowired
    private TranxSalesChallanProductSerialNumberRepository tranxSalesChallanProductSerialNumberRepository;

    @Autowired
    private TranxPurchaseChallanProductSrNumberRepository serialNumberRepository;

    public Object insertPOChallanInvoice(HttpServletRequest request) {
        TranxPurChallan mPOChallanTranx = null;
        ResponseMessage responseMessage = new ResponseMessage();
        mPOChallanTranx = saveIntoPOChallanInvoice(request);
        if (mPOChallanTranx != null) {
            responseMessage.setMessage("Purchase challan created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            /**
             * @implNote validation of Ledger Delete , if any tranx done for this ledger, user cant delete this ledger **
             * @auther ashwins@opethic.com
             * @version sprint 21
             **/
            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(mPOChallanTranx.getSundryCreditors().getId(), true);
            ledgerMaster.setIsDeleted(false);
            ledgerMasterRepository.save(ledgerMaster);
        } else {
            responseMessage.setMessage("Error in purchase invoice creation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    /*******  Save into Purchase Challan    *********/
    /******* @return *******/
    public TranxPurChallan saveIntoPOChallanInvoice(HttpServletRequest request) {
        TranxPurChallan mPOChallanTranx = null;
        TransactionTypeMaster tranxType = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        TranxPurChallan tranxPurChallan = new TranxPurChallan();
        tranxPurChallan.setCreatedBy(users.getId());
        if (users.getBranch() != null) {
            branch = users.getBranch();
            tranxPurChallan.setBranch(branch);
        }
        tranxPurChallan.setOutlet(outlet);
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        tranxPurChallan.setTransactionStatus(transactionStatus);
        /** Conversion ***/
        if (paramMap.containsKey("reference_po_ids")) {
            String jsonRef = request.getParameter("reference_po_ids");
            JsonParser parser = new JsonParser();
            JsonElement referenceDetailsJson = parser.parse(jsonRef);
            JsonArray referenceDetails = referenceDetailsJson.getAsJsonArray();
            String id = "";
            for (int i = 0; i < referenceDetails.size(); i++) {
                JsonObject object = referenceDetails.get(i).getAsJsonObject();
                id = id + object.get("id").getAsString();
                if (i < referenceDetails.size() - 1) id = id + ",";
            }
            tranxPurChallan.setOrderReference(id);
            //   setCloseSO(request.getParameter("reference_so_id"));
        }
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSCHN");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        tranxPurChallan.setTranxCode(tranxCode);
        System.out.println("Invoice Date:" + request.getParameter("invoice_date"));
        LocalDate invoiceDate = DateConvertUtil.convertStringToLocalDate(request.getParameter("invoice_date"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("invoice_date"));
        tranxPurChallan.setInvoiceDate(strDt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(invoiceDate);
        if (fiscalYear != null) {
            tranxPurChallan.setFiscalYear(fiscalYear);
            tranxPurChallan.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        if (paramMap.containsKey("transport_name"))
            tranxPurChallan.setTransportName(request.getParameter("transport_name"));
        else tranxPurChallan.setTransportName("");
        if (paramMap.containsKey("reference")) tranxPurChallan.setReference(request.getParameter("reference"));
        else tranxPurChallan.setReference("");
        tranxPurChallan.setVendorInvoiceNo(request.getParameter("invoice_no"));
        tranxPurChallan.setPurChallanSrno(Long.parseLong(request.getParameter("purchase_sr_no")));
        LedgerMaster purchaseAccount = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("purchase_id")), true);
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                tranxPurChallan.setGstNumber(request.getParameter("gstNo"));
            }
        }
        LedgerMaster discountLedger = null;
        if (users.getBranch() == null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("purchase discount", users.getOutlet().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("purchase discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        if (discountLedger != null) {
            tranxPurChallan.setPurchaseDiscountLedger(discountLedger);
        }
        LedgerMaster sundryCreditors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), true);
        tranxPurChallan.setPurchaseAccountLedger(purchaseAccount);
        tranxPurChallan.setSundryCreditors(sundryCreditors);
        LocalDate mDate = LocalDate.parse(request.getParameter("transaction_date"));
        tranxPurChallan.setTransactionDate(mDate);

        tranxPurChallan.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        tranxPurChallan.setPurchaseRoundOff(roundoff);
        tranxPurChallan.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
        tranxPurChallan.setPurchaseDiscountPer(Double.parseDouble(request.getParameter("purchase_discount")));
        tranxPurChallan.setPurchaseDiscountAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
        tranxPurChallan.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        tranxPurChallan.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
        if (taxFlag) {
            tranxPurChallan.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            tranxPurChallan.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            tranxPurChallan.setTotaligst(0.0);
        }
        /* if false : igst i.e inter state */
        else {
            tranxPurChallan.setTotalcgst(0.0);
            tranxPurChallan.setTotalsgst(0.0);
            tranxPurChallan.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }
        tranxPurChallan.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        tranxPurChallan.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        if (paramMap.containsKey("tcs")) tranxPurChallan.setTcs(Double.parseDouble(request.getParameter("tcs")));
        else {
            tranxPurChallan.setTcs(0.0);
        }
        tranxPurChallan.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        tranxPurChallan.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        tranxPurChallan.setStatus(true);
        tranxPurChallan.setOperations("insertion");
        if (paramMap.containsKey("narration")) tranxPurChallan.setNarration(request.getParameter("narration"));
        else {
            tranxPurChallan.setNarration("");
        }
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                tranxPurChallan.setGstNumber(request.getParameter("gstNo"));
            }
        }
        tranxPurChallan.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        tranxPurChallan.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        /*if (paramMap.containsKey("additionalChgLedger1")) {
            LedgerMaster additionalChgLedger1 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger1")), users.getOutlet().getId(), true);
            if (additionalChgLedger1 != null) {
                tranxPurChallan.setAdditionLedger1(additionalChgLedger1);
                tranxPurChallan.setAdditionLedgerAmt1(Double.valueOf(request.getParameter("addChgLedgerAmt1")));
            }
        }
        if (paramMap.containsKey("additionalChgLedger2")) {
            LedgerMaster additionalChgLedger2 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger2")), users.getOutlet().getId(), true);
            if (additionalChgLedger2 != null) {
                tranxPurChallan.setAdditionLedger2(additionalChgLedger2);
                tranxPurChallan.setAdditionLedgerAmt2(Double.valueOf(request.getParameter("addChgLedgerAmt2")));
            }
        }*/
        if (paramMap.containsKey("additionalChgLedger3")) {
            LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
            if (additionalChgLedger3 != null) {
                tranxPurChallan.setAdditionLedger3(additionalChgLedger3);
                tranxPurChallan.setAdditionLedgerAmt3(Double.valueOf(request.getParameter("addChgLedgerAmt3")));
            }
        }
        if (paramMap.containsKey("isRoundOffCheck"))
            tranxPurChallan.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));
        if (paramMap.containsKey("roundoff"))
            tranxPurChallan.setRoundOff(Double.valueOf((request.getParameter("roundoff"))));

        if (request.getParameterMap().containsKey("transactionTrackingNo"))
            tranxPurChallan.setTransactionTrackingNo(request.getParameter("transactionTrackingNo"));
        else
            tranxPurChallan.setTransactionTrackingNo(String.valueOf(new Date().getTime()));
        try {
            mPOChallanTranx = tranxPurChallanRepository.save(tranxPurChallan);
            if (mPOChallanTranx != null) {
                /* Save into Duties and Taxes */
                String taxStr = request.getParameter("taxCalculation");
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoPOChallanDutiesTaxes(duties_taxes, mPOChallanTranx, taxFlag);
                /* save into Additional Charges  */
                /*String strJson = request.getParameter("additionalCharges");
                JsonElement tradeElement = new JsonParser().parse(strJson);
                JsonArray additionalCharges = tradeElement.getAsJsonArray();
                saveIntoPurchaseAdditionalCharges(additionalCharges, mPOChallanTranx, tranxType.getId(), users.getOutlet().getId());*/

                /* save into Additional Charges  */
                String strJson = request.getParameter("additionalCharges");
                JsonElement tradeElement = new JsonParser().parse(strJson);
                JsonArray additionalCharges = tradeElement.getAsJsonArray();
                saveIntoPurchaseAdditionalCharges(additionalCharges, mPOChallanTranx, tranxType.getId(), users.getOutlet().getId());

                String jsonStr = request.getParameter("row");
                JsonParser parser = new JsonParser();
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                String referenceObject = request.getParameter("refObject");
                saveIntoPOChallanInvoiceDetails(array, mPOChallanTranx, branch, outlet, users.getId(), tranxType, referenceObject);
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purChallanLogger.error("Error in saveIntoPOChallanInvoice:->" + e.getMessage());
            //purChallanLogger.error("DataIntegrityViolationException in saveIntoPOChallanInvoice:" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            purChallanLogger.error("Error in saveIntoPOChallanInvoice :->" + e1.getMessage());
            //purChallanLogger.error("Exception in saveIntoPOChallanInvoice:" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return tranxPurChallan;
    }

    /* End of Purchase Invoice */
    /* Close All Purchase Orders */
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

    /****** Save into Duties and Taxes ******/
    public void saveIntoPOChallanDutiesTaxes(JsonObject duties_taxes, TranxPurChallan mPOChallanTranx, Boolean taxFlag) {
        List<TranxPurChallanDutiesTaxes> tranxPurChallanDutiesTaxes = new ArrayList<>();
        try {
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mList : cgstList) {
                        TranxPurChallanDutiesTaxes tranxPurChallanDutiesTaxes1 = new TranxPurChallanDutiesTaxes();
                        JsonObject cgstObject = mList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //    int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT CGST " + inputGst;

                        if (mPOChallanTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPOChallanTranx.getOutlet().getId(), mPOChallanTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPOChallanTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            tranxPurChallanDutiesTaxes1.setDutiesTaxes(dutiesTaxes);
                        }
                        tranxPurChallanDutiesTaxes1.setAmount(Double.parseDouble(cgstObject.get("amt").getAsString()));
                        tranxPurChallanDutiesTaxes1.setTranxPurChallan(mPOChallanTranx);
                        tranxPurChallanDutiesTaxes1.setSundryCreditors(mPOChallanTranx.getSundryCreditors());
                        tranxPurChallanDutiesTaxes1.setIntra(taxFlag);
                        tranxPurChallanDutiesTaxes1.setStatus(true);
                        tranxPurChallanDutiesTaxes1.setCreatedBy(mPOChallanTranx.getCreatedBy());

                        tranxPurChallanDutiesTaxes.add(tranxPurChallanDutiesTaxes1);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mList : sgstList) {
                        TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
                        JsonObject sgstObject = mList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT SGST " + inputGst;
                        if (mPOChallanTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPOChallanTranx.getOutlet().getId(), mPOChallanTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPOChallanTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(Double.parseDouble(sgstObject.get("amt").getAsString()));
                        taxes.setTranxPurChallan(mPOChallanTranx);
                        taxes.setSundryCreditors(mPOChallanTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        taxes.setCreatedBy(mPOChallanTranx.getCreatedBy());

                        tranxPurChallanDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mList : igstList) {
                        TranxPurChallanDutiesTaxes tranxPurChallanDutiesTaxes1 = new TranxPurChallanDutiesTaxes();
                        JsonObject igstObject = mList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        if (mPOChallanTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mPOChallanTranx.getOutlet().getId(), mPOChallanTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mPOChallanTranx.getOutlet().getId(), ledgerName);
                        if (dutiesTaxes != null) {
                            tranxPurChallanDutiesTaxes1.setDutiesTaxes(dutiesTaxes);
                        }
                        tranxPurChallanDutiesTaxes1.setAmount(Double.parseDouble(igstObject.get("amt").getAsString()));
                        tranxPurChallanDutiesTaxes1.setTranxPurChallan(mPOChallanTranx);
                        tranxPurChallanDutiesTaxes1.setSundryCreditors(mPOChallanTranx.getSundryCreditors());
                        tranxPurChallanDutiesTaxes1.setIntra(taxFlag);
                        tranxPurChallanDutiesTaxes1.setStatus(true);
                        tranxPurChallanDutiesTaxes1.setCreatedBy(mPOChallanTranx.getCreatedBy());
                        tranxPurChallanDutiesTaxes.add(tranxPurChallanDutiesTaxes1);
                    }
                }
            }

            /* save all Duties and Taxes into purchase Invoice Duties taxes table */
            tranxPurChallanDutiesTaxesRepository.saveAll(tranxPurChallanDutiesTaxes);
        } catch (DataIntegrityViolationException e) {
            //purChallanLogger.error("Error in Payment Creation :->" + e.getMessage());
            purChallanLogger.error("DataIntegrityViolationException in saveIntoPOChallanDutiesTaxes: " + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (Exception e1) {
            //purChallanLogger.error("Error in Payment Creation :->" + e1.getMessage());
            purChallanLogger.error("Exception in saveIntoPOChallanDutiesTaxes: " + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }
    }
    /* End of Purchase Duties and Taxes Ledger */


    /****** save into Purchase Invoice Details ******/
    public void saveIntoPOChallanInvoiceDetails(JsonArray array, TranxPurChallan mTranxPurChallan, Branch branch, Outlet outlet, Long userId, TransactionTypeMaster tranxType, String referenceObject) {

        /* Purchase Product Details Start here */
        try {
            String referenceType = "";
            Long referenceId = 0L;
            boolean flag_status = false;
            List<TranxPurchaseChallanProductSrNumber> newSerialNumbers = new ArrayList<>();
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
                TranxPurChallanDetailsUnits invoiceUnits = new TranxPurChallanDetailsUnits();
                invoiceUnits.setTranxPurChallan(mTranxPurChallan);
                invoiceUnits.setProduct(mProduct);
                invoiceUnits.setUnits(units);
                invoiceUnits.setQty(object.get("qty").getAsDouble());
                tranxQty = object.get("qty").getAsDouble();
                if (!object.get("free_qty").getAsString().equalsIgnoreCase("")) {
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
//                if (object.has("unit_conv")) invoiceUnits.setUnitConversions(object.get("unit_conv").getAsDouble());
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
                invoiceUnits.setTransactionStatus(1L);
                /******* Insert into Product Batch No *******/
                boolean flag = false;
                try {
                    if (object.get("is_batch").getAsBoolean()) {
                        flag = true;
                        Double qty = object.get("qty").getAsDouble();
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
                        }
                        if (object.get("b_details_id").getAsLong() == 0) {
                            ProductBatchNo mproductBatchNo = new ProductBatchNo();
                            if (object.has("b_no")) mproductBatchNo.setBatchNo(object.get("b_no").getAsString());
                            if (object.has("b_rate")) mproductBatchNo.setMrp(object.get("b_rate").getAsDouble());
                            if (object.has("b_purchase_rate") && !object.get("b_purchase_rate").equals(""))
                                mproductBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                            if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("")) {
                                System.out.println("B. Expiry dates:" + object.get("b_expiry").getAsString());
                                mproductBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                            }
                            if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase(""))
                                mproductBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
                            mproductBatchNo.setQnty(qty.intValue());
                            if (!object.get("free_qty").getAsString().equalsIgnoreCase(""))
                                mproductBatchNo.setFreeQty(object.get("free_qty").getAsDouble());
                            if (!object.get("sales_rate").getAsString().equalsIgnoreCase(""))
                                mproductBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                            mproductBatchNo.setCosting(costing);
                            mproductBatchNo.setCostingWithTax(costing_with_tax);
                            mproductBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                            mproductBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                            mproductBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                            if (object.has("min_margin"))
                                mproductBatchNo.setMinMargin(object.get("min_margin").getAsDouble());
                            mproductBatchNo.setStatus(true);
                            mproductBatchNo.setProduct(mProduct);
                            mproductBatchNo.setOutlet(outlet);
                            mproductBatchNo.setBranch(branch);
                            if (levelA != null) mproductBatchNo.setLevelA(levelA);
                            if (levelB != null) mproductBatchNo.setLevelB(levelB);
                            if (levelC != null) mproductBatchNo.setLevelC(levelC);
                            mproductBatchNo.setUnits(units);
                            mproductBatchNo.setSupplierId(mTranxPurChallan.getSundryCreditors().getId());
                            try {
                                productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("Exception " + e.getMessage());
                            }
                        } else {
                            productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                            productBatchNo.setQnty(qty.intValue());
                            productBatchNo.setFreeQty(free_qty);
                            if (object.has("sales_rate") && !object.get("sales_rate").getAsString().equalsIgnoreCase(""))
                                productBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                            productBatchNo.setCosting(costing);
                            productBatchNo.setCostingWithTax(costing_with_tax);
                            if (object.has("b_no")) productBatchNo.setBatchNo(object.get("b_no").getAsString());
                            if (object.has("b_rate")) productBatchNo.setMrp(object.get("b_rate").getAsDouble());
                            if (object.has("b_sale_rate"))
                                productBatchNo.setSalesRate(object.get("b_sale_rate").getAsDouble());
                            if (object.has("b_purchase_rate"))
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
                            if (object.has("margin_per") && !object.get("margin_per").getAsString().equals(""))
                                productBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
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
                    invoiceUnits.setProductBatchNo(productBatchNo);
                    TranxPurChallanDetailsUnits mTranxUnitDetails = tranxPurChallanDetailsUnitRepository.save(invoiceUnits);
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
                        /******* Insert into Tranx Product Seial Numbers  ******/
                        JsonArray jsonArray = object.getAsJsonArray("serialNo");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            for (JsonElement jsonElement : jsonArray) {
                                JsonObject jsonSrno = jsonElement.getAsJsonObject();
                                serialNo = jsonSrno.get("serial_no").getAsString();
                                TranxPurchaseChallanProductSrNumber productSerialNumber = new TranxPurchaseChallanProductSrNumber();
                                productSerialNumber.setProduct(mProduct);
                                productSerialNumber.setSerialNo(serialNo);
                                //productSerialNumber.setTranxPurChallan(mTranxPurChallan);
                                productSerialNumber.setTransactionStatus("Purchase Challan");
                                productSerialNumber.setStatus(true);
                                productSerialNumber.setCreatedBy(userId);
                                productSerialNumber.setOperations("Inserted");
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setBranch(mTranxPurChallan.getBranch());
                                productSerialNumber.setOutlet(mTranxPurChallan.getOutlet());
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setUnits(units);
                                productSerialNumber.setTranxPurChallanDetailsUnits(mTranxUnitDetails);
                                productSerialNumber.setLevelA(levelA);
                                productSerialNumber.setLevelB(levelB);
                                productSerialNumber.setLevelC(levelC);
                                productSerialNumber.setUnits(units);
                                TranxPurchaseChallanProductSrNumber mSerialNo = tranxPurchaseChallanProductSrNumberRepository.save(productSerialNumber);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToInventoryPostings("CR", mTranxPurChallan.getInvoiceDate(), mTranxPurChallan.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mTranxPurChallan.getFiscalYear(), serialNo);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    purChallanLogger.error("Exception in saveIntoPurchaseInvoiceDetails:" + e.getMessage());
                }
                /* Inserting into Barcode */
                ProductBarcode barcode = new ProductBarcode();
                barcode.setMrp(object.get("rate").getAsDouble());
                barcode.setTranxDate(mTranxPurChallan.getTransactionDate());
                barcode.setEnable(true);
                barcode.setStatus(true);
                barcode.setBranch(mTranxPurChallan.getBranch());
                barcode.setFiscalYear(mTranxPurChallan.getFiscalYear());
                barcode.setOutlet(mTranxPurChallan.getOutlet());
                barcode.setProduct(mProduct);
                if (levelA != null) barcode.setLevelA(levelA);
                if (levelB != null) barcode.setLevelB(levelB);
                if (levelC != null) barcode.setLevelC(levelC);
                barcode.setUnits(units);
                barcode.setTransactionTypeMaster(tranxType);
                String cName = mTranxPurChallan.getOutlet().getCompanyName();
                String firstLetter = cName.substring(0, 3);
                GenerateDates generateDates = new GenerateDates();
                String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
                Long lastRecord = barcodeRepository.findLastRecord(mTranxPurChallan.getOutlet().getId());
                if (lastRecord != null) {
                    String serailNo = String.format("%05d", lastRecord + 1);// 5 digit serial number
                    String bCode = firstLetter + currentMonth + serailNo;
                    barcode.setBarcodeUniqueCode(bCode);
                }
                barcode.setBatchNo(object.get("b_no").getAsString());
                if (productBatchNo != null) barcode.setProductBatch(productBatchNo);
                barcode.setTransactionId(mTranxPurChallan.getId());
                barcodeRepository.save(barcode);

                /**** end of Barcode ****/
                try {
                   /* if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                        flag = true;
                    }*/
                    /**** Inventory Postings *****/
                    if (mProduct.getIsInventory() && flag) {
                        /***** new architecture of Inventory Postings *****/
                        inventoryCommonPostings.callToInventoryPostings("CR", mTranxPurChallan.getInvoiceDate(), mTranxPurChallan.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mTranxPurChallan.getFiscalYear(), serialNo);
                        /***** End of new architecture of Inventory Postings *****/

                        /**
                         * @implNote New Logic of opening and closing Inventory posting
                         * @auther ashwins@opethic.com
                         * @version sprint 1
                         **/
                        closingUtility.stockPosting(outlet, branch, mTranxPurChallan.getFiscalYear().getId(), batchId,
                                mProduct, tranxType.getId(), mTranxPurChallan.getInvoiceDate(), tranxQty, free_qty,
                                mTranxPurChallan.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                mTranxPurChallan.getTranxCode(), userId, "IN", mProduct.getPackingMaster().getId());
                        closingUtility.stockPostingBatchWise(outlet, branch, mTranxPurChallan.getFiscalYear().getId(), batchId,
                                mProduct, tranxType.getId(), mTranxPurChallan.getInvoiceDate(), tranxQty, free_qty,
                                mTranxPurChallan.getId(), units.getId(), levelAId, levelBId, levelCId, productBatchNo,
                                mTranxPurChallan.getTranxCode(), userId, "IN", mProduct.getPackingMaster().getId());
                        /***** End of new logic of Inventory Postings *****/
                    }
                } catch (Exception e) {
                    System.out.println("Exception in Postings of Inventory:" + e.getMessage());
                } /* End of inserting into TranxPurchaseInvoiceDetailsUnits */

                /* closing of purchase orders while converting into purchase challan using its qnt */
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
            }
        } catch (Exception e) {
            purChallanLogger.error("Exception in saveIntoPOChallanInvoiceDetails: " + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /****** save into Purchase Challan Additional Charges ******/
    public void saveIntoPurchaseAdditionalCharges(JsonArray additionalCharges, TranxPurChallan mPurchaseTranx, Long tranxId, Long outletId) {
        List<TranxPurChallanAdditionalCharges> chargesList = new ArrayList<>();
        if (mPurchaseTranx.getAdditionalChargesTotal() > 0) {
            for (JsonElement mList : additionalCharges) {
                TranxPurChallanAdditionalCharges charges = new TranxPurChallanAdditionalCharges();
                JsonObject object = mList.getAsJsonObject();
                Double amount = object.get("amt").getAsDouble();
                Long ledgerId = object.get("ledgerId").getAsLong();
                LedgerMaster addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges.setAmount(amount);
                charges.setAdditionalCharges(addcharges);
                charges.setTranxPurChallan(mPurchaseTranx);
                charges.setStatus(true);
                charges.setPercent(object.get("percent").getAsDouble());
                charges.setOperation("inserted");
                charges.setCreatedBy(mPurchaseTranx.getCreatedBy());
                chargesList.add(charges);
            }
        }
        try {
            tranxPurChallanAddChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            //e1.printStackTrace();
            purChallanLogger.error("Error in saveIntoPurchaseAdditionalCharges" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            //e1.printStackTrace();
            purChallanLogger.error("Error in saveIntoPurchaseAdditionalCharges" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /****** End -> into Purchase Challan Additional Charges ******/

    /* list of Purchase challans outletwise*/
    public JsonObject poChallanInvoiceList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurChallan> tranxPurChallans = new ArrayList<>();
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
                tranxPurChallans = tranxPurChallanRepository.findPurchaseChallanListWithDate(users.getOutlet().getId(), users.getBranch().getId(), startDatep, endDatep, true);
            } else {
                tranxPurChallans = tranxPurChallanRepository.findPurchaseChallanListWithDateNoBr(users.getOutlet().getId(), startDatep, endDatep, true);
            }
        } else {
            if (users.getBranch() != null) {
                tranxPurChallans = tranxPurChallanRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
            } else {
                tranxPurChallans = tranxPurChallanRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
            }
        }

        for (TranxPurChallan invoices : tranxPurChallans) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getVendorInvoiceNo());
            response.addProperty("invoice_date", DateConvertUtil.convertDateToLocalDate(
                    invoices.getInvoiceDate()).toString());
            response.addProperty("transaction_date", invoices.getTransactionDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_creditor_name", invoices.getSundryCreditors().getLedgerName());
            response.addProperty("sundry_creditor_id", invoices.getSundryCreditors().getId());
            response.addProperty("supplier_code", invoices.getSundryCreditors().getLedgerCode());
            response.addProperty("narration", invoices.getNarration());
            response.addProperty("purchase_account_name", invoices.getPurchaseAccountLedger().getLedgerName());
            response.addProperty("purchase_challan_status", invoices.getTransactionStatus().getStatusName());
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

    //Start of Purchase challan list with pagination
    public Object poChallanInvoiceList(@RequestBody Map<String, String> request, HttpServletRequest req) {
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
        List<TranxPurChallan> purchaseChallan = new ArrayList<>();
        List<TranxPurChallan> purchaseArrayList = new ArrayList<>();
        List<PurChallanDTO> purChallanDTOList = new ArrayList<>();
        Map<String, String[]> paramMap = req.getParameterMap();
        String jsonToStr = "";
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_purchase_challan_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

            if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
                query += "  AND transaction_date BETWEEN '" + startDate + "' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND narration LIKE '%" + searchText + "%'";
            }
            if (paramMap.containsKey("sort"))

                jsonToStr = request.get("sort");


            if (!jsonToStr.isEmpty()) {
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
                }
            } else {
                query = query + " ORDER BY id DESC";
            }
            String query1 = query;       //we get all lists in this list
            System.out.println("query== " + query);

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            Query q = entityManager.createNativeQuery(query, TranxPurChallan.class);
            System.out.println("q ==" + q + "  purchaseChallan " + purchaseChallan);
            purchaseChallan = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxPurChallan.class);

            purchaseArrayList = q1.getResultList();
            System.out.println("Limit total rows " + purchaseArrayList.size());
            Integer total_pages = (purchaseArrayList.size() / pageSize);
            if ((purchaseArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (TranxPurChallan invoiceListView : purchaseChallan) {
                purChallanDTOList.add(convertToDTDTO(invoiceListView));
            }

            List<TranxPurChallan> salesInList = new ArrayList<>();
            salesInList = q1.getResultList();
            System.out.println("total rows " + salesInList.size());
            GenericDatatable<PurChallanDTO> data = new GenericDatatable<>(purChallanDTOList, purchaseArrayList.size(), pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(purChallanDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }
    //End of Purchase challan list with pagination

    //Start of DTO for purchase challan list
    private PurChallanDTO convertToDTDTO(TranxPurChallan purChallan) {
        PurChallanDTO purChallanDTO = new PurChallanDTO();
        purChallanDTO.setId(purChallan.getId());
        purChallanDTO.setInvoice_no(purChallan.getVendorInvoiceNo());
        purChallanDTO.setInvoice_date(DateConvertUtil.convertDateToLocalDate(purChallan.getInvoiceDate()).toString());
        purChallanDTO.setTransaction_date(purChallan.getTransactionDate().toString());
        purChallanDTO.setTotal_amount(purChallan.getTotalAmount());
        purChallanDTO.setSundry_creditor_name(purChallan.getSundryCreditors().getLedgerName());
        purChallanDTO.setSundry_creditor_id(purChallan.getSundryCreditors().getId());
        purChallanDTO.setSupplier_code(purChallan.getSundryCreditors().getLedgerCode());
        purChallanDTO.setNarration(purChallan.getNarration());
        purChallanDTO.setPurchase_account_name(purChallan.getPurchaseAccountLedger().getLedgerName());
        purChallanDTO.setPurchase_challan_status(purChallan.getTransactionStatus().getStatusName());
        purChallanDTO.setTax_amt(purChallan.getTotalTax() != null ? purChallan.getTotalTax() : 0.0);
        purChallanDTO.setTaxable_amt(purChallan.getTotalBaseAmount());
        purChallanDTO.setTransactionTrackingNo(purChallan.getTransactionTrackingNo());
        purChallanDTO.setTranxCode(purChallan.getTranxCode());
        String idList[];
        String referenceNo = "";
        purChallanDTO.setReferenceType(purChallan.getReference());
        if (purChallan.getReference() != null) {
            if (purChallan.getReference().equalsIgnoreCase("PRSORD")) {
                idList = purChallan.getOrderReference().split(",");
                for (int i = 0; i < idList.length; i++) {
                    TranxPurOrder tranxPurOrder = tranxPurOrderRepository.findByIdAndStatus(Long.parseLong(idList[i]), true);
                    if (tranxPurOrder != null) {
                        referenceNo = referenceNo + tranxPurOrder.getVendorInvoiceNo();
                        if (i < idList.length - 1) referenceNo = referenceNo + ",";
                    }
                }
                purChallanDTO.setReferenceNo(referenceNo);
            }
            purChallanDTO.setReferenceNo("");
        } else {
            purChallanDTO.setReferenceNo("");
        }
        return purChallanDTO;
    }
    //End of DTO for purchase challan list

    public JsonObject getPOChallanInvoiceWithIds(HttpServletRequest request) {
        JsonObject output = new JsonObject();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSCHN");
        List<TranxPurChallanAdditionalCharges> additionalCharges = new ArrayList<>();
        String str = request.getParameter("po_challan_ids");
        JsonParser parser = new JsonParser();
        JsonElement purDetailsJson = parser.parse(str);
        JsonArray jsonArray = purDetailsJson.getAsJsonArray();
        JsonArray row = new JsonArray();
        JsonArray jsonAdditionalList = new JsonArray();
        JsonObject invoiceData = new JsonObject();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            /* getting Units of Purchase Challan */
            JsonArray unitsJsonArray = new JsonArray();
            List<TranxPurChallanDetailsUnits> unitsArray = tranxPurChallanDetailsUnitRepository.findByTranxPurChallanIdAndTransactionStatusAndStatus(object.get("id").getAsLong(), 1L, true);
            for (TranxPurChallanDetailsUnits mUnits : unitsArray) {
                JsonObject unitsJsonObjects = new JsonObject();
                unitsJsonObjects.addProperty("details_id", mUnits.getId());
                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ? mUnits.getLevelA().getId().toString() : "");
                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ? mUnits.getLevelB().getId().toString() : "");
                unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ? mUnits.getProduct().getPackingMaster().getPackName() : "");
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
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1());
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt());
                unitsJsonObjects.addProperty("reference_id", mUnits.getTranxPurChallan().getId());
                unitsJsonObjects.addProperty("reference_type", tranxType.getTransactionCode());
                unitsJsonObjects.addProperty("add_chg_amt", mUnits.getAdditionChargesAmt());

                if (mUnits.getProductBatchNo() != null) {
                    if (mUnits.getProductBatchNo().getExpiryDate() != null) {
                        LocalDate invoiceDate = DateConvertUtil.convertDateToLocalDate(mUnits.getTranxPurChallan().getInvoiceDate());
                        if (invoiceDate.isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
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
                    unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA() != null ? mUnits.getProductBatchNo().getMinRateA() : 0);
                    unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB() != null ? mUnits.getProductBatchNo().getMinRateB() : 0);
                    unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC() != null ? mUnits.getProductBatchNo().getMinRateC() : 0);
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
                invoiceData.addProperty("id", mUnits.getTranxPurChallan().getId());
                invoiceData.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(mUnits.getTranxPurChallan().getInvoiceDate()).toString());
                invoiceData.addProperty("purchase_order", mUnits.getTranxPurChallan().getVendorInvoiceNo());
                invoiceData.addProperty("purchase_id", mUnits.getTranxPurChallan().getPurchaseAccountLedger().getId());
                invoiceData.addProperty("purchase_name", mUnits.getTranxPurChallan().getPurchaseAccountLedger().getLedgerName());
                invoiceData.addProperty("po_sr_no", mUnits.getTranxPurChallan().getId());
                invoiceData.addProperty("po_date", mUnits.getTranxPurChallan().getTransactionDate().toString());
                invoiceData.addProperty("transport_name", mUnits.getTranxPurChallan().getTransportName());
                invoiceData.addProperty("reference", mUnits.getTranxPurChallan().getReference());
                invoiceData.addProperty("supplier_id", mUnits.getTranxPurChallan().getSundryCreditors().getId());
                invoiceData.addProperty("supplier_name", mUnits.getTranxPurChallan().getSundryCreditors().getLedgerName());
                invoiceData.addProperty("gstNo", mUnits.getTranxPurChallan().getGstNumber());
                invoiceData.addProperty("narration", mUnits.getTranxPurChallan().getNarration());
            }

            additionalCharges = tranxPurChallanAddChargesRepository.findByTranxPurChallanIdAndStatus(object.get("id").getAsLong(), true);

            if (additionalCharges.size() > 0) {
                for (TranxPurChallanAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledgerId", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    json_charges.addProperty("percent", mAdditionalCharges.getPercent());
                    System.out.println("Inv Id:" + object.get("id").getAsLong());
                    System.out.println("Add.id:" + mAdditionalCharges.getId());
                    jsonAdditionalList.add(json_charges);
                }
            }
        }
//        additionalCharges = tranxPurChallanAddChargesRepository.findByTranxPurChallanIdAndStatus(mUnits.getTranxPurChallan().getId(), true);
        output.addProperty("discountLedgerId", 0);
        output.addProperty("discountInAmt", 0);
        output.addProperty("discountInPer", 0);
        output.addProperty("totalPurchaseDiscountAmt", 0);
        output.add("additional_charges", jsonAdditionalList);
        output.addProperty("reference_type", tranxType.getTransactionCode());
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("invoice_data", invoiceData);
        output.add("row", row);
        return output;
    }

    public JsonObject purchaseChallanDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxPurChallan invoiceTranx = tranxPurChallanRepository.findByIdAndStatusAndTransactionStatusId(Long.parseLong(request.getParameter("id")), true, 1L);

        TransactionTypeMaster purTranx = tranxRepository.findByTransactionCodeIgnoreCase("PRSCHN");
        try {
            if (invoiceTranx != null) {
                invoiceTranx.setStatus(false);
                invoiceTranx.setOperations("deletion");
                /**** Reverse Postings of Inventory for Purchase Challan ****/
                List<TranxPurChallanDetailsUnits> unitsList = new ArrayList<>();
                unitsList = tranxPurChallanDetailsUnitRepository.findByTranxPurChallanIdAndStatus(invoiceTranx.getId(), true);
                for (TranxPurChallanDetailsUnits mUnitObjects : unitsList) {
                    /***** new architecture of Inventory Postings *****/
                    inventoryCommonPostings.callToInventoryPostings("DR", invoiceTranx.getInvoiceDate(),
                            invoiceTranx.getId(), mUnitObjects.getQty(), invoiceTranx.getBranch(),
                            invoiceTranx.getOutlet(), mUnitObjects.getProduct(), purTranx, null, null,
                            null, mUnitObjects.getUnits(), mUnitObjects.getProductBatchNo(),
                            mUnitObjects.getProductBatchNo() != null ?
                                    mUnitObjects.getProductBatchNo().getBatchNo() : null, invoiceTranx.getFiscalYear(), null);
                    /***** End of new architecture of Inventory Postings *****/
                }
                tranxPurChallanRepository.save(invoiceTranx);

                jsonObject.addProperty("message", "Purchase challan deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Can't delete closed purchase challan");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }
        } catch (Exception e) {
            purChallanLogger.error("Error in purchaseDelete()->" + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject getChallanRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxPurChallanRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxPurChallanRepository.findLastRecord(users.getOutlet().getId());
        }

        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        /*String companyName = users.getOutlet().getCompanyName();
        companyName = companyName.substring(0, 3);*/ // fetching first 3 digits from company names
        /* getting Start and End year from fiscal Year */

        //first 3 digits of Current month
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
       /* String pcCode = companyName.toUpperCase() + "-" + startYear + endYear
                + "-" + "PC" + currentMonth + "-" + serailNo;*/
        String pcCode = "PC" + currentMonth + serailNo;

        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("serialNo", pcCode);
        return result;
    }

    /* Pending Purchase challan  */
    public JsonObject pCPendingOrder(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        LedgerMaster sundryCreditors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), true);
        List<TranxPurChallan> tranxPurChallan = tranxPurChallanRepository.findBySundryCreditorsIdAndStatusAndTransactionStatusId(sundryCreditors.getId(), true, 1L);
        for (TranxPurChallan invoices : tranxPurChallan) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("invoice_no", invoices.getVendorInvoiceNo());
            LocalDate invoiceDt = DateConvertUtil.convertDateToLocalDate(invoices.getInvoiceDate());
            response.addProperty("invoice_date", invoiceDt.toString());
            response.addProperty("transaction_date", invoices.getTransactionDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_creditor_name", invoices.getSundryCreditors().getLedgerName());
            response.addProperty("sundry_creditor_id", invoices.getSundryCreditors().getId());
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

    /* Purchase Challans Count of last Records */
    public JsonObject pcLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long count = tranxPurChallanRepository.findLastRecord(users.getOutlet().getId());
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        return result;


    }

    public JsonObject getChallan(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurChallanDetails> list = new ArrayList<>();
        List<TranxPurchaseChallanProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxPurChallanAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxPurChallan purChallan = tranxPurChallanRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            list = tranxPurChallanDetailsRepository.findByTranxPurChallanIdAndStatus(id, true);
            finalResult.addProperty("narration", purChallan.getNarration() != null ? purChallan.getNarration() : "");
            finalResult.addProperty("discountLedgerId", purChallan.getPurchaseDiscountLedger() != null ? purChallan.getPurchaseDiscountLedger().getId() : 0);
            finalResult.addProperty("discountInAmt", purChallan.getPurchaseDiscountAmount());
            finalResult.addProperty("discountInPer", purChallan.getPurchaseDiscountPer());
            finalResult.addProperty("totalPurchaseDiscountAmt", purChallan.getTotalPurchaseDiscountAmt());

            JsonObject result = new JsonObject();
            /* Purchase Invoice Data */
            result.addProperty("id", purChallan.getId());
            LocalDate invDate = DateConvertUtil.convertDateToLocalDate(purChallan.getInvoiceDate());
            result.addProperty("invoice_dt", invDate.toString());
            result.addProperty("purchase_order", purChallan.getVendorInvoiceNo());
            result.addProperty("purchase_id", purChallan.getPurchaseAccountLedger().getId());
            result.addProperty("po_sr_no", purChallan.getId());
            result.addProperty("po_date", purChallan.getTransactionDate().toString());
            result.addProperty("transport_name", purChallan.getTransportName());
            result.addProperty("reference", purChallan.getReference());
            result.addProperty("supplier_id", purChallan.getSundryCreditors().getId());
            result.addProperty("supplier_name", purChallan.getSundryCreditors().getLedgerName());
            result.addProperty("additional_charges_total", purChallan.getAdditionalChargesTotal());

            /* End of Purchase ORDER Data */

            /* Purchase ORDER Details */
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxPurChallanDetails mDetails : list) {
                    JsonObject prDetails = new JsonObject();
                    prDetails.addProperty("details_id", mDetails.getId());
                    prDetails.addProperty("product_id", mDetails.getProduct().getId());
                    /* getting Units of Purchase Orders */
                    List<TranxPurChallanDetailsUnits> unitDetails = tranxPurChallanDetailsUnitRepository.findByTranxPurChallanDetailsIdAndStatus(mDetails.getId(), true);
                    JsonArray productDetails = new JsonArray();
                    unitDetails.forEach(mUnit -> {
                        JsonObject mObject = new JsonObject();
                        JsonObject mUnitsObj = new JsonObject();
                        /*if (mUnit.getPackingMaster() != null) {
                            JsonObject package_obj = new JsonObject();
                            package_obj.addProperty("id", mUnit.getPackingMaster().getId());
                            package_obj.addProperty("pack_name", mUnit.getPackingMaster().getPackName());
                            package_obj.addProperty("label", mUnit.getPackingMaster().getPackName());
                            package_obj.addProperty("value", mUnit.getPackingMaster().getId());
                            mObject.add("package_id", package_obj);
                        } else {
                            mObject.addProperty("package_id", "");
                        }*/
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
                       /* mUnitsObj.addProperty("units_id", mUnit.getUnits().getId());
                        mUnitsObj.addProperty("value", mUnit.getUnits().getId());
                        mUnitsObj.addProperty("label", mUnit.getUnits().getUnitName());
                        mUnitsObj.addProperty("unit_name", mUnit.getUnits().getUnitName());
                        mObject.add("unitId", mUnitsObj);*/
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
            /* Purchase Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            if (additionalCharges.size() > 0) {
                for (TranxPurChallanAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledger_id", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    jsonAdditionalList.add(json_charges);
                }
            }
            /* End of Purchase Order Details */
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);
            finalResult.add("additional_charges", jsonAdditionalList);

        } catch (DataIntegrityViolationException e) {
            purChallanLogger.error("Error in getChallan :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            purChallanLogger.error("Error in getChallan :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public Object editPurchaseChallan(HttpServletRequest request) {
        TranxPurChallan mPurchaseTranx = null;
        ResponseMessage responseMessage = new ResponseMessage();
        mPurchaseTranx = saveIntoPCEdit(request);
        if (mPurchaseTranx != null) {
            //insertIntoLedgerTranxDetails(mPurchaseTranx);
            responseMessage.setMessage("Purchase Challan updated successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Error in purchase order updatation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    public TranxPurChallan saveIntoPCEdit(HttpServletRequest request) {
        TranxPurChallan mPurchaseTranx = null;
        TransactionTypeMaster tranxType = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        TranxPurChallan tranxPurChallan = new TranxPurChallan();
        tranxPurChallan = tranxPurChallanRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("id")), users.getOutlet().getId(), true);
        if (users.getBranch() != null) {
            branch = users.getBranch();
            tranxPurChallan.setBranch(branch);
        }
        tranxPurChallan.setOutlet(outlet);
        tranxType = tranxRepository.findByTransactionCodeIgnoreCase("PRSCHN");

        LocalDate date = LocalDate.parse(request.getParameter("invoice_date"));
        LocalDate invoiceDate = DateConvertUtil.convertStringToLocalDate(request.getParameter("invoice_date"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("invoice_date"));
        if (invoiceDate.isEqual(DateConvertUtil.convertDateToLocalDate(tranxPurChallan.getInvoiceDate()))) {
            strDt = tranxPurChallan.getInvoiceDate();
        }
        tranxPurChallan.setInvoiceDate(strDt);


        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            tranxPurChallan.setFiscalYear(fiscalYear);
            tranxPurChallan.setFinancialYear(fiscalYear.getFiscalYear());
        }
        /* End of fiscal year mapping */
        if (paramMap.containsKey("transport_name"))
            tranxPurChallan.setTransportName(request.getParameter("transport_name"));
        else tranxPurChallan.setTransportName("NA");
        if (paramMap.containsKey("reference")) tranxPurChallan.setReference(request.getParameter("reference"));
        else tranxPurChallan.setReference("");
        tranxPurChallan.setVendorInvoiceNo(request.getParameter("invoice_no"));
        tranxPurChallan.setPurChallanSrno(Long.parseLong(request.getParameter("purchase_sr_no")));
        LedgerMaster purchaseAccount = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("purchase_id")), true);
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                tranxPurChallan.setGstNumber(request.getParameter("gstNo"));
            }
        }
        LedgerMaster discountLedger = null;
        if (users.getBranch() == null)
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("purchase discount", users.getOutlet().getId(), true);
        else
            discountLedger = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("purchase discount", users.getOutlet().getId(), users.getBranch().getId(), true);
        if (discountLedger != null) {
            tranxPurChallan.setPurchaseDiscountLedger(discountLedger);
        }
        LedgerMaster sundryCreditors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), true);
        tranxPurChallan.setPurchaseAccountLedger(purchaseAccount);
        tranxPurChallan.setSundryCreditors(sundryCreditors);
        LocalDate mDate = LocalDate.parse(request.getParameter("transaction_date"));
        tranxPurChallan.setTransactionDate(mDate);

        tranxPurChallan.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        tranxPurChallan.setPurchaseRoundOff(roundoff);
        tranxPurChallan.setTotalAmount(Double.parseDouble(request.getParameter("bill_amount")));
        tranxPurChallan.setPurchaseDiscountPer(Double.parseDouble(request.getParameter("purchase_discount")));
        tranxPurChallan.setPurchaseDiscountAmount(Double.parseDouble(request.getParameter("purchase_discount_amt")));
        tranxPurChallan.setTotalPurchaseDiscountAmt(Double.parseDouble(request.getParameter("total_invoice_dis_amt")));
        tranxPurChallan.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
        if (taxFlag) {
            tranxPurChallan.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            tranxPurChallan.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            tranxPurChallan.setTotaligst(0.0);
        }
        /* if false : igst i.e inter state */
        else {
            tranxPurChallan.setTotalcgst(0.0);
            tranxPurChallan.setTotalsgst(0.0);
            tranxPurChallan.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }
        tranxPurChallan.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        tranxPurChallan.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        if (paramMap.containsKey("tcs")) tranxPurChallan.setTcs(Double.parseDouble(request.getParameter("tcs")));
        else {
            tranxPurChallan.setTcs(0.0);
        }
        tranxPurChallan.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        tranxPurChallan.setTotalTax(Double.valueOf(request.getParameter("total_tax_amt")));
        tranxPurChallan.setStatus(true);
        tranxPurChallan.setOperations("insertion");
        if (paramMap.containsKey("narration")) tranxPurChallan.setNarration(request.getParameter("narration"));
        else {
            tranxPurChallan.setNarration("NA");
        }
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                tranxPurChallan.setGstNumber(request.getParameter("gstNo"));
            }
        }
        tranxPurChallan.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_row_gross_amt"))); // RATE*QTY
        tranxPurChallan.setGrossAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        if (paramMap.containsKey("additionalChgLedger1")) {
            LedgerMaster additionalChgLedger1 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger1")), users.getOutlet().getId(), true);
            if (additionalChgLedger1 != null) {
                tranxPurChallan.setAdditionLedger1(additionalChgLedger1);
                tranxPurChallan.setAdditionLedgerAmt1(Double.valueOf(request.getParameter("addChgLedgerAmt1")));
            }
        }
        if (paramMap.containsKey("additionalChgLedger2")) {
            LedgerMaster additionalChgLedger2 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger2")), users.getOutlet().getId(), true);
            if (additionalChgLedger2 != null) {
                tranxPurChallan.setAdditionLedger2(additionalChgLedger2);
                tranxPurChallan.setAdditionLedgerAmt2(Double.valueOf(request.getParameter("addChgLedgerAmt2")));
            }
        }
        if (paramMap.containsKey("additionalChgLedger3")) {
            LedgerMaster additionalChgLedger3 = ledgerMasterRepository.findByIdAndOutletIdAndStatus(Long.parseLong(request.getParameter("additionalChgLedger3")), users.getOutlet().getId(), true);
            if (additionalChgLedger3 != null) {
                tranxPurChallan.setAdditionLedger3(additionalChgLedger3);
                tranxPurChallan.setAdditionLedgerAmt3(Double.valueOf(request.getParameter("addChgLedgerAmt3")));
            }
        }
        if (paramMap.containsKey("isRoundOffCheck"))
            tranxPurChallan.setIsRoundOff(Boolean.parseBoolean(request.getParameter("isRoundOffCheck")));
        if (paramMap.containsKey("roundoff"))
            tranxPurChallan.setRoundOff(Double.valueOf(request.getParameter("roundoff")));
        try {
            mPurchaseTranx = tranxPurChallanRepository.save(tranxPurChallan);
            if (mPurchaseTranx != null) {
                /* Save into Duties and Taxes */
                String taxStr = request.getParameter("taxCalculation");
                // JsonObject duties_taxes = new JsonObject(taxStr);
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                saveIntoPurChallanDutiesTaxesEdit(duties_taxes, mPurchaseTranx, taxFlag, tranxType, users.getOutlet().getId(), users.getId());
                String jsonStr = request.getParameter("row");
                JsonParser parser = new JsonParser();
                JsonElement purDetailsJson = parser.parse(jsonStr);
                JsonArray array = purDetailsJson.getAsJsonArray();
                String rowsDeleted = "";
                if (paramMap.containsKey("rowDelDetailsIds")) {
                    rowsDeleted = request.getParameter("rowDelDetailsIds");
                }
                saveIntoPurChallanDetailsEdit(array, mPurchaseTranx, users.getBranch() != null ? users.getBranch() : null, outlet, users.getId(), rowsDeleted, tranxType);


                String acRowsDeleted = "";
                if (paramMap.containsKey("additionalCharges")) {
                    String strJson = request.getParameter("additionalCharges");
                    JsonElement purAddChargesJson = parser.parse(strJson);
                    JsonArray additionalCharges = purAddChargesJson.getAsJsonArray();
                    if (additionalCharges.size() > 0) {
                        saveIntoPurChallanAdditionalChargesEdit(additionalCharges, mPurchaseTranx, tranxType, users.getOutlet().getId(), acRowsDeleted);
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

            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purChallanLogger.error("Error in saveIntoPCEdit :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            purChallanLogger.error("Error in saveIntoPCEdit :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mPurchaseTranx;
    }


    private void saveIntoPurChallanAdditionalChargesEdit(JsonArray additionalCharges, TranxPurChallan mPurchaseTranx, TransactionTypeMaster tranxType, Long outletId, String acRowsDeleted) {


        List<TranxPurChallanAdditionalCharges> chargesList = new ArrayList<>();
        for (JsonElement mAddCharges : additionalCharges) {
            JsonObject object = mAddCharges.getAsJsonObject();
            Double amount = 0.0;
            Double percent = 0.0;
            Long detailsId = 0L;
            if (object.has("amt") && !object.get("amt").getAsString().equalsIgnoreCase("")) {
                amount = object.get("amt").getAsDouble();

                if (object.has("percent")) {
                    // Retrieve the value associated with the key "percent" and convert it to a double
                    percent = object.get("percent").getAsDouble();
                } else {
                    // If the value is null or the key doesn't exist, assign 0.0 to the variable percent
                    percent = 0.0;
                }

                //percent = object.get("percent").getAsDouble();
                Long ledgerId = object.get("ledgerId").getAsLong();
                if (object.has("additional_charges_details_id"))
                    detailsId = object.get("additional_charges_details_id").getAsLong();
                LedgerMaster addcharges = null;
                TranxPurChallanAdditionalCharges charges = null;
                charges = tranxPurChallanAddChargesRepository.findByAdditionalChargesIdAndTranxPurChallanIdAndStatus(ledgerId, mPurchaseTranx.getId(), true);
                if (detailsId == 0L) {
                    charges = new TranxPurChallanAdditionalCharges();
                }
                addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges.setAmount(amount);
                charges.setPercent(percent);
                charges.setAdditionalCharges(addcharges);
                charges.setTranxPurChallan(mPurchaseTranx);
                charges.setOperation("Update");
                charges.setStatus(true);

                chargesList.add(charges);
                tranxPurChallanAddChargesRepository.save(charges);
                Boolean isContains = dbList.contains(addcharges.getId());
                mInputList.add(addcharges.getId());
                if (isContains) {
                    //      transactionDetailsRepository.ledgerPostingEdit(addcharges.getId(), mPurchaseTranx.getId(), tranxTypeMater.getId(), "DR", amount * -1);
                    /**** New Postings Logic *****/
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(addcharges.getId(), tranxType.getId(), mPurchaseTranx.getId());
                    if (mLedger != null) {
                        mLedger.setAmount(amount);
                        mLedger.setTransactionDate(mPurchaseTranx.getInvoiceDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    /* insert */
                    /**** New Postings Logic *****/
                    ledgerCommonPostings.callToPostings(amount, addcharges, tranxType, addcharges.getAssociateGroups(), mPurchaseTranx.getFiscalYear(), mPurchaseTranx.getBranch(), mPurchaseTranx.getOutlet(), mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), mPurchaseTranx.getVendorInvoiceNo(), "DR", true, "Purchase Invoice", "Insert");
                }
            }
        }

    }


    public void delAddCharges(JsonArray deletedArrays) {

        TranxPurChallanAdditionalCharges mDeletedInvoices = null;
        for (JsonElement element : deletedArrays) {
            JsonObject deletedRowsId = element.getAsJsonObject();

            if (deletedRowsId.has("del_id")) {
                mDeletedInvoices = tranxPurChallanAddChargesRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                if (mDeletedInvoices != null) {
                    mDeletedInvoices.setStatus(false);
                    try {
                        tranxPurChallanAddChargesRepository.save(mDeletedInvoices);
                    } catch (DataIntegrityViolationException de) {
                        purChallanLogger.error("Error in saveInto Purchase Challan Add.Charges Edit" + de.getMessage());
                        de.printStackTrace();
                        System.out.println("Exception:" + de.getMessage());

                    } catch (Exception ex) {
                        purChallanLogger.error("Error in saveInto Purchase Challan Add.Charges Edit" + ex.getMessage());
                        ex.printStackTrace();
                        System.out.println("Exception save Into Purchase Challan Add.Charges Edit:" + ex.getMessage());
                    }
                }
            }

        }
    }


    private void saveIntoPurchaseAdditionalChargesEdit(JsonArray additionalCharges, TranxPurChallan mPurchaseTranx, Long tranxId, Long outletId) {
        List<TranxPurChallanAdditionalCharges> chargesList = new ArrayList<>();
        if (mPurchaseTranx.getAdditionalChargesTotal() > 0) {
            for (JsonElement mList : additionalCharges) {
                TranxPurChallanAdditionalCharges charges = null;
                JsonObject object = mList.getAsJsonObject();
                Double amount = object.get("amt").getAsDouble();
                Long ledgerId = object.get("ledger").getAsLong();
                LedgerMaster addcharges = ledgerMasterRepository.findByIdAndOutletIdAndStatus(ledgerId, outletId, true);
                charges = tranxPurChallanAddChargesRepository.findByAdditionalChargesIdAndTranxPurChallanIdAndStatus(ledgerId, mPurchaseTranx.getId(), true);
                if (charges == null) {
                    charges = new TranxPurChallanAdditionalCharges();
                }
                charges.setAmount(amount);
                charges.setAdditionalCharges(addcharges);
                charges.setTranxPurChallan(mPurchaseTranx);
                charges.setStatus(true);
                charges.setOperation("updated");
                charges.setCreatedBy(mPurchaseTranx.getCreatedBy());
                chargesList.add(charges);
            }
        } else {
            mPurchaseTranx.setAdditionalChargesTotal(0.0);
//            TranxPurChallanAdditionalCharges charges = tranxPurChallanAddChargesRepository.findByStatusAndTranxPurChallanId(
//                      true,mPurchaseTranx.getId());
        }
        try {
            //tranxPurChallanAddChargesRepository.saveAll(chargesList);
        } catch (DataIntegrityViolationException e1) {
            //e1.printStackTrace();
            purChallanLogger.error("Error in saveIntoPurchaseAdditionalCharges" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Exception e) {
            //e1.printStackTrace();
            purChallanLogger.error("Error in saveIntoPurchaseAdditionalCharges" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveIntoPurChallanDetailsEdit(JsonArray array, TranxPurChallan mPurchaseTranx, Branch branch, Outlet outlet, Long userId, String rowsDeleted, TransactionTypeMaster tranxType) {
        /* Purchase Invoice Unit Edit */

        for (JsonElement mList : array) {

            JsonObject object = mList.getAsJsonObject();
            Double tranxQty = 0.0;

            Long details_id = object.get("details_id").getAsLong();
            TranxPurChallanDetailsUnits invoiceUnits = new TranxPurChallanDetailsUnits();
            if (details_id != 0) {
                invoiceUnits = tranxPurChallanDetailsUnitRepository.findByIdAndStatus(details_id, true);
                tranxQty = invoiceUnits.getQty();
            } else {
                invoiceUnits.setTransactionStatus(1L);
                tranxQty = object.get("qty").getAsDouble();
            }
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            /* Purchase Challan Unit Edit */
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
            invoiceUnits.setTranxPurChallan(mPurchaseTranx);
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
            /******* Insert into Product Batch No *******/
            try {
                boolean flag = false;
                if (object.get("is_batch").getAsBoolean()) {
                    flag = true;
                    Double qty = object.get("qty").getAsDouble();
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
                    if (object.get("b_details_id").getAsLong() == 0) {
                        ProductBatchNo mproductBatchNo = new ProductBatchNo();
                        if (object.has("b_no")) mproductBatchNo.setBatchNo(object.get("b_no").getAsString());
                        if (object.has("b_rate")) mproductBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        if (object.has("b_purchase_rate"))
                            mproductBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase(""))
                            mproductBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));

                        mproductBatchNo.setQnty(qty.intValue());
                        mproductBatchNo.setFreeQty(free_qty);
                        mproductBatchNo.setSalesRate(object.get("sales_rate").getAsDouble());
                        mproductBatchNo.setCosting(costing);
                        mproductBatchNo.setCostingWithTax(costing_with_tax);
                        mproductBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
                        mproductBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
                        mproductBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
                        if (object.has("min_margin"))
                            mproductBatchNo.setMinMargin(object.get("min_margin").getAsDouble());
                        mproductBatchNo.setStatus(true);
                        mproductBatchNo.setProduct(mProduct);
                        mproductBatchNo.setOutlet(outlet);
                        mproductBatchNo.setBranch(branch);
                        if (levelA != null) mproductBatchNo.setLevelA(levelA);
                        if (levelB != null) mproductBatchNo.setLevelB(levelB);
                        if (levelC != null) mproductBatchNo.setLevelC(levelC);
                        mproductBatchNo.setUnits(units);
                        try {
                            productBatchNo = productBatchNoRepository.save(mproductBatchNo);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Exception " + e.getMessage());
                        }
                    } else {
                        productBatchNo = productBatchNoRepository.findByIdAndStatus(object.get("b_details_id").getAsLong(), true);
                        productBatchNo.setQnty(qty.intValue());
                        productBatchNo.setFreeQty(free_qty);
                        productBatchNo.setSalesRate(object.get("sales_rate")!=null && !object.get("sales_rate").equals("") ? object.get("sales_rate").getAsDouble():0.0);
                        productBatchNo.setCosting(costing);
                        productBatchNo.setCostingWithTax(costing_with_tax);
                        if (object.has("b_no")) productBatchNo.setBatchNo(object.get("b_no").getAsString());
                        if (object.has("b_rate")) productBatchNo.setMrp(object.get("b_rate").getAsDouble());
                        if (object.has("b_sale_rate"))
                            productBatchNo.setSalesRate(object.get("b_sale_rate").getAsDouble());
                        if (object.has("b_purchase_rate"))
                            productBatchNo.setPurchaseRate(object.get("b_purchase_rate").getAsDouble());
                        if (object.has("b_expiry") && !object.get("b_expiry").getAsString().equalsIgnoreCase("") && !object.get("b_expiry").getAsString().toLowerCase().equals("invalid"))
                            productBatchNo.setExpiryDate(LocalDate.parse(object.get("b_expiry").getAsString()));
                        if (object.has("manufacturing_date") && !object.get("manufacturing_date").getAsString().equalsIgnoreCase("") && !object.get("manufacturing_date").getAsString().toLowerCase().equals("invalid"))
                            productBatchNo.setManufacturingDate(LocalDate.parse(object.get("manufacturing_date").getAsString()));
//                        if (object.has("rate_a") && !object.get("rate_a").getAsString().equalsIgnoreCase(""))
//                            productBatchNo.setMinRateA(object.get("rate_a").getAsDouble());
//                        if (object.has("rate_b") && !object.get("rate_b").getAsString().equalsIgnoreCase(""))
//                            productBatchNo.setMinRateB(object.get("rate_b").getAsDouble());
//                        if (object.has("rate_c") && !object.get("rate_c").getAsString().equalsIgnoreCase(""))
//                            productBatchNo.setMinRateC(object.get("rate_c").getAsDouble());
//                        if (object.has("margin_per") && !object.get("margin_per").getAsString().equals(""))
//                            productBatchNo.setMinMargin(object.get("margin_per").getAsDouble());
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
                invoiceUnits.setProductBatchNo(productBatchNo);
                TranxPurChallanDetailsUnits mDetailsUnits = tranxPurChallanDetailsUnitRepository.save(invoiceUnits);
                if (flag == false) {

                    /******* Insert into Tranx Product Seial Numbers  ******/
                    JsonArray jsonArray = object.getAsJsonArray("serialNo");
                   /* if (jsonArray != null && jsonArray.size() > 0) {
                        List<TranxPurchaseChallanProductSrNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            String jsonSrno = jsonElement.getAsString();
                            serialNo = jsonSrno;
                            TranxPurchaseChallanProductSrNumber productSerialNumber = new TranxPurchaseChallanProductSrNumber();
                            productSerialNumber.setProduct(mProduct);
                            productSerialNumber.setSerialNo(jsonSrno);
                            productSerialNumber.setTranxPurChallan(mPurchaseTranx);
                            productSerialNumber.setTransactionStatus("Purchase");
                            productSerialNumber.setStatus(true);
                            productSerialNumber.setCreatedBy(userId);
                            productSerialNumber.setOperations("Inserted");
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setBranch(mPurchaseTranx.getBranch());
                            productSerialNumber.setOutlet(mPurchaseTranx.getOutlet());
                            productSerialNumber.setTransactionTypeMaster(tranxType);
                            productSerialNumber.setUnits(units);
                            productSerialNumber.setTranxPurChallanDetailsUnits(mDetailsUnits);
                            productSerialNumber.setLevelA(levelA);
                            productSerialNumber.setLevelB(levelB);
                            productSerialNumber.setLevelC(levelC);
                            productSerialNumber.setUnits(units);
                            TranxPurchaseChallanProductSrNumber mSerialNo =
                                    tranxPurchaseChallanProductSrNumberRepository.save(productSerialNumber);
                            if (mProduct.getIsInventory()) {
                                inventoryCommonPostings.callToInventoryPostings("CR",
                                        mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(),
                                        object.get("qty").getAsDouble(), branch, outlet, mProduct, tranxType,
                                        levelA, levelB, levelC, units, productBatchNo, batchNo,
                                        mPurchaseTranx.getFiscalYear(), serialNo);
                            }
                        }
                    }*/
                    if (jsonArray != null && jsonArray.size() > 0) {
                        List<TranxPurchaseInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonSrno = jsonElement.getAsJsonObject();
                            serialNo = jsonSrno.get("serial_no").getAsString();
                            Long detailsId = jsonSrno.get("serial_detail_id").getAsLong();
                            if (detailsId == 0) {
                                TranxPurchaseChallanProductSrNumber productSerialNumber = new TranxPurchaseChallanProductSrNumber();
                                productSerialNumber.setProduct(mProduct);
                                productSerialNumber.setSerialNo(serialNo);
                                // productSerialNumber.setTranxPurChallan(mPurchaseTranx);
                                productSerialNumber.setTransactionStatus("Purchase");
                                productSerialNumber.setStatus(true);
                                productSerialNumber.setCreatedBy(userId);
                                productSerialNumber.setOperations("Inserted");
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setBranch(mPurchaseTranx.getBranch());
                                productSerialNumber.setOutlet(mPurchaseTranx.getOutlet());
                                productSerialNumber.setTransactionTypeMaster(tranxType);
                                productSerialNumber.setUnits(units);
                                productSerialNumber.setTranxPurChallanDetailsUnits(mDetailsUnits);
                                productSerialNumber.setLevelA(levelA);
                                productSerialNumber.setLevelB(levelB);
                                productSerialNumber.setLevelC(levelC);
                                productSerialNumber.setUnits(units);
                                TranxPurchaseChallanProductSrNumber mSerialNo = tranxPurchaseChallanProductSrNumberRepository.save(productSerialNumber);
                                if (mProduct.getIsInventory()) {
                                    inventoryCommonPostings.callToInventoryPostings("CR", mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear(), serialNo);
                                }
                            } else {
                                TranxPurchaseChallanProductSrNumber productSerialNumber1 = tranxPurchaseChallanProductSrNumberRepository.findByIdAndStatus(detailsId, true);
                                productSerialNumber1.setSerialNo(serialNo);
                                productSerialNumber1.setUpdatedBy(userId);
                                productSerialNumber1.setOperations("Updated");
                                TranxPurchaseChallanProductSrNumber mSerialNo = tranxPurchaseChallanProductSrNumberRepository.save(productSerialNumber1);
                            }
                        }
                    }
                    flag = true;
                }
                /* Inserting into Barcode */
                ProductBarcode barcode = null;
                if (productBatchNo != null) {
                    barcode = barcodeRepository.findByProductIdAndOutletIdAndStatusAndTransactionIdAndTransactionTypeMasterIdAndProductBatchId(mProduct.getId(), outlet.getId(), true, mPurchaseTranx.getId(), tranxType.getId(), productBatchNo.getId());
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
                    if (object.has("companybarcode"))
                        barcode.setCompanyBarcode(object.get("companybarcode").getAsString());
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
                    if (object.has("companybarcode"))
                        barcode.setCompanyBarcode(object.get("companybarcode").getAsString());
                    barcode.setQnty(object.get("qty").getAsInt());
                    barcodeRepository.save(barcode);
                }
                /**** end of Barcode ****/


                try {
                   /* if (mProduct.getIsInventory() == false && mProduct.getIsBatchNumber() == false) {
                        flag = true;
                    }*/
                    /**** Inventory Postings *****/
                    if (mProduct.getIsInventory() && flag) {
                        if (details_id != 0) {
                            inventoryCommonPostings.callToEditInventoryPostings(mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear());
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
                            } else {
                                /***** new architecture of Inventory Postings *****/
                                inventoryCommonPostings.callToInventoryPostings("CR", mPurchaseTranx.getInvoiceDate(), mPurchaseTranx.getId(), object.get("qty").getAsDouble() + free_qty, branch, outlet, mProduct, tranxType, levelA, levelB, levelC, units, productBatchNo, batchNo, mPurchaseTranx.getFiscalYear(), serialNo);
                                /***** End of new architecture of Inventory Postings *****/
                            }

                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception in Postings of Inventory:" + e.getMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
                purChallanLogger.error("Exception in saveIntoPurchaseChallanDetails:" + e.getMessage());
            }
            /* end of Purchase Invoice Units Edit */
        }
        /* if product is deleted from details table from front end, when user edit the purchase */
        Long purchaseInvoiceId = null;
        HashSet<Long> purchaseDetailsId = new HashSet<>();
        if (!rowsDeleted.isEmpty()) {
            JsonParser parser = new JsonParser();
            JsonElement purDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = purDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxPurChallanDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    if (deletedArrays.size() > 0) {
                        if (deletedRowsId.has("del_id")) {
                            mDeletedInvoices = tranxPurChallanDetailsUnitRepository.findByIdAndStatus(deletedRowsId.get("del_id").getAsLong(), true);
                            if (mDeletedInvoices != null) {
                                mDeletedInvoices.setStatus(false);
                                // mDeletedInvoices.setOperations("removed");
                                try {
                                    tranxPurChallanDetailsUnitRepository.save(mDeletedInvoices);
                                    /***** inventory effects of deleted rows *****/
                                    inventoryCommonPostings.callToInventoryPostings("DR", mDeletedInvoices.getTranxPurChallan().getInvoiceDate(), mDeletedInvoices.getTranxPurChallan().getId(), mDeletedInvoices.getQty() + mDeletedInvoices.getFreeQty(), branch, outlet, mDeletedInvoices.getProduct(), tranxType, mDeletedInvoices.getLevelA(), mDeletedInvoices.getLevelB(), mDeletedInvoices.getLevelC(), mDeletedInvoices.getUnits(), mDeletedInvoices.getProductBatchNo(), mDeletedInvoices.getProductBatchNo().getBatchNo(), mDeletedInvoices.getTranxPurChallan().getFiscalYear(), null);
                                    /***** End of new architecture of Inventory Postings *****/
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    purChallanLogger.error("Error in saveIntoPurChallanDetailsEdit :->" + e.getMessage());
                                }

                            }
                        }
                    }
                }
            }
        }
        /**** remove all product details of invoice ****/
        Iterator<Long> itr = purchaseDetailsId.iterator();
        while (itr.hasNext()) {
            Long id = itr.next();
            System.out.println("Details Id:" + id);
            Integer count = null;
            Integer rowCount = null;
            rowCount = tranxPurChallanDetailsUnitRepository.findCount(purchaseInvoiceId, id);
            count = tranxPurChallanDetailsUnitRepository.findStatus(purchaseInvoiceId, id);
            if (count != null && rowCount != null && count == rowCount) {
                TranxPurChallanDetails details = tranxPurChallanDetailsRepository.findByIdAndStatus(id, true);
                details.setStatus(false);
                try {
                    tranxPurChallanDetailsRepository.save(details);
                } catch (Exception e) {
                    purChallanLogger.error("Purchase Challan : Exception in Delete functionality of Row Deletion" + e.getMessage());
                }
            }
        }
    }

    /* for Purchase Details Edit */

    private void saveIntoPurChallanDutiesTaxesEdit(JsonObject duties_taxes, TranxPurChallan invoiceTranx, Boolean taxFlag, TransactionTypeMaster tranxType, Long outletId, Long userId) {
        List<TranxPurChallanDutiesTaxes> purchaseDutiesTaxes = new ArrayList<>();
        List<Long> db_dutiesLedgerIds = tranxPurChallanDutiesTaxesRepository.findByDutiesAndTaxesId(invoiceTranx.getId());
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId, invoiceTranx.getBranch() != null ? invoiceTranx.getBranch().getId() : null);

        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            //Updation into Purchase challan Duties and Taxes
            if (db_dutiesLedgerIds.size() > 0) {
                //insert old records in history
                purchaseDutiesTaxes = tranxPurChallanDutiesTaxesRepository.findByTranxPurChallanIdAndStatus(invoiceTranx.getId(), true);
                //  insertIntoDutiesAndTaxesHistory(purchaseDutiesTaxes);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) cgstObject.get("gst").getAsDouble();
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
                            mInputList.add(dutiesTaxes.getId());

                        }
                        taxes.setAmount(amt);
                        taxes.setStatus(true);
                        taxes.setTranxPurChallan(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "INPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        //    dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
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
                        taxes.setTranxPurChallan(invoiceTranx);
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
                        TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
                        JsonObject igstObject = mIgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) igstObject.get("gst").getAsDouble();
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
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxPurChallan(invoiceTranx);
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
                        TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
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
                        taxes.setTranxPurChallan(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) sgstObject.get("gst").getAsDouble();
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
                        taxes.setTranxPurChallan(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
                        JsonObject igstObject = igstList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "INPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        //   dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (invoiceTranx.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(invoiceTranx.getOutlet().getId(), invoiceTranx.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(invoiceTranx.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setTranxPurChallan(invoiceTranx);
                        taxes.setSundryCreditors(invoiceTranx.getSundryCreditors());
                        taxes.setIntra(taxFlag);
                        purchaseDutiesTaxes.add(taxes);
                    }
                }
            }
        }
        tranxPurChallanDutiesTaxesRepository.saveAll(purchaseDutiesTaxes);
    }

    private void insertIntoDutiesAndTaxesHistory(List<TranxPurChallanDutiesTaxes> purchaseDutiesTaxes) {
        for (TranxPurChallanDutiesTaxes mList : purchaseDutiesTaxes) {
            mList.setStatus(false);
            tranxPurChallanDutiesTaxesRepository.save(mList);
        }
    }

    /* private List<Long> getInputLedgerIds(Boolean taxFlag, JsonObject duties_taxes, Long outletId) {
         List<Long> returnLedgerIds = new ArrayList<>();
         if (taxFlag) {
             JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
             JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
             *//* this is for Cgst creation *//*
            if (cgstList.size() > 0) {
                for (JsonElement mCgst : cgstList) {
                    TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
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
                    TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
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
                    TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
                    JsonObject cgstObject = mCgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) cgstObject.get("gst").getAsDouble();
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
                    TranxPurChallanDutiesTaxes taxes = new TranxPurChallanDutiesTaxes();
                    JsonObject sgstObject = mSgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //  int inputGst = (int) sgstObject.get("gst").getAsDouble();
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
                    // int inputGst = (int) igstObject.get("gst").getAsDouble();
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

    public JsonObject getProductEditByIdByFPU(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxPurChallan invoiceTranx = tranxPurChallanRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        productIds = tranxPurChallanDetailsUnitRepository.findByTranxPurId(invoiceTranx.getId(), true);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getInvoiceDate(), productIds);
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getProductEditByIdsByFPU(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String str = request.getParameter("po_challan_ids");
        JsonParser parser = new JsonParser();
        JsonElement purDetailsJson = parser.parse(str);
        JsonArray jsonArray = purDetailsJson.getAsJsonArray();
        JsonArray productArray = new JsonArray();
        JsonObject output = new JsonObject();
        JsonObject result = new JsonObject();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            TranxPurChallan invoiceTranx = tranxPurChallanRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
            List<Object[]> productIds = new ArrayList<>();
            productIds = tranxPurChallanDetailsUnitRepository.findByTranxPurId(invoiceTranx.getId(), true);
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
                        List<Long> levelCunits = productUnitRepository.findByProductsLevelC(mProduct.getId(), mLeveA, mLeveB);
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
                            List<Object[]> unitList = productUnitRepository.findUniqueUnitsByProductId(mProduct.getId(), mLeveA, mLeveB, mLeveC);
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
                                    List<ProductBatchNo> batchNos = productBatchNoRepository.findByUniqueBatchProductIdAndStatus(mProduct.getId(), levelAId, levelBId, levelCId, unitId, true);
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
                                            batchObject.addProperty("costing_with_tax", mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.00);
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
                                                salesRateWithTax = mBatch.getSalesRate() + ((mBatch.getSalesRate() * mBatch.getProduct().getTaxMaster().getIgst()) / 100);
                                            batchObject.addProperty("sales_rate_with_tax", salesRateWithTax);
                                            if (mBatch.getExpiryDate() != null) {
                                                LocalDate invoiceDate = DateConvertUtil.convertDateToLocalDate(invoiceTranx.getInvoiceDate());
                                                if (invoiceDate.isAfter(mBatch.getExpiryDate())) {
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
            // productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getInvoiceDate(), productIds);

        }
//        result.add("invoice_list", productArray);
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);

        return output;
    }

    public JsonObject getPurchaseChallanByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurChallanDetails> list = new ArrayList<>();
        JsonArray units = new JsonArray();
        List<TranxPurchaseChallanProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxPurChallanAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            TranxPurChallan purchaseInvoice = tranxPurChallanRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            finalResult.addProperty("tcs", purchaseInvoice.getTcs());
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
            /*  Purchase Challan Data*/
            result.addProperty("id", purchaseInvoice.getId());
            LocalDate invoiceDate = DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getInvoiceDate());
            result.addProperty("invoice_dt", invoiceDate.toString());
            result.addProperty("invoice_no", purchaseInvoice.getVendorInvoiceNo());
            result.addProperty("tranx_unique_code", purchaseInvoice.getTranxCode());
            result.addProperty("purchase_sr_no", purchaseInvoice.getPurChallanSrno());
            result.addProperty("purchase_account_ledger_id", purchaseInvoice.getPurchaseAccountLedger().getId());
            result.addProperty("supplierId", purchaseInvoice.getSundryCreditors().getId());
            result.addProperty("supplierName", purchaseInvoice.getSundryCreditors().getLedgerName());
            result.addProperty("transaction_dt", purchaseInvoice.getTransactionDate().toString());
            result.addProperty("additional_charges_total", purchaseInvoice.getAdditionalChargesTotal());
            result.addProperty("gstNo", purchaseInvoice.getGstNumber() != null ? purchaseInvoice.getGstNumber() : "");
            result.addProperty("isRoundOffCheck", purchaseInvoice.getIsRoundOff());
            result.addProperty("roundoff", purchaseInvoice.getRoundOff());
            result.addProperty("source", "pur_challan");
            result.addProperty("ledgerStateCode", purchaseInvoice.getSundryCreditors().getStateCode());
            /*  End of Purchase Challan Data*/

            /*   Purchase Challan Details*/
            JsonArray row = new JsonArray();
            JsonArray unitsJsonArray = new JsonArray();
            List<TranxPurChallanDetailsUnits> unitsArray = tranxPurChallanDetailsUnitRepository.findByTranxPurChallanIdAndTransactionStatusAndStatus(purchaseInvoice.getId(), 1L, true);
            for (TranxPurChallanDetailsUnits mUnits : unitsArray) {
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
//                LocalDate invoiceDt = DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getInvoiceDate());
                if (mUnits.getProductBatchNo() != null) {
                    if (mUnits.getProductBatchNo().getExpiryDate() != null) {
                        if (DateConvertUtil.convertDateToLocalDate(purchaseInvoice.getInvoiceDate()).isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
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
                    unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA() != null ? mUnits.getProductBatchNo().getMinRateA() : 0);
                    unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB() != null ? mUnits.getProductBatchNo().getMinRateB() : 0);
                    unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC() != null ? mUnits.getProductBatchNo().getMinRateC() : 0);
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
            /*       End of Purchase Challan Details*/
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
            /* Purchase Additional Charges */
            JsonArray jsonAdditionalList = new JsonArray();
            additionalCharges = tranxPurChallanAddChargesRepository.findByTranxPurChallanIdAndStatus(purchaseInvoice.getId(), true);
            if (additionalCharges.size() > 0) {
                for (TranxPurChallanAdditionalCharges mAdditionalCharges : additionalCharges) {
                    JsonObject json_charges = new JsonObject();
                    json_charges.addProperty("additional_charges_details_id", mAdditionalCharges.getId());
                    json_charges.addProperty("ledgerId", mAdditionalCharges.getAdditionalCharges() != null ? mAdditionalCharges.getAdditionalCharges().getId() : 0);
                    json_charges.addProperty("amt", mAdditionalCharges.getAmount());
                    json_charges.addProperty("percent", mAdditionalCharges.getPercent());
                    jsonAdditionalList.add(json_charges);
                }
            }
            List<LedgerGstDetails> gstDetails = new ArrayList<>();
            gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(purchaseInvoice.getSundryCreditors().getId(), true);
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


            /*   End of Purchase Additional Charges*/
            finalResult.add("barcode_list", barcodeJsonList);
            finalResult.add("additionalCharges", jsonAdditionalList);
            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("invoice_data", result);
            finalResult.add("row", row);


        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            purChallanLogger.error("Error in getPurchaseInvoiceById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            purChallanLogger.error("Error in getPurchaseInvoiceById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public Object validatePurchaseChallan(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurChallan purInvoice = null;
        Long sundryCreditorId = Long.parseLong(request.getParameter("supplier_id"));
        if (users.getBranch() != null) {
            purInvoice = tranxPurChallanRepository.findByOutletIdAndBranchIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), sundryCreditorId, request.getParameter("bill_no"));
        } else {
            purInvoice = tranxPurChallanRepository.findByOutletIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), sundryCreditorId, request.getParameter("bill_no"));
        }
        if (purInvoice != null) {
            responseMessage.setMessage("Purchase Challan Already Exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New purchase challan number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }


    public JsonObject getChallanSupplierListByProductId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        Long productId = Long.parseLong(request.getParameter("productId"));


        List<TranxPurChallanDetailsUnits> tranxPurChallanDetailsUnits = tranxPurChallanDetailsUnitRepository.findByProductIdAndStatusOrderByIdDesc(productId, true);

        for (TranxPurChallanDetailsUnits obj : tranxPurChallanDetailsUnits) {
            JsonObject response = new JsonObject();
            response.addProperty("supplier_name", obj.getTranxPurChallan().getSundryCreditors().getLedgerName());
            response.addProperty("invoice_no", obj.getTranxPurChallan().getId());
            response.addProperty("invoice_date", obj.getTranxPurChallan().getInvoiceDate().toString());
            if (obj.getProductBatchNo() != null) {
                response.addProperty("batch", obj.getProductBatchNo().getBatchNo());
                response.addProperty("mrp", obj.getProductBatchNo().getMrp());
                response.addProperty("cost", obj.getProductBatchNo().getCosting());
                response.addProperty("rate", obj.getProductBatchNo().getSalesRate());


            } else {
                ProductUnitPacking productUnitPacking = productUnitRepository.findByIdAndStatus(productId, true);
                response.addProperty("mrp", productUnitPacking.getMrp());
                response.addProperty("cost", productUnitPacking.getCosting());
                response.addProperty("rate", productUnitPacking.getPurchaseRate());

            }
//            response.addProperty("batch", obj.getProductBatchNo().getBatchNo());
//            response.addProperty("mrp", obj.getProductBatchNo().getMrp());
            response.addProperty("quantity", obj.getQty());
//            response.addProperty("rate", obj.getRate());
//            response.addProperty("cost", obj.getProductBatchNo().getCosting());
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

    public Object validateChallanUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxPurChallan purChallan = null;
        Long sundryCreditorId = Long.parseLong(request.getParameter("supplier_id"));
        Long invoiceId = Long.parseLong(request.getParameter("challan_id"));
        System.out.println("Bill no = " + request.getParameter("bill_no"));
        if (users.getBranch() != null) {
            purChallan = tranxPurChallanRepository.findByOutletIdAndBranchIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), sundryCreditorId, request.getParameter("bill_no"));
        } else {
            purChallan = tranxPurChallanRepository.findByOutletIdAndSundryCreditorsIdAndVendorInvoiceNoIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), sundryCreditorId, request.getParameter("bill_no"));
        }
        if (purChallan != null && invoiceId != purChallan.getId()) {
            responseMessage.setMessage("Purchase Challan Already Exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New purchase invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }


    public JsonObject purchasePrintChallan(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxPurOrderDetailsUnits> list = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        TranxPurChallan purChallan = null;
        String source = request.getParameter("source");
        String key = request.getParameter("print_type"); //check whether printbill is calling from create page or from list page
        /***** if  print_type is create, then use serialnumber of invoice to fetch invoice details ,
         * if print_type is list then use invoice id to fetch invoice details *****/
        try {
            String invoiceNo = request.getParameter("id");
            Long id = 0L;
            if (source.equalsIgnoreCase("purchase_challan")) {
                if (users.getBranch() != null) {

                    id = Long.parseLong(invoiceNo);
                    purChallan = tranxPurChallanRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);

                } else {

                    id = Long.parseLong(invoiceNo);
                    purChallan = tranxPurChallanRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);

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
            if (purChallan != null) {
                //   list = tranxPurInvoiceUnitsRepository.findByPurchaseTransactionIdAndStatus(purInvoice.getId(), true);
                JsonObject companyObject = new JsonObject();
                companyObject.addProperty("company_name", users.getOutlet().getCompanyName());
                companyObject.addProperty("company_address", users.getOutlet().getCorporateAddress());
                companyObject.addProperty("phone_number", users.getOutlet().getMobileNumber());
                companyObject.addProperty("email_address", users.getOutlet().getEmail());
                companyObject.addProperty("gst_number", users.getOutlet().getGstNumber());
                JsonObject debtorsObject = new JsonObject();
                debtorsObject.addProperty("supplier_name", purChallan.getSundryCreditors().getLedgerName());
                debtorsObject.addProperty("supplier_address", purChallan.getSundryCreditors().getAddress());
                debtorsObject.addProperty("supplier_gstin", purChallan.getSundryCreditors().getGstin());
                debtorsObject.addProperty("supplier_phone", purChallan.getSundryCreditors().getMobile());
                JsonObject invoiceObject = new JsonObject();
                /* Sales Invoice Data */
                invoiceObject.addProperty("id", purChallan.getId());
                invoiceObject.addProperty("invoice_dt", purChallan.getInvoiceDate().toString());
                invoiceObject.addProperty("invoice_no", purChallan.getVendorInvoiceNo());
                invoiceObject.addProperty("state_code", purChallan.getOutlet().getStateCode());
                invoiceObject.addProperty("state_name", purChallan.getOutlet().getState().getName());
                invoiceObject.addProperty("taxable_amt", numFormat.numFormat(purChallan.getTaxableAmount()));
                invoiceObject.addProperty("tax_amount", numFormat.numFormat(purChallan.getTotaligst()));
                invoiceObject.addProperty("total_cgst", numFormat.numFormat(purChallan.getTotalcgst()));
                invoiceObject.addProperty("total_sgst", numFormat.numFormat(purChallan.getTotalsgst()));
                invoiceObject.addProperty("net_amount", numFormat.numFormat(purChallan.getTotalBaseAmount()));
                invoiceObject.addProperty("total_discount", numFormat.numFormat(purChallan.getTotalPurchaseDiscountAmt()));
                invoiceObject.addProperty("total_amount", numFormat.numFormat(purChallan.getTotalAmount()));
                // invoiceObject.addProperty("advanced_amount", numFormat.numFormat(purInvoice.getAd() != null ? purInvoice.getAdvancedAmount() : 0.0));
                // invoiceObject.addProperty("payment_mode", purInvoice.getPaymentMode());

                /* End of Sales Invoice Data */

                /* Sales Invoice Details */
                JsonObject productObject = new JsonObject();
                JsonArray row = new JsonArray();

                /* getting Units of Sales Quotations*/
                List<TranxPurChallanDetailsUnits> unitDetails = tranxPurChallanDetailsUnitRepository.findByTranxPurChallanIdAndStatus(purChallan.getId(), true);
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
            purChallanLogger.error("Error in getInvoiceBillPrint :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            purChallanLogger.error("Error in getInvoiceBillPrint :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }
}
