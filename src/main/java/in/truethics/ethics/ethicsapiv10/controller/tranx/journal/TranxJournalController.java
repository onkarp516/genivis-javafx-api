package in.truethics.ethics.ethicsapiv10.controller.tranx.journal;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.tranx_service.journal.TranxJournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController

public class TranxJournalController {
    @Autowired
    private TranxJournalService service;

    /* get last records of voucher journal   */
    @GetMapping(path = "/get_last_record_journal")
    public Object journalLastRecord(HttpServletRequest request) {
        JsonObject result = service.journalLastRecord(request);
        return result.toString();
    }

    /* Create journal */
    @PostMapping(path = "/create_journal")
    public Object createJournal(HttpServletRequest request) {
        JsonObject array = service.createJournal(request);
        return array.toString();
    }

    //start of journal list
    @PostMapping(path = "/get_journal_list_by_outlet")
    public Object journalListbyOutlet(@RequestBody Map<String, String> request, HttpServletRequest req) {

        return service.journalListbyOutlet(request, req);
    }
    //end of journal list


    /* Get  ledger details of journal   */
    @GetMapping(path = "/get_ledger_list_by_outlet")
    public Object getledgerDetails(HttpServletRequest request) {
        JsonObject object = service.getledgerDetails(request);
        return object.toString();
    }

    /*Update journal*/
    @PostMapping(path = "/update_journal")
    public Object updateJournal(HttpServletRequest request) {
        JsonObject array = service.updateJournal(request);
        return array.toString();
    }

    /*get journal by id*/
    @PostMapping(path = "/get_journal_by_id")
    public Object getjournalById(HttpServletRequest request) {
        JsonObject array = service.getjournalById(request);
        return array.toString();
    }

    /***** Delete Journal  ****/
    @PostMapping(path = "/delete_journal")
    public Object deleteJournal(HttpServletRequest request) {
        JsonObject object = service.deleteJournal(request);
        return object.toString();
    }


}
