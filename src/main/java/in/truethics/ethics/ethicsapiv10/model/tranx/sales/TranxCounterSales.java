package in.truethics.ethics.ethicsapiv10.model.tranx.sales;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tranx_counter_sales_tbl")
public class TranxCounterSales {
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

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxCounterSalesDetails> tranxCounterSalesDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxCounterSalesProdSrNo> tranxCounterSalesProdSrNos;
    @Column(name = "counter_sale_sr_no")
    private Long counterSaleSrNo; // it is used for counterNo
    @Column(name = "counter_sale_no")
    private String counterSaleNo;
    @Column(name = "transaction_date")
    private Date transactionDate;
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "doctor_id")
    private Long doctorId;
    @Column(name = "mobile_number")
    private Long mobileNumber;
    @Column(name = "total_bill")
    private Double totalBill;
    @Column(name = "total_base_amt")
    private Double totalBaseAmt;
    @Column(name = "taxable_amt")
    private Double taxableAmt;
    private Double roundoff;
    private Boolean status;
    @Column(name = "is_bill_converted")
    private Boolean isBillConverted;// 1: Converted  0: Not Converted
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "financial_year")
    private String financialYear;
    @Column(name = "total_discount")
    private Double totalDiscount;
    private String narrations;
    @Column(name = "free_qty")
    private Double freeQty; // free qty
    private Double totalqty;
    private Double totalcgst;
    private Double totalsgst;
    private Double totaligst;
    @Column(name = "payment_mode")
    private String paymentMode; //cash ,online , multi, all
    @Column(name = "payment_amount")
    private Double paymentAmount;
    private Double cash;
    private Double digital;
    @Column(name = "card_payment")
    private Double cardPayment;
    @Column(name = "advanced_amount")
    private Double advancedAmount;
    private String operations;
    @Column(name = "discount_amt")
    private Double discountAmt;//Discount In Amt
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    /*** only for Upahar Manufacturing Unit ***/
    @Column(name = "counter_sales_date")
    private LocalDate counterSalesDate;
    @Column(name = "transaction_status")
    private Long transactionStatus;

    @Column(name = "transaction_tracking_no")
    private String transactionTrackingNo;//transactionTrackingNo;
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed

}
