package in.truethics.ethics.ethicsapiv10.model.dispatch_management;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
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
@Table(name = "delivery_boy_tbl")
public class DeliveryBoy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "mobile_no")
    private Long mobileNo;
    private String address;
    @Column(name = "identity_document")
    private String identityDocument;
//    private String imagePath;


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

//    @JsonManagedReference
//    private Outlet outlet;

    @Column(name = "outlet_id")
    private Long outletId;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;



}
