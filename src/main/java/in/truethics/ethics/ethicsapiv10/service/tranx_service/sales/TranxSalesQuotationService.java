package in.truethics.ethics.ethicsapiv10.service.tranx_service.sales;

import com.google.gson.*;
import in.truethics.ethics.ethicsapiv10.common.*;
import in.truethics.ethics.ethicsapiv10.dto.salesdto.SalesInvoiceDTO;
import in.truethics.ethics.ethicsapiv10.dto.salesdto.SalesQoutationDTO;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerGstDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.product_barcode.ProductBarcodeRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.PurchaseInvoiceProductSrNumberRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesQuotaionDetailsUnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesQuotationDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesQuotationDutiesTaxesRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesQuotationRepository;
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
import java.security.PrivateKey;
import java.time.LocalDate;
import java.util.*;

@Service
public class TranxSalesQuotationService {

    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private TranxSalesQuotationRepository tranxSalesQuotationRepository;
    private TranxSalesQuotation mSalesQuotationTransaction;
    @Autowired
    private TranxSalesQuotationDutiesTaxesRepository salesQuotationDutiesTaxesRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TranxSalesQuotationDetailsRepository salesQuotationInvoiceDetailsRepository;
    @Autowired
    private TransactionStatusRepository transactionStatusRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private TranxSalesQuotaionDetailsUnitsRepository tranxSalesQuotaionDetailsUnitsRepository;
    @Autowired
    private ProductData productData;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;

    @Autowired
    private NumFormat numFormat;
    private static final Logger salesQuatationLogger = LogManager.getLogger(TranxSalesQuotationService.class);
    List<Long> dbList = new ArrayList<>(); // for saving all ledgers Id against Purchase invoice from DB
    List<Long> mInputList = new ArrayList<>(); // input all ledgers Id against Purchase invoice from request
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;

    @Autowired
    private LedgerGstDetailsRepository ledgerGstDetailsRepository;

    public JsonObject salesQuotationLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxSalesQuotationRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxSalesQuotationRepository.findLastRecord(users.getOutlet().getId());
        }
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String csCode = "SQ" + currentMonth + serailNo;

        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("serialNo", csCode);
        return result;
    }

    public JsonArray getAllSalesQuotations(HttpServletRequest request) throws Exception {

        List<TranxSalesQuotation> quotations = tranxSalesQuotationRepository.findAll();

        Branch branch = null;
        Outlet outlet = null;
        LedgerMaster sundryDebtors = null;
        LedgerMaster salesAccountLedger = null;
        LedgerMaster salesRoundOff = null;
        /*List<TranxSalesQuotationDetails> salesQuotationInvoiceDetails = null;
        List<TranxSalesQuotationDutiesTaxes> salesQuotationDutiesTaxes = null;*/

        JsonArray jsonArray = new JsonArray();
        if (!quotations.isEmpty()) {

            for (int i = 0; i < quotations.size(); i++) {
                JsonObject jsonObject = new JsonObject();
                TranxSalesQuotation salesQuotationTransaction = quotations.get(i);


                branch = salesQuotationTransaction.getBranch();
                outlet = salesQuotationTransaction.getOutlet();
                sundryDebtors = salesQuotationTransaction.getSundryDebtors();
                salesAccountLedger = salesQuotationTransaction.getSalesAccountLedger();
                salesRoundOff = salesQuotationTransaction.getSalesRoundOff();
//                salesQuotationInvoiceDetails = salesQuotationInvoiceDetailsRepository.findByTranxSalesQuotationId(salesQuotationTransaction.getId());
//                salesQuotationDutiesTaxes = salesQuotationTransaction.getSalesQuotationDutiesTaxes();

                jsonObject.addProperty("bill_date", salesQuotationTransaction.getBillDate().toString());
                jsonObject.addProperty("bill_no", "ABC123");
                jsonObject.addProperty("sales_acc_id", salesAccountLedger.getId());
                jsonObject.addProperty("transaction_dt", salesQuotationTransaction.getBillDate().toString());
                jsonObject.addProperty("debtors_id", sundryDebtors.getId());
                jsonObject.addProperty("roundoff", salesQuotationTransaction.getRoundOff());
                jsonObject.addProperty("narration", salesQuotationTransaction.getNarration());
                jsonObject.addProperty("total_base_amt", salesQuotationTransaction.getTotalBaseAmount());
                jsonObject.addProperty("totalamt", salesQuotationTransaction.getTotalAmount());
                jsonObject.addProperty("taxable_amount", salesQuotationTransaction.getTaxableAmount());
                jsonObject.addProperty("totalcgst", salesQuotationTransaction.getTotalcgst());
                jsonObject.addProperty("totalsgst", salesQuotationTransaction.getTotalsgst());
                jsonObject.addProperty("totaligst", salesQuotationTransaction.getTotaligst());
                jsonObject.addProperty("totalqty", salesQuotationTransaction.getTotalqty());
                jsonObject.addProperty("tcs", salesQuotationTransaction.getTcs());
                /*jsonObject.addProperty("sales_discount",0);
                jsonObject.addProperty("sales_discount_amt",0);
                jsonObject.addProperty("total_sales_discount_amt",0);
                jsonObject.addProperty("sales_disc_ledger",0);*/
                jsonObject.addProperty("additionalChargesTotal", salesQuotationTransaction.getAdditionalChargesTotal());
                jsonObject.addProperty("totalqty", salesQuotationTransaction.getTotalqty());

                /*JsonArray row = new JsonArray();

                for (int j = 0; j < salesQuotationInvoiceDetails.size(); j++) {
                    TranxSalesQuotationDetails salesInvoiceDetails = salesQuotationInvoiceDetails.get(j);
                    Product product = salesInvoiceDetails.getProduct();

                    JsonObject jo_details = new JsonObject();
                    jo_details.addProperty("details_id", salesInvoiceDetails.getId());
                    jo_details.addProperty("product_id", product.getId());
                    jo_details.addProperty("qtyH", salesInvoiceDetails.getQtyHigh());
                    jo_details.addProperty("rateH", salesInvoiceDetails.getRateHigh());
                    jo_details.addProperty("qtyM", salesInvoiceDetails.getQtyMedium());
                    jo_details.addProperty("rateM", salesInvoiceDetails.getRateMedium());
                    jo_details.addProperty("qtyL", salesInvoiceDetails.getQtyLow());
                    jo_details.addProperty("qtyL", salesInvoiceDetails.getQtyLow());
                    jo_details.addProperty("rateL", salesInvoiceDetails.getRateLow());
                    jo_details.addProperty("base_amt_H", salesInvoiceDetails.getBaseAmtHigh());
                    jo_details.addProperty("base_amt_L", salesInvoiceDetails.getBaseAmtLow());
                    jo_details.addProperty("base_amt_M", salesInvoiceDetails.getBaseAmtMedium());
                    jo_details.addProperty("base_amt", salesInvoiceDetails.getBase_amt());
                    jo_details.addProperty("dis_amt", salesInvoiceDetails.getDiscountAmount());
                    jo_details.addProperty("dis_per", salesInvoiceDetails.getDiscountPer());
                    jo_details.addProperty("dis_per_cal", salesInvoiceDetails.getDiscountPerCal());
                    jo_details.addProperty("dis_amt_cal", salesInvoiceDetails.getDiscountAmountCal());
                    jo_details.addProperty("total_amt", salesInvoiceDetails.getTotalAmount());
                    jo_details.addProperty("igst", salesInvoiceDetails.getIgst());
                    jo_details.addProperty("cgst", salesInvoiceDetails.getCgst());
                    jo_details.addProperty("sgst", salesInvoiceDetails.getSgst());
                    jo_details.addProperty("total_igst", salesInvoiceDetails.getTotalIgst());
                    jo_details.addProperty("total_cgst", salesInvoiceDetails.getTotalCgst());
                    jo_details.addProperty("total_sgst", salesInvoiceDetails.getTotalSgst());
                    jo_details.addProperty("final_amt", salesInvoiceDetails.getFinalAmount());
                    jo_details.addProperty("discount_proportional_cal", 0);
                    jo_details.addProperty("additional_charges_proportional_cal", 0);

                    row.add(jo_details);
                }
                jsonObject.add("row", row);
                jsonObject.addProperty("additionalCharges", "[]");*/
                jsonObject.addProperty("sale_type", "sales quotation");

                jsonArray.add(jsonObject);


            }
        }

        return jsonArray;
    }

    public JsonObject getSalesQuotation(HttpServletRequest request) throws Exception {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        TranxSalesQuotation tranxSalesQuotation = tranxSalesQuotationRepository.
                findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        List<TranxSalesQuotationDetails> list = new ArrayList<>();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            list = salesQuotationInvoiceDetailsRepository.findByTranxSalesQuotationIdAndStatus(id, true);
            JsonObject result = new JsonObject();
            /* Sales Quotations Data */
            result.addProperty("id", tranxSalesQuotation.getId());
            result.addProperty("sales_sr_no", tranxSalesQuotation.getSalesQuotationSrNo());
            result.addProperty("sales_account_id", tranxSalesQuotation.getSalesAccountLedger().getId());
            result.addProperty("sales_account", tranxSalesQuotation.getSalesAccountLedger().getLedgerName());
            result.addProperty("bill_date", tranxSalesQuotation.getBillDate().toString());
            result.addProperty("sq_bill_no", tranxSalesQuotation.getSq_bill_no());
            result.addProperty("round_off", tranxSalesQuotation.getRoundOff());
            result.addProperty("total_base_amount", tranxSalesQuotation.getTotalBaseAmount());
            result.addProperty("total_amount", tranxSalesQuotation.getTotalAmount());
            result.addProperty("total_cgst", tranxSalesQuotation.getTotalcgst());
            result.addProperty("total_sgst", tranxSalesQuotation.getTotalsgst());
            result.addProperty("total_igst", tranxSalesQuotation.getTotaligst());
            result.addProperty("total_qty", tranxSalesQuotation.getTotalqty());
            result.addProperty("taxable_amount", tranxSalesQuotation.getTaxableAmount());
            result.addProperty("tcs", tranxSalesQuotation.getTcs());
            result.addProperty("status", tranxSalesQuotation.getStatus());
            result.addProperty("financial_year", tranxSalesQuotation.getFinancialYear());
            result.addProperty("debtor_id", tranxSalesQuotation.getSundryDebtors().getId());
            result.addProperty("debtor_name", tranxSalesQuotation.getSundryDebtors().getLedgerName());
            result.addProperty("narration", tranxSalesQuotation.getNarration());
            //result.addProperty("narration", tranxSalesQuotation.getNarration());

            result.addProperty("narration", tranxSalesQuotation.getNarration() != null ? tranxSalesQuotation.getNarration() : "");
            /* End of Sales Quotation Data */

            /* Sales Quotation Details */
            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxSalesQuotationDetails mDetails : list) {
                    JsonObject prDetails = new JsonObject();
                    prDetails.addProperty("details_id", mDetails.getId());
                    prDetails.addProperty("product_id", mDetails.getProduct().getId());
                   /* prDetails.addProperty("qtyH", mDetails.getQtyHigh());
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
                    prDetails.addProperty("dis_per_cal", mDetails.getDiscountPerCal());
                    prDetails.addProperty("dis_per_cal", mDetails.getDiscountPerCal());
                    if (mDetails.getPackingMaster() != null) {
                        JsonObject package_obj = new JsonObject();
                        package_obj.addProperty("id", mDetails.getPackingMaster().getId());
                        package_obj.addProperty("pack_name", mDetails.getPackingMaster().getPackName());
                        package_obj.addProperty("label", mDetails.getPackingMaster().getPackName());
                        package_obj.addProperty("value", mDetails.getPackingMaster().getId());
                        prDetails.add("package_id", package_obj);
                    } else {
                        prDetails.addProperty("package_id", "");
                    }*/
                    JsonArray serialNo = new JsonArray();
                    /* getting Units of Sales Quotations*/
                    List<TranxSalesQuotationDetailsUnits> unitDetails = tranxSalesQuotaionDetailsUnitsRepository.findBySalesQuotationDetailsIdAndStatus(
                            mDetails.getId(), true);

                    JsonArray productDetails = new JsonArray();
                    unitDetails.forEach(mUnit -> {
                        JsonObject mObject = new JsonObject();
                        JsonObject mUnitsObj = new JsonObject();
                     /*   if (mUnit.getPackingMaster() != null) {
                            JsonObject package_obj = new JsonObject();
                            package_obj.addProperty("id", mUnit.getPackingMaster().getId());
                            package_obj.addProperty("pack_name", mUnit.getPackingMaster().getPackName());
                            package_obj.addProperty("label", mUnit.getPackingMaster().getPackName());
                            package_obj.addProperty("value", mUnit.getPackingMaster().getId());
                            mObject.add("package_id", package_obj);
                        } else {
                            mObject.addProperty("package_id", "");
                        }*/
                        /* mUnitsObj.addProperty("units_id", mUnit.getUnits().getId());
                        mUnitsObj.addProperty("value", mUnit.getUnits().getId());
                        mUnitsObj.addProperty("label", mUnit.getUnits().getUnitName());
                        mUnitsObj.addProperty("unit_name", mUnit.getUnits().getUnitName());
                        mObject.add("unitId", mUnitsObj);*/
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
            e.printStackTrace();
            salesQuatationLogger.error("Exception in getSalesQuotation:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesQuatationLogger.error("Exception in getSalesQuotation:" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject saveSalesQuotation(HttpServletRequest request) {

        JsonObject object = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        TranxSalesQuotation salesQuotationTransaction = saveSalesQuotationRequest(request, users);
        if (salesQuotationTransaction != null) {
            object.addProperty("message", "Sales quotation created successfully");
            object.addProperty("responseStatus", HttpStatus.OK.value());
            object.addProperty("id", salesQuotationTransaction.getId().toString());
            /**
             * @implNote validation of Ledger Delete , if any tranx done for this ledger, user cant delete this ledger **
             * @auther ashwins@opethic.com
             * @version sprint 21
             **/
            LedgerMaster ledgerMaster = ledgerMasterRepository.findByIdAndStatus(salesQuotationTransaction.getSundryDebtors().getId(), true);
            ledgerMaster.setIsDeleted(false);
            ledgerMasterRepository.save(ledgerMaster);

        } else {
            object.addProperty("message", "Error in Sales quotation creation");
            object.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return object;
    }

    private TranxSalesQuotation saveSalesQuotationRequest(HttpServletRequest request, Users users) {

        TranxSalesQuotation salesQuotationTransaction = new TranxSalesQuotation();
        Map<String, String[]> paramMap = request.getParameterMap();
        LedgerMaster sundryDebtors = null;
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
            salesQuotationTransaction.setBranch(branch);
        }
        Outlet outlet = users.getOutlet();
        salesQuotationTransaction.setOutlet(outlet);
        TransactionStatus transactionStatus = transactionStatusRepository.findByStatusNameAndStatus("opened", true);
        salesQuotationTransaction.setTransactionStatus(transactionStatus);
        salesQuotationTransaction.setSq_bill_no(request.getParameter("bill_no"));
        salesQuotationTransaction.setSalesQuotationSrNo(Long.parseLong(request.getParameter("sales_sr_no")));
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("bill_dt"));
        salesQuotationTransaction.setBillDate(strDt);
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            salesQuotationTransaction.setFiscalYear(fiscalYear);
            salesQuotationTransaction.setFinancialYear(fiscalYear.getFiscalYear());
        }
        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                salesQuotationTransaction.setGstNumber(request.getParameter("gstNo"));
            }
        }

        LedgerMaster salesAccounts = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sales_acc_id")), true);

        sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("debtors_id")), true);

        /*** this scenario is for Upahar Trading only , order cakes and payment made from client as an advance ****/

        salesQuotationTransaction.setSalesAccountLedger(salesAccounts);
        salesQuotationTransaction.setSundryDebtors(sundryDebtors);
        salesQuotationTransaction.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        // LedgerMaster roundoff = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId("Round off", users.getOutlet().getId());
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        salesQuotationTransaction.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        salesQuotationTransaction.setSalesRoundOff(roundoff);
        salesQuotationTransaction.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
        if (taxFlag) {
            salesQuotationTransaction.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
            salesQuotationTransaction.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
            salesQuotationTransaction.setTotaligst(0.0);
        }
        /* if false : igst i.e inter state */
        else {
            salesQuotationTransaction.setTotalcgst(0.0);
            salesQuotationTransaction.setTotalsgst(0.0);
            salesQuotationTransaction.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
        }
        salesQuotationTransaction.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        salesQuotationTransaction.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
        salesQuotationTransaction.setTcs(Double.parseDouble(request.getParameter("tcs")));
        salesQuotationTransaction.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        salesQuotationTransaction.setTotalTax(Double.parseDouble(request.getParameter("total_tax_amt")));
        salesQuotationTransaction.setNarration(request.getParameter("narration"));
        salesQuotationTransaction.setCreatedBy(users.getId());
        salesQuotationTransaction.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        salesQuotationTransaction.setStatus(true);
        salesQuotationTransaction.setOperations("inserted");
        salesQuotationTransaction.setCreatedBy(users.getId());
