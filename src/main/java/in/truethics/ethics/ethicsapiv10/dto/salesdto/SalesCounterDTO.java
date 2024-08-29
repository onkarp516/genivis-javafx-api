package in.truethics.ethics.ethicsapiv10.dto.salesdto;
import lombok.Data;

import java.time.LocalDate;

@Data

public class SalesCounterDTO {
    private Long id;
    private String invoice_no;
    private String customer_name;
    private Double total_amount;
    private String transaction_date;
    private Long mobile_no;
    private String narrations;
    private Long total_products;
    private Double taxable;
    private Double gst;
    private String transactionTrackingNo;//transactionTrackingNo;
    private String tranxCode;
}
