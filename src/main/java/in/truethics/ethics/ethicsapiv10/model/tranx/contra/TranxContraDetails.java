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
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_contra_details_tbl")
public class TranxContraDetails {

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
    @JoinColumn(name = "tranx_contra_master_id")
    @JsonManagedReference
    private TranxContraMaster tranxContraMaster;
    @Column(name = "type")
    private String type; //Cr or Dr
    @Column(name = "ledger_type")
    private String ledgerType;
    @Column(name = "ledger_name")
    private String ledgerName;
    @Column(name = "paid_amount")
    private Double paidAmount;
    private String payment_type; //cash ,cheque, UPI id
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "bank_payment_no")
    private String bankPaymentNo;
    @Column(name = "payment_date")
    private String paymentDate;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "status")
    private Boolean status;
}
