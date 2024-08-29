package in.truethics.ethics.ethicsapiv10.model.ledgers_details;

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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ledger_transaction_details_tbl")
public class LedgerTransactionDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   /* @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<PaymentTransactionDetails> paymentTransactionDetails;*/

    @ManyToOne
    @JoinColumn(name = "foundation_id")
    @JsonManagedReference
    private Foundations foundations;

    @ManyToOne
    @JoinColumn(name = "principle_id")
    @JsonManagedReference
    private Principles principles;

    @ManyToOne
    @JoinColumn(name = "principle_groups_id")
    @JsonManagedReference
    private PrincipleGroups principleGroups;

    @ManyToOne
    @JoinColumn(name = "ledger_master_id")
    @JsonManagedReference
    private LedgerMaster ledgerMaster;

    @ManyToOne
    @JoinColumn(name = "transaction_type_id")
    @JsonManagedReference
    private TransactionTypeMaster transactionType;

    @ManyToOne
    @JoinColumn(name = "balancing_method_id")
    @JsonManagedReference
    private BalancingMethod balanceMethod;

    @ManyToOne
    @JoinColumn(name = "associate_groups_id")
    @JsonManagedReference
    private AssociateGroups associateGroups;

   /* @ManyToOne(fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE})
    @JsonIgnoreProperties(value = {"ledger_transaction_details", "hibernateLazyInitializer"})
    @JoinColumn(name = "payment_status_id")
    private PaymentStatus paymentStatus;*/

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;
    @Column(name = "payment_status")
    private String paymentStatus;
    private Double debit;
    private Double credit;
    @Column(name = "opening_bal")
    private Double openingBal;
    @Column(name = "closing_bal")
    private Double closingBal;
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    @Column(name = "payment_date")
    private LocalDate paymentDate;
    @Column(name = "transaction_id")
    private Long transactionId; // Transaction Id (purchase,sale)
    @Column(name = "tranx_type")
    private String tranxType;
    @Column(name = "financial_year")
    private String financialYear;
    @Column(name = "under_prefix")
    private String underPrefix;
    @Column(name = "invoice_no")
    private String invoiceNo;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "updated_by")
    private Long updatedBy;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private Boolean status;
}
