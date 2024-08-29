package in.truethics.ethics.ethicsapiv10.dto.salesdto;

import lombok.Data;
@Data

public class SalesReturnDTO {
    private Long id;
    private String sales_return_no;
    private String transaction_date;
    private Double tax_amt;
    private Double taxable_amt;
    private Long sales_return_serial_number;
    private Double total_amount;
    private String sundry_debtor_name;
    private Long sundry_debtor_id;
    private String sales_account_name;
    private String invoice_no;
    private String narration;
    private String transactionTrackingNo;//transactionTrackingNo;
    private String tranxCode;
}
