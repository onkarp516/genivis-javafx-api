package in.truethics.ethics.ethicsapiv10.dto;

import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseProductData {
    private Long id;
    private Long hsnId;
    private String productName;
    private String productCode;
    private double igst;
    private double cgst;
    private double sgst;
    private Boolean isSerialNumber;
    private Boolean isNegativeStocks;
    private List<ProductUnitPacking> units = new ArrayList<>();
}
