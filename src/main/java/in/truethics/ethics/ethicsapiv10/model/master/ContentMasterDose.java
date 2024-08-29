package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "content_master_dose_tbl")
public class ContentMasterDose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "content_name_dose")
    private String contentNameDose;
    private Boolean status;

}
