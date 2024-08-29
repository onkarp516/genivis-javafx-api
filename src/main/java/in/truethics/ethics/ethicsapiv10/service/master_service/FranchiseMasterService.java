package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.PasswordEncoders;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageService;
import in.truethics.ethics.ethicsapiv10.model.appconfig.AppConfig;
import in.truethics.ethics.ethicsapiv10.model.appconfig.SystemConfigParameter;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FranchiseMasterService {
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private PincodeMasterRepository pincodeMasterRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private FranchiseMasterRepository franchiseRepository;
    @Autowired
    private AreaHeadRepository areaHeadRepository;
    @Value("${spring.serversource.url}")
    private String serverUrl;
    @Value("${spring.serversource.frurl}")
    private String serverFrUrl;
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private RegionRepository regionRepository;
    private static final Logger franchiseLogger = LogManager.getLogger(FranchiseMasterService.class);
    @Autowired
    private PasswordEncoders bcryptEncoder;
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    RestTemplate restTemplate;

    public Object createFranchise(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        FranchiseMaster franchise = new FranchiseMaster();
        ResponseMessage responseObject = new ResponseMessage();
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        if (validateDuplicateCompany(request.getParameter("franchiseName"))) {
            responseObject.setMessage("Franchise with this name is already exist");
            responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            try {
                franchise.setFranchiseName(request.getParameter("franchiseName"));
                if (paramMap.containsKey("franchiseCode"))
                    franchise.setFranchiseCode(request.getParameter("franchiseCode"));
                else
                    franchise.setFranchiseCode("");
                if (paramMap.containsKey("applicationName"))
                    franchise.setApplicantName(request.getParameter("applicationName"));
                if (paramMap.containsKey("partnerName"))
                    franchise.setPartnerName(request.getParameter("partnerName"));
                franchise.setDistrictId(Long.valueOf(request.getParameter("districtId")));
                franchise.setRegionalId(Long.valueOf(request.getParameter("rigionalId")));
                franchise.setZoneId(Long.valueOf(request.getParameter("zoneId")));
                franchise.setStateId(Long.valueOf(request.getParameter("stateId")));

                if (paramMap.containsKey("sameAsAddress"))
                    franchise.setIsSameAddress(Boolean.parseBoolean(request.getParameter("sameAsAddress")));
                if (paramMap.containsKey("mobileNumber"))
                    franchise.setMobileNumber(Long.parseLong(request.getParameter("mobileNumber")));
                if (paramMap.containsKey("whatsappNumber"))
                    franchise.setWhatsappNumber(Long.parseLong(request.getParameter("whatsappNumber")));
                if (paramMap.containsKey("email"))
                    franchise.setEmail(request.getParameter("email"));
                else franchise.setEmail("");
                if (paramMap.containsKey("gender"))
                    franchise.setGender(request.getParameter("gender"));
                else
                    franchise.setGender("");
                if (paramMap.containsKey("age"))
                    franchise.setAge(Long.valueOf(request.getParameter("age")));
                else
                    franchise.setAge(Long.valueOf(""));
                if (paramMap.containsKey("education"))
                    franchise.setEducation(request.getParameter("education"));
                else
                    franchise.setEducation("");
                if (paramMap.containsKey("presentOccupation"))
                    franchise.setPresentOccupation(request.getParameter("presentOccupation"));
                else
                    franchise.setPresentOccupation("");
                if (paramMap.containsKey("latitude"))
                    franchise.setLatitude(request.getParameter("latitude"));
                else
                    franchise.setLatitude("");
                if (paramMap.containsKey("longitude"))
                    franchise.setLongitude(request.getParameter("longitude"));
                else
                    franchise.setLongitude("");
                if (paramMap.containsKey("dob"))
                    franchise.setDob(LocalDate.parse(request.getParameter("dob")));

                /**** franchise Address & residencial address******/
                if (paramMap.containsKey("franchiseAddress"))
                    franchise.setFranchiseAddress(request.getParameter("franchiseAddress"));
                else
                    franchise.setFranchiseAddress("");
                if (paramMap.containsKey("residencialAddress"))
                    franchise.setResidencialAddress(request.getParameter("residencialAddress"));
                else
                    franchise.setResidencialAddress("");
                if (paramMap.containsKey("pincode"))
                    franchise.setPincode(request.getParameter("pincode"));
                else franchise.setPincode("");
                if (paramMap.containsKey("residencialPincode"))
                    franchise.setCorpPincode(request.getParameter("residencialPincode"));
                else franchise.setCorpPincode("");

                if (paramMap.containsKey("residencialState"))
                    franchise.setResidencialState(request.getParameter("residencialState"));
                else franchise.setResidencialState("");

                if (paramMap.containsKey("state")) {
                    franchise.setStateCode(request.getParameter("state"));
                    List<State> state = stateRepository.findByStateCode(request.getParameter("state"));
                    if (state != null) {
                        franchise.setState(state.get(0));
                    }
                } else franchise.setStateCode("");
                if (paramMap.containsKey("city"))
                    franchise.setDistrict(request.getParameter("city"));
                else franchise.setDistrict("");
                if (paramMap.containsKey("residencialcity"))
                    franchise.setResidencialDistrict(request.getParameter("residencialcity"));
                else franchise.setResidencialDistrict("");

                if (paramMap.containsKey("area")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("area"))).get();
                    franchise.setArea(pincodeMaster.getArea());
                } else
                    franchise.setArea("");
                if (paramMap.containsKey("residencialarea")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("residencialarea"))).get();
                    franchise.setResidencialArea(pincodeMaster.getArea());
                } else
                    franchise.setResidencialArea("");

                if (paramMap.containsKey("BusinessType"))
                    franchise.setBusinessType(request.getParameter("BusinessType"));
                if (paramMap.containsKey("investAmt"))
                    franchise.setInvestAmt(Boolean.valueOf(request.getParameter("investAmt")));

                Users sadmin = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
                franchise.setCreatedBy(sadmin.getId());
                franchise.setStatus(true);
                if (paramMap.containsKey("countryId")) {
                    Country country = countryRepository.findById(Long.parseLong(request.getParameter("countryId"))).get();
                    if (country != null)
                        franchise.setCountry(country);
                }

                if (paramMap.containsKey("bankName"))
                    franchise.setBankName(request.getParameter("bankName"));
                else
                    franchise.setBankName("");
                if (paramMap.containsKey("bankAccountNo"))
                    franchise.setAccountNumber(request.getParameter("bankAccountNo"));
                else
                    franchise.setAccountNumber("");

                if (paramMap.containsKey("bankIfsc"))
                    franchise.setIfsc(request.getParameter("bankIfsc"));
                else
                    franchise.setIfsc("");

                /*** END ****/
                /**** Franchise Funding, for Commision management *****/
                if (paramMap.containsKey("isFunded")) //is_funded : 1 gv is funding to FR and dont allow JV Entry,
                    // 0 Means gv is not funding to fR and allow JV Entry
                    franchise.setIsFunded(Boolean.parseBoolean(request.getParameter("isFunded")));
                else {
                    franchise.setIsFunded(false);
                }
                if (franchise.getIsFunded() && paramMap.containsKey("fundAmt") && !request.getParameter("fundAmt").isEmpty())
                    franchise.setFundAmt(Double.parseDouble(request.getParameter("fundAmt")));
                else
                    franchise.setFundAmt(0.00);
                FranchiseMaster savedFranchise = franchiseRepository.save(franchise);
                if (savedFranchise != null) {
                    try {
                        JsonObject franchiseObject = new JsonObject();
                        franchiseObject.addProperty("fullName", savedFranchise.getFranchiseName());
                        franchiseObject.addProperty("mobileNumber", savedFranchise.getMobileNumber());
                        franchiseObject.addProperty("email", savedFranchise.getEmail());
                        franchiseObject.addProperty("gender", savedFranchise.getGender());
                        franchiseObject.addProperty("usercode", savedFranchise.getFranchiseCode());
                        franchiseObject.addProperty("userRole", "SADMIN");
                        franchiseObject.addProperty("address", savedFranchise.getFranchiseAddress());
                        franchiseObject.addProperty("password", "1234");
                        franchiseObject.addProperty("companyCode", savedFranchise.getFranchiseCode());
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("branch", savedFranchise.getFranchiseCode());
                        headers.add("Authorization", request.getHeader("Authorization"));
                        HttpEntity<String> entity = new HttpEntity<String>(franchiseObject.toString(), headers);

                        String response = restTemplate.exchange(
                                serverFrUrl + "/saveFranchiseSuperadmin", HttpMethod.POST, entity, String.class).getBody();
                        System.out.println("API Response => " + response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                responseObject.setMessage("Franchise created successfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());

            } catch (Exception e) {
                e.printStackTrace();
                franchiseLogger.error("createFranchise -> failed to createFranchise" + e);
                responseObject.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseObject.setMessage("failed to create Franchise-> " + e.getMessage());
                e.printStackTrace();
                System.out.println("Exception:" + e.getMessage());
            }
        }
        return responseObject;
    }

    private Boolean validateDuplicateCompany(String franchiseName) {
        FranchiseMaster franchiseMaster = franchiseRepository.findByFranchiseNameIgnoreCaseAndStatus(franchiseName, true);
        Boolean flag;
        if (franchiseMaster != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    public JsonObject getFranchiseById(HttpServletRequest request) {
        FranchiseMaster franchiseMaster = franchiseRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        JsonObject response = new JsonObject();
        JsonObject res = new JsonObject();
        if (franchiseMaster != null) {
            response.addProperty("franchiseId", franchiseMaster.getId());
            response.addProperty("franchiseCode", franchiseMaster.getFranchiseCode());
            response.addProperty("franchiseName", franchiseMaster.getFranchiseName());
            response.addProperty("applicationName", franchiseMaster.getApplicantName());
            response.addProperty("partnerName", franchiseMaster.getPartnerName() != null ?franchiseMaster.getPartnerName():"");
            response.addProperty("sameAsAddress", franchiseMaster.getIsSameAddress());
            response.addProperty("mobileNumber", franchiseMaster.getMobileNumber());
            if(franchiseMaster.getWhatsappNumber()!=null) {
                response.addProperty("whatsappNumber", franchiseMaster.getWhatsappNumber());
            }
            else {
             response.addProperty("whatsappNumber","");
            }
            response.addProperty("email", franchiseMaster.getEmail());
            response.addProperty("businessType", franchiseMaster.getBusinessType());
            response.addProperty("investAmt", franchiseMaster.getInvestAmt());
            response.addProperty("age", franchiseMaster.getAge() != null ? franchiseMaster.getAge() : 0);
            response.addProperty("dob", franchiseMaster.getDob() != null ? franchiseMaster.getDob().toString() : "");
            response.addProperty("education", franchiseMaster.getEducation());
            response.addProperty("presentoccupation", franchiseMaster.getPresentOccupation() != null ? franchiseMaster.getPresentOccupation() : "");
            response.addProperty("latitude", franchiseMaster.getLatitude() != null ? franchiseMaster.getLatitude() : "");
            response.addProperty("longitude", franchiseMaster.getLongitude() != null ? franchiseMaster.getLongitude() : "");
            response.addProperty("gender", franchiseMaster.getGender() != null ? franchiseMaster.getGender() : "");
            response.addProperty("bankName", franchiseMaster.getBankName() != null ? franchiseMaster.getBankName() : "");
            response.addProperty("accountNum", franchiseMaster.getAccountNumber() != null ? franchiseMaster.getAccountNumber() : "");
            response.addProperty("ifsc", franchiseMaster.getIfsc() != null ? franchiseMaster.getIfsc() : "");
            response.addProperty("address",franchiseMaster.getFranchiseAddress()!=null?franchiseMaster.getFranchiseAddress():"");
            response.addProperty("residencialAddress", franchiseMaster.getResidencialAddress());
            response.addProperty("franchiseAddress", franchiseMaster.getFranchiseAddress());
            response.addProperty("pincode", franchiseMaster.getPincode());
            response.addProperty("residencialPincode", franchiseMaster.getCorpPincode());
            response.addProperty("isFunded",
                    franchiseMaster.getIsFunded() != null ? franchiseMaster.getIsFunded() : false);
            response.addProperty("fundAmt", franchiseMaster.getFundAmt() != null ? franchiseMaster.getFundAmt() : 0.00);
            AreaHead areaHead = areaHeadRepository.findByIdAndStatus(franchiseMaster.getStateId(), true);
            if (areaHead != null) {
                JsonObject stateObject = new JsonObject();
                stateObject.addProperty("stateId", areaHead.getId());
                stateObject.addProperty("stateName", areaHead.getFirstName() + " " + areaHead.getLastName());
                response.add("stateHead", stateObject);
            }
            AreaHead areaHead1 = areaHeadRepository.findByIdAndStatus(franchiseMaster.getZoneId(), true);
            if (areaHead1 != null) {
                JsonObject zoneObject = new JsonObject();
                zoneObject.addProperty("zoneId", areaHead1.getId());
                zoneObject.addProperty("zoneName", areaHead1.getFirstName() + " " + areaHead1.getLastName());
                response.add("zone", zoneObject);
            }
            AreaHead areaHead2 = areaHeadRepository.findByIdAndStatus(franchiseMaster.getRegionalId(), true);
            if (areaHead2 != null) {
                JsonObject regionalObject = new JsonObject();
                regionalObject.addProperty("regionalId", areaHead2.getId());
                regionalObject.addProperty("regionalName", areaHead2.getFirstName() + " " + areaHead2.getLastName());
                response.add("regional", regionalObject);
            }
            AreaHead areaHead3 = areaHeadRepository.findByIdAndStatus(franchiseMaster.getDistrictId(), true);
            if (areaHead3 != null) {
                JsonObject districtObject = new JsonObject();
                districtObject.addProperty("districtId", areaHead3.getId());
                districtObject.addProperty("districtName", areaHead3.getFirstName() + " " + areaHead3.getLastName());
                response.add("district", districtObject);
            }
            response.addProperty("residencialstate", franchiseMaster.getResidencialState() != null ? franchiseMaster.getResidencialState().toString().toUpperCase() : "");
            response.addProperty("state", franchiseMaster.getState() != null ? franchiseMaster.getState().getName() : "");
            response.addProperty("franchisestateId", franchiseMaster.getState() != null ?franchiseMaster.getState().getId().toString():"");

            State state = stateRepository.stateCodefind(Long.valueOf(franchiseMaster.getResidencialState()));
            if (state != null)
                response.addProperty("residencialStateName", state.getName());
            else
                response.addProperty("residencialStateName", "");

            response.addProperty("city", franchiseMaster.getDistrict());
            response.addProperty("residencialcity", franchiseMaster.getResidencialDistrict());
            response.addProperty("countryId",
                    franchiseMaster.getCountry() != null ? franchiseMaster.getCountry().getId().toString() : "");
            /**** Area of Registered Address ****/
            List<PincodeMaster> pincodeMasters = pincodeMasterRepository.findByPincode(
                    franchiseMaster.getPincode());
            JsonArray pincodeArray = new JsonArray();
            if (pincodeMasters != null && pincodeMasters.size() > 0) {
                for (PincodeMaster mPinocdes : pincodeMasters) {
                    JsonObject mObject = new JsonObject();
                    mObject.addProperty("area_id", mPinocdes.getId());
                    int lastIndex = 0;
                    int lastIndex1 = 0;
                    String mArea = "";


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
            if (franchiseMaster.getArea().equalsIgnoreCase("")) {
                response.addProperty("area", "");
            } else {
                try {

                    PincodeMaster pincodeMaster = pincodeMasterRepository.findByAreaAndDistrictAndPincode(
                            franchiseMaster.getArea(), franchiseMaster.getDistrict(), franchiseMaster.getPincode());
                    response.addProperty("areaId", pincodeMaster.getId());
                    response.addProperty("areaName", pincodeMaster.getArea());

                } catch (Exception e) {
                }
            }
            response.add("area_list", pincodeArray);
            /**** Area of Corporate Address ****/
            List<PincodeMaster> corpincodeMasters = pincodeMasterRepository.findByPincode(
                    franchiseMaster.getCorpPincode());
            JsonArray corporateArray = new JsonArray();
            if (corpincodeMasters != null && corpincodeMasters.size() > 0) {
                for (PincodeMaster mPinocdes : corpincodeMasters) {
                    JsonObject mObject = new JsonObject();
                    mObject.addProperty("residencial_area_id", mPinocdes.getId());
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
            if (franchiseMaster.getResidencialArea().equalsIgnoreCase("")) {
                response.addProperty("residencialArea", "");
            } else {
                try {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findByAreaAndDistrictAndPincode(
                            franchiseMaster.getResidencialArea(), franchiseMaster.getResidencialDistrict(), franchiseMaster.getCorpPincode());
                    response.addProperty("residencialAreaId", pincodeMaster.getId());
                    response.addProperty("residencialArea", pincodeMaster.getArea());
                } catch (Exception e) {
                }
            }
            response.add("residencial_area_list", corporateArray);
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

    public JsonObject updateFranchise(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        FranchiseMaster franchiseMaster = franchiseRepository.findByIdAndStatus(Long.parseLong(
                request.getParameter("id")), true);
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        JsonObject response = new JsonObject();
        try {
            if (franchiseMaster != null) {
                franchiseMaster.setFranchiseName(request.getParameter("franchiseName"));
                if (paramMap.containsKey("franchiseCode")) {
                    franchiseMaster.setFranchiseCode(request.getParameter("franchiseCode"));
                } else {
                    franchiseMaster.setFranchiseCode("");
                }
                if (paramMap.containsKey("email"))
                    franchiseMaster.setEmail(request.getParameter("email"));


                if (paramMap.containsKey("mobileNumber"))
                    franchiseMaster.setMobileNumber(Long.parseLong(request.getParameter("mobileNumber")));
                if (paramMap.containsKey("sameAsAddress"))
                    franchiseMaster.setIsSameAddress(Boolean.parseBoolean(request.getParameter("sameAsAddress")));
                if (paramMap.containsKey("residencialAddress"))
                    franchiseMaster.setResidencialAddress(request.getParameter("residencialAddress"));
                else
                    franchiseMaster.setResidencialAddress("");
                if (paramMap.containsKey("franchiseAddress"))
                    franchiseMaster.setFranchiseAddress(request.getParameter("franchiseAddress"));
                else
                    franchiseMaster.setFranchiseAddress("");
                if (paramMap.containsKey("pincode"))
                    franchiseMaster.setPincode(request.getParameter("pincode"));
                else franchiseMaster.setPincode("");
                if (paramMap.containsKey("residencialPincode"))
                    franchiseMaster.setCorpPincode(request.getParameter("residencialPincode"));
                else franchiseMaster.setCorpPincode("");
                if (paramMap.containsKey("stateCode")) {
                    franchiseMaster.setStateCode(request.getParameter("stateCode"));
                    List<State> state = stateRepository.findByStateCode(request.getParameter("stateCode"));
                    if (state != null) {
                        franchiseMaster.setState(state.get(0));
                    }
                } else franchiseMaster.setStateCode("");
                if (paramMap.containsKey("residencialState")) {
//                    List<State> state = stateRepository.findByStateCode(request.getParameter("residencialState"));
//                    if (state != null) {
//                        franchiseMaster.setResidencialState(state.get(0).ge());
//                    }
                    franchiseMaster.setResidencialState(request.getParameter("residencialState"));
                } else franchiseMaster.setResidencialState("");
                if (paramMap.containsKey("city"))
                    franchiseMaster.setDistrict(request.getParameter("city"));
                else franchiseMaster.setDistrict("");
                if (paramMap.containsKey("residencialcity"))
                    franchiseMaster.setResidencialDistrict(request.getParameter("residencialcity"));
                else franchiseMaster.setResidencialDistrict("");

                if (paramMap.containsKey("area")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("area"))).get();
                    franchiseMaster.setArea(pincodeMaster.getArea());
                } else
                    franchiseMaster.setArea("");
                if (paramMap.containsKey("residencialarea")) {
                    PincodeMaster pincodeMaster = pincodeMasterRepository.findById(
                            Long.parseLong(request.getParameter("residencialarea"))).get();
                    franchiseMaster.setResidencialArea(pincodeMaster.getArea());
                } else
                    franchiseMaster.setResidencialArea("");
                if (paramMap.containsKey("whatsappNumber"))
                    franchiseMaster.setWhatsappNumber(Long.parseLong(request.getParameter("whatsappNumber")));
                if (paramMap.containsKey("countryId")) {
                    Country country = countryRepository.findById(Long.parseLong(request.getParameter("countryId"))).get();
                    if (country != null)
                        franchiseMaster.setCountry(country);
                }
                Users users = jwtRequestFilter.getUserDataFromToken(
                        request.getHeader("Authorization").substring(7));
                /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/

                if (paramMap.containsKey("BusinessType"))
                    franchiseMaster.setBusinessType(request.getParameter("BusinessType"));
                else franchiseMaster.setBusinessType("");
                if (paramMap.containsKey("applicantName"))
                    franchiseMaster.setBusinessTrade(request.getParameter("applicantName"));
                else franchiseMaster.setBusinessTrade("");
                if (paramMap.containsKey("partnerName"))
                    franchiseMaster.setPartnerName(request.getParameter("partnerName"));
                else franchiseMaster.setPartnerName("");
                if (paramMap.containsKey("investAmt"))
                    franchiseMaster.setInvestAmt(Boolean.valueOf(request.getParameter("investAmt")));
                else franchiseMaster.setInvestAmt(Boolean.valueOf(""));

                if (paramMap.containsKey("age"))
                    franchiseMaster.setAge(Long.valueOf(request.getParameter("age")));
                else franchiseMaster.setAge(Long.valueOf(""));
                if (paramMap.containsKey("presentOccupation"))
                    franchiseMaster.setPresentOccupation(request.getParameter("presentOccupation"));
                else franchiseMaster.setPresentOccupation("");
                if (paramMap.containsKey("latitude"))
                    franchiseMaster.setLatitude(request.getParameter("latitude"));
                else
                    franchiseMaster.setLatitude("");
                if (paramMap.containsKey("longitude"))
                    franchiseMaster.setLongitude(request.getParameter("longitude"));
                else
                    franchiseMaster.setLongitude("");

                if (paramMap.containsKey("education"))
                    franchiseMaster.setEducation(request.getParameter("education"));
                else franchiseMaster.setPresentOccupation("");
                franchiseMaster.setGender(request.getParameter("gender"));
                if(!request.getParameter("districtId").isEmpty())
                franchiseMaster.setDistrictId(Long.valueOf(request.getParameter("districtId")));
                if(!request.getParameter("rigionalId").isEmpty())
                franchiseMaster.setRegionalId(Long.valueOf(request.getParameter("rigionalId")));
                if(!request.getParameter("zoneId").isEmpty())
                franchiseMaster.setZoneId(Long.valueOf(request.getParameter("zoneId")));
                if(!request.getParameter("stateId").isEmpty())
                franchiseMaster.setStateId(Long.valueOf(request.getParameter("stateId")));
                if (paramMap.containsKey("isFunded")) //is_funded : 1 gv is funding to FR and dont allow JV Entry,
                    // 0 Means gv is not funding to fR and allow JV Entry
                    franchiseMaster.setIsFunded(Boolean.parseBoolean(request.getParameter("isFunded")));
                if (paramMap.containsKey("fundAmt"))
                    franchiseMaster.setFundAmt(Double.parseDouble(request.getParameter("fundAmt")));
                franchiseRepository.save(franchiseMaster);

                /***** Company Admin Update *****/
                response.addProperty("message", "Franchise Updated Successfully");
                response.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                response.addProperty("message", "Not found");
                response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            franchiseLogger.error("updatefranchise -> failed to updatefranchise" + e);
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return response;
    }

    public JsonObject getAllFranchise(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        Long outletId = users.getOutlet().getId();
        List<FranchiseMaster> list = new ArrayList<>();

        list = franchiseRepository.findByStatus(true);

        if (list.size() > 0) {
            for (FranchiseMaster franchiseMaster : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", franchiseMaster.getId());
                response.addProperty("franchiseName", franchiseMaster.getFranchiseName());
                response.addProperty("franchiseCode", franchiseMaster.getFranchiseCode());
                response.addProperty("franchiseAddress", franchiseMaster.getFranchiseAddress());
                response.addProperty("franchisePincode", franchiseMaster.getPincode());
                response.addProperty("mobileNum", franchiseMaster.getMobileNumber());
                response.addProperty("isFunded",
                        franchiseMaster.getIsFunded() != null ? franchiseMaster.getIsFunded() : false);
                response.addProperty("fundAmt", franchiseMaster.getFundAmt() != null ? franchiseMaster.getFundAmt() : 0.00);
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


  /*  public String saveFranchiseSuperadmin(Map<String, String> jsonRequest, HttpServletRequest request) {
        JsonObject response = new JsonObject();

        Users users = new Users();
        try {
            if (jsonRequest.containsKey("mobileNumber"))
                users.setMobileNumber(Long.valueOf(jsonRequest.get("mobileNumber")));

            users.setFullName(jsonRequest.get("fullName"));
            if (jsonRequest.containsKey("email")) users.setEmail(jsonRequest.get("email"));
            else users.setEmail("");
            users.setGender(jsonRequest.get("gender"));
            users.setUsercode(jsonRequest.get("usercode"));
            users.setUsername(jsonRequest.get("usercode"));
            users.setUserRole(jsonRequest.get("userRole"));
            users.setCompanyCode(jsonRequest.get("companyCode"));
            if (jsonRequest.containsKey("address")) users.setAddress(jsonRequest.get("address"));
            else users.setAddress("");
            users.setStatus(true);
            users.setPassword(bcryptEncoder.passwordEncoderNew().encode(jsonRequest.get("password")));
            users.setPlain_password(jsonRequest.get("password"));
            users.setIsSuperAdmin(true);
            users.setPermissions("all");

            userRepository.save(users);
            response.addProperty("Message", "Franchise Super admin created successfully");
            response.addProperty("ResponseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            franchiseLogger.error("Exception in saveFranchiseSuperadmin:" + e.getMessage());
            response.addProperty("Message", "Internal Server Error");
            response.addProperty("ResponseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            e.printStackTrace();
        }
        return response.toString();
    }
*/
    public JsonObject getTotalFranchise(HttpServletRequest request) {
        AreaHead areaHead = jwtRequestFilter.getAreadHeadDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<FranchiseMaster> list = new ArrayList<>();
        if (areaHead.getAreaRole().equals("state")) {
            list = franchiseRepository.findByStateIdAndStatus(areaHead.getId(), true);
        } else if (areaHead.getAreaRole().equals("zonal")) {
            list = franchiseRepository.findByZoneIdAndStatus(areaHead.getId(), true);
        } else if (areaHead.getAreaRole().equals("region")) {
            list = franchiseRepository.findByRegionalIdAndStatus(areaHead.getId(), true);
        } else if (areaHead.getAreaRole().equals("district")) {
            list = franchiseRepository.findByDistrictIdAndStatus(areaHead.getId(), true);
        }
        if (list.size() > 0) {
            for (FranchiseMaster franchiseMaster : list) {
                JsonObject response = new JsonObject();
                response.addProperty("id", franchiseMaster.getId());
                response.addProperty("franchiseName", franchiseMaster.getFranchiseName());
                response.addProperty("franchiseCode", franchiseMaster.getFranchiseCode());
                response.addProperty("franchiseAddress", franchiseMaster.getFranchiseAddress());
                response.addProperty("franchisePincode", franchiseMaster.getPincode());
                response.addProperty("mobileNum", franchiseMaster.getMobileNumber());
                response.addProperty("email", franchiseMaster.getEmail());
                response.addProperty("regDate", franchiseMaster.getCreatedAt().toString());
                response.addProperty("lattitude", 18.5284);
                response.addProperty("longitude", 73.8739);
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

    public JsonObject validateFranchiseCode(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        FranchiseMaster franchiseMaster = null;
        franchiseMaster = franchiseRepository.findByFranchiseCodeAndStatus(request.getParameter("franchiseCode"), true);

        JsonObject result = new JsonObject();
        if (franchiseMaster != null) {
            result.addProperty("message", "Duplicate Franchise Code");
            result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            result.addProperty("message", "New Franchise Code");
            result.addProperty("responseStatus", HttpStatus.OK.value());
        }
        return result;
    }

    public Object validateFranchiseUpdate(HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        String franchiseCode = request.getParameter("franchiseCode");
        Long id = Long.parseLong(request.getParameter("id"));
        FranchiseMaster franchise = franchiseRepository.findByFranchiseCodeIgnoreCaseAndStatus(franchiseCode, true);
        if (franchise != null && id != franchise.getId()) {
            responseMessage.setMessage("Duplicate Franchise Code");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New Franchise Code");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

}
