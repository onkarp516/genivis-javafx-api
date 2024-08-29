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
@Table(name = "stock_opening_closing_summary_tbl")
public class InventorySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "closing_stock")
    private Double closingStock; // current Stocks : total available stocks as of Date
    @Column(name = "opening_stock")
    private Double openingStock;
    private Double valuation; // Valuation (qnt*purchase_rate)
    @Column(name = "avg_valuation")
    private Double avgValuation;  // valuation/closing_stock
    @Column(name = "pur_price")
    private Double purPrice;
    @Column(name = "sales_price")
    private Double salesPrice;
    @Column(name = "batch_id")
    private Long batchId;
    private Double qty;
    @Column(name = "in_total")
    private Double inTotal;
    @Column(name = "out_total")
    private Double outtotal;
    @Column(name = "package_id")
    private Long packageId;
    @Column(name = "units_id")
    private Long unitsId;
    @Column(name = "tranx_date")
    private Date tranxDate;
    private Boolean status;
    @Column(name = "fiscal_year_id")
    private Long fiscalYearId;
    @Column(name = "tranx_code")
    private String tranxCode;

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
