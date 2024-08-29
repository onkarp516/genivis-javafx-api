package in.truethics.ethics.ethicsapiv10.model.tranx.debit_note;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurReturnInvoice;
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
@Table(name = "tranx_debit_note_new_reference_tbl")
public class TranxDebitNoteNewReferenceMaster {
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
    @JoinColumn(name = "sundry_creditor_id")
    @JsonManagedReference
    private LedgerMaster sundryCreditor;

  /*  @ManyToOne
    @JoinColumn(name = "tranx_pur_invoice_id")
    @JsonManagedReference
    private TranxPurInvoice tranxPurInvoice;

    @ManyToOne
    @JoinColumn(name = "tranx_pur_challan_id")
    @JsonManagedReference
    private TranxPurChallan tranxPurChallan;*/

    @ManyToOne
    @JoinColumn(name = "tranx_pur_return_invoice_id")
    @JsonManagedReference
    private TranxPurReturnInvoice tranxPurReturnInvoice;

    @ManyToOne
    @JoinColumn(name = "transaction_status_id")
    @JsonManagedReference
    private TransactionStatus transactionStatus;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxDebitNoteDetails> tranxDebitNoteDetails;

    private Long srno;
    @Column(name = "debitnote_new_reference_no")
    private String debitnoteNewReferenceNo; //auto generate
    @Column(name = "round_off")
    private Double roundOff;
    @Column(name = "total_base_amount")
    private Double totalBaseAmount;  //qty*base_amount
    @Column(name = "total_amount")
    private Double totalAmount;
    @Column(name = "taxable_amount")
    private Double taxableAmount;
    private Double totalgst;
    @Column(name = "purchase_discount_amount")
    private Double purchaseDiscountAmount;
    @Column(name = "purchase_discount_per")
    private Double purchaseDiscountPer;
    @Column(name = "total_purchase_discount_amt")
    private Double totalPurchaseDiscountAmt;
    @Column(name = "additional_charges_total")
    private Double additionalChargesTotal;
    @Column(name = "financial_year")
    private String financialYear;
    private String source;
    @Column(name = "transcation_date")
    private Date transcationDate;
    private String narrations;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private boolean status;
    @Column(name = "adjustment_status")
    private String adjustmentStatus; // immediate,future or refund
    private Double balance;
    @Column(name = "purchase_invoice_id")
    private Long purchaseInvoiceId;
    @Column(name = "purchase_challan_id")
    private Long purchaseChallanId;
    @Column(name = "payment_id")
    private Long paymentId; // maitaining payment id incase of advance payment is given(new reference is created)
    @Column(name = "adjusted_id")
    private Long adjustedId;//maintain the adjusted Id,
    // Id is stored here while adjusting the debitnote against the purchase invocie bill
    @Column(name = "is_selected")
    private Boolean isSelected; // check whether this debitnote is selected or not while adjusting against the purchase invoice
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed



}
