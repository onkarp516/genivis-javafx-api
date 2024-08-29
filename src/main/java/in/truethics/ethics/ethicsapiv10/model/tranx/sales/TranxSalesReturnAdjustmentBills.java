package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurChallan;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.TranxPurInvoice;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tranx_sales_return_adj_bills_tbl")
public class TranxSalesReturnAdjustmentBills {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "tranx_sales_invoice_id")
    private TranxSalesInvoice tranxSalesInvoice;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "tranx_sales_challan_id")
    private TranxSalesChallan tranxSalesChallan;

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
    @Column(name = "tranx_sales_return_id")
    private Long tranxSalesReturnId; //refernce Id

}
