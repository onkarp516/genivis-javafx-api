package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_purchase_order_tbl")
public class TranxPurOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "outlet_id")
    private Outlet outlet;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "sundry_creditors_id")
    private LedgerMaster sundryCreditors;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "purchase_account_ledger_id")
    private LedgerMaster purchaseAccountLedger;



    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "purchase_roundoff_id")
    private LedgerMaster purchaseRoundOff;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "transaction_status_id", nullable = false)
    private TransactionStatus transactionStatus;

    @JsonBackReference
    @OneToMany
    private List<TranxPurOrderDetails> purchaseOrderDetails;

    @JsonBackReference
    @OneToMany
    private List<TranxPurOrderDutiesTaxes> tranxPurOrderDutiesTaxes;

    @JsonBackReference
    @OneToMany
    private List<TranxPurOrderDetailsUnits> tranxPurOrderDetailsUnits;

    @Column(name = "pur_ord_srno")
    private Long purOrdSrno;
    @Column(name = "vendor_invoice_no")
    private String vendorInvoiceNo;// purchase Order number
    @Column(name = "order_reference")
    private String orderReference;
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    @Column(name = "invoice_date")
    private Date invoiceDate;//purchase order date
    @Column(name = "transport_name")
    private String transportName;
    private String reference;
    @Column(name = "round_off")
    private Double roundOff;
    @Column(name = "total_base_amount")
    private Double totalBaseAmount;  //qty*base_amount
    @Column(name = "total_amount")
    private Double totalAmount;
    private Double totalcgst;
    private Long totalqty;
    private Double totalsgst;
    private Double totaligst;
    @Column(name = "taxable_amount")
    private Double taxableAmount;
    private Double tcs;
    private Boolean status;
    @Column(name = "financial_year")
    private String financialYear;
    private String narration;
    private String operations;
    @Column(name = "is_challan_converted")
    private Boolean isChallanConverted;
    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @ManyToOne
    @JoinColumn(name = "ledger_id")
    @JsonManagedReference
    private LedgerMaster ledgerMaster;
    @Column(name = "ledger_amt")
    private Double ledgerAmt;
    @Column(name = "free_qty")
    private Double freeQty;
    @Column(name = "purchase_discount_amount")
    private Double purchaseDiscountAmount; // purchase_discount
    @Column(name = "purchase_discount_per")
    private Double purchaseDiscountPer; // purchase_discount_amt
    @Column(name = "total_purchase_discount_amt")
    private Double totalPurchaseDiscountAmt; // discount
    @Column(name = "gross_amount")
    private Double grossAmount; // gross total
    @Column(name = "total_tax")
    private Double totalTax; // tax
    @Column(name = "gst_number")
    private String gstNumber;//gstNo;
    @Column(name = "transaction_tracking_no")
    private String transactionTrackingNo;//transactionTrackingNo;
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed

    @Column(name =  "order_status")
    private String orderStatus;
    @Column(name = "district_id")
    private Long districtId;
    @Column(name = "zone_id")
    private Long zoneId;
    @Column(name = "state_id")
    private Long stateId;
    @Column(name = "regional_id")
    private Long regionalId;

}
