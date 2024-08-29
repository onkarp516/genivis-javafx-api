package in.truethics.ethics.ethicsapiv10.model.master;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.appconfig.AppConfig;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBarcode;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.*;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerBalanceSummary;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.report.DayBook;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContra;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNote;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstouput.GstOutputMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
import in.truethics.ethics.ethicsapiv10.model.user.UserRole;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "franchise_master_tbl")
public class FranchiseMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "franchise_name")
    private String franchiseName;
    @Column(name = "franchise_code")
    private String franchiseCode;
    @Column(name = "applicant_name")
    private String applicantName;
    @Column(name = "partner_name")
    private String partnerName;
    @Column(name = "district_id")
    private Long districtId;
    @Column(name = "regional_id")
    private Long regionalId;
    @Column(name = "zone_id")
    private Long zoneId;
    @Column(name = "state_id")
    private Long stateId;
    @Column(name = "invest_amt")
    private Boolean investAmt;
    @Column(name = "franchise_address")
    private String franchiseAddress;
    @Column(name = "residencial_address")
    private String residencialAddress;
    private String pincode;
    @Column(name = "corp_pincode")
    private String corpPincode;
    @Column(name = "mobile_number")
    private Long mobileNumber;
    @Column(name = "whatsapp_number")
    private Long whatsappNumber;
    private String gender;
    private LocalDate Dob;
    private Long age;
    @Column(name = "education")
    private String education;
    @Column(name = "present_occupation")
    private String presentOccupation;
    @Column(name = "aadar_upload")
    private String aadarUpload;
    @Column(name = "pan_upload")
    private String panUpload;
    @Column(name = "dl1upload")
    private String dL1Upload;
    @Column(name = "dl2upload")
    private String dL2Upload;
    @Column(name = "dl3upload")
    private String dL3Upload;
    @Column(name = "bank_upload")
    private String bankUpload;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "account_number")
    private String accountNumber;
    private String ifsc;
    @Column(name = "bank_branch")
    private String bankBranch;
    private String email;
    @Column(name = "state_code")
    private String stateCode;
    private String currency;
    @Column(name = "company_data_path")
    private String companyDataPath;
    @Column(name = "aadhar_no")
    private  String aadharNo;
    @Column(name = "pan_no")
    private String panNo;
    @Column(name = "dl1no")
    private String dl1No;
    @Column(name = "dl2no")
    private String dl2No;
    @Column(name = "dl3no")
    private String dl3No;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @ManyToOne
    @JoinColumn(name = "country_id")
    @JsonManagedReference
    private Country country;
    @ManyToOne
    @JoinColumn(name = "addr_state_id")
    @JsonManagedReference
    private State state;
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<Branch> branches;
    @Column(name = "business_type")
    private String businessType;//
    @Column(name = "business_trade")
    private String businessTrade;//Retailer, Distributor, Manufacturing
    @Column(name = "is_same_address")
    private Boolean isSameAddress;// for used sameAsRegisteredAddress
    private String area;
    private String district;
    @Column(name = "residencial_area")
    private String residencialArea;
    @Column(name = "residencial_state")
    private String residencialState;
    @Column(name = "residencial_district")
    private String residencialDistrict;
    private String latitude;
    private String longitude;
    @Column(name = "is_funded")
    private Boolean isFunded;//is_funded:1 ,if GV is Funded to FR otherwise is_funded:0
    @Column(name = "fund_amt")
    private Double fundAmt;//GV Funding Amt to FR

}
