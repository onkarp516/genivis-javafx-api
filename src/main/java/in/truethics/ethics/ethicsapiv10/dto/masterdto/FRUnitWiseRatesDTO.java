package in.truethics.ethics.ethicsapiv10.dto.masterdto;

import lombok.Data;

@Data
public class FRUnitWiseRatesDTO {
    private long unitId;
    private String unitName;
    private double unitConv;
    private double closingstk;
    private double actstkcheck;
    private String rateUnitName;
    private double fsrmh;
    private double fsrai;
    private double csrmh;
    private double csrai;
    private double mrp;
    private double purchaserate;
    private boolean isNegetive;
}
