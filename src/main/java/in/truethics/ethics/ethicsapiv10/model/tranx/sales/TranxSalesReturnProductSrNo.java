package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.*;
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
@Table(name = "tranx_sales_return_prod_sr_no_tbl")
public class TranxSalesReturnProductSrNo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id", nullable = false)
    @JsonManagedReference
    private Outlet outlet;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonManagedReference
    private Product product;


    @ManyToOne
    @JoinColumn(name = "transaction_type_master_id", nullable = true)
    @JsonManagedReference
    private TransactionTypeMaster transactionTypeMaster;

    @Column(name = "serial_no")
    private String serialNo;
    @Column(name = "sale_return_created_at")
    private LocalDateTime saleReturnCreatedAt;
    @Column(name = "transaction_status")
    private String transactionStatus; //purchase or sales or counter sales
    private String operations;
    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    private Boolean status;

    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @ManyToOne
    @JoinColumn(name = "level_a_id")
    @JsonManagedReference
    private LevelA levelA;

    @ManyToOne
    @JoinColumn(name = "level_b_id")
    @JsonManagedReference
    private LevelB levelB;

    @ManyToOne
    @JoinColumn(name = "level_c_id")
    @JsonManagedReference
    private LevelC levelC;


    @ManyToOne
    @JoinColumn(name = "sales_return_unit_details_id")
    @JsonManagedReference
    private TranxSalesReturnDetailsUnits tranxSalesReturnDetailsUnits;

    @ManyToOne
    @JoinColumn(name = "units_id")
    @JsonManagedReference
    private Units units;

}

