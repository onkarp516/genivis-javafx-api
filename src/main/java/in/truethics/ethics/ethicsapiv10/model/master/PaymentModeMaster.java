package in.truethics.ethics.ethicsapiv10.model.master;

import com.fasterxml.jackson.annotation.JsonBackReference;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstinput.GstInputMaster;
import in.truethics.ethics.ethicsapiv10.model.tranx.gstouput.GstOutputMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payment_mode_tbl")
public class PaymentModeMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payment_mode")
    private String paymentMode;
    private Boolean status;
    @Column(name = "outlet_id")
    private Long outletId;
    @Column(name = "branch_id")
    private Long branchId;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstInputMaster> gstInputMasters;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<GstOutputMaster> gstOutputMasters;
}
