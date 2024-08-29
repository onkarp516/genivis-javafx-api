package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

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
@Table(name = "tranx_sales_comp_addi_charges_tbl")
public class TranxSalesCompInvoiceAdditionalCharges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sales_transaction_id")
    private Long salesTransactionId;

    @Column(name = "additional_charges_id")
    private Long additionalChargesId;

    private Double amount;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
    private String operation;
}
