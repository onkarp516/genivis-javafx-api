package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
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
@Table(name = "tranx_sales_challan_additional_charges_tbl")
public class TranxSalesChallanAdditionalCharges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_challan_id")
    @JsonManagedReference
    private TranxSalesChallan salesTransaction;

    @ManyToOne
    @JoinColumn(name = "additional_charges_id")
    @JsonManagedReference
    private LedgerMaster additionalCharges;

    private Boolean status;
    private Double amount;
    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "updated_by")
    private Long updatedBy;
    @Column(name = "updated_date")
    @UpdateTimestamp
    private LocalDateTime updatedDate;

    private Double percent;
}
