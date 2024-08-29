package in.truethics.ethics.ethicsapiv10.model.user;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.access_permissions.SystemAccessPermissions;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerBalanceSummary;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users_tbl")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String usercode;
    private String username;
    @Column(name = "user_role")
    private String userRole;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "mobile_number")
    private Long mobileNumber;
    private String email;
    private String address;
    private String gender;
    private String password;
    private String plain_password;
    @CreationTimestamp
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private Boolean status;
    @Column(name = "is_super_admin")
    private Boolean isSuperAdmin;
    private String permissions;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @Column(name = "company_code")
    private String companyCode;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonManagedReference
    private Outlet outlet;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerBalanceSummary> ledgerBalanceSummaries;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<SystemAccessPermissions> systemAccessPermissions;

    /****** Modification after PK(Jaipur) visits at Solapur 25th to 30th January 2023 ******/
    @ManyToOne
    @JoinColumn(name = "role_master_id")
    @JsonManagedReference
    private UserRole roleMaster;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoice> tranxSalesInvoices;

    private LocalDate anniversary;
    private LocalDate Dob;

}
