package in.truethics.ethics.ethicsapiv10.dto.accountentrydto;

import lombok.Data;
@Data
public class ReceiptDTO {
    private Long id;
    private String receipt_code;
    private String transaction_dt;
    private String narration;
    private Double receipt_sr_no;
    private Double total_amount;
    private Boolean auto_generated;
    private String ledger_name;
    private Boolean isFrReceipt;
}
