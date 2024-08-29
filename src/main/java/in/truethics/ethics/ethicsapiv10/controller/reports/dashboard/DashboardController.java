package in.truethics.ethics.ethicsapiv10.controller.reports.dashboard;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.service.reports_service.DayBookService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.dashboard.DashboardService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    JwtTokenUtil jwtRequestFilter;

    /* get All Ledger Transactions*/
    @PostMapping(path = "/mobile/get_dashboard_data")
    public Object getDashboardData(@RequestBody Map<String, String> request) {
        JsonObject result = dashboardService.getDashboardData(request);
        return result.toString();
    }

    @PostMapping(path = "/mobile/get_sales_data")
    public Object getSalesData(@RequestBody Map<String, String> request) {
        JsonObject result = dashboardService.getSalesData(request);
        return result.toString();
    }

    @PostMapping(path = "/get_dashboard_data1")
    public Object getDashboardData1(HttpServletRequest request) {
//        try {
//            System.out.println("request.getHeader(\"Authorization\")" + request.getHeader("Authorization"));
//            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//            System.out.println("users =-> " + users);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        JsonObject result = dashboardService.getDashboardData1(request);
        return result.toString();
    }


}
