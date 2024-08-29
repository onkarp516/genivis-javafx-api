package in.truethics.ethics.ethicsapiv10.model.barcode;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_barcode_tbl")
public class ProductBarcode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "barcode_unique_code")
    private String barcodeUniqueCode;
    @Column(name = "company_barcode")
    private String companyBarcode;
    @Column(name = "batch_no")
    private String batchNo;
    @Column(name = "serial_no")
    private String serialNo;
    private Double mrp;
    @Column(name = "tranx_date")
    private LocalDate tranxDate;
    private Boolean enable;
    private Boolean status;
    @Column(name = "print_qnt")
    private Integer printQnt;
    private Integer qnty;
    @Column(name = "is_auto")
    private Boolean isAuto;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "packaging_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    @JsonManagedReference
    private Units units;

    @ManyToOne
    @JoinColumn(name = "flavour_master_id")
    @JsonManagedReference
    private FlavourMaster flavourMaster;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    @JsonManagedReference
    private ProductBatchNo productBatch;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    @JsonManagedReference
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonManagedReference
    private Group group;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonManagedReference
    private Category category;

    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    @JsonManagedReference
    private Subcategory subcategory;

    @ManyToOne
    @JoinColumn(name = "tranx_id")
    @JsonManagedReference
    private TransactionTypeMaster transactionTypeMaster;

    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

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
    @Column(name = "transaction_id")
    private Long transactionId;//Transaction Purchase, Sales Id
}
