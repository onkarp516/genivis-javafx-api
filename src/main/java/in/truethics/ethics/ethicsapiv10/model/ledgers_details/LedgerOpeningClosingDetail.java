package in.truethics.ethics.ethicsapiv10.model.ledgers_details;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Table(name = "ledger_opening_closing_detail_tbl")
public class LedgerOpeningClosingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "opening_amount")
    private Double openingAmount;
    @Column(name = "tranx_action")
    private String tranxAction; //CR or DR
    @Column(name = "closing_amount")
    private Double closingAmount;
    @Column(name = "tranx_date")
    private Date tranxDate;
    @Column(name = "tranx_id")
    private Long tranxId;
    @Column(name = "tranx_type_id")
    private Long tranxTypeId;//PUR or SALE or PRTN or SLRTN
    private Double amount;
    @Column(name = "fiscal_year_id")
    private Long fiscalYearId;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;

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

    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed
}
