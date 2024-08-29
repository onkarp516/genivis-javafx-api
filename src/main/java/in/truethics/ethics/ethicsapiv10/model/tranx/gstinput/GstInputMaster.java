package in.truethics.ethics.ethicsapiv10.model.tranx.gstinput;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.TranxSalesInvoice;
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
@Table(name = "tranx_gst_input_tbl")
public class GstInputMaster {
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
    @JoinColumn(name = "supplier_id")
    @JsonManagedReference
    private LedgerMaster supplierLedger;

    @ManyToOne
    @JoinColumn(name = "posting_ledger_id")
    @JsonManagedReference
    private LedgerMaster postingLedger;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @ManyToOne
    @JoinColumn(name = "payment_mode_id")
    @JsonManagedReference
    private PaymentModeMaster paymentModeMaster;

    @ManyToOne
    @JoinColumn(name = "roundoff_id")
    @JsonManagedReference
    private LedgerMaster roundOffLedger;
    @Column(name = "total_igst")
    private Double totalIgst;
    @Column(name = "round_off")
    private Double roundOff;
    @Column(name = "total_cgst")
    private Double totalCgst;
    @Column(name = "total_sgst")
    private Double totalSgst;
    @Column(name = "voucher_sr_no")
    private String voucherSrNo;
    @Column(name = "voucher_no")
    private String voucherNo;
    @Column(name = "tranx_date")
    private LocalDate tranxDate;
    @Column(name = "voucher_date")
    private LocalDate voucherDate;
    private String narrations;
    @Column(name = "payment_tranx_no")
    private String paymentTranxNo;
    @Column(name = "total_amount")
    private Double totalAmount;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
    @Column(name = "updated_by")
    private Long updatedBy;
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstInputDetails> gstInputDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstInputDutiesTaxes> gstInputDutiesTaxes;
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed


}
