package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.master.AreaHead;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.AreaHeadRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.ContentMasterRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.service.master_service.AreaHeadService;
import in.truethics.ethics.ethicsapiv10.service.master_service.ContentMasterService;
import org.apache.poi.hssf.record.StandardRecord;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Map;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import java.util.Map;

@RestController
public class AreaHeadController {
    @Autowired
    private AreaHeadService areaHeadService;
    @Autowired
    private AreaHeadRepository areaHeadRepository;
    @Autowired
    JwtTokenUtil jwtUtil;
    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping(path = "/create_area_head")
    public ResponseEntity<?> createAreaHead(MultipartHttpServletRequest request) throws ParseException {
        return ResponseEntity.ok(areaHeadService.createAreaHead(request));
    }

    @GetMapping(path = "/get_all_area_head")
    public Object getAllAreaHeads(HttpServletRequest request) {
        JsonObject result = areaHeadService.getAllAreaHeads(request);
        return result.toString();
    }

    @PostMapping(path = "/get_area_head_by_id")
    public Object getAreaHeadById(HttpServletRequest request) {
        JsonObject result = areaHeadService.getAreaHeadById(request);
        return result.toString();
    }

    @GetMapping(path = "/get_count_by_areahead")
    public Object getCountByAreahead(HttpServletRequest request) {
        JsonObject result = areaHeadService.getCountByAreahead(request);
        return result.toString();
    }

    @PostMapping(path = "/get_areahead_by_areacode")
    public Object getAreaHeadByCode(@RequestBody Map<String, String> request, HttpServletRequest req) {
        JsonObject result = areaHeadService.getAreaHeadByCode(request,req);
        return result.toString();
    }

    @PostMapping(path = "/get_areahead_dashboard")
    public Object getAreaHeadDashboard(@RequestBody Map<String, String> request, HttpServletRequest req) {
        JsonObject result = areaHeadService.getAreaHeadDashboard(request,req);
        return result.toString();
    }

    /*@PostMapping(path = "/get_latest_purchase")
    public Object getFrLatestPurchase(@RequestBody Map<String, String> request, HttpServletRequest req) {
        JsonObject result = areaHeadService.getFrLatestPurchase(request,req);
        return result.toString();
    }*/

    @PostMapping(path = "/get_areahead_franchise_dashboard")
    public Object getAreaHeadFranchiseDashboard(@RequestBody Map<String, String> request, HttpServletRequest req) {
        JsonObject result = areaHeadService.getAreaHeadFranchiseDashboard(request,req);
        return result.toString();
    }

    @PostMapping(path = "/get_parent_head")
    public Object getParentHead(@RequestBody Map<String, String> request, HttpServletRequest req) {
        JsonObject result = areaHeadService.getParentHead(request,req);
        return result.toString();
    }

    @PostMapping(path = "/get_franchise_performers")
    public Object getFranchisePerformers(@RequestBody Map<String, String> request, HttpServletRequest req) {
        JsonObject result = areaHeadService.getFranchisePerformers(request,req);
        return result.toString();
    }




    @PostMapping(path = "/update_area_head")
    public Object updateAreaHead(MultipartHttpServletRequest request) throws ParseException {
        JsonObject object = areaHeadService.updateAreaHead(request);
        return object.toString();
    }

    @PostMapping(path= "/delete_area_head")
    public Object areaHeadDelete(HttpServletRequest request){
        JsonObject object =areaHeadService.areaHeadDelete(request);
        return object.toString();
    }

    @PostMapping(path = "/get_parent_head_by_role")
    public Object getParentHeadByRole(HttpServletRequest request) {
        JsonObject result = areaHeadService.getParentHeadByRole(request);
        return result.toString();
    }

    @GetMapping(path = "/get_all_district_head")
    public Object getAllDistrictHeads(HttpServletRequest request) {
        JsonObject result = areaHeadService.getAllDistrictHeads(request);
        return result.toString();
    }

