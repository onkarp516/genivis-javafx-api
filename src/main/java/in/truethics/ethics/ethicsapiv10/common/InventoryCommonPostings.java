package in.truethics.ethics.ethicsapiv10.common;

import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.InventoryDetailsPostings;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.*;
//import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.InventoryDetailsPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.InventoryDetailsPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductOpeningStocksRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

@Component
public class InventoryCommonPostings {
    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;

    @Autowired
    private ProductOpeningStocksRepository productOpeningStocksRepository;

    private static final Logger inventoryLogger = LogManager.getLogger(InventoryCommonPostings.class);

    public void callToInventoryPostings(String tranxAction, Date tranxDate, Long invoiceId, Double qty, Branch branch,
                                        Outlet outlet, Product product, TransactionTypeMaster tranxType,
                                        LevelA levelA, LevelB levelB, LevelC levelC, Units units,
                                        ProductBatchNo productBatch, String uniqueBatchno, FiscalYear fiscalYear,
                                        String serialNo) {
        try {
            System.out.println("Call To Inventory Postings....");
            InventoryDetailsPostings inventoryDetailsPostings = new InventoryDetailsPostings();
            inventoryDetailsPostings.setTranxAction(tranxAction);
            inventoryDetailsPostings.setTranxDate(tranxDate);
            inventoryDetailsPostings.setTranxId(invoiceId);
            inventoryDetailsPostings.setQty(qty);
            inventoryDetailsPostings.setFiscalYear(fiscalYear);
            inventoryDetailsPostings.setBranch(branch);
            inventoryDetailsPostings.setOutlet(outlet);
            inventoryDetailsPostings.setProduct(product);
            inventoryDetailsPostings.setTransactionType(tranxType);
            inventoryDetailsPostings.setLevelA(levelA);
            inventoryDetailsPostings.setLevelB(levelB);
            inventoryDetailsPostings.setLevelC(levelC);
            inventoryDetailsPostings.setUnits(units);
            inventoryDetailsPostings.setProductBatch(productBatch);
            inventoryDetailsPostings.setStatus(true);
            inventoryDetailsPostings.setUniqueBatchNo(uniqueBatchno);
            inventoryDetailsPostings.setSerialNo(serialNo);
            inventoryDetailsPostingsRepository.save(inventoryDetailsPostings);
        } catch (Exception e) {
            inventoryLogger.error("Exception in Inventory Details Postings :" + e.getMessage());
        }
    }

