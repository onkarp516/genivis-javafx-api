package in.truethics.ethics.ethicsapiv10.service.tranx_service.contra;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.GenerateDates;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.GenericDTData;
import in.truethics.ethics.ethicsapiv10.common.LedgerCommonPostings;
import in.truethics.ethics.ethicsapiv10.dto.accountentrydto.ContraDTO;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerOpeningClosingDetail;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerOpeningClosingDetailRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.TransactionTypeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.DaybookRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.contra_repository.TranxContraDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.contra_repository.TranxContraMasterRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TranxContraNewService {

    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;

    @Autowired
    private TransactionTypeMasterRepository tranxRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TranxContraMasterRepository tranxContaMasterRepository;
    @Autowired
    private TranxContraDetailsRepository tranxContraDetailsRepository;

    @Autowired
    private DaybookRepository daybookRepository;
    @Autowired
    private LedgerCommonPostings ledgerCommonPostings;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private PostingUtility postingUtility;

    private static final Logger contraLogger = LogManager.getLogger(TranxContraNewService.class);
    @Autowired
    private LedgerOpeningClosingDetailRepository ledgerOpeningClosingDetailRepository;

    List<Long> ledgerList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB
    List<Long> ledgerInputList = new ArrayList<>(); // for saving all ledgers Id against receipt from DB



    public JsonObject contraLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
//        Long count = tranxContaMasterRepository.findLastRecord(users.getOutlet().getId());


        Long count = 0L;
        if (users.getBranch() != null) {
            count = tranxContaMasterRepository.findBranchLastRecord(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            count = tranxContaMasterRepository.findLastRecord(users.getOutlet().getId());
        }

        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String csCode = "CNTR" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("contraNo", csCode);
        return result;
    }


    public JsonObject createContra(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();

        TranxContraMaster contraMaster = new TranxContraMaster();
        Branch branch = null;
        if (users.getBranch() != null)
            branch = users.getBranch();
        Outlet outlet = users.getOutlet();
        contraMaster.setBranch(branch);
        contraMaster.setOutlet(outlet);
        contraMaster.setStatus(true);
        String invoiceDate = request.getParameter("transaction_dt");
        LocalDate tranxDate = LocalDate.parse(invoiceDate);
        Date dt = DateConvertUtil.convertStringToDate(invoiceDate);
        contraMaster.setTransactionDate(dt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            contraMaster.setFiscalYear(fiscalYear);
            contraMaster.setFinancialYear(fiscalYear.getFiscalYear());
        }

        contraMaster.setContraSrNo(Long.parseLong(request.getParameter("voucher_contra_sr_no")));
        contraMaster.setContraNo(request.getParameter("voucher_contra_no"));
        contraMaster.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
        if (paramMap.containsKey("narration"))
            contraMaster.setNarrations(request.getParameter("narration"));
        else {
            contraMaster.setNarrations("");
        }
        contraMaster.setCreatedBy(users.getId());
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CNTR");
        String tranxCode = TranxCodeUtility.generateTxnId(tranxType.getTransactionCode());
        contraMaster.setTranxCode(tranxCode);
        TranxContraMaster tranxContraMaster = tranxContaMasterRepository.save(contraMaster);

        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("rows");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                JsonObject contraRow = row.get(i).getAsJsonObject();
                TranxContraDetails tranxContraDetails = new TranxContraDetails();
                LedgerMaster ledgerMaster = null;

                tranxContraDetails.setBranch(branch);
                tranxContraDetails.setOutlet(outlet);
                tranxContraDetails.setStatus(true);
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(contraRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null)
                    tranxContraDetails.setLedgerMaster(ledgerMaster);
                tranxContraDetails.setTranxContraMaster(tranxContraMaster);
                tranxContraDetails.setType(contraRow.get("type").getAsString());
//                tranxContraDetails.setLedgerName(contraRow.get("perticulars").getAsJsonObject().get("ledger_name").getAsString());
                total_amt = contraRow.get("paid_amt").getAsDouble();
                if (contraRow.has("bank_payment_type"))
                    tranxContraDetails.setPayment_type(contraRow.get("bank_payment_type").getAsString());
                tranxContraDetails.setPaidAmount(total_amt);

                if (contraRow.has("bank_payment_no"))
                    tranxContraDetails.setBankPaymentNo(contraRow.get("bank_payment_no").getAsString());
                if (contraRow.has("bank_name"))
                    tranxContraDetails.setBankName(contraRow.get("bank_name").getAsString());
                if (contraRow.has("payment_date") &&
                        !contraRow.get("payment_date").getAsString().equalsIgnoreCase("")
                        && !contraRow.get("payment_date").getAsString().toLowerCase().contains("invalid"))
                    tranxContraDetails.setPaymentDate(contraRow.get("payment_date").getAsString());

                JsonObject perticulars = contraRow.get("perticulars").getAsJsonObject();

                //   ledgerMaster = ledgerMasterRepository.findByIdAndStatus(perticulars.get("id").getAsLong(), true);
//                if (perticulars.get("type").getAsString().equalsIgnoreCase("bank_account"))
//                    tranxContraDetails.setBankName(perticulars.get("type").getAsString());
//                else {
//                    tranxContraDetails.setBankName("Cash A/c");
//                }

                tranxContraDetails.setLedgerType(ledgerMaster.getSlugName());
                tranxContraDetails.setCreatedBy(users.getId());

                TranxContraDetails mContra = tranxContraDetailsRepository.save(tranxContraDetails);
                insertIntoPostings(mContra, total_amt, contraRow.get("type").getAsString(), "Insert");
                //Accounting Postings
            }
            response.addProperty("message", "Contra created successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            contraLogger.error("Error in createContra :->" + e.getMessage());
            response.addProperty("message", "Error in Contra creation");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    private void insertIntoPostings(TranxContraDetails mContra, double total_amt, String crdrType,
                                    String operation) {
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CNTR");
        try {
            /**** New Postings Logic *****/
            ledgerCommonPostings.callToPostings(total_amt, mContra.getLedgerMaster(), tranxType,
                    mContra.getLedgerMaster().getAssociateGroups(), mContra.getTranxContraMaster().getFiscalYear(),
                    mContra.getBranch(), mContra.getOutlet(), mContra.getTranxContraMaster().getTransactionDate(),
                    mContra.getTranxContraMaster().getId(), mContra.getTranxContraMaster().getContraNo(),
                    crdrType, true, "Contra", operation);


            if (operation.equalsIgnoreCase("insert")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                postingUtility.callToPostingLedger(tranxType, crdrType, total_amt, mContra.getTranxContraMaster().getFiscalYear(),
                        mContra.getLedgerMaster(), mContra.getTranxContraMaster().getTransactionDate(), mContra.getTranxContraMaster().getId(),
                        mContra.getOutlet(), mContra.getBranch(), mContra.getTranxContraMaster().getTranxCode());
            }
            if (operation.equalsIgnoreCase("delete")) {
                /**** NEW METHOD FOR LEDGER POSTING ****/
                LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                        mContra.getLedgerMaster().getId(), tranxType.getId(), mContra.getTranxContraMaster().getId(), true);
                if (ledgerDetail != null) {
                    Double closing = Constants.CAL_DR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                    ledgerDetail.setAmount(0.0);
                    ledgerDetail.setClosingAmount(closing);
                    ledgerDetail.setStatus(false);
                    LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                    /***** NEW METHOD FOR LEDGER POSTING *****/
                    postingUtility.updateLedgerPostings(mContra.getLedgerMaster(), mContra.getTranxContraMaster().getTransactionDate(),
                            tranxType, mContra.getTranxContraMaster().getFiscalYear(), detail);
                }
            }
            /**** Save into Day Book ****/
            if (mContra.getType().equalsIgnoreCase("dr")) {
                saveIntoDayBook(mContra);
            }

        } catch (Exception e) {
            e.printStackTrace();
            contraLogger.error("Error in insertIntoPostings :->" + e.getMessage());
        }
    }

    private void saveIntoDayBook(TranxContraDetails mContra) {
        DayBook dayBook = new DayBook();
        dayBook.setOutlet(mContra.getOutlet());
        if (mContra.getBranch() != null)
            dayBook.setBranch(mContra.getBranch());
        dayBook.setAmount(mContra.getPaidAmount());
        LocalDate trDate = DateConvertUtil.convertDateToLocalDate(mContra.getTranxContraMaster().getTransactionDate());
        dayBook.setTranxDate(trDate);
        dayBook.setParticulars(mContra.getLedgerMaster().getLedgerName());
        dayBook.setVoucherNo(mContra.getTranxContraMaster().getContraNo());
        dayBook.setVoucherType("Contra");
        dayBook.setStatus(true);
        daybookRepository.save(dayBook);
    }

    public JsonObject contraListbyOutlet(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        List<TranxContraMaster> contra = new ArrayList<>();
        if (users.getBranch() != null) {
            contra = tranxContaMasterRepository.
                    findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            contra = tranxContaMasterRepository.
                    findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
        }

        for (TranxContraMaster vouchers : contra) {
            JsonObject response = new JsonObject();
            response.addProperty("id", vouchers.getId());
            response.addProperty("contra_code", vouchers.getContraNo());
            response.addProperty("transaction_dt",DateConvertUtil.convertDateToLocalDate(
                    vouchers.getTransactionDate()).toString());
            response.addProperty("contra_sr_no", vouchers.getContraSrNo());
            response.addProperty("narration", vouchers.getNarrations());
            List<TranxContraDetails> tranxContraDetails = tranxContraDetailsRepository.findLedgerName(vouchers.getId(), users.getOutlet().getId(), true);
            response.addProperty("ledger_name", tranxContraDetails != null && tranxContraDetails.size() > 0 ? tranxContraDetails.get(0).getLedgerMaster().getLedgerName() : "");
            response.addProperty("total_amount", vouchers.getTotalAmt());
            result.add(response);
        }

        JsonObject output = new JsonObject();
        output.addProperty("message", "success");
        output.addProperty("responseStatus", HttpStatus.OK.value());
        output.add("data", result);
        return output;
    }

    //start of Contra list API with pagination
    public Object contraListbyOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
//        System.out.println("request "+request+  "  req="+req);
        Integer pageNo = Integer.parseInt(request.get("pageNo"));
        Integer pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
        String startDate = request.get("startDate");
        String endDate = request.get("endDate");

        LocalDate endDatep = null;
        LocalDate startDatep = null;

        System.out.println("startdate " + startDatep + "  endDate " + endDatep);
        List<TranxContraMaster> contra = new ArrayList<>();
        List<TranxContraMaster> contraArrayList = new ArrayList<>();
        List<ContraDTO> contraDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `tranx_contra_master_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND status=1";
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }

//            if(!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase(""))
//                query += " AND transaction_date BETWEEN '" + startDate +"' AND '" + endDate + "'";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND narration LIKE '%" + searchText + "%'";
            }
            String jsonToStr = request.get("sort");
            System.out.println(" sort " + jsonToStr);
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
            } else {
                query = query + " ORDER BY id DESC";
            }
            String query1 = query;       //we get all lists here
            System.out.println("query== " + query);

            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;

            Query q = entityManager.createNativeQuery(query, TranxContraMaster.class);
            contra = q.getResultList();
            Query q1 = entityManager.createNativeQuery(query1, TranxContraMaster.class);

            contraArrayList = q1.getResultList();
//            System.out.println("Limit total rows " + contraArrayList.size());
            Integer total_pages = (contraArrayList.size() / pageSize);
            if ((contraArrayList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }

            for (TranxContraMaster contraListView : contra) {
                contraDTOList.add(convertToDTDTO(contraListView));
            }

            GenericDatatable<ContraDTO> data = new GenericDatatable<>(contraDTOList, contraArrayList.size(),
                    pageNo, pageSize, total_pages);

            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDTData.setRows(contraDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    //end of contra list API with pagination
    //start of DTO for contra list
    private ContraDTO convertToDTDTO(TranxContraMaster tranxContraMaster) {
        ContraDTO contraDTO = new ContraDTO();

        contraDTO.setId(tranxContraMaster.getId());
        contraDTO.setContra_code(tranxContraMaster.getContraNo());
        contraDTO.setTransaction_dt(DateConvertUtil.convertDateToLocalDate(tranxContraMaster.getTransactionDate()).toString());
        contraDTO.setContra_sr_no(tranxContraMaster.getContraSrNo());
        contraDTO.setNarration(tranxContraMaster.getNarrations());
        List<TranxContraDetails> tranxContraDetails = tranxContraDetailsRepository.
                findLedgerName(tranxContraMaster.getId(), tranxContraMaster.getOutlet().getId(), true);
        contraDTO.setLedger_name(tranxContraDetails != null && tranxContraDetails.size() > 0 ?
                tranxContraDetails.get(0).getLedgerMaster().getLedgerName() : "");
        contraDTO.setTotal_amount(tranxContraMaster.getTotalAmt());


        return contraDTO;
    }
    //end of DTO for contra list

    public JsonObject updateContra(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject response = new JsonObject();
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CNTR");
        TranxContraMaster contraMaster = tranxContaMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("contra_id")), true);

        ledgerList = ledgerOpeningClosingDetailRepository.getLedgersByTranxIdAndTranxTypeIdAndStatus(
                contraMaster.getId(), tranxType.getId(), true);
        Branch branch = null;
        if (users.getBranch() != null)
            branch = users.getBranch();
        Outlet outlet = users.getOutlet();
        contraMaster.setBranch(branch);
        contraMaster.setOutlet(outlet);
        //   contraMaster.setStatus(true);
        LocalDate tranxDate = LocalDate.parse(request.getParameter("transaction_dt"));
        String invoiceDate = request.getParameter("transaction_dt");
        Date dt = DateConvertUtil.convertStringToDate(invoiceDate);

        if (tranxDate.isEqual(DateConvertUtil.convertDateToLocalDate(contraMaster.getTransactionDate()))) {
            dt = contraMaster.getTransactionDate();
        }
        contraMaster.setTransactionDate(dt);
        /* fiscal year mapping */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(tranxDate);
        if (fiscalYear != null) {
            contraMaster.setFiscalYear(fiscalYear);
            contraMaster.setFinancialYear(fiscalYear.getFiscalYear());
        }

        contraMaster.setContraSrNo(Long.parseLong(request.getParameter("voucher_contra_sr_no")));
        contraMaster.setContraNo(request.getParameter("voucher_contra_no"));
        contraMaster.setTotalAmt(Double.parseDouble(request.getParameter("total_amt")));
        if (paramMap.containsKey("narration"))
            contraMaster.setNarrations(request.getParameter("narration"));
        contraMaster.setCreatedBy(users.getId());
        //contraMaster.setUpdatedBy(users.getId());
        TranxContraMaster tranxContraMaster = tranxContaMasterRepository.save(contraMaster);

        try {
            double total_amt = 0.0;
            String jsonStr = request.getParameter("rows");
            JsonParser parser = new JsonParser();
            JsonArray row = parser.parse(jsonStr).getAsJsonArray();
            for (int i = 0; i < row.size(); i++) {
                JsonObject contraRow = row.get(i).getAsJsonObject();
                TranxContraDetails tranxContraDetails = null;
                Long detailsId = 0L;
                if (contraRow.has("details_id"))
                    detailsId = contraRow.get("details_id").getAsLong();
                if (detailsId != 0) {
                    tranxContraDetails = tranxContraDetailsRepository.findByIdAndStatus(detailsId, true);
                } else {
                    tranxContraDetails = new TranxContraDetails();
                    tranxContraDetails.setStatus(true);
                }
//                TranxContraDetails tranxContraDetails = new TranxContraDetails();
                LedgerMaster ledgerMaster = null;
                tranxContraDetails.setBranch(branch);
                tranxContraDetails.setOutlet(outlet);
//               // tranxContraDetails.setStatus(true);
                ledgerMaster = ledgerMasterRepository.findByIdAndStatus(
                        contraRow.get("perticulars").getAsJsonObject().get("id").getAsLong(), true);
                if (ledgerMaster != null)
                    tranxContraDetails.setLedgerMaster(ledgerMaster);
                tranxContraDetails.setTranxContraMaster(tranxContraMaster);
                tranxContraDetails.setType(contraRow.get("type").getAsString());
//                tranxContraDetails.setLedgerName(contraRow.get("perticulars").getAsJsonObject().get("ledger_name").getAsString());
                total_amt = contraRow.get("paid_amt").getAsDouble();
                if (contraRow.has("bank_payment_type"))
                    tranxContraDetails.setPayment_type(contraRow.get("bank_payment_type").getAsString());
                tranxContraDetails.setPaidAmount(total_amt);

                if (contraRow.has("bank_payment_no"))
                    tranxContraDetails.setBankPaymentNo(contraRow.get("bank_payment_no").getAsString());
                if (contraRow.has("bank_name"))
                    tranxContraDetails.setBankName(contraRow.get("bank_name").getAsString());
                if (contraRow.has("payment_date") &&
                        !contraRow.get("payment_date").getAsString().equalsIgnoreCase("")
                        && !contraRow.get("payment_date").getAsString().toLowerCase().contains("invalid"))
                    tranxContraDetails.setPaymentDate(contraRow.get("payment_date").getAsString());
                JsonObject perticulars = contraRow.get("perticulars").getAsJsonObject();
                tranxContraDetails.setLedgerType(ledgerMaster.getSlugName());
                tranxContraDetails.setCreatedBy(users.getId());

                TranxContraDetails mContra = tranxContraDetailsRepository.save(tranxContraDetails);
                updateIntoPostings(mContra, total_amt, detailsId);
            }

            /* Remove all ledgers from DB if we found new input ledger id's while updating */
            for (Long mDblist : ledgerList) {
                if (!ledgerInputList.contains(mDblist)) {
                    contraLogger.info("removing unused previous ledger ::" + mDblist);
                    LedgerOpeningClosingDetail ledgerDetail = ledgerOpeningClosingDetailRepository.findByLedgerMasterIdAndTranxTypeIdAndTranxIdAndStatus(
                            mDblist, tranxType.getId(), contraMaster.getId(), true);
                    if (ledgerDetail != null) {
                        Double closing = Constants.CAL_CR_CLOSING(ledgerDetail.getOpeningAmount(), 0.0, 0.0);
                        ledgerDetail.setAmount(0.0);
                        ledgerDetail.setClosingAmount(closing);
                        ledgerDetail.setStatus(false);
                        LedgerOpeningClosingDetail detail = ledgerOpeningClosingDetailRepository.save(ledgerDetail);

                        /***** NEW METHOD FOR LEDGER POSTING *****/
                        postingUtility.updateLedgerPostings(ledgerDetail.getLedgerMaster(), contraMaster.getTransactionDate(),
                                tranxType, contraMaster.getFiscalYear(), detail);
                    }
                    contraLogger.info("removing unused previous ledger update done");
                }
            }
            response.addProperty("message", "Contra updated successfully");
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            contraLogger.error("Error in update Contra :->" + e.getMessage());
            response.addProperty("message", "Error in Contra creation");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }


    private void updateIntoPostings(TranxContraDetails mContra, double total_amt, Long detailsId) {
        try {
            Boolean isLedgerContains = false;
            String tranxAction = "DR";
            TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CNTR");
            if (mContra.getType().equalsIgnoreCase("dr")) {
                if (detailsId != 0) {
                    isLedgerContains = ledgerList.contains(mContra.getLedgerMaster().getId());
                    ledgerInputList.add(mContra.getLedgerMaster().getId());
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                            findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mContra.getLedgerMaster().getId(),
                                    tranxType.getId(), mContra.getTranxContraMaster().getId());
                    if (mLedger != null) {
                        mLedger.setAmount(total_amt);
                        mLedger.setTransactionDate(mContra.getTranxContraMaster().getTransactionDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    ledgerCommonPostings.callToPostings(total_amt, mContra.getLedgerMaster(), tranxType,
                            mContra.getLedgerMaster().getAssociateGroups(), mContra.getTranxContraMaster().getFiscalYear(), mContra.getBranch(),
                            mContra.getOutlet(), mContra.getTranxContraMaster().getTransactionDate(),
                            mContra.getTranxContraMaster().getId(), mContra.getTranxContraMaster().getContraNo(),
                            "DR", true, "Contra", "Insert");
                }

            } else {
                tranxAction = "CR";
                if (detailsId != 0) {
                    isLedgerContains = ledgerList.contains(mContra.getLedgerMaster().getId());
                    ledgerInputList.add(mContra.getLedgerMaster().getId());
                    LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.
                            findByLedgerMasterIdAndTransactionTypeIdAndTransactionId(mContra.getLedgerMaster().getId(),
                                    tranxType.getId(), mContra.getTranxContraMaster().getId());
                    if (mLedger != null) {
                        mLedger.setAmount(total_amt);
                        mLedger.setTransactionDate(mContra.getTranxContraMaster().getTransactionDate());
                        mLedger.setOperations("updated");
                        ledgerTransactionPostingsRepository.save(mLedger);
                    }
                } else {
                    ledgerCommonPostings.callToPostings(total_amt, mContra.getLedgerMaster(), tranxType,
                            mContra.getLedgerMaster().getAssociateGroups(), mContra.getTranxContraMaster().getFiscalYear(), mContra.getBranch(),
                            mContra.getOutlet(), mContra.getTranxContraMaster().getTransactionDate(),
                            mContra.getTranxContraMaster().getId(), mContra.getTranxContraMaster().getContraNo(),
                            "CR", true, "Contra", "Insert");
                }
            }

            Double amount = total_amt;
            /**** NEW METHOD FOR LEDGER POSTING ****/
            postingUtility.callToPostingLedgerForUpdate(isLedgerContains, amount, mContra.getLedgerMaster().getId(),
                    tranxType, tranxAction, mContra.getTranxContraMaster().getId(), mContra.getLedgerMaster(),
                    mContra.getTranxContraMaster().getTransactionDate(), mContra.getTranxContraMaster().getFiscalYear(),
                    mContra.getOutlet(), mContra.getBranch(), mContra.getTranxContraMaster().getTranxCode());
        } catch (Exception e) {
            e.printStackTrace();
            contraLogger.error("Error in updateIntoPostings :->" + e.getMessage());
        }
    }

    /*get contra by id*/
    public JsonObject getContraById(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<TranxContraDetails> list = new ArrayList<>();

        JsonObject finalResult = new JsonObject();
        try {
            Long contraId = Long.parseLong(request.getParameter("contra_id"));
            TranxContraMaster contraMaster = tranxContaMasterRepository.findByIdAndOutletIdAndStatus(contraId, users.getOutlet().getId(), true);

            list = tranxContraDetailsRepository.findByTranxContraMasterIdAndStatus(contraMaster.getId(), true);
            finalResult.addProperty("contra_no", contraMaster.getContraNo());
            finalResult.addProperty("contra_sr_no", contraMaster.getContraSrNo());
            finalResult.addProperty("tranx_date", DateConvertUtil.convertDateToLocalDate(contraMaster.getTransactionDate()).toString());
            finalResult.addProperty("total_amt", contraMaster.getTotalAmt());
            finalResult.addProperty("narrations", contraMaster.getNarrations());

            JsonArray row = new JsonArray();
            if (list.size() > 0) {
                for (TranxContraDetails mdetails : list) {
                    JsonObject rpdetails = new JsonObject();
                    rpdetails.addProperty("details_id", mdetails.getId());
                    rpdetails.addProperty("type", mdetails.getType());
                    rpdetails.addProperty("ledger_type", mdetails.getLedgerType());
                    rpdetails.addProperty("ledger_name", mdetails.getLedgerName());
                    rpdetails.addProperty("paid_amt", mdetails.getPaidAmount());
//                    rpdetails.addProperty("dr",mdetails.getDr());
//                    rpdetails.addProperty("cr",mdetails.getCr());
                    rpdetails.addProperty("payment_type", mdetails.getPayment_type());
                    rpdetails.addProperty("payment_no", mdetails.getBankPaymentNo());
                    rpdetails.addProperty("bankName", mdetails.getBankName());
                    rpdetails.addProperty("payment_date",
                            mdetails.getPaymentDate() != null ? mdetails.getPaymentDate().toString() : "");
                    rpdetails.addProperty("ledger_id", mdetails.getLedgerMaster().getId());
                    row.add(rpdetails);
                }
            }

            finalResult.addProperty("message", "success");
            finalResult.addProperty("responseStatus", HttpStatus.OK.value());
            finalResult.add("contra_details", row);

        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            contraLogger.error("Error in getContraById" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } catch (Exception e1) {
            e1.printStackTrace();
            contraLogger.error("Error in getContraById" + e1.getMessage());
            System.out.println(e1.getMessage());
            e1.printStackTrace();
            finalResult.addProperty("message", "error");
            finalResult.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return finalResult;
    }

    public JsonObject deleteContra(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        TranxContraMaster contraTranx = tranxContaMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        TransactionTypeMaster tranxType = tranxRepository.findByTransactionCodeIgnoreCase("CNTR");
        try {
            contraTranx.setStatus(false);
            tranxContaMasterRepository.save(contraTranx);
            if (contraTranx != null) {
                List<TranxContraDetails> tranxContraDetails = tranxContraDetailsRepository.
                        findByTranxContraMasterIdAndStatus(contraTranx.getId(), true);
                for (TranxContraDetails mDetail : tranxContraDetails) {
                    if (mDetail.getType().equalsIgnoreCase("CR"))
                        insertIntoPostings(mDetail, mDetail.getPaidAmount(), "DR", "Delete");// Accounting Postings
                    else
                        insertIntoPostings(mDetail, mDetail.getPaidAmount(), "CR", "Delete");// Accounting Postings
                }
                /**** make status=0 to all ledgers of respective Journal voucher id, due to this we wont get
                 details of deleted invoice when we want get details of respective ledger ****/
                List<LedgerTransactionPostings> mInoiceLedgers = new ArrayList<>();
                mInoiceLedgers = ledgerTransactionPostingsRepository.findByTransactionTypeIdAndTransactionIdAndStatus(tranxType.getId(), contraTranx.getId(), true);
                for (LedgerTransactionPostings mPostings : mInoiceLedgers) {
                    try {
                        mPostings.setStatus(false);
                        ledgerTransactionPostingsRepository.save(mPostings);
                    } catch (Exception e) {
                        contraLogger.error("Exception in Delete functionality for all ledgers of" + " deleted purchase invoice->" + e.getMessage());
                    }
                }
                jsonObject.addProperty("message", "Contra invoice deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "error in sales quotation deletion");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            contraLogger.error("Error in Contra invoice Delete()->" + e.getMessage());
        }
        return jsonObject;
    }

}
