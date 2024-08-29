package in.truethics.ethics.ethicsapiv10.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/* get Product by Id */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Double minimumStock;
    private Double maximumStock;
    private String category;
    private String group;
    private String subGroup;
    private String hsn;
    private String taxType;
    private Double cost;
    private Long id;
    private String product_name;
    private String search_code;
    private String description;
    private String brand;
    private String packing;
    private String barcode;
    private String unit;
    private Double mrp;
    private Double sales_rate;
    private Double purchase_rate;
    private Boolean isWarrantyApplicable;
    private Integer warrantyDays;
    private Boolean isSerialNumber;
    private Boolean isBatchNumber;
    private Boolean isNegativeStocks;
    private Boolean isInventory;
    private Integer unitCount;
    private Boolean isMultiUnits;
    private Boolean isPackaging;
    private Integer packingCount;
    private String alias;
    private Long hsnId;
    private Long groupId;
    private Long subGroupId;
    private Long categoryId;
    private Long subCategoryId;
    private Long taxMasterId;
    private String applicableDate;
    private Boolean isBrand;
    private Boolean isGroup;
    private Boolean isCategory;
    private Boolean isSubcategory;
    private Boolean isPackage;
    private Double opening_stocks;
    private Double closing_stocks;
    private Long unitId;
    private Double fsrmh;
    private Double fsrai;
    private Double csrmh;
    private Double csrai;
    private List<ProductUnitDTO> units = new ArrayList<>();


}
