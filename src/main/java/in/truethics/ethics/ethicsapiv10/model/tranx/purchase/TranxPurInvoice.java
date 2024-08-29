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
@Table(name = "tranx_purchase_invoice_tbl")
public class TranxPurInvoice {
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
    @JoinColumn(name = "purchase_discount_ledger_id")
    @JsonManagedReference
    private LedgerMaster purchaseDiscountLedger;

    @ManyToOne
    @JoinColumn(name = "associates_groups_id")
    @JsonManagedReference
    private AssociateGroups associateGroups;

    @ManyToOne
    @JoinColumn(name = "purchase_roundoff_id")
    @JsonManagedReference
    private LedgerMaster purchaseRoundOff;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceDutiesTaxes> tranxPurInvoiceDutiesTaxes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceAdditionalCharges> tranxPurInvoiceAdditionalCharges;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurchaseInvoiceProductSrNumber> productSerialNumbers;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoice> tranxPurReturnInvoices;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceDetails> tranxPurReturnInvoiceDetails;


    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceProductSrNo> tranxPurReturnInvoiceProdSrNos;

    /*@JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxDebitNoteNewReferenceMaster> tranxDebitNoteNewReferences;
*/
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceDetailsUnits> tranxPurInvoiceDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnAdjustmentBills> TranxPurReturnAdjustmentBills;

    private Long srno;
    @Column(name = "vendor_invoice_no")
    private String vendorInvoiceNo;
    @Column(name = "transaction_date")
    private LocalDate transactionDate; // tranx date
    @Column(name = "invoice_date")
    private Date invoiceDate; // invoice date
    @Column(name = "transport_name")
    private String transportName;
    private String reference;
    @Column(name = "round_off")
    private Double roundOff;
    @Column(name = "total_base_amount")
    private Double totalBaseAmount;  //qty*base_amount
    @Column(name = "total_amount")
    private Double totalAmount; // bill amount
    private Double totalcgst;
    private Long totalqty;
    private Double totalsgst;
    private Double totaligst;
    @Column(name = "purchase_discount_amount")
    private Double purchaseDiscountAmount; // purchase_discount
    @Column(name = "purchase_discount_per")
    private Double purchaseDiscountPer; // purchase_discount_amt
    @Column(name = "total_purchase_discount_amt")
    private Double totalPurchaseDiscountAmt; // discount
    @Column(name = "additional_charges_total")
    private Double additionalChargesTotal;
    @Column(name = "taxable_amount")
    private Double taxableAmount; // total
    private Double tcs;//TCS Per
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
    @Column(name = "financial_year")
    private String financialYear;
    private String narration;
    private String operations; //insertion , updatation , deletion
    private Double balance;
    /* Purchase Order and Purchase Challan reference */
    @Column(name = "po_id")
    private String poId; //Purchase Order Id
    @Column(name = "pc_id")
    private String pcId; // Purchase Challan Id
    @Column(name = "gst_number")
    private String gstNumber;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
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
    @Column(name = "image_path")
    private String imagePath;//uploading image of bill
    @Column(name = "is_round_off")
    private Boolean isRoundOff;
    @Column(name = "transaction_status")
    private Long transactionStatus; // maitaining return products while selecting bills, don't allow same bill next time for return
    @Column(name = "is_selected")
    private Boolean isSelected; // check whether this debitnote is selected or not while adjusting against the purchase invoice
    @Column(name = "is_debit_note_ref")
    private Boolean isDebitNoteRef; //check for the debit note reference while creating purchase invoice
    @Column(name = "tcs_amt")
    private Double tcsAmt;
    @Column(name = "tcs_mode")
    private String tcsMode;
    @Column(name = "tds_amt")
    private Double tdsAmt;
    @Column(name = "tds_per")
    private Double tdsPer;//TDS Per
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
    @Column(name = "payment_mode")
    private String paymentMode;

}
