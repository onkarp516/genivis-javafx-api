package in.truethics.ethics.ethicsapiv10.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.CommonAccessPermissions;
import in.truethics.ethics.ethicsapiv10.model.access_permissions.SystemAccessPermissions;
import in.truethics.ethics.ethicsapiv10.model.master.AreaHead;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.access_permission_repository.SystemAccessPermissionsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.AreaHeadRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.service.master_service.AreaHeadService;
import in.truethics.ethics.ethicsapiv10.service.user_service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtTokenUtil {
    @Autowired
    private UserService userService;
    @Autowired
    private AreaHeadService areaHeadService;
    private String SECRET_KEY = "SECRET_KEY";
    @Autowired
    private UsersRepository usersRepository;
    public static long ACCESS_VALIDITY = 24 * 60 * 60;
    public static long TOKEN_VALIDITY = 20 * 60 * 60;
    @Autowired
    private SystemAccessPermissionsRepository systemAccessPermissionsRepository;
    @Autowired
    private CommonAccessPermissions accessPermissions;
    @Autowired
    private AreaHeadRepository areaHeadRepository;

    public String getUsernameFromToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String username = decodedJWT.getSubject();
        return username;
    }

    public Users getUserDataFromToken(String jwtToken){
        String userName = this.getUsernameFromToken(jwtToken);
        if(userName != null) {
            Users user = (Users) userService.findUser(userName);
            return user;
        }
        return null;
    }

    public Long getAreaHeadIdFromToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        Long areaHeadId = Long.parseLong(decodedJWT.getClaim("areaHeadId").toString());
        return areaHeadId;
    }

    public AreaHead getAreadHeadDataFromToken(String jwtToken){
        Long areaHeadId = this.getAreaHeadIdFromToken(jwtToken);
        if(areaHeadId != null) {
            AreaHead areaHead = (AreaHead) areaHeadService.findAreaHead(areaHeadId);
            return areaHead;
        }
        return null;
    }


    public String getTokenFromUsername(String username){
        String access_token = "";
        try {
            Users users = usersRepository.findByUsernameAndStatus(username, true);
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
            JWTCreator.Builder jwtBuilder = JWT.create();
            jwtBuilder.withSubject(users.getUsercode());
            jwtBuilder.withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_VALIDITY * 1000));
            jwtBuilder.withIssuer("bypassLogin");
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
            builder.withIssuer("bypassLogin");

            return access_token;
        } catch (BadCredentialsException be) {
            be.printStackTrace();
            System.out.println("Exception " + be.getMessage());
            System.out.println("Incorrect Username or Password");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            System.out.println("Incorrect Username or Password");
        }
        return access_token;
    }

    public Object generateTokenForMobile(HttpServletRequest req, String username) {
        AreaHead areaHead = areaHeadRepository.findByUsernameIgnoreCaseAndStatus(username, true);
        if (areaHead == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
        String access_token = JWT.create()
                .withSubject(areaHead.getUsername().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (60 * 20) * 60 * 1000))
                .withIssuer(req.getRequestURI())
                .withClaim("name", areaHead.getFirstName()+" "+areaHead.getLastName())
                .withClaim("area_role", areaHead.getAreaRole())
                .withClaim("mobile", areaHead.getMobileNumber())
                .withClaim("areaHeadId", areaHead.getId())
                .withClaim("address", areaHead.getPermenantAddress() != null ? areaHead.getPermenantAddress() : null)
//                .withClaim("state_id", areaHead.getState().getId() != null ? areaHead.getState().getId() : null)
//                .withClaim("zone_id", areaHead.getZone().getId() != null ? areaHead.getZone().getId() : null)
//                .withClaim("region_id", areaHead.getRegion().getId() != null ? areaHead.getRegion().getId() : null)
//                .withClaim("area_id", areaHead.getAreaMaster().getId() != null ? areaHead.getAreaMaster().getId() : null)
//                .withClaim("district_id", areaHead.getDistrict().getId() != null ? areaHead.getDistrict().getId() : null)
//                .withClaim("corporate_area_id", areaHead.getCorporateAreaMaster().getId() != null ? areaHead.getCorporateAreaMaster().getId() : null)
                .sign(algorithm);

        String refresh_token = JWT.create()
                .withSubject(areaHead.getUsername().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + (60 * 24) * 60 * 1000))
                .withIssuer(req.getRequestURI())
                .sign(algorithm);

        System.out.println("Access token length " + access_token);
        System.out.println("Access token length " + access_token.length());
        System.out.println("Refresh token length " + refresh_token.length());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);

        return tokens;
    }
}
