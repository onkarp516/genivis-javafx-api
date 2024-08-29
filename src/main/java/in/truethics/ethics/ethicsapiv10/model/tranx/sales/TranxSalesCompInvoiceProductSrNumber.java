package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.*;
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
@Table(name = "tranx_sales_comp_pr_sr_no_tbl")
public class TranxSalesCompInvoiceProductSrNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "branch_id")
    private Long branchId;
    @Column(name = "outlet_id")
    private Long outletId;
    @Column(name = "product_id")
    private Long productId;
    @Column(name = "transaction_type_master_id")
    private Long transactionTypeMasterId;
    @Column(name = "serial_no")
    private String serialNo;
    @Column(name = "sale_created_at")
    private LocalDateTime saleCreatedAt;
    @Column(name = "transaction_status")
    private String transactionStatus; //purchase or sales or counter sales
    private String operations;
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
    private Boolean status;
    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @Column(name = "levelaid")
    private Long levelAId;
    @Column(name = "levelbid")
    private Long levelBId;
    @Column(name = "levelcid")
    private Long levelCId;
    @Column(name = "tranx_sales_invoice_details_units_id")
    private Long tranxSalesCompInvoiceDetailsUnitsId;
    @Column(name = "units_id")
    private Long unitsId;
}
