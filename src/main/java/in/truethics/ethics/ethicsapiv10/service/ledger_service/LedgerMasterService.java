package in.truethics.ethics.ethicsapiv10.service.ledger_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.GenerateSlugs;
import in.truethics.ethics.ethicsapiv10.common.GenericDTData;
import in.truethics.ethics.ethicsapiv10.common.NumFormat;
import in.truethics.ethics.ethicsapiv10.dto.ClientDetails;
import in.truethics.ethics.ethicsapiv10.dto.ClientsListDTO;
import in.truethics.ethics.ethicsapiv10.dto.masterdto.LedgerMasterDTO;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageProperties;
import in.truethics.ethics.ethicsapiv10.fileConfig.FileStorageService;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerBalanceSummary;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.*;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurChallanRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.pur_repository.TranxPurOrderRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesChallanRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesOrderRepository;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.sales_repository.TranxSalesQuotationRepository;
import in.truethics.ethics.ethicsapiv10.repository.user_repository.UsersRepository;
import in.truethics.ethics.ethicsapiv10.response.GenericDatatable;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LedgerMasterService {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private LedgerMasterRepository repository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    PrincipleRepository principleRepository;
    @Autowired
    PrincipleGroupsRepository principleGroupsRepository;
    @Autowired
    BalancingMethodRepository balancingMethodRepository;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private LedgerMasterRepository ledgerRepository;
    @Autowired
    private LedgerBalanceSummaryRepository balanceSummaryRepository;
    @Autowired
    private LedgerTransactionDetailsRepository transactionDetailsRepository;
    @Autowired
    private LedgerGstDetailsRepository ledgerGstDetailsRepository;
    @Autowired
    private LedgerShippingDetailsRepository ledgerShippingDetailsRepository;
    @Autowired
    private LedgerDeptDetailsRepository ledgerDeptDetailsRepository;
    @Autowired
    private LedgerBillingDetailsRepository ledgerBillingDetailsRepository;

    @Autowired
    private SalesmanMasterRepository salesmanMasterRepository;
    @Autowired
    private GenerateSlugs generateSlugs;
    @Autowired
    private AssociateGroupsRepository associateGroupsRepository;
    @Autowired
    private GstTypeMasterRepository gstMasterRepository;
    @Autowired
    private LedgerBankDetailsRepository ledgerbankDetailsRepository;
    @Autowired
    private LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;
    @Autowired
    private LedgerLicenseDetailsRepository ledgerLicenseDetailsRepository;
    @Autowired
    private NumFormat numFormat;
    private static final Logger ledgerLogger = LogManager.getLogger(LedgerMasterService.class);


    static String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    @Autowired
    private AreaMasterRepository areaMasterRepository;
    @Autowired
    private PaymentModeMasterRepository paymentModeMasterRepository;
    @Autowired
    private TranxPurOrderRepository tranxPurOrderRepository;
    @Autowired
    private TranxPurChallanRepository tranxPurChallanRepository;
    @Autowired
    private TranxSalesQuotationRepository tranxSalesQuotationRepository;
    @Autowired
    private TranxSalesOrderRepository tranxSalesOrderRepository;
    @Autowired
    private TranxSalesChallanRepository tranxSalesChallanRepository;
    @Autowired
    private LedgerOpeningBalanceRepository ledgerOpeningBalanceRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Value("${spring.serversource.url}")
    private String serverUrl;

    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private AreaHeadRepository areaHeadRepository;
    @Autowired
    private LedgerPaymentModeRepository ledgerPaymentModeRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private OutletRepository outletRepository;

    static String num_hash(int num) {
        if (num < 26) return Character.toString(alpha.charAt(num - 1));
        else {
            int q = Math.floorDiv(num, 26);
            int r = num % 26;
            if (r == 0) {
                if (q == 1) {
                    return Character.toString(alpha.charAt((26 + r - 1) % 26));
                } else return num_hash(q - 1) + alpha.charAt((26 + r - 1) % 26);
            } else return num_hash(q) + alpha.charAt((26 + r - 1) % 26);
        }
    }


    public Object createLedgerMaster(MultipartHttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        LedgerMaster mLedger = null;
        ResponseMessage responseMessage = new ResponseMessage();
        LedgerMaster ledgerMaster = new LedgerMaster();
        mLedger = ledgerCreateUpdate(request, "create", ledgerMaster);

        if (mLedger != null) {
//                insertIntoLedgerBalanceSummary(mLedger, "create");
            responseMessage.setMessage("Ledger created successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            responseMessage.setData(mLedger.
                    getId().toString());
        } else {
            responseMessage.setMessage("Error in ledger creation");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
         /*else {
            System.out.println("Already Ledger created with this name or code");
            responseMessage.setMessage("Already Ledger created..");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }*/
        return responseMessage;
    }

    public Object importLedgerMaster(MultipartHttpServletRequest request) {
        ResponseMessage responseObject = new ResponseMessage();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        Principles principles = null;
        PrincipleGroups groups = null;
        Foundations foundations = null;
        GstTypeMaster gstTypeMaster = null;
        try {
            MultipartFile excelFile = request.getFile("ledgerfile");
            if (excelFile != null) {
                XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
                XSSFSheet sheet = workbook.getSheetAt(0);
                System.out.println("Total Rows : " + sheet.getPhysicalNumberOfRows());
                for (int s = 1; s < sheet.getPhysicalNumberOfRows(); s++) {

                    LedgerMaster ledgerMaster = new LedgerMaster();
                    XSSFRow row = sheet.getRow(s);
                    if (row.getCell(1) == null) {
                        break;
                    } else {
                        ledgerMaster = ledgerRepository.findFirstByLedgerNameIgnoreCase(row.getCell(1).toString());
                        if (ledgerMaster == null) {

                            String gstnumber = "";
                            String stateCode = "";

                            ledgerMaster = new LedgerMaster();
                            if (users.getBranch() != null) branch = users.getBranch();

                            ledgerMaster.setIsPrivate(false);
                            ledgerMaster.setIsDeleted(true);
                            ledgerMaster.setStatus(true);
                            ledgerMaster.setIsDefaultLedger(false);
                            ledgerMaster.setOutlet(users.getOutlet());
                            ledgerMaster.setCreatedBy(users.getId());
                            ledgerMaster.setLedgerName(row.getCell(1).toString());
                            ledgerMaster.setMailingName(row.getCell(1).toString());
                            ledgerMaster.setIsLicense(false);
                            ledgerMaster.setIsShippingDetails(false);
                            ledgerMaster.setIsCredit(false);
                            ledgerMaster.setIsDepartment(false);
                            ledgerMaster.setIsBankDetails(false);
                            BalancingMethod balancingMethod = balancingMethodRepository.findByIdAndStatus(1L, true);
                            ledgerMaster.setBalancingMethod(balancingMethod);
                            ledgerMaster.setOpeningBalType(row.getCell(7).toString());
                            ledgerMaster.setAddress(row.getCell(2).toString());
                            String mobile = row.getCell(11) == null ? "" : row.getCell(11).getRawValue();
                            String whatsapp = row.getCell(12) == null ? "" : row.getCell(12).getRawValue();
                            if (mobile != null && !mobile.isEmpty())
                                ledgerMaster.setMobile(Long.parseLong(mobile));
                            else ledgerMaster.setMobile(0L);
                            if (whatsapp != null && !whatsapp.isEmpty())
                                ledgerMaster.setWhatsAppno(Long.parseLong(whatsapp));
                            else ledgerMaster.setWhatsAppno(0L);
                            ledgerMaster.setPincode(0L);
                            if (row.getCell(0) != null) {
                                ledgerMaster.setLedgerCode(row.getCell(0).toString());
                                String underLedger = row.getCell(0).toString().trim();
                                /***** importing parameters for Sundry Creditors *****/
                                if (underLedger.equalsIgnoreCase("D31")) {
                                    groups = principleGroupsRepository.findByIdAndStatus(5L, true);
                                    ledgerMaster.setPrincipleGroups(groups);
                                    ledgerMaster.setPrinciples(groups.getPrinciples());
                                    ledgerMaster.setFoundations(groups.getPrinciples().getFoundations());
                                    ledgerMaster.setUniqueCode(groups.getUniqueCode());
                                    ledgerMaster.setLedgerCode("C" + s);
                                    ledgerMaster.setSlugName("sundry_creditors");
                                    ledgerMaster.setUnderPrefix("PG#5");
                                    ledgerMaster.setOpeningBal(Double.valueOf(row.getCell(8).toString()));
                                } else {
                                    groups = principleGroupsRepository.findByIdAndStatus(1L, true);
                                    ledgerMaster.setPrincipleGroups(groups);
                                    ledgerMaster.setPrinciples(groups.getPrinciples());
                                    ledgerMaster.setFoundations(groups.getPrinciples().getFoundations());
                                    ledgerMaster.setUniqueCode(groups.getUniqueCode());
                                    ledgerMaster.setLedgerCode("D" + s);
                                    ledgerMaster.setSlugName("sundry_debtors");
                                    ledgerMaster.setUnderPrefix("PG#1");
                                    ledgerMaster.setOpeningBal(Double.valueOf(row.getCell(8).toString()) * -1);

                                    /***** importing parameters for Sundry Debtors *****/
                                }
                            }
                            if (row.getCell(4) != null) ledgerMaster.setPancard(row.getCell(4).toString());
                            if (row.getCell(3) != null) {
                                gstnumber = row.getCell(3).toString();
                                ledgerMaster.setGstin(row.getCell(3).toString());
                            }
                            if (row.getCell(5) != null) ledgerMaster.setLicenseNo(row.getCell(5).toString());
                            if (row.getCell(6) != null)
                                ledgerMaster.setCreditDays(Integer.valueOf(row.getCell(6).getRawValue()));
                            if (gstnumber.equalsIgnoreCase("")) {
                                ledgerMaster.setTaxable(false);
                                gstTypeMaster = gstMasterRepository.findById(3L).get();
                                ledgerMaster.setRegistrationType(gstTypeMaster.getId());
                                ledgerMaster.setStateCode(users.getOutlet().getStateCode());
                                ledgerMaster.setState(users.getOutlet().getState());
                            } else {
                                ledgerMaster.setTaxable(true);
                                stateCode = gstnumber.substring(0, 2);
                                ledgerMaster.setStateCode(stateCode);
                                gstTypeMaster = gstMasterRepository.findById(1L).get();
                                ledgerMaster.setRegistrationType(gstTypeMaster.getId());
                                Integer i1 = Integer.parseInt(stateCode);
                                String stCode = "";
                                if (i1 < 10) {
                                    stCode = stateCode.substring(1);//03
                                    stateCode = stCode;
                                }
                                List<State> state = stateRepository.findByStateCode(stateCode);
                                if (state != null) {
                                    ledgerMaster.setState(state.get(0));
                                }
                            }
                            LedgerMaster mLedger = ledgerRepository.save(ledgerMaster);
                            /***** Mapping GSt number into Child table of Ledger *****/
                            if (!gstnumber.equalsIgnoreCase("")) {
                                LedgerGstDetails gstDetails = new LedgerGstDetails();
                                gstDetails.setGstin(gstnumber);
                                gstDetails.setPanCard("");
                                gstDetails.setStateCode(stateCode);
                                gstDetails.setCreatedBy(users.getId());
                                gstDetails.setStatus(true);
                                gstDetails.setLedgerMaster(mLedger);
                                gstDetails.setRegistrationType(gstTypeMaster.getId());
                                try {
                                    ledgerGstDetailsRepository.save(gstDetails);
                                } catch (Exception e) {
                                    System.out.println("Exception in insertIntoGstDetails" + e.getMessage());
                                    e.getMessage();
                                    e.printStackTrace();
                                }
                            }
                            System.out.println(s + ". Ledger : " + row.getCell(1).toString() + " is created successfully!");

                        } else {
                            System.out.println("Ledger : " + row.getCell(1).toString() + " already exist!");
                        }

                    }
                    // No.of rows end
                }

                responseObject.setResponseStatus(200);
                responseObject.setMessage("Ledger import completed successfully!");
            } else {
                responseObject.setResponseStatus(400);
                responseObject.setMessage("Ledger import failed!");
            }
        } catch (Exception x) {
            System.out.println("Error=>" + x.toString());
            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ledgerLogger.error("Exception in ledger import:" + exceptionAsString);
            responseObject.setResponseStatus(400);
            responseObject.setMessage("Ledger import failed!");
        }
        return responseObject;
    }


    public LedgerMaster ledgerCreateUpdate(MultipartHttpServletRequest request, String key, LedgerMaster ledgerMaster) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = request.getParameterMap();
        PrincipleGroups groups = null;
        Principles principles = null;
        Foundations foundations = null;
        State mState = null;
        Country mCountry = null;
        LedgerMaster mLedger = null;
        BalancingMethod bMethod = null;
        try {
            if (key.equalsIgnoreCase("create")) bMethod = balancingMethodRepository.findByIdAndStatus(2L, true);
            ledgerMaster.setBalancingMethod(bMethod);
            if (paramMap.containsKey("principle_id")) {
                principles = principleRepository.findByIdAndStatus(Long.parseLong(request.getParameter("principle_id")), true);
                foundations = principles.getFoundations();
            }
            if (paramMap.containsKey("principle_group_id")) {
                groups = principleGroupsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("principle_group_id")), true);
            }
            /***** Associate Group if available ******/
            if (paramMap.containsKey("associates_id") && !request.getParameter("associates_id").equalsIgnoreCase("")) {
                AssociateGroups associateGroups = associateGroupsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("associates_id")), true);
                if (associateGroups != null) ledgerMaster.setAssociateGroups(associateGroups);
            }
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
            if (paramMap.containsKey("is_private"))
                ledgerMaster.setIsPrivate(Boolean.parseBoolean(request.getParameter("is_private")));
            if (key.equalsIgnoreCase("create")) {
                ledgerMaster.setIsDeleted(true); //isDelete : true means , we can delete this ledger
                // if it is not involved into any tranxs
                ledgerMaster.setStatus(true);
                ledgerMaster.setIsDefaultLedger(false);
            }
            if (users.getBranch() != null) ledgerMaster.setBranch(users.getBranch());
            ledgerMaster.setOutlet(users.getOutlet());
            ledgerMaster.setCreatedBy(users.getId());
            ledgerMaster.setLedgerName(request.getParameter("ledger_name"));
            if (paramMap.containsKey("slug")) ledgerMaster.setSlugName(request.getParameter("slug"));
            else ledgerMaster.setSlugName("");
            if (paramMap.containsKey("under_prefix")) ledgerMaster.setUnderPrefix(request.getParameter("under_prefix"));
            else ledgerMaster.setUnderPrefix("");
            if (request.getParameter("slug").equalsIgnoreCase("sundry_creditors") ||
                    request.getParameter("slug").equalsIgnoreCase("sundry_debtors")) {
                ledgerMaster.setTaxType("");
                if (paramMap.containsKey("supplier_code")) {
                    if (request.getParameter("supplier_code").equalsIgnoreCase("")) ledgerMaster.setLedgerCode(null);
                    else ledgerMaster.setLedgerCode(request.getParameter("supplier_code"));
                }
                if (paramMap.containsKey("mailing_name")) {
                    ledgerMaster.setMailingName(request.getParameter("mailing_name"));
                } else {
                    ledgerMaster.setMailingName("");
                }
                if (paramMap.containsKey("route")) {
                    ledgerMaster.setRoute(request.getParameter("route"));
                } else {
                    ledgerMaster.setRoute("");
                }
                if (paramMap.containsKey("salesmanId")) {
                    SalesManMaster salesManMaster = salesmanMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("salesmanId")), true);
                    ledgerMaster.setColumnA(request.getParameter("salesmanId"));  //columnA= salesman
                    ledgerMaster.setSalesmanId(salesManMaster.getId());
                }
                if (paramMap.containsKey("area")) {
                    AreaMaster areaMaster = areaMasterRepository.findByIdAndStatus(Long.parseLong(request.getParameter("area")), true);
                    ledgerMaster.setArea(request.getParameter("area"));  //columnA= salesman
                    ledgerMaster.setAreaId(areaMaster.getId());
                }
                if (paramMap.containsKey("opening_bal_type")) {
                    ledgerMaster.setOpeningBalType(request.getParameter("opening_bal_type"));
                } else {
                    ledgerMaster.setOpeningBalType("");
                }
                if (paramMap.containsKey("balancing_method")) {
                    BalancingMethod balancingMethod = balancingMethodRepository.findByIdAndStatus(Long.parseLong(request.getParameter("balancing_method")), true);
                    ledgerMaster.setBalancingMethod(balancingMethod);
                }
                if (paramMap.containsKey("address")) {
                    ledgerMaster.setAddress(request.getParameter("address"));
                } else {
                    ledgerMaster.setAddress("");
                }
                if (paramMap.containsKey("state")) {
                    Optional<State> state = stateRepository.findById(Long.parseLong(request.getParameter("state")));
                    mState = state.get();
                    ledgerMaster.setState(mState);
                    System.out.println("State Code-->" + mState.getStateCode());
                    ledgerMaster.setStateCode(mState.getStateCode());
                }
                if (paramMap.containsKey("country")) {
                    Optional<Country> country = countryRepository.findById(Long.parseLong(request.getParameter("country")));
                    mCountry = country.get();
                    ledgerMaster.setCountry(mCountry);
                }
                if (paramMap.containsKey("pincode")) {
                    ledgerMaster.setPincode(Long.valueOf(request.getParameter("pincode")));
                } else {
                    ledgerMaster.setPincode(0L);
                }
                if (paramMap.containsKey("city")) {
                    ledgerMaster.setCity(request.getParameter("city").trim());
                } else {
                    ledgerMaster.setCity("");
                }
                if (paramMap.containsKey("email")) {
                    ledgerMaster.setEmail(request.getParameter("email"));
                } else {
                    ledgerMaster.setEmail("");
                }
                if (paramMap.containsKey("mobile_no")) {
                    ledgerMaster.setMobile(Long.parseLong(request.getParameter("mobile_no").trim()));
                } else {
                    ledgerMaster.setMobile(0l);
                }
                if (paramMap.containsKey("whatsapp_no")) {
                    ledgerMaster.setWhatsAppno(Long.parseLong(request.getParameter("whatsapp_no").trim()));
                } else {
                    ledgerMaster.setWhatsAppno(0l);
                }
                ledgerMaster.setTaxable(Boolean.parseBoolean(request.getParameter("isGST")));
            /*if (Boolean.parseBoolean(request.getParameter("taxable"))) {
                Long registraton_type = Long.valueOf(request.getParameter("registration_type"));
                GstTypeMaster gstTypeMaster = gstMasterRepository.findById(registraton_type).get();
                ledgerMaster.setRegistrationType(gstTypeMaster.getId());
            } else {*/
                if (Boolean.parseBoolean(request.getParameter("isGST")) == false) {
                    GstTypeMaster gstTypeMaster = gstMasterRepository.findById(3L).get();
                    ledgerMaster.setRegistrationType(gstTypeMaster.getId());
                    if (mState != null) ledgerMaster.setStateCode(mState.getStateCode());
                    if (paramMap.containsKey("pan_no")) {
                        ledgerMaster.setPancard(request.getParameter("pan_no"));
                    } else {
                        ledgerMaster.setPancard("");
                    }
                }
                //}
                if (request.getParameter("slug").equalsIgnoreCase("sundry_creditors") || request.getParameter("slug").equalsIgnoreCase("sundry_debtors")) {
                    if (paramMap.containsKey("bank_name")) ledgerMaster.setBankName(request.getParameter("bank_name"));
                    else ledgerMaster.setBankName("");
                    if (paramMap.containsKey("account_no"))
                        ledgerMaster.setAccountNumber(request.getParameter("account_no"));
                    else ledgerMaster.setAccountNumber("");
                    if (paramMap.containsKey("ifsc_code")) ledgerMaster.setIfsc(request.getParameter("ifsc_code"));
                    else ledgerMaster.setIfsc("");
                    if (paramMap.containsKey("bank_branch"))
                        ledgerMaster.setBankBranch(request.getParameter("bank_branch"));
                    else ledgerMaster.setBankBranch("");
//                    ledgerMaster.setMailingName("");

                    if (paramMap.containsKey("defaultBank"))
                        ledgerMaster.setColumnR(Boolean.parseBoolean(request.getParameter("defaultBank")));
                    if (paramMap.containsKey("opening_bal") &&
                            !request.getParameter("opening_bal").equalsIgnoreCase("")) {
                        ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal").trim()));
                    /*if (request.getParameter("opening_bal_type").equalsIgnoreCase("Cr")) {
                        ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal").trim()));
                    } else {
                        Double openingBal = Double.parseDouble(request.getParameter("opening_bal").trim());
                        openingBal *= -1;
                        ledgerMaster.setOpeningBal(openingBal);
                    }*/
                    } else {
                        ledgerMaster.setOpeningBal(0.00);
                    }
                } else {
                    ledgerMaster.setBankName("");
                    ledgerMaster.setAccountNumber("");
                    ledgerMaster.setIfsc("");
                    ledgerMaster.setBankBranch("");
                    if (paramMap.containsKey("mailing_name")) {
                        ledgerMaster.setMailingName(request.getParameter("mailing_name"));
                    }
                    if (paramMap.containsKey("opening_bal") && !request.getParameter("opening_bal").equalsIgnoreCase("")) {
                        ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal")));
                    /*if (request.getParameter("opening_bal_type").equalsIgnoreCase("Dr")) {
                        Double openingBal = Double.parseDouble(request.getParameter("opening_bal").trim());
                        openingBal *= -1;
                        ledgerMaster.setOpeningBal(openingBal);
                    } else {
                        ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal").trim()));
                    }*/
                    } else {
                        ledgerMaster.setOpeningBal(0.00);
                    }
                }
                /* pune demo visit changes */
                ledgerMaster.setIsCredit(Boolean.parseBoolean(request.getParameter("isCredit")));
                if (paramMap.containsKey("credit_days") && Boolean.parseBoolean(request.getParameter("isCredit"))) {
                    ledgerMaster.setCreditDays(Integer.parseInt(request.getParameter("credit_days").trim()));
                    ledgerMaster.setApplicableFrom(request.getParameter("applicable_from"));
                    if (paramMap.containsKey("creditNumBills"))
                        ledgerMaster.setCreditNumBills(Double.parseDouble(request.getParameter("creditNumBills")));
                    if (paramMap.containsKey("creditBillValue"))
                        ledgerMaster.setCreditBillValue(Double.parseDouble(request.getParameter("creditBillValue")));
                    if (paramMap.containsKey("lrBillDate"))
                        ledgerMaster.setLrBillDate(LocalDate.parse(request.getParameter("lrBillDate")));
                    if (paramMap.containsKey("creditBillDate"))
                        ledgerMaster.setCreditBillDate(LocalDate.parse(request.getParameter("creditBillDate")));

                } else {
                    ledgerMaster.setCreditDays(0);
                    ledgerMaster.setApplicableFrom("");
                }
                /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
                if (paramMap.containsKey("tds")) ledgerMaster.setTds(Boolean.parseBoolean(request.getParameter("tds")));
                if (paramMap.containsKey("tds_applicable_date"))
                    ledgerMaster.setTdsApplicableDate(LocalDate.parse(request.getParameter("tds_applicable_date")));
                if (paramMap.containsKey("tcs")) ledgerMaster.setTcs(Boolean.parseBoolean(request.getParameter("tcs")));
                if (paramMap.containsKey("tcs_applicable_date"))
                    ledgerMaster.setTcsApplicableDate(LocalDate.parse(request.getParameter("tcs_applicable_date")));
                if (paramMap.containsKey("area")) ledgerMaster.setArea(request.getParameter("area"));
                if (paramMap.containsKey("landmark")) ledgerMaster.setArea(request.getParameter("landmark"));
                if (paramMap.containsKey("salesrate"))
                    ledgerMaster.setSalesRate(Double.parseDouble(request.getParameter("salesrate")));
                /******* License Details ******/

                ledgerMaster.setIsLicense(Boolean.parseBoolean(request.getParameter("isLicense")));
                if (paramMap.containsKey("licenseNo")) {
                    ledgerMaster.setLicenseNo(request.getParameter("licenseNo"));
                }
                if (paramMap.containsKey("reg_date") && !request.getParameter("reg_date").equalsIgnoreCase("")) {
                    ledgerMaster.setLicenseExpiry(LocalDate.parse(request.getParameter("reg_date")));
                } else {
                    ledgerMaster.setLicenseExpiry(null);
                }
                if (paramMap.containsKey("gstTransferDate"))
                    ledgerMaster.setGstTransferDate(LocalDate.parse(request.getParameter("gstTransferDate")));
                if (paramMap.containsKey("place")) ledgerMaster.setPlace(request.getParameter("place"));
                if (paramMap.containsKey("route")) ledgerMaster.setRoute(request.getParameter("route"));
                if (paramMap.containsKey("district")) ledgerMaster.setDistrict(request.getParameter("district"));
                if (paramMap.containsKey("businessType"))
                    ledgerMaster.setBusinessType(request.getParameter("businessType"));
                if (paramMap.containsKey("businessTrade"))
                    ledgerMaster.setBusinessTrade(request.getParameter("businessTrade"));
                if (paramMap.containsKey("anniversary"))
                    ledgerMaster.setAnniversary(LocalDate.parse(request.getParameter("anniversary")));
                if (paramMap.containsKey("dob")) ledgerMaster.setDob(LocalDate.parse(request.getParameter("dob")));
                ledgerMaster.setIsBankDetails(Boolean.parseBoolean(request.getParameter("isBankDetails")));
                ledgerMaster.setIsDepartment(Boolean.parseBoolean(request.getParameter("isDepartment")));
                ledgerMaster.setIsShippingDetails(Boolean.parseBoolean(request.getParameter("isShippingDetails")));

                if (request.getParameter("slug").equalsIgnoreCase("sundry_debtors")) {
                    if (paramMap.containsKey("districtId")) {
                        ledgerMaster.setDistrictHeadId(Long.valueOf(request.getParameter("districtId")));
                    }
                    if (paramMap.containsKey("regionalId")) {
                        ledgerMaster.setRegionalHeadId(Long.valueOf(request.getParameter("regionalId")));
                    }
                    if (paramMap.containsKey("stateId")) {
                        ledgerMaster.setStateHeadId(Long.valueOf(request.getParameter("stateId")));
                    }
                    if (paramMap.containsKey("zoneId")) {
                        ledgerMaster.setZonalHeadId(Long.valueOf(request.getParameter("zoneId")));
                    }

                }


                /*** END ****/
            } else if (request.getParameter("slug").equalsIgnoreCase("bank_account")) {
                if (paramMap.containsKey("opening_bal_type")) {
                    ledgerMaster.setOpeningBalType(request.getParameter("opening_bal_type"));
                }
                if (paramMap.containsKey("opening_bal") && !request.getParameter("opening_bal").equalsIgnoreCase("")) {
                    ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal")));
               /* if (request.getParameter("opening_bal_type").equalsIgnoreCase("Dr")) {
                    Double openingBal = Double.parseDouble(request.getParameter("opening_bal"));
                    openingBal *= -1;
                    ledgerMaster.setOpeningBal(openingBal);
                } else {
                    ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal")));
                }*/
                } else {
                    ledgerMaster.setOpeningBal(0.00);
                }
                if (paramMap.containsKey("state")) {
                    Optional<State> state = stateRepository.findById(Long.parseLong(request.getParameter("state")));
                    mState = state.get();
                    ledgerMaster.setState(mState);

                }
                if (paramMap.containsKey("country")) {
                    Optional<Country> country = countryRepository.findById(Long.parseLong(request.getParameter("country")));
                    mCountry = country.get();
                    ledgerMaster.setCountry(mCountry);
                }
                if (paramMap.containsKey("pincode")) {
                    ledgerMaster.setPincode(Long.parseLong(request.getParameter("pincode").trim()));
                }
                if (paramMap.containsKey("email")) {
                    ledgerMaster.setEmail(request.getParameter("email"));
                }
                if (paramMap.containsKey("address")) {
                    ledgerMaster.setAddress(request.getParameter("address"));
                } else {
                    ledgerMaster.setAddress("");
                }
                if (paramMap.containsKey("mobile_no")) {
                    ledgerMaster.setMobile(Long.parseLong(request.getParameter("mobile_no").trim()));
                }
                ledgerMaster.setTaxable(Boolean.parseBoolean(request.getParameter("taxable")));
                if (Boolean.parseBoolean(request.getParameter("taxable"))) {
                    ledgerMaster.setGstin(request.getParameter("gstin"));
                    if (paramMap.containsKey("gstType")) {
                        GstTypeMaster gstTypeMaster = gstMasterRepository.findById(Long.parseLong(request.getParameter("gstType"))).get();
                        ledgerMaster.setRegistrationType(gstTypeMaster.getId());
                    }
                } else {
                    GstTypeMaster gstTypeMaster = gstMasterRepository.findById(3L).get();
                    ledgerMaster.setRegistrationType(gstTypeMaster.getId());
                }
                if (paramMap.containsKey("pan_no")) {
                    ledgerMaster.setPancard(request.getParameter("pan_no"));
                } else {
                    ledgerMaster.setPancard("");
                }
                if (paramMap.containsKey("bank_name")) ledgerMaster.setBankName(request.getParameter("bank_name"));
                else ledgerMaster.setBankName("");
                if (paramMap.containsKey("account_no"))
                    ledgerMaster.setAccountNumber(request.getParameter("account_no"));
                else ledgerMaster.setAccountNumber("");
                if (paramMap.containsKey("ifsc_code")) ledgerMaster.setIfsc(request.getParameter("ifsc_code"));
                else ledgerMaster.setIfsc("");
                if (paramMap.containsKey("bank_branch"))
                    ledgerMaster.setBankBranch(request.getParameter("bank_branch"));
                else ledgerMaster.setBankBranch("");
                ledgerMaster.setMailingName("");
                ledgerMaster.setStateCode("");
                if (paramMap.containsKey("defaultBank"))
                    ledgerMaster.setColumnR(Boolean.parseBoolean(request.getParameter("defaultBank")));
            } else if (request.getParameter("slug").equalsIgnoreCase("duties_taxes")) {
                ledgerMaster.setTaxType(request.getParameter("tax_type"));
                ledgerMaster.setMailingName("");
                if (paramMap.containsKey("opening_bal_type")) {
                    ledgerMaster.setOpeningBalType(request.getParameter("opening_bal_type"));
                }
                if (paramMap.containsKey("opening_bal") && !request.getParameter("opening_bal").equalsIgnoreCase("")) {
                    ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal")));
            /*    if (request.getParameter("opening_bal_type").equalsIgnoreCase("Dr")) {
                    ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal").trim()));
                } else {
                    Double openingBal = Double.parseDouble(request.getParameter("opening_bal").trim());
                    openingBal *= -1;
                    ledgerMaster.setOpeningBal(openingBal);
                }*/
                } else {
                    ledgerMaster.setOpeningBal(0.0);
                }
                ledgerMaster.setAddress("");
                ledgerMaster.setTaxable(false);
                ledgerMaster.setGstin("");
                ledgerMaster.setRegistrationType(0L);
                ledgerMaster.setPancard("");
                ledgerMaster.setBankName("");
                ledgerMaster.setAccountNumber("");
                ledgerMaster.setIfsc("");
                ledgerMaster.setBankBranch("");
                ledgerMaster.setStateCode("");
            } else if (request.getParameter("slug").equalsIgnoreCase("others")) {
                if (paramMap.containsKey("pincode")) {
                    ledgerMaster.setPincode(Long.parseLong(request.getParameter("pincode").trim()));
                } /*else {
                ledgerMaster.setPincode(0L);
            }*/
                if (paramMap.containsKey("address")) {
                    ledgerMaster.setAddress(request.getParameter("address"));
                } else {
                    ledgerMaster.setAddress("");
                }
                if (paramMap.containsKey("mobile_no")) {
                    ledgerMaster.setMobile(Long.parseLong(request.getParameter("mobile_no").trim()));
                } /*else {
                ledgerMaster.setMobile(0L);
            }*/
                if (paramMap.containsKey("opening_bal_type")) {
                    ledgerMaster.setOpeningBalType(request.getParameter("opening_bal_type"));
                }
                if (paramMap.containsKey("opening_bal") && !request.getParameter("opening_bal").equalsIgnoreCase("")) {
                    ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal")));
                /*if (request.getParameter("opening_bal_type").equalsIgnoreCase("Dr")) {
                    ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal").trim()));
                } else {
                    Double openingBal = Double.parseDouble(request.getParameter("opening_bal").trim());
                    openingBal *= -1;
                    ledgerMaster.setOpeningBal(openingBal);
                }*/
                } else {
                    ledgerMaster.setOpeningBal(0.00);
                }
                ledgerMaster.setTaxType("");
                ledgerMaster.setMailingName("");
                ledgerMaster.setTaxable(false);
                ledgerMaster.setGstin("");
                ledgerMaster.setRegistrationType(0L);
                ledgerMaster.setPancard("");
                ledgerMaster.setBankName("");
                ledgerMaster.setAccountNumber("");
                ledgerMaster.setIfsc("");
                ledgerMaster.setBankBranch("");
                ledgerMaster.setStateCode("");
            } else if (request.getParameter("slug").equalsIgnoreCase("assets")) {
                if (paramMap.containsKey("opening_bal_type")) {
                    ledgerMaster.setOpeningBalType(request.getParameter("opening_bal_type"));
                } else {
                    ledgerMaster.setOpeningBalType("");
                }
                if (paramMap.containsKey("opening_bal") && !request.getParameter("opening_bal").equalsIgnoreCase("")) {
                    ledgerMaster.setOpeningBal(Double.parseDouble(request.getParameter("opening_bal")));
                } else {
                    ledgerMaster.setOpeningBal(0.00);
                }
                ledgerMaster.setTaxType("");
                ledgerMaster.setMailingName("");
                ledgerMaster.setTaxable(false);
                ledgerMaster.setGstin("");
                ledgerMaster.setRegistrationType(0L);
                ledgerMaster.setPancard("");
                ledgerMaster.setBankName("");
                ledgerMaster.setAccountNumber("");
                ledgerMaster.setIfsc("");
                ledgerMaster.setBankBranch("");
                ledgerMaster.setStateCode("");
                ledgerMaster.setAddress("");

            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ledgerLogger.error("Exception in ledgerCreateUpdate:" + exceptionAsString);
        }
        try {
            mLedger = repository.save(ledgerMaster); ///automatic trigger call : balance summary
            if (mLedger.getLedgerCode() == null && (mLedger.getUniqueCode().equalsIgnoreCase("SUCR") ||
                    mLedger.getUniqueCode().equalsIgnoreCase("SUDR"))) {
                long indexofLedger = 0;
                indexofLedger = mLedger.getId();
                String ans = num_hash((int) indexofLedger);
                mLedger.setLedgerCode(ans);
                repository.save(mLedger);
            }
            LedgerBalanceSummary mBalance = null;
            if (key.equalsIgnoreCase("edit")) {
                mBalance = balanceSummaryRepository.findByLedgerMasterId(mLedger.getId());
                    if (mLedger != null) {
                    mBalance.setPrinciples(mLedger.getPrinciples());
                    mBalance.setFoundations(mLedger.getFoundations());
                    mBalance.setPrincipleGroups(mLedger.getPrincipleGroups());
                    mBalance.setOpeningBal(mLedger.getOpeningBal());
                    mBalance.setUnderPrefix(mLedger.getUnderPrefix());
                    balanceSummaryRepository.save(mBalance);
                }


            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ledgerLogger.error("Exception in ledgerCreateUpdate:" + exceptionAsString);
        }
        if (paramMap.containsKey("gstdetails") && Boolean.parseBoolean(request.getParameter("isGST"))) {
            if (key.equalsIgnoreCase("create")) insertIntoGstDetails(mLedger, request);
            else updateGstDetails(mLedger, request);
        }
        if (paramMap.containsKey("shippingDetails") && Boolean.parseBoolean(request.getParameter("isShippingDetails"))) {
            if (key.equalsIgnoreCase("create")) insertIntoShippingDetails(mLedger, request);
            else updateShippingDetails(mLedger, request);
        }
        if (paramMap.containsKey("deptDetails") && Boolean.parseBoolean(request.getParameter("isDepartment"))) {
            if (key.equalsIgnoreCase("create")) insertIntoDeptDetails(mLedger, request);
            else updateDeptDetails(mLedger, request);
        }
        if (paramMap.containsKey("billingDetails")) {
            if (key.equalsIgnoreCase("create")) insertIntoBillingDetails(mLedger, request);
            else updateBillingDetails(mLedger, request);
        }
        if (paramMap.containsKey("licensesDetails") && Boolean.parseBoolean(request.getParameter("isLicense"))) {
            if (key.equalsIgnoreCase("create")) insertIntoLicenseDetails(mLedger, request);
            else updateLicenseDetails(mLedger, request);
        }
        if (paramMap.containsKey("bankDetails") && Boolean.parseBoolean(request.getParameter("isBankDetails"))) {
            if (key.equalsIgnoreCase("create")) insertIntobankDetails(mLedger, request);
            else updateBankDetails(mLedger, request);
        }
        if (paramMap.containsKey("payment_modes")) {
            if (key.equalsIgnoreCase("create")) insertIntoPaymentMode(mLedger, request);
            else updatePaymentMode(mLedger, request);
        }
        /*** opening balance of ledgers with invoice information ***/
        if (paramMap.containsKey("opening_bal_invoice_list")) {
            try {
                if (key.equalsIgnoreCase("create")) {
                    String strJson = request.getParameter("opening_bal_invoice_list");
                    JsonParser parser = new JsonParser();
                    JsonElement gstElements = parser.parse(strJson);
                    JsonArray openingDetailsJson = gstElements.getAsJsonArray();
                    insertOpeningBalanceWithInvoice(mLedger, openingDetailsJson, users.getId());
                    /**
                     * @implNote validation of Ledger Delete , if any tranx done for this ledger, user cant delete this ledger **
                     * @auther ashwins@opethic.com
                     * @version sprint 21
                     **/
                    LedgerMaster ledgerMst = ledgerRepository.findByIdAndStatus(mLedger.getId(), true);
                    ledgerMaster.setIsDeleted(false);
                    ledgerRepository.save(ledgerMst);
                } else {
                    updateOpeningBalance(mLedger, request, users.getId());
                    LedgerMaster ledgerMst = ledgerRepository.findByIdAndStatus(mLedger.getId(), true);
                    ledgerMaster.setIsDeleted(false);
                    ledgerRepository.save(ledgerMst);
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                ledgerLogger.error("Exception in ledgerCreateUpdate:" + exceptionAsString);
            }
        }
        return mLedger;
    }

    private void updatePaymentMode(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("payment_modes");
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray paymentmodeDetailsJson = gstElements.getAsJsonArray();
        if (paymentmodeDetailsJson.size() > 0) {
            for (JsonElement mList : paymentmodeDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                Long detailsId = object.get("detailsId").getAsLong();
                LedgerPaymentModeDetails paymentmodeDetails = new LedgerPaymentModeDetails();
                if (detailsId != 0) {
                    paymentmodeDetails = ledgerPaymentModeRepository.findByIdAndStatus(detailsId, true);
                    if (object.get("value").getAsInt() == 0L) {
                        paymentmodeDetails.setStatus(false);
                    }
                } else {
                    if (object.get("value").getAsInt() == 1L) {
                        paymentmodeDetails.setStatus(true);
                        paymentmodeDetails.setPaymentModeMasterId(object.get("id").getAsLong());
                        paymentmodeDetails.setLedgerId(mLedger.getId());
                        paymentmodeDetails.setCreatedBy(mLedger.getCreatedBy());
                    }
                }
                ledgerPaymentModeRepository.save(paymentmodeDetails);
            }
        }
    }

    private void insertIntoPaymentMode(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("payment_modes");
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray paymentmodeDetailsJson = gstElements.getAsJsonArray();
        if (paymentmodeDetailsJson.size() > 0) {
            for (JsonElement mList : paymentmodeDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                if (object.get("value").getAsInt() == 1) {
                    LedgerPaymentModeDetails paymentmodeDetails = new LedgerPaymentModeDetails();
                    paymentmodeDetails.setStatus(true);
                    paymentmodeDetails.setPaymentModeMasterId(object.get("id").getAsLong());
                    paymentmodeDetails.setLedgerId(mLedger.getId());
                    paymentmodeDetails.setCreatedBy(mLedger.getCreatedBy());
                    ledgerPaymentModeRepository.save(paymentmodeDetails);
                }
            }
        }
    }

    private void updateOpeningBalance(LedgerMaster mLedger, HttpServletRequest request, Long usersId) {
        String strJson = request.getParameter("opening_bal_invoice_list");
        JsonParser parser = new JsonParser();
        JsonElement openingElement = parser.parse(strJson);
        JsonArray openingDetailsJson = openingElement.getAsJsonArray();
        LedgerOpeningBalance openingDetails = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        if (openingDetailsJson.size() > 0) {
            for (JsonElement mList : openingDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                if (object.get("id").getAsLong() > 0) {
                    openingDetails = ledgerOpeningBalanceRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
                } else {
                    openingDetails = new LedgerOpeningBalance();
                    openingDetails.setStatus(true);
                }
                openingDetails.setInvoice_no(object.get("invoice_no").getAsString());
                if (object.has("invoice_date") &&
                        !object.get("invoice_date").getAsString().toLowerCase().contains("invalid"))
                    openingDetails.setInvoice_date(LocalDate.parse(object.get("invoice_date").getAsString()));
                if (object.get("due_days").getAsString().equalsIgnoreCase(""))
                    openingDetails.setDue_days(0L);
                else
                    openingDetails.setDue_days(Long.parseLong(object.get("due_days").getAsString()));
                if (object.get("bill_amt").getAsString().equalsIgnoreCase(""))
                    openingDetails.setBill_amt(0.0);
                else
                    openingDetails.setBill_amt(Double.parseDouble(object.get("bill_amt").getAsString()));
                if (object.get("invoice_paid_amt").getAsString().equalsIgnoreCase(""))
                    openingDetails.setInvoice_paid_amt(0.0);
                else
                    openingDetails.setInvoice_paid_amt(Double.parseDouble(object.get("invoice_paid_amt").getAsString()));
                if (object.get("invoice_bal_amt").getAsString().equalsIgnoreCase(""))
                    openingDetails.setInvoice_bal_amt(0.0);
                else
                    openingDetails.setInvoice_bal_amt(Double.parseDouble(object.get("invoice_bal_amt").getAsString()));
                openingDetails.setLedgerId(mLedger.getId());
                //! Javafx LedgerUpdate Opening Balance Type
                if (object.get("type") instanceof JsonObject) {
                    openingDetails.setBalancingType(object.get("type").getAsJsonObject().get("label").getAsString());
                } else {
                    openingDetails.setBalancingType(object.get("type").getAsString());
                }

                openingDetails.setUpdatedBy(usersId);
                ledgerOpeningBalanceRepository.save(openingDetails);
            }
        }
        /* Remove from existing and set status false */
        if (paramMap.containsKey("removeOpeningList")) {
            String removeOpeningDetails = request.getParameter("removeOpeningList");
            JsonElement removeOpeningElement = parser.parse(removeOpeningDetails);
            JsonArray removeOpeningJson = removeOpeningElement.getAsJsonArray();
            LedgerOpeningBalance mOpeningDetails = null;
            if (removeOpeningJson.size() > 0) {
                for (JsonElement mList : removeOpeningJson) {
                    Long object = mList.getAsLong();
                    if (object != 0) {
                        mOpeningDetails = ledgerOpeningBalanceRepository.findByIdAndStatus(object, true);
                        if (mOpeningDetails != null) mOpeningDetails.setStatus(false);
                        try {
                            ledgerOpeningBalanceRepository.save(mOpeningDetails);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            ledgerLogger.error("Exception in updateDeptDetails:" + exceptionAsString);
                        }
                    }
                }
            }
        }
    }

    private void insertOpeningBalanceWithInvoice(LedgerMaster mLedger, JsonArray openingDetailsJson, Long userId) {
        if (openingDetailsJson.size() > 0) {
            for (JsonElement mList : openingDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                LedgerOpeningBalance openingBalDetails = new LedgerOpeningBalance();
                openingBalDetails.setStatus(true);
                openingBalDetails.setInvoice_no(object.get("invoice_no").getAsString());
                if (object.has("invoice_date") &&
                        !object.get("invoice_date").getAsString().toLowerCase().contains("invalid"))
                    openingBalDetails.setInvoice_date(LocalDate.parse(object.get("invoice_date").getAsString()));
                if (object.get("due_days").getAsString().equalsIgnoreCase(""))
                    openingBalDetails.setDue_days(0L);
                else
                    openingBalDetails.setDue_days(Long.parseLong(object.get("due_days").getAsString()));
                if (object.get("bill_amt").getAsString().equalsIgnoreCase(""))
                    openingBalDetails.setBill_amt(0.0);
                else
                    openingBalDetails.setBill_amt(Double.parseDouble(object.get("bill_amt").getAsString()));
                if (object.get("invoice_paid_amt").getAsString().equalsIgnoreCase(""))
                    openingBalDetails.setInvoice_paid_amt(0.0);
                else
                    openingBalDetails.setInvoice_paid_amt(Double.parseDouble(object.get("invoice_paid_amt").getAsString()));
                if (object.get("invoice_bal_amt").getAsString().equalsIgnoreCase(""))
                    openingBalDetails.setInvoice_bal_amt(0.0);
                else
                    openingBalDetails.setInvoice_bal_amt(Double.parseDouble(object.get("invoice_bal_amt").getAsString()));
                openingBalDetails.setLedgerId(mLedger.getId());
                openingBalDetails.setBalancingType(object.get("type").getAsJsonObject().get("label").getAsString());
                openingBalDetails.setCreatedBy(userId);
                ledgerOpeningBalanceRepository.save(openingBalDetails);
            }
        }
    }

    private void updateLicenseDetails(LedgerMaster mLedger, MultipartHttpServletRequest request) {
        String licenseJson = request.getParameter("licensesDetails");
        JsonParser parser = new JsonParser();
        FileStorageProperties fileStorageProperties = new FileStorageProperties();

        JsonElement licenseElements = parser.parse(licenseJson);
        JsonArray licenseDetails = licenseElements.getAsJsonArray();
//        LedgerLicenseDetails mLicense = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        if (licenseDetails.size() > 0) {
//            for (JsonElement mList : licenseDetails) {
//                JsonObject object = mList.getAsJsonObject();
//                if (object.get("lid").getAsLong() > 0) {
//                    mLicense = ledgerLicenseDetailsRepository.findByIdAndStatus(object.get("lid").getAsLong(), true);
//                    mLicense.setUpdatedBy(mLedger.getUpdatedBy());
//                } else {
//                    mLicense = new LedgerLicenseDetails();
//                    mLicense.setStatus(true);
//                    mLicense.setCreatedBy(mLedger.getCreatedBy());
//                }
//                mLicense.setLicenseNum(object.get("licenses_num").getAsString());
//                if (object.has("licenses_exp") && !object.get("licenses_exp").getAsString().equalsIgnoreCase("")) {
//                    mLicense.setLicenseExp(LocalDate.parse(object.get("licenses_exp").getAsString()));
//                } else {
//                    mLicense.setLicenseExp(null);
//                }
//
//                mLicense.setSlugName(object.get("licences_type").getAsJsonObject().get("slug_name").getAsString());
//                mLicense.setLedgerMaster(mLedger);
//                ledgerLicenseDetailsRepository.save(mLicense);
//            }
            for (int i = 0; i < licenseDetails.size(); i++) {
                JsonObject object = licenseDetails.get(i).getAsJsonObject();
                LedgerLicenseDetails mLicense;
                if (object.get("lid").getAsLong() > 0) {
                    mLicense = ledgerLicenseDetailsRepository.findByIdAndStatus(object.get("lid").getAsLong(), true);
                    mLicense.setUpdatedBy(mLedger.getUpdatedBy());
                } else {
                    mLicense = new LedgerLicenseDetails();
                    mLicense.setCreatedBy(mLedger.getCreatedBy());
                }
                mLicense.setLicenseNum(object.get("licenses_num").getAsString());
                if (object.has("licenses_exp") && !object.get("licenses_exp").getAsString().equalsIgnoreCase(""))
                    mLicense.setLicenseExp(LocalDate.parse(object.get("licenses_exp").getAsString()));
                mLicense.setSlugName(object.get("licences_type").getAsJsonObject().get("slug_name").getAsString());

                if (request.getFile("license_doc_upload" + i) != null) {
                    MultipartFile image = request.getFile("license_doc_upload" + i);
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    System.out.println("imagepath" + imagePath);
                    if (imagePath != null) {
                        mLicense.setLicenseDocUpload(File.separator + "uploads" + File.separator + imagePath);
                    }
                } else {
                    if (object.has("license_doc_upload_old")) {
                        mLicense.setLicenseDocUpload(object.get("license_doc_upload_old").getAsString());
                    } else {
                        mLicense.setLicenseDocUpload("");
                    }
                }
                mLicense.setStatus(true);
                mLicense.setLedgerMaster(mLedger);
                ledgerLicenseDetailsRepository.save(mLicense);

            }
        }
        /* Remove from existing and set status false */
        if (paramMap.containsKey("removelicensesList")) {
            String removeLicenseDetails = request.getParameter("removelicensesList");
            JsonElement removeLicenseElement = parser.parse(removeLicenseDetails);
            JsonArray removeJsonLicense = removeLicenseElement.getAsJsonArray();
            LedgerLicenseDetails mLicenseDetails = null;
            if (removeJsonLicense.size() > 0) {
                for (JsonElement mList : removeJsonLicense) {
                    Long object = mList.getAsLong();
                    if (object != 0) {
                        mLicenseDetails = ledgerLicenseDetailsRepository.findByIdAndStatus(object, true);
                        if (mLicenseDetails != null) mLicenseDetails.setStatus(false);
                        try {
                            ledgerLicenseDetailsRepository.save(mLicenseDetails);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            ledgerLogger.error("Exception in updateLicenseDetails:" + exceptionAsString);
                        }
                    }
                }
            }
        }
    }

    private void insertIntoLicenseDetails(LedgerMaster mLedger, MultipartHttpServletRequest request) {
        String licenseJson = request.getParameter("licensesDetails");
        JsonParser parser = new JsonParser();
        JsonElement licenseElements = parser.parse(licenseJson);
        FileStorageProperties fileStorageProperties = new FileStorageProperties();

        JsonArray licenseDetails = licenseElements.getAsJsonArray();
        if (licenseDetails.size() > 0) {
            for (int i = 0; i < licenseDetails.size(); i++) {
                JsonObject object = licenseDetails.get(i).getAsJsonObject();
                LedgerLicenseDetails mLicense = new LedgerLicenseDetails();
                mLicense.setLicenseNum(object.get("licenses_num").getAsString());
                if (object.has("licenses_exp") && !object.get("licenses_exp").getAsString().equalsIgnoreCase(""))
                    mLicense.setLicenseExp(LocalDate.parse(object.get("licenses_exp").getAsString()));
                mLicense.setSlugName(object.get("licences_type").getAsJsonObject().get("slug_name").getAsString());

                if (request.getFile("license_doc_upload" + i) != null) {
                    MultipartFile image = request.getFile("license_doc_upload" + i);
                    fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator);
                    String imagePath = fileStorageService.storeFile(image, fileStorageProperties);
                    System.out.println("imagepath" + imagePath);
                    if (imagePath != null) {
                        mLicense.setLicenseDocUpload(File.separator + "uploads" + File.separator + imagePath);
                    }
                }
                mLicense.setStatus(true);
                mLicense.setCreatedBy(mLedger.getCreatedBy());
                mLicense.setLedgerMaster(mLedger);
                ledgerLicenseDetailsRepository.save(mLicense);
            }
        }
    }

    private void updateBankDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("bankDetails");
        JsonParser parser = new JsonParser();
        JsonElement bankElements = parser.parse(strJson);
        JsonArray bankDetailsJson = bankElements.getAsJsonArray();
        LedgerBankDetails bankDetails = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        if (bankDetailsJson.size() > 0) {
            for (JsonElement mList : bankDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                if (object.get("bid").getAsLong() > 0) {
                    bankDetails = ledgerbankDetailsRepository.findByIdAndStatus(object.get("bid").getAsLong(), true);
                } else {
                    bankDetails = new LedgerBankDetails();
                    bankDetails.setStatus(true);
                }
                bankDetails.setBankName(object.get("bank_name").getAsString());
                bankDetails.setAccountNo(object.get("bank_account_no").getAsString());
                bankDetails.setIfsc(object.get("bank_ifsc_code").getAsString());
                bankDetails.setBankBranch(object.get("bank_branch").getAsString());
                bankDetails.setCreatedBy(mLedger.getCreatedBy());
                bankDetails.setLedgerMaster(mLedger);
                ledgerbankDetailsRepository.save(bankDetails);
            }
        }
        /* Remove from existing and set status false */
        if (paramMap.containsKey("removeBankList")) {
            String removeBankDetails = request.getParameter("removeBankList");
            JsonElement removeBankElements = parser.parse(removeBankDetails);
            JsonArray removeDeptJson = removeBankElements.getAsJsonArray();
            LedgerBankDetails mBankDetails = null;
            if (removeDeptJson.size() > 0) {
                for (JsonElement mList : removeDeptJson) {
                    Long object = mList.getAsLong();
                    if (object != 0) {
                        mBankDetails = ledgerbankDetailsRepository.findByIdAndStatus(object, true);
                        if (mBankDetails != null) mBankDetails.setStatus(false);
                        try {
                            ledgerbankDetailsRepository.save(mBankDetails);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            ledgerLogger.error("Exception in updateBankDetails:" + exceptionAsString);
                        }
                    }
                }
            }
        }
    }

    private void updateBillingDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("billingDetails");
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray billDetailsJson = gstElements.getAsJsonArray();
        LedgerBillingDetails billDetails = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        if (billDetailsJson.size() > 0) {
            for (JsonElement mList : billDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                if (object.get("id").getAsLong() != 0) {
                    billDetails = ledgerBillingDetailsRepository.findByIdAndStatus(object.get("id").getAsLong(), true);
                } else {
                    billDetails = new LedgerBillingDetails();
                    billDetails.setStatus(true);
                }
                billDetails.setDistrict(object.get("district").getAsString());
                billDetails.setBillingAddress(object.get("billing_address").getAsString());
                billDetails.setCreatedBy(mLedger.getCreatedBy());
                billDetails.setLedgerMaster(mLedger);
                ledgerBillingDetailsRepository.save(billDetails);
            }
        }
        /* Remove from existing and set status false */
        if (paramMap.containsKey("removeBillingList")) {
            String removeBillingDetails = request.getParameter("removeBillingList");
            JsonElement removeBillingElements = parser.parse(removeBillingDetails);
            JsonArray removeBillingJson = removeBillingElements.getAsJsonArray();
            LedgerBillingDetails mDeptDetails = null;
            if (removeBillingJson.size() > 0) {
                for (JsonElement mList : removeBillingJson) {
                    Long object = mList.getAsLong();
                    if (object != 0) {
                        mDeptDetails = ledgerBillingDetailsRepository.findByIdAndStatus(object, true);
                        if (mDeptDetails != null) mDeptDetails.setStatus(false);
                        try {
                            ledgerBillingDetailsRepository.save(mDeptDetails);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            ledgerLogger.error("Exception in updateBillingDetails:" + exceptionAsString);
                        }
                    }
                }
            }
        }
    }

    private void updateDeptDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("deptDetails");
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray deptDetailsJson = gstElements.getAsJsonArray();
        LedgerDeptDetails deptDetails = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        if (deptDetailsJson.size() > 0) {
            for (JsonElement mList : deptDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                if (object.get("did").getAsLong() > 0) {
                    deptDetails = ledgerDeptDetailsRepository.findByIdAndStatus(object.get("did").getAsLong(), true);
                } else {
                    deptDetails = new LedgerDeptDetails();
                    deptDetails.setStatus(true);
                }
                deptDetails.setDept(object.get("dept").getAsString());
                deptDetails.setContactPerson(object.get("contact_person").getAsString());
                if (object.has("email")) deptDetails.setEmail(object.get("email").getAsString());
                else deptDetails.setEmail("");
                if (object.has("contact_no") && !object.get("contact_no").getAsString().equalsIgnoreCase(""))
                    deptDetails.setContactNo(object.get("contact_no").getAsLong());
                deptDetails.setCreatedBy(mLedger.getCreatedBy());
                deptDetails.setLedgerMaster(mLedger);
                ledgerDeptDetailsRepository.save(deptDetails);
            }
        }
        /* Remove from existing and set status false */
        if (paramMap.containsKey("removeDeptList")) {
            String removeDeptDetails = request.getParameter("removeDeptList");
            JsonElement removeDeptElements = parser.parse(removeDeptDetails);
            JsonArray removeDeptJson = removeDeptElements.getAsJsonArray();
            LedgerDeptDetails mDeptDetails = null;
            if (removeDeptJson.size() > 0) {
                for (JsonElement mList : removeDeptJson) {
                    Long object = mList.getAsLong();
                    if (object != 0) {
                        mDeptDetails = ledgerDeptDetailsRepository.findByIdAndStatus(object, true);
                        if (mDeptDetails != null) mDeptDetails.setStatus(false);
                        try {
                            ledgerDeptDetailsRepository.save(mDeptDetails);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            ledgerLogger.error("Exception in updateDeptDetails:" + exceptionAsString);
                        }
                    }
                }
            }
        }
    }

    private void updateShippingDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("shippingDetails");
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray shippingDetailsJson = gstElements.getAsJsonArray();
        LedgerShippingAddress spDetails = null;

        if (shippingDetailsJson.size() > 0) {
            for (JsonElement mList : shippingDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                if (object.get("sid").getAsLong() > 0) {
                    spDetails = ledgerShippingDetailsRepository.findByIdAndStatus(object.get("sid").getAsLong(), true);
                } else {
                    spDetails = new LedgerShippingAddress();
                    spDetails.setStatus(true);
                }
                Long districtId = object.get("district").getAsLong();
                State mState = stateRepository.findById(districtId).get();
                spDetails.setDistrict(mState.getName());
                spDetails.setShippingAddress(object.get("shipping_address").getAsString());
                spDetails.setCreatedBy(mLedger.getCreatedBy());
                spDetails.setLedgerMaster(mLedger);
                ledgerShippingDetailsRepository.save(spDetails);
            }
        }
        /* Remove from existing and set status false */
        if (paramMap.containsKey("removeShippingList")) {
            String removeShippingDetails = request.getParameter("removeShippingList");
            JsonElement removeShippingElements = parser.parse(removeShippingDetails);
            JsonArray removeShippingJson = removeShippingElements.getAsJsonArray();
            LedgerShippingAddress mShippingDetails = null;
            if (removeShippingJson.size() > 0) {
                for (JsonElement mList : removeShippingJson) {
                    Long object = mList.getAsLong();
                    if (object != 0) {
                        mShippingDetails = ledgerShippingDetailsRepository.findByIdAndStatus(object, true);
                        if (mShippingDetails != null) mShippingDetails.setStatus(false);
                        try {
                            ledgerShippingDetailsRepository.save(mShippingDetails);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            ledgerLogger.error("Exception in updateShippingDetails:" + exceptionAsString);
                        }
                    }
                }
            }
        }
    }

    private void insertIntobankDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("bankDetails");
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray bankDetailsJson = gstElements.getAsJsonArray();
        if (bankDetailsJson.size() > 0) {
            for (JsonElement mList : bankDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                LedgerBankDetails bankDetails = new LedgerBankDetails();
                bankDetails.setStatus(true);
                bankDetails.setBankName(object.get("bank_name").getAsString());
                bankDetails.setAccountNo(object.get("bank_account_no").getAsString());
                bankDetails.setIfsc(object.get("bank_ifsc_code").getAsString());
                bankDetails.setBankBranch(object.get("bank_branch").getAsString());
                bankDetails.setCreatedBy(mLedger.getCreatedBy());
                bankDetails.setLedgerMaster(mLedger);
                ledgerbankDetailsRepository.save(bankDetails);
            }
        }
    }


    private void updateGstDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("gstdetails");
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray gstDetailsJson = gstElements.getAsJsonArray();
        LedgerGstDetails gstDetails = null;
        if (gstDetailsJson.size() > 0) {
            for (JsonElement mList : gstDetailsJson) {
                JsonObject object = mList.getAsJsonObject();

                if (object.get("bid").getAsLong() > 0) {
                    gstDetails = ledgerGstDetailsRepository.findByIdAndStatus(object.get("bid").getAsLong(), true);
                } else {
                    gstDetails = new LedgerGstDetails();
                    gstDetails.setStatus(true);
                }
                gstDetails.setGstin(object.get("gstin").getAsString());
                if (object.has("dateofregistartion") && !object.get("dateofregistartion").getAsString().equalsIgnoreCase(""))
                    gstDetails.setDateOfRegistration(LocalDate.parse(object.get("dateofregistartion").getAsString()));
                if (object.has("pancard")) gstDetails.setPanCard(object.get("pancard").getAsString());
                else {
                    gstDetails.setPanCard("");
                }

                String stateCode = object.get("gstin").getAsString().substring(0, 2);
                gstDetails.setStateCode(stateCode);
                gstDetails.setCreatedBy(mLedger.getCreatedBy());
                gstDetails.setLedgerMaster(mLedger);
                Long registraton_type = Long.valueOf(object.get("registration_type").getAsString());
                GstTypeMaster gstTypeMaster = gstMasterRepository.findById(registraton_type).get();
                gstDetails.setRegistrationType(gstTypeMaster.getId());
                try {
                    ledgerGstDetailsRepository.save(gstDetails);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    ledgerLogger.error("Exception in updateGstDetails:" + exceptionAsString);
                }
            }
        }
        /* Remove from existing and set status false */
        if (paramMap.containsKey("removeGstList")) {
            String removeGstDetails = request.getParameter("removeGstList");
            JsonElement removeGstElements = parser.parse(removeGstDetails);
            JsonArray removeGstJson = removeGstElements.getAsJsonArray();
            LedgerGstDetails mGstDetails = null;
            if (removeGstJson.size() > 0) {
                for (JsonElement mList : removeGstJson) {
                    Long object = mList.getAsLong();
                    if (object != 0) {
                        mGstDetails = ledgerGstDetailsRepository.findByIdAndStatus(object, true);
                        if (mGstDetails != null) mGstDetails.setStatus(false);
                        try {
                            ledgerGstDetailsRepository.save(mGstDetails);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            ledgerLogger.error("Exception in updateGstDetails:" + exceptionAsString);
                        }
                    }
                }
            }
        }
    }

    private void insertIntoBillingDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("billingDetails");
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray billDetailsJson = gstElements.getAsJsonArray();
        if (billDetailsJson.size() > 0) {
            for (JsonElement mList : billDetailsJson) {
                LedgerBillingDetails billDetails = new LedgerBillingDetails();
                JsonObject object = mList.getAsJsonObject();
                billDetails.setDistrict(object.get("district").getAsString());
                billDetails.setBillingAddress(object.get("billing_address").getAsString());
                billDetails.setCreatedBy(mLedger.getCreatedBy());
                billDetails.setStatus(true);
                billDetails.setLedgerMaster(mLedger);
                ledgerBillingDetailsRepository.save(billDetails);
            }
        }
    }

    private void insertIntoDeptDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("deptDetails");
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray deptDetailsJson = gstElements.getAsJsonArray();
        if (deptDetailsJson.size() > 0) {
            for (JsonElement mList : deptDetailsJson) {
                LedgerDeptDetails deptDetails = new LedgerDeptDetails();
                JsonObject object = mList.getAsJsonObject();
                deptDetails.setDept(object.get("dept").getAsString());
                deptDetails.setContactPerson(object.get("contact_person").getAsString());
                if (object.has("email")) deptDetails.setEmail(object.get("email").getAsString());
                else deptDetails.setEmail("");
                if (object.has("contact_no") && !object.get("contact_no").getAsString().equalsIgnoreCase(""))
                    deptDetails.setContactNo(object.get("contact_no").getAsLong());
                deptDetails.setCreatedBy(mLedger.getCreatedBy());
                deptDetails.setStatus(true);
                deptDetails.setLedgerMaster(mLedger);
                ledgerDeptDetailsRepository.save(deptDetails);
            }
        }
    }

    private void insertIntoShippingDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("shippingDetails");
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray shippingDetailsJson = gstElements.getAsJsonArray();
        if (shippingDetailsJson.size() > 0) {
            for (JsonElement mList : shippingDetailsJson) {
                LedgerShippingAddress spDetails = new LedgerShippingAddress();
                JsonObject object = mList.getAsJsonObject();
                Long districtId = object.get("district").getAsLong();
                State mState = stateRepository.findById(districtId).get();
                spDetails.setDistrict(mState.getName());
                spDetails.setShippingAddress(object.get("shipping_address").getAsString());
                spDetails.setCreatedBy(mLedger.getCreatedBy());
                spDetails.setStatus(true);
                spDetails.setLedgerMaster(mLedger);
                ledgerShippingDetailsRepository.save(spDetails);
            }
        }
    }

    private void insertIntoGstDetails(LedgerMaster mLedger, HttpServletRequest request) {
        String strJson = request.getParameter("gstdetails");
        JsonParser parser = new JsonParser();
        JsonElement gstElements = parser.parse(strJson);
        JsonArray gstDetailsJson = gstElements.getAsJsonArray();
        String firststateCode = "";
        if (gstDetailsJson.size() > 0) {
            LedgerGstDetails gstDetails = null;
            firststateCode = gstDetailsJson.get(0).getAsJsonObject().get("gstin").getAsString().substring(0, 2);
            for (JsonElement mList : gstDetailsJson) {
                JsonObject object = mList.getAsJsonObject();
                gstDetails = new LedgerGstDetails();
                gstDetails.setGstin(object.get("gstin").getAsString());
                if (object.has("dateofregistartion") && !object.get("dateofregistartion").getAsString().equalsIgnoreCase(""))
                    gstDetails.setDateOfRegistration(LocalDate.parse(object.get("dateofregistartion").getAsString()));
                if (object.has("pancard")) gstDetails.setPanCard(object.get("pancard").getAsString());
                else {
                    gstDetails.setPanCard("");
                }
                String stateCode = object.get("gstin").getAsString().substring(0, 2);
                gstDetails.setStateCode(stateCode);
                gstDetails.setCreatedBy(mLedger.getCreatedBy());
                gstDetails.setStatus(true);
                gstDetails.setLedgerMaster(mLedger);
                Long registraton_type = Long.valueOf(object.get("registration_type").getAsString());
                GstTypeMaster gstTypeMaster = gstMasterRepository.findById(registraton_type).get();
                gstDetails.setRegistrationType(gstTypeMaster.getId());
                try {
                    mLedger.setStateCode(stateCode);
                    ledgerGstDetailsRepository.save(gstDetails);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    ledgerLogger.error("Error in insertIntoGstDetails:" + exceptionAsString);
                }
            }
            mLedger.setStateCode(firststateCode);
            ledgerRepository.save(mLedger);
        }
    }

    public void insertIntoLedgerBalanceSummary(LedgerMaster mLedger, String key) {
        LedgerBalanceSummary ledgerBalanceSummary = null;
        if (key.equalsIgnoreCase("create")) {
            ledgerBalanceSummary = new LedgerBalanceSummary();
        } /*else {
            ledgerBalanceSummary = balanceSummaryRepository.findByLedgerMasterId(mLedger.getId());
        }*/
        ledgerBalanceSummary.setLedgerMaster(mLedger);
        ledgerBalanceSummary.setFoundations(mLedger.getFoundations());
        ledgerBalanceSummary.setPrinciples(mLedger.getPrinciples());
        ledgerBalanceSummary.setPrincipleGroups(mLedger.getPrincipleGroups());
        ledgerBalanceSummary.setDebit(0.0);
        ledgerBalanceSummary.setCredit(0.0);
        ledgerBalanceSummary.setOpeningBal(mLedger.getOpeningBal());
        ledgerBalanceSummary.setClosingBal(0.0);
        ledgerBalanceSummary.setBalance(mLedger.getOpeningBal());
        ledgerBalanceSummary.setStatus(true);
        ledgerBalanceSummary.setUnderPrefix(mLedger.getUnderPrefix());
        try {
            balanceSummaryRepository.save(ledgerBalanceSummary);
        } catch (DataIntegrityViolationException e) {
            ledgerLogger.error("Exception in insertIntoLedgerBalanceSummary:" + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (Exception e1) {
            StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ledgerLogger.error("Exception in insertIntoLedgerBalanceSummary:" + exceptionAsString);
        }
    }

    /* get Sundry Creditors Ledgers by outlet id */
    public JsonObject getSundryCreditors(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));


        List<Object[]> sundryCreditors = new ArrayList<>();
        if (users.getBranch() != null) {
            sundryCreditors = ledgerRepository.findSundryCreditorsByOutletIdAndBranchId(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            sundryCreditors = ledgerRepository.findSundryCreditorsByOutletId(users.getOutlet().getId());
        }
        JsonArray result = new JsonArray();
        result = getResult(sundryCreditors);
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("message", "success");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        } else {
            response.addProperty("message", "empty");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        }
        return response;
    }

    public JsonArray getResult(List<Object[]> list) {
        JsonArray result = new JsonArray();
        for (int i = 0; i < list.size(); i++) {
            JsonObject response = new JsonObject();
            Object obj[] = list.get(i);
            response.addProperty("id", obj[0].toString());
            response.addProperty("name", obj[1].toString());
            if (obj[2] != null) response.addProperty("ledger_code", obj[2].toString());
            else response.addProperty("ledger_code", "");
            if (obj[3] != null) response.addProperty("state", obj[3].toString());
            response.addProperty("salesRate", 1);
            response.addProperty("isFirstDiscountPerCalculate", false);
            response.addProperty("takeDiscountAmountInLumpsum", false);
            if (obj[4] != null) {
                Double d = Double.parseDouble(obj[4].toString());
                response.addProperty("salesRate", d.intValue());
            }
            if (obj[5] != null) {
                response.addProperty("isFirstDiscountPerCalculate", Boolean.parseBoolean(obj[5].toString()));
            }
            if (obj[6] != null) {
                response.addProperty("takeDiscountAmountInLumpsum", Boolean.parseBoolean(obj[6].toString()));
            }
            response.add("gstDetails", getGSTDetails(Long.parseLong(obj[0].toString())));
            /***
             * @IMPNOTE : umcomment below code after closing new business logic implemented
             * @AUTHOR: harishg@opethic.com
             * @VERSION: SPRINT 2
             */
           /* Long sundryCreditorId = Long.parseLong(obj[0].toString());
            Double balance = balanceSummaryRepository.findBalance(sundryCreditorId);
            if (balance != null) {
                if (balance > 0) {
                    response.addProperty("ledger_balance", balance);
                    response.addProperty("ledger_balance_type", "CR");
                } else {
                    response.addProperty("ledger_balance", Math.abs(balance));
                    response.addProperty("ledger_balance_type", "DR");

                }
            }*/
            response.addProperty("ledger_balance", 0.0);
            response.addProperty("ledger_balance_type", "CR");
            result.add(response);
        }
        return result;
    }

    /* Get  */
    public JsonArray getGSTDetails(Long ledger_id) {

        JsonArray gstArray = new JsonArray();
        List<LedgerGstDetails> ledgerGstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(ledger_id, true);
        for (LedgerGstDetails mDetails : ledgerGstDetails) {
            JsonObject mObject = new JsonObject();
            mObject.addProperty("id", mDetails.getId());
            mObject.addProperty("gstin", mDetails.getGstin());
            mObject.addProperty("state", mDetails.getStateCode());
            gstArray.add(mObject);
        }
        //    gstDetails.add("gstDetails",gstArray);
        return gstArray;
    }

