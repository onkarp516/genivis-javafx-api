package in.truethics.ethics.ethicsapiv10.model.inventory;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/* units of individual product*/
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_unit_packing_tbl")
public class ProductUnitPacking {
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
    @Column(name = "min_ratea")
    private Double minRateA;//Sales Rate
    @Column(name = "min_rateb")
    private Double minRateB;
    @Column(name = "min_ratec")
    private Double minRateC;
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

    @ManyToOne
    @JoinColumn(name = "units_id")
    @JsonManagedReference
    private Units units;

    @ManyToOne
    @JoinColumn(name = "packing_master_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    @ManyToOne
    @JoinColumn(name = "flavour_master_id")
    @JsonManagedReference
    private FlavourMaster flavourMaster;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    @JsonManagedReference
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonManagedReference
    private Group group;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonManagedReference
    private Category category;

    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    @JsonManagedReference
    private Subcategory subcategory;

    @ManyToOne
    @JoinColumn(name = "hsn_id")
    @JsonManagedReference
    private ProductHsn productHsn;

    @ManyToOne
    @JoinColumn(name = "taxmaster_id")
    @JsonManagedReference
    private TaxMaster taxMaster;

    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @Column(name = "min_qty")
    private Double minQty;
    @Column(name = "max_qty")
    private Double maxQty;
    @Column(name = "opening_stocks")
    private Double openingStocks;
    private Double costing;
    @Column(name = "costing_with_tax")
    private Double costingWithTax;
    @Column(name = "fsrmh")
    private Double fsrmh;
    @Column(name = "fsrai")
    private Double fsrai;
    @Column(name = "csrmh")
    private Double csrmh;
    @Column(name = "csrai")
    private Double csrai;
    @Column(name = "is_rate")
    private Boolean isRate;

    @ManyToOne
    @JoinColumn(name = "level_a_id")
    @JsonManagedReference
    private LevelA levelA;

    @ManyToOne
    @JoinColumn(name = "level_b_id")
    @JsonManagedReference
    private LevelB levelB;

    @ManyToOne
    @JoinColumn(name = "level_c_id")
    @JsonManagedReference
    private LevelC levelC;
    /*** END ****/

}