//        if (paramMap.containsKey("transport")) salesOrderTransaction.setTransportName(request.getParameter("transport"));
//        else salesOrderTransaction.setTransportName("NA");

        // closing all references of purchase order ids
        /*salesOrderTransaction.setNarration(request.getParameter("narration"));
        salesOrderTransaction.setIsCounterSale(false);*/
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSORD");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        salesQuotationTransaction.setTranxCode(tranxCode);
        try {
            mSalesQuotationTransaction = tranxSalesQuotationRepository.save(salesQuotationTransaction);
            /* Save into Sales Duties and Taxes */
            if (mSalesQuotationTransaction != null) {


                /***** for unregister or composition company, for registered company uncomment below saveIntoSalesDutiesTaxesEdit ****/
                String taxStr = request.getParameter("taxCalculation");
                if (!taxStr.isEmpty()) {
                    JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);
                    saveIntoDutiesAndTaxes(duties_taxes, mSalesQuotationTransaction, taxFlag, users.getOutlet().getId());
                }
                JsonParser parser = new JsonParser();
                String jsonStr = request.getParameter("row");
                JsonElement quotationDetailsJson = parser.parse(jsonStr);
                JsonArray invoiceDetails = quotationDetailsJson.getAsJsonArray();
                String referenceObj = request.getParameter("refObject");
                saveIntoSalesQuotationInvoiceDetails(invoiceDetails, mSalesQuotationTransaction, branch, outlet, users.getId(), tranxType, referenceObj, "create", "");
            }

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesQuatationLogger.error("Error in saveSalesQuatationRequest :->" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesQuatationLogger.error("Error in saveSalesQuatationRequest :->" + e1.getMessage());
            System.out.println("Exception:" + e1.getMessage());
        }
        return mSalesQuotationTransaction;
    }

    private void saveIntoSalesQuotationInvoiceDetails(JsonArray array,
                                                      TranxSalesQuotation mSalesTranx, Branch branch,
                                                      Outlet outlet, Long id, TransactionTypeMaster tranxType, String referenceObj, String type, String rowsDeleted) {

        for (JsonElement mList : array) {
            JsonObject object = mList.getAsJsonObject();
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
            Long levelAId = null;
            Long levelBId = null;
            Long levelCId = null;
            String batchNo = null;

            ProductBatchNo productBatchNo = null;
            TranxSalesQuotationDetailsUnits orderUnits = null;
            LevelA levelA = null;
            LevelB levelB = null;
            LevelC levelC = null;
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
            if (type.equalsIgnoreCase("create")) {
                orderUnits = new TranxSalesQuotationDetailsUnits();
                orderUnits.setStatus(true);
                orderUnits.setTransactionStatus(1L);
            } else {
                Long detailsId = object.get("details_id").getAsLong();
                if (detailsId != 0) {
                    orderUnits = tranxSalesQuotaionDetailsUnitsRepository.findByIdAndStatus(detailsId, true);

                } else {
                    orderUnits = new TranxSalesQuotationDetailsUnits();
                    orderUnits.setStatus(true);
                    orderUnits.setTransactionStatus(1L);
                }
            }
            orderUnits.setSalesQuotation(mSalesTranx);
            orderUnits.setProduct(mProduct);
            orderUnits.setUnits(units);
            orderUnits.setQty(object.get("qty").getAsDouble());
            orderUnits.setFreeQty(object.get("free_qty").getAsDouble());
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
            orderUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
            orderUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
            orderUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
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
            tranxSalesQuotaionDetailsUnitsRepository.save(orderUnits);
            /**
             * @implNote validation of Product Delete , if any tranx done for this product, user cant delete this product **
             * @auther ashwins@opethic.com
             * @version sprint 21
             **/
            if (mProduct != null && mProduct.getIsDelete()) {
                mProduct.setIsDelete(false);
                productRepository.save(mProduct);
            }


        }
    }

    private void saveIntoDutiesAndTaxes(JsonObject duties_taxes,
                                        TranxSalesQuotation mSalesQuotationTransaction,
                                        Boolean taxFlag, Long outletId) {

        List<TranxSalesQuotationDutiesTaxes> salesQuotationDutiesTaxes = new ArrayList<>();
        if (taxFlag) {
            JsonArray cgstList = duties_taxes.get("cgst").getAsJsonArray();
            JsonArray sgstList = duties_taxes.get("sgst").getAsJsonArray();
            /* this is for Cgst creation */
            if (cgstList.size() > 0) {
                for (int i = 0; i < cgstList.size(); i++) {
                    JsonObject cgstObject = cgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //  int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT CGST " + inputGst;

                    if (mSalesQuotationTransaction.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesQuotationTransaction.getOutlet().getId(), mSalesQuotationTransaction.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesQuotationTransaction.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();
                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(cgstObject.get("amt").getAsString()));
                        taxes.setSalesQuotationTransaction(mSalesQuotationTransaction);
                        taxes.setSundryDebtors(mSalesQuotationTransaction.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesQuotationDutiesTaxes.add(taxes);
                    }
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (int i = 0; i < sgstList.size(); i++) {
                    JsonObject sgstObject = sgstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //   int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT SGST " + inputGst;
                 /*   dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(
                            ledgerName, outletId);*/
                    if (mSalesQuotationTransaction.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesQuotationTransaction.getOutlet().getId(), mSalesQuotationTransaction.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesQuotationTransaction.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();

                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(sgstObject.get("amt").getAsString()));
                        taxes.setSalesQuotationTransaction(mSalesQuotationTransaction);
                        taxes.setSundryDebtors(mSalesQuotationTransaction.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesQuotationDutiesTaxes.add(taxes);

                    }

                }
            }
        } else {
            JsonArray igstList = duties_taxes.get("igst").getAsJsonArray();
            /* this is for Igst creation */
            if (igstList.size() > 0) {
                for (int i = 0; i < igstList.size(); i++) {
                    JsonObject igstObject = igstList.get(i).getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) igstObject.get("gst").getAsDouble();
                    String inputGst = igstObject.get("gst").getAsString();
                    String ledgerName = "OUTPUT IGST " + inputGst;
                 /*   dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(
                            ledgerName, outletId);*/
                    if (mSalesQuotationTransaction.getBranch() != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mSalesQuotationTransaction.getOutlet().getId(), mSalesQuotationTransaction.getBranch().getId(), ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mSalesQuotationTransaction.getOutlet().getId(), ledgerName);

                    if (dutiesTaxes != null) {
                        TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();

                        taxes.setDutiesTaxes(dutiesTaxes);
                        taxes.setAmount(Double.parseDouble(igstObject.get("amt").getAsString()));
                        taxes.setSalesQuotationTransaction(mSalesQuotationTransaction);
                        taxes.setSundryDebtors(mSalesQuotationTransaction.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesQuotationDutiesTaxes.add(taxes);
                    }
                }
            }
        }
        try {
            /* save all Duties and Taxes into Sales Invoice Duties taxes table */
            salesQuotationDutiesTaxesRepository.saveAll(salesQuotationDutiesTaxes);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            salesQuatationLogger.error("Exception in saveIntoDutiesAndTaxes:" + e.getMessage());
            System.out.println("Exception:" + e.getMessage());

        } catch (Exception e1) {
            e1.printStackTrace();
            salesQuatationLogger.error("Exception in saveIntoDutiesAndTaxes:" + e1.getMessage());
            System.out.println(e1.getMessage());
        }
    }

    /* List of Sales Quotations Outletwise */
    public JsonObject AllsQInvoiceList(HttpServletRequest request) {

        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        List<TranxSalesQuotation> tranxSalesQuotations = new ArrayList<>();
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
                tranxSalesQuotations = tranxSalesQuotationRepository.findSaleQuotationListWithDateWithBr(users.getOutlet().getId(),
                        users.getBranch().getId(), startDatep, endDatep, true);
            } else {
                tranxSalesQuotations = tranxSalesQuotationRepository.findSaleQuotationListWithDate(users.getOutlet().getId(), startDatep, endDatep, true);
            }
        } else {
            if (users.getBranch() != null) {
                tranxSalesQuotations = tranxSalesQuotationRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(),
                        users.getBranch().getId(), true);
            } else {
                tranxSalesQuotations = tranxSalesQuotationRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
            }
        }

        for (TranxSalesQuotation invoices : tranxSalesQuotations) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("bill_no", invoices.getSq_bill_no());
            response.addProperty("bill_date", invoices.getBillDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount().toString());
            response.addProperty("total_base_amount", invoices.getTotalBaseAmount().toString());
            response.addProperty("sundry_debtors_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtors_id", invoices.getSundryDebtors().getId());
            response.addProperty("sales_quotation_status", invoices.getTransactionStatus().getStatusName());
            response.addProperty("sales_account", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration() != null ? invoices.getNarration().toString() : "");
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

    //start of sales qoutation list with pagination
    public Object sQInvoiceList(@RequestBody Map<String, String> request, HttpServletRequest req) {
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
        List<TranxSalesQuotation> saleQuotation = new ArrayList<>();
        List<TranxSalesQuotation> saleArrayList = new ArrayList<>();
        List<SalesQoutationDTO> salesQoutationDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_sales_quotation_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
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
//                query = query + " ORDER BY sundry_debtors_id ASC";
                query = query + " ORDER BY id DESC";
            }
            String query1 = query;       //we get all lists in this list
            System.out.println("query== " + query);
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            Query q = entityManager.createNativeQuery(query, TranxSalesQuotation.class);
            System.out.println("q ==" + q + "  saleQoutation " + saleQuotation);
            saleQuotation = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxSalesQuotation.class);

            saleArrayList = q1.getResultList();
            System.out.println("Limit total rows " + saleArrayList.size());
            Integer total_pages = (saleArrayList.size() / pageSize);
            if ((saleArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            System.out.println("total pages " + total_pages);
            for (TranxSalesQuotation qoutationListView : saleQuotation) {
                salesQoutationDTOList.add(convertToDTDTO(qoutationListView));
            }
            GenericDatatable<SalesQoutationDTO> data = new GenericDatatable<>(salesQoutationDTOList, saleArrayList.size(),
                    pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(salesQoutationDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    //end of sales qoutation list with pagination
    //start of DTO for sales qoutation
    private SalesQoutationDTO convertToDTDTO(TranxSalesQuotation tranxSalesQuotation) {
        SalesQoutationDTO salesQoutationDTO = new SalesQoutationDTO();
        salesQoutationDTO.setId(tranxSalesQuotation.getId() != null ? tranxSalesQuotation.getId() : 0);
        salesQoutationDTO.setBill_date(tranxSalesQuotation.getBillDate() != null ? DateConvertUtil.convertDateToLocalDate(tranxSalesQuotation.getBillDate()).toString() : "");
        salesQoutationDTO.setBill_no(tranxSalesQuotation.getSq_bill_no() != null ? tranxSalesQuotation.getSq_bill_no() : "");
        salesQoutationDTO.setTotal_amount(tranxSalesQuotation.getTotalAmount() != null ? tranxSalesQuotation.getTotalAmount() : 0.0);
        salesQoutationDTO.setTotal_base_amount(tranxSalesQuotation.getTotalBaseAmount() != null ? tranxSalesQuotation.getTotalBaseAmount() : 0.0);
        salesQoutationDTO.setNarration(tranxSalesQuotation.getNarration() != null ? tranxSalesQuotation.getNarration() : "");
//        salesQoutationDTO.setPayment_mode(tranxSalesQuotation.gePaymentMode());
        salesQoutationDTO.setSales_quotation_status(tranxSalesQuotation.getTransactionStatus().getStatusName() != null ? tranxSalesQuotation.getTransactionStatus().getStatusName() : "");
        salesQoutationDTO.setSale_account_name(tranxSalesQuotation.getSalesAccountLedger().getLedgerName() != null ? tranxSalesQuotation.getSalesAccountLedger().getLedgerName() : "");
//        salesQoutationDTO.setSale_serial_number(tranxSalesQuotation.getSalesSerialNumber());
        salesQoutationDTO.setSundry_debtors_id(tranxSalesQuotation.getSundryDebtors().getId() != null ? tranxSalesQuotation.getSundryDebtors().getId() : 0);
        salesQoutationDTO.setSundry_debtors_name(tranxSalesQuotation.getSundryDebtors().getLedgerName() != null ? tranxSalesQuotation.getSundryDebtors().getLedgerName() : "");
        salesQoutationDTO.setTax_amt(tranxSalesQuotation.getTotalTax() != null ? tranxSalesQuotation.getTotalTax() : 0.0);
        salesQoutationDTO.setTaxable_amt(tranxSalesQuotation.getTaxableAmount() != null ? tranxSalesQuotation.getTaxableAmount() : 0.0);
        salesQoutationDTO.setTranxCode(tranxSalesQuotation.getTranxCode() != null ? tranxSalesQuotation.getTranxCode() : "");

        return salesQoutationDTO;

    }
    //end of DTO for sales qoutation

    public JsonObject saleQuotationList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        List<TranxSalesQuotation> tranxSalesQuotations = tranxSalesQuotationRepository.findAllByStatus(true);
        for (TranxSalesQuotation invoices : tranxSalesQuotations) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("sq_bill_no", invoices.getSq_bill_no());
            response.addProperty("bill_date", invoices.getBillDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("sundry_debtor_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtor_id", invoices.getSundryDebtors().getId());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }


    /**
     * Delete sales quotation
     **/
    public JsonObject salesQuotationDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxSalesQuotation salesTranx = tranxSalesQuotationRepository.findByIdAndStatusAndTransactionStatusId(Long.parseLong(request.getParameter("id")), true, 1L);
        TranxSalesQuotation mPurchaseTranx;
        try {
            if (salesTranx != null) {

                salesTranx.setStatus(false);
                salesTranx.setOperations("deletion");
                mPurchaseTranx = tranxSalesQuotationRepository.save(salesTranx);
                jsonObject.addProperty("message", "deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "cant delete closed orders");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }
        } catch (Exception e) {
            salesQuatationLogger.error("Error in Sales Quotataion Delete()->" + e.getMessage());
        }
        return jsonObject;
    }


    public JsonObject getSaleQuotationWithIds(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject output = new JsonObject();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("SLSQTN");
        String str = request.getParameter("sale_quotation_ids");
        JsonParser parser = new JsonParser();
        JsonElement purDetailsJson = parser.parse(str);
        JsonArray jsonArray = purDetailsJson.getAsJsonArray();
        JsonArray newList = new JsonArray();
        JsonObject invoiceData = new JsonObject();
        Double totalDis_amt = 0.0, gross_amt = 0.0;
        JsonArray gstArray = new JsonArray();
        for (JsonElement mList : jsonArray) {
            JsonObject object = mList.getAsJsonObject();
            /* getting Units of Purchase Orders */
            JsonArray unitsJsonArray = new JsonArray();
            TranxSalesQuotation tranxSalesQuotation = tranxSalesQuotationRepository.findByIdAndOutletIdAndStatus(object.get("id").getAsLong(), users.getOutlet().getId(), true);
            List<TranxSalesQuotationDetailsUnits> unitsArray =
                    tranxSalesQuotaionDetailsUnitsRepository.findBySalesQuotationIdAndTransactionStatusAndStatus(
                            object.get("id").getAsLong(), 1L, true);

            for (TranxSalesQuotationDetailsUnits mUnits : unitsArray) {
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
                unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
                unitsJsonObjects.addProperty("dis_amt", mUnits.getDiscountAmount());
                unitsJsonObjects.addProperty("dis_per", mUnits.getDiscountPer());
                unitsJsonObjects.addProperty("dis_per_cal", mUnits.getDiscountPerCal() != null ? mUnits.getDiscountPerCal().toString() : "");
                unitsJsonObjects.addProperty("dis_amt_cal", mUnits.getDiscountAmountCal() != null ? mUnits.getDiscountAmountCal().toString() : "");
                unitsJsonObjects.addProperty("total_amt", mUnits.getTotalAmount());
                unitsJsonObjects.addProperty("pack_name", mUnits.getProduct().getPackingMaster() != null ?
                        mUnits.getProduct().getPackingMaster().getPackName() : "");
                unitsJsonObjects.addProperty("gst", mUnits.getIgst());
                unitsJsonObjects.addProperty("igst", mUnits.getIgst());
                unitsJsonObjects.addProperty("cgst", mUnits.getCgst());
                unitsJsonObjects.addProperty("sgst", mUnits.getSgst());
                unitsJsonObjects.addProperty("total_igst", mUnits.getTotalIgst());
                unitsJsonObjects.addProperty("total_cgst", mUnits.getTotalCgst());
                unitsJsonObjects.addProperty("total_sgst", mUnits.getTotalSgst());
                unitsJsonObjects.addProperty("final_amt", mUnits.getFinalAmount());
                unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty() != null ? mUnits.getFreeQty().toString() : "");
                unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer() != null ? mUnits.getDiscountBInPer().toString() : "");
                unitsJsonObjects.addProperty("row_dis_amt", mUnits.getTotalDiscountInAmt() != null ? mUnits.getTotalDiscountInAmt().toString() : "");
                unitsJsonObjects.addProperty("gross_amt", mUnits.getGrossAmt());
                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1());
                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt());
                unitsJsonObjects.addProperty("reference_id", mUnits.getSalesQuotation().getId());
                unitsJsonObjects.addProperty("reference_type", tranxType.getTransactionCode());
                unitsJsonObjects.addProperty("b_detailsId", 0);
                unitsJsonObjects.addProperty("is_batch", mUnits.getProduct().getIsBatchNumber() != null ?
                        mUnits.getProduct().getIsBatchNumber() : false);

                newList.add(unitsJsonObjects);
                invoiceData.addProperty("id", mUnits.getSalesQuotation().getId());
                invoiceData.addProperty("invoice_dt", DateConvertUtil.convertDateToLocalDate(mUnits.getSalesQuotation().getBillDate()).toString());
                invoiceData.addProperty("sales_quotation_no", mUnits.getSalesQuotation().getSq_bill_no());
                invoiceData.addProperty("sales_account_id", mUnits.getSalesQuotation().getSalesAccountLedger().getId());
                invoiceData.addProperty("sales_account_name", mUnits.getSalesQuotation().getSalesAccountLedger().getLedgerName());
                invoiceData.addProperty("sales_sr_no", mUnits.getSalesQuotation().getSalesQuotationSrNo());
                invoiceData.addProperty("sq_sr_no", mUnits.getSalesQuotation().getId());
                invoiceData.addProperty("sq_transaction_dt", DateConvertUtil.convertDateToLocalDate(mUnits.getSalesQuotation().getBillDate()).toString());
                invoiceData.addProperty("reference", mUnits.getSalesQuotation().getReference());
                invoiceData.addProperty("debtors_id", mUnits.getSalesQuotation().getSundryDebtors().getId());
                invoiceData.addProperty("debtors_name", mUnits.getSalesQuotation().getSundryDebtors().getLedgerName());
                invoiceData.addProperty("ledgerStateCode", mUnits.getSalesQuotation().getSundryDebtors().getStateCode());
                invoiceData.addProperty("narration", mUnits.getSalesQuotation().getNarration());
                invoiceData.addProperty("gstNo", mUnits.getSalesQuotation().getGstNumber());
//                >>>>>
//                invoiceData.addProperty("gross_total", tranxSalesQuotation.getTotalBaseAmount());
                invoiceData.addProperty("total_base_amount", tranxSalesQuotation.getTotalBaseAmount());
                invoiceData.addProperty("total_amount", tranxSalesQuotation.getTotalAmount());
                invoiceData.addProperty("total_cgst", tranxSalesQuotation.getTotalcgst());
                invoiceData.addProperty("total_sgst", tranxSalesQuotation.getTotalsgst());
                invoiceData.addProperty("total_igst", tranxSalesQuotation.getTotaligst());
                invoiceData.addProperty("total_tax", tranxSalesQuotation.getTotalTax());
                invoiceData.addProperty("taxable_amount", tranxSalesQuotation.getTaxableAmount().doubleValue());

                totalDis_amt = totalDis_amt + mUnits.getTotalDiscountInAmt();
                gross_amt = gross_amt + mUnits.getGrossAmt();

                List<LedgerGstDetails> gstDetails = new ArrayList<>();
                gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(mUnits.getSalesQuotation().getSundryDebtors().getId(), true);

                if (gstDetails != null && gstDetails.size() > 0) {
                    for (LedgerGstDetails mGstDetails : gstDetails) {
                        JsonObject mGstObject = new JsonObject();
                        mGstObject.addProperty("id", mGstDetails.getId());
                        mGstObject.addProperty("gstNo", mGstDetails.getGstin());
                        mGstObject.addProperty("state", mGstDetails.getStateCode() != null ? mGstDetails.getStateCode() : "");
                        gstArray.add(mGstObject);
                    }
                }
            }
            invoiceData.addProperty("total_dis", totalDis_amt);
            invoiceData.addProperty("gross_amt", gross_amt);
        }
        JsonArray jsonAdditionalList = new JsonArray();


        output.add("gstDetails", gstArray);
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

    public Object updateSalesQuotation(HttpServletRequest request) {
        TranxSalesQuotation mPurchaseTranx = null;
        ResponseMessage responseMessage = new ResponseMessage();
        mPurchaseTranx = saveIntoSalesquotationEdit(request);
        if (mPurchaseTranx != null) {
            //insertIntoLedgerTranxDetails(mPurchaseTranx);
            responseMessage.setMessage("Sales Quotation updated successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } else {
            responseMessage.setMessage("Error in Sales Quotation updatation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    public TranxSalesQuotation saveIntoSalesquotationEdit(HttpServletRequest request) {
        TranxSalesQuotation mTranxSalesQuatation = null;
        TransactionTypeMaster tranxType = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Outlet outlet = users.getOutlet();
        TranxSalesQuotation tranxSalesQuotation = new TranxSalesQuotation();
        tranxSalesQuotation = tranxSalesQuotationRepository.findByIdAndOutletIdAndStatus(Long.parseLong(
                request.getParameter("id")), users.getOutlet().getId(), true);
        if (users.getBranch() != null) {
            branch = users.getBranch();
            tranxSalesQuotation.setBranch(branch);
        }
        tranxSalesQuotation.setSq_bill_no(request.getParameter("bill_no"));
        LocalDate date = LocalDate.parse(request.getParameter("bill_dt"));
        Date strDt = DateConvertUtil.convertStringToDate(request.getParameter("bill_dt"));
        tranxSalesQuotation.setBillDate(strDt);
        tranxSalesQuotation.setSalesQuotationSrNo(
                Long.parseLong(request.getParameter("sales_sr_no")));
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(date);
        if (fiscalYear != null) {
            tranxSalesQuotation.setFiscalYear(fiscalYear);
            tranxSalesQuotation.setFinancialYear(fiscalYear.getFiscalYear());
        }

        LedgerMaster salesAccounts = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("sales_acc_id")), true);

        if (paramMap.containsKey("gstNo")) {
            if (!request.getParameter("gstNo").equalsIgnoreCase("")) {
                tranxSalesQuotation.setGstNumber(request.getParameter("gstNo"));
            }
        }
//        Long discountLedgerId = Long.parseLong(request.getParameter("sales_disc_ledger"));

//        if (discountLedgerId > 0) {
//            try {
//                LedgerMaster discountLedger = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(
//                        request.getParameter("sales_disc_ledger")), true);
//                //salesOrderTransaction.setSalesDiscountLedger(discountLedger);
//            } catch (Exception e) {
//                e.printStackTrace();
//                salesQuatationLogger.error("Exception in saveIntoSalesquotationEdit:" + e.getMessage());
//                System.out.println(e.getMessage());
//            }
//        }
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(
                Long.parseLong(request.getParameter("debtors_id")),
                true);
        tranxSalesQuotation.setSalesAccountLedger(salesAccounts);
        tranxSalesQuotation.setSundryDebtors(sundryDebtors);
        tranxSalesQuotation.setTotalBaseAmount(Double.parseDouble(request.getParameter("total_base_amt")));
        //   LedgerMaster roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), "Round off");
        LedgerMaster roundoff = null;
        if (users.getBranch() != null)
            roundoff = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(users.getOutlet().getId(), users.getBranch().getId(), "Round off");
        else
            roundoff = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(users.getOutlet().getId(), "Round off");
        tranxSalesQuotation.setRoundOff(Double.parseDouble(request.getParameter("roundoff")));
        tranxSalesQuotation.setSalesRoundOff(roundoff);
        tranxSalesQuotation.setTotalAmount(Double.parseDouble(request.getParameter("totalamt")));
        Boolean taxFlag = Boolean.parseBoolean(request.getParameter("taxFlag"));
        /* if true : cgst and sgst i.e intra state */
//        if (taxFlag) {
//            tranxSalesQuotation.setTotalcgst(Double.parseDouble(request.getParameter("totalcgst")));
//            tranxSalesQuotation.setTotalsgst(Double.parseDouble(request.getParameter("totalsgst")));
//            tranxSalesQuotation.setTotaligst(0.0);
//        }
//        /* if false : igst i.e inter state */
//        else {
//            tranxSalesQuotation.setTotalcgst(0.0);
//            tranxSalesQuotation.setTotalsgst(0.0);
//            tranxSalesQuotation.setTotaligst(Double.parseDouble(request.getParameter("totaligst")));
//        }
        tranxSalesQuotation.setTotalqty(Long.parseLong(request.getParameter("total_qty")));
        tranxSalesQuotation.setFreeQty(Double.valueOf(request.getParameter("total_free_qty")));
//        tranxSalesQuotation.setTcs(Double.parseDouble(request.getParameter("tcs")));
        tranxSalesQuotation.setTaxableAmount(Double.parseDouble(request.getParameter("taxable_amount")));
        tranxSalesQuotation.setTotalTax(Double.parseDouble(request.getParameter("total_tax_amt")));
        tranxSalesQuotation.setNarration(request.getParameter("narration"));
        tranxSalesQuotation.setCreatedBy(users.getId());
//        tranxSalesQuotation.setAdditionalChargesTotal(Double.parseDouble(request.getParameter("additionalChargesTotal")));
        tranxSalesQuotation.setStatus(true);
        tranxSalesQuotation.setUpdatedBy(users.getId());
        tranxSalesQuotation.setNarration(request.getParameter("narration"));
        tranxSalesQuotation.setOperations("updated");
        try {
            mTranxSalesQuatation = tranxSalesQuotationRepository.save(tranxSalesQuotation);
        } catch (Exception e) {
            e.printStackTrace();
            salesQuatationLogger.error("Exception in saveIntoSalesquotationEdit:" + e.getMessage());
        }
        if (mTranxSalesQuatation != null) {
            /* Save into Duties and Taxes */
            String taxStr = request.getParameter("taxCalculation");
            if (!taxStr.isEmpty()) {
                // JsonObject duties_taxes = new JsonObject(taxStr);
                JsonObject duties_taxes = new Gson().fromJson(taxStr, JsonObject.class);

                saveIntosalesQuotationDutiesTaxesEdit(duties_taxes, mTranxSalesQuatation, taxFlag, tranxType, users.getOutlet().getId(), users.getId());
            }
            /* save into Additional Charges  */
            String jsonStr = request.getParameter("row");
            JsonParser parser = new JsonParser();
            JsonElement purDetailsJson = parser.parse(jsonStr);
            JsonArray array = purDetailsJson.getAsJsonArray();
            //  JsonArray array = new JsonArray(jsonStr);
            String rowsDeleted = "";
            if (paramMap.containsKey("rowDelDetailsIds"))
                rowsDeleted = request.getParameter("rowDelDetailsIds");
            saveIntoSalesQuotationInvoiceDetailsEdit(array, mTranxSalesQuatation, users.getBranch() != null ? users.getBranch() : null, users.getOutlet(), users.getId(), rowsDeleted, tranxType);
            String referenceObj = request.getParameter("refObject");
        }
        return mTranxSalesQuatation;
    }

    private void saveIntoSalesQuotationInvoiceDetailsEdit(JsonArray array, TranxSalesQuotation mTranxSalesQuatation, Branch branch, Outlet outlet, Long id, String rowsDeleted, TransactionTypeMaster tranxType) {

        for (JsonElement mList : array) {
            JsonObject object = mList.getAsJsonObject();
            Long details_id = object.get("details_id").getAsLong();
            TranxSalesQuotationDetailsUnits orderUnits = new TranxSalesQuotationDetailsUnits();
            if (details_id != 0) {
                orderUnits = tranxSalesQuotaionDetailsUnitsRepository.findByIdAndStatus(details_id, true);
            } else {
                orderUnits.setTransactionStatus(1L);
            }
            Product mProduct = productRepository.findByIdAndStatus(object.get("productId").getAsLong(), true);
//                referenceType = object.get("reference_type").getAsString();
            {
                Long levelAId = null;
                Long levelBId = null;
                Long levelCId = null;
                String batchNo = null;

                ProductBatchNo productBatchNo = null;

                LevelA levelA = null;
                LevelB levelB = null;
                LevelC levelC = null;
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
//                if (type.equalsIgnoreCase("create")) {
//                    orderUnits = new TranxSalesQuotationDetailsUnits();
//                    orderUnits.setStatus(true);
//                } else {
//                    Long detailsId = object.get("details_id").getAsLong();
//                    if (detailsId != 0) {
//                        orderUnits = tranxSalesQuotaionDetailsUnitsRepository.findByIdAndStatus(detailsId, true);
//                        if (orderUnits != null) {
//                        }
//                    } else {
//                        orderUnits = new TranxSalesQuotationDetailsUnits();
//                        orderUnits.setStatus(true);
//                    }
//                }
                orderUnits.setSalesQuotation(mTranxSalesQuatation);
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
                orderUnits.setDiscountBInPer(object.get("dis_per2").getAsDouble());
                orderUnits.setTotalDiscountInAmt(object.get("row_dis_amt").getAsDouble());
                orderUnits.setGrossAmt(object.get("gross_amt").getAsDouble());
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
                tranxSalesQuotaionDetailsUnitsRepository.save(orderUnits);

            }

        }
        JsonParser parser = new JsonParser();
        JsonElement salesDetailsJson;
        if (!rowsDeleted.equalsIgnoreCase("")) {
            salesDetailsJson = parser.parse(rowsDeleted);
            JsonArray deletedArrays = salesDetailsJson.getAsJsonArray();
            if (deletedArrays.size() > 0) {
                TranxSalesQuotationDetailsUnits mDeletedInvoices = null;
                for (JsonElement element : deletedArrays) {
                    JsonObject deletedRowsId = element.getAsJsonObject();
                    mDeletedInvoices = tranxSalesQuotaionDetailsUnitsRepository.findByIdAndStatus(
                            deletedRowsId.get("del_id").getAsLong(), true);
                    if (mDeletedInvoices != null) {
                        mDeletedInvoices.setStatus(false);
                        try {
                            tranxSalesQuotaionDetailsUnitsRepository.save(mDeletedInvoices);
                            /***** inventory effects of deleted rows *****/

                            /***** End of new architecture of Inventory Postings *****/
                        } catch (DataIntegrityViolationException de) {
                            salesQuatationLogger.error("Error in saveInto Sales Invoice Details Edit" + de.getMessage());
                            de.printStackTrace();
                            System.out.println("Exception:" + de.getMessage());

                            salesQuatationLogger.error("Error in saveInto Sales Invoice Details Edit" + de.getMessage());
                            de.printStackTrace();
                            System.out.println("Exception save Into Sales Invoice Details Edit:" + de.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void saveIntosalesQuotationDutiesTaxesEdit(JsonObject duties_taxes,
                                                       TranxSalesQuotation mTranxSalesQuatation,
                                                       Boolean taxFlag, TransactionTypeMaster tranxType, Long outletId, Long user_id) {
        /* Sales Duties and Taxes */
        List<TranxSalesQuotationDutiesTaxes> salesQuotationDutiesTaxes = new ArrayList<>();
        /*** getting duties_taxes_ledger_id  ***/
        List<Long> db_dutiesLedgerIds = salesQuotationDutiesTaxesRepository.findByDutiesAndTaxesId(mTranxSalesQuatation.getId());
        List<Long> input_dutiesLedgerIds = getInputLedgerIds(taxFlag, duties_taxes, outletId,
                mTranxSalesQuatation.getBranch() != null ? mTranxSalesQuatation.getBranch().getId() : null);
        List<Long> travelArray = CustomArrayUtilities.getTwoArrayMergeUnique(db_dutiesLedgerIds, input_dutiesLedgerIds);
//                System.out.println("travelArray"+travelArray);
        List<Long> travelledArray = new ArrayList();
        if (travelArray.size() > 0) {
            //Updation into Purchase Duties and Taxes
            if (db_dutiesLedgerIds.size() > 0) {
                //insert old records in history
                salesQuotationDutiesTaxes = salesQuotationDutiesTaxesRepository.findBySalesQuotationTransactionIdAndStatus(mTranxSalesQuatation.getId(), true);
                //  insertIntoDutiesAndTaxesHistory(salesQuotationDutiesTaxes);
            }
            if (taxFlag) {
                JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
                JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
                /* this is for Cgst creation */
                if (cgstList.size() > 0) {
                    for (JsonElement mCgst : cgstList) {
                        TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //    int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT CGST " + inputGst;
                        //   dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (mTranxSalesQuatation.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mTranxSalesQuatation.getOutlet().getId(), mTranxSalesQuatation.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mTranxSalesQuatation.getOutlet().getId(), ledgerName);

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
                        taxes.setSalesQuotationTransaction(mTranxSalesQuatation);
                        taxes.setSundryDebtors(mTranxSalesQuatation.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesQuotationDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //  int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        //    dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (mTranxSalesQuatation.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mTranxSalesQuatation.getOutlet().getId(), mTranxSalesQuatation.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mTranxSalesQuatation.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());

                        }
                        taxes.setAmount(amt);
                        taxes.setSalesQuotationTransaction(mTranxSalesQuatation);
                        taxes.setSundryDebtors(mTranxSalesQuatation.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesQuotationDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList!=null && igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();
                        JsonObject igstObject = mIgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        // dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (mTranxSalesQuatation.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mTranxSalesQuatation.getOutlet().getId(), mTranxSalesQuatation.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mTranxSalesQuatation.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                            travelledArray.add(dutiesTaxes.getId());
                            Boolean isContains = dbList.contains(dutiesTaxes.getId());
                            mInputList.add(dutiesTaxes.getId());

                        }
                        taxes.setAmount(amt);
                        taxes.setSalesQuotationTransaction(mTranxSalesQuatation);
                        taxes.setSundryDebtors(mTranxSalesQuatation.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        taxes.setStatus(true);
                        salesQuotationDutiesTaxes.add(taxes);
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
                        TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();
                        JsonObject cgstObject = mCgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) cgstObject.get("gst").getAsDouble();
                        String inputGst = cgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT CGST " + inputGst;
                        Double amt = cgstObject.get("amt").getAsDouble();
                        //  dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (mTranxSalesQuatation.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mTranxSalesQuatation.getOutlet().getId(), mTranxSalesQuatation.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mTranxSalesQuatation.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            //   dutiesTaxesLedger.setDutiesTaxes(dutiesTaxes);
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesQuotationTransaction(mTranxSalesQuatation);
                        taxes.setSundryDebtors(mTranxSalesQuatation.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesQuotationDutiesTaxes.add(taxes);
                    }
                }
                /* this is for Sgst creation */
                if (sgstList.size() > 0) {
                    for (JsonElement mSgst : sgstList) {
                        TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();
                        JsonObject sgstObject = mSgst.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        //int inputGst = (int) sgstObject.get("gst").getAsDouble();
                        String inputGst = sgstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT SGST " + inputGst;
                        Double amt = sgstObject.get("amt").getAsDouble();
                        //dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (mTranxSalesQuatation.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mTranxSalesQuatation.getOutlet().getId(), mTranxSalesQuatation.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mTranxSalesQuatation.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesQuotationTransaction(mTranxSalesQuatation);
                        taxes.setSundryDebtors(mTranxSalesQuatation.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesQuotationDutiesTaxes.add(taxes);
                    }
                }
            } else {
                JsonArray igstList = duties_taxes.getAsJsonArray("igst");
                /* this is for Igst creation */
                if (igstList!=null && igstList.size() > 0) {
                    for (JsonElement mIgst : igstList) {
                        TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();
                        JsonObject igstObject = igstList.getAsJsonObject();
                        LedgerMaster dutiesTaxes = null;
                        // int inputGst = (int) igstObject.get("gst").getAsDouble();
                        String inputGst = igstObject.get("gst").getAsString();
                        String ledgerName = "OUTPUT IGST " + inputGst;
                        Double amt = igstObject.get("amt").getAsDouble();
                        //       dutiesTaxes = ledgerMasterRepository.findByLedgerNameIgnoreCaseAndOutletId(ledgerName, outletId);
                        if (mTranxSalesQuatation.getBranch() != null)
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(mTranxSalesQuatation.getOutlet().getId(), mTranxSalesQuatation.getBranch().getId(), ledgerName);
                        else
                            dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(mTranxSalesQuatation.getOutlet().getId(), ledgerName);

                        if (dutiesTaxes != null) {
                            taxes.setDutiesTaxes(dutiesTaxes);
                        }
                        taxes.setAmount(amt);
                        taxes.setSalesQuotationTransaction(mTranxSalesQuatation);
                        taxes.setSundryDebtors(mTranxSalesQuatation.getSundryDebtors());
                        taxes.setIntra(taxFlag);
                        salesQuotationDutiesTaxes.add(taxes);
                    }
                }
            }
        }
        salesQuotationDutiesTaxesRepository.saveAll(salesQuotationDutiesTaxes);
    }

    private void insertIntoDutiesAndTaxesHistory(List<TranxSalesQuotationDutiesTaxes> salesDutiesTaxes) {
        for (TranxSalesQuotationDutiesTaxes mList : salesDutiesTaxes) {
            mList.setStatus(false);
            salesQuotationDutiesTaxesRepository.save(mList);
        }
    }

    /*  private List<Long> getInputLedgerIds(Boolean taxFlag, JsonObject duties_taxes, Long outletId) {
          List<Long> returnLedgerIds = new ArrayList<>();
          if (taxFlag) {
              JsonArray cgstList = duties_taxes.getAsJsonArray("cgst");
              JsonArray sgstList = duties_taxes.getAsJsonArray("sgst");
              *//* this is for Cgst creation *//*
            if (cgstList.size() > 0) {
                for (JsonElement mCgst : cgstList) {
                    TranxSalesQuotationDutiesTaxes taxes = new TranxSalesQuotationDutiesTaxes();
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
                    TranxPurInvoiceDutiesTaxes taxes = new TranxPurInvoiceDutiesTaxes();
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
                    JsonObject cgstObject = mCgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) cgstObject.get("gst").getAsDouble();
                    String inputGst = cgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT CGST " + inputGst;
                    if (branchId != null)
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCase(outletId, branchId, ledgerName);
                    else
                        dutiesTaxes = ledgerMasterRepository.findByOutletIdAndLedgerNameIgnoreCaseAndBranchIsNull(outletId, ledgerName);
                    if (dutiesTaxes != null) {
                        returnLedgerIds.add(dutiesTaxes.getId());
                    }
                }
            }
            /* this is for Sgst creation */
            if (sgstList.size() > 0) {
                for (JsonElement mSgst : sgstList) {
                    /*   TranxSalesReturnInvoiceDutiesTaxes taxes = new TranxSalesReturnInvoiceDutiesTaxes();*/
                    JsonObject sgstObject = mSgst.getAsJsonObject();
                    LedgerMaster dutiesTaxes = null;
                    //int inputGst = (int) sgstObject.get("gst").getAsDouble();
                    String inputGst = sgstObject.get("gst").getAsString();
                    String ledgerName = "INPUT SGST " + inputGst;
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
        TranxSalesQuotation invoiceTranx = tranxSalesQuotationRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        List<Object[]> productIds = new ArrayList<>();
        productIds = tranxSalesQuotaionDetailsUnitsRepository.findByTranxPurId(invoiceTranx.getId(), true);
        // productArray = productData.getProductByBFPUCommon(invoiceTranx.getBillDate(), productIds);
        productArray = productData.getProductByBFPUCommonNew(invoiceTranx.getBillDate(), productIds);

        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getProductEditByIdsByFPU(HttpServletRequest request) {
        JsonArray productArray = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String str = request.getParameter("sale_quotation_ids");
        JsonParser parser = new JsonParser();
        JsonElement sqDetailsJson = parser.parse(str);
        JsonArray jsonArray = sqDetailsJson.getAsJsonArray();
        JsonObject result = new JsonObject();
        for (JsonElement msalesQuatation : jsonArray) {
            JsonObject object = msalesQuatation.getAsJsonObject();
            TranxSalesQuotation quotationTranx = tranxSalesQuotationRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
            List<Object[]> productIds = new ArrayList<>();
            productIds = tranxSalesQuotaionDetailsUnitsRepository.findByTranxPurId(quotationTranx.getId(), true);
            Long fiscalId = null;
            LocalDate currentDate = LocalDate.now();
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
            if (fiscalYear != null) fiscalId = fiscalYear.getId();

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
                                            Double closing = inventoryCommonPostings.getClosingStockProductFilters(
                                                    mBatch.getBranch() != null ? mBatch.getBranch().getId() : null,
                                                    mBatch.getOutlet().getId(), mProduct.getId(), levelAId, levelBId,
                                                    levelCId, unitId, mBatch.getId(), fiscalId);

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
                                            batchObject.addProperty("closing_stock", closing != null ? closing : 0.00);
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
                                                if (DateConvertUtil.convertDateToLocalDate(
                                                        quotationTranx.getBillDate()).isAfter(mBatch.getExpiryDate())) {
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
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("productIds", productArray);
        return output;
    }

    public JsonObject getSalesQuotationByIdNew(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesQuotationDetails> list = new ArrayList<>();
        JsonArray units = new JsonArray();
        JsonObject finalResult = new JsonObject();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            double totalDis_amt = 0.0, total_tax = 0.0;
            TranxSalesQuotation tranxSalesQuotation = tranxSalesQuotationRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
            list = salesQuotationInvoiceDetailsRepository.findByTranxSalesQuotationIdAndStatus(id, true);
            finalResult.addProperty("tcs", tranxSalesQuotation.getTcs());
            finalResult.addProperty("narration", tranxSalesQuotation.getNarration() != null ? tranxSalesQuotation.getNarration() : "");
            JsonObject result = new JsonObject();
            /* Purchase Order Data */
            result.addProperty("id", tranxSalesQuotation.getId());
            result.addProperty("sales_sr_no", tranxSalesQuotation.getSalesQuotationSrNo());
            result.addProperty("sales_account_id", tranxSalesQuotation.getSalesAccountLedger().getId());
            result.addProperty("sales_account", tranxSalesQuotation.getSalesAccountLedger().getLedgerName());
            result.addProperty("bill_date", DateConvertUtil.convertDateToLocalDate(tranxSalesQuotation.getBillDate()).toString());
            result.addProperty("sq_bill_no", tranxSalesQuotation.getSq_bill_no());
            result.addProperty("tranx_unique_code", tranxSalesQuotation.getTranxCode());
            result.addProperty("round_off", tranxSalesQuotation.getRoundOff());
            result.addProperty("total_base_amount", tranxSalesQuotation.getTotalBaseAmount());
            result.addProperty("total_amount", tranxSalesQuotation.getTotalAmount());
            result.addProperty("total_cgst", tranxSalesQuotation.getTotalcgst());
            result.addProperty("total_sgst", tranxSalesQuotation.getTotalsgst());
            result.addProperty("total_igst", tranxSalesQuotation.getTotaligst());
            result.addProperty("total_qty", tranxSalesQuotation.getTotalqty());
            result.addProperty("taxable_amount", tranxSalesQuotation.getTaxableAmount());
            result.addProperty("tcs", tranxSalesQuotation.getTcs());
            result.addProperty("status", tranxSalesQuotation.getStatus());
            result.addProperty("financial_year", tranxSalesQuotation.getFinancialYear());
            result.addProperty("debtor_id", tranxSalesQuotation.getSundryDebtors().getId());
            result.addProperty("debtor_name", tranxSalesQuotation.getSundryDebtors().getLedgerName());
            result.addProperty("ledgerStateCode", tranxSalesQuotation.getSundryDebtors().getStateCode());
            result.addProperty("narration", tranxSalesQuotation.getNarration());
            result.addProperty("gstNo", tranxSalesQuotation.getGstNumber() != null ? tranxSalesQuotation.getGstNumber().toString() : "");

            /* End of Sales Quotations Data */
            /* Sales Quotations Details */
            JsonArray row = new JsonArray();
            JsonArray unitsJsonArray = new JsonArray();
            List<TranxSalesQuotationDetailsUnits> unitsArray =
                    tranxSalesQuotaionDetailsUnitsRepository.findBySalesQuotationIdAndTransactionStatusAndStatus(
                            tranxSalesQuotation.getId(), 1L, true);

            for (TranxSalesQuotationDetailsUnits mUnits : unitsArray) {
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
                totalDis_amt = totalDis_amt + mUnits.getTotalDiscountInAmt();
                total_tax = total_tax + mUnits.getTotalIgst();
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
                result.addProperty("totalDis_amt", totalDis_amt);
                result.addProperty("total_tax", total_tax);

                row.add(unitsJsonObjects);
            }
//            gstDetails

            List<LedgerGstDetails> gstDetails = new ArrayList<>();
            gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(tranxSalesQuotation.getSundryDebtors().getId(), true);
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
            salesQuatationLogger.error("Error in getSalesQuotationByIdNew" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            salesQuatationLogger.error("Error in getSalesQuotationByIdNew" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }


    //    public JsonObject getSalesQuotationByIdNew(HttpServletRequest request) {
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        List<TranxPurchaseInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
//        List<TranxPurInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
//        JsonObject finalResult = new JsonObject();
//        JsonObject result = new JsonObject();
//        try {
//            Long id = Long.parseLong(request.getParameter("id"));
//            TranxSalesQuotation tranxSalesQuotation = tranxSalesQuotationRepository.findByIdAndOutletIdAndStatus(id, users.getOutlet().getId(), true);
//            if (tranxSalesQuotation != null) {
//                serialNumbers = serialNumberRepository.findByPurchaseTransactionId(tranxSalesQuotation.getId());
////                additionalCharges = purInvoiceAdditionalChargesRepository.findByPurchaseTransactionIdAndStatus(tranxSalesQuotation.getId(), true);
//                finalResult.addProperty("tcs", tranxSalesQuotation.getTcs());
//                finalResult.addProperty("narration", tranxSalesQuotation.getNarration() != null ? tranxSalesQuotation.getNarration() : "");
//                finalResult.addProperty("discountLedgerId", tranxSalesQuotation.getPurchaseDiscountLedger() != null ? tranxSalesQuotation.getPurchaseDiscountLedger().getId() : 0);
//                finalResult.addProperty("discountInAmt", tranxSalesQuotation.getPurchaseDiscountAmount());
//                finalResult.addProperty("discountInPer", tranxSalesQuotation.getPurchaseDiscountPer());
//                finalResult.addProperty("totalPurchaseDiscountAmt", tranxSalesQuotation.getTotalPurchaseDiscountAmt());
//                finalResult.addProperty("totalQty", tranxSalesQuotation.getTotalqty());
//                finalResult.addProperty("totalFreeQty", tranxSalesQuotation.getFreeQty());
//                finalResult.addProperty("grossTotal", tranxSalesQuotation.getGrossAmount());
//                finalResult.addProperty("totalTax", tranxSalesQuotation.getTotalTax());
////                finalResult.addProperty("additionLedger1", purchaseInvoice.getAdditionLedger1() != null ?
////                        purchaseInvoice.getAdditionLedger1().getId() : 0);
////                finalResult.addProperty("additionLedgerAmt1", purchaseInvoice.getAdditionLedgerAmt1() != null ?
////                        purchaseInvoice.getAdditionLedgerAmt1() : 0);
////                finalResult.addProperty("additionLedger2", purchaseInvoice.getAdditionLedger2() != null ?
////                        purchaseInvoice.getAdditionLedger2().getId() : 0);
////                finalResult.addProperty("additionLedgerAmt2", purchaseInvoice.getAdditionLedgerAmt2() != null ?
////                        purchaseInvoice.getAdditionLedgerAmt2() : 0);
////                finalResult.addProperty("additionLedger3", purchaseInvoice.getAdditionLedger3() != null ?
////                        purchaseInvoice.getAdditionLedger3().getId() : 0);
////                finalResult.addProperty("additionLedgerAmt3", purchaseInvoice.getAdditionLedgerAmt3() != null ?
////                        purchaseInvoice.getAdditionLedgerAmt3() : 0);
//
//                /* Purchase Invoice Data */
//                result.addProperty("id", tranxSalesQuotation.getId());
//                result.addProperty("invoice_dt", tranxSalesQuotation.getInvoiceDate().toString());
//                result.addProperty("invoice_no", tranxSalesQuotation.getVendorInvoiceNo().toString());
//                result.addProperty("purchase_sr_no", tranxSalesQuotation.getSrno());
//                result.addProperty("purchase_account_ledger_id", tranxSalesQuotation.getPurchaseAccountLedger().getId());
//                result.addProperty("supplierId", tranxSalesQuotation.getSundryCreditors().getId());
//                result.addProperty("transaction_dt", tranxSalesQuotation.getTransactionDate().toString());
//                result.addProperty("additional_charges_total", tranxSalesQuotation.getAdditionalChargesTotal());
//                result.addProperty("gstNo", tranxSalesQuotation.getGstNumber() != null ? purchaseInvoice.getGstNumber() : "");
//                /* End of Purchase Invoice Data */
//            }
//
//            /* Purchase Invoice Details */
//            JsonArray row = new JsonArray();
//            JsonArray unitsJsonArray = new JsonArray();
//            List<TranxPurInvoiceDetailsUnits> unitsArray =
//                    tranxSalesQuotaionDetailsUnitsRepository.findByPurchaseTransactionIdAndStatus(purchaseInvoice.getId(), true);
//            for (TranxPurInvoiceDetailsUnits mUnits : unitsArray) {
//                JsonObject unitsJsonObjects = new JsonObject();
//                unitsJsonObjects.addProperty("details_id", mUnits.getId());
//                unitsJsonObjects.addProperty("product_id", mUnits.getProduct().getId());
//                unitsJsonObjects.addProperty("product_name", mUnits.getProduct().getProductName());
//                unitsJsonObjects.addProperty("level_a_id", mUnits.getLevelA() != null ?
//                        mUnits.getLevelA().getId().toString() : "");
//                unitsJsonObjects.addProperty("level_b_id", mUnits.getLevelB() != null ?
//                        mUnits.getLevelB().getId().toString() : "");
//                unitsJsonObjects.addProperty("level_c_id", mUnits.getLevelC() != null ?
//                        mUnits.getLevelC().getId().toString() : "");
//                unitsJsonObjects.addProperty("unit_name", mUnits.getUnits().getUnitName());
//                unitsJsonObjects.addProperty("unitId", mUnits.getUnits().getId());
//                unitsJsonObjects.addProperty("unit_conv", mUnits.getUnitConversions());
//                unitsJsonObjects.addProperty("qty", mUnits.getQty());
//                unitsJsonObjects.addProperty("rate", mUnits.getRate());
//                unitsJsonObjects.addProperty("base_amt", mUnits.getBaseAmt());
//                unitsJsonObjects.addProperty("dis_amt", mUnits.getDiscountAmount());
//                unitsJsonObjects.addProperty("dis_per", mUnits.getDiscountPer());
//                unitsJsonObjects.addProperty("dis_per_cal", mUnits.getDiscountPerCal());
//                unitsJsonObjects.addProperty("dis_amt_cal", mUnits.getDiscountAmountCal());
//                unitsJsonObjects.addProperty("total_amt", mUnits.getTotalAmount());
//                unitsJsonObjects.addProperty("gst", mUnits.getIgst());
//                unitsJsonObjects.addProperty("igst", mUnits.getIgst());
//                unitsJsonObjects.addProperty("cgst", mUnits.getCgst());
//                unitsJsonObjects.addProperty("sgst", mUnits.getSgst());
//                unitsJsonObjects.addProperty("total_igst", mUnits.getTotalIgst());
//                unitsJsonObjects.addProperty("total_cgst", mUnits.getTotalCgst());
//                unitsJsonObjects.addProperty("total_sgst", mUnits.getTotalSgst());
//                unitsJsonObjects.addProperty("final_amt", mUnits.getFinalAmount());
//                unitsJsonObjects.addProperty("free_qty", mUnits.getFreeQty());
//                unitsJsonObjects.addProperty("dis_per2", mUnits.getDiscountBInPer());
//                unitsJsonObjects.addProperty("row_dis_amt", mUnits.getTotalDiscountInAmt());
//                unitsJsonObjects.addProperty("gross_amt", mUnits.getGrossAmt());
//                unitsJsonObjects.addProperty("add_chg_amt", mUnits.getAdditionChargesAmt());
//                unitsJsonObjects.addProperty("grossAmt1", mUnits.getGrossAmt1());
//                unitsJsonObjects.addProperty("invoice_dis_amt", mUnits.getInvoiceDisAmt());
//                if (mUnits.getProductBatchNo() != null) {
//                    if (purchaseInvoice.getInvoiceDate().isAfter(mUnits.getProductBatchNo().getExpiryDate())) {
//                        unitsJsonObjects.addProperty("is_expired", true);
//                    } else {
//                        unitsJsonObjects.addProperty("is_expired", false);
//                    }
//                    unitsJsonObjects.addProperty("b_detailsId", mUnits.getProductBatchNo().getId());
//                    unitsJsonObjects.addProperty("batch_no", mUnits.getProductBatchNo().getBatchNo());
//                    unitsJsonObjects.addProperty("b_expiry", mUnits.getProductBatchNo().getExpiryDate() != null ? mUnits.getProductBatchNo().getExpiryDate().toString() : "");
//                    unitsJsonObjects.addProperty("purchase_rate", mUnits.getProductBatchNo().getPurchaseRate());
//                    unitsJsonObjects.addProperty("is_batch", true);
//                    unitsJsonObjects.addProperty("min_rate_a", mUnits.getProductBatchNo().getMinRateA());
//                    unitsJsonObjects.addProperty("min_rate_b", mUnits.getProductBatchNo().getMinRateB());
//                    unitsJsonObjects.addProperty("min_rate_c", mUnits.getProductBatchNo().getMinRateC());
//                    unitsJsonObjects.addProperty("min_discount", mUnits.getProductBatchNo().getMinDiscount());
//                    unitsJsonObjects.addProperty("max_discount", mUnits.getProductBatchNo().getMaxDiscount());
//                    unitsJsonObjects.addProperty("manufacturing_date", mUnits.getProductBatchNo().getManufacturingDate() != null ? mUnits.getProductBatchNo().getManufacturingDate().toString() : "");
//                    unitsJsonObjects.addProperty("min_margin", mUnits.getProductBatchNo().getMinMargin());
//                    unitsJsonObjects.addProperty("b_rate", mUnits.getProductBatchNo().getMrp());
//                    unitsJsonObjects.addProperty("sales_rate", mUnits.getProductBatchNo().getSalesRate());
//                    unitsJsonObjects.addProperty("costing", mUnits.getProductBatchNo().getCosting());
//                    unitsJsonObjects.addProperty("costingWithTax", mUnits.getProductBatchNo().getCostingWithTax());
//                } else {
//                    unitsJsonObjects.addProperty("b_detailsId", "");
//                    unitsJsonObjects.addProperty("batch_no", "");
//                    unitsJsonObjects.addProperty("b_expiry", "");
//                    unitsJsonObjects.addProperty("purchase_rate", "");
//                    unitsJsonObjects.addProperty("is_batch", "");
//                    unitsJsonObjects.addProperty("min_rate_a", "");
//                    unitsJsonObjects.addProperty("min_rate_b", "");
//                    unitsJsonObjects.addProperty("min_rate_c", "");
//                    unitsJsonObjects.addProperty("min_discount", "");
//                    unitsJsonObjects.addProperty("max_discount", "");
//                    unitsJsonObjects.addProperty("manufacturing_date", "");
//                    unitsJsonObjects.addProperty("mrp", "");
//                    unitsJsonObjects.addProperty("min_margin", "");
//                    unitsJsonObjects.addProperty("b_rate", "");
//                    unitsJsonObjects.addProperty("costing", "");
//                    unitsJsonObjects.addProperty("costingWithTax", "");
//                }
//
//                JsonArray mLevelArray = new JsonArray();
//                List<Long> levelaArray = productUnitRepository.findLevelAIdDistinct(mUnits.getProduct().getId());
//                for (Long mLeveA : levelaArray) {
//                    JsonObject levelaJsonObject = new JsonObject();
//                    LevelA levelA = null;
//                    if (mLeveA != null) {
//                        levelA = levelARepository.findByIdAndStatus(mLeveA, true);
//                        if (levelA != null) {
//                            levelaJsonObject.addProperty("levela_id", levelA.getId());
//                            levelaJsonObject.addProperty("levela_name", levelA.getLevelName());
//                        }
//                    }
//                    mLevelArray.add(levelaJsonObject);
//                }
//                unitsJsonObjects.add("levelAOpt", mLevelArray);
//                row.add(unitsJsonObjects);
//            }
//            /* End of Purchase Invoice Details */
//            System.out.println("Row  " + row);
//            finalResult.add("row", row);
//            finalResult.add("invoice_data", result);
//            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
//
//        } catch (DataIntegrityViolationException e) {
//            e.printStackTrace();
//            purInvoiceLogger.error("Error in getPurchaseInvoiceById" + e.getMessage());
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//            finalResult.addProperty("message", "error");
//            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
//        } catch (Exception e1) {
//            e1.printStackTrace();
//            purInvoiceLogger.error("Error in getPurchaseInvoiceById" + e1.getMessage());
//            System.out.println(e1.getMessage());
//            e1.printStackTrace();
//            finalResult.addProperty("message", "error");
//            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
//        }
//        return finalResult;
//    }
    public JsonObject getSalesQuotationSupplierListByProductId(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        Long product = Long.parseLong(request.getParameter("productId"));

        List<TranxSalesQuotationDetailsUnits> tranxSalesReturnDetailsUnits = tranxSalesQuotaionDetailsUnitsRepository.findByProductIdAndStatusOrderByIdDesc(product, true);

        for (TranxSalesQuotationDetailsUnits obj : tranxSalesReturnDetailsUnits) {
            JsonObject response = new JsonObject();
            response.addProperty("supplier_name", obj.getSalesQuotation().getSundryDebtors().getLedgerName());
            response.addProperty("invoice_no", obj.getSalesQuotation().getId());
//        response.addProperty("invoice_date",obj.getSalesQuotation().getBillDate().toString());
//            response.addProperty("batch",obj.getProductBatchNo().getBatchNo());
            response.addProperty("mrp", obj.getProduct().getIsBatchNumber());
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

    public JsonObject getQuotationBillPrint(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxSalesQuotationDetailsUnits> list = new ArrayList<>();
        List<TranxCounterSalesDetailsUnits> listcs = new ArrayList<>();
        List<TranxSalesInvoiceProductSrNumber> serialNumbers = new ArrayList<>();
        List<TranxSalesInvoiceAdditionalCharges> additionalCharges = new ArrayList<>();
        JsonObject finalResult = new JsonObject();
        TranxSalesQuotation salesQuotation = null;
        TranxCounterSales counterSales = null;
        Long id = 0L;
        String source = request.getParameter("source");
        String key = request.getParameter("print_type"); //check whether printbill is calling from create page or from list page
        /***** if  print_type is create, then use serialnumber/ invoice_no of invoice to fetch invoice details ,
         * if print_type is list then use invoice id to fetch invoice details *****/
        try {
            String invoiceNo = request.getParameter("id");
            if (source.equalsIgnoreCase("sales_quotation")) { //counter_sales
                if (users.getBranch() != null) {
                    if (key.equalsIgnoreCase("create")) {
                        salesQuotation = tranxSalesQuotationRepository.findSqNoWithBranch(users.getOutlet().getId(), users.getBranch().getId(), invoiceNo);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesQuotation = tranxSalesQuotationRepository.findByIdAndOutletIdAndBranchIdAndStatus(id, users.getOutlet().getId(), users.getBranch().getId(), true);
                    }
                } else {
                    if (key.equalsIgnoreCase("create")) {
                        salesQuotation = tranxSalesQuotationRepository.findSqNo(users.getOutlet().getId(), invoiceNo);
                    } else {
                        id = Long.parseLong(invoiceNo);
                        salesQuotation = tranxSalesQuotationRepository.findByIdAndOutletIdAndStatusAndBranchIsNull(id, users.getOutlet().getId(), true);
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
                invoiceObject.addProperty("invoice_no", salesQuotation.getSalesQuotationSrNo());
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
            salesQuatationLogger.error("Error in getInvoiceBillPrint :->" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            salesQuatationLogger.error("Error in getInvoiceBillPrint :->" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject sQuotationPendingList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), true);
        List<TranxSalesQuotation> tranxSalesQuotations = tranxSalesQuotationRepository.findBySundryDebtorsIdAndStatusAndTransactionStatusId(sundryDebtors.getId(), true, 1L);
        for (TranxSalesQuotation invoices : tranxSalesQuotations) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("bill_no", invoices.getSq_bill_no());
            response.addProperty("bill_date", invoices.getBillDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount().toString());
            response.addProperty("total_base_amount", invoices.getTotalBaseAmount().toString());
            response.addProperty("sundry_debtors_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtors_id", invoices.getSundryDebtors().getId());
            response.addProperty("sales_quotation_status", invoices.getTransactionStatus().getStatusName());
            response.addProperty("sales_account", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration());
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

    public JsonObject saleQuotationPendingList(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        LedgerMaster sundryDebtors = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("supplier_code_id")), true);
        List<TranxSalesQuotation> tranxSalesQuotation = tranxSalesQuotationRepository.findBySundryDebtorsIdAndStatusAndTransactionStatusId(sundryDebtors.getId(), true, 1L);
        for (TranxSalesQuotation invoices : tranxSalesQuotation) {
            JsonObject response = new JsonObject();
            response.addProperty("id", invoices.getId());
            response.addProperty("bill_no", invoices.getSq_bill_no());
            response.addProperty("bill_date", invoices.getBillDate().toString());
            response.addProperty("total_amount", invoices.getTotalAmount());
            response.addProperty("total_base_amount", invoices.getTotalBaseAmount());
            response.addProperty("sundry_debtors_name", invoices.getSundryDebtors().getLedgerName());
            response.addProperty("sundry_debtors_id", invoices.getSundryDebtors().getId());
            response.addProperty("sales_challan_status", invoices.getTransactionStatus().getStatusName());
            response.addProperty("sale_account_name", invoices.getSalesAccountLedger().getLedgerName());
            response.addProperty("narration", invoices.getNarration());
            response.addProperty("tax_amt", invoices.getTotalTax() != null ? invoices.getTotalTax() : 0.0);
            response.addProperty("taxable_amt", invoices.getTaxableAmount());
            result.add(response);
        }
        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    public Object validateSalesQuotation(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String[]> paramMap = request.getParameterMap();
        TranxSalesQuotation salesQuotation = null;
        if (users.getBranch() != null) {
            salesQuotation = tranxSalesQuotationRepository.findSqNoWithBranch(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesQuotationNo"));
        } else {
            salesQuotation = tranxSalesQuotationRepository.findSqNo(users.getOutlet().getId(), request.getParameter("salesQuotationNo"));
        }
        if (salesQuotation != null) {
            responseMessage.setMessage("Duplicate sales quotation number exists");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales invoice number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object validateSalesQuotationUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        TranxSalesQuotation salesQuotation = null;
        Long invoiceId = Long.parseLong(request.getParameter("invoice_id"));
        if (users.getBranch() != null) {
            salesQuotation = tranxSalesQuotationRepository.findSqNoWithBranch(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("salesQuotationNo"));
        } else {
            salesQuotation = tranxSalesQuotationRepository.findSqNo(users.getOutlet().getId(), request.getParameter("salesQuotationNo"));
        }
        if (salesQuotation != null && invoiceId != salesQuotation.getId()) {
            responseMessage.setMessage("Duplicate sales quotation number");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New sales quotation number");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }
}