//   Api for GSTR1 sundary debtors details

    public JsonObject getGSTR1LedgerDetails(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        List<Object[]> sundryDebtors = ledgerRepository.findSundryDebtorsByOutletId(
//                users.getOutlet().getId());
        List<Object[]> sundryDebtors = new ArrayList<>();
        if (users.getBranch() != null) {
            sundryDebtors = ledgerRepository.findSundryDebtorsByOutletIdAndBranchId(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            sundryDebtors = ledgerRepository.findSundryDebtorsByOutletId(users.getOutlet().getId());
        }
        JsonArray result = new JsonArray();
        result = getResult(sundryDebtors);
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("message", "success");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        } else {
            response.addProperty("message", "empty");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        }
        return response;
    }

    public JsonObject getSundryDebtors(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
//        List<Object[]> sundryDebtors = ledgerRepository.findSundryDebtorsByOutletId(
//                users.getOutlet().getId());
        List<Object[]> sundryDebtors = new ArrayList<>();
        if (users.getBranch() != null) {
            sundryDebtors = ledgerRepository.findSundryDebtorsByOutletIdAndBranchId(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            sundryDebtors = ledgerRepository.findSundryDebtorsByOutletId(users.getOutlet().getId());
        }
        JsonArray result = new JsonArray();
        result = getResult(sundryDebtors);
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("message", "success");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        } else {
            response.addProperty("message", "empty");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        }
        return response;
    }

    /* get Purchase Account by outletId and principleId */
    public JsonObject getPurchaseAccount(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
     /*   if (users.getBranch() != null) {
            result = getLedgers("Purchase Accounts", users.getOutlet().getId(), users.getBranch());
        } else {
            result = getLedgers("Purchase Accounts", users.getOutlet().getId(), users.getBranch());
        }*/
        result = getLedgers("Purchase Accounts", users.getOutlet().getId(), users.getBranch());
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("message", "success");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        } else {
            response.addProperty("message", "empty");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        }
        return response;
    }

    private JsonArray getLedgers(String key, Long outletId, Branch branch) {
        Principles principles = principleRepository.findByPrincipleNameIgnoreCaseAndStatus(key, true);
        List<LedgerMaster> indirect_incomes = new ArrayList<>();
        if (branch != null) {
            indirect_incomes = ledgerRepository.findByOutletIdAndBranchIdAndPrinciplesIdAndStatus(outletId, branch.getId(), principles.getId(), true);

        } else {
            indirect_incomes = ledgerRepository.findByOutletIdAndPrinciplesIdAndStatusAndBranchIsNull(outletId, principles.getId(), true);
        }
        JsonArray result = new JsonArray();
        for (LedgerMaster mAccount : indirect_incomes) {
            JsonObject response = new JsonObject();
            response.addProperty("id", mAccount.getId());
            response.addProperty("name", mAccount.getLedgerName());
            response.addProperty("unique_code", principles.getUniqueCode());
            result.add(response);
        }
        return result;
    }

    /* get Sales Account by outletId and principleId */
    public JsonObject getSalesAccount(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
//        result = getLedgers("Sales Accounts", users.getOutlet().getId());
    /*    if (users.getBranch() != null) {
            result = getLedgers("Sales Accounts", users.getOutlet().getId(), users.getBranch());
        } else {
            result = getLedgers("Sales Accounts", users.getOutlet().getId(), users.getBranch());
        }*/
        result = getLedgers("Sales Accounts", users.getOutlet().getId(), users.getBranch());
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("message", "success");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        } else {
            response.addProperty("message", "empty");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        }
        return response;
    }

    /* get All Indirect incomes by principleId(here principle id: 9 is for indirect incomes) */
    public JsonObject getIndirectIncomes(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
//        result = getLedgers("Indirect Income", users.getOutlet().getId());
     /*   if (users.getBranch() != null) {
            result = getLedgers("Indirect Income", users.getOutlet().getId(), users.getBranch());
        } else {
            result = getLedgers("Indirect Income", users.getOutlet().getId(), users.getBranch());
        }*/
        result = getLedgers("Indirect Income", users.getOutlet().getId(), users.getBranch());
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("message", "success");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        } else {
            response.addProperty("message", "empty");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        }
        return response;
    }

    /* get All Indirect expenses by principleId(here principle id: 9 is for indirect incomes) */
    public JsonObject getIndirectExpenses(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
//        result = getLedgers("Indirect Expenses", users.getOutlet().getId());
      /*  if (users.getBranch() != null) {
            result = getLedgers("Indirect Expenses", users.getOutlet().getId(), users.getBranch());
        } else {
            result = getLedgers("Indirect Expenses", users.getOutlet().getId(), users.getBranch());
        }*/
        result = getLedgers("Indirect Expenses", users.getOutlet().getId(), users.getBranch());
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("message", "success");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        } else {
            response.addProperty("message", "empty");
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.add("list", result);
        }
        return response;
    }

    public JsonObject getAllLedgers(HttpServletRequest request) {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumCR = 0.0;
        Double sumDR = 0.0;
        DecimalFormat df = new DecimalFormat("0.00");
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<LedgerMaster> balanceSummaries = new ArrayList<>();
        if (users.getBranch() != null) {
            /**** Default ledgers for Branch Users *****/
            balanceSummaries = ledgerRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            balanceSummaries = ledgerRepository.findByOutletIdAndStatusAndBranchIsNullOrderByIdDesc(users.getOutlet().getId(), true);
        }
        for (LedgerMaster balanceSummary : balanceSummaries) {
            Long ledgerId = balanceSummary.getId();
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", balanceSummary.getId());
            jsonObject.addProperty("foundations_name", balanceSummary.getFoundations().getFoundationName());
            if (balanceSummary.getAssociateGroups() == null) {
                if (balanceSummary.getPrinciples() != null) {
                    jsonObject.addProperty("principle_name", balanceSummary.getPrinciples().getPrincipleName());
                }
                if (balanceSummary.getPrincipleGroups() != null) {
                    jsonObject.addProperty("subprinciple_name", balanceSummary.getPrincipleGroups().getGroupName());
                } else {
                    jsonObject.addProperty("subprinciple_name", "");
                }
            } else {
                if (balanceSummary.getAssociateGroups().getPrincipleGroups() != null) {
                    jsonObject.addProperty("principle_name", balanceSummary.getPrinciples().getPrincipleName());
                    jsonObject.addProperty("subprinciple_name", balanceSummary.getAssociateGroups().getAssociatesName());
                } else {
                    jsonObject.addProperty("principle_name", balanceSummary.getAssociateGroups().getAssociatesName());
                }
            }
            Double openingBalance = 0.0;
            try {
                openingBalance = ledgerRepository.findOpeningBalance(balanceSummary.getId());//1000
                sumCR = ledgerTransactionPostingsRepository.findsumCR(balanceSummary.getId());//-0.20
                sumDR = ledgerTransactionPostingsRepository.findsumDR(balanceSummary.getId());//-0.40
                closingBalance = openingBalance - sumDR + sumCR;//0-295+0
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                ledgerLogger.error("Exception:" + exceptionAsString);
            }
            jsonObject.addProperty("default_ledger", balanceSummary.getIsDefaultLedger());
            jsonObject.addProperty("ledger_form_parameter_slug", balanceSummary.getSlugName());
            jsonObject.addProperty("unique_code", balanceSummary.getUniqueCode());
//            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndStatus(balanceSummary.getId(), true);
            if (balanceSummary.getFoundations().getId() == 1) { //DR
                if (closingBalance > 0) {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                } else {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                }

            } else if (balanceSummary.getFoundations().getId() == 2) { //cr
                if (closingBalance > 0) {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));

                } else {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                }

            } else if (balanceSummary.getFoundations().getId() == 3) {
                if (closingBalance > 0) {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                } else {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                }

            } else if (balanceSummary.getFoundations().getId() == 4) {
                if (closingBalance < 0) {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                } else {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                }
            }
            jsonObject.addProperty("ledger_name", balanceSummary.getLedgerName());
            jsonObject.addProperty("balancing_method", balanceSummary.getBalancingMethod() != null ? balanceSummary.getBalancingMethod().getBalancingMethod() : "");
            jsonObject.addProperty("opening_balance", openingBalance);

            result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        json.addProperty("company_name", users.getOutlet().getCompanyName());
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    //    api for Pagination
    public Object getAllLedgersPagination(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        Map<String, String[]> paramMap = req.getParameterMap();
        ResponseMessage responseMessage = new ResponseMessage();
        System.out.println("request " + request + "  req=" + req);
        Integer pageNo = 1;
        Integer pageSize = 150;
        if (paramMap.containsKey("pageNo"))
            pageNo = Integer.parseInt(request.get("pageNo"));
        if (paramMap.containsKey("pageSize"))
            pageSize = Integer.parseInt(request.get("pageSize"));
        String searchText = request.get("searchText");
//        String SearchText = "";
//        String startDate = request.get("startDate");
//        String endDate = request.get("endDate");
//        LocalDate endDatep = null;
//        LocalDate startDatep = null;
        Boolean flag = false;
//        System.out.println("startdate "+startDatep+ "  endDate "+endDatep);
        List ledgerMasters = new ArrayList<>();
        List<LedgerMaster> ledgerMasterList = new ArrayList<>();
        List<LedgerMasterDTO> ledgerMasterDTOList = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT id FROM `ledger_master_tbl` WHERE status=1 AND outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";

            }

            if (!searchText.equalsIgnoreCase("")) {
                //query = query + " AND (ledger_name LIKE '%" + searchText + "%' OR foundations_name LIKE '%" + searchText + "%')";
                query = query + " AND (ledger_name LIKE '%" + searchText + "%')";
            }
            // String query1 = query;       //we get all lists in this list
            query = query + " ORDER BY id DESC";
            query = query + " LIMIT " + (pageNo - 1) * pageSize + ", " + pageSize;
            Query q = entityManager.createNativeQuery(query);
            ledgerMasters = q.getResultList();
      /*      Query q1 = entityManager.createNativeQuery(query1, LedgerMaster.class);

            ledgerMasterList = q1.getResultList();
            System.out.println("Limit total rows " + ledgerMasterDTOList.size());

            Integer total_pages = (ledgerMasterList.size() / pageSize);
            if ((ledgerMasterList.size() % pageSize > 0)) {
                total_pages = total_pages + 1;
            }*/
            String query1 = "SELECT COUNT(ledger_master_tbl.id) as totalcount FROM ledger_master_tbl WHERE " +
                    "ledger_master_tbl.status=? AND ledger_master_tbl.outlet_id=?";
            if (users.getBranch() != null) {
                query1 = query1 + " AND ledger_master_tbl.branch_id=?";
            } else {
                query1 = query1 + " AND ledger_master_tbl.branch_id IS NULL";
            }
            Query q1 = entityManager.createNativeQuery(query1);
            q1.setParameter(1, true);
            q1.setParameter(2, users.getOutlet().getId());
            if (users.getBranch() != null)
                q1.setParameter(3, users.getOutlet().getId());
            int totalProducts = ((BigInteger) q1.getSingleResult()).intValue();
            Integer total_pages = (totalProducts / pageSize);
            if ((totalProducts % pageSize > 0)) {
                total_pages = total_pages + 1;
            }
            for (Object mLedger : ledgerMasters) {
//                System.out.println("LM=>" + mLedger.toString());
                LedgerMaster ledgerMaster1 = ledgerRepository.findByIdAndStatus(Long.parseLong(mLedger.toString()), true);
                ledgerMasterDTOList.add(convertToDTDTO(ledgerMaster1));
            }
            GenericDatatable<LedgerMasterDTO> data = new GenericDatatable<>(ledgerMasterDTOList, ledgerMasterList.size(),
                    pageNo, pageSize, total_pages);
            responseMessage.setResponseObject(data);
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            genericDTData.setRows(ledgerMasterDTOList);
            genericDTData.setTotalRows(0);
        }
        return responseMessage;
    }

    private LedgerMasterDTO convertToDTDTO(LedgerMaster ledgerMasterList) {
        Double closingBalance = 0.0;
        Double sumCR = 0.0;
        Double sumDR = 0.0;
        DecimalFormat df = new DecimalFormat("0.00");

        LedgerMasterDTO ledgerMasterDTO = new LedgerMasterDTO();
        ledgerMasterDTO.setId(ledgerMasterList.getId());
        ledgerMasterDTO.setFoundations_name(ledgerMasterList.getFoundations().getFoundationName());
        if (ledgerMasterList.getAssociateGroups() == null) {
            if (ledgerMasterList.getPrinciples() != null) {
                ledgerMasterDTO.setPrinciple_name(ledgerMasterList.getPrinciples().getPrincipleName());
            }
            if (ledgerMasterList.getPrincipleGroups() != null) {
                ledgerMasterDTO.setSubprinciple_name(ledgerMasterList.getPrincipleGroups().getGroupName());
            } else {
                ledgerMasterDTO.setSubprinciple_name("");
            }
        } else {
            if (ledgerMasterList.getAssociateGroups().getPrincipleGroups() != null) {
                ledgerMasterDTO.setPrinciple_name(ledgerMasterList.getPrinciples().getPrincipleName());
                ledgerMasterDTO.setSubprinciple_name(ledgerMasterList.getAssociateGroups().getAssociatesName());
            } else {
                ledgerMasterDTO.setPrinciple_name(ledgerMasterList.getAssociateGroups().getAssociatesName());
                ledgerMasterDTO.setSubprinciple_name("");
            }
        }
        try {
            Double openingBalance = ledgerRepository.findOpeningBalance(ledgerMasterList.getId());//1000
            sumCR = ledgerTransactionPostingsRepository.findsumCR(ledgerMasterList.getId());//-0.20
            sumDR = ledgerTransactionPostingsRepository.findsumDR(ledgerMasterList.getId());//-0.40
            closingBalance = openingBalance - sumDR + sumCR;//0-295+0
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ledgerLogger.error("Exception:" + exceptionAsString);
        }


        ledgerMasterDTO.setDefault_ledger(ledgerMasterList.getIsDefaultLedger());
        ledgerMasterDTO.setLedger_form_parameter_slug(ledgerMasterList.getSlugName());

        if (ledgerMasterList.getFoundations().getId() == 1) { //DR
            if (closingBalance > 0) {
                ledgerMasterDTO.setCr(Double.valueOf(df.format(Math.abs(closingBalance))));
                ledgerMasterDTO.setDr(Double.valueOf(df.format(0)));
            } else {
                ledgerMasterDTO.setCr(Double.valueOf(df.format(0)));
                ledgerMasterDTO.setDr(Double.valueOf(df.format(Math.abs(closingBalance))));
            }

        } else if (ledgerMasterList.getFoundations().getId() == 2) { //cr
            if (closingBalance > 0) {
                ledgerMasterDTO.setCr(Double.valueOf(df.format(Math.abs(closingBalance))));
                ledgerMasterDTO.setDr(Double.valueOf(df.format(0)));

            } else {
                ledgerMasterDTO.setCr(Double.valueOf(df.format(0)));
                ledgerMasterDTO.setDr(Double.valueOf(df.format(Math.abs(closingBalance))));
            }

        } else if (ledgerMasterList.getFoundations().getId() == 3) {
            if (closingBalance > 0) {
                ledgerMasterDTO.setCr(Double.valueOf(df.format(Math.abs(closingBalance))));
                ledgerMasterDTO.setDr(Double.valueOf(df.format(0)));
            } else {
                ledgerMasterDTO.setCr(Double.valueOf(df.format(0)));
                ledgerMasterDTO.setDr(Double.valueOf(df.format(Math.abs(closingBalance))));
            }

        } else if (ledgerMasterList.getFoundations().getId() == 4) {
            if (closingBalance < 0) {
                ledgerMasterDTO.setCr(Double.valueOf(df.format(0)));
                ledgerMasterDTO.setDr(Double.valueOf(df.format(Math.abs(closingBalance))));
            } else {
                ledgerMasterDTO.setCr(Double.valueOf(df.format(Math.abs(closingBalance))));
                ledgerMasterDTO.setDr(Double.valueOf(df.format(0)));
            }
        }
        ledgerMasterDTO.setLedger_name(ledgerMasterList.getLedgerName());

        return ledgerMasterDTO;

    }

    public Object getAllLedgerWithPagination(HttpServletRequest req) {
        Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
//        Long adminId = users.getAdmin().getId();
        Integer from = Integer.parseInt(req.getParameter("from"));
        Integer to = Integer.parseInt(req.getParameter("to"));
        String searchText = req.getParameter("searchText");
        List<LedgerMaster> list = new ArrayList<>();
        GenericDTData genericDTData = new GenericDTData();
        try {
            String query = "SELECT * FROM `ledger_master_tbl` WHERE status=1 AND outlet_id=" + users.getOutlet().getId();
            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }
           /* if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND (product_name LIKE '%" + searchText + "%' OR prescription_type LIKE '%" +
                        searchText + "%' OR package_Name LIKE '%" + searchText + "%' OR company_name LIKE '%" +
                        searchText + "%' OR group_name LIKE '%" + searchText + "%' OR category_name LIKE '%" +
                        searchText + "%' OR mrp LIKE '%" + searchText + "%' OR product_code LIKE '%" + searchText + "%' )";
            }*/

//            String jsonToStr = req.getParameter("sort");
//            System.out.println(" sort " + jsonToStr);
//            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
//            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") &&
//                    jsonObject.get("colId").getAsString() != null) {
//                System.out.println(" ORDER BY " + jsonObject.get("colId").getAsString());
//                String sortBy = jsonObject.get("colId").getAsString();
//                query = query + " ORDER BY " + sortBy;
//                if (jsonObject.get("isAsc").getAsBoolean() == true) {
//                    query = query + " ASC";
//                } else {
//                    query = query + " DESC";
//                }
//            } else {
            query = query + " ORDER BY ledger_name ASC";
//            }
            String query1 = query;
            Integer endLimit = to - from;
            query = query + " LIMIT " + from + ", " + endLimit;
            System.out.println("query " + query);

            Query q = entityManager.createNativeQuery(query, LedgerMaster.class);
            Query q1 = entityManager.createNativeQuery(query1, LedgerMaster.class);

            list = q.getResultList();
            System.out.println("Limit total rows " + list.size());

            /*for (LedgerMaster ledgerList : list) {
                productDTDTOList.add(convertToDTDTO(productListView));
            }*/

            List<LedgerMaster> ledgerArrayList = new ArrayList<>();
            ledgerArrayList = q1.getResultList();
            System.out.println("total rows " + ledgerArrayList.size());

            genericDTData.setRows(list);
            genericDTData.setTotalRows(ledgerArrayList.size());
        } catch (Exception e) {
            genericDTData.setRows(list);
            genericDTData.setTotalRows(0);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ledgerLogger.error("Exception:" + exceptionAsString);
        }
        return genericDTData;
    }



