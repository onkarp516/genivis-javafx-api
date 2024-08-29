package in.truethics.ethics.ethicsapiv10.model.master;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "doctor_master_tbl")
public class DoctorMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "branch_id")
    private Long branchId;
    @Column(name = "outlet_id")
    private Long outletId;
    @Column(name = "doctor_name")
    private String doctorName;
    private String specialization;
    @Column(name = "hospital_name")
    private String hospitalName;
    @Column(name = "hospital_address")
    private String hospitalAddress;
    @Column(name = "mobile_number")
    private String mobileNumber;
    @Column(name = "qualification")
    private String qualification;
    @Column(name = "register_no")
    private String register;
    @Column(name = "commision")
    private Long commision;
    private Boolean status;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Long updatedBy;

}
