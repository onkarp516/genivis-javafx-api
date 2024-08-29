package in.truethics.ethics.ethicsapiv10.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.dto.masterdto.FRUnitWiseRatesDTO;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTranxDetailsBatchwise;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.Units;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.StockTranxDetailsBatchwiseRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.StockTranxDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class UnitConversion {

    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    StockTranxDetailsRepository stockTranxDetailsRepository;
    @Autowired
    StockTranxDetailsBatchwiseRepository stockTranxDetailsBatchwiseRepository;
    private static final Logger loggger = LoggerFactory.getLogger(UnitConversion.class);
    @Autowired
    private UnitsRepository unitsRepository;


    public Double convertToLowerUnit(Long productId, Long unitId, Double qty) {
        List<ProductUnitPacking> productList = productUnitRepository.findByProductId(productId);
        ProductUnitPacking requiredUnit = productUnitRepository.findByProductIdAndUnitsId(productId, unitId);
        int index = productList.indexOf(requiredUnit);
        List<ProductUnitPacking> sublist = new ArrayList<>();
        Double unitConvPCS = 1.0;
        // Check if the target object is found
        if (index != -1 && index < productList.size() - 1) {
            // Get the sublist of elements after the index of the target object
            sublist = productList.subList(index, productList.size());
            try {
                for (ProductUnitPacking obj : sublist) {
                    unitConvPCS = unitConvPCS * obj.getUnitConversion();
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                loggger.error("Error in convertToLowerUnit():" + exceptionAsString);
            }
        }
        loggger.debug("Product Id-->" + productId + "\tUnit Id-->" + unitId + "\tConversion in PCS--->" + unitConvPCS);
        return unitConvPCS * qty;
    }

    public List<ProductUnitPacking> showUnits(Long productId, Long unitId) {
        List<ProductUnitPacking> productList = productUnitRepository.findByProductId(productId);
        ProductUnitPacking requiredUnit = productUnitRepository.findByProductIdAndUnitsId(productId, unitId);
        int index = productList.indexOf(requiredUnit);
        List<ProductUnitPacking> sublist = new ArrayList<>();
        Double unitConvPCS = 1.0;
        // Check if the target object is found
        if (index != -1 && index < productList.size() - 1) {
            // Get the sublist of elements after the index of the target object
            sublist = productList.subList(index, productList.size());
            try {
                for (ProductUnitPacking obj : sublist) {
                    unitConvPCS = unitConvPCS * obj.getUnitConversion();
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                loggger.error("Error in showUnits():" + exceptionAsString);
            }
        }
        return sublist;
    }

    public JsonArray showStocks(Long productId) {
        JsonArray array = new JsonArray();
        try {
            List<ProductUnitPacking> productList = productUnitRepository.findByProductId(productId);//all units of given product id
            System.out.println("Product Id:" + productId);
            ProductUnitPacking isRateObj = productUnitRepository.findByProductIdAndIsRate(productId, true);
            double rate_fsrmh = 0.0;
            double rate_fsrai = 0.0;
            double rate_csrmh = 0.0;
            double rate_csrai = 0.0;
            double rateConv = 1;
            if (isRateObj != null) {
                rateConv = isRateObj.getUnitConversion();
                rate_fsrmh = isRateObj.getFsrmh();
                rate_fsrai = isRateObj.getFsrai();
                rate_csrmh = isRateObj.getCsrmh();
                rate_csrai = isRateObj.getCsrai();
            }
            int index = productList.indexOf(isRateObj);//0

            InventorySummaryTransactionDetails inventorySummaryTransactionDetails =
                    stockTranxDetailsRepository.findTop1ByProductIdOrderByIdDesc(productId);

            if (inventorySummaryTransactionDetails != null) {
                int dividend = inventorySummaryTransactionDetails.getClosingStock().intValue();
                int clostk = dividend;
                int size = productList.size() - 1;//2
                int i = 0;
                double cal_fsrmh = 0.0;
                for (ProductUnitPacking productUnitPacking : productList) {
                    int divisor = 1;
                    int multiplier = 1;
                    JsonObject jsonObject = new JsonObject();
                    for (int s = i; s <= size; s++) { //s=1
                        divisor = divisor * productList.get(s).getUnitConversion().intValue();
                        if (divisor == 0) divisor = 1;
                        if (index >= s) {//
                            multiplier = multiplier * productList.get(s).getUnitConversion().intValue();//1*12=12
                        }
                    }
                    if (index > i) {//0>0
                        cal_fsrmh = multiplier * rate_fsrmh;
                    }
                    if (index < i) {
                        cal_fsrmh = cal_fsrmh / (divisor);//171.48/
                    }
                    if (index == i) {
                        cal_fsrmh = rate_fsrmh;//171.48
                    }

                    i++;//1
                    int quotient = dividend / divisor;
                    dividend = dividend % divisor; //remainder

                    jsonObject.addProperty("unitid", productUnitPacking.getUnits().getId());
                    jsonObject.addProperty("unitName", productUnitPacking.getUnits().getUnitName());
                    jsonObject.addProperty("unitConv", productUnitPacking.getUnitConversion());
                    jsonObject.addProperty("closingstk", quotient);
                    jsonObject.addProperty("actstkcheck", (clostk / divisor));
                    jsonObject.addProperty("rateUnitName", isRateObj != null ? isRateObj.getUnits().getUnitName() : "");
                    /*** multi unit rate convesrion ***/
                    jsonObject.addProperty("fsrmh", productUnitPacking.getFsrmh());
                    jsonObject.addProperty("fsrai", productUnitPacking.getFsrai());
                    jsonObject.addProperty("csrmh", productUnitPacking.getCsrmh());
                    jsonObject.addProperty("csrai", productUnitPacking.getCsrai());
                    jsonObject.addProperty("mrp", productUnitPacking.getMrp() != null ? productUnitPacking.getMrp() : 0.0);
                    jsonObject.addProperty("purchaserate", productUnitPacking.getPurchaseRate() != null ? productUnitPacking.getPurchaseRate() : 0.0);
                    jsonObject.addProperty("is_negetive", productUnitPacking.getIsNegativeStocks());
                    array.add(jsonObject);
                   /* if (dividend <= 0)
                        break;*/
                }
            } else {
                for (ProductUnitPacking productUnitPacking : productList) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("unitid", productUnitPacking.getUnits().getId());
                    jsonObject.addProperty("unitName", productUnitPacking.getUnits().getUnitName());
                    jsonObject.addProperty("unitConv", productUnitPacking.getUnitConversion());
                    jsonObject.addProperty("closingstk", 0);
                    jsonObject.addProperty("actstkcheck", 0);
                    jsonObject.addProperty("rateUnitName", isRateObj != null ? isRateObj.getUnits().getUnitName() : "");
                    /*** multi unit rate convesrion ***/
                    jsonObject.addProperty("fsrmh", productUnitPacking.getFsrmh());
                    jsonObject.addProperty("fsrai", productUnitPacking.getFsrai());
                    jsonObject.addProperty("csrmh", productUnitPacking.getCsrmh());
                    jsonObject.addProperty("csrai", productUnitPacking.getCsrai());
                    jsonObject.addProperty("mrp", productUnitPacking.getMrp() != null ? productUnitPacking.getMrp() : 0.0);
                    jsonObject.addProperty("purchaserate", productUnitPacking.getPurchaseRate() != null ? productUnitPacking.getPurchaseRate() : 0.0);
                    jsonObject.addProperty("is_negetive", productUnitPacking.getIsNegativeStocks());
                    array.add(jsonObject);
                }
            }


        } catch (Exception e) {
            System.out.println("Exception-->" + e.toString());
            e.printStackTrace();
        }
        return array;
    }

    public List<FRUnitWiseRatesDTO> FRshowStocks(Long productId) {
//        JsonArray array = new JsonArray();
        List<FRUnitWiseRatesDTO> frUnitWiseRatesDTOList = new ArrayList<>();
        try {
            List<ProductUnitPacking> productList = productUnitRepository.findByProductId(productId);//all units of given product id
            System.out.println("Product Id:" + productId);
            ProductUnitPacking isRateObj = productUnitRepository.findByProductIdAndIsRate(productId, true);
            double rate_fsrmh = 0.0;
            double rate_fsrai = 0.0;
            double rate_csrmh = 0.0;
            double rate_csrai = 0.0;
            double rateConv = 1;
            if (isRateObj != null) {
                rateConv = isRateObj.getUnitConversion();
                rate_fsrmh = isRateObj.getFsrmh();
                rate_fsrai = isRateObj.getFsrai();
                rate_csrmh = isRateObj.getCsrmh();
                rate_csrai = isRateObj.getCsrai();
            }
            int index = productList.indexOf(isRateObj);//0

            InventorySummaryTransactionDetails inventorySummaryTransactionDetails =
                    stockTranxDetailsRepository.findTop1ByProductIdOrderByIdDesc(productId);

            if (inventorySummaryTransactionDetails != null) {
                int dividend = inventorySummaryTransactionDetails.getClosingStock().intValue();
                int clostk = dividend;
                int size = productList.size() - 1;//2
                int i = 0;
                double cal_fsrmh = 0.0;
                for (ProductUnitPacking productUnitPacking : productList) {
                    int divisor = 1;
                    int multiplier = 1;
//                    JsonObject jsonObject = new JsonObject();
                    FRUnitWiseRatesDTO frUnitWiseRatesDTO = new FRUnitWiseRatesDTO();
                    for (int s = i; s <= size; s++) { //s=1
                        divisor = divisor * productList.get(s).getUnitConversion().intValue();
                        if (divisor == 0) divisor = 1;
                        if (index >= s) {//
                            multiplier = multiplier * productList.get(s).getUnitConversion().intValue();//1*12=12
                        }
                    }
                    if (index > i) {//0>0
                        cal_fsrmh = multiplier * rate_fsrmh;
                    }
                    if (index < i) {
                        cal_fsrmh = cal_fsrmh / (divisor);//171.48/
                    }
                    if (index == i) {
                        cal_fsrmh = rate_fsrmh;//171.48
                    }

                    i++;//1
                    int quotient = dividend / divisor;
                    dividend = dividend % divisor; //remainder

/*                    jsonObject.addProperty("unitid", productUnitPacking.getUnits().getId());
                    jsonObject.addProperty("unitName", productUnitPacking.getUnits().getUnitName());
                    jsonObject.addProperty("unitConv", productUnitPacking.getUnitConversion());
                    jsonObject.addProperty("closingstk", quotient);
                    jsonObject.addProperty("actstkcheck", (clostk / divisor));
                    jsonObject.addProperty("rateUnitName", isRateObj != null ? isRateObj.getUnits().getUnitName() : "");
                    *//*** multi unit rate convesrion ***//*
                    jsonObject.addProperty("fsrmh", productUnitPacking.getFsrmh());
                    jsonObject.addProperty("fsrai", productUnitPacking.getFsrai());
                    jsonObject.addProperty("csrmh", productUnitPacking.getCsrmh());
                    jsonObject.addProperty("csrai", productUnitPacking.getCsrai());
                    jsonObject.addProperty("mrp", productUnitPacking.getMrp());
                    jsonObject.addProperty("purchaserate", productUnitPacking.getPurchaseRate());
                    jsonObject.addProperty("is_negetive", productUnitPacking.getIsNegativeStocks());
                    array.add(jsonObject);*/

                    frUnitWiseRatesDTO.setUnitId(productUnitPacking.getUnits().getId());
                    frUnitWiseRatesDTO.setUnitName(productUnitPacking.getUnits().getUnitName());
                    frUnitWiseRatesDTO.setUnitConv(productUnitPacking.getUnitConversion());
                    frUnitWiseRatesDTO.setClosingstk(quotient);
                    frUnitWiseRatesDTO.setActstkcheck((clostk / divisor));
                    frUnitWiseRatesDTO.setRateUnitName(isRateObj != null ? isRateObj.getUnits().getUnitName() : "");
                    frUnitWiseRatesDTO.setFsrmh(productUnitPacking.getFsrmh());
                    frUnitWiseRatesDTO.setFsrai(productUnitPacking.getFsrai());
                    frUnitWiseRatesDTO.setCsrmh(productUnitPacking.getCsrmh());
                    frUnitWiseRatesDTO.setCsrai(productUnitPacking.getCsrai());
                    frUnitWiseRatesDTO.setMrp(productUnitPacking.getMrp() != null ? productUnitPacking.getMrp() : 0.0);
                    frUnitWiseRatesDTO.setPurchaserate(productUnitPacking.getPurchaseRate() != null ? productUnitPacking.getPurchaseRate() : 0.0);
                    frUnitWiseRatesDTO.setNegetive(productUnitPacking.getIsNegativeStocks());
                    frUnitWiseRatesDTOList.add(frUnitWiseRatesDTO);
                   /* if (dividend <= 0)
                        break;*/
                }
            } else {
                for (ProductUnitPacking productUnitPacking : productList) {
                    /*JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("unitid", productUnitPacking.getUnits().getId());
                    jsonObject.addProperty("unitName", productUnitPacking.getUnits().getUnitName());
                    jsonObject.addProperty("unitConv", productUnitPacking.getUnitConversion());
                    jsonObject.addProperty("closingstk", 0);
                    jsonObject.addProperty("actstkcheck", 0);
                    jsonObject.addProperty("rateUnitName", isRateObj != null ? isRateObj.getUnits().getUnitName() : "");
                    *//*** multi unit rate convesrion ***//*
                    jsonObject.addProperty("fsrmh", productUnitPacking.getFsrmh());
                    jsonObject.addProperty("fsrai", productUnitPacking.getFsrai());
                    jsonObject.addProperty("csrmh", productUnitPacking.getCsrmh());
                    jsonObject.addProperty("csrai", productUnitPacking.getCsrai());
                    jsonObject.addProperty("mrp", productUnitPacking.getMrp());
                    jsonObject.addProperty("purchaserate", productUnitPacking.getPurchaseRate());
                    jsonObject.addProperty("is_negetive", productUnitPacking.getIsNegativeStocks());
                    array.add(jsonObject);*/
                    FRUnitWiseRatesDTO frUnitWiseRatesDTO = new FRUnitWiseRatesDTO();
                    frUnitWiseRatesDTO.setUnitId(productUnitPacking.getUnits().getId());
                    frUnitWiseRatesDTO.setUnitName(productUnitPacking.getUnits().getUnitName());
                    frUnitWiseRatesDTO.setUnitConv(productUnitPacking.getUnitConversion());
                    frUnitWiseRatesDTO.setClosingstk(0.0);
                    frUnitWiseRatesDTO.setActstkcheck(0.0);
                    frUnitWiseRatesDTO.setRateUnitName(isRateObj != null ? isRateObj.getUnits().getUnitName() : "");
                    frUnitWiseRatesDTO.setFsrmh(productUnitPacking.getFsrmh());
                    frUnitWiseRatesDTO.setFsrai(productUnitPacking.getFsrai());
                    frUnitWiseRatesDTO.setCsrmh(productUnitPacking.getCsrmh());
                    frUnitWiseRatesDTO.setCsrai(productUnitPacking.getCsrai());
                    frUnitWiseRatesDTO.setMrp(productUnitPacking.getMrp() != null ? productUnitPacking.getMrp() : 0.0);
                    frUnitWiseRatesDTO.setPurchaserate(productUnitPacking.getPurchaseRate() != null ? productUnitPacking.getPurchaseRate() : 0.0);
                    frUnitWiseRatesDTO.setNegetive(productUnitPacking.getIsNegativeStocks());
                    frUnitWiseRatesDTOList.add(frUnitWiseRatesDTO);
                }
            }


        } catch (Exception e) {
            System.out.println("Exception-->" + e.toString());
            e.printStackTrace();
        }

        return frUnitWiseRatesDTOList;
    }

    public JsonArray showWholeStocks(Long productId, LocalDate invoiceDate) {
        JsonArray array = new JsonArray();

        try {
            List<ProductUnitPacking> unitPackingList = productUnitRepository.findByProductId(productId);//all units of given product id
            ArrayList<InventorySummaryTranxDetailsBatchwise> batchList = stockTranxDetailsBatchwiseRepository.findBatchListByProductId(productId);
            JsonArray batchArray = new JsonArray();
            for (InventorySummaryTranxDetailsBatchwise batchObject :
                    batchList) {
                ProductBatchNo productBatchNo = productBatchNoRepository.findByIdAndStatus(batchObject.getBatchId(), true);
                JsonObject jsonObject = new JsonObject();
                if (productBatchNo != null) {
                    jsonObject.addProperty("batchId", productBatchNo.getId());
                    jsonObject.addProperty("product_name", productBatchNo.getProduct().getProductName());
                    jsonObject.addProperty("batch", productBatchNo.getBatchNo());
                    jsonObject.addProperty("purchase_date", productBatchNo.getPurchaseDate() == null ? "" : productBatchNo.getPurchaseDate().toString());
                    jsonObject.addProperty("manufacture_date", productBatchNo.getManufacturingDate() == null ? "" : productBatchNo.getManufacturingDate().toString());
                    jsonObject.addProperty("expiry_date", productBatchNo.getExpiryDate() == null ? "" : productBatchNo.getExpiryDate().toString());
                    jsonObject.addProperty("mrp", productBatchNo.getMrp() == null ? "" : productBatchNo.getMrp().toString());
                    jsonObject.addProperty("purchase_rate", productBatchNo.getPurchaseRate() == null ? "" : productBatchNo.getPurchaseRate().toString());
//                    jsonObject.addProperty("isExpired", productBatchNo.getPurchaseRate() == null ? "" : productBatchNo.getPurchaseRate().toString());

                    if (invoiceDate != null) {
                        if (productBatchNo.getExpiryDate() != null) {
                            if (invoiceDate.isAfter(productBatchNo.getExpiryDate())) {
                                jsonObject.addProperty("is_expired", true);
                            } else {
                                jsonObject.addProperty("is_expired", false);
                            }
                        } else {
                            jsonObject.addProperty("is_expired", false);
                        }
                    }
                }

                JsonArray unitArray = new JsonArray();
                for (ProductUnitPacking unitPacking :
                        unitPackingList) {
                    Units units = unitPacking.getUnits();
                    Long batchId = batchObject.getBatchId();

                    InventorySummaryTranxDetailsBatchwise currentStock = stockTranxDetailsBatchwiseRepository.findCurrentStock(productId, batchId, units.getId());
                    int physicalStock = 0;
                    if (currentStock != null) {
                        physicalStock = getPhysicalStock(currentStock, unitPackingList, 0.0);
                        JsonObject stockObject = new JsonObject();
                        Units productUnitPacking = unitPacking.getUnits();
                        stockObject.addProperty("unitid", productUnitPacking.getId());
                        stockObject.addProperty("unitName", productUnitPacking.getUnitName());
                        stockObject.addProperty("physical_stock", physicalStock);
                        stockObject.addProperty("logical_stock", 0);
                        stockObject.addProperty("conversion_rate", unitPacking.getUnitConversion().intValue());
                        stockObject.addProperty("fsrmh", unitPacking.getFsrmh());
                        stockObject.addProperty("fsrai", unitPacking.getFsrai());
                        stockObject.addProperty("csrmh", unitPacking.getCsrmh());
                        stockObject.addProperty("csrai", unitPacking.getCsrai());
                        stockObject.addProperty("mrp", unitPacking.getMrp() == null ? "" : unitPacking.getMrp().toString());
                        stockObject.addProperty("purchase_rate", unitPacking.getPurchaseRate() == null ? "" : unitPacking.getPurchaseRate().toString());

                        unitArray.add(stockObject);
                    } else {

                        JsonObject stockObject = new JsonObject();
                        Units productUnitPacking = unitPacking.getUnits();
                        stockObject.addProperty("unitid", productUnitPacking.getId());
                        stockObject.addProperty("unitName", productUnitPacking.getUnitName());
                        stockObject.addProperty("physical_stock", physicalStock);
                        stockObject.addProperty("logical_stock", 0);
                        stockObject.addProperty("conversion_rate", unitPacking.getUnitConversion().intValue());
                        stockObject.addProperty("fsrmh", unitPacking.getFsrmh());
                        stockObject.addProperty("fsrai", unitPacking.getFsrai());
                        stockObject.addProperty("csrmh", unitPacking.getCsrmh());
                        stockObject.addProperty("csrai", unitPacking.getCsrai());
                        stockObject.addProperty("mrp", unitPacking.getMrp() == null ? "" : unitPacking.getMrp().toString());
                        stockObject.addProperty("purchase_rate", unitPacking.getPurchaseRate() == null ? "" : unitPacking.getPurchaseRate().toString());
                        unitArray.add(stockObject);
                    }
                }
                jsonObject.add("unit_list", unitArray);
                batchArray.add(jsonObject);
            }

            array = getLogicalStock(batchArray);
        } catch (Exception e) {
            System.out.println("Exception-->" + e.toString());
            e.printStackTrace();
        }
        return array;
    }

    private JsonArray getLogicalStock(JsonArray jsonArray) {
        JsonArray newJsonArray = jsonArray;

        for (int i = 0; i < jsonArray.size(); i++) {

            JsonArray unitArray = jsonArray.get(i).getAsJsonObject().get("unit_list").getAsJsonArray();
            int finalLogicalStock = 0;
            for (int s = 0; s < unitArray.size(); s++) {
                int l_stock = 0;
                int p_stock = unitArray.get(s).getAsJsonObject().get("physical_stock").getAsInt();
                unitArray.get(s).getAsJsonObject().addProperty("logical_stock", finalLogicalStock);
                l_stock = finalLogicalStock + p_stock;
                unitArray.get(s).getAsJsonObject().addProperty("actual_stock", l_stock);

                int multiplier = unitArray.get(s).getAsJsonObject().get("conversion_rate").getAsInt();
                finalLogicalStock = l_stock * multiplier;
            }

        }
        return newJsonArray;
    }

    private int getPhysicalStock(InventorySummaryTranxDetailsBatchwise currentStockBatch, List<ProductUnitPacking> unitPackingList, Double closingStock) {
        int currentStock = 0;
        currentStock = currentStockBatch.getClosingStock().intValue();


        int dividend = currentStock;
        int size = unitPackingList.size();//2
        int i = 0;
        int divisor = 1;
        String unitName = "";

        for (int k = 0; k < size; k++) {
            if (unitPackingList.get(k).getUnits().getId() == currentStockBatch.getUnitId()) {
                i = k;
                unitName = unitPackingList.get(i).getUnits().getUnitName();
            }
        }
        for (int s = i; s < size; s++) { //s=1
            divisor = divisor * unitPackingList.get(s).getUnitConversion().intValue();
        }

        int quotient = dividend / divisor;

        return quotient;
    }


    public void convertToMultiUnitRate(List<ProductUnitPacking> unitPackingList, ProductUnitPacking requiredUnit) {
        int index = unitPackingList.indexOf(requiredUnit);
        Double fsrmh = requiredUnit.getFsrmh();
        Double fsrai = requiredUnit.getFsrai();
        Double csrmh = requiredUnit.getCsrmh();
        Double csrai = requiredUnit.getCsrai();
        Double mrp = requiredUnit.getMrp();
        Double purRate = requiredUnit.getPurchaseRate();

        List<ProductUnitPacking> sublist = new ArrayList<>();
        Double unitConvPCS = 1.0;
        // Check if the target object is found
        if (index == 0) {
            // Get the sublist of elements after the index of the target object
            try {
                for (int i = 0; i < unitPackingList.size(); i++) {
                    if (i == unitPackingList.size() - 1) break;
                    Double newFsrmh = fsrmh / unitPackingList.get(i).getUnitConversion();
                    Double newFsrai = fsrai / unitPackingList.get(i).getUnitConversion();
                    Double newCsrmh = csrmh / unitPackingList.get(i).getUnitConversion();
                    Double newCsrai = csrai / unitPackingList.get(i).getUnitConversion();
                    Double newMRP = mrp / unitPackingList.get(i).getUnitConversion();
                    Double newPurRate = purRate / unitPackingList.get(i).getUnitConversion();
                    ProductUnitPacking obj = unitPackingList.get(i + 1);
                    obj.setFsrmh(NumFormat.tranxRoundDigit(newFsrmh, 2));
                    obj.setFsrai(NumFormat.tranxRoundDigit(newFsrai, 2));
                    obj.setCsrmh(NumFormat.tranxRoundDigit(newCsrmh, 2));
                    obj.setCsrai(NumFormat.tranxRoundDigit(newCsrai, 2));
                    obj.setMrp(NumFormat.tranxRoundDigit(newMRP, 2));
                    obj.setPurchaseRate(NumFormat.tranxRoundDigit(newPurRate, 2));

                    fsrmh = newFsrmh;
                    fsrai = newFsrai;
                    csrmh = newCsrmh;
                    csrai = newCsrai;
                    mrp = newMRP;
                    purRate = newPurRate;
                    try {
                        productUnitRepository.save(obj);
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        loggger.error("Error of coverting rates in convertToMultiUnitRate():" + exceptionAsString);
                    }
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                loggger.error("Error in convertToLowerUnit():" + exceptionAsString);
            }
        }
        else if (index == unitPackingList.size() - 1) {
            for (int i = index; i > 0; i--) {
                Double newFsrmh = fsrmh * unitPackingList.get(i - 1).getUnitConversion();
                Double newFsrai = fsrai * unitPackingList.get(i - 1).getUnitConversion();
                Double newCsrmh = csrmh * unitPackingList.get(i - 1).getUnitConversion();
                Double newCsrai = csrai * unitPackingList.get(i - 1).getUnitConversion();
                Double newMRP = mrp * unitPackingList.get(i - 1).getUnitConversion();
                Double newPurRate = purRate * unitPackingList.get(i - 1).getUnitConversion();
                ProductUnitPacking obj = unitPackingList.get(i - 1);
                obj.setFsrmh(NumFormat.tranxRoundDigit(newFsrmh, 2));
                obj.setFsrai(NumFormat.tranxRoundDigit(newFsrai, 2));
                obj.setCsrmh(NumFormat.tranxRoundDigit(newCsrmh, 2));
                obj.setCsrai(NumFormat.tranxRoundDigit(newCsrai, 2));
                obj.setMrp(NumFormat.tranxRoundDigit(newMRP, 2));
                obj.setPurchaseRate(NumFormat.tranxRoundDigit(newPurRate, 2));

                fsrmh = newFsrmh;
                fsrai = newFsrai;
                csrmh = newCsrmh;
                csrai = newCsrai;
                mrp = newMRP;
                purRate = newPurRate;
                try {
                    productUnitRepository.save(obj);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    loggger.error("Error of coverting rates in convertToMultiUnitRate():" + exceptionAsString);
                }
            }
        } else {
            for (int i = 0; i < unitPackingList.size(); i++) {
                ProductUnitPacking obj = null;
                if (index > i) {
                    Double newFsrmh = fsrmh * unitPackingList.get(i).getUnitConversion();
                    Double newFsrai = fsrai * unitPackingList.get(i).getUnitConversion();
                    Double newCsrmh = csrmh * unitPackingList.get(i).getUnitConversion();
                    Double newCsrai = csrai * unitPackingList.get(i).getUnitConversion();
                    Double newMRP = mrp * unitPackingList.get(i).getUnitConversion();
                    Double newPurRate = purRate * unitPackingList.get(i).getUnitConversion();
                    System.out.println("Index > I");
                    System.out.println("FSRMH:" + newFsrmh);
                    System.out.println("FSRAI:" + newFsrai);
                    System.out.println("CSRMH:" + newCsrmh);
                    System.out.println("CSRAI:" + newCsrai);
                    obj = unitPackingList.get(i);
                    obj.setFsrmh(NumFormat.tranxRoundDigit(newFsrmh, 2));
                    obj.setFsrai(NumFormat.tranxRoundDigit(newFsrai, 2));
                    obj.setCsrmh(NumFormat.tranxRoundDigit(newCsrmh, 2));
                    obj.setCsrai(NumFormat.tranxRoundDigit(newCsrai, 2));
                    obj.setMrp(NumFormat.tranxRoundDigit(newMRP, 2));
                    obj.setPurchaseRate(NumFormat.tranxRoundDigit(newPurRate, 2));

                    fsrmh = newFsrmh;
                    fsrai = newFsrai;
                    csrmh = newCsrmh;
                    csrai = newCsrai;
                    mrp = newMRP;
                    purRate = newPurRate;

                    try {
                        productUnitRepository.save(obj);
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        loggger.error("Error of coverting rates in convertToMultiUnitRate():" + exceptionAsString);
                    }
                } else if (index < i) {
                    Double newFsrmh = fsrmh / unitPackingList.get(i - 1).getUnitConversion();
                    Double newFsrai = fsrai / unitPackingList.get(i - 1).getUnitConversion();
                    Double newCsrmh = csrmh / unitPackingList.get(i - 1).getUnitConversion();
                    Double newCsrai = csrai / unitPackingList.get(i - 1).getUnitConversion();
                    Double newMRP = mrp / unitPackingList.get(i - 1).getUnitConversion();
                    Double newPurRate = purRate / unitPackingList.get(i - 1).getUnitConversion();
                    System.out.println("Index < I");
                    System.out.println("FSRMH:" + newFsrmh);
                    System.out.println("FSRAI:" + newFsrai);
                    System.out.println("CSRMH:" + newCsrmh);
                    System.out.println("CSRAI:" + newCsrai);
                    obj = unitPackingList.get(i);
                    obj.setFsrmh(NumFormat.tranxRoundDigit(newFsrmh, 2));
                    obj.setFsrai(NumFormat.tranxRoundDigit(newFsrai, 2));
                    obj.setCsrmh(NumFormat.tranxRoundDigit(newCsrmh, 2));
                    obj.setCsrai(NumFormat.tranxRoundDigit(newCsrai, 2));
                    obj.setMrp(NumFormat.tranxRoundDigit(newMRP, 2));
                    obj.setPurchaseRate(NumFormat.tranxRoundDigit(newPurRate, 2));

                    fsrmh = newFsrmh;
                    fsrai = newFsrai;
                    csrmh = newCsrmh;
                    csrai = newCsrai;
                    mrp = newMRP;
                    purRate = newPurRate;

                    try {
                        productUnitRepository.save(obj);
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        loggger.error("Error of coverting rates in convertToMultiUnitRate():" + exceptionAsString);
                    }
                } else {
                    fsrmh = requiredUnit.getFsrmh();
                    fsrai = requiredUnit.getFsrai();
                    csrmh = requiredUnit.getCsrmh();
                    csrai = requiredUnit.getCsrai();
                    mrp = requiredUnit.getMrp();
                    purRate = requiredUnit.getPurchaseRate();

                }
            }
        }
    }


}
