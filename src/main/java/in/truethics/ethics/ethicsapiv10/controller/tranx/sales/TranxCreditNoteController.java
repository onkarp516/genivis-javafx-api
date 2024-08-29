package in.truethics.ethics.ethicsapiv10.controller.tranx.sales;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.sales.TranxCreditNoteNewReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController


public class TranxCreditNoteController {
    //    @Autowired
//    private TranxCreditNoteServices tranxCreditNoteServices;
    @Autowired
    private TranxCreditNoteNewReferenceService creditNoteNewReferenceService;

    /* get last records of voucher CreditNote   */
    @GetMapping(path = "/get_last_record_creditnote")
    public Object creditNoteLastRecord(HttpServletRequest request) {
        JsonObject result = creditNoteNewReferenceService.creditNoteLastRecord(request);
        return result.toString();
    }

    /* list all opened credit notes of type = "futures" (only for sales invoice) */
    @PostMapping(path = "/list_tranx_credit_notes")
    public Object tranxCreditNoteList(HttpServletRequest request) {
        JsonObject result = creditNoteNewReferenceService.tranxCreditNoteList(request);
        return result.toString();
    }

    @GetMapping(path = "/list_credit_notes")
    public Object creditListbyOutlet(HttpServletRequest request) {
        JsonObject result = creditNoteNewReferenceService.creditListbyOutlet(request);
        return result.toString();
    }

    /* create debit note*/
    @PostMapping(path = "/create_credit")
    public Object createcredit(HttpServletRequest request) {
        JsonObject result = creditNoteNewReferenceService.createcredit(request);
        return result.toString();
    }

    /*get voucher credit note note by id*/
    @PostMapping(path = "/get_credit_note_by_id")
    public Object getCreditById(HttpServletRequest request) {
        JsonObject result = creditNoteNewReferenceService.getCreditById(request);
        return result.toString();

    }

    /*update voucher credit note note*/
    @PostMapping(path = "/update_credit_note")
    public Object updatecredit(HttpServletRequest request) {
        JsonObject result = creditNoteNewReferenceService.updatecredit(request);
        return result.toString();
    }

    /* delete voucher creditNote note*/
    @PostMapping(path = "/delete_credit_note")
    public Object deletecreditnote(HttpServletRequest request) {
        JsonObject array = creditNoteNewReferenceService.deletecreditnote(request);
        return array.toString();
    }

}

