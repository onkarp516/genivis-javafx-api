package in.truethics.ethics.ethicsapiv10.model.appconfig;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "system_config_parameter_tbl")
public class SystemConfigParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "display_name")
    private String displayName;
    private String slug;
    @Column(name = "is_label")
    private Boolean isLabel;
    private Boolean status;
    
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<AppConfig> appConfigs;

}
