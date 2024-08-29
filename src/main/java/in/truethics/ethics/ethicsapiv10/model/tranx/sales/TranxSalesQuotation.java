package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_sales_quotation_tbl")
public class TranxSalesQuotation {
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
    @JoinColumn(name = "sundry_debtors_id")
    @JsonManagedReference
    private LedgerMaster sundryDebtors;

    @ManyToOne
    @JoinColumn(name = "sales_account_ledger_id")
    @JsonManagedReference
    private LedgerMaster salesAccountLedger;

    @ManyToOne
    @JoinColumn(name = "sales_roundoff_id")
    @JsonManagedReference
    private LedgerMaster salesRoundOff;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesQuotationDetails> tranxSalesQuotationDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesQuotationDutiesTaxes> salesQuotationDutiesTaxes;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "transaction_status_id", nullable = false)
    private TransactionStatus transactionStatus;

    @Column(name = "sales_quotation_sr_no")
    private Long salesQuotationSrNo;
    @Column(name = "bill_date")
    private Date billDate;
    private String reference;
    private String sq_bill_no;
    @Column(name = "gst_number")
    private String gstNumber;
    @Column(name = "round_off")
    private Double roundOff;
    @Column(name = "total_base_amount")
    private Double totalBaseAmount;  //qty*base_amount
    @Column(name = "total_amount")
    private Double totalAmount;
    private Double totalcgst;
    private Long totalqty;
    private Double totalsgst;
    private Double totaligst;
    @Column(name = "additional_charges_total")
    private Double additionalChargesTotal;
    @Column(name = "taxable_amount")
    private Double taxableAmount;
    private Double tcs;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
    @Column(name = "financial_year")
    private String financialYear;
    private String operations;
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Column(name = "updated_by")
    private Long updatedBy;
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    private String narration;



    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @ManyToOne
    @JoinColumn(name = "ledger_id")
    @JsonManagedReference
    private LedgerMaster ledgerMaster;
    @Column(name = "ledger_amt")
    private Double ledgerAmt;
    @ManyToOne
    @JoinColumn(name = "saleman_id")
    @JsonManagedReference
    private Users salesmanId;
    private String barcode;
    @Column(name = "free_qty")
    private Double freeQty;
    @Column(name = "gross_amount")
    private Double grossAmount; // gross total
    @Column(name = "total_tax")
    private Double totalTax; // tax

    @Column(name = "transaction_tracking_no")
    private String transactionTrackingNo;//transactionTrackingNo;
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed

}
