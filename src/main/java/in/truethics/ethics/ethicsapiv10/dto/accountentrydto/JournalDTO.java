package in.truethics.ethics.ethicsapiv10.dto.accountentrydto;

import lombok.Data;
@Data
public class JournalDTO {

    private Long id;
    private String journal_code;
    private String transaction_dt;
    private  Double journal_sr_no;
    private String ledger_name;
    private String narration;
    private Double total_amount;
}
