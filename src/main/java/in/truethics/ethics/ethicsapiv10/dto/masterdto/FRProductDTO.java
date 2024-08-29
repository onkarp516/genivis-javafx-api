package in.truethics.ethics.ethicsapiv10.dto.masterdto;

import lombok.Data;
import java.util.List;

@Data
public class FRProductDTO {
    private Long id;
    private String code;
    private String product_name;
    private String packing;
    private Long packing_id;
    private String barcode;
    private String batch_expiry;
    private Double mrp;
    private Double sales_rate;
    private Double purchaserate;
    private Double current_stock;
    private String unit;
    private Long unit_id;
    private Boolean is_negative;
    private String hsn;
    private Long hsn_id;
    private String tax_type;
    private Double tax_per;
    private Double igst;
    private Double cgst;
    private Double sgst;
    private String brand;
    private List<FRUnitWiseRatesDTO> unitList;
}
