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
@Table(name = "tranx_pur_return_invoice_additional_charges_tbl")
public class TranxPurReturnInvoiceAddCharges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pur_return_invoice_id")
    @JsonManagedReference
    private TranxPurReturnInvoice purReturnInvoice;

    @ManyToOne
    @JoinColumn(name = "additional_charges_id")
    @JsonManagedReference
    private LedgerMaster additionalCharges;


    private Double amount;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
    private String operation;
    private Double percent;
}
