package in.truethics.ethics.ethicsapiv10.model.tranx.receipt;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
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
@Table(name = "tranx_receipt_perticulars_tbl")
public class TranxReceiptPerticulars {
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
    @JoinColumn(name = "ledger_id")
    @JsonManagedReference
    private LedgerMaster ledgerMaster;

    @ManyToOne
    @JoinColumn(name = "tranx_receipt_master_id")
    @JsonManagedReference
    private TranxReceiptMaster tranxReceiptMaster;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxReceiptPerticularsDetails> tranxReceiptPerticularsDetails;

    private String type;
    @Column(name = "ledger_type")
    private String ledgerType;
    @Column(name = "ledger_name")
    private String ledgerName;
    private double dr;
    private double cr;
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "payment_tranx_no")
    private String paymentTranxNo;
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    private boolean status;
    @Column(name = "payment_amount")
    private Double paymentAmount;
    @Column(name = "tranx_invoice_id")
    private Long tranxInvoiceId;

    private String tranxtype;
    @Column(name = "tranx_no")
    private String tranxNo;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
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
    private LocalDate paymentDate;
}
