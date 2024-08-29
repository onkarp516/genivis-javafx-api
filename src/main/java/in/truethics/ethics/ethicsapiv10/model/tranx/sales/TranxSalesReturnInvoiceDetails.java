package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.PackingMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_sales_return_invoice_details_tbl")
public class TranxSalesReturnInvoiceDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_return_invoice_id")
    @JsonManagedReference
    private TranxSalesReturnInvoice tranxSalesReturnInvoice;

    @ManyToOne
    @JoinColumn(name = "sales_invoice_id")
    @JsonManagedReference
    private TranxSalesInvoice tranxSalesInvoice;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "packaging_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    @Column(name = "sales_invoice_no")
    private String salesInvoiceNo; //TranxSalesInvoice No
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
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Column(name = "created_by")
    private Long createdBy;
}

