package in.truethics.ethics.ethicsapiv10.model.master;

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
@Table(name = "area_head_tbl")
public class AreaHead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "middle_name")
    private String middleName;
    @Column(name = "last_name")
    private String lastName;
    private String email;
    @Column(name = "mobile_number")
    private String mobileNumber;
    @Column(name = "whatsapp_number")
    private String whatsappNumber;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    private String address;
    private String gender;
    @Column(name = "permenant_address")
    private String permenantAddress;
    @Column(name = "is_same_address")
    private Boolean isSameAddress;
    private String pincode;
    @Column(name = "corp_pincode")
    private String corpPincode;
    private String city;
    @Column(name = "corporate_city")
    private String corporateCity;
    private String area;
    @Column(name = "corporate_area")
    private String corporateArea;
    @Column(name = "temporary_address")
    private String temporaryAddress;
    @Column(name = "aadhar_card_no")
    private String aadharCardNo;
    @Column(name = "aadhar_card_file")
    private String aadharCardFile;
    @Column(name = "pan_card_no")
    private String panCardNo;
    @Column(name = "pan_card_file")
    private String panCardFile;
    @Column(name = "bank_acc_name")
    private String bankAccName;
    @Column(name = "bank_acc_no")
    private String bankAccNo;
    @Column(name = "bank_accifsc")
    private String bankAccIFSC;
    @Column(name = "bank_acc_file")
    private String bankAccFile;
    @Column(name = "area_role")
    private String areaRole;
    @Column(name = "state_code")
    private String stateCode;
    @Column(name = "zone_code")
    private String zoneCode;
    @Column(name = "region_code")
    private String regionCode;
    @Column(name = "district_code")
    private String districtCode;
    @Column(name = "partner_deed_file")
    private String partnerDeedFile;
    @Column (name = "zone_state_head")
    private String zoneStateHead;
    @Column (name = "region_zone_head_id")
    private String regionZoneHeadId;
    @Column (name = "region_state_head_id")
    private String regionStateHeadId;
    @Column(name = "district_region_head_id")
    private String districtRegionHeadId;
    @Column(name = "district_zone_head_id")
    private String districtZoneHeadId;
    @Column(name = "district_state_head_id")
    private String districtStateHeadId;
    private String username;
    private String password;
    private String plain_password;
    private Boolean status;
    @ManyToOne
    @JoinColumn(name = "country_id")
    @JsonManagedReference
    private Country country;

    @ManyToOne
    @JoinColumn(name = "state_id")
    @JsonManagedReference
    private State state;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    @JsonManagedReference
    private Zone zone;

    @ManyToOne
    @JoinColumn(name = "region_id")
    @JsonManagedReference
    private Region region;

    @ManyToOne
    @JoinColumn(name = "district_id")
    @JsonManagedReference
    private District district;

    @ManyToOne
    @JoinColumn(name = "area_id")
    @JsonManagedReference
    private PincodeMaster areaMaster;

    @ManyToOne
    @JoinColumn(name = "corporate_area_id")
    @JsonManagedReference
    private PincodeMaster corporateAreaMaster;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @Column(name = "outlet_id")
    private Long outletId;
    @Column(name = "branch_id")
    private Long branchId;


}
