package in.truethics.ethics.ethicsapiv10.service.user_service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.CommonAccessPermissions;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.PasswordEncoders;
import in.truethics.ethics.ethicsapiv10.model.access_permissions.SystemAccessPermissions;
import in.truethics.ethics.ethicsapiv10.model.access_permissions.SystemActionMapping;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.UserRole;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.access_permission_repository.SystemAccessPermissionsRepository;
import in.truethics.ethics.ethicsapiv10.repository.access_permission_repository.SystemActionMappingRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.BranchRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.GstTypeMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.OutletRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.StateRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UserRoleRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static javax.crypto.Cipher.SECRET_KEY;

@Service
@Slf4j
@Transactional
//@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Autowired
    private PasswordEncoders bcryptEncoder;
    @Autowired
    private SystemActionMappingRepository mappingRepository;
    @Autowired
    private SystemAccessPermissionsRepository accessPermissionsRepository;
    @Autowired
    private CommonAccessPermissions accessPermissions;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private SystemAccessPermissionsRepository systemAccessPermissionsRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Autowired
    private LedgerGstDetailsRepository ledgerGstDetailsRepository;
    @Autowired
    private LedgerShippingDetailsRepository ledgerShippingDetailsRepository;
    @Autowired
    private LedgerDeptDetailsRepository ledgerDeptDetailsRepository;
    @Autowired
    private LedgerBillingDetailsRepository ledgerBillingDetailsRepository;
    @Autowired
    private GstTypeMasterRepository gstMasterRepository;
    @Autowired
    private LedgerBankDetailsRepository ledgerbankDetailsRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private LedgerLicenseDetailsRepository ledgerLicenseDetailsRepository;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private LedgerOpeningBalanceRepository ledgerOpeningBalanceRepository;
    private static final Logger UserLogger = LogManager.getLogger(UserService.class);

    public ResponseMessage createSuperAdmin(HttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = new Users();
        Map<String, String[]> paramMap = request.getParameterMap();
        try {
            if (paramMap.containsKey("mobileNumber"))
                users.setMobileNumber(Long.valueOf(request.getParameter("mobileNumber")));


            users.setFullName(request.getParameter("fullName"));
            if (paramMap.containsKey("email")) users.setEmail(request.getParameter("email"));
            else users.setEmail("");
            users.setGender(request.getParameter("gender"));
            users.setUsercode(request.getParameter("usercode"));
            users.setUsername(request.getParameter("usercode"));
            users.setUserRole(request.getParameter("userRole"));
            if (paramMap.containsKey("address")) users.setAddress(request.getParameter("address"));
            else users.setAddress("");
            users.setStatus(true);
            users.setPassword(bcryptEncoder.passwordEncoderNew().encode(request.getParameter("password")));
            users.setPlain_password(request.getParameter("password"));
            users.setIsSuperAdmin(true);
            users.setPermissions("all");

            userRepository.save(users);
            responseObject.setMessage("Super admin created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            UserLogger.error("Exception in createSuperAdmin:" + e.getMessage());
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("Internal Server Error");
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return responseObject;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = userRepository.findByUsername(username);
        if (users == null) {
            log.error("User not found In the database");
            throw new UsernameNotFoundException("UserController not found with username: " + username);

        } else {
            log.info("User found In the database: {}", username);
            return new org.springframework.security.core.userdetails.User(users.getUsercode(), users.getPassword(), new ArrayList<>());
        }
    }

    public Users findUser(String usercode) throws UsernameNotFoundException {
        Users users = userRepository.findByUsername(usercode);
        if (users != null) {

        } else {
            throw new UsernameNotFoundException("User not found with username: " + usercode);
        }
        return users;

    }

    public Users findUserWithPassword(String usercode, String password) throws UsernameNotFoundException {
        Users users = userRepository.findByUsername(usercode);
        if (bcryptEncoder.passwordEncoderNew().matches(password, users.getPassword())) {
            return users;
        }
        return null;
    }

    public JsonObject addUser(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        //  ResponseMessage responseObject = new ResponseMessage();
        JsonObject responseObject = new JsonObject();
        Users users = new Users();
        Users user = null;
        try {
            if (paramMap.containsKey("mobileNumber")&& !request.getParameter("mobileNumber").isEmpty()) {
                users.setMobileNumber(Long.valueOf(request.getParameter("mobileNumber")));

            }
            if (paramMap.containsKey("fullName")) {
                users.setFullName(request.getParameter("fullName"));
            }
            if (paramMap.containsKey("email")) {
                users.setEmail(request.getParameter("email"));
            } else {
                users.setEmail("");
            }

            if (paramMap.containsKey("address")) {
                users.setAddress(request.getParameter("address"));
            } else {
                users.setAddress("");
            }
            if (paramMap.containsKey("gender")) {
                users.setGender(request.getParameter("gender"));
            } else {
                users.setGender("");
            }


            if (!request.getParameter("userRole").equals("BADMIN") && !request.getParameter("userRole").equals("CADMIN")) {
                UserRole userRole = userRoleRepository.findByIdAndStatus(Long.parseLong(request.getParameter("roleId")), true);
                if (userRole != null) {
                    users.setRoleMaster(userRole);
                }
            }
            users.setUsercode(request.getParameter("usercode"));
            users.setUsername(request.getParameter("usercode"));
            users.setUserRole(request.getParameter("userRole"));
            users.setStatus(true);
            users.setIsSuperAdmin(false);
            //  users.setPermissions(request.getParameter("permissions"));
            if (request.getHeader("Authorization") != null) {
                user = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
                users.setCreatedBy(user.getId());
                users.setCompanyCode(user.getCompanyCode());
            }
            users.setPassword(bcryptEncoder.passwordEncoderNew().encode(request.getParameter("password")));
            users.setPlain_password(request.getParameter("password"));
            if (paramMap.containsKey("companyId")) {
                Outlet mOutlet = outletRepository.findByIdAndStatus(Long.parseLong(request.getParameter("companyId")), true);
                users.setOutlet(mOutlet);
            }
            if (paramMap.containsKey("branchId")) {
                Branch mBranch = branchRepository.findByIdAndStatus(Long.parseLong(request.getParameter("branchId")), true);
                users.setBranch(mBranch);
            }
            if (paramMap.containsKey("permissions")) users.setPermissions(request.getParameter("permissions"));
            Users newUser = userRepository.save(users);
            try {
                if (request.getParameter("userRole").equalsIgnoreCase("USER")) {
                    /* Create Permissions */
                    String jsonStr = request.getParameter("user_permissions");
                    if (jsonStr != null) {
                        JsonArray userPermissions = new JsonParser().parse(jsonStr).getAsJsonArray();
                        for (int i = 0; i < userPermissions.size(); i++) {
                            JsonObject mObject = userPermissions.get(i).getAsJsonObject();
                            SystemAccessPermissions mPermissions = new SystemAccessPermissions();
                            mPermissions.setUsers(newUser);
                            SystemActionMapping mappings = mappingRepository.findByIdAndStatus(mObject.get("mapping_id").getAsLong(), true);
                            mPermissions.setSystemActionMapping(mappings);
                            mPermissions.setStatus(true);
                            mPermissions.setCreatedBy(user.getId());
                            JsonArray mActionsArray = mObject.get("actions").getAsJsonArray();
                            String actionsId = "";
                            for (int j = 0; j < mActionsArray.size(); j++) {
                                actionsId = actionsId + mActionsArray.get(j).getAsString();
                                if (j < mActionsArray.size() - 1) {
                                    actionsId = actionsId + ",";
                                }
                            }
                            mPermissions.setUserActionsId(actionsId);
                            accessPermissionsRepository.save(mPermissions);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                UserLogger.error("Exception in addUser: " + e.getMessage());
                System.out.println(e.getMessage());
            }
            responseObject.addProperty("message", "User ID Created successfully");
            responseObject.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (DataIntegrityViolationException e1) {
            e1.printStackTrace();
            UserLogger.error("Exception in addUser: " + e1.getMessage());
            System.out.println("DataIntegrityViolationException " + e1.getMessage());
            responseObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            responseObject.addProperty("message", "Usercode already used");
            return responseObject;
        } catch (Exception e) {
            e.printStackTrace();
            UserLogger.error("Exception in addUser: " + e.getMessage());
            responseObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.addProperty("message", "Internal Server Error");
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return responseObject;
    }

    //List of Users Service
    public JsonObject getUsers() {
        JsonObject res = new JsonObject();
        List<Users> list = userRepository.findAll();
        res = getUserData(list);
        return res;
    }

    //Get user By Id
    public JsonObject getUsersById(String id) {
        Users user = userRepository.findByIdAndStatus(Long.parseLong(id), true);
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        JsonArray user_permission = new JsonArray();
        if (user != null) {
            response.addProperty("id", user.getId());
            if (user.getOutlet() != null) {
                response.addProperty("companyName", user.getOutlet().getCompanyName());
                response.addProperty("companyId", user.getOutlet().getId());
            }
            if (user.getBranch() != null) {
                response.addProperty("branchName", user.getBranch().getBranchName());
                response.addProperty("branchId", user.getBranch().getId());
            }
            response.addProperty("roleId", user.getRoleMaster() != null ? user.getRoleMaster().getId().toString() : "");
            response.addProperty("userRole", user.getUserRole());
            response.addProperty("password", user.getPlain_password());
            response.addProperty("fullName", user.getFullName() != null ? user.getFullName().toString() : "");
            response.addProperty("mobileNumber", user.getMobileNumber() != null ? user.getMobileNumber().toString() : "");
            response.addProperty("email", user.getEmail() != null ? user.getEmail().toString() : "");
            response.addProperty("gender", user.getGender() != null ? user.getGender().toString() : "");
            response.addProperty("usercode", user.getUsercode());
            /***** get User Permissions from access_permissions_tbl ****/
            List<SystemAccessPermissions> accessPermissions = new ArrayList<>();
            accessPermissions = systemAccessPermissionsRepository.findByUsersIdAndStatus(user.getId(), true);
            for (SystemAccessPermissions mPermissions : accessPermissions) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("mapping_id", mPermissions.getSystemActionMapping().getId());
                mObject.addProperty("name",mPermissions.getSystemActionMapping().getName());
                JsonArray actions = new JsonArray();
                String actionsId = mPermissions.getUserActionsId();
                String[] actionsList = actionsId.split(",");
                Arrays.sort(actionsList);
                for (String actionId : actionsList) {
                    actions.add(actionId);
                }
                mObject.add("actions", actions);
                user_permission.add(mObject);
            }
            response.add("permissions", user_permission);
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());

            result.add("responseObject", response);
        } else {
            result.addProperty("message", "error");
            result.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
        }
        return result;
    }

    public Object updateUser(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        ResponseMessage responseObject = new ResponseMessage();
        Users users = userRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        Users loginUser = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        if (users != null) {
            if (paramMap.containsKey("mobileNumber")) {
                users.setMobileNumber(Long.valueOf(request.getParameter("mobileNumber")));
            }
            if (paramMap.containsKey("fullName")) {
                users.setFullName(request.getParameter("fullName"));
            } else {
                users.setEmail("");
            }
            if (paramMap.containsKey("email")) {
                users.setEmail(request.getParameter("email"));
            } else {
                users.setEmail("");
            }
            if (paramMap.containsKey("address")) {
                users.setAddress(request.getParameter("address"));
            } else {
                users.setAddress("");
            }
            if (paramMap.containsKey("gender")) {
                users.setGender(request.getParameter("gender"));
            } else {
                users.setGender("");
            }
            users.setUsercode(request.getParameter("usercode"));
            users.setUsername(request.getParameter("usercode"));
            if (!request.getParameter("userRole").equals("BADMIN") && !request.getParameter("userRole").equals("CADMIN")) {
                UserRole userRole = userRoleRepository.findByIdAndStatus(Long.parseLong(request.getParameter("roleId")), true);
                if (userRole != null) {
                    users.setRoleMaster(userRole);
                }
            }
            users.setUserRole(request.getParameter("userRole"));
            users.setStatus(true);
            users.setIsSuperAdmin(false);
            users.setPermissions(request.getParameter("permissions"));
            users.setPassword(bcryptEncoder.passwordEncoderNew().encode(request.getParameter("password")));
            users.setPlain_password(request.getParameter("password"));
            if (paramMap.containsKey("companyId")) {
                Outlet mOutlet = outletRepository.findByIdAndStatus(Long.parseLong(request.getParameter("companyId")), true);
                users.setOutlet(mOutlet);
            }
            if (paramMap.containsKey("branchId")) {
                Branch mBranch = branchRepository.findByIdAndStatus(Long.parseLong(request.getParameter("branchId")), true);
                users.setBranch(mBranch);
            }
            String del_user_perm = request.getParameter("del_user_permissions");
            if (del_user_perm != null) {
                JsonArray deleteUserPermission = new JsonParser().parse(del_user_perm).getAsJsonArray();
                for (int j = 0; j < deleteUserPermission.size(); j++) {
                    Long moduleId = deleteUserPermission.get(j).getAsLong();
                    //  SystemActionMapping delMapping = mappingRepository.findByIdAndStatus(moduleId, true);
                    SystemAccessPermissions delPermissions = accessPermissionsRepository.findByUsersIdAndStatusAndSystemActionMappingId(users.getId(), true, moduleId);
                    delPermissions.setStatus(false);
                    try {
                        accessPermissionsRepository.save(delPermissions);
                    } catch (Exception e) {
                    }
                }
            }
            /* Update Permissions */
            String jsonStr = request.getParameter("user_permissions");
            if (jsonStr != null) {
                JsonArray userPermissions = new JsonParser().parse(jsonStr).getAsJsonArray();
                for (int i = 0; i < userPermissions.size(); i++) {
                    JsonObject mObject = userPermissions.get(i).getAsJsonObject();
                    SystemActionMapping mappings = mappingRepository.findByIdAndStatus(mObject.get("mapping_id").getAsLong(), true);
                    SystemAccessPermissions mPermissions = accessPermissionsRepository.findByUsersIdAndStatusAndSystemActionMappingId(users.getId(), true, mappings.getId());
                    System.out.println("User Id:" + users.getId());
                    if (mPermissions != null) {
                        JsonArray mActionsArray = mObject.get("actions").getAsJsonArray();
                        String actionsId = "";
                        for (int j = 0; j < mActionsArray.size(); j++) {
                            actionsId = actionsId + mActionsArray.get(j).getAsString();
                            if (j < mActionsArray.size() - 1) {
                                actionsId = actionsId + ",";
                            }
                        }
                        mPermissions.setUserActionsId(actionsId);
                        mPermissions.setUsers(users);
                        accessPermissionsRepository.save(mPermissions);
                    } else {
                        /* Create Permissions */
                        SystemAccessPermissions mPermissions1 = new SystemAccessPermissions();
                        mPermissions1.setSystemActionMapping(mappings);
                        mPermissions1.setStatus(true);
                        // mPermissions1.setCreatedBy(users.getId());
                        mPermissions1.setCreatedBy(loginUser.getId());
                        JsonArray mActionsArray = mObject.get("actions").getAsJsonArray();
                        String actionsId = "";
                        for (int j = 0; j < mActionsArray.size(); j++) {
                            actionsId = actionsId + mActionsArray.get(j).getAsString();
                            if (j < mActionsArray.size() - 1) {
                                actionsId = actionsId + ",";
                            }
                        }
                        mPermissions1.setUserActionsId(actionsId);
                        mPermissions1.setUsers(users);

                        try {
                            accessPermissionsRepository.save(mPermissions1);
                        } catch (Exception e) {
                            System.out.println("Exception:" + e.getMessage());
                        }
                    }
                }
            }
            userRepository.save(users);
            responseObject.setMessage("User ID updated successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            /*else {
                responseObject.setResponseStatus(HttpStatus.FORBIDDEN.value());
                responseObject.setMessage("Not found");
            }*/
        } else {
            responseObject.setResponseStatus(HttpStatus.FORBIDDEN.value());
            responseObject.setMessage("Not found");
        }
        return responseObject;
    }

    /* Get all Branch Users of  Institute Admin */
    /*public JsonObject getBranchUsers(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Long branchId = users.getBranch().getId();
        List<Users> list = new ArrayList<>();
        JsonObject res = new JsonObject();
        list = userRepository.findByBranchIdAndStatus(branchId, true);
        res = getUserData(list);
        return res;
    }*/

    /* Get all outlet Users of Branch Admin */
    /*public JsonObject getOutletUsers(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Long outletId = users.getOutlet().getId();
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = userRepository.findByOutletIdAndStatus(outletId, true);
        res = getUserData(list);
        return res;
    }*/

    /* Get All users Rolewise of Super Admin */
    public JsonObject getUsersOfCompany(HttpServletRequest httpServletRequest, String userRole, String currentUserRole) {
        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        List<Users> list = new ArrayList<>();
        if (currentUserRole.equalsIgnoreCase("SADMIN")) list = userRepository.findByUserRoleAndStatus(userRole, true);
        else {
            list = userRepository.findByUserRoleAndCreatedByAndStatus(userRole, users.getId(), true);
        }
        JsonObject res = getUserData(list);
        return res;
    }


    public JsonObject getUserData(List<Users> list) {
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        if (list.size() > 0) {
            for (Users mUser : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mUser.getId());
                if (mUser.getOutlet() != null) response.addProperty("companyName", mUser.getOutlet().getCompanyName());
                if (mUser.getBranch() != null)
                    response.addProperty("branchName", mUser.getBranch() != null ? mUser.getBranch().getBranchName() : "");
                response.addProperty("username", mUser.getUsername());
                response.addProperty("fullName", mUser.getFullName()!= null ? mUser.getFullName().toString() : "");
                response.addProperty("mobileNumber", mUser.getMobileNumber() != null ? mUser.getMobileNumber().toString() : "");
                response.addProperty("email", mUser.getEmail() != null ? mUser.getEmail().toString() : "");
                response.addProperty("address", mUser.getAddress());
                response.addProperty("gender", mUser.getGender());
                response.addProperty("usercode", mUser.getUsercode());
                response.addProperty("isSwitch", mUser.getStatus() == true ? 1 : 0);
                result.add(response);
            }
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject getMesgForTokenExpired(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", "Hello Token");
        jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        return jsonObject;
    }

    public JsonObject getUsersOfCompanyNew(HttpServletRequest httpServletRequest) {
        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
        list = userRepository.findByUserRoleIgnoreCaseAndStatusAndOutletId("USER", true, users.getOutlet().getId());
        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject setUserPermissions(HttpServletRequest request) {
        Long userId = Long.parseLong(request.getParameter("user_id"));
        Users users = userRepository.findByIdAndStatus(userId, true);
        users.setPermissions(request.getParameter("permissions"));
        userRepository.save(users);
        JsonObject res = new JsonObject();
        res.addProperty("message", "success");
        res.addProperty("responseStatus", HttpStatus.OK.value());
        return res;
    }

    public JsonObject getCompanyAdmins(HttpServletRequest httpServletRequest) {
//        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
        // Long companyId = Long.parseLong(httpServletRequest.getParameter("companyId"));
        list = userRepository.findByUserRoleIgnoreCaseAndStatus("CADMIN", true);
        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject getBranchAdmins(HttpServletRequest httpServletRequest) {
//        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));

        // Long companyId = Long.parseLong(httpServletRequest.getParameter("companyId"));
        list = userRepository.findByUserRoleIgnoreCaseAndStatusAndOutletId("BADMIN", true, users.getOutlet().getId());
        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }


    public JsonObject getUsersOfBranchNew(HttpServletRequest httpServletRequest) {
        Users users = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
//        list = userRepository.findByUserRoleIgnoreCaseAndStatusAndOutletIdAndBranchId("USER", true, users.getOutlet().getId(), users.getBranch().getId());
        list = userRepository.findByUserRoleIgnoreCaseAndOutletId("USER", users.getOutlet().getId());
//        list = userRepository.findUsers("USER",true);

        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject getUserPermissions(HttpServletRequest request) {
        /* getting User Permissions */
        JsonObject finalResult = new JsonObject();
        JsonArray userPermissions = new JsonArray();
        JsonArray permissions = new JsonArray();
        JsonArray masterModules = new JsonArray();
        Long userId = Long.parseLong(request.getParameter("user_id"));
        List<SystemAccessPermissions> list = systemAccessPermissionsRepository.findByUsersIdAndStatus(userId, true);
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
        return finalResult;
    }

    public Object checkInvoiceDateIsBetweenFY(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
//            LocalDate invoiceDate = LocalDate.parse(request.getParameter("invoiceDate"));
            String invoiceDate = request.getParameter("invoiceDate");
           /* SimpleDateFormat sdf1 = new SimpleDateFormat("YYYY-MM-dd");
            Date curDate = sdf1.parse(invoiceDate);*/
            LocalDate curDate = LocalDate.parse(invoiceDate);
            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(curDate);
            if (fiscalYear == null) {
                response.addProperty("message", "Date is not in the financial year");
                response.addProperty("response", false);
                response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            } else {
                response.addProperty("message", "");
                response.addProperty("response", true);
                response.addProperty("responseStatus", HttpStatus.OK.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            response.addProperty("response", false);
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject validateUser(HttpServletRequest request) {
        //  Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        String userCode = request.getParameter("userCode");
        Users user = userRepository.findByUsercodeIgnoreCaseAndStatus(userCode, true);
        if (user != null) {
            jsonObject.addProperty("message", "User Name Already Exists, Use Different User ID");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            jsonObject.addProperty("message", "New User");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return jsonObject;
    }

    public JsonObject userDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Users user1 = userRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        try {

            if (user1 != null) {
                user1.setStatus(false);
                userRepository.save(user1);
                jsonObject.addProperty("message", "User deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());

            } else {
                jsonObject.addProperty("message", "Not allowed to delete default user");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return jsonObject;
    }


    public JsonObject forgetPasswordOwner(Map<String, String> request) {
        JsonObject responseMessage = new JsonObject();
        Users users = userRepository.findByUsernameAndUserRole(request.get("username"), "SADMIN");
        if (users != null) {
            try {
                responseMessage.addProperty("mobileNumber", users.getMobileNumber() != null ? users.getMobileNumber().toString() : "");
                responseMessage.addProperty("otp", "1234");
                responseMessage.addProperty("message", "Password changed successfully");
                responseMessage.addProperty("responseStatus", HttpStatus.OK.value());
            } catch (Exception e) {
                responseMessage.addProperty("message", "error");
                responseMessage.addProperty("responseStatus", HttpStatus.BAD_REQUEST.value());
            }
        } else {
            responseMessage.addProperty("message", "error");
            responseMessage.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    public Object changePassword(String username, String password) {
        Users users = null;
        ResponseMessage responseMessage = new ResponseMessage();
        users = userRepository.findByUsernameAndStatus(username, true);
        if (users != null) {
            users.setPlain_password(password);
            String encPassword = bcryptEncoder.passwordEncoderNew().encode(password);
            users.setPassword(encPassword);
            Users users1 = userRepository.save(users);
            if (users1 != null) {
                responseMessage.setMessage("Password changed successfully.");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            } else {
                responseMessage.setMessage("Failed to change password.");
                responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
            }
        } else {
            responseMessage.setMessage("Failed to match current password.");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    public JsonObject disableUser(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users user1 = userRepository.findById(Long.parseLong(request.getParameter("id"))).get();
        Boolean status = Boolean.parseBoolean(request.getParameter("isEnable"));//true for enable / false for disable
        try {

            if (user1 != null) {
                user1.setStatus(status);
                userRepository.save(user1);
                if (status) {
                    jsonObject.addProperty("message", "User Enabled successfully");
//                    jsonObject.addProperty("status", true);
                } else {
                    jsonObject.addProperty("message", "User Disable successfully");
//                    jsonObject.addProperty("status", false);
                }
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                jsonObject.addProperty("message", "Not allowed to delete default user");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject getCompanyallAdmins(HttpServletRequest httpServletRequest) {
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<Users> list = new ArrayList<>();
        // Long companyId = Long.parseLong(httpServletRequest.getParameter("companyId"));
        list = userRepository.findByUserRoleIgnoreCase("CADMIN");
        if (list.size() > 0) {
            res = getUserData(list);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonObject validateUserUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Users user = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        Long branchId = null;
        Long hsnId = Long.parseLong(request.getParameter("id"));
        if (paramMap.containsKey("branchId")) branchId = Long.parseLong(request.getParameter("branchId"));
        if (branchId != null) {
            user = userRepository.findByOutletIdAndBranchIdAndUsernameAndStatus(users.getOutlet().getId(), branchId, request.getParameter("userCode"), true);
        } else {
            user = userRepository.findByOutletIdAndUsernameAndStatusAndBranchIsNull(users.getOutlet().getId(), request.getParameter("userCode"), true);
        }
        JsonObject result = new JsonObject();
        if (user != null && hsnId != user.getId()) {
            result.addProperty("message", "User Name Already Exists, Use Different User ID");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            result.addProperty("message", "New user");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return result;
    }

    public JsonObject validateCadminUpdate(HttpServletRequest request) {

        Users user = null;
        Map<String, String[]> paramMap = request.getParameterMap();

        Long adminId = Long.parseLong(request.getParameter("id"));

        user = userRepository.findByUsernameAndStatus(request.getParameter("userCode"), true);

        JsonObject result = new JsonObject();
        if (user != null && adminId != user.getId()) {
            result.addProperty("message", "Duplicate User");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            result.addProperty("message", "New user");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return result;
    }

    public Object resetPasswordWithMobile(String password, String currentPassword, HttpServletRequest httpServletRequest) {
        Users tokenUser = jwtRequestFilter.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        Users users = null;
        ResponseMessage responseMessage = new ResponseMessage();
        users = userRepository.findByUsernameAndStatus(tokenUser.getUsername(), true);
        if (users != null) {
            if (bcryptEncoder.passwordEncoderNew().matches(currentPassword, users.getPassword())) {
                users.setPlain_password(password);
                String encPassword = bcryptEncoder.passwordEncoderNew().encode(password);
                users.setPassword(encPassword);
                userRepository.save(users);
                responseMessage.setMessage("Password changed successfully.");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            } else {
                responseMessage.setMessage("Current password is incorrect.");
                responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
            }
        } else {
            responseMessage.setMessage("Failed to match current password.");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    public JsonObject companyCreateByLedgerCode(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        JsonObject jsonObject = new JsonObject();

        try {
//            LedgerMaster mLedger = ledgerMasterRepository.findByLedgerCodeAndStatus(Long.parseLong(request.getParameter("companyCode")), true);
            LedgerMaster mLedger = ledgerMasterRepository.findByLedgerCodeAndStatus(request.getParameter("companyCode"), true);

            if (mLedger != null) {

                jsonObject.addProperty("id", mLedger.getId());
                jsonObject.addProperty("default_ledger", mLedger.getIsDefaultLedger());
                jsonObject.addProperty("ledger_name", mLedger.getLedgerName());
                jsonObject.addProperty("is_private", mLedger.getIsPrivate());
                jsonObject.addProperty("supplier_code", mLedger.getLedgerCode() != null ? mLedger.getLedgerCode() : null);
                if (mLedger.getMailingName() != null) jsonObject.addProperty("mailing_name", mLedger.getMailingName());
                if (mLedger.getOpeningBalType() != null)
                    jsonObject.addProperty("opening_bal_type", mLedger.getOpeningBalType());

                jsonObject.addProperty("sales_rate", mLedger.getSalesRate() != null ? mLedger.getSalesRate().toString() : "");
                if (mLedger.getSalesmanId() != null) {
                    //jsonObject.addProperty("salesman", mLedger.getColumnA()); // columnA= salesman
                    jsonObject.addProperty("salesmanId", mLedger.getSalesmanId());
                } else {
                    //  jsonObject.addProperty("salesman", ""); // columnA= salesman
                    jsonObject.addProperty("salesmanId", "");
                }
                jsonObject.addProperty("route", mLedger.getRoute() != null ? mLedger.getRoute() : "");
                if (mLedger.getArea() != null) {
                    jsonObject.addProperty("area", mLedger.getArea());
                    jsonObject.addProperty("areaId", mLedger.getAreaId());
                } else {
                    jsonObject.addProperty("area", "");
                    jsonObject.addProperty("areaId", "");
                }
                if (mLedger.getOpeningBal() != null)
                    jsonObject.addProperty("opening_bal", Math.abs(mLedger.getOpeningBal()));
                if (mLedger.getBalancingMethod() != null)
                    jsonObject.addProperty("balancing_method", mLedger.getBalancingMethod().getId());

                jsonObject.addProperty("address", mLedger.getAddress() != null ? mLedger.getAddress() : "");
                jsonObject.addProperty("state", mLedger.getState() != null ? mLedger.getState().getId() : null);
                jsonObject.addProperty("country", mLedger.getCountry() != null ? mLedger.getCountry().getId() : null);
                jsonObject.addProperty("pincode", (mLedger.getPincode() != null && mLedger.getPincode() != 0L) ? mLedger.getPincode().toString() : "");
                jsonObject.addProperty("city", mLedger.getCity() != null ? mLedger.getCity() : "");
                jsonObject.addProperty("email", mLedger.getEmail() != null ? mLedger.getEmail() : "");
                jsonObject.addProperty("mobile_no", (mLedger.getMobile() != null && mLedger.getMobile() != 0L) ? mLedger.getMobile().toString() : "");
                jsonObject.addProperty("whatsapp_no", (mLedger.getWhatsAppno() != null && mLedger.getWhatsAppno() != 0L) ? mLedger.getWhatsAppno().toString() : "");
                if (mLedger.getTaxable() != null) jsonObject.addProperty("taxable", mLedger.getTaxable());
                if (mLedger.getTaxType() != null) jsonObject.addProperty("tax_type", mLedger.getTaxType());
                jsonObject.addProperty("under_prefix", mLedger.getUnderPrefix());
                jsonObject.addProperty("under_prefix_separator", mLedger.getUnderPrefix().split("#")[0]);
                jsonObject.addProperty("under_id", mLedger.getUnderPrefix().split("#")[1]);
                /* pune visit changes */
                jsonObject.addProperty("credit_days", mLedger.getCreditDays());
                jsonObject.addProperty("applicable_from", mLedger.getApplicableFrom());
                jsonObject.addProperty("sales_rate", mLedger.getSalesRate());
                jsonObject.addProperty("fssai", mLedger.getFoodLicenseNo() != null ? mLedger.getFoodLicenseNo() : "");
                jsonObject.addProperty("fssai_expiry", mLedger.getFssaiExpiry() != null ? mLedger.getFssaiExpiry().toString() : "");
                jsonObject.addProperty("drug_expiry", mLedger.getDrugExpiry() != null ? mLedger.getDrugExpiry().toString() : "");
                jsonObject.addProperty("drug_license_no", mLedger.getDrugLicenseNo() != null ? mLedger.getDrugLicenseNo().toString() : "");
                jsonObject.addProperty("tds", mLedger.getTds());
                jsonObject.addProperty("tcs", mLedger.getTcs());
                jsonObject.addProperty("tds_applicable_date", mLedger.getTdsApplicableDate() != null ? mLedger.getTdsApplicableDate().toString() : "");
                jsonObject.addProperty("tcs_applicable_date", mLedger.getTcsApplicableDate() != null ? mLedger.getTcsApplicableDate().toString() : "");
                jsonObject.addProperty("licenseNo", mLedger.getLicenseNo());
                jsonObject.addProperty("reg_date", mLedger.getLicenseExpiry() != null ? mLedger.getLicenseExpiry().toString() : "");
                jsonObject.addProperty("manufacturingLicenseNo", mLedger.getManufacturingLicenseNo());
                jsonObject.addProperty("manufacturingLicenseExpiry", mLedger.getManufacturingLicenseExpiry() != null ? mLedger.getManufacturingLicenseExpiry().toString() : "");
                jsonObject.addProperty("gstTransferDate", mLedger.getGstTransferDate() != null ? mLedger.getGstTransferDate().toString() : "");
                jsonObject.addProperty("gstin", mLedger.getGstin());
                jsonObject.addProperty("place", mLedger.getPlace());
                jsonObject.addProperty("district", mLedger.getDistrict());
                jsonObject.addProperty("landMark", mLedger.getLandMark());
                jsonObject.addProperty("businessType", mLedger.getBusinessType());
                jsonObject.addProperty("businessTrade", mLedger.getBusinessTrade());
                jsonObject.addProperty("creditNumBills", mLedger.getCreditNumBills());
                jsonObject.addProperty("creditBillValue", mLedger.getCreditBillValue());
                jsonObject.addProperty("lrBillDate", mLedger.getLrBillDate() != null ? mLedger.getLrBillDate().toString() : "");
                jsonObject.addProperty("creditBillDate", mLedger.getCreditBillDate() != null ? mLedger.getCreditBillDate().toString() : "");
                jsonObject.addProperty("anniversary", mLedger.getAnniversary() != null ? mLedger.getAnniversary().toString() : "");
                jsonObject.addProperty("isCredit", mLedger.getIsCredit());
                jsonObject.addProperty("isGST", mLedger.getTaxable());
                jsonObject.addProperty("isLicense", mLedger.getIsLicense());
                jsonObject.addProperty("isShippingDetails", mLedger.getIsShippingDetails());
                jsonObject.addProperty("isDepartment", mLedger.getIsDepartment());
                jsonObject.addProperty("isBankDetails", mLedger.getIsBankDetails());

                jsonObject.addProperty("businessType", mLedger.getBusinessType());
                jsonObject.addProperty("ownerName", mLedger.getOwnerName());
                jsonObject.addProperty("ownerAddress", mLedger.getOwnerAddress());
                jsonObject.addProperty("ownerEmail", mLedger.getOwnerEmail());
                jsonObject.addProperty("ownerMobile", mLedger.getOwnerMobile());
                jsonObject.addProperty("ownerPincode", mLedger.getOwnerPincode());
                jsonObject.addProperty("ownerWhatsapp", mLedger.getOwnerWhatsappNo());
                jsonObject.addProperty("ownerstate", mLedger.getOwnerstate().getId());
                jsonObject.addProperty("education", mLedger.getEducation());
                jsonObject.addProperty("ownerDOB", mLedger.getDob() != null ? mLedger.getDob().toString() : "");
                jsonObject.addProperty("age", mLedger.getAge());
//                jsonObject.addProperty("ownerWhatsapp",mLedger.getPresentOccupation());
                jsonObject.addProperty("gender", mLedger.getGender());
                jsonObject.addProperty("presentOccupation", mLedger.getPresentOccupation());

                jsonObject.addProperty("aadarUpload", mLedger.getAadarUpload() != null ? serverUrl + mLedger.getAadarUpload() : "");
                jsonObject.addProperty("panUpload", mLedger.getPanUpload() != null ? serverUrl + mLedger.getPanUpload() : "");
                jsonObject.addProperty("dlUpload", mLedger.getDLUpload() != null ? serverUrl + mLedger.getDLUpload() : "");
//                jsonObject.addProperty("aadarUpload",mLedger.getAadarUpload());
//                jsonObject.addProperty("panUpload",mLedger.getPanUpload());
//                jsonObject.addProperty("dlUpload",mLedger.getDLUpload());


                /* gst Details of Ledger */
                JsonArray jsongstArray = new JsonArray();
                if (mLedger.getTaxable() != null && mLedger.getTaxable()) {
                    List<LedgerGstDetails> gstList = new ArrayList<>();
                    gstList = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                    if (gstList != null && gstList.size() > 0) {
                        for (LedgerGstDetails mList : gstList) {
                            JsonObject mObject = new JsonObject();
                            mObject.addProperty("id", mList.getId());
                            mObject.addProperty("gstin", mList.getGstin());
                            mObject.addProperty("dateOfRegistration", mList.getDateOfRegistration() != null ? mList.getDateOfRegistration().toString() : "");
                            mObject.addProperty("pancard", mList.getPanCard());
                            mObject.addProperty("registraion_type", mList.getRegistrationType());
                            Long registraton_type = mList.getRegistrationType();
                            GstTypeMaster gstTypeMaster = gstMasterRepository.findById(registraton_type).get();
                            mObject.addProperty("registraion_type1", gstTypeMaster != null ? gstTypeMaster.getGstType() : "");
                            jsongstArray.add(mObject);
                        }
                    }
                }
                jsonObject.add("gstdetails", jsongstArray);
                /* end of GST Details */

                /* Shipping Address Details */
                JsonArray jsonshippingArray = new JsonArray();
                if (mLedger.getIsShippingDetails() != null && mLedger.getIsShippingDetails()) {
                    List<LedgerShippingAddress> shippingList = new ArrayList<>();
                    shippingList = ledgerShippingDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                    if (shippingList != null && shippingList.size() > 0) {
                        for (LedgerShippingAddress mList : shippingList) {
                            JsonObject mObject = new JsonObject();
                            mObject.addProperty("id", mList.getId());
                            State mState = stateRepository.findByName(mList.getDistrict());
                            mObject.addProperty("district", mState != null ? mState.getId().toString() : "");
                            mObject.addProperty("shipping_address", mList.getShippingAddress());
                            jsonshippingArray.add(mObject);
                        }
                    }
                }
                jsonObject.add("shippingDetails", jsonshippingArray);
                /* End of Shipping Address Details */

                /* Billing Address Details */
                JsonArray jsonbillingArray = new JsonArray();
                List<LedgerBillingDetails> billingDetails = new ArrayList<>();
                billingDetails = ledgerBillingDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                if (billingDetails != null && billingDetails.size() > 0) {
                    for (LedgerBillingDetails mList : billingDetails) {
                        JsonObject mObject = new JsonObject();
                        mObject.addProperty("id", mList.getId());
                        mObject.addProperty("district", mList.getDistrict());
                        mObject.addProperty("billing_address", mList.getBillingAddress());
                        jsonbillingArray.add(mObject);
                    }
                }
                jsonObject.add("billingDetails", jsonbillingArray);
                /* End of Billing Address Details */

                /* Bank Details */
                JsonArray jsonbankArray = new JsonArray();
                if (mLedger.getIsBankDetails() != null && mLedger.getIsBankDetails()) {
                    List<LedgerBankDetails> ledgerBankDetails = new ArrayList<>();
                    ledgerBankDetails = ledgerbankDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                    if (ledgerBankDetails != null && ledgerBankDetails.size() > 0) {
                        for (LedgerBankDetails mList : ledgerBankDetails) {
                            JsonObject mObject = new JsonObject();
                            mObject.addProperty("id", mList.getId());
                            mObject.addProperty("bank_name", mList.getBankName());
                            mObject.addProperty("bank_ifsc_code", mList.getIfsc());
                            mObject.addProperty("bank_account_no", mList.getAccountNo());
                            mObject.addProperty("bank_branch", mList.getBankBranch());
                            jsonbankArray.add(mObject);
                        }
                    }
                }
                jsonObject.add("bankDetails", jsonbankArray);
                /* End of Billing Address Details */

                /* Deptartment Details */
                JsonArray jsondeptArray = new JsonArray();
                if (mLedger.getIsDepartment() != null && mLedger.getIsDepartment()) {
                    List<LedgerDeptDetails> deptDetails = new ArrayList<>();
                    deptDetails = ledgerDeptDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                    if (deptDetails != null && deptDetails.size() > 0) {
                        for (LedgerDeptDetails mList : deptDetails) {
                            JsonObject mObject = new JsonObject();
                            mObject.addProperty("id", mList.getId());
                            mObject.addProperty("dept", mList.getDept());
                            mObject.addProperty("contact_person", mList.getContactPerson());
                            mObject.addProperty("contact_no", mList.getContactNo() != null ? mList.getContactNo().toString() : "");
                            mObject.addProperty("email", mList.getEmail());
                            jsondeptArray.add(mObject);
                        }
                    }
                }
                jsonObject.add("deptDetails", jsondeptArray);
                /* End of Department Details */

                /* License Details */
                JsonArray jsonlicenseArray = new JsonArray();
                if (mLedger.getIsLicense() != null && mLedger.getIsLicense()) {
                    List<LedgerLicenseDetails> licenseDetails = new ArrayList<>();
                    licenseDetails = ledgerLicenseDetailsRepository.findByLedgerMasterIdAndStatus(mLedger.getId(), true);
                    if (licenseDetails != null && licenseDetails.size() > 0) {
                        for (LedgerLicenseDetails mList : licenseDetails) {
                            JsonObject mObject = new JsonObject();
                            mObject.addProperty("id", mList.getId());
                            mObject.addProperty("licenses_num", mList.getLicenseNum());
                            mObject.addProperty("licenses_exp", mList.getLicenseExp() != null ? mList.getLicenseExp().toString() : "");
                            JsonObject licenseType = new JsonObject();
                            licenseType.addProperty("slug_name", mList.getSlugName());
                            mObject.add("licences_type", licenseType);
                            jsonlicenseArray.add(mObject);
                        }
                    }
                }
                jsonObject.add("licensesDetails", jsonlicenseArray);
                /*** Ledger Opening Balance of SC and SD with Invoices ****/


                if (mLedger.getRegistrationType() != null)
                    jsonObject.addProperty("registration_type", mLedger.getRegistrationType());
                if (mLedger.getPancard() != null) jsonObject.addProperty("pancard_no", mLedger.getPancard());
                if (mLedger.getBankName() != null) jsonObject.addProperty("bank_name", mLedger.getBankName());
                if (mLedger.getAccountNumber() != null)
                    jsonObject.addProperty("account_no", mLedger.getAccountNumber());
                if (mLedger.getIfsc() != null) jsonObject.addProperty("ifsc_code", mLedger.getIfsc());
                if (mLedger.getBankBranch() != null) jsonObject.addProperty("bank_branch", mLedger.getBankBranch());
                if (mLedger.getPrincipleGroups() != null) {
                    jsonObject.addProperty("principle_id", mLedger.getPrinciples().getId());
                    jsonObject.addProperty("principle_name", mLedger.getPrinciples().getPrincipleName());
                    jsonObject.addProperty("ledger_form_parameter_id", mLedger.getPrincipleGroups().getLedgerFormParameter().getId());
                    jsonObject.addProperty("ledger_form_parameter_slug", mLedger.getPrincipleGroups().getLedgerFormParameter().getSlugName());
                    jsonObject.addProperty("sub_principle_id", mLedger.getPrincipleGroups().getId());
                    jsonObject.addProperty("subprinciple_name", mLedger.getPrincipleGroups().getGroupName());
                } else {
                    jsonObject.addProperty("principle_id", mLedger.getPrinciples().getId());
                    jsonObject.addProperty("principle_name", mLedger.getPrinciples().getPrincipleName());
                    jsonObject.addProperty("ledger_form_parameter_id", mLedger.getPrinciples().getLedgerFormParameter().getId());
                    jsonObject.addProperty("ledger_form_parameter_slug", mLedger.getPrinciples().getLedgerFormParameter().getSlugName());
                    jsonObject.addProperty("sub_principle_id", "");
                    jsonObject.addProperty("subprinciple_name", "");
                }
                result.addProperty("message", "success");
                result.addProperty("responseStatus", HttpStatus.OK.value());
                result.add("response", jsonObject);

                // }
                /*else{
                    result.addProperty("message", "Not Found");
                    result.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());}*/
            }
        } catch (Exception e) {
            result.addProperty("message", "Selected row already in use");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            UserLogger.error("Exception:" + exceptionAsString);
        }

        return result;

    }

    public Object changePasswordForBo(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        Users users = userRepository.findByUsernameAndStatus(request.getParameter("usercode"), true);
        if (users != null) {
            users.setPlain_password(request.getParameter("password"));
            String encPassword = bcryptEncoder.passwordEncoderNew().encode(request.getParameter("password"));
            users.setPassword(encPassword);
            Users users1 = userRepository.save(users);
            if (users1 != null) {
                responseMessage.setMessage("Password changed successfully.");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            } else {
                responseMessage.setMessage("Failed to change password.");
                responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
            }
        } else {
            responseMessage.setMessage("Failed to find user with username.");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    /* Get User Profile Details Mobile App */
    public JsonObject loginDetails(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//      JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        if (users != null) {
            JsonObject response = new JsonObject();
            response.addProperty("fullName", users.getFullName());
            response.addProperty("mobileNumber",users.getMobileNumber());
            response.addProperty("mail",users.getEmail());
            response.addProperty("companyCode",users.getCompanyCode());
            response.addProperty("userCode",users.getUsercode());
            response.addProperty("address",users.getAddress());
            response.addProperty("username",users.getUsername());
            response.addProperty("password",users.getPlain_password());
            if(users.getGender()=="true"){
                response.addProperty("gender","Male");
            }else {
                response.addProperty("gender","Female");
            }
//            result.add(response);
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", response);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", new JsonObject());
        }
        return res;
    }

}
