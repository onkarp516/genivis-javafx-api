package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "content_pkg_master_tbl")
public class ContentPackageMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "content_package_name")
    private String contentPackageName;
    private Boolean status;

}
