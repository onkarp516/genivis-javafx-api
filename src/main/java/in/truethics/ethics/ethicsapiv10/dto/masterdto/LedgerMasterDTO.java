package in.truethics.ethics.ethicsapiv10.dto.masterdto;
import lombok.Data;
@Data
public class LedgerMasterDTO {
    private Long id;
    private String foundations_name;
    private String  principle_name;
    private String subprinciple_name;
    private Boolean default_ledger;
    private String ledger_form_parameter_slug;
    private String ledger_name;
    private Double cr;

    private Double dr;

    private String company_name;



}
