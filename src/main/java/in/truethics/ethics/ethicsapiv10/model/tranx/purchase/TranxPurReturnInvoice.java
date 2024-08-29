package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_pur_return_invoice_tbl")
public class TranxPurReturnInvoice {
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
    @JoinColumn(name = "tranx_pur_invoice_id")
    @JsonManagedReference
    private TranxPurInvoice tranxPurInvoice;

    @ManyToOne
    @JoinColumn(name = "tranx_pur_challan_id")
    @JsonManagedReference
    private TranxPurChallan tranxPurChallan;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceDetails> tranxPurReturnInvoiceDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceAddCharges> tranxPurReturnInvoiceAddCharges;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceDutiesTaxes> tranxPurReturnInvoiceDutiesTaxes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceProductSrNo> tranxPurReturnInvoiceProdSrNos;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnDetailsUnits> tranxPurReturnDetailsUnits;



  /*  @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxDebitNoteNewReferenceMaster> tranxDebitNoteNewReferences;*/
  @Column(name = "pur_return_srno")
    private Long purReturnSrno;
    @Column(name = "pur_rtn_no")
    private String purRtnNo; //Auto Generated
    @Column(name = "transaction_date")
    private Date transactionDate;
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
    @Column(name = "purchase_discount_amount")
    private Double purchaseDiscountAmount;
    @Column(name = "purchase_discount_per")
    private Double purchaseDiscountPer;
    @Column(name = "total_purchase_discount_amt")
    private Double totalPurchaseDiscountAmt;
    @Column(name = "additional_charges_total")
    private Double additionalChargesTotal;
    @Column(name = "taxable_amount")
    private Double taxableAmount;
    private Double tcs;
    private Boolean status;
    @Column(name = "financial_year")
    private String financialYear;
    private String narration;
    private String operations; //insertion , updatation , deletion
    @Column(name = "gst_number")
    private String gstNumber;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "pur_return_date")
    private Date purReturnDate;

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
    @Column(name = "payment_mode")
    private String paymentMode;
    @Column(name = "payment_amount")
    private Double paymentAmount;
    @Column(name = "payment_transaction_num")
    private String paymentTransactionNum;
    @Column(name = "payment_date")
    private LocalDate paymentDate;
    @Column(name = "is_round_off")
    private Boolean isRoundOff;
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

}
