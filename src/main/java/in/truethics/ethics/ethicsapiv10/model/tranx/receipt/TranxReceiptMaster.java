package in.truethics.ethics.ethicsapiv10.model.tranx.receipt;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesCompInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesOrder;
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
@Table(name = "tranx_receipt_master_tbl")
public class TranxReceiptMaster {
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
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    /***** this scenario is only for Upahar Trading to maintained receipt against sales order or invoice ****/
    @ManyToOne
    @JoinColumn(name = "sales_invoice_id")
    @JsonManagedReference
    private TranxSalesOrder tranxSalesOrder;
    /***** this scenario is only for Upahar Trading ****/

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxReceiptPerticulars> tranxReceiptPerticulars;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxReceiptPerticularsDetails> tranxReceiptPerticularsDetails;

    @Column(name = "receipt_no")
    private String receiptNo;
    @Column(name = "receipt_sr_no")
    private double receiptSrNo;
    @Column(name = "transcation_date")
    private Date transcationDate;
    @Column(name = "total_amt")
    private double totalAmt;
    private boolean status;
    private String narrations;
    @Column(name = "financial_year")
    private String financialYear;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "return_amt")
    private Double returnAmt;

    /****** if Payment is done from Sales Invoice, to maintain the receipt against Sales Invoice ******/
    @ManyToOne
    @JoinColumn(name = "tranx_sales_invoice_id")
    @JsonManagedReference
    private TranxSalesInvoice tranxSalesInvoice;

    @ManyToOne
    @JoinColumn(name = "tranx_sales_comp_invoice_id")
    @JsonManagedReference
    private TranxSalesCompInvoice tranxSalesCompInvoice;
    @Column(name = "invoice_type")
    private String invoiceType;
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed

    /***** Franchise Specific Changes , to get Bank Details of GV in Franchise ******/
    @Column(name = "gv_payment_tranx_no")
    private String gvPaymentTranxNo;

    @Column(name = "gv_bank_name")
    private String gvBankName;

    @Column(name = "gv_bank_ledger_id")
    private Long gvBankLedgerId;

    @Column(name = "gv_payment_mode")
    private String gvPaymentMode;

    @Column(name = "gv_payment_date")
    private LocalDate gvPaymentDate;

    @Column(name = "is_fr_receipt")
    private Boolean isFrReceipt;

    /*****End of Franchise Specific Changes , to get Bank Details of GV in Franchise ******/

}
