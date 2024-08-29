package in.truethics.ethics.ethicsapiv10.model.tranx.payment;

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
@Table(name = "tranx_payment_perticulars_tbl")
public class TranxPaymentPerticulars {
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
    @JoinColumn(name = "tranx_payment_master_id")
    @JsonManagedReference
    private TranxPaymentMaster tranxPaymentMaster;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPaymentPerticularsDetails> tranxPaymentPerticularsDetails;

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

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;
}
