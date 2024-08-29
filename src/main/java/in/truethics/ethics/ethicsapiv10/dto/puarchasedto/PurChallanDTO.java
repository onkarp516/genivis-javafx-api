package in.truethics.ethics.ethicsapiv10.dto.puarchasedto;
import lombok.Data;
@Data

public class PurChallanDTO {
    private Long id;
    private String invoice_no;
    private String invoice_date;
    private String transaction_date;
    private Double total_amount;
    private String sundry_creditor_name;
    private Long sundry_creditor_id;
    private String supplier_code;
    private String narration;
    private String purchase_account_name;
    private String purchase_challan_status;
    private  Double tax_amt;
    private Double taxable_amt;
    private String referenceNo;
    private String referenceType;
    private String transactionTrackingNo;//transactionTrackingNo;
    private String tranxCode;
}
