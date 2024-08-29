package in.truethics.ethics.ethicsapiv10.model.inventory;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stock_opening_closing_snap_tbl")
public class InventorySummaryTransactionDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "opening_stock")
    private Double openingStock;
    @Column(name = "tranx_action")
    private String tranxAction; //IN or OUT
    @Column(name = "closing_stock")
    private Double closingStock;
    @Column(name = "tranx_date")
    private Date tranxDate;
    @Column(name = "tranx_id")
    private Long tranxId;
    @Column(name = "tranx_type_id")
    private Long tranxTypeId;//PUR or SALE or PRTN or SLRTN
    private Double qty;
    @Column(name = "financial_year")
    private Long financialYear;
    private Boolean status;
    private Double valuation; // valuation (qnt*purchase_rate)
    @Column(name = "avg_valuation")
    private Double avgValuation;  // valuation/closing_stock
    @Column(name = "pur_price")
    private Double purPrice;
    @Column(name = "sales_price")
    private Double salesPrice;
    @Column(name = "batch_id")
    private Long batchId;
    @Column(name = "package_id")
    private Long packageId;
    @Column(name = "unit_id")
    private Long unitId;
    @Column(name = "serial_num")
    private String serialNum;
    @Column(name = "levelaid")
    private Long levelAId;
    @Column(name = "levelbid")
    private Long levelBId;
    @Column(name = "levelcid")
    private Long levelCId;
    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;
    @Column(name = "tranx_code")
    private String tranxCode; //Tranx unique code

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(name = "created_by")
    private Long createBy;
    @Column(name = "updated_by")
    private Long updatedBy;

}
