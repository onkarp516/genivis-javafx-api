package in.truethics.ethics.ethicsapiv10.model.tranx.gstinput;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_gst_input_tax_tbl")
public class GstInputDutiesTaxes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gst_input_id", nullable = false)
    @JsonManagedReference
    private GstInputMaster gstInputMaster;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    @JsonManagedReference
    private LedgerMaster supplierLedger;

    @ManyToOne
    @JoinColumn(name = "duties_taxes_ledger_id", nullable = false)
    @JsonManagedReference
    private LedgerMaster dutiesTaxes;

    @ManyToOne
    @JoinColumn(name = "posting_ledger_id")
    @JsonManagedReference
    private LedgerMaster postingLedger;

    private Double amount;
    private Boolean status;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
