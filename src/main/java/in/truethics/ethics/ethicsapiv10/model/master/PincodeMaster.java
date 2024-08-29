package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pincode_master_tbl")
public class PincodeMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pincode;
    private String district;
    private String state;
    @Column(name = "state_code")
    private String stateCode;
    private String area;
}
