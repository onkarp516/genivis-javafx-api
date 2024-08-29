package in.truethics.ethics.ethicsapiv10.model.tranx.credit_note;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
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
@Table(name = "tranx_credit_note_tbl")
public class TranxCreditNote {
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
    @JoinColumn(name = "tranx_sales_invoice_id")
    @JsonManagedReference
    private TranxSalesInvoice tranxSalesInvoice;

    private String type; //Dr Or Cr
    @Column (name = "ledger_type")
    private String ledgerType; //SC or IE
    @Column (name = "creditnote_sr_no")
    private Long creditnoteSrNo;
    @Column (name = "tranx_date")
    private LocalDate tranxDate;
    @Column (name = "payable_amount")
    private Double payableAmount;
    @Column (name = "paid_amount")
    private Double paidAmount;
    private Double balance;
    private String payment_type; //cash ,cheque, UPI id
    @Column (name = "bank_name")
    private String bankName;
    private String narrations;
    @Column (name = "credit_note_unique_no")
    private String creditNoteUniqueNo; //unique number of payment
    @Column (name = "bank_payment_no")
    private String bankPaymentNo;
    @Column (name = "financial_year")
    private String financialYear;
    @CreationTimestamp
    @Column (name = "created_at")
    private LocalDateTime createdAt;
    @Column (name = "created_by")
    private Long createdBy;
    private Boolean status;
}
