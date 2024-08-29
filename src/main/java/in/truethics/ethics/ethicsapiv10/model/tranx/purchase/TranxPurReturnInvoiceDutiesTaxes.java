package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
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
@Table(name = "tranx_pur_return_invoice_duties_taxes_tbl")
public class TranxPurReturnInvoiceDutiesTaxes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pur_return_invoice_id", nullable = false)
    @JsonManagedReference
    private TranxPurReturnInvoice purReturnInvoice;

    @ManyToOne
    @JoinColumn(name = "sundry_creditors_id", nullable = false)
    @JsonManagedReference
    private LedgerMaster sundryCreditors;

    @ManyToOne
    @JoinColumn(name = "duties_taxes_ledger_id", nullable = false)
    @JsonManagedReference
    private LedgerMaster dutiesTaxes;

    private Boolean intra; //intra is for within state i.e cgst and sgst and inter is for outside state ie. igst
    private Double amount;
    private Boolean status;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
