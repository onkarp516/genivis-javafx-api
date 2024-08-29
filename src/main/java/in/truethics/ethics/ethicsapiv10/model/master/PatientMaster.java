package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "patient_master_tbl")
public class PatientMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "outlet_id")
    private Long outletId;
    @Column(name = "patient_name")
    private String patientName;
    @Column(name = "patient_address")
    private String patientAddress;
    @Column(name = "mobile_number")
    private String mobileNumber;
    private Long age;
    private Long weight;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Column(name = "id_no")
    private String idNo;
    private String gender;
    private Long pincode;
    @Column(name = "tb_diagnosis_date")
    private LocalDate tbDiagnosisDate;
    @Column(name = "tb_treatment_initiation_date")
    private LocalDate tbTreatmentInitiationDate;
    @Column(name = "blood_group")
    private String bloodGroup;
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
    @Column(name = "patient_weight")
    private Double patientWeight;
    @Column(name = "patient_code")
    private String patientCode;

}
