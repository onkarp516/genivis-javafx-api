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
@Table(name = "tranx_sales_challan_details_tbl")
public class TranxSalesChallanDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_challan_id")
    @JsonManagedReference
    private TranxSalesChallan salesTransaction;

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
    private List<TranxSalesChallanProductSerialNumber> productSerialNumbers;
    //private Double qty;
    private Double base_amt;
    //private Double rate;
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
    private String referenceId; // id of soId or sqId
    private String referenceType; //
    // sales_order or sales_quotation
    @CreationTimestamp
    private LocalDateTime createdDate;
    private Long createdBy;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
    private Long updatedBy;
}
