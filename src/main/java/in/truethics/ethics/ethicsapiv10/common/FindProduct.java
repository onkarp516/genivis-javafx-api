package in.truethics.ethics.ethicsapiv10.common;

import in.truethics.ethics.ethicsapiv10.model.inventory.InventoryDetailsPostings;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallanDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurOrderDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.InventoryDetailsPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurChallanDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurInvoiceDetailsUnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurReturnDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FindProduct {
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;

    @Autowired
    TranxPurReturnDetailsUnitRepository tranxPurReturnDetailsUnitRepository;

    @Autowired
    TranxSalesReturnDetailsUnitsRepository tranxSalesReturnDetailsUnitsRepository;

    @Autowired
    TranxPurInvoiceDetailsUnitsRepository tranxPurInvoiceDetailsUnitsRepository;

    @Autowired
    TranxPurOrderDetailsUnitRepository tranxPurOrderDetailsUnitRepository;

    @Autowired
    TranxPurChallanDetailsUnitRepository tranxPurChallanDetailsUnitRepository;

    @Autowired
    TranxSalesQuotaionDetailsUnitsRepository tranxSalesQuotaionDetailsUnitsRepository;

    @Autowired
    TranxSalesOrderDetailsUnitsRepository tranxSalesOrderDetailsUnitsRepository;

    @Autowired
    TranxSalesChallanDetailsUnitsRepository tranxSalesChallanDetailsUnitsRepository;
    @Autowired
    TranxSalesInvoiceDetailsUnitRepository tranxSalesInvoiceDetailsUnitRepository;
    @Autowired
    private InventoryDetailsPostingsRepository inventoryDetailsPostingsRepository;


    public int removeCommonMethod(long object) {
       /* List<LedgerTransactionPostings> ledgerTransactionPostings = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndStatus(true);

        for (LedgerTransactionPostings ltPosting : ledgerTransactionPostings) {
            long transactionId = ltPosting.getTransactionId();
            if (ltPosting.getTransactionType().getId() == 1) {
                List<TranxPurInvoiceDetailsUnits> tranxPurInvoiceDetailsUnits = tranxPurInvoiceDetailsUnitsRepository.findByPurchaseTransactionIdAndStatus(transactionId, true);
                if (tranxPurInvoiceDetailsUnits.size() > 0 && tranxPurInvoiceDetailsUnits != null) {
                    for (TranxPurInvoiceDetailsUnits mDetails : tranxPurInvoiceDetailsUnits) {
                        if (mDetails.getProduct() != null && mDetails.getProduct().getId() == object) {
                            count++;
                            break;
                       }

                    }
                }
            } else if (ltPosting.getTransactionType().getId() == 2) {
                List<TranxPurReturnDetailsUnits> tranxPurReturnDetailsUnits = tranxPurReturnDetailsUnitRepository.findByTranxPurReturnInvoiceIdAndStatus(transactionId, true);
                if (tranxPurReturnDetailsUnits.size() > 0 && tranxPurReturnDetailsUnits != null) {
                    for (TranxPurReturnDetailsUnits mDetails : tranxPurReturnDetailsUnits) {

                        if (mDetails.getProduct() != null && mDetails.getProduct().getId() == object) {
                            count++;
                            break;
                        }
                    }
                }

            } else if (ltPosting.getTransactionType().getId() == 3) {
                List<TranxSalesInvoiceDetailsUnits> salesInvoiceDetailsUnits = tranxSalesInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(transactionId, true);
                if (salesInvoiceDetailsUnits.size() > 0 && salesInvoiceDetailsUnits != null) {
                    for (TranxSalesInvoiceDetailsUnits mDetails : salesInvoiceDetailsUnits) {
                        if (mDetails.getProduct() != null && mDetails.getProduct().getId() == object) {
                            count++;
                            break;
                        }

                    }
                }

            } else if (ltPosting.getTransactionType().getId() == 4) {
                List<TranxSalesReturnDetailsUnits> tranxSalesReturnDetailsUnits = tranxSalesReturnDetailsUnitsRepository.findBySalesReturnInvoiceIdAndStatus(transactionId, true);
                if (tranxSalesReturnDetailsUnits.size() > 0 && tranxSalesReturnDetailsUnits != null) {
                    for (TranxSalesReturnDetailsUnits mDetails : tranxSalesReturnDetailsUnits) {
                        if (mDetails.getProduct() != null && mDetails.getProduct().getId() == object) {
                            count++;
                            break;
                        }

                    }
                }
            } else if (ltPosting.getTransactionType().getId() == 11) {
                List<TranxPurOrderDetailsUnits> tranxPurOrderDetailsUnits = tranxPurOrderDetailsUnitRepository.findByTranxPurOrderIdAndStatus(transactionId, true);
                if (tranxPurOrderDetailsUnits.size() > 0 && tranxPurOrderDetailsUnits != null) {
                    for (TranxPurOrderDetailsUnits mDetails : tranxPurOrderDetailsUnits) {
                        if (mDetails.getProduct() != null && mDetails.getProduct().getId() == object) {
                            count++;
                            break;
                        }

                    }
                }
            } else if (ltPosting.getTransactionType().getId() == 12) {
                List<TranxPurChallanDetailsUnits> tranxPurChallanDetailsUnits = tranxPurChallanDetailsUnitRepository.findByTranxPurChallanIdAndStatus(transactionId, true);
                if (tranxPurChallanDetailsUnits.size() > 0 && tranxPurChallanDetailsUnits != null) {
                    for (TranxPurChallanDetailsUnits mDetails : tranxPurChallanDetailsUnits) {
                        if (mDetails.getProduct() != null && mDetails.getProduct().getId() == object) {
                            count++;
                            break;
                        }

                    }
                }
            } else if (ltPosting.getTransactionType().getId() == 13) {
                List<TranxSalesQuotationDetailsUnits> tranxSalesQuotationDetailsUnits = tranxSalesQuotaionDetailsUnitsRepository.findBySalesQuotationIdAndStatus(transactionId, true);
                if (tranxSalesQuotationDetailsUnits.size() > 0 && tranxSalesQuotationDetailsUnits != null) {
                    for (TranxSalesQuotationDetailsUnits mDetails : tranxSalesQuotationDetailsUnits) {
                        if (mDetails.getProduct() != null && mDetails.getProduct().getId() == object) {
                            count++;
                            break;
                        }

                    }
                }
            } else if (ltPosting.getTransactionType().getId() == 14) {
                List<TranxSalesOrderDetailsUnits> tranxSalesOrderDetailsUnits = tranxSalesOrderDetailsUnitsRepository.findBySalesOrderIdAndStatus(transactionId, true);
                if (tranxSalesOrderDetailsUnits.size() > 0 && tranxSalesOrderDetailsUnits != null) {
                    for (TranxSalesOrderDetailsUnits mDetails : tranxSalesOrderDetailsUnits) {
                        if (mDetails.getProduct() != null && mDetails.getProduct().getId() == object) {
                            count++;
                            break;
                        }

                    }
                }
            } else if (ltPosting.getTransactionType().getId() == 15) {
                List<TranxSalesChallanDetailsUnits> tranxSalesChallanDetailsUnits = tranxSalesChallanDetailsUnitsRepository.findBySalesChallanIdAndStatus(transactionId, true);
                if (tranxSalesChallanDetailsUnits.size() > 0 && tranxSalesChallanDetailsUnits != null) {
                    for (TranxSalesChallanDetailsUnits mDetails : tranxSalesChallanDetailsUnits) {
                        if (mDetails.getProduct() != null && mDetails.getProduct().getId() == object) {
                            count++;
                            break;
                        }

                    }
                }
            } else {
                System.out.println("table is Empty");
            }
        }
        return count;*/
        int count = 0;
        List<InventoryDetailsPostings> inventoryList = new ArrayList<>();
        inventoryList = inventoryDetailsPostingsRepository.findByProductIdAndStatus(object, true);
        if (inventoryList != null && inventoryList.size() > 0) {
            count = 1;
        }
        return count;
    }
}
