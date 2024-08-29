package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_sales_return_invoice_tbl")
public class TranxSalesReturnInvoice {
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
    @JoinColumn(name = "sales_discount_ledger_id")
    @JsonManagedReference
    private LedgerMaster salesDiscountLedger;

    @ManyToOne
    @JoinColumn(name = "sales_roundoff_id")
    @JsonManagedReference
    private LedgerMaster salesRoundOff;

    @ManyToOne
    @JoinColumn(name = "associates_groups_id")
    @JsonManagedReference
    private AssociateGroups associateGroups;

    @ManyToOne
    @JoinColumn(name = "sales_invoice_id")
    @JsonManagedReference
    private TranxSalesInvoice tranxSalesInvoice;

    @ManyToOne
    @JoinColumn(name = "tranx_sales_challan_id")
    @JsonManagedReference
    private TranxSalesChallan tranxSalesChallan;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesReturnInvoiceAddCharges> tranxSalesReturnInvoiceAddCharges;

    @Column(name = "sales_rtn_sr_no")
    private Long salesRtnSrNo;
    @Column(name = "sales_return_no")
    private String salesReturnNo; // Sales ReturnNo is sundry debtors billNo
    @Column(name = "transaction_date")
    private Date transactionDate;
    @Column(name = "round_off")
    private Double roundOff;
    @Column(name = "total_base_amount")
    private Double totalBaseAmount;  //qty*base_amount
    @Column(name = "total_amount")
    private Double totalAmount;
    @Column(name = "totalcgst")
    private Double totalcgst;
    @Column(name = "totalqty")
    private Long totalqty;
    @Column(name = "totalsgst")
    private Double totalsgst;
    @Column(name = "totaligst")
    private Double totaligst;
    @Column(name = "sales_discount_amount")
    private Double salesDiscountAmount;
    @Column(name = "sales_discount_per")
    private Double salesDiscountPer;
    @Column(name = "total_sales_discount_amt")
    private Double totalSalesDiscountAmt;
    @Column(name = "additional_charges_total")
    private Double additionalChargesTotal;
    @Column(name = "taxable_amount")
    private Double taxableAmount;
    private Double tcs;
    private Boolean status;
    @Column(name = "financial_year")
    private String financialYear;
    private String narration;
    private String operations;
    @Column(name = "gst_number")
    private String gstNumber;
    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @ManyToOne
    @JoinColumn(name = "additional_ledger_id1")
    @JsonManagedReference
    private LedgerMaster additionLedger1;

    @ManyToOne
    @JoinColumn(name = "additional_ledger_id2")
    @JsonManagedReference
    private LedgerMaster additionLedger2;

    @ManyToOne
    @JoinColumn(name = "additional_ledger_id3")
    @JsonManagedReference
    private LedgerMaster additionLedger3;

    @Column(name = "addition_ledger_amt1")
    private Double additionLedgerAmt1;
    @Column(name = "addition_ledger_amt2")
    private Double additionLedgerAmt2;
    @Column(name = "addition_ledger_amt3")
    private Double additionLedgerAmt3;
    @Column(name = "free_qty")
    private Double freeQty; // free qty
    @Column(name = "gross_amount")
    private Double grossAmount; // gross total
    @Column(name = "total_tax")
    private Double totalTax; // tax
    @Column(name = "payment_mode")
    private String paymentMode;
    @Column(name = "payment_amount")
    private Double paymentAmount;
    @Column(name = "payment_transaction_num")
    private String paymentTransactionNum;
    @Column(name = "payment_date")
    private LocalDate paymentDate;
    @ManyToOne
    @JoinColumn(name = "saleman_id")
    @JsonManagedReference
    private Users salesmanId;

    private String barcode;
    @Column(name = "is_round_off")
    private Boolean isRoundOff; // check if round of is applicable or not
    @Column(name = "tcs_amt")
    private Double tcsAmt;
    @Column(name = "tcs_mode")
    private String tcsMode;
    @Column(name = "tds_amt")
    private Double tdsAmt;
    @Column(name = "tds_per")
    private Double tdsPer;//TDS Per

    @Column(name = "transaction_tracking_no")
    private String transactionTrackingNo;//transactionTrackingNo;
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed

}
