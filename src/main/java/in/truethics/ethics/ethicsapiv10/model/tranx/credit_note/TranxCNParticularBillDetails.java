package in.truethics.ethics.ethicsapiv10.model.tranx.credit_note;

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
@Table(name = "tranx_cn_bill_details_tbl")
public class TranxCNParticularBillDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "outlet_id")
    private Long outletId;

    @Column(name = "ledger_id")
    private Long ledgerMasterId;

    @Column(name = "tranx_creditnote_master_id")
    private Long tranxCreditNoteMasterId;

    @Column(name = "tranx_credit_note_details")
    private Long tranxCreditNoteDetailsId;
    @Column(name = "tranx_invoice_id")
    private Long tranxInvoiceId;
    @Column(name = "type")
    private String type;
    @Column(name = "paid_amt")
    private Double paidAmt;
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    @Column(name = "tranx_no")
    private String tranxNo;
    @Column(name = "status")
    private Boolean status;
    @Column(name = "total_amt")
    private Double totalAmt;
    @Column(name = "remaining_amt")
    private Double remainingAmt;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "balancing_type")
    private String balancingType;//cr or dr
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
}
