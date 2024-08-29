package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="tranx_purchase_order_duties_taxes_tbl")
public class TranxPurOrderDutiesTaxes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "tranx_pur_order_id", nullable = false)
    private TranxPurOrder tranxPurOrder;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "sundry_creditors_id", nullable = false)
    private LedgerMaster sundryCreditors;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "duties_taxes_ledger_id", nullable = false)
    private LedgerMaster dutiesTaxes;

    private Boolean intra; //intra is for within state i.e cgst and sgst and inter is for outside state ie. igst
    private Double amount;
    private Boolean status;
    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;
}
