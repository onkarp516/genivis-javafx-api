package in.truethics.ethics.ethicsapiv10.dto.puarchasedto;
import lombok.Data;
@Data
public class PurInvoiceDTO {
    private Long id;
    private String invoice_no;
    private String invoice_date;
    private String transaction_date;
    private Long purchase_serial_number;
    private Double total_amount;
    private String narration;
    private String supplier_code;
    private String sundry_creditor_name;
    private Long sundry_creditor_id;
    private String purchase_account_name;
    private Double tax_amt;
    private Double taxable_amt;
    private String referenceNo;
    private String referenceType;
    private String transactionTrackingNo;//transactionTrackingNo;
    private String tranxCode;
}
