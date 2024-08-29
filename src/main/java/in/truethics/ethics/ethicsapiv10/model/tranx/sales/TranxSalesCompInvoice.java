package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNote;
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
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_sales_comp_invoice_tbl")
public class TranxSalesCompInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "outlet_id")
    private Long outletId;

    @Column(name = "sundry_debtors_id")
    private Long sundryDebtorsId;

    @Column(name = "sales_account_ledger_id")
    private Long salesAccountLedgerId;

    @Column(name = "sales_discount_ledger_id")
    private Long salesDiscountLedgerId;

    @Column(name = "sales_round_off_id")
    private Long salesRoundOffId;

    @Column(name = "associate_groups_id")
    private Long associateGroupsId;

    @Column(name = "fiscal_year_id")
    private Long fiscalYearId;

    @Column(name = "sales_serial_number")
    private Long salesSerialNumber;
    @Column(name = "sales_invoice_no")
    private String salesInvoiceNo;
    @Column(name = "bill_date")
    private Date billDate;
    //    private LocalDate invoice_date;
    @Column(name = "transport_name")
    private String transportName;
    private String reference;
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
    @Column(name = "sales_discount_amount")
    private Double salesDiscountAmount; // purchase_discount
    @Column(name = "sales_discount_per")
    private Double salesDiscountPer; // purchase_discount_amt
    @Column(name = "total_sales_discount_amt")
    private Double totalSalesDiscountAmt; // discount
    @Column(name = "additional_charges_total")
    private Double additionalChargesTotal;
    @Column(name = "taxable_amount")
    private Double taxableAmount; // total
    private Double tcs;
    @Column(name = "is_counter_sale")
    private Boolean isCounterSale;
    @Column(name = "counter_sale_id")
    private String counterSaleId;
    private Boolean status;
    @Column(name = "financial_year")
    private String financialYear;
    private String narration;
    private String operations;
    @Column(name = "reference_sq_id")
    private String referenceSqId;//Reference of Sales Quatations Ids
    @Column(name = "reference_so_id")
    private String referenceSoId;//Reference of Sales Order Ids
    @Column(name = "reference_sc_id")
    private String referenceScId;//Reference of Sales Challan Ids
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "payment_mode")
    private String paymentMode;
    @Column(name = "payment_amount")
    private Double paymentAmount;
    private Double cash;
    private Double digital;
    @Column(name = "card_payment")
    private Double cardPayment;
    @Column(name = "advanced_amount")
    private Double advancedAmount;
    @Column(name = "gst_number")
    private String gstNumber;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private Double balance;

    /****** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @Column(name = "addition_ledger1id")
    private Long additionLedger1Id;
    @Column(name = "addition_ledger2id")
    private Long additionLedger2Id;
    @Column(name = "addition_ledger3id")
    private Long additionLedger3Id;
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
    @Column(name = "salesman_id")
    private Long salesmanId;
    private String barcode;
    @Column(name = "salesman_user")
    private Long salesmanUser;
    @Column(name = "transaction_status")
    private Long transactionStatus; // maitaining return products while selecting bills, dont allow same bill next time for return
    @Column(name = "is_selected")
    private Boolean isSelected; // check whether this debitnote is selected or not while adjusting against the purchase invoice
    @Column(name = "is_credit_note_ref")
    private Boolean isCreditNoteRef; //check for the debit note reference while creating purchase invoice
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
    @Column(name = "image_path")
    private String imagePath;//uploading image of bill
    @Column (name = "doctor_id")
    private Long doctorId;
    @Column (name = "client_name")
    private String clientName;
    @Column (name = "client_address")
    private String clientAddress;
    @Column (name = "mobile_number")
    private String mobileNumber;

    @Column(name = "transaction_tracking_no")
    private String transactionTrackingNo;//transactionTrackingNo;
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "doctor_address")
    private String doctorAddress;

    @Column(name = "image_upload")
    private String imageUpload;




}
