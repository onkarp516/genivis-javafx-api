package in.truethics.ethics.ethicsapiv10.dto.accountentrydto;

import lombok.Data;
@Data
public class ContraDTO {

    private Long id;
    private String contra_code;
    private String transaction_dt;
    private Double contra_sr_no;
    private String narration;
    private String ledger_name;
    private Double total_amount;
}
