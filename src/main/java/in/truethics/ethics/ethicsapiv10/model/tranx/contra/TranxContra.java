package in.truethics.ethics.ethicsapiv10.model.tranx.contra;

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
@Table(name = "tranx_contra_tbl")
public class TranxContra {
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

    @Column(name = "type")
    private String type; //Cr or Dr
    @Column(name = "ledger_type")
    private String ledgerType;
    @Column(name = "contra_sr_no")
    private Long contraSrNo;
    @Column(name = "tranx_date")
    private LocalDate tranxDate;
    @Column(name = "paid_amount")
    private Double paidAmount;
    @Column(name = "balance")
    private Double balance;
    @Column(name = "payment_type")
    private String payment_type; //cash ,cheque, UPI id
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "narrations")
    private String narrations;
    @Column(name = "contra_unique_no")
    private String contraUniqueNo; //unique number of payment
    @Column(name = "bank_payment_no")
    private String bankPaymentNo;
    @Column(name = "financial_year")
    private String financialYear;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "status")
    private Boolean status;
}

