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
@Table(name = "tranx_sales_invoice_additional_charges_tbl")
public class TranxSalesInvoiceAdditionalCharges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_invoice_id")
    private TranxSalesInvoice salesTransaction;

    @ManyToOne
    @JoinColumn(name = "additional_charges_id")
    private LedgerMaster additionalCharges;

    private Double amount;
    private Double percent;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
    private String operation;

}

