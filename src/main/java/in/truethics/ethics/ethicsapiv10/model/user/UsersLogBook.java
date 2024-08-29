package in.truethics.ethics.ethicsapiv10.model.user;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_log_books_tbl")
public class UsersLogBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean status;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "outlet_id")
    private Long outletId;

    @ManyToOne
    @JoinColumn(name = "ledger_master_id")
    @JsonManagedReference
    private LedgerMaster ledgerMaster;

    private String products;

    @ManyToOne
    @JoinColumn(name = "users_id")
    @JsonManagedReference
    private Users users;

    @Column(name = "log_type")
    private String logType;//NEW ,MODIFY, DELETE,ENTER,EXIT,CANCEL
    @Column(name = "tranx_id")
    private Long tranxId;//Trasaction Invoice Id
    @Column(name = "voucher_type")
    private String voucherType;//Purchase ,Sales ,Receipt, Payment
    @Column(name = "voucher_no")
    private String voucherNo;
    @Column(name = "tranx_date")
    private Date tranxDate;
    @Column(name = "invoice_date")
    private Date invoiceDate;
    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @Column(name = "tranx_value")
    private Double tranxValue;// Amount
    @Column(name = "modify_value")
    private Double modifyValue; // Modified Value
    @Column(name="difference")
    private Double difference;//
    @Column(name = "pc_name")
    private String pcName;
}
