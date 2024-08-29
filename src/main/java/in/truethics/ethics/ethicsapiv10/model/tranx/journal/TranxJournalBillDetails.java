package in.truethics.ethics.ethicsapiv10.model.tranx.journal;

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
@Table(name = "tranx_journal_bill_details_tbl")
public class TranxJournalBillDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "branch_id")
    private Integer branchId;

    @Column(name = "outlet_id")
     private Integer outletId;

    @Column(name = "ledger_id")
    private Long ledgerMasterId;

    @Column(name = "tranx_journal_master_id")
    private Long tranxJournalMasterId;

    @Column(name = "tranx_journal_details_id")
    private Long tranxJournalDetailsId;
    @Column(name = "tranx_invoice_id")
    private Long tranxInvoiceId;
    private String type;
    @Column(name = "paid_amt")
    private Double paidAmt;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    @Column(name = "tranx_no")
    private String tranxNo;
    private Boolean status;
    @Column(name = "total_amt")
    private Double totalAmt;
    @Column(name = "remaining_amt")
    private Double remainingAmt;
    private Double amount;
    @Column(name = "balancing_type")
    private String balancingType;//cr or dr
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
}