    public Double getClosingStock(Long productId, Long outletId, Long branchId, LocalDate startDate, LocalDate endDate,
                                  Boolean flag, FiscalYear fiscalYear) {
        Double openingStocks = null;
        Double closing = 0.0;
        Double crOpening = 0.0;
        Double drOpening = 0.0;
        Double opening = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;

        try {
            if (flag == true) {
                drClosing = inventoryDetailsPostingsRepository.findClosing(productId, outletId, branchId, "DR", startDate, endDate);
                crClosing = inventoryDetailsPostingsRepository.findClosing(productId, outletId, branchId, "CR", startDate, endDate);
                LocalDate previousDate = startDate.minusDays(1);
                crOpening = inventoryDetailsPostingsRepository.findProductOpeningStocks(productId, outletId, branchId, previousDate, "CR");
                drOpening = inventoryDetailsPostingsRepository.findProductOpeningStocks(productId, outletId, branchId, previousDate, "DR");
                opening = crOpening - drOpening;
                closing = opening - drClosing + crClosing;
            } else {
                openingStocks = productOpeningStocksRepository.findProductOpeningStocks(productId, fiscalYear.getId(), outletId, branchId);
                if (openingStocks != null) {
                    opening = openingStocks;
                }
                drClosing = inventoryDetailsPostingsRepository.findFiscalyearClosing(productId, outletId, branchId, "DR", fiscalYear.getId());
                crClosing = inventoryDetailsPostingsRepository.findFiscalyearClosing(productId, outletId, branchId, "CR", fiscalYear.getId());
                closing = opening - drClosing + crClosing;
            }

            System.out.println("\nProduct Id:" + productId + " Closing Stocks:" + closing);
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return closing;
    }

    public Double getOpeningStock(Long productId, Long outletId, Long branchId, LocalDate startDate, LocalDate endDate,
                                  Boolean flag, FiscalYear fiscalYear) {
        Double openingStocks = 0.0;
        Double closing = 0.0;
        Double crOpening = 0.0;
        Double drOpening = 0.0;
        Double opening = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;

        try {
            if (flag == true) {
                openingStocks = inventoryDetailsPostingsRepository.findOpening(productId, outletId, branchId, startDate, endDate);
                if (openingStocks != null) {
                    opening = openingStocks;
                }

            } else {
//                openingStocks = inventoryDetailsPostingsRepository.findFiscalyearOpening(productId,outletId, branchId,fiscalYear.getId());

                openingStocks = productOpeningStocksRepository.findProductOpeningStocks(productId, fiscalYear.getId(), outletId, branchId);
                if (openingStocks != null) {
                    opening = openingStocks;
                }
//                drClosing = inventoryDetailsPostingsRepository.findFiscalyearClosing(productId, outletId, branchId, "DR", fiscalYear.getId());
//                crClosing = inventoryDetailsPostingsRepository.findFiscalyearClosing(productId, outletId, branchId, "CR", fiscalYear.getId());
//                opening = drClosing + crClosing;
            }

            System.out.println("\nProduct Id:" + productId + " Closing Stocks:" + closing);
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return opening;
    }

    public Double getInwordStock(Long productId, Long outletId, Long branchId, LocalDate startDate, LocalDate endDate,
                                 Boolean flag, FiscalYear fiscalYear) {
        Double inwordStocks = 0.0;
        Double closing = 0.0;
        Double crOpening = 0.0;
        Double drOpening = 0.0;
        Double opening = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;

        try {
            if (flag == true) {
                inwordStocks = inventoryDetailsPostingsRepository.findInword(productId, outletId, branchId, "CR", startDate, endDate);

                if (inwordStocks != null) {
                    opening = inwordStocks;
                }

            } else {

                inwordStocks = inventoryDetailsPostingsRepository.findInwordFiscYear(productId, outletId, branchId, "CR", fiscalYear.getId());
                if (inwordStocks != null) {
                    opening = inwordStocks;
                }

            }

            System.out.println("\nProduct Id:" + productId + " Closing Stocks:" + closing);
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return opening;
    }

    public Double getOutwordStock(Long productId, Long outletId, Long branchId, LocalDate startDate, LocalDate endDate,
                                  Boolean flag, FiscalYear fiscalYear) {
        Double outwordStocks = 0.0;
        Double closing = 0.0;
        Double crOpening = 0.0;
        Double drOpening = 0.0;
        Double opening = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;

        try {
            if (flag == true) {
                outwordStocks = inventoryDetailsPostingsRepository.findInword(productId, outletId, branchId, "DR", startDate, endDate);

                if (outwordStocks != null) {
                    opening = outwordStocks;
                }

            } else {

                outwordStocks = inventoryDetailsPostingsRepository.findInwordFiscYear(productId, outletId, branchId, "DR", fiscalYear.getId());
                if (outwordStocks != null) {
                    opening = outwordStocks;
                }

            }

            System.out.println("\nProduct Id:" + productId + " Closing Stocks:" + closing);
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return opening;
    }


    //    public void callToEditInventoryPostings(LocalDate tranxDate, Long invoiceId, double qty, Branch branch, Outlet outlet,
//                                            Product mProduct, TransactionTypeMaster tranxType,
//                                            PackingMaster packaging, Group group,
//                                            Units units, Brand brand, Category category, Subcategory subcategory,
//                                            ProductBatchNo productBatch, String batchNo, FiscalYear fiscalYear) {
//
//    }
    public Double getClosingStockProduct(Long productId, Long outletId, Long branchId, FiscalYear fiscalYear) {
        Double closing = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;

        try {
            if (branchId != null) {
                drClosing = inventoryDetailsPostingsRepository.findClosingWithBranch(productId, outletId, branchId, "DR", fiscalYear.getId());
                crClosing = inventoryDetailsPostingsRepository.findClosingWithBranch(productId, outletId, branchId, "CR", fiscalYear.getId());

            } else {
                drClosing = inventoryDetailsPostingsRepository.findClosingWithoutBranch(productId, outletId, "DR", fiscalYear.getId());
                crClosing = inventoryDetailsPostingsRepository.findClosingWithoutBranch(productId, outletId, "CR", fiscalYear.getId());
            }
            closing = crClosing - drClosing;
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return closing;
    }

    public Double getmobileClosingStockProduct(Long productId, FiscalYear fiscalYear) {
        Double closing = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;
        try {

            drClosing = inventoryDetailsPostingsRepository.findMobileClosingWithoutBranch(productId, "DR", fiscalYear.getId());
            crClosing = inventoryDetailsPostingsRepository.findMobileClosingWithoutBranch(productId, "CR", fiscalYear.getId());

            closing = crClosing - drClosing;
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return closing;
    }

    public Double getClosingStockProductFilters(Long branch, Long outlet,
                                                Long mProduct,
                                                Long levelAId, Long levelbId,
                                                Long levelCId, Long units,
                                                Long batchId, Long fiscalYear) {
        Double closing = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;
        try {
            if (branch != null) {
                drClosing = inventoryDetailsPostingsRepository.findClosingWithBranchFilter(branch, outlet, mProduct,
                        levelAId, levelbId, levelCId, units, batchId, fiscalYear, "DR");
                crClosing = inventoryDetailsPostingsRepository.findClosingWithBranchFilter(branch, outlet, mProduct,
                        levelAId, levelbId, levelCId, units, batchId, fiscalYear, "CR");
            } else {
                drClosing = inventoryDetailsPostingsRepository.findClosingWithoutBranchFilter(outlet, mProduct,
                        levelAId, levelbId, levelCId, units, batchId, fiscalYear, "DR");
                crClosing = inventoryDetailsPostingsRepository.findClosingWithoutBranchFilter(outlet, mProduct,
                        levelAId, levelbId, levelCId, units, batchId, fiscalYear, "CR");
            }
            closing = crClosing - drClosing;
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return closing;
    }

    /****** closing calculations of Product List Page *****/
    public Double getClosingStockProductBatch(Long branch, Long outlet,
                                                Long mProduct,
                                                Long levelAId, Long levelbId,
                                                Long levelCId,
                                                Long batchId, Long fiscalYear) {
        Double closing = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;
        try {
            if (branch != null) {

                drClosing = inventoryDetailsPostingsRepository.findClosingWithProductBatch(branch, outlet, mProduct,
                        levelAId, levelbId, levelCId,  batchId, fiscalYear, "DR");
                crClosing = inventoryDetailsPostingsRepository.findClosingWithProductBatch(branch, outlet, mProduct,
                        levelAId, levelbId, levelCId,  batchId, fiscalYear, "CR");
            } else {
                drClosing = inventoryDetailsPostingsRepository.findClosingProductBatchWithoutBranch(outlet, mProduct,
                        levelAId, levelbId, levelCId,  batchId, fiscalYear, "DR");
                crClosing = inventoryDetailsPostingsRepository.findClosingProductBatchWithoutBranch(outlet, mProduct,
                        levelAId, levelbId, levelCId,  batchId, fiscalYear, "CR");
            }
            closing = crClosing - drClosing;
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return closing;
    }


    public void callToEditInventoryPostings(Date invoiceDate, Long id, double qty, Branch branch, Outlet outlet, Product mProduct, TransactionTypeMaster tranxType, LevelA levelA, LevelB levelB, LevelC levelC, Units units, ProductBatchNo productBatchNo, String batchNo, FiscalYear fiscalYear) {
        try {
            Long branchId = null;
            Long unitId = null;
            Long levelaId = null;
            Long levelbId = null;
            Long levelcId = null;
            Long produdctBatchId = null;
            Long fiscalyearId = null;
            if (branch != null)
                branchId = branch.getId();
            if (units != null) {
                unitId = units.getId();
            }
            if (levelA != null)
                levelaId = levelA.getId();
            if (levelB != null)
                levelbId = levelB.getId();
            if (levelC != null)
                levelcId = levelC.getId();
            if (productBatchNo != null)
                produdctBatchId = productBatchNo.getId();
            if (fiscalYear != null)
                fiscalyearId = fiscalYear.getId();
            InventoryDetailsPostings inventoryDetailsPostings = inventoryDetailsPostingsRepository.findByRow(mProduct.getId(),
                    fiscalyearId, outlet.getId(), branchId, tranxType.getId(), id, levelaId, levelbId, levelcId, produdctBatchId, unitId);
            if (inventoryDetailsPostings != null) {
                inventoryDetailsPostings.setTranxDate(invoiceDate);
                inventoryDetailsPostings.setQty(qty);
                inventoryDetailsPostings.setProductBatch(productBatchNo);
                inventoryDetailsPostingsRepository.save(inventoryDetailsPostings);
            }
        } catch (Exception e) {
            e.printStackTrace();
            inventoryLogger.error("Exception in Inventory Details Postings :" + e.getMessage());
        }
    }

    public void callToUpdateInventoryOfProduct(long inventoryId, Long invoiceId, Double qty) {
        try {
            InventoryDetailsPostings inventoryDetailsPostings = inventoryDetailsPostingsRepository.findByIdAndStatus(inventoryId, true);
            if (inventoryDetailsPostings != null) {
                inventoryDetailsPostings.setTranxId(invoiceId);
                inventoryDetailsPostings.setQty(qty);
                inventoryDetailsPostingsRepository.save(inventoryDetailsPostings);
            }
        } catch (Exception e) {
            e.printStackTrace();
            inventoryLogger.error("Exception in Inventory Details Postings callToUpdateInventoryOfProduct :" + e.getMessage());
        }
    }

    public Double calculateOpening(Long id, Long id1, Long branchId, FiscalYear fiscalYear) {

        Double openingStock = productOpeningStocksRepository.findSumProductOpeningStocks(id, id1, branchId,fiscalYear.getId());
        return openingStock;
    }
    public Double calculateFreeQty(Long id, Long id1, Long branchId, FiscalYear fiscalYear) {

        Double freeQty = productOpeningStocksRepository.findSumProductFreeQty(id, id1, branchId);
        return freeQty;
    }
}

