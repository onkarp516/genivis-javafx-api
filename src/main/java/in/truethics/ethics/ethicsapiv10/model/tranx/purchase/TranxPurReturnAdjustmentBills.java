package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tranx_pur_return_adj_bills_tbl")
public class TranxPurReturnAdjustmentBills {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "tranx_pur_invoice_id")
    private TranxPurInvoice tranxPurInvoice;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "tranx_pur_challan_id")
    private TranxPurChallan tranxPurChallan;

    @Column(name = "paid_amt")
    private Double paidAmt;
    @Column(name = "remaining_amt")
    private Double remainingAmt;
    @Column(name = "total_amt")
    private Double totalAmt;
    private String source;
    private Boolean status;
    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "tranx_pur_return_id")
    private Long tranxPurReturnId; //refernce Id

}
