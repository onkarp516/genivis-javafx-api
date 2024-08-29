package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

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

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_sales_order_details_tbl")
public class
TranxSalesOrderDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_order_invoice_id")
    @JsonManagedReference
    private TranxSalesOrder salesTransaction;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "packaging_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    //private Double qty;
    private Double base_amt;
    //private Double rate;
    @Column (name ="total_amount")
    private Double totalAmount;
    @Column (name ="discount_amount")
    private Double discountAmount;
    @Column (name ="reference_id")
    private String referenceId;
    @Column (name ="reference_type")
    private String referenceType;
    @Column (name ="discount_per")
    private Double discountPer;
    @Column (name ="discount_amount_cal")
    private Double discountAmountCal;
    @Column (name ="discount_per_cal")
    private Double discountPerCal;
    private Double igst;
    private Double sgst;
    private Double cgst;
    @Column (name ="total_igst")
    private Double totalIgst;
    @Column (name ="total_sgst")
    private Double totalSgst;
    @Column (name ="total_cgst")
    private Double totalCgst;
    @Column (name ="final_amount")
    private Double finalAmount;
    @Column (name ="qty_high")
    private Double qtyHigh;
    @Column (name ="rate_high")
    private Double rateHigh;
    @Column (name ="qty_medium")
    private Double qtyMedium;
    @Column (name ="rate_medium")
    private Double rateMedium;
    @Column (name ="qty_low")
    private Double qtyLow;
    @Column (name ="rate_low")
    private Double rateLow;
    @Column (name ="base_amt_high")
    private Double baseAmtHigh;
    @Column (name ="base_amt_low")
    private Double baseAmtLow;
    @Column (name ="base_amt_medium")
    private Double baseAmtMedium;
    private Boolean status;
    private String operations;
    @Column (name ="created_Date")
    @CreationTimestamp
    private LocalDateTime createdDate;
    @Column (name ="created_by")
    private Long createdBy;
    @Column (name ="updated_date")
    @UpdateTimestamp
    private LocalDateTime updatedDate;
    @Column (name ="updated_by")
    private Long updatedBy;
}
