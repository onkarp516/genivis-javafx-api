package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.PackingMaster;
import in.truethics.ethics.ethicsapiv10.model.master.TransactionTypeMaster;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Entity
@Table(name="tranx_purchase_challan_details_tbl")
public class TranxPurChallanDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tranx_pur_challan_id")
    @JsonManagedReference
    private TranxPurChallan tranxPurChallan;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "transaction_type_id")
    @JsonManagedReference
    private TransactionTypeMaster transactionType;

    @ManyToOne
    @JoinColumn(name = "packaging_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanDetailsUnits> tranxPurChallanDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurchaseChallanProductSrNumber> tranxPurchaseChallanProductSrNumbers;

    //private Double qty;
    private Double base_amt;
    //private String referenceType;

    @Column(name = "order_reference")
    private String orderReference;

    @Column(name = "is_purchase_order")
    private Boolean isPurchaseOrder;

    @Column(name = "purchase_order_id")
    private String PurchaseOrderId;
    //private Double rate;

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

    @Column(name = "qty_high")
    private Double qtyHigh;

    @Column(name = "rate_high")
    private Double rateHigh;

    @Column(name = "qty_medium")
    private Double qtyMedium;

    @Column(name = "rate_medium")
    private Double rateMedium;

    @Column(name = "qty_low")
    private Double qtyLow;

    @Column(name = "rate_low")
    private Double rateLow;

    @Column(name = "base_amt_high")
    private Double baseAmtHigh;

    @Column(name = "base_amt_low")
    private Double baseAmtLow;

    @Column(name = "base_amt_medium")
    private Double baseAmtMedium;

    private Boolean status;
    private String operations;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
