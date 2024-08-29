package in.truethics.ethics.ethicsapiv10.dto.salesdto;

import lombok.Data;
@Data

public class SalesOrderDTO {
    private Long id;
    private   String bill_no;
    private String bill_date;
//    private Long sale_serial_number;
    private Double total_amount;
    private Double total_base_amount;
    private String sundry_debtors_name;
    private Long sundry_debtors_id;
    private String sale_account_name;
    private String narration;
    private Double tax_amt;
    private Double taxable_amt;
//    private String payment_mode;
    private String sales_order_status;
    private String referenceNo;
    private String referenceType;
    private String transactionTrackingNo;//transactionTrackingNo;
    private String tranxCode;
}
