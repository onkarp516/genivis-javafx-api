package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ledger_opening_balance_tbl")
public class LedgerOpeningBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String invoice_no;
    private LocalDate invoice_date;
    private Long due_days;
    private Double bill_amt;
    private Double invoice_paid_amt;
    private Double invoice_bal_amt;
    @Column(name = "invoice_bal_type")
    private String invoiceBalType;// CR or DR
    @Column(name = "ledger_id")
    private Long ledgerId;
    @Column(name = "balancing_type")
    private String balancingType; //Ledger Balancing Type
    private Boolean status;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "updated_by")
    private Long updatedBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDate createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDate updatedAt;
}
