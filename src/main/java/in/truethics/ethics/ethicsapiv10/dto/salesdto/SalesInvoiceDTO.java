package in.truethics.ethics.ethicsapiv10.dto.salesdto;

import in.truethics.ethics.ethicsapiv10.model.master.LedgerMaster;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SalesInvoiceDTO {
    private Long id;
    private String invoice_no;
    private String invoice_date;
    private Long sale_serial_number;
    private Double total_amount;
    private String sundry_debtor_name;
    private Long sundry_debtor_id;
    private String sale_account_name;
    private String narration;
    private Double tax_amt;
    private Double taxable_amt;
    private String payment_mode;
    private String referenceNo;
    private String referenceType;

    private double totalcgst;

      private double totalsgst;

    private double totaligst;
//    private double total_base_amount;

    private String transactionTrackingNo;//transactionTrackingNo;
    private String tranxCode;
}
