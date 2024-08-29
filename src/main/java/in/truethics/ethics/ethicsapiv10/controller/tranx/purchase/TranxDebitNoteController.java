package in.truethics.ethics.ethicsapiv10.controller.tranx.purchase;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.purchase.TranxDebitNoteNewReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TranxDebitNoteController {

    @Autowired
    private TranxDebitNoteNewReferenceService service;

    /* get last records of voucher DebitNote   */
    @GetMapping(path = "/get_last_record_debitnote")
    public Object debitNoteLastRecord(HttpServletRequest request) {
        JsonObject result = service.debitNoteLastRecord(request);
        return result.toString();
    }

    /* list all opened debit notes of type = "futures" (only for Purchase invoice) */
    @PostMapping(path = "/list_tranx_debites_notes")
    public Object tranxDebitNoteList(HttpServletRequest request) {
        JsonObject result = service.tranxDebitNoteList(request);
        return result.toString();

    }

//    @GetMapping(path = "/list_debit_notes")
//    public Object debitListbyOutlet(HttpServletRequest request) {
//        JsonObject result = service.debitListbyOutlet(request);
//        return result.toString();
//
//    }
    //start
   @PostMapping(path = "/list_debit_notes")
    public Object debitListbyOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {

    return service.debitListbyOutlet(request, req);

}
    //end

    /* create debit note*/
    @PostMapping(path = "/create_debit")
    public Object createdebit(HttpServletRequest request) {
        JsonObject result = service.createdebit(request);
        return result.toString();

    }

    /*get voucher debitnote note by id*/
    @PostMapping(path = "/get_debit_note_by_id")
    public Object getDebitById(HttpServletRequest request) {
        JsonObject array = service.getDebitById(request);
        return array.toString();
    }

    /*update voucher debitnote note*/
    @PostMapping(path = "/update_debit_note")
    public Object updatedebit(HttpServletRequest request) {
        JsonObject array = service.updatedebit(request);
        return array.toString();
    }

    /* delete voucher debitnote note*/
    @PostMapping(path = "/delete_debit_note")
    public Object deletedebitnote(HttpServletRequest request) {
        JsonObject array = service.deletedebitnote(request);
        return array.toString();
    }

    /* list all opened debit notes of type = "futures" (only for Purchase invoice) */
   /* @PostMapping(path = "/list_tranx_debites_notes_new")
    public Object tranxDebitNoteDetailsList(HttpServletRequest request) {
        JsonObject result = service.tranxDebitNoteDetailsList(request);
        return result.toString();

    }
*/
}
