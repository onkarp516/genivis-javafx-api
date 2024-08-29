package in.truethics.ethics.ethicsapiv10.service.master_service;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.common.PasswordEncoders;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageService;
import in.truethics.ethics.ethicsapiv10.model.appconfig.AppConfig;
import in.truethics.ethics.ethicsapiv10.model.appconfig.SystemConfigParameter;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.appconfig.AppConfigRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerBankDetailsRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.SystemConfigParameterRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OutletService {

    @Autowired
    private OutletRepository outletRepository;

    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private GstTypeMasterRepository gstMasterRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private PasswordEncoders bcryptEncoder;
    @Autowired
    private UsersRepository userRepository;
    private static final Logger outletLogger = LogManager.getLogger(OutletService.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Autowired
    private SystemConfigParameterRepository systemConfigParameterRepository;
    @Autowired
    private PincodeMasterRepository pincodeMasterRepository;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Autowired
    private BalancingMethodRepository balancingMethodRepository;
    @Autowired
    private PrincipleRepository principleRepository;
    @Autowired
    private PrincipleGroupsRepository principleGroupsRepository;
    @Autowired
    private FoundationRepository foundationRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;
    @Autowired
    private LedgerBankDetailsRepository ledgerBankDetailsRepository;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private AssociateGroupsRepository associateGroupsRepository;

    public Object createOutlet(MultipartHttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Outlet outlet = new Outlet();
        ResponseMessage responseObject = new ResponseMessage();
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        if (validateDuplicateCompany(request.getParameter("companyName"))) {
            responseObject.setMessage("Company with this name is already exist");
            responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            try {
                outlet.setCompanyName(request.getParameter("companyName"));
                if (paramMap.containsKey("companyCode"))
                    outlet.setCompanyCode(request.getParameter("companyCode"));
                else
                    outlet.setCompanyCode("");
                if (paramMap.containsKey("sameAsAddress"))
                    outlet.setIsSameAddress(Boolean.parseBoolean(request.getParameter("sameAsAddress")));
                if (paramMap.containsKey("mobileNumber"))
                    outlet.setMobileNumber(Long.parseLong(request.getParameter("mobileNumber")));
                if (paramMap.containsKey("whatsappNumber"))
                    outlet.setWhatsappNumber(Long.parseLong(request.getParameter("whatsappNumber")));
                if (paramMap.containsKey("email"))
                    outlet.setEmail(request.getParameter("email"));
                else outlet.setEmail("");
                if (paramMap.containsKey("businessType"))
                    outlet.setBusinessType(request.getParameter("businessType"));
                else outlet.setBusinessType("");
                if (paramMap.containsKey("website"))
                    outlet.setWebsite(request.getParameter("website"));
                else
                    outlet.setWebsite("");
                if (paramMap.containsKey("registeredAddress"))
                    outlet.setRegisteredAddress(request.getParameter("registeredAddress"));
                else
                    outlet.setRegisteredAddress("");
                if (paramMap.containsKey("corporateAddress"))
                    outlet.setCorporateAddress(request.getParameter("corporateAddress"));
                else
                    outlet.setCorporateAddress("");
                if (paramMap.containsKey("pincode"))
                    outlet.setPincode(request.getParameter("pincode"));
                else outlet.setPincode("");
                if (paramMap.containsKey("corporatePincode"))
                    outlet.setCorpPincode(request.getParameter("corporatePincode"));
                else outlet.setCorpPincode("");
                if (paramMap.containsKey("stateCode")) {
                    outlet.setStateCode(request.getParameter("stateCode"));
                    List<State> state = stateRepository.findByStateCode(request.getParameter("stateCode"));
                    if (state != null) {
                        outlet.setState(state.get(0));
                    }
                } else outlet.setStateCode("");
                if (paramMap.containsKey("corporatestateCode")) {

                    List<State> state = stateRepository.findByStateCode(request.getParameter("corporatestateCode"));
                    if (state != null) {
                        outlet.setCorporateState(state.get(0).getName());
                    }
                } else outlet.setCorporateState("");
                if (paramMap.containsKey("city"))
                    outlet.setDistrict(request.getParameter("city"));
                else outlet.setDistrict("");
                if (paramMap.containsKey("corporatecity"))
                    outlet.setCorporateDistrict(request.getParameter("corporatecity"));
                else outlet.setCorporateDistrict("");
                if (paramMap.containsKey("area")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("area"))).get();
                    outlet.setArea(pincodeMaster.getArea());
                } else
                    outlet.setArea("");
                if (paramMap.containsKey("corporatearea")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("corporatearea"))).get();
                    outlet.setCorporateArea(pincodeMaster.getArea());
                } else
                    outlet.setCorporateArea("");
                if (Boolean.parseBoolean(request.getParameter("gstApplicable"))) {
                    outlet.setGstApplicable(true);
                    outlet.setGstNumber(request.getParameter("gstIn"));
                    Optional<GstTypeMaster> gstTypeMaster = gstMasterRepository.findById(
                            Long.parseLong(request.getParameter("gstType")));
                    outlet.setGstTypeMaster(gstTypeMaster.get());
                  /*  String stateCode = request.getParameter("gstIn").substring(0, 2);
                    outlet.setStateCode(stateCode);*/
                    if (paramMap.containsKey("gstApplicableDate")) {
                        LocalDate date = LocalDate.parse(request.getParameter("gstApplicableDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        outlet.setGstApplicableDate(date);
                    }
                } else {
                    outlet.setGstApplicable(false);
                    Optional<GstTypeMaster> gstTypeMaster = gstMasterRepository.findById(3L);
                    outlet.setGstTypeMaster(gstTypeMaster.get());
                }
                outlet.setCurrency(request.getParameter("currency"));
                if (paramMap.containsKey("natureOfBusiness"))
                    outlet.setBusinessType(request.getParameter("natureOfBusiness"));
                if (paramMap.containsKey("tradeOfBusiness"))
                    outlet.setBusinessTrade(request.getParameter("tradeOfBusiness"));
                if (paramMap.containsKey("companydatapath"))
                    outlet.setCompanyDataPath("companydatapath");
                else
                    outlet.setCompanyDataPath("");
                Users sadmin = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
                outlet.setCreatedBy(sadmin.getId());
                outlet.setStatus(true);
                if (paramMap.containsKey("countryId")) {
                    Country country = countryRepository.findById(Long.parseLong(request.getParameter("countryId"))).get();
                    if (country != null)
                        outlet.setCountry(country);
                }
                /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                if (paramMap.containsKey("licenseNo")) {
                    outlet.setLicenseNo(request.getParameter("licenseNo"));
                }
                if (paramMap.containsKey("licenseExpiryDate")) {
                    outlet.setLicenseExpiry(LocalDate.parse(request.getParameter("licenseExpiryDate")));
                }

                if (paramMap.containsKey("foodLicenseNo")) {
                    outlet.setFoodLicenseNo(request.getParameter("foodLicenseNo"));
                }
                if (paramMap.containsKey("foodLicenseExpiryDate")) {
                    outlet.setFoodLicenseExpiry(LocalDate.parse(request.getParameter("foodLicenseExpiryDate")));
                }
                if (paramMap.containsKey("manufacturingLicenseNo")) {
                    outlet.setManufacturingLicenseNo(request.getParameter("manufacturingLicenseNo"));
                }
                if (paramMap.containsKey("manufacturingLicenseExpiry")) {
                    outlet.setManufacturingLicenseExpiry(LocalDate.parse(request.getParameter("manufacturingLicenseExpiry")));
                }
                if (paramMap.containsKey("gstTransferDate"))
                    outlet.setGstTransferDate(LocalDate.parse(request.getParameter("gstTransferDate")));
                if (paramMap.containsKey("place"))
                    outlet.setPlace(request.getParameter("place"));
                if (paramMap.containsKey("route"))
                    outlet.setRoute(request.getParameter("route"));
                if (paramMap.containsKey("isMultiBranch"))
                    outlet.setIsMultiBranch(Boolean.parseBoolean(request.getParameter("isMultiBranch")));
                if (request.getFile("companyLogo") != null) {
                    MultipartFile image = request.getFile("companyLogo");
                    fileStorageProperties.setUploadDir("./uploads" + File.separator + sadmin.getId() + File.separator +
                            "company" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        outlet.setCompanyLogo("/uploads" + File.separator + sadmin.getId() +
                                File.separator + "company" + File.separator + imagePath);
                    }
                }
                /**** Uploading Company LOGO ****/
                if (request.getFile("uploadImage") != null) {
                    MultipartFile image = request.getFile("uploadImage");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        outlet.setCompanyLogo(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                /*** END ****/
                Outlet mOutlet = outletRepository.save(outlet);

                if (outlet != null && !request.getHeader("branch").equalsIgnoreCase("gvmh001")) {
                    LedgerMaster ledgerMaster = new LedgerMaster();
                    ledgerMaster.setLedgerCode("gvmh001");
                    ledgerMaster.setLedgerName("genivis");
                    BalancingMethod balancingMethod = balancingMethodRepository.findByIdAndStatus(1L, true);
                    ledgerMaster.setBalancingMethod(balancingMethod);
                    State state = stateRepository.findById(4008L).get();
                    ledgerMaster.setState(state);
                    ledgerMaster.setSlugName("sundry_creditors");
                    ledgerMaster.setUnderPrefix("PG#5");
                    ledgerMaster.setUniqueCode("SUCR");
                    ledgerMaster.setOpeningBalType("CR");
                    ledgerMaster.setOpeningBal(0.0);
                    ledgerMaster.setTaxable(true);
                    ledgerMaster.setStateCode("27");
                    ledgerMaster.setRegistrationType(1L);
                    ledgerMaster.setStatus(true);
                    ledgerMaster.setIsDeleted(true);
                    ledgerMaster.setIsDefaultLedger(false);
                    ledgerMaster.setIsPrivate(false);
                    Principles principles = principleRepository.findById(6L).get();
                    ledgerMaster.setPrinciples(principles);
                    PrincipleGroups principleGroups = principleGroupsRepository.findByIdAndStatus(5L, true);
                    ledgerMaster.setPrincipleGroups(principleGroups);
                    Foundations foundations = foundationRepository.findByIdAndStatus(2L, true);
                    ledgerMaster.setFoundations(foundations);
                    ledgerMaster.setOutlet(mOutlet);
                    ledgerMaster.setIsCredit(false);
                    ledgerMaster.setIsLicense(false);
                    ledgerMaster.setIsShippingDetails(false);
                    ledgerMaster.setIsDepartment(false);
                    ledgerMaster.setIsBankDetails(true);
                    ledgerMaster.setGstin("27LKMPV5623B1Z6");
                    LedgerMaster mLedger = ledgerMasterRepository.save(ledgerMaster);
                    /****** Bank Details of GV ****/
                    LedgerBankDetails bankDetails = new LedgerBankDetails();
                    bankDetails.setStatus(true);
                    bankDetails.setBankName("SBI");
                    bankDetails.setAccountNo("945897654312879");
                    bankDetails.setIfsc("SBI94");
                    bankDetails.setBankBranch("PUNE");
                    bankDetails.setCreatedBy(mLedger.getCreatedBy());
                    bankDetails.setLedgerMaster(mLedger);
                    ledgerBankDetailsRepository.save(bankDetails);
                }


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
                users.setOutlet(mOutlet);
                users.setCompanyCode(sadmin.getCompanyCode());
                Users newUser = usersRepository.save(users);
                /**** END ****/
                responseObject.setMessage("Company Created successfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());
                if (mOutlet != null) {
                    outletRepository.createDefaultLedgers(null,
                            mOutlet.getId(), mOutlet.getCreatedBy());
                    outletRepository.createLedgersPurSaleAc(null, mOutlet.getId(), mOutlet.getCreatedBy());
                    if (mOutlet.getGstTypeMaster().getId() == 1L) {
                        outletRepository.createDefaultGST(null,
                                mOutlet.getId(), mOutlet.getCreatedBy());
                        outletRepository.createDefaultRegisteredTCS(null,
                                mOutlet.getId(), mOutlet.getCreatedBy());

                    } else {
                        outletRepository.createDefaultGSTUnRegistered(null,
                                mOutlet.getId(), mOutlet.getCreatedBy());
                        outletRepository.createDefaultTCSUnRegistered(null,
                                mOutlet.getId(), mOutlet.getCreatedBy());
                    }
                    /***** Create Default Counter Customer Ledger  *****/
                    outletRepository.createCounterCustomer(mOutlet.getId(), mOutlet.getCreatedBy());
                    /**** Default Tax Master ****/
                    outletRepository.createLedgersTax(null, mOutlet.getId(), mOutlet.getCreatedBy());

                    /**** Create Default Ledger Group of Partner Commision *****/
                    try {
                        outletRepository.createPartnerCommission(mOutlet.getId(), mOutlet.getCreatedBy());
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        outletLogger.error("Error in createPartnerCommission:" + exceptionAsString);
                    }
                    /***** Create default Ledger Group of Provision Group *****/
                    try {
                        outletRepository.createDefaultProvisionGroup(mOutlet.getId(), mOutlet.getCreatedBy());
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        outletLogger.error("Error in createDefaultProvisionGroup:" + exceptionAsString);
                    }

                    /***** Create default Incentive Ledger of SH,RH,ZH,DH *****/
                    try {
                        outletRepository.createDefaultIncentiveLedger("SH Incentive", "sh_inc", mOutlet.getId(),
                                mOutlet.getCreatedBy());
                        outletRepository.createDefaultIncentiveLedger("RH Incentive", "rh_inc", mOutlet.getId(),
                                mOutlet.getCreatedBy());
                        outletRepository.createDefaultIncentiveLedger("ZH Incentive", "zh_inc", mOutlet.getId(),
                                mOutlet.getCreatedBy());
                        outletRepository.createDefaultIncentiveLedger("DH Incentive", "dh_inc", mOutlet.getId(),
                                mOutlet.getCreatedBy());
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        outletLogger.error("Error in createDefaultIncentiveLedger:" + exceptionAsString);
                    }
                    /***** Create default LTDS Commission Ledger under of Provision Head *****/
                    try {
                        AssociateGroups associateGroups = associateGroupsRepository.
                                findByAssociatesNameIgnoreCaseAndStatus("Provisions", true);
                        if (associateGroups != null) {
                            outletRepository.createDefaultTdsCommisionLedger("TDS 194H", "tds194h",
                                    mOutlet.getId(), mOutlet.getCreatedBy(), associateGroups.getId());
                        }
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        outletLogger.error("Error in createDefaultProvisionGroup:" + exceptionAsString);
                    }
                    /**** Create Default Consumer Customer Ledger Group ****/
                    try {
                        outletRepository.createConsumerCustLedgerGroup(mOutlet.getId(), mOutlet.getCreatedBy());
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        outletLogger.error("Error in createConsumerCustLedgerGroup:" + exceptionAsString);
                    }
                }
                String strJson = request.getParameter("userControlData");
                JsonArray settingArray = new JsonParser().parse(strJson).getAsJsonArray();
                for (JsonElement jsonElement : settingArray) {
                    JsonObject object = jsonElement.getAsJsonObject();
                    AppConfig appConfig = new AppConfig();
                    SystemConfigParameter systemConfigParameter =
                            systemConfigParameterRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
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
                    responseObject.setResponseObject(mOutlet.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                outletLogger.error("createOutlet -> failed to createOutlet" + e);
                responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseObject.setMessage("failed to create Outlet-> " + e.getMessage());
                e.printStackTrace();
                System.out.println("Exception:" + e.getMessage());
            }
        }
        return responseObject;
    }

    private Boolean validateDuplicateCompany(String companyName) {
        Outlet mOutlet = outletRepository.findByCompanyNameIgnoreCaseAndStatus(companyName, true);
        Boolean flag;
        if (mOutlet != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    /* get Outlet by id */
    public JsonObject getOutletById(HttpServletRequest request) {
        Outlet mOutlet = outletRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);

        JsonObject response = new JsonObject();
        JsonObject res = new JsonObject();
        if (mOutlet != null) {
            response.addProperty("companyId", mOutlet.getId());
            response.addProperty("companyCode", mOutlet.getCompanyCode());
            response.addProperty("companyName", mOutlet.getCompanyName());
            response.addProperty("sameAsAddress", mOutlet.getIsSameAddress());
            response.addProperty("mobileNumber", mOutlet.getMobileNumber()!=null?mOutlet.getMobileNumber().toString():"");
            response.addProperty("whatsappNumber", mOutlet.getWhatsappNumber()!=null?mOutlet.getWhatsappNumber().toString():"");
            response.addProperty("multiBranch", mOutlet.getIsMultiBranch());
            response.addProperty("email", mOutlet.getEmail());
            response.addProperty("website", mOutlet.getWebsite());
            response.addProperty("natureOfBusiness", mOutlet.getBusinessType());
            response.addProperty("tradeOfBusiness", mOutlet.getBusinessTrade());
            response.addProperty("gstApplicable", mOutlet.getGstApplicable());
            if (mOutlet.getGstApplicable()) {
                response.addProperty("gstType", mOutlet.getGstTypeMaster() != null ? mOutlet.getGstTypeMaster().getId().toString() : "");
                response.addProperty("gstIn", mOutlet.getGstNumber()!=null?mOutlet.getGstNumber():"");
                response.addProperty("gstTypeName", mOutlet.getGstTypeMaster().getGstType());
                response.addProperty("gstApplicableDate",
                        mOutlet.getGstApplicableDate() != null ? mOutlet.getGstApplicableDate().toString() : "");
            }
            response.addProperty("currency", mOutlet.getCurrency());
            response.addProperty("licenseNo", mOutlet.getLicenseNo()!=null?mOutlet.getLicenseNo():"");
            response.addProperty("licenseExpiryDate", mOutlet.getLicenseExpiry() != null ?
                    mOutlet.getLicenseExpiry().toString() : "");
            response.addProperty("foodLicenseNo", mOutlet.getFoodLicenseNo()!=null?mOutlet.getFoodLicenseNo():"");
            response.addProperty("foodLicenseExpiryDate", mOutlet.getFoodLicenseExpiry() != null ?
                    mOutlet.getFoodLicenseExpiry().toString() : "");
            response.addProperty("manufacturingLicenseNo", mOutlet.getManufacturingLicenseNo()!=null?mOutlet.getManufacturingLicenseNo():"");
            response.addProperty("manufacturingLicenseExpiry", mOutlet.getManufacturingLicenseExpiry() != null ?
                    mOutlet.getManufacturingLicenseExpiry().toString() : "");
            response.addProperty("gstTransferDate", mOutlet.getGstTransferDate() != null ?
                    mOutlet.getGstTransferDate().toString() : "");
            response.addProperty("place", mOutlet.getPlace()!=null?mOutlet.getPlace():"");
            response.addProperty("route", mOutlet.getRoute()!=null?mOutlet.getRoute():"");
            response.addProperty("registeredAddress", mOutlet.getRegisteredAddress());
            response.addProperty("corporateAddress", mOutlet.getCorporateAddress());
            response.addProperty("pincode", mOutlet.getPincode());
            response.addProperty("corporatePincode", mOutlet.getCorpPincode());
            response.addProperty("stateId", mOutlet.getState() != null ? mOutlet.getState().getId().toString() : "");
            response.addProperty("stateCode", mOutlet.getStateCode() != null ? mOutlet.getStateCode().toString() : "");
            response.addProperty("corporateStateCode",mOutlet.getStateCode()!=null?mOutlet.getStateCode():"");
            response.addProperty("state", mOutlet.getState() != null ? mOutlet.getState().getName().toUpperCase() : "");
            response.addProperty("corporatestate", mOutlet.getCorporateState() != null ? mOutlet.getCorporateState().toString().toUpperCase() : "");
            response.addProperty("city", mOutlet.getDistrict());
            response.addProperty("corporatecity", mOutlet.getCorporateDistrict());
            response.addProperty("countryId",
                    mOutlet.getCountry() != null ? mOutlet.getCountry().getId().toString() : "");
            response.addProperty("uploadImage", mOutlet.getCompanyLogo() != null ? serverUrl +
                    mOutlet.getCompanyLogo() : "");

            /**** Area of Registered Address ****/
            List<PincodeMaster> pincodeMasters = pincodeMasterRepository.findByPincode(
                    mOutlet.getPincode());
            JsonArray pincodeArray = new JsonArray();
            if (pincodeMasters != null && pincodeMasters.size() > 0) {
                for (PincodeMaster mPinocdes : pincodeMasters) {
                    JsonObject mObject = new JsonObject();
                    mObject.addProperty("area_id", mPinocdes.getId());
                    int lastIndex = 0;
                    int lastIndex1 = 0;
                    String mArea = "";
                    /*if (mPinocdes.getArea().endsWith("B.O"))
                        lastIndex = mPinocdes.getArea().lastIndexOf("B.O");
                    else
                        lastIndex1 = mPinocdes.getArea().lastIndexOf("S.O");
                    if (lastIndex > 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex);
                    }
                    if (lastIndex1 > 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex1);
                    }
                    mObject.addProperty("area_name", mArea.trim());*/

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
            if (mOutlet.getArea().equalsIgnoreCase("")) {
                response.addProperty("area", "");
            } else {
                try {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findByAreaAndDistrictAndPincode(
                            mOutlet.getArea(), mOutlet.getDistrict(), mOutlet.getPincode());
                    response.addProperty("area", pincodeMaster.getId());
                } catch (Exception e) {
                }
            }
            response.add("area_list", pincodeArray);
            /**** Area of Corporate Address ****/
            List<PincodeMaster> corpincodeMasters = pincodeMasterRepository.findByPincode(
                    mOutlet.getCorpPincode());
            JsonArray corporateArray = new JsonArray();
            if (corpincodeMasters != null && corpincodeMasters.size() > 0) {
                for (PincodeMaster mPinocdes : corpincodeMasters) {
                    JsonObject mObject = new JsonObject();
                    mObject.addProperty("corporate_area_id", mPinocdes.getId());
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
                    }
                    mObject.addProperty("corporate_area_name", mArea.trim());*/

                    if (mPinocdes.getArea().endsWith("B.O"))
                        lastIndex = mPinocdes.getArea().lastIndexOf("B.O");
                    else
                        lastIndex = mPinocdes.getArea().indexOf("BO");
                    if (mPinocdes.getArea().endsWith("S.O"))
                        lastIndex1 = mPinocdes.getArea().lastIndexOf("S.O");
                    else
                        lastIndex1 = mPinocdes.getArea().indexOf("SO");
                    if (lastIndex > 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex);
                    }
                    if (lastIndex1 > 0) {
                        mArea = mPinocdes.getArea().substring(0, lastIndex1);
                    }
                    mObject.addProperty("corporate_area_name", mArea.trim());
                    corporateArray.add(mObject);
                }
            }
            if (mOutlet.getCorporateArea().equalsIgnoreCase("")) {
                response.addProperty("corporatearea", "");
            } else {
                try {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findByAreaAndDistrictAndPincode(
                            mOutlet.getCorporateArea(), mOutlet.getCorporateDistrict(), mOutlet.getCorpPincode());
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

    public JsonObject updateOutlet(MultipartHttpServletRequest request) throws ParseException {
        Map<String, String[]> paramMap = request.getParameterMap();
        Outlet mOutlet = outletRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        JsonObject response = new JsonObject();
        try {
            if (mOutlet != null) {
                mOutlet.setCompanyName(request.getParameter("companyName"));
                if (paramMap.containsKey("companyCode")) {
                    mOutlet.setCompanyCode(request.getParameter("companyCode"));
                } else {
                    mOutlet.setCompanyCode("");
                }
                if (paramMap.containsKey("email"))
                    mOutlet.setEmail(request.getParameter("email"));

                if (Boolean.parseBoolean(request.getParameter("gstApplicable"))) {
                    mOutlet.setGstApplicable(true);
                    mOutlet.setGstNumber(request.getParameter("gstIn"));
                    Optional<GstTypeMaster> gstTypeMaster = gstMasterRepository.findById(
                            Long.parseLong(request.getParameter("gstType")));
                    mOutlet.setGstTypeMaster(gstTypeMaster.get());
                  /*  String stateCode = request.getParameter("gstIn").substring(0, 2);
                    mOutlet.setStateCode(stateCode);*/
                    if (paramMap.containsKey("gstApplicableDate")) {
                        LocalDate date = LocalDate.parse(request.getParameter("gstApplicableDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        mOutlet.setGstApplicableDate(date);
                    }
                } else {
                    mOutlet.setGstApplicableDate(null);
                    mOutlet.setGstApplicable(false);
                    //  mOutlet.setStateCode(state.get().getStateCode());
                    Optional<GstTypeMaster> gstTypeMaster = gstMasterRepository.findById(3L);
                    mOutlet.setGstTypeMaster(gstTypeMaster.get());
                }
                if (paramMap.containsKey("mobileNumber"))
                    mOutlet.setMobileNumber(Long.parseLong(request.getParameter("mobileNumber")));
                if (paramMap.containsKey("sameAsAddress"))
                    mOutlet.setIsSameAddress(Boolean.parseBoolean(request.getParameter("sameAsAddress")));
                if (paramMap.containsKey("registeredAddress"))
                    mOutlet.setRegisteredAddress(request.getParameter("registeredAddress"));
                else
                    mOutlet.setRegisteredAddress("");
                if (paramMap.containsKey("corporateAddress"))
                    mOutlet.setCorporateAddress(request.getParameter("corporateAddress"));
                else
                    mOutlet.setCorporateAddress("");
                if (paramMap.containsKey("pincode"))
                    mOutlet.setPincode(request.getParameter("pincode"));
                else mOutlet.setPincode("");
                if (paramMap.containsKey("corporatePincode"))
                    mOutlet.setCorpPincode(request.getParameter("corporatePincode"));
                else mOutlet.setCorpPincode("");
                if (paramMap.containsKey("stateCode")) {
                    mOutlet.setStateCode(request.getParameter("stateCode"));
                    List<State> state = stateRepository.findByStateCode(request.getParameter("stateCode"));
                    if (state != null) {
                        mOutlet.setState(state.get(0));
                    }
                } else mOutlet.setStateCode("");
                if (paramMap.containsKey("corporatestateCode")) {
                    List<State> state = stateRepository.findByStateCode(request.getParameter("corporatestateCode"));
                    if (state != null) {
                        mOutlet.setCorporateState(state.get(0).getName());
                    }
                } else mOutlet.setCorporateState("");
                if (paramMap.containsKey("city"))
                    mOutlet.setDistrict(request.getParameter("city"));
                else mOutlet.setDistrict("");
                if (paramMap.containsKey("corporatecity"))
                    mOutlet.setCorporateDistrict(request.getParameter("corporatecity"));
                else mOutlet.setCorporateDistrict("");

                if (paramMap.containsKey("area")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("area"))).get();
                    mOutlet.setArea(pincodeMaster.getArea());
                } else
                    mOutlet.setArea("");
                if (paramMap.containsKey("corporatearea")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("corporatearea"))).get();
                    mOutlet.setCorporateArea(pincodeMaster.getArea());
                } else
                    mOutlet.setCorporateArea("");
                if (paramMap.containsKey("whatsappNumber"))
                    mOutlet.setWhatsappNumber(Long.parseLong(request.getParameter("whatsappNumber")));
                if (paramMap.containsKey("website"))
                    mOutlet.setWebsite(request.getParameter("website"));
                else
                    mOutlet.setWebsite("");
                if (paramMap.containsKey("currency"))
                    mOutlet.setCurrency(request.getParameter("currency"));
                else
                    mOutlet.setCurrency("");
                if (paramMap.containsKey("countryId")) {
                    Country country = countryRepository.findById(Long.parseLong(request.getParameter("countryId"))).get();
                    if (country != null)
                        mOutlet.setCountry(country);
                }
                Users users = jwtRequestFilter.getUserDataFromToken(
                        request.getHeader("Authorization").substring(7));
                /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                if (paramMap.containsKey("licenseNo")) {
                    mOutlet.setLicenseNo(request.getParameter("licenseNo"));
                } else {
                    mOutlet.setLicenseNo("");
                }
                if (paramMap.containsKey("licenseExpiryDate")) {
                    mOutlet.setLicenseExpiry(LocalDate.parse(request.getParameter("licenseExpiryDate")));
                }
                if (paramMap.containsKey("foodLicenseNo")) {
                    mOutlet.setFoodLicenseNo(request.getParameter("foodLicenseNo"));
                } else {
                    mOutlet.setFoodLicenseNo("");
                }
                if (paramMap.containsKey("foodLicenseExpiryDate")) {
                    mOutlet.setFoodLicenseExpiry(LocalDate.parse(request.getParameter("foodLicenseExpiryDate")));
                }
                if (paramMap.containsKey("manufacturingLicenseNo")) {
                    mOutlet.setManufacturingLicenseNo(request.getParameter("manufacturingLicenseNo"));
                } else {
                    mOutlet.setManufacturingLicenseNo("");
                }
                if (paramMap.containsKey("manufacturingLicenseExpiry")) {
                    mOutlet.setManufacturingLicenseExpiry(LocalDate.parse(request.getParameter("manufacturingLicenseExpiry")));
                }
                if (paramMap.containsKey("gstTransferDate")) {
                    mOutlet.setGstTransferDate(LocalDate.parse(request.getParameter("gstTransferDate")));
                }
                if (paramMap.containsKey("natureOfBusiness"))
                    mOutlet.setBusinessType(request.getParameter("natureOfBusiness"));
                else mOutlet.setBusinessType("");
                if (paramMap.containsKey("tradeOfBusiness"))
                    mOutlet.setBusinessTrade(request.getParameter("tradeOfBusiness"));
                else mOutlet.setBusinessTrade("");
                if (paramMap.containsKey("place"))
                    mOutlet.setPlace(request.getParameter("place"));
                else mOutlet.setPlace("");
                if (paramMap.containsKey("route"))
                    mOutlet.setRoute(request.getParameter("route"));
                else mOutlet.setRoute("");
                if (paramMap.containsKey("stateCode")) {
                    mOutlet.setStateCode(request.getParameter("stateCode"));
                    List<State> state = stateRepository.findByStateCode(request.getParameter("stateCode"));
                    if (state != null) {
                        mOutlet.setState(state.get(0));
                    }
                }
                if (paramMap.containsKey("isMultiBranch"))
                    mOutlet.setIsMultiBranch(Boolean.parseBoolean(request.getParameter("isMultiBranch")));
                /**** Uploading Company LOGO ****/
                if (request.getFile("uploadImage") != null) {
                    MultipartFile image = request.getFile("uploadImage");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        mOutlet.setCompanyLogo(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                Outlet savedOutlet = outletRepository.save(mOutlet);
                /***** Company Admin Update *****/
                response.addProperty("message", "Company updated successfully");
                response.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                response.addProperty("message", "Not found");
                response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            outletLogger.error("updateOutlet -> failed to updateOutlet" + e);
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return response;
    }


    public JsonObject getUserData(List<Users> users) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        if (users.size() > 0) {
            for (Users mUsers : users) {
                JsonObject response = new JsonObject();
                if (mUsers.getOutlet() != null) {
                    response.addProperty("id", mUsers.getOutlet().getId());
                    response.addProperty("companyCode", mUsers.getOutlet().getCompanyCode());
                    response.addProperty("registeredAddress", mUsers.getOutlet().getRegisteredAddress());
                    response.addProperty("corporateAddress", mUsers.getOutlet().getCorporateAddress());
                    response.addProperty("companyName", mUsers.getOutlet().getCompanyName());
                    response.addProperty("email", mUsers.getOutlet().getEmail());
                    response.addProperty("status", mUsers.getStatus());
                    response.addProperty("gstApplicable", mUsers.getOutlet().getGstApplicable());
                    if (mUsers.getOutlet().getGstApplicable()) {
                        response.addProperty("gstType", mUsers.getOutlet().getGstTypeMaster().getId());
                        response.addProperty("gstTypeName", mUsers.getOutlet().getGstTypeMaster().getGstType());
                        response.addProperty("gstApplicableDate", mUsers.getOutlet().getGstApplicableDate() != null ?
                                mUsers.getOutlet().getGstApplicableDate().toString() : "");
                    }
                    response.addProperty("mobile", mUsers.getOutlet().getMobileNumber());
                    response.addProperty("licenseNo", mUsers.getOutlet().getLicenseNo());
                    response.addProperty("licenseExpiry", mUsers.getOutlet().getLicenseExpiry() != null ?
                            mUsers.getOutlet().getLicenseExpiry().toString() : "");
                    response.addProperty("userId", mUsers.getId());
                    response.addProperty("fullName", mUsers.getFullName());
                    result.add(response);
                    res.addProperty("message", "success");
                    res.addProperty("responseStatus", HttpStatus.OK.value());
                    res.add("responseObject", result);
                }
            }
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }

    public JsonArray getCompanyUserData(List<Users> users) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        if (users.size() > 0) {
            for (Users mUsers : users) {
                JsonObject response = new JsonObject();
                if (mUsers.getOutlet() != null) {
                    response.addProperty("id", mUsers.getOutlet().getId());
                    response.addProperty("companyCode", mUsers.getOutlet().getCompanyCode());
                    response.addProperty("registeredAddress", mUsers.getOutlet().getRegisteredAddress());
                    response.addProperty("corporateAddress", mUsers.getOutlet().getCorporateAddress());
                    response.addProperty("companyName", mUsers.getOutlet().getCompanyName());
                    response.addProperty("email", mUsers.getOutlet().getEmail());
                    response.addProperty("status", mUsers.getStatus());
                    response.addProperty("gstApplicable", mUsers.getOutlet().getGstApplicable());
                    if (mUsers.getOutlet().getGstApplicable()) {
                        response.addProperty("gstType", mUsers.getOutlet().getGstTypeMaster().getId());
                        response.addProperty("gstTypeName", mUsers.getOutlet().getGstTypeMaster().getGstType());
                        response.addProperty("gstApplicableDate", mUsers.getOutlet().getGstApplicableDate() != null ?
                                mUsers.getOutlet().getGstApplicableDate().toString() : "");
                    }
                    response.addProperty("mobile", mUsers.getOutlet().getMobileNumber());
                    response.addProperty("licenseNo", mUsers.getOutlet().getLicenseNo());
                    response.addProperty("licenseExpiry", mUsers.getOutlet().getLicenseExpiry() != null ?
                            mUsers.getOutlet().getLicenseExpiry().toString() : "");
                    response.addProperty("userId", mUsers.getId());
                    response.addProperty("fullName", mUsers.getFullName());
                    result.add(response);
                  /*  res.addProperty("message", "success");
                    res.addProperty("responseStatus", HttpStatus.OK.value());
                    res.add("responseObject", result);*/
                }
            }
        } /*else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }*/
        return result;
    }

    public JsonObject getOutletData(List<Outlet> outlets) {
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        if (outlets.size() > 0) {
            for (Outlet mOutlet : outlets) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mOutlet.getId());
                response.addProperty("companyCode", mOutlet.getCompanyCode());
                response.addProperty("registeredAddress", mOutlet.getRegisteredAddress());
                response.addProperty("corporateAddress", mOutlet.getCorporateAddress());
                response.addProperty("companyName", mOutlet.getCompanyName());
                response.addProperty("email", mOutlet.getEmail());
                response.addProperty("status", mOutlet.getStatus());
                response.addProperty("gstApplicable", mOutlet.getGstApplicable());
                if (mOutlet.getGstApplicable()) {
                    response.addProperty("gstType", mOutlet.getGstTypeMaster().getId());
                    response.addProperty("gstTypeName", mOutlet.getGstTypeMaster().getGstType());
                    response.addProperty("gstApplicableDate", mOutlet.getGstApplicableDate() != null ?
                            mOutlet.getGstApplicableDate().toString() : "");
                }
                response.addProperty("mobile", mOutlet.getMobileNumber()!=null?mOutlet.getMobileNumber().toString():"");
                response.addProperty("licenseNo", mOutlet.getLicenseNo());
                response.addProperty("licenseExpiry", mOutlet.getLicenseExpiry() != null ?
                        mOutlet.getLicenseExpiry().toString() : "");
                result.add(response);
                res.addProperty("message", "success");
                res.addProperty("responseStatus", HttpStatus.OK.value());
                res.add("responseObject", result);
            }
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", result);
        }
        return res;
    }


    public JsonObject getGstType() {
        JsonObject res = new JsonObject();
        JsonArray result = new JsonArray();
        List<GstTypeMaster> list = new ArrayList<>();
        list = gstMasterRepository.findAll();
        if (list.size() > 0) {
            for (GstTypeMaster mList : list) {
                if (mList.getId() != 3) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mList.getId());
                    response.addProperty("gstType", mList.getGstType());
                    result.add(response);
                }
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

    public JsonObject getOutletsOfSuperAdmin(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long userId = users.getId();
        List<Outlet> outletList = outletRepository.findByStatus(true);
        JsonObject res = new JsonObject();
        res = getOutletData(outletList);
        return res;
    }

    public Object validateOutlet(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage = new ResponseMessage();
        String companyName = request.getParameter("companyName");
        Outlet outlet = outletRepository.findByCompanyNameIgnoreCaseAndStatus(companyName, true);
        if (outlet != null) {
            responseMessage.setMessage("Duplicate company");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New Ledger");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public JsonObject getOutletByUser(HttpServletRequest request) {
        Outlet mOutlet = outletRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject res = new JsonObject();


        if (mOutlet != null) {
            response.addProperty("companyId", mOutlet.getId());
            response.addProperty("companyCode", mOutlet.getCompanyCode());
            response.addProperty("companyName", mOutlet.getCompanyName());
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

    public JsonObject getPincode(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        List<PincodeMaster> pincodeMaster = pincodeMasterRepository.findByPincode(request.getParameter("pincode"));
        if (pincodeMaster != null && pincodeMaster.size() > 0) {
            for (PincodeMaster mPincode : pincodeMaster) {
                JsonObject response = new JsonObject();
                response.addProperty("id", mPincode.getId());
                response.addProperty("pincode", mPincode.getPincode());
                response.addProperty("district", mPincode.getDistrict());
                response.addProperty("state", mPincode.getState().toUpperCase());
                response.addProperty("stateCode", mPincode.getStateCode());
                int lastIndex = 0;
                int lastIndex1 = 0;
                String mArea = "";
               /* if (mPincode.getArea().endsWith("B.O"))
                    lastIndex = mPincode.getArea().lastIndexOf("B.O");
                else
                    lastIndex = mPincode.getArea().indexOf("BO");
                if (mPincode.getArea().endsWith("S.O"))
                    lastIndex1 = mPincode.getArea().lastIndexOf("S.O");
                else
                    lastIndex1 = mPincode.getArea().indexOf("SO");
                if (lastIndex > 0) {
                    mArea = mPincode.getArea().substring(0, lastIndex);
                }
                if (lastIndex1 > 0) {
                    mArea = mPincode.getArea().substring(0, lastIndex1);
                }*/
                lastIndex = mPincode.getArea().lastIndexOf("B.O");
                if (lastIndex > 0) {
                    mArea = mPincode.getArea().substring(0, lastIndex);
                } else {
                    lastIndex = mPincode.getArea().indexOf("BO");
                    if (lastIndex > 0) {
                        mArea = mPincode.getArea().substring(0, lastIndex);
                    } else {
                        lastIndex = mPincode.getArea().indexOf("S.O");
                        if (lastIndex > 0) {
                            mArea = mPincode.getArea().substring(0, lastIndex);
                        } else {
                            lastIndex = mPincode.getArea().indexOf("SO");
                            if (lastIndex > 0) {
                                mArea = mPincode.getArea().substring(0, lastIndex);
                            }
                        }
                    }
                }
                response.addProperty("area", mArea.trim());
                jsonArray.add(response);
            }
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", jsonArray);
        } else {
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            res.add("responseObject", jsonArray);
        }
        return res;
    }

    public JsonObject companyDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
//        Outlet company = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Outlet company1 = outletRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        try {

            if (company1 != null) {
                company1.setStatus(false);
                outletRepository.save(company1);
                jsonObject.addProperty("message", " deleted successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());

            } else {
                jsonObject.addProperty("message", "not allowed to delete company");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return jsonObject;
    }

    public JsonObject validatePincode(HttpServletRequest request) {
        String pincode = request.getParameter("pincode");
        JsonObject result = new JsonObject();
        List<PincodeMaster> pincodeMaster = pincodeMasterRepository.findByPincode(pincode);
        if (pincodeMaster != null && pincodeMaster.size() > 0) {
            result.addProperty("message", "valid pincode");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        } else {
            result.addProperty("message", pincode + " Invalid pincode");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return result;
    }

    public JsonObject getCompanySuperAdmin(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long userId = users.getId();
        List<Outlet> list = outletRepository.findByStatus(true);
        JsonArray result = new JsonArray();
        JsonObject finalObj = new JsonObject();
        for (Outlet mList : list) {
            JsonObject res = new JsonObject();
            res.addProperty("id", mList.getId());
            res.addProperty("companyName", mList.getCompanyName());
            result.add(res);

        }
        if (result.size() > 0) {
            finalObj.addProperty("message", "success");
            finalObj.addProperty("responseStatus", HttpStatus.OK.value());
            finalObj.add("responseObject", result);
        } else {
            finalObj.addProperty("message", "empty list");
            finalObj.addProperty("responseStatus", HttpStatus.OK.value());
            finalObj.add("responseObject", result);
        }
        return finalObj;
    }

    public Object validateOutletUpdate(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        String companyName = request.getParameter("companyName");
        Long id = Long.parseLong(request.getParameter("id"));
        Outlet outlet = outletRepository.findByCompanyNameIgnoreCaseAndStatus(companyName, true);
        if (outlet != null && id != outlet.getId()) {
            responseMessage.setMessage("Duplicate company");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New company");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }
}
