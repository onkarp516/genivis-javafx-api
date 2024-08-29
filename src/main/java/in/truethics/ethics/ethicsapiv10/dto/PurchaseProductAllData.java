package in.truethics.ethics.ethicsapiv10.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PurchaseProductAllData {
    private Long id;
    private String productName;
    private String productCode;
    private double igst;
    private double cgst;
    private double sgst;
    private Long groupId;
    private String groupName;
    private Long subGroupId;
    private String subGroupName;
    private Long categoryId;
    private String categoryName;
    private Long subCategoryId;
    private String subCategoryName;
    private Long hsnId;
    private String hsnNo;
    private Long taxMasterId;
    private Boolean isSerialNumber;
    private Boolean isBatchNumber;
    private Boolean isNegativeStocks;
    private List<UnitDTO> units = new ArrayList<>();


}