    @PostMapping(path = "/get_parent_head_by_dh")
    public Object getParentHeadByDh(HttpServletRequest request) {
        JsonObject result = areaHeadService.getParentHeadByDh(request);
        return result.toString();
    }
    @RequestMapping(value = "/mLogin", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticateToken(@RequestBody Map<String, String> request, HttpServletRequest req) throws Exception {
        ResponseMessage responseMessage = new ResponseMessage();
        String username = request.get("username");
        String password = request.get("password");
        String areaRole = request.get("area_role");
        try {
            AreaHead userDetails = areaHeadRepository.findByUsernameAndAreaRoleAndStatus(username, areaRole, true);
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                if (userDetails.getStatus()) {
                    Object jwtToken = jwtUtil.generateTokenForMobile(req, userDetails.getUsername().toString());
                    responseMessage.setMessage("Login success");
                    responseMessage.setResponse(jwtToken);
                    responseMessage.setResponseStatus(HttpStatus.OK.value());
                    System.out.println("login success");
                } else if (!userDetails.getStatus()) {
                    responseMessage.setMessage("Unauthorized access to system, please contact to admin");
                    responseMessage.setResponseStatus(UNAUTHORIZED.value());
                    System.out.println("login success");
                } else {
                    System.out.println("login fail");
                    responseMessage.setMessage("Incorrect username or password");
                    responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
                }
            }
        } catch (Exception e1) {
            System.out.println(e1.getMessage());
            System.out.println("login fail");
            responseMessage.setMessage("User not found");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.ok(responseMessage);
        }
        return ResponseEntity.ok(responseMessage);
    }

    @PostMapping(path = "/mobile/get_analytics")
    public Object getAnalytics(HttpServletRequest request) {
        JsonObject result = areaHeadService.getAnalytics(request);
        return result.toString();
    }


    @PostMapping(path = "/mobile/stateHeadDetail")
    public Object stateHeadDetail(@RequestBody Map<String, String> jsonReq, HttpServletRequest request){
        return areaHeadService.stateHeadDetail(jsonReq, request).toString();
    }
    @PostMapping(path = "/mobile/getZonalHeadsList")
    public Object getZonalHeadsList(@RequestBody Map<String, String> jsonReq, HttpServletRequest request){
        return areaHeadService.getZonalHeadsList(jsonReq, request).toString();
    }
    @PostMapping(path = "/mobile/getFranchiseList")
    public Object getFranchiseList(@RequestBody Map<String, String> jsonReq, HttpServletRequest request){
        return areaHeadService.getFranchiseList(jsonReq, request).toString();
    }
    @PostMapping(path = "/mobile/zonalHeadDetail")
    public Object zonalHeadDetail(@RequestBody Map<String, String> jsonReq, HttpServletRequest request){
        return areaHeadService.zonalHeadDetail(jsonReq, request).toString();
    }

    @PostMapping(path = "/mobile/getRegionHeadsList")
    public Object getRegionHeadsList(@RequestBody Map<String, String> jsonReq, HttpServletRequest request){
        return areaHeadService.getRegionHeadsList(jsonReq, request).toString();
    }
    @PostMapping(path = "/mobile/regionHeadDetail")
    public Object regionHeadDetail(@RequestBody Map<String, String> jsonReq, HttpServletRequest request){
        return areaHeadService.regionHeadDetail(jsonReq, request).toString();
    }
    @PostMapping(path = "/mobile/getDistrictHeadsList")
    public Object getDistrictHeadsList(@RequestBody Map<String, String> jsonReq, HttpServletRequest request){
        return areaHeadService.getDistrictHeadsList(jsonReq, request).toString();
    }
    @PostMapping(path = "/mobile/districtHeadDetail")
    public Object districtHeadDetail(@RequestBody Map<String, String> jsonReq, HttpServletRequest request){
        return areaHeadService.districtHeadDetail(jsonReq, request).toString();
    }
}
