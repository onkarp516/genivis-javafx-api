package in.truethics.ethics.ethicsapiv10.model.tranx.purchase;


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
@Table(name = "tranx_pur_return_invoice_product_sr_no_tbl")
public class TranxPurReturnInvoiceProductSrNo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "transaction_type_master_id")
    @JsonManagedReference
    private TransactionTypeMaster transactionTypeMaster;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;
    @Column(name = "serial_no")
    private String serialNo;
    @Column(name = "pur_return_created_at")
    private LocalDateTime purReturnCreatedAt;
    @Column(name = "transaction_status")
    private String transactionStatus; //purchase or sales or counter sales
    private String operations;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
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
    @JoinColumn(name = "pur_return_unit_details_id")
    @JsonManagedReference
    private TranxPurReturnDetailsUnits tranxPurReturnDetailsUnits;

    @ManyToOne
    @JoinColumn(name = "units_id")
    @JsonManagedReference
    private Units units;
}
