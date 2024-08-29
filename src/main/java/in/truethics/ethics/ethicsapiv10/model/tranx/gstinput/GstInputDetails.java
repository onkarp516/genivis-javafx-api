package in.truethics.ethics.ethicsapiv10.model.tranx.gstinput;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductHsn;
import in.truethics.ethics.ethicsapiv10.model.master.TaxMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_gst_input_details_tbl")
public class GstInputDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gst_input_id", nullable = false)
    @JsonManagedReference
    private GstInputMaster gstInputMaster;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "hsn_id")
    @JsonManagedReference
    private ProductHsn productHsn;

    @ManyToOne
    @JoinColumn(name = "tax_id")
    @JsonManagedReference
    private TaxMaster taxMaster;


    private String particular;
    @Column(name = "hsn_no")
    private String hsnNo;
    private Double igst;
    private Double cgst;
    private Double sgst;
    private Double amount;
    private Double qty;
    @Column(name = "final_amt")
    private Double finalAmt;
    @Column(name = "base_amount")
    private Double baseAmount;
    private Boolean status;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
