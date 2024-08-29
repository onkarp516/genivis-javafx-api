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
@Table(name = "tranx_purchase_challan_tbl")
public class TranxPurChallan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;

    @ManyToOne
    @JoinColumn(name = "sundry_creditors_id")
    @JsonManagedReference
    private LedgerMaster sundryCreditors;

    @ManyToOne
    @JoinColumn(name = "purchase_account_ledger_id")
    @JsonManagedReference
    private LedgerMaster purchaseAccountLedger;

    @ManyToOne
    @JoinColumn(name = "purchase_roundoff_id")
    @JsonManagedReference
    private LedgerMaster purchaseRoundOff;

    @ManyToOne
    @JoinColumn(name = "transaction_status_id", nullable = false)
    @JsonManagedReference
    private TransactionStatus transactionStatus;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @ManyToOne
    @JoinColumn(name = "purchase_discount_ledger_id")
    @JsonManagedReference
    private LedgerMaster purchaseDiscountLedger;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurchaseChallanProductSrNumber> tranxPurchaseChallanProductSrNumbers;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanDetails> tranxPurChallanDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanDutiesTaxes> tranxPurChallanDutiesTaxes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoice> tranxPurReturnInvoices;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanAdditionalCharges> tranxPurChallanAdditionalCharges;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanDetailsUnits> tranxPurChallanDetailsUnits;

    /*@JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxDebitNoteNewReferenceMaster> tranxDebitNoteNewReferences;*/

    @Column(name = "pur_challan_srno")
    private Long purChallanSrno;

    @Column(name = "vendor_invoice_no")
    private String vendorInvoiceNo;

    @Column(name = "order_reference")
    private String orderReference;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "invoice_date")
    private Date invoiceDate;

    @Column(name = "transport_name")
    private String transportName;

    private String reference;

    @Column(name = "round_off")
    private Double roundOff;

    @Column(name = "total_base_amount")
    private Double totalBaseAmount;  //qty*base_amount

    @Column(name = "purchase_discount_amount")
    private Double purchaseDiscountAmount;

    @Column(name = "purchase_discount_per")
    private Double purchaseDiscountPer;

    @Column(name = "total_purchase_discount_amt")
    private Double totalPurchaseDiscountAmt;

    @Column(name = "additional_charges_total")
    private Double additionalChargesTotal;

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

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;
    @UpdateTimestamp

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @ManyToOne
    @JoinColumn(name = "additional_ledger_id1")
    @JsonManagedReference
    private LedgerMaster additionLedger1;
    @ManyToOne
    @JoinColumn(name = "additional_ledger_id2")
    @JsonManagedReference
    private LedgerMaster additionLedger2;
    @ManyToOne
    @JoinColumn(name = "additional_ledger_id3")
    @JsonManagedReference
    private LedgerMaster additionLedger3;

    @Column(name = "addition_ledger_amt1")
    private Double additionLedgerAmt1;

    @Column(name = "addition_ledger_amt2")
    private Double additionLedgerAmt2;

    @Column(name = "addition_ledger_amt3")
    private Double additionLedgerAmt3;

    @Column(name = "free_qty")
    private Double freeQty; // free qty

    @Column(name = "gross_amount")
    private Double grossAmount; // gross total

    @Column(name = "total_tax")
    private Double totalTax; // tax

    @Column(name = "is_round_off")
    private Boolean isRoundOff;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnAdjustmentBills> TranxPurReturnAdjustmentBills;

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
