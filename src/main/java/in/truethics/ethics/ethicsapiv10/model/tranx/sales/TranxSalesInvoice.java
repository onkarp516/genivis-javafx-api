package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNote;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
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
@Table(name = "tranx_sales_invoice_tbl")
public class TranxSalesInvoice {
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
    @JoinColumn(name = "sundry_debtors_id")
    @JsonManagedReference
    private LedgerMaster sundryDebtors;

    @ManyToOne
    @JoinColumn(name = "sales_account_ledger_id")
    @JsonManagedReference
    private LedgerMaster salesAccountLedger;

    @ManyToOne
    @JoinColumn(name = "sales_discount_ledger_id")
    @JsonManagedReference
    private LedgerMaster salesDiscountLedger;

    @ManyToOne
    @JoinColumn(name = "sales_roundoff_id")
    @JsonManagedReference
    private LedgerMaster salesRoundOff;

    @ManyToOne
    @JoinColumn(name = "associates_groups_id")
    @JsonManagedReference
    private AssociateGroups associateGroups;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceDetails> tranxSalesInvoiceDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceDutiesTaxes> tranxSalesInvoiceDutiesTaxes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceProductSrNumber> tranxSalesInvoicePrSrNo;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceAdditionalCharges> tranxSalesInvoiceAdditionalCharges;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceDetailsUnits> tranxSalesInvoiceDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceProductSrNumber> tranxSalesInvoiceProductSrNumbers;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxCreditNote> tranxCreditNotes;


    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesReturnInvoice> tranxSalesReturnInvoices;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesReturnInvoiceDetails> tranxSalesReturnInvoiceDetails;


    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxReceiptMaster> tranxReceiptMasters;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesPaymentType> tranxSalesPaymentTypes;

    @Column(name = "sales_serial_number")
    private Long salesSerialNumber;
    @Column(name = "sales_invoice_no")
    private String salesInvoiceNo;
    @Column(name = "bill_date")
    private Date billDate;
//    private LocalDate invoice_date;
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
    @Column(name = "sales_discount_amount")
    private Double salesDiscountAmount; // purchase_discount
    @Column(name = "sales_discount_per")
    private Double salesDiscountPer; // purchase_discount_amt
    @Column(name = "total_sales_discount_amt")
    private Double totalSalesDiscountAmt; // discount
    @Column(name = "additional_charges_total")
    private Double additionalChargesTotal;
    @Column(name = "taxable_amount")
    private Double taxableAmount; // total
    private Double tcs;
    @Column(name = "is_counter_sale")
    private Boolean isCounterSale;
    @Column(name = "counter_sale_id")
    private String counterSaleId;
    private Boolean status;
    @Column(name = "financial_year")
    private String financialYear;
    private String narration;
    private String operations;
    @Column(name = "reference_sq_id")
    private String referenceSqId;//Reference of Sales Quatations Ids
    @Column(name = "reference_so_id")
    private String referenceSoId;//Reference of Sales Order Ids
    @Column(name = "reference_sc_id")
    private String referenceScId;//Reference of Sales Challan Ids
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "payment_mode")
    private String paymentMode;
    @Column(name = "payment_amount")
    private Double paymentAmount;
    private Double cash;
    private Double digital;
    @Column(name = "card_payment")
    private Double cardPayment;
    @Column(name = "advanced_amount")
    private Double advancedAmount;
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
    private Double balance;
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
    @ManyToOne
    @JoinColumn(name = "saleman_id")
    @JsonManagedReference
    private Users salesmanId;
    private String barcode;
    @Column(name = "salesman_user")
    private Long salesmanUser;
    @Column(name = "transaction_status")
    private Long transactionStatus; // maitaining return products while selecting bills, dont allow same bill next time for return
    @Column(name = "is_selected")
    private Boolean isSelected; // check whether this debitnote is selected or not while adjusting against the purchase invoice
    @Column(name = "is_credit_note_ref")
    private Boolean isCreditNoteRef; //check for the debit note reference while creating purchase invoice
    @Column(name = "is_round_off")
    private Boolean isRoundOff; // check if round of is applicable or not
    @Column(name = "tcs_amt")
    private Double tcsAmt;
    @Column(name = "tcs_mode")
    private String tcsMode;
    @Column(name = "tds_amt")
    private Double tdsAmt;
    @Column(name = "tds_per")
    private Double tdsPer;//TDS Per
    @Column(name = "image_path")
    private String imagePath;//uploading image of bill
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

    @Column(name = "transaction_tracking_no")
    private String transactionTrackingNo;//transactionTrackingNo;
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed



}
