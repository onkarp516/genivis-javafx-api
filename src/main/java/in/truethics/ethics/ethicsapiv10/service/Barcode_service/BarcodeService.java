package in.truethics.ethics.ethicsapiv10.service.Barcode_service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.GenerateDates;
import in.truethics.ethics.ethicsapiv10.common.GenerateFiscalYear;
import in.truethics.ethics.ethicsapiv10.model.barcode.BarcodeHome;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.Units;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.BarcodeHomeRepository;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.BarcodeRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.UnitsRepository;
import in.truethics.ethics.ethicsapiv10.repository.product_barcode.ProductBarcodeRepository;
import in.truethics.ethics.ethicsapiv10.util.Constants;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class BarcodeService {

    @Autowired
    private ProductBarcodeRepository barcodeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UnitsRepository unitsRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;
    @Autowired
    private BarcodeHomeRepository barcodeHomeRepository;

    private static final Logger barcodeLogger = LogManager.getLogger(BarcodeService.class);


    public JsonObject createBarcode(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        String firstLetter = users.getOutlet().getCompanyName().substring(0, 3);
        Map<String, String[]> paramMap = request.getParameterMap();
        JsonObject object = new JsonObject();
        JsonObject result = new JsonObject();
        String bCode = "";
        ProductBarcode barcode = new ProductBarcode();
        Product product = productRepository.findByIdAndStatus(Long.parseLong(request.getParameter("productId")), true);
        if (product != null) barcode.setProduct(product);
        barcode.setQnty(1);
        barcode.setMrp(Double.parseDouble(request.getParameter("baseamt")));
        Units units = unitsRepository.findByIdAndStatus(Long.parseLong(request.getParameter("unitId")), true);
      //  barcode.setUnits(units);
        barcode.setStatus(true);
        barcode.setEnable(true);
        barcode.setOutlet(users.getOutlet());
        if (users.getBranch() != null) barcode.setBranch(users.getBranch());
        LocalDate currentDate = LocalDate.now();
        /*     fiscal year mapping  */
        FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
        if (fiscalYear != null) {
            barcode.setFiscalYear(fiscalYear);
        }
        if (paramMap.containsKey("code")) {
            //barcode.setBarcodeUniqueCode(request.getParameter("code"));
            barcode.setIsAuto(true);
        } else {
            Long lastRecord = barcodeRepository.findLastRecord(users.getOutlet().getId());
            GenerateDates generateDates = new GenerateDates();
            if (lastRecord != null) {
                String serailNo = String.format("%05d", lastRecord + 1);// 5 digit serial number
                bCode = firstLetter + generateDates.getCurrentMonth().substring(0, 3) + serailNo;
              //  barcode.setBarcodeUniqueCode(bCode);
                barcode.setIsAuto(false);
            }
        }
        barcode.setCreatedBy(users.getId());
        try {
            barcodeRepository.save(barcode);
            object.addProperty("barcode", bCode);
        } catch (Exception e) {
            e.printStackTrace();
            barcodeLogger.error("Exception in createBarcode:" + e.getMessage());
        }
        result.addProperty("mesage", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.add("data", object);
        return result;
    }



    public JsonObject getBarcode(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject object = new JsonObject();
        JsonObject result = new JsonObject();
        Long product_id = Long.parseLong(request.getParameter("product_id"));
        ProductBarcode productBarcode = barcodeRepository.findByProductIdAndOutletIdAndStatus(product_id, users.getOutlet().getId(), true);
        if (productBarcode != null) {
            object.addProperty("product_name", productBarcode.getProduct().getProductName());
           // object.addProperty("barcode", productBarcode.getBarcodeUniqueCode());
            object.addProperty("qty", productBarcode.getQnty());
         //   object.addProperty("package_name", productBarcode.getPackingMaster() != null ? productBarcode.getPackingMaster().getPackName() : "");
           // object.addProperty("unit_name", productBarcode.getUnits() != null ? productBarcode.getUnits().getUnitName() : "");
        }
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.add("data", object);
        return result;
    }



    public JsonObject getBarcodeHomePath(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        BarcodeHome barcodeHome =  barcodeHomeRepository.findFirstByStatus(true);
        JsonObject jsonObject=new JsonObject();
        JsonObject result=new JsonObject();
        if(barcodeHome!=null){
            jsonObject.addProperty("id",barcodeHome.getId());
            jsonObject.addProperty("barcode_home_path",barcodeHome.getBarcodeHomePath());
            jsonObject.addProperty("prn_file_name",barcodeHome.getPrnFileName());
            result.addProperty("status",HttpStatus.OK.value());
            result.addProperty("message","success");
            result.add("data",jsonObject);

        }else{
            result.addProperty("status",HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.addProperty("message","No records found");
            result.add("data",jsonObject);
        }
        return result;
    }



    public JsonObject updateBarcodeHomePath(HttpServletRequest request) {

        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject result = new JsonObject();
        Long id = Long.parseLong(request.getParameter("id"));

        BarcodeHome barcodeHome = barcodeHomeRepository.findByIdAndStatus(id,true);
        JsonObject jsonObject=new JsonObject();

        if(barcodeHome!=null){
            //Barcode Home Path found, so update the record

            barcodeHome.setPrnFileName(request.getParameter("prf_file_name"));
            barcodeHome.setBarcodeHomePath(request.getParameter("barcode_home_path"));
            barcodeHome.setStatus(true);
            barcodeHome.setOutletId(users.getOutlet().getId());
            if(users.getBranch()!=null)
                barcodeHome.setBranchId(users.getBranch().getId());
            barcodeHome.setCreatedBy(users.getId());

            barcodeHomeRepository.save(barcodeHome);

            jsonObject.addProperty("id",barcodeHome.getId());
            jsonObject.addProperty("barcode_home_path",barcodeHome.getBarcodeHomePath());
            jsonObject.addProperty("prn_file_name",barcodeHome.getPrnFileName());

            result.addProperty("status",HttpStatus.OK.value());
            result.addProperty("message","Barcode Path updated successfully!");
            result.add("data",jsonObject);

        }else{
            //No record found so insert new record for barcode home path
            barcodeHome=new BarcodeHome();
            barcodeHome.setPrnFileName(request.getParameter("prf_file_name"));
            barcodeHome.setBarcodeHomePath(request.getParameter("barcode_home_path"));
            barcodeHome.setStatus(true);
            barcodeHome.setOutletId(users.getOutlet().getId());
            if(users.getBranch()!=null)
                barcodeHome.setBranchId(users.getBranch().getId());
            barcodeHome.setCreatedBy(users.getId());
            BarcodeHome barcodeHomeResult = barcodeHomeRepository.save(barcodeHome);

                jsonObject.addProperty("id",barcodeHomeResult.getId());
                jsonObject.addProperty("barcode_home_path",barcodeHomeResult.getBarcodeHomePath());
                jsonObject.addProperty("prn_file_name",barcodeHomeResult.getPrnFileName());

                result.addProperty("status",HttpStatus.OK.value());
                result.addProperty("message","Barcode Path created successfully!");
                result.add("data",jsonObject);

        }


        return result;
    }


    public JsonObject createBarcodeHomePath(HttpServletRequest request) {
        return null;
    }
}
