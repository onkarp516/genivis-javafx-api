package in.truethics.ethics.ethicsapiv10.model.master;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "commission_master_tbl")
public class CommissionMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "role_type")
    private String roleType;
    @Column(name = "franchise_level")
    private String franchiseLevel;
    @Column(name = "product_level")
    private String productLevel;
    private Boolean status;
    @Column(name = "tds_per")
    private Double tdsPer;//Configuration paramter of TDS% to calculate TDS % on incentive during JV Entry of Partners


}