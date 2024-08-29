package in.truethics.ethics.ethicsapiv10.model.tranx.journal;

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

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_journal_details_tbl")
public class TranxJournalDetails {
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
    @JoinColumn(name = "tranx_journal_master_id")
    @JsonManagedReference
    private TranxJournalMaster tranxJournalMaster;

    private String type; //Cr or Dr
    @Column(name = "ledger_type")
    private String ledgerType;
    @Column(name = "paid_amount")
    private Double paidAmount;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
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
    private Double cr;
    private  Double dr;
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "payment_tranx_no")
    private String paymentTranxNo;
    @Column(name = "transaction_date")
    private LocalDate transactionDate;

}
