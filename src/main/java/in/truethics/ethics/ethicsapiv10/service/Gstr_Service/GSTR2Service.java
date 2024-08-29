package in.truethics.ethics.ethicsapiv10.service.Gstr_Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.NumFormat;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerGstDetails;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerGstDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

@Service
public class GSTR2Service {
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    FiscalYearRepository fiscalYearRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private LedgerGstDetailsRepository gstDetailsRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private NumFormat numFormat;

    public JsonObject getGSTR2Data(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        String searchText = request.getParameter("searchText");

        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            endDatep = LocalDate.parse(endDate);
            startDate = request.getParameter("start_date");
            startDatep = LocalDate.parse(startDate);

        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");
//            return 0;
        }

//        >>>>>>>>>>>
        List sundryCreditorsData = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();


        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_transaction_postings_tbl.status=1 AND ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (!startDatep.equals("") && !endDatep.equals(""))
            query += " AND DATE(transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        if (!searchText.equalsIgnoreCase("")) {
            query = query + " AND (ledger_master_id LIKE '%" + searchText + "%' OR " +
                    "ledger_master_tbl.ledger_name LIKE '%" + searchText + "%')";
        }
        query = query + " GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (jsonObject!= null && !jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUCR");
        q.setParameter(2, "CR");
        q.setParameter(3, 1);
        sundryCreditorsData = q.getResultList();

        for (Object scId : sundryCreditorsData) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(scId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;
                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails != null && mGstDetails.size() > 0) {
                    count = ledgerTransactionPostingsRepository.findSdInvoiceCount("CR", 1L, mLedger.getId());
                    list = gstDetailsRepository.findtheTotal(mLedger);
                }
                JsonObject mObject = new JsonObject();
                if(!list.equals("") && list!=null && list.size()>0) {
                    String data[] = list.get(0).split(",");
                    mObject.addProperty("total_amt", numFormat.numFormat(Double.parseDouble(data[0])));
                    mObject.addProperty("taxable_amt", numFormat.numFormat(Double.parseDouble(data[1])));
                    mObject.addProperty("total_tax", numFormat.numFormat(Double.parseDouble(data[2])));
                    mObject.addProperty("total_igst", numFormat.numFormat(Double.parseDouble(data[3])));
                    mObject.addProperty("total_cgst", numFormat.numFormat(Double.parseDouble(data[4])));
                    mObject.addProperty("total_sgst", numFormat.numFormat(Double.parseDouble(data[5])));
                    mObject.addProperty("ledger_name", mLedger.getLedgerName());
                    mObject.addProperty("ledger_id", mLedger.getId());
                    mObject.addProperty("gst_number", mGstDetails.get(0).getGstin());
                    mObject.addProperty("total_invoices", count);
                    mArray.add(mObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }
    
    public JsonObject getGSTR2DRNOTEReg(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        String searchText = request.getParameter("searchText");
        Boolean flag = false;

        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;

        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            endDatep = LocalDate.parse(endDate);
            startDate = request.getParameter("start_date");
            startDatep = LocalDate.parse(startDate);

        } else {
            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");
//            return 0;
        }

        List sundryCreditorsData = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();


        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_transaction_postings_tbl.status=1 AND ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (!startDatep.equals("") && !endDatep.equals(""))
            query += " AND DATE(transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        if (!searchText.equalsIgnoreCase("")) {
            query = query + " AND (ledger_master_id LIKE '%" + searchText + "%' OR " +
                    "ledger_master_tbl.ledger_name LIKE '%" + searchText + "%')";
        }
        query = query + " GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (jsonObject !=null && !jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUCR");
        q.setParameter(2, "DR");
        q.setParameter(3, 2);
        sundryCreditorsData = q.getResultList();

        for (Object scId : sundryCreditorsData) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(scId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;
                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails != null && mGstDetails.size() > 0) {
                    count = ledgerTransactionPostingsRepository.findInvoiceCount("DR", 2L, mLedger.getId());
                    list = gstDetailsRepository.findTotal(mLedger);
                }
                JsonObject mObject = new JsonObject();
                if (!list.equals("") && list != null && list.size() > 0) {
                    String data[] = list.get(0).split(",");
                    System.out.println("data1" + data);
                    mObject.addProperty("total_amt", numFormat.numFormat(Double.parseDouble(data[0])));
                    mObject.addProperty("taxable_amt", numFormat.numFormat(Double.parseDouble(data[1])));
                    mObject.addProperty("total_tax", numFormat.numFormat(Double.parseDouble(data[2])));
                    mObject.addProperty("total_igst", numFormat.numFormat(Double.parseDouble(data[3])));
                    mObject.addProperty("total_cgst", numFormat.numFormat(Double.parseDouble(data[4])));
                    mObject.addProperty("total_sgst", numFormat.numFormat(Double.parseDouble(data[5])));
                    mObject.addProperty("ledger_name", mLedger.getLedgerName());
                    mObject.addProperty("ledger_id", mLedger.getId());
                    mObject.addProperty("gst_number", mGstDetails.get(0).getGstin());
                    mObject.addProperty("invoice_no", data[6]);
                    mObject.addProperty("transaction_date", data[7]);
                    mObject.addProperty("id", data[8]);

                    mObject.addProperty("voucher_type", "Purchases Return");
//                mObject.addProperty("tranx_type",data[8]);
                    mArray.add(mObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    public JsonObject getGSTR2DRNOTEUnreg(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        String searchText = request.getParameter("searchText");
//        String startDate = request.getParameter("startDate");
//        String endDate = request.getParameter("endDate");
//        LocalDate endDatep = null;
//        LocalDate startDatep = null;
        Boolean flag = false;

//        <<<<<<<<<<<<
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
//        Long productId = Long.valueOf(request.getParameter("productId"));
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        //****This Code For Users Dates Selection Between Start And End Date Manually****//
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            endDatep = LocalDate.parse(endDate);
            startDate = request.getParameter("start_date");
            startDatep = LocalDate.parse(startDate);

        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");
        }

        List sundryCreditorsData = new ArrayList<>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();


        String query = "SELECT ledger_transaction_postings_tbl.ledger_master_id " +
                "FROM ledger_transaction_postings_tbl " +
                "LEFT JOIN ledger_master_tbl ON ledger_transaction_postings_tbl.ledger_master_id = ledger_master_tbl.id " +
                "WHERE ledger_transaction_postings_tbl.status=1 AND ledger_master_tbl.unique_code=? " +
                "AND ledger_transaction_postings_tbl.ledger_type =? " +
                "AND ledger_transaction_postings_tbl.transaction_type_id =?";

        if (!startDatep.equals("") && !endDatep.equals(""))
            query += " AND DATE(transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        if (!searchText.equalsIgnoreCase("")) {
            query = query + " AND (ledger_master_id LIKE '%" + searchText + "%' OR " +
                    "ledger_master_tbl.ledger_name LIKE '%" + searchText + "%')";
        }
        query = query + " GROUP by ledger_master_id ";
        String jsonToStr = request.getParameter("sort");
        JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
        if (jsonObject != null && !jsonObject.get("colId").toString().equalsIgnoreCase("null") && jsonObject.get("colId").getAsString() != null) {
            String sortBy = jsonObject.get("colId").getAsString();
            query = query + " ORDER BY " + sortBy;
            if (jsonObject.get("isAsc").getAsBoolean() == true) {
                query = query + " ASC";
            } else {
                query = query + " DESC";
            }
        } else {
            query = query + " ORDER BY ledger_transaction_postings_tbl.id DESC";
        }

        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, "SUCR");
        q.setParameter(2, "DR");
        q.setParameter(3, 2);
        sundryCreditorsData = q.getResultList();

        for (Object scId : sundryCreditorsData) {
            try {
                LedgerMaster mLedger = ledgerMasterRepository.findByIdAndStatus(Long.parseLong(scId.toString()), true);
                List<LedgerGstDetails> mGstDetails = new ArrayList<>();
                List<String> list = new ArrayList<>();
                Long count = 0L;
                mGstDetails = gstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (mGstDetails.size() == 0) {
                    count = ledgerTransactionPostingsRepository.findInvoiceCount("DR", 2L, mLedger.getId());
                    list = gstDetailsRepository.findTotal(mLedger);
                }
                JsonObject mObject = new JsonObject();
                if (!list.equals("") && list != null && list.size() > 0) {
                    String data[] = list.get(0).split(",");
                    System.out.println("data1" + data);
                    mObject.addProperty("total_amt", numFormat.numFormat(Double.parseDouble(data[0])));
                    mObject.addProperty("taxable_amt", numFormat.numFormat(Double.parseDouble(data[1])));
                    mObject.addProperty("total_tax", numFormat.numFormat(Double.parseDouble(data[2])));
                    mObject.addProperty("total_igst", numFormat.numFormat(Double.parseDouble(data[3])));
                    mObject.addProperty("total_cgst", numFormat.numFormat(Double.parseDouble(data[4])));
                    mObject.addProperty("total_sgst", numFormat.numFormat(Double.parseDouble(data[5])));
                    mObject.addProperty("ledger_name", mLedger.getLedgerName());
                    mObject.addProperty("ledger_id", mLedger.getId());
                    mObject.addProperty("invoice_no", data[6]);
                    mObject.addProperty("transaction_date", data[7]);
                    mObject.addProperty("voucher_type", "Purchases Return");
                    mObject.addProperty("id", data[8]);
                    mArray.add(mObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

    //   GSTR2 Nil rated Api
    public JsonObject getGSTR2NIlRATEReg(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        System.out.println("users " + users.toString());
        JsonObject finalObject = new JsonObject();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;

        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            startDate = request.getParameter("start_date");
            endDate = request.getParameter("end_date");
            if(endDate!=null && !endDate.isEmpty() && startDate!=null && !startDate.isEmpty()){
                endDatep = LocalDate.parse(endDate);
                startDatep = LocalDate.parse(startDate);
            }
            else{
                FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
                if (fiscalYear != null) {
                    startDatep = fiscalYear.getDateStart();
                    endDatep = fiscalYear.getDateEnd();
                }
            }
        } else {

            FiscalYear fiscalYear = fiscalYearRepository.findTopByOrderByIdDesc();
            if (fiscalYear != null) {
                startDatep = fiscalYear.getDateStart();
                endDatep = fiscalYear.getDateEnd();
            }

        }
        currentStartDate = startDatep;
        currentEndDate = endDatep;
        if (startDatep !=null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");
        }
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        String query ="SELECT product_tbl.tax_type, lmt.ledger_name, tpit.total_base_amount, tpit.id, tpit.total_amount FROM `tranx_purchase_invoice_details_units_tbl`" +
                " AS tpidut LEFT JOIN product_tbl ON tpidut.product_id = product_tbl.id LEFT JOIN tranx_purchase_invoice_tbl" +
                " AS tpit ON tpidut.id = tpit.id LEFT JOIN ledger_master_tbl AS lmt ON tpit.sundry_creditors_id = lmt.id" +
                " WHERE tpidut.status=1 AND (product_tbl.tax_type = 'nilrated' OR product_tbl.tax_type = 'exempted') AND DATE(tpit.transaction_date) BETWEEN '" + startDatep +"' AND '" + endDatep + "' ";

        Query q = entityManager.createNativeQuery(query);
        System.out.println("q-===>"+query);
        List<Object[]> nilrateData = q.getResultList();
        System.out.println("HSN-2 data"+nilrateData.size());

        String query1 ="SELECT product_tbl.tax_type, lmt.ledger_name, tprit.total_base_amount, tprit.id, tprit.total_amount FROM `tranx_purchase_return_details_units_tbl`" +
                " AS tprdut LEFT JOIN product_tbl ON tprdut.product_id = product_tbl.id LEFT JOIN tranx_pur_return_invoice_tbl" +
                " AS tprit ON tprdut.id = tprit.id LEFT JOIN ledger_master_tbl AS lmt ON tprit.sundry_creditors_id = lmt.id" +
                " WHERE tprdut.status=1 AND product_tbl.tax_type = 'nilrated' OR product_tbl.tax_type = 'exempted' AND DATE(tprit.transaction_date) BETWEEN '" + startDatep +"' AND '" + endDatep + "' ";

        Query q1 = entityManager.createNativeQuery(query1);
        System.out.println("q-===>"+query1);
        List<Object[]> nilrateData1 = q1.getResultList();
        System.out.println("HSN-2 data"+nilrateData1.size());
        try {
            for (int j = 0; j < nilrateData.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + nilrateData.get(j));
                if (nilrateData != null) {
                    inspObj.addProperty("tax_type", nilrateData.get(j)[0].toString());
                    inspObj.addProperty("ledger_name", nilrateData.get(j)[1].toString());
                    inspObj.addProperty("total_amount", parseDouble(nilrateData.get(j)[2].toString()));

                    inspObj.addProperty("id", parseLong(nilrateData.get(j)[3].toString()));
                    inspObj.addProperty("voucher_type", "Purchase Invoice");
                }
                mArray.add(inspObj);
            }
            for (int j = 0; j < nilrateData1.size(); j++) {
                JsonObject inspObj = new JsonObject();
                System.out.println("inspObj " + nilrateData1.get(j));
                if (nilrateData != null) {
                    inspObj.addProperty("tax_type", nilrateData1.get(j)[0].toString());
                    inspObj.addProperty("ledger_name", nilrateData1.get(j)[1].toString());
                    inspObj.addProperty("total_amount", parseDouble(nilrateData1.get(j)[2].toString()));

                    inspObj.addProperty("id", parseLong(nilrateData1.get(j)[3].toString()));
                    inspObj.addProperty("voucher_type", "Purchase Return");
                }
                mArray.add(inspObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }
}
