package in.truethics.ethics.ethicsapiv10.controller.users;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.CommonAccessPermissions;
import in.truethics.ethics.ethicsapiv10.common.PasswordEncoders;
import in.truethics.ethics.ethicsapiv10.model.access_permissions.SystemAccessPermissions;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.access_permission_repository.SystemAccessPermissionsRepository;
import in.truethics.ethics.ethicsapiv10.repository.access_permission_repository.SystemActionMappingRepository;
import in.truethics.ethics.ethicsapiv10.repository.appconfig.AppConfigRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.service.user_service.UserService;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private SystemActionMappingRepository systemActionMappingRepository;
    @Autowired
    private SystemAccessPermissionsRepository systemAccessPermissionsRepository;
    @Autowired
    private AppConfigRepository appConfigRepository;
    @Autowired
    private CommonAccessPermissions accessPermissions;
    @Autowired
    JwtTokenUtil jwtUtil;
    @Autowired
    UserService userService;
    @Autowired
    private PasswordEncoders bcryptEncoder;
    private String SECRET_KEY = "SECRET_KEY";
    public static long ACCESS_VALIDITY = 24 * 60 * 60;
    public static long TOKEN_VALIDITY = 20 * 60 * 60;

    /* Registration of  SuperAdmin */
    @PostMapping(path = "/register_superAdmin")
    public ResponseEntity<?> createSuperAdmin(HttpServletRequest request) {
        return ResponseEntity.ok(userService.createSuperAdmin(request));
    }

    /* Registration of Users including Admin */
    @PostMapping(path = "/register_user")
    public Object createUser(HttpServletRequest request) {
        JsonObject jsonObject = userService.addUser(request);
        return jsonObject.toString();
    }

    /* company creation using ledger code */
    @PostMapping(path = "/company_create_by_ledger_code")
    public Object companyCreateByLedgerCode(HttpServletRequest request) {
        JsonObject jsonObject = userService.companyCreateByLedgerCode(request);
        return jsonObject.toString();
    }

    /*** get access permissions of User *****/
    @PostMapping(path = "/get_user_permissions")
    public Object getUserPermissions(HttpServletRequest request) {
        JsonObject jsonObject = userService.getUserPermissions(request);
        return jsonObject.toString();
    }

    //
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticateToken(@RequestBody Map<String, String> request,
                                                     HttpServletRequest req) {
        ResponseMessage responseMessage = new ResponseMessage();
        String username = request.get("usercode");
        String password = request.get("password");
        try {

            /*String requestDate = "2023-12-09";
            Date dt = new Date();
//            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
//            String strDt = sdf.format(dt);
//            strDt = strDt.substring(11);
            String strDt = requestDate +"T"+ LocalTime.now();
            try {
//                dt = sdf.parse(strDt);
                System.out.println("strDt >>>>>>>>>>>>>>"+strDt);
//                System.out.println("dt >>>>>>>>>>>>>>"+dt);
//                System.out.println("dt tostring >>>>>>>>>>>>>>"+dt);

                DateTimeFormatter formatter
                        = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss.SSSZ");
                *//*String timestampString = "2017-01-01 00:08:57.231";
                LocalDateTime dateTime = LocalDateTime.parse(timestampString, formatter);
                System.out.println(dateTime);*//*

                LocalDateTime dateTime1 = LocalDateTime.parse(strDt, formatter);
                System.out.println("updated datetime >>>>>>>>>>>>>>"+dateTime1);

            }catch(Exception e){
                e.printStackTrace();
            }*/

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authenticationToken);
            Users users = userService.findUserWithPassword(username, password);

            if (users.getStatus() == false) {
                responseMessage.setMessage("User deactivated, contact to admin.");
                responseMessage.setResponseStatus(HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.ok(responseMessage);
            }
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
            JWTCreator.Builder jwtBuilder = JWT.create();
            String access_token = "";
            jwtBuilder.withSubject(users.getUsercode());
            jwtBuilder.withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_VALIDITY * 1000));
            jwtBuilder.withIssuer(req.getRequestURI().toString());
            jwtBuilder.withClaim("userId", users.getId());
            jwtBuilder.withClaim("isSuperAdmin", users.getIsSuperAdmin());
            jwtBuilder.withClaim("userRole", users.getUserRole());
            jwtBuilder.withClaim("userCode", users.getUsercode());
            jwtBuilder.withClaim("fullName", users.getFullName());
            jwtBuilder.withClaim("companyCode", users.getCompanyCode() != null ? users.getCompanyCode() : "");
            if (users.getOutlet() != null) {
                jwtBuilder.withClaim("gstType", users.getOutlet().getGstTypeMaster().getGstType());
                jwtBuilder.withClaim("gstTypeId", users.getOutlet().getGstTypeMaster().getId());
                jwtBuilder.withClaim("companyType", users.getOutlet().getBusinessTrade());
            }
            if (users.getUserRole() != null && users.getUserRole().equalsIgnoreCase("BADMIN")) {
                jwtBuilder.withClaim("branchId", users.getBranch().getId());
                jwtBuilder.withClaim("branchName", users.getBranch().getBranchName());
                jwtBuilder.withClaim("companyId", users.getOutlet().getId());
                jwtBuilder.withClaim("outletId", users.getOutlet().getId());
                jwtBuilder.withClaim("CompanyName", users.getOutlet().getCompanyName());
                jwtBuilder.withClaim("state", users.getOutlet().getStateCode());
            } else if (users.getUserRole() != null && users.getUserRole().equalsIgnoreCase("CADMIN")) {
                jwtBuilder.withClaim("companyId", users.getOutlet().getId());
                jwtBuilder.withClaim("outletId", users.getOutlet().getId());
                jwtBuilder.withClaim("CompanyName", users.getOutlet().getCompanyName());
                jwtBuilder.withClaim("state", users.getOutlet().getStateCode());
                jwtBuilder.withClaim("isMultiBranch", users.getOutlet().getIsMultiBranch() != null ? users.getOutlet().getIsMultiBranch() : false);
            } else if (users.getUserRole() != null && users.getUserRole().equalsIgnoreCase("USER")) {
                jwtBuilder.withClaim("branchId", users.getBranch() != null ? users.getBranch().getId().toString() : "");
                jwtBuilder.withClaim("branchName", users.getBranch() != null ? users.getBranch().getBranchName() : "");
                jwtBuilder.withClaim("outletId", users.getOutlet().getId());
                jwtBuilder.withClaim("companyId", users.getOutlet().getId());
                jwtBuilder.withClaim("CompanyName", users.getOutlet().getCompanyName());
                jwtBuilder.withClaim("state", users.getOutlet().getStateCode());
            }
            jwtBuilder.withClaim("status", "OK");
            /* getting User Permissions */
            JsonObject finalResult = new JsonObject();
            JsonArray userPermissions = new JsonArray();
            JsonArray permissions = new JsonArray();
            JsonArray masterModules = new JsonArray();
            List<SystemAccessPermissions> list = systemAccessPermissionsRepository.findByUsersIdAndStatus(users.getId(), true);
            /*
             * Print elements using the forEach
             */

            for (SystemAccessPermissions mapping : list) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mapping.getId());
                mObject.addProperty("action_mapping_id", mapping.getSystemActionMapping().getId());
                mObject.addProperty("action_mapping_name", mapping.getSystemActionMapping().getSystemMasterModules().getName());
                mObject.addProperty("action_mapping_slug", mapping.getSystemActionMapping().getSystemMasterModules().getSlug());
                String[] actions = mapping.getUserActionsId().split(",");
                permissions = accessPermissions.getActions(actions);
                masterModules = accessPermissions.getParentMasters(mapping.getSystemActionMapping().getSystemMasterModules().getParentModuleId());
                mObject.add("actions", permissions);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", mapping.getSystemActionMapping().getSystemMasterModules().getId());
                jsonObject.addProperty("name", mapping.getSystemActionMapping().getSystemMasterModules().getName());
                jsonObject.addProperty("slug", mapping.getSystemActionMapping().getSystemMasterModules().getSlug());
                masterModules.add(jsonObject);
                mObject.add("parent_modules", masterModules);
                userPermissions.add(mObject);
            }
            finalResult.add("userActions", userPermissions);
            /* end of User Permissions */
            access_token = jwtBuilder.sign(algorithm);
            JWTCreator.Builder builder = JWT.create();
            String refresh_token = "";
            builder.withSubject(users.getUsercode());
            builder.withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_VALIDITY * 1000));
            builder.withIssuer(req.getRequestURI().toString());
            refresh_token = builder.sign(algorithm);
            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", access_token);
            tokens.put("refresh_token", refresh_token);
            responseMessage.setMessage("Login Successfully");
            responseMessage.setResponseObject(tokens);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            responseMessage.setData(finalResult.toString());
            responseMessage.setUserRole(users.getUserRole());
        } catch (BadCredentialsException be) {
            be.printStackTrace();
            System.out.println("Exception " + be.getMessage());
            responseMessage.setMessage("Incorrect Username or Password");
            responseMessage.setResponseStatus(HttpStatus.UNAUTHORIZED.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            responseMessage.setMessage("Incorrect Username or Password");
            responseMessage.setResponseStatus(HttpStatus.UNAUTHORIZED.value());
        }

        //   responseMessage.setData(jwtToken);
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping(path = "/getUsers")
    public Object getUsers() {
        JsonObject res = userService.getUsers();
        return res.toString();
    }

    /* get user by id for edit */
    @PostMapping(path = "/get_user_by_id")
    public Object getUsersById(HttpServletRequest requestParam) {
        JsonObject response = userService.getUsersById(requestParam.getParameter("id"));
        return response.toString();
    }

    /**** update Users ****/
    @PostMapping(path = "/updateUser")
    public ResponseEntity<?> updateUser(HttpServletRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @PostMapping(path = "/get_c_admin_users_old")
    public Object getUsersOfCompanyOld(HttpServletRequest httpServletRequest, @RequestBody Map<String, String> request) {
        JsonObject res = userService.getUsersOfCompany(httpServletRequest, request.get("userRole"), request.get("currentUserRole"));
        return res.toString();
    }

    /*****for Sadmin Login, sdamin can only view cadmins ****/
    @GetMapping(path = "/get_c_admins")
    public Object getCompanyAdmins(HttpServletRequest httpServletRequest) {
        JsonObject res = userService.getCompanyallAdmins(httpServletRequest);
        return res.toString();
    }

    @GetMapping(path = "/get_all_admins")
    public Object getCompanyallAdmins(HttpServletRequest httpServletRequest) {
        JsonObject res = userService.getCompanyallAdmins(httpServletRequest);
        return res.toString();
    }


    /*****for CAdmin Login, cadmin can only view Cusers ****/
    @GetMapping(path = "/get_c_users")
    public Object getUsersOfCompany(HttpServletRequest httpServletRequest) {
        JsonObject res = userService.getUsersOfCompanyNew(httpServletRequest);
        return res.toString();
    }

    /* call to this api if expired Token */
    @GetMapping(path = "/get_mesg_for_token_expired")
    public Object getMesgForTokenExpired(HttpServletRequest request) {
        JsonObject result = userService.getMesgForTokenExpired(request);
        return result.toString();
    }

    /* call to this api if expired Token */
    @PostMapping(path = "/set_user_perissions")
    public Object setUserPermissions(HttpServletRequest request) {
        JsonObject result = userService.setUserPermissions(request);
        return result.toString();
    }

    @GetMapping(path = "/get_b_admins")
    public Object getBranchAdmins(HttpServletRequest httpServletRequest) {
        JsonObject res = userService.getBranchAdmins(httpServletRequest);
        return res.toString();
    }

    /******get Branch User****************/
    @GetMapping(path = "/get_b_users")
    public Object getUsersOfBranchNew(HttpServletRequest httpServletRequest) {
        JsonObject res = userService.getUsersOfBranchNew(httpServletRequest);
        return res.toString();
    }

    /*Check invoice date is between fiscal years*/
    @PostMapping(path = "/checkInvoiceDateIsBetweenFY")
    public Object checkInvoiceDateIsBetweenFY(HttpServletRequest request) {
        return userService.checkInvoiceDateIsBetweenFY(request).toString();
    }

    /* validate of Users for duplication */
    @PostMapping(path = "/validate_user")
    public Object validateUser(HttpServletRequest request) {
        JsonObject jsonObject = userService.validateUser(request);
        return jsonObject.toString();
    }

    @PostMapping(path = "/delete_user")
    public Object userDelete(HttpServletRequest request) {
        JsonObject object = userService.userDelete(request);
        return object.toString();
    }

    /******* API : Mobile Owner App ******/
    @RequestMapping(value = "/mobile/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> ownerLogin(@RequestBody Map<String, String> request,
                                        HttpServletRequest req) {
        ResponseMessage responseMessage = new ResponseMessage();
        String username = request.get("username");
        String password = request.get("password");
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authenticationToken);
            Users users = userService.findUserWithPassword(username, password);
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
            JWTCreator.Builder jwtBuilder = JWT.create();
            String access_token = "";
            jwtBuilder.withSubject(users.getUsercode());
            jwtBuilder.withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_VALIDITY * 1000));
            jwtBuilder.withIssuer(req.getRequestURI().toString());
            jwtBuilder.withClaim("userId", users.getId());
            jwtBuilder.withClaim("isSuperAdmin", users.getIsSuperAdmin());
            jwtBuilder.withClaim("userRole", users.getUserRole());
            jwtBuilder.withClaim("userCode", users.getUsercode());
            jwtBuilder.withClaim("fullName", users.getFullName());
      /*      if (users.getOutlet() != null)
                jwtBuilder.withClaim("gstType", users.getOutlet().getGstTypeMaster().getGstType());
            if (users.getUserRole() != null && users.getUserRole().equalsIgnoreCase("BADMIN")) {
                jwtBuilder.withClaim("branchId", users.getBranch().getId());
                jwtBuilder.withClaim("branchName", users.getBranch().getBranchName());
                jwtBuilder.withClaim("companyId", users.getOutlet().getId());
                jwtBuilder.withClaim("CompanyName", users.getOutlet().getCompanyName());
                jwtBuilder.withClaim("state", users.getOutlet().getStateCode());
            } else if (users.getUserRole() != null && users.getUserRole().equalsIgnoreCase("CADMIN")) {
                jwtBuilder.withClaim("companyId", users.getOutlet().getId());
                jwtBuilder.withClaim("CompanyName", users.getOutlet().getCompanyName());
                jwtBuilder.withClaim("state", users.getOutlet().getStateCode());
                jwtBuilder.withClaim("isMultiBranch", users.getOutlet().getIsMultiBranch() != null ? users.getOutlet().getIsMultiBranch() : false);
            } else if (users.getUserRole() != null && users.getUserRole().equalsIgnoreCase("USER")) {
                jwtBuilder.withClaim("branchId", users.getBranch() != null ? users.getBranch().getId().toString() : "");
                jwtBuilder.withClaim("branchName", users.getBranch() != null ? users.getBranch().getBranchName() : "");
                jwtBuilder.withClaim("companyId", users.getOutlet().getId());
                jwtBuilder.withClaim("CompanyName", users.getOutlet().getCompanyName());
                jwtBuilder.withClaim("state", users.getOutlet().getStateCode());
            }*/

            jwtBuilder.withClaim("status", "OK");
            access_token = jwtBuilder.sign(algorithm);
            JWTCreator.Builder builder = JWT.create();
            String refresh_token = "";
            builder.withSubject(users.getUsercode());
            builder.withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_VALIDITY * 1000));
            builder.withIssuer(req.getRequestURI().toString());
            refresh_token = builder.sign(algorithm);
            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", access_token);
            tokens.put("refresh_token", refresh_token);
            responseMessage.setMessage("Login Successfully");
            responseMessage.setResponseObject(tokens);
            responseMessage.setResponseStatus(HttpStatus.OK.value());

        } catch (BadCredentialsException be) {
            be.printStackTrace();
            System.out.println("Exception " + be.getMessage());
            responseMessage.setMessage("Incorrect Username or Password");
            responseMessage.setResponseStatus(HttpStatus.UNAUTHORIZED.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            responseMessage.setMessage("Incorrect Username or Password");
            responseMessage.setResponseStatus(HttpStatus.UNAUTHORIZED.value());
        }
        return ResponseEntity.ok(responseMessage);
    }

    @PostMapping(path = "/mobile/forgetPassword")
    public Object forgetPasswordOwner(@RequestBody Map<String, String> request) {
        return userService.forgetPasswordOwner(request).toString();
    }

    @PostMapping(path = "/mobile/changePassword")
    public Object changePasswordWithMobile(@RequestBody Map<String, String> request) {
        return userService.changePassword(request.get("username"), request.get("password"));
    }

    /***** Reset Password : to change the passoword for security reasion  *****/
    @PostMapping(path = "/mobile/resetPassword")
    public Object resetPasswordWithMobile(@RequestBody Map<String, String> request, HttpServletRequest httpServletRequest) {
        return userService.resetPasswordWithMobile(request.get("password"), request.get("currentPassword"), httpServletRequest);
    }

    @PostMapping(path = "/disable_user")
    public Object disableUser(HttpServletRequest request) {
        JsonObject object = userService.disableUser(request);
        return object.toString();
    }

    /* validate of Users for duplication */
    @PostMapping(path = "/validate_user_update")
    public Object validateUserUpdate(HttpServletRequest request) {
        JsonObject jsonObject = userService.validateUserUpdate(request);
        return jsonObject.toString();
    }

    /* validate of Users OF Company Admin for duplication */
    @PostMapping(path = "/validate_cadmin_update")
    public Object validateCadminUpdate(HttpServletRequest request) {
        JsonObject jsonObject = userService.validateCadminUpdate(request);
        return jsonObject.toString();
    }


    @RequestMapping(value = "/getUserToken", method = RequestMethod.POST)
    public Object getUserToken(HttpServletRequest req) {
        ResponseMessage responseMessage = new ResponseMessage();
        String username = req.getParameter("usercode");
        String companyCode = req.getHeader("branch");

        String access_token = "";
        try {
            Users users = userRepository.findTop1ByUserRoleIgnoreCaseAndCompanyCode("cadmin", companyCode);
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
            JWTCreator.Builder jwtBuilder = JWT.create();
            jwtBuilder.withSubject(users.getUsercode());
            jwtBuilder.withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_VALIDITY * 1000));
            jwtBuilder.withIssuer(req.getRequestURI().toString());
            jwtBuilder.withClaim("userId", users.getId());
            jwtBuilder.withClaim("isSuperAdmin", users.getIsSuperAdmin());
            jwtBuilder.withClaim("userRole", users.getUserRole());
            jwtBuilder.withClaim("userCode", users.getUsercode());
            jwtBuilder.withClaim("fullName", users.getFullName());
            jwtBuilder.withClaim("companyCode", users.getCompanyCode() != null ? users.getCompanyCode() : "");
            if (users.getOutlet() != null) {
                jwtBuilder.withClaim("gstType", users.getOutlet().getGstTypeMaster().getGstType());
                jwtBuilder.withClaim("gstTypeId", users.getOutlet().getGstTypeMaster().getId());
                jwtBuilder.withClaim("companyType", users.getOutlet().getBusinessTrade());
            }
            if (users.getUserRole() != null && users.getUserRole().equalsIgnoreCase("BADMIN")) {
                jwtBuilder.withClaim("branchId", users.getBranch().getId());
                jwtBuilder.withClaim("branchName", users.getBranch().getBranchName());
                jwtBuilder.withClaim("companyId", users.getOutlet().getId());
                jwtBuilder.withClaim("outletId", users.getOutlet().getId());
                jwtBuilder.withClaim("CompanyName", users.getOutlet().getCompanyName());
                jwtBuilder.withClaim("state", users.getOutlet().getStateCode());
            } else if (users.getUserRole() != null && users.getUserRole().equalsIgnoreCase("CADMIN")) {
                jwtBuilder.withClaim("companyId", users.getOutlet().getId());
                jwtBuilder.withClaim("outletId", users.getOutlet().getId());
                jwtBuilder.withClaim("CompanyName", users.getOutlet().getCompanyName());
                jwtBuilder.withClaim("state", users.getOutlet().getStateCode());
                jwtBuilder.withClaim("isMultiBranch", users.getOutlet().getIsMultiBranch() != null ? users.getOutlet().getIsMultiBranch() : false);
            } else if (users.getUserRole() != null && users.getUserRole().equalsIgnoreCase("USER")) {
                jwtBuilder.withClaim("branchId", users.getBranch() != null ? users.getBranch().getId().toString() : "");
                jwtBuilder.withClaim("branchName", users.getBranch() != null ? users.getBranch().getBranchName() : "");
                jwtBuilder.withClaim("outletId", users.getOutlet().getId());
                jwtBuilder.withClaim("companyId", users.getOutlet().getId());
                jwtBuilder.withClaim("CompanyName", users.getOutlet().getCompanyName());
                jwtBuilder.withClaim("state", users.getOutlet().getStateCode());
            }
            jwtBuilder.withClaim("status", "OK");
            /* getting User Permissions */
            JsonObject finalResult = new JsonObject();
            JsonArray userPermissions = new JsonArray();
            JsonArray permissions = new JsonArray();
            JsonArray masterModules = new JsonArray();
            List<SystemAccessPermissions> list = systemAccessPermissionsRepository.findByUsersIdAndStatus(users.getId(), true);
            /*
             * Print elements using the forEach
             */
            for (SystemAccessPermissions mapping : list) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mapping.getId());
                mObject.addProperty("action_mapping_id", mapping.getSystemActionMapping().getId());
                mObject.addProperty("action_mapping_name", mapping.getSystemActionMapping().getSystemMasterModules().getName());
                mObject.addProperty("action_mapping_slug", mapping.getSystemActionMapping().getSystemMasterModules().getSlug());
                String[] actions = mapping.getUserActionsId().split(",");
                permissions = accessPermissions.getActions(actions);
                masterModules = accessPermissions.getParentMasters(mapping.getSystemActionMapping().getSystemMasterModules().getParentModuleId());
                mObject.add("actions", permissions);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", mapping.getSystemActionMapping().getSystemMasterModules().getId());
                jsonObject.addProperty("name", mapping.getSystemActionMapping().getSystemMasterModules().getName());
                jsonObject.addProperty("slug", mapping.getSystemActionMapping().getSystemMasterModules().getSlug());
                masterModules.add(jsonObject);
                mObject.add("parent_modules", masterModules);
                userPermissions.add(mObject);
            }
            finalResult.add("userActions", userPermissions);
            /* end of User Permissions */
            access_token = jwtBuilder.sign(algorithm);
            JWTCreator.Builder builder = JWT.create();
            builder.withSubject(users.getUsercode());
            builder.withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_VALIDITY * 1000));
            builder.withIssuer(req.getRequestURI().toString());

            responseMessage.setResponse(access_token);
            responseMessage.setResponseObject(users);
            return responseMessage;
        } catch (BadCredentialsException be) {
            be.printStackTrace();
            System.out.println("Exception " + be.getMessage());
            responseMessage.setMessage("Incorrect Username or Password");
            responseMessage.setResponseStatus(HttpStatus.UNAUTHORIZED.value());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            responseMessage.setMessage("Incorrect Username or Password");
            responseMessage.setResponseStatus(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }

    @PostMapping(path = "/bo/changePassword")
    public Object changePasswordForBo(HttpServletRequest request) {
        return userService.changePasswordForBo(request);
    }

    /* Get User Profile Details Mobile App */
    @GetMapping(path = "/mobile/login-details")
    public Object loginDetails(HttpServletRequest request) {
        JsonObject result = userService.loginDetails(request);
        return result.toString();
    }
}
