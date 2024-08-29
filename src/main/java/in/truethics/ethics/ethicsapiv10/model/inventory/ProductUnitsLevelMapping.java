package in.truethics.ethics.ethicsapiv10.model.inventory;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.TaxMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_units_level_tbl")
public class ProductUnitsLevelMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "unit_conversion")
    private Double unitConversion;
    @Column(name = "unit_conv_margn")
    private Double unitConvMargn;
    @Column(name = "purchase_rate")
    private Double purchaseRate;
    private Double mrp;
    @Column(name = "is_negative_stocks")
    private Boolean isNegativeStocks;
    @Column(name = "min_sales_ratea")
    private Double minSalesRateA;
    @Column(name = "min_sales_rateb")
    private Double minSalesRateB;
    @Column(name = "min_sales_ratec")
    private Double minSalesRateC;
    @Column(name = "tax_applicable_date")
    private LocalDate taxApplicableDate;
    @Column(name = "max_discount")
    private Double maxDiscount;
    @Column(name = "min_discount")
    private Double minDiscount;
    @Column(name = "min_margin")
    private Double minMargin;
    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonManagedReference
    private Product product;
    @Column(name = "sub_filter_masters")
    private String subFilterMasters; //multiple level comma seperated ids
    @ManyToOne
    @JoinColumn(name = "hsn_id")
    @JsonManagedReference
    private ProductHsn productHsn;

    @ManyToOne
    @JoinColumn(name = "taxmaster_id")
    @JsonManagedReference
    private TaxMaster taxMaster;



}
