package in.truethics.ethics.ethicsapiv10.controller.gstr;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.Gstr_Service.GSTR1Service;
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
public class GSTRController {
    /* find all Sales invoices of outletwise limit 50 */
    @Autowired
    private GSTR1Service service;
    @PostMapping(path = "/get_GSTR1_data")
    public Object getGSTR1Data( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1Data(request);
      return  mObject.toString();
    }
    @PostMapping(path = "/get_GSTR1_B2B1_data")
    public Object getGSTR1DataScreen1( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1DataScreen1(request);
        return  mObject.toString();
    }

    @PostMapping(path = "/excel_export_GSTR1_B2B1_data")
    public Object exportExcelGSTR1B2B1Data(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "GSTR1_B2B1_ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelGSTR1B2B1Data(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
//
    @PostMapping(path = "/get_GSTR1_B2CL_data")
    public Object getGSTR1B2CLData( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1B2CLData(request);
        return  mObject.toString();
    }
//
    @PostMapping(path = "/get_GSTR1_B2CS_data")
    public Object getGSTR1B2CSData( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1B2CSData(request);
        return  mObject.toString();
    }
//
    @PostMapping(path = "/export_excel_GSTR1_sales_data")
    public Object ExportExcelGSTR1B2BSalesData(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "GSTR1_B2C_ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(service.ExportExcelGSTR1B2BSalesData(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
//
//    //   ExportExcel Api for GSTR1 B2C Large screen2 outward data
    @PostMapping(path = "/excel_export_GSTR1_B2CLarge1_data")
    public Object exportExcelGSTR1B2C1Data(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "GSTR1_B2C1_ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelGSTR1B2C1Data(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
//
//    //   ExportExcel Api for GSTR1 B2C Large screen2 outward data
    @PostMapping(path = "/excel_export_GSTR1_B2CLarge2_data")
    public Object exportExcelGSTR1B2C2Data(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "GSTR1_B2C2_ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelGSTR1B2C2Data(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
//    //   ExportExcel Api for GSTR1 B2C small screen1 outward data
    @PostMapping(path = "/excel_export_GSTR1_B2Csmall1_data")
    public Object exportExcelGSTR1B2C1Small(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "GSTR1_B2C_small_ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelGSTR1B2C1Small(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
//
//    //   ExportExcel Api for GSTR1 B2C small screen2 outward data
    @PostMapping(path = "/excel_export_GSTR1_B2Csmall2_data")
    public Object exportExcelGSTR1B2Csmall2Data(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "GSTR1_B2C2_ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(service.exportExcelGSTR1B2Csmall2Data(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    //    GSTR1 Credit/Debit Note Registered Api
    @PostMapping(path = "/get_GSTR1_CRNoteReg_data")
    public Object getGSTR2DRNOTEReg( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR2CRNOTEReg(request);
        return  mObject.toString();
    }

    //    GSTR1 Credit/Debit Note Un-Registered Api
    @PostMapping(path = "/get_GSTR2_CRNoteUnreg_data")
    public Object getGSTR2CRNOTEUnreg( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR2CRNOTEUnreg(request);
        return  mObject.toString();
    }

    //    GSTR1 Nil-rated Api
    @PostMapping(path = "/get_GSTR_1NilRate_data")
    public Object getGSTR1NIlRATEReg( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1NIlRATEReg(request);
        return  mObject.toString();
    }

    //    GSTR1 Main Screen B2B data Api
    @PostMapping(path = "/get_GSTR1_mainScreenb2b")
    public Object getGSTR1MainScreen1( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1MainScreen1(request);
        return  mObject.toString();
    }
    //    GSTR1 Main Screen B2C large data Api
    @PostMapping(path = "/get_GSTR1_mainScreenb2cLarge")
    public Object getGSTR1MainScreenB2CLarge( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1MainScreenB2CLarge(request);
        return  mObject.toString();
    }
    //    GSTR1 Main Screen B2C small data Api
    @PostMapping(path = "/get_GSTR1_mainScreenb2cSmall")
    public Object getGSTR1MainScreenB2Csmall( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1MainScreenB2Csmall(request);
        return  mObject.toString();
    }

    //    GSTR1 Main Screen creditNote Registered api data
    @PostMapping(path = "/get_GSTR1_mainScreenCRNote_reg")
    public Object getGSTR1MainScreenCRNoteReg( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1MainScreenCRNoteReg(request);
        return  mObject.toString();
    }

    //    GSTR1 Main Screen creditNote Registered api data
    @PostMapping(path = "/get_GSTR1_mainScreenCRNote_Unreg")
    public Object getGSTR1MainScreenCRNoteUnreg( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1MainScreenCRNoteUnreg(request);
        return  mObject.toString();
    }

    //    GSTR1 Main Screen HSN api data
    @PostMapping(path = "/get_GSTR1_mainScreenHSN")
    public Object getGSTR1MainScreenHSN( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1MainScreenHSN(request);
        return  mObject.toString();
    }

    //    Api for GSTR1 nilrated and Exempted excel export
    @PostMapping(path = "/excel_export_GSTR1_nilrated")
    public Object excelExportGSTR1Nilrated(@RequestBody Map<String, String> jsonRequest, HttpServletRequest request, HttpServletResponse response) {
        String filename = "GSTR1_nilRated_ExcelSheet.xlsx";
        InputStreamResource file = new InputStreamResource(service.excelExportGSTR1Nilrated(jsonRequest, request));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @PostMapping(path = "/get_GSTR1_hsn_data")
    public Object getGSTR1HsnData( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1HsnData(request);
        return  mObject.toString();
    }

    @PostMapping(path = "/get_GSTR1_hsn_screen2")
    public Object getGSTR1HsnScreen2( HttpServletRequest request) {
        JsonObject mObject =service.getGSTR1HsnScreen2(request);
        return  mObject.toString();
    }
}
