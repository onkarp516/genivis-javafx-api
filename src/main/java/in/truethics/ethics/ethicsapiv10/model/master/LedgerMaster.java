package in.truethics.ethics.ethicsapiv10.model.master;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerBalanceSummary;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionDetails;
import in.truethics.ethics.ethicsapiv10.model.ledgers_details.LedgerTransactionPostings;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContra;
import in.truethics.ethics.ethicsapiv10.model.tranx.contra.TranxContraDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.credit_note.TranxCreditNote;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputDutiesTaxes;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstouput.GstOutputDutiesTaxes;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstouput.GstOutputMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.journal.TranxJournalDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.payment.TranxPaymentPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.purchase.*;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticulars;
import in.truethics.ethics.ethicsapiv10.model.tranx.receipt.TranxReceiptPerticularsDetails;
import in.truethics.ethics.ethicsapiv10.model.tranx.sales.*;
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
@Table(name = "ledger_master_tbl")
public class LedgerMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ledger_name")
    private String ledgerName;
    @Column(name = "ledger_code")
    private String ledgerCode;
    @Column(name = "unique_code")
    private String uniqueCode;
    @Column(name = "mailing_name")
    private String mailingName;
    @Column(name = "opening_bal_type")
    private String openingBalType;
    @Column(name = "opening_bal")
    private Double openingBal;
    private String address;
    private Long pincode;
    private String email;
    private Long mobile;
    private Boolean taxable;//isGST
    private String gstin;
    @Column(name = "state_code")
    private String stateCode;
    @Column(name = "registration_type")
    private Long registrationType;
    @Column(name = "date_of_registration")
    private LocalDate dateOfRegistration;
    private String pancard;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "account_number")
    private String accountNumber;
    private String ifsc;
    @Column(name = "bank_branch")
    private String bankBranch;
    @Column(name = "tax_type")
    private String taxType;
    @Column(name = "slug_name")
    private String slugName;
    @Column(name = "created_by")
    private Long createdBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private Boolean status;
    @Column(name = "under_prefix")
    private String underPrefix;
    @Column(name = "is_deleted")
    private Boolean isDeleted; //isDelete : true means , we can delete this ledge,if it is not involved into any tranxs
    @Column(name = "is_default_ledger")
    private Boolean isDefaultLedger;
    @Column(name = "is_private")
    private Boolean isPrivate;
    /* pune visit new changes */
    @Column(name = "credit_days")
    private Integer creditDays;
    @Column(name = "applicable_from")
    private String applicableFrom; //from billDate or deliveryDate
    @Column(name = "food_license_no")
    private String foodLicenseNo;
    @Column(name = "fssai_expiry")
    private LocalDate fssaiExpiry; //Food License Expiry Date
    private Boolean tds;
    @Column(name = "tds_applicable_date")
    private LocalDate tdsApplicableDate;
    private Boolean tcs;
    @Column(name = "tcs_applicable_date")
    private LocalDate tcsApplicableDate;
    private String district;
    private String area;
    @Column(name = "land_mark")
    private String landMark;
    private String city;
    @Column(name = "drug_license_no")
    private String drugLicenseNo;
    @Column(name = "drug_expiry")
    private LocalDate drugExpiry;
    @Column(name = "sales_rate")
    private Double salesRate;
    /* ..... End .... */

    @ManyToOne
    @JoinColumn(name = "principle_id")
    @JsonManagedReference
    private Principles principles;

    @ManyToOne
    @JoinColumn(name = "principle_groups_id")
    @JsonManagedReference
    private PrincipleGroups principleGroups;

    @ManyToOne
    @JoinColumn(name = "foundation_id")
    @JsonManagedReference
    private Foundations foundations;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;

    @ManyToOne
    @JoinColumn(name = "country_id")
    @JsonManagedReference
    private Country country;

    @ManyToOne
    @JoinColumn(name = "state_id")
    @JsonManagedReference
    private State state;
    @ManyToOne
    @JoinColumn(name = "owner_state_id")
    @JsonManagedReference
    private State ownerstate;

    @ManyToOne
    @JoinColumn(name = "balancing_method_id")
    @JsonManagedReference
    private BalancingMethod balancingMethod;

    @ManyToOne
    @JoinColumn(name = "associates_groups_id")
    @JsonManagedReference
    private AssociateGroups associateGroups;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerTransactionDetails> ledgerTransactionDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerBalanceSummary> ledgerBalanceSummaries;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerDeptDetails> ledgerDeptDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerGstDetails> ledgerGstDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerShippingAddress> ledgerShippingAddresses;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerBillingDetails> ledgerBillingDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerBankDetails> ledgerBankDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceDutiesTaxes> tranxPurInvoiceDutiesTaxes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoiceAdditionalCharges> tranxPurInvoiceAdditionalCharges;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoice> tranxSalesInvoices;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceDutiesTaxes> tranxSalesInvoiceDutiesTaxes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesInvoiceAdditionalCharges> tranxSalesInvoiceAdditionalCharges;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<LedgerTransactionPostings> ledgerTransactionPostings;
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurInvoice> tranxPurInvoices;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxContra> tranxContras;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxContraDetails> tranxContraDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxCreditNote> tranxCreditNotes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoice> tranxPurReturnInvoices;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceAddCharges> tranxPurReturnInvoiceAddCharges;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurReturnInvoiceDutiesTaxes> tranxPurReturnInvoiceDutiesTaxes;

    @JsonBackReference
    @OneToMany
    private List<TranxPurOrderDutiesTaxes> tranxPurOrderDutiesTaxes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurOrder> tranxPurOrders;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallan> tranxPurChallans;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanAdditionalCharges> tranxPurChallanAdditionalCharges;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPurChallanDutiesTaxes> tranxPurChallanDutiesTaxes;


    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxReceiptPerticulars> tranxReceiptPerticulars;


    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxReceiptPerticularsDetails> tranxReceiptPerticularsDetails;


    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPaymentPerticulars> tranxPaymentPerticulars;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxPaymentPerticularsDetails> tranxPaymentPerticularsDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxJournalDetails> tranxJournalDetails;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesPaymentType> tranxSalesPaymentTypes;


    /**** Modification after PK visits at Solapur 25th to 30th January 2023 ******/
    @Column(name = "license_no")
    private String licenseNo;
    @Column(name = "license_expiry")
    private LocalDate licenseExpiry;
    @Column(name = "food_license_expiry")
    private LocalDate foodLicenseExpiry;
    @Column(name = "manufacturing_license_no")
    private String manufacturingLicenseNo;
    @Column(name = "manufacturing_license_expiry")
    private LocalDate manufacturingLicenseExpiry;
    @Column(name = "gst_transfer_date")
    private LocalDate gstTransferDate;
    private String place;
    @Column(name = "business_type")
    private String businessType;
    @Column(name = "business_Trade")
    private String businessTrade;
    private String route;
    @Column(name = "credit_bill_date")
    private LocalDate creditBillDate;
    @Column(name = "lr_bill_date")
    private LocalDate lrBillDate;
    private LocalDate anniversary;
    private LocalDate Dob;
    @Column(name = "credit_type_days")
    private String creditTypeDays; //no.of Days,
    @Column(name = "credit_type_bills")
    private String creditTypeBills; //no of Bills
    @Column(name = "credit_type_value")
    private String creditTypeValue; // Bill Value
    @Column(name = "credit_num_bills")
    private Double creditNumBills;
    @Column(name = "credit_bill_value")
    private Double creditBillValue;
    @Column(name = "is_first_discount_per_calculate")
    private Boolean isFirstDiscountPerCalculate; // if true then first disc per calculate then apply disc amount on amount in tranx level
    @Column(name = "take_discount_amount_in_lumpsum")
    private Boolean takeDiscountAmountInLumpsum; // if true then take discount amount in lumpsum else disc amount per piece in tranx level
    @Column(name = "is_migrated")
    private Boolean isMigrated; // 1: Migrated from Compositions to Registered And 0 : otherwise
    // Migrated from Unregistered to Compositions
    // Migrated from Unregistered to Registered
    @Column(name = "columna")
    private String columnA;   // columnA = salesman
    @Column(name = "columnb")
    private String columnB;
    @Column(name = "columnc")
    private String columnC;
    @Column(name = "columnd")
    private String columnD;
    @Column(name = "columne")
    private Double columnE;
    @Column(name = "columnf")
    private Double columnF;
    @Column(name = "columng")
    private Double columnG;
    @Column(name = "columnh")
    private Double columnH;
    @Column(name = "columni")
    private LocalDate columnI;
    @Column(name = "columnj")
    private LocalDate columnJ;
    @Column(name = "columnk")
    private LocalDate columnK;
    @Column(name = "columnl")
    private LocalDate columnL;
    @Column(name = "columnm")
    private Long columnM;
    @Column(name = "columnn")
    private Long columnN;
    @Column(name = "columno")
    private Boolean columnO;
    @Column(name = "columnp")
    private Boolean columnP;
    @Column(name = "columnq")
    private Boolean columnQ;
    @Column(name = "columnr")
    private Boolean columnR;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstInputMaster> gstInputMasters;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstInputDutiesTaxes> gstInputDutiesTaxes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstOutputMaster> gstOutputMasters;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstOutputDutiesTaxes> gstOutputDutiesTaxes;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxSalesReturnInvoice> tranxSalesReturnInvoices;
    @Column(name = "area_id")
    private Long areaId;
    @Column(name = "salesman_id")
    private Long salesmanId;
    @Column(name = "whats_appno")
    private Long whatsAppno;
    @Column(name = "is_credit")
    private Boolean isCredit;
    @Column(name = "is_license")
    private Boolean isLicense;
    @Column(name = "is_shipping_details")
    private Boolean isShippingDetails;
    @Column(name = "is_department")
    private Boolean isDepartment;
    @Column(name = "is_bank_details")
    private Boolean isBankDetails;
    @Column(name = "district_head_id")
    private Long districtHeadId;
    @Column(name = "zonal_head_id")
    private Long zonalHeadId;
    @Column(name = "regional_head_id")
    private Long regionalHeadId;
    @Column(name = "state_head_id")
    private Long stateHeadId;
    @Column(name = "owner_address")
    private String ownerAddress;
    @Column(name = "owner_name")
    private String ownerName;
    @Column(name = "owner_pincode")
    private Long ownerPincode;
    @Column(name = "owner_email")
    private String ownerEmail;
    @Column(name = "owner_mobile")
    private Long ownerMobile;
    @Column(name = "owner_whatsapp_no")
    private Long ownerWhatsappNo;
    private Long education;
    private Long age;
    @Column(name = "present_occupation")
    private String presentOccupation;
    @Column(name = "aadar_upload")
    private String aadarUpload;
    @Column(name = "pan_upload")
    private String panUpload;
    @Column(name = "dlupload")
    private String dLUpload;
    private String gender;


    /*** END ****/

}
