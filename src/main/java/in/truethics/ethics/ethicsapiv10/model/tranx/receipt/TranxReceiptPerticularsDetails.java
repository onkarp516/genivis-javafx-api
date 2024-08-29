package in.truethics.ethics.ethicsapiv10.model.tranx.receipt;

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
@Table(name = "tranx_receipt_perticulars_details_tbl")
public class TranxReceiptPerticularsDetails {
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

    @ManyToOne
    @JoinColumn(name = "tranx_receipt_perticulars_id")
    @JsonManagedReference
    private TranxReceiptPerticulars tranxReceiptPerticulars;

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
