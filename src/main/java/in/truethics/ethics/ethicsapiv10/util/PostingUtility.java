package in.truethics.ethics.ethicsapiv10.util;

import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerBalanceSummary;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerClosingDateSummary;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerBalanceSummaryRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerClosingDateSummaryRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerOpeningClosingDetailRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Component
public class PostingUtility {
    private static final Logger postingLogger = LogManager.getLogger(PostingUtility.class);
    @Autowired
    private LedgerOpeningClosingDetailRepository detailRepository;
    @Autowired
    private LedgerBalanceSummaryRepository ledgerBalanceSummaryRepository;
    @Autowired
    private LedgerClosingDateSummaryRepository ledgerClosingDateSummaryRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;

    public void callToPostingLedgerForUpdateByDetailsId(Double amount, Long oldLedgerId, TransactionTypeMaster tranxType,
                                                        String ledgerType, Long tranxId, LedgerMaster ledgerMaster, Date dt, FiscalYear fiscalYear,
                                                        Outlet outlet, Branch branch, String tranxCode) {
        try {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            LedgerOpeningClosingDetail ledgerDetail = detailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                    oldLedgerId, tranxType.getId(), tranxId, true);
            if (ledgerDetail != null) {
                if (ledgerDetail.getTranxDate().compareTo(dt) == 0 && ledgerDetail.getAmount() != amount) { // DATE SAME, AMOUNT DIFFERENT
                    Double closing = 0.0;
                    if (ledgerType.equalsIgnoreCase("cr"))
                        closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), amount, 0.0);
                    if (ledgerType.equalsIgnoreCase("dr"))
                        closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, amount);
                    ledgerDetail.setAmount(amount);
                    ledgerDetail.setClosingAmount(closing);
                    LedgerOpeningClosingDetail detail = detailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    updateLedgerPostings(ledgerMaster, dt, tranxType, fiscalYear, detail);
                } else if (ledgerDetail.getTranxDate().compareTo(dt) != 0) { // DATE DIFFERENT
                    Date oldDate = ledgerDetail.getTranxDate();
                    Date newDate = dt;

                    if (newDate.after(oldDate)) { // forward date
                        // old date record update
                        Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = detailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        updateLedgerPostingsBetweenDates(ledgerMaster, oldDate, newDate, tranxType, fiscalYear,
                                detail);
                    } else if (newDate.before(oldDate)) { // backward date
                        // old date record update
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = detailRepository.save(ledgerDetail);
                    }
                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    callToPostingLedger(tranxType, ledgerType, amount, fiscalYear, ledgerMaster, newDate,
                            tranxId, outlet, branch, tranxCode);
                }
            } else { // LEDGER CHANGE
                /***** NEW METHOD FOR LEDGER POSTING *****/
                callToPostingLedger(tranxType, ledgerType, amount, fiscalYear, ledgerMaster, dt, tranxId, outlet, branch,
                        tranxCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            postingLogger.info("Exception in callToPostingLedgerForUpdateByDetailsId::" + e.getMessage());
        }
        /**** NEW METHOD FOR LEDGER POSTING ****/
    }

    public void callToPostingLedgerForUpdate(Boolean isLedgerContains, Double amount, Long oldLedgerId, TransactionTypeMaster tranxType,
                                             String ledgerType, Long tranxId, LedgerMaster ledgerMaster, Date dt, FiscalYear fiscalYear,
                                             Outlet outlet, Branch branch, String tranxCode) {
        try {
            /**** NEW METHOD FOR LEDGER POSTING ****/
            LedgerOpeningClosingDetail ledgerDetail = detailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                    oldLedgerId, tranxType.getId(), tranxId, true);
            if (isLedgerContains) {
                if (ledgerDetail != null) {
                    if (ledgerDetail.getTranxDate().compareTo(dt) == 0 && ledgerDetail.getAmount() != amount) { // DATE SAME, AMOUNT DIFFERENT
                        Double closing = 0.0;
                        if (ledgerType.equalsIgnoreCase("cr"))
                            closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), amount, 0.0);
                        if (ledgerType.equalsIgnoreCase("dr"))
                            closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, amount);
                        ledgerDetail.setAmount(amount);
                        ledgerDetail.setClosingAmount(closing);
                        LedgerOpeningClosingDetail detail = detailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        updateLedgerPostings(ledgerMaster, dt, tranxType, fiscalYear, detail);
                    } else if (ledgerDetail.getTranxDate().compareTo(dt) != 0) { // DATE DIFFERENT
                        Date oldDate = ledgerDetail.getTranxDate();
                        Date newDate = dt;

                        if (newDate.after(oldDate)) { // forward date
                            // old date record update
                            Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                            ledgerDetail.setAmount(0.0);
                            ledgerDetail.setClosingAmount(closing);
                            ledgerDetail.setStatus(false);
                            LedgerOpeningClosingDetail detail = detailRepository.save(ledgerDetail);

                            /***** NEW METHOD FOR LEDGER POSTING *****/
                            updateLedgerPostingsBetweenDates(ledgerMaster, oldDate, newDate, tranxType, fiscalYear,
                                    detail);
                        } else if (newDate.before(oldDate)) { // backward date
                            // old date record update
                            ledgerDetail.setStatus(false);
                            LedgerOpeningClosingDetail detail = detailRepository.save(ledgerDetail);
                        }
                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        callToPostingLedger(tranxType, ledgerType, amount, fiscalYear, ledgerMaster, newDate,
                                tranxId, outlet, branch, tranxCode);
                    }
                }
            } else { // LEDGER CHANGE
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = detailRepository.save(ledgerDetail);

                    ledgerMaster = ledgerMasterRepository.findByIdAndStatus(oldLedgerId, true);
                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    updateLedgerPostings(ledgerMaster, dt, tranxType, fiscalYear, detail);
                }
                /***** NEW METHOD FOR LEDGER POSTING *****/
                callToPostingLedger(tranxType, ledgerType, amount, fiscalYear, ledgerMaster, dt, tranxId, outlet, branch,
                        tranxCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            postingLogger.info("Exception in callToPostingLedgerForUpdate::" + e.getMessage());
        }
        /**** NEW METHOD FOR LEDGER POSTING ****/
    }

    public void callToPostingLedger(TransactionTypeMaster tranxType, String ledgerType, Double amt, FiscalYear fiscalYear,
                                    LedgerMaster ledgerMaster, Date invoiceDate, Long tranxId, Outlet outlet,
                                    Branch branch, String tranxCode) {
        try {
            postingLogger.info("Entered in callToPostingLedger ");
            /***** NEW METHOD FOR LEDGER POSTING *****/
            Double opening = 0.0;
            Double orgOpening = 0.0;
            Double debit = 0.0;
            Double credit = 0.0;
            Double closing = 0.0;
            Double orgClosing = 0.0;

            LedgerOpeningClosingDetail openingClosingDetail = detailRepository.getLastRowByLedgerIdAndTranxDateAndTranxTypeIdAndStatus(
                    ledgerMaster.getId(), invoiceDate, true);
            if (openingClosingDetail == null) {  /*** record not present, take opening from ledger opening tbl ***/
                opening = ledgerMaster.getOpeningBal();
            } else {
                opening = openingClosingDetail.getClosingAmount();
            }
            orgOpening = opening;

            if (ledgerType.equalsIgnoreCase("cr"))
                closing = Constants.CAL_CR_CLOSING(opening, amt, debit);
            else closing = Constants.CAL_DR_CLOSING(opening, credit, amt);

            postingLogger.info("Before Save call opening ::" + opening);
            LedgerOpeningClosingDetail savedLedgerClosing = saveLedgerPosting(outlet, branch, fiscalYear, ledgerMaster, invoiceDate,
                    tranxId, tranxType.getId(), ledgerType, amt, opening, closing, true, tranxCode);

            opening = savedLedgerClosing != null ? savedLedgerClosing.getClosingAmount() : 0;
            orgClosing = savedLedgerClosing != null ? savedLedgerClosing.getClosingAmount() : 0;
            List<LedgerOpeningClosingDetail> openingClosingDetails = detailRepository.getNextRowsByLedgerIdAndTranxDateAndStatusForUpdate(
                    ledgerMaster.getId(), invoiceDate, savedLedgerClosing.getId(), true);
            postingLogger.info("openingClosingDetails size ::" + openingClosingDetails.size());
            postingLogger.info("invoiceDate ::" + invoiceDate);
            for (LedgerOpeningClosingDetail closingDetail : openingClosingDetails) {
                closingDetail.setOpeningAmount(opening);
                if (closingDetail.getTranxAction().equalsIgnoreCase("CR"))
                    closing = Constants.CAL_CR_CLOSING(opening, closingDetail.getAmount(), 0.0);
                else if (closingDetail.getTranxAction().equalsIgnoreCase("DR"))
                    closing = Constants.CAL_DR_CLOSING(opening, 0.0, closingDetail.getAmount());
                closingDetail.setClosingAmount(closing);
                detailRepository.save(closingDetail);
                opening = closing;
            }

            LedgerClosingDateSummary closingDateSummary = createOrUpdateDateWiseLedgerSummary(ledgerMaster, fiscalYear, invoiceDate, orgOpening, orgClosing);
            createOrUpdateLedgerBalanceSummary(ledgerMaster, fiscalYear, closing);

            updateLedgerNextDatesClosing(ledgerMaster, fiscalYear, invoiceDate);
            postingLogger.info("Exit from callToPostingLedger ");
        } catch (Exception e) {
            e.printStackTrace();
            postingLogger.info("Exception in callToPostingLedger::" + e.getMessage());
        }
        /***** NEW METHOD FOR LEDGER POSTING *****/
    }

    private void updateLedgerNextDatesClosing(LedgerMaster ledgerMaster, FiscalYear fiscalYear, Date invoiceDate) {
        try {
            LocalDate invDt = DateConvertUtil.convertDateToLocalDate(invoiceDate);
            List<LedgerClosingDateSummary> dateSummaryList = ledgerClosingDateSummaryRepository.findByLedgerMasterIdAndClosingDateAndStatus(
                    ledgerMaster.getId(), invDt, true);
            postingLogger.info("Start of updateLedgerNextDatesClosing update ");
            for (LedgerClosingDateSummary closingDetail : dateSummaryList) {
                Double closeAmt = detailRepository.getClosingAmountOfLedgerIdAndStatus(ledgerMaster.getId(), closingDetail.getClosingDate(), true);
                Double openAmt = detailRepository.getOpeningAmountOfLedgerIdAndStatus(ledgerMaster.getId(), closingDetail.getClosingDate(), true);
                closeAmt = closeAmt != null ? closeAmt : 0;
                openAmt = openAmt != null ? openAmt : 0;
                Double totalAmt = closeAmt - openAmt;
                closingDetail.setOpeningAmount(openAmt);
                closingDetail.setTotalAmount(totalAmt);
                closingDetail.setClosingAmount(closeAmt);
                try {
                    ledgerClosingDateSummaryRepository.save(closingDetail);

                    createOrUpdateLedgerBalanceSummary(ledgerMaster, fiscalYear, closeAmt);
                } catch (Exception e) {
                    e.printStackTrace();
                    postingLogger.error("Exception in updateLedgerPostings::LedgerOpeningClosingDetail save::" + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            postingLogger.info("Exception in updateLedgerNextDatesClosing::" + e.getMessage());
        }
    }

    private LedgerClosingDateSummary createOrUpdateDateWiseLedgerSummary(LedgerMaster ledgerMaster, FiscalYear fiscalYear,
                                                                         Date invoiceDate, Double orgOpening, Double orgClosing) {
        LedgerClosingDateSummary dtSummary = null;
        try {
            postingLogger.info("Start of LedgerClosingDateSummary ");
            LocalDate invDt = DateConvertUtil.convertDateToLocalDate(invoiceDate);
            LedgerClosingDateSummary dateSummary = ledgerClosingDateSummaryRepository.findByLedgerMasterIdAndFiscalYearIdAndClosingDateAndStatus(
                    ledgerMaster.getId(), fiscalYear.getId(), invDt, true);
            if (dateSummary == null) {
                Double totalAmt = orgClosing - orgOpening;

                dateSummary = new LedgerClosingDateSummary();
                dateSummary.setLedgerMaster(ledgerMaster);
                dateSummary.setBranch(ledgerMaster.getBranch());
                dateSummary.setOutlet(ledgerMaster.getOutlet());
                dateSummary.setFiscalYearId(fiscalYear.getId());
                dateSummary.setClosingDate(invDt);
                dateSummary.setOpeningAmount(orgOpening);
                dateSummary.setTotalAmount(totalAmt);
                dateSummary.setClosingAmount(orgClosing);
                dateSummary.setStatus(true);
                dtSummary = ledgerClosingDateSummaryRepository.save(dateSummary);
            } else {
                Double closeAmt = detailRepository.getClosingAmountOfLedgerIdAndStatus(ledgerMaster.getId(), invDt, true);
                Double openAmt = detailRepository.getOpeningAmountOfLedgerIdAndStatus(ledgerMaster.getId(), invDt, true);
                closeAmt = closeAmt != null ? closeAmt : 0;
                openAmt = openAmt != null ? openAmt : 0;
                Double totalAmt = closeAmt - openAmt;
                dateSummary.setOpeningAmount(openAmt);
                dateSummary.setTotalAmount(totalAmt);
                dateSummary.setClosingAmount(closeAmt);
                dtSummary = ledgerClosingDateSummaryRepository.save(dateSummary);
            }
            postingLogger.info("End of LedgerClosingDateSummary update ");
        } catch (Exception e) {
            e.printStackTrace();
            postingLogger.info("Exception in createOrUpdateDateWiseLedgerSummary::" + e.getMessage());
        }
        return dtSummary;
    }


    public void updateLedgerPostingsBetweenDates(LedgerMaster ledgerMaster, Date oldDate, Date newDate, TransactionTypeMaster tranxType,
                                                 FiscalYear fiscalYear, LedgerOpeningClosingDetail ledgerDetail) {
        try {
            postingLogger.info("Entered in updateLedgerPostings ");
            Double opening = ledgerDetail.getOpeningAmount();
            Double closing = 0.0;

            /*get list of between dates and manage opening and closing*/
            List<LedgerOpeningClosingDetail> openingClosingDetailList = detailRepository.getBetweenDatesByLedgerId(
                    ledgerMaster.getId(), oldDate, newDate, ledgerDetail.getId(), true);
            for (LedgerOpeningClosingDetail closingDetail : openingClosingDetailList) {
                closingDetail.setOpeningAmount(opening);
                if (closingDetail.getTranxAction().equalsIgnoreCase("CR"))
                    closing = Constants.CAL_CR_CLOSING(opening, closingDetail.getAmount(), 0.0);
                else if (closingDetail.getTranxAction().equalsIgnoreCase("DR"))
                    closing = Constants.CAL_DR_CLOSING(opening, 0.0, closingDetail.getAmount());
                closingDetail.setClosingAmount(closing);
                detailRepository.save(closingDetail);
                opening = closing;
            }
            postingLogger.info("End of updateLedgerPostings update ");
            LedgerClosingDateSummary closingDateSummary = createOrUpdateDateWiseLedgerSummary(ledgerMaster, fiscalYear, oldDate, opening, closing);
            createOrUpdateLedgerBalanceSummary(ledgerMaster, fiscalYear, closing);

            updateLedgerNextDatesClosing(ledgerMaster, fiscalYear, oldDate);
            postingLogger.info("Exit from updateLedgerPostings ");
        } catch (Exception e) {
            e.printStackTrace();
            postingLogger.info("Exception in updateLedgerPostingsBetweenDates::" + e.getMessage());
        }
    }

    public void updateLedgerPostings(LedgerMaster ledgerMaster, Date invoiceDate, TransactionTypeMaster tranxType,
                                     FiscalYear fiscalYear, LedgerOpeningClosingDetail openingClosingDetail) {
        try {
            postingLogger.info("Entered in updateLedgerPostings ");
            Double opening = openingClosingDetail.getClosingAmount();
            Double closing = 0.0;
            List<LedgerOpeningClosingDetail> openingClosingDetails = detailRepository.
                    getNextRowsByLedgerIdAndTranxDateAndStatusForUpdate(
                            ledgerMaster.getId(), invoiceDate, openingClosingDetail.getId(), true);
            postingLogger.info("Start of updateLedgerPostings update ");
            for (LedgerOpeningClosingDetail closingDetail : openingClosingDetails) {
                closingDetail.setOpeningAmount(opening);
                if (closingDetail.getTranxAction().equalsIgnoreCase("CR"))
                    closing = Constants.CAL_CR_CLOSING(opening, closingDetail.getAmount(), 0.0);
                else if (closingDetail.getTranxAction().equalsIgnoreCase("DR"))
                    closing = Constants.CAL_DR_CLOSING(opening, 0.0, closingDetail.getAmount());
                closingDetail.setClosingAmount(closing);
                try {
                    detailRepository.save(closingDetail);
                } catch (Exception e) {
                    e.printStackTrace();
                    postingLogger.error("Exception in updateLedgerPostings::LedgerOpeningClosingDetail save::" + e.getMessage());
                }
                opening = closing;
            }
            postingLogger.info("End of updateLedgerPostings update ");
            LedgerClosingDateSummary closingDateSummary = createOrUpdateDateWiseLedgerSummary(ledgerMaster, fiscalYear, invoiceDate, opening, closing);
            createOrUpdateLedgerBalanceSummary(ledgerMaster, fiscalYear, closing);

            updateLedgerNextDatesClosing(ledgerMaster, fiscalYear, invoiceDate);
            postingLogger.info("Exit from updateLedgerPostings ");
        } catch (Exception e) {
            e.printStackTrace();
            postingLogger.info("Exception in updateLedgerPostings::" + e.getMessage());
        }
    }


    public void createOrUpdateLedgerBalanceSummary(LedgerMaster ledgerMaster, FiscalYear fiscalYear, Double closing) {
        try {
            postingLogger.info("Start of createOrUpdateLedgerBalanceSummary ");
            LedgerBalanceSummary ledgerBalanceSummary = ledgerBalanceSummaryRepository.findByLedgerMasterIdAndFiscalYearIdAndStatus(
                    ledgerMaster.getId(), fiscalYear.getId(), true);
            if (ledgerBalanceSummary == null) {
                ledgerBalanceSummary = new LedgerBalanceSummary();
                ledgerBalanceSummary.setLedgerMaster(ledgerMaster);
                ledgerBalanceSummary.setFoundations(ledgerMaster.getFoundations());
                ledgerBalanceSummary.setPrinciples(ledgerMaster.getPrinciples());
                ledgerBalanceSummary.setPrincipleGroups(ledgerMaster.getPrincipleGroups());
                ledgerBalanceSummary.setAssociateGroups(ledgerMaster.getAssociateGroups());
                ledgerBalanceSummary.setBranch(ledgerMaster.getBranch());
                ledgerBalanceSummary.setOutlet(ledgerMaster.getOutlet());
                ledgerBalanceSummary.setFiscalYearId(fiscalYear.getId());
                ledgerBalanceSummary.setClosingBal(closing);
                ledgerBalanceSummary.setUnderPrefix(ledgerMaster.getUnderPrefix());
                ledgerBalanceSummary.setStatus(true);
                try {
                    ledgerBalanceSummaryRepository.save(ledgerBalanceSummary);
                } catch (Exception e) {
                    postingLogger.error("Exception in createOrUpdateLedgerBalanceSummary::LedgerBalanceSummary save::" + e.getMessage());
                }
            } else {
                ledgerBalanceSummary.setClosingBal(closing);
                try {
                    ledgerBalanceSummaryRepository.save(ledgerBalanceSummary);
                } catch (Exception e) {
                    postingLogger.error("Exception in createOrUpdateLedgerBalanceSummary::LedgerBalanceSummary update::" + e.getMessage());
                }
            }
            postingLogger.info("End of createOrUpdateLedgerBalanceSummary ");
        } catch (Exception e) {
            e.printStackTrace();
            postingLogger.info("Exception in createOrUpdateLedgerBalanceSummary::" + e.getMessage());
        }
    }

    public LedgerOpeningClosingDetail saveLedgerPosting(Outlet outlet, Branch branch, FiscalYear fiscalYear,
                                                        LedgerMaster ledgerMaster, Date tranxDate, Long tranxId,
                                                        Long tranxTypeId, String tranxAction, Double amount,
                                                        Double openingAmount, Double closingAmount, Boolean status,
                                                        String tranxCode) {
        LedgerOpeningClosingDetail savedLedgerClosing = null;
        try {
            postingLogger.info("Entered in saveLedgerPosting ");
            postingLogger.info("saveLedgerPosting openingAmount::" + openingAmount);
            try {
                LedgerOpeningClosingDetail detail = new LedgerOpeningClosingDetail();
                detail.setOutlet(outlet);
                detail.setBranch(branch);
                detail.setFiscalYearId(fiscalYear != null ? fiscalYear.getId() : null);
                detail.setLedgerMaster(ledgerMaster);
                detail.setTranxDate(tranxDate);
                detail.setTranxId(tranxId);
                detail.setTranxTypeId(tranxTypeId);
                detail.setTranxAction(tranxAction);
                detail.setAmount(amount);
                detail.setOpeningAmount(openingAmount);
                detail.setClosingAmount(closingAmount);
                detail.setTranxCode(tranxCode);
                detail.setStatus(status);

                savedLedgerClosing = detailRepository.save(detail);
            } catch (Exception e) {
                e.printStackTrace();
                postingLogger.error("Exception in saveLedgerPosting::LedgerOpeningClosingDetail save::", e.getMessage());
            }
            postingLogger.info("Exit from saveLedgerPosting ");
        } catch (Exception e) {
            e.printStackTrace();
            postingLogger.info("Exception in saveLedgerPosting::" + e.getMessage());
        }
        return savedLedgerClosing;
    }
}