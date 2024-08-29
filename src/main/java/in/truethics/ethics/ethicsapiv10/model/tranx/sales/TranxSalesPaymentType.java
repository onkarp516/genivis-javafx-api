package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNote;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNoteNewReferenceMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
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
@Table(name = "sales_payment_type_tbl")
public class TranxSalesPaymentType {
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
    @JoinColumn(name = "ledger_id")
    @JsonManagedReference
    private LedgerMaster ledgerMaster;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @ManyToOne
    @JoinColumn(name = "tranx_sales_invoice_id")
    @JsonManagedReference
    private TranxSalesInvoice tranxSalesInvoice;

    @ManyToOne
    @JoinColumn(name = "tranx_sales_comp_invoice_id")
    @JsonManagedReference
    private TranxSalesCompInvoice tranxSalesCompInvoice;


    private String type; //bank_account or others
    private String label;//SBI bank or Cash A/c
    @Column (name ="created_by")
    private Long createdBy;
    @Column (name ="payment_mode")
    private String paymentMode;
    @Column (name ="payment_amount")
    private Double paymentAmount;
    @Column (name ="created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column (name ="updated_by")
    private Long updatedBy;
    @Column (name ="updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private Boolean status;
    @Column (name ="invoice_type")
    private String invoiceType;
    @Column(name = "payment_master_id")
    private Long paymentMasterId;
    @Column(name = "reference_id")
    private String referenceId;
    @Column(name = "customer_bank")
    private String customerBank;

}
