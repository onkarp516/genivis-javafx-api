package in.truethics.ethics.ethicsapiv10.dto.accountentrydto;

import lombok.Data;
@Data
public class CreditNoteDTO {

    private String source;
    private Long id;
    private String credit_note_no;
    private String transaction_dt;
    private String narration;
    private String ledger_name;
    private Double total_amount;
}
