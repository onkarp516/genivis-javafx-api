package in.truethics.ethics.ethicsapiv10.controller.reports.stock;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.ProductService;
import in.truethics.ethics.ethicsapiv10.service.reports_service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    /****** WholeStock and Available Stock and Batch Stock product details ********/
    @GetMapping(path = "/get_whole_stock_product")
    public Object getWholeStockProducts(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        result = stockService.getWholeStockProducts(request);
        return result.toString();
    }

    //API for whole Stock Screen-1
    @PostMapping(path = "/exportToExcelWholeStock1")
    public ResponseEntity<?> exportExcelWholeStock1(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
      try {
          String filename = "excelExportStockReport1.xlsx";
          InputStreamResource file = new InputStreamResource(stockService.exportExcelWholeStock1(jsonRequest, request));
          return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                  .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                  .body(file);
      }catch(Exception e){
          System.out.println("Error in exporting excel "+e);
          return ResponseEntity.ok("");
      }
    }

    @PostMapping(path="/get_narcotic_product")
    public Object getNarcoticProduct(HttpServletRequest request){
        return stockService.getNarcoticProduct(request).toString();
    }
    @PostMapping(path="/get_narcotic_pur_product")
    public Object getNarcoticPurProduct(HttpServletRequest request){
        return stockService.getNarcoticPurProduct(request).toString();
    }
    @PostMapping(path="/get_scheduleH1_product")
    public Object getScheduleH1Product(HttpServletRequest request){
        return stockService.getScheduleH1Product(request).toString();
    }
    @PostMapping(path="/get_scheduleH1_Pur_product")
    public Object getScheduleH1PurProduct(HttpServletRequest request){
        return stockService.getScheduleH1PurProduct(request).toString();
    }
    @PostMapping(path="/get_scheduleH_product")
    public Object getScheduleHProduct(HttpServletRequest request){
        return stockService.getScheduleHProduct(request).toString();
    }
    @PostMapping(path="/get_scheduleH_Pur_product")
    public Object getScheduleHPurProduct(HttpServletRequest request){
        return stockService.getScheduleHPurProduct(request).toString();
    }

    /****** WholeStock and Available Stock Monthwise product details ********/
    @PostMapping(path = "/get_monthwise_whole_stock_details")
    public Object getMonthwiseWholeStockDetails(HttpServletRequest request) {
        return stockService.getMonthwiseWholeStockDetails(request).toString();
    }

    @PostMapping(path = "/get_monthwise_whole_stock_prdtranx_details")
    public Object getMonthwiseWholeStockDetailsPrdTranx(HttpServletRequest request) {
        return stockService.getMonthwiseWholeStockDetailsPrdTranx(request).toString();
    }

    /****** Batch wise Monthwise product details ********/
    @PostMapping(path = "/get_monthwise_batch_stock_details")
    public Object getMonthwiseBatchStockDetails(HttpServletRequest request) {
        return stockService.getMonthwiseBatchStockDetails(request).toString();
    }

    /****** Expiry product details ********/
    @GetMapping(path = "/get_expiry_product")
    public Object getExpiryProducts(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        result = stockService.getExpiredProducts(request);
        return result.toString();
    }

    /****** Expiry product details : Screen 2 ********/
    @PostMapping(path = "/get_expiry_product_monthwise")
    public Object getExpiryProductsMonthwise(HttpServletRequest request) {
        return stockService.getExpiryProductsMonthwise(request).toString();
    }

    /****** Expiry product details : Screen 3 ********/
    @PostMapping(path = "/get_expiry_product_details")
    public Object getExpiryProductsDetails(HttpServletRequest request) {
        return stockService.getExpiryProductsDetails(request).toString();
    }

    @PostMapping(path = "/get_allstock_valuation")
    public Object getAllstockValuation(HttpServletRequest request) {
        return stockService.getAllstockValuation(request).toString();
    }
    @PostMapping(path = "/get_monthwise_stock_valuation_details")
    public Object getMonthwiseStockValuationDetails(HttpServletRequest request) {
        return stockService.getMonthwiseStockValuationDetails(request).toString();
    }

    @PostMapping(path = "/get_allstock_Report")
    public Object getAllstockReport(HttpServletRequest request) {
        return stockService.getAllstockReport(request).toString();
    }
    @PostMapping(path = "/get_allstockValuation_Report")
    public Object getAllstockValReport(HttpServletRequest request) {
        return stockService.getAllstockValReport(request).toString();
    }

    @PostMapping(path = "/get_payable_report")
    public Object getPayableReport(HttpServletRequest request) {
        return stockService.getPayableReport(request).toString();
    }

    @PostMapping(path = "/get_receivable_report")
    public Object getReceivableReport(HttpServletRequest request) {
        return stockService.getReceivableReport(request).toString();
    }



    @PostMapping(path = "/exportToExcelPurchaseReg")
    public ResponseEntity<?> exportToExcelPurchaseReg(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "exportExcelPurchaseReg.xlsx";
            InputStreamResource file = new InputStreamResource(stockService.exportToExcelPurchaseReg(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }
    //API for whole Stock Screen-1
    @PostMapping(path = "/exportToExcelStockReportSC2")
    public ResponseEntity<?> exportExcelWholeStock2(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "excelExportStockReport2.xlsx";
            InputStreamResource file = new InputStreamResource(stockService.exportExcelWholeStock2(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        }catch(Exception e){
            System.out.println("Error in exporting excel "+e);
            return ResponseEntity.ok("");
        }
    }
//    @PostMapping(path = "/exportToExcelStockReportSC3")
//    public ResponseEntity<?> exportExcelWholeStock3(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
//        try {
//            String filename = "excelExportStockReport3.xlsx";
//            InputStreamResource file = new InputStreamResource(stockService.exportExcelWholeStock3(jsonRequest, request));
//            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
//                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
//                    .body(file);
//        }catch(Exception e){
//            System.out.println("Error in exporting excel "+e);
//            return ResponseEntity.ok("");
//        }
//    }


    @PostMapping(path = "/exportToExcelPayable")
    public ResponseEntity<?> exportToExcelPayable(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "exportExcelPayable.xlsx";
            InputStreamResource file = new InputStreamResource(stockService.exportToExcelPayable(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

    @PostMapping(path = "/exportToExcelReceivable")
    public ResponseEntity<?> exportToExcelReceivable(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = "exportToExcelReceivable.xlsx";
            InputStreamResource file = new InputStreamResource(stockService.exportToExcelReceivable(jsonRequest, request));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (Exception e) {
            System.out.println("Error in exporting excel " + e);
            return ResponseEntity.ok("");
        }
    }

}
