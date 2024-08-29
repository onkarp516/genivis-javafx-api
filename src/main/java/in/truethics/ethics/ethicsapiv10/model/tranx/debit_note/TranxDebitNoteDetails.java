package in.truethics.ethics.ethicsapiv10.model.tranx.debit_note;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.master.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_debit_note_details_tbl")
public class TranxDebitNoteDetails {
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

    @ManyToOne
    @JoinColumn(name = "transaction_status_id")
    @JsonManagedReference
    private TransactionStatus transactionStatus;

    @ManyToOne
    @JoinColumn(name = "tranx_debitnote_master_id")
    @JsonManagedReference
    private TranxDebitNoteNewReferenceMaster tranxDebitNoteMaster;

    @ManyToOne
    @JoinColumn(name = "ledger_id")
    @JsonManagedReference
    private LedgerMaster ledgerMaster;
    @Column(name = "type")
    private String type; //Cr or Dr
    @Column(name = "ledger_type")
    private String ledgerType;
    @Column(name = "total_amount")
    private Double totalAmount;

    private double balance;
    @Column(name = "adjusted_id")
    private Long adjustedId; //adjusted this debit note against purhcase invoice
    @Column(name = "adjusted_source")
    private String adjustedSource; // purchase invoice, payment or receipt
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private boolean status;
    @Column(name = "adjustment_status")
    private String adjustmentStatus; // immediate , future or refund
    @Column(name = "operations")
    private String operations;  // create or adjust
    @Column(name = "paid_amt")
    private Double paidAmt;
    private String source;

    private double dr;
    private double cr;
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "payment_tranx_no")
    private String paymentTranxNo;
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    @Column(name = "payable_amt")
    private Double payableAmt;
    @Column(name = "selected_amt")
    private Double selectedAmt;
    @Column(name = "remaining_amt")
    private Double remainingAmt;
    @Column(name = "is_advance")
    private Boolean isAdvance;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "payment_date")
    private Date paymentDate;

}
