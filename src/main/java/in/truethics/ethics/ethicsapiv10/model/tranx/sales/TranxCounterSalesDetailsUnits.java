package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

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

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_counter_sales_details_units_tbl")
public class TranxCounterSalesDetailsUnits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "counter_sales_id")
    @JsonManagedReference
    private TranxCounterSales counterSales;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    @JsonManagedReference
    private Units units;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    @JsonManagedReference
    private ProductBatchNo productBatchNo;
    @Column(name = "unit_conversions")
    private Double unitConversions;
    private Double qty;
    private Double rate;
    @Column(name = "base_amt")
    private Double baseAmt;
    @Column(name = "net_amount")
    private Double netAmount;
    @Column(name = "discount_amount")
    private Double discountAmount; // dis_amt
    @Column(name = "discount_per")
    private Double discountPer; // dis_per
    @Column(name = "row_discountamt")
    private Double rowDiscountamt;
    @Column(name = "discountbin_per")
    private Double discountBInPer; // dis_per2
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    private Boolean status;
    @Column(name = "free_qty")
    private Double freeQty;
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
    private Long transactionStatus;
    private Double igst; // tax_per
    private Double sgst;
    private Double cgst;
    @Column(name = "total_igst")
    private Double totalIgst; // tax_amount
    @Column(name = "total_sgst")
    private Double totalSgst;
    @Column(name = "total_cgst")
    private Double totalCgst;
}
