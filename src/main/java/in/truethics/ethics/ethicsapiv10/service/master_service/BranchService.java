package in.truethics.ethics.ethicsapiv10.service.master_service;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.PasswordEncoders;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BranchService {

    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GstTypeMasterRepository gstMasterRepository;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CountryRepository countryRepository;
    private static final Logger branchLogger = LogManager.getLogger(BranchService.class);
    @Autowired
    private PincodeMasterRepository pincodeMasterRepository;
    @Autowired
    private PasswordEncoders bcryptEncoder;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;

    public ResponseMessage createBranch(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = new Branch();
        ResponseMessage responseObject = new ResponseMessage();
        Outlet company = outletRepository.findByIdAndStatus(
                Long.parseLong(request.getParameter("companyId")), true);
        branch.setOutlet(company);
        branch.setCompanyName(company.getCompanyName());
        branch.setCompanyCode(company.getCompanyCode());
        try {
            if (paramMap.containsKey("branchCode"))
                branch.setBranchCode(request.getParameter("branchCode"));
            else
                branch.setBranchCode("");
            if (paramMap.containsKey("branchName"))
                branch.setBranchName(request.getParameter("branchName"));
            else
                branch.setBranchName("");

            if (paramMap.containsKey("sameAsAddress"))
                branch.setIsSameAddress(Boolean.parseBoolean(request.getParameter("sameAsAddress")));
            if (paramMap.containsKey("mobileNumber"))
                branch.setMobileNumber(Long.parseLong(request.getParameter("mobileNumber")));
            if (paramMap.containsKey("whatsappNumber"))
                branch.setWhatsappNumber(Long.parseLong(request.getParameter("whatsappNumber")));
            if (paramMap.containsKey("email"))
                branch.setEmail(request.getParameter("email"));
            else branch.setEmail("");
            if (paramMap.containsKey("website"))
                branch.setWebsite(request.getParameter("website"));
            else
                branch.setWebsite("");
            if (paramMap.containsKey("registeredAddress"))
                branch.setRegisteredAddress(request.getParameter("registeredAddress"));
            else
                branch.setRegisteredAddress("");
            if (paramMap.containsKey("corporateAddress"))
                branch.setCorporateAddress(request.getParameter("corporateAddress"));
            else
                branch.setCorporateAddress("");
            if (paramMap.containsKey("pincode"))
                branch.setPincode(request.getParameter("pincode"));
            else branch.setPincode("");
            if (paramMap.containsKey("corporatePincode"))
                branch.setCorpPincode(request.getParameter("corporatePincode"));
            else branch.setCorpPincode("");
            if (paramMap.containsKey("stateCode")) {
                branch.setStateCode(request.getParameter("stateCode"));
                List<State> state = stateRepository.findByStateCode(request.getParameter("stateCode"));
                if (state != null) {
                    branch.setState(state.get(0));
                }
            } else branch.setStateCode("");
            if (paramMap.containsKey("corporatestateCode")) {

                List<State> state = stateRepository.findByStateCode(request.getParameter("corporatestateCode"));
                if (state != null) {
                    branch.setCorporateState(state.get(0).getName());
                }
            } else branch.setCorporateState("");
            if (paramMap.containsKey("city"))
                branch.setDistrict(request.getParameter("city"));
            else branch.setDistrict("");
            if (paramMap.containsKey("corporatecity"))
                branch.setCorporateDistrict(request.getParameter("corporatecity"));
            else branch.setCorporateDistrict("");

            if (paramMap.containsKey("area")) {
                PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                        Long.parseLong(request.getParameter("area"))).get();
                branch.setArea(pincodeMaster.getArea());
            } else
                branch.setArea("");
            if (paramMap.containsKey("corporatearea")) {
                PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                        Long.parseLong(request.getParameter("corporatearea"))).get();
                branch.setCorporateArea(pincodeMaster.getArea());
            } else
                branch.setCorporateArea("");
            if (Boolean.parseBoolean(request.getParameter("gstApplicable"))) {
                branch.setGstApplicable(true);
                branch.setGstNumber(request.getParameter("gstIn"));
                Optional<GstTypeMaster> gstTypeMaster = gstMasterRepository.findById(
                        Long.parseLong(request.getParameter("gstType")));
                branch.setGstTypeMaster(gstTypeMaster.get());
                  /*  String stateCode = request.getParameter("gstIn").substring(0, 2);
                    branch.setStateCode(stateCode);*/
                if (paramMap.containsKey("gstApplicableDate")) {
                    LocalDate date = LocalDate.parse(request.getParameter("gstApplicableDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    branch.setGstApplicableDate(date);
                }
            } else {
                branch.setGstApplicable(false);
                Optional<GstTypeMaster> gstTypeMaster = gstMasterRepository.findById(3L);
                branch.setGstTypeMaster(gstTypeMaster.get());
            }
            branch.setCurrency(request.getParameter("currency"));
            if (paramMap.containsKey("natureOfBusiness"))
                branch.setBusinessType(request.getParameter("natureOfBusiness"));
            if (paramMap.containsKey("tradeOfBusiness"))
                branch.setBusinessTrade(request.getParameter("tradeOfBusiness"));
            Users sadmin = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            branch.setCreatedBy(sadmin.getId());
            branch.setStatus(true);
            /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
            if (paramMap.containsKey("licenseNo")) {
                branch.setLicenseNo(request.getParameter("licenseNo"));
            }
            if (paramMap.containsKey("licenseExpiryDate")) {
                branch.setLicenseExpiry(LocalDate.parse(request.getParameter("licenseExpiryDate")));
            }
            if (paramMap.containsKey("foodLicenseNo")) {
                branch.setFoodLicenseNo(request.getParameter("foodLicenseNo"));
            }
            if (paramMap.containsKey("foodLicenseExpiryDate")) {
                branch.setFoodLicenseExpiry(LocalDate.parse(request.getParameter("foodLicenseExpiryDate")));
            }
            if (paramMap.containsKey("manufacturingLicenseNo")) {
                branch.setManufacturingLicenseNo(request.getParameter("manufacturingLicenseNo"));
            }
            if (paramMap.containsKey("manufacturingLicenseExpiry")) {
                branch.setManufacturingLicenseExpiry(LocalDate.parse(request.getParameter("manufacturingLicenseExpiry")));
            }
            if (paramMap.containsKey("gstTransferDate"))
                branch.setGstTransferDate(LocalDate.parse(request.getParameter("gstTransferDate")));
            if (paramMap.containsKey("place"))
                branch.setPlace(request.getParameter("place"));
            if (paramMap.containsKey("route"))
                branch.setRoute(request.getParameter("route"));
            if (paramMap.containsKey("countryId")) {
                Country country = countryRepository.findById(Long.parseLong(request.getParameter("countryId"))).get();
                if (country != null)
                    branch.setCountry(country);
            }

            /*** END ****/
            Branch mBranch = branchRepository.save(branch);
            /***** Company Admin Creation *****/
            Users users = new Users();
            if (paramMap.containsKey("contactNumber")) {
                users.setMobileNumber(Long.valueOf(request.getParameter("contactNumber")));
            }
            if (paramMap.containsKey("emailId")) {
                users.setEmail(request.getParameter("emailId"));
            }
            if (paramMap.containsKey("fullName")) {
                users.setFullName(request.getParameter("fullName"));
            }

            if (paramMap.containsKey("address")) {
                users.setAddress(request.getParameter("address"));
            } else {
                users.setAddress("");
            }
            if (request.getParameter("gender") != null) {
                users.setGender(request.getParameter("gender"));
            }
            users.setUsercode(request.getParameter("usercode"));
            users.setUsername(request.getParameter("usercode"));
            if (paramMap.containsKey("userRole")) {
                users.setUserRole(request.getParameter("userRole"));
            }
            if (paramMap.containsKey("userDob")) {
                users.setDob(LocalDate.parse(request.getParameter("userDob")));
            }
            if (paramMap.containsKey("userDoa")) {
                users.setAnniversary(LocalDate.parse(request.getParameter("userDoa")));
            }
            users.setStatus(true);
            users.setIsSuperAdmin(false);
            users.setCreatedBy(sadmin.getId());
            users.setPassword(bcryptEncoder.passwordEncoderNew().encode(
                    request.getParameter("password")));
            users.setPlain_password(request.getParameter("password"));
            users.setOutlet(company);
            users.setBranch(mBranch);
            Users newUser = usersRepository.save(users);
            /**** END ****/
            responseObject.setMessage("Branch Created successfully");
            responseObject.setResponseStatus(HttpStatus.OK.value());
            if (mBranch != null) {
                outletRepository.createDefaultLedgers(
                        mBranch.getId(), company.getId(), mBranch.getCreatedBy());
                if (mBranch.getGstTypeMaster().getId() == 1L) {
                    outletRepository.createDefaultGST(
                            mBranch.getId(), company.getId(), mBranch.getCreatedBy());
                } else {
                    outletRepository.createDefaultGSTUnRegistered(
                            mBranch.getId(), company.getId(), mBranch.getCreatedBy());
                }
                /***** Create Default Counter Customer Ledger  *****/
                branchRepository.createCounterCustomerBranch(company.getId(), mBranch.getId(), mBranch.getCreatedBy());
            }

                /*String strJson = request.getParameter("userControlData");
                JsonArray settingArray = new JsonParser().parse(strJson).getAsJsonArray();
                for (JsonElement jsonElement : settingArray) {
                    JsonObject object = jsonElement.getAsJsonObject();
                    AppConfig appConfig = new AppConfig();
                    SystemConfigParameter systemConfigParameter = systemConfigParameterRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
                    appConfig.setSystemConfigParameter(systemConfigParameter);

                    appConfig.setConfigName(object.get("slug").getAsString());
                    if (object.get("value").getAsBoolean())
                        appConfig.setConfigValue(object.get("value").getAsInt());
                    else
                        appConfig.setConfigValue(object.get("value").getAsInt());
                    appConfig.setConfigLabel(object.get("label").getAsString());
                    appConfig.setBranch(users.getBranch());
                    appConfig.setOutlet(users.getOutlet());
                    appConfig.setCreatedBy(users.getId());
                    appConfig.setUpdatedBy(users.getId());
                    appConfig.setStatus(true);
                    AppConfig mAppConfig = appConfigRepository.save(appConfig);
                    responseObject.setMessage("Created Successfully");
                    responseObject.setResponseStatus(HttpStatus.OK.value());
                    responseObject.setResponseObject(mAppConfig.getId().toString());
                }*/
        } catch (Exception e) {
            e.printStackTrace();
            branchLogger.error("createBranch -> failed to createBranch" + e);
            responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseObject.setMessage("failed to create Branch-> " + e.getMessage());
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return responseObject;
    }

    public JsonObject getAllBranches(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        JsonObject res = new JsonObject();
        List<Branch> list = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        list = branchRepository.findByOutletIdAndStatus(users.getOutlet().getId(), true);
        res = getBranchData(list);
        return res;
    }

    /* get branch by id */
/*    public JsonObject getBranchById(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        Branch branch = branchRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")),
                true);
        if (branch != null) {
            response.addProperty("id", branch.getId());
            response.addProperty("companyId", branch.getOutlet().getId());
            response.addProperty("companyName",
                    branch.getOutlet().getCompanyName());
            response.addProperty("branchName", branch.getBranchName());
            response.addProperty("branchCode", branch.getBranchCode());
            response.addProperty("mobileNumber", branch.getMobileNumber());
            response.addProperty("registeredAddress", branch.getRegisteredAddress());
            response.addProperty("corporateAddress", branch.getCorporateAddress());
            response.addProperty("pincode", branch.getPincode());
            response.addProperty("whatsappNumber", branch.getWhatsappNumber());
            response.addProperty("country_id", branch.getCountry().getId());
            response.addProperty("state_id", branch.getState().getId());
            response.addProperty("email", branch.getEmail());
            response.addProperty("website", branch.getWebsite());
            response.addProperty("gstApplicable", branch.getGstApplicable());
            if (branch.getGstApplicable()) {
                response.addProperty("gstType", branch.getGstTypeMaster().getId());
                response.addProperty("gstIn", branch.getGstNumber());
                response.addProperty("gstTypeName", branch.getGstTypeMaster().getGstType());
                response.addProperty("gstApplicableDate",
                        branch.getGstApplicableDate() != null ? branch.getGstApplicableDate().toString() : "");
            }
            response.addProperty("currency", branch.getCurrency());
            result.addProperty("message", "success");
            result.addProperty("responseStatus", HttpStatus.OK.value());
            result.add("responseObject", response);
        } else {
            result.addProperty("message", "not found");
            result.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
        }
        return result;
    }*/
    public JsonObject getBranchById(HttpServletRequest request) {
        Branch branch = branchRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);

        JsonObject response = new JsonObject();
        JsonObject res = new JsonObject();
        if (branch != null) {

            response.addProperty("id", branch.getId());
            response.addProperty("branchCode", branch.getBranchCode());
            response.addProperty("branchName", branch.getBranchName());
            response.addProperty("companyId", branch.getOutlet().getId());
            response.addProperty("companyCode", branch.getCompanyCode());
            response.addProperty("companyName", branch.getCompanyName());
            response.addProperty("sameAsAddress", branch.getIsSameAddress());
            response.addProperty("mobileNumber", branch.getMobileNumber());
            response.addProperty("whatsappNumber", branch.getWhatsappNumber());
            response.addProperty("email", branch.getEmail());
            response.addProperty("website", branch.getWebsite());
            response.addProperty("website", branch.getWebsite());
            response.addProperty("natureOfBusiness", branch.getBusinessType());
            response.addProperty("tradeOfBusiness", branch.getBusinessTrade());
            response.addProperty("gstApplicable", branch.getGstApplicable());
            if (branch.getGstApplicable()) {
                response.addProperty("gstType", branch.getGstTypeMaster() != null ? branch.getGstTypeMaster().getId().toString() : "");
                response.addProperty("gstIn", branch.getGstNumber());
                response.addProperty("gstTypeName", branch.getGstTypeMaster().getGstType());
                response.addProperty("gstApplicableDate",
                        branch.getGstApplicableDate() != null ? branch.getGstApplicableDate().toString() : "");
            }
            response.addProperty("currency", branch.getCurrency());
            response.addProperty("licenseNo", branch.getLicenseNo());
            response.addProperty("licenseExpiryDate", branch.getLicenseExpiry() != null ?
                    branch.getLicenseExpiry().toString() : "");
            response.addProperty("foodLicenseNo", branch.getFoodLicenseNo());
            response.addProperty("foodLicenseExpiryDate", branch.getFoodLicenseExpiry() != null ?
                    branch.getFoodLicenseExpiry().toString() : "");
            response.addProperty("manufacturingLicenseNo", branch.getManufacturingLicenseNo());
            response.addProperty("manufacturingLicenseExpiry", branch.getManufacturingLicenseExpiry() != null ?
                    branch.getManufacturingLicenseExpiry().toString() : "");
            response.addProperty("gstTransferDate", branch.getGstTransferDate() != null ?
                    branch.getGstTransferDate().toString() : "");
            response.addProperty("place", branch.getPlace());
            response.addProperty("route", branch.getRoute());
            response.addProperty("registeredAddress", branch.getRegisteredAddress());
            response.addProperty("corporateAddress", branch.getCorporateAddress());
            response.addProperty("pincode", branch.getPincode());
            response.addProperty("corporatePincode", branch.getCorpPincode());
            response.addProperty("stateId", branch.getState() != null ? branch.getState().getId().toString() : "");
            response.addProperty("stateCode", branch.getStateCode() != null ? branch.getStateCode() : "");
            response.addProperty("state", branch.getState() != null ? branch.getState().getName() : "");
            response.addProperty("corporatestate", branch.getCorporateState() != null ? branch.getCorporateState() : "");
            response.addProperty("city", branch.getDistrict());
            response.addProperty("corporatecity", branch.getCorporateDistrict());
            response.addProperty("countryId", branch.getCountry() != null ? branch.getCountry().getId().toString() : "");
            /**** Area of Registered Address ****/
            List<PincodeMaster> pincodeMasters = pincodeMasterRepository.findByPincode(
                    branch.getPincode());
            JsonArray pincodeArray = new JsonArray();
            if (pincodeMasters != null && pincodeMasters.size() > 0) {
                for (PincodeMaster mPinocdes : pincodeMasters) {
                    JsonObject mObject = new JsonObject();
                    mObject.addProperty("area_id", mPinocdes.getId());
                    int lastIndex = 0;
                    int lastIndex1 = 0;
                    String mArea = "";
                   /* if (mPinocdes.getArea().endsWith("B.O"))
                        lastIndex = mPinocdes.getArea().lastIndexOf("B.O");
                    else
                        lastIndex1 = mPinocdes.getArea().lastIndexOf("S.O");
                    if (lastIndex != 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex);
                    }
                    if (lastIndex1 != 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex1);
                    }   */
                    lastIndex = mPinocdes.getArea().lastIndexOf("B.O");
                    if (lastIndex > 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex);
                    } else {
                        lastIndex = mPinocdes.getArea().indexOf("BO");
                        if (lastIndex > 0) {
                            mArea = mPinocdes.getArea().substring(0, lastIndex);
                        } else {
                            lastIndex = mPinocdes.getArea().indexOf("S.O");
                            if (lastIndex > 0) {
                                mArea = mPinocdes.getArea().substring(0, lastIndex);
                            } else {
                                lastIndex = mPinocdes.getArea().indexOf("SO");
                                if (lastIndex > 0) {
                                    mArea = mPinocdes.getArea().substring(0, lastIndex);
                                }
                            }
                        }
                    }

                    mObject.addProperty("area_name", mArea.trim());
                    pincodeArray.add(mObject);
                }
            }
            if (branch.getArea().equalsIgnoreCase("")) {
                response.addProperty("area", "");
            } else {
                try {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findByAreaAndDistrictAndPincode(
                            branch.getArea(), branch.getDistrict(), branch.getPincode());
                    response.addProperty("area", pincodeMaster.getId());
                } catch (Exception e) {
                }
            }
            response.add("area_list", pincodeArray);


            /**** Area of Corporate Address ****/
            List<PincodeMaster> corpincodeMasters = pincodeMasterRepository.findByPincode(
                    branch.getCorpPincode());
            JsonArray corporateArray = new JsonArray();
            if (corpincodeMasters != null && corpincodeMasters.size() > 0) {
                for (PincodeMaster mPinocdes : corpincodeMasters) {
                    JsonObject mObject = new JsonObject();
                    mObject.addProperty("corporate_area_id", mPinocdes.getId());
                    int lastIndex = 0;
                    int lastIndex1 = 0;
                    String mArea = "";
                    /*if (mPinocdes.getArea().endsWith("B.O"))
                        lastIndex = mPinocdes.getArea().lastIndexOf("B.O");
                    else
                        lastIndex1 = mPinocdes.getArea().lastIndexOf("S.O");
                    if (lastIndex != 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex);
                    }
                    if (lastIndex1 != 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex1);
                    }*/
                    lastIndex = mPinocdes.getArea().lastIndexOf("B.O");
                    if (lastIndex > 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex);
                    } else {
                        lastIndex = mPinocdes.getArea().indexOf("BO");
                        if (lastIndex > 0) {
                            mArea = mPinocdes.getArea().substring(0, lastIndex);
                        } else {
                            lastIndex = mPinocdes.getArea().indexOf("S.O");
                            if (lastIndex > 0) {
                                mArea = mPinocdes.getArea().substring(0, lastIndex);
                            } else {
                                lastIndex = mPinocdes.getArea().indexOf("SO");
                                if (lastIndex > 0) {
                                    mArea = mPinocdes.getArea().substring(0, lastIndex);
                                }
                            }
                        }
                    }
                    mObject.addProperty("corporate_area_name", mArea.trim());
                    corporateArray.add(mObject);
                }
            }
            if (branch.getCorporateArea().equalsIgnoreCase("")) {
                response.addProperty("corporatearea", "");
            } else {
                try {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findByAreaAndDistrictAndPincode(
                            branch.getCorporateArea(), branch.getCorporateDistrict(), branch.getCorpPincode());
                    response.addProperty("corporatearea", pincodeMaster.getId());
                } catch (Exception e) {
                }
            }
            response.add("corporate_area_list", corporateArray);
            /***** User Details *****/


            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", response);
        } else {
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            res.add("responseObject", response);
        }
        return res;
    }

    public JsonObject getBranchData(List<Branch> branches) {
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();

        if (branches.size() > 0) {
            for (Branch mBranch : branches) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mBranch.getId());
                response.addProperty("companyId", mBranch.getOutlet().getId());
                response.addProperty("companyName",
                        mBranch.getOutlet().getCompanyName());
                response.addProperty("branchName", mBranch.getBranchName());
                response.addProperty("branchCode", mBranch.getBranchCode());
                response.addProperty("mobileNumber", mBranch.getMobileNumber());
                response.addProperty("branchContactPerson", mBranch.getBranchContactPerson());
                response.addProperty("registeredAddress", mBranch.getRegisteredAddress());
                response.addProperty("corporateAddress", mBranch.getCorporateAddress());
                response.addProperty("gstApplicable", mBranch.getGstApplicable());
                response.addProperty("gstApplicableDate", mBranch.getGstApplicableDate() != null ? mBranch.getGstApplicableDate().toString() : "");

                result.add(response);
            }
            res.addProperty("message", "success");

        } else {
            res.addProperty("message", "empty list");
        }
        res.addProperty("responseStatus", HttpStatus.OK.value());
        res.add("responseObject", result);
        return res;
    }

    public JsonObject getBranchesCompany(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        List<Branch> list = new ArrayList<>();
        list = branchRepository.findAllByStatus(true);
        res = getBranchData(list);
        return res;
    }

    public JsonObject getBranchesBySelectionCompany(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        List<Branch> branchlist = branchRepository.findByOutletIdAndStatus(Long.valueOf(request.getParameter("id")), true);
        if (branchlist.size() > 0) {
            res = getBranchData(branchlist);
            return res;
        } else {
            res.addProperty("message", "Branch not found against Company");
            res.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public JsonObject duplicateBranch(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        String branchName = request.getParameter("branchName").trim();
        Long outletId = Long.parseLong(request.getParameter("outletId"));
        Branch branch = branchRepository.findByOutletIdAndBranchNameIgnoreCaseAndStatus(outletId, branchName, true);
        if (branch != null) {
            jsonObject.addProperty("message", "Branch is already exist,please try another");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            jsonObject.addProperty("message", "New Branch");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return jsonObject;
    }

    public JsonObject updateBranch(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Branch branch = branchRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        try {
            if (branch != null) {
                Outlet company = outletRepository.findByIdAndStatus(
                        Long.parseLong(request.getParameter("companyId")), true);
                branch.setOutlet(company);
                branch.setCompanyName(company.getCompanyName());
                if (paramMap.containsKey("branchCode"))
                    branch.setBranchCode(request.getParameter("branchCode"));
                else
                    branch.setBranchCode("");
                if (paramMap.containsKey("branchName"))
                    branch.setBranchName(request.getParameter("branchName"));
                else
                    branch.setBranchName("");
                if (paramMap.containsKey("email"))
                    branch.setEmail(request.getParameter("email"));

                if (Boolean.parseBoolean(request.getParameter("gstApplicable"))) {
                    branch.setGstApplicable(true);
                    branch.setGstNumber(request.getParameter("gstIn"));
                    Optional<GstTypeMaster> gstTypeMaster = gstMasterRepository.findById(
                            Long.parseLong(request.getParameter("gstType")));
                    branch.setGstTypeMaster(gstTypeMaster.get());

                    if (paramMap.containsKey("gstApplicableDate")) {
                        LocalDate date = LocalDate.parse(request.getParameter("gstApplicableDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        branch.setGstApplicableDate(date);
                    }
                } else {
                    branch.setGstApplicableDate(null);
                    branch.setGstApplicable(false);
                    Optional<GstTypeMaster> gstTypeMaster = gstMasterRepository.findById(3L);
                    branch.setGstTypeMaster(gstTypeMaster.get());
                }
                if (paramMap.containsKey("mobileNumber"))
                    branch.setMobileNumber(Long.parseLong(request.getParameter("mobileNumber")));
                if (paramMap.containsKey("sameAsAddress"))
                    branch.setIsSameAddress(Boolean.parseBoolean(request.getParameter("sameAsAddress")));
                if (paramMap.containsKey("registeredAddress"))
                    branch.setRegisteredAddress(request.getParameter("registeredAddress"));
                else
                    branch.setRegisteredAddress("");
                if (paramMap.containsKey("corporateAddress"))
                    branch.setCorporateAddress(request.getParameter("corporateAddress"));
                else
                    branch.setCorporateAddress("");
                if (paramMap.containsKey("pincode"))
                    branch.setPincode(request.getParameter("pincode"));
                else branch.setPincode("");
                if (paramMap.containsKey("corporatePincode"))
                    branch.setCorpPincode(request.getParameter("corporatePincode"));
                else branch.setCorpPincode("");
                if (paramMap.containsKey("stateCode")) {
                    branch.setStateCode(request.getParameter("stateCode"));
                    List<State> state = stateRepository.findByStateCode(request.getParameter("stateCode"));
                    if (state != null) {
                        branch.setState(state.get(0));
                    }

                } else branch.setStateCode("");
                if (paramMap.containsKey("corporatestateCode")) {
                    List<State> state = stateRepository.findByStateCode(request.getParameter("corporatestateCode"));
                    if (state != null) {
                        branch.setCorporateState(state.get(0).getName());
                    }
                } else branch.setCorporateState("");
                if (paramMap.containsKey("city"))
                    branch.setDistrict(request.getParameter("city"));
                else branch.setDistrict("");
                if (paramMap.containsKey("corporatecity"))
                    branch.setCorporateDistrict(request.getParameter("corporatecity"));
                else branch.setCorporateDistrict("");

                if (paramMap.containsKey("area")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("area"))).get();
                    branch.setArea(pincodeMaster.getArea());
                } else
                    branch.setArea("");
                if (paramMap.containsKey("corporatearea")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("corporatearea"))).get();
                    branch.setCorporateArea(pincodeMaster.getArea());
                } else
                    branch.setCorporateArea("");
                if (paramMap.containsKey("whatsappNumber"))
                    branch.setWhatsappNumber(Long.parseLong(request.getParameter("whatsappNumber")));
                if (paramMap.containsKey("website"))
                    branch.setWebsite(request.getParameter("website"));
                else
                    branch.setWebsite("");
                if (paramMap.containsKey("currency"))
                    branch.setCurrency(request.getParameter("currency"));
                else
                    branch.setCurrency("");
                if (paramMap.containsKey("countryId")) {
                    Country country = countryRepository.findById(Long.parseLong(request.getParameter("countryId"))).get();
                    if (country != null)
                        branch.setCountry(country);
                }
                Users users = jwtRequestFilter.getUserDataFromToken(
                        request.getHeader("Authorization").substring(7));
                /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                if (paramMap.containsKey("licenseNo")) {
                    branch.setLicenseNo(request.getParameter("licenseNo"));
                } else {
                    branch.setLicenseNo("");
                }
                if (paramMap.containsKey("licenseExpiryDate")) {
                    branch.setLicenseExpiry(LocalDate.parse(request.getParameter("licenseExpiryDate")));
                }
                if (paramMap.containsKey("foodLicenseNo")) {
                    branch.setFoodLicenseNo(request.getParameter("foodLicenseNo"));
                } else {
                    branch.setFoodLicenseNo("");
                }
                if (paramMap.containsKey("foodLicenseExpiryDate")) {
                    branch.setFoodLicenseExpiry(LocalDate.parse(request.getParameter("foodLicenseExpiryDate")));
                }
                if (paramMap.containsKey("manufacturingLicenseNo")) {
                    branch.setManufacturingLicenseNo(request.getParameter("manufacturingLicenseNo"));
                } else {
                    branch.setManufacturingLicenseNo("");
                }
                if (paramMap.containsKey("manufacturingLicenseExpiry")) {
                    branch.setManufacturingLicenseExpiry(LocalDate.parse(request.getParameter("manufacturingLicenseExpiry")));
                }
                if (paramMap.containsKey("gstTransferDate")) {
                    branch.setGstTransferDate(LocalDate.parse(request.getParameter("gstTransferDate")));
                }
                if (paramMap.containsKey("natureOfBusiness"))
                    branch.setBusinessType(request.getParameter("natureOfBusiness"));
                else branch.setBusinessType("");
                if (paramMap.containsKey("tradeOfBusiness"))
                    branch.setBusinessTrade(request.getParameter("tradeOfBusiness"));
                else branch.setBusinessTrade("");
                if (paramMap.containsKey("place"))
                    branch.setPlace(request.getParameter("place"));
                else branch.setPlace("");
                if (paramMap.containsKey("route"))
                    branch.setRoute(request.getParameter("route"));
                else branch.setRoute("");
                if (paramMap.containsKey("stateCode")) {
                    branch.setStateCode(request.getParameter("stateCode"));
                    List<State> state = stateRepository.findByStateCode(request.getParameter("stateCode"));
                    if (state != null) {
                        branch.setState(state.get(0));
                    }
                }
                Branch mBranch = branchRepository.save(branch);
                /***** Company Admin Update *****/
                response.addProperty("message", "Branch updated successfully");
                response.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                response.addProperty("message", "Not found");
                response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            branchLogger.error("updateOutlet -> failed to updateOutlet" + e);
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return response;
    }

    public JsonObject duplicateBranchAdmin(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Long outletId = Long.parseLong(request.getParameter("outletId"));
        Long branchId = Long.parseLong(request.getParameter("branchId"));
        String userCode = request.getParameter("userCode");
        Users users = usersRepository.findByOutletIdAndBranchIdAndStatusAndUsercode(outletId, branchId, true, userCode);
        if (users != null) {
            jsonObject.addProperty("message", "Username is already available ,please try another");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            jsonObject.addProperty("message", "New User");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return jsonObject;
    }

    public JsonObject duplicateBranchUpdate(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        String branchName = request.getParameter("branchName").trim();
        Long outletId = Long.parseLong(request.getParameter("outletId"));
        Long id = Long.parseLong(request.getParameter("id"));
        Branch branch = branchRepository.findByOutletIdAndBranchNameIgnoreCaseAndStatus(outletId, branchName, true);
        if (branch != null && id != branch.getId()) {
            jsonObject.addProperty("message", "Branch is already exist,please try another");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            jsonObject.addProperty("message", "New Branch");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return jsonObject;
    }
}

