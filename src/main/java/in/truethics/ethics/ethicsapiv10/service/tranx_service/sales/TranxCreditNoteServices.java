package in.truethics.ethics.ethicsapiv10.service.tranx_service.sales;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.common.GenerateDates;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.tranx_repository.creditnote_repository.TranxCreditNoteRepository;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service

public class TranxCreditNoteServices {
    @Autowired
    private TranxCreditNoteRepository tranxCreditNoteRepository;
    @Autowired
    private JwtTokenUtil jwtRequestFilter;
    public JsonObject creditNoteLastRecord(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(
                request.getHeader("Authorization").substring(7));
        Long count = tranxCreditNoteRepository.findCreditNoteLastRecord(users.getOutlet().getId());
        String serailNo = String.format("%05d", count + 1);// 5 digit serial number
        GenerateDates generateDates = new GenerateDates();
        String currentMonth = generateDates.getCurrentMonth().substring(0, 3);
        String creditNote = "CDTN" + currentMonth + serailNo;
        JsonObject result = new JsonObject();
        result.addProperty("message", "success");
        result.addProperty("responseStatus", HttpStatus.OK.value());
        result.addProperty("count", count + 1);
        result.addProperty("creditnoteNo", creditNote);
        return result;
    }
}
