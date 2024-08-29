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
@Table(name = "tranx_purchase_invoice_details_units_tbl")
public class TranxPurInvoiceDetailsUnits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_invoice_id")
    @JsonManagedReference
    private TranxPurInvoice purchaseTransaction;

    @ManyToOne
    @JoinColumn(name = "purchase_invoice_details_id")
    @JsonManagedReference
    private TranxPurInvoiceDetails purInvoiceDetails;

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

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "transaction_status_id")
    private TransactionStatus transactionStatus;
    @Column(name = "unit_conversions")
    private Double unitConversions;
    private Double qty;
    private Double rate;
    @Column(name = "base_amt")
    private Double baseAmt; // rate * qty
    @Column(name = "total_amount")
    private Double totalAmount; // total_amt OR taxable_amt
    @Column(name = "discount_amount")
    private Double discountAmount; // dis_amt
    @Column(name = "discount_per")
    private Double discountPer; // dis_per
    @Column(name = "discount_amount_cal")
    private Double discountAmountCal;
    @Column(name = "discount_per_cal")
    private Double discountPerCal;
    private Double igst; // tax_per
    private Double sgst;
    private Double cgst;
    @Column(name = "total_igst")
    private Double totalIgst; // tax_amount
    @Column(name = "total_sgst")
    private Double totalSgst;
    @Column(name = "total_cgst")
    private Double totalCgst;
    @Column(name = "final_amount")
    private Double finalAmount; // net_amount
    private Boolean status;
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
    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @Column(name = "free_qty")
    private Double freeQty;
    @Column(name = "discountbin_per")
    private Double discountBInPer; // dis_per2
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

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurchaseInvoiceProductSrNumber> tranxPurInvoiceProductSrNumbers;
    @Column(name = "return_qty")
    private Double returnQty; //maintain the quantity of the invoice while return the invoice
}
