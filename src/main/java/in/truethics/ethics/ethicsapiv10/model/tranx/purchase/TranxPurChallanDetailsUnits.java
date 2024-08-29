package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_purchase_challan_details_units_tbl")
public class TranxPurChallanDetailsUnits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "purchase_challan_id")
    @JsonManagedReference
    private TranxPurChallan tranxPurChallan;

    @ManyToOne
    @JoinColumn(name = "purchase_challan_details_id")
    @JsonManagedReference
    private TranxPurChallanDetails tranxPurChallanDetails;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    @JsonManagedReference
    private Units units;

    @ManyToOne
    @JoinColumn(name = "packaging_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    @ManyToOne
    @JoinColumn(name = "flavour_master_id")
    @JsonManagedReference
    private FlavourMaster flavourMaster;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    @JsonManagedReference
    private ProductBatchNo productBatchNo;

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

    @Column(name = "unit_conversions")
    private Double unitConversions;
    private Double qty;
    private Double rate;

    @Column(name = "base_amt")
    private Double baseAmt;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "discount_per")
    private Double discountPer;

    @Column(name = "discount_amount_cal")
    private Double discountAmountCal;

    @Column(name = "discount_per_cal")
    private Double discountPerCal;

    private Double igst;
    private Double sgst;
    private Double cgst;

    @Column(name = "total_igst")
    private Double totalIgst;

    @Column(name = "total_sgst")
    private Double totalSgst;

    @Column(name = "total_cgst")
    private Double totalCgst;

    @Column(name = "final_amount")
    private Double finalAmount;

    @CreationTimestamp

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;
    @UpdateTimestamp

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
    private Boolean status;
    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/

    @Column(name = "free_qty")
    private Double freeQty;

    @Column(name = "discountbin_per")
    private Double discountBInPer;

    @Column(name = "total_discount_in_amt")
    private Double totalDiscountInAmt; // row_dis_amt

    @Column(name = "gross_amt")
    private Double grossAmt; // gross_amt

    @Column(name = "addition_charges_amt")
    private Double additionChargesAmt; // add_chg_amt

    @Column(name = "gross_amt1")
    private Double grossAmt1; // gross_amt1 = gross_amt - add_chg_amt

    @Column(name = "invoice_dis_amt")
    private Double invoiceDisAmt; // invoice_dis_amt

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
    @Column(name = "transaction_status")
    private Long transactionStatus; // 1: Open 2.Closed

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurchaseChallanProductSrNumber> tranxPurchaseChallanProductSrNumbers;


}
