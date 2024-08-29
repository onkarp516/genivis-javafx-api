package in.truethics.ethics.ethicsapiv10.service.Gstr_Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

@Service
public class GSTR3Service {

    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    FiscalYearRepository fiscalYearRepository;
    @Autowired
    EntityManager entityManager;

    // GSTR3B - Outward Taxable Supplies
    public JsonObject getGSTR3BOutwardTaxSuplierData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
//        String searchText = request.getParameter("searchText");
//        String startDate = request.getParameter("startDate");
//        String endDate = request.getParameter("endDate");
//        LocalDate endDatep = null;
//        LocalDate startDatep = null;
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
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
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

        if ( startDatep != null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }

//        List sundryDebtorsData = new ArrayList<String>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        // sales invoice
        String query = "SELECT DATE(tsit.bill_date), tsit.sales_invoice_no, lmt.ledger_name,lgdt.gstin,tsit.total_base_amount, " +
                "tsit.totaligst, tsit.totalcgst, tsit.totalsgst, tsit.total_tax, tsit.total_amount, tsit.id from tranx_sales_invoice_tbl" +
                " AS tsit LEFT JOIN ledger_master_tbl AS lmt ON tsit.sundry_debtors_id =lmt.id LEFT JOIN ledger_gst_details_tbl " +
                "AS lgdt ON lmt.id = lgdt.ledger_id WHERE tsit.status=1 AND DATE(tsit.bill_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        Query q = entityManager.createNativeQuery(query);

        List<Object[]> sundryDebtorsData = q.getResultList();

        for (int j = 0; j < sundryDebtorsData.size(); j++) {
            JsonObject inspObj = new JsonObject();
//            System.out.println("----->>>."+sundryDebtorsData.get(j)[4]);
            inspObj.addProperty("bill_date", sundryDebtorsData.get(j)[0].toString());
            inspObj.addProperty("sales_invoice", sundryDebtorsData.get(j)[1].toString());
            inspObj.addProperty("ledger_name", sundryDebtorsData.get(j)[2].toString());
            inspObj.addProperty("gstin", sundryDebtorsData.get(j)[3] !=null ?  sundryDebtorsData.get(j)[3].toString(): "");
            inspObj.addProperty("taxable_amt",  sundryDebtorsData.get(j)[4] !=null ? parseDouble(sundryDebtorsData.get(j)[4].toString()) :  0.0);
            inspObj.addProperty("totaligst", sundryDebtorsData.get(j)[5] != null ? parseDouble(sundryDebtorsData.get(j)[5].toString()) :  0.0);
            inspObj.addProperty("totalcgst", sundryDebtorsData.get(j)[6] != null ? parseDouble(sundryDebtorsData.get(j)[6].toString()) :  0.0);
            inspObj.addProperty("totalsgst", sundryDebtorsData.get(j)[7] != null ? parseDouble(sundryDebtorsData.get(j)[7].toString()) :  0.0);
            inspObj.addProperty("total_tax", sundryDebtorsData.get(j)[8] != null ? parseDouble(sundryDebtorsData.get(j)[8].toString()) :  0.0);
            inspObj.addProperty("total_amount", sundryDebtorsData.get(j)[9] != null ? parseDouble(sundryDebtorsData.get(j)[9].toString()) :  0.0);
            inspObj.addProperty("id", sundryDebtorsData.get(j)[10] != null ? parseLong(sundryDebtorsData.get(j)[10].toString()) :  0);
            inspObj.addProperty("voucher_type", "Sales Invoice");
            mArray.add(inspObj);
        }

        // sales return
        String queryy = "SELECT DATE(tsrit.transaction_date), tsrit.sales_return_no, lmt.ledger_name,lgdt.gstin,tsrit.total_base_amount," +
                " tsrit.totaligst, tsrit.totalcgst, tsrit.totalsgst, tsrit.total_tax, tsrit.total_amount, tsrit.id from tranx_sales_return_invoice_tbl " +
                " AS tsrit LEFT JOIN ledger_master_tbl AS lmt ON tsrit.sundry_debtors_id =lmt.id LEFT JOIN ledger_gst_details_tbl" +
                " AS lgdt ON lmt.id = lgdt.ledger_id WHERE tsrit.status=1 AND DATE(tsrit.transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        Query qq = entityManager.createNativeQuery(queryy);

        List<Object[]> sundryDebtorsDataR = qq.getResultList();

