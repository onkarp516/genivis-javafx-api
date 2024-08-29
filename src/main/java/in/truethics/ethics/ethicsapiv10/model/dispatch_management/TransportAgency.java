package in.truethics.ethics.ethicsapiv10.model.dispatch_management;


import antlr.NameSpace;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transport_agency_tbl")
public class TransportAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "transport_agency_name")
    private String transportAgencyName;
    private String address;
    @Column(name = "contact_no")
    private Long contactNo;
    @Column(name = "contact_person")
    private String contactPerson;
//    private String city;
//    private String state;
    private String pincode;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JsonIgnoreProperties(value = {"transportAgency", "hibernateLazyInitializer"})
//    @JoinColumn(name = "country_id", nullable = false)
//    private Country country;

    @Column(name = "country_id")
    private Long countryId;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JsonIgnoreProperties(value = {"transportAgency", "hibernateLazyInitializer"})
//    @JoinColumn(name = "state_id", nullable = false)
//    private State state;

    @Column(name = "state_id")
    private Long stateId;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JsonIgnoreProperties(value = {"transportAgency", "hibernateLazyInitializer"})
//    @JoinColumn(name = "city_id", nullable = false)
//    private City city;

    @Column(name = "city_id")
    private Long cityId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    private Boolean status;

//    @ManyToOne
//    @JoinColumn(name = "branch_id")
//    @JsonManagedReference
//    private Branch branch;
@Column(name = "branch_id")
    private Long branchId;

//    @ManyToOne
//    @JoinColumn(name = "outlet_id")
//    @JsonManagedReference
//    private Outlet outlet;

    @Column(name = "outlet_id")
    private Long outletId;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;


//    public void setCountry(Country country) {
//    }
//
//    public void setCity(String valueOf) {
//    }
//
//    public void setState(String valueOf) {
//    }
}
