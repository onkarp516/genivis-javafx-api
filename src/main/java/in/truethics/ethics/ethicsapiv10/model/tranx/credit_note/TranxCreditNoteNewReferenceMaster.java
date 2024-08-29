package in.truethics.ethics.ethicsapiv10.model.tranx.credit_note;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesReturnInvoice;
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
@Table(name = "tranx_credit_note_new_reference_tbl")
public class TranxCreditNoteNewReferenceMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "outlet_id")
    private Long outletId;
    @Column(name = "branch_id")
    private Long branchId;
    @Column(name = "sundry_debtors_id")
    private Long sundryDebtorsId;
    @Column(name = "tranx_sales_return_invoice_id")
    private Long tranxSalesReturnInvoiceId;
    @Column(name = "transaction_status_id")
    private Long transactionStatusId;
    @Column(name = "fiscal_year_id")
    private Long fiscalYearId;
    private Long srno;
    @Column(name = "creditnote_new_reference_no")
    private String creditnoteNewReferenceNo; //auto generate
    @Column(name = "round_off")
    private Double roundOff;
    @Column(name = "total_base_amount")
    private Double totalBaseAmount;  //qty*base_amount
    @Column(name = "total_amount")
    private Double totalAmount;
    @Column(name = "taxable_amount")
    private Double taxableAmount;
    private Double totalgst;
    @Column(name = "sales_discount_amount")
    private Double salesDiscountAmount;
    @Column(name = "sales_discount_per")
    private Double salesDiscountPer;
    @Column(name = "total_sales_discount_amt")
    private Double totalSalesDiscountAmt;
    @Column(name = "additional_charges_total")
    private Double additionalChargesTotal;
    @Column(name = "financial_year")
    private String financialYear;
    @Column(name = "source")
    private String source;
    @Column(name = "transcation_date")
    private Date transcationDate;
    @Column(name = "narrations")
    private String narrations;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "status")
    private boolean status;
    @Column(name = "adjustment_status")
    private String adjustmentStatus; // immediate , future or refund
    @Column(name = "balance")
    private Double balance;
    @Column(name = "sales_invoice_id")
    private Long salesInvoiceId;
    @Column(name = "sales_challan_id")
    private Long salesChallanId;
    @Column(name = "receipt_id")
    private Long receiptId; // maitaining receipt id incase of advance payment is given(new reference is created)
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed


}