        for (int j = 0; j < sundryDebtorsDataR.size(); j++) {
            JsonObject inspObj = new JsonObject();
//            System.out.println("----->>>."+sundryDebtorsData.get(j)[4]);
            inspObj.addProperty("bill_date", sundryDebtorsDataR.get(j)[0].toString());
            inspObj.addProperty("sales_invoice", sundryDebtorsDataR.get(j)[1].toString());
            inspObj.addProperty("ledger_name", sundryDebtorsDataR.get(j)[2].toString());
            inspObj.addProperty("gstin", sundryDebtorsDataR.get(j)[3] !=null ?  sundryDebtorsDataR.get(j)[3].toString(): "");
            inspObj.addProperty("taxable_amt",  sundryDebtorsDataR.get(j)[4] !=null ? parseDouble(sundryDebtorsDataR.get(j)[4].toString()) :  0.0);
            inspObj.addProperty("totaligst", sundryDebtorsDataR.get(j)[5] != null ? parseDouble(sundryDebtorsDataR.get(j)[5].toString()) :  0.0);
            inspObj.addProperty("totalcgst", sundryDebtorsDataR.get(j)[6] != null ? parseDouble(sundryDebtorsDataR.get(j)[6].toString()) :  0.0);
            inspObj.addProperty("totalsgst", sundryDebtorsDataR.get(j)[7] != null ? parseDouble(sundryDebtorsDataR.get(j)[7].toString()) :  0.0);
            inspObj.addProperty("total_tax", sundryDebtorsDataR.get(j)[8] != null ? parseDouble(sundryDebtorsDataR.get(j)[8].toString()) :  0.0);
            inspObj.addProperty("total_amount", sundryDebtorsDataR.get(j)[9] != null ? parseDouble(sundryDebtorsDataR.get(j)[9].toString()) :  0.0);
            inspObj.addProperty("id", sundryDebtorsData.get(j)[10] != null ? parseLong(sundryDebtorsData.get(j)[10].toString()) :  0);
            inspObj.addProperty("voucher_type", "Sales Return");
            mArray.add(inspObj);
        }

        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }


    // GSTR3B - All Other ITC
    public JsonObject getGSTR3BAllOtherITCData(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject finalObject = new JsonObject();
        Map<String, String[]> paramMap = request.getParameterMap();
        String endDate = null;
        LocalDate endDatep = null;
        String startDate = null;
        LocalDate startDatep = null;
        LocalDate currentStartDate = null;
        LocalDate currentEndDate = null;
        if (paramMap.containsKey("end_date") && paramMap.containsKey("start_date")) {
            endDate = request.getParameter("end_date");
            startDate = request.getParameter("start_date");
            if (endDate != null && !endDate.isEmpty() &&
                    startDate != null && !startDate.isEmpty()) {
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

        if ( startDatep != null && startDatep.isAfter(endDatep)) {
            System.out.println("Start Date Should not be After");

        }

//        List sundryDebtorsData = new ArrayList<String>();
        List<LedgerTransactionPostings> sundryDebtors = new ArrayList<>();
        JsonArray mArray = new JsonArray();

        // purchase invoice
        String query = "SELECT DATE(tpit.transaction_date), tpit.vendor_invoice_no, lmt.ledger_name, lgdt.gstin, tpit.total_base_amount, " +
                "tpit.totaligst, tpit.totalcgst, tpit.totalsgst, tpit.total_tax, tpit.total_amount, tpit.id " +
                "FROM genivis_pharma_db.tranx_purchase_invoice_tbl AS tpit LEFT JOIN ledger_master_tbl AS lmt ON " +
                "tpit.sundry_creditors_id = lmt.id LEFT JOIN ledger_gst_details_tbl AS lgdt ON lmt.id = lgdt.ledger_id" +
                " WHERE tpit.status=1 AND" +
                " DATE(tpit.transaction_date) BETWEEN '" + startDatep + "' AND '" + endDatep + "'";

        Query q = entityManager.createNativeQuery(query);

        List<Object[]> sundryDebtorsData = q.getResultList();

        for (int j = 0; j < sundryDebtorsData.size(); j++) {
            JsonObject inspObj = new JsonObject();
//            System.out.println("----->>>."+sundryDebtorsData.get(j)[4]);
            inspObj.addProperty("bill_date", sundryDebtorsData.get(j)[0].toString());
            inspObj.addProperty("purchase_invoice", sundryDebtorsData.get(j)[1].toString());
            inspObj.addProperty("ledger_name", sundryDebtorsData.get(j)[2].toString());
            inspObj.addProperty("gstin", sundryDebtorsData.get(j)[3] !=null ?  sundryDebtorsData.get(j)[3].toString(): "");
            inspObj.addProperty("taxable_amt",  sundryDebtorsData.get(j)[4] !=null ? parseDouble(sundryDebtorsData.get(j)[4].toString()) :  0.0);
            inspObj.addProperty("totaligst", sundryDebtorsData.get(j)[5] != null ? parseDouble(sundryDebtorsData.get(j)[5].toString()) :  0.0);
            inspObj.addProperty("totalcgst", sundryDebtorsData.get(j)[6] != null ? parseDouble(sundryDebtorsData.get(j)[6].toString()) :  0.0);
            inspObj.addProperty("totalsgst", sundryDebtorsData.get(j)[7] != null ? parseDouble(sundryDebtorsData.get(j)[7].toString()) :  0.0);
            inspObj.addProperty("total_tax", sundryDebtorsData.get(j)[8] != null ? parseDouble(sundryDebtorsData.get(j)[8].toString()) :  0.0);
            inspObj.addProperty("total_amount", sundryDebtorsData.get(j)[9] != null ? parseDouble(sundryDebtorsData.get(j)[9].toString()) :  0.0);
            inspObj.addProperty("id", sundryDebtorsData.get(j)[10] != null ? parseLong(sundryDebtorsData.get(j)[10].toString()) :  0);
            inspObj.addProperty("voucher_type", "Purchase Invoice");
            mArray.add(inspObj);
        }

        finalObject.addProperty("start_date", String.valueOf(currentStartDate));
        finalObject.addProperty("end_date", String.valueOf(currentEndDate));
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        finalObject.add("data", mArray);

        return finalObject;

    }

}
