package in.truethics.ethics.ethicsapiv10.dto.salesdto;
import lombok.Data;
@Data

public class SalesChallanDTO {

    private Long id;
    private String bill_no;
    private String bill_date;
    private String narration;
    private String sales_challan_status;
    private String sale_account_name;
    private Long sundry_debtors_id;
    private String sundry_debtors_name;
    private Double tax_amt;
    private Double taxable_amt;
    private Double total_base_amount;
    private Double total_amount;
    private String referenceNo;
    private String referenceType;
    private String transactionTrackingNo;//transactionTrackingNo;
    private String tranxCode;

}