/*    public String exportReport(HttpServletRequest request) throws IOException, JRException, URISyntaxException {

        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));

        List<LedgerMaster> ledgerMasters = new ArrayList<>();
        if (users.getBranch() != null) {
            ledgerMasters = ledgerRepository.findTop3ByOutletIdAndBranchIdOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            ledgerMasters = ledgerRepository.findTop3ByOutletIdOrderByIdDesc(users.getOutlet().getId());
        }

        */

    /*** Japser Reports ****//*

        //File file = ResourceUtils.getFile("classpath:ledger_report.jrxml");
        File file = ResourceUtils.getFile("classpath:ledger_blank.jrxml");

        File newfile = new File("resources/ledeger_blank.html");

        String absolutePath = file.getAbsolutePath();
        System.out.println("File Path:" + file.getAbsolutePath());
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(ledgerMasters);
            Map<String, Object> map = new HashMap<>();
            map.put("createdBy", users.getFullName());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, dataSource);
            printReport(jasperPrint, "CT-D150");

        } catch (Exception e) {
            e.printStackTrace();
            ledgerLogger.error("Exception in exportReport:" + e.getMessage());
            System.out.println("" + e.getMessage());
        }
        return "Report Generated ";
    }*/

    /*public void printReport(JasperPrint jasperPrint, String selectedPrinter) throws JRException {
        PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
        printRequestAttributeSet.add(MediaSizeName.ISO_A4);
        if (jasperPrint.getOrientationValue() == net.sf.jasperreports.engine.type.OrientationEnum.LANDSCAPE) {
            printRequestAttributeSet.add(OrientationRequested.LANDSCAPE);
        } else {
            printRequestAttributeSet.add(OrientationRequested.PORTRAIT);
        }
        PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
        printServiceAttributeSet.add(new PrinterName(selectedPrinter, null));

        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        SimplePrintServiceExporterConfiguration configuration = new SimplePrintServiceExporterConfiguration();
        configuration.setPrintRequestAttributeSet(printRequestAttributeSet);
        configuration.setPrintServiceAttributeSet(printServiceAttributeSet);
        configuration.setDisplayPageDialog(false);
        configuration.setDisplayPrintDialog(false);

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setConfiguration(configuration);

        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService selectedService = null;
        if (services.length != 0 || services != null) {
            for (PrintService service : services) {
                String existingPrinter = service.getName();
                if (existingPrinter.equals(selectedPrinter)) {
                    selectedService = service;
                    break;
                }
            }
        }
        if (selectedService != null) {
            exporter.exportReport();
        } else {
            System.out.println("You did not set the printer!");
        }
    }*/

    /* Sundry creditors overdue for bil by bill */
    public JsonObject getTotalAmountBillbyBill(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        List<LedgerTransactionDetails> list = new ArrayList<>();
        if (users.getBranch() != null) {
            list = transactionDetailsRepository.findByLedgerMasterIdAndOutletIdAndBranchIdAndTransactionTypeId(Long.parseLong(request.getParameter("id")), users.getOutlet().getId(), users.getBranch().getId(), 1L);
        } else {
            list = transactionDetailsRepository.findByLedgerMasterIdAndOutletIdAndTransactionTypeId(Long.parseLong(request.getParameter("id")), users.getOutlet().getId(), 1L);
        }
        JsonArray result = new JsonArray();

        for (LedgerTransactionDetails mList : list) {
            JsonObject jsonObject = new JsonObject();
            if (!mList.getPaymentStatus().equalsIgnoreCase("completed")) {
                jsonObject.addProperty("id", mList.getId());
                jsonObject.addProperty("ledger_id", mList.getLedgerMaster().getId());
                jsonObject.addProperty("transaction_id", mList.getTransactionId());
                if (mList.getPaymentStatus().equalsIgnoreCase("pending")) {
                    jsonObject.addProperty("amount", mList.getCredit());
                } else {
                    /*PaymentTransactionDetails paymentDetails = paymentTransactionDetailsRepo.
                            findTopByTransactionDetailsIdAndPaymentStatusOrderByIdDesc(mList.getId(), "partially_paid");
                    jsonObject.addProperty("amount", paymentDetails.getRemainingAmt());*/
                }
                result.add(jsonObject);
            }
        }
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("message", "success");
            response.add("list", result);
        } else {
            response.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
            response.addProperty("message", "empty list");
            response.add("list", result);
        }
        return response;
    }

    public Object editLedgerMaster(MultipartHttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        Long id = Long.parseLong(request.getParameter("id"));
        LedgerMaster ledgerMaster = repository.findByIdAndStatus(id, true);
        LedgerMaster mLedger = ledgerCreateUpdate(request, "edit", ledgerMaster);
        if (mLedger != null) {
            // insertIntoLedgerBalanceSummary(mLedger, "edit");
            //   DataLockModel dataLockModel = DataLockModel.getInstance();
            responseMessage.setMessage("Ledger updated successfully");
            //dataLockModel.removeObject("ledgerMaster_" + mLedger.getId());
            responseMessage.setResponseStatus(HttpStatus.OK.value());
            responseMessage.setData(mLedger.getId().toString());
        } else {
            responseMessage.setMessage("error");
            responseMessage.setResponseStatus(HttpStatus.FORBIDDEN.value());
        }
        return responseMessage;
    }

    /* get total balance of each sundry creditors for Payment Vouchers  */
    public JsonObject getTotalAmount(HttpServletRequest request, String key) {
        List<Object[]> list = new ArrayList<>();
        if (key.equalsIgnoreCase("sc")) {
            list = balanceSummaryRepository.calculate_total_amount(5L);
        } else if (key.equalsIgnoreCase("sd")) {
            list = balanceSummaryRepository.calculate_total_amount(1L);
        }
        JsonArray result = new JsonArray();
        for (int i = 0; i < list.size(); i++) {
            JsonObject jsonObject = new JsonObject();
            Object obj[] = list.get(i);
            jsonObject.addProperty("id", obj[0].toString());
            jsonObject.addProperty("amount", Math.abs(Double.parseDouble(obj[1].toString())));
            jsonObject.addProperty("name", obj[2].toString());
            LedgerMaster creditors = ledgerRepository.findByIdAndStatus(Long.parseLong(obj[0].toString()), true);
            jsonObject.addProperty("balancing_method", generateSlugs.getSlug(creditors.getBalancingMethod().getBalancingMethod()));
            jsonObject.addProperty("slug", creditors.getSlugName());
            if (key.equalsIgnoreCase("sc")) {
                if (Double.parseDouble(obj[1].toString()) > 0) jsonObject.addProperty("type", "DR");
                else jsonObject.addProperty("type", "CR");
            } else if (key.equalsIgnoreCase("sd")) {
                if (Double.parseDouble(obj[1].toString()) > 0) jsonObject.addProperty("type", "CR");
                else jsonObject.addProperty("type", "DR");
            }
            result.add(jsonObject);
        }
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("message", "success");
            response.add("list", result);
        } else {
            response.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
            response.addProperty("message", "empty list");
            response.add("list", result);
        }
        return response;
    }

    @Transactional
    public JsonObject getLedgersById(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        JsonObject jsonObject = new JsonObject();
//        Long id = Long.parseLong(request.getParameter("ledger_form_parameter_id"));
//        Long id = Long.parseLong(request.getParameter("id"));
        //  String slug_name = request.getParameter("ledger_form_parameter_slug");
        //    LedgerMaster mLedger = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        //  String query =" select * from ledger_master_tbl wherer id="+Long.parseLong(request.getParameter("id");
        try {
//            LedgerMaster mLedger = entityManager.find(LedgerMaster.class, id, LockModeType.PESSIMISTIC_WRITE);
            LedgerMaster mLedger = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);

       /*     LedgerMaster mLedger = entityManager.find(LedgerMaster.class, id);
            entityManager.lock(mLedger,LockModeType.PESSIMISTIC_WRITE);*/
            if (mLedger != null) {
//                DataLockModel dataLockModel = DataLockModel.getInstance();
//                if (dataLockModel.isPresent("ledgerMaster_" + mLedger.getId())) {
//                    result.addProperty("message", "Selected row already in use");
//                    result.addProperty("responseStatus", HttpStatus.CONFLICT.value());
//                } else {
                // dataLockModel.addObject("ledgerMaster_" + mLedger.getId(), mLedger);
                jsonObject.addProperty("id", mLedger.getId());
                jsonObject.addProperty("default_ledger", mLedger.getIsDefaultLedger());
                jsonObject.addProperty("ledger_name", mLedger.getLedgerName());
                jsonObject.addProperty("is_private", mLedger.getIsPrivate());
                jsonObject.addProperty("supplier_code", mLedger.getLedgerCode() != null ? mLedger.getLedgerCode() : "");
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
                jsonObject.addProperty("ledgerStateCode", mLedger.getStateCode() != null ? mLedger.getStateCode() : null);
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
                jsonObject.addProperty("licenseNo", mLedger.getLicenseNo() != null ? mLedger.getLicenseNo() : "");
                jsonObject.addProperty("reg_date", mLedger.getLicenseExpiry() != null ? mLedger.getLicenseExpiry().toString() : "");
                jsonObject.addProperty("manufacturingLicenseNo", mLedger.getManufacturingLicenseNo());
                jsonObject.addProperty("manufacturingLicenseExpiry", mLedger.getManufacturingLicenseExpiry() != null ? mLedger.getManufacturingLicenseExpiry().toString() : "");
                jsonObject.addProperty("gstTransferDate", mLedger.getGstTransferDate() != null ? mLedger.getGstTransferDate().toString() : "");
                jsonObject.addProperty("gstin", mLedger.getGstin());
                jsonObject.addProperty("place", mLedger.getPlace());
                jsonObject.addProperty("district", mLedger.getDistrict());
                jsonObject.addProperty("landMark", mLedger.getLandMark());
                jsonObject.addProperty("businessType", mLedger.getBusinessType() != null ? mLedger.getBusinessType() : "");
                jsonObject.addProperty("businessTrade", mLedger.getBusinessTrade() != null ? mLedger.getBusinessTrade() : "");
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
                jsonObject.addProperty("districtId", mLedger.getDistrictHeadId());
                jsonObject.addProperty("regionalId", mLedger.getRegionalHeadId());
                jsonObject.addProperty("zoneId", mLedger.getZonalHeadId());
                jsonObject.addProperty("stateId", mLedger.getStateHeadId());
                AreaHead areaHead = new AreaHead();
                if (mLedger.getZonalHeadId() != null) {
                    areaHead = areaHeadRepository.findByIdAndStatus(mLedger.getZonalHeadId(), true);
                    jsonObject.addProperty("zoneName", areaHead.getFirstName() + " " + areaHead.getLastName());
                } else {
                    jsonObject.addProperty("zoneName", "franchiseHeads");
                }
                if (mLedger.getRegionalHeadId() != null) {
                    areaHead = areaHeadRepository.findByIdAndStatus(mLedger.getRegionalHeadId(), true);
                    jsonObject.addProperty("regionalName", areaHead.getFirstName() + " " + areaHead.getLastName());
                }
                if (mLedger.getDistrictHeadId() != null) {
                    areaHead = areaHeadRepository.findByIdAndStatus(mLedger.getDistrictHeadId(), true);
                    jsonObject.addProperty("districtName", areaHead.getFirstName() + " " + areaHead.getLastName());
                }
                if (mLedger.getStateHeadId() != null) {
                    areaHead = areaHeadRepository.findByIdAndStatus(mLedger.getStateHeadId(), true);
                    jsonObject.addProperty("stateName", areaHead.getFirstName() + " " + areaHead.getLastName());
                }
//                jsonObject.addProperty("businessType",mLedger.getBusinessType());
//                jsonObject.addProperty("ownerName",mLedger.getOwnerName());
//                jsonObject.addProperty("ownerAddress",mLedger.getOwnerAddress());
//                jsonObject.addProperty("ownerEmail",mLedger.getOwnerEmail());
//                jsonObject.addProperty("ownerMobile",mLedger.getOwnerMobile());
//                jsonObject.addProperty("ownerPincode",mLedger.getOwnerPincode());
//                jsonObject.addProperty("ownerWhatsapp",mLedger.getOwnerWhatsappNo());
//                jsonObject.addProperty("ownerstate",mLedger.getOwnerstate().getId());
//                jsonObject.addProperty("education",mLedger.getEducation());
//                jsonObject.addProperty("ownerDOB", mLedger.getDob() != null ? mLedger.getDob().toString() : "");
//                jsonObject.addProperty("age",mLedger.getAge());
////                jsonObject.addProperty("ownerWhatsapp",mLedger.getPresentOccupation());
//                jsonObject.addProperty("gender",mLedger.getGender());
//                jsonObject.addProperty("presentOccupation",mLedger.getPresentOccupation());
//
//                jsonObject.addProperty("aadarUpload", mLedger.getAadarUpload() != null ? serverUrl + mLedger.getAadarUpload() : "");
//                jsonObject.addProperty("panUpload", mLedger.getPanUpload() != null ? serverUrl + mLedger.getPanUpload() : "");
//                jsonObject.addProperty("dlUpload", mLedger.getDLUpload() != null ? serverUrl + mLedger.getDLUpload() : "");
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
                            mObject.addProperty("license_doc_upload_old", mList.getLicenseDocUpload());
                            mObject.addProperty("license_doc_upload", "");
                            mObject.add("licences_type", licenseType);
                            jsonlicenseArray.add(mObject);
                        }
                    }
                }
                jsonObject.add("licensesDetails", jsonlicenseArray);
                /*** Ledger Opening Balance of SC and SD with Invoices ****/

                /* payment mode  Details of Ledger */
                JsonArray jsonpaymentmodeArray = new JsonArray();
                if (mLedger.getColumnR() != null && mLedger.getColumnR()) {
                    List<LedgerPaymentModeDetails> paymentList = new ArrayList<>();
                    paymentList = ledgerPaymentModeRepository.findByLedgerIdAndStatus(mLedger.getId(), true);
                    if (paymentList != null && paymentList.size() > 0) {
                        for (LedgerPaymentModeDetails mList : paymentList) {
                            JsonObject mObject = new JsonObject();
                            mObject.addProperty("detailsId", mList.getId());
                            mObject.addProperty("id", mList.getPaymentModeMasterId());
                            PaymentModeMaster paymentmode = paymentModeMasterRepository.findById(mList.getPaymentModeMasterId()).get();
                            mObject.addProperty("label", paymentmode != null ? paymentmode.getPaymentMode() : "");
                            mObject.addProperty("value", 1);
                            jsonpaymentmodeArray.add(mObject);
                        }
                    }
                }
                jsonObject.add("payment_modes", jsonpaymentmodeArray);

                /* payment mode Details of Ledger */

                /* Opening Balance Details */
                JsonArray jsonOpeningArray = new JsonArray();
                List<LedgerOpeningBalance> openingDetails = new ArrayList<>();
                openingDetails = ledgerOpeningBalanceRepository.findByLedgerIdAndStatus(mLedger.getId(), true);
                if (openingDetails != null && openingDetails.size() > 0) {
                    for (LedgerOpeningBalance mList : openingDetails) {
                        JsonObject mObject = new JsonObject();
                        mObject.addProperty("id", mList.getId());
                        mObject.addProperty("invoice_no", mList.getInvoice_no());
                        mObject.addProperty("invoice_date", mList.getInvoice_date() != null ? mList.getInvoice_date().toString() : "");
                        mObject.addProperty("invoice_paid_amt", mList.getInvoice_paid_amt() != null ? mList.getInvoice_paid_amt().toString() : "");
                        mObject.addProperty("invoice_bal_amt", mList.getInvoice_bal_amt() != null ? mList.getInvoice_bal_amt().toString() : "");
                        JsonObject typeObject = new JsonObject();
                        if (mList.getBalancingType().equalsIgnoreCase("dr")) {
                            typeObject.addProperty("label", "Dr");
                            typeObject.addProperty("value", "1");
                        } else {
                            typeObject.addProperty("label", "Cr");
                            typeObject.addProperty("value", "2");
                        }
                        mObject.add("type", typeObject);
                        mObject.addProperty("due_days", mList.getDue_days());
                        mObject.addProperty("bill_amt", mList.getBill_amt());
                        jsonOpeningArray.add(mObject);
                    }
                }
                jsonObject.add("opening_bal_invoice_list", jsonOpeningArray);
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
            ledgerLogger.error("Exception:" + exceptionAsString);
        }

        return result;

    }


    /* public Object DTGetallledgers(Map<String, String> request, HttpServletRequest req) {
     *//*Users users = jwtRequestFilter.getUserDataFromToken(req.getHeader("Authorization").substring(7));
        Long outletId = users.getOutlet().getId();
        Integer from = Integer.parseInt(request.get("from"));
        Integer to = Integer.parseInt(request.get("to"));
        String searchText = request.get("searchText");

        GenericDatatable genericDatatable = new GenericDatatable();
        List<LedgerBalanceSummaryDtView> ledgerBalanceSummaryDtViewList = new ArrayList<>();
        try {
            String query = "SELECT * FROM `ledger_balance_summary_dt_view` WHERE ledger_balance_summary_dt_view.outlet_id='" + outletId
                    + "' AND ledger_balance_summary_dt_view.status=1";

            if (!searchText.equalsIgnoreCase("")) {
                query = query + " AND (id LIKE '%" + searchText + "%' OR  ledger_name LIKE '%" + searchText + "%' OR group_name LIKE '%" +
                        searchText + "%' OR  principle_name LIKE '%" + searchText + "%' OR credit LIKE '%" +
                        searchText + "%' OR debit LIKE '%" +
                        searchText + "%' )";
            }

            String jsonToStr = request.get("sort");
            System.out.println(" sort " + jsonToStr);
            JsonObject jsonObject = new Gson().fromJson(jsonToStr, JsonObject.class);
            if (!jsonObject.get("colId").toString().equalsIgnoreCase("null") &&
                    jsonObject.get("colId").getAsString() != null) {
                System.out.println(" ORDER BY " + jsonObject.get("colId").getAsString());
                String sortBy = jsonObject.get("colId").getAsString();
                query = query + " ORDER BY " + sortBy;
                if (jsonObject.get("isAsc").getAsBoolean() == true) {
                    query = query + " ASC";
                } else {
                    query = query + " DESC";
                }
            } else {
                query = query + " ORDER BY ledger_name ASC";
            }
            String query1 = query;
            Integer endLimit = to - from;
            query = query + " LIMIT " + from + ", " + endLimit;
            System.out.println("query " + query);*//*

        //  Query q = entityManager.createNativeQuery(query, LedgerBalanceSummaryDtView.class);
        //  Query q1 = entityManager.createNativeQuery(query1, LedgerBalanceSummaryDtView.class);

        // ledgerBalanceSummaryDtViewList = q.getResultList();
        //   System.out.println("Limit total rows " + ledgerBalanceSummaryDtViewList.size());

        //   List<LedgerBalanceSummaryDtView> ledgerBalanceSummaryDtViewArrayList = new ArrayList<>();
       *//*     ledgerBalanceSummaryDtViewArrayList = q1.getResultList();
            System.out.println("total rows " + ledgerBalanceSummaryDtViewArrayList.size());

            genericDatatable.setRows(ledgerBalanceSummaryDtViewList);
            genericDatatable.setTotalRows(ledgerBalanceSummaryDtViewArrayList.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            genericDatatable.setRows(ledgerBalanceSummaryDtViewList);
            genericDatatable.setTotalRows(0);
        }
        return genericDatatable;*//*
    }*/

    /* get sundry creditors, sundry debtors,cash account and  bank accounts*/
    public Object getClientList(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ClientsListDTO clientsListDTO = new ClientsListDTO();
        List<Object[]> sundryCreditors = new ArrayList<>();
        /* sundry Creditors List */
        if (users.getBranch() != null) {

            sundryCreditors = ledgerRepository.findSundryCreditorsByOutletIdAndBranchId(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            sundryCreditors = ledgerRepository.findSundryCreditorsByOutletId(users.getOutlet().getId());
        }
        List<ClientDetails> clientDetails = new ArrayList<>();
        for (int i = 0; i < sundryCreditors.size(); i++) {
            ClientDetails mDetails = new ClientDetails();
            Object obj[] = sundryCreditors.get(i);
            mDetails.setId(Long.parseLong(obj[0].toString()));
            mDetails.setLedger_name((String) obj[1]);
            mDetails.setLedger_code((String) obj[2]);
            mDetails.setStateCode((String) obj[3]);
            clientDetails.add(mDetails);
        }
        /* end of Sundry creditors List */

        /* sundry Debtors List */
        List<Object[]> sundryDebtors = new ArrayList<>();
        if (users.getBranch() != null) {
            sundryDebtors = ledgerRepository.findSundryDebtorsByOutletIdAndBranchId(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            sundryDebtors = ledgerRepository.findSundryDebtorsByOutletId(users.getOutlet().getId());
        }
        for (int i = 0; i < sundryDebtors.size(); i++) {
            ClientDetails mDetails = new ClientDetails();
            Object obj[] = sundryDebtors.get(i);
            mDetails.setId(Long.parseLong(obj[0].toString()));
            mDetails.setLedger_name((String) obj[1]);
            mDetails.setLedger_code((String) obj[2]);
            mDetails.setStateCode((String) obj[3]);
            clientDetails.add(mDetails);
        }
        /* end of Sundry debtors List */

        /* Cash-in Hand List */
        List<Object[]> cashInHands = new ArrayList<>();
        if (users.getBranch() != null) {
            cashInHands = ledgerRepository.findCashInHandByOutletIdAndBranch(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            cashInHands = ledgerRepository.findCashInHandByOutletId(users.getOutlet().getId());
        }
        for (int i = 0; i < cashInHands.size(); i++) {
            ClientDetails mDetails = new ClientDetails();
            Object obj[] = cashInHands.get(i);
            mDetails.setId(Long.parseLong(obj[0].toString()));
            mDetails.setLedger_name((String) obj[1]);
            mDetails.setLedger_code((String) obj[2]);
            mDetails.setStateCode((String) obj[3]);
            clientDetails.add(mDetails);
        }
        /* end of Cash in Hand List */

        /* Bank Accounts List */
        List<Object[]> bankAccounts = new ArrayList<>();
        if (users.getBranch() != null) {
            bankAccounts = ledgerRepository.findBankAccountsByOutletIdAndBranch(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            bankAccounts = ledgerRepository.findBankAccountsByOutletId(users.getOutlet().getId());
        }
        for (int i = 0; i < bankAccounts.size(); i++) {
            ClientDetails mDetails = new ClientDetails();
            Object obj[] = bankAccounts.get(i);
            mDetails.setId(Long.parseLong(obj[0].toString()));
            mDetails.setLedger_name((String) obj[1]);
            mDetails.setLedger_code((String) obj[2]);
            mDetails.setStateCode((String) obj[3]);
            clientDetails.add(mDetails);
        }
        /* end of Bank accounts List */
        if (clientDetails.size() > 0) {
            clientsListDTO.setMessage("success");
            clientsListDTO.setResponseStatus(HttpStatus.OK.value());
            clientsListDTO.setList(clientDetails);
        } else {
            clientsListDTO.setMessage("empty list");
            clientsListDTO.setResponseStatus(HttpStatus.OK.value());
            clientsListDTO.setList(clientDetails);
        }
        return clientsListDTO;
    }

    /* Get Cash-In-Hand and Bank Account Ledger from ledger balancer summary   */
    public JsonObject getCashAcBankAccount(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonArray result = new JsonArray();
        LedgerMaster ledgerMaster = null;
        if (users.getBranch() != null) {

            ledgerMaster = ledgerRepository.findLedgerIdAndBranchIdAndName(users.getOutlet().getId(), users.getBranch().getId());
        } else {
            ledgerMaster = ledgerRepository.findLedgerIdAndName(users.getOutlet().getId());
        }
        LedgerBalanceSummary cashList = balanceSummaryRepository.findByLedgerMasterId(ledgerMaster.getId());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", cashList.getId());
        jsonObject.addProperty("name", cashList.getLedgerMaster().getLedgerName());
        jsonObject.addProperty("slug", generateSlugs.getSlug(cashList.getPrincipleGroups().getGroupName()));
        if (cashList.getDebit() != 0.0) jsonObject.addProperty("amount", cashList.getDebit());
        else jsonObject.addProperty("amount", cashList.getCredit());
        result.add(jsonObject);
        List<LedgerBalanceSummary> bankList = balanceSummaryRepository.findByPrincipleGroupsId(2L);
        for (LedgerBalanceSummary mList : bankList) {
            JsonObject jsonObject_ = new JsonObject();
            jsonObject_.addProperty("id", mList.getId());
            jsonObject_.addProperty("name", mList.getLedgerMaster().getLedgerName());
            jsonObject_.addProperty("slug", generateSlugs.getSlug(mList.getPrincipleGroups().getGroupName()));
            if (cashList.getDebit() != 0.0) jsonObject.addProperty("amount", mList.getDebit());
            else jsonObject.addProperty("amount", mList.getCredit());
            result.add(jsonObject_);
        }
        JsonObject response = new JsonObject();
        if (result.size() > 0) {
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("message", "success");
            response.add("list", result);
        } else {
            response.addProperty("responseStatus", HttpStatus.FORBIDDEN.value());
            response.addProperty("message", "empty list");
            response.add("list", result);
        }
        return response;
    }

    public JsonObject getSundryDebtorsById(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();

        LedgerMaster sundryDebtors = ledgerRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sundry_debtors_id")), true);
        if (sundryDebtors != null) {
            result.addProperty("id", sundryDebtors.getId());
            result.addProperty("sundry_debtors_name", sundryDebtors.getLedgerName());
            result.addProperty("mobile", sundryDebtors.getMobile());
            result.addProperty("address", sundryDebtors.getAddress());
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("message", "success");
            response.add("data", result);
        } else {
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("message", "empty data");
            response.add("data", result);
        }

        return response;
    }

    public JsonObject getSundryCreditorsById(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject response = new JsonObject();
        JsonObject result = new JsonObject();
        System.out.println("Sundry Creditor Id:" + request.getParameter("sundry_creditors_id"));
        LedgerMaster sundryCreditor = ledgerRepository.findByIdAndStatus(Long.parseLong(request.getParameter("sundry_creditors_id")), true);
        if (sundryCreditor != null) {
            result.addProperty("id", sundryCreditor.getId());
            result.addProperty("sundry_creditor_name", sundryCreditor.getLedgerName());
            result.addProperty("mobile", sundryCreditor.getMobile());
            result.addProperty("address", sundryCreditor.getAddress());
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("message", "success");
            response.add("data", result);
        } else {
            response.addProperty("responseStatus", HttpStatus.OK.value());
            response.addProperty("message", "success");
            response.add("data", result);
        }
        return response;
    }

    public JsonObject getGstDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<LedgerGstDetails> gstDetails = new ArrayList<>();
        Long ledgerId = Long.valueOf(request.getParameter("ledger_id"));
        gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(ledgerId, true);
        if (gstDetails != null && gstDetails.size() > 0) {
            for (LedgerGstDetails mDetails : gstDetails) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("gstNo", mDetails.getGstin());
                mObject.addProperty("dateOfRegistration", mDetails.getDateOfRegistration() != null ? mDetails.getDateOfRegistration().toString() : "");
                mObject.addProperty("pancard", mDetails.getPanCard());
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject getShippingDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<LedgerShippingAddress> shippingDetails = new ArrayList<>();
        Long ledgerId = Long.valueOf(request.getParameter("ledger_id"));
        shippingDetails = ledgerShippingDetailsRepository.findByLedgerMasterIdAndStatus(ledgerId, true);
        if (shippingDetails != null && shippingDetails.size() > 0) {
            for (LedgerShippingAddress mDetails : shippingDetails) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("district", mDetails.getDistrict());
                mObject.addProperty("shipping_address", mDetails.getShippingAddress());
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject getDeptDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<LedgerDeptDetails> deptDetails = new ArrayList<>();
        Long ledgerId = Long.valueOf(request.getParameter("ledger_id"));
        deptDetails = ledgerDeptDetailsRepository.findByLedgerMasterIdAndStatus(ledgerId, true);
        if (deptDetails != null && deptDetails.size() > 0) {
            for (LedgerDeptDetails mDetails : deptDetails) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("department", mDetails.getDept());
                mObject.addProperty("contact_no", mDetails.getContactNo());
                mObject.addProperty("contact_person", mDetails.getContactPerson());
                mObject.addProperty("email", mDetails.getEmail());
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject getBillingDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<LedgerBillingDetails> billDetails = new ArrayList<>();
        Long ledgerId = Long.valueOf(request.getParameter("ledger_id"));
        billDetails = ledgerBillingDetailsRepository.findByLedgerMasterIdAndStatus(ledgerId, true);
        if (billDetails != null && billDetails.size() > 0) {
            for (LedgerBillingDetails mDetails : billDetails) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("billing_address", mDetails.getBillingAddress());
                mObject.addProperty("district", mDetails.getDistrict());
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject getCounterCustomer(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        LedgerMaster sundryDebtors = null;
        JsonObject object = new JsonObject();
        if (users.getBranch() != null) {
            sundryDebtors = ledgerRepository.findByLedgerNameIgnoreCaseAndOutletIdAndBranchIdAndStatus("Counter Customer", users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            sundryDebtors = ledgerRepository.findByLedgerNameIgnoreCaseAndOutletIdAndStatusAndBranchIsNull("Counter Customer", users.getOutlet().getId(), true);
        }
        JsonArray result = new JsonArray();
        JsonObject response = new JsonObject();
        if (sundryDebtors != null) {
            object.addProperty("name", sundryDebtors.getLedgerName());
            object.addProperty("id", sundryDebtors.getId());
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("data", object);
        return response;
    }

    public Object validateLedgerMaster(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long branchId = null;
        Long pgroupId = null;
        String ledgerCodeId = null;
        ResponseMessage responseMessage = new ResponseMessage();
        LedgerMaster lMaster = null;
        LedgerMaster lMaster1 = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        if (users.getBranch() != null) branchId = users.getBranch().getId();
          /*  lMaster = ledgerRepository.findByOutletIdAndBranchIdAndLedgerNameIgnoreCaseAndStatus(users.getOutlet().getId(), users.getBranch().getId(), request.getParameter("ledger_name"), true);
        } else {
            lMaster = ledgerRepository.findByOutletIdAndLedgerNameIgnoreCaseAndStatus(users.getOutlet().getId(), request.getParameter("ledger_name"), true);
        }*/
        if (paramMap.containsKey("principle_group_id"))
            pgroupId = Long.parseLong(request.getParameter("principle_group_id"));
        if (paramMap.containsKey("ledger_code")) {
            ledgerCodeId = request.getParameter("ledger_code");
        }
        if (pgroupId != null) {
//            lMaster = ledgerRepository.findDuplicateWithName(users.getOutlet().getId(), branchId, Long.parseLong(request.getParameter("principle_id")), pgroupId, request.getParameter("ledger_name").toLowerCase(), true);
//            lMaster = ledgerRepository.findDuplicateWithCode(users.getOutlet().getId(), branchId, Long.parseLong(request.getParameter("principle_id")), pgroupId, request.getParameter("ledger_code").toLowerCase(), true);
            lMaster = ledgerRepository.findDuplicateWithName(users.getOutlet().getId(), branchId, Long.parseLong(request.getParameter("principle_id")), pgroupId, request.getParameter("ledger_name").toLowerCase(), true);
            lMaster1 = ledgerRepository.findDuplicateWithCode(users.getOutlet().getId(), branchId, Long.parseLong(request.getParameter("principle_id")), pgroupId, ledgerCodeId, true);
        } else {
            lMaster = ledgerRepository.findDuplicate(users.getOutlet().getId(), branchId, Long.parseLong(request.getParameter("principle_id")), request.getParameter("ledger_name").toLowerCase(), true);
        }
        if (lMaster != null || lMaster1 != null) {
            responseMessage.setMessage("Duplicate ledger");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New Ledger");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public Object getGSTListByLedgerId(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            Long ledgerId = Long.valueOf(request.getParameter("ledgerId"));
            List<LedgerGstDetails> ledgerGstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(ledgerId, true);

            JsonArray gstArray = new JsonArray();
            for (LedgerGstDetails ledgerGstDetails1 : ledgerGstDetails) {
                JsonObject gstObject = new JsonObject();
                gstObject.addProperty("id", ledgerGstDetails1.getId());
                gstObject.addProperty("gstNumber", ledgerGstDetails1.getGstin());
                gstObject.addProperty("dateOfRegistration", ledgerGstDetails1.getDateOfRegistration().toString());
                gstObject.addProperty("panNumber", ledgerGstDetails1.getPanCard());

                gstArray.add(gstObject);
            }

            response.add("response", gstArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());

            response.addProperty("message", "Failed to get gst data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object checkLedgerDrugAndFssaiExpiryByLedgerId(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            Long ledgerId = Long.valueOf(request.getParameter("ledgerId"));
            LocalDate currentDate = LocalDate.now();

            LedgerMaster ledgerMaster = ledgerRepository.findByIdAndStatus(ledgerId, true);
            if (ledgerMaster != null) {

                System.out.println("current date is okay");
                response.addProperty("response", true);
                response.addProperty("responseStatus", HttpStatus.OK.value());

                String message = "";

                if (ledgerMaster.getFssaiExpiry() != null && currentDate.compareTo(ledgerMaster.getFssaiExpiry()) > 0) {
                    System.out.println("current date is greater than fssai date");

                    message = "Fssai licence";
                    response.addProperty("message", "Fssai licence expired");
                    response.addProperty("response", false);
                    response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
                }
                if (ledgerMaster.getDrugExpiry() != null && currentDate.compareTo(ledgerMaster.getDrugExpiry()) > 0) {
                    System.out.println("current date is greater than drug expiry date");

                    if (!message.equalsIgnoreCase("")) message = message + " & ";
                    message = message + "Drug licence";
                    response.addProperty("response", false);
                    response.addProperty("responseStatus", HttpStatus.NOT_FOUND.value());
                }
                response.addProperty("message", message + " expired");
            }
            return response;
        } catch (Exception e) {
            response.addProperty("response", false);
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ledgerLogger.error("Exception:" + exceptionAsString);

        }
        return response;
    }

    /*public JsonObject ledgerDelete(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        LedgerMaster ledgerMaster = ledgerRepository.findByIdAndIsDefaultLedgerAndStatus(Long.parseLong(request.getParameter("id")), false, true);
        Long count = 0L;
        boolean flag = true;
        try {
            if (ledgerMaster != null) {
                Double openingBal = ledgerRepository.findOpeningBalance(ledgerMaster.getId());
                count = ledgerTransactionPostingsRepository.findByLedgerTranx(ledgerMaster.getId(), true);
                if (openingBal == 0 && count == 0) {
                    jsonObject.addProperty("message", "Ledger Deleted successfully");
                    jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
                    ledgerMaster.setStatus(false);
                    ledgerRepository.save(ledgerMaster);
                } else {
                    if (openingBal != 0) {
                        jsonObject.addProperty("message", "Not allowed to delete ledger,This ledger has opening balance");
                        jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
                    } else {
                        jsonObject.addProperty("message", "Not allowed to delete ledger,This ledger is used in transactions");
                        jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
                    }
                }
            } else {
                jsonObject.addProperty("message", "Not allowed to delete default ledger");
                jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return jsonObject;
    }*/

    public Object ledgerTransactionsList(HttpServletRequest request) {
        Double sumCR = 0.0;
        Double sumDR = 0.0;
        Double closingBalance = 0.0;
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();

        List ledgerMasters = new ArrayList<>();
        String searchKey = request.getParameter("search") != null ? request.getParameter("search") : "";
        System.out.println();
        Users users = null;
        try {
            users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            String query = " SELECT id FROM `ledger_master_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND" + " status=1";

            if (users.getBranch() != null) {
                query = query + " AND branch_id=" + users.getBranch().getId();
            } else {
                query = query + " AND branch_id IS NULL";
            }
            if (!searchKey.equalsIgnoreCase("")) {
                query = query + " AND (ledger_code LIKE '%" + searchKey + "%' OR ledger_name LIKE '%" + searchKey + "%' " +
                        "OR city LIKE '%" + searchKey + "%' OR mobile LIKE '%" + searchKey + "%')";
            }
            query = query + " ORDER BY ledger_name LIMIT 500 ";
//            query = query + " ORDER BY id Desc";
           /* Query q = entityManager.createNativeQuery(query, LedgerMaster.class);
            ledgerMasters = q.getResultList();*/

            Query q = entityManager.createNativeQuery(query);
            System.out.println("Query:" + query);
            ledgerMasters = q.getResultList();
            if (ledgerMasters != null && ledgerMasters.size() > 0) {
                for (Object mLedger : ledgerMasters) {
                    LedgerMaster mDetails = ledgerRepository.findByIdAndStatus(Long.parseLong(mLedger.toString()), true);
                    JsonObject mObject = new JsonObject();
                    mObject.addProperty("id", mDetails.getId());
                    mObject.addProperty("code", mDetails.getLedgerCode() != null ? mDetails.getLedgerCode() : "");
                    mObject.addProperty("ledger_name", mDetails.getLedgerName() != null ? mDetails.getLedgerName() : "");
                    mObject.addProperty("ledger_code", mDetails.getLedgerCode() != null ? mDetails.getLedgerCode() : "");
                    mObject.addProperty("unique_code", mDetails.getUniqueCode() != null ? mDetails.getUniqueCode() : "");

                    mObject.addProperty("city", mDetails.getCity() != null ? mDetails.getCity() : "");
                    mObject.addProperty("contact_number", mDetails.getMobile() != null ? mDetails.getMobile().toString() : "");
                    if (mDetails.getSalesmanId() != null) {
                        SalesManMaster salesManMaster = salesmanMasterRepository.findByIdAndStatus(mDetails.getSalesmanId(), true);
                        if (mDetails.getSalesmanId() != null) {
                            mObject.addProperty("salesmanId", salesManMaster != null ? salesManMaster.getId().toString() : "");
                        } else {
                            mObject.addProperty("salesmanId", "");
                        }
                    } else {
                        mObject.addProperty("salesmanId", "");
                    }
                    JsonArray licenseArray = new JsonArray();

                    LocalDate expDate = null;
                    if (mDetails.getIsLicense() != null) {
                        List<LedgerLicenseDetails> ledgerLicenseDetails = ledgerLicenseDetailsRepository.findByLedgerMasterIdAndStatus(mDetails.getId(), true);
                        for (LedgerLicenseDetails mLedgerLiceDetails : ledgerLicenseDetails) {
                            JsonObject mLicenseObject = new JsonObject();
                            mLicenseObject.addProperty("license_num", mLedgerLiceDetails.getLicenseNum());
                            if (mLedgerLiceDetails.getLicenseExp() != null)
                                mLicenseObject.addProperty("license_exp", mLedgerLiceDetails.getLicenseExp().toString());

                            mLicenseObject.addProperty("slug_name", mLedgerLiceDetails.getSlugName());
                            if (mLedgerLiceDetails.getSlugName().equalsIgnoreCase("drug_number") && expDate == null) {
                                expDate = mLedgerLiceDetails.getLicenseExp();
                            }
                            licenseArray.add(mLicenseObject);
                        }
                    }

                    if (mDetails.getUniqueCode().equalsIgnoreCase("SUDR") || mDetails.getUniqueCode().equalsIgnoreCase("SUCR")) {
                        mObject.add("licenseDetails", licenseArray);
                        if (expDate != null && expDate.isAfter(LocalDate.now())) {
                            //valid
                            mObject.addProperty("isLicenseExp", false);
                        } else {
                            //expired
                            mObject.addProperty("isLicenseExp", true);
                        }
                    }
                    mObject.addProperty("stateCode", mDetails.getStateCode() != null ? mDetails.getStateCode() : "");
                    System.out.println("State Code-->" + mDetails.getStateCode());
                    mObject.addProperty("stateCode", mDetails.getStateCode() != "" ? mDetails.getStateCode() : "");
                    mObject.addProperty("salesRate", mDetails.getSalesRate() != null ? mDetails.getSalesRate() : 1);
                    mObject.addProperty("balancingMethod", generateSlugs.getSlug(
                            mDetails.getBalancingMethod() != null ?
                                    (mDetails.getBalancingMethod().getBalancingMethod() != null ?
                                            mDetails.getBalancingMethod().getBalancingMethod() : "") : ""));
                    if (mDetails.getUniqueCode().equalsIgnoreCase("SUCR")) mObject.addProperty("type", "SC");
                    else if (mDetails.getUniqueCode().equalsIgnoreCase("SUDR")) mObject.addProperty("type", "SD");
                    else if (mDetails.getUniqueCode().equalsIgnoreCase("INIC")) mObject.addProperty("type", "cr");
                    else if (mDetails.getUniqueCode().equalsIgnoreCase("INEX")) mObject.addProperty("type", "dr");
                    else if (mDetails.getUniqueCode().equalsIgnoreCase("STIH") || mDetails.getUniqueCode().equalsIgnoreCase("LNADV"))
                        mObject.addProperty("type", "dr");
                    else {
                        mObject.addProperty("type", "");
                    }
                    mObject.addProperty("under_slug", mDetails.getSlugName());
                    mObject.addProperty("isFirstDiscountPerCalculate", mDetails.getIsFirstDiscountPerCalculate() != null ? mDetails.getIsFirstDiscountPerCalculate() : false);
                    mObject.addProperty("takeDiscountAmountInLumpsum", mDetails.getTakeDiscountAmountInLumpsum() != null ? mDetails.getTakeDiscountAmountInLumpsum() : false);
                /*    Double balance = balanceSummaryRepository.findBalance(mDetails.getId());
                    if (balance != null) {
                        if (balance > 0) {
                            response.addProperty("ledger_balance", balance);
                            response.addProperty("ledger_balance_type", "CR");
                        } else {
                            response.addProperty("ledger_balance", Math.abs(balance));
                            response.addProperty("ledger_balance_type", "DR");
                        }
                    }*/
                    Double openingBalance = 0.0;
                    openingBalance = ledgerRepository.findOpeningBalance(mDetails.getId());
                    sumCR = ledgerTransactionPostingsRepository.findsumCR(mDetails.getId());//-0.20
                    sumDR = ledgerTransactionPostingsRepository.findsumDR(mDetails.getId());//-0.40
                    if (openingBalance != null)
                        closingBalance = openingBalance - sumDR + sumCR;//0-(-0.40)-0.20
                    else {
                        closingBalance = 0.0 - sumDR + sumCR;//0-(-0.40)-0.20
                    }

                    mObject.addProperty("current_balance", Math.abs(numFormat.numFormat(closingBalance)));
                    if (closingBalance == 0)
                        mObject.addProperty("balance_type", mDetails.getOpeningBalType().toUpperCase());
                    else {
                        if (mDetails.getFoundations().getId() == 1L) { //Assets
                            mObject.addProperty("balance_type", closingBalance < 0 ? "DR" : "CR");
                        } else if (mDetails.getFoundations().getId() == 2L) { //Liabilities
                            mObject.addProperty("balance_type", closingBalance > 0 ? "CR" : "DR");
                        } else if (mDetails.getFoundations().getId() == 3L) {//Inconme
                            mObject.addProperty("balance_type", closingBalance > 0 ? "CR" : "DR");
                        } else if (mDetails.getFoundations().getId() == 4L) {//Expenses
                            mObject.addProperty("balance_type", closingBalance < 0 ? "DR" : "CR");
                        }
                    }

                    List<LedgerGstDetails> gstDetails = new ArrayList<>();
                    gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(mDetails.getId(), true);
                    JsonArray gstArray = new JsonArray();
                    if (gstDetails != null && gstDetails.size() > 0) {
                        for (LedgerGstDetails mGstDetails : gstDetails) {
                            JsonObject mGstObject = new JsonObject();
                            mGstObject.addProperty("id", mGstDetails.getId());
                            mGstObject.addProperty("gstNo", mGstDetails.getGstin());
                            mGstObject.addProperty("state", mGstDetails.getStateCode() != null ? mGstDetails.getStateCode() : "");
                            gstArray.add(mGstObject);
                        }
                    }
                    mObject.add("gstDetails", gstArray);
                    /***** Pending List of transactions for SC  ******/
                    JsonArray sclist = new JsonArray();
                    JsonArray orderList = new JsonArray();
                    JsonArray challanList = new JsonArray();
                    if (mDetails.getUniqueCode().equalsIgnoreCase("SUCR")) {
                        Long purordersCount = tranxPurOrderRepository.countOrders(mDetails.getId(), 1L, true);
                        Long purchallanCount = tranxPurChallanRepository.countChallan(mDetails.getId(), 1L, true);
                        mObject.addProperty("pending_orders", purordersCount);
                        mObject.addProperty("pending_challans", purchallanCount);
                    } else {
                        Long quotationCount = tranxSalesQuotationRepository.countquotation(mDetails.getId(), 1L, true);
                        Long ordersCount = tranxSalesOrderRepository.countOrders(mDetails.getId(), 1L, true);
                        Long challanCount = tranxSalesChallanRepository.countChallan(mDetails.getId(), 1L, true);
                        mObject.addProperty("pending_quotation", quotationCount);
                        mObject.addProperty("pending_orders", ordersCount);
                        mObject.addProperty("pending_challans", challanCount);
                    }
                    result.add(mObject);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ledgerLogger.error("Exception:" + exceptionAsString);
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public Object ledgerVouchersList(HttpServletRequest request) {
        Double sumCR = 0.0;
        Double sumDR = 0.0;
        Double closingBalance = 0.0;
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<LedgerMaster> ledgerMasters = new ArrayList<>();
        String searchKey = request.getParameter("search");
        Users users = null;
        users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        String query = " SELECT * FROM `ledger_master_tbl` WHERE principle_groups_id NOT IN(1,5) OR principle_groups_id IS NULL AND status = 1";

        if (users.getBranch() != null) {
            query = query + " AND branch_id=" + users.getBranch().getId();
        }
        if (!searchKey.equalsIgnoreCase("")) {
            query = query + " AND (ledger_code LIKE '%" + searchKey + "%' OR ledger_name LIKE '%" + searchKey + "%' OR city LIKE '%" + searchKey + "%' OR mobile LIKE '%" + searchKey + "%')";
        }
        System.out.println("query " + query);
        Query q = entityManager.createNativeQuery(query, LedgerMaster.class);
        ledgerMasters = q.getResultList();

        /*if (searchKey.equalsIgnoreCase("")) {
            if (users.getBranch() != null) {
                ledgerMasters = ledgerRepository.findBySCSDWithBranch(users.getOutlet().getId(), users.getBranch().getId(),
                        1L, 5L, true);
            } else {
                ledgerMasters = ledgerRepository.findBySCSD(users.getOutlet().getId(), 1L, 5L, true);
            }
        } else {
            if (users.getBranch() != null)
                ledgerMasters = ledgerRepository.findSearchKeyWithBranch(users.getOutlet().getId(),
                        users.getBranch().getId(), searchKey, 1L, 5L, true);
            else
                ledgerMasters = ledgerRepository.findSearchKey(users.getOutlet().getId(), searchKey, 1L, 5L, true);
        }*/
        System.out.println("ledgerMasters size " + ledgerMasters.size());
        if (ledgerMasters != null && ledgerMasters.size() > 0) {
            for (LedgerMaster mDetails : ledgerMasters) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("code", mDetails.getLedgerCode() != null ? mDetails.getLedgerCode() : "");
                mObject.addProperty("ledger_name", mDetails.getLedgerName());
                mObject.addProperty("ledger_code", mDetails.getLedgerCode() != null ? mDetails.getLedgerCode() : "");
                mObject.addProperty("city", mDetails.getCity() != null ? mDetails.getCity() : "");
                mObject.addProperty("contact_number", mDetails.getMobile() != null ? mDetails.getMobile().toString() : "");
                mObject.addProperty("sales_man", mDetails.getColumnA() != "" ? mDetails.getColumnA() : "");
                mObject.addProperty("stateCode", mDetails.getStateCode() != "" ? mDetails.getStateCode() : "");
                mObject.addProperty("salesRate", mDetails.getSalesRate() != null ? mDetails.getSalesRate() : 1);
                mObject.addProperty("isFirstDiscountPerCalculate", mDetails.getIsFirstDiscountPerCalculate() != null ? mDetails.getIsFirstDiscountPerCalculate() : false);
                mObject.addProperty("takeDiscountAmountInLumpsum", mDetails.getTakeDiscountAmountInLumpsum() != null ? mDetails.getTakeDiscountAmountInLumpsum() : false);

                Double balance = balanceSummaryRepository.findBalance(mDetails.getId());
                if (balance != null) {
                    if (balance > 0) {
                        response.addProperty("ledger_balance", balance);
                        response.addProperty("ledger_balance_type", "CR");
                    } else {
                        response.addProperty("ledger_balance", Math.abs(balance));
                        response.addProperty("ledger_balance_type", "DR");
                    }
                }

                try {
                    Double openingBalance = ledgerRepository.findOpeningBalance(mDetails.getId());
                    sumCR = ledgerTransactionPostingsRepository.findsumCR(mDetails.getId());//-0.20
                    sumDR = ledgerTransactionPostingsRepository.findsumDR(mDetails.getId());//-0.40
                    closingBalance = openingBalance - sumDR + sumCR;//0-(-0.40)-0.20
                } catch (Exception e) {

                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    ledgerLogger.error("Exception:" + exceptionAsString);
                }
                mObject.addProperty("current_balance", Math.abs(closingBalance));
                if (closingBalance == 0) mObject.addProperty("balance_type", mDetails.getOpeningBalType());
                else {
                    if (mDetails.getFoundations().getId() == 1L) { //Assets
                        mObject.addProperty("balance_type", closingBalance < 0 ? "DR" : "CR");
                    } else if (mDetails.getFoundations().getId() == 2L) { //Liabilities
                        mObject.addProperty("balance_type", closingBalance > 0 ? "CR" : "DR");
                    } else if (mDetails.getFoundations().getId() == 3L) {//Inconme
                        mObject.addProperty("balance_type", closingBalance > 0 ? "CR" : "DR");
                    } else if (mDetails.getFoundations().getId() == 4L) {//Expenses
                        mObject.addProperty("balance_type", closingBalance < 0 ? "DR" : "CR");
                    }
                }

                List<LedgerGstDetails> gstDetails = new ArrayList<>();
                gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(mDetails.getId(), true);
                JsonArray gstArray = new JsonArray();
                if (gstDetails != null && gstDetails.size() > 0) {
                    for (LedgerGstDetails mGstDetails : gstDetails) {
                        JsonObject mGstObject = new JsonObject();
                        mGstObject.addProperty("id", mGstDetails.getId());
                        mGstObject.addProperty("gstNo", mGstDetails.getGstin());
                        mGstObject.addProperty("state", mGstDetails.getStateCode() != null ? mGstDetails.getStateCode() : "");
                        gstArray.add(mGstObject);
                    }
                }
                mObject.add("gstDetails", gstArray);
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public Object ledgerTransactionsDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonObject mObject = new JsonObject();
        Long ledgerId = Long.parseLong(request.getParameter("ledger_id"));
        LedgerMaster ledgerMasters = null;
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long companyId = users.getCreatedBy();
        Outlet company = outletRepository.findByIdAndStatus(companyId, true);
        System.out.println(".....companyId..... " + company.getStateCode());
//        String state_code = outletRepository.getStateCodeById();
        if (users.getBranch() != null) {
            ledgerMasters = ledgerRepository.findByOutletIdAndBranchIdAndStatusAndId(users.getOutlet().getId(), users.getBranch().getId(), true, ledgerId);
        } else {
            ledgerMasters = ledgerRepository.findByOutletIdAndStatusAndIdAndBranchIsNull(users.getOutlet().getId(), true, ledgerId);
        }
        if (ledgerMasters != null) {
            mObject.addProperty("license_number", ledgerMasters.getLicenseNo() != null ? ledgerMasters.getLicenseNo() : "");
            mObject.addProperty("fssai_number", ledgerMasters.getFoodLicenseNo() != null ? ledgerMasters.getFoodLicenseNo() : "");
            mObject.addProperty("area", ledgerMasters.getArea() != null ? ledgerMasters.getArea() : "");
            mObject.addProperty("route", ledgerMasters.getRoute() != null ? ledgerMasters.getRoute() : "");
            mObject.addProperty("credit_days", ledgerMasters.getCreditDays() != null ? ledgerMasters.getCreditDays() : 0);
            mObject.addProperty("balancingMethod", ledgerMasters.getBalancingMethod() != null ? ledgerMasters.getBalancingMethod().getBalancingMethod() : "");
            if (ledgerMasters.getPrincipleGroups() != null)
                mObject.addProperty("ledger_group", ledgerMasters.getPrincipleGroups().getGroupName());
            else mObject.addProperty("ledger_group", ledgerMasters.getPrinciples().getPrincipleName());
            /***** getting GST Details ****/
            List<LedgerGstDetails> gstDetails = new ArrayList<>();
            gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(ledgerId, true);
            if (gstDetails != null && gstDetails.size() > 0) {
                mObject.addProperty("gst_number", gstDetails.get(0).getGstin());
                mObject.addProperty("gstId", gstDetails.get(0).getId());
                mObject.addProperty("stateCode", gstDetails.get(0).getStateCode());
            } else {
                mObject.addProperty("gst_number", "");
                mObject.addProperty("stateCode", ledgerMasters.getStateCode());
            }

            /***** getting Bank Details ****/
            List<LedgerBankDetails> bankDetails = new ArrayList<>();
            bankDetails = ledgerbankDetailsRepository.findByLedgerMasterIdAndStatus(ledgerId, true);
            if (bankDetails != null && bankDetails.size() > 0) {
                mObject.addProperty("bank_name", bankDetails.get(0).getBankName());
                mObject.addProperty("account_number", bankDetails.get(0).getAccountNo());
            } else {
                mObject.addProperty("bank_name", "");
                mObject.addProperty("account_number", "");
            }
            /***** getting Contact Person Details ****/
            List<LedgerDeptDetails> ledgerDeptDetails = new ArrayList<>();
            ledgerDeptDetails = ledgerDeptDetailsRepository.findByLedgerMasterIdAndStatus(ledgerId, true);
            if (ledgerDeptDetails != null && ledgerDeptDetails.size() > 0) {
                mObject.addProperty("contact_name", ledgerDeptDetails.get(0).getContactPerson());
                mObject.addProperty("contact_no", ledgerDeptDetails.get(0).getContactNo());

            } else {
                mObject.addProperty("contact_name", "");
                mObject.addProperty("contact_no", "");

            }
        }
        response.addProperty("company_state_code", company.getStateCode());
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("result", mObject);
        return response;
    }


    public JsonObject getMobileAllLedgers() {
        JsonArray result = new JsonArray();
        Double closingBalance = 0.0;
        Double sumCR = 0.0;
        Double sumDR = 0.0;
        DecimalFormat df = new DecimalFormat("0.00");
//        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<LedgerMaster> balanceSummaries = new ArrayList<>();
//        if (users.getBranch() != null) {
//            /**** Default ledgers for Branch Users *****/
//            balanceSummaries = ledgerRepository.findByOutletIdAndBranchIdAndStatusOrderByIdDesc(users.getOutlet().getId(), users.getBranch().getId(), true);
//        } else {
//        endDatep = LocalDate.parse(request.get("end_date"));
//        startDatep = LocalDate.parse(request.get("start_date"));
        balanceSummaries = ledgerRepository.findAllLedgerList(true);
//        }
        for (LedgerMaster balanceSummary : balanceSummaries) {
            Long ledgerId = balanceSummary.getId();
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", balanceSummary.getId());
            jsonObject.addProperty("foundations_name", balanceSummary.getFoundations().getFoundationName());
            if (balanceSummary.getAssociateGroups() == null) {
                if (balanceSummary.getPrinciples() != null) {
                    jsonObject.addProperty("principle_name", balanceSummary.getPrinciples().getPrincipleName());
                }
                if (balanceSummary.getPrincipleGroups() != null) {
                    jsonObject.addProperty("subprinciple_name", balanceSummary.getPrincipleGroups().getGroupName());
                } else {
                    jsonObject.addProperty("subprinciple_name", "");
                }
            } else {
                if (balanceSummary.getAssociateGroups().getPrincipleGroups() != null) {
                    jsonObject.addProperty("principle_name", balanceSummary.getPrinciples().getPrincipleName());
                    jsonObject.addProperty("subprinciple_name", balanceSummary.getAssociateGroups().getAssociatesName());
                } else {
                    jsonObject.addProperty("principle_name", balanceSummary.getAssociateGroups().getAssociatesName());
                }
            }
            try {
                Double openingBalance = ledgerRepository.findOpeningBalance(balanceSummary.getId());
                sumCR = ledgerTransactionPostingsRepository.findsumCR(balanceSummary.getId());//-0.20
                sumDR = ledgerTransactionPostingsRepository.findsumDR(balanceSummary.getId());//-0.40
                closingBalance = openingBalance - sumDR + sumCR;//0-(-0.40)-0.20
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                ledgerLogger.error("Exception:" + exceptionAsString);
            }
            jsonObject.addProperty("default_ledger", balanceSummary.getIsDefaultLedger());
            jsonObject.addProperty("ledger_form_parameter_slug", balanceSummary.getSlugName());
//            LedgerTransactionPostings mLedger = ledgerTransactionPostingsRepository.findByLedgerMasterIdAndStatus(balanceSummary.getId(), true);
            if (balanceSummary.getFoundations().getId() == 1) {
                if (closingBalance > 0) {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                    jsonObject.addProperty("ledgerType", "CR");
                } else {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("ledgerType", "DR");
                }

            } else if (balanceSummary.getFoundations().getId() == 2) {
                if (closingBalance > 0) {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                    jsonObject.addProperty("ledgerType", "CR");

                } else {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("ledgerType", "DR");
                }

            } else if (balanceSummary.getFoundations().getId() == 3) {
                if (closingBalance > 0) {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                    jsonObject.addProperty("ledgerType", "CR");
                } else {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("ledgerType", "DR");
                }

            } else if (balanceSummary.getFoundations().getId() == 4) {
                if (closingBalance < 0) {
                    jsonObject.addProperty("cr", df.format(0));
                    jsonObject.addProperty("dr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("ledgerType", "DR");
                } else {
                    jsonObject.addProperty("cr", df.format(Math.abs(closingBalance)));
                    jsonObject.addProperty("dr", df.format(0));
                    jsonObject.addProperty("ledgerType", "CR");
                }
            }
            jsonObject.addProperty("ledger_name", balanceSummary.getLedgerName());

            LocalDateTime createdate = balanceSummary.getCreatedAt();
            LocalDate localDate = createdate.toLocalDate();

            jsonObject.addProperty("date", String.valueOf(localDate));

            result.add(jsonObject);
        }
        JsonObject json = new JsonObject();
//        json.addProperty("company_name", users.getOutlet().getCompanyName());
        json.addProperty("message", "success");
        json.addProperty("responseStatus", HttpStatus.OK.value());
        json.add("responseList", result);
        return json;
    }

    public JsonObject getIndirectExpensesList(HttpServletRequest request) {
        Double sumCR = 0.0;
        Double sumDR = 0.0;
        Double closingBalance = 0.0;
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<LedgerMaster> ledgerMasters = new ArrayList<>();
        String searchKey = request.getParameter("search");
        Users users = null;
        users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));

        String query = " SELECT * FROM `ledger_master_tbl` WHERE outlet_id=" + users.getOutlet().getId() + " AND" + " principle_id=12 AND status=1";

        if (users.getBranch() != null) {
            query = query + " AND branch_id=" + users.getBranch().getId();
        }
//        if (!searchKey.equalsIgnoreCase("")) {
//            query = query + " AND (ledger_code LIKE '%" + searchKey + "%' OR ledger_name LIKE '%" + searchKey + "%' OR city LIKE '%" + searchKey + "%' OR mobile LIKE '%" + searchKey + "%')";
//        }
        System.out.println("query " + query);
        Query q = entityManager.createNativeQuery(query, LedgerMaster.class);
        ledgerMasters = q.getResultList();
        if (ledgerMasters != null && ledgerMasters.size() > 0) {
            for (LedgerMaster mDetails : ledgerMasters) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("code", mDetails.getLedgerCode() != null ? mDetails.getLedgerCode() : "");
                mObject.addProperty("ledger_name", mDetails.getLedgerName());
                mObject.addProperty("ledger_code", mDetails.getLedgerCode() != null ? mDetails.getLedgerCode() : "");
                mObject.addProperty("city", mDetails.getCity() != null ? mDetails.getCity() : "");
                mObject.addProperty("contact_number", mDetails.getMobile() != null ? mDetails.getMobile().toString() : "");
                if (mDetails.getSalesmanId() != null) {
                    SalesManMaster salesManMaster = salesmanMasterRepository.findByIdAndStatus(mDetails.getSalesmanId(), true);
                    if (mDetails.getSalesmanId() != null) {
                        mObject.addProperty("salesmanId", salesManMaster != null ? salesManMaster.getId().toString() : "");
                        //  mObject.addProperty("sales_man", salesManMaster != null ? salesManMaster.getFirstName() : "");
                    } else {
                        mObject.addProperty("salesmanId", "");
                        // mObject.addProperty("sales_man", "");
                    }
                } else {
                    mObject.addProperty("salesmanId", "");
                    //   mObject.addProperty("sales_man", "");
                }

                mObject.addProperty("stateCode", mDetails.getStateCode() != "" ? mDetails.getStateCode() : "");
                mObject.addProperty("salesRate", mDetails.getSalesRate() != null ? mDetails.getSalesRate() : 1);
                mObject.addProperty("balancingMethod", generateSlugs.getSlug(mDetails.getBalancingMethod() != null ? (mDetails.getBalancingMethod().getBalancingMethod() != null ? mDetails.getBalancingMethod().getBalancingMethod() : "") : ""));
                if (mDetails.getUniqueCode().equalsIgnoreCase("SUCR")) mObject.addProperty("type", "SC");
                if (mDetails.getUniqueCode().equalsIgnoreCase("SUDR")) mObject.addProperty("type", "SD");
                mObject.addProperty("isFirstDiscountPerCalculate", mDetails.getIsFirstDiscountPerCalculate() != null ? mDetails.getIsFirstDiscountPerCalculate() : false);
                mObject.addProperty("takeDiscountAmountInLumpsum", mDetails.getTakeDiscountAmountInLumpsum() != null ? mDetails.getTakeDiscountAmountInLumpsum() : false);

                Double balance = balanceSummaryRepository.findBalance(mDetails.getId());
                if (balance != null) {
                    if (balance > 0) {
                        response.addProperty("ledger_balance", numFormat.numFormat(balance));
                        response.addProperty("ledger_balance_type", "CR");
                    } else {
                        response.addProperty("ledger_balance", numFormat.numFormat(Math.abs(balance)));
                        response.addProperty("ledger_balance_type", "DR");
                    }
                }

                try {
                    Double openingBalance = ledgerRepository.findOpeningBalance(mDetails.getId());
                    sumCR = ledgerTransactionPostingsRepository.findsumCR(mDetails.getId());//-0.20
                    sumDR = ledgerTransactionPostingsRepository.findsumDR(mDetails.getId());//-0.40
                    closingBalance = openingBalance - sumDR + sumCR;//0-(-0.40)-0.20
                } catch (Exception e) {
                    ledgerLogger.error("Exception:" + e.getMessage());
                    e.printStackTrace();
                }
                mObject.addProperty("current_balance", Math.abs(closingBalance));
                if (closingBalance == 0)
                    mObject.addProperty("balance_type", mDetails.getOpeningBalType().toUpperCase());
                else {
                    if (mDetails.getFoundations().getId() == 1L) { //Assets
                        mObject.addProperty("balance_type", closingBalance < 0 ? "DR" : "CR");
                    } else if (mDetails.getFoundations().getId() == 2L) { //Liabilities
                        mObject.addProperty("balance_type", closingBalance > 0 ? "CR" : "DR");
                    } else if (mDetails.getFoundations().getId() == 3L) {//Inconme
                        mObject.addProperty("balance_type", closingBalance > 0 ? "CR" : "DR");
                    } else if (mDetails.getFoundations().getId() == 4L) {//Expenses
                        mObject.addProperty("balance_type", closingBalance < 0 ? "DR" : "CR");
                    }
                }

                List<LedgerGstDetails> gstDetails = new ArrayList<>();
                gstDetails = ledgerGstDetailsRepository.findByLedgerMasterIdAndStatus(mDetails.getId(), true);
                JsonArray gstArray = new JsonArray();
                if (gstDetails != null && gstDetails.size() > 0) {
                    for (LedgerGstDetails mGstDetails : gstDetails) {
                        JsonObject mGstObject = new JsonObject();
                        mGstObject.addProperty("id", mGstDetails.getId());
                        mGstObject.addProperty("gstNo", mGstDetails.getGstin());
                        mGstObject.addProperty("state", mGstDetails.getStateCode() != null ? mGstDetails.getStateCode() : "");
                        gstArray.add(mGstObject);
                    }
                }
                mObject.add("gstDetails", gstArray);
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public Object validateLedgerMasterUpdate(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Long branchId = null;
        Long pgroupId = null;
        String ledgerCodeId = null;
        ResponseMessage responseMessage = new ResponseMessage();
        LedgerMaster lMaster = null;
        LedgerMaster lMaster1 = null;
        Map<String, String[]> paramMap = request.getParameterMap();
        if (users.getBranch() != null) branchId = users.getBranch().getId();
        if (paramMap.containsKey("principle_group_id"))
            pgroupId = Long.parseLong(request.getParameter("principle_group_id"));
        if (paramMap.containsKey("ledger_code")) {
            ledgerCodeId = request.getParameter("ledger_code");
        }
        if (pgroupId != null) {
            lMaster = ledgerRepository.findDuplicateWithName(users.getOutlet().getId(), branchId, Long.parseLong(request.getParameter("principle_id")), pgroupId, request.getParameter("ledger_name").toLowerCase(), true);
            lMaster1 = ledgerRepository.findDuplicateWithCode(users.getOutlet().getId(), branchId, Long.parseLong(request.getParameter("principle_id")), pgroupId, ledgerCodeId, true);
        } else {
            lMaster = ledgerRepository.findDuplicate(users.getOutlet().getId(), branchId, Long.parseLong(request.getParameter("principle_id")), request.getParameter("ledger_name").toLowerCase(), true);
        }
        Long ledgerId = Long.parseLong(request.getParameter("id"));
        if (lMaster != null && ledgerId != lMaster.getId()) {
            responseMessage.setMessage("Duplicate ledger");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else if (lMaster1 != null && ledgerId != lMaster1.getId()) {
            responseMessage.setMessage("Duplicate ledger");
            responseMessage.setResponseStatus(HttpStatus.CONFLICT.value());
        } else {
            responseMessage.setMessage("New Ledger");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        }
        return responseMessage;
    }

    public JsonObject getBankDetails(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<LedgerBankDetails> bankDetails = new ArrayList<>();
        Long ledgerId = Long.valueOf(request.getParameter("ledger_id"));
        bankDetails = ledgerbankDetailsRepository.findByLedgerMasterIdAndStatus(ledgerId, true);
        if (bankDetails != null && bankDetails.size() > 0) {
            for (LedgerBankDetails mDetails : bankDetails) {
                JsonObject mObject = new JsonObject();
                mObject.addProperty("id", mDetails.getId());
                mObject.addProperty("bank_name", mDetails.getBankName());
                result.add(mObject);
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject ledgerDelete(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject jsonObject = new JsonObject();
        Long ledgerId = Long.parseLong(request.getParameter("id"));
        LedgerMaster mLedger = ledgerRepository.findByIdAndStatusAndIsDeleted(ledgerId, true, true);
        if (mLedger != null) {
            jsonObject.addProperty("message", "Ledger deleted successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            mLedger.setStatus(false);
            ledgerRepository.save(mLedger);
        } else {
            jsonObject.addProperty("message", "Ledger is used in transaction ,first delete transaction");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        }
        return jsonObject;
    }

    public Object uploadDocument(MultipartHttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        String imagePath = "";
        LedgerMaster ledgerMaster = new LedgerMaster();
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        if (request.getFile("documentUpload") != null) {
            MultipartFile image = request.getFile("documentUpload");
            fileStorageProperties.setUploadDir("." + File.separator + "uploads" + File.separator + "documentUpload" + File.separator);
            imagePath = fileStorageService.storeFile(image, fileStorageProperties);

//            if (request.getParameter("key").equalsIgnoreCase("aadar")){
//                if (imagePath != null) {
//                    ledgerMaster.setAadarUpload(File.separator +"uploads" + File.separator + "documentUpload" + File.separator + imagePath);
//                } else {
//                    responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//                    responseMessage.setMessage("Failed to upload documents. Please try again!");
//                    return responseMessage;
//                }
//            } else if (request.getParameter("key").equalsIgnoreCase("pan")) {
//                if (imagePath != null) {
//                    ledgerMaster.setPanUpload(File.separator +"uploads" + File.separator + "documentUpload" + File.separator + imagePath);
//
//                } else {
//                    responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//                    responseMessage.setMessage("Failed to upload documents. Please try again!");
//                    return responseMessage;
//                }
//            }else if (request.getParameter("key").equalsIgnoreCase("dl")) {
//                if (imagePath != null) {
//                    ledgerMaster.setDLUpload(File.separator +"uploads" + File.separator + "documentUpload" + File.separator + imagePath);
//                } else {
//                    responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//                    responseMessage.setMessage("Failed to upload documents. Please try again!");
//                    return responseMessage;
//                }
//            }

        } else {
            responseMessage.setMessage("Please upload document");
            responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
            return responseMessage;
        }
        try {
//            ledgerRepository.save(ledgerMaster);
            responseMessage.setData(imagePath);
            responseMessage.setMessage("Upload Document successfully");
            responseMessage.setResponseStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseMessage.setMessage("Internal Server Error");
            e.printStackTrace();
            System.out.println("Exception:" + e.getMessage());
        }
        return responseMessage;
    }

    public JsonObject getPostingsList(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray result = new JsonArray();
        List<LedgerTransactionPostings> postingDetails = new ArrayList<>();
        String tranx_code = request.getParameter("tranx_code");
        postingDetails = ledgerTransactionPostingsRepository.findByTranxCode(tranx_code);
        if (postingDetails != null && postingDetails.size() > 0) {
            for (LedgerTransactionPostings mDetails : postingDetails) {
                JsonObject mObject = new JsonObject();
                if (mDetails.getAmount() != 0.0) {
                    mObject.addProperty("id", mDetails.getId());
                    mObject.addProperty("ledger_master_id", mDetails.getLedgerMaster().getId());
                    mObject.addProperty("ledger_name", mDetails.getLedgerMaster().getLedgerName());
                    mObject.addProperty("amount", mDetails.getAmount());
                    mObject.addProperty("type", mDetails.getLedgerType());
                    if (mDetails.getLedgerType().equalsIgnoreCase("CR")) {
                        mObject.addProperty("CR", numFormat.numFormat(mDetails.getAmount()));
                        mObject.addProperty("DR", 0.00);
                    } else {
                        mObject.addProperty("DR", numFormat.numFormat(mDetails.getAmount()));
                        mObject.addProperty("CR", 0.00);
                    }
                    mObject.addProperty("transaction_date", mDetails.getTransactionDate().toString());
                    mObject.addProperty("invoice_no", mDetails.getInvoiceNo());
                    result.add(mObject);
                }
            }
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("list", result);
        return response;
    }

    public JsonObject getBankPaymentModeList(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        List<LedgerPaymentModeDetails> paymentModeDetails = new ArrayList<>();
        JsonObject response = new JsonObject();
        List<LedgerMaster> bankDetails = new ArrayList<>();
        JsonArray data = new JsonArray();

        if (users.getBranch() != null)
            bankDetails = ledgerRepository.findByUniqueCodeAndBranchIdAndOutletIdAndStatusAndColumnR("BAAC",
                    users.getBranch().getId(), users.getOutlet().getId(), true, true);
        else {
            bankDetails = ledgerRepository.findByUniqueCodeAndBranchIdIsNullAndOutletIdAndStatusAndColumnR("BAAC", users.getOutlet().getId(), true, true);
        }
        JsonObject bankObject = new JsonObject();
        for (LedgerMaster bankLedger : bankDetails) {
            paymentModeDetails = ledgerPaymentModeRepository.findByLedgerIdAndStatus(bankLedger.getId(), true);
            JsonArray bankArray = new JsonArray();
            JsonObject dataObject = new JsonObject();
            dataObject.addProperty("bank_name", bankLedger.getLedgerName());
            dataObject.addProperty("bank_name", bankLedger.getLedgerName());
            dataObject.addProperty("bankId", bankLedger.getId());
            if (paymentModeDetails != null && paymentModeDetails.size() > 0) {
                for (LedgerPaymentModeDetails mPaymentDetails : paymentModeDetails) {
                    JsonObject mObject = new JsonObject();
                    PaymentModeMaster paymentModeMaster = paymentModeMasterRepository.findById(mPaymentDetails.getPaymentModeMasterId()).get();
                    mObject.addProperty("modeId", paymentModeMaster.getId());
                    mObject.addProperty("label", paymentModeMaster.getPaymentMode());
                    mObject.addProperty("amount", 0);
                    mObject.addProperty("refId", "");
                    bankArray.add(mObject);
                }
            }
            dataObject.add("payment_modes", bankArray);
            data.add(dataObject);
        }
        response.addProperty("message", "success");
        response.addProperty("responseStatus", HttpStatus.OK.value());
        response.add("data", data);
        return response;
    }


    public JsonObject getGvBankLedgers(HttpServletRequest request) {
        Users cadmin = usersRepository.findTop1ByUserRoleIgnoreCaseAndCompanyCode("cadmin", "gvmh001");
        String cadminToken = jwtRequestFilter.getTokenFromUsername(cadmin.getUsername());
        System.out.println("cadminToken :" + cadminToken);

        Long outletId = cadmin.getOutlet().getId();
        Long branchId = cadmin.getBranch() != null ? cadmin.getBranch().getId() : null;

        JsonObject response = new JsonObject();
        List<LedgerMaster> bankDetails = new ArrayList<>();

        if (branchId != null)
            bankDetails = ledgerRepository.findByUniqueCodeAndBranchIdAndOutletIdAndStatus("BAAC",
                    branchId, outletId, true);
        else {
            bankDetails = ledgerRepository.findByUniqueCodeAndOutletIdAndStatusAndBranchIdIsNull("BAAC", outletId, true);
        }
        Outlet cOutlet = cadmin.getOutlet();
        JsonObject GSTData = new JsonObject();
        JsonObject LicenseData = new JsonObject();
        JsonObject FoodLicenseData = new JsonObject();
        JsonObject MfgLicenseData = new JsonObject();

        if (cadmin.getOutlet().getGstApplicable()) {
            GSTData.addProperty("gstNumber", cOutlet.getGstNumber());
            if (cOutlet.getGstApplicableDate() != null) {
                GSTData.addProperty("regDate", cOutlet.getGstApplicableDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            GSTData.addProperty("gstType", cOutlet.getGstTypeMaster().getGstType());
        }

        if (cOutlet.getLicenseNo() != null) {
            JsonObject jsonObject = new JsonObject();
            LicenseData.addProperty("licenseNumber", cOutlet.getLicenseNo());
            if (cOutlet.getLicenseExpiry() != null) {
                LicenseData.addProperty("licenseExpiry", cOutlet.getLicenseExpiry().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

        }

        if (cOutlet.getFoodLicenseNo() != null) {
            FoodLicenseData.addProperty("foodLicenseNumber", cOutlet.getFoodLicenseNo());
            if (cOutlet.getFoodLicenseExpiry() != null) {
                FoodLicenseData.addProperty("foodLicenseExpiry", cOutlet.getFoodLicenseExpiry().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

        }

        if (cOutlet.getManufacturingLicenseNo() != null) {
            MfgLicenseData.addProperty("mfgLicenseNumber", cOutlet.getManufacturingLicenseNo());
            if (cOutlet.getManufacturingLicenseExpiry() != null) {
                MfgLicenseData.addProperty("mfgLicenseExpiry", cOutlet.getManufacturingLicenseExpiry().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        }


        JsonArray data = new JsonArray();
        for (LedgerMaster bankLedger : bankDetails) {
            JsonObject dataObject = new JsonObject();
            dataObject.addProperty("ledgerId", bankLedger.getId());
            dataObject.addProperty("ledgerName", bankLedger.getLedgerName());
            dataObject.addProperty("bankName", bankLedger.getBankName());
            dataObject.addProperty("accountNo", bankLedger.getAccountNumber());
            dataObject.addProperty("ifsc", bankLedger.getIfsc());
            dataObject.addProperty("branchName", bankLedger.getBankBranch());
            data.add(dataObject);
        }

        response.add("data", data);
        response.add("gstData", GSTData);
        response.add("licenseData", LicenseData);
        response.add("FoodLicenseData", FoodLicenseData);
        response.add("MfgLicenseData", MfgLicenseData);
        response.addProperty("responseStatus", HttpStatus.OK.value());
        return response;
    }
}

