package in.truethics.ethics.ethicsapiv10.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* get Product Unit by ProductId */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUnitDTO {
    private Long unitDetailId;
    private Long unitId;
    private String unitType;
    private Double saleRate;
    private Double minSaleRate;
    private Double purchaseRate;
    private Double mrp;
    private Double minQty;
    private Double maxQty;
    private Double minDisPer;
    private Double maxDisPer;
    private Double minDisAmt;
    private Double maxDisAmt;
    private Double unitConversion;
    private Double unitConvMargn;
    private Long packageId;
    private Long flavourId;
    private Long brandId;
    private Long groupId;
    private Long categoryId;
    private Long subcategoryId;

}
