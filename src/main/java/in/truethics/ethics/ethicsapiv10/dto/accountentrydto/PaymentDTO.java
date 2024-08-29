package in.truethics.ethics.ethicsapiv10.dto.accountentrydto;

import lombok.Data;
@Data
public class PaymentDTO {
    private Long id;
    private  String payment_code;
    private String transaction_dt;
    private Double payment_sr_no;
    private Double total_amount;
    private String ledger_name;
    private String narration;

}
