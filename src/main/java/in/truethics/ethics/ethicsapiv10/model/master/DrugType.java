package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.Data;

import javax.persistence.*;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "drug_type_tbl")
public class DrugType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "drug_name")
    private String drugName;
    private Boolean status;
}
