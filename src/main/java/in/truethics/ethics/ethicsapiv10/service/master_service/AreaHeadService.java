package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.PasswordEncoders;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageService;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.appconfig.AppConfigRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerMasterRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.report_repository.SystemConfigParameterRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.AreaheadCommissionRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceDetailsUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesInvoiceRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.Constants;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AreaHeadService {

    @Autowired
    private AreaHeadRepository areaHeadRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private DistrictRepository districtRepository;

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

    @Autowired
    private AreaMasterRepository areaMasterRepository;

    private static final Logger areaHeadLogger = LogManager.getLogger(AreaHeadService.class);

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
    private ZoneRepository zoneRepository;
    @Autowired
    private FranchiseMasterRepository franchiseMasterRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TranxSalesInvoiceRepository tranxSalesInvoiceRepository;
    @Autowired
    private CommissionMasterRepository commissionMasterRepository;
    @Autowired
    private AreaheadCommissionRepository areaheadCommissionRepository;
    @Autowired
    private TranxSalesInvoiceDetailsUnitRepository tranxSalesInvoiceDetailsUnitRepository;
    @Autowired
    private BalancingMethodRepository balancingMethodRepository;
    @Autowired
    private PrincipleRepository principleRepository;
    @Autowired
    private PrincipleGroupsRepository principleGroupsRepository;
    @Autowired
    private AssociateGroupsRepository associateGroupsRepository;
    @Autowired
    private OutletRepository outletRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private LedgerMasterRepository ledgerMasterRepository;

    public Object createAreaHead(MultipartHttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        AreaHead areaHead = new AreaHead();
        Zone zoneData = null;
        Region regionData = null;
        District districtData = null;
        ResponseMessage responseObject = new ResponseMessage();
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        String userName = request.getParameter("username");
        String areaRole = request.getParameter("areaRole");
        String stateId = request.getParameter("stateCode");
        String zoneId = paramMap.containsKey("zoneCode") ? request.getParameter("zoneCode") : "";
        String regionId = paramMap.containsKey("regionId") ? request.getParameter("regionId") : "";
        String districtId = paramMap.containsKey("district") ? request.getParameter("district") : "";
        if (validateDuplicateAreaHead(userName, areaRole, stateId, zoneId, regionId, districtId)) {
            responseObject.setMessage("Area Head with the given information is already exist");
            responseObject.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            try {
                if (users.getBranch() != null) areaHead.setBranchId(users.getBranch().getId());
                areaHead.setOutletId(users.getOutlet().getId());
                areaHead.setFirstName(request.getParameter("firstName"));
                areaHead.setMiddleName(request.getParameter("middleName"));
                areaHead.setLastName(request.getParameter("lastName"));
                if (paramMap.containsKey("email")) areaHead.setEmail(request.getParameter("email"));
                else areaHead.setEmail("");
                if (paramMap.containsKey("mobileNumber"))
                    areaHead.setMobileNumber(request.getParameter("mobileNumber"));
                if (paramMap.containsKey("whatsappNumber"))
                    areaHead.setWhatsappNumber(request.getParameter("whatsappNumber"));
                else areaHead.setWhatsappNumber("");
                if (paramMap.containsKey("birthDate"))
                    areaHead.setBirthDate(LocalDate.parse(request.getParameter("birthDate")));
                if (paramMap.containsKey("gender")) areaHead.setGender(request.getParameter("gender"));
                if (paramMap.containsKey("permenantAddress"))
                    areaHead.setPermenantAddress(request.getParameter("permenantAddress"));
                else areaHead.setPermenantAddress("");
                if (paramMap.containsKey("pincode")) areaHead.setPincode(request.getParameter("pincode"));
                else areaHead.setPincode("");
                if (paramMap.containsKey("corporatePincode"))
                    areaHead.setCorpPincode(request.getParameter("corporatePincode"));
                else areaHead.setCorpPincode("");
                if (paramMap.containsKey("city")) areaHead.setCity(request.getParameter("city"));
                else areaHead.setCity("");
                if (paramMap.containsKey("corporatecity"))
                    areaHead.setCorporateCity(request.getParameter("corporatecity"));
                else areaHead.setCorporateCity("");
                if (paramMap.containsKey("area")) {
                    PincodeMaster areaMaster = pincodeMasterRepository.findByIdAndPincode(Long.parseLong(request.getParameter("area")), request.getParameter("pincode"));
                    if (areaMaster != null) {
                        areaHead.setAreaMaster(areaMaster);
                        areaHead.setArea(areaMaster.getArea());
                    }
                }
                if (paramMap.containsKey("corporatearea")) {

                    PincodeMaster areaMaster = pincodeMasterRepository.findByIdAndPincode(Long.parseLong(request.getParameter("corporatearea")), request.getParameter("corporatePincode"));
                    if (areaMaster != null) {
                        areaHead.setCorporateAreaMaster(areaMaster);
                        areaHead.setCorporateArea(areaMaster.getArea());
                    }
                } else areaHead.setCorporateArea("");
                if (paramMap.containsKey("temporaryAddress"))
                    areaHead.setTemporaryAddress(request.getParameter("temporaryAddress"));
                else areaHead.setTemporaryAddress("");
                if (paramMap.containsKey("isSameAddress"))
                    areaHead.setIsSameAddress(Boolean.parseBoolean(request.getParameter("isSameAddress")));
                /**  Document Information **/
                if (paramMap.containsKey("aadharCardNo")) {
                    areaHead.setAadharCardNo(request.getParameter("aadharCardNo"));
                }
                /**** Uploading AadharCardFile ****/
                if (request.getFile("aadharCardFile") != null) {
                    MultipartFile image = request.getFile("aadharCardFile");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        areaHead.setAadharCardFile(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                if (paramMap.containsKey("panCardNo")) {
                    areaHead.setPanCardNo(request.getParameter("panCardNo"));
                }
                /**** Uploading PanCardFile ****/
                if (request.getFile("panCardFile") != null) {
                    MultipartFile image = request.getFile("panCardFile");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        areaHead.setPanCardFile(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                if (paramMap.containsKey("bankAccName")) areaHead.setBankAccName(request.getParameter("bankAccName"));
                if (paramMap.containsKey("bankAccNo")) {
                    areaHead.setBankAccNo(request.getParameter("bankAccNo"));
                }
                if (paramMap.containsKey("bankAccIFSC")) areaHead.setBankAccIFSC(request.getParameter("bankAccIFSC"));
                /**** Uploading BankAccFile ****/
                if (request.getFile("bankAccFile") != null) {
                    MultipartFile image = request.getFile("bankAccFile");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        areaHead.setBankAccFile(File.separator + "uploads" + File.separator + imagePath);
                    }
                }

                /**  Account Information ***/
                if (paramMap.containsKey("areaRole")) areaHead.setAreaRole(request.getParameter("areaRole"));
                if (paramMap.containsKey("stateCode")) {
                    areaHead.setStateCode(request.getParameter("stateCode"));
                    State state = stateRepository.findById(Long.parseLong(request.getParameter("stateCode"))).get();
                    if (state != null) {
                        areaHead.setState(state);
                    }
                } else areaHead.setStateCode("");
                if (paramMap.containsKey("zoneCode")) {
                    zoneData = zoneRepository.findByIdAndStatus(Long.parseLong(request.getParameter("zoneCode")), true);
                    areaHead.setZone(zoneData);

                } else areaHead.setZone(zoneData);
                if (paramMap.containsKey("regionId")) {
                    regionData = regionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("regionId")), true);
                    areaHead.setRegion(regionData);
                } else areaHead.setRegionCode("");

                if (paramMap.containsKey("district")) {
                    districtData = districtRepository.findByIdAndStatus(Long.parseLong(request.getParameter("district")), true);
                    areaHead.setDistrict(districtData);
                } else areaHead.setDistrictCode("");
                /**** Uploading BankAccFile ****/
                if (request.getFile("partnerDeedFile") != null) {
                    MultipartFile image = request.getFile("partnerDeedFile");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        areaHead.setPartnerDeedFile(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                if (paramMap.containsKey("zoneStateHead")) {
                    areaHead.setZoneStateHead(request.getParameter("zoneStateHead"));
                } else areaHead.setZoneStateHead("");
                if (paramMap.containsKey("regionZoneHeadId")) {
                    areaHead.setRegionZoneHeadId(request.getParameter("regionZoneHeadId"));
                } else areaHead.setRegionZoneHeadId("");
                if (paramMap.containsKey("regionStateHeadId")) {
                    areaHead.setRegionStateHeadId(request.getParameter("regionStateHeadId"));
                } else areaHead.setRegionStateHeadId("");
                if (paramMap.containsKey("districtRegionHeadId")) {
                    areaHead.setDistrictRegionHeadId(request.getParameter("districtRegionHeadId"));
                } else areaHead.setDistrictRegionHeadId("");
                if (paramMap.containsKey("districtZoneHeadId")) {
                    areaHead.setDistrictZoneHeadId(request.getParameter("districtZoneHeadId"));
                } else areaHead.setDistrictZoneHeadId("");
                if (paramMap.containsKey("districtStateHeadId")) {
                    areaHead.setDistrictStateHeadId(request.getParameter("districtStateHeadId"));
                } else areaHead.setDistrictStateHeadId("");
                areaHead.setStatus(true);
                if (paramMap.containsKey("countryId")) {
                    Country country = countryRepository.findById(Long.parseLong(request.getParameter("countryId"))).get();
                    if (country != null) areaHead.setCountry(country);
                }
                areaHead.setUsername(request.getParameter("username"));
                areaHead.setPassword(bcryptEncoder.passwordEncoderNew().encode(request.getParameter("password")));
                areaHead.setPlain_password(request.getParameter("password"));
                AreaHead mAreaHead = areaHeadRepository.save(areaHead);
                /***** Create Auto Ledger against Area Head ******/
                createAutoAreaHeadLedger(mAreaHead);
                responseObject.setMessage("Created Successfully");
                responseObject.setResponseStatus(HttpStatus.OK.value());
                responseObject.setResponseObject(mAreaHead.getId());
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                areaHeadLogger.error("Error in createAreaHead :" + exceptionAsString);
            }
        }
        return responseObject;
    }

    private void createAutoAreaHeadLedger(AreaHead mAreaHead) {
        try {
            LedgerMaster ledgerMaster = new LedgerMaster();
            BalancingMethod bMethod = balancingMethodRepository.findByIdAndStatus(2L, true);
            ledgerMaster.setBalancingMethod(bMethod);
            Principles principles = principleRepository.findByIdAndStatus(6L, true);
            Foundations foundations = principles.getFoundations();
            PrincipleGroups groups = principleGroupsRepository.findByIdAndStatus(5L, true);
            /***** Associate Group if available ******/
            AssociateGroups associateGroups = associateGroupsRepository.findByAssociatesNameIgnoreCaseAndStatus("Partner Commission", true);
            if (associateGroups != null) ledgerMaster.setAssociateGroups(associateGroups);
            if (groups != null) {
                ledgerMaster.setPrincipleGroups(groups);
                ledgerMaster.setPrinciples(principles);
                ledgerMaster.setUniqueCode(groups.getUniqueCode());
            } else {
                ledgerMaster.setPrincipleGroups(groups);
                ledgerMaster.setPrinciples(principles);
                ledgerMaster.setUniqueCode(principles.getUniqueCode());
            }

            if (foundations != null) ledgerMaster.setFoundations(foundations);
            ledgerMaster.setIsPrivate(false);
            ledgerMaster.setIsDeleted(true); //isDelete : true means , we can delete this ledger
            // if it is not involved into any tranxs,default value is true
            ledgerMaster.setStatus(true);
            ledgerMaster.setIsDefaultLedger(false);
            Outlet mOutlet = outletRepository.findByIdAndStatus(mAreaHead.getOutletId(), true);
            ledgerMaster.setOutlet(mOutlet);
            if (mAreaHead.getBranchId() != null) {
                Branch branch = branchRepository.findByIdAndStatus(mAreaHead.getBranchId(), true);
                mAreaHead.setBranchId(branch.getId());
            }
            ledgerMaster.setCreatedBy(mAreaHead.getCreatedBy());
            String mName = "";
            if (mAreaHead.getMiddleName() != null && !mAreaHead.getMiddleName().isEmpty())
                mName = mAreaHead.getMiddleName();
            String lName = "";
            if (mAreaHead.getLastName() != null && !mAreaHead.getLastName().isEmpty())
                lName = mAreaHead.getLastName();
            ledgerMaster.setLedgerName(mAreaHead.getFirstName() + " " + mName + " " + lName);
            ledgerMaster.setSlugName("sundry_creditors");
            ledgerMaster.setUnderPrefix("AG#" + associateGroups.getId());
            if (mAreaHead.getAreaRole().equalsIgnoreCase("state"))
                ledgerMaster.setStateHeadId(mAreaHead.getId());
            else if (mAreaHead.getAreaRole().equalsIgnoreCase("zonal"))
                ledgerMaster.setZonalHeadId(mAreaHead.getId());
            else if (mAreaHead.getAreaRole().equalsIgnoreCase("region"))
                ledgerMaster.setRegionalHeadId(mAreaHead.getId());
            else if (mAreaHead.getAreaRole().equalsIgnoreCase("district"))
                ledgerMaster.setDistrictHeadId(mAreaHead.getId());

            ledgerMaster.setMailingName(mAreaHead.getFirstName() + " " + mName + " " + lName);
            ledgerMaster.setRoute("");
            ledgerMaster.setOpeningBalType("CR");
            ledgerMaster.setAddress(mAreaHead.getPermenantAddress());
            List<PincodeMaster> pincodes = pincodeMasterRepository.findByPincode(mAreaHead.getPincode());
            List<State> state = stateRepository.findByStateCode(pincodes.get(0).getStateCode());
            ledgerMaster.setState(state.get(0));
            ledgerMaster.setStateCode(pincodes.get(0).getStateCode());
            ledgerMaster.setPincode(pincodes.get(0).getId());
            ledgerMaster.setEmail(mAreaHead.getEmail());
            ledgerMaster.setMobile(Long.parseLong(mAreaHead.getMobileNumber()));
            ledgerMaster.setWhatsAppno(Long.parseLong(mAreaHead.getWhatsappNumber()));
            ledgerMaster.setTaxable(false);
            GstTypeMaster gstTypeMaster = gstMasterRepository.findById(3L).get();
            ledgerMaster.setRegistrationType(gstTypeMaster.getId());
            ledgerMaster.setPancard("");
            ledgerMaster.setBankName(mAreaHead.getBankAccName());
            ledgerMaster.setAccountNumber(mAreaHead.getBankAccNo());
            ledgerMaster.setIfsc(mAreaHead.getBankAccIFSC());
            ledgerMaster.setBankBranch("");
            ledgerMaster.setColumnR(true);
            ledgerMaster.setOpeningBal(0.00);
            /* pune demo visit changes */
            ledgerMaster.setIsCredit(false);
            ledgerMaster.setCreditDays(0);
            ledgerMaster.setApplicableFrom("");
            /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
            ledgerMaster.setIsLicense(false);
            ledgerMaster.setLicenseExpiry(null);
            ledgerMaster.setIsBankDetails(false);
            ledgerMaster.setIsDepartment(false);
            ledgerMaster.setIsShippingDetails(false);
            LedgerMaster mLedger =ledgerMasterRepository.save(ledgerMaster);
            long indexofLedger = 0;
            indexofLedger = mLedger.getId();
            String ans = Constants.num_hash((int) indexofLedger);
            mLedger.setLedgerCode(ans);
            ledgerMasterRepository.save(mLedger);
            /*** END ****/
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaHeadLogger.error("Exception in creteAutoLedger:" + exceptionAsString);
        }
    }

    public AreaHead findAreaHead(Long id) throws UsernameNotFoundException {
        AreaHead areaHead = areaHeadRepository.findByIdAndStatus(id, true);
        if (areaHead != null) {

        } else {
            throw new UsernameNotFoundException("User not found with username: " + areaHead.getUsername());
        }
        return areaHead;
    }

    public JsonObject getParentHeadByRole(HttpServletRequest request) {
        String role = request.getParameter("role");
        String areaId = request.getParameter("areaId");
        AreaHead regionHead = new AreaHead();
        AreaHead zoneHead = new AreaHead();
        AreaHead stateHead = new AreaHead();

        if (role.equalsIgnoreCase("zonal")) {
            //1. get state head
            Zone zonal = zoneRepository.findByIdAndStatus(Long.parseLong(areaId), true);
            stateHead = areaHeadRepository.findByAreaRoleAndStateIdAndStatus("state", zonal.getState().getId(), true);

        } else if (role.equalsIgnoreCase("region")) {
            //1. get state head
            //2. get zonal head
            Region region = regionRepository.findByIdAndStatus(Long.parseLong(areaId), true);
            if (region.getZone() != null)
                zoneHead = areaHeadRepository.findByAreaRoleAndZoneIdAndStatus("zonal", region.getZone().getId(), true);
            else zoneHead = null;

            if (region.getState() != null)
                stateHead = areaHeadRepository.findByAreaRoleAndStateIdAndStatus("state", region.getState().getId(), true);
            else stateHead = null;


        } else if (role.equalsIgnoreCase("district")) {
            //1. get state head
            //2. get zone head
            //3. get region head

            District district = districtRepository.findByIdAndStatus(Long.parseLong(areaId), true);
            if (district.getRegion() != null)
                regionHead = areaHeadRepository.findByAreaRoleAndRegionIdAndStatus("region", district.getRegion().getId(), true);
            else regionHead = null;

            if (district.getZone() != null)
                zoneHead = areaHeadRepository.findByAreaRoleAndZoneIdAndStatus("zonal", district.getZone().getId(), true);
            else zoneHead = null;

            if (district.getState() != null)
                stateHead = areaHeadRepository.findByAreaRoleAndStateIdAndStatus("state", district.getState().getId(), true);
        }

        JsonArray result = new JsonArray();

        if (stateHead != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("role", "statehead");
            jsonObject.addProperty("name", stateHead.getFirstName() + " " + stateHead.getLastName());
            jsonObject.addProperty("id", stateHead.getId());
            jsonObject.addProperty("stateId", stateHead.getState().getId());

            result.add(jsonObject);
        }

        if (zoneHead != null && zoneHead.getId() != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("role", "zonehead");
            jsonObject.addProperty("name", zoneHead.getFirstName() + " " + zoneHead.getLastName());
            jsonObject.addProperty("id", zoneHead.getId());
            jsonObject.addProperty("zoneId", zoneHead.getZone().getId());
            jsonObject.addProperty("stateId", zoneHead.getState().getId());

            result.add(jsonObject);
        }

        if (regionHead != null && regionHead.getId() != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("role", "regionhead");
            jsonObject.addProperty("name", regionHead.getFirstName() + " " + regionHead.getLastName());
            jsonObject.addProperty("id", regionHead.getId());
            jsonObject.addProperty("regionId", regionHead.getRegion().getId());
            jsonObject.addProperty("zoneId", regionHead.getZone().getId());
            jsonObject.addProperty("stateId", regionHead.getState().getId());
            result.add(jsonObject);
//            result.addProperty("regionhead",regionHead.getFirstName()+" "+stateHead.getLastName());
        }


        JsonObject response = new JsonObject();
        response.addProperty("message", "Data Found");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("result", result);

        return response;
    }

    public JsonObject getParentHeadByDh(HttpServletRequest request) {
        String districtHeadId = request.getParameter("districtHeadId");

        AreaHead districtHead = new AreaHead();
        AreaHead regionHead = new AreaHead();
        AreaHead zoneHead = new AreaHead();
        AreaHead stateHead = new AreaHead();


        //1. get state head
        //2. get zone head
        //3. get region head

        districtHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(districtHeadId), true);
        if (districtHead != null) {

            if (districtHead.getRegion() != null)
                regionHead = areaHeadRepository.findByAreaRoleAndRegionIdAndStatus("region", districtHead.getRegion().getId(), true);
            else regionHead = null;

            if (districtHead.getZone() != null)
                zoneHead = areaHeadRepository.findByAreaRoleAndZoneIdAndStatus("zonal", districtHead.getZone().getId(), true);
            else zoneHead = null;

            if (districtHead.getState() != null)
                stateHead = areaHeadRepository.findByAreaRoleAndStateIdAndStatus("state", districtHead.getState().getId(), true);

        }


        JsonArray result = new JsonArray();

        if (stateHead != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("role", "statehead");
            jsonObject.addProperty("name", stateHead.getFirstName() + " " + stateHead.getLastName());
            jsonObject.addProperty("id", stateHead.getId());
            jsonObject.addProperty("stateId", stateHead.getState()!=null?stateHead.getState().getId().toString():"");

            result.add(jsonObject);
        }

        if (zoneHead != null && zoneHead.getId() != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("role", "zonehead");
            jsonObject.addProperty("name", zoneHead.getFirstName() + " " + zoneHead.getLastName());
            jsonObject.addProperty("id", zoneHead.getId());
            jsonObject.addProperty("zoneId", zoneHead.getZone().getId());
            jsonObject.addProperty("stateId", zoneHead.getState().getId());

            result.add(jsonObject);
        }

        if (regionHead != null && regionHead.getId() != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("role", "regionhead");
            jsonObject.addProperty("name", regionHead.getFirstName() + " " + regionHead.getLastName());
            jsonObject.addProperty("id", regionHead.getId());
            jsonObject.addProperty("regionId", regionHead.getRegion().getId());
            jsonObject.addProperty("zoneId", regionHead.getZone().getId());
            jsonObject.addProperty("stateId", regionHead.getState().getId());
            result.add(jsonObject);
        }


        JsonObject response = new JsonObject();
        response.addProperty("message", "Data Found");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("result", result);

        return response;
    }


    public JsonObject getAllAreaHeads(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<AreaHead> list = new ArrayList<>();
        try {
            list = areaHeadRepository.findByStatus(true);
            if (list.size() > 0) {
                for (AreaHead mAreaHead : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mAreaHead.getId());
                    response.addProperty("firstName", mAreaHead.getFirstName());
                    response.addProperty("middleName", mAreaHead.getMiddleName());
                    response.addProperty("lastName", mAreaHead.getLastName());
                    response.addProperty("fullName", mAreaHead.getFirstName() + " " + mAreaHead.getLastName());
                    response.addProperty("email", mAreaHead.getEmail());
                    response.addProperty("mobileNumber", mAreaHead.getMobileNumber() != null ? mAreaHead.getMobileNumber().toString() : "");
                    response.addProperty("whatsAppNumber", mAreaHead.getWhatsappNumber() != null ? mAreaHead.getWhatsappNumber().toString() : "");
                    response.addProperty("DOB", String.valueOf(mAreaHead.getBirthDate()));
                    response.addProperty("address", mAreaHead.getPermenantAddress());
                    response.addProperty("gender", mAreaHead.getGender());
                    response.addProperty("aadharCard", mAreaHead.getAadharCardNo());
                    response.addProperty("panCard", mAreaHead.getPanCardNo());
                    response.addProperty("areaRole", mAreaHead.getAreaRole());
                    response.addProperty("isActive", mAreaHead.getStatus());
                    List<State> state = stateRepository.findByStateCode(mAreaHead.getStateCode());
                    for (State state1 : state) {
                        response.addProperty("state", state1.getName());
                    }
                    if (mAreaHead.getAreaRole().equalsIgnoreCase("state")) {
                        State state2 = mAreaHead.getState();
//                        JsonObject jsonObject=new JsonObject();
//                        jsonObject.addProperty("value", state2.getId());
//                        jsonObject.addProperty("label", state2.getName());
                        response.addProperty("state", state2.getName());
                    }

                    if (mAreaHead.getAreaRole().equalsIgnoreCase("zonal")) {
                        State state3 = mAreaHead.getState();
                        Zone zone = mAreaHead.getZone();

                        if (state3 != null) {
//                            JsonObject stateObject=new JsonObject();
//                            stateObject.addProperty("value", state3.getId());
//                            stateObject.addProperty("label", state3.getName());
                            response.addProperty("state", state3.getName());
                        }

//                        JsonObject zoneObject=new JsonObject();
//                        zoneObject.addProperty("value", zone.getId());
//                        zoneObject.addProperty("label", zone.getZoneName());
                        response.addProperty("zone", zone.getZoneName());
                    }

                    if (mAreaHead.getAreaRole().equalsIgnoreCase("region")) {
                        State state4 = mAreaHead.getState();
                        Zone zone = mAreaHead.getZone();
                        Region region = mAreaHead.getRegion();

                        if (state4 != null) {
//                            JsonObject stateObject=new JsonObject();
//                            stateObject.addProperty("value", state4.getId());
//                            stateObject.addProperty("label", state4.getName());
                            response.addProperty("state", state4.getName());
                        }


                        if (zone != null) {
//                            JsonObject zoneObject=new JsonObject();
//                            zoneObject.addProperty("value", zone.getId());
//                            zoneObject.addProperty("label", zone.getZoneName());
                            response.addProperty("zone", zone.getZoneName());
                        }

//                        JsonObject rgionObject=new JsonObject();
//                        rgionObject.addProperty("value", region.getId());
//                        rgionObject.addProperty("label", region.getRegionName());
                        response.addProperty("region", region.getRegionName());

                    }

                    if (mAreaHead.getAreaRole().equalsIgnoreCase("district")) {
                        State state5 = mAreaHead.getState();
                        Zone zone = mAreaHead.getZone();
                        Region region = mAreaHead.getRegion();
                        District district = mAreaHead.getDistrict();

                        if (state5 != null) {
//                            JsonObject stateObject=new JsonObject();
//                            stateObject.addProperty("value", state5.getId());
//                            stateObject.addProperty("label", state5.getName());
                            response.addProperty("state", state5.getName());
                        }


                        if (zone != null) {
//                            JsonObject zoneObject=new JsonObject();
//                            zoneObject.addProperty("value", zone.getId());
//                            zoneObject.addProperty("label", zone.getZoneName());
                            response.addProperty("zone", zone.getZoneName());
                        }

                        if (region != null) {
//                            JsonObject rgionObject=new JsonObject();
//                            rgionObject.addProperty("value", region.getId());
//                            rgionObject.addProperty("label", region.getRegionName());
                            response.addProperty("region", region.getRegionName());
                        }

//                        JsonObject districtObject=new JsonObject();
//                        districtObject.addProperty("value", district.getId());
//                        districtObject.addProperty("label", district.getDistrictName());
                        response.addProperty("district", district.getDistrictName());

                    }

//                response.addProperty("state",mAreaHead.getStateCode());
//                response.addProperty("district", mAreaHead.getDistrictCode()!=""?mAreaHead.getDistrictCode():"");
//                response.addProperty("region", mAreaHead.getRegionCode()!=""?mAreaHead.getRegionCode():"");
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
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaHeadLogger.error("Error in getAllAreaHead:" + exceptionAsString);
        }
        return res;
    }

    private Boolean validateDuplicateAreaHead(String username, String areaRole, String stateId, String zoneId, String regionId, String districtId) {
        AreaHead mAreaHead = areaHeadRepository.findByUsernameIgnoreCaseAndStatus(username, true);
        Boolean flag = false;
        if (mAreaHead != null) {
            flag = true;
        } else {

            switch (areaRole) {
                case "state":
                    mAreaHead = null;
                    mAreaHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(stateId), true);
                    if (mAreaHead != null) flag = true;
                    break;
                case "zonal":
                    mAreaHead = null;
                    mAreaHead = areaHeadRepository.findByAreaRoleAndZoneIdAndStatus(areaRole, Long.parseLong(zoneId), true);
                    if (mAreaHead != null) flag = true;
                    break;
                case "region":
                    mAreaHead = null;
                    mAreaHead = areaHeadRepository.findByAreaRoleAndRegionIdAndStatus(areaRole, Long.parseLong(regionId), true);
                    if (mAreaHead != null) flag = true;
                    break;
                case "district":
                    mAreaHead = null;
                    mAreaHead = areaHeadRepository.findByAreaRoleAndDistrictIdAndStatus(areaRole, Long.parseLong(districtId), true);
                    if (mAreaHead != null) flag = true;
                    break;
                default:
                    flag = false;
            }
        }


        return flag;
    }

    public JsonObject updateAreaHead(MultipartHttpServletRequest request) throws ParseException {
        Map<String, String[]> paramMap = request.getParameterMap();
        AreaHead areaHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        JsonObject response = new JsonObject();
        try {
            if (areaHead != null) {
                areaHead.setFirstName(request.getParameter("firstName"));
                areaHead.setMiddleName(request.getParameter("middleName"));
                areaHead.setLastName(request.getParameter("lastName"));
                if (paramMap.containsKey("email")) areaHead.setEmail(request.getParameter("email"));
                else areaHead.setEmail("");
                if (paramMap.containsKey("mobileNumber"))
                    areaHead.setMobileNumber(request.getParameter("mobileNumber"));
                if (paramMap.containsKey("whatsappNumber"))
                    areaHead.setWhatsappNumber(request.getParameter("whatsappNumber"));
                else areaHead.setWhatsappNumber("");
                if (paramMap.containsKey("birthDate"))
                    areaHead.setBirthDate(LocalDate.parse(request.getParameter("birthDate")));
                if (paramMap.containsKey("gender")) areaHead.setGender(request.getParameter("gender"));
                if (paramMap.containsKey("permenantAddress"))
                    areaHead.setPermenantAddress(request.getParameter("permenantAddress"));
                else areaHead.setPermenantAddress("");
                if (paramMap.containsKey("temporaryAddress"))
                    areaHead.setTemporaryAddress(request.getParameter("temporaryAddress"));
                else areaHead.setTemporaryAddress("");
                if (paramMap.containsKey("sameAsAddress"))
                    areaHead.setIsSameAddress(Boolean.parseBoolean(request.getParameter("sameAsAddress")));
                /**  Document Information **/

                if (paramMap.containsKey("pincode")) {
                    areaHead.setPincode(request.getParameter("pincode"));
                }
                if (paramMap.containsKey("corporatePincode")) {
                    areaHead.setCorpPincode(request.getParameter("corporatePincode"));
                }
                if (paramMap.containsKey("aadharCardNo")) {
                    areaHead.setAadharCardNo(request.getParameter("aadharCardNo"));
                }
                if (paramMap.containsKey("bankAccName")) {
                    areaHead.setBankAccName(request.getParameter("bankAccName"));
                }
                if (paramMap.containsKey("bankAccNo")) {
                    areaHead.setBankAccNo(request.getParameter("bankAccNo"));
                }
                if (paramMap.containsKey("bankAccIFSC")) {
                    areaHead.setBankAccIFSC(request.getParameter("bankAccIFSC"));
                }
                if (paramMap.containsKey("area")) {
                    areaHead.setArea(request.getParameter("area"));
                }
                if (paramMap.containsKey("corporatearea")) {
                    areaHead.setCorporateArea(request.getParameter("corporatearea"));
                }

                /**** Uploading AadharCardFile ****/
                if (request.getFile("aadharCardFile") != null) {
                    MultipartFile image = request.getFile("aadharCardFile");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        areaHead.setAadharCardFile(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                if (paramMap.containsKey("panCardNo")) {
                    areaHead.setPanCardNo(request.getParameter("panCardNo"));
                }
                /**** Uploading PanCardFile ****/
                if (request.getFile("panCardFile") != null) {
                    MultipartFile image = request.getFile("panCardFile");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        areaHead.setPanCardFile(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                if (paramMap.containsKey("aadharCardNo")) {
                    areaHead.setAadharCardNo(request.getParameter("aadharCardNo"));
                }
                /**** Uploading BankAccFile ****/
                if (request.getFile("bankAccFile") != null) {
                    MultipartFile image = request.getFile("bankAccFile");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        areaHead.setBankAccFile(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                /**  Account Information ***/
                if (paramMap.containsKey("areaRole")) areaHead.setAreaRole(request.getParameter("areaRole"));
                if (paramMap.containsKey("stateCode")) {
                    State state = stateRepository.findById(Long.parseLong(request.getParameter("stateCode"))).get();
                    if (state != null) {
                        areaHead.setState(state);
                    }
                }

                if (paramMap.containsKey("zoneCode")) {
                    Zone zone = zoneRepository.findByIdAndStatus(Long.parseLong(request.getParameter("zoneCode")), true);
                    if (zone != null) {
                        areaHead.setZone(zone);
                    }
                }

                if (paramMap.containsKey("zonal")) {
                    Zone zone = zoneRepository.findByIdAndStatus(Long.parseLong(request.getParameter("zonal")), true);
                    if (zone != null) {
                        areaHead.setZone(zone);
                    }
                }

                if (paramMap.containsKey("district")) {
                    District district = districtRepository.findByIdAndStatus(Long.parseLong(request.getParameter("district")), true);
                    if (district != null) {
                        areaHead.setDistrict(district);
                    }
                }

                if (paramMap.containsKey("region")) {
                    Region region = regionRepository.findByIdAndStatus(Long.parseLong(request.getParameter("region")), true);
                    areaHead.setRegion(region);
                }
                /**** Uploading BankAccFile ****/
                if (request.getFile("partnerDeedFile") != null) {
                    MultipartFile image = request.getFile("partnerDeedFile");
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    if (imagePath != null) {
                        areaHead.setPartnerDeedFile(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                areaHead.setStatus(true);
                if (paramMap.containsKey("countryId")) {
                    Country country = countryRepository.findById(Long.parseLong(request.getParameter("countryId"))).get();
                    if (country != null) areaHead.setCountry(country);
                }
                areaHead.setUsername(request.getParameter("username"));
                areaHead.setPassword(bcryptEncoder.passwordEncoderNew().encode(request.getParameter("password")));
                areaHead.setPlain_password(request.getParameter("password"));

                AreaHead mAreaHead = areaHeadRepository.save(areaHead);
                response.addProperty("message", "Area Head updated successfully");
                response.addProperty("responseStatus", HttpStatus.OK.value());
            } else {
                response.addProperty("message", "Not found");
                response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            areaHeadLogger.error("updateAreaHead -> failed to updateAreaHead" + e);
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return response;
    }

    public JsonObject getParentHead(Map<String, String> request, HttpServletRequest req) {

//        AreaHead areaHead = jwtRequestFilter.getAreadHeadDataFromToken(req.getHeader("Authorization").substring(7));
        JsonObject res = new JsonObject();

        try {
            AreaHead areaHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(request.get("areaHeadId")), true);

            String areaRole = areaHead.getAreaRole();


            if (areaRole != null) {

                if (areaRole.equalsIgnoreCase("zonal")) {
                    AreaHead stateHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(areaHead.getZoneStateHead()), true);

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", stateHead.getId());
                    jsonObject.addProperty("name", stateHead.getFirstName() + " " + stateHead.getLastName());
                    jsonObject.addProperty("doj", "" + stateHead.getBirthDate());
                    jsonObject.addProperty("mobile", stateHead.getMobileNumber());
                    jsonObject.addProperty("email", stateHead.getEmail());
                    jsonObject.addProperty("whatsapp", stateHead.getWhatsappNumber());
                    jsonObject.addProperty("role", "sh");


                    res.addProperty("message", "success");
                    res.add("result", jsonObject);
                    res.addProperty("status", HttpStatus.OK.value());

                } else if (areaRole.equalsIgnoreCase("region")) {

                    AreaHead zoneHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(areaHead.getRegionZoneHeadId()), true);

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", zoneHead.getId());
                    jsonObject.addProperty("name", zoneHead.getFirstName() + " " + zoneHead.getLastName());
                    jsonObject.addProperty("doj", "" + zoneHead.getBirthDate());
                    jsonObject.addProperty("mobile", zoneHead.getMobileNumber());
                    jsonObject.addProperty("email", zoneHead.getEmail());
                    jsonObject.addProperty("whatsapp", zoneHead.getWhatsappNumber());
                    jsonObject.addProperty("role", "zh");


                    res.addProperty("message", "success");
                    res.add("result", jsonObject);
                    res.addProperty("status", HttpStatus.OK.value());

                } else if (areaRole.equalsIgnoreCase("district")) {

                    AreaHead regionHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(areaHead.getDistrictRegionHeadId()), true);

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", regionHead.getId());
                    jsonObject.addProperty("name", regionHead.getFirstName() + " " + regionHead.getLastName());
                    jsonObject.addProperty("doj", "" + regionHead.getBirthDate());
                    jsonObject.addProperty("mobile", regionHead.getMobileNumber());
                    jsonObject.addProperty("email", regionHead.getEmail());
                    jsonObject.addProperty("whatsapp", regionHead.getWhatsappNumber());
                    jsonObject.addProperty("role", "rh");


                    res.addProperty("message", "success");
                    res.add("result", jsonObject);
                    res.addProperty("status", HttpStatus.OK.value());

                }

                return res;
            }


        } catch (Exception x) {
            x.printStackTrace();
            System.out.println("Error=>" + x.toString());
        }

        res.addProperty("message", "success");
        res.addProperty("status", HttpStatus.OK.value());
        res.add("result", new JsonObject());
        return res;
    }


    public JsonObject getAreaHeadDashboard(Map<String, String> request, HttpServletRequest req) {

//        AreaHead areaHead = jwtRequestFilter.getAreadHeadDataFromToken(req.getHeader("Authorization").substring(7));
        /*LocalDate fromDate=LocalDate.parse(request.get("fromDate"));
        LocalDate toDate=LocalDate.parse(request.get("toDate"));*/


        String fromDate = request.get("fromDate").toString();
        String toDate = request.get("toDate").toString();
        String areaHeadId = request.get("areaHeadId").toString();
        AreaHead areaHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(areaHeadId), true);
        double commissionAmount = areaheadCommissionRepository.getTotalCommission(fromDate, toDate, areaHeadId);
        double purchaseAmount = areaheadCommissionRepository.getPurchaseAmount(fromDate, toDate, areaHeadId);
        double commissionPercentage = 0;
        double purchasePercentage = 0;
        if (commissionAmount > 0 && purchaseAmount > 0) {
            commissionPercentage = (commissionAmount / purchaseAmount) * 100;
            purchasePercentage = 100 - commissionPercentage;
        }

        int totalFranchise = 0, totalAreaHead = 0;
        switch (areaHead.getAreaRole()) {
            case "state":
                totalFranchise = franchiseMasterRepository.countByStateIdAndStatus(Long.parseLong(areaHeadId), true);
                totalAreaHead = areaHeadRepository.countByStateIdAndStatus(areaHead.getState().getId(), true);
                break;
            case "zonal":
                totalFranchise = franchiseMasterRepository.countByZoneIdAndStatus(Long.parseLong(areaHeadId), true);
                totalAreaHead = areaHeadRepository.countByZoneIdAndStatus(areaHead.getZone().getId(), true);
                break;
            case "region":
                totalFranchise = franchiseMasterRepository.countByRegionalIdAndStatus(Long.parseLong(areaHeadId), true);
                totalAreaHead = areaHeadRepository.countByRegionIdAndStatus(areaHead.getRegion().getId(), true);
                break;
            case "district":
                totalFranchise = franchiseMasterRepository.countByDistrictIdAndStatus(Long.parseLong(areaHeadId), true);
                totalAreaHead = areaHeadRepository.countByDistrictIdAndStatus(areaHead.getRegion().getId(), true);
                break;
        }


        JsonObject res = new JsonObject();
        JsonObject jsonObject = new JsonObject();

        try {
            jsonObject.addProperty("commissionAmount", commissionAmount);
            jsonObject.addProperty("commissionPercentage", commissionPercentage);
            jsonObject.addProperty("purchaseAmount", purchaseAmount);
            jsonObject.addProperty("purchasePercentage", (purchasePercentage));
            jsonObject.addProperty("totalFranchise", totalFranchise);
            jsonObject.addProperty("totalAreaHead", totalAreaHead);


        } catch (Exception x) {
            x.printStackTrace();
            System.out.println("Error=>" + x.toString());
        }

        res.addProperty("message", "success");
        res.addProperty("responseStatus", HttpStatus.OK.value());
        res.add("responseObject", jsonObject);
        return res;
    }

    public JsonArray getFrLatestPurchase(String franchiseCode) {

        JsonArray jsonArray = new JsonArray();

        try {

//            String franchiseCode= request.get("franchiseCode").toString();
            String invoiceNumber = areaheadCommissionRepository.findTop1ByFranchiseCodeOrderByIdDesc(franchiseCode).getSalesInvoiceNumber();
            Long invoiceId = tranxSalesInvoiceRepository.findBySalesInvoiceNo(invoiceNumber).getId();
            List<TranxSalesInvoiceDetailsUnits> orderDetails = tranxSalesInvoiceDetailsUnitRepository.findBySalesInvoiceIdAndStatus(invoiceId, true);


            for (TranxSalesInvoiceDetailsUnits product : orderDetails) {
                JsonObject jObject = new JsonObject();
                jObject.addProperty("productName", product.getProduct().getProductName());
                Category category = product.getProduct().getCategory();
                jObject.addProperty("categoryName", category != null ? category.getCategoryName() : "NA");
                jsonArray.add(jObject);
            }


        } catch (Exception x) {
            x.printStackTrace();
            System.out.println("Error=>" + x.toString());
        }


        return jsonArray;
    }

    public JsonArray getFrPerformers(String areaheadId, String startDate, String endDate, String orderType) {

        JsonArray frList = new JsonArray();

        try {

            String query = "SELECT franchise_code FROM areahead_commission_tbl where areahead_id=" + areaheadId + " and (invoice_date between '" + startDate + "' and '" + endDate + "') group by franchise_code order by SUM(sales_invoice_amount) " + orderType + " LIMIT 5";
            Query q = entityManager.createNativeQuery(query);
            List products = q.getResultList();


            for (int i = 0; i < products.size(); i++) {
//                System.out.println(products.get(i));
                FranchiseMaster franchiseMaster = franchiseMasterRepository.findByFranchiseCodeAndStatus(products.get(i).toString(), true);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("franchiseName", franchiseMaster.getFranchiseName());
                jsonObject.addProperty("franchiseCode", franchiseMaster.getFranchiseCode());
                jsonObject.addProperty("franchiseId", franchiseMaster.getId());

                frList.add(jsonObject);
            }


        } catch (Exception x) {
            x.printStackTrace();
            System.out.println("Error=>" + x.toString());
        }


        return frList;
    }

    public JsonArray getAllFrPerformers(String areaheadId, String startDate, String endDate, String orderType) {

        JsonArray frList = new JsonArray();

        try {

            String query = "SELECT franchise_code FROM areahead_commission_tbl where areahead_id=" + areaheadId + " and (invoice_date between '" + startDate + "' and '" + endDate + "') group by franchise_code order by SUM(sales_invoice_amount) " + orderType;
            Query q = entityManager.createNativeQuery(query);
            List products = q.getResultList();


            for (int i = 0; i < products.size(); i++) {
//                System.out.println(products.get(i));
                FranchiseMaster franchiseMaster = franchiseMasterRepository.findByFranchiseCodeAndStatus(products.get(i).toString(), true);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("franchiseName", franchiseMaster.getFranchiseName());
                jsonObject.addProperty("franchiseCode", franchiseMaster.getFranchiseCode());
                jsonObject.addProperty("franchiseId", franchiseMaster.getId());

                frList.add(jsonObject);
            }


        } catch (Exception x) {
            x.printStackTrace();
            System.out.println("Error=>" + x.toString());
        }


        return frList;
    }

    public JsonObject getAreaHeadFranchiseDashboard(Map<String, String> request, HttpServletRequest req) {

//        AreaHead areaHead = jwtRequestFilter.getAreadHeadDataFromToken(req.getHeader("Authorization").substring(7));
        /*LocalDate fromDate=LocalDate.parse(request.get("fromDate"));
        LocalDate toDate=LocalDate.parse(request.get("toDate"));*/


        String fromDate = request.get("fromDate").toString();
        String toDate = request.get("toDate").toString();
        String areaHeadId = request.get("areaHeadId").toString();
        String franchiseCode = request.get("franchiseCode");

        AreaHead areaHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(areaHeadId), true);
        double commissionAmount = areaheadCommissionRepository.getTotalCommission(fromDate, toDate, areaHeadId, franchiseCode);
        double purchaseAmount = areaheadCommissionRepository.getPurchaseAmount(fromDate, toDate, areaHeadId, franchiseCode);
        double commissionPercentage = (commissionAmount / purchaseAmount) * 100;
        JsonArray latestPurchase = getFrLatestPurchase(franchiseCode);


        JsonObject res = new JsonObject();
        JsonObject jsonObject = new JsonObject();

        try {
            jsonObject.addProperty("commissionAmount", commissionAmount);
            jsonObject.addProperty("commissionPercentage", commissionPercentage);
            jsonObject.addProperty("purchaseAmount", purchaseAmount);
            jsonObject.addProperty("purchasePercentage", (100 - commissionPercentage));
            jsonObject.add("latestPurchase", latestPurchase);


        } catch (Exception x) {
            x.printStackTrace();
            System.out.println("Error=>" + x.toString());
        }

        res.addProperty("message", "success");
        res.addProperty("responseStatus", HttpStatus.OK.value());
        res.add("responseObject", jsonObject);
        return res;
    }

    public JsonObject getAreaHeadByCode(Map<String, String> request, HttpServletRequest req) {

        AreaHead areaHead = jwtRequestFilter.getAreadHeadDataFromToken(req.getHeader("Authorization").substring(7));
        JsonArray jsonArray = new JsonArray();
        JsonObject res = new JsonObject();

        try {
            String areaRole = areaHead.getAreaRole();

            String areaCode = request.get("areaCode");


            if (areaRole != null) {

                if (areaRole.equalsIgnoreCase("state")) {
                    Long areaHeadId = areaHead.getState().getId();
                    List<AreaHead> heads = areaHeadRepository.findByAreaRoleAndStateId(areaCode, areaHeadId);
                    for (AreaHead a : heads) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("id", a.getId());
                        jsonObject.addProperty("name", a.getFirstName() + " " + a.getLastName());
                        jsonObject.addProperty("doj", "" + a.getBirthDate());
                        jsonObject.addProperty("mobile", a.getMobileNumber());
                        jsonObject.addProperty("email", a.getEmail());
                        jsonObject.addProperty("whatsapp", a.getWhatsappNumber());

                        jsonArray.add(jsonObject);

                    }

                    res.addProperty("message", "success");
                    res.add("result", jsonArray);
                    res.addProperty("status", HttpStatus.OK.value());

                } else if (areaRole.equalsIgnoreCase("zonal")) {

                    Long areaHeadId = areaHead.getZone().getId();
                    List<AreaHead> heads = areaHeadRepository.findByAreaRoleAndZoneId(areaCode, areaHeadId);

                    for (AreaHead a : heads) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("id", a.getId());
                        jsonObject.addProperty("name", a.getFirstName() + " " + a.getLastName());
                        jsonObject.addProperty("doj", "" + a.getBirthDate());
                        jsonObject.addProperty("mobile", a.getMobileNumber());
                        jsonObject.addProperty("email", a.getEmail());
                        jsonObject.addProperty("whatsapp", a.getWhatsappNumber());


                        jsonArray.add(jsonObject);

                    }

                    res.addProperty("message", "success");
                    res.add("result", jsonArray);
                    res.addProperty("status", HttpStatus.OK.value());

                } else if (areaRole.equalsIgnoreCase("region")) {
                    Long areaHeadId = areaHead.getRegion().getId();
                    List<AreaHead> heads = areaHeadRepository.findByAreaRoleAndRegionId(areaCode, areaHeadId);
                    for (AreaHead a : heads) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("id", a.getId());
                        jsonObject.addProperty("name", a.getFirstName() + " " + a.getLastName());
                        jsonObject.addProperty("doj", "" + a.getBirthDate());
                        jsonObject.addProperty("mobile", a.getMobileNumber());
                        jsonObject.addProperty("email", a.getEmail());
                        jsonObject.addProperty("whatsapp", a.getWhatsappNumber());

                        jsonArray.add(jsonObject);

                    }

                    res.addProperty("message", "success");
                    res.add("result", jsonArray);
                    res.addProperty("status", HttpStatus.OK.value());

                } else {

                }

                return res;
            }


        } catch (Exception x) {
            x.printStackTrace();
            System.out.println("Error=>" + x.toString());
        }

        res.addProperty("message", "success");
        res.addProperty("status", HttpStatus.OK.value());
        res.add("result", new JsonObject());
        return res;
    }


    public JsonObject getCountByAreahead(HttpServletRequest request) {

        AreaHead areaHead = jwtRequestFilter.getAreadHeadDataFromToken(request.getHeader("Authorization").substring(7));


        String areaRole = areaHead.getAreaRole();
        Long areaCode = 0L;
        JsonObject response = new JsonObject();
        JsonObject res = new JsonObject();
        int zoneCount = 0, regionCount = 0, districtCount = 0;
        if (areaRole != null) {

            if (areaRole.equalsIgnoreCase("state")) {
                areaCode = areaHead.getState().getId();
                zoneCount = areaHeadRepository.countByAreaRoleAndStateIdAndStatus("zonal", areaCode, true);
                regionCount = areaHeadRepository.countByAreaRoleAndStateIdAndStatus("region", areaCode, true);
                districtCount = areaHeadRepository.countByAreaRoleAndStateIdAndStatus("district", areaCode, true);

                response.addProperty("zoneCount", zoneCount);
                response.addProperty("regionCount", regionCount);
                response.addProperty("districtCount", districtCount);
            } else if (areaRole.equalsIgnoreCase("zonal")) {
                areaCode = areaHead.getZone().getId();
                regionCount = areaHeadRepository.countByAreaRoleAndZoneIdAndStatus("region", areaCode, true);
                districtCount = areaHeadRepository.countByAreaRoleAndZoneIdAndStatus("district", areaCode, true);

                response.addProperty("regionCount", regionCount);
                response.addProperty("districtCount", districtCount);
            } else if (areaRole.equalsIgnoreCase("region")) {
                areaCode = areaHead.getRegion().getId();
                districtCount = areaHeadRepository.countByAreaRoleAndRegionIdAndStatus("district", areaCode, true);
                response.addProperty("districtCount", districtCount);
            } else {

            }

            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", response);
        } else {
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("responseObject", response);
        }

        return res;
    }

    public JsonObject getAreaHeadById(HttpServletRequest request) {
        AreaHead mAreaHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);

        JsonObject response = new JsonObject();
        JsonObject res = new JsonObject();
        if (mAreaHead != null) {
            response.addProperty("id", mAreaHead.getId());
            response.addProperty("firstName", mAreaHead.getFirstName());
            response.addProperty("middleName", mAreaHead.getMiddleName());
            response.addProperty("lastName", mAreaHead.getLastName());
            response.addProperty("email", mAreaHead.getEmail());
            response.addProperty("mobileNumber", mAreaHead.getMobileNumber() != null ? mAreaHead.getMobileNumber().toString() : "");
            response.addProperty("whatsAppNumber", mAreaHead.getWhatsappNumber() != null ? mAreaHead.getWhatsappNumber().toString() : "");
            response.addProperty("DOB", String.valueOf(mAreaHead.getBirthDate()));
            response.addProperty("permenantAddress", mAreaHead.getPermenantAddress());
            response.addProperty("temporaryAddress", mAreaHead.getTemporaryAddress());
            response.addProperty("gender", mAreaHead.getGender());
            response.addProperty("aadharCard", mAreaHead.getAadharCardNo());
            response.addProperty("aadharCardFile", mAreaHead.getAadharCardFile() != null ? serverUrl + mAreaHead.getAadharCardFile() : "");
            response.addProperty("panCard", mAreaHead.getPanCardNo());
            response.addProperty("panCardFile", mAreaHead.getPanCardFile() != null ? serverUrl + mAreaHead.getPanCardFile() : "");
            response.addProperty("bankAccNo", mAreaHead.getBankAccNo());
            response.addProperty("bankAccFile", mAreaHead.getBankAccFile() != null ? serverUrl + mAreaHead.getBankAccFile() : "");
            response.addProperty("areaRole", mAreaHead.getAreaRole());
            response.addProperty("sameAsAddress", mAreaHead.getIsSameAddress());
            response.addProperty("pincode", mAreaHead.getPincode());
            response.addProperty("city", mAreaHead.getCity());
            response.addProperty("area", mAreaHead.getArea());
            response.addProperty("areaId",mAreaHead.getAreaMaster().getId());
//            response.addProperty("areaId", mAreaHead.getArea());
            response.addProperty("stateName", mAreaHead.getAreaMaster().getState());
            response.addProperty("countryId", mAreaHead.getCountry().getId());
            response.addProperty("countryName", mAreaHead.getCountry().getName());
            response.addProperty("userName", mAreaHead.getUsername());
            response.addProperty("userPassword", mAreaHead.getPlain_password());
            response.addProperty("stateCode", mAreaHead.getAreaMaster().getStateCode());
            response.addProperty("corpPincode", mAreaHead.getCorpPincode());
            response.addProperty("corpCity", mAreaHead.getCorporateCity());
//            response.addProperty("corpArea", mAreaHead.getCorporateArea());
            response.addProperty("corpAreaId",mAreaHead.getCorporateAreaMaster().getId());
//            response.addProperty("corpAreaId", mAreaHead.getCorporateArea());
            response.addProperty("corpStateName", mAreaHead.getCorporateAreaMaster().getState());
            response.addProperty("corpStateNameCode", mAreaHead.getCorporateAreaMaster().getStateCode());
            response.addProperty("stateCode", mAreaHead.getStateCode() != null ? mAreaHead.getStateCode() : "");
//            response.addProperty("regionId", mAreaHead.getStateCode() != null ?  mAreaHead.getStateCode() : "");
//            response.addProperty("district", mAreaHead.getStateCode() != null ?  mAreaHead.getStateCode() : "");
            response.addProperty("bankAccName", mAreaHead.getBankAccName());
            response.addProperty("bankIfsc", mAreaHead.getBankAccIFSC());
            response.addProperty("zoneStateHead", mAreaHead.getZoneStateHead() != null ? mAreaHead.getZoneStateHead() : "");
            if (!mAreaHead.getZoneStateHead().isEmpty()) {
                AreaHead stateHead = areaHeadRepository.getById(Long.parseLong(mAreaHead.getZoneStateHead()));
                response.addProperty("zoneStateHeadName", stateHead != null ? (stateHead.getFirstName() + " " + stateHead.getLastName()) : "");
            }
            response.addProperty("regionStateHead", mAreaHead.getRegionStateHeadId() != null ? mAreaHead.getRegionStateHeadId() : "");
//            if (!mAreaHead.getRegionStateHeadId().isEmpty()) {
//                AreaHead stateHead1 = areaHeadRepository.getById(Long.parseLong(mAreaHead.getRegionStateHeadId()));
//                response.addProperty("regionStateHeadName", stateHead1 != null ? (stateHead1.getFirstName() + " " + stateHead1.getLastName()) : "");
//            }
            response.addProperty("regionZoneHead", mAreaHead.getRegionZoneHeadId() != null ? mAreaHead.getRegionZoneHeadId() : "");
            if (!mAreaHead.getRegionZoneHeadId().isEmpty()) {
                AreaHead zoneHead1 = areaHeadRepository.getById(Long.parseLong(mAreaHead.getRegionZoneHeadId()));
                response.addProperty("regionZoneHeadName", zoneHead1 != null ? (zoneHead1.getFirstName() + " " + zoneHead1.getLastName()) : "");
            }
            response.addProperty("districtStateHead", mAreaHead.getDistrictStateHeadId() != null ? mAreaHead.getDistrictStateHeadId() : "");
            if (!mAreaHead.getDistrictStateHeadId().isEmpty()) {
                AreaHead stateHead2 = areaHeadRepository.getById(Long.parseLong(mAreaHead.getDistrictStateHeadId()));
                response.addProperty("districtStateHeadName", stateHead2 != null ? (stateHead2.getFirstName() + " " + stateHead2.getLastName()) : "");
            }
            response.addProperty("districtZoneHead", mAreaHead.getDistrictZoneHeadId() != null ? mAreaHead.getDistrictZoneHeadId() : "");
//            if (!mAreaHead.getDistrictZoneHeadId().isEmpty()) {
//                AreaHead zoneHead2 = areaHeadRepository.getById(Long.parseLong(mAreaHead.getDistrictZoneHeadId()));
//                response.addProperty("districtZoneHeadName", zoneHead2 != null ? (zoneHead2.getFirstName() + " " + zoneHead2.getLastName()) : "");
//            }
            response.addProperty("districtRegionHead", mAreaHead.getDistrictRegionHeadId() != null ? mAreaHead.getDistrictRegionHeadId() : "");
            if (!mAreaHead.getDistrictRegionHeadId().isEmpty()) {
                AreaHead regionHead1 = areaHeadRepository.getById(Long.parseLong(mAreaHead.getDistrictRegionHeadId()));
                response.addProperty("districtRegionHeadName", regionHead1 != null ? (regionHead1.getFirstName() + " " + regionHead1.getLastName()) : "");
            }
            List<PincodeMaster> areaMasterList = pincodeMasterRepository.findByPincode(mAreaHead.getPincode());
            JsonArray jsonArray = new JsonArray();
            for (PincodeMaster areaMaster : areaMasterList) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("areaId", areaMaster.getId());
                jsonObject.addProperty("areaName", areaMaster.getArea());
                jsonObject.addProperty("areaPincode", areaMaster.getPincode());
                jsonArray.add(jsonObject);
            }
            response.add("areaList", jsonArray);

            if (mAreaHead.getIsSameAddress()) {
                response.add("corpAreaList", jsonArray);
            } else {

                List<PincodeMaster> corpAreaMasterList = pincodeMasterRepository.findByPincode(mAreaHead.getCorpPincode());
                JsonArray corpJsonArray = new JsonArray();
                for (PincodeMaster areaMaster : corpAreaMasterList) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("areaId", areaMaster.getId());
                    jsonObject.addProperty("areaName", areaMaster.getArea());
                    jsonObject.addProperty("areaPincode", areaMaster.getPincode());
                    jsonArray.add(jsonObject);
                }
                response.add("corpAreaList", corpJsonArray);
            }


            if (mAreaHead.getAreaRole().equalsIgnoreCase("state")) {
                State state = mAreaHead.getState();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("value", state.getId());
                jsonObject.addProperty("label", state.getName());
                response.add("state", jsonObject);
            }

            if (mAreaHead.getAreaRole().equalsIgnoreCase("zonal")) {
                State state = mAreaHead.getState();
                Zone zone = mAreaHead.getZone();

                if (state != null) {
                    JsonObject stateObject = new JsonObject();
                    stateObject.addProperty("value", state.getId());
                    stateObject.addProperty("label", state.getName());
                    response.add("state", stateObject);
                }

                JsonObject zoneObject = new JsonObject();
                zoneObject.addProperty("value", zone.getId());
                zoneObject.addProperty("label", zone.getZoneName());
                response.add("zone", zoneObject);
            }

            if (mAreaHead.getAreaRole().equalsIgnoreCase("region")) {
                State state = mAreaHead.getState();
                Zone zone = mAreaHead.getZone();
                Region region = mAreaHead.getRegion();

                if (state != null) {
                    JsonObject stateObject = new JsonObject();
                    stateObject.addProperty("value", state.getId());
                    stateObject.addProperty("label", state.getName());
                    response.add("state", stateObject);
                }


                if (zone != null) {
                    JsonObject zoneObject = new JsonObject();
                    zoneObject.addProperty("value", zone.getId());
                    zoneObject.addProperty("label", zone.getZoneName());
                    response.add("zone", zoneObject);
                }

                JsonObject rgionObject = new JsonObject();
                rgionObject.addProperty("value", region.getId());
                rgionObject.addProperty("label", region.getRegionName());
                response.add("region", rgionObject);

            }

            if (mAreaHead.getAreaRole().equalsIgnoreCase("district")) {
                State state = mAreaHead.getState();
                Zone zone = mAreaHead.getZone();
                Region region = mAreaHead.getRegion();
                District district = mAreaHead.getDistrict();

                if (state != null) {
                    JsonObject stateObject = new JsonObject();
                    stateObject.addProperty("value", state.getId());
                    stateObject.addProperty("label", state.getName());
                    response.add("state", stateObject);
                }


                if (zone != null) {
                    JsonObject zoneObject = new JsonObject();
                    zoneObject.addProperty("value", zone.getId());
                    zoneObject.addProperty("label", zone.getZoneName());
                    response.add("zone", zoneObject);
                }

                if (region != null) {
                    JsonObject rgionObject = new JsonObject();
                    rgionObject.addProperty("value", region.getId());
                    rgionObject.addProperty("label", region.getRegionName());
                    response.add("region", rgionObject);
                }

                JsonObject districtObject = new JsonObject();
                districtObject.addProperty("value", district.getId());
                districtObject.addProperty("label", district.getDistrictName());
                response.add("district", districtObject);

            }


            response.addProperty("partnerDeedFile", mAreaHead.getPartnerDeedFile() != null ? serverUrl + mAreaHead.getPartnerDeedFile() : "");

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

    public JsonObject areaHeadDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
//        Outlet company = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        AreaHead areaHead = areaHeadRepository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        try {

            if (areaHead != null) {
                areaHead.setStatus(false);
                areaHeadRepository.save(areaHead);
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

    public JsonObject getAllDistrictHeads(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        JsonObject res = new JsonObject();
        List<AreaHead> list = new ArrayList<>();
        try {
            list = areaHeadRepository.findByStatusAndAreaRole(true, "district");
            if (list.size() > 0) {
                for (AreaHead mAreaHead : list) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", mAreaHead.getId());
                    response.addProperty("fullName", mAreaHead.getFirstName() + " " + mAreaHead.getLastName());
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
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            areaHeadLogger.error("Error in getAllDistrictHead:" + exceptionAsString);
        }
        return res;
    }

    public Object stateHeadDetail(Map<String, String> jsonReq, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            AreaHead areaHead = jwtRequestFilter.getAreadHeadDataFromToken(request.getHeader("Authorization").substring(7));
            JsonObject headDetail = new JsonObject();

            int totalFranchise = franchiseMasterRepository.countByStateIdAndStatus(areaHead.getState().getId(), true);
            int totalZonal = areaHeadRepository.countByAreaRoleAndStateIdAndStatus("zonal", areaHead.getState().getId(), true);

            String yearMonth = null;
            if (!jsonReq.get("currentMonth").equals("")) {
                System.out.println("jsonReq " + jsonReq.get("currentMonth"));
                String[] currentMonth = jsonReq.get("currentMonth").split("-");
                String userMonth = currentMonth[0];
                String userYear = currentMonth[1];
                yearMonth = userYear + "-" + userMonth;
            } else {
                yearMonth = LocalDate.now().getYear() + "-" + LocalDate.now().getMonthValue();
            }
            System.out.println("yearMonth " + yearMonth);
            double totalPurchase = tranxSalesInvoiceRepository.getPurchaseAmountTotalByStateHead(areaHead.getId(), yearMonth);

            headDetail.addProperty("totalCommission", 0);
            headDetail.addProperty("totalPurchase", totalPurchase);
            headDetail.addProperty("totalFranchise", totalFranchise);
            headDetail.addProperty("totalZonal", totalZonal);

            response.add("response", headDetail);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object getZonalHeadsList(Map<String, String> jsonReq, HttpServletRequest request) {
        JsonObject response = new JsonObject();

        try {
            Long stateHeadId = Long.valueOf(jsonReq.get("stateHeadId"));
            AreaHead areaHead = areaHeadRepository.findByIdAndStatus(stateHeadId, true);

            JsonArray headsArr = new JsonArray();
            List<AreaHead> zonalHeads = areaHeadRepository.findByAreaRoleAndStateIdAndStatusNative("zonal", areaHead.getState().getId(), true);
            for (AreaHead zonal : zonalHeads) {
                JsonObject object = new JsonObject();
                object.addProperty("zonalId", zonal.getId());
                object.addProperty("firstName", zonal.getFirstName());
                object.addProperty("lastName", zonal.getLastName());
                object.addProperty("mobileNumber", zonal.getMobileNumber());
                object.addProperty("whatsappNumber", zonal.getWhatsappNumber());
                object.addProperty("email", zonal.getEmail());

                headsArr.add(object);
            }
            response.add("response", headsArr);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object zonalHeadDetail(Map<String, String> jsonReq, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            JsonObject headDetail = new JsonObject();
            Long zoneHeadId = Long.valueOf(jsonReq.get("zoneHeadId"));

            JsonObject obj = new JsonObject();
            AreaHead areaHead = areaHeadRepository.findByIdAndStatus(zoneHeadId, true);
            if (areaHead != null) {
                obj.addProperty("firstName", areaHead.getFirstName());
                obj.addProperty("lastName", areaHead.getLastName());
                obj.addProperty("mobileNumber", areaHead.getMobileNumber());
                obj.addProperty("whatsappNumber", areaHead.getWhatsappNumber());
                obj.addProperty("email", areaHead.getEmail());
            }

            int totalFranchise = franchiseMasterRepository.countByZoneIdAndStatus(zoneHeadId, true);
            int totalRegion = areaHeadRepository.countByAreaRoleAndZoneIdAndStatus("region", areaHead.getZone().getId(), true);

            String yearMonth = null;
            if (!jsonReq.get("currentMonth").equals("")) {
                System.out.println("jsonReq " + jsonReq.get("currentMonth"));
                String[] currentMonth = jsonReq.get("currentMonth").split("-");
                String userMonth = currentMonth[0];
                String userYear = currentMonth[1];
                yearMonth = userYear + "-" + userMonth;
            } else {
                yearMonth = LocalDate.now().getYear() + "-" + LocalDate.now().getMonthValue();
            }
            System.out.println("yearMonth " + yearMonth);
            double totalPurchase = tranxSalesInvoiceRepository.getPurchaseAmountTotalByZoneHead(areaHead.getId(), yearMonth);

            headDetail.addProperty("totalCommission", 0);
            headDetail.addProperty("totalPurchase", totalPurchase);
            headDetail.addProperty("totalFranchise", totalFranchise);
            headDetail.addProperty("totalRegion", totalRegion);
            headDetail.add("zoneHeadDetails", obj);

            response.add("response", headDetail);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object getRegionHeadsList(Map<String, String> jsonReq, HttpServletRequest request) {
        JsonObject response = new JsonObject();

        try {
            Long zoneHeadId = Long.valueOf(jsonReq.get("zoneHeadId"));
            AreaHead areaHead = areaHeadRepository.findByIdAndStatus(zoneHeadId, true);

            JsonArray headsArr = new JsonArray();
            List<AreaHead> zonalHeads = areaHeadRepository.findByAreaRoleAndZoneIdAndStatusNative("region", areaHead.getZone().getId(), true);
            for (AreaHead areaHd : zonalHeads) {
                JsonObject object = new JsonObject();
                object.addProperty("regionalId", areaHd.getId());
                object.addProperty("firstName", areaHd.getFirstName());
                object.addProperty("lastName", areaHd.getLastName());
                object.addProperty("mobileNumber", areaHd.getMobileNumber());
                object.addProperty("whatsappNumber", areaHd.getWhatsappNumber());
                object.addProperty("email", areaHd.getEmail());

                headsArr.add(object);
            }
            response.add("response", headsArr);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object regionHeadDetail(Map<String, String> jsonReq, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            JsonObject headDetail = new JsonObject();
            Long regionalHeadId = Long.valueOf(jsonReq.get("regionalHeadId"));

            JsonObject obj = new JsonObject();
            AreaHead areaHead = areaHeadRepository.findByIdAndStatus(regionalHeadId, true);
            if (areaHead != null) {
                obj.addProperty("firstName", areaHead.getFirstName());
                obj.addProperty("lastName", areaHead.getLastName());
                obj.addProperty("mobileNumber", areaHead.getMobileNumber());
                obj.addProperty("whatsappNumber", areaHead.getWhatsappNumber());
                obj.addProperty("email", areaHead.getEmail());
            }

            int totalFranchise = franchiseMasterRepository.countByRegionalIdAndStatus(areaHead.getRegion().getId(), true);
            int totalDistrict = areaHeadRepository.countByAreaRoleAndRegionIdAndStatus("region", areaHead.getRegion().getId(), true);

            String yearMonth = null;
            if (!jsonReq.get("currentMonth").equals("")) {
                System.out.println("jsonReq " + jsonReq.get("currentMonth"));
                String[] currentMonth = jsonReq.get("currentMonth").split("-");
                String userMonth = currentMonth[0];
                String userYear = currentMonth[1];
                yearMonth = userYear + "-" + userMonth;
            } else {
                yearMonth = LocalDate.now().getYear() + "-" + LocalDate.now().getMonthValue();
            }
            System.out.println("yearMonth " + yearMonth);
            double totalPurchase = tranxSalesInvoiceRepository.getPurchaseAmountTotalByRegionalHead(areaHead.getId(), yearMonth);

            headDetail.addProperty("totalCommission", 0);
            headDetail.addProperty("totalPurchase", totalPurchase);
            headDetail.addProperty("totalFranchise", totalFranchise);
            headDetail.addProperty("totalDistrict", totalDistrict);
            headDetail.add("regionHeadDetails", obj);

            response.add("response", headDetail);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object getDistrictHeadsList(Map<String, String> jsonReq, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            Long regionalHeadId = Long.valueOf(jsonReq.get("regionalHeadId"));
            AreaHead areaHead = areaHeadRepository.findByIdAndStatus(regionalHeadId, true);

            JsonArray headsArr = new JsonArray();
            List<AreaHead> distrHeads = areaHeadRepository.findByAreaRoleAndZoneIdAndStatusNative("district", areaHead.getZone().getId(), true);
            for (AreaHead areaHd : distrHeads) {
                JsonObject object = new JsonObject();
                object.addProperty("regionalId", areaHd.getId());
                object.addProperty("firstName", areaHd.getFirstName());
                object.addProperty("lastName", areaHd.getLastName());
                object.addProperty("mobileNumber", areaHd.getMobileNumber());
                object.addProperty("whatsappNumber", areaHd.getWhatsappNumber());
                object.addProperty("email", areaHd.getEmail());

                headsArr.add(object);
            }
            response.add("response", headsArr);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object districtHeadDetail(Map<String, String> jsonReq, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            JsonObject headDetail = new JsonObject();
            Long districtHeadId = Long.valueOf(jsonReq.get("districtHeadId"));

            JsonObject obj = new JsonObject();
            AreaHead areaHead = areaHeadRepository.findByIdAndStatus(districtHeadId, true);
            if (areaHead != null) {
                obj.addProperty("firstName", areaHead.getFirstName());
                obj.addProperty("lastName", areaHead.getLastName());
                obj.addProperty("mobileNumber", areaHead.getMobileNumber());
                obj.addProperty("whatsappNumber", areaHead.getWhatsappNumber());
                obj.addProperty("email", areaHead.getEmail());
            }

            int totalFranchise = franchiseMasterRepository.countByDistrictIdAndStatus(areaHead.getDistrict().getId(), true);
            String yearMonth = null;
            if (!jsonReq.get("currentMonth").equals("")) {
                System.out.println("jsonReq " + jsonReq.get("currentMonth"));
                String[] currentMonth = jsonReq.get("currentMonth").split("-");
                String userMonth = currentMonth[0];
                String userYear = currentMonth[1];
                yearMonth = userYear + "-" + userMonth;
            } else {
                yearMonth = LocalDate.now().getYear() + "-" + LocalDate.now().getMonthValue();
            }
            System.out.println("yearMonth " + yearMonth);
            double totalPurchase = tranxSalesInvoiceRepository.getPurchaseAmountTotalByDistrictHead(areaHead.getDistrict().getId(), yearMonth);

            headDetail.addProperty("totalCommission", 0);
            headDetail.addProperty("totalPurchase", totalPurchase);
            headDetail.addProperty("totalFranchise", totalFranchise);
            headDetail.add("districtHeadDetails", obj);

            response.add("response", headDetail);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject getAnalytics(HttpServletRequest request) {
        AreaHead areaHead = jwtRequestFilter.getAreadHeadDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject response = new JsonObject();
        JsonObject jsonObject = new JsonObject();
        LocalDate date = LocalDate.parse(request.getParameter("date"));
        Double percentage = 0.0;
        Double tabxableAmount = 0.0;
        Double purchase = 0.0;
        Double commission = 0.0;
        if (areaHead.getAreaRole().equals("state")) {
            percentage = commissionMasterRepository.findByRoleTypeAndStatus("state", true);
            tabxableAmount = tranxSalesInvoiceRepository.getSumOfTaxableOrTotalAmount("taxable_amount", "state_id", areaHead.getState().getId(), String.valueOf(date.getMonthValue()), String.valueOf(date.getYear()));
            purchase = tranxSalesInvoiceRepository.getSumOfTaxableOrTotalAmount("total_amount", "state_id", areaHead.getState().getId(), String.valueOf(date.getMonthValue()), String.valueOf(date.getYear()));
            commission = (tabxableAmount * percentage) / 100;
        } else if (areaHead.getAreaRole().equals("zonal")) {
            percentage = commissionMasterRepository.findByRoleTypeAndStatus("zonal", true);
            tabxableAmount = tranxSalesInvoiceRepository.getSumOfTaxableOrTotalAmount("taxable_amount", "zone_id", areaHead.getState().getId(), String.valueOf(date.getMonthValue()), String.valueOf(date.getYear()));
            purchase = tranxSalesInvoiceRepository.getSumOfTaxableOrTotalAmount("total_amount", "zone_id", areaHead.getState().getId(), String.valueOf(date.getMonthValue()), String.valueOf(date.getYear()));
            commission = (tabxableAmount * percentage) / 100;
        } else if (areaHead.getAreaRole().equals("region")) {
            percentage = commissionMasterRepository.findByRoleTypeAndStatus("region", true);
            tabxableAmount = tranxSalesInvoiceRepository.getSumOfTaxableOrTotalAmount("taxable_amount", "region_id", areaHead.getState().getId(), String.valueOf(date.getMonthValue()), String.valueOf(date.getYear()));
            purchase = tranxSalesInvoiceRepository.getSumOfTaxableOrTotalAmount("total_amount", "region_id", areaHead.getState().getId(), String.valueOf(date.getMonthValue()), String.valueOf(date.getYear()));
            commission = (tabxableAmount * percentage) / 100;
        } else if (areaHead.getAreaRole().equals("district")) {
            percentage = commissionMasterRepository.findByRoleTypeAndStatus("district", true);
            tabxableAmount = tranxSalesInvoiceRepository.getSumOfTaxableOrTotalAmount("taxable_amount", "district_id", areaHead.getState().getId(), String.valueOf(date.getMonthValue()), String.valueOf(date.getYear()));
            purchase = tranxSalesInvoiceRepository.getSumOfTaxableOrTotalAmount("total_amount", "district_id", areaHead.getState().getId(), String.valueOf(date.getMonthValue()), String.valueOf(date.getYear()));
            commission = (tabxableAmount * percentage) / 100;
        }
        jsonObject.addProperty("purchase", purchase);
        jsonObject.addProperty("commission", commission);
        response.add("response", jsonObject);
        response.addProperty("responseStatus", HttpStatus.OK.value());
        return response;
    }

    public Object getFranchiseList(Map<String, String> jsonReq, HttpServletRequest request) {
        JsonObject response = new JsonObject();

        try {
            List<FranchiseMaster> franchiseList = new ArrayList<>();
            String query = "SELECT * FROM franchise_master_tbl WHERE status=1";

            if (jsonReq.containsKey("stateId")) query += " AND state_id=" + jsonReq.get("stateId");
            if (jsonReq.containsKey("zoneId")) query += " AND zone_id=" + jsonReq.get("zoneId");
            if (jsonReq.containsKey("regionId")) query += " AND regional_id=" + jsonReq.get("regionId");
            if (jsonReq.containsKey("districtId")) query += " AND region_id=" + jsonReq.get("districtId");

            System.out.println("query " + query);
            Query q = entityManager.createNativeQuery(query, FranchiseMaster.class);

            franchiseList = q.getResultList();
            System.out.println("Limit total rows " + franchiseList.size());

            JsonArray frArr = new JsonArray();
            for (FranchiseMaster franchiseMaster : franchiseList) {
                JsonObject object = new JsonObject();
                object.addProperty("franchiseId", franchiseMaster.getId());
                object.addProperty("franchiseCode", franchiseMaster.getFranchiseCode());
                object.addProperty("franchiseName", franchiseMaster.getFranchiseName());
                object.addProperty("franchiseAddress", franchiseMaster.getFranchiseAddress());
                object.addProperty("mobileNumber", franchiseMaster.getMobileNumber());
                object.addProperty("whatsappNumber", franchiseMaster.getWhatsappNumber());
                object.addProperty("email", franchiseMaster.getEmail());

                frArr.add(object);
            }
            response.add("response", frArr);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("message", "Failed to load data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject getFranchisePerformers(Map<String, String> request, HttpServletRequest req) {
        JsonObject response = new JsonObject();
        JsonObject res = new JsonObject();

        try {
            String startDate = request.get("fromDate").toString();
            String endDate = request.get("toDate").toString();
            String areaheadId = request.get("areaHeadId").toString();

            if (request.containsKey("sortBy")) {
                JsonArray frList = new JsonArray();

                String sortBy = request.get("sortBy").toString();
                if (sortBy.equalsIgnoreCase("top")) frList = getAllFrPerformers(areaheadId, startDate, endDate, "DESC");
                else frList = getAllFrPerformers(areaheadId, startDate, endDate, "ASC");

                res.add("frList", frList);
                response.addProperty("responseStatus", HttpStatus.OK.value());
                response.addProperty("message", "Data Found");
                response.add("response", res);

            } else {

                JsonArray topFrList = getFrPerformers(areaheadId, startDate, endDate, "DESC");
                JsonArray underFrList = getFrPerformers(areaheadId, startDate, endDate, "ASC");
                res.add("topList", topFrList);
                res.add("underList", underFrList);

                response.addProperty("responseStatus", HttpStatus.OK.value());
                response.addProperty("message", "Data Found");
                response.add("response", res);
            }


        } catch (Exception x) {
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.addProperty("message", "Try after some time");
            x.printStackTrace();
        }


        return response;
    }


}
