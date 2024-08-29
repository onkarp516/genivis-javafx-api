package in.truethics.ethics.ethicsapiv10.model.inventory;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstouput.GstOutputDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceDetailsUnits;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoiceProductSrNumber;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnDetailsUnits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_tbl")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_name")
    private String productName;
    @Column(name = "product_code")
    private String productCode;
    private Boolean status;
    private String description;
    private String alias;
    @Column(name = "is_warranty_applicable")
    private Boolean isWarrantyApplicable;
    @Column(name = "warranty_days")
    private int warrantyDays;
    @Column(name = "is_serial_number")
    private Boolean isSerialNumber;
    @Column(name = "is_batch_number")
    private Boolean isBatchNumber;
    @Column(name = "is_draft")
    private Boolean isDraft;
    @Column(name = "is_inventory")
    private Boolean isInventory;
    @Column(name = "is_brand")
    private Boolean isBrand;
    @Column(name = "is_group")
    private Boolean isGroup;
    @Column(name = "is_category")
    private Boolean isCategory;
    @Column(name = "is_sub_category")
    private Boolean isSubCategory;
    @Column(name = "is_mis")
    private Boolean isMIS;
    @Column(name = "is_formulation")
    private Boolean isFormulation;
    @Column(name = "upload_image")
    private String uploadImage;
    @Column(name = "is_package")
    private Boolean isPackage;
    @Column(name = "drug_type")
    private String drugType;
    @Column(name = "product_type")
    private String productType;
    @Column(name = "drug_contents")
    private String drugContents;
    @Column(name = "gv_of_products")
    private String gvOfProducts;
    @Column(name = "is_commision")
    private Boolean isCommision;
    @Column(name = "isgvproducts")
    private Boolean isGVProducts;
    @Column(name = "is_prescription")
    private Boolean isPrescription;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "fsr")
    private Double fsr;
    @Column(name = "csr")
    private Double csr;
    @Column(name = "mrp")
    private Double mrp;
    @Column(name = "fsrmh")
    private Double fsrmh;
    @Column(name = "csrmh")
    private Double csrmh;
    @Column(name = "fsrai")
    private Double fsrai;
    @Column(name = "csrai")
    private Double csrai;
    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;


    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductContentMaster> productContentMaster;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductUnitPacking> productUnitPackings;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductBarcode> productBarcodes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductBatchNo> productBatchNos;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerTransactionPostings> ledgerTransactionPostings;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductImagesMaster> productImagesMasters;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductOpeningStocks> productOpeningStocks;


    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<ProductTaxDateMaster> productTaxDateMasters;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceDetails> tranxPurInvoiceDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceDetailsUnits> tranxPurInvoiceDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceDetailsUnits> tranxSalesInvoiceDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesReturnDetailsUnits> tranxSalesReturnDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceProductSrNumber> tranxSalesInvoiceProductSrNumbers;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurchaseInvoiceProductSrNumber> tranxPurchaseInvoiceProductSrNumbers;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceDetails> tranxPurReturnInvoiceDetails;


    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceProductSrNo> tranxPurReturnInvoiceProductSrNos;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnDetailsUnits> tranxPurReturnDetailsUnits;

    @JsonBackReference
    @OneToMany
    private List<TranxPurOrderDetails> purchaseOrderDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurOrderDetailsUnits> tranxPurOrderDetailsUnits;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanDetails> tranxPurChallanDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanDetailsUnits> tranxPurChallanDetailsUnits;
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurchaseChallanProductSrNumber> tranxPurchaseChallanProductSrNumbers;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<InventoryDetailsPostings> inventoryDetailsPostings;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @ManyToOne
    @JoinColumn(name = "packing_master_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

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
    @JoinColumn(name = "hsn_id")
    @JsonManagedReference
    private ProductHsn productHsn;

    @ManyToOne
    @JoinColumn(name = "taxmaster_id")
    @JsonManagedReference
    private TaxMaster taxMaster;

    @Column(name = "shelf_id")
    private String shelfId;
    @Column(name = "tax_type")
    private String taxType; //taxable, tax paid, exmpted
    @Column(name = "applicable_date")
    private LocalDate applicableDate;
    private Double igst;
    private Double cgst;
    private Double sgst;
    @Column(name = "barcode_sales_qty")
    private Double barcodeSalesQty;
    @Column(name = "purchase_rate")
    private Double purchaseRate;
    @Column(name = "margin_per")
    private Double marginPer;//margin in percentage
    @Column(name = "barcode_no")
    private String barcodeNo;//barcode Number
    private Double weight;
    @Column(name = "weight_unit")
    private String weightUnit;
    @Column(name = "discount_in_per")
    private Double discountInPer;
    @Column(name = "min_stock")
    private Double minStock;
    @Column(name = "max_stock")
    private Double maxStock;



    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstInputDetails> gstInputDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstOutputDetails> gstOutputDetails;

    @ManyToOne
    @JoinColumn(name = "subgroup_id")
    @JsonManagedReference
    private Subgroup subgroup;
    @Column(name = "is_delete")
    private Boolean isDelete;//whether product can delete or not, 1: can delete ,if it is not involved into any tranxs,
    // 0: can't delete,if it is involved into any tranxs
    /**** END ****/

    @Column(name = "ecommerce_type_id")
    private Long ecommerceTypeId;
    @Column(name = "selling_price")
    private Double sellingPrice;
    @Column(name = "discount_per")
    private Double discountPer;
    private Double amount;
    private Double loyalty;
    private String image1;
    private String image2;
    private String image3;
    private String image4;
    private String image5;

}


