package in.truethics.ethics.ethicsapiv10.model.master;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "areahead_commission_tbl")
public class AreaheadCommission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "areahead_id")
    private Long areaheadId;
    @Column(name = "areahead_role")
    private String areaheadRole;
    @Column(name = "sales_invoice_number")
    private String salesInvoiceNumber;
    @Column(name = "sales_invoice_amount")
    private Double salesInvoiceAmount;
    @Column(name = "commission_percentage")
    private Double commissionPercentage;
    @Column(name = "commission_amount")
    private Double commissionAmount;
    @Column(name = "invoice_base_amount")
    private Double invoiceBaseAmount;
    @Column(name = "invoice_date")
    private LocalDate invoiceDate;
    @Column(name = "franchise_code")
    private String franchiseCode;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "updated_by")
    private Long updatedBy;
    private Boolean status;

}