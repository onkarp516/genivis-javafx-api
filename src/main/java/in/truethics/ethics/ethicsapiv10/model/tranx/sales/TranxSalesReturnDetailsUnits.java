package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

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
@Table(name = "tranx_sales_return_details_units_tbl")
public class TranxSalesReturnDetailsUnits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_return_id")
    private Long salesReturnInvoiceId;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "level_a_id")
    private Long levelAId;

    @Column(name = "level_b_id")
    private Long levelBId;

    @Column(name = "level_c_id")
    private Long levelCId;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesReturnProductSrNo> tranxSalesReturnProductSrNos;


}
