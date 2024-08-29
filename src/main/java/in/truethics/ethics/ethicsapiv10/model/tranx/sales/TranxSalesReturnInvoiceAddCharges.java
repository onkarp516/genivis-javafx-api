package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

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
@Table(name = "tranx_sales_return_addi_charges_tbl")
public class TranxSalesReturnInvoiceAddCharges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_return_invoice_id")
    @JsonManagedReference
    private TranxSalesReturnInvoice tranxSalesReturnInvoice;


    @ManyToOne
    @JoinColumn(name = "additional_charges_id")
    @JsonManagedReference
    private LedgerMaster additionalCharges;

    private Double amount;
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
    private String operation;
    private Double percent;

}
