package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

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
@Table(name = "tranx_sales_comp_invoice_details_tbl")
public class TranxSalesCompInvoiceDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sales_invoice_id")
    private Long salesInvoiceId;
    @Column(name = "product_id")
    private Long productId;
    @Column(name = "packing_master_id")
    private Long packingMasterId;
    @Column(name = "base_amt")
    private Double base_amt;
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
    private String referenceId;  // Sq,So,and Sc id
    @Column(name = "reference_type")
    private String referenceType;
    private Double returnqtyh;
    private Double returnqtyl;
    private Double returnqtym;
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
