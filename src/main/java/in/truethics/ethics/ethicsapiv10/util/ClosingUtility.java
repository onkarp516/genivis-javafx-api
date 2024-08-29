package in.truethics.ethics.ethicsapiv10.util;


import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummary;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventorySummaryTranxDetailsBatchwise;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductOpeningStocksRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.StockSummaryRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.StockTranxDetailsBatchwiseRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.StockTranxDetailsRepository;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ClosingUtility {
    private static final Logger inventoryLogger = LogManager.getLogger(ClosingUtility.class);
    @Autowired
    private StockTranxDetailsRepository stockTranxDetailsRepository;
    @Autowired
    private StockTranxDetailsBatchwiseRepository stockTranxDetailsBatchwiseRepository;
    @Autowired
    private StockSummaryRepository stockSummaryRepository;
    @Autowired
    private ProductOpeningStocksRepository productOpeningStocksRepository;

    public InventorySummaryTransactionDetails insertIntoStockDetails(String tranxAction, Date tranxDate, Long invoiceId, Double qty, Branch branch,
                                                                     Outlet outlet, Product product, Long tranxTypeId,
                                                                     Long levelAId, Long levelBId, Long levelCId, Long unitsId,
                                                                     Long batchId, Long fiscalYearId,
                                                                     String serialNo, Double purRate, Double saleRate, Double openingStk,
                                                                     Double closingStk, String tranxUniqueCode,
                                                                     Long userId,Long pkgId) {
        InventorySummaryTransactionDetails mTranxDetails = null;
        try {
            InventorySummaryTransactionDetails inventoryDetailsPostings = new InventorySummaryTransactionDetails();
            inventoryDetailsPostings.setTranxAction(tranxAction);
            inventoryDetailsPostings.setTranxDate(tranxDate);
            inventoryDetailsPostings.setTranxId(invoiceId);
            inventoryDetailsPostings.setQty(qty);
            inventoryDetailsPostings.setFinancialYear(fiscalYearId);
            inventoryDetailsPostings.setBranch(branch);
            inventoryDetailsPostings.setOutlet(outlet);
            inventoryDetailsPostings.setProduct(product);
            inventoryDetailsPostings.setTranxTypeId(tranxTypeId);
            inventoryDetailsPostings.setUnitId(unitsId);
            inventoryDetailsPostings.setBatchId(batchId);
            inventoryDetailsPostings.setStatus(true);
            inventoryDetailsPostings.setSerialNum(serialNo);
            inventoryDetailsPostings.setLevelAId(levelAId);
            inventoryDetailsPostings.setLevelBId(levelBId);
            inventoryDetailsPostings.setLevelCId(levelCId);
            inventoryDetailsPostings.setPurPrice(purRate);
            inventoryDetailsPostings.setSalesPrice(saleRate);
            inventoryDetailsPostings.setOpeningStock(openingStk);
            inventoryDetailsPostings.setClosingStock(closingStk);
            inventoryDetailsPostings.setTranxCode(tranxUniqueCode);
            inventoryDetailsPostings.setCreateBy(userId);
            inventoryDetailsPostings.setPackageId(pkgId);
            inventoryDetailsPostings.setValuation(qty * purRate);
            mTranxDetails = stockTranxDetailsRepository.save(inventoryDetailsPostings);

        } catch (Exception e) {
            inventoryLogger.error("Exception in Inventory Details Postings :" + e.getMessage());
        }
        return mTranxDetails;
    }

    public Double CAL_DR_STOCK(Double openingStock, Double tranxQty, Double free_qty) {
        return openingStock - tranxQty - free_qty;
    }

    public Double CAL_CR_STOCK(Double openingStock, double tranxQty, double free_qty) {
        return openingStock + tranxQty + free_qty;
    }

    public void insertIntoStockSummary(Outlet outlet, Branch branch, Product product, Long batchId, Long unitId,
                                       Double closing, Long fiscalYearId, Date tranxDate, Double openingSummary,
                                       String tranxUniqueCode, Long userId, Long pkgId) {
        InventorySummary summary = null;
        if (branch != null) {
            summary = stockSummaryRepository.
                    findByOutletIdAndBranchIdAndProductIdAndUnitsIdAndTranxDate(outlet.getId(),
                            branch.getId(), product.getId(), unitId, tranxDate);
        } else {
            summary = stockSummaryRepository.findByOutletIdAndBranchIdIsNullAndProductIdAndUnitsIdAndTranxDate(
                    outlet.getId(), product.getId(), unitId, tranxDate);
        }
        if (summary != null) {
            summary.setOpeningStock(openingSummary);
            summary.setClosingStock(closing);
            summary.setUpdatedBy(userId);
        } else {
            summary = new InventorySummary();
            summary.setBranch(branch);
            summary.setOutlet(outlet);
            summary.setProduct(product);
            summary.setUnitsId(unitId);
            summary.setBatchId(batchId);
            summary.setStatus(true);
            summary.setClosingStock(closing);
            summary.setTranxDate(tranxDate);
            summary.setOpeningStock(openingSummary);
            summary.setFiscalYearId(fiscalYearId);
            summary.setTranxCode(tranxUniqueCode);
            summary.setCreateBy(userId);
            summary.setPackageId(pkgId);
        }
        stockSummaryRepository.save(summary);
    }

    public void stockPosting(Outlet outlet, Branch branch, Long fiscalYearId, Long batchId, Product product,
                             Long tranxTypeId, Date invoiceDate, Double tranxQty, Double free_qty, Long tranxId,
                             Long unitId, Long levelAId, Long levelBId, Long levelCId, ProductBatchNo productBatchNo,
                             String tranxUniqueCode, Long userId, String actionType, Long pkgId) {
        try {
            Double openingStock = 0.0;
            Double openingSummary = 0.0;
            Double closingStk = 0.0;
            Long branchId = null;
            Double closing = 0.0;


            InventorySummaryTransactionDetails row = stockTranxDetailsRepository.findTranx(
                    product.getId(), invoiceDate, true, fiscalYearId);
/*            InventorySummaryTransactionDetails row = stockTranxDetailsRepository.findTranx(
                    product.getId(), invoiceDate, true);*/

            if (row != null) {
                /*** adjust the opening and closing from in between records or last records ****/
                openingStock = row.getClosingStock();
            } else {
                Double stkQty = 0.0;
                Double freeQty = 0.0;
                if (branch != null) {
                    branchId = branch.getId();
                }
                stkQty = productOpeningStocksRepository.findSumProductOpeningStocksBatchwise(
                        product.getId(), outlet.getId(), branchId, fiscalYearId, batchId);
                freeQty = productOpeningStocksRepository.findSumProductFreeQtyBatchwise(
                        product.getId(), outlet.getId(), branchId, fiscalYearId, batchId);
                openingStock = stkQty + freeQty;

            }
            if (actionType.equalsIgnoreCase("IN"))
                closing = CAL_CR_STOCK(openingStock, tranxQty, free_qty);
            else
                closing = CAL_DR_STOCK(openingStock, tranxQty, free_qty);
            InventorySummaryTransactionDetails mInventory = insertIntoStockDetails(actionType,
                    invoiceDate, tranxId,
                    tranxQty + free_qty, branch, outlet, product, tranxTypeId,
                    levelAId, levelBId, levelCId, unitId, batchId,
                    fiscalYearId, "",
                    productBatchNo.getCosting(), productBatchNo.getSalesRate(),
                    openingStock, closing, tranxUniqueCode, userId, pkgId);
            List<InventorySummaryTransactionDetails> list = stockTranxDetailsRepository.
                    findSuccessiveRow(product.getId(), mInventory.getId(), invoiceDate, true);
            openingSummary = openingStock;
            openingStock = closing;
            for (InventorySummaryTransactionDetails mDetails : list) {
                mDetails.setOpeningStock(openingStock);
                if (mDetails.getTranxAction().equalsIgnoreCase(actionType))
                    closingStk = CAL_CR_STOCK(openingStock, mDetails.getQty(),
                            free_qty);
                else {
                    closingStk = CAL_DR_STOCK(openingStock, mDetails.getQty(),
                            free_qty);
                }
                mDetails.setClosingStock(closingStk);
                stockTranxDetailsRepository.save(mDetails);
                openingStock = closingStk;
            }


            /***** Date wise summary *****/
            insertIntoStockSummary(outlet, branch, product, productBatchNo.getId(), unitId, closing, fiscalYearId,
                    invoiceDate, openingSummary, tranxUniqueCode, userId, pkgId);
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
    }

    public void updatePosting(InventorySummaryTransactionDetails mInventory, Long productId, Date invoiceDate) {
        Double openingStock = mInventory.getClosingStock();
        Double closingStk = 0.0;
        List<InventorySummaryTransactionDetails> rowList =
                stockTranxDetailsRepository.findSuccessiveRow(productId,
                        mInventory.getId(), invoiceDate, true);
        for (InventorySummaryTransactionDetails mDetails : rowList) {
            mDetails.setOpeningStock(openingStock);
            if (mDetails.getTranxAction().equalsIgnoreCase("IN")) {
                closingStk = CAL_CR_STOCK(openingStock, mDetails.getQty(), 0.0);
            } else {
                closingStk = CAL_DR_STOCK(openingStock, mDetails.getQty(), 0.0);
            }
            mDetails.setClosingStock(closingStk);
            stockTranxDetailsRepository.save(mDetails);
            openingStock = closingStk;
        }
    }

    /**** Batch Wise Stock *****/
    public void stockPostingBatchWise(Outlet outlet, Branch branch, Long fiscalYearId, Long batchId, Product product,
                                      Long tranxTypeId, Date invoiceDate, Double tranxQty, Double free_qty, Long tranxId,
                                      Long unitId, Long levelAId, Long levelBId, Long levelCId, ProductBatchNo productBatchNo,
                                      String tranxUniqueCode, Long userId, String actionType, Long pkgId) {
        try {
            Double openingStock = 0.0;
            Double openingSummary = 0.0;
            Double closingStk = 0.0;
            Long branchId = null;
            Double closing = 0.0;


            InventorySummaryTranxDetailsBatchwise row = stockTranxDetailsBatchwiseRepository.findTranx(
                    product.getId(), invoiceDate, true, fiscalYearId, batchId);
/*            InventorySummaryTransactionDetails row = stockTranxDetailsRepository.findTranx(
                    product.getId(), invoiceDate, true);*/

            if (row != null) {
                /*** adjust the opening and closing from in between records or last records ****/
                openingStock = row.getClosingStock();
            } else {
                Double stkQty = 0.0;
                Double freeQty = 0.0;
                if (branch != null) {
                    branchId = branch.getId();
                }
                stkQty = productOpeningStocksRepository.findSumProductOpeningStocksBatchwise(
                        product.getId(), outlet.getId(), branchId, fiscalYearId, batchId);
                freeQty = productOpeningStocksRepository.findSumProductFreeQtyBatchwise(
                        product.getId(), outlet.getId(), branchId, fiscalYearId, batchId);
                openingStock = stkQty + freeQty;

            }
            if (actionType.equalsIgnoreCase("IN"))
                closing = CAL_CR_STOCK(openingStock, tranxQty, free_qty);
            else
                closing = CAL_DR_STOCK(openingStock, tranxQty, free_qty);
            InventorySummaryTranxDetailsBatchwise mInventory = insertIntoStockDetailsBatchWise(actionType,
                    invoiceDate, tranxId,
                    tranxQty + free_qty, branch, outlet, product, tranxTypeId,
                    levelAId, levelBId, levelCId, unitId, batchId,
                    fiscalYearId, "",
                    productBatchNo.getCosting(), productBatchNo.getSalesRate(),
                    openingStock, closing, tranxUniqueCode, userId, pkgId);
            List<InventorySummaryTranxDetailsBatchwise> list = stockTranxDetailsBatchwiseRepository.
                    findSuccessiveRow(product.getId(), mInventory.getId(), invoiceDate, true, batchId);
            openingSummary = openingStock;
            openingStock = closing;
            for (InventorySummaryTranxDetailsBatchwise mDetails : list) {
                mDetails.setOpeningStock(openingStock);
                if (mDetails.getTranxAction().equalsIgnoreCase(actionType))
                    closingStk = CAL_CR_STOCK(openingStock, mDetails.getQty(),
                            free_qty);
                else {
                    closingStk = CAL_DR_STOCK(openingStock, mDetails.getQty(),
                            free_qty);
                }
                mDetails.setClosingStock(closingStk);
                stockTranxDetailsBatchwiseRepository.save(mDetails);
                openingStock = closingStk;
            }


        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
    }

    /**** Batch Wise  ****/


    public InventorySummaryTranxDetailsBatchwise insertIntoStockDetailsBatchWise(String tranxAction, Date tranxDate, Long invoiceId, Double qty, Branch branch,
                                                                                 Outlet outlet, Product product, Long tranxTypeId,
                                                                                 Long levelAId, Long levelBId, Long levelCId, Long unitsId,
                                                                                 Long batchId, Long fiscalYearId,
                                                                                 String serialNo, Double purRate, Double saleRate, Double openingStk,
                                                                                 Double closingStk, String tranxUniqueCode,
                                                                                 Long userId, Long pkgId) {
        InventorySummaryTranxDetailsBatchwise mTranxDetails = null;
        try {
            InventorySummaryTranxDetailsBatchwise inventoryDetailsPostings = new InventorySummaryTranxDetailsBatchwise();
            inventoryDetailsPostings.setTranxAction(tranxAction);
            inventoryDetailsPostings.setTranxDate(tranxDate);
            inventoryDetailsPostings.setTranxId(invoiceId);
            inventoryDetailsPostings.setQty(qty);
            inventoryDetailsPostings.setFinancialYear(fiscalYearId);
            inventoryDetailsPostings.setBranch(branch);
            inventoryDetailsPostings.setOutlet(outlet);
            inventoryDetailsPostings.setProduct(product);
            inventoryDetailsPostings.setTranxTypeId(tranxTypeId);
            inventoryDetailsPostings.setUnitId(unitsId);
            inventoryDetailsPostings.setBatchId(batchId);
            inventoryDetailsPostings.setStatus(true);
            inventoryDetailsPostings.setSerialNum(serialNo);
            inventoryDetailsPostings.setLevelAId(levelAId);
            inventoryDetailsPostings.setLevelBId(levelBId);
            inventoryDetailsPostings.setLevelCId(levelCId);
            inventoryDetailsPostings.setPurPrice(purRate);
            inventoryDetailsPostings.setSalesPrice(saleRate);
            inventoryDetailsPostings.setOpeningStock(openingStk);
            inventoryDetailsPostings.setClosingStock(closingStk);
            inventoryDetailsPostings.setTranxCode(tranxUniqueCode);
            inventoryDetailsPostings.setCreateBy(userId);
            inventoryDetailsPostings.setPackageId(pkgId);
            inventoryDetailsPostings.setValuation(qty * purRate);
            mTranxDetails = stockTranxDetailsBatchwiseRepository.save(inventoryDetailsPostings);

        } catch (Exception e) {
            inventoryLogger.error("Exception in Inventory Details Postings :" + e.getMessage());
        }
        return mTranxDetails;
    }

}


