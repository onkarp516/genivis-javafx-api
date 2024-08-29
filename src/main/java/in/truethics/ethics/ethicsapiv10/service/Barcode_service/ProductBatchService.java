package in.truethics.ethics.ethicsapiv10.service.Barcode_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.InventoryCommonPostings;
import in.truethics.ethics.ethicsapiv10.common.UnitConversion;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductOpeningStocksRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.LevelARepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.LevelBRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.LevelCRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurChallanDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceDetailsUnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesChallanDetailsUnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.servlet.oauth2.client.OAuth2ClientSecurityMarker;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ProductBatchService {
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private TranxPurInvoiceDetailsUnitsRepository tranxPurInvoiceDetailsUnitsRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductOpeningStocksRepository productOpeningStocksRepository;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private FiscalYearRepository fiscalYearRepository;
    @Autowired
    private TranxSalesInvoiceDetailsUnitRepository tranxSalesInvoiceDetailsUnitRepository;
    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private TranxSalesChallanDetailsUnitsRepository tranxSalesChallanDetailsUnitsRepository;
    @Autowired
    private TranxPurChallanDetailsUnitRepository purChallanDetailsUnitRepository;
    @Autowired
    private UnitConversion unitConversion;

    public ProductBatchService() {
    }

    public JsonObject getProductBatch(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonArray batchArray = new JsonArray();
        JsonObject result = new JsonObject();
        Long level_a_id = null;
        Long level_b_id = null;
        Long level_c_id = null;
     /*   Long sub_category_id = null;
        Long package_id = null;*/
        Long unit_id = null;
        Long branchId = null;
        if (users.getBranch() != null) {
            branchId = users.getBranch().getId();
        }
        Long fiscalId = null;
        LocalDate currentDate = LocalDate.now();
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        if (fiscalYear != null) fiscalId = fiscalYear.getId();
        Long product_id = Long.parseLong(request.getParameter("product_id"));
        if (paramMap.containsKey("level_a_id") && !request.getParameter("level_a_id").equalsIgnoreCase(""))
            level_a_id = Long.parseLong(request.getParameter("level_a_id"));
        if (paramMap.containsKey("level_b_id") && !request.getParameter("level_b_id").equalsIgnoreCase(""))
            level_b_id = Long.parseLong(request.getParameter("level_b_id"));
        if (paramMap.containsKey("level_c_id") && !request.getParameter("level_c_id").equalsIgnoreCase(""))
            level_c_id = Long.parseLong(request.getParameter("level_c_id"));
        if (paramMap.containsKey("unit_id")) unit_id = Long.parseLong(request.getParameter("unit_id"));

        ProductUnitPacking unitPack = productUnitRepository.findRate(product_id, level_a_id, level_b_id,
                level_c_id, unit_id, true);
        LocalDate invoiceDate = LocalDate.parse(request.getParameter("invoice_date"));
        List<ProductBatchNo> productbatch = productBatchNoRepository.findByBatchList(product_id, users.getOutlet().getId(),
                true, level_a_id, level_b_id, level_c_id);
        if (productbatch != null && productbatch.size() > 0) {
            for (ProductBatchNo mBatch : productbatch) {
                Double opening = productOpeningStocksRepository.findSumProductOpeningStocksBatchwise(mBatch.getProduct().getId(), mBatch.getOutlet().getId(), mBatch.getBranch() != null ? mBatch.getBranch().getId() : null, fiscalId, mBatch.getId());
                Double free_qnt = productOpeningStocksRepository.findSumProductFreeQtyBatchwise(mBatch.getProduct().getId(), mBatch.getOutlet().getId(), mBatch.getBranch() != null ? mBatch.getBranch().getId() : null, fiscalId, mBatch.getId());
                Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(), product_id, level_a_id, level_b_id, level_c_id, unit_id, mBatch.getId(), fiscalId);
                JsonObject object = new JsonObject();
                if (mBatch.getExpiryDate() != null) {
                    if (invoiceDate.isAfter(mBatch.getExpiryDate())) {
                        object.addProperty("is_expired", true);
                    } else {
                        object.addProperty("is_expired", false);
                    }
                } else {
                    object.addProperty("is_expired", false);
                }
                object.addProperty("id", mBatch.getId());
                object.addProperty("b_details_id", mBatch.getId());
                object.addProperty("org_b_details_id", mBatch.getId());
                object.addProperty("product_name", mBatch.getProduct().getProductName());
                object.addProperty("batch_no", mBatch.getBatchNo());
                object.addProperty("qty", mBatch.getQnty() != null ? mBatch.getQnty() : 0.0);
                object.addProperty("free_qty", mBatch.getFreeQty() != null ? mBatch.getFreeQty().toString() : "0.0");
                object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                object.addProperty("purchase_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0);
                object.addProperty("sales_rate", mBatch.getSalesRate() != null ? mBatch.getSalesRate() : 0.0);
                object.addProperty("mrp", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
                object.addProperty("min_rate_a", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.0);
                object.addProperty("min_rate_b", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0.0);
                object.addProperty("min_rate_c", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0.0);
                object.addProperty("net_rate", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
                object.addProperty("fsrmh", mBatch.getFsrmh() != null ? mBatch.getFsrmh() : 0.0);
                object.addProperty("fsrai", mBatch.getFsrai() != null ? mBatch.getFsrai() : 0.0);
                object.addProperty("csrmh", mBatch.getCsrmh() != null ? mBatch.getCsrmh() : 0.0);
                object.addProperty("csrai", mBatch.getCsrai() != null ? mBatch.getCsrai() : 0.0);
                double salesRateWithTax = mBatch.getSalesRate();
                if (mBatch.getProduct().getTaxMaster() != null)
                    salesRateWithTax = mBatch.getSalesRate() + ((mBatch.getSalesRate() * mBatch.getProduct().getTaxMaster().getIgst()) / 100);
                object.addProperty("sales_rate_with_tax", salesRateWithTax);
                object.addProperty("manufacturing_date", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
                object.addProperty("min_margin", mBatch.getMinMargin() != null ? mBatch.getMinMargin() : 0.0);
                object.addProperty("b_rate", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
                object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                object.addProperty("costing", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
                object.addProperty("costingWithTax", mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.0);
                object.addProperty("closing_stock", closing + opening + free_qnt);
                object.addProperty("opening_stock", opening);
                object.addProperty("min_margin", mBatch.getMinMargin() != null ? mBatch.getMinMargin() : 0.0);
                object.addProperty("max_discount", mBatch.getMaxDiscount() != null ? mBatch.getMaxDiscount() : 0.0);
                object.addProperty("min_discount", mBatch.getMinDiscount() != null ? mBatch.getMinDiscount() : 0.0);
                object.addProperty("dis_per", mBatch.getDisPer() != null ? mBatch.getDisPer() : 0.0);
                object.addProperty("dis_amt", mBatch.getDisAmt() != null ? mBatch.getDisAmt() : 0.0);
                object.addProperty("cess_per", mBatch.getCessPer() != null ? mBatch.getCessPer() : 0.0);
                object.addProperty("cess_amt", mBatch.getCessAmt() != null ? mBatch.getCessAmt() : 0.0);
                object.addProperty("barcode", mBatch.getBarcode() != null ? mBatch.getBarcode() : 0.0);
                object.addProperty("pur_date", mBatch.getPurchaseDate() != null ? mBatch.getPurchaseDate().toString() : "");
                object.addProperty("tax_per", mBatch.getProduct().getTaxMaster().getIgst());

                /***** Check for the Batch is sale out(check batch is used in sales invoice/challan details units
                 * and product has is_negative options off then make batch isEditable=>false ******/
                Long invoiceCount = 0L;
                Long challanCount = 0L;
                 /*   invoiceCount = tranxSalesInvoiceDetailsUnitRepository.countBatchExists(
                            mBatch.getId(), true);
                    challanCount = tranxSalesChallanDetailsUnitsRepository.countChallanBatchExists(
                            mBatch.getId(), true);*/
                invoiceCount = tranxPurInvoiceDetailsUnitsRepository.countBatchExists(
                        mBatch.getId(), true);
                challanCount = purChallanDetailsUnitRepository.countChallanBatchExists(
                        mBatch.getId(), true);
                String key = "";
               /* if (paramMap.containsKey("source"))
                    key = request.getParameter("source");*/
//                if (key.equalsIgnoreCase("")) {
                if ((invoiceCount > 0 || challanCount > 0) && unitPack.getIsNegativeStocks() == false)
                    object.addProperty("is_edit", false);
                else {
                    object.addProperty("is_edit", true);
                }
               /* } else {
                    object.addProperty("is_edit", true);
                }*/
                batchArray.add(object);
//                }
            }
        } /*else {
            JsonObject object = new JsonObject();
            Product mProduct = productRepository.findByIdAndStatus(product_id, true);
            object.addProperty("product_name", mProduct.getProductName());
            batchArray.add(object);
        }*/
        ProductBatchNo productBatchNo = productBatchNoRepository.getLastRecordByFilterForCosting(product_id,
                users.getOutlet().getId(), true, level_a_id, level_b_id, level_c_id, unit_id);
        if (productBatchNo != null) {
            result.addProperty("costing", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0.0);
            result.addProperty("costingWithTax", productBatchNo.getCostingWithTax() != null ? productBatchNo.getCostingWithTax() : 0.0);
        } else {
            result.addProperty("costing", 0.0);
            result.addProperty("costingWithTax", 0.0);
        }
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.add("data", batchArray);
        return result;
    }

    public JsonObject fetchProductBatchNo(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonArray batchArray = new JsonArray();
        JsonObject result = new JsonObject();
        ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(Long.parseLong(request.getParameter("batch_id")), true);
        LocalDate invoiceDate = LocalDate.parse(request.getParameter("invoice_date"));
        if (productBatchNo != null) {
            JsonObject object = new JsonObject();
            if (invoiceDate.isAfter(productBatchNo.getExpiryDate())) {
                result.addProperty("message", "batch expired");
                result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
                result.addProperty("flag", 1);
            } else {
                result.addProperty("message", "batch not expired");
                result.addProperty("responseStatus", 0);
            }
        }
        return result;

    }

    public Object batchTransactionsDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            Long batchNo = Long.parseLong(request.getParameter("id"));
            ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(batchNo, true);
            if (productBatchNo != null) {
                JsonObject batchObject = new JsonObject();
                TranxPurInvoiceDetailsUnits tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findTop1ByProductBatchNoIdOrderByIdDesc(productBatchNo.getId());
                if (tranxPurInvoiceDetailsUnits != null) {
                    batchObject.addProperty("supplierName", tranxPurInvoiceDetailsUnits.getPurchaseTransaction().getSundryCreditors().getLedgerName());
                    batchObject.addProperty("billNo", tranxPurInvoiceDetailsUnits.getPurchaseTransaction().getVendorInvoiceNo());
                    batchObject.addProperty("billDate", tranxPurInvoiceDetailsUnits.getPurchaseTransaction().getInvoiceDate().toString());
                } else {
                    batchObject.addProperty("supplierName", "");
                    batchObject.addProperty("billNo", "");
                    batchObject.addProperty("billDate", "");
                }

                batchObject.addProperty("id", productBatchNo.getId());
                batchObject.addProperty("b_details_id", productBatchNo.getId());
                batchObject.addProperty("minMargin", productBatchNo.getMinMargin() != null ? productBatchNo.getMinMargin() : 0.0);
                batchObject.addProperty("mfgDate", productBatchNo.getManufacturingDate() != null ? productBatchNo.getManufacturingDate().toString() : "");
                batchObject.addProperty("expDate", productBatchNo.getExpiryDate() != null ? productBatchNo.getExpiryDate().toString() : "");
                batchObject.addProperty("batchNo", productBatchNo.getBatchNo() != null ? productBatchNo.getBatchNo() : "");
                batchObject.addProperty("batchMrp", productBatchNo.getMrp() != null ? productBatchNo.getMrp() : 0);
                batchObject.addProperty("cost", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0);
                batchObject.addProperty("pur_rate", productBatchNo.getPurchaseRate() != null ? productBatchNo.getPurchaseRate() : 0);
                batchObject.addProperty("cost_with_tax", productBatchNo.getCostingWithTax() != null ? productBatchNo.getCostingWithTax() : 0);
                batchObject.addProperty("product_name", productBatchNo.getProduct().getProductName());
                batchObject.addProperty("qty", productBatchNo.getQnty());
                batchObject.addProperty("free_qty", productBatchNo.getFreeQty() != null ? productBatchNo.getFreeQty().toString() : "");
                batchObject.addProperty("expiry_date", productBatchNo.getExpiryDate() != null ? productBatchNo.getExpiryDate().toString() : "");
                batchObject.addProperty("purchase_rate", productBatchNo.getPurchaseRate() != null ? productBatchNo.getPurchaseRate() : 0.0);
                batchObject.addProperty("sales_rate", productBatchNo.getSalesRate() != null ? productBatchNo.getSalesRate() : 0.0);
                batchObject.addProperty("mrp", productBatchNo.getMrp() != null ? productBatchNo.getMrp() : 0.0);
                batchObject.addProperty("min_rate_a", productBatchNo.getMinRateA() != null ? productBatchNo.getMinRateA() : 0.0);
                batchObject.addProperty("min_rate_b", productBatchNo.getMinRateB() != null ? productBatchNo.getMinRateB() : 0.0);
                batchObject.addProperty("min_rate_c", productBatchNo.getMinRateC() != null ? productBatchNo.getMinRateC() : 0.0);
                batchObject.addProperty("net_rate", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0.0);
                batchObject.addProperty("fsrmh", productBatchNo.getFsrmh() != null ? productBatchNo.getFsrmh() : 0.0);
                batchObject.addProperty("fsrai", productBatchNo.getFsrai() != null ? productBatchNo.getFsrai() : 0.0);
                batchObject.addProperty("csrmh", productBatchNo.getCsrmh() != null ? productBatchNo.getCsrmh() : 0.0);
                batchObject.addProperty("csrai", productBatchNo.getCsrai() != null ? productBatchNo.getCsrai() : 0.0);
                double salesRateWithTax = productBatchNo.getSalesRate();
                if (productBatchNo.getProduct().getTaxMaster() != null)
                    salesRateWithTax = productBatchNo.getSalesRate() + ((productBatchNo.getSalesRate() * productBatchNo.getProduct().getTaxMaster().getIgst()) / 100);
                batchObject.addProperty("sales_rate_with_tax", salesRateWithTax);
                batchObject.addProperty("manufacturing_date", productBatchNo.getManufacturingDate() != null ? productBatchNo.getManufacturingDate().toString() : "");
                batchObject.addProperty("min_margin", productBatchNo.getMinMargin() != null ? productBatchNo.getMinMargin() : 0.0);
                batchObject.addProperty("b_rate", productBatchNo.getMrp() != null ? productBatchNo.getMrp() : 0.0);
                batchObject.addProperty("costing", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0.0);
                batchObject.addProperty("costingWithTax", productBatchNo.getCostingWithTax() != null ? productBatchNo.getCostingWithTax() : 0.0);
                batchObject.addProperty("tax_per", productBatchNo.getProduct().getTaxMaster().getIgst());
                batchObject.addProperty("expiry_date", productBatchNo.getExpiryDate() != null ? productBatchNo.getExpiryDate().toString() : "");
                batchObject.addProperty("b_dis_per", productBatchNo.getDisPer() != null ? productBatchNo.getDisPer() : 0.0);
                batchObject.addProperty("b_dis_amt", productBatchNo.getDisAmt() != null ? productBatchNo.getDisAmt() : 0.0);
                batchObject.addProperty("pur_date", productBatchNo.getPurchaseDate() != null ? productBatchNo.getPurchaseDate().toString() : "");
                batchObject.addProperty("barcode", productBatchNo.getBarcode() != null ? productBatchNo.getBarcode().toString() : "");

                response.add("response", batchObject);
                response.addProperty("responseStatus", HttpStatus.OK.value());
                return response;
            } else {
                response.addProperty("message", "Data not found");
                response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object createBatchDetails(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject result = new JsonObject();
        /**** Creating new batch senarios
         * case 1 : if supplier is changed ,create new batch even batch number is same or different
         * against that supplier so that we can maintain valuation of that product supplier wise
         * case 2 : if anything changes in purchase rate or costing ,create new batch even
         * batch number is same or different
         */
        ProductBatchNo mproductBatchNo = new ProductBatchNo();
        try {
            if (paramMap.containsKey("b_no")) mproductBatchNo.setBatchNo(request.getParameter("b_no"));
            if (paramMap.containsKey("b_expiry") && !request.getParameter("b_expiry").equalsIgnoreCase("") && request.getParameter("b_expiry") != null)
                mproductBatchNo.setExpiryDate(LocalDate.parse(request.getParameter("b_expiry")));
            if (paramMap.containsKey("manufacturing_date") && !request.getParameter("manufacturing_date").equalsIgnoreCase(""))
                mproductBatchNo.setManufacturingDate(LocalDate.parse(request.getParameter("manufacturing_date")));
            if (paramMap.containsKey("pur_date") && !request.getParameter("pur_date").equalsIgnoreCase(""))
                mproductBatchNo.setPurchaseDate(LocalDate.parse(request.getParameter("pur_date")));
            mproductBatchNo.setStatus(true);
            if (paramMap.containsKey("product_id")) {
                Long productId = Long.parseLong(request.getParameter("product_id"));
                Product mProduct = productRepository.findByIdAndStatus(productId, true);
                mproductBatchNo.setProduct(mProduct);
            }
            mproductBatchNo.setOutlet(users.getOutlet());
            mproductBatchNo.setBranch(users.getBranch());
            if (paramMap.containsKey("level_a_id") && !request.getParameter("level_a_id").equalsIgnoreCase("")) {
                LevelA levelA = levelARepository.findByIdAndStatus(Long.parseLong(request.getParameter("level_a_id")), true);
                if (levelA != null) mproductBatchNo.setLevelA(levelA);
            }
            if (paramMap.containsKey("level_b_id") && !request.getParameter("level_b_id").equalsIgnoreCase("")) {
                LevelB levelB = levelBRepository.findByIdAndStatus(Long.parseLong(request.getParameter("level_b_id")), true);
                if (levelB != null) mproductBatchNo.setLevelB(levelB);
            }
            if (paramMap.containsKey("level_c_id") && !request.getParameter("level_c_id").equalsIgnoreCase("")) {
                LevelC levelC = levelCRepository.findByIdAndStatus(Long.parseLong(request.getParameter("level_c_id")), true);
                if (levelC != null) mproductBatchNo.setLevelC(levelC);
            }
            if (paramMap.containsKey("unit_id")) {
                Long unitId = Long.parseLong(request.getParameter("unit_id"));
                Units units = unitsRepository.findByIdAndStatus(unitId, true);
                mproductBatchNo.setUnits(units);
            }
            mproductBatchNo.setMrp(Double.parseDouble(request.getParameter("mrp")));
//            mproductBatchNo.setQnty(0);
//            mproductBatchNo.setSalesRate(0.0);
//            mproductBatchNo.setPurchaseRate(0.0);
//            mproductBatchNo.setMinRateA(0.0);
//            mproductBatchNo.setMinRateB(0.0);
//            mproductBatchNo.setMinRateC(0.0);
//            mproductBatchNo.setMinMargin(0.0);
//            mproductBatchNo.setFreeQty(0.0);
//            mproductBatchNo.setCosting(0.0);
//            mproductBatchNo.setCostingWithTax(0.0);
            if (paramMap.containsKey("b_qty") && !request.getParameter("b_qty").equalsIgnoreCase("")) {
                mproductBatchNo.setQnty(Integer.parseInt(request.getParameter("b_qty")));
            }

            mproductBatchNo.setSalesRate(0.0);
            if (paramMap.containsKey("b_rate") && !request.getParameter("b_rate").equalsIgnoreCase("")) {
                mproductBatchNo.setPurchaseRate(Double.parseDouble(request.getParameter("b_rate")));
            }
            if (paramMap.containsKey("b_rate_a") && !request.getParameter("b_rate_a").equalsIgnoreCase("")) {
                mproductBatchNo.setMinRateA(Double.parseDouble(request.getParameter("b_rate_a")));
            }
            if (paramMap.containsKey("b_rate_b") && !request.getParameter("b_rate_b").equalsIgnoreCase("")) {
                mproductBatchNo.setMinRateB(Double.parseDouble(request.getParameter("b_rate_b")));
            }
            if (paramMap.containsKey("b_rate_c") && !request.getParameter("b_rate_c").equalsIgnoreCase("")) {
                mproductBatchNo.setMinRateC(Double.parseDouble(request.getParameter("b_rate_c")));
            }
            if (paramMap.containsKey("margin") && !request.getParameter("margin").equalsIgnoreCase("")) {
                mproductBatchNo.setMinMargin(Double.parseDouble(request.getParameter("margin")));
            }
            if (paramMap.containsKey("b_freeQty") && !request.getParameter("b_freeQty").equalsIgnoreCase("")) {
                mproductBatchNo.setFreeQty(Double.parseDouble(request.getParameter("b_freeQty")));
            }
            if (paramMap.containsKey("b_dis_per") && !request.getParameter("b_dis_per").equalsIgnoreCase("")) {
                mproductBatchNo.setDisPer(Double.parseDouble(request.getParameter("b_dis_per")));
            }
            if (paramMap.containsKey("b_dis_amt") && !request.getParameter("b_dis_amt").equalsIgnoreCase("")) {
                mproductBatchNo.setDisAmt(Double.parseDouble(request.getParameter("b_dis_amt")));
            }
            if (paramMap.containsKey("b_cess_per") && !request.getParameter("b_cess_per").equalsIgnoreCase("")) {
                mproductBatchNo.setCessPer(Double.parseDouble(request.getParameter("b_cess_per")));
            }
            if (paramMap.containsKey("b_cess_amt") && !request.getParameter("b_cess_amt").equalsIgnoreCase("")) {
                mproductBatchNo.setCessAmt(Double.parseDouble(request.getParameter("b_cess_amt")));
            }
            if (paramMap.containsKey("barcode") && !request.getParameter("barcode").equalsIgnoreCase("")) {
                mproductBatchNo.setBarcode(Double.parseDouble(request.getParameter("barcode")));
            }
            if (paramMap.containsKey("costing") && !request.getParameter("costing").equalsIgnoreCase("")) {
                mproductBatchNo.setCosting(Double.parseDouble(request.getParameter("costing")));
            }
            if (paramMap.containsKey("costingWithTax") && !request.getParameter("costingWithTax").equalsIgnoreCase("")) {
                mproductBatchNo.setCostingWithTax(Double.parseDouble(request.getParameter("costingWithTax")));
            }
            if (paramMap.containsKey("supplier_id") && !request.getParameter("supplier_id").equalsIgnoreCase("")) {
                mproductBatchNo.setSupplierId(Long.parseLong(request.getParameter("supplier_id")));
            }

//            mproductBatchNo.setCosting(0.0);
//            mproductBatchNo.setCostingWithTax(0.0);

            /* fiscal year mapping */
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(LocalDate.now());
            if (fiscalYear != null) {
                mproductBatchNo.setFiscalYear(fiscalYear);
            }
            productBatchNoRepository.save(mproductBatchNo);
            result.addProperty("message", "Batch created successfully");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.addProperty("Data", mproductBatchNo.getId());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            result.addProperty("message", "Error in batch creation");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return result;
    }

    public Object editBatchDetails(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject result = new JsonObject();
        /**** Creating new batch senarios
         * case 1 : if supplier is changed ,create new batch even batch number is same or different
         * against that supplier so that we can maintain valuation of that product supplier wise
         * case 2 : if anything changes in purchase rate or costing ,create new batch even
         * batch number is same or different
         */
        Long detailsId = Long.parseLong(request.getParameter("b_details_id"));
        ProductBatchNo mproductBatchNo = productBatchNoRepository.findByIdAndStatus(detailsId, true);
        try {
            if (mproductBatchNo != null) {
                if (paramMap.containsKey("b_no")) mproductBatchNo.setBatchNo(request.getParameter("b_no"));
                if (paramMap.containsKey("b_expiry") && !request.getParameter("b_expiry").equalsIgnoreCase(""))
                    mproductBatchNo.setExpiryDate(LocalDate.parse(request.getParameter("b_expiry")));
                if (paramMap.containsKey("manufacturing_date") && !request.getParameter("manufacturing_date").equalsIgnoreCase(""))
                    mproductBatchNo.setManufacturingDate(LocalDate.parse(request.getParameter("manufacturing_date")));
                if (paramMap.containsKey("pur_date") && !request.getParameter("pur_date").equalsIgnoreCase(""))
                    mproductBatchNo.setPurchaseDate(LocalDate.parse(request.getParameter("pur_date")));

                if (paramMap.containsKey("b_qty") && !request.getParameter("b_qty").equalsIgnoreCase("")) {
                    Double qty = Double.parseDouble(request.getParameter("b_qty"));
                    mproductBatchNo.setQnty(qty.intValue());
                }

                mproductBatchNo.setSalesRate(0.0);
                if (paramMap.containsKey("b_rate") && !request.getParameter("b_rate").equalsIgnoreCase("")) {
                    mproductBatchNo.setPurchaseRate(Double.parseDouble(request.getParameter("b_rate")));
                }
                if (paramMap.containsKey("b_rate_a") && !request.getParameter("b_rate_a").equalsIgnoreCase("")) {
                    mproductBatchNo.setMinRateA(Double.parseDouble(request.getParameter("b_rate_a")));
                }
                if (paramMap.containsKey("b_rate_b") && !request.getParameter("b_rate_b").equalsIgnoreCase("")) {
                    mproductBatchNo.setMinRateB(Double.parseDouble(request.getParameter("b_rate_b")));
                }
                if (paramMap.containsKey("b_rate_c") && !request.getParameter("b_rate_c").equalsIgnoreCase("")) {
                    mproductBatchNo.setMinRateC(Double.parseDouble(request.getParameter("b_rate_c")));
                }
                if (paramMap.containsKey("margin")) {
                    String marginParam = request.getParameter("margin");
                    if (marginParam != null && !marginParam.equalsIgnoreCase("") && !marginParam.equalsIgnoreCase("null")) {
                        mproductBatchNo.setMinMargin(Double.parseDouble(request.getParameter("margin")));
                    }
                }
                if (paramMap.containsKey("b_freeQty") && !request.getParameter("b_freeQty").equalsIgnoreCase("")) {
                    mproductBatchNo.setFreeQty(Double.parseDouble(request.getParameter("b_freeQty")));
                }
                if (paramMap.containsKey("b_dis_per") && !request.getParameter("b_dis_per").equalsIgnoreCase("")) {
                    mproductBatchNo.setDisPer(Double.parseDouble(request.getParameter("b_dis_per")));
                }
                if (paramMap.containsKey("b_dis_amt") && !request.getParameter("b_dis_amt").equalsIgnoreCase("")) {
                    mproductBatchNo.setDisAmt(Double.parseDouble(request.getParameter("b_dis_amt")));
                }
                if (paramMap.containsKey("b_cess_per") && !request.getParameter("b_cess_per").equalsIgnoreCase("")) {
                    mproductBatchNo.setCessPer(Double.parseDouble(request.getParameter("b_cess_per")));
                }
                if (paramMap.containsKey("b_cess_amt") && !request.getParameter("b_cess_amt").equalsIgnoreCase("")) {
                    mproductBatchNo.setCessAmt(Double.parseDouble(request.getParameter("b_cess_amt")));
                }
                if (paramMap.containsKey("barcode") && !request.getParameter("barcode").equalsIgnoreCase("")) {
                    mproductBatchNo.setBarcode(Double.parseDouble(request.getParameter("barcode")));
                }
                if (paramMap.containsKey("costing") && !request.getParameter("costing").equalsIgnoreCase("")) {
                    mproductBatchNo.setCosting(Double.parseDouble(request.getParameter("costing")));
                }
                if (paramMap.containsKey("costingWithTax") && !request.getParameter("costingWithTax").equalsIgnoreCase("")) {
                    mproductBatchNo.setCostingWithTax(Double.parseDouble(request.getParameter("costingWithTax")));
                }


//                mproductBatchNo.setQnty(Integer.parseInt(request.getParameter("b_qty")));
                // mproductBatchNo.setSalesRate(0.0);
//                mproductBatchNo.setPurchaseRate(Double.parseDouble(request.getParameter("b_rate")));
//                mproductBatchNo.setMinRateA(Double.parseDouble(request.getParameter("b_rate_a")));
//                mproductBatchNo.setMinRateB(Double.parseDouble(request.getParameter("b_rate_b")));
//                mproductBatchNo.setMinRateC(Double.parseDouble(request.getParameter("b_rate_c")));
//                mproductBatchNo.setMinMargin(Double.parseDouble(request.getParameter("margin")));
//                mproductBatchNo.setFreeQty(Double.parseDouble(request.getParameter("b_freeQty")));
//                mproductBatchNo.setCosting(Double.parseDouble(request.getParameter("costing")));
//                mproductBatchNo.setCostingWithTax(Double.parseDouble(request.getParameter("costWithTax")));

//                mproductBatchNo.setDisPer(Double.parseDouble(request.getParameter("b_dis_per")));
//                mproductBatchNo.setDisAmt(Double.parseDouble(request.getParameter("b_dis_amt")));
//                mproductBatchNo.setCessPer(Double.parseDouble(request.getParameter("b_cess_per")));
//                mproductBatchNo.setCessAmt(Double.parseDouble(request.getParameter("b_cess_amt")));
//                mproductBatchNo.setBarcode(Double.parseDouble(request.getParameter("barcode")));
                mproductBatchNo.setOutlet(users.getOutlet());
                mproductBatchNo.setBranch(users.getBranch());
                mproductBatchNo.setMrp(Double.parseDouble(request.getParameter("mrp")));
                productBatchNoRepository.save(mproductBatchNo);
                result.addProperty("message", "Batch updated successfully");
                result.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            result.addProperty("message", "Error in batch creation");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return result;
    }

    public Object batchProductDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        try {

            JsonArray batchArray = new JsonArray();
            Long level_a_id = null;
            Long level_b_id = null;
            Long level_c_id = null;
     /*   Long sub_category_id = null;
        Long package_id = null;*/
            Long unit_id = null;
            Long branchId = null;
            if (users.getBranch() != null) {
                branchId = users.getBranch().getId();
            }
            Long fiscalId = null;
            LocalDate currentDate = LocalDate.now();
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
            if (fiscalYear != null) fiscalId = fiscalYear.getId();
            Long product_id = Long.parseLong(request.getParameter("product_id"));
            if (paramMap.containsKey("level_a_id") && !request.getParameter("level_a_id").equalsIgnoreCase(""))
                level_a_id = Long.parseLong(request.getParameter("level_a_id"));
            if (paramMap.containsKey("level_b_id") && !request.getParameter("level_b_id").equalsIgnoreCase(""))
                level_b_id = Long.parseLong(request.getParameter("level_b_id"));
            if (paramMap.containsKey("level_c_id") && !request.getParameter("level_c_id").equalsIgnoreCase(""))
                level_c_id = Long.parseLong(request.getParameter("level_c_id"));
            Long productId = Long.parseLong(request.getParameter("product_id"));
            List<ProductBatchNo> productbatch = productBatchNoRepository.findByProductIdAndStatus(productId, true);
            if (productbatch != null) {
                for (ProductBatchNo mBatch : productbatch) {
                    Double opening = productOpeningStocksRepository.findSumProductOpeningStocksBatchwise(mBatch.getProduct().getId(), mBatch.getOutlet().getId(), mBatch.getBranch() != null ? mBatch.getBranch().getId() : null, fiscalId, mBatch.getId());
                    Double free_qnt = productOpeningStocksRepository.findSumProductFreeQtyBatchwise(mBatch.getProduct().getId(), mBatch.getOutlet().getId(), mBatch.getBranch() != null ? mBatch.getBranch().getId() : null, fiscalId, mBatch.getId());
                    Double closing = inventoryCommonPostings.getClosingStockProductBatch(branchId, users.getOutlet().getId(), product_id, level_a_id, level_b_id, level_c_id, mBatch.getId(), fiscalId);
                    JsonObject object = new JsonObject();
                    object.addProperty("org_b_details_id", mBatch.getId());
                    object.addProperty("product_name", mBatch.getProduct().getProductName());
                    object.addProperty("batch_no", mBatch.getBatchNo());
                    object.addProperty("qty", mBatch.getQnty());
                    object.addProperty("free_qty", mBatch.getFreeQty() != null ? mBatch.getFreeQty().toString() : "");
                    object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                    object.addProperty("purchase_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0);
                    object.addProperty("sales_rate", mBatch.getSalesRate() != null ? mBatch.getSalesRate() : 0.0);
                    object.addProperty("mrp", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
                    object.addProperty("min_rate_a", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.0);
                    object.addProperty("min_rate_b", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0.0);
                    object.addProperty("min_rate_c", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0.0);
                    object.addProperty("net_rate", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
                    double salesRateWithTax = mBatch.getSalesRate();
                    if (mBatch.getProduct().getTaxMaster() != null)
                        salesRateWithTax = mBatch.getSalesRate() + ((mBatch.getSalesRate() * mBatch.getProduct().getTaxMaster().getIgst()) / 100);
                    object.addProperty("sales_rate_with_tax", salesRateWithTax);
                    object.addProperty("manufacturing_date", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
                    object.addProperty("min_margin", mBatch.getMinMargin() != null ? mBatch.getMinMargin() : 0.0);
                    object.addProperty("b_rate", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
                    object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                    object.addProperty("costing", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
                    object.addProperty("costingWithTax", mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.0);
                    object.addProperty("closing_stock", closing + opening + free_qnt);
                    object.addProperty("opening_stock", opening);
                    object.addProperty("min_margin", mBatch.getMinMargin() != null ? mBatch.getMinMargin() : 0.0);
                    object.addProperty("max_discount", mBatch.getMaxDiscount() != null ? mBatch.getMaxDiscount() : 0.0);
                    object.addProperty("min_discount", mBatch.getMinDiscount() != null ? mBatch.getMinDiscount() : 0.0);
                    object.addProperty("dis_per", mBatch.getDisPer() != null ? mBatch.getDisPer() : 0.0);
                    object.addProperty("dis_amt", mBatch.getDisAmt() != null ? mBatch.getDisAmt() : 0.0);
                    object.addProperty("cess_per", mBatch.getCessPer() != null ? mBatch.getCessPer() : 0.0);
                    object.addProperty("cess_amt", mBatch.getCessAmt() != null ? mBatch.getCessAmt() : 0.0);
                    object.addProperty("barcode", mBatch.getBarcode() != null ? mBatch.getBarcode() : 0.0);
                    object.addProperty("pur_date", mBatch.getPurchaseDate() != null ? mBatch.getPurchaseDate().toString() : "");
                    batchArray.add(object);

                }
                response.add("response", batchArray);
                response.addProperty("responseStatus", HttpStatus.OK.value());
                return response;
            }

            response.addProperty("message", "Data not found");
            response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }


    public JsonArray getProductStock(HttpServletRequest request) {
        JsonArray jsonArray = new JsonArray();
        jsonArray = unitConversion.showStocks(Long.parseLong(request.getParameter("productId")));
        return jsonArray;
    }

    public JsonArray getProductWholeStock(HttpServletRequest request) {
        JsonArray jsonArray = new JsonArray();
        LocalDate invoiceDate = LocalDate.parse(request.getParameter("invoice_date"));
        jsonArray = unitConversion.showWholeStocks(Long.parseLong(request.getParameter("productId")), invoiceDate);
        return jsonArray;
    }

    public JsonObject getProductBatchMultiUnit(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonArray batchArray = new JsonArray();
        JsonObject result = new JsonObject();
        Long level_a_id = null;
        Long level_b_id = null;
        Long level_c_id = null;
        Long unit_id = null;
        Long branchId = null;
        if (users.getBranch() != null) {
            branchId = users.getBranch().getId();
        }
        Long fiscalId = null;
        LocalDate currentDate = LocalDate.now();
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        if (fiscalYear != null) fiscalId = fiscalYear.getId();
        Long product_id = Long.parseLong(request.getParameter("product_id"));
        if (paramMap.containsKey("level_a_id") && !request.getParameter("level_a_id").equalsIgnoreCase(""))
            level_a_id = Long.parseLong(request.getParameter("level_a_id"));
        if (paramMap.containsKey("level_b_id") && !request.getParameter("level_b_id").equalsIgnoreCase(""))
            level_b_id = Long.parseLong(request.getParameter("level_b_id"));
        if (paramMap.containsKey("level_c_id") && !request.getParameter("level_c_id").equalsIgnoreCase(""))
            level_c_id = Long.parseLong(request.getParameter("level_c_id"));
        ProductUnitPacking unitPack = productUnitRepository.findRateMultiUnit(product_id, level_a_id, level_b_id,
                level_c_id, true);
        LocalDate invoiceDate = LocalDate.parse(request.getParameter("invoice_date"));
        List<ProductBatchNo> productbatch = productBatchNoRepository.findByBatchList(product_id, users.getOutlet().getId(),
                true, level_a_id, level_b_id, level_c_id);
        if (productbatch != null && productbatch.size() > 0) {
            for (ProductBatchNo mBatch : productbatch) {
                Double opening = productOpeningStocksRepository.findSumProductOpeningStocksBatchwise(mBatch.getProduct().getId(), mBatch.getOutlet().getId(), mBatch.getBranch() != null ? mBatch.getBranch().getId() : null, fiscalId, mBatch.getId());
                Double free_qnt = productOpeningStocksRepository.findSumProductFreeQtyBatchwise(mBatch.getProduct().getId(), mBatch.getOutlet().getId(), mBatch.getBranch() != null ? mBatch.getBranch().getId() : null, fiscalId, mBatch.getId());
                Double closing = inventoryCommonPostings.getClosingStockProductFilters(branchId, users.getOutlet().getId(), product_id, level_a_id, level_b_id, level_c_id, unit_id, mBatch.getId(), fiscalId);
                JsonObject object = new JsonObject();
                if (mBatch.getExpiryDate() != null) {
                    if (invoiceDate.isAfter(mBatch.getExpiryDate())) {
                        object.addProperty("is_expired", true);
                    } else {
                        object.addProperty("is_expired", false);
                    }
                } else {
                    object.addProperty("is_expired", false);
                }
                object.addProperty("id", mBatch.getId());
                object.addProperty("b_details_id", mBatch.getId());
                object.addProperty("org_b_details_id", mBatch.getId());
                object.addProperty("product_name", mBatch.getProduct().getProductName());
                object.addProperty("batch_no", mBatch.getBatchNo());
                object.addProperty("qty", mBatch.getQnty() != null ? mBatch.getQnty() : 0.0);
                object.addProperty("free_qty", mBatch.getFreeQty() != null ? mBatch.getFreeQty().toString() : "0.0");
                object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                object.addProperty("purchase_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.0);
                object.addProperty("sales_rate", mBatch.getSalesRate() != null ? mBatch.getSalesRate() : 0.0);
                object.addProperty("mrp", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
                object.addProperty("min_rate_a", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.0);
                object.addProperty("min_rate_b", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0.0);
                object.addProperty("min_rate_c", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0.0);
                object.addProperty("net_rate", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
                object.addProperty("fsrmh", mBatch.getFsrmh() != null ? mBatch.getFsrmh() : 0.0);
                object.addProperty("fsrai", mBatch.getFsrai() != null ? mBatch.getFsrai() : 0.0);
                object.addProperty("csrmh", mBatch.getCsrmh() != null ? mBatch.getCsrmh() : 0.0);
                object.addProperty("csrai", mBatch.getCsrai() != null ? mBatch.getCsrai() : 0.0);
                double salesRateWithTax = mBatch.getSalesRate();
                if (mBatch.getProduct().getTaxMaster() != null)
                    salesRateWithTax = mBatch.getSalesRate() + ((mBatch.getSalesRate() * mBatch.getProduct().getTaxMaster().getIgst()) / 100);
                object.addProperty("sales_rate_with_tax", salesRateWithTax);
                object.addProperty("manufacturing_date", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
                object.addProperty("min_margin", mBatch.getMinMargin() != null ? mBatch.getMinMargin() : 0.0);
                object.addProperty("b_rate", mBatch.getMrp() != null ? mBatch.getMrp() : 0.0);
                object.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                object.addProperty("costing", mBatch.getCosting() != null ? mBatch.getCosting() : 0.0);
                object.addProperty("costingWithTax", mBatch.getCostingWithTax() != null ? mBatch.getCostingWithTax() : 0.0);
                object.addProperty("closing_stock", closing + opening + free_qnt);
                object.addProperty("opening_stock", opening);
                object.addProperty("min_margin", mBatch.getMinMargin() != null ? mBatch.getMinMargin() : 0.0);
                object.addProperty("max_discount", mBatch.getMaxDiscount() != null ? mBatch.getMaxDiscount() : 0.0);
                object.addProperty("min_discount", mBatch.getMinDiscount() != null ? mBatch.getMinDiscount() : 0.0);
                object.addProperty("dis_per", mBatch.getDisPer() != null ? mBatch.getDisPer() : 0.0);
                object.addProperty("dis_amt", mBatch.getDisAmt() != null ? mBatch.getDisAmt() : 0.0);
                object.addProperty("cess_per", mBatch.getCessPer() != null ? mBatch.getCessPer() : 0.0);
                object.addProperty("cess_amt", mBatch.getCessAmt() != null ? mBatch.getCessAmt() : 0.0);
                object.addProperty("barcode", mBatch.getBarcode() != null ? mBatch.getBarcode() : 0.0);
                object.addProperty("pur_date", mBatch.getPurchaseDate() != null ? mBatch.getPurchaseDate().toString() : "");
                object.addProperty("tax_per", mBatch.getProduct().getTaxMaster().getIgst());

                /***** Check for the Batch is sale out(check batch is used in sales invoice/challan details units
                 * and product has is_negative options off then make batch isEditable=>false ******/
                Long invoiceCount = 0L;
                Long challanCount = 0L;
                invoiceCount = tranxPurInvoiceDetailsUnitsRepository.countBatchExists(
                        mBatch.getId(), true);
                challanCount = purChallanDetailsUnitRepository.countChallanBatchExists(
                        mBatch.getId(), true);
                if ((invoiceCount > 0 || challanCount > 0) && unitPack.getIsNegativeStocks() == false)
                    object.addProperty("is_edit", false);
                else {
                    object.addProperty("is_edit", true);
                }

                batchArray.add(object);
            }
        }
        ProductBatchNo productBatchNo = productBatchNoRepository.getLastRecordByFilterForCosting(product_id,
                users.getOutlet().getId(), true, level_a_id, level_b_id, level_c_id, unit_id);
        if (productBatchNo != null) {
            result.addProperty("costing", productBatchNo.getCosting() != null ? productBatchNo.getCosting() : 0.0);
            result.addProperty("costingWithTax", productBatchNo.getCostingWithTax() != null ? productBatchNo.getCostingWithTax() : 0.0);
        } else {
            result.addProperty("costing", 0.0);
            result.addProperty("costingWithTax", 0.0);
        }
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.add("data", batchArray);
        return result;
    }

    public JsonObject productBatchDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        ProductBatchNo productBatch = productBatchNoRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);

        try {

            productBatch.setStatus(false);
            productBatchNoRepository.save(productBatch);
            jsonObject.addProperty("message", "Batch deleted successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return jsonObject;
    }
}
