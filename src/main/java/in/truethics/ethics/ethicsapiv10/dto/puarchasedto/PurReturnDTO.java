package in.truethics.ethics.ethicsapiv10.dto.puarchasedto;

import lombok.Data;
@Data

public class PurReturnDTO {
    private Long id;
    private String pur_return_no;
    private String transaction_date;
    private String purchase_return_date;
    private  Long purchase_return_serial_number;
    private Double total_amount;
    private String sundry_creditor_name;
    private Long sundry_creditor_id;
    private Double tax_amt;
    private Double taxable_amt;
    private String purchase_account_name;
    private String invoice_no;
    private String narration;
    private String transactionTrackingNo;//transactionTrackingNo;
    private String tranxCode;
}
