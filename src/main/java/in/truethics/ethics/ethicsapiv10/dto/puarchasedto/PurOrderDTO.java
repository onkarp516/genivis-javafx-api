package in.truethics.ethics.ethicsapiv10.dto.puarchasedto;
import lombok.Data;
@Data

public class PurOrderDTO {

    private Long id;
    private String invoice_no;
    private String invoice_date;
    private String transaction_date;
    private Double total_amount;
    private String sundry_creditor_name;
    private Long sundry_creditor_id;
    private String supplier_code;
    private String narration;
    private String purchase_order_status;
    private String purchase_account_name;
    private Double tax_amt;
    private Double taxable_amt;
    private Double totaligst;
    private Double totalsgst;
    private Double totalcgst;

    private String transactionTrackingNo;//transactionTrackingNo;
    private String tranxCode;
    private String orderStatus;
}
