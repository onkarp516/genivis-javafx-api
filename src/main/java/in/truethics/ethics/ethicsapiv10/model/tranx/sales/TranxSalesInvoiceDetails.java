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
@Table(name = "tranx_sales_invoice_details_tbl")
public class TranxSalesInvoiceDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_invoice_id")
    @JsonManagedReference
    private TranxSalesInvoice salesInvoice;

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
    private List<TranxSalesInvoiceProductSrNumber> tranxSalesInvoicePrSrNo;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceProductSrNumber> tranxSalesInvoiceProductSrNumbers;

    //private Double qty;
    private Double base_amt;
    private Double totalAmount;
    private Double discountAmount;
    private Double discountPer;
    private Double discountAmountCal;
    private Double discountPerCal;
    private Double igst;
    private Double sgst;
    private Double cgst;
    private Double totalIgst;
    private Double totalSgst;
    private Double totalCgst;
    private Double finalAmount;
    private Double qtyHigh;
    private Double rateHigh;
    private Double qtyMedium;
    private Double rateMedium;
    private Double qtyLow;
    private Double rateLow;
    private Double baseAmtHigh;
    private Double baseAmtLow;
    private Double baseAmtMedium;
    private Boolean status;
    private String operations;
    private String referenceId;  // Sq,So,and Sc id
    private String referenceType;
    private Double returnqtyh;
    private Double returnqtyl;
    private Double returnqtym;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private Long createdBy;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private Long updatedBy;

}
