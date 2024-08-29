package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.PackingMaster;
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
@Table(name = "tranx_purchase_invoice_details_tbl")
public class TranxPurInvoiceDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_invoice_id")
    @JsonManagedReference
    private TranxPurInvoice purchaseTransaction;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "packaging_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurchaseInvoiceProductSrNumber> productSerialNumbers;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceDetailsUnits> tranxPurInvoiceDetailsUnits;

    @Column(name = "base_amt")
    private Double base_amt;
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
    @Column(name = "reference_id")
    private String referenceId; // id of poId or PCId
    @Column(name = "reference_type")
    private String referenceType; // purchase_order or purchase_challan
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
}
