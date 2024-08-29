package in.truethics.ethics.ethicsapiv10.common;

import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

@Component
public class LedgerCommonPostings {
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    private static final Logger postingLogger = LogManager.getLogger(LedgerCommonPostings.class);

    public void callToPostings(Double totalAmount, LedgerMaster ledgerMaster, TransactionTypeMaster tranxTypeMaster,
                               AssociateGroups associateGroups, FiscalYear fiscalYear, Branch branch, Outlet outlet,
                               Date invoiceDate, Long invoiceId, String vendorInvoiceNo, String crdrType,
                               boolean status, String tranxType, String operations)     {
        try {
            LedgerTransactionPostings ledgerTransactionPostings = new LedgerTransactionPostings();
            ledgerTransactionPostings.setAmount(totalAmount);
            ledgerTransactionPostings.setLedgerMaster(ledgerMaster);
            ledgerTransactionPostings.setTransactionType(tranxTypeMaster);
            ledgerTransactionPostings.setAssociateGroups(associateGroups);
            ledgerTransactionPostings.setFiscalYear(fiscalYear);
            ledgerTransactionPostings.setBranch(branch);
            ledgerTransactionPostings.setOutlet(outlet);
            ledgerTransactionPostings.setTransactionDate(invoiceDate);
            ledgerTransactionPostings.setTransactionId(invoiceId);
            ledgerTransactionPostings.setInvoiceNo(vendorInvoiceNo);
            ledgerTransactionPostings.setLedgerType(crdrType);
            ledgerTransactionPostings.setTranxType(tranxType);
            ledgerTransactionPostings.setOperations(operations);
            ledgerTransactionPostings.setStatus(status);
            ledgerTransactionPostingsRepository.save(ledgerTransactionPostings);
        } catch (Exception e) {
            postingLogger.error("Exception in Postings :" + e.getMessage());
        }
    }

    public Double getOpeningStock(Long ledgerId, Long outletId, Long branchId, LocalDate startDate, LocalDate endDate,
                                  Boolean flag, FiscalYear fiscalYear) {
        Double openingStocks = 0.0;
        Double closing = 0.0;
        Double crOpening = 0.0;
        Double drOpening = 0.0;
        Double opening = 0.0;
        Double drClosing = 0.0;
        Double crClosing = 0.0;
        Double opning_balance = 0.0;

        try {
            if (flag == true) {
                if (branchId != null) {
                    LocalDate previousDate = startDate.minusDays(1);
                    crOpening = ledgerTransactionPostingsRepository.findLedgerOpeningBranch(ledgerId, outletId, branchId, "CR", previousDate);
                    drOpening = ledgerTransactionPostingsRepository.findLedgerOpeningBranch(ledgerId, outletId, branchId, "DR", previousDate);
                    openingStocks = crOpening - drOpening;
                } else {
                    LocalDate previousDate = startDate.minusDays(1);
                    crOpening = ledgerTransactionPostingsRepository.findLedgerOpening(ledgerId, outletId, "CR", previousDate);
                    drOpening = ledgerTransactionPostingsRepository.findLedgerOpening(ledgerId, outletId, "DR", previousDate);
                    openingStocks = crOpening - drOpening;
                }
                if (openingStocks != null) {
                    opening = openingStocks;
                }

            }
            if (branchId != null) {
                crOpening = ledgerMasterRepository.findLedgerOpeningStocksBranch(ledgerId, outletId, branchId, "CR");
                drOpening = ledgerMasterRepository.findLedgerOpeningStocksBranch(ledgerId, outletId, branchId, "DR");
                openingStocks = crOpening - drOpening;
            } else {
                crOpening = ledgerMasterRepository.findLedgerOpeningStocks(ledgerId, outletId, "CR");
                drOpening = ledgerMasterRepository.findLedgerOpeningStocks(ledgerId, outletId, "DR");
                openingStocks = crOpening - drOpening;
            }
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return openingStocks + opening;
    }


    public Double getmobileOpeningStock(Long ledgerId, LocalDate startDate, LocalDate endDate,
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

                LocalDate previousDate = startDate.minusDays(1);
                crOpening = ledgerTransactionPostingsRepository.findMobileLedgerOpening(ledgerId, "CR", previousDate);
                drOpening = ledgerTransactionPostingsRepository.findMobileLedgerOpening(ledgerId, "DR", previousDate);
                openingStocks = crOpening - drOpening;

                if (openingStocks != null) {
                    opening = openingStocks;
                }

            } else {
//                openingStocks = inventoryDetailsPostingsRepository.findFiscalyearOpening(productId,outletId, branchId,fiscalYear.getId());

                crOpening = ledgerMasterRepository.findMobileLedgerOpeningStocks(ledgerId, "CR");
                drOpening = ledgerMasterRepository.findMobileLedgerOpeningStocks(ledgerId, "DR");
                openingStocks = crOpening - drOpening;


                if (openingStocks != null) {
                    opening = openingStocks;
                }
//                drClosing = inventoryDetailsPostingsRepository.findFiscalyearClosing(productId, outletId, branchId, "DR", fiscalYear.getId());
//                crClosing = inventoryDetailsPostingsRepository.findFiscalyearClosing(productId, outletId, branchId, "CR", fiscalYear.getId());
//                opening = drClosing + crClosing;
            }

            System.out.println("\nProduct Id:" + ledgerId + " opening Stocks:" + opening);
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
        }
        return opening;
    }
    public void callToPostingsTranxCode(Double totalAmount, LedgerMaster ledgerMaster, TransactionTypeMaster tranxTypeMaster,
                                        AssociateGroups associateGroups, FiscalYear fiscalYear, Branch branch, Outlet outlet,
                                        Date invoiceDate, Long invoiceId, String vendorInvoiceNo, String crdrType,
                                        boolean status, String tranxType, String operations, String tranxCode) {
        try {
            LedgerTransactionPostings ledgerTransactionPostings = new LedgerTransactionPostings();
            ledgerTransactionPostings.setAmount(totalAmount);
            ledgerTransactionPostings.setLedgerMaster(ledgerMaster);
            ledgerTransactionPostings.setTransactionType(tranxTypeMaster);
            ledgerTransactionPostings.setAssociateGroups(associateGroups);
            ledgerTransactionPostings.setFiscalYear(fiscalYear);
            ledgerTransactionPostings.setBranch(branch);
            ledgerTransactionPostings.setOutlet(outlet);
            ledgerTransactionPostings.setTransactionDate(invoiceDate);
            ledgerTransactionPostings.setTransactionId(invoiceId);
            ledgerTransactionPostings.setInvoiceNo(vendorInvoiceNo);
            ledgerTransactionPostings.setLedgerType(crdrType);
            ledgerTransactionPostings.setTranxType(tranxType);
            ledgerTransactionPostings.setOperations(operations);
            ledgerTransactionPostings.setStatus(status);
            ledgerTransactionPostings.setTranxCode(tranxCode);
            ledgerTransactionPostingsRepository.save(ledgerTransactionPostings);
        } catch (Exception e) {
            postingLogger.error("Exception in Postings :" + e.getMessage());
        }
    }




}

